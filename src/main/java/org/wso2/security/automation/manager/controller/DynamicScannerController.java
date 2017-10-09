package org.wso2.security.automation.manager.controller;
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
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.service.DynamicScannerService;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("dynamicScanner")
public class DynamicScannerController {

    @Value("${DYNAMIC_SCANNER_DOCKER_IMAGE}")
    private String dockerImage;

    @Value("${UPLOAD_EXTRACT_AND_START_SERVER}")
    private String uploadExtractAndStartServer;

    @Value("${START_ZAP_SCAN}")
    private String startZapScan;

    @Value("${GET_REPORT_AND_MAIL}")
    private String getReportAndMail;

    @Value("${DYNAMIC_SCANNER_CONFIGURE_NOTIFICATION_MANAGER}")
    private String configureNotificationManager;

    @Value("${AUTOMATION_MANAGER_HOST}")
    private String myHost;

    @Value("${AUTOMATION_MANAGER_PORT}")
    private int myPort;

    private final DynamicScannerService dynamicScannerService;

    private final MailHandler mailHandler;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public DynamicScannerController(DynamicScannerService dynamicScannerService, MailHandler mailHandler) {
        this.dynamicScannerService = dynamicScannerService;
        this.mailHandler = mailHandler;
    }

    @PostMapping(value = "start")
    public @ResponseBody
    String start(@RequestParam String ipAddress, @RequestParam int containerPort, @RequestParam int hostPort) {
        String containerId = DockerHandler.createContainer(dockerImage, ipAddress, String.valueOf(containerPort),
                String.valueOf(hostPort), null, new String[]{"port=" + containerPort});

        if (containerId != null) {
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            DynamicScanner dynamicScanner = new DynamicScanner(containerId, createdTime, ipAddress, containerPort, hostPort);
            dynamicScanner = dynamicScannerService.save(dynamicScanner);

            if (DockerHandler.startContainer(containerId)) {

                dynamicScanner.setStatus("running");
                dynamicScannerService.save(dynamicScanner);
                return containerId;
            }
        }
        return null;
    }

    @GetMapping(value = "/configureNotificationManager")
    public @ResponseBody
    void configureNotificationManager(@RequestParam String ipAddress, @RequestParam int hostPort, @RequestParam String containerId) {
        try {
            URI uri = (new URIBuilder()).setHost(ipAddress).setPort(hostPort).setScheme("http").setPath(configureNotificationManager)
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


    @PostMapping(value = "uploadZipFileExtractAndStartServer")
    public @ResponseBody
    void uploadZipFileExtractAndStartServer(@RequestParam String containerId, @RequestParam MultipartFile zipFile) throws IOException {
        try {
            DynamicScanner dynamicScanner = dynamicScannerService.findOne(containerId);

            URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress()).setPort(dynamicScanner.getHostPort()).setScheme("http")
                    .setPath(uploadExtractAndStartServer)
                    .build();

            HttpResponse httpResponse = HttpRequestHandler.sendMultipartRequest(uri, zipFile, new HashMap<>());
            HttpRequestHandler.printResponse(httpResponse);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "runZapScan")
    public @ResponseBody
    void startZapScan(@RequestParam String containerId,
                      @RequestParam String zapHost,
                      @RequestParam int zapPort,
                      @RequestParam String contextName,
                      @RequestParam String sessionName,
                      @RequestParam String productHostRelativeToZap,
                      @RequestParam String productHostRelativeToDynamicScanner,
                      @RequestParam int productPort,
                      @RequestParam String urlListPath,
                      @RequestParam boolean isAuthenticatedScan) {
        try {
            DynamicScanner dynamicScanner = dynamicScannerService.findOne(containerId);
            URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress()).setPort(dynamicScanner.getHostPort())
                    .setScheme("http").setPath(startZapScan)

                    .addParameter("zapHost", zapHost)
                    .addParameter("zapPort", String.valueOf(zapPort))
                    .addParameter("contextName", contextName)
                    .addParameter("sessionName", sessionName)
                    .addParameter("productHostRelativeToZap", productHostRelativeToZap)
                    .addParameter("productHostRelativeToThis", productHostRelativeToDynamicScanner)
                    .addParameter("productPort", String.valueOf(productPort))
                    .addParameter("urlListPath", urlListPath)
                    .addParameter("isAuthenticatedScan", String.valueOf(isAuthenticatedScan))
                    .build();

            HttpResponse httpResponse = HttpRequestHandler.sendGetRequest(uri);
            HttpRequestHandler.printResponse(httpResponse);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping(path = "getReportAndMail")
    public @ResponseBody
    void getReport(@RequestParam String containerId, @RequestParam String to) throws Exception {

        DynamicScanner dynamicScanner = dynamicScannerService.findOne(containerId);
//        URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
//                .setPort(dynamicScanner.getHostPort()).setScheme("http").setPath(getReportAndMail)
//                .build();

        URI uri = (new URIBuilder()).setHost("localhost")
                .setPort(8081).setScheme("http").setPath(getReportAndMail)
                .build();
        HttpResponse httpResponse = HttpRequestHandler.sendGetRequest(uri);


        if (httpResponse.getEntity() != null) {
            String subject = "Dynamic Scan Report: ";
            mailHandler.sendMail(to, subject, "This is auto generated message", httpResponse.getEntity().getContent(), "ZapReport.html");
        }
    }

    @GetMapping(path = "kill")
    public @ResponseBody
    void kill(@RequestParam String containerId) throws Exception {
        DynamicScanner dynamicScanner = dynamicScannerService.findOne(containerId);
        DockerHandler.killContainer(containerId);
        dynamicScanner.setStatus("killed");
        dynamicScannerService.save(dynamicScanner);
    }
}
