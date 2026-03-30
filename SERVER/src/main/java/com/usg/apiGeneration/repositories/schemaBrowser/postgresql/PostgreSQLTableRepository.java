package com.usg.apiGeneration.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostgreSQLTableRepository extends PostgreSQLRepository {

    // ============================================================
    // TABLE METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllTables() {
        try {
            String sql = "SELECT " +
                    "    c.relname as table_name, " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    'VALID' as status, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    0 as avg_row_len, " +
                    "    c.relpages as blocks, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed, " +
                    "    NULL as created, " +
                    "    'VALID' as object_status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'r' " +
                    "ORDER BY c.relname";

            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getAllTables: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getAllTablesForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<Map<String, Object>> tables = getAllTables();

            for (Map<String, Object> table : tables) {
                Map<String, Object> transformed = new HashMap<>();
                String tableName = (String) table.get("table_name");
                transformed.put("id", "table-" + System.currentTimeMillis() + "-" + tableName);
                transformed.put("name", tableName);
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "TABLE");
                transformed.put("status", table.get("status"));
                transformed.put("rowCount", table.get("num_rows"));
                transformed.put("size", formatBytes(getLongValue(getTableSize(tableName))));
                transformed.put("comments", getTableComment(tableName));
                transformed.put("created", table.get("created"));
                transformed.put("lastModified", table.get("last_analyzed"));
                transformed.put("tablespace", table.get("tablespace_name"));
                transformed.put("icon", "table");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            // PostgreSQL doesn't have synonyms
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (tables)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllTablesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    public Map<String, Object> getAllTablesForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String tableCountSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'r'";
            int tableCount = getJdbcTemplate().queryForObject(tableCountSql, Integer.class);
            int totalCount = tableCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (tableCount > 0 && offset < tableCount) {
                int tableOffset = offset;
                int tableLimit = Math.min(pageSize, tableCount - tableOffset);

                if (tableLimit > 0) {
                    String tableSql = "SELECT " +
                            "    c.relname as name, " +
                            "    'TABLE' as type, " +
                            "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                            "    'VALID' as status, " +
                            "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                            "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed, " +
                            "    NULL as created, " +
                            "    (SELECT obj_description(c.oid, 'pg_class')) as comments, " +
                            "    pg_total_relation_size(c.oid) as size_bytes " +
                            "FROM pg_class c " +
                            "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                            "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                            "WHERE n.nspname = current_schema() AND c.relkind = 'r' " +
                            "ORDER BY c.relname " +
                            "OFFSET ? LIMIT ?";

                    List<Map<String, Object>> tables = getJdbcTemplate().queryForList(tableSql, tableOffset, tableLimit);
                    allItems.addAll(tables);
                }
            }

            List<Map<String, Object>> transformed = transformTableItems(allItems);
            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getAllTablesForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getTableDetailsForFrontend(String tableName) {
        try {
            Map<String, Object> details = getTableDetails(getCurrentSchema(), tableName);
            List<Map<String, Object>> columns = getTableColumns(getCurrentSchema(), tableName);
            List<Map<String, Object>> constraints = getTableConstraints(getCurrentSchema(), tableName);
            List<Map<String, Object>> indexes = getTableIndexes(getCurrentSchema(), tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("table_name", tableName);
            result.put("name", tableName);
            result.put("owner", getCurrentSchema());
            result.put("type", "TABLE");
            result.put("table_status", details.get("table_status"));
            result.put("object_status", details.get("object_status"));
            result.put("num_rows", details.get("num_rows"));
            result.put("bytes", details.get("size_bytes"));
            result.put("size", formatBytes(getLongValue(details.get("size_bytes"))));
            result.put("comments", getTableComment(tableName));
            result.put("columns", transformColumnsForFrontend(columns));
            result.put("constraints", transformConstraintsForFrontend(constraints));
            result.put("indexes", transformIndexesForFrontend(indexes));
            result.put("created", details.get("created"));
            result.put("lastModified", details.get("last_analyzed"));
            result.put("tablespace_name", details.get("tablespace_name"));
            result.put("icon", "table");
            result.put("isSynonym", false);

            return result;

        } catch (Exception e) {
            log.error("Error in getTableDetailsForFrontend for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTableDetailsForFrontend(String tableName, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> details = getTableDetails(getCurrentSchema(), tableName);
            Map<String, Object> columnsResult = getTableColumnsPaginated(tableName, getCurrentSchema(), page, pageSize);
            List<Map<String, Object>> constraints = getTableConstraints(getCurrentSchema(), tableName);
            List<Map<String, Object>> indexes = getTableIndexes(getCurrentSchema(), tableName);

            result.put("table_name", tableName);
            result.put("name", tableName);
            result.put("owner", getCurrentSchema());
            result.put("type", "TABLE");
            result.put("table_status", details.get("table_status"));
            result.put("object_status", details.get("object_status"));
            result.put("num_rows", details.get("num_rows"));
            result.put("bytes", details.get("size_bytes"));
            result.put("size", formatBytes(getLongValue(details.get("size_bytes"))));
            result.put("comments", getTableComment(tableName));
            result.put("columns", columnsResult.get("items"));
            result.put("totalColumns", columnsResult.get("totalCount"));
            result.put("constraints", transformConstraintsForFrontend(constraints));
            result.put("indexes", transformIndexesForFrontend(indexes));
            result.put("created", details.get("created"));
            result.put("lastModified", details.get("last_analyzed"));
            result.put("tablespace_name", details.get("tablespace_name"));
            result.put("icon", "table");
            result.put("isSynonym", false);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", columnsResult.get("totalPages"));

        } catch (Exception e) {
            log.error("Error in getTableDetailsForFrontend paginated for {}: {}", tableName, e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getTableData(String tableName, int page, int pageSize,
                                            String sortColumn, String sortDirection) {
        return getTableDataWithPagination(tableName, page, pageSize, sortColumn, sortDirection);
    }

    public Map<String, Object> getTableDataWithPagination(String tableName, int page, int pageSize,
                                                          String sortColumn, String sortDirection) {
        try {
            log.info("Getting data for table: {}, page: {}, pageSize: {}", tableName, page, pageSize);

            if (!isValidIdentifier(tableName)) {
                throw new IllegalArgumentException("Invalid table name: " + tableName);
            }

            int offset = (page - 1) * pageSize;
            String countSql = "SELECT COUNT(*) FROM " + tableName;
            int totalRows = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String orderBy = "";
            if (sortColumn != null && !sortColumn.isEmpty() && isValidIdentifier(sortColumn)) {
                orderBy = " ORDER BY \"" + sortColumn + "\" " + (sortDirection != null ? sortDirection : "ASC");
            }

            String dataSql = "SELECT * FROM " + tableName + orderBy + " OFFSET ? LIMIT ?";
            List<Map<String, Object>> rows = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> columns = getTableColumns(getCurrentSchema(), tableName);
            List<Map<String, String>> simpleColumns = columns.stream().map(col -> {
                Map<String, String> simpleCol = new HashMap<>();
                simpleCol.put("name", (String) col.get("column_name"));
                simpleCol.put("type", (String) col.get("data_type"));
                simpleCol.put("nullable", (String) col.get("is_nullable"));
                return simpleCol;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("rows", rows);
            result.put("columns", simpleColumns);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalRows", totalRows);
            result.put("totalPages", (int) Math.ceil((double) totalRows / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getTableDataWithPagination for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve data for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTableDataAdvanced(String tableName, int page, int pageSize,
                                                    String sortColumn, String sortDirection,
                                                    String filter) {
        try {
            log.info("Getting advanced data for table: {}, page: {}, pageSize: {}", tableName, page, pageSize);

            if (!isValidIdentifier(tableName)) {
                throw new IllegalArgumentException("Invalid table name: " + tableName);
            }

            String countSql = "SELECT COUNT(*) FROM " + tableName;
            if (filter != null && !filter.isEmpty()) {
                countSql += " WHERE " + sanitizeFilter(filter);
            }
            int totalRows = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String colSql = "SELECT column_name, data_type, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = current_schema() AND table_name = ? " +
                    "ORDER BY ordinal_position";
            List<Map<String, Object>> allColumns = getJdbcTemplate().queryForList(colSql, tableName);

            String orderBy = "";
            if (sortColumn != null && !sortColumn.isEmpty() && isValidIdentifier(sortColumn)) {
                String dir = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
                orderBy = " ORDER BY \"" + sortColumn + "\" " + dir;
            }

            String whereClause = "";
            if (filter != null && !filter.isEmpty()) {
                whereClause = " WHERE " + sanitizeFilter(filter);
            }

            int offset = (page - 1) * pageSize;

            String dataSql = "SELECT * FROM " + tableName + whereClause + orderBy + " OFFSET ? LIMIT ?";

            List<Map<String, Object>> rows = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> formattedRows = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Map<String, Object> formattedRow = new HashMap<>();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    formattedRow.put(entry.getKey().toLowerCase(), entry.getValue());
                }
                formattedRows.add(formattedRow);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rows", formattedRows);
            result.put("columns", allColumns);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalRows", totalRows);
            result.put("totalPages", (int) Math.ceil((double) totalRows / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getTableDataAdvanced for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve advanced table data: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTableDetails(String owner, String tableName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    c.relname as table_name, " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    'VALID' as table_status, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    0 as avg_row_len, " +
                    "    c.relpages as blocks, " +
                    "    c.relpages as empty_blocks, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed, " +
                    "    NULL as degree, " +
                    "    NULL as instances, " +
                    "    false as cache, " +
                    "    NULL as table_lock, " +
                    "    false as row_movement, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as object_status " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r'";

            List<Map<String, Object>> result = getJdbcTemplate().queryForList(sql, owner, tableName);

            if (!result.isEmpty()) {
                details.putAll(result.get(0));
                addTableAdditionalInfo(details, owner, tableName);
            } else {
                details.put("error", "Table not found");
                details.put("exists", false);
            }

        } catch (Exception e) {
            log.warn("Error getting table details for {}.{}: {}", owner, tableName, e.getMessage());
            details.put("error", e.getMessage());
        }
        return details;
    }

    public List<Map<String, Object>> getTableColumns(String owner, String tableName) {
        try {
            String sql = "SELECT " +
                    "    ordinal_position as column_id, " +
                    "    column_name, " +
                    "    data_type, " +
                    "    character_maximum_length as data_length, " +
                    "    numeric_precision as data_precision, " +
                    "    numeric_scale as data_scale, " +
                    "    is_nullable as nullable, " +
                    "    column_default as data_default, " +
                    "    character_maximum_length as char_length, " +
                    "    NULL as char_used " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ? " +
                    "ORDER BY ordinal_position";

            return getJdbcTemplate().queryForList(sql, owner, tableName);
        } catch (Exception e) {
            log.warn("Error getting columns for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableConstraints(String owner, String tableName) {
        try {
            String sql = "SELECT " +
                    "    tc.constraint_name, " +
                    "    tc.constraint_type, " +
                    "    'ENABLED' as constraint_status, " +
                    "    CASE WHEN tc.is_deferrable = 'YES' THEN 'DEFERRABLE' ELSE 'NOT DEFERRABLE' END as deferrable, " +
                    "    'IMMEDIATE' as deferred, " +  // PostgreSQL doesn't have is_deferred column in information_schema
                    "    'VALIDATED' as validated, " +
                    "    NULL as references_owner, " +
                    "    NULL as references_constraint, " +
                    "    fk.delete_rule, " +
                    "    NULL as index_owner, " +
                    "    NULL as index_name, " +
                    "    false as invalid, " +
                    "    false as view_related, " +
                    "    (SELECT string_agg(column_name, ', ') " +
                    "     FROM information_schema.key_column_usage kcu " +
                    "     WHERE kcu.constraint_name = tc.constraint_name " +
                    "       AND kcu.constraint_schema = tc.constraint_schema) as columns, " +
                    "    (SELECT COUNT(*) FROM information_schema.key_column_usage kcu " +
                    "     WHERE kcu.constraint_name = tc.constraint_name " +
                    "       AND kcu.constraint_schema = tc.constraint_schema) as column_count " +
                    "FROM information_schema.table_constraints tc " +
                    "LEFT JOIN information_schema.referential_constraints fk " +
                    "    ON tc.constraint_name = fk.constraint_name " +
                    "    AND tc.constraint_schema = fk.constraint_schema " +
                    "WHERE tc.table_schema = ? AND tc.table_name = ? " +
                    "ORDER BY tc.constraint_type, tc.constraint_name";

            return getJdbcTemplate().queryForList(sql, owner, tableName);
        } catch (Exception e) {
            log.warn("Error getting constraints for {}.{}: {}", owner, tableName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }



    public List<Map<String, Object>> getTableIndexes(String owner, String tableName) {
        try {
            String sql = "SELECT " +
                    "    i.indexname as index_name, " +
                    "    'BTREE' as index_type, " +
                    "    CASE WHEN i.indisunique THEN 'UNIQUE' ELSE 'NONUNIQUE' END as uniqueness, " +
                    "    ts.spcname as tablespace_name, " +
                    "    'VALID' as index_status, " +
                    "    'VISIBLE' as visibility, " +
                    "    NULL as distinct_keys, " +
                    "    NULL as leaf_blocks, " +
                    "    NULL as clustering_factor, " +
                    "    false as partitioned, " +
                    "    false as temporary, " +
                    "    false as dropped, " +
                    "    false as funcdict_index, " +
                    "    false as join_index, " +
                    "    NULL as domidx_opstatus, " +
                    "    NULL as ityp_name, " +
                    "    NULL as parameters, " +
                    "    (SELECT string_agg(attname, ', ') " +
                    "     FROM pg_attribute a " +
                    "     WHERE a.attrelid = i.indexrelid AND a.attnum > 0) as columns, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a " +
                    "     WHERE a.attrelid = i.indexrelid AND a.attnum > 0) as column_count, " +
                    "    pg_relation_size(i.indexrelid) as size_bytes " +
                    "FROM pg_index i " +
                    "JOIN pg_class c ON i.indrelid = c.oid " +
                    "JOIN pg_class idx ON i.indexrelid = idx.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "LEFT JOIN pg_tablespace ts ON idx.reltablespace = ts.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? " +
                    "ORDER BY i.indexname";

            return getJdbcTemplate().queryForList(sql, owner, tableName);
        } catch (Exception e) {
            log.warn("Error getting indexes for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getTableColumnsPaginated(String tableName, String owner, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ?";

            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class, owner, tableName);

            String colSql = "SELECT " +
                    "    ordinal_position as column_id, " +
                    "    column_name, " +
                    "    data_type, " +
                    "    character_maximum_length as data_length, " +
                    "    numeric_precision as data_precision, " +
                    "    numeric_scale as data_scale, " +
                    "    is_nullable as nullable, " +
                    "    column_default as data_default, " +
                    "    character_maximum_length as char_length, " +
                    "    NULL as char_used " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ? " +
                    "ORDER BY ordinal_position " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> columns = getJdbcTemplate().queryForList(
                    colSql, owner, tableName, offset, pageSize);

            result.put("items", columns);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getTableColumnsPaginated for {}.{}: {}", owner, tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve table columns: " + e.getMessage(), e);
        }
        return result;
    }

    public Map<String, Object> getTableStatistics(String tableName) {
        try {
            Map<String, Object> tableLocation = findObjectLocation(tableName, "TABLE");
            String owner = (String) tableLocation.get("owner");

            if (owner == null) {
                owner = getCurrentSchema();
            }

            String sql = "SELECT " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    c.relpages as blocks, " +
                    "    0 as empty_blocks, " +
                    "    0 as avg_space, " +
                    "    0 as chain_cnt, " +
                    "    0 as avg_row_len, " +
                    "    pg_stat_get_live_tuples(c.oid) as sample_size, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed, " +
                    "    true as global_stats, " +
                    "    false as user_stats, " +
                    "    NULL as stattype_locked " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r'";

            return getJdbcTemplate().queryForMap(sql, owner, tableName);

        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("message", "No statistics found");
            return empty;
        } catch (Exception e) {
            log.error("Error in getTableStatistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve table statistics: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getTablesWithRowCount() {
        try {
            String sql = "SELECT " +
                    "    c.relname as table_name, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed, " +
                    "    'VALID' as status " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'r' " +
                    "ORDER BY num_rows DESC NULLS LAST";
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getTablesWithRowCount: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableCountByTablespace() {
        try {
            String sql = "SELECT " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    COUNT(*) as table_count, " +
                    "    SUM(pg_stat_get_live_tuples(c.oid)) as total_rows, " +
                    "    SUM(c.relpages) as total_blocks, " +
                    "    COUNT(*) as valid_count, " +
                    "    0 as invalid_count " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'r' " +
                    "GROUP BY tablespace_name " +
                    "ORDER BY tablespace_name";
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getTableCountByTablespace: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getRecentTables(int days) {
        try {
            // PostgreSQL doesn't track creation/modification dates in the same way
            // Return all tables with basic info
            String sql = "SELECT " +
                    "    c.relname as table_name, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'r' " +
                    "ORDER BY c.relname DESC";
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getRecentTables: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> searchTables(String searchPattern) {
        try {
            String sql = "SELECT " +
                    "    c.relname as table_name, " +
                    "    n.nspname as owner, " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    'VALID' as table_status, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed, " +
                    "    NULL as created, " +
                    "    'VALID' as object_status " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'r' " +
                    "AND c.relname ILIKE ? " +
                    "ORDER BY n.nspname, c.relname";
            return getJdbcTemplate().queryForList(sql, "%" + searchPattern + "%");
        } catch (Exception e) {
            log.error("Error in searchTables: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTablesBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    c.relname as table_name, " +
                    "    n.nspname as owner, " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    'VALID' as status, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed, " +
                    "    NULL as created, " +
                    "    'VALID' as object_status " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relkind = 'r' " +
                    "ORDER BY c.relname";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getTablesBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getTablesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            String countSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'r'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    c.relname as table_name, " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    'VALID' as status, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    0 as avg_row_len, " +
                    "    c.relpages as blocks, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'r' " +
                    "ORDER BY c.relname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> tables = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", tables);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getTablesPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public List<Map<String, Object>> getTablePartitions(String owner, String tableName) {
        // PostgreSQL uses table inheritance for partitioning, but doesn't have a simple partitions view
        try {
            String sql = "SELECT " +
                    "    c.relname as partition_name, " +
                    "    0 as subpartition_count, " +
                    "    pg_get_expr(c.relpartbound, c.oid) as high_value, " +
                    "    0 as high_value_length, " +
                    "    c.relispartition as partition_position, " +
                    "    CASE WHEN c.reltablespace = 0 THEN 'pg_default' ELSE ts.spcname END as tablespace_name, " +
                    "    pg_stat_get_live_tuples(c.oid) as num_rows, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_analyzed " +
                    "FROM pg_class c " +
                    "LEFT JOIN pg_tablespace ts ON c.reltablespace = ts.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relispartition = true " +
                    "ORDER BY c.relname";
            return getJdbcTemplate().queryForList(sql, owner, tableName);
        } catch (Exception e) {
            log.debug("No partitions found for {}.{}", owner, tableName);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private Long getTableSize(String tableName) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT pg_total_relation_size(quote_ident(?))",
                    Long.class, tableName);
        } catch (Exception e) {
            return 0L;
        }
    }

    private String getTableComment(String tableName) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT obj_description(quote_ident(?)::regclass, 'pg_class')",
                    String.class, tableName);
        } catch (Exception e) {
            return "";
        }
    }

    private List<Map<String, Object>> transformTableItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("TABLE".equals(type)) {
                transformedItem.put("id", "table-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentSchema());
                transformedItem.put("type", "TABLE");
                transformedItem.put("status", item.get("status"));
                transformedItem.put("rowCount", item.get("num_rows"));
                transformedItem.put("size", formatBytes(getLongValue(item.get("size_bytes"))));
                transformedItem.put("comments", item.get("comments"));
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("last_analyzed"));
                transformedItem.put("tablespace", item.get("tablespace_name"));
                transformedItem.put("icon", "table");
                transformedItem.put("isSynonym", false);
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }

    private void addTableAdditionalInfo(Map<String, Object> details, String owner, String tableName) {
        try {
            // Get column count
            String colCountSql = "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ?";
            Integer columnCount = getJdbcTemplate().queryForObject(colCountSql, Integer.class, owner, tableName);
            details.put("column_count", columnCount);

            // Add column details
            List<Map<String, Object>> columns = getTableColumns(owner, tableName);
            details.put("columns", columns);

            // Get size
            String sizeSql = "SELECT pg_total_relation_size(quote_ident(?) || '.' || quote_ident(?))";
            Long sizeBytes = getJdbcTemplate().queryForObject(sizeSql, Long.class, owner, tableName);
            details.put("size_bytes", sizeBytes != null ? sizeBytes : 0);

            // Get comments
            String commentSql = "SELECT obj_description(quote_ident(?) || '.' || quote_ident(?)::regclass, 'pg_class')";
            String comments = getJdbcTemplate().queryForObject(commentSql, String.class, owner, tableName);
            details.put("comments", comments != null ? comments : "");

        } catch (Exception e) {
            log.debug("Could not get additional info for {}.{}: {}", owner, tableName, e.getMessage());
        }
    }

    public Map<String, Object> getTableDetails(String tableName) {
        try {
            Map<String, Object> tableLocation = findObjectLocation(tableName, "TABLE");
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getTableDetails(owner, tableName);
        } catch (Exception e) {
            log.error("Error in getTableDetails: {}", e.getMessage(), e);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("table_name", tableName);
            emptyResult.put("message", "Table not found");
            return emptyResult;
        }
    }

    public List<Map<String, Object>> getTableColumns(String tableName) {
        try {
            Map<String, Object> tableLocation = findObjectLocation(tableName, "TABLE");
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getTableColumns(owner, tableName);
        } catch (Exception e) {
            log.error("Error in getTableColumns: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableConstraints(String tableName) {
        try {
            Map<String, Object> tableLocation = findObjectLocation(tableName, "TABLE");
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getTableConstraints(owner, tableName);
        } catch (Exception e) {
            log.error("Error in getTableConstraints: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableIndexes(String tableName) {
        try {
            Map<String, Object> tableLocation = findObjectLocation(tableName, "TABLE");
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getTableIndexes(owner, tableName);
        } catch (Exception e) {
            log.error("Error in getTableIndexes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}