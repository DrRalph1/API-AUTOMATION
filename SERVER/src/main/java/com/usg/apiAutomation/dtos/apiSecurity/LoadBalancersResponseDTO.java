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
public class LoadBalancersResponseDTO {
    private List<LoadBalancerDTO> loadBalancers;
    private Integer total;
    private Map<String, Object> performance;
}