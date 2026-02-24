package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Repository
public class SchemaBrowserRepository {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(SchemaBrowserRepository.class);

    // ==================== EXISTING TABLE METHODS ====================
    // (All existing table methods remain unchanged)

    /**
     * Get all tables from the current Oracle schema with enhanced diagnostics
     * Using actual Oracle system tables: USER_TABLES and USER_OBJECTS
     */
    public List<Map<String, Object>> getAllTables() {
        try {
            // Log the current user for debugging
            String currentUser = getCurrentUser();
            log.info("Getting all tables for current user: {}", currentUser);

            // Check if user has any tables
            Integer tableCount = getUserTableCount();
            log.info("User {} has {} tables in their schema", currentUser, tableCount);

            // If no tables in user schema, check what's accessible in all_tables
            if (tableCount == 0) {
                log.warn("No tables found in user schema. Checking accessible tables...");
                List<String> accessibleOwners = getAccessibleOwners();
                log.info("Users with accessible tables: {}", accessibleOwners);

                // Check if EMPLOYEES table exists anywhere
                checkSpecificTable("EMPLOYEES");
            }

            // Get the tables
            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    t.tablespace_name, " +
                    "    t.status, " +
                    "    t.num_rows, " +
                    "    t.avg_row_len, " +
                    "    t.blocks, " +
                    "    t.last_analyzed, " +
                    "    o.created " +
                    "FROM user_tables t " +
                    "LEFT JOIN user_objects o ON t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "ORDER BY t.table_name";

            log.debug("Executing query: {}", sql);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql);
            log.info("Query returned {} tables", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error in getAllTables: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tables: " + e.getMessage(), e);
        }
    }

    /**
     * Get the current schema name
     */
    public String getCurrentSchema() {
        try {
            // Try to get current schema first (this gives the actual schema being used)
            String schema = oracleJdbcTemplate.queryForObject(
                    "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL",
                    String.class
            );
            log.info("Current schema: {}", schema);
            return schema;
        } catch (Exception e) {
            log.warn("Failed to get CURRENT_SCHEMA, falling back to USER: {}", e.getMessage());
            try {
                // Fallback to USER
                String user = oracleJdbcTemplate.queryForObject(
                        "SELECT USER FROM DUAL",
                        String.class
                );
                log.info("Current user (fallback): {}", user);
                return user;
            } catch (Exception ex) {
                log.error("Failed to get current user/schema: {}", ex.getMessage());
                return "UNKNOWN";
            }
        }
    }

