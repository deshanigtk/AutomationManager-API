package org.wso2.security.automationmanager.controller;
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
import org.wso2.security.automationmanager.entity.DynamicScanner;
import org.wso2.security.automationmanager.handlers.DockerHandler;
import org.wso2.security.automationmanager.handlers.HttpRequestHandler;
import org.wso2.security.automationmanager.handlers.MailHandler;
import org.wso2.security.automationmanager.service.DynamicScannerService;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private DynamicScannerService dynamicScannerService;

    @Autowired
    private MailHandler mailHandler;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @PostMapping(value = "start")
    public @ResponseBody
    String start(@RequestParam String userId, @RequestParam String ipAddress, @RequestParam String containerPort, @RequestParam String hostPort) {
        if (DockerHandler.pullImage(dockerImage)) {
            String containerId = DockerHandler.createContainer(dockerImage, ipAddress, containerPort, hostPort, null);

            if (containerId != null) {
                String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                DynamicScanner dynamicScanner = new DynamicScanner(containerId, userId, createdTime, ipAddress, containerPort, hostPort);
                dynamicScanner = dynamicScannerService.save(dynamicScanner);

                if (DockerHandler.startContainer(containerId)) {
                    dynamicScanner.setStatus("running");
                    dynamicScannerService.save(dynamicScanner);
                    return containerId;
                }

            }
        }
        return null;
    }

    @PostMapping(value = "uploadZipFileExtractAndStartServer")
    public @ResponseBody
    void uploadZipFileExtractAndStartServer(@RequestParam String containerId, @RequestParam MultipartFile zipFile, @RequestParam boolean replaceExisting) throws IOException {
        try {
            DynamicScanner dynamicScanner = dynamicScannerService.findOne(containerId);

            URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress()).setPort(Integer.parseInt(dynamicScanner.getHostPort())).setScheme("http").setPath(uploadExtractAndStartServer)
                    .build();

            Map<String, String> bodyContent = new HashMap<>();
            bodyContent.put("replaceExisting", String.valueOf(replaceExisting));
            HttpRequestHandler.sendMultipartRequest(uri, zipFile, bodyContent);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "zapScan")
    public @ResponseBody
    void startZapScan(@RequestParam String containerId,
                      @RequestParam String zapHost,
                      @RequestParam int zapPort,
                      @RequestParam String sessionName,
                      @RequestParam String productHostRelativeToZap,
                      @RequestParam String productHostRelativeToDynamicScanner,
                      @RequestParam int productPort,
                      @RequestParam String urlListPath,
                      @RequestParam boolean isAuthenticatedScan) {
        try {
            DynamicScanner dynamicScanner = dynamicScannerService.findOne(containerId);
            URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress()).setPort(Integer.parseInt(dynamicScanner.getHostPort()))
                    .setScheme("http").setPath(startZapScan)

                    .addParameter("zapHost", zapHost)
                    .addParameter("zapPort", String.valueOf(zapPort))
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
        URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
                .setPort(Integer.parseInt(dynamicScanner.getHostPort())).setScheme("http").setPath(getReportAndMail)
                .build();

        HttpResponse httpResponse = HttpRequestHandler.sendGetRequest(uri);

        if (httpResponse.getEntity() != null) {
            String subject = "Dynamic Scan Report: ";
            mailHandler.sendMail(to, subject, "This is auto generated message", httpResponse.getEntity().getContent());
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
