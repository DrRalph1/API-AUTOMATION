package com.usg.autoAPIGenerator.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiAnalyticsDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiDetailsResponseDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiExecutionLogDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiTestRequestDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiTestResultDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ExecuteApiResponseDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GeneratedAPIDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GeneratedApiResponseDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.helpers.JwtHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.LoggingHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.RequestExtractorHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.RequestValidatorHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.ResponseBuilderHelper;
import com.usg.autoAPIGenerator.services.AutoAPIGeneratorEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping({"/plx/api"})
@Tag(name = "API GENERATION ENGINE", description = "Endpoints for generating, executing and managing APIs")
public class AutoAPIGeneratorEngineController {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(com.usg.autoAPIGenerator.controllers.AutoAPIGeneratorEngineController.class);

    private final AutoAPIGeneratorEngineService autoAPIGeneratorEngineService;

    private final JwtHelper jwtHelper;

    private final RequestExtractorHelper requestExtractorHelper;

    private final ResponseBuilderHelper responseBuilderHelper;

    private final RequestValidatorHelper requestValidatorHelper;

    private final LoggingHelper loggingHelper;

    private final ObjectMapper objectMapper;

    @Generated
    public AutoAPIGeneratorEngineController(AutoAPIGeneratorEngineService autoAPIGeneratorEngineService, JwtHelper jwtHelper, RequestExtractorHelper requestExtractorHelper, ResponseBuilderHelper responseBuilderHelper, RequestValidatorHelper requestValidatorHelper, LoggingHelper loggingHelper, ObjectMapper objectMapper) {
        this.autoAPIGeneratorEngineService = autoAPIGeneratorEngineService;
        this.jwtHelper = jwtHelper;
        this.requestExtractorHelper = requestExtractorHelper;
        this.responseBuilderHelper = responseBuilderHelper;
        this.requestValidatorHelper = requestValidatorHelper;
        this.loggingHelper = loggingHelper;
        this.objectMapper = objectMapper;
    }

