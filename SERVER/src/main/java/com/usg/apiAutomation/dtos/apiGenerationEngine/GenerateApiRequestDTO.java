package com.usg.apiAutomation.dtos.apiGenerationEngine;

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
    private List<String> tags;

    // Schema Configuration
    private ApiSchemaConfigDTO schemaConfig;

    // Parameters
    private List<ApiParameterDTO> parameters;

    // Response Mappings
    private List<ApiResponseMappingDTO> responseMappings;

    // Authentication
    private ApiAuthConfigDTO authConfig;

    // Headers
    private List<ApiHeaderDTO> headers;

    // Request Body
    private ApiRequestConfigDTO requestBody;

    // Response Body
    private ApiResponseConfigDTO responseBody;

    // Tests
    private ApiTestsDTO tests;

    // Settings
    private ApiSettingsDTO settings;

    // Source Object Info
    private Map<String, Object> sourceObject;

    // Add this field for regeneration control
    private Boolean regenerateComponents;  // Flag to control component regeneration

    // Lombok's @Data will generate the getter automatically,
    // but if you want a custom getter, remove this method or implement it properly:

    // public Boolean getRegenerateComponents() {
    //     return regenerateComponents;
    // }

    // public void setRegenerateComponents(Boolean regenerateComponents) {
    //     this.regenerateComponents = regenerateComponents;
    // }
}