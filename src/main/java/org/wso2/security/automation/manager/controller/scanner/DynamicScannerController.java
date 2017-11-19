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
 * The <code>DynamicScannerController</code> is the web controller which defines the routines for initiating dynamic scans.
 * When a controller method is hit, it calls a method in {@link DynamicScannerService}
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

    /**
     * The general contract of the method <code>startScan</code> is to call <code>startScan</code> method in {@link DynamicScannerService}.
     *
     * @param scanType       Indicates the dynamic scan type. Eg: ZAP, Qualis
     * @param userId         Unique identifier of a logged in user. Here email address is taken from authenticated user
     * @param testName       User defined name for the test
     * @param ipAddress      Ip address to spawn the containers
     * @param productName    Name of the product to be scanned
     * @param wumLevel       WUM level of the product. Default value will be 0
     * @param isFileUpload   Indicates whether the product is uploaded. False means the product is already in up and running status
     * @param zipFile        Zip file of the product to be scanned. This is not required if <code>isFileUpload</code> parameter is false
     * @param urlListFile    URLs of the product to be scanned is sent as a file
     * @param wso2ServerHost Host address if the product is already in up and running state
     * @param wso2ServerPort Port of a product which is in up and running state
     */
    @PostMapping(value = "startScan")
    @ApiOperation(value = "Start Dynamic Scanner container, upload the product zip file or else give IP address and " +
            "port of already running server and start OWASP ZAP scan")
    public @ResponseBody
    void startScan(@RequestParam String scanType,
                   @RequestParam String userId,
                   @RequestParam String testName,
                   @RequestParam String ipAddress,
                   @RequestParam String productName,
                   @RequestParam(defaultValue = "0") String wumLevel,
                   @RequestParam boolean isFileUpload,
                   @RequestParam(required = false) MultipartFile zipFile,
                   @RequestParam MultipartFile urlListFile,
                   @RequestParam(required = false) String wso2ServerHost,
                   @RequestParam(required = false, defaultValue = "-1") int wso2ServerPort) {

        dynamicScannerService.startScan(scanType, userId, testName, ipAddress, productName, wumLevel, isFileUpload,
                zipFile, urlListFile, wso2ServerHost, wso2ServerPort);
    }

    @GetMapping(path = "kill")
    @ApiOperation(value = "Stop a running container")
    public @ResponseBody
    void kill(@RequestParam String containerId) {
        dynamicScannerService.kill(containerId);
    }
}
