package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAPIRequest {
    private String objectType;
    private String objectName;
    private String connectionId;
    private String apiType; // REST, GraphQL, SOAP
    private Map<String, String> options;
    private String language; // Java, Python, JavaScript
    private String framework; // Spring Boot, Express, Flask
    private String outputFormat; // ZIP, GIT, DIRECTORY
    private Boolean includeSwagger;
    private Boolean includeTests;
    private Boolean includeSecurity;
}