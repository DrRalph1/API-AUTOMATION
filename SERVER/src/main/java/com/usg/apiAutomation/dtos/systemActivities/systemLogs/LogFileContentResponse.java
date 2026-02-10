package com.usg.apiAutomation.dtos.systemActivities.systemLogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogFileContentResponse {
    private String filename;
    private String content;
    private String searchTerm;
}