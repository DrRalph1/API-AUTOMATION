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
public class TestImplementationRequest {
    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotBlank(message = "Collection ID is required")
    private String collectionId;

    private List<String> components;
    private String testType; // "unit", "integration", "e2e"
    private Map<String, Object> testData;
    private Map<String, Object> testOptions;
}