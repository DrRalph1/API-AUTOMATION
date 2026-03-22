package com.usg.apiAutomation.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.collections.*;
import com.usg.apiAutomation.factories.ApiExecutionHelperFactory;
import com.usg.apiAutomation.factories.ApiMetadataHelperFactory;
import com.usg.apiAutomation.factories.DatabaseTypeServiceFactory;
import com.usg.apiAutomation.factories.ParameterGeneratorFactory;
import com.usg.apiAutomation.helpers.*;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiExecutionHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiValidationHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiValidationHelper;
import com.usg.apiAutomation.repositories.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.codeBase.*;
import com.usg.apiAutomation.repositories.codeBase.FolderRepository;
import com.usg.apiAutomation.repositories.collections.AuthConfigRepository;
import com.usg.apiAutomation.repositories.collections.HeaderRepository;
import com.usg.apiAutomation.repositories.collections.ParameterRepository;
import com.usg.apiAutomation.repositories.documentation.*;
import com.usg.apiAutomation.helpers.apiEngine.*;
import com.usg.apiAutomation.services.schemaBrowser.DatabaseSchemaService;
import com.usg.apiAutomation.services.schemaBrowser.OracleSchemaService;
import com.usg.apiAutomation.services.schemaBrowser.PostgreSQLSchemaService;
import com.usg.apiAutomation.utils.apiEngine.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.usg.apiAutomation.utils.apiEngine.executor.oracle.*;
import com.usg.apiAutomation.utils.apiEngine.executor.postgresql.PostgreSQLFunctionExecutorUtil;
import com.usg.apiAutomation.utils.apiEngine.executor.postgresql.PostgreSQLProcedureExecutorUtil;
import com.usg.apiAutomation.utils.apiEngine.executor.postgresql.PostgreSQLTableExecutorUtil;
import com.usg.apiAutomation.utils.apiEngine.executor.postgresql.PostgreSQLViewExecutorUtil;
import com.usg.apiAutomation.utils.apiEngine.generator.CodeBaseGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.generator.CollectionsGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.generator.DocumentationGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.apiAutomation.utils.apiEngine.OracleParameterGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.OracleParameterValidatorUtil;
import com.usg.apiAutomation.utils.apiEngine.OracleTypeMapperUtil;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final com.usg.apiAutomation.repositories.collections.CollectionRepository collectionsCollectionRepository;
    private final com.usg.apiAutomation.repositories.collections.FolderRepository collectionsFolderRepository;
    private final com.usg.apiAutomation.repositories.collections.RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;
    private final APICollectionRepository docCollectionRepository;
    private final com.usg.apiAutomation.repositories.documentation.FolderRepository docFolderRepository;
    private final APIEndpointRepository endpointRepository;
    private final com.usg.apiAutomation.repositories.documentation.HeaderRepository docHeaderRepository;
    private final com.usg.apiAutomation.repositories.documentation.ParameterRepository docParameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;

    // ==================== HELPERS (Business Logic) ====================
    private final OracleApiValidationHelper oracleValidationHelper;
    private final PostgreSQLApiValidationHelper postgresValidationHelper;
    private final ApiConversionHelper conversionHelper;
    private final ApiResponseHelper responseHelper;
    // REMOVE THIS: private final OracleApiExecutionHelper executionHelper;
    private final ApiComponentHelper componentHelper;
    // REMOVE THESE: private final OracleApiMetadataHelper oracleMetadataHelper;
    // REMOVE THESE: private final PostgreSQLApiMetadataHelper postgresMetadataHelper;

    // ==================== UTILS (Technical Utilities) ====================
    private final GenUrlBuilderUtil genUrlBuilder;
    private final OracleTypeMapperUtil typeMapper;
    private final OracleObjectResolverUtil objectResolver;
    private final OracleParameterValidatorUtil parameterValidator;
    private final AuthenticationServiceUtil authenticationService;

    // ==================== EXECUTORS ====================
    private final OracleTableExecutorUtil oracleTableExecutorUtil;
    private final OracleViewExecutorUtil oracleViewExecutorUtil;
    private final OracleProcedureExecutorUtil oracleProcedureExecutorUtil;
    private final OracleFunctionExecutorUtil oracleFunctionExecutorUtil;
    private final OraclePackageExecutorUtil oraclePackageExecutorUtil;
    private final PostgreSQLTableExecutorUtil postgreSQLTableExecutorUtil;
    private final PostgreSQLViewExecutorUtil postgreSQLViewExecutorUtil;
    private final PostgreSQLProcedureExecutorUtil postgreSQLProcedureExecutorUtil;
    private final PostgreSQLFunctionExecutorUtil postgreSQLFunctionExecutorUtil;

    // ==================== GENERATORS ====================
    private final CodeBaseGeneratorUtil codeBaseGeneratorUtil;
    private final CollectionsGeneratorUtil collectionsGeneratorUtil;
    private final DocumentationGeneratorUtil documentationGeneratorUtil;
    private final CodeLanguageGeneratorUtil codeLanguageGeneratorUtil;
    private final OracleParameterGeneratorUtil oracleParameterGeneratorUtil;
    private final PostgreSQLParameterGeneratorUtil postgreSQLParameterGeneratorUtil;

    // ==================== FACTORIES ====================
    private final ParameterGeneratorFactory parameterGeneratorFactory;
    private final DatabaseTypeServiceFactory databaseTypeFactory;
    private final ApiExecutionHelperFactory executionHelperFactory;
    private final ApiMetadataHelperFactory metadataHelperFactory;

    // ==================== API REQUEST LOGGING ====================
    private final ApiRequestService apiRequestService;

    // ==================== EXTERNAL DEPENDENCIES ====================
    private final ObjectMapper objectMapper;
    private final LoggerUtil loggerUtil;
    private final ApiValidatorHelper validatorService;
    private final OracleSchemaService oracleSchemaService;
    private final PostgreSQLSchemaService postgreSQLSchemaService;
    private final EntityManager entityManager;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    // ==================== PUBLIC API METHODS ====================

    @Transactional
    public GeneratedApiResponseDTO generateApi(String requestId, String performedBy, GenerateApiRequestDTO request) {
        long startTime = System.currentTimeMillis();
        try {
            String databaseType = request.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                if (request.getSourceObject() != null && request.getSourceObject().containsKey("databaseType")) {
                    databaseType = (String) request.getSourceObject().get("databaseType");
                }
            }
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Generating API: " + request.getApiName() +
                    " for database: " + databaseType + " by: " + performedBy);

            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);
            validationHelper.validateApiCodeUniqueness(generatedAPIRepository, request.getApiCode());

            Object schemaService = databaseTypeFactory.getSchemaService(databaseType);
            ApiSourceObjectDTO sourceObjectDTO = convertAndValidateSourceObjectForGeneration(request, schemaService, databaseType);
            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());
            String endpointPath = buildEndpointPathFromRequest(request);
            DatabaseParameterGeneratorUtil parameterGenerator = parameterGeneratorFactory.getGenerator(databaseType);

            // Use factory to get the correct execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            GeneratedApiEntity savedApi = executionHelper.createAndSaveApiEntity(
                    request, sourceObjectDTO, collectionInfo, endpointPath, performedBy, null,
                    generatedAPIRepository, objectMapper, parameterGenerator, conversionHelper, databaseType);

            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(savedApi);
            String codeBaseRequestId = codeBaseGeneratorUtil.generate(savedApi, performedBy, request, collectionInfo);

            Map<String, String> collectionResult = collectionsGeneratorUtil.generateWithDetails(
                    savedApi, performedBy, request, collectionInfo);
            String collectionId = collectionResult.get("collectionId");
            String collectionsRequestId = collectionResult.get("requestId");

            savedApi.setSourceRequestId(collectionsRequestId);
            generatedAPIRepository.save(savedApi);

            String docCollectionId = documentationGeneratorUtil.generate(savedApi, performedBy, request,
                    codeBaseRequestId, collectionId, collectionInfo);

            GeneratedApiResponseDTO response = responseHelper.buildGenerateApiResponse(
                    savedApi, genUrlInfo, codeBaseRequestId, collectionId, docCollectionId,
                    collectionInfo, genUrlBuilder, conversionHelper,
                    (api) -> generateApiCode(api));

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + savedApi.getId() +
                    ", Database: " + databaseType +
                    " in " + (System.currentTimeMillis() - startTime) + "ms");

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error generating API: " + e.getMessage());
            log.error("Error generating API", e);
            throw new RuntimeException("Failed to generate API: " + e.getMessage(), e);
        }
    }



    @Transactional
    public GeneratedApiResponseDTO updateApi(String requestId, String apiId, String performedBy,
                                             GenerateApiRequestDTO request) {
        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Updating API: " + apiId + " by: " + performedBy);

            // Get the existing API entity
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type from the existing API
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            log.info("Updating API: {} on database: {}", apiId, databaseType);

            // Get the appropriate execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            // Store the original sourceRequestId before clearing relationships
            String originalSourceRequestId = api.getSourceRequestId();

            // Get the appropriate validation helper
            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);

            // Check API code uniqueness if changed
            validationHelper.validateApiCodeUniquenessOnUpdate(
                    generatedAPIRepository, api.getApiCode(), request.getApiCode());

            // Get the appropriate schema service
            Object schemaService = databaseTypeFactory.getSchemaService(databaseType);

            // Convert and validate source object
            ApiSourceObjectDTO sourceObjectDTO = convertAndValidateSourceObjectForUpdate(request, schemaService, databaseType);

            // Validate collection info
            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());

            // Update API entity
            executionHelper.updateApiEntity(api, request, sourceObjectDTO, collectionInfo, performedBy);

            // Clear and recreate relationships
            executionHelper.clearApiRelationships(api);

            // Get the appropriate parameter generator
            DatabaseParameterGeneratorUtil parameterGenerator = parameterGeneratorFactory.getGenerator(databaseType);

            // Recreate relationships
            executionHelper.recreateApiRelationships(api, request, sourceObjectDTO,
                    parameterGenerator, conversionHelper);

            // Set the original sourceRequestId back
            api.setSourceRequestId(originalSourceRequestId);

            // Ensure database type is preserved
            api.setDatabaseType(databaseType);

            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);
            entityManager.flush();

            // Update components
            componentHelper.updateComponents(
                    savedApi, performedBy, request, collectionInfo,
                    shouldRegenerateComponents(request),
                    codeBaseGeneratorUtil, collectionsGeneratorUtil, documentationGeneratorUtil,
                    (apiEntity, user, req, collInfo) -> updateCodeBase(apiEntity, user, req, collInfo),
                    (apiEntity, user, req, collInfo, originalId) ->
                            updateCollections(apiEntity, user, req, collInfo, originalId),
                    (apiEntity, user, req, collInfo, codeBaseId, collectionId) ->
                            updateDocumentation(apiEntity, user, req, collInfo, codeBaseId, collectionId),
                    (apiEntity) -> getCodeBaseRequestId(apiEntity),
                    (apiEntity) -> getCollectionsCollectionId(apiEntity),
                    (apiEntity) -> generateApiCode(apiEntity)
            );

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", API updated successfully: " + savedApi.getId() +
                    " on database: " + databaseType);

            return conversionHelper.mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error updating API: " + e.getMessage());
            log.error("Error updating API on database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update API: " + e.getMessage(), e);
        }
    }



        // Helper method to convert and validate source object based on database type
    private ApiSourceObjectDTO convertAndValidateSourceObject(GenerateApiRequestDTO request, String databaseType) {
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            // For PostgreSQL, use PostgreSQL-specific conversion if needed
            // Your existing conversion logic, but with PostgreSQL in mind
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
            // Default to Oracle
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), oracleSchemaService, objectMapper);
        }
    }


    /**
     * Helper method to convert and validate source object for generation
     */
    private ApiSourceObjectDTO convertAndValidateSourceObjectForGeneration(GenerateApiRequestDTO request,
                                                                           Object schemaService,
                                                                           String databaseType) {
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            // For PostgreSQL, use PostgreSQL-specific conversion
            // Pass null for OracleSchemaService as it's not needed for PostgreSQL
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
            // Default to Oracle
            if (!(schemaService instanceof OracleSchemaService)) {
                throw new IllegalArgumentException("Expected OracleSchemaService for Oracle database type");
            }
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), (OracleSchemaService) schemaService, objectMapper);
        }
    }




    /**
     * Helper method to convert and validate source object for update based on database type
     */
    private ApiSourceObjectDTO convertAndValidateSourceObjectForUpdate(GenerateApiRequestDTO request,
                                                                       Object schemaService,
                                                                       String databaseType) {
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            // For PostgreSQL, use PostgreSQL-specific conversion
            // You might need to inject a PostgreSQL-specific conversion helper
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
            // Default to Oracle
            if (!(schemaService instanceof OracleSchemaService)) {
                throw new IllegalArgumentException("Expected OracleSchemaService for Oracle database type");
            }
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), (OracleSchemaService) schemaService, objectMapper);
        }
    }

    /**
     * Get the appropriate parameter generator based on database type
     */
    private DatabaseParameterGeneratorUtil getParameterGenerator(String databaseType) {
        return parameterGeneratorFactory.getGenerator(databaseType);
    }


    // Add this overloaded updateCollections method that accepts the original sourceRequestId
    private void updateCollections(GeneratedApiEntity api, String performedBy,
                                   GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                   String originalSourceRequestId) {
        try {
            log.info("Updating collections for API: {} with original request ID: {}",
                    api.getId(), originalSourceRequestId);

            // If we have an original sourceRequestId, try to update the existing request
            if (originalSourceRequestId != null && !originalSourceRequestId.isEmpty()) {
                log.info("Attempting to update existing collections request with ID: {}", originalSourceRequestId);

                // Check if the request still exists
                Optional<com.usg.apiAutomation.entities.postgres.collections.RequestEntity> existingRequestOpt =
                        collectionsRequestRepository.findById(originalSourceRequestId);

                if (existingRequestOpt.isPresent()) {
                    var existingRequest = existingRequestOpt.get();

                    // CRITICAL FIX: Check if collection or folder has changed
                    String currentCollectionId = existingRequest.getCollection() != null ?
                            existingRequest.getCollection().getId() : null;
                    String currentFolderId = existingRequest.getFolder() != null ?
                            existingRequest.getFolder().getId() : null;

                    String newCollectionId = collectionInfo != null ? collectionInfo.getCollectionId() : null;
                    String newFolderId = collectionInfo != null ? collectionInfo.getFolderId() : null;

                    boolean collectionChanged = (newCollectionId != null && !newCollectionId.equals(currentCollectionId));
                    boolean folderChanged = (newFolderId != null && !newFolderId.equals(currentFolderId));

                    log.info("Collection changed: {} -> {}, Folder changed: {} -> {}",
                            currentCollectionId, newCollectionId, currentFolderId, newFolderId);

                    if (collectionChanged || folderChanged) {
                        log.info("Collection or folder changed, need to recreate the request with new parent structure");

                        // FIRST: Completely clear all relationships from the existing request
                        // This is critical to avoid constraint violations
                        clearAllRequestRelationships(existingRequest);

                        // Get or create the new collection
                        CollectionEntity newCollection;
                        if (newCollectionId != null) {
                            newCollection = collectionsCollectionRepository.findById(newCollectionId)
                                    .orElseGet(() -> {
                                        // Create new collection if it doesn't exist
                                        CollectionEntity newColl = new CollectionEntity();
                                        newColl.setId(newCollectionId);
                                        newColl.setGeneratedApiId(api.getId());
                                        newColl.setName(collectionInfo.getCollectionName());
                                        newColl.setDescription("Collection for " + collectionInfo.getCollectionName());
                                        newColl.setOwner(performedBy);
                                        newColl.setExpanded(false);
                                        newColl.setEditing(false);
                                        newColl.setFavorite(false);
                                        newColl.setLastActivity(LocalDateTime.now());
                                        newColl.setColor(getRandomColor());
                                        newColl.setCreatedAt(LocalDateTime.now());
                                        newColl.setUpdatedAt(LocalDateTime.now());
                                        return collectionsCollectionRepository.save(newColl);
                                    });
                        } else {
                            newCollection = existingRequest.getCollection();
                        }

                        // Get or create the new folder
                        FolderEntity newFolder;
                        if (newFolderId != null) {
                            newFolder = collectionsFolderRepository.findById(newFolderId)
                                    .orElseGet(() -> {
                                        // Create new folder if it doesn't exist
                                        FolderEntity newFldr = new FolderEntity();
                                        newFldr.setId(newFolderId);
                                        newFldr.setGeneratedApiId(api.getId());
                                        newFldr.setName(collectionInfo.getFolderName());
                                        newFldr.setDescription("Folder for " + collectionInfo.getFolderName());
                                        newFldr.setExpanded(false);
                                        newFldr.setEditing(false);
                                        newFldr.setRequestCount(0);
                                        newFldr.setCollection(newCollection);
                                        newFldr.setCreatedAt(LocalDateTime.now());
                                        newFldr.setUpdatedAt(LocalDateTime.now());
                                        return collectionsFolderRepository.save(newFldr);
                                    });
                        } else {
                            newFolder = existingRequest.getFolder();
                        }

                        // Update the request with new collection and folder
                        existingRequest.setCollection(newCollection);
                        existingRequest.setFolder(newFolder);

                        // Update the request content
                        updateRequestContent(existingRequest, api, performedBy, request);

                        // Save the updated request
                        collectionsRequestRepository.save(existingRequest);

                        // Update folder request counts
                        updateFolderRequestCounts(currentFolderId, newFolderId);

                        log.info("Successfully updated request with new collection/folder structure");
                        return;
                    }

                    // If collection/folder hasn't changed, just update the content
                    // First clear relationships to avoid duplicates
                    clearAllRequestRelationships(existingRequest);
                    updateRequestContent(existingRequest, api, performedBy, request);
                    log.info("Successfully updated existing collections request: {}", originalSourceRequestId);
                    return;
                } else {
                    log.warn("Existing collections request with ID {} not found. Will generate new one.",
                            originalSourceRequestId);
                }
            }

            // If no existing request or it was deleted, generate new ones
            log.info("No existing collections request found, generating new ones for API: {}", api.getId());
            Map<String, String> collectionResult = collectionsGeneratorUtil.generateWithDetails(
                    api, performedBy, request, collectionInfo);

            String collectionsRequestId = collectionResult.get("requestId");

            // CRITICAL: Update the API with the new collections request ID
            if (collectionsRequestId != null) {
                api.setSourceRequestId(collectionsRequestId);
                generatedAPIRepository.save(api);
                log.info("Generated new collections request with ID: {} and updated API", collectionsRequestId);
            } else {
                log.error("Failed to generate new collections request - requestId is null");
            }

        } catch (Exception e) {
            log.error("Error updating collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update collections: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to completely clear all relationships from a request
     * This is critical to avoid duplicate key constraint violations
     */
    private void clearAllRequestRelationships(com.usg.apiAutomation.entities.postgres.collections.RequestEntity request) {
        try {
            log.debug("Clearing all relationships for request: {}", request.getId());

            // Clear headers
            if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                List<HeaderEntity> headersToRemove = new ArrayList<>(request.getHeaders());
                request.getHeaders().clear();
                if (!headersToRemove.isEmpty()) {
                    collectionsHeaderRepository.deleteAll(headersToRemove);
                    log.debug("Deleted {} headers for request: {}", headersToRemove.size(), request.getId());
                }
            }

            // Clear parameters
            if (request.getParams() != null && !request.getParams().isEmpty()) {
                List<ParameterEntity> paramsToRemove = new ArrayList<>(request.getParams());
                request.getParams().clear();
                if (!paramsToRemove.isEmpty()) {
                    collectionsParameterRepository.deleteAll(paramsToRemove);
                    log.debug("Deleted {} parameters for request: {}", paramsToRemove.size(), request.getId());
                }
            }

            // CRITICAL: Clear auth config properly
            if (request.getAuthConfig() != null) {
                AuthConfigEntity authConfig = request.getAuthConfig();
                log.debug("Deleting existing auth config with ID: {}", authConfig.getId());
                request.setAuthConfig(null);
                collectionsAuthConfigRepository.delete(authConfig);
                log.debug("Deleted auth config for request: {}", request.getId());
            }

            // Flush to ensure all deletions are processed immediately
            collectionsHeaderRepository.flush();
            collectionsParameterRepository.flush();
            collectionsAuthConfigRepository.flush();

            log.debug("Successfully cleared all relationships for request: {}", request.getId());

        } catch (Exception e) {
            log.error("Error clearing relationships for request {}: {}", request.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to clear request relationships", e);
        }
    }

    /**
     * Helper method to update request content (without parent structure changes)
     */
    private void updateRequestContent(com.usg.apiAutomation.entities.postgres.collections.RequestEntity request,
                                      GeneratedApiEntity api, String performedBy,
                                      GenerateApiRequestDTO generateRequest) {
        try {
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

            // Update basic info
            request.setName(api.getApiName() + " - " + api.getHttpMethod());
            request.setMethod(api.getHttpMethod());
            request.setUrl(genUrlInfo.getFullUrl());
            request.setDescription(api.getDescription());
            request.setLastModified(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());

            // Set request body
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                request.setBody(api.getRequestConfig().getSample());
            }

            // Set auth type
            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                request.setAuthType(api.getAuthConfig().getAuthType().toLowerCase());
            }

            // Save the request first
            request = collectionsRequestRepository.save(request);

            // Create and add new headers
            List<HeaderEntity> newHeaders = createHeadersForRequest(api, request, api.getId());
            for (HeaderEntity header : newHeaders) {
                request.addHeader(header);
            }
            if (!newHeaders.isEmpty()) {
                collectionsHeaderRepository.saveAll(newHeaders);
            }

            // Create and add new parameters
            List<ParameterEntity> newParams = createParametersForRequest(api, request, api.getId());
            for (ParameterEntity param : newParams) {
                request.addParameter(param);
            }
            if (!newParams.isEmpty()) {
                collectionsParameterRepository.saveAll(newParams);
            }

            // Create new auth config (old one was already deleted in clearAllRequestRelationships)
            createAuthConfig(api, request, api.getId());

            // Final save
            collectionsRequestRepository.save(request);

            log.info("Successfully updated request content for: {}", request.getId());

        } catch (Exception e) {
            log.error("Error updating request content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update request content", e);
        }
    }

    // Helper method to update folder request counts
    public void updateFolderRequestCounts(String oldFolderId, String newFolderId) {
        try {
            // Decrement count for old folder if it exists and is different
            if (oldFolderId != null && !oldFolderId.equals(newFolderId)) {
                collectionsFolderRepository.findById(oldFolderId).ifPresent(oldFolder -> {
                    int currentCount = Optional.ofNullable(oldFolder.getRequestCount()).orElse(0);
                    int newCount = Math.max(0, currentCount - 1);
                    oldFolder.setRequestCount(newCount);
                    collectionsFolderRepository.save(oldFolder);
                    log.debug("Decremented request count for old folder: {} from {} to {}",
                            oldFolderId, currentCount, newCount);
                });
            }

            // Increment count for new folder if it exists and is different
            if (newFolderId != null && !newFolderId.equals(oldFolderId)) {
                collectionsFolderRepository.findById(newFolderId).ifPresent(newFolder -> {
                    int currentCount = Optional.ofNullable(newFolder.getRequestCount()).orElse(0);
                    int newCount = currentCount + 1;
                    newFolder.setRequestCount(newCount);
                    collectionsFolderRepository.save(newFolder);
                    log.debug("Incremented request count for new folder: {} from {} to {}",
                            newFolderId, currentCount, newCount);
                });
            }
        } catch (Exception e) {
            log.warn("Failed to update folder request counts: {}", e.getMessage());
        }
    }

    // Helper method to create headers for a request
    private List<HeaderEntity> createHeadersForRequest(GeneratedApiEntity api,
                                                       com.usg.apiAutomation.entities.postgres.collections.RequestEntity request,
                                                       String generatedApiId) {
        List<HeaderEntity> newHeaders = new ArrayList<>();

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                    HeaderEntity header = new HeaderEntity();
                    header.setId(UUID.randomUUID().toString());
                    header.setGeneratedApiId(generatedApiId);
                    header.setKey(apiHeader.getKey());
                    header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                    header.setDescription(apiHeader.getDescription());
                    header.setEnabled(apiHeader.getRequired() != null ? apiHeader.getRequired() : true);
                    header.setRequest(request);
                    newHeaders.add(header);
                }
            }
        }

        if (api.getParameters() != null) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                if ("header".equals(apiParam.getParameterType())) {
                    HeaderEntity header = new HeaderEntity();
                    header.setId(UUID.randomUUID().toString());
                    header.setGeneratedApiId(generatedApiId);
                    header.setKey(apiParam.getKey());
                    header.setValue(apiParam.getExample() != null ? apiParam.getExample() : "");
                    header.setDescription(apiParam.getDescription());
                    header.setEnabled(apiParam.getRequired() != null ? apiParam.getRequired() : true);
                    header.setRequest(request);
                    newHeaders.add(header);
                }
            }
        }

        return newHeaders;
    }

    // Helper method to create parameters for a request
    private List<ParameterEntity> createParametersForRequest(GeneratedApiEntity api,
                                                             com.usg.apiAutomation.entities.postgres.collections.RequestEntity request,
                                                             String generatedApiId) {
        List<ParameterEntity> newParams = new ArrayList<>();

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                ParameterEntity param = new ParameterEntity();
                param.setId(UUID.randomUUID().toString());
                param.setGeneratedApiId(generatedApiId);
                param.setKey(apiParam.getKey());
                param.setValue(apiParam.getExample());
                param.setDescription(apiParam.getDescription());
                param.setEnabled(apiParam.getRequired() != null ? apiParam.getRequired() : true);
                param.setDbColumn(apiParam.getDbColumn());
                param.setDbParameter(apiParam.getDbParameter());
                param.setParameterType(apiParam.getParameterType());
                param.setOracleType(apiParam.getOracleType());
                param.setApiType(apiParam.getApiType());
                param.setParameterLocation(apiParam.getParameterLocation());
                param.setRequired(apiParam.getRequired());
                param.setValidationPattern(apiParam.getValidationPattern());
                param.setDefaultValue(apiParam.getDefaultValue());
                param.setInBody(apiParam.getInBody());
                param.setIsPrimaryKey(apiParam.getIsPrimaryKey());
                param.setParamMode(apiParam.getParamMode() != null ? apiParam.getParamMode() : "IN");
                param.setPosition(apiParam.getPosition() != null ? apiParam.getPosition() : 0);
                param.setRequest(request);
                newParams.add(param);
            }
        }

        return newParams;
    }

    // Helper method to create auth config
    private void createAuthConfig(GeneratedApiEntity api,
                                  com.usg.apiAutomation.entities.postgres.collections.RequestEntity request,
                                  String generatedApiId) {
        if (api.getAuthConfig() == null || "NONE".equals(api.getAuthConfig().getAuthType())) {
            return;
        }

        AuthConfigEntity authConfig = new AuthConfigEntity();
        authConfig.setId(UUID.randomUUID().toString());
        authConfig.setGeneratedApiId(generatedApiId);
        authConfig.setRequest(request);
        authConfig.setType(api.getAuthConfig().getAuthType());

        switch (api.getAuthConfig().getAuthType()) {
            case "API_KEY":
                authConfig.setKey(api.getAuthConfig().getApiKeyHeader() != null ?
                        api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                authConfig.setValue("{{apiKey}}");
                authConfig.setAddTo("header");
                break;
            case "BEARER":
            case "JWT":
                authConfig.setType("bearer");
                authConfig.setToken("{{jwtToken}}");
                authConfig.setAddTo("header");
                break;
            case "BASIC":
                authConfig.setUsername("{{username}}");
                authConfig.setPassword("{{password}}");
                authConfig.setAddTo("header");
                break;
            case "ORACLE_ROLES":
                authConfig.setKey("X-Oracle-Session");
                authConfig.setValue("{{oracleSessionId}}");
                authConfig.setAddTo("header");
                break;
        }

        collectionsAuthConfigRepository.save(authConfig);
        request.setAuthConfig(authConfig);
    }

    // Helper method to get random color
    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }


    // Keep the original updateDocumentation method as is
    private void updateDocumentation(GeneratedApiEntity api, String performedBy,
                                     GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                     String codeBaseRequestId, String collectionsCollectionId) {
        // Your existing implementation
        componentHelper.updateDocumentation(api, performedBy, request, collectionInfo,
                codeBaseRequestId, collectionsCollectionId, documentationGeneratorUtil,
                docCollectionRepository, docFolderRepository, endpointRepository, entityManager);
    }


    /**
     * Execute API based on the database type stored in the API entity
     * Supports multiple database types: Oracle, PostgreSQL, etc.
     */
    // @Transactional
    public ExecuteApiResponseDTO executeApi(String requestId, String performedBy,
                                            String apiId, ExecuteApiRequestDTO executeRequest,
                                            String clientIp, String userAgent,
                                            HttpServletRequest httpServletRequest) {

        long startTime = System.currentTimeMillis();
        String capturedRequestId = null;
        ApiRequestResponseDTO capturedRequest = null;
        String databaseType = "oracle"; // Declare outside try block with default value

        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Executing API: " + apiId + " by: " + performedBy);

            // 1. Get the API entity
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // 2. Get the database type from the API entity
            databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            log.info("Executing API: {} on database: {}", apiId, databaseType);

            // 3. Get the appropriate execution helper based on database type
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            // 4. Check API status
            String apiStatus = api.getStatus() != null ? api.getStatus().toUpperCase() : "UNKNOWN";

            if (!"ACTIVE".equals(apiStatus)) {
                String errorMsg;
                int statusCode;

                switch (apiStatus) {
                    case "ARCHIVED":
                        errorMsg = "This API has been archived and cannot be executed. Please contact system administrator.";
                        statusCode = 410;
                        break;
                    case "DEPRECATED":
                        errorMsg = "This API is deprecated and no longer available for execution. Please contact system administrator for migration assistance.";
                        statusCode = 410;
                        break;
                    case "DRAFT":
                        errorMsg = "This API is in draft mode and has not been published yet. Please contact system administrator to publish the API.";
                        statusCode = 403;
                        break;
                    case "INACTIVE":
                        errorMsg = "This API is inactive and cannot be executed. Please contact system administrator to activate it.";
                        statusCode = 403;
                        break;
                    case "PENDING":
                        errorMsg = "This API is pending approval and cannot be executed yet. Please contact system administrator.";
                        statusCode = 403;
                        break;
                    case "SUSPENDED":
                        errorMsg = "This API has been suspended and cannot be executed. Please contact system administrator.";
                        statusCode = 403;
                        break;
                    default:
                        errorMsg = String.format("API is in '%s' state and cannot be executed. Please contact system administrator.",
                                api.getStatus());
                        statusCode = 403;
                        break;
                }

                log.warn("API execution blocked - Status: {}, API ID: {}, Database: {}", apiStatus, apiId, databaseType);

                if (capturedRequestId != null) {
                    try {
                        updateCapturedRequestWithError(requestId, capturedRequestId, statusCode,
                                errorMsg, System.currentTimeMillis() - startTime);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, executeRequest,
                        null, statusCode, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(statusCode, errorMsg, startTime);
            }

            // 5. Check isActive flag
            if (!api.getIsActive()) {
                String errorMsg = "API is inactive and cannot be executed. Please contact system administrator to activate it.";
                log.warn("API execution blocked - isActive flag is false for API ID: {}", apiId);

                if (capturedRequestId != null) {
                    try {
                        updateCapturedRequestWithError(requestId, capturedRequestId, 403,
                                errorMsg, System.currentTimeMillis() - startTime);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, executeRequest,
                        null, 403, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(403, errorMsg, startTime);
            }

            // 6. Validate request structure
            if (executeRequest == null) {
                executeRequest = new ExecuteApiRequestDTO();
                executeRequest.setRequestId(UUID.randomUUID().toString());
            }

            // 7. Extract HTTP method
            String httpMethod = null;
            if (httpServletRequest != null) {
                httpMethod = httpServletRequest.getMethod();
                log.info("HTTP method from HttpServletRequest: {}", httpMethod);
                executeRequest.setHttpMethod(httpMethod);
            }

            // 8. Prepare and validate the request
            ExecuteApiRequestDTO validatedRequest = executionHelper.prepareValidatedRequest(api, executeRequest);

            if (validatedRequest.getHttpMethod() == null && httpMethod != null) {
                validatedRequest.setHttpMethod(httpMethod);
                log.info("Re-set HTTP method in validatedRequest: {}", httpMethod);
            }

            // 9. Capture request before execution
            try {
                ApiRequestDTO requestDTO = convertExecuteRequestToApiRequestDTO(validatedRequest, api);
                requestDTO.setClientIpAddress(clientIp);
                requestDTO.setUserAgent(userAgent);
                requestDTO.setRequestedBy(performedBy);
                requestDTO.setCorrelationId(executeRequest.getRequestId());

                capturedRequest = apiRequestService.captureRequest(
                        requestId, apiId, requestDTO, performedBy, httpServletRequest);
                capturedRequestId = capturedRequest.getId();
                log.info("Request captured successfully with ID: {}", capturedRequestId);
            } catch (Exception e) {
                log.error("Failed to capture request: {}", e.getMessage());
            }

            // 10. Validate HTTP method
            if (!validateHttpMethod(api, validatedRequest.getHttpMethod())) {
                String errorMsg = String.format("HTTP method not allowed. Expected: %s, Actual: %s",
                        api.getHttpMethod(), validatedRequest.getHttpMethod());

                if (capturedRequestId != null) {
                    try {
                        updateCapturedRequestWithError(requestId, capturedRequestId, 405, errorMsg,
                                System.currentTimeMillis() - startTime);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 405, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(405, errorMsg, startTime);
            }

            // 11. Validate authentication
            try {
                AuthenticationServiceUtil.AuthenticationResult authResult =
                        authenticationService.validateAuthentication(api, validatedRequest);

                if (!authResult.isAuthenticated()) {
                    String errorMsg = "Authentication failed: " + authResult.getReason();

                    if (capturedRequestId != null) {
                        try {
                            updateCapturedRequestWithError(requestId, capturedRequestId, 401,
                                    errorMsg, System.currentTimeMillis() - startTime);
                        } catch (Exception ex) {
                            log.error("Failed to update captured request with error: {}", ex.getMessage());
                        }
                    }

                    executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                            null, 401, System.currentTimeMillis() - startTime,
                            performedBy, clientIp, userAgent, errorMsg, objectMapper);

                    return responseHelper.createErrorResponse(401, errorMsg, startTime);
                }
            } catch (Exception e) {
                log.error("Authentication validation error: {}", e.getMessage(), e);
                throw e;
            }

            // 12. Get all API parameters
            List<ApiParameterEntity> apiParameters = api.getParameters();
            log.info("API parameter definitions for database: {}", databaseType);
            apiParameters.forEach(p ->
                    log.info("  - {}: type={}, location={}, required={}",
                            p.getKey(), p.getParameterType(), p.getParameterLocation(), p.getRequired()));

            // 13. Create consolidated params
            Map<String, Object> consolidatedParams = createConsolidatedParamsWithHeaders(validatedRequest);

            // 14. Parse XML body if present
            if (validatedRequest.getBody() instanceof String) {
                String rawBody = (String) validatedRequest.getBody();
                if (rawBody.trim().startsWith("<")) {
                    log.info("XML body detected, parsing to extract parameters");
                    Map<String, Object> xmlParams = parseXmlParameters(rawBody, api.getParameters());

                    if (validatedRequest.getBody() == null) {
                        validatedRequest.setBody(new HashMap<>());
                    }

                    if (validatedRequest.getBody() instanceof Map) {
                        ((Map<String, Object>) validatedRequest.getBody()).putAll(xmlParams);
                        log.info("Added {} extracted XML parameters to request body", xmlParams.size());
                    } else {
                        Map<String, Object> newBody = new HashMap<>();
                        newBody.putAll(xmlParams);
                        validatedRequest.setBody(newBody);
                        log.info("Replaced body with extracted XML parameters: {}", xmlParams.keySet());
                    }

                    consolidatedParams.putAll(xmlParams);
                }
            }

            // 15. Auto-add Content-Type header if missing
            boolean contentTypeRequired = apiParameters.stream()
                    .anyMatch(p -> "Content-Type".equalsIgnoreCase(p.getKey()) && p.getRequired());

            boolean contentTypeMissing = !consolidatedParams.containsKey("Content-Type") &&
                    !consolidatedParams.containsKey("content-type");

            if (contentTypeRequired && contentTypeMissing) {
                String contentTypeValue = "application/json";
                consolidatedParams.put("Content-Type", contentTypeValue);
                if (validatedRequest.getHeaders() == null) {
                    validatedRequest.setHeaders(new HashMap<>());
                }
                validatedRequest.getHeaders().put("Content-Type", contentTypeValue);
            }

            // 16. Validate required parameters
            Map<String, String> validationErrors = validateRequiredParametersEnhanced(api, consolidatedParams, validatedRequest);

            if (!validationErrors.isEmpty()) {
                String missingParams = String.join(", ", validationErrors.keySet());
                String errorMsg = "Required parameter(s) missing: " + missingParams;

                if (capturedRequestId != null) {
                    try {
                        updateCapturedRequestWithError(requestId, capturedRequestId, 400,
                                errorMsg, System.currentTimeMillis() - startTime);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 400, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(400, errorMsg, startTime);
            }

            // 17. Authorization check
            if (!validatorService.validateAuthorization(api, performedBy)) {
                String errorMsg = "User not authorized to access this API";

                if (capturedRequestId != null) {
                    try {
                        updateCapturedRequestWithError(requestId, capturedRequestId, 403,
                                errorMsg, System.currentTimeMillis() - startTime);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 403, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(403, errorMsg, startTime);
            }

            // 18. Rate limiting check
            if (!validatorService.checkRateLimit(api, clientIp)) {
                String errorMsg = "Rate limit exceeded";

                if (capturedRequestId != null) {
                    try {
                        updateCapturedRequestWithError(requestId, capturedRequestId, 429,
                                errorMsg, System.currentTimeMillis() - startTime);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 429, System.currentTimeMillis() - startTime,
                        performedBy, clientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(429,
                        "Rate limit exceeded. Please try again later.", startTime);
            }

            // 19. Extract source object from API
            ApiSourceObjectDTO sourceObject = conversionHelper.extractSourceObject(api, objectMapper);

            // 20. Convert parameters to DTOs for execution
            List<ApiParameterDTO> configuredParamDTOs = conversionHelper.convertParametersToDTOs(api.getParameters());

            // 21. Execute against the appropriate database
            Object result;
            long executionTime;

            try {
                log.info("Executing against database: {}", databaseType);

                // Use the execution helper to execute against the database
                result = executionHelper.executeAgainstDatabase(api, sourceObject, validatedRequest, configuredParamDTOs);

                executionTime = System.currentTimeMillis() - startTime;

                // 22. Format the response
                Object formattedResponse = responseHelper.formatResponse(api, result);

                // 23. Update captured request with success response
                if (capturedRequestId != null) {
                    try {
                        ExecuteApiResponseDTO successResponse = new ExecuteApiResponseDTO();
                        successResponse.setResponseCode(200);
                        successResponse.setSuccess(true);
                        successResponse.setMessage("Success");
                        successResponse.setData(formattedResponse);

                        apiRequestService.updateRequestWithResponse(
                                requestId, capturedRequestId, successResponse, 200, "Success", executionTime);
                    } catch (Exception e) {
                        log.error("Failed to update captured request: {}", e.getMessage());
                    }
                }

                // 24. Update API statistics
                executionHelper.updateApiStats(api, generatedAPIRepository);

                // 25. Log the successful execution
                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        formattedResponse, 200, executionTime, performedBy,
                        clientIp, userAgent, null, objectMapper);

                // 26. Build success response
                ExecuteApiResponseDTO response = responseHelper.buildSuccessResponse(
                        formattedResponse, executionTime, api);

                loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                        ", API executed successfully: " + apiId +
                        " on database: " + databaseType +
                        " - Time: " + executionTime + "ms");

                return response;

            } catch (Exception e) {
                executionTime = System.currentTimeMillis() - startTime;
                log.error("Database execution failed for {}: ", databaseType, e);

                // Extract the actual database error
                String detailedError = extractDatabaseError(e, databaseType);

                // Log the raw error for debugging
                log.info("Returning raw database error: {}", detailedError);

                // Create error response - DO NOT RE-THROW, return this directly
                ExecuteApiResponseDTO errorResponse = new ExecuteApiResponseDTO();
                errorResponse.setResponseCode(500);
                errorResponse.setSuccess(false);
                errorResponse.setMessage(detailedError);

                Map<String, Object> errorData = new HashMap<>();
                errorData.put("error", detailedError);
                errorData.put("timestamp", LocalDateTime.now().toString());
                errorData.put("executionTimeMs", executionTime);
                errorResponse.setData(Collections.singletonList(errorData));

                // Update captured request with the error
                if (capturedRequestId != null) {
                    try {
                        String dbErrorMessage = truncateErrorMessage(detailedError, 250);
                        apiRequestService.updateRequestWithError(
                                requestId, capturedRequestId, 500, dbErrorMessage, executionTime);
                    } catch (Exception updateError) {
                        log.error("Failed to update captured request: {}", updateError.getMessage());
                    }
                }

                // Log the error
                try {
                    String logErrorMessage = truncateErrorMessage(detailedError, 1000);
                    executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                            null, 500, executionTime, performedBy, clientIp, userAgent,
                            logErrorMessage, objectMapper);
                } catch (Exception logError) {
                    log.error("Failed to log execution error: {}", logError.getMessage());
                }

                // Return the error response directly
                return errorResponse;
            }

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error executing API: " + e.getMessage());
            log.error("Error executing API: ", e);

            // Extract the actual database error if it's a data integrity violation
            String detailedError = extractDatabaseError(e, databaseType);

            ExecuteApiResponseDTO safeResponse = new ExecuteApiResponseDTO();
            safeResponse.setResponseCode(500);
            safeResponse.setSuccess(false);
            safeResponse.setMessage(detailedError);

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", detailedError);
            errorData.put("timestamp", LocalDateTime.now().toString());
            errorData.put("executionTimeMs", executionTime);
            safeResponse.setData(Collections.singletonList(errorData));

            if (capturedRequestId != null) {
                try {
                    String dbErrorMessage = truncateErrorMessage(detailedError, 250);
                    apiRequestService.updateRequestWithError(
                            requestId, capturedRequestId, 500, dbErrorMessage, executionTime);
                } catch (Exception updateError) {
                    log.error("Failed to update captured request: {}", updateError.getMessage());
                }
            }

            try {
                GeneratedApiEntity api = null;
                try {
                    api = generatedAPIRepository.findById(apiId).orElse(null);
                } catch (Exception ex) {
                    // Ignore
                }

                String logErrorMessage = truncateErrorMessage(detailedError, 1000);
                BaseApiExecutionHelper defaultHelper = executionHelperFactory.getExecutionHelper("oracle");
                defaultHelper.logExecution(executionLogRepository, api, executeRequest,
                        null, 500, executionTime, performedBy, clientIp, userAgent,
                        logErrorMessage, objectMapper);
            } catch (Exception logError) {
                log.error("Failed to log execution error: {}", logError.getMessage());
            }

            return safeResponse;
        }
    }


    /**
     * Execute PostgreSQL table operation
     */
    private Object executePostgreSQLTable(String tableName, String schema, String operation,
                                          Map<String, Object> params, GeneratedApiEntity api,
                                          List<ApiParameterDTO> configuredParamDTOs) {

        switch (operation.toUpperCase()) {
            case "SELECT":
                return postgreSQLTableExecutorUtil.executeSelect(tableName, schema, params, api, configuredParamDTOs);
            case "INSERT":
                return postgreSQLTableExecutorUtil.executeInsert(tableName, schema, params, api, configuredParamDTOs);
            case "UPDATE":
                return postgreSQLTableExecutorUtil.executeUpdate(tableName, schema, params, api, configuredParamDTOs);
            case "DELETE":
                return postgreSQLTableExecutorUtil.executeDelete(tableName, schema, params, api, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported table operation: " + operation);
        }
    }

    /**
     * Extracts the actual database error without modification
     * This returns the raw error message that PostgreSQL or Oracle returns
     */
    private String extractDatabaseError(Exception e, String databaseType) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null) {
                // PostgreSQL-specific errors - return the raw error
                if (databaseType.equalsIgnoreCase("postgresql") || databaseType.equalsIgnoreCase("postgres")) {
                    if (message.contains("ERROR:")) {
                        // Return the complete PostgreSQL error line
                        Pattern pattern = Pattern.compile("ERROR:[^\\n]*");
                        Matcher matcher = pattern.matcher(message);
                        if (matcher.find()) {
                            String rawError = matcher.group();
                            log.info("Returning raw PostgreSQL error: {}", rawError);
                            return rawError;
                        }
                        return message;
                    }
                    // Handle PostgreSQL specific exceptions by checking class name
                    if (cause.getClass().getName().contains("PSQLException")) {
                        return cause.getMessage();
                    }
                }

                // Oracle-specific errors - return raw ORA error
                if (databaseType.equalsIgnoreCase("oracle") && message.contains("ORA-")) {
                    Pattern pattern = Pattern.compile("ORA-[0-9]{5}:[^\\n]*");
                    Matcher matcher = pattern.matcher(message);
                    if (matcher.find()) {
                        return matcher.group();
                    }
                    return message;
                }

                // Generic SQL Exception
                if (cause instanceof java.sql.SQLException) {
                    return cause.getMessage();
                }
            }
            cause = cause.getCause();
        }

        // If no specific error found, return the original message
        return e.getMessage() != null ? e.getMessage() : "Unknown database error";
    }


    /**
     * Extracts the full database error with all details
     */
    private String extractFullDatabaseError(Exception e) {
        StringBuilder fullError = new StringBuilder();
        Throwable cause = e;
        while (cause != null) {
            if (cause.getMessage() != null) {
                if (fullError.length() > 0) {
                    fullError.append("; ");
                }
                fullError.append(cause.getMessage());
            }
            cause = cause.getCause();
        }
        return fullError.toString();
    }

    /**
     * Extracts the detailed Oracle error message from exception chain
     */
    private String extractDetailedOracleError(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null) {
                // Look for ORA-xxxxx pattern
                if (message.contains("ORA-")) {
                    return message;  // Return the full Oracle error
                }
                // Check for SQLException with Oracle error
                if (cause instanceof java.sql.SQLException) {
                    return message;
                }
            }
            cause = cause.getCause();
        }

        // Fallback to original message if no ORA error found
        return e.getMessage() != null ? e.getMessage() : "Unknown database error";
    }

    /**
     * Truncate error message to fit database column limits
     */
    private String truncateErrorMessage(String message, int maxLength) {
        if (message == null) {
            return null;
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }

    /**
     * Helper method to update captured request with error (with truncation for database)
     */
    private void updateCapturedRequestWithError(String requestId, String capturedRequestId,
                                                int statusCode, String errorMessage,
                                                long executionDurationMs) {
        try {
            // Truncate error message for database storage (255 chars max)
            String dbErrorMessage = truncateErrorMessage(errorMessage, 250);

            apiRequestService.updateRequestWithError(
                    requestId,
                    capturedRequestId,
                    statusCode,
                    dbErrorMessage,
                    executionDurationMs
            );
        } catch (Exception e) {
            log.error("Failed to update captured request with error: {}", e.getMessage());
            // Don't rethrow - this is a non-critical operation
        }
    }

    /**
     * Convert ExecuteApiRequestDTO to ApiRequestDTO
     */
    private ApiRequestDTO convertExecuteRequestToApiRequestDTO(ExecuteApiRequestDTO executeRequest, GeneratedApiEntity api) {
        ApiRequestDTO dto = new ApiRequestDTO();

        dto.setRequestName("Execution: " + api.getApiName());
        dto.setDescription("API execution request");
        dto.setHttpMethod(executeRequest.getHttpMethod());
        dto.setUrl(executeRequest.getUrl());
        dto.setBasePath(api.getBasePath());
        dto.setEndpointPath(api.getEndpointPath());
        dto.setRequestTimeoutSeconds(executeRequest.getTimeoutSeconds());

        // Set request components
        dto.setPathParameters(executeRequest.getPathParams());
        dto.setQueryParameters(executeRequest.getQueryParams());
        dto.setHeaders(executeRequest.getHeaders());
        dto.setRequestBody(executeRequest.getBody() instanceof Map ?
                (Map<String, Object>) executeRequest.getBody() : null);

        // Set correlation ID
        dto.setCorrelationId(executeRequest.getRequestId());

        // Set metadata
        dto.setMetadata(executeRequest.getMetadata());

        // Set auth type from API (will be populated from request if available)
        if (api.getAuthConfig() != null) {
            dto.setAuthType(api.getAuthConfig().getAuthType());
        }

        return dto;
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
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Testing API: " + apiId + " with test: " + testRequest.getTestName());

            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Get the appropriate execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

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
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error testing API: " + e.getMessage());
            throw new RuntimeException("Failed to test API: " + e.getMessage(), e);
        }
    }


    public GeneratedApiResponseDTO getApiDetails(String requestId, String apiId) {
        try {
            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Get the appropriate execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            GeneratedApiResponseDTO response = conversionHelper.mapToResponse(api);

            response.setTotalCalls(api.getTotalCalls());
            response.setLastCalledAt(api.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(databaseType);
                analyticsHelper.addAverageExecutionTime(response, avgTime);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());
            throw new RuntimeException("Failed to get API details: " + e.getMessage(), e);
        }
    }



    public GeneratedAPIDTO getGeneratedApiDetails(String requestId, String apiId) {
        try {
            // First get the API entity to know its database type
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type from the API
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Get the appropriate execution helper based on database type
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            // Get the API entity using the helper (though we already have it, this ensures consistency)
            GeneratedApiEntity apiEntity = executionHelper.getApiEntity(generatedAPIRepository, apiId);

            // Map to DTO
            GeneratedAPIDTO response = conversionHelper.mapToGeneratedAPIDTO(apiEntity, objectMapper);

            // Set statistics
            response.setTotalCalls(apiEntity.getTotalCalls());
            response.setLastCalledAt(apiEntity.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            // Optionally add average time to response if needed

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());
            throw new RuntimeException("Failed to get API details: " + e.getMessage(), e);
        }
    }



    public ApiDetailsResponseDTO getCompleteApiDetails(String requestId, String apiId) {
        try {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
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
                ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
                analyticsHelper.addAverageExecutionTimeToDetails(response, avgTime);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
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
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
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

            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", API partially updated successfully: " + savedApi.getId());

            return conversionHelper.mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error partially updating API: " + e.getMessage());
            throw new RuntimeException("Failed to partially update API: " + e.getMessage(), e);
        }
    }


    @Transactional
    public GeneratedApiResponseDTO updateApiStatus(String apiId, String status, String performedBy) {
        try {
            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            // Get the appropriate execution helper (not really needed for status update, but for consistency)
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            // Get the appropriate validation helper based on the API's database type
            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);

            validationHelper.validateApiStatus(status);

            api.setStatus(status);
            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);

            GeneratedApiEntity updatedApi = generatedAPIRepository.save(api);

            loggerUtil.log("apiAutomation", "API status updated: " + apiId +
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
            // Get the API entity first
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Get the database type
            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(databaseType);
            return analyticsHelper.buildApiAnalytics(
                    executionLogRepository, apiId, startDate, endDate);
        } catch (Exception e) {
            loggerUtil.log("apiAutomation", "Request ID: " + requestId +
                    ", Error getting API analytics: " + e.getMessage());
            throw new RuntimeException("Failed to get API analytics: " + e.getMessage(), e);
        }
    }


        /**
         * Validates source object for API generation
         * Supports multiple database types: Oracle, PostgreSQL, etc.
         */
    public Map<String, Object> validateSourceObject(ApiSourceObjectDTO sourceObject) {
        // Get database type from source object
        String databaseType = sourceObject.getDatabaseType();

        // If not set, try to determine from context
        if (databaseType == null || databaseType.isEmpty()) {
            databaseType = determineDatabaseType(sourceObject);
            sourceObject.setDatabaseType(databaseType);
        }

        log.info("Validating source object: {}.{} ({}) on database: {}",
                sourceObject.getOwner(), sourceObject.getObjectName(),
                sourceObject.getObjectType(), databaseType);

        // Get the appropriate validation helper
        DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);

        // Get the appropriate schema service and cast to DatabaseSchemaService
        DatabaseSchemaService schemaService = (DatabaseSchemaService) databaseTypeFactory.getSchemaService(databaseType);

        // Get the appropriate metadata helper
        DatabaseMetadataHelper metadataHelper = databaseTypeFactory.getMetadataHelper(databaseType);

        // Validate using the helper
        return validationHelper.validateSourceObject(
                schemaService,
                sourceObject,
                (source) -> metadataHelper.getSourceObjectDetails(schemaService, source)
        );
    }

    /**
     * Validate Oracle source object
     */
    private Map<String, Object> validateOracleSourceObject(ApiSourceObjectDTO sourceObject) {
        return oracleValidationHelper.validateSourceObject(
                oracleSchemaService,
                sourceObject,
                this::getOracleSourceObjectDetails
        );
    }

    /**
     * Validate PostgreSQL source object
     */
    private Map<String, Object> validatePostgreSQLSourceObject(ApiSourceObjectDTO sourceObject) {
        return postgresValidationHelper.validateSourceObject(
                postgreSQLSchemaService,
                sourceObject,
                this::getPostgreSQLSourceObjectDetails
        );
    }

    /**
     * Get Oracle source object details using metadata helper factory
     */
    private Map<String, Object> getOracleSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        DatabaseMetadataHelper metadataHelper = metadataHelperFactory.getMetadataHelper("oracle");
        return metadataHelper.getSourceObjectDetails(oracleSchemaService, sourceObject);
    }

    /**
     * Get PostgreSQL source object details using metadata helper factory
     */
    private Map<String, Object> getPostgreSQLSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        DatabaseMetadataHelper metadataHelper = metadataHelperFactory.getMetadataHelper("postgresql");
        return metadataHelper.getSourceObjectDetails(postgreSQLSchemaService, sourceObject);
    }


    /**
     * Determine database type from source object context
     */
    private String determineDatabaseType(ApiSourceObjectDTO sourceObject) {
        // First, check if database type is explicitly set in the DTO
        if (sourceObject.getDatabaseType() != null && !sourceObject.getDatabaseType().isEmpty()) {
            return sourceObject.getDatabaseType();
        }

        // Check owner - PostgreSQL often uses 'public' schema
        if ("public".equalsIgnoreCase(sourceObject.getOwner())) {
            return "postgresql";
        }

        // Check if the object name uses PostgreSQL naming conventions (lowercase with underscores)
        String objectName = sourceObject.getObjectName();
        if (objectName != null && objectName.matches("^[a-z0-9_]+$")) {
            return "postgresql";
        }

        // Check if the object type is PostgreSQL-specific
        String objectType = sourceObject.getObjectType();
        if (objectType != null) {
            String upperType = objectType.toUpperCase();
            if (upperType.equals("MATERIALIZED VIEW") ||
                    upperType.equals("SEQUENCE") ||
                    upperType.equals("TYPE")) {
                return "postgresql";
            }
        }

        // Check if the object has target info (for synonyms - Oracle specific)
        if (sourceObject.getIsSynonym() != null && sourceObject.getIsSynonym()) {
            return "oracle";
        }

        // Default to Oracle
        return "oracle";
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
        // First get the API entity to know its database type
        GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

        // Get the database type from the API
        String databaseType = api.getDatabaseType();
        if (databaseType == null || databaseType.isEmpty()) {
            databaseType = "oracle";
        }

        // Get the appropriate execution helper based on database type
        BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);
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
        // This method is called from validation - we need to know the database type
        String databaseType = sourceObject.getDatabaseType();
        if (databaseType == null || databaseType.isEmpty()) {
            databaseType = determineDatabaseType(sourceObject);
            sourceObject.setDatabaseType(databaseType);
        }

        // Get the appropriate metadata helper
        DatabaseMetadataHelper metadataHelper = databaseTypeFactory.getMetadataHelper(databaseType);

        // Get the appropriate schema service and cast to DatabaseSchemaService
        DatabaseSchemaService schemaService = (DatabaseSchemaService) databaseTypeFactory.getSchemaService(databaseType);

        return metadataHelper.getSourceObjectDetails(schemaService, sourceObject);
    }

    private boolean shouldRegenerateComponents(GenerateApiRequestDTO request) {
        return false;
    // return request.getRegenerateComponents() != null && request.getRegenerateComponents();
    }

    private String getCodeBaseRequestId(GeneratedApiEntity api) {
        ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
        return analyticsHelper.getCodeBaseRequestId(api);
    }

    private String getCollectionsCollectionId(GeneratedApiEntity api) {
        ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
        return analyticsHelper.getCollectionsCollectionId(api);
    }

    private String getDocumentationCollectionId(GeneratedApiEntity api) {
        ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
        return analyticsHelper.getDocumentationCollectionId(api);
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



    /**
     * Sets the generated API ID for any entity that has a generatedApiId field
     * This ensures all related entities are properly linked to the main API
     */
    private void setGeneratedApiIdForEntity(Object entity, String generatedApiId) {
        if (entity == null || generatedApiId == null) return;

        try {
            // Use reflection to find and set generatedApiId field
            java.lang.reflect.Field field = getGeneratedApiIdField(entity.getClass());
            if (field != null) {
                field.setAccessible(true);
                field.set(entity, generatedApiId);
            }
        } catch (Exception e) {
            log.warn("Could not set generatedApiId for entity: {} - {}",
                    entity.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Recursively find the generatedApiId field in class hierarchy
     */
    private java.lang.reflect.Field getGeneratedApiIdField(Class<?> clazz) {
        while (clazz != null && clazz != Object.class) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField("generatedApiId");
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Sets generatedApiId for a collection of entities
     */
    private <T> void setGeneratedApiIdForEntities(Collection<T> entities, String generatedApiId) {
        if (entities == null) return;
        entities.forEach(entity -> setGeneratedApiIdForEntity(entity, generatedApiId));
    }


    /**
     * Parse XML body and extract parameter values
     */
    private Map<String, Object> parseXmlParameters(String xmlBody, List<ApiParameterEntity> parameters) {
        Map<String, Object> extractedParams = new HashMap<>();

        if (xmlBody == null || xmlBody.trim().isEmpty()) {
            return extractedParams;
        }

        log.info("Parsing XML body to extract parameter values");

        try {
            // For each parameter, try to extract its value from XML
            for (ApiParameterEntity param : parameters) {
                String paramKey = param.getKey();
                if (paramKey == null || paramKey.isEmpty()) {
                    continue;
                }

                // Look for XML tags with this key (case-insensitive)
                Pattern pattern = Pattern.compile("<" + paramKey + ">(.*?)</" + paramKey + ">",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(xmlBody);

                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    extractedParams.put(paramKey, value);
                    log.info("✅ Extracted XML parameter: {} = {}", paramKey, value);
                }
            }

            log.info("Extracted {} parameters from XML: {}", extractedParams.size(), extractedParams.keySet());

        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
    }

}