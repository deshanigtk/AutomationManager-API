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

package org.wso2.security.automation.manager.entity.scanner.dynamic;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Dynamic scanner entity
 *
 * @author Deshani Geethika
 */
@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "testName"}))
public abstract class DynamicScannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    @Column(unique = true)
    private String containerId;

    @NotNull
    private String userId;
    private String createdTime;
    private String status;
    private String ipAddress;
    private String dockerIpAddress;
    private int containerPort;
    private int hostPort;
    private String scanStatus;
    private int scanProgress = -1;
    private String scanProgressTime;
    private boolean reportReady;
    private String reportReadyTime;
    private boolean reportSent;
    private String reportSentTime;
    private String message;

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
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

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public void setScanProgress(int scanProgress) {
        this.scanProgress = scanProgress;
    }

    public void setScanProgressTime(String scanProgressTime) {
        this.scanProgressTime = scanProgressTime;
    }

    public void setReportReady(boolean reportReady) {
        this.reportReady = reportReady;
    }

    public void setReportReadyTime(String reportReadyTime) {
        this.reportReadyTime = reportReadyTime;
    }

    public void setReportSent(boolean reportSent) {
        this.reportSent = reportSent;
    }

    public void setReportSentTime(String reportSentTime) {
        this.reportSentTime = reportSentTime;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getScanStatus() {
        return scanStatus;
    }

    public int getScanProgress() {
        return scanProgress;
    }

    public String getScanProgressTime() {
        return scanProgressTime;
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

    public String getMessage() {
        return message;
    }
}