    /**
     * Get all tables from a specific schema
     * Using actual Oracle system tables: ALL_TABLES and ALL_OBJECTS
     */
    public List<Map<String, Object>> getTablesBySchema(String schemaName) {
        try {
            log.info("Getting tables for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    t.owner, " +
                    "    t.tablespace_name, " +
                    "    t.status, " +
                    "    t.num_rows, " +
                    "    t.avg_row_len, " +
                    "    t.blocks, " +
                    "    t.last_analyzed, " +
                    "    o.created " +
                    "FROM all_tables t " +
                    "LEFT JOIN all_objects o ON t.owner = o.owner AND t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.table_name";

            log.debug("Executing query for schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} tables in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getTablesBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tables for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get detailed information about a specific table
     */
    public Map<String, Object> getTableDetails(String tableName) {
        try {
            log.info("Getting details for table: {}", tableName);

            // First check if table exists and where
            Map<String, Object> tableLocation = findTableLocation(tableName);
            if (tableLocation.isEmpty()) {
                log.warn("Table {} not found in any accessible schema", tableName);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("table_name", tableName);
                emptyResult.put("message", "Table not found");
                return emptyResult;
            }

            String owner = (String) tableLocation.get("owner");
            log.info("Table {} found in schema {}", tableName, owner);

            String sql;
            if (owner.equals(getCurrentUser())) {
                // Table owned by current user
                sql = "SELECT " +
                        "    t.table_name, " +
                        "    t.tablespace_name, " +
                        "    t.status, " +
                        "    t.num_rows, " +
                        "    t.avg_row_len, " +
                        "    t.blocks, " +
                        "    t.last_analyzed, " +
                        "    o.created, " +
                        "    (SELECT COUNT(*) FROM user_tab_columns WHERE table_name = t.table_name) as column_count, " +
                        "    (SELECT COUNT(*) FROM user_constraints WHERE table_name = t.table_name AND constraint_type = 'P') as primary_key_count, " +
                        "    (SELECT COUNT(*) FROM user_constraints WHERE table_name = t.table_name AND constraint_type = 'R') as foreign_key_count, " +
                        "    (SELECT COUNT(*) FROM user_indexes WHERE table_name = t.table_name) as index_count " +
                        "FROM user_tables t " +
                        "LEFT JOIN user_objects o ON t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                        "WHERE UPPER(t.table_name) = UPPER(?)";
            } else {
                // Table owned by different user - use all_* views
                sql = "SELECT " +
                        "    t.table_name, " +
                        "    t.owner, " +
                        "    t.tablespace_name, " +
                        "    t.status, " +
                        "    t.num_rows, " +
                        "    t.avg_row_len, " +
                        "    t.blocks, " +
                        "    t.last_analyzed, " +
                        "    o.created, " +
                        "    (SELECT COUNT(*) FROM all_tab_columns WHERE owner = t.owner AND table_name = t.table_name) as column_count " +
                        "FROM all_tables t " +
                        "LEFT JOIN all_objects o ON t.owner = o.owner AND t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                        "WHERE UPPER(t.table_name) = UPPER(?) AND UPPER(t.owner) = UPPER(?)";

                log.debug("Executing query for table details with owner: {}", owner);
                return oracleJdbcTemplate.queryForMap(sql, tableName, owner);
            }

            log.debug("Executing query for table details: {}", tableName);
            return oracleJdbcTemplate.queryForMap(sql, tableName);

        } catch (Exception e) {
            log.error("Error in getTableDetails for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get columns for a specific table with statistics
     */
    public List<Map<String, Object>> getTableColumns(String tableName) {
        try {
            log.info("Getting columns for table: {}", tableName);

            // Check if table exists and get owner
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");

            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                // Use all_* views for tables in other schemas
                sql = "SELECT " +
                        "    c.column_id, " +
                        "    c.column_name, " +
                        "    c.data_type, " +
                        "    c.data_length, " +
                        "    c.data_precision, " +
                        "    c.data_scale, " +
                        "    c.nullable, " +
                        "    c.default_length, " +
                        "    c.data_default, " +
                        "    c.char_length, " +
                        "    c.char_used " +
                        "FROM all_tab_columns c " +
                        "WHERE UPPER(c.table_name) = UPPER(?) AND UPPER(c.owner) = UPPER(?) " +
                        "ORDER BY c.column_id";

                return oracleJdbcTemplate.queryForList(sql, tableName, owner);
            } else {
                // Use user_* views for own tables
                sql = "SELECT " +
                        "    c.column_id, " +
                        "    c.column_name, " +
                        "    c.data_type, " +
                        "    c.data_length, " +
                        "    c.data_precision, " +
                        "    c.data_scale, " +
                        "    c.nullable, " +
                        "    c.default_length, " +
                        "    c.data_default, " +
                        "    s.num_distinct, " +
                        "    s.density, " +
                        "    s.num_nulls, " +
                        "    s.last_analyzed, " +
                        "    c.char_length, " +
                        "    c.char_used " +
                        "FROM user_tab_columns c " +
                        "LEFT JOIN user_tab_col_statistics s " +
                        "    ON c.table_name = s.table_name " +
                        "    AND c.column_name = s.column_name " +
                        "WHERE UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.column_id";

                return oracleJdbcTemplate.queryForList(sql, tableName);
            }

        } catch (Exception e) {
            log.error("Error in getTableColumns for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve columns for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get constraints for a specific table
     */
    public List<Map<String, Object>> getTableConstraints(String tableName) {
        try {
            log.info("Getting constraints for table: {}", tableName);

            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");

            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    c.constraint_name, " +
                        "    c.constraint_type, " +
                        "    c.status, " +
                        "    c.deferrable, " +
                        "    c.deferred, " +
                        "    c.validated, " +
                        "    c.r_owner as references_owner, " +
                        "    c.r_constraint_name as references_constraint, " +
                        "    c.delete_rule, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY position) " +
                        "     FROM all_cons_columns " +
                        "     WHERE owner = c.owner AND constraint_name = c.constraint_name) as columns " +
                        "FROM all_constraints c " +
                        "WHERE UPPER(c.table_name) = UPPER(?) AND UPPER(c.owner) = UPPER(?) " +
                        "ORDER BY c.constraint_type, c.constraint_name";

                return oracleJdbcTemplate.queryForList(sql, tableName, owner);
            } else {
                sql = "SELECT " +
                        "    c.constraint_name, " +
                        "    c.constraint_type, " +
                        "    c.status, " +
                        "    c.deferrable, " +
                        "    c.deferred, " +
                        "    c.validated, " +
                        "    c.r_owner as references_owner, " +
                        "    c.r_constraint_name as references_constraint, " +
                        "    c.delete_rule, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY position) " +
                        "     FROM user_cons_columns " +
                        "     WHERE constraint_name = c.constraint_name) as columns " +
                        "FROM user_constraints c " +
                        "WHERE UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.constraint_type, c.constraint_name";

                return oracleJdbcTemplate.queryForList(sql, tableName);
            }

        } catch (Exception e) {
            log.error("Error in getTableConstraints for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve constraints for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get indexes for a specific table
     */
    public List<Map<String, Object>> getTableIndexes(String tableName) {
        try {
            log.info("Getting indexes for table: {}", tableName);

            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");

            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    i.index_name, " +
                        "    i.index_type, " +
                        "    i.uniqueness, " +
                        "    i.tablespace_name, " +
                        "    i.status, " +
                        "    i.visibility, " +
                        "    i.distinct_keys, " +
                        "    i.leaf_blocks, " +
                        "    i.clustering_factor, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY column_position) " +
                        "     FROM all_ind_columns " +
                        "     WHERE index_owner = i.owner AND index_name = i.index_name) as columns " +
                        "FROM all_indexes i " +
                        "WHERE UPPER(i.table_name) = UPPER(?) AND UPPER(i.owner) = UPPER(?) " +
                        "ORDER BY i.index_name";

                return oracleJdbcTemplate.queryForList(sql, tableName, owner);
            } else {
                sql = "SELECT " +
                        "    i.index_name, " +
                        "    i.index_type, " +
                        "    i.uniqueness, " +
                        "    i.tablespace_name, " +
                        "    i.status, " +
                        "    i.visibility, " +
                        "    i.distinct_keys, " +
                        "    i.leaf_blocks, " +
                        "    i.clustering_factor, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY column_position) " +
                        "     FROM user_ind_columns " +
                        "     WHERE index_name = i.index_name) as columns " +
                        "FROM user_indexes i " +
                        "WHERE UPPER(i.table_name) = UPPER(?) " +
                        "ORDER BY i.index_name";

                return oracleJdbcTemplate.queryForList(sql, tableName);
            }

        } catch (Exception e) {
            log.error("Error in getTableIndexes for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve indexes for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get table statistics
     */
    public Map<String, Object> getTableStatistics(String tableName) {
        try {
            log.info("Getting statistics for table: {}", tableName);

            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");

            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    num_rows, " +
                        "    blocks, " +
                        "    empty_blocks, " +
                        "    avg_space, " +
                        "    chain_cnt, " +
                        "    avg_row_len, " +
                        "    sample_size, " +
                        "    last_analyzed, " +
                        "    global_stats, " +
                        "    user_stats " +
                        "FROM all_tab_statistics " +
                        "WHERE UPPER(table_name) = UPPER(?) AND UPPER(owner) = UPPER(?)";

                return oracleJdbcTemplate.queryForMap(sql, tableName, owner);
            } else {
                sql = "SELECT " +
                        "    num_rows, " +
                        "    blocks, " +
                        "    empty_blocks, " +
                        "    avg_space, " +
                        "    chain_cnt, " +
                        "    avg_row_len, " +
                        "    avg_space_freelist_blocks, " +
                        "    num_freelist_blocks, " +
                        "    avg_cached_blocks, " +
                        "    avg_cache_hit_ratio, " +
                        "    sample_size, " +
                        "    last_analyzed, " +
                        "    global_stats, " +
                        "    user_stats " +
                        "FROM user_tab_statistics " +
                        "WHERE UPPER(table_name) = UPPER(?)";

                return oracleJdbcTemplate.queryForMap(sql, tableName);
            }

        } catch (Exception e) {
            log.error("Error in getTableStatistics for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve statistics for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Search for tables by name pattern across all accessible schemas
     */
    public List<Map<String, Object>> searchTables(String searchPattern) {
        try {
            log.info("Searching for tables with pattern: {}", searchPattern);

            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    t.owner, " +
                    "    t.tablespace_name, " +
                    "    t.status, " +
                    "    t.num_rows, " +
                    "    t.last_analyzed, " +
                    "    o.created " +
                    "FROM all_tables t " +
                    "LEFT JOIN all_objects o ON t.owner = o.owner AND t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "WHERE UPPER(t.table_name) LIKE UPPER(?) " +
                    "ORDER BY t.owner, t.table_name";

            log.debug("Executing search query with pattern: {}", searchPattern);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, "%" + searchPattern + "%");
            log.info("Search returned {} tables", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error in searchTables for pattern {}: {}", searchPattern, e.getMessage(), e);
            throw new RuntimeException("Failed to search tables with pattern " + searchPattern + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get tables with row count estimate (from current user only)
     */
    public List<Map<String, Object>> getTablesWithRowCount() {
        try {
            String sql = "SELECT " +
                    "    table_name, " +
                    "    num_rows, " +
                    "    last_analyzed " +
                    "FROM user_tables " +
                    "ORDER BY num_rows DESC NULLS LAST";

            log.debug("Executing query for tables with row count");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getTablesWithRowCount: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tables with row count: " + e.getMessage(), e);
        }
    }

    /**
     * Get table count by tablespace (from current user only)
     */
    public List<Map<String, Object>> getTableCountByTablespace() {
        try {
            String sql = "SELECT " +
                    "    tablespace_name, " +
                    "    COUNT(*) as table_count, " +
                    "    SUM(num_rows) as total_rows, " +
                    "    SUM(blocks) as total_blocks " +
                    "FROM user_tables " +
                    "WHERE tablespace_name IS NOT NULL " +
                    "GROUP BY tablespace_name " +
                    "ORDER BY tablespace_name";

            log.debug("Executing query for tablespace statistics");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getTableCountByTablespace: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tablespace statistics: " + e.getMessage(), e);
        }
    }

    /**
     * Get recently created/modified tables (from current user only)
     */
    public List<Map<String, Object>> getRecentTables(int days) {
        try {
            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    o.created, " +
                    "    o.last_ddl_time " +
                    "FROM user_tables t " +
                    "JOIN user_objects o ON t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "WHERE (o.created > SYSDATE - ? OR o.last_ddl_time > SYSDATE - ?) " +
                    "ORDER BY o.last_ddl_time DESC";

            log.debug("Executing query for recent tables (last {} days)", days);
            return oracleJdbcTemplate.queryForList(sql, days, days);

        } catch (Exception e) {
            log.error("Error in getRecentTables: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recent tables: " + e.getMessage(), e);
        }
    }

    // ==================== NEW METHODS FOR OTHER OBJECT TYPES ====================

    /**
     * Get all views in the current schema
     */
    public List<Map<String, Object>> getAllViews() {
        try {
            String sql = "SELECT " +
                    "    view_name, " +
                    "    text_length, " +
                    "    text, " +
                    "    read_only, " +
                    "    created, " +
                    "    last_ddl_time " +
                    "FROM user_views " +
                    "ORDER BY view_name";

            log.debug("Executing query for all views");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllViews: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve views: " + e.getMessage(), e);
        }
    }

    /**
     * Get all views from a specific schema
     */
    public List<Map<String, Object>> getViewsBySchema(String schemaName) {
        try {
            log.info("Getting views for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    v.owner, " +
                    "    v.view_name, " +
                    "    v.text_length, " +
                    "    v.text, " +
                    "    v.read_only, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status " +
                    "FROM all_views v " +
                    "JOIN all_objects o ON v.owner = o.owner AND v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                    "WHERE UPPER(v.owner) = UPPER(?) " +
                    "ORDER BY v.view_name";

            log.debug("Executing query for views in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} views in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getViewsBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve views for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get view details including columns
     */
    public Map<String, Object> getViewDetails(String viewName) {
        try {
            log.info("Getting details for view: {}", viewName);

            // Find view location
            Map<String, Object> viewLocation = findObjectLocation(viewName, "VIEW");
            if (viewLocation.isEmpty()) {
                log.warn("View {} not found", viewName);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("view_name", viewName);
                emptyResult.put("message", "View not found");
                return emptyResult;
            }

            String owner = (String) viewLocation.get("owner");

            // Get view definition
            String viewSql;
            List<Map<String, Object>> columns;

            if (owner.equals(getCurrentUser())) {
                viewSql = "SELECT view_name, text, read_only, created, last_ddl_time " +
                        "FROM user_views v " +
                        "JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                        "WHERE UPPER(v.view_name) = UPPER(?)";

                columns = getViewColumns(viewName, owner);
            } else {
                viewSql = "SELECT v.owner, v.view_name, v.text, v.read_only, " +
                        "o.created, o.last_ddl_time, o.status " +
                        "FROM all_views v " +
                        "JOIN all_objects o ON v.owner = o.owner AND v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                        "WHERE UPPER(v.view_name) = UPPER(?) AND UPPER(v.owner) = UPPER(?)";

                columns = getViewColumns(viewName, owner);
            }

            Map<String, Object> viewInfo = owner.equals(getCurrentUser())
                    ? oracleJdbcTemplate.queryForMap(viewSql, viewName)
                    : oracleJdbcTemplate.queryForMap(viewSql, viewName, owner);

            Map<String, Object> result = new HashMap<>();
            result.put("viewInfo", viewInfo);
            result.put("columns", columns);
            result.put("columnCount", columns.size());

            return result;

        } catch (Exception e) {
            log.error("Error in getViewDetails for {}: {}", viewName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for view " + viewName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get columns for a view
     */
    public List<Map<String, Object>> getViewColumns(String viewName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    column_id, " +
                        "    column_name, " +
                        "    data_type, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    nullable " +
                        "FROM all_tab_columns " +
                        "WHERE UPPER(table_name) = UPPER(?) AND UPPER(owner) = UPPER(?) " +
                        "ORDER BY column_id";

                return oracleJdbcTemplate.queryForList(sql, viewName, owner);
            } else {
                sql = "SELECT " +
                        "    column_id, " +
                        "    column_name, " +
                        "    data_type, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    nullable " +
                        "FROM user_tab_columns " +
                        "WHERE UPPER(table_name) = UPPER(?) " +
                        "ORDER BY column_id";

                return oracleJdbcTemplate.queryForList(sql, viewName);
            }
        } catch (Exception e) {
            log.error("Error getting view columns: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all procedures in the current schema
     */
    public List<Map<String, Object>> getAllProcedures() {
        try {
            String sql = "SELECT " +
                    "    object_name as procedure_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    (SELECT COUNT(*) FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL) as parameter_count " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'PROCEDURE' " +
                    "ORDER BY object_name";

            log.debug("Executing query for all procedures");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllProcedures: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedures: " + e.getMessage(), e);
        }
    }

    /**
     * Get all procedures from a specific schema
     */
    public List<Map<String, Object>> getProceduresBySchema(String schemaName) {
        try {
            log.info("Getting procedures for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as procedure_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    (SELECT COUNT(*) FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL) as parameter_count " +
                    "FROM all_objects o " +
                    "WHERE o.object_type = 'PROCEDURE' AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name";

            log.debug("Executing query for procedures in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} procedures in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getProceduresBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedures for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get procedure details including parameters
     */
    public Map<String, Object> getProcedureDetails(String procedureName) {
        try {
            log.info("Getting details for procedure: {}", procedureName);

            // Find procedure location
            Map<String, Object> procLocation = findObjectLocation(procedureName, "PROCEDURE");
            if (procLocation.isEmpty()) {
                log.warn("Procedure {} not found", procedureName);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("procedure_name", procedureName);
                emptyResult.put("message", "Procedure not found");
                return emptyResult;
            }

            String owner = (String) procLocation.get("owner");

            // Get procedure info
            String procSql;
            List<Map<String, Object>> parameters;

            if (owner.equals(getCurrentUser())) {
                procSql = "SELECT " +
                        "    object_name as procedure_name, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    status, " +
                        "    'PROCEDURE' as object_type " +
                        "FROM user_objects " +
                        "WHERE object_name = UPPER(?) AND object_type = 'PROCEDURE'";

                parameters = getProcedureParameters(procedureName, owner);
            } else {
                procSql = "SELECT " +
                        "    owner, " +
                        "    object_name as procedure_name, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    status, " +
                        "    object_type " +
                        "FROM all_objects " +
                        "WHERE object_name = UPPER(?) AND owner = UPPER(?) AND object_type = 'PROCEDURE'";

                parameters = getProcedureParameters(procedureName, owner);
            }

            Map<String, Object> procInfo = owner.equals(getCurrentUser())
                    ? oracleJdbcTemplate.queryForMap(procSql, procedureName)
                    : oracleJdbcTemplate.queryForMap(procSql, procedureName, owner);

            Map<String, Object> result = new HashMap<>();
            result.put("procedureInfo", procInfo);
            result.put("parameters", parameters);
            result.put("parameterCount", parameters.size());

            return result;

        } catch (Exception e) {
            log.error("Error in getProcedureDetails for {}: {}", procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for procedure " + procedureName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get procedure parameters
     */
    public List<Map<String, Object>> getProcedureParameters(String procedureName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    argument_name, " +
                        "    position, " +
                        "    sequence, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    defaulted " +
                        "FROM all_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND UPPER(owner) = UPPER(?) AND package_name IS NULL " +
                        "ORDER BY position, sequence";

                return oracleJdbcTemplate.queryForList(sql, procedureName, owner);
            } else {
                sql = "SELECT " +
                        "    argument_name, " +
                        "    position, " +
                        "    sequence, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    defaulted " +
                        "FROM user_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND package_name IS NULL " +
                        "ORDER BY position, sequence";

                return oracleJdbcTemplate.queryForList(sql, procedureName);
            }
        } catch (Exception e) {
            log.error("Error getting procedure parameters: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all functions in the current schema
     */
    public List<Map<String, Object>> getAllFunctions() {
        try {
            String sql = "SELECT " +
                    "    object_name as function_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    (SELECT COUNT(*) FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL) as parameter_count " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'FUNCTION' " +
                    "ORDER BY object_name";

            log.debug("Executing query for all functions");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllFunctions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve functions: " + e.getMessage(), e);
        }
    }

    /**
     * Get all functions from a specific schema
     */
    public List<Map<String, Object>> getFunctionsBySchema(String schemaName) {
        try {
            log.info("Getting functions for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as function_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    (SELECT COUNT(*) FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL) as parameter_count " +
                    "FROM all_objects o " +
                    "WHERE o.object_type = 'FUNCTION' AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name";

            log.debug("Executing query for functions in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} functions in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getFunctionsBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve functions for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get function details including parameters and return type
     */
    public Map<String, Object> getFunctionDetails(String functionName) {
        try {
            log.info("Getting details for function: {}", functionName);

            // Find function location
            Map<String, Object> funcLocation = findObjectLocation(functionName, "FUNCTION");
            if (funcLocation.isEmpty()) {
                log.warn("Function {} not found", functionName);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("function_name", functionName);
                emptyResult.put("message", "Function not found");
                return emptyResult;
            }

            String owner = (String) funcLocation.get("owner");

            // Get function info
            String funcSql;
            List<Map<String, Object>> parameters;
            Map<String, Object> returnType = new HashMap<>();

            if (owner.equals(getCurrentUser())) {
                funcSql = "SELECT " +
                        "    object_name as function_name, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    status, " +
                        "    'FUNCTION' as object_type " +
                        "FROM user_objects " +
                        "WHERE object_name = UPPER(?) AND object_type = 'FUNCTION'";

                parameters = getFunctionParameters(functionName, owner);
                returnType = getFunctionReturnType(functionName, owner);
            } else {
                funcSql = "SELECT " +
                        "    owner, " +
                        "    object_name as function_name, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    status, " +
                        "    object_type " +
                        "FROM all_objects " +
                        "WHERE object_name = UPPER(?) AND owner = UPPER(?) AND object_type = 'FUNCTION'";

                parameters = getFunctionParameters(functionName, owner);
                returnType = getFunctionReturnType(functionName, owner);
            }

            Map<String, Object> funcInfo = owner.equals(getCurrentUser())
                    ? oracleJdbcTemplate.queryForMap(funcSql, functionName)
                    : oracleJdbcTemplate.queryForMap(funcSql, functionName, owner);

            Map<String, Object> result = new HashMap<>();
            result.put("functionInfo", funcInfo);
            result.put("parameters", parameters);
            result.put("returnType", returnType);
            result.put("parameterCount", parameters.size());

            return result;

        } catch (Exception e) {
            log.error("Error in getFunctionDetails for {}: {}", functionName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for function " + functionName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get function parameters
     */
    public List<Map<String, Object>> getFunctionParameters(String functionName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    argument_name, " +
                        "    position, " +
                        "    sequence, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    defaulted " +
                        "FROM all_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND UPPER(owner) = UPPER(?) AND package_name IS NULL " +
                        "AND argument_name IS NOT NULL " +
                        "ORDER BY position, sequence";

                return oracleJdbcTemplate.queryForList(sql, functionName, owner);
            } else {
                sql = "SELECT " +
                        "    argument_name, " +
                        "    position, " +
                        "    sequence, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    defaulted " +
                        "FROM user_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND package_name IS NULL " +
                        "AND argument_name IS NOT NULL " +
                        "ORDER BY position, sequence";

                return oracleJdbcTemplate.queryForList(sql, functionName);
            }
        } catch (Exception e) {
            log.error("Error getting function parameters: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get function return type
     */
    public Map<String, Object> getFunctionReturnType(String functionName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    data_type, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    char_length " +
                        "FROM all_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND UPPER(owner) = UPPER(?) " +
                        "AND package_name IS NULL AND argument_name IS NULL " +
                        "AND ROWNUM = 1";

                return oracleJdbcTemplate.queryForMap(sql, functionName, owner);
            } else {
                sql = "SELECT " +
                        "    data_type, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    char_length " +
                        "FROM user_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND package_name IS NULL " +
                        "AND argument_name IS NULL AND ROWNUM = 1";

                return oracleJdbcTemplate.queryForMap(sql, functionName);
            }
        } catch (Exception e) {
            log.debug("No return type found for function {}: {}", functionName, e.getMessage());
            Map<String, Object> empty = new HashMap<>();
            empty.put("data_type", "VOID");
            return empty;
        }
    }

    /**
     * Get all packages in the current schema
     */
    public List<Map<String, Object>> getAllPackages() {
        try {
            String sql = "SELECT " +
                    "    object_name as package_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status " +
                    "FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') " +
                    "ORDER BY object_name, object_type";

            log.debug("Executing query for all packages");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllPackages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve packages: " + e.getMessage(), e);
        }
    }

    /**
     * Get all packages from a specific schema
     */
    public List<Map<String, Object>> getPackagesBySchema(String schemaName) {
        try {
            log.info("Getting packages for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as package_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status " +
                    "FROM all_objects o " +
                    "WHERE o.object_type IN ('PACKAGE', 'PACKAGE BODY') AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name, o.object_type";

            log.debug("Executing query for packages in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} packages in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getPackagesBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve packages for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get package details including specification and body info
     */
    public Map<String, Object> getPackageDetails(String packageName) {
        try {
            log.info("Getting details for package: {}", packageName);

            // Find package location
            Map<String, Object> pkgLocation = findObjectLocation(packageName, "PACKAGE");
            if (pkgLocation.isEmpty()) {
                // Try PACKAGE BODY
                pkgLocation = findObjectLocation(packageName, "PACKAGE BODY");
                if (pkgLocation.isEmpty()) {
                    log.warn("Package {} not found", packageName);
                    Map<String, Object> emptyResult = new HashMap<>();
                    emptyResult.put("package_name", packageName);
                    emptyResult.put("message", "Package not found");
                    return emptyResult;
                }
            }

            String owner = (String) pkgLocation.get("owner");

            // Get package spec info
            Map<String, Object> packageSpec = getPackageSpec(packageName, owner);
            Map<String, Object> packageBody = getPackageBody(packageName, owner);
            List<Map<String, Object>> procedures = getPackageProcedures(packageName, owner);
            List<Map<String, Object>> functions = getPackageFunctions(packageName, owner);
            List<Map<String, Object>> variables = getPackageVariables(packageName, owner);

            Map<String, Object> result = new HashMap<>();
            result.put("packageSpec", packageSpec);
            result.put("packageBody", packageBody);
            result.put("procedures", procedures);
            result.put("functions", functions);
            result.put("variables", variables);
            result.put("procedureCount", procedures.size());
            result.put("functionCount", functions.size());
            result.put("variableCount", variables.size());

            return result;

        } catch (Exception e) {
            log.error("Error in getPackageDetails for {}: {}", packageName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for package " + packageName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get package specification
     */
    private Map<String, Object> getPackageSpec(String packageName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT owner, object_name, created, last_ddl_time, status " +
                        "FROM all_objects " +
                        "WHERE object_name = UPPER(?) AND owner = UPPER(?) AND object_type = 'PACKAGE'";
                return oracleJdbcTemplate.queryForMap(sql, packageName, owner);
            } else {
                sql = "SELECT object_name, created, last_ddl_time, status " +
                        "FROM user_objects " +
                        "WHERE object_name = UPPER(?) AND object_type = 'PACKAGE'";
                return oracleJdbcTemplate.queryForMap(sql, packageName);
            }
        } catch (Exception e) {
            log.debug("No package spec found for {}: {}", packageName, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get package body
     */
    private Map<String, Object> getPackageBody(String packageName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT owner, object_name, created, last_ddl_time, status " +
                        "FROM all_objects " +
                        "WHERE object_name = UPPER(?) AND owner = UPPER(?) AND object_type = 'PACKAGE BODY'";
                return oracleJdbcTemplate.queryForMap(sql, packageName, owner);
            } else {
                sql = "SELECT object_name, created, last_ddl_time, status " +
                        "FROM user_objects " +
                        "WHERE object_name = UPPER(?) AND object_type = 'PACKAGE BODY'";
                return oracleJdbcTemplate.queryForMap(sql, packageName);
            }
        } catch (Exception e) {
            log.debug("No package body found for {}: {}", packageName, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get package procedures
     */
    public List<Map<String, Object>> getPackageProcedures(String packageName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT DISTINCT " +
                        "    a.object_name as procedure_name, " +
                        "    a.procedure_name as subprogram_name, " +
                        "    a.overload, " +
                        "    a.argument_name, " +
                        "    a.position, " +
                        "    a.sequence, " +
                        "    a.data_type, " +
                        "    a.in_out, " +
                        "    a.data_length, " +
                        "    a.data_precision, " +
                        "    a.data_scale " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(a.package_name) = UPPER(?) AND UPPER(a.owner) = UPPER(?) " +
                        "AND a.object_name = a.package_name " +
                        "AND a.argument_name IS NOT NULL " +
                        "ORDER BY a.procedure_name, a.position";
            } else {
                sql = "SELECT DISTINCT " +
                        "    a.object_name as procedure_name, " +
                        "    a.procedure_name as subprogram_name, " +
                        "    a.overload, " +
                        "    a.argument_name, " +
                        "    a.position, " +
                        "    a.sequence, " +
                        "    a.data_type, " +
                        "    a.in_out, " +
                        "    a.data_length, " +
                        "    a.data_precision, " +
                        "    a.data_scale " +
                        "FROM user_arguments a " +
                        "WHERE UPPER(a.package_name) = UPPER(?) " +
                        "AND a.object_name = a.package_name " +
                        "AND a.argument_name IS NOT NULL " +
                        "ORDER BY a.procedure_name, a.position";
            }

            return oracleJdbcTemplate.queryForList(sql, packageName, owner);

        } catch (Exception e) {
            log.error("Error getting package procedures: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get package functions
     */
    public List<Map<String, Object>> getPackageFunctions(String packageName, String owner) {
        try {
            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT DISTINCT " +
                        "    a.object_name as function_name, " +
                        "    a.procedure_name as subprogram_name, " +
                        "    a.overload, " +
                        "    a.argument_name, " +
                        "    a.position, " +
                        "    a.sequence, " +
                        "    a.data_type, " +
                        "    a.in_out, " +
                        "    a.data_length, " +
                        "    a.data_precision, " +
                        "    a.data_scale, " +
                        "    (SELECT data_type FROM all_arguments b " +
                        "     WHERE b.owner = a.owner AND b.package_name = a.package_name " +
                        "     AND b.object_name = a.object_name AND b.procedure_name = a.procedure_name " +
                        "     AND b.overload = a.overload AND b.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(a.package_name) = UPPER(?) AND UPPER(a.owner) = UPPER(?) " +
                        "AND a.object_name = a.package_name " +
                        "AND a.argument_name IS NOT NULL " +
                        "AND EXISTS (SELECT 1 FROM all_arguments c " +
                        "            WHERE c.owner = a.owner AND c.package_name = a.package_name " +
                        "            AND c.object_name = a.object_name AND c.procedure_name = a.procedure_name " +
                        "            AND c.argument_name IS NULL) " +
                        "ORDER BY a.procedure_name, a.position";
            } else {
                sql = "SELECT DISTINCT " +
                        "    a.object_name as function_name, " +
                        "    a.procedure_name as subprogram_name, " +
                        "    a.overload, " +
                        "    a.argument_name, " +
                        "    a.position, " +
                        "    a.sequence, " +
                        "    a.data_type, " +
                        "    a.in_out, " +
                        "    a.data_length, " +
                        "    a.data_precision, " +
                        "    a.data_scale, " +
                        "    (SELECT data_type FROM user_arguments b " +
                        "     WHERE b.package_name = a.package_name " +
                        "     AND b.object_name = a.object_name AND b.procedure_name = a.procedure_name " +
                        "     AND b.overload = a.overload AND b.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                        "FROM user_arguments a " +
                        "WHERE UPPER(a.package_name) = UPPER(?) " +
                        "AND a.object_name = a.package_name " +
                        "AND a.argument_name IS NOT NULL " +
                        "AND EXISTS (SELECT 1 FROM user_arguments c " +
                        "            WHERE c.package_name = a.package_name " +
                        "            AND c.object_name = a.object_name AND c.procedure_name = a.procedure_name " +
                        "            AND c.argument_name IS NULL) " +
                        "ORDER BY a.procedure_name, a.position";
            }

            return oracleJdbcTemplate.queryForList(sql, packageName, owner);

        } catch (Exception e) {
            log.error("Error getting package functions: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get package variables (this is limited as Oracle doesn't expose package variables easily)
     */
    public List<Map<String, Object>> getPackageVariables(String packageName, String owner) {
        // This is a simplified version - getting actual package variables is complex
        // and often requires parsing the package source
        return new ArrayList<>();
    }

    /**
     * Get all triggers in the current schema
     */
    public List<Map<String, Object>> getAllTriggers() {
        try {
            String sql = "SELECT " +
                    "    trigger_name, " +
                    "    trigger_type, " +
                    "    triggering_event, " +
                    "    table_name, " +
                    "    status, " +
                    "    description, " +
                    "    trigger_body, " +
                    "    created, " +
                    "    last_ddl_time " +
                    "FROM user_triggers t " +
                    "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "ORDER BY trigger_name";

            log.debug("Executing query for all triggers");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTriggers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve triggers: " + e.getMessage(), e);
        }
    }

    /**
     * Get all triggers from a specific schema
     */
    public List<Map<String, Object>> getTriggersBySchema(String schemaName) {
        try {
            log.info("Getting triggers for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    t.owner, " +
                    "    t.trigger_name, " +
                    "    t.trigger_type, " +
                    "    t.triggering_event, " +
                    "    t.table_owner, " +
                    "    t.table_name, " +
                    "    t.status, " +
                    "    t.description, " +
                    "    o.created, " +
                    "    o.last_ddl_time " +
                    "FROM all_triggers t " +
                    "JOIN all_objects o ON t.owner = o.owner AND t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.trigger_name";

            log.debug("Executing query for triggers in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} triggers in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getTriggersBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve triggers for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get trigger details
     */
    public Map<String, Object> getTriggerDetails(String triggerName) {
        try {
            log.info("Getting details for trigger: {}", triggerName);

            // Find trigger location
            Map<String, Object> triggerLocation = findObjectLocation(triggerName, "TRIGGER");
            if (triggerLocation.isEmpty()) {
                log.warn("Trigger {} not found", triggerName);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("trigger_name", triggerName);
                emptyResult.put("message", "Trigger not found");
                return emptyResult;
            }

            String owner = (String) triggerLocation.get("owner");

            String sql;
            if (owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    trigger_name, " +
                        "    trigger_type, " +
                        "    triggering_event, " +
                        "    table_name, " +
                        "    referencing_names, " +
                        "    when_clause, " +
                        "    status, " +
                        "    description, " +
                        "    trigger_body, " +
                        "    created, " +
                        "    last_ddl_time " +
                        "FROM user_triggers t " +
                        "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                        "WHERE UPPER(t.trigger_name) = UPPER(?)";

                return oracleJdbcTemplate.queryForMap(sql, triggerName);
            } else {
                sql = "SELECT " +
                        "    t.owner, " +
                        "    t.trigger_name, " +
                        "    t.trigger_type, " +
                        "    t.triggering_event, " +
                        "    t.table_owner, " +
                        "    t.table_name, " +
                        "    t.referencing_names, " +
                        "    t.when_clause, " +
                        "    t.status, " +
                        "    t.description, " +
                        "    t.trigger_body, " +
                        "    o.created, " +
                        "    o.last_ddl_time " +
                        "FROM all_triggers t " +
                        "JOIN all_objects o ON t.owner = o.owner AND t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                        "WHERE UPPER(t.trigger_name) = UPPER(?) AND UPPER(t.owner) = UPPER(?)";

                return oracleJdbcTemplate.queryForMap(sql, triggerName, owner);
            }

        } catch (Exception e) {
            log.error("Error in getTriggerDetails for {}: {}", triggerName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for trigger " + triggerName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get all synonyms in the current schema
     */
    public List<Map<String, Object>> getAllSynonyms() {
        try {
            String sql = "SELECT " +
                    "    synonym_name, " +
                    "    table_owner, " +
                    "    table_name, " +
                    "    db_link " +
                    "FROM user_synonyms " +
                    "ORDER BY synonym_name";

            log.debug("Executing query for all synonyms");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSynonyms: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve synonyms: " + e.getMessage(), e);
        }
    }

    /**
     * Get all synonyms from a specific schema
     */
    public List<Map<String, Object>> getSynonymsBySchema(String schemaName) {
        try {
            log.info("Getting synonyms for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    owner, " +
                    "    synonym_name, " +
                    "    table_owner, " +
                    "    table_name, " +
                    "    db_link " +
                    "FROM all_synonyms " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY synonym_name";

            log.debug("Executing query for synonyms in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} synonyms in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getSynonymsBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve synonyms for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get synonym details
     */
    public Map<String, Object> getSynonymDetails(String synonymName) {
        try {
            log.info("Getting details for synonym: {}", synonymName);

            Map<String, Object> synonymLocation = findObjectLocation(synonymName, "SYNONYM");
            if (synonymLocation.isEmpty()) {
                log.warn("Synonym {} not found", synonymName);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("synonym_name", synonymName);
                emptyResult.put("message", "Synonym not found");
                return emptyResult;
            }

            String owner = (String) synonymLocation.get("owner");

            String sql;
            if (owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    synonym_name, " +
                        "    table_owner, " +
                        "    table_name, " +
                        "    db_link " +
                        "FROM user_synonyms " +
                        "WHERE UPPER(synonym_name) = UPPER(?)";
            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    synonym_name, " +
                        "    table_owner, " +
                        "    table_name, " +
                        "    db_link " +
                        "FROM all_synonyms " +
                        "WHERE UPPER(synonym_name) = UPPER(?) AND UPPER(owner) = UPPER(?)";
            }

            return oracleJdbcTemplate.queryForMap(sql, synonymName, owner);

        } catch (Exception e) {
            log.error("Error in getSynonymDetails for {}: {}", synonymName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for synonym " + synonymName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get all sequences in the current schema
     */
    public List<Map<String, Object>> getAllSequences() {
        try {
            String sql = "SELECT " +
                    "    sequence_name, " +
                    "    min_value, " +
                    "    max_value, " +
                    "    increment_by, " +
                    "    cycle_flag, " +
                    "    order_flag, " +
                    "    cache_size, " +
                    "    last_number " +
                    "FROM user_sequences " +
                    "ORDER BY sequence_name";

            log.debug("Executing query for all sequences");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSequences: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequences: " + e.getMessage(), e);
        }
    }

    /**
     * Get all sequences from a specific schema
     */
    public List<Map<String, Object>> getSequencesBySchema(String schemaName) {
        try {
            log.info("Getting sequences for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    sequence_owner as owner, " +
                    "    sequence_name, " +
                    "    min_value, " +
                    "    max_value, " +
                    "    increment_by, " +
                    "    cycle_flag, " +
                    "    order_flag, " +
                    "    cache_size, " +
                    "    last_number " +
                    "FROM all_sequences " +
                    "WHERE UPPER(sequence_owner) = UPPER(?) " +
                    "ORDER BY sequence_name";

            log.debug("Executing query for sequences in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} sequences in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getSequencesBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequences for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get sequence details
     */
    public Map<String, Object> getSequenceDetails(String sequenceName) {
        try {
            log.info("Getting details for sequence: {}", sequenceName);

            Map<String, Object> seqLocation = findObjectLocation(sequenceName, "SEQUENCE");
            if (seqLocation.isEmpty()) {
                log.warn("Sequence {} not found", sequenceName);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("sequence_name", sequenceName);
                emptyResult.put("message", "Sequence not found");
                return emptyResult;
            }

            String owner = (String) seqLocation.get("owner");

            String sql;
            if (owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    sequence_name, " +
                        "    min_value, " +
                        "    max_value, " +
                        "    increment_by, " +
                        "    cycle_flag, " +
                        "    order_flag, " +
                        "    cache_size, " +
                        "    last_number " +
                        "FROM user_sequences " +
                        "WHERE UPPER(sequence_name) = UPPER(?)";
            } else {
                sql = "SELECT " +
                        "    sequence_owner as owner, " +
                        "    sequence_name, " +
                        "    min_value, " +
                        "    max_value, " +
                        "    increment_by, " +
                        "    cycle_flag, " +
                        "    order_flag, " +
                        "    cache_size, " +
                        "    last_number " +
                        "FROM all_sequences " +
                        "WHERE UPPER(sequence_name) = UPPER(?) AND UPPER(sequence_owner) = UPPER(?)";
            }

            return oracleJdbcTemplate.queryForMap(sql, sequenceName, owner);

        } catch (Exception e) {
            log.error("Error in getSequenceDetails for {}: {}", sequenceName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for sequence " + sequenceName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get all types in the current schema
     */
    public List<Map<String, Object>> getAllTypes() {
        try {
            String sql = "SELECT " +
                    "    type_name, " +
                    "    typecode, " +
                    "    attributes, " +
                    "    methods, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status " +
                    "FROM user_types t " +
                    "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "ORDER BY type_name";

            log.debug("Executing query for all types");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTypes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve types: " + e.getMessage(), e);
        }
    }

    /**
     * Get all types from a specific schema
     */
    public List<Map<String, Object>> getTypesBySchema(String schemaName) {
        try {
            log.info("Getting types for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    t.owner, " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status " +
                    "FROM all_types t " +
                    "JOIN all_objects o ON t.owner = o.owner AND t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.type_name";

            log.debug("Executing query for types in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} types in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getTypesBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve types for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get type details including attributes and methods
     */
    public Map<String, Object> getTypeDetails(String typeName) {
        try {
            log.info("Getting details for type: {}", typeName);

            Map<String, Object> typeLocation = findObjectLocation(typeName, "TYPE");
            if (typeLocation.isEmpty()) {
                // Try TYPE BODY
                typeLocation = findObjectLocation(typeName, "TYPE BODY");
                if (typeLocation.isEmpty()) {
                    log.warn("Type {} not found", typeName);
                    Map<String, Object> emptyResult = new HashMap<>();
                    emptyResult.put("type_name", typeName);
                    emptyResult.put("message", "Type not found");
                    return emptyResult;
                }
            }

            String owner = (String) typeLocation.get("owner");

            Map<String, Object> typeInfo;
            List<Map<String, Object>> attributes = new ArrayList<>();
            List<Map<String, Object>> methods = new ArrayList<>();

            if (owner.equals(getCurrentUser())) {
                String typeSql = "SELECT " +
                        "    type_name, " +
                        "    typecode, " +
                        "    attributes, " +
                        "    methods, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    status " +
                        "FROM user_types t " +
                        "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                        "WHERE UPPER(t.type_name) = UPPER(?)";

                typeInfo = oracleJdbcTemplate.queryForMap(typeSql, typeName);

                // Get type attributes
                String attrSql = "SELECT " +
                        "    attr_name, " +
                        "    attr_type_name, " +
                        "    length, " +
                        "    precision, " +
                        "    scale " +
                        "FROM user_type_attrs " +
                        "WHERE UPPER(type_name) = UPPER(?) " +
                        "ORDER BY attr_no";

                attributes = oracleJdbcTemplate.queryForList(attrSql, typeName);

                // Get type methods
                String methodSql = "SELECT " +
                        "    method_name, " +
                        "    method_no, " +
                        "    method_type, " +
                        "    parameters, " +
                        "    results " +
                        "FROM user_type_methods " +
                        "WHERE UPPER(type_name) = UPPER(?) " +
                        "ORDER BY method_no";

                methods = oracleJdbcTemplate.queryForList(methodSql, typeName);

            } else {
                String typeSql = "SELECT " +
                        "    t.owner, " +
                        "    t.type_name, " +
                        "    t.typecode, " +
                        "    t.attributes, " +
                        "    t.methods, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status " +
                        "FROM all_types t " +
                        "JOIN all_objects o ON t.owner = o.owner AND t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                        "WHERE UPPER(t.type_name) = UPPER(?) AND UPPER(t.owner) = UPPER(?)";

                typeInfo = oracleJdbcTemplate.queryForMap(typeSql, typeName, owner);

                // For other schemas, we might have limited access to type details
            }

            Map<String, Object> result = new HashMap<>();
            result.put("typeInfo", typeInfo);
            result.put("attributes", attributes);
            result.put("methods", methods);
            result.put("attributeCount", attributes.size());
            result.put("methodCount", methods.size());

            return result;

        } catch (Exception e) {
            log.error("Error in getTypeDetails for {}: {}", typeName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for type " + typeName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get all database links
     */
    public List<Map<String, Object>> getAllDbLinks() {
        try {
            String sql = "SELECT " +
                    "    db_link, " +
                    "    username, " +
                    "    host, " +
                    "    created " +
                    "FROM user_db_links " +
                    "ORDER BY db_link";

            log.debug("Executing query for all database links");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllDbLinks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links: " + e.getMessage(), e);
        }
    }

    /**
     * Get all database links from a specific schema
     */
    public List<Map<String, Object>> getDbLinksBySchema(String schemaName) {
        try {
            log.info("Getting database links for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    owner, " +
                    "    db_link, " +
                    "    username, " +
                    "    host, " +
                    "    created " +
                    "FROM all_db_links " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY db_link";

            log.debug("Executing query for database links in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} database links in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getDbLinksBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Search for any object by name pattern
     */
    public List<Map<String, Object>> searchObjects(String searchPattern) {
        try {
            log.info("Searching for objects with pattern: {}", searchPattern);

            String sql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status " +
                    "FROM all_objects " +
                    "WHERE UPPER(object_name) LIKE UPPER(?) " +
                    "ORDER BY object_type, object_name";

            log.debug("Executing object search with pattern: {}", searchPattern);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, "%" + searchPattern + "%");
            log.info("Search returned {} objects", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error in searchObjects for pattern {}: {}", searchPattern, e.getMessage(), e);
            throw new RuntimeException("Failed to search objects with pattern " + searchPattern + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get object count by type
     */
    public List<Map<String, Object>> getObjectCountByType() {
        try {
            String sql = "SELECT " +
                    "    object_type, " +
                    "    COUNT(*) as object_count, " +
                    "    SUM(CASE WHEN status = 'VALID' THEN 1 ELSE 0 END) as valid_count, " +
                    "    SUM(CASE WHEN status = 'INVALID' THEN 1 ELSE 0 END) as invalid_count " +
                    "FROM user_objects " +
                    "GROUP BY object_type " +
                    "ORDER BY object_type";

            log.debug("Executing query for object count by type");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getObjectCountByType: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve object count by type: " + e.getMessage(), e);
        }
    }

    /**
     * Get all objects in the current schema
     */
    public List<Map<String, Object>> getAllObjects() {
        try {
            String sql = "SELECT " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status " +
                    "FROM user_objects " +
                    "ORDER BY object_type, object_name";

            log.debug("Executing query for all objects");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllObjects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get all objects from a specific schema
     */
    public List<Map<String, Object>> getObjectsBySchema(String schemaName) {
        try {
            log.info("Getting objects for schema: {}", schemaName);

            String sql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status " +
                    "FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY object_type, object_name";

            log.debug("Executing query for objects in schema: {}", schemaName);
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, schemaName);
            log.info("Found {} objects in schema {}", results.size(), schemaName);

            return results;

        } catch (Exception e) {
            log.error("Error in getObjectsBySchema for {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve objects for schema " + schemaName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Test database connection
     */
    public void testConnection() {
        try {
            String result = oracleJdbcTemplate.queryForObject(
                    "SELECT 'Connected to: ' || USER || ' at ' || TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DUAL",
                    String.class
            );
            log.info("Oracle connection test: {}", result);
        } catch (Exception e) {
            log.error("Oracle connection failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database connection test failed", e);
        }
    }

    /**
     * Get object DDL (if user has access to DBMS_METADATA)
     */
    public String getObjectDDL(String objectName, String objectType) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                log.warn("Object {} of type {} not found", objectName, objectType);
                return null;
            }

            String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) FROM DUAL";
            return oracleJdbcTemplate.queryForObject(sql, String.class, objectType.toUpperCase(), objectName.toUpperCase(), owner.toUpperCase());

        } catch (Exception e) {
            log.warn("Could not get DDL for object {}: {}", objectName, e.getMessage());
            return null;
        }
    }

    /**
     * Get object size information
     */
    public Map<String, Object> getObjectSize(String objectName, String objectType) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                log.warn("Object {} not found", objectName);
                return new HashMap<>();
            }

            String sql;
            if (owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    segment_name, " +
                        "    segment_type, " +
                        "    tablespace_name, " +
                        "    bytes, " +
                        "    blocks, " +
                        "    extents " +
                        "FROM user_segments " +
                        "WHERE UPPER(segment_name) = UPPER(?) AND segment_type LIKE ? || '%'";

                return oracleJdbcTemplate.queryForMap(sql, objectName, objectType);
            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    segment_name, " +
                        "    segment_type, " +
                        "    tablespace_name, " +
                        "    bytes, " +
                        "    blocks, " +
                        "    extents " +
                        "FROM all_segments " +
                        "WHERE UPPER(segment_name) = UPPER(?) AND UPPER(owner) = UPPER(?) AND segment_type LIKE ? || '%'";

                return oracleJdbcTemplate.queryForMap(sql, objectName, owner, objectType);
            }

        } catch (Exception e) {
            log.error("Error in getObjectSize for {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve size for object " + objectName + ": " + e.getMessage(), e);
        }
    }

    // ==================== DIAGNOSTIC METHODS ====================

    /**
     * Comprehensive diagnostic method to check database connection and objects
     */
    public Map<String, Object> diagnoseDatabase() {
        Map<String, Object> diagnostics = new HashMap<>();
        List<String> issues = new ArrayList<>();

        try {
            // Test basic connection
            String currentUser = getCurrentUser();
            String currentSchema = getCurrentSchema();
            diagnostics.put("currentUser", currentUser);
            diagnostics.put("currentSchema", currentSchema);
            diagnostics.put("connectionStatus", "SUCCESS");

            // Check user tables
            Integer userTableCount = getUserTableCount();
            diagnostics.put("userTableCount", userTableCount);

            if (userTableCount == 0) {
                issues.add("User " + currentUser + " has no tables in their schema");
            }

            // Check object counts
            diagnostics.put("objectCounts", getObjectCountByType());

            // Check accessible tables
            List<Map<String, Object>> accessibleTables = getAccessibleTablesSample();
            diagnostics.put("accessibleTablesSample", accessibleTables);

            // Check for common HR tables
            List<String> commonTables = List.of("EMPLOYEES", "DEPARTMENTS", "JOBS", "LOCATIONS");
            Map<String, Object> tableLocations = new HashMap<>();

            for (String table : commonTables) {
                Map<String, Object> location = findTableLocation(table);
                if (!location.isEmpty()) {
                    tableLocations.put(table, location);
                }
            }
            diagnostics.put("commonTableLocations", tableLocations);

            if (tableLocations.isEmpty()) {
                issues.add("None of the common HR tables (EMPLOYEES, DEPARTMENTS, etc.) were found");
            }

            // Check database version
            String dbVersion = getDatabaseVersion();
            diagnostics.put("databaseVersion", dbVersion);

            // Check user privileges
            List<String> privileges = getUserPrivileges();
            diagnostics.put("userPrivileges", privileges);

            diagnostics.put("issues", issues);
            diagnostics.put("diagnosticStatus", issues.isEmpty() ? "HEALTHY" : "ISSUES_FOUND");

        } catch (Exception e) {
            log.error("Diagnostic failed: {}", e.getMessage(), e);
            diagnostics.put("connectionStatus", "FAILED");
            diagnostics.put("error", e.getMessage());
            diagnostics.put("diagnosticStatus", "ERROR");
        }

        return diagnostics;
    }

    /**
     * Get current Oracle user
     */
    private String getCurrentUser() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT USER FROM DUAL", String.class);
        } catch (Exception e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Get count of tables owned by current user
     */
    private Integer getUserTableCount() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_tables", Integer.class);
        } catch (Exception e) {
            log.error("Failed to get user table count: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Get sample of accessible tables
     */
    private List<Map<String, Object>> getAccessibleTablesSample() {
        try {
            return oracleJdbcTemplate.queryForList(
                    "SELECT owner, table_name FROM all_tables WHERE ROWNUM <= 10"
            );
        } catch (Exception e) {
            log.error("Failed to get accessible tables sample: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get list of owners with accessible tables
     */
    private List<String> getAccessibleOwners() {
        try {
            return oracleJdbcTemplate.queryForList(
                    "SELECT DISTINCT owner FROM all_tables WHERE ROWNUM <= 10",
                    String.class
            );
        } catch (Exception e) {
            log.error("Failed to get accessible owners: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check if a specific table exists and where
     */
    private Map<String, Object> findTableLocation(String tableName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT owner, table_name FROM all_tables WHERE UPPER(table_name) = UPPER(?) AND ROWNUM = 1";
            return oracleJdbcTemplate.queryForMap(sql, tableName);
        } catch (Exception e) {
            log.debug("Table {} not found in all_tables", tableName);
            return result;
        }
    }

    /**
     * Find object location by name and type
     */
    private Map<String, Object> findObjectLocation(String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT owner, object_name FROM all_objects " +
                    "WHERE UPPER(object_name) = UPPER(?) AND object_type = ? AND ROWNUM = 1";
            return oracleJdbcTemplate.queryForMap(sql, objectName, objectType);
        } catch (Exception e) {
            log.debug("Object {} of type {} not found", objectName, objectType);
            return result;
        }
    }

    /**
     * Check for specific table and log results
     */
    private void checkSpecificTable(String tableName) {
        try {
            List<Map<String, Object>> tables = oracleJdbcTemplate.queryForList(
                    "SELECT owner, table_name FROM all_tables WHERE UPPER(table_name) = UPPER(?)",
                    tableName
            );

            if (tables.isEmpty()) {
                log.info("Table {} not found in any accessible schema", tableName);
            } else {
                log.info("Table {} found in schemas: {}", tableName, tables);
            }
        } catch (Exception e) {
            log.error("Error checking for table {}: {}", tableName, e.getMessage());
        }
    }

    /**
     * Get database version
     */
    private String getDatabaseVersion() {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT banner FROM v$version WHERE ROWNUM = 1",
                    String.class
            );
        } catch (Exception e) {
            log.error("Failed to get database version: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Get user privileges
     */
    private List<String> getUserPrivileges() {
        try {
            return oracleJdbcTemplate.queryForList(
                    "SELECT privilege FROM user_sys_privs WHERE ROWNUM <= 20",
                    String.class
            );
        } catch (Exception e) {
            log.error("Failed to get user privileges: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}