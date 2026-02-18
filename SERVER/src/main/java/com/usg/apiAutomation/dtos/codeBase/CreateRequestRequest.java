package com.usg.apiAutomation.dtos.codeBase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequestRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String method;

    @NotBlank
    private String url;

    private String description;

    @NotNull
    private String collectionId;

    private String folderId;

    private List<String> tags;

    private List<HeaderDTO> headers;

    private List<ParameterDTO> queryParameters;

    private List<ParameterDTO> pathParameters;

    private Map<String, Object> requestBody;

    private Map<String, Object> responseExample;
}