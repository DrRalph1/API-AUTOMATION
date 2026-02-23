package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardIpWhitelistEntryDTO {
    private String id;
    private String name;
    private String ipRange;
    private String description;
    private String status;
    private String createdAt;
}