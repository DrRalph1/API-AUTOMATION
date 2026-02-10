package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class RequestDetailsResponse {
    private String requestId;
    private String name;
    private String method;
    private String url;
    private String description;
    private String authType;
    private List<HeaderDto> headers;
    private List<ParameterDto> parameters;
    private BodyDto body;
    private AuthConfigDto authConfig;
    private String preRequestScript;
    private String tests;
    private String createdAt;
    private String updatedAt;
    private String collectionId;
    private String folderId;
    private boolean isSaved;
}