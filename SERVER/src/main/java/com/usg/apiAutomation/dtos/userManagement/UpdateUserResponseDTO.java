package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserResponseDTO {
    private boolean success;
    private String message;
    private Date updatedAt;
    private UserDTO user;
    private Map<String, String> auditLog;
}