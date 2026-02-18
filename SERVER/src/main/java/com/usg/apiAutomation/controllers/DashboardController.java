package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.dashboard.*;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.DashboardService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "DASHBOARD", description = "Endpoints for dashboard statistics and monitoring")
public class DashboardController {

    private final DashboardService dashboardService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET DASHBOARD STATISTICS
    // ============================================================
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getDashboardStats(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting dashboard statistics");
        if (authValidation != null) {
            loggerUtil.log("dashboard", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting dashboard statistics");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("dashboard", "RequestEntity ID: " + requestId +
                    ", Getting dashboard statistics for user: " + performedBy);

            DashboardStatsResponse stats = dashboardService.getDashboardStats(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard statistics retrieved successfully");
            response.put("data", stats);
            response.put("requestId", requestId);

            loggerUtil.log("dashboard", "RequestEntity ID: " + requestId +
                    ", Dashboard statistics retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("dashboard", "RequestEntity ID: " + requestId +
                    ", Error getting dashboard statistics: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET DASHBOARD CONNECTIONS
    // ============================================================
    @GetMapping("/connections")
    @Operation(summary = "Get dashboard connections", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardConnections(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting dashboard connections");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardConnectionsResponse connections = dashboardService.getDashboardConnections(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard connections retrieved successfully");
            response.put("data", connections);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard connections: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET DASHBOARD APIS
    // ============================================================
    @GetMapping("/apis")
    @Operation(summary = "Get dashboard APIs", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardApis(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting dashboard APIs");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardApisResponse apis = dashboardService.getDashboardApis(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard APIs retrieved successfully");
            response.put("data", apis);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard APIs: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. GET DASHBOARD ACTIVITIES
    // ============================================================
    @GetMapping("/activities")
    @Operation(summary = "Get dashboard activities", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "page", description = "Page number (default: 1)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size (default: 10)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getDashboardActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting dashboard activities");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardActivitiesResponse activities = dashboardService.getDashboardActivities(
                    requestId, req, performedBy, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard activities retrieved successfully");
            response.put("data", activities);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard activities: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. GET DASHBOARD SCHEMA STATISTICS
    // ============================================================
    @GetMapping("/schema-stats")
    @Operation(summary = "Get dashboard schema statistics", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardSchemaStats(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting dashboard schema statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardSchemaStatsResponse schemaStats = dashboardService.getDashboardSchemaStats(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard schema statistics retrieved successfully");
            response.put("data", schemaStats);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard schema statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. GET CODE GENERATION STATISTICS
    // ============================================================
    @GetMapping("/code-generation-stats")
    @Operation(summary = "Get code generation statistics", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getCodeGenerationStats(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting code generation statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> codeStats = dashboardService.getCodeGenerationStats(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Code generation statistics retrieved successfully");
            response.put("data", codeStats);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting code generation statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. GET COMPREHENSIVE DASHBOARD DATA
    // ============================================================
    @GetMapping("/comprehensive")
    @Operation(summary = "Get comprehensive dashboard data", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getComprehensiveDashboard(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting comprehensive dashboard data");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);

            // Fetch all dashboard data in parallel/synchronously
            DashboardStatsResponse stats = dashboardService.getDashboardStats(requestId, req, performedBy);
            DashboardConnectionsResponse connections = dashboardService.getDashboardConnections(requestId, req, performedBy);
            DashboardApisResponse apis = dashboardService.getDashboardApis(requestId, req, performedBy);
            DashboardActivitiesResponse activities = dashboardService.getDashboardActivities(requestId, req, performedBy, 1, 6);
            DashboardSchemaStatsResponse schemaStats = dashboardService.getDashboardSchemaStats(requestId, req, performedBy);
            Map<String, Object> systemHealth = dashboardService.getDashboardSystemHealth(requestId, req, performedBy);
            Map<String, Object> codeStats = dashboardService.getCodeGenerationStats(requestId, req, performedBy);

            Map<String, Object> comprehensiveData = new HashMap<>();
            comprehensiveData.put("stats", stats);
            comprehensiveData.put("connections", connections);
            comprehensiveData.put("apis", apis);
            comprehensiveData.put("recentActivities", activities);
            comprehensiveData.put("schemaStats", schemaStats);
            comprehensiveData.put("systemHealth", systemHealth);
            comprehensiveData.put("codeGenerationStats", codeStats);
            comprehensiveData.put("lastUpdated", java.time.LocalDateTime.now().toString());
            comprehensiveData.put("generatedFor", performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Comprehensive dashboard data retrieved successfully");
            response.put("data", comprehensiveData);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting comprehensive dashboard data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. SEARCH DASHBOARD ACTIVITIES
    // ============================================================
    @PostMapping("/activities/search")
    @Operation(summary = "Search dashboard activities with filters", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> searchDashboardActivities(
            @Valid @RequestBody DashboardActivitySearchDTO searchDTO,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching dashboard activities");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors: " + validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);

            // In a real implementation, this would call a service method with search filters
            // For now, returning filtered activities based on simple criteria
            DashboardActivitiesResponse activities = dashboardService.getDashboardActivities(
                    requestId, req, performedBy, searchDTO.getPage(), searchDTO.getSize());

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard activities search completed successfully");
            response.put("data", activities);
            response.put("filters", searchDTO);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching dashboard activities: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}