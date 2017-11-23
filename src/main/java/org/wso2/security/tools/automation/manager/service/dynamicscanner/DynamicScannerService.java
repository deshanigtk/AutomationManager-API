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

package org.wso2.security.tools.automation.manager.service.dynamicscanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.FileHandler;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.DynamicScannerRepository;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScannerExecutor;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.cloudbased.CloudBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.cloudbased.CloudBasedDynamicScannerEnum;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.ContainerBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased
        .ContainerBasedDynamicScannerEnum;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.factory.AbstractDynamicScannerFactory;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.factory.DynamicScannerFactoryProducer;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.CloudBasedProductManager;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.ContainerBasedProductManager;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.ProductManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Dynamic scanner service level methods
 *
 * @author Deshani Geethika
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class DynamicScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);

    private final DynamicScannerRepository dynamicScannerRepository;

    @Autowired
    public DynamicScannerService(DynamicScannerRepository dynamicScannerRepository) {
        this.dynamicScannerRepository = dynamicScannerRepository;
    }

    public Iterable<DynamicScannerEntity> findAll() {
        return dynamicScannerRepository.findAll();
    }

    public DynamicScannerEntity findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    public Iterable<DynamicScannerEntity> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    public DynamicScannerEntity save(DynamicScannerEntity dynamicScanner) {
        return dynamicScannerRepository.save(dynamicScanner);
    }

    public void startScan(String scanType, String userId, String testName, String productName, String wumLevel,
                          boolean isFileUpload, MultipartFile zipFile, MultipartFile urlListFile, String
                                  wso2ServerHost, int wso2ServerPort, String scannerHost, int scannerPort) throws
            AutomationManagerException {

        String fileUploadLocation = ScannerProperties.getTempFolderPath() + File.separator + userId + new
                SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
        String urlListFileName = urlListFile.getOriginalFilename();
        DynamicScanner dynamicScanner = null;
        ProductManager productManager = null;

        uploadFilesToTempDirectory(fileUploadLocation, isFileUpload, zipFile, urlListFile);
        if (isCloudBasedDynamicScanner(scanType)) {
            dynamicScanner = createAndInitCloudBasedDynamicScanner(scanType, userId, fileUploadLocation,
                    urlListFileName, scannerHost, scannerPort);
        } else if (isContainerBasedDynamicScanner(scanType)) {
            dynamicScanner = createAndInitContainerBasedDynamicScanner(scanType, userId, fileUploadLocation,
                    urlListFileName);
        }

        if (dynamicScanner == null) {
            throw new AutomationManagerException("Error occurred while creating dynamic scanner");
        }
        if (isFileUpload && zipFile != null) {
            productManager = createAndInitContainerBasedProductManager(userId, testName, productName, wumLevel,
                    fileUploadLocation, zipFile.getOriginalFilename(), dynamicScanner.getId());
        } else if (!isFileUpload) {
            productManager = createAndInitCloudBasedProductManager(userId, testName, productName, wumLevel,
                    wso2ServerHost, wso2ServerPort, dynamicScanner.getId());
        }
        if (productManager == null) {
            throw new AutomationManagerException("Error occurred while creating product manager");
        }
        DynamicScannerExecutor executor = new DynamicScannerExecutor(productManager, dynamicScanner);
        new Thread(executor).start();
    }

    private void uploadFilesToTempDirectory(String fileUploadLocation, boolean isFileUpload, MultipartFile zipFile,
                                            MultipartFile urlListFile) throws AutomationManagerException {
        File tempDirectory = new File(ScannerProperties.getTempFolderPath());
        File uploadDirectory = new File(fileUploadLocation);
        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new AutomationManagerException("Please upload a zip file");
            }
        }
        if (tempDirectory.exists() || tempDirectory.mkdir()) {
            if (uploadDirectory.exists() || uploadDirectory.mkdir()) {
                String zipFileName = zipFile.getOriginalFilename();
                String urlListFileName = urlListFile.getOriginalFilename();
                if (isFileUpload && !FileHandler.uploadFile(zipFile, fileUploadLocation + File.separator +
                        zipFileName)) {
                    throw new AutomationManagerException("Cannot upload zip file");
                }
                if (!FileHandler.uploadFile(zipFile, fileUploadLocation + File.separator + urlListFileName)) {
                    throw new AutomationManagerException("Cannot upload URL list file");
                }
            } else {
                throw new AutomationManagerException("File upload location is not available");
            }
        } else {
            throw new AutomationManagerException("Temp directory is not available");
        }
    }

    /**
     * Check if a given scan type is a cloud based one.
     *
     * @param scanType Scan type
     * @return Boolean to indicate the scanner is a cloud based
     */
    private boolean isCloudBasedDynamicScanner(String scanType) {
        for (CloudBasedDynamicScannerEnum e : CloudBasedDynamicScannerEnum.values()) {
            if (e.name().equalsIgnoreCase(scanType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isContainerBasedDynamicScanner(String scanType) {
        for (ContainerBasedDynamicScannerEnum e : ContainerBasedDynamicScannerEnum.values()) {
            if (e.name().equalsIgnoreCase(scanType)) {
                return true;
            }
        }
        return false;
    }

    private CloudBasedDynamicScanner createAndInitCloudBasedDynamicScanner(String scanType, String userId, String
            fileUploadLocation, String urlListFileName, String scannerHost, int scannerPort) throws
            AutomationManagerException {
        String factoryType = "cloud";
        AbstractDynamicScannerFactory dynamicScannerFactory = DynamicScannerFactoryProducer.getDynamicScannerFactory
                (factoryType);
        if (dynamicScannerFactory == null) {
            throw new AutomationManagerException("Cannot create dynamic scanner factory");
        }
        CloudBasedDynamicScanner dynamicScanner = dynamicScannerFactory.getCloudBasedDynamicScanner(scanType);
        if (dynamicScanner == null) {
            throw new AutomationManagerException("Dynamic scanner cannot be created");
        }
        dynamicScanner.init(userId, fileUploadLocation, urlListFileName, scannerHost, scannerPort);
        return dynamicScanner;
    }

    private ContainerBasedDynamicScanner createAndInitContainerBasedDynamicScanner(String scanType, String userId,
                                                                                   String fileUploadLocation, String
                                                                                           urlListFileName) throws
            AutomationManagerException {
        String factoryType = "container";
        AbstractDynamicScannerFactory dynamicScannerFactory = DynamicScannerFactoryProducer.getDynamicScannerFactory
                (factoryType);
        if (dynamicScannerFactory == null) {
            throw new AutomationManagerException("Cannot create dynamic scanner factory");
        }
        ContainerBasedDynamicScanner dynamicScanner = dynamicScannerFactory.getContainerBasedDynamicScanner(scanType);
        if (dynamicScanner == null) {
            throw new AutomationManagerException("Dynamic scanner cannot be created");
        }
        dynamicScanner.init(userId, ScannerProperties.getIpAddress(), fileUploadLocation, urlListFileName);
        return dynamicScanner;
    }

    private ContainerBasedProductManager createAndInitContainerBasedProductManager(String userId, String testName,
                                                                                   String productName, String wumLevel,
                                                                                   String fileUploadLocation, String
                                                                                           zipFileName, int
                                                                                           dynamicScannerId) {
        ContainerBasedProductManager productManager = new ContainerBasedProductManager();
        productManager.init(userId, testName, ScannerProperties.getIpAddress(), productName, wumLevel,
                fileUploadLocation,
                zipFileName, dynamicScannerId);
        return productManager;
    }

    private CloudBasedProductManager createAndInitCloudBasedProductManager(String userId, String testName,
                                                                           String productName, String wumLevel,
                                                                           String wso2serverHost, int wso2ServerPort,
                                                                           int dynamicScannerId) {
        CloudBasedProductManager productManager = new CloudBasedProductManager();
        productManager.init(userId, testName, ScannerProperties.getIpAddress(), productName, wumLevel, wso2serverHost,
                wso2ServerPort, dynamicScannerId);
        return productManager;
    }

}