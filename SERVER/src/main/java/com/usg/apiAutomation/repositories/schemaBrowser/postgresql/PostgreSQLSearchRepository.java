package com.usg.apiAutomation.repositories.schemaBrowser.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostgreSQLSearchRepository extends PostgreSQLRepository {

    // ============================================================
    // SEARCH METHODS - POSTGRESQL VERSION
    // ============================================================

    public Map<String, Object> searchObjectsForFrontend(String searchPattern, String type,
                                                        int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            String searchParam = "%" + searchPattern.toLowerCase() + "%";
            int offset = (page - 1) * pageSize;

            // Search in tables, views, materialized views, functions, procedures
            int objectCount = 0;

            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                String objectCountSql = getSearchCountSqlForType(type);
                objectCount = getJdbcTemplate().queryForObject(objectCountSql, Integer.class, searchParam);
            } else {
                String objectCountSql = "SELECT COUNT(*) FROM (" +
                        "SELECT relname FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND c.relkind IN ('r', 'v', 'm', 'f', 'p') " +
                        "AND c.relname ILIKE ?) t";
                objectCount = getJdbcTemplate().queryForObject(objectCountSql, Integer.class, searchParam);
            }

            int totalCount = objectCount; // PostgreSQL doesn't have synonyms
            List<Map<String, Object>> allItems = new ArrayList<>();

            // Get objects
            if (objectCount > 0 && offset < objectCount) {
                int objOffset = offset;
                int objLimit = Math.min(pageSize, objectCount - objOffset);

                if (objLimit > 0) {
                    List<Map<String, Object>> objects = getSearchObjects(searchParam, type, objOffset, objLimit);
                    for (Map<String, Object> obj : objects) {
                        obj.put("isSynonym", false);
                        obj.put("icon", getObjectTypeIcon((String) obj.get("type")));
                    }
                    allItems.addAll(objects);
                }
            }

            List<Map<String, Object>> transformed = transformSearchResults(allItems);
            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);
            result.put("query", searchPattern);
            result.put("type", type);

        } catch (Exception e) {
            log.error("Error in searchObjectsForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> searchObjectsPaginated(String searchPattern, String type,
                                                      int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            String searchParam = "%" + searchPattern.toLowerCase() + "%";
            int offset = (page - 1) * pageSize;

            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                String countSql = getSearchCountSqlForType(type);
                int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class, searchParam);

                String dataSql = getSearchDataSqlForType(type);
                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(
                        dataSql, searchParam, offset, pageSize);

                result.put("results", objects);
                result.put("totalCount", totalCount);

            } else {
                // Search all object types
                String fullCountSql = "SELECT COUNT(*) FROM (" +
                        "SELECT c.relname FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
                        "AND c.relname ILIKE ?) t";
                int totalCount = getJdbcTemplate().queryForObject(fullCountSql, Integer.class, searchParam);

                // Get all results and paginate
                List<Map<String, Object>> allResults = new ArrayList<>();
                String objectsSql = "SELECT " +
                        "    n.nspname as owner, " +
                        "    c.relname as name, " +
                        "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                        "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                        "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                        "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                        "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                        "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                        "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                        "         ELSE 'OTHER' END as type, " +
                        "    'VALID' as status " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
                        "AND c.relname ILIKE ? " +
                        "ORDER BY type, name";

                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(objectsSql, searchParam);

                for (Map<String, Object> obj : objects) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("name", obj.get("name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("type")));
                    transformed.put("isSynonym", false);
                    allResults.add(transformed);
                }

                allResults.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));

                int fromIndex = Math.min(offset, allResults.size());
                int toIndex = Math.min(offset + pageSize, allResults.size());
                List<Map<String, Object>> paginatedResults = allResults.subList(fromIndex, toIndex);

                result.put("results", paginatedResults);
                result.put("totalCount", allResults.size());
            }

            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) getLongValue(result.get("totalCount")) / pageSize));
            result.put("query", searchPattern);
            result.put("type", type);

        } catch (Exception e) {
            log.error("Error in searchObjectsPaginated: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search objects: " + e.getMessage(), e);
        }
        return result;
    }

    public List<Map<String, Object>> searchObjectsForFrontend(String searchPattern, String type, int maxResults) {
        try {
            String searchParam = "%" + searchPattern.toLowerCase() + "%";
            List<Map<String, Object>> results = new ArrayList<>();

            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                String sql = getSearchDataSqlForType(type);
                // Add LIMIT for maxResults
                sql = sql.replace("OFFSET ? LIMIT ?", "LIMIT ?");
                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(
                        sql, searchParam, maxResults);

                for (Map<String, Object> obj : objects) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "search-" + System.currentTimeMillis() + "-" + obj.get("name"));
                    transformed.put("name", obj.get("name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("created", obj.get("created"));
                    transformed.put("lastModified", obj.get("lastModified"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("type")));
                    results.add(transformed);
                }
            } else {
                String sql = "SELECT " +
                        "    n.nspname as owner, " +
                        "    c.relname as name, " +
                        "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                        "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                        "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                        "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                        "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                        "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                        "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                        "         ELSE 'OTHER' END as type, " +
                        "    'VALID' as status, " +
                        "    NULL as created, " +
                        "    NULL as lastModified " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
                        "AND c.relname ILIKE ? " +
                        "ORDER BY type, name " +
                        "LIMIT ?";

                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(sql, searchParam, maxResults);

                for (Map<String, Object> obj : objects) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "search-" + System.currentTimeMillis() + "-" + obj.get("name"));
                    transformed.put("name", obj.get("name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("created", obj.get("created"));
                    transformed.put("lastModified", obj.get("lastModified"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("type")));
                    results.add(transformed);
                }
            }
            return results;
        } catch (Exception e) {
            log.error("Error in searchObjectsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> comprehensiveSearch(String searchPattern) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            String searchParam = "%" + searchPattern.toLowerCase() + "%";

            // Search all objects
            String objectSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "         ELSE 'OTHER' END as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
                    "AND c.relname ILIKE ? " +
                    "ORDER BY object_type, object_name";

            List<Map<String, Object>> objects = getJdbcTemplate().queryForList(objectSql, searchParam);

            for (Map<String, Object> obj : objects) {
                obj.put("id", "obj-" + System.currentTimeMillis() + "-" + obj.get("object_name"));
                obj.put("searchType", "OBJECT");
                obj.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
                obj.put("displayType", formatObjectTypeForDisplay((String) obj.get("object_type")));
            }
            results.addAll(objects);

            // PostgreSQL doesn't have synonyms

            results.sort((a, b) -> {
                String typeA = (String) a.get("object_type");
                String typeB = (String) b.get("object_type");
                int typeCompare = typeA.compareTo(typeB);
                if (typeCompare != 0) return typeCompare;
                String nameA = (String) a.get("object_name");
                String nameB = (String) b.get("object_name");
                return nameA.compareTo(nameB);
            });

            return results;
        } catch (Exception e) {
            log.error("Error in comprehensiveSearch: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> getSearchObjects(String searchParam, String type, int offset, int limit) {
        if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            String objectSql = getSearchDataSqlForType(type);
            return getJdbcTemplate().queryForList(objectSql, searchParam, offset, limit);
        } else {
            String objectSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as name, " +
                    "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                    "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                    "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                    "         WHEN c.relkind = 'f' THEN 'FUNCTION' " +
                    "         WHEN c.relkind = 'p' THEN 'PROCEDURE' " +
                    "         WHEN c.relkind = 'S' THEN 'SEQUENCE' " +
                    "         WHEN c.relkind = 'i' THEN 'INDEX' " +
                    "         ELSE 'OTHER' END as type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind IN ('r', 'v', 'm', 'f', 'p', 'S', 'i') " +
                    "AND c.relname ILIKE ? " +
                    "ORDER BY type, name " +
                    "OFFSET ? LIMIT ?";
            return getJdbcTemplate().queryForList(objectSql, searchParam, offset, limit);
        }
    }

    private String getSearchCountSqlForType(String type) {
        String upperType = type.toUpperCase();
        String relKind = getRelationKind(upperType);

        if (relKind != null) {
            return "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = ? " +
                    "AND c.relname ILIKE ?";
        } else if ("FUNCTION".equals(upperType)) {
            return "SELECT COUNT(*) FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'f' " +
                    "AND p.proname ILIKE ?";
        } else if ("PROCEDURE".equals(upperType)) {
            return "SELECT COUNT(*) FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'p' " +
                    "AND p.proname ILIKE ?";
        }
        return "SELECT COUNT(*) FROM pg_class c " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "WHERE n.nspname = current_schema() " +
                "AND c.relname ILIKE ?";
    }

    private String getSearchDataSqlForType(String type) {
        String upperType = type.toUpperCase();
        String relKind = getRelationKind(upperType);

        if (relKind != null) {
            return "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as name, " +
                    "    '" + upperType + "' as type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = ? " +
                    "AND c.relname ILIKE ? " +
                    "ORDER BY c.relname " +
                    "OFFSET ? LIMIT ?";
        } else if ("FUNCTION".equals(upperType)) {
            return "SELECT " +
                    "    n.nspname as owner, " +
                    "    p.proname as name, " +
                    "    'FUNCTION' as type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'f' " +
                    "AND p.proname ILIKE ? " +
                    "ORDER BY p.proname " +
                    "OFFSET ? LIMIT ?";
        } else if ("PROCEDURE".equals(upperType)) {
            return "SELECT " +
                    "    n.nspname as owner, " +
                    "    p.proname as name, " +
                    "    'PROCEDURE' as type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'p' " +
                    "AND p.proname ILIKE ? " +
                    "ORDER BY p.proname " +
                    "OFFSET ? LIMIT ?";
        }
        return "SELECT " +
                "    n.nspname as owner, " +
                "    c.relname as name, " +
                "    'OTHER' as type, " +
                "    'VALID' as status, " +
                "    NULL as created, " +
                "    NULL as last_ddl_time " +
                "FROM pg_class c " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "WHERE n.nspname = current_schema() " +
                "AND c.relname ILIKE ? " +
                "ORDER BY c.relname " +
                "OFFSET ? LIMIT ?";
    }

    private List<Map<String, Object>> transformSearchResults(List<Map<String, Object>> items) {
        return items.stream().map(item -> {
            Map<String, Object> transformed = new HashMap<>();
            transformed.put("id", "search-" + System.currentTimeMillis() + "-" + item.get("name"));
            transformed.put("name", item.get("name"));
            transformed.put("owner", item.get("owner"));
            transformed.put("type", item.get("type"));
            transformed.put("status", item.get("status"));
            transformed.put("created", item.get("created"));
            transformed.put("lastModified", item.get("last_ddl_time"));
            transformed.put("icon", item.get("icon"));
            transformed.put("isSynonym", item.get("isSynonym"));

            // PostgreSQL doesn't have synonyms, so no target info
            return transformed;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> searchProceduresWithSynonyms(String query, int page, int pageSize) {
        // PostgreSQL doesn't have synonyms, so just search procedures
        Map<String, Object> result = new HashMap<>();

        int offset = (page - 1) * pageSize;
        String searchParam = "%" + query.toLowerCase() + "%";

        String sql = "SELECT " +
                "    p.proname as NAME, " +
                "    n.nspname as OWNER, " +
                "    'PROCEDURE' as TYPE, " +
                "    'VALID' as STATUS, " +
                "    NULL as TARGET_OWNER, " +
                "    NULL as TARGET_NAME " +
                "FROM pg_proc p " +
                "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                "WHERE n.nspname = current_schema() " +
                "AND p.prokind = 'p' " +
                "AND p.proname ILIKE ? " +
                "ORDER BY p.proname " +
                "OFFSET ? LIMIT ?";

        List<Map<String, Object>> items = getJdbcTemplate().queryForList(sql, searchParam, offset, pageSize);

        // Get total count
        String countSql = "SELECT COUNT(*) FROM pg_proc p " +
                "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                "WHERE n.nspname = current_schema() " +
                "AND p.prokind = 'p' " +
                "AND p.proname ILIKE ?";

        int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class, searchParam);

        result.put("items", items);
        result.put("totalCount", totalCount);
        result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

    public int getSearchCount(String query, String[] types) {
        try {
            String searchParam = "%" + query.toLowerCase() + "%";

            if (types != null && types.length > 0) {
                StringBuilder typeCondition = new StringBuilder();
                typeCondition.append("AND (");
                for (int i = 0; i < types.length; i++) {
                    if (i > 0) typeCondition.append(" OR ");
                    String upperType = types[i].toUpperCase();
                    String relKind = getRelationKind(upperType);
                    if (relKind != null) {
                        typeCondition.append("c.relkind = '").append(relKind).append("'");
                    } else if ("FUNCTION".equals(upperType)) {
                        typeCondition.append("p.prokind = 'f'");
                    } else if ("PROCEDURE".equals(upperType)) {
                        typeCondition.append("p.prokind = 'p'");
                    }
                }
                typeCondition.append(")");

                String sql = "SELECT COUNT(*) FROM (" +
                        "SELECT c.relname FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND c.relname ILIKE ? " +
                        typeCondition.toString() +
                        " UNION " +
                        "SELECT p.proname FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND p.proname ILIKE ? " +
                        "AND (p.prokind = 'f' OR p.prokind = 'p')" +
                        ") t";

                return getJdbcTemplate().queryForObject(sql, Integer.class, searchParam, searchParam);
            } else {
                String sql = "SELECT COUNT(*) FROM (" +
                        "SELECT c.relname FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND c.relname ILIKE ? " +
                        "UNION " +
                        "SELECT p.proname FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = current_schema() " +
                        "AND p.proname ILIKE ?" +
                        ") t";

                return getJdbcTemplate().queryForObject(sql, Integer.class, searchParam, searchParam);
            }
        } catch (Exception e) {
            log.error("Error in getSearchCount: {}", e.getMessage(), e);
            return 0;
        }
    }

    public Map<String, Object> searchCombinedTypes(String query, String[] types, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        int offset = (page - 1) * pageSize;
        String searchParam = "%" + query.toLowerCase() + "%";

        List<Map<String, Object>> allResults = new ArrayList<>();

        // Build type condition for tables, views, etc.
        boolean includeTables = false;
        boolean includeViews = false;
        boolean includeMatViews = false;
        boolean includeFunctions = false;
        boolean includeProcedures = false;
        boolean includeSequences = false;

        if (types != null && types.length > 0) {
            for (String type : types) {
                String upperType = type.toUpperCase();
                switch (upperType) {
                    case "TABLE":
                        includeTables = true;
                        break;
                    case "VIEW":
                        includeViews = true;
                        break;
                    case "MATERIALIZED VIEW":
                        includeMatViews = true;
                        break;
                    case "FUNCTION":
                        includeFunctions = true;
                        break;
                    case "PROCEDURE":
                        includeProcedures = true;
                        break;
                    case "SEQUENCE":
                        includeSequences = true;
                        break;
                }
            }
        } else {
            includeTables = includeViews = includeMatViews = includeFunctions = includeProcedures = includeSequences = true;
        }

        // Search tables
        if (includeTables) {
            String tableSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    'TABLE' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'r' " +
                    "AND c.relname ILIKE ?";
            List<Map<String, Object>> tables = getJdbcTemplate().queryForList(tableSql, searchParam);
            allResults.addAll(tables);
        }

        // Search views
        if (includeViews) {
            String viewSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    'VIEW' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'v' " +
                    "AND c.relname ILIKE ?";
            List<Map<String, Object>> views = getJdbcTemplate().queryForList(viewSql, searchParam);
            allResults.addAll(views);
        }

        // Search materialized views
        if (includeMatViews) {
            String matViewSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    'MATERIALIZED VIEW' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'm' " +
                    "AND c.relname ILIKE ?";
            List<Map<String, Object>> matViews = getJdbcTemplate().queryForList(matViewSql, searchParam);
            allResults.addAll(matViews);
        }

        // Search functions
        if (includeFunctions) {
            String functionSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    p.proname as object_name, " +
                    "    'FUNCTION' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'f' " +
                    "AND p.proname ILIKE ?";
            List<Map<String, Object>> functions = getJdbcTemplate().queryForList(functionSql, searchParam);
            allResults.addAll(functions);
        }

        // Search procedures
        if (includeProcedures) {
            String procedureSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    p.proname as object_name, " +
                    "    'PROCEDURE' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'p' " +
                    "AND p.proname ILIKE ?";
            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql, searchParam);
            allResults.addAll(procedures);
        }

        // Search sequences
        if (includeSequences) {
            String sequenceSql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as object_name, " +
                    "    'SEQUENCE' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'S' " +
                    "AND c.relname ILIKE ?";
            List<Map<String, Object>> sequences = getJdbcTemplate().queryForList(sequenceSql, searchParam);
            allResults.addAll(sequences);
        }

        // Sort results
        allResults.sort((a, b) -> {
            String nameA = (String) a.get("object_name");
            String nameB = (String) b.get("object_name");
            return nameA.compareTo(nameB);
        });

        int totalCount = allResults.size();
        int fromIndex = Math.min(offset, totalCount);
        int toIndex = Math.min(offset + pageSize, totalCount);
        List<Map<String, Object>> paginatedResults = allResults.subList(fromIndex, toIndex);

        result.put("items", paginatedResults);
        result.put("totalCount", totalCount);
        result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

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
}