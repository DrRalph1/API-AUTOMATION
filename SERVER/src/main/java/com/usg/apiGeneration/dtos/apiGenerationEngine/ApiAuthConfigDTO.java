package com.usg.apiGeneration.dtos.apiGenerationEngine;

import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.ApiAuthConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuthConfigDTO {

    private String id;
    private String apiId;

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

    // Metadata
    private Boolean isConfigured;
    private String lastUpdated;

    /**
     * Convert Entity to DTO
     */
    public static ApiAuthConfigDTO fromEntity(ApiAuthConfigEntity entity, String apiId) {
        if (entity == null) {
            return ApiAuthConfigDTO.builder()
                    .apiId(apiId)
                    .isConfigured(false)
                    .build();
        }

        // Convert comma-separated CORS origins to List
        List<String> corsOriginsList = null;
        if (entity.getCorsOrigins() != null && !entity.getCorsOrigins().isEmpty()) {
            corsOriginsList = Arrays.stream(entity.getCorsOrigins().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        return ApiAuthConfigDTO.builder()
                .id(entity.getId())
                .apiId(entity.getGeneratedApi() != null ? entity.getGeneratedApi().getId() : apiId)
                .authType(entity.getAuthType())

                // API Key fields
                .apiKeyHeader(entity.getApiKeyHeader())
                .apiKeyValue(entity.getApiKeyValue())
                .apiKeySecret(entity.getApiKeySecret())
                .apiKeyLocation(entity.getApiKeyLocation())
                .apiKeyPrefix(entity.getApiKeyPrefix())
                .apiSecretHeader(entity.getApiSecretHeader())
                .apiSecretValue(entity.getApiSecretValue())

                // Basic Auth fields
                .basicUsername(entity.getBasicUsername())
                .basicPassword(entity.getBasicPassword())
                .basicRealm(entity.getBasicRealm())

                // JWT fields
                .jwtToken(entity.getJwtToken())
                .jwtSecret(entity.getJwtSecret())
                .jwtIssuer(entity.getJwtIssuer())
                .jwtAudience(entity.getJwtAudience())
                .jwtExpiration(entity.getJwtExpiration())
                .jwtAlgorithm(entity.getJwtAlgorithm())

                // OAuth2 fields
                .oauthClientId(entity.getOauthClientId())
                .oauthClientSecret(entity.getOauthClientSecret())
                .oauthTokenUrl(entity.getOauthTokenUrl())
                .oauthAuthUrl(entity.getOauthAuthUrl())
                .oauthScopes(entity.getOauthScopes())

                // Role/Custom Auth fields
                .requiredRoles(entity.getRequiredRoles())
                .customAuthFunction(entity.getCustomAuthFunction())
                .validateSession(entity.getValidateSession())
                .checkObjectPrivileges(entity.getCheckObjectPrivileges())

                // Security Settings
                .ipWhitelist(entity.getIpWhitelist())
                .rateLimitRequests(entity.getRateLimitRequests())
                .rateLimitPeriod(entity.getRateLimitPeriod())
                .enableRateLimiting(entity.getEnableRateLimiting())
                .corsOrigins(corsOriginsList)
                .corsCredentials(entity.getCorsCredentials())
                .auditLevel(entity.getAuditLevel())

                // Metadata
                .isConfigured(true)
                .lastUpdated(new java.util.Date().toString())
                .build();
    }
}