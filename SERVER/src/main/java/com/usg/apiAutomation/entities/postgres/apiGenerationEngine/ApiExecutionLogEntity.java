package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_execution_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionLogEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "request_id")
    private String requestId;

    @Type(JsonType.class)
    @Column(name = "request_params", columnDefinition = "jsonb")
    private Map<String, Object> requestParams;

    @Type(JsonType.class)
    @Column(name = "request_body", columnDefinition = "jsonb")
    private Map<String, Object> requestBody;

    @Type(JsonType.class)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private Map<String, Object> responseBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiExecutionLogEntity that = (ApiExecutionLogEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(requestParams, that.requestParams) &&
                Objects.equals(requestBody, that.requestBody) &&
                Objects.equals(responseBody, that.responseBody) &&
                Objects.equals(responseStatus, that.responseStatus) &&
                Objects.equals(executionTimeMs, that.executionTimeMs) &&
                Objects.equals(executedAt, that.executedAt) &&
                Objects.equals(executedBy, that.executedBy) &&
                Objects.equals(clientIp, that.clientIp) &&
                Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestId, requestParams, requestBody, responseBody,
                responseStatus, executionTimeMs, executedAt, executedBy,
                clientIp, userAgent, errorMessage,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiExecutionLogEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", requestId='" + requestId + '\'' +
                ", requestParams=" + maskSensitiveData(requestParams) +
                ", requestBody=" + maskSensitiveData(requestBody) +
                ", responseBody=" + maskSensitiveData(responseBody) +
                ", responseStatus=" + responseStatus +
                ", executionTimeMs=" + executionTimeMs +
                ", executedAt=" + executedAt +
                ", executedBy='" + executedBy + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    /**
     * Helper method to mask sensitive data in toString()
     * Truncates JSON data to avoid huge log entries
     */
    private String maskSensitiveData(Map<String, Object> data) {
        if (data == null) return null;
        if (data.isEmpty()) return "{}";
        return "{size=" + data.size() + ", keys=" + data.keySet() + "}";
    }

    /**
     * Helper method to mask sensitive string data
     */
    private String maskSensitiveData(String data) {
        if (data == null) return null;
        if (data.length() <= 10) return "********";
        return data.substring(0, 5) + "..." + data.substring(data.length() - 5);
    }
}