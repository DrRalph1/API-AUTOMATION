package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivitiesResponseDTO {
    private List<ActivityDTO> activities;
    private int page;
    private int size;
    private int totalItems;
    private int totalPages;
    private String timestamp;

    // Constructor for easy instantiation
    public DashboardActivitiesResponseDTO(List<ActivityDTO> activities, int page, int size, int totalItems, int totalPages) {
        this.activities = activities;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}