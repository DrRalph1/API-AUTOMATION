package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class CodeSnippetRequestDTO {
    private String language;
    private String method;
    private String url;
    private List<HeaderDTO> headers;
    private String body;
}