package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private String id;
    private String action;
    private String description;
    private String user;
    private String time;
    private String timestamp;
    private String icon;
    private String priority;
    private String details;
    private String affectedResource;
    private String actionType;

    // Additional fields
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String location;
    private String department;
    private String project;
    private String module;
    private String severity;
    private String status;
    private String duration;
    private String startTime;
    private String endTime;
    private List<String> relatedResources;
    private String errorCode;
    private String errorMessage;
    private String resolution;
    private String assignedTo;
    private String tags;
    private String source;
    private String target;
    private String oldValue;
    private String newValue;
    private String metadata;
}