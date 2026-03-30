package com.usg.apiGeneration.dtos.dashboard;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardSecurityEventDTO {
    // Core fields
    private String id;
    private String type;
    private String severity;
    private String sourceIp;
    private String endpoint;
    private String message;
    private String timestamp;

    // Additional fields that might be needed
    private String method; // HTTP method
    private String userId; // User who triggered the event
    private String userName;
    private String status; // BLOCKED, ALLOWED, FLAGGED
    private Integer statusCode; // HTTP status code

    // Resolution fields
    private boolean resolved;
    private String resolvedBy;
    private String resolvedAt;
    private String resolution; // How it was resolved

    // Event details
    private Map<String, Object> details; // Additional event-specific details
    private Map<String, Object> requestHeaders;
    private Map<String, Object> responseHeaders;

    // Attack information
    private String attackType; // SQL_INJECTION, XSS, CSRF, etc.
    private String payload; // The malicious payload
    private String ruleId; // Which security rule was triggered
    private String ruleName;

    // Location info
    private String country;
    private String city;
    private String userAgent;

    // Counters
    private Integer attemptCount; // Number of attempts
    private String firstSeen;
    private String lastSeen;

    // Audit fields
    private String createdAt;
    private String updatedAt;
    private String createdBy;
}