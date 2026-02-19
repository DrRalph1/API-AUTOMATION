package com.usg.apiAutomation.entities.apiSecurity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity(name = "SecurityScanEntity")
@Table(name = "tb_sec_scans")
public class SecurityScanEntity {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String scanId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer totalFindings;
    private Long criticalFindings;
    private String scanDuration;
    private Integer securityScore;
    private String performedBy;

    @Lob
    private String findings; // JSON string

}