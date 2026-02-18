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
public class CopyImplementationRequest {
    @NotBlank(message = "Source language is required")
    private String sourceLanguage;

    @NotBlank(message = "Target language is required")
    private String targetLanguage;

    @NotBlank(message = "RequestEntity ID is required")
    private String requestId;

    @NotBlank(message = "CollectionEntity ID is required")
    private String collectionId;

    private List<String> components;
    private Map<String, Object> conversionOptions;
}