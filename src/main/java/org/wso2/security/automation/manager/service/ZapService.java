package org.wso2.security.automation.manager.service;
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
import org.springframework.stereotype.Service;
import org.wso2.security.automation.manager.entity.Zap;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.repository.ZapRepository;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@PropertySource("classpath:global.properties")
public class ZapService {

    @Value("${ZAP_DOCKER_IMAGE}")
    private String dockerImage;


    private final ZapRepository zapRepository;

    @Autowired
    public ZapService(ZapRepository zapRepository) {
        this.zapRepository = zapRepository;
    }

    public Object findAll() {
        return zapRepository.findAll();
    }

    public Zap findOne(int id) {

        return zapRepository.findOne(id);
    }

    public Zap findOneByContainerId(String containerId) {
        return zapRepository.findOneByContainerId(containerId);
    }

    public Iterable<Zap> findByStatus(String status) {
        return zapRepository.findByStatusEquals(status);
    }

    public Zap save(Zap zap) {
        return zapRepository.save(zap);
    }

    public Zap
    startZap(String userId, String name, String ipAddress) {
        Zap zap = new Zap();
        zap.setUserId(userId);
        zap.setName(name);
        zap.setStatus("initiated");
        save(zap);

        int port = calculatePort(zap.getId());

        List<String> command = Arrays.asList("zap.sh", "-daemon", "-config", "api.disablekey=true", "-config",
                "api.addrs.addr.name=.*", "-config", "api.addrs.addr.regex=true", "-port", String.valueOf(port), "-host", "0.0.0.0");

        String containerId = DockerHandler.createContainer(dockerImage, ipAddress, String.valueOf(port),
                String.valueOf(port), command, null);

        if (containerId != null) {
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            zap.setContainerId(containerId);
            zap.setIpAddress(ipAddress);
            zap.setContainerPort(port);
            zap.setHostPort(port);
            zap.setStatus("created");
            zap.setCreatedTime(createdTime);

            save(zap);

            if (DockerHandler.startContainer(containerId)) {
                zap.setStatus("running");
                zap.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                save(zap);
                return zap;
            }
        }
        return null;
    }

    private int calculatePort(int id) {
        if (20000 + id > 40000) {
            id = 1;
        }
        return (20000 + id) % 40000;
    }

    public void kill(String containerId) {
        Zap zap = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        zap.setStatus("killed");
        save(zap);
    }
}
