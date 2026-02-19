package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIEndpointDto {
    private String id;
    private String name;
    private String method;
    private String url;
    private String description;
    private List<String> tags;
    private String lastModified;
    private boolean requiresAuth;
    private boolean deprecated;
    private String folder;
    private String collectionId;
    private Map<String, Object> examples; // curl, javascript, python, etc.
    private List<HeaderDto> headers;
    private String body;
}