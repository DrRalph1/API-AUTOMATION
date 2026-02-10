package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSnippetResponse {
    private String requestId;
    private String collectionId;
    private String language;
    private String snippetType;
    private String code;
    private Date generatedAt;
    private Map<String, Object> metadata;
    private String snippetId;
}