package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateImplementationResponse {
    private String language;
    private String component;
    private Date validatedAt;
    private Boolean isValid;
    private List<ValidationIssue> issues;
    private String validationId;
    private String status;
}

