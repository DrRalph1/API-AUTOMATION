package com.usg.apiAutomation.dtos.systemActivities.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserPasswordResetRequestDTO {

    @JsonProperty("user_id")
    // @NotBlank(message = "User ID is required.")
    private String userId;

    @JsonProperty("new_password")
    // @NotBlank(message = "New password is required.")
    // @Size(min = 8, message = "Password should be at least 8 characters long.")
    private String newPassword;

    @JsonProperty("confirm_password")
    // @NotBlank(message = "Confirm password is required.")
    // @Size(min = 8, message = "Password should be at least 8 characters long.")
    private String confirmPassword;

    @JsonCreator
    public UserPasswordResetRequestDTO(
            @JsonProperty("user_id") String userId,
            @JsonProperty("new_password") String newPassword,
            @JsonProperty("confirm_password") String confirmPassword) {
        this.userId = userId;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public String toString() {
        return "UserPasswordResetRequestDTO{" +
                "user_id='" + userId + '\'' +
                ", new_password='" + newPassword + '\'' +
                ", confirm_password='" + confirmPassword + '\'' +
                '}';
    }
}
