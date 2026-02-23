package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardMockServerDTO {
    private String id;
    private String collectionId;
    private String collectionName;
    private String mockServerUrl;
    private boolean active;
    private String description;
    private String createdAt;
    private String expiresAt;
}