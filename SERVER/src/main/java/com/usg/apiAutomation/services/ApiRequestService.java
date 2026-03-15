package com.usg.apiAutomation.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiRequestService {

    // ==================== REPOSITORIES ====================
    private final ApiRequestRepository apiRequestRepository;
    private final GeneratedAPIRepository generatedAPIRepository;
    private final LoggerUtil loggerUtil;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    // ==================== CONSTANTS ====================
    private static final String REQUEST_STATUS_SUCCESS = "SUCCESS";
    private static final String REQUEST_STATUS_FAILED = "FAILED";
    private static final String REQUEST_STATUS_TIMEOUT = "TIMEOUT";
    private static final String REQUEST_STATUS_PENDING = "PENDING";

    // =====================================================
    // CAPTURE REQUEST METHODS
    // =====================================================

    /**
     * Capture an API request before execution
     */
    @Transactional
    public ApiRequestResponseDTO captureRequest(
            String requestId,
            String apiId,
            ApiRequestDTO requestDTO,
            String performedBy,
            HttpServletRequest httpServletRequest) {

        long startTime = System.currentTimeMillis();

        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Capturing API request for API: " + apiId + " by: " + performedBy);

            // Get the API entity
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found with ID: " + apiId));

            // Create and populate the request entity
            ApiRequestEntity requestEntity = buildRequestEntity(api, requestDTO, performedBy, httpServletRequest);

            // Set correlation ID if not provided
            if (requestEntity.getCorrelationId() == null) {
                requestEntity.setCorrelationId(generateCorrelationId(apiId));
            }

            // Set request timestamp
            requestEntity.setRequestTimestamp(LocalDateTime.now());
            requestEntity.setRequestStatus(REQUEST_STATUS_PENDING);

            // Generate curl command if needed
            requestEntity.generateCurlCommand();

            // Save the request
            ApiRequestEntity savedRequest = apiRequestRepository.save(requestEntity);

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", API request captured successfully with ID: " + savedRequest.getId() +
                    ", Correlation ID: " + savedRequest.getCorrelationId() +
                    " in " + (System.currentTimeMillis() - startTime) + "ms");

            // Return response DTO
            return mapToResponseDTO(savedRequest, api);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error capturing API request: " + e.getMessage());
            log.error("Error capturing API request", e);
            throw new RuntimeException("Failed to capture API request: " + e.getMessage(), e);
        }
    }

    /**
     * Capture a request with execution details (for async execution)
     */
    @Transactional
    public ApiRequestResponseDTO captureRequestWithExecution(
            String requestId,
            String apiId,
            ExecuteApiRequestDTO executeRequest,
            String performedBy,
            String clientIp,
            String userAgent,
            String correlationId) {

        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Capturing API request with execution for API: " + apiId);

            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found with ID: " + apiId));

            // Convert ExecuteApiRequestDTO to ApiRequestDTO
            ApiRequestDTO requestDTO = convertExecuteRequestToApiRequest(executeRequest, api);
            requestDTO.setClientIpAddress(clientIp);
            requestDTO.setUserAgent(userAgent);
            requestDTO.setRequestedBy(performedBy);
            requestDTO.setCorrelationId(correlationId != null ? correlationId : generateCorrelationId(apiId));

            // Capture the request
            return captureRequest(requestId, apiId, requestDTO, performedBy, null);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error capturing request with execution: " + e.getMessage());
            throw new RuntimeException("Failed to capture request with execution: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // UPDATE RESPONSE METHODS
    // =====================================================

    /**
     * Update a captured request with response details
     */
    @Transactional
    public ApiRequestResponseDTO updateRequestWithResponse(
            String requestId,
            String capturedRequestId,
            ExecuteApiResponseDTO responseDTO,
            Integer statusCode,
            String statusMessage,
            Long executionDurationMs) {

        long startTime = System.currentTimeMillis();

        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Updating captured request: " + capturedRequestId + " with response");

            // Find the captured request
            ApiRequestEntity requestEntity = apiRequestRepository.findById(capturedRequestId)
                    .orElseThrow(() -> new RuntimeException("Captured request not found with ID: " + capturedRequestId));

            // Update with response details
            requestEntity.setResponseTimestamp(LocalDateTime.now());

            if (responseDTO != null) {
                requestEntity.setResponseStatusCode(responseDTO.getResponseCode());
                requestEntity.setResponseStatusMessage(responseDTO.getMessage());

                // Convert response data to Map if possible
                if (responseDTO.getData() != null) {
                    try {
                        if (responseDTO.getData() instanceof Map) {
                            requestEntity.setResponseBody((Map<String, Object>) responseDTO.getData());
                        } else {
                            // Wrap non-Map responses
                            Map<String, Object> wrappedResponse = new HashMap<>();
                            wrappedResponse.put("data", responseDTO.getData());
                            requestEntity.setResponseBody(wrappedResponse);
                        }
                    } catch (Exception e) {
                        log.warn("Could not convert response data to Map: {}", e.getMessage());
                        Map<String, Object> errorWrapped = new HashMap<>();
                        errorWrapped.put("rawResponse", responseDTO.getData().toString());
                        requestEntity.setResponseBody(errorWrapped);
                    }
                }
            }

            // Override with explicit parameters if provided
            if (statusCode != null) {
                requestEntity.setResponseStatusCode(statusCode);
            }
            if (statusMessage != null) {
                requestEntity.setResponseStatusMessage(statusMessage);
            }
            if (executionDurationMs != null) {
                requestEntity.setExecutionDurationMs(executionDurationMs);
            } else if (requestEntity.getExecutionDurationMs() == null) {
                // Calculate duration if not set
                requestEntity.calculateExecutionDuration();
            }

            // Determine request status based on response code
            determineAndSetRequestStatus(requestEntity);

            // Save the updated request
            ApiRequestEntity updatedRequest = apiRequestRepository.save(requestEntity);

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Request updated successfully with response. Status: " + updatedRequest.getRequestStatus() +
                    ", Status Code: " + updatedRequest.getResponseStatusCode() +
                    " in " + (System.currentTimeMillis() - startTime) + "ms");

            // Get the associated API for the response
            GeneratedApiEntity api = updatedRequest.getGeneratedApi();

            return mapToResponseDTO(updatedRequest, api);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error updating request with response: " + e.getMessage());
            log.error("Error updating request with response", e);
            throw new RuntimeException("Failed to update request with response: " + e.getMessage(), e);
        }
    }

    /**
     * Update a captured request with error details
     */
    @Transactional
    public ApiRequestResponseDTO updateRequestWithError(
            String requestId,
            String capturedRequestId,
            Integer statusCode,
            String errorMessage,
            Long executionDurationMs) {

        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Updating captured request: " + capturedRequestId + " with error");

            ApiRequestEntity requestEntity = apiRequestRepository.findById(capturedRequestId)
                    .orElseThrow(() -> new RuntimeException("Captured request not found with ID: " + capturedRequestId));

            requestEntity.setResponseTimestamp(LocalDateTime.now());
            requestEntity.setResponseStatusCode(statusCode != null ? statusCode : 500);
            requestEntity.setResponseStatusMessage(errorMessage);
            requestEntity.setErrorMessage(errorMessage);
            requestEntity.setRequestStatus(REQUEST_STATUS_FAILED);

            if (executionDurationMs != null) {
                requestEntity.setExecutionDurationMs(executionDurationMs);
            } else {
                requestEntity.calculateExecutionDuration();
            }

            // Create error response body
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", true);
            errorBody.put("message", errorMessage);
            errorBody.put("timestamp", LocalDateTime.now().toString());
            errorBody.put("statusCode", statusCode != null ? statusCode : 500);
            requestEntity.setResponseBody(errorBody);

            ApiRequestEntity updatedRequest = apiRequestRepository.save(requestEntity);
            GeneratedApiEntity api = updatedRequest.getGeneratedApi();

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Request updated with error successfully");

            return mapToResponseDTO(updatedRequest, api);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error updating request with error: " + e.getMessage());
            throw new RuntimeException("Failed to update request with error: " + e.getMessage(), e);
        }
    }

    /**
     * Batch update multiple requests with responses
     */
    @Transactional
    public List<ApiRequestResponseDTO> batchUpdateResponses(
            String requestId,
            Map<String, ExecuteApiResponseDTO> requestIdToResponseMap) {

        List<ApiRequestResponseDTO> results = new ArrayList<>();

        for (Map.Entry<String, ExecuteApiResponseDTO> entry : requestIdToResponseMap.entrySet()) {
            try {
                ApiRequestResponseDTO updated = updateRequestWithResponse(
                        requestId,
                        entry.getKey(),
                        entry.getValue(),
                        null,
                        null,
                        null
                );
                results.add(updated);
            } catch (Exception e) {
                log.error("Failed to update request {}: {}", entry.getKey(), e.getMessage());
            }
        }

        return results;
    }

    // =====================================================
    // RETRIEVAL METHODS
    // =====================================================

    /**
     * Get request by ID
     */
    public ApiRequestResponseDTO getRequestById(String requestId, String capturedRequestId) {
        try {
            ApiRequestEntity requestEntity = apiRequestRepository.findById(capturedRequestId)
                    .orElseThrow(() -> new RuntimeException("Request not found with ID: " + capturedRequestId));

            GeneratedApiEntity api = requestEntity.getGeneratedApi();
            return mapToResponseDTO(requestEntity, api);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error getting request by ID: " + e.getMessage());
            throw new RuntimeException("Failed to get request: " + e.getMessage(), e);
        }
    }

    /**
     * Get request by correlation ID
     */
    public ApiRequestResponseDTO getRequestByCorrelationId(String requestId, String correlationId) {
        try {
            ApiRequestEntity requestEntity = apiRequestRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new RuntimeException("Request not found with correlation ID: " + correlationId));

            GeneratedApiEntity api = requestEntity.getGeneratedApi();
            return mapToResponseDTO(requestEntity, api);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error getting request by correlation ID: " + e.getMessage());
            throw new RuntimeException("Failed to get request by correlation ID: " + e.getMessage(), e);
        }
    }

    /**
     * Get all requests for an API
     */
    public List<ApiRequestResponseDTO> getRequestsByApiId(String apiId, int limit) {
        try {
            PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("requestTimestamp").descending());
            Page<ApiRequestEntity> requestsPage = apiRequestRepository.findByGeneratedApiId(apiId, pageRequest);

            GeneratedApiEntity api = generatedAPIRepository.findById(apiId).orElse(null);

            return requestsPage.getContent().stream()
                    .map(req -> mapToResponseDTO(req, api))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting requests by API ID: {}", e.getMessage());
            throw new RuntimeException("Failed to get requests by API ID: " + e.getMessage(), e);
        }
    }

    /**
     * Get requests with pagination and filtering
     */
    public Page<ApiRequestResponseDTO> searchRequests(String requestId, ApiRequestFilterDTO filter) {
        try {
            Pageable pageable = createPageableFromFilter(filter);
            Page<ApiRequestEntity> requestsPage;

            if (filter.getApiId() != null && !filter.getApiId().isEmpty()) {
                if (filter.getFromDate() != null && filter.getToDate() != null) {
                    // Complex query with date range
                    List<ApiRequestEntity> requests = apiRequestRepository.findApiRequestsByStatusAndDateRange(
                            filter.getApiId(),
                            filter.getRequestStatus() != null ? filter.getRequestStatus() : "",
                            filter.getFromDate(),
                            filter.getToDate()
                    );
                    // Manual pagination (simplified - in production use proper pagination)
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), requests.size());
                    List<ApiRequestEntity> pagedContent = start < requests.size() ?
                            requests.subList(start, end) : new ArrayList<>();

                    return new org.springframework.data.domain.PageImpl<>(
                            pagedContent.stream()
                                    .map(req -> mapToResponseDTO(req, req.getGeneratedApi()))
                                    .collect(Collectors.toList()),
                            pageable,
                            requests.size()
                    );
                } else {
                    // Simple query by API ID
                    requestsPage = apiRequestRepository.findByGeneratedApiId(filter.getApiId(), pageable);
                }
            } else if (filter.getRequestStatus() != null) {
                requestsPage = apiRequestRepository.findByRequestStatus(filter.getRequestStatus(), pageable);
            } else if (filter.getCorrelationId() != null) {
                Optional<ApiRequestEntity> request = apiRequestRepository.findByCorrelationId(filter.getCorrelationId());
                List<ApiRequestEntity> requestList = request.map(List::of).orElseGet(List::of);
                return new org.springframework.data.domain.PageImpl<>(
                        requestList.stream()
                                .map(req -> mapToResponseDTO(req, req.getGeneratedApi()))
                                .collect(Collectors.toList()),
                        pageable,
                        requestList.size()
                );
            } else {
                requestsPage = apiRequestRepository.findAllByOrderByRequestTimestampDesc(pageable);
            }

            return requestsPage.map(req -> mapToResponseDTO(req, req.getGeneratedApi()));

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error searching requests: " + e.getMessage());
            throw new RuntimeException("Failed to search requests: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // STATISTICS AND ANALYTICS METHODS
    // =====================================================

    /**
     * Get request statistics for an API
     */
    public ApiRequestStatisticsDTO getRequestStatistics(String apiId, LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            List<ApiRequestEntity> requests;

            if (fromDate != null && toDate != null) {
                requests = apiRequestRepository.findByGeneratedApiIdAndRequestTimestampBetween(
                        apiId, fromDate, toDate);
            } else {
                // Default to last 30 days
                toDate = LocalDateTime.now();
                fromDate = toDate.minusDays(30);
                requests = apiRequestRepository.findByGeneratedApiIdAndRequestTimestampBetween(
                        apiId, fromDate, toDate);
            }

            return buildStatistics(requests, fromDate, toDate);

        } catch (Exception e) {
            log.error("Error getting request statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to get request statistics: " + e.getMessage(), e);
        }
    }



    /**
     * Get requests for an API within a date range
     * This method returns the entity objects directly
     */
    public List<ApiRequestEntity> getRequestsByApiIdAndDateRange(
            String apiId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Fetching requests for API: {} between {} and {}", apiId, startDate, endDate);

        return apiRequestRepository.findByGeneratedApiIdAndRequestTimestampBetween(
                apiId, startDate, endDate);
    }

    /**
     * Get requests for an API within a date range and return as DTOs
     */
    public List<ApiRequestResponseDTO> getRequestsByApiIdAndDateRangeAsDTO(
            String apiId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Fetching requests DTOs for API: {} between {} and {}", apiId, startDate, endDate);

        List<ApiRequestEntity> requests = apiRequestRepository.findByGeneratedApiIdAndRequestTimestampBetween(
                apiId, startDate, endDate);

        GeneratedApiEntity api = generatedAPIRepository.findById(apiId).orElse(null);

        return requests.stream()
                .map(req -> mapToResponseDTO(req, api))
                .collect(Collectors.toList());
    }



    /**
     * Get overall system statistics
     */
    public ApiRequestStatisticsDTO getSystemStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            List<ApiRequestEntity> requests;

            if (fromDate != null && toDate != null) {
                requests = apiRequestRepository.findByRequestTimestampBetween(fromDate, toDate);
            } else {
                // Default to last 7 days
                toDate = LocalDateTime.now();
                fromDate = toDate.minusDays(7);
                requests = apiRequestRepository.findByRequestTimestampBetween(fromDate, toDate);
            }

            return buildStatistics(requests, fromDate, toDate);

        } catch (Exception e) {
            log.error("Error getting system statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to get system statistics: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // DELETE METHODS
    // =====================================================

    /**
     * Delete a captured request
     */
    @Transactional
    public void deleteRequest(String requestId, String capturedRequestId) {
        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Deleting captured request: " + capturedRequestId);

            if (!apiRequestRepository.existsById(capturedRequestId)) {
                throw new RuntimeException("Request not found with ID: " + capturedRequestId);
            }

            apiRequestRepository.deleteById(capturedRequestId);

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Request deleted successfully: " + capturedRequestId);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error deleting request: " + e.getMessage());
            throw new RuntimeException("Failed to delete request: " + e.getMessage(), e);
        }
    }

    /**
     * Delete old requests (cleanup)
     */
    @Transactional
    public long deleteOldRequests(LocalDateTime beforeDate) {
        try {
            long deletedCount = apiRequestRepository.deleteByRequestTimestampBefore(beforeDate);
            log.info("Deleted {} requests older than {}", deletedCount, beforeDate);
            return deletedCount;
        } catch (Exception e) {
            log.error("Error deleting old requests: {}", e.getMessage());
            throw new RuntimeException("Failed to delete old requests: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================

    /**
     * Build request entity from DTO
     */
    private ApiRequestEntity buildRequestEntity(
            GeneratedApiEntity api,
            ApiRequestDTO dto,
            String performedBy,
            HttpServletRequest httpServletRequest) {

        ApiRequestEntity entity = new ApiRequestEntity();

        // Set API reference
        entity.setGeneratedApi(api);
        entity.setApiId(api.getId());

        // Set basic request info
        entity.setRequestName(dto.getRequestName() != null ? dto.getRequestName() :
                "Request for " + api.getApiName());
        entity.setDescription(dto.getDescription());
        entity.setHttpMethod(dto.getHttpMethod() != null ? dto.getHttpMethod() : api.getHttpMethod());
        entity.setUrl(dto.getUrl() != null ? dto.getUrl() : buildFullUrl(api));
        entity.setBasePath(dto.getBasePath() != null ? dto.getBasePath() : api.getBasePath());
        entity.setEndpointPath(dto.getEndpointPath() != null ? dto.getEndpointPath() : api.getEndpointPath());
        entity.setRequestTimeoutSeconds(dto.getRequestTimeoutSeconds());

        // Set request components
        entity.setPathParameters(dto.getPathParameters() != null ? dto.getPathParameters() : new HashMap<>());
        entity.setQueryParameters(dto.getQueryParameters() != null ? dto.getQueryParameters() : new HashMap<>());
        entity.setHeaders(dto.getHeaders() != null ? dto.getHeaders() : new HashMap<>());
        entity.setRequestBody(dto.getRequestBody());
        entity.setFormData(dto.getFormData());
        entity.setMultipartData(dto.getMultipartData());

        // Set authentication
        entity.setAuthType(dto.getAuthType());
        entity.setAuthToken(dto.getAuthToken()); // In production, encrypt this!
        entity.setApiKey(dto.getApiKey()); // In production, encrypt this!

        // Set client information (prioritize DTO, then HTTP request)
        entity.setClientIpAddress(dto.getClientIpAddress());
        entity.setUserAgent(dto.getUserAgent());
        entity.setSourceApplication(dto.getSourceApplication());
        entity.setRequestedBy(dto.getRequestedBy() != null ? dto.getRequestedBy() : performedBy);

        // Set tracking
        entity.setCorrelationId(dto.getCorrelationId());
        entity.setIsMockRequest(dto.getIsMockRequest() != null ? dto.getIsMockRequest() : false);
        entity.setMetadata(dto.getMetadata() != null ? dto.getMetadata() : new HashMap<>());

        // Set retry count
        entity.setRetryCount(0);

        return entity;
    }

    /**
     * Convert ExecuteApiRequestDTO to ApiRequestDTO
     */
    private ApiRequestDTO convertExecuteRequestToApiRequest(ExecuteApiRequestDTO executeRequest, GeneratedApiEntity api) {
        ApiRequestDTO dto = new ApiRequestDTO();

        dto.setRequestName("Execution: " + api.getApiName());
        dto.setHttpMethod(executeRequest.getHttpMethod());
        dto.setUrl(executeRequest.getUrl());

        // Convert parameters
        if (executeRequest.getPathParams() != null) {
            dto.setPathParameters(new HashMap<>(executeRequest.getPathParams()));
        }
        if (executeRequest.getQueryParams() != null) {
            dto.setQueryParameters(new HashMap<>(executeRequest.getQueryParams()));
        }
        if (executeRequest.getHeaders() != null) {
            dto.setHeaders(new HashMap<>(executeRequest.getHeaders()));
        }

        // Convert body
        if (executeRequest.getBody() != null) {
            if (executeRequest.getBody() instanceof Map) {
                dto.setRequestBody((Map<String, Object>) executeRequest.getBody());
            } else {
                Map<String, Object> wrappedBody = new HashMap<>();
                wrappedBody.put("data", executeRequest.getBody());
                dto.setRequestBody(wrappedBody);
            }
        }

        dto.setRequestTimeoutSeconds(executeRequest.getTimeoutSeconds());
        dto.setCorrelationId(executeRequest.getRequestId());
        dto.setMetadata(executeRequest.getMetadata());

        return dto;
    }

    /**
     * Map entity to response DTO
     */
    private ApiRequestResponseDTO mapToResponseDTO(ApiRequestEntity entity, GeneratedApiEntity api) {
        if (entity == null) return null;

        ApiRequestResponseDTO dto = new ApiRequestResponseDTO();

        // Core identification
        dto.setId(entity.getId());
        dto.setApiId(entity.getApiId());
        dto.setApiName(api != null ? api.getApiName() : null);
        dto.setApiCode(api != null ? api.getApiCode() : null);
        dto.setRequestName(entity.getRequestName());
        dto.setCorrelationId(entity.getCorrelationId());

        // Request details
        dto.setHttpMethod(entity.getHttpMethod());
        dto.setUrl(entity.getUrl());
        dto.setBasePath(entity.getBasePath());
        dto.setEndpointPath(entity.getEndpointPath());
        dto.setRequestTimeoutSeconds(entity.getRequestTimeoutSeconds());

        // Request components
        dto.setPathParameters(entity.getPathParameters());
        dto.setQueryParameters(entity.getQueryParameters());
        dto.setHeaders(entity.getHeaders());
        dto.setRequestBody(entity.getRequestBody());
        dto.setFormData(entity.getFormData());
        dto.setMultipartData(entity.getMultipartData());

        // Response details
        dto.setResponseStatusCode(entity.getResponseStatusCode());
        dto.setResponseStatusMessage(entity.getResponseStatusMessage());
        dto.setResponseBody(entity.getResponseBody());
        dto.setResponseHeaders(entity.getResponseHeaders());
        dto.setResponseSizeBytes(entity.getResponseSizeBytes());

        // Timing information
        dto.setRequestTimestamp(entity.getRequestTimestamp());
        dto.setResponseTimestamp(entity.getResponseTimestamp());
        dto.setExecutionDurationMs(entity.getExecutionDurationMs());
        dto.setFormattedDuration(formatDuration(entity.getExecutionDurationMs()));

        // Status & error
        dto.setRequestStatus(entity.getRequestStatus());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setRetryCount(entity.getRetryCount());

        // Authentication
        dto.setAuthType(entity.getAuthType());
        dto.setIsAuthenticated(entity.getResponseStatusCode() != null &&
                entity.getResponseStatusCode() < 400 &&
                entity.getResponseStatusCode() != 401 &&
                entity.getResponseStatusCode() != 403);

        // Client information
        dto.setClientIpAddress(entity.getClientIpAddress());
        dto.setUserAgent(entity.getUserAgent());
        dto.setSourceApplication(entity.getSourceApplication());
        dto.setRequestedBy(entity.getRequestedBy());

        // Additional information
        dto.setIsMockRequest(entity.getIsMockRequest());
        dto.setCurlCommand(entity.getCurlCommand());
        dto.setMetadata(entity.getMetadata());

        // Audit fields
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getRequestedBy());

        // Add summary statistics if API is provided
        if (api != null) {
            dto.setSummary(buildRequestSummary(api.getId()));
        }

        return dto;
    }

    /**
     * Build request summary for an API
     */
    private ApiRequestResponseDTO.ApiRequestSummaryDTO buildRequestSummary(String apiId) {
        long totalRequests = apiRequestRepository.countByGeneratedApiId(apiId);
        long successfulRequests = apiRequestRepository.countSuccessfulRequestsByApiId(apiId);
        long failedRequests = apiRequestRepository.countFailedRequestsByApiId(apiId);
        Double avgTime = apiRequestRepository.getAverageResponseTimeByApiId(apiId);
        Long maxTime = apiRequestRepository.getMaxResponseTimeByApiId(apiId);
        Long minTime = apiRequestRepository.getMinResponseTimeByApiId(apiId);

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayCount = apiRequestRepository.countByGeneratedApiIdAndRequestTimestampBetween(
                apiId, today, LocalDateTime.now());

        return ApiRequestResponseDTO.ApiRequestSummaryDTO.builder()
                .totalRequestsForApi(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .averageResponseTime(avgTime != null ? avgTime : 0.0)
                .minResponseTime(minTime != null ? minTime : 0L)
                .maxResponseTime(maxTime != null ? maxTime : 0L)
                .requestCountToday((int) todayCount)
                .build();
    }

    /**
     * Build full URL from API
     */
    private String buildFullUrl(GeneratedApiEntity api) {
        StringBuilder url = new StringBuilder();
        if (api.getBasePath() != null && !api.getBasePath().isEmpty()) {
            url.append(api.getBasePath());
        }
        if (api.getEndpointPath() != null && !api.getEndpointPath().isEmpty()) {
            if (url.length() > 0 && !api.getEndpointPath().startsWith("/")) {
                url.append("/");
            }
            url.append(api.getEndpointPath());
        }
        return url.toString();
    }

    /**
     * Generate correlation ID
     */
    private String generateCorrelationId(String apiId) {
        return "REQ-" + apiId + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Format duration in human-readable format
     */
    private String formatDuration(Long durationMs) {
        if (durationMs == null) return null;
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.2fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Determine and set request status based on response code
     */
    private void determineAndSetRequestStatus(ApiRequestEntity entity) {
        if (entity.getResponseStatusCode() == null) {
            entity.setRequestStatus(REQUEST_STATUS_PENDING);
            return;
        }

        int statusCode = entity.getResponseStatusCode();

        if (statusCode >= 200 && statusCode < 300) {
            entity.setRequestStatus(REQUEST_STATUS_SUCCESS);
        } else if (statusCode == 408 || statusCode == 504) {
            entity.setRequestStatus(REQUEST_STATUS_TIMEOUT);
        } else {
            entity.setRequestStatus(REQUEST_STATUS_FAILED);
        }
    }

    /**
     * Create Pageable from filter
     */
    private Pageable createPageableFromFilter(ApiRequestFilterDTO filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "requestTimestamp";
        Sort.Direction direction = "DESC".equalsIgnoreCase(filter.getSortDirection()) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    /**
     * Build statistics from requests
     */
    private ApiRequestStatisticsDTO buildStatistics(List<ApiRequestEntity> requests, LocalDateTime fromDate, LocalDateTime toDate) {
        ApiRequestStatisticsDTO stats = new ApiRequestStatisticsDTO();
        stats.setFromDate(fromDate);
        stats.setToDate(toDate);

        long total = requests.size();
        long success = requests.stream()
                .filter(r -> r.getResponseStatusCode() != null && r.getResponseStatusCode() >= 200 && r.getResponseStatusCode() < 300)
                .count();
        long failed = requests.stream()
                .filter(r -> r.getResponseStatusCode() != null && r.getResponseStatusCode() >= 400)
                .count();
        long timeout = requests.stream()
                .filter(r -> r.getResponseStatusCode() != null && (r.getResponseStatusCode() == 408 || r.getResponseStatusCode() == 504))
                .count();
        long pending = requests.stream()
                .filter(r -> r.getRequestStatus() == null || REQUEST_STATUS_PENDING.equals(r.getRequestStatus()))
                .count();

        stats.setTotalRequests(total);
        stats.setSuccessfulRequests(success);
        stats.setFailedRequests(failed);
        stats.setTimeoutRequests(timeout);
        stats.setPendingRequests(pending);
        stats.setSuccessRate(total > 0 ? (success * 100.0 / total) : 0.0);
        stats.setFailureRate(total > 0 ? (failed * 100.0 / total) : 0.0);

        // Performance metrics
        DoubleSummaryStatistics durationStats = requests.stream()
                .filter(r -> r.getExecutionDurationMs() != null)
                .mapToDouble(ApiRequestEntity::getExecutionDurationMs)
                .summaryStatistics();

        stats.setAverageResponseTime(durationStats.getAverage());
        stats.setMinResponseTime((long) durationStats.getMin());
        stats.setMaxResponseTime((long) durationStats.getMax());

        // Percentile calculations (simplified)
        List<Long> durations = requests.stream()
                .filter(r -> r.getExecutionDurationMs() != null)
                .map(ApiRequestEntity::getExecutionDurationMs)
                .sorted()
                .collect(Collectors.toList());

        if (!durations.isEmpty()) {
            stats.setMedianResponseTime(calculatePercentile(durations, 50));
            stats.setP95ResponseTime(calculatePercentile(durations, 95));
            stats.setP99ResponseTime(calculatePercentile(durations, 99));
        }

        // Status code distribution
        Map<Integer, Long> statusDist = requests.stream()
                .filter(r -> r.getResponseStatusCode() != null)
                .collect(Collectors.groupingBy(
                        ApiRequestEntity::getResponseStatusCode,
                        Collectors.counting()
                ));
        stats.setStatusCodeDistribution(statusDist);

        // Method distribution
        Map<String, Long> methodDist = requests.stream()
                .collect(Collectors.groupingBy(
                        ApiRequestEntity::getHttpMethod,
                        Collectors.counting()
                ));
        stats.setMethodDistribution(methodDist);

        return stats;
    }

    /**
     * Calculate percentile (simplified implementation)
     */
    private Double calculatePercentile(List<Long> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) return 0.0;
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index).doubleValue();
    }
}