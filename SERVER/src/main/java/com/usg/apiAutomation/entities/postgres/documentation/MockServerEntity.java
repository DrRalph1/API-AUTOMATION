package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "MockServerEntity")
@Table(name = "tb_doc_mock_servers")
@Data
@NoArgsConstructor
public class MockServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false, unique = true)
    private APICollectionEntity collection;

    @Column(name = "mock_server_url", nullable = false)
    private String mockServerUrl;

    @Column(name = "is_active")
    private boolean isActive = true;

    private String description;

    @OneToMany(mappedBy = "mockServer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockEndpointEntity> mockEndpoints = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_by")
    private String createdBy;
}