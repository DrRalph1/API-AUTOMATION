package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiHeaderDTO {
    private String id;
    private String key;
    private String value;
    private Boolean required;
    private String description;
    private Boolean isRequestHeader;
    private Boolean isResponseHeader;
}