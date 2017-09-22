package org.wso2.security.automationmanager.entity;
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
import java.text.SimpleDateFormat;

@Entity
public class StaticScanner {
    @Id
    private String id;
    private String userId;
    private SimpleDateFormat createdTime;
    private String name;
    private String status;
    private String ipAddress;
    private String containerPort;
    private String hostPort;

    private boolean isProductAvailable;

    public StaticScanner(String id, String userId, SimpleDateFormat createdTime, String ipAddress, String containerPort, String hostPort) {
        this.id = id;
        this.userId = userId;
        this.createdTime = createdTime;
        this.status = "created";
        this.ipAddress = ipAddress;
        this.containerPort = containerPort;
        this.hostPort = hostPort;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public SimpleDateFormat getCreatedTime() {
        return createdTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getContainerPort() {
        return containerPort;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setProductAvailable(boolean productAvailable) {
        this.isProductAvailable = productAvailable;
    }

    public boolean isProductAvailable() {
        return isProductAvailable;
    }
}
