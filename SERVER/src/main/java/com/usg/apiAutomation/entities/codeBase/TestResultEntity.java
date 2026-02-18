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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tb_test_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String requestId;

    private String requestName;  // Denormalized for quick access

    private String collectionId;

    private String collectionName;  // Denormalized for quick access

    @Column(nullable = false)
    private String language;

    private String version;  // Version of the implementation tested

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> testResults;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> testCases;

    private Integer testsPassed;

    private Integer testsFailed;

    private Integer testsSkipped;

    private Integer totalTests;

    private String coverage;  // Code coverage percentage

    private String executionTime;  // Total execution time

    private Double successRate;  // Calculated success rate

    private String status; // PASSED, FAILED, PARTIAL, ERROR

    private String testType; // "unit", "integration", "e2e", "performance"

    private String testedBy;  // User who ran the tests

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> testEnvironment;  // Environment details

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> failedTests;  // Details of failed tests

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> performanceMetrics;  // Performance test metrics

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> coverageDetails;  // Detailed coverage by component

    private String buildNumber;  // CI/CD build number if applicable

    private String branch;  // Git branch if applicable

    private String commitId;  // Git commit ID if applicable

    @Column(length = 5000)
    private String errorMessage;  // Overall error message if any

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;  // Additional metadata

    private Boolean isBaseline;  // Whether this is a baseline test run

    private String baselineId;  // Reference to baseline test run

    private Integer flakyTests;  // Number of flaky tests detected

    @CreationTimestamp
    private LocalDateTime testedAt;

    private LocalDateTime completedAt;

    // Helper methods
    public boolean isPassed() {
        return "PASSED".equals(status) && (testsFailed == null || testsFailed == 0);
    }

    public boolean isFailed() {
        return "FAILED".equals(status) || (testsFailed != null && testsFailed > 0);
    }

    public boolean isPartial() {
        return "PARTIAL".equals(status) || (testsFailed != null && testsFailed > 0 && testsPassed > 0);
    }

    public void calculateSuccessRate() {
        if (totalTests != null && totalTests > 0) {
            this.successRate = (testsPassed * 100.0) / totalTests;
        }
    }

    public void addFailedTest(Map<String, Object> failedTest) {
        if (this.failedTests == null) {
            this.failedTests = new ArrayList<>();
        }
        this.failedTests.add(failedTest);
        this.testsFailed = this.failedTests.size();
    }

    public void incrementPassed() {
        this.testsPassed = this.testsPassed == null ? 1 : this.testsPassed + 1;
        this.totalTests = this.totalTests == null ? 1 : this.totalTests + 1;
        calculateSuccessRate();
    }

    public void incrementFailed() {
        this.testsFailed = this.testsFailed == null ? 1 : this.testsFailed + 1;
        this.totalTests = this.totalTests == null ? 1 : this.totalTests + 1;
        calculateSuccessRate();
    }

    public void incrementSkipped() {
        this.testsSkipped = this.testsSkipped == null ? 1 : this.testsSkipped + 1;
        this.totalTests = this.totalTests == null ? 1 : this.totalTests + 1;
    }

    public void markAsPassed() {
        this.status = "PASSED";
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsPartial() {
        this.status = "PARTIAL";
        this.completedAt = LocalDateTime.now();
    }

    public void markAsError(String error) {
        this.status = "ERROR";
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }

    public String getSummary() {
        return String.format("Tests: %d total, %d passed, %d failed, %d skipped (%.1f%%)",
                totalTests != null ? totalTests : 0,
                testsPassed != null ? testsPassed : 0,
                testsFailed != null ? testsFailed : 0,
                testsSkipped != null ? testsSkipped : 0,
                successRate != null ? successRate : 0.0);
    }

    public boolean isBetterThan(TestResultEntity other) {
        if (other == null) return true;
        double thisRate = this.successRate != null ? this.successRate : 0;
        double otherRate = other.successRate != null ? other.successRate : 0;
        return thisRate > otherRate;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        calculateSuccessRate();
        if (totalTests != null) {
            if (testsFailed != null && testsFailed > 0) {
                if (testsPassed != null && testsPassed > 0) {
                    this.status = "PARTIAL";
                } else {
                    this.status = "FAILED";
                }
            } else if (testsPassed != null && testsPassed.equals(totalTests)) {
                this.status = "PASSED";
            }
        }
    }
}