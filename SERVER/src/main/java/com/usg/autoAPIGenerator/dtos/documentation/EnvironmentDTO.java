package com.usg.autoAPIGenerator.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentDTO {
    private String id;
    private String name;
    private String baseUrl;
    private boolean isActive;
    private String description;
    private String apiKey;
    private String secret;
    private Map<String, Object> variables;
    private String lastUsed;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
}