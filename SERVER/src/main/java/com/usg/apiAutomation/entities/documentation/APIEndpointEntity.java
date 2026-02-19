package com.usg.apiAutomation.entities.documentation;

import jakarta.persistence.*;
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

@Entity(name = "APIEndpointEntity")
@Table(name = "tb_doc_api_endpoints")
@Data
@NoArgsConstructor
public class APIEndpointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "tb_endpoint_tags",
            joinColumns = @JoinColumn(name = "endpoint_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(name = "requires_auth")
    private boolean requiresAuth;

    @Column(name = "is_deprecated")  // FIXED: Match the actual column name
    private boolean deprecated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private APICollectionEntity collection;

    private String category;

    // FIX: Change from Long to JSON type
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rate_limit", columnDefinition = "jsonb")
    private Map<String, Object> rateLimit;

    @Column(name = "api_version")
    private String apiVersion;

    @Column(name = "request_body_example", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> requestBodyExample;

    @OneToMany(mappedBy = "endpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeaderEntity> headers = new ArrayList<>();

    @OneToMany(mappedBy = "endpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParameterEntity> parameters = new ArrayList<>();

    @OneToMany(mappedBy = "endpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResponseExampleEntity> responseExamples = new ArrayList<>();

    @OneToMany(mappedBy = "endpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeExampleEntity> codeExamples = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}