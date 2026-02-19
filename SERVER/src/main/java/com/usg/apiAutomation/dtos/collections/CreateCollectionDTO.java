package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class CreateCollectionDTO {
    private String name;
    private String description;
    private List<VariableDTO> variables;
}