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

    private boolean dependencyCheckReportReady;
    private String dependencyCheckReportReadyTime;

    private boolean findSecBugsReportReady;
    private String findSecBugsReportReadyTime;

    public StaticScanner() {
    }

    public StaticScanner(String id, String userId, String createdTime, String ipAddress, int containerPort, int hostPort) {
        this.id = id;
        this.userId = userId;
//        this.name = name;
        this.createdTime = createdTime;
        this.status = "created";
        this.ipAddress = ipAddress;
        this.containerPort = containerPort;
        this.hostPort = hostPort;
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

    public void setDependencyCheckReportReadyTime(String dependencyCheckReportReadyTime) {
        this.dependencyCheckReportReadyTime = dependencyCheckReportReadyTime;
    }

    public void setFindSecBugsReportReadyTime(String findSecBugsReportReadyTime) {
        this.findSecBugsReportReadyTime = findSecBugsReportReadyTime;
    }

    public String getId() {
        return id;
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


}
