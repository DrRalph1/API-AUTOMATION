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
    private String authType;

    // API Key
    private String apiKeyHeader;
    private String apiKeyValue;
    private String apiKeySecret;
    private String apiKeyLocation;
    private String apiKeyPrefix;

    // Basic Auth
    private String basicUsername;
    private String basicPassword;
    private String basicRealm;

    // JWT
    private String jwtSecret;
    private String jwtIssuer;
    private String jwtAudience;
    private Long jwtExpiration;
    private String jwtAlgorithm;

    // OAuth2
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthTokenUrl;
    private String oauthAuthUrl;
    private List<String> oauthScopes;

    // Oracle Roles
    private List<String> requiredRoles;
    private String customAuthFunction;
    private Boolean validateSession;
    private Boolean checkObjectPrivileges;

    // Security Settings
    private String ipWhitelist;
    private Integer rateLimitRequests;
    private String rateLimitPeriod;
    private String auditLevel;
    private String corsOrigins;
    private Boolean corsCredentials;
}