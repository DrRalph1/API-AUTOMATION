package com.usg.autoAPIGenerator.entities.postgres.collections;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity(name = "CollectionEntityCollections")
@Table(name = "tb_collections", indexes = {
        // Index for filtering by owner (most common query)
        @Index(name = "idx_collections_owner", columnList = "owner"),

        // Index for sorting by timestamps
        @Index(name = "idx_collections_created_at", columnList = "created_at"),
        @Index(name = "idx_collections_updated_at", columnList = "updated_at"),
        @Index(name = "idx_collections_last_activity", columnList = "last_activity"),

        // Index for filtering by collection type
        @Index(name = "idx_collections_collection_type", columnList = "collection_type"),

        // Index for favorite collections
        @Index(name = "idx_collections_is_favorite", columnList = "is_favorite"),

        // Composite indexes for common query patterns
        @Index(name = "idx_collections_owner_favorite", columnList = "owner, is_favorite"),
        @Index(name = "idx_collections_owner_updated", columnList = "owner, updated_at"),
        @Index(name = "idx_collections_type_owner", columnList = "collection_type, owner"),

        // Index for name searches (if you search by name)
        @Index(name = "idx_collections_name", columnList = "name"),

        // Index for API ID lookups
        @Index(name = "idx_collections_api_id", columnList = "api_id")
})
@Data
@ToString(exclude = {"folders", "variables"})
public class CollectionEntity {

    @Id
    private String id;

    @Column(name = "api_id")
    private String generatedApiId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "is_favorite")
    private boolean isFavorite = false;

    @Column(name = "is_expanded")
    private boolean isExpanded = false;

    @Column(name = "is_editing")
    private boolean isEditing = false;

    @Column(name = "owner")
    private String owner;

    @Column(name = "color")
    private String color;

    /**
     * Collection type from frontend (core, channel, payment, thirdparty, admin, misc)
     */
    @Column(name = "collection_type")
    private String collectionType;

    @ElementCollection
    @CollectionTable(name = "tb_col_tags",
            joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FolderEntity> folders = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VariableEntity> variables = new ArrayList<>();

    @Column(name = "comments", length = 2000)
    private String comments;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    /**
     * Additional metadata stored as JSONB
     * Can store collection type, source info, etc.
     */
    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    // Helper methods remain unchanged
    public String getCollectionType() {
        if (collectionType != null) {
            return collectionType;
        }
        if (metadata != null && metadata.containsKey("collectionType")) {
            return (String) metadata.get("collectionType");
        }
        return null;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("collectionType", collectionType);
    }

    public boolean isNewCollection() {
        return metadata != null && metadata.containsKey("isNewCollection") &&
                Boolean.TRUE.equals(metadata.get("isNewCollection"));
    }

    public String getFrontendId() {
        if (metadata != null && metadata.containsKey("frontendId")) {
            return (String) metadata.get("frontendId");
        }
        return id;
    }
}