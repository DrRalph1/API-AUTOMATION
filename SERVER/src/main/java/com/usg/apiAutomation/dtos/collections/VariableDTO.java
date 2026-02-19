package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

@Data
public class VariableDTO {
    private String id;
    private String key;
    private String value;
    private String type;
    private boolean enabled;
}