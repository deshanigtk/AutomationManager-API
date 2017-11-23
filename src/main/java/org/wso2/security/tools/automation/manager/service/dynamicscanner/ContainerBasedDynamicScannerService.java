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
package org.wso2.security.tools.automation.manager.service.dynamicscanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased
        .ContainerBasedDynamicScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.MailHandler;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.ContainerBasedDynamicScannerRepository;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ContainerBasedDynamicScannerService {

    private final ContainerBasedDynamicScannerRepository dynamicScannerRepository;
    private final MailHandler mailHandler;

    @Autowired
    public ContainerBasedDynamicScannerService(ContainerBasedDynamicScannerRepository dynamicScannerRepository,
                                               MailHandler mailHandler) {
        this.dynamicScannerRepository = dynamicScannerRepository;
        this.mailHandler = mailHandler;
    }

    public Iterable<ContainerBasedDynamicScannerEntity> findAll() {
        return dynamicScannerRepository.findAll();
    }

    public ContainerBasedDynamicScannerEntity findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    public ContainerBasedDynamicScannerEntity findOneByContainerId(String containerId) {
        return dynamicScannerRepository.findOneByContainerId(containerId);
    }

    public Iterable<ContainerBasedDynamicScannerEntity> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    public ContainerBasedDynamicScannerEntity save(ContainerBasedDynamicScannerEntity dynamicScanner) {
        return dynamicScannerRepository.save(dynamicScanner);
    }

    public void kill(String containerId) {
        ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        DockerHandler.killContainer(containerId);
        DockerHandler.removeContainer(containerId);
        dynamicScanner.setStatus(ScannerProperties.getStatusRemoved());
        save(dynamicScanner);
    }

    public void updateScanStatus(String containerId, String status, int progress) {
        ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setScanStatus(status);
        dynamicScanner.setScanProgress(progress);
        dynamicScanner.setScanProgressTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        save(dynamicScanner);
    }

    public void updateReportReady(String containerId, boolean status, String reportFilePath) throws
            AutomationManagerException {
        ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setReportReady(status);
        dynamicScanner.setReportReadyTime(new SimpleDateFormat(ScannerProperties.getDatePattern()).format(new Date()));
        save(dynamicScanner);
        getReportAndMail(containerId, reportFilePath);
    }

    public void updateMessage(String containerId, String status) {
        ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setMessage(status);
        save(dynamicScanner);
    }

    private void getReportAndMail(String containerId, String reportFilePath) throws AutomationManagerException {
        try {
            ContainerBasedDynamicScannerEntity dynamicScannerEntity = findOneByContainerId(containerId);
            String subject = "Dynamic Scan Report: ";
            if (mailHandler.sendMail(dynamicScannerEntity.getUserId(), subject, "This is auto generated message",
                    new FileInputStream(new File(reportFilePath)), "ZapReport.html")) {
                dynamicScannerEntity.setReportSent(true);
                dynamicScannerEntity.setReportSentTime(new SimpleDateFormat(ScannerProperties.getDatePattern())
                        .format(new Date()));
                kill(containerId);
            }
        } catch (Exception e) {
            throw new AutomationManagerException("Error occurred while getting dynamic scanner " +
                    "report and mail");
        }
    }
}
