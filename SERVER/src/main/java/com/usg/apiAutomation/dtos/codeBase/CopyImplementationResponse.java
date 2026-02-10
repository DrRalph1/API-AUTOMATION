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
public class CopyImplementationResponse {
    private String sourceLanguage;
    private String targetLanguage;
    private String requestId;
    private String collectionId;
    private Date convertedAt;
    private String status;
    private Map<String, String> implementations;
    private String conversionId;
    private int componentsConverted;
}