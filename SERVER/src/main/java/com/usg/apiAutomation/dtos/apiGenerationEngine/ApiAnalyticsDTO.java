package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAnalyticsDTO {
    private String apiId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long totalCalls;
    private Double averageExecutionTimeMs;
    private Long totalErrors;
    private Double successRate;
    private Map<Integer, Long> statusDistribution;
    private Map<String, Long> dailyCallStats;
}