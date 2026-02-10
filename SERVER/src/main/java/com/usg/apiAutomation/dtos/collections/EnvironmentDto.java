package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class EnvironmentDto {
    private String id;
    private String name;
    private boolean isActive;
    private List<VariableDto> variables;
}