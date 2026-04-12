package com.usg.autoAPIGenerator.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardMockServersResponseDTO {
    private List<DashboardMockServerDTO> mockServers;
}