package com.usg.apiAutomation.dtos.systemActivities.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResultDTO {
    private String userId;
    private String username;
    private String fullName;
    private String roleId;
    private String roleName;
    private String status;
    private String message;
}