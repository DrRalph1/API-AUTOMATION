package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "ChangelogEntryEntity")
@Table(name = "tb_doc_changelog_entries")
@Data
@NoArgsConstructor
public class ChangelogEntryEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String date;

    private String type; // major, minor, patch, experimental, pre-release

    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private APICollectionEntity collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id")
    private APIEndpointEntity endpoint;

    @ElementCollection
    @CollectionTable(name = "tb_doc_changelog_changes",
            joinColumns = @JoinColumn(name = "changelog_entry_id"))
    @Column(name = "change_description", length = 2000)
    private List<String> changes = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}