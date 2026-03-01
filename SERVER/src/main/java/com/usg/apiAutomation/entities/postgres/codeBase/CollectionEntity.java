package com.usg.apiAutomation.entities.postgres.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "CollectionEntityCodeBase")
@Table(name = "tb_cbase_collections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String version;

    private String owner;

    private Boolean isFavorite = false;

    private Boolean isExpanded = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Fix: mappedBy should match the field name in FolderEntity
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FolderEntity> folders = new ArrayList<>(); // Changed from folderEntities to folders

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RequestEntity> requests = new ArrayList<>();
}