package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteQueryResponse {
    private List<Map<String, Object>> results;
    private String query;
    private Integer rowCount;
    private String message;
    private Boolean success;
    private List<String> columns;
    private List<String> columnTypes;
    private Long executionTimeMs;
    private Integer affectedRows;
    private String queryId;
    private String lastUpdated;

    public ExecuteQueryResponse(List<Map<String, Object>> results, String query,
                                Integer rowCount, String message) {
        this.results = results;
        this.query = query;
        this.rowCount = rowCount;
        this.message = message;
        this.success = true;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}