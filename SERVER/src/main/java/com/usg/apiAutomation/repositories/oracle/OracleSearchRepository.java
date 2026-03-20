package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OracleSearchRepository extends OracleRepository {

    // ============================================================
    // SEARCH METHODS
    // ============================================================

    public Map<String, Object> searchObjectsForFrontend(String searchPattern, String type,
                                                        int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            String searchParam = "%" + searchPattern.toUpperCase() + "%";
            int offset = (page - 1) * pageSize;

            int objectCount = 0;
            int synonymCount = 0;

            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                String objectCountSql = "SELECT COUNT(*) FROM all_objects WHERE UPPER(object_name) LIKE ? AND object_type = ?";
                objectCount = getJdbcTemplate().queryForObject(objectCountSql, Integer.class, searchParam, type.toUpperCase());

                String synonymCountSql = "SELECT COUNT(*) FROM user_synonyms s WHERE " +
                        "EXISTS (SELECT 1 FROM all_objects WHERE owner = s.table_owner AND object_name = s.table_name AND object_type = ?) " +
                        "AND UPPER(s.synonym_name) LIKE ?";
                try {
                    synonymCount = getJdbcTemplate().queryForObject(synonymCountSql, Integer.class, type.toUpperCase(), searchParam);
                } catch (Exception e) {
                    log.warn("Error counting synonyms in search: {}", e.getMessage());
                }
            } else {
                String objectCountSql = "SELECT COUNT(*) FROM all_objects WHERE UPPER(object_name) LIKE ?";
                objectCount = getJdbcTemplate().queryForObject(objectCountSql, Integer.class, searchParam);

                String synonymCountSql = "SELECT COUNT(*) FROM user_synonyms WHERE UPPER(synonym_name) LIKE ?";
                try {
                    synonymCount = getJdbcTemplate().queryForObject(synonymCountSql, Integer.class, searchParam);
                } catch (Exception e) {
                    log.warn("Error counting synonyms in search: {}", e.getMessage());
                }
            }

            int totalCount = objectCount + synonymCount;
            List<Map<String, Object>> allItems = new ArrayList<>();

            // Get objects first
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

            // Get synonyms if needed
            if (allItems.size() < pageSize && synonymCount > 0) {
                int synOffset;
                int synLimit;

                if (offset >= objectCount) {
                    synOffset = offset - objectCount;
                    synLimit = pageSize;
                } else {
                    synOffset = 0;
                    synLimit = pageSize - allItems.size();
                }

                if (synLimit > 0) {
                    List<Map<String, Object>> synonyms = getSearchSynonyms(searchParam, type, synOffset, synLimit);
                    for (Map<String, Object> syn : synonyms) {
                        syn.put("isSynonym", true);
                        syn.put("icon", "synonym");
                        syn.put("targetIcon", getObjectTypeIcon((String) syn.get("target_type")));
                    }
                    allItems.addAll(synonyms);
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
            String searchParam = "%" + searchPattern.toUpperCase() + "%";
            int offset = (page - 1) * pageSize;

            String countSql;
            String dataSql;

            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                countSql = "SELECT COUNT(*) FROM all_objects WHERE UPPER(object_name) LIKE ? AND object_type = ?";
                int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class, searchParam, type.toUpperCase());

                dataSql = "SELECT owner, object_name, object_type, status, created, last_ddl_time, " +
                        "temporary, generated, secondary FROM all_objects " +
                        "WHERE UPPER(object_name) LIKE ? AND object_type = ? " +
                        "ORDER BY object_type, object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(
                        dataSql, searchParam, type.toUpperCase(), offset, pageSize);

                result.put("results", objects);
                result.put("totalCount", totalCount);

            } else {
                String fullCountSql = "SELECT COUNT(*) FROM (SELECT object_name FROM all_objects WHERE UPPER(object_name) LIKE ? " +
                        "UNION SELECT synonym_name FROM all_synonyms WHERE UPPER(synonym_name) LIKE ?)";
                int totalCount = getJdbcTemplate().queryForObject(fullCountSql, Integer.class, searchParam, searchParam);

                // Get all results and paginate in memory
                List<Map<String, Object>> allResults = new ArrayList<>();
                String objectsSql = "SELECT owner, object_name, object_type, status, created, last_ddl_time " +
                        "FROM all_objects WHERE UPPER(object_name) LIKE ? ORDER BY object_type, object_name";
                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(objectsSql, searchParam);

                for (Map<String, Object> obj : objects) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("name", obj.get("object_name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("object_type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
                    transformed.put("isSynonym", false);
                    allResults.add(transformed);
                }

                try {
                    String synonymSql = "SELECT s.owner, s.synonym_name as name, 'SYNONYM' as type, " +
                            "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE o.status END as status, " +
                            "s.table_owner as target_owner, s.table_name as target_name, o.object_type as target_type, s.db_link " +
                            "FROM all_synonyms s LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                            "WHERE UPPER(s.synonym_name) LIKE ? ORDER BY s.synonym_name";

                    List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql, searchParam);
                    for (Map<String, Object> syn : synonyms) {
                        syn.put("icon", "synonym");
                        syn.put("targetIcon", getObjectTypeIcon((String) syn.get("target_type")));
                        syn.put("isSynonym", true);
                        allResults.add(syn);
                    }
                } catch (Exception e) {
                    log.error("Error fetching synonyms: {}", e.getMessage());
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
            String searchParam = "%" + searchPattern.toUpperCase() + "%";
            List<Map<String, Object>> results = new ArrayList<>();

            if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                String sql = "SELECT owner, object_name, object_type, created, last_ddl_time, status " +
                        "FROM all_objects WHERE UPPER(object_name) LIKE ? AND object_type = ? AND ROWNUM <= ? " +
                        "ORDER BY object_type, object_name";

                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(
                        sql, searchParam, type.toUpperCase(), maxResults);

                for (Map<String, Object> obj : objects) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "search-" + System.currentTimeMillis() + "-" + obj.get("object_name"));
                    transformed.put("name", obj.get("object_name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("object_type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("created", obj.get("created"));
                    transformed.put("lastModified", obj.get("last_ddl_time"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
                    results.add(transformed);
                }
            } else {
                String sql = "SELECT owner, object_name, object_type, created, last_ddl_time, status " +
                        "FROM all_objects WHERE UPPER(object_name) LIKE ? AND ROWNUM <= ? " +
                        "ORDER BY object_type, object_name";

                List<Map<String, Object>> objects = getJdbcTemplate().queryForList(sql, searchParam, maxResults);

                for (Map<String, Object> obj : objects) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "search-" + System.currentTimeMillis() + "-" + obj.get("object_name"));
                    transformed.put("name", obj.get("object_name"));
                    transformed.put("owner", obj.get("owner"));
                    transformed.put("type", obj.get("object_type"));
                    transformed.put("status", obj.get("status"));
                    transformed.put("created", obj.get("created"));
                    transformed.put("lastModified", obj.get("last_ddl_time"));
                    transformed.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
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
            String searchParam = "%" + searchPattern.toUpperCase() + "%";

            // Search regular objects
            String objectSql = "SELECT owner, object_name, object_type, status, created, last_ddl_time " +
                    "FROM all_objects WHERE UPPER(object_name) LIKE ? ORDER BY object_type, object_name";
            List<Map<String, Object>> objects = getJdbcTemplate().queryForList(objectSql, searchParam);

            for (Map<String, Object> obj : objects) {
                obj.put("id", "obj-" + System.currentTimeMillis() + "-" + obj.get("object_name"));
                obj.put("searchType", "OBJECT");
                obj.put("icon", getObjectTypeIcon((String) obj.get("object_type")));
                obj.put("displayType", formatObjectTypeForDisplay((String) obj.get("object_type")));
            }
            results.addAll(objects);

            // Search synonyms
            String synonymSql = "SELECT s.owner, s.synonym_name as object_name, 'SYNONYM' as object_type, 'VALID' as status, " +
                    "NULL as created, NULL as last_ddl_time, s.table_owner as target_owner, s.table_name as target_name, " +
                    "s.db_link, o.object_type as target_type, o.status as target_status " +
                    "FROM all_synonyms s LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) LIKE ? OR UPPER(s.table_name) LIKE ? ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql, searchParam, searchParam);

            for (Map<String, Object> syn : synonyms) {
                syn.put("id", "syn-" + System.currentTimeMillis() + "-" + syn.get("object_name"));
                syn.put("searchType", "SYNONYM");
                syn.put("icon", "synonym");
                syn.put("displayType", "Synonym");
                syn.put("targetDisplay", syn.get("target_owner") + "." + syn.get("target_name") +
                        (syn.get("db_link") != null ? "@" + syn.get("db_link") : ""));
            }
            results.addAll(synonyms);

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
            String objectSql = "SELECT owner, object_name as name, object_type as type, status, created, last_ddl_time " +
                    "FROM all_objects WHERE UPPER(object_name) LIKE ? AND object_type = ? " +
                    "ORDER BY object_type, object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            return getJdbcTemplate().queryForList(objectSql, searchParam, type.toUpperCase(), offset, limit);
        } else {
            String objectSql = "SELECT owner, object_name as name, object_type as type, status, created, last_ddl_time " +
                    "FROM all_objects WHERE UPPER(object_name) LIKE ? " +
                    "ORDER BY object_type, object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            return getJdbcTemplate().queryForList(objectSql, searchParam, offset, limit);
        }
    }

    private List<Map<String, Object>> getSearchSynonyms(String searchParam, String type, int offset, int limit) {
        if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            String synonymSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT s.owner, s.synonym_name as name, " +
                    "'SYNONYM' as type, CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE o.status END as status, " +
                    "NULL as created, NULL as last_ddl_time, s.table_owner as target_owner, s.table_name as target_name, " +
                    "o.object_type as target_type, s.db_link FROM all_synonyms s LEFT JOIN all_objects o " +
                    "ON s.table_owner = o.owner AND s.table_name = o.object_name WHERE o.object_type = ? " +
                    "AND UPPER(s.synonym_name) LIKE ? ORDER BY s.synonym_name) a) WHERE rnum > ? AND rnum <= ?";
            return getJdbcTemplate().queryForList(synonymSql, type.toUpperCase(), searchParam, offset, offset + limit);
        } else {
            String synonymSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT s.owner, s.synonym_name as name, " +
                    "'SYNONYM' as type, CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE o.status END as status, " +
                    "NULL as created, NULL as last_ddl_time, s.table_owner as target_owner, s.table_name as target_name, " +
                    "o.object_type as target_type, s.db_link FROM all_synonyms s LEFT JOIN all_objects o " +
                    "ON s.table_owner = o.owner AND s.table_name = o.object_name WHERE UPPER(s.synonym_name) LIKE ? " +
                    "ORDER BY s.synonym_name) a) WHERE rnum > ? AND rnum <= ?";
            return getJdbcTemplate().queryForList(synonymSql, searchParam, offset, offset + limit);
        }
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

            if (Boolean.TRUE.equals(item.get("isSynonym"))) {
                transformed.put("targetOwner", item.get("target_owner"));
                transformed.put("targetName", item.get("target_name"));
                transformed.put("targetType", item.get("target_type"));
                transformed.put("dbLink", item.get("db_link"));
                transformed.put("isRemote", item.get("db_link") != null);
                transformed.put("targetIcon", item.get("targetIcon"));
                transformed.put("targetDisplayType", formatObjectTypeForDisplay((String) item.get("target_type")));
            }
            return transformed;
        }).collect(Collectors.toList());
    }


    public Map<String, Object> searchProceduresWithSynonyms(String query, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        int offset = (page - 1) * pageSize;

        String sql = "SELECT * FROM ( " +
                "  SELECT a.*, ROWNUM rnum FROM ( " +
                "    SELECT " +
                "      OBJECT_NAME as NAME, " +
                "      OWNER, " +
                "      OBJECT_TYPE as TYPE, " +
                "      STATUS, " +
                "      NULL as TARGET_OWNER, " +
                "      NULL as TARGET_NAME " +
                "    FROM ALL_OBJECTS " +
                "    WHERE OBJECT_TYPE = 'PROCEDURE' " +
                "      AND UPPER(OBJECT_NAME) LIKE UPPER(?) " +
                "    UNION ALL " +
                "    SELECT " +
                "      s.SYNONYM_NAME as NAME, " +
                "      s.OWNER, " +
                "      'SYNONYM' as TYPE, " +
                "      'VALID' as STATUS, " +
                "      s.TABLE_OWNER as TARGET_OWNER, " +
                "      s.TABLE_NAME as TARGET_NAME " +
                "    FROM ALL_SYNONYMS s " +
                "    WHERE EXISTS ( " +
                "      SELECT 1 FROM ALL_OBJECTS o " +
                "      WHERE o.OBJECT_NAME = s.TABLE_NAME " +
                "        AND o.OWNER = s.TABLE_OWNER " +
                "        AND o.OBJECT_TYPE = 'PROCEDURE' " +
                "    ) " +
                "    AND UPPER(s.SYNONYM_NAME) LIKE UPPER(?) " +
                "    ORDER BY NAME " +
                "  ) a WHERE ROWNUM <= ? " +
                ") WHERE rnum > ?";

        List<Map> items = getJdbcTemplate().query(
                sql,
                new Object[]{ "%" + query + "%", "%" + query + "%", offset + pageSize, offset },
                new BeanPropertyRowMapper<>(Map.class)
        );

        // Get total count (optimized - just counts)
        String countSql = "SELECT " +
                "  (SELECT COUNT(*) FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'PROCEDURE' AND UPPER(OBJECT_NAME) LIKE UPPER(?)) + " +
                "  (SELECT COUNT(*) FROM ALL_SYNONYMS s " +
                "   WHERE EXISTS (SELECT 1 FROM ALL_OBJECTS o " +
                "                WHERE o.OBJECT_NAME = s.TABLE_NAME " +
                "                  AND o.OWNER = s.TABLE_OWNER " +
                "                  AND o.OBJECT_TYPE = 'PROCEDURE') " +
                "   AND UPPER(s.SYNONYM_NAME) LIKE UPPER(?)) as TOTAL " +
                "FROM DUAL";

        int totalCount = getJdbcTemplate().queryForObject(
                countSql,
                new Object[]{ "%" + query + "%", "%" + query + "%" },
                Integer.class
        );

        result.put("items", items);
        result.put("totalCount", totalCount);
        result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }


    public int getSearchCount(String query, String[] types) {
        String typeCondition = "";
        if (types != null && types.length > 0) {
            typeCondition = "AND OBJECT_TYPE IN (";
            for (int i = 0; i < types.length; i++) {
                if (i > 0) typeCondition += ",";
                typeCondition += "'" + types[i] + "'";
            }
            typeCondition += ")";
        }

        String sql = "SELECT COUNT(*) FROM ALL_OBJECTS " +
                "WHERE (UPPER(OBJECT_NAME) LIKE UPPER(?) OR UPPER(?) IS NULL) " + typeCondition;

        return getJdbcTemplate().queryForObject(
                sql,
                new Object[]{ "%" + query + "%", query },
                Integer.class
        );
    }


    public Map<String, Object> searchCombinedTypes(String query, String[] types, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        int offset = (page - 1) * pageSize;

        // Build the type condition
        String typeCondition = "";
        if (types != null && types.length > 0) {
            typeCondition = "AND UPPER(OBJECT_TYPE) IN (";
            for (int i = 0; i < types.length; i++) {
                if (i > 0) typeCondition += ",";
                typeCondition += "UPPER('" + types[i] + "')";
            }
            typeCondition += ")";
        }

        // Main search query
        String sql = "SELECT * FROM ( " +
                "  SELECT a.*, ROWNUM rnum FROM ( " +
                "    SELECT " +
                "      OBJECT_NAME, " +
                "      OWNER, " +
                "      OBJECT_TYPE, " +
                "      STATUS, " +
                "      CREATED, " +
                "      LAST_DDL_TIME, " +
                "      CASE " +
                "        WHEN OBJECT_TYPE = 'SYNONYM' THEN " +
                "          (SELECT TABLE_OWNER FROM ALL_SYNONYMS WHERE SYNONYM_NAME = OBJECT_NAME AND OWNER = a.OWNER) " +
                "        ELSE NULL " +
                "      END as TABLE_OWNER, " +
                "      CASE " +
                "        WHEN OBJECT_TYPE = 'SYNONYM' THEN " +
                "          (SELECT TABLE_NAME FROM ALL_SYNONYMS WHERE SYNONYM_NAME = OBJECT_NAME AND OWNER = a.OWNER) " +
                "        ELSE NULL " +
                "      END as TABLE_NAME " +
                "    FROM ALL_OBJECTS a " +
                "    WHERE (UPPER(OBJECT_NAME) LIKE UPPER(?) OR UPPER(?) IS NULL) " +
                typeCondition +
                "    ORDER BY OBJECT_NAME " +
                "  ) a WHERE ROWNUM <= ? " +
                ") WHERE rnum > ?";

        List<Map> items = getJdbcTemplate().query(
                sql,
                new Object[]{ "%" + query + "%", query, offset + pageSize, offset },
                new BeanPropertyRowMapper<>(Map.class)
        );

        // Get total count
        String countSql = "SELECT COUNT(*) FROM ALL_OBJECTS " +
                "WHERE (UPPER(OBJECT_NAME) LIKE UPPER(?) OR UPPER(?) IS NULL) " + typeCondition;

        int totalCount = getJdbcTemplate().queryForObject(
                countSql,
                new Object[]{ "%" + query + "%", query },
                Integer.class
        );

        result.put("items", items);
        result.put("totalCount", totalCount);
        result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }
}