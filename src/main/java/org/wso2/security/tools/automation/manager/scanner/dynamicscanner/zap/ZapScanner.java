/*
 * Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.zap;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.scanner.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.scanner.dynamicscanner.ProductManagerEntity;
import org.wso2.security.tools.automation.manager.entity.scanner.dynamicscanner.ZapEntity;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.HttpsRequestHandler;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScanner;
import org.wso2.security.tools.automation.manager.service.DynamicScannerService;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main ZAP scanning methods
 *
 * @author Deshani Geethika
 */
public class ZapScanner implements DynamicScanner {

    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";
    private static final String POST = "POST";
    private static final String CONTEXT_NAME = "new context";
    private static final String SESSION_NAME = "new session";

    private final static Logger LOGGER = LoggerFactory.getLogger(ZapScanner.class);

    private String contextId;
    private URI productUriRelativeToZap;

    private int id;
    private String ipAddress;
    private String fileUploadLocation;
    private boolean isFileUpload;
    private File urlListFile;

    private String wso2ServerHost;
    private int wso2ServerPort;

    private DynamicScannerService dynamicScannerService;
    private ZapEntity zap;

    public ZapScanner() {
        dynamicScannerService = ApplicationContextUtils.getApplicationContext().getBean(DynamicScannerService.class);
    }

    @Override
    public void init(String userId, String ipAddress, boolean isFileUpload, String fileUploadLocation, String
            urlListFileName,
                     String wso2ServerHost, int wso2ServerPort, String scannerHost, int scannerPort) {

        this.ipAddress = ipAddress;
        this.isFileUpload = isFileUpload;
        this.fileUploadLocation = fileUploadLocation;
        this.urlListFile = new File(fileUploadLocation + File.separator + urlListFileName);
        this.wso2ServerHost = wso2ServerHost;
        this.wso2ServerPort = wso2ServerPort;

        zap = new ZapEntity();
        zap.setUserId(userId);
        zap.setStatus(ScannerProperties.getStatusInitiated());
        dynamicScannerService.save(zap);
        id = zap.getId();
    }

    @Override
    public int calculateDynamicScannerPort(int id) {
        if (5000 + id > 20000) {
            id = 1;
        }
        return (5000 + id) % 20000;
    }

    @Override
    public DynamicScannerEntity startContainer() {
        int port = calculateDynamicScannerPort(id);

        List<String> command = Arrays.asList("zap.sh", "-daemon", "-config", "api.disablekey=true", "-config",
                "api.addrs.addr.name=.*", "-config", "api.addrs.addr.regex=true", "-port", String.valueOf(port),
                "-host", "0.0.0.0");

        String containerId = DockerHandler.createContainer(ScannerProperties.getZapDockerImage(), ipAddress,
                String.valueOf(port), String.valueOf(port), command, null);

        if (containerId != null) {
            String createdTime = new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
            zap.setContainerId(containerId);
            zap.setIpAddress(ipAddress);
            zap.setContainerPort(port);
            zap.setHostPort(port);
            zap.setStatus(ScannerProperties.getStatusCreated());
            zap.setCreatedTime(createdTime);
            dynamicScannerService.save(zap);

            if (DockerHandler.startContainer(containerId)) {
                zap.setStatus(ScannerProperties.getStatusRunning());
                zap.setDockerIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                dynamicScannerService.save(zap);
                return zap;
            }
        }
        return null;
    }

