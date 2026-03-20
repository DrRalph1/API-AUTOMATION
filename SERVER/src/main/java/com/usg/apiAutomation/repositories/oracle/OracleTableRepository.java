package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OracleTableRepository extends OracleRepository {

    // ============================================================
    // TABLE METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getAllTables() {
        try {
            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    t.tablespace_name, " +
                    "    t.status, " +
                    "    t.num_rows, " +
                    "    t.avg_row_len, " +
                    "    t.blocks, " +
                    "    t.last_analyzed, " +
                    "    o.created, " +
                    "    o.status as object_status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM user_tables t " +
                    "LEFT JOIN user_objects o ON t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "ORDER BY t.table_name";

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
                transformed.put("id", "table-" + System.currentTimeMillis() + "-" + table.get("table_name"));
                transformed.put("name", table.get("table_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "TABLE");
                transformed.put("status", table.get("status") != null ? table.get("status") : table.get("object_status"));
                transformed.put("rowCount", table.get("num_rows"));
                transformed.put("size", formatBytes(getLongValue(getTableSize((String) table.get("table_name")))));
                transformed.put("comments", getTableComment((String) table.get("table_name")));
                transformed.put("created", table.get("created"));
                transformed.put("lastModified", table.get("last_analyzed"));
                transformed.put("tablespace", table.get("tablespace_name"));
                transformed.put("icon", "table");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            result.addAll(getTableSynonyms());
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (tables + synonyms)", result.size());

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

            String tableCountSql = "SELECT COUNT(*) FROM user_tables";
            int tableCount = getJdbcTemplate().queryForObject(tableCountSql, Integer.class);
            int synonymCount = countTableSynonyms();
            int totalCount = tableCount + synonymCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (tableCount > 0 && offset < tableCount) {
                int tableOffset = offset;
                int tableLimit = Math.min(pageSize, tableCount - tableOffset);

                if (tableLimit > 0) {
                    String tableSql = "SELECT " +
                            "    t.table_name as name, " +
                            "    'TABLE' as type, " +
                            "    t.tablespace_name, " +
                            "    t.status, " +
                            "    t.num_rows, " +
                            "    t.last_analyzed, " +
                            "    o.created, " +
                            "    (SELECT comments FROM user_tab_comments WHERE table_name = t.table_name) as comments, " +
                            "    (SELECT SUM(bytes) FROM user_segments WHERE segment_name = t.table_name AND segment_type = 'TABLE') as size_bytes " +
                            "FROM user_tables t " +
                            "LEFT JOIN user_objects o ON t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                            "ORDER BY t.table_name " +
                            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    List<Map<String, Object>> tables = getJdbcTemplate().queryForList(tableSql, tableOffset, tableLimit);
                    allItems.addAll(tables);
                }
            }

            if (allItems.size() < pageSize && synonymCount > 0) {
                addTableSynonymsPaginated(allItems, page, pageSize, tableCount);
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
            Map<String, Object> synonymInfo = checkIfSynonymAndGetTarget(tableName, "TABLE");

            if (synonymInfo != null && (boolean) synonymInfo.get("isSynonym")) {
                return getSynonymTableDetails(tableName, synonymInfo);
            }

            Map<String, Object> details = getTableDetails(getCurrentUser(), tableName);
            List<Map<String, Object>> columns = getTableColumns(getCurrentUser(), tableName);
            List<Map<String, Object>> constraints = getTableConstraints(getCurrentUser(), tableName);
            List<Map<String, Object>> indexes = getTableIndexes(getCurrentUser(), tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("table_name", tableName);
            result.put("name", tableName);
            result.put("owner", getCurrentUser());
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
            Map<String, Object> synonymInfo = checkIfSynonymAndGetTarget(tableName, "TABLE");

            if (synonymInfo != null && (boolean) synonymInfo.get("isSynonym")) {
                result.put("table_name", tableName);
                result.put("name", tableName);
                result.put("owner", getCurrentUser());
                result.put("type", "SYNONYM");
                result.put("isSynonym", true);
                result.put("targetOwner", synonymInfo.get("targetOwner"));
                result.put("targetName", synonymInfo.get("targetName"));
                result.put("targetType", synonymInfo.get("targetType"));
                result.put("dbLink", synonymInfo.get("dbLink"));
                result.put("isRemote", synonymInfo.get("isRemote"));
                result.put("icon", "synonym");
                result.put("targetIcon", "table");

                if (!(boolean) synonymInfo.get("isRemote") && synonymInfo.get("targetName") != null) {
                    Map<String, Object> columnsResult = getTableColumnsPaginated(
                            (String) synonymInfo.get("targetName"),
                            (String) synonymInfo.get("targetOwner"),
                            page, pageSize);
                    result.put("columns", columnsResult.get("items"));
                    result.put("totalColumns", columnsResult.get("totalCount"));
                    result.put("page", page);
                    result.put("pageSize", pageSize);
                    result.put("totalPages", columnsResult.get("totalPages"));
                }
                return result;
            }

            Map<String, Object> details = getTableDetails(getCurrentUser(), tableName);
            Map<String, Object> columnsResult = getTableColumnsPaginated(tableName, getCurrentUser(), page, pageSize);
            List<Map<String, Object>> constraints = getTableConstraints(getCurrentUser(), tableName);
            List<Map<String, Object>> indexes = getTableIndexes(getCurrentUser(), tableName);

            result.put("table_name", tableName);
            result.put("name", tableName);
            result.put("owner", getCurrentUser());
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

            String dataSql = "SELECT * FROM " + tableName + orderBy + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> rows = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> columns = getTableColumns(getCurrentUser(), tableName);
            List<Map<String, String>> simpleColumns = columns.stream().map(col -> {
                Map<String, String> simpleCol = new HashMap<>();
                simpleCol.put("name", (String) col.get("column_name"));
                simpleCol.put("type", (String) col.get("data_type"));
                simpleCol.put("nullable", (String) col.get("nullable"));
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

            String colSql = "SELECT column_name, data_type, nullable FROM all_tab_columns " +
                    "WHERE table_name = UPPER(?) ORDER BY column_id";
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

            String dataSql = "SELECT * FROM " + tableName + whereClause + orderBy +
                    " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

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
            String sql;
            List<Map<String, Object>> result;

            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    t.table_name, " +
                        "    t.tablespace_name, " +
                        "    t.status as table_status, " +
                        "    t.num_rows, " +
                        "    t.avg_row_len, " +
                        "    t.blocks, " +
                        "    t.empty_blocks, " +
                        "    t.last_analyzed, " +
                        "    t.degree, " +
                        "    t.instances, " +
                        "    t.cache, " +
                        "    t.table_lock, " +
                        "    t.row_movement, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status " +
                        "FROM user_tables t " +
                        "LEFT JOIN user_objects o ON t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                        "WHERE UPPER(t.table_name) = UPPER(?)";

                result = getJdbcTemplate().queryForList(sql, tableName);
            } else {
                sql = "SELECT " +
                        "    t.owner, " +
                        "    t.table_name, " +
                        "    t.tablespace_name, " +
                        "    t.status as table_status, " +
                        "    t.num_rows, " +
                        "    t.avg_row_len, " +
                        "    t.blocks, " +
                        "    t.empty_blocks, " +
                        "    t.last_analyzed, " +
                        "    t.degree, " +
                        "    t.instances, " +
                        "    t.cache, " +
                        "    t.table_lock, " +
                        "    t.row_movement, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status " +
                        "FROM all_tables t " +
                        "LEFT JOIN all_objects o ON t.owner = o.owner AND t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                        "WHERE UPPER(t.owner) = UPPER(?) AND UPPER(t.table_name) = UPPER(?)";

                result = getJdbcTemplate().queryForList(sql, owner, tableName);
            }

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
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
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
                        "    c.char_used, " +
                        "    s.num_distinct, " +
                        "    s.density, " +
                        "    s.num_nulls, " +
                        "    s.last_analyzed as stats_last_analyzed " +
                        "FROM user_tab_columns c " +
                        "LEFT JOIN user_tab_col_statistics s " +
                        "    ON c.table_name = s.table_name AND c.column_name = s.column_name " +
                        "WHERE UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.column_id";

                return getJdbcTemplate().queryForList(sql, tableName);
            } else {
                sql = "SELECT " +
                        "    c.column_id, " +
                        "    c.column_name, " +
                        "    c.data_type, " +
                        "    c.data_length, " +
                        "    c.data_precision, " +
                        "    c.data_scale, " +
                        "    c.nullable, " +
                        "    c.data_default, " +
                        "    s.num_distinct, " +
                        "    s.num_nulls, " +
                        "    s.last_analyzed as stats_last_analyzed " +
                        "FROM all_tab_columns c " +
                        "LEFT JOIN all_tab_col_statistics s " +
                        "    ON c.owner = s.owner AND c.table_name = s.table_name AND c.column_name = s.column_name " +
                        "WHERE UPPER(c.owner) = UPPER(?) AND UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.column_id";

                return getJdbcTemplate().queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            log.warn("Error getting columns for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableConstraints(String owner, String tableName) {
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    c.constraint_name, " +
                        "    c.constraint_type, " +
                        "    c.status as constraint_status, " +
                        "    c.deferrable, " +
                        "    c.deferred, " +
                        "    c.validated, " +
                        "    c.r_owner as references_owner, " +
                        "    c.r_constraint_name as references_constraint, " +
                        "    c.delete_rule, " +
                        "    c.index_owner, " +
                        "    c.index_name, " +
                        "    c.invalid, " +
                        "    c.view_related, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY position) " +
                        "     FROM user_cons_columns " +
                        "     WHERE constraint_name = c.constraint_name) as columns, " +
                        "    (SELECT COUNT(*) FROM user_cons_columns " +
                        "     WHERE constraint_name = c.constraint_name) as column_count " +
                        "FROM user_constraints c " +
                        "WHERE UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.constraint_type, c.constraint_name";

                return getJdbcTemplate().queryForList(sql, tableName);
            } else {
                sql = "SELECT " +
                        "    c.owner, " +
                        "    c.constraint_name, " +
                        "    c.constraint_type, " +
                        "    c.status as constraint_status, " +
                        "    c.deferrable, " +
                        "    c.deferred, " +
                        "    c.validated, " +
                        "    c.r_owner as references_owner, " +
                        "    c.r_constraint_name as references_constraint, " +
                        "    c.delete_rule, " +
                        "    c.index_owner, " +
                        "    c.index_name, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY position) " +
                        "     FROM all_cons_columns " +
                        "     WHERE owner = c.owner AND constraint_name = c.constraint_name) as columns, " +
                        "    (SELECT COUNT(*) FROM all_cons_columns " +
                        "     WHERE owner = c.owner AND constraint_name = c.constraint_name) as column_count " +
                        "FROM all_constraints c " +
                        "WHERE UPPER(c.owner) = UPPER(?) AND UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.constraint_type, c.constraint_name";

                return getJdbcTemplate().queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            log.warn("Error getting constraints for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableIndexes(String owner, String tableName) {
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    i.index_name, " +
                        "    i.index_type, " +
                        "    i.uniqueness, " +
                        "    i.tablespace_name, " +
                        "    i.status as index_status, " +
                        "    i.visibility, " +
                        "    i.distinct_keys, " +
                        "    i.leaf_blocks, " +
                        "    i.clustering_factor, " +
                        "    i.partitioned, " +
                        "    i.temporary, " +
                        "    i.dropped, " +
                        "    i.funcdict_index, " +
                        "    i.join_index, " +
                        "    i.domidx_opstatus, " +
                        "    i.ityp_name, " +
                        "    i.parameters, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY column_position) " +
                        "     FROM user_ind_columns " +
                        "     WHERE index_name = i.index_name) as columns, " +
                        "    (SELECT COUNT(*) FROM user_ind_columns " +
                        "     WHERE index_name = i.index_name) as column_count, " +
                        "    (SELECT bytes FROM user_segments WHERE segment_name = i.index_name AND ROWNUM = 1) as size_bytes " +
                        "FROM user_indexes i " +
                        "WHERE UPPER(i.table_name) = UPPER(?) " +
                        "ORDER BY i.index_name";

                return getJdbcTemplate().queryForList(sql, tableName);
            } else {
                sql = "SELECT " +
                        "    i.owner, " +
                        "    i.index_name, " +
                        "    i.index_type, " +
                        "    i.uniqueness, " +
                        "    i.tablespace_name, " +
                        "    i.status as index_status, " +
                        "    i.visibility, " +
                        "    i.distinct_keys, " +
                        "    i.leaf_blocks, " +
                        "    i.clustering_factor, " +
                        "    i.partitioned, " +
                        "    i.temporary, " +
                        "    (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY column_position) " +
                        "     FROM all_ind_columns " +
                        "     WHERE index_owner = i.owner AND index_name = i.index_name) as columns, " +
                        "    (SELECT COUNT(*) FROM all_ind_columns " +
                        "     WHERE index_owner = i.owner AND index_name = i.index_name) as column_count " +
                        "FROM all_indexes i " +
                        "WHERE UPPER(i.owner) = UPPER(?) AND UPPER(i.table_name) = UPPER(?) " +
                        "ORDER BY i.index_name";

                return getJdbcTemplate().queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            log.warn("Error getting indexes for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getTableColumnsPaginated(String tableName, String owner, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM all_tab_columns " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";

            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class, owner, tableName);

            String colSql = "SELECT * FROM ( " +
                    "  SELECT a.*, ROWNUM rnum FROM ( " +
                    "    SELECT " +
                    "        column_id, " +
                    "        column_name, " +
                    "        data_type, " +
                    "        data_length, " +
                    "        data_precision, " +
                    "        data_scale, " +
                    "        nullable, " +
                    "        data_default, " +
                    "        char_length, " +
                    "        char_used " +
                    "    FROM all_tab_columns " +
                    "    WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                    "    ORDER BY column_id " +
                    "  ) a WHERE ROWNUM <= ? " +
                    ") WHERE rnum > ?";

            List<Map<String, Object>> columns = getJdbcTemplate().queryForList(
                    colSql, owner, tableName, offset + pageSize, offset);

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
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
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
                        "    user_stats, " +
                        "    stattype_locked " +
                        "FROM user_tab_statistics " +
                        "WHERE UPPER(table_name) = UPPER(?)";

                return getJdbcTemplate().queryForMap(sql, tableName);
            } else {
                sql = "SELECT " +
                        "    num_rows, " +
                        "    blocks, " +
                        "    empty_blocks, " +
                        "    avg_row_len, " +
                        "    sample_size, " +
                        "    last_analyzed " +
                        "FROM all_tab_statistics " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";

                return getJdbcTemplate().queryForMap(sql, owner, tableName);
            }

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
                    "    table_name, " +
                    "    num_rows, " +
                    "    last_analyzed, " +
                    "    status " +
                    "FROM user_tables " +
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
                    "    tablespace_name, " +
                    "    COUNT(*) as table_count, " +
                    "    SUM(num_rows) as total_rows, " +
                    "    SUM(blocks) as total_blocks, " +
                    "    SUM(CASE WHEN status = 'VALID' THEN 1 ELSE 0 END) as valid_count, " +
                    "    SUM(CASE WHEN status = 'INVALID' THEN 1 ELSE 0 END) as invalid_count " +
                    "FROM user_tables " +
                    "WHERE tablespace_name IS NOT NULL " +
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
            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    t.tablespace_name, " +
                    "    t.num_rows " +
                    "FROM user_tables t " +
                    "JOIN user_objects o ON t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "WHERE (o.created > SYSDATE - ? OR o.last_ddl_time > SYSDATE - ?) " +
                    "ORDER BY o.last_ddl_time DESC";
            return getJdbcTemplate().queryForList(sql, days, days);
        } catch (Exception e) {
            log.error("Error in getRecentTables: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> searchTables(String searchPattern) {
        try {
            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    t.owner, " +
                    "    t.tablespace_name, " +
                    "    t.status as table_status, " +
                    "    t.num_rows, " +
                    "    t.last_analyzed, " +
                    "    o.created, " +
                    "    o.status as object_status " +
                    "FROM all_tables t " +
                    "LEFT JOIN all_objects o ON t.owner = o.owner AND t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "WHERE UPPER(t.table_name) LIKE UPPER(?) " +
                    "ORDER BY t.owner, t.table_name";
            return getJdbcTemplate().queryForList(sql, "%" + searchPattern + "%");
        } catch (Exception e) {
            log.error("Error in searchTables: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTablesBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    t.table_name, " +
                    "    t.owner, " +
                    "    t.tablespace_name, " +
                    "    t.status, " +
                    "    t.num_rows, " +
                    "    t.last_analyzed, " +
                    "    o.created, " +
                    "    o.status as object_status " +
                    "FROM all_tables t " +
                    "LEFT JOIN all_objects o ON t.owner = o.owner AND t.table_name = o.object_name AND o.object_type = 'TABLE' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.table_name";
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
            String countSql = "SELECT COUNT(*) FROM user_tables";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    table_name, " +
                    "    tablespace_name, " +
                    "    status, " +
                    "    num_rows, " +
                    "    avg_row_len, " +
                    "    blocks, " +
                    "    last_analyzed " +
                    "FROM user_tables " +
                    "ORDER BY table_name " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

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
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    partition_name, " +
                        "    subpartition_count, " +
                        "    high_value, " +
                        "    high_value_length, " +
                        "    partition_position, " +
                        "    tablespace_name, " +
                        "    num_rows, " +
                        "    last_analyzed " +
                        "FROM user_tab_partitions " +
                        "WHERE UPPER(table_name) = UPPER(?) " +
                        "ORDER BY partition_position";
                return getJdbcTemplate().queryForList(sql, tableName);
            } else {
                sql = "SELECT " +
                        "    partition_name, " +
                        "    high_value, " +
                        "    partition_position, " +
                        "    tablespace_name, " +
                        "    num_rows " +
                        "FROM all_tab_partitions " +
                        "WHERE UPPER(table_owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                        "ORDER BY partition_position";
                return getJdbcTemplate().queryForList(sql, owner, tableName);
            }
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
                    "SELECT SUM(bytes) FROM user_segments WHERE segment_name = UPPER(?) AND segment_type = 'TABLE'",
                    Long.class, tableName);
        } catch (Exception e) {
            return 0L;
        }
    }

    private String getTableComment(String tableName) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT comments FROM user_tab_comments WHERE table_name = UPPER(?)",
                    String.class, tableName);
        } catch (Exception e) {
            return "";
        }
    }

    private List<Map<String, Object>> getTableSynonyms() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String synonymSql =
                    "SELECT " +
                            "    s.synonym_name as name, " +
                            "    'SYNONYM' as type, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                            "        ELSE o.status " +
                            "    END as status, " +
                            "    NULL as created, " +
                            "    NULL as last_analyzed, " +
                            "    NULL as num_rows, " +
                            "    s.table_owner as target_owner, " +
                            "    s.table_name as target_name, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_TABLE' " +
                            "        ELSE o.object_type " +
                            "    END as target_type, " +
                            "    s.db_link, " +
                            "    (SELECT comments FROM all_tab_comments WHERE owner = s.table_owner AND table_name = s.table_name) as comments, " +
                            "    (SELECT tablespace_name FROM all_tables WHERE owner = s.table_owner AND table_name = s.table_name) as tablespace_name " +
                            "FROM user_synonyms s " +
                            "LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                            "    AND s.table_name = o.object_name " +
                            "WHERE (o.object_type = 'TABLE' OR s.db_link IS NOT NULL) " +
                            "ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);

            for (Map<String, Object> syn : synonyms) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "syn-table-" + System.currentTimeMillis() + "-" + syn.get("name"));
                transformed.put("name", syn.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                transformed.put("rowCount", 0);
                transformed.put("size", "0 Bytes");
                transformed.put("comments", syn.get("comments"));
                transformed.put("created", null);
                transformed.put("lastModified", null);
                transformed.put("tablespace", syn.get("tablespace_name"));
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "TABLE");
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);
                transformed.put("isSynonym", true);
                transformed.put("icon", "synonym");
                transformed.put("targetIcon", "table");

                String targetType = (String) syn.get("target_type");
                if (targetType != null) {
                    transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                } else {
                    transformed.put("targetDisplayType", "Table");
                }

                if (syn.get("db_link") == null && syn.get("target_name") != null) {
                    try {
                        String targetSql = "SELECT status, created, last_ddl_time, num_rows " +
                                "FROM all_objects o " +
                                "LEFT JOIN all_tables t ON o.owner = t.owner AND o.object_name = t.table_name " +
                                "WHERE o.owner = ? AND o.object_name = ? AND o.object_type = 'TABLE'";
                        Map<String, Object> targetInfo = getJdbcTemplate().queryForMap(
                                targetSql, syn.get("target_owner"), syn.get("target_name"));
                        transformed.put("targetStatus", targetInfo.get("status"));
                        transformed.put("targetCreated", targetInfo.get("created"));
                        transformed.put("targetModified", targetInfo.get("last_ddl_time"));
                        transformed.put("targetRowCount", targetInfo.get("num_rows"));
                    } catch (Exception e) {
                        transformed.put("targetStatus", "UNKNOWN");
                    }
                } else if (syn.get("db_link") != null) {
                    transformed.put("targetStatus", "REMOTE");
                }

                result.add(transformed);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for tables: {}", e.getMessage());
        }
        return result;
    }

    private int countTableSynonyms() {
        try {
            String synonymCountSql =
                    "SELECT COUNT(*) FROM user_synonyms s " +
                            "WHERE EXISTS (SELECT 1 FROM all_objects " +
                            "              WHERE owner = s.table_owner " +
                            "                AND object_name = s.table_name " +
                            "                AND object_type = 'TABLE') " +
                            "   OR (s.db_link IS NOT NULL)";
            return getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
        } catch (Exception e) {
            log.warn("Error counting synonyms for tables: {}", e.getMessage());
            return 0;
        }
    }

    private void addTableSynonymsPaginated(List<Map<String, Object>> allItems, int page, int pageSize, int tableCount) {
        try {
            int offset = (page - 1) * pageSize;
            int synOffset;
            int synLimit;

            if (offset >= tableCount) {
                synOffset = offset - tableCount;
                synLimit = pageSize;
            } else {
                synOffset = 0;
                synLimit = pageSize - allItems.size();
            }

            if (synLimit > 0) {
                String synonymSql =
                        "SELECT * FROM ( " +
                                "  SELECT a.*, ROWNUM rnum FROM ( " +
                                "    SELECT " +
                                "      s.synonym_name as name, " +
                                "      'SYNONYM' as type, " +
                                "      NULL as tablespace_name, " +
                                "      CASE " +
                                "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                                "        ELSE o.status " +
                                "      END as status, " +
                                "      NULL as num_rows, " +
                                "      NULL as last_analyzed, " +
                                "      NULL as created, " +
                                "      (SELECT comments FROM all_tab_comments WHERE owner = s.table_owner AND table_name = s.table_name) as comments, " +
                                "      0 as size_bytes, " +
                                "      s.table_owner as target_owner, " +
                                "      s.table_name as target_name, " +
                                "      CASE " +
                                "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_TABLE' " +
                                "        ELSE o.object_type " +
                                "      END as target_type, " +
                                "      s.db_link " +
                                "    FROM user_synonyms s " +
                                "    LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                                "        AND s.table_name = o.object_name " +
                                "    WHERE (o.object_type = 'TABLE' OR s.db_link IS NOT NULL) " +
                                "    ORDER BY s.synonym_name " +
                                "  ) a " +
                                ") WHERE rnum > ? AND rnum <= ?";

                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(
                        synonymSql, synOffset, synOffset + synLimit);
                allItems.addAll(synonyms);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for tables: {}", e.getMessage());
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
                transformedItem.put("owner", getCurrentUser());
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
            } else {
                transformedItem.put("id", "syn-table-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "SYNONYM");
                transformedItem.put("status", item.get("status") != null ? item.get("status") : "VALID");
                transformedItem.put("rowCount", 0);
                transformedItem.put("size", "0 Bytes");
                transformedItem.put("comments", item.get("comments"));
                transformedItem.put("created", null);
                transformedItem.put("lastModified", null);
                transformedItem.put("tablespace", item.get("tablespace_name"));
                transformedItem.put("targetOwner", item.get("target_owner"));
                transformedItem.put("targetName", item.get("target_name"));
                transformedItem.put("targetType", item.get("target_type") != null ? item.get("target_type") : "TABLE");
                transformedItem.put("dbLink", item.get("db_link"));
                transformedItem.put("isRemote", item.get("db_link") != null);
                transformedItem.put("isSynonym", true);
                transformedItem.put("icon", "synonym");
                transformedItem.put("targetIcon", "table");
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }

    private Map<String, Object> getSynonymTableDetails(String tableName, Map<String, Object> synonymInfo) {
        Map<String, Object> result = new HashMap<>();
        result.put("table_name", tableName);
        result.put("name", tableName);
        result.put("owner", getCurrentUser());
        result.put("type", "SYNONYM");
        result.put("isSynonym", true);
        result.put("targetOwner", synonymInfo.get("targetOwner"));
        result.put("targetName", synonymInfo.get("targetName"));
        result.put("targetType", synonymInfo.get("targetType"));
        result.put("dbLink", synonymInfo.get("dbLink"));
        result.put("isRemote", synonymInfo.get("isRemote"));
        result.put("icon", "synonym");
        result.put("targetIcon", "table");

        String targetType = (String) synonymInfo.get("targetType");
        if (targetType != null) {
            result.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
        }

        if (!(boolean) synonymInfo.get("isRemote") && synonymInfo.get("targetName") != null) {
            try {
                Map<String, Object> targetDetails = getTableDetails(
                        (String) synonymInfo.get("targetOwner"),
                        (String) synonymInfo.get("targetName"));
                result.put("targetDetails", targetDetails);
                result.put("targetStatus", targetDetails.get("status"));
            } catch (Exception e) {
                result.put("targetStatus", "UNKNOWN");
            }
        } else {
            result.put("targetStatus", "REMOTE");
        }

        return result;
    }

    private void addTableAdditionalInfo(Map<String, Object> details, String owner, String tableName) {
        try {
            // Get column count
            String colCountSql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                colCountSql = "SELECT COUNT(*) FROM user_tab_columns WHERE UPPER(table_name) = UPPER(?)";
                Integer columnCount = getJdbcTemplate().queryForObject(colCountSql, Integer.class, tableName);
                details.put("column_count", columnCount);
            } else {
                colCountSql = "SELECT COUNT(*) FROM all_tab_columns WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                Integer columnCount = getJdbcTemplate().queryForObject(colCountSql, Integer.class, owner, tableName);
                details.put("column_count", columnCount);
            }

            // Add column details
            List<Map<String, Object>> columns = getTableColumns(owner, tableName);
            details.put("columns", columns);

            // Get size
            String sizeSql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sizeSql = "SELECT SUM(bytes) FROM user_segments WHERE UPPER(segment_name) = UPPER(?) AND segment_type = 'TABLE'";
                Long sizeBytes = getJdbcTemplate().queryForObject(sizeSql, Long.class, tableName);
                details.put("size_bytes", sizeBytes != null ? sizeBytes : 0);
            } else {
                sizeSql = "SELECT SUM(bytes) FROM all_segments WHERE UPPER(owner) = UPPER(?) AND UPPER(segment_name) = UPPER(?) AND segment_type = 'TABLE'";
                Long sizeBytes = getJdbcTemplate().queryForObject(sizeSql, Long.class, owner, tableName);
                details.put("size_bytes", sizeBytes != null ? sizeBytes : 0);
            }

            // Get comments
            String commentSql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                commentSql = "SELECT comments FROM user_tab_comments WHERE UPPER(table_name) = UPPER(?)";
                String comments = getJdbcTemplate().queryForObject(commentSql, String.class, tableName);
                details.put("comments", comments != null ? comments : "");
            } else {
                commentSql = "SELECT comments FROM all_tab_comments WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                String comments = getJdbcTemplate().queryForObject(commentSql, String.class, owner, tableName);
                details.put("comments", comments != null ? comments : "");
            }

        } catch (Exception e) {
            log.debug("Could not get additional info for {}.{}: {}", owner, tableName, e.getMessage());
        }
    }

    public Map<String, Object> getTableDetails(String tableName) {
        try {
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
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
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
            }
            return getTableColumns(owner, tableName);
        } catch (Exception e) {
            log.error("Error in getTableColumns: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableConstraints(String tableName) {
        try {
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
            }
            return getTableConstraints(owner, tableName);
        } catch (Exception e) {
            log.error("Error in getTableConstraints: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getTableIndexes(String tableName) {
        try {
            Map<String, Object> tableLocation = findTableLocation(tableName);
            String owner = (String) tableLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
            }
            return getTableIndexes(owner, tableName);
        } catch (Exception e) {
            log.error("Error in getTableIndexes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}