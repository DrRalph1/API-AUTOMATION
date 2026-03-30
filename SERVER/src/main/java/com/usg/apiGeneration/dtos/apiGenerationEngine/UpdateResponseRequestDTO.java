package com.usg.apiGeneration.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResponseRequestDTO {
    private Integer statusCode;
    private String message;
    private Object data;
    private Long executionDurationMs;
    private Map<String, Object> responseHeaders;
    private Long responseSizeBytes;
}