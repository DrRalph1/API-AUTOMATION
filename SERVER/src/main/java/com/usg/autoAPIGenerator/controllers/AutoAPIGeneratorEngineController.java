package com.usg.autoAPIGenerator.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.helpers.*;
import com.usg.autoAPIGenerator.helpers.apiEngine.LoggingHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.RequestExtractorHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.RequestValidatorHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.ResponseBuilderHelper;
import com.usg.autoAPIGenerator.services.AutoAPIGeneratorEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api")
@RequiredArgsConstructor
@Tag(name = "API GENERATION ENGINE", description = "Endpoints for generating, executing and managing APIs")
public class AutoAPIGeneratorEngineController {

    private final AutoAPIGeneratorEngineService autoAPIGeneratorEngineService;
    private final JwtHelper jwtHelper;
    private final RequestExtractorHelper requestExtractorHelper;
    private final ResponseBuilderHelper responseBuilderHelper;
    private final RequestValidatorHelper requestValidatorHelper;
    private final LoggingHelper loggingHelper;
    private final ObjectMapper objectMapper;

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

            GeneratedApiResponseDTO response = autoAPIGeneratorEngineService.generateApi(requestId, performedBy, request);
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

    @PostMapping(value = "/gen-engine/{apiId}/execute", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Execute API", description = "Execute a generated API - Accepts any content type including multipart/form-data for file uploads")
    public ResponseEntity<?> executeApi(
            @PathVariable String apiId,
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart(required = false) Map<String, MultipartFile> fileMap,
            HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();

        try {
            String performedBy = jwtHelper.extractPerformedBy(request);
            String clientIp = requestExtractorHelper.extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String contentType = request.getContentType();

            loggingHelper.logApiExecution(requestId, apiId, performedBy, contentType, clientIp, userAgent);

            // Create execute request DTO
            ExecuteApiRequestDTO executeRequest = new ExecuteApiRequestDTO();
            executeRequest.setRequestId(requestId);
            executeRequest.setHttpMethod(request.getMethod());

            // Set URL
            StringBuffer requestURL = request.getRequestURL();
            String queryString = request.getQueryString();
            String fullUrl = requestURL.toString();
            if (queryString != null && !queryString.isEmpty()) {
                fullUrl = fullUrl + "?" + queryString;
            }
            executeRequest.setUrl(fullUrl);
            log.info("Request ID: {} - Set URL: {}", requestId, fullUrl);

            // Extract headers
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
            log.info("Request ID: {} - Extracted {} headers", requestId, headers.size());

            // Extract query parameters
            if (queryString != null && !queryString.isEmpty()) {
                Map<String, Object> queryParams = new HashMap<>();
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    queryParams.put(key, value);
                }
                executeRequest.setQueryParams(queryParams);
                log.info("Request ID: {} - Set query params: {}", requestId, queryParams.keySet());
            }

            // Handle file uploads from @RequestPart
            Map<String, MultipartFile> finalFileMap = new HashMap<>();

            // Check for fileMap parameter (multiple files with names)
            if (fileMap != null && !fileMap.isEmpty()) {
                finalFileMap.putAll(fileMap);
                log.info("Request ID: {} - Received fileMap with {} files", requestId, fileMap.size());
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    log.info("Request ID: {} - File in fileMap: '{}' -> {} ({} bytes)",
                            requestId, entry.getKey(), entry.getValue().getOriginalFilename(), entry.getValue().getSize());
                }
            }

            // Check for single file parameter
            if (file != null && !file.isEmpty()) {
                // Try to determine the parameter name from the API configuration
                // For now, use "file" as default, but you can look up from API parameters
                finalFileMap.put("file", file);
                log.info("Request ID: {} - Received single file: {} ({} bytes)",
                        requestId, file.getOriginalFilename(), file.getSize());
            }

            // Also try to extract from multipart request directly if not found via @RequestPart
            if (finalFileMap.isEmpty() && request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                Map<String, MultipartFile> directFileMap = multipartRequest.getFileMap();
                if (directFileMap != null && !directFileMap.isEmpty()) {
                    finalFileMap.putAll(directFileMap);
                    log.info("Request ID: {} - Found {} files via direct multipart extraction", requestId, directFileMap.size());
                    for (Map.Entry<String, MultipartFile> entry : directFileMap.entrySet()) {
                        log.info("Request ID: {} - File: '{}' -> {} ({} bytes)",
                                requestId, entry.getKey(), entry.getValue().getOriginalFilename(), entry.getValue().getSize());
                    }
                }
            }

            // Set files in DTO
            if (!finalFileMap.isEmpty()) {
                executeRequest.setFileMap(finalFileMap);
                executeRequest.setFiles(new ArrayList<>(finalFileMap.values()));
                if (finalFileMap.size() == 1) {
                    executeRequest.setFile(finalFileMap.values().iterator().next());
                }
                log.info("Request ID: {} - Set fileMap with {} entries, file={}, files list size={}",
                        requestId, finalFileMap.size(),
                        executeRequest.getFile() != null ? executeRequest.getFile().getOriginalFilename() : "null",
                        executeRequest.getFiles() != null ? executeRequest.getFiles().size() : 0);
            } else {
                log.info("Request ID: {} - No files found in request", requestId);
            }

            // Extract form parameters (non-file fields)
            if (allParams != null && !allParams.isEmpty()) {
                executeRequest.setBody(new HashMap<>(allParams));
                log.info("Request ID: {} - Set body with form parameters: {}", requestId, allParams.keySet());
            }

            // Also check for body in the original request (for JSON requests)
            if (executeRequest.getBody() == null && request.getContentType() != null &&
                    request.getContentType().contains("application/json")) {
                // Read JSON body
                StringBuilder requestBody = new StringBuilder();
                try (java.io.BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestBody.append(line);
                    }
                }
                String bodyString = requestBody.toString();
                if (bodyString != null && !bodyString.isEmpty()) {
                    try {
                        Map<String, Object> bodyMap = objectMapper.readValue(bodyString,
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        executeRequest.setBody(bodyMap);
                        log.info("Request ID: {} - Set body from JSON: {}", requestId, bodyMap.keySet());
                    } catch (Exception e) {
                        executeRequest.setBody(bodyString);
                        log.info("Request ID: {} - Set body as raw string", requestId);
                    }
                }
            }

            // Log final extracted request details
            log.info("Request ID: {} - FINAL EXTRACTED REQUEST: fileMap={}, file={}, files={}, bodyType={}",
                    requestId,
                    executeRequest.getFileMap() != null ? executeRequest.getFileMap().size() : 0,
                    executeRequest.getFile() != null ? executeRequest.getFile().getOriginalFilename() : "null",
                    executeRequest.getFiles() != null ? executeRequest.getFiles().size() : 0,
                    executeRequest.getBody() != null ? executeRequest.getBody().getClass().getSimpleName() : "null");

            // Execute the API
            ExecuteApiResponseDTO response = autoAPIGeneratorEngineService.executeApi(
                    requestId, performedBy, apiId, executeRequest, clientIp, userAgent, request);

            log.debug("Request ID: {} - API execution completed with status: {}",
                    requestId, response.getResponseCode());

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

            log.info("Request ID: {} - Content-Type: {}, isMultipart: {}", requestId, contentType, isMultipart);

            if (isMultipart && request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

                // CRITICAL: Get all file names from the request
                java.util.Iterator<String> fileNames = multipartRequest.getFileNames();
                Map<String, MultipartFile> fileMap = new HashMap<>();

                log.info("Request ID: {} - Iterating through file names", requestId);

                while (fileNames != null && fileNames.hasNext()) {
                    String paramName = fileNames.next();
                    MultipartFile file = multipartRequest.getFile(paramName);

                    if (file != null && !file.isEmpty()) {
                        fileMap.put(paramName, file);
                        log.info("Request ID: {} - Found file: param='{}', filename='{}', size={} bytes",
                                requestId, paramName, file.getOriginalFilename(), file.getSize());
                    }
                }

                // Also try getFileMap as a backup
                if (fileMap.isEmpty()) {
                    Map<String, MultipartFile> directFileMap = multipartRequest.getFileMap();
                    if (directFileMap != null && !directFileMap.isEmpty()) {
                        fileMap.putAll(directFileMap);
                        log.info("Request ID: {} - Found {} files via getFileMap()", requestId, directFileMap.size());
                        for (Map.Entry<String, MultipartFile> entry : directFileMap.entrySet()) {
                            log.info("Request ID: {} - File in fileMap: '{}' -> {}",
                                    requestId, entry.getKey(), entry.getValue().getOriginalFilename());
                        }
                    }
                }

                // Set the file map in the DTO
                if (!fileMap.isEmpty()) {
                    executeRequest.setFileMap(fileMap);
                    executeRequest.setFiles(new ArrayList<>(fileMap.values()));

                    // Set single file if only one
                    if (fileMap.size() == 1) {
                        executeRequest.setFile(fileMap.values().iterator().next());
                    }

                    log.info("Request ID: {} - Set fileMap with {} entries", requestId, fileMap.size());
                } else {
                    log.warn("Request ID: {} - NO FILES FOUND in multipart request!", requestId);
                }

                // Extract form parameters (excluding files)
                Map<String, String[]> parameterMap = multipartRequest.getParameterMap();
                Map<String, Object> formData = new HashMap<>();

                log.info("Request ID: {} - Processing {} form parameters", requestId, parameterMap.size());

                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0) {
                        if (values.length == 1) {
                            formData.put(key, values[0]);
                            log.info("Request ID: {} - Form parameter: {} = {}", requestId, key, values[0]);
                        } else {
                            formData.put(key, Arrays.asList(values));
                            log.info("Request ID: {} - Form parameter: {} = {}", requestId, key, Arrays.asList(values));
                        }
                    }
                }

