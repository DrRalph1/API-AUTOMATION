package com.usg.apiAutomation.dtos.userManagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationRequestDTO {
    @NotBlank(message = "Operation is required")
    private String operation; // activate, suspend, deactivate, delete, reset_password

    @NotEmpty(message = "At least one user must be selected")
    private List<String> userIds;

    private Map<String, Object> parameters;
    private boolean sendNotifications;
    private String reason;
}