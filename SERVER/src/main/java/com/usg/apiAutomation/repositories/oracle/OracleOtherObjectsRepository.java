package com.usg.apiAutomation.repositories.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OracleOtherObjectsRepository extends OracleRepository {

    // ============================================================
    // TRIGGER METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getAllTriggersForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String triggerSql = "SELECT " +
                    "    t.trigger_name as name, " +
                    "    'TRIGGER' as type, " +
                    "    t.trigger_type, " +
                    "    t.triggering_event, " +
                    "    t.table_name, " +
                    "    t.status as trigger_status, " +
                    "    t.description, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status as object_status, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link " +
                    "FROM user_triggers t " +
                    "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "ORDER BY t.trigger_name";

            List<Map<String, Object>> triggers = getJdbcTemplate().queryForList(triggerSql);

            for (Map<String, Object> trigger : triggers) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "trigger-" + System.currentTimeMillis() + "-" + trigger.get("name"));
                transformed.put("name", trigger.get("name"));
                transformed.put("owner", getCurrentUser());
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

            result.addAll(getTriggerSynonyms());
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (triggers + synonyms)", result.size());

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

            String triggerCountSql = "SELECT COUNT(*) FROM user_triggers";
            int triggerCount = getJdbcTemplate().queryForObject(triggerCountSql, Integer.class);

            String synonymCountSql =
                    "SELECT COUNT(*) FROM user_synonyms s " +
                            "WHERE EXISTS (SELECT 1 FROM all_objects " +
                            "              WHERE owner = s.table_owner " +
                            "                AND object_name = s.table_name " +
                            "                AND object_type = 'TRIGGER') " +
                            "   OR (s.db_link IS NOT NULL)";

            int synonymCount = 0;
            try {
                synonymCount = getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
            } catch (Exception e) {
                log.warn("Error counting synonyms for triggers: {}", e.getMessage());
            }

            int totalCount = triggerCount + synonymCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (triggerCount > 0 && offset < triggerCount) {
                int trigOffset = offset;
                int trigLimit = Math.min(pageSize, triggerCount - trigOffset);

                if (trigLimit > 0) {
                    String triggerSql = "SELECT " +
                            "    t.trigger_name as name, " +
                            "    'TRIGGER' as type, " +
                            "    t.trigger_type, " +
                            "    t.triggering_event, " +
                            "    t.table_name, " +
                            "    t.status as trigger_status, " +
                            "    t.description, " +
                            "    o.created, " +
                            "    o.last_ddl_time, " +
                            "    o.status as object_status, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM user_triggers t " +
                            "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                            "ORDER BY t.trigger_name " +
                            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    List<Map<String, Object>> triggers = getJdbcTemplate().queryForList(triggerSql, trigOffset, trigLimit);
                    allItems.addAll(triggers);
                }
            }

            if (allItems.size() < pageSize && synonymCount > 0) {
                addTriggerSynonymsPaginated(allItems, page, pageSize, triggerCount);
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
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    t.trigger_name, " +
                        "    t.trigger_type, " +
                        "    t.triggering_event, " +
                        "    t.table_name, " +
                        "    t.referencing_names, " +
                        "    t.when_clause, " +
                        "    t.status as trigger_status, " +
                        "    t.description, " +
                        "    t.trigger_body, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status, " +
                        "    o.temporary, " +
                        "    o.generated, " +
                        "    o.secondary " +
                        "FROM user_triggers t " +
                        "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                        "WHERE UPPER(t.trigger_name) = UPPER(?)";

                Map<String, Object> triggerInfo = getJdbcTemplate().queryForMap(sql, triggerName);
                details.putAll(triggerInfo);

                // Get columns referenced if any
                String colSql = "SELECT column_name FROM user_trigger_cols " +
                        "WHERE UPPER(trigger_name) = UPPER(?) ORDER BY column_name";

                List<Map<String, Object>> columns = getJdbcTemplate().queryForList(colSql, triggerName);
                if (!columns.isEmpty()) {
                    details.put("referenced_columns", columns);
                }

            } else {
                sql = "SELECT " +
                        "    t.owner, " +
                        "    t.trigger_name, " +
                        "    t.trigger_type, " +
                        "    t.triggering_event, " +
                        "    t.table_owner, " +
                        "    t.table_name, " +
                        "    t.referencing_names, " +
                        "    t.when_clause, " +
                        "    t.status as trigger_status, " +
                        "    t.description, " +
                        "    t.trigger_body, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status as object_status, " +
                        "    o.temporary, " +
                        "    o.generated, " +
                        "    o.secondary " +
                        "FROM all_triggers t " +
                        "JOIN all_objects o ON t.owner = o.owner AND t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                        "WHERE UPPER(t.owner) = UPPER(?) AND UPPER(t.trigger_name) = UPPER(?)";

                Map<String, Object> triggerInfo = getJdbcTemplate().queryForMap(sql, owner, triggerName);
                details.putAll(triggerInfo);
            }

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
                owner = getCurrentUser();
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
                    "    t.owner, " +
                    "    t.trigger_name, " +
                    "    t.trigger_type, " +
                    "    t.triggering_event, " +
                    "    t.table_owner, " +
                    "    t.table_name, " +
                    "    t.status as trigger_status, " +
                    "    t.description, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status as object_status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_triggers t " +
                    "JOIN all_objects o ON t.owner = o.owner AND t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.trigger_name";
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

            String countSql = "SELECT COUNT(*) FROM user_triggers";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT trigger_name, trigger_type, triggering_event, table_name, status " +
                    "FROM user_triggers ORDER BY trigger_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
    // SYNONYM METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getAllSynonymsForFrontend() {
        try {
            List<Map<String, Object>> synonyms = getAllSynonymsWithDetails();

            return synonyms.stream().map(syn -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "synonym-" + System.currentTimeMillis() + "-" + syn.get("synonym_name"));
                transformed.put("name", syn.get("synonym_name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type"));
                transformed.put("targetStatus", syn.get("target_status"));
                transformed.put("targetCreated", syn.get("target_created"));
                transformed.put("targetModified", syn.get("target_modified"));
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);
                transformed.put("isSynonym", true);

                if (syn.get("target_type") != null) {
                    String targetType = syn.get("target_type").toString();
                    transformed.put("targetIcon", getObjectTypeIcon(targetType));
                    transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                }

                return transformed;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getAllSynonymsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getAllSynonymsForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_synonyms";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'DATABASE_LINK' " +
                    "        ELSE (SELECT object_type FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_type, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                    "        ELSE (SELECT status FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_status " +
                    "FROM user_synonyms s " +
                    "ORDER BY s.synonym_name " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> transformed = synonyms.stream().map(syn -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", "syn-" + System.currentTimeMillis() + "-" + syn.get("synonym_name"));
                item.put("name", syn.get("synonym_name"));
                item.put("owner", getCurrentUser());
                item.put("type", "SYNONYM");
                item.put("isSynonym", true);
                item.put("targetOwner", syn.get("target_owner"));
                item.put("targetName", syn.get("target_name"));
                item.put("dbLink", syn.get("db_link"));
                item.put("isRemote", syn.get("db_link") != null);

                String targetType = (String) syn.get("target_type");
                item.put("targetType", targetType != null ? targetType : "UNKNOWN");

                if (syn.get("target_status") != null) {
                    item.put("targetStatus", syn.get("target_status"));
                }

                if (targetType != null) {
                    item.put("targetIcon", getObjectTypeIcon(targetType));
                    item.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                }

                item.put("icon", "synonym");
                return item;
            }).collect(Collectors.toList());

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getAllSynonymsForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }

    public Map<String, Object> getSynonymDetails(String synonymName) {
        try {
            log.info("Getting details for synonym: {}", synonymName);

            String sql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    o.object_type as target_type, " +
                    "    o.status as target_status, " +
                    "    o.created as target_created, " +
                    "    o.last_ddl_time as target_modified, " +
                    "    o.temporary as target_temporary, " +
                    "    o.generated as target_generated, " +
                    "    o.secondary as target_secondary " +
                    "FROM user_synonyms s " +
                    "LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) = UPPER(?)";

            Map<String, Object> synonymInfo = getJdbcTemplate().queryForMap(sql, synonymName);

            String targetType = (String) synonymInfo.get("target_type");
            String targetOwner = (String) synonymInfo.get("target_owner");
            String targetName = (String) synonymInfo.get("target_name");
            String dbLink = (String) synonymInfo.get("db_link");

            if (targetType != null && dbLink == null) {
                Map<String, Object> targetInfo = getBasicObjectInfo(targetOwner, targetName, targetType);
                synonymInfo.put("targetBasicInfo", targetInfo);
            } else if (dbLink != null) {
                Map<String, Object> remoteInfo = new HashMap<>();
                remoteInfo.put("message", "Remote object via database link: " + dbLink);
                remoteInfo.put("dbLink", dbLink);
                remoteInfo.put("fullPath", targetOwner + "." + targetName + "@" + dbLink);
                synonymInfo.put("targetDetails", remoteInfo);
            }

            return synonymInfo;

        } catch (EmptyResultDataAccessException e) {
            log.warn("Synonym {} not found", synonymName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("synonym_name", synonymName);
            emptyResult.put("message", "Synonym not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getSynonymDetails for {}: {}", synonymName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve details for synonym " + synonymName + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> resolveSynonym(String synonymName) {
        try {
            String sql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    o.object_type as target_type, " +
                    "    o.status as target_status, " +
                    "    CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE 'LOCAL' END as location_type " +
                    "FROM user_synonyms s " +
                    "LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) = UPPER(?)";

            Map<String, Object> resolved = getJdbcTemplate().queryForMap(sql, synonymName);

            if (resolved.get("db_link") != null) {
                resolved.put("isRemote", true);
                resolved.put("target_type", "DATABASE_LINK");
            }

            StringBuilder navPath = new StringBuilder();
            if (resolved.get("db_link") != null) {
                navPath.append(resolved.get("target_owner"))
                        .append(".")
                        .append(resolved.get("target_name"))
                        .append("@")
                        .append(resolved.get("db_link"));
            } else {
                navPath.append(resolved.get("target_owner"))
                        .append(".")
                        .append(resolved.get("target_name"));
            }
            resolved.put("navigationPath", navPath.toString());

            return resolved;

        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("synonym_name", synonymName);
            notFound.put("message", "Synonym not found");
            notFound.put("status", "NOT_FOUND");
            return notFound;
        } catch (Exception e) {
            log.error("Error resolving synonym {}: {}", synonymName, e.getMessage());
            throw new RuntimeException("Failed to resolve synonym: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getSynonymTargetDetails(String synonymName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    o.object_type as target_type, " +
                    "    o.status as target_status, " +
                    "    o.created as target_created, " +
                    "    o.last_ddl_time as target_modified " +
                    "FROM user_synonyms s " +
                    "LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE UPPER(s.synonym_name) = UPPER(?)";

            Map<String, Object> target = getJdbcTemplate().queryForMap(sql, synonymName);

            result.put("targetOwner", target.get("target_owner"));
            result.put("targetName", target.get("target_name"));
            result.put("targetType", target.get("target_type") != null ? target.get("target_type") : "UNKNOWN");
            result.put("targetStatus", target.get("target_status"));
            result.put("dbLink", target.get("db_link"));
            result.put("isRemote", target.get("db_link") != null);
            result.put("targetCreated", target.get("target_created"));
            result.put("targetModified", target.get("target_modified"));

            if (target.get("db_link") != null) {
                result.put("fullPath", target.get("target_owner") + "." +
                        target.get("target_name") + "@" + target.get("db_link"));
            } else {
                result.put("fullPath", target.get("target_owner") + "." + target.get("target_name"));
            }

        } catch (EmptyResultDataAccessException e) {
            result.put("error", "Synonym not found");
        } catch (Exception e) {
            log.error("Error resolving synonym target {}: {}", synonymName, e.getMessage());
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> validateSynonym(String synonymName) {
        Map<String, Object> result = new HashMap<>();
        result.put("synonymName", synonymName);

        try {
            String sql = "SELECT " +
                    "    synonym_name, " +
                    "    table_owner, " +
                    "    table_name, " +
                    "    db_link " +
                    "FROM user_synonyms " +
                    "WHERE UPPER(synonym_name) = UPPER(?)";

            Map<String, Object> synonym = getJdbcTemplate().queryForMap(sql, synonymName);
            result.put("synonym", synonym);
            result.put("exists", true);

            String targetOwner = (String) synonym.get("table_owner");
            String targetName = (String) synonym.get("table_name");
            String dbLink = (String) synonym.get("db_link");

            if (dbLink != null) {
                result.put("isRemote", true);
                result.put("targetStatus", "REMOTE");
                result.put("message", "Remote object via database link: " + dbLink);
            } else {
                String targetSql = "SELECT object_type, status FROM all_objects " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";

                try {
                    Map<String, Object> target = getJdbcTemplate().queryForMap(targetSql, targetOwner, targetName);
                    result.put("targetType", target.get("object_type"));
                    result.put("targetStatus", target.get("status"));
                    result.put("targetValid", "VALID".equalsIgnoreCase((String) target.get("status")));
                } catch (EmptyResultDataAccessException e) {
                    result.put("targetStatus", "TARGET_NOT_FOUND");
                    result.put("message", "Synonym target not found or not accessible");
                }
            }

        } catch (EmptyResultDataAccessException e) {
            result.put("exists", false);
            result.put("status", "NOT_FOUND");
            result.put("message", "Synonym not found");
        } catch (Exception e) {
            result.put("exists", false);
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }

        return result;
    }

    public List<Map<String, Object>> getSynonymsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    synonym_name, " +
                    "    table_owner, " +
                    "    table_name, " +
                    "    db_link " +
                    "FROM all_synonyms " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY synonym_name";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getSynonymsBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // SEQUENCE METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getAllSequencesForFrontend() {
        try {
            String sql = "SELECT " +
                    "    sequence_name, " +
                    "    min_value, " +
                    "    max_value, " +
                    "    increment_by, " +
                    "    cycle_flag, " +
                    "    order_flag, " +
                    "    cache_size, " +
                    "    last_number " +
                    "FROM user_sequences " +
                    "ORDER BY sequence_name";

            List<Map<String, Object>> sequences = getJdbcTemplate().queryForList(sql);

            return sequences.stream().map(seq -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "sequence-" + System.currentTimeMillis() + "-" + seq.get("sequence_name"));
                transformed.put("name", seq.get("sequence_name"));
                transformed.put("owner", getCurrentUser());
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

            String countSql = "SELECT COUNT(*) FROM user_sequences";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    sequence_name, " +
                    "    min_value, " +
                    "    max_value, " +
                    "    increment_by, " +
                    "    cycle_flag, " +
                    "    order_flag, " +
                    "    cache_size, " +
                    "    last_number " +
                    "FROM user_sequences " +
                    "ORDER BY sequence_name " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> sequences = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> transformed = sequences.stream().map(seq -> {
                Map<String, Object> transformedSeq = new HashMap<>();
                transformedSeq.put("id", "sequence-" + System.currentTimeMillis() + "-" + seq.get("sequence_name"));
                transformedSeq.put("name", seq.get("sequence_name"));
                transformedSeq.put("owner", getCurrentUser());
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
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    sequence_name, " +
                        "    min_value, " +
                        "    max_value, " +
                        "    increment_by, " +
                        "    cycle_flag, " +
                        "    order_flag, " +
                        "    cache_size, " +
                        "    last_number, " +
                        "    (CASE WHEN cycle_flag = 'Y' THEN 'YES' ELSE 'NO' END) as cycles, " +
                        "    (CASE WHEN order_flag = 'Y' THEN 'YES' ELSE 'NO' END) as orders " +
                        "FROM user_sequences " +
                        "WHERE UPPER(sequence_name) = UPPER(?)";

                Map<String, Object> seqInfo = getJdbcTemplate().queryForMap(sql, sequenceName);
                details.putAll(seqInfo);

            } else {
                sql = "SELECT " +
                        "    sequence_owner as owner, " +
                        "    sequence_name, " +
                        "    min_value, " +
                        "    max_value, " +
                        "    increment_by, " +
                        "    cycle_flag, " +
                        "    order_flag, " +
                        "    cache_size, " +
                        "    last_number " +
                        "FROM all_sequences " +
                        "WHERE UPPER(sequence_owner) = UPPER(?) AND UPPER(sequence_name) = UPPER(?)";

                Map<String, Object> seqInfo = getJdbcTemplate().queryForMap(sql, owner, sequenceName);
                details.putAll(seqInfo);
            }

            // Get current value if possible
            try {
                String currValSql = "SELECT " + (owner.equalsIgnoreCase(getCurrentUser()) ? "" : owner + ".") +
                        sequenceName + ".CURRVAL FROM DUAL";
                Long currVal = getJdbcTemplate().queryForObject(currValSql, Long.class);
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
                owner = getCurrentUser();
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
                    "    sequence_owner as owner, " +
                    "    sequence_name, " +
                    "    min_value, " +
                    "    max_value, " +
                    "    increment_by, " +
                    "    cycle_flag, " +
                    "    order_flag, " +
                    "    cache_size, " +
                    "    last_number " +
                    "FROM all_sequences " +
                    "WHERE UPPER(sequence_owner) = UPPER(?) " +
                    "ORDER BY sequence_name";
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

            String countSql = "SELECT COUNT(*) FROM user_sequences";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT sequence_name, min_value, max_value, increment_by, cycle_flag, cache_size, last_number " +
                    "FROM user_sequences ORDER BY sequence_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
    // TYPE METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getAllTypesForFrontend() {
        try {
            String sql = "SELECT " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status " +
                    "FROM user_types t " +
                    "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "ORDER BY t.type_name";

            List<Map<String, Object>> types = getJdbcTemplate().queryForList(sql);

            return types.stream().map(type -> {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "type-" + System.currentTimeMillis() + "-" + type.get("type_name"));
                transformed.put("name", type.get("type_name"));
                transformed.put("owner", getCurrentUser());
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

            String countSql = "SELECT COUNT(*) FROM user_types";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status " +
                    "FROM user_types t " +
                    "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "ORDER BY t.type_name " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> types = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            List<Map<String, Object>> transformed = types.stream().map(type -> {
                Map<String, Object> transformedType = new HashMap<>();
                transformedType.put("id", "type-" + System.currentTimeMillis() + "-" + type.get("type_name"));
                transformedType.put("name", type.get("type_name"));
                transformedType.put("owner", getCurrentUser());
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
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    t.type_name, " +
                        "    t.typecode, " +
                        "    t.attributes, " +
                        "    t.methods, " +
                        "    t.final, " +
                        "    t.instantiable, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status, " +
                        "    o.temporary, " +
                        "    o.generated " +
                        "FROM user_types t " +
                        "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                        "WHERE UPPER(t.type_name) = UPPER(?)";

                Map<String, Object> typeInfo = getJdbcTemplate().queryForMap(sql, typeName);
                details.putAll(typeInfo);

                // Get attributes
                String attrSql = "SELECT " +
                        "    attr_name, " +
                        "    attr_type_name, " +
                        "    length, " +
                        "    precision, " +
                        "    scale, " +
                        "    character_set_name " +
                        "FROM user_type_attrs " +
                        "WHERE UPPER(type_name) = UPPER(?) " +
                        "ORDER BY attr_no";

                List<Map<String, Object>> attributes = getJdbcTemplate().queryForList(attrSql, typeName);
                details.put("attributes", attributes);

                // Get methods
                String methodSql = "SELECT " +
                        "    method_name, " +
                        "    method_no, " +
                        "    method_type, " +
                        "    parameters, " +
                        "    results " +
                        "FROM user_type_methods " +
                        "WHERE UPPER(type_name) = UPPER(?) " +
                        "ORDER BY method_no";

                List<Map<String, Object>> methods = getJdbcTemplate().queryForList(methodSql, typeName);
                details.put("methods", methods);

            } else {
                sql = "SELECT " +
                        "    t.owner, " +
                        "    t.type_name, " +
                        "    t.typecode, " +
                        "    t.attributes, " +
                        "    t.methods, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status, " +
                        "    o.temporary, " +
                        "    o.generated " +
                        "FROM all_types t " +
                        "JOIN all_objects o ON t.owner = o.owner AND t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                        "WHERE UPPER(t.owner) = UPPER(?) AND UPPER(t.type_name) = UPPER(?)";

                Map<String, Object> typeInfo = getJdbcTemplate().queryForMap(sql, owner, typeName);
                details.putAll(typeInfo);

                // Try to get attributes if accessible
                try {
                    String attrSql = "SELECT " +
                            "    attr_name, " +
                            "    attr_type_name, " +
                            "    length, " +
                            "    precision, " +
                            "    scale " +
                            "FROM all_type_attrs " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(type_name) = UPPER(?) " +
                            "ORDER BY attr_no";

                    List<Map<String, Object>> attributes = getJdbcTemplate().queryForList(attrSql, owner, typeName);
                    details.put("attributes", attributes);
                } catch (Exception e) {
                    log.debug("Could not get attributes for type {}.{}", owner, typeName);
                }
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
            if (typeLocation.isEmpty()) {
                typeLocation = findObjectLocation(typeName, "TYPE BODY");
            }

            String owner = (String) typeLocation.get("owner");
            if (owner == null) {
                owner = getCurrentUser();
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
                    "    t.owner, " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM all_types t " +
                    "JOIN all_objects o ON t.owner = o.owner AND t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "WHERE UPPER(t.owner) = UPPER(?) " +
                    "ORDER BY t.type_name";
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

            String countSql = "SELECT COUNT(*) FROM user_types";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT type_name, typecode, attributes, methods " +
                    "FROM user_types ORDER BY type_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> getTriggerSynonyms() {
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
                            "    NULL as trigger_type, " +
                            "    NULL as triggering_event, " +
                            "    NULL as table_name, " +
                            "    NULL as description, " +
                            "    NULL as created, " +
                            "    NULL as last_ddl_time, " +
                            "    s.table_owner as target_owner, " +
                            "    s.table_name as target_name, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_TRIGGER' " +
                            "        ELSE o.object_type " +
                            "    END as target_type, " +
                            "    s.db_link " +
                            "FROM user_synonyms s " +
                            "LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                            "    AND s.table_name = o.object_name " +
                            "WHERE (o.object_type = 'TRIGGER' OR s.db_link IS NOT NULL) " +
                            "ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);

            for (Map<String, Object> syn : synonyms) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "syn-trig-" + System.currentTimeMillis() + "-" + syn.get("name"));
                transformed.put("name", syn.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                transformed.put("triggerType", null);
                transformed.put("triggeringEvent", null);
                transformed.put("tableName", null);
                transformed.put("description", null);
                transformed.put("created", null);
                transformed.put("lastModified", null);
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "TRIGGER");
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);
                transformed.put("isSynonym", true);
                transformed.put("icon", "synonym");
                transformed.put("targetIcon", "trigger");

                String targetType = (String) syn.get("target_type");
                if (targetType != null) {
                    transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                } else {
                    transformed.put("targetDisplayType", "Trigger");
                }

                if (syn.get("db_link") == null && syn.get("target_name") != null) {
                    try {
                        String targetSql = "SELECT status, created, last_ddl_time " +
                                "FROM all_objects " +
                                "WHERE owner = ? AND object_name = ? AND object_type = 'TRIGGER'";
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
            log.error("Error fetching synonyms for triggers: {}", e.getMessage());
        }
        return result;
    }

    private void addTriggerSynonymsPaginated(List<Map<String, Object>> allItems, int page, int pageSize, int triggerCount) {
        try {
            int offset = (page - 1) * pageSize;
            int synOffset;
            int synLimit;

            if (offset >= triggerCount) {
                synOffset = offset - triggerCount;
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
                                "      NULL as trigger_type, " +
                                "      NULL as triggering_event, " +
                                "      NULL as table_name, " +
                                "      NULL as description, " +
                                "      NULL as created, " +
                                "      NULL as last_ddl_time, " +
                                "      s.table_owner as target_owner, " +
                                "      s.table_name as target_name, " +
                                "      CASE " +
                                "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_TRIGGER' " +
                                "        ELSE o.object_type " +
                                "      END as target_type, " +
                                "      s.db_link " +
                                "    FROM user_synonyms s " +
                                "    LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                                "        AND s.table_name = o.object_name " +
                                "    WHERE (o.object_type = 'TRIGGER' OR s.db_link IS NOT NULL) " +
                                "    ORDER BY s.synonym_name " +
                                "  ) a " +
                                ") WHERE rnum > ? AND rnum <= ?";

                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(
                        synonymSql, synOffset, synOffset + synLimit);
                allItems.addAll(synonyms);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for triggers: {}", e.getMessage());
        }
    }

    private List<Map<String, Object>> transformTriggerItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("TRIGGER".equals(type)) {
                transformedItem.put("id", "trigger-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
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
            } else {
                transformedItem.put("id", "syn-trig-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "SYNONYM");
                transformedItem.put("status", item.get("status") != null ? item.get("status") : "VALID");
                transformedItem.put("triggerType", null);
                transformedItem.put("triggeringEvent", null);
                transformedItem.put("tableName", null);
                transformedItem.put("description", null);
                transformedItem.put("created", null);
                transformedItem.put("lastModified", null);
                transformedItem.put("targetOwner", item.get("target_owner"));
                transformedItem.put("targetName", item.get("target_name"));
                transformedItem.put("targetType", item.get("target_type") != null ? item.get("target_type") : "TRIGGER");
                transformedItem.put("dbLink", item.get("db_link"));
                transformedItem.put("isRemote", item.get("db_link") != null);
                transformedItem.put("isSynonym", true);
                transformedItem.put("icon", "synonym");
                transformedItem.put("targetIcon", "trigger");

                String targetType = (String) item.get("target_type");
                if (targetType != null) {
                    transformedItem.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                } else {
                    transformedItem.put("targetDisplayType", "Trigger");
                }
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }

    public List<Map<String, Object>> getAllSynonymsWithDetails() {
        try {
            String sql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'DATABASE_LINK' " +
                    "        ELSE (SELECT object_type FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_type, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                    "        ELSE (SELECT status FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_status, " +
                    "    (SELECT created FROM all_objects " +
                    "     WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) as target_created, " +
                    "    (SELECT last_ddl_time FROM all_objects " +
                    "     WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) as target_modified " +
                    "FROM user_synonyms s " +
                    "ORDER BY s.synonym_name";

            log.debug("Executing query for synonyms with target details");
            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSynonymsWithDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve synonyms with details: " + e.getMessage(), e);
        }
    }



    private Map<String, Object> getBasicObjectInfo(String owner, String objectName, String objectType) {
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


    /**
     * Get database link details
     */
    public Map<String, Object> getDatabaseLinkDetails(String owner, String dbLinkName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    db_link, " +
                        "    username, " +
                        "    host, " +
                        "    created " +
                        "FROM user_db_links " +
                        "WHERE UPPER(db_link) = UPPER(?)";

                Map<String, Object> linkInfo = getJdbcTemplate().queryForMap(sql, dbLinkName);
                details.putAll(linkInfo);

            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    db_link, " +
                        "    username, " +
                        "    host, " +
                        "    created " +
                        "FROM all_db_links " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(db_link) = UPPER(?)";

                Map<String, Object> linkInfo = getJdbcTemplate().queryForMap(sql, owner, dbLinkName);
                details.putAll(linkInfo);
            }

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


    /**
     * Get materialized view details
     */
    public Map<String, Object> getMaterializedViewDetails(String owner, String mvName) {
        Map<String, Object> details = new HashMap<>();
        try {
            String sql;
            if (owner.equalsIgnoreCase(getCurrentUser())) {
                sql = "SELECT " +
                        "    mview_name, " +
                        "    container_name, " +
                        "    query, " +
                        "    refresh_method, " +
                        "    refresh_mode, " +
                        "    build_mode, " +
                        "    fast_refreshable, " +
                        "    last_refresh_type, " +
                        "    last_refresh_date, " +
                        "    staleness, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status " +
                        "FROM user_mviews m " +
                        "JOIN user_objects o ON m.mview_name = o.object_name AND o.object_type = 'MATERIALIZED VIEW' " +
                        "WHERE UPPER(m.mview_name) = UPPER(?)";

                Map<String, Object> mvInfo = getJdbcTemplate().queryForMap(sql, mvName);
                details.putAll(mvInfo);

                // Get columns
                String colSql = "SELECT column_name, data_type, nullable " +
                        "FROM user_tab_columns WHERE table_name = ? ORDER BY column_id";

                List<Map<String, Object>> columns = getJdbcTemplate().queryForList(colSql, mvName);
                details.put("columns", columns);

            } else {
                sql = "SELECT " +
                        "    owner, " +
                        "    mview_name, " +
                        "    container_name, " +
                        "    refresh_method, " +
                        "    refresh_mode, " +
                        "    build_mode, " +
                        "    fast_refreshable, " +
                        "    last_refresh_type, " +
                        "    last_refresh_date, " +
                        "    staleness, " +
                        "    o.created, " +
                        "    o.last_ddl_time, " +
                        "    o.status " +
                        "FROM all_mviews m " +
                        "JOIN all_objects o ON m.owner = o.owner AND m.mview_name = o.object_name AND o.object_type = 'MATERIALIZED VIEW' " +
                        "WHERE UPPER(m.owner) = UPPER(?) AND UPPER(m.mview_name) = UPPER(?)";

                Map<String, Object> mvInfo = getJdbcTemplate().queryForMap(sql, owner, mvName);
                details.putAll(mvInfo);
            }

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



    public Map<String, Object> getSynonymsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            // Fast count query
            String countSql = "SELECT COUNT(*) FROM user_synonyms";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            // Enhanced query that resolves target types
            String dataSql = "SELECT " +
                    "    s.synonym_name, " +
                    "    s.table_owner as target_owner, " +
                    "    s.table_name as target_name, " +
                    "    s.db_link, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'DATABASE_LINK' " +
                    "        ELSE (SELECT object_type FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_type, " +
                    "    CASE " +
                    "        WHEN s.db_link IS NOT NULL THEN 'REMOTE' " +
                    "        ELSE (SELECT status FROM all_objects " +
                    "              WHERE owner = s.table_owner AND object_name = s.table_name AND ROWNUM = 1) " +
                    "    END as target_status " +
                    "FROM user_synonyms s " +
                    "ORDER BY s.synonym_name " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            // Transform to frontend format with actual target types
            List<Map<String, Object>> transformed = synonyms.stream().map(syn -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", "syn-" + System.currentTimeMillis() + "-" + syn.get("synonym_name"));
                item.put("name", syn.get("synonym_name"));
                item.put("owner", getCurrentUser());
                item.put("type", "SYNONYM");
                item.put("targetOwner", syn.get("target_owner"));
                item.put("targetName", syn.get("target_name"));
                item.put("dbLink", syn.get("db_link"));
                item.put("isRemote", syn.get("db_link") != null);

                // Get actual target type from the query
                String targetType = (String) syn.get("target_type");
                item.put("targetType", targetType != null ? targetType : "UNKNOWN");

                // Add target status if available
                if (syn.get("target_status") != null) {
                    item.put("targetStatus", syn.get("target_status"));
                }

                // Add icon based on target type
                if (targetType != null) {
                    item.put("targetIcon", getObjectTypeIcon(targetType));
                    item.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                }

                return item;
            }).collect(Collectors.toList());

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getSynonymsPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
    }



    public List<Map<String, Object>> getAllSequences() {
        try {
            String sql = "SELECT " +
                    "    sequence_name, " +
                    "    min_value, " +
                    "    max_value, " +
                    "    increment_by, " +
                    "    cycle_flag, " +
                    "    order_flag, " +
                    "    cache_size, " +
                    "    last_number " +
                    "FROM user_sequences " +
                    "ORDER BY sequence_name";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSequences: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve sequences: " + e.getMessage(), e);
        }
    }


    public List<Map<String, Object>> getAllTypes() {
        try {
            String sql = "SELECT " +
                    "    t.type_name, " +
                    "    t.typecode, " +
                    "    t.attributes, " +
                    "    t.methods, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM user_types t " +
                    "JOIN user_objects o ON t.type_name = o.object_name AND o.object_type LIKE '%TYPE' " +
                    "ORDER BY t.type_name";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTypes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve types: " + e.getMessage(), e);
        }
    }



    public List<Map<String, Object>> getAllDbLinks() {
        try {
            String sql = "SELECT " +
                    "    db_link, " +
                    "    username, " +
                    "    host, " +
                    "    created " +
                    "FROM user_db_links " +
                    "ORDER BY db_link";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllDbLinks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links: " + e.getMessage(), e);
        }
    }


    public List<Map<String, Object>> getAllSynonyms() {
        try {
            String sql = "SELECT " +
                    "    synonym_name, " +
                    "    table_owner, " +
                    "    table_name, " +
                    "    db_link " +
                    "FROM user_synonyms " +
                    "ORDER BY synonym_name";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllSynonyms: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve synonyms: " + e.getMessage(), e);
        }
    }



    public List<Map<String, Object>> getAllTriggers() {
        try {
            String sql = "SELECT " +
                    "    t.trigger_name, " +
                    "    t.trigger_type, " +
                    "    t.triggering_event, " +
                    "    t.table_name, " +
                    "    t.status as trigger_status, " +
                    "    t.description, " +
                    "    t.trigger_body, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status as object_status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary " +
                    "FROM user_triggers t " +
                    "JOIN user_objects o ON t.trigger_name = o.object_name AND o.object_type = 'TRIGGER' " +
                    "ORDER BY t.trigger_name";

            return getJdbcTemplate().queryForList(sql);

        } catch (Exception e) {
            log.error("Error in getAllTriggers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve triggers: " + e.getMessage(), e);
        }
    }


    public List<Map<String, Object>> getDbLinksBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    owner, " +
                    "    db_link, " +
                    "    username, " +
                    "    host, " +
                    "    created " +
                    "FROM all_db_links " +
                    "WHERE UPPER(owner) = UPPER(?) " +
                    "ORDER BY db_link";

            return getJdbcTemplate().queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getDbLinksBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve database links for schema: " + e.getMessage(), e);
        }
    }


    public List<Map<String, Object>> getFunctionsBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as function_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary, " +
                    "    (SELECT COUNT(*) FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL AND argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM all_objects o " +
                    "WHERE o.object_type = 'FUNCTION' AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name";

            return getJdbcTemplate().queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getFunctionsBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve functions for schema: " + e.getMessage(), e);
        }
    }


    public List<Map<String, Object>> getSynonymsByTargetType(String targetType) {
        try {
            List<Map<String, Object>> allSynonyms = getAllSynonymsWithDetails();

            return allSynonyms.stream()
                    .filter(syn -> targetType.equalsIgnoreCase((String) syn.get("target_type")))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in getSynonymsByTargetType: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}