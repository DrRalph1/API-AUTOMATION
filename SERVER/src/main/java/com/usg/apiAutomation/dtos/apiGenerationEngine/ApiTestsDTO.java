package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestsDTO {
    private String unitTests;           // JSON string containing unit tests
    private String integrationTests;     // JSON string containing integration tests
    private String testData;             // JSON string containing test data
    private List<String> assertions;     // List of assertion strings
    private Integer performanceThreshold; // Performance threshold in ms
    private String testEnvironment;       // Test environment (DEV, QA, PROD)
    private Integer testUsers;            // Number of test users for load testing
    private Integer testIterations;       // Number of test iterations
}