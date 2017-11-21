package org.wso2.security.tools.automation.manager.entity.scanner.dynamicscanner;/*
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

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@SuppressWarnings("unused")
@Entity
//@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "testName"}))
public class ProductManagerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private boolean isContainer;
    @Column(unique = true)
    private String containerId;
    @NotNull
    private String userId;
    private String relatedDynamicScannerId;
    private String testName;
    private String productName;
    private String wumLevel;
    private String createdTime;
    private String status;
    private String ipAddress;
    private String dockerIpAddress;
    private int containerPort;
    private int hostPort;

    private boolean fileUploaded;
    private String fileUploadedTime;
    private boolean fileExtracted;
    private String fileExtractedTime;
    private boolean serverStarted;
    private String serverStartedTime;
    private String message;

    public void setContainer(boolean container) {
        isContainer = container;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRelatedDynamicScannerId(String relatedDynamicScannerId) {
        this.relatedDynamicScannerId = relatedDynamicScannerId;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setWumLevel(String wumLevel) {
        this.wumLevel = wumLevel;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setDockerIpAddress(String dockerIpAddress) {
        this.dockerIpAddress = dockerIpAddress;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }

    public void setFileUploadedTime(String fileUploadedTime) {
        this.fileUploadedTime = fileUploadedTime;
    }

    public void setFileExtracted(boolean fileExtracted) {
        this.fileExtracted = fileExtracted;
    }

    public void setFileExtractedTime(String fileExtractedTime) {
        this.fileExtractedTime = fileExtractedTime;
    }

    public void setServerStarted(boolean serverStarted) {
        this.serverStarted = serverStarted;
    }

    public void setServerStartedTime(String serverStartedTime) {
        this.serverStartedTime = serverStartedTime;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRelatedDynamicScannerId() {
        return relatedDynamicScannerId;
    }

    public String getTestName() {
        return testName;
    }

    public String getProductName() {
        return productName;
    }

    public String getWumLevel() {
        return wumLevel;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getStatus() {
        return status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDockerIpAddress() {
        return dockerIpAddress;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public int getHostPort() {
        return hostPort;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public String getFileUploadedTime() {
        return fileUploadedTime;
    }

    public boolean isFileExtracted() {
        return fileExtracted;
    }

    public String getFileExtractedTime() {
        return fileExtractedTime;
    }

    public boolean isServerStarted() {
        return serverStarted;
    }

    public String getServerStartedTime() {
        return serverStartedTime;
    }

    public String getMessage() {
        return message;
    }
}
