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
public class UpdateRequestRequest {
    @NotBlank(message = "RequestEntity ID is required")
    private String requestId;

    @NotBlank(message = "CollectionEntity ID is required")
    private String collectionId;

    private String folderId;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Method is required")
    private String method;

    @NotBlank(message = "URL is required")
    private String url;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private List<String> tags;
    private Map<String, Object> body;
    private List<HeaderItem> headers;
    private Map<String, Object> requestOptions;
}