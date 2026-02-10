package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<SearchResultDto> results;
    private String searchQuery;
    private Integer totalResults;
    private Integer searchTimeMs;
    private String searchType;
    private List<String> searchedSchemas;
    private List<String> searchedObjectTypes;
    private Boolean isComplete;
    private String lastUpdated;

    public SearchResponse(List<SearchResultDto> results, String searchQuery, Integer totalResults) {
        this.results = results;
        this.searchQuery = searchQuery;
        this.totalResults = totalResults;
        this.searchTimeMs = 0;
        this.isComplete = true;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}