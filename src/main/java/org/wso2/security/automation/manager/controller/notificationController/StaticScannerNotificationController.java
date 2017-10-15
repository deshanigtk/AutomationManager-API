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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.service.StaticScannerService;

@Controller
@RequestMapping("staticScanner/notify")
public class StaticScannerNotificationController {

    private final StaticScannerService staticScannerService;

    @Autowired
    public StaticScannerNotificationController(StaticScannerService staticScannerService) {
        this.staticScannerService = staticScannerService;
    }

    @GetMapping(value = "fileExtracted")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status, @RequestParam String time) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFileExtracted(status);
        staticScanner.setFileExtractedTime(time);
        staticScanner.setProductAvailable(true);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "productCloned")
    public @ResponseBody
    void updateProductCloned(@RequestParam String containerId, @RequestParam boolean status, @RequestParam String time) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setProductCloned(status);
        staticScanner.setProductClonedTime(time);
        staticScanner.setProductAvailable(true);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "dependencyCheckReportReady")
    public @ResponseBody
    void updateDependencyCheckReportReady(@RequestParam String containerId, @RequestParam boolean status, @RequestParam String time) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setDependencyCheckReportReady(status);
        staticScanner.setDependencyCheckReportReadyTime(time);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "findSecBugsReportReady")
    public @ResponseBody
    void updateFindSecBugsReportReady(@RequestParam String containerId, @RequestParam boolean status, @RequestParam String time) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFindSecBugsReportReady(status);
        staticScanner.setFindSecBugsReportReadyTime(time);
        staticScannerService.save(staticScanner);
    }


}
