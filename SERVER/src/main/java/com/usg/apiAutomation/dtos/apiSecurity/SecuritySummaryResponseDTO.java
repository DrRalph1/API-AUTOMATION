package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySummaryResponseDTO {
    private Integer totalEndpoints;
    private Integer securedEndpoints;
    private Integer vulnerableEndpoints;
    private Integer blockedRequests;
    private Integer throttledRequests;
    private String avgResponseTime;
    private Integer securityScore;
    private String lastScan;
    private Map<String, Object> quickStats;
}