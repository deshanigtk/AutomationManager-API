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
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.handlers.DockerHandler;
import org.wso2.security.automation.manager.handlers.HttpRequestHandler;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.repository.StaticScannerRepository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@PropertySource("classpath:global.properties")
@Service
public class StaticScannerService {

    @Value("${STATIC_SCANNER_DOCKER_IMAGE}")
    private String dockerImage;

    @Value("${IS_STATIC_SCANNER_READY}")
    private String isReady;

    @Value("${STATIC_SCANNER_START_SCAN}")
    private String startScan;

    @Value("${STATIC_SCANNER_GET_REPORT}")
    private String getReport;

    @Value("${AUTOMATION_MANAGER_HOST}")
    private String myHost;

    @Value("${AUTOMATION_MANAGER_PORT}")
    private int myPort;

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

    public StaticScanner startStaticScanner(String userId, String name, String ipAddress) {
        StaticScanner staticScanner = new StaticScanner();
        staticScanner.setUserId(userId);
        staticScanner.setName(name);
        staticScanner.setStatus("initiated");
        save(staticScanner);

        int port = calculatePort(staticScanner.getId());

        String containerId = DockerHandler.createContainer(dockerImage, ipAddress, String.valueOf(port),
                String.valueOf(port), null, new String[]{"port=" + port});

        if (containerId != null) {
            LOGGER.info("Container Id: " + containerId);
            String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
            staticScanner.setContainerId(containerId);
            staticScanner.setIpAddress(ipAddress);
            staticScanner.setContainerPort(port);
            staticScanner.setHostPort(port);
            staticScanner.setStatus("created");
            staticScanner.setCreatedTime(createdTime);

            save(staticScanner);

            if (DockerHandler.startContainer(containerId)) {
                staticScanner.setStatus("running");
                staticScanner.setIpAddress(DockerHandler.inspectContainer(containerId).networkSettings().ipAddress());
                save(staticScanner);
                return staticScanner;
            }
        }
        return null;
    }

    public String startScan(StaticScanner staticScanner, boolean isFileUpload, MultipartFile zipFile, String url, String branch, String tag, boolean isFindSecBugs,
                            boolean isDependencyCheck) {
        try {
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

            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(startScan)
                    .addParameter("automationManagerHost", myHost)
                    .addParameter("automationManagerPort", String.valueOf(myPort))
                    .addParameter("myContainerId", staticScanner.getContainerId())
                    .addParameter("isFileUpload", String.valueOf(isFileUpload))
                    .addParameter("url", url)
                    .addParameter("branch", branch)
                    .addParameter("tag", tag)
                    .addParameter("isFindSecBugs", String.valueOf(isFindSecBugs))
                    .addParameter("isDependencyCheck", String.valueOf(isDependencyCheck))
                    .build();

            Map<String, MultipartFile> files = new HashMap<>();
            if (zipFile != null) {
                files.put("zipFile", zipFile);
            }
            HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, files, null);
            if (response != null) {
                return HttpRequestHandler.printResponse(response);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "Cannot start scanner";
    }

    public void getReportAndMail(String containerId) {
        try {
            StaticScanner staticScanner = findOneByContainerId(containerId);
            if (staticScanner != null) {
                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress())
                        .setPort(staticScanner.getHostPort()).setScheme("http").setPath(getReport)
                        .build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);

                if (response != null) {
                    if (response.getEntity() != null) {
                        String subject = "Static Scan Report: ";
                        if (mailHandler.sendMail(staticScanner.getUserId(), subject, "This is auto generated message",
                                response.getEntity().getContent(), "Reports.zip")) {
                            staticScanner.setFindSecBugsReportSent(true);
                            staticScanner.setFindSecBugsReportSentTime(new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
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
                    .setPort(staticScanner.getHostPort()).setScheme("http").setPath(isReady)
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

    private int calculatePort(int id) {
        if (40000 + id > 65535) {
            id = 1;
        }
        return (40000 + id) % 65535;
    }

    public void kill(String containerId) {
        StaticScanner staticScanner = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        staticScanner.setStatus("killed");
        save(staticScanner);
    }
}
