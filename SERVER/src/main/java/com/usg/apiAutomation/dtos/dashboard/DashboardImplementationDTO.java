package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class DashboardImplementationDTO {
    private String id;
    private String name;
    private String description;
    private String method;
    private String url;
    private String collection;
    private String folder;
    private List<String> languages;
    private int implementationsCount;
    private String lastModified;
}