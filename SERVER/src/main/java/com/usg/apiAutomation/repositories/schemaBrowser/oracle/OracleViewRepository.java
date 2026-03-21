package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class OracleViewRepository extends OracleRepository {

    // ============================================================
    // VIEW METHODS
    // ============================================================

    public List<Map<String, Object>> getAllViews() {
        try {
            String sql = "SELECT v.view_name, v.text_length, v.text, v.read_only, " +
                    "o.created, o.last_ddl_time, o.status, o.temporary, o.generated, o.secondary " +
                    "FROM user_views v JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                    "ORDER BY v.view_name";
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getAllViews: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getAllViewsForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String viewSql = "SELECT v.view_name as name, 'VIEW' as type, v.text_length, v.read_only, " +
                    "o.created, o.last_ddl_time, o.status, " +
                    "(SELECT COUNT(*) FROM user_tab_columns WHERE table_name = v.view_name) as column_count " +
                    "FROM user_views v JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                    "ORDER BY v.view_name";

            List<Map<String, Object>> views = getJdbcTemplate().queryForList(viewSql);

            for (Map<String, Object> view : views) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "view-" + System.currentTimeMillis() + "-" + view.get("name"));
                transformed.put("name", view.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "VIEW");
                transformed.put("status", view.get("status"));
                transformed.put("columnCount", view.get("column_count"));
                transformed.put("textLength", view.get("text_length"));
                transformed.put("readOnly", view.get("read_only"));
                transformed.put("created", view.get("created"));
                transformed.put("lastModified", view.get("last_ddl_time"));
                transformed.put("icon", "view");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            result.addAll(getViewSynonyms());
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (views + synonyms)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllViewsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    public Map<String, Object> getAllViewsForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            String viewCountSql = "SELECT COUNT(*) FROM user_views";
            int viewCount = getJdbcTemplate().queryForObject(viewCountSql, Integer.class);
            int synonymCount = countViewSynonyms();
            int totalCount = viewCount + synonymCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (viewCount > 0 && offset < viewCount) {
                int viewOffset = offset;
                int viewLimit = Math.min(pageSize, viewCount - viewOffset);

                if (viewLimit > 0) {
                    String viewSql = "SELECT v.view_name as name, 'VIEW' as type, v.text_length, v.read_only, " +
                            "o.created, o.last_ddl_time, o.status, " +
                            "(SELECT COUNT(*) FROM user_tab_columns WHERE table_name = v.view_name) as column_count " +
                            "FROM user_views v JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                            "ORDER BY v.view_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    List<Map<String, Object>> views = getJdbcTemplate().queryForList(viewSql, viewOffset, viewLimit);
                    allItems.addAll(views);
                }
            }

            if (allItems.size() < pageSize && synonymCount > 0) {
                addViewSynonymsPaginated(allItems, page, pageSize, viewCount);
            }

            List<Map<String, Object>> transformed = transformViewItems(allItems);
            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getAllViewsForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    public Map<String, Object> getViewDetails(String owner, String viewName) {
        Map<String, Object> details = new HashMap<>();
        try {
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                String sql = "SELECT v.view_name, v.text_length, v.text, v.read_only, " +
                        "o.created, o.last_ddl_time, o.status, " +
                        "(SELECT COUNT(*) FROM user_tab_columns WHERE table_name = v.view_name) as column_count " +
                        "FROM user_views v JOIN user_objects o ON v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                        "WHERE UPPER(v.view_name) = UPPER(?)";

                Map<String, Object> viewInfo = getJdbcTemplate().queryForMap(sql, viewName);
                details.putAll(viewInfo);
                details.put("columns", getViewColumns(owner, viewName));
            } else {
                String sql = "SELECT v.owner, v.view_name, v.text_length, v.text, v.read_only, " +
                        "o.created, o.last_ddl_time, o.status, " +
                        "(SELECT COUNT(*) FROM all_tab_columns WHERE owner = v.owner AND table_name = v.view_name) as column_count " +
                        "FROM all_views v JOIN all_objects o ON v.owner = o.owner AND v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                        "WHERE UPPER(v.owner) = UPPER(?) AND UPPER(v.view_name) = UPPER(?)";

                Map<String, Object> viewInfo = getJdbcTemplate().queryForMap(sql, owner, viewName);
                details.putAll(viewInfo);
                details.put("columns", getViewColumns(owner, viewName));
            }
        } catch (Exception e) {
            log.warn("Error getting view details for {}.{}: {}", owner, viewName, e.getMessage());
            details.put("error", e.getMessage());
        }
        return details;
    }

    public List<Map<String, Object>> getViewColumns(String owner, String viewName) {
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT column_id, column_name, data_type, data_length, data_precision, data_scale, nullable " +
                        "FROM user_tab_columns WHERE UPPER(table_name) = UPPER(?) ORDER BY column_id";
                return getJdbcTemplate().queryForList(sql, viewName);
            } else {
                sql = "SELECT column_id, column_name, data_type, data_length, data_precision, data_scale, nullable " +
                        "FROM all_tab_columns WHERE UPPER(owner) = UPPER(?) AND UPPER(table_name) = UPPER(?) ORDER BY column_id";
                return getJdbcTemplate().queryForList(sql, owner, viewName);
            }
        } catch (Exception e) {
            log.warn("Error getting view columns for {}.{}: {}", owner, viewName, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> getViewSynonyms() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String synonymSql = "SELECT s.synonym_name as name, 'SYNONYM' as type, " +
                    "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE o.status END as status, " +
                    "NULL as text_length, NULL as read_only, NULL as created, NULL as last_ddl_time, " +
                    "(SELECT COUNT(*) FROM all_tab_columns WHERE owner = s.table_owner AND table_name = s.table_name) as column_count, " +
                    "s.table_owner as target_owner, s.table_name as target_name, " +
                    "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE_VIEW' ELSE o.object_type END as target_type, s.db_link " +
                    "FROM user_synonyms s LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE (o.object_type = 'VIEW' OR s.db_link IS NOT NULL) ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);

            for (Map<String, Object> syn : synonyms) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "syn-view-" + System.currentTimeMillis() + "-" + syn.get("name"));
                transformed.put("name", syn.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                transformed.put("columnCount", syn.get("column_count") != null ? syn.get("column_count") : 0);
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "VIEW");
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);
                transformed.put("isSynonym", true);
                transformed.put("icon", "synonym");
                transformed.put("targetIcon", "view");
                result.add(transformed);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for views: {}", e.getMessage());
        }
        return result;
    }

    private int countViewSynonyms() {
        try {
            String synonymCountSql = "SELECT COUNT(*) FROM user_synonyms s WHERE " +
                    "EXISTS (SELECT 1 FROM all_objects WHERE owner = s.table_owner AND object_name = s.table_name AND object_type = 'VIEW') " +
                    "OR (s.db_link IS NOT NULL)";
            return getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
        } catch (Exception e) {
            log.warn("Error counting synonyms for views: {}", e.getMessage());
            return 0;
        }
    }

    private void addViewSynonymsPaginated(List<Map<String, Object>> allItems, int page, int pageSize, int viewCount) {
        try {
            int offset = (page - 1) * pageSize;
            int synOffset;
            int synLimit;

            if (offset >= viewCount) {
                synOffset = offset - viewCount;
                synLimit = pageSize;
            } else {
                synOffset = 0;
                synLimit = pageSize - allItems.size();
            }

            if (synLimit > 0) {
                String synonymSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT s.synonym_name as name, " +
                        "'SYNONYM' as type, NULL as text_length, NULL as read_only, NULL as created, NULL as last_ddl_time, " +
                        "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE o.status END as status, " +
                        "(SELECT COUNT(*) FROM all_tab_columns WHERE owner = s.table_owner AND table_name = s.table_name) as column_count, " +
                        "s.table_owner as target_owner, s.table_name as target_name, " +
                        "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE_VIEW' ELSE o.object_type END as target_type, s.db_link " +
                        "FROM user_synonyms s LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                        "WHERE (o.object_type = 'VIEW' OR s.db_link IS NOT NULL) ORDER BY s.synonym_name) a " +
                        ") WHERE rnum > ? AND rnum <= ?";

                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql, synOffset, synOffset + synLimit);
                allItems.addAll(synonyms);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for views: {}", e.getMessage());
        }
    }

    private List<Map<String, Object>> transformViewItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("VIEW".equals(type)) {
                transformedItem.put("id", "view-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "VIEW");
                transformedItem.put("status", item.get("status"));
                transformedItem.put("columnCount", item.get("column_count"));
                transformedItem.put("textLength", item.get("text_length"));
                transformedItem.put("readOnly", item.get("read_only"));
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("last_ddl_time"));
                transformedItem.put("icon", "view");
                transformedItem.put("isSynonym", false);
            } else {
                transformedItem.put("id", "syn-view-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "SYNONYM");
                transformedItem.put("status", item.get("status") != null ? item.get("status") : "VALID");
                transformedItem.put("columnCount", item.get("column_count") != null ? item.get("column_count") : 0);
                transformedItem.put("targetOwner", item.get("target_owner"));
                transformedItem.put("targetName", item.get("target_name"));
                transformedItem.put("targetType", item.get("target_type") != null ? item.get("target_type") : "VIEW");
                transformedItem.put("dbLink", item.get("db_link"));
                transformedItem.put("isRemote", item.get("db_link") != null);
                transformedItem.put("isSynonym", true);
                transformedItem.put("icon", "synonym");
                transformedItem.put("targetIcon", "view");
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }



    public Map<String, Object> getViewDetails(String viewName) {
        try {
            Map<String, Object> viewLocation = findObjectLocation(viewName, "VIEW");
            String owner = (String) viewLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
            }
            return getViewDetails(owner, viewName);
        } catch (EmptyResultDataAccessException e) {
            log.warn("View {} not found", viewName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("view_name", viewName);
            emptyResult.put("message", "View not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getViewDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve view details: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getViewsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    v.owner, " +
                    "    v.view_name, " +
                    "    v.text_length, " +
                    "    v.text, " +
                    "    v.read_only, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_views v " +
                    "JOIN all_objects o ON v.owner = o.owner AND v.view_name = o.object_name AND o.object_type = 'VIEW' " +
                    "WHERE UPPER(v.owner) = UPPER(?) " +
                    "ORDER BY v.view_name";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getViewsBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getViewsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_views";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT view_name, text_length, read_only FROM user_views ORDER BY view_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            List<Map<String, Object>> views = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", views);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getViewsPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }
}