package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tb_eng_generated_apis")
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

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
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
}