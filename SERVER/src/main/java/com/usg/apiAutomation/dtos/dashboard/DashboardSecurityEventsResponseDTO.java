package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardSecurityEventsResponseDTO {
    private List<DashboardSecurityEventDTO> events;
    private Map<String, Object> insights;
}