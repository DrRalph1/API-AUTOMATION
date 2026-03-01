package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "PublishedDocumentationEntity")
@Table(name = "tb_doc_published_documentation")
@Data
@NoArgsConstructor
public class PublishedDocumentationEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private APICollectionEntity collection;

    @Column(name = "published_url", nullable = false)
    private String publishedUrl;

    private String title;

    private String visibility; // public, private, team

    @Column(name = "custom_domain")
    private String customDomain;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "published_at")
    @CreationTimestamp
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "published_by")
    private String publishedBy;

    @Column(name = "version")
    private String version;
}