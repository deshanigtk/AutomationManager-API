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

package org.wso2.security.tools.automation.manager.entity.staticscanner;

import javax.persistence.*;

/**
 * The  abstract class {@code StaticScannerEntity} is the database entity to store details about static scans and
 * scanners. {@code InheritanceType} is defined as joined table strategy. To add a static scanner, simply have to
 * extend the {@code StaticScannerEntity}
 *
 * @author Deshani Geethika
 */
@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
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
    private String scanStatus;
    private String scanStatusTime;
    private boolean reportReady;
    private String reportReadyTime;
    private boolean reportSent;
    private String reportSentTime;
    private String message;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getWumLevel() {
        return wumLevel;
    }

    public void setWumLevel(String wumLevel) {
        this.wumLevel = wumLevel;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public boolean isProductAvailable() {
        return isProductAvailable;
    }

    public void setProductAvailable(boolean productAvailable) {
        this.isProductAvailable = productAvailable;
    }

    public boolean isFileExtracted() {
        return fileExtracted;
    }

    public void setFileExtracted(boolean fileExtracted) {
        this.fileExtracted = fileExtracted;
    }

    public String getFileExtractedTime() {
        return fileExtractedTime;
    }

    public void setFileExtractedTime(String fileExtractedTime) {
        this.fileExtractedTime = fileExtractedTime;
    }

    public boolean isProductCloned() {
        return productCloned;
    }

    public void setProductCloned(boolean productCloned) {
        this.productCloned = productCloned;
    }

    public String getProductClonedTime() {
        return productClonedTime;
    }

    public void setProductClonedTime(String productClonedTime) {
        this.productClonedTime = productClonedTime;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public String getScanStatusTime() {
        return scanStatusTime;
    }

    public void setScanStatusTime(String scanStatusTime) {
        this.scanStatusTime = scanStatusTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isReportReady() {
        return reportReady;
    }

    public void setReportReady(boolean reportReady) {
        this.reportReady = reportReady;
    }

    public String getReportReadyTime() {
        return reportReadyTime;
    }

    public void setReportReadyTime(String reportReadyTime) {
        this.reportReadyTime = reportReadyTime;
    }

    public boolean isReportSent() {
        return reportSent;
    }

    public void setReportSent(boolean reportSent) {
        this.reportSent = reportSent;
    }

    public String getReportSentTime() {
        return reportSentTime;
    }

    public void setReportSentTime(String reportSentTime) {
        this.reportSentTime = reportSentTime;
    }
}