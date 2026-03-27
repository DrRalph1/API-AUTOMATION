package com.usg.apiAutomation.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.helpers.apiEngine.ApiConversionHelper;
import com.usg.apiAutomation.helpers.apiEngine.ApiResponseHelper;
import com.usg.apiAutomation.repositories.apiGenerationEngine.*;
import com.usg.apiAutomation.utils.apiEngine.DatabaseParameterGeneratorUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseApiExecutionHelper {

    protected final ApiResponseHelper responseHelper;
    protected final LoggerUtil loggerUtil;
    protected final ApiConversionHelper conversionHelper;
    protected final TransactionTemplate transactionTemplate;
    
    // Connection pool management
    private final Map<String, Boolean> objectExistenceCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> schemaCache = new ConcurrentHashMap<>();
    
    // Performance configuration
    protected static final int BATCH_SIZE = 500;
    protected static final int FETCH_SIZE = 1000;
    protected static final int QUERY_TIMEOUT_SECONDS = 30;
    protected static final int MAX_ROWS = 10000;
    protected static final int MAX_RETRY_ATTEMPTS = 3;
    protected static final long RETRY_DELAY_MS = 1000;
    
    // Cache TTL in milliseconds
    protected static final long OBJECT_CACHE_TTL = 300000; // 5 minutes
    protected static final long SCHEMA_CACHE_TTL = 600000; // 10 minutes
    
    private final Map<String, Long> objectCacheTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Long> schemaCacheTimestamps = new ConcurrentHashMap<>();
    
    // ==================== CORE METHODS WITH CONNECTION MANAGEMENT ====================
    
    /**
     * Execute with automatic retry and connection recovery
     */
    @Retryable(
        value = {DataAccessException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_DELAY_MS, multiplier = 2)
    )
    protected <T> T executeWithRetry(Supplier<T> operation, String operationName, String databaseType) {
        long startTime = System.nanoTime();
        
        try {
            T result = operation.get();
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            
            if (duration > 1000) {
                log.warn("Slow {} operation on {}: {} ms", databaseType, operationName, duration);
            }
            
            return result;
            
        } catch (DataAccessException e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            
            // Check for connection pool exhaustion
            if (e.getMessage() != null && e.getMessage().contains("Connection is not available")) {
                log.error("Connection pool exhausted for {} after {} ms", databaseType, duration);
                // Force connection pool refresh
                refreshConnectionPool(databaseType);
            }
            
            log.error("{} operation failed on {} after {} ms: {}", operationName, databaseType, duration, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Refresh connection pool when exhaustion detected
     */
    protected void refreshConnectionPool(String databaseType) {
        try {
            log.warn("Refreshing connection pool for {}", databaseType);
            JdbcTemplate template = getJdbcTemplate(databaseType);
            if (template != null && template.getDataSource() != null) {
                // Force connection validation
                template.queryForObject("SELECT 1", Integer.class);
                log.info("Connection pool refresh successful for {}", databaseType);
            }
        } catch (Exception e) {
            log.error("Failed to refresh connection pool: {}", e.getMessage());
        }
    }
    
    /**
     * Ensure connection is valid and release if stuck
     */
    protected void ensureConnectionValid(JdbcTemplate template, String databaseType) {
        try {
            if (template != null) {
                String testQuery = "postgresql".equalsIgnoreCase(databaseType) ? "SELECT 1" : "SELECT 1 FROM DUAL";
                template.queryForObject(testQuery, Integer.class);
            }
        } catch (Exception e) {
            log.warn("Connection validation failed for {}: {}", databaseType, e.getMessage());
            refreshConnectionPool(databaseType);
        }
    }
    
    // ==================== EXISTING METHODS WITH ENHANCEMENTS ====================
    
    @Retryable(
        value = {DataAccessException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_DELAY_MS, multiplier = 2)
    )
    public GeneratedApiEntity createAndSaveApiEntity(
            GenerateApiRequestDTO request,
            ApiSourceObjectDTO sourceObjectDTO,
            CollectionInfoDTO collectionInfo,
            String endpointPath,
            String performedBy,
            String sourceRequestId,
            GeneratedAPIRepository repository,
            ObjectMapper objectMapper,
            DatabaseParameterGeneratorUtil parameterGenerator,
            ApiConversionHelper conversionHelper,
            String databaseType) {

        return transactionTemplate.execute(status -> {
            GeneratedApiEntity api = new GeneratedApiEntity();

            api.setApiName(request.getApiName());
            api.setApiCode(request.getApiCode());
            api.setDescription(request.getDescription());
            api.setVersion(request.getVersion());
            api.setHttpMethod(request.getHttpMethod());
            api.setBasePath(request.getBasePath());
            api.setEndpointPath(endpointPath);
            api.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
            api.setIsActive(true);
            api.setCategory(request.getCategory());
            api.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
            api.setCreatedAt(LocalDateTime.now());
            api.setCreatedBy(performedBy);
            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);
            api.setOwner(performedBy);
            api.setTotalCalls(0L);
            api.setDatabaseType(databaseType);

            if (sourceObjectDTO != null) {
                try {
                    Map<String, Object> sourceObjectMap = objectMapper.convertValue(
                            sourceObjectDTO,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    api.setSourceObjectInfo(sourceObjectMap);
                } catch (Exception e) {
                    log.error("Error converting source object to map", e);
                }
            }

            if (collectionInfo != null) {
                try {
                    Map<String, Object> collectionInfoMap = objectMapper.convertValue(
                            collectionInfo,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    api.setCollectionInfo(collectionInfoMap);
                } catch (Exception e) {
                    log.error("Error converting collection info to map", e);
                }
            }

            api.setSourceRequestId(sourceRequestId);
            setupApiRelationships(api, request, sourceObjectDTO, parameterGenerator, conversionHelper);

            GeneratedApiEntity savedApi = repository.save(api);
            log.info("Created API entity with ID: {}, Database Type: {}", savedApi.getId(), savedApi.getDatabaseType());

            return savedApi;
        });
    }

    public GeneratedApiEntity getApiEntity(GeneratedAPIRepository repository, String apiId) {
        return repository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API not found: " + apiId));
    }

    @Retryable(
        value = {DataAccessException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 500)
    )
    @Transactional
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

        if (request.getTags() != null) {
            api.setTags(request.getTags());
        }

        if (sourceObjectDTO != null) {
            try {
                Map<String, Object> sourceObjectMap = new HashMap<>();
                sourceObjectMap.put("objectName", sourceObjectDTO.getObjectName());
                sourceObjectMap.put("objectType", sourceObjectDTO.getObjectType());
                sourceObjectMap.put("owner", sourceObjectDTO.getOwner());
                sourceObjectMap.put("OWNER", sourceObjectDTO.getOwner());
                sourceObjectMap.put("schemaName", sourceObjectDTO.getSchemaName());
                sourceObjectMap.put("SchemaName", sourceObjectDTO.getSchemaName());
                sourceObjectMap.put("SCHEMA_NAME", sourceObjectDTO.getSchemaName());
                sourceObjectMap.put("operation", sourceObjectDTO.getOperation());
                sourceObjectMap.put("databaseType", sourceObjectDTO.getDatabaseType());

                if (sourceObjectDTO.getIsSynonym() != null) {
                    sourceObjectMap.put("isSynonym", sourceObjectDTO.getIsSynonym());
                    sourceObjectMap.put("targetType", sourceObjectDTO.getTargetType());
                    sourceObjectMap.put("targetName", sourceObjectDTO.getTargetName());
                    sourceObjectMap.put("targetOwner", sourceObjectDTO.getTargetOwner());
                }

                api.setSourceObjectInfo(sourceObjectMap);
            } catch (Exception e) {
                log.error("Error converting source object to map", e);
            }
        }

        if (collectionInfo != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> collectionInfoMap = objectMapper.convertValue(
                        collectionInfo,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                api.setCollectionInfo(collectionInfoMap);
            } catch (Exception e) {
                log.error("Error converting collection info to map", e);
            }
        }

        log.info("Updated API entity: {}, Preserving database type: {}", api.getId(), api.getDatabaseType());
    }

    public void clearApiRelationships(GeneratedApiEntity api) {
        if (api == null) return;

        log.debug("Clearing relationships for API: {}", api.getId());

        api.setSchemaConfig(null);
        api.setAuthConfig(null);
        api.setRequestConfig(null);
        api.setResponseConfig(null);
        api.setSettings(null);

        if (api.getParameters() != null) api.getParameters().clear();
        if (api.getResponseMappings() != null) api.getResponseMappings().clear();
        if (api.getHeaders() != null) api.getHeaders().clear();
        if (api.getTests() != null) api.getTests().clear();

        log.debug("Successfully cleared relationships for API: {}", api.getId());
    }

    public void recreateApiRelationships(GeneratedApiEntity api,
                                         GenerateApiRequestDTO request,
                                         ApiSourceObjectDTO sourceObjectDTO,
                                         DatabaseParameterGeneratorUtil parameterGenerator,
                                         ApiConversionHelper conversionHelper) {

        log.debug("Recreating relationships for API: {}", api.getId());

        if (api.getParameters() == null) api.setParameters(new ArrayList<>());
        if (api.getHeaders() == null) api.setHeaders(new ArrayList<>());
        if (api.getResponseMappings() == null) api.setResponseMappings(new ArrayList<>());
        if (api.getTests() == null) api.setTests(new ArrayList<>());

        setupApiRelationships(api, request, sourceObjectDTO, parameterGenerator, conversionHelper);

        log.debug("Successfully recreated relationships for API: {}", api.getId());
    }

    public ExecuteApiRequestDTO prepareValidatedRequest(GeneratedApiEntity api, ExecuteApiRequestDTO executeRequest) {
        if (executeRequest == null) {
            executeRequest = new ExecuteApiRequestDTO();
        }

        if (executeRequest.getRequestId() == null) {
            executeRequest.setRequestId(UUID.randomUUID().toString());
        }

        if (executeRequest.getUrl() == null && api.getBasePath() != null && api.getEndpointPath() != null) {
            executeRequest.setUrl(api.getBasePath() + api.getEndpointPath());
        }

        return executeRequest;
    }

    @Retryable(
        value = {DataAccessException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 500)
    )
    public void logExecution(ApiExecutionLogRepository logRepository, GeneratedApiEntity api,
                             ExecuteApiRequestDTO request, Object response, int statusCode,
                             long executionTime, String performedBy, String clientIp,
                             String userAgent, String errorMessage, ObjectMapper objectMapper) {
        try {
            ApiExecutionLogEntity logEntity = ApiExecutionLogEntity.builder()
                    .executedAt(LocalDateTime.now())
                    .executedBy(performedBy)
                    .executionTimeMs(executionTime)
                    .responseStatus(statusCode)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .errorMessage(errorMessage)
                    .build();

            if (api != null) logEntity.setGeneratedApi(api);
            if (request != null) logEntity.setRequestId(request.getRequestId());

            if (request != null && (request.getPathParams() != null || request.getQueryParams() != null || request.getHeaders() != null)) {
                Map<String, Object> requestParams = new HashMap<>();
                if (request.getPathParams() != null) requestParams.put("pathParams", request.getPathParams());
                if (request.getQueryParams() != null) requestParams.put("queryParams", request.getQueryParams());
                if (request.getHeaders() != null) requestParams.put("headers", request.getHeaders());
                if (request.getUrl() != null) requestParams.put("url", request.getUrl());
                if (request.getHttpMethod() != null) requestParams.put("httpMethod", request.getHttpMethod());
                logEntity.setRequestParams(requestParams);
            }

            if (request != null && request.getBody() != null) {
                try {
                    if (request.getBody() instanceof Map) {
                        logEntity.setRequestBody((Map<String, Object>) request.getBody());
                    } else {
                        Map<String, Object> bodyMap = new HashMap<>();
                        bodyMap.put("value", request.getBody());
                        logEntity.setRequestBody(bodyMap);
                    }
                } catch (Exception e) {
                    log.warn("Failed to convert request body to map: {}", e.getMessage());
                    Map<String, Object> errorBody = new HashMap<>();
                    errorBody.put("error", "Failed to serialize body");
                    errorBody.put("type", request.getBody().getClass().getSimpleName());
                    logEntity.setRequestBody(errorBody);
                }
            }

            if (response != null) {
                try {
                    if (response instanceof Map) {
                        logEntity.setResponseBody((Map<String, Object>) response);
                    } else {
                        Map<String, Object> responseMap = new HashMap<>();
                        responseMap.put("value", response);
                        logEntity.setResponseBody(responseMap);
                    }
                } catch (Exception e) {
                    log.warn("Failed to convert response to map: {}", e.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Failed to serialize response");
                    errorResponse.put("type", response.getClass().getSimpleName());
                    logEntity.setResponseBody(errorResponse);
                }
            }

            try {
                logRepository.save(logEntity);
                log.debug("Execution log saved successfully with ID: {}", logEntity.getId());
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic locking failure for execution log (non-critical): {}", e.getMessage());
            } catch (Exception e) {
                log.error("Failed to save execution log (non-critical): {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Error in logExecution (non-critical): {}", e.getMessage(), e);
        }
    }

    public void updateApiStats(GeneratedApiEntity api, GeneratedAPIRepository repository) {
        if (api != null) {
            api.setTotalCalls(api.getTotalCalls() != null ? api.getTotalCalls() + 1 : 1);
            api.setLastCalledAt(LocalDateTime.now());
            repository.save(api);
        }
    }

    public ApiTestEntity saveTestResult(ApiTestRepository testRepository, GeneratedApiEntity api,
                                        ApiTestRequestDTO testRequest, ExecuteApiResponseDTO executionResult,
                                        boolean passed, long executionTime, String performedBy,
                                        ObjectMapper objectMapper) {

        Map<String, Object> testData = new HashMap<>();
        if (testRequest.getPathParams() != null) testData.put("pathParams", testRequest.getPathParams());
        if (testRequest.getQueryParams() != null) testData.put("queryParams", testRequest.getQueryParams());
        if (testRequest.getHeaders() != null) testData.put("headers", testRequest.getHeaders());
        if (testRequest.getBody() != null) testData.put("body", testRequest.getBody());

        Map<String, Object> expectedResponse = new HashMap<>();
        if (testRequest.getExpectedResponse() != null) {
            if (testRequest.getExpectedResponse() instanceof Map) {
                expectedResponse = (Map<String, Object>) testRequest.getExpectedResponse();
            } else {
                expectedResponse.put("value", testRequest.getExpectedResponse());
            }
        }

        Map<String, Object> actualResponse = new HashMap<>();
        if (executionResult.getData() != null) {
            if (executionResult.getData() instanceof Map) {
                actualResponse = (Map<String, Object>) executionResult.getData();
            } else {
                actualResponse.put("value", executionResult.getData());
            }
        }

        Map<String, Object> executionResults = new HashMap<>();
        executionResults.put("responseCode", executionResult.getResponseCode());
        executionResults.put("success", executionResult.getSuccess());
        executionResults.put("message", executionResult.getMessage());
        executionResults.put("passed", passed);

        ApiTestEntity test = ApiTestEntity.builder()
                .id(UUID.randomUUID().toString())
                .testName(testRequest.getTestName())
                .testType(testRequest.getTestType() != null ? testRequest.getTestType() : "UNIT")
                .status(passed ? "PASSED" : "FAILED")
                .executionTimeMs(executionTime)
                .executedAt(LocalDateTime.now())
                .executedBy(performedBy)
                .testData(testData)
                .expectedResponse(expectedResponse)
                .actualResponse(actualResponse)
                .executionResults(executionResults)
                .testConnection(true)
                .testObjectAccess(true)
                .testDataTypes(true)
                .testQueryPerformance(true)
                .testSQLInjection(true)
                .testAuthentication(true)
                .testAuthorization(true)
                .build();

        test.setGeneratedApi(api);
        return testRepository.save(test);
    }

    // ==================== HELPER METHODS ====================
    
    protected JdbcTemplate getJdbcTemplate(String databaseType) {
        // This should be implemented by subclasses
        return null;
    }
    
    protected boolean objectExists(String schema, String objectName, String objectType, String databaseType) {
        String cacheKey = String.format("%s:%s:%s:%s", databaseType, schema, objectName, objectType).toLowerCase();
        
        Long timestamp = objectCacheTimestamps.get(cacheKey);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < OBJECT_CACHE_TTL) {
            Boolean exists = objectExistenceCache.get(cacheKey);
            if (exists != null) {
                return exists;
            }
        }
        
        boolean exists = checkObjectExistsInDatabase(schema, objectName, objectType, databaseType);
        
        objectExistenceCache.put(cacheKey, exists);
        objectCacheTimestamps.put(cacheKey, System.currentTimeMillis());
        
        return exists;
    }
    
    protected String resolveSchema(String objectName, String defaultSchema, String databaseType) {
        String cacheKey = String.format("%s:%s:%s", databaseType, objectName, defaultSchema).toLowerCase();
        
        Long timestamp = schemaCacheTimestamps.get(cacheKey);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < SCHEMA_CACHE_TTL) {
            Map<String, Object> cached = schemaCache.get(cacheKey);
            if (cached != null && cached.containsKey("schema")) {
                return (String) cached.get("schema");
            }
        }
        
        String resolvedSchema = resolveSchemaFromDatabase(objectName, defaultSchema, databaseType);
        
        Map<String, Object> cacheEntry = new HashMap<>();
        cacheEntry.put("schema", resolvedSchema);
        cacheEntry.put("timestamp", System.currentTimeMillis());
        schemaCache.put(cacheKey, cacheEntry);
        schemaCacheTimestamps.put(cacheKey, System.currentTimeMillis());
        
        return resolvedSchema;
    }
    
    public void clearCaches() {
        objectExistenceCache.clear();
        schemaCache.clear();
        objectCacheTimestamps.clear();
        schemaCacheTimestamps.clear();
        log.info("All caches cleared");
    }
    
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("objectCacheSize", objectExistenceCache.size());
        stats.put("schemaCacheSize", schemaCache.size());
        stats.put("objectCacheTTL", OBJECT_CACHE_TTL);
        stats.put("schemaCacheTTL", SCHEMA_CACHE_TTL);
        return stats;
    }
    
    // ==================== ABSTRACT METHODS ====================
    
    protected abstract boolean checkObjectExistsInDatabase(String schema, String objectName, 
                                                           String objectType, String databaseType);
    
    protected abstract String resolveSchemaFromDatabase(String objectName, String defaultSchema, String databaseType);
    
    protected abstract void setupApiRelationships(GeneratedApiEntity api,
                                                  GenerateApiRequestDTO request,
                                                  ApiSourceObjectDTO sourceObjectDTO,
                                                  DatabaseParameterGeneratorUtil parameterGenerator,
                                                  ApiConversionHelper conversionHelper);
    
    public abstract Object executeAgainstDatabase(GeneratedApiEntity api,
                                                  ApiSourceObjectDTO sourceObject,
                                                  ExecuteApiRequestDTO validatedRequest,
                                                  List<ApiParameterDTO> configuredParamDTOs) throws SQLException;
}