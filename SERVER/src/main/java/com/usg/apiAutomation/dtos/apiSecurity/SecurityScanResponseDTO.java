package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityScanResponseDTO {
    private String scanId;
    private String startedAt;
    private String status;
    private List<Map<String, Object>> findings;
    private Integer totalFindings;
    private Long criticalFindings;
    private String scanDuration;
    private Integer securityScore;
}