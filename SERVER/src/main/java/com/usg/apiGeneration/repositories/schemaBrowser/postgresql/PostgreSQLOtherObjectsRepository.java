package com.usg.apiGeneration.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostgreSQLOtherObjectsRepository extends PostgreSQLRepository {

    // ============================================================
    // TRIGGER METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllTriggersForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String triggerSql = "SELECT " +
                    "    t.tgname as name, " +
                    "    'TRIGGER' as type, " +
                    "    CASE WHEN t.tgenabled = 'D' THEN 'DISABLED' ELSE 'ENABLED' END as trigger_status, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 2 = 2 THEN 'BEFORE' " +
                    "        WHEN t.tgtype & 64 = 64 THEN 'INSTEAD OF' " +
                    "        ELSE 'AFTER' " +
                    "    END as trigger_type, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 4 = 4 THEN 'INSERT' " +
                    "        WHEN t.tgtype & 8 = 8 THEN 'DELETE' " +
                    "        WHEN t.tgtype & 16 = 16 THEN 'UPDATE' " +
                    "        WHEN t.tgtype & 32 = 32 THEN 'TRUNCATE' " +
                    "        ELSE 'UNKNOWN' " +
                    "    END as triggering_event, " +
                    "    c.relname as table_name, " +
                    "    pg_get_triggerdef(t.oid) as description, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as object_status " +
                    "FROM pg_trigger t " +
                    "JOIN pg_class c ON t.tgrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND NOT t.tgisinternal " +
                    "ORDER BY t.tgname";

            List<Map<String, Object>> triggers = getJdbcTemplate().queryForList(triggerSql);

            for (Map<String, Object> trigger : triggers) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "trigger-" + System.currentTimeMillis() + "-" + trigger.get("name"));
                transformed.put("name", trigger.get("name"));
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "TRIGGER");
                transformed.put("status", trigger.get("trigger_status"));
                transformed.put("objectStatus", trigger.get("object_status"));
                transformed.put("triggerType", trigger.get("trigger_type"));
                transformed.put("triggeringEvent", trigger.get("triggering_event"));
                transformed.put("tableName", trigger.get("table_name"));
                transformed.put("description", trigger.get("description"));
                transformed.put("created", trigger.get("created"));
                transformed.put("lastModified", trigger.get("last_ddl_time"));
                transformed.put("icon", "trigger");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            // PostgreSQL doesn't have synonyms, so no synonym handling
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (triggers)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllTriggersForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    public Map<String, Object> getAllTriggersForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_trigger t " +
                    "JOIN pg_class c ON t.tgrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND NOT t.tgisinternal";
            int triggerCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            int totalCount = triggerCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (triggerCount > 0 && offset < triggerCount) {
                int trigOffset = offset;
                int trigLimit = Math.min(pageSize, triggerCount - trigOffset);

                if (trigLimit > 0) {
                    String triggerSql = "SELECT " +
                            "    t.tgname as name, " +
                            "    'TRIGGER' as type, " +
                            "    CASE WHEN t.tgenabled = 'D' THEN 'DISABLED' ELSE 'ENABLED' END as trigger_status, " +
                            "    CASE " +
                            "        WHEN t.tgtype & 2 = 2 THEN 'BEFORE' " +
                            "        WHEN t.tgtype & 64 = 64 THEN 'INSTEAD OF' " +
                            "        ELSE 'AFTER' " +
                            "    END as trigger_type, " +
                            "    CASE " +
                            "        WHEN t.tgtype & 4 = 4 THEN 'INSERT' " +
                            "        WHEN t.tgtype & 8 = 8 THEN 'DELETE' " +
                            "        WHEN t.tgtype & 16 = 16 THEN 'UPDATE' " +
                            "        WHEN t.tgtype & 32 = 32 THEN 'TRUNCATE' " +
                            "        ELSE 'UNKNOWN' " +
                            "    END as triggering_event, " +
                            "    c.relname as table_name, " +
                            "    pg_get_triggerdef(t.oid) as description, " +
                            "    NULL as created, " +
                            "    NULL as last_ddl_time, " +
                            "    'VALID' as object_status, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM pg_trigger t " +
                            "JOIN pg_class c ON t.tgrelid = c.oid " +
                            "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                            "WHERE n.nspname = current_schema() " +
                            "AND NOT t.tgisinternal " +
                            "ORDER BY t.tgname " +
                            "OFFSET ? LIMIT ?";

                    List<Map<String, Object>> triggers = getJdbcTemplate().queryForList(triggerSql, trigOffset, trigLimit);
                    allItems.addAll(triggers);
                }
            }

            List<Map<String, Object>> transformed = transformTriggerItems(allItems);
            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getAllTriggersForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }

    public Map<String, Object> getTriggerDetails(String owner, String triggerName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    t.tgname as trigger_name, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 2 = 2 THEN 'BEFORE' " +
                    "        WHEN t.tgtype & 64 = 64 THEN 'INSTEAD OF' " +
                    "        ELSE 'AFTER' " +
                    "    END as trigger_type, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 4 = 4 THEN 'INSERT' " +
                    "        WHEN t.tgtype & 8 = 8 THEN 'DELETE' " +
                    "        WHEN t.tgtype & 16 = 16 THEN 'UPDATE' " +
                    "        WHEN t.tgtype & 32 = 32 THEN 'TRUNCATE' " +
                    "        ELSE 'UNKNOWN' " +
                    "    END as triggering_event, " +
                    "    c.relname as table_name, " +
                    "    CASE WHEN t.tgenabled = 'D' THEN 'DISABLED' ELSE 'ENABLED' END as trigger_status, " +
                    "    pg_get_triggerdef(t.oid) as trigger_body, " +
                    "    pg_get_triggerdef(t.oid) as description, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as object_status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_trigger t " +
                    "JOIN pg_class c ON t.tgrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND t.tgname = ? " +
                    "AND NOT t.tgisinternal";

            Map<String, Object> triggerInfo = getJdbcTemplate().queryForMap(sql, owner, triggerName);
            details.putAll(triggerInfo);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Trigger {}.{} not found", owner, triggerName);
            details.put("error", "Trigger not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting trigger details for {}.{}: {}", owner, triggerName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    public Map<String, Object> getTriggerDetails(String triggerName) {
        try {
            Map<String, Object> triggerLocation = findObjectLocation(triggerName, "TRIGGER");
            String owner = (String) triggerLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getTriggerDetails(owner, triggerName);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Trigger {} not found", triggerName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("trigger_name", triggerName);
            emptyResult.put("message", "Trigger not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getTriggerDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve trigger details: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getTriggersBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    t.tgname as trigger_name, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 2 = 2 THEN 'BEFORE' " +
                    "        WHEN t.tgtype & 64 = 64 THEN 'INSTEAD OF' " +
                    "        ELSE 'AFTER' " +
                    "    END as trigger_type, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 4 = 4 THEN 'INSERT' " +
                    "        WHEN t.tgtype & 8 = 8 THEN 'DELETE' " +
                    "        WHEN t.tgtype & 16 = 16 THEN 'UPDATE' " +
                    "        WHEN t.tgtype & 32 = 32 THEN 'TRUNCATE' " +
                    "        ELSE 'UNKNOWN' " +
                    "    END as triggering_event, " +
                    "    c.relname as table_name, " +
                    "    CASE WHEN t.tgenabled = 'D' THEN 'DISABLED' ELSE 'ENABLED' END as trigger_status, " +
                    "    pg_get_triggerdef(t.oid) as description, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as object_status " +
                    "FROM pg_trigger t " +
                    "JOIN pg_class c ON t.tgrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? " +
                    "AND NOT t.tgisinternal " +
                    "ORDER BY t.tgname";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getTriggersBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getTriggersPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_trigger t " +
                    "JOIN pg_class c ON t.tgrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND NOT t.tgisinternal";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    t.tgname as trigger_name, " +
                    "    CASE WHEN t.tgenabled = 'D' THEN 'DISABLED' ELSE 'ENABLED' END as status, " +
                    "    c.relname as table_name " +
                    "FROM pg_trigger t " +
                    "JOIN pg_class c ON t.tgrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND NOT t.tgisinternal " +
                    "ORDER BY t.tgname " +
                    "OFFSET ? LIMIT ?";
            List<Map<String, Object>> triggers = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", triggers);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getTriggersPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    // ============================================================
    // SEQUENCE METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllSequencesForFrontend() {
        try {
            String sql = "SELECT " +
                    "    c.relname as sequence_name, " +
                    "    s.seqmin as min_value, " +
                    "    s.seqmax as max_value, " +
                    "    s.seqincrement as increment_by, " +
                    "    CASE WHEN s.seqcycle THEN 'Y' ELSE 'N' END as cycle_flag, " +
                    "    'N' as order_flag, " +
                    "    s.seqcache as cache_size, " +
                    "    s.seqstart as last_number " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'S' " +
                    "ORDER BY c.relname";

            List<Map<String, Object>> sequences = getJdbcTemplate().queryForList(sql);

            return sequences.stream().map(seq -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "sequence-" + System.currentTimeMillis() + "-" + seq.get("sequence_name"));
                transformed.put("name", seq.get("sequence_name"));
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "SEQUENCE");
                transformed.put("minValue", seq.get("min_value"));
                transformed.put("maxValue", seq.get("max_value"));
                transformed.put("incrementBy", seq.get("increment_by"));
                transformed.put("cycleFlag", seq.get("cycle_flag"));
                transformed.put("orderFlag", seq.get("order_flag"));
                transformed.put("cacheSize", seq.get("cache_size"));
                transformed.put("lastNumber", seq.get("last_number"));
                transformed.put("icon", "sequence");
                transformed.put("isSynonym", false);
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllSequencesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getAllSequencesForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'S'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    c.relname as sequence_name, " +
                    "    s.seqmin as min_value, " +
                    "    s.seqmax as max_value, " +
                    "    s.seqincrement as increment_by, " +
                    "    CASE WHEN s.seqcycle THEN 'Y' ELSE 'N' END as cycle_flag, " +
                    "    'N' as order_flag, " +
                    "    s.seqcache as cache_size, " +
                    "    s.seqstart as last_number " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'S' " +
                    "ORDER BY c.relname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> sequences = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> transformed = sequences.stream().map(seq -> {
                Map<String, Object> transformedSeq = new HashMap<>();
                transformedSeq.put("id", "sequence-" + System.currentTimeMillis() + "-" + seq.get("sequence_name"));
                transformedSeq.put("name", seq.get("sequence_name"));
                transformedSeq.put("owner", getCurrentSchema());
                transformedSeq.put("type", "SEQUENCE");
                transformedSeq.put("minValue", seq.get("min_value"));
                transformedSeq.put("maxValue", seq.get("max_value"));
                transformedSeq.put("incrementBy", seq.get("increment_by"));
                transformedSeq.put("cycleFlag", seq.get("cycle_flag"));
                transformedSeq.put("orderFlag", seq.get("order_flag"));
                transformedSeq.put("cacheSize", seq.get("cache_size"));
                transformedSeq.put("lastNumber", seq.get("last_number"));
                transformedSeq.put("icon", "sequence");
                transformedSeq.put("isSynonym", false);
                return transformedSeq;
            }).collect(Collectors.toList());

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getAllSequencesForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    public Map<String, Object> getSequenceDetails(String owner, String sequenceName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    c.relname as sequence_name, " +
                    "    s.seqmin as min_value, " +
                    "    s.seqmax as max_value, " +
                    "    s.seqincrement as increment_by, " +
                    "    CASE WHEN s.seqcycle THEN 'Y' ELSE 'N' END as cycle_flag, " +
                    "    'N' as order_flag, " +
                    "    s.seqcache as cache_size, " +
                    "    s.seqstart as last_number " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = ? AND c.relname = ? " +
                    "AND c.relkind = 'S'";

            Map<String, Object> seqInfo = getJdbcTemplate().queryForMap(sql, owner, sequenceName);
            details.putAll(seqInfo);

            // Get current value if possible
            try {
                String currValSql = "SELECT currval(quote_ident(?) || '.' || quote_ident(?))";
                Long currVal = getJdbcTemplate().queryForObject(currValSql, Long.class, owner, sequenceName);
                details.put("current_value", currVal);
            } catch (Exception e) {
                details.put("current_value", "Not available in current session");
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Sequence {}.{} not found", owner, sequenceName);
            details.put("error", "Sequence not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting sequence details for {}.{}: {}", owner, sequenceName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    public Map<String, Object> getSequenceDetails(String sequenceName) {
        try {
            Map<String, Object> seqLocation = findObjectLocation(sequenceName, "SEQUENCE");
            String owner = (String) seqLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getSequenceDetails(owner, sequenceName);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Sequence {} not found", sequenceName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("sequence_name", sequenceName);
            emptyResult.put("message", "Sequence not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getSequenceDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequence details: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getSequencesBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    c.relname as sequence_name, " +
                    "    s.seqmin as min_value, " +
                    "    s.seqmax as max_value, " +
                    "    s.seqincrement as increment_by, " +
                    "    CASE WHEN s.seqcycle THEN 'Y' ELSE 'N' END as cycle_flag, " +
                    "    'N' as order_flag, " +
                    "    s.seqcache as cache_size, " +
                    "    s.seqstart as last_number " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = ? AND c.relkind = 'S' " +
                    "ORDER BY c.relname";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getSequencesBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getSequencesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND c.relkind = 'S'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    c.relname as sequence_name, " +
                    "    s.seqmin as min_value, " +
                    "    s.seqmax as max_value, " +
                    "    s.seqincrement as increment_by, " +
                    "    CASE WHEN s.seqcycle THEN 'Y' ELSE 'N' END as cycle_flag, " +
                    "    s.seqcache as cache_size, " +
                    "    s.seqstart as last_number " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'S' " +
                    "ORDER BY c.relname " +
                    "OFFSET ? LIMIT ?";
            List<Map<String, Object>> sequences = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", sequences);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getSequencesPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    // ============================================================
    // TYPE METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllTypesForFrontend() {
        try {
            String sql = "SELECT " +
                    "    t.typname as type_name, " +
                    "    CASE " +
                    "        WHEN t.typtype = 'c' THEN 'OBJECT' " +
                    "        WHEN t.typtype = 'd' THEN 'DOMAIN' " +
                    "        WHEN t.typtype = 'e' THEN 'ENUM' " +
                    "        WHEN t.typtype = 'p' THEN 'PSEUDO' " +
                    "        ELSE 'BASE' " +
                    "    END as typecode, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a WHERE a.attrelid = t.typrelid) as attributes, " +
                    "    0 as methods, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status " +
                    "FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND t.typtype IN ('c', 'd', 'e') " +
                    "ORDER BY t.typname";

            List<Map<String, Object>> types = getJdbcTemplate().queryForList(sql);

            return types.stream().map(type -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "type-" + System.currentTimeMillis() + "-" + type.get("type_name"));
                transformed.put("name", type.get("type_name"));
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "TYPE");
                transformed.put("status", type.get("status"));
                transformed.put("typecode", type.get("typecode"));
                transformed.put("attributeCount", type.get("attributes"));
                transformed.put("methodCount", type.get("methods"));
                transformed.put("created", type.get("created"));
                transformed.put("lastModified", type.get("last_ddl_time"));
                transformed.put("icon", "type");
                transformed.put("isSynonym", false);
                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllTypesForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getAllTypesForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND t.typtype IN ('c', 'd', 'e')";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    t.typname as type_name, " +
                    "    CASE " +
                    "        WHEN t.typtype = 'c' THEN 'OBJECT' " +
                    "        WHEN t.typtype = 'd' THEN 'DOMAIN' " +
                    "        WHEN t.typtype = 'e' THEN 'ENUM' " +
                    "        ELSE 'BASE' " +
                    "    END as typecode, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a WHERE a.attrelid = t.typrelid) as attributes, " +
                    "    0 as methods, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status " +
                    "FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND t.typtype IN ('c', 'd', 'e') " +
                    "ORDER BY t.typname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> types = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> transformed = types.stream().map(type -> {
                Map<String, Object> transformedType = new HashMap<>();
                transformedType.put("id", "type-" + System.currentTimeMillis() + "-" + type.get("type_name"));
                transformedType.put("name", type.get("type_name"));
                transformedType.put("owner", getCurrentSchema());
                transformedType.put("type", "TYPE");
                transformedType.put("status", type.get("status"));
                transformedType.put("typecode", type.get("typecode"));
                transformedType.put("attributeCount", type.get("attributes"));
                transformedType.put("methodCount", type.get("methods"));
                transformedType.put("created", type.get("created"));
                transformedType.put("lastModified", type.get("last_ddl_time"));
                transformedType.put("icon", "type");
                transformedType.put("isSynonym", false);
                return transformedType;
            }).collect(Collectors.toList());

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getAllTypesForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    public Map<String, Object> getTypeDetails(String owner, String typeName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    t.typname as type_name, " +
                    "    CASE " +
                    "        WHEN t.typtype = 'c' THEN 'OBJECT' " +
                    "        WHEN t.typtype = 'd' THEN 'DOMAIN' " +
                    "        WHEN t.typtype = 'e' THEN 'ENUM' " +
                    "        ELSE 'BASE' " +
                    "    END as typecode, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a WHERE a.attrelid = t.typrelid AND a.attnum > 0) as attributes, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated " +
                    "FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = ? AND t.typname = ? " +
                    "AND t.typtype IN ('c', 'd', 'e')";

            Map<String, Object> typeInfo = getJdbcTemplate().queryForMap(sql, owner, typeName);
            details.putAll(typeInfo);

            // Get attributes for composite types
            if ("OBJECT".equals(details.get("typecode"))) {
                String attrSql = "SELECT " +
                        "    a.attname as attr_name, " +
                        "    format_type(a.atttypid, a.atttypmod) as attr_type_name, " +
                        "    a.attlen as length, " +
                        "    a.atttypmod as precision, " +
                        "    0 as scale " +
                        "FROM pg_attribute a " +
                        "JOIN pg_type t ON a.attrelid = t.typrelid " +
                        "WHERE t.typname = ? AND a.attnum > 0 " +
                        "ORDER BY a.attnum";

                List<Map<String, Object>> attributes = getJdbcTemplate().queryForList(attrSql, typeName);
                details.put("attributes", attributes);
            }

        } catch (EmptyResultDataAccessException e) {
            log.warn("Type {}.{} not found", owner, typeName);
            details.put("error", "Type not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting type details for {}.{}: {}", owner, typeName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    public Map<String, Object> getTypeDetails(String typeName) {
        try {
            Map<String, Object> typeLocation = findObjectLocation(typeName, "TYPE");
            String owner = (String) typeLocation.get("owner");
            if (owner == null) {
                owner = getCurrentSchema();
            }
            return getTypeDetails(owner, typeName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Type {} not found", typeName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("type_name", typeName);
            emptyResult.put("message", "Type not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getTypeDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve type details: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getTypesBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    t.typname as type_name, " +
                    "    CASE " +
                    "        WHEN t.typtype = 'c' THEN 'OBJECT' " +
                    "        WHEN t.typtype = 'd' THEN 'DOMAIN' " +
                    "        WHEN t.typtype = 'e' THEN 'ENUM' " +
                    "        ELSE 'BASE' " +
                    "    END as typecode, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a WHERE a.attrelid = t.typrelid AND a.attnum > 0) as attributes, " +
                    "    0 as methods, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status " +
                    "FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = ? " +
                    "AND t.typtype IN ('c', 'd', 'e') " +
                    "ORDER BY t.typname";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getTypesBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getTypesPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND t.typtype IN ('c', 'd', 'e')";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    t.typname as type_name, " +
                    "    CASE WHEN t.typtype = 'c' THEN 'OBJECT' WHEN t.typtype = 'd' THEN 'DOMAIN' WHEN t.typtype = 'e' THEN 'ENUM' ELSE 'BASE' END as typecode, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a WHERE a.attrelid = t.typrelid AND a.attnum > 0) as attributes, " +
                    "    0 as methods " +
                    "FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND t.typtype IN ('c', 'd', 'e') " +
                    "ORDER BY t.typname " +
                    "OFFSET ? LIMIT ?";
            List<Map<String, Object>> types = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", types);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getTypesPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    // ============================================================
    // DATABASE LINK METHODS - POSTGRESQL VERSION
    // ============================================================

    public Map<String, Object> getDatabaseLinkDetails(String owner, String dbLinkName) {
        Map<String, Object> details = new HashMap<>();
        try {
            // PostgreSQL uses foreign data wrappers for database links
            String sql = "SELECT " +
                    "    fdw.fdwname as db_link, " +
                    "    fs.srvname as server_name, " +
                    "    fs.srvoptions as host, " +
                    "    NULL as username, " +
                    "    NULL as created " +
                    "FROM pg_foreign_data_wrapper fdw " +
                    "LEFT JOIN pg_foreign_server fs ON fs.srvfdw = fdw.oid " +
                    "WHERE fdw.fdwname = ?";

            Map<String, Object> linkInfo = getJdbcTemplate().queryForMap(sql, dbLinkName);
            details.putAll(linkInfo);
            details.put("owner", owner);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Database link {}.{} not found", owner, dbLinkName);
            details.put("error", "Database link not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting database link details for {}.{}: {}", owner, dbLinkName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    public List<Map<String, Object>> getAllDbLinks() {
        try {
            String sql = "SELECT " +
                    "    fdw.fdwname as db_link, " +
                    "    NULL as username, " +
                    "    fs.srvoptions as host, " +
                    "    NULL as created " +
                    "FROM pg_foreign_data_wrapper fdw " +
                    "LEFT JOIN pg_foreign_server fs ON fs.srvfdw = fdw.oid " +
                    "ORDER BY fdw.fdwname";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllDbLinks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getDbLinksBySchema(String schemaName) {
        try {
            // PostgreSQL foreign data wrappers are not schema-specific
            return getAllDbLinks();
        } catch (Exception e) {
            log.error("Error in getDbLinksBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links for schema: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // SYNONYM METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllSynonymsForFrontend() {
        // PostgreSQL doesn't have synonyms
        return new ArrayList<>();
    }

    public Map<String, Object> getAllSynonymsForFrontend(int page, int pageSize) {
        // PostgreSQL doesn't have synonyms
        Map<String, Object> result = new HashMap<>();
        result.put("items", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", 0);
        return result;
    }

    public Map<String, Object> getSynonymDetails(String synonymName) {
        // PostgreSQL doesn't have synonyms
        Map<String, Object> emptyResult = new HashMap<>();
        emptyResult.put("synonym_name", synonymName);
        emptyResult.put("message", "Synonyms not supported in PostgreSQL");
        emptyResult.put("status", "NOT_SUPPORTED");
        return emptyResult;
    }

    public Map<String, Object> resolveSynonym(String synonymName) {
        // PostgreSQL doesn't have synonyms
        Map<String, Object> notFound = new HashMap<>();
        notFound.put("synonym_name", synonymName);
        notFound.put("message", "Synonyms not supported in PostgreSQL");
        notFound.put("status", "NOT_SUPPORTED");
        return notFound;
    }

    public Map<String, Object> getSynonymTargetDetails(String synonymName) {
        // PostgreSQL doesn't have synonyms
        Map<String, Object> result = new HashMap<>();
        result.put("error", "Synonyms not supported in PostgreSQL");
        return result;
    }

    public Map<String, Object> validateSynonym(String synonymName) {
        // PostgreSQL doesn't have synonyms
        Map<String, Object> result = new HashMap<>();
        result.put("synonymName", synonymName);
        result.put("exists", false);
        result.put("status", "NOT_SUPPORTED");
        result.put("message", "Synonyms not supported in PostgreSQL");
        return result;
    }

    public List<Map<String, Object>> getSynonymsBySchema(String schemaName) {
        // PostgreSQL doesn't have synonyms
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getAllSynonyms() {
        // PostgreSQL doesn't have synonyms
        return new ArrayList<>();
    }

    public Map<String, Object> getSynonymsPaginated(int page, int pageSize) {
        // PostgreSQL doesn't have synonyms
        Map<String, Object> result = new HashMap<>();
        result.put("items", new ArrayList<>());
        result.put("totalCount", 0);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", 0);
        return result;
    }

    public List<Map<String, Object>> getSynonymsByTargetType(String targetType) {
        // PostgreSQL doesn't have synonyms
        return new ArrayList<>();
    }

    // ============================================================
    // MATERIALIZED VIEW METHODS - POSTGRESQL VERSION
    // ============================================================

    public Map<String, Object> getMaterializedViewDetails(String owner, String mvName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    c.relname as mview_name, " +
                    "    c.relname as container_name, " +
                    "    pg_get_viewdef(c.oid) as query, " +
                    "    'COMPLETE' as refresh_method, " +
                    "    'MANUAL' as refresh_mode, " +
                    "    'IMMEDIATE' as build_mode, " +
                    "    NULL as fast_refreshable, " +
                    "    NULL as last_refresh_type, " +
                    "    pg_stat_get_last_analyze_time(c.oid) as last_refresh_date, " +
                    "    'FRESH' as staleness, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = ? AND c.relname = ? " +
                    "AND c.relkind = 'm'";

            Map<String, Object> mvInfo = getJdbcTemplate().queryForMap(sql, owner, mvName);
            details.putAll(mvInfo);

            // Get columns
            String colSql = "SELECT column_name, data_type, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ? " +
                    "ORDER BY ordinal_position";

            List<Map<String, Object>> columns = getJdbcTemplate().queryForList(colSql, owner, mvName);
            details.put("columns", columns);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Materialized view {}.{} not found", owner, mvName);
            details.put("error", "Materialized view not found");
            details.put("exists", false);
        } catch (Exception e) {
            log.warn("Error getting materialized view details for {}.{}: {}", owner, mvName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> transformTriggerItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("TRIGGER".equals(type)) {
                transformedItem.put("id", "trigger-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentSchema());
                transformedItem.put("type", "TRIGGER");
                transformedItem.put("status", item.get("trigger_status"));
                transformedItem.put("objectStatus", item.get("object_status"));
                transformedItem.put("triggerType", item.get("trigger_type"));
                transformedItem.put("triggeringEvent", item.get("triggering_event"));
                transformedItem.put("tableName", item.get("table_name"));
                transformedItem.put("description", item.get("description"));
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("last_ddl_time"));
                transformedItem.put("icon", "trigger");
                transformedItem.put("isSynonym", false);
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }

    public List<Map<String, Object>> getAllSequences() {
        try {
            String sql = "SELECT " +
                    "    c.relname as sequence_name, " +
                    "    s.seqmin as min_value, " +
                    "    s.seqmax as max_value, " +
                    "    s.seqincrement as increment_by, " +
                    "    CASE WHEN s.seqcycle THEN 'Y' ELSE 'N' END as cycle_flag, " +
                    "    'N' as order_flag, " +
                    "    s.seqcache as cache_size, " +
                    "    s.seqstart as last_number " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "JOIN pg_sequence s ON c.oid = s.seqrelid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND c.relkind = 'S' " +
                    "ORDER BY c.relname";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSequences: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequences: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getAllTypes() {
        try {
            String sql = "SELECT " +
                    "    t.typname as type_name, " +
                    "    CASE " +
                    "        WHEN t.typtype = 'c' THEN 'OBJECT' " +
                    "        WHEN t.typtype = 'd' THEN 'DOMAIN' " +
                    "        WHEN t.typtype = 'e' THEN 'ENUM' " +
                    "        ELSE 'BASE' " +
                    "    END as typecode, " +
                    "    (SELECT COUNT(*) FROM pg_attribute a WHERE a.attrelid = t.typrelid AND a.attnum > 0) as attributes, " +
                    "    0 as methods, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status " +
                    "FROM pg_type t " +
                    "JOIN pg_namespace n ON t.typnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND t.typtype IN ('c', 'd', 'e') " +
                    "ORDER BY t.typname";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTypes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve types: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getAllTriggers() {
        try {
            String sql = "SELECT " +
                    "    t.tgname as trigger_name, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 2 = 2 THEN 'BEFORE' " +
                    "        WHEN t.tgtype & 64 = 64 THEN 'INSTEAD OF' " +
                    "        ELSE 'AFTER' " +
                    "    END as trigger_type, " +
                    "    CASE " +
                    "        WHEN t.tgtype & 4 = 4 THEN 'INSERT' " +
                    "        WHEN t.tgtype & 8 = 8 THEN 'DELETE' " +
                    "        WHEN t.tgtype & 16 = 16 THEN 'UPDATE' " +
                    "        WHEN t.tgtype & 32 = 32 THEN 'TRUNCATE' " +
                    "        ELSE 'UNKNOWN' " +
                    "    END as triggering_event, " +
                    "    c.relname as table_name, " +
                    "    CASE WHEN t.tgenabled = 'D' THEN 'DISABLED' ELSE 'ENABLED' END as trigger_status, " +
                    "    pg_get_triggerdef(t.oid) as trigger_body, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as object_status " +
                    "FROM pg_trigger t " +
                    "JOIN pg_class c ON t.tgrelid = c.oid " +
                    "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND NOT t.tgisinternal " +
                    "ORDER BY t.tgname";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTriggers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve triggers: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getFunctionsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    n.nspname as owner, " +
                    "    p.proname as function_name, " +
                    "    'FUNCTION' as object_type, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary, " +
                    "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                    "    pg_get_function_result(p.oid) as return_type " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.prokind = 'f' " +
                    "ORDER BY p.proname";

            return getJdbcTemplate().queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getFunctionsBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve functions for schema: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getAllSynonymsWithDetails() {
        // PostgreSQL doesn't have synonyms
        return new ArrayList<>();
    }

    private Map<String, Object> getBasicObjectInfo(String owner, String objectName, String objectType) {
        try {
            String relKind = getRelationKind(objectType);
            if (relKind != null) {
                String sql = "SELECT " +
                        "    n.nspname as owner, " +
                        "    c.relname as object_name, " +
                        "    CASE WHEN c.relkind = 'r' THEN 'TABLE' " +
                        "         WHEN c.relkind = 'v' THEN 'VIEW' " +
                        "         WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW' " +
                        "         ELSE 'OTHER' END as object_type, " +
                        "    NULL as created, " +
                        "    NULL as last_ddl_time, " +
                        "    'VALID' as status, " +
                        "    false as temporary, " +
                        "    false as generated, " +
                        "    false as secondary " +
                        "FROM pg_class c " +
                        "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                        "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = ?";
                return getJdbcTemplate().queryForMap(sql, owner, objectName, relKind);
            }
        } catch (Exception e) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("owner", owner);
            empty.put("object_name", objectName);
            empty.put("object_type", objectType);
            empty.put("status", "VALID");
            return empty;
        }
        return new HashMap<>();
    }

    private String getRelationKind(String objectType) {
        if (objectType == null) return null;
        String upperType = objectType.toUpperCase();
        switch (upperType) {
            case "TABLE": return "r";
            case "VIEW": return "v";
            case "MATERIALIZED VIEW": return "m";
            default: return null;
        }
    }
}