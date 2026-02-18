package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventDTO {
    private String id;
    private String type;
    private String severity;
    private String sourceIp;
    private String endpoint;
    private String method;
    private String message;
    private String timestamp;
}