package com.usg.apiAutomation.entities.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "tb_implementations",
        indexes = {
                @Index(name = "idx_implementations_request", columnList = "request_id"),
                @Index(name = "idx_implementations_language", columnList = "language"),
                @Index(name = "idx_implementations_component", columnList = "component"),
                @Index(name = "idx_implementations_lang_comp", columnList = "language, component"),
                @Index(name = "idx_implementations_validated", columnList = "is_validated"),
                @Index(name = "idx_implementations_created", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_implementation_request_lang_comp",
                        columnNames = {"request_id", "language", "component"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImplementationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(nullable = false, length = 100)
    private String component;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "lines_of_code")
    private Integer linesOfCode;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_validated")
    private Boolean isValidated = false;

    @Column(name = "validation_score")
    private Integer validationScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "framework", length = 100)
    private String framework;

    @Column(name = "dependencies", columnDefinition = "TEXT")
    private String dependencies;

    @Column(name = "test_coverage")
    private Double testCoverage;

    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;

    @Column(name = "last_test_status", length = 20)
    private String lastTestStatus;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "is_generated")
    private Boolean isGenerated = true;

    @Column(name = "generated_by", length = 255)
    private String generatedBy;

    @Column(name = "git_repository", length = 500)
    private String gitRepository;

    @Column(name = "git_branch", length = 255)
    private String gitBranch;

    @Column(name = "git_commit", length = 255)
    private String gitCommit;

    // RELATIONSHIP - This is the ONLY mapping to request_id column
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestEntity request;  // This maps to request_id column

    // DO NOT ADD any other field with @Column(name = "request_id")
    // DO NOT ADD a separate requestId field with @Column

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "opt_lock_version")
    private Integer optLockVersion;

    // Helper methods - NO requestId getter/setter that tries to access a separate field

    public void calculateLinesOfCode() {
        if (code != null) {
            this.linesOfCode = code.split("\n").length;
            this.fileSize = (long) code.length();
        }
    }

    public void updateFileName() {
        if (component != null && language != null) {
            String extension = getFileExtension(language);
            this.fileName = component + extension;
        }
    }

    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> ".java";
            case "javascript" -> ".js";
            case "python" -> ".py";
            case "csharp" -> ".cs";
            case "php" -> ".php";
            case "go" -> ".go";
            case "ruby" -> ".rb";
            case "kotlin" -> ".kt";
            case "swift" -> ".swift";
            case "rust" -> ".rs";
            default -> ".txt";
        };
    }

    public void markAsValidated(int score) {
        this.isValidated = true;
        this.validationScore = score;
    }

    public void markAsInvalid() {
        this.isValidated = false;
        this.validationScore = 0;
    }

    public void incrementUsage() {
        if (this.usageCount == null) {
            this.usageCount = 1;
        } else {
            this.usageCount++;
        }
    }

    public void updateTestStatus(String status, Double coverage) {
        this.lastTestStatus = status;
        this.testCoverage = coverage;
        this.lastTestedAt = LocalDateTime.now();
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    // Convenience method to get request ID without creating a separate field
    public String getRequestId() {
        return request != null ? request.getId() : null;
    }

    public Map<String, Object> toSummaryMap() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", id);
        summary.put("language", language);
        summary.put("component", component);
        summary.put("fileName", fileName);
        summary.put("linesOfCode", linesOfCode);
        summary.put("isValidated", isValidated);
        summary.put("validationScore", validationScore);
        summary.put("framework", framework);
        summary.put("requestId", getRequestId());
        summary.put("createdAt", createdAt != null ? createdAt.toString() : null);
        summary.put("updatedAt", updatedAt != null ? updatedAt.toString() : null);
        return summary;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        calculateLinesOfCode();
        updateFileName();
        if (isValidated == null) isValidated = false;
        if (usageCount == null) usageCount = 0;
        if (version == null) version = 1;
        if (metadata == null) metadata = new HashMap<>();

        // Add default metadata
        metadata.put("lastValidated", isValidated ? LocalDateTime.now().toString() : null);
    }
}