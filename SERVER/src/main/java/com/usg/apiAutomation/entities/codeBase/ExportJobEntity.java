package com.usg.apiAutomation.entities.codeBase;

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

@Entity(name = "ExportJobEntity")
@Table(name = "tb_cbase_export_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String format;

    private String requestId;

    private String collectionId;

    private String performedBy;  // Added to track user

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> exportData;

    private String downloadUrl;

    private String fileSize;

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, READY, FAILED, EXPIRED

    private Integer fileCount;  // Number of files in export

    private Long totalSize;  // Total size in bytes

    private String errorMessage;  // Store error if failed

    private LocalDateTime expiresAt;

    private Integer retryCount;  // Number of retry attempts

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;  // When processing started

    private LocalDateTime completedAt;

    @Version
    private Integer version;  // Optimistic locking

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isReady() {
        return "READY".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public void markAsProcessing() {
        this.status = "PROCESSING";
        this.startedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = "READY";
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }
}