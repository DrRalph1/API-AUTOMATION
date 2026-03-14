package com.usg.apiAutomation.utils.apiEngine.generator;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.collections.*;
import com.usg.apiAutomation.repositories.postgres.collections.*;
import com.usg.apiAutomation.utils.apiEngine.GenUrlBuilderUtil;
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
public class CollectionsGeneratorUtil {

    private final CollectionRepository collectionsCollectionRepository;
    private final FolderRepository collectionsFolderRepository;
    private final RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;
    private final EnvironmentRepository collectionsEnvironmentRepository;
    private final EnvironmentVariableRepository environmentVariableRepository;
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
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity =
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
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity =
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
                createEnvironmentVariable(savedDevEnv, "baseUrl", "http://localhost:8080", "string", true, generatedApiId),
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
                createEnvironmentVariable(savedProdEnv, "baseUrl", "https://api.example.com/v1", "string", true, generatedApiId),
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

    private com.usg.apiAutomation.entities.postgres.collections.RequestEntity createRequest(
            GeneratedApiEntity api, String performedBy, CollectionEntity collection,
            FolderEntity folder, String generatedApiId) {

        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

        com.usg.apiAutomation.entities.postgres.collections.RequestEntity request =
                new com.usg.apiAutomation.entities.postgres.collections.RequestEntity();

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
        collectionsRequestRepository.save(request);
    }

    private void createHeaderEntities(GeneratedApiEntity api,
                                      com.usg.apiAutomation.entities.postgres.collections.RequestEntity request,
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
                                         com.usg.apiAutomation.entities.postgres.collections.RequestEntity request,
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
}