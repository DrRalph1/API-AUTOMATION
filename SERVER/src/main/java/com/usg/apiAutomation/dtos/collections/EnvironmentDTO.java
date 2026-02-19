package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class EnvironmentDTO {
    private String id;
    private String name;
    private boolean isActive;
    private List<VariableDTO> variables;
}