                if (!formData.isEmpty()) {
                    executeRequest.setBody(formData);
                    log.info("Request ID: {} - Set body with form data keys: {}", requestId, formData.keySet());
                }
            } else {
                log.info("Request ID: {} - Not a multipart request, using standard extraction", requestId);
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

            // Extract headers
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

            // Set URL
            if (executeRequest.getUrl() == null) {
                StringBuffer requestURL = request.getRequestURL();
                String queryString = request.getQueryString();
                String fullUrl = requestURL.toString();
                if (queryString != null && !queryString.isEmpty()) {
                    fullUrl = fullUrl + "?" + queryString;
                }
                executeRequest.setUrl(fullUrl);
            }

            // Set HTTP method
            executeRequest.setHttpMethod(request.getMethod());

            // Final summary log
            log.info("Request ID: {} - FINAL EXTRACTED REQUEST: fileMap={}, file={}, files={}",
                    requestId,
                    executeRequest.getFileMap() != null ? executeRequest.getFileMap().size() : 0,
                    executeRequest.getFile() != null ? executeRequest.getFile().getOriginalFilename() : "null",
                    executeRequest.getFiles() != null ? executeRequest.getFiles().size() : 0);

        } catch (Exception e) {
            log.error("Request ID: {} - Error extracting request components: {}", requestId, e.getMessage(), e);
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

            ApiTestResultDTO result = autoAPIGeneratorEngineService.testApi(requestId, performedBy, apiId, testRequest, req);

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

            GeneratedApiResponseDTO details = autoAPIGeneratorEngineService.getApiDetails(requestId, apiId);

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

            GeneratedAPIDTO details = autoAPIGeneratorEngineService.getGeneratedApiDetails(requestId, apiId);

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

            ApiAnalyticsDTO analytics = autoAPIGeneratorEngineService.getApiAnalytics(
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

            GeneratedApiResponseDTO apiDetails = autoAPIGeneratorEngineService.getApiDetails(requestId, apiId);
            Map<String, String> generatedFiles = autoAPIGeneratorEngineService.generateApiCode(
                    autoAPIGeneratorEngineService.getApiEntity(apiId));

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

            Map<String, String> generatedFiles = autoAPIGeneratorEngineService.generateApiCode(
                    autoAPIGeneratorEngineService.getApiEntity(apiId));

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

            List<ApiExecutionLogDTO> logs = autoAPIGeneratorEngineService.getExecutionLogs(
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

            List<ApiTestResultDTO> testResults = autoAPIGeneratorEngineService.getTestResults(apiId);

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

            GeneratedApiResponseDTO updatedApi = autoAPIGeneratorEngineService.updateApiStatus(
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

            Map<String, Object> validationResult = autoAPIGeneratorEngineService.validateSourceObject(sourceObject);

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

            GeneratedApiResponseDTO apiDetails = autoAPIGeneratorEngineService.getApiDetails(requestId, apiId);
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

            GeneratedApiResponseDTO response = autoAPIGeneratorEngineService.updateApi(requestId, apiId, performedBy, request);
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

            GeneratedApiResponseDTO response = autoAPIGeneratorEngineService.partialUpdateApi(
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

            GeneratedApiEntity api = autoAPIGeneratorEngineService.getApiEntity(apiId);
            autoAPIGeneratorEngineService.syncGeneratedComponents(api, performedBy);

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

            ApiDetailsResponseDTO details = autoAPIGeneratorEngineService.getCompleteApiDetails(requestId, apiId);

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
                    RequestMethod.OPTIONS},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
    @Operation(summary = "Execute API by ID", description = "Execute a generated API using its ID in the URL path")
    public ResponseEntity<?> executeApiById(
            @PathVariable String apiId,
            HttpServletRequest request,
            @RequestParam(required = false) MultiValueMap<String, String> formParams,
            @RequestParam(required = false) Map<String, MultipartFile> fileMap,
            @RequestParam(required = false) MultipartFile file) {

        String requestId = UUID.randomUUID().toString();

        log.debug("Request ID: {} - Received {} request for API ID: {}", requestId, request.getMethod(), apiId);
        log.debug("Request ID: {} - Full URL: {}", requestId, request.getRequestURL().toString());
        log.debug("Request ID: {} - Content-Type: {}", requestId, request.getContentType());

        try {
            String performedBy = jwtHelper.extractPerformedBy(request);
            String clientIp = requestExtractorHelper.extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String contentType = request.getContentType();

            loggingHelper.logApiExecution(requestId, apiId, performedBy,
                    contentType, clientIp, userAgent);

            // ============ FIX: USE REQUEST EXTRACTOR HELPER (like Oracle does) ============
            ExecuteApiRequestDTO executeRequest = requestExtractorHelper.extractRequestComponents(
                    request, requestId, apiId);

            // Set HTTP method if not set
            if (executeRequest.getHttpMethod() == null) {
                executeRequest.setHttpMethod(request.getMethod());
            }

            // Override file uploads from parameters if present (for multipart support)
            Map<String, MultipartFile> finalFileMap = new HashMap<>();

            if (fileMap != null && !fileMap.isEmpty()) {
                finalFileMap.putAll(fileMap);
            }

            if (file != null && !file.isEmpty()) {
                finalFileMap.put("file", file);
            }

            if (!finalFileMap.isEmpty()) {
                executeRequest.setFileMap(finalFileMap);
                executeRequest.setFiles(new ArrayList<>(finalFileMap.values()));
                if (finalFileMap.size() == 1) {
                    executeRequest.setFile(finalFileMap.values().iterator().next());
                }
            }

            // Override form parameters if present
            if (formParams != null && !formParams.isEmpty()) {
                Map<String, Object> formData = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : formParams.entrySet()) {
                    List<String> values = entry.getValue();
                    if (values != null && !values.isEmpty()) {
                        if (values.size() == 1) {
                            formData.put(entry.getKey(), values.get(0));
                        } else {
                            formData.put(entry.getKey(), values);
                        }
                    }
                }
                if (!formData.isEmpty()) {
                    executeRequest.setBody(formData);
                }
            }
            // ============ END FIX ============

            // Log final extracted request details
            log.info("Request ID: {} - FINAL EXTRACTED REQUEST: pathParams={}, queryParams={}, fileMap={}, file={}, files={}, bodyType={}",
                    requestId,
                    executeRequest.getPathParams() != null ? executeRequest.getPathParams().keySet() : "null",
                    executeRequest.getQueryParams() != null ? executeRequest.getQueryParams().keySet() : "null",
                    executeRequest.getFileMap() != null ? executeRequest.getFileMap().size() : 0,
                    executeRequest.getFile() != null ? executeRequest.getFile().getOriginalFilename() : "null",
                    executeRequest.getFiles() != null ? executeRequest.getFiles().size() : 0,
                    executeRequest.getBody() != null ? executeRequest.getBody().getClass().getSimpleName() : "null");

            // Execute the API
            ExecuteApiResponseDTO response = autoAPIGeneratorEngineService.executeApi(
                    requestId, performedBy, apiId, executeRequest, clientIp, userAgent, request);

            log.debug("Request ID: {} - API execution completed with status: {}",
                    requestId, response.getResponseCode());

            return ResponseEntity.status(response.getResponseCode()).body(response);

        } catch (Exception e) {
            return handleException(requestId, apiId, e);
        }
    }


    /**
     * Helper class to convert Servlet Part to Spring MultipartFile
     */
    private static class StandardMultipartFile implements MultipartFile {
        private final Part part;

        public StandardMultipartFile(Part part) {
            this.part = part;
        }

        @Override
        public String getName() {
            return part.getName();
        }

        @Override
        public String getOriginalFilename() {
            return part.getSubmittedFileName();
        }

        @Override
        public String getContentType() {
            return part.getContentType();
        }

        @Override
        public boolean isEmpty() {
            return part.getSize() == 0;
        }

        @Override
        public long getSize() {
            return part.getSize();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return part.getInputStream().readAllBytes();
        }

        @Override
        public java.io.InputStream getInputStream() throws IOException {
            return part.getInputStream();
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (java.io.InputStream in = part.getInputStream();
                 java.io.FileOutputStream out = new java.io.FileOutputStream(dest)) {
                in.transferTo(out);
            }
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

            boolean isAvailable = autoAPIGeneratorEngineService.isApiCodeAvailable(apiCode);

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