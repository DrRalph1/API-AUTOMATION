package com.usg.apiAutomation.dtos.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log search request")
public class AuditLogSearchRequest {

    // Add getters for validation
    @Schema(
            description = "User ID to filter by",
            example = "user123",
            minLength = 1,
            maxLength = 100
    )
    @Pattern(regexp = "^[a-zA-Z0-9._@-]*$", message = "User ID can only contain alphanumeric characters, dots, underscores, @, and hyphens")
    private String userId;

    @Schema(
            description = "Action to filter by",
            example = "LOGIN",
            minLength = 1,
            maxLength = 50
    )
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Action can only contain alphanumeric characters and underscores")
    private String action;

    @Schema(
            description = "Operation to filter by",
            example = "CREATE",
            minLength = 1,
            maxLength = 50
    )
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Operation can only contain alphanumeric characters and underscores")
    private String operation;

    @Schema(
            description = "Details text to search for",
            example = "failed login attempt",
            minLength = 1,
            maxLength = 500
    )
    @Pattern(regexp = "^[a-zA-Z0-9\\s.,!?@#$%^&*()_+=\\-\\[\\]{}|;:'\"<>/\\\\]*$",
            message = "Details contains invalid characters")
    private String details;

    @Schema(
            description = "Start date for filtering (inclusive). Format: yyyy-MM-dd'T'HH:mm:ss",
            example = "2024-01-01T00:00:00",
            type = "string",
            format = "date-time"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @Schema(
            description = "End date for filtering (inclusive). Format: yyyy-MM-dd'T'HH:mm:ss",
            example = "2024-12-31T23:59:59",
            type = "string",
            format = "date-time"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    @Schema(
            description = "Specific audit log ID",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID auditId;

    // Setters with trimming
    public void setUserId(String userId) {
        this.userId = userId != null ? userId.trim() : null;
    }

    public void setAction(String action) {
        this.action = action != null ? action.trim() : null;
    }

    public void setOperation(String operation) {
        this.operation = operation != null ? operation.trim() : null;
    }

    public void setDetails(String details) {
        this.details = details != null ? details.trim() : null;
    }
}