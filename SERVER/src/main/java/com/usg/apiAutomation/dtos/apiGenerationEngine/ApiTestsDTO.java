package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestsDTO {
    private String unitTests;
    private String integrationTests;
    private String testData;
    private List<String> assertions;
    private Integer performanceThreshold;
    private String testEnvironment;
    private Integer testUsers;
    private Integer testIterations;
}