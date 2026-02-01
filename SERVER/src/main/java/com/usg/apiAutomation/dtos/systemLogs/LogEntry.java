package com.usg.apiAutomation.dtos.systemLogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    private Long id;
    private Date timestamp;
    private String sourceIp;
    private String ruleId;
    private String message;
    private String severity;
    private String action;
    private String requestUrl;
    private String userAgent;
    private String logFile;
    private LogEntryDetails details;
}