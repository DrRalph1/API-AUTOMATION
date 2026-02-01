package com.usg.apiAutomation.dtos.jwt;

import jakarta.validation.constraints.NotBlank;

public class CheckTokenExpiryRequestDTO {
    @NotBlank(message = "Token is required")
    private String token;

    // Constructor
    public CheckTokenExpiryRequestDTO() {}

    public CheckTokenExpiryRequestDTO(String token) {
        this.token = token;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}