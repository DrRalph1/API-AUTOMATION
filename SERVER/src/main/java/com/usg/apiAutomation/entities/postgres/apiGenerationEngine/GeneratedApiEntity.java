package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ApiSchemaConfigEntity schemaConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ApiAuthConfigEntity authConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ApiRequestConfigEntity requestConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ApiResponseConfigEntity responseConfig;

    @OneToOne(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ApiSettingsEntity settings;

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ApiParameterEntity> parameters = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ApiResponseMappingEntity> responseMappings = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ApiHeaderEntity> headers = new ArrayList<>();

    @OneToMany(mappedBy = "generatedApi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ApiTestEntity> tests = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_eng_tags", joinColumns = @JoinColumn(name = "api_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "source_object_info", columnDefinition = "jsonb")
    private Map<String, Object> sourceObjectInfo;

    /**
     * Collection information from the frontend
     * Stores details about which collection and folder this API belongs to
     */
    @Type(JsonType.class)
    @Column(name = "collection_info", columnDefinition = "jsonb")
    private Map<String, Object> collectionInfo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedApiEntity that = (GeneratedApiEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(apiName, that.apiName) &&
                Objects.equals(apiCode, that.apiCode) &&
                Objects.equals(version, that.version) &&
                Objects.equals(status, that.status) &&
                Objects.equals(httpMethod, that.httpMethod) &&
                Objects.equals(basePath, that.basePath) &&
                Objects.equals(endpointPath, that.endpointPath) &&
                Objects.equals(category, that.category) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(isActive, that.isActive) &&
                Objects.equals(totalCalls, that.totalCalls) &&
                Objects.equals(lastCalledAt, that.lastCalledAt) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(sourceObjectInfo, that.sourceObjectInfo) &&
                Objects.equals(collectionInfo, that.collectionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, apiName, apiCode, version, status, httpMethod, basePath,
                endpointPath, category, owner, createdAt, updatedAt, updatedBy,
                createdBy, isActive, totalCalls, lastCalledAt, tags, sourceObjectInfo, collectionInfo);
    }

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
                ", updatedBy='" + updatedBy + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", isActive=" + isActive +
                ", totalCalls=" + totalCalls +
                ", lastCalledAt=" + lastCalledAt +
                ", tags=" + tags +
                ", sourceObjectInfo=" + sourceObjectInfo +
                ", collectionInfo=" + collectionInfo +
                '}';
    }
}