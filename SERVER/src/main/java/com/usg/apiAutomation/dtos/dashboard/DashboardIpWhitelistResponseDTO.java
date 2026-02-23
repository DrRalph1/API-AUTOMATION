package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardIpWhitelistResponseDTO {
    private List<DashboardIpWhitelistEntryDTO> entries;
    private Map<String, Object> analysis;
}