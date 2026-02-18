package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportUsersResponseDTO {
    private String fileName;
    private String fileType;
    private int totalRecords;
    private int importedCount;
    private int failedCount;
    private List<Map<String, Object>> importedUsers;
    private List<Map<String, String>> importErrors;
    private Date importedAt;
    private String status;
    private Map<String, Object> summary;
}