package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class ComprehensiveDashboardResponseDTO {
    private DashboardStatsResponseDTO stats;
    private DashboardCollectionsResponseDTO collections;
    private DashboardEndpointsResponseDTO endpoints;
    private DashboardRateLimitRulesResponseDTO rateLimitRules;
    private DashboardIpWhitelistResponseDTO ipWhitelist;
    private DashboardLoadBalancersResponseDTO loadBalancers;
    private DashboardSecurityEventsResponseDTO securityEvents;
    private DashboardSecurityAlertsResponseDTO securityAlerts;
    private DashboardSecuritySummaryResponseDTO securitySummary;
    private DashboardLanguagesResponseDTO languages;
    private DashboardCodeGenerationSummaryResponseDTO codeGenerationSummary;
    private DashboardDocumentationResponseDTO documentation;
    private DashboardMockServersResponseDTO mockServers;
    private DashboardEnvironmentsResponseDTO environments;
    private DashboardUsersResponseDTO users;
    private DashboardUserActivitiesResponseDTO userActivities;
    private DashboardNotificationsResponseDTO notifications;
    private String generatedAt;
    private String generatedFor;
    private String requestId;
}