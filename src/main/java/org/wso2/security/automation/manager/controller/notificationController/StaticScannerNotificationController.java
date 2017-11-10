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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("staticScanner/notify")
public class StaticScannerNotificationController {

    private final StaticScannerService staticScannerService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public StaticScannerNotificationController(StaticScannerService staticScannerService) {
        this.staticScannerService = staticScannerService;
    }

    @GetMapping(value = "fileExtracted")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFileExtracted(status);
        staticScanner.setFileExtractedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        staticScanner.setProductAvailable(true);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "productCloned")
    public @ResponseBody
    void updateProductCloned(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setProductCloned(status);
        staticScanner.setProductClonedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        staticScanner.setProductAvailable(true);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "findSecBugsStatus")
    public @ResponseBody
    void updateFindSecBugsStatus(@RequestParam String containerId, @RequestParam String status) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFindSecBugsStatus(status);
        LOGGER.info("Find Sec Bugs status: " + status);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "dependencyCheckStatus")
    public @ResponseBody
    void updateDependencyCheckStatus(@RequestParam String containerId, @RequestParam String status) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setDependencyCheckStatus(status);
        LOGGER.info("Dependency Check status: " + status);
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "dependencyCheckReportReady")
    public @ResponseBody
    void updateDependencyCheckReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setDependencyCheckReportReady(status);
        staticScanner.setDependencyCheckReportReadyTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        LOGGER.info("Dependency Check report ready");
        staticScannerService.save(staticScanner);

    }

    @GetMapping(value = "findSecBugsReportReady")
    public @ResponseBody
    void updateFindSecBugsReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setFindSecBugsReportReady(status);
        staticScanner.setFindSecBugsReportReadyTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        LOGGER.info("Find sec bugs report ready");
        staticScannerService.save(staticScanner);
    }

    @GetMapping(value = "reportReady")
    public @ResponseBody
    void updateReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        StaticScanner staticScanner = staticScannerService.findOneByContainerId(containerId);
        staticScanner.setReportReady(status);
        staticScanner.setReportReadyTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        LOGGER.info("Full report ready");
        staticScannerService.save(staticScanner);
        staticScannerService.getReportAndMail(containerId);
    }

}
