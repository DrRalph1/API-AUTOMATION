package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSchemaConfigDTO {
    private String schemaName;
    private String objectType;
    private String objectName;
    private String operation;
    private String primaryKeyColumn;
    private String sequenceName;
    private Boolean enablePagination;
    private Integer pageSize;
    private Boolean enableSorting;
    private String defaultSortColumn;
    private String defaultSortDirection;
    private Boolean isSynonym;
    private String targetType;
    private String targetName;
    private String targetOwner;
}