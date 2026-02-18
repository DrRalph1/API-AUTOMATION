package com.usg.apiAutomation.dtos.userManagement;

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
public class ValidationResponseDTO {
    private String language;
    private Date validatedAt;
    private boolean isValid;
    private List<ValidationIssueDTO> issues;
    private int score;
}