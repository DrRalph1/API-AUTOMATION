package com.usg.autoAPIGenerator.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.util.*;

@Slf4j
@Repository
public class PostgreSQLObjectRepository extends PostgreSQLRepository {

    @Autowired
    private PostgreSQLTableRepository tableRepository;

    @Autowired
    private PostgreSQLViewRepository viewRepository;

    @Autowired
    private PostgreSQLProcedureRepository procedureRepository;

    @Autowired
    private PostgreSQLFunctionRepository functionRepository;

    @Autowired
    private PostgreSQLOtherObjectsRepository otherObjectsRepository;

    // ============================================================
    // GENERAL OBJECT METHODS - POSTGRESQL VERSION
    // ============================================================

    public Map<String, Object> getObjectDetails(String objectName, String objectType, String owner) {
        // PostgreSQL doesn't have synonyms, so skip synonym check
        return getObjectDetailsByNameAndType(objectName, objectType, owner);
    }

    public Map<String, Object> getObjectDetails(String objectName, String objectType,
                                                String owner, int page, int pageSize) {
        // PostgreSQL doesn't have synonyms, so skip synonym check
        Map<String, Object> result = getObjectDetailsPaginated(objectName, objectType, owner, page, pageSize, true);

        // Add constraint details for TABLE objects
        if ("TABLE".equalsIgnoreCase(objectType)) {
            try {
                String resolvedOwner = owner != null && !owner.isEmpty() ? owner : getCurrentSchema();
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

        return result;
    }

    public Map<String, Object> getObjectDetailsPaginated(String objectName, String objectType,
                                                         String owner, int page, int pageSize,
                                                         boolean includeCounts) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            String upperType = objectType.toUpperCase();
            int offset = (page - 1) * pageSize;

            // First, get basic object info
            String basicInfoSql = getBasicInfoSql(upperType);

            Map<String, Object> basicInfo;
            try {
                basicInfo = getJdbcTemplate().queryForMap(basicInfoSql, owner, objectName);
                result.putAll(basicInfo);
            } catch (EmptyResultDataAccessException e) {
                // Try without object type restriction
                String altSql = "SELECT " +
                        "    n.nspname as owner, " +
                        "    c.relname as object_name, " +
                        "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                        "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                        "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                        "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                        "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                        "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                        "         WHEN c.relkind = 'i' THEN 'INDEX' END as object_type, " +
                        "    'VALID' as status, " +
                        "    NULL as created, " +
                        "    NULL as last_ddl_time " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ?";
                basicInfo = getJdbcTemplate().queryForMap(altSql, owner, objectName);
                result.putAll(basicInfo);
            }

            // Get total counts if requested or if we need them for pagination
            long totalColumns = 0;
            long totalParameters = 0;

            if (includeCounts) {
                // Get column count for tables/views
                if ("TABLE".equals(upperType) || "VIEW".equals(upperType) || "MATERIALIZED VIEW".equals(upperType)) {
                    String countSql = "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE table_schema = ? AND table_name = ?";
                    totalColumns = getJdbcTemplate().queryForObject(countSql, Long.class, owner, objectName);
                }

                // Get parameter count for functions/procedures
                if ("FUNCTION".equals(upperType) || "PROCEDURE".equals(upperType)) {
                    String paramCountSql = "SELECT COUNT(*) FROM pg_proc_info WHERE proname = ?";
                    try {
                        totalParameters = getJdbcTemplate().queryForObject(paramCountSql, Long.class, objectName);
                    } catch (Exception e) {
                        totalParameters = 0;
                    }
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
                case "MATERIALIZED VIEW":
                    List<Map<String, Object>> columns = getTableColumnsPaginatedInternal(owner, objectName, offset, pageSize);
                    result.put("columns", columns);
                    if (!includeCounts) {
                        String countSql = "SELECT COUNT(*) FROM information_schema.columns " +
                                "WHERE table_schema = ? AND table_name = ?";
                        totalColumns = getJdbcTemplate().queryForObject(countSql, Long.class, owner, objectName);
                        result.put("totalColumns", totalColumns);
                        result.put("totalCount", totalColumns);
                    }
                    break;

                case "FUNCTION":
                case "PROCEDURE":
                    List<Map<String, Object>> params = getArgumentsPaginatedInternal(owner, objectName, offset, pageSize);
                    result.put("parameters", params);
                    if (!includeCounts) {
                        String paramCountSql = "SELECT COUNT(*) FROM pg_proc_info WHERE proname = ?";
                        try {
                            totalParameters = getJdbcTemplate().queryForObject(paramCountSql, Long.class, objectName);
                        } catch (Exception e) {
                            totalParameters = 0;
                        }
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
                owner = getCurrentSchema();
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
                if ("TABLE".equals(originalObjectType) || "VIEW".equals(originalObjectType) ||
                        "MATERIALIZED VIEW".equals(originalObjectType)) {
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
                details.put("message", "Object not found as " + originalObjectType);
                details.put("notFound", true);
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
                owner = getCurrentSchema();
            }

            String upperType = objectType.toUpperCase();

            // Check if object exists
            String existsSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ?";

            int exists = getJdbcTemplate().queryForObject(existsSql, Integer.class, owner, objectName);
            counts.put("exists", exists > 0);

            // PostgreSQL doesn't have synonyms
            counts.put("isSynonym", false);

            if (exists > 0) {
                // Get column count for tables/views
                if ("TABLE".equals(upperType) || "VIEW".equals(upperType) || "MATERIALIZED VIEW".equals(upperType)) {
                    String colCountSql = "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE table_schema = ? AND table_name = ?";
                    long totalColumns = getJdbcTemplate().queryForObject(
                            colCountSql, Long.class, owner, objectName);
                    counts.put("totalColumns", totalColumns);
                }

                // Get parameter count for functions/procedures
                if ("FUNCTION".equals(upperType) || "PROCEDURE".equals(upperType)) {
                    String paramCountSql = "SELECT COUNT(*) FROM pg_proc_info WHERE proname = ?";
                    try {
                        long totalParameters = getJdbcTemplate().queryForObject(
                                paramCountSql, Long.class, objectName);
                        counts.put("totalParameters", totalParameters);
                    } catch (Exception e) {
                        counts.put("totalParameters", 0);
                    }
                }

                // Get dependency count
                String depCountSql = "SELECT COUNT(*) FROM pg_depend dep " +
                        "JOIN pg_class c ON dep.objid = c.oid " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ?";
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
                owner = getCurrentSchema();
                result.put("owner", owner);
            }

            // Find the object in PostgreSQL system catalogs
            String sql = getValidationSql(objectType);

            boolean foundInObjects = false;

            try {
                Map<String, Object> objectInfo = getJdbcTemplate().queryForMap(sql, owner, objectName);
                foundInObjects = true;
                result.put("exists", true);
                result.put("status", "VALID");
                result.put("accessible", true);
                result.put("valid", true);
                result.put("source", "OBJECT");
                result.putAll(objectInfo);
            } catch (EmptyResultDataAccessException e) {
                foundInObjects = false;
            }

            if (!foundInObjects) {
                result.put("exists", false);
                result.put("status", "NOT_FOUND");
                result.put("accessible", false);
                result.put("message", "Object not found");
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

            // Get table size for tables
            String sql = "SELECT " +
                    "    pg_total_relation_size(quote_ident(?) || '.' || quote_ident(?)) as bytes, " +
                    "    pg_relation_size(quote_ident(?) || '.' || quote_ident(?)) as table_bytes, " +
                    "    pg_indexes_size(quote_ident(?) || '.' || quote_ident(?)) as index_bytes, " +
                    "    pg_table_size(quote_ident(?) || '.' || quote_ident(?)) as total_bytes, " +
                    "    (SELECT tablespace FROM pg_tablespace WHERE oid = c.reltablespace) as tablespace_name " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ?";

            Map<String, Object> result = getJdbcTemplate().queryForMap(sql,
                    owner, objectName, owner, objectName, owner, objectName, owner, objectName,
                    owner, objectName);

            result.put("segment_name", objectName);
            result.put("segment_type", objectType);
            result.put("status", "SUCCESS");

            return result;

        } catch (Exception e) {
            log.error("Error in getObjectSize for {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve size for object " + objectName + ": " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getAllObjects() {
        try {
            String sql = "SELECT " +
                    "    c.relname as object_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "         ELSE c.relkind::text END as object_type, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
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
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "         ELSE c.relkind::text END as object_type, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? " +
                    "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
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
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "         ELSE c.relkind::text END as object_type, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE c.relname ILIKE ? " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
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
                    "    CASE WHEN relkind = 'r' THEN 'TABLE' " +
                    "         WHEN relkind = 'v' THEN 'VIEW' " +
                    "         WHEN relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN relkind = 'i' THEN 'INDEX' " +
                    "         ELSE relkind::text END as object_type, " +
                    "    COUNT(*) as object_count, " +
                    "    COUNT(*) as valid_count, " +
                    "    0 as invalid_count, " +
                    "    0 as temporary_count, " +
                    "    0 as generated_count, " +
                    "    0 as secondary_count " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
                    "GROUP BY relkind " +
                    "ORDER BY object_type";
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getObjectCountByType: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getObjectsByStatus(String status) {
        // PostgreSQL doesn't have object status like Oracle, so return all objects
        return getAllObjects();
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
                case "MATERIALIZED VIEW":
                    return viewRepository.getMaterializedViewDetails(owner, objectName);
                case "PROCEDURE":
                    return procedureRepository.getProcedureDetails(owner, objectName);
                case "FUNCTION":
                    return functionRepository.getFunctionDetails(owner, objectName);
                case "SEQUENCE":
                    return otherObjectsRepository.getSequenceDetails(owner, objectName);
                case "TRIGGER":
                    return otherObjectsRepository.getTriggerDetails(owner, objectName);
                case "INDEX":
                    return getIndexDetails(owner, objectName);
                case "TYPE":
                    return otherObjectsRepository.getTypeDetails(owner, objectName);
                default:
                    return getBasicObjectInfo(owner, objectName, objectType);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    private String getBasicInfoSql(String objectType) {
        String relKind = getRelationKind(objectType);
        if (relKind != null) {
            return "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "         ELSE c.relkind::text END as object_type, " +
                    "    'VALID' as status, " +
                    "    pg_catalog.pg_get_userbyid(c.relowner) as owner_name, " +
                    "    c.reltuples as row_count, " +
                    "    c.relpages as page_count, " +
                    "    c.relfilenode as file_node, " +
                    "    c.reloptions as storage_options, " +
                    "    c.relacl as privileges, " +
                    "    c.relhastriggers as has_triggers, " +
                    "    c.relhasindex as has_indexes, " +
                    "    c.relhasrules as has_rules, " +
                    "    c.relrowsecurity as has_row_security, " +
                    "    c.relispopulated as is_populated, " +
                    "    c.relreplident as replica_identity, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a WHERE a.attrelid = c.oid AND a.attnum > 0 AND NOT a.attisdropped) as column_count, " +
                    "    (SELECT COUNT(*) FROM pg_constraint con WHERE con.conrelid = c.oid) as constraint_count, " +
                    "    c.relkind as relation_kind, " +
                    "    current_setting('server_version') as postgres_version, " +
                    "    now() as current_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = ?";
        }
        return "SELECT " +
                "    n.nspname as owner, " +
                "    c.relname as object_name, " +
                "    'OTHER' as object_type, " +
                "    'VALID' as status, " +
                "    pg_catalog.pg_get_userbyid(c.relowner) as owner_name, " +
                "    c.reltuples as row_count, " +
                "    c.relpages as page_count, " +
                "    c.reloptions as storage_options, " +
                "    c.relacl as privileges, " +
                "    'OTHER' as relation_kind, " +
                "    current_setting('server_version') as postgres_version, " +
                "    now() as current_time " +
                "FROM pg_class c " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "WHERE n.nspname = ? AND c.relname = ?";
    }


    public Map<String, Object> getDetailedObjectInfo(String owner, String objectName, String objectType) {
        Map<String, Object> details = new HashMap<>();

        try {
            // Get basic info
            Map<String, Object> basicInfo = getBasicObjectInfo(owner, objectName, objectType);
            details.putAll(basicInfo);

            // Get column information - only for table-like objects
            String upperType = objectType.toUpperCase();
            if ("TABLE".equals(upperType) || "VIEW".equals(upperType) || "MATERIALIZED VIEW".equals(upperType)) {
                List<Map<String, Object>> columns = getColumnInfo(owner, objectName);
                details.put("columns", columns);

                // Get constraints - only for tables
                if ("TABLE".equals(upperType)) {
                    List<Map<String, Object>> constraints = getConstraints(owner, objectName);
                    details.put("constraints", constraints);
                }

                // Get indexes - only for tables
                if ("TABLE".equals(upperType)) {
                    List<Map<String, Object>> indexes = getIndexes(owner, objectName);
                    details.put("indexes", indexes);
                }
            }

            // Get statistics - ONLY for table-like objects (TABLE, VIEW, MATERIALIZED VIEW)
            if ("TABLE".equals(upperType) || "VIEW".equals(upperType) || "MATERIALIZED VIEW".equals(upperType)) {
                try {
                    // Check if this is actually a table-like object
                    boolean isTableLike = isTableLikeObject(owner, objectName);
                    if (isTableLike) {
                        try {
                            Map<String, Object> statistics = getTableStatistics(owner, objectName);
                            details.put("statistics", statistics);
                        } catch (EmptyResultDataAccessException e) {
                            // Handle gracefully
                            Map<String, Object> emptyStats = new HashMap<>();
                            emptyStats.put("message", "Statistics not available");
                            details.put("statistics", emptyStats);
                        }
                    } else {
                        Map<String, Object> note = new HashMap<>();
                        note.put("message", "Statistics not applicable for " + objectType);
                        details.put("statistics", note);
                    }
                } catch (EmptyResultDataAccessException e) {
                    // Statistics not available for this object, log and continue
                    log.debug("No statistics found for {}.{} (type: {}): {}",
                            owner, objectName, objectType, e.getMessage());
                    Map<String, Object> emptyStats = new HashMap<>();
                    emptyStats.put("message", "Statistics not available for this object type");
                    emptyStats.put("estimated_row_count", 0);
                    emptyStats.put("page_count", 0);
                    details.put("statistics", emptyStats);
                }
            } else {
                // For non-table objects, add a note that statistics aren't applicable
                Map<String, Object> note = new HashMap<>();
                note.put("message", "Statistics not applicable for " + objectType);
                details.put("statistics", note);
            }

            // Get dependencies (applicable to all object types)
            List<Map<String, Object>> dependencies = getDependencies(owner, objectName);
            details.put("dependencies", dependencies);

            return details;

        } catch (Exception e) {
            log.error("Error getting detailed object info for {}.{} (type: {}): {}",
                    owner, objectName, objectType, e.getMessage(), e);
            throw new RuntimeException("Failed to get detailed object info", e);
        }
    }

    private List<Map<String, Object>> getColumnInfo(String owner, String objectName) {
        String sql = "SELECT " +
                "    a.attname as column_name, " +
                "    pg_catalog.format_type(a.atttypid, a.atttypmod) as data_type, " +
                "    a.attnotnull as is_nullable, " +
                "    a.attnum as ordinal_position, " +
                "    pg_catalog.col_description(a.attrelid, a.attnum) as column_comment, " +
                "    a.atttypmod as character_maximum_length, " +
                "    a.atttypid as data_type_id " +
                "FROM pg_attribute a " +
                "WHERE a.attrelid = (SELECT c.oid FROM pg_class c JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "                    WHERE n.nspname = ? AND c.relname = ?) " +
                "AND a.attnum > 0 " +
                "AND NOT a.attisdropped " +
                "ORDER BY a.attnum";

        return getJdbcTemplate().queryForList(sql, owner, objectName);
    }

    private List<Map<String, Object>> getConstraints(String owner, String objectName) {
        String sql = "SELECT " +
                "    con.conname as constraint_name, " +
                "    con.contype as constraint_type, " +
                "    pg_catalog.pg_get_constraintdef(con.oid, true) as constraint_definition, " +
                "    con.condeferrable as is_deferrable, " +
                "    con.condeferred as is_deferred " +
                "FROM pg_constraint con " +
                "WHERE con.conrelid = (SELECT c.oid FROM pg_class c JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "                      WHERE n.nspname = ? AND c.relname = ?)";

        return getJdbcTemplate().queryForList(sql, owner, objectName);
    }

    private List<Map<String, Object>> getIndexes(String owner, String objectName) {
        String sql = "SELECT " +
                "    i.relname as index_name, " +
                "    pg_catalog.pg_get_indexdef(idx.indexrelid) as index_definition, " +
                "    idx.indisunique as is_unique, " +
                "    idx.indisprimary as is_primary, " +
                "    idx.indisclustered as is_clustered, " +
                "    array_to_string(array_agg(a.attname), ', ') as columns " +
                "FROM pg_index idx " +
                "JOIN pg_class i ON idx.indexrelid = i.oid " +
                "JOIN pg_class t ON idx.indrelid = t.oid " +
                "JOIN pg_namespace n ON t.relnamespace = n.oid " +
                "LEFT JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(idx.indkey) " +
                "WHERE n.nspname = ? AND t.relname = ? " +
                "GROUP BY i.relname, idx.indexrelid, idx.indisunique, idx.indisprimary, idx.indisclustered";

        return getJdbcTemplate().queryForList(sql, owner, objectName);
    }

    private Map<String, Object> getTableStatistics(String owner, String objectName) {
        String sql = "SELECT " +
                "    c.reltuples as estimated_row_count, " +
                "    c.relpages as page_count, " +
                "    c.relallvisible as all_visible_pages, " +
                "    pg_stat_get_last_vacuum_time(c.oid) as last_vacuum, " +
                "    pg_stat_get_last_autovacuum_time(c.oid) as last_autovacuum, " +
                "    pg_stat_get_last_analyze_time(c.oid) as last_analyze, " +
                "    pg_stat_get_last_autoanalyze_time(c.oid) as last_autoanalyze, " +
                "    pg_stat_get_live_tuples(c.oid) as live_tuples, " +
                "    pg_stat_get_dead_tuples(c.oid) as dead_tuples, " +
                "    pg_stat_get_mod_since_analyze(c.oid) as modifications_since_analyze " +
                "FROM pg_class c " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "WHERE n.nspname = ? AND c.relname = ?";

        try {
            return getJdbcTemplate().queryForMap(sql, owner, objectName);
        } catch (EmptyResultDataAccessException e) {
            // Return empty statistics with meaningful message instead of throwing
            log.debug("No statistics found for {}.{}", owner, objectName);
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("estimated_row_count", 0);
            emptyStats.put("page_count", 0);
            emptyStats.put("all_visible_pages", 0);
            emptyStats.put("last_vacuum", null);
            emptyStats.put("last_autovacuum", null);
            emptyStats.put("last_analyze", null);
            emptyStats.put("last_autoanalyze", null);
            emptyStats.put("live_tuples", 0);
            emptyStats.put("dead_tuples", 0);
            emptyStats.put("modifications_since_analyze", 0);
            emptyStats.put("message", "Statistics not available for this object");
            return emptyStats;
        }
    }

    private List<Map<String, Object>> getDependencies(String owner, String objectName) {
        String sql = "SELECT " +
                "    dep.refobjname as dependent_object, " +
                "    dep.refobjnamespace as dependent_schema, " +
                "    dep.refobjtype as dependent_type, " +
                "    dep.deptype as dependency_type " +
                "FROM pg_depend d " +
                "JOIN pg_class c ON d.objid = c.oid " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "JOIN LATERAL (SELECT " +
                "    pc.relname as refobjname, " +
                "    pn.nspname as refobjnamespace, " +
                "    CASE WHEN pc.relkind = 'r' THEN 'TABLE' " +
                "         WHEN pc.relkind = 'v' THEN 'VIEW' " +
                "         ELSE 'OTHER' END as refobjtype, " +
                "    d.deptype " +
                "    FROM pg_class pc " +
                "    JOIN pg_namespace pn ON pc.relnamespace = pn.oid " +
                "    WHERE pc.oid = d.refobjid) dep ON true " +
                "WHERE n.nspname = ? AND c.relname = ? " +
                "AND d.deptype IN ('n', 'a')";

        return getJdbcTemplate().queryForList(sql, owner, objectName);
    }


    private String getValidationSql(String objectType) {
        String relKind = getRelationKind(objectType);
        if (relKind != null) {
            return "SELECT " +
                    "    c.relname as object_name, " +
                    "    'VALID' as status " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = ?";
        }
        return "SELECT " +
                "    c.relname as object_name, " +
                "    'VALID' as status " +
                "FROM pg_class c " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "WHERE n.nspname = ? AND c.relname = ?";
    }

    private List<Map<String, Object>> getTableColumnsPaginatedInternal(String owner, String tableName,
                                                                       int offset, int pageSize) {
        String sql = "SELECT " +
                "    ordinal_position as column_id, " +
                "    column_name, " +
                "    data_type, " +
                "    character_maximum_length as data_length, " +
                "    numeric_precision as data_precision, " +
                "    numeric_scale as data_scale, " +
                "    is_nullable as nullable, " +
                "    column_default as data_default, " +
                "    character_maximum_length as char_length, " +
                "    NULL as char_used " +
                "FROM information_schema.columns " +
                "WHERE table_schema = ? AND table_name = ? " +
                "ORDER BY ordinal_position " +
                "OFFSET ? LIMIT ?";

        return getJdbcTemplate().queryForList(sql, owner, tableName, offset, pageSize);
    }

    private List<Map<String, Object>> getArgumentsPaginatedInternal(String owner, String objectName,
                                                                    int offset, int pageSize) {
        try {
            // Get function OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            Long funcOid = getJdbcTemplate().queryForObject(oidSql, Long.class, owner, objectName);

            if (funcOid != null) {
                // Get function arguments
                String argsSql = "SELECT " +
                        "    proargnames, " +
                        "    proargtypes, " +
                        "    proargmodes " +
                        "FROM pg_proc WHERE oid = ?";

                Map<String, Object> funcInfo = getJdbcTemplate().queryForMap(argsSql, funcOid);

                Array proargnames = (Array) funcInfo.get("proargnames");
                Array proargtypes = (Array) funcInfo.get("proargtypes");
                Array proargmodes = (Array) funcInfo.get("proargmodes");

                String[] argNames = proargnames != null ? (String[]) proargnames.getArray() : new String[0];
                String[] argTypes = proargtypes != null ? (String[]) proargtypes.getArray() : new String[0];
                String[] argModes = proargmodes != null ? (String[]) proargmodes.getArray() : new String[0];

                List<Map<String, Object>> params = new ArrayList<>();
                for (int i = 0; i < argNames.length; i++) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("argument_name", argNames[i]);
                    param.put("position", i + 1);
                    param.put("sequence", i + 1);
                    param.put("data_type", getDataTypeName(argTypes[i]));
                    param.put("in_out", getParameterMode(i < argModes.length ? argModes[i] : "i"));
                    param.put("defaulted", "N");

                    params.add(param);
                }

                // Apply pagination
                int fromIndex = Math.min(offset, params.size());
                int toIndex = Math.min(offset + pageSize, params.size());
                return params.subList(fromIndex, toIndex);
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.debug("Error getting arguments: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getBasicObjectInfo(String owner, String objectName, String objectType) {
        try {
            String sql = getBasicInfoSql(objectType);
            return getJdbcTemplate().queryForMap(sql, owner, objectName);
        } catch (Exception e) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("owner", owner);
            empty.put("object_name", objectName);
            empty.put("object_type", objectType);
            empty.put("status", "VALID");
            return empty;
        }
    }

    private String getSourceForTableOrView(String owner, String objectName, String objectType) {
        try {
            if ("TABLE".equalsIgnoreCase(objectType)) {
                return generateTableDDL(owner, objectName);
            } else if ("VIEW".equalsIgnoreCase(objectType)) {
                return generateViewDDL(owner, objectName);
            } else if ("MATERIALIZED VIEW".equalsIgnoreCase(objectType)) {
                return generateMaterializedViewDDL(owner, objectName);
            }
            return null;
        } catch (Exception e) {
            log.warn("Could not generate source for {} {}.{}: {}", objectType, owner, objectName, e.getMessage());
            return null;
        }
    }

    private String generateTableDDL(String owner, String tableName) {
        try {
            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE ");
            if (!owner.equalsIgnoreCase(getCurrentSchema())) {
                ddl.append(owner).append(".");
            }
            ddl.append(tableName).append(" (\n");

            List<Map<String, Object>> columns = getTableColumns(owner, tableName);
            for (int i = 0; i < columns.size(); i++) {
                Map<String, Object> col = columns.get(i);
                ddl.append("    ").append(col.get("column_name")).append(" ").append(col.get("data_type"));

                Integer charMaxLen = (Integer) col.get("character_maximum_length");
                Integer numericPrecision = (Integer) col.get("numeric_precision");
                Integer numericScale = (Integer) col.get("numeric_scale");
                String dataType = (String) col.get("data_type");

                if (charMaxLen != null && charMaxLen > 0 &&
                        ("character varying".equals(dataType) || "varchar".equals(dataType))) {
                    ddl.append("(").append(charMaxLen).append(")");
                } else if (numericPrecision != null &&
                        ("numeric".equals(dataType) || "decimal".equals(dataType))) {
                    ddl.append("(").append(numericPrecision);
                    if (numericScale != null && numericScale > 0) {
                        ddl.append(",").append(numericScale);
                    }
                    ddl.append(")");
                }

                if ("NO".equals(col.get("is_nullable"))) {
                    ddl.append(" NOT NULL");
                }

                Object defaultValue = col.get("column_default");
                if (defaultValue != null && !defaultValue.toString().isEmpty()) {
                    ddl.append(" DEFAULT ").append(defaultValue);
                }

                if (i < columns.size() - 1) {
                    ddl.append(",");
                }
                ddl.append("\n");
            }

            ddl.append(");");
            return ddl.toString();

        } catch (Exception e) {
            log.debug("generateTableDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateViewDDL(String owner, String viewName) {
        try {
            String sql = "SELECT 'CREATE OR REPLACE VIEW ' || " +
                    "CASE WHEN ? != current_schema() THEN ? || '.' ELSE '' END || ? || ' AS ' || " +
                    "pg_get_viewdef(c.oid, true) as ddl " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'v'";

            return getJdbcTemplate().queryForObject(sql, String.class,
                    owner, owner, viewName, owner, viewName);
        } catch (Exception e) {
            log.debug("generateViewDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateMaterializedViewDDL(String owner, String mvName) {
        try {
            String sql = "SELECT 'CREATE MATERIALIZED VIEW ' || " +
                    "CASE WHEN ? != current_schema() THEN ? || '.' ELSE '' END || ? || ' AS ' || " +
                    "pg_get_viewdef(c.oid, true) as ddl " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'm'";

            return getJdbcTemplate().queryForObject(sql, String.class,
                    owner, owner, mvName, owner, mvName);
        } catch (Exception e) {
            log.debug("generateMaterializedViewDDL failed: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> getIndexDetails(String owner, String indexName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    c.relname as index_name, " +
                    "    t.relname as table_name, " +
                    "    CASE WHEN i.indisunique THEN 'UNIQUE' ELSE 'NONUNIQUE' END as uniqueness, " +
                    "    'VALID' as status " +
                    "FROM pg_index i " +
                    "JOIN pg_class c ON i.indexrelid = c.oid " +
                    "JOIN pg_class t ON i.indrelid = t.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ?";
            return getJdbcTemplate().queryForMap(sql, owner, indexName);
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return details;
        }
    }

    private String getDataTypeName(String typeOid) {
        try {
            String sql = "SELECT typname FROM pg_type WHERE oid = ?::regtype::oid";
            return getJdbcTemplate().queryForObject(sql, String.class, typeOid);
        } catch (Exception e) {
            return typeOid;
        }
    }

    private String getParameterMode(String modeCode) {
        if (modeCode == null) return "IN";
        switch (modeCode) {
            case "i": return "IN";
            case "o": return "OUT";
            case "b": return "IN/OUT";
            case "v": return "VARIADIC";
            default: return "IN";
        }
    }

    private String getRelationKind(String objectType) {
        if (objectType == null) return null;
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "TABLE": return "r";
            case "VIEW": return "v";
            case "MATERIALIZED VIEW": return "m";
            case "FUNCTION": return "f";
            case "PROCEDURE": return "p";
            case "SEQUENCE": return "S";
            case "INDEX": return "i";
            default: return null;
        }
    }

    private List<Map<String, Object>> getTableColumns(String owner, String tableName) {
        try {
            String sql = "SELECT column_name, data_type, character_maximum_length, " +
                    "numeric_precision, numeric_scale, is_nullable, column_default " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ? " +
                    "ORDER BY ordinal_position";
            return getJdbcTemplate().queryForList(sql, owner, tableName);
        } catch (Exception e) {
            log.debug("getTableColumns failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }


    private boolean isTableLikeObject(String owner, String objectName) {
        String sql = "SELECT relkind FROM pg_class c " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "WHERE n.nspname = ? AND c.relname = ?";

        try {
            String relKind = getJdbcTemplate().queryForObject(sql, String.class, owner, objectName);
            return "r".equals(relKind) || "v".equals(relKind) || "m".equals(relKind);
        } catch (Exception e) {
            return false;
        }
    }
}