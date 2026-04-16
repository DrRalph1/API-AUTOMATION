package com.usg.autoAPIGenerator.utils.apiEngine.generator;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GraphQLConfigDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.SoapConfigDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.autoAPIGenerator.entities.postgres.codeBase.*;
import com.usg.autoAPIGenerator.repositories.codeBase.*;
import com.usg.autoAPIGenerator.utils.apiEngine.CodeLanguageGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.GenUrlBuilderUtil;
import jakarta.persistence.EntityManager;
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
    private final LanguageConfigRepository languageConfigRepository;
    private final ParameterRepository codeBaseParameterRepository;
    private final HeaderRepository codeBaseHeaderRepository;
    private final GenUrlBuilderUtil genUrlBuilder;
    private final EntityManager entityManager;

    @Transactional
    public String generate(GeneratedApiEntity api, String performedBy,
                           GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Generating Code Base for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo.getCollectionName());

            String generatedApiId = api.getId();

            // ============ COLLECTION ENTITY ============
            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity codeBaseCollection =
                    createOrUpdateCollection(api, performedBy, collectionInfo, generatedApiId);

            // ============ FOLDER ENTITY ============
            com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity codeBaseFolder =
                    createOrUpdateFolder(api, performedBy, collectionInfo, codeBaseCollection, generatedApiId);

            // ============ REQUEST ENTITY ============
            RequestEntity codeBaseRequest = createRequest(api, performedBy, codeBaseCollection,
                    codeBaseFolder, generatedApiId);

            // ============ IMPLEMENTATION ENTITIES ============
            generateImplementations(api, codeBaseRequest, generatedApiId, performedBy);

            // ============ HEADER ENTITIES ============
            createHeaderEntities(api, codeBaseRequest, generatedApiId);

            // ============ PARAMETER ENTITIES ============
            createParameterEntities(api, codeBaseRequest, generatedApiId);

            // ============ LANGUAGE CONFIG ENTITIES ============
            createLanguageConfigs(generatedApiId);

            log.info("Code Base generated successfully with Request ID: {} using Collection: {}, Folder: {}",
                    codeBaseRequest.getId(), collectionInfo.getCollectionName(), collectionInfo.getFolderName());

            return codeBaseRequest.getId();

        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic locking failure in generateCodeBase: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Code Base due to concurrent modification. Please try again.", e);
        } catch (Exception e) {
            log.error("Error generating Code Base: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Code Base: " + e.getMessage(), e);
        }
    }

    /**
     * UPDATE METHOD - Similar to DocumentationGeneratorUtil.update()
     * Updates existing codebase documentation when moving between collections/folders
     */
    @Transactional
    public void update(GeneratedApiEntity api, String performedBy,
                       GenerateApiRequestDTO request,
                       String collectionsCollectionId,
                       CollectionInfoDTO collectionInfo) {
        try {
            log.info("Updating Code Base for API: {} to collection: {}, folder: {}",
                    api.getApiCode(), collectionInfo.getCollectionName(), collectionInfo.getFolderName());

            String generatedApiId = api.getId();

            // Find existing requests for this API
            List<RequestEntity> existingRequests = codeBaseRequestRepository.findByGeneratedApiId(generatedApiId);

            if (existingRequests.isEmpty()) {
                // No existing documentation, generate new one
                log.info("No existing codebase found, generating new one for API: {}", api.getApiCode());
                generate(api, performedBy, request, collectionInfo);
                return;
            }

            // Get the first request (use this one)
            RequestEntity codeBaseRequest = existingRequests.get(0);
            String requestId = codeBaseRequest.getId();

            // Store current IDs BEFORE any changes
            String currentCollectionId = codeBaseRequest.getCollection() != null ?
                    codeBaseRequest.getCollection().getId() : null;
            String currentFolderId = codeBaseRequest.getFolder() != null ?
                    codeBaseRequest.getFolder().getId() : null;

            log.info("Current location - Collection: {}, Folder: {}", currentCollectionId, currentFolderId);

            // Get or create collection
            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity codeBaseCollection =
                    getOrCreateCollection(api, performedBy, collectionInfo, generatedApiId);

            // Get or create folder
            com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity codeBaseFolder =
                    getOrCreateFolder(api, performedBy, collectionInfo, codeBaseCollection, generatedApiId);

            boolean collectionChanged = (codeBaseCollection.getId() != null &&
                    !codeBaseCollection.getId().equals(currentCollectionId));
            boolean folderChanged = (codeBaseFolder.getId() != null &&
                    !codeBaseFolder.getId().equals(currentFolderId));

            log.info("CodeBase - Collection changed: {} -> {}, Folder changed: {} -> {}",
                    currentCollectionId, codeBaseCollection.getId(),
                    currentFolderId, codeBaseFolder.getId());

            if (collectionChanged || folderChanged) {
                log.info("CodeBase - Collection or folder changed, moving request to new location");

                // CRITICAL: First, update folder flags BEFORE moving the request
                // This ensures we check the old folder's count while the request is still there
                if (folderChanged && currentFolderId != null) {
                    log.info("Checking if old folder '{}' will have any requests after move", currentFolderId);
                    // Count requests in old folder BEFORE we move
                    long currentRequestCount = codeBaseRequestRepository.countByFolderId(currentFolderId);
                    log.info("Old folder '{}' currently has {} requests", currentFolderId, currentRequestCount);

                    // After move, it will have (currentRequestCount - 1) requests
                    boolean willHaveRequests = (currentRequestCount - 1) > 0;

                    // Update old folder's hasRequests flag
                    codeBaseFolderRepository.findById(currentFolderId).ifPresent(oldFolder -> {
                        oldFolder.setHasRequests(willHaveRequests);
                        codeBaseFolderRepository.save(oldFolder);
                        log.info("Updated old folder '{}' hasRequests: {} (will have {} requests after move)",
                                currentFolderId, willHaveRequests, currentRequestCount - 1);
                    });
                }

                // Clear all relationships from the existing request
                clearAllCodeBaseRequestRelationships(codeBaseRequest);

                // Update the request with new collection and folder
                codeBaseRequest.setCollection(codeBaseCollection);
                codeBaseRequest.setFolder(codeBaseFolder);

                // Update the request content (this will recreate implementations)
                updateCodeBaseRequestContent(codeBaseRequest, api, performedBy, request);

                // Save the updated request
                codeBaseRequest = codeBaseRequestRepository.save(codeBaseRequest);

                // Now update the new folder's hasRequests flag to true
                if (folderChanged && codeBaseFolder.getId() != null) {
                    codeBaseFolderRepository.findById(codeBaseFolder.getId()).ifPresent(newFolder -> {
                        newFolder.setHasRequests(true);
                        codeBaseFolderRepository.save(newFolder);
                        log.info("Updated new folder '{}' hasRequests: true", codeBaseFolder.getId());
                    });
                }

                // Also update collection counts if needed
                updateCollectionRequestCounts(currentCollectionId, codeBaseCollection.getId());

                // CRITICAL: Flush and clear the EntityManager to ensure all changes are persisted
                entityManager.flush();
                entityManager.clear();

                log.info("Successfully moved codebase request from folder '{}' to folder '{}'",
                        currentFolderId, codeBaseFolder.getId());
                return;
            }

            // If collection/folder hasn't changed, just update the content
            log.info("CodeBase - No collection/folder change, just updating content");
            clearAllCodeBaseRequestRelationships(codeBaseRequest);
            updateCodeBaseRequestContent(codeBaseRequest, api, performedBy, request);
            codeBaseRequestRepository.save(codeBaseRequest);
            log.info("Successfully updated existing codebase request: {}", requestId);

        } catch (Exception e) {
            log.error("Error updating Code Base: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update Code Base: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to update folder request flags for codebase
     * This method is now simplified since we handle folder updates in the main update method
     */
    private void updateCodeBaseFolderRequestFlags(String oldFolderId, String newFolderId) {
        // This method is kept for backward compatibility but the main logic is now in the update method
        try {
            log.debug("Updating folder request flags - Old: {}, New: {}", oldFolderId, newFolderId);

            // This method is now deprecated - use the logic in the main update method instead
            log.warn("updateCodeBaseFolderRequestFlags is deprecated - use the logic in update() method");

        } catch (Exception e) {
            log.error("Failed to update folder request flags: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to check if a folder has any requests (excluding the current request if needed)
     */
    private boolean checkIfFolderHasRequests(String folderId, String excludeRequestId) {
        try {
            List<RequestEntity> requests = codeBaseRequestRepository.findByFolderId(folderId);
            if (requests == null) return false;

            // Filter out the current request
            long count = requests.stream()
                    .filter(r -> !r.getId().equals(excludeRequestId))
                    .count();

            log.debug("Folder '{}' has {} requests (excluding request {})", folderId, count, excludeRequestId);
            return count > 0;
        } catch (Exception e) {
            log.error("Error checking if folder has requests: {}", e.getMessage(), e);
            return true;
        }
    }

    /**
     * Helper method to update collection request counts if needed
     */
    private void updateCollectionRequestCounts(String oldCollectionId, String newCollectionId) {
        try {
            // You may or may not need this - depends on your collection entity structure
            // Some collections have a requestCount field, others don't
            log.debug("Updating collection request counts - Old: {}, New: {}", oldCollectionId, newCollectionId);

            // If you have a requestCount field in CollectionEntity, update it here
            // Otherwise, this method can be empty or removed
        } catch (Exception e) {
            log.warn("Failed to update collection request counts: {}", e.getMessage());
        }
    }


    // ==================== HELPER METHODS FOR UPDATE ====================

    /**
     * Get or create codebase collection
     */
    private com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity getOrCreateCollection(
            GeneratedApiEntity api, String performedBy, CollectionInfoDTO collectionInfo, String generatedApiId) {

        Optional<com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity> existing =
                codeBaseCollectionRepository.findById(collectionInfo.getCollectionId());

        if (existing.isPresent()) {
            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection = existing.get();
            if (!collectionInfo.getCollectionName().equals(collection.getName())) {
                collection.setName(collectionInfo.getCollectionName());
                collection.setUpdatedAt(LocalDateTime.now());
                return codeBaseCollectionRepository.save(collection);
            }
            if (collection.getGeneratedApiId() == null) {
                collection.setGeneratedApiId(generatedApiId);
                collection.setUpdatedAt(LocalDateTime.now());
                return codeBaseCollectionRepository.save(collection);
            }
            return collection;
        }

        // Create new collection
        com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection =
                com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity.builder()
                        .id(collectionInfo.getCollectionId())
                        .generatedApiId(generatedApiId)
                        .name(collectionInfo.getCollectionName())
                        .description("Collection for " + collectionInfo.getCollectionName())
                        .version(api.getVersion())
                        .owner(performedBy)
                        .isExpanded(false)
                        .isFavorite(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
        return codeBaseCollectionRepository.save(collection);
    }

    /**
     * Get or create codebase folder
     */
    private com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity getOrCreateFolder(
            GeneratedApiEntity api, String performedBy, CollectionInfoDTO collectionInfo,
            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection, String generatedApiId) {

        Optional<com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity> existing =
                codeBaseFolderRepository.findById(collectionInfo.getFolderId());

        if (existing.isPresent()) {
            com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity folder = existing.get();
            if (!collectionInfo.getFolderName().equals(folder.getName())) {
                folder.setName(collectionInfo.getFolderName());
                folder.setUpdatedAt(LocalDateTime.now());
                return codeBaseFolderRepository.save(folder);
            }
            if (folder.getGeneratedApiId() == null) {
                folder.setGeneratedApiId(generatedApiId);
                folder.setUpdatedAt(LocalDateTime.now());
                return codeBaseFolderRepository.save(folder);
            }
            return folder;
        }

        // Create new folder
        com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity folder =
                com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity.builder()
                        .id(collectionInfo.getFolderId())
                        .generatedApiId(generatedApiId)
                        .name(collectionInfo.getFolderName())
                        .description("Folder for " + collectionInfo.getFolderName())
                        .isExpanded(false)
                        .collection(collection)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
        return codeBaseFolderRepository.save(folder);
    }

    /**
     * Helper method to completely clear all relationships from a codebase request
     * This is critical to avoid constraint violations
     */
    private void clearAllCodeBaseRequestRelationships(RequestEntity request) {
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

            // Clear headers
            if (request.getHeaders() != null) {
                request.getHeaders().clear();
            }

            // Clear path parameters
            if (request.getPathParameters() != null) {
                request.getPathParameters().clear();
            }

            // Clear query parameters
            if (request.getQueryParameters() != null) {
                request.getQueryParameters().clear();
            }

            // Clear tags
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
    private void updateCodeBaseRequestContent(RequestEntity request,
                                              GeneratedApiEntity api,
                                              String performedBy,
                                              GenerateApiRequestDTO generateRequest) {
        try {
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);
            String protocolType = api.getProtocolType() != null ? api.getProtocolType() : "rest";

            // Update basic info
            request.setName(api.getApiName() + " - " + api.getHttpMethod());

            // ============ PROTOCOL-SPECIFIC METHOD OVERRIDE ============
            if ("soap".equalsIgnoreCase(protocolType) || "graphql".equalsIgnoreCase(protocolType)) {
                request.setMethod("POST");
                log.info("Overriding method to POST for {} protocol", protocolType);
            } else {
                request.setMethod(api.getHttpMethod());
            }

            request.setUrl(genUrlInfo.getFullUrl());
            request.setDescription(api.getDescription());
            request.setUpdatedAt(LocalDateTime.now());

            // Set request body from API request config or generate protocol-specific body
            if ("soap".equalsIgnoreCase(protocolType)) {
                // Generate SOAP request body
                String soapBody = generateSoapRequestBody(api);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("sample", soapBody);
                bodyMap.put("format", "xml");
                request.setRequestBody(bodyMap);
                log.info("Generated SOAP request body for codebase");
            } else if ("graphql".equalsIgnoreCase(protocolType)) {
                // Generate GraphQL request body
                String graphqlBody = generateGraphQLRequestBody(api);
                try {
                    Map<String, Object> bodyMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            graphqlBody,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    request.setRequestBody(bodyMap);
                } catch (Exception e) {
                    Map<String, Object> fallbackMap = new HashMap<>();
                    fallbackMap.put("query", graphqlBody);
                    request.setRequestBody(fallbackMap);
                }
                log.info("Generated GraphQL request body for codebase");
            } else if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                try {
                    Map<String, Object> bodyMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            api.getRequestConfig().getSample(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    request.setRequestBody(bodyMap);
                } catch (Exception e) {
                    log.warn("Failed to parse request body sample as JSON: {}", e.getMessage());
                    Map<String, Object> fallbackMap = new HashMap<>();
                    fallbackMap.put("sample", api.getRequestConfig().getSample());
                    request.setRequestBody(fallbackMap);
                }
            }

            // Set headers as JSON with protocol-specific Content-Type
            List<Map<String, Object>> headers = buildHeadersList(api);

            // Add protocol-specific headers
            if ("soap".equalsIgnoreCase(protocolType)) {
                // Add SOAP-specific headers
                Map<String, Object> contentTypeHeader = new HashMap<>();
                contentTypeHeader.put("key", "Content-Type");
                contentTypeHeader.put("value", "text/xml; charset=utf-8");
                contentTypeHeader.put("description", "SOAP request content type");
                contentTypeHeader.put("required", true);
                contentTypeHeader.put("disabled", false);
                headers.add(0, contentTypeHeader);

                if (api.getSoapConfig() != null && api.getSoapConfig().getSoapAction() != null) {
                    Map<String, Object> soapActionHeader = new HashMap<>();
                    soapActionHeader.put("key", "SOAPAction");
                    soapActionHeader.put("value", api.getSoapConfig().getSoapAction());
                    soapActionHeader.put("description", "SOAP action header");
                    soapActionHeader.put("required", true);
                    soapActionHeader.put("disabled", false);
                    headers.add(1, soapActionHeader);
                }
            } else if ("graphql".equalsIgnoreCase(protocolType)) {
                // Add GraphQL-specific headers
                Map<String, Object> contentTypeHeader = new HashMap<>();
                contentTypeHeader.put("key", "Content-Type");
                contentTypeHeader.put("value", "application/json");
                contentTypeHeader.put("description", "GraphQL request content type");
                contentTypeHeader.put("required", true);
                contentTypeHeader.put("disabled", false);
                headers.add(0, contentTypeHeader);
            }

            request.setHeaders(headers);

            // Set path parameters
            if (api.getParameters() != null) {
                List<Map<String, Object>> pathParams = api.getParameters().stream()
                        .filter(p -> "path".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> param = new HashMap<>();
                            param.put("name", p.getKey());
                            param.put("type", p.getApiType() != null ? p.getApiType() : "string");
                            param.put("required", p.getRequired());
                            param.put("description", p.getDescription());
                            param.put("key", p.getKey());
                            param.put("value", p.getExample() != null ? p.getExample() : "{{" + p.getKey() + "}}");
                            return param;
                        })
                        .collect(Collectors.toList());
                request.setPathParameters(pathParams);

                // Set query parameters
                List<Map<String, Object>> queryParams = api.getParameters().stream()
                        .filter(p -> "query".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> param = new HashMap<>();
                            param.put("name", p.getKey());
                            param.put("type", p.getApiType() != null ? p.getApiType() : "string");
                            param.put("required", p.getRequired());
                            param.put("description", p.getDescription());
                            param.put("key", p.getKey());
                            param.put("value", p.getExample() != null ? p.getExample() : "{{" + p.getKey() + "}}");
                            return param;
                        })
                        .collect(Collectors.toList());
                request.setQueryParameters(queryParams);
            }

            // Set response example with protocol-specific formatting
            setResponseExample(api, request, protocolType);

            // Set metadata with protocol info
            Map<String, Object> metadata = request.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }
            metadata.put("protocolType", protocolType);
            if ("soap".equalsIgnoreCase(protocolType) && api.getSoapConfig() != null) {
                metadata.put("soapVersion", api.getSoapConfig().getVersion());
                metadata.put("soapNamespace", api.getSoapConfig().getNamespace());
                metadata.put("soapAction", api.getSoapConfig().getSoapAction());
            }
            if ("graphql".equalsIgnoreCase(protocolType) && api.getGraphqlConfig() != null) {
                metadata.put("graphqlOperationType", api.getGraphqlConfig().getOperationType());
                metadata.put("graphqlOperationName", api.getGraphqlConfig().getOperationName());
            }
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
    private void createImplementationsForRequest(RequestEntity request,
                                                 GeneratedApiEntity api,
                                                 String performedBy) {
        try {
            log.info("Creating implementations for request: {}", request.getId());

            // Generate code for all languages
            CodeLanguageGeneratorUtil codeGenerator = new CodeLanguageGeneratorUtil();
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

            List<String> languages = Arrays.asList("java", "javascript", "python", "curl",
                    "csharp", "php", "ruby", "go", "kotlin", "swift", "rust");

            List<ImplementationEntity> newImplementations = new ArrayList<>();

            for (String language : languages) {
                try {
                    String code = codeGenerator.generateCodeForLanguage(api, language, genUrlInfo);

                    if (code != null && !code.trim().isEmpty()) {
                        ImplementationEntity impl = ImplementationEntity.builder()
                                .id(UUID.randomUUID().toString())
                                .generatedApiId(api.getId())
                                .language(language)
                                .component("api_" + api.getApiCode().toLowerCase())
                                .code(code)
                                .fileName(api.getApiCode() + getFileExtension(language))
                                .linesOfCode(code != null ? code.split("\n").length : 0)
                                .fileSize(code != null ? (long) code.length() : 0L)
                                .request(request)
                                .isValidated(false)
                                .validationScore(0)
                                .isGenerated(true)
                                .generatedBy(performedBy)
                                .version(1)
                                .usageCount(0)
                                .framework(getDefaultFramework(language))
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        newImplementations.add(impl);
                    }
                } catch (Exception e) {
                    log.warn("Failed to generate {} implementation: {}", language, e.getMessage());
                }
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
            } else {
                log.warn("No code generated for API: {}", api.getId());
            }

        } catch (Exception e) {
            log.error("Error creating implementations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create implementations", e);
        }
    }



    // ==================== EXISTING PRIVATE METHODS ====================

    private com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity createOrUpdateCollection(
            GeneratedApiEntity api, String performedBy, CollectionInfoDTO collectionInfo, String generatedApiId) {

        Optional<com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity> existing =
                codeBaseCollectionRepository.findById(collectionInfo.getCollectionId());

        if (existing.isPresent()) {
            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection = existing.get();
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
                collection.setUpdatedAt(LocalDateTime.now());
                return codeBaseCollectionRepository.save(collection);
            }
            return collection;
        } else {
            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection =
                    com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity.builder()
                            .id(collectionInfo.getCollectionId())
                            .generatedApiId(generatedApiId)
                            .name(collectionInfo.getCollectionName())
                            .description("Collection for " + collectionInfo.getCollectionName())
                            .version(api.getVersion())
                            .owner(performedBy)
                            .isExpanded(false)
                            .isFavorite(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
            return codeBaseCollectionRepository.save(collection);
        }
    }

    private com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity createOrUpdateFolder(
            GeneratedApiEntity api, String performedBy, CollectionInfoDTO collectionInfo,
            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection, String generatedApiId) {

        Optional<com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity> existing =
                codeBaseFolderRepository.findById(collectionInfo.getFolderId());

        if (existing.isPresent()) {
            com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity folder = existing.get();
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
                return codeBaseFolderRepository.save(folder);
            }
            return folder;
        } else {
            com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity folder =
                    com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity.builder()
                            .id(collectionInfo.getFolderId())
                            .generatedApiId(generatedApiId)
                            .name(collectionInfo.getFolderName())
                            .description("Folder for " + collectionInfo.getFolderName())
                            .isExpanded(false)
                            .collection(collection)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
            return codeBaseFolderRepository.save(folder);
        }
    }

    private RequestEntity createRequest(GeneratedApiEntity api, String performedBy,
                                        com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection,
                                        com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity folder,
                                        String generatedApiId) {

        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);
        String requestId = UUID.randomUUID().toString();
        String protocolType = api.getProtocolType() != null ? api.getProtocolType() : "rest";

        RequestEntity request = RequestEntity.builder()
                .id(requestId)
                .generatedApiId(generatedApiId)
                .name(api.getApiName() + " - " + api.getHttpMethod())
                .method(api.getHttpMethod())
                .url(genUrlInfo.getFullUrl())
                .description(api.getDescription())
                .collection(collection)
                .folder(folder)
                .tags(api.getTags() != null ? new ArrayList<>(api.getTags()) : new ArrayList<>())
                .implementations(new ArrayList<>())
                .implementationsCount(0)
                .isFavorite(false)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // ============ PROTOCOL-SPECIFIC METHOD OVERRIDE ============
        if ("soap".equalsIgnoreCase(protocolType) || "graphql".equalsIgnoreCase(protocolType)) {
            request.setMethod("POST");
            log.info("Overriding method to POST for {} protocol", protocolType);
        }

        // Set headers as JSON
        List<Map<String, Object>> headers = buildHeadersList(api);

        // Add protocol-specific headers
        if ("soap".equalsIgnoreCase(protocolType)) {
            Map<String, Object> contentTypeHeader = new HashMap<>();
            contentTypeHeader.put("key", "Content-Type");
            contentTypeHeader.put("value", "text/xml; charset=utf-8");
            contentTypeHeader.put("description", "SOAP request content type");
            contentTypeHeader.put("required", true);
            contentTypeHeader.put("disabled", false);
            headers.add(0, contentTypeHeader);

            if (api.getSoapConfig() != null && api.getSoapConfig().getSoapAction() != null) {
                Map<String, Object> soapActionHeader = new HashMap<>();
                soapActionHeader.put("key", "SOAPAction");
                soapActionHeader.put("value", api.getSoapConfig().getSoapAction());
                soapActionHeader.put("description", "SOAP action header");
                soapActionHeader.put("required", true);
                soapActionHeader.put("disabled", false);
                headers.add(1, soapActionHeader);
            }
        } else if ("graphql".equalsIgnoreCase(protocolType)) {
            Map<String, Object> contentTypeHeader = new HashMap<>();
            contentTypeHeader.put("key", "Content-Type");
            contentTypeHeader.put("value", "application/json");
            contentTypeHeader.put("description", "GraphQL request content type");
            contentTypeHeader.put("required", true);
            contentTypeHeader.put("disabled", false);
            headers.add(0, contentTypeHeader);
        }

        request.setHeaders(headers);

        // Set path parameters
        if (api.getParameters() != null) {
            List<Map<String, Object>> pathParams = api.getParameters().stream()
                    .filter(p -> "path".equals(p.getParameterType()))
                    .map(p -> {
                        Map<String, Object> param = new HashMap<>();
                        param.put("name", p.getKey());
                        param.put("type", p.getApiType() != null ? p.getApiType() : "string");
                        param.put("required", p.getRequired());
                        param.put("description", p.getDescription());
                        param.put("key", p.getKey());
                        param.put("value", p.getExample() != null ? p.getExample() : "{{" + p.getKey() + "}}");
                        return param;
                    })
                    .collect(Collectors.toList());
            request.setPathParameters(pathParams);

            // Set query parameters
            List<Map<String, Object>> queryParams = api.getParameters().stream()
                    .filter(p -> "query".equals(p.getParameterType()))
                    .map(p -> {
                        Map<String, Object> param = new HashMap<>();
                        param.put("name", p.getKey());
                        param.put("type", p.getApiType() != null ? p.getApiType() : "string");
                        param.put("required", p.getRequired());
                        param.put("description", p.getDescription());
                        param.put("key", p.getKey());
                        param.put("value", p.getExample() != null ? p.getExample() : "{{" + p.getKey() + "}}");
                        return param;
                    })
                    .collect(Collectors.toList());
            request.setQueryParameters(queryParams);
        }

        // Set request body based on protocol
        setRequestBody(api, request, protocolType);

        // Set response example based on protocol
        setResponseExample(api, request, protocolType);

        // Set metadata with protocol info
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("protocolType", protocolType);
        if ("soap".equalsIgnoreCase(protocolType) && api.getSoapConfig() != null) {
            metadata.put("soapVersion", api.getSoapConfig().getVersion());
            metadata.put("soapNamespace", api.getSoapConfig().getNamespace());
            metadata.put("soapAction", api.getSoapConfig().getSoapAction());
            metadata.put("wsdlUrl", genUrlInfo.getFullUrl() + "?wsdl");
        }
        if ("graphql".equalsIgnoreCase(protocolType) && api.getGraphqlConfig() != null) {
            metadata.put("graphqlOperationType", api.getGraphqlConfig().getOperationType());
            metadata.put("graphqlOperationName", api.getGraphqlConfig().getOperationName());
            metadata.put("graphqlEndpoint", genUrlInfo.getFullUrl());
            if (api.getGraphqlConfig().getSchema() != null) {
                metadata.put("graphqlSchema", api.getGraphqlConfig().getSchema());
            }
        }
        request.setMetadata(metadata);

        return codeBaseRequestRepository.save(request);
    }

    private List<Map<String, Object>> buildHeadersList(GeneratedApiEntity api) {
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

        // Add auth headers
        addAuthHeaders(api, headers);

        return headers;
    }

    private void addAuthHeaders(GeneratedApiEntity api, List<Map<String, Object>> headers) {
        if (api.getAuthConfig() == null || "NONE".equals(api.getAuthConfig().getAuthType())) {
            return;
        }

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

    private void setRequestBody(GeneratedApiEntity api, RequestEntity request, String protocolType) {
        if ("soap".equalsIgnoreCase(protocolType)) {
            // Generate SOAP request body
            String soapBody = generateSoapRequestBody(api);
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("sample", soapBody);
            requestBodyMap.put("format", "xml");
            request.setRequestBody(requestBodyMap);
            log.debug("Set SOAP request body for codebase");
        } else if ("graphql".equalsIgnoreCase(protocolType)) {
            // Generate GraphQL request body
            String graphqlBody = generateGraphQLRequestBody(api);
            try {
                Map<String, Object> requestBodyMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        graphqlBody,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                request.setRequestBody(requestBodyMap);
            } catch (Exception e) {
                Map<String, Object> fallbackMap = new HashMap<>();
                fallbackMap.put("query", graphqlBody);
                request.setRequestBody(fallbackMap);
            }
            log.debug("Set GraphQL request body for codebase");
        } else if (api.getParameters() != null) {
            // REST API - existing logic
            List<ApiParameterEntity> bodyParams = api.getParameters().stream()
                    .filter(p -> "body".equals(p.getParameterType()))
                    .collect(Collectors.toList());

            if (!bodyParams.isEmpty()) {
                Map<String, Object> requestBodyMap = new HashMap<>();
                for (ApiParameterEntity param : bodyParams) {
                    requestBodyMap.put(param.getKey(), param.getExample() != null ?
                            param.getExample() : "{{" + param.getKey() + "}}");
                }
                request.setRequestBody(requestBodyMap);
            } else if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                try {
                    Map<String, Object> requestBodyMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            api.getRequestConfig().getSample(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    request.setRequestBody(requestBodyMap);
                } catch (Exception e) {
                    log.warn("Failed to parse request body sample as JSON: {}", e.getMessage());
                }
            }
        }
    }

    private void setResponseExample(GeneratedApiEntity api, RequestEntity request, String protocolType) {
        if ("soap".equalsIgnoreCase(protocolType) && api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            // For SOAP, try to keep the response as XML string
            try {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("xml", api.getResponseConfig().getSuccessSchema());
                request.setResponseExample(responseMap);
            } catch (Exception e) {
                log.warn("Failed to set SOAP response example: {}", e.getMessage());
            }
        } else if ("graphql".equalsIgnoreCase(protocolType) && api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            try {
                Map<String, Object> responseMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        api.getResponseConfig().getSuccessSchema(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                request.setResponseExample(responseMap);
            } catch (Exception e) {
                log.warn("Failed to parse response example as JSON: {}", e.getMessage());
            }
        } else if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            try {
                Map<String, Object> responseMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        api.getResponseConfig().getSuccessSchema(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                request.setResponseExample(responseMap);
            } catch (Exception e) {
                log.warn("Failed to parse response example as JSON: {}", e.getMessage());
            }
        }
    }

    // Keep original method for backward compatibility
    private void setResponseExample(GeneratedApiEntity api, RequestEntity request) {
        setResponseExample(api, request, "rest");
    }

    private void generateImplementations(GeneratedApiEntity api, RequestEntity request,
                                         String generatedApiId, String performedBy) {
        CodeLanguageGeneratorUtil codeGenerator = new CodeLanguageGeneratorUtil();
        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

        List<String> languages = Arrays.asList("java", "javascript", "python", "curl",
                "csharp", "php", "ruby", "go", "kotlin", "swift", "rust");

        for (String language : languages) {
            try {
                Optional<ImplementationEntity> existing = implementationRepository
                        .findByRequestIdAndLanguageAndComponent(request.getId(), language, "main");

                if (existing.isEmpty()) {
                    String code = codeGenerator.generateCodeForLanguage(api, language, genUrlInfo);

                    ImplementationEntity implementation = ImplementationEntity.builder()
                            .id(UUID.randomUUID().toString())
                            .generatedApiId(generatedApiId)
                            .language(language)
                            .component("main")
                            .code(code)
                            .fileName(api.getApiCode() + getFileExtension(language))
                            .linesOfCode(code != null ? code.split("\n").length : 0)
                            .fileSize(code != null ? (long) code.length() : 0L)
                            .request(request)
                            .isValidated(false)
                            .validationScore(0)
                            .isGenerated(true)
                            .generatedBy(performedBy)
                            .version(1)
                            .usageCount(0)
                            .framework(getDefaultFramework(language))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    implementationRepository.save(implementation);
                    log.debug("Generated {} implementation for request: {}", language, request.getId());
                }
            } catch (Exception e) {
                log.warn("Failed to generate {} implementation: {}", language, e.getMessage());
            }
        }
    }

    private String getFileExtension(String language) {
        switch (language.toLowerCase()) {
            case "java": return ".java";
            case "javascript": return ".js";
            case "python": return ".py";
            case "csharp": return ".cs";
            case "php": return ".php";
            case "ruby": return ".rb";
            case "go": return ".go";
            case "kotlin": return ".kt";
            case "swift": return ".swift";
            case "rust": return ".rs";
            default: return ".txt";
        }
    }

    private String getDefaultFramework(String language) {
        switch (language.toLowerCase()) {
            case "java": return "Spring Boot";
            case "javascript": return "Express";
            case "python": return "Flask";
            case "csharp": return ".NET Core";
            case "php": return "Laravel";
            case "ruby": return "Rails";
            case "go": return "Gin";
            default: return null;
        }
    }

    private void createHeaderEntities(GeneratedApiEntity api, RequestEntity request, String generatedApiId) {
        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                    HeaderEntity header = HeaderEntity.builder()
                            .id(UUID.randomUUID().toString())
                            .generatedApiId(generatedApiId)
                            .key(apiHeader.getKey())
                            .value(apiHeader.getValue())
                            .description(apiHeader.getDescription())
                            .required(apiHeader.getRequired())
                            .disabled(false)
                            .requestEntity(request)
                            .build();

                    codeBaseHeaderRepository.save(header);
                }
            }
        }
    }

    private void createParameterEntities(GeneratedApiEntity api, RequestEntity request, String generatedApiId) {
        if (api.getParameters() != null) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                ParameterEntity param = ParameterEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .generatedApiId(generatedApiId)
                        .name(apiParam.getKey())
                        .type(apiParam.getParameterLocation() != null ? apiParam.getParameterLocation() : "query")
                        .value(apiParam.getExample())
                        .description(apiParam.getDescription())
                        .required(apiParam.getRequired())
                        .defaultValue(apiParam.getDefaultValue())
                        .requestEntity(request)
                        .build();

                codeBaseParameterRepository.save(param);
            }
        }
    }

    private void createLanguageConfigs(String generatedApiId) {
        try {
            // First, ensure any pending changes are flushed
            entityManager.flush();

            // Clear the persistence context to avoid state conflicts
            entityManager.clear();

            // Check if language configs already exist
            if (languageConfigRepository.count() > 0) {
                return;
            }

            List<LanguageConfigEntity> configs = Arrays.asList(
                    createLanguageConfig(generatedApiId, "java", "Java", "Spring Boot", "#007396",
                            "java-icon", "mvn", "maven", ".java", "java"),
                    createLanguageConfig(generatedApiId, "javascript", "JavaScript", "Node.js/Express", "#F7DF1E",
                            "js-icon", "npm", "npm", ".js", "javascript"),
                    createLanguageConfig(generatedApiId, "python", "Python", "Flask/FastAPI", "#3776AB",
                            "python-icon", "pip", "pip", ".py", "python"),
                    createLanguageConfig(generatedApiId, "curl", "cURL", "Command Line", "#0B3B5C",
                            "curl-icon", null, null, ".sh", "bash"),
                    createLanguageConfig(generatedApiId, "csharp", "C#", ".NET Core", "#178600",
                            "csharp-icon", "dotnet", "nuget", ".cs", "csharp"),
                    createLanguageConfig(generatedApiId, "php", "PHP", "Laravel", "#777BB4",
                            "php-icon", "composer", "composer", ".php", "php"),
                    createLanguageConfig(generatedApiId, "ruby", "Ruby", "Rails", "#CC342D",
                            "ruby-icon", "gem", "bundler", ".rb", "ruby"),
                    createLanguageConfig(generatedApiId, "go", "Go", "Gin", "#00ADD8",
                            "go-icon", "go mod", "go", ".go", "go")
            );

            languageConfigRepository.saveAll(configs);
            log.debug("Created {} language configurations", configs.size());

        } catch (Exception e) {
            log.error("Error creating language configs: {}", e.getMessage(), e);
            // Don't throw the exception - this is not critical for API generation
        }
    }

    private LanguageConfigEntity createLanguageConfig(String generatedApiId, String language,
                                                      String displayName, String framework,
                                                      String color, String icon,
                                                      String command, String packageManager,
                                                      String fileExtension, String formatterName) {
        return LanguageConfigEntity.builder()
                .id(UUID.randomUUID().toString())
                .generatedApiId(generatedApiId)
                .language(language)
                .displayName(displayName)
                .framework(framework)
                .color(color)
                .icon(icon)
                .command(command)
                .packageManager(packageManager)
                .fileExtension(fileExtension)
                .formatterName(formatterName)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== PROTOCOL-SPECIFIC HELPER METHODS ====================

    /**
     * Generate SOAP request body for codebase
     */
    private String generateSoapRequestBody(GeneratedApiEntity api) {
        SoapConfigDTO soapConfig = api.getSoapConfig();
        String operationName = soapConfig != null && soapConfig.getSoapAction() != null ?
                soapConfig.getSoapAction() : api.getApiCode();
        String namespace = soapConfig != null && soapConfig.getNamespace() != null ?
                soapConfig.getNamespace() : "http://tempuri.org/";
        String soapVersion = soapConfig != null && soapConfig.getVersion() != null ?
                soapConfig.getVersion() : "1.1";

        StringBuilder soapBody = new StringBuilder();
        soapBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        if ("1.2".equals(soapVersion)) {
            soapBody.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n");
        } else {
            soapBody.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
        }

        soapBody.append("  <soap:Header/>\n");
        soapBody.append("  <soap:Body>\n");
        soapBody.append("    <").append(operationName).append(" xmlns=\"").append(namespace).append("\">\n");

        // Add input parameters (IN parameters)
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("IN".equalsIgnoreCase(param.getParamMode()) || param.getParamMode() == null) {
                    String example = param.getExample() != null ? param.getExample() : "string";
                    soapBody.append("      <").append(param.getKey()).append(">").append(example).append("</").append(param.getKey()).append(">\n");
                }
            }
        }

        soapBody.append("    </").append(operationName).append(">\n");
        soapBody.append("  </soap:Body>\n");
        soapBody.append("</soap:Envelope>");

        return soapBody.toString();
    }

    /**
     * Generate GraphQL request body for codebase
     */
    private String generateGraphQLRequestBody(GeneratedApiEntity api) {
        GraphQLConfigDTO graphqlConfig = api.getGraphqlConfig();
        String operationType = graphqlConfig != null && graphqlConfig.getOperationType() != null ?
                graphqlConfig.getOperationType() : "query";
        String operationName = graphqlConfig != null && graphqlConfig.getOperationName() != null ?
                graphqlConfig.getOperationName() : api.getApiCode();

        Map<String, Object> graphqlBody = new LinkedHashMap<>();

        // Build query string
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(operationType).append(" ").append(operationName).append("(");

        if (api.getParameters() != null) {
            List<ApiParameterEntity> inParams = api.getParameters().stream()
                    .filter(p -> "IN".equalsIgnoreCase(p.getParamMode()) || p.getParamMode() == null)
                    .collect(Collectors.toList());

            if (!inParams.isEmpty()) {
                queryBuilder.append("$");
                queryBuilder.append(inParams.stream()
                        .map(p -> p.getKey() + ": " + getGraphQLInputType(p.getOracleType()))
                        .collect(Collectors.joining(", ")));
            }
        }

        queryBuilder.append(") ");

        if ("query".equalsIgnoreCase(operationType)) {
            queryBuilder.append("{\n");
            queryBuilder.append("  ").append(operationName).append("(");

            if (api.getParameters() != null) {
                queryBuilder.append(api.getParameters().stream()
                        .filter(p -> "IN".equalsIgnoreCase(p.getParamMode()) || p.getParamMode() == null)
                        .map(p -> p.getKey() + ": $" + p.getKey())
                        .collect(Collectors.joining(", ")));
            }

            queryBuilder.append(") {\n");

            // Add response fields
            if (api.getResponseMappings() != null) {
                queryBuilder.append(api.getResponseMappings().stream()
                        .limit(5)
                        .map(m -> "    " + m.getApiField())
                        .collect(Collectors.joining("\n")));
                if (api.getResponseMappings().size() > 5) {
                    queryBuilder.append("\n    # ... more fields");
                }
            }

            queryBuilder.append("\n  }\n}");
        } else {
            // Mutation
            queryBuilder.append("{\n");
            queryBuilder.append("  ").append(operationName).append("(input: {\n");

            if (api.getParameters() != null) {
                queryBuilder.append(api.getParameters().stream()
                        .filter(p -> "IN".equalsIgnoreCase(p.getParamMode()) || p.getParamMode() == null)
                        .map(p -> "    " + p.getKey() + ": $" + p.getKey())
                        .collect(Collectors.joining(",\n")));
            }

            queryBuilder.append("\n  }) {\n");
            queryBuilder.append("    success\n");
            queryBuilder.append("    message\n");
            queryBuilder.append("    data {\n");

            if (api.getResponseMappings() != null) {
                queryBuilder.append(api.getResponseMappings().stream()
                        .limit(3)
                        .map(m -> "      " + m.getApiField())
                        .collect(Collectors.joining("\n")));
            }

            queryBuilder.append("\n    }\n");
            queryBuilder.append("  }\n}");
        }

        graphqlBody.put("query", queryBuilder.toString());

        // Add variables
        if (api.getParameters() != null) {
            Map<String, Object> variables = new LinkedHashMap<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("IN".equalsIgnoreCase(param.getParamMode()) || param.getParamMode() == null) {
                    variables.put(param.getKey(), param.getExample() != null ? param.getExample() : "");
                }
            }
            if (!variables.isEmpty()) {
                graphqlBody.put("variables", variables);
            }
        }

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(graphqlBody);
        } catch (Exception e) {
            return "{\n  \"query\": \"" + operationType + " " + operationName + " {\\n    id\\n  }\\\"\\n}";
        }
    }

    /**
     * Get GraphQL input type from Oracle type
     */
    private String getGraphQLInputType(String oracleType) {
        if (oracleType == null) return "String";
        String type = oracleType.toUpperCase();
        if (type.contains("NUMBER") || type.contains("INT") || type.contains("FLOAT") || type.contains("DECIMAL")) {
            return "Int";
        }
        if (type.contains("BOOLEAN")) {
            return "Boolean";
        }
        if (type.contains("DATE") || type.contains("TIMESTAMP")) {
            return "DateTime";
        }
        return "String";
    }
}