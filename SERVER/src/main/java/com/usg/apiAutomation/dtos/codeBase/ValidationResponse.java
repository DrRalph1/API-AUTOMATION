package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private String language;
    private Date validatedAt;
    private boolean isValid;
    private List<ValidationIssue> issues;
    private int score;
}