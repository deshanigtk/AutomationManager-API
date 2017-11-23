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

package org.wso2.security.tools.automation.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.StaticScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.FileHandler;
import org.wso2.security.tools.automation.manager.repository.staticscanner.StaticScannerRepository;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.StaticScanner;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.factory.StaticScannerFactoryProducer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Static scanner service
 *
 * @author Deshani Geethika
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Service
public class StaticScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticScannerService.class);

    private final StaticScannerRepository staticScannerRepository;

    @Autowired
    public StaticScannerService(StaticScannerRepository staticScannerRepository) {
        this.staticScannerRepository = staticScannerRepository;
    }

    public Iterable<StaticScannerEntity> findAll() {
        return staticScannerRepository.findAll();
    }

    public StaticScannerEntity findOne(int id) {
        return staticScannerRepository.findOne(id);
    }

    public Iterable<StaticScannerEntity> findByUserId(String userId) {
        return staticScannerRepository.findByUserId(userId);
    }

    public StaticScannerEntity save(StaticScannerEntity staticScanner) {
        return staticScannerRepository.save(staticScanner);
    }

    public void startScan(String scanType, String userId, String testName, String productName,
                          String wumLevel, boolean isFileUpload, MultipartFile zipFile, String gitUrl, String
                                  gitUsername, String gitPassword) throws AutomationManagerException {
        String zipFileName = null;
        String uploadLocation = ScannerProperties.getTempFolderPath() + File.separator + userId + new
                SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
        String ipAddress = ScannerProperties.getIpAddress();

        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new AutomationManagerException("Please upload product zip file");
            } else {
                if (new File(ScannerProperties.getTempFolderPath()).exists() || new File(ScannerProperties
                        .getTempFolderPath()).mkdir()) {
                    if (new File(uploadLocation).exists() || new File(uploadLocation).mkdir()) {
                        zipFileName = zipFile.getOriginalFilename();
                        if (!FileHandler.uploadFile(zipFile, uploadLocation + File.separator + zipFileName)) {
                            throw new AutomationManagerException("Cannot upload zip file");
                        }
                    } else {
                        throw new AutomationManagerException("Error occurred while creating upload location");
                    }
                } else {
                    throw new AutomationManagerException("Error occurred while creating temp folder");
                }
            }
        } else {
            if (gitUrl == null) {
                throw new AutomationManagerException("Please enter URL to clone");
            }
        }
        StaticScannerFactoryProducer staticScannerFactoryProducer = new StaticScannerFactoryProducer();
        StaticScanner staticScanner = staticScannerFactoryProducer.getStaticScanner(scanType);
        staticScanner.init(userId, testName, ipAddress, productName, wumLevel, isFileUpload, uploadLocation,
                zipFileName, gitUrl, gitUsername, gitPassword);
        new Thread(staticScanner).start();
    }

}
