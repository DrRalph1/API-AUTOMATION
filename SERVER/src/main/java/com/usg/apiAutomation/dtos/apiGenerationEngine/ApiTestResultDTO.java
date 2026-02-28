package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestResultDTO {
    private String testName;
    private Boolean passed;
    private Long executionTimeMs;
    private Integer statusCode;
    private Object actualResponse;
    private String message;
}