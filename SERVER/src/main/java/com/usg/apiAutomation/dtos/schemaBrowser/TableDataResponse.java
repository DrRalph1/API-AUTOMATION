package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDataResponse {
    private List<Map<String, Object>> data;
    private Integer totalRows;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;
    private List<String> columns;
    private Long queryTimeMs;
    private String sqlQuery;
    private String errorMessage;
    private Boolean hasMore;
    private String lastUpdated;

    public TableDataResponse(List<Map<String, Object>> data, Integer totalRows,
                             Integer currentPage, Integer pageSize, Integer totalPages) {
        this.data = data;
        this.totalRows = totalRows;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}