    @Override
    public void startScan(DynamicScannerEntity dynamicScannerEntity, ProductManagerEntity productManagerEntity) {
        try {
            String productHostRelativeToZap;
            String productHostRelativeToThis;
            int productPort;

            if (isFileUpload) {
                productHostRelativeToZap = productManagerEntity.getIpAddress();
                productHostRelativeToThis = productManagerEntity.getIpAddress();
                productPort = ScannerProperties.getProductManagerProductPort();
            } else {
                productHostRelativeToZap = wso2ServerHost;
                productHostRelativeToThis = wso2ServerHost;
                productPort = wso2ServerPort;
            }

            Map<String, Object> loginCredentials = new HashMap<>();

            loginCredentials.put(ScannerProperties.getWso2ProductKeyUsername(),
                    ScannerProperties.getWso2ProductValueUsername());
            loginCredentials.put(ScannerProperties.getWso2ProductKeyPassword(),
                    ScannerProperties.getWso2ProductValuePassword());

            ZapClient zapClient = new ZapClient(dynamicScannerEntity.getDockerIpAddress(),
                    dynamicScannerEntity.getHostPort(), HTTP_SCHEME);

            productUriRelativeToZap = new URIBuilder().setHost(productHostRelativeToZap).setPort(productPort)
                    .setScheme(HTTPS_SCHEME).build();

            String site = productHostRelativeToZap + ":" + productPort;
            System.out.println(site);
            LOGGER.info("Starting ZAP scanning process...");

            //Create new context
            HttpResponse createNewContextResponse = zapClient.createNewContext(CONTEXT_NAME, false);
            contextId = extractJsonValue(createNewContextResponse, "contextId");

            HttpResponse includeInContextResponse = zapClient.includeInContext(CONTEXT_NAME, "\\Q" +
                    productUriRelativeToZap.toString() + "\\E.*", false);
            LOGGER.info("Include in context response: " + HttpRequestHandler.printResponse(includeInContextResponse));

            //Create an empty session
            HttpResponse createEmptySessionResponse = zapClient.createEmptySession(site, SESSION_NAME, false);
            LOGGER.info("Creating empty session " + HttpRequestHandler.printResponse(createEmptySessionResponse));

            //login to wso2 server
            Map<String, String> props = new HashMap<>();
            props.put("Content-Type", "text/plain");

            URI loginUri = (new URIBuilder()).setHost(productHostRelativeToThis).setPort(productPort)
                    .setScheme("https").setPath(ScannerProperties.getWso2ProductManagementConsoleLoginUrl()).build();
            LOGGER.info("URI to login to wso2server: " + loginUri.toString());

            HttpsURLConnection httpsURLConnection = HttpsRequestHandler.sendRequest(loginUri.toString(), props,
                    loginCredentials, POST);
            List<String> setCookieResponseList = HttpsRequestHandler
                    .getResponseValue("Set-Cookie", httpsURLConnection);

            assert setCookieResponseList != null;
            String setCookieResponse = setCookieResponseList.get(0);
            String jSessionId = setCookieResponse.substring(setCookieResponse.indexOf("=") + 1,
                    setCookieResponse.indexOf(";"));

            HttpResponse setSessionTokenResponse = zapClient.setSessionTokenValue(site, SESSION_NAME,
                    "JSESSIONID", jSessionId, false);
            LOGGER.info("Setting JSESSIONID to the newly created session: " +
                    HttpRequestHandler.printResponse(setSessionTokenResponse));

            //Exclude logout url from spider
            URI logoutUri = (new URIBuilder()).setHost(productHostRelativeToThis).setPort(productPort)
                    .setScheme("https").setPath(ScannerProperties.getWso2ProductManagementConsoleLogoutUrl()).build();
            LOGGER.info("Logout URI: " + logoutUri.toString());

            HttpsURLConnection httpsURLConnectionLogout = HttpsRequestHandler.sendRequest(logoutUri.toString(), props,
                    null, POST);
            LOGGER.info("Response of sending logout request to server: " +
                    HttpsRequestHandler.printResponse(httpsURLConnectionLogout));

            logoutUri = (new URIBuilder()).setHost(productHostRelativeToZap).setPort(productPort).setScheme("https")
                    .setPath(ScannerProperties.getWso2ProductManagementConsoleLogoutUrl()).build();

            HttpResponse excludeFromSpiderResponse = zapClient.excludeFromSpider(logoutUri.toString(), false);
            LOGGER.info("Exclude logout from spider response: " +
                    HttpRequestHandler.printResponse(excludeFromSpiderResponse));

            createEmptySessionResponse = zapClient.createEmptySession(site, SESSION_NAME + "2", false);
            LOGGER.info("Creating empty session " + HttpRequestHandler.printResponse(createEmptySessionResponse));

            httpsURLConnection = HttpsRequestHandler.sendRequest(loginUri.toString(), props, loginCredentials, POST);
            setCookieResponseList = HttpsRequestHandler.getResponseValue("Set-Cookie", httpsURLConnection);

            assert setCookieResponseList != null;
            setCookieResponse = setCookieResponseList.get(0);
            jSessionId = setCookieResponse.substring(setCookieResponse.indexOf("=") + 1, setCookieResponse.indexOf(";" +
                    ""));

            setSessionTokenResponse = zapClient.setSessionTokenValue(site, SESSION_NAME + "2",
                    "JSESSIONID", jSessionId, false);
            LOGGER.info("Setting JSESSIONID to the newly created session: "
                    + HttpRequestHandler.printResponse(setSessionTokenResponse));

            runSpider(zapClient);
            runAjaxSpider(zapClient);
            runActiveScan(dynamicScannerEntity, zapClient);

            HttpResponse generatedHtmlReport = zapClient.generateHtmlReport(false);
            HttpRequestHandler.saveResponseToFile(generatedHtmlReport, new File(fileUploadLocation +
                    File.separator + ScannerProperties.getZapReport()));

        } catch (Exception e) {
            LOGGER.error("Error occurred while starting scan", e);
        }
    }

