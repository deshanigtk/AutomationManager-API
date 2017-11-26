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
package org.wso2.security.tools.automation.manager.service.staticscanner;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.config.StaticScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.ContainerBasedStaticScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.MailHandler;
import org.wso2.security.tools.automation.manager.repository.staticscanner.ContainerBasedStaticScannerRepository;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ContainerBasedStaticScannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerBasedStaticScannerService.class);

    private final ContainerBasedStaticScannerRepository staticScannerRepository;
    private final MailHandler mailHandler;

    @Autowired
    public ContainerBasedStaticScannerService(ContainerBasedStaticScannerRepository staticScannerRepository,
                                              MailHandler mailHandler) {
        this.staticScannerRepository = staticScannerRepository;
        this.mailHandler = mailHandler;
    }

    public Iterable<ContainerBasedStaticScannerEntity> findAll() {
        return staticScannerRepository.findAll();
    }

    public ContainerBasedStaticScannerEntity findOne(int id) {
        return staticScannerRepository.findOne(id);
    }

    public ContainerBasedStaticScannerEntity findOneByContainerId(String containerId) {
        return staticScannerRepository.findOneByContainerId(containerId);
    }

    public Iterable<ContainerBasedStaticScannerEntity> findByUserId(String userId) {
        return staticScannerRepository.findByUserId(userId);
    }

    public ContainerBasedStaticScannerEntity save(ContainerBasedStaticScannerEntity staticScanner) {
        return staticScannerRepository.save(staticScanner);
    }

    public void kill(String containerId) throws AutomationManagerException {
        try {
            ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
            DockerHandler.killContainer(containerId);
            DockerHandler.removeContainer(containerId);
            staticScanner.setStatus(AutomationManagerProperties.getStatusRemoved());
            save(staticScanner);
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new AutomationManagerException("Error occurred while removing product manager container", e);
        }
    }

    public void updateFileUploaded(String containerId, boolean status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setFileUploaded(status);
        staticScanner.setFileUploadedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    public void updateFileExtracted(String containerId, boolean status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setFileExtracted(status);
        staticScanner.setFileExtractedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    public void updateProductCloned(String containerId, boolean status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setProductCloned(status);
        staticScanner.setProductClonedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    public void updateScanStatus(String containerId, String status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setScanStatus(status);
        staticScanner.setScanStatusTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date()));
        save(staticScanner);
    }

    public void updateReportReady(String containerId, boolean status){
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setReportReady(status);
        staticScanner.setReportReadyTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date()));
        save(staticScanner);
        try {
            getReportAndMail(containerId);
        } catch (AutomationManagerException e) {
            e.printStackTrace();
        }
    }


    public void getReportAndMail(String containerId) throws AutomationManagerException {
        try {
            ContainerBasedStaticScannerEntity staticScannerEntity = findOneByContainerId(containerId);
            if (staticScannerEntity != null) {
                URI uri = (new URIBuilder()).setHost(staticScannerEntity.getIpAddress())
                        .setPort(staticScannerEntity.getHostPort()).setScheme("http")
                        .setPath(staticScannerEntity.getContextPath() + StaticScannerProperties.getStaticScannerGetReport())
                        .build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);

                if (response != null && response.getEntity() != null) {
                    String subject = "Static Scan Report: " + staticScannerEntity.getCreatedTime();
                    mailHandler.sendMail(staticScannerEntity.getUserId(), subject, "This is auto generated message",
                            response.getEntity().getContent(), "Reports.zip");
                    staticScannerEntity.setReportSent(true);
                    staticScannerEntity.setReportSentTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern())
                            .format(new Date()));
                    kill(containerId);
                }
            }
        } catch (IOException | URISyntaxException | MessagingException e) {
            throw new AutomationManagerException("Error occurred while getting static scanner report and mail", e);
        }
    }
}
