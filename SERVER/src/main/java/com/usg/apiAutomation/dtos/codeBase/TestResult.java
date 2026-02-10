package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    private String name;
    private String duration;
    private String message;
    private String component;
    private Boolean passed;
    private List<TestCase> testCases;
    private Double executionTime;
    private String status;
}
