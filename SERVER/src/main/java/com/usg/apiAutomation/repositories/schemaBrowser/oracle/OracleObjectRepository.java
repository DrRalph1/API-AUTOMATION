package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class OracleObjectRepository extends OracleRepository {

    @Autowired
    private OracleTableRepository tableRepository;

    @Autowired
    private OracleViewRepository viewRepository;

    @Autowired
    private OracleProcedureRepository procedureRepository;

    @Autowired
    private OracleFunctionRepository functionRepository;

    @Autowired
    private OraclePackageRepository packageRepository;

    @Autowired
    private OracleOtherObjectsRepository otherObjectsRepository;

    // ============================================================
    // GENERAL OBJECT METHODS - ORIGINAL
    // ============================================================

    public Map<String, Object> getObjectDetails(String objectName, String objectType, String owner) {
        // First check if it's a synonym
        Map<String, Object> synonymInfo = checkIfSynonymAndGetTarget(objectName, objectType);

        if (synonymInfo != null && (boolean) synonymInfo.get("isSynonym")) {
            Map<String, Object> result = new HashMap<>();
            result.put("objectName", objectName);
            result.put("objectType", "SYNONYM");
            result.put("owner", owner);
            result.put("isSynonym", true);
            result.putAll(synonymInfo);

            // Get target object details
            if (!(boolean) synonymInfo.get("isRemote") && synonymInfo.get("targetName") != null) {
                try {
                    Map<String, Object> targetDetails = getObjectDetailsByNameAndType(
                            (String) synonymInfo.get("targetName"),
                            (String) synonymInfo.get("targetType"),
                            (String) synonymInfo.get("targetOwner"));
                    result.put("targetDetails", targetDetails);
                } catch (Exception e) {
                    log.warn("Could not fetch target details: {}", e.getMessage());
                    result.put("targetError", e.getMessage());
                    // Also add basic info about the target if possible
                    try {
                        Map<String, Object> basicTargetInfo = getBasicObjectInfo(
                                (String) synonymInfo.get("targetOwner"),
                                (String) synonymInfo.get("targetName"),
                                (String) synonymInfo.get("targetType"));
                        result.put("targetBasicInfo", basicTargetInfo);
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }

            return result;
        }

        return getObjectDetailsByNameAndType(objectName, objectType, owner);
    }

    public Map<String, Object> getObjectDetails(String objectName, String objectType,
                                                String owner, int page, int pageSize) {
        // First check if it's a synonym
        Map<String, Object> synonymInfo = checkIfSynonymAndGetTarget(objectName, objectType);

        if (synonymInfo != null && (boolean) synonymInfo.get("isSynonym")) {
            Map<String, Object> result = new HashMap<>();
            result.put("objectName", objectName);
            result.put("objectType", "SYNONYM");
            result.put("owner", owner);
            result.put("isSynonym", true);
            result.putAll(synonymInfo);

            // Get paginated target details
            if (!(boolean) synonymInfo.get("isRemote") && synonymInfo.get("targetName") != null) {
                try {
                    Map<String, Object> targetDetails = getObjectDetailsPaginated(
                            (String) synonymInfo.get("targetName"),
                            (String) synonymInfo.get("targetType"),
                            (String) synonymInfo.get("targetOwner"),
                            page, pageSize, true);
                    result.put("targetDetails", targetDetails);
                } catch (Exception e) {
                    log.warn("Could not fetch target details: {}", e.getMessage());
                }
            }

            return result;
        }

        Map<String, Object> result = getObjectDetailsPaginated(objectName, objectType, owner, page, pageSize, true);

        // Add constraint details for TABLE objects
        if ("TABLE".equalsIgnoreCase(objectType)) {
            try {
                String resolvedOwner = owner != null && !owner.isEmpty() ? owner : getCurrentUser();
                List<Map<String, Object>> constraints = tableRepository.getTableConstraints(resolvedOwner, objectName);

                // Add constraints to the result
                if (constraints != null && !constraints.isEmpty()) {
                    result.put("constraints", constraints);
                    result.put("constraintsCount", constraints.size());

                    // Also add transformed constraints for frontend if needed
                    List<Map<String, Object>> transformedConstraints = transformConstraintsForFrontend(constraints);
                    result.put("constraintsFormatted", transformedConstraints);
                } else {
                    result.put("constraints", new ArrayList<>());
                    result.put("constraintsCount", 0);
                }
            } catch (Exception e) {
                log.warn("Could not fetch constraints for table {}: {}", objectName, e.getMessage());
                result.put("constraints", new ArrayList<>());
                result.put("constraintsCount", 0);
                result.put("constraintsError", e.getMessage());
            }
        }

        // Optionally add constraint details for VIEW objects as well (some views can have constraints)
        else if ("VIEW".equalsIgnoreCase(objectType)) {
            try {
                String resolvedOwner = owner != null && !owner.isEmpty() ? owner : getCurrentUser();
                // Check if the view has any constraints (views can have WITH CHECK OPTION constraints)
                String sql = "SELECT " +
                        "    constraint_name, " +
                        "    constraint_type, " +
                        "    status, " +
                        "    deferrable, " +
                        "    deferred, " +
                        "    validated " +
                        "FROM all_constraints " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                        "AND constraint_type IN ('V', 'O')"; // V = Check option on view, O = Read-only view

                List<Map<String, Object>> viewConstraints = getJdbcTemplate().queryForList(sql, resolvedOwner, objectName);

                if (viewConstraints != null && !viewConstraints.isEmpty()) {
                    result.put("constraints", viewConstraints);
                    result.put("constraintsCount", viewConstraints.size());
                }
            } catch (Exception e) {
                log.debug("Could not fetch constraints for view {}: {}", objectName, e.getMessage());
            }
        }

        return result;
    }

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
                basicInfo = getJdbcTemplate().queryForMap(basicInfoSql, owner, objectName, objectType);
                result.putAll(basicInfo);
            } catch (EmptyResultDataAccessException e) {
                // Try without object type
                String altSql = "SELECT owner, object_name, object_type, status, created, last_ddl_time " +
                        "FROM all_objects WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";
                basicInfo = getJdbcTemplate().queryForMap(altSql, owner, objectName);
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
                    totalColumns = getJdbcTemplate().queryForObject(countSql, Long.class, owner, objectName);
                }

                // Get parameter count for procedures/functions/packages
                if ("PROCEDURE".equals(upperType) || "FUNCTION".equals(upperType) || "PACKAGE".equals(upperType)) {
                    String paramCountSql = "SELECT COUNT(*) FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND argument_name IS NOT NULL";
                    totalParameters = getJdbcTemplate().queryForObject(paramCountSql, Long.class, owner, objectName);
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
                        totalColumns = getJdbcTemplate().queryForObject(countSql, Long.class, owner, objectName);
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
                        totalParameters = getJdbcTemplate().queryForObject(paramCountSql, Long.class, owner, objectName);
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

            String originalObjectType = objectType.toUpperCase();

            // Try to get details for the specified object type first
            Map<String, Object> result = getDetailsByType(originalObjectType, owner, objectName);

            // Check if we got valid data (not an error)
            boolean foundData = !result.isEmpty() && !result.containsKey("error") && !result.containsKey("message");

            if (foundData) {
                details.putAll(result);

                // ADD SOURCE FOR TABLES AND VIEWS
                if ("TABLE".equals(originalObjectType) || "VIEW".equals(originalObjectType)) {
                    try {
                        String source = getSourceForTableOrView(owner, objectName, originalObjectType);
                        if (source != null && !source.isEmpty()) {
                            details.put("source", source);
                        }
                    } catch (Exception e) {
                        log.warn("Could not get source for {} {}: {}", originalObjectType, objectName, e.getMessage());
                    }
                }

            } else {
                // If not found as requested type, try as synonym
                log.info("No data found for {} {} as {}, trying as SYNONYM", owner, objectName, originalObjectType);

                Map<String, Object> synonymDetails = otherObjectsRepository.getSynonymDetails(objectName);

                if (!synonymDetails.isEmpty() && !synonymDetails.containsKey("error")) {
                    details.putAll(synonymDetails);
                    details.put("originalObjectType", originalObjectType);
                    details.put("objectType", "SYNONYM");
                    details.put("isSynonym", true);
                    details.put("fallbackUsed", true);

                    // Get target object details
                    String targetOwner = (String) synonymDetails.get("target_owner");
                    String targetName = (String) synonymDetails.get("target_name");
                    String targetType = (String) synonymDetails.get("target_type");

                    if (targetOwner != null && targetName != null && targetType != null) {
                        Map<String, Object> targetDetails = getDetailsByType(targetType, targetOwner, targetName);

                        if (!targetDetails.isEmpty() && !targetDetails.containsKey("error")) {
                            details.put("targetObjectDetails", targetDetails);

                            // ALSO GET SOURCE FOR TARGET TABLE/VIEW
                            if ("TABLE".equalsIgnoreCase(targetType) || "VIEW".equalsIgnoreCase(targetType)) {
                                try {
                                    String targetSource = getSourceForTableOrView(targetOwner, targetName, targetType);
                                    if (targetSource != null && !targetSource.isEmpty()) {
                                        if (!targetDetails.containsKey("source")) {
                                            targetDetails.put("source", targetSource);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("Could not get source for target {} {}: {}", targetType, targetName, e.getMessage());
                                }
                            }
                        } else {
                            Map<String, Object> targetInfo = getBasicObjectInfo(targetOwner, targetName, targetType);
                            details.put("targetObjectInfo", targetInfo);

                            if ("TABLE".equalsIgnoreCase(targetType) || "VIEW".equalsIgnoreCase(targetType)) {
                                try {
                                    String targetSource = getSourceForTableOrView(targetOwner, targetName, targetType);
                                    if (targetSource != null && !targetSource.isEmpty()) {
                                        targetInfo.put("source", targetSource);
                                    }
                                } catch (Exception e) {
                                    log.warn("Could not get source for target {} {}: {}", targetType, targetName, e.getMessage());
                                }
                            }
                        }
                    }
                } else {
                    details.put("message", "Object not found as " + originalObjectType + " or as a SYNONYM");
                    details.put("notFound", true);
                }
            }

        } catch (Exception e) {
            log.warn("Could not get details for {} {}: {}", objectType, objectName, e.getMessage());
            details.put("error", e.getMessage());
            details.put("hasError", true);
        }

        return details;
    }

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

            int exists = getJdbcTemplate().queryForObject(existsSql, Integer.class, owner, objectName);
            counts.put("exists", exists > 0);

            // Check if it's a synonym
            String synonymCheckSql = "SELECT COUNT(*) FROM user_synonyms " +
                    "WHERE UPPER(synonym_name) = UPPER(?)";
            int isSynonym = getJdbcTemplate().queryForObject(synonymCheckSql, Integer.class, objectName);
            counts.put("isSynonym", isSynonym > 0);

            if (isSynonym > 0) {
                // Get synonym target info
                String targetSql = "SELECT table_owner, table_name, db_link FROM user_synonyms " +
                        "WHERE UPPER(synonym_name) = UPPER(?)";
                Map<String, Object> target = getJdbcTemplate().queryForMap(targetSql, objectName);
                counts.put("targetOwner", target.get("table_owner"));
                counts.put("targetName", target.get("table_name"));
                counts.put("dbLink", target.get("db_link"));
                counts.put("isRemote", target.get("db_link") != null);
            }

            if (exists > 0) {
                // Get column count for tables/views
                if ("TABLE".equals(upperType) || "VIEW".equals(upperType)) {
                    String colCountSql = "SELECT COUNT(*) FROM all_tab_columns " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?)";
                    long totalColumns = getJdbcTemplate().queryForObject(
                            colCountSql, Long.class, owner, objectName);
                    counts.put("totalColumns", totalColumns);
                }

                // Get parameter count for procedures/functions/packages
                if ("PROCEDURE".equals(upperType) || "FUNCTION".equals(upperType) || "PACKAGE".equals(upperType)) {
                    String paramCountSql = "SELECT COUNT(*) FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND argument_name IS NOT NULL";
                    long totalParameters = getJdbcTemplate().queryForObject(
                            paramCountSql, Long.class, owner, objectName);
                    counts.put("totalParameters", totalParameters);
                }

                // Get dependency count
                String depCountSql = "SELECT COUNT(*) FROM all_dependencies " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?)";
                long dependencies = getJdbcTemplate().queryForObject(
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

            // First try to find the object in all_objects
            String sql = "SELECT status FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND object_type = ?";

            String status = null;
            boolean foundInObjects = false;

            try {
                status = getJdbcTemplate().queryForObject(sql, String.class, owner, objectName, objectType);
                foundInObjects = true;
            } catch (EmptyResultDataAccessException e) {
                foundInObjects = false;
            }

            if (foundInObjects) {
                result.put("exists", true);
                result.put("status", status);
                result.put("accessible", true);
                result.put("valid", "VALID".equalsIgnoreCase(status));
                result.put("source", "OBJECT");
            } else {
                // Check if it's a synonym
                String synonymSql = "SELECT s.table_owner, s.table_name, s.db_link, " +
                        "o.status as table_status " +
                        "FROM all_synonyms s " +
                        "LEFT JOIN all_objects o ON UPPER(o.owner) = UPPER(s.table_owner) " +
                        "AND UPPER(o.object_name) = UPPER(s.table_name) " +
                        "WHERE UPPER(s.owner) = UPPER(?) AND UPPER(s.synonym_name) = UPPER(?)";

                try {
                    Map<String, Object> synonymResult = getJdbcTemplate().queryForMap(synonymSql, owner, objectName);

                    if (synonymResult != null && !synonymResult.isEmpty()) {
                        String tableOwner = (String) synonymResult.get("table_owner");
                        String tableName = (String) synonymResult.get("table_name");
                        String dbLink = (String) synonymResult.get("db_link");
                        String tableStatus = (String) synonymResult.get("table_status");

                        result.put("exists", true);
                        result.put("is_synonym", true);
                        result.put("target_owner", tableOwner);
                        result.put("target_object", tableName);
                        result.put("db_link", dbLink);
                        result.put("source", "SYNONYM");

                        if (tableStatus != null) {
                            result.put("status", tableStatus);
                            result.put("valid", "VALID".equalsIgnoreCase(tableStatus));
                            result.put("target_exists", true);
                        } else {
                            if (dbLink != null && !dbLink.isEmpty()) {
                                result.put("status", "REMOTE");
                                result.put("valid", true);
                                result.put("target_exists", true);
                                result.put("message", "Remote object via database link");
                            } else {
                                result.put("status", "TARGET_NOT_FOUND");
                                result.put("valid", false);
                                result.put("target_exists", false);
                                result.put("message", "Synonym target object not found");
                            }
                        }

                        result.put("accessible", checkSynonymAccess(owner, objectName));
                    } else {
                        result.put("exists", false);
                        result.put("status", "NOT_FOUND");
                        result.put("accessible", false);
                        result.put("message", "Object or synonym not found");
                    }
                } catch (EmptyResultDataAccessException ex) {
                    result.put("exists", false);
                    result.put("status", "NOT_FOUND");
                    result.put("accessible", false);
                    result.put("message", "Object or synonym not found");
                }
            }

        } catch (Exception e) {
            result.put("exists", false);
            result.put("status", "ERROR");
            result.put("accessible", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

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

                return getJdbcTemplate().queryForMap(sql, objectName, objectType);
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

                return getJdbcTemplate().queryForMap(sql, objectName, owner, objectType);
            }

        } catch (Exception e) {
            log.error("Error in getObjectSize for {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve size for object " + objectName + ": " + e.getMessage(), e);
        }
    }

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
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getAllObjects: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

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
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getObjectsBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

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
            return getJdbcTemplate().queryForList(sql, "%" + searchPattern + "%");
        } catch (Exception e) {
            log.error("Error in searchObjects: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

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
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getObjectCountByType: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

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
            return getJdbcTemplate().queryForList(sql, status);
        } catch (Exception e) {
            log.error("Error in getObjectsByStatus: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private Map<String, Object> getDetailsByType(String objectType, String owner, String objectName) {
        try {
            switch (objectType) {
                case "TABLE":
                    return tableRepository.getTableDetails(owner, objectName);
                case "VIEW":
                    return viewRepository.getViewDetails(owner, objectName);
                case "PROCEDURE":
                    return procedureRepository.getProcedureDetails(owner, objectName);
                case "FUNCTION":
                    return functionRepository.getFunctionDetails(owner, objectName);
                case "PACKAGE":
                    return packageRepository.getPackageDetails(owner, objectName);
                case "PACKAGE BODY":
                    return packageRepository.getPackageDetails(owner, objectName);
                case "SEQUENCE":
                    return otherObjectsRepository.getSequenceDetails(owner, objectName);
                case "SYNONYM":
                    return otherObjectsRepository.getSynonymDetails(objectName);
                case "TRIGGER":
                    return otherObjectsRepository.getTriggerDetails(owner, objectName);
                case "INDEX":
                    return getIndexDetails(owner, objectName);
                case "TYPE":
                    return otherObjectsRepository.getTypeDetails(owner, objectName);
                case "TYPE BODY":
                    return getTypeBodyDetails(owner, objectName);
                case "MATERIALIZED VIEW":
                    return getMaterializedViewDetails(owner, objectName);
                case "DATABASE LINK":
                    return getDatabaseLinkDetails(owner, objectName);
                case "JAVA CLASS":
                case "JAVA SOURCE":
                case "JAVA RESOURCE":
                    return getJavaObjectDetails(owner, objectName, objectType);
                default:
                    return getBasicObjectInfo(owner, objectName, objectType);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

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
                "    char_used " +
                "FROM all_tab_columns " +
                "WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) " +
                "ORDER BY column_id " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        return getJdbcTemplate().queryForList(sql, owner, tableName, offset, pageSize);
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

        return getJdbcTemplate().queryForList(sql, owner, objectName, offset, pageSize);
    }

    public Map<String, Object> getBasicObjectInfo(String owner, String objectName, String objectType) {
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

            return getJdbcTemplate().queryForMap(sql, owner, objectName, objectType);
        } catch (Exception e) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("owner", owner);
            empty.put("object_name", objectName);
            empty.put("object_type", objectType);
            empty.put("status", "UNKNOWN");
            return empty;
        }
    }

    private String getSourceForTableOrView(String owner, String objectName, String objectType) {
        try {
            StringBuilder source = new StringBuilder();

            if ("TABLE".equalsIgnoreCase(objectType)) {
                source.append("CREATE TABLE ");
                if (!owner.equalsIgnoreCase(getCurrentUser())) {
                    source.append(owner).append(".");
                }
                source.append(objectName).append(" (\n");

                List<Map<String, Object>> columns = tableRepository.getTableColumns(owner, objectName);
                for (int i = 0; i < columns.size(); i++) {
                    Map<String, Object> col = columns.get(i);
                    source.append("    ").append(col.get("column_name"))
                            .append(" ").append(col.get("data_type"));

                    Number dataLength = (Number) col.get("data_length");
                    Number dataPrecision = (Number) col.get("data_precision");
                    Number dataScale = (Number) col.get("data_scale");
                    String dataType = (String) col.get("data_type");

                    if (dataLength != null && dataLength.intValue() > 0 &&
                            ("VARCHAR2".equalsIgnoreCase(dataType) || "CHAR".equalsIgnoreCase(dataType) ||
                                    "VARCHAR".equalsIgnoreCase(dataType) || "NVARCHAR2".equalsIgnoreCase(dataType))) {
                        source.append("(").append(dataLength).append(")");
                    } else if (dataPrecision != null) {
                        source.append("(").append(dataPrecision);
                        if (dataScale != null && dataScale.intValue() > 0) {
                            source.append(",").append(dataScale);
                        }
                        source.append(")");
                    }

                    if ("N".equals(col.get("nullable"))) {
                        source.append(" NOT NULL");
                    }

                    Object defaultValue = col.get("data_default");
                    if (defaultValue != null && !defaultValue.toString().isEmpty()) {
                        source.append(" DEFAULT ").append(defaultValue);
                    }

                    if (i < columns.size() - 1) {
                        source.append(",");
                    }
                    source.append("\n");
                }

                List<Map<String, Object>> constraints = tableRepository.getTableConstraints(owner, objectName);
                for (Map<String, Object> constraint : constraints) {
                    if ("P".equals(constraint.get("constraint_type"))) {
                        source.append("    CONSTRAINT ").append(constraint.get("constraint_name"))
                                .append(" PRIMARY KEY (").append(constraint.get("columns")).append(")\n");
                        break;
                    }
                }

                source.append(");\n");

                String comment = getTableComment(objectName);
                if (comment != null && !comment.isEmpty()) {
                    source.append("\nCOMMENT ON TABLE ");
                    if (!owner.equalsIgnoreCase(getCurrentUser())) {
                        source.append(owner).append(".");
                    }
                    source.append(objectName).append(" IS '").append(comment).append("';\n");
                }

            } else if ("VIEW".equalsIgnoreCase(objectType)) {
                String viewText;
                if (owner.equalsIgnoreCase(getCurrentUser())) {
                    String sql = "SELECT text FROM user_views WHERE UPPER(view_name) = UPPER(?)";
                    viewText = getJdbcTemplate().queryForObject(sql, String.class, objectName);
                } else {
                    String sql = "SELECT text FROM all_views WHERE UPPER(owner) = UPPER(?) AND UPPER(view_name) = UPPER(?)";
                    viewText = getJdbcTemplate().queryForObject(sql, String.class, owner, objectName);
                }

                if (viewText != null && !viewText.isEmpty()) {
                    source.append("CREATE OR REPLACE VIEW ");
                    if (!owner.equalsIgnoreCase(getCurrentUser())) {
                        source.append(owner).append(".");
                    }
                    source.append(objectName).append(" AS\n");
                    source.append(viewText);
                    if (!viewText.trim().endsWith(";")) {
                        source.append(";");
                    }
                }
            }

            return source.length() > 0 ? source.toString() : null;

        } catch (Exception e) {
            log.warn("Could not generate source for {} {}.{}: {}", objectType, owner, objectName, e.getMessage());
            return null;
        }
    }

    private Map<String, Object> getIndexDetails(String owner, String indexName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT index_name, table_name, uniqueness, status FROM all_indexes " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(index_name) = UPPER(?)";
            return getJdbcTemplate().queryForMap(sql, owner, indexName);
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return details;
        }
    }

    private Map<String, Object> getTypeBodyDetails(String owner, String typeName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT owner, object_name, object_type, status FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'TYPE BODY'";
            return getJdbcTemplate().queryForMap(sql, owner, typeName);
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return details;
        }
    }

    private Map<String, Object> getMaterializedViewDetails(String owner, String mvName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT mview_name, refresh_method, refresh_mode, last_refresh_date FROM all_mviews " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(mview_name) = UPPER(?)";
            return getJdbcTemplate().queryForMap(sql, owner, mvName);
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return details;
        }
    }

    private Map<String, Object> getDatabaseLinkDetails(String owner, String dbLinkName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT db_link, username, host FROM all_db_links " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(db_link) = UPPER(?)";
            return getJdbcTemplate().queryForMap(sql, owner, dbLinkName);
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return details;
        }
    }

    private Map<String, Object> getJavaObjectDetails(String owner, String objectName, String objectType) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT owner, object_name, object_type, status FROM all_objects " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = ?";
            return getJdbcTemplate().queryForMap(sql, owner, objectName, objectType);
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return details;
        }
    }

    private String getTableComment(String tableName) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT comments FROM user_tab_comments WHERE table_name = UPPER(?)",
                    String.class, tableName);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean checkSynonymAccess(String owner, String synonymName) {
        try {
            String accessSql = "SELECT COUNT(*) FROM all_synonyms " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(synonym_name) = UPPER(?)";
            Integer count = getJdbcTemplate().queryForObject(accessSql, Integer.class, owner, synonymName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}