package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class AuthConfigDto {
    private String type;
    private String token;
    private String tokenType;
    private String username;
    private String password;
    private String key;
    private String value;
    private String addTo;
}