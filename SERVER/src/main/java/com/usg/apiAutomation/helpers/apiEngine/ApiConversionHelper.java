package com.usg.apiAutomation.helpers.apiEngine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.codeBase.RequestEntity;
import com.usg.apiAutomation.services.OracleSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

}