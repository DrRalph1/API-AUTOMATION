package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardDocumentationResponseDTO {
    private List<DashboardDocumentationDTO> documentation;
}