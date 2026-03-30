package com.usg.apiGeneration.dtos.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardEndpointDTO {
    // Basic IDs
    private String id; // Endpoint/Request ID
    private String apiId;
    private String apiCode; // API Code

    // Collection and Folder IDs
    private String collectionId;
    private String collectionName;
    private String folderId;
    private String folderName;

    // Basic Info
    private String name;
    private String method;
    private String url;
    private String description;
    private String status;
    private Long totalCalls;
    private LocalDateTime lastCalledAt;
    private String version;
    private String owner;

    // Timestamps
    private String lastUpdated;
    private String timeAgo;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
//
//    // Performance metrics
//    private int calls;
//    private String latency;
//    private String successRate;
//    private int errors;
//    private String avgResponseTime;
//
//    // Complex objects with their own IDs
//    private List<Map<String, Object>> tags;
//    private List<Map<String, Object>> parameters; // Each has ID
//    private List<Map<String, Object>> responseMappings; // Each has ID
//    private List<Map<String, Object>> headers; // Each has ID
//    private Map<String, Object> authConfig; // Has its own ID
//    private Map<String, Object> requestBody; // Has its own ID
//    private Map<String, Object> responseBody; // Has its own ID
//    private Map<String, Object> schemaConfig; // Has its own ID
//    private Map<String, Object> testConfig; // Has its own ID
//    private Map<String, Object> settings; // Has its own ID
//    private Map<String, Object> collectionInfo; // Collection metadata
//    private Map<String, Object> requestDetails; // Full request details
}