package com.usg.apiAutomation.entities.postgres.codeBase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "TestExecutionResultEntity")
        @Table(name = "tb_cbase_test_execution_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(nullable = false)
    private String language;

    @Column(name = "test_name", nullable = false)
    private String testName;

    private String status;

    private String duration;

    private String message;

    private String coverage;

    @Column(name = "execution_time")
    private String executionTime;

    @Column(name = "tests_passed")
    private Integer testsPassed;

    @Column(name = "tests_failed")
    private Integer testsFailed;

    @Column(name = "total_tests")
    private Integer totalTests;

    @CreationTimestamp
    @Column(name = "tested_at", updatable = false)
    private LocalDateTime testedAt;
}