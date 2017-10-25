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

import org.apache.commons.io.FileUtils;
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
import org.wso2.security.automation.manager.Constants;
import org.wso2.security.automation.manager.entity.Zap;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.FileHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.repository.DynamicScannerRepository;
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.scanners.DynamicScannerThread;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class DynamicScannerService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final DynamicScannerRepository dynamicScannerRepository;

    private final ZapService zapService;

    private final MailHandler mailHandler;

    @Autowired
    public DynamicScannerService(DynamicScannerRepository dynamicScannerRepository, MailHandler mailHandler, ZapService zapService) {
        this.dynamicScannerRepository = dynamicScannerRepository;
        this.mailHandler = mailHandler;
        this.zapService = zapService;
    }

    public Iterable<DynamicScanner> findAll() {
        return dynamicScannerRepository.findAll();
    }

    public DynamicScanner findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    public DynamicScanner findOneByContainerId(String containerId) {

        return dynamicScannerRepository.findOneByContainerId(containerId);
    }

    public Iterable<DynamicScanner> findByUserIdAndStatus(String userId, String status) {
        return dynamicScannerRepository.findByUserIdAndStatus(userId, status);
    }

    public Iterable<DynamicScanner> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    public DynamicScanner save(DynamicScanner dynamicScanner) {
        return dynamicScannerRepository.save(dynamicScanner);
    }


    public String startScan(String userId, String name, String ipAddress, boolean isFileUpload, MultipartFile zipFile,
                            MultipartFile urlListFile, String wso2ServerHost, int wso2ServerPort,
                            boolean isAuthenticatedScan) {

        if (isFileUpload) {
            if (zipFile == null) {
                return "Please upload a zip file";
            }
        } else {
            if (wso2ServerHost == null || wso2ServerPort == -1) {
                return "Please enter valid details of running WSO2 server";
            }
        }

        String uploadLocation = Constants.TEMP_FOLDER_PATH + File.separator + userId + new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
        String urlListFileName;
        String zipFileName = null;

        if (new File(Constants.TEMP_FOLDER_PATH).exists() || new File(Constants.TEMP_FOLDER_PATH).mkdir()) {

            if (new File(uploadLocation).mkdir()) {
                urlListFileName = urlListFile.getOriginalFilename();
                if (FileHandler.uploadFile(urlListFile, uploadLocation + File.separator + urlListFileName)) {

                    if (zipFile != null) {
                        zipFileName = zipFile.getOriginalFilename();
                        if (!FileHandler.uploadFile(zipFile, uploadLocation + File.separator + zipFileName)) {
                            return "Cannot upload zip file";
                        }
                    }
                    DynamicScannerThread dynamicScannerThread = new DynamicScannerThread(userId, name, ipAddress, isFileUpload, uploadLocation,
                            urlListFileName, zipFileName, wso2ServerHost, wso2ServerPort, isAuthenticatedScan);
                    new Thread(dynamicScannerThread).start();
                    return "Ok";
                }
            }
        }
        return "Cannot upload files to temp location";
    }

    @Retryable(value = IOException.class, maxAttempts = 10, backoff = @Backoff(delay = 3000))
    public boolean isDynamicScannerReady(DynamicScanner dynamicScanner) throws IOException {
        boolean status = false;
        try {

            URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
                    .setPort(dynamicScanner.getHostPort()).setScheme("http").setPath(Constants.IS_DYNAMIC_SCANNER_READY)
                    .build();

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet httpGet = new HttpGet(uri);
            HttpResponse response = httpClient.execute(httpGet);

            if (response != null) {
                status = Boolean.parseBoolean(HttpRequestHandler.printResponse(response));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        LOGGER.info("Dynamic Scanner is ready: " + status);
        return status;
    }

    public void getReportAndMail(String containerId) {
        try {
            DynamicScanner dynamicScanner = findOneByContainerId(containerId);
            if (dynamicScanner != null) {
                URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
                        .setPort(dynamicScanner.getHostPort()).setScheme("http").setPath(Constants.DYNAMIC_SCANNER_GET_REPORT)
                        .build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);

                if (response != null) {
                    if (response.getEntity() != null) {
                        String subject = "Dynamic Scan Report: ";
                        if (mailHandler.sendMail(dynamicScanner.getUserId(), subject, "This is auto generated message",
                                response.getEntity().getContent(), "ZapReport.html")) {
                            dynamicScanner.setReportSent(true);
                            dynamicScanner.setReportSentTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
                            kill(containerId);

                            String zapContainerId = dynamicScanner.getRelatedZapId();
                            zapService.kill(zapContainerId);

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void kill(String containerId) {
        DynamicScanner dynamicScanner = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        dynamicScanner.setStatus("killed");
        save(dynamicScanner);
    }
}
