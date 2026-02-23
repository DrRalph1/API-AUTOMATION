package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    private String id;
    private String type;
    private String description;
    private Date timestamp;
    private String ipAddress;
    private String device;
    private String severity;
    private String performedBy;
    private String action;
    private String location;
    private boolean success;
    private Map<String, Object> details;
}