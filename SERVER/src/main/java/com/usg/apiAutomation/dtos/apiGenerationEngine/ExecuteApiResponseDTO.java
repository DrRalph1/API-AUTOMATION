package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteApiResponseDTO {
    private String requestId;
    private Integer statusCode;
    private Map<String, String> headers;
    private Object data;
    private Map<String, Object> metadata;
    private Long executionTimeMs;
    private Boolean success;
    private String message;
    private Map<String, Object> error;
}