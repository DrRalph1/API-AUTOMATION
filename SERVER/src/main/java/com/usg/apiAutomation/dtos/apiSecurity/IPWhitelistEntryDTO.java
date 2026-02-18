package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IPWhitelistEntryDTO {
    private String id;
    private String name;
    private String ipRange;
    private String description;
    private String endpoints;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}