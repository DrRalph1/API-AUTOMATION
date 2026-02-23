package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardRateLimitRuleDTO {
    private String id;
    private String name;
    private String description;
    private String endpoint;
    private String method;
    private Integer limit;
    private String window;
    private String status;
    private String createdAt;
}