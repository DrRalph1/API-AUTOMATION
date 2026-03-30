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
public class OracleObjectResolverUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    /**
     * Resolve any Oracle object, handling synonyms and returning complete resolution info
     * @param owner The schema owner (can be null)
     * @param objectName The object name
     * @param objectType The object type (PROCEDURE, FUNCTION, TABLE, VIEW, etc.)
     * @return Map containing resolution information
     */
    public Map<String, Object> resolveObject(String owner, String objectName, String objectType) {
        Map<String, Object> result = new HashMap<>();

        // Initialize result with default values
        result.put("originalOwner", owner);
        result.put("originalName", objectName);
        result.put("originalType", objectType);
        result.put("targetOwner", owner);
        result.put("targetName", objectName);
        result.put("targetType", objectType);
        result.put("isSynonym", false);
        result.put("exists", false);
        result.put("valid", false);
        result.put("status", "UNKNOWN");
        result.put("dbLink", null);
        result.put("resolutionPath", new ArrayList<String>());

        try {
            log.info("🔍 Resolving object: {}.{} ({})", owner, objectName, objectType);

            // If owner is not provided, try to determine it
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
                result.put("originalOwner", owner);
                result.put("targetOwner", owner);
                log.info("Owner not provided, using current schema: {}", owner);
            }

            // First, check if it's a synonym
            Map<String, Object> synonymResolution = resolveSynonym(owner, objectName, objectType);

            if ((boolean) synonymResolution.getOrDefault("isSynonym", false)) {
                // It's a synonym, use the resolved target
                result.put("isSynonym", true);
                result.put("synonymOwner", synonymResolution.get("synonymOwner"));
                result.put("targetOwner", synonymResolution.get("targetOwner"));
                result.put("targetName", synonymResolution.get("targetName"));
                result.put("targetType", synonymResolution.get("targetType"));
                result.put("dbLink", synonymResolution.get("dbLink"));
                result.put("resolutionPath", synonymResolution.get("resolutionPath"));

                // Now validate the target object
                validateTargetObject(result,
                        (String) result.get("targetOwner"),
                        (String) result.get("targetName"),
                        (String) result.get("targetType"));
            } else {
                // Not a synonym, validate the object directly
                validateTargetObject(result, owner, objectName, objectType);
            }

            log.info("✅ Resolved to: {}.{} ({}) with status: {}",
                    result.get("targetOwner"), result.get("targetName"),
                    result.get("targetType"), result.get("status"));

        } catch (Exception e) {
            log.error("Error resolving object {}.{}: {}", owner, objectName, e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Resolve a synonym to its target (handles chains of synonyms)
     */
    private Map<String, Object> resolveSynonym(String owner, String synonymName, String expectedTargetType) {
        Map<String, Object> result = new HashMap<>();
        result.put("isSynonym", false);

        try {
            List<Map<String, Object>> synonyms = new ArrayList<>();
            List<String> resolutionPath = new ArrayList<>();
            resolutionPath.add(owner + "." + synonymName + " (SYNONYM)");

            // Track the current resolution level
            String currentOwner = owner;
            String currentName = synonymName;
            boolean continueResolution = true;
            int maxDepth = 10; // Prevent infinite loops
            int depth = 0;

            while (continueResolution && depth < maxDepth) {
                depth++;

                // Try to find synonym with current owner
                String sql = "SELECT OWNER, TABLE_OWNER, TABLE_NAME, DB_LINK " +
                        "FROM ALL_SYNONYMS " +
                        "WHERE OWNER = ? AND SYNONYM_NAME = ?";

                synonyms = oracleJdbcTemplate.queryForList(sql, currentOwner, currentName);

                // If not found, try PUBLIC synonym
                if (synonyms.isEmpty()) {
                    sql = "SELECT OWNER, TABLE_OWNER, TABLE_NAME, DB_LINK " +
                            "FROM ALL_SYNONYMS " +
                            "WHERE OWNER = 'PUBLIC' AND SYNONYM_NAME = ?";
                    synonyms = oracleJdbcTemplate.queryForList(sql, currentName);
                    if (!synonyms.isEmpty()) {
                        resolutionPath.add("PUBLIC." + currentName + " (SYNONYM)");
                    }
                }

                if (synonyms.isEmpty()) {
                    // No more synonyms found, stop resolution
                    continueResolution = false;
                } else {
                    Map<String, Object> synonym = synonyms.get(0);
                    String synonymOwner = (String) synonym.get("OWNER");
                    String targetOwner = (String) synonym.get("TABLE_OWNER");
                    String targetName = (String) synonym.get("TABLE_NAME");
                    String dbLink = (String) synonym.get("DB_LINK");

                    resolutionPath.add("→ " + targetOwner + "." + targetName);

                    // Check if this target is itself a synonym
                    String checkSynonymSql = "SELECT COUNT(*) FROM ALL_SYNONYMS " +
                            "WHERE OWNER = ? AND SYNONYM_NAME = ?";
                    Integer count = oracleJdbcTemplate.queryForObject(
                            checkSynonymSql, Integer.class, targetOwner, targetName);

                    if (count != null && count > 0) {
                        // Target is another synonym, continue resolution
                        currentOwner = targetOwner;
                        currentName = targetName;
                        log.info("Chain resolution: {} -> {}.{} (another synonym)",
                                synonymName, targetOwner, targetName);
                    } else {
                        // Reached final target
                        result.put("isSynonym", true);
                        result.put("synonymOwner", synonymOwner);
                        result.put("targetOwner", targetOwner);
                        result.put("targetName", targetName);
                        result.put("dbLink", dbLink);

                        // Determine target type
                        String targetType = getObjectType(targetOwner, targetName);
                        result.put("targetType", targetType);

                        continueResolution = false;
                    }
                }
            }

            result.put("resolutionPath", resolutionPath);

            if (depth >= maxDepth) {
                log.warn("Synonym resolution exceeded max depth for {}.{}", owner, synonymName);
                result.put("error", "Synonym resolution exceeded maximum depth (possible circular reference)");
            }

        } catch (Exception e) {
            log.error("Error resolving synonym {}.{}: {}", owner, synonymName, e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get the type of an object
     */
    private String getObjectType(String owner, String objectName) {
        try {
            String sql = "SELECT OBJECT_TYPE FROM ALL_OBJECTS " +
                    "WHERE OWNER = ? AND OBJECT_NAME = ?";
            List<Map<String, Object>> objects = oracleJdbcTemplate.queryForList(sql, owner, objectName);

            if (!objects.isEmpty()) {
                return (String) objects.get(0).get("OBJECT_TYPE");
            }
        } catch (Exception e) {
            log.debug("Could not determine object type for {}.{}: {}", owner, objectName, e.getMessage());
        }
        return "UNKNOWN";
    }

    /**
     * Validate the target object and add parameter/column counts
     */
    private void validateTargetObject(Map<String, Object> result, String owner, String objectName, String objectType) {
        try {
            String sql = "SELECT STATUS FROM ALL_OBJECTS " +
                    "WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = ?";

            List<Map<String, Object>> objects = oracleJdbcTemplate.queryForList(
                    sql, owner, objectName, objectType);

            if (!objects.isEmpty()) {
                result.put("exists", true);
                String status = (String) objects.get(0).get("STATUS");
                result.put("status", status);
                result.put("valid", "VALID".equalsIgnoreCase(status));

                log.info("✅ Found {}.{} ({}) with status: {}",
                        owner, objectName, objectType, status);

                // Add parameter/column counts based on object type
                switch (objectType.toUpperCase()) {
                    case "PROCEDURE":
                    case "FUNCTION":
                        addParameterCounts(owner, objectName, result);
                        break;
                    case "TABLE":
                    case "VIEW":
                        addColumnCounts(owner, objectName, result);
                        break;
                }
            } else {
                log.warn("❌ {}.{} ({}) not found", owner, objectName, objectType);
            }

        } catch (Exception e) {
            log.error("Error validating target object {}.{}: {}", owner, objectName, e.getMessage());
            result.put("validationError", e.getMessage());
        }
    }

    /**
     * Add parameter counts for procedures/functions
     */
    private void addParameterCounts(String owner, String objectName, Map<String, Object> result) {
        try {
            // Total parameters
            String totalSql = "SELECT COUNT(*) FROM ALL_ARGUMENTS " +
                    "WHERE OWNER = ? AND OBJECT_NAME = ? AND DATA_LEVEL = 0";
            Integer totalParams = oracleJdbcTemplate.queryForObject(totalSql, Integer.class, owner, objectName);
            result.put("totalParameters", totalParams != null ? totalParams : 0);

            // IN parameters
            String inSql = "SELECT COUNT(*) FROM ALL_ARGUMENTS " +
                    "WHERE OWNER = ? AND OBJECT_NAME = ? AND DATA_LEVEL = 0 " +
                    "AND IN_OUT IN ('IN', 'IN/OUT')";
            Integer inParams = oracleJdbcTemplate.queryForObject(inSql, Integer.class, owner, objectName);
            result.put("inParameters", inParams != null ? inParams : 0);

            // OUT parameters
            String outSql = "SELECT COUNT(*) FROM ALL_ARGUMENTS " +
                    "WHERE OWNER = ? AND OBJECT_NAME = ? AND DATA_LEVEL = 0 " +
                    "AND IN_OUT IN ('OUT', 'IN/OUT')";
            Integer outParams = oracleJdbcTemplate.queryForObject(outSql, Integer.class, owner, objectName);
            result.put("outParameters", outParams != null ? outParams : 0);

            log.info("{} {}.{} has {} total parameters ({} IN, {} OUT)",
                    objectName, owner, objectName, totalParams, inParams, outParams);

        } catch (Exception e) {
            log.debug("Could not get parameter counts for {}.{}: {}", owner, objectName, e.getMessage());
        }
    }

    /**
     * Add column counts for tables/views
     */
    private void addColumnCounts(String owner, String objectName, Map<String, Object> result) {
        try {
            String sql = "SELECT COUNT(*) FROM ALL_TAB_COLUMNS " +
                    "WHERE OWNER = ? AND TABLE_NAME = ?";
            Integer columnCount = oracleJdbcTemplate.queryForObject(sql, Integer.class, owner, objectName);
            result.put("columnCount", columnCount != null ? columnCount : 0);

            // Get primary key columns
            String pkSql = "SELECT COUNT(DISTINCT cols.column_name) " +
                    "FROM ALL_CONSTRAINTS cons, ALL_CONS_COLUMNS cols " +
                    "WHERE cons.CONSTRAINT_TYPE = 'P' " +
                    "AND cons.OWNER = ? " +
                    "AND cons.TABLE_NAME = ? " +
                    "AND cons.OWNER = cols.OWNER " +
                    "AND cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME " +
                    "AND cols.TABLE_NAME = ?";

            Integer pkCount = oracleJdbcTemplate.queryForObject(
                    pkSql, Integer.class, owner, objectName, objectName);
            result.put("primaryKeyCount", pkCount != null ? pkCount : 0);

            log.info("Table {}.{} has {} columns ({} primary keys)",
                    owner, objectName, columnCount, pkCount);

        } catch (Exception e) {
            log.debug("Could not get column counts for {}.{}: {}", owner, objectName, e.getMessage());
        }
    }

    /**
     * Validate a package procedure exists and is valid
     * @param schemaName The schema/owner name
     * @param packageName The package name
     * @param procedureName The procedure name
     * @throws ValidationException if the package procedure doesn't exist or is invalid
     */
    public void validatePackageProcedure(String schemaName, String packageName, String procedureName) {
        log.info("Validating package procedure: {}.{}.{}", schemaName, packageName, procedureName);

        try {
            // First, check if the package exists and is valid
            String packageSql = "SELECT STATUS FROM ALL_OBJECTS " +
                    "WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = 'PACKAGE'";

            String packageStatus = oracleJdbcTemplate.queryForObject(packageSql, String.class, schemaName, packageName);
            log.info("Package {}.{} status: {}", schemaName, packageName, packageStatus);

            if ("INVALID".equalsIgnoreCase(packageStatus)) {
                throw new ValidationException(
                        String.format("The package '%s.%s' exists but is INVALID (compilation error). Please check the database for compilation errors and recompile it.",
                                schemaName, packageName)
                );
            }

            // Then, check if the procedure exists within the package
            String procedureSql = "SELECT COUNT(*) FROM ALL_PROCEDURES " +
                    "WHERE OWNER = ? AND OBJECT_NAME = ? AND PROCEDURE_NAME = ?";

            Integer count = oracleJdbcTemplate.queryForObject(procedureSql, Integer.class,
                    schemaName, packageName, procedureName);

            if (count == null || count == 0) {
                throw new ValidationException(
                        String.format("The procedure '%s.%s.%s' does not exist or you don't have access to it.",
                                schemaName, packageName, procedureName)
                );
            }

            log.info("✅ Package procedure {}.{}.{} exists", schemaName, packageName, procedureName);

            // Optional: Check if the procedure has compilation errors
            String errorSql = "SELECT COUNT(*) FROM ALL_ERRORS " +
                    "WHERE OWNER = ? AND NAME = ? AND TYPE = 'PACKAGE' AND SEQUENCE > 0";

            Integer errorCount = oracleJdbcTemplate.queryForObject(errorSql, Integer.class,
                    schemaName, packageName);

            if (errorCount != null && errorCount > 0) {
                log.warn("⚠️ Package {}.{} has {} compilation errors. Procedure may be invalid.",
                        schemaName, packageName, errorCount);

                // Get the first few errors for logging
                String errorDetailSql = "SELECT LINE, TEXT FROM ALL_ERRORS " +
                        "WHERE OWNER = ? AND NAME = ? AND TYPE = 'PACKAGE' AND SEQUENCE > 0 " +
                        "ORDER BY SEQUENCE FETCH FIRST 5 ROWS ONLY";

                List<Map<String, Object>> errors = oracleJdbcTemplate.queryForList(errorDetailSql,
                        schemaName, packageName);

                if (!errors.isEmpty()) {
                    StringBuilder errorMsg = new StringBuilder();
                    errorMsg.append(String.format("Package %s.%s has compilation errors: ",
                            schemaName, packageName));
                    for (Map<String, Object> error : errors) {
                        errorMsg.append(String.format(" Line %s: %s; ",
                                error.get("LINE"), error.get("TEXT")));
                    }
                    log.warn(errorMsg.toString());
                }
            }

        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("The package '%s.%s' does not exist or you don't have access to it.",
                            schemaName, packageName)
            );
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating package procedure {}.{}.{}: {}",
                    schemaName, packageName, procedureName, e.getMessage());
            throw new ValidationException(
                    String.format("Failed to validate package procedure '%s.%s.%s': %s",
                            schemaName, packageName, procedureName, e.getMessage())
            );
        }
    }

    /**
     * Resolve a package procedure target (handle synonyms for packages and procedures)
     * @param owner The schema owner
     * @param packageName The package name
     * @param procedureName The procedure name
     * @return Map containing resolution information
     */
    public Map<String, Object> resolvePackageProcedureTarget(String owner, String packageName, String procedureName) {
        Map<String, Object> result = new HashMap<>();

        result.put("originalOwner", owner);
        result.put("originalPackageName", packageName);
        result.put("originalProcedureName", procedureName);
        result.put("targetOwner", owner);
        result.put("targetPackageName", packageName);
        result.put("targetProcedureName", procedureName);
        result.put("isSynonym", false);

        try {
            // First, check if the package itself is a synonym
            Map<String, Object> packageResolution = resolveObject(owner, packageName, "PACKAGE");

            if ((boolean) packageResolution.getOrDefault("isSynonym", false)) {
                result.put("isSynonym", true);
                result.put("targetOwner", packageResolution.get("targetOwner"));
                result.put("targetPackageName", packageResolution.get("targetName"));
                result.put("targetProcedureName", procedureName);

                log.info("✅ Resolved package synonym: {}.{} -> {}.{}",
                        owner, packageName, result.get("targetOwner"), result.get("targetPackageName"));
            }

            // Validate the resolved package procedure
            validatePackageProcedure(
                    (String) result.get("targetOwner"),
                    (String) result.get("targetPackageName"),
                    (String) result.get("targetProcedureName")
            );

        } catch (Exception e) {
            log.error("Error resolving package procedure {}.{}.{}: {}",
                    owner, packageName, procedureName, e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ==================== EXISTING METHODS (keep as is) ====================

    public void validateDatabaseObject(String schemaName, String objectName, String objectType) {
        // Your existing implementation - keep unchanged
        String sql = "";

        switch(objectType.toUpperCase()) {
            case "PROCEDURE":
                sql = "SELECT STATUS FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = 'PROCEDURE'";
                try {
                    String status = oracleJdbcTemplate.queryForObject(sql, String.class, schemaName, objectName);

                    log.info("Procedure {}.{} status: {}", schemaName, objectName, status);

                    if ("INVALID".equalsIgnoreCase(status)) {
                        throw new ValidationException(
                                String.format("The procedure '%s.%s' exists but is INVALID (compilation error). Please check the database for compilation errors and recompile it.",
                                        schemaName, objectName)
                        );
                    }

                    log.info("✅ Procedure {}.{} is {}", schemaName, objectName, status);

                } catch (EmptyResultDataAccessException e) {
                    throw new ValidationException(
                            String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                                    schemaName, objectName)
                    );
                }
                break;

            case "FUNCTION":
                sql = "SELECT STATUS FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = 'FUNCTION'";
                try {
                    String status = oracleJdbcTemplate.queryForObject(sql, String.class, schemaName, objectName);

                    log.info("Function {}.{} status: {}", schemaName, objectName, status);

                    if ("INVALID".equalsIgnoreCase(status)) {
                        throw new ValidationException(
                                String.format("The function '%s.%s' exists but is INVALID (compilation error). Please check the database for compilation errors and recompile it.",
                                        schemaName, objectName)
                        );
                    }

                    log.info("✅ Function {}.{} is {}", schemaName, objectName, status);

                } catch (EmptyResultDataAccessException e) {
                    throw new ValidationException(
                            String.format("The function '%s.%s' does not exist or you don't have access to it.",
                                    schemaName, objectName)
                    );
                }
                break;

            case "TABLE":
                sql = "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME = ?";
                try {
                    Integer count = oracleJdbcTemplate.queryForObject(sql, Integer.class, schemaName, objectName);
                    if (count == 0) {
                        throw new ValidationException(
                                String.format("Table '%s.%s' does not exist or you don't have access to it.",
                                        schemaName, objectName)
                        );
                    }
                    log.info("✅ Table {}.{} exists", schemaName, objectName);

                } catch (EmptyResultDataAccessException e) {
                    throw new ValidationException(
                            String.format("Table '%s.%s' does not exist or you don't have access to it.",
                                    schemaName, objectName)
                    );
                }
                break;

            case "VIEW":
                String viewSql = "SELECT COUNT(*) FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME = ?";
                try {
                    Integer count = oracleJdbcTemplate.queryForObject(viewSql, Integer.class, schemaName, objectName);
                    if (count == 0) {
                        throw new ValidationException(
                                String.format("The view '%s.%s' does not exist or you don't have access to it.",
                                        schemaName, objectName)
                        );
                    }

                    // Additional validation for views - check if it's accessible
                    try {
                        oracleJdbcTemplate.execute("SELECT 1 FROM " + schemaName + "." + objectName + " WHERE ROWNUM = 1");
                        log.info("✅ View {}.{} is accessible", schemaName, objectName);
                    } catch (Exception e) {
                        throw new ValidationException(
                                String.format("The view '%s.%s' is invalid or inaccessible. Error: %s",
                                        schemaName, objectName, e.getMessage())
                        );
                    }

                } catch (EmptyResultDataAccessException e) {
                    throw new ValidationException(
                            String.format("The view '%s.%s' does not exist or you don't have access to it.",
                                    schemaName, objectName)
                    );
                }
                break;

            default:
                validateGenericObject(schemaName, objectName, objectType);
                break;
        }
    }

    public Map<String, Object> resolveSynonymTarget(String owner, String synonymName) {
        // Keep your existing implementation or delegate to resolveObject
        return resolveObject(owner, synonymName, "SYNONYM");
    }

    public Map<String, Object> resolveProcedureTarget(String owner, String procedureName) {
        // Keep your existing implementation or delegate to resolveObject
        return resolveObject(owner, procedureName, "PROCEDURE");
    }

    private void validateGenericObject(String owner, String objectName, String objectType) {
        String sql = "SELECT COUNT(*) FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = ?";
        try {
            Integer count = oracleJdbcTemplate.queryForObject(sql, Integer.class, owner, objectName, objectType);
            if (count == 0) {
                throw new ValidationException(
                        String.format("%s '%s.%s' does not exist", objectType, owner, objectName)
                );
            }

            // Check status if available
            try {
                String statusSql = "SELECT STATUS FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = ?";
                String status = oracleJdbcTemplate.queryForObject(statusSql, String.class, owner, objectName, objectType);

                if ("INVALID".equalsIgnoreCase(status)) {
                    throw new ValidationException(
                            String.format("%s '%s.%s' is INVALID. Please compile it before using.",
                                    objectType, owner, objectName)
                    );
                }
            } catch (Exception e) {
                // Status might not be available for this object type
                log.debug("Could not check status for {} {}.{}", objectType, owner, objectName);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(
                    String.format("%s '%s.%s' does not exist", objectType, owner, objectName)
            );
        }
    }

    /**
     * Get the current schema name
     */
    public String getCurrentSchema() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL", String.class);
        } catch (Exception e) {
            log.error("Error getting current schema: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get the current user name
     */
    public String getCurrentUser() {
        try {
            return oracleJdbcTemplate.queryForObject("SELECT USER FROM DUAL", String.class);
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return null;
        }
    }
}