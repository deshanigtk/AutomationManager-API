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

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class StaticScanner {
    @Id
    private String id;
    private String userId;
    private String createdTime;
    private String status;
    private String ipAddress;
    private int containerPort;
    private int hostPort;
    private boolean fileUploaded;
    private boolean fileExtracted;
    private boolean productCloned;
    private boolean dependencyCheckReportReady;
    private boolean findSecBugsReportReady;

    private String fileUploadedTime;
    private String fileExtractedTime;
    private String productClonedTime;
    private String dependencyCheckReportReadyTime;
    private String findSecBugsReportReadyTime;


    private boolean isProductAvailable;

    public StaticScanner() {
    }

    public StaticScanner(String id, String userId, String createdTime, String ipAddress, int containerPort, int hostPort) {
        this.id = id;
        this.userId = userId;
        this.createdTime = createdTime;
        this.status = "created";
        this.ipAddress = ipAddress;
        this.containerPort = containerPort;
        this.hostPort = hostPort;
        this.isProductAvailable = false;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }

    public void setFileExtracted(boolean fileExtracted) {
        this.fileExtracted = fileExtracted;
    }

    public void setProductCloned(boolean productCloned) {
        this.productCloned = productCloned;
    }

    public void setFileUploadedTime(String fileUploadedTime) {
        this.fileUploadedTime = fileUploadedTime;
    }

    public void setFileExtractedTime(String fileExtractedTime) {
        this.fileExtractedTime = fileExtractedTime;
    }

    public void setProductClonedTime(String productClonedTime) {
        this.productClonedTime = productClonedTime;
    }

    public void setDependencyCheckReportReady(boolean dependencyCheckReportReady) {
        this.dependencyCheckReportReady = dependencyCheckReportReady;
    }

    public void setFindSecBugsReportReady(boolean findSecBugsReportReady) {
        this.findSecBugsReportReady = findSecBugsReportReady;
    }

    public void setDependencyCheckReportReadyTime(String dependencyCheckReportReadyTime) {
        this.dependencyCheckReportReadyTime = dependencyCheckReportReadyTime;
    }

    public void setFindSecBugsReportReadyTime(String findSecBugsReportReadyTime) {
        this.findSecBugsReportReadyTime = findSecBugsReportReadyTime;
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

    public boolean isProductCloned() {
        return productCloned;
    }

    public boolean isFileExtracted() {
        return fileExtracted;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public String getFileUploadedTime() {
        return fileUploadedTime;
    }

    public String getFileExtractedTime() {
        return fileExtractedTime;
    }

    public String getProductClonedTime() {
        return productClonedTime;
    }

    public boolean isDependencyCheckReportReady() {
        return dependencyCheckReportReady;
    }

    public boolean isFindSecBugsReportReady() {
        return findSecBugsReportReady;
    }

    public String getDependencyCheckReportReadyTime() {
        return dependencyCheckReportReadyTime;
    }

    public String getFindSecBugsReportReadyTime() {
        return findSecBugsReportReadyTime;
    }

    public void setProductAvailable(boolean productAvailable) {
        this.isProductAvailable = productAvailable;
    }

    public boolean isProductAvailable() {
        return isProductAvailable;
    }
}
