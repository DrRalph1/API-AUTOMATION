package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class EnvironmentsResponseDTO {
    private List<EnvironmentDTO> environments;

    public EnvironmentsResponseDTO(List<EnvironmentDTO> environments) {
        this.environments = environments;
    }
}