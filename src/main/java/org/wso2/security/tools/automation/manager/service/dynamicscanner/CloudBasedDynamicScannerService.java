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
package org.wso2.security.tools.automation.manager.service.dynamicscanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.cloudbased.CloudBasedDynamicScannerEntity;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.CloudBasedDynamicScannerRepository;

@Service
public class CloudBasedDynamicScannerService {

    private final CloudBasedDynamicScannerRepository dynamicScannerRepository;

    @Autowired
    public CloudBasedDynamicScannerService(CloudBasedDynamicScannerRepository dynamicScannerRepository) {
        this.dynamicScannerRepository = dynamicScannerRepository;
    }

    public Iterable<CloudBasedDynamicScannerEntity> findAll() {
        return dynamicScannerRepository.findAll();
    }

    public CloudBasedDynamicScannerEntity findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    public Iterable<CloudBasedDynamicScannerEntity> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    public CloudBasedDynamicScannerEntity save(CloudBasedDynamicScannerEntity dynamicScanner) {
        return dynamicScannerRepository.save(dynamicScanner);
    }

}
