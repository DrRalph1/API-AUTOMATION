package com.usg.apiGeneration.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class PostgreSQLPackageRepository extends PostgreSQLRepository {

    // ============================================================
    // PACKAGE METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllPackagesForFrontend() {
        // PostgreSQL doesn't have packages as a separate object type
        // Instead, we use schemas for organization
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // Return schemas as package-like objects for frontend compatibility
            String schemaSql = "SELECT " +
                    "    nspname as name, " +
                    "    'PACKAGE' as type, " +
                    "    'VALID' as status, " +
                    "    'VALID' as specStatus, " +
                    "    'VALID' as bodyStatus, " +
                    "    NULL as created, " +
                    "    NULL as lastModified " +
                    "FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%' " +
                    "ORDER BY nspname";

            List<Map<String, Object>> schemas = getJdbcTemplate().queryForList(schemaSql);

            for (Map<String, Object> schema : schemas) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "package-" + System.currentTimeMillis() + "-" + schema.get("name"));
                transformed.put("name", schema.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "PACKAGE");
                transformed.put("isSynonym", false);
                transformed.put("status", schema.get("status"));
                transformed.put("specStatus", schema.get("specStatus"));
                transformed.put("bodyStatus", schema.get("bodyStatus"));
                transformed.put("created", schema.get("created"));
                transformed.put("lastModified", schema.get("lastModified"));
                transformed.put("icon", "package");
                result.add(transformed);
            }

            // PostgreSQL doesn't have synonyms, so no synonym handling
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (schemas as packages)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllPackagesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    public Map<String, Object> getAllPackagesForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            // Count schemas
            String countSql = "SELECT COUNT(*) FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            // Get paginated schemas
            String dataSql = "SELECT " +
                    "    nspname as name, " +
                    "    'PACKAGE' as type, " +
                    "    'VALID' as status, " +
                    "    'VALID' as specStatus, " +
                    "    'VALID' as bodyStatus, " +
                    "    NULL as created, " +
                    "    NULL as lastModified " +
                    "FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%' " +
                    "ORDER BY nspname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> schemas = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> transformed = new ArrayList<>();
            for (Map<String, Object> schema : schemas) {
                Map<String, Object> transformedItem = new HashMap<>();
                transformedItem.put("id", "package-" + System.currentTimeMillis() + "-" + schema.get("name"));
                transformedItem.put("name", schema.get("name"));
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "PACKAGE");
                transformedItem.put("status", schema.get("status"));
                transformedItem.put("specStatus", schema.get("specStatus"));
                transformedItem.put("bodyStatus", schema.get("bodyStatus"));
                transformedItem.put("created", schema.get("created"));
                transformedItem.put("lastModified", schema.get("lastModified"));
                transformedItem.put("icon", "package");
                transformedItem.put("isSynonym", false);
                transformed.add(transformedItem);
            }

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getAllPackagesForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }

    public Map<String, Object> getPackageDetails(String owner, String packageName) {
        Map<String, Object> details = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            // In PostgreSQL, schemas contain objects instead of packages
            details.put("package_name", packageName);
            details.put("owner", owner);
            details.put("is_schema", true);

            // Get objects in this schema (like package items)
            Map<String, Object> items = getSchemaItems(packageName);
            details.putAll(items);

            // Get schema information
            String schemaSql = "SELECT " +
                    "    nspname, " +
                    "    nspowner " +
                    "FROM pg_namespace " +
                    "WHERE nspname = ?";

            try {
                Map<String, Object> schemaInfo = getJdbcTemplate().queryForMap(schemaSql, packageName);
                details.put("specification", schemaInfo);
                details.put("body", null); // No separate body in PostgreSQL
                details.put("specSource", null);
                details.put("bodySource", null);
            } catch (EmptyResultDataAccessException e) {
                details.put("specification", null);
            }

        } catch (Exception e) {
            log.warn("Error getting package details for {}.{}: {}", owner, packageName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    public Map<String, Object> getPackageItemsPaginated(String packageName, String owner,
                                                        String itemType, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            int offset = (page - 1) * pageSize;
            String upperItemType = itemType.toUpperCase();

            // In PostgreSQL, package items are functions/procedures in a schema
            String countSql;
            String dataSql;

            if ("PROCEDURE".equals(upperItemType)) {
                countSql = "SELECT COUNT(*) FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'p'";

                dataSql = "SELECT " +
                        "    p.proname as procedure_name, " +
                        "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'p' " +
                        "ORDER BY p.proname " +
                        "OFFSET ? LIMIT ?";

            } else if ("FUNCTION".equals(upperItemType)) {
                countSql = "SELECT COUNT(*) FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'f'";

                dataSql = "SELECT " +
                        "    p.proname as procedure_name, " +
                        "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                        "    pg_get_function_result(p.oid) as return_type " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'f' " +
                        "ORDER BY p.proname " +
                        "OFFSET ? LIMIT ?";

            } else {
                // ALL types - both functions and procedures
                countSql = "SELECT COUNT(*) FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ?";

                dataSql = "SELECT " +
                        "    p.proname as procedure_name, " +
                        "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                        "    CASE WHEN p.prokind = 'f' THEN pg_get_function_result(p.oid) ELSE NULL END as return_type, " +
                        "    CASE WHEN p.prokind = 'f' THEN 'FUNCTION' ELSE 'PROCEDURE' END as item_type " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? " +
                        "ORDER BY p.proname " +
                        "OFFSET ? LIMIT ?";
            }

            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class, packageName);
            List<Map<String, Object>> items = getJdbcTemplate().queryForList(
                    dataSql, packageName, offset, pageSize);

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

    public Map<String, Object> getPackagesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    nspname as package_name " +
                    "FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%' " +
                    "ORDER BY nspname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> packages = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

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

    public List<Map<String, Object>> getPackagesBySchema(String schemaName) {
        try {
            // Return schemas themselves as packages
            String sql = "SELECT " +
                    "    nspname as owner, " +
                    "    nspname as package_name, " +
                    "    'PACKAGE' as object_type, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_namespace " +
                    "WHERE nspname = ?";

            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getPackagesBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private Map<String, Object> getSchemaItems(String schemaName) {
        Map<String, Object> details = new HashMap<>();

        try {
            // Get functions in the schema
            String funcSql = "SELECT " +
                    "    p.proname as procedure_name, " +
                    "    pg_get_function_result(p.oid) as return_type, " +
                    "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.prokind = 'f' " +
                    "ORDER BY p.proname";

            List<Map<String, Object>> functions = getJdbcTemplate().queryForList(funcSql, schemaName);

            // Get procedures in the schema
            String procSql = "SELECT " +
                    "    p.proname as procedure_name, " +
                    "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.prokind = 'p' " +
                    "ORDER BY p.proname";

            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procSql, schemaName);

            details.put("procedures", procedures);
            details.put("functions", functions);
            details.put("procedureCount", procedures.size());
            details.put("functionCount", functions.size());

            // Get tables in the schema (similar to package variables)
            String tableSql = "SELECT " +
                    "    c.relname as variable_name, " +
                    "    'TABLE' as type " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relkind = 'r' " +
                    "ORDER BY c.relname";

            List<Map<String, Object>> tables = getJdbcTemplate().queryForList(tableSql, schemaName);
            details.put("variables", tables);
            details.put("variableCount", tables.size());

        } catch (Exception e) {
            log.warn("Error getting schema items: {}", e.getMessage());
            details.put("procedures", new ArrayList<>());
            details.put("functions", new ArrayList<>());
            details.put("variables", new ArrayList<>());
            details.put("procedureCount", 0);
            details.put("functionCount", 0);
            details.put("variableCount", 0);
        }

        return details;
    }

    public List<Map<String, Object>> getAllPackages() {
        try {
            String sql = "SELECT " +
                    "    nspname as package_name, " +
                    "    'PACKAGE' as object_type, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%' " +
                    "ORDER BY nspname";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllPackages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve packages: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getPackageDetails(String packageName) {
        try {
            // Check if the schema exists
            String checkSql = "SELECT COUNT(*) FROM pg_namespace WHERE nspname = ?";
            int exists = getJdbcTemplate().queryForObject(checkSql, Integer.class, packageName);

            if (exists > 0) {
                return getPackageDetails(packageName, packageName);
            } else {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("package_name", packageName);
                emptyResult.put("message", "Package (schema) not found");
                emptyResult.put("status", "NOT_FOUND");
                return emptyResult;
            }

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

    // Helper method to get schema source (for compatibility with package source)
    public String getSchemaSource(String schemaName) {
        try {
            // Generate CREATE SCHEMA statement
            String sql = "SELECT 'CREATE SCHEMA ' || nspname || ';' as source " +
                    "FROM pg_namespace WHERE nspname = ?";
            return getJdbcTemplate().queryForObject(sql, String.class, schemaName);
        } catch (Exception e) {
            log.debug("Could not get schema source: {}", e.getMessage());
            return null;
        }
    }
}