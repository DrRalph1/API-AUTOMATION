package com.usg.apiAutomation.entities.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_search_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String query;

    private String language;

    private String collectionId;

    private String method;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column(name = "performed_by")
    private String performedBy;

    @CreationTimestamp
    @Column(name = "searched_at", updatable = false)
    private LocalDateTime searchedAt;
}