package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSpecRequest {
    @NotBlank(message = "Source is required")
    private String source; // "openapi", "postman", "github", "url", "file"

    private String url;
    private MultipartFile file;
    private String content;
    private String format; // "json", "yaml", "xml"
    private String collectionName;
    private String description;
    private Boolean generateImplementations;
    private List<String> targetLanguages;
    private Map<String, Object> importOptions;
}