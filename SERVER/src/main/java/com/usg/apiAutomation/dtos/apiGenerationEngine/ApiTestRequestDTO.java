package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestRequestDTO {
    private String testName;
    private String testType;
    private Map<String, Object> pathParams;
    private Map<String, Object> queryParams;
    private Map<String, String> headers;
    private Object body;
    private Object expectedResponse;
}