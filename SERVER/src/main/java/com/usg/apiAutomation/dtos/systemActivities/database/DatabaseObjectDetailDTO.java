package com.usg.apiAutomation.dtos.systemActivities.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseObjectDetailDTO {
    private DatabaseObjectDTO objectInfo;
    private List<ParameterDTO> parameters;
    private String sourceCode;
    private List<DatabaseObjectDTO> dependencies;
    private Map<String, Object> usageStatistics;
}