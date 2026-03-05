package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuthConfigDTO {

    // Auth Type (none, apiKey, bearer, basic, oauth2, jwt, custom)
    private String authType;

    // ========================
    // API Key Configuration
    // ========================
    private String apiKeyHeader;
    private String apiKeyValue;
    private String apiKeySecret;
    private String apiKeyLocation;
    private String apiKeyPrefix;

    // Optional separate secret header/value (from second DTO)
    private String apiSecretHeader;
    private String apiSecretValue;

    // ========================
    // Basic Authentication
    // ========================
    private String basicUsername;
    private String basicPassword;
    private String basicRealm;

    // ========================
    // JWT / Bearer Configuration
    // ========================
    private String jwtToken;
    private String jwtSecret;
    private String jwtIssuer;
    private String jwtAudience;
    private Long jwtExpiration;
    private String jwtAlgorithm;

    // ========================
    // OAuth2 Configuration
    // ========================
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthTokenUrl;
    private String oauthAuthUrl;
    private List<String> oauthScopes;

    // ========================
    // Role / Custom Auth
    // ========================
    private List<String> requiredRoles;
    private String customAuthFunction;
    private Boolean validateSession;
    private Boolean checkObjectPrivileges;

    // ========================
    // Security Settings
    // ========================
    private String ipWhitelist;

    // Rate limiting
    private Integer rateLimitRequests;
    private String rateLimitPeriod;
    private Boolean enableRateLimiting;

    // CORS
    private List<String> corsOrigins;
    private Boolean corsCredentials;

    // Auditing
    private String auditLevel;
}