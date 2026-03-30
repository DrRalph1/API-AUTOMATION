package com.usg.apiGeneration.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedAPIDTO {

    // Core API Information
    private String id;
    private String apiName;
    private String apiCode;
    private String description;
    private String version;
    private String status;
    private String httpMethod;
    private String basePath;
    private String endpointPath;
    private String fullEndpoint;
    private String category;
    private String owner;
    private Boolean isActive;
    private List<String> tags;

    // Collection & Folder Information
    private CollectionInfoDTO collectionInfo;

    private Long totalCalls;
    private LocalDateTime lastCalledAt;

    // Source Object Information
    private ApiSourceObjectDTO sourceObject;

    // Schema Configuration
    private ApiSchemaConfigDTO schemaConfig;

    // Authentication Configuration
    private ApiAuthConfigDTO authConfig;

    // Parameters (IN and IN/OUT parameters)
    private List<ParameterDTO> parameters;
    private Integer parametersCount;

    // Response Mappings (OUT and IN/OUT parameters)
    private List<ResponseMappingDTO> responseMappings;
    private Integer responseMappingsCount;

    // Headers Configuration
    private List<HeaderDTO> headers;
    private Integer headersCount;

    // Request Body Configuration
    private RequestBodyConfigDTO requestBody;

    // Response Body Configuration
    private ResponseBodyConfigDTO responseBody;

    // Database Tests Configuration
    private TestsConfigDTO tests;

    // Settings Configuration
    private ApiSettingsDTO settings;

    // Generated Files
    private Map<String, String> generatedFiles;

    // Validation Result
    private ValidationResultDTO validation;

    // Metadata
    private Map<String, Object> metadata;

    // Nested DTOs

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterDTO {
        private String id;
        private String key;
        private String dbColumn;
        private String oracleType;
        private String apiType;
        private String parameterLocation; // query, path, header, body
        private Boolean required;
        private String description;
        private String example;
        private String validationPattern;
        private String defaultValue;
        private Boolean inBody;
        private Boolean isPrimaryKey;
        private String paramMode; // IN, IN/OUT
        private String bodyFormat; // For body parameters
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseMappingDTO {
        private String id;
        private String apiField;
        private String dbColumn;
        private String oracleType;
        private String apiType;
        private String format; // date-time, etc.
        private Boolean nullable;
        private Boolean isPrimaryKey;
        private Boolean includeInResponse;
        private Boolean inResponse;
        private String paramMode; // OUT, IN/OUT
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeaderDTO {
        private String id;
        private String key;
        private String value;
        private Boolean required;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestBodyConfigDTO {
        private String bodyType; // none, json, xml, form-data, urlencoded, raw
        private String sample;
        private List<String> requiredFields;
        private Boolean validateSchema;
        private Integer maxSize; // bytes
        private List<String> allowedMediaTypes;
        private String contentType; // For request
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseBodyConfigDTO {
        private String successSchema;
        private String errorSchema;
        private Boolean includeMetadata;
        private List<String> metadataFields;
        private String contentType;
        private String compression; // gzip, deflate, none
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestsConfigDTO {
        // Database connectivity tests
        private Boolean testConnection;
        private Boolean testObjectAccess;
        private Boolean testPrivileges;

        // Data validation tests
        private Boolean testDataTypes;
        private Boolean testNullConstraints;
        private Boolean testUniqueConstraints;
        private Boolean testForeignKeyReferences;

        // Performance tests
        private Boolean testQueryPerformance;
        private Integer performanceThreshold; // ms
        private Boolean testWithSampleData;
        private Integer sampleDataRows;

        // PL/SQL specific tests
        private Boolean testProcedureExecution;
        private Boolean testFunctionReturn;
        private Boolean testExceptionHandling;

        // Security tests
        private Boolean testSQLInjection;
        private Boolean testAuthentication;
        private Boolean testAuthorization;

        private Map<String, Object> testData;
        private List<String> testQueries;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResultDTO {
        private Boolean valid;
        private String message;
        private List<String> errors;
        private List<String> warnings;
    }

    // Helper method to create a request for API generation
    public static GeneratedAPIDTO fromGeneratedApiResponse(GeneratedApiResponseDTO response) {
        if (response == null) return null;

        return GeneratedAPIDTO.builder()
                .id(response.getId())
                .apiName(response.getApiName())
                .apiCode(response.getApiCode())
                .description(response.getDescription())
                .version(response.getVersion())
                .status(response.getStatus())
                .httpMethod(response.getHttpMethod())
                .basePath(response.getBasePath())
                .endpointPath(response.getEndpointPath())
                .fullEndpoint(response.getFullEndpoint())
                .category(response.getCategory())
                .owner(response.getOwner())
                .isActive(response.getIsActive())
                .tags(response.getTags())
                .schemaConfig(response.getSchemaConfig())
                .authConfig(response.getAuthConfig())
                .parametersCount(response.getParametersCount())
                .responseMappingsCount(response.getResponseMappingsCount())
                .headersCount(response.getHeadersCount())
                .generatedFiles(response.getGeneratedFiles())
                .metadata(response.getMetadata())
                .build();
    }
}