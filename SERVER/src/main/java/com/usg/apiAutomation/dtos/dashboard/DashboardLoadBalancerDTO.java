package com.usg.apiAutomation.dtos.dashboard;

import lombok.Data;

@Data
public class DashboardLoadBalancerDTO {
    private String id;
    private String name;
    private String algorithm;
    private String status;
    private int totalConnections;
    private int serverCount;
    private int healthyServers;
}