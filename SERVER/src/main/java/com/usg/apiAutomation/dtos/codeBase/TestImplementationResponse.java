package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestImplementationResponse {
    private String language;
    private String requestId;
    private String collectionId;
    private Date testedAt;
    private String status;
    private Map<String, TestResult> results;
    private String testId;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private double coverage;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TestCase {
    private String name;
    private Boolean passed;
    private String message;
    private Double executionTime;
    private String error;
    private String stackTrace;
}