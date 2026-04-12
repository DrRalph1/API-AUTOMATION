package com.usg.autoAPIGenerator.dtos.userManagement;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserRequestDTO {
    private String userId; // For updates, null for new users
    private String username;
    private String email;
    private String password;
    private String role;
    private Map<String, Object> customData;
}