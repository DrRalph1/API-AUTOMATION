package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_auth_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuthConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "auth_type")
    private String authType;

    // API Key fields
    @Column(name = "api_key_header")
    private String apiKeyHeader;

    @Column(name = "api_key_value")
    private String apiKeyValue;

    @Column(name = "api_key_secret")
    private String apiKeySecret;

    @Column(name = "api_key_location")
    private String apiKeyLocation;

    @Column(name = "api_key_prefix")
    private String apiKeyPrefix;

    // Basic Auth fields
    @Column(name = "basic_username")
    private String basicUsername;

    @Column(name = "basic_password")
    private String basicPassword;

    @Column(name = "basic_realm")
    private String basicRealm;

    // JWT fields
    @Column(name = "jwt_secret")
    private String jwtSecret;

    @Column(name = "jwt_issuer")
    private String jwtIssuer;

    @Column(name = "jwt_audience")
    private String jwtAudience;

    @Column(name = "jwt_expiration")
    private Long jwtExpiration;

    @Column(name = "jwt_algorithm")
    private String jwtAlgorithm;

    // OAuth2 fields
    @Column(name = "oauth_client_id")
    private String oauthClientId;

    @Column(name = "oauth_client_secret")
    private String oauthClientSecret;

    @Column(name = "oauth_token_url")
    private String oauthTokenUrl;

    @Column(name = "oauth_auth_url")
    private String oauthAuthUrl;

    @ElementCollection
    @CollectionTable(name = "tb_eng_oauth_scopes", joinColumns = @JoinColumn(name = "auth_config_id"))
    @Column(name = "scope")
    private List<String> oauthScopes;

    // Oracle Roles fields
    @ElementCollection
    @CollectionTable(name = "tb_eng_required_roles", joinColumns = @JoinColumn(name = "auth_config_id"))
    @Column(name = "role_name")
    private List<String> requiredRoles;

    @Column(name = "custom_auth_function")
    private String customAuthFunction;

    @Column(name = "validate_session")
    private Boolean validateSession;

    @Column(name = "check_object_privileges")
    private Boolean checkObjectPrivileges;

    // Security settings
    @Column(name = "ip_whitelist", columnDefinition = "text")
    private String ipWhitelist;

    @Column(name = "rate_limit_requests")
    private Integer rateLimitRequests;

    @Column(name = "rate_limit_period")
    private String rateLimitPeriod;

    @Column(name = "audit_level")
    private String auditLevel;

    @Column(name = "cors_origins", columnDefinition = "text")
    private String corsOrigins;

    @Column(name = "cors_credentials")
    private Boolean corsCredentials;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiAuthConfigEntity that = (ApiAuthConfigEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(authType, that.authType) &&
                Objects.equals(apiKeyHeader, that.apiKeyHeader) &&
                Objects.equals(apiKeyValue, that.apiKeyValue) &&
                Objects.equals(apiKeySecret, that.apiKeySecret) &&
                Objects.equals(apiKeyLocation, that.apiKeyLocation) &&
                Objects.equals(apiKeyPrefix, that.apiKeyPrefix) &&
                Objects.equals(basicUsername, that.basicUsername) &&
                Objects.equals(basicPassword, that.basicPassword) &&
                Objects.equals(basicRealm, that.basicRealm) &&
                Objects.equals(jwtSecret, that.jwtSecret) &&
                Objects.equals(jwtIssuer, that.jwtIssuer) &&
                Objects.equals(jwtAudience, that.jwtAudience) &&
                Objects.equals(jwtExpiration, that.jwtExpiration) &&
                Objects.equals(jwtAlgorithm, that.jwtAlgorithm) &&
                Objects.equals(oauthClientId, that.oauthClientId) &&
                Objects.equals(oauthClientSecret, that.oauthClientSecret) &&
                Objects.equals(oauthTokenUrl, that.oauthTokenUrl) &&
                Objects.equals(oauthAuthUrl, that.oauthAuthUrl) &&
                Objects.equals(oauthScopes, that.oauthScopes) &&
                Objects.equals(requiredRoles, that.requiredRoles) &&
                Objects.equals(customAuthFunction, that.customAuthFunction) &&
                Objects.equals(validateSession, that.validateSession) &&
                Objects.equals(checkObjectPrivileges, that.checkObjectPrivileges) &&
                Objects.equals(ipWhitelist, that.ipWhitelist) &&
                Objects.equals(rateLimitRequests, that.rateLimitRequests) &&
                Objects.equals(rateLimitPeriod, that.rateLimitPeriod) &&
                Objects.equals(auditLevel, that.auditLevel) &&
                Objects.equals(corsOrigins, that.corsOrigins) &&
                Objects.equals(corsCredentials, that.corsCredentials) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, authType, apiKeyHeader, apiKeyValue, apiKeySecret,
                apiKeyLocation, apiKeyPrefix, basicUsername, basicPassword,
                basicRealm, jwtSecret, jwtIssuer, jwtAudience, jwtExpiration,
                jwtAlgorithm, oauthClientId, oauthClientSecret, oauthTokenUrl,
                oauthAuthUrl, oauthScopes, requiredRoles, customAuthFunction,
                validateSession, checkObjectPrivileges, ipWhitelist,
                rateLimitRequests, rateLimitPeriod, auditLevel, corsOrigins,
                corsCredentials, generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiAuthConfigEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", authType='" + authType + '\'' +
                ", apiKeyHeader='" + maskSensitiveData(apiKeyHeader) + '\'' +
                ", apiKeyValue='" + maskSensitiveData(apiKeyValue) + '\'' +
                ", apiKeySecret='" + maskSensitiveData(apiKeySecret) + '\'' +
                ", apiKeyLocation='" + apiKeyLocation + '\'' +
                ", apiKeyPrefix='" + apiKeyPrefix + '\'' +
                ", basicUsername='" + basicUsername + '\'' +
                ", basicPassword='" + maskSensitiveData(basicPassword) + '\'' +
                ", basicRealm='" + basicRealm + '\'' +
                ", jwtSecret='" + maskSensitiveData(jwtSecret) + '\'' +
                ", jwtIssuer='" + jwtIssuer + '\'' +
                ", jwtAudience='" + jwtAudience + '\'' +
                ", jwtExpiration=" + jwtExpiration +
                ", jwtAlgorithm='" + jwtAlgorithm + '\'' +
                ", oauthClientId='" + maskSensitiveData(oauthClientId) + '\'' +
                ", oauthClientSecret='" + maskSensitiveData(oauthClientSecret) + '\'' +
                ", oauthTokenUrl='" + oauthTokenUrl + '\'' +
                ", oauthAuthUrl='" + oauthAuthUrl + '\'' +
                ", oauthScopes=" + oauthScopes +
                ", requiredRoles=" + requiredRoles +
                ", customAuthFunction='" + customAuthFunction + '\'' +
                ", validateSession=" + validateSession +
                ", checkObjectPrivileges=" + checkObjectPrivileges +
                ", ipWhitelist='" + ipWhitelist + '\'' +
                ", rateLimitRequests=" + rateLimitRequests +
                ", rateLimitPeriod='" + rateLimitPeriod + '\'' +
                ", auditLevel='" + auditLevel + '\'' +
                ", corsOrigins='" + corsOrigins + '\'' +
                ", corsCredentials=" + corsCredentials +
                '}';
    }

    /**
     * Helper method to mask sensitive data in toString()
     */
    private String maskSensitiveData(String value) {
        return value != null ? "********" : null;
    }
}