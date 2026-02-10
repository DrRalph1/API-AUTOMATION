package com.usg.apiAutomation.dtos.userManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class OtpVerificationRequestDTO {

    @JsonProperty("user_id")
    @NotBlank(message = "User ID is required")
    private String userId;

    @JsonProperty("otp")
    @NotBlank(message = "OTP is required")
    private String otp;

    // Default constructor
    public OtpVerificationRequestDTO() {}

    // Constructor with parameters
    public OtpVerificationRequestDTO(String userId, String otp) {
        this.userId = userId;
        this.otp = otp;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Override
    public String toString() {
        return "OtpVerificationRequestDTO{" +
                "user_id='" + userId + '\'' +
                ", otp='" + otp + '\'' +
                '}';
    }
}
