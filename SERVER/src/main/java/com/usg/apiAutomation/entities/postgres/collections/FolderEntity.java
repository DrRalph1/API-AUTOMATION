package com.usg.apiAutomation.entities.postgres.collections;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "FoldersEntityCollections")
@Table(name = "tb_col_folders")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class FolderEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String name;

    @Column(length = 1000)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String description;

    @Column(name = "is_expanded")
    private boolean isExpanded = false;

    @Column(name = "is_editing")
    private boolean isEditing = false;

    @Column(name = "request_count")
    private int requestCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CollectionEntity collection;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RequestEntity> requests = new ArrayList<>();

    // Helper methods for maintaining bidirectional relationships
    public void addRequest(RequestEntity request) {
        requests.add(request);
        request.setFolder(this);
        updateRequestCount();
    }

    public void removeRequest(RequestEntity request) {
        requests.remove(request);
        request.setFolder(null);
        updateRequestCount();
    }

    public void updateRequestCount() {
        this.requestCount = requests.size();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateRequestCount();
    }
}