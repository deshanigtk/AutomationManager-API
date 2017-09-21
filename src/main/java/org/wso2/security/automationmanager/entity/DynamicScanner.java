package org.wso2.security.automationmanager.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DynamicScanner {

    @Id
    private String id;
    private String userId;
    private String name;
    private String status;

    public DynamicScanner(String id, String userId) {
        this.id = id;
        this.userId = userId;
        this.status = "created";
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
