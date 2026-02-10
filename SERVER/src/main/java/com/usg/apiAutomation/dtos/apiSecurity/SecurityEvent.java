package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEvent {
    private String id;
    private String type;
    private String severity;
    private String sourceIp;
    private String endpoint;
    private String method;
    private String message;
    private String timestamp;
}