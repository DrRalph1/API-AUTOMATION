package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserResponseDTO {
    private boolean success;
    private String message;
    private Date deletedAt;
    private UserDTO deletedUser;
    private Map<String, Object> cleanupInfo;
}