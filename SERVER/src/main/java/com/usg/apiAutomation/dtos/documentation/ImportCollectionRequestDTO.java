package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportCollectionRequestDTO {
    private String importType; // postman, openapi, curl
    private String content;
    private String url;
    private String name;
    private String description;
    private boolean overwrite;
    private Map<String, String> options;
}