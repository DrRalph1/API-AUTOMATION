package com.usg.apiAutomation.entities.postgres.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity(name = "ImportJobEntity")
@Table(name = "tb_cbase_import_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String source; // "openapi", "postman", "github", "url", "file"

    private String format; // "json", "yaml", "yml", "xml"

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, VALIDATING

    private String performedBy;  // Added to track user

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> importData;

    private String collectionId;

    private String collectionName;  // Name of created collection

    private Integer endpointsImported;

    private Integer foldersCreated;

    private Integer implementationsGenerated;

    private String originalFileName;  // Name of imported file

    private Long fileSize;  // Size of imported file

    private String errorMessage;  // Store error if failed

    private Map<String, Object> validationResults;  // Results of validation

    private Integer retryCount;  // Number of retry attempts

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;  // When processing started

    private LocalDateTime completedAt;

    @Version
    private Integer version;  // Optimistic locking

    // Helper methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isValidating() {
        return "VALIDATING".equals(status);
    }

    public void markAsProcessing() {
        this.status = "PROCESSING";
        this.startedAt = LocalDateTime.now();
    }

    public void markAsValidating() {
        this.status = "VALIDATING";
        this.startedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }

    public void incrementRetry() {
        this.retryCount = this.retryCount == null ? 1 : this.retryCount + 1;
    }
}