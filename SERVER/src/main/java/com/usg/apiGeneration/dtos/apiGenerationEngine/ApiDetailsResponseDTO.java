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
public class ApiDetailsResponseDTO {
    // Basic Info
    private String id;
    private String requestId;

    // API Details Tab (exactly as captured)
    private String apiName;
    private String apiCode;
    private String description;
    private String version;
    private String status;
    private String httpMethod;
    private String basePath;
    private String endpointPath;
    private String category;
    private String owner;
    private Map<String, Object> validation;
    private Map<String, Object> responseExamples;
    private String apiDetails;
    private List<String> tags;

    // Collection & Folder Info (exactly as captured)
    private CollectionInfoDTO collectionInfo;

    // Schema Configuration (exactly as captured)
    private ApiSchemaConfigDTO schemaConfig;

    // Parameters Tab (exactly as captured)
    private List<ApiParameterDTO> parameters;

    // Response Mappings Tab (exactly as captured)
    private List<ApiResponseMappingDTO> responseMappings;

    // Authentication Tab (exactly as captured)
    private ApiAuthConfigDTO authConfig;

    // Request Tab (exactly as captured)
    private ApiRequestConfigDTO requestBody;

    // Response Tab (exactly as captured)
    private ApiResponseConfigDTO responseBody;

    // Headers (exactly as captured)
    private List<ApiHeaderDTO> headers;

    // Database Tests Tab (exactly as captured)
    private ApiTestsDTO tests;

    // Settings Tab (exactly as captured)
    private ApiSettingsDTO settings;

    // Source Object Info (exactly as captured)
    private Map<String, Object> sourceObject;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
    private Long totalCalls;
    private LocalDateTime lastCalledAt;

    // Related components info
    private Map<String, Object> metadata;
    private Map<String, String> generatedFiles;
}