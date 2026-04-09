package com.usg.apiGeneration.controllers;

import com.usg.apiGeneration.dtos.apiGenerationEngine.*;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiGeneration.helpers.*;
import com.usg.apiGeneration.helpers.apiEngine.LoggingHelper;
import com.usg.apiGeneration.helpers.apiEngine.RequestExtractorHelper;
import com.usg.apiGeneration.helpers.apiEngine.RequestValidatorHelper;
import com.usg.apiGeneration.helpers.apiEngine.ResponseBuilderHelper;
import com.usg.apiGeneration.services.AutomationEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api")
@RequiredArgsConstructor
@Tag(name = "API GENERATION ENGINE", description = "Endpoints for generating, executing and managing APIs")
public class AutomationEngineController {

    private final AutomationEngineService automationEngineService;
    private final JwtHelper jwtHelper;
    private final RequestExtractorHelper requestExtractorHelper;
    private final ResponseBuilderHelper responseBuilderHelper;
    private final RequestValidatorHelper requestValidatorHelper;
    private final LoggingHelper loggingHelper;

    @PostMapping("/gen-engine/generate")
    @Operation(summary = "Generate API", description = "Generate a new API based on configuration")
    public ResponseEntity<?> generateApi(
            @Valid @RequestBody GenerateApiRequestDTO request,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate authorization
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating API");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            // Check validation errors
            String validationErrors = requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null) {
                return responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logApiGeneration(requestId, request.getApiName(), performedBy);

            GeneratedApiResponseDTO response = automationEngineService.generateApi(requestId, performedBy, request);
            loggingHelper.logSuccess(requestId, "API generation", "ID: " + response.getId());

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API generated successfully",
                    response);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "generating API", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/gen-engine/{apiId}/execute")
    @Operation(summary = "Execute API", description = "Execute a generated API - Accepts any content type including multipart/form-data for file uploads")
    public ResponseEntity<?> executeApi(
            @PathVariable String apiId,
            HttpServletRequest req,
            HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();

        try {
            String performedBy = jwtHelper.extractPerformedBy(request);
            String clientIp = requestExtractorHelper.extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String contentType = request.getContentType();

            loggingHelper.logApiExecution(requestId, apiId, performedBy, contentType, clientIp, userAgent);

            // Extract all request components with file support
            ExecuteApiRequestDTO executeRequest = extractRequestComponentsWithFileSupport(request, requestId, apiId);

            // Execute the API
            ExecuteApiResponseDTO response = automationEngineService.executeApi(
                    requestId, performedBy, apiId, executeRequest, clientIp, userAgent, req);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    response.getMessage(),
                    response,
                    HttpStatus.valueOf(response.getResponseCode()));

        } catch (Exception e) {
            loggingHelper.logError(requestId, "executing API", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while executing API: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // NEW HELPER METHOD: Extracts request components with file upload support
    private ExecuteApiRequestDTO extractRequestComponentsWithFileSupport(HttpServletRequest request, String requestId, String apiId) {
        ExecuteApiRequestDTO executeRequest = new ExecuteApiRequestDTO();
        executeRequest.setRequestId(requestId);

        try {
            // Check if this is a multipart request (file upload)
            String contentType = request.getContentType();
            boolean isMultipart = contentType != null && contentType.startsWith("multipart/form-data");

            if (isMultipart && request instanceof MultipartHttpServletRequest) {
                // Handle file upload request
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

                // Extract files
                Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
                if (fileMap != null && !fileMap.isEmpty()) {
                    executeRequest.setFileMap(fileMap);

                    // Set single file if there's exactly one
                    if (fileMap.size() == 1) {
                        executeRequest.setFile(fileMap.values().iterator().next());
                    }

                    // Set multiple files list
                    executeRequest.setFiles(new ArrayList<>(fileMap.values()));

                    log.info("Extracted {} file(s) from multipart request", fileMap.size());
                    for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                        log.info("  File field '{}': {} ({} bytes)",
                                entry.getKey(),
                                entry.getValue().getOriginalFilename(),
                                entry.getValue().getSize());
                    }
                }

                // Extract form parameters (excluding files)
                Map<String, String[]> parameterMap = multipartRequest.getParameterMap();
                Map<String, Object> formData = new HashMap<>();

                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0) {
                        if (values.length == 1) {
                            formData.put(key, values[0]);
                        } else {
                            formData.put(key, Arrays.asList(values));
                        }
                    }
                }

                if (!formData.isEmpty()) {
                    executeRequest.setBody(formData);
                    log.info("Extracted form parameters: {}", formData.keySet());
                }

                // Check for base64 encoded files in form data
                boolean hasBase64Files = false;
                for (Object value : formData.values()) {
                    if (value instanceof String) {
                        String strValue = (String) value;
                        if (strValue != null && strValue.startsWith("data:") && strValue.contains(";base64,")) {
                            hasBase64Files = true;
                            break;
                        }
                    }
                }
                executeRequest.setHasBase64Files(hasBase64Files);
                if (hasBase64Files) {
                    log.info("Detected base64 encoded files in form data");
                }
            } else {
                // Not a multipart request - use existing extractor
                ExecuteApiRequestDTO extracted = requestExtractorHelper.extractRequestComponents(request, requestId, apiId);
                if (extracted != null) {
                    executeRequest.setUrl(extracted.getUrl());
                    executeRequest.setPathParams(extracted.getPathParams());
                    executeRequest.setQueryParams(extracted.getQueryParams());
                    executeRequest.setBody(extracted.getBody());
                    executeRequest.setHttpMethod(extracted.getHttpMethod());
                    executeRequest.setTimeoutSeconds(extracted.getTimeoutSeconds());
                    executeRequest.setMetadata(extracted.getMetadata());
                }
            }

            // Extract headers (common for all request types)
            Enumeration<String> headerNames = request.getHeaderNames();
            Map<String, String> headers = new HashMap<>();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    headers.put(headerName, headerValue);
                }
            }
            executeRequest.setHeaders(headers);

            // Set URL if not already set
            if (executeRequest.getUrl() == null) {
                StringBuffer requestURL = request.getRequestURL();
                String queryString = request.getQueryString();
                String fullUrl = requestURL.toString();
                if (queryString != null && !queryString.isEmpty()) {
                    fullUrl = fullUrl + "?" + queryString;
                }
                executeRequest.setUrl(fullUrl);
            }

            // Set query params if not already set
            if (executeRequest.getQueryParams() == null && request.getQueryString() != null) {
                Map<String, Object> queryParams = new HashMap<>();
                String[] pairs = request.getQueryString().split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    queryParams.put(key, value);
                }
                executeRequest.setQueryParams(queryParams);
            }

            // Set HTTP method
            executeRequest.setHttpMethod(request.getMethod());

        } catch (Exception e) {
            log.error("Error extracting request components with file support: {}", e.getMessage(), e);
            // Fall back to standard extraction
            ExecuteApiRequestDTO fallback = requestExtractorHelper.extractRequestComponents(request, requestId, apiId);
            if (fallback != null) {
                executeRequest = fallback;
                executeRequest.setRequestId(requestId);
            }
        }

        return executeRequest;
    }

    @PostMapping("/gen-engine/{apiId}/test")
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
            String validationErrors = requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null) {
                return responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logApiTest(requestId, apiId, testRequest.getTestName(), performedBy);

            ApiTestResultDTO result = automationEngineService.testApi(requestId, performedBy, apiId, testRequest, req);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API test completed",
                    result);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "testing API", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while testing API: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}")
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
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            GeneratedApiResponseDTO details = automationEngineService.getApiDetails(requestId, apiId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API details retrieved successfully",
                    details);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting API details", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting API details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen/api/{apiId}")
    @Operation(summary = "Get Full Generated API details", description = "Get full details of a generated API")
    public ResponseEntity<?> getGeneratedApiDetails(
            @PathVariable String apiId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting API details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            GeneratedAPIDTO details = automationEngineService.getGeneratedApiDetails(requestId, apiId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API details retrieved successfully",
                    details);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting API details", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting API details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}/analytics")
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
            loggingHelper.logGetApiAnalytics(requestId, apiId, performedBy);

            ApiAnalyticsDTO analytics = automationEngineService.getApiAnalytics(
                    requestId, apiId, startDate, endDate);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API analytics retrieved successfully",
                    analytics);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting API analytics", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting API analytics: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}/code")
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
            loggingHelper.logCodeGeneration(requestId, apiId, null, performedBy);

            GeneratedApiResponseDTO apiDetails = automationEngineService.getApiDetails(requestId, apiId);
            Map<String, String> generatedFiles = automationEngineService.generateApiCode(
                    automationEngineService.getApiEntity(apiId));

            Map<String, Object> data = Map.of(
                    "apiDetails", apiDetails,
                    "generatedFiles", generatedFiles
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API code generated successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "generating API code", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while generating API code: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}/code/{language}")
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
            loggingHelper.logCodeGeneration(requestId, apiId, language, performedBy);

            Map<String, String> generatedFiles = automationEngineService.generateApiCode(
                    automationEngineService.getApiEntity(apiId));

            String codeExample = generatedFiles.getOrDefault(language.toLowerCase(),
                    "Code example not available for " + language);

            Map<String, Object> data = Map.of(
                    "apiId", apiId,
                    "language", language,
                    "code", codeExample
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Code example retrieved successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting code example", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting code example: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}/logs")
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
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            List<ApiExecutionLogDTO> logs = automationEngineService.getExecutionLogs(
                    apiId, fromDate, toDate, limit);

            Map<String, Object> data = Map.of(
                    "apiId", apiId,
                    "logs", logs,
                    "totalCount", logs.size()
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Execution logs retrieved successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting execution logs", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting execution logs: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}/tests")
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
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            List<ApiTestResultDTO> testResults = automationEngineService.getTestResults(apiId);

            Map<String, Object> data = Map.of(
                    "apiId", apiId,
                    "tests", testResults,
                    "totalCount", testResults.size()
            );

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Test results retrieved successfully",
                    data);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting test results", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting test results: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/gen-engine/{apiId}/status")
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

            RequestValidatorHelper.ValidationResult validationResult =
                    requestValidatorHelper.validateStatusUpdate(newStatus);

            if (!validationResult.isValid()) {
                return responseBuilderHelper.buildValidationErrorResponse(
                        requestId, validationResult.getErrors());
            }

            loggingHelper.logStatusUpdate(requestId, apiId, newStatus, performedBy);

            GeneratedApiResponseDTO updatedApi = automationEngineService.updateApiStatus(
                    apiId, newStatus, performedBy);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API status updated successfully",
                    updatedApi);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "updating API status", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while updating API status: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/validate/source-object")
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
            loggingHelper.logSourceValidation(requestId, objectName, performedBy);

            ApiSourceObjectDTO sourceObject = ApiSourceObjectDTO.builder()
                    .objectName(objectName)
                    .objectType(objectType)
                    .owner(owner)
                    .build();

            Map<String, Object> validationResult = automationEngineService.validateSourceObject(sourceObject);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Source object validated successfully",
                    validationResult);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "validating source object", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while validating source object: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}/related-components")
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
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            GeneratedApiResponseDTO apiDetails = automationEngineService.getApiDetails(requestId, apiId);
            Map<String, Object> relatedComponents = extractRelatedComponents(apiDetails);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Related components retrieved successfully",
                    relatedComponents);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting related components", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting related components: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> extractRelatedComponents(GeneratedApiResponseDTO apiDetails) {
        Map<String, Object> relatedComponents = new HashMap<>();

        if (apiDetails.getMetadata() != null) {
            Map<String, Object> metadata = apiDetails.getMetadata();
            relatedComponents.put("codeBaseRequestId", metadata.get("codeBaseRequestId"));
            relatedComponents.put("collectionsCollectionId", metadata.get("collectionsCollectionId"));
            relatedComponents.put("documentationCollectionId", metadata.get("documentationCollectionId"));
            relatedComponents.put("urls", metadata.get("urls"));
        }

        return relatedComponents;
    }

    @PutMapping("/gen-engine/{apiId}")
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
            String validationErrors = requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null) {
                return responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logApiGeneration(requestId, request.getApiName(), performedBy);

            GeneratedApiResponseDTO response = automationEngineService.updateApi(requestId, apiId, performedBy, request);
            loggingHelper.logSuccess(requestId, "API update", "ID: " + apiId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API updated successfully",
                    response);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "updating API", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while updating API: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/gen-engine/{apiId}")
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
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            GeneratedApiResponseDTO response = automationEngineService.partialUpdateApi(
                    requestId, apiId, performedBy, updates);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API updated successfully",
                    response);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "partially updating API", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while updating API: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/gen-engine/{apiId}/sync-components")
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
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            GeneratedApiEntity api = automationEngineService.getApiEntity(apiId);
            automationEngineService.syncGeneratedComponents(api, performedBy);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Components synced successfully",
                    null);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "syncing components", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while syncing components: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gen-engine/{apiId}/complete-details")
    @Operation(summary = "Get Complete API Details",
            description = "Get all API details exactly as they were captured during generation")
    public ResponseEntity<?> getCompleteApiDetails(
            @PathVariable String apiId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting complete API details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logGetApiDetails(requestId, apiId, performedBy);

            ApiDetailsResponseDTO details = automationEngineService.getCompleteApiDetails(requestId, apiId);

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "Complete API details retrieved successfully",
                    details);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "getting complete API details", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while getting complete API details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/gen/{apiId}/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD,
                    RequestMethod.OPTIONS}
    )
    @Operation(summary = "Execute API by ID", description = "Execute a generated API using its ID in the URL path")
    public ResponseEntity<?> executeApiById(
            @PathVariable String apiId,
            HttpServletRequest req,
            @RequestParam(required = false) MultiValueMap<String, String> formParams) {

        String requestId = UUID.randomUUID().toString();

        log.debug("Request ID: {} - Received {} request for API ID: {}", requestId, req.getMethod(), apiId);
        log.debug("Request ID: {} - Full URL: {}", requestId, req.getRequestURL().toString());

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            String clientIp = requestExtractorHelper.extractClientIp(req);
            String userAgent = req.getHeader("User-Agent");

            loggingHelper.logApiExecution(requestId, apiId, performedBy,
                    req.getContentType(), clientIp, userAgent);

            // Extract request components with file support
            ExecuteApiRequestDTO executeRequest = extractRequestComponentsWithFileSupport(req, requestId, apiId);

            // Set HTTP method
            executeRequest.setHttpMethod(req.getMethod());

            // Execute the API
            ExecuteApiResponseDTO response = automationEngineService.executeApi(
                    requestId, performedBy, apiId, executeRequest, clientIp, userAgent, req);

            log.debug("Request ID: {} - API execution completed with status: {}",
                    requestId, response.getResponseCode());

            return ResponseEntity.status(response.getResponseCode()).body(response);

        } catch (Exception e) {
            return handleException(requestId, apiId, e);
        }
    }

    private ResponseEntity<Map<String, Object>> handleException(String requestId, String apiId, Exception e) {
        loggingHelper.logError(requestId, "executing API by ID", e.getMessage(), e);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("requestId", requestId);
        errorResponse.put("timestamp", new Date().toInstant().toString());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An error occurred while executing API: " + e.getMessage());
        errorResponse.put("path", apiId);

        if (log.isDebugEnabled()) {
            errorResponse.put("exception", e.getClass().getName());
            errorResponse.put("stackTrace", Arrays.stream(e.getStackTrace())
                    .limit(5)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @RequestMapping(value = "/gen/{apiId}/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok()
                .header("Allow", "GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS")
                .header("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    @GetMapping("/gen-engine/check-code")
    @Operation(summary = "Check API code availability",
            description = "Check if an API code is available for use")
    public ResponseEntity<?> checkApiCodeAvailability(
            @RequestParam String apiCode,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "checking API code availability");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggingHelper.logGetApiDetails(requestId, apiCode, performedBy);

            boolean isAvailable = automationEngineService.isApiCodeAvailable(apiCode);

            Map<String, Object> response = new HashMap<>();
            response.put("available", isAvailable);
            response.put("apiCode", apiCode);
            response.put("message", isAvailable ? "API code is available" : "API code already exists");

            return responseBuilderHelper.buildSuccessResponse(
                    requestId,
                    "API code availability checked successfully",
                    response);

        } catch (Exception e) {
            loggingHelper.logError(requestId, "checking API code availability", e.getMessage(), e);
            return responseBuilderHelper.buildErrorResponse(
                    requestId,
                    "An error occurred while checking API code availability: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}