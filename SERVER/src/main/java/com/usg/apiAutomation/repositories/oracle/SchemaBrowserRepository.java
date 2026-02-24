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
     * Get table DDL (if user has access to DBMS_METADATA)
     */
    public String getTableDDL(String tableName) {
        try {
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");

            if (owner == null) {
                log.warn("Table {} not found", tableName);
                return null;
            }

            String sql = "SELECT DBMS_METADATA.GET_DDL('TABLE', ?, ?) FROM DUAL";
            return oracleJdbcTemplate.queryForObject(sql, String.class, tableName.toUpperCase(), owner.toUpperCase());

        } catch (Exception e) {
            log.warn("Could not get DDL for table {}: {}", tableName, e.getMessage());
            return null;
        }
    }

    /**
     * Get table size information
     */
    public Map<String, Object> getTableSize(String tableName) {
        try {
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");

            String sql;
            if (owner != null && !owner.equals(getCurrentUser())) {
                sql = "SELECT " +
                        "    t.table_name, " +
                        "    t.owner, " +
                        "    t.num_rows, " +
                        "    t.avg_row_len, " +
                        "    t.blocks, " +
                        "    (t.blocks * 8192) as estimated_bytes, " +
                        "    s.bytes as segment_bytes " +
                        "FROM all_tables t " +
                        "LEFT JOIN all_segments s ON t.owner = s.owner AND t.table_name = s.segment_name AND s.segment_type = 'TABLE' " +
                        "WHERE UPPER(t.table_name) = UPPER(?) AND UPPER(t.owner) = UPPER(?)";

                return oracleJdbcTemplate.queryForMap(sql, tableName, owner);
            } else {
                sql = "SELECT " +
                        "    t.table_name, " +
                        "    t.num_rows, " +
                        "    t.avg_row_len, " +
                        "    t.blocks, " +
                        "    (t.blocks * 8192) as estimated_bytes, " +
                        "    s.bytes as segment_bytes " +
                        "FROM user_tables t " +
                        "LEFT JOIN user_segments s ON t.table_name = s.segment_name AND s.segment_type = 'TABLE' " +
                        "WHERE UPPER(t.table_name) = UPPER(?)";

                return oracleJdbcTemplate.queryForMap(sql, tableName);
            }

        } catch (Exception e) {
            log.error("Error in getTableSize for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve size for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    // ==================== DIAGNOSTIC METHODS ====================

    /**
     * Comprehensive diagnostic method to check database connection and tables
     */
    public Map<String, Object> diagnoseDatabase() {
        Map<String, Object> diagnostics = new HashMap<>();
        List<String> issues = new ArrayList<>();

        try {
            // Test basic connection
            String currentUser = getCurrentUser();
            diagnostics.put("currentUser", currentUser);
            diagnostics.put("connectionStatus", "SUCCESS");

            // Check user tables
            Integer userTableCount = getUserTableCount();
            diagnostics.put("userTableCount", userTableCount);

            if (userTableCount == 0) {
                issues.add("User " + currentUser + " has no tables in their schema");
            }

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