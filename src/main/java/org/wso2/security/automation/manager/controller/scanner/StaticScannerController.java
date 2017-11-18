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
import org.wso2.security.automation.manager.service.StaticScannerService;

/**
 * Controller for static scanner
 *
 * @author Deshani Geethika
 */
@Controller
@RequestMapping("staticScanner")
@Api(value = "staticScanner", description = "Static Scanner container related APIs")
public class StaticScannerController {

    private final StaticScannerService staticScannerService;

    @Autowired
    public StaticScannerController(StaticScannerService staticScannerService) {
        this.staticScannerService = staticScannerService;
    }

    @PostMapping(value = "startScan")
    @ApiOperation(value = "Start Static Scanner container, upload the product zip file or else clone product from GitHub and start scans - FindSecBugsEntity and/or OWASP Dependency Check")
    public @ResponseBody
    void startScan(@RequestParam String userId,
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

        staticScannerService.startStaticScan(userId, testName, ipAddress, productName, wumLevel, isFileUpload,
                zipFile, url, branch, tag, isFindSecBugs, isDependencyCheck);
    }

    @GetMapping(path = "kill")
    @ApiOperation(value = "Stop a running container")
    public @ResponseBody
    void kill(@RequestParam String containerId) {
        staticScannerService.kill(containerId);
    }
}
