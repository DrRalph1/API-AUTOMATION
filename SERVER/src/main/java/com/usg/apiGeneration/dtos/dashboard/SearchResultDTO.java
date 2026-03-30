package com.usg.apiGeneration.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private String id;
    private String type;
    private String title;
    private String description;
    private String url;
    private Map<String, Object> metadata;
}