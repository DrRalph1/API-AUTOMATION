package com.usg.apiAutomation.dtos.codeBase;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponse {
    private String format;
    private String language;
    private Date exportedAt;
    private String status;
    private Map<String, Object> exportData;
    private String downloadUrl;
    private String fileSize;
    private String exportId;
}