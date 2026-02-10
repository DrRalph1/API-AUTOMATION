package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlert {
    private String id;
    private String type;
    private String severity;
    private String message;
    private String endpoint;
    private boolean read;
    private String timestamp;
}