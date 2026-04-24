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
public class ApiRequestDTO {

    // =====================================================
    // Basic Request Information
    // =====================================================

    private String id;
    private String requestName;
    private String description;
    private String httpMethod;
    private String url;
    private String basePath;
    private String endpointPath;
    private Integer requestTimeoutSeconds;

    // =====================================================
    // Request Components
    // =====================================================

    private Map<String, Object> pathParameters;
    private Map<String, Object> queryParameters;
    private Map<String, String> headers;
    private Map<String, Object> requestBody;
    private Map<String, Object> formData;
    private Map<String, Object> multipartData;

    // =====================================================
    // RAW Request/Response Storage (NO MODIFICATION)
    // =====================================================

    private String rawRequestBody;      // Exact raw request body as received (XML/JSON)
    private String rawResponseBody;     // Exact raw response body as returned (XML/JSON)

    // =====================================================
    // Authentication
    // =====================================================

    private String authType; // BASIC, BEARER, API_KEY, OAUTH2, NONE
    private String authToken; // Will be encrypted/masked
    private String apiKey; // Will be encrypted/masked

    // =====================================================
    // Client Information
    // =====================================================

    private String clientIpAddress;
    private String userAgent;
    private String sourceApplication;
    private String requestedBy;

    // =====================================================
    // Tracking & Metadata
    // =====================================================

    private String correlationId;
    private Boolean isMockRequest;
    private Map<String, Object> metadata;
    private List<String> tags;

    // =====================================================
    // Control flags
    // =====================================================

    private Boolean executeImmediately;
    private Boolean saveRequest;
    private Boolean isEditing;
}