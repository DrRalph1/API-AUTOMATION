package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportSecurityRequestDTO {
    private String format;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> includeData;
}