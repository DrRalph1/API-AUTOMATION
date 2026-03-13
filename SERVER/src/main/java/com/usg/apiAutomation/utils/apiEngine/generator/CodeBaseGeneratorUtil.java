package com.usg.apiAutomation.utils.apiEngine.generator;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.codeBase.*;
import com.usg.apiAutomation.repositories.postgres.codeBase.*;
import com.usg.apiAutomation.utils.apiEngine.CodeLanguageGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.GenUrlBuilderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeBaseGeneratorUtil {

    private final CollectionRepository codeBaseCollectionRepository;
    private final FolderRepository codeBaseFolderRepository;
    private final RequestRepository codeBaseRequestRepository;
    private final ImplementationRepository implementationRepository;
    private final GenUrlBuilderUtil genUrlBuilder;

    @Transactional
    public String generate(GeneratedApiEntity api, String performedBy,
                           GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Generating Code Base for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo.getCollectionName());

            com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity codeBaseCollection;

            log.debug("Attempting to lock codebase collection with ID: {}", collectionInfo.getCollectionId());

            Optional<com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity> existingCodeBaseCollection =
                    codeBaseCollectionRepository.findByIdWithLock(collectionInfo.getCollectionId());

            log.debug("findByIdWithLock returned: {}", existingCodeBaseCollection.isPresent());

            if (existingCodeBaseCollection.isPresent()) {
                codeBaseCollection = existingCodeBaseCollection.get();
                log.info("Found existing code base collection with lock: {}", codeBaseCollection.getId());

                boolean needsUpdate = false;

                if (!collectionInfo.getCollectionName().equals(codeBaseCollection.getName())) {
                    log.debug("Collection name changed from '{}' to '{}'",
                            codeBaseCollection.getName(), collectionInfo.getCollectionName());
                    codeBaseCollection.setName(collectionInfo.getCollectionName());
                    needsUpdate = true;
                }

                String expectedDescription = "Collection for " + collectionInfo.getCollectionName() +
                        (collectionInfo.getCollectionType() != null ?
                                " (" + collectionInfo.getCollectionType() + ")" : "");
                if (!expectedDescription.equals(codeBaseCollection.getDescription())) {
                    log.debug("Collection description changed");
                    codeBaseCollection.setDescription(expectedDescription);
                    needsUpdate = true;
                }

                if (!api.getVersion().equals(codeBaseCollection.getVersion())) {
                    log.debug("Collection version changed from '{}' to '{}'",
                            codeBaseCollection.getVersion(), api.getVersion());
                    codeBaseCollection.setVersion(api.getVersion());
                    needsUpdate = true;
                }

                if (!performedBy.equals(codeBaseCollection.getOwner())) {
                    log.debug("Collection owner changed from '{}' to '{}'",
                            codeBaseCollection.getOwner(), performedBy);
                    codeBaseCollection.setOwner(performedBy);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    codeBaseCollection.setUpdatedAt(LocalDateTime.now());
                    codeBaseCollection = codeBaseCollectionRepository.saveAndFlush(codeBaseCollection);
                    log.debug("Updated code base collection: {}, new version: {}",
                            codeBaseCollection.getId(), codeBaseCollection.getVersion());
                }
            } else {
                log.info("Codebase collection not found with lock, attempting to create: {}", collectionInfo.getCollectionId());

                try {
                    codeBaseCollection = com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity.builder()
                            .id(collectionInfo.getCollectionId())
                            .name(collectionInfo.getCollectionName())
                            .description("Collection for " + collectionInfo.getCollectionName() +
                                    (collectionInfo.getCollectionType() != null ?
                                            " (" + collectionInfo.getCollectionType() + ")" : ""))
                            .version(api.getVersion())
                            .owner(performedBy)
                            .isExpanded(false)
                            .isFavorite(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    codeBaseCollection = codeBaseCollectionRepository.saveAndFlush(codeBaseCollection);
                    log.info("Successfully created new code base collection: {}, version: {}",
                            codeBaseCollection.getId(), codeBaseCollection.getVersion());

                } catch (Exception e) {
                    log.warn("Failed to create collection, another transaction may have created it. Error: {}", e.getMessage());

                    Optional<com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity> retryFetch =
                            codeBaseCollectionRepository.findByIdWithLock(collectionInfo.getCollectionId());

                    if (retryFetch.isPresent()) {
                        codeBaseCollection = retryFetch.get();
                        log.info("Retrieved collection created by another transaction: {}", codeBaseCollection.getId());
                    } else {
                        throw new RuntimeException("Failed to create or retrieve collection: " + collectionInfo.getCollectionId());
                    }
                }
            }

            com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity codeBaseFolder;

            log.debug("Attempting to lock codebase folder with ID: {}", collectionInfo.getFolderId());

            Optional<com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity> existingCodeBaseFolder =
                    codeBaseFolderRepository.findByIdWithLock(collectionInfo.getFolderId());

            if (existingCodeBaseFolder.isPresent()) {
                codeBaseFolder = existingCodeBaseFolder.get();
                log.info("Found existing code base folder with lock: {}", codeBaseFolder.getId());

                boolean folderNeedsUpdate = false;

                if (!collectionInfo.getFolderName().equals(codeBaseFolder.getName())) {
                    log.debug("Folder name changed from '{}' to '{}'",
                            codeBaseFolder.getName(), collectionInfo.getFolderName());
                    codeBaseFolder.setName(collectionInfo.getFolderName());
                    folderNeedsUpdate = true;
                }

                String folderDescription = "Folder for " + collectionInfo.getFolderName();
                if (!folderDescription.equals(codeBaseFolder.getDescription())) {
                    log.debug("Folder description changed");
                    codeBaseFolder.setDescription(folderDescription);
                    folderNeedsUpdate = true;
                }

                if (folderNeedsUpdate) {
                    codeBaseFolder.setUpdatedAt(LocalDateTime.now());
                    codeBaseFolder = codeBaseFolderRepository.saveAndFlush(codeBaseFolder);
                    log.debug("Updated code base folder: {}", codeBaseFolder.getId());
                }
            } else {
                log.info("Codebase folder not found, creating new one with ID: {}", collectionInfo.getFolderId());

                try {
                    codeBaseFolder = com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity.builder()
                            .id(collectionInfo.getFolderId())
                            .name(collectionInfo.getFolderName())
                            .description("Folder for " + collectionInfo.getFolderName())
                            .isExpanded(false)
                            .collection(codeBaseCollection)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    codeBaseFolder = codeBaseFolderRepository.saveAndFlush(codeBaseFolder);
                    log.info("Created new code base folder: {}", codeBaseFolder.getId());
                } catch (Exception e) {
                    log.warn("Failed to create folder, another transaction may have created it. Error: {}", e.getMessage());

                    Optional<com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity> retryFolder =
                            codeBaseFolderRepository.findByIdWithLock(collectionInfo.getFolderId());

                    if (retryFolder.isPresent()) {
                        codeBaseFolder = retryFolder.get();
                        log.info("Retrieved folder created by another transaction: {}", codeBaseFolder.getId());
                    } else {
                        throw new RuntimeException("Failed to create or retrieve folder: " + collectionInfo.getFolderId());
                    }
                }
            }

            // Use the full URL from GenUrlBuilderUtil
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);
            String fullUrl = genUrlInfo.getFullUrl();
            log.info("Built gen URL for code base: {}", fullUrl);

            String requestId = UUID.randomUUID().toString();
            log.debug("Generated request ID: {} for API: {}", requestId, api.getApiCode());

            RequestEntity codeBaseRequest = RequestEntity.builder()
                    .id(requestId)
                    .name(api.getApiName() + " - " + api.getHttpMethod())
                    .method(api.getHttpMethod())
                    .url(fullUrl)  // Use gen URL from centralized builder
                    .description(api.getDescription())
                    .collection(codeBaseCollection)
                    .folder(codeBaseFolder)
                    .tags(new ArrayList<>())
                    .implementations(new ArrayList<>())
                    .implementationsCount(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<Map<String, Object>> headers = new ArrayList<>();

            // Add header parameters
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    if ("header".equals(param.getParameterType())) {
                        Map<String, Object> headerMap = new HashMap<>();
                        headerMap.put("key", param.getKey());
                        headerMap.put("value", param.getExample() != null ? param.getExample() : "{{" + param.getKey() + "}}");
                        headerMap.put("description", param.getDescription());
                        headerMap.put("required", param.getRequired());
                        headerMap.put("disabled", false);
                        headers.add(headerMap);
                    }
                }
            }

            // Add headers from headers array
            if (api.getHeaders() != null) {
                for (ApiHeaderEntity header : api.getHeaders()) {
                    if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                        Map<String, Object> headerMap = new HashMap<>();
                        headerMap.put("key", header.getKey() != null ? header.getKey() : "");
                        headerMap.put("value", header.getValue() != null ? header.getValue() : "");
                        headerMap.put("description", header.getDescription() != null ? header.getDescription() : "");
                        headerMap.put("required", header.getRequired() != null ? header.getRequired() : false);
                        headerMap.put("disabled", false);
                        headers.add(headerMap);
                    }
                }
            }

            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                Map<String, Object> authHeader = new HashMap<>();
                switch (api.getAuthConfig().getAuthType()) {
                    case "API_KEY":
                        authHeader.put("key", api.getAuthConfig().getApiKeyHeader() != null ?
                                api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                        authHeader.put("value", "{{apiKey}}");
                        authHeader.put("description", "API Key for authentication");
                        authHeader.put("required", true);
                        authHeader.put("disabled", false);
                        headers.add(authHeader);

                        if (api.getAuthConfig().getApiSecretHeader() != null) {
                            Map<String, Object> secretHeader = new HashMap<>();
                            secretHeader.put("key", api.getAuthConfig().getApiSecretHeader());
                            secretHeader.put("value", "{{apiSecret}}");
                            secretHeader.put("description", "API Secret for authentication");
                            secretHeader.put("required", true);
                            secretHeader.put("disabled", false);
                            headers.add(secretHeader);
                        }
                        break;
                    case "BEARER":
                    case "JWT":
                        authHeader.put("key", "Authorization");
                        authHeader.put("value", "Bearer {{jwtToken}}");
                        authHeader.put("description", "Bearer token authentication");
                        authHeader.put("required", true);
                        authHeader.put("disabled", false);
                        headers.add(authHeader);
                        break;
                    case "BASIC":
                        authHeader.put("key", "Authorization");
                        authHeader.put("value", "Basic {{base64Credentials}}");
                        authHeader.put("description", "Basic authentication");
                        authHeader.put("required", true);
                        authHeader.put("disabled", false);
                        headers.add(authHeader);
                        break;
                    case "ORACLE_ROLES":
                        authHeader.put("key", "X-Oracle-Session");
                        authHeader.put("value", "{{oracleSessionId}}");
                        authHeader.put("description", "Oracle Database Session ID");
                        authHeader.put("required", true);
                        authHeader.put("disabled", false);
                        headers.add(authHeader);
                        break;
                }
            }
            codeBaseRequest.setHeaders(headers);

            // Set path parameters
            if (api.getParameters() != null) {
                List<Map<String, Object>> pathParams = api.getParameters().stream()
                        .filter(p -> "path".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> param = new HashMap<>();
                            param.put("name", p.getKey() != null ? p.getKey() : "");
                            param.put("type", p.getApiType() != null ? p.getApiType() : "string");
                            param.put("required", p.getRequired() != null ? p.getRequired() : false);
                            param.put("description", p.getDescription() != null ? p.getDescription() : "");
                            param.put("key", p.getKey() != null ? p.getKey() : "");
                            param.put("value", p.getExample() != null ? p.getExample() : "{{" + p.getKey() + "}}");
                            return param;
                        })
                        .collect(Collectors.toList());
                codeBaseRequest.setPathParameters(pathParams);
            }

            // Set query parameters
            if (api.getParameters() != null) {
                List<Map<String, Object>> queryParams = api.getParameters().stream()
                        .filter(p -> "query".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> param = new HashMap<>();
                            param.put("name", p.getKey() != null ? p.getKey() : "");
                            param.put("type", p.getApiType() != null ? p.getApiType() : "string");
                            param.put("required", p.getRequired() != null ? p.getRequired() : false);
                            param.put("description", p.getDescription() != null ? p.getDescription() : "");
                            param.put("key", p.getKey() != null ? p.getKey() : "");
                            param.put("value", p.getExample() != null ? p.getExample() : "{{" + p.getKey() + "}}");
                            return param;
                        })
                        .collect(Collectors.toList());
                codeBaseRequest.setQueryParameters(queryParams);
            }

            // Set request body from body parameters
            if (api.getParameters() != null) {
                List<ApiParameterEntity> bodyParams = api.getParameters().stream()
                        .filter(p -> "body".equals(p.getParameterType()))
                        .collect(Collectors.toList());

                if (!bodyParams.isEmpty()) {
                    Map<String, Object> requestBodyMap = new HashMap<>();
                    for (ApiParameterEntity param : bodyParams) {
                        requestBodyMap.put(param.getKey(), param.getExample() != null ?
                                param.getExample() : "{{" + param.getKey() + "}}");
                    }
                    codeBaseRequest.setRequestBody(requestBodyMap);
                } else if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                    try {
                        Map<String, Object> requestBodyMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                                api.getRequestConfig().getSample(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                        );
                        codeBaseRequest.setRequestBody(requestBodyMap);
                    } catch (Exception e) {
                        log.warn("Failed to parse request body sample as JSON: {}", e.getMessage());
                        Map<String, Object> fallbackBody = new HashMap<>();
                        fallbackBody.put("content", api.getRequestConfig().getSample());
                        codeBaseRequest.setRequestBody(fallbackBody);
                    }
                }
            }

            if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
                try {
                    Map<String, Object> responseMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            api.getResponseConfig().getSuccessSchema(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                    );
                    codeBaseRequest.setResponseExample(responseMap);
                } catch (Exception e) {
                    log.warn("Failed to parse response example as JSON: {}", e.getMessage());
                    Map<String, Object> fallbackResponse = new HashMap<>();
                    fallbackResponse.put("data", api.getResponseConfig().getSuccessSchema());
                    codeBaseRequest.setResponseExample(fallbackResponse);
                }
            }

            RequestEntity savedRequest = codeBaseRequestRepository.save(codeBaseRequest);
            codeBaseRequestRepository.flush();

            generateImplementations(api, savedRequest);

            log.info("Code Base generated successfully with Request ID: {} using Collection: {}, Folder: {}",
                    savedRequest.getId(), collectionInfo.getCollectionName(), collectionInfo.getFolderName());

            return savedRequest.getId();

        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic locking failure in generateCodeBase: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Code Base due to concurrent modification. Please try again.", e);
        } catch (Exception e) {
            log.error("Error generating Code Base: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Code Base: " + e.getMessage(), e);
        }
    }

    private void generateImplementations(GeneratedApiEntity api, RequestEntity request) {
        CodeLanguageGeneratorUtil codeGenerator = new CodeLanguageGeneratorUtil();

        List<String> languages = Arrays.asList("java", "javascript", "python", "curl", "csharp", "php", "ruby", "go");

        for (String language : languages) {
            try {
                Optional<ImplementationEntity> existing = implementationRepository
                        .findByRequestIdAndLanguageAndComponent(request.getId(), language, "main");

                if (existing.isEmpty()) {
                    String code = codeGenerator.generateCodeForLanguage(api, language, genUrlBuilder.buildGenUrlInfo(api));

                    ImplementationEntity implementation = ImplementationEntity.builder()
                            .language(language)
                            .component("main")
                            .code(code)
                            .linesOfCode(code != null ? code.split("\n").length : 0)
                            .request(request)
                            .isValidated(false)
                            .build();

                    implementationRepository.save(implementation);
                }
            } catch (Exception e) {
                log.warn("Failed to generate {} implementation: {}", language, e.getMessage());
            }
        }
    }
}