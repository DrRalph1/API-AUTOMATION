package com.usg.autoAPIGenerator.repositories.schemaBrowser.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OracleDependencyRepository extends OracleRepository {

    // ============================================================
    // DEPENDENCY METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getUsedBy(String objectName, String objectType, String owner) {
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            log.info("Finding objects that depend on {}.{} ({})", owner, objectName, objectType);

            // Query all dependencies where this object is referenced
            String sql = "SELECT " +
                    "    d.owner as dependent_owner, " +
                    "    d.name as dependent_name, " +
                    "    d.type as dependent_type, " +
                    "    d.referenced_owner, " +
                    "    d.referenced_name, " +
                    "    d.referenced_type, " +
                    "    d.dependency_type, " +
                    "    o.status as dependent_status, " +
                    "    o.created as dependent_created, " +
                    "    o.last_ddl_time as dependent_modified, " +
                    "    (SELECT COUNT(*) FROM all_arguments a " +
                    "     WHERE a.owner = d.owner " +
                    "       AND a.object_name = d.name " +
                    "       AND (a.package_name = d.name OR a.package_name IS NULL) " +
                    "       AND a.argument_name IS NOT NULL) as parameter_count " +
                    "FROM all_dependencies d " +
                    "JOIN all_objects o ON d.owner = o.owner AND d.name = o.object_name AND d.type = o.object_type " +
                    "WHERE UPPER(d.referenced_owner) = UPPER(?) " +
                    "  AND UPPER(d.referenced_name) = UPPER(?) " +
                    "  AND UPPER(d.referenced_type) = UPPER(?) " +
                    "ORDER BY d.type, d.name";

            List<Map<String, Object>> dependencies = getJdbcTemplate().queryForList(
                    sql, owner, objectName, objectType);

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
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;

            // Get total count first
            String countSql = "SELECT COUNT(*) FROM all_dependencies " +
                    "WHERE UPPER(referenced_owner) = UPPER(?) " +
                    "  AND UPPER(referenced_name) = UPPER(?) " +
                    "  AND UPPER(referenced_type) = UPPER(?)";

            int totalCount = getJdbcTemplate().queryForObject(
                    countSql, Integer.class, owner, objectName, objectType);

            // Get paginated dependencies
            String dataSql = "SELECT * FROM ( " +
                    "  SELECT a.*, ROWNUM rnum FROM ( " +
                    "    SELECT " +
                    "        d.owner as dependent_owner, " +
                    "        d.name as dependent_name, " +
                    "        d.type as dependent_type, " +
                    "        d.referenced_owner, " +
                    "        d.referenced_name, " +
                    "        d.referenced_type, " +
                    "        d.dependency_type, " +
                    "        o.status as dependent_status, " +
                    "        o.created as dependent_created, " +
                    "        o.last_ddl_time as dependent_modified, " +
                    "        (SELECT COUNT(*) FROM all_arguments a " +
                    "         WHERE a.owner = d.owner " +
                    "           AND a.object_name = d.name " +
                    "           AND (a.package_name = d.name OR a.package_name IS NULL) " +
                    "           AND a.argument_name IS NOT NULL) as parameter_count " +
                    "    FROM all_dependencies d " +
                    "    JOIN all_objects o ON d.owner = o.owner AND d.name = o.object_name AND d.type = o.object_type " +
                    "    WHERE UPPER(d.referenced_owner) = UPPER(?) " +
                    "      AND UPPER(d.referenced_name) = UPPER(?) " +
                    "      AND UPPER(d.referenced_type) = UPPER(?) " +
                    "    ORDER BY d.type, d.name " +
                    "  ) a WHERE ROWNUM <= ? " +
                    ") WHERE rnum > ?";

            List<Map<String, Object>> dependencies = getJdbcTemplate().queryForList(
                    dataSql, owner, objectName, objectType, offset + pageSize, offset);

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
                owner = getCurrentUser();
            }

            // Get objects that this object depends on (referenced_by)
            String referencedBySql = "SELECT " +
                    "    referenced_owner, " +
                    "    referenced_name, " +
                    "    referenced_type " +
                    "FROM all_dependencies " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "  AND UPPER(name) = UPPER(?) " +
                    "  AND UPPER(type) = UPPER(?) " +
                    "ORDER BY referenced_type, referenced_name";

            List<Map<String, Object>> referencedBy = getJdbcTemplate().queryForList(
                    referencedBySql, owner, objectName, objectType);

            // Get objects that depend on this object (used_by)
            String usedBySql = "SELECT " +
                    "    owner as dependent_owner, " +
                    "    name as dependent_name, " +
                    "    type as dependent_type " +
                    "FROM all_dependencies " +
                    "WHERE UPPER(referenced_owner) = UPPER(?) " +
                    "  AND UPPER(referenced_name) = UPPER(?) " +
                    "  AND UPPER(referenced_type) = UPPER(?) " +
                    "ORDER BY type, name";

            List<Map<String, Object>> usedBy = getJdbcTemplate().queryForList(
                    usedBySql, owner, objectName, objectType);

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

                // Get status for dependent objects
                try {
                    String statusSql = "SELECT status FROM all_objects " +
                            "WHERE owner = ? AND object_name = ? AND object_type = ?";
                    String status = getJdbcTemplate().queryForObject(
                            statusSql, String.class,
                            use.get("dependent_owner"),
                            use.get("dependent_name"),
                            use.get("dependent_type"));
                    item.put("status", status);
                } catch (Exception e) {
                    item.put("status", "UNKNOWN");
                }

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

    public int getUsedByCount(String objectName, String objectType, String owner) {
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            String sql = "SELECT COUNT(*) FROM all_dependencies " +
                    "WHERE UPPER(referenced_owner) = UPPER(?) " +
                    "  AND UPPER(referenced_name) = UPPER(?) " +
                    "  AND UPPER(referenced_type) = UPPER(?)";

            return getJdbcTemplate().queryForObject(
                    sql, Integer.class, owner, objectName, objectType);

        } catch (Exception e) {
            log.error("Error in getUsedByCount for {}.{}: {}", owner, objectName, e.getMessage());
            return 0;
        }
    }

    public Map<String, Object> getUsedBySummary(String objectName, String objectType, String owner) {
        Map<String, Object> summary = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            String sql = "SELECT " +
                    "    type as dependent_type, " +
                    "    COUNT(*) as count, " +
                    "    SUM(CASE WHEN o.status = 'VALID' THEN 1 ELSE 0 END) as valid_count, " +
                    "    SUM(CASE WHEN o.status = 'INVALID' THEN 1 ELSE 0 END) as invalid_count " +
                    "FROM all_dependencies d " +
                    "JOIN all_objects o ON d.owner = o.owner AND d.name = o.object_name AND d.type = o.object_type " +
                    "WHERE UPPER(d.referenced_owner) = UPPER(?) " +
                    "  AND UPPER(d.referenced_name) = UPPER(?) " +
                    "  AND UPPER(d.referenced_type) = UPPER(?) " +
                    "GROUP BY type " +
                    "ORDER BY type";

            List<Map<String, Object>> typeSummary = getJdbcTemplate().queryForList(
                    sql, owner, objectName, objectType);

            summary.put("byType", typeSummary);
            summary.put("totalCount", typeSummary.stream()
                    .mapToInt(m -> ((Number) m.get("count")).intValue()).sum());

        } catch (Exception e) {
            log.error("Error in getUsedBySummary for {}.{}: {}", owner, objectName, e.getMessage());
            summary.put("error", e.getMessage());
        }

        return summary;
    }
}