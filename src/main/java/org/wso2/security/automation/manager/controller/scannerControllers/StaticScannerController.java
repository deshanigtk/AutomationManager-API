package org.wso2.security.automation.manager.controller.scannerControllers;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("/staticScanner")
public class StaticScannerController {

    @Value("${STATIC_SCANNER_DOCKER_IMAGE}")
    private String dockerImage;

    @Value("${CLONE_FROM_GITHUB}")
    private String cloneFromGitHub;

    @Value("${UPLOAD_PRODUCT_ZIP_FILE_AND_EXTRACT}")
    private String uploadProductZipFileAndExtract;

    @Value("${FIND_SEC_BUGS}")
    private String runFindSecBugs;

    @Value("${DEPENDENCY_CHECK}")
    private String runDependencyCheck;

    @Value("${STATIC_SCANNER_GET_REPORT}")
    private String getReport;

    @Value("${STATIC_SCANNER_CONFIGURE_NOTIFICATION_MANAGER}")
    private String configureNotificationManager;

    @Value("${AUTOMATION_MANAGER_HOST}")
    private String myHost;

    @Value("${AUTOMATION_MANAGER_PORT}")
    private int myPort;


    private final StaticScannerService staticScannerService;

    private final MailHandler mailHandler;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public StaticScannerController(StaticScannerService staticScannerService, MailHandler mailHandler) {
        this.staticScannerService = staticScannerService;
        this.mailHandler = mailHandler;
    }

    @PostMapping(value = "start")
    public @ResponseBody
    String start(@RequestParam String userId, @RequestParam String ipAddress, @RequestParam int containerPort, @RequestParam int hostPort) {
        String containerId = DockerHandler.createContainer(dockerImage, ipAddress, String.valueOf(containerPort), String.valueOf(hostPort), null, new String[]{"port=" + containerPort});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            StaticScanner staticScanner = new StaticScanner(containerId, userId, createdTime, ipAddress, containerPort, hostPort);
            staticScannerService.save(staticScanner);

            if (DockerHandler.startContainer(containerId)) {
                staticScanner.setStatus("running");
                staticScannerService.save(staticScanner);
                return containerId;
            }
        }
        return null;
    }

    @GetMapping(value = "configureNotificationManager")
    public @ResponseBody
    void configureNotificationManager(@RequestParam String containerId) {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);

            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(staticScanner.getHostPort()).setScheme("http").setPath(configureNotificationManager)
                    .addParameter("automationManagerHost", myHost)
                    .addParameter("automationManagerPort", String.valueOf(myPort))
                    .addParameter("myContainerId", containerId)
                    .build();
            LOGGER.info("URI to configure notification manager: " + uri);
            HttpRequestHandler.sendGetRequest(uri);

        } catch (URISyntaxException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    @PostMapping(value = "cloneProductFromGitHub")
    public @ResponseBody
    String clone(@RequestParam String containerId, @RequestParam String url, @RequestParam String branch, @RequestParam String tag) {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);
            if (staticScanner.getStatus().equals("running")) {
                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(staticScanner.getHostPort()).setScheme("http")
                        .setPath(cloneFromGitHub)
                        .addParameter("url", url)
                        .addParameter("branch", branch)
                        .addParameter("tag", tag)
                        .build();

                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);
                return HttpRequestHandler.printResponse(response);
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return null;
    }

    @PostMapping(value = "uploadProductZipFileAndExtract")
    public @ResponseBody
    String uploadProductZipFileAndExtract(@RequestParam String containerId, @RequestParam MultipartFile zipFile) throws IOException {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);

            if (staticScanner != null) {
                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(staticScanner.getHostPort()).setScheme("http")
                        .setPath(uploadProductZipFileAndExtract)
                        .build();

                HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, zipFile, null);
                return HttpRequestHandler.printResponse(response);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping(value = "runFindSecBugs")
    public @ResponseBody
    String runFindSecBugs(@RequestParam String containerId) {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);
            if (staticScanner.getStatus().equals("running") && staticScanner.isProductAvailable()) {

                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(staticScanner.getHostPort()).setScheme("http").setPath(runFindSecBugs)
                        .build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);
                return HttpRequestHandler.printResponse(response);
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }

    @PostMapping(value = "runDependencyCheck")
    public @ResponseBody
    void runDependencyCheck(@RequestParam String containerId) {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);
            if (staticScanner.getStatus().equals("running") && staticScanner.isProductAvailable()) {

                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(staticScanner.getHostPort()).setScheme("http").setPath(runDependencyCheck)
                        .build();
                HttpRequestHandler.sendGetRequest(uri);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @PostMapping(path = "getReportAndMail")
    public @ResponseBody
    void getReport(@RequestParam String containerId, @RequestParam String to, @RequestParam boolean dependencyCheckReport) throws Exception {

        StaticScanner staticScanner = staticScannerService.findOne(containerId);
        URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                .setPort(staticScanner.getHostPort()).setScheme("http").setPath(getReport)
                .addParameter("dependencyCheckReport", String.valueOf(dependencyCheckReport))
                .build();

        HttpResponse httpResponse = HttpRequestHandler.sendGetRequest(uri);
        System.out.println(httpResponse.getEntity().getContent());

        if (httpResponse.getEntity() != null) {
            String subject = "Static Scan Report: ";
            String fileName;
            if (dependencyCheckReport) {
                fileName = "Dependency-Check-Reports.zip";
            } else {
                fileName = "Find-Sec-Bugs-Reports.zip";
            }
            mailHandler.sendMail(to, subject, "This is auto generated message", httpResponse.getEntity().getContent(), fileName);
        }
    }
}