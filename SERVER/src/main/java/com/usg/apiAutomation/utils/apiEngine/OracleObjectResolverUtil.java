package com.usg.apiAutomation.utils.apiEngine;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OracleObjectResolverUtil {

    private final JdbcTemplate oracleJdbcTemplate;

    public void validateDatabaseObject(String schemaName, String objectName, String objectType) {
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
        Map<String, Object> result = new HashMap<>();
        result.put("exists", false);
        result.put("targetValid", false);

        try {
            // Try multiple approaches to find the synonym
            List<Map<String, Object>> synonyms = new java.util.ArrayList<>();

            // Approach 1: Check with the provided owner
            if (owner != null && !owner.isEmpty()) {
                String sql1 = "SELECT TABLE_OWNER, TABLE_NAME, DB_LINK FROM ALL_SYNONYMS WHERE OWNER = ? AND SYNONYM_NAME = ?";
                synonyms = oracleJdbcTemplate.queryForList(sql1, owner, synonymName);
                log.info("Checked synonym under owner: {}, found: {}", owner, synonyms.size());
            }

            // Approach 2: If not found, check PUBLIC synonyms
            if (synonyms.isEmpty()) {
                String sql2 = "SELECT TABLE_OWNER, TABLE_NAME, DB_LINK FROM ALL_SYNONYMS WHERE OWNER = 'PUBLIC' AND SYNONYM_NAME = ?";
                synonyms = oracleJdbcTemplate.queryForList(sql2, synonymName);
                log.info("Checked PUBLIC synonyms, found: {}", synonyms.size());
                if (!synonyms.isEmpty()) {
                    result.put("isPublic", true);
                }
            }

            // Approach 3: If still not found, check across all schemas (but limit results)
            if (synonyms.isEmpty()) {
                String sql3 = "SELECT OWNER, TABLE_OWNER, TABLE_NAME, DB_LINK FROM ALL_SYNONYMS WHERE SYNONYM_NAME = ? AND ROWNUM <= 5";
                synonyms = oracleJdbcTemplate.queryForList(sql3, synonymName);
                log.info("Checked all schemas, found: {}", synonyms.size());
            }

            if (synonyms.isEmpty()) {
                log.warn("Synonym {} not found in any schema", synonymName);
                return result;
            }

            // Use the first found synonym
            Map<String, Object> synonym = synonyms.get(0);
            String synonymOwner = (String) synonym.get("OWNER");
            String targetOwner = (String) synonym.get("TABLE_OWNER");
            String targetName = (String) synonym.get("TABLE_NAME");
            String dbLink = (String) synonym.get("DB_LINK");

            result.put("exists", true);
            result.put("synonymOwner", synonymOwner);
            result.put("targetOwner", targetOwner);
            result.put("targetName", targetName);
            result.put("dbLink", dbLink);

            log.info("Found synonym {}.{} -> {}.{}",
                    synonymOwner != null ? synonymOwner : owner,
                    synonymName,
                    targetOwner,
                    targetName);

            // Get the target object type and status
            String typeSql = "SELECT OBJECT_TYPE, STATUS FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ?";
            List<Map<String, Object>> targets = oracleJdbcTemplate.queryForList(typeSql, targetOwner, targetName);

            if (!targets.isEmpty()) {
                Map<String, Object> target = targets.get(0);
                String targetType = (String) target.get("OBJECT_TYPE");
                String status = (String) target.get("STATUS");

                result.put("targetType", targetType);
                result.put("status", status);
                result.put("targetValid", "VALID".equalsIgnoreCase(status));

                log.info("Resolved synonym to: {}.{} ({}) with status: {}",
                        targetOwner, targetName, targetType, status);
            } else {
                log.warn("Synonym points to non-existent object: {}.{}", targetOwner, targetName);
            }

        } catch (Exception e) {
            log.error("Error resolving synonym {}.{}: {}", owner, synonymName, e.getMessage());
        }

        return result;
    }

    public Map<String, Object> resolveProcedureTarget(String owner, String procedureName) {
        Map<String, Object> result = new HashMap<>();
        result.put("exists", false);
        result.put("valid", false);
        result.put("isSynonym", false);
        result.put("status", "UNKNOWN");

        try {
            // First, check if it's a synonym
            String synonymSql = "SELECT TABLE_OWNER, TABLE_NAME FROM ALL_SYNONYMS " +
                    "WHERE OWNER = ? AND SYNONYM_NAME = ?";

            List<Map<String, Object>> synonyms = oracleJdbcTemplate.queryForList(
                    synonymSql, owner, procedureName);

            if (!synonyms.isEmpty()) {
                Map<String, Object> synonym = synonyms.get(0);
                String targetOwner = (String) synonym.get("TABLE_OWNER");
                String targetName = (String) synonym.get("TABLE_NAME");

                log.info("Found synonym {}.{} -> {}.{}", owner, procedureName, targetOwner, targetName);

                result.put("isSynonym", true);
                result.put("targetOwner", targetOwner);
                result.put("targetName", targetName);

                // Check if the target procedure exists and is valid
                String procSql = "SELECT STATUS FROM ALL_OBJECTS " +
                        "WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = 'PROCEDURE'";

                List<Map<String, Object>> procedures = oracleJdbcTemplate.queryForList(
                        procSql, targetOwner, targetName);

                if (!procedures.isEmpty()) {
                    result.put("exists", true);
                    String status = (String) procedures.get(0).get("STATUS");
                    result.put("status", status);
                    result.put("valid", "VALID".equalsIgnoreCase(status));

                    log.info("Target procedure status: {}", status);
                } else {
                    log.warn("Synonym points to non-existent procedure: {}.{}", targetOwner, targetName);
                }
            } else {
                // Not a synonym, check directly for procedure
                String procSql = "SELECT STATUS FROM ALL_OBJECTS " +
                        "WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = 'PROCEDURE'";

                List<Map<String, Object>> procedures = oracleJdbcTemplate.queryForList(
                        procSql, owner, procedureName);

                if (!procedures.isEmpty()) {
                    result.put("exists", true);
                    String status = (String) procedures.get(0).get("STATUS");
                    result.put("status", status);
                    result.put("valid", "VALID".equalsIgnoreCase(status));
                    result.put("targetOwner", owner);
                    result.put("targetName", procedureName);

                    log.info("Found procedure {}.{} with status: {}", owner, procedureName, status);
                }
            }

        } catch (Exception e) {
            log.error("Error resolving procedure target: {}", e.getMessage());
        }

        return result;
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
}