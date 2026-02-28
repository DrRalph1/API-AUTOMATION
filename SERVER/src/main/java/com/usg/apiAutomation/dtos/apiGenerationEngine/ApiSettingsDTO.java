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
public class ApiSettingsDTO {
    private Integer timeout;
    private Integer maxRecords;
    private Boolean enableLogging;
    private String logLevel;
    private Boolean enableCaching;
    private Integer cacheTtl;
    private Boolean enableRateLimiting;
    private Integer rateLimit;
    private String rateLimitPeriod;
    private Boolean enableAudit;
    private String auditLevel;
    private Boolean generateSwagger;
    private Boolean generatePostman;
    private Boolean generateClientSdk;
    private Boolean enableMonitoring;
    private Boolean enableAlerts;
    private String alertEmail;
    private Boolean enableTracing;
    private Boolean corsEnabled;
    private List<String> corsOrigins;
}