package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class CreateCollectionDto {
    private String name;
    private String description;
    private List<VariableDto> variables;
}