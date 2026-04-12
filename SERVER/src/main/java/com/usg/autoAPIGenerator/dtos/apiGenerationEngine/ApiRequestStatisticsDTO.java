package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestStatisticsDTO {

    // =====================================================
    // Time Period
    // =====================================================

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String period; // HOURLY, DAILY, WEEKLY, MONTHLY, CUSTOM

    // =====================================================
    // Overall Statistics
    // =====================================================

    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
    private Long pendingRequests;
    private Long timeoutRequests;
    private Double successRate;
    private Double failureRate;
    private Double timeoutRate;

    // =====================================================
    // Performance Metrics
    // =====================================================

    private Double averageResponseTime;
    private Long minResponseTime;
    private Long maxResponseTime;
    private Double medianResponseTime;
    private Double p95ResponseTime; // 95th percentile
    private Double p99ResponseTime; // 99th percentile
    private Double standardDeviation;
    private Long totalExecutionTime; // Sum of all execution times

    // =====================================================
    // Distribution Maps
    // =====================================================

    private Map<Integer, Long> statusCodeDistribution;
    private Map<String, Long> methodDistribution;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> authTypeDistribution;
    private Map<String, Long> sourceApplicationDistribution;

    // =====================================================
    // Time Series Data
    // =====================================================

    private List<TimeSeriesDataPoint> timeSeriesData;
    private List<HourlyStats> hourlyStats;
    private List<DailyStats> dailyStats;

    // =====================================================
    // Top Lists
    // =====================================================

    private List<ApiPerformanceDTO> topApisByCalls;
    private List<ApiPerformanceDTO> topApisByResponseTime;
    private List<ApiPerformanceDTO> slowestApis;
    private List<ApiPerformanceDTO> fastestApis;

    // =====================================================
    // Error Analysis
    // =====================================================

    private List<ErrorAnalysisDTO> topErrors;
    private Map<Integer, Long> errorTrend;
    private List<String> mostCommonErrorMessages;

    // =====================================================
    // Client Analysis
    // =====================================================

    private List<ClientStatsDTO> topClients;
    private Map<String, Long> userAgentDistribution;
    private Map<String, Long> ipAddressDistribution;

    // =====================================================
    // Request Size Analysis
    // =====================================================

    private Double averageRequestSize;
    private Double averageResponseSize;
    private Long totalRequestSize;
    private Long totalResponseSize;
    private Map<String, Long> sizeDistribution;

    // =====================================================
    // Nested Classes
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesDataPoint {
        private LocalDateTime timestamp;
        private Long requestCount;
        private Long successCount;
        private Long failureCount;
        private Long timeoutCount;
        private Double averageResponseTime;
        private Long minResponseTime;
        private Long maxResponseTime;
        private Integer averageStatusCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyStats {
        private String hour; // Format: yyyy-MM-dd HH:00
        private LocalDateTime hourStart;
        private Long requestCount;
        private Double averageResponseTime;
        private Map<Integer, Long> statusCodes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStats {
        private String date; // Format: yyyy-MM-dd
        private LocalDateTime dayStart;
        private Long requestCount;
        private Double averageResponseTime;
        private Map<Integer, Long> statusCodes;
        private Long uniqueClients;
        private Long uniqueApis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiPerformanceDTO {
        private String apiId;
        private String apiName;
        private String apiCode;
        private String httpMethod;
        private String endpointPath;
        private Long totalCalls;
        private Long successfulCalls;
        private Long failedCalls;
        private Double successRate;
        private Double averageResponseTime;
        private Long maxResponseTime;
        private Long minResponseTime;
        private Double p95ResponseTime;
        private Double p99ResponseTime;
        private LocalDateTime lastCalledAt;
        private Long totalExecutionTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorAnalysisDTO {
        private Integer statusCode;
        private String errorMessage;
        private Long occurrenceCount;
        private Double percentageOfTotal;
        private List<String> sampleCorrelationIds;
        private List<String> affectedApis;
        private String firstOccurrence;
        private String lastOccurrence;
        private String suggestedFix;
        private String errorCategory; // CLIENT_ERROR, SERVER_ERROR, TIMEOUT, AUTH_ERROR
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientStatsDTO {
        private String clientIpAddress;
        private String sourceApplication;
        private Long requestCount;
        private Long successfulRequests;
        private Long failedRequests;
        private Double averageResponseTime;
        private List<String> mostUsedApis;
        private String lastRequestTime;
    }
}