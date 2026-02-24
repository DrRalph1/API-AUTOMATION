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

@Repository
public class SchemaBrowserRepository {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(SchemaBrowserRepository.class);

    /**
     * Get all tables from the current Oracle schema
     * Using actual Oracle system tables: USER_TABLES
     */
    public List<Map<String, Object>> getAllTables() {
        String sql = "SELECT " +
                "    table_name, " +
                "    tablespace_name, " +
                "    status, " +
                "    num_rows, " +
                "    avg_row_len, " +
                "    blocks, " +
                "    last_analyzed, " +
                "    created " +
                "FROM user_tables " + // USER_TABLES shows tables owned by current user
                "ORDER BY table_name";

        log.debug("Executing query: {}", sql);
        return oracleJdbcTemplate.queryForList(sql);
    }

    /**
     * Get all tables from a specific schema
     * Using actual Oracle system tables: ALL_TABLES
     */
    public List<Map<String, Object>> getTablesBySchema(String schemaName) {
        String sql = "SELECT " +
                "    table_name, " +
                "    owner, " +
                "    tablespace_name, " +
                "    status, " +
                "    num_rows, " +
                "    avg_row_len, " +
                "    blocks, " +
                "    last_analyzed, " +
                "    created " +
                "FROM all_tables " + // ALL_TABLES shows tables accessible to current user
                "WHERE UPPER(owner) = UPPER(?) " +
                "ORDER BY table_name";

        log.debug("Executing query for schema: {}", schemaName);
        return oracleJdbcTemplate.queryForList(sql, schemaName);
    }

    /**
     * Get detailed information about a specific table
     * Using actual Oracle system tables: USER_TABLES, USER_TAB_COLUMNS, USER_CONSTRAINTS, USER_INDEXES
     */
    public Map<String, Object> getTableDetails(String tableName) {
        String sql = "SELECT " +
                "    t.table_name, " +
                "    t.tablespace_name, " +
                "    t.status, " +
                "    t.num_rows, " +
                "    t.avg_row_len, " +
                "    t.blocks, " +
                "    t.last_analyzed, " +
                "    t.created, " +
                "    (SELECT COUNT(*) FROM user_tab_columns WHERE table_name = t.table_name) as column_count, " +
                "    (SELECT COUNT(*) FROM user_constraints WHERE table_name = t.table_name AND constraint_type = 'P') as primary_key_count, " +
                "    (SELECT COUNT(*) FROM user_constraints WHERE table_name = t.table_name AND constraint_type = 'R') as foreign_key_count, " +
                "    (SELECT COUNT(*) FROM user_indexes WHERE table_name = t.table_name) as index_count " +
                "FROM user_tables t " +
                "WHERE UPPER(t.table_name) = UPPER(?)";

        log.debug("Executing query for table details: {}", tableName);
        return oracleJdbcTemplate.queryForMap(sql, tableName);
    }

    /**
     * Get columns for a specific table with statistics
     * Using actual Oracle system tables: USER_TAB_COLUMNS and USER_TAB_COL_STATISTICS
     */
    public List<Map<String, Object>> getTableColumns(String tableName) {
        String sql = "SELECT " +
                "    c.column_id, " +
                "    c.column_name, " +
                "    c.data_type, " +
                "    c.data_length, " +
                "    c.data_precision, " +
                "    c.data_scale, " +
                "    c.nullable, " +
                "    c.default_length, " +
                "    c.data_default, " +
                "    s.num_distinct, " +      // From statistics view
                "    s.density, " +            // From statistics view
                "    s.num_nulls, " +          // From statistics view
                "    s.last_analyzed, " +      // From statistics view
                "    c.char_length, " +
                "    c.char_used " +
                "FROM user_tab_columns c " +
                "LEFT JOIN user_tab_col_statistics s " +  // Join with statistics view
                "    ON c.table_name = s.table_name " +
                "    AND c.column_name = s.column_name " +
                "WHERE UPPER(c.table_name) = UPPER(?) " +
                "ORDER BY c.column_id";

        log.debug("Executing query for table columns: {}", tableName);
        return oracleJdbcTemplate.queryForList(sql, tableName);
    }

    /**
     * Get constraints for a specific table
     * Using actual Oracle system tables: USER_CONSTRAINTS, USER_CONS_COLUMNS
     */
    public List<Map<String, Object>> getTableConstraints(String tableName) {
        String sql = "SELECT " +
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
                "     FROM user_cons_columns " + // USER_CONS_COLUMNS shows columns in constraints
                "     WHERE constraint_name = c.constraint_name) as columns " +
                "FROM user_constraints c " + // USER_CONSTRAINTS shows constraints on tables owned by current user
                "WHERE UPPER(c.table_name) = UPPER(?) " +
                "ORDER BY c.constraint_type, c.constraint_name";

        log.debug("Executing query for table constraints: {}", tableName);
        return oracleJdbcTemplate.queryForList(sql, tableName);
    }

    /**
     * Get indexes for a specific table
     * Using actual Oracle system tables: USER_INDEXES, USER_IND_COLUMNS
     */
    public List<Map<String, Object>> getTableIndexes(String tableName) {
        String sql = "SELECT " +
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
                "     FROM user_ind_columns " + // USER_IND_COLUMNS shows columns in indexes
                "     WHERE index_name = i.index_name) as columns " +
                "FROM user_indexes i " + // USER_INDEXES shows indexes owned by current user
                "WHERE UPPER(i.table_name) = UPPER(?) " +
                "ORDER BY i.index_name";

        log.debug("Executing query for table indexes: {}", tableName);
        return oracleJdbcTemplate.queryForList(sql, tableName);
    }