    @PostMapping({"/gen-engine/generate"})
    @Operation(summary = "Generate API", description = "Generate a new API based on configuration")
    public ResponseEntity<?> generateApi(@Valid @RequestBody GenerateApiRequestDTO request, BindingResult bindingResult, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "generating API");
        if (authValidation != null)
            return authValidation;
        try {
            String validationErrors = this.requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null)
                return this.responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logApiGeneration(requestId, request.getApiName(), performedBy);
            GeneratedApiResponseDTO response = this.autoAPIGeneratorEngineService.generateApi(requestId, performedBy, request);
            this.loggingHelper.logSuccess(requestId, "API generation", "ID: " + response.getId());
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API generated successfully", response);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "generating API", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = {"/gen-engine/{apiId}/execute"}, consumes = {"multipart/form-data", "application/json"})
    @Operation(summary = "Execute API", description = "Execute a generated API - Accepts any content type including multipart/form-data for file uploads")
    public ResponseEntity<?> executeApi(@PathVariable String apiId, @RequestParam(required = false) Map<String, String> allParams, @RequestPart(required = false) MultipartFile file, @RequestPart(required = false) Map<String, MultipartFile> fileMap, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        try {
            String performedBy = jwtHelper.extractPerformedBy(request);
            String clientIp = this.requestExtractorHelper.extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String contentType = request.getContentType();
            this.loggingHelper.logApiExecution(requestId, apiId, performedBy, contentType, clientIp, userAgent);
            ExecuteApiRequestDTO executeRequest = new ExecuteApiRequestDTO();
            executeRequest.setRequestId(requestId);
            executeRequest.setHttpMethod(request.getMethod());
            StringBuffer requestURL = request.getRequestURL();
            String queryString = request.getQueryString();
            String fullUrl = requestURL.toString();
            if (queryString != null && !queryString.isEmpty())
                fullUrl = fullUrl + "?" + fullUrl;
            executeRequest.setUrl(fullUrl);
            log.info("Request ID: {} - Set URL: {}", requestId, fullUrl);
            Enumeration<String> headerNames = request.getHeaderNames();
            Map<String, String> headers = new HashMap<>();
            if (headerNames != null)
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    headers.put(headerName, headerValue);
                }
            executeRequest.setHeaders(headers);
            log.info("Request ID: {} - Extracted {} headers", requestId, Integer.valueOf(headers.size()));
            if (queryString != null && !queryString.isEmpty()) {
                Map<String, Object> queryParams = new HashMap<>();
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    String key = keyValue[0];
                    String value = (keyValue.length > 1) ? keyValue[1] : "";
                    queryParams.put(key, value);
                }
                executeRequest.setQueryParams(queryParams);
                log.info("Request ID: {} - Set query params: {}", requestId, queryParams.keySet());
            }
            Map<String, MultipartFile> finalFileMap = new HashMap<>();
            if (fileMap != null && !fileMap.isEmpty()) {
                finalFileMap.putAll(fileMap);
                log.info("Request ID: {} - Received fileMap with {} files", requestId, Integer.valueOf(fileMap.size()));
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    log.info("Request ID: {} - File in fileMap: '{}' -> {} ({} bytes)", new Object[] { requestId, entry
                            .getKey(), ((MultipartFile)entry.getValue()).getOriginalFilename(), Long.valueOf(((MultipartFile)entry.getValue()).getSize()) });
                }
            }
            if (file != null && !file.isEmpty()) {
                finalFileMap.put("file", file);
                log.info("Request ID: {} - Received single file: {} ({} bytes)", new Object[] { requestId, file
                        .getOriginalFilename(), Long.valueOf(file.getSize()) });
            }
            if (finalFileMap.isEmpty() && request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
                Map<String, MultipartFile> directFileMap = multipartRequest.getFileMap();
                if (directFileMap != null && !directFileMap.isEmpty()) {
                    finalFileMap.putAll(directFileMap);
                    log.info("Request ID: {} - Found {} files via direct multipart extraction", requestId, Integer.valueOf(directFileMap.size()));
                    for (Map.Entry<String, MultipartFile> entry : directFileMap.entrySet()) {
                        log.info("Request ID: {} - File: '{}' -> {} ({} bytes)", new Object[] { requestId, entry
                                .getKey(), ((MultipartFile)entry.getValue()).getOriginalFilename(), Long.valueOf(((MultipartFile)entry.getValue()).getSize()) });
                    }
                }
            }
            if (!finalFileMap.isEmpty()) {
                executeRequest.setFileMap(finalFileMap);
                executeRequest.setFiles(new ArrayList(finalFileMap.values()));
                if (finalFileMap.size() == 1)
                    executeRequest.setFile(finalFileMap.values().iterator().next());
                log.info("Request ID: {} - Set fileMap with {} entries, file={}, files list size={}", new Object[] { requestId,
                        Integer.valueOf(finalFileMap.size()),
                        (executeRequest.getFile() != null) ? executeRequest.getFile().getOriginalFilename() : "null",
                        Integer.valueOf((executeRequest.getFiles() != null) ? executeRequest.getFiles().size() : 0) });
            } else {
                log.info("Request ID: {} - No files found in request", requestId);
            }
            if (allParams != null && !allParams.isEmpty()) {
                executeRequest.setBody(new HashMap<>(allParams));
                log.info("Request ID: {} - Set body with form parameters: {}", requestId, allParams.keySet());
            }
            if (executeRequest.getBody() == null && request.getContentType() != null && request
                    .getContentType().contains("application/json")) {
                StringBuilder requestBody = new StringBuilder();
                BufferedReader reader = request.getReader();
                try {
                    String line;
                    while ((line = reader.readLine()) != null)
                        requestBody.append(line);
                    if (reader != null)
                        reader.close();
                } catch (Throwable throwable) {
                    if (reader != null)
                        try {
                            reader.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                String bodyString = requestBody.toString();
                if (bodyString != null && !bodyString.isEmpty())
                    try {
                        Map<String, Object> bodyMap = (Map<String, Object>)this.objectMapper.readValue(bodyString, (TypeReference)new Object());
                        executeRequest.setBody(bodyMap);
                        log.info("Request ID: {} - Set body from JSON: {}", requestId, bodyMap.keySet());
                    } catch (Exception e) {
                        executeRequest.setBody(bodyString);
                        log.info("Request ID: {} - Set body as raw string", requestId);
                    }
            }
            log.info("Request ID: {} - FINAL EXTRACTED REQUEST: fileMap={}, file={}, files={}, bodyType={}", new Object[] { requestId,

                    Integer.valueOf((executeRequest.getFileMap() != null) ? executeRequest.getFileMap().size() : 0),
                    (executeRequest.getFile() != null) ? executeRequest.getFile().getOriginalFilename() : "null",
                    Integer.valueOf((executeRequest.getFiles() != null) ? executeRequest.getFiles().size() : 0),
                    (executeRequest.getBody() != null) ? executeRequest.getBody().getClass().getSimpleName() : "null" });
            ExecuteApiResponseDTO response = this.autoAPIGeneratorEngineService.executeApi(requestId, performedBy, apiId, executeRequest, clientIp, userAgent, request);
            log.debug("Request ID: {} - API execution completed with status: {}", requestId, response
                    .getResponseCode());
            return this.responseBuilderHelper.buildSuccessResponse(requestId, response

                            .getMessage(), response,

                    HttpStatus.valueOf(response.getResponseCode().intValue()));
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "executing API", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while executing API: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ExecuteApiRequestDTO extractRequestComponentsWithFileSupport(HttpServletRequest request, String requestId, String apiId) {
        ExecuteApiRequestDTO executeRequest = new ExecuteApiRequestDTO();
        executeRequest.setRequestId(requestId);
        try {
            String contentType = request.getContentType();
            boolean isMultipart = (contentType != null && contentType.startsWith("multipart/form-data"));
            log.info("Request ID: {} - Content-Type: {}, isMultipart: {}", new Object[] { requestId, contentType, Boolean.valueOf(isMultipart) });
            if (isMultipart && request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
                Iterator<String> fileNames = multipartRequest.getFileNames();
                Map<String, MultipartFile> fileMap = new HashMap<>();
                log.info("Request ID: {} - Iterating through file names", requestId);
                while (fileNames != null && fileNames.hasNext()) {
                    String paramName = fileNames.next();
                    MultipartFile file = multipartRequest.getFile(paramName);
                    if (file != null && !file.isEmpty()) {
                        fileMap.put(paramName, file);
                        log.info("Request ID: {} - Found file: param='{}', filename='{}', size={} bytes", new Object[] { requestId, paramName, file
                                .getOriginalFilename(), Long.valueOf(file.getSize()) });
                    }
                }
                if (fileMap.isEmpty()) {
                    Map<String, MultipartFile> directFileMap = multipartRequest.getFileMap();
                    if (directFileMap != null && !directFileMap.isEmpty()) {
                        fileMap.putAll(directFileMap);
                        log.info("Request ID: {} - Found {} files via getFileMap()", requestId, Integer.valueOf(directFileMap.size()));
                        for (Map.Entry<String, MultipartFile> entry : directFileMap.entrySet()) {
                            log.info("Request ID: {} - File in fileMap: '{}' -> {}", new Object[] { requestId, entry
                                    .getKey(), ((MultipartFile)entry.getValue()).getOriginalFilename() });
                        }
                    }
                }
                if (!fileMap.isEmpty()) {
                    executeRequest.setFileMap(fileMap);
                    executeRequest.setFiles(new ArrayList(fileMap.values()));
                    if (fileMap.size() == 1)
                        executeRequest.setFile(fileMap.values().iterator().next());
                    log.info("Request ID: {} - Set fileMap with {} entries", requestId, Integer.valueOf(fileMap.size()));
                } else {
                    log.warn("Request ID: {} - NO FILES FOUND in multipart request!", requestId);
                }
                Map<String, String[]> parameterMap = multipartRequest.getParameterMap();
                Map<String, Object> formData = new HashMap<>();
                log.info("Request ID: {} - Processing {} form parameters", requestId, Integer.valueOf(parameterMap.size()));
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0) {
                        if (values.length == 1) {
                            formData.put(key, values[0]);
                            log.info("Request ID: {} - Form parameter: {} = {}", new Object[] { requestId, key, values[0] });
                            continue;
                        }
                        formData.put(key, Arrays.asList(values));
                        log.info("Request ID: {} - Form parameter: {} = {}", new Object[] { requestId, key, Arrays.asList(values) });
                    }
                }
                if (!formData.isEmpty()) {
                    executeRequest.setBody(formData);
                    log.info("Request ID: {} - Set body with form data keys: {}", requestId, formData.keySet());
                }
            } else {
                log.info("Request ID: {} - Not a multipart request, using standard extraction", requestId);
                ExecuteApiRequestDTO extracted = this.requestExtractorHelper.extractRequestComponents(request, requestId, apiId);
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
            Enumeration<String> headerNames = request.getHeaderNames();
            Map<String, String> headers = new HashMap<>();
            if (headerNames != null)
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    headers.put(headerName, headerValue);
                }
            executeRequest.setHeaders(headers);
            if (executeRequest.getUrl() == null) {
                StringBuffer requestURL = request.getRequestURL();
                String queryString = request.getQueryString();
                String fullUrl = requestURL.toString();
                if (queryString != null && !queryString.isEmpty())
                    fullUrl = fullUrl + "?" + fullUrl;
                executeRequest.setUrl(fullUrl);
            }
            executeRequest.setHttpMethod(request.getMethod());
            log.info("Request ID: {} - FINAL EXTRACTED REQUEST: fileMap={}, file={}, files={}", new Object[] { requestId,

                    Integer.valueOf((executeRequest.getFileMap() != null) ? executeRequest.getFileMap().size() : 0),
                    (executeRequest.getFile() != null) ? executeRequest.getFile().getOriginalFilename() : "null",
                    Integer.valueOf((executeRequest.getFiles() != null) ? executeRequest.getFiles().size() : 0) });
        } catch (Exception e) {
            log.error("Request ID: {} - Error extracting request components: {}", new Object[] { requestId, e.getMessage(), e });
            ExecuteApiRequestDTO fallback = this.requestExtractorHelper.extractRequestComponents(request, requestId, apiId);
            if (fallback != null) {
                executeRequest = fallback;
                executeRequest.setRequestId(requestId);
            }
        }
        return executeRequest;
    }

    @PostMapping({"/gen-engine/{apiId}/test"})
    @Operation(summary = "Test API", description = "Test a generated API with sample data")
    public ResponseEntity<?> testApi(@PathVariable String apiId, @Valid @RequestBody ApiTestRequestDTO testRequest, BindingResult bindingResult, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "testing API");
        if (authValidation != null)
            return authValidation;
        try {
            String validationErrors = this.requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null)
                return this.responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logApiTest(requestId, apiId, testRequest.getTestName(), performedBy);
            ApiTestResultDTO result = this.autoAPIGeneratorEngineService.testApi(requestId, performedBy, apiId, testRequest, req);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API test completed", result);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "testing API", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while testing API: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}"})
    @Operation(summary = "Get API details", description = "Get details of a generated API")
    public ResponseEntity<?> getApiDetails(@PathVariable String apiId, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting API details");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            GeneratedApiResponseDTO details = this.autoAPIGeneratorEngineService.getApiDetails(requestId, apiId);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API details retrieved successfully", details);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting API details", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting API details: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen/api/{apiId}"})
    @Operation(summary = "Get Full Generated API details", description = "Get full details of a generated API")
    public ResponseEntity<?> getGeneratedApiDetails(@PathVariable String apiId, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting API details");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            GeneratedAPIDTO details = this.autoAPIGeneratorEngineService.getGeneratedApiDetails(requestId, apiId);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API details retrieved successfully", details);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting API details", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting API details: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}/analytics"})
    @Operation(summary = "Get API analytics", description = "Get analytics for a generated API")
    public ResponseEntity<?> getApiAnalytics(@PathVariable String apiId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting API analytics");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiAnalytics(requestId, apiId, performedBy);
            ApiAnalyticsDTO analytics = this.autoAPIGeneratorEngineService.getApiAnalytics(requestId, apiId, startDate, endDate);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API analytics retrieved successfully", analytics);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting API analytics", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting API analytics: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}/code"})
    @Operation(summary = "Generate API code", description = "Generate code for an API (PL/SQL, OpenAPI, Postman)")
    public ResponseEntity<?> generateApiCode(@PathVariable String apiId, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "generating API code");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logCodeGeneration(requestId, apiId, null, performedBy);
            GeneratedApiResponseDTO apiDetails = this.autoAPIGeneratorEngineService.getApiDetails(requestId, apiId);
            Map<String, String> generatedFiles = this.autoAPIGeneratorEngineService.generateApiCode(this.autoAPIGeneratorEngineService
                    .getApiEntity(apiId));
            Map<String, Object> data = Map.of("apiDetails", apiDetails, "generatedFiles", generatedFiles);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API code generated successfully", data);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "generating API code", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while generating API code: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}/code/{language}"})
    @Operation(summary = "Get code example", description = "Get code example for a specific language")
    public ResponseEntity<?> getCodeExample(@PathVariable String apiId, @PathVariable String language, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting code example");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logCodeGeneration(requestId, apiId, language, performedBy);
            Map<String, String> generatedFiles = this.autoAPIGeneratorEngineService.generateApiCode(this.autoAPIGeneratorEngineService
                    .getApiEntity(apiId));
            String codeExample = generatedFiles.getOrDefault(language.toLowerCase(), "Code example not available for " + language);
            Map<String, Object> data = Map.of("apiId", apiId, "language", language, "code", codeExample);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "Code example retrieved successfully", data);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting code example", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting code example: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}/logs"})
    @Operation(summary = "Get execution logs", description = "Get execution logs for an API")
    public ResponseEntity<?> getExecutionLogs(@PathVariable String apiId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate, @RequestParam(required = false, defaultValue = "100") int limit, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting execution logs");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            List<ApiExecutionLogDTO> logs = this.autoAPIGeneratorEngineService.getExecutionLogs(apiId, fromDate, toDate, limit);
            Map<String, Object> data = Map.of("apiId", apiId, "logs", logs, "totalCount",

                    Integer.valueOf(logs.size()));
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "Execution logs retrieved successfully", data);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting execution logs", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting execution logs: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}/tests"})
    @Operation(summary = "Get test results", description = "Get test results for an API")
    public ResponseEntity<?> getTestResults(@PathVariable String apiId, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting test results");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            List<ApiTestResultDTO> testResults = this.autoAPIGeneratorEngineService.getTestResults(apiId);
            Map<String, Object> data = Map.of("apiId", apiId, "tests", testResults, "totalCount",

                    Integer.valueOf(testResults.size()));
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "Test results retrieved successfully", data);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting test results", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting test results: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping({"/gen-engine/{apiId}/status"})
    @Operation(summary = "Update API status", description = "Update the status of an API (DRAFT, ACTIVE, DEPRECATED)")
    public ResponseEntity<?> updateApiStatus(@PathVariable String apiId, @RequestBody Map<String, String> statusRequest, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "updating API status");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            String newStatus = statusRequest.get("status");
            RequestValidatorHelper.ValidationResult validationResult = this.requestValidatorHelper.validateStatusUpdate(newStatus);
            if (!validationResult.isValid())
                return this.responseBuilderHelper.buildValidationErrorResponse(requestId, validationResult
                        .getErrors());
            this.loggingHelper.logStatusUpdate(requestId, apiId, newStatus, performedBy);
            GeneratedApiResponseDTO updatedApi = this.autoAPIGeneratorEngineService.updateApiStatus(apiId, newStatus, performedBy);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API status updated successfully", updatedApi);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "updating API status", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while updating API status: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/validate/source-object"})
    @Operation(summary = "Validate source object", description = "Validate an Oracle source object before API generation")
    public ResponseEntity<?> validateSourceObject(@RequestParam String objectName, @RequestParam String objectType, @RequestParam(required = false) String owner, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "validating source object");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logSourceValidation(requestId, objectName, performedBy);
            ApiSourceObjectDTO sourceObject = ApiSourceObjectDTO.builder().objectName(objectName).objectType(objectType).owner(owner).build();
            Map<String, Object> validationResult = this.autoAPIGeneratorEngineService.validateSourceObject(sourceObject);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "Source object validated successfully", validationResult);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "validating source object", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while validating source object: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}/related-components"})
    @Operation(summary = "Get related components", description = "Get related components (Code Base, Collections, Documentation)")
    public ResponseEntity<?> getRelatedComponents(@PathVariable String apiId, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting related components");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            GeneratedApiResponseDTO apiDetails = this.autoAPIGeneratorEngineService.getApiDetails(requestId, apiId);
            Map<String, Object> relatedComponents = extractRelatedComponents(apiDetails);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "Related components retrieved successfully", relatedComponents);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting related components", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting related components: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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

    @PutMapping({"/gen-engine/{apiId}"})
    @Operation(summary = "Update API", description = "Update an existing generated API")
    public ResponseEntity<?> updateApi(@PathVariable String apiId, @Valid @RequestBody GenerateApiRequestDTO request, BindingResult bindingResult, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "updating API");
        if (authValidation != null)
            return authValidation;
        try {
            String validationErrors = this.requestValidatorHelper.extractValidationErrors(bindingResult);
            if (validationErrors != null)
                return this.responseBuilderHelper.buildValidationErrorResponse(requestId, validationErrors);
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logApiGeneration(requestId, request.getApiName(), performedBy);
            GeneratedApiResponseDTO response = this.autoAPIGeneratorEngineService.updateApi(requestId, apiId, performedBy, request);
            this.loggingHelper.logSuccess(requestId, "API update", "ID: " + apiId);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API updated successfully", response);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "updating API", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while updating API: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping({"/gen-engine/{apiId}"})
    @Operation(summary = "Partially update API", description = "Partially update specific fields of an API")
    public ResponseEntity<?> partialUpdateApi(@PathVariable String apiId, @RequestBody Map<String, Object> updates, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "partially updating API");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            GeneratedApiResponseDTO response = this.autoAPIGeneratorEngineService.partialUpdateApi(requestId, apiId, performedBy, updates);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API updated successfully", response);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "partially updating API", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while updating API: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping({"/gen-engine/{apiId}/sync-components"})
    @Operation(summary = "Sync components", description = "Manually sync code base, collections, and documentation")
    public ResponseEntity<?> syncComponents(@PathVariable String apiId, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "syncing components");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            GeneratedApiEntity api = this.autoAPIGeneratorEngineService.getApiEntity(apiId);
            this.autoAPIGeneratorEngineService.syncGeneratedComponents(api, performedBy);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "Components synced successfully", null);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "syncing components", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while syncing components: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/gen-engine/{apiId}/complete-details"})
    @Operation(summary = "Get Complete API Details", description = "Get all API details exactly as they were captured during generation")
    public ResponseEntity<?> getCompleteApiDetails(@PathVariable String apiId, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "getting complete API details");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiId, performedBy);
            ApiDetailsResponseDTO details = this.autoAPIGeneratorEngineService.getCompleteApiDetails(requestId, apiId);
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "Complete API details retrieved successfully", details);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "getting complete API details", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while getting complete API details: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = {"/gen/{apiId}/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS})
    @Operation(summary = "Execute API by ID", description = "Execute a generated API using its ID in the URL path")
    public ResponseEntity<?> executeApiById(@PathVariable String apiId, HttpServletRequest request, @RequestParam(required = false) MultiValueMap<String, String> formParams, @RequestParam(required = false) Map<String, MultipartFile> fileMap, @RequestParam(required = false) MultipartFile file) {
        String requestId = UUID.randomUUID().toString();
        log.debug("Request ID: {} - Received {} request for API ID: {}", new Object[] { requestId, request.getMethod(), apiId });
        log.debug("Request ID: {} - Full URL: {}", requestId, request.getRequestURL().toString());
        log.debug("Request ID: {} - Content-Type: {}", requestId, request.getContentType());
        try {
            String performedBy = jwtHelper.extractPerformedBy(request);
            String clientIp = this.requestExtractorHelper.extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String contentType = request.getContentType();
            this.loggingHelper.logApiExecution(requestId, apiId, performedBy, contentType, clientIp, userAgent);
            ExecuteApiRequestDTO executeRequest = this.requestExtractorHelper.extractRequestComponents(request, requestId, apiId);
            if (executeRequest.getHttpMethod() == null)
                executeRequest.setHttpMethod(request.getMethod());
            Map<String, MultipartFile> finalFileMap = new HashMap<>();
            if (fileMap != null && !fileMap.isEmpty())
                finalFileMap.putAll(fileMap);
            if (file != null && !file.isEmpty())
                finalFileMap.put("file", file);
            if (!finalFileMap.isEmpty()) {
                executeRequest.setFileMap(finalFileMap);
                executeRequest.setFiles(new ArrayList(finalFileMap.values()));
                if (finalFileMap.size() == 1)
                    executeRequest.setFile(finalFileMap.values().iterator().next());
            }
            if (formParams != null && !formParams.isEmpty()) {
                Map<String, Object> formData = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : (Iterable<Map.Entry<String, List<String>>>)formParams.entrySet()) {
                    List<String> values = entry.getValue();
                    if (values != null && !values.isEmpty()) {
                        if (values.size() == 1) {
                            formData.put(entry.getKey(), values.get(0));
                            continue;
                        }
                        formData.put(entry.getKey(), values);
                    }
                }
                if (!formData.isEmpty())
                    executeRequest.setBody(formData);
            }
            log.info("Request ID: {} - FINAL EXTRACTED REQUEST: pathParams={}, queryParams={}, fileMap={}, file={}, files={}, bodyType={}", new Object[] { requestId,

                    (executeRequest.getPathParams() != null) ? executeRequest.getPathParams().keySet() : "null",
                    (executeRequest.getQueryParams() != null) ? executeRequest.getQueryParams().keySet() : "null",
                    Integer.valueOf((executeRequest.getFileMap() != null) ? executeRequest.getFileMap().size() : 0),
                    (executeRequest.getFile() != null) ? executeRequest.getFile().getOriginalFilename() : "null",
                    Integer.valueOf((executeRequest.getFiles() != null) ? executeRequest.getFiles().size() : 0),
                    (executeRequest.getBody() != null) ? executeRequest.getBody().getClass().getSimpleName() : "null" });
            ExecuteApiResponseDTO response = this.autoAPIGeneratorEngineService.executeApi(requestId, performedBy, apiId, executeRequest, clientIp, userAgent, request);
            log.debug("Request ID: {} - API execution completed with status: {}, protocol: {}", new Object[] { requestId, response
                    .getResponseCode(), response.getProtocolType() });
            String protocolType = response.getProtocolType();
            if ("soap".equalsIgnoreCase(protocolType) &&
                    response.getData() instanceof String) {
                String soapXml = (String)response.getData();
                log.info("Returning raw SOAP XML response (length: {} characters)", Integer.valueOf(soapXml.length()));
                return ResponseEntity.status(response.getResponseCode().intValue())
                        .contentType(MediaType.APPLICATION_XML)
                        .body(soapXml);
            }
            if ("graphql".equalsIgnoreCase(protocolType) &&
                    response.getData() != null) {
                log.info("Returning GraphQL response");
                return ResponseEntity.status(response.getResponseCode().intValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response.getData());
            }
            return ResponseEntity.status(response.getResponseCode().intValue()).body(response);
        } catch (Exception e) {
            return handleException(requestId, apiId, e);
        }
    }

    private ResponseEntity<Map<String, Object>> handleException(String requestId, String apiId, Exception e) {
        this.loggingHelper.logError(requestId, "executing API by ID", e.getMessage(), e);
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("requestId", requestId);
        errorResponse.put("timestamp", (new Date()).toInstant().toString());
        errorResponse.put("status", Integer.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An error occurred while executing API: " + e.getMessage());
        errorResponse.put("path", apiId);
        if (log.isDebugEnabled()) {
            errorResponse.put("exception", e.getClass().getName());
            errorResponse.put("stackTrace", Arrays.<StackTraceElement>stream(e.getStackTrace())
                    .limit(5L)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }
        return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @RequestMapping(value = {"/gen/{apiId}/**"}, method = {RequestMethod.OPTIONS})
    public ResponseEntity<Void> handleOptions() {
        return ((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)ResponseEntity.ok()
                .header("Allow", new String[] { "GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS" })).header("Access-Control-Allow-Methods", new String[] { "GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS" })).header("Access-Control-Allow-Headers", new String[] { "Content-Type, Authorization, X-Requested-With" })).header("Access-Control-Max-Age", new String[] { "3600" })).build();
    }

    @GetMapping({"/gen-engine/check-code"})
    @Operation(summary = "Check API code availability", description = "Check if an API code is available for use")
    public ResponseEntity<?> checkApiCodeAvailability(@RequestParam String apiCode, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        ResponseEntity<?> authValidation = this.jwtHelper.validateAuthorizationHeader(req, "checking API code availability");
        if (authValidation != null)
            return authValidation;
        try {
            String performedBy = this.jwtHelper.extractPerformedBy(req);
            this.loggingHelper.logGetApiDetails(requestId, apiCode, performedBy);
            boolean isAvailable = this.autoAPIGeneratorEngineService.isApiCodeAvailable(apiCode);
            Map<String, Object> response = new HashMap<>();
            response.put("available", Boolean.valueOf(isAvailable));
            response.put("apiCode", apiCode);
            response.put("message", isAvailable ? "API code is available" : "API code already exists");
            return this.responseBuilderHelper.buildSuccessResponse(requestId, "API code availability checked successfully", response);
        } catch (Exception e) {
            this.loggingHelper.logError(requestId, "checking API code availability", e.getMessage(), e);
            return this.responseBuilderHelper.buildErrorResponse(requestId, "An error occurred while checking API code availability: " + e

                    .getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
