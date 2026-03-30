package com.usg.apiGeneration.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class PostgreSQLDDLRepository extends PostgreSQLRepository {
    private final JdbcTemplate postgreSQLJdbcTemplate;

    public PostgreSQLDDLRepository(JdbcTemplate postgreSQLJdbcTemplate) {
        super();
        this.postgreSQLJdbcTemplate = postgreSQLJdbcTemplate;
    }

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
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "pg_get_functiondef";
                }
            }

            // Method 2: Using pg_get_viewdef for views
            if (ddl == null && isViewBasedObject(objectType)) {
                ddl = getViewDDL(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "pg_get_viewdef";
                }
            }

            // Method 3: Generate DDL for tables
            if (ddl == null && "TABLE".equalsIgnoreCase(objectType)) {
                ddl = generateTableDDL(objectName);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "TABLE_GENERATED";
                    // Clean up the DDL
                    ddl = cleanUpDDL(ddl, objectType);
                }
            }

            // Method 4: From pg_class and pg_namespace for other object types
            if (ddl == null) {
                ddl = getDDLFromSystemCatalog(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "SYSTEM_CATALOG";
                }
            }

            // Method 5: Generate from information_schema
            if (ddl == null) {
                ddl = generateDDLFromSchema(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) {
                    methodUsed = "GENERATED_FROM_SCHEMA";
                }
            }

            // Clean up the DDL if it exists
            if (ddl != null && !ddl.isEmpty()) {
                ddl = cleanUpDDL(ddl, objectType);
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
            result.put("executionTimeMs", System.currentTimeMillis() - startTime);
        }

        return result;
    }

    /**
     * Generate clean CREATE TABLE DDL with proper PostgreSQL syntax - FIXED
     */
    private String generateTableDDL(String tableName) {
        try {
            // Get columns with detailed information
            String columnSql = "SELECT " +
                    "    column_name, " +
                    "    data_type, " +
                    "    is_nullable, " +
                    "    column_default, " +
                    "    udt_name, " +
                    "    character_maximum_length, " +
                    "    numeric_precision, " +
                    "    numeric_scale " +
                    "FROM information_schema.columns " +
                    "WHERE table_name = ? AND table_schema = 'public' " +
                    "ORDER BY ordinal_position";

            List<Map<String, Object>> columns = postgreSQLJdbcTemplate.queryForList(columnSql, tableName);

            if (columns.isEmpty()) {
                return null;
            }

            // Get primary key columns
            String pkSql = "SELECT " +
                    "    kcu.column_name " +
                    "FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage kcu " +
                    "    ON tc.constraint_name = kcu.constraint_name " +
                    "WHERE tc.table_name = ? " +
                    "    AND tc.table_schema = 'public' " +
                    "    AND tc.constraint_type = 'PRIMARY KEY' " +
                    "ORDER BY kcu.ordinal_position";

            List<Map<String, Object>> pkColumns = postgreSQLJdbcTemplate.queryForList(pkSql, tableName);

            // Build CREATE TABLE statement with proper formatting
            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE ").append(tableName).append(" (\n");

            // Add columns
            List<String> columnLines = new ArrayList<>();
            for (Map<String, Object> col : columns) {
                String columnName = (String) col.get("column_name");
                String dataType = getPostgreSQLDataType(col);
                String isNullable = (String) col.get("is_nullable");
                String defaultValue = (String) col.get("column_default");

                StringBuilder colLine = new StringBuilder();
                colLine.append("    ").append(columnName).append(" ").append(dataType);

                // Add DEFAULT clause
                if (defaultValue != null && !defaultValue.isEmpty() &&
                        !defaultValue.equals("NULL") && !defaultValue.equals("null")) {
                    colLine.append(" DEFAULT ").append(defaultValue);
                }

                // Add NOT NULL constraint
                if ("NO".equalsIgnoreCase(isNullable)) {
                    colLine.append(" NOT NULL");
                }

                columnLines.add(colLine.toString());
            }

            // Add PRIMARY KEY constraint as a separate line if there are primary keys
            if (!pkColumns.isEmpty()) {
                StringBuilder pkLine = new StringBuilder();
                pkLine.append("    CONSTRAINT ").append(tableName).append("_pkey PRIMARY KEY (");
                for (int i = 0; i < pkColumns.size(); i++) {
                    if (i > 0) pkLine.append(", ");
                    pkLine.append(pkColumns.get(i).get("column_name"));
                }
                pkLine.append(")");
                columnLines.add(pkLine.toString());
            }

            // Join all lines with commas
            for (int i = 0; i < columnLines.size(); i++) {
                ddl.append(columnLines.get(i));
                if (i < columnLines.size() - 1) {
                    ddl.append(",\n");
                } else {
                    ddl.append("\n");
                }
            }

            ddl.append(");");

            return ddl.toString();

        } catch (Exception e) {
            log.error("Error generating table DDL for {}: {}", tableName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get PostgreSQL data type with length/precision - FIXED for Integer/Long casting
     */
    private String getPostgreSQLDataType(Map<String, Object> column) {
        String dataType = (String) column.get("data_type");
        String udtName = (String) column.get("udt_name");
        Object charMaxLengthObj = column.get("character_maximum_length");
        Object numericPrecisionObj = column.get("numeric_precision");
        Object numericScaleObj = column.get("numeric_scale");

        // Safely convert to Long
        Long charMaxLength = null;
        if (charMaxLengthObj != null) {
            if (charMaxLengthObj instanceof Long) {
                charMaxLength = (Long) charMaxLengthObj;
            } else if (charMaxLengthObj instanceof Integer) {
                charMaxLength = ((Integer) charMaxLengthObj).longValue();
            } else if (charMaxLengthObj instanceof Number) {
                charMaxLength = ((Number) charMaxLengthObj).longValue();
            }
        }

        Long numericPrecision = null;
        if (numericPrecisionObj != null) {
            if (numericPrecisionObj instanceof Long) {
                numericPrecision = (Long) numericPrecisionObj;
            } else if (numericPrecisionObj instanceof Integer) {
                numericPrecision = ((Integer) numericPrecisionObj).longValue();
            } else if (numericPrecisionObj instanceof Number) {
                numericPrecision = ((Number) numericPrecisionObj).longValue();
            }
        }

        Long numericScale = null;
        if (numericScaleObj != null) {
            if (numericScaleObj instanceof Long) {
                numericScale = (Long) numericScaleObj;
            } else if (numericScaleObj instanceof Integer) {
                numericScale = ((Integer) numericScaleObj).longValue();
            } else if (numericScaleObj instanceof Number) {
                numericScale = ((Number) numericScaleObj).longValue();
            }
        }

        // Handle character types
        if ("character varying".equals(dataType) || "varchar".equals(dataType)) {
            if (charMaxLength != null && charMaxLength > 0) {
                return "VARCHAR(" + charMaxLength + ")";
            }
            return "VARCHAR";
        }

        if ("character".equals(dataType) || "char".equals(dataType)) {
            if (charMaxLength != null && charMaxLength > 0) {
                return "CHAR(" + charMaxLength + ")";
            }
            return "CHAR";
        }

        // Handle text type
        if ("text".equals(dataType)) {
            return "TEXT";
        }

        // Handle numeric types with precision
        if ("numeric".equals(dataType) || "decimal".equals(dataType)) {
            if (numericPrecision != null) {
                if (numericScale != null && numericScale > 0) {
                    return "NUMERIC(" + numericPrecision + "," + numericScale + ")";
                }
                return "NUMERIC(" + numericPrecision + ")";
            }
            return "NUMERIC";
        }

        // Handle integer types
        if ("integer".equals(dataType) || "int".equals(dataType) || "int4".equals(udtName)) {
            return "INTEGER";
        }

        if ("bigint".equals(dataType) || "int8".equals(udtName)) {
            return "BIGINT";
        }

        if ("smallint".equals(dataType) || "int2".equals(udtName)) {
            return "SMALLINT";
        }

        // Handle floating point types
        if ("real".equals(dataType) || "float4".equals(udtName)) {
            return "REAL";
        }

        if ("double precision".equals(dataType) || "float8".equals(udtName)) {
            return "DOUBLE PRECISION";
        }

        // Handle date/time types
        if ("timestamp without time zone".equals(dataType)) {
            return "TIMESTAMP";
        }

        if ("timestamp with time zone".equals(dataType)) {
            return "TIMESTAMPTZ";
        }

        if ("date".equals(dataType)) {
            return "DATE";
        }

        if ("time without time zone".equals(dataType)) {
            return "TIME";
        }

        if ("time with time zone".equals(dataType)) {
            return "TIMETZ";
        }

        // Handle interval
        if ("interval".equals(dataType)) {
            return "INTERVAL";
        }

        // Handle boolean
        if ("boolean".equals(dataType)) {
            return "BOOLEAN";
        }

        // Handle JSON types
        if ("json".equals(dataType)) {
            return "JSON";
        }

        if ("jsonb".equals(dataType)) {
            return "JSONB";
        }

        // Handle UUID
        if ("uuid".equals(dataType)) {
            return "UUID";
        }

        // Handle array types
        if (dataType != null && dataType.endsWith("[]")) {
            return dataType.toUpperCase();
        }

        // Handle geometry/PostGIS types
        if ("geometry".equals(udtName)) {
            return "GEOMETRY";
        }

        // Return the actual type name in uppercase
        return dataType != null ? dataType.toUpperCase() :
                udtName != null ? udtName.toUpperCase() : "TEXT";
    }

    /**
     * Enhanced DDL cleaning with better formatting
     */
    private String cleanUpDDL(String ddl, String objectType) {
        if (ddl == null || ddl.isEmpty()) {
            return ddl;
        }

        String cleaned = ddl;

        // For TABLE objects
        if ("TABLE".equalsIgnoreCase(objectType)) {
            // Ensure proper formatting with newlines and indentation
            cleaned = formatTableDDLNicely(cleaned);

            // Ensure there's a semicolon at the end
            if (!cleaned.trim().endsWith(";")) {
                cleaned = cleaned.trim() + ";";
            }
        }

        return cleaned;
    }

    /**
     * Format table DDL nicely with proper indentation
     */
    private String formatTableDDLNicely(String ddl) {
        // If the DDL is all on one line, reformat it
        if (!ddl.contains("\n")) {
            // Find the CREATE TABLE part
            String createPart = ddl.substring(0, ddl.indexOf("(")).trim();
            String columnsPart = ddl.substring(ddl.indexOf("(") + 1, ddl.lastIndexOf(")"));

            // Split columns by commas, but be careful with commas inside parentheses
            List<String> columns = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            int parenCount = 0;

            for (char c : columnsPart.toCharArray()) {
                if (c == '(') parenCount++;
                if (c == ')') parenCount--;

                if (c == ',' && parenCount == 0) {
                    columns.add(current.toString().trim());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
            columns.add(current.toString().trim());

            // Rebuild with proper formatting
            StringBuilder formatted = new StringBuilder();
            formatted.append(createPart).append(" (\n");

            for (int i = 0; i < columns.size(); i++) {
                formatted.append("    ").append(columns.get(i));
                if (i < columns.size() - 1) {
                    formatted.append(",\n");
                } else {
                    formatted.append("\n");
                }
            }

            formatted.append(")");
            return formatted.toString();
        }

        // If already formatted, just ensure indentation is consistent
        String[] lines = ddl.split("\n");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            if (i == 0 || line.startsWith("CREATE")) {
                formatted.append(line);
            } else if (line.startsWith(")")) {
                formatted.append(line);
            } else {
                formatted.append("    ").append(line);
            }

            if (i < lines.length - 1) {
                formatted.append("\n");
            }
        }

        return formatted.toString();
    }


    /**
     * Fix missing commas before constraints
     */
    private String fixMissingCommasBeforeConstraints(String ddl) {
        // Pattern: column definition followed by CONSTRAINT without comma
        // e.g., "version character varying(255)\n    CONSTRAINT"
        Pattern pattern = Pattern.compile("(\\w+\\s+[\\w\\s\\(\\)]+)\\s+(CONSTRAINT)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(ddl);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Check if there's already a comma before CONSTRAINT
            String before = matcher.group(1);
            if (!before.trim().endsWith(",")) {
                matcher.appendReplacement(result, "$1,\n    $2");
            } else {
                matcher.appendReplacement(result, "$1\n    $2");
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Fix trailing commas
     */
    private String fixTrailingCommas(String ddl) {
        // Remove commas before closing parenthesis
        return ddl.replaceAll(",\\s*\\)", "\n)");
    }

    /**
     * Format table DDL nicely
     */
    private String formatTableDDL(String ddl) {
        // Split into lines
        String[] lines = ddl.split("\n");
        StringBuilder formatted = new StringBuilder();

        for (String line : lines) {
            // Indent column definitions
            if (line.trim().matches("^\\w+\\s+\\w+.*") && !line.trim().startsWith("CREATE")) {
                formatted.append("    ").append(line.trim());
            } else {
                formatted.append(line.trim());
            }

            // Add proper spacing
            if (!line.trim().endsWith("(") && !line.trim().endsWith(")")) {
                formatted.append("\n");
            }
        }

        return formatted.toString();
    }

    /**
     * Clean view DDL
     */
    private String cleanViewDDL(String ddl) {
        // Remove extra whitespace
        ddl = ddl.replaceAll("\\s+", " ");

        // Format SELECT part nicely
        if (ddl.toUpperCase().contains("SELECT")) {
            ddl = ddl.replaceAll("SELECT", "\nSELECT");
            ddl = ddl.replaceAll("FROM", "\nFROM");
            ddl = ddl.replaceAll("WHERE", "\nWHERE");
            ddl = ddl.replaceAll("JOIN", "\n  JOIN");
            ddl = ddl.replaceAll("LEFT JOIN", "\n  LEFT JOIN");
            ddl = ddl.replaceAll("RIGHT JOIN", "\n  RIGHT JOIN");
            ddl = ddl.replaceAll("INNER JOIN", "\n  INNER JOIN");
            ddl = ddl.replaceAll("GROUP BY", "\nGROUP BY");
            ddl = ddl.replaceAll("ORDER BY", "\nORDER BY");
            ddl = ddl.replaceAll("HAVING", "\nHAVING");
        }

        return ddl;
    }

    /**
     * Clean function/procedure DDL
     */
    private String cleanFunctionDDL(String ddl) {
        // Format function/procedure for readability
        ddl = ddl.replaceAll("\\s+", " ");
        ddl = ddl.replaceAll("CREATE OR REPLACE", "CREATE OR REPLACE\n");
        ddl = ddl.replaceAll("RETURNS", "\nRETURNS");
        ddl = ddl.replaceAll("LANGUAGE", "\nLANGUAGE");
        ddl = ddl.replaceAll("AS \\$\\$", "\nAS $$\n");
        ddl = ddl.replaceAll("\\$\\$;", "\n$$;");

        return ddl;
    }

    /**
     * Get DDL from system catalog (improved version)
     */
    private String getDDLFromSystemCatalog(String objectName, String objectType) {
        try {
            String sql = null;

            if ("TABLE".equalsIgnoreCase(objectType)) {
                // Use pg_dump or generate from system catalogs
                return generateTableDDL(objectName);
            } else if ("VIEW".equalsIgnoreCase(objectType)) {
                sql = "SELECT pg_get_viewdef(c.oid, true) as ddl " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE c.relname = ? AND c.relkind = 'v'";
            } else if ("MATERIALIZED VIEW".equalsIgnoreCase(objectType)) {
                sql = "SELECT pg_get_viewdef(c.oid, true) as ddl " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE c.relname = ? AND c.relkind = 'm'";
            } else if ("SEQUENCE".equalsIgnoreCase(objectType)) {
                sql = "SELECT 'CREATE SEQUENCE ' || c.relname || ';' as ddl " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE c.relname = ? AND c.relkind = 'S'";
            }

            if (sql != null) {
                try {
                    return postgreSQLJdbcTemplate.queryForObject(sql, String.class, objectName);
                } catch (EmptyResultDataAccessException e) {
                    log.debug("Object not found in system catalog: {} {}", objectType, objectName);
                    return null;
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error getting DDL from system catalog: {}", e.getMessage());
            return null;
        }
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