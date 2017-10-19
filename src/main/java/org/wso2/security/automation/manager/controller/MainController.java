package org.wso2.security.automation.manager.controller;/*
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.service.DynamicScannerService;
import org.wso2.security.automation.manager.service.StaticScannerService;

@Controller
@RequestMapping("automationManager")
public class MainController {

    private final StaticScannerService staticScannerService;
    private final DynamicScannerService dynamicScannerService;

    @Autowired
    public MainController(StaticScannerService staticScannerService, DynamicScannerService dynamicScannerService) {
        this.staticScannerService = staticScannerService;
        this.dynamicScannerService = dynamicScannerService;
    }

    @GetMapping(value = "getStaticScanners")
    @ResponseBody
    public Iterable<StaticScanner> getStaticScanners(String userId) {
        return staticScannerService.findByUserId(userId);
    }

    @GetMapping(value = "getDynamicScanners")
    @ResponseBody
    public Iterable<DynamicScanner> getDynamicScanners(String userId) {
        return dynamicScannerService.findByUserId(userId);
    }
}
