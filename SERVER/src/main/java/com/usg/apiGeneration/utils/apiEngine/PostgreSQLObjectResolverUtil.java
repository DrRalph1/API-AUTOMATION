package com.usg.apiGeneration.utils.apiEngine;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PostgreSQLObjectResolverUtil {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    /**
     * Resolve any PostgreSQL object (handles schema resolution)
     * @param schema The schema name (can be null)
     * @param objectName The object name
     * @param objectType The object type (PROCEDURE, FUNCTION, TABLE, VIEW, etc.)
     * @return Map containing resolution information
     */
    public Map<String, Object> resolveObject(String schema, String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();

        // Initialize result with default values
        result.put("originalOwner", schema);
        result.put("originalName", objectName);
        result.put("originalType", objectType);
        result.put("targetOwner", schema);
        result.put("targetName", objectName);
        result.put("targetType", objectType);
        result.put("isSynonym", false);  // PostgreSQL doesn't have synonyms
        result.put("exists", false);
        result.put("valid", true);  // PostgreSQL objects are always valid
        result.put("status", "VALID");
        result.put("dbLink", null);  // PostgreSQL doesn't have database links
        result.put("resolutionPath", new ArrayList<String>());

        try {
            log.info("🔍 Resolving object: {}.{} ({})", schema, objectName, objectType);

            // If schema is not provided, try to determine it
            if (schema == null || schema.isEmpty()) {
                schema = getCurrentSchema();
                result.put("originalOwner", schema);
                result.put("targetOwner", schema);
                log.info("Schema not provided, using current schema: {}", schema);
            }

            // Validate the object directly (PostgreSQL doesn't have synonyms)
            validateTargetObject(result, schema, objectName, objectType);

            log.info("✅ Resolved to: {}.{} ({}) with status: {}",
                    result.get("targetOwner"), result.get("targetName"),
                    result.get("targetType"), result.get("status"));

        } catch (Exception e) {
            log.error("Error resolving object {}.{}: {}", schema, objectName, e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get the type of an object
     */
    private String getObjectType(String schema, String objectName) {
        try {
            // Check tables, views, materialized views
            String relSql = "SELECT relkind FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ?";

            List<Map<String, Object>> objects = postgresqlJdbcTemplate.queryForList(relSql, schema, objectName);

            if (!objects.isEmpty()) {
                String relkind = (String) objects.get(0).get("relkind");
                switch (relkind) {
                    case "r": return "TABLE";
                    case "v": return "VIEW";
                    case "m": return "MATERIALIZED VIEW";
                    case "f": return "FUNCTION";
                    case "p": return "PROCEDURE";
                    case "S": return "SEQUENCE";
                    case "i": return "INDEX";
                    default: return "UNKNOWN";
                }
            }

            // Check functions/procedures
            String procSql = "SELECT prokind FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            List<Map<String, Object>> procs = postgresqlJdbcTemplate.queryForList(procSql, schema, objectName);

            if (!procs.isEmpty()) {
                String prokind = (String) procs.get(0).get("prokind");
                if ("f".equals(prokind)) return "FUNCTION";
                if ("p".equals(prokind)) return "PROCEDURE";
            }

        } catch (Exception e) {
            log.debug("Could not determine object type for {}.{}: {}", schema, objectName, e.getMessage());
        }
        return "UNKNOWN";
    }

    /**
     * Validate the target object and add parameter/column counts
     */
    private void validateTargetObject(Map<String, Object> result, String schema, String objectName, String objectType) {
        try {
            boolean exists = false;
            String upperType = objectType.toUpperCase();

            switch (upperType) {
                case "TABLE":
                    exists = tableExists(schema, objectName);
                    if (exists) {
                        addColumnCounts(schema, objectName, result);
                    }
                    break;
                case "VIEW":
                    exists = viewExists(schema, objectName);
                    if (exists) {
                        addColumnCounts(schema, objectName, result);
                    }
                    break;
                case "MATERIALIZED VIEW":
                    exists = materializedViewExists(schema, objectName);
                    if (exists) {
                        addColumnCounts(schema, objectName, result);
                    }
                    break;
                case "FUNCTION":
                    exists = functionExists(schema, objectName);
                    if (exists) {
                        addParameterCounts(schema, objectName, result);
                    }
                    break;
                case "PROCEDURE":
                    exists = procedureExists(schema, objectName);
                    if (exists) {
                        addParameterCounts(schema, objectName, result);
                    }
                    break;
                case "SEQUENCE":
                    exists = sequenceExists(schema, objectName);
                    break;
                default:
                    exists = objectExists(schema, objectName, upperType);
                    break;
            }

            if (exists) {
                result.put("exists", true);
                result.put("status", "VALID");
                result.put("valid", true);

                log.info("✅ Found {}.{} ({})", schema, objectName, objectType);
            } else {
                log.warn("❌ {}.{} ({}) not found", schema, objectName, objectType);
            }

        } catch (Exception e) {
            log.error("Error validating target object {}.{}: {}", schema, objectName, e.getMessage());
            result.put("validationError", e.getMessage());
        }
    }

    /**
     * Check if a table exists
     */
    private boolean tableExists(String schema, String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = ? AND table_name = ? AND table_type = 'BASE TABLE'";
            Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a view exists
     */
    private boolean viewExists(String schema, String viewName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.views " +
                    "WHERE table_schema = ? AND table_name = ?";
            Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, viewName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a materialized view exists
     */
    private boolean materializedViewExists(String schema, String mvName) {
        try {
            String sql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'm'";
            Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, mvName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a function exists
     */
    private boolean functionExists(String schema, String functionName) {
        try {
            String sql = "SELECT COUNT(*) FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'f'";
            Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, functionName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a procedure exists
     */
    private boolean procedureExists(String schema, String procedureName) {
        try {
            String sql = "SELECT COUNT(*) FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'p'";
            Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, procedureName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a sequence exists
     */
    private boolean sequenceExists(String schema, String sequenceName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.sequences " +
                    "WHERE sequence_schema = ? AND sequence_name = ?";
            Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, sequenceName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a generic object exists
     */
    private boolean objectExists(String schema, String objectName, String objectType) {
        try {
            String relKind = getRelationKind(objectType);
            if (relKind != null) {
                String sql = "SELECT COUNT(*) FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = ?";
                Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, objectName, relKind);
                return count != null && count > 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Add parameter counts for functions/procedures
     */
    private void addParameterCounts(String schema, String objectName, Map<String, Object> result) {
        try {
            // Get function/procedure OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            Long oid = null;
            try {
                oid = postgresqlJdbcTemplate.queryForObject(oidSql, Long.class, schema, objectName);
            } catch (EmptyResultDataAccessException e) {
                log.debug("Could not find OID for {}.{}", schema, objectName);
                return;
            }

            if (oid != null) {
                // Get argument information
                String argsSql = "SELECT proargnames, proargmodes FROM pg_proc WHERE oid = ?";
                Map<String, Object> procInfo = postgresqlJdbcTemplate.queryForMap(argsSql, oid);

                java.sql.Array proargnames = (java.sql.Array) procInfo.get("proargnames");
                java.sql.Array proargmodes = (java.sql.Array) procInfo.get("proargmodes");

                String[] argNames = proargnames != null ? (String[]) proargnames.getArray() : new String[0];
                String[] argModes = proargmodes != null ? (String[]) proargmodes.getArray() : new String[0];

                int totalParams = argNames.length;
                int inParams = 0;
                int outParams = 0;

                for (String mode : argModes) {
                    if ("i".equals(mode)) {
                        inParams++;
                    } else if ("o".equals(mode)) {
                        outParams++;
                    } else if ("b".equals(mode)) {
                        // INOUT counts as both
                        inParams++;
                        outParams++;
                    }
                }

                result.put("totalParameters", totalParams);
                result.put("inParameters", inParams);
                result.put("outParameters", outParams);

                log.info("{} {}.{} has {} total parameters ({} IN, {} OUT)",
                        result.get("targetType"), schema, objectName, totalParams, inParams, outParams);
            }

        } catch (Exception e) {
            log.debug("Could not get parameter counts for {}.{}: {}", schema, objectName, e.getMessage());
        }
    }

    /**
     * Add column counts for tables/views
     */
    private void addColumnCounts(String schema, String objectName, Map<String, Object> result) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ?";
            Integer columnCount = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schema, objectName);
            result.put("columnCount", columnCount != null ? columnCount : 0);

            // Get primary key columns
            String pkSql = "SELECT COUNT(*) FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage kcu " +
                    "ON tc.constraint_name = kcu.constraint_name " +
                    "WHERE tc.constraint_type = 'PRIMARY KEY' " +
                    "AND tc.table_schema = ? AND tc.table_name = ?";

            Integer pkCount = postgresqlJdbcTemplate.queryForObject(pkSql, Integer.class, schema, objectName);
            result.put("primaryKeyCount", pkCount != null ? pkCount : 0);

            log.info("Table {}.{} has {} columns ({} primary keys)",
                    schema, objectName, columnCount, pkCount);

        } catch (Exception e) {
            log.debug("Could not get column counts for {}.{}: {}", schema, objectName, e.getMessage());
        }
    }

    /**
     * Validate a function exists and is valid
     */
    public void validateFunction(String schemaName, String functionName) {
        log.info("Validating function: {}.{}", schemaName, functionName);

        try {
            boolean exists = functionExists(schemaName, functionName);

            if (!exists) {
                throw new ValidationException(
                        String.format("The function '%s.%s' does not exist or you don't have access to it.",
                                schemaName, functionName)
                );
            }

            log.info("✅ Function {}.{} exists", schemaName, functionName);

        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("The function '%s.%s' does not exist or you don't have access to it.",
                            schemaName, functionName)
            );
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating function {}.{}: {}", schemaName, functionName, e.getMessage());
            throw new ValidationException(
                    String.format("Failed to validate function '%s.%s': %s",
                            schemaName, functionName, e.getMessage())
            );
        }
    }

    /**
     * Validate a procedure exists and is valid
     */
    public void validateProcedure(String schemaName, String procedureName) {
        log.info("Validating procedure: {}.{}", schemaName, procedureName);

        try {
            boolean exists = procedureExists(schemaName, procedureName);

            if (!exists) {
                throw new ValidationException(
                        String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                                schemaName, procedureName)
                );
            }

            log.info("✅ Procedure {}.{} exists", schemaName, procedureName);

        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                            schemaName, procedureName)
            );
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating procedure {}.{}: {}", schemaName, procedureName, e.getMessage());
            throw new ValidationException(
                    String.format("Failed to validate procedure '%s.%s': %s",
                            schemaName, procedureName, e.getMessage())
            );
        }
    }

    /**
     * Validate a table exists and is accessible
     */
    public void validateTable(String schemaName, String tableName) {
        log.info("Validating table: {}.{}", schemaName, tableName);

        try {
            boolean exists = tableExists(schemaName, tableName);

            if (!exists) {
                throw new ValidationException(
                        String.format("Table '%s.%s' does not exist or you don't have access to it.",
                                schemaName, tableName)
                );
            }

            log.info("✅ Table {}.{} exists", schemaName, tableName);

        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("Table '%s.%s' does not exist or you don't have access to it.",
                            schemaName, tableName)
            );
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating table {}.{}: {}", schemaName, tableName, e.getMessage());
            throw new ValidationException(
                    String.format("Failed to validate table '%s.%s': %s",
                            schemaName, tableName, e.getMessage())
            );
        }
    }

    /**
     * Validate a view exists and is accessible
     */
    public void validateView(String schemaName, String viewName) {
        log.info("Validating view: {}.{}", schemaName, viewName);

        try {
            boolean exists = viewExists(schemaName, viewName);

            if (!exists) {
                throw new ValidationException(
                        String.format("The view '%s.%s' does not exist or you don't have access to it.",
                                schemaName, viewName)
                );
            }

            // Additional validation for views - check if it's accessible
            try {
                postgresqlJdbcTemplate.execute("SELECT 1 FROM " + schemaName + "." + viewName + " LIMIT 1");
                log.info("✅ View {}.{} is accessible", schemaName, viewName);
            } catch (Exception e) {
                throw new ValidationException(
                        String.format("The view '%s.%s' is invalid or inaccessible. Error: %s",
                                schemaName, viewName, e.getMessage())
                );
            }

        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("The view '%s.%s' does not exist or you don't have access to it.",
                            schemaName, viewName)
            );
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating view {}.{}: {}", schemaName, viewName, e.getMessage());
            throw new ValidationException(
                    String.format("Failed to validate view '%s.%s': %s",
                            schemaName, viewName, e.getMessage())
            );
        }
    }

    /**
     * Validate a generic object
     */
    private void validateGenericObject(String schema, String objectName, String objectType) {
        boolean exists = objectExists(schema, objectName, objectType);

        if (!exists) {
            throw new ValidationException(
                    String.format("%s '%s.%s' does not exist", objectType, schema, objectName)
            );
        }

        log.info("✅ {}.{} exists", objectName, objectType);
    }

    /**
     * Validate a database object (delegates to specific methods based on type)
     */
    public void validateDatabaseObject(String schemaName, String objectName, String objectType) {
        switch (objectType.toUpperCase()) {
            case "PROCEDURE":
                validateProcedure(schemaName, objectName);
                break;
            case "FUNCTION":
                validateFunction(schemaName, objectName);
                break;
            case "TABLE":
                validateTable(schemaName, objectName);
                break;
            case "VIEW":
                validateView(schemaName, objectName);
                break;
            case "MATERIALIZED VIEW":
                validateMaterializedView(schemaName, objectName);
                break;
            case "SEQUENCE":
                validateSequence(schemaName, objectName);
                break;
            default:
                validateGenericObject(schemaName, objectName, objectType);
                break;
        }
    }

    /**
     * Validate a materialized view
     */
    public void validateMaterializedView(String schemaName, String mvName) {
        log.info("Validating materialized view: {}.{}", schemaName, mvName);

        try {
            boolean exists = materializedViewExists(schemaName, mvName);

            if (!exists) {
                throw new ValidationException(
                        String.format("The materialized view '%s.%s' does not exist or you don't have access to it.",
                                schemaName, mvName)
                );
            }

            // Check if it's accessible
            try {
                postgresqlJdbcTemplate.execute("SELECT 1 FROM " + schemaName + "." + mvName + " LIMIT 1");
                log.info("✅ Materialized view {}.{} is accessible", schemaName, mvName);
            } catch (Exception e) {
                throw new ValidationException(
                        String.format("The materialized view '%s.%s' is invalid or inaccessible. Error: %s",
                                schemaName, mvName, e.getMessage())
                );
            }

        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("The materialized view '%s.%s' does not exist or you don't have access to it.",
                            schemaName, mvName)
            );
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating materialized view {}.{}: {}", schemaName, mvName, e.getMessage());
            throw new ValidationException(
                    String.format("Failed to validate materialized view '%s.%s': %s",
                            schemaName, mvName, e.getMessage())
            );
        }
    }

    /**
     * Validate a sequence
     */
    public void validateSequence(String schemaName, String sequenceName) {
        log.info("Validating sequence: {}.{}", schemaName, sequenceName);

        try {
            boolean exists = sequenceExists(schemaName, sequenceName);

            if (!exists) {
                throw new ValidationException(
                        String.format("The sequence '%s.%s' does not exist or you don't have access to it.",
                                schemaName, sequenceName)
                );
            }

            log.info("✅ Sequence {}.{} exists", schemaName, sequenceName);

        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("The sequence '%s.%s' does not exist or you don't have access to it.",
                            schemaName, sequenceName)
            );
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating sequence {}.{}: {}", schemaName, sequenceName, e.getMessage());
            throw new ValidationException(
                    String.format("Failed to validate sequence '%s.%s': %s",
                            schemaName, sequenceName, e.getMessage())
            );
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get the current schema name
     */
    public String getCurrentSchema() {
        try {
            return postgresqlJdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        } catch (Exception e) {
            log.error("Error getting current schema: {}", e.getMessage());
            return "public";
        }
    }

    /**
     * Get the current user name
     */
    public String getCurrentUser() {
        try {
            return postgresqlJdbcTemplate.queryForObject("SELECT current_user", String.class);
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get the relation kind for an object type
     */
    private String getRelationKind(String objectType) {
        if (objectType == null) return null;
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "TABLE": return "r";
            case "VIEW": return "v";
            case "MATERIALIZED VIEW": return "m";
            case "SEQUENCE": return "S";
            case "INDEX": return "i";
            default: return null;
        }
    }

    /**
     * Resolve a procedure target (no synonyms in PostgreSQL)
     */
    public Map<String, Object> resolveProcedureTarget(String schema, String procedureName) {
        return resolveObject(schema, procedureName, "PROCEDURE");
    }

    /**
     * Resolve a function target
     */
    public Map<String, Object> resolveFunctionTarget(String schema, String functionName) {
        return resolveObject(schema, functionName, "FUNCTION");
    }

    /**
     * Resolve a table target
     */
    public Map<String, Object> resolveTableTarget(String schema, String tableName) {
        return resolveObject(schema, tableName, "TABLE");
    }

    /**
     * Resolve a view target
     */
    public Map<String, Object> resolveViewTarget(String schema, String viewName) {
        return resolveObject(schema, viewName, "VIEW");
    }
}