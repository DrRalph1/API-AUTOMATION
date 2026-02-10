package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterItem {
    private String key;
    private String name;
    private String value;
    private String description;
    private Boolean required;
    private String type; // query, path, header, etc.
}