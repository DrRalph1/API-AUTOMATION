package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardSecuritySummaryResponseDTO {
    private int totalEndpoints;
    private int securedEndpoints;
    private int vulnerableEndpoints;
    private long blockedRequests;
    private long throttledRequests;
    private String avgResponseTime;
    private int securityScore;
    private String lastScan;
    private Map<String, Object> quickStats;
    private int activeRateLimitRules;
    private int activeIpWhitelistEntries;
    private int unreadAlerts;
}