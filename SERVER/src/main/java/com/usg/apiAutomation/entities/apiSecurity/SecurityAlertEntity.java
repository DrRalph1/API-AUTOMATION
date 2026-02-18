package com.usg.apiAutomation.entities.apiSecurity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "tb_security_alerts")
public class SecurityAlertEntity {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String type;
    private String severity;
    private String message;
    private String endpoint;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime timestamp;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String status;

}