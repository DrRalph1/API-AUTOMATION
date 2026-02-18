package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusResponseDTO {
    private boolean success;
    private String message;
    private String userId;
    private String previousStatus;
    private String newStatus;
    private Date updatedAt;
    private Map<String, Object> notificationInfo;
}