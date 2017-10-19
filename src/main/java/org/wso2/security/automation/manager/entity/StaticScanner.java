package org.wso2.security.automation.manager.entity;
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

import org.hibernate.validator.constraints.Email;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "name"}))
public class StaticScanner {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(unique = true)
    private String containerId;

    private String userId;
    private String name;
    private String createdTime;
    private String status;
    private String ipAddress;
    private int containerPort;
    private int hostPort;
    private boolean isProductAvailable;

    private boolean fileExtracted;
    private String fileExtractedTime;

    private boolean productCloned;
    private String productClonedTime;

    private String findSecBugsStatus;
    private String dependencyCheckStatus;

    private boolean dependencyCheckReportReady;
    private String dependencyCheckReportReadyTime;

    private boolean findSecBugsReportReady;
    private String findSecBugsReportReadyTime;

    private boolean findSecBugsReportSent;
    private String findSecBugsReportSentTime;

    private boolean dependencyCheckReportSent;
    private String dependencyCheckReportSentTime;

    private String message;

    public StaticScanner() {
    }

    public StaticScanner(String containerId, String userId, String name, String createdTime, String ipAddress, int containerPort, int hostPort) {
        this.containerId = containerId;
        this.userId = userId;
        this.name = name;
        this.createdTime = createdTime;
        this.status = "created";
        this.ipAddress = ipAddress;
        this.containerPort = containerPort;
        this.hostPort = hostPort;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProductAvailable(boolean productAvailable) {
        this.isProductAvailable = productAvailable;
    }

    public void setFileExtracted(boolean fileExtracted) {
        this.fileExtracted = fileExtracted;
    }

    public void setProductCloned(boolean productCloned) {
        this.productCloned = productCloned;
    }

    public void setDependencyCheckReportReady(boolean dependencyCheckReportReady) {
        this.dependencyCheckReportReady = dependencyCheckReportReady;
    }

    public void setFindSecBugsReportReady(boolean findSecBugsReportReady) {
        this.findSecBugsReportReady = findSecBugsReportReady;
    }

    public void setFileExtractedTime(String fileExtractedTime) {
        this.fileExtractedTime = fileExtractedTime;
    }

    public void setProductClonedTime(String productClonedTime) {
        this.productClonedTime = productClonedTime;
    }

    public void setFindSecBugsStatus(String findSecBugsStatus) {
        this.findSecBugsStatus = findSecBugsStatus;
    }

    public void setDependencyCheckStatus(String dependencyCheckStatus) {
        this.dependencyCheckStatus = dependencyCheckStatus;
    }

    public void setDependencyCheckReportReadyTime(String dependencyCheckReportReadyTime) {
        this.dependencyCheckReportReadyTime = dependencyCheckReportReadyTime;
    }

    public void setFindSecBugsReportReadyTime(String findSecBugsReportReadyTime) {
        this.findSecBugsReportReadyTime = findSecBugsReportReadyTime;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFindSecBugsReportSent(boolean findSecBugsReportSent) {
        this.findSecBugsReportSent = findSecBugsReportSent;
    }

    public void setFindSecBugsReportSentTime(String findSecBugsReportSentTime) {
        this.findSecBugsReportSentTime = findSecBugsReportSentTime;
    }

    public void setDependencyCheckReportSent(boolean dependencyCheckReportSent) {
        this.dependencyCheckReportSent = dependencyCheckReportSent;
    }

    public void setDependencyCheckReportSentTime(String dependencyCheckReportSentTime) {
        this.dependencyCheckReportSentTime = dependencyCheckReportSentTime;
    }

    public int getId() {
        return id;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
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

    public int getContainerPort() {
        return containerPort;
    }

    public int getHostPort() {
        return hostPort;
    }

    public boolean isProductAvailable() {
        return isProductAvailable;
    }

    public boolean isFileExtracted() {
        return fileExtracted;
    }

    public String getFileExtractedTime() {
        return fileExtractedTime;
    }

    public boolean isProductCloned() {
        return productCloned;
    }

    public String getProductClonedTime() {
        return productClonedTime;
    }

    public String getFindSecBugsStatus() {
        return findSecBugsStatus;
    }

    public String getDependencyCheckStatus() {
        return dependencyCheckStatus;
    }

    public boolean isDependencyCheckReportReady() {
        return dependencyCheckReportReady;
    }

    public String getDependencyCheckReportReadyTime() {
        return dependencyCheckReportReadyTime;
    }

    public boolean isFindSecBugsReportReady() {
        return findSecBugsReportReady;
    }

    public String getFindSecBugsReportReadyTime() {
        return findSecBugsReportReadyTime;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFindSecBugsReportSent() {
        return findSecBugsReportSent;
    }

    public boolean isDependencyCheckReportSent() {
        return dependencyCheckReportSent;
    }

    public String getFindSecBugsReportSentTime() {
        return findSecBugsReportSentTime;
    }

    public String getDependencyCheckReportSentTime() {
        return dependencyCheckReportSentTime;
    }
}
