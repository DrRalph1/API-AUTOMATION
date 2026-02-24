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

    /**
     * Get all tables from Oracle schema
     */
    public Map<String, Object> getAllTables(String requestId, HttpServletRequest req, String performedBy) {
        log.info("RequestEntity ID: {}, Getting all Oracle tables for user: {}", requestId, performedBy);
        loggerUtil.log("oracleSchema",
                "RequestEntity ID: " + requestId + ", Getting all Oracle tables for user: " + performedBy);

        try {
            List<Map<String, Object>> tables = schemaBrowserRepository.getAllTables();

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
            result.put("schema", "Current User");
            result.put("generatedAt", java.time.LocalDateTime.now().toString());
            result.put("generatedBy", performedBy);

            log.info("RequestEntity ID: {}, Retrieved {} Oracle tables", requestId, tables.size());

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

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
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

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("searchPattern", searchPattern);
            result.put("database", "Oracle");
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

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("database", "Oracle");
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

            Map<String, Object> result = new HashMap<>();
            result.put("tablespaceStats", tablespaceStats);
            result.put("totalTablespaces", tablespaceStats.size());
            result.put("database", "Oracle");
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

            Map<String, Object> result = new HashMap<>();
            result.put("tables", tables);
            result.put("totalCount", tables.size());
            result.put("days", days);
            result.put("database", "Oracle");
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
}