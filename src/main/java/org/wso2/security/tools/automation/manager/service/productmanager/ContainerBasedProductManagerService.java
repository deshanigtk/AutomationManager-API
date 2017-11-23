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
package org.wso2.security.tools.automation.manager.service.productmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.productmanager.containerbased
        .ContainerBasedProductManagerEntity;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.repository.productmanager.ContainerBasedProductManagerRepository;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class ContainerBasedProductManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerBasedProductManagerService.class);
    private final ContainerBasedProductManagerRepository productManagerRepository;

    @Autowired
    public ContainerBasedProductManagerService(ContainerBasedProductManagerRepository productManagerRepository) {
        this.productManagerRepository = productManagerRepository;
    }

    public Iterable<ContainerBasedProductManagerEntity> findAll() {
        return productManagerRepository.findAll();
    }

    public ContainerBasedProductManagerEntity findOne(int id) {
        return productManagerRepository.findOne(id);
    }

    public ContainerBasedProductManagerEntity findOneByContainerId(String containerId) {
        return productManagerRepository.findOneByContainerId(containerId);
    }

    public Iterable<ContainerBasedProductManagerEntity> findByUserId(String userId) {
        return productManagerRepository.findByUserId(userId);
    }

    public ContainerBasedProductManagerEntity save(ContainerBasedProductManagerEntity productManagerEntity) {
        return productManagerRepository.save(productManagerEntity);
    }

    public void updateFileUploaded(String containerId, boolean status) {
        ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setFileUploaded(status);
        productManagerEntity.setFileUploadedTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new
                Date()));
        save(productManagerEntity);
    }

    public void updateFileExtracted(String containerId, boolean status) {
        ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setFileExtracted(status);
        productManagerEntity.setFileExtractedTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new
                Date()));
        save(productManagerEntity);
    }

    public void updateServerStarted(String containerId, boolean status) {
        ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setServerStarted(status);
        productManagerEntity.setServerStartedTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new
                Date()));
        save(productManagerEntity);
    }

    public void kill(String containerId) {
        ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        DockerHandler.removeContainer(containerId);
        productManagerEntity.setStatus(ScannerProperties.getStatusRemoved());
        save(productManagerEntity);
    }

}
