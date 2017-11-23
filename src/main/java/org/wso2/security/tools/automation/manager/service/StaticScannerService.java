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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.StaticScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.FileHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.MailHandler;
import org.wso2.security.tools.automation.manager.repository.StaticScannerRepository;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.StaticScanner;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.factory.StaticScannerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    private static final String STATUS_REMOVED = "removed";
    private final StaticScannerRepository staticScannerRepository;
    private final MailHandler mailHandler;

    @Autowired
    public StaticScannerService(StaticScannerRepository staticScannerRepository, MailHandler mailHandler) {
        this.staticScannerRepository = staticScannerRepository;
        this.mailHandler = mailHandler;
    }

    public Iterable<StaticScannerEntity> findAll() {
        return staticScannerRepository.findAll();
    }

    public StaticScannerEntity findOne(int id) {
        return staticScannerRepository.findOne(id);
    }

    public StaticScannerEntity findOneByContainerId(String containerId) {
        return staticScannerRepository.findOneByContainerId(containerId);
    }

    public Iterable<StaticScannerEntity> findByUserId(String userId) {
        return staticScannerRepository.findByUserId(userId);
    }

    public StaticScannerEntity save(StaticScannerEntity staticScanner) {
        return staticScannerRepository.save(staticScanner);
    }

    public void startScan(String scanType, String userId, String testName, String productName,
                          String wumLevel, boolean isFileUpload, MultipartFile zipFile, String gitUrl, String
                                  gitUsername, String gitPassword) {
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
        StaticScannerFactory staticScannerFactory = new StaticScannerFactory();
        StaticScanner staticScanner = staticScannerFactory.getStaticScanner(scanType);
        staticScanner.init(userId, testName, ipAddress, productName, wumLevel, isFileUpload, uploadLocation,
                zipFileName, gitUrl, gitUsername, gitPassword);
        new Thread(staticScanner).start();
    }

    public void getReportAndMail(String containerId) {
        try {
            StaticScannerEntity staticScanner = findOneByContainerId(containerId);
            if (staticScanner != null) {
                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                        .setPort(staticScanner.getHostPort()).setScheme("http")
                        .setPath(ScannerProperties.getStaticScannerGetReport())
                        .build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);

                if (response != null) {
                    if (response.getEntity() != null) {
                        String subject = "Static Scan Report: " + staticScanner.getCreatedTime();
                        if (mailHandler.sendMail(staticScanner.getUserId(), subject, "This is auto generated message",
                                response.getEntity().getContent(), "Reports.zip")) {
                            staticScanner.setReportSent(true);
                            staticScanner.setReportSentTime(new SimpleDateFormat(ScannerProperties.getDatePattern())
                                    .format(new Date()));
                            kill(containerId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new AutomationManagerException("Error occurred while getting static scanner report and mail");
        }
    }

    @Retryable(value = IOException.class, maxAttempts = 20, backoff = @Backoff(delay = 5000))
    public boolean isStaticScannerReady(StaticScannerEntity staticScanner) throws IOException {
        LOGGER.info("Checking Micro Service Started....");
        boolean status = false;
        try {
            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(ScannerProperties
                            .getStaticScannerIsReady())
                    .build();

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet httpGet = new HttpGet(uri);
            return Boolean.parseBoolean(HttpRequestHandler.printResponse(httpClient.execute(httpGet)));

        } catch (URISyntaxException e) {
            throw new AutomationManagerException("Error occurred while getting static scanner status");
        }
    }

    public void kill(String containerId) {
        StaticScannerEntity staticScanner = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        DockerHandler.removeContainer(containerId);
        staticScanner.setStatus(STATUS_REMOVED);
        save(staticScanner);
    }

    public void updateFileExtracted(String containerId, boolean status) {
        StaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setFileExtracted(status);
        staticScanner.setFileExtractedTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    public void updateProductCloned(String containerId, boolean status) {
        StaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setProductCloned(status);
        staticScanner.setProductClonedTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    public void updateScanStatus(String containerId, String status) {
        StaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setScanStatus(status);
        staticScanner.setScanStatusTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        save(staticScanner);
    }

    public void updateReportReady(String containerId, boolean status) {
        StaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setReportReady(status);
        staticScanner.setReportReadyTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        save(staticScanner);
        getReportAndMail(containerId);
    }
}
