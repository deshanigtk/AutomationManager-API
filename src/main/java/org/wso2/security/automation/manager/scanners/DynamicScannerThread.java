package org.wso2.security.automation.manager.scanners;/*
*  Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.automation.manager.Constants;
import org.wso2.security.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.entity.Zap;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.service.DynamicScannerService;
import org.wso2.security.automation.manager.service.ZapService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DynamicScannerThread implements Runnable {

    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private boolean isFileUpload;
    private File zipFile;
    private File urlListFile;
    private String wso2ServerHost;
    private int wso2ServerPort;
    private boolean isAuthenticatedScan;

    private DynamicScannerService dynamicScannerService;
    private ZapService zapService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public DynamicScannerThread(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean isFileUpload,
                                String uploadLocation, String urlListFileName, String zipFileName, String wso2ServerHost, int wso2ServerPort,
                                boolean isAuthenticatedScan) {
        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        this.isFileUpload = isFileUpload;
        if (zipFileName != null) {
            this.zipFile = new File(uploadLocation + File.separator + zipFileName);
        }
        this.urlListFile = new File(uploadLocation + File.separator + urlListFileName);
        this.wso2ServerHost = wso2ServerHost;
        this.wso2ServerPort = wso2ServerPort;
        this.isAuthenticatedScan = isAuthenticatedScan;

        dynamicScannerService = ApplicationContextUtils.getApplicationContext().getBean(DynamicScannerService.class);
        zapService = ApplicationContextUtils.getApplicationContext().getBean(ZapService.class);
    }

    @Override
    public void run() {
        try {
            Zap zap = startZap();
            if (zap != null) {
                DynamicScanner dynamicScanner = startDynamicScanner(zap);
                if (dynamicScanner != null) {
                    if (dynamicScannerService.isDynamicScannerReady(dynamicScanner)) {
                        String startScanResponse = startScan(dynamicScanner, zap);
                        LOGGER.info("Start Dynamic Scan response: " + startScanResponse);
                    }
                } else {
                    LOGGER.error("Cannot start Dynamic Scanner");
                }
            } else {
                LOGGER.error("Cannot start ZAP");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Zap startZap() {
        Zap zap = new Zap();
        zap.setUserId(userId);
        zap.setName(testName);
        zap.setStatus("initiated");
        LOGGER.info("New zap record");
        zapService.save(zap);

        int port = calculateZapPort(zap.getId());

        List<String> command = Arrays.asList("zap.sh", "-daemon", "-config", "api.disablekey=true", "-config",
                "api.addrs.addr.testName=.*", "-config", "api.addrs.addr.regex=true", "-port", String.valueOf(port), "-host", "0.0.0.0");

        String containerId = DockerHandler.createContainer(Constants.ZAP_DOCKER_IMAGE, ipAddress, String.valueOf(port),
                String.valueOf(port), command, null);

        if (containerId != null) {
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            zap.setContainerId(containerId);
            zap.setIpAddress(ipAddress);
            zap.setContainerPort(port);
            zap.setHostPort(port);
            zap.setStatus("created");
            zap.setCreatedTime(createdTime);

            zapService.save(zap);

            if (DockerHandler.startContainer(containerId)) {
                zap.setStatus("running");
                zap.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                zapService.save(zap);
                return zap;
            }
        }
        return null;
    }

    private DynamicScanner startDynamicScanner(Zap zap) {
        DynamicScanner dynamicScanner = new DynamicScanner();
        dynamicScanner.setUserId(userId);
        dynamicScanner.setTestName(testName);
        dynamicScanner.setProductName(productName);
        dynamicScanner.setWumLevel(wumLevel);
        dynamicScanner.setRelatedZapId(zap.getContainerId());
        dynamicScanner.setStatus("initiated");
        dynamicScannerService.save(dynamicScanner);

        int port = calculateDynamicScannerPort(dynamicScanner.getId());

        String containerId = DockerHandler.createContainer(Constants.DYNAMIC_SCANNER_DOCKER_IMAGE, ipAddress, String.valueOf(port),
                String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            LOGGER.info("Container Id: " + containerId);
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            dynamicScanner.setContainerId(containerId);
            dynamicScanner.setIpAddress(ipAddress);
            dynamicScanner.setContainerPort(port);
            dynamicScanner.setHostPort(port);
            dynamicScanner.setStatus("created");
            dynamicScanner.setCreatedTime(createdTime);

            dynamicScannerService.save(dynamicScanner);

            if (DockerHandler.startContainer(containerId)) {
                dynamicScanner.setStatus("running");
                dynamicScanner.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                dynamicScannerService.save(dynamicScanner);
                return dynamicScanner;
            }
        }
        return null;
    }

    private String startScan(DynamicScanner dynamicScanner, Zap zap) {

        try {
            String productHostRelativeToZap;
            String productHostRelativeToDynamicScanner;
            int productPort;

            if (isFileUpload) {
                productHostRelativeToZap = dynamicScanner.getIpAddress();
                productHostRelativeToDynamicScanner = "localhost";
                productPort = 9443;
            } else {
                productHostRelativeToZap = wso2ServerHost;
                productHostRelativeToDynamicScanner = wso2ServerHost;
                productPort = wso2ServerPort;
            }

            if ((isFileUpload && zipFile != null) || (!isFileUpload && wso2ServerHost != null && wso2ServerPort != -1)) {
                URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
                        .setPort(dynamicScanner.getHostPort()).setScheme("http").setPath(Constants.DYNAMIC_SCANNER_START_SCAN)
                        .addParameter("automationManagerHost", Constants.AUTOMATION_MANAGER_HOST)
                        .addParameter("automationManagerPort", String.valueOf(Constants.AUTOMATION_MANAGER_PORT))
                        .addParameter("myContainerId", dynamicScanner.getContainerId())
                        .addParameter("isFileUpload", String.valueOf(isFileUpload))
                        .addParameter("zapHost", zap.getIpAddress())
                        .addParameter("zapPort", String.valueOf(zap.getHostPort()))
                        .addParameter("productHostRelativeToZap", productHostRelativeToZap)
                        .addParameter("productHostRelativeToThis", productHostRelativeToDynamicScanner)
                        .addParameter("productPort", String.valueOf(productPort))
                        .addParameter("isAuthenticatedScan", String.valueOf(isAuthenticatedScan))
                        .build();

                Map<String, File> files = new HashMap<>();
                if (zipFile != null) {
                    files.put("zipFile", zipFile);
                }
                files.put("urlListFile", urlListFile);

                HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, files, null);

                String message = HttpRequestHandler.printResponse(response);
                LOGGER.info("Start zap scan response: " + message);
                return message;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "Cannot execute ZAP scan command";
    }

    private int calculateZapPort(int id) {
        if (5000 + id > 20000) {
            id = 1;
        }
        return (5000 + id) % 20000;
    }

    private int calculateDynamicScannerPort(int id) {
        if (20000 + id > 40000) {
            id = 1;
        }
        return (20000 + id) % 40000;
    }
}
