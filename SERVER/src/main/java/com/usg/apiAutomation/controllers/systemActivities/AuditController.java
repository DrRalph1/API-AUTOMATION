package com.usg.apiAutomation.controllers.systemActivities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.systemActivities.audit.AuditLogDTO;
import com.usg.apiAutomation.dtos.systemActivities.audit.AuditLogSearchRequest;
import com.usg.apiAutomation.helpers.ApiKeyNSecretHelper;
import com.usg.apiAutomation.helpers.ClientIpHelper;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.systemActivities.AuditService;
import com.usg.apiAutomation.utils.JwtUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/audit")
@RequiredArgsConstructor
//@Tag(name = "AUDIT LOGS", description = "Endpoints for logging and retrieving userManagement actions")
@Tag(name = "SYSTEM ACTIVITIES", description = "System-level endpoints")
public class AuditController {

    private final AuditService auditService;
    private final LoggerUtil loggerUtil;
    private final ApiKeyNSecretHelper apiKeyNSecretHelper;
    private final ClientIpHelper clientIpHelper;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final JwtHelper jwtHelper;

    @PostMapping
    @Operation(summary = "Create a new audit log", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Audit log created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> logAction(
            @Valid @RequestBody AuditLogDTO dto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "creating an audit log");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Authorization failed for creating audit log: " + dto.getAction());
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        try {
            // Log the incoming request
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Creating audit log for action: " + dto.getAction());

            // Validate request body
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", Validation errors: " + validationErrors);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors");
                errorResponse.put("errors", validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId +
                            ", Creating audit log: " + dto.getAction() +
                            ", Requested by: " + performedBy);

            // Call service method
            AuditLogDTO createdLog = auditService.logAction(dto, requestId, req, performedBy);

            // Create success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("responseCode", 201);
            successResponse.put("message", "Audit log created successfully");
            successResponse.put("data", createdLog);
            successResponse.put("requestId", requestId);

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Audit log created successfully. Audit ID: " + createdLog.getAuditId());

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (Exception e) {
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error creating audit log: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while creating audit log: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    @Operation(summary = "Get all audit logs (paginated + sortable + unique filters)", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No audit logs found"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllLogs(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all audit logs");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Authorization failed for getting all audit logs");
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId +
                            ", Getting all audit logs with pagination - Page: " +
                            pageable.getPageNumber() + ", Size: " + pageable.getPageSize() + ", Sort: " + pageable.getSort() +
                            ", Requested by: " + performedBy);

            // Call service method - now returns Map with unique filter values
            Map<String, Object> result = auditService.getAllLogs(pageable, requestId, req, performedBy);

            // Extract data from result map
            List<AuditLogDTO> logs = (List<AuditLogDTO>) result.get("logs");
            Map<String, Object> pagination = (Map<String, Object>) result.get("pagination");
            List<String> uniqueActions = (List<String>) result.get("uniqueActions");
            List<String> uniqueUsers = (List<String>) result.get("uniqueUsers");
            List<String> uniqueOperations = (List<String>) result.get("uniqueOperations");

            // Check if no content
            if (logs.isEmpty()) {
                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", No audit logs found");

                Map<String, Object> noContentResponse = new HashMap<>();
                noContentResponse.put("responseCode", 204);
                noContentResponse.put("message", "No audit logs found");
                noContentResponse.put("uniqueActions", uniqueActions);
                noContentResponse.put("uniqueUsers", uniqueUsers);
                noContentResponse.put("uniqueOperations", uniqueOperations);
                noContentResponse.put("requestId", requestId);
                return ResponseEntity.ok(noContentResponse);
            }

