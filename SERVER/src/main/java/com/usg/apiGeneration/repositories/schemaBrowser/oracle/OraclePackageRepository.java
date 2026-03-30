package com.usg.apiGeneration.repositories.schemaBrowser.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OraclePackageRepository extends OracleRepository {

    // ============================================================
    // PACKAGE METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getAllPackagesForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String distinctSql = "SELECT DISTINCT object_name as package_name FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') ORDER BY object_name";

            List<Map<String, Object>> packageNames = getJdbcTemplate().queryForList(distinctSql);

            for (Map<String, Object> pkg : packageNames) {
                String pkgName = (String) pkg.get("package_name");
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "package-" + System.currentTimeMillis() + "-" + pkgName);
                transformed.put("name", pkgName);
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "PACKAGE");
                transformed.put("isSynonym", false);

                String specStatus = getPackageSpecStatus(pkgName);
                String bodyStatus = getPackageBodyStatus(pkgName);

                String status = "VALID";
                if ("INVALID".equals(specStatus) || "INVALID".equals(bodyStatus)) {
                    status = "INVALID";
                }

                transformed.put("status", status);
                transformed.put("specStatus", specStatus);
                transformed.put("bodyStatus", bodyStatus);
                transformed.put("created", getPackageCreated(pkgName));
                transformed.put("lastModified", getPackageLastModified(pkgName));
                transformed.put("icon", "package");
                result.add(transformed);
            }

            result.addAll(getPackageSynonyms());
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (packages + synonyms)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllPackagesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    public Map<String, Object> getAllPackagesForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String packageCountSql = "SELECT COUNT(DISTINCT object_name) FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY')";
            int packageCount = getJdbcTemplate().queryForObject(packageCountSql, Integer.class);

            String synonymCountSql =
                    "SELECT COUNT(*) FROM user_synonyms s " +
                            "WHERE EXISTS (SELECT 1 FROM all_objects " +
                            "              WHERE owner = s.table_owner " +
                            "                AND object_name = s.table_name " +
                            "                AND object_type = 'PACKAGE') " +
                            "   OR (s.db_link IS NOT NULL)";

            int synonymCount = 0;
            try {
                synonymCount = getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
            } catch (Exception e) {
                log.warn("Error counting synonyms for packages: {}", e.getMessage());
            }

            int totalCount = packageCount + synonymCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (packageCount > 0 && offset < packageCount) {
                int pkgOffset = offset;
                int pkgLimit = Math.min(pageSize, packageCount - pkgOffset);

                if (pkgLimit > 0) {
                    String packageSql = "SELECT DISTINCT object_name as name, 'PACKAGE' as type " +
                            "FROM user_objects " +
                            "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') " +
                            "ORDER BY object_name " +
                            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    List<Map<String, Object>> packages = getJdbcTemplate().queryForList(packageSql, pkgOffset, pkgLimit);
                    for (Map<String, Object> pkg : packages) {
                        String pkgName = (String) pkg.get("name");
                        String specStatus = getPackageSpecStatus(pkgName);
                        String bodyStatus = getPackageBodyStatus(pkgName);
                        String status = "VALID";
                        if ("INVALID".equals(specStatus) || "INVALID".equals(bodyStatus)) {
                            status = "INVALID";
                        }
                        pkg.put("status", status);
                        pkg.put("specStatus", specStatus);
                        pkg.put("bodyStatus", bodyStatus);
                        pkg.put("created", getPackageCreated(pkgName));
                        pkg.put("lastModified", getPackageLastModified(pkgName));
                    }
                    allItems.addAll(packages);
                }
            }

            if (allItems.size() < pageSize && synonymCount > 0) {
                addPackageSynonymsPaginated(allItems, page, pageSize, packageCount);
            }

            List<Map<String, Object>> transformed = transformPackageItems(allItems);
            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getAllPackagesForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }

    public Map<String, Object> getPackageDetails(String owner, String packageName) {
        Map<String, Object> details = new HashMap<>();

        try {
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                // Get package spec
                String specSql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE'";

                try {
                    Map<String, Object> spec = getJdbcTemplate().queryForMap(specSql, packageName);
                    details.put("specification", spec);

                    // Get package spec source
                    String sourceSql = "SELECT text FROM user_source " +
                            "WHERE UPPER(name) = UPPER(?) AND type = 'PACKAGE' " +
                            "ORDER BY line";

                    List<String> sourceLines = getJdbcTemplate().queryForList(sourceSql, String.class, packageName);
                    if (!sourceLines.isEmpty()) {
                        details.put("specSource", String.join("", sourceLines));
                    }

                } catch (EmptyResultDataAccessException e) {
                    details.put("specification", null);
                }

                // Get package body
                String bodySql = "SELECT " +
                        "    object_name, " +
                        "    object_type, " +
                        "    status, " +
                        "    created, " +
                        "    last_ddl_time, " +
                        "    temporary, " +
                        "    generated, " +
                        "    secondary " +
                        "FROM user_objects " +
                        "WHERE UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE BODY'";

                try {
                    Map<String, Object> body = getJdbcTemplate().queryForMap(bodySql, packageName);
                    details.put("body", body);

                    // Get package body source
                    String bodySourceSql = "SELECT text FROM user_source " +
                            "WHERE UPPER(name) = UPPER(?) AND type = 'PACKAGE BODY' " +
                            "ORDER BY line";

                    List<String> bodySourceLines = getJdbcTemplate().queryForList(bodySourceSql, String.class, packageName);
                    if (!bodySourceLines.isEmpty()) {
                        details.put("bodySource", String.join("", bodySourceLines));
                    }

                } catch (EmptyResultDataAccessException e) {
                    details.put("body", null);
                }

                // Get package procedures and functions
                Map<String, Object> items = getPackageItems(packageName);
                details.putAll(items);

                // Get package variables
                String varSql = "SELECT DISTINCT " +
                        "    procedure_name as variable_name " +
                        "FROM user_identifiers " +
                        "WHERE UPPER(name) = UPPER(?) AND object_type = 'PACKAGE' " +
                        "AND usage = 'DECLARATION' AND type = 'VARIABLE'";

                List<Map<String, Object>> variables = getJdbcTemplate().queryForList(varSql, packageName);
                details.put("variables", variables);
                details.put("variableCount", variables.size());

            } else {
                // Similar for all_* views with owner
                details.putAll(getPackageDetailsFromAllViews(owner, packageName));
            }

        } catch (Exception e) {
            log.warn("Error getting package details for {}.{}: {}", owner, packageName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    public Map<String, Object> getPackageItemsPaginated(String packageName, String owner,
                                                        String itemType, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;
            String upperItemType = itemType.toUpperCase();

            // Build query based on item type
            String countSql;
            String dataSql;

            if ("PROCEDURE".equals(upperItemType)) {
                countSql = "SELECT COUNT(DISTINCT procedure_name) FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND NOT EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "                WHERE a2.owner = all_arguments.owner " +
                        "                AND a2.package_name = all_arguments.package_name " +
                        "                AND a2.procedure_name = all_arguments.procedure_name " +
                        "                AND a2.argument_name IS NULL)";

                dataSql = "SELECT DISTINCT procedure_name, " +
                        "(SELECT COUNT(*) FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NOT NULL) as parameter_count " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND NOT EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "                WHERE a2.owner = a.owner " +
                        "                AND a2.package_name = a.package_name " +
                        "                AND a2.procedure_name = a.procedure_name " +
                        "                AND a2.argument_name IS NULL) " +
                        "ORDER BY procedure_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            } else if ("FUNCTION".equals(upperItemType)) {
                countSql = "SELECT COUNT(DISTINCT procedure_name) FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "            WHERE a2.owner = all_arguments.owner " +
                        "            AND a2.package_name = all_arguments.package_name " +
                        "            AND a2.procedure_name = all_arguments.procedure_name " +
                        "            AND a2.argument_name IS NULL)";

                dataSql = "SELECT DISTINCT procedure_name, " +
                        "(SELECT COUNT(*) FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NOT NULL) as parameter_count, " +
                        "(SELECT data_type FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "AND EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "            WHERE a2.owner = a.owner " +
                        "            AND a2.package_name = a.package_name " +
                        "            AND a2.procedure_name = a.procedure_name " +
                        "            AND a2.argument_name IS NULL) " +
                        "ORDER BY procedure_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            } else {
                // ALL types
                countSql = "SELECT COUNT(DISTINCT procedure_name) FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL";

                dataSql = "SELECT DISTINCT procedure_name, " +
                        "(SELECT COUNT(*) FROM all_arguments a2 " +
                        " WHERE a2.owner = a.owner AND a2.package_name = a.package_name " +
                        " AND a2.procedure_name = a.procedure_name " +
                        " AND a2.argument_name IS NOT NULL) as parameter_count, " +
                        "CASE WHEN EXISTS (SELECT 1 FROM all_arguments a2 " +
                        "                  WHERE a2.owner = a.owner " +
                        "                  AND a2.package_name = a.package_name " +
                        "                  AND a2.procedure_name = a.procedure_name " +
                        "                  AND a2.argument_name IS NULL) " +
                        "     THEN 'FUNCTION' ELSE 'PROCEDURE' END as item_type " +
                        "FROM all_arguments a " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND procedure_name IS NOT NULL " +
                        "ORDER BY procedure_name " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            }

            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class, owner, packageName);
            List<Map<String, Object>> items = getJdbcTemplate().queryForList(
                    dataSql, owner, packageName, offset, pageSize);

            result.put("items", items);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getPackageItemsPaginated for {}.{}: {}", owner, packageName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve package items: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getPackagesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(DISTINCT object_name) FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY')";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT DISTINCT object_name as package_name FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') " +
                    "ORDER BY object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> packages = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", packages);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getPackagesPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    public List<Map<String, Object>> getPackagesBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as package_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_objects o " +
                    "WHERE o.object_type IN ('PACKAGE', 'PACKAGE BODY') AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name, o.object_type";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getPackagesBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> getPackageSynonyms() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String synonymSql =
                    "SELECT " +
                            "    s.synonym_name as name, " +
                            "    'SYNONYM' as type, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                            "        ELSE o.status " +
                            "    END as status, " +
                            "    NULL as created, " +
                            "    NULL as last_ddl_time, " +
                            "    s.table_owner as target_owner, " +
                            "    s.table_name as target_name, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_PACKAGE' " +
                            "        ELSE o.object_type " +
                            "    END as target_type, " +
                            "    s.db_link " +
                            "FROM user_synonyms s " +
                            "LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                            "    AND s.table_name = o.object_name " +
                            "WHERE (o.object_type = 'PACKAGE' OR s.db_link IS NOT NULL) " +
                            "ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);

            for (Map<String, Object> syn : synonyms) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "syn-pkg-" + System.currentTimeMillis() + "-" + syn.get("name"));
                transformed.put("name", syn.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                transformed.put("created", null);
                transformed.put("lastModified", null);
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "PACKAGE");
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);
                transformed.put("isSynonym", true);
                transformed.put("icon", "synonym");
                transformed.put("targetIcon", "package");

                String targetType = (String) syn.get("target_type");
                if (targetType != null) {
                    transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                } else {
                    transformed.put("targetDisplayType", "Package");
                }

                if (syn.get("db_link") == null && syn.get("target_name") != null) {
                    try {
                        String targetSql = "SELECT status, created, last_ddl_time " +
                                "FROM all_objects " +
                                "WHERE owner = ? AND object_name = ? AND object_type = 'PACKAGE'";
                        Map<String, Object> targetInfo = getJdbcTemplate().queryForMap(
                                targetSql, syn.get("target_owner"), syn.get("target_name"));
                        transformed.put("targetStatus", targetInfo.get("status"));
                        transformed.put("targetCreated", targetInfo.get("created"));
                        transformed.put("targetModified", targetInfo.get("last_ddl_time"));
                    } catch (Exception e) {
                        transformed.put("targetStatus", "UNKNOWN");
                    }
                } else if (syn.get("db_link") != null) {
                    transformed.put("targetStatus", "REMOTE");
                }

                result.add(transformed);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for packages: {}", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> getPackageItems(String packageName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String procSql = "SELECT DISTINCT " +
                    "    procedure_name, " +
                    "    overload, " +
                    "    (SELECT COUNT(*) FROM user_arguments a " +
                    "     WHERE a.package_name = ? AND a.object_name = ? " +
                    "     AND a.procedure_name = p.procedure_name " +
                    "     AND a.argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM user_arguments a " +
                    "     WHERE a.package_name = ? AND a.object_name = ? " +
                    "     AND a.procedure_name = p.procedure_name " +
                    "     AND a.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM user_arguments p " +
                    "WHERE UPPER(p.package_name) = UPPER(?) " +
                    "AND p.object_name = p.package_name " +
                    "AND p.procedure_name IS NOT NULL " +
                    "ORDER BY p.procedure_name";

            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(
                    procSql, packageName, packageName, packageName, packageName, packageName);

            // Separate into procedures and functions based on return_type
            List<Map<String, Object>> packageProcedures = procedures.stream()
                    .filter(p -> p.get("return_type") == null)
                    .collect(Collectors.toList());

            List<Map<String, Object>> packageFunctions = procedures.stream()
                    .filter(p -> p.get("return_type") != null)
                    .collect(Collectors.toList());

            details.put("procedures", packageProcedures);
            details.put("functions", packageFunctions);
            details.put("procedureCount", packageProcedures.size());
            details.put("functionCount", packageFunctions.size());

        } catch (Exception e) {
            log.warn("Error getting package items: {}", e.getMessage());
        }
        return details;
    }

    private Map<String, Object> getPackageDetailsFromAllViews(String owner, String packageName) {
        Map<String, Object> details = new HashMap<>();

        try {
            // Get package spec
            String specSql = "SELECT " +
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
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE'";

            try {
                Map<String, Object> spec = getJdbcTemplate().queryForMap(specSql, owner, packageName);
                details.put("specification", spec);
            } catch (EmptyResultDataAccessException e) {
                details.put("specification", null);
            }

            // Get package body
            String bodySql = "SELECT " +
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
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) AND object_type = 'PACKAGE BODY'";

            try {
                Map<String, Object> body = getJdbcTemplate().queryForMap(bodySql, owner, packageName);
                details.put("body", body);
            } catch (EmptyResultDataAccessException e) {
                details.put("body", null);
            }

            // Get procedures and functions from arguments
            String procSql = "SELECT DISTINCT " +
                    "    procedure_name, " +
                    "    overload, " +
                    "    (SELECT COUNT(*) FROM all_arguments a " +
                    "     WHERE a.owner = ? AND a.package_name = ? AND a.object_name = ? " +
                    "     AND a.procedure_name = p.procedure_name " +
                    "     AND a.argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM all_arguments a " +
                    "     WHERE a.owner = ? AND a.package_name = ? AND a.object_name = ? " +
                    "     AND a.procedure_name = p.procedure_name " +
                    "     AND a.argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM all_arguments p " +
                    "WHERE UPPER(p.owner) = UPPER(?) AND UPPER(p.package_name) = UPPER(?) " +
                    "AND p.object_name = p.package_name " +
                    "AND p.procedure_name IS NOT NULL " +
                    "ORDER BY p.procedure_name";

            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(
                    procSql, owner, packageName, packageName, owner, packageName, packageName, owner, packageName);

            List<Map<String, Object>> packageProcedures = procedures.stream()
                    .filter(p -> p.get("return_type") == null)
                    .collect(Collectors.toList());

            List<Map<String, Object>> packageFunctions = procedures.stream()
                    .filter(p -> p.get("return_type") != null)
                    .collect(Collectors.toList());

            details.put("procedures", packageProcedures);
            details.put("functions", packageFunctions);
            details.put("procedureCount", packageProcedures.size());
            details.put("functionCount", packageFunctions.size());

        } catch (Exception e) {
            log.warn("Error getting package details from all_views for {}.{}: {}", owner, packageName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    private void addPackageSynonymsPaginated(List<Map<String, Object>> allItems, int page, int pageSize, int packageCount) {
        try {
            int offset = (page - 1) * pageSize;
            int synOffset;
            int synLimit;

            if (offset >= packageCount) {
                synOffset = offset - packageCount;
                synLimit = pageSize;
            } else {
                synOffset = 0;
                synLimit = pageSize - allItems.size();
            }

            if (synLimit > 0) {
                String synonymSql =
                        "SELECT * FROM ( " +
                                "  SELECT a.*, ROWNUM rnum FROM ( " +
                                "    SELECT " +
                                "      s.synonym_name as name, " +
                                "      'SYNONYM' as type, " +
                                "      CASE " +
                                "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                                "        ELSE o.status " +
                                "      END as status, " +
                                "      NULL as specStatus, " +
                                "      NULL as bodyStatus, " +
                                "      NULL as created, " +
                                "      NULL as lastModified, " +
                                "      s.table_owner as target_owner, " +
                                "      s.table_name as target_name, " +
                                "      CASE " +
                                "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_PACKAGE' " +
                                "        ELSE o.object_type " +
                                "      END as target_type, " +
                                "      s.db_link " +
                                "    FROM user_synonyms s " +
                                "    LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                                "        AND s.table_name = o.object_name " +
                                "    WHERE (o.object_type = 'PACKAGE' OR s.db_link IS NOT NULL) " +
                                "    ORDER BY s.synonym_name " +
                                "  ) a " +
                                ") WHERE rnum > ? AND rnum <= ?";

                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(
                        synonymSql, synOffset, synOffset + synLimit);
                allItems.addAll(synonyms);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for packages: {}", e.getMessage());
        }
    }

    private List<Map<String, Object>> transformPackageItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("PACKAGE".equals(type)) {
                transformedItem.put("id", "package-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "PACKAGE");
                transformedItem.put("status", item.get("status"));
                transformedItem.put("specStatus", item.get("specStatus"));
                transformedItem.put("bodyStatus", item.get("bodyStatus"));
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("lastModified"));
                transformedItem.put("icon", "package");
                transformedItem.put("isSynonym", false);
            } else {
                transformedItem.put("id", "syn-pkg-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "SYNONYM");
                transformedItem.put("status", item.get("status") != null ? item.get("status") : "VALID");
                transformedItem.put("specStatus", null);
                transformedItem.put("bodyStatus", null);
                transformedItem.put("created", null);
                transformedItem.put("lastModified", null);
                transformedItem.put("targetOwner", item.get("target_owner"));
                transformedItem.put("targetName", item.get("target_name"));
                transformedItem.put("targetType", item.get("target_type") != null ? item.get("target_type") : "PACKAGE");
                transformedItem.put("dbLink", item.get("db_link"));
                transformedItem.put("isRemote", item.get("db_link") != null);
                transformedItem.put("isSynonym", true);
                transformedItem.put("icon", "synonym");
                transformedItem.put("targetIcon", "package");

                String targetType = (String) item.get("target_type");
                if (targetType != null) {
                    transformedItem.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                } else {
                    transformedItem.put("targetDisplayType", "Package");
                }
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }



    public List<Map<String, Object>> getAllPackages() {
        try {
            String sql = "SELECT " +
                    "    object_name as package_name, " +
                    "    object_type, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    status, " +
                    "    temporary, " +
                    "    generated, " +
                    "    secondary " +
                    "FROM user_objects " +
                    "WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') " +
                    "ORDER BY object_name, object_type";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllPackages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve packages: " + e.getMessage(), e);
        }
    }


    public Map<String, Object> getPackageDetails(String packageName) {
        try {
            Map<String, Object> pkgLocation = findObjectLocation(packageName, "PACKAGE");
            if (pkgLocation.isEmpty()) {
                pkgLocation = findObjectLocation(packageName, "PACKAGE BODY");
            }

            String owner = (String) pkgLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
            }

            return getPackageDetails(owner, packageName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Package {} not found", packageName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("package_name", packageName);
            emptyResult.put("message", "Package not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getPackageDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve package details: " + e.getMessage(), e);
        }
    }
}