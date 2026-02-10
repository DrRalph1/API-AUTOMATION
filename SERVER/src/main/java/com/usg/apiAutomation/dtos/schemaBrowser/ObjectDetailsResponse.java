package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectDetailsResponse {
    private String objectName;
    private String objectType;
    private String owner;
    private String status;
    private String created;
    private String lastDDL;
    private String tablespace;
    private Long rowCount;
    private String size;
    private String comment;
    private List<ColumnDto> columns;
    private List<ParameterDto> parameters;
    private List<ConstraintDto> constraints;
    private List<IndexDto> indexes;
    private List<TriggerDto> triggers;
    private Map<String, Object> grants;
    private Map<String, Object> dependencies;
    private Map<String, Object> statistics;
    private String ddl;
    private String lastUpdated;
    private String schema;
    private String database;
    private String partitionType;
    private String compression;
    private String caching;
    private String monitoring;
    private String rowMovement;
}