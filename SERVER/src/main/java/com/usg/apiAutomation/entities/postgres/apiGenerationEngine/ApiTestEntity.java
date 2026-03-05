package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Entity
@Table(name = "tb_eng_tests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "test_name")
    private String testName;

    // =============================
    // Database connectivity tests
    // =============================
    @Column(name = "test_connection")
    private Boolean testConnection;

    @Column(name = "test_object_access")
    private Boolean testObjectAccess;

    @Column(name = "test_privileges")
    private Boolean testPrivileges;

    // =============================
    // Data validation tests
    // =============================
    @Column(name = "test_data_types")
    private Boolean testDataTypes;

    @Column(name = "test_null_constraints")
    private Boolean testNullConstraints;

    @Column(name = "test_unique_constraints")
    private Boolean testUniqueConstraints;

    @Column(name = "test_foreign_key_refs")
    private Boolean testForeignKeyReferences;

    // =============================
    // Performance tests
    // =============================
    @Column(name = "test_query_performance")
    private Boolean testQueryPerformance;

    @Column(name = "performance_threshold")
    private Integer performanceThreshold;

    @Column(name = "test_with_sample_data")
    private Boolean testWithSampleData;

    @Column(name = "sample_data_rows")
    private Integer sampleDataRows;

    // =============================
    // PL/SQL specific tests
    // =============================
    @Column(name = "test_procedure_execution")
    private Boolean testProcedureExecution;

    @Column(name = "test_function_return")
    private Boolean testFunctionReturn;

    @Column(name = "test_exception_handling")
    private Boolean testExceptionHandling;

    // =============================
    // Security tests
    // =============================
    @Column(name = "test_sql_injection")
    private Boolean testSQLInjection;

    @Column(name = "test_authentication")
    private Boolean testAuthentication;

    @Column(name = "test_authorization")
    private Boolean testAuthorization;

    // =============================
    // Query Storage
    // =============================
    @Type(JsonType.class)
    @Column(name = "test_queries", columnDefinition = "jsonb")
    private List<String> testQueries;

    // =============================
    // Test Data & Responses
    // =============================
    @Type(JsonType.class)
    @Column(name = "test_data", columnDefinition = "jsonb")
    private Map<String, Object> testData;

    @Type(JsonType.class)
    @Column(name = "expected_response", columnDefinition = "jsonb")
    private Map<String, Object> expectedResponse;

    @Type(JsonType.class)
    @Column(name = "actual_response", columnDefinition = "jsonb")
    private Map<String, Object> actualResponse;

    @Type(JsonType.class)
    @Column(name = "execution_results", columnDefinition = "jsonb")
    private Map<String, Object> executionResults;

    // =============================
    // Execution Metadata
    // =============================
    @Column(name = "status")
    private String status;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "test_type")
    private String testType;

    // =====================================================
    // equals & hashCode
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiTestEntity that)) return false;

        return Objects.equals(id, that.id) &&
                Objects.equals(testName, that.testName) &&
                Objects.equals(testType, that.testType) &&
                Objects.equals(testData, that.testData) &&
                Objects.equals(expectedResponse, that.expectedResponse) &&
                Objects.equals(actualResponse, that.actualResponse) &&
                Objects.equals(status, that.status) &&
                Objects.equals(executionTimeMs, that.executionTimeMs) &&
                Objects.equals(executedAt, that.executedAt) &&
                Objects.equals(executedBy, that.executedBy) &&
                Objects.equals(
                        generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, testName, testType, testData,
                expectedResponse, actualResponse,
                status, executionTimeMs, executedAt, executedBy,
                generatedApi != null ? generatedApi.getId() : null
        );
    }

    // =====================================================
    // Utility Methods
    // =====================================================

    @Override
    public String toString() {
        return "ApiTestEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", testName='" + testName + '\'' +
                ", testType='" + testType + '\'' +
                ", testData=" + summarizeJson(testData) +
                ", expectedResponse=" + summarizeJson(expectedResponse) +
                ", actualResponse=" + summarizeJson(actualResponse) +
                ", status='" + status + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", executedAt=" + formatDateTime(executedAt) +
                ", executedBy='" + executedBy + '\'' +
                '}';
    }

    private String summarizeJson(Map<String, Object> json) {
        if (json == null) return null;
        if (json.isEmpty()) return "{}";
        return "{size=" + json.size() + ", keys=" + json.keySet() + "}";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null :
                dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public boolean isPassed() {
        return "PASSED".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return status == null || "PENDING".equalsIgnoreCase(status);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equalsIgnoreCase(status);
    }

    public boolean isExecutionSuccessful() {
        return !"ERROR".equalsIgnoreCase(status)
                && !"FAILED".equalsIgnoreCase(status);
    }

    public String getFormattedExecutionTime() {
        if (executionTimeMs == null) return "N/A";
        if (executionTimeMs < 1000) return executionTimeMs + " ms";
        if (executionTimeMs < 60000)
            return String.format("%.2f seconds", executionTimeMs / 1000.0);
        return String.format("%.2f minutes", executionTimeMs / 60000.0);
    }

    public boolean validateResponse() {
        return actualResponse != null
                && expectedResponse != null
                && actualResponse.equals(expectedResponse);
    }

    public Map<String, Object> getResponseDifferences() {
        if (actualResponse == null || expectedResponse == null) {
            return Map.of("error", "Cannot compare null responses");
        }

        Map<String, Object> differences = new HashMap<>();

        for (Map.Entry<String, Object> entry : expectedResponse.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = actualResponse.get(key);

            if (!actualResponse.containsKey(key)) {
                differences.put(key, "Missing in actual response");
            } else if (!Objects.equals(expectedValue, actualValue)) {
                differences.put(key, Map.of(
                        "expected", expectedValue,
                        "actual", actualValue
                ));
            }
        }

        for (String key : actualResponse.keySet()) {
            if (!expectedResponse.containsKey(key)) {
                differences.put(key, "Unexpected field in actual response");
            }
        }

        return differences;
    }

    // =====================================================
    // Builder defaults
    // =====================================================

    public static class ApiTestEntityBuilder {
        private String status = "PENDING";
        private String testType = "UNIT";
    }
}