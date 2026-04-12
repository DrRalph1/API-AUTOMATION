package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSourceObjectDTO {

    // Original object info
    private String objectName;
    private String objectType;  // TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE, CUSTOM_QUERY
    private String owner;
    private String operation; // SELECT, INSERT, UPDATE, DELETE, EXECUTE

    private String schemaName;

    private String databaseType; // "oracle" or "postgresql"

    // For synonyms - target object info
    private Boolean isSynonym;
    private String targetType;
    private String targetName;
    private String targetOwner;

    // For packages - specific procedure/function within package
    private String packageProcedure;
    private String packageProcedureType; // PROCEDURE or FUNCTION

    // ============ NEW FIELDS FOR CUSTOM SELECT STATEMENTS ============
    // For custom SELECT statement (instead of table/view)
    private String customSelectStatement;  // The actual SELECT SQL query
    private String queryAlias;              // Alias for the query (used in API naming)
    private List<QueryColumnDTO> queryColumns;  // Parsed column metadata
    private List<String> sourceTables;      // Tables referenced in the query
    private String fromClause;              // Extracted FROM clause
    private String whereClause;             // Extracted WHERE clause
    private String groupByClause;           // Extracted GROUP BY clause
    private String havingClause;            // Extracted HAVING clause
    private String orderByClause;           // Extracted ORDER BY clause
    private Boolean isDynamicQuery;         // Whether query has dynamic parameters
    private List<String> parameterMarkers;  // Named parameters in the query (e.g., :paramName)

    // For parameter extraction from custom queries
    private List<ApiParameterDTO> extractedParameters;

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
        // If this is a custom query, return CUSTOM_QUERY as the effective type
        if (customSelectStatement != null && !customSelectStatement.trim().isEmpty()) {
            return "CUSTOM_QUERY";
        }
        if (Boolean.TRUE.equals(isSynonym) && targetType != null) {
            return targetType;
        }
        return objectType;
    }

    // Helper method to get effective object name (resolves synonyms)
    public String getEffectiveObjectName() {
        if (customSelectStatement != null && !customSelectStatement.trim().isEmpty()) {
            return queryAlias != null ? queryAlias : "custom_query";
        }
        if (Boolean.TRUE.equals(isSynonym) && targetName != null) {
            return targetName;
        }
        return objectName;
    }

    // Helper method to get effective owner (resolves synonyms)
    public String getEffectiveOwner() {
        if (customSelectStatement != null && !customSelectStatement.trim().isEmpty()) {
            return schemaName != null ? schemaName : "public";
        }
        if (Boolean.TRUE.equals(isSynonym) && targetOwner != null) {
            return targetOwner;
        }
        return owner;
    }

    // Check if this is a custom query
    public boolean isCustomQuery() {
        return customSelectStatement != null && !customSelectStatement.trim().isEmpty();
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