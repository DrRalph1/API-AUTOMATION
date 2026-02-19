package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocumentationResponseDTO {
    private List<SearchResultDTO> results;
    private String searchQuery;
    private int totalResults;
    private String timestamp;

    public SearchDocumentationResponseDTO(List<SearchResultDTO> results, String searchQuery, int totalResults) {
        this.results = results;
        this.searchQuery = searchQuery;
        this.totalResults = totalResults;
        this.timestamp = LocalDateTime.now().toString();
    }
}