package com.usg.apiAutomation.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "tb_security_configuration")
public class SecurityConfigurationEntity {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String configKey;
    private String configValue;
    private String dataType;
    private String description;
    private LocalDateTime updatedAt;
    private String updatedBy;

}