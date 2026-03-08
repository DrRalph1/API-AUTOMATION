package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardEndpointDTO {
    private String id;
    private String name;
    private String description;
    private String method;
    private String url;
    private String status;
    private String version;
    private int calls;
    private String latency;
    private String successRate;
    private int errors;
    private String avgResponseTime;
    private String owner;
    private String collectionId;
    private String collectionName;
    private String folderId;
    private String folderName;
    private String lastUpdated;
    private String timeAgo;
    private List<Map<String, Object>> parameters;
    private List<Map<String, Object>> responseMappings;
    private List<String> tags;
}