package com.usg.apiAutomation.dtos.apiGenerationEngine;

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
public class GeneratedApiResponseDTO {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private Boolean isActive;
    private Long totalCalls;
    private LocalDateTime lastCalledAt;
    private List<String> tags;

    // Configuration summaries
    private ApiSchemaConfigDTO schemaConfig;
    private ApiAuthConfigDTO authConfig;
    private Integer parametersCount;
    private Integer responseMappingsCount;
    private Integer headersCount;

    // Generated files
    private Map<String, String> generatedFiles;
    private Map<String, Object> metadata;
}