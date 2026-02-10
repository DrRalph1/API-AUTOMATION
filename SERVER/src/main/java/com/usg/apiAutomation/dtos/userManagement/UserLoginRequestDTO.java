package com.usg.apiAutomation.dtos.userManagement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for userManagement login requests containing authentication credentials.
 * Extends BaseUserRequestDTO to include device and channel information.
 */
public class UserLoginRequestDTO {

    @JsonProperty("user_id")
    // @NotBlank(message = "User ID cannot be blank")
    // @NotNull(message = "User ID cannot be null")
    // @Size(min = 3, max = 50, message = "User ID must be between 3 and 50 characters")
    // @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "User ID can only contain alphanumeric characters and underscores")
    private String userId;

    @JsonProperty("password")
    // @NotBlank(message = "Password cannot be blank")
    // @NotNull(message = "Password cannot be null")
    // @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            // message = "Password must contain at least one uppercase, one lowercase, one number and one special character")
    private String password;

    // Constructors
    public UserLoginRequestDTO() {
        super();
    }


    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // toString Method
    @Override
    public String toString() {
        return "UserLoginRequestDTO{" +
                "userId='" + userId + '\'' +
                ", password='[PROTECTED]'" +
                ", phoneNumber='[PROTECTED]'" +
                '}';
    }
}