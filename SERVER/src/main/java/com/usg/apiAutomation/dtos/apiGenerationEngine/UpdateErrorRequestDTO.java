package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateErrorRequestDTO {
    private Integer statusCode;
    private String errorMessage;
    private Long executionDurationMs;
    private String errorDetails;
    private String errorType;
}