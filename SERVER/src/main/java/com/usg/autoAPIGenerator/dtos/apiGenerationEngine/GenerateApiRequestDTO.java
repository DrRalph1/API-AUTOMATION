package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateApiRequestDTO {
    // API Details Tab
    private String apiName;
    private String apiCode;
    private String description;
    private String version;
    private String status; // DRAFT, ACTIVE, DEPRECATED, ARCHIVED
    private String httpMethod; // GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    private String basePath;
    private String endpointPath;
    private String category;
    private String owner;
    private Map<String, Object> validation;
    private Map<String, Object> responseExamples;
    private String apiDetails;
    private List<String> tags;

    private String databaseType; // "oracle", "postgresql", "mysql", etc.

    // Collection & Folder Info
    private CollectionInfoDTO collectionInfo;

    // Schema Configuration
    private ApiSchemaConfigDTO schemaConfig;

    // Parameters Tab
    private List<ApiParameterDTO> parameters;

    // Response Mappings Tab
    private List<ApiResponseMappingDTO> responseMappings;

    // Authentication Tab
    private ApiAuthConfigDTO authConfig;

    // Request Tab
    private ApiRequestConfigDTO requestBody;

    // Response Tab
    private ApiResponseConfigDTO responseBody;

    // Headers (part of Request/Response)
    private List<ApiHeaderDTO> headers;

    // Database Tests Tab
    private ApiTestsDTO tests;

    // Settings Tab
    private ApiSettingsDTO settings;

    // Source Object Info
    private Map<String, Object> sourceObject;

    // Control flags
    private Boolean regenerateComponents;
    private Boolean isEditing;

    // ============ NEW FIELDS FOR CUSTOM SELECT STATEMENTS (OPTIONAL) ============
    // These are optional and won't affect existing flow
    private String customSelectStatement;   // Custom SELECT query (alternative to sourceObject)
    private Boolean useCustomQuery;          // Flag to indicate using custom query

}