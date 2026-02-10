package com.usg.apiAutomation.dtos.systemActivities.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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