package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiNavSummaryDTO {
    private String apiId;
    private String apiName;
    private String apiCode;
    private Integer totalRequests;
    private Integer successCount;
    private Integer failedCount;
    private Double successRate;
    private String lastRequestTime;
    private String lastRequestStatus;
    private Integer averageResponseTimeMs;
}