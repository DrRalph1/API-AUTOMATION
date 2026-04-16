package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteApiResponseDTO {
//    private String requestId;
//    private Integer statusCode;
//    private Map<String, String> headers;
    private Object data;
//    private Map<String, Object> metadata;
//    private Long executionTimeMs;
//    private String correlationId;
    private Boolean success;
    private String message;
    private Integer responseCode;
    private String contentType;  // "application/json" or "application/xml"
    private String protocolType; // "rest", "soap", "graphql"
//    private Map<String, Object> error;
}