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
public class ObjectValidationResultDTO {
    private boolean valid;
    private String status;
    private String message;
    private List<String> errors;
    private List<String> missingDependencies;
}