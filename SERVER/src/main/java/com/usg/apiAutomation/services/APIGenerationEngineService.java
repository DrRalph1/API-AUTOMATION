package com.usg.apiAutomation.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.services.apiGenerationEngine.ApiValidatorService;
import com.usg.apiAutomation.utils.LoggerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class APIGenerationEngineService {

    private final GeneratedApiRepository generatedApiRepository;
    private final ApiExecutionLogRepository executionLogRepository;
    private final ApiTestRepository apiTestRepository;
    private final ObjectMapper objectMapper;
    private final LoggerUtil loggerUtil;
    private final ApiValidatorService validatorService;

    @Transactional
    public GeneratedApiResponseDTO generateApi(String requestId, String performedBy, GenerateApiRequestDTO request) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Generating API: " + request.getApiName() + " by: " + performedBy);

            // Validate API code uniqueness
            if (generatedApiRepository.existsByApiCode(request.getApiCode())) {
                throw new RuntimeException("API code already exists: " + request.getApiCode());
            }

            // Create main API entity
            GeneratedApiEntity api = GeneratedApiEntity.builder()
                    .apiName(request.getApiName())
                    .apiCode(request.getApiCode())
                    .description(request.getDescription())
                    .version(request.getVersion())
                    .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                    .httpMethod(request.getHttpMethod())
                    .basePath(request.getBasePath())
                    .endpointPath(request.getEndpointPath())
                    .category(request.getCategory())
                    .owner(request.getOwner())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(performedBy)
                    .isActive(true)
                    .totalCalls(0L)
                    .tags(request.getTags())
                    .sourceObjectInfo(request.getSourceObject() != null ?
                            objectMapper.writeValueAsString(request.getSourceObject()) : null)
                    .build();

            // Save schema config
            if (request.getSchemaConfig() != null) {
                ApiSchemaConfigEntity schemaConfig = mapToSchemaConfigEntity(request.getSchemaConfig(), api);
                api.setSchemaConfig(schemaConfig);
            }

            // Save auth config
            if (request.getAuthConfig() != null) {
                ApiAuthConfigEntity authConfig = mapToAuthConfigEntity(request.getAuthConfig(), api);
                api.setAuthConfig(authConfig);
            }

            // Save request config
            if (request.getRequestBody() != null) {
                ApiRequestConfigEntity requestConfig = mapToRequestConfigEntity(request.getRequestBody(), api);
                api.setRequestConfig(requestConfig);
            }

            // Save response config
            if (request.getResponseBody() != null) {
                ApiResponseConfigEntity responseConfig = mapToResponseConfigEntity(request.getResponseBody(), api);
                api.setResponseConfig(responseConfig);
            }

            // Save settings
            if (request.getSettings() != null) {
                ApiSettingsEntity settings = mapToSettingsEntity(request.getSettings(), api);
                api.setSettings(settings);
            }

            // Save parameters
            if (request.getParameters() != null) {
                List<ApiParameterEntity> parameters = new ArrayList<>();
                for (int i = 0; i < request.getParameters().size(); i++) {
                    ApiParameterDTO paramDto = request.getParameters().get(i);
                    ApiParameterEntity param = mapToParameterEntity(paramDto, api);
                    param.setPosition(i);
                    parameters.add(param);
                }
                api.setParameters(parameters);
            }

            // Save response mappings
            if (request.getResponseMappings() != null) {
                List<ApiResponseMappingEntity> mappings = new ArrayList<>();
                for (int i = 0; i < request.getResponseMappings().size(); i++) {
                    ApiResponseMappingDTO mappingDto = request.getResponseMappings().get(i);
                    ApiResponseMappingEntity mapping = mapToResponseMappingEntity(mappingDto, api);
                    mapping.setPosition(i);
                    mappings.add(mapping);
                }
                api.setResponseMappings(mappings);
            }

            // Save headers
            if (request.getHeaders() != null) {
                List<ApiHeaderEntity> headers = request.getHeaders().stream()
                        .map(headerDto -> mapToHeaderEntity(headerDto, api))
                        .collect(Collectors.toList());
                api.setHeaders(headers);
            }

            // Save tests
            if (request.getTests() != null) {
                List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
                api.setTests(tests);
            }

            // Save to database
            GeneratedApiEntity savedApi = generatedApiRepository.save(api);

            // Generate code files
            Map<String, String> generatedFiles = generateApiCode(savedApi);

            // Build response
            GeneratedApiResponseDTO response = mapToResponse(savedApi);
            response.setGeneratedFiles(generatedFiles);

            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("parametersCount", savedApi.getParameters() != null ? savedApi.getParameters().size() : 0);
            metadata.put("responseMappingsCount", savedApi.getResponseMappings() != null ? savedApi.getResponseMappings().size() : 0);
            metadata.put("headersCount", savedApi.getHeaders() != null ? savedApi.getHeaders().size() : 0);
            metadata.put("generatedAt", LocalDateTime.now().toString());
            response.setMetadata(metadata);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + savedApi.getId());

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error generating API: " + e.getMessage());
            throw new RuntimeException("Failed to generate API: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ExecuteApiResponseDTO executeApi(String requestId, String performedBy,
                                            String apiId, ExecuteApiRequestDTO executeRequest,
                                            String clientIp, String userAgent) {
        long startTime = System.currentTimeMillis();

        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Executing API: " + apiId + " by: " + performedBy);

            // Get API configuration
            GeneratedApiEntity api = generatedApiRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Validate authentication
            if (!validatorService.validateAuthentication(api, executeRequest)) {
                return createErrorResponse(requestId, 401, "Authentication failed", startTime);
            }

            // Validate authorization
            if (!validatorService.validateAuthorization(api, performedBy)) {
                return createErrorResponse(requestId, 403, "Authorization failed", startTime);
            }

            // Validate rate limiting
            if (!validatorService.checkRateLimit(api, clientIp)) {
                return createErrorResponse(requestId, 429, "Rate limit exceeded", startTime);
            }

            // Validate parameters
            Map<String, String> validationErrors = validatorService.validateParameters(api, executeRequest);
            if (!validationErrors.isEmpty()) {
                ExecuteApiResponseDTO response = createErrorResponse(requestId, 400, "Validation failed", startTime);
                response.setData(validationErrors);
                return response;
            }

            // Execute the API against Oracle database
            Object result = executeAgainstOracle(api, executeRequest);

            // Format response based on configuration
            Object formattedResponse = formatResponse(api, result);

            long executionTime = System.currentTimeMillis() - startTime;

            // Update API stats
            api.setTotalCalls(api.getTotalCalls() + 1);
            api.setLastCalledAt(LocalDateTime.now());
            generatedApiRepository.save(api);

            // Log execution
            logExecution(api, executeRequest, formattedResponse, 200, executionTime,
                    performedBy, clientIp, userAgent, null);

            // Build response
            ExecuteApiResponseDTO response = ExecuteApiResponseDTO.builder()
                    .requestId(requestId)
                    .statusCode(200)
                    .data(formattedResponse)
                    .metadata(buildResponseMetadata(api, executionTime))
                    .executionTimeMs(executionTime)
                    .success(true)
                    .message("API executed successfully")
                    .build();

            // Add response headers if configured
            if (api.getHeaders() != null) {
                Map<String, String> responseHeaders = api.getHeaders().stream()
                        .filter(h -> Boolean.TRUE.equals(h.getIsResponseHeader()))
                        .collect(Collectors.toMap(
                                ApiHeaderEntity::getKey,
                                h -> h.getValue() != null ? h.getValue() : ""
                        ));
                response.setHeaders(responseHeaders);
            }

            return response;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error executing API: " + e.getMessage());

            // Log error execution
            logExecution(null, executeRequest, null, 500, executionTime,
                    performedBy, clientIp, userAgent, e.getMessage());

            return createErrorResponse(requestId, 500, e.getMessage(), startTime);
        }
    }

    @Transactional
    public ApiTestResultDTO testApi(String requestId, String performedBy,
                                    String apiId, ApiTestRequestDTO testRequest) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Testing API: " + apiId + " with test: " + testRequest.getTestName());

            GeneratedApiEntity api = generatedApiRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            long startTime = System.currentTimeMillis();

            // Execute test
            ExecuteApiRequestDTO executeRequest = ExecuteApiRequestDTO.builder()
                    .pathParams(testRequest.getPathParams())
                    .queryParams(testRequest.getQueryParams())
                    .headers(testRequest.getHeaders())
                    .body(testRequest.getBody())
                    .requestId(requestId)
                    .build();

            ExecuteApiResponseDTO executionResult = executeApi(requestId, performedBy,
                    apiId, executeRequest, "127.0.0.1", "API-Test");

            long executionTime = System.currentTimeMillis() - startTime;

            // Compare with expected response
            boolean passed = compareResponses(executionResult, testRequest.getExpectedResponse());

            // Save test result
            ApiTestEntity testEntity = ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName(testRequest.getTestName())
                    .testType(testRequest.getTestType())
                    .testData(objectMapper.writeValueAsString(testRequest))
                    .expectedResponse(objectMapper.writeValueAsString(testRequest.getExpectedResponse()))
                    .actualResponse(objectMapper.writeValueAsString(executionResult))
                    .status(passed ? "PASSED" : "FAILED")
                    .executionTimeMs(executionTime)
                    .executedAt(LocalDateTime.now())
                    .executedBy(performedBy)
                    .build();

            apiTestRepository.save(testEntity);

            return ApiTestResultDTO.builder()
                    .testName(testRequest.getTestName())
                    .passed(passed)
                    .executionTimeMs(executionTime)
                    .statusCode(executionResult.getStatusCode())
                    .actualResponse(executionResult.getData())
                    .message(passed ? "Test passed" : "Test failed - response mismatch")
                    .build();

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error testing API: " + e.getMessage());
            throw new RuntimeException("Failed to test API: " + e.getMessage(), e);
        }
    }

    public GeneratedApiResponseDTO getApiDetails(String requestId, String apiId) {
        try {
            GeneratedApiEntity api = generatedApiRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            GeneratedApiResponseDTO response = mapToResponse(api);

            // Add execution stats
            response.setTotalCalls(api.getTotalCalls());
            response.setLastCalledAt(api.getLastCalledAt());

            // Add average execution time
            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                Map<String, Object> metadata = response.getMetadata() != null ?
                        response.getMetadata() : new HashMap<>();
                metadata.put("averageExecutionTimeMs", avgTime);
                response.setMetadata(metadata);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());
            throw new RuntimeException("Failed to get API details: " + e.getMessage(), e);
        }
    }

    public ApiAnalyticsDTO getApiAnalytics(String requestId, String apiId,
                                           LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<ApiExecutionLogEntity> logs = executionLogRepository
                    .findByGeneratedApiIdAndExecutedAtBetween(apiId, startDate, endDate);

            // Calculate statistics
            long totalCalls = logs.size();
            double avgExecutionTime = logs.stream()
                    .mapToLong(ApiExecutionLogEntity::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);
            long totalErrors = logs.stream()
                    .filter(log -> log.getResponseStatus() >= 400)
                    .count();
            double successRate = totalCalls > 0 ?
                    ((totalCalls - totalErrors) * 100.0 / totalCalls) : 0.0;

            // Status code distribution
            Map<Integer, Long> statusDistribution = logs.stream()
                    .collect(Collectors.groupingBy(
                            ApiExecutionLogEntity::getResponseStatus,
                            Collectors.counting()
                    ));

            // Daily call stats
            List<Object[]> dailyStats = executionLogRepository
                    .getDailyCallStats(apiId, startDate);

            return ApiAnalyticsDTO.builder()
                    .apiId(apiId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalCalls(totalCalls)
                    .averageExecutionTimeMs(avgExecutionTime)
                    .totalErrors(totalErrors)
                    .successRate(successRate)
                    .statusDistribution(statusDistribution)
                    .dailyCallStats(dailyStats.stream()
                            .collect(Collectors.toMap(
                                    stat -> stat[0].toString(),
                                    stat -> (Long) stat[1]
                            )))
                    .build();

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API analytics: " + e.getMessage());
            throw new RuntimeException("Failed to get API analytics: " + e.getMessage(), e);
        }
    }

    public Map<String, String> generateApiCode(GeneratedApiEntity api) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Generate PL/SQL package
        generatedFiles.put("plsql", generatePlSqlPackage(api));

        // Generate OpenAPI spec if enabled
        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGenerateSwagger())) {
            generatedFiles.put("openapi", generateOpenApiSpec(api));
        }

        // Generate Postman collection if enabled
        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGeneratePostman())) {
            generatedFiles.put("postman", generatePostmanCollection(api));
        }

        return generatedFiles;
    }

    private String generatePlSqlPackage(GeneratedApiEntity api) {
        StringBuilder sb = new StringBuilder();

        sb.append("-- ============================================================\n");
        sb.append("-- Generated API Package: ").append(api.getApiName()).append("\n");
        sb.append("-- Generated: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("-- Version: ").append(api.getVersion()).append("\n");
        sb.append("-- API Code: ").append(api.getApiCode()).append("\n");
        sb.append("-- ============================================================\n\n");

        if (api.getSchemaConfig() != null) {
            sb.append("-- Source Object: ")
                    .append(api.getSchemaConfig().getSchemaName()).append(".")
                    .append(api.getSchemaConfig().getObjectName())
                    .append(" (").append(api.getSchemaConfig().getObjectType()).append(")\n");
            sb.append("-- Operation: ").append(api.getSchemaConfig().getOperation()).append("\n\n");
        }

        sb.append("CREATE OR REPLACE PACKAGE ").append(api.getApiCode()).append("_PKG AS\n\n");
        sb.append("  -- Main procedure\n");
        sb.append("  PROCEDURE execute_api(\n");

        // Add parameters
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (int i = 0; i < api.getParameters().size(); i++) {
                var param = api.getParameters().get(i);
                sb.append("    p_").append(param.getKey()).append(" IN ").append(param.getOracleType());
                if (Boolean.TRUE.equals(param.getRequired())) {
                    sb.append(" NOT NULL");
                }
                if (param.getDefaultValue() != null && !param.getDefaultValue().isEmpty()) {
                    sb.append(" DEFAULT ").append(param.getDefaultValue());
                }
                if (i < api.getParameters().size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
        } else {
            sb.append("    -- No parameters\n");
        }
        sb.append("  );\n\n");

        sb.append("END ").append(api.getApiCode()).append("_PKG;\n");
        sb.append("/\n\n");

        sb.append("CREATE OR REPLACE PACKAGE BODY ").append(api.getApiCode()).append("_PKG AS\n\n");
        sb.append("  PROCEDURE execute_api(\n");

        // Add parameters again for body
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (int i = 0; i < api.getParameters().size(); i++) {
                var param = api.getParameters().get(i);
                sb.append("    p_").append(param.getKey()).append(" IN ").append(param.getOracleType());
                if (i < api.getParameters().size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
        } else {
            sb.append("    -- No parameters\n");
        }
        sb.append("  ) IS\n");
        sb.append("    v_cursor SYS_REFCURSOR;\n");
        sb.append("    v_start_time TIMESTAMP := SYSTIMESTAMP;\n");
        sb.append("  BEGIN\n\n");

        // Generate operation logic based on config
        if (api.getSchemaConfig() != null) {
            switch (api.getSchemaConfig().getOperation()) {
                case "SELECT":
                    sb.append("    OPEN v_cursor FOR\n");
                    sb.append("    SELECT ");
                    if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                        sb.append(api.getResponseMappings().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getIncludeInResponse()))
                                .map(ApiResponseMappingEntity::getDbColumn)
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("*"));
                    } else {
                        sb.append("*");
                    }
                    sb.append("\n    FROM ")
                            .append(api.getSchemaConfig().getSchemaName()).append(".")
                            .append(api.getSchemaConfig().getObjectName());
                    sb.append("\n    WHERE 1=1\n");

                    // Add parameter filters
                    if (api.getParameters() != null) {
                        for (var param : api.getParameters()) {
                            if ("query".equals(param.getParameterType()) ||
                                    "path".equals(param.getParameterType())) {
                                sb.append("    AND ")
                                        .append(param.getDbColumn() != null ? param.getDbColumn() : param.getKey())
                                        .append(" = p_").append(param.getKey()).append("\n");
                            }
                        }
                    }
                    break;

                case "INSERT":
                    sb.append("    INSERT INTO ")
                            .append(api.getSchemaConfig().getSchemaName()).append(".")
                            .append(api.getSchemaConfig().getObjectName())
                            .append(" (");

                    // Add columns
                    if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                        String columns = api.getResponseMappings().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getIncludeInResponse()))
                                .map(ApiResponseMappingEntity::getDbColumn)
                                .collect(Collectors.joining(", "));
                        sb.append(columns);
                    }
                    sb.append(") VALUES (");

                    // Add values from parameters
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        String values = api.getParameters().stream()
                                .map(p -> "p_" + p.getKey())
                                .collect(Collectors.joining(", "));
                        sb.append(values);
                    }
                    sb.append(");\n");
                    break;

                case "UPDATE":
                    sb.append("    UPDATE ")
                            .append(api.getSchemaConfig().getSchemaName()).append(".")
                            .append(api.getSchemaConfig().getObjectName())
                            .append("\n    SET ");

                    // Add set clauses
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        String setClauses = api.getParameters().stream()
                                .filter(p -> !"query".equals(p.getParameterType()) && !"path".equals(p.getParameterType()))
                                .map(p -> p.getDbColumn() + " = p_" + p.getKey())
                                .collect(Collectors.joining(", "));
                        sb.append(setClauses);
                    }
                    sb.append("\n    WHERE 1=1\n");

                    // Add where conditions from path/query parameters
                    if (api.getParameters() != null) {
                        for (var param : api.getParameters()) {
                            if ("query".equals(param.getParameterType()) ||
                                    "path".equals(param.getParameterType())) {
                                sb.append("    AND ")
                                        .append(param.getDbColumn() != null ? param.getDbColumn() : param.getKey())
                                        .append(" = p_").append(param.getKey()).append("\n");
                            }
                        }
                    }
                    break;

                case "DELETE":
                    sb.append("    DELETE FROM ")
                            .append(api.getSchemaConfig().getSchemaName()).append(".")
                            .append(api.getSchemaConfig().getObjectName());
                    sb.append("\n    WHERE 1=1\n");

                    // Add where conditions from path/query parameters
                    if (api.getParameters() != null) {
                        for (var param : api.getParameters()) {
                            if ("query".equals(param.getParameterType()) ||
                                    "path".equals(param.getParameterType())) {
                                sb.append("    AND ")
                                        .append(param.getDbColumn() != null ? param.getDbColumn() : param.getKey())
                                        .append(" = p_").append(param.getKey()).append("\n");
                            }
                        }
                    }
                    break;

                case "EXECUTE":
                    sb.append("    -- Execute procedure/function\n");
                    sb.append("    ");
                    if (api.getSchemaConfig().getObjectType().equals("FUNCTION")) {
                        sb.append("v_result := ");
                    }
                    sb.append(api.getSchemaConfig().getSchemaName()).append(".")
                            .append(api.getSchemaConfig().getObjectName()).append("(\n");

                    // Add parameters
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        for (int i = 0; i < api.getParameters().size(); i++) {
                            var param = api.getParameters().get(i);
                            sb.append("        p_").append(param.getKey());
                            if (i < api.getParameters().size() - 1) {
                                sb.append(",");
                            }
                            sb.append("\n");
                        }
                    }
                    sb.append("    );\n");
                    break;
            }
        }

        sb.append("\n\n    -- Log execution\n");
        sb.append("    DBMS_OUTPUT.PUT_LINE('API executed in: ' || (SYSTIMESTAMP - v_start_time));\n\n");

        sb.append("    -- Return cursor for SELECT operations\n");
        sb.append("    IF '" + (api.getSchemaConfig() != null ? api.getSchemaConfig().getOperation() : "") + "' = 'SELECT' THEN\n");
        sb.append("      DBMS_SQL.RETURN_RESULT(v_cursor);\n");
        sb.append("    END IF;\n\n");

        sb.append("  EXCEPTION\n");
        sb.append("    WHEN OTHERS THEN\n");
        sb.append("      DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);\n");
        sb.append("      RAISE;\n");
        sb.append("  END execute_api;\n\n");

        sb.append("END ").append(api.getApiCode()).append("_PKG;\n");
        sb.append("/\n");

        return sb.toString();
    }

    private String generateOpenApiSpec(GeneratedApiEntity api) {
        Map<String, Object> spec = new HashMap<>();

        // OpenAPI version
        spec.put("openapi", "3.0.0");

        // Info
        Map<String, Object> info = new HashMap<>();
        info.put("title", api.getApiName());
        info.put("description", api.getDescription());
        info.put("version", api.getVersion());

        if (api.getOwner() != null) {
            Map<String, Object> contact = new HashMap<>();
            contact.put("name", api.getOwner());
            info.put("contact", contact);
        }
        spec.put("info", info);

        // Servers
        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> server = new HashMap<>();
        server.put("url", "{baseUrl}" + (api.getBasePath() != null ? api.getBasePath() : ""));
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> baseUrlVar = new HashMap<>();
        baseUrlVar.put("default", "https://api.example.com");
        variables.put("baseUrl", baseUrlVar);
        server.put("variables", variables);
        servers.add(server);
        spec.put("servers", servers);

        // Paths
        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> pathItem = new HashMap<>();
        Map<String, Object> operation = new HashMap<>();

        operation.put("summary", api.getApiName());
        operation.put("description", api.getDescription());
        operation.put("operationId", api.getApiCode().toLowerCase());
        operation.put("tags", api.getTags() != null ? api.getTags() : Arrays.asList("default"));

        // Parameters
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (var param : api.getParameters()) {
                Map<String, Object> paramSpec = new HashMap<>();
                paramSpec.put("name", param.getKey());
                paramSpec.put("in", param.getParameterType());
                paramSpec.put("description", param.getDescription());
                paramSpec.put("required", param.getRequired());

                Map<String, Object> schema = new HashMap<>();
                schema.put("type", param.getApiType());
                if (param.getExample() != null) {
                    schema.put("example", param.getExample());
                }
                if (param.getValidationPattern() != null && !param.getValidationPattern().isEmpty()) {
                    schema.put("pattern", param.getValidationPattern());
                }
                paramSpec.put("schema", schema);

                parameters.add(paramSpec);
            }
            operation.put("parameters", parameters);
        }

        // Request Body
        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("description", "Request body");
            requestBody.put("required", true);

            Map<String, Object> content = new HashMap<>();
            Map<String, Object> mediaType = new HashMap<>();

            try {
                mediaType.put("schema", objectMapper.readValue(api.getRequestConfig().getSample(), Map.class));
            } catch (Exception e) {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                mediaType.put("schema", schema);
            }

            content.put(api.getRequestConfig().getSchemaType() != null ?
                    api.getRequestConfig().getSchemaType() : "application/json", mediaType);
            requestBody.put("content", content);

            operation.put("requestBody", requestBody);
        }

        // Responses
        Map<String, Object> responses = new HashMap<>();

        // Success response (200)
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "Successful response");

        if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> mediaType = new HashMap<>();

            try {
                mediaType.put("schema", objectMapper.readValue(api.getResponseConfig().getSuccessSchema(), Map.class));
            } catch (Exception e) {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");

                // Add properties from response mappings
                if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                    Map<String, Object> properties = new HashMap<>();
                    for (var mapping : api.getResponseMappings()) {
                        if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                            Map<String, Object> propSchema = new HashMap<>();
                            propSchema.put("type", mapping.getApiType());
                            if (mapping.getFormat() != null && !mapping.getFormat().isEmpty()) {
                                propSchema.put("format", mapping.getFormat());
                            }
                            if (Boolean.TRUE.equals(mapping.getNullable())) {
                                propSchema.put("nullable", true);
                            }
                            properties.put(mapping.getApiField(), propSchema);
                        }
                    }
                    schema.put("properties", properties);
                }

                mediaType.put("schema", schema);
            }

            content.put(api.getResponseConfig().getContentType() != null ?
                    api.getResponseConfig().getContentType() : "application/json", mediaType);
            successResponse.put("content", content);
        }

        responses.put("200", successResponse);
        responses.put("400", Map.of("description", "Bad Request"));
        responses.put("401", Map.of("description", "Unauthorized"));
        responses.put("403", Map.of("description", "Forbidden"));
        responses.put("404", Map.of("description", "Not Found"));
        responses.put("429", Map.of("description", "Too Many Requests"));
        responses.put("500", Map.of("description", "Internal Server Error"));

        operation.put("responses", responses);

        // Security
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            List<Map<String, List<String>>> security = new ArrayList<>();
            Map<String, List<String>> securityReq = new HashMap<>();
            securityReq.put(api.getAuthConfig().getAuthType().toLowerCase(), new ArrayList<>());
            security.add(securityReq);
            operation.put("security", security);
        }

        pathItem.put(api.getHttpMethod().toLowerCase(), operation);
        paths.put(api.getEndpointPath(), pathItem);
        spec.put("paths", paths);

        // Components (Security Schemes)
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            Map<String, Object> components = new HashMap<>();
            Map<String, Object> securitySchemes = new HashMap<>();

            Map<String, Object> authScheme = new HashMap<>();
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    authScheme.put("type", "apiKey");
                    authScheme.put("name", api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                    authScheme.put("in", api.getAuthConfig().getApiKeyLocation() != null ?
                            api.getAuthConfig().getApiKeyLocation() : "header");
                    break;
                case "BASIC":
                    authScheme.put("type", "http");
                    authScheme.put("scheme", "basic");
                    break;
                case "JWT":
                case "BEARER":
                    authScheme.put("type", "http");
                    authScheme.put("scheme", "bearer");
                    authScheme.put("bearerFormat", "JWT");
                    break;
                case "OAUTH2":
                    authScheme.put("type", "oauth2");
                    Map<String, Object> flows = new HashMap<>();
                    Map<String, Object> clientCredentials = new HashMap<>();
                    clientCredentials.put("tokenUrl", api.getAuthConfig().getOauthTokenUrl() != null ?
                            api.getAuthConfig().getOauthTokenUrl() : "https://auth.example.com/token");
                    if (api.getAuthConfig().getOauthScopes() != null) {
                        Map<String, String> scopes = new HashMap<>();
                        for (String scope : api.getAuthConfig().getOauthScopes()) {
                            scopes.put(scope, scope + " access");
                        }
                        clientCredentials.put("scopes", scopes);
                    }
                    flows.put("clientCredentials", clientCredentials);
                    authScheme.put("flows", flows);
                    break;
            }

            securitySchemes.put(api.getAuthConfig().getAuthType().toLowerCase(), authScheme);
            components.put("securitySchemes", securitySchemes);
            spec.put("components", components);
        }

        // Convert to JSON string
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec);
        } catch (Exception e) {
            log.error("Error generating OpenAPI spec: {}", e.getMessage());
            return "{}";
        }
    }

    private String generatePostmanCollection(GeneratedApiEntity api) {
        Map<String, Object> collection = new HashMap<>();

        // Info
        Map<String, Object> info = new HashMap<>();
        info.put("name", api.getApiName());
        info.put("description", api.getDescription());
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put("info", info);

        // Items
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", api.getApiName());

        // Request
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getHttpMethod());

        // URL
        Map<String, Object> url = new HashMap<>();
        String fullPath = (api.getBasePath() != null ? api.getBasePath() : "") +
                (api.getEndpointPath() != null ? api.getEndpointPath() : "");
        url.put("raw", "{{baseUrl}}" + fullPath);
        url.put("host", Arrays.asList("{{baseUrl}}"));

        // Parse path
        if (fullPath != null && !fullPath.isEmpty()) {
            String[] pathSegments = fullPath.split("/");
            List<String> pathList = new ArrayList<>();
            for (String segment : pathSegments) {
                if (!segment.isEmpty()) {
                    pathList.add(segment);
                }
            }
            url.put("path", pathList);
        }

        // Query parameters
        if (api.getParameters() != null) {
            List<Map<String, Object>> queryParams = new ArrayList<>();
            for (var param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    Map<String, Object> queryParam = new HashMap<>();
                    queryParam.put("key", param.getKey());
                    queryParam.put("value", param.getExample() != null ? param.getExample() : "");
                    queryParam.put("description", param.getDescription());
                    queryParam.put("disabled", !Boolean.TRUE.equals(param.getRequired()));
                    queryParams.add(queryParam);
                }
            }
            if (!queryParams.isEmpty()) {
                url.put("query", queryParams);
            }
        }

        request.put("url", url);

        // Headers
        if (api.getHeaders() != null || (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()))) {
            List<Map<String, Object>> headers = new ArrayList<>();

            // Add configured headers
            if (api.getHeaders() != null) {
                for (var header : api.getHeaders()) {
                    if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                        Map<String, Object> headerSpec = new HashMap<>();
                        headerSpec.put("key", header.getKey());
                        headerSpec.put("value", header.getValue() != null ? header.getValue() : "");
                        headerSpec.put("description", header.getDescription());
                        headerSpec.put("disabled", !Boolean.TRUE.equals(header.getRequired()));
                        headers.add(headerSpec);
                    }
                }
            }

            // Add auth headers based on auth type
            if (api.getAuthConfig() != null) {
                switch (api.getAuthConfig().getAuthType()) {
                    case "API_KEY":
                        Map<String, Object> apiKeyHeader = new HashMap<>();
                        apiKeyHeader.put("key", api.getAuthConfig().getApiKeyHeader() != null ?
                                api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                        apiKeyHeader.put("value", api.getAuthConfig().getApiKeyValue() != null ?
                                api.getAuthConfig().getApiKeyValue() : "{{apiKey}}");
                        apiKeyHeader.put("description", "API Key for authentication");
                        apiKeyHeader.put("disabled", false);
                        headers.add(apiKeyHeader);

                        if (api.getAuthConfig().getApiKeySecret() != null) {
                            Map<String, Object> apiSecretHeader = new HashMap<>();
                            apiSecretHeader.put("key", "X-API-Secret");
                            apiSecretHeader.put("value", "{{apiSecret}}");
                            apiSecretHeader.put("description", "API Secret for authentication");
                            apiSecretHeader.put("disabled", false);
                            headers.add(apiSecretHeader);
                        }
                        break;

                    case "BASIC":
                        Map<String, Object> basicHeader = new HashMap<>();
                        basicHeader.put("key", "Authorization");
                        basicHeader.put("value", "Basic {{base64Credentials}}");
                        basicHeader.put("description", "Basic Authentication");
                        basicHeader.put("disabled", false);
                        headers.add(basicHeader);
                        break;

                    case "JWT":
                    case "BEARER":
                        Map<String, Object> bearerHeader = new HashMap<>();
                        bearerHeader.put("key", "Authorization");
                        bearerHeader.put("value", "Bearer {{jwtToken}}");
                        bearerHeader.put("description", "Bearer Token Authentication");
                        bearerHeader.put("disabled", false);
                        headers.add(bearerHeader);
                        break;
                }
            }

            if (!headers.isEmpty()) {
                request.put("header", headers);
            }
        }

        // Body
        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            Map<String, Object> body = new HashMap<>();
            body.put("mode", "raw");
            body.put("raw", api.getRequestConfig().getSample());

            if (api.getRequestConfig().getSchemaType() != null) {
                Map<String, String> options = new HashMap<>();
                options.put("raw", api.getRequestConfig().getSchemaType());
                body.put("options", Map.of("raw", options));
            }

            request.put("body", body);
        }

        item.put("request", request);

        // Add response examples if available
        if (api.getResponseConfig() != null) {
            List<Map<String, Object>> responses = new ArrayList<>();

            if (api.getResponseConfig().getSuccessSchema() != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("name", "Success Response");
                response.put("originalRequest", request);
                response.put("status", "OK");
                response.put("code", 200);
                response.put("body", api.getResponseConfig().getSuccessSchema());
                responses.add(response);
            }

            if (api.getResponseConfig().getErrorSchema() != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("name", "Error Response");
                response.put("originalRequest", request);
                response.put("status", "Bad Request");
                response.put("code", 400);
                response.put("body", api.getResponseConfig().getErrorSchema());
                responses.add(response);
            }

            if (!responses.isEmpty()) {
                item.put("response", responses);
            }
        }

        items.add(item);
        collection.put("item", items);

        // Variables
        List<Map<String, Object>> variables = new ArrayList<>();

        Map<String, Object> baseUrlVar = new HashMap<>();
        baseUrlVar.put("key", "baseUrl");
        baseUrlVar.put("value", "https://api.example.com");
        baseUrlVar.put("type", "string");
        variables.add(baseUrlVar);

        // Add auth variables based on auth type
        if (api.getAuthConfig() != null) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    if (api.getAuthConfig().getApiKeyValue() != null) {
                        Map<String, Object> apiKeyVar = new HashMap<>();
                        apiKeyVar.put("key", "apiKey");
                        apiKeyVar.put("value", api.getAuthConfig().getApiKeyValue());
                        apiKeyVar.put("type", "string");
                        variables.add(apiKeyVar);
                    }
                    if (api.getAuthConfig().getApiKeySecret() != null) {
                        Map<String, Object> apiSecretVar = new HashMap<>();
                        apiSecretVar.put("key", "apiSecret");
                        apiSecretVar.put("value", api.getAuthConfig().getApiKeySecret());
                        apiSecretVar.put("type", "string");
                        variables.add(apiSecretVar);
                    }
                    break;

                case "BASIC":
                    if (api.getAuthConfig().getBasicUsername() != null &&
                            api.getAuthConfig().getBasicPassword() != null) {
                        String credentials = api.getAuthConfig().getBasicUsername() + ":" +
                                api.getAuthConfig().getBasicPassword();
                        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

                        Map<String, Object> basicVar = new HashMap<>();
                        basicVar.put("key", "base64Credentials");
                        basicVar.put("value", encoded);
                        basicVar.put("type", "string");
                        variables.add(basicVar);
                    }
                    break;

                case "JWT":
                    if (api.getAuthConfig().getJwtSecret() != null) {
                        Map<String, Object> jwtVar = new HashMap<>();
                        jwtVar.put("key", "jwtToken");
                        jwtVar.put("value", api.getAuthConfig().getJwtSecret());
                        jwtVar.put("type", "string");
                        variables.add(jwtVar);
                    }
                    break;
            }
        }

        collection.put("variable", variables);

        // Convert to JSON string
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (Exception e) {
            log.error("Error generating Postman collection: {}", e.getMessage());
            return "{}";
        }
    }

    // Private helper methods

    private ApiSchemaConfigEntity mapToSchemaConfigEntity(ApiSchemaConfigDTO dto, GeneratedApiEntity api) {
        return ApiSchemaConfigEntity.builder()
                .generatedApi(api)
                .schemaName(dto.getSchemaName())
                .objectType(dto.getObjectType())
                .objectName(dto.getObjectName())
                .operation(dto.getOperation())
                .primaryKeyColumn(dto.getPrimaryKeyColumn())
                .sequenceName(dto.getSequenceName())
                .enablePagination(dto.getEnablePagination())
                .pageSize(dto.getPageSize())
                .enableSorting(dto.getEnableSorting())
                .defaultSortColumn(dto.getDefaultSortColumn())
                .defaultSortDirection(dto.getDefaultSortDirection())
                .isSynonym(dto.getIsSynonym())
                .targetType(dto.getTargetType())
                .targetName(dto.getTargetName())
                .targetOwner(dto.getTargetOwner())
                .build();
    }

    private ApiAuthConfigEntity mapToAuthConfigEntity(ApiAuthConfigDTO dto, GeneratedApiEntity api) {
        ApiAuthConfigEntity entity = ApiAuthConfigEntity.builder()
                .generatedApi(api)
                .authType(dto.getAuthType())
                .apiKeyHeader(dto.getApiKeyHeader())
                .apiKeyValue(dto.getApiKeyValue())
                .apiKeySecret(dto.getApiKeySecret())
                .apiKeyLocation(dto.getApiKeyLocation())
                .apiKeyPrefix(dto.getApiKeyPrefix())
                .basicUsername(dto.getBasicUsername())
                .basicPassword(dto.getBasicPassword())
                .basicRealm(dto.getBasicRealm())
                .jwtSecret(dto.getJwtSecret())
                .jwtIssuer(dto.getJwtIssuer())
                .jwtAudience(dto.getJwtAudience())
                .jwtExpiration(dto.getJwtExpiration())
                .jwtAlgorithm(dto.getJwtAlgorithm())
                .oauthClientId(dto.getOauthClientId())
                .oauthClientSecret(dto.getOauthClientSecret())
                .oauthTokenUrl(dto.getOauthTokenUrl())
                .oauthAuthUrl(dto.getOauthAuthUrl())
                .oauthScopes(dto.getOauthScopes())
                .requiredRoles(dto.getRequiredRoles())
                .customAuthFunction(dto.getCustomAuthFunction())
                .validateSession(dto.getValidateSession())
                .checkObjectPrivileges(dto.getCheckObjectPrivileges())
                .ipWhitelist(dto.getIpWhitelist())
                .rateLimitRequests(dto.getRateLimitRequests())
                .rateLimitPeriod(dto.getRateLimitPeriod())
                .auditLevel(dto.getAuditLevel())
                .corsOrigins(dto.getCorsOrigins())
                .corsCredentials(dto.getCorsCredentials())
                .build();

        return entity;
    }

    private ApiRequestConfigEntity mapToRequestConfigEntity(ApiRequestConfigDTO dto, GeneratedApiEntity api) {
        try {
            return ApiRequestConfigEntity.builder()
                    .generatedApi(api)
                    .schemaType(dto.getSchemaType())
                    .sample(dto.getSample())
                    .maxSize(dto.getMaxSize())
                    .validateSchema(dto.getValidateSchema())
                    .allowedMediaTypes(dto.getAllowedMediaTypes() != null ?
                            objectMapper.writeValueAsString(dto.getAllowedMediaTypes()) : null)
                    .requiredFields(dto.getRequiredFields())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map request config: " + e.getMessage());
        }
    }

    private ApiResponseConfigEntity mapToResponseConfigEntity(ApiResponseConfigDTO dto, GeneratedApiEntity api) {
        return ApiResponseConfigEntity.builder()
                .generatedApi(api)
                .successSchema(dto.getSuccessSchema())
                .errorSchema(dto.getErrorSchema())
                .includeMetadata(dto.getIncludeMetadata())
                .metadataFields(dto.getMetadataFields())
                .contentType(dto.getContentType())
                .compression(dto.getCompression())
                .build();
    }

    private ApiSettingsEntity mapToSettingsEntity(ApiSettingsDTO dto, GeneratedApiEntity api) {
        try {
            return ApiSettingsEntity.builder()
                    .generatedApi(api)
                    .timeout(dto.getTimeout())
                    .maxRecords(dto.getMaxRecords())
                    .enableLogging(dto.getEnableLogging())
                    .logLevel(dto.getLogLevel())
                    .enableCaching(dto.getEnableCaching())
                    .cacheTtl(dto.getCacheTtl())
                    .enableRateLimiting(dto.getEnableRateLimiting())
                    .rateLimit(dto.getRateLimit())
                    .rateLimitPeriod(dto.getRateLimitPeriod())
                    .enableAudit(dto.getEnableAudit())
                    .auditLevel(dto.getAuditLevel())
                    .generateSwagger(dto.getGenerateSwagger())
                    .generatePostman(dto.getGeneratePostman())
                    .generateClientSdk(dto.getGenerateClientSdk())
                    .enableMonitoring(dto.getEnableMonitoring())
                    .enableAlerts(dto.getEnableAlerts())
                    .alertEmail(dto.getAlertEmail())
                    .enableTracing(dto.getEnableTracing())
                    .corsEnabled(dto.getCorsEnabled())
                    .corsOrigins(dto.getCorsOrigins() != null ?
                            objectMapper.writeValueAsString(dto.getCorsOrigins()) : null)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map settings: " + e.getMessage());
        }
    }

    private ApiParameterEntity mapToParameterEntity(ApiParameterDTO dto, GeneratedApiEntity api) {
        return ApiParameterEntity.builder()
                .generatedApi(api)
                .key(dto.getKey())
                .dbColumn(dto.getDbColumn())
                .dbParameter(dto.getDbParameter())
                .oracleType(dto.getOracleType())
                .apiType(dto.getApiType())
                .parameterType(dto.getParameterType())
                .required(dto.getRequired())
                .description(dto.getDescription())
                .example(dto.getExample())
                .validationPattern(dto.getValidationPattern())
                .defaultValue(dto.getDefaultValue())
                .position(dto.getPosition())
                .build();
    }

    private ApiResponseMappingEntity mapToResponseMappingEntity(ApiResponseMappingDTO dto, GeneratedApiEntity api) {
        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField(dto.getApiField())
                .dbColumn(dto.getDbColumn())
                .oracleType(dto.getOracleType())
                .apiType(dto.getApiType())
                .format(dto.getFormat())
                .nullable(dto.getNullable())
                .isPrimaryKey(dto.getIsPrimaryKey())
                .includeInResponse(dto.getIncludeInResponse())
                .position(dto.getPosition())
                .build();
    }

    private ApiHeaderEntity mapToHeaderEntity(ApiHeaderDTO dto, GeneratedApiEntity api) {
        return ApiHeaderEntity.builder()
                .generatedApi(api)
                .key(dto.getKey())
                .value(dto.getValue())
                .required(dto.getRequired())
                .description(dto.getDescription())
                .isRequestHeader(dto.getIsRequestHeader() != null ? dto.getIsRequestHeader() : true)
                .isResponseHeader(dto.getIsResponseHeader() != null ? dto.getIsResponseHeader() : false)
                .build();
    }

    private List<ApiTestEntity> createTestEntities(ApiTestsDTO dto, GeneratedApiEntity api) {
        List<ApiTestEntity> tests = new ArrayList<>();

        // Unit test
        if (dto.getUnitTests() != null && !dto.getUnitTests().isEmpty()) {
            tests.add(ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName("Unit Tests")
                    .testType("UNIT")
                    .testData(dto.getUnitTests())
                    .build());
        }

        // Integration test
        if (dto.getIntegrationTests() != null && !dto.getIntegrationTests().isEmpty()) {
            tests.add(ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName("Integration Tests")
                    .testType("INTEGRATION")
                    .testData(dto.getIntegrationTests())
                    .build());
        }

        // Test data
        if (dto.getTestData() != null && !dto.getTestData().isEmpty()) {
            tests.add(ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName("Test Data")
                    .testType("DATA")
                    .testData(dto.getTestData())
                    .build());
        }

        return tests;
    }

    private GeneratedApiResponseDTO mapToResponse(GeneratedApiEntity entity) {
        try {
            GeneratedApiResponseDTO response = GeneratedApiResponseDTO.builder()
                    .id(entity.getId())
                    .apiName(entity.getApiName())
                    .apiCode(entity.getApiCode())
                    .description(entity.getDescription())
                    .version(entity.getVersion())
                    .status(entity.getStatus())
                    .httpMethod(entity.getHttpMethod())
                    .basePath(entity.getBasePath())
                    .endpointPath(entity.getEndpointPath())
                    .fullEndpoint((entity.getBasePath() != null ? entity.getBasePath() : "") +
                            (entity.getEndpointPath() != null ? entity.getEndpointPath() : ""))
                    .category(entity.getCategory())
                    .owner(entity.getOwner())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .createdBy(entity.getCreatedBy())
                    .isActive(entity.getIsActive())
                    .totalCalls(entity.getTotalCalls())
                    .lastCalledAt(entity.getLastCalledAt())
                    .tags(entity.getTags())
                    .parametersCount(entity.getParameters() != null ? entity.getParameters().size() : 0)
                    .responseMappingsCount(entity.getResponseMappings() != null ? entity.getResponseMappings().size() : 0)
                    .headersCount(entity.getHeaders() != null ? entity.getHeaders().size() : 0)
                    .build();

            // Map schema config
            if (entity.getSchemaConfig() != null) {
                ApiSchemaConfigDTO schemaDto = ApiSchemaConfigDTO.builder()
                        .schemaName(entity.getSchemaConfig().getSchemaName())
                        .objectType(entity.getSchemaConfig().getObjectType())
                        .objectName(entity.getSchemaConfig().getObjectName())
                        .operation(entity.getSchemaConfig().getOperation())
                        .primaryKeyColumn(entity.getSchemaConfig().getPrimaryKeyColumn())
                        .sequenceName(entity.getSchemaConfig().getSequenceName())
                        .enablePagination(entity.getSchemaConfig().getEnablePagination())
                        .pageSize(entity.getSchemaConfig().getPageSize())
                        .enableSorting(entity.getSchemaConfig().getEnableSorting())
                        .defaultSortColumn(entity.getSchemaConfig().getDefaultSortColumn())
                        .defaultSortDirection(entity.getSchemaConfig().getDefaultSortDirection())
                        .isSynonym(entity.getSchemaConfig().getIsSynonym())
                        .targetType(entity.getSchemaConfig().getTargetType())
                        .targetName(entity.getSchemaConfig().getTargetName())
                        .targetOwner(entity.getSchemaConfig().getTargetOwner())
                        .build();
                response.setSchemaConfig(schemaDto);
            }

            // Map auth config
            if (entity.getAuthConfig() != null) {
                ApiAuthConfigDTO authDto = ApiAuthConfigDTO.builder()
                        .authType(entity.getAuthConfig().getAuthType())
                        .apiKeyHeader(entity.getAuthConfig().getApiKeyHeader())
                        .apiKeyValue(entity.getAuthConfig().getApiKeyValue())
                        .apiKeySecret(entity.getAuthConfig().getApiKeySecret())
                        .apiKeyLocation(entity.getAuthConfig().getApiKeyLocation())
                        .apiKeyPrefix(entity.getAuthConfig().getApiKeyPrefix())
                        .basicUsername(entity.getAuthConfig().getBasicUsername())
                        .basicPassword(entity.getAuthConfig().getBasicPassword())
                        .basicRealm(entity.getAuthConfig().getBasicRealm())
                        .jwtSecret(entity.getAuthConfig().getJwtSecret())
                        .jwtIssuer(entity.getAuthConfig().getJwtIssuer())
                        .jwtAudience(entity.getAuthConfig().getJwtAudience())
                        .jwtExpiration(entity.getAuthConfig().getJwtExpiration())
                        .jwtAlgorithm(entity.getAuthConfig().getJwtAlgorithm())
                        .oauthClientId(entity.getAuthConfig().getOauthClientId())
                        .oauthClientSecret(entity.getAuthConfig().getOauthClientSecret())
                        .oauthTokenUrl(entity.getAuthConfig().getOauthTokenUrl())
                        .oauthAuthUrl(entity.getAuthConfig().getOauthAuthUrl())
                        .oauthScopes(entity.getAuthConfig().getOauthScopes())
                        .requiredRoles(entity.getAuthConfig().getRequiredRoles())
                        .customAuthFunction(entity.getAuthConfig().getCustomAuthFunction())
                        .validateSession(entity.getAuthConfig().getValidateSession())
                        .checkObjectPrivileges(entity.getAuthConfig().getCheckObjectPrivileges())
                        .ipWhitelist(entity.getAuthConfig().getIpWhitelist())
                        .rateLimitRequests(entity.getAuthConfig().getRateLimitRequests())
                        .rateLimitPeriod(entity.getAuthConfig().getRateLimitPeriod())
                        .auditLevel(entity.getAuthConfig().getAuditLevel())
                        .corsOrigins(entity.getAuthConfig().getCorsOrigins())
                        .corsCredentials(entity.getAuthConfig().getCorsCredentials())
                        .build();
                response.setAuthConfig(authDto);
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to map response: " + e.getMessage());
        }
    }

    private void logExecution(GeneratedApiEntity api, ExecuteApiRequestDTO request,
                              Object response, int status, long executionTime,
                              String performedBy, String clientIp, String userAgent,
                              String errorMessage) {
        try {
            ApiExecutionLogEntity log = ApiExecutionLogEntity.builder()
                    .generatedApi(api)
                    .requestId(request != null ? request.getRequestId() : UUID.randomUUID().toString())
                    .requestParams(request != null && request.getQueryParams() != null ?
                            objectMapper.writeValueAsString(request.getQueryParams()) : null)
                    .requestBody(request != null && request.getBody() != null ?
                            objectMapper.writeValueAsString(request.getBody()) : null)
                    .responseBody(response != null ?
                            objectMapper.writeValueAsString(response) : null)
                    .responseStatus(status)
                    .executionTimeMs(executionTime)
                    .executedAt(LocalDateTime.now())
                    .executedBy(performedBy)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .errorMessage(errorMessage)
                    .build();

            executionLogRepository.save(log);

        } catch (Exception e) {
            log.error("Failed to log execution: {}", e.getMessage());
        }
    }

    private ExecuteApiResponseDTO createErrorResponse(String requestId, int statusCode,
                                                      String message, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;

        Map<String, Object> error = new HashMap<>();
        error.put("code", statusCode);
        error.put("message", message);

        return ExecuteApiResponseDTO.builder()
                .requestId(requestId)
                .statusCode(statusCode)
                .success(false)
                .message(message)
                .error(error)
                .executionTimeMs(executionTime)
                .build();
    }

    private Object executeAgainstOracle(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        // This would integrate with your Oracle database service
        // For now, return a placeholder based on the response mappings
        Map<String, Object> result = new HashMap<>();

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            for (var mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    // Generate sample data based on type
                    switch (mapping.getApiType()) {
                        case "integer":
                            result.put(mapping.getApiField(), 1);
                            break;
                        case "number":
                            result.put(mapping.getApiField(), 1.0);
                            break;
                        case "boolean":
                            result.put(mapping.getApiField(), true);
                            break;
                        case "string":
                        default:
                            if ("date".equals(mapping.getFormat())) {
                                result.put(mapping.getApiField(), LocalDateTime.now().toString());
                            } else {
                                result.put(mapping.getApiField(), "sample_" + mapping.getApiField());
                            }
                            break;
                    }
                }
            }
        } else {
            result.put("message", "API executed successfully");
            result.put("api", api.getApiCode());
            result.put("timestamp", LocalDateTime.now().toString());
        }

        return result;
    }

    private Object formatResponse(GeneratedApiEntity api, Object data) {
        // Format response based on API configuration
        if (api.getResponseConfig() != null && Boolean.TRUE.equals(api.getResponseConfig().getIncludeMetadata())) {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("data", data);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timestamp", LocalDateTime.now().toString());
            metadata.put("apiVersion", api.getVersion());
            metadata.put("requestId", UUID.randomUUID().toString());

            if (api.getResponseConfig().getMetadataFields() != null) {
                // Include only specified metadata fields
                Map<String, Object> filteredMetadata = new HashMap<>();
                for (String field : api.getResponseConfig().getMetadataFields()) {
                    if (metadata.containsKey(field)) {
                        filteredMetadata.put(field, metadata.get(field));
                    }
                }
                formatted.put("metadata", filteredMetadata);
            } else {
                formatted.put("metadata", metadata);
            }

            return formatted;
        }

        return data;
    }

    private Map<String, Object> buildResponseMetadata(GeneratedApiEntity api, long executionTime) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", api.getVersion());
        metadata.put("timestamp", LocalDateTime.now().toString());
        metadata.put("executionTimeMs", executionTime);

        if (api.getResponseConfig() != null && api.getResponseConfig().getMetadataFields() != null) {
            Map<String, Object> filtered = new HashMap<>();
            for (String field : api.getResponseConfig().getMetadataFields()) {
                if (metadata.containsKey(field)) {
                    filtered.put(field, metadata.get(field));
                }
            }
            return filtered;
        }

        return metadata;
    }

    private boolean compareResponses(ExecuteApiResponseDTO actual, Object expected) {
        // Simple comparison - in reality, you'd need more sophisticated comparison
        try {
            if (expected == null) return true;

            String actualJson = objectMapper.writeValueAsString(actual.getData());
            String expectedJson = objectMapper.writeValueAsString(expected);

            return actualJson.equals(expectedJson);

        } catch (Exception e) {
            return false;
        }
    }
}