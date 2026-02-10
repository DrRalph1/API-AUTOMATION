package com.usg.apiAutomation.dtos.systemActivities.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDTO {
    private String parameterName;
    private String dataType;
    private String inOut;
    private Integer position;
    private String defaultValue;
}