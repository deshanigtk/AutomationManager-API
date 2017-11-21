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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.scanner.dynamicscanner.ProductManagerEntity;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.service.ProductManagerService;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Product manager
 *
 * @author Deshani Geethika
 */
@SuppressWarnings("WeakerAccess")
public class ProductManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScanner.class);
    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private boolean isFileUpload;
    private File zipFile;
    private String wso2ServerHost;
    private int wso2ServerPort;
    private boolean isInitialized = false;
    private ProductManagerService productManagerService;
    private ProductManagerEntity productManagerEntity;

    public ProductManager(String userId, String testName, String ipAddress, String productName, String wumLevel,
                          boolean isFileUpload, String fileUploadLocation, String zipFileName,
                          String wso2ServerHost, int wso2ServerPort) {
        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        this.isFileUpload = isFileUpload;
        if (zipFileName != null) {
            this.zipFile = new File(fileUploadLocation + File.separator + zipFileName);
        }
        this.wso2ServerHost = wso2ServerHost;
        this.wso2ServerPort = wso2ServerPort;
        productManagerService = ApplicationContextUtils.getApplicationContext().getBean(ProductManagerService.class);
    }

    public void init() {
        productManagerEntity = new ProductManagerEntity();
        productManagerEntity.setUserId(userId);
        productManagerEntity.setTestName(testName);
        productManagerEntity.setIpAddress(ipAddress);
        productManagerEntity.setProductName(productName);
        productManagerEntity.setWumLevel(wumLevel);
        productManagerEntity.setStatus(ScannerProperties.getStatusInitiated());
        productManagerService.save(productManagerEntity);
        isInitialized = true;
    }

    public ProductManagerEntity startContainer(String relatedDynamicScannerId) {
        if (!isInitialized) {
            init();
        }
        int port = calculateWso2ServerPort(productManagerEntity.getId());
        String containerId = DockerHandler.createContainer(ScannerProperties.getProductManagerDockerImage(), ipAddress,
                String.valueOf(port), String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
            productManagerEntity.setContainerId(containerId);
            productManagerEntity.setDockerIpAddress(ipAddress);
            productManagerEntity.setContainerPort(port);
            productManagerEntity.setHostPort(port);
            productManagerEntity.setStatus(ScannerProperties.getStatusCreated());
            productManagerEntity.setCreatedTime(createdTime);
            productManagerEntity.setRelatedDynamicScannerId(relatedDynamicScannerId);
            productManagerService.save(productManagerEntity);

            if (DockerHandler.startContainer(containerId)) {
                productManagerEntity.setStatus(ScannerProperties.getStatusRunning());
                productManagerEntity.setDockerIpAddress(DockerHandler.inspectContainer(containerId).networkSettings()
                        .ipAddress());
                productManagerService.save(productManagerEntity);
                return productManagerEntity;
            }
        }
        return null;
    }

    public boolean startWso2Server() {
        try {
            if (isFileUpload && zipFile != null) {
                URI uri = (new URIBuilder()).setHost(productManagerEntity.getDockerIpAddress())
                        .setPort(productManagerEntity.getHostPort()).setScheme("http")
                        .setPath(ScannerProperties.getProductManagerStartServer())
                        .addParameter("automationManagerHost", ScannerProperties.getAutomationManagerHost())
                        .addParameter("automationManagerPort", String.valueOf(ScannerProperties
                                .getAutomationManagerPort()))
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

    public boolean isFileUpload() {
        return isFileUpload;
    }

    private int calculateWso2ServerPort(int id) {
        if (20000 + id > 40000) {
            id = 1;
        }
        return (20000 + id) % 40000;
    }

    public String getWso2ServerHost() {
        return wso2ServerHost;
    }

    public int getWso2ServerPort() {
        return wso2ServerPort;
    }
}
