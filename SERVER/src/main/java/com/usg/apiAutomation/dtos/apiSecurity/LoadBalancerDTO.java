package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadBalancerDTO {
    private String id;
    private String name;
    private String algorithm;
    private String healthCheck;
    private String healthCheckInterval;
    private List<Map<String, Object>> servers;
    private String status;
    private Integer totalConnections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}