package com.usg.apiAutomation.entities.apiSecurity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "IPWhitelistEntryEntity")
@Table(name = "tb_sec_ip_whitelist_entries")
@Data
@NoArgsConstructor
public class IPWhitelistEntryEntity {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String ipRange;
    private String description;
    private String endpoints;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}