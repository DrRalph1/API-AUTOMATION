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
public class SearchResultDTO {
    private String id;
    private String type;
    private String title;
    private String description;
    private String url;
    private Map<String, Object> metadata;
}