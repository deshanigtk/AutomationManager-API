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

package org.wso2.security.automation.manager.controller.notification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.automation.manager.config.ScannerProperty;
import org.wso2.security.automation.manager.entity.scanner.statics.StaticScannerEntity;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Notification controller for static scanner
 *
 * @author Deshani Geethika
 */
@Controller
@RequestMapping("staticScanner/notify")
@Api(value = "staticScannerNotifications", description = "Static DependencyCheckScanner containers notify status after a task is completed")
public class StaticScannerNotificationController {

    private final StaticScannerService staticScannerService;

    @Autowired
    public StaticScannerNotificationController(StaticScannerService staticScannerService) {
        this.staticScannerService = staticScannerService;
    }

    @GetMapping(value = "fileExtracted")
    @ApiOperation(value = "Update that a zip file is uploaded and the uploaded time to the container")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status) {
        staticScannerService.updateFileExtracted(containerId, status);
    }

    @GetMapping(value = "productCloned")
    @ApiOperation(value = "Update that a zip file is extracted and the extracted time to the container")
    public @ResponseBody
    void updateProductCloned(@RequestParam String containerId, @RequestParam boolean status) {
        staticScannerService.updateProductCloned(containerId, status);
    }

    @GetMapping(value = "reportReady")
    public @ResponseBody
    @ApiOperation(value = "Update that the full report is ready (FindSecBugsEntity report and/or Dependency Check report)")
    void updateReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        staticScannerService.updateReportReady(containerId, status);
    }
}
