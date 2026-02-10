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
public class ImportSpecResponse {
    private String source;
    private String format;
    private Date importedAt;
    private String status;
    private Map<String, Object> importData;
    private String collectionId;
    private String importId;
    private int endpointsImported;
    private int implementationsGenerated;
}