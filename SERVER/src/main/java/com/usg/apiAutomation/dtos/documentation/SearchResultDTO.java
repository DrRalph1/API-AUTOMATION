package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String id;
    private String title;
    private String type;
    private String category;
    private String description;
    private int relevanceScore;
    private String collection;
    private String endpointUrl;
    private String lastUpdated;
}