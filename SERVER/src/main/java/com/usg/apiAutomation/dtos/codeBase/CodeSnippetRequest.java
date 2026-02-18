package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSnippetRequest {
    @NotBlank(message = "RequestEntity ID is required")
    private String requestId;

    @NotBlank(message = "CollectionEntity ID is required")
    private String collectionId;

    @NotBlank(message = "Language is required")
    private String language;

    private String snippetType; // "curl", "http", "axios", "fetch", "python", "java"
    private Map<String, Object> options;
    private Boolean includeHeaders;
    private Boolean includeBody;
}