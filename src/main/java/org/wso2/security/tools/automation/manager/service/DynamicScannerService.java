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
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerRuntimeException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.FileHandler;
import org.wso2.security.tools.automation.manager.handler.MailHandler;
import org.wso2.security.tools.automation.manager.repository.DynamicScannerRepository;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScannerFactory;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.MainScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.ProductManager;

import java.io.File;
import java.io.FileInputStream;
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

    private static final String STATUS_REMOVED = "removed";
    private final DynamicScannerRepository dynamicScannerRepository;
    private final MailHandler mailHandler;

    @Autowired
    public DynamicScannerService(DynamicScannerRepository dynamicScannerRepository, MailHandler mailHandler) {
        this.dynamicScannerRepository = dynamicScannerRepository;
        this.mailHandler = mailHandler;
    }

    public Iterable<DynamicScannerEntity> findAll() {
        return dynamicScannerRepository.findAll();
    }

    public DynamicScannerEntity findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    public DynamicScannerEntity findOneByContainerId(String containerId) {
        return dynamicScannerRepository.findOneByContainerId(containerId);
    }

    public Iterable<DynamicScannerEntity> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    public DynamicScannerEntity save(DynamicScannerEntity dynamicScanner) {
        return dynamicScannerRepository.save(dynamicScanner);
    }

    public void startScan(String scanType, String userId, String testName, String productName,
                          String wumLevel, boolean isFileUpload, MultipartFile zipFile, MultipartFile urlListFile,
                          String wso2ServerHost, int wso2ServerPort, String scannerHost, int scannerPort) {
        String urlListFileName;
        String zipFileName = null;
        String ipAddress = ScannerProperties.getIpAddress();
        String uploadLocation = ScannerProperties.getTempFolderPath() + File.separator + userId + new
                SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date());
        File tempDirectory = new File(ScannerProperties.getTempFolderPath());
        File uploadDirectory = new File(uploadLocation);
        DynamicScannerFactory dynamicScannerFactory = new DynamicScannerFactory();

        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new AutomationManagerRuntimeException("Please upload a zip file");
            } else {
                if (tempDirectory.exists() || tempDirectory.mkdir()) {
                    if (uploadDirectory.exists() || uploadDirectory.mkdir()) {
                        zipFileName = zipFile.getOriginalFilename();
                        if (!FileHandler.uploadFile(zipFile, uploadLocation + File.separator + zipFileName)) {
                            throw new AutomationManagerRuntimeException("Cannot upload zip file");
                        }
                    } else {
                        throw new AutomationManagerRuntimeException("Error occurred while creating upload location");
                    }
                } else {
                    throw new AutomationManagerRuntimeException("Error occurred while creating temp folder");
                }
            }
        } else {
            if (wso2ServerHost == null || wso2ServerPort == -1) {
                throw new AutomationManagerRuntimeException("Please enter valid details of running WSO2 server");
            }
        }
        if (tempDirectory.exists() || tempDirectory.mkdir()) {
            if (uploadDirectory.exists() || uploadDirectory.mkdir()) {
                urlListFileName = urlListFile.getOriginalFilename();
                if (FileHandler.uploadFile(urlListFile, uploadLocation + File.separator + urlListFileName)) {

                    ProductManager productManager = new ProductManager(userId, testName, ipAddress, productName,
                            wumLevel, isFileUpload, uploadLocation, zipFileName,
                            wso2ServerHost, wso2ServerPort);
                    DynamicScanner dynamicScanner = dynamicScannerFactory.getDynamicScanner(scanType);
                    dynamicScanner.init(userId, ipAddress, isFileUpload, uploadLocation, urlListFileName,
                            wso2ServerHost, wso2ServerPort, scannerHost, scannerPort);
                    MainScanner mainScanner = new MainScanner(productManager, dynamicScanner);
                    new Thread(mainScanner).start();
                }
            } else {
                throw new AutomationManagerRuntimeException("Cannot upload files to temp location");
            }
        }
    }

    public void kill(String containerId) {
        DynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        DockerHandler.removeContainer(containerId);
        dynamicScanner.setStatus(STATUS_REMOVED);
        save(dynamicScanner);
    }

    public void updateScanStatus(String containerId, String status, int progress) {
        DynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setScanStatus(status);
        dynamicScanner.setScanProgress(progress);
        dynamicScanner.setScanProgressTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        save(dynamicScanner);
    }

    public void updateReportReady(String containerId, boolean status, String reportFilePath) {
        DynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setReportReady(status);
        dynamicScanner.setReportReadyTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        save(dynamicScanner);
        getReportAndMail(containerId, reportFilePath);
    }

    public void updateMessage(String containerId, String status) {
        DynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setMessage(status);
        save(dynamicScanner);
    }

    private void getReportAndMail(String containerId, String reportFilePath) {
        try {
            DynamicScannerEntity dynamicScannerEntity = findOneByContainerId(containerId);
            String subject = "Dynamic Scan Report: ";
            if (mailHandler.sendMail(dynamicScannerEntity.getUserId(), subject, "This is auto generated message",
                    new FileInputStream(new File(reportFilePath)), "ZapReport.html")) {
                dynamicScannerEntity.setReportSent(true);
                dynamicScannerEntity.setReportSentTime(new SimpleDateFormat(ScannerProperties.getDatePattern())
                        .format(new Date()));
                kill(containerId);
            }
        } catch (Exception e) {
            throw new AutomationManagerRuntimeException("Error occurred while getting dynamicscanner scanner report " +
                    "and mail");
        }
    }
}
