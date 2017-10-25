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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.service.DynamicScannerService;

import java.io.*;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("dynamicScanner")
public class DynamicScannerController {


    private final DynamicScannerService dynamicScannerService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public DynamicScannerController(DynamicScannerService dynamicScannerService) {
        this.dynamicScannerService = dynamicScannerService;
    }

    @PostMapping(value = "startScan")
    public @ResponseBody
    String runZapScan(@RequestParam String userId,
                      @RequestParam String name,
                      @RequestParam String ipAddress,
                      @RequestParam boolean isFileUpload,
                      @RequestParam MultipartFile zipFile,
                      @RequestParam MultipartFile urlListFile,
                      @RequestParam(required = false) String wso2ServerHost,
                      @RequestParam(required = false, defaultValue = "-1") int wso2ServerPort,
                      @RequestParam boolean isAuthenticatedScan) {

        return dynamicScannerService.startScan(userId, name, ipAddress, isFileUpload, zipFile, urlListFile,
                wso2ServerHost, wso2ServerPort, isAuthenticatedScan);
    }

    @GetMapping(path = "kill")
    public @ResponseBody
    void kill(@RequestParam String containerId) {
        dynamicScannerService.kill(containerId);
    }

}
