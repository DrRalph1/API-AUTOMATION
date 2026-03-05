package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestConfigDTO {
    private String schemaType;
    private String sample;
    private Long maxSize;
    private Boolean validateSchema;
    private List<String> allowedMediaTypes;
    private String bodyType;
    private List<String> requiredFields;
}