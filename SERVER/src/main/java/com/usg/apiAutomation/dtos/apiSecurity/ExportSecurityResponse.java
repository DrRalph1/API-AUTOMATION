package com.usg.apiAutomation.dtos.apiSecurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportSecurityResponse {
    private String exportId;
    private String format;
    private String status;
    private String exportedAt;
    private Map<String, Object> exportInfo;
}