package org.wso2.security.automation.manager.scanner.dynamic;/*
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
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.automation.manager.config.ScannerProperty;
import org.wso2.security.automation.manager.entity.ProductManagerEntity;
import org.wso2.security.automation.manager.entity.scanner.dynamic.DynamicScannerEntity;
import org.wso2.security.automation.manager.handler.DockerHandler;
import org.wso2.security.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.automation.manager.service.ProductManagerService;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class DynamicScanner implements Runnable {

    protected static final String STATUS_INITIATED = "initiated";
    protected static final String STATUS_CREATED = "created";
    protected static final String STATUS_RUNNING = "running";

    protected static final String DATE_PATTERN = "yyyy-MM-dd:HH.mm.ss";

    protected String userId;
    protected String testName;
    protected String ipAddress;
    protected String productName;
    protected String wumLevel;

    protected String fileUploadLocation;
    protected boolean isFileUpload;
    protected File zipFile;
    protected File urlListFile;

    protected String wso2ServerHost;
    protected int wso2ServerPort;

    protected String scannerHost;
    protected int scannerPort;

    private ProductManagerService productManagerService;

    private final static Logger LOGGER = LoggerFactory.getLogger(DynamicScanner.class);

    public DynamicScanner(String userId, String testName, String ipAddress, String productName, String wumLevel,
                          boolean isFileUpload, String fileUploadLocation, String urlListFileName, String zipFileName,
                          String wso2ServerHost, int wso2ServerPort, String scannerHost) {
        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        this.fileUploadLocation = fileUploadLocation;
        this.isFileUpload = isFileUpload;

        if (zipFileName != null) {
            this.zipFile = new File(fileUploadLocation + File.separator + zipFileName);
        }

        this.urlListFile = new File(fileUploadLocation + File.separator + urlListFileName);
        this.wso2ServerHost = wso2ServerHost;
        this.wso2ServerPort = wso2ServerPort;
        this.scannerHost = scannerHost;

        productManagerService = ApplicationContextUtils.getApplicationContext().getBean(ProductManagerService.class);
    }

    @Override
    public void run() {
        DynamicScannerEntity dynamicScannerEntity = startDynamicScanner();
        if (dynamicScannerEntity != null) {
            if (isFileUpload) {
                ProductManagerEntity productManagerEntity = startProductManager();
                if (productManagerEntity != null) {
                    if (hostAvailabilityCheck(productManagerEntity.getIpAddress(), productManagerEntity.getHostPort(), 12 * 3)) {
                        if (startWso2Server(productManagerEntity)) {
                            if (hostAvailabilityCheck(productManagerEntity.getIpAddress(), 9443, 12 * 5)) {
                                startScan(dynamicScannerEntity);
                            }
                        }
                    }
                }
            } else {
                if (hostAvailabilityCheck(wso2ServerHost, wso2ServerPort, 10)) {
                    startScan(dynamicScannerEntity);
                }
            }
        }
    }

    private ProductManagerEntity startProductManager() {
        ProductManagerEntity productManagerEntity = new ProductManagerEntity();
        productManagerEntity.setUserId(userId);
        productManagerEntity.setTestName(testName);
        productManagerEntity.setProductName(productName);
        productManagerEntity.setWumLevel(wumLevel);
        productManagerEntity.setStatus(STATUS_INITIATED);
        productManagerService.save(productManagerEntity);

        int port = calculateWso2ServerPort(productManagerEntity.getId());

        String containerId = DockerHandler.createContainer(ScannerProperty.getDynamicScannerDockerImage(), ipAddress,
                String.valueOf(port), String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat(DATE_PATTERN).format(new Date());
            productManagerEntity.setContainerId(containerId);
            productManagerEntity.setIpAddress(ipAddress);
            productManagerEntity.setContainerPort(port);
            productManagerEntity.setHostPort(port);
            productManagerEntity.setStatus(STATUS_CREATED);
            productManagerEntity.setCreatedTime(createdTime);

            productManagerService.save(productManagerEntity);

            if (DockerHandler.startContainer(containerId)) {
                productManagerEntity.setStatus(STATUS_RUNNING);
                productManagerEntity.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                productManagerService.save(productManagerEntity);
                return productManagerEntity;
            }
        }
        return null;
    }

    private boolean startWso2Server(ProductManagerEntity productManagerEntity) {
        try {
            if (isFileUpload && zipFile != null) {
                URI uri = (new URIBuilder()).setHost(productManagerEntity.getIpAddress())
                        .setPort(productManagerEntity.getHostPort()).setScheme("http")
                        .setPath(ScannerProperty.getProductManagerStartServer())
                        .addParameter("automationManagerHost", ScannerProperty.getAutomationManagerHost())
                        .addParameter("automationManagerPort", String.valueOf(ScannerProperty.getAutomationManagerPort()))
                        .addParameter("myContainerId", productManagerEntity.getContainerId())
                        .build();

                Map<String, File> files = new HashMap<>();
                files.put("zipFile", zipFile);

                HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, files, null);
                if (response != null) {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        return true;
                    }
                }
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Error occurred while executing start wso2server command", e);
        }
        return false;
    }

    protected int calculateDynamicScannerPort(int id) {
        if (5000 + id > 20000) {
            id = 1;
        }
        return (5000 + id) % 20000;
    }

    private int calculateWso2ServerPort(int id) {
        if (20000 + id > 40000) {
            id = 1;
        }
        return (20000 + id) % 40000;
    }

    protected boolean hostAvailabilityCheck(String host, int port, int times) {
        int i = 0;
        while (i < times) {
            LOGGER.info("Checking host availability...");
            try (Socket s = new Socket(host, port)) {
                LOGGER.info(host + ":" + port + " is available");
                return true;
            } catch (IOException e) {
                LOGGER.error(e.toString());
                try {
                    Thread.sleep(5000);
                    i++;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public abstract DynamicScannerEntity startDynamicScanner();

    public abstract void startScan(DynamicScannerEntity dynamicScannerEntity);
}
