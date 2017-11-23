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

package org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.findsecbugs;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.StaticScannerEntity;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.ContainerBasedStaticScannerEntity;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.findsecbugs.FindSecBugsEntity;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.ContainerBasedStaticScanner;
import org.wso2.security.tools.automation.manager.service.StaticScannerService;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FindSecBugsScanner implements ContainerBasedStaticScanner {
    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private boolean isFileUpload;
    private File zipFile;
    private String gitUrl;
    private String gitUsername;
    private String gitPassword;

    private StaticScannerService staticScannerService;
    private ContainerBasedStaticScannerEntity staticScannerEntity;

    public FindSecBugsScanner() {
        staticScannerEntity = new FindSecBugsEntity();
        staticScannerService = ApplicationContextUtils.getApplicationContext().getBean(StaticScannerService.class);
    }

    @Override
    public void init(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean
            isFileUpload, String uploadLocation, String zipFileName, String gitUrl, String gitUsername, String
                             gitPassword) {
        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        this.isFileUpload = isFileUpload;
        if (zipFileName != null) {
            this.zipFile = new File(uploadLocation + File.separator + zipFileName);
        }
        this.gitUrl = gitUrl;
        this.gitUsername = gitUsername;
        this.gitPassword = gitPassword;
    }

    @Override
    public StaticScannerEntity startScanner() {
        staticScannerEntity.setUserId(userId);
        staticScannerEntity.setTestName(testName);
        staticScannerEntity.setProductName(productName);
        staticScannerEntity.setWumLevel(wumLevel);
        staticScannerEntity.setStatus(ScannerProperties.getStatusInitiated());
        staticScannerService.save(staticScannerEntity);

        int port = ContainerBasedStaticScanner.calculatePort(staticScannerEntity.getId());

        String containerId = DockerHandler.createContainer(ScannerProperties.getFindsecbugsScannerDockerImage(),
                ipAddress, String.valueOf(port),
                String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
            staticScannerEntity.setContainerId(containerId);
            staticScannerEntity.setIpAddress(ipAddress);
            staticScannerEntity.setContainerPort(port);
            staticScannerEntity.setHostPort(port);
            staticScannerEntity.setStatus(ScannerProperties.getStatusCreated());
            staticScannerEntity.setCreatedTime(createdTime);

            staticScannerService.save(staticScannerEntity);
            if (DockerHandler.startContainer(containerId)) {
                staticScannerEntity.setStatus(ScannerProperties.getStatusRunning());
                staticScannerEntity.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                return staticScannerService.save(staticScannerEntity);
            }
        }
        return null;
    }

    @Override
    public void startScan() {
        try {
            URI uri = (new URIBuilder()).setHost(staticScannerEntity.getIpAddress())
                    .setPort(staticScannerEntity.getHostPort()).setScheme("http").setPath(ScannerProperties
                            .getStaticScannerStartScan())
                    .addParameter("automationManagerHost", ScannerProperties.getAutomationManagerHost())
                    .addParameter("automationManagerPort", String.valueOf(ScannerProperties.getAutomationManagerPort()))
                    .addParameter("myContainerId", staticScannerEntity.getContainerId())
                    .addParameter("isFileUpload", String.valueOf(isFileUpload))
                    .addParameter("gitUrl", gitUrl)
                    .addParameter("gitUsername", gitUsername)
                    .addParameter("gitPassword", gitPassword)
                    .build();

            Map<String, File> files = new HashMap<>();
            if (zipFile != null) {
                files.put("zipFile", zipFile);
            }
            HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, files, null);
            if (response != null) {
                String message = HttpRequestHandler.printResponse(response);
                LOGGER.info("Start static scan response: " + message);
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Error occurred while executing static scan command", e);
        }
    }
}
