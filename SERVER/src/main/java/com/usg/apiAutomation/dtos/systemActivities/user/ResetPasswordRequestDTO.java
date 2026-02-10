package com.usg.apiAutomation.dtos.systemActivities.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetPasswordRequestDTO {

    @JsonProperty("user_id")
    // @NotBlank(message = "User ID is required.")
    // // @Size(max = 50, message = "User ID cannot exceed 50 characters.")
    private String userId;

    @JsonProperty("old_password")
    // @NotBlank(message = "Current password is required.")
    // @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    private String oldPassword;

    @JsonProperty("new_password")
    // @NotBlank(message = "New password is required.")
    // @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    private String newPassword;

    // All-args constructor
    public ResetPasswordRequestDTO(String userId, String oldPassword, String newPassword) {
        this.userId = userId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // No-args constructor
    public ResetPasswordRequestDTO() {
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "ResetPasswordRequestDTO{" +
                "user_id='" + userId + '\'' +
                ", old_password='[PROTECTED]'" +
                ", new_password='[PROTECTED]'" +
                '}';
    }
}