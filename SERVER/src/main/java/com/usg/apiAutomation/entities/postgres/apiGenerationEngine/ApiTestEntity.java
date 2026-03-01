package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

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
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "test_name")
    private String testName;

    @Column(name = "test_type")
    private String testType;

    @Type(JsonType.class)
    @Column(name = "test_data", columnDefinition = "jsonb")
    private Map<String, Object> testData;

    @Type(JsonType.class)
    @Column(name = "expected_response", columnDefinition = "jsonb")
    private Map<String, Object> expectedResponse;

    @Type(JsonType.class)
    @Column(name = "actual_response", columnDefinition = "jsonb")
    private Map<String, Object> actualResponse;

    @Column(name = "status")
    private String status;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "executed_by")
    private String executedBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiTestEntity that = (ApiTestEntity) o;
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
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, testName, testType, testData, expectedResponse,
                actualResponse, status, executionTimeMs, executedAt, executedBy,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiTestEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", testName='" + testName + '\'' +
                ", testType='" + testType + '\'' +
                ", testData=" + summarizeJson(testData) + '\'' +
                ", expectedResponse=" + summarizeJson(expectedResponse) + '\'' +
                ", actualResponse=" + summarizeJson(actualResponse) + '\'' +
                ", status='" + status + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", executedAt=" + formatDateTime(executedAt) +
                ", executedBy='" + executedBy + '\'' +
                '}';
    }

    /**
     * Helper method to summarize JSON in toString()
     */
    private String summarizeJson(Map<String, Object> json) {
        if (json == null) return null;
        if (json.isEmpty()) return "{}";
        return "{size=" + json.size() + ", keys=" + json.keySet() + "}";
    }

    /**
     * Helper method to format datetime in toString()
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Checks if the test passed
     */
    public boolean isPassed() {
        return "PASSED".equalsIgnoreCase(status);
    }

    /**
     * Checks if the test failed
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }

    /**
     * Checks if the test is pending
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status) || status == null;
    }

    /**
     * Checks if the test is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equalsIgnoreCase(status);
    }

    /**
     * Gets the test execution duration in a readable format
     */
    public String getFormattedExecutionTime() {
        if (executionTimeMs == null) return "N/A";

        if (executionTimeMs < 1000) {
            return executionTimeMs + " ms";
        } else if (executionTimeMs < 60000) {
            return String.format("%.2f seconds", executionTimeMs / 1000.0);
        } else {
            return String.format("%.2f minutes", executionTimeMs / 60000.0);
        }
    }

    /**
     * Validates if the actual response matches the expected response
     */
    public boolean validateResponse() {
        if (actualResponse == null || expectedResponse == null) {
            return false;
        }
        return actualResponse.equals(expectedResponse);
    }

    /**
     * Gets the difference between actual and expected response
     * Returns a map of fields that differ
     */
    public Map<String, Object> getResponseDifferences() {
        if (actualResponse == null || expectedResponse == null) {
            return Map.of("error", "Cannot compare null responses");
        }

        Map<String, Object> differences = new java.util.HashMap<>();

        // Check for fields in expected but not in actual
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

        // Check for extra fields in actual response
        for (String key : actualResponse.keySet()) {
            if (!expectedResponse.containsKey(key)) {
                differences.put(key, "Unexpected field in actual response");
            }
        }

        return differences;
    }

    /**
     * Gets the test data value for a specific key
     */
    public Object getTestDataValue(String key) {
        if (testData == null) return null;
        return testData.get(key);
    }

    /**
     * Gets the expected response value for a specific key
     */
    public Object getExpectedValue(String key) {
        if (expectedResponse == null) return null;
        return expectedResponse.get(key);
    }

    /**
     * Gets the actual response value for a specific key
     */
    public Object getActualValue(String key) {
        if (actualResponse == null) return null;
        return actualResponse.get(key);
    }

    /**
     * Checks if the test execution was successful (no errors)
     */
    public boolean isExecutionSuccessful() {
        return !"ERROR".equalsIgnoreCase(status) &&
                !"FAILED".equalsIgnoreCase(status);
    }

    /**
     * Builder with defaults
     */
    public static class ApiTestEntityBuilder {
        private String status = "PENDING"; // Default status
        private String testType = "UNIT"; // Default test type
    }
}