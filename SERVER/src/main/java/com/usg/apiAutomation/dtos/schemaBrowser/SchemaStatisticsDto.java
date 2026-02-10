package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaStatisticsDto {
    private Integer totalObjects;
    private Integer totalTables;
    private Integer totalViews;
    private Integer totalProcedures;
    private Integer totalFunctions;
    private Integer totalPackages;
    private Integer totalSequences;
    private Integer totalSynonyms;
    private Integer totalTypes;
    private Integer totalTriggers;
    private Integer totalIndexes;
    private String databaseSize;
    private String schemaSize;
    private String lastAnalyzed;
    private String databaseName;
    private String schemaName;
    private Integer objectCount;
    private String lastUpdated;
}