package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaErrorResponse {
    private String errorCode;
    private String errorMessage;
    private String detailedMessage;
    private String timestamp;
    private String requestId;
    private String path;
    private String objectType;
    private String objectName;
    private String connectionId;
    private String sqlState;
    private Integer vendorCode;

    public SchemaErrorResponse(String errorCode, String errorMessage, String requestId) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.requestId = requestId;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}