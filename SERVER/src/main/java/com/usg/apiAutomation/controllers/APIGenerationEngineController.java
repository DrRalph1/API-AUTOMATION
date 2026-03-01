package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.apiGenerationEngine.APIGenerationEngineService;
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
import java.util.List;
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

    @GetMapping("/{apiId}/code")
    @Operation(summary = "Generate API code", description = "Generate code for an API (PL/SQL, OpenAPI, Postman)")
    public ResponseEntity<?> generateApiCode(
            @PathVariable String apiId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating API code");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Generating code for API: " + apiId);

            // First get the API details
            GeneratedApiResponseDTO apiDetails = apiGenerationEngineService.getApiDetails(requestId, apiId);

            // Generate code
            Map<String, String> generatedFiles = apiGenerationEngineService.generateApiCode(
                    apiGenerationEngineService.getApiEntity(apiId));

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "API code generated successfully");
            response.put("data", Map.of(
                    "apiDetails", apiDetails,
                    "generatedFiles", generatedFiles
            ));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error generating API code: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while generating API code: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{apiId}/code/{language}")
    @Operation(summary = "Get code example", description = "Get code example for a specific language")
    public ResponseEntity<?> getCodeExample(
            @PathVariable String apiId,
            @PathVariable String language,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting code example");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Getting " + language + " code example for API: " + apiId);

            GeneratedApiResponseDTO apiDetails = apiGenerationEngineService.getApiDetails(requestId, apiId);

            // Generate code for specific language
            Map<String, String> generatedFiles = apiGenerationEngineService.generateApiCode(
                    apiGenerationEngineService.getApiEntity(apiId));

            String codeExample = generatedFiles.getOrDefault(language.toLowerCase(),
                    "Code example not available for " + language);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Code example retrieved successfully");
            response.put("data", Map.of(
                    "apiId", apiId,
                    "language", language,
                    "code", codeExample
            ));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting code example: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting code example: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{apiId}/logs")
    @Operation(summary = "Get execution logs", description = "Get execution logs for an API")
    public ResponseEntity<?> getExecutionLogs(
            @PathVariable String apiId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false, defaultValue = "100") int limit,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting execution logs");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Getting execution logs for API: " + apiId);

            // This would need to be implemented in the service
            List<ApiExecutionLogDTO> logs = apiGenerationEngineService.getExecutionLogs(
                    apiId, fromDate, toDate, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Execution logs retrieved successfully");
            response.put("data", Map.of(
                    "apiId", apiId,
                    "logs", logs,
                    "totalCount", logs.size()
            ));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting execution logs: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting execution logs: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{apiId}/tests")
    @Operation(summary = "Get test results", description = "Get test results for an API")
    public ResponseEntity<?> getTestResults(
            @PathVariable String apiId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting test results");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Getting test results for API: " + apiId);

            // This would need to be implemented in the service
            List<ApiTestResultDTO> testResults = apiGenerationEngineService.getTestResults(apiId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Test results retrieved successfully");
            response.put("data", Map.of(
                    "apiId", apiId,
                    "tests", testResults,
                    "totalCount", testResults.size()
            ));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting test results: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting test results: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{apiId}/status")
    @Operation(summary = "Update API status", description = "Update the status of an API (DRAFT, ACTIVE, DEPRECATED)")
    public ResponseEntity<?> updateApiStatus(
            @PathVariable String apiId,
            @RequestBody Map<String, String> statusRequest,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating API status");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            String newStatus = statusRequest.get("status");

            if (newStatus == null || newStatus.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Status is required");
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Updating status for API: " + apiId + " to: " + newStatus);

            // This would need to be implemented in the service
            GeneratedApiResponseDTO updatedApi = apiGenerationEngineService.updateApiStatus(
                    apiId, newStatus, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "API status updated successfully");
            response.put("data", updatedApi);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error updating API status: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating API status: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/validate/source-object")
    @Operation(summary = "Validate source object", description = "Validate an Oracle source object before API generation")
    public ResponseEntity<?> validateSourceObject(
            @RequestParam String objectName,
            @RequestParam String objectType,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating source object");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Validating source object: " + objectName);

            ApiSourceObjectDTO sourceObject = ApiSourceObjectDTO.builder()
                    .objectName(objectName)
                    .objectType(objectType)
                    .owner(owner)
                    .build();

            // Call validation method
            Map<String, Object> validationResult = apiGenerationEngineService.validateSourceObject(sourceObject);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Source object validated successfully");
            response.put("data", validationResult);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error validating source object: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while validating source object: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{apiId}/related-components")
    @Operation(summary = "Get related components", description = "Get related components (Code Base, Collections, Documentation)")
    public ResponseEntity<?> getRelatedComponents(
            @PathVariable String apiId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting related components");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Getting related components for API: " + apiId);

            GeneratedApiResponseDTO apiDetails = apiGenerationEngineService.getApiDetails(requestId, apiId);

            Map<String, Object> relatedComponents = new HashMap<>();

            if (apiDetails.getMetadata() != null) {
                Map<String, Object> metadata = apiDetails.getMetadata();
                relatedComponents.put("codeBaseRequestId", metadata.get("codeBaseRequestId"));
                relatedComponents.put("collectionsCollectionId", metadata.get("collectionsCollectionId"));
                relatedComponents.put("documentationCollectionId", metadata.get("documentationCollectionId"));
                relatedComponents.put("urls", metadata.get("urls"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Related components retrieved successfully");
            response.put("data", relatedComponents);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting related components: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting related components: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PutMapping("/{apiId}")
    @Operation(summary = "Update API", description = "Update an existing generated API")
    public ResponseEntity<?> updateApi(
            @PathVariable String apiId,
            @Valid @RequestBody GenerateApiRequestDTO request,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating API");
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
                    ", Updating API: " + apiId + " by: " + performedBy);

            GeneratedApiResponseDTO response = apiGenerationEngineService.updateApi(requestId, apiId, performedBy, request);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "API updated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error updating API: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating API: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PatchMapping("/{apiId}")
    @Operation(summary = "Partially update API", description = "Partially update specific fields of an API")
    public ResponseEntity<?> partialUpdateApi(
            @PathVariable String apiId,
            @RequestBody Map<String, Object> updates,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "partially updating API");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Partially updating API: " + apiId + " by: " + performedBy);

            GeneratedApiResponseDTO response = apiGenerationEngineService.partialUpdateApi(
                    requestId, apiId, performedBy, updates);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "API updated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error partially updating API: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating API: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/{apiId}/sync-components")
    @Operation(summary = "Sync components", description = "Manually sync code base, collections, and documentation")
    public ResponseEntity<?> syncComponents(
            @PathVariable String apiId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "syncing components");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Syncing components for API: " + apiId);

            GeneratedApiEntity api = apiGenerationEngineService.getApiEntity(apiId);
            apiGenerationEngineService.syncGeneratedComponents(api, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Components synced successfully");
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error syncing components: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while syncing components: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}