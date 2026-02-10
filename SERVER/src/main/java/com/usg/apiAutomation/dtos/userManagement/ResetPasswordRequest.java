package com.usg.apiAutomation.dtos.userManagement;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    private boolean forceLogout;
    private boolean sendEmail;
    private String emailTemplate;
    private boolean generateTemporaryPassword;
    private String newPassword; // If provided by admin
}