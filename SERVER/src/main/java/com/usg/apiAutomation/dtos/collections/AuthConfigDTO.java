package com.usg.apiAutomation.dtos.collections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthConfigDTO {
    private String type;
    private String token;
    private String tokenType;
    private String username;
    private String password;
    private String key;
    private String value;
    private String addTo;
}