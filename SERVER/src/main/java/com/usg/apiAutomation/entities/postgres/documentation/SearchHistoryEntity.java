package com.usg.apiAutomation.entities.postgres.documentation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "SearchHistoryEntityDocumentation")
@Table(name = "tb_doc_search_history")
@Data
@NoArgsConstructor
public class SearchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String query;

    @Column(name = "search_type")
    private String searchType;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "result_count")
    private int resultCount;

    @Column(name = "search_time_ms")
    private Long searchTimeMs;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}