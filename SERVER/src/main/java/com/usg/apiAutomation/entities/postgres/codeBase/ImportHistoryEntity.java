package com.usg.apiAutomation.entities.postgres.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "ImportHistoryEntity")
@Table(name = "tb_cbase_import_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String source;

    private String format;

    @Column(nullable = false)
    private String collectionId;

    @Column(name = "endpoints_imported")
    private Integer endpointsImported;

    @Column(name = "implementations_generated")
    private Integer implementationsGenerated;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "imported_at", updatable = false)
    private LocalDateTime importedAt;
}