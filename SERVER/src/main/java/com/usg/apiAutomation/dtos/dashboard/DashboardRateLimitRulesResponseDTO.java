package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardRateLimitRulesResponseDTO {
    private List<DashboardRateLimitRuleDTO> rules;
    private Map<String, Object> statistics;
}