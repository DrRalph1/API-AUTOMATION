package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    @EqualsAndHashCode.Exclude
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiSettingsEntity that = (ApiSettingsEntity) o;
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
                Objects.equals(generateClientSdk, that.generateClientSdk) &&
                Objects.equals(enableMonitoring, that.enableMonitoring) &&
                Objects.equals(enableAlerts, that.enableAlerts) &&
                Objects.equals(alertEmail, that.alertEmail) &&
                Objects.equals(enableTracing, that.enableTracing) &&
                Objects.equals(corsEnabled, that.corsEnabled) &&
                Objects.equals(corsOrigins, that.corsOrigins) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeout, maxRecords, enableLogging, logLevel,
                enableCaching, cacheTtl, enableRateLimiting, rateLimit,
                rateLimitPeriod, enableAudit, auditLevel, generateSwagger,
                generatePostman, generateClientSdk, enableMonitoring,
                enableAlerts, alertEmail, enableTracing, corsEnabled, corsOrigins,
                generatedApi != null ? generatedApi.getId() : null);
    }

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
                ", generateClientSdk=" + generateClientSdk +
                ", enableMonitoring=" + enableMonitoring +
                ", enableAlerts=" + enableAlerts +
                ", alertEmail='" + maskEmail(alertEmail) + '\'' +
                ", enableTracing=" + enableTracing +
                ", corsEnabled=" + corsEnabled +
                ", corsOrigins='" + corsOrigins + '\'' +
                '}';
    }

    /**
     * Helper method to mask email in toString()
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) return null;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***@" + email.substring(atIndex + 1);
        return email.substring(0, 1) + "***" + email.substring(atIndex - 1);
    }

    /**
     * Checks if logging is enabled
     */
    public boolean isLoggingEnabled() {
        return enableLogging != null && enableLogging;
    }

    /**
     * Checks if caching is enabled
     */
    public boolean isCachingEnabled() {
        return enableCaching != null && enableCaching;
    }

    /**
     * Gets cache TTL in seconds with default
     */
    public int getCacheTtlSeconds() {
        if (cacheTtl == null || cacheTtl <= 0) {
            return 3600; // Default 1 hour
        }
        return cacheTtl;
    }

    /**
     * Checks if rate limiting is enabled
     */
    public boolean isRateLimitingEnabled() {
        return enableRateLimiting != null && enableRateLimiting;
    }

    /**
     * Gets rate limit per period
     */
    public int getRateLimitValue() {
        if (rateLimit == null || rateLimit <= 0) {
            return 100; // Default 100 requests
        }
        return rateLimit;
    }

    /**
     * Gets rate limit period in seconds
     */
    public long getRateLimitPeriodSeconds() {
        if (rateLimitPeriod == null) {
            return 60; // Default 1 minute
        }

        return switch (rateLimitPeriod.toLowerCase()) {
            case "second" -> 1;
            case "minute" -> 60;
            case "hour" -> 3600;
            case "day" -> 86400;
            case "week" -> 604800;
            case "month" -> 2592000;
            default -> 60;
        };
    }

    /**
     * Checks if audit is enabled
     */
    public boolean isAuditEnabled() {
        return enableAudit != null && enableAudit;
    }

    /**
     * Checks if monitoring is enabled
     */
    public boolean isMonitoringEnabled() {
        return enableMonitoring != null && enableMonitoring;
    }

    /**
     * Checks if alerts are enabled
     */
    public boolean isAlertsEnabled() {
        return enableAlerts != null && enableAlerts;
    }

    /**
     * Gets alert email addresses as list
     */
    public List<String> getAlertEmailList() {
        if (alertEmail == null || alertEmail.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(alertEmail.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();
    }

    /**
     * Checks if tracing is enabled
     */
    public boolean isTracingEnabled() {
        return enableTracing != null && enableTracing;
    }

    /**
     * Checks if CORS is enabled
     */
    public boolean isCorsEnabled() {
        return corsEnabled != null && corsEnabled;
    }

    /**
     * Gets CORS origins as list
     */
    public List<String> getCorsOriginsList() {
        if (corsOrigins == null || corsOrigins.trim().isEmpty()) {
            return List.of("*"); // Default allow all
        }
        return Arrays.stream(corsOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    /**
     * Validates if all settings are consistent
     */
    public boolean validateSettings() {
        // Rate limiting validation
        if (isRateLimitingEnabled()) {
            if (rateLimit == null || rateLimit <= 0) return false;
            if (rateLimitPeriod == null || rateLimitPeriod.trim().isEmpty()) return false;
        }

        // CORS validation
        if (isCorsEnabled() && corsOrigins != null) {
            String[] origins = corsOrigins.split(",");
            for (String origin : origins) {
                if (!origin.trim().matches("^https?://.*|\\*$")) {
                    return false;
                }
            }
        }

        // Email validation for alerts
        if (isAlertsEnabled() && alertEmail != null) {
            String[] emails = alertEmail.split(",");
            for (String email : emails) {
                if (!email.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Builder with defaults
     */
    public static class ApiSettingsEntityBuilder {
        private Integer timeout = 30000; // Default 30 seconds
        private Integer maxRecords = 1000; // Default max records
        private Boolean enableLogging = true; // Default enable logging
        private String logLevel = "INFO"; // Default log level
        private Boolean enableCaching = false; // Default no caching
        private Integer cacheTtl = 3600; // Default 1 hour
        private Boolean enableRateLimiting = true; // Default enable rate limiting
        private Integer rateLimit = 100; // Default 100 requests
        private String rateLimitPeriod = "minute"; // Default per minute
        private Boolean enableAudit = true; // Default enable audit
        private String auditLevel = "BASIC"; // Default audit level
        private Boolean generateSwagger = true; // Default generate Swagger
        private Boolean generatePostman = true; // Default generate Postman
        private Boolean generateClientSdk = false; // Default no SDK generation
        private Boolean enableMonitoring = true; // Default enable monitoring
        private Boolean enableAlerts = false; // Default no alerts
        private Boolean enableTracing = false; // Default no tracing
        private Boolean corsEnabled = true; // Default enable CORS
        private String corsOrigins = "*"; // Default allow all origins
    }
}