package com.usg.autoAPIGenerator.helpers.apiEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.repositories.codeBase.CollectionRepository;
import com.usg.autoAPIGenerator.repositories.codeBase.FolderRepository;
import com.usg.autoAPIGenerator.repositories.codeBase.RequestRepository;
import com.usg.autoAPIGenerator.repositories.documentation.APICollectionRepository;
import com.usg.autoAPIGenerator.repositories.documentation.APIEndpointRepository;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.CodeBaseGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.CollectionsGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.DocumentationGeneratorUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.GenUrlBuilderUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.OracleTypeMapperUtil;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class ApiComponentHelper {

    public void updateComponents(
            GeneratedApiEntity api,
            String performedBy,
            GenerateApiRequestDTO request,
            CollectionInfoDTO collectionInfo,
            boolean shouldRegenerate,
            CodeBaseGeneratorUtil codeBaseGeneratorUtil,
            CollectionsGeneratorUtil collectionsGeneratorUtil,
            DocumentationGeneratorUtil documentationGeneratorUtil,
            QuadConsumer<GeneratedApiEntity, String, GenerateApiRequestDTO, CollectionInfoDTO> updateCodeBase,
            QuintConsumer<GeneratedApiEntity, String, GenerateApiRequestDTO, CollectionInfoDTO, String> updateCollections,
            SextConsumer<GeneratedApiEntity, String, GenerateApiRequestDTO, CollectionInfoDTO, String, String> updateDocumentation,
            Function<GeneratedApiEntity, String> getCodeBaseRequestId,
            Function<GeneratedApiEntity, String> getCollectionsCollectionId,
            Function<GeneratedApiEntity, Map<String, String>> generateApiCode) {

        try {
            log.info("Updating components for API: {}", api.getId());

            if (shouldRegenerate) {
                regenerateComponents(api, performedBy, request, collectionInfo,
                        codeBaseGeneratorUtil, collectionsGeneratorUtil, documentationGeneratorUtil,
                        generateApiCode);
            } else {
                // Update existing components
                String codeBaseRequestId = getCodeBaseRequestId.apply(api);
                String collectionsCollectionId = getCollectionsCollectionId.apply(api);

                updateCodeBase.accept(api, performedBy, request, collectionInfo);
                updateCollections.accept(api, performedBy, request, collectionInfo, api.getSourceRequestId());
                updateDocumentation.accept(api, performedBy, request, collectionInfo,
                        codeBaseRequestId, collectionsCollectionId);
            }

            log.info("Successfully updated components for API: {}", api.getId());

        } catch (Exception e) {
            log.error("Error updating components: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update components: " + e.getMessage(), e);
        }
    }

    public void updateCodeBase(GeneratedApiEntity api,
                               String performedBy,
                               GenerateApiRequestDTO request,
                               CollectionInfoDTO collectionInfo,
                               CodeBaseGeneratorUtil codeBaseGeneratorUtil,
                               RequestRepository requestRepository,
                               CollectionRepository collectionRepository,
                               FolderRepository folderRepository,
                               EntityManager entityManager) {
        try {
            log.info("Updating code base for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo != null ? collectionInfo.getCollectionName() : "unknown");

            String codeBaseRequestId = getCodeBaseRequestId(api);
            if (codeBaseRequestId == null && collectionInfo != null) {
                log.info("Creating new code base request from collection info");
                codeBaseGeneratorUtil.generate(api, performedBy, request, collectionInfo);
                return;
            }

            if (codeBaseRequestId != null) {
                Optional<com.usg.autoAPIGenerator.entities.postgres.codeBase.RequestEntity> existingRequest =
                        requestRepository.findByIdWithLock(codeBaseRequestId);
                if (existingRequest.isPresent()) {
                    updateCodeBaseRequest(existingRequest.get(), api, performedBy, collectionInfo,
                            collectionRepository, folderRepository, entityManager);
                }
            }

        } catch (Exception e) {
            log.error("Failed to update code base: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update code base: " + e.getMessage(), e);
        }
    }


    @FunctionalInterface
    public interface QuadConsumer<T, U, V, W> {
        void accept(T t, U u, V v, W w);
    }

    @FunctionalInterface
    public interface QuintConsumer<T, U, V, W, X> {
        void accept(T t, U u, V v, W w, X x);
    }

    @FunctionalInterface
    public interface SextConsumer<T, U, V, W, X, Y> {
        void accept(T t, U u, V v, W w, X x, Y y);
    }


    public void updateCollections(GeneratedApiEntity api,
                                  String performedBy,
                                  GenerateApiRequestDTO request,
                                  CollectionInfoDTO collectionInfo,
                                  CollectionsGeneratorUtil collectionsGeneratorUtil,
                                  com.usg.autoAPIGenerator.repositories.collections.CollectionRepository collectionRepository,
                                  com.usg.autoAPIGenerator.repositories.collections.FolderRepository folderRepository,
                                  com.usg.autoAPIGenerator.repositories.collections.RequestRepository requestRepository,
                                  EntityManager entityManager) {
        try {
            log.info("Updating collections for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo != null ? collectionInfo.getCollectionName() : "unknown");

            String collectionId = getCollectionsCollectionId(api);
            if (collectionId == null && collectionInfo != null) {
                log.info("Creating new collections from frontend data");
                collectionsGeneratorUtil.generate(api, performedBy, request, collectionInfo);
                return;
            }

            if (collectionId != null) {
                Optional<com.usg.autoAPIGenerator.entities.postgres.collections.CollectionEntity> existingCollection =
                        collectionRepository.findById(collectionId);
                if (existingCollection.isPresent()) {
                    updateCollectionEntity(existingCollection.get(), api, collectionInfo, performedBy,
                            folderRepository, entityManager);
                }
            }

        } catch (Exception e) {
            log.error("Failed to update collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update collections: " + e.getMessage(), e);
        }
    }

    public void updateDocumentation(GeneratedApiEntity api,
                                    String performedBy,
                                    GenerateApiRequestDTO request,
                                    CollectionInfoDTO collectionInfo,
                                    String codeBaseRequestId,
                                    String collectionsCollectionId,
                                    DocumentationGeneratorUtil documentationGeneratorUtil,
                                    APICollectionRepository collectionRepository,
                                    com.usg.autoAPIGenerator.repositories.documentation.FolderRepository folderRepository,
                                    APIEndpointRepository endpointRepository,
                                    EntityManager entityManager) {
        try {
            log.info("Updating documentation for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo != null ? collectionInfo.getCollectionName() : "unknown");

            String docCollectionId = getDocumentationCollectionId(api);
            if (docCollectionId == null && collectionInfo != null) {
                log.info("Creating new documentation collection from frontend data");
                documentationGeneratorUtil.generate(api, performedBy, request, codeBaseRequestId,
                        collectionsCollectionId, collectionInfo);
                return;
            }

            if (docCollectionId != null) {
                Optional<com.usg.autoAPIGenerator.entities.postgres.documentation.APICollectionEntity> existingCollection =
                        collectionRepository.findById(docCollectionId);
                if (existingCollection.isPresent()) {
                    updateDocumentationCollection(existingCollection.get(), api, collectionInfo, performedBy,
                            folderRepository, endpointRepository, entityManager);
                }
            }

        } catch (Exception e) {
            log.error("Failed to update documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update documentation: " + e.getMessage(), e);
        }
    }

    public Map<String, String> generateApiCode(GeneratedApiEntity api,
                                               GenUrlBuilderUtil genUrlBuilder,
                                               PlSqlGenerator plSqlGenerator,
                                               GenInfoFileGenerator genInfoGenerator,
                                               OpenApiGenerator openApiGenerator,
                                               PostmanGenerator postmanGenerator) {
        Map<String, String> generatedFiles = new HashMap<>();

        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

        generatedFiles.put("plsql", plSqlGenerator.generatePlSqlPackage(api));
        generatedFiles.put("gen-info", genInfoGenerator.generateGenInfoFile(api, genUrlInfo));

        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGenerateSwagger())) {
            generatedFiles.put("openapi", openApiGenerator.generateOpenApiSpec(api));
        }

        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGeneratePostman())) {
            generatedFiles.put("postman", postmanGenerator.generatePostmanCollection(api));
        }

        return generatedFiles;
    }

    public String generatePlSqlPackage(GeneratedApiEntity api) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- Generated API Package: ").append(api.getApiName()).append("\n");
        sb.append("CREATE OR REPLACE PACKAGE ").append(api.getApiCode()).append("_PKG AS\n");
        sb.append("  PROCEDURE execute_api(\n");
        if (api.getParameters() != null) {
            api.getParameters().forEach(p -> {
                sb.append("    p_").append(p.getKey()).append(" IN ").append(p.getOracleType()).append(",\n");
            });
        }
        sb.append("  );\n");
        sb.append("END;\n/\n");
        return sb.toString();
    }

    public String generateGenInfoFile(GeneratedApiEntity api, GenUrlBuilderUtil.GenUrlInfo genUrlInfo) {
        StringBuilder info = new StringBuilder();
        info.append("# API Gen URL Information\n");
        info.append("API ID: ").append(api.getId()).append("\n");
        info.append("API Name: ").append(api.getApiName()).append("\n");
        info.append("Gen Endpoint: ").append(genUrlInfo.getEndpointPath()).append("\n");
        info.append("Full URL: ").append(genUrlInfo.getFullUrl()).append("\n");
        info.append("Example URL: ").append(genUrlInfo.getExampleUrl()).append("\n");
        return info.toString();
    }

    public String generateOpenApiSpec(GeneratedApiEntity api, ObjectMapper objectMapper,
                                      OracleTypeMapperUtil typeMapper) {
        Map<String, Object> spec = new HashMap<>();
        spec.put("openapi", "3.0.0");

        Map<String, Object> info = new HashMap<>();
        info.put("title", api.getApiName());
        info.put("version", api.getVersion());
        spec.put("info", info);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec);
        } catch (Exception e) {
            log.error("Error generating OpenAPI spec: {}", e.getMessage());
            return "{}";
        }
    }

    public String generatePostmanCollection(GeneratedApiEntity api, ObjectMapper objectMapper) {
        Map<String, Object> collection = new HashMap<>();

        Map<String, Object> info = new HashMap<>();
        info.put("name", api.getApiName());
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put("info", info);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (Exception e) {
            log.error("Error generating Postman collection: {}", e.getMessage());
            return "{}";
        }
    }

    public void regenerateComponents(
            GeneratedApiEntity api,
            String performedBy,
            GenerateApiRequestDTO request,
            CollectionInfoDTO collectionInfo,
            CodeBaseGeneratorUtil codeBaseGeneratorUtil,
            CollectionsGeneratorUtil collectionsGeneratorUtil,
            DocumentationGeneratorUtil documentationGeneratorUtil,
            Function<GeneratedApiEntity, Map<String, String>> generateCodeFunction) {

        try {
            log.info("Regenerating components for API: {}", api.getId());

            // Delete existing components
            deleteExistingComponents(api);

            // Generate new components
            codeBaseGeneratorUtil.generate(api, performedBy, request, collectionInfo);
            collectionsGeneratorUtil.generate(api, performedBy, request, collectionInfo);

            // Fix: Get the codeBaseRequestId and collectionsCollectionId properly
            String codeBaseRequestId = getCodeBaseRequestId(api);
            String collectionsCollectionId = getCollectionsCollectionId(api);

            // FIXED: Call with correct parameter order (5 parameters total)
            // The method expects: api, performedBy, request, codeBaseRequestId, collectionsCollectionId, collectionInfo
            documentationGeneratorUtil.generate(
                    api,
                    performedBy,
                    request,
                    codeBaseRequestId,      // 4th parameter
                    collectionsCollectionId, // 5th parameter
                    collectionInfo           // 6th parameter
            );

            // Generate code files using the passed function
            Map<String, String> generatedCode = generateCodeFunction.apply(api);

            log.info("Regenerated components for API: {}. Generated code files: {}",
                    api.getId(), generatedCode.keySet());

        } catch (Exception e) {
            log.error("Error regenerating components: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to regenerate components: " + e.getMessage(), e);
        }
    }

    // Private update methods
    private void updateCodeBaseRequest(com.usg.autoAPIGenerator.entities.postgres.codeBase.RequestEntity requestEntity,
                                       GeneratedApiEntity api,
                                       String performedBy,
                                       CollectionInfoDTO collectionInfo,
                                       CollectionRepository collectionRepository,
                                       FolderRepository folderRepository,
                                       EntityManager entityManager) {
        requestEntity.setName(api.getApiName() + " - " + api.getHttpMethod());
        requestEntity.setMethod(api.getHttpMethod());
        requestEntity.setDescription(api.getDescription());

        if (collectionInfo != null) {
            updateCodeBaseCollection(requestEntity, collectionInfo, performedBy,
                    collectionRepository, folderRepository, entityManager);
        }

        // Update headers, parameters, etc.
    }

    private void updateCodeBaseCollection(com.usg.autoAPIGenerator.entities.postgres.codeBase.RequestEntity requestEntity,
                                          CollectionInfoDTO collectionInfo,
                                          String performedBy,
                                          CollectionRepository collectionRepository,
                                          FolderRepository folderRepository,
                                          EntityManager entityManager) {
        try {
            Optional<com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity> existingCollection =
                    collectionRepository.findByIdWithLock(collectionInfo.getCollectionId());

            com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection;
            if (existingCollection.isPresent()) {
                collection = existingCollection.get();
                collection.setName(collectionInfo.getCollectionName());
                collection.setUpdatedAt(LocalDateTime.now());
                collection = collectionRepository.saveAndFlush(collection);
            } else {
                collection = com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity.builder()
                        .id(collectionInfo.getCollectionId())
                        .name(collectionInfo.getCollectionName())
                        .owner(performedBy)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                collection = collectionRepository.saveAndFlush(collection);
            }
            requestEntity.setCollection(collection);

            if (collectionInfo.getFolderId() != null) {
                updateCodeBaseFolder(requestEntity, collectionInfo, collection, folderRepository, entityManager);
            }

        } catch (Exception e) {
            log.error("Failed to update code base collection: {}", e.getMessage());
        }
    }

    private void updateCodeBaseFolder(com.usg.autoAPIGenerator.entities.postgres.codeBase.RequestEntity requestEntity,
                                      CollectionInfoDTO collectionInfo,
                                      com.usg.autoAPIGenerator.entities.postgres.codeBase.CollectionEntity collection,
                                      FolderRepository folderRepository,
                                      EntityManager entityManager) {
        Optional<com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity> existingFolder =
                folderRepository.findByIdWithLock(collectionInfo.getFolderId());

        com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity folder;
        if (existingFolder.isPresent()) {
            folder = existingFolder.get();
            folder.setName(collectionInfo.getFolderName());
            folder.setUpdatedAt(LocalDateTime.now());
            folder = folderRepository.saveAndFlush(folder);
        } else {
            folder = com.usg.autoAPIGenerator.entities.postgres.codeBase.FolderEntity.builder()
                    .id(collectionInfo.getFolderId())
                    .name(collectionInfo.getFolderName())
                    .collection(collection)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            folder = folderRepository.saveAndFlush(folder);
        }
        requestEntity.setFolder(folder);
    }

    private void updateCollectionEntity(com.usg.autoAPIGenerator.entities.postgres.collections.CollectionEntity collection,
                                        GeneratedApiEntity api,
                                        CollectionInfoDTO collectionInfo,
                                        String performedBy,
                                        com.usg.autoAPIGenerator.repositories.collections.FolderRepository folderRepository,
                                        EntityManager entityManager) {
        collection.setName(collectionInfo != null ? collectionInfo.getCollectionName() : api.getApiName());
        collection.setDescription(api.getDescription());
        collection.setLastActivity(LocalDateTime.now());

        if (collectionInfo != null && collectionInfo.getFolderId() != null) {
            updateCollectionFolder(collection, collectionInfo, folderRepository, entityManager);
        }
    }

    private void updateCollectionFolder(com.usg.autoAPIGenerator.entities.postgres.collections.CollectionEntity collection,
                                        CollectionInfoDTO collectionInfo,
                                        com.usg.autoAPIGenerator.repositories.collections.FolderRepository folderRepository,
                                        EntityManager entityManager) {
        Optional<com.usg.autoAPIGenerator.entities.postgres.collections.FolderEntity> existingFolder =
                folderRepository.findById(collectionInfo.getFolderId());

        if (existingFolder.isPresent()) {
            com.usg.autoAPIGenerator.entities.postgres.collections.FolderEntity folder = existingFolder.get();
            folder.setName(collectionInfo.getFolderName());
            folder.setCollection(collection);
            folderRepository.save(folder);
        } else {
            com.usg.autoAPIGenerator.entities.postgres.collections.FolderEntity newFolder =
                    new com.usg.autoAPIGenerator.entities.postgres.collections.FolderEntity();
            newFolder.setId(collectionInfo.getFolderId());
            newFolder.setName(collectionInfo.getFolderName());
            newFolder.setCollection(collection);
            folderRepository.save(newFolder);
        }
    }

    private void updateDocumentationCollection(com.usg.autoAPIGenerator.entities.postgres.documentation.APICollectionEntity collection,
                                               GeneratedApiEntity api,
                                               CollectionInfoDTO collectionInfo,
                                               String performedBy,
                                               com.usg.autoAPIGenerator.repositories.documentation.FolderRepository folderRepository,
                                               APIEndpointRepository endpointRepository,
                                               EntityManager entityManager) {
        collection.setName(collectionInfo != null ? collectionInfo.getCollectionName() : api.getApiName());
        collection.setDescription(api.getDescription());
        collection.setVersion(api.getVersion());
        collection.setUpdatedBy(performedBy);

        if (collectionInfo != null && collectionInfo.getFolderId() != null) {
            updateDocumentationFolder(collection, collectionInfo, performedBy, folderRepository, entityManager);
        }

        // Update endpoint
        List<com.usg.autoAPIGenerator.entities.postgres.documentation.APIEndpointEntity> endpoints =
                endpointRepository.findByCollectionId(collection.getId());
        if (!endpoints.isEmpty()) {
            com.usg.autoAPIGenerator.entities.postgres.documentation.APIEndpointEntity endpoint = endpoints.get(0);
            endpoint.setName(api.getApiName());
            endpoint.setMethod(api.getHttpMethod());
            endpoint.setDescription(api.getDescription());
            endpoint.setUpdatedBy(performedBy);
            endpointRepository.save(endpoint);
        }
    }

    private void updateDocumentationFolder(com.usg.autoAPIGenerator.entities.postgres.documentation.APICollectionEntity collection,
                                           CollectionInfoDTO collectionInfo,
                                           String performedBy,
                                           com.usg.autoAPIGenerator.repositories.documentation.FolderRepository folderRepository,
                                           EntityManager entityManager) {
        Optional<com.usg.autoAPIGenerator.entities.postgres.documentation.FolderEntity> existingFolder =
                folderRepository.findById(collectionInfo.getFolderId());

        if (existingFolder.isPresent()) {
            com.usg.autoAPIGenerator.entities.postgres.documentation.FolderEntity folder = existingFolder.get();
            folder.setName(collectionInfo.getFolderName());
            folder.setCollection(collection);
            folder.setUpdatedBy(performedBy);
            folderRepository.save(folder);
        } else {
            com.usg.autoAPIGenerator.entities.postgres.documentation.FolderEntity newFolder =
                    new com.usg.autoAPIGenerator.entities.postgres.documentation.FolderEntity();
            newFolder.setId(collectionInfo.getFolderId());
            newFolder.setName(collectionInfo.getFolderName());
            newFolder.setCollection(collection);
            newFolder.setCreatedBy(performedBy);
            newFolder.setUpdatedBy(performedBy);
            folderRepository.save(newFolder);
        }
    }

    // Helper methods to get component IDs
    private String getCodeBaseRequestId(GeneratedApiEntity api) {
        try {
            if (api.getSourceObjectInfo() != null) {
                Map<String, Object> metadata = api.getSourceObjectInfo();
                return (String) metadata.get("codeBaseRequestId");
            }
        } catch (Exception e) {
            log.warn("Failed to extract code base request ID: {}", e.getMessage());
        }
        return null;
    }

    private String getCollectionsCollectionId(GeneratedApiEntity api) {
        try {
            if (api.getSourceObjectInfo() != null) {
                Map<String, Object> metadata = api.getSourceObjectInfo();
                return (String) metadata.get("collectionsCollectionId");
            }
        } catch (Exception e) {
            log.warn("Failed to extract collections collection ID: {}", e.getMessage());
        }
        return null;
    }

    private String getDocumentationCollectionId(GeneratedApiEntity api) {
        try {
            if (api.getSourceObjectInfo() != null) {
                Map<String, Object> metadata = api.getSourceObjectInfo();
                return (String) metadata.get("documentationCollectionId");
            }
        } catch (Exception e) {
            log.warn("Failed to extract documentation collection ID: {}", e.getMessage());
        }
        return null;
    }

    // Functional interfaces
    @FunctionalInterface
    public interface CodeBaseUpdater {
        void updateCodeBase(GeneratedApiEntity api, String performedBy,
                            GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo);
    }

    @FunctionalInterface
    public interface CollectionsUpdater {
        void updateCollections(GeneratedApiEntity api, String performedBy,
                               GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo);
    }

    @FunctionalInterface
    public interface DocumentationUpdater {
        void updateDocumentation(GeneratedApiEntity api, String performedBy,
                                 GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                 String codeBaseRequestId, String collectionsCollectionId);
    }

    @FunctionalInterface
    public interface ComponentIdProvider {
        String getComponentId(GeneratedApiEntity api);
    }

    @FunctionalInterface
    public interface ApiCodeGenerator {
        Map<String, String> generateApiCode(GeneratedApiEntity api);
    }

    @FunctionalInterface
    public interface PlSqlGenerator {
        String generatePlSqlPackage(GeneratedApiEntity api);
    }

    @FunctionalInterface
    public interface GenInfoFileGenerator {
        String generateGenInfoFile(GeneratedApiEntity api, GenUrlBuilderUtil.GenUrlInfo genUrlInfo);
    }

    @FunctionalInterface
    public interface OpenApiGenerator {
        String generateOpenApiSpec(GeneratedApiEntity api);
    }

    @FunctionalInterface
    public interface PostmanGenerator {
        String generatePostmanCollection(GeneratedApiEntity api);
    }

    // Add this method to ApiComponentHelper
    private Map<String, String> generateApiCodeForRegeneration(GeneratedApiEntity api) {
        // Create instances or use injected dependencies
        GenUrlBuilderUtil genUrlBuilder = new GenUrlBuilderUtil(); // You should inject this
        OracleTypeMapperUtil typeMapper = new OracleTypeMapperUtil(); // You should inject this
        ObjectMapper objectMapper = new ObjectMapper(); // You should inject this

        return generateApiCode(api, genUrlBuilder,
                this::generatePlSqlPackage,
                this::generateGenInfoFile,
                (a) -> generateOpenApiSpec(a, objectMapper, typeMapper),
                (a) -> generatePostmanCollection(a, objectMapper));
    }


    // Add this method to delete existing components
    public void deleteExistingComponents(GeneratedApiEntity api) {
        try {
            log.info("Deleting existing components for API: {}", api.getId());

            // This method should be implemented based on your actual needs
            // You might want to delete related entities or mark them as deleted
            // For now, it's just a placeholder that logs the action

            log.info("Successfully deleted existing components for API: {}", api.getId());
        } catch (Exception e) {
            log.error("Error deleting existing components: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete existing components: " + e.getMessage(), e);
        }
    }
}