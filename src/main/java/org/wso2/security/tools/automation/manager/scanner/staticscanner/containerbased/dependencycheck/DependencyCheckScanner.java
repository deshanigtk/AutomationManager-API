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

package org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.dependencycheck;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.dependencycheck.DependencyCheckEntity;
import org.wso2.security.tools.automation.manager.entity.staticscanner.StaticScannerEntity;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.ServerHandler;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.StaticScanner;
import org.wso2.security.tools.automation.manager.service.StaticScannerService;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DependencyCheckScanner implements StaticScanner {
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
    private StaticScannerEntity staticScanner;

    public DependencyCheckScanner() {
        staticScanner = new DependencyCheckEntity();
        staticScannerService = ApplicationContextUtils.getApplicationContext().getBean(StaticScannerService.class);
    }

    @Override
    public void run() {
        if (startContainer() != null) {
            if (ServerHandler.hostAvailabilityCheck(staticScanner.getIpAddress(), staticScanner.getHostPort(), 12)) {
                startScan();
            }
        }
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
    public StaticScannerEntity startContainer() {
        staticScanner.setUserId(userId);
        staticScanner.setTestName(testName);
        staticScanner.setProductName(productName);
        staticScanner.setWumLevel(wumLevel);
        staticScanner.setStatus(ScannerProperties.getStatusInitiated());
        staticScannerService.save(staticScanner);

        int port = StaticScanner.calculatePort(staticScanner.getId());

        String containerId = DockerHandler.createContainer(ScannerProperties.getDependencyCheckDockerImage(),
                ipAddress, String.valueOf(port),
                String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
            staticScanner.setContainerId(containerId);
            staticScanner.setIpAddress(ipAddress);
            staticScanner.setContainerPort(port);
            staticScanner.setHostPort(port);
            staticScanner.setStatus(ScannerProperties.getStatusCreated());
            staticScanner.setCreatedTime(createdTime);

            staticScannerService.save(staticScanner);
            if (DockerHandler.startContainer(containerId)) {
                staticScanner.setStatus(ScannerProperties.getStatusRunning());
                staticScanner.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                return staticScannerService.save(staticScanner);
            }
        }
        return null;
    }

    @Override
    public void startScan() {
        try {
            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(ScannerProperties
                            .getStaticScannerStartScan())
                    .addParameter("automationManagerHost", ScannerProperties.getAutomationManagerHost())
                    .addParameter("automationManagerPort", String.valueOf(ScannerProperties.getAutomationManagerPort()))
                    .addParameter("myContainerId", staticScanner.getContainerId())
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
