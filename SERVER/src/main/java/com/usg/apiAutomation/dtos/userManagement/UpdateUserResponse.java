package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserResponse {
    private boolean success;
    private String message;
    private Date updatedAt;
    private UserDto user;
    private Map<String, String> auditLog;
}