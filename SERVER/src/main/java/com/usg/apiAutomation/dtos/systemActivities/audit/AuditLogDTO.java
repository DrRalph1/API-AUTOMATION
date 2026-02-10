package com.usg.apiAutomation.dtos.systemActivities.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for audit logs")
public class AuditLogDTO {

    @Schema(description = "Unique identifier of the audit log", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID auditId;

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "ID of the userManagement performing the action", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String userId;

    @NotBlank(message = "Action cannot be blank")
    @Schema(description = "Action performed by the userManagement", example = "CREATE_TRANSACTION")
    private String action;

    @NotBlank(message = "Operation cannot be blank")
    @Schema(description = "Operation associated with the action", example = "Transaction API")
    private String operation;

    @Schema(description = "Additional details about the action", example = "Created transaction with reference TXN12345")
    private String details;

    @Schema(description = "Timestamp when the action occurred")
    private LocalDateTime timestamp;
}
