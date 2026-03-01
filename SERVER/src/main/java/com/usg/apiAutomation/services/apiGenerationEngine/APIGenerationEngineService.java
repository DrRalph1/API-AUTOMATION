package com.usg.apiAutomation.services.apiGenerationEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.codeBase.*;
import com.usg.apiAutomation.dtos.collections.*;
import com.usg.apiAutomation.dtos.documentation.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.codeBase.ImplementationEntity;
import com.usg.apiAutomation.entities.postgres.codeBase.RequestEntity;
import com.usg.apiAutomation.entities.postgres.collections.*;
import com.usg.apiAutomation.entities.postgres.collections.FolderEntity;
import com.usg.apiAutomation.entities.postgres.collections.HeaderEntity;
import com.usg.apiAutomation.entities.postgres.collections.ParameterEntity;
import com.usg.apiAutomation.entities.postgres.documentation.*;
import com.usg.apiAutomation.repositories.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.repositories.postgres.codeBase.*;
import com.usg.apiAutomation.repositories.postgres.codeBase.CollectionRepository;
import com.usg.apiAutomation.repositories.postgres.codeBase.FolderRepository;
import com.usg.apiAutomation.repositories.postgres.codeBase.RequestRepository;
import com.usg.apiAutomation.repositories.postgres.collections.*;
import com.usg.apiAutomation.repositories.postgres.collections.HeaderRepository;
import com.usg.apiAutomation.repositories.postgres.collections.ParameterRepository;
import com.usg.apiAutomation.repositories.postgres.documentation.*;
import com.usg.apiAutomation.services.OracleSchemaService;
import com.usg.apiAutomation.utils.LoggerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
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
    private final ApiValidatorService validatorService;
    private final OracleSchemaService oracleSchemaService;
    private final JdbcTemplate oracleJdbcTemplate;

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
                    .tags(request.getTags())
                    .sourceObjectInfo(sourceObjectDTO != null ?
                            objectMapper.writeValueAsString(sourceObjectDTO) : null)
                    .build();

            // Save schema config (Oracle object mapping)
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
                // Auto-generate parameters from source object (fallback)
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
                // Auto-generate response mappings from source object (fallback)
                List<ApiResponseMappingEntity> mappings = generateResponseMappingsFromSource(sourceObjectDTO, api);
                api.setResponseMappings(mappings);
            }

            // Save headers
            if (request.getHeaders() != null) {
                List<ApiHeaderEntity> headers = request.getHeaders().stream()
                        .map(headerDto -> mapToHeaderEntity(headerDto, api))
                        .collect(Collectors.toList());
                api.setHeaders(headers);
            }

            // Save tests
            if (request.getTests() != null) {
                List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
                api.setTests(tests);
            }

            // Save to database
            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);

            // Generate code and documentation
            Map<String, String> generatedFiles = generateApiCode(savedApi);
            String codeBaseRequestId = generateCodeBase(savedApi, performedBy, request);
            String collectionId = generateCollections(savedApi, performedBy, request);
            String docCollectionId = generateDocumentation(savedApi, performedBy, request, codeBaseRequestId, collectionId);

            // Build response
            GeneratedApiResponseDTO response = mapToResponse(savedApi);
            response.setGeneratedFiles(generatedFiles);

            // Add metadata with references to generated components
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("parametersCount", savedApi.getParameters() != null ? savedApi.getParameters().size() : 0);
            metadata.put("responseMappingsCount", savedApi.getResponseMappings() != null ? savedApi.getResponseMappings().size() : 0);
            metadata.put("headersCount", savedApi.getHeaders() != null ? savedApi.getHeaders().size() : 0);
            metadata.put("generatedAt", LocalDateTime.now().toString());
            metadata.put("codeBaseRequestId", codeBaseRequestId);
            metadata.put("collectionsCollectionId", collectionId);
            metadata.put("documentationCollectionId", docCollectionId);

            Map<String, String> urls = new HashMap<>();
            urls.put("codeBase", "/plx/api/code-base/requests/" + codeBaseRequestId);
            urls.put("collections", "/plx/api/collections/collections/" + collectionId);
            urls.put("documentation", "/plx/api/documentation/collections/" + docCollectionId);
            metadata.put("urls", urls);

            response.setMetadata(metadata);

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", API generated successfully with ID: " + savedApi.getId());

            return response;

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error generating API: " + e.getMessage());
            throw new RuntimeException("Failed to generate API: " + e.getMessage(), e);
        }
    }

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
            api.setTags(request.getTags());

            if (sourceObjectDTO != null) {
                api.setSourceObjectInfo(objectMapper.writeValueAsString(sourceObjectDTO));
            }

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
                // Auto-generate parameters from source object
                List<ApiParameterEntity> parameters = generateParametersFromSource(sourceObjectDTO, api);
                api.setParameters(parameters);
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
                // Auto-generate response mappings from source object
                List<ApiResponseMappingEntity> mappings = generateResponseMappingsFromSource(sourceObjectDTO, api);
                api.setResponseMappings(mappings);
            }

            // Update headers
            if (request.getHeaders() != null) {
                List<ApiHeaderEntity> headers = request.getHeaders().stream()
                        .map(headerDto -> mapToHeaderEntity(headerDto, api))
                        .collect(Collectors.toList());
                api.setHeaders(headers);
            }

            // Update tests
            if (request.getTests() != null) {
                List<ApiTestEntity> tests = createTestEntities(request.getTests(), api);
                api.setTests(tests);
            }

            // Save updated API
            GeneratedApiEntity savedApi = generatedAPIRepository.save(api);

            // Optionally regenerate code and documentation if major changes
            if (shouldRegenerateComponents(request)) {
                regenerateComponents(savedApi, performedBy, request);
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

    // Helper method to clear existing relationships
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

    // Convert SchemaConfig Entity to DTO
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

    // Convert AuthConfig Entity to DTO
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
                .corsOrigins(entity.getCorsOrigins())
                .corsCredentials(entity.getCorsCredentials())
                .build();
    }

    // Convert RequestConfig Entity to DTO
    private ApiRequestConfigDTO convertRequestConfigToDTO(ApiRequestConfigEntity entity) {
        if (entity == null) return null;

        List<String> allowedMediaTypes = null;
        if (entity.getAllowedMediaTypes() != null) {
            try {
                allowedMediaTypes = objectMapper.readValue(
                        entity.getAllowedMediaTypes(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                );
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

    // Convert ResponseConfig Entity to DTO
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

    // Convert Settings Entity to DTO
    private ApiSettingsDTO convertSettingsToDTO(ApiSettingsEntity entity) {
        if (entity == null) return null;

        List<String> corsOrigins = null;
        if (entity.getCorsOrigins() != null) {
            try {
                corsOrigins = objectMapper.readValue(
                        entity.getCorsOrigins(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                );
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
                .generateClientSdk(entity.getGenerateClientSdk())
                .enableMonitoring(entity.getEnableMonitoring())
                .enableAlerts(entity.getEnableAlerts())
                .alertEmail(entity.getAlertEmail())
                .enableTracing(entity.getEnableTracing())
                .corsEnabled(entity.getCorsEnabled())
                .corsOrigins(corsOrigins)
                .build();
    }

    // Convert Parameter Entity to DTO
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

    // Convert ResponseMapping Entity to DTO
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

    // Convert Header Entity to DTO
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

    // Helper method to convert entity back to DTO
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

        // Convert schema config
        if (api.getSchemaConfig() != null) {
            request.setSchemaConfig(convertSchemaConfigToDTO(api.getSchemaConfig()));
        }

        // Convert auth config
        if (api.getAuthConfig() != null) {
            request.setAuthConfig(convertAuthConfigToDTO(api.getAuthConfig()));
        }

        // Convert request body config
        if (api.getRequestConfig() != null) {
            request.setRequestBody(convertRequestConfigToDTO(api.getRequestConfig()));
        }

        // Convert response body config
        if (api.getResponseConfig() != null) {
            request.setResponseBody(convertResponseConfigToDTO(api.getResponseConfig()));
        }

        // Convert settings
        if (api.getSettings() != null) {
            request.setSettings(convertSettingsToDTO(api.getSettings()));
        }

        // Convert parameters
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterDTO> paramDTOs = api.getParameters().stream()
                    .map(this::convertParameterToDTO)
                    .collect(Collectors.toList());
            request.setParameters(paramDTOs);
        }

        // Convert response mappings
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingDTO> mappingDTOs = api.getResponseMappings().stream()
                    .map(this::convertResponseMappingToDTO)
                    .collect(Collectors.toList());
            request.setResponseMappings(mappingDTOs);
        }

        // Convert headers
        if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
            List<ApiHeaderDTO> headerDTOs = api.getHeaders().stream()
                    .map(this::convertHeaderToDTO)
                    .collect(Collectors.toList());
            request.setHeaders(headerDTOs);
        }

        // Parse source object info
        if (api.getSourceObjectInfo() != null) {
            try {
                Map<String, Object> sourceObject = objectMapper.readValue(
                        api.getSourceObjectInfo(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
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

        // Add auth headers if configured
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
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
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
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
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

    // Update Code Base
    private void updateCodeBase(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        try {
            // Get the code base request ID from metadata
            String codeBaseRequestId = getCodeBaseRequestId(api);
            if (codeBaseRequestId == null) {
                log.warn("No code base request ID found for API: {}", api.getId());
                return;
            }

            // Find existing request
            Optional<RequestEntity> existingRequest = codeBaseRequestRepository.findById(codeBaseRequestId);
            if (existingRequest.isPresent()) {
                RequestEntity requestEntity = existingRequest.get();

                // Update request details
                requestEntity.setName(api.getApiName() + " - " + api.getHttpMethod());
                requestEntity.setMethod(api.getHttpMethod());

                String fullUrl = (api.getBasePath() != null ? api.getBasePath() : "") +
                        (api.getEndpointPath() != null ? api.getEndpointPath() : "");
                requestEntity.setUrl(fullUrl);
                requestEntity.setDescription(api.getDescription());

                // Update headers
                updateCodeBaseHeaders(requestEntity, api);

                // Update parameters
                updateCodeBaseParameters(requestEntity, api);

                // Update request body
                updateCodeBaseRequestBody(requestEntity, api);

                // Update response example
                updateCodeBaseResponseExample(requestEntity, api);

                codeBaseRequestRepository.save(requestEntity);
                log.info("Updated code base request: {}", codeBaseRequestId);
            }

        } catch (Exception e) {
            log.error("Failed to update code base: {}", e.getMessage(), e);
        }
    }

    private void updateCollectionsAuthConfig(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            AuthConfigEntity authConfig = requestEntity.getAuthConfig();
            if (authConfig == null) {
                authConfig = new AuthConfigEntity();
                authConfig.setRequest(requestEntity);
            }

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
        }
    }

    private void updateCollectionsHeaders(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (api.getHeaders() != null) {
            List<HeaderEntity> headers = new ArrayList<>();
            for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                    HeaderEntity header = new HeaderEntity();
                    header.setKey(apiHeader.getKey());
                    header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                    header.setDescription(apiHeader.getDescription());
                    header.setEnabled(apiHeader.getRequired() != null ? apiHeader.getRequired() : true);
                    header.setRequest(requestEntity);
                    headers.add(header);
                }
            }
            requestEntity.setHeaders(headers);
        }
    }

    private void updateCollectionsParameters(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (api.getParameters() != null) {
            List<ParameterEntity> params = new ArrayList<>();
            for (ApiParameterEntity apiParam : api.getParameters()) {
                ParameterEntity param = new ParameterEntity();
                param.setKey(apiParam.getKey());
                param.setValue(apiParam.getExample() != null ? apiParam.getExample() : "");
                param.setDescription(apiParam.getDescription());
                param.setEnabled(apiParam.getRequired() != null ? apiParam.getRequired() : true);
                param.setRequest(requestEntity);
                params.add(param);
            }
            requestEntity.setParams(params);
        }
    }

    private void updateCollectionsBody(
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity,
            GeneratedApiEntity api) {

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            requestEntity.setBody(api.getRequestConfig().getSample());
        }
    }

    // Update Collections
    private void updateCollections(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        try {
            // Get the collection ID from metadata
            String collectionId = getCollectionsCollectionId(api);
            if (collectionId == null) {
                log.warn("No collection ID found for API: {}", api.getId());
                return;
            }

            // Find existing collection and its request
            Optional<CollectionEntity> existingCollection = collectionsCollectionRepository.findById(collectionId);
            if (existingCollection.isPresent()) {
                CollectionEntity collection = existingCollection.get();

                // Update collection
                collection.setName(api.getApiName() + " Collection");
                collection.setDescription(api.getDescription());
                collection.setLastActivity(LocalDateTime.now());

                // Find and update the request in this collection
                List<com.usg.apiAutomation.entities.postgres.collections.RequestEntity> requests =
                        collectionsRequestRepository.findByCollectionId(collectionId);

                if (!requests.isEmpty()) {
                    com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity = requests.get(0);

                    // Update request
                    requestEntity.setName(api.getApiName() + " - " + api.getHttpMethod());
                    requestEntity.setMethod(api.getHttpMethod());
                    requestEntity.setUrl("{{baseUrl}}" + api.getEndpointPath());
                    requestEntity.setDescription(api.getDescription());
                    requestEntity.setLastModified(LocalDateTime.now());

                    // Update auth config
                    updateCollectionsAuthConfig(requestEntity, api);

                    // Update headers
                    updateCollectionsHeaders(requestEntity, api);

                    // Update parameters
                    updateCollectionsParameters(requestEntity, api);

                    // Update body
                    updateCollectionsBody(requestEntity, api);

                    collectionsRequestRepository.save(requestEntity);
                }

                collectionsCollectionRepository.save(collection);
                log.info("Updated collections collection: {}", collectionId);
            }

        } catch (Exception e) {
            log.error("Failed to update collections: {}", e.getMessage(), e);
        }
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
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                endpoint.setRequestBodyExample(bodyMap);
            } catch (Exception e) {
                log.warn("Failed to parse request body example: {}", e.getMessage());
            }
        }
    }

    private void updateDocumentationHeaders(APIEndpointEntity endpoint, GeneratedApiEntity api) {
        // Delete existing headers
        List<com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity> existingHeaders =
                docHeaderRepository.findByEndpointId(endpoint.getId());
        docHeaderRepository.deleteAll(existingHeaders);

        // Add new headers
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

        // Add auth headers
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
        // Delete existing parameters
        List<com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity> existingParams =
                docParameterRepository.findByEndpointId(endpoint.getId());
        docParameterRepository.deleteAll(existingParams);

        // Add new parameters
        if (api.getParameters() != null) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity param =
                        new com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity();
                param.setName(apiParam.getKey());
                param.setIn(apiParam.getParameterType());
                param.setType(apiParam.getApiType());
                param.setRequired(apiParam.getRequired() != null ? apiParam.getRequired() : false);
                param.setDescription(apiParam.getDescription());
                param.setExample(apiParam.getExample());
                param.setEndpoint(endpoint);
                docParameterRepository.save(param);
            }
        }
    }

    private void updateDocumentationResponses(APIEndpointEntity endpoint, GeneratedApiEntity api) {
        // Delete existing responses
        List<ResponseExampleEntity> existingResponses =
                responseExampleRepository.findByEndpointId(endpoint.getId());
        responseExampleRepository.deleteAll(existingResponses);

        // Add success response
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
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                successExample.setExample(exampleMap);
            } catch (Exception e) {
                log.warn("Failed to parse success response example: {}", e.getMessage());
            }

            responseExampleRepository.save(successExample);
        }

        // Add error response
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
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
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
        changelog.setDate(String.valueOf(LocalDateTime.now()));
        changelog.setType("UPDATED");
        changelog.setAuthor(performedBy);
        changelog.setCollection(collection);

        List<String> changes = new ArrayList<>();
        changes.add("Updated endpoint: " + api.getApiName() + " (" + api.getHttpMethod() + ")");
        changes.add("API details modified");
        changelog.setChanges(changes);

        changelogRepository.save(changelog);
    }

    // Update Documentation
    private void updateDocumentation(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        try {
            // Get the documentation collection ID from metadata
            String docCollectionId = getDocumentationCollectionId(api);
            if (docCollectionId == null) {
                log.warn("No documentation collection ID found for API: {}", api.getId());
                return;
            }

            // Find existing documentation collection and its endpoint
            Optional<APICollectionEntity> existingCollection = docCollectionRepository.findById(docCollectionId);
            if (existingCollection.isPresent()) {
                APICollectionEntity collection = existingCollection.get();

                // Update collection
                collection.setName(api.getApiName() + " API Documentation");
                collection.setDescription(api.getDescription());
                collection.setVersion(api.getVersion());
                collection.setBaseUrl(api.getBasePath() != null ? api.getBasePath() : "");
                collection.setTags(api.getTags());
                collection.setUpdatedBy(performedBy);

                // Find and update the endpoint
                List<APIEndpointEntity> endpoints = endpointRepository.findByCollectionId(docCollectionId);
                if (!endpoints.isEmpty()) {
                    APIEndpointEntity endpoint = endpoints.get(0);

                    // Update endpoint
                    endpoint.setName(api.getApiName());
                    endpoint.setMethod(api.getHttpMethod());
                    endpoint.setUrl((api.getBasePath() != null ? api.getBasePath() : "") +
                            (api.getEndpointPath() != null ? api.getEndpointPath() : ""));
                    endpoint.setDescription(api.getDescription());
                    endpoint.setApiVersion(api.getVersion());
                    endpoint.setRequiresAuth(api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()));
                    endpoint.setCategory(api.getCategory());
                    endpoint.setTags(api.getTags());
                    endpoint.setUpdatedBy(performedBy);

                    // Update rate limit
                    updateDocumentationRateLimit(endpoint, api);

                    // Update request body example
                    updateDocumentationRequestBody(endpoint, api);

                    endpointRepository.save(endpoint);

                    // Update headers
                    updateDocumentationHeaders(endpoint, api);

                    // Update parameters
                    updateDocumentationParameters(endpoint, api);

                    // Update response examples
                    updateDocumentationResponses(endpoint, api);
                }

                // Add changelog entry
                addDocumentationChangelog(collection, api, performedBy);

                docCollectionRepository.save(collection);
                log.info("Updated documentation collection: {}", docCollectionId);
            }

        } catch (Exception e) {
            log.error("Failed to update documentation: {}", e.getMessage(), e);
        }
    }

    // Helper methods to extract IDs from metadata
    private String getCodeBaseRequestId(GeneratedApiEntity api) {
        try {
            if (api.getSourceObjectInfo() != null) {
                Map<String, Object> metadata = objectMapper.readValue(
                        api.getSourceObjectInfo(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
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
                Map<String, Object> metadata = objectMapper.readValue(
                        api.getSourceObjectInfo(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
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
                Map<String, Object> metadata = objectMapper.readValue(
                        api.getSourceObjectInfo(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                return (String) metadata.get("documentationCollectionId");
            }
        } catch (Exception e) {
            log.warn("Failed to extract documentation collection ID: {}", e.getMessage());
        }
        return null;
    }

    // Helper method to determine if components should be regenerated
    private boolean shouldRegenerateComponents(GenerateApiRequestDTO request) {
        return request.getRegenerateComponents() != null && request.getRegenerateComponents();
    }

    // Helper method to regenerate components
    private void regenerateComponents(GeneratedApiEntity api, String performedBy,
                                      GenerateApiRequestDTO request) {
        try {
            // Generate code
            generateApiCode(api);

            // Update code base
            generateCodeBase(api, performedBy, request);

            // Update collections
            generateCollections(api, performedBy, request);

            // Update documentation
            generateDocumentation(api, performedBy, request, null, null);

        } catch (Exception e) {
            log.warn("Failed to regenerate components: {}", e.getMessage());
        }
    }

    /**
     * Generate parameters automatically from Oracle source object
     */
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
                    // Get table columns as parameters
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
                            // For SELECT, primary key columns become path parameters
                            if ("SELECT".equals(sourceObject.getOperation())) {
                                boolean isPrimaryKey = column.get("isPrimaryKey") != null &&
                                        (Boolean) column.get("isPrimaryKey") ||
                                        "P".equals(column.get("constraint_type"));
                                if (isPrimaryKey) {
                                    ApiParameterEntity param = createParameterFromColumn(column, api, position++);
                                    param.setParameterType("path");
                                    parameters.add(param);
                                }
                            }
                            // For INSERT, all non-nullable columns become required parameters
                            else if ("INSERT".equals(sourceObject.getOperation())) {
                                boolean isNullable = "Y".equals(column.get("nullable"));
                                if (!isNullable) {
                                    ApiParameterEntity param = createParameterFromColumn(column, api, position++);
                                    param.setParameterType("body");
                                    parameters.add(param);
                                }
                            }
                            // For UPDATE/DELETE, primary key columns become path parameters
                            else if ("UPDATE".equals(sourceObject.getOperation()) ||
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
                    // Get procedure/function parameters
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
                    // For packages, we need to know which procedure/function to call
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

                        // Extract parameters for the specific procedure/function
                        // This would require additional logic to get arguments
                    }
                    break;
            }

        } catch (Exception e) {
            log.warn("Could not auto-generate parameters from source: {}", e.getMessage());
        }

        return parameters;
    }

    /**
     * Create parameter entity from database column
     */
    private ApiParameterEntity createParameterFromColumn(Map<String, Object> column, GeneratedApiEntity api, int position) {
        String columnName = (String) (column.get("name") != null ? column.get("name") : column.get("COLUMN_NAME"));
        String dataType = (String) (column.get("data_type") != null ? column.get("data_type") : column.get("DATA_TYPE"));
        boolean nullable = "Y".equals(column.get("nullable")) || "Y".equals(column.get("NULLABLE"));

        return ApiParameterEntity.builder()
                .generatedApi(api)
                .key(columnName.toLowerCase())
                .dbColumn(columnName)
                .dbParameter(null) // No dbParameter for columns
                .oracleType(mapOracleType(dataType))
                .apiType(mapToApiType(dataType))
                .parameterType("query")
                .required(!nullable)
                .description("Column: " + columnName)
                .example(generateExample(dataType))
                .position(position)
                .build();
    }

    /**
     * Create parameter entity from procedure/function argument
     */
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
                .dbColumn(null) // No dbColumn for parameters
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

    /**
     * Generate response mappings automatically from Oracle source object
     */
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
                    // Get table columns as response fields
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
                    // For functions, the return value becomes a response field
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
                    // For procedures, OUT parameters become response fields
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

    /**
     * Create response mapping from database column
     */
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

    /**
     * Create response mapping from procedure/function argument
     */
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

    /**
     * Create response mapping from function return type
     */
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

    /**
     * Map Oracle data type to API type
     */
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

    /**
     * Map to format
     */
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

    /**
     * Map to Oracle type
     */
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

    /**
     * Generate example value
     */
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
                    sourceObject = objectMapper.readValue(api.getSourceObjectInfo(), ApiSourceObjectDTO.class);
                } catch (Exception e) {
                    log.warn("Failed to parse source object info: {}", e.getMessage());
                }
            }

            // Validate authentication
            if (!validatorService.validateAuthentication(api, executeRequest)) {
                return createErrorResponse(requestId, 401, "Authentication failed", startTime);
            }

            // Validate authorization
            if (!validatorService.validateAuthorization(api, performedBy)) {
                return createErrorResponse(requestId, 403, "Authorization failed", startTime);
            }

            // Validate rate limiting
            if (!validatorService.checkRateLimit(api, clientIp)) {
                return createErrorResponse(requestId, 429, "Rate limit exceeded", startTime);
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
                                h -> h.getValue() != null ? h.getValue() : ""
                        ));
                response.setHeaders(responseHeaders);
            }

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
     * Execute against Oracle database based on source object type
     */
    private Object executeAgainstOracle(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                        ExecuteApiRequestDTO request) {
        try {
            if (sourceObject == null || api.getSchemaConfig() == null) {
                // Return sample data based on response mappings
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

    /**
     * Execute table operation (SELECT, INSERT, UPDATE, DELETE)
     */
    private Object executeTableOperation(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                         String tableName, String owner, ExecuteApiRequestDTO request) {
        String operation = sourceObject.getOperation() != null ?
                sourceObject.getOperation() : api.getSchemaConfig().getOperation();

        // Build parameters map
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

    /**
     * Execute SELECT on table
     */
    private Object executeTableSelect(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        if (owner != null) {
            sql.append(owner).append(".");
        }
        sql.append(tableName);

        // Build WHERE clause from parameters
        List<Object> paramValues = new ArrayList<>();
        if (params != null && !params.isEmpty()) {
            sql.append(" WHERE 1=1");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sql.append(" AND ").append(entry.getKey()).append(" = ?");
                paramValues.add(entry.getValue());
            }
        }

        // Add pagination if enabled
        if (api.getSchemaConfig() != null &&
                Boolean.TRUE.equals(api.getSchemaConfig().getEnablePagination())) {
            int pageSize = api.getSchemaConfig().getPageSize() != null ?
                    api.getSchemaConfig().getPageSize() : 10;
            int offset = 0;

            // Check for page parameter
            if (params.containsKey("page")) {
                int page = Integer.parseInt(params.get("page").toString());
                offset = (page - 1) * pageSize;
                sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
                paramValues.add(offset);
                paramValues.add(pageSize);
            }
        }

        // Execute query
        List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(
                sql.toString(), paramValues.toArray());

        return results;
    }

    /**
     * Execute INSERT on table
     */
    private Object executeTableInsert(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        if (params.isEmpty()) {
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

        String sql = "INSERT INTO " + (owner != null ? owner + "." : "") + tableName +
                " (" + columns + ") VALUES (" + values + ")";

        int rowsAffected = oracleJdbcTemplate.update(sql, paramValues.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("rowsAffected", rowsAffected);
        result.put("message", "Insert successful");

        // Get the inserted row if possible
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            // Find primary key column
            String pkColumn = api.getResponseMappings().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                    .map(ApiResponseMappingEntity::getDbColumn)
                    .findFirst()
                    .orElse(null);

            if (pkColumn != null && params.containsKey(pkColumn.toLowerCase())) {
                String selectSql = "SELECT * FROM " + (owner != null ? owner + "." : "") + tableName +
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

    /**
     * Execute UPDATE on table
     */
    private Object executeTableUpdate(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        if (params.isEmpty()) {
            throw new RuntimeException("No parameters provided for UPDATE");
        }

        // Find primary key columns
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

        String sql = "UPDATE " + (owner != null ? owner + "." : "") + tableName +
                " SET " + setClause + whereClause;

        List<Object> allParams = new ArrayList<>(setValues);
        allParams.addAll(whereValues);

        int rowsAffected = oracleJdbcTemplate.update(sql, allParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("rowsAffected", rowsAffected);
        result.put("message", rowsAffected > 0 ? "Update successful" : "No rows updated");

        return result;
    }

    /**
     * Execute DELETE on table
     */
    private Object executeTableDelete(String tableName, String owner, Map<String, Object> params,
                                      GeneratedApiEntity api) {
        if (params.isEmpty()) {
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

        String sql = "DELETE FROM " + (owner != null ? owner + "." : "") + tableName + whereClause;

        int rowsAffected = oracleJdbcTemplate.update(sql, whereValues.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("rowsAffected", rowsAffected);
        result.put("message", rowsAffected > 0 ? "Delete successful" : "No rows deleted");

        return result;
    }

    /**
     * Execute view operation (SELECT only)
     */
    private Object executeViewOperation(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                        String viewName, String owner, ExecuteApiRequestDTO request) {
        return executeTableSelect(viewName, owner,
                request.getQueryParams() != null ? request.getQueryParams() : new HashMap<>(),
                api);
    }

    /**
     * Execute stored procedure
     */
    private Object executeProcedure(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                    String procedureName, String owner, ExecuteApiRequestDTO request) {
        // Build parameters map
        Map<String, Object> inParams = new HashMap<>();
        if (request.getQueryParams() != null) inParams.putAll(request.getQueryParams());
        if (request.getPathParams() != null) inParams.putAll(request.getPathParams());
        if (request.getBody() instanceof Map) {
            inParams.putAll((Map<String, Object>) request.getBody());
        }

        // Prepare SimpleJdbcCall
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                .withSchemaName(owner)
                .withProcedureName(procedureName);

        // Add out parameters based on response mappings
        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    jdbcCall.declareParameters(
                            new SqlOutParameter(mapping.getDbColumn(), mapToSqlType(mapping.getOracleType()))
                    );
                }
            }
        }

        // Add in parameters based on API parameters
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (inParams.containsKey(param.getKey())) {
                    // Check if this is a procedure parameter (using dbParameter) or table column (using dbColumn)
                    String paramName = param.getDbParameter() != null ?
                            param.getDbParameter() :
                            (param.getDbColumn() != null ? param.getDbColumn() : param.getKey());

                    jdbcCall.declareParameters(
                            new SqlParameter(paramName, mapToSqlType(param.getOracleType()))
                    );
                }
            }
        }

        // Execute the procedure
        Map<String, Object> result = jdbcCall.execute(inParams);

        // Map out parameters to response fields
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

    /**
     * Execute function
     */
    private Object executeFunction(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                   String functionName, String owner, ExecuteApiRequestDTO request) {
        // Build parameters map
        Map<String, Object> inParams = new HashMap<>();
        if (request.getQueryParams() != null) inParams.putAll(request.getQueryParams());
        if (request.getPathParams() != null) inParams.putAll(request.getPathParams());
        if (request.getBody() instanceof Map) {
            inParams.putAll((Map<String, Object>) request.getBody());
        }

        // Prepare SimpleJdbcCall
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                .withSchemaName(owner)
                .withFunctionName(functionName);

        // Add return value
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            String returnType = api.getResponseMappings().get(0).getOracleType();
            jdbcCall.declareParameters(
                    new SqlOutParameter("return", mapToSqlType(returnType))
            );
        }

        // Add in parameters based on API parameters
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (inParams.containsKey(param.getKey())) {
                    // Check if this is a function parameter (using dbParameter) or table column (using dbColumn)
                    String paramName = param.getDbParameter() != null ?
                            param.getDbParameter() :
                            (param.getDbColumn() != null ? param.getDbColumn() : param.getKey());

                    jdbcCall.declareParameters(
                            new SqlParameter(paramName, mapToSqlType(param.getOracleType()))
                    );
                }
            }
        }

        // Execute the function
        Map<String, Object> result = jdbcCall.execute(inParams);

        // Map return value to response
        Map<String, Object> responseData = new HashMap<>();
        if (result.containsKey("return")) {
            responseData.put("result", result.get("return"));
        }

        return responseData;
    }

    /**
     * Execute package procedure/function
     */
    private Object executePackageProcedure(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                                           String packageName, String owner, ExecuteApiRequestDTO request) {
        if (sourceObject.getPackageProcedure() == null) {
            throw new RuntimeException("Package procedure not specified");
        }

        // Build parameters map
        Map<String, Object> inParams = new HashMap<>();
        if (request.getQueryParams() != null) inParams.putAll(request.getQueryParams());
        if (request.getPathParams() != null) inParams.putAll(request.getPathParams());
        if (request.getBody() instanceof Map) {
            inParams.putAll((Map<String, Object>) request.getBody());
        }

        // Prepare SimpleJdbcCall for package procedure
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                .withSchemaName(owner)
                .withCatalogName(packageName)
                .withProcedureName(sourceObject.getPackageProcedure());

        // Add out parameters based on response mappings
        if (api.getResponseMappings() != null) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    jdbcCall.declareParameters(
                            new SqlOutParameter(mapping.getDbColumn(), mapToSqlType(mapping.getOracleType()))
                    );
                }
            }
        }

        // Add in parameters based on API parameters
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (inParams.containsKey(param.getKey())) {
                    // Check if this is a procedure parameter (using dbParameter) or table column (using dbColumn)
                    String paramName = param.getDbParameter() != null ?
                            param.getDbParameter() :
                            (param.getDbColumn() != null ? param.getDbColumn() : param.getKey());

                    jdbcCall.declareParameters(
                            new SqlParameter(paramName, mapToSqlType(param.getOracleType()))
                    );
                }
            }
        }

        // Execute the package procedure
        Map<String, Object> result = jdbcCall.execute(inParams);

        // Map out parameters to response fields
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

    // Add this method to get the entity
    public GeneratedApiEntity getApiEntity(String apiId) {
        return generatedAPIRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API not found: " + apiId));
    }

    // Add this method to get execution logs
    public List<ApiExecutionLogDTO> getExecutionLogs(String apiId, LocalDateTime fromDate,
                                                     LocalDateTime toDate, int limit) {
        try {
            List<ApiExecutionLogEntity> logs;

            if (fromDate != null && toDate != null) {
                logs = executionLogRepository.findByGeneratedApiIdAndExecutedAtBetween(
                        apiId, fromDate, toDate);
            } else {
                // Use pagination for the default case
                org.springframework.data.domain.PageRequest pageRequest =
                        org.springframework.data.domain.PageRequest.of(0, limit);
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

    // Update this method to use your existing repository methods
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

    // Add this method to update API status
    @Transactional
    public GeneratedApiResponseDTO updateApiStatus(String apiId, String status, String performedBy) {
        try {
            GeneratedApiEntity api = getApiEntity(apiId);

            // Validate status
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

    // Add this method to validate source object (public version of your existing private method)
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

                // If it's a synonym, resolve it
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

                // Get additional details based on object type
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

    // Helper method to get source object details
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

    // Helper method to map execution log entity to DTO
    private ApiExecutionLogDTO mapToExecutionLogDTO(ApiExecutionLogEntity entity) {
        if (entity == null) return null;

        return ApiExecutionLogDTO.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .apiId(entity.getGeneratedApi() != null ? entity.getGeneratedApi().getId() : null)
                .requestId(entity.getRequestId())
                .requestParams(entity.getRequestParams())
                .requestBody(entity.getRequestBody())
                .responseBody(entity.getResponseBody())
                .responseStatus(entity.getResponseStatus())
                .executionTimeMs(entity.getExecutionTimeMs())
                .executedAt(entity.getExecutedAt())
                .executedBy(entity.getExecutedBy())
                .clientIp(entity.getClientIp())
                .userAgent(entity.getUserAgent())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    // Helper method to map test entity to result DTO
    private ApiTestResultDTO mapToTestResultDTO(ApiTestEntity entity) {
        if (entity == null) return null;

        Object actualResponse = null;
        try {
            if (entity.getActualResponse() != null) {
                actualResponse = objectMapper.readValue(entity.getActualResponse(), Object.class);
            }
        } catch (Exception e) {
            log.warn("Failed to parse actual response: {}", e.getMessage());
        }

        return ApiTestResultDTO.builder()
                .testName(entity.getTestName())
                .passed("PASSED".equals(entity.getStatus()))
                .executionTimeMs(entity.getExecutionTimeMs())
                .statusCode(extractStatusCode(entity))
                .actualResponse(actualResponse)
                .message(entity.getStatus())
                .build();
    }

    // Helper method to extract status code from test entity
    private int extractStatusCode(ApiTestEntity entity) {
        try {
            if (entity.getActualResponse() != null) {
                Map<String, Object> response = objectMapper.readValue(
                        entity.getActualResponse(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                if (response.containsKey("statusCode")) {
                    return (Integer) response.get("statusCode");
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return 200;
    }

    /**
     * Map Oracle type to SQL type for JDBC
     */
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

    /**
     * Generate sample response based on response mappings
     */
    private Object generateSampleResponse(GeneratedApiEntity api) {
        Map<String, Object> result = new HashMap<>();

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            for (var mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    // Generate sample data based on type
                    switch (mapping.getApiType()) {
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

            // Execute test
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

            // Compare with expected response
            boolean passed = compareResponses(executionResult, testRequest.getExpectedResponse());

            // Save test result
            ApiTestEntity testEntity = ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName(testRequest.getTestName())
                    .testType(testRequest.getTestType())
                    .testData(objectMapper.writeValueAsString(testRequest))
                    .expectedResponse(objectMapper.writeValueAsString(testRequest.getExpectedResponse()))
                    .actualResponse(objectMapper.writeValueAsString(executionResult))
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

    public GeneratedApiResponseDTO getApiDetails(String requestId, String apiId) {
        try {
            GeneratedApiEntity api = generatedAPIRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException("API not found: " + apiId));

            GeneratedApiResponseDTO response = mapToResponse(api);

            // Add execution stats
            response.setTotalCalls(api.getTotalCalls());
            response.setLastCalledAt(api.getLastCalledAt());

            // Add average execution time
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

            // Calculate statistics
            long totalCalls = logs.size();
            double avgExecutionTime = logs.stream()
                    .mapToLong(ApiExecutionLogEntity::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);
            long totalErrors = logs.stream()
                    .filter(log -> log.getResponseStatus() >= 400)
                    .count();
            double successRate = totalCalls > 0 ?
                    ((totalCalls - totalErrors) * 100.0 / totalCalls) : 0.0;

            // Status code distribution
            Map<Integer, Long> statusDistribution = logs.stream()
                    .collect(Collectors.groupingBy(
                            ApiExecutionLogEntity::getResponseStatus,
                            Collectors.counting()
                    ));

            // Daily call stats
            List<Object[]> dailyStats = executionLogRepository
                    .getDailyCallStats(apiId, startDate);

            return ApiAnalyticsDTO.builder()
                    .apiId(apiId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalCalls(totalCalls)
                    .averageExecutionTimeMs(avgExecutionTime)
                    .totalErrors(totalErrors)
                    .successRate(successRate)
                    .statusDistribution(statusDistribution)
                    .dailyCallStats(dailyStats.stream()
                            .collect(Collectors.toMap(
                                    stat -> stat[0].toString(),
                                    stat -> (Long) stat[1]
                            )))
                    .build();

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error getting API analytics: " + e.getMessage());
            throw new RuntimeException("Failed to get API analytics: " + e.getMessage(), e);
        }
    }

    public Map<String, String> generateApiCode(GeneratedApiEntity api) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Generate PL/SQL package
        generatedFiles.put("plsql", generatePlSqlPackage(api));

        // Generate OpenAPI spec if enabled
        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGenerateSwagger())) {
            generatedFiles.put("openapi", generateOpenApiSpec(api));
        }

        // Generate Postman collection if enabled
        if (api.getSettings() != null && Boolean.TRUE.equals(api.getSettings().getGeneratePostman())) {
            generatedFiles.put("postman", generatePostmanCollection(api));
        }

        return generatedFiles;
    }

    // ============================================================
    // Code Generation Methods (PL/SQL, OpenAPI, Postman)
    // ============================================================

    private String generatePlSqlPackage(GeneratedApiEntity api) {
        StringBuilder sb = new StringBuilder();

        sb.append("-- ============================================================\n");
        sb.append("-- Generated API Package: ").append(api.getApiName()).append("\n");
        sb.append("-- Generated: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("-- Version: ").append(api.getVersion()).append("\n");
        sb.append("-- API Code: ").append(api.getApiCode()).append("\n");
        sb.append("-- ============================================================\n\n");

        if (api.getSchemaConfig() != null) {
            sb.append("-- Source Object: ")
                    .append(api.getSchemaConfig().getSchemaName()).append(".")
                    .append(api.getSchemaConfig().getObjectName())
                    .append(" (").append(api.getSchemaConfig().getObjectType()).append(")\n");
            sb.append("-- Operation: ").append(api.getSchemaConfig().getOperation()).append("\n\n");
        }

        // Package specification
        sb.append("CREATE OR REPLACE PACKAGE ").append(api.getApiCode()).append("_PKG AS\n\n");
        sb.append("  -- Main procedure\n");
        sb.append("  PROCEDURE execute_api(\n");

        // Add parameters
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (int i = 0; i < api.getParameters().size(); i++) {
                var param = api.getParameters().get(i);
                // For parameters, use dbParameter if available (for procedures/functions)
                String paramName = param.getDbParameter() != null ?
                        "p_" + param.getDbParameter() :
                        "p_" + param.getKey();

                sb.append("    ").append(paramName).append(" IN ").append(param.getOracleType());
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

        // Helper functions
        sb.append("  FUNCTION validate_parameters RETURN BOOLEAN;\n");
        sb.append("  FUNCTION format_response RETURN CLOB;\n\n");

        sb.append("END ").append(api.getApiCode()).append("_PKG;\n");
        sb.append("/\n\n");

        // Package body
        sb.append("CREATE OR REPLACE PACKAGE BODY ").append(api.getApiCode()).append("_PKG AS\n\n");

        sb.append("  g_api_version CONSTANT VARCHAR2(10) := '").append(api.getVersion()).append("';\n");
        sb.append("  g_api_name CONSTANT VARCHAR2(100) := '").append(api.getApiName()).append("';\n\n");

        sb.append("  PROCEDURE execute_api(\n");

        // Add parameters again for body
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (int i = 0; i < api.getParameters().size(); i++) {
                var param = api.getParameters().get(i);
                String paramName = param.getDbParameter() != null ?
                        "p_" + param.getDbParameter() :
                        "p_" + param.getKey();

                sb.append("    ").append(paramName).append(" IN ").append(param.getOracleType());
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

        // Determine return type
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            sb.append(api.getResponseMappings().get(0).getOracleType());
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

        // Generate operation logic based on config
        if (api.getSchemaConfig() != null) {
            String schemaName = api.getSchemaConfig().getSchemaName();
            String objectName = api.getSchemaConfig().getObjectName();
            String operation = api.getSchemaConfig().getOperation();

            switch (operation) {
                case "SELECT":
                    sb.append("    OPEN v_cursor FOR\n");
                    sb.append("    SELECT ");
                    if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                        sb.append(api.getResponseMappings().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getIncludeInResponse()))
                                .map(ApiResponseMappingEntity::getDbColumn)
                                .collect(Collectors.joining(", ")));
                    } else {
                        sb.append("*");
                    }
                    sb.append("\n    FROM ")
                            .append(schemaName).append(".")
                            .append(objectName);
                    sb.append("\n    WHERE 1=1\n");

                    // Add parameter filters
                    if (api.getParameters() != null) {
                        for (var param : api.getParameters()) {
                            if ("query".equals(param.getParameterType()) ||
                                    "path".equals(param.getParameterType())) {
                                String columnName = param.getDbColumn() != null ? param.getDbColumn() : param.getKey();
                                String paramName = param.getDbParameter() != null ?
                                        "p_" + param.getDbParameter() :
                                        "p_" + param.getKey();

                                sb.append("    AND ")
                                        .append(columnName)
                                        .append(" = ").append(paramName).append("\n");
                            }
                        }
                    }

                    // Add pagination
                    if (Boolean.TRUE.equals(api.getSchemaConfig().getEnablePagination())) {
                        sb.append("    OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY\n");
                    }
                    break;

                case "INSERT":
                    sb.append("    INSERT INTO ")
                            .append(schemaName).append(".")
                            .append(objectName)
                            .append(" (\n      ");

                    // Add columns
                    if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                        String columns = api.getResponseMappings().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getIncludeInResponse()))
                                .map(ApiResponseMappingEntity::getDbColumn)
                                .collect(Collectors.joining(",\n      "));
                        sb.append(columns);
                    }
                    sb.append("\n    ) VALUES (\n      ");

                    // Add values from parameters
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        String values = api.getParameters().stream()
                                .map(p -> {
                                    return p.getDbParameter() != null ?
                                            "p_" + p.getDbParameter() :
                                            "p_" + p.getKey();
                                })
                                .collect(Collectors.joining(",\n      "));
                        sb.append(values);
                    }
                    sb.append("\n    );\n");

                    // Get the inserted row using sequence or returning clause
                    if (api.getSchemaConfig().getSequenceName() != null) {
                        sb.append("\n    -- Get generated ID\n");
                        sb.append("    SELECT ").append(api.getSchemaConfig().getSequenceName())
                                .append(".CURRVAL INTO v_result FROM DUAL;\n");
                    }
                    break;

                case "UPDATE":
                    sb.append("    UPDATE ")
                            .append(schemaName).append(".")
                            .append(objectName)
                            .append("\n    SET\n      ");

                    // Add set clauses
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        String setClauses = api.getParameters().stream()
                                .filter(p -> !"query".equals(p.getParameterType()) && !"path".equals(p.getParameterType()))
                                .map(p -> {
                                    String columnName = p.getDbColumn() != null ? p.getDbColumn() : p.getKey();
                                    String paramName = p.getDbParameter() != null ?
                                            "p_" + p.getDbParameter() :
                                            "p_" + p.getKey();
                                    return columnName + " = " + paramName;
                                })
                                .collect(Collectors.joining(",\n      "));
                        sb.append(setClauses);
                    }
                    sb.append("\n    WHERE 1=1\n");

                    // Add where conditions from path/query parameters
                    if (api.getParameters() != null) {
                        for (var param : api.getParameters()) {
                            if ("query".equals(param.getParameterType()) ||
                                    "path".equals(param.getParameterType())) {
                                String columnName = param.getDbColumn() != null ? param.getDbColumn() : param.getKey();
                                String paramName = param.getDbParameter() != null ?
                                        "p_" + param.getDbParameter() :
                                        "p_" + param.getKey();

                                sb.append("      AND ")
                                        .append(columnName)
                                        .append(" = ").append(paramName).append("\n");
                            }
                        }
                    }
                    break;

                case "DELETE":
                    sb.append("    DELETE FROM ")
                            .append(schemaName).append(".")
                            .append(objectName);
                    sb.append("\n    WHERE 1=1\n");

                    // Add where conditions from path/query parameters
                    if (api.getParameters() != null) {
                        for (var param : api.getParameters()) {
                            if ("query".equals(param.getParameterType()) ||
                                    "path".equals(param.getParameterType())) {
                                String columnName = param.getDbColumn() != null ? param.getDbColumn() : param.getKey();
                                String paramName = param.getDbParameter() != null ?
                                        "p_" + param.getDbParameter() :
                                        "p_" + param.getKey();

                                sb.append("      AND ")
                                        .append(columnName)
                                        .append(" = ").append(paramName).append("\n");
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
                    sb.append(schemaName).append(".")
                            .append(objectName).append("(\n");

                    // Add parameters
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        for (int i = 0; i < api.getParameters().size(); i++) {
                            var param = api.getParameters().get(i);
                            String paramName = param.getDbParameter() != null ?
                                    "p_" + param.getDbParameter() :
                                    "p_" + param.getKey();

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

        // validate_parameters function
        sb.append("  FUNCTION validate_parameters RETURN BOOLEAN IS\n");
        sb.append("  BEGIN\n");
        sb.append("    -- Add parameter validation logic here\n");
        sb.append("    RETURN TRUE;\n");
        sb.append("  END validate_parameters;\n\n");

        // format_response function
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

        // OpenAPI version
        spec.put("openapi", "3.0.0");

        // Info
        Map<String, Object> info = new HashMap<>();
        info.put("title", api.getApiName());
        info.put("description", api.getDescription());
        info.put("version", api.getVersion());

        if (api.getOwner() != null) {
            Map<String, Object> contact = new HashMap<>();
            contact.put("name", api.getOwner());
            info.put("contact", contact);
        }
        spec.put("info", info);

        // Servers
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

        // Paths
        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> pathItem = new HashMap<>();
        Map<String, Object> operation = new HashMap<>();

        operation.put("summary", api.getApiName());
        operation.put("description", api.getDescription());
        operation.put("operationId", api.getApiCode().toLowerCase());
        operation.put("tags", api.getTags() != null ? api.getTags() : Arrays.asList("default"));

        // Parameters
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (var param : api.getParameters()) {
                Map<String, Object> paramSpec = new HashMap<>();
                paramSpec.put("name", param.getKey());
                paramSpec.put("in", param.getParameterType());
                paramSpec.put("description", param.getDescription());
                paramSpec.put("required", param.getRequired());

                Map<String, Object> schema = new HashMap<>();
                schema.put("type", param.getApiType());
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

        // Request Body
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

        // Responses
        Map<String, Object> responses = new HashMap<>();

        // Success response (200)
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

                // Add properties from response mappings
                if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                    Map<String, Object> properties = new HashMap<>();
                    for (var mapping : api.getResponseMappings()) {
                        if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                            Map<String, Object> propSchema = new HashMap<>();
                            propSchema.put("type", mapping.getApiType());
                            if (mapping.getFormat() != null && !mapping.getFormat().isEmpty()) {
                                propSchema.put("format", mapping.getFormat());
                            }
                            if (Boolean.TRUE.equals(mapping.getNullable())) {
                                propSchema.put("nullable", true);
                            }
                            properties.put(mapping.getApiField(), propSchema);
                        }
                    }
                    schema.put("properties", properties);
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

        // Security
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            List<Map<String, List<String>>> security = new ArrayList<>();
            Map<String, List<String>> securityReq = new HashMap<>();
            securityReq.put(api.getAuthConfig().getAuthType().toLowerCase(), new ArrayList<>());
            security.add(securityReq);
            operation.put("security", security);
        }

        pathItem.put(api.getHttpMethod().toLowerCase(), operation);
        paths.put(api.getEndpointPath() != null ? api.getEndpointPath() : "/", pathItem);
        spec.put("paths", paths);

        // Components (Security Schemes)
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

        // Convert to JSON string
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec);
        } catch (Exception e) {
            log.error("Error generating OpenAPI spec: {}", e.getMessage());
            return "{}";
        }
    }

    private String generatePostmanCollection(GeneratedApiEntity api) {
        Map<String, Object> collection = new HashMap<>();

        // Info
        Map<String, Object> info = new HashMap<>();
        info.put("name", api.getApiName());
        info.put("description", api.getDescription());
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put("info", info);

        // Items
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", api.getApiName());

        // Request
        Map<String, Object> request = new HashMap<>();
        request.put("method", api.getHttpMethod());

        // URL
        Map<String, Object> url = new HashMap<>();
        String fullPath = (api.getBasePath() != null ? api.getBasePath() : "") +
                (api.getEndpointPath() != null ? api.getEndpointPath() : "");
        url.put("raw", "{{baseUrl}}" + fullPath);
        url.put("host", Arrays.asList("{{baseUrl}}"));

        // Parse path
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

        // Query parameters
        if (api.getParameters() != null) {
            List<Map<String, Object>> queryParams = new ArrayList<>();
            for (var param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    Map<String, Object> queryParam = new HashMap<>();
                    queryParam.put("key", param.getKey());
                    queryParam.put("value", param.getExample() != null ? param.getExample() : "");
                    queryParam.put("description", param.getDescription());
                    queryParam.put("disabled", !Boolean.TRUE.equals(param.getRequired()));
                    queryParams.add(queryParam);
                }
            }
            if (!queryParams.isEmpty()) {
                url.put("query", queryParams);
            }
        }

        request.put("url", url);

        // Headers
        if (api.getHeaders() != null || (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()))) {
            List<Map<String, Object>> headers = new ArrayList<>();

            // Add configured headers
            if (api.getHeaders() != null) {
                for (var header : api.getHeaders()) {
                    if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                        Map<String, Object> headerSpec = new HashMap<>();
                        headerSpec.put("key", header.getKey());
                        headerSpec.put("value", header.getValue() != null ? header.getValue() : "");
                        headerSpec.put("description", header.getDescription());
                        headerSpec.put("disabled", !Boolean.TRUE.equals(header.getRequired()));
                        headers.add(headerSpec);
                    }
                }
            }

            // Add auth headers based on auth type
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

        // Body
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

        // Add response examples if available
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

        // Variables
        List<Map<String, Object>> variables = new ArrayList<>();

        Map<String, Object> baseUrlVar = new HashMap<>();
        baseUrlVar.put("key", "baseUrl");
        baseUrlVar.put("value", "https://api.example.com");
        baseUrlVar.put("type", "string");
        variables.add(baseUrlVar);

        // Add auth variables based on auth type
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

        // Convert to JSON string
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        } catch (Exception e) {
            log.error("Error generating Postman collection: {}", e.getMessage());
            return "{}";
        }
    }

    // Helper method to convert Map to ApiSourceObjectDTO
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

    // Helper method to convert Map to ApiSourceObjectDTO
    private ApiSourceObjectDTO convertToSourceObjectDTO(Object sourceObject) {
        if (sourceObject == null) {
            return null;
        }

        try {
            // If it's already a DTO, return it
            if (sourceObject instanceof ApiSourceObjectDTO) {
                return (ApiSourceObjectDTO) sourceObject;
            }

            // If it's a Map, convert it to DTO
            if (sourceObject instanceof Map) {
                return objectMapper.convertValue(sourceObject, ApiSourceObjectDTO.class);
            }

            // If it's a String (JSON), parse it
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

    // ============================================================
    // Helper Methods for Code Generation
    // ============================================================

    private String generateCodeBase(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        try {
            log.info("Generating Code Base for API: {}", api.getApiCode());

            // Create or get collection in code base
            com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity collection;
            String collectionName = api.getApiName() + " API Collection";

            Optional<com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity> existingCollection =
                    codeBaseCollectionRepository.findByNameAndOwner(collectionName, performedBy);

            if (existingCollection.isPresent()) {
                collection = existingCollection.get();
            } else {
                collection = com.usg.apiAutomation.entities.postgres.codeBase.CollectionEntity.builder()
                        .name(collectionName)
                        .description("Auto-generated collection for " + api.getApiName() + " API")
                        .version(api.getVersion())
                        .owner(performedBy)
                        .isExpanded(false)
                        .isFavorite(false)
                        .build();
                collection = codeBaseCollectionRepository.save(collection);
            }

            // Create folder for this API
            com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity folder =
                    com.usg.apiAutomation.entities.postgres.codeBase.FolderEntity.builder()
                            .name(api.getApiName())
                            .description(api.getDescription())
                            .isExpanded(false)
                            .collection(collection)
                            .build();
            folder = codeBaseFolderRepository.save(folder);

            // Build full endpoint URL
            String fullUrl = (api.getBasePath() != null ? api.getBasePath() : "") +
                    (api.getEndpointPath() != null ? api.getEndpointPath() : "");

            // Create request entity
            RequestEntity codeBaseRequest = RequestEntity.builder()
                    .name(api.getApiName() + " - " + api.getHttpMethod())
                    .method(api.getHttpMethod())
                    .url(fullUrl)
                    .description(api.getDescription())
                    .collection(collection)
                    .folder(folder)
                    .build();

            // Add headers
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

            // Add auth headers if configured
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
            codeBaseRequest.setHeaders(headers);

            // Add path parameters
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
                codeBaseRequest.setPathParameters(pathParams);
            }

            // Add request body
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                try {
                    Map<String, Object> requestBodyMap = objectMapper.readValue(
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

            // Add response example
            if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(
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

            // Generate implementations for multiple languages
            generateImplementations(api, savedRequest);

            log.info("Code Base generated successfully with Request ID: {}", savedRequest.getId());
            return savedRequest.getId();

        } catch (Exception e) {
            log.error("Error generating Code Base: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Code Base: " + e.getMessage(), e);
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
                            .linesOfCode(code.split("\n").length)
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

    private String generateCollections(GeneratedApiEntity api, String performedBy, GenerateApiRequestDTO request) {
        try {
            log.info("Generating Collections for API: {}", api.getApiCode());

            // Create collection
            CollectionEntity collection = new CollectionEntity();
            collection.setName(api.getApiName() + " Collection");
            collection.setDescription(api.getDescription());
            collection.setOwner(performedBy);
            collection.setExpanded(false);
            collection.setEditing(false);
            collection.setFavorite(false);
            collection.setLastActivity(LocalDateTime.now());
            collection.setColor(getRandomColor());

            // Add variables
            List<VariableEntity> variables = new ArrayList<>();

            // Add auth variables
            if (api.getAuthConfig() != null) {
                if (api.getAuthConfig().getApiKeyValue() != null) {
                    VariableEntity apiKeyVar = new VariableEntity();
                    apiKeyVar.setKey("apiKey");
                    apiKeyVar.setValue(api.getAuthConfig().getApiKeyValue());
                    apiKeyVar.setType("string");
                    apiKeyVar.setEnabled(true);
                    apiKeyVar.setCollection(collection);
                    variables.add(apiKeyVar);
                }
                if (api.getAuthConfig().getApiKeySecret() != null) {
                    VariableEntity apiSecretVar = new VariableEntity();
                    apiSecretVar.setKey("apiSecret");
                    apiSecretVar.setValue(api.getAuthConfig().getApiKeySecret());
                    apiSecretVar.setType("string");
                    apiSecretVar.setEnabled(true);
                    apiSecretVar.setCollection(collection);
                    variables.add(apiSecretVar);
                }
            }

            // Add base URL variable
            VariableEntity baseUrlVar = new VariableEntity();
            baseUrlVar.setKey("baseUrl");
            baseUrlVar.setValue(api.getBasePath() != null ? api.getBasePath() : "https://api.example.com");
            baseUrlVar.setType("string");
            baseUrlVar.setEnabled(true);
            baseUrlVar.setCollection(collection);
            variables.add(baseUrlVar);

            collection.setVariables(variables);

            CollectionEntity savedCollection = collectionsCollectionRepository.save(collection);

            // Create folder
            FolderEntity folder = new FolderEntity();
            folder.setName(api.getApiName());
            folder.setDescription(api.getDescription());
            folder.setExpanded(false);
            folder.setEditing(false);
            folder.setRequestCount(0);
            folder.setCollection(savedCollection);
            FolderEntity savedFolder = collectionsFolderRepository.save(folder);

            // Create request
            com.usg.apiAutomation.entities.postgres.collections.RequestEntity requestEntity =
                    new com.usg.apiAutomation.entities.postgres.collections.RequestEntity();
            requestEntity.setName(api.getApiName() + " - " + api.getHttpMethod());
            requestEntity.setMethod(api.getHttpMethod());
            requestEntity.setUrl("{{baseUrl}}" + api.getEndpointPath());
            requestEntity.setDescription(api.getDescription());
            requestEntity.setCollection(savedCollection);
            requestEntity.setFolder(savedFolder);
            requestEntity.setSaved(true);
            requestEntity.setLastModified(LocalDateTime.now());

            // Add auth config
            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                AuthConfigEntity authConfig = new AuthConfigEntity();
                authConfig.setType(api.getAuthConfig().getAuthType());
                authConfig.setRequest(requestEntity);

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
            }

            // Add headers
            if (api.getHeaders() != null) {
                List<HeaderEntity> headers = new ArrayList<>();
                for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                    if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                        HeaderEntity header = new HeaderEntity();
                        header.setKey(apiHeader.getKey());
                        header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                        header.setDescription(apiHeader.getDescription());
                        header.setEnabled(apiHeader.getRequired() != null ? apiHeader.getRequired() : true);
                        header.setRequest(requestEntity);
                        headers.add(header);
                    }
                }
                requestEntity.setHeaders(headers);
            }

            // Add parameters
            if (api.getParameters() != null) {
                List<ParameterEntity> params = new ArrayList<>();
                for (ApiParameterEntity apiParam : api.getParameters()) {
                    ParameterEntity param = new ParameterEntity();
                    param.setKey(apiParam.getKey());
                    param.setValue(apiParam.getExample() != null ? apiParam.getExample() : "");
                    param.setDescription(apiParam.getDescription());
                    param.setEnabled(apiParam.getRequired() != null ? apiParam.getRequired() : true);
                    param.setRequest(requestEntity);
                    params.add(param);
                }
                requestEntity.setParams(params);
            }

            // Add body
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                requestEntity.setBody(api.getRequestConfig().getSample());
            }

            com.usg.apiAutomation.entities.postgres.collections.RequestEntity savedRequest =
                    collectionsRequestRepository.save(requestEntity);

            // Update folder request count
            savedFolder.setRequestCount(1);
            collectionsFolderRepository.save(savedFolder);

            log.info("Collections generated successfully with Collection ID: {}", savedCollection.getId());
            return savedCollection.getId();

        } catch (Exception e) {
            log.error("Error generating Collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Collections: " + e.getMessage(), e);
        }
    }

    private String generateDocumentation(GeneratedApiEntity api, String performedBy,
                                         GenerateApiRequestDTO request,
                                         String codeBaseRequestId,
                                         String collectionsCollectionId) {
        try {
            log.info("Generating Documentation for API: {}", api.getApiCode());

            // Create documentation collection
            APICollectionEntity docCollection = new APICollectionEntity();
            docCollection.setName(api.getApiName() + " API Documentation");
            docCollection.setDescription(api.getDescription());
            docCollection.setVersion(api.getVersion());
            docCollection.setOwner(performedBy);
            docCollection.setType("REST");
            docCollection.setFavorite(false);
            docCollection.setExpanded(false);
            docCollection.setColor(getRandomColor());
            docCollection.setStatus("published");
            docCollection.setBaseUrl(api.getBasePath() != null ? api.getBasePath() : "");
            docCollection.setTags(api.getTags());
            docCollection.setCreatedBy(performedBy);
            docCollection.setUpdatedBy(performedBy);
            docCollection.setTotalEndpoints(0);
            docCollection.setTotalFolders(0);

            APICollectionEntity savedDocCollection = docCollectionRepository.save(docCollection);

            // Create folder
            com.usg.apiAutomation.entities.postgres.documentation.FolderEntity docFolder =
                    new com.usg.apiAutomation.entities.postgres.documentation.FolderEntity();
            docFolder.setName(api.getApiName());
            docFolder.setDescription(api.getDescription());
            docFolder.setCollection(savedDocCollection);
            docFolder.setDisplayOrder(1);
            docFolder.setCreatedBy(performedBy);
            docFolder.setUpdatedBy(performedBy);
            com.usg.apiAutomation.entities.postgres.documentation.FolderEntity savedDocFolder =
                    docFolderRepository.save(docFolder);

            // Update collection folder count
            savedDocCollection.setTotalFolders(1);
            docCollectionRepository.save(savedDocCollection);

            // Create endpoint
            APIEndpointEntity endpoint = new APIEndpointEntity();
            endpoint.setName(api.getApiName());
            endpoint.setMethod(api.getHttpMethod());
            endpoint.setUrl((api.getBasePath() != null ? api.getBasePath() : "") +
                    (api.getEndpointPath() != null ? api.getEndpointPath() : ""));
            endpoint.setDescription(api.getDescription());
            endpoint.setCollection(savedDocCollection);
            endpoint.setFolder(savedDocFolder);
            endpoint.setApiVersion(api.getVersion());
            endpoint.setRequiresAuth(api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType()));
            endpoint.setDeprecated(false);
            endpoint.setCategory(api.getCategory());
            endpoint.setTags(api.getTags());
            endpoint.setCreatedBy(performedBy);
            endpoint.setUpdatedBy(performedBy);
            endpoint.setLastModifiedBy(performedBy);

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
                    Map<String, Object> bodyMap = objectMapper.readValue(api.getRequestConfig().getSample(), Map.class);
                    endpoint.setRequestBodyExample(bodyMap);
                } catch (Exception e) {
                    log.warn("Failed to parse request body example: {}", e.getMessage());
                }
            }

            APIEndpointEntity savedEndpoint = endpointRepository.save(endpoint);

            // Add headers
            if (api.getHeaders() != null) {
                for (ApiHeaderEntity apiHeader : api.getHeaders()) {
                    if (Boolean.TRUE.equals(apiHeader.getIsRequestHeader())) {
                        com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity header =
                                new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();
                        header.setKey(apiHeader.getKey());
                        header.setValue(apiHeader.getValue() != null ? apiHeader.getValue() : "");
                        header.setDescription(apiHeader.getDescription());
                        header.setRequired(apiHeader.getRequired() != null ? apiHeader.getRequired() : false);
                        header.setEndpoint(savedEndpoint);
                        docHeaderRepository.save(header);
                    }
                }
            }

            // Add auth headers to documentation
            if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
                com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity authHeader =
                        new com.usg.apiAutomation.entities.postgres.documentation.HeaderEntity();
                authHeader.setRequired(true);
                authHeader.setEndpoint(savedEndpoint);

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

            // Add parameters
            if (api.getParameters() != null) {
                for (ApiParameterEntity apiParam : api.getParameters()) {
                    com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity param =
                            new com.usg.apiAutomation.entities.postgres.documentation.ParameterEntity();
                    param.setName(apiParam.getKey());
                    param.setIn(apiParam.getParameterType());
                    param.setType(apiParam.getApiType());
                    param.setRequired(apiParam.getRequired() != null ? apiParam.getRequired() : false);
                    param.setDescription(apiParam.getDescription());
                    param.setExample(apiParam.getExample());
                    param.setEndpoint(savedEndpoint);
                    docParameterRepository.save(param);
                }
            }

            // Add success response example
            if (api.getResponseConfig() != null && api.getResponseConfig().getSuccessSchema() != null) {
                ResponseExampleEntity successExample = new ResponseExampleEntity();
                successExample.setStatusCode(200);
                successExample.setDescription("Successful response");
                successExample.setContentType(api.getResponseConfig().getContentType() != null ?
                        api.getResponseConfig().getContentType() : "application/json");
                successExample.setEndpoint(savedEndpoint);

                try {
                    Map<String, Object> exampleMap = objectMapper.readValue(
                            api.getResponseConfig().getSuccessSchema(), Map.class);
                    successExample.setExample(exampleMap);
                } catch (Exception e) {
                    log.warn("Failed to parse success response example: {}", e.getMessage());
                }

                responseExampleRepository.save(successExample);
            }

            // Add error response example
            if (api.getResponseConfig() != null && api.getResponseConfig().getErrorSchema() != null) {
                ResponseExampleEntity errorExample = new ResponseExampleEntity();
                errorExample.setStatusCode(400);
                errorExample.setDescription("Error response");
                errorExample.setContentType(api.getResponseConfig().getContentType() != null ?
                        api.getResponseConfig().getContentType() : "application/json");
                errorExample.setEndpoint(savedEndpoint);

                try {
                    Map<String, Object> exampleMap = objectMapper.readValue(
                            api.getResponseConfig().getErrorSchema(), Map.class);
                    errorExample.setExample(exampleMap);
                } catch (Exception e) {
                    log.warn("Failed to parse error response example: {}", e.getMessage());
                }

                responseExampleRepository.save(errorExample);
            }

            // Add code examples
            generateDocumentationCodeExamples(api, savedEndpoint, codeBaseRequestId);

            // Add changelog entry
            ChangelogEntryEntity changelog = new ChangelogEntryEntity();
            changelog.setVersion(api.getVersion());
            changelog.setDate(String.valueOf(LocalDateTime.now()));
            changelog.setType("ADDED");
            changelog.setAuthor(performedBy);
            changelog.setCollection(savedDocCollection);

            List<String> changes = new ArrayList<>();
            changes.add("Added endpoint: " + api.getApiName() + " (" + api.getHttpMethod() + ")");
            changes.add("Initial version of the API");
            changelog.setChanges(changes);

            changelogRepository.save(changelog);

            // Update endpoint count
            savedDocCollection.setTotalEndpoints(1);
            docCollectionRepository.save(savedDocCollection);

            log.info("Documentation generated successfully with Collection ID: {}", savedDocCollection.getId());
            return savedDocCollection.getId();

        } catch (Exception e) {
            log.error("Error generating Documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Documentation: " + e.getMessage(), e);
        }
    }

    private void generateDocumentationCodeExamples(GeneratedApiEntity api, APIEndpointEntity endpoint,
                                                   String codeBaseRequestId) {
        List<String> languages = Arrays.asList("curl", "javascript", "python", "java", "csharp", "php", "ruby", "go");

        for (String language : languages) {
            try {
                String code = generateCodeForLanguage(api, language);

                CodeExampleEntity codeExample = new CodeExampleEntity();
                codeExample.setLanguage(language);
                codeExample.setCode(code);
                codeExample.setDescription("Auto-generated " + language + " code example");
                codeExample.setEndpoint(endpoint);
                codeExample.setDefault(language.equals("curl"));

                codeExampleRepository.save(codeExample);
            } catch (Exception e) {
                log.warn("Failed to generate documentation code example for {}: {}", language, e.getMessage());
            }
        }
    }

    private String generateCodeForLanguage(GeneratedApiEntity api, String language) {
        String fullUrl = (api.getBasePath() != null ? api.getBasePath() : "") +
                (api.getEndpointPath() != null ? api.getEndpointPath() : "");
        String method = api.getHttpMethod().toLowerCase();

        switch (language) {
            case "curl":
                return generateCurlCode(api, fullUrl);
            case "javascript":
                return generateJavaScriptCode(api, fullUrl);
            case "python":
                return generatePythonCode(api, fullUrl);
            case "java":
                return generateJavaCode(api, fullUrl);
            case "csharp":
                return generateCSharpCode(api, fullUrl);
            case "php":
                return generatePhpCode(api, fullUrl);
            case "ruby":
                return generateRubyCode(api, fullUrl);
            case "go":
                return generateGoCode(api, fullUrl);
            default:
                return "// No code example available for " + language;
        }
    }

    private String generateCurlCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(api.getHttpMethod()).append(" \\\n");
        curl.append("  '").append(fullUrl).append("'");

        // Add headers
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            curl.append(" \\\n");
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    curl.append("  -H '").append(header).append(": YOUR_API_KEY'");
                    break;
                case "BEARER":
                case "JWT":
                    curl.append("  -H 'Authorization: Bearer YOUR_JWT_TOKEN'");
                    break;
                case "BASIC":
                    curl.append("  -u 'username:password'");
                    break;
                case "ORACLE_ROLES":
                    curl.append("  -H 'X-Oracle-Session: YOUR_SESSION_ID'");
                    break;
            }
        }

        // Add other headers
        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                    curl.append(" \\\n");
                    curl.append("  -H '").append(header.getKey()).append(": ").append(header.getValue()).append("'");
                }
            }
        }

        // Add body for non-GET requests
        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            curl.append(" \\\n");
            curl.append("  -d '").append(api.getRequestConfig().getSample().replace("'", "\\'")).append("'");
        }

        return curl.toString();
    }

    private String generateJavaScriptCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder js = new StringBuilder();
        js.append("// Auto-generated JavaScript code for ").append(api.getApiName()).append("\n\n");
        js.append("fetch('").append(fullUrl).append("', {\n");
        js.append("  method: '").append(api.getHttpMethod()).append("',\n");

        // Headers
        js.append("  headers: {\n");
        js.append("    'Content-Type': 'application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    js.append("    '").append(header).append("': 'YOUR_API_KEY',\n");
                    break;
                case "BEARER":
                case "JWT":
                    js.append("    'Authorization': 'Bearer YOUR_JWT_TOKEN',\n");
                    break;
                case "BASIC":
                    js.append("    'Authorization': 'Basic ' + btoa('username:password'),\n");
                    break;
                case "ORACLE_ROLES":
                    js.append("    'X-Oracle-Session': 'YOUR_SESSION_ID',\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                    js.append("    '").append(header.getKey()).append("': '").append(header.getValue()).append("',\n");
                }
            }
        }
        js.append("  },\n");

        // Body for non-GET requests
        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            js.append("  body: JSON.stringify(").append(api.getRequestConfig().getSample()).append(")\n");
        }

        js.append("})\n");
        js.append("  .then(response => response.json())\n");
        js.append("  .then(data => console.log(data))\n");
        js.append("  .catch(error => console.error('Error:', error));\n");

        return js.toString();
    }

    private String generatePythonCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder py = new StringBuilder();
        py.append("# Auto-generated Python code for ").append(api.getApiName()).append("\n\n");
        py.append("import requests\n\n");

        // Headers
        py.append("headers = {\n");
        py.append("    'Content-Type': 'application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    py.append("    '").append(header).append("': 'YOUR_API_KEY',\n");
                    break;
                case "BEARER":
                case "JWT":
                    py.append("    'Authorization': 'Bearer YOUR_JWT_TOKEN',\n");
                    break;
                case "BASIC":
                    py.append("    'Authorization': 'Basic base64_encoded_credentials',\n");
                    break;
                case "ORACLE_ROLES":
                    py.append("    'X-Oracle-Session': 'YOUR_SESSION_ID',\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                    py.append("    '").append(header.getKey()).append("': '").append(header.getValue()).append("',\n");
                }
            }
        }
        py.append("}\n\n");

        // Make request
        py.append("response = requests.").append(api.getHttpMethod().toLowerCase());
        py.append("(\n");
        py.append("    url='").append(fullUrl).append("',\n");
        py.append("    headers=headers");

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            py.append(",\n    json=").append(api.getRequestConfig().getSample());
        }
        py.append("\n)\n\n");

        py.append("print(response.json())\n");

        return py.toString();
    }

    private String generateJavaCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder java = new StringBuilder();
        java.append("// Auto-generated Java code for ").append(api.getApiName()).append("\n\n");
        java.append("import java.net.http.HttpClient;\n");
        java.append("import java.net.http.HttpRequest;\n");
        java.append("import java.net.http.HttpResponse;\n");
        java.append("import java.net.URI;\n\n");
        java.append("public class ").append(api.getApiCode()).append("Client {\n\n");
        java.append("    public static void main(String[] args) throws Exception {\n");
        java.append("        HttpClient client = HttpClient.newHttpClient();\n\n");

        // Build request
        java.append("        HttpRequest request = HttpRequest.newBuilder()\n");
        java.append("                .uri(URI.create(\"").append(fullUrl).append("\"))\n");
        java.append("                .method(\"").append(api.getHttpMethod()).append("\", HttpRequest.BodyPublishers.noBody())\n");

        // Headers
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    java.append("                .header(\"").append(header).append("\", \"YOUR_API_KEY\")\n");
                    break;
                case "BEARER":
                case "JWT":
                    java.append("                .header(\"Authorization\", \"Bearer YOUR_JWT_TOKEN\")\n");
                    break;
                case "BASIC":
                    java.append("                .header(\"Authorization\", \"Basic base64_encoded_credentials\")\n");
                    break;
                case "ORACLE_ROLES":
                    java.append("                .header(\"X-Oracle-Session\", \"YOUR_SESSION_ID\")\n");
                    break;
            }
        }

        java.append("                .header(\"Content-Type\", \"application/json\")\n");
        java.append("                .build();\n\n");

        java.append("        HttpResponse<String> response = client.send(request, \n");
        java.append("                HttpResponse.BodyHandlers.ofString());\n\n");
        java.append("        System.out.println(response.body());\n");
        java.append("    }\n");
        java.append("}\n");

        return java.toString();
    }

    private String generateCSharpCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder cs = new StringBuilder();
        cs.append("// Auto-generated C# code for ").append(api.getApiName()).append("\n\n");
        cs.append("using System;\n");
        cs.append("using System.Net.Http;\n");
        cs.append("using System.Threading.Tasks;\n\n");
        cs.append("class Program\n");
        cs.append("{\n");
        cs.append("    static async Task Main(string[] args)\n");
        cs.append("    {\n");
        cs.append("        using var client = new HttpClient();\n\n");

        // Headers
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    cs.append("        client.DefaultRequestHeaders.Add(\"").append(header).append("\", \"YOUR_API_KEY\");\n");
                    break;
                case "BEARER":
                case "JWT":
                    cs.append("        client.DefaultRequestHeaders.Add(\"Authorization\", \"Bearer YOUR_JWT_TOKEN\");\n");
                    break;
                case "BASIC":
                    cs.append("        client.DefaultRequestHeaders.Add(\"Authorization\", \"Basic base64_encoded_credentials\");\n");
                    break;
                case "ORACLE_ROLES":
                    cs.append("        client.DefaultRequestHeaders.Add(\"X-Oracle-Session\", \"YOUR_SESSION_ID\");\n");
                    break;
            }
        }

        cs.append("\n");
        cs.append("        var response = await client.").append(api.getHttpMethod()).append("Async(\"");
        cs.append(fullUrl).append("\");\n\n");
        cs.append("        var content = await response.Content.ReadAsStringAsync();\n");
        cs.append("        Console.WriteLine(content);\n");
        cs.append("    }\n");
        cs.append("}\n");

        return cs.toString();
    }

    private String generatePhpCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder php = new StringBuilder();
        php.append("<?php\n\n");
        php.append("// Auto-generated PHP code for ").append(api.getApiName()).append("\n\n");

        php.append("$ch = curl_init();\n\n");
        php.append("curl_setopt($ch, CURLOPT_URL, \"").append(fullUrl).append("\");\n");
        php.append("curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);\n");
        php.append("curl_setopt($ch, CURLOPT_CUSTOMREQUEST, \"").append(api.getHttpMethod()).append("\");\n\n");

        // Headers
        php.append("$headers = [\n");
        php.append("    'Content-Type: application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    php.append("    '").append(header).append(": YOUR_API_KEY',\n");
                    break;
                case "BEARER":
                case "JWT":
                    php.append("    'Authorization: Bearer YOUR_JWT_TOKEN',\n");
                    break;
                case "BASIC":
                    php.append("    'Authorization: Basic ' . base64_encode('username:password'),\n");
                    break;
                case "ORACLE_ROLES":
                    php.append("    'X-Oracle-Session: YOUR_SESSION_ID',\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                    php.append("    '").append(header.getKey()).append(": ").append(header.getValue()).append("',\n");
                }
            }
        }
        php.append("];\n");
        php.append("curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);\n\n");

        // Body for non-GET requests
        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            php.append("curl_setopt($ch, CURLOPT_POSTFIELDS, ").append(api.getRequestConfig().getSample()).append(");\n\n");
        }

        php.append("$response = curl_exec($ch);\n");
        php.append("curl_close($ch);\n\n");
        php.append("echo $response;\n");
        php.append("?>\n");

        return php.toString();
    }

    private String generateRubyCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder rb = new StringBuilder();
        rb.append("# Auto-generated Ruby code for ").append(api.getApiName()).append("\n\n");
        rb.append("require 'uri'\n");
        rb.append("require 'net/http'\n");
        rb.append("require 'json'\n\n");

        rb.append("uri = URI.parse('").append(fullUrl).append("')\n");
        rb.append("request = Net::HTTP::").append(api.getHttpMethod().charAt(0) +
                api.getHttpMethod().substring(1).toLowerCase()).append(".new(uri)\n\n");

        // Headers
        rb.append("request['Content-Type'] = 'application/json'\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    rb.append("request['").append(header).append("'] = 'YOUR_API_KEY'\n");
                    break;
                case "BEARER":
                case "JWT":
                    rb.append("request['Authorization'] = 'Bearer YOUR_JWT_TOKEN'\n");
                    break;
                case "BASIC":
                    rb.append("request.basic_auth('username', 'password')\n");
                    break;
                case "ORACLE_ROLES":
                    rb.append("request['X-Oracle-Session'] = 'YOUR_SESSION_ID'\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                    rb.append("request['").append(header.getKey()).append("'] = '").append(header.getValue()).append("'\n");
                }
            }
        }

        rb.append("\n");
        rb.append("response = Net::HTTP.start(uri.hostname, uri.port, use_ssl: uri.scheme == 'https') do |http|\n");
        rb.append("  http.request(request)\n");
        rb.append("end\n\n");
        rb.append("puts response.body\n");

        return rb.toString();
    }

    private String generateGoCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder go = new StringBuilder();
        go.append("// Auto-generated Go code for ").append(api.getApiName()).append("\n\n");
        go.append("package main\n\n");
        go.append("import (\n");
        go.append("    \"fmt\"\n");
        go.append("    \"io\"\n");
        go.append("    \"net/http\"\n");
        go.append("    \"strings\"\n");
        go.append(")\n\n");
        go.append("func main() {\n");

        // Body for non-GET requests
        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            go.append("    jsonBody := `").append(api.getRequestConfig().getSample()).append("`\n");
            go.append("    body := strings.NewReader(jsonBody)\n");
            go.append("    req, err := http.NewRequest(\"").append(api.getHttpMethod()).append("\", \"");
            go.append(fullUrl).append("\", body)\n");
        } else {
            go.append("    req, err := http.NewRequest(\"").append(api.getHttpMethod()).append("\", \"");
            go.append(fullUrl).append("\", nil)\n");
        }

        go.append("    if err != nil {\n");
        go.append("        fmt.Println(err)\n");
        go.append("        return\n");
        go.append("    }\n\n");

        // Headers
        go.append("    req.Header.Set(\"Content-Type\", \"application/json\")\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    go.append("    req.Header.Set(\"").append(header).append("\", \"YOUR_API_KEY\")\n");
                    break;
                case "BEARER":
                case "JWT":
                    go.append("    req.Header.Set(\"Authorization\", \"Bearer YOUR_JWT_TOKEN\")\n");
                    break;
                case "BASIC":
                    go.append("    req.SetBasicAuth(\"username\", \"password\")\n");
                    break;
                case "ORACLE_ROLES":
                    go.append("    req.Header.Set(\"X-Oracle-Session\", \"YOUR_SESSION_ID\")\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader())) {
                    go.append("    req.Header.Set(\"").append(header.getKey()).append("\", \"");
                    go.append(header.getValue()).append("\")\n");
                }
            }
        }

        go.append("\n    client := &http.Client{}\n");
        go.append("    resp, err := client.Do(req)\n");
        go.append("    if err != nil {\n");
        go.append("        fmt.Println(err)\n");
        go.append("        return\n");
        go.append("    }\n");
        go.append("    defer resp.Body.Close()\n\n");
        go.append("    body, err := io.ReadAll(resp.Body)\n");
        go.append("    if err != nil {\n");
        go.append("        fmt.Println(err)\n");
        go.append("        return\n");
        go.append("    }\n\n");
        go.append("    fmt.Println(string(body))\n");
        go.append("}\n");

        return go.toString();
    }

    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"};
        return colors[new Random().nextInt(colors.length)];
    }

    // ============================================================
    // Mapping Methods
    // ============================================================

    private ApiSchemaConfigEntity mapToSchemaConfigEntity(ApiSchemaConfigDTO dto, GeneratedApiEntity api) {
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
        ApiAuthConfigEntity entity = ApiAuthConfigEntity.builder()
                .generatedApi(api)
                .authType(dto.getAuthType())
                .apiKeyHeader(dto.getApiKeyHeader())
                .apiKeyValue(dto.getApiKeyValue())
                .apiKeySecret(dto.getApiKeySecret())
                .apiKeyLocation(dto.getApiKeyLocation())
                .apiKeyPrefix(dto.getApiKeyPrefix())
                .basicUsername(dto.getBasicUsername())
                .basicPassword(dto.getBasicPassword())
                .basicRealm(dto.getBasicRealm())
                .jwtSecret(dto.getJwtSecret())
                .jwtIssuer(dto.getJwtIssuer())
                .jwtAudience(dto.getJwtAudience())
                .jwtExpiration(dto.getJwtExpiration())
                .jwtAlgorithm(dto.getJwtAlgorithm())
                .oauthClientId(dto.getOauthClientId())
                .oauthClientSecret(dto.getOauthClientSecret())
                .oauthTokenUrl(dto.getOauthTokenUrl())
                .oauthAuthUrl(dto.getOauthAuthUrl())
                .oauthScopes(dto.getOauthScopes())
                .requiredRoles(dto.getRequiredRoles())
                .customAuthFunction(dto.getCustomAuthFunction())
                .validateSession(dto.getValidateSession())
                .checkObjectPrivileges(dto.getCheckObjectPrivileges())
                .ipWhitelist(dto.getIpWhitelist())
                .rateLimitRequests(dto.getRateLimitRequests())
                .rateLimitPeriod(dto.getRateLimitPeriod())
                .auditLevel(dto.getAuditLevel())
                .corsOrigins(dto.getCorsOrigins())
                .corsCredentials(dto.getCorsCredentials())
                .build();

        return entity;
    }

    private ApiRequestConfigEntity mapToRequestConfigEntity(ApiRequestConfigDTO dto, GeneratedApiEntity api) {
        try {
            return ApiRequestConfigEntity.builder()
                    .generatedApi(api)
                    .schemaType(dto.getSchemaType())
                    .sample(dto.getSample())
                    .maxSize(dto.getMaxSize())
                    .validateSchema(dto.getValidateSchema())
                    .allowedMediaTypes(dto.getAllowedMediaTypes() != null ?
                            objectMapper.writeValueAsString(dto.getAllowedMediaTypes()) : null)
                    .requiredFields(dto.getRequiredFields())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map request config: " + e.getMessage());
        }
    }

    private ApiResponseConfigEntity mapToResponseConfigEntity(ApiResponseConfigDTO dto, GeneratedApiEntity api) {
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
        try {
            return ApiSettingsEntity.builder()
                    .generatedApi(api)
                    .timeout(dto.getTimeout())
                    .maxRecords(dto.getMaxRecords())
                    .enableLogging(dto.getEnableLogging())
                    .logLevel(dto.getLogLevel())
                    .enableCaching(dto.getEnableCaching())
                    .cacheTtl(dto.getCacheTtl())
                    .enableRateLimiting(dto.getEnableRateLimiting())
                    .rateLimit(dto.getRateLimit())
                    .rateLimitPeriod(dto.getRateLimitPeriod())
                    .enableAudit(dto.getEnableAudit())
                    .auditLevel(dto.getAuditLevel())
                    .generateSwagger(dto.getGenerateSwagger())
                    .generatePostman(dto.getGeneratePostman())
                    .generateClientSdk(dto.getGenerateClientSdk())
                    .enableMonitoring(dto.getEnableMonitoring())
                    .enableAlerts(dto.getEnableAlerts())
                    .alertEmail(dto.getAlertEmail())
                    .enableTracing(dto.getEnableTracing())
                    .corsEnabled(dto.getCorsEnabled())
                    .corsOrigins(dto.getCorsOrigins() != null ?
                            objectMapper.writeValueAsString(dto.getCorsOrigins()) : null)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map settings: " + e.getMessage());
        }
    }

    private ApiParameterEntity mapToParameterEntity(ApiParameterDTO dto, GeneratedApiEntity api) {
        return ApiParameterEntity.builder()
                .generatedApi(api)
                .key(dto.getKey())
                .dbColumn(dto.getDbColumn())
                .dbParameter(dto.getDbParameter())
                .oracleType(dto.getOracleType())
                .apiType(dto.getApiType())
                .parameterType(dto.getParameterType())
                .required(dto.getRequired())
                .description(dto.getDescription())
                .example(dto.getExample())
                .validationPattern(dto.getValidationPattern())
                .defaultValue(dto.getDefaultValue())
                .position(dto.getPosition())
                .build();
    }

    private ApiResponseMappingEntity mapToResponseMappingEntity(ApiResponseMappingDTO dto, GeneratedApiEntity api) {
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
                .position(dto.getPosition())
                .build();
    }

    private ApiHeaderEntity mapToHeaderEntity(ApiHeaderDTO dto, GeneratedApiEntity api) {
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
        List<ApiTestEntity> tests = new ArrayList<>();

        // Unit test
        if (dto.getUnitTests() != null && !dto.getUnitTests().isEmpty()) {
            tests.add(ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName("Unit Tests")
                    .testType("UNIT")
                    .testData(dto.getUnitTests())
                    .build());
        }

        // Integration test
        if (dto.getIntegrationTests() != null && !dto.getIntegrationTests().isEmpty()) {
            tests.add(ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName("Integration Tests")
                    .testType("INTEGRATION")
                    .testData(dto.getIntegrationTests())
                    .build());
        }

        // Test data
        if (dto.getTestData() != null && !dto.getTestData().isEmpty()) {
            tests.add(ApiTestEntity.builder()
                    .generatedApi(api)
                    .testName("Test Data")
                    .testType("DATA")
                    .testData(dto.getTestData())
                    .build());
        }

        return tests;
    }

    private GeneratedApiResponseDTO mapToResponse(GeneratedApiEntity entity) {
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
                    .totalCalls(entity.getTotalCalls())
                    .lastCalledAt(entity.getLastCalledAt())
                    .tags(entity.getTags())
                    .parametersCount(entity.getParameters() != null ? entity.getParameters().size() : 0)
                    .responseMappingsCount(entity.getResponseMappings() != null ? entity.getResponseMappings().size() : 0)
                    .headersCount(entity.getHeaders() != null ? entity.getHeaders().size() : 0)
                    .build();

            // Map schema config
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

            // Map auth config
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
                        .corsOrigins(entity.getAuthConfig().getCorsOrigins())
                        .corsCredentials(entity.getAuthConfig().getCorsCredentials())
                        .build();
                response.setAuthConfig(authDto);
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to map response: " + e.getMessage());
        }
    }

    // ============================================================
    // Logging and Utility Methods
    // ============================================================

    private void logExecution(GeneratedApiEntity api, ExecuteApiRequestDTO request,
                              Object response, int status, long executionTime,
                              String performedBy, String clientIp, String userAgent,
                              String errorMessage) {
        try {
            ApiExecutionLogEntity log = ApiExecutionLogEntity.builder()
                    .generatedApi(api)
                    .requestId(request != null ? request.getRequestId() : UUID.randomUUID().toString())
                    .requestParams(request != null && request.getQueryParams() != null ?
                            objectMapper.writeValueAsString(request.getQueryParams()) : null)
                    .requestBody(request != null && request.getBody() != null ?
                            objectMapper.writeValueAsString(request.getBody()) : null)
                    .responseBody(response != null ?
                            objectMapper.writeValueAsString(response) : null)
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
                if (metadata.containsKey(field)) {
                    filtered.put(field, metadata.get(field));
                }
            }
            return filtered;
        }

        return metadata;
    }

    private Object formatResponse(GeneratedApiEntity api, Object data) {
        // Format response based on API configuration
        if (api.getResponseConfig() != null && Boolean.TRUE.equals(api.getResponseConfig().getIncludeMetadata())) {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("data", data);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timestamp", LocalDateTime.now().toString());
            metadata.put("apiVersion", api.getVersion());
            metadata.put("requestId", UUID.randomUUID().toString());

            if (api.getResponseConfig().getMetadataFields() != null) {
                // Include only specified metadata fields
                Map<String, Object> filteredMetadata = new HashMap<>();
                for (String field : api.getResponseConfig().getMetadataFields()) {
                    if (metadata.containsKey(field)) {
                        filteredMetadata.put(field, metadata.get(field));
                    }
                }
                formatted.put("metadata", filteredMetadata);
            } else {
                formatted.put("metadata", metadata);
            }

            return formatted;
        }

        return data;
    }

    private boolean compareResponses(ExecuteApiResponseDTO actual, Object expected) {
        // Simple comparison - in reality, you'd need more sophisticated comparison
        try {
            if (expected == null) return true;

            String actualJson = objectMapper.writeValueAsString(actual.getData());
            String expectedJson = objectMapper.writeValueAsString(expected);

            return actualJson.equals(expectedJson);

        } catch (Exception e) {
            return false;
        }
    }
}