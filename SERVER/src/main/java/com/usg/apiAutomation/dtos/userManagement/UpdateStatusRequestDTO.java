package com.usg.apiAutomation.dtos.userManagement;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDTO {
    @NotBlank(message = "Status is required")
    private String status; // active, inactive, suspended, pending

    private String reason;
    private boolean sendNotification;
    private String notificationMessage;
    private Date effectiveDate;
}