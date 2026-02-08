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
public class DashboardApisResponse {
    private List<ApiDto> apis;
    private int total;
    private int active;
    private int deprecated;
    private String avgSuccessRate;
    private String avgLatency;
    private int totalEndpoints;
    private int totalCalls;
    private String lastUpdated;

    // Constructor for easy instantiation
    public DashboardApisResponse(List<ApiDto> apis) {
        this.apis = apis;
        this.total = apis.size();
        this.active = (int) apis.stream().filter(a -> "active".equals(a.getStatus())).count();
        this.deprecated = (int) apis.stream().filter(a -> "deprecated".equals(a.getStatus())).count();

        // Calculate averages
        double avgSuccess = apis.stream()
                .mapToDouble(a -> Double.parseDouble(a.getSuccessRate().replace("%", "")))
                .average()
                .orElse(0.0);
        this.avgSuccessRate = String.format("%.1f%%", avgSuccess);

        double avgLatency = apis.stream()
                .mapToDouble(a -> Double.parseDouble(a.getLatency().replace("ms", "")))
                .average()
                .orElse(0.0);
        this.avgLatency = String.format("%.0fms", avgLatency);

        this.totalEndpoints = apis.stream().mapToInt(ApiDto::getEndpointCount).sum();
        this.totalCalls = apis.stream().mapToInt(ApiDto::getCalls).sum();
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }

    // Constructor with all parameters
    public DashboardApisResponse(List<ApiDto> apis, int total, int active, int deprecated,
                                 String avgSuccessRate, String avgLatency, int totalEndpoints,
                                 int totalCalls) {
        this.apis = apis;
        this.total = total;
        this.active = active;
        this.deprecated = deprecated;
        this.avgSuccessRate = avgSuccessRate;
        this.avgLatency = avgLatency;
        this.totalEndpoints = totalEndpoints;
        this.totalCalls = totalCalls;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}