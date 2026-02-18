package com.usg.apiAutomation.dtos.userManagement;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportUsersRequestDTO {
    @NotBlank(message = "Format is required")
    private String format; // csv, json, excel

    private Map<String, Object> filters;
    private List<String> fields;
    private boolean includeInactive;
    private String filename;
    private Map<String, String> options;
}