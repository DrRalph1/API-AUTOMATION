package com.usg.apiGeneration.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardDocumentationDTO {
    private String collectionId;
    private String collectionName;
    private String description;
    private String version;
    private int endpointsCount;
    private String lastUpdated;
    private boolean published;
    private String publishedUrl;
    private String publishedAt;

    // Add these missing fields
    private String publishedId;
    private String publishedBy;
}