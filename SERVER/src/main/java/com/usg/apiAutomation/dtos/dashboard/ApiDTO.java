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
public class ApiDTO {
    private String id;
    private String name;
    private String description;
    private String version;
    private String status;
    private int endpointCount;
    private String lastUpdated;
    private int calls;
    private String latency;
    private String successRate;
    private String baseUrl;
    private String documentation;
    private List<String> supportedMethods;
    private String security;
    private String rateLimit;
    private int errors;
    private String avgResponseTime;
    private String uptime;
    private String lastDeployed;
    private String owner;
    private String category;

    // Additional fields
    private String repositoryUrl;
    private String swaggerUrl;
    private String healthCheckUrl;
    private boolean requiresAuthentication;
    private String authenticationType;
    private List<String> tags;
    private String environment;
    private String department;
    private String sla;
    private String responseFormat;
    private int requestSizeLimit;
    private int responseSizeLimit;
    private String createdBy;
    private String createdAt;
    private String updatedBy;
    private String techStack;
    private String monitoringUrl;
    private String logUrl;
    private String alertEmail;
    private String backupFrequency;
    private String retentionPolicy;
}