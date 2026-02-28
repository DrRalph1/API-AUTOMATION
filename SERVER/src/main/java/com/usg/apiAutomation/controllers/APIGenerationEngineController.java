package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.APIGenerationEngineService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/generated-apis")
@RequiredArgsConstructor
@Tag(name = "API GENERATION ENGINE", description = "Endpoints for generating and managing APIs")
public class APIGenerationEngineController {

    private final APIGenerationEngineService apiGenerationEngineService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    @PostMapping("/generate")
    @Operation(summary = "Generate API", description = "Generate a new API based on configuration")
    public ResponseEntity<?> generateApi(
            @Valid @RequestBody GenerateApiRequestDTO request,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating API");
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
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Generating API: " + request.getApiName() + " by: " + performedBy);

            GeneratedApiResponseDTO response = apiGenerationEngineService.generateApi(requestId, performedBy, request);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "API generated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + response.getId());

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error generating API: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while generating API: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{apiId}/execute")
    @Operation(summary = "Execute API", description = "Execute a generated API")
    public ResponseEntity<?> executeApi(
            @PathVariable String apiId,
            @RequestBody ExecuteApiRequestDTO executeRequest,
            HttpServletRequest req) {

        String requestId = executeRequest.getRequestId() != null ?
                executeRequest.getRequestId() : UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "executing API");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            String clientIp = req.getRemoteAddr();
            String userAgent = req.getHeader("User-Agent");

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Executing API: " + apiId + " by: " + performedBy);

            ExecuteApiResponseDTO response = apiGenerationEngineService.executeApi(
                    requestId, performedBy, apiId, executeRequest, clientIp, userAgent);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", response.getStatusCode());
            apiResponse.put("message", response.getMessage());
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            return ResponseEntity.status(response.getStatusCode()).body(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error executing API: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while executing API: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{apiId}/test")
    @Operation(summary = "Test API", description = "Test a generated API with sample data")
    public ResponseEntity<?> testApi(
            @PathVariable String apiId,
            @Valid @RequestBody ApiTestRequestDTO testRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "testing API");
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
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Testing API: " + apiId + " with test: " + testRequest.getTestName());

            ApiTestResultDTO result = apiGenerationEngineService.testApi(requestId, performedBy, apiId, testRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "API test completed");
            apiResponse.put("data", result);
            apiResponse.put("requestId", requestId);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error testing API: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while testing API: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{apiId}")
    @Operation(summary = "Get API details", description = "Get details of a generated API")
    public ResponseEntity<?> getApiDetails(
            @PathVariable String apiId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting API details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Getting API details for: " + apiId);

            GeneratedApiResponseDTO details = apiGenerationEngineService.getApiDetails(requestId, apiId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "API details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting API details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{apiId}/analytics")
    @Operation(summary = "Get API analytics", description = "Get analytics for a generated API")
    public ResponseEntity<?> getApiAnalytics(
            @PathVariable String apiId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting API analytics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Getting analytics for API: " + apiId);

            ApiAnalyticsDTO analytics = apiGenerationEngineService.getApiAnalytics(
                    requestId, apiId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "API analytics retrieved successfully");
            response.put("data", analytics);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API analytics: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting API analytics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}