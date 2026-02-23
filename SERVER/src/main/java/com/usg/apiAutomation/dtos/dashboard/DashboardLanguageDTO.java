package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardLanguageDTO {
    private String id;
    private String name;
    private String framework;
    private String color;
    private String icon;
    private int implementationCount;
    private boolean available;
}