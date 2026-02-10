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
public class SecurityReportResponse {
    private String reportId;
    private String generatedAt;
    private String status;
    private Map<String, Object> summary;
    private List<Map<String, Object>> recommendations;
    private String downloadUrl;
    private String expiresAt;
}