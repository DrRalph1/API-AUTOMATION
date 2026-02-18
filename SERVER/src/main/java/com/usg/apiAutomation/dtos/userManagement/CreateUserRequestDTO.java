package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String password;
    private UUID roleId;
    private String department;
    private String phoneNumber;
    private String location;
    private boolean mfaEnabled;
    private boolean sendWelcomeEmail;
    private List<String> tags;
    private Map<String, Object> customAttributes;
}