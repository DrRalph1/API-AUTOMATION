package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPWhitelistEntry {
    private String id;
    private String name;
    private String ipRange;
    private String description;
    private String endpoints;
    private String status;
    private String createdAt;
}