package org.wso2.security.automation.manager.controller.viewControllers;/*
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.controller.scannerControllers.DynamicScannerController;
import org.wso2.security.automation.manager.controller.scannerControllers.StaticScannerController;
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.service.DynamicScannerService;
import org.wso2.security.automation.manager.service.StaticScannerService;

import java.io.IOException;
import java.util.ArrayList;


@Controller

public class ViewController {

    private final StaticScannerService staticScannerService;

    private final DynamicScannerService dynamicScannerService;

    private final StaticScannerController staticScannerController;

    private final DynamicScannerController dynamicScannerController;

    @Autowired
    public ViewController(StaticScannerController staticScannerController, DynamicScannerService dynamicScannerService, StaticScannerService staticScannerService, DynamicScannerController dynamicScannerController) {
        this.staticScannerController = staticScannerController;
        this.dynamicScannerService = dynamicScannerService;
        this.staticScannerService = staticScannerService;
        this.dynamicScannerController = dynamicScannerController;
    }

    @GetMapping(value = "/signin")
    public String signIn() {
        return "signin/signin";
    }

    @GetMapping(value = "/mainScanners")
    public String mainScanners() {
        return "mainScanners";
    }

    @GetMapping(value = "/myScans")
    public String myScans(Model model) {
        Iterable<StaticScanner> staticScanners = staticScannerService.findAll();
        Iterable<DynamicScanner> dynamicScanners = dynamicScannerService.findAll();

        model.addAttribute("staticScanners", staticScanners);
        model.addAttribute("dynamicScanners", dynamicScanners);
        return "myScans";
    }

    @PostMapping(value = "staticScanner/started")
    public String staticScannerStarted(Model model, @RequestParam String userId, @RequestParam String ipAddress, @RequestParam int containerPort, @RequestParam int hostPort) throws InterruptedException {
        String containerId = staticScannerController.start(userId, ipAddress, containerPort, hostPort);
        if (containerId != null) {
            model.addAttribute("containerId", containerId);
            return "staticScanner/productUploader";
        }
        return mainScanners();
    }

    @GetMapping(value = "staticScanner/started")
    public String getStarted() throws InterruptedException {
        return "staticScanner/productUploader";
    }

    @PostMapping(value = "staticScanner/productCloned")
    public String productCloned(@RequestParam String containerId, @RequestParam String url, @RequestParam String branch, @RequestParam String tag) throws InterruptedException {
        staticScannerController.configureNotificationManager(containerId);
        String cloned = staticScannerController.clone(containerId, url, branch, tag);

        return "staticScanner/scanners";
    }

    @PostMapping(value = "staticScanner/productUploaded")
    public String productZipUploaded(@RequestParam String containerId, @RequestParam MultipartFile zipFile) throws InterruptedException {
        staticScannerController.configureNotificationManager(containerId);
        try {
            String cloned = staticScannerController.uploadProductZipFileAndExtract(containerId, zipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "staticScanner/scanners";
    }


    @GetMapping(value = "staticScanner/test1")
    public String test1() throws InterruptedException {
        return "staticScanner/scanners";

    }

    @PostMapping(value = "dynamicScanner/started")
    public String dynamicScannerStarted(Model model, @RequestParam String ipAddress, @RequestParam int containerPort, @RequestParam int hostPort) throws InterruptedException {
        String containerId = dynamicScannerController.start(ipAddress, containerPort, hostPort);
        if (containerId != null) {

            model.addAttribute("containerId", containerId);
            return "dynamicScanner/scanner";
        }
        return mainScanners();
    }

    @GetMapping(value = "dynamicScanner/started")
    public String test4() throws InterruptedException {
        return "dynamicScanner/scanner";
    }
}

