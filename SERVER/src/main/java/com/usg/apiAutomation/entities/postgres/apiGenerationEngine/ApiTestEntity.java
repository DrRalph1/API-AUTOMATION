package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_eng_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne
    @JoinColumn(name = "api_id")
    private GeneratedApiEntity generatedApi;

    @Column(name = "test_name")
    private String testName;

    @Column(name = "test_type")
    private String testType;

    @Column(name = "test_data", columnDefinition = "jsonb")
    private String testData;

    @Column(name = "expected_response", columnDefinition = "jsonb")
    private String expectedResponse;

    @Column(name = "actual_response", columnDefinition = "jsonb")
    private String actualResponse;

    @Column(name = "status")
    private String status;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "executed_by")
    private String executedBy;
}