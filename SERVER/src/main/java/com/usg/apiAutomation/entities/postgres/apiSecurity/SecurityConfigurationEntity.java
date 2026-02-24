package com.usg.apiAutomation.entities.postgres.apiSecurity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity(name = "SecurityConfigurationEntity")
@Table(name = "tb_sec_configuration")
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