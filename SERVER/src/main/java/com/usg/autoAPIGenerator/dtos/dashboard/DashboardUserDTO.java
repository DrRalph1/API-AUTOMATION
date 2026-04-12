package com.usg.autoAPIGenerator.dtos.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class DashboardUserDTO {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private String department;
    private String lastActive;
    private String joinedDate;
    private int securityScore;
    private boolean mfaEnabled;
    private List<String> tags;
    private String createdAt;
    private String createdBy;
    private String updatedAt;
}