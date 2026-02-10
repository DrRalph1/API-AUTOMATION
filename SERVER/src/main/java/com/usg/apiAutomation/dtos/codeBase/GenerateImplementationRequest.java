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
public class GenerateImplementationRequest {
    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotBlank(message = "Collection ID is required")
    private String collectionId;

    @NotBlank(message = "Language is required")
    private String language;

    private List<String> components;
    private Boolean includeTests;
    private Boolean includeDocumentation;
    private String framework;
    private Map<String, Object> options;
}