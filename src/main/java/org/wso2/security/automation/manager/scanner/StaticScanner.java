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

package org.wso2.security.automation.manager.scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.automation.manager.entity.StaticScannerEntity;
import org.wso2.security.automation.manager.handler.DockerHandler;
import org.wso2.security.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.automation.manager.property.ScannerProperty;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Static scanner {@link Runnable} class to run the scan asynchronously
 *
 * @author Deshani Geethika
 */
public class StaticScanner implements Runnable {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final String STATUS_INITIATED = "initiated";
    private static final String STATUS_CREATED = "created";
    private static final String STATUS_RUNNING = "running";

    private static final String DATE_PATTERN = "yyyy-MM-dd:HH.mm.ss";

    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private boolean isFileUpload;
    private File zipFile;
    private String url;
    private String branch;
    private String tag;
    private boolean isFindSecBugs;
    private boolean isDependencyCheck;

    private StaticScannerService staticScannerService;

    public StaticScanner(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean isFileUpload, String uploadLocation, String zipFileName, String url,
                         String branch, String tag, boolean isFindSecBugs, boolean isDependencyCheck) {
        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        this.isFileUpload = isFileUpload;
        if (zipFileName != null) {
            this.zipFile = new File(uploadLocation + File.separator + zipFileName);
        }
        this.url = url;
        this.branch = branch;
        this.tag = tag;
        this.isFindSecBugs = isFindSecBugs;
        this.isDependencyCheck = isDependencyCheck;

        staticScannerService = ApplicationContextUtils.getApplicationContext().getBean(StaticScannerService.class);
    }

    @Override
    public void run() {
        try {
            StaticScannerEntity staticScanner = startStaticScanner();
            if (staticScanner != null) {
                if (staticScannerService.isStaticScannerReady(staticScanner)) {
                    startScan(staticScanner);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred while running static scanner", e);
        }
    }

    private StaticScannerEntity startStaticScanner() {
        StaticScannerEntity staticScanner = new StaticScannerEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(testName);
        staticScanner.setProductName(productName);
        staticScanner.setWumLevel(wumLevel);
        staticScanner.setStatus(STATUS_INITIATED);
        staticScannerService.save(staticScanner);

        int port = calculatePort(staticScanner.getId());

        String containerId = DockerHandler.createContainer(ScannerProperty.getStaticScannerDockerImage(), ipAddress, String.valueOf(port),
                String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat(DATE_PATTERN).format(new Date());
            staticScanner.setContainerId(containerId);
            staticScanner.setIpAddress(ipAddress);
            staticScanner.setContainerPort(port);
            staticScanner.setHostPort(port);
            staticScanner.setStatus(STATUS_CREATED);
            staticScanner.setCreatedTime(createdTime);

            staticScannerService.save(staticScanner);

            if (DockerHandler.startContainer(containerId)) {
                staticScanner.setStatus(STATUS_RUNNING);
                staticScanner.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                staticScannerService.save(staticScanner);
                return staticScanner;
            }
        }
        return null;
    }

    private void startScan(StaticScannerEntity staticScanner) {
        try {
            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(ScannerProperty.getStaticScannerStartScan())
                    .addParameter("automationManagerHost", ScannerProperty.getAutomationManagerHost())
                    .addParameter("automationManagerPort", String.valueOf(ScannerProperty.getAutomationManagerPort()))
                    .addParameter("myContainerId", staticScanner.getContainerId())
                    .addParameter("isFileUpload", String.valueOf(isFileUpload))
                    .addParameter("url", url)
                    .addParameter("branch", branch)
                    .addParameter("tag", tag)
                    .addParameter("isFindSecBugs", String.valueOf(isFindSecBugs))
                    .addParameter("isDependencyCheck", String.valueOf(isDependencyCheck))
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

    private int calculatePort(int id) {
        if (40000 + id > 65535) {
            id = 1;
        }
        return (40000 + id) % 65535;
    }
}
