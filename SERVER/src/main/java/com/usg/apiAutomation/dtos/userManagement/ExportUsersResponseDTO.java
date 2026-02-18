package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportUsersResponseDTO {
    private String format;
    private int totalUsers;
    private Date exportedAt;
    private String status;
    private Map<String, Object> exportData;
}