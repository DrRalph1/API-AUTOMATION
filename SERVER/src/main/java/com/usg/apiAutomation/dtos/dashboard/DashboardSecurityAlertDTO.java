package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardSecurityAlertDTO {
    private String id;
    private String type;
    private String severity;
    private String message;
    private String endpoint;
    private boolean read;
    private String timestamp;
}