package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAPIResponse {
    private List<APIEndpointDto> endpoints;
    private String objectType;
    private String objectName;
    private String message;
    private Boolean success;
    private Map<String, Object> swaggerDefinition;
    private Map<String, Object> openApiDefinition;
    private List<String> generatedFiles;
    private String baseUrl;
    private String language;
    private String framework;
    private Long generationTimeMs;
    private String lastUpdated;

    public GenerateAPIResponse(List<APIEndpointDto> endpoints, String objectType,
                               String objectName, String message) {
        this.endpoints = endpoints;
        this.objectType = objectType;
        this.objectName = objectName;
        this.message = message;
        this.success = true;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}