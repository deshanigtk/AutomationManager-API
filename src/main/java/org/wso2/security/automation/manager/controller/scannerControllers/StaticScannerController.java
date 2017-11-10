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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.Constants;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.net.URI;

@Controller
@RequestMapping("staticScanner")
@Api(value = "staticScanner", description = "Static Scanner container related APIs")
public class StaticScannerController {

    private final StaticScannerService staticScannerService;

    private final MailHandler mailHandler;

    @Autowired
    public StaticScannerController(StaticScannerService staticScannerService, MailHandler mailHandler) {
        this.staticScannerService = staticScannerService;
        this.mailHandler = mailHandler;
    }


    @PostMapping(value = "startScan")
    @ApiOperation(value = "Start Static Scanner container, upload the product zip file or else clone product from GitHub and start scans - FindSecBugs and/or OWASP Dependency Check")
    public @ResponseBody
    String startScan(@RequestParam String userId,
                     @RequestParam String testName,
                     @RequestParam String ipAddress,
                     @RequestParam String productName,
                     @RequestParam String wumLevel,
                     @RequestParam boolean isFileUpload,
                     @RequestParam(required = false) MultipartFile zipFile,
                     @RequestParam(required = false) String url,
                     @RequestParam(required = false, defaultValue = "master") String branch,
                     @RequestParam(required = false) String tag,
                     @RequestParam boolean isFindSecBugs,
                     @RequestParam boolean isDependencyCheck) {

        return staticScannerService.startStaticScan(userId, testName, ipAddress, productName, wumLevel, isFileUpload, zipFile, url, branch, tag,
                isFindSecBugs, isDependencyCheck);
    }


    @PostMapping(path = "getReportAndMail")
    @ApiOperation(value = "Get the generated scan report from a container and mail it to the user")
    public @ResponseBody
    void getReport(@RequestParam String containerId, @RequestParam String to, @RequestParam boolean dependencyCheckReport) throws Exception {

        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                .setPort(staticScanner.getHostPort()).setScheme("http").setPath(Constants.STATIC_SCANNER_GET_REPORT)
                .addParameter("dependencyCheckReport", String.valueOf(dependencyCheckReport))
                .build();

        HttpResponse httpResponse = HttpRequestHandler.sendGetRequest(uri);

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
    @ApiOperation(value = "Stop a running container")
    public @ResponseBody
    void kill(@RequestParam String containerId) {
        staticScannerService.kill(containerId);
    }
}