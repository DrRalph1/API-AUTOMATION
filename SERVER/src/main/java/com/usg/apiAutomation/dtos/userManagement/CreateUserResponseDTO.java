package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponseDTO {
    private String id;
    private boolean success;
    private String message;
    private UserDTO user;
    private Date generatedAt;
    private List<String> nextSteps;
    private String temporaryPassword; // Only if auto-generated
    private Map<String, Object> metadata;
}