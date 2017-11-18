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

package org.wso2.security.automation.manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.automation.manager.entity.scanner.dynamic.ZapEntity;
import org.wso2.security.automation.manager.handler.DockerHandler;
import org.wso2.security.automation.manager.repository.ZapRepository;

/**
 * Zap service
 *
 * @author Deshani Geethika
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class ZapService {

    private static final String STATUS_REMOVED = "removed";

    private final ZapRepository zapRepository;

    @Autowired
    public ZapService(ZapRepository zapRepository) {
        this.zapRepository = zapRepository;
    }

    public Object findAll() {
        return zapRepository.findAll();
    }

    public ZapEntity findOne(int id) {

        return zapRepository.findOne(id);
    }

    public ZapEntity findOneByContainerId(String containerId) {
        return zapRepository.findOneByContainerId(containerId);
    }

    public Iterable<ZapEntity> findByUserId(String userId) {
        return zapRepository.findByUserId(userId);
    }

    public ZapEntity save(ZapEntity zap) {
        return zapRepository.save(zap);
    }

    public void kill(String containerId) {
        ZapEntity zap = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        DockerHandler.removeContainer(containerId);
        zap.setStatus(STATUS_REMOVED);
        save(zap);
    }
}
