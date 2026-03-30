package com.usg.apiGeneration.entities.postgres.apiGenerationEngine;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tb_eng_api_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    // =====================================================
    // Reference to Generated API
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    @ToString.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "api_id", insertable = false, updatable = false)
    private String apiId;

    // =====================================================
    // Request Core Details
    // =====================================================

    @Column(name = "request_name", nullable = false)
    private String requestName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    @Column(name = "url", nullable = false, length = 2000)
    private String url;

    @Column(name = "base_path")
    private String basePath;

    @Column(name = "endpoint_path")
    private String endpointPath;

    @Column(name = "request_timeout_seconds")
    private Integer requestTimeoutSeconds;

    // =====================================================
    // Request Components
    // =====================================================

    @Type(JsonType.class)
    @Column(name = "path_parameters", columnDefinition = "jsonb")
    private Map<String, Object> pathParameters = new HashMap<>();

    @Type(JsonType.class)
    @Column(name = "query_parameters", columnDefinition = "jsonb")
    private Map<String, Object> queryParameters = new HashMap<>();

    @Type(JsonType.class)
    @Column(name = "headers", columnDefinition = "jsonb")
    private Map<String, String> headers = new HashMap<>();

    @Type(JsonType.class)
    @Column(name = "request_body", columnDefinition = "jsonb")
    private Map<String, Object> requestBody;

    @Type(JsonType.class)
    @Column(name = "form_data", columnDefinition = "jsonb")
    private Map<String, Object> formData;

    @Type(JsonType.class)
    @Column(name = "multipart_data", columnDefinition = "jsonb")
    private Map<String, Object> multipartData;

    // =====================================================
    // Request Execution Details
    // =====================================================

    @Column(name = "request_timestamp")
    private LocalDateTime requestTimestamp;

    @Column(name = "response_timestamp")
    private LocalDateTime responseTimestamp;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @Column(name = "response_status_code")
    private Integer responseStatusCode;

    @Column(name = "response_status_message")
    private String responseStatusMessage;

    @Type(JsonType.class)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private Map<String, Object> responseBody;

    @Type(JsonType.class)
    @Column(name = "response_headers", columnDefinition = "jsonb")
    private Map<String, String> responseHeaders;

    @Column(name = "response_size_bytes")
    private Long responseSizeBytes;

    // =====================================================
    // Request Status & Tracking
    // =====================================================

    @Column(name = "request_status")
    private String requestStatus; // SUCCESS, FAILED, TIMEOUT, PENDING

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "is_mock_request")
    private Boolean isMockRequest;

    @Column(name = "correlation_id")
    private String correlationId;

    // =====================================================
    // Authentication Details
    // =====================================================

    @Column(name = "auth_type")
    private String authType; // BASIC, BEARER, API_KEY, OAUTH2, etc.

    @Column(name = "auth_token")
    private String authToken; // Store masked/encrypted tokens in production

    @Column(name = "api_key")
    private String apiKey; // Store masked/encrypted keys in production

    // =====================================================
    // Client Information
    // =====================================================

    @Column(name = "client_ip_address")
    private String clientIpAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "source_application")
    private String sourceApplication;

    @Column(name = "requested_by")
    private String requestedBy;

    // =====================================================
    // Audit Fields
    // =====================================================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    // =====================================================
    // Additional Metadata
    // =====================================================

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    // =====================================================
    // CURL Command
    // =====================================================

    @Column(name = "curl_command", length = 4000)
    private String curlCommand;

    // =====================================================
    // equals & hashCode
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiRequestEntity that)) return false;

        return Objects.equals(id, that.id) &&
                Objects.equals(requestTimestamp, that.requestTimestamp) &&
                Objects.equals(correlationId, that.correlationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestTimestamp, correlationId);
    }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        return "ApiRequestEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + apiId + '\'' +
                ", requestName='" + requestName + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", url='" + url + '\'' +
                ", requestTimestamp=" + requestTimestamp +
                ", responseTimestamp=" + responseTimestamp +
                ", executionDurationMs=" + executionDurationMs +
                ", responseStatusCode=" + responseStatusCode +
                ", requestStatus='" + requestStatus + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (requestTimestamp == null) {
            requestTimestamp = LocalDateTime.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (isMockRequest == null) {
            isMockRequest = false;
        }
    }

    /**
     * Calculate execution duration
     */
    public void calculateExecutionDuration() {
        if (requestTimestamp != null && responseTimestamp != null) {
            executionDurationMs = java.time.Duration.between(requestTimestamp, responseTimestamp).toMillis();
        }
    }

    /**
     * Generate curl command from request details
     */
    public void generateCurlCommand() {
        StringBuilder curl = new StringBuilder("curl -X ").append(httpMethod);

        // Add headers
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, value) ->
                    curl.append(" -H '").append(key).append(": ").append(value).append("'")
            );
        }

        // Add request body
        if (requestBody != null && !requestBody.isEmpty()) {
            curl.append(" -d '").append(requestBody.toString()).append("'");
        }

        // Add URL
        curl.append(" '").append(url).append("'");

        this.curlCommand = curl.toString();
    }
}