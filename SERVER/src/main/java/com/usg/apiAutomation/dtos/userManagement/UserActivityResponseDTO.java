package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponseDTO {
    private String userId;
    private List<ActivityLogDTO> activities;
    private int totalActivities;
    private Date generatedAt;
    private Map<String, Object> activityStats;
}