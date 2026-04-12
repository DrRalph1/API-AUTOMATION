package com.usg.autoAPIGenerator.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardEnvironmentDTO {
    private String id;
    private String name;
    private String baseUrl;
    private boolean active;
    private String description;
    private String lastUsed;
    private int variablesCount;
    private String createdAt;
    private String updatedAt;
    private String createdBy;

    private String variables;
}