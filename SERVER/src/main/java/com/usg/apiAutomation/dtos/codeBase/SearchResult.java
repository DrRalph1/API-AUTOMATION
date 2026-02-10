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
public class SearchResult {
    private String id;
    private String name;
    private String description;
    private String method;
    private String url;
    private String collection;
    private String folder;
    private List<String> languages;
    private String lastModified;
    private Integer implementations;
    private String matchType; // "name", "description", "code", "tag"
}