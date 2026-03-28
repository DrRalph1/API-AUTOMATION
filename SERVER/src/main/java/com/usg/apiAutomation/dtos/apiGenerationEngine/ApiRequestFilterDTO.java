package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestFilterDTO {

    // =====================================================
    // Basic Filters
    // =====================================================

    private String apiId;
    private String apiCode;
    private String requestName;
    private String httpMethod;
    private String requestStatus; // SUCCESS, FAILED, TIMEOUT, PENDING
    private List<Integer> responseStatusCodes;
    private String correlationId;

    // ADD THIS - Search term for full-text search
    private String search;  // This will search across requestName, url, correlationId, etc.

    // =====================================================
    // Date Range Filters
    // =====================================================

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // =====================================================
    // Performance Filters
    // =====================================================

    private Long minDuration; // Minimum execution duration in ms
    private Long maxDuration; // Maximum execution duration in ms
    private Boolean hasError; // Filter requests with errors

    // =====================================================
    // Client Information Filters
    // =====================================================

    private String clientIpAddress;
    private String userAgent;
    private String sourceApplication;
    private String requestedBy;

    // =====================================================
    // Request Type Filters
    // =====================================================

    private Boolean isMockRequest;
    private String authType;

    // =====================================================
    // Metadata Filters
    // =====================================================

    private List<String> tags;
    private String metadataKey; // Filter by existence of metadata key
    private String metadataValue; // Filter by metadata key-value pair

    // =====================================================
    // Pagination
    // =====================================================

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection; // ASC or DESC

    // =====================================================
    // Advanced Filters
    // =====================================================

    private Boolean hasRequestBody; // Filter requests with body
    private Boolean hasResponseBody; // Filter requests with response
    private String requestBodyContains; // Search in request body
    private String responseBodyContains; // Search in response body
    private String headerKey; // Filter by header existence
    private String headerValue; // Filter by header key-value pair
    private String queryParamKey; // Filter by query parameter existence
    private String pathParamKey; // Filter by path parameter existence

    // =====================================================
    // Helper Methods
    // =====================================================

    public boolean hasDateRange() {
        return fromDate != null && toDate != null;
    }

    public boolean hasDurationRange() {
        return minDuration != null && maxDuration != null;
    }

    public boolean hasPagination() {
        return page != null && size != null;
    }

    public boolean hasSorting() {
        return sortBy != null && !sortBy.isEmpty();
    }

    // Add helper for search
    public boolean hasSearch() {
        return search != null && !search.trim().isEmpty();
    }
}