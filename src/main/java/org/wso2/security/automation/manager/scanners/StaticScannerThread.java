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
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StaticScannerThread implements Runnable {

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

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public StaticScannerThread(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean isFileUpload, String uploadLocation, String zipFileName, String url,
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
            StaticScanner staticScanner = startStaticScanner();
            if (staticScanner != null) {
                if (staticScannerService.isStaticScannerReady(staticScanner)) {
                    String startScanResponse = startScan(staticScanner);
                    LOGGER.info("start static scanner response: " + startScanResponse);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    private StaticScanner startStaticScanner() {
        StaticScanner staticScanner = new StaticScanner();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(testName);
        staticScanner.setProductName(productName);
        staticScanner.setWumLevel(wumLevel);
        staticScanner.setStatus("initiated");
        staticScannerService.save(staticScanner);

        int port = calculatePort(staticScanner.getId());

        String containerId = DockerHandler.createContainer(Constants.STATIC_SCANNER_DOCKER_IMAGE, ipAddress, String.valueOf(port),
                String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            staticScanner.setContainerId(containerId);
            staticScanner.setIpAddress(ipAddress);
            staticScanner.setContainerPort(port);
            staticScanner.setHostPort(port);
            staticScanner.setStatus("created");
            staticScanner.setCreatedTime(createdTime);

            staticScannerService.save(staticScanner);

            if (DockerHandler.startContainer(containerId)) {
                staticScanner.setStatus("running");
                staticScanner.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                staticScannerService.save(staticScanner);
                return staticScanner;
            }
        }
        return null;
    }


    private String startScan(StaticScanner staticScanner) {
        try {

            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(Constants.STATIC_SCANNER_START_SCAN)
                    .addParameter("automationManagerHost", Constants.AUTOMATION_MANAGER_HOST)
                    .addParameter("automationManagerPort", String.valueOf(Constants.AUTOMATION_MANAGER_PORT))
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
                return HttpRequestHandler.printResponse(response);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "Cannot start scanner";
    }


    private int calculatePort(int id) {
        if (40000 + id > 65535) {
            id = 1;
        }
        return (40000 + id) % 65535;
    }

}
