package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardUsersResponseDTO {
    private List<DashboardUserDTO> users;
    private int total;
    private int page;
    private int pageSize;
    private int totalPages;
    private Map<String, Object> stats;
}