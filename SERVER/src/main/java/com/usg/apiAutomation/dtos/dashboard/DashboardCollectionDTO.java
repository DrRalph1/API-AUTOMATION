package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardCollectionDTO {
    private String id;
    private String name;
    private String description;
    private int requestsCount;
    private int folderCount;
    private boolean favorite;
    private String owner;
    private String lastUpdated;
    private int totalFolders;
    private int totalRequests;
}