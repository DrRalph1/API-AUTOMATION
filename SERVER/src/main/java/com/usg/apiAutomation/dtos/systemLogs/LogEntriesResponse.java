package com.usg.apiAutomation.dtos.systemLogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntriesResponse {
    private List<LogEntry> logs;
    private int currentPage;
    private int pageSize;
    private int totalItems;
    private int totalPages;
}