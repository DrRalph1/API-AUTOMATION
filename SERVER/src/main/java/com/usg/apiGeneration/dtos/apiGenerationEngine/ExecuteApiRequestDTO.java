package com.usg.apiGeneration.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteApiRequestDTO {
    private String url;
    private Map<String, Object> pathParams;
    private Map<String, Object> queryParams;
    private Map<String, String> headers;
    private Object body;
    private String requestId;
    private String httpMethod;
    private Integer timeoutSeconds;
    private Map<String, Object> metadata;

    // ============ FILE UPLOAD SUPPORT ============
    private MultipartFile file;                      // Single file upload
    private List<MultipartFile> files;               // Multiple files upload
    private Map<String, MultipartFile> fileMap;      // Named files (parameterName -> file)

    // For backward compatibility with base64 encoded files in JSON
    private Boolean hasBase64Files;                  // Flag indicating base64 files in body
}