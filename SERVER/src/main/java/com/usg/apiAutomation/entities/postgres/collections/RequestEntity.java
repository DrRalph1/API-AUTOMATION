package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "RequestsEntityCollections")
@Table(name = "tb_col_requests", indexes = {
        // CRITICAL: Index for batch query by collection_id (used in dashboard)
        @Index(name = "idx_requests_collection_id", columnList = "collection_id"),

        // Index for folder lookups
        @Index(name = "idx_requests_folder_id", columnList = "folder_id"),

        // Index for API ID lookups
        @Index(name = "idx_requests_api_id", columnList = "api_id"),

        // Index for sorting and filtering
        @Index(name = "idx_requests_created_at", columnList = "created_at"),
        @Index(name = "idx_requests_updated_at", columnList = "updated_at"),
        @Index(name = "idx_requests_last_modified", columnList = "last_modified"),

        // Index for filtering by method (GET, POST, etc.)
        @Index(name = "idx_requests_method", columnList = "method"),

        // Index for filtering by status
        @Index(name = "idx_requests_status", columnList = "status"),

        // Composite indexes for common query patterns
        @Index(name = "idx_requests_collection_folder", columnList = "collection_id, folder_id"),
        @Index(name = "idx_requests_collection_updated", columnList = "collection_id, updated_at"),
        @Index(name = "idx_requests_method_status", columnList = "method, status"),

        // Index for name searches (if you search by name)
        @Index(name = "idx_requests_name", columnList = "name")
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RequestEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(name = "api_id")
    private String generatedApiId;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String name;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String method;

    @Column(nullable = false, length = 2000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String url;

    @Column(length = 2000)
    private String description;

    @Column(name = "is_editing")
    private boolean isEditing = false;

    @Column(name = "status")
    private String status;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "body", length = 10000)
    private String body;

    @Column(name = "tests", length = 10000)
    private String tests;

    @Column(name = "pre_request_script", length = 10000)
    private String preRequestScript;

    @Column(name = "is_saved")
    private boolean isSaved = false;

    @Column(name = "auth_type")
    private String authType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CollectionEntity collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FolderEntity folder;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AuthConfigEntity authConfig;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<HeaderEntity> headers = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ParameterEntity> params = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    // Helper methods for maintaining bidirectional relationships
    public void addHeader(HeaderEntity header) {
        headers.add(header);
        header.setRequest(this);
    }

    public void removeHeader(HeaderEntity header) {
        headers.remove(header);
        header.setRequest(null);
    }

    public void addParameter(ParameterEntity param) {
        params.add(param);
        param.setRequest(this);
    }

    public void removeParameter(ParameterEntity param) {
        params.remove(param);
        param.setRequest(null);
    }

    public void setAuthConfig(AuthConfigEntity authConfig) {
        this.authConfig = authConfig;
        if (authConfig != null) {
            authConfig.setRequest(this);
        }
    }

    public void setCollection(CollectionEntity collection) {
        this.collection = collection;
    }

    public void setFolder(FolderEntity folder) {
        this.folder = folder;
    }
}