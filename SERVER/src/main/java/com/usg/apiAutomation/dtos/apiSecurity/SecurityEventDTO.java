package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventDTO {
    // Core fields
    private String id;
    private String type;
    private String severity;
    private String sourceIp;
    private String endpoint;
    private String method;
    private String message;
    private String timestamp;

    // User information
    private String userId;
    private String userName;
    private String sessionId;

    // Status information
    private String status; // BLOCKED, ALLOWED, FLAGGED, MONITORED
    private Integer statusCode; // HTTP status code
    private String reason; // Reason for blocking/flagging

    // Resolution fields
    private boolean resolved;
    private String resolvedBy;
    private String resolvedAt;
    private String resolutionNotes;

    // Attack information
    private String attackType; // SQL_INJECTION, XSS, CSRF, BRUTE_FORCE, etc.
    private String ruleId; // Security rule that was triggered
    private String ruleName;
    private String category; // AUTH, RATE_LIMIT, SQL_INJECTION, etc.

    // Request details
    private String requestUri;
    private Map<String, String> requestHeaders;
    private Map<String, String> queryParams;
    private String requestBody; // Truncated/sanitized
    private String payload; // Malicious payload if applicable

    // Response details
    private Integer responseSize;
    private Long responseTime;
    private Map<String, String> responseHeaders;

    // Location information
    private String country;
    private String city;
    private String region;
    private Double latitude;
    private Double longitude;
    private String userAgent;
    private String referer;

    // Rate limiting info
    private Integer rateLimit;
    private Integer rateLimitRemaining;
    private String rateLimitPolicy;

    // Counters
    private Integer attemptCount; // Number of attempts from this IP
    private String firstSeen;
    private String lastSeen;

    // Related events
    private String relatedEventId;
    private String correlationId; // To group related events

    // Additional metadata
    private Map<String, Object> metadata;
    private String environment; // dev, test, prod
    private String application; // Application name

    // Audit fields
    private String createdAt;
    private String updatedAt;
    private String createdBy;
}