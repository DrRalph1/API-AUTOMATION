package com.usg.apiGeneration.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardUserActivitiesResponseDTO {
    private List<ActivityDTO> activities;
}