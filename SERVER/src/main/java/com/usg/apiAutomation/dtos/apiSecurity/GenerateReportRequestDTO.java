package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportRequestDTO {
    private String reportType;
    private String format;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> includeSections;
}