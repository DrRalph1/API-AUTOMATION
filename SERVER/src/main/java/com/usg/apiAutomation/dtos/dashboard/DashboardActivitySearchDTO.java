package com.usg.apiAutomation.dtos.dashboard;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivitySearchDTO {

    private String user;
    private String actionType;
    private String priority;
    private String startDate;
    private String endDate;

    @Min(value = 1, message = "Page must be at least 1")
    @Builder.Default
    private int page = 1;

    @Min(value = 1, message = "Size must be at least 1")
    @Builder.Default
    private int size = 10;
}