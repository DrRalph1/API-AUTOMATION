package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDTO {
    private String id;
    private String name;
    private String method;
    private String url;
    private String description;
    private String collectionId;
    private String collectionName;
    private String folderId;
    private String folderName;
    private List<String> tags;
    private List<HeaderDTO> headers;
    private List<ParameterDTO> queryParameters;
    private List<ParameterDTO> pathParameters;
    private Map<String, Object> requestBody;
    private Map<String, Object> responseExample;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer implementationsCount;
}