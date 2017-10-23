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
import org.wso2.security.automation.manager.entity.Zap;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.repository.DynamicScannerRepository;
import org.wso2.security.automation.manager.entity.DynamicScanner;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@PropertySource("classpath:global.properties")
@Service
public class DynamicScannerService {

    @Value("${DYNAMIC_SCANNER_DOCKER_IMAGE}")
    private String dockerImage;

    @Value("${IS_DYNAMIC_SCANNER_READY}")
    private String isReady;

    @Value("${DYNAMIC_SCANNER_START_SCAN}")
    private String startScan;

    @Value("${DYNAMIC_SCANNER_GET_REPORT}")
    private String getReport;

    @Value("${AUTOMATION_MANAGER_HOST}")
    private String myHost;

    @Value("${AUTOMATION_MANAGER_PORT}")
    private int myPort;

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

    public DynamicScanner startDynamicScanner(String userId, String name, String ipAddress, String relatedZapContainerId) {
        DynamicScanner dynamicScanner = new DynamicScanner();
        dynamicScanner.setUserId(userId);
        dynamicScanner.setName(name);
        dynamicScanner.setRelatedZapId(relatedZapContainerId);
        dynamicScanner.setStatus("initiated");
        save(dynamicScanner);

        int port = calculatePort(dynamicScanner.getId());

        String containerId = DockerHandler.createContainer(dockerImage, ipAddress, String.valueOf(port),
                String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            LOGGER.info("Container Id: " + containerId);
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            dynamicScanner.setContainerId(containerId);
            dynamicScanner.setIpAddress(ipAddress);
            dynamicScanner.setContainerPort(port);
            dynamicScanner.setHostPort(port);
            dynamicScanner.setStatus("created");
            dynamicScanner.setCreatedTime(createdTime);

            save(dynamicScanner);

            if (DockerHandler.startContainer(containerId)) {
                dynamicScanner.setStatus("running");
                dynamicScanner.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                save(dynamicScanner);
                return dynamicScanner;
            }
        }
        return null;
    }

    public String startScan(DynamicScanner dynamicScanner, boolean isFileUpload, MultipartFile zipFile,
                            MultipartFile urlListFile, String relatedZapContainerId, String wso2ServerHost, int wso2ServerPort,
                            boolean isAuthenticatedScan) {

        try {
            String productHostRelativeToZap;
            String productHostRelativeToDynamicScanner;
            int productPort;

            if (isFileUpload) {
                productHostRelativeToZap = dynamicScanner.getIpAddress();
                productHostRelativeToDynamicScanner = "localhost";
                productPort = 9443;
            } else {
                productHostRelativeToZap = wso2ServerHost;
                productHostRelativeToDynamicScanner = wso2ServerHost;
                productPort = wso2ServerPort;
            }

            Zap zap = zapService.findOneByContainerId(relatedZapContainerId);

            if (zap != null) {
                if ((isFileUpload && zipFile != null) || (!isFileUpload && wso2ServerHost != null && wso2ServerPort != -1)) {
                    URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
                            .setPort(dynamicScanner.getHostPort()).setScheme("http").setPath(startScan)
                            .addParameter("automationManagerHost", myHost)
                            .addParameter("automationManagerPort", String.valueOf(myPort))
                            .addParameter("myContainerId", dynamicScanner.getContainerId())
                            .addParameter("isFileUpload", String.valueOf(isFileUpload))
                            .addParameter("zapHost", zap.getIpAddress())
                            .addParameter("zapPort", String.valueOf(zap.getHostPort()))
                            .addParameter("productHostRelativeToZap", productHostRelativeToZap)
                            .addParameter("productHostRelativeToThis", productHostRelativeToDynamicScanner)
                            .addParameter("productPort", String.valueOf(productPort))
                            .addParameter("isAuthenticatedScan", String.valueOf(isAuthenticatedScan))
                            .build();

                    Map<String, MultipartFile> files = new HashMap<>();
                    if (zipFile != null) {
                        files.put("zipFile", zipFile);
                    }
                    files.put("urlListFile", urlListFile);

                    HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, files, null);

                    String message = HttpRequestHandler.printResponse(response);
                    LOGGER.info("Start zap scan response: " + message);
                    return message;
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "Cannot execute ZAP scan command";
    }

    @Retryable(value = IOException.class, maxAttempts = 10, backoff = @Backoff(delay = 3000))
    public boolean isDynamicScannerReady(DynamicScanner dynamicScanner) throws IOException {
        boolean status = false;
        try {

            URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
                    .setPort(dynamicScanner.getHostPort()).setScheme("http").setPath(isReady)
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

    private int calculatePort(int id) {
        if (1000 + id > 20000) {
            id = 1;
        }
        return (1000 + id) % 20000;
    }

    public void getReportAndMail(String containerId) {
        try {
            DynamicScanner dynamicScanner = findOneByContainerId(containerId);
            if (dynamicScanner != null) {
                URI uri = (new URIBuilder()).setHost(dynamicScanner.getIpAddress())
                        .setPort(dynamicScanner.getHostPort()).setScheme("http").setPath(getReport)
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
