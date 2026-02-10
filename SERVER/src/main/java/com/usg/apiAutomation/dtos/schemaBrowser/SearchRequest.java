package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String searchQuery;
    private String searchType; // ALL, TABLE, VIEW, PROCEDURE, etc.
    private Integer maxResults;
    private List<String> schemas;
    private List<String> objectTypes;
    private Boolean caseSensitive;
    private Boolean exactMatch;
    private Boolean searchComments;
    private Boolean searchColumns;

    // Add the missing field
    private String connectionId;
}