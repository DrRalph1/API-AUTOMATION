package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationIssue {
    private String type; // "error", "warning", "info"
    private String message;
    private Integer line;
    private Integer column;
    private String severity;
    private String fix;
}
