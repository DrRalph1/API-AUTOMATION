package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Format is required")
    private String format; // "complete", "single", "github", "docker"

    private String requestId;
    private String collectionId;
    private List<String> components;
    private Boolean includeConfig;
    private Boolean includeTests;
    private Boolean includeDocumentation;
    private String outputType; // "zip", "tar", "files"
    private Map<String, Object> exportOptions;
}