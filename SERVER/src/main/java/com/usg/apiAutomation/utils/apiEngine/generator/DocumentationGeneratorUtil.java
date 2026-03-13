package com.usg.apiAutomation.utils.apiEngine.generator;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.documentation.*;
import com.usg.apiAutomation.repositories.postgres.documentation.*;
import com.usg.apiAutomation.utils.apiEngine.CodeLanguageGeneratorUtil;
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
public class DocumentationGeneratorUtil {

    private final APICollectionRepository docCollectionRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.FolderRepository docFolderRepository;
    private final APIEndpointRepository endpointRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.HeaderRepository docHeaderRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.ParameterRepository docParameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;
    private final GenUrlBuilderUtil genUrlBuilder;
    private final CodeLanguageGeneratorUtil codeLanguageGeneratorUtil;

    @Transactional
    public String generate(GeneratedApiEntity api, String performedBy,
                           GenerateApiRequestDTO request,
                           String codeBaseRequestId,
                           String collectionsCollectionId,
                           CollectionInfoDTO collectionInfo) {
        try {
            log.info("Generating Documentation for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo.getCollectionName());

            APICollectionEntity docCollection;
            Optional<APICollectionEntity> existingCollection = docCollectionRepository
                    .findById(collectionInfo.getCollectionId());

            if (existingCollection.isPresent()) {
                docCollection = existingCollection.get();
                log.info("Found existing documentation collection: {}", docCollection.getId());

                docCollection.setName(collectionInfo.getCollectionName());
                docCollection.setDescription(api.getDescription());
                docCollection.setVersion(api.getVersion());
                docCollection.setTags(api.getTags());
                docCollection.setUpdatedBy(performedBy);
            } else {
                docCollection = new APICollectionEntity();
                docCollection.setId(collectionInfo.getCollectionId());
                docCollection.setName(collectionInfo.getCollectionName());
                docCollection.setDescription(api.getDescription());
                docCollection.setVersion(api.getVersion());
                docCollection.setOwner(performedBy);
                docCollection.setType("REST");
                docCollection.setFavorite(false);
                docCollection.setExpanded(false);
                docCollection.setColor(getRandomColor());
                docCollection.setStatus("published");
                docCollection.setTags(new ArrayList<>());
                docCollection.setCreatedBy(performedBy);
                docCollection.setUpdatedBy(performedBy);
                docCollection.setTotalEndpoints(0);
                docCollection.setTotalFolders(0);

                log.info("Created new documentation collection: {}", docCollection.getId());
            }

            APICollectionEntity savedDocCollection = docCollectionRepository.saveAndFlush(docCollection);
            log.debug("Saved documentation collection with ID: {}", savedDocCollection.getId());

            com.usg.apiAutomation.entities.postgres.documentation.FolderEntity docFolder;
            Optional<com.usg.apiAutomation.entities.postgres.documentation.FolderEntity> existingFolder =
                    docFolderRepository.findById(collectionInfo.getFolderId());

            if (existingFolder.isPresent()) {
                docFolder = existingFolder.get();
                log.info("Found existing documentation folder: {}", docFolder.getId());

                docFolder.setName(collectionInfo.getFolderName());
                docFolder.setDescription("Folder for " + collectionInfo.getFolderName());
                docFolder.setUpdatedBy(performedBy);
            } else {
                docFolder = new com.usg.apiAutomation.entities.postgres.documentation.FolderEntity();
                docFolder.setId(collectionInfo.getFolderId());
                docFolder.setName(collectionInfo.getFolderName());
                docFolder.setDescription("Folder for " + collectionInfo.getFolderName());
                docFolder.setCollection(savedDocCollection);
                docFolder.setDisplayOrder(1);
                docFolder.setCreatedBy(performedBy);
                docFolder.setUpdatedBy(performedBy);

                log.info("Created new documentation folder: {}", docFolder.getId());
            }

            com.usg.apiAutomation.entities.postgres.documentation.FolderEntity savedDocFolder =
                    docFolderRepository.saveAndFlush(docFolder);
            log.debug("Saved documentation folder with ID: {}", savedDocFolder.getId());

            savedDocCollection.setTotalFolders(1);
            docCollectionRepository.save(savedDocCollection);

            // Use the full URL from GenUrlBuilderUtil
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo = genUrlBuilder.buildGenUrlInfo(api);
            String endpointUrl = genUrlInfo.getFullUrl();
            log.info("Built endpoint URL for documentation: {}", endpointUrl);

            APIEndpointEntity endpoint = new APIEndpointEntity();
            endpoint.setId(UUID.randomUUID().toString());
            log.info("Creating new endpoint with ID: {} for API: {}", endpoint.getId(), api.getApiCode());

            endpoint.setName(api.getApiName());
            endpoint.setMethod(api.getHttpMethod());
            endpoint.setUrl(endpointUrl);  // Use gen URL from centralized builder
            endpoint.setDescription(api.getDescription());
            endpoint.setCollection(savedDocCollection);
            endpoint.setFolder(savedDocFolder);
            endpoint.setApiVersion(api.getVersion());
            endpoint.setRequiresAuth(api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()));
            endpoint.setDeprecated(false);
            endpoint.setCategory(api.getCategory());
            endpoint.setTags(api.getTags() != null ? api.getTags() : new ArrayList<>());
            endpoint.setCreatedBy(performedBy);
            endpoint.setUpdatedBy(performedBy);
            endpoint.setLastModifiedBy(performedBy);

            // Add gen URL information to endpoint metadata from GenUrlInfo
            Map<String, Object> endpointMetadata = new HashMap<>();
            endpointMetadata.put("genPath", genUrlInfo.getEndpointPath());
            endpointMetadata.put("apiId", api.getId());
            endpointMetadata.put("fullGenUrl", genUrlInfo.getFullUrl());
            endpointMetadata.put("genUrlPattern", genUrlInfo.getUrlPattern());
            endpointMetadata.put("exampleGenUrl", genUrlInfo.getExampleUrl());

            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                Map<String, Object> paramInfo = new HashMap<>();

                List<Map<String, Object>> pathParams = api.getParameters().stream()
                        .filter(p -> "path".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> paramMap = new HashMap<>();
                            paramMap.put("key", p.getKey());
                            paramMap.put("example", p.getExample());
                            paramMap.put("required", p.getRequired());
                            paramMap.put("description", p.getDescription());
                            paramMap.put("apiType", p.getApiType());
                            paramMap.put("oracleType", p.getOracleType());
                            return paramMap;
                        })
                        .collect(Collectors.toList());

                List<Map<String, Object>> queryParams = api.getParameters().stream()
                        .filter(p -> "query".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> paramMap = new HashMap<>();
                            paramMap.put("key", p.getKey());
                            paramMap.put("example", p.getExample());
                            paramMap.put("required", p.getRequired());
                            paramMap.put("description", p.getDescription());
                            paramMap.put("apiType", p.getApiType());
                            paramMap.put("oracleType", p.getOracleType());
                            return paramMap;
                        })
                        .collect(Collectors.toList());

