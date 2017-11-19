package org.wso2.security.automation.manager.service;/*
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.wso2.security.automation.manager.entity.scanner.dynamic.ProductManagerEntity;
import org.wso2.security.automation.manager.repository.ProductManagerRepository;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class ProductManagerService {
    private final ProductManagerRepository productManagerRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductManagerService.class);

    @Autowired
    public ProductManagerService(ProductManagerRepository productManagerRepository) {
        this.productManagerRepository = productManagerRepository;
    }

    public Iterable<ProductManagerEntity> findAll() {
        return productManagerRepository.findAll();
    }

    public ProductManagerEntity findOne(int id) {
        return productManagerRepository.findOne(id);
    }

    public ProductManagerEntity findOneByContainerId(String containerId) {
        return productManagerRepository.findOneByContainerId(containerId);
    }

    public Iterable<ProductManagerEntity> findByUserId(String userId) {
        return productManagerRepository.findByUserId(userId);
    }

    public ProductManagerEntity save(ProductManagerEntity dynamicScanner) {
        return productManagerRepository.save(dynamicScanner);
    }

    public void updateFileUploaded(String containerId, boolean status) {
        ProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setFileUploaded(status);
        productManagerEntity.setFileUploadedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        save(productManagerEntity);
    }

    public void updateFileExtracted(String containerId, boolean status){
        ProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setFileExtracted(status);
        productManagerEntity.setFileExtractedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        save(productManagerEntity);
    }

    public void updateServerStarted(String containerId, boolean status){
        ProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setServerStarted(status);
        productManagerEntity.setServerStartedTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        save(productManagerEntity);
    }
}
