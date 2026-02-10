package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIEndpointDto {
    private String method;
    private String path;
    private String description;
    private Map<String, Object> requestExample;
    private Map<String, Object> responseExample;
    private String operationId;
    private String summary;
    private Map<String, Object> parameters;
    private Map<String, Object> requestBody;
    private Map<String, Object> responses;
    private String security;
    private String tags;
    private Boolean deprecated;
    private String consumes;
    private String produces;
}