package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSettingsEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    private GeneratedApiEntity generatedApi;

    // =============================
    // Core Settings
    // =============================

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "max_records")
    private Integer maxRecords;

    // =============================
    // Logging & Monitoring
    // =============================

    @Column(name = "enable_logging")
    private Boolean enableLogging;

    @Column(name = "log_level")
    private String logLevel;

    @Column(name = "enable_monitoring")
    private Boolean enableMonitoring;

    @Column(name = "enable_tracing")
    private Boolean enableTracing;

    // =============================
    // Caching
    // =============================

    @Column(name = "enable_caching")
    private Boolean enableCaching;

    @Column(name = "cache_ttl")
    private Integer cacheTtl;

    // =============================
    // Rate Limiting
    // =============================

    @Column(name = "enable_rate_limiting")
    private Boolean enableRateLimiting;

    @Column(name = "rate_limit")
    private Integer rateLimit;

    @Column(name = "rate_limit_period")
    private String rateLimitPeriod;

    // =============================
    // Audit
    // =============================

    @Column(name = "enable_audit")
    private Boolean enableAudit;

    @Column(name = "audit_level")
    private String auditLevel;

    // =============================
    // Documentation Generation
    // =============================

    @Column(name = "generate_swagger")
    private Boolean generateSwagger;

    @Column(name = "generate_postman")
    private Boolean generatePostman;

    @Column(name = "generate_client_sdk")
    private Boolean generateClientSDK;

    // =============================
    // Alerts
    // =============================

    @Column(name = "enable_alerts")
    private Boolean enableAlerts;

    @Column(name = "alert_email")
    private String alertEmail;

    // =============================
    // CORS
    // =============================

    @Column(name = "cors_enabled")
    private Boolean corsEnabled;

    @Column(name = "cors_origins", columnDefinition = "text")
    private String corsOrigins;

    // =====================================================
    // equals & hashCode
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiSettingsEntity that)) return false;

        return Objects.equals(id, that.id) &&
                Objects.equals(timeout, that.timeout) &&
                Objects.equals(maxRecords, that.maxRecords) &&
                Objects.equals(enableLogging, that.enableLogging) &&
                Objects.equals(logLevel, that.logLevel) &&
                Objects.equals(enableCaching, that.enableCaching) &&
                Objects.equals(cacheTtl, that.cacheTtl) &&
                Objects.equals(enableRateLimiting, that.enableRateLimiting) &&
                Objects.equals(rateLimit, that.rateLimit) &&
                Objects.equals(rateLimitPeriod, that.rateLimitPeriod) &&
                Objects.equals(enableAudit, that.enableAudit) &&
                Objects.equals(auditLevel, that.auditLevel) &&
                Objects.equals(generateSwagger, that.generateSwagger) &&
                Objects.equals(generatePostman, that.generatePostman) &&
                Objects.equals(generateClientSDK, that.generateClientSDK) &&
                Objects.equals(enableMonitoring, that.enableMonitoring) &&
                Objects.equals(enableAlerts, that.enableAlerts) &&
                Objects.equals(alertEmail, that.alertEmail) &&
                Objects.equals(enableTracing, that.enableTracing) &&
                Objects.equals(corsEnabled, that.corsEnabled) &&
                Objects.equals(corsOrigins, that.corsOrigins) &&
                Objects.equals(
                        generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, timeout, maxRecords, enableLogging, logLevel,
                enableCaching, cacheTtl, enableRateLimiting, rateLimit,
                rateLimitPeriod, enableAudit, auditLevel, generateSwagger,
                generatePostman, generateClientSDK, enableMonitoring,
                enableAlerts, alertEmail, enableTracing, corsEnabled,
                corsOrigins,
                generatedApi != null ? generatedApi.getId() : null
        );
    }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        return "ApiSettingsEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", timeout=" + timeout +
                ", maxRecords=" + maxRecords +
                ", enableLogging=" + enableLogging +
                ", logLevel='" + logLevel + '\'' +
                ", enableCaching=" + enableCaching +
                ", cacheTtl=" + cacheTtl +
                ", enableRateLimiting=" + enableRateLimiting +
                ", rateLimit=" + rateLimit +
                ", rateLimitPeriod='" + rateLimitPeriod + '\'' +
                ", enableAudit=" + enableAudit +
                ", auditLevel='" + auditLevel + '\'' +
                ", generateSwagger=" + generateSwagger +
                ", generatePostman=" + generatePostman +
                ", generateClientSDK=" + generateClientSDK +
                ", enableMonitoring=" + enableMonitoring +
                ", enableAlerts=" + enableAlerts +
                ", alertEmail='" + maskEmail(alertEmail) + '\'' +
                ", enableTracing=" + enableTracing +
                ", corsEnabled=" + corsEnabled +
                ", corsOrigins='" + corsOrigins + '\'' +
                '}';
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) return null;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***@" + email.substring(atIndex + 1);
        return email.substring(0, 1) + "***" + email.substring(atIndex - 1);
    }

    // =====================================================
    // Builder Defaults
    // =====================================================

    public static class ApiSettingsEntityBuilder {
        private Integer timeout = 30000;
        private Integer maxRecords = 1000;
        private Boolean enableLogging = true;
        private String logLevel = "INFO";
        private Boolean enableCaching = false;
        private Integer cacheTtl = 3600;
        private Boolean enableRateLimiting = true;
        private Integer rateLimit = 100;
        private String rateLimitPeriod = "minute";
        private Boolean enableAudit = true;
        private String auditLevel = "BASIC";
        private Boolean generateSwagger = true;
        private Boolean generatePostman = true;
        private Boolean generateClientSDK = false;
        private Boolean enableMonitoring = true;
        private Boolean enableAlerts = false;
        private Boolean enableTracing = false;
        private Boolean corsEnabled = true;
        private String corsOrigins = "*";
    }
}