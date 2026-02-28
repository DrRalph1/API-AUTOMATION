package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseMappingDTO {
    private String id;
    private String apiField;
    private String dbColumn;
    private String oracleType;
    private String apiType;
    private String format;
    private Boolean nullable;
    private Boolean isPrimaryKey;
    private Boolean includeInResponse;
    private Integer position;
}