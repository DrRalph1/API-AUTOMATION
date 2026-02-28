package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_eng_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSettingsEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "max_records")
    private Integer maxRecords;

    @Column(name = "enable_logging")
    private Boolean enableLogging;

    @Column(name = "log_level")
    private String logLevel;

    @Column(name = "enable_caching")
    private Boolean enableCaching;

    @Column(name = "cache_ttl")
    private Integer cacheTtl;

    @Column(name = "enable_rate_limiting")
    private Boolean enableRateLimiting;

    @Column(name = "rate_limit")
    private Integer rateLimit;

    @Column(name = "rate_limit_period")
    private String rateLimitPeriod;

    @Column(name = "enable_audit")
    private Boolean enableAudit;

    @Column(name = "audit_level")
    private String auditLevel;

    @Column(name = "generate_swagger")
    private Boolean generateSwagger;

    @Column(name = "generate_postman")
    private Boolean generatePostman;

    @Column(name = "generate_client_sdk")
    private Boolean generateClientSdk;

    @Column(name = "enable_monitoring")
    private Boolean enableMonitoring;

    @Column(name = "enable_alerts")
    private Boolean enableAlerts;

    @Column(name = "alert_email")
    private String alertEmail;

    @Column(name = "enable_tracing")
    private Boolean enableTracing;

    @Column(name = "cors_enabled")
    private Boolean corsEnabled;

    @Column(name = "cors_origins", columnDefinition = "text")
    private String corsOrigins;
}