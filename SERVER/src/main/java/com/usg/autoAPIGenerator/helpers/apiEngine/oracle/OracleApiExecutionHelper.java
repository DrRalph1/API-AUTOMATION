package com.usg.autoAPIGenerator.helpers.apiEngine.oracle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.helpers.BaseApiExecutionHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.ApiConversionHelper;
import com.usg.autoAPIGenerator.helpers.apiEngine.ApiResponseHelper;
import com.usg.autoAPIGenerator.repositories.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.utils.LoggerUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.DatabaseParameterGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.CustomQueryExecutionHelper;
import com.usg.autoAPIGenerator.utils.apiEngine.executor.oracle.*;
import com.usg.autoAPIGenerator.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.OracleParameterValidatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OracleApiExecutionHelper extends BaseApiExecutionHelper {

    private final OracleObjectResolverUtil objectResolver;
    private final OracleParameterValidatorUtil parameterValidator;
    private final OracleTableExecutorUtil oracleTableExecutorUtil;
    private final OracleViewExecutorUtil oracleViewExecutorUtil;
    private final OracleProcedureExecutorUtil oracleProcedureExecutorUtil;
    private final OracleFunctionExecutorUtil oracleFunctionExecutorUtil;
    private final OraclePackageExecutorUtil oraclePackageExecutorUtil;
    private final CustomQueryExecutionHelper customQueryExecutionHelper;

    @Qualifier("oracleJdbcTemplate")
    private final JdbcTemplate oracleJdbcTemplate;

    public OracleApiExecutionHelper(
            ApiResponseHelper responseHelper,
            LoggerUtil loggerUtil,
            ApiConversionHelper conversionHelper,
            org.springframework.transaction.support.TransactionTemplate transactionTemplate,
            OracleObjectResolverUtil objectResolver,
            OracleParameterValidatorUtil parameterValidator,
            OracleTableExecutorUtil oracleTableExecutorUtil,
            OracleViewExecutorUtil oracleViewExecutorUtil,
            OracleProcedureExecutorUtil oracleProcedureExecutorUtil,
            OracleFunctionExecutorUtil oracleFunctionExecutorUtil,
            OraclePackageExecutorUtil oraclePackageExecutorUtil, CustomQueryExecutionHelper customQueryExecutionHelper,
            @Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbcTemplate) {
        super(responseHelper, loggerUtil, conversionHelper, transactionTemplate);
        this.objectResolver = objectResolver;
        this.parameterValidator = parameterValidator;
        this.oracleTableExecutorUtil = oracleTableExecutorUtil;
        this.oracleViewExecutorUtil = oracleViewExecutorUtil;
        this.oracleProcedureExecutorUtil = oracleProcedureExecutorUtil;
        this.oracleFunctionExecutorUtil = oracleFunctionExecutorUtil;
        this.oraclePackageExecutorUtil = oraclePackageExecutorUtil;
        this.customQueryExecutionHelper = customQueryExecutionHelper;
        this.oracleJdbcTemplate = oracleJdbcTemplate;
    }

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
            DatabaseParameterGeneratorUtil parameterGenerator,
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
                .build();

        // Save configurations using helper setters
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

        // Save parameters using helper methods
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            List<ApiParameterEntity> parameters = createParametersFromDTOs(request.getParameters(), api);
            api.replaceParameters(parameters);
        } else if (sourceObjectDTO != null) {
            List<ApiParameterEntity> parameters = parameterGenerator.generateParametersFromSource(sourceObjectDTO, api);
            api.replaceParameters(parameters);
        }

        // Save response mappings using helper methods
        if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingEntity> mappings = createResponseMappingsFromDTOs(request.getResponseMappings(), api);
            api.replaceResponseMappings(mappings);
        } else if (sourceObjectDTO != null) {
            List<ApiResponseMappingEntity> mappings = parameterGenerator.generateResponseMappingsFromSource(sourceObjectDTO, api);
            api.replaceResponseMappings(mappings);
        }

        // Save headers using helper methods
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeaderEntity> headers = createHeadersFromDTOs(request.getHeaders(), api);
            api.replaceHeaders(headers);
        }

        // Save tests using helper methods
        if (request.getTests() != null) {
            List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
            api.replaceTests(tests);
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
        api.replaceTags(request.getTags() != null ? request.getTags() : new ArrayList<>());

        if (sourceObjectDTO != null) {
            api.setSourceObjectInfo(new ObjectMapper().convertValue(sourceObjectDTO, Map.class));
        }

        if (collectionInfo != null) {
            api.setCollectionInfo(new ObjectMapper().convertValue(collectionInfo, Map.class));
        }
    }

    /**
     * Clear all relationships using the helper methods
     */
    public void clearApiRelationships(GeneratedApiEntity api) {
        if (api == null) return;

        log.debug("Clearing relationships for API: {}", api.getId());

        // Clear one-to-one relationships
        api.setSchemaConfig(null);
        api.setAuthConfig(null);
        api.setRequestConfig(null);
        api.setResponseConfig(null);
        api.setSettings(null);

        // Clear collections using helper methods
        api.clearParameters();
        api.clearResponseMappings();
        api.clearHeaders();
        api.clearTests();
        api.clearTags();

        log.debug("Successfully cleared relationships for API: {}", api.getId());
    }

    /**
     * Recreate relationships using the helper methods (REPLACE not APPEND)
     */
    public void recreateApiRelationships(GeneratedApiEntity api,
                                         GenerateApiRequestDTO request,
                                         ApiSourceObjectDTO sourceObjectDTO,
                                         DatabaseParameterGeneratorUtil parameterGenerator,
                                         ApiConversionHelper conversionHelper) {

        log.debug("Recreating relationships for API: {}", api.getId());

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

        // ============ REPLACE parameters (CLEAR then ADD) ============
        api.clearParameters();  // Clear existing

        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            List<ApiParameterEntity> parameters = createParametersFromDTOs(request.getParameters(), api);
            api.replaceParameters(parameters);
        } else if (sourceObjectDTO != null) {
            List<ApiParameterEntity> parameters = parameterGenerator.generateParametersFromSource(sourceObjectDTO, api);
            api.replaceParameters(parameters);
        }
        log.info("Recreated {} parameters for API: {}", api.getParameters().size(), api.getId());

        // ============ REPLACE response mappings (CLEAR then ADD) ============
        api.clearResponseMappings();  // Clear existing

        if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingEntity> mappings = createResponseMappingsFromDTOs(request.getResponseMappings(), api);
            api.replaceResponseMappings(mappings);
        } else if (sourceObjectDTO != null) {
            List<ApiResponseMappingEntity> mappings = parameterGenerator.generateResponseMappingsFromSource(sourceObjectDTO, api);
            api.replaceResponseMappings(mappings);
        }
        log.info("Recreated {} response mappings for API: {}", api.getResponseMappings().size(), api.getId());

        // ============ REPLACE headers (CLEAR then ADD) ============
        api.clearHeaders();  // Clear existing

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeaderEntity> headers = createHeadersFromDTOs(request.getHeaders(), api);
            api.replaceHeaders(headers);
        }
        log.info("Recreated {} headers for API: {}", api.getHeaders().size(), api.getId());

        // ============ REPLACE tests (CLEAR then ADD) ============
        api.clearTests();  // Clear existing

        if (request.getTests() != null) {
            List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
            api.replaceTests(tests);
        }
        log.info("Recreated {} tests for API: {}", api.getTests().size(), api.getId());

        log.debug("Successfully recreated relationships for API: {}", api.getId());
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

        // Extract path parameters from the URL if they're not already in pathParams
        if (pathParams.isEmpty() && executeRequest.getUrl() != null && api.getEndpointPath() != null) {
            extractPathParamsFromUrl(api, executeRequest, pathParams);
        }

        // AUTO-ADD CONTENT-TYPE HEADER FOR POST/PUT REQUESTS WITH BODY
        if (("POST".equals(api.getHttpMethod()) || "PUT".equals(api.getHttpMethod()))
                && body != null) {

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
        if (api.hasParameters()) {
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

    /**
     * Extract path parameters from the URL based on the API's endpoint path pattern
     */
    private void extractPathParamsFromUrl(GeneratedApiEntity api, ExecuteApiRequestDTO executeRequest, Map<String, Object> pathParams) {
        try {
            String url = executeRequest.getUrl();
            String endpointPattern = api.getEndpointPath();

            log.info("Extracting path params from URL: {} using pattern: {}", url, endpointPattern);

            // Extract all parameter names from the pattern (e.g., {acct_link})
            Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(endpointPattern);

            List<String> paramNames = new ArrayList<>();
            while (matcher.find()) {
                paramNames.add(matcher.group(1));
            }

            if (paramNames.isEmpty()) {
                log.debug("No path parameters found in endpoint pattern");
                return;
            }

            log.info("Found {} path parameters in pattern: {}", paramNames.size(), paramNames);

            // Find the position where the endpoint path starts in the URL
            String apiId = api.getId();
            String apiIdPattern = "/gen/" + apiId;
            int apiIdIndex = url.indexOf(apiIdPattern);

            if (apiIdIndex == -1) {
                log.warn("Could not find API ID pattern '{}' in URL: {}", apiIdPattern, url);
                return;
            }

            // Get the substring after the API ID
            String afterApiId = url.substring(apiIdIndex + apiIdPattern.length());
            log.info("URL after API ID: {}", afterApiId);

            // Split by "/" and filter out empty strings
            String[] urlSegments = afterApiId.split("/");
            List<String> filteredUrlSegments = new ArrayList<>();
            for (String segment : urlSegments) {
                if (segment != null && !segment.isEmpty()) {
                    filteredUrlSegments.add(segment);
                }
            }

            log.info("URL segments: {}", filteredUrlSegments);

            // Split the endpoint pattern by "/"
            String[] patternSegments = endpointPattern.split("/");
            List<String> filteredPatternSegments = new ArrayList<>();
            for (String segment : patternSegments) {
                if (segment != null && !segment.isEmpty()) {
                    filteredPatternSegments.add(segment);
                }
            }

            log.info("Pattern segments: {}", filteredPatternSegments);

            // Find where the pattern starts in the URL segments
            int patternStartIndex = -1;
            for (int i = 0; i <= filteredUrlSegments.size() - filteredPatternSegments.size(); i++) {
                boolean matches = true;
                for (int j = 0; j < filteredPatternSegments.size(); j++) {
                    String patternSegment = filteredPatternSegments.get(j);
                    String urlSegment = filteredUrlSegments.get(i + j);

                    if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                        continue;
                    }
                    if (!patternSegment.equals(urlSegment)) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    patternStartIndex = i;
                    break;
                }
            }

            if (patternStartIndex == -1) {
                log.warn("Could not find pattern in URL segments");
                return;
            }

            log.info("Pattern starts at URL segment index: {}", patternStartIndex);

            // Extract path parameters
            for (int i = 0; i < filteredPatternSegments.size(); i++) {
                String patternSegment = filteredPatternSegments.get(i);
                if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                    String paramName = patternSegment.substring(1, patternSegment.length() - 1);
                    String paramValue = filteredUrlSegments.get(patternStartIndex + i);
                    pathParams.put(paramName, paramValue);
                    log.info("Extracted path parameter: {} = {}", paramName, paramValue);
                }
            }

            log.info("Total extracted {} path parameters: {}", pathParams.size(), pathParams);

        } catch (Exception e) {
            log.error("Failed to extract path parameters from URL: {}", e.getMessage(), e);
        }
    }


    public Map<String, Object> createConsolidatedParams(ExecuteApiRequestDTO request) {
        Map<String, Object> consolidated = new HashMap<>();

        if (request.getPathParams() != null) {
            consolidated.putAll(request.getPathParams());
        }
        if (request.getQueryParams() != null) {
            consolidated.putAll(request.getQueryParams());
        }
        if (request.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
            if (bodyMap != null) {
                consolidated.putAll(bodyMap);
            }
        }

        return consolidated;
    }

    public Object executeAgainstOracle(
            GeneratedApiEntity api,
            ApiSourceObjectDTO sourceObject,
            ExecuteApiRequestDTO request,
            List<ApiParameterDTO> configuredParamDTOs,
            OracleObjectResolverUtil objectResolver,
            OracleParameterValidatorUtil parameterValidator,
            OracleTableExecutorUtil oracleTableExecutorUtil,
            OracleViewExecutorUtil oracleViewExecutorUtil,
            OracleProcedureExecutorUtil oracleProcedureExecutorUtil,
            OracleFunctionExecutorUtil oracleFunctionExecutorUtil,
            OraclePackageExecutorUtil oraclePackageExecutorUtil,
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
                return executeTableOperation(oracleTableExecutorUtil, targetName, targetOwner, operation,
                        consolidatedParams, api, configuredParamDTOs);
            case "VIEW":
                String viewOwner = targetOwner;
                if (viewOwner == null || viewOwner.isEmpty()) {
                    viewOwner = sourceObject.getOwner();
                    if (viewOwner == null || viewOwner.isEmpty()) {
                        viewOwner = sourceObject.getSchemaName();
                    }
                }
                return oracleViewExecutorUtil.execute(api, sourceObject, targetName, viewOwner, request,
                        configuredParamDTOs);
            case "PROCEDURE":
                return oracleProcedureExecutorUtil.execute(api, sourceObject, targetName, targetOwner, request,
                        configuredParamDTOs);
            case "FUNCTION":
                return oracleFunctionExecutorUtil.execute(api, sourceObject, targetName, targetOwner, request,
                        configuredParamDTOs);
            case "PACKAGE":
                return oraclePackageExecutorUtil.execute(api, sourceObject, targetName, targetOwner, request, configuredParamDTOs);
            default:
                log.warn("Unknown target type: {}, generating sample response", targetType);
                return sampleGenerator.generateSampleResponse(api);
        }
    }

    private Object executeTableOperation(OracleTableExecutorUtil oracleTableExecutorUtil,
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
        log.info("Configured parameters count: {}", configuredParamDTOs != null ? configuredParamDTOs.size() : 0);

        switch (operation.toUpperCase()) {
            case "SELECT":
                return oracleTableExecutorUtil.executeSelect(tableName, owner, params, api, configuredParamDTOs);
            case "INSERT":
                return oracleTableExecutorUtil.executeInsert(tableName, owner, params, api, configuredParamDTOs);
            case "UPDATE":
                return oracleTableExecutorUtil.executeUpdate(tableName, owner, params, api, configuredParamDTOs);
            case "DELETE":
                return oracleTableExecutorUtil.executeDelete(tableName, owner, params, api, configuredParamDTOs);
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

    // Private mapping methods
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


    @Override
    protected void setupApiRelationships(GeneratedApiEntity api,
                                         GenerateApiRequestDTO request,
                                         ApiSourceObjectDTO sourceObjectDTO,
                                         DatabaseParameterGeneratorUtil parameterGenerator,
                                         ApiConversionHelper conversionHelper) {

        // Create and set auth config
        if (request.getAuthConfig() != null) {
            ApiAuthConfigEntity authConfig = conversionHelper.createAuthConfigEntity(request.getAuthConfig());
            authConfig.setGeneratedApi(api);
            api.setAuthConfig(authConfig);
        }

        // Create and set request config
        if (request.getRequestBody() != null) {
            ApiRequestConfigEntity requestConfig = conversionHelper.createRequestConfigEntity(request.getRequestBody());
            requestConfig.setGeneratedApi(api);
            api.setRequestConfig(requestConfig);
        }

        // Create and set response config
        if (request.getResponseBody() != null) {
            ApiResponseConfigEntity responseConfig = conversionHelper.createResponseConfigEntity(request.getResponseBody());
            responseConfig.setGeneratedApi(api);
            api.setResponseConfig(responseConfig);
        }

        // Create and set schema config
        ApiSchemaConfigEntity schemaConfig = conversionHelper.createSchemaConfigEntity(sourceObjectDTO);
        schemaConfig.setGeneratedApi(api);
        api.setSchemaConfig(schemaConfig);

        // Create and set settings
        if (request.getSettings() != null) {
            ApiSettingsEntity settings = conversionHelper.createSettingsEntity(request.getSettings());
            settings.setGeneratedApi(api);
            api.setSettings(settings);
        }

        // ============ REPLACE parameters (CLEAR then ADD) ============
        api.clearParameters();  // Clear existing

        List<ApiParameterEntity> parameters = parameterGenerator.generateParameters(
                sourceObjectDTO, request.getParameters(), api.getId());
        parameters.forEach(param -> param.setGeneratedApi(api));
        api.replaceParameters(parameters);
        log.info("Recreated {} parameters for API: {}", parameters.size(), api.getId());

        // ============ REPLACE headers (CLEAR then ADD) ============
        api.clearHeaders();  // Clear existing

        if (request.getHeaders() != null) {
            List<ApiHeaderEntity> headers = conversionHelper.createHeaderEntities(request.getHeaders(), api);
            headers.forEach(header -> header.setGeneratedApi(api));
            api.replaceHeaders(headers);
            log.info("Recreated {} headers for API: {}", headers.size(), api.getId());
        }

        // ============ REPLACE response mappings (CLEAR then ADD) ============
        api.clearResponseMappings();  // Clear existing

        if (request.getResponseMappings() != null) {
            List<ApiResponseMappingEntity> responseMappings =
                    conversionHelper.createResponseMappingEntities(request.getResponseMappings(), api.getId());
            responseMappings.forEach(mapping -> mapping.setGeneratedApi(api));
            api.replaceResponseMappings(responseMappings);
            log.info("Recreated {} response mappings for API: {}", responseMappings.size(), api.getId());
        }
    }

    @Override
    public Object executeAgainstDatabase(GeneratedApiEntity api,
                                         ApiSourceObjectDTO sourceObject,
                                         ExecuteApiRequestDTO validatedRequest,
                                         List<ApiParameterDTO> configuredParamDTOs) {

        log.info("Executing Oracle operation for API: {}", api.getId());

        // ============ CHECK FOR CUSTOM QUERY FIRST ============
        if (sourceObject != null && sourceObject.isCustomQuery()) {
            log.info("Executing custom SELECT query for Oracle API: {}", api.getApiCode());
            return customQueryExecutionHelper.executeCustomQuery(
                    api, sourceObject, validatedRequest, configuredParamDTOs, oracleJdbcTemplate
            );
        }

        // ============ EXISTING LOGIC FOR REGULAR DATABASE OBJECTS ============
        ApiSchemaConfigEntity schemaConfig = api.getSchemaConfig();
        if (schemaConfig == null) {
            throw new RuntimeException("Schema configuration not found");
        }

        String objectType = schemaConfig.getObjectType();

        switch (objectType.toUpperCase()) {
            case "TABLE":
                return executeTableOperation(api, sourceObject, validatedRequest, configuredParamDTOs);
            case "VIEW":
                return executeViewOperation(api, sourceObject, validatedRequest, configuredParamDTOs);
            case "PROCEDURE":
                return executeProcedureOperation(api, sourceObject, validatedRequest, configuredParamDTOs);
            case "FUNCTION":
                return executeFunctionOperation(api, sourceObject, validatedRequest, configuredParamDTOs);
            case "PACKAGE":
                return executePackageOperation(api, sourceObject, validatedRequest, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported Oracle object type: " + objectType);
        }
    }

    private Object executeTableOperation(GeneratedApiEntity api,
                                         ApiSourceObjectDTO sourceObject,
                                         ExecuteApiRequestDTO validatedRequest,
                                         List<ApiParameterDTO> configuredParamDTOs) {
        String tableName = api.getSchemaConfig().getObjectName();
        String schema = api.getSchemaConfig().getSchemaName();
        String operation = api.getSchemaConfig().getOperation();

        Map<String, Object> params = new HashMap<>();
        if (validatedRequest.getPathParams() != null) params.putAll(validatedRequest.getPathParams());
        if (validatedRequest.getQueryParams() != null) params.putAll(validatedRequest.getQueryParams());
        if (validatedRequest.getBody() instanceof Map) {
            params.putAll((Map<String, Object>) validatedRequest.getBody());
        }

        switch (operation.toUpperCase()) {
            case "SELECT":
                return oracleTableExecutorUtil.executeSelect(tableName, schema, params, api, configuredParamDTOs);
            case "INSERT":
                return oracleTableExecutorUtil.executeInsert(tableName, schema, params, api, configuredParamDTOs);
            case "UPDATE":
                return oracleTableExecutorUtil.executeUpdate(tableName, schema, params, api, configuredParamDTOs);
            case "DELETE":
                return oracleTableExecutorUtil.executeDelete(tableName, schema, params, api, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported table operation: " + operation);
        }
    }

    private Object executeViewOperation(GeneratedApiEntity api,
                                        ApiSourceObjectDTO sourceObject,
                                        ExecuteApiRequestDTO validatedRequest,
                                        List<ApiParameterDTO> configuredParamDTOs) {
        return oracleViewExecutorUtil.execute(
                api,
                sourceObject,
                api.getSchemaConfig().getObjectName(),
                api.getSchemaConfig().getSchemaName(),
                validatedRequest,
                configuredParamDTOs
        );
    }

    private Object executeProcedureOperation(GeneratedApiEntity api,
                                             ApiSourceObjectDTO sourceObject,
                                             ExecuteApiRequestDTO validatedRequest,
                                             List<ApiParameterDTO> configuredParamDTOs) {
        return oracleProcedureExecutorUtil.execute(
                api,
                sourceObject,
                api.getSchemaConfig().getObjectName(),
                api.getSchemaConfig().getSchemaName(),
                validatedRequest,
                configuredParamDTOs
        );
    }

    private Object executeFunctionOperation(GeneratedApiEntity api,
                                            ApiSourceObjectDTO sourceObject,
                                            ExecuteApiRequestDTO validatedRequest,
                                            List<ApiParameterDTO> configuredParamDTOs) {
        return oracleFunctionExecutorUtil.execute(
                api,
                sourceObject,
                api.getSchemaConfig().getObjectName(),
                api.getSchemaConfig().getSchemaName(),
                validatedRequest,
                configuredParamDTOs
        );
    }

    private Object executePackageOperation(GeneratedApiEntity api,
                                           ApiSourceObjectDTO sourceObject,
                                           ExecuteApiRequestDTO validatedRequest,
                                           List<ApiParameterDTO> configuredParamDTOs) {
        return oraclePackageExecutorUtil.execute(
                api,
                sourceObject,
                api.getSchemaConfig().getObjectName(),
                api.getSchemaConfig().getSchemaName(),
                validatedRequest,
                configuredParamDTOs
        );
    }

    @Override
    protected boolean checkObjectExistsInDatabase(String schema, String objectName,
                                                  String objectType, String databaseType) {
        try {
            String sql = "SELECT COUNT(*) FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = ?";
            Integer count = oracleJdbcTemplate.queryForObject(sql, Integer.class, schema, objectName, objectType);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("Error checking object existence in Oracle: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected String resolveSchemaFromDatabase(String objectName, String defaultSchema, String databaseType) {
        try {
            // Try to find the object in accessible schemas
            String sql = "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND OWNER NOT IN ('SYS', 'SYSTEM') AND ROWNUM = 1";
            List<String> owners = oracleJdbcTemplate.queryForList(sql, String.class, objectName);
            if (!owners.isEmpty()) {
                return owners.get(0);
            }

            // Check if it's a synonym
            sql = "SELECT TABLE_OWNER FROM ALL_SYNONYMS WHERE SYNONYM_NAME = ? AND ROWNUM = 1";
            owners = oracleJdbcTemplate.queryForList(sql, String.class, objectName);
            if (!owners.isEmpty()) {
                return owners.get(0);
            }
        } catch (Exception e) {
            log.debug("Error resolving schema in Oracle: {}", e.getMessage());
        }

        return defaultSchema;
    }
}