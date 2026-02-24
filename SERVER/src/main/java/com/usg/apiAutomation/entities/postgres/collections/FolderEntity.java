package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "FoldersEntityCollections")
@Table(name = "tb_col_folders")
@Data
@ToString(exclude = {"requests", "collection"})
public class FolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "is_expanded")
    private boolean isExpanded = false;

    @Column(name = "is_editing")
    private boolean isEditing = false;

    @Column(name = "request_count")
    private int requestCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionEntity collection;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequestEntity> requests = new ArrayList<>();
}