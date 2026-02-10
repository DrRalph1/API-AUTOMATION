package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDetailsResponse {
    private String id;
    private String name;
    private String method;
    private String url;
    private String description;
    private String collectionId;
    private String folderId;
    private String lastModified;
    private List<String> tags;
    private List<HeaderItem> headers;
    private Map<String, Object> body;
    private Map<String, Map<String, String>> implementations;
    private String requestGroupId;
    private String baseUrl;
    private Map<String, Object> responseExample;
    private List<ParameterItem> pathParameters;
}