    /**
     * Get table statistics
     * Using actual Oracle system tables: USER_TAB_STATISTICS
     */
    public Map<String, Object> getTableStatistics(String tableName) {
        String sql = "SELECT " +
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
                "FROM user_tab_statistics " + // USER_TAB_STATISTICS shows statistics for tables owned by current user
                "WHERE UPPER(table_name) = UPPER(?)";

        log.debug("Executing query for table statistics: {}", tableName);
        return oracleJdbcTemplate.queryForMap(sql, tableName);
    }

    /**
     * Search for tables by name pattern
     * Using actual Oracle system tables: USER_TABLES
     */
    public List<Map<String, Object>> searchTables(String searchPattern) {
        String sql = "SELECT " +
                "    table_name, " +
                "    tablespace_name, " +
                "    status, " +
                "    num_rows, " +
                "    last_analyzed " +
                "FROM user_tables " + // USER_TABLES shows tables owned by current user
                "WHERE UPPER(table_name) LIKE UPPER(?) " +
                "ORDER BY table_name";

        log.debug("Executing search query with pattern: {}", searchPattern);
        return oracleJdbcTemplate.queryForList(sql, "%" + searchPattern + "%");
    }

    /**
     * Get tables with row count estimate
     * Using actual Oracle system tables: USER_TABLES
     */
    public List<Map<String, Object>> getTablesWithRowCount() {
        String sql = "SELECT " +
                "    table_name, " +
                "    num_rows, " +
                "    last_analyzed " +
                "FROM user_tables " + // USER_TABLES shows tables owned by current user
                "ORDER BY num_rows DESC NULLS LAST";

        log.debug("Executing query for tables with row count");
        return oracleJdbcTemplate.queryForList(sql);
    }

    /**
     * Get table count by tablespace
     * Using actual Oracle system tables: USER_TABLES
     */
    public List<Map<String, Object>> getTableCountByTablespace() {
        String sql = "SELECT " +
                "    tablespace_name, " +
                "    COUNT(*) as table_count, " +
                "    SUM(num_rows) as total_rows, " +
                "    SUM(blocks) as total_blocks " +
                "FROM user_tables " + // USER_TABLES shows tables owned by current user
                "WHERE tablespace_name IS NOT NULL " +
                "GROUP BY tablespace_name " +
                "ORDER BY tablespace_name";

        log.debug("Executing query for tablespace statistics");
        return oracleJdbcTemplate.queryForList(sql);
    }

    /**
     * Get recently created/modified tables
     * Using actual Oracle system tables: USER_TABLES
     */
    public List<Map<String, Object>> getRecentTables(int days) {
        String sql = "SELECT " +
                "    table_name, " +
                "    created, " +
                "    last_ddl_time " +
                "FROM user_tables " + // USER_TABLES shows tables owned by current user
                "WHERE (created > SYSDATE - ? OR last_ddl_time > SYSDATE - ?) " +
                "ORDER BY last_ddl_time DESC";

        log.debug("Executing query for recent tables (last {} days)", days);
        return oracleJdbcTemplate.queryForList(sql, days, days);
    }

    /**
     * Get all views in the current schema
     * Using actual Oracle system tables: USER_VIEWS
     */
    public List<Map<String, Object>> getAllViews() {
        String sql = "SELECT " +
                "    view_name, " +
                "    text_length, " +
                "    text, " +
                "    read_only, " +
                "    created, " +
                "    last_ddl_time " +
                "FROM user_views " + // USER_VIEWS shows views owned by current user
                "ORDER BY view_name";

        log.debug("Executing query for all views");
        return oracleJdbcTemplate.queryForList(sql);
    }

    /**
     * Get all sequences in the current schema
     * Using actual Oracle system tables: USER_SEQUENCES
     */
    public List<Map<String, Object>> getAllSequences() {
        String sql = "SELECT " +
                "    sequence_name, " +
                "    min_value, " +
                "    max_value, " +
                "    increment_by, " +
                "    cycle_flag, " +
                "    order_flag, " +
                "    cache_size, " +
                "    last_number " +
                "FROM user_sequences " + // USER_SEQUENCES shows sequences owned by current user
                "ORDER BY sequence_name";

        log.debug("Executing query for all sequences");
        return oracleJdbcTemplate.queryForList(sql);
    }

    /**
     * Get all synonyms in the current schema
     * Using actual Oracle system tables: USER_SYNONYMS
     */
    public List<Map<String, Object>> getAllSynonyms() {
        String sql = "SELECT " +
                "    synonym_name, " +
                "    table_owner, " +
                "    table_name, " +
                "    db_link " +
                "FROM user_synonyms " + // USER_SYNONYMS shows synonyms owned by current user
                "ORDER BY synonym_name";

        log.debug("Executing query for all synonyms");
        return oracleJdbcTemplate.queryForList(sql);
    }

    /**
     * Test database connection
     * Using actual Oracle system tables: DUAL
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
        }
    }
}