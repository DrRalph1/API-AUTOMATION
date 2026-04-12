package com.usg.autoAPIGenerator.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardSecurityAlertsResponseDTO {
    private List<DashboardSecurityAlertDTO> alerts;
    private int unreadCount;
}