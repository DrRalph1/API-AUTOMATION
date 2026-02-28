package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiParameterDTO {
    private String id;
    private String key;
    private String dbColumn;
    private String dbParameter;
    private String oracleType;
    private String apiType;
    private String parameterType;
    private Boolean required;
    private String description;
    private String example;
    private String validationPattern;
    private String defaultValue;
    private Integer position;
}