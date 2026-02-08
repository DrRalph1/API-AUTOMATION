package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private int totalConnections;
    private int activeConnections;
    private int totalApis;
    private int activeApis;
    private int totalCalls;
    private String avgLatency;
    private String successRate;
    private String uptime;

    // Additional metrics
    private int connectionsChange;
    private int apisChange;
    private int callsChange;
    private float successRateChange;
    private String avgResponseTime;
    private String errorRate;
    private int peakConnections;
    private String dataTransferred;

    // Timestamp
    private String lastUpdated;
}