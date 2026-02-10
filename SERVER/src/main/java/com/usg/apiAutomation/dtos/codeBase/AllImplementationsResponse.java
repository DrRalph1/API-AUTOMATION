package com.usg.apiAutomation.dtos.codeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllImplementationsResponse {
    private String requestId;
    private String collectionId;
    private Map<String, Map<String, String>> implementations;
    private int totalLanguages;
    private int totalFiles;
    private Date retrievedAt;
}
