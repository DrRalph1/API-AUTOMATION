package com.usg.apiAutomation.dtos.systemActivities.systemLogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryDetails {
    private String payload;
    private String parameter;
    private String protocol;
    private String method;
    private Integer responseCode;
    private String requestId;
    private String performedBy;
    private String policyId;
    private String responseTime;
    private String error;
    private String uri;
}