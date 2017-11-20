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

package org.wso2.security.tools.automation.manager.entity.scanner.dynamic;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Dynamic scanner entity
 *
 * @author Deshani Geethika
 */
@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DynamicScannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected int id;
    protected boolean isContainer;
    @Column(unique = true)
    protected String containerId;
    @NotNull
    protected String userId;
    protected String type;
    protected String createdTime;
    protected String status;
    protected String ipAddress;
    protected String dockerIpAddress;
    protected int containerPort;
    protected int hostPort;
    protected String scanStatus;
    protected int scanProgress = -1;
    protected String scanProgressTime;
    protected boolean reportReady;
    protected String reportReadyTime;
    protected boolean reportSent;
    protected String reportSentTime;
    protected String message;

    public void setContainer(boolean container) {
        isContainer = container;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
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

    public boolean isContainer() {
        return isContainer;
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
