package com.usg.apiAutomation.dtos.systemActivities.systemLogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogFileResponse {
    private String name;
    private String path;
    private String size;
    private LocalDateTime lastModified;
    private long entries;
    private String description;
    private String format;
    private String content;
}