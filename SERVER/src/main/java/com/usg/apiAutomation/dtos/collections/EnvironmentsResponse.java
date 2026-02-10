package com.usg.apiAutomation.dtos.collections;

import lombok.Data;

import java.util.List;

@Data
public class EnvironmentsResponse {
    private List<EnvironmentDto> environments;

    public EnvironmentsResponse(List<EnvironmentDto> environments) {
        this.environments = environments;
    }
}