package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private String avatarColor;
    private String department;
    private List<String> permissions;
    private boolean mfaEnabled;
    private boolean emailVerified;
    private boolean phoneVerified;
    private int apiAccessCount;
    private String lastLoginIp;
    private String location;
    private String timezone;
    private int totalLogins;
    private int failedLogins;
    private int securityScore;
    private List<String> tags;
    private List<Map<String, Object>> devices;
    private int apiKeys;
    private int activeSessions;
    private String lastActive;
    private String joinedDate;
}