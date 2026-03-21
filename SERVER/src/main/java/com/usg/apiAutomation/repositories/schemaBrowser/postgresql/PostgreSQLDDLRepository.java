package com.usg.apiAutomation.repositories.schemaBrowser.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class PostgreSQLDDLRepository extends PostgreSQLRepository {

    // ============================================================
    // DDL METHODS
    // ============================================================

    public Map<String, Object> getObjectDDLForFrontend(String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();
        result.put("objectName", objectName);
        result.put("objectType", objectType);

        long startTime = System.currentTimeMillis();

        try {
            String ddl = null;
            String methodUsed = null;

            // Method 1: Using pg_get_functiondef for functions/procedures
            if (isFunctionBasedObject(objectType)) {
                ddl = getFunctionDDL(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "pg_get_functiondef";
            }

            // Method 2: Using pg_get_viewdef for views
            if (ddl == null && isViewBasedObject(objectType)) {
                ddl = getViewDDL(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "pg_get_viewdef";
            }

            // Method 3: Using pg_get_tabledef for tables
            if (ddl == null && "TABLE".equalsIgnoreCase(objectType)) {
                ddl = getTableDDL(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "pg_get_tabledef";
            }

            // Method 4: From pg_class and pg_namespace
            if (ddl == null) {
                ddl = getDDLFromSystemCatalog(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "SYSTEM_CATALOG";
            }

            // Method 5: Generate from information_schema
            if (ddl == null) {
                ddl = generateDDLFromSchema(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "GENERATED_FROM_SCHEMA";
            }

            long executionTime = System.currentTimeMillis() - startTime;
            result.put("executionTimeMs", executionTime);

            if (ddl != null && !ddl.isEmpty()) {
                result.put("ddl", ddl);
                result.put("status", "SUCCESS");
                result.put("method", methodUsed);
                result.put("message", "DDL retrieved successfully using " + methodUsed);
            } else {
                result.put("ddl", generateDetailedErrorMessage(objectName, objectType));
                result.put("status", "NOT_AVAILABLE");
                result.put("message", "Could not retrieve DDL after trying multiple methods");
                result.put("diagnostics", getObjectDiagnostics(objectName, objectType));
            }

        } catch (Exception e) {
            log.error("Error in getObjectDDLForFrontend for {} {}: {}", objectType, objectName, e.getMessage(), e);
            result.put("ddl", "-- Error retrieving DDL: " + e.getMessage());
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private String getFunctionDDL(String objectName, String objectType) {
        try {
            String functionType = objectType.toUpperCase();
            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String schema = (String) objectLocation.get("schema");

            if (schema == null) {
                schema = getCurrentSchema();
            }

            String sql;
            if ("PROCEDURE".equals(functionType)) {
                sql = "SELECT pg_get_functiondef(p.oid) as ddl " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'p'";
            } else if ("FUNCTION".equals(functionType)) {
                sql = "SELECT pg_get_functiondef(p.oid) as ddl " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'f'";
            } else {
                return null;
            }

            try {
                return getJdbcTemplate().queryForObject(sql, String.class, schema, objectName.toLowerCase());
            } catch (Exception e) {
                // Try without schema
                sql = sql.replace("n.nspname = ? AND ", "");
                return getJdbcTemplate().queryForObject(sql, String.class, objectName.toLowerCase());
            }
        } catch (Exception e) {
            log.debug("getFunctionDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String getViewDDL(String objectName, String objectType) {
        try {
            if (!"VIEW".equalsIgnoreCase(objectType) && !"MATERIALIZED VIEW".equalsIgnoreCase(objectType)) {
                return null;
            }

            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String schema = (String) objectLocation.get("schema");

            if (schema == null) {
                schema = getCurrentSchema();
            }

            boolean isMaterialized = "MATERIALIZED VIEW".equalsIgnoreCase(objectType);
            String sql;

            if (isMaterialized) {
                sql = "SELECT 'CREATE MATERIALIZED VIEW ' || n.nspname || '.' || c.relname || ' AS ' || " +
                        "pg_get_viewdef(c.oid) as ddl " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'm'";
            } else {
                sql = "SELECT 'CREATE OR REPLACE VIEW ' || n.nspname || '.' || c.relname || ' AS ' || " +
                        "pg_get_viewdef(c.oid) as ddl " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'v'";
            }

            try {
                return getJdbcTemplate().queryForObject(sql, String.class, schema, objectName.toLowerCase());
            } catch (Exception e) {
                // Try without schema
                sql = sql.replace("n.nspname = ? AND ", "");
                return getJdbcTemplate().queryForObject(sql, String.class, objectName.toLowerCase());
            }
        } catch (Exception e) {
            log.debug("getViewDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String getTableDDL(String objectName, String objectType) {
        try {
            if (!"TABLE".equalsIgnoreCase(objectType)) {
                return null;
            }

            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String schema = (String) objectLocation.get("schema");

            if (schema == null) {
                schema = getCurrentSchema();
            }

            // PostgreSQL doesn't have a built-in function to get full table DDL
            // We'll construct it from system catalogs
            return generateTableDDL(schema, objectName);
        } catch (Exception e) {
            log.debug("getTableDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String getDDLFromSystemCatalog(String objectName, String objectType) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String schema = (String) objectLocation.get("schema");
            String relKind = getRelationKind(objectType);

            if (schema == null || relKind == null) {
                return null;
            }

            // Try to get DDL using pg_dump like functionality
            String sql = "SELECT 'CREATE ' || " +
                    "CASE WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "WHEN c.relkind = 's' THEN 'SEQUENCE' " +
                    "WHEN c.relkind = 'i' THEN 'INDEX' END || ' ' || " +
                    "n.nspname || '.' || c.relname || ' AS ' || " +
                    "pg_get_viewdef(c.oid) as ddl " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = ?";

            try {
                return getJdbcTemplate().queryForObject(sql, String.class, schema, objectName.toLowerCase(), relKind);
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            log.debug("getDDLFromSystemCatalog failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateDDLFromSchema(String objectName, String objectType) {
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "FUNCTION":
            case "PROCEDURE":
                return generateFunctionDDL(objectName, upperType);
            case "TABLE":
                return generateTableDDL(null, objectName);
            case "VIEW":
                return generateViewDDL(null, objectName);
            case "SEQUENCE":
                return generateSequenceDDL(objectName);
            case "INDEX":
                return generateIndexDDL(objectName);
            default:
                return null;
        }
    }

    private String generateTableDDL(String schema, String tableName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(tableName, "TABLE");
            if (schema == null) {
                schema = (String) objectLocation.get("schema");
            }
            if (schema == null) {
                schema = getCurrentSchema();
            }

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE ");
            if (!schema.equalsIgnoreCase(getCurrentSchema())) {
                ddl.append(schema).append(".");
            }
            ddl.append(tableName).append(" (\n");

            // Get columns
            List<Map<String, Object>> columns = getTableColumns(schema, tableName);
            if (columns.isEmpty()) {
                return null;
            }

            for (int i = 0; i < columns.size(); i++) {
                Map<String, Object> col = columns.get(i);
                ddl.append("    ").append(col.get("column_name")).append(" ").append(col.get("data_type"));

                // Add data type modifiers
                Integer charMaxLen = (Integer) col.get("character_maximum_length");
                Integer numericPrecision = (Integer) col.get("numeric_precision");
                Integer numericScale = (Integer) col.get("numeric_scale");
                String dataType = (String) col.get("data_type");

                if (charMaxLen != null && charMaxLen > 0 &&
                        ("character varying".equals(dataType) || "varchar".equals(dataType) ||
                                "character".equals(dataType) || "char".equals(dataType))) {
                    ddl.append("(").append(charMaxLen).append(")");
                } else if (numericPrecision != null &&
                        ("numeric".equals(dataType) || "decimal".equals(dataType))) {
                    ddl.append("(").append(numericPrecision);
                    if (numericScale != null && numericScale > 0) {
                        ddl.append(",").append(numericScale);
                    }
                    ddl.append(")");
                }

                // Add NOT NULL constraint
                if ("NO".equals(col.get("is_nullable"))) {
                    ddl.append(" NOT NULL");
                }

                // Add DEFAULT value
                Object defaultValue = col.get("column_default");
                if (defaultValue != null && !defaultValue.toString().isEmpty()) {
                    ddl.append(" DEFAULT ").append(defaultValue);
                }

                if (i < columns.size() - 1) {
                    ddl.append(",");
                }
                ddl.append("\n");
            }

            // Get primary key
            String pkConstraint = getPrimaryKeyConstraint(schema, tableName);
            if (pkConstraint != null) {
                ddl.append("    ").append(pkConstraint).append("\n");
            }

            ddl.append(");");

            // Add table comments if any
            String comment = getTableComment(schema, tableName);
            if (comment != null && !comment.isEmpty()) {
                ddl.append("\n\nCOMMENT ON TABLE ");
                if (!schema.equalsIgnoreCase(getCurrentSchema())) {
                    ddl.append(schema).append(".");
                }
                ddl.append(tableName).append(" IS '").append(comment.replace("'", "''")).append("';");
            }

            return ddl.toString();
        } catch (Exception e) {
            log.debug("generateTableDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateViewDDL(String schema, String viewName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(viewName, "VIEW");
            if (schema == null) {
                schema = (String) objectLocation.get("schema");
            }
            if (schema == null) {
                schema = getCurrentSchema();
            }

            String sql = "SELECT 'CREATE OR REPLACE VIEW ' || " +
                    "CASE WHEN ? != ? THEN ? || '.' ELSE '' END || ? || ' AS ' || " +
                    "pg_get_viewdef(c.oid, true) as ddl " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'v'";

            return getJdbcTemplate().queryForObject(sql, String.class,
                    schema, getCurrentSchema(), schema, viewName, schema, viewName.toLowerCase());
        } catch (Exception e) {
            log.debug("generateViewDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateFunctionDDL(String functionName, String functionType) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(functionName, functionType);
            String schema = (String) objectLocation.get("schema");

            if (schema == null) {
                schema = getCurrentSchema();
            }

            String sql = "SELECT pg_get_functiondef(p.oid) as ddl " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            return getJdbcTemplate().queryForObject(sql, String.class, schema, functionName.toLowerCase());
        } catch (Exception e) {
            log.debug("generateFunctionDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateSequenceDDL(String sequenceName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(sequenceName, "SEQUENCE");
            String schema = (String) objectLocation.get("schema");

            if (schema == null) {
                schema = getCurrentSchema();
            }

            String sql = "SELECT 'CREATE SEQUENCE ' || n.nspname || '.' || c.relname || " +
                    "' START WITH ' || s.seqstart || " +
                    "' INCREMENT BY ' || s.seqincrement || " +
                    "' MINVALUE ' || s.seqmin || " +
                    "' MAXVALUE ' || s.seqmax || " +
                    "CASE WHEN s.seqcycle THEN ' CYCLE' ELSE ' NO CYCLE' END || ';' as ddl " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'S'";

            return getJdbcTemplate().queryForObject(sql, String.class, schema, sequenceName.toLowerCase());
        } catch (Exception e) {
            log.debug("generateSequenceDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateIndexDDL(String indexName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(indexName, "INDEX");
            String schema = (String) objectLocation.get("schema");

            if (schema == null) {
                schema = getCurrentSchema();
            }

            String sql = "SELECT pg_get_indexdef(i.indexrelid) as ddl " +
                    "FROM pg_index i " +
                    "JOIN pg_class c ON i.indexrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ?";

            return getJdbcTemplate().queryForObject(sql, String.class, schema, indexName.toLowerCase());
        } catch (Exception e) {
            log.debug("generateIndexDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private boolean isFunctionBasedObject(String objectType) {
        if (objectType == null) return false;
        String upperType = objectType.toUpperCase();
        return upperType.equals("FUNCTION") || upperType.equals("PROCEDURE");
    }

    private boolean isViewBasedObject(String objectType) {
        if (objectType == null) return false;
        String upperType = objectType.toUpperCase();
        return upperType.equals("VIEW") || upperType.equals("MATERIALIZED VIEW");
    }

    private String getRelationKind(String objectType) {
        if (objectType == null) return null;
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "TABLE": return "r";
            case "VIEW": return "v";
            case "MATERIALIZED VIEW": return "m";
            case "INDEX": return "i";
            case "SEQUENCE": return "S";
            default: return null;
        }
    }

    public Map<String, Object> findObjectLocation(String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();
        try {
            String relKind = getRelationKind(objectType);
            String sql;

            if (relKind != null) {
                sql = "SELECT n.nspname as schema, c.relname as object_name, " +
                        "CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                        "WHEN c.relkind = 'v' THEN 'VIEW' " +
                        "WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                        "WHEN c.relkind = 'i' THEN 'INDEX' " +
                        "WHEN c.relkind = 'S' THEN 'SEQUENCE' END as object_type " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "AND c.relname = ? AND c.relkind = ?";
                Map<String, Object> queryResult = getJdbcTemplate().queryForMap(sql, objectName.toLowerCase(), relKind);
                result.putAll(queryResult);
            } else if ("FUNCTION".equalsIgnoreCase(objectType) || "PROCEDURE".equalsIgnoreCase(objectType)) {
                sql = "SELECT n.nspname as schema, p.proname as object_name, " +
                        "CASE WHEN p.prokind = 'f' THEN 'FUNCTION' ELSE 'PROCEDURE' END as object_type " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "AND p.proname = ?";
                Map<String, Object> queryResult = getJdbcTemplate().queryForMap(sql, objectName.toLowerCase());
                result.putAll(queryResult);
            }
            return result;
        } catch (Exception e) {
            log.debug("Object {} of type {} not found", objectName, objectType);
            return result;
        }
    }

    private Map<String, Object> getObjectDiagnostics(String objectName, String objectType) {
        Map<String, Object> diagnostics = new HashMap<>();
        try {
            String relKind = getRelationKind(objectType);
            String sql;

            if (relKind != null) {
                sql = "SELECT n.nspname as schema, c.relname, c.relkind, " +
                        "pg_total_relation_size(c.oid) as size_bytes " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE c.relname = ? AND c.relkind = ?";
                try {
                    Map<String, Object> objectInfo = getJdbcTemplate().queryForMap(sql, objectName.toLowerCase(), relKind);
                    diagnostics.put("exists", true);
                    diagnostics.put("schema", objectInfo.get("schema"));
                    diagnostics.put("sizeBytes", objectInfo.get("size_bytes"));
                } catch (Exception e) {
                    diagnostics.put("exists", false);
                    diagnostics.put("message", "Object not found");
                }
            } else {
                diagnostics.put("exists", false);
                diagnostics.put("message", "Object type not supported");
            }

            diagnostics.put("currentUser", getCurrentUser());
            diagnostics.put("currentSchema", getCurrentSchema());
            diagnostics.put("searchPath", getSearchPath());

        } catch (Exception e) {
            diagnostics.put("error", e.getMessage());
        }
        return diagnostics;
    }

    private String generateDetailedErrorMessage(String objectName, String objectType) {
        StringBuilder msg = new StringBuilder();
        msg.append("-- DDL not available for ").append(objectType).append(" ").append(objectName).append("\n");
        msg.append("-- \n");
        msg.append("-- Possible reasons and solutions:\n");
        msg.append("-- 1. OBJECT DOES NOT EXIST: Verify the object name and type\n");
        msg.append("-- 2. INSUFFICIENT PRIVILEGES: Grant USAGE on schema and SELECT on the object\n");
        msg.append("-- 3. WRONG SCHEMA: Specify the schema: schema_name.").append(objectName).append("\n");
        msg.append("-- 4. CHECK search_path: SET search_path TO schema_name, public;\n");
        return msg.toString();
    }

    private List<Map<String, Object>> getTableColumns(String schema, String tableName) {
        try {
            String sql = "SELECT column_name, data_type, character_maximum_length, " +
                    "numeric_precision, numeric_scale, is_nullable, column_default " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ? " +
                    "ORDER BY ordinal_position";
            return getJdbcTemplate().queryForList(sql, schema, tableName);
        } catch (Exception e) {
            log.debug("getTableColumns failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getPrimaryKeyConstraint(String schema, String tableName) {
        try {
            String sql = "SELECT 'CONSTRAINT ' || tc.constraint_name || ' PRIMARY KEY (' || " +
                    "string_agg(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) || ')' as pk_constraint " +
                    "FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage kcu " +
                    "ON tc.constraint_name = kcu.constraint_name " +
                    "AND tc.table_schema = kcu.table_schema " +
                    "WHERE tc.constraint_type = 'PRIMARY KEY' " +
                    "AND tc.table_schema = ? AND tc.table_name = ? " +
                    "GROUP BY tc.constraint_name";
            return getJdbcTemplate().queryForObject(sql, String.class, schema, tableName);
        } catch (Exception e) {
            return null;
        }
    }

    private String getTableComment(String schema, String tableName) {
        try {
            String sql = "SELECT obj_description(c.oid) as comment " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r'";
            return getJdbcTemplate().queryForObject(sql, String.class, schema, tableName);
        } catch (Exception e) {
            return null;
        }
    }

    private String getSearchPath() {
        try {
            return getJdbcTemplate().queryForObject("SHOW search_path", String.class);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // These methods should be implemented in the parent PostgreSQLRepository class
    public String getCurrentUser() {
        try {
            return getJdbcTemplate().queryForObject("SELECT current_user", String.class);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public String getCurrentSchema() {
        try {
            return getJdbcTemplate().queryForObject("SELECT current_schema()", String.class);
        } catch (Exception e) {
            return "public";
        }
    }
}