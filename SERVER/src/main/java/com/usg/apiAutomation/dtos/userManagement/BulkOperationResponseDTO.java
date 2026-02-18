package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResponseDTO {
    private String operation;
    private int processedCount;
    private int failedCount;
    private List<String> processedUsers;
    private List<String> failedUsers;
    private Date completedAt;
    private Map<String, Object> summary;
}