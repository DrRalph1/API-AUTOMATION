package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportCollectionRequest {
    private String collectionId;
    private String format; // json, yaml, markdown, postman
    private boolean includeExamples;
    private boolean includeMockData;
    private Map<String, String> options;
}