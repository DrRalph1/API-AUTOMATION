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

    private final com.usg.apiAutomation.repositories.postgres.collections.CollectionRepository collectionsCollectionRepository;
    private final com.usg.apiAutomation.repositories.postgres.collections.FolderRepository collectionsFolderRepository;
    private final com.usg.apiAutomation.repositories.postgres.collections.RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;
    private final GenUrlBuilderUtil genUrlBuilder;

    @Transactional
    public String generate(GeneratedApiEntity api, String performedBy,
                           GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Generating Collections for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo.getCollectionName());

            CollectionEntity collection;
            Optional<CollectionEntity> existingCollection = collectionsCollectionRepository
                    .findById(collectionInfo.getCollectionId());

            if (existingCollection.isPresent()) {
                collection = existingCollection.get();
                log.info("Found existing collection: {}", collection.getId());

                boolean needsUpdate = false;

                if (!collectionInfo.getCollectionName().equals(collection.getName())) {
                    collection.setName(collectionInfo.getCollectionName());
                    needsUpdate = true;
                }

                String newDescription = api.getDescription() != null ? api.getDescription() :
                        "Collection for " + collectionInfo.getCollectionName();
                if (!newDescription.equals(collection.getDescription())) {
                    collection.setDescription(newDescription);
                    needsUpdate = true;
                }

                collection.setLastActivity(LocalDateTime.now());
                needsUpdate = true;

                if (collection.getColor() == null) {
                    collection.setColor(getRandomColor());
                    needsUpdate = true;
                }

                if (collectionInfo.getCollectionType() != null) {
                    Map<String, Object> metadata = collection.getMetadata() != null ?
                            collection.getMetadata() : new HashMap<>();
                    if (!collectionInfo.getCollectionType().equals(metadata.get("collectionType"))) {
                        metadata.put("collectionType", collectionInfo.getCollectionType());
                        collection.setMetadata(metadata);
                        needsUpdate = true;
                    }
                }

                if (needsUpdate) {
                    collection.setUpdatedAt(LocalDateTime.now());
                    collection = collectionsCollectionRepository.save(collection);
                    log.debug("Updated collection: {}", collection.getId());
                }
            } else {
                collection = new CollectionEntity();
                collection.setId(collectionInfo.getCollectionId());
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
                    collection.setMetadata(metadata);
                }

                log.info("Created new collection: {}", collection.getId());
            }

            if (collection.getVariables() == null || collection.getVariables().isEmpty()) {
                List<VariableEntity> variables = new ArrayList<>();

                if (api.getAuthConfig() != null) {
                    if (api.getAuthConfig().getApiKeyValue() != null) {
                        VariableEntity apiKeyVar = new VariableEntity();
                        apiKeyVar.setId(UUID.randomUUID().toString());
                        apiKeyVar.setKey("apiKey");
                        apiKeyVar.setValue(api.getAuthConfig().getApiKeyValue());
                        apiKeyVar.setType("string");
                        apiKeyVar.setEnabled(true);
                        variables.add(apiKeyVar);
                    }
                    if (api.getAuthConfig().getApiSecretValue() != null) {
                        VariableEntity apiSecretVar = new VariableEntity();
                        apiSecretVar.setId(UUID.randomUUID().toString());
                        apiSecretVar.setKey("apiSecret");
                        apiSecretVar.setValue(api.getAuthConfig().getApiSecretValue());
                        apiSecretVar.setType("string");
                        apiSecretVar.setEnabled(true);
                        variables.add(apiSecretVar);
                    }
                }

                VariableEntity baseUrlVar = new VariableEntity();
                baseUrlVar.setId(UUID.randomUUID().toString());
                baseUrlVar.setKey("baseUrl");
                baseUrlVar.setValue("");
                baseUrlVar.setType("string");
                baseUrlVar.setEnabled(true);
                variables.add(baseUrlVar);

                // Add API ID as a variable
                VariableEntity apiIdVar = new VariableEntity();
                apiIdVar.setId(UUID.randomUUID().toString());
                apiIdVar.setKey("apiId");
                apiIdVar.setValue(api.getId());
                apiIdVar.setType("string");
                apiIdVar.setEnabled(true);
                variables.add(apiIdVar);

                // Add variables for path parameters
                if (api.getParameters() != null) {
                    for (ApiParameterEntity param : api.getParameters()) {
                        if ("path".equals(param.getParameterType())) {
                            VariableEntity pathVar = new VariableEntity();
                            pathVar.setId(UUID.randomUUID().toString());
                            pathVar.setKey(param.getKey());
                            pathVar.setValue(param.getExample() != null ? param.getExample() : "");
                            pathVar.setType("string");
                            pathVar.setEnabled(true);
                            variables.add(pathVar);
                        }
                    }
                }

                for (VariableEntity var : variables) {
                    var.setCollection(collection);
                }

                collection.setVariables(variables);
            }

            CollectionEntity savedCollection = collectionsCollectionRepository.save(collection);
            collectionsCollectionRepository.flush();

            FolderEntity folder;
            Optional<FolderEntity> existingFolder = collectionsFolderRepository
                    .findById(collectionInfo.getFolderId());

            if (existingFolder.isPresent()) {
                folder = existingFolder.get();
                log.info("Found existing folder: {}", folder.getId());

                boolean folderNeedsUpdate = false;

                if (!collectionInfo.getFolderName().equals(folder.getName())) {
                    folder.setName(collectionInfo.getFolderName());
                    folderNeedsUpdate = true;
                }

                String folderDescription = "Folder for " + collectionInfo.getFolderName();
                if (!folderDescription.equals(folder.getDescription())) {
                    folder.setDescription(folderDescription);
                    folderNeedsUpdate = true;
                }

                if (folderNeedsUpdate) {
                    folder.setUpdatedAt(LocalDateTime.now());
                    folder = collectionsFolderRepository.save(folder);
                    log.debug("Updated folder: {}", folder.getId());
                }
            } else {
                folder = new FolderEntity();
                folder.setId(collectionInfo.getFolderId());
                folder.setName(collectionInfo.getFolderName());
                folder.setDescription("Folder for " + collectionInfo.getFolderName());
                folder.setExpanded(false);
                folder.setEditing(false);
                folder.setRequestCount(0);
                folder.setCollection(savedCollection);
                folder.setCreatedAt(LocalDateTime.now());
                folder.setUpdatedAt(LocalDateTime.now());

                log.info("Created new folder: {}", folder.getId());
            }

            FolderEntity savedFolder = collectionsFolderRepository.save(folder);
            collectionsFolderRepository.flush();

            // Use the full URL from GenUrlBuilderUtil
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);
            String endpointUrl = genUrlInfo.getFullUrl();
            log.info("Built endpoint URL for collections: {}", endpointUrl);

            String requestName = api.getApiName() + " - " + api.getHttpMethod();

            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity =
                    new com.usg.apiAutomation.entities.postgres.collections.RequestEntity();

            requestEntity.setId(UUID.randomUUID().toString());
            requestEntity.setName(requestName);
            requestEntity.setMethod(api.getHttpMethod());
            requestEntity.setUrl(endpointUrl);  // Use gen URL from centralized builder
            requestEntity.setDescription(api.getDescription());
            requestEntity.setLastModified(LocalDateTime.now());
            requestEntity.setSaved(true);
            requestEntity.setCollection(savedCollection);
            requestEntity.setFolder(savedFolder);

            requestEntity.setHeaders(new ArrayList<>());
            requestEntity.setParams(new ArrayList<>());
            requestEntity.setAuthConfig(null);

            requestEntity.setCreatedAt(LocalDateTime.now());
            requestEntity.setUpdatedAt(LocalDateTime.now());

            log.info("Creating new request in folder: {} with name: {}", savedFolder.getName(), requestName);

            com.usg.apiAutomation.entities.postgres.collections.RequestEntity savedRequest =
                    collectionsRequestRepository.save(requestEntity);
            collectionsRequestRepository.flush();

            log.info("Saved new request entity with ID: {}", savedRequest.getId());

            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                AuthConfigEntity authConfig = new AuthConfigEntity();
                authConfig.setId(UUID.randomUUID().toString());
                authConfig.setRequest(savedRequest);
                authConfig.setType(api.getAuthConfig().getAuthType());

                switch (api.getAuthConfig().getAuthType()) {
                    case "API_KEY":
                        authConfig.setKey(api.getAuthConfig().getApiKeyHeader() != null ?
                                api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                        authConfig.setValue(api.getAuthConfig().getApiKeyValue() != null ?
                                api.getAuthConfig().getApiKeyValue() : "{{apiKey}}");
                        authConfig.setAddTo("header");
                        break;
                    case "BEARER":
                    case "JWT":
                        authConfig.setType("bearer");
                        authConfig.setToken("{{jwtToken}}");
                        break;
                    case "BASIC":
                        authConfig.setUsername("{{username}}");
                        authConfig.setPassword("{{password}}");
                        break;
                    case "ORACLE_ROLES":
                        authConfig.setType("oracle-roles");
                        authConfig.setKey("X-Oracle-Session");
                        authConfig.setValue("{{oracleSessionId}}");
                        authConfig.setAddTo("header");
                        break;
                }

                collectionsAuthConfigRepository.save(authConfig);

                savedRequest.setAuthConfig(authConfig);
                savedRequest = collectionsRequestRepository.save(savedRequest);

                log.debug("Added auth config to request");
            }

            // Add header parameters
            if (api.getParameters() != null) {
                for (ApiParameterEntity apiParam : api.getParameters()) {
                    if ("header".equals(apiParam.getParameterType())) {
                        HeaderEntity header = new HeaderEntity();
                        header.setId(UUID.randomUUID().toString());
                        header.setKey(apiParam.getKey() != null ? apiParam.getKey() : "");
                        header.setValue(apiParam.getExample() != null ? apiParam.getExample() : "{{" + apiParam.getKey() + "}}");
                        header.setDescription(apiParam.getDescription() != null ? apiParam.getDescription() : "");
                        header.setEnabled(apiParam.getRequired() != null ? apiParam.getRequired() : true);
                        header.setRequest(savedRequest);

                        collectionsHeaderRepository.save(header);

                        savedRequest.getHeaders().add(header);
                    }
                }
            }

            // Add headers from headers array
            if (api.getHeaders() != null) {
                for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                    if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                        HeaderEntity header = new HeaderEntity();
                        header.setId(UUID.randomUUID().toString());
                        header.setKey(apiHeader.getKey() != null ? apiHeader.getKey() : "");
                        header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                        header.setDescription(apiHeader.getDescription() != null ? apiHeader.getDescription() : "");
                        header.setEnabled(apiHeader.getRequired() != null ? apiHeader.getRequired() : true);
                        header.setRequest(savedRequest);

                        collectionsHeaderRepository.save(header);

                        savedRequest.getHeaders().add(header);
                    }
                }
                savedRequest = collectionsRequestRepository.save(savedRequest);
                log.debug("Added {} headers to request", savedRequest.getHeaders().size());
            }

            // Add all parameters (including path, query, body)
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                for (ApiParameterEntity apiParam : api.getParameters()) {
                    ParameterEntity param = new ParameterEntity();

                    param.setId(UUID.randomUUID().toString());
                    param.setKey(apiParam.getKey() != null ? apiParam.getKey() : "");
                    param.setValue(apiParam.getExample() != null ? apiParam.getExample() : "");
                    param.setDescription(apiParam.getDescription() != null ? apiParam.getDescription() : "");
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

                    param.setRequest(savedRequest);

                    collectionsParameterRepository.save(param);

                    savedRequest.getParams().add(param);
                }
                savedRequest = collectionsRequestRepository.save(savedRequest);
                log.debug("Added {} parameters to request", savedRequest.getParams().size());
            }

            // Set request body from body parameters
            if (api.getParameters() != null) {
                List<ApiParameterEntity> bodyParams = api.getParameters().stream()
                        .filter(p -> "body".equals(p.getParameterType()))
                        .collect(Collectors.toList());

                if (!bodyParams.isEmpty()) {
                    try {
                        Map<String, Object> bodyMap = new HashMap<>();
                        for (ApiParameterEntity param : bodyParams) {
                            bodyMap.put(param.getKey(), param.getExample() != null ?
                                    param.getExample() : "{{" + param.getKey() + "}}");
                        }
                        savedRequest.setBody(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bodyMap));
                    } catch (Exception e) {
                        log.warn("Failed to create body from parameters: {}", e.getMessage());
                    }
                } else if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                    savedRequest.setBody(api.getRequestConfig().getSample());
                }
                savedRequest = collectionsRequestRepository.save(savedRequest);
            }

            collectionsRequestRepository.flush();

            long requestCount = collectionsRequestRepository.countByFolderId(savedFolder.getId());
            savedFolder.setRequestCount((int) requestCount);
            collectionsFolderRepository.save(savedFolder);
            collectionsFolderRepository.flush();

            log.info("Collections generated successfully with Collection ID: {} using Folder: {}",
                    savedCollection.getId(), collectionInfo.getFolderName());

            return savedCollection.getId();

        } catch (Exception e) {
            log.error("Error generating Collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Collections: " + e.getMessage(), e);
        }
    }

    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }
}