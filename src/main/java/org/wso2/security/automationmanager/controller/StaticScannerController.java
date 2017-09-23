package org.wso2.security.automationmanager.controller;
/*
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

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wso2.security.automationmanager.entity.StaticScanner;
import org.wso2.security.automationmanager.handlers.DockerHandler;
import org.wso2.security.automationmanager.handlers.HttpRequestHandler;
import org.wso2.security.automationmanager.service.StaticScannerService;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("/staticScanner")
public class StaticScannerController {

    @Value("${STATIC_SCANNER_DOCKER_IMAGE}")
    private String dockerImage;

    @Autowired
    private StaticScannerService staticScannerService;

    private final String RUN_FIND_SEC_BUGS = "/staticScanner/findSecBugs";

    @PostMapping(value = "/start")
    public @ResponseBody
    String start(@RequestParam String userId, @RequestParam String ipAddress, @RequestParam String containerPort, @RequestParam String hostPort) {
        if (DockerHandler.pullImage(dockerImage)) {
            String containerId = DockerHandler.createContainer(dockerImage, ipAddress, containerPort, hostPort, null);

            if (containerId != null) {
                String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                StaticScanner staticScanner = new StaticScanner(containerId, userId, createdTime, ipAddress, containerPort, hostPort);
                staticScannerService.save(staticScanner);

                if (DockerHandler.startContainer(containerId)) {
                    staticScanner = staticScannerService.findOne(containerId);
                    staticScanner.setStatus("running");
                    staticScannerService.save(staticScanner);
                    return containerId;
                }

            }
        }
        return null;
    }

    @PostMapping(value = "/cloneProductFromGitHub")
    public @ResponseBody
    void clone(@RequestParam String userId, @RequestParam String containerId, @RequestParam String gitUrl, @RequestParam String branch) {

    }

    @PostMapping(value = "/getRunningContainersByUser")
    public @ResponseBody
    Iterable<StaticScanner> clone(@RequestParam String userId) {
        return staticScannerService.findByUserIdAndStatus(userId, "running");
    }


    @PostMapping(value = "/runFindSecBugs")
    public @ResponseBody
    void runFindSecBugs(@RequestParam String userId, @RequestParam String containerId) {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);
            if (staticScanner.getStatus().equals("running") && staticScanner.getUserId().equals(userId) && staticScanner.isProductAvailable()) {

                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(Integer.parseInt(staticScanner.getHostPort())).setScheme("http").setPath(RUN_FIND_SEC_BUGS)
                        .build();

                HttpRequestHandler.sendGetRequest(uri.toString());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}