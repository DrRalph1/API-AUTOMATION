package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardNotificationDTO {
    // Core fields
    private String id;
    private String title;
    private String message;
    private String type; // info, success, warning, error
    private boolean read; // primitive boolean for isRead() method
    private String time;
    private String icon;
    private String actionUrl;

    // Additional fields that might be needed
    private String priority; // high, medium, low
    private String category; // security, user, system, api, etc.

    // Entity references
    private String entityId;
    private String entityType;

    // User references
    private String userId;
    private String username;

    // Timestamps
    private String createdAt;
    private String expiresAt;
    private String acknowledgedAt;

    // Actions
    private String dismissUrl;
    private String acknowledgeUrl;

    // Metadata
    private Object data; // Additional data associated with notification

    // Expiration
    private boolean expired;

    // For grouping
    private String groupId;
    private Integer groupCount;
}