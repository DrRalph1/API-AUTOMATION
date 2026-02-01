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
public class DatabaseObjectSearchDTO {
    private String owner;
    private String objectType;
    private String status;
    private String searchTerm;
    private String tableName;
    private String jobType;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}