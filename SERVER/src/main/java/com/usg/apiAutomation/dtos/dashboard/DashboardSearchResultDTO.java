package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardSearchResultDTO {
    private String id;
    private String title;
    private String type;
    private String description;
    private String url;
    private String subtitle;
}