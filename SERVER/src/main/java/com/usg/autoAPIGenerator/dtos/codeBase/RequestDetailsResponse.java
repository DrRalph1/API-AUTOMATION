package com.usg.autoAPIGenerator.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    // ============= ADD THESE FIELDS =============
    private String protocolType;           // "rest", "soap", "graphql"
    private String apiId;                  // API ID from generated API
    private String generatedApiId;         // Alias for apiId
    private Map<String, Object> soapConfig;     // SOAP configuration
    private Map<String, Object> graphqlConfig; // GraphQL configuration
    // ============= END =============
}