package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocumentationResponse {
    private List<SearchResultDto> results;
    private String searchQuery;
    private int totalResults;
    private String timestamp;

    public SearchDocumentationResponse(List<SearchResultDto> results, String searchQuery, int totalResults) {
        this.results = results;
        this.searchQuery = searchQuery;
        this.totalResults = totalResults;
        this.timestamp = LocalDateTime.now().toString();
    }
}