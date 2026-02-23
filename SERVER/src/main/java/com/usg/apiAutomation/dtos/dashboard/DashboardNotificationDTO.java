package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardNotificationDTO {
    private String id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private String time;
    private String icon;
    private String actionUrl;
}