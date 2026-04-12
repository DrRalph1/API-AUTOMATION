package com.usg.autoAPIGenerator.dtos.dashboard;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardCollectionDTO {
    // Basic IDs
    private String id;
    private String name;
    private String description;

    // Counts
    private int requestsCount;
    private int folderCount;
    private int totalFolders;
    private int totalRequests;

    // Status
    private boolean favorite;
    private String owner;
    private String lastUpdated;
    private String createdAt;
    private String updatedAt;
    private String createdBy;

    // Version info
    private String version;

    // Folder details with IDs
    private List<Map<String, Object>> folders; // Each folder has id, name, description, requestsCount

    // Additional metadata
    private Map<String, Object> metadata;
    private List<String> tags;

    // Statistics
    private int totalEndpoints;
    private int publishedEndpoints;
    private int mockServersCount;
}