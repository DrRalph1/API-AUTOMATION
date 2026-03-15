package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiRequestEntity;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.helpers.apiEngine.LoggingHelper;
import com.usg.apiAutomation.helpers.apiEngine.RequestExtractorHelper;
import com.usg.apiAutomation.helpers.apiEngine.RequestValidatorHelper;
import com.usg.apiAutomation.helpers.apiEngine.ResponseBuilderHelper;
import com.usg.apiAutomation.services.ApiRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/requests")
@RequiredArgsConstructor
@Tag(name = "API REQUESTS", description = "Endpoints for capturing, tracking and managing API requests")
public class ApiRequestController {

    private final ApiRequestService apiRequestService;
    private final JwtHelper jwtHelper;
    private final RequestExtractorHelper requestExtractorHelper;
    private final ResponseBuilderHelper responseBuilderHelper;
    private final RequestValidatorHelper requestValidatorHelper;
    private final LoggingHelper loggingHelper;

    // =====================================================
    // CAPTURE REQUEST ENDPOINTS
    // =====================================================

    @PostMapping("/capture/{apiId}")
    @Operation(summary = "Capture API Request", description = "Capture an API request before execution")
    public ResponseEntity<?> captureRequest(
            @PathVariable String apiId,
            @Valid @RequestBody ApiRequestDTO requestDTO,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "capturing API request");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String validationErrors = requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null) {
                return responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Capturing API request for API: " + apiId + " by: " + performedBy);

            ApiRequestResponseDTO response = apiRequestService.captureRequest(
                    requestId, apiId, requestDTO, performedBy, req);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API request captured successfully",
                    response);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "capturing API request", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while capturing API request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/capture/{apiId}/from-execution")
    @Operation(summary = "Capture Request from Execution", description = "Capture an API request from execution details")
    public ResponseEntity<?> captureRequestFromExecution(
            @PathVariable String apiId,
            @Valid @RequestBody ExecuteApiRequestDTO executeRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "capturing API request from execution");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String validationErrors = requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null) {
                return responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            String clientIp = requestExtractorHelper.extractClientIp(req);
            String userAgent = req.getHeader("User-Agent");
            String correlationId = req.getHeader("X-Correlation-ID");

            loggingHelper.logInfo(requestId, "Capturing API request from execution for API: " + apiId);

            ApiRequestResponseDTO response = apiRequestService.captureRequestWithExecution(
                    requestId, apiId, executeRequest, performedBy, clientIp, userAgent, correlationId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API request captured successfully from execution",
                    response);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "capturing API request from execution", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while capturing API request from execution: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =====================================================
    // UPDATE RESPONSE ENDPOINTS
    // =====================================================

    @PutMapping("/{capturedRequestId}/response")
    @Operation(summary = "Update Request with Response", description = "Update a captured request with response details")
    public ResponseEntity<?> updateRequestWithResponse(
            @PathVariable String capturedRequestId,
            @RequestBody UpdateResponseRequestDTO responseRequest,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating request with response");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Updating captured request: " + capturedRequestId + " with response");

            ExecuteApiResponseDTO responseDTO = ExecuteApiResponseDTO.builder()
                    .responseCode(responseRequest.getStatusCode())
                    .message(responseRequest.getMessage())
                    .data(responseRequest.getData())
                    .build();

            ApiRequestResponseDTO updatedRequest = apiRequestService.updateRequestWithResponse(
                    requestId,
                    capturedRequestId,
                    responseDTO,
                    responseRequest.getStatusCode(),
                    responseRequest.getMessage(),
                    responseRequest.getExecutionDurationMs());

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Request updated with response successfully",
                    updatedRequest);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "updating request with response", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while updating request with response: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{capturedRequestId}/error")
    @Operation(summary = "Update Request with Error", description = "Update a captured request with error details")
    public ResponseEntity<?> updateRequestWithError(
            @PathVariable String capturedRequestId,
            @RequestBody UpdateErrorRequestDTO errorRequest,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating request with error");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Updating captured request: " + capturedRequestId + " with error");

            ApiRequestResponseDTO updatedRequest = apiRequestService.updateRequestWithError(
                    requestId,
                    capturedRequestId,
                    errorRequest.getStatusCode(),
                    errorRequest.getErrorMessage(),
                    errorRequest.getExecutionDurationMs());

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Request updated with error successfully",
                    updatedRequest);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "updating request with error", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while updating request with error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/batch/update-responses")
    @Operation(summary = "Batch Update Responses", description = "Batch update multiple requests with responses")
    public ResponseEntity<?> batchUpdateResponses(
            @RequestBody Map<String, ExecuteApiResponseDTO> requestIdToResponseMap,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "batch updating responses");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Batch updating " + requestIdToResponseMap.size() + " requests");

            List<ApiRequestResponseDTO> updatedRequests = apiRequestService.batchUpdateResponses(
                    requestId, requestIdToResponseMap);

            Map<String, Object> data = Map.of(
                    "updatedCount", updatedRequests.size(),
                    "requests", updatedRequests
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Batch update completed successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "batch updating responses", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while batch updating responses: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =====================================================
    // RETRIEVAL ENDPOINTS
    // =====================================================

    @GetMapping("/{capturedRequestId}")
    @Operation(summary = "Get Request by ID", description = "Get a captured request by its ID")
    public ResponseEntity<?> getRequestById(
            @PathVariable String capturedRequestId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting request by ID");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Getting request by ID: " + capturedRequestId);

            ApiRequestResponseDTO request = apiRequestService.getRequestById(requestId, capturedRequestId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Request retrieved successfully",
                    request);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting request by ID", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get Request by Correlation ID", description = "Get a captured request by correlation ID")
    public ResponseEntity<?> getRequestByCorrelationId(
            @PathVariable String correlationId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting request by correlation ID");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Getting request by correlation ID: " + correlationId);

            ApiRequestResponseDTO request = apiRequestService.getRequestByCorrelationId(requestId, correlationId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Request retrieved successfully",
                    request);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting request by correlation ID", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting request by correlation ID: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/{apiId}")
    @Operation(summary = "Get Requests by API ID", description = "Get all requests for a specific API")
    public ResponseEntity<?> getRequestsByApiId(
            @PathVariable String apiId,
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting requests by API ID");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Getting requests for API: " + apiId + " with limit: " + limit);

            List<ApiRequestResponseDTO> requests = apiRequestService.getRequestsByApiId(apiId, limit);

            Map<String, Object> data = Map.of(
                    "apiId", apiId,
                    "requests", requests,
                    "totalCount", requests.size()
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Requests retrieved successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting requests by API ID", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting requests: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/search")
    @Operation(summary = "Search Requests", description = "Search requests with advanced filtering and pagination")
    public ResponseEntity<?> searchRequests(
            @RequestBody ApiRequestFilterDTO filter,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching requests");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Searching requests with filter");

            Page<ApiRequestResponseDTO> requestsPage = apiRequestService.searchRequests(requestId, filter);

            Map<String, Object> data = Map.of(
                    "content", requestsPage.getContent(),
                    "totalElements", requestsPage.getTotalElements(),
                    "totalPages", requestsPage.getTotalPages(),
                    "currentPage", requestsPage.getNumber(),
                    "pageSize", requestsPage.getSize()
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Requests searched successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "searching requests", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while searching requests: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =====================================================
    // STATISTICS ENDPOINTS
    // =====================================================

    @GetMapping("/statistics/api/{apiId}")
    @Operation(summary = "Get Request Statistics", description = "Get request statistics for a specific API")
    public ResponseEntity<?> getRequestStatistics(
            @PathVariable String apiId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting request statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Getting statistics for API: " + apiId);

            ApiRequestStatisticsDTO statistics = apiRequestService.getRequestStatistics(apiId, fromDate, toDate);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Request statistics retrieved successfully",
                    statistics);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting request statistics", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting request statistics: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/statistics/system")
    @Operation(summary = "Get System Statistics", description = "Get overall system request statistics")
    public ResponseEntity<?> getSystemStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting system statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Getting system statistics");

            ApiRequestStatisticsDTO statistics = apiRequestService.getSystemStatistics(fromDate, toDate);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "System statistics retrieved successfully",
                    statistics);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting system statistics", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting system statistics: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/statistics/api/{apiId}/daily-breakdown")
    @Operation(summary = "Get Daily Breakdown", description = "Get daily breakdown of requests for an API")
    public ResponseEntity<?> getDailyBreakdown(
            @PathVariable String apiId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting daily breakdown");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Getting daily breakdown for API: " + apiId);

            List<ApiRequestEntity> requests = apiRequestService.getRequestsByApiIdAndDateRange(
                    apiId, fromDate, toDate);

            Map<String, Long> breakdown = requests.stream()
                    .collect(Collectors.groupingBy(
                            request -> request.getRequestTimestamp().toLocalDate().toString(),
                            Collectors.counting()
                    ));

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Daily breakdown retrieved successfully",
                    breakdown);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting daily breakdown", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting daily breakdown: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =====================================================
    // DELETE ENDPOINTS
    // =====================================================

    @DeleteMapping("/{capturedRequestId}")
    @Operation(summary = "Delete Request", description = "Delete a captured request")
    public ResponseEntity<?> deleteRequest(
            @PathVariable String capturedRequestId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting request");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Deleting request: " + capturedRequestId);

            apiRequestService.deleteRequest(requestId, capturedRequestId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Request deleted successfully",
                    null);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "deleting request", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while deleting request: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Cleanup Old Requests", description = "Delete requests older than specified date")
    public ResponseEntity<?> cleanupOldRequests(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "cleaning up old requests");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Cleaning up requests older than: " + beforeDate);

            long deletedCount = apiRequestService.deleteOldRequests(beforeDate);

            Map<String, Object> data = Map.of(
                    "deletedCount", deletedCount,
                    "beforeDate", beforeDate
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Old requests cleaned up successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "cleaning up old requests", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while cleaning up old requests: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =====================================================
    // EXPORT ENDPOINT
    // =====================================================

    @PostMapping("/export/api/{apiId}")
    @Operation(summary = "Export Requests", description = "Export requests for an API within a date range")
    public ResponseEntity<?> exportRequests(
            @PathVariable String apiId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "JSON") String format,
            @RequestBody(required = false) ApiRequestExportDTO.ExportConfig config,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting requests");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logInfo(requestId, "Exporting requests for API: " + apiId);

            List<ApiRequestEntity> requests = apiRequestService.getRequestsByApiIdAndDateRange(
                    apiId, fromDate, toDate);

            // Build export response
            ApiRequestExportDTO exportDTO = ApiRequestExportDTO.builder()
                    .exportId(UUID.randomUUID().toString())
                    .exportTimestamp(LocalDateTime.now())
                    .exportedBy(performedBy)
                    .format(format)
                    .config(config != null ? config : ApiRequestExportDTO.ExportConfig.builder().build())
                    .recordCount(requests.size())
                    .build();

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Requests exported successfully",
                    exportDTO);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "exporting requests", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while exporting requests: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =====================================================
    // REQUEST DTOs FOR UPDATES
    // =====================================================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateResponseRequest {
        private Integer statusCode;
        private String message;
        private Object data;
        private Long executionDurationMs;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateErrorRequest {
        private Integer statusCode;
        private String errorMessage;
        private Long executionDurationMs;
    }
}