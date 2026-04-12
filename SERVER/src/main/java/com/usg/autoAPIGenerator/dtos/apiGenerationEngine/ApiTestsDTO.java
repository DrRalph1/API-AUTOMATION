package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

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
    private List<String> assertions;     // List of assertion strings
    private String testEnvironment;       // Test environment (DEV, QA, PROD)
    private Integer testUsers;            // Number of test users for load testing
    private Integer testIterations;       // Number of test iterations

    // Database connectivity tests
    private Boolean testConnection;
    private Boolean testObjectAccess;
    private Boolean testPrivileges;

    // Data validation tests
    private Boolean testDataTypes;
    private Boolean testNullConstraints;
    private Boolean testUniqueConstraints;
    private Boolean testForeignKeyReferences;

    // Performance tests
    private Boolean testQueryPerformance;
    private Integer performanceThreshold;
    private Boolean testWithSampleData;
    private Integer sampleDataRows;

    // PL/SQL specific tests
    private Boolean testProcedureExecution;
    private Boolean testFunctionReturn;
    private Boolean testExceptionHandling;

    // Security tests
    private Boolean testSQLInjection;
    private Boolean testAuthentication;
    private Boolean testAuthorization;

    // Test data
    private Map<String, Object> testData;
    private List<String> testQueries;
}