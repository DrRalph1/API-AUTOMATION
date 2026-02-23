package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponseDTO {
    // Collection/API stats
    private int totalApis;
    private int totalCollections;
    private int totalRateLimitRules;
    private int totalIpWhitelistEntries;
    private int unreadSecurityAlerts;

    // Code generation stats
    private int totalCodeImplementations;
    private int supportedLanguages;

    // User stats
    private int totalUsers;
    private int activeUsers;

    // Documentation stats
    private int totalDocumentationEndpoints;
    private int publishedDocumentation;

    // Legacy fields (for backward compatibility)
    private Integer totalConnections;
    private Integer activeConnections;
    private Integer activeApis;
    private Integer totalCalls;
    private String avgLatency;
    private String successRate;
    private String uptime;
    private Integer connectionsChange;
    private Integer apisChange;
    private Integer callsChange;
    private Float successRateChange;
    private String avgResponseTime;
    private String errorRate;
    private Integer peakConnections;
    private String dataTransferred;

    // Timestamp
    private String lastUpdated;
}