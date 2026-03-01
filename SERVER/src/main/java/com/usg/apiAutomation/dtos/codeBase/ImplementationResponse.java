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
public class ImplementationResponse {
    private String language;
    private String component;
    private String requestId;
    private String collectionId;
    private String code;
    private Boolean notFound;
    private String fileName;
    private Map<String, Object> languageInfo;
    private Date generatedAt;
    private String status;
    private Long fileSize;  // Added field
    private Integer linesOfCode;  // Added field
    private Map<String, Object> syntaxInfo;  // Added field
}