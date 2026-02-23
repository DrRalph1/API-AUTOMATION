package com.usg.apiAutomation.dtos.dashboard;

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
}