            // Create success response with pagination metadata and unique filters
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("responseCode", 200);
            successResponse.put("message", "Audit logs retrieved successfully");
            successResponse.put("data", logs);
            successResponse.put("pagination", pagination);
            successResponse.put("uniqueActions", uniqueActions);
            successResponse.put("uniqueUsers", uniqueUsers);
            successResponse.put("uniqueOperations", uniqueOperations);
            successResponse.put("requestId", requestId);

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Get all audit logs completed. Total elements: " +
                            pagination.get("total_elements") + ", Total pages: " + pagination.get("total_pages") +
                            ", Unique actions: " + uniqueActions.size() +
                            ", Unique users: " + uniqueUsers.size() +
                            ", Unique operations: " + uniqueOperations.size());

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error getting audit logs: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting audit logs: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/search")
    @Operation(summary = "Search audit logs with filters", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No audit logs found"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchLogs(
            @Valid @RequestBody AuditLogSearchRequest searchRequest,
            BindingResult bindingResult,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching audit logs");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Authorization failed for searching audit logs");
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        try {
            // Validate request body
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", Validation errors in search request: " + validationErrors);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors in search request");
                errorResponse.put("errors", validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate date range
            if (searchRequest.getStartDate() != null && searchRequest.getEndDate() != null) {
                if (searchRequest.getStartDate().isAfter(searchRequest.getEndDate())) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("responseCode", 400);
                    errorResponse.put("message", "Start date cannot be after end date");
                    errorResponse.put("requestId", requestId);
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            // Log the search request
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId +
                            ", Searching audit logs with filters: " + objectMapper.writeValueAsString(searchRequest) +
                            ", Page: " + pageable.getPageNumber() + ", Size: " + pageable.getPageSize() +
                            ", Requested by: " + performedBy);

            // Call service method
            Page<AuditLogDTO> logsPage = auditService.searchLogs(searchRequest, pageable, requestId, req, performedBy);

            // Check if no content
            if (logsPage.isEmpty()) {
                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", No audit logs found for the given search criteria");

                Map<String, Object> noContentResponse = new HashMap<>();
                noContentResponse.put("responseCode", 204);
                noContentResponse.put("message", "No audit logs found for the given search criteria");
                noContentResponse.put("searchCriteria", searchRequest);
                noContentResponse.put("requestId", requestId);
                return ResponseEntity.ok(noContentResponse);
            }

            // Create success response with pagination metadata
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("responseCode", 200);
            successResponse.put("message", "Audit logs retrieved successfully");
            successResponse.put("data", logsPage.getContent());
            successResponse.put("searchCriteria", searchRequest);
            successResponse.put("pagination", Map.of(
                    "page_number", logsPage.getNumber(),
                    "page_size", logsPage.getSize(),
                    "total_elements", logsPage.getTotalElements(),
                    "total_pages", logsPage.getTotalPages(),
                    "is_first", logsPage.isFirst(),
                    "is_last", logsPage.isLast()
            ));
            successResponse.put("requestId", requestId);

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Search completed. Found " + logsPage.getTotalElements() +
                            " audit logs matching the criteria");

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error searching audit logs: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching audit logs: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/search")
    @Operation(summary = "Search audit logs using query parameters", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "userId", description = "Filter by userManagement ID", in = ParameterIn.QUERY),
            @Parameter(name = "action", description = "Filter by action", in = ParameterIn.QUERY),
            @Parameter(name = "operation", description = "Filter by operation", in = ParameterIn.QUERY),
            @Parameter(name = "details", description = "Search in details", in = ParameterIn.QUERY),
            @Parameter(name = "startDate", description = "Start date (yyyy-MM-dd'T'HH:mm:ss)", in = ParameterIn.QUERY),
            @Parameter(name = "endDate", description = "End date (yyyy-MM-dd'T'HH:mm:ss)", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No audit logs found"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchLogsByQuery(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest req) {

        // Create search request from query parameters
        AuditLogSearchRequest searchRequest = new AuditLogSearchRequest();
        searchRequest.setUserId(userId);
        searchRequest.setAction(action);
        searchRequest.setOperation(operation);
        searchRequest.setDetails(details);
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);

        // Create a DataBinder to get BindingResult for validation
        DataBinder dataBinder = new DataBinder(searchRequest);
        BindingResult bindingResult = dataBinder.getBindingResult();

        // Manually validate the request object
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<AuditLogSearchRequest>> violations = validator.validate(searchRequest);

        for (ConstraintViolation<AuditLogSearchRequest> violation : violations) {
            bindingResult.addError(new FieldError(
                    "auditLogSearchRequest",
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
            ));
        }

        // Use the existing search method with BindingResult
        return searchLogs(searchRequest, bindingResult, pageable, req);
    }

}