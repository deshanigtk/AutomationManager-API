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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.zap;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased.zap.ZapEntity;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.HttpsRequestHandler;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.ContainerBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.service.dynamicscanner.DynamicScannerService;

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
 * The class {@code ZapScanner} implements the interface {@code DynamicScanner}. The main contract of this class is
 * to start a ZAP scanner (ZAP scanner is a docker container), and automate the ZAP scanning process by calling
 * {@link ZapClient} methods
 *
 * @author Deshani Geethika
 */
public class ZapScanner implements ContainerBasedDynamicScanner {
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";
    private static final String POST = "POST";
    private static final String CONTEXT_NAME = "context";
    private static final String SESSION_NAME_1 = "session_1";
    private static final String SESSION_NAME_2 = "session_2";
    private final static Logger LOGGER = LoggerFactory.getLogger(ZapScanner.class);
    private String contextId;
    private URI productUriRelativeToZap;
    private String ipAddress;
    private String fileUploadLocation;
    private File urlListFile;
    private DynamicScannerService dynamicScannerService;
    private ZapEntity zap;

    public ZapScanner() {
        dynamicScannerService = ApplicationContextUtils.getApplicationContext().getBean(DynamicScannerService.class);
        zap = new ZapEntity();
    }

    /**
     * Overrides the {@code init} method to initialize instance variables, initialize {@link ZapEntity} and store
     * {@link ZapEntity} object in database
     * {@inheritDoc}
     */
    @Override
    public void init(String userId, String ipAddress, String fileUploadLocation, String urlListFileName) {
        this.ipAddress = ipAddress;
        this.fileUploadLocation = fileUploadLocation;
        this.urlListFile = new File(fileUploadLocation + File.separator + urlListFileName);

        zap.setUserId(userId);
        zap.setStatus(ScannerProperties.getStatusInitiated());
        dynamicScannerService.save(zap);
    }

