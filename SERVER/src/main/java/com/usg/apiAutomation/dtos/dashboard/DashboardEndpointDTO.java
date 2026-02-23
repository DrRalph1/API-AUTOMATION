package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardEndpointDTO {
    private String id;
    private String name;
    private String method;
    private String url;
    private String description;
    private String collectionId;
    private String collectionName;
    private String folderId;
    private String folderName;
    private String lastModified;
}