/*
 * Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.automation.manager.entity.scanner.statics;

import javax.persistence.*;

/**
 * Static scanner entity
 *
 * @author Deshani Geethika
 */
@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "testName"}))
public abstract class StaticScannerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(unique = true)
    private String containerId;
    private String userId;
    private String type;
    private String testName;
    private String productName;
    private String wumLevel;
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
    private boolean reportReady;
    private String reportReadyTime;
    private boolean reportSent;
    private String reportSentTime;
    private String message;

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setReportReady(boolean reportReady) {
        this.reportReady = reportReady;
    }

    public void setReportReadyTime(String reportReadyTime) {
        this.reportReadyTime = reportReadyTime;
    }

    public void setFileExtractedTime(String fileExtractedTime) {
        this.fileExtractedTime = fileExtractedTime;
    }

    public void setProductClonedTime(String productClonedTime) {
        this.productClonedTime = productClonedTime;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReportSent(boolean reportSent) {
        this.reportSent = reportSent;
    }

    public void setReportSentTime(String reportSentTime) {
        this.reportSentTime = reportSentTime;
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

    public String getType() {
        return type;
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

    public String getMessage() {
        return message;
    }

    public boolean isReportReady() {
        return reportReady;
    }

    public String getReportReadyTime() {
        return reportReadyTime;
    }

    public boolean isReportSent() {
        return reportSent;
    }

    public String getReportSentTime() {
        return reportSentTime;
    }
}