    @Override
    public DynamicScannerEntity startScanner() {
        int port = ContainerBasedDynamicScanner.calculateDynamicScannerPort(zap.getId());
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
    public void startScan(String productHostRelativeToScanner, String productHostRelativeToThis, int productPort) {
        try {
            Map<String, Object> loginCredentials = new HashMap<>();
            loginCredentials.put(ScannerProperties.getWso2ProductKeyUsername(), ScannerProperties
                    .getWso2ProductValueUsername());
            loginCredentials.put(ScannerProperties.getWso2ProductKeyPassword(), ScannerProperties
                    .getWso2ProductValuePassword());

            ZapClient zapClient = new ZapClient(zap.getDockerIpAddress(), zap.getHostPort(), HTTP_SCHEME);
            productUriRelativeToZap = new URIBuilder().setHost(productHostRelativeToScanner).setPort(productPort)
                    .setScheme(HTTPS_SCHEME).build();

            String site = productHostRelativeToScanner + ":" + productPort;
            LOGGER.info("Starting ZAP scanning process...");

            //Create new context
            HttpResponse createNewContextResponse = zapClient.createNewContext(CONTEXT_NAME, false);
            contextId = extractJsonValue(createNewContextResponse, "contextId");

            HttpResponse includeInContextResponse = zapClient.includeInContext(CONTEXT_NAME, "\\Q" +
                    productUriRelativeToZap.toString() + "\\E.*", false);
            LOGGER.info("Include in context response: " + HttpRequestHandler.printResponse(includeInContextResponse));

            //Create an empty session
            HttpResponse createEmptySessionResponse = zapClient.createEmptySession(site, SESSION_NAME_1, false);
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
                    .extractValueFromResponseHeader("Set-Cookie", httpsURLConnection);

            assert setCookieResponseList != null;
            String setCookieResponse = setCookieResponseList.get(0);
            String jSessionId = setCookieResponse.substring(setCookieResponse.indexOf("=") + 1,
                    setCookieResponse.indexOf(";"));

            HttpResponse setSessionTokenResponse = zapClient.setSessionTokenValue(site, SESSION_NAME_1,
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

            logoutUri = (new URIBuilder()).setHost(productHostRelativeToScanner).setPort(productPort).setScheme("https")
                    .setPath(ScannerProperties.getWso2ProductManagementConsoleLogoutUrl()).build();

            HttpResponse excludeFromSpiderResponse = zapClient.excludeFromSpider(logoutUri.toString(), false);
            LOGGER.info("Exclude logout from spider response: " +
                    HttpRequestHandler.printResponse(excludeFromSpiderResponse));

            createEmptySessionResponse = zapClient.createEmptySession(site, SESSION_NAME_2, false);
            LOGGER.info("Creating empty session " + HttpRequestHandler.printResponse(createEmptySessionResponse));

            httpsURLConnection = HttpsRequestHandler.sendRequest(loginUri.toString(), props, loginCredentials, POST);
            setCookieResponseList = HttpsRequestHandler.extractValueFromResponseHeader("Set-Cookie",
                    httpsURLConnection);

            assert setCookieResponseList != null;
            setCookieResponse = setCookieResponseList.get(0);
            jSessionId = setCookieResponse.substring(setCookieResponse.indexOf("=") + 1, setCookieResponse.indexOf(";" +
                    ""));

            setSessionTokenResponse = zapClient.setSessionTokenValue(site, SESSION_NAME_2,
                    "JSESSIONID", jSessionId, false);
            LOGGER.info("Setting JSESSIONID to the newly created session: "
                    + HttpRequestHandler.printResponse(setSessionTokenResponse));

            runSpider(zapClient);
            runAjaxSpider(zapClient);
            runActiveScan(zapClient);

            HttpResponse generatedHtmlReport = zapClient.generateHtmlReport(false);
            String reportFilePath = fileUploadLocation + File.separator + ScannerProperties.getZapReport();
            if (HttpRequestHandler.saveResponseToFile(generatedHtmlReport, new File(reportFilePath))) {
                dynamicScannerService.updateReportReady(zap.getContainerId(), true, reportFilePath);
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while starting scan", e);
        }
    }

    @Override
    public int getId() {
        return zap.getId();
    }

    private void runSpider(ZapClient zapClient) {
        try {
            BufferedReader bufferedReader;
            ArrayList<String> spiderScanIds = new ArrayList<>();
            bufferedReader = new BufferedReader(new FileReader(urlListFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                HttpResponse spiderResponse = zapClient.spider(productUriRelativeToZap.toString() + line, "",
                        "", "", "", false);
                String scanId = extractJsonValue(spiderResponse, "scan");
                spiderScanIds.add(scanId);
            }

            for (String scanId : spiderScanIds) {
                HttpResponse spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                LOGGER.info("Sending request to check spider status");

                while (Integer.parseInt(extractJsonValue(spiderStatusResponse, "status")) < 100) {
                    spiderStatusResponse = zapClient.spiderStatus(scanId, false);
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
            while (!extractJsonValue(ajaxSpiderStatusResponse, "status").equals("stopped")) {
                ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
                Thread.sleep(3000);
            }
        } catch (InterruptedException | IOException | URISyntaxException e) {
            LOGGER.error("Error occurred while running ajax spider", e);
        }
    }

    private void runActiveScan(ZapClient zapClient) {
        int progress = 0;
        try {
            HttpResponse activeScanResponse = zapClient.activeScan(productUriRelativeToZap.toString(), "",
                    "", "", "", "", contextId, false);
            String activeScanId = extractJsonValue(activeScanResponse, "scan");
            Thread.sleep(500);
            HttpResponse activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
            progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));

            while (progress < 100) {
                activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
                progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));

                dynamicScannerService.updateScanStatus(zap.getContainerId(),
                        ScannerProperties.getStatusRunning(), progress);
                Thread.sleep(1000 * 60);
            }
            if (progress == 100) {
                dynamicScannerService.updateScanStatus(zap.getContainerId(),
                        ScannerProperties.getStatusCompleted(), progress);
            }
        } catch (InterruptedException | IOException | URISyntaxException e) {
            LOGGER.error("Error occurred while running active scan", e);
            dynamicScannerService.updateScanStatus(zap.getContainerId(),
                    ScannerProperties.getStatusFailed(), progress);
        }
    }

    private String extractJsonValue(HttpResponse httpResponse, String key) {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }

}
