package com.usg.apiGeneration.utils.apiEngine.generator;

import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.*;
import com.usg.apiGeneration.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.apiGeneration.entities.postgres.collections.*;
import com.usg.apiGeneration.repositories.collections.*;
import com.usg.apiGeneration.utils.apiEngine.GenUrlBuilderUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionsGeneratorUtil {

    private final CollectionRepository collectionsCollectionRepository;
    private final FolderRepository collectionsFolderRepository;
    private final RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;
    private final EnvironmentRepository collectionsEnvironmentRepository;
    private final EnvironmentVariableRepository environmentVariableRepository;
    private final EntityManager entityManager;
    private final GenUrlBuilderUtil genUrlBuilder;

    /**
     * Generate collections and return a map containing both collectionId and requestId
     */
    @Transactional
    public Map<String, String> generateWithDetails(GeneratedApiEntity api, String performedBy,
                                                   GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Generating Collections for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo.getCollectionName());

            String generatedApiId = api.getId();

            // ============ COLLECTION ENTITY ============
            CollectionEntity collection = createOrUpdateCollection(api, performedBy, collectionInfo, generatedApiId);

            // ============ ENVIRONMENT ENTITIES ============
            createEnvironments(api, performedBy, collection, generatedApiId);

            // ============ FOLDER ENTITY ============
            FolderEntity folder = createOrUpdateFolder(api, performedBy, collectionInfo, collection, generatedApiId);

            // ============ REQUEST ENTITY ============
            com.usg.apiGeneration.entities.postgres.collections.RequestEntity requestEntity =
                    createRequest(api, performedBy, collection, folder, generatedApiId);

            // ============ AUTH CONFIG ENTITY ============
            createAuthConfig(api, requestEntity, generatedApiId);

            // ============ HEADER ENTITIES ============
            createHeaderEntities(api, requestEntity, generatedApiId);

            // ============ PARAMETER ENTITIES ============
            createParameterEntities(api, requestEntity, generatedApiId);

            // Update folder request count
            folder.setRequestCount(1);
            collectionsFolderRepository.save(folder);

            log.info("Collections generated successfully with Collection ID: {} and Request ID: {} using Folder: {}",
                    collection.getId(), requestEntity.getId(), collectionInfo.getFolderName());

            // Return both collection ID and request ID
            Map<String, String> result = new HashMap<>();
            result.put("collectionId", collection.getId());
            result.put("requestId", requestEntity.getId());  // This is the tb_col_requests ID
            return result;

        } catch (Exception e) {
            log.error("Error generating Collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Collections: " + e.getMessage(), e);
        }
    }


    @Transactional
    public String generate(GeneratedApiEntity api, String performedBy,
                           GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Generating Collections for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo.getCollectionName());

            String generatedApiId = api.getId();

            // ============ COLLECTION ENTITY ============
            CollectionEntity collection = createOrUpdateCollection(api, performedBy, collectionInfo, generatedApiId);

            // ============ ENVIRONMENT ENTITIES ============
            createEnvironments(api, performedBy, collection, generatedApiId);

            // ============ FOLDER ENTITY ============
            FolderEntity folder = createOrUpdateFolder(api, performedBy, collectionInfo, collection, generatedApiId);

            // ============ REQUEST ENTITY ============
            com.usg.apiGeneration.entities.postgres.collections.RequestEntity requestEntity =
                    createRequest(api, performedBy, collection, folder, generatedApiId);

            // ============ AUTH CONFIG ENTITY ============
            createAuthConfig(api, requestEntity, generatedApiId);

            // ============ HEADER ENTITIES ============
            createHeaderEntities(api, requestEntity, generatedApiId);

            // ============ PARAMETER ENTITIES ============
            createParameterEntities(api, requestEntity, generatedApiId);

            // Update folder request count
            folder.setRequestCount(1);
            collectionsFolderRepository.save(folder);

            log.info("Collections generated successfully with Collection ID: {} using Folder: {}",
                    collection.getId(), collectionInfo.getFolderName());

            return collection.getId();

        } catch (Exception e) {
            log.error("Error generating Collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Collections: " + e.getMessage(), e);
        }
    }

    // ==================== UPDATE METHODS FOR EXISTING REQUESTS ====================

    @Transactional
    public void updateExistingRequest(com.usg.apiGeneration.entities.postgres.collections.RequestEntity existingRequest,
                                      GeneratedApiEntity api,
                                      String performedBy,
                                      GenerateApiRequestDTO request,
                                      CollectionInfoDTO collectionInfo) {
        try {
            log.info("Updating existing collections request: {} for API: {}",
                    existingRequest.getId(), api.getApiCode());

            String generatedApiId = api.getId();
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

            // CRITICAL FIX: Use the helper methods to properly clear collections
            // This maintains bidirectional relationship integrity

            // Clear headers properly
            if (existingRequest.getHeaders() != null) {
                // Create a copy to avoid ConcurrentModificationException
                List<HeaderEntity> headersToRemove = new ArrayList<>(existingRequest.getHeaders());
                for (HeaderEntity header : headersToRemove) {
                    existingRequest.removeHeader(header);
                }
                // Now delete using repository
                if (!headersToRemove.isEmpty()) {
                    collectionsHeaderRepository.deleteAll(headersToRemove);
                    log.debug("Deleted {} headers for request: {}", headersToRemove.size(), existingRequest.getId());
                }
            }

            // Clear parameters properly
            if (existingRequest.getParams() != null) {
                List<ParameterEntity> paramsToRemove = new ArrayList<>(existingRequest.getParams());
                for (ParameterEntity param : paramsToRemove) {
                    existingRequest.removeParameter(param);
                }
                if (!paramsToRemove.isEmpty()) {
                    collectionsParameterRepository.deleteAll(paramsToRemove);
                    log.debug("Deleted {} parameters for request: {}", paramsToRemove.size(), existingRequest.getId());
                }
            }

            // Handle auth config
            if (existingRequest.getAuthConfig() != null) {
                AuthConfigEntity authConfig = existingRequest.getAuthConfig();
                existingRequest.setAuthConfig(null); // This breaks the relationship
                collectionsAuthConfigRepository.delete(authConfig);
            }

            // Flush deletions to ensure they're processed
            collectionsHeaderRepository.flush();
            collectionsParameterRepository.flush();
            collectionsAuthConfigRepository.flush();

            // Update request basic info
            existingRequest.setName(api.getApiName() + " - " + api.getHttpMethod());
            existingRequest.setMethod(api.getHttpMethod());
            existingRequest.setUrl(genUrlInfo.getFullUrl());
            existingRequest.setDescription(api.getDescription());
            existingRequest.setLastModified(LocalDateTime.now());
            existingRequest.setUpdatedAt(LocalDateTime.now());

            // Set request body
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                existingRequest.setBody(api.getRequestConfig().getSample());
            }

            // Set auth type
            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                existingRequest.setAuthType(api.getAuthConfig().getAuthType().toLowerCase());
            }

            // Save the request first
            existingRequest = collectionsRequestRepository.save(existingRequest);

            // Create and add new headers using helper method
            List<HeaderEntity> newHeaders = createHeadersForRequest(api, existingRequest, generatedApiId);
            for (HeaderEntity header : newHeaders) {
                existingRequest.addHeader(header);
            }
            if (!newHeaders.isEmpty()) {
                collectionsHeaderRepository.saveAll(newHeaders);
            }

            // Create and add new parameters using helper method
            List<ParameterEntity> newParams = createParametersForRequest(api, existingRequest, generatedApiId);
            for (ParameterEntity param : newParams) {
                existingRequest.addParameter(param);
            }
            if (!newParams.isEmpty()) {
                collectionsParameterRepository.saveAll(newParams);
            }

            // Create new auth config
            createAuthConfig(api, existingRequest, generatedApiId);

            // Final save
            collectionsRequestRepository.save(existingRequest);

            log.info("Successfully updated collections request: {}", existingRequest.getId());

        } catch (Exception e) {
            log.error("Error updating existing collections request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update existing collections request: " + e.getMessage(), e);
        }
    }

    /**
     * Delete all collections from a request without using orphanRemoval
     */
    private void deleteRequestCollections(com.usg.apiGeneration.entities.postgres.collections.RequestEntity request) {
        if (request != null) {
            log.debug("Deleting collections for request: {}", request.getId());

            // CRITICAL: First, clear the collections to break the relationship
            // This prevents Hibernate from trying to cascade operations
            if (request.getHeaders() != null) {
                List<HeaderEntity> headersToDelete = new ArrayList<>(request.getHeaders());
                if (!headersToDelete.isEmpty()) {
                    // First clear the collection
                    request.getHeaders().clear();
                    // Then delete using repository
                    collectionsHeaderRepository.deleteAll(headersToDelete);
                    log.debug("Deleted {} headers for request: {}", headersToDelete.size(), request.getId());
                }
            }

            if (request.getParams() != null) {
                List<ParameterEntity> paramsToDelete = new ArrayList<>(request.getParams());
                if (!paramsToDelete.isEmpty()) {
                    // First clear the collection
                    request.getParams().clear();
                    // Then delete using repository
                    collectionsParameterRepository.deleteAll(paramsToDelete);
                    log.debug("Deleted {} parameters for request: {}", paramsToDelete.size(), request.getId());
                }
            }

            log.debug("Successfully deleted collections for request: {}", request.getId());
        }
    }

    /**
     * Create headers for a request
     */
    private List<HeaderEntity> createHeadersForRequest(GeneratedApiEntity api,
                                                       com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
                                                       String generatedApiId) {
        List<HeaderEntity> newHeaders = new ArrayList<>();

        // Add headers from headers array
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
                    // DON'T set request here - let the helper method do it
                    newHeaders.add(header);
                }
            }
        }

        // Add header parameters
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
                    // DON'T set request here - let the helper method do it
                    newHeaders.add(header);
                }
            }
        }

        return newHeaders;
    }

    /**
     * Create parameters for a request
     */
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

        if (!newParams.isEmpty()) {
            collectionsParameterRepository.saveAll(newParams);
        }

        return newParams;
    }

    /**
     * Helper method to update an existing collection
     */
    public void updateExistingCollection(CollectionEntity existingCollection,
                                         GeneratedApiEntity api,
                                         CollectionInfoDTO collectionInfo) {
        try {
            log.info("Updating existing collection: {} for API: {}",
                    existingCollection.getId(), api.getApiCode());

            boolean needsUpdate = false;

            if (collectionInfo != null && !collectionInfo.getCollectionName().equals(existingCollection.getName())) {
                existingCollection.setName(collectionInfo.getCollectionName());
                needsUpdate = true;
            }

            if (needsUpdate) {
                existingCollection.setLastActivity(LocalDateTime.now());
                existingCollection.setUpdatedAt(LocalDateTime.now());
                collectionsCollectionRepository.save(existingCollection);
                log.info("Updated collection metadata: {}", existingCollection.getId());
            }

        } catch (Exception e) {
            log.error("Error updating existing collection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update existing collection: " + e.getMessage(), e);
        }
    }

    // ==================== PRIVATE HELPER METHODS FOR UPDATES ====================


    /**
     * Update auth config for an existing request - COMPLETELY REPLACES old config
     */
    private void updateAuthConfig(com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
                                  GeneratedApiEntity api, String generatedApiId) {
        try {
            // CRITICAL: Delete existing auth config if present
            if (request.getAuthConfig() != null) {
                AuthConfigEntity oldAuthConfig = request.getAuthConfig();
                log.info("Removing existing auth config: type={}, id={}",
                        oldAuthConfig.getType(), oldAuthConfig.getId());

                // First, break the relationship
                request.setAuthConfig(null);
                collectionsRequestRepository.save(request);

                // Force flush to ensure relationship is broken
                collectionsRequestRepository.flush();

                // Now delete the old auth config
                collectionsAuthConfigRepository.delete(oldAuthConfig);
                collectionsAuthConfigRepository.flush();

                log.info("Successfully deleted old auth config");
            }

            // Create new auth config if needed
            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                AuthConfigEntity authConfig = new AuthConfigEntity();
                authConfig.setId(UUID.randomUUID().toString());
                authConfig.setGeneratedApiId(generatedApiId);
                authConfig.setRequest(request);

                // Set fields based on auth type - CLEAR ALL FIELDS FIRST
                String authType = api.getAuthConfig().getAuthType();
                authConfig.setType(authType);

                // Clear all fields initially
                authConfig.setKey(null);
                authConfig.setValue(null);
                authConfig.setToken(null);
                authConfig.setTokenType(null);
                authConfig.setUsername(null);
                authConfig.setPassword(null);
                authConfig.setAddTo("header"); // Default to header

                switch (authType) {
                    case "API_KEY":
                        authConfig.setKey(api.getAuthConfig().getApiKeyHeader() != null ?
                                api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                        authConfig.setValue("{{apiKey}}");
                        // Clear any JWT/Basic specific fields
                        authConfig.setToken(null);
                        authConfig.setTokenType(null);
                        authConfig.setUsername(null);
                        authConfig.setPassword(null);
                        break;

                    case "BEARER":
                    case "JWT":
                        authConfig.setType("bearer");
                        authConfig.setToken("{{jwtToken}}");
                        authConfig.setTokenType("Bearer");
                        // Clear API Key and Basic specific fields
                        authConfig.setKey(null);
                        authConfig.setValue(null);
                        authConfig.setUsername(null);
                        authConfig.setPassword(null);
                        break;

                    case "BASIC":
                        authConfig.setUsername("{{username}}");
                        authConfig.setPassword("{{password}}");
                        // Clear other auth fields
                        authConfig.setKey(null);
                        authConfig.setValue(null);
                        authConfig.setToken(null);
                        authConfig.setTokenType(null);
                        break;

                    case "OAUTH2":
                        authConfig.setType("oauth2");
                        authConfig.setKey("client_id");
                        authConfig.setValue("{{clientId}}");
                        authConfig.setToken("{{accessToken}}");
                        authConfig.setTokenType("Bearer");
                        // Clear Basic auth fields
                        authConfig.setUsername(null);
                        authConfig.setPassword(null);
                        break;

                    case "ORACLE_ROLES":
                        authConfig.setKey("X-Oracle-Session");
                        authConfig.setValue("{{oracleSessionId}}");
                        // Clear other auth fields
                        authConfig.setToken(null);
                        authConfig.setTokenType(null);
                        authConfig.setUsername(null);
                        authConfig.setPassword(null);
                        break;

                    default:
                        log.warn("Unknown auth type: {}", authType);
                        return;
                }

                // Save the new auth config
                collectionsAuthConfigRepository.save(authConfig);
                request.setAuthConfig(authConfig);
                collectionsRequestRepository.save(request);

                log.info("Successfully created new auth config: type={}, key={}, hasToken={}",
                        authConfig.getType(),
                        authConfig.getKey() != null,
                        authConfig.getToken() != null);
            } else {
                // If no auth config or auth type is NONE, ensure no auth config is attached
                log.info("No auth config needed for API: {}", api.getApiCode());
            }

        } catch (Exception e) {
            log.error("Error updating auth config: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update auth config: " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING PRIVATE METHODS ====================

    private CollectionEntity createOrUpdateCollection(GeneratedApiEntity api, String performedBy,
                                                      CollectionInfoDTO collectionInfo, String generatedApiId) {
        Optional<CollectionEntity> existing = collectionsCollectionRepository
                .findById(collectionInfo.getCollectionId());

        if (existing.isPresent()) {
            CollectionEntity collection = existing.get();
            boolean needsUpdate = false;

            if (!collectionInfo.getCollectionName().equals(collection.getName())) {
                collection.setName(collectionInfo.getCollectionName());
                needsUpdate = true;
            }
            if (collection.getGeneratedApiId() == null) {
                collection.setGeneratedApiId(generatedApiId);
                needsUpdate = true;
            }
            if (needsUpdate) {
                collection.setLastActivity(LocalDateTime.now());
                collection.setUpdatedAt(LocalDateTime.now());
                return collectionsCollectionRepository.save(collection);
            }
            return collection;
        } else {
            CollectionEntity collection = new CollectionEntity();
            collection.setId(collectionInfo.getCollectionId());
            collection.setGeneratedApiId(generatedApiId);
            collection.setName(collectionInfo.getCollectionName());
            collection.setDescription(api.getDescription() != null ? api.getDescription() :
                    "Collection for " + collectionInfo.getCollectionName());
            collection.setOwner(performedBy);
            collection.setExpanded(false);
            collection.setEditing(false);
            collection.setFavorite(false);
            collection.setLastActivity(LocalDateTime.now());
            collection.setColor(getRandomColor());
            collection.setCreatedAt(LocalDateTime.now());
            collection.setUpdatedAt(LocalDateTime.now());

            if (collectionInfo.getCollectionType() != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("collectionType", collectionInfo.getCollectionType());
                metadata.put("apiId", api.getId());
                metadata.put("apiCode", api.getApiCode());
                collection.setMetadata(metadata);
            }

            return collectionsCollectionRepository.save(collection);
        }
    }

    private void createEnvironments(GeneratedApiEntity api, String performedBy,
                                    CollectionEntity collection, String generatedApiId) {
        // Create Development Environment
        EnvironmentEntity devEnv = new EnvironmentEntity();
        devEnv.setId(UUID.randomUUID().toString());
        devEnv.setGeneratedApiId(generatedApiId);
        devEnv.setName("Development");
        devEnv.setActive(true);
        devEnv.setOwner(performedBy);
        devEnv.setCreatedAt(LocalDateTime.now());
        devEnv.setUpdatedAt(LocalDateTime.now());

        EnvironmentEntity savedDevEnv = collectionsEnvironmentRepository.save(devEnv);

        // Create variables for dev environment
        List<EnvironmentVariableEntity> devVariables = Arrays.asList(
                createEnvironmentVariable(savedDevEnv, "baseUrl", "http://10.203.14.33:8182/apiGeneration", "string", true, generatedApiId),
                createEnvironmentVariable(savedDevEnv, "apiKey", "dev-api-key-123", "string", true, generatedApiId),
                createEnvironmentVariable(savedDevEnv, "timeout", "30000", "number", true, generatedApiId)
        );
        environmentVariableRepository.saveAll(devVariables);

        // Create Production Environment
        EnvironmentEntity prodEnv = new EnvironmentEntity();
        prodEnv.setId(UUID.randomUUID().toString());
        prodEnv.setGeneratedApiId(generatedApiId);
        prodEnv.setName("Production");
        prodEnv.setActive(false);
        prodEnv.setOwner(performedBy);
        prodEnv.setCreatedAt(LocalDateTime.now());
        prodEnv.setUpdatedAt(LocalDateTime.now());

        EnvironmentEntity savedProdEnv = collectionsEnvironmentRepository.save(prodEnv);

        // Create variables for prod environment
        List<EnvironmentVariableEntity> prodVariables = Arrays.asList(
                createEnvironmentVariable(savedProdEnv, "baseUrl", "{{baseUrl}}", "string", true, generatedApiId),
                createEnvironmentVariable(savedProdEnv, "apiKey", "{{prodApiKey}}", "string", true, generatedApiId),
                createEnvironmentVariable(savedProdEnv, "timeout", "10000", "number", true, generatedApiId)
        );
        environmentVariableRepository.saveAll(prodVariables);

        log.debug("Created environments for collection: {}", collection.getId());
    }

    private EnvironmentVariableEntity createEnvironmentVariable(EnvironmentEntity environment, String key,
                                                                String value, String type, boolean enabled,
                                                                String generatedApiId) {
        EnvironmentVariableEntity variable = new EnvironmentVariableEntity();
        variable.setId(UUID.randomUUID().toString());
        variable.setGeneratedApiId(generatedApiId);
        variable.setKey(key);
        variable.setValue(value);
        variable.setType(type);
        variable.setEnabled(enabled);
        variable.setEnvironment(environment);
        return variable;
    }

    private FolderEntity createOrUpdateFolder(GeneratedApiEntity api, String performedBy,
                                              CollectionInfoDTO collectionInfo, CollectionEntity collection,
                                              String generatedApiId) {
        Optional<FolderEntity> existing = collectionsFolderRepository
                .findById(collectionInfo.getFolderId());

        if (existing.isPresent()) {
            FolderEntity folder = existing.get();
            boolean needsUpdate = false;

            if (!collectionInfo.getFolderName().equals(folder.getName())) {
                folder.setName(collectionInfo.getFolderName());
                needsUpdate = true;
            }
            if (folder.getGeneratedApiId() == null) {
                folder.setGeneratedApiId(generatedApiId);
                needsUpdate = true;
            }
            if (needsUpdate) {
                folder.setUpdatedAt(LocalDateTime.now());
                return collectionsFolderRepository.save(folder);
            }
            return folder;
        } else {
            FolderEntity folder = new FolderEntity();
            folder.setId(collectionInfo.getFolderId());
            folder.setGeneratedApiId(generatedApiId);
            folder.setName(collectionInfo.getFolderName());
            folder.setDescription("Folder for " + collectionInfo.getFolderName());
            folder.setExpanded(false);
            folder.setEditing(false);
            folder.setRequestCount(0);
            folder.setCollection(collection);
            folder.setCreatedAt(LocalDateTime.now());
            folder.setUpdatedAt(LocalDateTime.now());
            return collectionsFolderRepository.save(folder);
        }
    }

    private com.usg.apiGeneration.entities.postgres.collections.RequestEntity createRequest(
            GeneratedApiEntity api, String performedBy, CollectionEntity collection,
            FolderEntity folder, String generatedApiId) {

        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

        com.usg.apiGeneration.entities.postgres.collections.RequestEntity request =
                new com.usg.apiGeneration.entities.postgres.collections.RequestEntity();

        request.setId(UUID.randomUUID().toString());
        request.setGeneratedApiId(generatedApiId);
        request.setName(api.getApiName() + " - " + api.getHttpMethod());
        request.setMethod(api.getHttpMethod());
        request.setUrl(genUrlInfo.getFullUrl());
        request.setDescription(api.getDescription());
        request.setLastModified(LocalDateTime.now());
        request.setSaved(true);
        request.setCollection(collection);
        request.setFolder(folder);
        request.setHeaders(new ArrayList<>());
        request.setParams(new ArrayList<>());
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        // Set request body
        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            request.setBody(api.getRequestConfig().getSample());
        }

        // Set auth type
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            request.setAuthType(api.getAuthConfig().getAuthType().toLowerCase());
        }

        return collectionsRequestRepository.save(request);
    }

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
        collectionsRequestRepository.save(request);
    }

    private void createHeaderEntities(GeneratedApiEntity api,
                                      com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
                                      String generatedApiId) {
        // Add headers from headers array
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

                    collectionsHeaderRepository.save(header);
                    request.getHeaders().add(header);
                }
            }
        }

        // Add header parameters
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

                    collectionsHeaderRepository.save(header);
                    request.getHeaders().add(header);
                }
            }
        }

        if (!request.getHeaders().isEmpty()) {
            collectionsRequestRepository.save(request);
        }
    }

    private void createParameterEntities(GeneratedApiEntity api,
                                         com.usg.apiGeneration.entities.postgres.collections.RequestEntity request,
                                         String generatedApiId) {
        if (api.getParameters() == null || api.getParameters().isEmpty()) {
            return;
        }

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

            collectionsParameterRepository.save(param);
            request.getParams().add(param);
        }

        collectionsRequestRepository.save(request);
    }

    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }



    @Transactional
    public void updateExistingRequestWithNewParent(
            com.usg.apiGeneration.entities.postgres.collections.RequestEntity existingRequest,
            GeneratedApiEntity api,
            String performedBy,
            GenerateApiRequestDTO request,
            CollectionInfoDTO collectionInfo,
            CollectionEntity newCollection,
            FolderEntity newFolder) {

        try {
            log.info("Updating existing collections request with new parent structure: {} -> {}",
                    existingRequest.getId(), newCollection.getId());

            // Clear existing relationships
            deleteRequestCollections(existingRequest);

            // Update request with new parent
            existingRequest.setCollection(newCollection);
            existingRequest.setFolder(newFolder);

            // Update content
            updateRequestContent(existingRequest, api, performedBy, request);

            // Save the request
            collectionsRequestRepository.save(existingRequest);

            log.info("Successfully updated request with new parent structure");

        } catch (Exception e) {
            log.error("Error updating request with new parent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update request with new parent", e);
        }
    }



    /**
     * Update the content of an existing request (without changing parent structure)
     */
    /**
     * Update the content of an existing request (without changing parent structure)
     */
    @Transactional
    public void updateRequestContent(
            com.usg.apiGeneration.entities.postgres.collections.RequestEntity existingRequest,
            GeneratedApiEntity api,
            String performedBy,
            GenerateApiRequestDTO request) {

        try {
            log.info("Updating request content for: {}", existingRequest.getId());

            String generatedApiId = api.getId();
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

            // Clear existing relationships properly
            clearRequestRelationships(existingRequest);

            // Update basic info
            existingRequest.setName(api.getApiName() + " - " + api.getHttpMethod());
            existingRequest.setMethod(api.getHttpMethod());
            existingRequest.setUrl(genUrlInfo.getFullUrl());
            existingRequest.setDescription(api.getDescription());
            existingRequest.setLastModified(LocalDateTime.now());
            existingRequest.setUpdatedAt(LocalDateTime.now());

            // Set request body
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                existingRequest.setBody(api.getRequestConfig().getSample());
            }

            // Set auth type
            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                existingRequest.setAuthType(api.getAuthConfig().getAuthType().toLowerCase());
            } else {
                existingRequest.setAuthType(null);
            }

            // Save the request first
            existingRequest = collectionsRequestRepository.save(existingRequest);

            // Create and add new headers
            List<HeaderEntity> newHeaders = createHeadersForRequest(api, existingRequest, generatedApiId);
            for (HeaderEntity header : newHeaders) {
                existingRequest.addHeader(header);
            }
            if (!newHeaders.isEmpty()) {
                collectionsHeaderRepository.saveAll(newHeaders);
            }

            // Create and add new parameters
            List<ParameterEntity> newParams = createParametersForRequest(api, existingRequest, generatedApiId);
            for (ParameterEntity param : newParams) {
                existingRequest.addParameter(param);
            }
            if (!newParams.isEmpty()) {
                collectionsParameterRepository.saveAll(newParams);
            }

            // Create new auth config (old one was already deleted in clearRequestRelationships)
            // Now update auth config with complete replacement logic
            updateAuthConfig(existingRequest, api, generatedApiId);

            // Final save
            collectionsRequestRepository.save(existingRequest);

            log.info("Successfully updated request content for: {}", existingRequest.getId());

        } catch (Exception e) {
            log.error("Error updating request content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update request content", e);
        }
    }



    /**
     * Clear all relationships from a request
     */
    private void clearRequestRelationships(com.usg.apiGeneration.entities.postgres.collections.RequestEntity request) {
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
            log.info("Clearing existing auth config: type={}, id={}",
                    authConfig.getType(), authConfig.getId());

            // First, break the relationship
            request.setAuthConfig(null);
            collectionsRequestRepository.save(request);

            // Force flush to ensure relationship is broken
            collectionsRequestRepository.flush();

            // Now delete the old auth config
            collectionsAuthConfigRepository.delete(authConfig);
            log.debug("Deleted auth config for request: {}", request.getId());
        }

        // Flush to ensure deletions are processed
        collectionsHeaderRepository.flush();
        collectionsParameterRepository.flush();
        collectionsAuthConfigRepository.flush();
    }


    /**
     * Create auth config for a request
     */
    private void createAuthConfigForRequest(GeneratedApiEntity api,
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

    /**
     * Update folder request counts (fixed version without int vs null issue)
     */
    public void updateFolderRequestCounts(String oldFolderId, String newFolderId) {
        try {
            // Decrement count for old folder if it exists and is different
            if (oldFolderId != null && !oldFolderId.equals(newFolderId)) {
                collectionsFolderRepository.findById(oldFolderId).ifPresent(oldFolder -> {
                    Integer currentCount = oldFolder.getRequestCount();
                    int newCount = (currentCount != null ? currentCount : 0) - 1;
                    oldFolder.setRequestCount(Math.max(0, newCount));
                    collectionsFolderRepository.save(oldFolder);
                    log.debug("Decremented request count for old folder: {} to {}",
                            oldFolderId, oldFolder.getRequestCount());
                });
            }

            // Increment count for new folder if it exists and is different
            if (newFolderId != null && !newFolderId.equals(oldFolderId)) {
                collectionsFolderRepository.findById(newFolderId).ifPresent(newFolder -> {
                    Integer currentCount = newFolder.getRequestCount();
                    int newCount = (currentCount != null ? currentCount : 0) + 1;
                    newFolder.setRequestCount(newCount);
                    collectionsFolderRepository.save(newFolder);
                    log.debug("Incremented request count for new folder: {} to {}",
                            newFolderId, newFolder.getRequestCount());
                });
            }
        } catch (Exception e) {
            log.warn("Failed to update folder request counts: {}", e.getMessage());
        }
    }


}