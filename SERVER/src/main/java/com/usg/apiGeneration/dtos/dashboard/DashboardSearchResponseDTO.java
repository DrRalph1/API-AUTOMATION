package com.usg.apiGeneration.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardSearchResponseDTO {
    private String query;
    private List<DashboardSearchResultDTO> results;
    private int total;
}