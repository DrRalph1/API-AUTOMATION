package com.usg.apiAutomation.helpers.apiEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.usg.apiAutomation.utils.apiEngine.*;
import com.usg.apiAutomation.utils.apiEngine.executor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiExecutionHelper {

    public GeneratedApiEntity getApiEntity(GeneratedAPIRepository repository, String apiId) {
        return repository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API not found: " + apiId));
    }

    public GeneratedApiEntity createAndSaveApiEntity(
            GenerateApiRequestDTO request,
            ApiSourceObjectDTO sourceObjectDTO,
            CollectionInfoDTO collectionInfo,
            String endpointPath,
            String performedBy,
            String requestId,
            GeneratedAPIRepository generatedAPIRepository,
            ObjectMapper objectMapper,
            ParameterGeneratorUtil parameterGenerator,
            ApiConversionHelper conversionHelper) {

        // Create main API entity
        GeneratedApiEntity api = GeneratedApiEntity.builder()
                .apiName(request.getApiName())
                .apiCode(request.getApiCode())
                .description(request.getDescription())
                .version(request.getVersion())
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .httpMethod(request.getHttpMethod())
                .basePath(request.getBasePath())
                .endpointPath(endpointPath)
                .category(request.getCategory())
                .owner(request.getOwner())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(performedBy)
                .isActive(true)
                .totalCalls(0L)
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .sourceObjectInfo(sourceObjectDTO != null ?
                        objectMapper.convertValue(sourceObjectDTO, Map.class) : null)
                .collectionInfo(objectMapper.convertValue(collectionInfo, Map.class))
                .sourceRequestId(requestId)
                .build();

        // Save configurations
        if (request.getSchemaConfig() != null) {
            api.setSchemaConfig(mapToSchemaConfigEntity(request.getSchemaConfig(), api));
        }

        if (request.getAuthConfig() != null) {
            api.setAuthConfig(mapToAuthConfigEntity(request.getAuthConfig(), api));
        }

        if (request.getRequestBody() != null) {
            api.setRequestConfig(mapToRequestConfigEntity(request.getRequestBody(), api));
        }

        if (request.getResponseBody() != null) {
            api.setResponseConfig(mapToResponseConfigEntity(request.getResponseBody(), api));
        }

        if (request.getSettings() != null) {
            api.setSettings(mapToSettingsEntity(request.getSettings(), api));
        }

        // Save parameters
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            List<ApiParameterEntity> parameters = createParametersFromDTOs(request.getParameters(), api);
            api.setParameters(parameters);
        } else if (sourceObjectDTO != null) {
            List<ApiParameterEntity> parameters = parameterGenerator.generateParametersFromSource(sourceObjectDTO, api);
            api.setParameters(parameters);
        }

        // Save response mappings
        if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingEntity> mappings = createResponseMappingsFromDTOs(request.getResponseMappings(), api);
            api.setResponseMappings(mappings);
        } else if (sourceObjectDTO != null) {
            List<ApiResponseMappingEntity> mappings = parameterGenerator.generateResponseMappingsFromSource(sourceObjectDTO, api);
            api.setResponseMappings(mappings);
        }

        // Save headers
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeaderEntity> headers = createHeadersFromDTOs(request.getHeaders(), api);
            api.setHeaders(headers);
        }

        // Save tests
        if (request.getTests() != null) {
            List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
            api.setTests(tests);
        }

        return generatedAPIRepository.save(api);
    }

    public void updateApiEntity(GeneratedApiEntity api,
                                GenerateApiRequestDTO request,
                                ApiSourceObjectDTO sourceObjectDTO,
                                CollectionInfoDTO collectionInfo,
                                String performedBy) {
        api.setApiName(request.getApiName());
        api.setApiCode(request.getApiCode());
        api.setDescription(request.getDescription());
        api.setVersion(request.getVersion());
        api.setStatus(request.getStatus() != null ? request.getStatus() : api.getStatus());
        api.setHttpMethod(request.getHttpMethod());
        api.setBasePath(request.getBasePath());
        api.setEndpointPath(request.getEndpointPath());
        api.setCategory(request.getCategory());
        api.setOwner(request.getOwner());
        api.setUpdatedAt(LocalDateTime.now());
        api.setUpdatedBy(performedBy);
        api.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());

        if (sourceObjectDTO != null) {
            api.setSourceObjectInfo(new ObjectMapper().convertValue(sourceObjectDTO, Map.class));
        }

        if (collectionInfo != null) {
            api.setCollectionInfo(new ObjectMapper().convertValue(collectionInfo, Map.class));
        }
    }

    public void clearApiRelationships(GeneratedApiEntity api) {
        if (api.getSchemaConfig() != null) {
            api.getSchemaConfig().setGeneratedApi(null);
        }
        if (api.getAuthConfig() != null) {
            api.getAuthConfig().setGeneratedApi(null);
        }
        if (api.getRequestConfig() != null) {
            api.getRequestConfig().setGeneratedApi(null);
        }
        if (api.getResponseConfig() != null) {
            api.getResponseConfig().setGeneratedApi(null);
        }
        if (api.getSettings() != null) {
            api.getSettings().setGeneratedApi(null);
        }
        if (api.getParameters() != null) {
            api.getParameters().forEach(p -> p.setGeneratedApi(null));
            api.setParameters(null);
        }
        if (api.getResponseMappings() != null) {
            api.getResponseMappings().forEach(m -> m.setGeneratedApi(null));
            api.setResponseMappings(null);
        }
        if (api.getHeaders() != null) {
            api.getHeaders().forEach(h -> h.setGeneratedApi(null));
            api.setHeaders(null);
        }
        if (api.getTests() != null) {
            api.getTests().forEach(t -> t.setGeneratedApi(null));
            api.setTests(null);
        }
    }

    public void recreateApiRelationships(GeneratedApiEntity api,
                                         GenerateApiRequestDTO request,
                                         ApiSourceObjectDTO sourceObjectDTO,
                                         ParameterGeneratorUtil parameterGenerator,
                                         ApiConversionHelper conversionHelper) {
        // Recreate schema config
        if (request.getSchemaConfig() != null) {
            api.setSchemaConfig(mapToSchemaConfigEntity(request.getSchemaConfig(), api));
        }

        // Recreate auth config
        if (request.getAuthConfig() != null) {
            api.setAuthConfig(mapToAuthConfigEntity(request.getAuthConfig(), api));
        }

        // Recreate request config
        if (request.getRequestBody() != null) {
            api.setRequestConfig(mapToRequestConfigEntity(request.getRequestBody(), api));
        }

        // Recreate response config
        if (request.getResponseBody() != null) {
            api.setResponseConfig(mapToResponseConfigEntity(request.getResponseBody(), api));
        }

        // Recreate settings
        if (request.getSettings() != null) {
            api.setSettings(mapToSettingsEntity(request.getSettings(), api));
        }

        // Recreate parameters
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            List<ApiParameterEntity> parameters = createParametersFromDTOs(request.getParameters(), api);
            api.setParameters(parameters);
        } else if (sourceObjectDTO != null) {
            List<ApiParameterEntity> parameters = parameterGenerator.generateParametersFromSource(sourceObjectDTO, api);
            api.setParameters(parameters);
        }

        // Recreate response mappings
        if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingEntity> mappings = createResponseMappingsFromDTOs(request.getResponseMappings(), api);
            api.setResponseMappings(mappings);
        } else if (sourceObjectDTO != null) {
            List<ApiResponseMappingEntity> mappings = parameterGenerator.generateResponseMappingsFromSource(sourceObjectDTO, api);
            api.setResponseMappings(mappings);
        }

        // Recreate headers
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeaderEntity> headers = createHeadersFromDTOs(request.getHeaders(), api);
            api.setHeaders(headers);
        }

        // Recreate tests
        if (request.getTests() != null) {
            List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
            api.setTests(tests);
        }
    }

    public String buildEndpointPathWithParameters(GenerateApiRequestDTO request,
                                                  ApiSourceObjectDTO sourceObjectDTO,
                                                  ParameterGeneratorUtil parameterGenerator,
                                                  String requestId,
                                                  LoggerUtil loggerUtil) {
        String endpointPath = request.getEndpointPath();
        List<ApiParameterDTO> parametersForPath = new ArrayList<>();

        if (sourceObjectDTO != null && (request.getParameters() == null || request.getParameters().isEmpty())) {
            parametersForPath = parameterGenerator.generateParameterDTOsFromSource(sourceObjectDTO);
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Generated " + parametersForPath.size() + " parameter DTOs from source for path building");
        } else if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            parametersForPath.addAll(request.getParameters());
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Using " + parametersForPath.size() + " parameters from request for path building");
        }

        if (!parametersForPath.isEmpty()) {
            StringBuilder pathBuilder = new StringBuilder(endpointPath);
            List<ApiParameterDTO> pathParamList = new ArrayList<>();
            List<ApiParameterDTO> queryParamList = new ArrayList<>();

            parametersForPath.forEach(param -> {
                if (param.getKey() == null) return;
                if ("path".equals(param.getParameterType())) {
                    pathParamList.add(param);
                } else if ("query".equals(param.getParameterType())) {
                    queryParamList.add(param);
                }
            });

            // Add path parameters - FIXED: Use traditional for loop instead of forEach with lambda
            if (!pathParamList.isEmpty()) {
                pathParamList.sort(Comparator.comparingInt(ApiParameterDTO::getPosition));

                // Use traditional for loop instead of forEach with lambda
                for (ApiParameterDTO param : pathParamList) {
                    if (!endpointPath.contains("{" + param.getKey() + "}")) {
                        if (!pathBuilder.toString().endsWith("/")) {
                            pathBuilder.append("/");
                        }
                        pathBuilder.append("{").append(param.getKey()).append("}");
                    }
                }
            }

            // Add query parameters
            if (!queryParamList.isEmpty()) {
                List<String> queryPlaceholders = new ArrayList<>();

                // Use traditional for loop instead of stream
                for (ApiParameterDTO param : queryParamList) {
                    queryPlaceholders.add(param.getKey() + "={" + param.getKey() + "}");
                }
                Collections.sort(queryPlaceholders);

                pathBuilder.append("?").append(String.join("&", queryPlaceholders));
            }

            endpointPath = pathBuilder.toString();

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Enhanced endpoint path with parameters: " + endpointPath +
                    " (Path params: " + pathParamList.size() +
                    ", Query params: " + queryParamList.size() + ")");
        }

        return endpointPath;
    }


    public ExecuteApiRequestDTO prepareValidatedRequest(GeneratedApiEntity api, ExecuteApiRequestDTO executeRequest) {
        // Create a new request with properly organized parameters
        ExecuteApiRequestDTO validatedRequest = new ExecuteApiRequestDTO();
        validatedRequest.setRequestId(executeRequest.getRequestId() != null ?
                executeRequest.getRequestId() : UUID.randomUUID().toString());

        // Initialize maps safely
        Map<String, Object> pathParams = new HashMap<>();
        if (executeRequest.getPathParams() != null) {
            pathParams.putAll(executeRequest.getPathParams());
        }

        Map<String, Object> queryParams = new HashMap<>();
        if (executeRequest.getQueryParams() != null) {
            queryParams.putAll(executeRequest.getQueryParams());
        }

        Map<String, String> headers = new HashMap<>();
        if (executeRequest.getHeaders() != null) {
            headers.putAll(executeRequest.getHeaders());
        }

        Object body = executeRequest.getBody();

        log.debug("Incoming path params: {}", pathParams);
        log.debug("Incoming query params: {}", queryParams);
        log.debug("Incoming headers: {}", headers);
        log.debug("Incoming body: {}", body);

        // AUTO-ADD CONTENT-TYPE HEADER FOR POST/PUT REQUESTS WITH BODY
        if (("POST".equals(api.getHttpMethod()) || "PUT".equals(api.getHttpMethod()))
                && body != null) {

            // Check if Content-Type is already present (case-insensitive)
            boolean hasContentType = false;
            for (String headerKey : headers.keySet()) {
                if ("Content-Type".equalsIgnoreCase(headerKey)) {
                    hasContentType = true;
                    break;
                }
            }

            if (!hasContentType) {
                headers.put("Content-Type", "application/json");
                log.debug("Auto-added Content-Type: application/json for {} request with body",
                        api.getHttpMethod());
            }
        }

        // Categorize parameters based on API configuration
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            Map<String, Object> allProvidedParams = new HashMap<>();
            allProvidedParams.putAll(pathParams);
            allProvidedParams.putAll(queryParams);

            if (body instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> bodyMap = (Map<String, Object>) body;
                if (bodyMap != null) {
                    allProvidedParams.putAll(bodyMap);
                }
            }

            log.debug("Consolidated parameters: {}", allProvidedParams);

            for (ApiParameterEntity param : api.getParameters()) {
                if (param == null || param.getKey() == null) continue;

                String paramKey = param.getKey();
                String paramType = param.getParameterType();
                if (paramType == null) continue;

                if (allProvidedParams.containsKey(paramKey)) {
                    Object value = allProvidedParams.get(paramKey);
                    if (value != null) {
                        // Handle collection/array values
                        if (value instanceof List || value.getClass().isArray()) {
                            Collection<?> collection = value instanceof List ?
                                    (List<?>) value : Arrays.asList((Object[]) value);
                            if (!collection.isEmpty()) {
                                value = collection.iterator().next();
                                log.debug("Extracted first value from collection for parameter '{}'", paramKey);
                            } else {
                                value = null;
                            }
                        }

                        if (value != null) {
                            switch (paramType) {
                                case "path":
                                    pathParams.put(paramKey, value);
                                    break;
                                case "query":
                                    queryParams.put(paramKey, value);
                                    break;
                                case "header":
                                    headers.put(paramKey, value.toString());
                                    break;
                                case "body":
                                    if (body == null) {
                                        body = new HashMap<String, Object>();
                                    }
                                    if (body instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> bodyMap = (Map<String, Object>) body;
                                        bodyMap.put(paramKey, value);
                                    }
                                    break;
                                default:
                                    queryParams.put(paramKey, value);
                                    break;
                            }
                        }
                    }
                }
            }
        }

        validatedRequest.setPathParams(pathParams);
        validatedRequest.setQueryParams(queryParams);
        validatedRequest.setHeaders(headers);
        validatedRequest.setBody(body);

        return validatedRequest;
    }



    // In ApiExecutionHelper or wherever createConsolidatedParams is defined
    public Map<String, Object> createConsolidatedParams(ExecuteApiRequestDTO request) {
        Map<String, Object> params = new HashMap<>();

        // Add path variables
        if (request.getPathParams() != null) {
            params.putAll(request.getPathParams());
            log.info("Path variables: {}", request.getPathParams());
        }

        // Add query params
        if (request.getQueryParams() != null) {
            params.putAll(request.getQueryParams());
            log.info("Query params: {}", request.getQueryParams());
        }

        // Add body params (for POST/PUT)
        if (request.getBody() instanceof Map) {
            params.putAll((Map<String, Object>) request.getBody());
            log.info("Body params: {}", request.getBody());
        }

        return params;
    }

    public Object executeAgainstOracle(
            GeneratedApiEntity api,
            ApiSourceObjectDTO sourceObject,
            ExecuteApiRequestDTO request,
            List<ApiParameterDTO> configuredParamDTOs,
            OracleObjectResolverUtil objectResolver,
            ParameterValidatorUtil parameterValidator,
            TableExecutorUtil tableExecutorUtil,
            ViewExecutorUtil viewExecutorUtil,
            ProcedureExecutorUtil procedureExecutorUtil,
            FunctionExecutorUtil functionExecutorUtil,
            PackageExecutorUtil packageExecutorUtil,
            SampleResponseGenerator sampleGenerator) {

        if (sourceObject == null || api.getSchemaConfig() == null) {
            log.warn("No source object or schema config, generating sample response");
            return sampleGenerator.generateSampleResponse(api);
        }

        String targetType = sourceObject.getTargetType() != null ?
                sourceObject.getTargetType().trim().toUpperCase() :
                (sourceObject.getObjectType() != null ? sourceObject.getObjectType().trim().toUpperCase() : null);

        String targetName = sourceObject.getTargetName() != null ?
                sourceObject.getTargetName().trim() :
                (sourceObject.getObjectName() != null ? sourceObject.getObjectName().trim() : null);

        String targetOwner = sourceObject.getTargetOwner() != null ?
                sourceObject.getTargetOwner().trim().toUpperCase() :
                (sourceObject.getOwner() != null ? sourceObject.getOwner().trim().toUpperCase() : null);

        log.info("Executing Oracle operation - Type: {}, Name: {}, Owner: {}", targetType, targetName, targetOwner);

        if (targetName == null) {
            throw new RuntimeException("Target object name is required but was not provided");
        }

        // Handle SYNONYM resolution
        if ("SYNONYM".equals(targetType)) {
            Map<String, Object> resolution = objectResolver.resolveSynonymTarget(targetOwner, targetName);
            if (!(boolean) resolution.getOrDefault("exists", false)) {
                throw new RuntimeException(String.format("Synonym '%s.%s' does not exist", targetOwner, targetName));
            }
            targetOwner = (String) resolution.get("targetOwner");
            targetName = (String) resolution.get("targetName");
            targetType = (String) resolution.get("targetType");
        }

        // Get operation from source object or schema config
        String operation = sourceObject.getOperation() != null ?
                sourceObject.getOperation() :
                (api.getSchemaConfig() != null ? api.getSchemaConfig().getOperation() : null);

        // Create consolidated params map for the executors
        Map<String, Object> consolidatedParams = createConsolidatedParams(request);

        switch (targetType) {
            case "TABLE":
                return executeTableOperation(tableExecutorUtil, targetName, targetOwner, operation,
                        consolidatedParams, api, configuredParamDTOs);
            case "VIEW":
                return viewExecutorUtil.execute(api, sourceObject, targetName, targetOwner, request,
                        configuredParamDTOs);
            case "PROCEDURE":
                return procedureExecutorUtil.execute(api, sourceObject, targetName, targetOwner, request,
                        configuredParamDTOs);
            case "FUNCTION":
                return functionExecutorUtil.execute(api, sourceObject, targetName, targetOwner, request,
                        configuredParamDTOs);
            case "PACKAGE":
                return packageExecutorUtil.execute(api, sourceObject, targetName, targetOwner, request);
            default:
                log.warn("Unknown target type: {}, generating sample response", targetType);
                return sampleGenerator.generateSampleResponse(api);
        }
    }

    private Object executeTableOperation(TableExecutorUtil tableExecutorUtil,
                                         String tableName,
                                         String owner,
                                         String operation,
                                         Map<String, Object> params,
                                         GeneratedApiEntity api,
                                         List<ApiParameterDTO> configuredParamDTOs) {
        if (operation == null) {
            operation = "SELECT";
        }

        log.info("Executing {} operation on {}.{} with params: {}", operation, owner, tableName, params);

        switch (operation.toUpperCase()) {
            case "SELECT":
                return tableExecutorUtil.executeSelect(tableName, owner, params, api, configuredParamDTOs);
            case "INSERT":
                return tableExecutorUtil.executeInsert(tableName, owner, params, api);
            case "UPDATE":
                return tableExecutorUtil.executeUpdate(tableName, owner, params, api);
            case "DELETE":
                return tableExecutorUtil.executeDelete(tableName, owner, params, api);
            default:
                throw new RuntimeException("Unsupported table operation: " + operation);
        }
    }

    public void updateApiStats(GeneratedApiEntity api, GeneratedAPIRepository repository) {
        api.setTotalCalls(api.getTotalCalls() + 1);
        api.setLastCalledAt(LocalDateTime.now());
        repository.save(api);
    }

    public void logExecution(
            ApiExecutionLogRepository logRepository,
            GeneratedApiEntity api,
            ExecuteApiRequestDTO request,
            Object response,
            int status,
            long executionTime,
            String performedBy,
            String clientIp,
            String userAgent,
            String errorMessage,
            ObjectMapper objectMapper) {

        try {
            ApiExecutionLogEntity log = ApiExecutionLogEntity.builder()
                    .generatedApi(api)
                    .requestId(request != null && request.getRequestId() != null ?
                            request.getRequestId() : UUID.randomUUID().toString())
                    .requestParams(request != null && request.getQueryParams() != null ?
                            request.getQueryParams() : null)
                    .requestBody(request != null && request.getBody() instanceof Map ?
                            (Map<String, Object>) request.getBody() : null)
                    .responseBody(response instanceof Map ?
                            (Map<String, Object>) response : convertToMap(response, objectMapper))
                    .responseStatus(status)
                    .executionTimeMs(executionTime)
                    .executedAt(LocalDateTime.now())
                    .executedBy(performedBy)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .errorMessage(errorMessage)
                    .build();

            logRepository.save(log);

        } catch (Exception e) {
            log.error("Failed to log execution: {}", e.getMessage());
        }
    }

    public ApiTestEntity saveTestResult(
            ApiTestRepository testRepository,
            GeneratedApiEntity api,
            ApiTestRequestDTO testRequest,
            ExecuteApiResponseDTO executionResult,
            boolean passed,
            long executionTime,
            String performedBy,
            ObjectMapper objectMapper) {

        Map<String, Object> testDataMap = new HashMap<>();
        if (testRequest.getPathParams() != null) testDataMap.put("pathParams", testRequest.getPathParams());
        if (testRequest.getQueryParams() != null) testDataMap.put("queryParams", testRequest.getQueryParams());
        if (testRequest.getHeaders() != null) testDataMap.put("headers", testRequest.getHeaders());
        if (testRequest.getBody() != null) testDataMap.put("body", testRequest.getBody());
        testDataMap.put("expectedResponse", testRequest.getExpectedResponse());
        testDataMap.put("testName", testRequest.getTestName());
        testDataMap.put("testType", testRequest.getTestType());

        ApiTestEntity testEntity = ApiTestEntity.builder()
                .generatedApi(api)
                .testName(testRequest.getTestName())
                .testType(testRequest.getTestType() != null ? testRequest.getTestType() : "UNIT")
                .testData(testDataMap)
                .expectedResponse(testRequest.getExpectedResponse() != null ?
                        convertToMap(testRequest.getExpectedResponse(), objectMapper) : new HashMap<>())
                .actualResponse(executionResult.getData() instanceof Map ?
                        (Map<String, Object>) executionResult.getData() : convertToMap(executionResult.getData(), objectMapper))
                .status(passed ? "PASSED" : "FAILED")
                .executionTimeMs(executionTime)
                .executedAt(LocalDateTime.now())
                .executedBy(performedBy)
                .build();

        return testRepository.save(testEntity);
    }

    private Map<String, Object> convertToMap(Object obj, ObjectMapper objectMapper) {
        if (obj == null) return new HashMap<>();
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        try {
            return objectMapper.convertValue(obj, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("value", obj.toString());
            return result;
        }
    }

    // Private mapping methods (same as in the original service)
    private ApiSchemaConfigEntity mapToSchemaConfigEntity(ApiSchemaConfigDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;
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
        if (dto == null) return null;
        return ApiAuthConfigEntity.builder()
                .generatedApi(api)
                .authType(dto.getAuthType())
                .apiKeyHeader(dto.getApiKeyHeader())
                .apiKeyValue(dto.getApiKeyValue())
                .apiSecretHeader(dto.getApiSecretHeader())
                .apiSecretValue(dto.getApiSecretValue())
                .jwtToken(dto.getJwtToken())
                .jwtIssuer(dto.getJwtIssuer())
                .basicUsername(dto.getBasicUsername())
                .basicPassword(dto.getBasicPassword())
                .ipWhitelist(dto.getIpWhitelist())
                .rateLimitRequests(dto.getRateLimitRequests())
                .rateLimitPeriod(dto.getRateLimitPeriod())
                .enableRateLimiting(dto.getEnableRateLimiting())
                .corsOrigins(dto.getCorsOrigins() != null ? dto.getCorsOrigins().toString() : null)
                .auditLevel(dto.getAuditLevel())
                .build();
    }

    private ApiRequestConfigEntity mapToRequestConfigEntity(ApiRequestConfigDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;
        return ApiRequestConfigEntity.builder()
                .generatedApi(api)
                .bodyType(dto.getBodyType())
                .sample(dto.getSample())
                .maxSize(dto.getMaxSize())
                .validateSchema(dto.getValidateSchema())
                .allowedMediaTypes(dto.getAllowedMediaTypes() != null ?
                        String.join(",", dto.getAllowedMediaTypes()) : null)
                .requiredFields(dto.getRequiredFields())
                .build();
    }

    private ApiResponseConfigEntity mapToResponseConfigEntity(ApiResponseConfigDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;
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
        if (dto == null) return null;
        return ApiSettingsEntity.builder()
                .generatedApi(api)
                .timeout(dto.getTimeout())
                .maxRecords(dto.getMaxRecords())
                .enableLogging(dto.getEnableLogging())
                .logLevel(dto.getLogLevel())
                .enableCaching(dto.getEnableCaching())
                .cacheTtl(dto.getCacheTtl())
                .generateSwagger(dto.getGenerateSwagger())
                .generatePostman(dto.getGeneratePostman())
                .generateClientSDK(dto.getGenerateClientSDK())
                .enableMonitoring(dto.getEnableMonitoring())
                .enableAlerts(dto.getEnableAlerts())
                .alertEmail(dto.getAlertEmail())
                .enableTracing(dto.getEnableTracing())
                .corsEnabled(dto.getCorsEnabled())
                .build();
    }

    private List<ApiParameterEntity> createParametersFromDTOs(List<ApiParameterDTO> dtos, GeneratedApiEntity api) {
        List<ApiParameterEntity> parameters = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            ApiParameterDTO dto = dtos.get(i);
            ApiParameterEntity param = ApiParameterEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .generatedApi(api)
                    .key(dto.getKey())
                    .dbColumn(dto.getDbColumn())
                    .dbParameter(dto.getDbParameter())
                    .oracleType(dto.getOracleType())
                    .apiType(dto.getApiType())
                    .parameterType(dto.getParameterType())
                    .parameterLocation(dto.getParameterLocation())
                    .required(dto.getRequired())
                    .description(dto.getDescription())
                    .example(dto.getExample())
                    .validationPattern(dto.getValidationPattern())
                    .defaultValue(dto.getDefaultValue())
                    .inBody(dto.getInBody())
                    .isPrimaryKey(dto.getIsPrimaryKey())
                    .paramMode(dto.getParamMode())
                    .position(i)
                    .build();
            parameters.add(param);
        }
        return parameters;
    }

    private List<ApiResponseMappingEntity> createResponseMappingsFromDTOs(List<ApiResponseMappingDTO> dtos,
                                                                          GeneratedApiEntity api) {
        List<ApiResponseMappingEntity> mappings = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            ApiResponseMappingDTO dto = dtos.get(i);
            ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                    .generatedApi(api)
                    .apiField(dto.getApiField())
                    .dbColumn(dto.getDbColumn())
                    .oracleType(dto.getOracleType())
                    .apiType(dto.getApiType())
                    .format(dto.getFormat())
                    .nullable(dto.getNullable())
                    .isPrimaryKey(dto.getIsPrimaryKey())
                    .includeInResponse(dto.getIncludeInResponse())
                    .inResponse(dto.getInResponse())
                    .position(i)
                    .build();
            mappings.add(mapping);
        }
        return mappings;
    }

    private List<ApiHeaderEntity> createHeadersFromDTOs(List<ApiHeaderDTO> dtos, GeneratedApiEntity api) {
        List<ApiHeaderEntity> headers = new ArrayList<>();
        for (ApiHeaderDTO dto : dtos) {
            ApiHeaderEntity header = ApiHeaderEntity.builder()
                    .generatedApi(api)
                    .key(dto.getKey())
                    .value(dto.getValue())
                    .required(dto.getRequired())
                    .description(dto.getDescription())
                    .isRequestHeader(dto.getIsRequestHeader() != null ? dto.getIsRequestHeader() : true)
                    .isResponseHeader(dto.getIsResponseHeader() != null ? dto.getIsResponseHeader() : false)
                    .build();
            headers.add(header);
        }
        return headers;
    }

    private List<ApiTestEntity> createTestEntities(ApiTestsDTO dto, GeneratedApiEntity api) {
        if (dto == null) return new ArrayList<>();

        List<ApiTestEntity> tests = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        if (dto.getUnitTests() != null && !dto.getUnitTests().isEmpty()) {
            tests.add(createTestEntity(api, dto, "Unit Tests", "UNIT", dto.getUnitTests(), objectMapper));
        }

        if (dto.getIntegrationTests() != null && !dto.getIntegrationTests().isEmpty()) {
            tests.add(createTestEntity(api, dto, "Integration Tests", "INTEGRATION", dto.getIntegrationTests(), objectMapper));
        }

        return tests;
    }

    private ApiTestEntity createTestEntity(GeneratedApiEntity api, ApiTestsDTO dto,
                                           String name, String type, String testsContent,
                                           ObjectMapper objectMapper) {
        Map<String, Object> testData = new HashMap<>();
        testData.put("tests", testsContent);
        testData.put("type", type.toLowerCase());
        testData.put("assertions", dto.getAssertions());
        testData.put("environment", dto.getTestEnvironment());
        testData.put("iterations", dto.getTestIterations());
        testData.put("users", dto.getTestUsers());

        return ApiTestEntity.builder()
                .generatedApi(api)
                .testName(name)
                .testType(type)
                .testData(testData)
                .status("PENDING")
                .build();
    }

    @FunctionalInterface
    public interface SampleResponseGenerator {
        Object generateSampleResponse(GeneratedApiEntity api);
    }


    /**
     * Extract path parameters from the actual URL based on the API's endpoint template
     */
    public Map<String, Object> extractPathParamsFromUrl(String actualUrl, GeneratedApiEntity api) {
        Map<String, Object> pathParams = new HashMap<>();

        try {
            // Get the endpoint template from the API
            String endpointTemplate = api.getEndpointPath();
            if (endpointTemplate == null || endpointTemplate.isEmpty()) {
                log.warn("No endpoint template found for API: {}", api.getId());
                return pathParams;
            }

            // Parse the actual URL to get just the path
            java.net.URL url = new java.net.URL(actualUrl);
            String actualPath = url.getPath();

            log.info("Extracting path params - Template: {}, Actual Path: {}", endpointTemplate, actualPath);

            // Get all path parameters defined in the API
            List<ApiParameterEntity> pathParamDefinitions = new ArrayList<>();
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String location = param.getParameterLocation() != null ?
                            param.getParameterLocation().toLowerCase() : "";
                    String type = param.getParameterType() != null ?
                            param.getParameterType().toLowerCase() : "";

                    if ("path".equals(location) || "path".equals(type)) {
                        pathParamDefinitions.add(param);
                        log.debug("Found path parameter definition: {} at position {}",
                                param.getKey(), param.getPosition());
                    }
                }
            }

            if (pathParamDefinitions.isEmpty()) {
                log.info("No path parameters defined for API: {}", api.getId());
                return pathParams;
            }

            // Method 1: Extract using template pattern matching
            Map<String, Object> extracted = extractUsingTemplateMatching(actualPath, endpointTemplate, pathParamDefinitions);
            if (!extracted.isEmpty()) {
                return extracted;
            }

            // Method 2: If template matching fails, try regex pattern matching
            extracted = extractUsingRegexPattern(actualPath, endpointTemplate, pathParamDefinitions);
            if (!extracted.isEmpty()) {
                return extracted;
            }

            // Method 3: Last resort - manual extraction based on API ID and known pattern
            extracted = extractManually(actualPath, api, pathParamDefinitions);
            if (!extracted.isEmpty()) {
                return extracted;
            }

        } catch (Exception e) {
            log.error("Failed to extract path parameters from URL: {}", e.getMessage(), e);
        }

        return pathParams;
    }

    /**
     * Alternative extraction method using regex patterns
     */
    private Map<String, Object> extractUsingPattern(String actualPath, String template,
                                                    List<ApiParameterEntity> pathParamDefinitions) {
        Map<String, Object> pathParams = new HashMap<>();

        try {
            // Convert template to regex pattern
            String regexPattern = template.replaceAll("\\{[^/]+\\}", "([^/]+)");
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(actualPath);

            if (matcher.matches()) {
                // Find all parameter names from template
                List<String> paramNames = new ArrayList<>();
                java.util.regex.Matcher paramMatcher = java.util.regex.Pattern.compile("\\{([^/]+)\\}")
                        .matcher(template);
                while (paramMatcher.find()) {
                    paramNames.add(paramMatcher.group(1));
                }

                // Match values to parameter names
                for (int i = 0; i < paramNames.size() && i < matcher.groupCount(); i++) {
                    String paramName = paramNames.get(i);
                    String paramValue = matcher.group(i + 1);

                    // Verify this is a defined path parameter
                    Optional<ApiParameterEntity> matchingParam = pathParamDefinitions.stream()
                            .filter(p -> paramName.equalsIgnoreCase(p.getKey()))
                            .findFirst();

                    if (matchingParam.isPresent()) {
                        pathParams.put(paramName, paramValue);
                        log.info("Extracted path parameter using regex: {} = {}", paramName, paramValue);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Regex extraction failed: {}", e.getMessage());
        }

        return pathParams;
    }

    /**
     * Create consolidated parameters map from request, with automatic path parameter extraction
     */
    public Map<String, Object> createConsolidatedParams(ExecuteApiRequestDTO request, GeneratedApiEntity api) {
        Map<String, Object> params = new HashMap<>();

        log.info("===== CREATE CONSOLIDATED PARAMS START =====");
        log.info("Request URL: {}", request.getUrl());
        log.info("Request path params (before): {}", request.getPathParams());
        log.info("API ID: {}", api != null ? api.getId() : "null");

        // 1. First, try to get path params from the request
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            params.putAll(request.getPathParams());
            log.info("Path params from request: {}", request.getPathParams());
        }

        // 2. If no path params in request, try to extract from URL using API's endpoint template
        if ((request.getPathParams() == null || request.getPathParams().isEmpty())
                && request.getUrl() != null && api != null) {

            log.info("Attempting to extract path params from URL: {}", request.getUrl());
            log.info("API endpoint template: {}", api.getEndpointPath());

            Map<String, Object> extractedPathParams = extractPathParamsFromUrl(request.getUrl(), api);
            if (!extractedPathParams.isEmpty()) {
                params.putAll(extractedPathParams);
                // Update the request with extracted params for logging
                request.setPathParams(extractedPathParams);
                log.info("Extracted path params from URL: {}", extractedPathParams);
            } else {
                log.warn("Could not extract path params from URL");
            }
        }

        // 3. Add query params
        if (request.getQueryParams() != null) {
            params.putAll(request.getQueryParams());
            log.info("Query params: {}", request.getQueryParams());
        }

        // 4. Add body params (for POST/PUT)
        if (request.getBody() instanceof Map) {
            params.putAll((Map<String, Object>) request.getBody());
            log.info("Body params: {}", request.getBody());
        }

        log.info("Final consolidated params: {}", params);
        log.info("===== CREATE CONSOLIDATED PARAMS END =====");

        return params;
    }


    /**
     * Method 1: Extract using direct template segment matching
     */
    private Map<String, Object> extractUsingTemplateMatching(String actualPath, String template,
                                                             List<ApiParameterEntity> pathParamDefinitions) {
        Map<String, Object> pathParams = new HashMap<>();

        try {
            // Remove base path if present (like /plx/api/gen/{apiId})
            String processedTemplate = template;
            String processedActual = actualPath;

            // Find where the actual path starts matching the template
            // This handles cases where the URL has a base path prefix

            // Split both paths into segments
            String[] templateSegments = processedTemplate.split("/");
            String[] actualSegments = processedActual.split("/");

            log.debug("Template segments: {}", Arrays.toString(templateSegments));
            log.debug("Actual segments: {}", Arrays.toString(actualSegments));

            // Create a map of parameter name to position in template
            Map<Integer, String> positionToParamMap = new HashMap<>();
            for (int i = 0; i < templateSegments.length; i++) {
                String segment = templateSegments[i];
                if (segment.startsWith("{") && segment.endsWith("}")) {
                    String paramName = segment.substring(1, segment.length() - 1);
                    positionToParamMap.put(i, paramName);
                }
            }

            // Match segments and extract values where template has placeholders
            for (Map.Entry<Integer, String> entry : positionToParamMap.entrySet()) {
                int templatePosition = entry.getKey();
                String paramName = entry.getValue();

                // Check if we have a corresponding actual segment
                if (templatePosition < actualSegments.length) {
                    String actualValue = actualSegments[templatePosition];

                    // Verify this is a defined path parameter
                    Optional<ApiParameterEntity> matchingParam = pathParamDefinitions.stream()
                            .filter(p -> paramName.equalsIgnoreCase(p.getKey()))
                            .findFirst();

                    if (matchingParam.isPresent()) {
                        pathParams.put(paramName, actualValue);
                        log.info("Extracted path parameter using template matching: {} = {}", paramName, actualValue);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Template matching failed: {}", e.getMessage());
        }

        return pathParams;
    }

    /**
     * Method 2: Extract using regex pattern matching
     */
    private Map<String, Object> extractUsingRegexPattern(String actualPath, String template,
                                                         List<ApiParameterEntity> pathParamDefinitions) {
        Map<String, Object> pathParams = new HashMap<>();

        try {
            // Convert template to regex pattern
            // First, escape any regex special characters
            String regexPattern = template.replaceAll("\\{[^/]+\\}", "([^/]+)");

            // Also handle the base path prefix if present
            // The actual path might have additional prefix like /plx/api/gen/{apiId}
            // We need to find where the template matches

            Pattern pattern = Pattern.compile(".*" + regexPattern + "$");
            Matcher matcher = pattern.matcher(actualPath);

            if (matcher.matches()) {
                // Find all parameter names from template
                List<String> paramNames = new ArrayList<>();
                java.util.regex.Matcher paramMatcher = java.util.regex.Pattern.compile("\\{([^/]+)\\}")
                        .matcher(template);
                while (paramMatcher.find()) {
                    paramNames.add(paramMatcher.group(1));
                }

                // Match values to parameter names
                for (int i = 0; i < paramNames.size() && i < matcher.groupCount(); i++) {
                    String paramName = paramNames.get(i);
                    String paramValue = matcher.group(i + 1);

                    // Verify this is a defined path parameter
                    Optional<ApiParameterEntity> matchingParam = pathParamDefinitions.stream()
                            .filter(p -> paramName.equalsIgnoreCase(p.getKey()))
                            .findFirst();

                    if (matchingParam.isPresent()) {
                        pathParams.put(paramName, paramValue);
                        log.info("Extracted path parameter using regex: {} = {}", paramName, paramValue);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Regex extraction failed: {}", e.getMessage());
        }

        return pathParams;
    }

    /**
     * Method 3: Manual extraction based on API ID and known patterns
     */
    private Map<String, Object> extractManually(String actualPath, GeneratedApiEntity api,
                                                List<ApiParameterEntity> pathParamDefinitions) {
        Map<String, Object> pathParams = new HashMap<>();

        try {
            // For your specific case, the pattern is:
            // /plx/api/gen/{apiId}/api/v1/{endpoint}/{pathParam}

            String[] segments = actualPath.split("/");
            log.debug("All segments: {}", Arrays.toString(segments));

            // Find the position after the API ID
            // Look for the pattern: after "/gen/{apiId}/api/v1/"
            boolean foundGen = false;
            boolean foundApiV1 = false;
            int startPosition = -1;

            for (int i = 0; i < segments.length; i++) {
                if ("gen".equals(segments[i])) {
                    foundGen = true;
                    // Skip the API ID segment
                    i += 2; // Skip past gen/{apiId}
                    continue;
                }
                if (foundGen && "api".equals(segments[i]) && i + 1 < segments.length && "v1".equals(segments[i + 1])) {
                    foundApiV1 = true;
                    startPosition = i + 2; // Start after api/v1
                    break;
                }
            }

            if (startPosition > 0 && startPosition < segments.length) {
                // Now extract path parameters in order
                List<ApiParameterEntity> sortedParams = new ArrayList<>(pathParamDefinitions);
                sortedParams.sort(Comparator.comparing(ApiParameterEntity::getPosition));

                for (int i = 0; i < sortedParams.size(); i++) {
                    int paramPosition = startPosition + i;
                    if (paramPosition < segments.length) {
                        ApiParameterEntity param = sortedParams.get(i);
                        String value = segments[paramPosition];

                        // Skip empty values and query parameters
                        if (value != null && !value.isEmpty() && !value.contains("?")) {
                            pathParams.put(param.getKey(), value);
                            log.info("Extracted path parameter manually: {} = {}", param.getKey(), value);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Manual extraction failed: {}", e.getMessage());
        }

        return pathParams;
    }
}