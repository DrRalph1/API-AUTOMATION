package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardLoadBalancersResponseDTO {
    private List<DashboardLoadBalancerDTO> loadBalancers;
    private Map<String, Object> performance;
}