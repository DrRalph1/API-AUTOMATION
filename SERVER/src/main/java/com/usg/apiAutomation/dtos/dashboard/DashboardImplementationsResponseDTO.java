package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardImplementationsResponseDTO {
    private List<DashboardImplementationDTO> implementations;
    private int total;
    private int page;
    private int pageSize;
    private int totalPages;
}