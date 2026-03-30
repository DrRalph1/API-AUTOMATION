package com.usg.apiGeneration.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Repository("PostgreSQLRepository")
@Primary  // Add this annotation
public class PostgreSQLRepository {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    // ============================================================
    // JDBC TEMPLATE ACCESS
    // ============================================================

    public JdbcTemplate getJdbcTemplate() {
        return postgresqlJdbcTemplate;
    }

    // ============================================================
    // BASE SCHEMA INFO METHODS
    // ============================================================

    public String getCurrentUser() {
        try {
            return postgresqlJdbcTemplate.queryForObject("SELECT current_user", String.class);
        } catch (Exception e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    public String getCurrentSchema() {
        try {
            return postgresqlJdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        } catch (Exception e) {
            log.warn("Failed to get current_schema, falling back to public: {}", e.getMessage());
            return "public";
        }
    }

    public String getDatabaseVersion() {
        try {
            return postgresqlJdbcTemplate.queryForObject("SELECT version()", String.class);
        } catch (Exception e) {
            log.error("Failed to get database version: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    public Map<String, Object> getCurrentSchemaInfo() {
        Map<String, Object> schemaInfo = new HashMap<>();
        try {
            schemaInfo.put("currentUser", getCurrentUser());
            schemaInfo.put("currentSchema", getCurrentSchema());
            schemaInfo.put("databaseVersion", getDatabaseVersion());
            schemaInfo.put("objectCounts", getAllObjectCounts());
            schemaInfo.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("Error getting current schema info: {}", e.getMessage(), e);
            schemaInfo.put("error", e.getMessage());
        }
        return schemaInfo;
    }

    public Map<String, Object> getAllObjectCounts() {
        Map<String, Object> counts = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = current_schema() AND table_type = 'BASE TABLE') as tables, " +
                    "(SELECT COUNT(*) FROM information_schema.views WHERE table_schema = current_schema()) as views, " +
                    "(SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = current_schema() AND p.prokind = 'p') as procedures, " +
                    "(SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = current_schema() AND p.prokind = 'f') as functions, " +
                    "(SELECT COUNT(*) FROM pg_class c JOIN pg_namespace n ON c.relnamespace = n.oid WHERE n.nspname = current_schema() AND c.relkind = 'S') as sequences, " +
                    "(SELECT COUNT(*) FROM pg_type t JOIN pg_namespace n ON t.typnamespace = n.oid WHERE n.nspname = current_schema() AND t.typtype = 'c') as types, " +
                    "(SELECT COUNT(*) FROM pg_trigger t JOIN pg_class c ON t.tgrelid = c.oid JOIN pg_namespace n ON c.relnamespace = n.oid WHERE n.nspname = current_schema()) as triggers, " +
                    "(SELECT COUNT(*) FROM pg_index i JOIN pg_class c ON i.indexrelid = c.oid JOIN pg_namespace n ON c.relnamespace = n.oid WHERE n.nspname = current_schema()) as indexes";

            Map<String, Object> result = postgresqlJdbcTemplate.queryForMap(sql);
            result.forEach((key, value) -> counts.put(key, value != null ? value : 0));

            long total = 0;
            for (Object value : result.values()) {
                if (value instanceof Number) {
                    total += ((Number) value).longValue();
                }
            }
            counts.put("total", total);

        } catch (Exception e) {
            log.error("Error in getAllObjectCounts: {}", e.getMessage(), e);
            counts.put("tables", 0);
            counts.put("views", 0);
            counts.put("procedures", 0);
            counts.put("functions", 0);
            counts.put("sequences", 0);
            counts.put("types", 0);
            counts.put("triggers", 0);
            counts.put("indexes", 0);
            counts.put("total", 0);
        }
        return counts;
    }

    // ============================================================
    // SCHEMA METHODS (NEW - for package equivalents)
    // ============================================================

    /**
     * Get all schemas in the database (excluding system schemas)
     * This serves as the PostgreSQL equivalent of Oracle packages
     */
    public List<Map<String, Object>> getAllSchemas() {
        List<Map<String, Object>> schemas = new ArrayList<>();
        try {
            String sql = "SELECT " +
                    "    nspname as name, " +
                    "    'SCHEMA' as type, " +
                    "    'VALID' as status, " +
                    "    nspowner as owner, " +
                    "    NULL as created, " +
                    "    NULL as lastModified " +
                    "FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%' " +
                    "ORDER BY nspname";

            schemas = postgresqlJdbcTemplate.queryForList(sql);

            // Add icons for frontend
            for (Map<String, Object> schema : schemas) {
                schema.put("icon", "schema");
                schema.put("objectCount", getObjectCountInSchema((String) schema.get("name")));
            }

            log.debug("Retrieved {} schemas", schemas.size());
        } catch (Exception e) {
            log.error("Error getting all schemas: {}", e.getMessage(), e);
        }
        return schemas;
    }

    /**
     * Get object count in a specific schema
     */
    public int getObjectCountInSchema(String schemaName) {
        try {
            String sql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? " +
                    "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i')";
            return postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schemaName);
        } catch (Exception e) {
            log.debug("Error getting object count for schema {}: {}", schemaName, e.getMessage());
            return 0;
        }
    }

    /**
     * Get schema details
     */
    public Map<String, Object> getSchemaDetails(String schemaName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    nspname as name, " +
                    "    nspowner as owner, " +
                    "    (SELECT usename FROM pg_user WHERE usesysid = nspowner) as owner_name, " +
                    "    (SELECT COUNT(*) FROM pg_class c WHERE c.relnamespace = n.oid) as object_count " +
                    "FROM pg_namespace n " +
                    "WHERE nspname = ?";
            details = postgresqlJdbcTemplate.queryForMap(sql, schemaName);
            details.put("icon", "schema");
            details.put("type", "SCHEMA");
            details.put("status", "VALID");
        } catch (Exception e) {
            log.error("Error getting schema details for {}: {}", schemaName, e.getMessage());
            details.put("error", e.getMessage());
        }
        return details;
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    public String formatBytes(long bytes) {
        if (bytes == 0) return "0 Bytes";
        String[] sizes = {"Bytes", "KB", "MB", "GB", "TB"};
        int i = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, i), sizes[i]);
    }

    public long getLongValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getObjectTypeIcon(String objectType) {
        if (objectType == null) return "default";
        switch (objectType.toUpperCase()) {
            case "TABLE": return "table";
            case "VIEW": return "view";
            case "MATERIALIZED VIEW": return "materialized-view";
            case "PROCEDURE": return "procedure";
            case "FUNCTION": return "function";
            case "SEQUENCE": return "sequence";
            case "TRIGGER": return "trigger";
            case "INDEX": return "index";
            case "TYPE": return "type";
            case "SCHEMA": return "schema";
            case "DOMAIN": return "domain";
            case "FOREIGN TABLE": return "foreign-table";
            default: return "default";
        }
    }

    public String formatObjectTypeForDisplay(String objectType) {
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

    public boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) return false;
        return identifier.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

    public String sanitizeFilter(String filter) {
        if (filter == null || filter.isEmpty()) return "";
        return filter.replaceAll(";", "")
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
    }

    // ============================================================
    // OBJECT LOCATION METHODS
    // ============================================================

    public Map<String, Object> findObjectLocation(String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();
        try {
            String relKind = getRelationKind(objectType);
            String sql;

            if (relKind != null) {
                sql = "SELECT n.nspname as owner, c.relname as object_name, " +
                        "CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                        "WHEN c.relkind = 'v' THEN 'VIEW' " +
                        "WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                        "WHEN c.relkind = 'i' THEN 'INDEX' " +
                        "WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                        "WHEN c.relkind = 'f' THEN 'FOREIGN TABLE' END as status " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "AND c.relname = ? AND c.relkind = ? " +
                        "LIMIT 1";
                return postgresqlJdbcTemplate.queryForMap(sql, objectName.toLowerCase(), relKind);
            } else if ("FUNCTION".equalsIgnoreCase(objectType) || "PROCEDURE".equalsIgnoreCase(objectType)) {
                sql = "SELECT n.nspname as owner, p.proname as object_name, " +
                        "CASE WHEN p.prokind = 'f' THEN 'FUNCTION' ELSE 'PROCEDURE' END as status " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "AND p.proname = ? " +
                        "LIMIT 1";
                return postgresqlJdbcTemplate.queryForMap(sql, objectName.toLowerCase());
            } else if ("TRIGGER".equalsIgnoreCase(objectType)) {
                sql = "SELECT n.nspname as owner, t.tgname as object_name, 'TRIGGER' as status " +
                        "FROM pg_trigger t " +
                        "JOIN pg_class c ON t.tgrelid = c.oid " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "AND t.tgname = ? " +
                        "LIMIT 1";
                return postgresqlJdbcTemplate.queryForMap(sql, objectName.toLowerCase());
            } else if ("TYPE".equalsIgnoreCase(objectType)) {
                sql = "SELECT n.nspname as owner, t.typname as object_name, 'TYPE' as status " +
                        "FROM pg_type t " +
                        "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                        "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "AND t.typname = ? AND t.typtype = 'c' " +
                        "LIMIT 1";
                return postgresqlJdbcTemplate.queryForMap(sql, objectName.toLowerCase());
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.debug("Object {} of type {} not found", objectName, objectType);
            return result;
        } catch (Exception e) {
            log.debug("Error finding object location: {}", e.getMessage());
            return result;
        }
    }

    public Map<String, Object> findTableLocation(String tableName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT table_schema as owner, table_name, 'TABLE' as status " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema NOT IN ('pg_catalog', 'information_schema') " +
                    "AND table_name = ? AND table_type = 'BASE TABLE' " +
                    "LIMIT 1";
            return postgresqlJdbcTemplate.queryForMap(sql, tableName.toLowerCase());
        } catch (Exception e) {
            log.debug("Table {} not found", tableName);
            return result;
        }
    }

    // ============================================================
    // SYNONYM RESOLUTION METHODS (PostgreSQL uses views instead of synonyms)
    // ============================================================

    public Map<String, Object> checkIfSynonymAndGetTarget(String objectName, String expectedTargetType) {
        Map<String, Object> result = new HashMap<>();
        result.put("isSynonym", false);
        try {
            // PostgreSQL doesn't have synonyms, but we can check for views that might act as aliases
            String sql = "SELECT table_name as synonym_name, table_schema as target_owner, " +
                    "table_name as target_name, 'VIEW' as target_type, 'VALID' as target_status, " +
                    "NULL as db_link, false as is_remote " +
                    "FROM information_schema.views " +
                    "WHERE table_schema = current_schema() AND table_name = ?";

            try {
                Map<String, Object> view = postgresqlJdbcTemplate.queryForMap(sql, objectName.toLowerCase());
                if (view != null && !view.isEmpty()) {
                    result.put("isSynonym", true);
                    result.put("synonymName", view.get("synonym_name"));
                    result.put("targetOwner", view.get("target_owner"));
                    result.put("targetName", view.get("target_name"));
                    result.put("targetType", view.get("target_type"));
                    result.put("targetStatus", view.get("target_status"));
                    result.put("dbLink", view.get("db_link"));
                    result.put("isRemote", false);
                }
            } catch (EmptyResultDataAccessException e) {
                // Not a view, ignore
            }
        } catch (Exception e) {
            log.debug("Error checking if {} is a view: {}", objectName, e.getMessage());
        }
        return result;
    }

    public Map<String, Object> resolveSynonymIfNeeded(String objectName, String owner) {
        // PostgreSQL doesn't have synonyms, return null
        return null;
    }

    // ============================================================
    // SOURCE CODE METHODS
    // ============================================================

    public String removeComments(String source) {
        if (source == null) return null;
        StringBuilder result = new StringBuilder();
        String[] lines = source.split("\\n");
        for (String line : lines) {
            boolean inQuotes = false;
            int commentStart = -1;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\'') {
                    inQuotes = !inQuotes;
                } else if (!inQuotes && c == '-' && i + 1 < line.length() && line.charAt(i + 1) == '-') {
                    commentStart = i;
                    break;
                }
            }
            if (commentStart != -1) {
                result.append(line.substring(0, commentStart));
            } else {
                result.append(line);
            }
            result.append("\n");
        }
        return result.toString();
    }

    public List<String> splitParametersByComma(String text) {
        List<String> parameters = new ArrayList<>();
        if (text == null || text.isEmpty()) return parameters;

        StringBuilder currentParam = new StringBuilder();
        int parenCount = 0;
        boolean inQuotes = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\'') {
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                if (c == '(') {
                    parenCount++;
                } else if (c == ')') {
                    parenCount--;
                } else if (c == ',' && parenCount == 0) {
                    String param = currentParam.toString().trim();
                    if (!param.isEmpty()) {
                        parameters.add(param);
                    }
                    currentParam = new StringBuilder();
                    continue;
                }
            }

            currentParam.append(c);
        }

        String lastParam = currentParam.toString().trim();
        if (!lastParam.isEmpty()) {
            parameters.add(lastParam);
        }

        return parameters;
    }

    public List<String> splitParameterDeclarations(String paramsSection) {
        return splitParametersByComma(paramsSection);
    }

    // ============================================================
    // DATA TYPE EXTRACTION METHODS
    // ============================================================

    public Integer extractDataTypeLength(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    public Integer extractDataTypePrecision(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*\\d+\\)");
        java.util.regex.Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    public Integer extractDataTypeScale(String dataType) {
        if (dataType == null) return null;
        Pattern pattern = Pattern.compile("\\(\\d+,\\s*(\\d+)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    // ============================================================
    // PACKAGE HELPER METHODS (PostgreSQL uses schemas instead of packages)
    // ============================================================

    public String getPackageSpecStatus(String packageName) {
        // PostgreSQL doesn't have packages, check for schema
        try {
            return postgresqlJdbcTemplate.queryForObject(
                    "SELECT 'VALID' as status WHERE EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = ?)",
                    String.class, packageName.toLowerCase());
        } catch (Exception e) {
            return "NOT_FOUND";
        }
    }

    public String getPackageBodyStatus(String packageName) {
        // PostgreSQL doesn't have package bodies
        return "NOT_APPLICABLE";
    }

    public String getPackageCreated(String packageName) {
        try {
            return postgresqlJdbcTemplate.queryForObject(
                    "SELECT created FROM pg_namespace n " +
                            "JOIN pg_class c ON c.relnamespace = n.oid " +
                            "WHERE n.nspname = ? LIMIT 1",
                    String.class, packageName.toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }

    public String getPackageLastModified(String packageName) {
        try {
            return postgresqlJdbcTemplate.queryForObject(
                    "SELECT MAX(last_ddl_time) FROM (" +
                            "SELECT pg_stat_get_last_analyze_time(c.oid) as last_ddl_time " +
                            "FROM pg_class c " +
                            "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                            "WHERE n.nspname = ?) t",
                    String.class, packageName.toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }

    // ============================================================
    // TRANSFORMATION METHODS
    // ============================================================

    public List<Map<String, Object>> transformColumnsForFrontend(List<Map<String, Object>> columns) {
        return columns.stream().map(col -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", col.get("column_name"));
            transformed.put("type", col.get("data_type"));
            transformed.put("nullable", "YES".equals(col.get("is_nullable")) ? "Y" : "N");
            transformed.put("position", col.get("ordinal_position"));
            transformed.put("dataLength", col.get("character_maximum_length"));
            transformed.put("dataPrecision", col.get("numeric_precision"));
            transformed.put("dataScale", col.get("numeric_scale"));
            transformed.put("defaultValue", col.get("column_default"));
            transformed.put("distinctCount", col.get("distinct_count"));
            transformed.put("nullCount", col.get("null_count"));
            return transformed;
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> transformConstraintsForFrontend(List<Map<String, Object>> constraints) {
        return constraints.stream().map(con -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", con.get("constraint_name"));
            transformed.put("type", formatConstraintType((String) con.get("constraint_type")));
            transformed.put("typeCode", con.get("constraint_type"));
            transformed.put("columns", con.get("columns"));
            transformed.put("columnCount", con.get("column_count"));
            transformed.put("status", con.get("constraint_status"));
            transformed.put("refTable", con.get("references_table"));
            transformed.put("deleteRule", con.get("delete_rule"));
            transformed.put("deferrable", con.get("is_deferrable"));
            transformed.put("deferred", con.get("is_deferred"));
            transformed.put("validated", con.get("is_validated"));
            return transformed;
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> transformIndexesForFrontend(List<Map<String, Object>> indexes) {
        return indexes.stream().map(idx -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("name", idx.get("index_name"));
            transformed.put("type", idx.get("index_type"));
            transformed.put("uniqueness", idx.get("is_unique"));
            transformed.put("columns", idx.get("columns"));
            transformed.put("columnCount", idx.get("column_count"));
            transformed.put("status", idx.get("index_status"));
            transformed.put("visibility", idx.get("is_visible"));
            transformed.put("tablespace", idx.get("tablespace_name"));
            transformed.put("distinctKeys", idx.get("distinct_keys"));
            transformed.put("leafBlocks", idx.get("leaf_blocks"));
            transformed.put("clusteringFactor", idx.get("clustering_factor"));
            transformed.put("size", idx.get("size_bytes") != null ?
                    formatBytes(getLongValue(idx.get("size_bytes"))) : null);
            return transformed;
        }).collect(java.util.stream.Collectors.toList());
    }

    public String formatConstraintType(String type) {
        if (type == null) return "";
        switch (type) {
            case "PRIMARY KEY": return "PRIMARY KEY";
            case "FOREIGN KEY": return "FOREIGN KEY";
            case "UNIQUE": return "UNIQUE";
            case "CHECK": return "CHECK";
            case "EXCLUDE": return "EXCLUDE";
            default: return type;
        }
    }

    // ============================================================
    // DIAGNOSTIC METHODS
    // ============================================================

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

            diagnostics.put("objectCounts", getAllObjectCounts());

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

    public List<Map<String, Object>> getInvalidObjects() {
        try {
            // PostgreSQL doesn't have an "invalid" status like Oracle
            // We'll check for broken views and functions
            String sql = "SELECT " +
                    "c.relname as object_name, " +
                    "CASE WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "WHEN c.relkind = 'f' THEN 'FOREIGN TABLE' END as object_type, " +
                    "c.relname, " +
                    "c.relkind as status " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind IN ('v', 'm') " +
                    "AND NOT pg_is_other_temp_schema(n.oid)";
            return postgresqlJdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getInvalidObjects: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Integer getUserTableCount() {
        try {
            return postgresqlJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = current_schema() AND table_type = 'BASE TABLE'",
                    Integer.class);
        } catch (Exception e) {
            log.error("Failed to get user table count: {}", e.getMessage());
            return 0;
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private String getRelationKind(String objectType) {
        if (objectType == null) return null;
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "TABLE": return "r";
            case "VIEW": return "v";
            case "MATERIALIZED VIEW": return "m";
            case "INDEX": return "i";
            case "SEQUENCE": return "S";
            case "FOREIGN TABLE": return "f";
            default: return null;
        }
    }



    /**
     * Get all schemas with pagination
     */
    public Map<String, Object> getAllSchemasPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            // Count total schemas
            String countSql = "SELECT COUNT(*) FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%'";
            int totalCount = postgresqlJdbcTemplate.queryForObject(countSql, Integer.class);

            // Get paginated schemas
            String dataSql = "SELECT " +
                    "    nspname as name, " +
                    "    'SCHEMA' as type, " +
                    "    'VALID' as status, " +
                    "    nspowner as owner, " +
                    "    NULL as created, " +
                    "    NULL as lastModified " +
                    "FROM pg_namespace " +
                    "WHERE nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast') " +
                    "AND nspname NOT LIKE 'pg_temp_%' " +
                    "AND nspname NOT LIKE 'pg_toast_temp_%' " +
                    "ORDER BY nspname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> items = postgresqlJdbcTemplate.queryForList(dataSql, offset, pageSize);

            // Add icons and object counts
            for (Map<String, Object> schema : items) {
                schema.put("icon", "schema");
                schema.put("objectCount", getObjectCountInSchema((String) schema.get("name")));
            }

            result.put("items", items);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error getting paginated schemas: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }

    /**
     * Get schema items (functions, procedures, tables, views) paginated
     * This is the PostgreSQL equivalent of Oracle package items
     */
    public Map<String, Object> getSchemaItemsPaginated(String schemaName, String itemType, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            String upperItemType = itemType.toUpperCase();
            List<Map<String, Object>> items = new ArrayList<>();
            int totalCount = 0;

            if ("FUNCTION".equals(upperItemType) || "ALL".equals(upperItemType)) {
                // Get functions
                String funcCountSql = "SELECT COUNT(*) FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'f'";
                totalCount = postgresqlJdbcTemplate.queryForObject(funcCountSql, Integer.class, schemaName);

                String funcSql = "SELECT " +
                        "    p.proname as name, " +
                        "    'FUNCTION' as type, " +
                        "    pg_get_function_result(p.oid) as return_type, " +
                        "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                        "    'VALID' as status " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'f' " +
                        "ORDER BY p.proname " +
                        "OFFSET ? LIMIT ?";

                List<Map<String, Object>> functions = postgresqlJdbcTemplate.queryForList(funcSql, schemaName, offset, pageSize);
                items.addAll(functions);
            }

            if ("PROCEDURE".equals(upperItemType) || "ALL".equals(upperItemType)) {
                // Get procedures
                String procCountSql = "SELECT COUNT(*) FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'p'";
                int procCount = postgresqlJdbcTemplate.queryForObject(procCountSql, Integer.class, schemaName);

                if ("PROCEDURE".equals(upperItemType)) {
                    totalCount = procCount;
                } else {
                    totalCount += procCount;
                }

                String procSql = "SELECT " +
                        "    p.proname as name, " +
                        "    'PROCEDURE' as type, " +
                        "    NULL as return_type, " +
                        "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                        "    'VALID' as status " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.prokind = 'p' " +
                        "ORDER BY p.proname " +
                        "OFFSET ? LIMIT ?";

                List<Map<String, Object>> procedures = postgresqlJdbcTemplate.queryForList(procSql, schemaName, offset, pageSize);
                items.addAll(procedures);
            }

            if ("TABLE".equals(upperItemType) || "ALL".equals(upperItemType)) {
                // Get tables
                String tableCountSql = "SELECT COUNT(*) FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relkind = 'r'";
                int tableCount = postgresqlJdbcTemplate.queryForObject(tableCountSql, Integer.class, schemaName);

                if ("TABLE".equals(upperItemType)) {
                    totalCount = tableCount;
                } else {
                    totalCount += tableCount;
                }

                String tableSql = "SELECT " +
                        "    c.relname as name, " +
                        "    'TABLE' as type, " +
                        "    NULL as return_type, " +
                        "    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = c.relname) as parameter_count, " +
                        "    'VALID' as status " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relkind = 'r' " +
                        "ORDER BY c.relname " +
                        "OFFSET ? LIMIT ?";

                List<Map<String, Object>> tables = postgresqlJdbcTemplate.queryForList(tableSql, schemaName, schemaName, offset, pageSize);
                items.addAll(tables);
            }

            if ("VIEW".equals(upperItemType) || "ALL".equals(upperItemType)) {
                // Get views
                String viewCountSql = "SELECT COUNT(*) FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relkind = 'v'";
                int viewCount = postgresqlJdbcTemplate.queryForObject(viewCountSql, Integer.class, schemaName);

                if ("VIEW".equals(upperItemType)) {
                    totalCount = viewCount;
                } else {
                    totalCount += viewCount;
                }

                String viewSql = "SELECT " +
                        "    c.relname as name, " +
                        "    'VIEW' as type, " +
                        "    NULL as return_type, " +
                        "    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = c.relname) as parameter_count, " +
                        "    'VALID' as status " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relkind = 'v' " +
                        "ORDER BY c.relname " +
                        "OFFSET ? LIMIT ?";

                List<Map<String, Object>> views = postgresqlJdbcTemplate.queryForList(viewSql, schemaName, schemaName, offset, pageSize);
                items.addAll(views);
            }

            if ("MATERIALIZED VIEW".equals(upperItemType) || "ALL".equals(upperItemType)) {
                // Get materialized views
                String mvCountSql = "SELECT COUNT(*) FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relkind = 'm'";
                int mvCount = postgresqlJdbcTemplate.queryForObject(mvCountSql, Integer.class, schemaName);

                if ("MATERIALIZED VIEW".equals(upperItemType)) {
                    totalCount = mvCount;
                } else {
                    totalCount += mvCount;
                }

                String mvSql = "SELECT " +
                        "    c.relname as name, " +
                        "    'MATERIALIZED VIEW' as type, " +
                        "    NULL as return_type, " +
                        "    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = c.relname) as parameter_count, " +
                        "    'VALID' as status " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relkind = 'm' " +
                        "ORDER BY c.relname " +
                        "OFFSET ? LIMIT ?";

                List<Map<String, Object>> mvs = postgresqlJdbcTemplate.queryForList(mvSql, schemaName, schemaName, offset, pageSize);
                items.addAll(mvs);
            }

            // Sort items by name
            items.sort((a, b) -> {
                String nameA = (String) a.get("name");
                String nameB = (String) b.get("name");
                return nameA.compareTo(nameB);
            });

            result.put("items", items);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error getting schema items paginated for schema {}: {}", schemaName, e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }


}