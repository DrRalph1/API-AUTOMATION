package com.usg.apiAutomation.dtos.collections;

import lombok.Data;
import java.util.List;

@Data
public class RequestDTO {
    private String id;
    private String name;
    private String method;
    private String url;
    private String authType;
    private String description;
    private boolean isEditing;
    private String status;
    private String lastModified;
    private AuthConfigDTO auth;
    private List<ParameterDTO> params;
    private List<HeaderDTO> headers;
    private String body;
    private String tests;
    private String preRequestScript;
    private boolean isSaved;
    private String collectionId;
    private String folderId;
}