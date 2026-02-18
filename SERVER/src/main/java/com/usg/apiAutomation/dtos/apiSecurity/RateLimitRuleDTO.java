package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRuleDTO {
    private String id;
    private String name;
    private String description;
    private String endpoint;
    private String method;
    private Integer limit;
    private String window;
    private Integer burst;
    private String action;
    private String status;
    private Integer limitValue;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}