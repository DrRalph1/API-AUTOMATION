package com.usg.apiAutomation.entities.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity(name = "RequestEntity")
@Table(name = "tb_cbase_requests",
        indexes = {
                @Index(name = "idx_requests_collection", columnList = "collection_id"),
                @Index(name = "idx_requests_folder", columnList = "folder_id"),
                @Index(name = "idx_requests_method", columnList = "method"),
                @Index(name = "idx_requests_name", columnList = "name"),
                @Index(name = "idx_requests_created", columnList = "created_at"),
                @Index(name = "idx_requests_updated", columnList = "updated_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "tb_cbase_request_tags",
            joinColumns = @JoinColumn(name = "request_id"),
            indexes = @Index(name = "idx_request_tags_tag", columnList = "tag"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> requestBody;  // Changed from 'body' to match service

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> responseExample;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> headers;  // Changed to List<Map> for flexibility

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> pathParameters;  // Changed from separate entity

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> queryParameters;  // Changed from separate entity

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    private Integer implementationsCount;  // Denormalized for quick access

    private Boolean isFavorite;  // Whether this request is favorited

    private Integer version;  // Version of the request

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionEntity collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImplementationEntity> implementations = new ArrayList<>();

    // Helper methods
    public void addImplementation(ImplementationEntity implementation) {
        implementations.add(implementation);
        implementation.setRequest(this);
        updateImplementationsCount();
    }

    public void removeImplementation(ImplementationEntity implementation) {
        implementations.remove(implementation);
        implementation.setRequest(null);
        updateImplementationsCount();
    }

    public void updateImplementationsCount() {
        this.implementationsCount = implementations.size();
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }
    }

    public void addHeader(String key, String value, String description) {
        if (this.headers == null) {
            this.headers = new ArrayList<>();
        }
        Map<String, Object> header = Map.of(
                "key", key,
                "value", value,
                "description", description != null ? description : "",
                "required", false,
                "disabled", false
        );
        this.headers.add(header);
    }

    public void addPathParameter(String name, String type, boolean required, String description) {
        if (this.pathParameters == null) {
            this.pathParameters = new ArrayList<>();
        }
        Map<String, Object> param = Map.of(
                "name", name,
                "type", type != null ? type : "string",
                "required", required,
                "description", description != null ? description : ""
        );
        this.pathParameters.add(param);
    }

    public void addQueryParameter(String name, String type, boolean required, String description) {
        if (this.queryParameters == null) {
            this.queryParameters = new ArrayList<>();
        }
        Map<String, Object> param = Map.of(
                "name", name,
                "type", type != null ? type : "string",
                "required", required,
                "description", description != null ? description : ""
        );
        this.queryParameters.add(param);
    }

    public boolean hasImplementations() {
        return implementations != null && !implementations.isEmpty();
    }

    public boolean hasImplementationForLanguage(String language) {
        return implementations != null &&
                implementations.stream().anyMatch(impl -> language.equals(impl.getLanguage()));
    }

    public List<String> getAvailableLanguages() {
        if (implementations == null) return new ArrayList<>();
        return implementations.stream()
                .map(ImplementationEntity::getLanguage)
                .distinct()
                .toList();
    }

    public Map<String, Object> toSummaryMap() {
        return Map.of(
                "id", id,
                "name", name,
                "method", method,
                "url", url,
                "description", description,
                "implementationsCount", implementationsCount != null ? implementationsCount : 0,
                "tags", tags != null ? tags : new ArrayList<>(),
                "updatedAt", updatedAt != null ? updatedAt.toString() : null,
                "collectionId", collection != null ? collection.getId() : null,
                "folderId", folder != null ? folder.getId() : null
        );
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateImplementationsCount();
        if (headers == null) headers = new ArrayList<>();
        if (pathParameters == null) pathParameters = new ArrayList<>();
        if (queryParameters == null) queryParameters = new ArrayList<>();
        if (tags == null) tags = new ArrayList<>();
    }
}