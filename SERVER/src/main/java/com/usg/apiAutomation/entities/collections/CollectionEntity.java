package com.usg.apiAutomation.entities.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "CollectionEntityCollections")
@Table(name = "tb_collections")
@Data
@ToString(exclude = {"folders", "variables"})
public class CollectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}