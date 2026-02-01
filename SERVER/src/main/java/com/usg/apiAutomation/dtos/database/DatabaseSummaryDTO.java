package com.usg.apiAutomation.dtos.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSummaryDTO {
    private Map<String, Long> countsByType;
    private Map<String, Map<String, Long>> countsBySchema;
    private List<DatabaseObjectDTO> recentObjects;
    private List<DatabaseObjectDTO> invalidObjects;
    private Map<String, Long> topSchemas;
}