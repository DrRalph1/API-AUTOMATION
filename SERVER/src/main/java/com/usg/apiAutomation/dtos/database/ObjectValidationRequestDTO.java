package com.usg.apiAutomation.dtos.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectValidationRequestDTO {
    @NotBlank(message = "Object type is required")
    private String objectType;

    @NotBlank(message = "Object name is required")
    private String objectName;

    private String owner;
}