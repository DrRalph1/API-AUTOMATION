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
public class AddLoadBalancerRequestDTO {
    private String name;
    private String algorithm;
    private String healthCheck;
    private String healthCheckInterval;
    private List<Map<String, Object>> servers;
}