                List<Map<String, Object>> headerParams = api.getParameters().stream()
                        .filter(p -> "header".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> paramMap = new HashMap<>();
                            paramMap.put("key", p.getKey());
                            paramMap.put("example", p.getExample());
                            paramMap.put("required", p.getRequired());
                            paramMap.put("description", p.getDescription());
                            paramMap.put("apiType", p.getApiType());
                            paramMap.put("oracleType", p.getOracleType());
                            return paramMap;
                        })
                        .collect(Collectors.toList());

                List<Map<String, Object>> bodyParams = api.getParameters().stream()
                        .filter(p -> "body".equals(p.getParameterType()))
                        .map(p -> {
                            Map<String, Object> paramMap = new HashMap<>();
                            paramMap.put("key", p.getKey());
                            paramMap.put("example", p.getExample());
                            paramMap.put("required", p.getRequired());
                            paramMap.put("description", p.getDescription());
                            paramMap.put("apiType", p.getApiType());
                            paramMap.put("oracleType", p.getOracleType());
                            return paramMap;
                        })
                        .collect(Collectors.toList());

                if (!pathParams.isEmpty()) paramInfo.put("path", pathParams);
                if (!queryParams.isEmpty()) paramInfo.put("query", queryParams);
                if (!headerParams.isEmpty()) paramInfo.put("header", headerParams);
                if (!bodyParams.isEmpty()) paramInfo.put("body", bodyParams);

                endpointMetadata.put("parameters", paramInfo);

                // Add URL template
                endpointMetadata.put("urlTemplate", genUrlBuilder.buildUrlTemplate(api));
            }

            endpoint.setMetaData(endpointMetadata);

            if (api.getSettings() != null && api.getSettings().getEnableRateLimiting() != null) {
                Map<String, Object> rateLimit = new HashMap<>();
                rateLimit.put("enabled", api.getSettings().getEnableRateLimiting());
                rateLimit.put("requestsPerMinute", api.getSettings().getRateLimit() != null ?
                        api.getSettings().getRateLimit() : 60);
                rateLimit.put("strategy", "token_bucket");
                endpoint.setRateLimit(rateLimit);
            }

            // Set request body example from body parameters
            if (api.getParameters() != null) {
                List<ApiParameterEntity> bodyParams = api.getParameters().stream()
                        .filter(p -> "body".equals(p.getParameterType()))
                        .collect(Collectors.toList());

                if (!bodyParams.isEmpty()) {
                    try {
                        Map<String, Object> bodyMap = new HashMap<>();
                        for (ApiParameterEntity param : bodyParams) {
                            bodyMap.put(param.getKey(), param.getExample() != null ?
                                    param.getExample() : "sample");
                        }
                        endpoint.setRequestBodyExample(bodyMap);
                    } catch (Exception e) {
                        log.warn("Failed to create request body example: {}", e.getMessage());
                    }
                } else if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                    try {
                        Map<String, Object> bodyMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                                api.getRequestConfig().getSample(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                        );
                        endpoint.setRequestBodyExample(bodyMap);
                    } catch (Exception e) {
                        log.warn("Failed to parse request body example: {}", e.getMessage());
                    }
                }
            }

            APIEndpointEntity savedEndpoint = endpointRepository.saveAndFlush(endpoint);
            log.debug("Saved endpoint with ID: {}", savedEndpoint.getId());

            endpointRepository.flush();

            // Add headers
            if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
                int headerCount = 0;
                for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                    if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader()) &&
                            apiHeader.getKey() != null && !apiHeader.getKey().trim().isEmpty()) {

                        com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity header =
                                new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();

                        header.setId(UUID.randomUUID().toString());
                        header.setKey(apiHeader.getKey().trim());
                        header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                        header.setDescription(apiHeader.getDescription() != null ? apiHeader.getDescription() : "");
                        header.setRequired(apiHeader.getRequired() != null ? apiHeader.getRequired() : false);
                        header.setEndpoint(savedEndpoint);

                        docHeaderRepository.save(header);
                        headerCount++;
                    }
                }
                docHeaderRepository.flush();
                log.debug("Saved {} headers for endpoint", headerCount);
            }

            // Add header parameters
            if (api.getParameters() != null) {
                int headerParamCount = 0;
                for (ApiParameterEntity apiParam : api.getParameters()) {
                    if ("header".equals(apiParam.getParameterType()) &&
                            apiParam.getKey() != null && !apiParam.getKey().trim().isEmpty()) {

                        com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity header =
                                new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();

                        header.setId(UUID.randomUUID().toString());
                        header.setKey(apiParam.getKey().trim());
                        header.setValue(apiParam.getExample() != null ? apiParam.getExample() : "");
                        header.setDescription(apiParam.getDescription() != null ? apiParam.getDescription() : "");
                        header.setRequired(apiParam.getRequired() != null ? apiParam.getRequired() : false);
                        header.setEndpoint(savedEndpoint);

                        docHeaderRepository.save(header);
                        headerParamCount++;
                    }
                }
                if (headerParamCount > 0) {
                    docHeaderRepository.flush();
                    log.debug("Saved {} header parameters for endpoint", headerParamCount);
                }
            }

            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity authHeader =
                        new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();

                authHeader.setId(UUID.randomUUID().toString());
                authHeader.setRequired(true);
                authHeader.setEndpoint(savedEndpoint);

                String key = null;
                String value = null;
                String description = null;

                switch (api.getAuthConfig().getAuthType()) {
                    case "API_KEY":
                        key = api.getAuthConfig().getApiKeyHeader() != null ?
                                api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                        value = "Your API Key";
                        description = "API Key for authentication";

                        // Add API Secret if configured
                        if (api.getAuthConfig().getApiSecretHeader() != null) {
                            com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity secretHeader =
                                    new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();
                            secretHeader.setId(UUID.randomUUID().toString());
                            secretHeader.setKey(api.getAuthConfig().getApiSecretHeader());
                            secretHeader.setValue("Your API Secret");
                            secretHeader.setDescription("API Secret for authentication");
                            secretHeader.setRequired(true);
                            secretHeader.setEndpoint(savedEndpoint);
                            docHeaderRepository.save(secretHeader);
                        }
                        break;
                    case "BEARER":
                    case "JWT":
                        key = "Authorization";
                        value = "Bearer YOUR_JWT_TOKEN";
                        description = "Bearer token authentication";
                        break;
                    case "BASIC":
                        key = "Authorization";
                        value = "Basic base64_encoded_credentials";
                        description = "Basic authentication (username:password encoded in base64)";
                        break;
                    case "ORACLE_ROLES":
                        key = "X-Oracle-Session";
                        value = "Your Oracle Session ID";
                        description = "Oracle Database Session ID for authentication";
                        break;
                }

                if (key != null && !key.trim().isEmpty()) {
                    authHeader.setKey(key);
                    authHeader.setValue(value);
                    authHeader.setDescription(description);
                    docHeaderRepository.save(authHeader);
                    docHeaderRepository.flush();
                    log.debug("Saved auth header for endpoint");
                }
            }

            // Add parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                int paramCount = 0;
                log.debug("Creating {} new parameters for endpoint", api.getParameters().size());

                for (ApiParameterEntity apiParam : api.getParameters()) {
                    if ("header".equals(apiParam.getParameterType())) {
                        continue; // Headers already handled separately
                    }

                    if (apiParam.getKey() == null || apiParam.getKey().trim().isEmpty()) {
                        log.warn("Skipping parameter with null or empty key");
                        continue;
                    }

                    com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity param =
                            new com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity();

                    param.setId(UUID.randomUUID().toString());

                    param.setName(apiParam.getKey().trim());
                    param.setKey(apiParam.getKey().trim());

                    param.setDbColumn(apiParam.getDbColumn());
                    param.setDbParameter(apiParam.getDbParameter());

                    param.setParameterType(apiParam.getApiType() != null ?
                            apiParam.getApiType() : "");
                    param.setOracleType(apiParam.getOracleType());
                    param.setApiType(apiParam.getApiType());

                    param.setParameterLocation(apiParam.getParameterLocation() != null ?
                            apiParam.getParameterLocation() : "query");
                    param.setRequired(apiParam.getRequired() != null ? apiParam.getRequired() : false);

                    param.setDescription(apiParam.getDescription() != null ?
                            apiParam.getDescription() : "");
                    param.setDefaultValue(apiParam.getDefaultValue());
                    param.setExample(apiParam.getExample());
                    param.setValue(apiParam.getDefaultValue());

                    param.setValidationPattern(apiParam.getValidationPattern());

                    param.setInBody(apiParam.getInBody() != null ? apiParam.getInBody() : false);
                    param.setIsPrimaryKey(apiParam.getIsPrimaryKey() != null ?
                            apiParam.getIsPrimaryKey() : false);

                    param.setParamMode(apiParam.getParamMode() != null ?
                            apiParam.getParamMode() : "IN");

                    param.setEnabled(true);
                    param.setPosition(apiParam.getPosition() != null ?
                            apiParam.getPosition() : paramCount);

                    param.setEndpoint(savedEndpoint);

                    try {
                        com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity saved =
                                docParameterRepository.save(param);
                        paramCount++;
                        log.debug("Saved documentation parameter: {} with ID: {}",
                                saved.getKey(), saved.getId());
                    } catch (Exception e) {
                        log.error("Failed to save documentation parameter {}: {}",
                                apiParam.getKey(), e.getMessage());
                    }
                }

                docParameterRepository.flush();
                log.info("Saved {} documentation parameters for endpoint: {}",
                        paramCount, savedEndpoint.getId());
            } else {
                log.debug("No parameters to save for endpoint: {}", savedEndpoint.getId());
            }

            if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
                ResponseExampleEntity successExample = new ResponseExampleEntity();
                successExample.setId(UUID.randomUUID().toString());
                successExample.setStatusCode(200);
                successExample.setDescription("Successful response");
                successExample.setContentType(api.getResponseConfig().getContentType() != null ?
                        api.getResponseConfig().getContentType() : "application/json");
                successExample.setEndpoint(savedEndpoint);

                try {
                    Map<String, Object> exampleMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            api.getResponseConfig().getSuccessSchema(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                    );
                    successExample.setExample(exampleMap);
                } catch (Exception e) {
                    log.warn("Failed to parse success response example: {}", e.getMessage());
                }

                responseExampleRepository.save(successExample);
                log.debug("Saved success response example for endpoint");
            }

            if (api.getResponseConfig() != null && api.getResponseConfig().getErrorSchema() != null) {
                ResponseExampleEntity errorExample = new ResponseExampleEntity();
                errorExample.setId(UUID.randomUUID().toString());
                errorExample.setStatusCode(400);
                errorExample.setDescription("Error response");
                errorExample.setContentType(api.getResponseConfig().getContentType() != null ?
                        api.getResponseConfig().getContentType() : "application/json");
                errorExample.setEndpoint(savedEndpoint);

                try {
                    Map<String, Object> exampleMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            api.getResponseConfig().getErrorSchema(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                    );
                    errorExample.setExample(exampleMap);
                } catch (Exception e) {
                    log.warn("Failed to parse error response example: {}", e.getMessage());
                }

                responseExampleRepository.save(errorExample);
                log.debug("Saved error response example for endpoint");
            }

            responseExampleRepository.flush();

            generateCodeExamples(api, savedEndpoint, codeBaseRequestId, genUrlInfo);

            ChangelogEntryEntity changelog = new ChangelogEntryEntity();
            changelog.setId(UUID.randomUUID().toString());
            changelog.setVersion(api.getVersion());
            changelog.setDate(LocalDateTime.now().toString());
            changelog.setType("ADDED");
            changelog.setAuthor(performedBy);
            changelog.setCollection(savedDocCollection);

            List<String> changes = new ArrayList<>();
            changes.add("Added endpoint: " + api.getApiName() + " (" + api.getHttpMethod() + ")");
            changes.add("Initial version of the API");
            changes.add("Added to collection: " + collectionInfo.getCollectionName());
            changes.add("Added to folder: " + collectionInfo.getFolderName());
            changes.add("Gen URL: " + genUrlInfo.getEndpointPath());
            changes.add("URL Pattern: " + genUrlInfo.getUrlPattern());
            changelog.setChanges(changes);

            changelogRepository.save(changelog);
            changelogRepository.flush();
            log.debug("Saved changelog entry for collection");

            savedDocCollection.setTotalEndpoints(1);
            docCollectionRepository.save(savedDocCollection);
            docCollectionRepository.flush();

            log.info("Documentation generated successfully with Collection ID: {} using Folder: {}",
                    savedDocCollection.getId(), collectionInfo.getFolderName());

            return savedDocCollection.getId();

        } catch (Exception e) {
            log.error("Error generating Documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Documentation: " + e.getMessage(), e);
        }
    }

    private void generateCodeExamples(GeneratedApiEntity api, APIEndpointEntity endpoint,
                                      String codeBaseRequestId, GenUrlBuilderUtil.GenUrlInfo genUrlInfo) {
        try {
            log.info("Generating documentation code examples for endpoint: {}", endpoint.getId());

            List<String> languages = Arrays.asList("java", "javascript", "python", "curl", "csharp", "php", "ruby", "go");

            List<CodeExampleEntity> existingExamples = codeExampleRepository.findByEndpointId(endpoint.getId());
            if (existingExamples != null && !existingExamples.isEmpty()) {
                log.debug("Deleting {} existing code examples", existingExamples.size());

                for (CodeExampleEntity example : existingExamples) {
                    example.setEndpoint(null);
                }

                codeExampleRepository.deleteAll(existingExamples);
                codeExampleRepository.flush();
            }

            int exampleCount = 0;

            for (String language : languages) {
                try {
                    String code = codeLanguageGeneratorUtil.generateCodeForLanguage(api, language, genUrlInfo);

                    if (code != null && !code.trim().isEmpty()) {
                        CodeExampleEntity codeExample = new CodeExampleEntity();
                        codeExample.setId(UUID.randomUUID().toString());
                        codeExample.setLanguage(language);
                        codeExample.setCode(code);
                        codeExample.setDescription("Auto-generated " + language + " code example");
                        codeExample.setEndpoint(endpoint);

                        codeExample.setDefault(language.equals("curl") || language.equals("java"));

                        codeExampleRepository.save(codeExample);
                        exampleCount++;

                        log.debug("Saved {} code example for endpoint with ID: {}", language, codeExample.getId());
                    } else {
                        log.warn("Generated code for {} was null or empty", language);
                    }

                } catch (Exception e) {
                    log.error("Failed to generate documentation code example for {}: {}", language, e.getMessage(), e);
                }
            }

            codeExampleRepository.flush();

            log.info("Successfully generated {} code examples for endpoint: {}", exampleCount, endpoint.getId());

        } catch (Exception e) {
            log.error("Failed to generate documentation code examples: {}", e.getMessage(), e);
        }
    }

    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }
}