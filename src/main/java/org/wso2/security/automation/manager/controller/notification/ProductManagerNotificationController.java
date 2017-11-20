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
import org.springframework.web.bind.annotation.*;
import org.wso2.security.automation.manager.service.ProductManagerService;

@Controller
@RequestMapping("dynamicScanner/notify")
@Api(value = "dynamicScannerNotifications", description = "Dynamic DependencyCheckScanner containers notify status after a task is completed")
public class ProductManagerNotificationController {
    private final ProductManagerService productManagerService;

    @Autowired
    public ProductManagerNotificationController(ProductManagerService productManagerService) {
        this.productManagerService = productManagerService;
    }

    @GetMapping(value = "fileUploaded")
    @ApiOperation(value = "Update that a zip file is uploaded to the container")
    public @ResponseBody
    void updateFileUploaded(@RequestParam String containerId, @RequestParam boolean status) {
        productManagerService.updateFileUploaded(containerId, status);
    }

    @GetMapping(value = "fileExtracted")
    @ApiOperation(value = "Update that a zip file is extracted to the container")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status) {
        productManagerService.updateFileExtracted(containerId, status);
    }

    @GetMapping(value = "serverStarted")
    @ApiOperation(value = "Update that a server is started inside the container")
    public @ResponseBody
    void updateServerStarted(@RequestParam String containerId, @RequestParam boolean status) {
        productManagerService.updateServerStarted(containerId, status);
    }
}
