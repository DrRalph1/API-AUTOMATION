package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ApiExecutionLogDTO {
    private String id;
    private String apiId;
    private String requestId;
    private String requestParams;
    private String requestBody;
    private String responseBody;
    private Integer responseStatus;
    private Long executionTimeMs;
    private LocalDateTime executedAt;
    private String executedBy;
    private String clientIp;
    private String userAgent;
    private String errorMessage;
    private Map<String, Object> metadata;
}