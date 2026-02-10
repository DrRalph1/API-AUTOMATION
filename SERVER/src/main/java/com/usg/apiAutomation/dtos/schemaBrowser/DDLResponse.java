package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DDLResponse {
    private String ddl;
    private String objectType;
    private String objectName;
    private String message;
    private Boolean success;
    private String formattedDDL;
    private String syntaxHighlighted;
    private Long generationTimeMs;
    private String lastUpdated;

    public DDLResponse(String ddl, String objectType, String objectName, String message) {
        this.ddl = ddl;
        this.objectType = objectType;
        this.objectName = objectName;
        this.message = message;
        this.success = true;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}