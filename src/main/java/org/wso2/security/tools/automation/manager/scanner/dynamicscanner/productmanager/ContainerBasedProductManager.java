/*
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
package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.productmanager.containerbased
        .ContainerBasedProductManagerEntity;
import org.wso2.security.tools.automation.manager.exception.ProductManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.ServerHandler;
import org.wso2.security.tools.automation.manager.service.productmanager.ProductManagerService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ContainerBasedProductManager implements ProductManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerBasedProductManager.class);
    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private File zipFile;
    private ProductManagerService productManagerService;
    private ContainerBasedProductManagerEntity productManagerEntity;

    //TODO:add a comment
    private static int calculateContainerPort(int id) {
        if (20000 + id > 40000) {
            id = 1;
        }
        return (20000 + id) % 40000;
    }

    //TODO: add from a DTO
    public void init(String userId, String testName, String ipAddress, String productName, String wumLevel,
                     String fileUploadLocation, String zipFileName) {

        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        if (zipFileName != null) {
            this.zipFile = new File(fileUploadLocation + File.separator + zipFileName);
        }
    }

    private void createAndSaveProductManager(int relatedDynamicScannerId) {
        productManagerService = ApplicationContextUtils.getApplicationContext().getBean(ProductManagerService.class);
        productManagerEntity = new ContainerBasedProductManagerEntity();
        productManagerEntity.setUserId(userId);
        productManagerEntity.setTestName(testName);
        LOGGER.info("Before setting related ds id");
        productManagerEntity.setRelatedDynamicScannerId(relatedDynamicScannerId);
        LOGGER.info("After setting related ds id");
        productManagerEntity.setIpAddress(ipAddress);
        productManagerEntity.setProductName(productName);
        productManagerEntity.setWumLevel(wumLevel);
        productManagerEntity.setStatus(ScannerProperties.getStatusInitiated());
        productManagerService.save(productManagerEntity);
    }

    @Override
    public boolean startProductManager(int relatedDynamicScannerId) throws ProductManagerException {
        createAndSaveProductManager(relatedDynamicScannerId);
        boolean productManagerStarted = false;
        try {
            int port = calculateContainerPort(productManagerEntity.getId());
            String containerId = DockerHandler.createContainer(ScannerProperties.getProductManagerDockerImage(),
                    ipAddress, String.valueOf(port), String.valueOf(port), null, new String[]{"port=" + port});

            if (containerId != null) {
                String createdTime = new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
                productManagerEntity.setContainerId(containerId);
                productManagerEntity.setDockerIpAddress(ipAddress);
                productManagerEntity.setContainerPort(port);
                productManagerEntity.setHostPort(port);
                productManagerEntity.setStatus(ScannerProperties.getStatusCreated());
                productManagerEntity.setCreatedTime(createdTime);
                productManagerService.save(productManagerEntity);

                if (DockerHandler.startContainer(containerId)) {
                    productManagerEntity.setStatus(ScannerProperties.getStatusRunning());
                    productManagerEntity.setDockerIpAddress(DockerHandler.inspectContainer(containerId)
                            .networkSettings().ipAddress());
                    productManagerService.save(productManagerEntity);
                    productManagerStarted = ServerHandler.hostAvailabilityCheck(productManagerEntity
                                    .getDockerIpAddress(),
                            productManagerEntity.getHostPort(), 12 * 5);
                }
            }
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new ProductManagerException("Error occurred while starting Product Manager", e);
        }
        return productManagerStarted;
    }

    @Override
    public boolean startServer() throws ProductManagerException {
        boolean serverStarted = false;
        try {
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
                    Thread.sleep(1000 * 60);
                    serverStarted = ServerHandler.hostAvailabilityCheck(productManagerEntity.getDockerIpAddress(),
                            ScannerProperties.getProductManagerProductPort(), 12 * 4);
                }

            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new ProductManagerException("Error occurred while starting wso2 server", e);
        }
        return serverStarted;
    }

    @Override
    public String getHost() {
        return productManagerEntity.getDockerIpAddress();
    }

    @Override
    public int getPort() {
        return productManagerEntity.getHostPort();
    }
}
