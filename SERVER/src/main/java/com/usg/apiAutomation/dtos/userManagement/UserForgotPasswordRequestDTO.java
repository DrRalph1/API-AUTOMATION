package com.usg.apiAutomation.dtos.userManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserForgotPasswordRequestDTO {

    @NotBlank(message = "User ID is required")
    @JsonProperty("user_id")
    private String userId;

    // Optional: you can include additional fields if needed, e.g., email or phone number
    // @Email(message = "Invalid email format")
    // private String email;
}
