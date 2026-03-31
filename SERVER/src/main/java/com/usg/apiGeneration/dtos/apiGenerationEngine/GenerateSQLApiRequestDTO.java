package com.usg.apiGeneration.dtos.apiGenerationEngine;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GenerateSQLApiRequestDTO {
    // API metadata
    private String apiCode;
    private String apiName;
    private String description;
    private String version;
    private String databaseType; // oracle, postgresql, mysql, etc.

    // SQL content
    private String sql; // The SQL statement to be executed

    // Collection/Folder info
    private String collectionId;
    private String collectionName;
    private String folderId;
    private String folderName;

    // Optional: schema to use if not specified in SQL
    private String defaultSchema;

    // Security settings
    private String authType; // NONE, API_KEY, BEARER, etc.
    private Map<String, Object> authConfig;

    // Rate limiting
    private Boolean enableRateLimiting;
    private Integer rateLimit;

    // Tags for categorization
    private List<String> tags;
    private String category;
}