package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.apiSecurity.*;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.APISecurityService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/plx/api/security")
@RequiredArgsConstructor
@Tag(name = "API SECURITY", description = "Endpoints for API security management")
public class APISecurityController {

    private final APISecurityService apiSecurityService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET RATE LIMIT RULES
    // ============================================================
    @GetMapping("/rate-limit-rules")
    @Operation(summary = "Get rate limit rules", description = "Retrieve all rate limit rules for API security")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rate limit rules retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getRateLimitRules(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting rate limit rules");
        if (authValidation != null) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Authorization failed for getting rate limit rules");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting rate limit rules for user: " + performedBy);

            RateLimitRulesResponse rules = apiSecurityService.getRateLimitRules(requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Rate limit rules retrieved successfully");
            response.put("data", rules);
            response.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Rate limit rules retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting rate limit rules: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting rate limit rules: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET IP WHITELIST
    // ============================================================
    @GetMapping("/ip-whitelist")
    @Operation(summary = "Get IP whitelist", description = "Retrieve IP whitelist entries")
    public ResponseEntity<?> getIPWhitelist(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting IP whitelist");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting IP whitelist for user: " + performedBy);

            IPWhitelistResponse whitelist = apiSecurityService.getIPWhitelist(requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "IP whitelist retrieved successfully");
            response.put("data", whitelist);
            response.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", IP whitelist retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting IP whitelist: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting IP whitelist: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET LOAD BALANCERS
    // ============================================================
    @GetMapping("/load-balancers")
    @Operation(summary = "Get load balancers", description = "Retrieve load balancer configurations")
    public ResponseEntity<?> getLoadBalancers(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting load balancers");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting load balancers for user: " + performedBy);

            LoadBalancersResponse loadBalancers = apiSecurityService.getLoadBalancers(requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Load balancers retrieved successfully");
            response.put("data", loadBalancers);
            response.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Load balancers retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting load balancers: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting load balancers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. GET SECURITY EVENTS
    // ============================================================
    @GetMapping("/security-events")
    @Operation(summary = "Get security events", description = "Retrieve recent security events")
    public ResponseEntity<?> getSecurityEvents(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting security events");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security events for user: " + performedBy);

            SecurityEventsResponse events = apiSecurityService.getSecurityEvents(requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Security events retrieved successfully");
            response.put("data", events);
            response.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security events retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security events: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting security events: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. GET SECURITY SUMMARY
    // ============================================================
    @GetMapping("/security-summary")
    @Operation(summary = "Get security summary", description = "Retrieve API security summary and metrics")
    public ResponseEntity<?> getSecuritySummary(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting security summary");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security summary for user: " + performedBy);

            SecuritySummaryResponse summary = apiSecurityService.getSecuritySummary(requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Security summary retrieved successfully");
            response.put("data", summary);
            response.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security summary retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security summary: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting security summary: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. ADD RATE LIMIT RULE
    // ============================================================
    @PostMapping("/rate-limit-rules")
    @Operation(summary = "Add rate limit rule", description = "Add a new rate limit rule")
    public ResponseEntity<?> addRateLimitRule(
            @Valid @RequestBody AddRuleRequest addRuleRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "adding rate limit rule");
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
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Adding rate limit rule: " + addRuleRequest.getName());

            AddRuleResponse response = apiSecurityService.addRateLimitRule(
                    requestId, performedBy, addRuleRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 201);
            apiResponse.put("message", "Rate limit rule added successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Rate limit rule added successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error adding rate limit rule: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while adding rate limit rule: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. ADD IP WHITELIST ENTRY
    // ============================================================
    @PostMapping("/ip-whitelist")
    @Operation(summary = "Add IP whitelist entry", description = "Add a new IP whitelist entry")
    public ResponseEntity<?> addIPWhitelistEntry(
            @Valid @RequestBody AddIPEntryRequest addIPEntryRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "adding IP whitelist entry");
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
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Adding IP whitelist entry: " + addIPEntryRequest.getName());

            AddIPEntryResponse response = apiSecurityService.addIPWhitelistEntry(
                    requestId, performedBy, addIPEntryRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 201);
            apiResponse.put("message", "IP whitelist entry added successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", IP whitelist entry added successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error adding IP whitelist entry: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while adding IP whitelist entry: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. ADD LOAD BALANCER
    // ============================================================
    @PostMapping("/load-balancers")
    @Operation(summary = "Add load balancer", description = "Add a new load balancer configuration")
    public ResponseEntity<?> addLoadBalancer(
            @Valid @RequestBody AddLoadBalancerRequest addLoadBalancerRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "adding load balancer");
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
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Adding load balancer: " + addLoadBalancerRequest.getName());

            AddLoadBalancerResponse response = apiSecurityService.addLoadBalancer(
                    requestId, performedBy, addLoadBalancerRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 201);
            apiResponse.put("message", "Load balancer added successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Load balancer added successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error adding load balancer: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while adding load balancer: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. UPDATE RULE STATUS
    // ============================================================
    @PutMapping("/rate-limit-rules/{ruleId}/status")
    @Operation(summary = "Update rule status", description = "Update status of a rate limit rule")
    public ResponseEntity<?> updateRuleStatus(
            @PathVariable String ruleId,
            @Valid @RequestBody UpdateRuleStatusRequest updateRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating rule status");
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
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Updating rule status: " + ruleId + " to " + updateRequest.getStatus());

            UpdateRuleStatusResponse response = apiSecurityService.updateRuleStatus(
                    requestId, performedBy, updateRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Rule status updated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Rule status updated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error updating rule status: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating rule status: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. DELETE RULE
    // ============================================================
    @DeleteMapping("/rate-limit-rules/{ruleId}")
    @Operation(summary = "Delete rule", description = "Delete a rate limit rule")
    public ResponseEntity<?> deleteRule(
            @PathVariable String ruleId,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting rule");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Deleting rule: " + ruleId);

            DeleteRuleResponse response = apiSecurityService.deleteRule(requestId, performedBy, ruleId);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Rule deleted successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Rule deleted successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error deleting rule: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while deleting rule: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. GENERATE SECURITY REPORT
    // ============================================================
    @PostMapping("/reports/generate")
    @Operation(summary = "Generate security report", description = "Generate comprehensive security report")
    public ResponseEntity<?> generateSecurityReport(
            @Valid @RequestBody GenerateReportRequest reportRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating security report");
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
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Generating security report");

            SecurityReportResponse response = apiSecurityService.generateSecurityReport(
                    requestId, performedBy, reportRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Security report generated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security report generated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error generating security report: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while generating security report: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. RUN SECURITY SCAN
    // ============================================================
    @PostMapping("/scan")
    @Operation(summary = "Run security scan", description = "Run a comprehensive security scan")
    public ResponseEntity<?> runSecurityScan(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "running security scan");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Running security scan for user: " + performedBy);

            SecurityScanResponse response = apiSecurityService.runSecurityScan(requestId, performedBy);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Security scan completed successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security scan completed successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error running security scan: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while running security scan: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. GET SECURITY CONFIGURATION
    // ============================================================
    @GetMapping("/configuration")
    @Operation(summary = "Get security configuration", description = "Retrieve current security configuration")
    public ResponseEntity<?> getSecurityConfiguration(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting security configuration");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security configuration for user: " + performedBy);

            SecurityConfigResponse response = apiSecurityService.getSecurityConfiguration(requestId, performedBy);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Security configuration retrieved successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security configuration retrieved successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security configuration: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting security configuration: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 14. UPDATE SECURITY CONFIGURATION
    // ============================================================
    @PutMapping("/configuration")
    @Operation(summary = "Update security configuration", description = "Update security configuration settings")
    public ResponseEntity<?> updateSecurityConfiguration(
            @Valid @RequestBody UpdateConfigRequest configRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating security configuration");
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
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Updating security configuration");

            UpdateConfigResponse response = apiSecurityService.updateSecurityConfiguration(
                    requestId, performedBy, configRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Security configuration updated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security configuration updated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error updating security configuration: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating security configuration: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 15. GET SECURITY ALERTS
    // ============================================================
    @GetMapping("/alerts")
    @Operation(summary = "Get security alerts", description = "Retrieve security alerts and notifications")
    public ResponseEntity<?> getSecurityAlerts(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting security alerts");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security alerts for user: " + performedBy);

            SecurityAlertsResponse response = apiSecurityService.getSecurityAlerts(requestId, performedBy);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Security alerts retrieved successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security alerts retrieved successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security alerts: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting security alerts: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 16. EXPORT SECURITY DATA
    // ============================================================
    @PostMapping("/export")
    @Operation(summary = "Export security data", description = "Export security data in specified format")
    public ResponseEntity<?> exportSecurityData(
            @Valid @RequestBody ExportSecurityRequest exportRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting security data");
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
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Exporting security data in format: " + exportRequest.getFormat());

            ExportSecurityResponse response = apiSecurityService.exportSecurityData(
                    requestId, performedBy, exportRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Security data exported successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Security data exported successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error exporting security data: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while exporting security data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}