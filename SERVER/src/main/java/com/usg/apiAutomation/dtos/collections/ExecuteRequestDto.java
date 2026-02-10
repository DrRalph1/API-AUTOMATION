package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteRequestDto {
    private String method;
    private String url;
    private List<HeaderDto> headers;
    private List<ParameterDto> parameters;
    private String body;
    private AuthConfigDto authConfig;
    private int timeout;
    private boolean followRedirects;
}