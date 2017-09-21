package org.wso2.security.automationmanager.controller;/*
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
import org.wso2.security.automationmanager.repository.StaticScannerRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("/staticScanner")
public class StaticScannerController {

    @Value("${static_scanner_docker_image}")
    private String dockerImage;

    @Autowired
    private StaticScannerRepository staticScannerRepository;

    private final String RUN_FIND_SEC_BUGS = "/staticScanner/findSecBugs";

    @PostMapping(path = "/start")
    public @ResponseBody
    void start(@RequestParam String userId, @RequestParam String ipAddress, @RequestParam String containerPort, @RequestParam String hostPort) {
        if (DockerHandler.pullImage(dockerImage)) {
            String containerId = DockerHandler.createContainer(dockerImage, ipAddress, containerPort, hostPort, new String[]{});

            if (containerId != null) {
                SimpleDateFormat createdTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                StaticScanner staticScanner = new StaticScanner(containerId, userId, createdTime, ipAddress, containerPort, hostPort);
                staticScannerRepository.save(staticScanner);

                if (DockerHandler.startContainer(containerId)) {
                    staticScanner = staticScannerRepository.findOne(containerId);
                    staticScanner.setStatus("running");
                }
            }
        }
    }

    @PostMapping(path = "/cloneProductFromGitHub")
    public @ResponseBody
    void clone(@RequestParam String userId, @RequestParam String gitUrl, @RequestParam String branch) {

    }

    @PostMapping(path = "/getRunningContainersByUser")
    public @ResponseBody
    Iterable<StaticScanner> clone(@RequestParam String userId) {
        return staticScannerRepository.findByUserIdAndStatus(userId, "running");
    }


    @PostMapping(path = "/runFindSecBugs")
    public @ResponseBody
    void runFindSecBugs(@RequestParam String userId, @RequestParam String containerId) {
        try {
            StaticScanner staticScanner = staticScannerRepository.findOne(containerId);
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