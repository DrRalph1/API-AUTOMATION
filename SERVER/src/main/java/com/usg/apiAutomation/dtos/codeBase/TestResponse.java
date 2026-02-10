package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResponse {
    private String requestId;
    private String language;
    private Date testedAt;
    private List<TestResult> testResults;
    private int testsPassed;
    private int testsFailed;
    private int totalTests;
    private String coverage;
    private String executionTime;
    private String status; // PASSED, FAILED, PARTIAL
}