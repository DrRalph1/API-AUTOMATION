package com.usg.apiAutomation.utils.apiEngine.generator;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.documentation.*;
import com.usg.apiAutomation.repositories.documentation.*;
import com.usg.apiAutomation.utils.apiEngine.CodeLanguageGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.GenUrlBuilderUtil;
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
public class DocumentationGeneratorUtil {

    private final APICollectionRepository docCollectionRepository;
    private final FolderRepository docFolderRepository;
    private final APIEndpointRepository endpointRepository;
    private final HeaderRepository docHeaderRepository;
    private final ParameterRepository docParameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;
    private final DocumentationSettingsRepository documentationSettingsRepository;
    private final EnvironmentRepository documentationEnvironmentRepository;
    private final NotificationRepository notificationRepository;
    private final PublishedDocumentationRepository publishedDocumentationRepository;
    private final SearchHistoryRepository documentationSearchHistoryRepository;
    private final MockServerRepository mockServerRepository;
    private final MockEndpointRepository mockEndpointRepository;
    private final GenUrlBuilderUtil genUrlBuilder;
    private final CodeLanguageGeneratorUtil codeLanguageGeneratorUtil;
    private final EntityManager entityManager;

    @Transactional
    public String generate(GeneratedApiEntity api, String performedBy,
                           GenerateApiRequestDTO request,
                           String codeBaseRequestId,
                           String collectionsCollectionId,
                           CollectionInfoDTO collectionInfo) {
        try {
            log.info("Generating Documentation for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo.getCollectionName());

            String generatedApiId = api.getId();

            // CRITICAL: Don't do any operations that might trigger auto-flush before we're ready
            // Don't call entityManager.flush() or entityManager.clear() here

            // ============ API COLLECTION ENTITY ============
            APICollectionEntity docCollection = createOrUpdateCollection(api, performedBy, collectionInfo, generatedApiId);

            // ============ DOCUMENTATION SETTINGS ENTITY ============
            createDocumentationSettings(api, performedBy, generatedApiId);

            // ============ ENVIRONMENT ENTITIES ============
            createEnvironments(api, performedBy, generatedApiId);

            // ============ MOCK SERVER ENTITIES ============
            MockServerEntity mockServer = createOrUpdateMockServer(api, performedBy, docCollection.getId(), generatedApiId);

            // ============ FOLDER ENTITY ============
            com.usg.apiAutomation.entities.postgres.documentation.FolderEntity docFolder =
                    createOrUpdateFolder(api, performedBy, collectionInfo, docCollection, generatedApiId);

            // ============ ENDPOINT ENTITY ============
            APIEndpointEntity endpoint = createEndpoint(api, performedBy, docCollection, docFolder, generatedApiId);

            // ============ MOCK ENDPOINT ENTITY ============
            createOrUpdateMockEndpoint(api, endpoint, mockServer, generatedApiId);

            // ============ HEADER ENTITIES ============
            createHeaderEntities(api, endpoint, generatedApiId);

            // ============ PARAMETER ENTITIES ============
            createParameterEntities(api, endpoint, generatedApiId);

            // ============ RESPONSE EXAMPLE ENTITIES ============
            createResponseExamples(api, endpoint, generatedApiId);

            // ============ CODE EXAMPLE ENTITIES ============
            generateCodeExamples(api, endpoint, codeBaseRequestId, generatedApiId);

            // ============ CHANGELOG ENTITY ============
            createChangelog(api, performedBy, docCollection, endpoint, generatedApiId);

            // ============ PUBLISHED DOCUMENTATION ENTITY ============
            createPublishedDocumentation(api, performedBy, docCollection, generatedApiId);

            // ============ NOTIFICATION ENTITIES ============
            createNotifications(api, performedBy, generatedApiId, docCollection.getId(), endpoint.getId());

            // ============ SEARCH HISTORY ENTITY ============
            createSearchHistory(api, performedBy, generatedApiId);

            // Update collection counts
            docCollection.setTotalEndpoints(1);
            docCollection.setTotalFolders(1);
            docCollectionRepository.save(docCollection);

            log.info("Documentation generated successfully with Collection ID: {} using Folder: {}",
                    docCollection.getId(), collectionInfo.getFolderName());

            return docCollection.getId();

        } catch (Exception e) {
            log.error("Error generating Documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Documentation: " + e.getMessage(), e);
        }
    }

    /**
     * Create or update a mock server for the collection - FIXED to avoid entity graph issues
     */
    private MockServerEntity createOrUpdateMockServer(GeneratedApiEntity api, String performedBy,
                                                      String collectionId, String generatedApiId) {
        // Use a simple count query first to avoid loading collections
        long count = mockServerRepository.countByCollectionId(collectionId);

        MockServerEntity mockServer;

        if (count > 0) {
            // Load only what we need
            List<MockServerEntity> existingMockServers = mockServerRepository
                    .findByCollectionIdWithoutCollections(collectionId);

            if (existingMockServers.size() > 1) {
                // Delete duplicates
                for (int i = 1; i < existingMockServers.size(); i++) {
                    mockServerRepository.delete(existingMockServers.get(i));
                }
                mockServerRepository.flush();
            }

            mockServer = existingMockServers.get(0);

            // Reload with minimal associations
            if (mockServer.getCollection() == null || !entityManager.contains(mockServer.getCollection())) {
                APICollectionEntity collection = docCollectionRepository.findById(collectionId).orElseThrow();
                mockServer.setCollection(collection);
            }

            mockServer.setActive(true);
            mockServer.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new
            mockServer = new MockServerEntity();
            mockServer.setId(UUID.randomUUID().toString());
            mockServer.setGeneratedApiId(generatedApiId);

            APICollectionEntity collection = docCollectionRepository.findById(collectionId).orElseThrow();
            mockServer.setCollection(collection);

            mockServer.setMockServerUrl("https://mock." + api.getApiCode().toLowerCase() + ".example.com");
            mockServer.setActive(true);
            mockServer.setDescription("Mock server for " + api.getApiName());
            mockServer.setCreatedBy(performedBy);
            mockServer.setCreatedAt(LocalDateTime.now());
            mockServer.setExpiresAt(LocalDateTime.now().plusDays(30));
        }

        return mockServerRepository.save(mockServer);
    }

    /**
     * Create or update a mock endpoint - FIXED to handle detached entities
     */
    private void createOrUpdateMockEndpoint(GeneratedApiEntity api, APIEndpointEntity endpoint,
                                            MockServerEntity mockServer, String generatedApiId) {
        // Ensure endpoint is managed
        APIEndpointEntity managedEndpoint;
        if (!entityManager.contains(endpoint)) {
            managedEndpoint = entityManager.merge(endpoint);
        } else {
            managedEndpoint = endpoint;
        }

        // Ensure mockServer is managed
        MockServerEntity managedMockServer;
        if (!entityManager.contains(mockServer)) {
            managedMockServer = entityManager.merge(mockServer);
        } else {
            managedMockServer = mockServer;
        }

        List<MockEndpointEntity> existingMockEndpoints = mockEndpointRepository
                .findBySourceEndpointId(managedEndpoint.getId());

        MockEndpointEntity mockEndpoint;
        if (existingMockEndpoints != null && !existingMockEndpoints.isEmpty()) {
            mockEndpoint = existingMockEndpoints.get(0);
            // Ensure the mock endpoint is managed
            if (!entityManager.contains(mockEndpoint)) {
                mockEndpoint = entityManager.merge(mockEndpoint);
            }
            mockEndpoint.setMethod(api.getHttpMethod());
            mockEndpoint.setPath(managedEndpoint.getUrl());
            mockEndpoint.setDescription("Mock endpoint for " + api.getApiName() + " (updated)");
            mockEndpoint.setEnabled(true);
            mockEndpoint.setMockServer(managedMockServer);
            log.debug("Updating existing mock endpoint for endpoint: {}", managedEndpoint.getId());

            // Delete any additional mock endpoints
            if (existingMockEndpoints.size() > 1) {
                for (int i = 1; i < existingMockEndpoints.size(); i++) {
                    MockEndpointEntity duplicate = existingMockEndpoints.get(i);
                    // Ensure the duplicate is managed before deletion
                    if (!entityManager.contains(duplicate)) {
                        duplicate = entityManager.merge(duplicate);
                    }
                    mockEndpointRepository.delete(duplicate);
                    log.debug("Deleted duplicate mock endpoint: {}", duplicate.getId());
                }
            }
        } else {
            mockEndpoint = new MockEndpointEntity();
            mockEndpoint.setId(UUID.randomUUID().toString());
            mockEndpoint.setGeneratedApiId(generatedApiId);
            mockEndpoint.setMethod(api.getHttpMethod());
            mockEndpoint.setPath(managedEndpoint.getUrl());
            mockEndpoint.setStatusCode(200);
            mockEndpoint.setResponseDelay(0);
            mockEndpoint.setDescription("Mock endpoint for " + api.getApiName());
            mockEndpoint.setEnabled(true);
            mockEndpoint.setMockServer(managedMockServer);
            mockEndpoint.setSourceEndpoint(managedEndpoint);
            log.debug("Creating new mock endpoint for endpoint: {}", managedEndpoint.getId());
        }

        // Set response body from success schema
        if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            try {
                Map<String, Object> responseBody = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        api.getResponseConfig().getSuccessSchema(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                mockEndpoint.setResponseBody(responseBody);
            } catch (Exception e) {
                log.warn("Failed to parse response body for mock endpoint: {}", e.getMessage());
            }
        }

        mockEndpointRepository.save(mockEndpoint);
        log.debug("Saved MockEndpoint: {}", mockEndpoint.getId());
    }

    private APICollectionEntity createOrUpdateCollection(GeneratedApiEntity api, String performedBy,
                                                         CollectionInfoDTO collectionInfo, String generatedApiId) {
        Optional<APICollectionEntity> existing = docCollectionRepository
                .findById(collectionInfo.getCollectionId());

        APICollectionEntity collection;
        if (existing.isPresent()) {
            collection = existing.get();
            collection.setName(collectionInfo.getCollectionName());
            collection.setDescription(api.getDescription());
            collection.setVersion(api.getVersion());
            collection.setTags(api.getTags() != null ? new ArrayList<>(api.getTags()) : new ArrayList<>());
            collection.setUpdatedBy(performedBy);
            if (collection.getGeneratedApiId() == null) {
                collection.setGeneratedApiId(generatedApiId);
            }
        } else {
            collection = new APICollectionEntity();
            collection.setId(collectionInfo.getCollectionId());
            collection.setGeneratedApiId(generatedApiId);
            collection.setName(collectionInfo.getCollectionName());
            collection.setDescription(api.getDescription());
            collection.setVersion(api.getVersion());
            collection.setOwner(performedBy);
            collection.setType("REST");
            collection.setFavorite(false);
            collection.setExpanded(false);
            collection.setColor(getRandomColor());
            collection.setStatus("published");
            collection.setTags(api.getTags() != null ? new ArrayList<>(api.getTags()) : new ArrayList<>());
            collection.setCreatedBy(performedBy);
            collection.setUpdatedBy(performedBy);
            collection.setTotalEndpoints(0);
            collection.setTotalFolders(0);
            collection.setCreatedAt(LocalDateTime.now());
            collection.setUpdatedAt(LocalDateTime.now());
        }

        return docCollectionRepository.save(collection);
    }

    private void createDocumentationSettings(GeneratedApiEntity api, String performedBy, String generatedApiId) {
        DocumentationSettingsEntity settings = new DocumentationSettingsEntity();
        settings.setId(UUID.randomUUID().toString());
        settings.setGeneratedApiId(generatedApiId);
        settings.setUserId(performedBy);
        settings.setAutoSave(true);
        settings.setDarkMode(false);
        settings.setDefaultLanguage("java");
        settings.setDefaultEnvironment("development");
        settings.setShowLineNumbers(true);
        settings.setWordWrap(true);
        settings.setFontSize(14);
        settings.setFontFamily("Monaco");
        settings.setTheme("light");
        settings.setShowSidebar(true);
        settings.setCompactMode(false);

        Map<String, Object> customSettings = new HashMap<>();
        customSettings.put("apiCode", api.getApiCode());
        customSettings.put("apiName", api.getApiName());
        settings.setCustomSettings(customSettings);

        documentationSettingsRepository.save(settings);
        log.debug("Created DocumentationSettings: {}", settings.getId());
    }

    private void createEnvironments(GeneratedApiEntity api, String performedBy, String generatedApiId) {
        // Development Environment
        EnvironmentEntity devEnv = new EnvironmentEntity();
        devEnv.setId(UUID.randomUUID().toString());
        devEnv.setGeneratedApiId(generatedApiId);
        devEnv.setName("Development");
        devEnv.setBaseUrl("http://localhost:8080");
        devEnv.setActive(true);
        devEnv.setDescription("Local development environment");
        devEnv.setCreatedBy(performedBy);
        devEnv.setCreatedAt(LocalDateTime.now());
        devEnv.setUpdatedAt(LocalDateTime.now());

        Map<String, Object> devVars = new HashMap<>();
        devVars.put("apiKey", "dev-key-123");
        devVars.put("timeout", 30000);
        devEnv.setVariables(devVars);

        documentationEnvironmentRepository.save(devEnv);

        // Production Environment
        EnvironmentEntity prodEnv = new EnvironmentEntity();
        prodEnv.setId(UUID.randomUUID().toString());
        prodEnv.setGeneratedApiId(generatedApiId);
        prodEnv.setName("Production");
        prodEnv.setBaseUrl("{{baseUrl}}");
        prodEnv.setActive(false);
        prodEnv.setDescription("Production environment");
        prodEnv.setCreatedBy(performedBy);
        prodEnv.setCreatedAt(LocalDateTime.now());
        prodEnv.setUpdatedAt(LocalDateTime.now());

        Map<String, Object> prodVars = new HashMap<>();
        prodVars.put("apiKey", "{{productionApiKey}}");
        prodVars.put("timeout", 10000);
        prodEnv.setVariables(prodVars);

        documentationEnvironmentRepository.save(prodEnv);

        log.debug("Created documentation environments");
    }

    private com.usg.apiAutomation.entities.postgres.documentation.FolderEntity createOrUpdateFolder(
            GeneratedApiEntity api, String performedBy, CollectionInfoDTO collectionInfo,
            APICollectionEntity collection, String generatedApiId) {

        Optional<com.usg.apiAutomation.entities.postgres.documentation.FolderEntity> existing =
                docFolderRepository.findById(collectionInfo.getFolderId());

        com.usg.apiAutomation.entities.postgres.documentation.FolderEntity folder;
        if (existing.isPresent()) {
            folder = existing.get();
            folder.setName(collectionInfo.getFolderName());
            folder.setUpdatedBy(performedBy);
            if (folder.getGeneratedApiId() == null) {
                folder.setGeneratedApiId(generatedApiId);
            }
        } else {
            folder = new com.usg.apiAutomation.entities.postgres.documentation.FolderEntity();
            folder.setId(collectionInfo.getFolderId());
            folder.setGeneratedApiId(generatedApiId);
            folder.setName(collectionInfo.getFolderName());
            folder.setDescription("Folder for " + collectionInfo.getFolderName());
            folder.setCollection(collection);
            folder.setDisplayOrder(1);
            folder.setCreatedBy(performedBy);
            folder.setUpdatedBy(performedBy);
            folder.setCreatedAt(LocalDateTime.now());
            folder.setUpdatedAt(LocalDateTime.now());
        }

        return docFolderRepository.save(folder);
    }

    private APIEndpointEntity createEndpoint(GeneratedApiEntity api, String performedBy,
                                             APICollectionEntity collection,
                                             com.usg.apiAutomation.entities.postgres.documentation.FolderEntity folder,
                                             String generatedApiId) {

        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

        APIEndpointEntity endpoint = new APIEndpointEntity();
        endpoint.setId(UUID.randomUUID().toString());
        endpoint.setGeneratedApiId(generatedApiId);
        endpoint.setName(api.getApiName());
        endpoint.setMethod(api.getHttpMethod());
        endpoint.setUrl(genUrlInfo.getFullUrl());
        endpoint.setDescription(api.getDescription());
        endpoint.setCollection(collection);
        endpoint.setFolder(folder);
        endpoint.setApiVersion(api.getVersion());
        endpoint.setRequiresAuth(api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()));
        endpoint.setDeprecated(false);
        endpoint.setCategory(api.getCategory());
        endpoint.setTags(api.getTags() != null ? new ArrayList<>(api.getTags()) : new ArrayList<>());
        endpoint.setCreatedBy(performedBy);
        endpoint.setUpdatedBy(performedBy);
        endpoint.setLastModifiedBy(performedBy);
        endpoint.setCreatedAt(LocalDateTime.now());
        endpoint.setUpdatedAt(LocalDateTime.now());

        // Add metadata
        Map<String, Object> endpointMetadata = new HashMap<>();
        endpointMetadata.put("genPath", genUrlInfo.getEndpointPath());
        endpointMetadata.put("apiId", api.getId());
        endpointMetadata.put("fullGenUrl", genUrlInfo.getFullUrl());
        endpointMetadata.put("genUrlPattern", genUrlInfo.getUrlPattern());
        endpointMetadata.put("exampleGenUrl", genUrlInfo.getExampleUrl());
        endpoint.setMetaData(endpointMetadata);

        // Set rate limit
        if (api.getSettings() != null && api.getSettings().getEnableRateLimiting() != null) {
            Map<String, Object> rateLimit = new HashMap<>();
            rateLimit.put("enabled", api.getSettings().getEnableRateLimiting());
            rateLimit.put("requestsPerMinute", api.getSettings().getRateLimit() != null ?
                    api.getSettings().getRateLimit() : 60);
            rateLimit.put("strategy", "token_bucket");
            endpoint.setRateLimit(rateLimit);
        }

        // Set request body example
        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            try {
                Map<String, Object> bodyMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        api.getRequestConfig().getSample(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                endpoint.setRequestBodyExample(bodyMap);
            } catch (Exception e) {
                log.warn("Failed to parse request body example: {}", e.getMessage());
            }
        }

        return endpointRepository.save(endpoint);
    }

