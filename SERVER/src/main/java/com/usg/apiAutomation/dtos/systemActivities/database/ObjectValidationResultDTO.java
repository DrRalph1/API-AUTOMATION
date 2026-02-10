package com.usg.apiAutomation.dtos.systemActivities.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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