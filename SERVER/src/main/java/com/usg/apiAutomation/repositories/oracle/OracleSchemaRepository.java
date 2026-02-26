package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class OracleSchemaRepository {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(OracleSchemaRepository.class);

    // ============================================================
    // 1. PAGINATED OBJECT DETAILS REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> getObjectDetailsPaginated(String objectName, String objectType,
                                                         String owner, int page, int pageSize,
                                                         boolean includeCounts) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            String upperType = objectType.toUpperCase();
            int offset = (page - 1) * pageSize;

            // First, get basic object info
            String basicInfoSql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    status, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = ?";

            Map<String, Object> basicInfo;
            try {
                basicInfo = oracleJdbcTemplate.queryForMap(basicInfoSql, owner, objectName, objectType);
                result.putAll(basicInfo);
            } catch (EmptyResultDataAccessException e) {
                // Try without object type
                String altSql = "SELECT owner, object_name, object_type, status, created, last_ddl_time " +
                        "FROM all_objects WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";
                basicInfo = oracleJdbcTemplate.queryForMap(altSql, owner, objectName);
                result.putAll(basicInfo);
            }

            // Get total counts if requested or if we need them for pagination
            long totalColumns = 0;
            long totalParameters = 0;

            if (includeCounts) {
                // Get column count for tables/views
                if ("TABLE".equals(upperType) || "VIEW".equals(upperType)) {
                    String countSql = "SELECT COUNT(*) FROM all_tab_columns " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    totalColumns = oracleJdbcTemplate.queryForObject(countSql, Long.class, owner, objectName);
                }

                // Get parameter count for procedures/functions/packages
                if ("PROCEDURE".equals(upperType) || "FUNCTION".equals(upperType) || "PACKAGE".equals(upperType)) {
                    String paramCountSql = "SELECT COUNT(*) FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND argument_name IS NOT NULL";
                    totalParameters = oracleJdbcTemplate.queryForObject(paramCountSql, Long.class, owner, objectName);
                }

                result.put("totalColumns", totalColumns);
                result.put("totalParameters", totalParameters);
                result.put("totalCount", Math.max(totalColumns, totalParameters));

                // If we only need counts, return early
                if (includeCounts && pageSize == 0) {
                    return result;
                }
            }

            // Get paginated data based on object type
            switch (upperType) {
                case "TABLE":
                case "VIEW":
                    List<Map<String, Object>> columns = getTableColumnsPaginatedInternal(owner, objectName, offset, pageSize);
                    result.put("columns", columns);
                    if (!includeCounts) {
                        String countSql = "SELECT COUNT(*) FROM all_tab_columns " +
                                "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                        totalColumns = oracleJdbcTemplate.queryForObject(countSql, Long.class, owner, objectName);
                        result.put("totalColumns", totalColumns);
                        result.put("totalCount", totalColumns);
                    }
                    break;

                case "PROCEDURE":
                case "FUNCTION":
                case "PACKAGE":
                    List<Map<String, Object>> params = getArgumentsPaginatedInternal(owner, objectName, offset, pageSize);
                    result.put("parameters", params);
                    if (!includeCounts) {
                        String paramCountSql = "SELECT COUNT(*) FROM all_arguments " +
                                "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                                "AND argument_name IS NOT NULL";
                        totalParameters = oracleJdbcTemplate.queryForObject(paramCountSql, Long.class, owner, objectName);
                        result.put("totalParameters", totalParameters);
                        result.put("totalCount", totalParameters);
                    }
                    break;

                default:
                    result.put("message", "Pagination not supported for object type: " + objectType);
            }

            return result;

        } catch (Exception e) {
            log.error("Error in getObjectDetailsPaginated for {}.{}: {}", owner, objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve paginated object details: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 2. ADVANCED TABLE DATA REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> getTableDataAdvanced(String tableName, int page, int pageSize,
                                                    String sortColumn, String sortDirection,
                                                    String filter) {
        try {
            log.info("Getting advanced data for table: {}, page: {}, pageSize: {}", tableName, page, pageSize);

            // Validate table name to prevent SQL injection
            if (!isValidIdentifier(tableName)) {
                throw new IllegalArgumentException("Invalid table name: " + tableName);
            }

            // Get total count
            String countSql = "SELECT COUNT(*) FROM " + tableName;
            if (filter != null && !filter.isEmpty()) {
                countSql += " WHERE " + sanitizeFilter(filter);
            }
            int totalRows = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            // Get column information
            String colSql = "SELECT column_name, data_type, nullable FROM all_tab_columns " +
                    "WHERE table_name = UPPER(?) ORDER BY column_id";
            List<Map<String, Object>> allColumns = oracleJdbcTemplate.queryForList(colSql, tableName);

            // Build ORDER BY clause
            String orderBy = "";
            if (sortColumn != null && !sortColumn.isEmpty() && isValidIdentifier(sortColumn)) {
                String dir = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
                orderBy = " ORDER BY \"" + sortColumn + "\" " + dir;
            }

            // Build WHERE clause
            String whereClause = "";
            if (filter != null && !filter.isEmpty()) {
                whereClause = " WHERE " + sanitizeFilter(filter);
            }

            // Calculate offset
            int offset = (page - 1) * pageSize;

            // Get paginated data
            String dataSql = "SELECT * FROM " + tableName + whereClause + orderBy +
                    " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> rows = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            // Format rows for JSON response
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

    // ============================================================
    // 3. PAGINATED PROCEDURE PARAMETERS REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> getProcedureParametersPaginated(String procedureName, String owner,
                                                               int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;

            // Get total count
            String countSql = "SELECT COUNT(*) FROM all_arguments " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND package_name IS NULL AND argument_name IS NOT NULL";

            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class, owner, procedureName);

            // Get paginated parameters
            String paramSql = "SELECT " +
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
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND package_name IS NULL AND argument_name IS NOT NULL " +
                    "ORDER BY position, sequence " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> parameters = oracleJdbcTemplate.queryForList(
                    paramSql, owner, procedureName, offset, pageSize);

            result.put("parameters", parameters);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getProcedureParametersPaginated for {}.{}: {}", owner, procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedure parameters: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 4. PAGINATED FUNCTION PARAMETERS REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> getFunctionParametersPaginated(String functionName, String owner,
                                                              int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;

            // Get total count including return type
            String countSql = "SELECT COUNT(*) FROM all_arguments " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND package_name IS NULL";

            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class, owner, functionName);

            // Get paginated parameters
            String paramSql = "SELECT " +
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
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND package_name IS NULL " +
                    "ORDER BY position, sequence " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> allArgs = oracleJdbcTemplate.queryForList(
                    paramSql, owner, functionName, offset, pageSize);

            // Separate return type (position = 0) from parameters
            List<Map<String, Object>> parameters = new ArrayList<>();
            Map<String, Object> returnType = null;

            for (Map<String, Object> arg : allArgs) {
                Number position = (Number) arg.get("position");
                if (position != null && position.intValue() == 0) {
                    returnType = arg;
                } else {
                    parameters.add(arg);
                }
            }

            result.put("parameters", parameters);
            result.put("returnType", returnType);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getFunctionParametersPaginated for {}.{}: {}", owner, functionName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve function parameters: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 5. PAGINATED PACKAGE ITEMS REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> getPackageItemsPaginated(String packageName, String owner,
                                                        String itemType, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;
            String upperItemType = itemType.toUpperCase();

            // Build query based on item type
            String countSql;
            String dataSql;

            if ("PROCEDURE".equals(upperItemType)) {
                countSql = "SELECT COUNT(DISTINCT procedure_name) FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND NOT EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "                WHERE a2.owner = all_arguments.owner " +
                        "                AND a2.package_name = all_arguments.package_name " +
                        "                AND a2.procedure_name = all_arguments.procedure_name " +
                        "                AND a2.argument_name IS NULL)";

                dataSql = "SELECT DISTINCT procedure_name, " +
                        "(SELECT COUNT(*) FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NOT NULL) as parameter_count " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND NOT EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "                WHERE a2.owner = a.owner " +
                        "                AND a2.package_name = a.package_name " +
                        "                AND a2.procedure_name = a.procedure_name " +
                        "                AND a2.argument_name IS NULL) " +
                        "ORDER BY procedure_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            } else if ("FUNCTION".equals(upperItemType)) {
                countSql = "SELECT COUNT(DISTINCT procedure_name) FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "            WHERE a2.owner = all_arguments.owner " +
                        "            AND a2.package_name = all_arguments.package_name " +
                        "            AND a2.procedure_name = all_arguments.procedure_name " +
                        "            AND a2.argument_name IS NULL)";

                dataSql = "SELECT DISTINCT procedure_name, " +
                        "(SELECT COUNT(*) FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NOT NULL) as parameter_count, " +
                        "(SELECT data_type FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "            WHERE a2.owner = a.owner " +
                        "            AND a2.package_name = a.package_name " +
                        "            AND a2.procedure_name = a.procedure_name " +
                        "            AND a2.argument_name IS NULL) " +
                        "ORDER BY procedure_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            } else {
                // ALL types
                countSql = "SELECT COUNT(DISTINCT procedure_name) FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL";

                dataSql = "SELECT DISTINCT procedure_name, " +
                        "(SELECT COUNT(*) FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NOT NULL) as parameter_count, " +
                        "CASE WHEN EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "                  WHERE a2.owner = a.owner " +
                        "                  AND a2.package_name = a.package_name " +
                        "                  AND a2.procedure_name = a.procedure_name " +
                        "                  AND a2.argument_name IS NULL) " +
                        "     THEN 'FUNCTION' ELSE 'PROCEDURE' END as item_type " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "ORDER BY procedure_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            }

            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class, owner, packageName);
            List<Map<String, Object>> items = oracleJdbcTemplate.queryForList(
                    dataSql, owner, packageName, offset, pageSize);

            result.put("items", items);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getPackageItemsPaginated for {}.{}: {}", owner, packageName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve package items: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 6. PAGINATED TABLE COLUMNS REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> getTableColumnsPaginated(String tableName, String owner,
                                                        int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;

            // Get total count
            String countSql = "SELECT COUNT(*) FROM all_tab_columns " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";

            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class, owner, tableName);

            // Get paginated columns
            String colSql = "SELECT " +
                    "    column_id, " +
                    "    column_name, " +
                    "    data_type, " +
                    "    data_length, " +
                    "    data_precision, " +
                    "    data_scale, " +
                    "    nullable, " +
                    "    data_default, " +
                    "    char_length, " +
                    "    char_used, " +
                    "    virtual_column " +
                    "FROM all_tab_columns " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                    "ORDER BY column_id " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(
                    colSql, owner, tableName, offset, pageSize);

            result.put("columns", columns);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getTableColumnsPaginated for {}.{}: {}", owner, tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve table columns: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 7. PAGINATED SEARCH REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> searchObjectsPaginated(String searchPattern, String type,
                                                      int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            String searchParam = "%" + searchPattern.toUpperCase() + "%";
            int offset = (page - 1) * pageSize;

            String countSql;
            String dataSql;

            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                // Search by specific type
                countSql = "SELECT COUNT(*) FROM all_objects " +
                        "WHERE UPPER(object_name) LIKE ? AND object_type = ?";

                dataSql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time " +
                        "FROM all_objects " +
                        "WHERE UPPER(object_name) LIKE ? AND object_type = ? " +
                        "ORDER BY object_type, object_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                int totalCount = oracleJdbcTemplate.queryForObject(
                        countSql, Integer.class, searchParam, type.toUpperCase());

                List<Map<String, Object>> objects = oracleJdbcTemplate.queryForList(
                        dataSql, searchParam, type.toUpperCase(), offset, pageSize);

                result.put("results", objects);
                result.put("totalCount", totalCount);

            } else {
                // Search all types
                countSql = "SELECT COUNT(*) FROM all_objects " +
                        "WHERE UPPER(object_name) LIKE ?";

                dataSql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time " +
                        "FROM all_objects " +
                        "WHERE UPPER(object_name) LIKE ? " +
                        "ORDER BY object_type, object_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                int totalCount = oracleJdbcTemplate.queryForObject(
                        countSql, Integer.class, searchParam);

                List<Map<String, Object>> objects = oracleJdbcTemplate.queryForList(
                        dataSql, searchParam, offset, pageSize);

                result.put("results", objects);
                result.put("totalCount", totalCount);
            }

            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double)
                    getLongValue(result.get("totalCount")) / pageSize));
            result.put("query", searchPattern);
            result.put("type", type);

            return result;

        } catch (Exception e) {
            log.error("Error in searchObjectsPaginated: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search objects: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 8. GET OBJECT COUNTS ONLY REPOSITORY METHOD
    // ============================================================

    public Map<String, Object> getObjectCountsOnly(String objectName, String objectType, String owner) {
        Map<String, Object> counts = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            String upperType = objectType.toUpperCase();

            // Check if object exists
            String existsSql = "SELECT COUNT(*) FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";

            int exists = oracleJdbcTemplate.queryForObject(existsSql, Integer.class, owner, objectName);
            counts.put("exists", exists > 0);

            if (exists > 0) {
                // Get column count for tables/views
                if ("TABLE".equals(upperType) || "VIEW".equals(upperType)) {
                    String colCountSql = "SELECT COUNT(*) FROM all_tab_columns " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    long totalColumns = oracleJdbcTemplate.queryForObject(
                            colCountSql, Long.class, owner, objectName);
                    counts.put("totalColumns", totalColumns);
                }

                // Get parameter count for procedures/functions/packages
                if ("PROCEDURE".equals(upperType) || "FUNCTION".equals(upperType) || "PACKAGE".equals(upperType)) {
                    String paramCountSql = "SELECT COUNT(*) FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND argument_name IS NOT NULL";
                    long totalParameters = oracleJdbcTemplate.queryForObject(
                            paramCountSql, Long.class, owner, objectName);
                    counts.put("totalParameters", totalParameters);
                }

                // Get dependency count
                String depCountSql = "SELECT COUNT(*) FROM all_dependencies " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?)";
                long dependencies = oracleJdbcTemplate.queryForObject(
                        depCountSql, Long.class, owner, objectName);
                counts.put("dependencies", dependencies);
            }

            counts.put("owner", owner);
            counts.put("objectName", objectName);
            counts.put("objectType", objectType);

            return counts;

        } catch (Exception e) {
            log.error("Error in getObjectCountsOnly for {}.{}: {}", owner, objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve object counts: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 9. INTERNAL HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> getTableColumnsPaginatedInternal(String owner, String tableName,
                                                                       int offset, int pageSize) {
        String sql = "SELECT " +
                "    column_id, " +
                "    column_name, " +
                "    data_type, " +
                "    data_length, " +
                "    data_precision, " +
                "    data_scale, " +
                "    nullable, " +
                "    data_default, " +
                "    char_length, " +
                "    char_used, " +
                "    virtual_column " +
                "FROM all_tab_columns " +
                "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                "ORDER BY column_id " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        return oracleJdbcTemplate.queryForList(sql, owner, tableName, offset, pageSize);
    }

    private List<Map<String, Object>> getArgumentsPaginatedInternal(String owner, String objectName,
                                                                    int offset, int pageSize) {
        String sql = "SELECT " +
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
                "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                "AND argument_name IS NOT NULL " +
                "ORDER BY position, sequence " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        return oracleJdbcTemplate.queryForList(sql, owner, objectName, offset, pageSize);
    }

    private boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) return false;
        // Check for valid Oracle identifier (letters, numbers, _, $, #)
        return identifier.matches("^[a-zA-Z][a-zA-Z0-9_$#]*$");
    }

    private String sanitizeFilter(String filter) {
        if (filter == null || filter.isEmpty()) return "";

        // Basic sanitization - remove dangerous characters
        // This is a simple implementation - in production, use a proper SQL builder
        String sanitized = filter.replaceAll(";", "")
                .replaceAll("--", "")
                .replaceAll("/\\*", "")
                .replaceAll("\\*/", "")
                .replaceAll("exec\\s", "")
                .replaceAll("execute\\s", "")
                .replaceAll("drop\\s", "")
                .replaceAll("insert\\s", "")
                .replaceAll("update\\s", "")
                .replaceAll("delete\\s", "")
                .replaceAll("create\\s", "")
                .replaceAll("alter\\s", "");

        return sanitized;
    }



    // ==================== ENHANCED SYNONYM METHODS ====================

    /**
     * Get all synonyms with their resolved target object types and status
     */
    public List<Map<String, Object>> getAllSynonymsWithDetails() {
        try {
            String sql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'DATABASE_LINK' " +
                    "        ELSE (SELECT object_type FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_type, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                    "        ELSE (SELECT status FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_status, " +
                    "    (SELECT created FROM all_objects " +
                    "     WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) as target_created, " +
                    "    (SELECT last_ddl_time FROM all_objects " +
                    "     WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) as target_modified " +
                    "FROM user_synonyms s " +
                    "ORDER BY s.synonym_name";

            log.debug("Executing query for synonyms with target details");
            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSynonymsWithDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve synonyms with details: " + e.getMessage(), e);
        }
    }

    /**
     * Get synonyms filtered by target object type
     */
    public List<Map<String, Object>> getSynonymsByTargetType(String targetType) {
        try {
            List<Map<String, Object>> allSynonyms = getAllSynonymsWithDetails();

            return allSynonyms.stream()
                    .filter(syn -> targetType.equalsIgnoreCase((String) syn.get("target_type")))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getSynonymsByTargetType: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get all synonyms for frontend with proper type information
     */
    public List<Map<String, Object>> getAllSynonymsForFrontend() {
        try {
            List<Map<String, Object>> synonyms = getAllSynonymsWithDetails();

            return synonyms.stream().map(syn -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "synonym-" + System.currentTimeMillis() + "-" + syn.get("synonym_name"));
                transformed.put("name", syn.get("synonym_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type"));
                transformed.put("targetStatus", syn.get("target_status"));
                transformed.put("targetCreated", syn.get("target_created"));
                transformed.put("targetModified", syn.get("target_modified"));
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);

                // Add additional info based on target type
                if (syn.get("target_type") != null) {
                    String targetType = syn.get("target_type").toString();
                    transformed.put("targetIcon", getObjectTypeIcon(targetType));
                    transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                }

                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllSynonymsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get synonym details with full target object information
     */
    public Map<String, Object> getSynonymDetails(String synonymName) {
        try {
            log.info("Getting details for synonym: {}", synonymName);

            // Get synonym base info
            String sql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    o.object_type as target_type, " +
                    "    o.status as target_status, " +
                    "    o.created as target_created, " +
                    "    o.last_ddl_time as target_modified, " +
                    "    o.temporary as target_temporary, " +
                    "    o.generated as target_generated, " +
                    "    o.secondary as target_secondary " +
                    "FROM user_synonyms s " +
                    "LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) = UPPER(?)";

            Map<String, Object> synonymInfo = oracleJdbcTemplate.queryForMap(sql, synonymName);

            // Enrich with target object details based on type
            String targetType = (String) synonymInfo.get("target_type");
            String targetOwner = (String) synonymInfo.get("target_owner");
            String targetName = (String) synonymInfo.get("target_name");
            String dbLink = (String) synonymInfo.get("db_link");

            if (targetType != null && dbLink == null) {
                Map<String, Object> targetDetails = getObjectDetailsByNameAndType(targetName, targetType, targetOwner);
                synonymInfo.put("targetDetails", targetDetails);
            } else if (dbLink != null) {
                Map<String, Object> remoteInfo = new HashMap<>();
                remoteInfo.put("message", "Remote object via database link: " + dbLink);
                remoteInfo.put("dbLink", dbLink);
                remoteInfo.put("fullPath", targetOwner + "." + targetName + "@" + dbLink);
                synonymInfo.put("targetDetails", remoteInfo);
            }

            return synonymInfo;

        } catch (EmptyResultDataAccessException e) {
            log.warn("Synonym {} not found", synonymName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("synonym_name", synonymName);
            emptyResult.put("message", "Synonym not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getSynonymDetails for {}: {}", synonymName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for synonym " + synonymName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Resolve synonym to its target object
     */
    public Map<String, Object> resolveSynonym(String synonymName) {
        try {
            String sql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    o.object_type as target_type, " +
                    "    o.status as target_status, " +
                    "    CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE 'LOCAL' END as location_type " +
                    "FROM user_synonyms s " +
                    "LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) = UPPER(?)";

            Map<String, Object> resolved = oracleJdbcTemplate.queryForMap(sql, synonymName);

            if (resolved.get("db_link") != null) {
                resolved.put("isRemote", true);
                resolved.put("target_type", "DATABASE_LINK");
            }

            // Add navigation path
            StringBuilder navPath = new StringBuilder();
            if (resolved.get("db_link") != null) {
                navPath.append(resolved.get("target_owner"))
                        .append(".")
                        .append(resolved.get("target_name"))
                        .append("@")
                        .append(resolved.get("db_link"));
            } else {
                navPath.append(resolved.get("target_owner"))
                        .append(".")
                        .append(resolved.get("target_name"));
            }
            resolved.put("navigationPath", navPath.toString());

            return resolved;

        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("synonym_name", synonymName);
            notFound.put("message", "Synonym not found");
            notFound.put("status", "NOT_FOUND");
            return notFound;
        } catch (Exception e) {
            log.error("Error resolving synonym {}: {}", synonymName, e.getMessage());
            throw new RuntimeException("Failed to resolve synonym: " + e.getMessage(), e);
        }
    }

    // ==================== ENHANCED OBJECT RETRIEVAL METHODS ====================

    /**
     * Get details for any object by name and type
     */
    public Map<String, Object> getObjectDetailsByNameAndType(String objectName, String objectType, String owner) {
        Map<String, Object> details = new HashMap<>();
        details.put("objectName", objectName);
        details.put("objectType", objectType);
        details.put("owner", owner);

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
                details.put("owner", owner);
            }

            switch (objectType.toUpperCase()) {
                case "TABLE":
                    Map<String, Object> tableDetails = getTableDetails(owner, objectName);
                    details.putAll(tableDetails);
                    break;
                case "VIEW":
                    details.putAll(getViewDetails(owner, objectName));
                    break;
                case "PROCEDURE":
                    details.putAll(getProcedureDetails(owner, objectName));
                    break;
                case "FUNCTION":
                    details.putAll(getFunctionDetails(owner, objectName));
                    break;
                case "PACKAGE":
                    details.putAll(getPackageDetails(owner, objectName));
                    break;
                case "PACKAGE BODY":
                    details.putAll(getPackageBodyDetails(owner, objectName));
                    break;
                case "SEQUENCE":
                    details.putAll(getSequenceDetails(owner, objectName));
                    break;
                case "SYNONYM":
                    details.putAll(getSynonymDetails(objectName));
                    break;
                case "TRIGGER":
                    details.putAll(getTriggerDetails(owner, objectName));
                    break;
                case "INDEX":
                    details.putAll(getIndexDetails(owner, objectName));
                    break;
                case "TYPE":
                    details.putAll(getTypeDetails(owner, objectName));
                    break;
                case "TYPE BODY":
                    details.putAll(getTypeBodyDetails(owner, objectName));
                    break;
                case "MATERIALIZED VIEW":
                    details.putAll(getMaterializedViewDetails(owner, objectName));
                    break;
                case "DATABASE LINK":
                    details.putAll(getDatabaseLinkDetails(owner, objectName));
                    break;
                case "JAVA CLASS":
                case "JAVA SOURCE":
                case "JAVA RESOURCE":
                    details.putAll(getJavaObjectDetails(owner, objectName, objectType));
                    break;
                default:
                    details.put("message", "Detailed information not available for object type: " + objectType);
                    details.put("basicInfo", getBasicObjectInfo(owner, objectName, objectType));
            }
        } catch (Exception e) {
            log.warn("Could not get details for {} {}: {}", objectType, objectName, e.getMessage());
            details.put("error", e.getMessage());
            details.put("hasError", true);
        }

        return details;
    }

    /**
     * Get basic object information from all_objects
     */
    private Map<String, Object> getBasicObjectInfo(String owner, String objectName, String objectType) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = ?";

            return oracleJdbcTemplate.queryForMap(sql, owner, objectName, objectType);
        } catch (Exception e) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("owner", owner);
            empty.put("object_name", objectName);
            empty.put("object_type", objectType);
            empty.put("status", "UNKNOWN");
            return empty;
        }
    }

    // ==================== OBJECT-SPECIFIC DETAIL METHODS ====================

    /**
     * Get table details with owner
     */
    private Map<String, Object> getTableDetails(String owner, String tableName) {
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

                result = oracleJdbcTemplate.queryForList(sql, tableName);
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

                result = oracleJdbcTemplate.queryForList(sql, owner, tableName);
            }

            if (!result.isEmpty()) {
                details.putAll(result.get(0));

                // Get additional info in separate queries
                try {
                    // Get column count
                    String colCountSql;
                    if (owner.equalsIgnoreCase(getCurrentUser())) {
                        colCountSql = "SELECT COUNT(*) FROM user_tab_columns WHERE UPPER(table_name) = UPPER(?)";
                        Integer columnCount = oracleJdbcTemplate.queryForObject(colCountSql, Integer.class, tableName);
                        details.put("column_count", columnCount);
                    } else {
                        colCountSql = "SELECT COUNT(*) FROM all_tab_columns WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                        Integer columnCount = oracleJdbcTemplate.queryForObject(colCountSql, Integer.class, owner, tableName);
                        details.put("column_count", columnCount);
                    }
                } catch (Exception e) {
                    log.debug("Could not get column count for {}.{}: {}", owner, tableName, e.getMessage());
                }

                // Add column details to the result
                try {
                    List<Map<String, Object>> columns = getTableColumns(owner, tableName);
                    details.put("columns", columns);
                    details.put("column_count", columns.size());
                } catch (Exception e) {
                    log.debug("Could not get columns for {}.{}: {}", owner, tableName, e.getMessage());
                }

                try {
                    // Get size
                    String sizeSql;
                    if (owner.equalsIgnoreCase(getCurrentUser())) {
                        sizeSql = "SELECT SUM(bytes) FROM user_segments WHERE UPPER(segment_name) = UPPER(?) AND segment_type = 'TABLE'";
                        Long sizeBytes = oracleJdbcTemplate.queryForObject(sizeSql, Long.class, tableName);
                        details.put("size_bytes", sizeBytes != null ? sizeBytes : 0);
                    } else {
                        sizeSql = "SELECT SUM(bytes) FROM all_segments WHERE UPPER(owner) = UPPER(?) AND UPPER(segment_name) = UPPER(?) AND segment_type = 'TABLE'";
                        Long sizeBytes = oracleJdbcTemplate.queryForObject(sizeSql, Long.class, owner, tableName);
                        details.put("size_bytes", sizeBytes != null ? sizeBytes : 0);
                    }
                } catch (Exception e) {
                    log.debug("Could not get size for {}.{}: {}", owner, tableName, e.getMessage());
                    details.put("size_bytes", 0);
                }

                try {
                    // Get comments
                    String commentSql;
                    if (owner.equalsIgnoreCase(getCurrentUser())) {
                        commentSql = "SELECT comments FROM user_tab_comments WHERE UPPER(table_name) = UPPER(?)";
                        String comments = oracleJdbcTemplate.queryForObject(commentSql, String.class, tableName);
                        details.put("comments", comments != null ? comments : "");
                    } else {
                        commentSql = "SELECT comments FROM all_tab_comments WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                        String comments = oracleJdbcTemplate.queryForObject(commentSql, String.class, owner, tableName);
                        details.put("comments", comments != null ? comments : "");
                    }
                } catch (Exception e) {
                    log.debug("Could not get comments for {}.{}: {}", owner, tableName, e.getMessage());
                    details.put("comments", "");
                }

                // Get constraints and indexes
                try {
                    List<Map<String, Object>> constraints = getTableConstraints(owner, tableName);
                    details.put("constraints", constraints);
                } catch (Exception e) {
                    log.debug("Could not get constraints for {}.{}: {}", owner, tableName, e.getMessage());
                }

                try {
                    List<Map<String, Object>> indexes = getTableIndexes(owner, tableName);
                    details.put("indexes", indexes);
                } catch (Exception e) {
                    log.debug("Could not get indexes for {}.{}: {}", owner, tableName, e.getMessage());
                }

                try {
                    List<Map<String, Object>> partitions = getTablePartitions(owner, tableName);
                    if (!partitions.isEmpty()) {
                        details.put("partitions", partitions);
                        details.put("partition_count", partitions.size());
                    }
                } catch (Exception e) {
                    log.debug("Could not get partitions for {}.{}: {}", owner, tableName, e.getMessage());
                }

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

    /**
     * Get view details with owner
     */
    private Map<String, Object> getViewDetails(String owner, String viewName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    v.view_name, " +
                        "    v.text_length, " +
                        "    v.text, " +
                        "    v.read_only, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status, " +
                        "    (SELECT COUNT(*) FROM user_tab_columns WHERE table_name = v.view_name) as column_count " +
                        "FROM user_views v " +
                        "JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                        "WHERE UPPER(v.view_name) = UPPER(?)";

                Map<String, Object> viewInfo = oracleJdbcTemplate.queryForMap(sql, viewName);
                details.putAll(viewInfo);

                // Get columns
                List<Map<String, Object>> columns = getViewColumns(owner, viewName);
                details.put("columns", columns);

            } else {
                sql = "SELECT " +
                        "    v.owner, " +
                        "    v.view_name, " +
                        "    v.text_length, " +
                        "    v.text, " +
                        "    v.read_only, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status, " +
                        "    (SELECT COUNT(*) FROM all_tab_columns WHERE owner = v.owner AND table_name = v.view_name) as column_count " +
                        "FROM all_views v " +
                        "JOIN all_objects o ON v.owner = o.owner AND v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                        "WHERE UPPER(v.owner) = UPPER(?) AND UPPER(v.view_name) = UPPER(?)";

                Map<String, Object> viewInfo = oracleJdbcTemplate.queryForMap(sql, owner, viewName);
                details.putAll(viewInfo);

                // Get columns
                List<Map<String, Object>> columns = getViewColumns(owner, viewName);
                details.put("columns", columns);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("View {}.{} not found", owner, viewName);
            details.put("error", "View not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting view details for {}.{}: {}", owner, viewName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get procedure details with owner
     */
    private Map<String, Object> getProcedureDetails(String owner, String procedureName) {
        Map<String, Object> details = new HashMap<>();

        try {
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                String sql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'PROCEDURE'";

                Map<String, Object> procInfo = oracleJdbcTemplate.queryForMap(sql, procedureName);
                details.putAll(procInfo);

                // Get parameters
                String paramSql = "SELECT " +
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

                List<Map<String, Object>> params = oracleJdbcTemplate.queryForList(paramSql, procedureName);
                details.put("parameters", params);
                details.put("parameterCount", params.size());

                // Get source code if available
                String sourceSql = "SELECT text FROM user_source " +
                        "WHERE UPPER(name) = UPPER(?) AND type = 'PROCEDURE' " +
                        "ORDER BY line";

                List<String> sourceLines = oracleJdbcTemplate.queryForList(sourceSql, String.class, procedureName);
                if (!sourceLines.isEmpty()) {
                    details.put("source", String.join("", sourceLines));
                }

            } else {
                String sql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM all_objects " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'PROCEDURE'";

                Map<String, Object> procInfo = oracleJdbcTemplate.queryForMap(sql, owner, procedureName);
                details.putAll(procInfo);

                // Get parameters
                String paramSql = "SELECT " +
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
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL AND argument_name IS NOT NULL " +
                        "ORDER BY position, sequence";

                List<Map<String, Object>> params = oracleJdbcTemplate.queryForList(paramSql, owner, procedureName);
                details.put("parameters", params);
                details.put("parameterCount", params.size());
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Procedure {}.{} not found", owner, procedureName);
            details.put("error", "Procedure not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting procedure details for {}.{}: {}", owner, procedureName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get function details with owner
     */
    private Map<String, Object> getFunctionDetails(String owner, String functionName) {
        Map<String, Object> details = new HashMap<>();

        try {
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                String sql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'FUNCTION'";

                Map<String, Object> funcInfo = oracleJdbcTemplate.queryForMap(sql, functionName);
                details.putAll(funcInfo);

                // Get parameters and return type
                String paramSql = "SELECT " +
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

                List<Map<String, Object>> allArgs = oracleJdbcTemplate.queryForList(paramSql, functionName);

                // Separate return type (argument_name IS NULL) from parameters
                List<Map<String, Object>> params = allArgs.stream()
                        .filter(arg -> arg.get("argument_name") != null)
                        .collect(Collectors.toList());

                Map<String, Object> returnType = allArgs.stream()
                        .filter(arg -> arg.get("argument_name") == null)
                        .findFirst()
                        .orElse(new HashMap<>());

                details.put("parameters", params);
                details.put("parameterCount", params.size());
                details.put("returnType", returnType);

                // Get source code
                String sourceSql = "SELECT text FROM user_source " +
                        "WHERE UPPER(name) = UPPER(?) AND type = 'FUNCTION' " +
                        "ORDER BY line";

                List<String> sourceLines = oracleJdbcTemplate.queryForList(sourceSql, String.class, functionName);
                if (!sourceLines.isEmpty()) {
                    details.put("source", String.join("", sourceLines));
                }

            } else {
                String sql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM all_objects " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'FUNCTION'";

                Map<String, Object> funcInfo = oracleJdbcTemplate.queryForMap(sql, owner, functionName);
                details.putAll(funcInfo);

                // Get parameters and return type
                String paramSql = "SELECT " +
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
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL " +
                        "ORDER BY position, sequence";

                List<Map<String, Object>> allArgs = oracleJdbcTemplate.queryForList(paramSql, owner, functionName);

                List<Map<String, Object>> params = allArgs.stream()
                        .filter(arg -> arg.get("argument_name") != null)
                        .collect(Collectors.toList());

                Map<String, Object> returnType = allArgs.stream()
                        .filter(arg -> arg.get("argument_name") == null)
                        .findFirst()
                        .orElse(new HashMap<>());

                details.put("parameters", params);
                details.put("parameterCount", params.size());
                details.put("returnType", returnType);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Function {}.{} not found", owner, functionName);
            details.put("error", "Function not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting function details for {}.{}: {}", owner, functionName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get package details with owner
     */
    private Map<String, Object> getPackageDetails(String owner, String packageName) {
        Map<String, Object> details = new HashMap<>();

        try {
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                // Get package spec
                String specSql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE'";

                try {
                    Map<String, Object> spec = oracleJdbcTemplate.queryForMap(specSql, packageName);
                    details.put("specification", spec);

                    // Get package spec source
                    String sourceSql = "SELECT text FROM user_source " +
                            "WHERE UPPER(name) = UPPER(?) AND type = 'PACKAGE' " +
                            "ORDER BY line";

                    List<String> sourceLines = oracleJdbcTemplate.queryForList(sourceSql, String.class, packageName);
                    if (!sourceLines.isEmpty()) {
                        details.put("specSource", String.join("", sourceLines));
                    }

                } catch (EmptyResultDataAccessException e) {
                    details.put("specification", null);
                }

                // Get package body
                String bodySql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE BODY'";

                try {
                    Map<String, Object> body = oracleJdbcTemplate.queryForMap(bodySql, packageName);
                    details.put("body", body);

                    // Get package body source
                    String bodySourceSql = "SELECT text FROM user_source " +
                            "WHERE UPPER(name) = UPPER(?) AND type = 'PACKAGE BODY' " +
                            "ORDER BY line";

                    List<String> bodySourceLines = oracleJdbcTemplate.queryForList(bodySourceSql, String.class, packageName);
                    if (!bodySourceLines.isEmpty()) {
                        details.put("bodySource", String.join("", bodySourceLines));
                    }

                } catch (EmptyResultDataAccessException e) {
                    details.put("body", null);
                }

                // Get package procedures and functions
                String procSql = "SELECT DISTINCT " +
                        "    procedure_name, " +
                        "    overload, " +
                        "    (SELECT COUNT(*) FROM user_arguments a " +
                        "     WHERE a.package_name = ? AND a.object_name = ? " +
                        "     AND a.procedure_name = p.procedure_name " +
                        "     AND a.argument_name IS NOT NULL) as parameter_count, " +
                        "    (SELECT data_type FROM user_arguments a " +
                        "     WHERE a.package_name = ? AND a.object_name = ? " +
                        "     AND a.procedure_name = p.procedure_name " +
                        "     AND a.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                        "FROM user_arguments p " +
                        "WHERE UPPER(p.package_name) = UPPER(?) " +
                        "AND p.object_name = p.package_name " +
                        "AND p.procedure_name IS NOT NULL " +
                        "ORDER BY p.procedure_name";

                List<Map<String, Object>> procedures = oracleJdbcTemplate.queryForList(
                        procSql, packageName, packageName, packageName, packageName, packageName);

                // Separate into procedures and functions based on return_type
                List<Map<String, Object>> packageProcedures = procedures.stream()
                        .filter(p -> p.get("return_type") == null)
                        .collect(Collectors.toList());

                List<Map<String, Object>> packageFunctions = procedures.stream()
                        .filter(p -> p.get("return_type") != null)
                        .collect(Collectors.toList());

                details.put("procedures", packageProcedures);
                details.put("functions", packageFunctions);
                details.put("procedureCount", packageProcedures.size());
                details.put("functionCount", packageFunctions.size());

                // Get package variables (limited info)
                String varSql = "SELECT DISTINCT " +
                        "    procedure_name as variable_name " +
                        "FROM user_identifiers " +
                        "WHERE UPPER(name) = UPPER(?) AND object_type = 'PACKAGE' " +
                        "AND usage = 'DECLARATION' AND type = 'VARIABLE'";

                List<Map<String, Object>> variables = oracleJdbcTemplate.queryForList(varSql, packageName);
                details.put("variables", variables);
                details.put("variableCount", variables.size());

            } else {
                // Similar for all_* views with owner
                details.putAll(getPackageDetailsFromAllViews(owner, packageName));
            }

        } catch (Exception e) {
            log.warn("Error getting package details for {}.{}: {}", owner, packageName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get package body details
     */
    private Map<String, Object> getPackageBodyDetails(String owner, String packageName) {
        Map<String, Object> details = new HashMap<>();

        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE BODY'";

                Map<String, Object> bodyInfo = oracleJdbcTemplate.queryForMap(sql, packageName);
                details.putAll(bodyInfo);

                // Get source
                String sourceSql = "SELECT text FROM user_source " +
                        "WHERE UPPER(name) = UPPER(?) AND type = 'PACKAGE BODY' " +
                        "ORDER BY line";

                List<String> sourceLines = oracleJdbcTemplate.queryForList(sourceSql, String.class, packageName);
                if (!sourceLines.isEmpty()) {
                    details.put("source", String.join("", sourceLines));
                }

            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM all_objects " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE BODY'";

                Map<String, Object> bodyInfo = oracleJdbcTemplate.queryForMap(sql, owner, packageName);
                details.putAll(bodyInfo);
            }

        } catch (Exception e) {
            log.warn("Error getting package body details for {}.{}: {}", owner, packageName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get package details from all_views (for other schemas)
     */
    private Map<String, Object> getPackageDetailsFromAllViews(String owner, String packageName) {
        Map<String, Object> details = new HashMap<>();

        try {
            // Get package spec
            String specSql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    status, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE'";

            try {
                Map<String, Object> spec = oracleJdbcTemplate.queryForMap(specSql, owner, packageName);
                details.put("specification", spec);
            } catch (EmptyResultDataAccessException e) {
                details.put("specification", null);
            }

            // Get package body
            String bodySql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    status, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE BODY'";

            try {
                Map<String, Object> body = oracleJdbcTemplate.queryForMap(bodySql, owner, packageName);
                details.put("body", body);
            } catch (EmptyResultDataAccessException e) {
                details.put("body", null);
            }

            // Get procedures and functions from arguments
            String procSql = "SELECT DISTINCT " +
                    "    procedure_name, " +
                    "    overload, " +
                    "    (SELECT COUNT(*) FROM all_arguments a " +
                    "     WHERE a.owner = ? AND a.package_name = ? AND a.object_name = ? " +
                    "     AND a.procedure_name = p.procedure_name " +
                    "     AND a.argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM all_arguments a " +
                    "     WHERE a.owner = ? AND a.package_name = ? AND a.object_name = ? " +
                    "     AND a.procedure_name = p.procedure_name " +
                    "     AND a.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM all_arguments p " +
                    "WHERE UPPER(p.owner) = UPPER(?) AND UPPER(p.package_name) = UPPER(?) " +
                    "AND p.object_name = p.package_name " +
                    "AND p.procedure_name IS NOT NULL " +
                    "ORDER BY p.procedure_name";

            List<Map<String, Object>> procedures = oracleJdbcTemplate.queryForList(
                    procSql, owner, packageName, packageName, owner, packageName, packageName, owner, packageName);

            List<Map<String, Object>> packageProcedures = procedures.stream()
                    .filter(p -> p.get("return_type") == null)
                    .collect(Collectors.toList());

            List<Map<String, Object>> packageFunctions = procedures.stream()
                    .filter(p -> p.get("return_type") != null)
                    .collect(Collectors.toList());

            details.put("procedures", packageProcedures);
            details.put("functions", packageFunctions);
            details.put("procedureCount", packageProcedures.size());
            details.put("functionCount", packageFunctions.size());

        } catch (Exception e) {
            log.warn("Error getting package details from all_views for {}.{}: {}", owner, packageName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get sequence details with owner
     */
    private Map<String, Object> getSequenceDetails(String owner, String sequenceName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    sequence_name, " +
                        "    min_value, " +
                        "    max_value, " +
                        "    increment_by, " +
                        "    cycle_flag, " +
                        "    order_flag, " +
                        "    cache_size, " +
                        "    last_number, " +
                        "    (CASE WHEN cycle_flag = 'Y' THEN 'YES' ELSE 'NO' END) as cycles, " +
                        "    (CASE WHEN order_flag = 'Y' THEN 'YES' ELSE 'NO' END) as orders " +
                        "FROM user_sequences " +
                        "WHERE UPPER(sequence_name) = UPPER(?)";

                Map<String, Object> seqInfo = oracleJdbcTemplate.queryForMap(sql, sequenceName);
                details.putAll(seqInfo);

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
                        "WHERE UPPER(sequence_owner) = UPPER(?) AND UPPER(sequence_name) = UPPER(?)";

                Map<String, Object> seqInfo = oracleJdbcTemplate.queryForMap(sql, owner, sequenceName);
                details.putAll(seqInfo);
            }

            // Get current value if possible
            try {
                String currValSql = "SELECT " + (owner.equalsIgnoreCase(getCurrentUser()) ? "" : owner + ".") +
                        sequenceName + ".CURRVAL FROM DUAL";
                Long currVal = oracleJdbcTemplate.queryForObject(currValSql, Long.class);
                details.put("current_value", currVal);
            } catch (Exception e) {
                // CURRVAL may not be accessible or not yet selected in this session
                details.put("current_value", "Not available in current session");
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Sequence {}.{} not found", owner, sequenceName);
            details.put("error", "Sequence not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting sequence details for {}.{}: {}", owner, sequenceName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get trigger details with owner
     */
    private Map<String, Object> getTriggerDetails(String owner, String triggerName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    t.trigger_name, " +
                        "    t.trigger_type, " +
                        "    t.triggering_event, " +
                        "    t.table_name, " +
                        "    t.referencing_names, " +
                        "    t.when_clause, " +
                        "    t.status as trigger_status, " +
                        "    t.description, " +
                        "    t.trigger_body, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status, " +
                        "    o.temporary, " +
                        "    o.generated, " +
                        "    o.secondary " +
                        "FROM user_triggers t " +
                        "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                        "WHERE UPPER(t.trigger_name) = UPPER(?)";

                Map<String, Object> triggerInfo = oracleJdbcTemplate.queryForMap(sql, triggerName);
                details.putAll(triggerInfo);

                // Get columns referenced if any
                String colSql = "SELECT column_name FROM user_trigger_cols " +
                        "WHERE UPPER(trigger_name) = UPPER(?) ORDER BY column_name";

                List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(colSql, triggerName);
                if (!columns.isEmpty()) {
                    details.put("referenced_columns", columns);
                }

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
                        "    t.status as trigger_status, " +
                        "    t.description, " +
                        "    t.trigger_body, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status, " +
                        "    o.temporary, " +
                        "    o.generated, " +
                        "    o.secondary " +
                        "FROM all_triggers t " +
                        "JOIN all_objects o ON t.owner = o.owner AND t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                        "WHERE UPPER(t.owner) = UPPER(?) AND UPPER(t.trigger_name) = UPPER(?)";

                Map<String, Object> triggerInfo = oracleJdbcTemplate.queryForMap(sql, owner, triggerName);
                details.putAll(triggerInfo);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Trigger {}.{} not found", owner, triggerName);
            details.put("error", "Trigger not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting trigger details for {}.{}: {}", owner, triggerName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get index details with owner
     */
    private Map<String, Object> getIndexDetails(String owner, String indexName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    i.index_name, " +
                        "    i.table_name, " +
                        "    i.tablespace_name, " +
                        "    i.uniqueness, " +
                        "    i.index_type, " +
                        "    i.status as index_status, " +
                        "    i.visibility, " +
                        "    i.distinct_keys, " +
                        "    i.leaf_blocks, " +
                        "    i.clustering_factor, " +
                        "    i.partitioned, " +
                        "    i.temporary, " +
                        "    i.dropped, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status " +
                        "FROM user_indexes i " +
                        "JOIN user_objects o ON i.index_name = o.object_name AND o.object_type = 'INDEX' " +
                        "WHERE UPPER(i.index_name) = UPPER(?)";

                Map<String, Object> indexInfo = oracleJdbcTemplate.queryForMap(sql, indexName);
                details.putAll(indexInfo);

                // Get indexed columns
                String colSql = "SELECT column_name, column_position FROM user_ind_columns " +
                        "WHERE UPPER(index_name) = UPPER(?) ORDER BY column_position";

                List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(colSql, indexName);
                details.put("columns", columns);

                // Get size
                String sizeSql = "SELECT bytes FROM user_segments " +
                        "WHERE UPPER(segment_name) = UPPER(?) AND segment_type = 'INDEX'";

                try {
                    Long bytes = oracleJdbcTemplate.queryForObject(sizeSql, Long.class, indexName);
                    details.put("size_bytes", bytes);
                    details.put("size_display", formatBytes(bytes));
                } catch (Exception e) {
                    details.put("size_bytes", 0);
                }

            } else {
                sql = "SELECT " +
                        "    i.owner, " +
                        "    i.index_name, " +
                        "    i.table_owner, " +
                        "    i.table_name, " +
                        "    i.tablespace_name, " +
                        "    i.uniqueness, " +
                        "    i.index_type, " +
                        "    i.status as index_status, " +
                        "    i.visibility, " +
                        "    i.distinct_keys, " +
                        "    i.leaf_blocks, " +
                        "    i.clustering_factor, " +
                        "    i.partitioned, " +
                        "    i.temporary, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status " +
                        "FROM all_indexes i " +
                        "JOIN all_objects o ON i.owner = o.owner AND i.index_name = o.object_name AND o.object_type = 'INDEX' " +
                        "WHERE UPPER(i.owner) = UPPER(?) AND UPPER(i.index_name) = UPPER(?)";

                Map<String, Object> indexInfo = oracleJdbcTemplate.queryForMap(sql, owner, indexName);
                details.putAll(indexInfo);

                // Get indexed columns
                String colSql = "SELECT column_name, column_position FROM all_ind_columns " +
                        "WHERE UPPER(index_owner) = UPPER(?) AND UPPER(index_name) = UPPER(?) ORDER BY column_position";

                List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(colSql, owner, indexName);
                details.put("columns", columns);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Index {}.{} not found", owner, indexName);
            details.put("error", "Index not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting index details for {}.{}: {}", owner, indexName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get type details with owner
     */
    private Map<String, Object> getTypeDetails(String owner, String typeName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    t.type_name, " +
                        "    t.typecode, " +
                        "    t.attributes, " +
                        "    t.methods, " +
                        "    t.final, " +
                        "    t.instantiable, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status, " +
                        "    o.temporary, " +
                        "    o.generated " +
                        "FROM user_types t " +
                        "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                        "WHERE UPPER(t.type_name) = UPPER(?)";

                Map<String, Object> typeInfo = oracleJdbcTemplate.queryForMap(sql, typeName);
                details.putAll(typeInfo);

                // Get attributes
                String attrSql = "SELECT " +
                        "    attr_name, " +
                        "    attr_type_name, " +
                        "    length, " +
                        "    precision, " +
                        "    scale, " +
                        "    character_set_name " +
                        "FROM user_type_attrs " +
                        "WHERE UPPER(type_name) = UPPER(?) " +
                        "ORDER BY attr_no";

                List<Map<String, Object>> attributes = oracleJdbcTemplate.queryForList(attrSql, typeName);
                details.put("attributes", attributes);

                // Get methods
                String methodSql = "SELECT " +
                        "    method_name, " +
                        "    method_no, " +
                        "    method_type, " +
                        "    parameters, " +
                        "    results " +
                        "FROM user_type_methods " +
                        "WHERE UPPER(type_name) = UPPER(?) " +
                        "ORDER BY method_no";

                List<Map<String, Object>> methods = oracleJdbcTemplate.queryForList(methodSql, typeName);
                details.put("methods", methods);

            } else {
                sql = "SELECT " +
                        "    t.owner, " +
                        "    t.type_name, " +
                        "    t.typecode, " +
                        "    t.attributes, " +
                        "    t.methods, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status, " +
                        "    o.temporary, " +
                        "    o.generated " +
                        "FROM all_types t " +
                        "JOIN all_objects o ON t.owner = o.owner AND t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                        "WHERE UPPER(t.owner) = UPPER(?) AND UPPER(t.type_name) = UPPER(?)";

                Map<String, Object> typeInfo = oracleJdbcTemplate.queryForMap(sql, owner, typeName);
                details.putAll(typeInfo);

                // Try to get attributes if accessible
                try {
                    String attrSql = "SELECT " +
                            "    attr_name, " +
                            "    attr_type_name, " +
                            "    length, " +
                            "    precision, " +
                            "    scale " +
                            "FROM all_type_attrs " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(type_name) = UPPER(?) " +
                            "ORDER BY attr_no";

                    List<Map<String, Object>> attributes = oracleJdbcTemplate.queryForList(attrSql, owner, typeName);
                    details.put("attributes", attributes);
                } catch (Exception e) {
                    log.debug("Could not get attributes for type {}.{}", owner, typeName);
                }
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Type {}.{} not found", owner, typeName);
            details.put("error", "Type not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting type details for {}.{}: {}", owner, typeName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get type body details
     */
    private Map<String, Object> getTypeBodyDetails(String owner, String typeName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'TYPE BODY'";

                Map<String, Object> bodyInfo = oracleJdbcTemplate.queryForMap(sql, typeName);
                details.putAll(bodyInfo);

                // Get source
                String sourceSql = "SELECT text FROM user_source " +
                        "WHERE UPPER(name) = UPPER(?) AND type = 'TYPE BODY' " +
                        "ORDER BY line";

                List<String> sourceLines = oracleJdbcTemplate.queryForList(sourceSql, String.class, typeName);
                if (!sourceLines.isEmpty()) {
                    details.put("source", String.join("", sourceLines));
                }

            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM all_objects " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'TYPE BODY'";

                Map<String, Object> bodyInfo = oracleJdbcTemplate.queryForMap(sql, owner, typeName);
                details.putAll(bodyInfo);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Type body {}.{} not found", owner, typeName);
            details.put("error", "Type body not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting type body details for {}.{}: {}", owner, typeName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get materialized view details
     */
    private Map<String, Object> getMaterializedViewDetails(String owner, String mvName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    mview_name, " +
                        "    container_name, " +
                        "    query, " +
                        "    refresh_method, " +
                        "    refresh_mode, " +
                        "    build_mode, " +
                        "    fast_refreshable, " +
                        "    last_refresh_type, " +
                        "    last_refresh_date, " +
                        "    staleness, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status " +
                        "FROM user_mviews m " +
                        "JOIN user_objects o ON m.mview_name = o.object_name AND o.object_type = 'MATERIALIZED VIEW' " +
                        "WHERE UPPER(m.mview_name) = UPPER(?)";

                Map<String, Object> mvInfo = oracleJdbcTemplate.queryForMap(sql, mvName);
                details.putAll(mvInfo);

                // Get columns
                String colSql = "SELECT column_name, data_type, nullable " +
                        "FROM user_tab_columns WHERE table_name = ? ORDER BY column_id";

                List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(colSql, mvName);
                details.put("columns", columns);

            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    mview_name, " +
                        "    container_name, " +
                        "    refresh_method, " +
                        "    refresh_mode, " +
                        "    build_mode, " +
                        "    fast_refreshable, " +
                        "    last_refresh_type, " +
                        "    last_refresh_date, " +
                        "    staleness, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status " +
                        "FROM all_mviews m " +
                        "JOIN all_objects o ON m.owner = o.owner AND m.mview_name = o.object_name AND o.object_type = 'MATERIALIZED VIEW' " +
                        "WHERE UPPER(m.owner) = UPPER(?) AND UPPER(m.mview_name) = UPPER(?)";

                Map<String, Object> mvInfo = oracleJdbcTemplate.queryForMap(sql, owner, mvName);
                details.putAll(mvInfo);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Materialized view {}.{} not found", owner, mvName);
            details.put("error", "Materialized view not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting materialized view details for {}.{}: {}", owner, mvName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get database link details
     */
    private Map<String, Object> getDatabaseLinkDetails(String owner, String dbLinkName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    db_link, " +
                        "    username, " +
                        "    host, " +
                        "    created " +
                        "FROM user_db_links " +
                        "WHERE UPPER(db_link) = UPPER(?)";

                Map<String, Object> linkInfo = oracleJdbcTemplate.queryForMap(sql, dbLinkName);
                details.putAll(linkInfo);

            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    db_link, " +
                        "    username, " +
                        "    host, " +
                        "    created " +
                        "FROM all_db_links " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(db_link) = UPPER(?)";

                Map<String, Object> linkInfo = oracleJdbcTemplate.queryForMap(sql, owner, dbLinkName);
                details.putAll(linkInfo);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Database link {}.{} not found", owner, dbLinkName);
            details.put("error", "Database link not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting database link details for {}.{}: {}", owner, dbLinkName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    /**
     * Get Java object details
     */
    private Map<String, Object> getJavaObjectDetails(String owner, String objectName, String objectType) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    status, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = ?";

            Map<String, Object> objInfo = oracleJdbcTemplate.queryForMap(sql, owner, objectName, objectType);
            details.putAll(objInfo);

            // Try to get source for Java source
            if ("JAVA SOURCE".equals(objectType) && owner.equalsIgnoreCase(getCurrentUser())) {
                String sourceSql = "SELECT source FROM user_java_classes WHERE UPPER(name) = UPPER(?)";
                try {
                    String source = oracleJdbcTemplate.queryForObject(sourceSql, String.class, objectName);
                    details.put("source", source);
                } catch (Exception e) {
                    log.debug("Could not get source for Java object: {}", objectName);
                }
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Java object {}.{} not found", owner, objectName);
            details.put("error", "Java object not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting Java object details for {}.{}: {}", owner, objectName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    // ==================== COLUMN, CONSTRAINT, INDEX RETRIEVAL METHODS ====================

    /**
     * Get table columns with owner
     */
    private List<Map<String, Object>> getTableColumns(String owner, String tableName) {
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
                        "    s.last_analyzed as stats_last_analyzed, " +
                        "    c.char_col_decl_length, " +
                        "    c.global_stats, " +
                        "    c.user_stats, " +
                        "    c.avg_col_len, " +
                        "    c.char_length, " +
                        "    c.data_type_owner " +
                        "FROM user_tab_columns c " +
                        "LEFT JOIN user_tab_col_statistics s " +
                        "    ON c.table_name = s.table_name AND c.column_name = s.column_name " +
                        "WHERE UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.column_id";

                return oracleJdbcTemplate.queryForList(sql, tableName);
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
                        "    s.last_analyzed as stats_last_analyzed, " +
                        "    c.char_length " +
                        "FROM all_tab_columns c " +
                        "LEFT JOIN all_tab_col_statistics s " +
                        "    ON c.owner = s.owner AND c.table_name = s.table_name AND c.column_name = s.column_name " +
                        "WHERE UPPER(c.owner) = UPPER(?) AND UPPER(c.table_name) = UPPER(?) " +
                        "ORDER BY c.column_id";

                return oracleJdbcTemplate.queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            log.warn("Error getting columns for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get table constraints with owner
     */
    private List<Map<String, Object>> getTableConstraints(String owner, String tableName) {
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

                return oracleJdbcTemplate.queryForList(sql, tableName);
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

                return oracleJdbcTemplate.queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            log.warn("Error getting constraints for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get table indexes with owner
     */
    private List<Map<String, Object>> getTableIndexes(String owner, String tableName) {
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

                return oracleJdbcTemplate.queryForList(sql, tableName);
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

                return oracleJdbcTemplate.queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            log.warn("Error getting indexes for {}.{}: {}", owner, tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get view columns with owner
     */
    private List<Map<String, Object>> getViewColumns(String owner, String viewName) {
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    column_id, " +
                        "    column_name, " +
                        "    data_type, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    nullable, " +
                        "    char_length, " +
                        "    char_used " +
                        "FROM user_tab_columns " +
                        "WHERE UPPER(table_name) = UPPER(?) " +
                        "ORDER BY column_id";

                return oracleJdbcTemplate.queryForList(sql, viewName);
            } else {
                sql = "SELECT " +
                        "    column_id, " +
                        "    column_name, " +
                        "    data_type, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    nullable, " +
                        "    char_length " +
                        "FROM all_tab_columns " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                        "ORDER BY column_id";

                return oracleJdbcTemplate.queryForList(sql, owner, viewName);
            }
        } catch (Exception e) {
            log.warn("Error getting view columns for {}.{}: {}", owner, viewName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get table partitions
     */
    private List<Map<String, Object>> getTablePartitions(String owner, String tableName) {
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

                return oracleJdbcTemplate.queryForList(sql, tableName);
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

                return oracleJdbcTemplate.queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            log.debug("No partitions found for {}.{}", owner, tableName);
            return new ArrayList<>();
        }
    }

    // ==================== ENHANCED DDL METHODS ====================

    /**
     * Get object DDL for frontend with enhanced multiple fallback methods
     */
    public Map<String, Object> getObjectDDLForFrontend(String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();
        result.put("objectName", objectName);
        result.put("objectType", objectType);

        long startTime = System.currentTimeMillis();

        try {
            // Try multiple methods in sequence until we get DDL
            String ddl = null;
            String methodUsed = null;

            // Method 1: Try with DBMS_METADATA (standard)
            ddl = getObjectDDLWithMetadata(objectName, objectType);
            if (ddl != null && !ddl.isEmpty()) {
                methodUsed = "DBMS_METADATA";
            }

            // Method 2: Try with DBMS_METADATA and explicit transform
            if (ddl == null) {
                ddl = getObjectDDLWithTransform(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "DBMS_METADATA with TRANSFORM";
                }
            }

            // Method 3: Try to get from USER_SOURCE for procedures/functions/packages
            if (ddl == null && isSourceBasedObject(objectType)) {
                ddl = getDDLFromSource(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "USER_SOURCE";
                }
            }

            // Method 4: Try with ALL_SOURCE if not in current schema
            if (ddl == null) {
                ddl = getDDLFromAllSource(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "ALL_SOURCE";
                }
            }

            // Method 5: Try with DBMS_METADATA using different owner resolution
            if (ddl == null) {
                ddl = getObjectDDLWithOwnerResolution(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "DBMS_METADATA with owner resolution";
                }
            }

            // Method 6: For procedures, try to get from DBA_SOURCE if available
            if (ddl == null) {
                ddl = getDDLFromDBASource(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "DBA_SOURCE";
                }
            }

            // Method 7: Last resort - try to generate from data dictionary
            if (ddl == null) {
                if (objectType.equalsIgnoreCase("PROCEDURE")) {
                    ddl = generateProcedureDDL(objectName);
                } else if (objectType.equalsIgnoreCase("FUNCTION")) {
                    ddl = generateFunctionDDL(objectName);
                } else if (objectType.equalsIgnoreCase("TABLE")) {
                    ddl = generateTableDDL(objectName);
                } else if (objectType.equalsIgnoreCase("VIEW")) {
                    ddl = generateViewDDL(objectName);
                }

                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "GENERATED";
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;
            result.put("executionTimeMs", executionTime);

            if (ddl != null && !ddl.isEmpty()) {
                result.put("ddl", ddl);
                result.put("status", "SUCCESS");
                result.put("method", methodUsed);
                result.put("message", "DDL retrieved successfully using " + methodUsed);
            } else {
                // Provide detailed diagnostic information
                result.put("ddl", generateDetailedErrorMessage(objectName, objectType));
                result.put("status", "NOT_AVAILABLE");
                result.put("message", "Could not retrieve DDL after trying multiple methods");

                // Add diagnostic info
                result.put("diagnostics", getObjectDiagnostics(objectName, objectType));
            }

        } catch (Exception e) {
            log.error("Error in getObjectDDLForFrontend for {} {}: {}",
                    objectType, objectName, e.getMessage(), e);

            result.put("ddl", "-- Error retrieving DDL: " + e.getMessage() + "\n" +
                    "-- Please check logs for more details");
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("stacktrace", e.toString());
        }

        return result;
    }

    /**
     * Check if object type can be retrieved from source views
     */
    private boolean isSourceBasedObject(String objectType) {
        if (objectType == null) return false;
        String upperType = objectType.toUpperCase();
        return upperType.equals("PROCEDURE") ||
                upperType.equals("FUNCTION") ||
                upperType.equals("PACKAGE") ||
                upperType.equals("PACKAGE BODY") ||
                upperType.equals("TYPE") ||
                upperType.equals("TYPE BODY") ||
                upperType.equals("TRIGGER") ||
                upperType.equals("JAVA SOURCE");
    }

    /**
     * Method 1: Standard DBMS_METADATA approach
     */
    private String getObjectDDLWithMetadata(String objectName, String objectType) {
        try {
            log.info("Attempting to get DDL for {} {} using DBMS_METADATA", objectType, objectName);

            String metadataType = convertToMetadataObjectType(objectType);
            String currentUser = getCurrentUser();

            // First try with current user as owner
            String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) FROM DUAL";

            try {
                return oracleJdbcTemplate.queryForObject(
                        sql,
                        String.class,
                        metadataType,
                        objectName.toUpperCase(),
                        currentUser
                );
            } catch (Exception e) {
                log.debug("DBMS_METADATA failed with current user: {}", e.getMessage());

                // Try to find the actual owner
                Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
                String owner = (String) objectLocation.get("owner");

                if (owner != null && !owner.equals(currentUser)) {
                    return oracleJdbcTemplate.queryForObject(
                            sql,
                            String.class,
                            metadataType,
                            objectName.toUpperCase(),
                            owner
                    );
                }
                return null;
            }
        } catch (Exception e) {
            log.debug("getObjectDDLWithMetadata failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Method 2: DBMS_METADATA with TRANSFORM parameter
     */
    private String getObjectDDLWithTransform(String objectName, String objectType) {
        try {
            log.info("Attempting to get DDL for {} {} with TRANSFORM", objectType, objectName);

            String metadataType = convertToMetadataObjectType(objectType);
            String currentUser = getCurrentUser();

            String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?, " +
                    "DBMS_METADATA.SESSION_TRANSFORM('SQLTERMINATOR', TRUE) || " +
                    "DBMS_METADATA.SESSION_TRANSFORM('PRETTY', TRUE)) FROM DUAL";

            try {
                return oracleJdbcTemplate.queryForObject(
                        sql,
                        String.class,
                        metadataType,
                        objectName.toUpperCase(),
                        currentUser
                );
            } catch (Exception e) {
                Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
                String owner = (String) objectLocation.get("owner");

                if (owner != null && !owner.equals(currentUser)) {
                    return oracleJdbcTemplate.queryForObject(
                            sql,
                            String.class,
                            metadataType,
                            objectName.toUpperCase(),
                            owner
                    );
                }
                return null;
            }
        } catch (Exception e) {
            log.debug("getObjectDDLWithTransform failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Method 3: Get DDL from USER_SOURCE
     */
    private String getDDLFromSource(String objectName, String objectType) {
        try {
            log.info("Attempting to get DDL for {} {} from USER_SOURCE", objectType, objectName);

            String sourceType = objectType.toUpperCase();
            if (sourceType.equals("PACKAGE BODY")) {
                sourceType = "PACKAGE BODY";
            } else if (sourceType.equals("TYPE BODY")) {
                sourceType = "TYPE BODY";
            }

            String sql = "SELECT text FROM user_source " +
                    "WHERE UPPER(name) = UPPER(?) AND UPPER(type) = UPPER(?) " +
                    "ORDER BY line";

            List<String> sourceLines = oracleJdbcTemplate.queryForList(
                    sql,
                    String.class,
                    objectName,
                    sourceType
            );

            if (!sourceLines.isEmpty()) {
                StringBuilder ddl = new StringBuilder();

                // Add CREATE OR REPLACE if not present
                String firstLine = sourceLines.get(0).toUpperCase();
                if (!firstLine.contains("CREATE OR REPLACE") &&
                        !firstLine.contains("CREATE") &&
                        !firstLine.contains("FUNCTION") &&
                        !firstLine.contains("PROCEDURE") &&
                        !firstLine.contains("PACKAGE") &&
                        !firstLine.contains("TYPE")) {

                    ddl.append("CREATE OR REPLACE ");
                }

                for (String line : sourceLines) {
                    ddl.append(line);
                }

                // Add trailing slash for procedures/functions
                String ddlStr = ddl.toString();
                if (objectType.equalsIgnoreCase("PROCEDURE") ||
                        objectType.equalsIgnoreCase("FUNCTION") ||
                        objectType.equalsIgnoreCase("PACKAGE") ||
                        objectType.equalsIgnoreCase("PACKAGE BODY")) {
                    if (!ddlStr.trim().endsWith("/")) {
                        ddlStr = ddlStr + "\n/";
                    }
                }

                return ddlStr;
            }

            return null;
        } catch (Exception e) {
            log.debug("getDDLFromSource failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Method 4: Get DDL from ALL_SOURCE
     */
    private String getDDLFromAllSource(String objectName, String objectType) {
        try {
            log.info("Attempting to get DDL for {} {} from ALL_SOURCE", objectType, objectName);

            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                return null;
            }

            String sourceType = objectType.toUpperCase();
            if (sourceType.equals("PACKAGE BODY")) {
                sourceType = "PACKAGE BODY";
            }

            String sql = "SELECT text FROM all_source " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                    "AND UPPER(type) = UPPER(?) ORDER BY line";

            List<String> sourceLines = oracleJdbcTemplate.queryForList(
                    sql,
                    String.class,
                    owner,
                    objectName,
                    sourceType
            );

            if (!sourceLines.isEmpty()) {
                return String.join("", sourceLines);
            }

            return null;
        } catch (Exception e) {
            log.debug("getDDLFromAllSource failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Method 5: DBMS_METADATA with explicit owner resolution
     */
    private String getObjectDDLWithOwnerResolution(String objectName, String objectType) {
        try {
            log.info("Attempting to get DDL for {} {} with owner resolution", objectType, objectName);

            // First find the object owner
            String findOwnerSql = "SELECT owner FROM all_objects " +
                    "WHERE UPPER(object_name) = UPPER(?) " +
                    "AND UPPER(object_type) = UPPER(?) AND ROWNUM = 1";

            String owner;
            try {
                owner = oracleJdbcTemplate.queryForObject(
                        findOwnerSql,
                        String.class,
                        objectName,
                        objectType
                );
            } catch (EmptyResultDataAccessException e) {
                // Try without object type filter
                String findAnySql = "SELECT owner FROM all_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND ROWNUM = 1";
                owner = oracleJdbcTemplate.queryForObject(
                        findAnySql,
                        String.class,
                        objectName
                );
            }

            if (owner == null) {
                return null;
            }

            String metadataType = convertToMetadataObjectType(objectType);

            // Try with fully qualified name
            String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?) FROM DUAL";

            try {
                return oracleJdbcTemplate.queryForObject(
                        sql,
                        String.class,
                        metadataType,
                        owner + "." + objectName.toUpperCase()
                );
            } catch (Exception e) {
                // Try with separate owner parameter
                String sql2 = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) FROM DUAL";
                return oracleJdbcTemplate.queryForObject(
                        sql2,
                        String.class,
                        metadataType,
                        objectName.toUpperCase(),
                        owner
                );
            }

        } catch (Exception e) {
            log.debug("getObjectDDLWithOwnerResolution failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Method 6: Try DBA_SOURCE if available (requires higher privileges)
     */
    private String getDDLFromDBASource(String objectName, String objectType) {
        try {
            log.info("Attempting to get DDL for {} {} from DBA_SOURCE", objectType, objectName);

            // Check if user has access to DBA_SOURCE
            String checkSql = "SELECT COUNT(*) FROM all_tables WHERE table_name = 'DBA_SOURCE'";
            Integer count = oracleJdbcTemplate.queryForObject(checkSql, Integer.class);

            if (count == 0) {
                return null;
            }

            String sourceType = objectType.toUpperCase();
            if (sourceType.equals("PACKAGE BODY")) {
                sourceType = "PACKAGE BODY";
            }

            String sql = "SELECT text FROM dba_source " +
                    "WHERE UPPER(name) = UPPER(?) AND UPPER(type) = UPPER(?) " +
                    "ORDER BY line";

            List<String> sourceLines = oracleJdbcTemplate.queryForList(
                    sql,
                    String.class,
                    objectName,
                    sourceType
            );

            if (!sourceLines.isEmpty()) {
                return String.join("", sourceLines);
            }

            return null;
        } catch (Exception e) {
            log.debug("getDDLFromDBASource failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Method 7: Generate procedure DDL from data dictionary
     */
    private String generateProcedureDDL(String procedureName) {
        try {
            log.info("Attempting to generate DDL for procedure {}", procedureName);

            Map<String, Object> objectLocation = findObjectLocation(procedureName, "PROCEDURE");
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE OR REPLACE PROCEDURE ");
            if (!owner.equalsIgnoreCase(getCurrentUser())) {
                ddl.append(owner).append(".");
            }
            ddl.append(procedureName).append("\n");

            // Get parameters
            String paramSql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                paramSql = "SELECT " +
                        "    argument_name, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale " +
                        "FROM user_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND package_name IS NULL " +
                        "AND argument_name IS NOT NULL " +
                        "ORDER BY position";
            } else {
                paramSql = "SELECT " +
                        "    argument_name, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale " +
                        "FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL AND argument_name IS NOT NULL " +
                        "ORDER BY position";
            }

            List<Map<String, Object>> params;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                params = oracleJdbcTemplate.queryForList(paramSql, procedureName);
            } else {
                params = oracleJdbcTemplate.queryForList(paramSql, owner, procedureName);
            }

            if (!params.isEmpty()) {
                ddl.append("(\n");
                for (int i = 0; i < params.size(); i++) {
                    Map<String, Object> param = params.get(i);
                    ddl.append("    ");

                    String argumentName = (String) param.get("argument_name");
                    String dataType = (String) param.get("data_type");
                    String inOut = (String) param.get("in_out");

                    if (inOut != null) {
                        if ("IN".equals(inOut)) {
                            ddl.append(argumentName).append(" ");
                        } else if ("OUT".equals(inOut)) {
                            ddl.append(argumentName).append(" OUT ");
                        } else if ("IN/OUT".equals(inOut)) {
                            ddl.append(argumentName).append(" IN OUT ");
                        }
                    } else {
                        ddl.append(argumentName).append(" ");
                    }

                    ddl.append(dataType);

                    // Add length/precision if applicable
                    Number dataLength = (Number) param.get("data_length");
                    Number dataPrecision = (Number) param.get("data_precision");
                    Number dataScale = (Number) param.get("data_scale");

                    if (dataLength != null && dataLength.intValue() > 0 &&
                            ("VARCHAR2".equalsIgnoreCase(dataType) || "CHAR".equalsIgnoreCase(dataType) ||
                                    "VARCHAR".equalsIgnoreCase(dataType) || "NVARCHAR2".equalsIgnoreCase(dataType))) {
                        ddl.append("(").append(dataLength).append(")");
                    } else if (dataPrecision != null) {
                        ddl.append("(").append(dataPrecision);
                        if (dataScale != null && dataScale.intValue() > 0) {
                            ddl.append(",").append(dataScale);
                        }
                        ddl.append(")");
                    }

                    if (i < params.size() - 1) {
                        ddl.append(",");
                    }
                    ddl.append("\n");
                }
                ddl.append(")\n");
            }

            ddl.append("IS\n");
            ddl.append("BEGIN\n");
            ddl.append("    -- Procedure logic here\n");
            ddl.append("    NULL;\n");
            ddl.append("END ").append(procedureName).append(";\n/");

            return ddl.toString();

        } catch (Exception e) {
            log.debug("generateProcedureDDL failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate function DDL from data dictionary
     */
    private String generateFunctionDDL(String functionName) {
        try {
            log.info("Attempting to generate DDL for function {}", functionName);

            Map<String, Object> objectLocation = findObjectLocation(functionName, "FUNCTION");
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE OR REPLACE FUNCTION ");
            if (!owner.equalsIgnoreCase(getCurrentUser())) {
                ddl.append(owner).append(".");
            }
            ddl.append(functionName).append("\n");

            // Get parameters and return type
            String paramSql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                paramSql = "SELECT " +
                        "    argument_name, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    position " +
                        "FROM user_arguments " +
                        "WHERE UPPER(object_name) = UPPER(?) AND package_name IS NULL " +
                        "ORDER BY position, sequence";
            } else {
                paramSql = "SELECT " +
                        "    argument_name, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    position " +
                        "FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL " +
                        "ORDER BY position, sequence";
            }

            List<Map<String, Object>> allArgs;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                allArgs = oracleJdbcTemplate.queryForList(paramSql, functionName);
            } else {
                allArgs = oracleJdbcTemplate.queryForList(paramSql, owner, functionName);
            }

            // Separate return type (position = 0) from parameters
            Map<String, Object> returnType = null;
            List<Map<String, Object>> params = new ArrayList<>();

            for (Map<String, Object> arg : allArgs) {
                Number position = (Number) arg.get("position");
                if (position != null && position.intValue() == 0) {
                    returnType = arg;
                } else {
                    params.add(arg);
                }
            }

            if (!params.isEmpty()) {
                ddl.append("(\n");
                for (int i = 0; i < params.size(); i++) {
                    Map<String, Object> param = params.get(i);
                    ddl.append("    ");

                    String argumentName = (String) param.get("argument_name");
                    String dataType = (String) param.get("data_type");
                    String inOut = (String) param.get("in_out");

                    if (inOut != null && !"IN".equals(inOut)) {
                        if ("OUT".equals(inOut)) {
                            ddl.append(argumentName).append(" OUT ");
                        } else if ("IN/OUT".equals(inOut)) {
                            ddl.append(argumentName).append(" IN OUT ");
                        }
                    } else {
                        ddl.append(argumentName).append(" ");
                    }

                    ddl.append(dataType);

                    // Add length/precision if applicable
                    Number dataLength = (Number) param.get("data_length");
                    Number dataPrecision = (Number) param.get("data_precision");
                    Number dataScale = (Number) param.get("data_scale");

                    if (dataLength != null && dataLength.intValue() > 0 &&
                            ("VARCHAR2".equalsIgnoreCase(dataType) || "CHAR".equalsIgnoreCase(dataType) ||
                                    "VARCHAR".equalsIgnoreCase(dataType) || "NVARCHAR2".equalsIgnoreCase(dataType))) {
                        ddl.append("(").append(dataLength).append(")");
                    } else if (dataPrecision != null) {
                        ddl.append("(").append(dataPrecision);
                        if (dataScale != null && dataScale.intValue() > 0) {
                            ddl.append(",").append(dataScale);
                        }
                        ddl.append(")");
                    }

                    if (i < params.size() - 1) {
                        ddl.append(",");
                    }
                    ddl.append("\n");
                }
                ddl.append(")\n");
            }

            if (returnType != null) {
                ddl.append("RETURN ").append(returnType.get("data_type"));

                Number dataLength = (Number) returnType.get("data_length");
                Number dataPrecision = (Number) returnType.get("data_precision");
                Number dataScale = (Number) returnType.get("data_scale");
                String dataType = (String) returnType.get("data_type");

                if (dataLength != null && dataLength.intValue() > 0 &&
                        ("VARCHAR2".equalsIgnoreCase(dataType) || "CHAR".equalsIgnoreCase(dataType) ||
                                "VARCHAR".equalsIgnoreCase(dataType) || "NVARCHAR2".equalsIgnoreCase(dataType))) {
                    ddl.append("(").append(dataLength).append(")");
                } else if (dataPrecision != null) {
                    ddl.append("(").append(dataPrecision);
                    if (dataScale != null && dataScale.intValue() > 0) {
                        ddl.append(",").append(dataScale);
                    }
                    ddl.append(")");
                }
                ddl.append("\n");
            }

            ddl.append("IS\n");
            ddl.append("BEGIN\n");
            ddl.append("    -- Function logic here\n");
            ddl.append("    RETURN NULL;\n");
            ddl.append("END ").append(functionName).append(";\n/");

            return ddl.toString();

        } catch (Exception e) {
            log.debug("generateFunctionDDL failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate table DDL from data dictionary
     */
    private String generateTableDDL(String tableName) {
        try {
            log.info("Attempting to generate DDL for table {}", tableName);

            Map<String, Object> objectLocation = findObjectLocation(tableName, "TABLE");
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE ");
            if (!owner.equalsIgnoreCase(getCurrentUser())) {
                ddl.append(owner).append(".");
            }
            ddl.append(tableName).append(" (\n");

            List<Map<String, Object>> columns = getTableColumns(owner, tableName);

            for (int i = 0; i < columns.size(); i++) {
                Map<String, Object> col = columns.get(i);
                ddl.append("    ").append(col.get("column_name"))
                        .append(" ").append(col.get("data_type"));

                Number dataLength = (Number) col.get("data_length");
                Number dataPrecision = (Number) col.get("data_precision");
                Number dataScale = (Number) col.get("data_scale");
                String dataType = (String) col.get("data_type");

                if (dataLength != null && dataLength.intValue() > 0 &&
                        ("VARCHAR2".equalsIgnoreCase(dataType) || "CHAR".equalsIgnoreCase(dataType) ||
                                "VARCHAR".equalsIgnoreCase(dataType) || "NVARCHAR2".equalsIgnoreCase(dataType))) {
                    ddl.append("(").append(dataLength).append(")");
                } else if (dataPrecision != null) {
                    ddl.append("(").append(dataPrecision);
                    if (dataScale != null && dataScale.intValue() > 0) {
                        ddl.append(",").append(dataScale);
                    }
                    ddl.append(")");
                }

                if ("N".equals(col.get("nullable"))) {
                    ddl.append(" NOT NULL");
                }

                Object defaultValue = col.get("data_default");
                if (defaultValue != null && !defaultValue.toString().isEmpty()) {
                    ddl.append(" DEFAULT ").append(defaultValue);
                }

                if (i < columns.size() - 1) {
                    ddl.append(",");
                }
                ddl.append("\n");
            }

            // Add primary key constraint if exists
            List<Map<String, Object>> constraints = getTableConstraints(owner, tableName);
            for (Map<String, Object> constraint : constraints) {
                if ("P".equals(constraint.get("constraint_type"))) {
                    ddl.append("    CONSTRAINT ").append(constraint.get("constraint_name"))
                            .append(" PRIMARY KEY (").append(constraint.get("columns")).append(")\n");
                    break;
                }
            }

            ddl.append(");");
            return ddl.toString();

        } catch (Exception e) {
            log.debug("generateTableDDL failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate view DDL from data dictionary
     */
    private String generateViewDDL(String viewName) {
        try {
            log.info("Attempting to generate DDL for view {}", viewName);

            Map<String, Object> objectLocation = findObjectLocation(viewName, "VIEW");
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            String viewText;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                String sql = "SELECT text FROM user_views WHERE UPPER(view_name) = UPPER(?)";
                viewText = oracleJdbcTemplate.queryForObject(sql, String.class, viewName);
            } else {
                String sql = "SELECT text FROM all_views WHERE UPPER(owner) = UPPER(?) AND UPPER(view_name) = UPPER(?)";
                viewText = oracleJdbcTemplate.queryForObject(sql, String.class, owner, viewName);
            }

            if (viewText != null && !viewText.isEmpty()) {
                StringBuilder ddl = new StringBuilder();
                ddl.append("CREATE OR REPLACE VIEW ");
                if (!owner.equalsIgnoreCase(getCurrentUser())) {
                    ddl.append(owner).append(".");
                }
                ddl.append(viewName).append(" AS\n");
                ddl.append(viewText);
                return ddl.toString();
            }

            return null;

        } catch (Exception e) {
            log.debug("generateViewDDL failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get object diagnostics to help debug DDL retrieval issues
     */
    private Map<String, Object> getObjectDiagnostics(String objectName, String objectType) {
        Map<String, Object> diagnostics = new HashMap<>();

        try {
            // Check if object exists
            String checkSql = "SELECT owner, status, created, last_ddl_time " +
                    "FROM all_objects WHERE UPPER(object_name) = UPPER(?) " +
                    "AND UPPER(object_type) = UPPER(?)";

            try {
                Map<String, Object> objectInfo = oracleJdbcTemplate.queryForMap(
                        checkSql, objectName, objectType);
                diagnostics.put("exists", true);
                diagnostics.put("owner", objectInfo.get("owner"));
                diagnostics.put("status", objectInfo.get("status"));
                diagnostics.put("created", objectInfo.get("created"));
                diagnostics.put("lastModified", objectInfo.get("last_ddl_time"));
            } catch (EmptyResultDataAccessException e) {
                diagnostics.put("exists", false);
                diagnostics.put("message", "Object not found in all_objects");
            }

            // Check current user
            diagnostics.put("currentUser", getCurrentUser());
            diagnostics.put("currentSchema", getCurrentSchema());

            // Check privileges
            checkPrivileges(diagnostics);

            // Check if object is in source views
            checkSourceAvailability(diagnostics, objectName, objectType);

        } catch (Exception e) {
            diagnostics.put("error", e.getMessage());
        }

        return diagnostics;
    }

    /**
     * Check if user has necessary privileges
     */
    private void checkPrivileges(Map<String, Object> diagnostics) {
        try {
            // Check SELECT_CATALOG_ROLE
            String roleSql = "SELECT COUNT(*) FROM session_roles WHERE role = 'SELECT_CATALOG_ROLE'";
            Integer hasRole = oracleJdbcTemplate.queryForObject(roleSql, Integer.class);
            diagnostics.put("hasSelectCatalogRole", hasRole > 0);

            // Check EXECUTE on DBMS_METADATA
            String execSql = "SELECT COUNT(*) FROM all_tab_privs " +
                    "WHERE table_name = 'DBMS_METADATA' AND privilege = 'EXECUTE' " +
                    "AND grantee IN (USER, 'PUBLIC')";
            Integer hasExec = oracleJdbcTemplate.queryForObject(execSql, Integer.class);
            diagnostics.put("hasExecuteOnDbmsMetadata", hasExec > 0);

        } catch (Exception e) {
            diagnostics.put("privilegeCheckError", e.getMessage());
        }
    }

    /**
     * Check if object is available in source views
     */
    private void checkSourceAvailability(Map<String, Object> diagnostics,
                                         String objectName, String objectType) {
        try {
            String sourceType = objectType.toUpperCase();
            if (sourceType.equals("PACKAGE BODY")) {
                sourceType = "PACKAGE BODY";
            }

            // Check USER_SOURCE
            String userSourceSql = "SELECT COUNT(*) FROM user_source " +
                    "WHERE UPPER(name) = UPPER(?) AND UPPER(type) = UPPER(?)";
            Integer userSourceCount = oracleJdbcTemplate.queryForObject(
                    userSourceSql, Integer.class, objectName, sourceType);
            diagnostics.put("inUserSource", userSourceCount > 0);

            // Check ALL_SOURCE
            String allSourceSql = "SELECT COUNT(*) FROM all_source " +
                    "WHERE UPPER(name) = UPPER(?) AND UPPER(type) = UPPER(?)";
            Integer allSourceCount = oracleJdbcTemplate.queryForObject(
                    allSourceSql, Integer.class, objectName, sourceType);
            diagnostics.put("inAllSource", allSourceCount > 0);

        } catch (Exception e) {
            diagnostics.put("sourceCheckError", e.getMessage());
        }
    }

    /**
     * Generate detailed error message
     */
    private String generateDetailedErrorMessage(String objectName, String objectType) {
        StringBuilder msg = new StringBuilder();
        msg.append("-- DDL not available for ").append(objectType).append(" ").append(objectName).append("\n");
        msg.append("-- \n");
        msg.append("-- Possible reasons and solutions:\n");
        msg.append("-- \n");
        msg.append("-- 1. INSUFFICIENT PRIVILEGES:\n");
        msg.append("--    Run as DBA: GRANT SELECT_CATALOG_ROLE TO ").append(getCurrentUser()).append(";\n");
        msg.append("--    Run as DBA: GRANT EXECUTE ON DBMS_METADATA TO ").append(getCurrentUser()).append(";\n");
        msg.append("-- \n");
        msg.append("-- 2. OBJECT IN DIFFERENT SCHEMA:\n");
        msg.append("--    Try specifying the owner: OWNER.").append(objectName).append("\n");
        msg.append("-- \n");
        msg.append("-- 3. OBJECT TYPE NOT SUPPORTED:\n");
        msg.append("--    Some object types may not have DDL available\n");
        msg.append("-- \n");
        msg.append("-- 4. DATABASE VERSION COMPATIBILITY:\n");
        msg.append("--    DBMS_METADATA may behave differently in different Oracle versions\n");
        msg.append("-- \n");
        msg.append("-- 5. TEMPORARY OR GENERATED OBJECT:\n");
        msg.append("--    Temporary or generated objects may not have stored DDL\n");

        return msg.toString();
    }

    /**
     * Convert object type to DBMS_METADATA format
     */
    private String convertToMetadataObjectType(String objectType) {
        if (objectType == null) return null;

        String upperType = objectType.toUpperCase();

        switch (upperType) {
            case "PACKAGE BODY":
                return "PACKAGE_BODY";
            case "TYPE BODY":
                return "TYPE_BODY";
            case "MATERIALIZED VIEW":
                return "MATERIALIZED_VIEW";
            case "DATABASE LINK":
                return "DB_LINK";
            case "JAVA CLASS":
            case "JAVA SOURCE":
            case "JAVA RESOURCE":
                return "JAVA";
            case "PROCEDURE":
            case "FUNCTION":
            case "PACKAGE":
            case "TABLE":
            case "VIEW":
            case "TRIGGER":
            case "INDEX":
            case "SEQUENCE":
            case "SYNONYM":
            case "TYPE":
                return upperType;
            default:
                return upperType.replace(' ', '_');
        }
    }

    // ==================== VALIDATION AND UTILITY METHODS ====================

    /**
     * Validate if an object exists and is accessible
     */
    public Map<String, Object> validateObject(String objectName, String objectType, String owner) {
        Map<String, Object> result = new HashMap<>();
        result.put("objectName", objectName);
        result.put("objectType", objectType);
        result.put("owner", owner);

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
                result.put("owner", owner);
            }

            String sql = "SELECT status FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND object_type = ?";

            String status = oracleJdbcTemplate.queryForObject(sql, String.class, owner, objectName, objectType);

            result.put("exists", true);
            result.put("status", status);
            result.put("accessible", true);
            result.put("valid", "VALID".equalsIgnoreCase(status));

        } catch (EmptyResultDataAccessException e) {
            result.put("exists", false);
            result.put("status", "NOT_FOUND");
            result.put("accessible", false);
            result.put("message", "Object not found");
        } catch (DataAccessException e) {
            result.put("exists", false);
            result.put("status", "ERROR");
            result.put("accessible", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Validate synonym and resolve target
     */
    public Map<String, Object> validateSynonym(String synonymName) {
        Map<String, Object> result = new HashMap<>();
        result.put("synonymName", synonymName);

        try {
            // First check if synonym exists
            String sql = "SELECT " +
                    "    synonym_name, " +
                    "    table_owner, " +
                    "    table_name, " +
                    "    db_link " +
                    "FROM user_synonyms " +
                    "WHERE UPPER(synonym_name) = UPPER(?)";

            Map<String, Object> synonym = oracleJdbcTemplate.queryForMap(sql, synonymName);
            result.put("synonym", synonym);
            result.put("exists", true);

            String targetOwner = (String) synonym.get("table_owner");
            String targetName = (String) synonym.get("table_name");
            String dbLink = (String) synonym.get("db_link");

            if (dbLink != null) {
                result.put("isRemote", true);
                result.put("targetStatus", "REMOTE");
                result.put("message", "Remote object via database link: " + dbLink);
            } else {
                // Validate target object
                String targetSql = "SELECT object_type, status FROM all_objects " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";

                try {
                    Map<String, Object> target = oracleJdbcTemplate.queryForMap(targetSql, targetOwner, targetName);
                    result.put("targetType", target.get("object_type"));
                    result.put("targetStatus", target.get("status"));
                    result.put("targetValid", "VALID".equalsIgnoreCase((String) target.get("status")));
                } catch (EmptyResultDataAccessException e) {
                    result.put("targetStatus", "TARGET_NOT_FOUND");
                    result.put("message", "Synonym target not found or not accessible");
                }
            }

        } catch (EmptyResultDataAccessException e) {
            result.put("exists", false);
            result.put("status", "NOT_FOUND");
            result.put("message", "Synonym not found");
        } catch (Exception e) {
            result.put("exists", false);
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }

        return result;
    }

    // ==================== ENHANCED SEARCH METHODS ====================

    /**
     * Search across all objects including synonym targets
     */
    public List<Map<String, Object>> comprehensiveSearch(String searchPattern) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            String searchParam = "%" + searchPattern.toUpperCase() + "%";

            // Search regular objects
            String objectSql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    status, " +
                    "    created, " +
                    "    last_ddl_time " +
                    "FROM all_objects " +
                    "WHERE UPPER(object_name) LIKE ? " +
                    "ORDER BY object_type, object_name";

            List<Map<String, Object>> objects = oracleJdbcTemplate.queryForList(objectSql, searchParam);

            // Transform objects with type icons
            objects.forEach(obj -> {
                obj.put("id", "obj-" + System.currentTimeMillis() + "-" + obj.get("object_name"));
                obj.put("searchType", "OBJECT");
                obj.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
                obj.put("displayType", formatObjectTypeForDisplay((String) obj.get("object_type")));
            });

            results.addAll(objects);

            // Search synonyms and their targets
            String synonymSql = "SELECT " +
                    "    s.owner, " +
                    "    s.synonym_name as object_name, " +
                    "    'SYNONYM' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    o.object_type as target_type, " +
                    "    o.status as target_status " +
                    "FROM all_synonyms s " +
                    "LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) LIKE ? " +
                    "OR UPPER(s.table_name) LIKE ? " +
                    "ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = oracleJdbcTemplate.queryForList(
                    synonymSql, searchParam, searchParam);

            // Transform synonyms
            synonyms.forEach(syn -> {
                syn.put("id", "syn-" + System.currentTimeMillis() + "-" + syn.get("object_name"));
                syn.put("searchType", "SYNONYM");
                syn.put("icon", "synonym");
                syn.put("displayType", "Synonym");
                syn.put("targetDisplay", syn.get("target_owner") + "." + syn.get("target_name") +
                        (syn.get("db_link") != null ? "@" + syn.get("db_link") : ""));
            });

            results.addAll(synonyms);

            // Sort by object type and name
            results.sort((a, b) -> {
                String typeA = (String) a.get("object_type");
                String typeB = (String) b.get("object_type");
                int typeCompare = typeA.compareTo(typeB);
                if (typeCompare != 0) return typeCompare;

                String nameA = (String) a.get("object_name");
                String nameB = (String) b.get("object_name");
                return nameA.compareTo(nameB);
            });

            return results;

        } catch (Exception e) {
            log.error("Error in comprehensiveSearch: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Search objects for frontend (without pagination)
     */
    public List<Map<String, Object>> searchObjectsForFrontend(String searchPattern, String type, int maxResults) {
        try {
            log.info("Searching for objects with pattern: {}, type: {}, maxResults: {}", searchPattern, type, maxResults);

            String searchParam = "%" + searchPattern.toUpperCase() + "%";

            String sql;
            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                sql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    status, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM all_objects " +
                        "WHERE UPPER(object_name) LIKE ? AND object_type = ? AND ROWNUM <= ? " +
                        "ORDER BY object_type, object_name";

                List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, searchParam, type.toUpperCase(), maxResults);

                // Transform to frontend format
                return results.stream().map(obj -> {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "search-" + System.currentTimeMillis() + "-" + obj.get("object_name"));
                    transformed.put("name", obj.get("object_name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("object_type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("created", obj.get("created"));
                    transformed.put("lastModified", obj.get("last_ddl_time"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
                    return transformed;
                }).collect(Collectors.toList());

            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    object_name, " +
                        "    object_type, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    status, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM all_objects " +
                        "WHERE UPPER(object_name) LIKE ? AND ROWNUM <= ? " +
                        "ORDER BY object_type, object_name";

                List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, searchParam, maxResults);

                // Transform to frontend format
                return results.stream().map(obj -> {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "search-" + System.currentTimeMillis() + "-" + obj.get("object_name"));
                    transformed.put("name", obj.get("object_name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("object_type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("created", obj.get("created"));
                    transformed.put("lastModified", obj.get("last_ddl_time"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
                    return transformed;
                }).collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("Error in searchObjectsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING TABLE METHODS ====================

    /**
     * Get all tables from the current Oracle schema
     */
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

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTables: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tables: " + e.getMessage(), e);
        }
    }

    /**
     * Get all tables with frontend-friendly format
     */
    public List<Map<String, Object>> getAllTablesForFrontend() {
        try {
            List<Map<String, Object>> tables = getAllTables();

            return tables.stream().map(table -> {
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
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllTablesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get table details with frontend-friendly format
     */
    public Map<String, Object> getTableDetailsForFrontend(String tableName) {
        try {
            Map<String, Object> details = getTableDetails(getCurrentUser(), tableName);

            // Get additional info
            List<Map<String, Object>> columns = getTableColumns(getCurrentUser(), tableName);
            List<Map<String, Object>> constraints = getTableConstraints(getCurrentUser(), tableName);
            List<Map<String, Object>> indexes = getTableIndexes(getCurrentUser(), tableName);

            Map<String, Object> result = new HashMap<>();
            result.put("table_name", tableName);
            result.put("owner", getCurrentUser());
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

            return result;

        } catch (Exception e) {
            log.error("Error in getTableDetailsForFrontend for {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get table data with pagination (for frontend)
     */
    public Map<String, Object> getTableDataWithPagination(String tableName, int page, int pageSize, String sortColumn, String sortDirection) {
        try {
            log.info("Getting data for table: {}, page: {}, pageSize: {}", tableName, page, pageSize);

            // First get total count
            String countSql = "SELECT COUNT(*) FROM " + tableName;
            int totalRows = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            // Calculate offset
            int offset = (page - 1) * pageSize;

            // Build order by clause
            String orderBy = "";
            if (sortColumn != null && !sortColumn.isEmpty()) {
                orderBy = " ORDER BY \"" + sortColumn + "\" " + (sortDirection != null ? sortDirection : "ASC");
            }

            // Get data with pagination
            String dataSql = "SELECT * FROM " + tableName + orderBy + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> rows = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            // Get column information
            List<Map<String, Object>> columns = getTableColumns(getCurrentUser(), tableName);

            // Simplify column info for data response
            List<Map<String, String>> simpleColumns = columns.stream()
                    .map(col -> {
                        Map<String, String> simpleCol = new HashMap<>();
                        simpleCol.put("name", (String) col.get("column_name"));
                        simpleCol.put("type", (String) col.get("data_type"));
                        simpleCol.put("nullable", (String) col.get("nullable"));
                        return simpleCol;
                    })
                    .collect(Collectors.toList());

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

    /**
     * Get tables by schema
     */
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

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getTablesBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tables for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get table details
     */
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
            throw new RuntimeException("Failed to retrieve table details: " + e.getMessage(), e);
        }
    }

    /**
     * Get table columns
     */
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
            throw new RuntimeException("Failed to retrieve table columns: " + e.getMessage(), e);
        }
    }

    /**
     * Get table constraints
     */
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
            throw new RuntimeException("Failed to retrieve table constraints: " + e.getMessage(), e);
        }
    }

    /**
     * Get table indexes
     */
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
            throw new RuntimeException("Failed to retrieve table indexes: " + e.getMessage(), e);
        }
    }

    /**
     * Search for tables by name pattern
     */
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

            return oracleJdbcTemplate.queryForList(sql, "%" + searchPattern + "%");

        } catch (Exception e) {
            log.error("Error in searchTables: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search tables: " + e.getMessage(), e);
        }
    }

    /**
     * Get table statistics
     */
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

                return oracleJdbcTemplate.queryForMap(sql, tableName);
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

                return oracleJdbcTemplate.queryForMap(sql, owner, tableName);
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

    /**
     * Get tables with row count
     */
    public List<Map<String, Object>> getTablesWithRowCount() {
        try {
            String sql = "SELECT " +
                    "    table_name, " +
                    "    num_rows, " +
                    "    last_analyzed, " +
                    "    status " +
                    "FROM user_tables " +
                    "ORDER BY num_rows DESC NULLS LAST";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getTablesWithRowCount: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tables with row count: " + e.getMessage(), e);
        }
    }

    /**
     * Get table count by tablespace
     */
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

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getTableCountByTablespace: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tablespace statistics: " + e.getMessage(), e);
        }
    }

    /**
     * Get recent tables
     */
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

            return oracleJdbcTemplate.queryForList(sql, days, days);

        } catch (Exception e) {
            log.error("Error in getRecentTables: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recent tables: " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING VIEW METHODS ====================

    /**
     * Get all views
     */
    public List<Map<String, Object>> getAllViews() {
        try {
            String sql = "SELECT " +
                    "    v.view_name, " +
                    "    v.text_length, " +
                    "    v.text, " +
                    "    v.read_only, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM user_views v " +
                    "JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                    "ORDER BY v.view_name";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllViews: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve views: " + e.getMessage(), e);
        }
    }

    /**
     * Get views by schema
     */
    public List<Map<String, Object>> getViewsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    v.owner, " +
                    "    v.view_name, " +
                    "    v.text_length, " +
                    "    v.text, " +
                    "    v.read_only, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_views v " +
                    "JOIN all_objects o ON v.owner = o.owner AND v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                    "WHERE UPPER(v.owner) = UPPER(?) " +
                    "ORDER BY v.view_name";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getViewsBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve views for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get view details
     */
    public Map<String, Object> getViewDetails(String viewName) {
        try {
            Map<String, Object> viewLocation = findObjectLocation(viewName, "VIEW");
            String owner = (String) viewLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            return getViewDetails(owner, viewName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("View {} not found", viewName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("view_name", viewName);
            emptyResult.put("message", "View not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getViewDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve view details: " + e.getMessage(), e);
        }
    }

    /**
     * Get all views for frontend
     */
    public List<Map<String, Object>> getAllViewsForFrontend() {
        try {
            String sql = "SELECT " +
                    "    v.view_name, " +
                    "    v.text_length, " +
                    "    v.read_only, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    (SELECT COUNT(*) FROM user_tab_columns WHERE table_name = v.view_name) as column_count " +
                    "FROM user_views v " +
                    "JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                    "ORDER BY v.view_name";

            List<Map<String, Object>> views = oracleJdbcTemplate.queryForList(sql);

            return views.stream().map(view -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "view-" + System.currentTimeMillis() + "-" + view.get("view_name"));
                transformed.put("name", view.get("view_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "VIEW");
                transformed.put("status", view.get("status"));
                transformed.put("columnCount", view.get("column_count"));
                transformed.put("textLength", view.get("text_length"));
                transformed.put("readOnly", view.get("read_only"));
                transformed.put("created", view.get("created"));
                transformed.put("lastModified", view.get("last_ddl_time"));
                transformed.put("icon", "view");
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllViewsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING PROCEDURE METHODS ====================

    /**
     * Get all procedures
     */
    public List<Map<String, Object>> getAllProcedures() {
        try {
            String sql = "SELECT " +
                    "    object_name as procedure_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary, " +
                    "    (SELECT COUNT(*) FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'PROCEDURE' " +
                    "ORDER BY object_name";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllProcedures: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedures: " + e.getMessage(), e);
        }
    }

    /**
     * Get procedures by schema
     */
    public List<Map<String, Object>> getProceduresBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as procedure_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary, " +
                    "    (SELECT COUNT(*) FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count " +
                    "FROM all_objects o " +
                    "WHERE o.object_type = 'PROCEDURE' AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getProceduresBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedures for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get procedure details
     */
    public Map<String, Object> getProcedureDetails(String procedureName) {
        try {
            Map<String, Object> procLocation = findObjectLocation(procedureName, "PROCEDURE");
            String owner = (String) procLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            return getProcedureDetails(owner, procedureName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Procedure {} not found", procedureName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("procedure_name", procedureName);
            emptyResult.put("message", "Procedure not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getProcedureDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedure details: " + e.getMessage(), e);
        }
    }

    /**
     * Get all procedures for frontend
     */
    public List<Map<String, Object>> getAllProceduresForFrontend() {
        try {
            String sql = "SELECT " +
                    "    object_name as procedure_name, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    (SELECT COUNT(*) FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'PROCEDURE' " +
                    "ORDER BY object_name";

            List<Map<String, Object>> procedures = oracleJdbcTemplate.queryForList(sql);

            return procedures.stream().map(proc -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "procedure-" + System.currentTimeMillis() + "-" + proc.get("procedure_name"));
                transformed.put("name", proc.get("procedure_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "PROCEDURE");
                transformed.put("status", proc.get("status"));
                transformed.put("parameterCount", proc.get("parameter_count"));
                transformed.put("created", proc.get("created"));
                transformed.put("lastModified", proc.get("last_ddl_time"));
                transformed.put("icon", "procedure");
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllProceduresForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING FUNCTION METHODS ====================

    /**
     * Get all functions
     */
    public List<Map<String, Object>> getAllFunctions() {
        try {
            String sql = "SELECT " +
                    "    object_name as function_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary, " +
                    "    (SELECT COUNT(*) FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL AND argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'FUNCTION' " +
                    "ORDER BY object_name";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllFunctions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve functions: " + e.getMessage(), e);
        }
    }

    /**
     * Get functions by schema
     */
    public List<Map<String, Object>> getFunctionsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as function_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary, " +
                    "    (SELECT COUNT(*) FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL AND argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM all_objects o " +
                    "WHERE o.object_type = 'FUNCTION' AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getFunctionsBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve functions for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get function details
     */
    public Map<String, Object> getFunctionDetails(String functionName) {
        try {
            Map<String, Object> funcLocation = findObjectLocation(functionName, "FUNCTION");
            String owner = (String) funcLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            return getFunctionDetails(owner, functionName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Function {} not found", functionName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("function_name", functionName);
            emptyResult.put("message", "Function not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getFunctionDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve function details: " + e.getMessage(), e);
        }
    }

    /**
     * Get all functions for frontend
     */
    public List<Map<String, Object>> getAllFunctionsForFrontend() {
        try {
            String sql = "SELECT " +
                    "    object_name as function_name, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    (SELECT COUNT(*) FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL AND argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'FUNCTION' " +
                    "ORDER BY object_name";

            List<Map<String, Object>> functions = oracleJdbcTemplate.queryForList(sql);

            return functions.stream().map(func -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "function-" + System.currentTimeMillis() + "-" + func.get("function_name"));
                transformed.put("name", func.get("function_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "FUNCTION");
                transformed.put("status", func.get("status"));
                transformed.put("parameterCount", func.get("parameter_count"));
                transformed.put("returnType", func.get("return_type"));
                transformed.put("created", func.get("created"));
                transformed.put("lastModified", func.get("last_ddl_time"));
                transformed.put("icon", "function");
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllFunctionsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING PACKAGE METHODS ====================

    /**
     * Get all packages
     */
    public List<Map<String, Object>> getAllPackages() {
        try {
            String sql = "SELECT " +
                    "    object_name as package_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') " +
                    "ORDER BY object_name, object_type";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllPackages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve packages: " + e.getMessage(), e);
        }
    }

    /**
     * Get packages by schema
     */
    public List<Map<String, Object>> getPackagesBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as package_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_objects o " +
                    "WHERE o.object_type IN ('PACKAGE', 'PACKAGE BODY') AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name, o.object_type";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getPackagesBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve packages for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get package details
     */
    public Map<String, Object> getPackageDetails(String packageName) {
        try {
            Map<String, Object> pkgLocation = findObjectLocation(packageName, "PACKAGE");
            if (pkgLocation.isEmpty()) {
                pkgLocation = findObjectLocation(packageName, "PACKAGE BODY");
            }

            String owner = (String) pkgLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
            }

            return getPackageDetails(owner, packageName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Package {} not found", packageName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("package_name", packageName);
            emptyResult.put("message", "Package not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getPackageDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve package details: " + e.getMessage(), e);
        }
    }

    /**
     * Get all packages for frontend
     */
    public List<Map<String, Object>> getAllPackagesForFrontend() {
        try {
            String distinctSql = "SELECT DISTINCT object_name as package_name FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') ORDER BY object_name";

            List<Map<String, Object>> packageNames = oracleJdbcTemplate.queryForList(distinctSql);

            return packageNames.stream().map(pkg -> {
                String pkgName = (String) pkg.get("package_name");
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "package-" + System.currentTimeMillis() + "-" + pkgName);
                transformed.put("name", pkgName);
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "PACKAGE");

                String specStatus = getPackageSpecStatus(pkgName);
                String bodyStatus = getPackageBodyStatus(pkgName);

                String status = "VALID";
                if ("INVALID".equals(specStatus) || "INVALID".equals(bodyStatus)) {
                    status = "INVALID";
                }

                transformed.put("status", status);
                transformed.put("specStatus", specStatus);
                transformed.put("bodyStatus", bodyStatus);
                transformed.put("created", getPackageCreated(pkgName));
                transformed.put("lastModified", getPackageLastModified(pkgName));
                transformed.put("icon", "package");

                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllPackagesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING TRIGGER METHODS ====================

    /**
     * Get all triggers
     */
    public List<Map<String, Object>> getAllTriggers() {
        try {
            String sql = "SELECT " +
                    "    t.trigger_name, " +
                    "    t.trigger_type, " +
                    "    t.triggering_event, " +
                    "    t.table_name, " +
                    "    t.status as trigger_status, " +
                    "    t.description, " +
                    "    t.trigger_body, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status as object_status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM user_triggers t " +
                    "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "ORDER BY t.trigger_name";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTriggers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve triggers: " + e.getMessage(), e);
        }
    }

    /**
     * Get triggers by schema
     */
    public List<Map<String, Object>> getTriggersBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    t.owner, " +
                    "    t.trigger_name, " +
                    "    t.trigger_type, " +
                    "    t.triggering_event, " +
                    "    t.table_owner, " +
                    "    t.table_name, " +
                    "    t.status as trigger_status, " +
                    "    t.description, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status as object_status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_triggers t " +
                    "JOIN all_objects o ON t.owner = o.owner AND t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.trigger_name";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getTriggersBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve triggers for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get trigger details
     */
    public Map<String, Object> getTriggerDetails(String triggerName) {
        try {
            Map<String, Object> triggerLocation = findObjectLocation(triggerName, "TRIGGER");
            String owner = (String) triggerLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            return getTriggerDetails(owner, triggerName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Trigger {} not found", triggerName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("trigger_name", triggerName);
            emptyResult.put("message", "Trigger not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getTriggerDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve trigger details: " + e.getMessage(), e);
        }
    }

    /**
     * Get all triggers for frontend
     */
    public List<Map<String, Object>> getAllTriggersForFrontend() {
        try {
            String sql = "SELECT " +
                    "    t.trigger_name, " +
                    "    t.trigger_type, " +
                    "    t.triggering_event, " +
                    "    t.table_name, " +
                    "    t.status as trigger_status, " +
                    "    t.description, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status as object_status " +
                    "FROM user_triggers t " +
                    "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "ORDER BY t.trigger_name";

            List<Map<String, Object>> triggers = oracleJdbcTemplate.queryForList(sql);

            return triggers.stream().map(trigger -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "trigger-" + System.currentTimeMillis() + "-" + trigger.get("trigger_name"));
                transformed.put("name", trigger.get("trigger_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "TRIGGER");
                transformed.put("status", trigger.get("trigger_status"));
                transformed.put("objectStatus", trigger.get("object_status"));
                transformed.put("triggerType", trigger.get("trigger_type"));
                transformed.put("triggeringEvent", trigger.get("triggering_event"));
                transformed.put("tableName", trigger.get("table_name"));
                transformed.put("description", trigger.get("description"));
                transformed.put("created", trigger.get("created"));
                transformed.put("lastModified", trigger.get("last_ddl_time"));
                transformed.put("icon", "trigger");
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllTriggersForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING SEQUENCE METHODS ====================

    /**
     * Get all sequences
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

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSequences: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequences: " + e.getMessage(), e);
        }
    }

    /**
     * Get sequences by schema
     */
    public List<Map<String, Object>> getSequencesBySchema(String schemaName) {
        try {
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

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getSequencesBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequences for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get sequence details
     */
    public Map<String, Object> getSequenceDetails(String sequenceName) {
        try {
            Map<String, Object> seqLocation = findObjectLocation(sequenceName, "SEQUENCE");
            String owner = (String) seqLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            return getSequenceDetails(owner, sequenceName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Sequence {} not found", sequenceName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("sequence_name", sequenceName);
            emptyResult.put("message", "Sequence not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getSequenceDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequence details: " + e.getMessage(), e);
        }
    }

    /**
     * Get all sequences for frontend
     */
    public List<Map<String, Object>> getAllSequencesForFrontend() {
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

            List<Map<String, Object>> sequences = oracleJdbcTemplate.queryForList(sql);

            return sequences.stream().map(seq -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "sequence-" + System.currentTimeMillis() + "-" + seq.get("sequence_name"));
                transformed.put("name", seq.get("sequence_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SEQUENCE");
                transformed.put("minValue", seq.get("min_value"));
                transformed.put("maxValue", seq.get("max_value"));
                transformed.put("incrementBy", seq.get("increment_by"));
                transformed.put("cycleFlag", seq.get("cycle_flag"));
                transformed.put("orderFlag", seq.get("order_flag"));
                transformed.put("cacheSize", seq.get("cache_size"));
                transformed.put("lastNumber", seq.get("last_number"));
                transformed.put("icon", "sequence");
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllSequencesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING SYNONYM METHODS ====================

    /**
     * Get all synonyms
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

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSynonyms: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve synonyms: " + e.getMessage(), e);
        }
    }

    /**
     * Get synonyms by schema
     */
    public List<Map<String, Object>> getSynonymsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    synonym_name, " +
                    "    table_owner, " +
                    "    table_name, " +
                    "    db_link " +
                    "FROM all_synonyms " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY synonym_name";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getSynonymsBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve synonyms for schema: " + e.getMessage(), e);
        }
    }

    // ==================== EXISTING TYPE METHODS ====================

    /**
     * Get all types
     */
    public List<Map<String, Object>> getAllTypes() {
        try {
            String sql = "SELECT " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM user_types t " +
                    "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "ORDER BY t.type_name";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTypes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve types: " + e.getMessage(), e);
        }
    }

    /**
     * Get types by schema
     */
    public List<Map<String, Object>> getTypesBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    t.owner, " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_types t " +
                    "JOIN all_objects o ON t.owner = o.owner AND t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.type_name";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getTypesBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve types for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Get type details
     */
    public Map<String, Object> getTypeDetails(String typeName) {
        try {
            Map<String, Object> typeLocation = findObjectLocation(typeName, "TYPE");
            if (typeLocation.isEmpty()) {
                typeLocation = findObjectLocation(typeName, "TYPE BODY");
            }

            String owner = (String) typeLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
            }

            return getTypeDetails(owner, typeName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Type {} not found", typeName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("type_name", typeName);
            emptyResult.put("message", "Type not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getTypeDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve type details: " + e.getMessage(), e);
        }
    }

    /**
     * Get all types for frontend
     */
    public List<Map<String, Object>> getAllTypesForFrontend() {
        try {
            String sql = "SELECT " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status " +
                    "FROM user_types t " +
                    "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "ORDER BY t.type_name";

            List<Map<String, Object>> types = oracleJdbcTemplate.queryForList(sql);

            return types.stream().map(type -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "type-" + System.currentTimeMillis() + "-" + type.get("type_name"));
                transformed.put("name", type.get("type_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "TYPE");
                transformed.put("status", type.get("status"));
                transformed.put("typecode", type.get("typecode"));
                transformed.put("attributeCount", type.get("attributes"));
                transformed.put("methodCount", type.get("methods"));
                transformed.put("created", type.get("created"));
                transformed.put("lastModified", type.get("last_ddl_time"));
                transformed.put("icon", "type");
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllTypesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== EXISTING DATABASE LINK METHODS ====================

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

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllDbLinks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links: " + e.getMessage(), e);
        }
    }

    /**
     * Get database links by schema
     */
    public List<Map<String, Object>> getDbLinksBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    db_link, " +
                    "    username, " +
                    "    host, " +
                    "    created " +
                    "FROM all_db_links " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY db_link";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getDbLinksBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links for schema: " + e.getMessage(), e);
        }
    }

    // ==================== GENERAL OBJECT METHODS ====================

    /**
     * Get all objects
     */
    public List<Map<String, Object>> getAllObjects() {
        try {
            String sql = "SELECT " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM user_objects " +
                    "ORDER BY object_type, object_name";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllObjects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get objects by schema
     */
    public List<Map<String, Object>> getObjectsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY object_type, object_name";

            return oracleJdbcTemplate.queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getObjectsBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve objects for schema: " + e.getMessage(), e);
        }
    }

    /**
     * Search objects by name pattern
     */
    public List<Map<String, Object>> searchObjects(String searchPattern) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM all_objects " +
                    "WHERE UPPER(object_name) LIKE UPPER(?) " +
                    "ORDER BY object_type, object_name";

            return oracleJdbcTemplate.queryForList(sql, "%" + searchPattern + "%");

        } catch (Exception e) {
            log.error("Error in searchObjects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search objects: " + e.getMessage(), e);
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
                    "    SUM(CASE WHEN status = 'INVALID' THEN 1 ELSE 0 END) as invalid_count, " +
                    "    SUM(CASE WHEN temporary = 'Y' THEN 1 ELSE 0 END) as temporary_count, " +
                    "    SUM(CASE WHEN generated = 'Y' THEN 1 ELSE 0 END) as generated_count, " +
                    "    SUM(CASE WHEN secondary = 'Y' THEN 1 ELSE 0 END) as secondary_count " +
                    "FROM user_objects " +
                    "GROUP BY object_type " +
                    "ORDER BY object_type";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getObjectCountByType: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve object count by type: " + e.getMessage(), e);
        }
    }

    /**
     * Get invalid objects
     */
    public List<Map<String, Object>> getInvalidObjects() {
        try {
            String sql = "SELECT " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status " +
                    "FROM user_objects " +
                    "WHERE status = 'INVALID' " +
                    "ORDER BY object_type, object_name";

            return oracleJdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getInvalidObjects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve invalid objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get objects by status
     */
    public List<Map<String, Object>> getObjectsByStatus(String status) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    object_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status " +
                    "FROM all_objects " +
                    "WHERE UPPER(status) = UPPER(?) " +
                    "ORDER BY owner, object_type, object_name";

            return oracleJdbcTemplate.queryForList(sql, status);

        } catch (Exception e) {
            log.error("Error in getObjectsByStatus: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve objects by status: " + e.getMessage(), e);
        }
    }

    // ==================== EXECUTE QUERY METHODS ====================

    /**
     * Execute SQL query
     */
    public Map<String, Object> executeQuery(String query, int timeoutSeconds, boolean readOnly) {
        try {
            log.info("Executing query, timeout: {}, readOnly: {}", timeoutSeconds, readOnly);

            long startTime = System.currentTimeMillis();

            if (timeoutSeconds > 0) {
                oracleJdbcTemplate.setQueryTimeout(timeoutSeconds);
            }

            List<Map<String, Object>> rows = oracleJdbcTemplate.queryForList(query);

            long executionTime = System.currentTimeMillis() - startTime;

            oracleJdbcTemplate.setQueryTimeout(-1);

            List<Map<String, String>> columns = new ArrayList<>();
            if (!rows.isEmpty()) {
                Map<String, Object> firstRow = rows.get(0);
                for (String key : firstRow.keySet()) {
                    Map<String, String> col = new HashMap<>();
                    col.put("name", key);
                    col.put("type", "VARCHAR2");
                    columns.add(col);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rows", rows);
            result.put("columns", columns);
            result.put("rowCount", rows.size());
            result.put("executionTime", executionTime);
            result.put("message", "Query executed successfully, " + rows.size() + " rows returned");

            return result;

        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    // ==================== DDL METHODS ====================

    /**
     * Get object DDL
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
     * Get object size
     */
    public Map<String, Object> getObjectSize(String objectName, String objectType) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String owner = (String) objectLocation.get("owner");

            if (owner == null) {
                log.warn("Object {} not found", objectName);
                Map<String, Object> empty = new HashMap<>();
                empty.put("status", "NOT_FOUND");
                return empty;
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


    // ============================================================
// PAGINATED METHODS FOR LARGE DATASETS
// ============================================================

    /**
     * Get paginated tables with total count only
     */
    public Map<String, Object> getTablesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            // Get total count first (fast)
            String countSql = "SELECT COUNT(*) FROM user_tables";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            // Get paginated data
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

            List<Map<String, Object>> tables = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

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

    /**
     * Get paginated views with total count only
     */
    public Map<String, Object> getViewsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_views";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            String dataSql = "SELECT view_name, text_length, read_only FROM user_views ORDER BY view_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> views = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            result.put("items", views);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getViewsPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get paginated procedures with total count only
     */
    public Map<String, Object> getProceduresPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_objects WHERE object_type = 'PROCEDURE'";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            String dataSql = "SELECT object_name as procedure_name, status, created, last_ddl_time " +
                    "FROM user_objects WHERE object_type = 'PROCEDURE' " +
                    "ORDER BY object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> procedures = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            result.put("items", procedures);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getProceduresPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get paginated functions with total count only
     */
    public Map<String, Object> getFunctionsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_objects WHERE object_type = 'FUNCTION'";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            String dataSql = "SELECT object_name as function_name, status, created, last_ddl_time " +
                    "FROM user_objects WHERE object_type = 'FUNCTION' " +
                    "ORDER BY object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> functions = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            result.put("items", functions);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getFunctionsPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get paginated packages with total count only
     */
    public Map<String, Object> getPackagesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(DISTINCT object_name) FROM user_objects WHERE object_type IN ('PACKAGE', 'PACKAGE BODY')";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            String dataSql = "SELECT DISTINCT object_name as package_name FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') " +
                    "ORDER BY object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> packages = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            result.put("items", packages);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getPackagesPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get paginated synonyms with total count only - CRITICAL for performance
     */
    public Map<String, Object> getSynonymsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            // Fast count query
            String countSql = "SELECT COUNT(*) FROM user_synonyms";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            // Only fetch the page we need
            String dataSql = "SELECT synonym_name, table_owner, table_name, db_link " +
                    "FROM user_synonyms ORDER BY synonym_name " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> synonyms = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            // Transform to frontend format without resolving target types (lazy loading)
            List<Map<String, Object>> transformed = synonyms.stream().map(syn -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", "syn-" + System.currentTimeMillis() + "-" + syn.get("synonym_name"));
                item.put("name", syn.get("synonym_name"));
                item.put("owner", getCurrentUser());
                item.put("type", "SYNONYM");
                item.put("targetOwner", syn.get("table_owner"));
                item.put("targetName", syn.get("table_name"));
                item.put("dbLink", syn.get("db_link"));
                item.put("isRemote", syn.get("db_link") != null);
                // Don't resolve target type here - do it lazily when user expands
                item.put("targetType", "PENDING");
                return item;
            }).collect(Collectors.toList());

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getSynonymsPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get paginated sequences with total count only
     */
    public Map<String, Object> getSequencesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_sequences";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            String dataSql = "SELECT sequence_name, min_value, max_value, increment_by, cycle_flag, cache_size, last_number " +
                    "FROM user_sequences ORDER BY sequence_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> sequences = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            result.put("items", sequences);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getSequencesPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get paginated types with total count only
     */
    public Map<String, Object> getTypesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_types";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            String dataSql = "SELECT type_name, typecode, attributes, methods " +
                    "FROM user_types ORDER BY type_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> types = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            result.put("items", types);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getTypesPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get paginated triggers with total count only
     */
    public Map<String, Object> getTriggersPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_triggers";
            int totalCount = oracleJdbcTemplate.queryForObject(countSql, Integer.class);

            String dataSql = "SELECT trigger_name, trigger_type, triggering_event, table_name, status " +
                    "FROM user_triggers ORDER BY trigger_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> triggers = oracleJdbcTemplate.queryForList(dataSql, offset, pageSize);

            result.put("items", triggers);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getTriggersPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * Get only the total counts for all object types (very fast)
     */
    public Map<String, Object> getAllObjectCounts() {
        Map<String, Object> counts = new HashMap<>();
        try {
            // Get all counts in a single query where possible
            String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM user_tables) as tables, " +
                    "(SELECT COUNT(*) FROM user_views) as views, " +
                    "(SELECT COUNT(*) FROM user_objects WHERE object_type = 'PROCEDURE') as procedures, " +
                    "(SELECT COUNT(*) FROM user_objects WHERE object_type = 'FUNCTION') as functions, " +
                    "(SELECT COUNT(DISTINCT object_name) FROM user_objects WHERE object_type IN ('PACKAGE', 'PACKAGE BODY')) as packages, " +
                    "(SELECT COUNT(*) FROM user_sequences) as sequences, " +
                    "(SELECT COUNT(*) FROM user_synonyms) as synonyms, " +
                    "(SELECT COUNT(*) FROM user_types) as types, " +
                    "(SELECT COUNT(*) FROM user_triggers) as triggers " +
                    "FROM DUAL";

            Map<String, Object> result = oracleJdbcTemplate.queryForMap(sql);

            counts.put("tables", result.get("tables"));
            counts.put("views", result.get("views"));
            counts.put("procedures", result.get("procedures"));
            counts.put("functions", result.get("functions"));
            counts.put("packages", result.get("packages"));
            counts.put("sequences", result.get("sequences"));
            counts.put("synonyms", result.get("synonyms"));
            counts.put("types", result.get("types"));
            counts.put("triggers", result.get("triggers"));
            counts.put("total", result.values().stream().mapToInt(v -> ((Number) v).intValue()).sum());

        } catch (Exception e) {
            log.error("Error in getAllObjectCounts: {}", e.getMessage(), e);
            // Initialize all counts to 0
            counts.put("tables", 0);
            counts.put("views", 0);
            counts.put("procedures", 0);
            counts.put("functions", 0);
            counts.put("packages", 0);
            counts.put("sequences", 0);
            counts.put("synonyms", 0);
            counts.put("types", 0);
            counts.put("triggers", 0);
            counts.put("total", 0);
        }
        return counts;
    }

    /**
     * Resolve synonym target type on demand (lazy loading)
     */
    public Map<String, Object> resolveSynonymTarget(String synonymName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT s.table_owner, s.table_name, s.db_link, " +
                    "o.object_type, o.status " +
                    "FROM user_synonyms s " +
                    "LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) = UPPER(?)";

            Map<String, Object> target = oracleJdbcTemplate.queryForMap(sql, synonymName);

            result.put("targetOwner", target.get("table_owner"));
            result.put("targetName", target.get("table_name"));
            result.put("targetType", target.get("object_type"));
            result.put("targetStatus", target.get("status"));
            result.put("dbLink", target.get("db_link"));
            result.put("isRemote", target.get("db_link") != null);

        } catch (Exception e) {
            log.error("Error resolving synonym target {}: {}", synonymName, e.getMessage());
            result.put("error", e.getMessage());
        }
        return result;
    }



    // ==================== DIAGNOSTIC METHODS ====================

    /**
     * Get current schema name
     */
    public String getCurrentSchema() {
        try {
            String schema = oracleJdbcTemplate.queryForObject(
                    "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL",
                    String.class
            );
            log.info("Current schema: {}", schema);
            return schema;
        } catch (Exception e) {
            log.warn("Failed to get CURRENT_SCHEMA, falling back to USER: {}", e.getMessage());
            try {
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
     * Comprehensive diagnostic method
     */
    public Map<String, Object> diagnoseDatabase() {
        Map<String, Object> diagnostics = new HashMap<>();
        List<String> issues = new ArrayList<>();

        try {
            String currentUser = getCurrentUser();
            String currentSchema = getCurrentSchema();
            diagnostics.put("currentUser", currentUser);
            diagnostics.put("currentSchema", currentSchema);
            diagnostics.put("connectionStatus", "SUCCESS");

            Integer userTableCount = getUserTableCount();
            diagnostics.put("userTableCount", userTableCount);

            if (userTableCount == 0) {
                issues.add("User " + currentUser + " has no tables in their schema");
            }

            diagnostics.put("objectCounts", getObjectCountByType());

            List<Map<String, Object>> invalidObjects = getInvalidObjects();
            diagnostics.put("invalidObjectCount", invalidObjects.size());
            if (!invalidObjects.isEmpty()) {
                issues.add("Found " + invalidObjects.size() + " invalid objects");
                diagnostics.put("invalidObjects", invalidObjects);
            }

            String dbVersion = getDatabaseVersion();
            diagnostics.put("databaseVersion", dbVersion);

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

    // ==================== HELPER METHODS ====================

    /**
     * Get current Oracle user
     */
    public String getCurrentUser() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT USER FROM DUAL", String.class);
        } catch (Exception e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Get user table count
     */
    private Integer getUserTableCount() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_tables", Integer.class);
        } catch (Exception e) {
            log.error("Failed to get user table count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get database version
     */
    public String getDatabaseVersion() {
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
     * Get table size
     */
    private Long getTableSize(String tableName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT SUM(bytes) FROM user_segments WHERE segment_name = UPPER(?) AND segment_type = 'TABLE'",
                    Long.class,
                    tableName
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get table comment
     */
    private String getTableComment(String tableName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT comments FROM user_tab_comments WHERE table_name = UPPER(?)",
                    String.class,
                    tableName
            );
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get package spec status
     */
    private String getPackageSpecStatus(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT status FROM user_objects WHERE object_name = UPPER(?) AND object_type = 'PACKAGE'",
                    String.class,
                    packageName
            );
        } catch (Exception e) {
            return "NOT_FOUND";
        }
    }

    /**
     * Get package body status
     */
    private String getPackageBodyStatus(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT status FROM user_objects WHERE object_name = UPPER(?) AND object_type = 'PACKAGE BODY'",
                    String.class,
                    packageName
            );
        } catch (Exception e) {
            return "NOT_FOUND";
        }
    }

    /**
     * Get package created date
     */
    private String getPackageCreated(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT MIN(created) FROM user_objects WHERE object_name = UPPER(?) AND object_type IN ('PACKAGE', 'PACKAGE BODY')",
                    String.class,
                    packageName
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get package last modified
     */
    private String getPackageLastModified(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT MAX(last_ddl_time) FROM user_objects WHERE object_name = UPPER(?) AND object_type IN ('PACKAGE', 'PACKAGE BODY')",
                    String.class,
                    packageName
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Find table location
     */
    private Map<String, Object> findTableLocation(String tableName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT owner, table_name, status FROM all_tables WHERE UPPER(table_name) = UPPER(?) AND ROWNUM = 1";
            return oracleJdbcTemplate.queryForMap(sql, tableName);
        } catch (Exception e) {
            log.debug("Table {} not found in all_tables", tableName);
            return result;
        }
    }

    /**
     * Find object location
     */
    private Map<String, Object> findObjectLocation(String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT owner, object_name, status FROM all_objects " +
                    "WHERE UPPER(object_name) = UPPER(?) AND object_type = ? AND ROWNUM = 1";
            return oracleJdbcTemplate.queryForMap(sql, objectName, objectType);
        } catch (Exception e) {
            log.debug("Object {} of type {} not found", objectName, objectType);
            return result;
        }
    }

    /**
     * Get object type icon
     */
    private String getObjectTypeIcon(String objectType) {
        if (objectType == null) return "default";
        switch (objectType.toUpperCase()) {
            case "TABLE": return "table";
            case "VIEW": return "view";
            case "PROCEDURE": return "procedure";
            case "FUNCTION": return "function";
            case "PACKAGE": return "package";
            case "PACKAGE BODY": return "package-body";
            case "SEQUENCE": return "sequence";
            case "SYNONYM": return "synonym";
            case "TRIGGER": return "trigger";
            case "INDEX": return "index";
            case "TYPE": return "type";
            case "TYPE BODY": return "type-body";
            case "MATERIALIZED VIEW": return "materialized-view";
            case "DATABASE LINK": return "database-link";
            case "JAVA CLASS":
            case "JAVA SOURCE":
            case "JAVA RESOURCE": return "java";
            default: return "default";
        }
    }

    /**
     * Format object type for display
     */
    private String formatObjectTypeForDisplay(String objectType) {
        if (objectType == null) return "Unknown";
        String formatted = objectType.replace("_", " ").toLowerCase();
        StringBuilder result = new StringBuilder();
        for (String word : formatted.split(" ")) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Transform columns for frontend
     */
    private List<Map<String, Object>> transformColumnsForFrontend(List<Map<String, Object>> columns) {
        return columns.stream().map(col -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", col.get("column_name"));
            transformed.put("type", col.get("data_type"));
            transformed.put("nullable", col.get("nullable"));
            transformed.put("position", col.get("column_id"));
            transformed.put("dataLength", col.get("data_length"));
            transformed.put("dataPrecision", col.get("data_precision"));
            transformed.put("dataScale", col.get("data_scale"));
            transformed.put("defaultValue", col.get("data_default"));
            transformed.put("distinctCount", col.get("num_distinct"));
            transformed.put("nullCount", col.get("num_nulls"));
            return transformed;
        }).collect(Collectors.toList());
    }

    /**
     * Transform constraints for frontend
     */
    private List<Map<String, Object>> transformConstraintsForFrontend(List<Map<String, Object>> constraints) {
        return constraints.stream().map(con -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", con.get("constraint_name"));
            transformed.put("type", formatConstraintType((String) con.get("constraint_type")));
            transformed.put("typeCode", con.get("constraint_type"));
            transformed.put("columns", con.get("columns"));
            transformed.put("columnCount", con.get("column_count"));
            transformed.put("status", con.get("constraint_status"));
            transformed.put("refTable", con.get("references_owner") != null ?
                    con.get("references_owner") + "." + con.get("references_constraint") : null);
            transformed.put("deleteRule", con.get("delete_rule"));
            transformed.put("deferrable", con.get("deferrable"));
            transformed.put("deferred", con.get("deferred"));
            transformed.put("validated", con.get("validated"));
            return transformed;
        }).collect(Collectors.toList());
    }

    /**
     * Transform indexes for frontend
     */
    private List<Map<String, Object>> transformIndexesForFrontend(List<Map<String, Object>> indexes) {
        return indexes.stream().map(idx -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", idx.get("index_name"));
            transformed.put("type", idx.get("index_type"));
            transformed.put("uniqueness", idx.get("uniqueness"));
            transformed.put("columns", idx.get("columns"));
            transformed.put("columnCount", idx.get("column_count"));
            transformed.put("status", idx.get("index_status"));
            transformed.put("visibility", idx.get("visibility"));
            transformed.put("tablespace", idx.get("tablespace_name"));
            transformed.put("distinctKeys", idx.get("distinct_keys"));
            transformed.put("leafBlocks", idx.get("leaf_blocks"));
            transformed.put("clusteringFactor", idx.get("clustering_factor"));
            transformed.put("size", idx.get("size_bytes") != null ?
                    formatBytes(getLongValue(idx.get("size_bytes"))) : null);
            return transformed;
        }).collect(Collectors.toList());
    }

    /**
     * Format constraint type
     */
    private String formatConstraintType(String type) {
        if (type == null) return "";
        switch (type) {
            case "P": return "PRIMARY KEY";
            case "R": return "FOREIGN KEY";
            case "U": return "UNIQUE";
            case "C": return "CHECK";
            case "V": return "VIEW CHECK OPTION";
            case "O": return "VIEW READ ONLY";
            default: return type;
        }
    }

    /**
     * Format bytes to human readable string
     */
    private String formatBytes(long bytes) {
        if (bytes == 0) return "0 Bytes";
        String[] sizes = {"Bytes", "KB", "MB", "GB", "TB"};
        int i = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, i), sizes[i]);
    }

    /**
     * Safely get long value from object
     */
    private long getLongValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}