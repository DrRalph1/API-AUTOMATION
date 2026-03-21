package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class OracleDDLRepository extends OracleRepository {

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

            // Method 1: Standard DBMS_METADATA
            ddl = getObjectDDLWithMetadata(objectName, objectType);
            if (ddl != null && !ddl.isEmpty()) methodUsed = "DBMS_METADATA";

            // Method 2: With transform
            if (ddl == null) {
                ddl = getObjectDDLWithTransform(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "DBMS_METADATA with TRANSFORM";
            }

            // Method 3: From USER_SOURCE
            if (ddl == null && isSourceBasedObject(objectType)) {
                ddl = getDDLFromSource(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "USER_SOURCE";
            }

            // Method 4: From ALL_SOURCE
            if (ddl == null) {
                ddl = getDDLFromAllSource(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "ALL_SOURCE";
            }

            // Method 5: With owner resolution
            if (ddl == null) {
                ddl = getObjectDDLWithOwnerResolution(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "DBMS_METADATA with owner resolution";
            }

            // Method 6: From DBA_SOURCE
            if (ddl == null) {
                ddl = getDDLFromDBASource(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "DBA_SOURCE";
            }

            // Method 7: Generate from dictionary
            if (ddl == null) {
                ddl = generateDDLFromDictionary(objectName, objectType);
                if (ddl != null && !ddl.isEmpty()) methodUsed = "GENERATED";
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

    private String getObjectDDLWithMetadata(String objectName, String objectType) {
        try {
            String metadataType = convertToMetadataObjectType(objectType);
            String currentUser = getCurrentUser();
            String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) FROM DUAL";
            try {
                return getJdbcTemplate().queryForObject(sql, String.class, metadataType, objectName.toUpperCase(), currentUser);
            } catch (Exception e) {
                Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
                String owner = (String) objectLocation.get("owner");
                if (owner != null && !owner.equals(currentUser)) {
                    return getJdbcTemplate().queryForObject(sql, String.class, metadataType, objectName.toUpperCase(), owner);
                }
                return null;
            }
        } catch (Exception e) {
            log.debug("getObjectDDLWithMetadata failed: {}", e.getMessage());
            return null;
        }
    }

    private String getObjectDDLWithTransform(String objectName, String objectType) {
        try {
            String metadataType = convertToMetadataObjectType(objectType);
            String currentUser = getCurrentUser();
            String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?, DBMS_METADATA.SESSION_TRANSFORM('SQLTERMINATOR', TRUE) || " +
                    "DBMS_METADATA.SESSION_TRANSFORM('PRETTY', TRUE)) FROM DUAL";
            try {
                return getJdbcTemplate().queryForObject(sql, String.class, metadataType, objectName.toUpperCase(), currentUser);
            } catch (Exception e) {
                Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
                String owner = (String) objectLocation.get("owner");
                if (owner != null && !owner.equals(currentUser)) {
                    return getJdbcTemplate().queryForObject(sql, String.class, metadataType, objectName.toUpperCase(), owner);
                }
                return null;
            }
        } catch (Exception e) {
            log.debug("getObjectDDLWithTransform failed: {}", e.getMessage());
            return null;
        }
    }

    private String getDDLFromSource(String objectName, String objectType) {
        try {
            String sourceType = objectType.toUpperCase();
            if ("PACKAGE BODY".equals(sourceType)) sourceType = "PACKAGE BODY";
            else if ("TYPE BODY".equals(sourceType)) sourceType = "TYPE BODY";

            String sql = "SELECT text FROM user_source WHERE UPPER(name) = UPPER(?) AND UPPER(type) = UPPER(?) ORDER BY line";
            List<String> sourceLines = getJdbcTemplate().queryForList(sql, String.class, objectName, sourceType);

            if (!sourceLines.isEmpty()) {
                StringBuilder ddl = new StringBuilder();
                String firstLine = sourceLines.get(0).toUpperCase();
                if (!firstLine.contains("CREATE OR REPLACE") && !firstLine.contains("CREATE") &&
                        !firstLine.contains("FUNCTION") && !firstLine.contains("PROCEDURE") &&
                        !firstLine.contains("PACKAGE") && !firstLine.contains("TYPE")) {
                    ddl.append("CREATE OR REPLACE ");
                }
                for (String line : sourceLines) ddl.append(line);
                String ddlStr = ddl.toString();
                if (objectType.equalsIgnoreCase("PROCEDURE") || objectType.equalsIgnoreCase("FUNCTION") ||
                        objectType.equalsIgnoreCase("PACKAGE") || objectType.equalsIgnoreCase("PACKAGE BODY")) {
                    if (!ddlStr.trim().endsWith("/")) ddlStr = ddlStr + "\n/";
                }
                return ddlStr;
            }
            return null;
        } catch (Exception e) {
            log.debug("getDDLFromSource failed: {}", e.getMessage());
            return null;
        }
    }

    private String getDDLFromAllSource(String objectName, String objectType) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(objectName, objectType);
            String owner = (String) objectLocation.get("owner");
            if (owner == null) return null;

            String sourceType = objectType.toUpperCase();
            if ("PACKAGE BODY".equals(sourceType)) sourceType = "PACKAGE BODY";

            String sql = "SELECT text FROM all_source WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                    "AND UPPER(type) = UPPER(?) ORDER BY line";
            List<String> sourceLines = getJdbcTemplate().queryForList(sql, String.class, owner, objectName, sourceType);
            return sourceLines.isEmpty() ? null : String.join("", sourceLines);
        } catch (Exception e) {
            log.debug("getDDLFromAllSource failed: {}", e.getMessage());
            return null;
        }
    }

    private String getObjectDDLWithOwnerResolution(String objectName, String objectType) {
        try {
            String findOwnerSql = "SELECT owner FROM all_objects WHERE UPPER(object_name) = UPPER(?) " +
                    "AND UPPER(object_type) = UPPER(?) AND ROWNUM = 1";
            String owner;
            try {
                owner = getJdbcTemplate().queryForObject(findOwnerSql, String.class, objectName, objectType);
            } catch (Exception e) {
                String findAnySql = "SELECT owner FROM all_objects WHERE UPPER(object_name) = UPPER(?) AND ROWNUM = 1";
                owner = getJdbcTemplate().queryForObject(findAnySql, String.class, objectName);
            }
            if (owner == null) return null;

            String metadataType = convertToMetadataObjectType(objectType);
            try {
                String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?) FROM DUAL";
                return getJdbcTemplate().queryForObject(sql, String.class, metadataType, owner + "." + objectName.toUpperCase());
            } catch (Exception e) {
                String sql2 = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) FROM DUAL";
                return getJdbcTemplate().queryForObject(sql2, String.class, metadataType, objectName.toUpperCase(), owner);
            }
        } catch (Exception e) {
            log.debug("getObjectDDLWithOwnerResolution failed: {}", e.getMessage());
            return null;
        }
    }

    private String getDDLFromDBASource(String objectName, String objectType) {
        try {
            String checkSql = "SELECT COUNT(*) FROM all_tables WHERE table_name = 'DBA_SOURCE'";
            Integer count = getJdbcTemplate().queryForObject(checkSql, Integer.class);
            if (count == 0) return null;

            String sourceType = objectType.toUpperCase();
            if ("PACKAGE BODY".equals(sourceType)) sourceType = "PACKAGE BODY";

            String sql = "SELECT text FROM dba_source WHERE UPPER(name) = UPPER(?) AND UPPER(type) = UPPER(?) ORDER BY line";
            List<String> sourceLines = getJdbcTemplate().queryForList(sql, String.class, objectName, sourceType);
            return sourceLines.isEmpty() ? null : String.join("", sourceLines);
        } catch (Exception e) {
            log.debug("getDDLFromDBASource failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateDDLFromDictionary(String objectName, String objectType) {
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "PROCEDURE": return generateProcedureDDL(objectName);
            case "FUNCTION": return generateFunctionDDL(objectName);
            case "TABLE": return generateTableDDL(objectName);
            case "VIEW": return generateViewDDL(objectName);
            default: return null;
        }
    }

    private String generateProcedureDDL(String procedureName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(procedureName, "PROCEDURE");
            String owner = (String) objectLocation.get("owner");
            if (owner == null) owner = getCurrentUser();

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE OR REPLACE PROCEDURE ");
            if (!owner.equalsIgnoreCase(getCurrentUser())) ddl.append(owner).append(".");
            ddl.append(procedureName).append("\n");

            String paramSql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                paramSql = "SELECT argument_name, data_type, in_out, data_length, data_precision, data_scale " +
                        "FROM user_arguments WHERE UPPER(object_name) = UPPER(?) AND package_name IS NULL " +
                        "AND argument_name IS NOT NULL ORDER BY position";
            } else {
                paramSql = "SELECT argument_name, data_type, in_out, data_length, data_precision, data_scale " +
                        "FROM all_arguments WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL AND argument_name IS NOT NULL ORDER BY position";
            }

            List<Map<String, Object>> params;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                params = getJdbcTemplate().queryForList(paramSql, procedureName);
            } else {
                params = getJdbcTemplate().queryForList(paramSql, owner, procedureName);
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
                        if ("IN".equals(inOut)) ddl.append(argumentName).append(" ");
                        else if ("OUT".equals(inOut)) ddl.append(argumentName).append(" OUT ");
                        else if ("IN/OUT".equals(inOut)) ddl.append(argumentName).append(" IN OUT ");
                    } else {
                        ddl.append(argumentName).append(" ");
                    }
                    ddl.append(dataType);
                    Number dataLength = (Number) param.get("data_length");
                    Number dataPrecision = (Number) param.get("data_precision");
                    Number dataScale = (Number) param.get("data_scale");

                    if (dataLength != null && dataLength.intValue() > 0 && ("VARCHAR2".equalsIgnoreCase(dataType) ||
                            "CHAR".equalsIgnoreCase(dataType) || "VARCHAR".equalsIgnoreCase(dataType))) {
                        ddl.append("(").append(dataLength).append(")");
                    } else if (dataPrecision != null) {
                        ddl.append("(").append(dataPrecision);
                        if (dataScale != null && dataScale.intValue() > 0) ddl.append(",").append(dataScale);
                        ddl.append(")");
                    }
                    if (i < params.size() - 1) ddl.append(",");
                    ddl.append("\n");
                }
                ddl.append(")\n");
            }

            ddl.append("IS\nBEGIN\n    NULL;\nEND ").append(procedureName).append(";\n/");
            return ddl.toString();
        } catch (Exception e) {
            log.debug("generateProcedureDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateFunctionDDL(String functionName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(functionName, "FUNCTION");
            String owner = (String) objectLocation.get("owner");
            if (owner == null) owner = getCurrentUser();

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE OR REPLACE FUNCTION ");
            if (!owner.equalsIgnoreCase(getCurrentUser())) ddl.append(owner).append(".");
            ddl.append(functionName).append("\n");

            String paramSql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                paramSql = "SELECT argument_name, data_type, in_out, data_length, data_precision, data_scale, position " +
                        "FROM user_arguments WHERE UPPER(object_name) = UPPER(?) AND package_name IS NULL ORDER BY position";
            } else {
                paramSql = "SELECT argument_name, data_type, in_out, data_length, data_precision, data_scale, position " +
                        "FROM all_arguments WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL ORDER BY position";
            }

            List<Map<String, Object>> allArgs;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                allArgs = getJdbcTemplate().queryForList(paramSql, functionName);
            } else {
                allArgs = getJdbcTemplate().queryForList(paramSql, owner, functionName);
            }

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
                        if ("OUT".equals(inOut)) ddl.append(argumentName).append(" OUT ");
                        else if ("IN/OUT".equals(inOut)) ddl.append(argumentName).append(" IN OUT ");
                    } else {
                        ddl.append(argumentName).append(" ");
                    }
                    ddl.append(dataType);
                    Number dataLength = (Number) param.get("data_length");
                    Number dataPrecision = (Number) param.get("data_precision");
                    Number dataScale = (Number) param.get("data_scale");

                    if (dataLength != null && dataLength.intValue() > 0 && ("VARCHAR2".equalsIgnoreCase(dataType) ||
                            "CHAR".equalsIgnoreCase(dataType) || "VARCHAR".equalsIgnoreCase(dataType))) {
                        ddl.append("(").append(dataLength).append(")");
                    } else if (dataPrecision != null) {
                        ddl.append("(").append(dataPrecision);
                        if (dataScale != null && dataScale.intValue() > 0) ddl.append(",").append(dataScale);
                        ddl.append(")");
                    }
                    if (i < params.size() - 1) ddl.append(",");
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

                if (dataLength != null && dataLength.intValue() > 0 && ("VARCHAR2".equalsIgnoreCase(dataType) ||
                        "CHAR".equalsIgnoreCase(dataType) || "VARCHAR".equalsIgnoreCase(dataType))) {
                    ddl.append("(").append(dataLength).append(")");
                } else if (dataPrecision != null) {
                    ddl.append("(").append(dataPrecision);
                    if (dataScale != null && dataScale.intValue() > 0) ddl.append(",").append(dataScale);
                    ddl.append(")");
                }
                ddl.append("\n");
            }

            ddl.append("IS\nBEGIN\n    RETURN NULL;\nEND ").append(functionName).append(";\n/");
            return ddl.toString();
        } catch (Exception e) {
            log.debug("generateFunctionDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateTableDDL(String tableName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(tableName, "TABLE");
            String owner = (String) objectLocation.get("owner");
            if (owner == null) owner = getCurrentUser();

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE ");
            if (!owner.equalsIgnoreCase(getCurrentUser())) ddl.append(owner).append(".");
            ddl.append(tableName).append(" (\n");

            List<Map<String, Object>> columns = getTableColumns(owner, tableName);
            for (int i = 0; i < columns.size(); i++) {
                Map<String, Object> col = columns.get(i);
                ddl.append("    ").append(col.get("column_name")).append(" ").append(col.get("data_type"));

                Number dataLength = (Number) col.get("data_length");
                Number dataPrecision = (Number) col.get("data_precision");
                Number dataScale = (Number) col.get("data_scale");
                String dataType = (String) col.get("data_type");

                if (dataLength != null && dataLength.intValue() > 0 && ("VARCHAR2".equalsIgnoreCase(dataType) ||
                        "CHAR".equalsIgnoreCase(dataType) || "VARCHAR".equalsIgnoreCase(dataType))) {
                    ddl.append("(").append(dataLength).append(")");
                } else if (dataPrecision != null) {
                    ddl.append("(").append(dataPrecision);
                    if (dataScale != null && dataScale.intValue() > 0) ddl.append(",").append(dataScale);
                    ddl.append(")");
                }

                if ("N".equals(col.get("nullable"))) ddl.append(" NOT NULL");
                Object defaultValue = col.get("data_default");
                if (defaultValue != null && !defaultValue.toString().isEmpty()) ddl.append(" DEFAULT ").append(defaultValue);
                if (i < columns.size() - 1) ddl.append(",");
                ddl.append("\n");
            }

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

    private String generateViewDDL(String viewName) {
        try {
            Map<String, Object> objectLocation = findObjectLocation(viewName, "VIEW");
            String owner = (String) objectLocation.get("owner");
            if (owner == null) owner = getCurrentUser();

            String viewText;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                String sql = "SELECT text FROM user_views WHERE UPPER(view_name) = UPPER(?)";
                viewText = getJdbcTemplate().queryForObject(sql, String.class, viewName);
            } else {
                String sql = "SELECT text FROM all_views WHERE UPPER(owner) = UPPER(?) AND UPPER(view_name) = UPPER(?)";
                viewText = getJdbcTemplate().queryForObject(sql, String.class, owner, viewName);
            }

            if (viewText != null && !viewText.isEmpty()) {
                StringBuilder ddl = new StringBuilder();
                ddl.append("CREATE OR REPLACE VIEW ");
                if (!owner.equalsIgnoreCase(getCurrentUser())) ddl.append(owner).append(".");
                ddl.append(viewName).append(" AS\n").append(viewText);
                return ddl.toString();
            }
            return null;
        } catch (Exception e) {
            log.debug("generateViewDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private boolean isSourceBasedObject(String objectType) {
        if (objectType == null) return false;
        String upperType = objectType.toUpperCase();
        return upperType.equals("PROCEDURE") || upperType.equals("FUNCTION") || upperType.equals("PACKAGE") ||
                upperType.equals("PACKAGE BODY") || upperType.equals("TYPE") || upperType.equals("TYPE BODY") ||
                upperType.equals("TRIGGER") || upperType.equals("JAVA SOURCE");
    }

    private String convertToMetadataObjectType(String objectType) {
        if (objectType == null) return null;
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "PACKAGE BODY": return "PACKAGE_BODY";
            case "TYPE BODY": return "TYPE_BODY";
            case "MATERIALIZED VIEW": return "MATERIALIZED_VIEW";
            case "DATABASE LINK": return "DB_LINK";
            case "PROCEDURE": case "FUNCTION": case "PACKAGE": case "TABLE":
            case "VIEW": case "TRIGGER": case "INDEX": case "SEQUENCE":
            case "SYNONYM": case "TYPE":
                return upperType;
            default: return upperType.replace(' ', '_');
        }
    }

    public Map<String, Object> findObjectLocation(String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT owner, object_name, status FROM all_objects " +
                    "WHERE UPPER(object_name) = UPPER(?) AND object_type = ? AND ROWNUM = 1";
            return getJdbcTemplate().queryForMap(sql, objectName, objectType);
        } catch (Exception e) {
            log.debug("Object {} of type {} not found", objectName, objectType);
            return result;
        }
    }

    private Map<String, Object> getObjectDiagnostics(String objectName, String objectType) {
        Map<String, Object> diagnostics = new HashMap<>();
        try {
            String checkSql = "SELECT owner, status, created, last_ddl_time FROM all_objects " +
                    "WHERE UPPER(object_name) = UPPER(?) AND UPPER(object_type) = UPPER(?)";
            try {
                Map<String, Object> objectInfo = getJdbcTemplate().queryForMap(checkSql, objectName, objectType);
                diagnostics.put("exists", true);
                diagnostics.put("owner", objectInfo.get("owner"));
                diagnostics.put("status", objectInfo.get("status"));
                diagnostics.put("created", objectInfo.get("created"));
                diagnostics.put("lastModified", objectInfo.get("last_ddl_time"));
            } catch (Exception e) {
                diagnostics.put("exists", false);
                diagnostics.put("message", "Object not found in all_objects");
            }
            diagnostics.put("currentUser", getCurrentUser());
            diagnostics.put("currentSchema", getCurrentSchema());

            // Check privileges
            String roleSql = "SELECT COUNT(*) FROM session_roles WHERE role = 'SELECT_CATALOG_ROLE'";
            Integer hasRole = getJdbcTemplate().queryForObject(roleSql, Integer.class);
            diagnostics.put("hasSelectCatalogRole", hasRole > 0);

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
        msg.append("-- 1. INSUFFICIENT PRIVILEGES: GRANT SELECT_CATALOG_ROLE TO ").append(getCurrentUser()).append(";\n");
        msg.append("-- 2. GRANT EXECUTE ON DBMS_METADATA TO ").append(getCurrentUser()).append(";\n");
        msg.append("-- 3. Try specifying the owner: OWNER.").append(objectName).append("\n");
        return msg.toString();
    }

    private List<Map<String, Object>> getTableColumns(String owner, String tableName) {
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT column_id, column_name, data_type, data_length, data_precision, data_scale, nullable, " +
                        "data_default FROM user_tab_columns WHERE UPPER(table_name) = UPPER(?) ORDER BY column_id";
                return getJdbcTemplate().queryForList(sql, tableName);
            } else {
                sql = "SELECT column_id, column_name, data_type, data_length, data_precision, data_scale, nullable, " +
                        "data_default FROM all_tab_columns WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) ORDER BY column_id";
                return getJdbcTemplate().queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getTableConstraints(String owner, String tableName) {
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT constraint_name, constraint_type, columns FROM user_constraints c " +
                        "LEFT JOIN (SELECT constraint_name, LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY position) as columns " +
                        "FROM user_cons_columns GROUP BY constraint_name) cc ON c.constraint_name = cc.constraint_name " +
                        "WHERE UPPER(table_name) = UPPER(?)";
                return getJdbcTemplate().queryForList(sql, tableName);
            } else {
                sql = "SELECT constraint_name, constraint_type, columns FROM all_constraints c " +
                        "LEFT JOIN (SELECT constraint_name, owner, LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY position) as columns " +
                        "FROM all_cons_columns GROUP BY constraint_name, owner) cc ON c.constraint_name = cc.constraint_name AND c.owner = cc.owner " +
                        "WHERE UPPER(c.owner) = UPPER(?) AND UPPER(c.table_name) = UPPER(?)";
                return getJdbcTemplate().queryForList(sql, owner, tableName);
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}