    private void runSpider(ZapClient zapClient) {
        try {
            BufferedReader bufferedReader;
            ArrayList<String> spiderScanIds = new ArrayList<>();

            bufferedReader = new BufferedReader(new FileReader(urlListFile));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                LOGGER.info("Reading URL list of wso2 product: " + productUriRelativeToZap.toString() + line);

                HttpResponse spiderResponse = zapClient.spider(productUriRelativeToZap.toString() + line, "",
                        "", "", "", false);
                LOGGER.info("Spider HTTP Response");

                String scanId = extractJsonValue(spiderResponse, "scan");
                spiderScanIds.add(scanId);
                LOGGER.info("Adding ScanIds of Spider Scans to array: " + scanId);
            }

            for (String scanId : spiderScanIds) {
                HttpResponse spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                LOGGER.info("Sending request to check spider status");

                while (Integer.parseInt(extractJsonValue(spiderStatusResponse, "status")) < 100) {
                    spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                    LOGGER.info("Sending request to check spider status: " + spiderStatusResponse);
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException | URISyntaxException | IOException e) {
            LOGGER.error("Error occurred while running spider scan", e);
        }
    }

    private void runAjaxSpider(ZapClient zapClient) {
        try {
            HttpResponse ajaxSpiderResponse = zapClient.ajaxSpider(productUriRelativeToZap.toString(), "",
                    "", "", false);
            LOGGER.info("Starting Ajax spider: " + ajaxSpiderResponse);

            HttpResponse ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
            LOGGER.info("Ajax spider status: " + ajaxSpiderStatusResponse);

            while (!extractJsonValue(ajaxSpiderStatusResponse, "status").equals("stopped")) {
                ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
                LOGGER.info("Ajax spider status: " + ajaxSpiderStatusResponse);
                Thread.sleep(3000);
            }
        } catch (InterruptedException | IOException | URISyntaxException e) {
            LOGGER.error("Error occurred while running ajax spider", e);
        }
    }

    private void runActiveScan(DynamicScannerEntity dynamicScannerEntity, ZapClient zapClient) {
        int progress = 0;
        try {
            HttpResponse activeScanResponse = zapClient.activeScan(productUriRelativeToZap.toString(), "",
                    "", "", "", "", contextId, false);
            String activeScanId = extractJsonValue(activeScanResponse, "scan");

            LOGGER.info("Scan Id of active scan: " + activeScanId);
            Thread.sleep(500);

            HttpResponse activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
            progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));

            while (progress < 100) {
                activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
                progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));

                dynamicScannerService.updateScanStatus(dynamicScannerEntity.getContainerId(),
                        ScannerProperties.getStatusRunning(), progress);
                Thread.sleep(1000 * 60);
            }
            if (progress == 100) {
                dynamicScannerService.updateScanStatus(dynamicScannerEntity.getContainerId(),
                        ScannerProperties.getStatusCompleted(), progress);
            }
        } catch (InterruptedException | IOException | URISyntaxException e) {
            LOGGER.error("Error occurred while running active scan", e);
            dynamicScannerService.updateScanStatus(dynamicScannerEntity.getContainerId(),
                    ScannerProperties.getStatusFailed(), progress);
        }
    }

    private String extractJsonValue(HttpResponse httpResponse, String key) {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }
}
