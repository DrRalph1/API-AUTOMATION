package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;
import jakarta.validation.constraints.Email;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Email(message = "Invalid email format")
    private String email;

    private String fullName;
    private String role;
    private String status;
    private String department;
    private Boolean mfaEnabled;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String location;
    private String timezone;
    private List<String> tags;
    private List<String> permissions;
    private Map<String, Object> customFields;
}