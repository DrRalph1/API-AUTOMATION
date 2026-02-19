package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDTO {
    private String name;
    private String type;
    private String in; // path, query, header, body
    private boolean required;
    private String description;
    private String defaultValue;
    private String example;
    private String format;

    // Additional constructors for convenience
    public ParameterDTO(String name, String type, String in, boolean required, String description) {
        this.name = name;
        this.type = type;
        this.in = in;
        this.required = required;
        this.description = description;
    }

    public ParameterDTO(String name, String type, String in, boolean required, String description, String example) {
        this.name = name;
        this.type = type;
        this.in = in;
        this.required = required;
        this.description = description;
        this.example = example;
    }
}