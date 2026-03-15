package com.usg.apiAutomation.dtos.apiGenerationEngine;

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
public class ApiRequestResponseDTO {

    // =====================================================
    // Core Identification
    // =====================================================

    private String id;
    private String apiId;
    private String apiName;
    private String apiCode;
    private String requestName;
    private String correlationId;

    // =====================================================
    // Request Details
    // =====================================================

    private String httpMethod;
    private String url;
    private String basePath;
    private String endpointPath;
    private Integer requestTimeoutSeconds;

    // =====================================================
    // Request Components
    // =====================================================

    private Map<String, Object> pathParameters;
    private Map<String, Object> queryParameters;
    private Map<String, String> headers;
    private Map<String, Object> requestBody;
    private Map<String, Object> formData;
    private Map<String, Object> multipartData;

    // =====================================================
    // Response Details
    // =====================================================

    private Integer responseStatusCode;
    private String responseStatusMessage;
    private Map<String, Object> responseBody;
    private Map<String, String> responseHeaders;
    private Long responseSizeBytes;

    // =====================================================
    // Timing Information
    // =====================================================

    private LocalDateTime requestTimestamp;
    private LocalDateTime responseTimestamp;
    private Long executionDurationMs;
    private String formattedDuration; // Human readable format (e.g., "2.5s", "150ms")

    // =====================================================
    // Status & Error Information
    // =====================================================

    private String requestStatus; // SUCCESS, FAILED, TIMEOUT, PENDING
    private String errorMessage;
    private Integer retryCount;

    // =====================================================
    // Authentication
    // =====================================================

    private String authType;
    private Boolean isAuthenticated;

    // =====================================================
    // Client Information
    // =====================================================

    private String clientIpAddress;
    private String userAgent;
    private String sourceApplication;
    private String requestedBy;

    // =====================================================
    // Additional Information
    // =====================================================

    private Boolean isMockRequest;
    private String curlCommand;
    private Map<String, Object> metadata;
    private List<String> tags;

    // =====================================================
    // Audit Fields
    // =====================================================

    private LocalDateTime createdAt;
    private String createdBy;

    // =====================================================
    // Summary Statistics
    // =====================================================

    private ApiRequestSummaryDTO summary;

    // Helper inner class for summary statistics
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiRequestSummaryDTO {
        private Long totalRequestsForApi;
        private Long successfulRequests;
        private Long failedRequests;
        private Double averageResponseTime;
        private Long minResponseTime;
        private Long maxResponseTime;
        private Integer requestCountToday;
    }
}