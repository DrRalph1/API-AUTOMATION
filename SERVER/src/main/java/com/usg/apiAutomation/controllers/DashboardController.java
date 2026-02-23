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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            loggerUtil.log("dashboard", "Request ID: " + requestId +
                    ", Authorization failed for getting dashboard statistics");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("dashboard", "Request ID: " + requestId +
                    ", Getting dashboard statistics for user: " + performedBy);

            DashboardStatsResponseDTO stats = dashboardService.getDashboardStats(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard statistics retrieved successfully");
            response.put("data", stats);
            response.put("requestId", requestId);

            loggerUtil.log("dashboard", "Request ID: " + requestId +
                    ", Dashboard statistics retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("dashboard", "Request ID: " + requestId +
                    ", Error getting dashboard statistics: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET DASHBOARD COLLECTIONS
    // ============================================================
    @GetMapping("/collections")
    @Operation(summary = "Get dashboard collections overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardCollections(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting dashboard collections");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardCollectionsResponseDTO collections = dashboardService.getDashboardCollections(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard collections retrieved successfully");
            response.put("data", collections);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard collections: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET DASHBOARD ENDPOINTS
    // ============================================================
    @GetMapping("/endpoints")
    @Operation(summary = "Get dashboard endpoints overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardEndpoints(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting dashboard endpoints");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardEndpointsResponseDTO endpoints = dashboardService.getDashboardEndpoints(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard endpoints retrieved successfully");
            response.put("data", endpoints);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dashboard endpoints: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. GET RATE LIMIT RULES
    // ============================================================
    @GetMapping("/rate-limit-rules")
    @Operation(summary = "Get rate limit rules overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardRateLimitRules(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting rate limit rules");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardRateLimitRulesResponseDTO rules = dashboardService.getDashboardRateLimitRules(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Rate limit rules retrieved successfully");
            response.put("data", rules);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting rate limit rules: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. GET IP WHITELIST
    // ============================================================
    @GetMapping("/ip-whitelist")
    @Operation(summary = "Get IP whitelist overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardIpWhitelist(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting IP whitelist");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardIpWhitelistResponseDTO whitelist = dashboardService.getDashboardIpWhitelist(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "IP whitelist retrieved successfully");
            response.put("data", whitelist);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting IP whitelist: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. GET LOAD BALANCERS
    // ============================================================
    @GetMapping("/load-balancers")
    @Operation(summary = "Get load balancers overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardLoadBalancers(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting load balancers");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardLoadBalancersResponseDTO loadBalancers = dashboardService.getDashboardLoadBalancers(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Load balancers retrieved successfully");
            response.put("data", loadBalancers);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting load balancers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. GET SECURITY EVENTS
    // ============================================================
    @GetMapping("/security-events")
    @Operation(summary = "Get security events overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardSecurityEvents(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting security events");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardSecurityEventsResponseDTO events = dashboardService.getDashboardSecurityEvents(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Security events retrieved successfully");
            response.put("data", events);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting security events: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. GET SECURITY ALERTS
    // ============================================================
    @GetMapping("/security-alerts")
    @Operation(summary = "Get security alerts overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardSecurityAlerts(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting security alerts");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardSecurityAlertsResponseDTO alerts = dashboardService.getDashboardSecurityAlerts(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Security alerts retrieved successfully");
            response.put("data", alerts);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting security alerts: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. GET SECURITY SUMMARY
    // ============================================================
    @GetMapping("/security-summary")
    @Operation(summary = "Get security summary", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardSecuritySummary(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting security summary");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardSecuritySummaryResponseDTO summary = dashboardService.getDashboardSecuritySummary(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Security summary retrieved successfully");
            response.put("data", summary);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting security summary: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. GET CODE LANGUAGES
    // ============================================================
    @GetMapping("/languages")
    @Operation(summary = "Get supported programming languages", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardLanguages(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting languages");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardLanguagesResponseDTO languages = dashboardService.getDashboardLanguages(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Languages retrieved successfully");
            response.put("data", languages);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting languages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. GET CODE IMPLEMENTATIONS
    // ============================================================
    @GetMapping("/implementations")
    @Operation(summary = "Get code implementations", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "page", description = "Page number (default: 1)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size (default: 10)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getDashboardImplementations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting implementations");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardImplementationsResponseDTO implementations = dashboardService.getDashboardImplementations(
                    requestId, req, performedBy, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Implementations retrieved successfully");
            response.put("data", implementations);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting implementations: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. GET CODE GENERATION SUMMARY
    // ============================================================
    @GetMapping("/code-generation-summary")
    @Operation(summary = "Get code generation summary", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardCodeGenerationSummary(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting code generation summary");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardCodeGenerationSummaryResponseDTO summary = dashboardService.getDashboardCodeGenerationSummary(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Code generation summary retrieved successfully");
            response.put("data", summary);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting code generation summary: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. GET DOCUMENTATION OVERVIEW
    // ============================================================
    @GetMapping("/documentation")
    @Operation(summary = "Get documentation overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardDocumentation(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting documentation");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardDocumentationResponseDTO documentation = dashboardService.getDashboardDocumentation(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Documentation retrieved successfully");
            response.put("data", documentation);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting documentation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 15. GET ENVIRONMENTS
    // ============================================================
    @GetMapping("/environments")
    @Operation(summary = "Get environments overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardEnvironments(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting environments");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardEnvironmentsResponseDTO environments = dashboardService.getDashboardEnvironments(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Environments retrieved successfully");
            response.put("data", environments);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting environments: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 17. GET USERS OVERVIEW
    // ============================================================
    @GetMapping("/users")
    @Operation(summary = "Get users overview", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "page", description = "Page number (default: 1)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size (default: 10)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getDashboardUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting users");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardUsersResponseDTO users = dashboardService.getDashboardUsers(requestId, req, performedBy, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Users retrieved successfully");
            response.put("data", users);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting users: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 18. GET USER ACTIVITIES
    // ============================================================
    @GetMapping("/user-activities")
    @Operation(summary = "Get user activities", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "limit", description = "Number of activities to return (default: 20)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getDashboardUserActivities(
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting user activities");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardUserActivitiesResponseDTO activities = dashboardService.getDashboardUserActivities(
                    requestId, req, performedBy, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "User activities retrieved successfully");
            response.put("data", activities);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting user activities: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 19. GET COMPREHENSIVE DASHBOARD DATA
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
            ComprehensiveDashboardResponseDTO comprehensiveData = dashboardService.getComprehensiveDashboard(requestId, req, performedBy);

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
    // 20. SEARCH DASHBOARD
    // ============================================================
    @GetMapping("/search")
    @Operation(summary = "Search across dashboard", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "query", description = "Search query", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "type", description = "Type to search (all, collections, endpoints, users)", in = ParameterIn.QUERY),
            @Parameter(name = "limit", description = "Maximum results (default: 20)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> searchDashboard(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching dashboard");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardSearchResponseDTO searchResults = dashboardService.searchDashboard(
                    requestId, req, performedBy, query, type, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dashboard search completed successfully");
            response.put("data", searchResults);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching dashboard: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}