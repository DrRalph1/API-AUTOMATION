package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryColumnDTO {
    private String columnName;
    private String alias;
    private String dataType;
    private String dataTypeClass;
    private Integer dataLength;
    private Integer dataPrecision;
    private Integer dataScale;
    private Boolean isNullable;
    private Boolean isPrimaryKey;
    private String description;
    private Integer position;
}