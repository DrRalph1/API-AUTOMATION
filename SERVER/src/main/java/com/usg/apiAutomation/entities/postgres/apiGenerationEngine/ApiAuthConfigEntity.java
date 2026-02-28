package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_eng_auth_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuthConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne
    @JoinColumn(name = "api_id")
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
    private java.util.List<String> oauthScopes;

    // Oracle Roles fields
    @ElementCollection
    @CollectionTable(name = "tb_eng_required_roles", joinColumns = @JoinColumn(name = "auth_config_id"))
    @Column(name = "role_name")
    private java.util.List<String> requiredRoles;

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
}