    private void createHeaderEntities(GeneratedApiEntity api, APIEndpointEntity endpoint, String generatedApiId) {
        // Add headers from headers array
        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                    // Skip headers with null keys
                    if (apiHeader.getKey() == null || apiHeader.getKey().trim().isEmpty()) {
                        log.warn("Skipping header with null/empty key for API: {}", api.getApiCode());
                        continue;
                    }

                    com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity header =
                            new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();
                    header.setId(UUID.randomUUID().toString());
                    header.setGeneratedApiId(generatedApiId);
                    header.setKey(apiHeader.getKey());
                    header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                    header.setDescription(apiHeader.getDescription() != null ? apiHeader.getDescription() : "");
                    header.setRequired(apiHeader.getRequired() != null ? apiHeader.getRequired() : false);
                    header.setEndpoint(endpoint);
                    docHeaderRepository.save(header);
                }
            }
        }

        // Add auth header
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity authHeader =
                    new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();
            authHeader.setId(UUID.randomUUID().toString());
            authHeader.setGeneratedApiId(generatedApiId);
            authHeader.setRequired(true);
            authHeader.setEndpoint(endpoint);

            // Set default values
            String authType = api.getAuthConfig().getAuthType();

            switch (authType) {
                case "API_KEY":
                    authHeader.setKey(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                    authHeader.setValue("Your API Key");
                    authHeader.setDescription("API Key for authentication");
                    break;
                case "BEARER":
                case "JWT":
                    authHeader.setKey("Authorization");
                    authHeader.setValue("Bearer YOUR_JWT_TOKEN");
                    authHeader.setDescription("Bearer token authentication");
                    break;
                case "BASIC":
                    authHeader.setKey("Authorization");
                    authHeader.setValue("Basic base64_encoded_credentials");
                    authHeader.setDescription("Basic authentication");
                    break;
                case "ORACLE_ROLES":
                    authHeader.setKey("X-Oracle-Session");
                    authHeader.setValue("Your Oracle Session ID");
                    authHeader.setDescription("Oracle Database Session ID");
                    break;
                default:
                    log.warn("Unknown auth type: {}, using default Authorization header", authType);
                    authHeader.setKey("Authorization");
                    authHeader.setValue("Bearer YOUR_TOKEN");
                    authHeader.setDescription("Authentication header");
                    break;
            }

            docHeaderRepository.save(authHeader);
        }
    }

    private void createParameterEntities(GeneratedApiEntity api, APIEndpointEntity endpoint, String generatedApiId) {
        if (api.getParameters() == null || api.getParameters().isEmpty()) {
            return;
        }

        for (ApiParameterEntity apiParam : api.getParameters()) {
            if ("header".equals(apiParam.getParameterType())) {
                continue; // Headers already handled separately
            }

            com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity param =
                    new com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity();

            param.setId(UUID.randomUUID().toString());
            param.setGeneratedApiId(generatedApiId);
            param.setName(apiParam.getKey());
            param.setKey(apiParam.getKey());
            param.setDbColumn(apiParam.getDbColumn());
            param.setDbParameter(apiParam.getDbParameter());
            param.setParameterType(apiParam.getApiType() != null ? apiParam.getApiType() : "string");
            param.setOracleType(apiParam.getOracleType());
            param.setApiType(apiParam.getApiType());
            param.setParameterLocation(apiParam.getParameterLocation() != null ?
                    apiParam.getParameterLocation() : "query");
            param.setRequired(apiParam.getRequired() != null ? apiParam.getRequired() : false);
            param.setDescription(apiParam.getDescription());
            param.setDefaultValue(apiParam.getDefaultValue());
            param.setExample(apiParam.getExample());
            param.setValue(apiParam.getDefaultValue());
            param.setValidationPattern(apiParam.getValidationPattern());
            param.setInBody(apiParam.getInBody() != null ? apiParam.getInBody() : false);
            param.setIsPrimaryKey(apiParam.getIsPrimaryKey() != null ? apiParam.getIsPrimaryKey() : false);
            param.setParamMode(apiParam.getParamMode() != null ? apiParam.getParamMode() : "IN");
            param.setEnabled(true);
            param.setPosition(apiParam.getPosition() != null ? apiParam.getPosition() : 0);
            param.setEndpoint(endpoint);

            docParameterRepository.save(param);
        }
    }

    private void createResponseExamples(GeneratedApiEntity api, APIEndpointEntity endpoint, String generatedApiId) {
        if (api.getResponseConfig() != null) {
            // Success response
            if (api.getResponseConfig().getSuccessSchema() != null) {
                ResponseExampleEntity successExample = new ResponseExampleEntity();
                successExample.setId(UUID.randomUUID().toString());
                successExample.setGeneratedApiId(generatedApiId);
                successExample.setStatusCode(200);
                successExample.setDescription("Successful response");
                successExample.setContentType(api.getResponseConfig().getContentType() != null ?
                        api.getResponseConfig().getContentType() : "application/json");
                successExample.setEndpoint(endpoint);

                try {
                    Map<String, Object> exampleMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            api.getResponseConfig().getSuccessSchema(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    successExample.setExample(exampleMap);
                } catch (Exception e) {
                    log.warn("Failed to parse success response example: {}", e.getMessage());
                }
                responseExampleRepository.save(successExample);
            }

            // Error response
            if (api.getResponseConfig().getErrorSchema() != null) {
                ResponseExampleEntity errorExample = new ResponseExampleEntity();
                errorExample.setId(UUID.randomUUID().toString());
                errorExample.setGeneratedApiId(generatedApiId);
                errorExample.setStatusCode(400);
                errorExample.setDescription("Error response");
                errorExample.setContentType(api.getResponseConfig().getContentType() != null ?
                        api.getResponseConfig().getContentType() : "application/json");
                errorExample.setEndpoint(endpoint);

                try {
                    Map<String, Object> exampleMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            api.getResponseConfig().getErrorSchema(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    errorExample.setExample(exampleMap);
                } catch (Exception e) {
                    log.warn("Failed to parse error response example: {}", e.getMessage());
                }
                responseExampleRepository.save(errorExample);
            }
        }
    }

    private void generateCodeExamples(GeneratedApiEntity api, APIEndpointEntity endpoint,
                                      String codeBaseRequestId, String generatedApiId) {
        CodeLanguageGeneratorUtil codeGenerator = new CodeLanguageGeneratorUtil();
        GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);

        List<String> languages = Arrays.asList("java", "javascript", "python", "curl",
                "csharp", "php", "ruby", "go", "kotlin", "swift", "rust");

        for (String language : languages) {
            try {
                String code = codeGenerator.generateCodeForLanguage(api, language, genUrlInfo);

                if (code != null && !code.trim().isEmpty()) {
                    CodeExampleEntity codeExample = new CodeExampleEntity();
                    codeExample.setId(UUID.randomUUID().toString());
                    codeExample.setGeneratedApiId(generatedApiId);
                    codeExample.setLanguage(language);
                    codeExample.setCode(code);
                    codeExample.setDescription("Auto-generated " + language + " code example");
                    codeExample.setEndpoint(endpoint);
                    codeExample.setDefault(language.equals("curl") || language.equals("java"));
                    codeExample.setDisplayOrder(getLanguageOrder(language));

                    codeExampleRepository.save(codeExample);
                }
            } catch (Exception e) {
                log.warn("Failed to generate documentation code example for {}: {}", language, e.getMessage());
            }
        }
    }

    private Integer getLanguageOrder(String language) {
        Map<String, Integer> order = Map.ofEntries(
                Map.entry("curl", 1),
                Map.entry("java", 2),
                Map.entry("javascript", 3),
                Map.entry("python", 4),
                Map.entry("csharp", 5),
                Map.entry("php", 6),
                Map.entry("ruby", 7),
                Map.entry("go", 8),
                Map.entry("kotlin", 9),
                Map.entry("swift", 10),
                Map.entry("rust", 11)
        );
        return order.getOrDefault(language, 99);
    }

    private void createChangelog(GeneratedApiEntity api, String performedBy,
                                 APICollectionEntity collection, APIEndpointEntity endpoint,
                                 String generatedApiId) {
        ChangelogEntryEntity changelog = new ChangelogEntryEntity();
        changelog.setId(UUID.randomUUID().toString());
        changelog.setGeneratedApiId(generatedApiId);
        changelog.setVersion(api.getVersion());
        changelog.setDate(LocalDateTime.now().toString());
        changelog.setType("ADDED");
        changelog.setAuthor(performedBy);
        changelog.setCollection(collection);
        changelog.setEndpoint(endpoint);

        List<String> changes = new ArrayList<>();
        changes.add("Added endpoint: " + api.getApiName() + " (" + api.getHttpMethod() + ")");
        changes.add("Initial version of the API");
        changes.add("Generated from Oracle source: " + api.getSourceObjectInfo());
        changelog.setChanges(changes);
        changelog.setCreatedAt(LocalDateTime.now());

        changelogRepository.save(changelog);
    }

    private void createPublishedDocumentation(GeneratedApiEntity api, String performedBy,
                                              APICollectionEntity collection, String generatedApiId) {
        PublishedDocumentationEntity published = new PublishedDocumentationEntity();
        published.setId(UUID.randomUUID().toString());
        published.setGeneratedApiId(generatedApiId);
        published.setCollection(collection);
        published.setPublishedUrl("https://docs.example.com/collections/" + collection.getId());
        published.setTitle(api.getApiName() + " API Documentation");
        published.setVisibility("team");
        published.setActive(true);
        published.setPublishedBy(performedBy);
        published.setVersion(api.getVersion());
        published.setPublishedAt(LocalDateTime.now());

        publishedDocumentationRepository.save(published);
        log.debug("Created PublishedDocumentation: {}", published.getId());
    }

    private void createNotifications(GeneratedApiEntity api, String performedBy,
                                     String generatedApiId, String collectionId, String endpointId) {
        List<NotificationEntity> notifications = Arrays.asList(
                createNotification("API Generated", "API '" + api.getApiName() + "' was successfully generated",
                        "success", performedBy, collectionId, endpointId, generatedApiId),
                createNotification("Documentation Ready", "API documentation is now available",
                        "info", performedBy, collectionId, endpointId, generatedApiId),
                createNotification("Mock Server Created", "Mock server is ready for testing",
                        "info", performedBy, collectionId, endpointId, generatedApiId)
        );

        notificationRepository.saveAll(notifications);
        log.debug("Created {} notifications", notifications.size());
    }

    private NotificationEntity createNotification(String title, String message, String type,
                                                  String userId, String collectionId, String endpointId,
                                                  String generatedApiId) {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(UUID.randomUUID().toString());
        notification.setGeneratedApiId(generatedApiId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setUserId(userId);
        notification.setCollectionId(collectionId);
        notification.setEndpointId(endpointId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setExpiresAt(LocalDateTime.now().plusDays(7));
        return notification;
    }

    private void createSearchHistory(GeneratedApiEntity api, String performedBy, String generatedApiId) {
        SearchHistoryEntity searchHistory = new SearchHistoryEntity();
        searchHistory.setId(UUID.randomUUID().toString());
        searchHistory.setGeneratedApiId(generatedApiId);
        searchHistory.setQuery(api.getApiName());
        searchHistory.setSearchType("endpoint");
        searchHistory.setUserId(performedBy);
        searchHistory.setResultCount(1);
        searchHistory.setSearchTimeMs(100L);
        searchHistory.setCreatedAt(LocalDateTime.now());

        documentationSearchHistoryRepository.save(searchHistory);
    }

    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }
}