package com.usg.apiAutomation.entities.apiSecurity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity(name = "SecurityReportEntity")
@Table(name = "tb_sec_reports")
public class SecurityReportEntity {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String reportId;
    private String status;
    private Integer totalChecks;
    private Integer issuesFound;
    private Integer securityScore;
    private String threatLevel;
    private String downloadUrl;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    private String generatedBy;

    @Lob
    private String recommendations; // JSON string

}