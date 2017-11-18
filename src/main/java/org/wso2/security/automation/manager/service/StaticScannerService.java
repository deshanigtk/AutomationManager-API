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
import org.wso2.security.automation.manager.entity.scanner.statics.StaticScannerEntity;
import org.wso2.security.automation.manager.exception.AutomationManagerRuntimeException;
import org.wso2.security.automation.manager.handler.DockerHandler;
import org.wso2.security.automation.manager.handler.FileHandler;
import org.wso2.security.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.automation.manager.handler.MailHandler;
import org.wso2.security.automation.manager.config.ScannerProperty;
import org.wso2.security.automation.manager.repository.StaticScannerRepository;
import org.wso2.security.automation.manager.scanner.statics.StaticScanner;

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
    private static final String DATE_PATTERN = "yyyy-MM-dd:HH.mm.ss";

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

    public void startStaticScan(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean isFileUpload, MultipartFile zipFile, String url, String branch,
                                String tag, boolean isFindSecBugs, boolean isDependencyCheck) {
        String zipFileName = null;
        String uploadLocation = ScannerProperty.getTempFolderPath() + File.separator + userId + new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());

        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new AutomationManagerRuntimeException("Please upload product zip file");
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
            if (url == null || branch == null) {
                throw new AutomationManagerRuntimeException("Please enter URL and branch to clone");
            }
        }
        if (!isFindSecBugs && !isDependencyCheck) {
            throw new AutomationManagerRuntimeException("Please enter at least one scan");
        }

        StaticScanner staticScannerThread = new StaticScanner(userId, testName, ipAddress, productName, wumLevel,
                isFileUpload, uploadLocation, zipFileName, url, branch, tag, isFindSecBugs, isDependencyCheck);
        new Thread(staticScannerThread).start();
    }

    public void getReportAndMail(String containerId) {
        try {
            StaticScannerEntity staticScanner = findOneByContainerId(containerId);
            if (staticScanner != null) {
                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                        .setPort(staticScanner.getHostPort()).setScheme("http")
                        .setPath(ScannerProperty.getStaticScannerGetReport())
                        .build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);

                if (response != null) {
                    if (response.getEntity() != null) {
                        String subject = "Static Scan Report: " + staticScanner.getCreatedTime();
                        if (mailHandler.sendMail(staticScanner.getUserId(), subject, "This is auto generated message",
                                response.getEntity().getContent(), "Reports.zip")) {
                            staticScanner.setReportSent(true);
                            staticScanner.setReportSentTime(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
                            kill(containerId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new AutomationManagerRuntimeException("Error occurred while getting static scanner report and mail");
        }
    }

    @Retryable(value = IOException.class, maxAttempts = 20, backoff = @Backoff(delay = 5000))
    public boolean isStaticScannerReady(StaticScannerEntity staticScanner) throws IOException {
        LOGGER.info("Checking Micro Service Started....");
        boolean status = false;
        try {
            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(ScannerProperty.getStaticScannerIsReady())
                    .build();

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet httpGet = new HttpGet(uri);
            return Boolean.parseBoolean(HttpRequestHandler.printResponse(httpClient.execute(httpGet)));

        } catch (URISyntaxException e) {
            throw new AutomationManagerRuntimeException("Error occurred while getting static scanner status");
        }
    }

    public void kill(String containerId) {
        StaticScannerEntity staticScanner = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        DockerHandler.removeContainer(containerId);
        staticScanner.setStatus(STATUS_REMOVED);
        save(staticScanner);
    }
}
