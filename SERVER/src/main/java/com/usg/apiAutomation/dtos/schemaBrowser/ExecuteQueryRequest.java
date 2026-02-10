package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteQueryRequest {
    private String query;
    private Integer timeoutSeconds;
    private Boolean readOnly;
    private Integer maxRows;
    private Map<String, Object> parameters;
    private String connectionId;
    private Boolean autoCommit;
    private Integer fetchSize;
    private String resultType; // JSON, XML, CSV
}