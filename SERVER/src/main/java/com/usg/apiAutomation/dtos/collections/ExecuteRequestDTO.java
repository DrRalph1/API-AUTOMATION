package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteRequestDTO {
    private String method;
    private String url;
    private List<HeaderDTO> headers;
    private List<ParameterDTO> parameters;
    private String body;
    private AuthConfigDTO authConfig;
    private int timeout;
    private boolean followRedirects;
}