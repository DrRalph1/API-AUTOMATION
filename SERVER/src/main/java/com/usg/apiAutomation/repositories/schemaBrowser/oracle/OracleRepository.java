package com.usg.apiAutomation.repositories.schemaBrowser.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class OracleRepository {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    // ============================================================
    // JDBC TEMPLATE ACCESS
    // ============================================================

    public JdbcTemplate getJdbcTemplate() {
        return oracleJdbcTemplate;
    }

    // ============================================================
    // BASE SCHEMA INFO METHODS
    // ============================================================

    public String getCurrentUser() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT USER FROM DUAL", String.class);
        } catch (Exception e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    public String getCurrentSchema() {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL",
                    String.class
            );
        } catch (Exception e) {
            log.warn("Failed to get CURRENT_SCHEMA, falling back to USER: {}", e.getMessage());
            return getCurrentUser();
        }
    }

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
            result.forEach((key, value) -> counts.put(key, value));
            counts.put("total", result.values().stream().mapToInt(v -> ((Number) v).intValue()).sum());

        } catch (Exception e) {
            log.error("Error in getAllObjectCounts: {}", e.getMessage(), e);
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
            case "JAVA CLASS": return "java";
            case "JAVA SOURCE": return "java";
            case "JAVA RESOURCE": return "java";
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
        return identifier.matches("^[a-zA-Z][a-zA-Z0-9_$#]*$");
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
            String sql = "SELECT owner, object_name, status FROM all_objects " +
                    "WHERE UPPER(object_name) = UPPER(?) AND object_type = ? AND ROWNUM = 1";
            return oracleJdbcTemplate.queryForMap(sql, objectName, objectType);
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
            String sql = "SELECT owner, table_name, status FROM all_tables WHERE UPPER(table_name) = UPPER(?) AND ROWNUM = 1";
            return oracleJdbcTemplate.queryForMap(sql, tableName);
        } catch (Exception e) {
            log.debug("Table {} not found in all_tables", tableName);
            return result;
        }
    }

    // ============================================================
    // SYNONYM RESOLUTION METHODS
    // ============================================================

    public Map<String, Object> checkIfSynonymAndGetTarget(String objectName, String expectedTargetType) {
        Map<String, Object> result = new HashMap<>();
        result.put("isSynonym", false);
        try {
            String sql = "SELECT s.synonym_name, s.table_owner as target_owner, s.table_name as target_name, " +
                    "s.db_link, o.object_type as target_type, o.status as target_status " +
                    "FROM user_synonyms s LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) = UPPER(?)";

            Map<String, Object> synonym = oracleJdbcTemplate.queryForMap(sql, objectName);
            if (synonym != null && !synonym.isEmpty()) {
                result.put("isSynonym", true);
                result.put("synonymName", synonym.get("synonym_name"));
                result.put("targetOwner", synonym.get("target_owner"));
                result.put("targetName", synonym.get("target_name"));
                result.put("targetType", synonym.get("target_type"));
                result.put("targetStatus", synonym.get("target_status"));
                result.put("dbLink", synonym.get("db_link"));
                result.put("isRemote", synonym.get("db_link") != null);
            }
        } catch (EmptyResultDataAccessException e) {
            // Not a synonym, ignore
        } catch (Exception e) {
            log.debug("Error checking if {} is a synonym: {}", objectName, e.getMessage());
        }
        return result;
    }

    public Map<String, Object> resolveSynonymIfNeeded(String objectName, String owner) {
        try {
            String sql = "SELECT synonym_name, table_owner as target_owner, " +
                    "table_name as target_name, db_link " +
                    "FROM all_synonyms WHERE UPPER(synonym_name) = UPPER(?) AND UPPER(owner) = UPPER(?)";
            return oracleJdbcTemplate.queryForMap(sql, objectName, owner);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            log.debug("Error checking if {} is a synonym: {}", objectName, e.getMessage());
            return null;
        }
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
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\((\\d+)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    public Integer extractDataTypePrecision(String dataType) {
        if (dataType == null) return null;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\((\\d+),\\s*\\d+\\)");
        java.util.regex.Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    public Integer extractDataTypeScale(String dataType) {
        if (dataType == null) return null;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\(\\d+,\\s*(\\d+)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(dataType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    // ============================================================
    // PACKAGE HELPER METHODS
    // ============================================================

    public String getPackageSpecStatus(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT status FROM user_objects WHERE object_name = UPPER(?) AND object_type = 'PACKAGE'",
                    String.class, packageName);
        } catch (Exception e) {
            return "NOT_FOUND";
        }
    }

    public String getPackageBodyStatus(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT status FROM user_objects WHERE object_name = UPPER(?) AND object_type = 'PACKAGE BODY'",
                    String.class, packageName);
        } catch (Exception e) {
            return "NOT_FOUND";
        }
    }

    public String getPackageCreated(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT MIN(created) FROM user_objects WHERE object_name = UPPER(?) AND object_type IN ('PACKAGE', 'PACKAGE BODY')",
                    String.class, packageName);
        } catch (Exception e) {
            return null;
        }
    }

    public String getPackageLastModified(String packageName) {
        try {
            return oracleJdbcTemplate.queryForObject(
                    "SELECT MAX(last_ddl_time) FROM user_objects WHERE object_name = UPPER(?) AND object_type IN ('PACKAGE', 'PACKAGE BODY')",
                    String.class, packageName);
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
            transformed.put("nullable", col.get("nullable"));
            transformed.put("position", col.get("column_id"));
            transformed.put("dataLength", col.get("data_length"));
            transformed.put("dataPrecision", col.get("data_precision"));
            transformed.put("dataScale", col.get("data_scale"));
            transformed.put("defaultValue", col.get("data_default"));
            transformed.put("distinctCount", col.get("num_distinct"));
            transformed.put("nullCount", col.get("num_nulls"));
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
            transformed.put("refTable", con.get("references_owner") != null ?
                    con.get("references_owner") + "." + con.get("references_constraint") : null);
            transformed.put("deleteRule", con.get("delete_rule"));
            transformed.put("deferrable", con.get("deferrable"));
            transformed.put("deferred", con.get("deferred"));
            transformed.put("validated", con.get("validated"));
            return transformed;
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> transformIndexesForFrontend(List<Map<String, Object>> indexes) {
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
        }).collect(java.util.stream.Collectors.toList());
    }

    public String formatConstraintType(String type) {
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
            String sql = "SELECT object_name, object_type, created, last_ddl_time, status " +
                    "FROM user_objects WHERE status = 'INVALID' ORDER BY object_type, object_name";
            return oracleJdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getInvalidObjects: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Integer getUserTableCount() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_tables", Integer.class);
        } catch (Exception e) {
            log.error("Failed to get user table count: {}", e.getMessage());
            return 0;
        }
    }



}