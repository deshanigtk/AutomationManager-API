package org.wso2.security.automation.manager.controller.notificationController;/*
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.service.DynamicScannerService;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("dynamicScanner/notify")
public class DynamicScannerNotificationController {

    private final DynamicScannerService dynamicScannerService;

    @Autowired
    public DynamicScannerNotificationController(DynamicScannerService dynamicScannerService) {
        this.dynamicScannerService = dynamicScannerService;
    }

    @GetMapping(value = "fileUploaded")
    public @ResponseBody
    void updateFileUploaded(@RequestParam String containerId, @RequestParam boolean status) {
        DynamicScanner dynamicScanner = dynamicScannerService.findOneByContainerId(containerId);
        dynamicScanner.setFileUploaded(status);
        dynamicScanner.setFileUploadedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        dynamicScannerService.save(dynamicScanner);
    }

    @GetMapping(value = "fileExtracted")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status) {
        DynamicScanner dynamicScanner = dynamicScannerService.findOneByContainerId(containerId);
        dynamicScanner.setFileExtracted(status);
        dynamicScanner.setFileExtractedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        dynamicScannerService.save(dynamicScanner);
    }

    @GetMapping(value = "serverStarted")
    public @ResponseBody
    void updateServerStarted(@RequestParam String containerId, @RequestParam boolean status) {
        DynamicScanner dynamicScanner = dynamicScannerService.findOneByContainerId(containerId);
        dynamicScanner.setServerStarted(status);
        dynamicScanner.setServerStartedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        dynamicScannerService.save(dynamicScanner);
    }

    @GetMapping(value = "zapScanStatus")
    public @ResponseBody
    void updateZapScanStatus(@RequestParam String containerId, @RequestParam String status, @RequestParam int progress) {
        DynamicScanner dynamicScanner = dynamicScannerService.findOneByContainerId(containerId);
        dynamicScanner.setZapScanStatus(status);
        dynamicScanner.setZapScanProgress(progress);
        dynamicScanner.setZapScanProgressTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        dynamicScannerService.save(dynamicScanner);
    }

    @GetMapping(value = "reportReady")
    public @ResponseBody
    void updateReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        DynamicScanner dynamicScanner = dynamicScannerService.findOneByContainerId(containerId);
        dynamicScanner.setReportReady(status);
        dynamicScanner.setReportReadyTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        dynamicScannerService.save(dynamicScanner);
        dynamicScannerService.getReportAndMail(containerId);
    }

    @GetMapping(value = "message")
    public @ResponseBody
    void updateMessage(@RequestParam String containerId, @RequestParam String status) {
        DynamicScanner dynamicScanner = dynamicScannerService.findOneByContainerId(containerId);
        dynamicScanner.setMessage(status);
        dynamicScannerService.save(dynamicScanner);
    }
}
