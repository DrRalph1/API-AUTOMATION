package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tb_eng_generated_apis", indexes = {
        // Index for batch query by source_request_id (CRITICAL for dashboard performance)
        @Index(name = "idx_eng_api_source_request_id", columnList = "source_request_id"),

        // Index for API lookups
        @Index(name = "idx_eng_api_api_code", columnList = "api_code", unique = true),
        @Index(name = "idx_eng_api_status", columnList = "status"),
        @Index(name = "idx_eng_api_is_active", columnList = "is_active"),

        // Index for filtering by owner
        @Index(name = "idx_eng_api_owner", columnList = "owner"),

        // Index for sorting and filtering by timestamps
        @Index(name = "idx_eng_api_created_at", columnList = "created_at"),
        @Index(name = "idx_eng_api_updated_at", columnList = "updated_at"),
        @Index(name = "idx_eng_api_last_called_at", columnList = "last_called_at"),

        // Composite indexes for common query patterns
        @Index(name = "idx_eng_api_owner_status", columnList = "owner, status"),
        @Index(name = "idx_eng_api_active_owner", columnList = "is_active, owner"),
        @Index(name = "idx_eng_api_database_type", columnList = "database_type"),

        // Index for JSONB fields (PostgreSQL specific)
        @Index(name = "idx_eng_api_collection_info", columnList = "collection_info")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedApiEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    // =====================================================
    // Basic API Info
    // =====================================================

    @Column(name = "source_request_id")
    private String sourceRequestId;  // Store the original request ID

    @Column(name = "api_name", nullable = false)
    private String apiName;

    @Column(name = "api_code", nullable = false, unique = true)
    private String apiCode;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "version")
    private String version;

    @Column(name = "status")
    private String status;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "base_path")
    private String basePath;

    @Column(name = "endpoint_path")
    private String endpointPath;

    @Column(name = "category")
    private String category;

    @Column(name = "owner")
    private String owner;

    @Column(name = "database_type", length = 50)
    private String databaseType; // "oracle", "postgresql", "mysql", etc.

    // =====================================================
    // Audit & Metrics
    // =====================================================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "total_calls")
    private Long totalCalls;

    @Column(name = "last_called_at")
    private LocalDateTime lastCalledAt;

    // =====================================================
    // Tags
    // =====================================================

    @ElementCollection
    @CollectionTable(name = "tb_eng_tags", joinColumns = @JoinColumn(name = "api_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    // =====================================================
    // JSON Metadata
    // =====================================================

    @Type(JsonType.class)
    @Column(name = "source_object_info", columnDefinition = "jsonb")
    private Map<String, Object> sourceObjectInfo;

    @Type(JsonType.class)
    @Column(name = "collection_info", columnDefinition = "jsonb")
    private Map<String, Object> collectionInfo;

    // =====================================================
    // One-to-One Configurations
    // =====================================================

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private ApiSchemaConfigEntity schemaConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private ApiAuthConfigEntity authConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private ApiRequestConfigEntity requestConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private ApiResponseConfigEntity responseConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private ApiSettingsEntity settings;

    // =====================================================
    // One-to-Many Relationships
    // =====================================================

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("position ASC")
    @ToString.Exclude
    private List<ApiParameterEntity> parameters = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("position ASC")
    @ToString.Exclude
    private List<ApiResponseMappingEntity> responseMappings = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiHeaderEntity> headers = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<ApiTestEntity> tests = new ArrayList<>();

    // =====================================================
    // equals & hashCode
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneratedApiEntity that)) return false;

        return Objects.equals(id, that.id) &&
                Objects.equals(apiCode, that.apiCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, apiCode);
    }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        return "GeneratedApiEntity{" +
                "id='" + id + '\'' +
                ", apiName='" + apiName + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", basePath='" + basePath + '\'' +
                ", endpointPath='" + endpointPath + '\'' +
                ", category='" + category + '\'' +
                ", owner='" + owner + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isActive=" + isActive +
                ", totalCalls=" + totalCalls +
                ", lastCalledAt=" + lastCalledAt +
                '}';
    }

    // =====================================================
    // Collection Management Helper Methods
    // =====================================================

    // === Parameter Management ===

    public void addParameter(ApiParameterEntity parameter) {
        if (parameter == null) return;
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(parameter);
        parameter.setGeneratedApi(this);
    }

    public void removeParameter(ApiParameterEntity parameter) {
        if (parameter == null || this.parameters == null) return;
        this.parameters.remove(parameter);
        parameter.setGeneratedApi(null);
    }

    public void clearParameters() {
        if (this.parameters != null) {
            // Create a copy to avoid ConcurrentModificationException
            List<ApiParameterEntity> parametersToRemove = new ArrayList<>(this.parameters);
            for (ApiParameterEntity parameter : parametersToRemove) {
                removeParameter(parameter);
            }
        }
    }

    // === Header Management ===

    public void addHeader(ApiHeaderEntity header) {
        if (header == null) return;
        if (this.headers == null) {
            this.headers = new ArrayList<>();
        }
        this.headers.add(header);
        header.setGeneratedApi(this);
    }

    public void removeHeader(ApiHeaderEntity header) {
        if (header == null || this.headers == null) return;
        this.headers.remove(header);
        header.setGeneratedApi(null);
    }

    public void clearHeaders() {
        if (this.headers != null) {
            // Create a copy to avoid ConcurrentModificationException
            List<ApiHeaderEntity> headersToRemove = new ArrayList<>(this.headers);
            for (ApiHeaderEntity header : headersToRemove) {
                removeHeader(header);
            }
        }
    }

    // === Response Mapping Management ===

    public void addResponseMapping(ApiResponseMappingEntity responseMapping) {
        if (responseMapping == null) return;
        if (this.responseMappings == null) {
            this.responseMappings = new ArrayList<>();
        }
        this.responseMappings.add(responseMapping);
        responseMapping.setGeneratedApi(this);
    }

    public void removeResponseMapping(ApiResponseMappingEntity responseMapping) {
        if (responseMapping == null || this.responseMappings == null) return;
        this.responseMappings.remove(responseMapping);
        responseMapping.setGeneratedApi(null);
    }

    public void clearResponseMappings() {
        if (this.responseMappings != null) {
            List<ApiResponseMappingEntity> mappingsToRemove = new ArrayList<>(this.responseMappings);
            for (ApiResponseMappingEntity mapping : mappingsToRemove) {
                removeResponseMapping(mapping);
            }
        }
    }

    // === Test Management ===

    public void addTest(ApiTestEntity test) {
        if (test == null) return;
        if (this.tests == null) {
            this.tests = new ArrayList<>();
        }
        this.tests.add(test);
        test.setGeneratedApi(this);
    }

    public void removeTest(ApiTestEntity test) {
        if (test == null || this.tests == null) return;
        this.tests.remove(test);
        test.setGeneratedApi(null);
    }

    public void clearTests() {
        if (this.tests != null) {
            List<ApiTestEntity> testsToRemove = new ArrayList<>(this.tests);
            for (ApiTestEntity test : testsToRemove) {
                removeTest(test);
            }
        }
    }

    // === Tag Management ===

    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) return;
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        if (tag == null || this.tags == null) return;
        this.tags.remove(tag);
    }

    public void clearTags() {
        if (this.tags != null) {
            this.tags.clear();
        }
    }

    // === One-to-One Config Management ===

    public void setSchemaConfig(ApiSchemaConfigEntity schemaConfig) {
        if (this.schemaConfig != null && this.schemaConfig != schemaConfig) {
            this.schemaConfig.setGeneratedApi(null);
        }
        this.schemaConfig = schemaConfig;
        if (schemaConfig != null) {
            schemaConfig.setGeneratedApi(this);
        }
    }

    public void setAuthConfig(ApiAuthConfigEntity authConfig) {
        if (this.authConfig != null && this.authConfig != authConfig) {
            this.authConfig.setGeneratedApi(null);
        }
        this.authConfig = authConfig;
        if (authConfig != null) {
            authConfig.setGeneratedApi(this);
        }
    }

    public void setRequestConfig(ApiRequestConfigEntity requestConfig) {
        if (this.requestConfig != null && this.requestConfig != requestConfig) {
            this.requestConfig.setGeneratedApi(null);
        }
        this.requestConfig = requestConfig;
        if (requestConfig != null) {
            requestConfig.setGeneratedApi(this);
        }
    }

    public void setResponseConfig(ApiResponseConfigEntity responseConfig) {
        if (this.responseConfig != null && this.responseConfig != responseConfig) {
            this.responseConfig.setGeneratedApi(null);
        }
        this.responseConfig = responseConfig;
        if (responseConfig != null) {
            responseConfig.setGeneratedApi(this);
        }
    }

    public void setSettings(ApiSettingsEntity settings) {
        if (this.settings != null && this.settings != settings) {
            this.settings.setGeneratedApi(null);
        }
        this.settings = settings;
        if (settings != null) {
            settings.setGeneratedApi(this);
        }
    }

    // === Bulk Collection Management ===

    /**
     * Safely replace all parameters with a new list
     */
    public void replaceParameters(List<ApiParameterEntity> newParameters) {
        clearParameters();
        if (newParameters != null) {
            for (ApiParameterEntity parameter : newParameters) {
                addParameter(parameter);
            }
        }
    }

    /**
     * Safely replace all headers with a new list
     */
    public void replaceHeaders(List<ApiHeaderEntity> newHeaders) {
        clearHeaders();
        if (newHeaders != null) {
            for (ApiHeaderEntity header : newHeaders) {
                addHeader(header);
            }
        }
    }

    /**
     * Safely replace all response mappings with a new list
     */
    public void replaceResponseMappings(List<ApiResponseMappingEntity> newResponseMappings) {
        clearResponseMappings();
        if (newResponseMappings != null) {
            for (ApiResponseMappingEntity mapping : newResponseMappings) {
                addResponseMapping(mapping);
            }
        }
    }

    /**
     * Safely replace all tests with a new list
     */
    public void replaceTests(List<ApiTestEntity> newTests) {
        clearTests();
        if (newTests != null) {
            for (ApiTestEntity test : newTests) {
                addTest(test);
            }
        }
    }

    /**
     * Safely replace all tags with a new list
     */
    public void replaceTags(List<String> newTags) {
        clearTags();
        if (newTags != null) {
            for (String tag : newTags) {
                addTag(tag);
            }
        }
    }

    // === Utility Methods ===

    /**
     * Check if the API has any parameters
     */
    public boolean hasParameters() {
        return parameters != null && !parameters.isEmpty();
    }

    /**
     * Check if the API has any headers
     */
    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    /**
     * Get the count of parameters
     */
    public int getParameterCount() {
        return parameters != null ? parameters.size() : 0;
    }

    /**
     * Get the count of headers
     */
    public int getHeaderCount() {
        return headers != null ? headers.size() : 0;
    }

    /**
     * Find a parameter by its key
     */
    public Optional<ApiParameterEntity> findParameterByKey(String key) {
        if (key == null || parameters == null) return Optional.empty();
        return parameters.stream()
                .filter(p -> key.equals(p.getKey()))
                .findFirst();
    }

    /**
     * Find a header by its key
     */
    public Optional<ApiHeaderEntity> findHeaderByKey(String key) {
        if (key == null || headers == null) return Optional.empty();
        return headers.stream()
                .filter(h -> key.equals(h.getKey()))
                .findFirst();
    }

    /**
     * Check if the API requires authentication
     */
    public boolean requiresAuth() {
        return authConfig != null &&
                authConfig.getAuthType() != null &&
                !"NONE".equals(authConfig.getAuthType());
    }

    /**
     * Get the full endpoint URL
     */
    public String getFullEndpointUrl() {
        if (basePath == null && endpointPath == null) return "";
        if (basePath == null) return endpointPath;
        if (endpointPath == null) return basePath;

        String base = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        String endpoint = endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
        return base + endpoint;
    }

    // =====================================================
    // JPA Lifecycle Callbacks
    // =====================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (totalCalls == null) {
            totalCalls = 0L;
        }
        if (status == null) {
            status = "DRAFT";
        }
        if (version == null) {
            version = "1.0.0";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PreRemove
    protected void onRemove() {
        // Clear all bidirectional relationships
        clearParameters();
        clearHeaders();
        clearResponseMappings();
        clearTests();
        clearTags();

        // Clear one-to-one relationships
        setSchemaConfig(null);
        setAuthConfig(null);
        setRequestConfig(null);
        setResponseConfig(null);
        setSettings(null);
    }
}