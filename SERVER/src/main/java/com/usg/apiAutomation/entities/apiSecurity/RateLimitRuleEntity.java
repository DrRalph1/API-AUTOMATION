package com.usg.apiAutomation.entities.apiSecurity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity(name = "RateLimitRuleEntity")
@Table(name = "tb_sec_rate_limit_rules")
@Getter
@Setter
public class RateLimitRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;
    private String endpoint;
    private String method;

    @Column(name = "limit_value")
    private Integer limitValue;

    @Column(name = "time_window")  // Changed from "window" to "time_window"
    private String window;

    private Integer burst;
    private String action;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}