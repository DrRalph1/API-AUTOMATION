package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImplementationDTO {
    private String id;
    private String requestId;
    private String requestName;
    private String language;
    private String component;
    private String code;
    private Map<String, Object> metadata;
    private Integer linesOfCode;
    private Boolean isValidated;
    private Integer validationScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}