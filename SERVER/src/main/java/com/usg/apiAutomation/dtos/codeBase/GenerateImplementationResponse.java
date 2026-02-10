package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateImplementationResponse {
    private String requestId;
    private String collectionId;
    private String language;
    private Date generatedAt;
    private String status;
    private Map<String, String> implementations;
    private Map<String, String> quickStartGuide;
    private List<String> features;
    private String downloadUrl;
    private String generationId;
    private Map<String, Object> metadata;  // Added field
}