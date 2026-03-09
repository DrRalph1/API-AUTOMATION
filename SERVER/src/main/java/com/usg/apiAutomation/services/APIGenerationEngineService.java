package com.usg.apiAutomation.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.codeBase.ImplementationEntity;
import com.usg.apiAutomation.entities.postgres.codeBase.RequestEntity;
import com.usg.apiAutomation.entities.postgres.collections.*;
import com.usg.apiAutomation.entities.postgres.collections.FolderEntity;
import com.usg.apiAutomation.entities.postgres.collections.HeaderEntity;
import com.usg.apiAutomation.entities.postgres.collections.ParameterEntity;
import com.usg.apiAutomation.entities.postgres.documentation.*;
import com.usg.apiAutomation.helpers.ApiValidatorHelper;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.postgres.codeBase.*;
import com.usg.apiAutomation.repositories.postgres.codeBase.CollectionRepository;
import com.usg.apiAutomation.repositories.postgres.codeBase.FolderRepository;
import com.usg.apiAutomation.repositories.postgres.codeBase.RequestRepository;
import com.usg.apiAutomation.repositories.postgres.collections.*;
import com.usg.apiAutomation.repositories.postgres.collections.HeaderRepository;
import com.usg.apiAutomation.repositories.postgres.collections.ParameterRepository;
import com.usg.apiAutomation.repositories.postgres.documentation.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class APIGenerationEngineService {

    private final GeneratedAPIRepository generatedAPIRepository;
    private final ApiExecutionLogRepository executionLogRepository;
    private final ApiTestRepository apiTestRepository;
    private final ObjectMapper objectMapper;
    private final LoggerUtil loggerUtil;
    private final ApiValidatorHelper validatorService;
    private final OracleSchemaService oracleSchemaService;
    private final JdbcTemplate oracleJdbcTemplate;
    private final EntityManager entityManager;

    // CodeBase repositories
    private final CollectionRepository codeBaseCollectionRepository;
    private final FolderRepository codeBaseFolderRepository;
    private final RequestRepository codeBaseRequestRepository;
    private final ImplementationRepository implementationRepository;

    // Collections repositories
    private final com.usg.apiAutomation.repositories.postgres.collections.CollectionRepository collectionsCollectionRepository;
    private final com.usg.apiAutomation.repositories.postgres.collections.FolderRepository collectionsFolderRepository;
    private final com.usg.apiAutomation.repositories.postgres.collections.RequestRepository collectionsRequestRepository;
    private final HeaderRepository collectionsHeaderRepository;
    private final ParameterRepository collectionsParameterRepository;
    private final AuthConfigRepository collectionsAuthConfigRepository;

    // Documentation repositories
    private final APICollectionRepository docCollectionRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.FolderRepository docFolderRepository;
    private final APIEndpointRepository endpointRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.HeaderRepository docHeaderRepository;
    private final com.usg.apiAutomation.repositories.postgres.documentation.ParameterRepository docParameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;

    /**
     * Inner class to hold Gen URL information
     */
    @lombok.Data
    @lombok.Builder
    private static class GenUrlInfo {
        private String endpointPath;      // /gen/{apiId}/api/v1/ach-file-bc-gen/{entry_count}/{company_identification}
        private String fullUrl;            // {{baseUrl}}/gen/{apiId}/api/v1/ach-file-bc-gen/{entry_count}/{company_identification}
        private String urlPattern;         // {{baseUrl}}/gen/{apiId}/api/v1/ach-file-bc-gen/{entry_count}/{company_identification}?service_code={service_code}&total_debit_entry={total_debit_entry}
        private String exampleUrl;         // https://api.example.com/gen/123e4567/api/v1/ach-file-bc-gen/100/CBX001?service_code=ACH001&total_debit_entry=5000
        private String curlExample;        // curl -X POST 'https://api.example.com/gen/123e4567/api/v1/ach-file-bc-gen/100/CBX001?service_code=ACH001&total_debit_entry=5000' ...
    }

    /**
     * Build complete Gen URL information - CENTRALIZED METHOD
     * This is the single source of truth for gen URL construction
     */
    private GenUrlInfo buildGenUrlInfo(GeneratedApiEntity api) {
        String baseUrl = getBaseUrl();
        String apiId = api.getId();

        // Build endpoint path with gen prefix and API ID
        StringBuilder endpointPath = new StringBuilder();
        endpointPath.append("/gen/").append(apiId);

        // Add base path if present
        if (api.getBasePath() != null && !api.getBasePath().isEmpty()) {
            if (!api.getBasePath().startsWith("/")) {
                endpointPath.append("/");
            }
            endpointPath.append(api.getBasePath());
        }

        // Add endpoint path
        if (api.getEndpointPath() != null && !api.getEndpointPath().isEmpty()) {
            if (!api.getEndpointPath().startsWith("/")) {
                endpointPath.append("/");
            }
            endpointPath.append(api.getEndpointPath());
        }

        // Add path parameter placeholders
        if (api.getParameters() != null) {
            List<ApiParameterEntity> pathParams = api.getParameters().stream()
                    .filter(p -> "path".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .collect(Collectors.toList());

            for (ApiParameterEntity param : pathParams) {
                endpointPath.append("/{").append(param.getKey()).append("}");
            }
        }

        String endpointPathStr = endpointPath.toString();

        // Build full URL with base URL placeholder
        String fullUrl = "{{baseUrl}}" + endpointPathStr;

        // Build URL pattern with query parameter placeholders
        StringBuilder urlPattern = new StringBuilder(fullUrl);

        if (api.getParameters() != null) {
            List<ApiParameterEntity> queryParams = api.getParameters().stream()
                    .filter(p -> "query".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .collect(Collectors.toList());

            if (!queryParams.isEmpty()) {
                urlPattern.append("?");
                for (int i = 0; i < queryParams.size(); i++) {
                    ApiParameterEntity param = queryParams.get(i);
                    if (i > 0) {
                        urlPattern.append("&");
                    }
                    urlPattern.append(param.getKey()).append("={").append(param.getKey()).append("}");
                }
            }
        }

        String urlPatternStr = urlPattern.toString();

        // Build example URL with sample values
        StringBuilder exampleUrl = new StringBuilder("https://api.example.com").append(endpointPathStr);

        // Replace path parameter placeholders with examples
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("path".equals(param.getParameterType())) {
                    String placeholder = "{" + param.getKey() + "}";
                    String example = param.getExample() != null && !param.getExample().isEmpty() ?
                            param.getExample() : param.getKey().toLowerCase() + "-value";
                    int startIndex = exampleUrl.indexOf(placeholder);
                    if (startIndex != -1) {
                        exampleUrl.replace(startIndex, startIndex + placeholder.length(), example);
                    }
                }
            }
        }

        // Add query parameters with examples
        if (api.getParameters() != null) {
            List<String> queryExamples = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    String example = param.getExample() != null && !param.getExample().isEmpty() ?
                            param.getExample() : param.getKey().toLowerCase() + "-value";
                    queryExamples.add(param.getKey() + "=" + example);
                }
            }

            if (!queryExamples.isEmpty()) {
                exampleUrl.append(exampleUrl.toString().contains("?") ? "&" : "?").append(String.join("&", queryExamples));
            }
        }

        String exampleUrlStr = exampleUrl.toString();

        // Build curl example
        String curlExample = buildCurlExample(api, exampleUrlStr);

        return GenUrlInfo.builder()
                .endpointPath(endpointPathStr)
                .fullUrl(fullUrl)
                .urlPattern(urlPatternStr)
                .exampleUrl(exampleUrlStr)
                .curlExample(curlExample)
                .build();
    }

    /**
     * Build curl example with the gen URL
     */
    private String buildCurlExample(GeneratedApiEntity api, String exampleUrl) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(api.getHttpMethod() != null ? api.getHttpMethod() : "GET").append(" \\\n");
        curl.append("  '").append(exampleUrl).append("'");

        // Add headers
        curl.append(" \\\n  -H 'Content-Type: application/json'");
        curl.append(" \\\n  -H 'Accept: application/json'");

        // Add header parameters
        if (api.getParameters() != null) {
            List<ApiParameterEntity> headerParams = api.getParameters().stream()
                    .filter(p -> "header".equals(p.getParameterType()))
                    .collect(Collectors.toList());

            for (ApiParameterEntity param : headerParams) {
                String example = param.getExample() != null && !param.getExample().isEmpty() ?
                        param.getExample() : param.getKey().toLowerCase() + "-value";
                curl.append(" \\\n  -H '").append(param.getKey()).append(": ").append(example).append("'");
            }
        }

        // Add auth headers if configured
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            curl.append(" \\\n");
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    curl.append("  -H '").append(header).append(": your-api-key'");

                    if (api.getAuthConfig().getApiSecretHeader() != null) {
                        curl.append(" \\\n  -H '").append(api.getAuthConfig().getApiSecretHeader()).append(": your-api-secret'");
                    }
                    break;
                case "BEARER":
                case "JWT":
                    curl.append("  -H 'Authorization: Bearer your-jwt-token'");
                    break;
                case "BASIC":
                    curl.append("  -u 'username:password'");
                    break;
                case "ORACLE_ROLES":
                    curl.append("  -H 'X-Oracle-Session: your-session-id'");
                    break;
            }
        }

        // Add custom headers from headers array
        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null &&
                        !header.getKey().isEmpty()) {
                    curl.append(" \\\n");
                    curl.append("  -H '").append(header.getKey()).append(": ");
                    curl.append(header.getValue() != null ? header.getValue() : "value");
                    curl.append("'");
                }
            }
        }

        // Add request body for non-GET requests and if there are body parameters
        if (!"GET".equals(api.getHttpMethod())) {
            // Build sample body from body parameters
            if (api.getParameters() != null) {
                List<ApiParameterEntity> bodyParams = api.getParameters().stream()
                        .filter(p -> "body".equals(p.getParameterType()))
                        .collect(Collectors.toList());

                if (!bodyParams.isEmpty()) {
                    Map<String, Object> sampleBody = new HashMap<>();
                    for (ApiParameterEntity param : bodyParams) {
                        String example = param.getExample() != null && !param.getExample().isEmpty() ?
                                param.getExample() : param.getKey().toLowerCase() + "-value";

                        if ("integer".equals(param.getApiType()) || "number".equals(param.getApiType())) {
                            try {
                                sampleBody.put(param.getKey(), Long.parseLong(example));
                            } catch (NumberFormatException e) {
                                sampleBody.put(param.getKey(), 123);
                            }
                        } else if ("boolean".equals(param.getApiType())) {
                            sampleBody.put(param.getKey(), true);
                        } else {
                            sampleBody.put(param.getKey(), example);
                        }
                    }

                    try {
                        String jsonBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sampleBody);
                        curl.append(" \\\n");
                        curl.append("  -d '").append(jsonBody.replace("'", "\\'")).append("'");
                    } catch (Exception e) {
                        log.warn("Failed to generate sample JSON body: {}", e.getMessage());
                    }
                } else if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                    curl.append(" \\\n");
                    curl.append("  -d '").append(api.getRequestConfig().getSample().replace("'", "\\'")).append("'");
                }
            }
        }

        return curl.toString();
    }

    /**
     * Get the base URL from configuration
     */
    private String getBaseUrl() {
        // Try system property first
        String baseUrl = System.getProperty("api.base.url");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return removeTrailingSlash(baseUrl);
        }

        // Try environment variable
        baseUrl = System.getenv("API_BASE_URL");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return removeTrailingSlash(baseUrl);
        }

        // Default fallback
        return "{{baseUrl}}";
    }

    /**
     * Remove trailing slash from URL if present
     */
    private String removeTrailingSlash(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Build URL template with placeholders
     */
    private String buildUrlTemplate(GeneratedApiEntity api) {
        StringBuilder template = new StringBuilder();
        template.append("{{baseUrl}}/gen/").append(api.getId());

        if (api.getBasePath() != null && !api.getBasePath().isEmpty()) {
            template.append(api.getBasePath().startsWith("/") ? "" : "/").append(api.getBasePath());
        }

        if (api.getEndpointPath() != null && !api.getEndpointPath().isEmpty()) {
            template.append(api.getEndpointPath().startsWith("/") ? "" : "/").append(api.getEndpointPath());
        }

        // Add path parameter placeholders
        if (api.getParameters() != null) {
            List<ApiParameterEntity> pathParams = api.getParameters().stream()
                    .filter(p -> "path".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .collect(Collectors.toList());

            for (ApiParameterEntity param : pathParams) {
                template.append("/{").append(param.getKey()).append("}");
            }
        }

        // Add query parameter placeholders
        if (api.getParameters() != null) {
            List<ApiParameterEntity> queryParams = api.getParameters().stream()
                    .filter(p -> "query".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .collect(Collectors.toList());

            if (!queryParams.isEmpty()) {
                template.append("?");
                for (int i = 0; i < queryParams.size(); i++) {
                    ApiParameterEntity param = queryParams.get(i);
                    if (i > 0) {
                        template.append("&");
                    }
                    template.append(param.getKey()).append("={").append(param.getKey()).append("}");
                }
            }
        }

        return template.toString();
    }

    /**
     * Build path placeholders string
     */
    private String buildPathPlaceholders(GeneratedApiEntity api) {
        if (api.getParameters() == null) return "";

        return api.getParameters().stream()
                .filter(p -> "path".equals(p.getParameterType()))
                .map(p -> "{" + p.getKey() + "}")
                .collect(Collectors.joining("/", "/", ""));
    }

    /**
     * Build query placeholders string
     */
    private String buildQueryPlaceholders(GeneratedApiEntity api) {
        if (api.getParameters() == null) return "";

        List<ApiParameterEntity> queryParams = api.getParameters().stream()
                .filter(p -> "query".equals(p.getParameterType()))
                .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                .collect(Collectors.toList());

        if (queryParams.isEmpty()) return "";

        return queryParams.stream()
                .map(p -> p.getKey() + "={" + p.getKey() + "}")
                .collect(Collectors.joining("&", "?", ""));
    }

    @Transactional
    public GeneratedApiResponseDTO generateApi(String requestId, String performedBy, GenerateApiRequestDTO request) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Generating API: " + request.getApiName() + " by: " + performedBy);

            // Validate API code uniqueness
            if (generatedAPIRepository.existsByApiCode(request.getApiCode())) {
                throw new RuntimeException("API code already exists: " + request.getApiCode());
            }

            // Convert source object map to DTO
            ApiSourceObjectDTO sourceObjectDTO = null;
            if (request.getSourceObject() != null && !request.getSourceObject().isEmpty()) {
                sourceObjectDTO = convertMapToSourceObjectDTO(request.getSourceObject());
            }

            // Validate source object if provided
            if (sourceObjectDTO != null) {
                validateSourceObject(sourceObjectDTO);
            }

            // Extract collection and folder information from request
            CollectionInfoDTO collectionInfo = request.getCollectionInfo();
            if (collectionInfo == null) {
                throw new RuntimeException("Collection information is required");
            }

            // Validate collection info
            validateCollectionInfo(collectionInfo);

            // Create main API entity
            GeneratedApiEntity api = GeneratedApiEntity.builder()
                    .apiName(request.getApiName())
                    .apiCode(request.getApiCode())
                    .description(request.getDescription())
                    .version(request.getVersion())
                    .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                    .httpMethod(request.getHttpMethod())
                    .basePath(request.getBasePath())
                    .endpointPath(request.getEndpointPath())
                    .category(request.getCategory())
                    .owner(request.getOwner())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(performedBy)
                    .isActive(true)
                    .totalCalls(0L)
                    .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                    .sourceObjectInfo(sourceObjectDTO != null ?
                            objectMapper.convertValue(sourceObjectDTO, Map.class) : null)
                    .collectionInfo(objectMapper.convertValue(collectionInfo, Map.class))
                    .sourceRequestId(requestId)
                    .build();

            // Save schema config
            if (request.getSchemaConfig() != null) {
                ApiSchemaConfigEntity schemaConfig = mapToSchemaConfigEntity(request.getSchemaConfig(), api);
                api.setSchemaConfig(schemaConfig);
            }

            // Save auth config
            if (request.getAuthConfig() != null) {
                ApiAuthConfigEntity authConfig = mapToAuthConfigEntity(request.getAuthConfig(), api);
                api.setAuthConfig(authConfig);
            }

            // Save request config
            if (request.getRequestBody() != null) {
                ApiRequestConfigEntity requestConfig = mapToRequestConfigEntity(request.getRequestBody(), api);
                api.setRequestConfig(requestConfig);
            }

            // Save response config
            if (request.getResponseBody() != null) {
                ApiResponseConfigEntity responseConfig = mapToResponseConfigEntity(request.getResponseBody(), api);
                api.setResponseConfig(responseConfig);
            }

            // Save settings
            if (request.getSettings() != null) {
                ApiSettingsEntity settings = mapToSettingsEntity(request.getSettings(), api);
                api.setSettings(settings);
            }

            // Save tests
            if (request.getTests() != null) {
                List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
                api.setTests(tests);
            }

            // Save parameters from UI
            if (request.getParameters() != null && !request.getParameters().isEmpty()) {
                List<ApiParameterEntity> parameters = new ArrayList<>();
                for (int i = 0; i < request.getParameters().size(); i++) {
                    ApiParameterDTO paramDto = request.getParameters().get(i);
                    ApiParameterEntity param = mapToParameterEntity(paramDto, api);
                    param.setPosition(i);
                    parameters.add(param);
                }
                api.setParameters(parameters);
            } else if (sourceObjectDTO != null) {
                List<ApiParameterEntity> parameters = generateParametersFromSource(sourceObjectDTO, api);
                api.setParameters(parameters);
            }

            // Save response mappings from UI
            if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
                List<ApiResponseMappingEntity> mappings = new ArrayList<>();
                for (int i = 0; i < request.getResponseMappings().size(); i++) {
                    ApiResponseMappingDTO mappingDto = request.getResponseMappings().get(i);
                    ApiResponseMappingEntity mapping = mapToResponseMappingEntity(mappingDto, api);
                    mapping.setPosition(i);
                    mappings.add(mapping);
                }
                api.setResponseMappings(mappings);
            } else if (sourceObjectDTO != null) {
                List<ApiResponseMappingEntity> mappings = generateResponseMappingsFromSource(sourceObjectDTO, api);
                api.setResponseMappings(mappings);
            }

            // Save headers
            if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                List<ApiHeaderEntity> headers = request.getHeaders().stream()
                        .map(headerDto -> mapToHeaderEntity(headerDto, api))
                        .collect(Collectors.toList());
                api.setHeaders(headers);
            }

            // Save to database
            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);

            // Generate code and documentation
            Map<String, String> generatedFiles = generateApiCode(savedApi);

            // Generate related components using the same URL pattern
            GenUrlInfo genUrlInfo = buildGenUrlInfo(savedApi);
            String codeBaseRequestId = generateCodeBase(savedApi, performedBy, request, collectionInfo, genUrlInfo);
            String collectionId = generateCollections(savedApi, performedBy, request, collectionInfo, genUrlInfo);
            String docCollectionId = generateDocumentation(savedApi, performedBy, request, codeBaseRequestId, collectionId, collectionInfo, genUrlInfo);

            // Build response
            GeneratedApiResponseDTO response = mapToResponse(savedApi);
            response.setGeneratedFiles(generatedFiles);

            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("parametersCount", savedApi.getParameters() != null ? savedApi.getParameters().size() : 0);
            metadata.put("responseMappingsCount", savedApi.getResponseMappings() != null ? savedApi.getResponseMappings().size() : 0);
            metadata.put("headersCount", savedApi.getHeaders() != null ? savedApi.getHeaders().size() : 0);
            metadata.put("generatedAt", LocalDateTime.now().toString());
            metadata.put("codeBaseRequestId", codeBaseRequestId);
            metadata.put("collectionsCollectionId", collectionId);
            metadata.put("documentationCollectionId", docCollectionId);

            // Add gen endpoint information from centralized builder
            metadata.put("genEndpointPath", genUrlInfo.getEndpointPath());
            metadata.put("fullGenUrl", genUrlInfo.getFullUrl());
            metadata.put("exampleGenUrl", genUrlInfo.getExampleUrl());
            metadata.put("genUrlPattern", genUrlInfo.getUrlPattern());
            metadata.put("curlExample", genUrlInfo.getCurlExample());

            // Add parameter information for URL building
            if (savedApi.getParameters() != null && !savedApi.getParameters().isEmpty()) {
                Map<String, Object> paramInfo = new HashMap<>();
                List<Map<String, Object>> pathParams = new ArrayList<>();
                List<Map<String, Object>> queryParams = new ArrayList<>();
                List<Map<String, Object>> headerParams = new ArrayList<>();
                List<Map<String, Object>> bodyParams = new ArrayList<>();

                for (ApiParameterEntity param : savedApi.getParameters()) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("key", param.getKey());
                    paramMap.put("required", param.getRequired());
                    paramMap.put("example", param.getExample());
                    paramMap.put("description", param.getDescription());
                    paramMap.put("parameterType", param.getParameterType());
                    paramMap.put("oracleType", param.getOracleType());
                    paramMap.put("apiType", param.getApiType());

                    if ("path".equals(param.getParameterType())) {
                        pathParams.add(paramMap);
                    } else if ("query".equals(param.getParameterType())) {
                        queryParams.add(paramMap);
                    } else if ("header".equals(param.getParameterType())) {
                        headerParams.add(paramMap);
                    } else if ("body".equals(param.getParameterType())) {
                        bodyParams.add(paramMap);
                    }
                }

                if (!pathParams.isEmpty()) {
                    paramInfo.put("pathParameters", pathParams);
                }
                if (!queryParams.isEmpty()) {
                    paramInfo.put("queryParameters", queryParams);
                }
                if (!headerParams.isEmpty()) {
                    paramInfo.put("headerParameters", headerParams);
                }
                if (!bodyParams.isEmpty()) {
                    paramInfo.put("bodyParameters", bodyParams);
                }
                metadata.put("parameters", paramInfo);

                // Add URL template with placeholders
                metadata.put("urlTemplate", buildUrlTemplate(savedApi));
                metadata.put("pathPlaceholders", buildPathPlaceholders(savedApi));
                metadata.put("queryPlaceholders", buildQueryPlaceholders(savedApi));
            }

            // Add collection info to metadata
            Map<String, Object> collectionMetadata = new HashMap<>();
            collectionMetadata.put("collectionId", collectionInfo.getCollectionId());
            collectionMetadata.put("collectionName", collectionInfo.getCollectionName());
            collectionMetadata.put("collectionType", collectionInfo.getCollectionType());
            collectionMetadata.put("folderId", collectionInfo.getFolderId());
            collectionMetadata.put("folderName", collectionInfo.getFolderName());
            metadata.put("collectionInfo", collectionMetadata);

            // Add all URLs
            Map<String, String> urls = new HashMap<>();
            urls.put("codeBase", "/plx/api/code-base/requests/" + codeBaseRequestId);
            urls.put("collections", "/plx/api/collections/collections/" + collectionId);
            urls.put("documentation", "/plx/api/documentation/collections/" + docCollectionId);
            urls.put("genEndpoint", genUrlInfo.getEndpointPath());
            urls.put("fullGenUrl", genUrlInfo.getFullUrl());
            urls.put("exampleGenUrl", genUrlInfo.getExampleUrl());
            urls.put("genUrlPattern", genUrlInfo.getUrlPattern());
            urls.put("curlExample", genUrlInfo.getCurlExample());
            urls.put("urlTemplate", buildUrlTemplate(savedApi));

            metadata.put("urls", urls);

            response.setMetadata(metadata);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + savedApi.getId() +
                    ", Gen URL: " + genUrlInfo.getEndpointPath());

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error generating API: " + e.getMessage());
            throw new RuntimeException("Failed to generate API: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Code Base with consistent URL from GenUrlInfo
     */
    private String generateCodeBase(GeneratedApiEntity api, String performedBy,
                                    GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                    GenUrlInfo genUrlInfo) {
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

            // Use the full URL from GenUrlInfo
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
                        Map<String, Object> requestBodyMap = objectMapper.readValue(
                                api.getRequestConfig().getSample(),
                                new TypeReference<Map<String, Object>>() {}
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
                    Map<String, Object> responseMap = objectMapper.readValue(
                            api.getResponseConfig().getSuccessSchema(),
                            new TypeReference<Map<String, Object>>() {}
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

    /**
     * Generate Collections with consistent URL from GenUrlInfo
     */
    private String generateCollections(GeneratedApiEntity api, String performedBy,
                                       GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                       GenUrlInfo genUrlInfo) {
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

            // Use the full URL from GenUrlInfo
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
                        savedRequest.setBody(objectMapper.writeValueAsString(bodyMap));
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

    @Transactional
    String generateDocumentation(GeneratedApiEntity api, String performedBy,
                                 GenerateApiRequestDTO request,
                                 String codeBaseRequestId,
                                 String collectionsCollectionId,
                                 CollectionInfoDTO collectionInfo,
                                 GenUrlInfo genUrlInfo) {
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

            // Use the full URL from GenUrlInfo
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

                // FIXED: Use HashMap instead of Map.of()
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
                endpointMetadata.put("urlTemplate", buildUrlTemplate(api));
                endpointMetadata.put("pathPlaceholders", buildPathPlaceholders(api));
                endpointMetadata.put("queryPlaceholders", buildQueryPlaceholders(api));
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
                        Map<String, Object> bodyMap = objectMapper.readValue(
                                api.getRequestConfig().getSample(),
                                new TypeReference<Map<String, Object>>() {}
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

            if (api.getTags() != null && !api.getTags().isEmpty()) {
                for (String tag : api.getTags()) {
                    if (tag != null && !tag.trim().isEmpty()) {
                        try {
                            String sql = "INSERT INTO tb_doc_endpoint_tags (endpoint_id, tag) VALUES (?, ?)";
                            oracleJdbcTemplate.update(sql, savedEndpoint.getId(), tag.trim());
                            log.debug("Inserted tag: {} for endpoint: {}", tag, savedEndpoint.getId());
                        } catch (Exception e) {
                            log.error("Failed to insert tag: {} - {}", tag, e.getMessage());
                        }
                    }
                }
            }

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
                    Map<String, Object> exampleMap = objectMapper.readValue(
                            api.getResponseConfig().getSuccessSchema(),
                            new TypeReference<Map<String, Object>>() {}
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
                    Map<String, Object> exampleMap = objectMapper.readValue(
                            api.getResponseConfig().getErrorSchema(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    errorExample.setExample(exampleMap);
                } catch (Exception e) {
                    log.warn("Failed to parse error response example: {}", e.getMessage());
                }

                responseExampleRepository.save(errorExample);
                log.debug("Saved error response example for endpoint");
            }

            responseExampleRepository.flush();

            generateDocumentationCodeExamples(api, savedEndpoint, codeBaseRequestId, genUrlInfo);

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

    /**
     * Generate an info file with gen URL details
     */
    private String generateGenInfoFile(GeneratedApiEntity api, String genEndpointPath, String exampleGenUrl) {
        GenUrlInfo genUrlInfo = buildGenUrlInfo(api);

        StringBuilder info = new StringBuilder();
        info.append("# API Gen URL Information\n");
        info.append("=======================\n\n");
        info.append("API ID: ").append(api.getId()).append("\n");
        info.append("API Name: ").append(api.getApiName()).append("\n");
        info.append("API Code: ").append(api.getApiCode()).append("\n");
        info.append("HTTP Method: ").append(api.getHttpMethod()).append("\n");
        info.append("Version: ").append(api.getVersion()).append("\n\n");

        info.append("## Gen Endpoint Path\n");
        info.append(genUrlInfo.getEndpointPath()).append("\n\n");

        info.append("## Full URL Pattern\n");
        info.append(genUrlInfo.getFullUrl()).append("\n\n");

        info.append("## URL with Parameter Placeholders\n");
        info.append(genUrlInfo.getUrlPattern()).append("\n\n");

        info.append("## Example URL\n");
        info.append(genUrlInfo.getExampleUrl()).append("\n\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            info.append("## Parameters by Location\n");

            List<ApiParameterEntity> pathParams = api.getParameters().stream()
                    .filter(p -> "path".equals(p.getParameterType()))
                    .collect(Collectors.toList());
            if (!pathParams.isEmpty()) {
                info.append("\n### Path Parameters\n");
                for (ApiParameterEntity param : pathParams) {
                    info.append("- ").append(param.getKey())
                            .append(" (").append(param.getOracleType()).append(")")
                            .append(Boolean.TRUE.equals(param.getRequired()) ? " [required]" : " [optional]")
                            .append("\n");
                    if (param.getExample() != null && !param.getExample().isEmpty()) {
                        info.append("  Example: ").append(param.getExample()).append("\n");
                    }
                    if (param.getDescription() != null) {
                        info.append("  Description: ").append(param.getDescription()).append("\n");
                    }
                }
            }

            List<ApiParameterEntity> queryParams = api.getParameters().stream()
                    .filter(p -> "query".equals(p.getParameterType()))
                    .collect(Collectors.toList());
            if (!queryParams.isEmpty()) {
                info.append("\n### Query Parameters\n");
                for (ApiParameterEntity param : queryParams) {
                    info.append("- ").append(param.getKey())
                            .append(" (").append(param.getOracleType()).append(")")
                            .append(Boolean.TRUE.equals(param.getRequired()) ? " [required]" : " [optional]")
                            .append("\n");
                    if (param.getExample() != null && !param.getExample().isEmpty()) {
                        info.append("  Example: ").append(param.getExample()).append("\n");
                    }
                    if (param.getDescription() != null) {
                        info.append("  Description: ").append(param.getDescription()).append("\n");
                    }
                }
            }

            List<ApiParameterEntity> headerParams = api.getParameters().stream()
                    .filter(p -> "header".equals(p.getParameterType()))
                    .collect(Collectors.toList());
            if (!headerParams.isEmpty()) {
                info.append("\n### Header Parameters\n");
                for (ApiParameterEntity param : headerParams) {
                    info.append("- ").append(param.getKey())
                            .append(" (").append(param.getOracleType()).append(")")
                            .append(Boolean.TRUE.equals(param.getRequired()) ? " [required]" : " [optional]")
                            .append("\n");
                    if (param.getExample() != null && !param.getExample().isEmpty()) {
                        info.append("  Example: ").append(param.getExample()).append("\n");
                    }
                    if (param.getDescription() != null) {
                        info.append("  Description: ").append(param.getDescription()).append("\n");
                    }
                }
            }

            List<ApiParameterEntity> bodyParams = api.getParameters().stream()
                    .filter(p -> "body".equals(p.getParameterType()))
                    .collect(Collectors.toList());
            if (!bodyParams.isEmpty()) {
                info.append("\n### Body Parameters\n");
                for (ApiParameterEntity param : bodyParams) {
                    info.append("- ").append(param.getKey())
                            .append(" (").append(param.getOracleType()).append(")")
                            .append(Boolean.TRUE.equals(param.getRequired()) ? " [required]" : " [optional]")
                            .append("\n");
                    if (param.getExample() != null && !param.getExample().isEmpty()) {
                        info.append("  Example: ").append(param.getExample()).append("\n");
                    }
                    if (param.getDescription() != null) {
                        info.append("  Description: ").append(param.getDescription()).append("\n");
                    }
                }
            }

            info.append("\n");
        }

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            info.append("## Authentication\n");
            info.append("Type: ").append(api.getAuthConfig().getAuthType()).append("\n");
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    info.append("API Key Header: ").append(api.getAuthConfig().getApiKeyHeader()).append("\n");
                    if (api.getAuthConfig().getApiSecretHeader() != null) {
                        info.append("API Secret Header: ").append(api.getAuthConfig().getApiSecretHeader()).append("\n");
                    }
                    break;
                case "BEARER":
                case "JWT":
                    info.append("Authorization Header: Bearer {token}\n");
                    info.append("JWT Issuer: ").append(api.getAuthConfig().getJwtIssuer()).append("\n");
                    break;
                case "BASIC":
                    info.append("Authorization Header: Basic base64(username:password)\n");
                    break;
                case "ORACLE_ROLES":
                    info.append("Oracle Session Header: X-Oracle-Session\n");
                    break;
            }
            info.append("\n");
        }

        info.append("## cURL Example\n");
        info.append(buildCurlExample(api, genUrlInfo.getExampleUrl())).append("\n");

        return info.toString();
    }

    public Map<String, String> generateApiCode(GeneratedApiEntity api) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Pass the gen URL information to code generators
        GenUrlInfo genUrlInfo = buildGenUrlInfo(api);

        generatedFiles.put("plsql", generatePlSqlPackage(api));
        generatedFiles.put("gen-info", generateGenInfoFile(api, genUrlInfo.getEndpointPath(), genUrlInfo.getExampleUrl()));

        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGenerateSwagger())) {
            generatedFiles.put("openapi", generateOpenApiSpec(api));
        }

        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGeneratePostman())) {
            generatedFiles.put("postman", generatePostmanCollection(api));
        }

        return generatedFiles;
    }

    /**
     * Updated generateDocumentationCodeExamples to use GenUrlInfo
     */
    private void generateDocumentationCodeExamples(GeneratedApiEntity api, APIEndpointEntity endpoint,
                                                   String codeBaseRequestId, GenUrlInfo genUrlInfo) {
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
                    String code = generateCodeForLanguage(api, language, genUrlInfo);

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

    /**
     * Generate code for language with GenUrlInfo
     */
    private String generateCodeForLanguage(GeneratedApiEntity api, String language, GenUrlInfo genUrlInfo) {
        String fullUrl = genUrlInfo.getFullUrl();
        String method = api.getHttpMethod() != null ? api.getHttpMethod().toLowerCase() : "get";

        switch (language) {
            case "curl":
                return generateFunctionalCurlCode(api, fullUrl);
            case "javascript":
                return generateFunctionalJavaScriptCode(api, fullUrl);
            case "python":
                return generateFunctionalPythonCode(api, fullUrl);
            case "java":
                return generateFunctionalJavaCode(api, fullUrl);
            case "csharp":
                return generateFunctionalCSharpCode(api, fullUrl);
            case "php":
                return generateFunctionalPhpCode(api, fullUrl);
            case "ruby":
                return generateFunctionalRubyCode(api, fullUrl);
            case "go":
                return generateFunctionalGoCode(api, fullUrl);
            default:
                return "// No code example available for " + language;
        }
    }

    // ==================== OVERLOADED METHODS FOR BACKWARD COMPATIBILITY ====================

    /**
     * Overloaded generateCodeBase for backward compatibility
     */
    private String generateCodeBase(GeneratedApiEntity api, String performedBy,
                                    GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        GenUrlInfo genUrlInfo = buildGenUrlInfo(api);
        return generateCodeBase(api, performedBy, request, collectionInfo, genUrlInfo);
    }

    /**
     * Overloaded generateCollections for backward compatibility
     */
    private String generateCollections(GeneratedApiEntity api, String performedBy,
                                       GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        GenUrlInfo genUrlInfo = buildGenUrlInfo(api);
        return generateCollections(api, performedBy, request, collectionInfo, genUrlInfo);
    }

    /**
     * Overloaded generateDocumentation for backward compatibility
     */
    String generateDocumentation(GeneratedApiEntity api, String performedBy,
                                 GenerateApiRequestDTO request,
                                 String codeBaseRequestId,
                                 String collectionsCollectionId,
                                 CollectionInfoDTO collectionInfo) {
        GenUrlInfo genUrlInfo = buildGenUrlInfo(api);
        return generateDocumentation(api, performedBy, request, codeBaseRequestId,
                collectionsCollectionId, collectionInfo, genUrlInfo);
    }

    // ==================== ALL EXISTING METHODS BELOW ====================
    // These methods remain exactly as they were in your original code

    @Transactional
    public GeneratedApiResponseDTO updateApi(String requestId, String apiId, String performedBy,
                                             GenerateApiRequestDTO request) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Updating API: " + apiId + " by: " + performedBy);

            // Get existing API
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Check if API code is being changed and if it already exists
            if (!api.getApiCode().equals(request.getApiCode()) &&
                    generatedAPIRepository.existsByApiCode(request.getApiCode())) {
                throw new RuntimeException("API code already exists: " + request.getApiCode());
            }

            // Convert source object map to DTO
            ApiSourceObjectDTO sourceObjectDTO = null;
            if (request.getSourceObject() != null && !request.getSourceObject().isEmpty()) {
                sourceObjectDTO = convertMapToSourceObjectDTO(request.getSourceObject());
            }

            // Validate source object if provided
            if (sourceObjectDTO != null) {
                validateSourceObject(sourceObjectDTO);
            }

            // Validate collection info
            if (request.getCollectionInfo() == null) {
                throw new RuntimeException("Collection information is required");
            }
            validateCollectionInfo(request.getCollectionInfo());

            // Update main API entity
            api.setApiName(request.getApiName());
            api.setApiCode(request.getApiCode());
            api.setDescription(request.getDescription());
            api.setVersion(request.getVersion());
            api.setStatus(request.getStatus() != null ? request.getStatus() : api.getStatus());
            api.setHttpMethod(request.getHttpMethod());
            api.setBasePath(request.getBasePath());
            api.setEndpointPath(request.getEndpointPath());
            api.setCategory(request.getCategory());
            api.setOwner(request.getOwner());
            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);
            api.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());

            if (sourceObjectDTO != null) {
                api.setSourceObjectInfo(objectMapper.convertValue(sourceObjectDTO, Map.class));
            }

            // Update collection info
            api.setCollectionInfo(objectMapper.convertValue(request.getCollectionInfo(), Map.class));

            // Clear existing relationships to replace them
            clearApiRelationships(api);

            // Update schema config
            if (request.getSchemaConfig() != null) {
                ApiSchemaConfigEntity schemaConfig = mapToSchemaConfigEntity(request.getSchemaConfig(), api);
                api.setSchemaConfig(schemaConfig);
            }

            // Update auth config
            if (request.getAuthConfig() != null) {
                ApiAuthConfigEntity authConfig = mapToAuthConfigEntity(request.getAuthConfig(), api);
                api.setAuthConfig(authConfig);
            }

            // Update request config
            if (request.getRequestBody() != null) {
                ApiRequestConfigEntity requestConfig = mapToRequestConfigEntity(request.getRequestBody(), api);
                api.setRequestConfig(requestConfig);
            }

            // Update response config
            if (request.getResponseBody() != null) {
                ApiResponseConfigEntity responseConfig = mapToResponseConfigEntity(request.getResponseBody(), api);
                api.setResponseConfig(responseConfig);
            }

            // Update settings
            if (request.getSettings() != null) {
                ApiSettingsEntity settings = mapToSettingsEntity(request.getSettings(), api);
                api.setSettings(settings);
            }

            // Update parameters from UI
            if (request.getParameters() != null && !request.getParameters().isEmpty()) {
                List<ApiParameterEntity> parameters = new ArrayList<>();
                for (int i = 0; i < request.getParameters().size(); i++) {
                    ApiParameterDTO paramDto = request.getParameters().get(i);
                    ApiParameterEntity param = mapToParameterEntity(paramDto, api);
                    param.setPosition(i);
                    parameters.add(param);
                }
                api.setParameters(parameters);
            } else if (sourceObjectDTO != null) {
                List<ApiParameterEntity> parameters = generateParametersFromSource(sourceObjectDTO, api);
                api.setParameters(parameters);
            } else {
                api.setParameters(new ArrayList<>());
            }

            // Update response mappings from UI
            if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
                List<ApiResponseMappingEntity> mappings = new ArrayList<>();
                for (int i = 0; i < request.getResponseMappings().size(); i++) {
                    ApiResponseMappingDTO mappingDto = request.getResponseMappings().get(i);
                    ApiResponseMappingEntity mapping = mapToResponseMappingEntity(mappingDto, api);
                    mapping.setPosition(i);
                    mappings.add(mapping);
                }
                api.setResponseMappings(mappings);
            } else if (sourceObjectDTO != null) {
                List<ApiResponseMappingEntity> mappings = generateResponseMappingsFromSource(sourceObjectDTO, api);
                api.setResponseMappings(mappings);
            } else {
                api.setResponseMappings(new ArrayList<>());
            }

            // Update headers
            if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                List<ApiHeaderEntity> headers = request.getHeaders().stream()
                        .map(headerDto -> mapToHeaderEntity(headerDto, api))
                        .collect(Collectors.toList());
                api.setHeaders(headers);
            } else {
                api.setHeaders(new ArrayList<>());
            }

            // Update tests
            if (request.getTests() != null) {
                List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
                api.setTests(tests);
            } else {
                api.setTests(new ArrayList<>());
            }

            // Save updated API
            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);

            // Optionally regenerate code and documentation if major changes
            if (shouldRegenerateComponents(request)) {
                regenerateComponents(savedApi, performedBy, request, request.getCollectionInfo());
            } else {
                updateCodeBase(savedApi, performedBy, request, request.getCollectionInfo());
                updateCollections(savedApi, performedBy, request, request.getCollectionInfo());
                updateDocumentation(savedApi, performedBy, request, request.getCollectionInfo(),
                        getCodeBaseRequestId(savedApi), getCollectionsCollectionId(savedApi));
            }

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API updated successfully: " + savedApi.getId());

            return mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error updating API: " + e.getMessage());
            throw new RuntimeException("Failed to update API: " + e.getMessage(), e);
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

            return mapToResponse(savedApi);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error partially updating API: " + e.getMessage());
            throw new RuntimeException("Failed to partially update API: " + e.getMessage(), e);
        }
    }

    private void clearApiRelationships(GeneratedApiEntity api) {
        if (api.getSchemaConfig() != null) {
            api.getSchemaConfig().setGeneratedApi(null);
        }
        if (api.getAuthConfig() != null) {
            api.getAuthConfig().setGeneratedApi(null);
        }
        if (api.getRequestConfig() != null) {
            api.getRequestConfig().setGeneratedApi(null);
        }
        if (api.getResponseConfig() != null) {
            api.getResponseConfig().setGeneratedApi(null);
        }
        if (api.getSettings() != null) {
            api.getSettings().setGeneratedApi(null);
        }
        if (api.getParameters() != null) {
            api.getParameters().forEach(p -> p.setGeneratedApi(null));
            api.setParameters(null);
        }
        if (api.getResponseMappings() != null) {
            api.getResponseMappings().forEach(m -> m.setGeneratedApi(null));
            api.setResponseMappings(null);
        }
        if (api.getHeaders() != null) {
            api.getHeaders().forEach(h -> h.setGeneratedApi(null));
            api.setHeaders(null);
        }
        if (api.getTests() != null) {
            api.getTests().forEach(t -> t.setGeneratedApi(null));
            api.setTests(null);
        }
    }

    @Transactional
    public void syncGeneratedComponents(GeneratedApiEntity api, String performedBy) {
        try {
            log.info("Syncing generated components for API: {}", api.getId());

            // Create a request DTO from the API entity
            GenerateApiRequestDTO request = convertEntityToRequestDTO(api);

            // Update Code Base
            updateCodeBase(api, performedBy, request);

            // Update Collections
            updateCollections(api, performedBy, request);

            // Update Documentation
            updateDocumentation(api, performedBy, request);

            log.info("Successfully synced all components for API: {}", api.getId());

        } catch (Exception e) {
            log.error("Failed to sync components: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync components: " + e.getMessage(), e);
        }
    }

    private ApiSchemaConfigDTO convertSchemaConfigToDTO(ApiSchemaConfigEntity entity) {
        if (entity == null) return null;

        return ApiSchemaConfigDTO.builder()
                .schemaName(entity.getSchemaName())
                .objectType(entity.getObjectType())
                .objectName(entity.getObjectName())
                .operation(entity.getOperation())
                .primaryKeyColumn(entity.getPrimaryKeyColumn())
                .sequenceName(entity.getSequenceName())
                .enablePagination(entity.getEnablePagination())
                .pageSize(entity.getPageSize())
                .enableSorting(entity.getEnableSorting())
                .defaultSortColumn(entity.getDefaultSortColumn())
                .defaultSortDirection(entity.getDefaultSortDirection())
                .isSynonym(entity.getIsSynonym())
                .targetType(entity.getTargetType())
                .targetName(entity.getTargetName())
                .targetOwner(entity.getTargetOwner())
                .build();
    }

    private ApiAuthConfigDTO convertAuthConfigToDTO(ApiAuthConfigEntity entity) {
        if (entity == null) return null;

        return ApiAuthConfigDTO.builder()
                .authType(entity.getAuthType())
                .apiKeyHeader(entity.getApiKeyHeader())
                .apiKeyValue(entity.getApiKeyValue())
                .apiKeySecret(entity.getApiKeySecret())
                .apiKeyLocation(entity.getApiKeyLocation())
                .apiKeyPrefix(entity.getApiKeyPrefix())
                .basicUsername(entity.getBasicUsername())
                .basicPassword(entity.getBasicPassword())
                .basicRealm(entity.getBasicRealm())
                .jwtSecret(entity.getJwtSecret())
                .jwtIssuer(entity.getJwtIssuer())
                .jwtAudience(entity.getJwtAudience())
                .jwtExpiration(entity.getJwtExpiration())
                .jwtAlgorithm(entity.getJwtAlgorithm())
                .oauthClientId(entity.getOauthClientId())
                .oauthClientSecret(entity.getOauthClientSecret())
                .oauthTokenUrl(entity.getOauthTokenUrl())
                .oauthAuthUrl(entity.getOauthAuthUrl())
                .oauthScopes(entity.getOauthScopes())
                .requiredRoles(entity.getRequiredRoles())
                .customAuthFunction(entity.getCustomAuthFunction())
                .validateSession(entity.getValidateSession())
                .checkObjectPrivileges(entity.getCheckObjectPrivileges())
                .ipWhitelist(entity.getIpWhitelist())
                .rateLimitRequests(entity.getRateLimitRequests())
                .rateLimitPeriod(entity.getRateLimitPeriod())
                .auditLevel(entity.getAuditLevel())
                .corsOrigins(Collections.singletonList(entity.getCorsOrigins()))
                .corsCredentials(entity.getCorsCredentials())
                .build();
    }

    private ApiRequestConfigDTO convertRequestConfigToDTO(ApiRequestConfigEntity entity) {
        if (entity == null) return null;

        List<String> allowedMediaTypes = null;
        if (entity.getAllowedMediaTypes() != null) {
            try {
                allowedMediaTypes = Arrays.asList(entity.getAllowedMediaTypes().split(","));
            } catch (Exception e) {
                log.warn("Failed to parse allowed media types: {}", e.getMessage());
            }
        }

        return ApiRequestConfigDTO.builder()
                .schemaType(entity.getSchemaType())
                .sample(entity.getSample())
                .maxSize(entity.getMaxSize())
                .validateSchema(entity.getValidateSchema())
                .allowedMediaTypes(allowedMediaTypes)
                .requiredFields(entity.getRequiredFields())
                .build();
    }

    private ApiResponseConfigDTO convertResponseConfigToDTO(ApiResponseConfigEntity entity) {
        if (entity == null) return null;

        return ApiResponseConfigDTO.builder()
                .successSchema(entity.getSuccessSchema())
                .errorSchema(entity.getErrorSchema())
                .includeMetadata(entity.getIncludeMetadata())
                .metadataFields(entity.getMetadataFields())
                .contentType(entity.getContentType())
                .compression(entity.getCompression())
                .build();
    }

    private ApiSettingsDTO convertSettingsToDTO(ApiSettingsEntity entity) {
        if (entity == null) return null;

        List<String> corsOrigins = null;
        if (entity.getCorsOrigins() != null) {
            try {
                corsOrigins = Arrays.asList(entity.getCorsOrigins().split(","));
            } catch (Exception e) {
                log.warn("Failed to parse cors origins: {}", e.getMessage());
            }
        }

        return ApiSettingsDTO.builder()
                .timeout(entity.getTimeout())
                .maxRecords(entity.getMaxRecords())
                .enableLogging(entity.getEnableLogging())
                .logLevel(entity.getLogLevel())
                .enableCaching(entity.getEnableCaching())
                .cacheTtl(entity.getCacheTtl())
                .enableRateLimiting(entity.getEnableRateLimiting())
                .rateLimit(entity.getRateLimit())
                .rateLimitPeriod(entity.getRateLimitPeriod())
                .enableAudit(entity.getEnableAudit())
                .auditLevel(entity.getAuditLevel())
                .generateSwagger(entity.getGenerateSwagger())
                .generatePostman(entity.getGeneratePostman())
                .generateClientSDK(entity.getGenerateClientSDK())
                .enableMonitoring(entity.getEnableMonitoring())
                .enableAlerts(entity.getEnableAlerts())
                .alertEmail(entity.getAlertEmail())
                .enableTracing(entity.getEnableTracing())
                .corsEnabled(entity.getCorsEnabled())
                .corsOrigins(corsOrigins)
                .build();
    }

    private ApiParameterDTO convertParameterToDTO(ApiParameterEntity entity) {
        if (entity == null) return null;

        return ApiParameterDTO.builder()
                .key(entity.getKey())
                .dbColumn(entity.getDbColumn())
                .dbParameter(entity.getDbParameter())
                .oracleType(entity.getOracleType())
                .apiType(entity.getApiType())
                .parameterType(entity.getParameterType())
                .required(entity.getRequired())
                .description(entity.getDescription())
                .example(entity.getExample())
                .validationPattern(entity.getValidationPattern())
                .defaultValue(entity.getDefaultValue())
                .position(entity.getPosition())
                .build();
    }

    private ApiResponseMappingDTO convertResponseMappingToDTO(ApiResponseMappingEntity entity) {
        if (entity == null) return null;

        return ApiResponseMappingDTO.builder()
                .apiField(entity.getApiField())
                .dbColumn(entity.getDbColumn())
                .oracleType(entity.getOracleType())
                .apiType(entity.getApiType())
                .format(entity.getFormat())
                .nullable(entity.getNullable())
                .isPrimaryKey(entity.getIsPrimaryKey())
                .includeInResponse(entity.getIncludeInResponse())
                .position(entity.getPosition())
                .build();
    }

    private ApiHeaderDTO convertHeaderToDTO(ApiHeaderEntity entity) {
        if (entity == null) return null;

        return ApiHeaderDTO.builder()
                .key(entity.getKey())
                .value(entity.getValue())
                .required(entity.getRequired())
                .description(entity.getDescription())
                .isRequestHeader(entity.getIsRequestHeader())
                .isResponseHeader(entity.getIsResponseHeader())
                .build();
    }

    private GenerateApiRequestDTO convertEntityToRequestDTO(GeneratedApiEntity api) {
        GenerateApiRequestDTO request = new GenerateApiRequestDTO();
        request.setApiName(api.getApiName());
        request.setApiCode(api.getApiCode());
        request.setDescription(api.getDescription());
        request.setVersion(api.getVersion());
        request.setHttpMethod(api.getHttpMethod());
        request.setBasePath(api.getBasePath());
        request.setEndpointPath(api.getEndpointPath());
        request.setCategory(api.getCategory());
        request.setOwner(api.getOwner());
        request.setTags(api.getTags());

        if (api.getSchemaConfig() != null) {
            request.setSchemaConfig(convertSchemaConfigToDTO(api.getSchemaConfig()));
        }

        if (api.getAuthConfig() != null) {
            request.setAuthConfig(convertAuthConfigToDTO(api.getAuthConfig()));
        }

        if (api.getRequestConfig() != null) {
            request.setRequestBody(convertRequestConfigToDTO(api.getRequestConfig()));
        }

        if (api.getResponseConfig() != null) {
            request.setResponseBody(convertResponseConfigToDTO(api.getResponseConfig()));
        }

        if (api.getSettings() != null) {
            request.setSettings(convertSettingsToDTO(api.getSettings()));
        }

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterDTO> paramDTOs = api.getParameters().stream()
                    .map(this::convertParameterToDTO)
                    .collect(Collectors.toList());
            request.setParameters(paramDTOs);
        }

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingDTO> mappingDTOs = api.getResponseMappings().stream()
                    .map(this::convertResponseMappingToDTO)
                    .collect(Collectors.toList());
            request.setResponseMappings(mappingDTOs);
        }

        if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
            List<ApiHeaderDTO> headerDTOs = api.getHeaders().stream()
                    .map(this::convertHeaderToDTO)
                    .collect(Collectors.toList());
            request.setHeaders(headerDTOs);
        }

        if (api.getSourceObjectInfo() != null) {
            try {
                Map<String, Object> sourceObject = api.getSourceObjectInfo();
                request.setSourceObject(sourceObject);
            } catch (Exception e) {
                log.warn("Failed to parse source object info: {}", e.getMessage());
            }
        }

        return request;
    }

    private void updateCodeBaseHeaders(RequestEntity requestEntity, GeneratedApiEntity api) {
        List<Map<String, Object>> headers = new ArrayList<>();

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                    Map<String, Object> headerMap = new HashMap<>();
                    headerMap.put("key", header.getKey());
                    headerMap.put("value", header.getValue() != null ? header.getValue() : "");
                    headerMap.put("description", header.getDescription());
                    headerMap.put("required", header.getRequired());
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

        requestEntity.setHeaders(headers);
    }

    private void updateCodeBaseParameters(RequestEntity requestEntity, GeneratedApiEntity api) {
        if (api.getParameters() != null) {
            List<Map<String, Object>> pathParams = api.getParameters().stream()
                    .filter(p -> "path".equals(p.getParameterType()))
                    .map(p -> {
                        Map<String, Object> param = new HashMap<>();
                        param.put("name", p.getKey());
                        param.put("type", p.getApiType());
                        param.put("required", p.getRequired());
                        param.put("description", p.getDescription());
                        param.put("key", p.getKey());
                        param.put("value", p.getExample() != null ? p.getExample() : "");
                        return param;
                    })
                    .collect(Collectors.toList());
            requestEntity.setPathParameters(pathParams);
        }
    }

    private void updateCodeBaseRequestBody(RequestEntity requestEntity, GeneratedApiEntity api) {
        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            try {
                Map<String, Object> requestBodyMap = objectMapper.readValue(
                        api.getRequestConfig().getSample(),
                        new TypeReference<Map<String, Object>>() {}
                );
                requestEntity.setRequestBody(requestBodyMap);
            } catch (Exception e) {
                log.warn("Failed to parse request body sample as JSON: {}", e.getMessage());
                Map<String, Object> fallbackBody = new HashMap<>();
                fallbackBody.put("content", api.getRequestConfig().getSample());
                requestEntity.setRequestBody(fallbackBody);
            }
        }
    }

    private void updateCodeBaseResponseExample(RequestEntity requestEntity, GeneratedApiEntity api) {
        if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            try {
                Map<String, Object> responseMap = objectMapper.readValue(
                        api.getResponseConfig().getSuccessSchema(),
                        new TypeReference<Map<String, Object>>() {}
                );
                requestEntity.setResponseExample(responseMap);
            } catch (Exception e) {
                log.warn("Failed to parse response example as JSON: {}", e.getMessage());
                Map<String, Object> fallbackResponse = new HashMap<>();
                fallbackResponse.put("data", api.getResponseConfig().getSuccessSchema());
                requestEntity.setResponseExample(fallbackResponse);
            }
        }
    }

    private void updateCodeBase(GeneratedApiEntity api, String performedBy,
                                GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Updating code base for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo != null ? collectionInfo.getCollectionName() : "unknown");

            String codeBaseRequestId = null;

            if (collectionInfo != null && collectionInfo.getCollectionId() != null) {
                Optional<RequestEntity> requestByCollection = findRequestByCollectionInfo(collectionInfo);
                if (requestByCollection.isPresent()) {
                    codeBaseRequestId = requestByCollection.get().getId();
                    log.info("Found code base request by collection info: {}", codeBaseRequestId);
                }
            }

            if (codeBaseRequestId == null) {
                codeBaseRequestId = getCodeBaseRequestId(api);
            }

            if (codeBaseRequestId == null) {
                log.warn("No code base request ID found for API: {}", api.getId());

                if (collectionInfo != null) {
                    log.info("Creating new code base request from collection info");
                    generateCodeBase(api, performedBy, request, collectionInfo);
                }
                return;
            }

            Optional<RequestEntity> existingRequest = codeBaseRequestRepository.findByIdWithLock(codeBaseRequestId);
            if (existingRequest.isPresent()) {
                RequestEntity requestEntity = existingRequest.get();

                requestEntity.setName(api.getApiName() + " - " + api.getHttpMethod());
                requestEntity.setMethod(api.getHttpMethod());

                // Use centralized URL building
                GenUrlInfo genUrlInfo = buildGenUrlInfo(api);
                String fullUrl = genUrlInfo.getFullUrl();
                requestEntity.setUrl(fullUrl);
                requestEntity.setDescription(api.getDescription());

                if (collectionInfo != null) {
                    updateCodeBaseCollectionInfo(requestEntity, collectionInfo, performedBy);
                }

                updateCodeBaseHeaders(requestEntity, api);
                updateCodeBaseParameters(requestEntity, api);
                updateCodeBaseRequestBody(requestEntity, api);
                updateCodeBaseResponseExample(requestEntity, api);

                RequestEntity savedRequest = codeBaseRequestRepository.saveAndFlush(requestEntity);
                log.info("Updated code base request: {} with collection: {}",
                        savedRequest.getId(),
                        collectionInfo != null ? collectionInfo.getCollectionName() : "default");

            } else {
                log.warn("Code base request not found with ID: {}", codeBaseRequestId);

                if (collectionInfo != null) {
                    log.info("Creating new code base request as existing one not found");
                    generateCodeBase(api, performedBy, request, collectionInfo);
                }
            }

        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic locking failure while updating code base: {}", e.getMessage());
            throw new RuntimeException("Failed to update Code Base due to concurrent modification. Please try again.", e);
        } catch (Exception e) {
            log.error("Failed to update code base: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update code base: " + e.getMessage(), e);
        }
    }

    private void updateCodeBaseCollectionInfo(RequestEntity requestEntity, CollectionInfoDTO collectionInfo,
                                              String performedBy) {
        try {
            Optional<com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity> existingCollection =
                    codeBaseCollectionRepository.findByIdWithLock(collectionInfo.getCollectionId());

            com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity collection;

            if (existingCollection.isPresent()) {
                collection = existingCollection.get();
                log.debug("Found existing code base collection with lock: {}", collection.getId());

                boolean needsUpdate = false;

                if (!collectionInfo.getCollectionName().equals(collection.getName())) {
                    collection.setName(collectionInfo.getCollectionName());
                    needsUpdate = true;
                }

                String expectedDescription = collectionInfo.getCollectionType() != null ?
                        "Collection type: " + collectionInfo.getCollectionType() :
                        "Collection for " + collectionInfo.getCollectionName();

                if (!expectedDescription.equals(collection.getDescription())) {
                    collection.setDescription(expectedDescription);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    collection.setUpdatedAt(LocalDateTime.now());
                    collection = codeBaseCollectionRepository.saveAndFlush(collection);
                    log.debug("Updated code base collection: {}", collection.getId());
                } else {
                    entityManager.refresh(collection);
                    log.debug("Refreshed code base collection: {}", collection.getId());
                }
            } else {
                collection = com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity.builder()
                        .id(collectionInfo.getCollectionId())
                        .name(collectionInfo.getCollectionName())
                        .description(collectionInfo.getCollectionType() != null ?
                                "Collection type: " + collectionInfo.getCollectionType() :
                                "Collection for " + collectionInfo.getCollectionName())
                        .version("1.0.0")
                        .owner(performedBy)
                        .isExpanded(false)
                        .isFavorite(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                collection = codeBaseCollectionRepository.saveAndFlush(collection);
                log.debug("Created new code base collection: {}", collection.getId());
            }

            requestEntity.setCollection(collection);

            if (collectionInfo.getFolderId() != null) {
                Optional<com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity> existingFolder =
                        codeBaseFolderRepository.findByIdWithLock(collectionInfo.getFolderId());

                com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity folder;

                if (existingFolder.isPresent()) {
                    folder = existingFolder.get();
                    log.debug("Found existing code base folder with lock: {}", folder.getId());

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
                        folder = codeBaseFolderRepository.saveAndFlush(folder);
                        log.debug("Updated code base folder: {}", folder.getId());
                    } else {
                        entityManager.refresh(folder);
                        log.debug("Refreshed code base folder: {}", folder.getId());
                    }
                } else {
                    folder = com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity.builder()
                            .id(collectionInfo.getFolderId())
                            .name(collectionInfo.getFolderName())
                            .description("Folder for " + collectionInfo.getFolderName())
                            .isExpanded(false)
                            .collection(collection)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    folder = codeBaseFolderRepository.saveAndFlush(folder);
                    log.debug("Created new code base folder: {}", folder.getId());
                }

                requestEntity.setFolder(folder);
            }

        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic locking failure in updateCodeBaseCollectionInfo: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to update code base collection info: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update code base collection info: " + e.getMessage(), e);
        }
    }

    private Optional<RequestEntity> findRequestByCollectionInfo(CollectionInfoDTO collectionInfo) {
        try {
            if (collectionInfo == null || collectionInfo.getCollectionId() == null) {
                return Optional.empty();
            }

            List<RequestEntity> requests = codeBaseRequestRepository.findByCollectionId(collectionInfo.getCollectionId());

            if (!requests.isEmpty()) {
                return Optional.of(requests.get(0));
            }

            if (collectionInfo.getFolderId() != null) {
                requests = codeBaseRequestRepository.findByFolderId(collectionInfo.getFolderId());
                if (!requests.isEmpty()) {
                    return Optional.of(requests.get(0));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("Failed to find request by collection info: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void updateCodeBase(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        CollectionInfoDTO collectionInfo = null;
        if (request != null && request.getCollectionInfo() != null) {
            collectionInfo = request.getCollectionInfo();
        } else if (api.getCollectionInfo() != null) {
            collectionInfo = objectMapper.convertValue(api.getCollectionInfo(), CollectionInfoDTO.class);
        }

        updateCodeBase(api, performedBy, request, collectionInfo);
    }

    private void updateCollectionsAuthConfig(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            if (requestEntity.getAuthConfig() != null) {
                requestEntity.setAuthConfig(null);
            }

            AuthConfigEntity authConfig = new AuthConfigEntity();
            authConfig.setId(UUID.randomUUID().toString());
            authConfig.setRequest(requestEntity);
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
            requestEntity.setAuthConfig(authConfig);
        } else {
            if (requestEntity.getAuthConfig() != null) {
                requestEntity.setAuthConfig(null);
            }
        }
    }

    private void updateCollectionsHeaders(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                    HeaderEntity header = new HeaderEntity();
                    header.setId(UUID.randomUUID().toString());
                    header.setKey(apiHeader.getKey() != null ? apiHeader.getKey() : "");
                    header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                    header.setDescription(apiHeader.getDescription() != null ? apiHeader.getDescription() : "");
                    header.setEnabled(apiHeader.getRequired() != null ? apiHeader.getRequired() : true);
                    header.setRequest(requestEntity);
                    requestEntity.getHeaders().add(header);
                }
            }
        }
    }

    private void updateCollectionsParameters(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (requestEntity.getParams() == null) {
            requestEntity.setParams(new ArrayList<>());
        }

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

                param.setRequest(requestEntity);

                requestEntity.getParams().add(param);
            }
        }
    }

    private void updateCollectionsBody(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            requestEntity.setBody(api.getRequestConfig().getSample());
        }
    }

    private void updateCollections(GeneratedApiEntity api, String performedBy,
                                   GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            log.info("Updating collections for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo != null ? collectionInfo.getCollectionName() : "unknown");

            String collectionId = null;

            if (collectionInfo != null && collectionInfo.getCollectionId() != null) {
                collectionId = collectionInfo.getCollectionId();
                log.info("Using collection ID from frontend: {}", collectionId);
            }

            if (collectionId == null) {
                collectionId = getCollectionsCollectionId(api);
                log.info("Using collection ID from metadata: {}", collectionId);
            }

            if (collectionId == null) {
                log.warn("No collection ID found for API: {}", api.getId());

                if (collectionInfo != null) {
                    log.info("Creating new collections from frontend data");
                    generateCollections(api, performedBy, request, collectionInfo);
                }
                return;
            }

            Optional<CollectionEntity> existingCollection = collectionsCollectionRepository.findById(collectionId);
            if (existingCollection.isPresent()) {
                CollectionEntity collection = existingCollection.get();

                collection.setName(collectionInfo != null ?
                        collectionInfo.getCollectionName() :
                        api.getApiName() + " Collection");
                collection.setDescription(api.getDescription() != null ? api.getDescription() :
                        "Collection for " + (collectionInfo != null ? collectionInfo.getCollectionName() : api.getApiName()));
                collection.setLastActivity(LocalDateTime.now());

                if (collectionInfo != null && collectionInfo.getCollectionType() != null) {
                    Map<String, Object> metadata = collection.getMetadata() != null ?
                            collection.getMetadata() : new HashMap<>();
                    metadata.put("collectionType", collectionInfo.getCollectionType());
                    collection.setMetadata(metadata);
                }

                if (collectionInfo != null && collectionInfo.getFolderId() != null) {
                    updateCollectionsFolder(collection, collectionInfo, performedBy);
                }

                collectionsCollectionRepository.save(collection);

                log.info("Updated collections collection: {} with folder: {}",
                        collectionId,
                        collectionInfo != null ? collectionInfo.getFolderName() : "default");

            } else {
                log.warn("Collections collection not found with ID: {}", collectionId);

                if (collectionInfo != null) {
                    log.info("Creating new collections from frontend data as existing not found");
                    generateCollections(api, performedBy, request, collectionInfo);
                }
            }

        } catch (Exception e) {
            log.error("Failed to update collections: {}", e.getMessage(), e);
        }
    }

    private void updateCollectionsFolder(CollectionEntity collection, CollectionInfoDTO collectionInfo,
                                         String performedBy) {
        try {
            Optional<FolderEntity> existingFolder = collectionsFolderRepository.findById(collectionInfo.getFolderId());

            if (existingFolder.isPresent()) {
                FolderEntity folder = existingFolder.get();
                folder.setName(collectionInfo.getFolderName());
                folder.setDescription("Folder for " + collectionInfo.getFolderName());
                folder.setCollection(collection);
                collectionsFolderRepository.save(folder);
                log.debug("Updated existing folder: {}", folder.getId());
            } else {
                FolderEntity newFolder = new FolderEntity();
                newFolder.setId(collectionInfo.getFolderId());
                newFolder.setName(collectionInfo.getFolderName());
                newFolder.setDescription("Folder for " + collectionInfo.getFolderName());
                newFolder.setExpanded(false);
                newFolder.setEditing(false);
                newFolder.setRequestCount(0);
                newFolder.setCollection(collection);
                collectionsFolderRepository.save(newFolder);
                log.debug("Created new folder: {}", newFolder.getId());
            }
        } catch (Exception e) {
            log.error("Failed to update collections folder: {}", e.getMessage());
        }
    }

    private void updateFolderRequestCount(String folderId) {
        try {
            Optional<FolderEntity> folderOpt = collectionsFolderRepository.findById(folderId);
            if (folderOpt.isPresent()) {
                FolderEntity folder = folderOpt.get();
                List<com.usg.apiAutomation.entities.postgres.collections.RequestEntity> requests =
                        collectionsRequestRepository.findByFolderId(folderId);
                folder.setRequestCount(requests != null ? requests.size() : 0);
                collectionsFolderRepository.save(folder);
            }
        } catch (Exception e) {
            log.error("Failed to update folder request count: {}", e.getMessage());
        }
    }

    private void updateCollections(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        CollectionInfoDTO collectionInfo = null;
        if (request != null && request.getCollectionInfo() != null) {
            collectionInfo = request.getCollectionInfo();
        } else if (api.getCollectionInfo() != null) {
            collectionInfo = objectMapper.convertValue(api.getCollectionInfo(), CollectionInfoDTO.class);
        }

        updateCollections(api, performedBy, request, collectionInfo);
    }

    private void updateDocumentationRateLimit(APIEndpointEntity endpoint, GeneratedApiEntity api) {
        if (api.getSettings() != null && api.getSettings().getEnableRateLimiting() != null) {
            Map<String, Object> rateLimit = new HashMap<>();
            rateLimit.put("enabled", api.getSettings().getEnableRateLimiting());
            rateLimit.put("requestsPerMinute", api.getSettings().getRateLimit() != null ?
                    api.getSettings().getRateLimit() : 60);
            rateLimit.put("strategy", "token_bucket");
            endpoint.setRateLimit(rateLimit);
        }
    }

    private void updateDocumentationRequestBody(APIEndpointEntity endpoint, GeneratedApiEntity api) {
        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            try {
                Map<String, Object> bodyMap = objectMapper.readValue(
                        api.getRequestConfig().getSample(),
                        new TypeReference<Map<String, Object>>() {}
                );
                endpoint.setRequestBodyExample(bodyMap);
            } catch (Exception e) {
                log.warn("Failed to parse request body example: {}", e.getMessage());
            }
        }
    }

    private void updateDocumentationHeaders(APIEndpointEntity endpoint, GeneratedApiEntity api) {
        List<com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity> existingHeaders =
                docHeaderRepository.findByEndpointId(endpoint.getId());
        docHeaderRepository.deleteAll(existingHeaders);

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                    com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity header =
                            new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();
                    header.setKey(apiHeader.getKey());
                    header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                    header.setDescription(apiHeader.getDescription());
                    header.setRequired(apiHeader.getRequired() != null ? apiHeader.getRequired() : false);
                    header.setEndpoint(endpoint);
                    docHeaderRepository.save(header);
                }
            }
        }

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity authHeader =
                    new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();
            authHeader.setRequired(true);
            authHeader.setEndpoint(endpoint);

            switch (api.getAuthConfig().getAuthType()) {
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
                    authHeader.setDescription("Basic authentication (username:password encoded in base64)");
                    break;
                case "ORACLE_ROLES":
                    authHeader.setKey("X-Oracle-Session");
                    authHeader.setValue("Your Oracle Session ID");
                    authHeader.setDescription("Oracle Database Session ID for authentication");
                    break;
            }
            docHeaderRepository.save(authHeader);
        }
    }

    private void updateDocumentationParameters(APIEndpointEntity endpoint, GeneratedApiEntity api) {
        try {
            log.info("Updating documentation parameters for endpoint: {}", endpoint.getId());

            List<com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity> existingParams =
                    docParameterRepository.findByEndpointId(endpoint.getId());

            if (existingParams != null && !existingParams.isEmpty()) {
                log.debug("Deleting {} existing parameters", existingParams.size());

                for (com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity param : existingParams) {
                    param.setEndpoint(null);
                }

                docParameterRepository.deleteAll(existingParams);
                docParameterRepository.flush();
            }

            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                int paramCount = 0;

                for (ApiParameterEntity apiParam : api.getParameters()) {

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

                    String paramType = apiParam.getApiType() != null ? apiParam.getApiType() : "string";
                    param.setParameterType(paramType);
                    param.setOracleType(apiParam.getOracleType());
                    param.setApiType(apiParam.getApiType());

                    String paramLocation = apiParam.getParameterType() != null ? apiParam.getParameterType() : "query";
                    param.setParameterLocation(paramLocation);
                    boolean required = apiParam.getRequired() != null ? apiParam.getRequired() : false;
                    param.setRequired(required);

                    String description = apiParam.getDescription() != null ? apiParam.getDescription() : "";
                    param.setDescription(description);
                    param.setDefaultValue(apiParam.getDefaultValue());
                    param.setExample(apiParam.getExample());
                    param.setValue(apiParam.getDefaultValue());

                    param.setValidationPattern(apiParam.getValidationPattern());

                    boolean inBody = apiParam.getInBody() != null ? apiParam.getInBody() : false;
                    param.setInBody(inBody);
                    boolean isPrimaryKey = apiParam.getIsPrimaryKey() != null ? apiParam.getIsPrimaryKey() : false;
                    param.setIsPrimaryKey(isPrimaryKey);

                    String paramMode = apiParam.getParamMode() != null ? apiParam.getParamMode() : "IN";
                    param.setParamMode(paramMode);

                    param.setEnabled(true);
                    int position = apiParam.getPosition() != null ? apiParam.getPosition() : paramCount;
                    param.setPosition(position);

                    param.setEndpoint(endpoint);

                    try {
                        com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity saved =
                                docParameterRepository.save(param);
                        docParameterRepository.flush();
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
                        paramCount, endpoint.getId());

            } else {
                log.debug("No parameters to save for endpoint: {}", endpoint.getId());
            }

        } catch (Exception e) {
            log.error("Error updating documentation parameters: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update documentation parameters: " + e.getMessage(), e);
        }
    }

    private void updateDocumentationResponses(APIEndpointEntity endpoint, GeneratedApiEntity api) {
        List<ResponseExampleEntity> existingResponses =
                responseExampleRepository.findByEndpointId(endpoint.getId());
        responseExampleRepository.deleteAll(existingResponses);

        if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            ResponseExampleEntity successExample = new ResponseExampleEntity();
            successExample.setStatusCode(200);
            successExample.setDescription("Successful response");
            successExample.setContentType(api.getResponseConfig().getContentType() != null ?
                    api.getResponseConfig().getContentType() : "application/json");
            successExample.setEndpoint(endpoint);

            try {
                Map<String, Object> exampleMap = objectMapper.readValue(
                        api.getResponseConfig().getSuccessSchema(),
                        new TypeReference<Map<String, Object>>() {}
                );
                successExample.setExample(exampleMap);
            } catch (Exception e) {
                log.warn("Failed to parse success response example: {}", e.getMessage());
            }

            responseExampleRepository.save(successExample);
        }

        if (api.getResponseConfig() != null && api.getResponseConfig().getErrorSchema() != null) {
            ResponseExampleEntity errorExample = new ResponseExampleEntity();
            errorExample.setStatusCode(400);
            errorExample.setDescription("Error response");
            errorExample.setContentType(api.getResponseConfig().getContentType() != null ?
                    api.getResponseConfig().getContentType() : "application/json");
            errorExample.setEndpoint(endpoint);

            try {
                Map<String, Object> exampleMap = objectMapper.readValue(
                        api.getResponseConfig().getErrorSchema(),
                        new TypeReference<Map<String, Object>>() {}
                );
                errorExample.setExample(exampleMap);
            } catch (Exception e) {
                log.warn("Failed to parse error response example: {}", e.getMessage());
            }

            responseExampleRepository.save(errorExample);
        }
    }

    private void addDocumentationChangelog(APICollectionEntity collection, GeneratedApiEntity api, String performedBy) {
        ChangelogEntryEntity changelog = new ChangelogEntryEntity();
        changelog.setVersion(api.getVersion());
        changelog.setDate(LocalDateTime.now().toString());
        changelog.setType("UPDATED");
        changelog.setAuthor(performedBy);
        changelog.setCollection(collection);

        List<String> changes = new ArrayList<>();
        changes.add("Updated endpoint: " + api.getApiName() + " (" + api.getHttpMethod() + ")");
        changes.add("API details modified");
        changelog.setChanges(changes);

        changelogRepository.save(changelog);
    }

    private void updateDocumentation(GeneratedApiEntity api, String performedBy,
                                     GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo,
                                     String codeBaseRequestId, String collectionsCollectionId) {
        try {
            log.info("Updating documentation for API: {} using collection: {}",
                    api.getApiCode(), collectionInfo != null ? collectionInfo.getCollectionName() : "unknown");

            String docCollectionId = collectionInfo != null ? collectionInfo.getCollectionId() : getDocumentationCollectionId(api);

            if (docCollectionId == null) {
                log.warn("No documentation collection ID found for API: {}", api.getId());
                return;
            }

            Optional<APICollectionEntity> existingCollection = docCollectionRepository.findById(docCollectionId);
            if (existingCollection.isPresent()) {
                APICollectionEntity collection = existingCollection.get();

                collection.setName(collectionInfo != null ?
                        collectionInfo.getCollectionName()  :
                        api.getApiName() );
                collection.setDescription(api.getDescription());
                collection.setVersion(api.getVersion());
                collection.setBaseUrl(api.getBasePath() != null ? api.getBasePath() : "");
                collection.setTags(api.getTags());
                collection.setUpdatedBy(performedBy);

                if (collectionInfo != null && collectionInfo.getFolderId() != null) {
                    updateDocumentationFolder(collection, collectionInfo, performedBy, api);
                }

                List<APIEndpointEntity> endpoints = endpointRepository.findByCollectionId(docCollectionId);
                APIEndpointEntity endpoint;

                if (!endpoints.isEmpty()) {
                    endpoint = endpoints.get(0);
                    log.info("Found existing endpoint: {}", endpoint.getId());
                } else {
                    endpoint = new APIEndpointEntity();
                    endpoint.setCollection(collection);

                    if (collectionInfo != null && collectionInfo.getFolderId() != null) {
                        Optional<com.usg.apiAutomation.entities.postgres.documentation.FolderEntity> folder =
                                docFolderRepository.findById(collectionInfo.getFolderId());
                        folder.ifPresent(endpoint::setFolder);
                    }

                    log.info("Creating new endpoint for collection: {}", docCollectionId);
                }

                // Use centralized URL building
                GenUrlInfo genUrlInfo = buildGenUrlInfo(api);
                String endpointUrl = genUrlInfo.getFullUrl();

                endpoint.setName(api.getApiName());
                endpoint.setMethod(api.getHttpMethod());
                endpoint.setUrl(endpointUrl);
                endpoint.setDescription(api.getDescription());
                endpoint.setApiVersion(api.getVersion());
                endpoint.setRequiresAuth(api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()));
                endpoint.setCategory(api.getCategory());
                endpoint.setTags(api.getTags());
                endpoint.setUpdatedBy(performedBy);

                updateDocumentationRateLimit(endpoint, api);
                updateDocumentationRequestBody(endpoint, api);

                APIEndpointEntity savedEndpoint = endpointRepository.save(endpoint);
                log.debug("Saved endpoint: {}", savedEndpoint.getId());

                updateDocumentationHeaders(savedEndpoint, api);
                updateDocumentationParameters(savedEndpoint, api);
                updateDocumentationResponses(savedEndpoint, api);

                addDocumentationChangelog(collection, api, performedBy, collectionInfo);

                docCollectionRepository.save(collection);

                log.info("Updated documentation collection: {} with folder: {}",
                        docCollectionId,
                        collectionInfo != null ? collectionInfo.getFolderName() : "default");

            } else {
                log.warn("Documentation collection not found with ID: {}", docCollectionId);

                if (collectionInfo != null) {
                    log.info("Creating new documentation collection from frontend data");
                    generateDocumentation(api, performedBy, request, codeBaseRequestId,
                            collectionsCollectionId, collectionInfo);
                }
            }

        } catch (Exception e) {
            log.error("Failed to update documentation: {}", e.getMessage(), e);
        }
    }

    private void updateDocumentationFolder(APICollectionEntity collection, CollectionInfoDTO collectionInfo,
                                           String performedBy, GeneratedApiEntity api) {
        try {
            Optional<com.usg.apiAutomation.entities.postgres.documentation.FolderEntity> existingFolder =
                    docFolderRepository.findById(collectionInfo.getFolderId());

            if (existingFolder.isPresent()) {
                com.usg.apiAutomation.entities.postgres.documentation.FolderEntity folder = existingFolder.get();
                folder.setName(collectionInfo.getFolderName());
                folder.setDescription("Folder for " + collectionInfo.getFolderName() + " APIs");
                folder.setUpdatedBy(performedBy);
                docFolderRepository.save(folder);
                log.debug("Updated existing folder: {}", folder.getId());
            } else {
                com.usg.apiAutomation.entities.postgres.documentation.FolderEntity newFolder =
                        new com.usg.apiAutomation.entities.postgres.documentation.FolderEntity();
                newFolder.setId(collectionInfo.getFolderId());
                newFolder.setName(collectionInfo.getFolderName());
                newFolder.setDescription("Folder for " + collectionInfo.getFolderName() + " APIs");
                newFolder.setCollection(collection);
                newFolder.setDisplayOrder(1);
                newFolder.setCreatedBy(performedBy);
                newFolder.setUpdatedBy(performedBy);
                docFolderRepository.save(newFolder);
                log.debug("Created new folder: {}", newFolder.getId());
            }
        } catch (Exception e) {
            log.error("Failed to update documentation folder: {}", e.getMessage());
        }
    }

    private void updateDocumentation(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        CollectionInfoDTO collectionInfo = null;
        if (request != null && request.getCollectionInfo() != null) {
            collectionInfo = request.getCollectionInfo();
        } else if (api.getCollectionInfo() != null) {
            collectionInfo = objectMapper.convertValue(api.getCollectionInfo(), CollectionInfoDTO.class);
        }

        updateDocumentation(api, performedBy, request, collectionInfo,
                getCodeBaseRequestId(api), getCollectionsCollectionId(api));
    }

    private void addDocumentationChangelog(APICollectionEntity collection, GeneratedApiEntity api,
                                           String performedBy, CollectionInfoDTO collectionInfo) {
        ChangelogEntryEntity changelog = new ChangelogEntryEntity();
        changelog.setVersion(api.getVersion());
        changelog.setDate(LocalDateTime.now().toString());
        changelog.setType("UPDATED");
        changelog.setAuthor(performedBy);
        changelog.setCollection(collection);

        List<String> changes = new ArrayList<>();
        changes.add("Updated endpoint: " + api.getApiName() + " (" + api.getHttpMethod() + ")");
        changes.add("API details modified");

        if (collectionInfo != null) {
            changes.add("Collection: " + collectionInfo.getCollectionName());
            changes.add("Folder: " + collectionInfo.getFolderName());
        }

        changelog.setChanges(changes);

        changelogRepository.save(changelog);
    }

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

    private boolean shouldRegenerateComponents(GenerateApiRequestDTO request) {
        return request.getRegenerateComponents() != null && request.getRegenerateComponents();
    }

    private void regenerateComponents(GeneratedApiEntity api, String performedBy,
                                      GenerateApiRequestDTO request, CollectionInfoDTO collectionInfo) {
        try {
            generateApiCode(api);
            generateCodeBase(api, performedBy, request, collectionInfo);
            generateCollections(api, performedBy, request, collectionInfo);
            String codeBaseRequestId = getCodeBaseRequestId(api);
            String collectionId = getCollectionsCollectionId(api);
            generateDocumentation(api, performedBy, request, codeBaseRequestId, collectionId, collectionInfo);

        } catch (Exception e) {
            log.warn("Failed to regenerate components: {}", e.getMessage());
        }
    }

    private List<ApiParameterEntity> generateParametersFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api) {
        List<ApiParameterEntity> parameters = new ArrayList<>();

        try {
            String targetType = sourceObject.getTargetType() != null ?
                    sourceObject.getTargetType() : sourceObject.getObjectType();
            String targetOwner = sourceObject.getTargetOwner() != null ?
                    sourceObject.getTargetOwner() : sourceObject.getOwner();
            String targetName = sourceObject.getTargetName() != null ?
                    sourceObject.getTargetName() : sourceObject.getObjectName();

            switch (targetType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    Map<String, Object> tableDetails = oracleSchemaService.getTableDetailsForFrontendPaginated(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName,
                            1, 100
                    );

                    Map<String, Object> tableData = (Map<String, Object>) tableDetails.get("data");
                    if (tableData != null && tableData.containsKey("columns")) {
                        List<Map<String, Object>> columns = (List<Map<String, Object>>) tableData.get("columns");
                        int position = 0;
                        for (Map<String, Object> column : columns) {
                            if ("SELECT".equals(sourceObject.getOperation())) {
                                boolean isPrimaryKey = column.get("isPrimaryKey") != null &&
                                        (Boolean) column.get("isPrimaryKey") ||
                                        "P".equals(column.get("constraint_type"));
                                if (isPrimaryKey) {
                                    ApiParameterEntity param = createParameterFromColumn(column, api, position++);
                                    param.setParameterType("path");
                                    parameters.add(param);
                                }
                            } else if ("INSERT".equals(sourceObject.getOperation())) {
                                boolean isNullable = "Y".equals(column.get("nullable"));
                                if (!isNullable) {
                                    ApiParameterEntity param = createParameterFromColumn(column, api, position++);
                                    param.setParameterType("body");
                                    parameters.add(param);
                                }
                            } else if ("UPDATE".equals(sourceObject.getOperation()) ||
                                    "DELETE".equals(sourceObject.getOperation())) {
                                boolean isPrimaryKey = column.get("isPrimaryKey") != null &&
                                        (Boolean) column.get("isPrimaryKey") ||
                                        "P".equals(column.get("constraint_type"));
                                if (isPrimaryKey) {
                                    ApiParameterEntity param = createParameterFromColumn(column, api, position++);
                                    param.setParameterType("path");
                                    parameters.add(param);
                                }
                            }
                        }
                    }
                    break;

                case "PROCEDURE":
                case "FUNCTION":
                    Map<String, Object> objectDetails;
                    if ("PROCEDURE".equalsIgnoreCase(targetType)) {
                        objectDetails = oracleSchemaService.getProcedureDetails(
                                UUID.randomUUID().toString(),
                                null,
                                "system",
                                targetName
                        );
                    } else {
                        objectDetails = oracleSchemaService.getFunctionDetails(
                                UUID.randomUUID().toString(),
                                null,
                                "system",
                                targetName
                        );
                    }

                    Map<String, Object> procData = (Map<String, Object>) objectDetails.get("data");
                    if (procData != null && procData.containsKey("parameters")) {
                        List<Map<String, Object>> params = (List<Map<String, Object>>) procData.get("parameters");
                        int position = 0;
                        for (Map<String, Object> param : params) {
                            ApiParameterEntity apiParam = createParameterFromArgument(param, api, position++);
                            parameters.add(apiParam);
                        }
                    }
                    break;

                case "PACKAGE":
                    if (sourceObject.getPackageProcedure() != null) {
                        Map<String, Object> packageItems = oracleSchemaService.getPackageItemsPaginated(
                                UUID.randomUUID().toString(),
                                null,
                                "system",
                                targetName,
                                targetOwner,
                                sourceObject.getPackageProcedure(),
                                1, 100
                        );
                    }
                    break;
            }

        } catch (Exception e) {
            log.warn("Could not auto-generate parameters from source: {}", e.getMessage());
        }

        return parameters;
    }

    private ApiParameterEntity createParameterFromColumn(Map<String, Object> column, GeneratedApiEntity api, int position) {
        String columnName = (String) (column.get("name") != null ? column.get("name") : column.get("COLUMN_NAME"));
        String dataType = (String) (column.get("data_type") != null ? column.get("data_type") : column.get("DATA_TYPE"));
        boolean nullable = "Y".equals(column.get("nullable")) || "Y".equals(column.get("NULLABLE"));

        return ApiParameterEntity.builder()
                .generatedApi(api)
                .key(columnName.toLowerCase())
                .dbColumn(columnName)
                .dbParameter(null)
                .oracleType(mapOracleType(dataType))
                .apiType(mapToApiType(dataType))
                .parameterType("query")
                .required(!nullable)
                .description("Column: " + columnName)
                .example(generateExample(dataType))
                .position(position)
                .build();
    }

    private ApiParameterEntity createParameterFromArgument(Map<String, Object> argument, GeneratedApiEntity api, int position) {
        String argName = (String) (argument.get("ARGUMENT_NAME") != null ?
                argument.get("ARGUMENT_NAME") : argument.get("argument_name"));
        String dataType = (String) (argument.get("DATA_TYPE") != null ?
                argument.get("DATA_TYPE") : argument.get("data_type"));
        String inOut = (String) (argument.get("IN_OUT") != null ?
                argument.get("IN_OUT") : argument.get("in_out"));

        return ApiParameterEntity.builder()
                .generatedApi(api)
                .key(argName != null ? argName.toLowerCase() : "param_" + position)
                .dbColumn(null)
                .dbParameter(argName)
                .oracleType(mapOracleType(dataType))
                .apiType(mapToApiType(dataType))
                .parameterType("IN".equals(inOut) ? "query" : "body")
                .required(!"OUT".equals(inOut))
                .description("Parameter: " + argName + " (" + inOut + ")")
                .example(generateExample(dataType))
                .position(position)
                .build();
    }

    private List<ApiResponseMappingEntity> generateResponseMappingsFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api) {
        List<ApiResponseMappingEntity> mappings = new ArrayList<>();

        try {
            String targetType = sourceObject.getTargetType() != null ?
                    sourceObject.getTargetType() : sourceObject.getObjectType();
            String targetOwner = sourceObject.getTargetOwner() != null ?
                    sourceObject.getTargetOwner() : sourceObject.getOwner();
            String targetName = sourceObject.getTargetName() != null ?
                    sourceObject.getTargetName() : sourceObject.getObjectName();

            switch (targetType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    Map<String, Object> tableDetails = oracleSchemaService.getTableDetailsForFrontend(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> tableData = (Map<String, Object>) tableDetails.get("data");
                    if (tableData != null && tableData.containsKey("columns")) {
                        List<Map<String, Object>> columns = (List<Map<String, Object>>) tableData.get("columns");
                        int position = 0;
                        for (Map<String, Object> column : columns) {
                            ApiResponseMappingEntity mapping = createResponseMappingFromColumn(column, api, position++);
                            mappings.add(mapping);
                        }
                    }
                    break;

                case "FUNCTION":
                    Map<String, Object> functionDetails = oracleSchemaService.getFunctionDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> funcData = (Map<String, Object>) functionDetails.get("data");
                    if (funcData != null && funcData.containsKey("returnType")) {
                        Map<String, Object> returnType = (Map<String, Object>) funcData.get("returnType");
                        ApiResponseMappingEntity mapping = createResponseMappingFromReturnType(returnType, api, 0);
                        mappings.add(mapping);
                    }
                    break;

                case "PROCEDURE":
                    Map<String, Object> procedureDetails = oracleSchemaService.getProcedureDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> procData = (Map<String, Object>) procedureDetails.get("data");
                    if (procData != null && procData.containsKey("parameters")) {
                        List<Map<String, Object>> params = (List<Map<String, Object>>) procData.get("parameters");
                        int position = 0;
                        for (Map<String, Object> param : params) {
                            String inOut = (String) param.get("IN_OUT");
                            if ("OUT".equals(inOut) || "IN/OUT".equals(inOut)) {
                                ApiResponseMappingEntity mapping = createResponseMappingFromArgument(param, api, position++);
                                mappings.add(mapping);
                            }
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            log.warn("Could not auto-generate response mappings from source: {}", e.getMessage());
        }

        return mappings;
    }

    private ApiResponseMappingEntity createResponseMappingFromColumn(Map<String, Object> column, GeneratedApiEntity api, int position) {
        String columnName = (String) (column.get("name") != null ? column.get("name") : column.get("COLUMN_NAME"));
        String dataType = (String) (column.get("data_type") != null ? column.get("data_type") : column.get("DATA_TYPE"));
        boolean nullable = "Y".equals(column.get("nullable")) || "Y".equals(column.get("NULLABLE"));
        boolean isPrimaryKey = column.get("isPrimaryKey") != null && (Boolean) column.get("isPrimaryKey") ||
                "P".equals(column.get("constraint_type"));

        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField(columnName.toLowerCase())
                .dbColumn(columnName)
                .oracleType(mapOracleType(dataType))
                .apiType(mapToApiType(dataType))
                .format(mapToFormat(dataType))
                .nullable(nullable)
                .isPrimaryKey(isPrimaryKey)
                .includeInResponse(true)
                .position(position)
                .build();
    }

    private ApiResponseMappingEntity createResponseMappingFromArgument(Map<String, Object> argument, GeneratedApiEntity api, int position) {
        String argName = (String) (argument.get("ARGUMENT_NAME") != null ?
                argument.get("ARGUMENT_NAME") : argument.get("argument_name"));
        String dataType = (String) (argument.get("DATA_TYPE") != null ?
                argument.get("DATA_TYPE") : argument.get("data_type"));

        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField(argName != null ? argName.toLowerCase() : "result")
                .dbColumn(argName)
                .oracleType(mapOracleType(dataType))
                .apiType(mapToApiType(dataType))
                .format(mapToFormat(dataType))
                .nullable(true)
                .isPrimaryKey(false)
                .includeInResponse(true)
                .position(position)
                .build();
    }

    private ApiResponseMappingEntity createResponseMappingFromReturnType(Map<String, Object> returnType, GeneratedApiEntity api, int position) {
        String dataType = (String) (returnType.get("DATA_TYPE") != null ?
                returnType.get("DATA_TYPE") : returnType.get("data_type"));

        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField("result")
                .dbColumn("RETURN_VALUE")
                .oracleType(mapOracleType(dataType))
                .apiType(mapToApiType(dataType))
                .format(mapToFormat(dataType))
                .nullable(false)
                .isPrimaryKey(false)
                .includeInResponse(true)
                .position(position)
                .build();
    }

    private String mapToApiType(String oracleType) {
        if (oracleType == null) return "string";

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR") || upperType.contains("CHAR") || upperType.contains("CLOB")) {
            return "string";
        } else if (upperType.contains("NUMBER") || upperType.contains("INTEGER") ||
                upperType.contains("FLOAT") || upperType.contains("DECIMAL")) {
            return "integer";
        } else if (upperType.contains("DATE") || upperType.contains("TIMESTAMP")) {
            return "string";
        } else if (upperType.contains("BLOB") || upperType.contains("RAW")) {
            return "string";
        }
        return "string";
    }

    private String mapToFormat(String oracleType) {
        if (oracleType == null) return null;

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("DATE")) {
            return "date";
        } else if (upperType.contains("TIMESTAMP")) {
            return "date-time";
        } else if (upperType.contains("NUMBER")) {
            return "double";
        } else if (upperType.contains("INTEGER")) {
            return "int32";
        } else if (upperType.contains("FLOAT")) {
            return "float";
        }
        return null;
    }

    private String mapOracleType(String dataType) {
        if (dataType == null) return "VARCHAR2";

        String upperType = dataType.toUpperCase();
        if (upperType.contains("VARCHAR")) return "VARCHAR2";
        if (upperType.contains("CHAR")) return "CHAR";
        if (upperType.contains("CLOB")) return "CLOB";
        if (upperType.contains("NUMBER")) return "NUMBER";
        if (upperType.contains("INTEGER")) return "NUMBER";
        if (upperType.contains("FLOAT")) return "NUMBER";
        if (upperType.contains("DATE")) return "DATE";
        if (upperType.contains("TIMESTAMP")) return "TIMESTAMP";
        if (upperType.contains("BLOB")) return "BLOB";
        return "VARCHAR2";
    }

    private String generateExample(String oracleType) {
        if (oracleType == null) return "";

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR") || upperType.contains("CHAR")) {
            return "sample";
        } else if (upperType.contains("NUMBER") || upperType.contains("INTEGER")) {
            return "1";
        } else if (upperType.contains("DATE")) {
            return "2024-01-01";
        } else if (upperType.contains("TIMESTAMP")) {
            return "2024-01-01T00:00:00Z";
        }
        return "";
    }

    /**
     * ==================== FIXED EXECUTE API METHOD ====================
     * This is the main method that handles API execution with proper authentication
     */
    @Transactional
    public ExecuteApiResponseDTO executeApi(String requestId, String performedBy,
                                            String apiId, ExecuteApiRequestDTO executeRequest,
                                            String clientIp, String userAgent) {
        long startTime = System.currentTimeMillis();

        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Executing API: " + apiId + " by: " + performedBy);

            // Get API configuration
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            // Parse source object info from stored JSON
            ApiSourceObjectDTO sourceObject = null;
            if (api.getSourceObjectInfo() != null && !api.getSourceObjectInfo().isEmpty()) {
                try {
                    sourceObject = objectMapper.convertValue(api.getSourceObjectInfo(), ApiSourceObjectDTO.class);
                } catch (Exception e) {
                    log.warn("Failed to parse source object info: {}", e.getMessage());
                }
            }

            // Log authentication attempt details
            loggerUtil.log("apiGeneration", "Auth Check - API ID: " + apiId +
                    ", API Auth Type: " + (api.getAuthConfig() != null ? api.getAuthConfig().getAuthType() : "NONE") +
                    ", Headers present: " + (executeRequest != null && executeRequest.getHeaders() != null ?
                    executeRequest.getHeaders().keySet() : "none"));

            // Validate authentication
            AuthenticationResult authResult = validateAuthentication(api, executeRequest);
            if (!authResult.isAuthenticated()) {
                loggerUtil.log("apiGeneration", "Authentication failed for API: " + apiId +
                        " - Reason: " + authResult.getReason());
                return createErrorResponse(requestId, 401,
                        "Authentication failed: " + authResult.getReason(), startTime);
            }

            // Validate authorization
            if (!validatorService.validateAuthorization(api, performedBy)) {
                return createErrorResponse(requestId, 403, "Authorization failed", startTime);
            }

            // Validate rate limiting
            if (!validatorService.checkRateLimit(api, clientIp)) {
                return createErrorResponse(requestId, 429, "Rate limit exceeded", startTime);
            }

            // ENHANCED: Map URL path parameters to API parameters
            if (executeRequest.getPathParams() != null && !executeRequest.getPathParams().isEmpty()) {
                executeRequest = mapUrlPathToApiParameters(api, executeRequest);
            }

            // Validate parameters
            Map<String, String> validationErrors = validatorService.validateParameters(api, executeRequest);
            if (!validationErrors.isEmpty()) {
                ExecuteApiResponseDTO response = createErrorResponse(requestId, 400, "Validation failed", startTime);
                response.setData(validationErrors);
                return response;
            }

            // Execute the API against Oracle database
            Object result = executeAgainstOracle(api, sourceObject, executeRequest);

            // Format response based on configuration
            Object formattedResponse = formatResponse(api, result);

            long executionTime = System.currentTimeMillis() - startTime;

            // Update API stats
            api.setTotalCalls(api.getTotalCalls() + 1);
            api.setLastCalledAt(LocalDateTime.now());
            generatedAPIRepository.save(api);

            // Log execution
            logExecution(api, executeRequest, formattedResponse, 200, executionTime,
                    performedBy, clientIp, userAgent, null);

            // Build response
            ExecuteApiResponseDTO response = ExecuteApiResponseDTO.builder()
                    .requestId(requestId)
                    .statusCode(200)
                    .data(formattedResponse)
                    .metadata(buildResponseMetadata(api, executionTime))
                    .executionTimeMs(executionTime)
                    .success(true)
                    .message("API executed successfully")
                    .build();

            // Add response headers if configured
            if (api.getHeaders() != null) {
                Map<String, String> responseHeaders = api.getHeaders().stream()
                        .filter(h -> Boolean.TRUE.equals(h.getIsResponseHeader()))
                        .collect(Collectors.toMap(
                                ApiHeaderEntity::getKey,
                                h -> h.getValue() != null ? h.getValue() : "",
                                (v1, v2) -> v1 // Handle duplicate keys
                        ));
                response.setHeaders(responseHeaders);
            }

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API executed successfully: " + apiId + " - Time: " + executionTime + "ms");

            return response;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error executing API: " + e.getMessage());

            // Log error execution
            logExecution(null, executeRequest, null, 500, executionTime,
                    performedBy, clientIp, userAgent, e.getMessage());

            return createErrorResponse(requestId, 500, e.getMessage(), startTime);
        }
    }

    /**
     * NEW METHOD: Map URL path parameters to API parameters based on the endpoint pattern
     * Updated to work with Map<String, Object>
     */
    private ExecuteApiRequestDTO mapUrlPathToApiParameters(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, Object> urlPathParams = request.getPathParams();
        if (urlPathParams == null || urlPathParams.isEmpty()) {
            return request;
        }

        // Get the API's endpoint pattern
        String endpointPath = api.getEndpointPath();
        if (endpointPath == null || endpointPath.isEmpty()) {
            return request;
        }

        // Parse the endpoint path to identify parameter names
        // Example: /api/v1/users/{userId}/orders/{orderId}
        List<String> pathParamNames = extractPathParameterNames(endpointPath);

        if (pathParamNames.isEmpty()) {
            return request;
        }

        // Map numbered parameters (param1, param2, etc.) to actual parameter names
        Map<String, Object> mappedPathParams = new HashMap<>();

        // Get the remaining path from the request (excluding /gen/{apiId})
        // The pathParams map contains param1, param2, etc. from the URL segments
        for (int i = 0; i < pathParamNames.size(); i++) {
            String paramName = pathParamNames.get(i);
            Object urlParamValue = urlPathParams.get("param" + (i + 1));

            if (urlParamValue != null) {
                mappedPathParams.put(paramName, urlParamValue);
                log.debug("Mapped URL param{} to {}: {}", i + 1, paramName, urlParamValue);
            }
        }

        // Also check for direct parameter names in the pathParams
        // This handles cases where the frontend already mapped them correctly
        for (String paramName : pathParamNames) {
            if (urlPathParams.containsKey(paramName)) {
                mappedPathParams.put(paramName, urlPathParams.get(paramName));
                log.debug("Found direct mapping for: {}", paramName);
            }
        }

        // Update the request with mapped parameters
        if (!mappedPathParams.isEmpty()) {
            // Merge with existing parameters (prefer mapped ones)
            Map<String, Object> updatedPathParams = new HashMap<>();
            if (request.getPathParams() != null) {
                updatedPathParams.putAll(request.getPathParams());
            }
            updatedPathParams.putAll(mappedPathParams);

            // Create new request DTO with updated parameters
            return ExecuteApiRequestDTO.builder()
                    .pathParams(updatedPathParams)
                    .queryParams(request.getQueryParams())
                    .headers(request.getHeaders())
                    .body(request.getBody())
                    .requestId(request.getRequestId())
                    .build();
        }

        return request;
    }

    /**
     * Extract parameter names from endpoint path
     * Example: /api/v1/users/{userId}/orders/{orderId} -> ["userId", "orderId"]
     */
    private List<String> extractPathParameterNames(String endpointPath) {
        List<String> paramNames = new ArrayList<>();

        if (endpointPath == null || endpointPath.isEmpty()) {
            return paramNames;
        }

        // Split the path and look for parameters in curly braces
        String[] segments = endpointPath.split("/");
        for (String segment : segments) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                // Extract parameter name without braces
                String paramName = segment.substring(1, segment.length() - 1);
                paramNames.add(paramName);
            }
        }

        return paramNames;
    }

    /**
     * Inner class to hold authentication result with reason
     */
    private static class AuthenticationResult {
        private final boolean authenticated;
        private final String reason;

        public AuthenticationResult(boolean authenticated, String reason) {
            this.authenticated = authenticated;
            this.reason = reason;
        }

        public boolean isAuthenticated() { return authenticated; }
        public String getReason() { return reason; }

        public static AuthenticationResult success() {
            return new AuthenticationResult(true, null);
        }

        public static AuthenticationResult failure(String reason) {
            return new AuthenticationResult(false, reason);
        }
    }

    /**
     * Validate authentication based on API configuration
     * Supports API Key, Basic Auth, Bearer/JWT, and No Auth
     */
    private AuthenticationResult validateAuthentication(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        // If API has no auth configured, always pass
        if (api.getAuthConfig() == null ||
                api.getAuthConfig().getAuthType() == null ||
                "NONE".equals(api.getAuthConfig().getAuthType())) {
            return AuthenticationResult.success();
        }

        String apiAuthType = api.getAuthConfig().getAuthType();

        switch (apiAuthType) {
            case "API_KEY":
                return validateApiKeyFromHeaders(api, request);

            case "BASIC":
                return validateBasicAuth(api, request);

            case "BEARER":
            case "JWT":
                return validateBearerToken(api, request);

            case "OAUTH2":
                return validateOAuth2(api, request);

            case "ORACLE_ROLES":
                return validateOracleSession(api, request);

            default:
                return AuthenticationResult.failure("Unsupported auth type: " + apiAuthType);
        }
    }

    /**
     * FIXED: Validate API Key authentication from headers
     * This handles the case where API keys are sent as headers while Auth tab shows "No Auth"
     */
    private AuthenticationResult validateApiKeyFromHeaders(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            log.debug("❌ API Key validation failed: No headers provided");
            return AuthenticationResult.failure("No headers provided for API Key authentication");
        }

        log.debug("🔍 Validating API Key - Available headers: {}", headers.keySet());

        // Log all headers for debugging
        headers.forEach((key, value) -> {
            log.debug("   Header: {} = {}", key, value);
        });

        // Get expected API key header and value from API config
        String expectedApiKeyHeader = api.getAuthConfig().getApiKeyHeader();
        String expectedApiKeyValue = api.getAuthConfig().getApiKeyValue();
        String expectedApiSecret = api.getAuthConfig().getApiKeySecret();

        log.debug("Expected API Key header: '{}', value: '{}'", expectedApiKeyHeader, expectedApiKeyValue);

        // If no API key is configured, authentication passes
        if (expectedApiKeyHeader == null || expectedApiKeyHeader.isEmpty() ||
                expectedApiKeyValue == null || expectedApiKeyValue.isEmpty()) {
            log.debug("⚠️ API Key not fully configured, skipping validation");
            return AuthenticationResult.success();
        }

        // Validate API Key header
        String actualApiKeyValue = null;

        // Try exact match
        if (headers.containsKey(expectedApiKeyHeader)) {
            actualApiKeyValue = headers.get(expectedApiKeyHeader);
            log.debug("✅ Found exact match for header: {}", expectedApiKeyHeader);
        } else {
            // Try case-insensitive match
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(expectedApiKeyHeader)) {
                    actualApiKeyValue = entry.getValue();
                    log.debug("✅ Found case-insensitive match: {} -> {}", entry.getKey(), expectedApiKeyHeader);
                    break;
                }
            }
        }

        if (actualApiKeyValue == null) {
            log.debug("❌ Missing required header: {}", expectedApiKeyHeader);
            return AuthenticationResult.failure("Missing required header: " + expectedApiKeyHeader);
        }

        log.debug("Comparing - Expected: '{}', Actual: '{}'", expectedApiKeyValue, actualApiKeyValue);

        if (!expectedApiKeyValue.equals(actualApiKeyValue)) {
            log.debug("❌ API Key value mismatch for header: {}", expectedApiKeyHeader);
            return AuthenticationResult.failure("Invalid API Key value for header: " + expectedApiKeyHeader);
        }

        // Validate API Secret if configured
        if (expectedApiSecret != null && !expectedApiSecret.isEmpty()) {
            String actualApiSecret = null;

            // Check for common secret header names
            String[] secretHeaders = {"X-API-Secret", "X-API-Secret-Key", "API-Secret", "Secret"};
            for (String secretHeader : secretHeaders) {
                if (headers.containsKey(secretHeader)) {
                    actualApiSecret = headers.get(secretHeader);
                    log.debug("Found exact match for secret header: {}", secretHeader);
                    break;
                }
                // Case-insensitive check
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(secretHeader)) {
                        actualApiSecret = entry.getValue();
                        log.debug("Found case-insensitive secret match: {} -> {}", entry.getKey(), secretHeader);
                        break;
                    }
                }
                if (actualApiSecret != null) break;
            }

            if (actualApiSecret == null) {
                log.debug("❌ Missing API Secret header");
                return AuthenticationResult.failure("Missing API Secret header");
            }

            if (!expectedApiSecret.equals(actualApiSecret)) {
                log.debug("❌ API Secret value mismatch");
                return AuthenticationResult.failure("Invalid API Secret");
            }
        }

        log.debug("✅ API Key validation successful for header: {}", expectedApiKeyHeader);
        return AuthenticationResult.success();
    }

    /**
     * Validate Basic Authentication
     */
    private AuthenticationResult validateBasicAuth(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for Basic authentication");
        }

        String authHeader = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Authorization")) {
                authHeader = entry.getValue();
                break;
            }
        }

        if (authHeader == null) {
            return AuthenticationResult.failure("Missing Authorization header for Basic authentication");
        }

        if (!authHeader.startsWith("Basic ")) {
            return AuthenticationResult.failure("Authorization header must start with 'Basic '");
        }

        try {
            String base64Credentials = authHeader.substring(6).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);

            if (parts.length != 2) {
                return AuthenticationResult.failure("Invalid Basic authentication format");
            }

            String username = parts[0];
            String password = parts[1];

            String expectedUsername = api.getAuthConfig().getBasicUsername();
            String expectedPassword = api.getAuthConfig().getBasicPassword();

            if (expectedUsername == null || expectedPassword == null) {
                return AuthenticationResult.failure("Basic authentication not configured on API");
            }

            if (!expectedUsername.equals(username)) {
                return AuthenticationResult.failure("Invalid username");
            }

            if (!expectedPassword.equals(password)) {
                return AuthenticationResult.failure("Invalid password");
            }

            log.debug("Basic authentication successful for user: {}", username);
            return AuthenticationResult.success();

        } catch (IllegalArgumentException e) {
            return AuthenticationResult.failure("Invalid Base64 encoding in Authorization header");
        }
    }

    /**
     * Validate Bearer/JWT token authentication
     */
    private AuthenticationResult validateBearerToken(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for Bearer authentication");
        }

        String authHeader = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Authorization")) {
                authHeader = entry.getValue();
                break;
            }
        }

        if (authHeader == null) {
            return AuthenticationResult.failure("Missing Authorization header for Bearer authentication");
        }

        if (!authHeader.startsWith("Bearer ")) {
            return AuthenticationResult.failure("Authorization header must start with 'Bearer '");
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            return AuthenticationResult.failure("Empty Bearer token");
        }

        String expectedToken = api.getAuthConfig().getJwtSecret();
        if (expectedToken == null || expectedToken.isEmpty()) {
            return AuthenticationResult.failure("Bearer token not configured on API");
        }

        if (!expectedToken.equals(token)) {
            return AuthenticationResult.failure("Invalid Bearer token");
        }

        log.debug("Bearer token validation successful");
        return AuthenticationResult.success();
    }

    /**
     * Validate OAuth2 token
     */
    private AuthenticationResult validateOAuth2(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for OAuth2 authentication");
        }

        String authHeader = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Authorization")) {
                authHeader = entry.getValue();
                break;
            }
        }

        if (authHeader == null) {
            return AuthenticationResult.failure("Missing Authorization header for OAuth2 authentication");
        }

        if (!authHeader.startsWith("Bearer ")) {
            return AuthenticationResult.failure("Authorization header must start with 'Bearer ' for OAuth2");
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            return AuthenticationResult.failure("Empty OAuth2 token");
        }

        String expectedToken = api.getAuthConfig().getOauthClientSecret();
        if (expectedToken != null && !expectedToken.isEmpty() && !expectedToken.equals(token)) {
            return AuthenticationResult.failure("Invalid OAuth2 token");
        }

        log.debug("OAuth2 token validation successful");
        return AuthenticationResult.success();
    }

    /**
     * Validate Oracle session authentication
     */
    private AuthenticationResult validateOracleSession(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for Oracle session authentication");
        }

        String sessionHeader = null;
        String[] sessionHeaders = {"X-Oracle-Session", "Oracle-Session-ID", "Session-ID"};

        for (String headerName : sessionHeaders) {
            if (headers.containsKey(headerName)) {
                sessionHeader = headers.get(headerName);
                break;
            }
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(headerName)) {
                    sessionHeader = entry.getValue();
                    break;

                }
            }
            if (sessionHeader != null) break;
        }

        if (sessionHeader == null) {
            return AuthenticationResult.failure("Missing Oracle session header");
        }

        if (sessionHeader.isEmpty()) {
            return AuthenticationResult.failure("Empty Oracle session ID");
        }

        log.debug("Oracle session validation successful");
        return AuthenticationResult.success();
    }

    private Object executeAgainstOracle(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                        ExecuteApiRequestDTO request) {
        try {
            if (sourceObject == null || api.getSchemaConfig() == null) {
                return generateSampleResponse(api);
            }

            String targetType = sourceObject.getTargetType() != null ?
                    sourceObject.getTargetType() : sourceObject.getObjectType();
            String targetName = sourceObject.getTargetName() != null ?
                    sourceObject.getTargetName() : sourceObject.getObjectName();
            String targetOwner = sourceObject.getTargetOwner() != null ?
                    sourceObject.getTargetOwner() : sourceObject.getOwner();

            switch (targetType.toUpperCase()) {
                case "TABLE":
                    return executeTableOperation(api, sourceObject, targetName, targetOwner, request);
                case "VIEW":
                    return executeViewOperation(api, sourceObject, targetName, targetOwner, request);
                case "PROCEDURE":
                    return executeProcedure(api, sourceObject, targetName, targetOwner, request);
                case "FUNCTION":
                    return executeFunction(api, sourceObject, targetName, targetOwner, request);
                case "PACKAGE":
                    return executePackageProcedure(api, sourceObject, targetName, targetOwner, request);
                default:
                    return generateSampleResponse(api);
            }

        } catch (Exception e) {
            log.error("Error executing Oracle operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute Oracle operation: " + e.getMessage(), e);
        }
    }

    private Object executeTableOperation(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                         String tableName, String owner, ExecuteApiRequestDTO request) {
        String operation = sourceObject.getOperation() != null ?
                sourceObject.getOperation() : api.getSchemaConfig().getOperation();

        Map<String, Object> params = new HashMap<>();
        if (request.getQueryParams() != null) params.putAll(request.getQueryParams());
        if (request.getPathParams() != null) params.putAll(request.getPathParams());
        if (request.getBody() instanceof Map) {
            params.putAll((Map<String, Object>) request.getBody());
        }

        switch (operation) {
            case "SELECT":
                return executeTableSelect(tableName, owner, params, api);
            case "INSERT":
                return executeTableInsert(tableName, owner, params, api);
            case "UPDATE":
                return executeTableUpdate(tableName, owner, params, api);
            case "DELETE":
                return executeTableDelete(tableName, owner, params, api);
            default:
                return generateSampleResponse(api);
        }
    }

    private Object executeTableSelect(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        if (owner != null && !owner.isEmpty()) {
            sql.append(owner).append(".");
        }
        sql.append(tableName);

        List<Object> paramValues = new ArrayList<>();
        if (params != null && !params.isEmpty()) {
            sql.append(" WHERE 1=1");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sql.append(" AND ").append(entry.getKey()).append(" = ?");
                paramValues.add(entry.getValue());
            }
        }

        if (api.getSchemaConfig() != null &&
                Boolean.TRUE.equals(api.getSchemaConfig().getEnablePagination())) {
            int pageSize = api.getSchemaConfig().getPageSize() != null ?
                    api.getSchemaConfig().getPageSize() : 10;
            int offset = 0;

            if (params != null && params.containsKey("page")) {
                int page = Integer.parseInt(params.get("page").toString());
                offset = (page - 1) * pageSize;
                sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
                paramValues.add(offset);
                paramValues.add(pageSize);
            }
        }

        List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(
                sql.toString(), paramValues.toArray());

        return results;
    }

    private Object executeTableInsert(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for INSERT");
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("?");
            paramValues.add(entry.getValue());
        }

        String sql = "INSERT INTO " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                " (" + columns + ") VALUES (" + values + ")";

        int rowsAffected = oracleJdbcTemplate.update(sql, paramValues.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("rowsAffected", rowsAffected);
        result.put("message", "Insert successful");

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            String pkColumn = api.getResponseMappings().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                    .map(ApiResponseMappingEntity::getDbColumn)
                    .findFirst()
                    .orElse(null);

            if (pkColumn != null && params.containsKey(pkColumn.toLowerCase())) {
                String selectSql = "SELECT * FROM " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                        " WHERE " + pkColumn + " = ?";
                List<Map<String, Object>> inserted = oracleJdbcTemplate.queryForList(
                        selectSql, params.get(pkColumn.toLowerCase()));
                if (!inserted.isEmpty()) {
                    result.put("data", inserted.get(0));
                }
            }
        }

        return result;
    }

    private Object executeTableUpdate(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for UPDATE");
        }

        List<String> pkColumns = api.getResponseMappings().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                .map(ApiResponseMappingEntity::getDbColumn)
                .collect(Collectors.toList());

        if (pkColumns.isEmpty()) {
            throw new RuntimeException("No primary key defined for UPDATE operation");
        }

        StringBuilder setClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        List<Object> setValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            boolean isPk = pkColumns.stream().anyMatch(pk -> pk.equalsIgnoreCase(key));

            if (isPk) {
                if (whereClause.length() > 0) {
                    whereClause.append(" AND ");
                } else {
                    whereClause.append(" WHERE ");
                }
                whereClause.append(key).append(" = ?");
                whereValues.add(entry.getValue());
            } else {
                if (setClause.length() > 0) {
                    setClause.append(", ");
                }
                setClause.append(key).append(" = ?");
                setValues.add(entry.getValue());
            }
        }

        if (whereValues.isEmpty()) {
            throw new RuntimeException("No primary key values provided for UPDATE");
        }

        String sql = "UPDATE " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                " SET " + setClause + whereClause;

        List<Object> allParams = new ArrayList<>(setValues);
        allParams.addAll(whereValues);

        int rowsAffected = oracleJdbcTemplate.update(sql, allParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("rowsAffected", rowsAffected);
        result.put("message", rowsAffected > 0 ? "Update successful" : "No rows updated");

        return result;
    }

    private Object executeTableDelete(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for DELETE");
        }

        StringBuilder whereClause = new StringBuilder();
        List<Object> whereValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            } else {
                whereClause.append(" WHERE ");
            }
            whereClause.append(entry.getKey()).append(" = ?");
            whereValues.add(entry.getValue());
        }

        String sql = "DELETE FROM " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName + whereClause;

        int rowsAffected = oracleJdbcTemplate.update(sql, whereValues.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("rowsAffected", rowsAffected);
        result.put("message", rowsAffected > 0 ? "Delete successful" : "No rows deleted");

        return result;
    }

    private Object executeViewOperation(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                        String viewName, String owner, ExecuteApiRequestDTO request) {
        return executeTableSelect(viewName, owner,
                request.getQueryParams() != null ? request.getQueryParams() : new HashMap<>(),
                api);
    }

    private Object executeProcedure(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                    String procedureName, String owner, ExecuteApiRequestDTO request) {
        Map<String, Object> inParams = new HashMap<>();
        if (request.getQueryParams() != null) inParams.putAll(request.getQueryParams());
        if (request.getPathParams() != null) inParams.putAll(request.getPathParams());
        if (request.getBody() instanceof Map) {
            inParams.putAll((Map<String, Object>) request.getBody());
        }

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                .withSchemaName(owner)
                .withProcedureName(procedureName);

        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    jdbcCall.declareParameters(
                            new SqlOutParameter(mapping.getDbColumn(), mapToSqlType(mapping.getOracleType()))
                    );
                }
            }
        }

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (inParams.containsKey(param.getKey())) {
                    String paramName = param.getDbParameter() != null ?
                            param.getDbParameter() :
                            (param.getDbColumn() != null ? param.getDbColumn() : param.getKey());

                    jdbcCall.declareParameters(
                            new SqlParameter(paramName, mapToSqlType(param.getOracleType()))
                    );
                }
            }
        }

        Map<String, Object> result = jdbcCall.execute(inParams);

        Map<String, Object> responseData = new HashMap<>();
        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                String dbColumn = mapping.getDbColumn();
                if (result.containsKey(dbColumn)) {
                    responseData.put(mapping.getApiField(), result.get(dbColumn));
                }
            }
        }

        return responseData;
    }

    private Object executeFunction(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                   String functionName, String owner, ExecuteApiRequestDTO request) {
        Map<String, Object> inParams = new HashMap<>();
        if (request.getQueryParams() != null) inParams.putAll(request.getQueryParams());
        if (request.getPathParams() != null) inParams.putAll(request.getPathParams());
        if (request.getBody() instanceof Map) {
            inParams.putAll((Map<String, Object>) request.getBody());
        }

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                .withSchemaName(owner)
                .withFunctionName(functionName);

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            String returnType = api.getResponseMappings().get(0).getOracleType();
            jdbcCall.declareParameters(
                    new SqlOutParameter("return", mapToSqlType(returnType))
            );
        }

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (inParams.containsKey(param.getKey())) {
                    String paramName = param.getDbParameter() != null ?
                            param.getDbParameter() :
                            (param.getDbColumn() != null ? param.getDbColumn() : param.getKey());

                    jdbcCall.declareParameters(
                            new SqlParameter(paramName, mapToSqlType(param.getOracleType()))
                    );
                }
            }
        }

        Map<String, Object> result = jdbcCall.execute(inParams);

        Map<String, Object> responseData = new HashMap<>();
        if (result.containsKey("return")) {
            responseData.put("result", result.get("return"));
        }

        return responseData;
    }

    private Object executePackageProcedure(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                           String packageName, String owner, ExecuteApiRequestDTO request) {
        if (sourceObject.getPackageProcedure() == null) {
            throw new RuntimeException("Package procedure not specified");
        }

        Map<String, Object> inParams = new HashMap<>();
        if (request.getQueryParams() != null) inParams.putAll(request.getQueryParams());
        if (request.getPathParams() != null) inParams.putAll(request.getPathParams());
        if (request.getBody() instanceof Map) {
            inParams.putAll((Map<String, Object>) request.getBody());
        }

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                .withSchemaName(owner)
                .withCatalogName(packageName)
                .withProcedureName(sourceObject.getPackageProcedure());

        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    jdbcCall.declareParameters(
                            new SqlOutParameter(mapping.getDbColumn(), mapToSqlType(mapping.getOracleType()))
                    );
                }
            }
        }

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (inParams.containsKey(param.getKey())) {
                    String paramName = param.getDbParameter() != null ?
                            param.getDbParameter() :
                            (param.getDbColumn() != null ? param.getDbColumn() : param.getKey());

                    jdbcCall.declareParameters(
                            new SqlParameter(paramName, mapToSqlType(param.getOracleType()))
                    );
                }
            }
        }

        Map<String, Object> result = jdbcCall.execute(inParams);

        Map<String, Object> responseData = new HashMap<>();
        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (result.containsKey(mapping.getDbColumn())) {
                    responseData.put(mapping.getApiField(), result.get(mapping.getDbColumn()));
                }
            }
        }

        return responseData;
    }

    public GeneratedApiEntity getApiEntity(String apiId) {
        return generatedAPIRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API not found: " + apiId));
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
                    .map(this::mapToExecutionLogDTO)
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
                    .map(this::mapToTestResultDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting test results: {}", e.getMessage());
            throw new RuntimeException("Failed to get test results: " + e.getMessage(), e);
        }
    }

    @Transactional
    public GeneratedApiResponseDTO updateApiStatus(String apiId, String status, String performedBy) {
        try {
            GeneratedApiEntity api = getApiEntity(apiId);

            List<String> validStatuses = Arrays.asList("DRAFT", "ACTIVE", "DEPRECATED", "ARCHIVED");
            if (!validStatuses.contains(status)) {
                throw new RuntimeException("Invalid status: " + status +
                        ". Valid statuses: " + String.join(", ", validStatuses));
            }

            api.setStatus(status);
            api.setUpdatedAt(LocalDateTime.now());
            api.setUpdatedBy(performedBy);

            GeneratedApiEntity updatedApi = generatedAPIRepository.save(api);

            loggerUtil.log("apiGeneration", "API status updated: " + apiId +
                    " to " + status + " by: " + performedBy);

            return mapToResponse(updatedApi);

        } catch (Exception e) {
            log.error("Error updating API status: {}", e.getMessage());
            throw new RuntimeException("Failed to update API status: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> validateSourceObject(ApiSourceObjectDTO sourceObject) {
        try {
            Map<String, Object> validation = oracleSchemaService.validateObject(
                    UUID.randomUUID().toString(),
                    null,
                    "system",
                    sourceObject.getObjectName(),
                    sourceObject.getObjectType(),
                    sourceObject.getOwner()
            );

            Map<String, Object> result = new HashMap<>();
            Map<String, Object> data = (Map<String, Object>) validation.get("data");

            if (data != null && Boolean.TRUE.equals(data.get("exists"))) {
                result.put("valid", true);
                result.put("exists", true);
                result.put("objectName", sourceObject.getObjectName());
                result.put("objectType", sourceObject.getObjectType());
                result.put("owner", sourceObject.getOwner());

                if (sourceObject.getObjectType().equalsIgnoreCase("SYNONYM")) {
                    Map<String, Object> resolved = oracleSchemaService.resolveSynonym(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            sourceObject.getObjectName()
                    );

                    Map<String, Object> resolvedData = (Map<String, Object>) resolved.get("data");
                    if (resolvedData != null && !resolvedData.containsKey("error")) {
                        result.put("targetOwner", resolvedData.get("targetOwner"));
                        result.put("targetName", resolvedData.get("targetName"));
                        result.put("targetType", resolvedData.get("targetType"));
                        result.put("isSynonym", true);
                    }
                }

                Map<String, Object> details = getSourceObjectDetails(sourceObject);
                result.put("details", details);

            } else {
                result.put("valid", false);
                result.put("exists", false);
                result.put("message", "Source object not found");
            }

            return result;

        } catch (Exception e) {
            log.error("Error validating source object: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    private Map<String, Object> getSourceObjectDetails(ApiSourceObjectDTO sourceObject) {
        Map<String, Object> details = new HashMap<>();

        try {
            String targetType = sourceObject.getTargetType() != null ?
                    sourceObject.getTargetType() : sourceObject.getObjectType();
            String targetName = sourceObject.getTargetName() != null ?
                    sourceObject.getTargetName() : sourceObject.getObjectName();
            String targetOwner = sourceObject.getTargetOwner() != null ?
                    sourceObject.getTargetOwner() : sourceObject.getOwner();

            switch (targetType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    Map<String, Object> tableDetails = oracleSchemaService.getTableDetailsForFrontend(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> tableData = (Map<String, Object>) tableDetails.get("data");
                    if (tableData != null) {
                        details.put("columns", tableData.get("columns"));
                        details.put("primaryKey", tableData.get("primaryKey"));
                        details.put("rowCount", tableData.get("rowCount"));
                    }
                    break;

                case "PROCEDURE":
                    Map<String, Object> procDetails = oracleSchemaService.getProcedureDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> procData = (Map<String, Object>) procDetails.get("data");
                    if (procData != null) {
                        details.put("parameters", procData.get("parameters"));
                        details.put("parameterCount", procData.get("parameterCount"));
                    }
                    break;

                case "FUNCTION":
                    Map<String, Object> funcDetails = oracleSchemaService.getFunctionDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> funcData = (Map<String, Object>) funcDetails.get("data");
                    if (funcData != null) {
                        details.put("parameters", funcData.get("parameters"));
                        details.put("returnType", funcData.get("returnType"));
                        details.put("parameterCount", funcData.get("parameterCount"));
                    }
                    break;
            }

        } catch (Exception e) {
            log.warn("Could not get source object details: {}", e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    private ApiExecutionLogDTO mapToExecutionLogDTO(ApiExecutionLogEntity entity) {
        if (entity == null) return null;

        return ApiExecutionLogDTO.builder()
                .id(entity.getId())
                .apiId(entity.getGeneratedApi() != null ? entity.getGeneratedApi().getId() : null)
                .requestId(entity.getRequestId())
                .requestParams(entity.getRequestParams() != null ? entity.getRequestParams().toString() : null)
                .requestBody(entity.getRequestBody() != null ? entity.getRequestBody().toString() : null)
                .responseBody(entity.getResponseBody() != null ? entity.getResponseBody().toString() : null)
                .responseStatus(entity.getResponseStatus())
                .executionTimeMs(entity.getExecutionTimeMs())
                .executedAt(entity.getExecutedAt())
                .executedBy(entity.getExecutedBy())
                .clientIp(entity.getClientIp())
                .userAgent(entity.getUserAgent())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    private ApiTestResultDTO mapToTestResultDTO(ApiTestEntity entity) {
        if (entity == null) return null;

        return ApiTestResultDTO.builder()
                .testName(entity.getTestName())
                .passed("PASSED".equals(entity.getStatus()))
                .executionTimeMs(entity.getExecutionTimeMs())
                .statusCode(extractStatusCode(entity))
                .actualResponse(entity.getActualResponse())
                .message(entity.getStatus())
                .build();
    }

    private int extractStatusCode(ApiTestEntity entity) {
        try {
            if (entity.getActualResponse() != null) {
                if (entity.getActualResponse().containsKey("statusCode")) {
                    Object statusCode = entity.getActualResponse().get("statusCode");
                    if (statusCode instanceof Integer) {
                        return (Integer) statusCode;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return 200;
    }

    private int mapToSqlType(String oracleType) {
        if (oracleType == null) return Types.VARCHAR;

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR")) return Types.VARCHAR;
        if (upperType.contains("CHAR")) return Types.CHAR;
        if (upperType.contains("CLOB")) return Types.CLOB;
        if (upperType.contains("NUMBER")) return Types.NUMERIC;
        if (upperType.contains("DATE")) return Types.DATE;
        if (upperType.contains("TIMESTAMP")) return Types.TIMESTAMP;
        if (upperType.contains("BLOB")) return Types.BLOB;
        return Types.VARCHAR;
    }

    private Object generateSampleResponse(GeneratedApiEntity api) {
        Map<String, Object> result = new HashMap<>();

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    switch (mapping.getApiType() != null ? mapping.getApiType() : "string") {
                        case "integer":
                        case "number":
                            result.put(mapping.getApiField(), 1);
                            break;
                        case "boolean":
                            result.put(mapping.getApiField(), true);
                            break;
                        case "string":
                        default:
                            if ("date".equals(mapping.getFormat())) {
                                result.put(mapping.getApiField(), LocalDateTime.now().toString());
                            } else {
                                result.put(mapping.getApiField(), "sample_" + mapping.getApiField());
                            }
                            break;
                    }
                }
            }
        } else {
            result.put("message", "API executed successfully");
            result.put("api", api.getApiCode());
            result.put("timestamp", LocalDateTime.now().toString());
        }

        return result;
    }

    @Transactional
    public ApiTestResultDTO testApi(String requestId, String performedBy,
                                    String apiId, ApiTestRequestDTO testRequest) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Testing API: " + apiId + " with test: " + testRequest.getTestName());

            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            long startTime = System.currentTimeMillis();

            ExecuteApiRequestDTO executeRequest = ExecuteApiRequestDTO.builder()
                    .pathParams(testRequest.getPathParams())
                    .queryParams(testRequest.getQueryParams())
                    .headers(testRequest.getHeaders())
                    .body(testRequest.getBody())
                    .requestId(requestId)
                    .build();

            ExecuteApiResponseDTO executionResult = executeApi(requestId, performedBy,
                    apiId, executeRequest, "127.0.0.1", "API-Test");

            long executionTime = System.currentTimeMillis() - startTime;

            boolean passed = compareResponses(executionResult, testRequest.getExpectedResponse());

            Map<String, Object> testDataMap = new HashMap<>();
            if (testRequest.getPathParams() != null) testDataMap.put("pathParams", testRequest.getPathParams());
            if (testRequest.getQueryParams() != null) testDataMap.put("queryParams", testRequest.getQueryParams());
            if (testRequest.getHeaders() != null) testDataMap.put("headers", testRequest.getHeaders());
            if (testRequest.getBody() != null) testDataMap.put("body", testRequest.getBody());
            testDataMap.put("expectedResponse", testRequest.getExpectedResponse());
            testDataMap.put("testName", testRequest.getTestName());
            testDataMap.put("testType", testRequest.getTestType());

            ApiTestEntity testEntity = ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName(testRequest.getTestName())
                    .testType(testRequest.getTestType() != null ? testRequest.getTestType() : "UNIT")
                    .testData(testDataMap)
                    .expectedResponse(testRequest.getExpectedResponse() != null ?
                            convertToMap(testRequest.getExpectedResponse()) : new HashMap<>())
                    .actualResponse(executionResult.getData() instanceof Map ?
                            (Map<String, Object>) executionResult.getData() : convertToMap(executionResult.getData()))
                    .status(passed ? "PASSED" : "FAILED")
                    .executionTimeMs(executionTime)
                    .executedAt(LocalDateTime.now())
                    .executedBy(performedBy)
                    .build();

            apiTestRepository.save(testEntity);

            return ApiTestResultDTO.builder()
                    .testName(testRequest.getTestName())
                    .passed(passed)
                    .executionTimeMs(executionTime)
                    .statusCode(executionResult.getStatusCode())
                    .actualResponse(executionResult.getData())
                    .message(passed ? "Test passed" : "Test failed - response mismatch")
                    .build();

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error testing API: " + e.getMessage());
            throw new RuntimeException("Failed to test API: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> convertToMap(Object obj) {
        if (obj == null) return new HashMap<>();
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        try {
            return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("value", obj.toString());
            return result;
        }
    }

    public GeneratedApiResponseDTO getApiDetails(String requestId, String apiId) {
        try {
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            GeneratedApiResponseDTO response = mapToResponse(api);

            response.setTotalCalls(api.getTotalCalls());
            response.setLastCalledAt(api.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                Map<String, Object> metadata = response.getMetadata() != null ?
                        response.getMetadata() : new HashMap<>();
                metadata.put("averageExecutionTimeMs", avgTime);
                response.setMetadata(metadata);
            }

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API details: " + e.getMessage());
            throw new RuntimeException("Failed to get API details: " + e.getMessage(), e);
        }
    }

    public ApiAnalyticsDTO getApiAnalytics(String requestId, String apiId,
                                           LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<ApiExecutionLogEntity> logs = executionLogRepository
                    .findByGeneratedApiIdAndExecutedAtBetween(apiId, startDate, endDate);

            long totalCalls = logs.size();
            double avgExecutionTime = logs.stream()
                    .mapToLong(ApiExecutionLogEntity::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);
            long totalErrors = logs.stream()
                    .filter(log -> log.getResponseStatus() != null && log.getResponseStatus() >= 400)
                    .count();
            double successRate = totalCalls > 0 ?
                    ((totalCalls - totalErrors) * 100.0 / totalCalls) : 0.0;

            Map<Integer, Long> statusDistribution = logs.stream()
                    .filter(log -> log.getResponseStatus() != null)
                    .collect(Collectors.groupingBy(
                            ApiExecutionLogEntity::getResponseStatus,
                            Collectors.counting()
                    ));

            List<Object[]> dailyStats = executionLogRepository
                    .getDailyCallStats(apiId, startDate, endDate);

            Map<String, Long> dailyCallStats = new HashMap<>();
            if (dailyStats != null) {
                for (Object[] stat : dailyStats) {
                    if (stat.length >= 2 && stat[0] != null && stat[1] != null) {
                        dailyCallStats.put(stat[0].toString(), ((Number) stat[1]).longValue());
                    }
                }
            }

            return ApiAnalyticsDTO.builder()
                    .apiId(apiId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalCalls(totalCalls)
                    .averageExecutionTimeMs(avgExecutionTime)
                    .totalErrors(totalErrors)
                    .successRate(successRate)
                    .statusDistribution(statusDistribution)
                    .dailyCallStats(dailyCallStats)
                    .build();

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API analytics: " + e.getMessage());
            throw new RuntimeException("Failed to get API analytics: " + e.getMessage(), e);
        }
    }


    private String generatePlSqlPackage(GeneratedApiEntity api) {
        StringBuilder sb = new StringBuilder();

        sb.append("-- ============================================================\n");
        sb.append("-- Generated API Package: ").append(api.getApiName()).append("\n");
        sb.append("-- Generated: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("-- Version: ").append(api.getVersion()).append("\n");
        sb.append("-- API Code: ").append(api.getApiCode()).append("\n");
        sb.append("-- ============================================================\n\n");

        if (api.getSchemaConfig() != null) {
            sb.append("-- Source Object: ");
            if (api.getSchemaConfig().getSchemaName() != null) {
                sb.append(api.getSchemaConfig().getSchemaName()).append(".");
            }
            sb.append(api.getSchemaConfig().getObjectName())
                    .append(" (").append(api.getSchemaConfig().getObjectType()).append(")\n");
            sb.append("-- Operation: ").append(api.getSchemaConfig().getOperation()).append("\n\n");
        }

        sb.append("CREATE OR REPLACE PACKAGE ").append(api.getApiCode()).append("_PKG AS\n\n");
        sb.append("  -- Main procedure\n");
        sb.append("  PROCEDURE execute_api(\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (int i = 0; i < api.getParameters().size(); i++) {
                ApiParameterEntity param = api.getParameters().get(i);
                String paramName = param.getDbParameter() != null ?
                        "p_" + param.getDbParameter() :
                        "p_" + (param.getKey() != null ? param.getKey() : "param" + i);

                sb.append("    ").append(paramName).append(" IN ").append(param.getOracleType() != null ? param.getOracleType() : "VARCHAR2");
                if (Boolean.TRUE.equals(param.getRequired())) {
                    sb.append(" NOT NULL");
                }
                if (param.getDefaultValue() != null && !param.getDefaultValue().isEmpty()) {
                    sb.append(" DEFAULT ").append(param.getDefaultValue());
                }
                if (i < api.getParameters().size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
        } else {
            sb.append("    -- No parameters\n");
        }
        sb.append("  );\n\n");

        sb.append("  FUNCTION validate_parameters RETURN BOOLEAN;\n");
        sb.append("  FUNCTION format_response RETURN CLOB;\n\n");

        sb.append("END ").append(api.getApiCode()).append("_PKG;\n");
        sb.append("/\n\n");

        sb.append("CREATE OR REPLACE PACKAGE BODY ").append(api.getApiCode()).append("_PKG AS\n\n");

        sb.append("  g_api_version CONSTANT VARCHAR2(10) := '").append(api.getVersion() != null ? api.getVersion() : "1.0").append("';\n");
        sb.append("  g_api_name CONSTANT VARCHAR2(100) := '").append(api.getApiName() != null ? api.getApiName() : "API").append("';\n\n");

        sb.append("  PROCEDURE execute_api(\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (int i = 0; i < api.getParameters().size(); i++) {
                ApiParameterEntity param = api.getParameters().get(i);
                String paramName = param.getDbParameter() != null ?
                        "p_" + param.getDbParameter() :
                        "p_" + (param.getKey() != null ? param.getKey() : "param" + i);

                sb.append("    ").append(paramName).append(" IN ").append(param.getOracleType() != null ? param.getOracleType() : "VARCHAR2");
                if (i < api.getParameters().size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
        } else {
            sb.append("    -- No parameters\n");
        }
        sb.append("  ) IS\n");
        sb.append("    v_cursor SYS_REFCURSOR;\n");
        sb.append("    v_start_time TIMESTAMP := SYSTIMESTAMP;\n");
        sb.append("    v_result ");

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            sb.append(api.getResponseMappings().get(0).getOracleType() != null ?
                    api.getResponseMappings().get(0).getOracleType() : "VARCHAR2");
        } else {
            sb.append("VARCHAR2(4000)");
        }
        sb.append(";\n");
        sb.append("  BEGIN\n\n");

        sb.append("    -- Log request\n");
        sb.append("    DBMS_OUTPUT.PUT_LINE('API Request: ' || g_api_name || ' - ' || SYSTIMESTAMP);\n\n");

        sb.append("    -- Validate parameters\n");
        sb.append("    IF NOT validate_parameters THEN\n");
        sb.append("      RAISE_APPLICATION_ERROR(-20001, 'Invalid parameters');\n");
        sb.append("    END IF;\n\n");

        if (api.getSchemaConfig() != null) {
            String schemaName = api.getSchemaConfig().getSchemaName();
            String objectName = api.getSchemaConfig().getObjectName();
            String operation = api.getSchemaConfig().getOperation();

            if (schemaName != null && objectName != null && operation != null) {
                switch (operation) {
                    case "SELECT":
                        sb.append("    OPEN v_cursor FOR\n");
                        sb.append("    SELECT ");
                        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                            sb.append(api.getResponseMappings().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getIncludeInResponse()))
                                    .map(ApiResponseMappingEntity::getDbColumn)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.joining(", ")));
                        } else {
                            sb.append("*");
                        }
                        sb.append("\n    FROM ");
                        if (schemaName != null && !schemaName.isEmpty()) {
                            sb.append(schemaName).append(".");
                        }
                        sb.append(objectName);
                        sb.append("\n    WHERE 1=1\n");

                        if (api.getParameters() != null) {
                            for (ApiParameterEntity param : api.getParameters()) {
                                if ("query".equals(param.getParameterType()) ||
                                        "path".equals(param.getParameterType())) {
                                    String columnName = param.getDbColumn() != null ? param.getDbColumn() : param.getKey();
                                    String paramName = param.getDbParameter() != null ?
                                            "p_" + param.getDbParameter() :
                                            "p_" + param.getKey();

                                    if (columnName != null && paramName != null) {
                                        sb.append("    AND ")
                                                .append(columnName)
                                                .append(" = ").append(paramName).append("\n");
                                    }
                                }
                            }
                        }

                        if (Boolean.TRUE.equals(api.getSchemaConfig().getEnablePagination())) {
                            sb.append("    OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY\n");
                        }
                        break;

                    case "INSERT":
                        sb.append("    INSERT INTO ");
                        if (schemaName != null && !schemaName.isEmpty()) {
                            sb.append(schemaName).append(".");
                        }
                        sb.append(objectName)
                                .append(" (\n      ");

                        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                            String columns = api.getResponseMappings().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getIncludeInResponse()))
                                    .map(ApiResponseMappingEntity::getDbColumn)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.joining(",\n      "));
                            sb.append(columns);
                        }
                        sb.append("\n    ) VALUES (\n      ");

                        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                            String values = api.getParameters().stream()
                                    .map(p -> {
                                        return p.getDbParameter() != null ?
                                                "p_" + p.getDbParameter() :
                                                "p_" + (p.getKey() != null ? p.getKey() : "param");
                                    })
                                    .collect(Collectors.joining(",\n      "));
                            sb.append(values);
                        }
                        sb.append("\n    );\n");

                        if (api.getSchemaConfig().getSequenceName() != null) {
                            sb.append("\n    -- Get generated ID\n");
                            sb.append("    SELECT ").append(api.getSchemaConfig().getSequenceName())
                                    .append(".CURRVAL INTO v_result FROM DUAL;\n");
                        }
                        break;

                    case "UPDATE":
                        sb.append("    UPDATE ");
                        if (schemaName != null && !schemaName.isEmpty()) {
                            sb.append(schemaName).append(".");
                        }
                        sb.append(objectName)
                                .append("\n    SET\n      ");

                        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                            String setClauses = api.getParameters().stream()
                                    .filter(p -> !"query".equals(p.getParameterType()) && !"path".equals(p.getParameterType()))
                                    .map(p -> {
                                        String columnName = p.getDbColumn() != null ? p.getDbColumn() : p.getKey();
                                        String paramName = p.getDbParameter() != null ?
                                                "p_" + p.getDbParameter() :
                                                "p_" + p.getKey();
                                        return (columnName != null && paramName != null) ?
                                                columnName + " = " + paramName : null;
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.joining(",\n      "));
                            sb.append(setClauses);
                        }
                        sb.append("\n    WHERE 1=1\n");

                        if (api.getParameters() != null) {
                            for (ApiParameterEntity param : api.getParameters()) {
                                if ("query".equals(param.getParameterType()) ||
                                        "path".equals(param.getParameterType())) {
                                    String columnName = param.getDbColumn() != null ? param.getDbColumn() : param.getKey();
                                    String paramName = param.getDbParameter() != null ?
                                            "p_" + param.getDbParameter() :
                                            "p_" + param.getKey();

                                    if (columnName != null && paramName != null) {
                                        sb.append("      AND ")
                                                .append(columnName)
                                                .append(" = ").append(paramName).append("\n");
                                    }
                                }
                            }
                        }
                        break;

                    case "DELETE":
                        sb.append("    DELETE FROM ");
                        if (schemaName != null && !schemaName.isEmpty()) {
                            sb.append(schemaName).append(".");
                        }
                        sb.append(objectName);
                        sb.append("\n    WHERE 1=1\n");

                        if (api.getParameters() != null) {
                            for (ApiParameterEntity param : api.getParameters()) {
                                if ("query".equals(param.getParameterType()) ||
                                        "path".equals(param.getParameterType())) {
                                    String columnName = param.getDbColumn() != null ? param.getDbColumn() : param.getKey();
                                    String paramName = param.getDbParameter() != null ?
                                            "p_" + param.getDbParameter() :
                                            "p_" + param.getKey();

                                    if (columnName != null && paramName != null) {
                                        sb.append("      AND ")
                                                .append(columnName)
                                                .append(" = ").append(paramName).append("\n");
                                    }
                                }
                            }
                        }
                        break;

                    case "EXECUTE":
                        sb.append("    -- Execute ").append(api.getSchemaConfig().getObjectType()).append("\n");
                        sb.append("    ");
                        if ("FUNCTION".equals(api.getSchemaConfig().getObjectType())) {
                            sb.append("v_result := ");
                        }
                        if (schemaName != null && !schemaName.isEmpty()) {
                            sb.append(schemaName).append(".");
                        }
                        sb.append(objectName).append("(\n");

                        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                            for (int i = 0; i < api.getParameters().size(); i++) {
                                ApiParameterEntity param = api.getParameters().get(i);
                                String paramName = param.getDbParameter() != null ?
                                        "p_" + param.getDbParameter() :
                                        "p_" + (param.getKey() != null ? param.getKey() : "param" + i);

                                sb.append("        ").append(paramName);
                                if (i < api.getParameters().size() - 1) {
                                    sb.append(",");
                                }
                                sb.append("\n");
                            }
                        }
                        sb.append("    );\n");
                        break;
                }
            }
        }

        sb.append("\n\n    -- Log execution\n");
        sb.append("    DBMS_OUTPUT.PUT_LINE('Execution time: ' || (SYSTIMESTAMP - v_start_time));\n\n");

        sb.append("    -- Return cursor for SELECT operations\n");
        sb.append("    IF '" + (api.getSchemaConfig() != null ? api.getSchemaConfig().getOperation() : "") + "' = 'SELECT' THEN\n");
        sb.append("      DBMS_SQL.RETURN_RESULT(v_cursor);\n");
        sb.append("    END IF;\n\n");

        sb.append("  EXCEPTION\n");
        sb.append("    WHEN OTHERS THEN\n");
        sb.append("      DBMS_OUTPUT.PUT_LINE('API Error: ' || SQLERRM);\n");
        sb.append("      RAISE_APPLICATION_ERROR(-20001, 'API Error: ' || SQLERRM);\n");
        sb.append("  END execute_api;\n\n");

        sb.append("  FUNCTION validate_parameters RETURN BOOLEAN IS\n");
        sb.append("  BEGIN\n");
        sb.append("    -- Add parameter validation logic here\n");
        sb.append("    RETURN TRUE;\n");
        sb.append("  END validate_parameters;\n\n");

        sb.append("  FUNCTION format_response RETURN CLOB IS\n");
        sb.append("    v_response CLOB;\n");
        sb.append("  BEGIN\n");
        sb.append("    -- Add response formatting logic here\n");
        sb.append("    RETURN v_response;\n");
        sb.append("  END format_response;\n\n");

        sb.append("END ").append(api.getApiCode()).append("_PKG;\n");
        sb.append("/\n");

        return sb.toString();
    }

    private String generateOpenApiSpec(GeneratedApiEntity api) {
        Map<String, Object> spec = new HashMap<>();

        spec.put("openapi", "3.0.0");

        Map<String, Object> info = new HashMap<>();
        info.put("title", api.getApiName() != null ? api.getApiName() : "API");
        info.put("description", api.getDescription() != null ? api.getDescription() : "");
        info.put("version", api.getVersion() != null ? api.getVersion() : "1.0.0");

        if (api.getOwner() != null) {
            Map<String, Object> contact = new HashMap<>();
            contact.put("name", api.getOwner());
            info.put("contact", contact);
        }
        spec.put("info", info);

        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> server = new HashMap<>();
        server.put("url", "{baseUrl}" + (api.getBasePath() != null ? api.getBasePath() : ""));
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> baseUrlVar = new HashMap<>();
        baseUrlVar.put("default", "https://api.example.com");
        variables.put("baseUrl", baseUrlVar);
        server.put("variables", variables);
        servers.add(server);
        spec.put("servers", servers);

        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> pathItem = new HashMap<>();
        Map<String, Object> operation = new HashMap<>();

        operation.put("summary", api.getApiName() != null ? api.getApiName() : "API");
        operation.put("description", api.getDescription() != null ? api.getDescription() : "");
        operation.put("operationId", api.getApiCode() != null ? api.getApiCode().toLowerCase() : "api");
        operation.put("tags", api.getTags() != null && !api.getTags().isEmpty() ? api.getTags() : Arrays.asList("default"));

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                Map<String, Object> paramSpec = new HashMap<>();
                paramSpec.put("name", param.getKey() != null ? param.getKey() : "param");
                paramSpec.put("in", param.getParameterType() != null ? param.getParameterType() : "query");
                paramSpec.put("description", param.getDescription() != null ? param.getDescription() : "");
                paramSpec.put("required", param.getRequired() != null ? param.getRequired() : false);

                Map<String, Object> schema = new HashMap<>();
                schema.put("type", param.getApiType() != null ? param.getApiType() : "string");
                if (param.getExample() != null) {
                    schema.put("example", param.getExample());
                }
                if (param.getValidationPattern() != null && !param.getValidationPattern().isEmpty()) {
                    schema.put("pattern", param.getValidationPattern());
                }
                paramSpec.put("schema", schema);

                parameters.add(paramSpec);
            }
            operation.put("parameters", parameters);
        }

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("description", "Request body");
            requestBody.put("required", true);

            Map<String, Object> content = new HashMap<>();
            Map<String, Object> mediaType = new HashMap<>();

            try {
                mediaType.put("schema", objectMapper.readValue(api.getRequestConfig().getSample(), Map.class));
            } catch (Exception e) {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                mediaType.put("schema", schema);
            }

            content.put(api.getRequestConfig().getSchemaType() != null ?
                    api.getRequestConfig().getSchemaType() : "application/json", mediaType);
            requestBody.put("content", content);

            operation.put("requestBody", requestBody);
        }

        Map<String, Object> responses = new HashMap<>();

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "Successful response");

        if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> mediaType = new HashMap<>();

            try {
                mediaType.put("schema", objectMapper.readValue(api.getResponseConfig().getSuccessSchema(), Map.class));
            } catch (Exception e) {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");

                if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                    Map<String, Object> properties = new HashMap<>();
                    for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                        if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                            Map<String, Object> propSchema = new HashMap<>();
                            propSchema.put("type", mapping.getApiType() != null ? mapping.getApiType() : "string");
                            if (mapping.getFormat() != null && !mapping.getFormat().isEmpty()) {
                                propSchema.put("format", mapping.getFormat());
                            }
                            if (Boolean.TRUE.equals(mapping.getNullable())) {
                                propSchema.put("nullable", true);
                            }
                            if (mapping.getApiField() != null) {
                                properties.put(mapping.getApiField(), propSchema);
                            }
                        }
                    }
                    if (!properties.isEmpty()) {
                        schema.put("properties", properties);
                    }
                }

                mediaType.put("schema", schema);
            }

            content.put(api.getResponseConfig().getContentType() != null ?
                    api.getResponseConfig().getContentType() : "application/json", mediaType);
            successResponse.put("content", content);
        }

        responses.put("200", successResponse);
        responses.put("400", Map.of("description", "Bad Request"));
        responses.put("401", Map.of("description", "Unauthorized"));
        responses.put("403", Map.of("description", "Forbidden"));
        responses.put("404", Map.of("description", "Not Found"));
        responses.put("429", Map.of("description", "Too Many Requests"));
        responses.put("500", Map.of("description", "Internal Server Error"));

        operation.put("responses", responses);

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            List<Map<String, List<String>>> security = new ArrayList<>();
            Map<String, List<String>> securityReq = new HashMap<>();
            securityReq.put(api.getAuthConfig().getAuthType().toLowerCase(), new ArrayList<>());
            security.add(securityReq);
            operation.put("security", security);
        }

        pathItem.put(api.getHttpMethod() != null ? api.getHttpMethod().toLowerCase() : "get", operation);
        paths.put(api.getEndpointPath() != null ? api.getEndpointPath() : "/", pathItem);
        spec.put("paths", paths);

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            Map<String, Object> components = new HashMap<>();
            Map<String, Object> securitySchemes = new HashMap<>();

            Map<String, Object> authScheme = new HashMap<>();
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    authScheme.put("type", "apiKey");
                    authScheme.put("name", api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                    authScheme.put("in", api.getAuthConfig().getApiKeyLocation() != null ?
                            api.getAuthConfig().getApiKeyLocation() : "header");
                    break;
                case "BASIC":
                    authScheme.put("type", "http");
                    authScheme.put("scheme", "basic");
                    break;
                case "JWT":
                case "BEARER":
                    authScheme.put("type", "http");
                    authScheme.put("scheme", "bearer");
                    authScheme.put("bearerFormat", "JWT");
                    break;
                case "OAUTH2":
                    authScheme.put("type", "oauth2");
                    Map<String, Object> flows = new HashMap<>();
                    Map<String, Object> clientCredentials = new HashMap<>();
                    clientCredentials.put("tokenUrl", api.getAuthConfig().getOauthTokenUrl() != null ?
                            api.getAuthConfig().getOauthTokenUrl() : "https://auth.example.com/token");
                    if (api.getAuthConfig().getOauthScopes() != null) {
                        Map<String, String> scopes = new HashMap<>();
                        for (String scope : api.getAuthConfig().getOauthScopes()) {
                            scopes.put(scope, scope + " access");
                        }
                        clientCredentials.put("scopes", scopes);
                    }
                    flows.put("clientCredentials", clientCredentials);
                    authScheme.put("flows", flows);
                    break;
                case "ORACLE_ROLES":
                    authScheme.put("type", "apiKey");
                    authScheme.put("name", "X-Oracle-Session");
                    authScheme.put("in", "header");
                    authScheme.put("description", "Oracle database session ID");
                    break;
            }

            securitySchemes.put(api.getAuthConfig().getAuthType().toLowerCase(), authScheme);
            components.put("securitySchemes", securitySchemes);
            spec.put("components", components);
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec);
        } catch (Exception e) {
            log.error("Error generating OpenAPI spec: {}", e.getMessage());
            return "{}";
        }
    }

    private String generatePostmanCollection(GeneratedApiEntity api) {
        Map<String, Object> collection = new HashMap<>();

        Map<String, Object> info = new HashMap<>();
        info.put("name", api.getApiName() != null ? api.getApiName() : "API Collection");
        info.put("description", api.getDescription() != null ? api.getDescription() : "");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put("info", info);

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", api.getApiName() != null ? api.getApiName() : "API");

        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getHttpMethod() != null ? api.getHttpMethod() : "GET");

        Map<String, Object> url = new HashMap<>();
        String fullPath = (api.getBasePath() != null ? api.getBasePath() : "") +
                (api.getEndpointPath() != null ? api.getEndpointPath() : "");
        url.put("raw", "{{baseUrl}}" + fullPath);
        url.put("host", Arrays.asList("{{baseUrl}}"));

        if (fullPath != null && !fullPath.isEmpty()) {
            String[] pathSegments = fullPath.split("/");
            List<String> pathList = new ArrayList<>();
            for (String segment : pathSegments) {
                if (!segment.isEmpty()) {
                    pathList.add(segment);
                }
            }
            url.put("path", pathList);
        }

        if (api.getParameters() != null) {
            List<Map<String, Object>> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    Map<String, Object> queryParam = new HashMap<>();
                    queryParam.put("key", param.getKey() != null ? param.getKey() : "");
                    queryParam.put("value", param.getExample() != null ? param.getExample() : "");
                    queryParam.put("description", param.getDescription() != null ? param.getDescription() : "");
                    queryParam.put("disabled", !Boolean.TRUE.equals(param.getRequired()));
                    queryParams.add(queryParam);
                }
            }
            if (!queryParams.isEmpty()) {
                url.put("query", queryParams);
            }
        }

        request.put("url", url);

        if (api.getHeaders() != null || (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()))) {
            List<Map<String, Object>> headers = new ArrayList<>();

            if (api.getHeaders() != null) {
                for (ApiHeaderEntity header : api.getHeaders()) {
                    if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                        Map<String, Object> headerSpec = new HashMap<>();
                        headerSpec.put("key", header.getKey() != null ? header.getKey() : "");
                        headerSpec.put("value", header.getValue() != null ? header.getValue() : "");
                        headerSpec.put("description", header.getDescription() != null ? header.getDescription() : "");
                        headerSpec.put("disabled", !Boolean.TRUE.equals(header.getRequired()));
                        headers.add(headerSpec);
                    }
                }
            }

            if (api.getAuthConfig() != null) {
                switch (api.getAuthConfig().getAuthType()) {
                    case "API_KEY":
                        Map<String, Object> apiKeyHeader = new HashMap<>();
                        apiKeyHeader.put("key", api.getAuthConfig().getApiKeyHeader() != null ?
                                api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                        apiKeyHeader.put("value", api.getAuthConfig().getApiKeyValue() != null ?
                                api.getAuthConfig().getApiKeyValue() : "{{apiKey}}");
                        apiKeyHeader.put("description", "API Key for authentication");
                        apiKeyHeader.put("disabled", false);
                        headers.add(apiKeyHeader);

                        if (api.getAuthConfig().getApiKeySecret() != null) {
                            Map<String, Object> apiSecretHeader = new HashMap<>();
                            apiSecretHeader.put("key", "X-API-Secret");
                            apiSecretHeader.put("value", "{{apiSecret}}");
                            apiSecretHeader.put("description", "API Secret for authentication");
                            apiSecretHeader.put("disabled", false);
                            headers.add(apiSecretHeader);
                        }
                        break;

                    case "BASIC":
                        Map<String, Object> basicHeader = new HashMap<>();
                        basicHeader.put("key", "Authorization");
                        basicHeader.put("value", "Basic {{base64Credentials}}");
                        basicHeader.put("description", "Basic Authentication");
                        basicHeader.put("disabled", false);
                        headers.add(basicHeader);
                        break;

                    case "JWT":
                    case "BEARER":
                        Map<String, Object> bearerHeader = new HashMap<>();
                        bearerHeader.put("key", "Authorization");
                        bearerHeader.put("value", "Bearer {{jwtToken}}");
                        bearerHeader.put("description", "Bearer Token Authentication");
                        bearerHeader.put("disabled", false);
                        headers.add(bearerHeader);
                        break;

                    case "ORACLE_ROLES":
                        Map<String, Object> oracleHeader = new HashMap<>();
                        oracleHeader.put("key", "X-Oracle-Session");
                        oracleHeader.put("value", "{{oracleSessionId}}");
                        oracleHeader.put("description", "Oracle Database Session ID");
                        oracleHeader.put("disabled", false);
                        headers.add(oracleHeader);
                        break;
                }
            }

            if (!headers.isEmpty()) {
                request.put("header", headers);
            }
        }

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            Map<String, Object> body = new HashMap<>();
            body.put("mode", "raw");
            body.put("raw", api.getRequestConfig().getSample());

            if (api.getRequestConfig().getSchemaType() != null) {
                Map<String, String> options = new HashMap<>();
                options.put("raw", api.getRequestConfig().getSchemaType());
                body.put("options", Map.of("raw", options));
            }

            request.put("body", body);
        }

        item.put("request", request);

        if (api.getResponseConfig() != null) {
            List<Map<String, Object>> responses = new ArrayList<>();

            if (api.getResponseConfig().getSuccessSchema() != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("name", "Success Response");
                response.put("originalRequest", request);
                response.put("status", "OK");
                response.put("code", 200);
                response.put("body", api.getResponseConfig().getSuccessSchema());
                responses.add(response);
            }

            if (api.getResponseConfig().getErrorSchema() != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("name", "Error Response");
                response.put("originalRequest", request);
                response.put("status", "Bad Request");
                response.put("code", 400);
                response.put("body", api.getResponseConfig().getErrorSchema());
                responses.add(response);
            }

            if (!responses.isEmpty()) {
                item.put("response", responses);
            }
        }

        items.add(item);
        collection.put("item", items);

        List<Map<String, Object>> variables = new ArrayList<>();

        Map<String, Object> baseUrlVar = new HashMap<>();
        baseUrlVar.put("key", "baseUrl");
        baseUrlVar.put("value", "https://api.example.com");
        baseUrlVar.put("type", "string");
        variables.add(baseUrlVar);

        if (api.getAuthConfig() != null) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    if (api.getAuthConfig().getApiKeyValue() != null) {
                        Map<String, Object> apiKeyVar = new HashMap<>();
                        apiKeyVar.put("key", "apiKey");
                        apiKeyVar.put("value", api.getAuthConfig().getApiKeyValue());
                        apiKeyVar.put("type", "string");
                        variables.add(apiKeyVar);
                    }
                    if (api.getAuthConfig().getApiKeySecret() != null) {
                        Map<String, Object> apiSecretVar = new HashMap<>();
                        apiSecretVar.put("key", "apiSecret");
                        apiSecretVar.put("value", api.getAuthConfig().getApiKeySecret());
                        apiSecretVar.put("type", "string");
                        variables.add(apiSecretVar);
                    }
                    break;

                case "BASIC":
                    if (api.getAuthConfig().getBasicUsername() != null &&
                            api.getAuthConfig().getBasicPassword() != null) {
                        String credentials = api.getAuthConfig().getBasicUsername() + ":" +
                                api.getAuthConfig().getBasicPassword();
                        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

                        Map<String, Object> basicVar = new HashMap<>();
                        basicVar.put("key", "base64Credentials");
                        basicVar.put("value", encoded);
                        basicVar.put("type", "string");
                        variables.add(basicVar);
                    }
                    break;

                case "JWT":
                    if (api.getAuthConfig().getJwtSecret() != null) {
                        Map<String, Object> jwtVar = new HashMap<>();
                        jwtVar.put("key", "jwtToken");
                        jwtVar.put("value", api.getAuthConfig().getJwtSecret());
                        jwtVar.put("type", "string");
                        variables.add(jwtVar);
                    }
                    break;

                case "ORACLE_ROLES":
                    Map<String, Object> oracleSessionVar = new HashMap<>();
                    oracleSessionVar.put("key", "oracleSessionId");
                    oracleSessionVar.put("value", "");
                    oracleSessionVar.put("type", "string");
                    variables.add(oracleSessionVar);
                    break;
            }
        }

        collection.put("variable", variables);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (Exception e) {
            log.error("Error generating Postman collection: {}", e.getMessage());
            return "{}";
        }
    }

    private ApiSourceObjectDTO convertMapToSourceObjectDTO(Map<String, Object> sourceObjectMap) {
        if (sourceObjectMap == null || sourceObjectMap.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.convertValue(sourceObjectMap, ApiSourceObjectDTO.class);
        } catch (Exception e) {
            log.error("Error converting source object map to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert source object: " + e.getMessage(), e);
        }
    }

    private ApiSourceObjectDTO convertToSourceObjectDTO(Object sourceObject) {
        if (sourceObject == null) {
            return null;
        }

        try {
            if (sourceObject instanceof ApiSourceObjectDTO) {
                return (ApiSourceObjectDTO) sourceObject;
            }

            if (sourceObject instanceof Map) {
                return objectMapper.convertValue(sourceObject, ApiSourceObjectDTO.class);
            }

            if (sourceObject instanceof String) {
                return objectMapper.readValue((String) sourceObject, ApiSourceObjectDTO.class);
            }

            log.warn("Unexpected source object type: {}", sourceObject.getClass());
            return null;

        } catch (Exception e) {
            log.error("Error converting source object to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert source object: " + e.getMessage(), e);
        }
    }

    private void validateCollectionInfo(CollectionInfoDTO collectionInfo) {
        if (collectionInfo.getCollectionId() == null || collectionInfo.getCollectionId().trim().isEmpty()) {
            throw new RuntimeException("Collection ID is required");
        }
        if (collectionInfo.getCollectionName() == null || collectionInfo.getCollectionName().trim().isEmpty()) {
            throw new RuntimeException("Collection name is required");
        }
        if (collectionInfo.getFolderId() == null || collectionInfo.getFolderId().trim().isEmpty()) {
            throw new RuntimeException("Folder ID is required");
        }
        if (collectionInfo.getFolderName() == null || collectionInfo.getFolderName().trim().isEmpty()) {
            throw new RuntimeException("Folder name is required");
        }
    }

    private void generateImplementations(GeneratedApiEntity api, RequestEntity request) {
        List<String> languages = Arrays.asList("java", "javascript", "python", "curl", "csharp", "php", "ruby", "go");

        for (String language : languages) {
            try {
                Optional<ImplementationEntity> existing = implementationRepository
                        .findByRequestIdAndLanguageAndComponent(request.getId(), language, "main");

                if (existing.isEmpty()) {
                    String code = generateCodeForLanguage(api, language);

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

    private void generateDocumentationCodeExamples(GeneratedApiEntity api, APIEndpointEntity endpoint,
                                                   String codeBaseRequestId) {
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
                    System.out.println("api::::::" + api);
                    System.out.println("language::::::" + language);
                    String code = generateCodeForLanguage(api, language);

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

    private String generateCodeForLanguage(GeneratedApiEntity api, String language) {
        String fullUrl = (api.getBasePath() != null ? api.getBasePath() : "") +
                (api.getEndpointPath() != null ? api.getEndpointPath() : "");
        String method = api.getHttpMethod() != null ? api.getHttpMethod().toLowerCase() : "get";

        switch (language) {
            case "curl":
                return generateFunctionalCurlCode(api, fullUrl);
            case "javascript":
                return generateFunctionalJavaScriptCode(api, fullUrl);
            case "python":
                return generateFunctionalPythonCode(api, fullUrl);
            case "java":
                return generateFunctionalJavaCode(api, fullUrl);
            case "csharp":
                return generateFunctionalCSharpCode(api, fullUrl);
            case "php":
                return generateFunctionalPhpCode(api, fullUrl);
            case "ruby":
                return generateFunctionalRubyCode(api, fullUrl);
            case "go":
                return generateFunctionalGoCode(api, fullUrl);
            default:
                return "// No code example available for " + language;
        }
    }

    private String generateFunctionalCurlCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder curl = new StringBuilder();
        curl.append("#!/bin/bash\n\n");
        curl.append("# Auto-generated functional cURL script for ").append(api.getApiName()).append("\n\n");

        String actualUrl = fullUrl;

        if (api.getParameters() != null) {
            List<String> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    queryParams.add(param.getKey() + "=" + param.getExample());
                }
            }
            if (!queryParams.isEmpty()) {
                actualUrl += (actualUrl.contains("?") ? "&" : "?") + String.join("&", queryParams);
            }
        }

        curl.append("curl -X ").append(api.getHttpMethod() != null ? api.getHttpMethod() : "GET").append(" \\\n");
        curl.append("  '").append(actualUrl).append("'");

        curl.append(" \\\n  -H 'Content-Type: application/json'");
        curl.append(" \\\n  -H 'Accept: application/json'");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            curl.append(" \\\n");
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    String value = api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "test-api-key";
                    curl.append("  -H '").append(header).append(": ").append(value).append("'");
                    break;
                case "BEARER":
                case "JWT":
                    curl.append("  -H 'Authorization: Bearer ").append(
                            api.getAuthConfig().getJwtSecret() != null ?
                                    api.getAuthConfig().getJwtSecret() : "test-jwt-token"
                    ).append("'");
                    break;
                case "BASIC":
                    String username = api.getAuthConfig().getBasicUsername() != null ?
                            api.getAuthConfig().getBasicUsername() : "testuser";
                    String password = api.getAuthConfig().getBasicPassword() != null ?
                            api.getAuthConfig().getBasicPassword() : "testpass";
                    curl.append("  -u '").append(username).append(":").append(password).append("'");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) && header.getKey() != null) {
                    curl.append(" \\\n");
                    curl.append("  -H '").append(header.getKey()).append(": ").append(
                            header.getValue() != null ? header.getValue() : ""
                    ).append("'");
                }
            }
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            curl.append(" \\\n");
            curl.append("  -d '").append(api.getRequestConfig().getSample().replace("'", "\\'")).append("'");
        }

        return curl.toString();
    }

    private String generateFunctionalJavaScriptCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder js = new StringBuilder();
        js.append("// Auto-generated functional JavaScript code for ").append(api.getApiName()).append("\n\n");

        String serverUrl = "{{baseUrl}}/gen/" + api.getId();
        String baseUrl = serverUrl + fullUrl;

        js.append("// Base URL\n");
        js.append("const baseUrl = '").append(escapeJavaScriptString(baseUrl)).append("';\n\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                js.append("// Build URL with query parameters\n");
                js.append("const queryParams = new URLSearchParams();\n");

                for (ApiParameterEntity param : queryParams) {
                    if (param.getExample() != null) {
                        js.append("queryParams.append('").append(escapeJavaScriptString(param.getKey()))
                                .append("', '").append(escapeJavaScriptString(param.getExample())).append("');\n");
                    } else {
                        js.append("// Optional parameter: ").append(param.getKey()).append("\n");
                        js.append("if (params && params.").append(param.getKey()).append(") {\n");
                        js.append("  queryParams.append('").append(escapeJavaScriptString(param.getKey()))
                                .append("', params.").append(param.getKey()).append(");\n");
                        js.append("}\n");
                    }
                }

                js.append("const url = queryParams.toString() ? \n");
                js.append("  `${baseUrl}?${queryParams.toString()}` : baseUrl;\n\n");
            } else {
                js.append("const url = baseUrl;\n\n");
            }
        } else {
            js.append("const url = baseUrl;\n\n");
        }

        js.append("// Headers with actual values\n");
        js.append("const headers = {\n");
        js.append("  'Content-Type': 'application/json',\n");
        js.append("  'Accept': 'application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    js.append("  '").append(escapeJavaScriptString(
                                    api.getAuthConfig().getApiKeyHeader() != null ?
                                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key"))
                            .append("': '").append(escapeJavaScriptString(
                                    api.getAuthConfig().getApiKeyValue() != null ?
                                            api.getAuthConfig().getApiKeyValue() : "")).append("',\n");
                    break;
                case "BEARER":
                case "JWT":
                    js.append("  'Authorization': 'Bearer ").append(escapeJavaScriptString(
                            api.getAuthConfig().getJwtSecret() != null ?
                                    api.getAuthConfig().getJwtSecret() : "")).append("',\n");
                    break;
                case "BASIC":
                    js.append("  'Authorization': 'Basic ' + btoa(");
                    js.append("'").append(escapeJavaScriptString(
                            api.getAuthConfig().getBasicUsername() != null ?
                                    api.getAuthConfig().getBasicUsername() : "")).append(":' + ");
                    js.append("'").append(escapeJavaScriptString(
                            api.getAuthConfig().getBasicPassword() != null ?
                                    api.getAuthConfig().getBasicPassword() : "")).append("'),\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    js.append("  '").append(escapeJavaScriptString(header.getKey()))
                            .append("': '").append(escapeJavaScriptString(header.getValue())).append("',\n");
                }
            }
        }

        js.append("};\n\n");

        js.append("/**\n");
        js.append(" * Call the ").append(api.getApiName()).append(" API\n");
        js.append(" * @param {Object} params - Request parameters\n");
        js.append(" * @returns {Promise<Object>} API response\n");
        js.append(" */\n");
        js.append("async function callApi(params = {}) {\n");

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            js.append("  const requestBody = params.body || ").append(api.getRequestConfig().getSample()).append(";\n");
            js.append("  \n");
        }

        js.append("  const options = {\n");
        js.append("    method: '").append(api.getHttpMethod()).append("',\n");
        js.append("    headers,\n");

        if (!"GET".equals(api.getHttpMethod())) {
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                js.append("    body: JSON.stringify(requestBody)\n");
            } else {
                js.append("    body: params.body ? JSON.stringify(params.body) : undefined\n");
            }
        }

        js.append("  };\n\n");

        js.append("  console.log('Making request to:', url);\n");
        js.append("  console.log('With headers:', headers);\n\n");

        js.append("  try {\n");
        js.append("    const response = await fetch(url, options);\n");
        js.append("    console.log('Status:', response.status);\n");
        js.append("    \n");
        js.append("    const data = await response.json();\n");
        js.append("    \n");
        js.append("    if (response.ok) {\n");
        js.append("      console.log('Success:', data);\n");
        js.append("      return data;\n");
        js.append("    } else {\n");
        js.append("      console.error('Error:', data);\n");
        js.append("      throw new Error(data.message || `HTTP error ${response.status}`);\n");
        js.append("    }\n");
        js.append("  } catch (error) {\n");
        js.append("    console.error('Network error:', error);\n");
        js.append("    throw error;\n");
        js.append("  }\n");
        js.append("}\n\n");

        js.append("// Example usage with actual values:\n");
        js.append("/*\n");
        js.append("callApi({\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getExample() != null) {
                    js.append("  ").append(param.getKey()).append(": '")
                            .append(escapeJavaScriptString(param.getExample())).append("',\n");
                }
            }
        }

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            js.append("  body: ").append(api.getRequestConfig().getSample()).append("\n");
        }

        js.append("})\n");
        js.append("  .then(data => console.log('Success:', data))\n");
        js.append("  .catch(error => console.error('Error:', error));\n");
        js.append("*/\n");

        return js.toString();
    }

    private String escapeJavaScriptString(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }

    private String generateFunctionalPythonCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder py = new StringBuilder();
        py.append("# Auto-generated functional Python code for ").append(api.getApiName()).append("\n\n");
        py.append("import requests\n");
        py.append("import json\n");
        py.append("from urllib.parse import urlencode\n\n");

        String serverUrl = "{{baseUrl}}/gen/" + api.getId();
        String baseUrl = serverUrl + fullUrl;

        py.append("base_url = \"").append(baseUrl).append("\"\n\n");

        if (api.getParameters() != null) {
            List<String> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    queryParams.add("    '" + param.getKey() + "': '" + param.getExample() + "'");
                }
            }
            if (!queryParams.isEmpty()) {
                py.append("# Query parameters with example values\n");
                py.append("query_params = {\n");
                py.append(String.join(",\n", queryParams));
                py.append("\n}\n");
                py.append("url = base_url + '?' + urlencode(query_params)\n");
            } else {
                py.append("url = base_url\n");
            }
        } else {
            py.append("url = base_url\n");
        }
        py.append("\n");

        py.append("headers = {\n");
        py.append("    'Content-Type': 'application/json',\n");
        py.append("    'Accept': 'application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    py.append("    '").append(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key").append("': '");
                    py.append(api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "").append("',\n");
                    break;
                case "BEARER":
                case "JWT":
                    py.append("    'Authorization': 'Bearer ");
                    py.append(api.getAuthConfig().getJwtSecret() != null ?
                            api.getAuthConfig().getJwtSecret() : "").append("',\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    py.append("    '").append(header.getKey()).append("': '");
                    py.append(header.getValue()).append("',\n");
                }
            }
        }

        py.append("}\n\n");

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            py.append("# Request body\n");
            py.append("data = ").append(api.getRequestConfig().getSample()).append("\n\n");
        }

        py.append("print(f\"Making ").append(api.getHttpMethod()).append(" request to: {url}\")\n");
        py.append("response = requests.").append(api.getHttpMethod() != null ? api.getHttpMethod().toLowerCase() : "get");
        py.append("(url, headers=headers");

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            py.append(", json=data");
        }

        py.append(")\n\n");
        py.append("print(f\"Status Code: {response.status_code}\")\n");
        py.append("print(\"Response Headers:\", response.headers)\n");
        py.append("print(\"Response Body:\")\n");
        py.append("try:\n");
        py.append("    print(json.dumps(response.json(), indent=2))\n");
        py.append("except:\n");
        py.append("    print(response.text)\n");

        return py.toString();
    }

    private String generateFunctionalJavaCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder java = new StringBuilder();
        java.append("// Auto-generated functional Java code for ").append(escapeJavaString(api.getApiName())).append("\n\n");
        java.append("package com.example.api;\n\n");
        java.append("import java.net.URI;\n");
        java.append("import java.net.http.HttpClient;\n");
        java.append("import java.net.http.HttpRequest;\n");
        java.append("import java.net.http.HttpResponse;\n");
        java.append("import java.time.Duration;\n");
        java.append("import java.util.HashMap;\n");
        java.append("import java.util.Map;\n");
        java.append("import java.util.ArrayList;\n");
        java.append("import java.util.List;\n");
        java.append("import java.net.URLEncoder;\n");
        java.append("import java.nio.charset.StandardCharsets;\n");
        java.append("import java.util.Base64;\n");
        java.append("import com.fasterxml.jackson.databind.ObjectMapper;\n");
        java.append("import com.fasterxml.jackson.core.type.TypeReference;\n\n");

        String serverUrl = "{{baseUrl}}/gen/" + api.getId();
        String fullEndpoint = serverUrl + fullUrl;

        java.append("public class ").append(api.getApiCode()).append("Client {\n\n");
        java.append("    private static final String BASE_URL = \"").append(escapeJavaString(fullEndpoint)).append("\";\n");
        java.append("    private final HttpClient httpClient;\n");
        java.append("    private final ObjectMapper objectMapper;\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            if (api.getAuthConfig().getApiKeyValue() != null) {
                java.append("    private final String API_KEY = \"").append(escapeJavaString(api.getAuthConfig().getApiKeyValue())).append("\";\n");
            }
            if (api.getAuthConfig().getJwtSecret() != null) {
                java.append("    private final String JWT_TOKEN = \"").append(escapeJavaString(api.getAuthConfig().getJwtSecret())).append("\";\n");
            }
            if (api.getAuthConfig().getBasicUsername() != null) {
                java.append("    private final String USERNAME = \"").append(escapeJavaString(api.getAuthConfig().getBasicUsername())).append("\";\n");
            }
            if (api.getAuthConfig().getBasicPassword() != null) {
                java.append("    private final String PASSWORD = \"").append(escapeJavaString(api.getAuthConfig().getBasicPassword())).append("\";\n");
            }
        }

        java.append("\n");
        java.append("    public ").append(api.getApiCode()).append("Client() {\n");
        java.append("        this.httpClient = HttpClient.newBuilder()\n");
        java.append("                .connectTimeout(Duration.ofSeconds(30))\n");
        java.append("                .build();\n");
        java.append("        this.objectMapper = new ObjectMapper();\n");
        java.append("    }\n\n");

        java.append("    public static class RequestParams {\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                String type = mapToJavaType(param.getApiType());
                java.append("        private ").append(type).append(" ").append(param.getKey()).append(";\n");
            }
        }

        java.append("        private Map<String, Object> body;\n\n");

        java.append("        public RequestParams() {\n");
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getExample() != null && !param.getExample().isEmpty()) {
                    String value = param.getExample();
                    if ("String".equals(mapToJavaType(param.getApiType()))) {
                        value = "\"" + escapeJavaString(value) + "\"";
                    } else {
                        value = value;
                    }
                    java.append("            this.").append(param.getKey()).append(" = ").append(value).append(";\n");
                }
            }
        }
        java.append("        }\n\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                String type = mapToJavaType(param.getApiType());
                String key = param.getKey();
                String capitalized = key.substring(0, 1).toUpperCase() + key.substring(1);

                java.append("        public ").append(type).append(" get").append(capitalized).append("() {\n");
                java.append("            return ").append(key).append(";\n");
                java.append("        }\n\n");

                java.append("        public void set").append(capitalized).append("(").append(type).append(" ").append(key).append(") {\n");
                java.append("            this.").append(key).append(" = ").append(key).append(";\n");
                java.append("        }\n\n");
            }
        }

        java.append("        public Map<String, Object> getBody() {\n");
        java.append("            return body;\n");
        java.append("        }\n\n");
        java.append("        public void setBody(Map<String, Object> body) {\n");
        java.append("            this.body = body;\n");
        java.append("        }\n");
        java.append("    }\n\n");

        java.append("    public static class ApiResponse {\n");
        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    String type = mapToJavaType(mapping.getApiType());
                    java.append("        private ").append(type).append(" ").append(mapping.getApiField()).append(";\n");
                }
            }
            java.append("\n");
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    String type = mapToJavaType(mapping.getApiType());
                    String field = mapping.getApiField();
                    String capitalized = field.substring(0, 1).toUpperCase() + field.substring(1);

                    java.append("        public ").append(type).append(" get").append(capitalized).append("() {\n");
                    java.append("            return ").append(field).append(";\n");
                    java.append("        }\n\n");

                    java.append("        public void set").append(capitalized).append("(").append(type).append(" ").append(field).append(") {\n");
                    java.append("            this.").append(field).append(" = ").append(field).append(";\n");
                    java.append("        }\n\n");
                }
            }
        }
        java.append("    }\n\n");

        java.append("    public ApiResponse callApi(RequestParams params) throws Exception, IOException, InterruptedException {\n");
        java.append("        // Build URL with query parameters\n");
        java.append("        StringBuilder urlBuilder = new StringBuilder(BASE_URL);\n");
        java.append("        \n");
        java.append("        // Add query parameters\n");
        java.append("        List<String> queryParams = new ArrayList<>();\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    java.append("        if (params.get").append(capitalize(param.getKey())).append("() != null) {\n");
                    java.append("            queryParams.add(\"").append(param.getKey()).append("=\" + \n");
                    java.append("                URLEncoder.encode(params.get").append(capitalize(param.getKey())).append("().toString(), \n");
                    java.append("                StandardCharsets.UTF_8.name()));\n");
                    java.append("        }\n");
                }
            }
        }

        java.append("        \n");
        java.append("        if (!queryParams.isEmpty()) {\n");
        java.append("            urlBuilder.append(\"?\").append(String.join(\"&\", queryParams));\n");
        java.append("        }\n");
        java.append("        \n");
        java.append("        // Build request\n");
        java.append("        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()\n");
        java.append("                .uri(URI.create(urlBuilder.toString()))\n");
        java.append("                .timeout(Duration.ofSeconds(30))\n");
        java.append("                .header(\"Content-Type\", \"application/json\")\n");
        java.append("                .header(\"Accept\", \"application/json\");\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    java.append("        if (API_KEY != null && !API_KEY.isEmpty()) {\n");
                    java.append("            requestBuilder.header(\"")
                            .append(escapeJavaString(api.getAuthConfig().getApiKeyHeader() != null ?
                                    api.getAuthConfig().getApiKeyHeader() : "X-API-Key"))
                            .append("\", API_KEY);\n");
                    java.append("        }\n");
                    break;
                case "BEARER":
                case "JWT":
                    java.append("        if (JWT_TOKEN != null && !JWT_TOKEN.isEmpty()) {\n");
                    java.append("            requestBuilder.header(\"Authorization\", \"Bearer \" + JWT_TOKEN);\n");
                    java.append("        }\n");
                    break;
                case "BASIC":
                    java.append("        if (USERNAME != null && PASSWORD != null && \n");
                    java.append("            !USERNAME.isEmpty() && !PASSWORD.isEmpty()) {\n");
                    java.append("            String auth = USERNAME + \":\" + PASSWORD;\n");
                    java.append("            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));\n");
                    java.append("            requestBuilder.header(\"Authorization\", \"Basic \" + encodedAuth);\n");
                    java.append("        }\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    java.append("        requestBuilder.header(\"").append(escapeJavaString(header.getKey()))
                            .append("\", \"").append(escapeJavaString(header.getValue())).append("\");\n");
                }
            }
        }

        if (!"GET".equals(api.getHttpMethod())) {
            java.append("        \n");
            java.append("        String requestBody = params.getBody() != null ? \n");
            java.append("                objectMapper.writeValueAsString(params.getBody()) : \"{}\";\n");
            java.append("        requestBuilder.method(\"").append(api.getHttpMethod()).append("\", \n");
            java.append("                HttpRequest.BodyPublishers.ofString(requestBody));\n");
        } else {
            java.append("        requestBuilder.GET();\n");
        }

        java.append("        \n");
        java.append("        HttpRequest request = requestBuilder.build();\n");
        java.append("        \n");
        java.append("        // Send request\n");
        java.append("        HttpResponse<String> response = httpClient.send(request,\n");
        java.append("                HttpResponse.BodyHandlers.ofString());\n");
        java.append("        \n");
        java.append("        // Parse response\n");
        java.append("        if (response.statusCode() >= 200 && response.statusCode() < 300) {\n");
        java.append("            return objectMapper.readValue(response.body(), ApiResponse.class);\n");
        java.append("        } else {\n");
        java.append("            throw new RuntimeException(\"API call failed with status: \" \n");
        java.append("                    + response.statusCode() + \" - \" + response.body());\n");
        java.append("        }\n");
        java.append("    }\n\n");

        java.append("    public static void main(String[] args) {\n");
        java.append("        try {\n");
        java.append("            ").append(api.getApiCode()).append("Client client = new ").append(api.getApiCode()).append("Client();\n");
        java.append("            RequestParams params = new RequestParams();\n");
        java.append("            \n");
        java.append("            ApiResponse response = client.callApi(params);\n");
        java.append("            System.out.println(\"Success! Response: \");\n");
        java.append("            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter()\n");
        java.append("                    .writeValueAsString(response));\n");
        java.append("        } catch (Exception e) {\n");
        java.append("            System.err.println(\"Error calling API: \" + e.getMessage());\n");
        java.append("            e.printStackTrace();\n");
        java.append("        }\n");
        java.append("    }\n");
        java.append("}\n");

        return java.toString();
    }

    private String escapeJavaString(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }

    private String generateFunctionalCSharpCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder cs = new StringBuilder();
        cs.append("// Auto-generated functional C# code for ").append(api.getApiName()).append("\n\n");
        cs.append("using System;\n");
        cs.append("using System.Net.Http;\n");
        cs.append("using System.Net.Http.Headers;\n");
        cs.append("using System.Threading.Tasks;\n");
        cs.append("using System.Collections.Generic;\n");
        cs.append("using System.Text.Json;\n");
        cs.append("using System.Text.Json.Serialization;\n");
        cs.append("using System.Linq;\n\n");

        cs.append("namespace ApiClient\n");
        cs.append("{\n");
        cs.append("    public class ").append(api.getApiCode()).append("Client\n");
        cs.append("    {\n");
        cs.append("        private readonly HttpClient _httpClient;\n");
        cs.append("        private readonly JsonSerializerOptions _jsonOptions;\n");
        cs.append("        private readonly string _baseUrl = \"").append(fullUrl).append("\";\n\n");

        cs.append("        public ").append(api.getApiCode()).append("Client()\n");
        cs.append("        {\n");
        cs.append("            _httpClient = new HttpClient\n");
        cs.append("            {\n");
        cs.append("                Timeout = TimeSpan.FromSeconds(30)\n");
        cs.append("            };\n");
        cs.append("            _httpClient.DefaultRequestHeaders.Accept.Add(\n");
        cs.append("                new MediaTypeWithQualityHeaderValue(\"application/json\"));\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    cs.append("            var apiKey = Environment.GetEnvironmentVariable(\"API_KEY\");\n");
                    cs.append("            if (!string.IsNullOrEmpty(apiKey))\n");
                    cs.append("            {\n");
                    cs.append("                _httpClient.DefaultRequestHeaders.Add(\"")
                            .append(api.getAuthConfig().getApiKeyHeader() != null ?
                                    api.getAuthConfig().getApiKeyHeader() : "X-API-Key")
                            .append("\", apiKey);\n");
                    cs.append("            }\n");
                    break;
                case "BEARER":
                case "JWT":
                    cs.append("            var token = Environment.GetEnvironmentVariable(\"JWT_TOKEN\");\n");
                    cs.append("            if (!string.IsNullOrEmpty(token))\n");
                    cs.append("            {\n");
                    cs.append("                _httpClient.DefaultRequestHeaders.Authorization =\n");
                    cs.append("                    new AuthenticationHeaderValue(\"Bearer\", token);\n");
                    cs.append("            }\n");
                    break;
            }
        }

        cs.append("            _jsonOptions = new JsonSerializerOptions\n");
        cs.append("            {\n");
        cs.append("                PropertyNamingPolicy = JsonNamingPolicy.CamelCase,\n");
        cs.append("                WriteIndented = true\n");
        cs.append("            };\n");
        cs.append("        }\n\n");

        cs.append("        public class RequestParams\n");
        cs.append("        {\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                String type = mapToCSharpType(param.getApiType());
                cs.append("            [JsonPropertyName(\"").append(param.getKey()).append("\")]\n");
                cs.append("            public ").append(type).append(" ").append(capitalize(param.getKey())).append(" { get; set; }\n");
            }
        }

        cs.append("            public Dictionary<string, object> Body { get; set; }\n");
        cs.append("        }\n\n");

        cs.append("        public class ApiResponse\n");
        cs.append("        {\n");

        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    String type = mapToCSharpType(mapping.getApiType());
                    cs.append("            [JsonPropertyName(\"").append(mapping.getApiField()).append("\")]\n");
                    cs.append("            public ").append(type).append(" ").append(capitalize(mapping.getApiField())).append(" { get; set; }\n");
                }
            }
        }

        cs.append("        }\n\n");

        cs.append("        public async Task<ApiResponse> CallApiAsync(RequestParams params)\n");
        cs.append("        {\n");
        cs.append("            // Build URL with query parameters\n");
        cs.append("            var queryParams = new List<string>();\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    cs.append("            if (params.").append(capitalize(param.getKey())).append(" != null)\n");
                    cs.append("            {\n");
                    cs.append("                queryParams.Add($\"").append(param.getKey()).append("={params.").append(capitalize(param.getKey())).append("}\");\n");
                    cs.append("            }\n");
                }
            }
        }

        cs.append("\n");
        cs.append("            var url = _baseUrl;\n");
        cs.append("            if (queryParams.Any())\n");
        cs.append("            {\n");
        cs.append("                url += \"?\" + string.Join(\"&\", queryParams);\n");
        cs.append("            }\n\n");

        cs.append("            // Create request\n");
        cs.append("            var request = new HttpRequestMessage\n");
        cs.append("            {\n");
        cs.append("                Method = HttpMethod.").append(capitalize(api.getHttpMethod() != null ? api.getHttpMethod().toLowerCase() : "get")).append(",\n");
        cs.append("                RequestUri = new Uri(url)\n");
        cs.append("            };\n\n");

        if (!"GET".equals(api.getHttpMethod())) {
            cs.append("            if (params.Body != null)\n");
            cs.append("            {\n");
            cs.append("                var json = JsonSerializer.Serialize(params.Body, _jsonOptions);\n");
            cs.append("                request.Content = new StringContent(json, System.Text.Encoding.UTF8, \"application/json\");\n");
            cs.append("            }\n\n");
        }

        cs.append("            // Send request\n");
        cs.append("            var response = await _httpClient.SendAsync(request);\n");
        cs.append("            var content = await response.Content.ReadAsStringAsync();\n\n");

        cs.append("            if (response.IsSuccessStatusCode)\n");
        cs.append("            {\n");
        cs.append("                return JsonSerializer.Deserialize<ApiResponse>(content, _jsonOptions);\n");
        cs.append("            }\n");
        cs.append("            else\n");
        cs.append("            {\n");
        cs.append("                throw new Exception($\"API call failed: {response.StatusCode} - {content}\");\n");
        cs.append("            }\n");
        cs.append("        }\n\n");

        cs.append("        public static async Task Main(string[] args)\n");
        cs.append("        {\n");
        cs.append("            try\n");
        cs.append("            {\n");
        cs.append("                var client = new ").append(api.getApiCode()).append("Client();\n");
        cs.append("                var requestParams = new RequestParams\n");
        cs.append("                {\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getExample() != null) {
                    String type = mapToCSharpType(param.getApiType());
                    String value = param.getExample();
                    if ("string".equals(type.toLowerCase())) {
                        value = "\"" + value + "\"";
                    }
                    cs.append("                    ").append(capitalize(param.getKey())).append(" = ").append(value).append(",\n");
                }
            }
        }

        cs.append("                };\n\n");
        cs.append("                var response = await client.CallApiAsync(requestParams);\n");
        cs.append("                Console.WriteLine(JsonSerializer.Serialize(response, new JsonSerializerOptions { WriteIndented = true }));\n");
        cs.append("            }\n");
        cs.append("            catch (Exception ex)\n");
        cs.append("            {\n");
        cs.append("                Console.WriteLine($\"Error: {ex.Message}\");\n");
        cs.append("            }\n");
        cs.append("        }\n");
        cs.append("    }\n");
        cs.append("}\n");

        return cs.toString();
    }

    private String generateFunctionalPhpCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder php = new StringBuilder();
        php.append("<?php\n\n");
        php.append("/**\n");
        php.append(" * Auto-generated functional PHP code for ").append(api.getApiName()).append("\n");
        php.append(" */\n\n");

        php.append("class ").append(api.getApiCode()).append("Client {\n");
        php.append("    private $baseUrl = '").append(fullUrl).append("';\n");
        php.append("    private $timeout = 30;\n");
        php.append("    private $headers = [];\n\n");

        php.append("    public function __construct() {\n");
        php.append("        $this->headers = [\n");
        php.append("            'Content-Type: application/json',\n");
        php.append("            'Accept: application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    php.append("            '")
                            .append(api.getAuthConfig().getApiKeyHeader() != null ?
                                    api.getAuthConfig().getApiKeyHeader() : "X-API-Key")
                            .append(": ' . getenv('API_KEY'),\n");
                    break;
                case "BEARER":
                case "JWT":
                    php.append("            'Authorization: Bearer ' . getenv('JWT_TOKEN'),\n");
                    break;
                case "BASIC":
                    php.append("            'Authorization: Basic ' . base64_encode(getenv('API_USERNAME') . ':' . getenv('API_PASSWORD')),\n");
                    break;
                case "ORACLE_ROLES":
                    php.append("            'X-Oracle-Session: ' . getenv('ORACLE_SESSION_ID'),\n");
                    break;
            }
        }

        php.append("        ];\n");
        php.append("    }\n\n");

        php.append("    /**\n");
        php.append("     * Call the API\n");
        php.append("     * @param array $params Request parameters\n");
        php.append("     * @return array API response\n");
        php.append("     * @throws Exception\n");
        php.append("     */\n");
        php.append("    public function callApi($params = []) {\n");
        php.append("        // Build query string\n");
        php.append("        $queryParams = [];\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    php.append("        if (isset($params['").append(param.getKey()).append("'])) {\n");
                    php.append("            $queryParams['").append(param.getKey()).append("'] = $params['").append(param.getKey()).append("'];\n");
                    php.append("        }\n");
                }
            }
        }

        php.append("\n");
        php.append("        $url = $this->baseUrl;\n");
        php.append("        if (!empty($queryParams)) {\n");
        php.append("            $url .= '?' . http_build_query($queryParams);\n");
        php.append("        }\n\n");

        php.append("        // Initialize cURL\n");
        php.append("        $ch = curl_init($url);\n");
        php.append("        \n");
        php.append("        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);\n");
        php.append("        curl_setopt($ch, CURLOPT_TIMEOUT, $this->timeout);\n");
        php.append("        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, '")
                .append(api.getHttpMethod() != null ? api.getHttpMethod() : "GET")
                .append("');\n");
        php.append("        curl_setopt($ch, CURLOPT_HTTPHEADER, $this->headers);\n");
        php.append("        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);\n\n");

        if (!"GET".equals(api.getHttpMethod())) {
            php.append("        if (isset($params['body'])) {\n");
            php.append("            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($params['body']));\n");
            php.append("        }\n\n");
        }

        php.append("        // Execute request\n");
        php.append("        $response = curl_exec($ch);\n");
        php.append("        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);\n");
        php.append("        $error = curl_error($ch);\n");
        php.append("        curl_close($ch);\n\n");

        php.append("        if ($error) {\n");
        php.append("            throw new Exception('cURL Error: ' . $error);\n");
        php.append("        }\n\n");

        php.append("        $data = json_decode($response, true);\n\n");

        php.append("        if ($httpCode >= 200 && $httpCode < 300) {\n");
        php.append("            return $data;\n");
        php.append("        } else {\n");
        php.append("            $errorMsg = isset($data['message']) ? $data['message'] : 'Unknown error';\n");
        php.append("            throw new Exception('API Error (' . $httpCode . '): ' . $errorMsg);\n");
        php.append("        }\n");
        php.append("    }\n");
        php.append("}\n\n");

        php.append("// Example usage:\n");
        php.append("/*\n");
        php.append("try {\n");
        php.append("    $client = new ").append(api.getApiCode()).append("Client();\n");
        php.append("    $params = [\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getExample() != null) {
                    php.append("        '").append(param.getKey()).append("' => '").append(param.getExample()).append("',\n");
                }
            }
        }

        php.append("    ];\n");
        php.append("    \n");
        php.append("    $result = $client->callApi($params);\n");
        php.append("    print_r($result);\n");
        php.append("} catch (Exception $e) {\n");
        php.append("    echo 'Error: ' . $e->getMessage();\n");
        php.append("}\n");
        php.append("*/\n");

        return php.toString();
    }

    private String generateFunctionalRubyCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder rb = new StringBuilder();
        rb.append("# Auto-generated functional Ruby code for ").append(api.getApiName()).append("\n\n");
        rb.append("require 'uri'\n");
        rb.append("require 'net/http'\n");
        rb.append("require 'json'\n");
        rb.append("require 'dotenv/load'\n\n");

        rb.append("class ").append(api.getApiCode()).append("Client\n");
        rb.append("  def initialize\n");
        rb.append("    @base_url = '").append(fullUrl).append("'\n");
        rb.append("    @timeout = 30\n");
        rb.append("  end\n\n");

        rb.append("  def call_api(params = {})\n");
        rb.append("    # Build URL with query parameters\n");
        rb.append("    uri = URI(@base_url)\n");
        rb.append("    \n");
        rb.append("    # Add query parameters\n");
        rb.append("    query_params = {}\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    rb.append("    query_params['").append(param.getKey()).append("'] = params[:").append(param.getKey()).append("] if params[:").append(param.getKey()).append("]\n");
                }
            }
        }

        rb.append("    \n");
        rb.append("    unless query_params.empty?\n");
        rb.append("      uri.query = URI.encode_www_form(query_params)\n");
        rb.append("    end\n\n");

        rb.append("    # Build request\n");
        rb.append("    http = Net::HTTP.new(uri.host, uri.port)\n");
        rb.append("    http.use_ssl = uri.scheme == 'https'\n");
        rb.append("    http.read_timeout = @timeout\n\n");

        String methodName = api.getHttpMethod() != null ?
                capitalize(api.getHttpMethod().toLowerCase()) : "Get";
        rb.append("    request = Net::HTTP::").append(methodName).append(".new(uri)\n");
        rb.append("    \n");
        rb.append("    # Set headers\n");
        rb.append("    request['Content-Type'] = 'application/json'\n");
        rb.append("    request['Accept'] = 'application/json'\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    rb.append("    request['")
                            .append(api.getAuthConfig().getApiKeyHeader() != null ?
                                    api.getAuthConfig().getApiKeyHeader() : "X-API-Key")
                            .append("'] = ENV['API_KEY']\n");
                    break;
                case "BEARER":
                case "JWT":
                    rb.append("    request['Authorization'] = \"Bearer \\#{ENV['JWT_TOKEN']}\"\n");
                    break;
                case "BASIC":
                    rb.append("    request.basic_auth(ENV['API_USERNAME'], ENV['API_PASSWORD'])\n");
                    break;
                case "ORACLE_ROLES":
                    rb.append("    request['X-Oracle-Session'] = ENV['ORACLE_SESSION_ID']\n");
                    break;
            }
        }

        rb.append("\n");

        if (!"GET".equals(api.getHttpMethod())) {
            rb.append("    if params[:body]\n");
            rb.append("      request.body = params[:body].to_json\n");
            rb.append("    end\n\n");
        }

        rb.append("    # Execute request\n");
        rb.append("    response = http.request(request)\n\n");

        rb.append("    # Parse response\n");
        rb.append("    data = JSON.parse(response.body) if response.body && !response.body.empty?\n\n");

        rb.append("    if response.is_a?(Net::HTTPSuccess)\n");
        rb.append("      return data\n");
        rb.append("    else\n");
        rb.append("      error_msg = data && data['message'] ? data['message'] : 'Unknown error'\n");
        rb.append("      raise \"API Error (\\#{response.code}): \\#{error_msg}\"\n");
        rb.append("    end\n");
        rb.append("  end\n");
        rb.append("end\n\n");

        rb.append("# Example usage:\n");
        rb.append("# client = ").append(api.getApiCode()).append("Client.new\n");
        rb.append("# result = client.call_api({\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getExample() != null) {
                    rb.append("#   ").append(param.getKey()).append(": '").append(param.getExample()).append("',\n");
                }
            }
        }

        rb.append("# })\n");
        rb.append("# puts JSON.pretty_generate(result)\n");

        return rb.toString();
    }

    private String generateFunctionalGoCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder go = new StringBuilder();
        go.append("// Auto-generated functional Go code for ").append(api.getApiName()).append("\n\n");
        go.append("package main\n\n");
        go.append("import (\n");
        go.append("    \"bytes\"\n");
        go.append("    \"encoding/json\"\n");
        go.append("    \"fmt\"\n");
        go.append("    \"io\"\n");
        go.append("    \"net/http\"\n");
        go.append("    \"os\"\n");
        go.append("    \"time\"\n");
        go.append(")\n\n");

        go.append("type ").append(api.getApiCode()).append("RequestParams struct {\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                String type = mapToGoType(param.getApiType());
                go.append("    ").append(capitalize(param.getKey())).append(" ").append(type).append(" `json:\"").append(param.getKey()).append("\"`\n");
            }
        }

        go.append("    Body map[string]interface{} `json:\"body,omitempty\"`\n");
        go.append("}\n\n");

        go.append("type ").append(api.getApiCode()).append("Response struct {\n");

        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    String type = mapToGoType(mapping.getApiType());
                    go.append("    ").append(capitalize(mapping.getApiField())).append(" ").append(type).append(" `json:\"").append(mapping.getApiField()).append("\"`\n");
                }
            }
        }

        go.append("}\n\n");

        go.append("type ").append(api.getApiCode()).append("Client struct {\n");
        go.append("    baseUrl    string\n");
        go.append("    httpClient *http.Client\n");
        go.append("}\n\n");

        go.append("func New").append(api.getApiCode()).append("Client() *").append(api.getApiCode()).append("Client {\n");
        go.append("    return &").append(api.getApiCode()).append("Client{\n");
        go.append("        baseUrl: \"").append(fullUrl).append("\",\n");
        go.append("        httpClient: &http.Client{\n");
        go.append("            Timeout: time.Second * 30,\n");
        go.append("        },\n");
        go.append("    }\n");
        go.append("}\n\n");

        go.append("func (c *").append(api.getApiCode()).append("Client) CallAPI(params *").append(api.getApiCode()).append("RequestParams) (*").append(api.getApiCode()).append("Response, error) {\n");
        go.append("    // Build URL with query parameters\n");
        go.append("    req, err := http.NewRequest(\"")
                .append(api.getHttpMethod() != null ? api.getHttpMethod() : "GET")
                .append("\", c.baseUrl, nil)\n");
        go.append("    if err != nil {\n");
        go.append("        return nil, fmt.Errorf(\"failed to create request: %w\", err)\n");
        go.append("    }\n\n");

        go.append("    // Add query parameters\n");
        go.append("    q := req.URL.Query()\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    go.append("    if params.").append(capitalize(param.getKey())).append(" != \"\" {\n");
                    go.append("        q.Add(\"").append(param.getKey()).append("\", params.").append(capitalize(param.getKey())).append(")\n");
                    go.append("    }\n");
                }
            }
        }

        go.append("    req.URL.RawQuery = q.Encode()\n\n");

        go.append("    // Set headers\n");
        go.append("    req.Header.Set(\"Content-Type\", \"application/json\")\n");
        go.append("    req.Header.Set(\"Accept\", \"application/json\")\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    go.append("    if apiKey := os.Getenv(\"API_KEY\"); apiKey != \"\" {\n");
                    go.append("        req.Header.Set(\"")
                            .append(api.getAuthConfig().getApiKeyHeader() != null ?
                                    api.getAuthConfig().getApiKeyHeader() : "X-API-Key")
                            .append("\", apiKey)\n");
                    go.append("    }\n");
                    break;
                case "BEARER":
                case "JWT":
                    go.append("    if token := os.Getenv(\"JWT_TOKEN\"); token != \"\" {\n");
                    go.append("        req.Header.Set(\"Authorization\", \"Bearer \"+token)\n");
                    go.append("    }\n");
                    break;
            }
        }

        go.append("\n");

        if (!"GET".equals(api.getHttpMethod())) {
            go.append("    // Add request body\n");
            go.append("    if params.Body != nil {\n");
            go.append("        bodyBytes, err := json.Marshal(params.Body)\n");
            go.append("        if err != nil {\n");
            go.append("            return nil, fmt.Errorf(\"failed to marshal body: %w\", err)\n");
            go.append("        }\n");
            go.append("        req.Body = io.NopCloser(bytes.NewReader(bodyBytes))\n");
            go.append("        req.ContentLength = int64(len(bodyBytes))\n");
            go.append("    }\n\n");
        }

        go.append("    // Execute request\n");
        go.append("    resp, err := c.httpClient.Do(req)\n");
        go.append("    if err != nil {\n");
        go.append("        return nil, fmt.Errorf(\"failed to execute request: %w\", err)\n");
        go.append("    }\n");
        go.append("    defer resp.Body.Close()\n\n");

        go.append("    // Read response body\n");
        go.append("    body, err := io.ReadAll(resp.Body)\n");
        go.append("    if err != nil {\n");
        go.append("        return nil, fmt.Errorf(\"failed to read response body: %w\", err)\n");
        go.append("    }\n\n");

        go.append("    // Check status code\n");
        go.append("    if resp.StatusCode < 200 || resp.StatusCode >= 300 {\n");
        go.append("        return nil, fmt.Errorf(\"API returned error status: %d - %s\", resp.StatusCode, string(body))\n");
        go.append("    }\n\n");

        go.append("    // Parse response\n");
        go.append("    var response ").append(api.getApiCode()).append("Response\n");
        go.append("    if err := json.Unmarshal(body, &response); err != nil {\n");
        go.append("        return nil, fmt.Errorf(\"failed to parse response: %w\", err)\n");
        go.append("    }\n\n");

        go.append("    return &response, nil\n");
        go.append("}\n\n");

        go.append("func main() {\n");
        go.append("    client := New").append(api.getApiCode()).append("Client()\n");
        go.append("    \n");
        go.append("    params := &").append(api.getApiCode()).append("RequestParams{\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getExample() != null) {
                    String type = mapToGoType(param.getApiType());
                    String value = param.getExample();
                    if ("string".equals(type)) {
                        value = "\"" + value + "\"";
                    }
                    go.append("        ").append(capitalize(param.getKey())).append(": ").append(value).append(",\n");
                }
            }
        }

        go.append("    }\n\n");
        go.append("    response, err := client.CallAPI(params)\n");
        go.append("    if err != nil {\n");
        go.append("        fmt.Printf(\"Error: %v\\n\", err)\n");
        go.append("        return\n");
        go.append("    }\n\n");
        go.append("    jsonResponse, _ := json.MarshalIndent(response, \"\", \"  \")\n");
        go.append("    fmt.Println(string(jsonResponse))\n");
        go.append("}\n");

        return go.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String mapToJavaType(String apiType) {
        if (apiType == null) return "String";
        switch (apiType.toLowerCase()) {
            case "integer":
                return "Integer";
            case "number":
                return "Double";
            case "boolean":
                return "Boolean";
            case "array":
                return "List<Object>";
            case "object":
                return "Map<String, Object>";
            default:
                return "String";
        }
    }

    private String mapToCSharpType(String apiType) {
        if (apiType == null) return "string";
        switch (apiType.toLowerCase()) {
            case "integer":
                return "int?";
            case "number":
                return "double?";
            case "boolean":
                return "bool?";
            case "array":
                return "List<object>";
            case "object":
                return "Dictionary<string, object>";
            default:
                return "string";
        }
    }

    private String mapToGoType(String apiType) {
        if (apiType == null) return "string";
        switch (apiType.toLowerCase()) {
            case "integer":
                return "int64";
            case "number":
                return "float64";
            case "boolean":
                return "bool";
            case "array":
                return "[]interface{}";
            case "object":
                return "map[string]interface{}";
            default:
                return "string";
        }
    }

    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }

    private ApiSchemaConfigEntity mapToSchemaConfigEntity(ApiSchemaConfigDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiSchemaConfigEntity.builder()
                .generatedApi(api)
                .schemaName(dto.getSchemaName())
                .objectType(dto.getObjectType())
                .objectName(dto.getObjectName())
                .operation(dto.getOperation())
                .primaryKeyColumn(dto.getPrimaryKeyColumn())
                .sequenceName(dto.getSequenceName())
                .enablePagination(dto.getEnablePagination())
                .pageSize(dto.getPageSize())
                .enableSorting(dto.getEnableSorting())
                .defaultSortColumn(dto.getDefaultSortColumn())
                .defaultSortDirection(dto.getDefaultSortDirection())
                .isSynonym(dto.getIsSynonym())
                .targetType(dto.getTargetType())
                .targetName(dto.getTargetName())
                .targetOwner(dto.getTargetOwner())
                .build();
    }

    private ApiAuthConfigEntity mapToAuthConfigEntity(ApiAuthConfigDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiAuthConfigEntity.builder()
                .generatedApi(api)
                .authType(dto.getAuthType())
                .apiKeyHeader(dto.getApiKeyHeader())
                .apiKeyValue(dto.getApiKeyValue())
                .apiSecretHeader(dto.getApiSecretHeader())
                .apiSecretValue(dto.getApiSecretValue())
                .jwtToken(dto.getJwtToken())
                .jwtIssuer(dto.getJwtIssuer())
                .basicUsername(dto.getBasicUsername())
                .basicPassword(dto.getBasicPassword())
                .ipWhitelist(dto.getIpWhitelist())
                .rateLimitRequests(dto.getRateLimitRequests())
                .rateLimitPeriod(dto.getRateLimitPeriod())
                .enableRateLimiting(dto.getEnableRateLimiting())
                .corsOrigins(dto.getCorsOrigins().toString())
                .auditLevel(dto.getAuditLevel())
                .build();
    }

    private ApiRequestConfigEntity mapToRequestConfigEntity(ApiRequestConfigDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiRequestConfigEntity.builder()
                .generatedApi(api)
                .bodyType(dto.getBodyType())
                .sample(dto.getSample())
                .maxSize(dto.getMaxSize())
                .validateSchema(dto.getValidateSchema())
                .allowedMediaTypes(String.valueOf(dto.getAllowedMediaTypes()))
                .requiredFields(dto.getRequiredFields())
                .build();
    }

    private ApiResponseConfigEntity mapToResponseConfigEntity(ApiResponseConfigDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiResponseConfigEntity.builder()
                .generatedApi(api)
                .successSchema(dto.getSuccessSchema())
                .errorSchema(dto.getErrorSchema())
                .includeMetadata(dto.getIncludeMetadata())
                .metadataFields(dto.getMetadataFields())
                .contentType(dto.getContentType())
                .compression(dto.getCompression())
                .build();
    }

    private ApiSettingsEntity mapToSettingsEntity(ApiSettingsDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiSettingsEntity.builder()
                .generatedApi(api)
                .timeout(dto.getTimeout())
                .maxRecords(dto.getMaxRecords())
                .enableLogging(dto.getEnableLogging())
                .logLevel(dto.getLogLevel())
                .enableCaching(dto.getEnableCaching())
                .cacheTtl(dto.getCacheTtl())
                .generateSwagger(dto.getGenerateSwagger())
                .generatePostman(dto.getGeneratePostman())
                .generateClientSDK(dto.getGenerateClientSDK())
                .enableMonitoring(dto.getEnableMonitoring())
                .enableAlerts(dto.getEnableAlerts())
                .alertEmail(dto.getAlertEmail())
                .enableTracing(dto.getEnableTracing())
                .corsEnabled(dto.getCorsEnabled())
                .build();
    }

    private ApiParameterEntity mapToParameterEntity(ApiParameterDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiParameterEntity.builder()
                .id(UUID.randomUUID().toString())
                .generatedApi(api)
                .key(dto.getKey())
                .dbColumn(dto.getDbColumn())
                .dbParameter(dto.getDbParameter())
                .oracleType(dto.getOracleType())
                .apiType(dto.getApiType())
                .parameterType(dto.getParameterType())
                .parameterLocation(dto.getParameterLocation())
                .required(dto.getRequired())
                .description(dto.getDescription())
                .example(dto.getExample())
                .validationPattern(dto.getValidationPattern())
                .defaultValue(dto.getDefaultValue())
                .inBody(dto.getInBody())
                .isPrimaryKey(dto.getIsPrimaryKey())
                .paramMode(dto.getParamMode())
                .position(dto.getPosition() != null ? dto.getPosition() : 0)
                .build();
    }

    private ApiResponseMappingEntity mapToResponseMappingEntity(ApiResponseMappingDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField(dto.getApiField())
                .dbColumn(dto.getDbColumn())
                .oracleType(dto.getOracleType())
                .apiType(dto.getApiType())
                .format(dto.getFormat())
                .nullable(dto.getNullable())
                .isPrimaryKey(dto.getIsPrimaryKey())
                .includeInResponse(dto.getIncludeInResponse())
                .inResponse(dto.getInResponse())
                .position(dto.getPosition() != null ? dto.getPosition() : 0)
                .build();
    }

    private ApiHeaderEntity mapToHeaderEntity(ApiHeaderDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiHeaderEntity.builder()
                .generatedApi(api)
                .key(dto.getKey())
                .value(dto.getValue())
                .required(dto.getRequired())
                .description(dto.getDescription())
                .isRequestHeader(dto.getIsRequestHeader() != null ? dto.getIsRequestHeader() : true)
                .isResponseHeader(dto.getIsResponseHeader() != null ? dto.getIsResponseHeader() : false)
                .build();
    }

    private List<ApiTestEntity> createTestEntities(ApiTestsDTO dto, GeneratedApiEntity api) {
        if (dto == null) return new ArrayList<>();

        List<ApiTestEntity> tests = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        if (dto.getUnitTests() != null && !dto.getUnitTests().isEmpty()) {
            try {
                Map<String, Object> unitTestData = new HashMap<>();

                if (dto.getUnitTests().trim().startsWith("{") || dto.getUnitTests().trim().startsWith("[")) {
                    Object parsed = objectMapper.readValue(dto.getUnitTests(), Object.class);
                    unitTestData.put("tests", parsed);
                } else {
                    unitTestData.put("tests", dto.getUnitTests());
                }

                unitTestData.put("type", "unit");
                unitTestData.put("assertions", dto.getAssertions());
                unitTestData.put("environment", dto.getTestEnvironment());
                unitTestData.put("iterations", dto.getTestIterations());
                unitTestData.put("users", dto.getTestUsers());

                tests.add(ApiTestEntity.builder()
                        .generatedApi(api)
                        .testName("Unit Tests")
                        .testType("UNIT")
                        .testData(unitTestData)
                        .status("PENDING")
                        .build());
            } catch (Exception e) {
                log.warn("Failed to parse unit tests JSON: {}", e.getMessage());
                Map<String, Object> fallbackData = new HashMap<>();
                fallbackData.put("tests", dto.getUnitTests());
                fallbackData.put("type", "unit");
                tests.add(ApiTestEntity.builder()
                        .generatedApi(api)
                        .testName("Unit Tests")
                        .testType("UNIT")
                        .testData(fallbackData)
                        .status("PENDING")
                        .build());
            }
        }

        if (dto.getIntegrationTests() != null && !dto.getIntegrationTests().isEmpty()) {
            try {
                Map<String, Object> integrationTestData = new HashMap<>();

                if (dto.getIntegrationTests().trim().startsWith("{") || dto.getIntegrationTests().trim().startsWith("[")) {
                    Object parsed = objectMapper.readValue(dto.getIntegrationTests(), Object.class);
                    integrationTestData.put("tests", parsed);
                } else {
                    integrationTestData.put("tests", dto.getIntegrationTests());
                }

                integrationTestData.put("type", "integration");
                integrationTestData.put("assertions", dto.getAssertions());
                integrationTestData.put("environment", dto.getTestEnvironment());
                integrationTestData.put("iterations", dto.getTestIterations());
                integrationTestData.put("users", dto.getTestUsers());
                integrationTestData.put("performanceThreshold", dto.getPerformanceThreshold());

                tests.add(ApiTestEntity.builder()
                        .generatedApi(api)
                        .testName("Integration Tests")
                        .testType("INTEGRATION")
                        .testData(integrationTestData)
                        .status("PENDING")
                        .build());
            } catch (Exception e) {
                log.warn("Failed to parse integration tests JSON: {}", e.getMessage());
                Map<String, Object> fallbackData = new HashMap<>();
                fallbackData.put("tests", dto.getIntegrationTests());
                fallbackData.put("type", "integration");
                tests.add(ApiTestEntity.builder()
                        .generatedApi(api)
                        .testName("Integration Tests")
                        .testType("INTEGRATION")
                        .testData(fallbackData)
                        .status("PENDING")
                        .build());
            }
        }

        if (dto.getTestData() != null && !dto.getTestData().isEmpty()) {
            try {
                Map<String, Object> testDataMap = new HashMap<>();

                if (dto.getTestData() != null) {
                    testDataMap.putAll(dto.getTestData());
                } else {
                    testDataMap.put("data", dto.getTestData());
                }

                testDataMap.put("type", "test-data");
                testDataMap.put("environment", dto.getTestEnvironment());
                testDataMap.put("iterations", dto.getTestIterations());

                tests.add(ApiTestEntity.builder()
                        .generatedApi(api)
                        .testName("Test Data")
                        .testType("DATA")
                        .testData(testDataMap)
                        .status("PENDING")
                        .build());
            } catch (Exception e) {
                log.warn("Failed to parse test data JSON: {}", e.getMessage());
                Map<String, Object> fallbackData = new HashMap<>();
                fallbackData.put("data", dto.getTestData());
                fallbackData.put("type", "test-data");
                tests.add(ApiTestEntity.builder()
                        .generatedApi(api)
                        .testName("Test Data")
                        .testType("DATA")
                        .testData(fallbackData)
                        .status("PENDING")
                        .build());
            }
        }

        if (tests.isEmpty() && dto.getAssertions() != null && !dto.getAssertions().isEmpty()) {
            Map<String, Object> genericTestData = new HashMap<>();
            genericTestData.put("assertions", dto.getAssertions());
            genericTestData.put("environment", dto.getTestEnvironment());
            genericTestData.put("performanceThreshold", dto.getPerformanceThreshold());
            genericTestData.put("iterations", dto.getTestIterations());
            genericTestData.put("users", dto.getTestUsers());

            tests.add(ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName("Generic Tests")
                    .testType("GENERIC")
                    .testData(genericTestData)
                    .status("PENDING")
                    .build());
        }

        return tests;
    }

    private GeneratedApiResponseDTO mapToResponse(GeneratedApiEntity entity) {
        if (entity == null) return null;

        try {
            GeneratedApiResponseDTO response = GeneratedApiResponseDTO.builder()
                    .id(entity.getId())
                    .apiName(entity.getApiName())
                    .apiCode(entity.getApiCode())
                    .description(entity.getDescription())
                    .version(entity.getVersion())
                    .status(entity.getStatus())
                    .httpMethod(entity.getHttpMethod())
                    .basePath(entity.getBasePath())
                    .endpointPath(entity.getEndpointPath())
                    .fullEndpoint((entity.getBasePath() != null ? entity.getBasePath() : "") +
                            (entity.getEndpointPath() != null ? entity.getEndpointPath() : ""))
                    .category(entity.getCategory())
                    .owner(entity.getOwner())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .createdBy(entity.getCreatedBy())
                    .isActive(entity.getIsActive())
                    .totalCalls(entity.getTotalCalls() != null ? entity.getTotalCalls() : 0L)
                    .lastCalledAt(entity.getLastCalledAt())
                    .tags(entity.getTags() != null ? entity.getTags() : new ArrayList<>())
                    .parametersCount(entity.getParameters() != null ? entity.getParameters().size() : 0)
                    .responseMappingsCount(entity.getResponseMappings() != null ? entity.getResponseMappings().size() : 0)
                    .headersCount(entity.getHeaders() != null ? entity.getHeaders().size() : 0)
                    .build();

            if (entity.getSchemaConfig() != null) {
                ApiSchemaConfigDTO schemaDto = ApiSchemaConfigDTO.builder()
                        .schemaName(entity.getSchemaConfig().getSchemaName())
                        .objectType(entity.getSchemaConfig().getObjectType())
                        .objectName(entity.getSchemaConfig().getObjectName())
                        .operation(entity.getSchemaConfig().getOperation())
                        .primaryKeyColumn(entity.getSchemaConfig().getPrimaryKeyColumn())
                        .sequenceName(entity.getSchemaConfig().getSequenceName())
                        .enablePagination(entity.getSchemaConfig().getEnablePagination())
                        .pageSize(entity.getSchemaConfig().getPageSize())
                        .enableSorting(entity.getSchemaConfig().getEnableSorting())
                        .defaultSortColumn(entity.getSchemaConfig().getDefaultSortColumn())
                        .defaultSortDirection(entity.getSchemaConfig().getDefaultSortDirection())
                        .isSynonym(entity.getSchemaConfig().getIsSynonym())
                        .targetType(entity.getSchemaConfig().getTargetType())
                        .targetName(entity.getSchemaConfig().getTargetName())
                        .targetOwner(entity.getSchemaConfig().getTargetOwner())
                        .build();
                response.setSchemaConfig(schemaDto);
            }

            if (entity.getAuthConfig() != null) {
                ApiAuthConfigDTO authDto = ApiAuthConfigDTO.builder()
                        .authType(entity.getAuthConfig().getAuthType())
                        .apiKeyHeader(entity.getAuthConfig().getApiKeyHeader())
                        .apiKeyValue(entity.getAuthConfig().getApiKeyValue())
                        .apiKeySecret(entity.getAuthConfig().getApiKeySecret())
                        .apiKeyLocation(entity.getAuthConfig().getApiKeyLocation())
                        .apiKeyPrefix(entity.getAuthConfig().getApiKeyPrefix())
                        .basicUsername(entity.getAuthConfig().getBasicUsername())
                        .basicPassword(entity.getAuthConfig().getBasicPassword())
                        .basicRealm(entity.getAuthConfig().getBasicRealm())
                        .jwtSecret(entity.getAuthConfig().getJwtSecret())
                        .jwtIssuer(entity.getAuthConfig().getJwtIssuer())
                        .jwtAudience(entity.getAuthConfig().getJwtAudience())
                        .jwtExpiration(entity.getAuthConfig().getJwtExpiration())
                        .jwtAlgorithm(entity.getAuthConfig().getJwtAlgorithm())
                        .oauthClientId(entity.getAuthConfig().getOauthClientId())
                        .oauthClientSecret(entity.getAuthConfig().getOauthClientSecret())
                        .oauthTokenUrl(entity.getAuthConfig().getOauthTokenUrl())
                        .oauthAuthUrl(entity.getAuthConfig().getOauthAuthUrl())
                        .oauthScopes(entity.getAuthConfig().getOauthScopes())
                        .requiredRoles(entity.getAuthConfig().getRequiredRoles())
                        .customAuthFunction(entity.getAuthConfig().getCustomAuthFunction())
                        .validateSession(entity.getAuthConfig().getValidateSession())
                        .checkObjectPrivileges(entity.getAuthConfig().getCheckObjectPrivileges())
                        .ipWhitelist(entity.getAuthConfig().getIpWhitelist())
                        .rateLimitRequests(entity.getAuthConfig().getRateLimitRequests())
                        .rateLimitPeriod(entity.getAuthConfig().getRateLimitPeriod())
                        .auditLevel(entity.getAuthConfig().getAuditLevel())
                        .corsOrigins(Collections.singletonList(entity.getAuthConfig().getCorsOrigins()))
                        .corsCredentials(entity.getAuthConfig().getCorsCredentials())
                        .build();
                response.setAuthConfig(authDto);
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to map response: " + e.getMessage());
        }
    }

    private void logExecution(GeneratedApiEntity api, ExecuteApiRequestDTO request,
                              Object response, int status, long executionTime,
                              String performedBy, String clientIp, String userAgent,
                              String errorMessage) {
        try {
            ApiExecutionLogEntity log = ApiExecutionLogEntity.builder()
                    .generatedApi(api)
                    .requestId(request != null && request.getRequestId() != null ?
                            request.getRequestId() : UUID.randomUUID().toString())
                    .requestParams(request != null && request.getQueryParams() != null ?
                            request.getQueryParams() : null)
                    .requestBody(request != null && request.getBody() instanceof Map ?
                            (Map<String, Object>) request.getBody() : null)
                    .responseBody(response instanceof Map ?
                            (Map<String, Object>) response : convertToMap(response))
                    .responseStatus(status)
                    .executionTimeMs(executionTime)
                    .executedAt(LocalDateTime.now())
                    .executedBy(performedBy)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .errorMessage(errorMessage)
                    .build();

            executionLogRepository.save(log);

        } catch (Exception e) {
            log.error("Failed to log execution: {}", e.getMessage());
        }
    }

    private ExecuteApiResponseDTO createErrorResponse(String requestId, int statusCode,
                                                      String message, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;

        Map<String, Object> error = new HashMap<>();
        error.put("code", statusCode);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now().toString());

        return ExecuteApiResponseDTO.builder()
                .requestId(requestId)
                .statusCode(statusCode)
                .success(false)
                .message(message)
                .error(error)
                .executionTimeMs(executionTime)
                .build();
    }

    private Map<String, Object> buildResponseMetadata(GeneratedApiEntity api, long executionTime) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", api.getVersion());
        metadata.put("timestamp", LocalDateTime.now().toString());
        metadata.put("executionTimeMs", executionTime);

        if (api.getResponseConfig() != null && api.getResponseConfig().getMetadataFields() != null) {
            Map<String, Object> filtered = new HashMap<>();
            for (String field : api.getResponseConfig().getMetadataFields()) {
                if (field != null && metadata.containsKey(field)) {
                    filtered.put(field, metadata.get(field));
                }
            }
            return filtered;
        }

        return metadata;
    }

    private Object formatResponse(GeneratedApiEntity api, Object data) {
        if (api.getResponseConfig() != null && Boolean.TRUE.equals(api.getResponseConfig().getIncludeMetadata())) {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("data", data);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timestamp", LocalDateTime.now().toString());
            metadata.put("apiVersion", api.getVersion());
            metadata.put("requestId", UUID.randomUUID().toString());

            if (api.getResponseConfig().getMetadataFields() != null && !api.getResponseConfig().getMetadataFields().isEmpty()) {
                Map<String, Object> filteredMetadata = new HashMap<>();
                for (String field : api.getResponseConfig().getMetadataFields()) {
                    if (field != null && metadata.containsKey(field)) {
                        filteredMetadata.put(field, metadata.get(field));
                    }
                }
                if (!filteredMetadata.isEmpty()) {
                    formatted.put("metadata", filteredMetadata);
                }
            } else {
                formatted.put("metadata", metadata);
            }

            return formatted;
        }

        return data;
    }

    private boolean compareResponses(ExecuteApiResponseDTO actual, Object expected) {
        try {
            if (expected == null) return true;

            String actualJson = objectMapper.writeValueAsString(actual.getData());
            String expectedJson = objectMapper.writeValueAsString(expected);

            return actualJson.equals(expectedJson);

        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public ApiDetailsResponseDTO getCompleteApiDetails(String requestId, String apiId) {
        try {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Fetching complete API details for: " + apiId);

            GeneratedApiEntity api = generatedAPIRepository.findByIdWithConfigs(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            List<ApiParameterEntity> parameters = generatedAPIRepository.findParametersByApiId(apiId);
            List<ApiResponseMappingEntity> responseMappings = generatedAPIRepository.findResponseMappingsByApiId(apiId);
            List<ApiHeaderEntity> headers = generatedAPIRepository.findHeadersByApiId(apiId);
            List<ApiTestEntity> tests = generatedAPIRepository.findTestsByApiId(apiId);

            api.setParameters(parameters);
            api.setResponseMappings(responseMappings);
            api.setHeaders(headers);
            api.setTests(tests);

            ApiDetailsResponseDTO response = mapToApiDetailsResponse(api);

            Map<String, String> generatedFiles = generateApiCode(api);
            response.setGeneratedFiles(generatedFiles);

            response.setTotalCalls(api.getTotalCalls() != null ? api.getTotalCalls() : 0L);
            response.setLastCalledAt(api.getLastCalledAt());

            Double avgTime = executionLogRepository.getAverageExecutionTime(apiId);
            if (avgTime != null) {
                if (response.getMetadata() == null) {
                    response.setMetadata(new HashMap<>());
                }
                response.getMetadata().put("averageExecutionTimeMs", avgTime);
            }

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Complete API details fetched successfully for: " + apiId);

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error fetching complete API details: " + e.getMessage());
            throw new RuntimeException("Failed to fetch complete API details: " + e.getMessage(), e);
        }
    }

    private ApiDetailsResponseDTO mapToApiDetailsResponse(GeneratedApiEntity entity) {
        if (entity == null) return null;

        ApiDetailsResponseDTO response = ApiDetailsResponseDTO.builder()
                .id(entity.getId())
                .requestId(entity.getSourceRequestId())
                .apiName(entity.getApiName())
                .apiCode(entity.getApiCode())
                .description(entity.getDescription())
                .version(entity.getVersion())
                .status(entity.getStatus())
                .httpMethod(entity.getHttpMethod())
                .basePath(entity.getBasePath())
                .endpointPath(entity.getEndpointPath())
                .category(entity.getCategory())
                .owner(entity.getOwner())
                .tags(entity.getTags() != null ? entity.getTags() : new ArrayList<>())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .isActive(entity.getIsActive())
                .totalCalls(entity.getTotalCalls())
                .lastCalledAt(entity.getLastCalledAt())
                .metadata(new HashMap<>())
                .build();

        if (entity.getSourceObjectInfo() != null && entity.getSourceObjectInfo().containsKey("validation")) {
            response.setValidation((Map<String, Object>) entity.getSourceObjectInfo().get("validation"));
        }

        if (entity.getSourceObjectInfo() != null && entity.getSourceObjectInfo().containsKey("responseExamples")) {
            response.setResponseExamples((Map<String, Object>) entity.getSourceObjectInfo().get("responseExamples"));
        }

        if (entity.getSourceObjectInfo() != null && entity.getSourceObjectInfo().containsKey("apiDetails")) {
            response.setApiDetails((String) entity.getSourceObjectInfo().get("apiDetails"));
        }

        if (entity.getCollectionInfo() != null) {
            try {
                CollectionInfoDTO collectionInfo = objectMapper.convertValue(
                        entity.getCollectionInfo(), CollectionInfoDTO.class);
                response.setCollectionInfo(collectionInfo);
            } catch (Exception e) {
                log.warn("Failed to parse collection info: {}", e.getMessage());
            }
        }

        if (entity.getSourceObjectInfo() != null) {
            response.setSourceObject(entity.getSourceObjectInfo());
        }

        if (entity.getSchemaConfig() != null) {
            response.setSchemaConfig(mapSchemaConfigToDTO(entity.getSchemaConfig()));
        }

        if (entity.getAuthConfig() != null) {
            response.setAuthConfig(mapAuthConfigToDTO(entity.getAuthConfig()));
        }

        if (entity.getRequestConfig() != null) {
            response.setRequestBody(mapRequestConfigToDTO(entity.getRequestConfig()));
        }

        if (entity.getResponseConfig() != null) {
            response.setResponseBody(mapResponseConfigToDTO(entity.getResponseConfig()));
        }

        if (entity.getSettings() != null) {
            response.setSettings(mapSettingsToDTO(entity.getSettings()));
        }

        if (entity.getParameters() != null && !entity.getParameters().isEmpty()) {
            List<ApiParameterDTO> parameterDTOs = entity.getParameters().stream()
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(this::mapParameterToDTO)
                    .collect(Collectors.toList());
            response.setParameters(parameterDTOs);
        }

        if (entity.getResponseMappings() != null && !entity.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingDTO> mappingDTOs = entity.getResponseMappings().stream()
                    .sorted(Comparator.comparing(ApiResponseMappingEntity::getPosition,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(this::mapResponseMappingToDTO)
                    .collect(Collectors.toList());
            response.setResponseMappings(mappingDTOs);
        }

        if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
            List<ApiHeaderDTO> headerDTOs = entity.getHeaders().stream()
                    .map(this::mapHeaderToDTO)
                    .collect(Collectors.toList());
            response.setHeaders(headerDTOs);
        }

        if (entity.getTests() != null && !entity.getTests().isEmpty()) {
            ApiTestsDTO testsDTO = combineTestEntitiesToDTO(entity.getTests());
            response.setTests(testsDTO);
        }

        return response;
    }

    private ApiTestsDTO combineTestEntitiesToDTO(List<ApiTestEntity> testEntities) {
        if (testEntities == null || testEntities.isEmpty()) {
            return null;
        }

        ApiTestsDTO testsDTO = new ApiTestsDTO();
        Map<String, Object> combinedTestData = new HashMap<>();

        for (ApiTestEntity test : testEntities) {
            if (test.getTestData() != null) {
                combinedTestData.putAll(test.getTestData());
            }

            switch (test.getTestType()) {
                case "UNIT":
                    if (test.getTestData() != null && test.getTestData().containsKey("tests")) {
                        Object unitTests = test.getTestData().get("tests");
                        if (unitTests instanceof String) {
                            testsDTO.setUnitTests((String) unitTests);
                        } else if (unitTests != null) {
                            try {
                                testsDTO.setUnitTests(objectMapper.writeValueAsString(unitTests));
                            } catch (Exception e) {
                                log.warn("Failed to convert unit tests to string: {}", e.getMessage());
                            }
                        }
                    }
                    break;
                case "INTEGRATION":
                    if (test.getTestData() != null && test.getTestData().containsKey("tests")) {
                        Object integrationTests = test.getTestData().get("tests");
                        if (integrationTests instanceof String) {
                            testsDTO.setIntegrationTests((String) integrationTests);
                        } else if (integrationTests != null) {
                            try {
                                testsDTO.setIntegrationTests(objectMapper.writeValueAsString(integrationTests));
                            } catch (Exception e) {
                                log.warn("Failed to convert integration tests to string: {}", e.getMessage());
                            }
                        }
                    }
                    break;
                case "DATA":
                    if (test.getTestData() != null && test.getTestData().containsKey("data")) {
                        Object testData = test.getTestData().get("data");
                        if (testData instanceof Map) {
                            testsDTO.setTestData((Map<String, Object>) testData);
                        }
                    }
                    break;
            }
        }

        if (combinedTestData.containsKey("assertions")) {
            testsDTO.setAssertions((List<String>) combinedTestData.get("assertions"));
        }
        if (combinedTestData.containsKey("environment")) {
            testsDTO.setTestEnvironment((String) combinedTestData.get("environment"));
        }
        if (combinedTestData.containsKey("iterations")) {
            testsDTO.setTestIterations((Integer) combinedTestData.get("iterations"));
        }
        if (combinedTestData.containsKey("users")) {
            testsDTO.setTestUsers((Integer) combinedTestData.get("users"));
        }
        if (combinedTestData.containsKey("performanceThreshold")) {
            testsDTO.setPerformanceThreshold((Integer) combinedTestData.get("performanceThreshold"));
        }

        return testsDTO;
    }

    private ApiSchemaConfigDTO mapSchemaConfigToDTO(ApiSchemaConfigEntity entity) {
        if (entity == null) return null;

        return ApiSchemaConfigDTO.builder()
                .schemaName(entity.getSchemaName())
                .objectType(entity.getObjectType())
                .objectName(entity.getObjectName())
                .operation(entity.getOperation())
                .primaryKeyColumn(entity.getPrimaryKeyColumn())
                .sequenceName(entity.getSequenceName())
                .enablePagination(entity.getEnablePagination())
                .pageSize(entity.getPageSize())
                .enableSorting(entity.getEnableSorting())
                .defaultSortColumn(entity.getDefaultSortColumn())
                .defaultSortDirection(entity.getDefaultSortDirection())
                .isSynonym(entity.getIsSynonym())
                .targetType(entity.getTargetType())
                .targetName(entity.getTargetName())
                .targetOwner(entity.getTargetOwner())
                .build();
    }

    private ApiAuthConfigDTO mapAuthConfigToDTO(ApiAuthConfigEntity entity) {
        if (entity == null) return null;

        return ApiAuthConfigDTO.builder()
                .authType(entity.getAuthType())
                .apiKeyHeader(entity.getApiKeyHeader())
                .apiKeyValue(entity.getApiKeyValue())
                .apiKeySecret(entity.getApiKeySecret())
                .apiKeyLocation(entity.getApiKeyLocation())
                .apiKeyPrefix(entity.getApiKeyPrefix())
                .basicUsername(entity.getBasicUsername())
                .basicPassword(entity.getBasicPassword())
                .basicRealm(entity.getBasicRealm())
                .jwtSecret(entity.getJwtSecret())
                .jwtIssuer(entity.getJwtIssuer())
                .jwtAudience(entity.getJwtAudience())
                .jwtExpiration(entity.getJwtExpiration())
                .jwtAlgorithm(entity.getJwtAlgorithm())
                .oauthClientId(entity.getOauthClientId())
                .oauthClientSecret(entity.getOauthClientSecret())
                .oauthTokenUrl(entity.getOauthTokenUrl())
                .oauthAuthUrl(entity.getOauthAuthUrl())
                .oauthScopes(entity.getOauthScopes())
                .requiredRoles(entity.getRequiredRoles())
                .customAuthFunction(entity.getCustomAuthFunction())
                .validateSession(entity.getValidateSession())
                .checkObjectPrivileges(entity.getCheckObjectPrivileges())
                .ipWhitelist(entity.getIpWhitelist())
                .rateLimitRequests(entity.getRateLimitRequests())
                .rateLimitPeriod(entity.getRateLimitPeriod())
                .auditLevel(entity.getAuditLevel())
                .corsOrigins(parseCorsOrigins(entity.getCorsOrigins()))
                .corsCredentials(entity.getCorsCredentials())
                .build();
    }

    private ApiRequestConfigDTO mapRequestConfigToDTO(ApiRequestConfigEntity entity) {
        if (entity == null) return null;

        return ApiRequestConfigDTO.builder()
                .bodyType(entity.getBodyType())
                .sample(entity.getSample())
                .maxSize(entity.getMaxSize())
                .validateSchema(entity.getValidateSchema())
                .allowedMediaTypes(parseAllowedMediaTypes(entity.getAllowedMediaTypes()))
                .requiredFields(entity.getRequiredFields())
                .build();
    }

    private ApiResponseConfigDTO mapResponseConfigToDTO(ApiResponseConfigEntity entity) {
        if (entity == null) return null;

        return ApiResponseConfigDTO.builder()
                .successSchema(entity.getSuccessSchema())
                .errorSchema(entity.getErrorSchema())
                .includeMetadata(entity.getIncludeMetadata())
                .metadataFields(entity.getMetadataFields())
                .contentType(entity.getContentType())
                .compression(entity.getCompression())
                .build();
    }

    private ApiSettingsDTO mapSettingsToDTO(ApiSettingsEntity entity) {
        if (entity == null) return null;

        return ApiSettingsDTO.builder()
                .timeout(entity.getTimeout())
                .maxRecords(entity.getMaxRecords())
                .enableLogging(entity.getEnableLogging())
                .logLevel(entity.getLogLevel())
                .enableCaching(entity.getEnableCaching())
                .cacheTtl(entity.getCacheTtl())
                .generateSwagger(entity.getGenerateSwagger())
                .generatePostman(entity.getGeneratePostman())
                .generateClientSDK(entity.getGenerateClientSDK())
                .enableMonitoring(entity.getEnableMonitoring())
                .enableAlerts(entity.getEnableAlerts())
                .alertEmail(entity.getAlertEmail())
                .enableTracing(entity.getEnableTracing())
                .corsEnabled(entity.getCorsEnabled())
                .build();
    }

    private ApiParameterDTO mapParameterToDTO(ApiParameterEntity entity) {
        if (entity == null) return null;

        return ApiParameterDTO.builder()
                .key(entity.getKey())
                .dbColumn(entity.getDbColumn())
                .dbParameter(entity.getDbParameter())
                .oracleType(entity.getOracleType())
                .apiType(entity.getApiType())
                .parameterType(entity.getParameterType())
                .parameterLocation(entity.getParameterLocation())
                .required(entity.getRequired())
                .description(entity.getDescription())
                .example(entity.getExample())
                .validationPattern(entity.getValidationPattern())
                .defaultValue(entity.getDefaultValue())
                .inBody(entity.getInBody())
                .isPrimaryKey(entity.getIsPrimaryKey())
                .paramMode(entity.getParamMode())
                .position(entity.getPosition())
                .build();
    }

    private ApiResponseMappingDTO mapResponseMappingToDTO(ApiResponseMappingEntity entity) {
        if (entity == null) return null;

        return ApiResponseMappingDTO.builder()
                .apiField(entity.getApiField())
                .dbColumn(entity.getDbColumn())
                .oracleType(entity.getOracleType())
                .apiType(entity.getApiType())
                .format(entity.getFormat())
                .nullable(entity.getNullable())
                .isPrimaryKey(entity.getIsPrimaryKey())
                .includeInResponse(entity.getIncludeInResponse())
                .inResponse(entity.getInResponse())
                .position(entity.getPosition())
                .build();
    }

    private ApiHeaderDTO mapHeaderToDTO(ApiHeaderEntity entity) {
        if (entity == null) return null;

        return ApiHeaderDTO.builder()
                .key(entity.getKey())
                .value(entity.getValue())
                .required(entity.getRequired())
                .description(entity.getDescription())
                .isRequestHeader(entity.getIsRequestHeader())
                .isResponseHeader(entity.getIsResponseHeader())
                .build();
    }

    private List<String> parseCorsOrigins(String corsOrigins) {
        if (corsOrigins == null || corsOrigins.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return Arrays.asList(corsOrigins.split(","));
        } catch (Exception e) {
            log.warn("Failed to parse cors origins: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> parseAllowedMediaTypes(String allowedMediaTypes) {
        if (allowedMediaTypes == null || allowedMediaTypes.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return Arrays.asList(allowedMediaTypes.split(","));
        } catch (Exception e) {
            log.warn("Failed to parse allowed media types: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}