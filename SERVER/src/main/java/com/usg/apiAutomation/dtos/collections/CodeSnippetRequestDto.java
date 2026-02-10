package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class CodeSnippetRequestDto {
    private String language;
    private String method;
    private String url;
    private List<HeaderDto> headers;
    private String body;
}