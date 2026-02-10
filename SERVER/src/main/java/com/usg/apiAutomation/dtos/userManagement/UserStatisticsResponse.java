package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private int totalUsers;
    private int activeUsers;
    private int admins;
    private int developers;
    private int viewers;
    private int pendingUsers;
    private int suspendedUsers;
    private int mfaEnabledUsers;
    private double avgSecurityScore;
    private Date generatedAt;
    private Map<String, Integer> trends;
    private Map<String, Integer> departmentBreakdown;
    private Map<String, Integer> roleDistribution;
    private Map<String, Integer> statusDistribution;
}