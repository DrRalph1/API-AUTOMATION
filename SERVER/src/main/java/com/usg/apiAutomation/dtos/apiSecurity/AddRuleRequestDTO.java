package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddRuleRequestDTO {
    private String name;
    private String description;
    private String endpoint;
    private String method;
    private Integer limit;
    private String window;
    private Integer burst;
    private String action;
    private String status;
}