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
@Table(name = "tb_col_requests")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

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