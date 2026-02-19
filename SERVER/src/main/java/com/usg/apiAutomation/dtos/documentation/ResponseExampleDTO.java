package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseExampleDTO {
    private int statusCode;
    private String description;
    private String example;
    private String contentType;
    private List<HeaderDTO> headers;
    private String schema;
}