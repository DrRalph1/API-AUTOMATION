package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlertDTO {
    private String id;
    private String type;
    private String severity;
    private String message;
    private String endpoint;
    private boolean read;
    private String timestamp;
    private Boolean isRead;
}