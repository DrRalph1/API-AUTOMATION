package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private String query;
    private Date searchAt;
    private List<SearchResult> results;
    private int total;
    private int page;
    private int pageSize;
    private int totalPages;
}