package com.usg.apiAutomation.dtos.dashboard;

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
}