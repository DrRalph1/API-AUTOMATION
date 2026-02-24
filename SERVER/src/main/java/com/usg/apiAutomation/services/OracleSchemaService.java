package com.usg.apiAutomation.services;

import com.usg.apiAutomation.repositories.oracle.SchemaBrowserRepository;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OracleSchemaService {

    private final SchemaBrowserRepository schemaBrowserRepository;
    private final LoggerUtil loggerUtil;

    // ==================== TABLE METHODS ====================

    /**
     * Get all tables from Oracle schema
     */
    public Map<String, Object> getAllTables(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle tables for user: {}", requestId, performedBy);
        loggerUtil.log("oracleSchema",
                "RequestEntity ID: " + requestId + ", Getting all Oracle tables for user: " + performedBy);

        try {
            List<Map<String, Object>> tables = schemaBrowserRepository.getAllTables();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle tables from schema: {}",
                    requestId, tables.size(), currentSchema);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle tables: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle tables: " + e.getMessage(), e);
        }
    }

    /**
     * Get tables from a specific Oracle schema
     */
    public Map<String, Object> getTablesBySchema(String requestId, HttpServletRequest req,
                                                 String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting tables from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> tables = schemaBrowserRepository.getTablesBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} tables from Oracle schema: {}",
                    requestId, tables.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle tables from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle tables from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific table
     */
    public Map<String, Object> getTableDetails(String requestId, HttpServletRequest req,
                                               String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> tableDetails = schemaBrowserRepository.getTableDetails(tableName);
            List<Map<String, Object>> columns = schemaBrowserRepository.getTableColumns(tableName);
            List<Map<String, Object>> constraints = schemaBrowserRepository.getTableConstraints(tableName);
            List<Map<String, Object>> indexes = schemaBrowserRepository.getTableIndexes(tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("tableInfo", tableDetails);
            result.put("columns", columns);
            result.put("constraints", constraints);
            result.put("indexes", indexes);
            result.put("columnCount", columns.size());
            result.put("constraintCount", constraints.size());
            result.put("indexCount", indexes.size());
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle table: {}, found {} columns",
                    requestId, tableName, columns.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle table {}: {}",
                    requestId, tableName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Search for tables by name pattern
     */
    public Map<String, Object> searchTables(String requestId, HttpServletRequest req,
                                            String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching Oracle tables with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> tables = schemaBrowserRepository.searchTables(searchPattern);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("currentSchema", currentSchema);
            result.put("totalCount", tables.size());
            result.put("searchPattern", searchPattern);
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} Oracle tables matching pattern: {}",
                    requestId, tables.size(), searchPattern);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching Oracle tables with pattern {}: {}",
                    requestId, searchPattern, e.getMessage());
            throw new RuntimeException("Failed to search Oracle tables: " + e.getMessage(), e);
        }
    }

    /**
     * Get table statistics
     */
    public Map<String, Object> getTableStatistics(String requestId, HttpServletRequest req,
                                                  String performedBy, String tableName) {
        log.info("RequestEntity ID: {}, Getting statistics for Oracle table: {} for user: {}",
                requestId, tableName, performedBy);

        try {
            Map<String, Object> statistics = schemaBrowserRepository.getTableStatistics(tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("tableName", tableName);
            result.put("statistics", statistics);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved statistics for Oracle table: {}", requestId, tableName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting statistics for Oracle table {}: {}",
                    requestId, tableName, e.getMessage());
            throw new RuntimeException("Failed to retrieve statistics for Oracle table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get tables with row count
     */
    public Map<String, Object> getTablesWithRowCount(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle tables with row count for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tables = schemaBrowserRepository.getTablesWithRowCount();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved row counts for {} Oracle tables", requestId, tables.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle tables with row count: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle tables with row count: " + e.getMessage(), e);
        }
    }

    /**
     * Get table count by tablespace
     */
    public Map<String, Object> getTableCountByTablespace(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle table count by tablespace for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> tablespaceStats = schemaBrowserRepository.getTableCountByTablespace();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tablespaceStats", tablespaceStats);
            result.put("totalTablespaces", tablespaceStats.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved tablespace statistics for {} tablespaces",
                    requestId, tablespaceStats.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle table count by tablespace: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle table count by tablespace: " + e.getMessage(), e);
        }
    }

    /**
     * Get recent tables
     */
    public Map<String, Object> getRecentTables(String requestId, HttpServletRequest req,
                                               String performedBy, int days) {
        log.info("RequestEntity ID: {}, Getting Oracle tables modified in last {} days for user: {}",
                requestId, days, performedBy);

        try {
            List<Map<String, Object>> tables = schemaBrowserRepository.getRecentTables(days);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("days", days);
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} Oracle tables modified in last {} days",
                    requestId, tables.size(), days);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting recent Oracle tables: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve recent Oracle tables: " + e.getMessage(), e);
        }
    }

    // ==================== VIEW METHODS ====================

    /**
     * Get all views from Oracle schema
     */
    public Map<String, Object> getAllViews(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle views for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> views = schemaBrowserRepository.getAllViews();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("views", views);
            result.put("totalCount", views.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle views", requestId, views.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle views: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle views: " + e.getMessage(), e);
        }
    }

    /**
     * Get views from a specific Oracle schema
     */
    public Map<String, Object> getViewsBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting views from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> views = schemaBrowserRepository.getViewsBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("views", views);
            result.put("totalCount", views.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} views from Oracle schema: {}",
                    requestId, views.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle views from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle views from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific view
     */
    public Map<String, Object> getViewDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String viewName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle view: {} for user: {}",
                requestId, viewName, performedBy);

        try {
            Map<String, Object> viewDetails = schemaBrowserRepository.getViewDetails(viewName);

            viewDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            viewDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle view: {}",
                    requestId, viewName);

            return viewDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle view {}: {}",
                    requestId, viewName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle view " + viewName + ": " + e.getMessage(), e);
        }
    }

    // ==================== PROCEDURE METHODS ====================

    /**
     * Get all procedures from Oracle schema
     */
    public Map<String, Object> getAllProcedures(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle procedures for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> procedures = schemaBrowserRepository.getAllProcedures();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("procedures", procedures);
            result.put("totalCount", procedures.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle procedures", requestId, procedures.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle procedures: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle procedures: " + e.getMessage(), e);
        }
    }

    /**
     * Get procedures from a specific Oracle schema
     */
    public Map<String, Object> getProceduresBySchema(String requestId, HttpServletRequest req,
                                                     String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting procedures from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> procedures = schemaBrowserRepository.getProceduresBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("procedures", procedures);
            result.put("totalCount", procedures.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} procedures from Oracle schema: {}",
                    requestId, procedures.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle procedures from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle procedures from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific procedure
     */
    public Map<String, Object> getProcedureDetails(String requestId, HttpServletRequest req,
                                                   String performedBy, String procedureName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle procedure: {} for user: {}",
                requestId, procedureName, performedBy);

        try {
            Map<String, Object> procedureDetails = schemaBrowserRepository.getProcedureDetails(procedureName);

            procedureDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            procedureDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle procedure: {}",
                    requestId, procedureName);

            return procedureDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle procedure {}: {}",
                    requestId, procedureName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle procedure " + procedureName + ": " + e.getMessage(), e);
        }
    }

    // ==================== FUNCTION METHODS ====================

    /**
     * Get all functions from Oracle schema
     */
    public Map<String, Object> getAllFunctions(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle functions for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> functions = schemaBrowserRepository.getAllFunctions();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("functions", functions);
            result.put("totalCount", functions.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle functions", requestId, functions.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle functions: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle functions: " + e.getMessage(), e);
        }
    }

    /**
     * Get functions from a specific Oracle schema
     */
    public Map<String, Object> getFunctionsBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting functions from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> functions = schemaBrowserRepository.getFunctionsBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("functions", functions);
            result.put("totalCount", functions.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} functions from Oracle schema: {}",
                    requestId, functions.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle functions from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle functions from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific function
     */
    public Map<String, Object> getFunctionDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String functionName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle function: {} for user: {}",
                requestId, functionName, performedBy);

        try {
            Map<String, Object> functionDetails = schemaBrowserRepository.getFunctionDetails(functionName);

            functionDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            functionDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle function: {}",
                    requestId, functionName);

            return functionDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle function {}: {}",
                    requestId, functionName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle function " + functionName + ": " + e.getMessage(), e);
        }
    }

    // ==================== PACKAGE METHODS ====================

    /**
     * Get all packages from Oracle schema
     */
    public Map<String, Object> getAllPackages(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle packages for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> packages = schemaBrowserRepository.getAllPackages();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("packages", packages);
            result.put("totalCount", packages.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle packages", requestId, packages.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle packages: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle packages: " + e.getMessage(), e);
        }
    }

    /**
     * Get packages from a specific Oracle schema
     */
    public Map<String, Object> getPackagesBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting packages from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> packages = schemaBrowserRepository.getPackagesBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("packages", packages);
            result.put("totalCount", packages.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} packages from Oracle schema: {}",
                    requestId, packages.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle packages from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle packages from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific package
     */
    public Map<String, Object> getPackageDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String packageName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle package: {} for user: {}",
                requestId, packageName, performedBy);

        try {
            Map<String, Object> packageDetails = schemaBrowserRepository.getPackageDetails(packageName);

            packageDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            packageDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle package: {}",
                    requestId, packageName);

            return packageDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle package {}: {}",
                    requestId, packageName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle package " + packageName + ": " + e.getMessage(), e);
        }
    }

    // ==================== TRIGGER METHODS ====================

    /**
     * Get all triggers from Oracle schema
     */
    public Map<String, Object> getAllTriggers(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle triggers for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> triggers = schemaBrowserRepository.getAllTriggers();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("triggers", triggers);
            result.put("totalCount", triggers.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle triggers", requestId, triggers.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle triggers: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle triggers: " + e.getMessage(), e);
        }
    }

    /**
     * Get triggers from a specific Oracle schema
     */
    public Map<String, Object> getTriggersBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting triggers from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> triggers = schemaBrowserRepository.getTriggersBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("triggers", triggers);
            result.put("totalCount", triggers.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} triggers from Oracle schema: {}",
                    requestId, triggers.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle triggers from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle triggers from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific trigger
     */
    public Map<String, Object> getTriggerDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String triggerName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle trigger: {} for user: {}",
                requestId, triggerName, performedBy);

        try {
            Map<String, Object> triggerDetails = schemaBrowserRepository.getTriggerDetails(triggerName);

            triggerDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            triggerDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle trigger: {}",
                    requestId, triggerName);

            return triggerDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle trigger {}: {}",
                    requestId, triggerName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle trigger " + triggerName + ": " + e.getMessage(), e);
        }
    }

    // ==================== SYNONYM METHODS ====================

    /**
     * Get all synonyms from Oracle schema
     */
    public Map<String, Object> getAllSynonyms(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle synonyms for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> synonyms = schemaBrowserRepository.getAllSynonyms();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("synonyms", synonyms);
            result.put("totalCount", synonyms.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle synonyms", requestId, synonyms.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle synonyms: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle synonyms: " + e.getMessage(), e);
        }
    }

    /**
     * Get synonyms from a specific Oracle schema
     */
    public Map<String, Object> getSynonymsBySchema(String requestId, HttpServletRequest req,
                                                   String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting synonyms from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> synonyms = schemaBrowserRepository.getSynonymsBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("synonyms", synonyms);
            result.put("totalCount", synonyms.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} synonyms from Oracle schema: {}",
                    requestId, synonyms.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle synonyms from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle synonyms from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific synonym
     */
    public Map<String, Object> getSynonymDetails(String requestId, HttpServletRequest req,
                                                 String performedBy, String synonymName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle synonym: {} for user: {}",
                requestId, synonymName, performedBy);

        try {
            Map<String, Object> synonymDetails = schemaBrowserRepository.getSynonymDetails(synonymName);

            synonymDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            synonymDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle synonym: {}",
                    requestId, synonymName);

            return synonymDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle synonym {}: {}",
                    requestId, synonymName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle synonym " + synonymName + ": " + e.getMessage(), e);
        }
    }

    // ==================== SEQUENCE METHODS ====================

    /**
     * Get all sequences from Oracle schema
     */
    public Map<String, Object> getAllSequences(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle sequences for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> sequences = schemaBrowserRepository.getAllSequences();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("sequences", sequences);
            result.put("totalCount", sequences.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle sequences", requestId, sequences.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle sequences: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle sequences: " + e.getMessage(), e);
        }
    }

    /**
     * Get sequences from a specific Oracle schema
     */
    public Map<String, Object> getSequencesBySchema(String requestId, HttpServletRequest req,
                                                    String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting sequences from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> sequences = schemaBrowserRepository.getSequencesBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("sequences", sequences);
            result.put("totalCount", sequences.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} sequences from Oracle schema: {}",
                    requestId, sequences.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle sequences from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle sequences from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific sequence
     */
    public Map<String, Object> getSequenceDetails(String requestId, HttpServletRequest req,
                                                  String performedBy, String sequenceName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle sequence: {} for user: {}",
                requestId, sequenceName, performedBy);

        try {
            Map<String, Object> sequenceDetails = schemaBrowserRepository.getSequenceDetails(sequenceName);

            sequenceDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            sequenceDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle sequence: {}",
                    requestId, sequenceName);

            return sequenceDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle sequence {}: {}",
                    requestId, sequenceName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle sequence " + sequenceName + ": " + e.getMessage(), e);
        }
    }

    // ==================== TYPE METHODS ====================

    /**
     * Get all types from Oracle schema
     */
    public Map<String, Object> getAllTypes(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle types for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> types = schemaBrowserRepository.getAllTypes();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("types", types);
            result.put("totalCount", types.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle types", requestId, types.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle types: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle types: " + e.getMessage(), e);
        }
    }

    /**
     * Get types from a specific Oracle schema
     */
    public Map<String, Object> getTypesBySchema(String requestId, HttpServletRequest req,
                                                String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting types from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> types = schemaBrowserRepository.getTypesBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("types", types);
            result.put("totalCount", types.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} types from Oracle schema: {}",
                    requestId, types.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle types from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle types from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific type
     */
    public Map<String, Object> getTypeDetails(String requestId, HttpServletRequest req,
                                              String performedBy, String typeName) {
        log.info("RequestEntity ID: {}, Getting details for Oracle type: {} for user: {}",
                requestId, typeName, performedBy);

        try {
            Map<String, Object> typeDetails = schemaBrowserRepository.getTypeDetails(typeName);

            typeDetails.put("generatedAt", java.time.LocalDateTime.now().toString());
            typeDetails.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved details for Oracle type: {}",
                    requestId, typeName);

            return typeDetails;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting details for Oracle type {}: {}",
                    requestId, typeName, e.getMessage());
            throw new RuntimeException("Failed to retrieve details for Oracle type " + typeName + ": " + e.getMessage(), e);
        }
    }

    // ==================== DATABASE LINK METHODS ====================

    /**
     * Get all database links from Oracle schema
     */
    public Map<String, Object> getAllDbLinks(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle database links for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> dbLinks = schemaBrowserRepository.getAllDbLinks();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("dbLinks", dbLinks);
            result.put("totalCount", dbLinks.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle database links", requestId, dbLinks.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle database links: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle database links: " + e.getMessage(), e);
        }
    }

    /**
     * Get database links from a specific Oracle schema
     */
    public Map<String, Object> getDbLinksBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting database links from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> dbLinks = schemaBrowserRepository.getDbLinksBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("dbLinks", dbLinks);
            result.put("totalCount", dbLinks.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} database links from Oracle schema: {}",
                    requestId, dbLinks.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle database links from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle database links from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    // ==================== GENERAL OBJECT METHODS ====================

    /**
     * Get all objects from Oracle schema
     */
    public Map<String, Object> getAllObjects(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle objects for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objects = schemaBrowserRepository.getAllObjects();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle objects", requestId, objects.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle objects: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get objects from a specific Oracle schema
     */
    public Map<String, Object> getObjectsBySchema(String requestId, HttpServletRequest req,
                                                  String performedBy, String schemaName) {
        log.info("RequestEntity ID: {}, Getting objects from Oracle schema: {} for user: {}",
                requestId, schemaName, performedBy);

        try {
            List<Map<String, Object>> objects = schemaBrowserRepository.getObjectsBySchema(schemaName);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("schema", schemaName);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} objects from Oracle schema: {}",
                    requestId, objects.size(), schemaName);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle objects from schema {}: {}",
                    requestId, schemaName, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle objects from schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Search for objects by name pattern
     */
    public Map<String, Object> searchObjects(String requestId, HttpServletRequest req,
                                             String performedBy, String searchPattern) {
        log.info("RequestEntity ID: {}, Searching Oracle objects with pattern: {} for user: {}",
                requestId, searchPattern, performedBy);

        try {
            List<Map<String, Object>> objects = schemaBrowserRepository.searchObjects(searchPattern);

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objects", objects);
            result.put("totalCount", objects.size());
            result.put("searchPattern", searchPattern);
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Found {} Oracle objects matching pattern: {}",
                    requestId, objects.size(), searchPattern);

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error searching Oracle objects with pattern {}: {}",
                    requestId, searchPattern, e.getMessage());
            throw new RuntimeException("Failed to search Oracle objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get object count by type
     */
    public Map<String, Object> getObjectCountByType(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting Oracle object count by type for user: {}", requestId, performedBy);

        try {
            List<Map<String, Object>> objectCounts = schemaBrowserRepository.getObjectCountByType();

            // Get the actual schema name from the repository
            String currentSchema = schemaBrowserRepository.getCurrentSchema();

            Map<String, Object> result = new HashMap<>();
            result.put("objectCounts", objectCounts);
            result.put("totalCount", objectCounts.size());
            result.put("database", "Oracle");
            result.put("schema", currentSchema);
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved object counts for {} object types", requestId, objectCounts.size());

            return result;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error getting Oracle object count by type: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Oracle object count by type: " + e.getMessage(), e);
        }
    }

    // ==================== DIAGNOSTIC METHODS ====================

    /**
     * Run comprehensive database diagnostics
     */
    public Map<String, Object> diagnoseDatabase(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Running Oracle database diagnostics for user: {}", requestId, performedBy);

        try {
            Map<String, Object> diagnostics = schemaBrowserRepository.diagnoseDatabase();

            diagnostics.put("generatedAt", java.time.LocalDateTime.now().toString());
            diagnostics.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Database diagnostics completed with status: {}",
                    requestId, diagnostics.get("diagnosticStatus"));

            return diagnostics;

        } catch (Exception e) {
            log.error("RequestEntity ID: {}, Error diagnosing Oracle database: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to diagnose Oracle database: " + e.getMessage(), e);
        }
    }
}