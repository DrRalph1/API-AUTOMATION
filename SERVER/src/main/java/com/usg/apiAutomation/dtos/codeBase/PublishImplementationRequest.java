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
public class PublishImplementationRequest {
    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotBlank(message = "Collection ID is required")
    private String collectionId;

    @NotBlank(message = "Target is required")
    private String target; // "github", "gitlab", "dockerhub", "nexus", "s3"

    private Map<String, Object> targetConfig;
    private Map<String, Object> publishOptions;
    private Boolean makePublic;
    private String version;
}