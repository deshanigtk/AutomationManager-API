package org.wso2.security.automationmanager.service;/*
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
import org.springframework.stereotype.Service;
import org.wso2.security.automationmanager.entity.DynamicScanner;
import org.wso2.security.automationmanager.repository.DynamicScannerRepository;

@Service
public class DynamicScannerService {

    @Autowired
    private DynamicScannerRepository dynamicScannerRepository;

    public Object findAll() {
        return dynamicScannerRepository.findAll();
    }

    public DynamicScanner findOne(String containerId) {

        return dynamicScannerRepository.findOne(containerId);
    }
    public Iterable<DynamicScanner> findByUserIdAndStatus(String userId, String status) {
        return dynamicScannerRepository.findByUserIdAndStatus(userId, status);
    }


    public DynamicScanner save(DynamicScanner dynamicScanner) {
        return dynamicScannerRepository.save(dynamicScanner);
    }
}
