package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationStatsDTO {
    private int totalCollections;
    private int totalEndpoints;
    private int publishedCollections;
    private int totalVisitors;
    private int apiCallsToday;
    private int totalCodeExamples;
    private int totalMockEndpoints;
    private Map<String, Integer> endpointByMethod; // GET, POST, etc.
    private Map<String, Integer> collectionsByType; // REST, SOAP, etc.
}