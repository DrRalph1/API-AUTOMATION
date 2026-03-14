package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {
    private String query;
    private List<SearchResultDTO> results;
    private long totalResults;
    private Map<String, Long> countsByType;
}