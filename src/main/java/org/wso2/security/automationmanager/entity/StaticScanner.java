package org.wso2.security.automationmanager.entity;

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
