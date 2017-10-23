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
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.io.IOException;
import java.net.URI;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("staticScanner")
public class StaticScannerController {

    @Value("${STATIC_SCANNER_DOCKER_IMAGE}")
    private String dockerImage;

    @Value("${STATIC_SCANNER_GET_REPORT}")
    private String getReport;

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


    @PostMapping(value = "startScan")
    public @ResponseBody
    String startScan(@RequestParam String userId,
                     @RequestParam String name,
                     @RequestParam String ipAddress,
                     @RequestParam boolean isFileUpload,
                     @RequestParam(required = false) MultipartFile zipFile,
                     @RequestParam(required = false) String url,
                     @RequestParam(required = false, defaultValue = "master") String branch,
                     @RequestParam(required = false) String tag,
                     @RequestParam boolean isFindSecBugs,
                     @RequestParam boolean isDependencyCheck) {

        return staticScannerService.startStaticScan(userId, name, ipAddress, isFileUpload, zipFile, url, branch, tag,
                isFindSecBugs, isDependencyCheck);
    }


    @PostMapping(path = "getReportAndMail")
    public @ResponseBody
    void getReport(@RequestParam String containerId, @RequestParam String to, @RequestParam boolean dependencyCheckReport) throws Exception {

        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
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

    @GetMapping(path = "kill")
    public @ResponseBody
    void kill(@RequestParam String containerId) {
        staticScannerService.kill(containerId);
    }
}