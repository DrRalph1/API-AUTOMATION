package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardSecurityEventDTO {
    private String id;
    private String type;
    private String severity;
    private String sourceIp;
    private String endpoint;
    private String message;
    private String timestamp;
}