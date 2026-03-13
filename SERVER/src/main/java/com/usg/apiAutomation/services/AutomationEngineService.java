package com.usg.apiAutomation.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.postgres.codeBase.*;
import com.usg.apiAutomation.repositories.postgres.codeBase.FolderRepository;
import com.usg.apiAutomation.repositories.postgres.collections.AuthConfigRepository;
import com.usg.apiAutomation.repositories.postgres.collections.HeaderRepository;
import com.usg.apiAutomation.repositories.postgres.collections.ParameterRepository;
import com.usg.apiAutomation.repositories.postgres.documentation.*;
import com.usg.apiAutomation.helpers.ApiValidatorHelper;
import com.usg.apiAutomation.helpers.apiEngine.*;
import com.usg.apiAutomation.utils.apiEngine.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.usg.apiAutomation.utils.apiEngine.executor.*;
import com.usg.apiAutomation.utils.apiEngine.generator.CodeBaseGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.generator.CollectionsGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.generator.DocumentationGeneratorUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationEngineService {

    // ==================== REPOSITORIES ====================
    private final GeneratedAPIRepository generatedAPIRepository;
    private final ApiExecutionLogRepository executionLogRepository;
    private final ApiTestRepository apiTestRepository;
    private final CollectionRepository codeBaseCollectionRepository;
    private final FolderRepository codeBaseFolderRepository;
    private final RequestRepository codeBaseRequestRepository;
    private final ImplementationRepository implementationRepository;
    private final com.usg.apiAutomation.repositories.postgres.collections.CollectionRepository collectionsCollectionRepository;
    private final com.usg.apiAutomation.repositories.postgres.collections.FolderRepository collectionsFolderRepository;
    private final com.usg.apiAutomation.repositories.postgres.collections.RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;
    private final APICollectionRepository docCollectionRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.FolderRepository docFolderRepository;
    private final APIEndpointRepository endpointRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.HeaderRepository docHeaderRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.ParameterRepository docParameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;

    // ==================== HELPERS (Business Logic) ====================
    private final ApiValidationHelper validationHelper;
    private final ApiConversionHelper conversionHelper;
    private final ApiResponseHelper responseHelper;
    private final ApiExecutionHelper executionHelper;
    private final ApiComponentHelper componentHelper;
    private final ApiMetadataHelper metadataHelper;

    // ==================== UTILS (Technical Utilities) ====================
    private final GenUrlBuilderUtil genUrlBuilder;
    private final OracleTypeMapperUtil typeMapper;
    private final OracleObjectResolverUtil objectResolver;
    private final ParameterValidatorUtil parameterValidator;
    private final AuthenticationServiceUtil authenticationService;

    // ==================== EXECUTORS ====================
    private final TableExecutorUtil tableExecutorUtil;
    private final ViewExecutorUtil viewExecutorUtil;
    private final ProcedureExecutorUtil procedureExecutorUtil;
    private final FunctionExecutorUtil functionExecutorUtil;
    private final PackageExecutorUtil packageExecutorUtil;

    // ==================== GENERATORS ====================
    private final CodeBaseGeneratorUtil codeBaseGeneratorUtil;
    private final CollectionsGeneratorUtil collectionsGeneratorUtil;
    private final DocumentationGeneratorUtil documentationGeneratorUtil;
    private final CodeLanguageGeneratorUtil codeLanguageGeneratorUtil;
    private final ParameterGeneratorUtil parameterGeneratorUtil;

    // ==================== EXTERNAL DEPENDENCIES ====================
    private final ObjectMapper objectMapper;
    private final LoggerUtil loggerUtil;
    private final ApiValidatorHelper validatorService;
    private final OracleSchemaService oracleSchemaService;
    private final EntityManager entityManager;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    // ==================== PUBLIC API METHODS ====================

    @Transactional
    public GeneratedApiResponseDTO generateApi(String requestId, String performedBy, GenerateApiRequestDTO request) {
        long startTime = System.currentTimeMillis();
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Generating API: " + request.getApiName() + " by: " + performedBy);

            // Validate API code uniqueness
            validationHelper.validateApiCodeUniqueness(generatedAPIRepository, request.getApiCode());

            // Convert and validate source object
            ApiSourceObjectDTO sourceObjectDTO = conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), oracleSchemaService, objectMapper);

            // Validate collection info
            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());

            // Build endpoint path with parameters - FIX THIS METHOD
            String endpointPath = buildEndpointPathFromRequest(request);

            // Create and save main API entity
            GeneratedApiEntity savedApi = executionHelper.createAndSaveApiEntity(
                    request, sourceObjectDTO, collectionInfo, endpointPath, performedBy, requestId,
                    generatedAPIRepository, objectMapper, parameterGeneratorUtil, conversionHelper);

            // Generate related components
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(savedApi);
            String codeBaseRequestId = codeBaseGeneratorUtil.generate(savedApi, performedBy, request, collectionInfo);
            String collectionId = collectionsGeneratorUtil.generate(savedApi, performedBy, request, collectionInfo);
            String docCollectionId = documentationGeneratorUtil.generate(savedApi, performedBy, request,
                    codeBaseRequestId, collectionId, collectionInfo);

            // Build response
            GeneratedApiResponseDTO response = responseHelper.buildGenerateApiResponse(
                    savedApi, genUrlInfo, codeBaseRequestId, collectionId, docCollectionId,
                    collectionInfo, genUrlBuilder, conversionHelper, this::generateApiCode);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + savedApi.getId() +
                    " in " + (System.currentTimeMillis() - startTime) + "ms");

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error generating API: " + e.getMessage());
            log.error("Error generating API", e);
            throw new RuntimeException("Failed to generate API: " + e.getMessage(), e);
        }
    }


    @Transactional
    public GeneratedApiResponseDTO updateApi(String requestId, String apiId, String performedBy,
                                             GenerateApiRequestDTO request) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Updating API: " + apiId + " by: " + performedBy);

            GeneratedApiEntity api = executionHelper.getApiEntity(generatedAPIRepository, apiId);

            // Check API code uniqueness if changed
            validationHelper.validateApiCodeUniquenessOnUpdate(
                    generatedAPIRepository, api.getApiCode(), request.getApiCode());

            // Convert and validate source object
            ApiSourceObjectDTO sourceObjectDTO = conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), oracleSchemaService, objectMapper);

            // Validate collection info
            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());

            // Update API entity
            executionHelper.updateApiEntity(api, request, sourceObjectDTO, collectionInfo, performedBy);

            // Clear and recreate relationships
            executionHelper.clearApiRelationships(api);
            executionHelper.recreateApiRelationships(api, request, sourceObjectDTO,
                    parameterGeneratorUtil, conversionHelper);

            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);

            // Update components
            componentHelper.updateComponents(savedApi, performedBy, request, collectionInfo,
                    shouldRegenerateComponents(request),
                    codeBaseGeneratorUtil, collectionsGeneratorUtil, documentationGeneratorUtil,
                    this::updateCodeBase, this::updateCollections, this::updateDocumentation,
                    this::getCodeBaseRequestId, this::getCollectionsCollectionId);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API updated successfully: " + savedApi.getId());

            return conversionHelper.mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error updating API: " + e.getMessage());
            throw new RuntimeException("Failed to update API: " + e.getMessage(), e);
        }
    }


    @Transactional
    public ExecuteApiResponseDTO executeApi(String requestId, String performedBy,
                                            String apiId, ExecuteApiRequestDTO executeRequest,
                                            String clientIp, String userAgent,
                                            HttpServletRequest httpServletRequest) {

        long startTime = System.currentTimeMillis();

        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Executing API: " + apiId + " by: " + performedBy);

            // 1. Get the API entity
            GeneratedApiEntity api = executionHelper.getApiEntity(generatedAPIRepository, apiId);

            // 2. Check if API is active
            if (!api.getIsActive()) {
                return responseHelper.createErrorResponse(403,
                        "API is inactive and cannot be executed", startTime);
            }

            // 3. Validate request structure
            if (executeRequest == null) {
                executeRequest = new ExecuteApiRequestDTO();
                executeRequest.setRequestId(UUID.randomUUID().toString());
            }

            // 4. CRITICAL: Extract HTTP method from HttpServletRequest
            String httpMethod = null;
            if (httpServletRequest != null) {
                httpMethod = httpServletRequest.getMethod();
                log.info("HTTP method from HttpServletRequest: {}", httpMethod);

                // Set it directly in the DTO
                executeRequest.setHttpMethod(httpMethod);
            }

            // 5. Prepare and validate the request
            ExecuteApiRequestDTO validatedRequest = executionHelper.prepareValidatedRequest(api, executeRequest);

            // 6. Ensure HTTP method is preserved (in case prepareValidatedRequest cleared it)
            if (validatedRequest.getHttpMethod() == null && httpMethod != null) {
                validatedRequest.setHttpMethod(httpMethod);
                log.info("Re-set HTTP method in validatedRequest: {}", httpMethod);
            }

            // 7. Validate HTTP method
            if (!validateHttpMethod(api, validatedRequest.getHttpMethod())) {
                String errorMsg = String.format("HTTP method not allowed. Expected: %s, Actual: %s",
                        api.getHttpMethod(), validatedRequest.getHttpMethod() != null ?
                                validatedRequest.getHttpMethod() : "null");

                log.warn("HTTP method mismatch. {}", errorMsg);

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 405, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(405, errorMsg, startTime);
            }

            // 8. Validate authentication
            AuthenticationServiceUtil.AuthenticationResult authResult =
                    authenticationService.validateAuthentication(api, validatedRequest);
            if (!authResult.isAuthenticated()) {
                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 401, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent,
                        "Authentication failed: " + authResult.getReason(), objectMapper);

                return responseHelper.createErrorResponse(401,
                        "Authentication failed: " + authResult.getReason(), startTime);
            }

            // 9. Get all API parameters and log them
            List<ApiParameterEntity> apiParameters = api.getParameters();
            log.info("API parameter definitions:");
            apiParameters.forEach(p ->
                    log.info("  - {}: type={}, location={}, required={}",
                            p.getKey(), p.getParameterType(), p.getParameterLocation(), p.getRequired()));

            // 10. Create consolidated params using the enhanced method that includes headers
            Map<String, Object> consolidatedParams = createConsolidatedParamsWithHeaders(validatedRequest);

            // 11. AUTO-ADD CONTENT-TYPE HEADER IF MISSING BUT REQUIRED
            boolean contentTypeRequired = apiParameters.stream()
                    .anyMatch(p -> "Content-Type".equalsIgnoreCase(p.getKey()) && p.getRequired());

            boolean contentTypeMissing = !consolidatedParams.containsKey("Content-Type") &&
                    !consolidatedParams.containsKey("content-type");

            if (contentTypeRequired && contentTypeMissing) {
                String contentTypeValue = "application/json";
                log.info("Auto-adding missing required Content-Type header with value: {}", contentTypeValue);

                // Add to consolidated params with original case
                consolidatedParams.put("Content-Type", contentTypeValue);

                // Also add to the request headers for proper execution
                if (validatedRequest.getHeaders() == null) {
                    validatedRequest.setHeaders(new HashMap<>());
                }
                validatedRequest.getHeaders().put("Content-Type", contentTypeValue);

                log.info("Content-Type header auto-added successfully");
            }

            log.info("Consolidated params after extraction: {}", consolidatedParams);
            log.info("Request path params after extraction: {}", validatedRequest.getPathParams());
            log.info("Request query params: {}", validatedRequest.getQueryParams());
            log.info("Request headers: {}", validatedRequest.getHeaders() != null ?
                    validatedRequest.getHeaders().keySet() : "null");
            log.info("Request body: {}", validatedRequest.getBody());
            log.info("Final path params in request: {}", validatedRequest.getPathParams());

            // 12. Validate required parameters - USING ENHANCED METHOD
            Map<String, String> validationErrors = validateRequiredParametersEnhanced(api, consolidatedParams, validatedRequest);

            if (!validationErrors.isEmpty()) {
                String missingParams = String.join(", ", validationErrors.keySet());

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 400, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent,
                        "Required parameter(s) missing: " + missingParams, objectMapper);

                return responseHelper.createErrorResponse(400,
                        "Required parameter(s) missing: " + missingParams, startTime);
            }

            // 13. Authorization check
            if (!validatorService.validateAuthorization(api, performedBy)) {
                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 403, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent,
                        "User not authorized to access this API", objectMapper);

                return responseHelper.createErrorResponse(403,
                        "User not authorized to access this API", startTime);
            }

            // 14. Rate limiting check
            if (!validatorService.checkRateLimit(api, clientIp)) {
                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 429, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent,
                        "Rate limit exceeded", objectMapper);

                return responseHelper.createErrorResponse(429,
                        "Rate limit exceeded. Please try again later.", startTime);
            }

            // 15. Extract source object from API
            ApiSourceObjectDTO sourceObject = conversionHelper.extractSourceObject(api, objectMapper);

            // 16. Convert parameters to DTOs for execution
            List<ApiParameterDTO> configuredParamDTOs = conversionHelper.convertParametersToDTOs(api.getParameters());

            // 17. Execute against Oracle
            Object result;
            try {
                result = executionHelper.executeAgainstOracle(
                        api,
                        sourceObject,
                        validatedRequest,
                        configuredParamDTOs,
                        objectResolver,
                        parameterValidator,
                        tableExecutorUtil,
                        viewExecutorUtil,
                        procedureExecutorUtil,
                        functionExecutorUtil,
                        packageExecutorUtil,
                        this::generateSampleResponse
                );
            } catch (Exception e) {
                log.error("Oracle execution failed: {}", e.getMessage(), e);

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 500, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent,
                        "Database execution error: " + e.getMessage(), objectMapper);

                return responseHelper.createErrorResponse(500,
                        "Database execution error: " + e.getMessage(), startTime);
            }

            // 18. Format the response
            Object formattedResponse = responseHelper.formatResponse(api, result);

            long executionTime = System.currentTimeMillis() - startTime;

            // 19. Update API statistics
            executionHelper.updateApiStats(api, generatedAPIRepository);

            // 20. Log the successful execution
            executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                    formattedResponse, 200, executionTime, performedBy,
                    clientIp, userAgent, null, objectMapper);

            // 21. Build success response
            ExecuteApiResponseDTO response = responseHelper.buildSuccessResponse(
                    formattedResponse, executionTime, api);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API executed successfully: " + apiId +
                    " - Time: " + executionTime + "ms");

            return response;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error executing API: " + e.getMessage());
            log.error("Error executing API: {}", e.getMessage(), e);

            // Try to log the error
            try {
                GeneratedApiEntity api = null;
                try {
                    api = generatedAPIRepository.findById(apiId).orElse(null);
                } catch (Exception ex) {
                    // Ignore
                }

                executionHelper.logExecution(executionLogRepository, api, executeRequest,
                        null, 500, executionTime, performedBy, clientIp, userAgent,
                        e.getMessage(), objectMapper);
            } catch (Exception logError) {
                log.error("Failed to log execution error: {}", logError.getMessage());
            }

            return responseHelper.createSafeErrorResponse(e, startTime);
        }
    }

    /**
     * Validate HTTP method matches API configuration
     */
    private boolean validateHttpMethod(GeneratedApiEntity api, String requestMethod) {
        if (api == null || api.getHttpMethod() == null) {
            log.warn("API or HTTP method not configured");
            return false;
        }

        if (requestMethod == null || requestMethod.trim().isEmpty()) {
            log.warn("Request method is null or empty");
            return false;
        }

        String configuredMethod = api.getHttpMethod().toUpperCase().trim();
        String actualMethod = requestMethod.toUpperCase().trim();

        boolean isValid = configuredMethod.equals(actualMethod);

        if (!isValid) {
            log.warn("HTTP method mismatch. Configured: {}, Actual: {}", configuredMethod, actualMethod);
        } else {
            log.debug("HTTP method validation passed: {}", actualMethod);
        }

        return isValid;
    }



    @Transactional
    public ApiTestResultDTO testApi(String requestId, String performedBy,
                                    String apiId, ApiTestRequestDTO testRequest, HttpServletRequest req) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Testing API: " + apiId + " with test: " + testRequest.getTestName());

            GeneratedApiEntity api = executionHelper.getApiEntity(generatedAPIRepository, apiId);

            long startTime = System.currentTimeMillis();

            ExecuteApiRequestDTO executeRequest = conversionHelper.createExecuteRequest(testRequest, requestId);

            ExecuteApiResponseDTO executionResult = executeApi(requestId, performedBy,
                    apiId, executeRequest, "127.0.0.1", "API-Test", req);

            long executionTime = System.currentTimeMillis() - startTime;

            boolean passed = responseHelper.compareResponses(executionResult, testRequest.getExpectedResponse(), objectMapper);

            // Save test result
            ApiTestEntity testEntity = executionHelper.saveTestResult(
                    apiTestRepository, api, testRequest, executionResult,
                    passed, executionTime, performedBy, objectMapper);

            return responseHelper.buildTestResult(testRequest, passed, executionTime,
                    executionResult.getResponseCode(), executionResult.getData());

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error testing API: " + e.getMessage());
            throw new RuntimeException("Failed to test API: " + e.getMessage(), e);
        }
    }


    public GeneratedApiResponseDTO getApiDetails(String requestId, String apiId) {
        try {
            GeneratedApiEntity api = executionHelper.getApiEntity(generatedAPIRepository, apiId);
            GeneratedApiResponseDTO response = conversionHelper.mapToResponse(api);

            response.setTotalCalls(api.getTotalCalls());
            response.setLastCalledAt(api.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                metadataHelper.addAverageExecutionTime(response, avgTime);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());
            throw new RuntimeException("Failed to get API details: " + e.getMessage(), e);
        }
    }



    public ApiDetailsResponseDTO getCompleteApiDetails(String requestId, String apiId) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Fetching complete API details for: " + apiId);

            GeneratedApiEntity api = generatedAPIRepository.findByIdWithConfigs(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Load all related entities
            List<ApiParameterEntity> parameters = generatedAPIRepository.findParametersByApiId(apiId);
            List<ApiResponseMappingEntity> responseMappings = generatedAPIRepository.findResponseMappingsByApiId(apiId);
            List<ApiHeaderEntity> headers = generatedAPIRepository.findHeadersByApiId(apiId);
            List<ApiTestEntity> tests = generatedAPIRepository.findTestsByApiId(apiId);

            api.setParameters(parameters);
            api.setResponseMappings(responseMappings);
            api.setHeaders(headers);
            api.setTests(tests);

            ApiDetailsResponseDTO response = conversionHelper.mapToApiDetailsResponse(api, objectMapper);

            response.setGeneratedFiles(generateApiCode(api));
            response.setTotalCalls(api.getTotalCalls() != null ? api.getTotalCalls() : 0L);
            response.setLastCalledAt(api.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                metadataHelper.addAverageExecutionTimeToDetails(response, avgTime);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error fetching complete API details: " + e.getMessage());
            throw new RuntimeException("Failed to fetch complete API details: " + e.getMessage(), e);
        }
    }



    public List<ApiExecutionLogDTO> getExecutionLogs(String apiId, LocalDateTime fromDate,
                                                     LocalDateTime toDate, int limit) {
        try {
            List<ApiExecutionLogEntity> logs;

            if (fromDate != null && toDate != null) {
                logs = executionLogRepository.findByGeneratedApiIdAndExecutedAtBetween(
                        apiId, fromDate, toDate);
            } else {
                PageRequest pageRequest = PageRequest.of(0, limit);
                logs = executionLogRepository.findByGeneratedApiIdOrderByExecutedAtDesc(apiId, pageRequest)
                        .getContent();
            }

            return logs.stream()
                    .map(conversionHelper::mapToExecutionLogDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting execution logs: {}", e.getMessage());
            throw new RuntimeException("Failed to get execution logs: " + e.getMessage(), e);
        }
    }


    public List<ApiTestResultDTO> getTestResults(String apiId) {
        try {
            List<ApiTestEntity> tests = apiTestRepository.findByGeneratedApiIdOrderByExecutedAtDesc(apiId);
            return tests.stream()
                    .map(conversionHelper::mapToTestResultDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting test results: {}", e.getMessage());
            throw new RuntimeException("Failed to get test results: " + e.getMessage(), e);
        }
    }


    @Transactional
    public GeneratedApiResponseDTO partialUpdateApi(String requestId, String apiId, String performedBy,
                                                    Map<String, Object> updates) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Partially updating API: " + apiId + " by: " + performedBy);

            // Get existing API
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Apply partial updates
            if (updates.containsKey("apiName")) {
                api.setApiName((String) updates.get("apiName"));
            }
            if (updates.containsKey("description")) {
                api.setDescription((String) updates.get("description"));
            }
            if (updates.containsKey("version")) {
                api.setVersion((String) updates.get("version"));
            }
            if (updates.containsKey("status")) {
                String newStatus = (String) updates.get("status");
                List<String> validStatuses = Arrays.asList("DRAFT", "ACTIVE", "DEPRECATED", "ARCHIVED");
                if (!validStatuses.contains(newStatus)) {
                    throw new RuntimeException("Invalid status: " + newStatus);
                }
                api.setStatus(newStatus);
            }
            if (updates.containsKey("category")) {
                api.setCategory((String) updates.get("category"));
            }
            if (updates.containsKey("tags")) {
                api.setTags((List<String>) updates.get("tags"));
            }

            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);

            // Save updated API
            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API partially updated successfully: " + savedApi.getId());

            return conversionHelper.mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error partially updating API: " + e.getMessage());
            throw new RuntimeException("Failed to partially update API: " + e.getMessage(), e);
        }
    }


    @Transactional
    public GeneratedApiResponseDTO updateApiStatus(String apiId, String status, String performedBy) {
        try {
            GeneratedApiEntity api = executionHelper.getApiEntity(generatedAPIRepository, apiId);

            validationHelper.validateApiStatus(status);

            api.setStatus(status);
            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);

            GeneratedApiEntity updatedApi = generatedAPIRepository.save(api);

            loggerUtil.log("apiGeneration", "API status updated: " + apiId +
                    " to " + status + " by: " + performedBy);

            return conversionHelper.mapToResponse(updatedApi);

        } catch (Exception e) {
            log.error("Error updating API status: {}", e.getMessage());
            throw new RuntimeException("Failed to update API status: " + e.getMessage(), e);
        }
    }



    public ApiAnalyticsDTO getApiAnalytics(String requestId, String apiId,
                                           LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return metadataHelper.buildApiAnalytics(
                    executionLogRepository, apiId, startDate, endDate);
        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API analytics: " + e.getMessage());
            throw new RuntimeException("Failed to get API analytics: " + e.getMessage(), e);
        }
    }


    public Map<String, Object> validateSourceObject(ApiSourceObjectDTO sourceObject) {
        return validationHelper.validateSourceObject(
                oracleSchemaService, sourceObject, this::getSourceObjectDetails);
    }


    @Transactional
    public void syncGeneratedComponents(GeneratedApiEntity api, String performedBy) {
        try {
            log.info("Syncing generated components for API: {}", api.getId());

            GenerateApiRequestDTO request = conversionHelper.convertEntityToRequestDTO(api, objectMapper);
            CollectionInfoDTO collectionInfo = conversionHelper.extractCollectionInfo(api, objectMapper);

            updateCodeBase(api, performedBy, request, collectionInfo);
            updateCollections(api, performedBy, request, collectionInfo);
            updateDocumentation(api, performedBy, request, collectionInfo,
                    getCodeBaseRequestId(api), getCollectionsCollectionId(api));

            log.info("Successfully synced all components for API: {}", api.getId());

        } catch (Exception e) {
            log.error("Failed to sync components: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync components: " + e.getMessage(), e);
        }
    }


    public GeneratedApiEntity getApiEntity(String apiId) {
        return executionHelper.getApiEntity(generatedAPIRepository, apiId);
    }

    // ==================== DELEGATED METHODS ====================

    public Map<String, String> generateApiCode(GeneratedApiEntity api) {
        return componentHelper.generateApiCode(api, genUrlBuilder, this::generatePlSqlPackage,
                this::generateGenInfoFile, this::generateOpenApiSpec, this::generatePostmanCollection);
    }

    private String generatePlSqlPackage(GeneratedApiEntity api) {
        return componentHelper.generatePlSqlPackage(api);
    }

    private String generateGenInfoFile(GeneratedApiEntity api, GenUrlBuilderUtil.GenUrlInfo genUrlInfo) {
        return componentHelper.generateGenInfoFile(api, genUrlInfo);
    }

    private String generateOpenApiSpec(GeneratedApiEntity api) {
        return componentHelper.generateOpenApiSpec(api, objectMapper, typeMapper);
    }

    private String generatePostmanCollection(GeneratedApiEntity api) {
        return componentHelper.generatePostmanCollection(api, objectMapper);
    }

    private Object generateSampleResponse(GeneratedApiEntity api) {
        return responseHelper.generateSampleResponse(api);
    }

    private Map<String, Object> getSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        return metadataHelper.getSourceObjectDetails(oracleSchemaService, sourceObject);
    }

    private boolean shouldRegenerateComponents(GenerateApiRequestDTO request) {
        return request.getRegenerateComponents() != null && request.getRegenerateComponents();
    }

    private String getCodeBaseRequestId(GeneratedApiEntity api) {
        return metadataHelper.getCodeBaseRequestId(api);
    }

    private String getCollectionsCollectionId(GeneratedApiEntity api) {
        return metadataHelper.getCollectionsCollectionId(api);
    }

    private String getDocumentationCollectionId(GeneratedApiEntity api) {
        return metadataHelper.getDocumentationCollectionId(api);
    }

    private void updateCodeBase(GeneratedApiEntity api, String performedBy,
                                GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        componentHelper.updateCodeBase(api, performedBy, request, collectionInfo,
                codeBaseGeneratorUtil, codeBaseRequestRepository, codeBaseCollectionRepository,
                codeBaseFolderRepository, entityManager);
    }

    private void updateCollections(GeneratedApiEntity api, String performedBy,
                                   GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        componentHelper.updateCollections(api, performedBy, request, collectionInfo,
                collectionsGeneratorUtil, collectionsCollectionRepository, collectionsFolderRepository,
                collectionsRequestRepository, entityManager);
    }

    private void updateDocumentation(GeneratedApiEntity api, String performedBy,
                                     GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                     String codeBaseRequestId, String collectionsCollectionId) {
        componentHelper.updateDocumentation(api, performedBy, request, collectionInfo,
                codeBaseRequestId, collectionsCollectionId, documentationGeneratorUtil,
                docCollectionRepository, docFolderRepository, endpointRepository, entityManager);
    }

    private void regenerateComponents(GeneratedApiEntity api, String performedBy,
                                      GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        componentHelper.regenerateComponents(api, performedBy, request, collectionInfo,
                codeBaseGeneratorUtil, collectionsGeneratorUtil, documentationGeneratorUtil,
                this::generateApiCode);
    }

    private CollectionInfoDTO extractCollectionInfo(GeneratedApiEntity api) {
        return conversionHelper.extractCollectionInfo(api, objectMapper);
    }



    public boolean isApiCodeAvailable(String apiCode) {
        try {
            // Check if an API with this code already exists
            return !generatedAPIRepository.existsByApiCode(apiCode);
        } catch (Exception e) {
            log.error("Error checking API code availability: {}", e.getMessage());
            // Return false in case of error to be safe
            return false;
        }
    }


    // Add this method to AutomationEngineService
    private String buildEndpointPathFromRequest(GenerateApiRequestDTO request) {
        String baseEndpoint = request.getEndpointPath();
        if (baseEndpoint == null || baseEndpoint.isEmpty()) {
            baseEndpoint = "/api/v1/" + request.getApiCode().toLowerCase();
        }

        log.info("Building endpoint path from request. Base: {}", baseEndpoint);

        // Use the parameters DIRECTLY from the request
        List<ApiParameterDTO> pathParams = request.getParameters().stream()
                .filter(p -> "path".equalsIgnoreCase(p.getParameterLocation()))
                .sorted(Comparator.comparing(ApiParameterDTO::getPosition,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        log.info("Found {} path parameters from request: {}", pathParams.size(),
                pathParams.stream().map(ApiParameterDTO::getKey).collect(Collectors.joining(", ")));

        StringBuilder endpointBuilder = new StringBuilder(baseEndpoint);

        // Remove trailing slash if present
        if (endpointBuilder.length() > 0 && endpointBuilder.charAt(endpointBuilder.length() - 1) == '/') {
            endpointBuilder.setLength(endpointBuilder.length() - 1);
        }

        // Add each path parameter as a placeholder
        for (ApiParameterDTO param : pathParams) {
            endpointBuilder.append("/{").append(param.getKey()).append("}");
        }

        String fullEndpoint = endpointBuilder.toString();
        log.info("Final endpoint path with placeholders: {}", fullEndpoint);

        return fullEndpoint;
    }



    /**
     * Create consolidated parameters map that INCLUDES HEADERS for validation
     */
    private Map<String, Object> createConsolidatedParamsWithHeaders(ExecuteApiRequestDTO executeRequest) {
        Map<String, Object> allParams = new HashMap<>();

        // Add path parameters
        if (executeRequest.getPathParams() != null && !executeRequest.getPathParams().isEmpty()) {
            allParams.putAll(executeRequest.getPathParams());
            log.info("Added path params to consolidated map: {}", executeRequest.getPathParams().keySet());
        }

        // Add query parameters
        if (executeRequest.getQueryParams() != null && !executeRequest.getQueryParams().isEmpty()) {
            allParams.putAll(executeRequest.getQueryParams());
            log.info("Added query params to consolidated map: {}", executeRequest.getQueryParams().keySet());
        }

        // CRITICAL FIX: Add headers to the consolidated params map
        if (executeRequest.getHeaders() != null && !executeRequest.getHeaders().isEmpty()) {
            // Add headers as-is (original case)
            allParams.putAll(executeRequest.getHeaders());

            // Also add lowercase versions for case-insensitive matching
            executeRequest.getHeaders().forEach((key, value) -> {
                allParams.put(key.toLowerCase(), value);
            });

            log.info("Added headers to consolidated map: {}", executeRequest.getHeaders().keySet());

            // Specifically log if ac_no header is present
            if (executeRequest.getHeaders().containsKey("ac_no")) {
                log.info("Found ac_no header with value: {}", executeRequest.getHeaders().get("ac_no"));
            } else if (executeRequest.getHeaders().containsKey("AC_NO")) {
                log.info("Found AC_NO header with value: {}", executeRequest.getHeaders().get("AC_NO"));
            }
        } else {
            log.warn("No headers found in the request!");
        }

        // Add body parameters if it's a map
        if (executeRequest.getBody() != null) {
            if (executeRequest.getBody() instanceof Map) {
                allParams.putAll((Map<String, Object>) executeRequest.getBody());
                log.info("Added body params to consolidated map: {}", ((Map<?, ?>) executeRequest.getBody()).keySet());
            } else {
                // For non-map bodies, wrap in a special key
                allParams.put("_body", executeRequest.getBody());
                log.info("Added raw body to consolidated map");
            }
        }

        log.info("Final consolidated params keys: {}", allParams.keySet());

        // Check specifically for ac_no in any form
        boolean hasAcNo = false;
        for (String key : allParams.keySet()) {
            if (key.equalsIgnoreCase("ac_no")) {
                hasAcNo = true;
                log.info("Found ac_no (case-insensitive) with key: {} and value: {}", key, allParams.get(key));
                break;
            }
        }

        if (!hasAcNo) {
            log.warn("WARNING: ac_no parameter NOT found in consolidated params!");
        }

        return allParams;
    }

    /**
     * Enhanced validation that checks headers properly (without HttpServletRequest parameter)
     */
    private Map<String, String> validateRequiredParametersEnhanced(
            GeneratedApiEntity api,
            Map<String, Object> consolidatedParams,
            ExecuteApiRequestDTO executeRequest) {

        Map<String, String> errors = new HashMap<>();
        List<ApiParameterEntity> parameters = api.getParameters();

        log.info("=== Starting Enhanced Required Parameter Validation ===");
        log.info("Processing {} parameters", parameters.size());
        log.info("Consolidated params keys: {}", consolidatedParams.keySet());

        for (ApiParameterEntity param : parameters) {
            if (!param.getRequired()) {
                continue;
            }

            String paramKey = param.getKey();
            String location = param.getParameterLocation();

            log.info("Checking required parameter: {} (location: {})", paramKey, location);

            Object value = null;

            // Strategy 1: Check in consolidated params (case-sensitive)
            value = consolidatedParams.get(paramKey);

            // Strategy 2: Try case-insensitive match in consolidated params
            if (value == null) {
                for (Map.Entry<String, Object> entry : consolidatedParams.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(paramKey)) {
                        value = entry.getValue();
                        log.info("Found case-insensitive match: {} = {}", entry.getKey(), value);
                        break;
                    }
                }
            }

            // Strategy 3: For headers, check in the executeRequest headers directly
            if (value == null && "header".equalsIgnoreCase(location) &&
                    executeRequest != null && executeRequest.getHeaders() != null) {

                Map<String, String> headers = executeRequest.getHeaders();

                // Check exact match
                value = headers.get(paramKey);

                // Check case-insensitive
                if (value == null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(paramKey)) {
                            value = entry.getValue();
                            log.info("Found header in executeRequest headers: {} = {}", entry.getKey(), value);
                            break;
                        }
                    }
                }
            }

            // Check if value exists and is not empty
            if (value == null || value.toString().trim().isEmpty()) {
                log.error("Required parameter [{}] not found or empty", paramKey);
                errors.put(paramKey, "Required parameter '" + paramKey + "' is missing");
            } else {
                log.info("Required parameter [{}] found with value: {}", paramKey, value);
            }
        }

        log.info("=== Validation complete. Errors: {} ===\n", errors.size());
        return errors;
    }

}