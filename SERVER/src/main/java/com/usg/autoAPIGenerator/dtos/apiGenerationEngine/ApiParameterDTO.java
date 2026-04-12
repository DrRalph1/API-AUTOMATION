package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

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
    private String type;
    private String bodyFormat;
    private String dbParameter;
    private String oracleType;
    private String apiType;
    private String parameterType;
    private String parameterLocation; // query, path, header, body
    private Boolean required;
    private String description;
    private String example;
    private String validationPattern;
    private String defaultValue;
    private Boolean inBody;
    private Boolean isPrimaryKey;
    private String paramMode; // IN, OUT, IN OUT (for procedures)
    private Integer position;
}