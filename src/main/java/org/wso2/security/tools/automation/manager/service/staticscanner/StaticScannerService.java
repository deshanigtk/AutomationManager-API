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

package org.wso2.security.tools.automation.manager.service.staticscanner;

import org.apache.commons.io.FilenameUtils;
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
import org.wso2.security.tools.automation.manager.scanner.staticscanner.StaticScannerExecutor;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.cloudbased.CloudBasedStaticScanner;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.cloudbased.CloudBasedStaticScannerEnum;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.ContainerBasedStaticScanner;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.ContainerBasedStaticScannerEnum;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.factory.AbstractStaticScannerFactory;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.factory.StaticScannerFactoryProducer;

import java.io.File;
import java.io.IOException;
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
        String fileUploadLocation = ScannerProperties.getTempFolderPath() + File.separator + userId + new
                SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
        StaticScanner staticScanner = null;
        try {
            if (isFileUpload) {
                if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                    throw new AutomationManagerException("Please upload product zip file");
                }
                zipFileName = zipFile.getOriginalFilename();
                uploadFileToTempDirectory(fileUploadLocation, zipFile);
            } else {
                if (gitUrl == null) {
                    throw new AutomationManagerException("Please enter URL to clone");
                }
            }
            if (isCloudBasedStaticScanner(scanType)) {
//                staticScanner = createAndInitCloudBasedStaticScanner(scanType,userId,ScannerProperties.getIpAddress()
//                        ,fileUploadLocation,zipFileName, null, 0);
            } else if (isContainerBasedStaticScanner(scanType)) {
                staticScanner = createAndInitContainerBasedDynamicScanner(scanType, userId, testName,
                        ScannerProperties.getIpAddress(), productName, wumLevel, isFileUpload, uploadLocation,
                        zipFileName, gitUrl, gitUsername, gitPassword);
            }

            if (staticScanner == null) {
                throw new AutomationManagerException("Error occurred while creating dynamic scanner");
            }

            StaticScannerExecutor staticScannerExecutor = new StaticScannerExecutor(staticScanner);
            new Thread(staticScannerExecutor).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadFileToTempDirectory(String fileUploadLocation, MultipartFile file) throws IOException {
        File tempDirectory = new File(ScannerProperties.getTempFolderPath());
        File uploadDirectory = new File(fileUploadLocation);

        if (tempDirectory.exists() || tempDirectory.mkdir()) {
            if (uploadDirectory.exists() || uploadDirectory.mkdir()) {
                String filename = file.getOriginalFilename();
                FileHandler.uploadFile(file, fileUploadLocation + File.separator + filename);
            }
        }
    }

    private boolean isCloudBasedStaticScanner(String scanType) {
        for (CloudBasedStaticScannerEnum e : CloudBasedStaticScannerEnum.values()) {
            if (e.name().equalsIgnoreCase(scanType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isContainerBasedStaticScanner(String scanType) {
        for (ContainerBasedStaticScannerEnum e : ContainerBasedStaticScannerEnum.values()) {
            if (e.name().equalsIgnoreCase(scanType)) {
                return true;
            }
        }
        return false;
    }

    private CloudBasedStaticScanner createAndInitCloudBasedStaticScanner(String scanType, String userId, String
            fileUploadLocation, String filename, String scannerHost, int scannerPort) throws
            AutomationManagerException {
        String factoryType = "cloud";
        AbstractStaticScannerFactory staticScannerFactory = StaticScannerFactoryProducer.getStaticScannerFactory
                (factoryType);
        if (staticScannerFactory == null) {
            throw new AutomationManagerException("Cannot create dynamic scanner factory");
        }
        CloudBasedStaticScanner staticScanner = staticScannerFactory.getCloudBasedStaticScanner(scanType);
        if (staticScanner == null) {
            throw new AutomationManagerException("Dynamic scanner cannot be created");
        }
//        staticScanner.init(userId, fileUploadLocation, filename, scannerHost, scannerPort);
        return staticScanner;
    }


    private ContainerBasedStaticScanner createAndInitContainerBasedDynamicScanner(String scanType, String userId,
                                                                                  String testName, String ipAddress,
                                                                                  String productName, String wumLevel,
                                                                                  boolean isFileUpload, String
                                                                                          uploadLocation, String
                                                                                          zipFileName, String gitUrl,
                                                                                  String gitUsername, String
                                                                                          gitPassword) throws
            AutomationManagerException {
        String factoryType = "container";
        AbstractStaticScannerFactory staticScannerFactory = StaticScannerFactoryProducer.getStaticScannerFactory
                (factoryType);
        if (staticScannerFactory == null) {
            throw new AutomationManagerException("Cannot create dynamic scanner factory");
        }
        ContainerBasedStaticScanner staticScanner = staticScannerFactory.getContainerBasedStaticScanner(scanType);
        if (staticScanner == null) {
            throw new AutomationManagerException("Dynamic scanner cannot be created");
        }
        staticScanner.init(userId, testName, ipAddress, productName, wumLevel, isFileUpload, uploadLocation,
                zipFileName, gitUrl, gitUsername, gitPassword);
        return staticScanner;
    }
}