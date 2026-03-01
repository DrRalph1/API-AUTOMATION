package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "APICollectionEntity")
@Table(name = "tb_doc_api_collections")
@Data
@NoArgsConstructor
public class APICollectionEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String version;

    @Column(name = "owner")
    private String owner;

    @Column(name = "api_type")
    private String type; // REST, SOAP, GraphQL, etc.

    @Column(name = "is_favorite")
    private boolean isFavorite;

    @Column(name = "is_expanded")
    private boolean isExpanded;

    @Column(name = "total_endpoints")
    private int totalEndpoints;

    @Column(name = "total_folders")
    private int totalFolders;

    private String color;
    private String status;

    @Column(name = "base_url")
    private String baseUrl;

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

    @ElementCollection
    @CollectionTable(name = "tb_doc_collection_tags",
            joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_doc_collection_metadata",
            joinColumns = @JoinColumn(name = "collection_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 2000)
    private java.util.Map<String, String> metadata;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderEntity> folders = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<APIEndpointEntity> endpoints = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChangelogEntryEntity> changelog = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PublishedDocumentationEntity> publications = new ArrayList<>();

    @OneToOne(mappedBy = "collection", cascade = CascadeType.ALL)
    private MockServerEntity mockServer;

    // Add defensive copying in setter
    public void setTags(List<String> tags) {
        if (tags == null) {
            this.tags = new ArrayList<>();
        } else {
            this.tags = new ArrayList<>(tags);
        }
    }
}