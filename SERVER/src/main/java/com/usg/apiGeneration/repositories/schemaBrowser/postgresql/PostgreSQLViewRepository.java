package com.usg.apiGeneration.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class PostgreSQLViewRepository extends PostgreSQLRepository {

    // ============================================================
    // VIEW METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllViews() {
        try {
            String sql = "SELECT " +
                    "    c.relname as view_name, " +
                    "    pg_get_viewdef(c.oid) as text, " +
                    "    false as read_only, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'v' " +
                    "ORDER BY c.relname";
            return getJdbcTemplate().queryForList(sql);
        } catch (Exception e) {
            log.error("Error in getAllViews: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getAllViewsForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String viewSql = "SELECT " +
                    "    c.relname as name, " +
                    "    'VIEW' as type, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    false as read_only, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    (SELECT COUNT(*) FROM information_schema.columns " +
                    "     WHERE table_schema = current_schema() AND table_name = c.relname) as column_count " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'v' " +
                    "ORDER BY c.relname";

            List<Map<String, Object>> views = getJdbcTemplate().queryForList(viewSql);

            for (Map<String, Object> view : views) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "view-" + System.currentTimeMillis() + "-" + view.get("name"));
                transformed.put("name", view.get("name"));
                transformed.put("owner", getCurrentSchema());
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

            // PostgreSQL doesn't have synonyms
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (views)", result.size());

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

            String viewCountSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'v'";
            int viewCount = getJdbcTemplate().queryForObject(viewCountSql, Integer.class);
            int totalCount = viewCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (viewCount > 0 && offset < viewCount) {
                int viewOffset = offset;
                int viewLimit = Math.min(pageSize, viewCount - viewOffset);

                if (viewLimit > 0) {
                    String viewSql = "SELECT " +
                            "    c.relname as name, " +
                            "    'VIEW' as type, " +
                            "    length(pg_get_viewdef(c.oid)) as text_length, " +
                            "    false as read_only, " +
                            "    NULL as created, " +
                            "    NULL as last_ddl_time, " +
                            "    'VALID' as status, " +
                            "    (SELECT COUNT(*) FROM information_schema.columns " +
                            "     WHERE table_schema = current_schema() AND table_name = c.relname) as column_count " +
                            "FROM pg_class c " +
                            "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                            "WHERE n.nspname = current_schema() AND c.relkind = 'v' " +
                            "ORDER BY c.relname " +
                            "OFFSET ? LIMIT ?";

                    List<Map<String, Object>> views = getJdbcTemplate().queryForList(viewSql, viewOffset, viewLimit);
                    allItems.addAll(views);
                }
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
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            String sql = "SELECT " +
                    "    c.relname as view_name, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    pg_get_viewdef(c.oid) as text, " +
                    "    false as read_only, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    (SELECT COUNT(*) FROM information_schema.columns " +
                    "     WHERE table_schema = ? AND table_name = c.relname) as column_count " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'v'";

            Map<String, Object> viewInfo = getJdbcTemplate().queryForMap(sql, owner, owner, viewName);
            details.putAll(viewInfo);
            details.put("columns", getViewColumns(owner, viewName));

        } catch (Exception e) {
            log.warn("Error getting view details for {}.{}: {}", owner, viewName, e.getMessage());
            details.put("error", e.getMessage());
        }
        return details;
    }

    public List<Map<String, Object>> getViewColumns(String owner, String viewName) {
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            String sql = "SELECT " +
                    "    ordinal_position as column_id, " +
                    "    column_name, " +
                    "    data_type, " +
                    "    character_maximum_length as data_length, " +
                    "    numeric_precision as data_precision, " +
                    "    numeric_scale as data_scale, " +
                    "    is_nullable as nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ? " +
                    "ORDER BY ordinal_position";

            return getJdbcTemplate().queryForList(sql, owner, viewName);
        } catch (Exception e) {
            log.warn("Error getting view columns for {}.{}: {}", owner, viewName, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ============================================================
    // MATERIALIZED VIEW METHODS
    // ============================================================

    public List<Map<String, Object>> getAllMaterializedViewsForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String mvSql = "SELECT " +
                    "    c.relname as name, " +
                    "    'MATERIALIZED VIEW' as type, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    false as read_only, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    (SELECT COUNT(*) FROM information_schema.columns " +
                    "     WHERE table_schema = current_schema() AND table_name = c.relname) as column_count " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'm' " +
                    "ORDER BY c.relname";

            List<Map<String, Object>> views = getJdbcTemplate().queryForList(mvSql);

            for (Map<String, Object> view : views) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "materialized-view-" + System.currentTimeMillis() + "-" + view.get("name"));
                transformed.put("name", view.get("name"));
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "MATERIALIZED VIEW");
                transformed.put("status", view.get("status"));
                transformed.put("columnCount", view.get("column_count"));
                transformed.put("textLength", view.get("text_length"));
                transformed.put("readOnly", view.get("read_only"));
                transformed.put("created", view.get("created"));
                transformed.put("lastModified", view.get("last_ddl_time"));
                transformed.put("icon", "materialized-view");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (materialized views)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllMaterializedViewsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }


    public Map<String, Object> getAllMaterializedViewsForFrontend(int page, int pageSize) {
        Map<String, Object> paginatedResult = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // First get total count
            String countSql = "SELECT COUNT(*) as total FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'm'";

            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            // Calculate offset
            int offset = (page - 1) * pageSize;

            // Get paginated materialized views
            String mvSql = "SELECT " +
                    "    c.relname as name, " +
                    "    'MATERIALIZED VIEW' as type, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    false as read_only, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    (SELECT COUNT(*) FROM information_schema.columns " +
                    "     WHERE table_schema = current_schema() AND table_name = c.relname) as column_count " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'm' " +
                    "ORDER BY c.relname " +
                    "LIMIT ? OFFSET ?";

            List<Map<String, Object>> views = getJdbcTemplate().queryForList(mvSql, pageSize, offset);

            for (Map<String, Object> view : views) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "materialized-view-" + System.currentTimeMillis() + "-" + view.get("name"));
                transformed.put("name", view.get("name"));
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "MATERIALIZED VIEW");
                transformed.put("status", view.get("status"));
                transformed.put("columnCount", view.get("column_count"));
                transformed.put("textLength", view.get("text_length"));
                transformed.put("readOnly", view.get("read_only"));
                transformed.put("created", view.get("created"));
                transformed.put("lastModified", view.get("last_ddl_time"));
                transformed.put("icon", "materialized-view");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            // Calculate total pages
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            paginatedResult.put("items", result);
            paginatedResult.put("totalCount", totalCount);
            paginatedResult.put("page", page);
            paginatedResult.put("pageSize", pageSize);
            paginatedResult.put("totalPages", totalPages);
            paginatedResult.put("hasNext", page < totalPages);
            paginatedResult.put("hasPrev", page > 1);

            log.info("Returning {} of {} total materialized views (page: {}/{})",
                    result.size(), totalCount, page, totalPages);

        } catch (Exception e) {
            log.error("Error in getAllMaterializedViewsForFrontend paginated: {}", e.getMessage(), e);
            paginatedResult.put("items", new ArrayList<>());
            paginatedResult.put("totalCount", 0);
            paginatedResult.put("page", page);
            paginatedResult.put("pageSize", pageSize);
            paginatedResult.put("totalPages", 0);
            paginatedResult.put("hasNext", false);
            paginatedResult.put("hasPrev", false);
        }

        return paginatedResult;
    }


    public Map<String, Object> getMaterializedViewDetails(String owner, String mvName) {
        Map<String, Object> details = new HashMap<>();
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            String sql = "SELECT " +
                    "    c.relname as mview_name, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    pg_get_viewdef(c.oid) as query, " +
                    "    false as read_only, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_refresh_date, " +
                    "    (SELECT COUNT(*) FROM information_schema.columns " +
                    "     WHERE table_schema = ? AND table_name = c.relname) as column_count " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'm'";

            Map<String, Object> mvInfo = getJdbcTemplate().queryForMap(sql, owner, owner, mvName);
            details.putAll(mvInfo);
            details.put("columns", getViewColumns(owner, mvName));

        } catch (Exception e) {
            log.warn("Error getting materialized view details for {}.{}: {}", owner, mvName, e.getMessage());
            details.put("error", e.getMessage());
        }
        return details;
    }

    public Map<String, Object> getMaterializedViewDetails(String mvName) {
        try {
            Map<String, Object> mvLocation = findObjectLocation(mvName, "MATERIALIZED VIEW");
            String owner = (String) mvLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getMaterializedViewDetails(owner, mvName);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Materialized view {} not found", mvName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("mview_name", mvName);
            emptyResult.put("message", "Materialized view not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getMaterializedViewDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve materialized view details: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getMaterializedViewsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as mview_name, " +
                    "    pg_get_viewdef(c.oid) as query, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_refresh_date, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relkind = 'm' " +
                    "ORDER BY c.relname";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getMaterializedViewsBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> transformViewItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("VIEW".equals(type)) {
                transformedItem.put("id", "view-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentSchema());
                transformedItem.put("type", "VIEW");
                transformedItem.put("status", item.get("status"));
                transformedItem.put("columnCount", item.get("column_count"));
                transformedItem.put("textLength", item.get("text_length"));
                transformedItem.put("readOnly", item.get("read_only"));
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("last_ddl_time"));
                transformedItem.put("icon", "view");
                transformedItem.put("isSynonym", false);
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
                owner = getCurrentSchema();
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
                    "    n.nspname as owner, " +
                    "    c.relname as view_name, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    pg_get_viewdef(c.oid) as text, " +
                    "    false as read_only, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relkind = 'v' " +
                    "ORDER BY c.relname";
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

            String countSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'v'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    c.relname as view_name, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    false as read_only " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'v' " +
                    "ORDER BY c.relname " +
                    "OFFSET ? LIMIT ?";

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

    public Map<String, Object> getMaterializedViewsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'm'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    c.relname as mview_name, " +
                    "    length(pg_get_viewdef(c.oid)) as text_length, " +
                    "    false as read_only, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_refresh_date " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'm' " +
                    "ORDER BY c.relname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> views = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", views);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getMaterializedViewsPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }
}