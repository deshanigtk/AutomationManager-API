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
import org.wso2.security.automation.manager.entity.StaticScannerEntity;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Notification controller for dynamic scanner
 *
 * @author Deshani Geethika
 */
@Controller
@RequestMapping("staticScanner/notify")
@Api(value = "staticScannerNotifications",
        description = "Static Scanner containers notify status after a task is completed")
public class StaticScannerNotificationController {

    private static final String DATE_PATTERN = "yyyy-MM-dd:HH.mm.ss";
    private final StaticScannerService staticScannerService;

    @Autowired
    public StaticScannerNotificationController(StaticScannerService staticScannerService) {
        this.staticScannerService = staticScannerService;
    }

    @GetMapping(value = "fileExtracted")
    @ApiOperation(value = "Update that a zip file is uploaded and the uploaded time to the container")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScannerEntity staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFileExtracted(status);
        staticScanner.setFileExtractedTime(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
        staticScanner.setProductAvailable(true);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "productCloned")
    @ApiOperation(value = "Update that a zip file is extracted and the extracted time to the container")
    public @ResponseBody
    void updateProductCloned(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScannerEntity staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setProductCloned(status);
        staticScanner.setProductClonedTime(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
        staticScanner.setProductAvailable(true);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "findSecBugsStatus")
    @ApiOperation(value = "Update the status of FindSecBugs scan")
    public @ResponseBody
    void updateFindSecBugsStatus(@RequestParam String containerId, @RequestParam String status) {
        StaticScannerEntity staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFindSecBugsStatus(status);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "dependencyCheckStatus")
    public @ResponseBody
    @ApiOperation(value = "Update the status of Dependency Check scan")
    void updateDependencyCheckStatus(@RequestParam String containerId, @RequestParam String status) {
        StaticScannerEntity staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setDependencyCheckStatus(status);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "dependencyCheckReportReady")
    public @ResponseBody
    @ApiOperation(value = "Update that the Dependency Check report is ready, and the time")
    void updateDependencyCheckReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScannerEntity staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setDependencyCheckReportReady(status);
        staticScanner.setDependencyCheckReportReadyTime(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
        staticScannerService.save(staticScanner);

    }

    @GetMapping(value = "findSecBugsReportReady")
    @ApiOperation(value = "Update that the FindSecBugs scan report is ready, and the time")
    public @ResponseBody
    void updateFindSecBugsReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScannerEntity staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFindSecBugsReportReady(status);
        staticScanner.setFindSecBugsReportReadyTime(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "reportReady")
    public @ResponseBody
    @ApiOperation(value = "Update that the full report is ready (FindSecBugs report and/or Dependency Check report)")
    void updateReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScannerEntity staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setReportReady(status);
        staticScanner.setReportReadyTime(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
        staticScannerService.save(staticScanner);
        staticScannerService.getReportAndMail(containerId);
    }
}
