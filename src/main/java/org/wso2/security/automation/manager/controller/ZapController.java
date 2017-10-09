package org.wso2.security.automation.manager.controller;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wso2.security.automation.manager.entity.Zap;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.service.ZapService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("/zap")
public class ZapController {


    @Value("${ZAP_DOCKER_IMAGE}")
    private String dockerImage;

    private static List<String> command;

    @Autowired
    private ZapService zapService;

    @PostMapping(value = "/start")
    public @ResponseBody
    String start(@RequestParam String ipAddress, @RequestParam String containerPort, @RequestParam String hostPort) {
        if (DockerHandler.pullImage(dockerImage)) {
            command = Arrays.asList("zap.sh", "-daemon", "-config", "api.disablekey=true", "-config",
                    "api.addrs.addr.name=.*", "-config", "api.addrs.addr.regex=true", "-port", containerPort, "-host", ipAddress);

            Iterable<Zap> runningContainers = zapService.findByStatus("running");
            if (!runningContainers.iterator().hasNext()) {
                String containerId = DockerHandler.createContainer(dockerImage, ipAddress, containerPort, hostPort, command, null);

                if (containerId != null) {
                    String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                    Zap zap = new Zap(containerId, createdTime, ipAddress, containerPort, hostPort);
                    zapService.save(zap);

                    if (DockerHandler.startContainer(containerId)) {
                        zap = zapService.findOne(containerId);
                        zap.setStatus("running");
                        zapService.save(zap);
                        return containerId;
                    }
                }
            }
        }
        return null;
    }

}
