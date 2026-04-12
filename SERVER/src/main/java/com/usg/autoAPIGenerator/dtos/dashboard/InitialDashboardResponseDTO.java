package com.usg.autoAPIGenerator.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitialDashboardResponseDTO {
    private DashboardStatsResponseDTO stats;
    private List<DashboardCollectionDTO> recentCollections;
    private List<DashboardEndpointDTO> recentEndpoints;
    private List<ActivityDTO> recentActivities;
    private DashboardSecuritySummaryResponseDTO securitySummary;
}