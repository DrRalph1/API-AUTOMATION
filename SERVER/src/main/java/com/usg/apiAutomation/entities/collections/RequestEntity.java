package com.usg.apiAutomation.entities.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "RequestsEntityCollections")
@Table(name = "tb_col_requests")
@Data
@ToString(exclude = {"headers", "params", "collection", "folder"})
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
    private CollectionEntity collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private AuthConfigEntity authConfig;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HeaderEntity> headers = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ParameterEntity> params = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}