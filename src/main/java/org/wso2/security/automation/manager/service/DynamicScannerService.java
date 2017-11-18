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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.entity.scanner.dynamic.DynamicScannerEntity;
import org.wso2.security.automation.manager.exception.AutomationManagerRuntimeException;
import org.wso2.security.automation.manager.handler.DockerHandler;
import org.wso2.security.automation.manager.handler.FileHandler;
import org.wso2.security.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.automation.manager.handler.MailHandler;
import org.wso2.security.automation.manager.config.ScannerProperty;
import org.wso2.security.automation.manager.repository.DynamicScannerRepository;
import org.wso2.security.automation.manager.scanner.dynamic.DynamicScanner;
import org.wso2.security.automation.manager.scanner.dynamic.zap.ZapScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;

/**
 * Dynamic scanner service
 *
 * @author Deshani Geethika
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@PropertySource("classpath:scanner.properties")
@Service
public class DynamicScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);

    private static final String STATUS_REMOVED = "removed";
    private static final String DATE_PATTERN = "yyyy-MM-dd:HH.mm.ss";

    private final DynamicScannerRepository dynamicScannerRepository;

    @Value("${scanner.dynamic.port}")
    private int productPort;

    private final ZapService zapService;
    private final MailHandler mailHandler;

    @Autowired
    public DynamicScannerService(DynamicScannerRepository dynamicScannerRepository, MailHandler mailHandler,
                                 ZapService zapService) {
        this.dynamicScannerRepository = dynamicScannerRepository;
        this.mailHandler = mailHandler;
        this.zapService = zapService;
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

    public void startScan(String userId, String testName, String ipAddress, String productName, String wumLevel,
                          boolean isFileUpload, MultipartFile zipFile, MultipartFile urlListFile, String wso2ServerHost,
                          int wso2ServerPort, boolean isAuthenticatedScan) {

        String urlListFileName;
        String zipFileName = null;
        String uploadLocation = ScannerProperty.getTempFolderPath() + File.separator + userId +
                new SimpleDateFormat(DATE_PATTERN).format(new Date());

        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new AutomationManagerRuntimeException("Please upload a zip file");
            } else {
                if (new File(ScannerProperty.getTempFolderPath()).exists() || new File(ScannerProperty.getTempFolderPath()).mkdir()) {
                    if (new File(uploadLocation).exists() || new File(uploadLocation).mkdir()) {
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

        if (new File(ScannerProperty.getTempFolderPath()).exists() || new File(ScannerProperty.getTempFolderPath()).mkdir()) {
            if (new File(uploadLocation).exists() || new File(uploadLocation).mkdir()) {
                urlListFileName = urlListFile.getOriginalFilename();
                if (FileHandler.uploadFile(urlListFile, uploadLocation + File.separator + urlListFileName)) {
                    DynamicScanner zapScanner = new ZapScanner(userId, testName, ipAddress, productName,
                            wumLevel, isFileUpload, uploadLocation, urlListFileName, zipFileName,
                            wso2ServerHost, wso2ServerPort, ipAddress);
                    new Thread(zapScanner).start();
                }
            } else {
                throw new AutomationManagerRuntimeException("Cannot upload files to temp location");
            }
        }
    }

    public void getReportAndMail(String containerId, String reportFilePath) {
        try {
            DynamicScannerEntity dynamicScannerEntity = findOneByContainerId(containerId);
            String subject = "Dynamic Scan Report: ";
            if (mailHandler.sendMail(dynamicScannerEntity.getUserId(), subject, "This is auto generated message",
                    new FileInputStream(new File(reportFilePath)), "ZapReport.html")) {
                dynamicScannerEntity.setReportSent(true);
                dynamicScannerEntity.setReportSentTime(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
                kill(containerId);
            }
        } catch (Exception e) {
            throw new AutomationManagerRuntimeException("Error occurred while getting dynamic scanner report and mail");
        }
    }


    public HttpResponse getReport(HttpServletResponse response, String reportFilePath) {
        if (new File(reportFilePath).exists()) {
            try {
                InputStream inputStream = new FileInputStream(reportFilePath);
                IOUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
                LOGGER.info("Successfully write to output stream");
                return (HttpResponse) response;
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.toString());
            }
        } else {
            LOGGER.error("Report is not found");
        }
        return null;
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
        dynamicScanner.setScanProgressTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        save(dynamicScanner);
    }

    public void updateReportReady(String containerId, boolean status) {
        DynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setReportReady(status);
        dynamicScanner.setReportReadyTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
        save(dynamicScanner);
        getReportAndMail(containerId,"//home/deshani/Documents/ZapReport.html");
    }

    public void updateMessage(String containerId, String status) {
        DynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setMessage(status);
        save(dynamicScanner);
    }
}
