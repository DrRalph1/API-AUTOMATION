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
public class DatabaseObjectDTO {
    private String owner;
    private String objectName;
    private String objectType;
    private String status;
    private LocalDateTime created;
    private LocalDateTime lastDdlTime;
    private String jobType;
    private String tableOwner;
    private String tableName;
    private String readOnly;
}