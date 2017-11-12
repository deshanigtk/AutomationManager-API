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

package org.wso2.security.automation.manager.controller.scanner;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.service.DynamicScannerService;

/**
 * Controller for dynamic scanner
 *
 * @author Deshani Geethika
 */
@Controller
@RequestMapping("dynamicScanner")
@Api(value = "dynamicScanner", description = "Dynamic Scanner container related APIs")
public class DynamicScannerController {

    private final DynamicScannerService dynamicScannerService;

    @Autowired
    public DynamicScannerController(DynamicScannerService dynamicScannerService) {
        this.dynamicScannerService = dynamicScannerService;
    }

    @PostMapping(value = "startScan")
    @ApiOperation(value = "Start Dynamic Scanner container, upload the product zip file or else give IP address and " +
            "port of already running server and start OWASP Zap scan")
    public @ResponseBody
    void runZapScan(@RequestParam String userId,
                    @RequestParam String testName,
                    @RequestParam String ipAddress,
                    @RequestParam String productName,
                    @RequestParam String wumLevel,
                    @RequestParam boolean isFileUpload,
                    @RequestParam MultipartFile zipFile,
                    @RequestParam MultipartFile urlListFile,
                    @RequestParam(required = false) String wso2ServerHost,
                    @RequestParam(required = false, defaultValue = "-1") int wso2ServerPort,
                    @RequestParam boolean isAuthenticatedScan) {

        dynamicScannerService.startScan(userId, testName, ipAddress, productName, wumLevel, isFileUpload,
                zipFile, urlListFile, wso2ServerHost, wso2ServerPort, isAuthenticatedScan);
    }

    @GetMapping(path = "kill")
    @ApiOperation(value = "Stop a running container")
    public @ResponseBody
    void kill(@RequestParam String containerId) {
        dynamicScannerService.kill(containerId);
    }
}
