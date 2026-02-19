package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class ParameterDTO {
    private String id;
    private String key;
    private String value;
    private String description;
    private boolean enabled;
}