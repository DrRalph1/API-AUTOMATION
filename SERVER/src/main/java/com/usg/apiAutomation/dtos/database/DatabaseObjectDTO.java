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