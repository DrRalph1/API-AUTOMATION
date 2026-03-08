package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class DashboardSearchResultDTO {
    // Core fields
    private String id;
    private String title;
    private String type; // Collection, Endpoint, User, Documentation, etc.
    private String description;
    private String url;
    private String subtitle;

    // Additional IDs for relationships
    private String collectionId;
    private String folderId;
    private String parentId;

    // Ownership and timestamps
    private String owner;
    private String createdAt;
    private String updatedAt;

    // Status and metadata
    private String status; // active, inactive, draft, published
    private String version;
    private String method; // For endpoints: GET, POST, etc.

    // Tags for categorization
    private List<String> tags;

    // Counts for collections/folders
    private Integer itemCount; // Number of items in collection/folder
    private Integer requestCount; // Number of requests/endpoints

    // Additional metadata
    private Map<String, Object> metadata;

    // Icon or color for UI display
    private String icon;
    private String color;

    // Match information
    private String matchedField; // Which field matched the search query
    private Float relevanceScore; // Search relevance score

    // Action buttons/links
    private Map<String, String> actions; // Custom actions for this result type

    // Breadcrumb navigation
    private List<Map<String, String>> breadcrumbs; // For nested items
}