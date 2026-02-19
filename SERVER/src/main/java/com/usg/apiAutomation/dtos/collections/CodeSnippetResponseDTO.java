package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class CodeSnippetResponseDTO {
    private String code;
    private String language;
    private String message;

    public CodeSnippetResponseDTO(String code, String language, String message) {
        this.code = code;
        this.language = language;
        this.message = message;
    }
}