package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSourceObjectDTO {

    // Original object info
    private String objectName;
    private String objectType;
    private String owner;
    private String operation; // SELECT, INSERT, UPDATE, DELETE, EXECUTE

    private String SchemaName;

    // For synonyms - target object info
    private Boolean isSynonym;
    private String targetType;
    private String targetName;
    private String targetOwner;

    // For packages - specific procedure/function within package
    private String packageProcedure;
    private String packageProcedureType; // PROCEDURE or FUNCTION

    // Object metadata
    private Integer columnCount;
    private Integer parameterCount;
    private String status;
    private String comments;

    // Additional configuration
    private Boolean usePrimaryKeyAsPathParam;
    private Boolean enablePagination;
    private Integer defaultPageSize;

    // Helper method to get effective object type (resolves synonyms)
    public String getEffectiveObjectType() {
        if (Boolean.TRUE.equals(isSynonym) && targetType != null) {
            return targetType;
        }
        return objectType;
    }

    // Helper method to get effective object name (resolves synonyms)
    public String getEffectiveObjectName() {
        if (Boolean.TRUE.equals(isSynonym) && targetName != null) {
            return targetName;
        }
        return objectName;
    }

    // Helper method to get effective owner (resolves synonyms)
    public String getEffectiveOwner() {
        if (Boolean.TRUE.equals(isSynonym) && targetOwner != null) {
            return targetOwner;
        }
        return owner;
    }

    // Check if this is a DML operation (INSERT/UPDATE/DELETE)
    public boolean isDmlOperation() {
        return "INSERT".equals(operation) || "UPDATE".equals(operation) || "DELETE".equals(operation);
    }

    // Check if this is a query operation (SELECT)
    public boolean isQueryOperation() {
        return "SELECT".equals(operation);
    }

    // Check if this is an execution operation (EXECUTE)
    public boolean isExecuteOperation() {
        return "EXECUTE".equals(operation);
    }

    // Get HTTP method based on operation
    public String getHttpMethod() {
        switch (operation) {
            case "SELECT": return "GET";
            case "INSERT": return "POST";
            case "UPDATE": return "PUT";
            case "DELETE": return "DELETE";
            case "EXECUTE": return "POST";
            default: return "GET";
        }
    }
}