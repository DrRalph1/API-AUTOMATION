package com.usg.apiGeneration.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiGeneration.dtos.apiGenerationEngine.*;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.*;
import com.usg.apiGeneration.entities.postgres.apiSecurity.IPWhitelistEntryEntity;
import com.usg.apiGeneration.entities.postgres.codeBase.ImplementationEntity;
import com.usg.apiGeneration.entities.postgres.collections.*;
import com.usg.apiGeneration.factories.ApiExecutionHelperFactory;
import com.usg.apiGeneration.factories.ApiMetadataHelperFactory;
import com.usg.apiGeneration.factories.DatabaseTypeServiceFactory;
import com.usg.apiGeneration.factories.ParameterGeneratorFactory;
import com.usg.apiGeneration.helpers.*;
import com.usg.apiGeneration.helpers.apiEngine.oracle.OracleApiValidationHelper;
import com.usg.apiGeneration.helpers.apiEngine.postgresql.PostgreSQLApiValidationHelper;
import com.usg.apiGeneration.repositories.apiGenerationEngine.*;
import com.usg.apiGeneration.repositories.apiSecurity.IPWhitelistEntryRepository;
import com.usg.apiGeneration.repositories.codeBase.*;
import com.usg.apiGeneration.repositories.codeBase.FolderRepository;
import com.usg.apiGeneration.repositories.collections.AuthConfigRepository;
import com.usg.apiGeneration.repositories.collections.HeaderRepository;
import com.usg.apiGeneration.repositories.collections.ParameterRepository;
import com.usg.apiGeneration.repositories.documentation.*;
import com.usg.apiGeneration.helpers.apiEngine.*;
import com.usg.apiGeneration.interfaces.DatabaseSchemaService;
import com.usg.apiGeneration.services.schemaBrowser.OracleSchemaService;
import com.usg.apiGeneration.services.schemaBrowser.PostgreSQLSchemaService;
import com.usg.apiGeneration.utils.apiEngine.*;
import com.usg.apiGeneration.utils.LoggerUtil;
import com.usg.apiGeneration.utils.apiEngine.executor.oracle.*;
import com.usg.apiGeneration.utils.apiEngine.executor.postgresql.PostgreSQLFunctionExecutorUtil;
import com.usg.apiGeneration.utils.apiEngine.executor.postgresql.PostgreSQLProcedureExecutorUtil;
import com.usg.apiGeneration.utils.apiEngine.executor.postgresql.PostgreSQLTableExecutorUtil;
import com.usg.apiGeneration.utils.apiEngine.executor.postgresql.PostgreSQLViewExecutorUtil;
import com.usg.apiGeneration.utils.apiEngine.generator.CodeBaseGeneratorUtil;
import com.usg.apiGeneration.utils.apiEngine.generator.CollectionsGeneratorUtil;
import com.usg.apiGeneration.utils.apiEngine.generator.CustomQueryParserUtil;
import com.usg.apiGeneration.utils.apiEngine.generator.DocumentationGeneratorUtil;
import com.usg.apiGeneration.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.apiGeneration.utils.apiEngine.OracleParameterGeneratorUtil;
import com.usg.apiGeneration.utils.apiEngine.OracleParameterValidatorUtil;
import com.usg.apiGeneration.utils.apiEngine.OracleTypeMapperUtil;
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
    private final com.usg.apiGeneration.repositories.collections.CollectionRepository collectionsCollectionRepository;
    private final com.usg.apiGeneration.repositories.collections.FolderRepository collectionsFolderRepository;
    private final com.usg.apiGeneration.repositories.collections.RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;
    private final APICollectionRepository docCollectionRepository;
    private final com.usg.apiGeneration.repositories.documentation.FolderRepository docFolderRepository;
    private final APIEndpointRepository endpointRepository;
    private final com.usg.apiGeneration.repositories.documentation.HeaderRepository docHeaderRepository;
    private final com.usg.apiGeneration.repositories.documentation.ParameterRepository docParameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;

    // ==================== HELPERS (Business Logic) ====================
    private final OracleApiValidationHelper oracleValidationHelper;
    private final PostgreSQLApiValidationHelper postgresValidationHelper;
    private final ApiConversionHelper conversionHelper;
    private final ApiResponseHelper responseHelper;
    private final CustomQueryParserUtil customQueryParserUtil;
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

    private final IPWhitelistEntryRepository ipWhitelistRepository;

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

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Generating API: " + request.getApiName() +
                    " for database: " + databaseType + " by: " + performedBy);

            // Log if this is a custom query
            if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                    (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {
                loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                        ", This is a CUSTOM QUERY API generation");
            }

            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);
            validationHelper.validateApiCodeUniqueness(generatedAPIRepository, request.getApiCode());

            Object schemaService = databaseTypeFactory.getSchemaService(databaseType);
            ApiSourceObjectDTO sourceObjectDTO = convertAndValidateSourceObjectForGeneration(request, schemaService, databaseType);
            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());
            String endpointPath = buildEndpointPathFromRequest(request);
            DatabaseParameterGeneratorUtil parameterGenerator = parameterGeneratorFactory.getGenerator(databaseType);

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

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + savedApi.getId() +
                    ", Database: " + databaseType +
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

            // Log if this is a custom query update
            if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                    (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {
                loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                        ", This is a CUSTOM QUERY API update");
            }

            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            String databaseType = api.getDatabaseType();
            if (databaseType == null || databaseType.isEmpty()) {
                databaseType = "oracle";
            }

            log.info("Updating API: {} on database: {}", apiId, databaseType);

            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);
            String originalSourceRequestId = api.getSourceRequestId();

            DatabaseValidationHelper validationHelper = databaseTypeFactory.getValidationHelper(databaseType);
            validationHelper.validateApiCodeUniquenessOnUpdate(
                    generatedAPIRepository, api.getApiCode(), request.getApiCode());

            Object schemaService = databaseTypeFactory.getSchemaService(databaseType);

            // CRITICAL: For custom queries, pass the custom SELECT statement through
            ApiSourceObjectDTO sourceObjectDTO = convertAndValidateSourceObjectForUpdate(request, schemaService, databaseType);

            CollectionInfoDTO collectionInfo = validationHelper.validateAndGetCollectionInfo(request.getCollectionInfo());

            executionHelper.updateApiEntity(api, request, sourceObjectDTO, collectionInfo, performedBy);
            executionHelper.clearApiRelationships(api);

            DatabaseParameterGeneratorUtil parameterGenerator = parameterGeneratorFactory.getGenerator(databaseType);
            executionHelper.recreateApiRelationships(api, request, sourceObjectDTO,
                    parameterGenerator, conversionHelper);

            api.setSourceRequestId(originalSourceRequestId);
            api.setDatabaseType(databaseType);

            // CRITICAL: Store custom query information in the API entity
            if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                    (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {

                Map<String, Object> sourceObjectMap = api.getSourceObjectInfo();
                if (sourceObjectMap == null) {
                    sourceObjectMap = new HashMap<>();
                }
                sourceObjectMap.put("isCustomQuery", true);
                sourceObjectMap.put("customSelectStatement", request.getCustomSelectStatement());
                sourceObjectMap.put("useCustomQuery", true);
                sourceObjectMap.put("objectType", "CUSTOM_QUERY");
                sourceObjectMap.put("operation", "SELECT");
                sourceObjectMap.put("databaseType", databaseType);
                api.setSourceObjectInfo(sourceObjectMap);

                log.info("Stored custom query in API entity: {}",
                        request.getCustomSelectStatement().substring(0, Math.min(100, request.getCustomSelectStatement().length())));
            }

            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);
            entityManager.flush();

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

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API updated successfully: " + savedApi.getId() +
                    " on database: " + databaseType);

            return conversionHelper.mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
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

        // Check for custom query FIRST
        if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {

            log.info("Processing custom SELECT statement for API generation");

            Map<String, Object> sourceObjectMap = request.getSourceObject();
            if (sourceObjectMap == null) {
                sourceObjectMap = new HashMap<>();
            }
            sourceObjectMap.put("customSelectStatement", request.getCustomSelectStatement());
            sourceObjectMap.put("objectType", "CUSTOM_QUERY");
            sourceObjectMap.put("operation", "SELECT");
            sourceObjectMap.put("databaseType", databaseType);
            sourceObjectMap.put("isCustomQuery", true);
            sourceObjectMap.put("useCustomQuery", true);

            return conversionHelper.convertAndValidateSourceObjectForCustomQuery(
                    sourceObjectMap, objectMapper, databaseType, customQueryParserUtil
            );
        }

        // Regular database object flow
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
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
        // Check for custom query FIRST
        if (Boolean.TRUE.equals(request.getUseCustomQuery()) ||
                (request.getCustomSelectStatement() != null && !request.getCustomSelectStatement().trim().isEmpty())) {

            log.info("Processing custom SELECT statement for API update");

            Map<String, Object> sourceObjectMap = request.getSourceObject();
            if (sourceObjectMap == null) {
                sourceObjectMap = new HashMap<>();
            }
            sourceObjectMap.put("customSelectStatement", request.getCustomSelectStatement());
            sourceObjectMap.put("objectType", "CUSTOM_QUERY");
            sourceObjectMap.put("operation", "SELECT");
            sourceObjectMap.put("databaseType", databaseType);
            sourceObjectMap.put("isCustomQuery", true);
            sourceObjectMap.put("useCustomQuery", true);

            return conversionHelper.convertAndValidateSourceObjectForCustomQuery(
                    sourceObjectMap, objectMapper, databaseType, customQueryParserUtil
            );
        }

        // Regular database object flow
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            return conversionHelper.convertAndValidateSourceObject(
                    request.getSourceObject(), null, objectMapper);
        } else {
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
                Optional<com.usg.apiGeneration.entities.postgres.collections.RequestEntity> existingRequestOpt =
                        collectionsRequestRepository.findById(originalSourceRequestId);

                if (existingRequestOpt.isPresent()) {
                    var existingRequest = existingRequestOpt.get();

                    // Store current IDs BEFORE any changes
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
                        log.info("Collection or folder changed, moving request to new location");

                        // Get or create the new collection
                        CollectionEntity newCollection;
                        if (newCollectionId != null) {
                            Optional<CollectionEntity> existingCollectionOpt = collectionsCollectionRepository.findById(newCollectionId);
                            if (existingCollectionOpt.isPresent()) {
                                newCollection = existingCollectionOpt.get();
                            } else {
                                // Create new collection
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
                                newCollection = collectionsCollectionRepository.save(newColl);
                            }
                        } else {
                            newCollection = existingRequest.getCollection();
                        }

                        // Get or create the new folder
                        FolderEntity newFolder;
                        if (newFolderId != null) {
                            Optional<FolderEntity> existingFolderOpt = collectionsFolderRepository.findById(newFolderId);
                            if (existingFolderOpt.isPresent()) {
                                newFolder = existingFolderOpt.get();
                            } else {
                                // Create new folder
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
                                newFolder = collectionsFolderRepository.save(newFldr);
                            }
                        } else {
                            newFolder = existingRequest.getFolder();
                        }

                        // CRITICAL: Clear all relationships from the existing request
                        clearAllRequestRelationships(existingRequest);

                        // Update the request with new collection and folder
                        existingRequest.setCollection(newCollection);
                        existingRequest.setFolder(newFolder);

                        // Update the request content
                        updateRequestContent(existingRequest, api, performedBy, request);

                        // Save the updated request
                        collectionsRequestRepository.save(existingRequest);

                        // CRITICAL: Flush and clear the EntityManager to ensure all changes are persisted
                        entityManager.flush();
                        entityManager.clear();

                        log.info("Successfully moved request to new collection/folder");
                        return;
                    }

                    // If collection/folder hasn't changed, just update the content
                    log.info("No collection/folder change, just updating content");
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
    private void clearAllRequestRelationships(com.usg.apiGeneration.entities.postgres.collections.RequestEntity request) {
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
    private void updateRequestContent(com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
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
                                                       com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
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
                                                             com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
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
                                  com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
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


    /**
     * Update documentation for an API
     * Handles moving endpoints between collections and folders WITHOUT deleting endpoints
     */
    private void updateDocumentation(GeneratedApiEntity api, String performedBy,
                                     GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                     String codeBaseRequestId, String collectionsCollectionId) {
        try {
            log.info("Updating documentation for API: {} with codeBaseRequestId: {} and collectionId: {}",
                    api.getId(), codeBaseRequestId, collectionsCollectionId);

            // Call the new update method instead of generate
            documentationGeneratorUtil.update(api, performedBy, request,
                    codeBaseRequestId, collectionsCollectionId, collectionInfo);

        } catch (Exception e) {
            log.error("Error updating documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update documentation: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to build endpoint URL
     */
    private String buildEndpointUrl(GeneratedApiEntity api) {
        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);
        return genUrlInfo.getFullUrl();
    }

    /**
     * Helper method to update documentation collection
     */
    private void updateDocumentationCollection(
            com.usg.apiGeneration.entities.postgres.documentation.APICollectionEntity collection,
            GeneratedApiEntity api, String performedBy,
            GenerateApiRequestDTO request,
            CollectionInfoDTO collectionInfo) {
        try {
            if (collection == null) return;

            if (collectionInfo != null && collectionInfo.getCollectionName() != null) {
                collection.setName(collectionInfo.getCollectionName());
            } else if (api.getApiName() != null) {
                collection.setName(api.getApiName() + " Documentation");
            }

            collection.setDescription("Documentation for " + api.getApiName());
            collection.setUpdatedAt(LocalDateTime.now());
            collection.setUpdatedBy(performedBy);

            // Update version
            if (api.getVersion() != null) {
                collection.setVersion(api.getVersion());
            }

            // Update metadata
            if (collection.getMetadata() == null) {
                collection.setMetadata(new java.util.HashMap<>());
            }
            collection.getMetadata().put("apiCode", api.getApiCode());
            collection.getMetadata().put("apiVersion", api.getVersion() != null ? api.getVersion() : "1.0");
            collection.getMetadata().put("lastUpdated", LocalDateTime.now().toString());
            collection.getMetadata().put("updatedBy", performedBy);

            // Update total endpoints count if endpoints list exists
            if (collection.getEndpoints() != null) {
                collection.setTotalEndpoints(collection.getEndpoints().size());
            }

            // Update total folders count
            if (collection.getFolders() != null) {
                collection.setTotalFolders(collection.getFolders().size());
            }

            docCollectionRepository.save(collection);

        } catch (Exception e) {
            log.error("Error updating documentation collection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update documentation collection", e);
        }
    }

    /**
     * Helper method to clear endpoint relationships
     */
    private void clearEndpointRelationships(
            com.usg.apiGeneration.entities.postgres.documentation.APIEndpointEntity endpoint) {
        try {
            if (endpoint.getHeaders() != null && !endpoint.getHeaders().isEmpty()) {
                docHeaderRepository.deleteAll(endpoint.getHeaders());
                endpoint.getHeaders().clear();
            }

            if (endpoint.getParameters() != null && !endpoint.getParameters().isEmpty()) {
                docParameterRepository.deleteAll(endpoint.getParameters());
                endpoint.getParameters().clear();
            }

            if (endpoint.getResponseExamples() != null && !endpoint.getResponseExamples().isEmpty()) {
                responseExampleRepository.deleteAll(endpoint.getResponseExamples());
                endpoint.getResponseExamples().clear();
            }

            docHeaderRepository.flush();
            docParameterRepository.flush();
            responseExampleRepository.flush();

        } catch (Exception e) {
            log.error("Error clearing endpoint relationships: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to clear endpoint relationships", e);
        }
    }

    /**
     * Helper method to create documentation headers
     */
    private List<com.usg.apiGeneration.entities.postgres.documentation.HeaderEntity> createDocumentationHeaders(
            GeneratedApiEntity api,
            com.usg.apiGeneration.entities.postgres.documentation.APIEndpointEntity endpoint) {

        List<com.usg.apiGeneration.entities.postgres.documentation.HeaderEntity> headers = new ArrayList<>();

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                com.usg.apiGeneration.entities.postgres.documentation.HeaderEntity header =
                        new com.usg.apiGeneration.entities.postgres.documentation.HeaderEntity();
                header.setId(UUID.randomUUID().toString());
                header.setGeneratedApiId(api.getId());
                header.setKey(apiHeader.getKey());
                header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                header.setDescription(apiHeader.getDescription());
                header.setRequired(apiHeader.getRequired() != null ? apiHeader.getRequired() : false);
                header.setEndpoint(endpoint);
                headers.add(header);
            }
        }

        return headers;
    }

    /**
     * Helper method to create documentation parameters
     */
    private List<com.usg.apiGeneration.entities.postgres.documentation.ParameterEntity> createDocumentationParameters(
            GeneratedApiEntity api,
            com.usg.apiGeneration.entities.postgres.documentation.APIEndpointEntity endpoint) {

        List<com.usg.apiGeneration.entities.postgres.documentation.ParameterEntity> parameters = new ArrayList<>();

        if (api.getParameters() != null) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                com.usg.apiGeneration.entities.postgres.documentation.ParameterEntity param =
                        new com.usg.apiGeneration.entities.postgres.documentation.ParameterEntity();
                param.setId(UUID.randomUUID().toString());
                param.setGeneratedApiId(api.getId());
                param.setName(apiParam.getKey());
                param.setKey(apiParam.getKey());
                param.setDbColumn(apiParam.getDbColumn());
                param.setDbParameter(apiParam.getDbParameter());
                param.setParameterType(apiParam.getParameterType());
                param.setOracleType(apiParam.getOracleType());
                param.setApiType(apiParam.getApiType());
                param.setParameterLocation(apiParam.getParameterLocation());
                param.setRequired(apiParam.getRequired());
                param.setDescription(apiParam.getDescription());
                param.setDefaultValue(apiParam.getDefaultValue());
                param.setExample(apiParam.getExample());
                param.setValidationPattern(apiParam.getValidationPattern());
                param.setInBody(apiParam.getInBody());
                param.setIsPrimaryKey(apiParam.getIsPrimaryKey());
                param.setParamMode(apiParam.getParamMode());
                param.setPosition(apiParam.getPosition());
                param.setEndpoint(endpoint);
                parameters.add(param);
            }
        }

        return parameters;
    }

    /**
     * Helper method to create response examples using ApiResponseConfigEntity
     */
    private List<com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity> createResponseExamples(
            GeneratedApiEntity api,
            com.usg.apiGeneration.entities.postgres.documentation.APIEndpointEntity endpoint) {

        List<com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity> responses = new ArrayList<>();
        ApiResponseConfigEntity responseConfig = api.getResponseConfig();

        if (responseConfig != null) {
            // Create success response example from successSchema
            if (responseConfig.getSuccessSchema() != null && !responseConfig.getSuccessSchema().isEmpty()) {
                com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity successResponse =
                        new com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity();
                successResponse.setId(UUID.randomUUID().toString());
                successResponse.setGeneratedApiId(api.getId());
                successResponse.setStatusCode(200);
                successResponse.setDescription("Success response");
                successResponse.setContentType(responseConfig.getContentType() != null ?
                        responseConfig.getContentType() : "application/json");

                // Parse successSchema to Map if it's JSON
                try {
                    String successSchema = responseConfig.getSuccessSchema();
                    if (successSchema != null) {
                        Map<String, Object> exampleMap = objectMapper.readValue(successSchema, Map.class);
                        successResponse.setExample(exampleMap);
                    }
                } catch (Exception e) {
                    log.warn("Could not parse successSchema as JSON: {}", e.getMessage());
                    Map<String, Object> fallbackMap = new HashMap<>();
                    fallbackMap.put("response", responseConfig.getSuccessSchema());
                    successResponse.setExample(fallbackMap);
                }

                successResponse.setEndpoint(endpoint);
                responses.add(successResponse);
            }

            // Create error response example from errorSchema
            if (responseConfig.getErrorSchema() != null && !responseConfig.getErrorSchema().isEmpty()) {
                com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity errorResponse =
                        new com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity();
                errorResponse.setId(UUID.randomUUID().toString());
                errorResponse.setGeneratedApiId(api.getId());
                errorResponse.setStatusCode(400);
                errorResponse.setDescription("Error response");
                errorResponse.setContentType(responseConfig.getContentType() != null ?
                        responseConfig.getContentType() : "application/json");

                // Parse errorSchema to Map if it's JSON
                try {
                    String errorSchema = responseConfig.getErrorSchema();
                    if (errorSchema != null) {
                        Map<String, Object> exampleMap = objectMapper.readValue(errorSchema, Map.class);
                        errorResponse.setExample(exampleMap);
                    }
                } catch (Exception e) {
                    log.warn("Could not parse errorSchema as JSON: {}", e.getMessage());
                    Map<String, Object> fallbackMap = new HashMap<>();
                    fallbackMap.put("error", "Bad Request");
                    fallbackMap.put("message", responseConfig.getErrorSchema());
                    errorResponse.setExample(fallbackMap);
                }

                errorResponse.setEndpoint(endpoint);
                responses.add(errorResponse);
            }

            // If no specific schemas, add default response
            if (responses.isEmpty()) {
                com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity defaultResponse =
                        new com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity();
                defaultResponse.setId(UUID.randomUUID().toString());
                defaultResponse.setGeneratedApiId(api.getId());
                defaultResponse.setStatusCode(200);
                defaultResponse.setDescription("Response");
                defaultResponse.setContentType("application/json");
                Map<String, Object> defaultMap = new HashMap<>();
                defaultMap.put("message", "API response");
                defaultResponse.setExample(defaultMap);
                defaultResponse.setEndpoint(endpoint);
                responses.add(defaultResponse);
            }
        } else {
            // No response config, add default response
            com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity defaultResponse =
                    new com.usg.apiGeneration.entities.postgres.documentation.ResponseExampleEntity();
            defaultResponse.setId(UUID.randomUUID().toString());
            defaultResponse.setGeneratedApiId(api.getId());
            defaultResponse.setStatusCode(200);
            defaultResponse.setDescription("Response");
            defaultResponse.setContentType("application/json");
            Map<String, Object> defaultMap = new HashMap<>();
            defaultMap.put("message", "API response");
            defaultResponse.setExample(defaultMap);
            defaultResponse.setEndpoint(endpoint);
            responses.add(defaultResponse);
        }

        return responses;
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
        String databaseType = "oracle";

        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
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

            // 3. Get the appropriate execution helper
            BaseApiExecutionHelper executionHelper = executionHelperFactory.getExecutionHelper(databaseType);

            // 4. Validate request structure
            if (executeRequest == null) {
                executeRequest = new ExecuteApiRequestDTO();
                executeRequest.setRequestId(UUID.randomUUID().toString());
            }

            // 5. Extract HTTP method
            String httpMethod = null;
            if (httpServletRequest != null) {
                httpMethod = httpServletRequest.getMethod();
                log.info("HTTP method from HttpServletRequest: {}", httpMethod);
                executeRequest.setHttpMethod(httpMethod);
            }

            // 6. Prepare and validate the request
            ExecuteApiRequestDTO validatedRequest = executionHelper.prepareValidatedRequest(api, executeRequest);

            if (validatedRequest.getHttpMethod() == null && httpMethod != null) {
                validatedRequest.setHttpMethod(httpMethod);
                log.info("Re-set HTTP method in validatedRequest: {}", httpMethod);
            }

            // 7. Get actual client IP and request path
            String actualClientIp = getClientIpAddress(httpServletRequest);
            String fullRequestPath = httpServletRequest != null ? httpServletRequest.getRequestURI() : "";
            String endpointPath = api.getEndpointPath();

            // ============ CAPTURE REQUEST BEFORE ANY VALIDATION ============
            // This ensures we capture ALL attempts including IP whitelist failures
            try {
                ApiRequestDTO requestDTO = convertExecuteRequestToApiRequestDTO(validatedRequest, api);
                requestDTO.setClientIpAddress(actualClientIp);
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
            // ============ END CAPTURE ============

            // ============ IP WHITELIST VALIDATION ============
            try {
                validateIpWhitelist(actualClientIp, fullRequestPath, endpointPath);
            } catch (RuntimeException e) {
                // IP whitelist validation failed
                String errorMsg = e.getMessage();
                int statusCode = 403;

                log.warn("IP whitelist validation failed: {}", errorMsg);

                // Update captured request with the error
                if (capturedRequestId != null) {
                    try {
                        updateCapturedRequestWithError(requestId, capturedRequestId, statusCode,
                                errorMsg, System.currentTimeMillis() - startTime);
                    } catch (Exception ex) {
                        log.error("Failed to update captured request with error: {}", ex.getMessage());
                    }
                }

                // Log the execution
                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, statusCode, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(statusCode, errorMsg, startTime);
            }
            // ============ END IP WHITELIST VALIDATION ============

            // 8. Check API status
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

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, statusCode, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(statusCode, errorMsg, startTime);
            }

            // 9. Check isActive flag
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

                executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                        null, 403, System.currentTimeMillis() - startTime,
                        performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(403, errorMsg, startTime);
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
                        performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

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
                            performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

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
            Map<String, Object> consolidatedParams = createConsolidatedParamsWithHeaders(validatedRequest, api);

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
                        performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

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
                        performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

                return responseHelper.createErrorResponse(403, errorMsg, startTime);
            }

            // 18. Rate limiting check
            if (!validatorService.checkRateLimit(api, actualClientIp)) {
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
                        performedBy, actualClientIp, userAgent, errorMsg, objectMapper);

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

                // ============ GENERIC RESPONSE HANDLING ============
                // Check if the result contains response metadata (like responseCode, status, etc.)
                boolean isResponseWithStatus = false;
                Integer determinedHttpStatus = null;
                String responseMessage = null;
                Object responseData = null;

                if (result instanceof Map) {
                    Map<String, Object> resultMap = (Map<String, Object>) result;

                    // Check for common response code fields
                    String responseCode = null;
                    if (resultMap.containsKey("responseCode")) {
                        responseCode = resultMap.get("responseCode").toString();
                        isResponseWithStatus = true;
                    } else if (resultMap.containsKey("code")) {
                        responseCode = resultMap.get("code").toString();
                        isResponseWithStatus = true;
                    } else if (resultMap.containsKey("status")) {
                        responseCode = resultMap.get("status").toString();
                        isResponseWithStatus = true;
                    }

                    // Extract message if present
                    if (resultMap.containsKey("message")) {
                        responseMessage = resultMap.get("message").toString();
                    } else if (resultMap.containsKey("msg")) {
                        responseMessage = resultMap.get("msg").toString();
                    }

                    // Extract data if present
                    if (resultMap.containsKey("data")) {
                        responseData = resultMap.get("data");
                    }

                    // Determine HTTP status based on response code patterns
                    if (isResponseWithStatus && responseCode != null) {
                        // Handle success codes (000, 200, "SUCCESS", etc.)
                        if ("000".equals(responseCode) || "200".equals(responseCode) ||
                                "SUCCESS".equalsIgnoreCase(responseCode) || "OK".equalsIgnoreCase(responseCode)) {
                            determinedHttpStatus = 200;
                        }
                        // Handle error codes
                        else {
                            // Try to map common error patterns
                            if (responseCode.startsWith("4")) {
                                try {
                                    determinedHttpStatus = Integer.parseInt(responseCode);
                                } catch (NumberFormatException e) {
                                    determinedHttpStatus = 400; // Default to 400 for 4xx
                                }
                            } else if (responseCode.startsWith("5")) {
                                try {
                                    determinedHttpStatus = Integer.parseInt(responseCode);
                                } catch (NumberFormatException e) {
                                    determinedHttpStatus = 500;
                                }
                            } else {
                                // For custom codes (like 401, 423, etc.), try to parse
                                try {
                                    int code = Integer.parseInt(responseCode);
                                    if (code >= 400 && code < 600) {
                                        determinedHttpStatus = code;
                                    } else {
                                        determinedHttpStatus = 500; // Default error
                                    }
                                } catch (NumberFormatException e) {
                                    determinedHttpStatus = 500; // Default error for non-numeric codes
                                }
                            }
                        }
                    }
                }

                // Prepare the final response
                ExecuteApiResponseDTO finalResponse;

                if (isResponseWithStatus && determinedHttpStatus != null && determinedHttpStatus >= 400) {
                    // This is an error response from the procedure
                    log.info("Procedure returned error status: HTTP={}, responseCode={}",
                            determinedHttpStatus, ((Map<String, Object>) result).get("responseCode"));

                    // Update captured request with error
                    if (capturedRequestId != null) {
                        try {
                            String errorMessage = responseMessage != null ? responseMessage : "Procedure execution error";
                            String dbErrorMessage = truncateErrorMessage(errorMessage, 250);
                            apiRequestService.updateRequestWithError(
                                    requestId, capturedRequestId, determinedHttpStatus,
                                    dbErrorMessage, executionTime);
                        } catch (Exception e) {
                            log.error("Failed to update captured request: {}", e.getMessage());
                        }
                    }

                    // Log the execution
                    executionHelper.logExecution(executionLogRepository, api, validatedRequest,
                            null, determinedHttpStatus, executionTime, performedBy,
                            actualClientIp, userAgent, responseMessage, objectMapper);

                    // Build error response
                    finalResponse = new ExecuteApiResponseDTO();
                    finalResponse.setResponseCode(determinedHttpStatus);
                    finalResponse.setSuccess(false);
                    finalResponse.setMessage(responseMessage != null ? responseMessage : "Operation failed");

                    // Set data - preserve the original structure
                    if (responseData != null) {
                        finalResponse.setData(responseData instanceof List ?
                                (List<?>) responseData : Collections.singletonList(responseData));
                    } else if (result instanceof Map) {
                        // If no data field, but result has other fields, use the whole result
                        Map<String, Object> resultMap = (Map<String, Object>) result;
                        // Remove metadata fields to avoid duplication
                        resultMap.remove("responseCode");
                        resultMap.remove("code");
                        resultMap.remove("status");
                        resultMap.remove("message");
                        resultMap.remove("msg");
                        if (!resultMap.isEmpty()) {
                            finalResponse.setData(Collections.singletonList(resultMap));
                        } else {
                            finalResponse.setData(Collections.emptyList());
                        }
                    } else {
                        finalResponse.setData(Collections.emptyList());
                    }

                    loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                            ", API returned error: HTTP=" + determinedHttpStatus +
                            ", responseCode=" + ((Map<String, Object>) result).get("responseCode"));

                    return finalResponse;
                }

                // ============ SUCCESS RESPONSE HANDLING ============

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
                        actualClientIp, userAgent, null, objectMapper);

                // 26. Build success response
                finalResponse = responseHelper.buildSuccessResponse(
                        formattedResponse, executionTime, api);

                loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                        ", API executed successfully: " + apiId +
                        " on database: " + databaseType +
                        " - Time: " + executionTime + "ms");

                return finalResponse;

            } catch (Exception e) {
                executionTime = System.currentTimeMillis() - startTime;
                log.error("Database execution failed for {}: ", databaseType, e);

                // Extract the actual database error
                String detailedError = extractDatabaseError(e, databaseType);

                log.info("Returning raw database error: {}", detailedError);

                // Create error response
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
                            null, 500, executionTime, performedBy, actualClientIp, userAgent,
                            logErrorMessage, objectMapper);
                } catch (Exception logError) {
                    log.error("Failed to log execution error: {}", logError.getMessage());
                }

                return errorResponse;
            }

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error executing API: " + e.getMessage());
            log.error("Error executing API: ", e);

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
                        null, 500, executionTime, performedBy, getClientIpAddress(httpServletRequest), userAgent,
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
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
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
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
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
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
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
                ApiAnalyticsHelper analyticsHelper = databaseTypeFactory.getAnalyticsHelper(api.getDatabaseType());
                analyticsHelper.addAverageExecutionTimeToDetails(response, avgTime);
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
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
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



    /**
     * Update codebase components when API is moved between collections/folders
     * This method delegates to CodeBaseGeneratorUtil.update() for the actual update logic
     */
    private void updateCodeBase(GeneratedApiEntity api, String performedBy,
                                GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Updating codebase for API: {} with collection: {}",
                    api.getId(), collectionInfo.getCollectionName());

            // Call the new update method in CodeBaseGeneratorUtil instead of handling it here
            codeBaseGeneratorUtil.update(api, performedBy, request, null, collectionInfo);

            // Note: The sourceRequestId is updated inside CodeBaseGeneratorUtil.update()
            // to maintain consistency with the new request

            log.info("Codebase update completed for API: {}", api.getId());

        } catch (Exception e) {
            log.error("Error updating codebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update codebase: " + e.getMessage(), e);
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

    /**
     * Helper method to completely clear all relationships from a codebase request
     * This is critical to avoid constraint violations
     */
    private void clearAllCodeBaseRequestRelationships(
            com.usg.apiGeneration.entities.postgres.codeBase.RequestEntity request) {
        try {
            log.debug("Clearing all relationships for codebase request: {}", request.getId());

            // CRITICAL: Clear and delete all implementations
            if (request.getImplementations() != null && !request.getImplementations().isEmpty()) {
                List<ImplementationEntity> implsToRemove = new ArrayList<>(request.getImplementations());
                request.getImplementations().clear();
                if (!implsToRemove.isEmpty()) {
                    implementationRepository.deleteAll(implsToRemove);
                    log.debug("Deleted {} implementations for request: {}", implsToRemove.size(), request.getId());
                }
            }

            // Clear collections and lists
            if (request.getHeaders() != null) {
                request.getHeaders().clear();
            }
            if (request.getPathParameters() != null) {
                request.getPathParameters().clear();
            }
            if (request.getQueryParameters() != null) {
                request.getQueryParameters().clear();
            }
            if (request.getTags() != null) {
                request.getTags().clear();
            }

            // Flush to ensure all deletions are processed immediately
            implementationRepository.flush();

            log.debug("Successfully cleared all relationships for codebase request: {}", request.getId());

        } catch (Exception e) {
            log.error("Error clearing relationships for codebase request {}: {}",
                    request.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to clear codebase request relationships", e);
        }
    }

    /**
     * Helper method to update codebase request content
     */
    private void updateCodeBaseRequestContent(
            com.usg.apiGeneration.entities.postgres.codeBase.RequestEntity request,
            GeneratedApiEntity api, String performedBy,
            GenerateApiRequestDTO generateRequest) {
        try {
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

            // Update basic info
            request.setName(api.getApiName());
            request.setMethod(api.getHttpMethod());
            request.setUrl(genUrlInfo.getFullUrl());
            request.setDescription(api.getDescription());
            request.setUpdatedAt(LocalDateTime.now());

            // Set request body from API request config
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("sample", api.getRequestConfig().getSample());
                request.setRequestBody(bodyMap);
            }

            // Set headers
            if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
                List<Map<String, Object>> headers = new ArrayList<>();
                for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                    Map<String, Object> header = new HashMap<>();
                    header.put("key", apiHeader.getKey());
                    header.put("value", apiHeader.getValue());
                    header.put("description", apiHeader.getDescription());
                    header.put("required", apiHeader.getRequired());
                    headers.add(header);
                }
                request.setHeaders(headers);
            }

            // Set path parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                List<Map<String, Object>> pathParams = new ArrayList<>();
                List<Map<String, Object>> queryParams = new ArrayList<>();

                for (ApiParameterEntity apiParam : api.getParameters()) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("name", apiParam.getKey());
                    param.put("type", apiParam.getOracleType());
                    param.put("required", apiParam.getRequired());
                    param.put("description", apiParam.getDescription());
                    param.put("example", apiParam.getExample());

                    if ("path".equalsIgnoreCase(apiParam.getParameterLocation())) {
                        pathParams.add(param);
                    } else if ("query".equalsIgnoreCase(apiParam.getParameterLocation())) {
                        queryParams.add(param);
                    }
                }

                request.setPathParameters(pathParams);
                request.setQueryParameters(queryParams);
            }

            // Set metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("apiId", api.getId());
            metadata.put("apiCode", api.getApiCode());
            metadata.put("databaseType", api.getDatabaseType());
            metadata.put("status", api.getStatus());
            metadata.put("version", api.getVersion());
            metadata.put("updatedBy", performedBy);
            request.setMetadata(metadata);

            // Set tags
            if (api.getTags() != null && !api.getTags().isEmpty()) {
                request.setTags(new ArrayList<>(api.getTags()));
            }

            // Save the request first
            request = codeBaseRequestRepository.save(request);

            // Create implementations (this is the critical part for codebase)
            createImplementationsForRequest(request, api, performedBy);

            log.info("Successfully updated codebase request content for: {}", request.getId());

        } catch (Exception e) {
            log.error("Error updating codebase request content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update codebase request content", e);
        }
    }

    /**
     * Helper method to create implementations for a request
     */
    private void createImplementationsForRequest(
            com.usg.apiGeneration.entities.postgres.codeBase.RequestEntity request,
            GeneratedApiEntity api, String performedBy) {
        try {
            log.info("Creating implementations for request: {}", request.getId());

            // Generate code for all languages
            Map<String, String> generatedCode = generateApiCode(api);

            if (generatedCode != null && !generatedCode.isEmpty()) {
                List<ImplementationEntity> newImplementations = new ArrayList<>();

                for (Map.Entry<String, String> codeEntry : generatedCode.entrySet()) {
                    String language = codeEntry.getKey();
                    String code = codeEntry.getValue();

                    ImplementationEntity impl = new ImplementationEntity();
                    impl.setId(UUID.randomUUID().toString());
                    impl.setGeneratedApiId(api.getId());
                    impl.setLanguage(language);
                    impl.setComponent("api_" + api.getApiCode().toLowerCase());
                    impl.setCode(code);
                    impl.setIsGenerated(true);
                    impl.setGeneratedBy(performedBy);
                    impl.setVersion(1);
                    impl.setIsValidated(false);
                    impl.setUsageCount(0);

                    // Set metadata
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("apiName", api.getApiName());
                    metadata.put("apiCode", api.getApiCode());
                    metadata.put("httpMethod", api.getHttpMethod());
                    metadata.put("endpointPath", api.getEndpointPath());
                    metadata.put("generatedAt", LocalDateTime.now().toString());
                    impl.setMetadata(metadata);

                    // Update file name and calculate lines
                    impl.updateFileName();
                    impl.calculateLinesOfCode();

                    newImplementations.add(impl);
                }

                // Save all implementations
                if (!newImplementations.isEmpty()) {
                    implementationRepository.saveAll(newImplementations);

                    // Add implementations to request
                    for (ImplementationEntity impl : newImplementations) {
                        request.addImplementation(impl);
                    }

                    log.info("Created {} implementations for request: {}",
                            newImplementations.size(), request.getId());
                }
            } else {
                log.warn("No code generated for API: {}", api.getId());
            }

        } catch (Exception e) {
            log.error("Error creating implementations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create implementations", e);
        }
    }

    /**
     * Helper method to update folder request flags for codebase
     * Note: FolderEntity uses hasRequests boolean, not requestCount
     */
    private void updateCodeBaseFolderRequestCounts(String oldFolderId, String newFolderId) {
        try {
            // Update old folder if it exists and is different from new folder
            if (oldFolderId != null && !oldFolderId.equals(newFolderId)) {
                codeBaseFolderRepository.findById(oldFolderId).ifPresent(oldFolder -> {
                    // Check if this folder still has any other requests
                    // You might need to count remaining requests in this folder
                    boolean hasOtherRequests = checkIfFolderHasRequests(oldFolder.getId());
                    oldFolder.setHasRequests(hasOtherRequests);
                    codeBaseFolderRepository.save(oldFolder);
                    log.debug("Updated hasRequests flag for old codebase folder: {} to {}",
                            oldFolderId, hasOtherRequests);
                });
            }

            // Update new folder if it exists and is different from old folder
            if (newFolderId != null && !newFolderId.equals(oldFolderId)) {
                codeBaseFolderRepository.findById(newFolderId).ifPresent(newFolder -> {
                    newFolder.setHasRequests(true);
                    codeBaseFolderRepository.save(newFolder);
                    log.debug("Set hasRequests flag to true for new codebase folder: {}", newFolderId);
                });
            }
        } catch (Exception e) {
            log.warn("Failed to update codebase folder request flags: {}", e.getMessage());
        }
    }

    /**
     * Helper method to check if a folder has any requests
     */
    private boolean checkIfFolderHasRequests(String folderId) {
        try {
            // Query to count requests in this folder
            // You might need to add this method to your RequestRepository
            long requestCount = codeBaseRequestRepository.countByFolderId(folderId);
            return requestCount > 0;
        } catch (Exception e) {
            log.error("Error checking if folder has requests: {}", e.getMessage());
            return true; // Assume true to be safe
        }
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


    // Add this helper method near the other helper methods
    /**
     * Generate current timestamp in format YYYYMMDDHHMMSS
     * Used for AUTOGENERATE data type parameters
     */
    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d%02d%02d%02d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
    }


    /**
     * Create consolidated parameters map that INCLUDES HEADERS for validation
     * Also handles AUTOGENERATE data types by auto-populating with current timestamp
     */
    private Map<String, Object> createConsolidatedParamsWithHeaders(ExecuteApiRequestDTO executeRequest, GeneratedApiEntity api) {
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

        // ============ HANDLE AUTOGENERATE PARAMETERS ============
        // Get all AUTOGENERATE parameters from the API and auto-populate them
        if (api != null && api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("AUTOGENERATE".equalsIgnoreCase(param.getOracleType())) {
                    String paramKey = param.getKey();
                    String timestamp = getCurrentTimestamp();

                    // Check if this parameter already has a meaningful value
                    boolean hasExistingValue = false;
                    Object existingValue = null;

                    for (Map.Entry<String, Object> entry : allParams.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(paramKey)) {
                            existingValue = entry.getValue();
                            // Treat null OR empty string as "no value"
                            if (existingValue != null && !existingValue.toString().trim().isEmpty()) {
                                hasExistingValue = true;
                                log.info("AUTOGENERATE parameter [{}] already has value: {}, keeping existing value",
                                        paramKey, existingValue);
                            } else {
                                log.info("AUTOGENERATE parameter [{}] has empty value, will auto-populate", paramKey);
                            }
                            break;
                        }
                    }

                    // If no meaningful existing value, auto-populate with timestamp
                    if (!hasExistingValue) {
                        allParams.put(paramKey, timestamp);
                        log.info("🔧 AUTOGENERATE parameter [{}] auto-populated with timestamp: {}", paramKey, timestamp);

                        // Also add to the appropriate location in the request
                        String location = param.getParameterLocation();
                        if ("path".equalsIgnoreCase(location) && executeRequest.getPathParams() != null) {
                            executeRequest.getPathParams().put(paramKey, timestamp);
                        } else if ("query".equalsIgnoreCase(location) && executeRequest.getQueryParams() != null) {
                            executeRequest.getQueryParams().put(paramKey, timestamp);
                        } else if ("header".equalsIgnoreCase(location) && executeRequest.getHeaders() != null) {
                            executeRequest.getHeaders().put(paramKey, timestamp);
                        } else if ("body".equalsIgnoreCase(location) && executeRequest.getBody() instanceof Map) {
                            ((Map<String, Object>) executeRequest.getBody()).put(paramKey, timestamp);
                        }
                    }
                }
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
            // Skip AUTOGENERATE parameters - they are auto-populated
            if ("AUTOGENERATE".equalsIgnoreCase(param.getOracleType())) {
                log.info("Skipping validation for AUTOGENERATE parameter: {}", param.getKey());
                continue;
            }

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



    /**
     * Check if the requesting IP is whitelisted and has access to the endpoint
     * DENY BY DEFAULT - Only allow if explicitly whitelisted
     * @param clientIp The client IP address (automatically obtained from request)
     * @param fullRequestPath The complete request path (e.g., /plx/api/gen/{apiId}/api/v1/create-bio-data)
     * @param endpointPath The API endpoint path (e.g., /create-bio-data)
     * @throws RuntimeException with appropriate message if access is denied
     */
    private void validateIpWhitelist(String clientIp, String fullRequestPath, String endpointPath) {
        try {
            // Get all active whitelist entries (case-insensitive)
            List<IPWhitelistEntryEntity> allEntries = ipWhitelistRepository.findAll();
            List<IPWhitelistEntryEntity> whitelistEntries = allEntries.stream()
                    .filter(entry -> entry.getStatus() != null && "ACTIVE".equalsIgnoreCase(entry.getStatus().trim()))
                    .collect(Collectors.toList());

            // DENY BY DEFAULT - If no whitelist entries exist, block everything
            if (whitelistEntries.isEmpty()) {
                String errorMsg = "Access denied: No IP whitelist configuration found. Please contact system administrator.";
                log.warn("IP whitelist validation failed - No active whitelist entries found. IP: {}, FullPath: {}, Endpoint: {}",
                        clientIp, fullRequestPath, endpointPath);
                throw new RuntimeException(errorMsg);
            }

            // Check if the client IP matches any whitelist entry
            boolean ipMatches = false;
            IPWhitelistEntryEntity matchingEntry = null;

            for (IPWhitelistEntryEntity entry : whitelistEntries) {
                if (isIpInRange(clientIp, entry.getIpRange())) {
                    ipMatches = true;
                    matchingEntry = entry;
                    log.info("IP {} matched whitelist entry: {} (Range: {})", clientIp, entry.getName(), entry.getIpRange());
                    break;
                }
            }

            // DENY if IP not found in any whitelist entry
            if (!ipMatches) {
                String errorMsg = String.format("Access denied: IP address %s is not whitelisted. Please contact system administrator for access.", clientIp);
                log.warn("IP whitelist validation failed - IP: {}, FullPath: {}, Endpoint: {}", clientIp, fullRequestPath, endpointPath);
                throw new RuntimeException(errorMsg);
            }

            // Check endpoint access using the FULL REQUEST PATH
            String allowedEndpoints = matchingEntry.getEndpoints();

            // Clean the endpoints string - remove brackets, quotes, and trim
            String cleanedEndpoints = "";
            if (allowedEndpoints != null) {
                cleanedEndpoints = allowedEndpoints.trim();
                // Remove square brackets if present
                if (cleanedEndpoints.startsWith("[") && cleanedEndpoints.endsWith("]")) {
                    cleanedEndpoints = cleanedEndpoints.substring(1, cleanedEndpoints.length() - 1);
                }
                // Remove quotes if present
                cleanedEndpoints = cleanedEndpoints.replace("\"", "");
            }

            // If endpoints is empty or null or contains "/**", they can access all endpoints
            if (cleanedEndpoints == null || cleanedEndpoints.trim().isEmpty() || "/**".equals(cleanedEndpoints.trim())) {
                log.info("IP {} has access to all endpoints (allowedEndpoints: {})", clientIp, cleanedEndpoints);
                return;
            }

            // Parse allowed endpoints (comma-separated)
            String[] allowedEndpointList = cleanedEndpoints.split(",");
            boolean endpointAllowed = false;

            for (String allowedEndpoint : allowedEndpointList) {
                // Clean each endpoint pattern - remove any remaining brackets or quotes
                String trimmedEndpoint = allowedEndpoint.trim();
                trimmedEndpoint = trimmedEndpoint.replace("[", "").replace("]", "").replace("\"", "");

                if (trimmedEndpoint.isEmpty()) {
                    continue;
                }

                // Check against BOTH full request path and endpoint path
                if (isEndpointMatch(fullRequestPath, trimmedEndpoint) || isEndpointMatch(endpointPath, trimmedEndpoint)) {
                    endpointAllowed = true;
                    log.info("IP {} granted access to path: {} (matched pattern: {})", clientIp, fullRequestPath, trimmedEndpoint);
                    break;
                }
            }

            // DENY if endpoint not in allowed list
            if (!endpointAllowed) {
                String errorMsg = String.format("Access denied: IP address %s does not have access to path '%s'. Allowed paths: %s",
                        clientIp, fullRequestPath, cleanedEndpoints);
                log.warn("IP endpoint access denied - IP: {}, FullPath: {}, Allowed: {}", clientIp, fullRequestPath, cleanedEndpoints);
                throw new RuntimeException(errorMsg);
            }

            log.info("IP whitelist validation passed - IP: {}, FullPath: {}", clientIp, fullRequestPath);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during IP whitelist validation: {}", e.getMessage(), e);
            // DENY on error to be safe
            throw new RuntimeException("Access validation failed. Please contact system administrator.");
        }
    }

    /**
     * Check if a path matches an allowed pattern
     * Supports:
     * - Exact matches: "/create-bio-data"
     * - Path prefix with /** : "/plx/api/gen/**" (matches any path under that prefix)
     * - Wildcard in the middle: "/plx/api/gen/create-bio-data"
     */
    private boolean isEndpointMatch(String actualPath, String allowedPattern) {
        if (actualPath == null || allowedPattern == null) {
            return false;
        }

        actualPath = actualPath.trim();
        String pattern = allowedPattern.trim();

        // Clean the pattern - remove any brackets or quotes that might have been stored
        pattern = pattern.replace("[", "").replace("]", "").replace("\"", "");

        // Handle /** which means allow all paths
        if ("/**".equals(pattern)) {
            return true;
        }

        // Handle empty pattern
        if (pattern.isEmpty()) {
            return true;
        }

        // Exact match
        if (actualPath.equals(pattern)) {
            return true;
        }

        // Path prefix match (if pattern ends with /**)
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            // Remove trailing slash if present for cleaner matching
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            // Check if actual path starts with the prefix
            if (prefix.isEmpty()) {
                return true;
            }
            // Special handling for paths with variable segments (like API IDs)
            if (actualPath.startsWith(prefix)) {
                log.debug("Path {} matches prefix pattern {}", actualPath, pattern);
                return true;
            }
        }

        // Simple wildcard match (single *)
        if (pattern.contains("*") && !pattern.contains("/**")) {
            // Convert to simple regex - escape regex special characters except *
            String regexPattern = pattern
                    .replace(".", "\\.")
                    .replace("?", "\\?")
                    .replace("+", "\\+")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace("{", "\\{")
                    .replace("}", "\\}")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("*", ".*");

            try {
                boolean matches = actualPath.matches(regexPattern);
                if (matches) {
                    log.debug("Path {} matches wildcard pattern {}", actualPath, pattern);
                }
                return matches;
            } catch (Exception e) {
                log.warn("Regex error for pattern {}: {}", pattern, e.getMessage());
                // Fall back to simple contains check
                return actualPath.contains(pattern.replace("*", ""));
            }
        }

        return false;
    }

    /**
     * Check if an IP address falls within a CIDR range, matches an exact IP, or matches a wildcard pattern
     * Supports:
     * - Exact IP: "192.168.1.100"
     * - CIDR notation: "192.168.1.0/24"
     * - Wildcard patterns: "192.168.1.*", "192.168.*.*", "192.*.*.*", "192.168.*"
     * - Range notation: "192.168.1.1-192.168.1.100"
     * @param ipAddress The client IP address to check
     * @param ipRange The IP range/pattern to check against
     * @return true if the IP matches the range/pattern, false otherwise
     */
    private boolean isIpInRange(String ipAddress, String ipRange) {
        if (ipAddress == null || ipRange == null) {
            return false;
        }

        ipAddress = ipAddress.trim();
        ipRange = ipRange.trim();

        // Check for exact IP match
        if (ipAddress.equals(ipRange)) {
            return true;
        }

        // Check for CIDR notation (e.g., "192.168.1.0/24")
        if (ipRange.contains("/")) {
            try {
                String[] parts = ipRange.split("/");
                String networkAddress = parts[0];
                int prefixLength = Integer.parseInt(parts[1]);

                // Convert IPs to integers for comparison
                int ipInt = ipToInt(ipAddress);
                int networkInt = ipToInt(networkAddress);

                // Calculate mask based on prefix length
                int mask = prefixLength == 0 ? 0 : (0xFFFFFFFF << (32 - prefixLength));

                // Check if IP is in the network range
                return (ipInt & mask) == (networkInt & mask);
            } catch (Exception e) {
                log.warn("Failed to parse CIDR range: {}", ipRange, e);
                return false;
            }
        }

        // Check for IP range notation (e.g., "192.168.1.1-192.168.1.100")
        if (ipRange.contains("-")) {
            try {
                String[] parts = ipRange.split("-");
                String startIp = parts[0].trim();
                String endIp = parts[1].trim();

                int startInt = ipToInt(startIp);
                int endInt = ipToInt(endIp);
                int ipInt = ipToInt(ipAddress);

                return ipInt >= startInt && ipInt <= endInt;
            } catch (Exception e) {
                log.warn("Failed to parse IP range: {}", ipRange, e);
                return false;
            }
        }

        // Check for wildcard pattern (e.g., "192.168.1.*", "192.168.*.*", "192.168.*")
        if (ipRange.contains("*")) {
            // Handle partial wildcard like "192.168.*" (meaning 192.168.x.x)
            String normalizedRange = ipRange;
            String[] rangeParts = normalizedRange.split("\\.");

            // If pattern has fewer than 4 parts, pad with *
            if (rangeParts.length < 4) {
                StringBuilder padded = new StringBuilder();
                for (int i = 0; i < rangeParts.length; i++) {
                    if (i > 0) padded.append(".");
                    padded.append(rangeParts[i]);
                }
                for (int i = rangeParts.length; i < 4; i++) {
                    padded.append(".*");
                }
                normalizedRange = padded.toString();
            }

            // Use simple wildcard matching
            return matchWildcardSimple(ipAddress, normalizedRange);
        }

        return false;
    }

    /**
     * Simple wildcard matching for IP addresses
     * Supports patterns like "192.168.1.*" and "192.168.*.*"
     */
    private boolean matchWildcardSimple(String ipAddress, String ipRange) {
        String[] ipParts = ipAddress.split("\\.");
        String[] rangeParts = ipRange.split("\\.");

        if (ipParts.length != 4 || rangeParts.length != 4) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (rangeParts[i].equals("*")) {
                continue; // Wildcard matches anything
            }
            if (!ipParts[i].equals(rangeParts[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert IP address string to integer for CIDR calculations
     */
    private int ipToInt(String ipAddress) {
        try {
            String[] parts = ipAddress.split("\\.");
            if (parts.length != 4) {
                return 0;
            }
            int result = 0;
            for (int i = 0; i < 4; i++) {
                result |= (Integer.parseInt(parts[i]) << (24 - (8 * i)));
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to convert IP to int: {}", ipAddress, e);
            return 0;
        }
    }

    /**
     * Get the actual client IP address from HttpServletRequest
     * This handles proxies and load balancers properly
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // If multiple IPs in X-Forwarded-For, take the first one (original client)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress != null ? ipAddress : "unknown";
    }
}