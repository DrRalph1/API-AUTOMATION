package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDataRequest {
    private String tableName;
    private Integer page;
    private Integer pageSize;
    private String sortColumn;
    private String sortDirection; // ASC, DESC
    private Map<String, String> filters;
    private List<String> columns;
    private Boolean distinct;
    private String whereClause;
    private String connectionId;
}