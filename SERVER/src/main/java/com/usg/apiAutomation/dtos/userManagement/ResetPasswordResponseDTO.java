package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponseDTO {
    private boolean success;
    private String message;
    private String userId;
    private Date resetAt;
    private Date expiresAt;
    private Map<String, Object> securityInfo;
}