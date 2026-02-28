package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_eng_execution_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionLogEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "request_params", columnDefinition = "jsonb")
    private String requestParams;

    @Column(name = "request_body", columnDefinition = "jsonb")
    private String requestBody;

    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

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
}