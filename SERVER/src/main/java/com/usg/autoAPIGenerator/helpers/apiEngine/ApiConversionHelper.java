package com.usg.autoAPIGenerator.helpers.apiEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.services.schemaBrowser.OracleSchemaService;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.CustomQueryParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiConversionHelper {

    public ApiSourceObjectDTO convertAndValidateSourceObject(Map<String, Object> sourceObjectMap,
                                                             OracleSchemaService oracleSchemaService,
                                                             ObjectMapper objectMapper) {
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

    public ApiSourceObjectDTO extractSourceObject(GeneratedApiEntity api, ObjectMapper objectMapper) {
        if (api.getSourceObjectInfo() == null || api.getSourceObjectInfo().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.convertValue(api.getSourceObjectInfo(), ApiSourceObjectDTO.class);
        } catch (Exception e) {
            log.warn("Failed to parse source object info: {}", e.getMessage());
            return null;
        }
    }

    public CollectionInfoDTO extractCollectionInfo(GeneratedApiEntity api, ObjectMapper objectMapper) {
        if (api.getCollectionInfo() == null || api.getCollectionInfo().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.convertValue(api.getCollectionInfo(), CollectionInfoDTO.class);
        } catch (Exception e) {
            log.warn("Failed to parse collection info: {}", e.getMessage());
            return null;
        }
    }

    public GeneratedApiResponseDTO mapToResponse(GeneratedApiEntity entity) {
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

            if (entity.getAuthConfig() != null) {
                response.setAuthConfig(mapAuthConfigToDTO(entity.getAuthConfig()));
            }

            if (entity.getSchemaConfig() != null) {
                response.setSchemaConfig(mapSchemaConfigToDTO(entity.getSchemaConfig()));
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to map response: " + e.getMessage(), e);
        }
    }

    public ApiDetailsResponseDTO mapToApiDetailsResponse(GeneratedApiEntity entity, ObjectMapper objectMapper) {
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

        return response;
    }

    public ExecuteApiRequestDTO createExecuteRequest(ApiTestRequestDTO testRequest, String requestId) {
        return ExecuteApiRequestDTO.builder()
                .pathParams(testRequest.getPathParams())
                .queryParams(testRequest.getQueryParams())
                .headers(testRequest.getHeaders())
                .body(testRequest.getBody())
                .requestId(requestId)
                .build();
    }

    public GenerateApiRequestDTO convertEntityToRequestDTO(GeneratedApiEntity api, ObjectMapper objectMapper) {
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
            request.setSchemaConfig(mapSchemaConfigToDTO(api.getSchemaConfig()));
        }

        if (api.getAuthConfig() != null) {
            request.setAuthConfig(mapAuthConfigToDTO(api.getAuthConfig()));
        }

        if (api.getRequestConfig() != null) {
            request.setRequestBody(mapRequestConfigToDTO(api.getRequestConfig()));
        }

        if (api.getResponseConfig() != null) {
            request.setResponseBody(mapResponseConfigToDTO(api.getResponseConfig()));
        }

        if (api.getSettings() != null) {
            request.setSettings(mapSettingsToDTO(api.getSettings()));
        }

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterDTO> paramDTOs = api.getParameters().stream()
                    .map(this::mapParameterToDTO)
                    .collect(Collectors.toList());
            request.setParameters(paramDTOs);
        }

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingDTO> mappingDTOs = api.getResponseMappings().stream()
                    .map(this::mapResponseMappingToDTO)
                    .collect(Collectors.toList());
            request.setResponseMappings(mappingDTOs);
        }

        if (api.getHeaders() != null && !api.getHeaders().isEmpty()) {
            List<ApiHeaderDTO> headerDTOs = api.getHeaders().stream()
                    .map(this::mapHeaderToDTO)
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

    public List<ApiParameterDTO> convertParametersToDTOs(List<ApiParameterEntity> parameters) {
        if (parameters == null) return new ArrayList<>();
        return parameters.stream()
                .map(this::mapParameterToDTO)
                .collect(Collectors.toList());
    }

    public ApiExecutionLogDTO mapToExecutionLogDTO(ApiExecutionLogEntity entity) {
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

    public ApiTestResultDTO mapToTestResultDTO(ApiTestEntity entity) {
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

    // Private mapping methods

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

    private ApiRequestConfigDTO mapRequestConfigToDTO(ApiRequestConfigEntity entity) {
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
                .bodyType(entity.getBodyType())
                .sample(entity.getSample())
                .maxSize(entity.getMaxSize())
                .validateSchema(entity.getValidateSchema())
                .allowedMediaTypes(allowedMediaTypes)
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

    private ApiParameterDTO mapParameterToDTO(ApiParameterEntity entity) {
        if (entity == null) return null;

        return ApiParameterDTO.builder()
                .id(entity.getId())
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


    public GeneratedAPIDTO mapToGeneratedAPIDTO(GeneratedApiEntity entity, ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }

        try {
            return GeneratedAPIDTO.builder()
                    // Core API Information
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
                    .isActive(entity.getIsActive())
                    .tags(entity.getTags() != null ? entity.getTags() : new ArrayList<>())

                    // Collection & Folder Information
                    .collectionInfo(extractCollectionInfo(entity, objectMapper))

                    // Source Object Information
                    .sourceObject(extractSourceObject(entity, objectMapper))

                    // Schema Configuration
                    .schemaConfig(mapSchemaConfigToDTO(entity.getSchemaConfig()))

                    // Authentication Configuration - FIXED to include both key and secret
                    .authConfig(mapAuthConfigToGeneratedAPIDTO(entity.getAuthConfig()))

                    // Parameters
                    .parameters(mapParameterDTOs(entity.getParameters()))
                    .parametersCount(entity.getParameters() != null ? entity.getParameters().size() : 0)

                    // Response Mappings
                    .responseMappings(mapResponseMappingDTOs(entity.getResponseMappings()))
                    .responseMappingsCount(entity.getResponseMappings() != null ? entity.getResponseMappings().size() : 0)

                    // Headers
                    .headers(mapHeaderDTOs(entity.getHeaders()))
                    .headersCount(entity.getHeaders() != null ? entity.getHeaders().size() : 0)

                    // Request Body
                    .requestBody(mapToRequestBodyConfigDTO(entity.getRequestConfig()))

                    // Response Body
                    .responseBody(mapToResponseBodyConfigDTO(entity.getResponseConfig()))

                    // Tests - Aggregate all test configurations
                    .tests(mapToTestsConfigDTO(entity.getTests()))

                    // Settings
                    .settings(mapSettingsToDTO(entity.getSettings()))

                    // Generated Files
                    .generatedFiles(new HashMap<>())

                    // Validation Result
                    .validation(extractValidationResult(entity))

                    // With this:
                    .metadata(new HashMap<String, Object>() {{
                        put("sourceRequestId", entity.getSourceRequestId());
                        put("createdAt", entity.getCreatedAt());
                        put("updatedAt", entity.getUpdatedAt());
                        put("createdBy", entity.getCreatedBy());
                        put("updatedBy", entity.getUpdatedBy());
                    }})

                    .build();

        } catch (Exception e) {
            log.error("Error mapping to GeneratedAPIDTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map to GeneratedAPIDTO: " + e.getMessage(), e);
        }
    }


    private List<GeneratedAPIDTO.ParameterDTO> mapParameterDTOs(List<ApiParameterEntity> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return new ArrayList<>();
        }

        return parameters.stream()
                .sorted(Comparator.comparing(ApiParameterEntity::getPosition,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(param -> {
                    ApiParameterDTO dto = mapParameterToDTO(param);
                    if (dto == null) return null;

                    return GeneratedAPIDTO.ParameterDTO.builder()
                            .id(dto.getId())
                            .key(dto.getKey())
                            .dbColumn(dto.getDbColumn())
                            .oracleType(dto.getOracleType())
                            .apiType(dto.getApiType())
                            .parameterLocation(dto.getParameterLocation())
                            .required(dto.getRequired())
                            .description(dto.getDescription())
                            .example(dto.getExample())
                            .validationPattern(dto.getValidationPattern())
                            .defaultValue(dto.getDefaultValue())
                            .inBody(dto.getInBody())
                            .isPrimaryKey(dto.getIsPrimaryKey())
                            .paramMode(dto.getParamMode())
                            .bodyFormat(null) // Set based on request config if needed
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<GeneratedAPIDTO.ResponseMappingDTO> mapResponseMappingDTOs(List<ApiResponseMappingEntity> responseMappings) {
        if (responseMappings == null || responseMappings.isEmpty()) {
            return new ArrayList<>();
        }

        return responseMappings.stream()
                .sorted(Comparator.comparing(ApiResponseMappingEntity::getPosition,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(mapping -> {
                    ApiResponseMappingDTO dto = mapResponseMappingToDTO(mapping);
                    if (dto == null) return null;

                    return GeneratedAPIDTO.ResponseMappingDTO.builder()
                            .id(mapping.getId())
                            .apiField(dto.getApiField())
                            .dbColumn(dto.getDbColumn())
                            .oracleType(dto.getOracleType())
                            .apiType(dto.getApiType())
                            .format(dto.getFormat())
                            .nullable(dto.getNullable())
                            .isPrimaryKey(dto.getIsPrimaryKey())
                            .includeInResponse(dto.getIncludeInResponse())
                            .inResponse(dto.getInResponse())
                            .paramMode(null)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<GeneratedAPIDTO.HeaderDTO> mapHeaderDTOs(List<ApiHeaderEntity> headers) {
        if (headers == null || headers.isEmpty()) {
            return new ArrayList<>();
        }

        return headers.stream()
                .map(header -> {
                    ApiHeaderDTO dto = mapHeaderToDTO(header);
                    if (dto == null) return null;

                    return GeneratedAPIDTO.HeaderDTO.builder()
                            .id(header.getId())
                            .key(dto.getKey())
                            .value(dto.getValue())
                            .required(dto.getRequired())
                            .description(dto.getDescription())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private GeneratedAPIDTO.RequestBodyConfigDTO mapToRequestBodyConfigDTO(ApiRequestConfigEntity requestConfig) {
        if (requestConfig == null) return null;

        ApiRequestConfigDTO dto = mapRequestConfigToDTO(requestConfig);
        if (dto == null) return null;

        return GeneratedAPIDTO.RequestBodyConfigDTO.builder()
                .bodyType(dto.getBodyType())
                .sample(dto.getSample())
                .requiredFields(dto.getRequiredFields())
                .validateSchema(dto.getValidateSchema())
                .maxSize(dto.getMaxSize() != null ? dto.getMaxSize().intValue() : null)
                .allowedMediaTypes(dto.getAllowedMediaTypes())
                .contentType(null) // Not in request config, can be derived from bodyType
                .build();
    }

    private GeneratedAPIDTO.ResponseBodyConfigDTO mapToResponseBodyConfigDTO(ApiResponseConfigEntity responseConfig) {
        if (responseConfig == null) return null;

        ApiResponseConfigDTO dto = mapResponseConfigToDTO(responseConfig);
        if (dto == null) return null;

        return GeneratedAPIDTO.ResponseBodyConfigDTO.builder()
                .successSchema(dto.getSuccessSchema())
                .errorSchema(dto.getErrorSchema())
                .includeMetadata(dto.getIncludeMetadata())
                .metadataFields(dto.getMetadataFields())
                .contentType(dto.getContentType())
                .compression(dto.getCompression())
                .build();
    }

    private GeneratedAPIDTO.TestsConfigDTO mapToTestsConfigDTO(List<ApiTestEntity> tests) {
        if (tests == null || tests.isEmpty()) {
            return GeneratedAPIDTO.TestsConfigDTO.builder().build();
        }

        // Use the first test entity as the source of truth for test configuration
        ApiTestEntity testEntity = tests.get(0);

        return GeneratedAPIDTO.TestsConfigDTO.builder()
                // Database connectivity tests
                .testConnection(testEntity.getTestConnection())
                .testObjectAccess(testEntity.getTestObjectAccess())
                .testPrivileges(testEntity.getTestPrivileges())

                // Data validation tests
                .testDataTypes(testEntity.getTestDataTypes())
                .testNullConstraints(testEntity.getTestNullConstraints())
                .testUniqueConstraints(testEntity.getTestUniqueConstraints())
                .testForeignKeyReferences(testEntity.getTestForeignKeyReferences())

                // Performance tests
                .testQueryPerformance(testEntity.getTestQueryPerformance())
                .performanceThreshold(testEntity.getPerformanceThreshold())
                .testWithSampleData(testEntity.getTestWithSampleData())
                .sampleDataRows(testEntity.getSampleDataRows())

                // PL/SQL specific tests
                .testProcedureExecution(testEntity.getTestProcedureExecution())
                .testFunctionReturn(testEntity.getTestFunctionReturn())
                .testExceptionHandling(testEntity.getTestExceptionHandling())

                // Security tests
                .testSQLInjection(testEntity.getTestSQLInjection())
                .testAuthentication(testEntity.getTestAuthentication())
                .testAuthorization(testEntity.getTestAuthorization())

                // Test data and queries
                .testData(testEntity.getTestData())
                .testQueries(testEntity.getTestQueries())

                .build();
    }

    private GeneratedAPIDTO.ValidationResultDTO extractValidationResult(GeneratedApiEntity entity) {
        if (entity.getSourceObjectInfo() != null && entity.getSourceObjectInfo().containsKey("validation")) {
            try {
                Object validationObj = entity.getSourceObjectInfo().get("validation");
                if (validationObj instanceof Map) {
                    Map<String, Object> validationMap = (Map<String, Object>) validationObj;
                    return GeneratedAPIDTO.ValidationResultDTO.builder()
                            .valid((Boolean) validationMap.getOrDefault("valid", true))
                            .message((String) validationMap.getOrDefault("message", ""))
                            .errors((List<String>) validationMap.getOrDefault("errors", new ArrayList<>()))
                            .warnings((List<String>) validationMap.getOrDefault("warnings", new ArrayList<>()))
                            .build();
                }
            } catch (Exception e) {
                log.warn("Failed to extract validation result: {}", e.getMessage());
            }
        }

        // If no validation info in source object, check tests
        if (entity.getTests() != null && !entity.getTests().isEmpty()) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            for (ApiTestEntity test : entity.getTests()) {
                if ("FAILED".equals(test.getStatus())) {
                    errors.add("Test '" + test.getTestName() + "' failed");
                } else if (test.getActualResponse() != null && test.getExpectedResponse() != null) {
                    Map<String, Object> differences = test.getResponseDifferences();
                    if (!differences.isEmpty()) {
                        warnings.add("Test '" + test.getTestName() + "' has response differences");
                    }
                }
            }

            return GeneratedAPIDTO.ValidationResultDTO.builder()
                    .valid(errors.isEmpty())
                    .message(errors.isEmpty() ? "All tests passed" : "Some tests failed")
                    .errors(errors)
                    .warnings(warnings)
                    .build();
        }

        // Default
        return GeneratedAPIDTO.ValidationResultDTO.builder()
                .valid(true)
                .message("No validation results")
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
    }


    // Add this new helper method for auth config mapping
    private ApiAuthConfigDTO mapAuthConfigToGeneratedAPIDTO(ApiAuthConfigEntity entity) {
        if (entity == null) return null;

        return ApiAuthConfigDTO.builder()
                .authType(entity.getAuthType())
                .apiKeyHeader(entity.getApiKeyHeader())
                .apiKeyValue(entity.getApiKeyValue())
                .apiKeySecret(entity.getApiKeySecret())
                .apiSecretHeader(entity.getApiSecretHeader())
                .apiSecretValue(entity.getApiSecretValue())
                .apiKeyLocation(entity.getApiKeyLocation())
                .apiKeyPrefix(entity.getApiKeyPrefix())
                .basicUsername(entity.getBasicUsername())
                .basicPassword(entity.getBasicPassword())
                .basicRealm(entity.getBasicRealm())
                .jwtSecret(entity.getJwtSecret())
                .jwtIssuer(entity.getJwtIssuer())
                .jwtToken(entity.getJwtToken())
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


    /**
     * Create Auth Config Entity from DTO
     */
    public ApiAuthConfigEntity createAuthConfigEntity(ApiAuthConfigDTO authConfigDTO) {
        if (authConfigDTO == null) return null;

        ApiAuthConfigEntity entity = new ApiAuthConfigEntity();

        entity.setAuthType(authConfigDTO.getAuthType());

        // API Key fields
        entity.setApiKeyHeader(authConfigDTO.getApiKeyHeader());
        entity.setApiKeyValue(authConfigDTO.getApiKeyValue());
        entity.setApiKeySecret(authConfigDTO.getApiKeySecret());
        entity.setApiSecretHeader(authConfigDTO.getApiSecretHeader());
        entity.setApiSecretValue(authConfigDTO.getApiSecretValue());
        entity.setApiKeyLocation(authConfigDTO.getApiKeyLocation());
        entity.setApiKeyPrefix(authConfigDTO.getApiKeyPrefix());

        // Basic Auth fields
        entity.setBasicUsername(authConfigDTO.getBasicUsername());
        entity.setBasicPassword(authConfigDTO.getBasicPassword());
        entity.setBasicRealm(authConfigDTO.getBasicRealm());

        // JWT fields
        entity.setJwtToken(authConfigDTO.getJwtToken());
        entity.setJwtSecret(authConfigDTO.getJwtSecret());
        entity.setJwtIssuer(authConfigDTO.getJwtIssuer());
        entity.setJwtAudience(authConfigDTO.getJwtAudience());
        entity.setJwtExpiration(authConfigDTO.getJwtExpiration());
        entity.setJwtAlgorithm(authConfigDTO.getJwtAlgorithm());

        // OAuth2 fields
        entity.setOauthClientId(authConfigDTO.getOauthClientId());
        entity.setOauthClientSecret(authConfigDTO.getOauthClientSecret());
        entity.setOauthTokenUrl(authConfigDTO.getOauthTokenUrl());
        entity.setOauthAuthUrl(authConfigDTO.getOauthAuthUrl());
        entity.setOauthScopes(authConfigDTO.getOauthScopes());

        // Oracle Roles fields
        entity.setRequiredRoles(authConfigDTO.getRequiredRoles());

        // Other fields
        entity.setCustomAuthFunction(authConfigDTO.getCustomAuthFunction());
        entity.setValidateSession(authConfigDTO.getValidateSession());
        entity.setCheckObjectPrivileges(authConfigDTO.getCheckObjectPrivileges());
        entity.setIpWhitelist(authConfigDTO.getIpWhitelist());
        entity.setRateLimitRequests(authConfigDTO.getRateLimitRequests());
        entity.setRateLimitPeriod(authConfigDTO.getRateLimitPeriod());
        entity.setEnableRateLimiting(authConfigDTO.getEnableRateLimiting());
        entity.setAuditLevel(authConfigDTO.getAuditLevel());

        // CORS fields
        if (authConfigDTO.getCorsOrigins() != null && !authConfigDTO.getCorsOrigins().isEmpty()) {
            entity.setCorsOrigins(String.join(",", authConfigDTO.getCorsOrigins()));
        }
        entity.setCorsCredentials(authConfigDTO.getCorsCredentials());

        return entity;
    }

    /**
     * Create Request Config Entity from DTO
     */
    public ApiRequestConfigEntity createRequestConfigEntity(ApiRequestConfigDTO requestConfigDTO) {
        if (requestConfigDTO == null) return null;

        ApiRequestConfigEntity entity = new ApiRequestConfigEntity();
        entity.setBodyType(requestConfigDTO.getBodyType());
        entity.setSample(requestConfigDTO.getSample());
        entity.setMaxSize(requestConfigDTO.getMaxSize());
        entity.setValidateSchema(requestConfigDTO.getValidateSchema());

        if (requestConfigDTO.getAllowedMediaTypes() != null && !requestConfigDTO.getAllowedMediaTypes().isEmpty()) {
            entity.setAllowedMediaTypes(String.join(",", requestConfigDTO.getAllowedMediaTypes()));
        }

        entity.setRequiredFields(requestConfigDTO.getRequiredFields());

        return entity;
    }

    /**
     * Create Response Config Entity from DTO
     */
    public ApiResponseConfigEntity createResponseConfigEntity(ApiResponseConfigDTO responseConfigDTO) {
        if (responseConfigDTO == null) return null;

        ApiResponseConfigEntity entity = new ApiResponseConfigEntity();
        entity.setSuccessSchema(responseConfigDTO.getSuccessSchema());
        entity.setErrorSchema(responseConfigDTO.getErrorSchema());
        entity.setIncludeMetadata(responseConfigDTO.getIncludeMetadata());
        entity.setMetadataFields(responseConfigDTO.getMetadataFields());
        entity.setContentType(responseConfigDTO.getContentType());
        entity.setCompression(responseConfigDTO.getCompression());

        return entity;
    }

    /**
     * Create Schema Config Entity from Source Object DTO
     */
    public ApiSchemaConfigEntity createSchemaConfigEntity(ApiSourceObjectDTO sourceObjectDTO) {
        if (sourceObjectDTO == null) return null;

        ApiSchemaConfigEntity entity = new ApiSchemaConfigEntity();
        entity.setSchemaName(sourceObjectDTO.getOwner());
        entity.setObjectType(sourceObjectDTO.getObjectType());
        entity.setObjectName(sourceObjectDTO.getObjectName());
        entity.setOperation(sourceObjectDTO.getOperation());

        // Set default values
        entity.setEnablePagination(true);
        entity.setPageSize(10);
        entity.setEnableSorting(true);
        entity.setDefaultSortDirection("ASC");
        entity.setIsSynonym(sourceObjectDTO.getIsSynonym() != null ? sourceObjectDTO.getIsSynonym() : false);
        entity.setTargetType(sourceObjectDTO.getTargetType());
        entity.setTargetName(sourceObjectDTO.getTargetName());
        entity.setTargetOwner(sourceObjectDTO.getTargetOwner());

        return entity;
    }

    /**
     * Create Settings Entity from DTO
     */
    public ApiSettingsEntity createSettingsEntity(ApiSettingsDTO settingsDTO) {
        if (settingsDTO == null) return null;

        ApiSettingsEntity entity = new ApiSettingsEntity();
        entity.setTimeout(settingsDTO.getTimeout());
        entity.setMaxRecords(settingsDTO.getMaxRecords());
        entity.setEnableLogging(settingsDTO.getEnableLogging());
        entity.setLogLevel(settingsDTO.getLogLevel());
        entity.setEnableCaching(settingsDTO.getEnableCaching());
        entity.setCacheTtl(settingsDTO.getCacheTtl());
        entity.setEnableRateLimiting(settingsDTO.getEnableRateLimiting());
        entity.setRateLimit(settingsDTO.getRateLimit());
        entity.setRateLimitPeriod(settingsDTO.getRateLimitPeriod());
        entity.setEnableAudit(settingsDTO.getEnableAudit());
        entity.setAuditLevel(settingsDTO.getAuditLevel());
        entity.setGenerateSwagger(settingsDTO.getGenerateSwagger());
        entity.setGeneratePostman(settingsDTO.getGeneratePostman());
        entity.setGenerateClientSDK(settingsDTO.getGenerateClientSDK());
        entity.setEnableMonitoring(settingsDTO.getEnableMonitoring());
        entity.setEnableAlerts(settingsDTO.getEnableAlerts());
        entity.setAlertEmail(settingsDTO.getAlertEmail());
        entity.setEnableTracing(settingsDTO.getEnableTracing());
        entity.setCorsEnabled(settingsDTO.getCorsEnabled());

        if (settingsDTO.getCorsOrigins() != null && !settingsDTO.getCorsOrigins().isEmpty()) {
            entity.setCorsOrigins(String.join(",", settingsDTO.getCorsOrigins()));
        }

        return entity;
    }

    /**
     * Create Header Entities from DTOs
     */
    public List<ApiHeaderEntity> createHeaderEntities(List<ApiHeaderDTO> headerDTOs, GeneratedApiEntity api) {
        if (headerDTOs == null) return new ArrayList<>();

        List<ApiHeaderEntity> headers = new ArrayList<>();
        for (ApiHeaderDTO dto : headerDTOs) {
            ApiHeaderEntity entity = ApiHeaderEntity.builder()
                    .key(dto.getKey())
                    .value(dto.getValue())
                    .required(dto.getRequired())
                    .description(dto.getDescription())
                    .isRequestHeader(dto.getIsRequestHeader() != null ? dto.getIsRequestHeader() : true)
                    .isResponseHeader(dto.getIsResponseHeader() != null ? dto.getIsResponseHeader() : false)
                    .build();

            // Set the bidirectional relationship
            entity.setGeneratedApi(api);
            headers.add(entity);
        }

        return headers;
    }

    /**
     * Create Response Mapping Entities from DTOs
     */
    public List<ApiResponseMappingEntity> createResponseMappingEntities(
            List<ApiResponseMappingDTO> mappingDTOs, String generatedApiId) {
        if (mappingDTOs == null) return new ArrayList<>();

        List<ApiResponseMappingEntity> mappings = new ArrayList<>();
        for (ApiResponseMappingDTO dto : mappingDTOs) {
            ApiResponseMappingEntity entity = new ApiResponseMappingEntity();
            entity.setGeneratedApi(null); // Will be set by caller
            entity.setApiField(dto.getApiField());
            entity.setDbColumn(dto.getDbColumn());
            entity.setOracleType(dto.getOracleType());
            entity.setApiType(dto.getApiType());
            entity.setFormat(dto.getFormat());
            entity.setNullable(dto.getNullable());
            entity.setIsPrimaryKey(dto.getIsPrimaryKey());
            entity.setIncludeInResponse(dto.getIncludeInResponse());
            entity.setInResponse(dto.getInResponse());
            entity.setPosition(dto.getPosition());
            mappings.add(entity);
        }

        return mappings;
    }


    /**
     * Convert and validate source object that might contain a custom SELECT statement
     */
    public ApiSourceObjectDTO convertAndValidateSourceObjectForCustomQuery(Map<String, Object> sourceObject,
                                                                           ObjectMapper objectMapper,
                                                                           String databaseType,
                                                                           CustomQueryParserUtil queryParserUtil) {
        ApiSourceObjectDTO dto = convertAndValidateSourceObject(sourceObject, null, objectMapper);

        // Check if this is a custom query
        if (sourceObject.containsKey("customSelectStatement")) {
            String customQuery = (String) sourceObject.get("customSelectStatement");

            if (customQuery != null && !customQuery.trim().isEmpty()) {
                dto.setCustomSelectStatement(customQuery);
                dto.setObjectType("CUSTOM_QUERY");
                dto.setOperation("SELECT");

                // Parse the query to extract metadata
                Map<String, Object> parsedMetadata = queryParserUtil.parseCustomQuery(customQuery, databaseType);

                // Set extracted metadata
                dto.setQueryColumns((List<QueryColumnDTO>) parsedMetadata.get("columns"));
                dto.setSourceTables((List<String>) parsedMetadata.get("sourceTables"));
                dto.setFromClause((String) parsedMetadata.get("fromClause"));
                dto.setWhereClause((String) parsedMetadata.get("whereClause"));
                dto.setGroupByClause((String) parsedMetadata.get("groupByClause"));
                dto.setHavingClause((String) parsedMetadata.get("havingClause"));
                dto.setOrderByClause((String) parsedMetadata.get("orderByClause"));
                dto.setColumnCount((Integer) parsedMetadata.get("columnCount"));
                dto.setParameterCount(((List<QueryParameterDTO>) parsedMetadata.get("parameters")).size());
                dto.setQueryAlias((String) parsedMetadata.get("queryAlias"));
                dto.setIsDynamicQuery((Boolean) parsedMetadata.get("hasParameters"));

                // Convert extracted parameters to ApiParameterDTO list
                List<ApiParameterDTO> extractedParams = convertQueryParametersToApiParameters(
                        (List<QueryParameterDTO>) parsedMetadata.get("parameters")
                );
                dto.setExtractedParameters(extractedParams);
            }
        }

        return dto;
    }

    /**
     * Convert QueryParameterDTO to ApiParameterDTO
     */
    private List<ApiParameterDTO> convertQueryParametersToApiParameters(List<QueryParameterDTO> queryParams) {
        if (queryParams == null) return new ArrayList<>();

        return queryParams.stream()
                .map(qp -> ApiParameterDTO.builder()
                        .key(qp.getParameterName())
                        .parameterType(qp.getDataType())
                        .parameterLocation("query") // Default to query parameter
                        .required(qp.getIsRequired())
                        .description(qp.getDescription())
                        .defaultValue(qp.getDefaultValue())
                        .build())
                .collect(Collectors.toList());
    }



    // Add these methods to your existing ApiConversionHelper.java

    /**
     * Map GeneratedApiEntity to GeneratedApiResponseDTO with protocol fields
     */
    public GeneratedApiResponseDTO mapToResponseWithProtocol(GeneratedApiEntity entity) {
        GeneratedApiResponseDTO response = mapToResponse(entity);

        if (response != null && entity != null) {
            // Add protocol-specific fields to metadata
            Map<String, Object> metadata = response.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }

            // Add protocol type
            metadata.put("protocolType", entity.getProtocolType());

            // Add SOAP config if present
            if (entity.getSoapConfig() != null) {
                metadata.put("soapConfig", entity.getSoapConfig());
            }

            // Add GraphQL config if present
            if (entity.getGraphqlConfig() != null) {
                metadata.put("graphqlConfig", entity.getGraphqlConfig());
            }

            // Add custom query info
            if (entity.getIsCustomQuery() != null && entity.getIsCustomQuery()) {
                metadata.put("isCustomQuery", true);
                metadata.put("customSelectStatement", entity.getCustomSelectStatement());
            }

            // Add file upload config
            if (entity.getFileUploadConfig() != null) {
                metadata.put("fileUploadConfig", entity.getFileUploadConfig());
            }

            // Add database type
            metadata.put("databaseType", entity.getDatabaseType());

            response.setMetadata(metadata);
        }

        return response;
    }

    /**
     * Map GeneratedApiEntity to GeneratedAPIDTO with protocol fields
     */
    public GeneratedAPIDTO mapToGeneratedAPIDTOWithProtocol(GeneratedApiEntity entity, ObjectMapper objectMapper) {
        GeneratedAPIDTO dto = mapToGeneratedAPIDTO(entity, objectMapper);

        if (dto != null && entity != null) {
            Map<String, Object> metadata = dto.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }

            metadata.put("protocolType", entity.getProtocolType());

            if (entity.getSoapConfig() != null) {
                metadata.put("soapConfig", entity.getSoapConfig());
            }

            if (entity.getGraphqlConfig() != null) {
                metadata.put("graphqlConfig", entity.getGraphqlConfig());
            }

            if (entity.getIsCustomQuery() != null && entity.getIsCustomQuery()) {
                metadata.put("isCustomQuery", true);
                metadata.put("customSelectStatement", entity.getCustomSelectStatement());
            }

            if (entity.getFileUploadConfig() != null) {
                metadata.put("fileUploadConfig", entity.getFileUploadConfig());
            }

            metadata.put("databaseType", entity.getDatabaseType());

            dto.setMetadata(metadata);
        }

        return dto;
    }



}