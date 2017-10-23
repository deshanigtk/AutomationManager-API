package org.wso2.security.automation.manager.service;
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
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.repository.StaticScannerRepository;
import org.wso2.security.automation.manager.scanners.StaticScannerThread;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class StaticScannerService {

    private final StaticScannerRepository staticScannerRepository;

    private final MailHandler mailHandler;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public StaticScannerService(StaticScannerRepository staticScannerRepository, MailHandler mailHandler) {
        this.staticScannerRepository = staticScannerRepository;
        this.mailHandler = mailHandler;
    }

    public Iterable<StaticScanner> findAll() {
        return staticScannerRepository.findAll();
    }

    public StaticScanner findOne(int id) {

        return staticScannerRepository.findOne(id);
    }

    public StaticScanner findOneByContainerId(String containerId) {
        return staticScannerRepository.findOneByContainerId(containerId);
    }

    public Iterable<StaticScanner> findByUserId(String userId) {
        return staticScannerRepository.findByUserId(userId);
    }

    public StaticScanner save(StaticScanner staticScanner) {
        return staticScannerRepository.save(staticScanner);
    }

    public String startStaticScan(String userId, String name, String ipAddress, boolean isFileUpload, MultipartFile zipFile, String url, String branch,
                                  String tag, boolean isFindSecBugs, boolean isDependencyCheck) {

        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                return "Please upload product zip file";
            }
        } else {
            if (url == null || branch == null) {
                return "Please enter URL and branch to clone";
            }
        }
        if (!isFindSecBugs && !isDependencyCheck) {
            return "Please enter at least one scan";
        }
        StaticScannerThread staticScannerThread = new StaticScannerThread(userId, name, ipAddress, isFileUpload,
                zipFile, url, branch, tag, isFindSecBugs, isDependencyCheck);
        new Thread(staticScannerThread).start();
        return "Ok";
    }

    public void getReportAndMail(String containerId) {
        try {
            StaticScanner staticScanner = findOneByContainerId(containerId);
            if (staticScanner != null) {
                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                        .setPort(staticScanner.getHostPort()).setScheme("http").setPath(Constants.STATIC_SCANNER_GET_REPORT)
                        .build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);

                if (response != null) {
                    if (response.getEntity() != null) {
                        String subject = "Static Scan Report: " + staticScanner.getCreatedTime();
                        if (mailHandler.sendMail(staticScanner.getUserId(), subject, "This is auto generated message",
                                response.getEntity().getContent(), "Reports.zip")) {
                            staticScanner.setReportSent(true);
                            staticScanner.setReportSentTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
                            kill(containerId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Retryable(value = IOException.class, maxAttempts = 10, backoff = @Backoff(delay = 3000))
    public boolean isStaticScannerReady(StaticScanner staticScanner) throws IOException {
        boolean status = false;
        try {

            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(Constants.IS_STATIC_SCANNER_READY)
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
        LOGGER.info("Static Scanner is ready: " + status);
        return status;
    }


    public void kill(String containerId) {
        StaticScanner staticScanner = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        staticScanner.setStatus("killed");
        save(staticScanner);
    }

}
