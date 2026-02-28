package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseConfigDTO {
    private String successSchema;
    private String errorSchema;
    private Boolean includeMetadata;
    private List<String> metadataFields;
    private String contentType;
    private String compression;
}