package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardCodeGenerationSummaryResponseDTO {
    private int totalImplementations;
    private int supportedLanguages;
    private Map<String, Integer> languageDistribution;
    private List<Map<String, Object>> recentImplementations;
    private String validationSuccessRate;
    private String averageGenerationTime;
}