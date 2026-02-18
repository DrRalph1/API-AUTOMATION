package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationIssueDTO {
    private String type; // error, warning, info
    private String field;
    private String message;
    private String severity; // high, medium, low
    private String code;
    private Map<String, Object> details;
}