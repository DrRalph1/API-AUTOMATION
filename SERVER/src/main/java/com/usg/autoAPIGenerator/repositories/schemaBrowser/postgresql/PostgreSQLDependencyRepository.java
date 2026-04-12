package com.usg.autoAPIGenerator.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostgreSQLDependencyRepository extends PostgreSQLRepository {

    // ============================================================
    // DEPENDENCY METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getUsedBy(String objectName, String objectType, String owner) {
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            log.info("Finding objects that depend on {}.{} ({})", owner, objectName, objectType);

            // Query dependencies from PostgreSQL system catalogs
            List<Map<String, Object>> dependencies = getPostgreSQLDependencies(objectName, objectType, owner);

            log.info("Found {} dependent objects", dependencies.size());

            // Transform to frontend-friendly format
            List<Map<String, Object>> transformed = dependencies.stream().map(dep -> {
                Map<String, Object> item = new HashMap<>();
                String dependentType = (String) dep.get("dependent_type");

                item.put("id", "dep-" + System.currentTimeMillis() + "-" + dep.get("dependent_name"));
                item.put("name", dep.get("dependent_name"));
                item.put("owner", dep.get("dependent_owner"));
                item.put("type", dependentType);
                item.put("object_type", dependentType);
                item.put("status", dep.get("dependent_status"));
                item.put("created", dep.get("dependent_created"));
                item.put("lastModified", dep.get("dependent_modified"));
                item.put("dependencyType", dep.get("dependency_type"));
                item.put("parameterCount", dep.get("parameter_count") != null ? dep.get("parameter_count") : 0);
                item.put("icon", getObjectTypeIcon(dependentType));

                if ("PROCEDURE".equals(dependentType) || "FUNCTION".equals(dependentType)) {
                    item.put("hasParameters", ((Number) dep.get("parameter_count")).intValue() > 0);
                }

                item.put("reference", dep.get("referenced_owner") + "." +
                        dep.get("referenced_name") + " (" +
                        dep.get("referenced_type") + ")");

                return item;
            }).collect(Collectors.toList());

            return transformed;

        } catch (Exception e) {
            log.error("Error in getUsedBy for {}.{}: {}", owner, objectName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getUsedByPaginated(String objectName, String objectType,
                                                  String owner, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            int offset = (page - 1) * pageSize;

            // Get total count first
            int totalCount = getUsedByCount(objectName, objectType, owner);

            // Get paginated dependencies
            List<Map<String, Object>> dependencies = getPostgreSQLDependenciesPaginated(
                    objectName, objectType, owner, offset, pageSize);

            // Transform to frontend-friendly format
            List<Map<String, Object>> transformed = dependencies.stream().map(dep -> {
                Map<String, Object> item = new HashMap<>();
                String dependentType = (String) dep.get("dependent_type");

                item.put("id", "dep-" + System.currentTimeMillis() + "-" + dep.get("dependent_name"));
                item.put("name", dep.get("dependent_name"));
                item.put("owner", dep.get("dependent_owner"));
                item.put("type", dependentType);
                item.put("object_type", dependentType);
                item.put("status", dep.get("dependent_status"));
                item.put("created", dep.get("dependent_created"));
                item.put("lastModified", dep.get("dependent_modified"));
                item.put("dependencyType", dep.get("dependency_type"));
                item.put("parameterCount", dep.get("parameter_count") != null ? dep.get("parameter_count") : 0);
                item.put("icon", getObjectTypeIcon(dependentType));

                if ("PROCEDURE".equals(dependentType) || "FUNCTION".equals(dependentType)) {
                    item.put("hasParameters", ((Number) dep.get("parameter_count")).intValue() > 0);
                }

                return item;
            }).collect(Collectors.toList());

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getUsedByPaginated for {}.{}: {}", owner, objectName, e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("error", e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getDependencyHierarchy(String objectName, String objectType, String owner) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            // Get objects that this object depends on (referenced_by)
            List<Map<String, Object>> referencedBy = getObjectsThisDependsOn(objectName, objectType, owner);

            // Get objects that depend on this object (used_by)
            List<Map<String, Object>> usedBy = getObjectsThatDependOnThis(objectName, objectType, owner);

            // Transform results
            List<Map<String, Object>> transformedReferencedBy = referencedBy.stream().map(ref -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", ref.get("referenced_name"));
                item.put("owner", ref.get("referenced_owner"));
                item.put("type", ref.get("referenced_type"));
                item.put("icon", getObjectTypeIcon((String) ref.get("referenced_type")));
                return item;
            }).collect(Collectors.toList());

            List<Map<String, Object>> transformedUsedBy = usedBy.stream().map(use -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", use.get("dependent_name"));
                item.put("owner", use.get("dependent_owner"));
                item.put("type", use.get("dependent_type"));
                item.put("icon", getObjectTypeIcon((String) use.get("dependent_type")));
                item.put("status", use.get("dependent_status"));
                return item;
            }).collect(Collectors.toList());

            result.put("objectName", objectName);
            result.put("objectType", objectType);
            result.put("owner", owner);
            result.put("dependsOn", transformedReferencedBy);
            result.put("usedBy", transformedUsedBy);
            result.put("dependsOnCount", transformedReferencedBy.size());
            result.put("usedByCount", transformedUsedBy.size());

        } catch (Exception e) {
            log.error("Error in getDependencyHierarchy for {}.{}: {}", owner, objectName, e.getMessage(), e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getUsedBySummary(String objectName, String objectType, String owner) {
        Map<String, Object> summary = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            // Get the OID and also verify the object exists
            String objectOid = getObjectOid(owner, objectName, objectType);
            if (objectOid == null) {
                log.debug("Object {}.{} of type {} not found", owner, objectName, objectType);
                summary.put("byType", new ArrayList<>());
                summary.put("totalCount", 0);
                return summary;
            }

            // Convert OID string to long for numeric comparison
            long oidValue = Long.parseLong(objectOid);

            // Corrected query - we need to join to pg_class c to get the dependent object's relkind
            String sql = "SELECT " +
                    "    CASE WHEN dep_obj.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN dep_obj.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN dep_obj.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN dep_obj.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN dep_obj.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN dep_obj.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN dep_obj.relkind = 'i' THEN 'INDEX' " +
                    "         WHEN dep_obj.relkind = 't' THEN 'TOAST TABLE' " +
                    "         ELSE 'OTHER' END as dependent_type, " +
                    "    COUNT(DISTINCT dep.objid) as count, " +
                    "    COUNT(DISTINCT CASE WHEN dep_obj.relkind IS NOT NULL THEN dep.objid END) as valid_count " +
                    "FROM pg_depend dep " +
                    "JOIN pg_class dep_obj ON dep.objid = dep_obj.oid " +
                    "JOIN pg_namespace n ON dep_obj.relnamespace = n.oid " +
                    "WHERE dep.refobjid = ? " +
                    "AND dep.deptype IN ('n', 'a') " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "AND dep.objid != dep.refobjid " +
                    "GROUP BY dep_obj.relkind " +
                    "ORDER BY dependent_type";

            List<Map<String, Object>> typeSummary = getJdbcTemplate().queryForList(sql, oidValue);

            summary.put("byType", typeSummary);
            summary.put("totalCount", typeSummary.stream()
                    .mapToInt(m -> ((Number) m.get("count")).intValue()).sum());

        } catch (Exception e) {
            log.error("Error in getUsedBySummary for {}.{}: {}", owner, objectName, e.getMessage(), e);
            summary.put("error", e.getMessage());
            summary.put("byType", new ArrayList<>());
            summary.put("totalCount", 0);
        }

        return summary;
    }

    public int getUsedByCount(String objectName, String objectType, String owner) {
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            String objectOid = getObjectOid(owner, objectName, objectType);
            if (objectOid == null) {
                return 0;
            }

            long oidValue = Long.parseLong(objectOid);

            // Count objects that depend on this object
            String sql = "SELECT COUNT(DISTINCT dep.objid) FROM pg_depend dep " +
                    "WHERE dep.refobjid = ? " +
                    "AND dep.deptype IN ('n', 'a') " + // normal and auto dependencies
                    "AND dep.objid != dep.refobjid"; // exclude self-references

            Integer count = getJdbcTemplate().queryForObject(sql, Integer.class, oidValue);
            return count != null ? count : 0;

        } catch (Exception e) {
            log.error("Error in getUsedByCount for {}.{}: {}", owner, objectName, e.getMessage());
            return 0;
        }
    }

    private List<Map<String, Object>> getPostgreSQLDependencies(String objectName, String objectType, String owner) {
        try {
            String objectOid = getObjectOid(owner, objectName, objectType);
            if (objectOid == null) {
                return new ArrayList<>();
            }

            long oidValue = Long.parseLong(objectOid);

            String sql = "SELECT DISTINCT " +
                    "    n.nspname as dependent_owner, " +
                    "    c.relname as dependent_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "         ELSE 'OTHER' END as dependent_type, " +
                    "    ? as referenced_owner, " +
                    "    ? as referenced_name, " +
                    "    ? as referenced_type, " +
                    "    dep.deptype as dependency_type, " +
                    "    'VALID' as dependent_status, " +
                    "    c.relowner as dependent_created, " +
                    "    c.relowner as dependent_modified, " +
                    "    (SELECT COUNT(*) FROM pg_proc p WHERE p.proname = c.relname) as parameter_count " +
                    "FROM pg_depend dep " +
                    "JOIN pg_class c ON dep.objid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE dep.refobjid = ? " +
                    "AND dep.deptype IN ('n', 'a') " +
                    "AND dep.objid != dep.refobjid " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "ORDER BY dependent_type, dependent_name";

            return getJdbcTemplate().queryForList(sql, owner, objectName, objectType, oidValue);

        } catch (Exception e) {
            log.debug("Error getting PostgreSQL dependencies: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getPostgreSQLDependenciesPaginated(String objectName, String objectType,
                                                                         String owner, int offset, int pageSize) {
        try {
            String objectOid = getObjectOid(owner, objectName, objectType);
            if (objectOid == null) {
                return new ArrayList<>();
            }

            long oidValue = Long.parseLong(objectOid);

            String sql = "WITH dependencies AS ( " +
                    "  SELECT " +
                    "      n.nspname as dependent_owner, " +
                    "      c.relname as dependent_name, " +
                    "      CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "           WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "           WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "           WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "           WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "           WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "           WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "           ELSE 'OTHER' END as dependent_type, " +
                    "      ? as referenced_owner, " +
                    "      ? as referenced_name, " +
                    "      ? as referenced_type, " +
                    "      dep.deptype as dependency_type, " +
                    "      'VALID' as dependent_status, " +
                    "      c.relowner as dependent_created, " +
                    "      c.relowner as dependent_modified, " +
                    "      (SELECT COUNT(*) FROM pg_proc p WHERE p.proname = c.relname) as parameter_count " +
                    "  FROM pg_depend dep " +
                    "  JOIN pg_class c ON dep.objid = c.oid " +
                    "  JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "  WHERE dep.refobjid = ? " +
                    "  AND dep.deptype IN ('n', 'a') " +
                    "  AND dep.objid != dep.refobjid " +
                    "  AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    ") " +
                    "SELECT *, ROW_NUMBER() OVER (ORDER BY dependent_type, dependent_name) as rnum " +
                    "FROM dependencies " +
                    "LIMIT ? OFFSET ?";

            return getJdbcTemplate().queryForList(sql, owner, objectName, objectType, oidValue, pageSize, offset);

        } catch (Exception e) {
            log.debug("Error getting paginated PostgreSQL dependencies: {}", e.getMessage());
            return new ArrayList<>();
        }
    }




    private List<Map<String, Object>> getObjectsThisDependsOn(String objectName, String objectType, String owner) {
        try {
            String objectOid = getObjectOid(owner, objectName, objectType);
            if (objectOid == null) {
                return new ArrayList<>();
            }

            long oidValue;
            try {
                oidValue = Long.parseLong(objectOid);
            } catch (NumberFormatException e) {
                log.error("Invalid OID format for {}.{}: {}", owner, objectName, objectOid);
                return new ArrayList<>();
            }

            String sql = "SELECT DISTINCT " +
                    "    n.nspname as referenced_owner, " +
                    "    c.relname as referenced_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         ELSE 'OTHER' END as referenced_type " +
                    "FROM pg_depend dep " +
                    "JOIN pg_class c ON dep.refobjid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE dep.objid = ? " +
                    "AND dep.deptype IN ('n', 'a') " +
                    "AND dep.objid != dep.refobjid " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "ORDER BY referenced_type, referenced_name";

            return getJdbcTemplate().queryForList(sql, oidValue);

        } catch (Exception e) {
            log.debug("Error getting objects this depends on: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getObjectsThatDependOnThis(String objectName, String objectType, String owner) {
        try {
            String objectOid = getObjectOid(owner, objectName, objectType);
            if (objectOid == null) {
                return new ArrayList<>();
            }

            long oidValue;
            try {
                oidValue = Long.parseLong(objectOid);
            } catch (NumberFormatException e) {
                log.error("Invalid OID format for {}.{}: {}", owner, objectName, objectOid);
                return new ArrayList<>();
            }

            String sql = "SELECT DISTINCT " +
                    "    n.nspname as dependent_owner, " +
                    "    c.relname as dependent_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         ELSE 'OTHER' END as dependent_type, " +
                    "    'VALID' as dependent_status " +
                    "FROM pg_depend dep " +
                    "JOIN pg_class c ON dep.objid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE dep.refobjid = ? " +
                    "AND dep.deptype IN ('n', 'a') " +
                    "AND dep.objid != dep.refobjid " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "ORDER BY dependent_type, dependent_name";

            return getJdbcTemplate().queryForList(sql, oidValue);

        } catch (Exception e) {
            log.debug("Error getting objects that depend on this: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================


    private String getObjectOid(String owner, String objectName, String objectType) {
        try {
            String relKind = getRelationKind(objectType);
            if (relKind == null) {
                // Try to find by name without type restriction
                String sql = "SELECT c.oid FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ? " +
                        "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                        "LIMIT 1";
                return getJdbcTemplate().queryForObject(sql, String.class, owner, objectName.toLowerCase());
            } else {
                String sql = "SELECT c.oid FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = ? " +
                        "AND n.nspname NOT IN ('pg_catalog', 'information_schema')";
                return getJdbcTemplate().queryForObject(sql, String.class, owner, objectName.toLowerCase(), relKind);
            }
        } catch (Exception e) {
            log.debug("Object {}.{} of type {} not found", owner, objectName, objectType);
            return null;
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
            case "FOREIGN TABLE": return "f";
            default: return null;
        }
    }
}