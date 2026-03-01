package com.usg.apiAutomation.entities.postgres.collections;

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
@Table(name = "tb_collections")
@Data
@ToString(exclude = {"folders", "variables"})
public class CollectionEntity {

    @Id
    private String id;

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

    // Helper method to get collection type from metadata if not directly set
    public String getCollectionType() {
        if (collectionType != null) {
            return collectionType;
        }
        if (metadata != null && metadata.containsKey("collectionType")) {
            return (String) metadata.get("collectionType");
        }
        return null;
    }

    // Helper method to set collection type in both field and metadata
    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("collectionType", collectionType);
    }

    // Helper method to check if collection is new (from frontend)
    public boolean isNewCollection() {
        return metadata != null && metadata.containsKey("isNewCollection") &&
                Boolean.TRUE.equals(metadata.get("isNewCollection"));
    }

    // Helper method to get frontend ID if available
    public String getFrontendId() {
        if (metadata != null && metadata.containsKey("frontendId")) {
            return (String) metadata.get("frontendId");
        }
        return id;
    }
}