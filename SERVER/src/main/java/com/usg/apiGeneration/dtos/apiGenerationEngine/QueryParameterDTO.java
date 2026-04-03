package com.usg.apiGeneration.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParameterDTO {
    private String parameterName;
    private String parameterType;  // IN, OUT, INOUT
    private String dataType;
    private String dataTypeClass;
    private Integer dataLength;
    private Boolean isRequired;
    private String description;
    private String defaultValue;
    private Integer position;
}