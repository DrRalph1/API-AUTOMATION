package com.usg.autoAPIGenerator.repositories.schemaBrowser.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class OracleProcedureRepository extends OracleRepository {

    // ============================================================
    // PROCEDURE METHODS - ORIGINAL
    // ============================================================

    public List<Map<String, Object>> getAllProceduresForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String procedureSql = "SELECT " +
                    "    object_name as name, " +
                    "    'PROCEDURE' as type, " +
                    "    status, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    (SELECT COUNT(*) FROM user_arguments " +
                    "     WHERE object_name = o.object_name " +
                    "       AND package_name IS NULL " +
                    "       AND argument_name IS NOT NULL) as parameter_count, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'PROCEDURE' " +
                    "ORDER BY object_name";

            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql);

            for (Map<String, Object> proc : procedures) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "procedure-" + System.currentTimeMillis() + "-" + proc.get("name"));
                transformed.put("name", proc.get("name"));
                transformed.put("procedure_name", proc.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "PROCEDURE");
                transformed.put("object_type", "PROCEDURE");
                transformed.put("status", proc.get("status"));
                transformed.put("parameterCount", proc.get("parameter_count") != null ? proc.get("parameter_count") : 0);
                transformed.put("created", proc.get("created"));
                transformed.put("lastModified", proc.get("last_ddl_time"));
                transformed.put("last_ddl_time", proc.get("last_ddl_time"));
                transformed.put("icon", "procedure");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            result.addAll(getProcedureSynonyms());
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (procedures + synonyms)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllProceduresForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    public Map<String, Object> getAllProceduresForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String procedureCountSql = "SELECT COUNT(*) FROM user_objects WHERE object_type = 'PROCEDURE'";
            int procedureCount = getJdbcTemplate().queryForObject(procedureCountSql, Integer.class);

            String synonymCountSql =
                    "SELECT COUNT(*) FROM user_synonyms s " +
                            "WHERE EXISTS (SELECT 1 FROM all_objects " +
                            "              WHERE owner = s.table_owner " +
                            "                AND object_name = s.table_name " +
                            "                AND object_type = 'PROCEDURE') " +
                            "   OR (s.db_link IS NOT NULL)";

            int synonymCount = 0;
            try {
                synonymCount = getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
            } catch (Exception e) {
                log.warn("Error counting synonyms for procedures: {}", e.getMessage());
            }

            int totalCount = procedureCount + synonymCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (procedureCount > 0 && offset < procedureCount) {
                int procOffset = offset;
                int procLimit = Math.min(pageSize, procedureCount - procOffset);

                if (procLimit > 0) {
                    String procedureSql = "SELECT " +
                            "    object_name as name, " +
                            "    'PROCEDURE' as type, " +
                            "    status, " +
                            "    created, " +
                            "    last_ddl_time, " +
                            "    (SELECT COUNT(*) FROM user_arguments " +
                            "     WHERE object_name = o.object_name " +
                            "       AND package_name IS NULL " +
                            "       AND argument_name IS NOT NULL) as parameter_count, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM user_objects o " +
                            "WHERE object_type = 'PROCEDURE' " +
                            "ORDER BY object_name " +
                            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql, procOffset, procLimit);
                    allItems.addAll(procedures);
                }
            }

            if (allItems.size() < pageSize && synonymCount > 0) {
                addProcedureSynonymsPaginated(allItems, page, pageSize, procedureCount);
            }

            List<Map<String, Object>> transformed = transformProcedureItems(allItems);
            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getAllProceduresForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }




    /**
     * Get procedure details with owner - FIXED version
     */
    public Map<String, Object> getProcedureDetails(String owner, String procedureName) {
        Map<String, Object> details = new HashMap<>();

        try {
            // First check if this is a package procedure
            String checkPackageSql = "SELECT DISTINCT package_name FROM all_arguments " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND package_name IS NOT NULL";

            List<String> packages = getJdbcTemplate().queryForList(
                    checkPackageSql, String.class, owner, procedureName);

            boolean isPackageProcedure = !packages.isEmpty();

            if (isPackageProcedure) {
                String packageName = packages.get(0);
                details.put("package_name", packageName);
                details.put("is_package_procedure", true);
                details.put("object_name", procedureName);
                details.put("owner", owner);
                details.put("object_type", "PROCEDURE");
                log.info("Procedure {} is in package: {}", procedureName, packageName);
            } else {
                // Get standalone procedure info
                try {
                    String sql = "SELECT " +
                            "    object_name, " +
                            "    object_type, " +
                            "    status, " +
                            "    created, " +
                            "    last_ddl_time, " +
                            "    temporary, " +
                            "    generated, " +
                            "    secondary " +
                            "FROM all_objects " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND object_type = 'PROCEDURE'";

                    Map<String, Object> procInfo = getJdbcTemplate().queryForMap(sql, owner, procedureName);
                    details.putAll(procInfo);
                } catch (EmptyResultDataAccessException e) {
                    // Try without object type filter
                    String altSql = "SELECT object_name, object_type, status, created, last_ddl_time " +
                            "FROM all_objects WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";
                    Map<String, Object> procInfo = getJdbcTemplate().queryForMap(altSql, owner, procedureName);
                    details.putAll(procInfo);
                }
            }

            // Try to get parameters from database first
            List<Map<String, Object>> params = getProcedureParametersFromDatabase(owner, procedureName, isPackageProcedure, packages);

            // If database parameters are empty or suspicious (like only 1 parameter when there should be many),
            // try parsing from source
            if (params.isEmpty() || (params.size() == 1 && !isLikelyCorrectParameterCount(params, procedureName))) {
                log.info("Database parameters insufficient for {}.{}, trying enhanced source parsing",
                        owner, procedureName);
                params = parseProcedureParametersEnhanced(owner, procedureName);
            }

            details.put("parameters", params);
            details.put("parameterCount", params.size());

            // Calculate parameter counts by type
            int inCount = 0;
            int outCount = 0;
            int inOutCount = 0;

            for (Map<String, Object> param : params) {
                String inOut = (String) param.get("in_out");
                if (inOut == null) inOut = (String) param.get("IN_OUT");

                if (inOut != null) {
                    String upperInOut = inOut.toUpperCase();
                    if (upperInOut.contains("IN") && upperInOut.contains("OUT")) {
                        inOutCount++;
                    } else if (upperInOut.contains("OUT")) {
                        outCount++;
                    } else if (upperInOut.contains("IN")) {
                        inCount++;
                    }
                }
            }

            details.put("inParameterCount", inCount);
            details.put("outParameterCount", outCount);
            details.put("inOutParameterCount", inOutCount);

            // Get source code
            String source = getProcedureSource(owner, procedureName, isPackageProcedure, packages);
            if (source != null) {
                details.put("sourceCode", source);
            }

        } catch (Exception e) {
            log.warn("Error getting procedure details for {}.{}: {}", owner, procedureName, e.getMessage());
            details.put("error", e.getMessage());
        }

        return details;
    }




    public Map<String, Object> getProcedureDetails(String procedureName) {
        try {
            Map<String, Object> procLocation = findObjectLocation(procedureName, "PROCEDURE");
            String owner = (String) procLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
            }

            return getProcedureDetails(owner, procedureName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Procedure {} not found", procedureName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("procedure_name", procedureName);
            emptyResult.put("message", "Procedure not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getProcedureDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedure details: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProcedureParametersPaginated(String procedureName, String owner,
                                                               int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentUser();
            }

            int offset = (page - 1) * pageSize;

            // First, check if this is a synonym and resolve it
            Map<String, Object> resolved = resolveSynonymIfNeeded(procedureName, owner);

            String actualOwner;
            String actualProcedureName;
            boolean isSynonym = false;

            if (resolved != null && !resolved.isEmpty()) {
                actualOwner = (String) resolved.get("target_owner");
                actualProcedureName = (String) resolved.get("target_name");
                isSynonym = true;
                log.info("Resolved synonym {} to target {}.{}", procedureName, actualOwner, actualProcedureName);
            } else {
                actualOwner = owner;
                actualProcedureName = procedureName;
            }

            // Try to get parameters from database first
            String countSql = "SELECT COUNT(*) FROM all_arguments " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                    "AND package_name IS NULL AND argument_name IS NOT NULL";

            int totalCount = 0;
            List<Map<String, Object>> parameters = new ArrayList<>();
            boolean fromSource = false;

            try {
                totalCount = getJdbcTemplate().queryForObject(
                        countSql, Integer.class, actualOwner, actualProcedureName);

                if (totalCount > 0) {
                    // Get paginated parameters from database
                    String paramSql = "SELECT " +
                            "    argument_name, " +
                            "    position, " +
                            "    sequence, " +
                            "    data_type, " +
                            "    in_out, " +
                            "    data_length, " +
                            "    data_precision, " +
                            "    data_scale, " +
                            "    defaulted " +
                            "FROM all_arguments " +
                            "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                            "AND package_name IS NULL AND argument_name IS NOT NULL " +
                            "ORDER BY position, sequence " +
                            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    parameters = getJdbcTemplate().queryForList(
                            paramSql, actualOwner, actualProcedureName, offset, pageSize);
                }
            } catch (Exception e) {
                log.debug("Error querying all_arguments for {}.{}: {}",
                        actualOwner, actualProcedureName, e.getMessage());
            }

            // If no parameters found in database, try parsing from source
            if (parameters.isEmpty()) {
                log.info("No parameters found in database for {}.{}, attempting to parse from source",
                        actualOwner, actualProcedureName);

                List<Map<String, Object>> allParams = parseProcedureParametersFromSource(
                        actualOwner, actualProcedureName);

                totalCount = allParams.size();

                // Apply pagination to the parsed parameters
                if (!allParams.isEmpty() && offset < allParams.size()) {
                    int endIndex = Math.min(offset + pageSize, allParams.size());
                    parameters = allParams.subList(offset, endIndex);
                }

                fromSource = true;
                log.info("Parsed {} parameters from source for {}.{}",
                        totalCount, actualOwner, actualProcedureName);
            }

            result.put("items", parameters);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            if (isSynonym) {
                result.put("isSynonym", true);
                result.put("originalOwner", owner);
                result.put("originalName", procedureName);
                result.put("resolvedOwner", actualOwner);
                result.put("resolvedName", actualProcedureName);
            }

            if (fromSource) {
                result.put("fromSource", true);
                result.put("message", "Parameters parsed from source code");
            }

            return result;

        } catch (Exception e) {
            log.error("Error in getProcedureParametersPaginated for {}.{}: {}",
                    owner, procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedure parameters: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProceduresPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String procedureCountSql = "SELECT COUNT(*) FROM user_objects WHERE object_type = 'PROCEDURE'";
            int procedureCount = getJdbcTemplate().queryForObject(procedureCountSql, Integer.class);

            String synonymCountSql =
                    "SELECT COUNT(*) FROM user_synonyms s " +
                            "WHERE EXISTS (SELECT 1 FROM all_objects " +
                            "              WHERE owner = s.table_owner " +
                            "                AND object_name = s.table_name " +
                            "                AND object_type = 'PROCEDURE') " +
                            "   OR (s.db_link IS NOT NULL)";

            int synonymCount = 0;
            try {
                synonymCount = getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
            } catch (Exception e) {
                log.warn("Error counting synonyms: {}", e.getMessage());
                synonymCount = 0;
            }

            int totalCount = procedureCount + synonymCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (procedureCount > 0 && offset < procedureCount) {
                int procOffset = offset;
                int procLimit = Math.min(pageSize, procedureCount - procOffset);

                if (procLimit > 0) {
                    String procedureSql =
                            "SELECT " +
                                    "    object_name as name, " +
                                    "    'PROCEDURE' as type, " +
                                    "    status, " +
                                    "    created, " +
                                    "    last_ddl_time, " +
                                    "    NULL as target_owner, " +
                                    "    NULL as target_name, " +
                                    "    NULL as target_type, " +
                                    "    NULL as db_link " +
                                    "FROM user_objects " +
                                    "WHERE object_type = 'PROCEDURE' " +
                                    "ORDER BY object_name " +
                                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(
                            procedureSql, procOffset, procLimit);
                    allItems.addAll(procedures);
                }
            }

            if (allItems.size() < pageSize && synonymCount > 0) {
                addProcedureSynonymsPaginated(allItems, page, pageSize, procedureCount);
            }

            List<Map<String, Object>> transformed = transformProcedureItems(allItems);
            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getProceduresPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }

    public List<Map<String, Object>> getProceduresBySchema(String schemaName) {
        try {
            String sql = "SELECT " +
                    "    o.owner, " +
                    "    o.object_name as procedure_name, " +
                    "    o.object_type, " +
                    "    o.created, " +
                    "    o.last_ddl_time, " +
                    "    o.status, " +
                    "    o.temporary, " +
                    "    o.generated, " +
                    "    o.secondary, " +
                    "    (SELECT COUNT(*) FROM all_arguments WHERE owner = o.owner AND object_name = o.object_name AND package_name IS NULL AND argument_name IS NOT NULL) as parameter_count " +
                    "FROM all_objects o " +
                    "WHERE o.object_type = 'PROCEDURE' AND UPPER(o.owner) = UPPER(?) " +
                    "ORDER BY o.object_name";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getProceduresBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> getProcedureSynonyms() {
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
                            "    0 as parameter_count, " +
                            "    s.table_owner as target_owner, " +
                            "    s.table_name as target_name, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_PROCEDURE' " +
                            "        ELSE o.object_type " +
                            "    END as target_type, " +
                            "    s.db_link " +
                            "FROM user_synonyms s " +
                            "LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                            "    AND s.table_name = o.object_name " +
                            "WHERE (o.object_type = 'PROCEDURE' OR s.db_link IS NOT NULL) " +
                            "ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);

            for (Map<String, Object> syn : synonyms) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "syn-proc-" + System.currentTimeMillis() + "-" + syn.get("name"));
                transformed.put("name", syn.get("name"));
                transformed.put("procedure_name", syn.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("object_type", "SYNONYM");
                transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                transformed.put("parameterCount", 0);
                transformed.put("created", null);
                transformed.put("lastModified", null);
                transformed.put("last_ddl_time", null);
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "PROCEDURE");
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);
                transformed.put("isSynonym", true);
                transformed.put("icon", "synonym");
                transformed.put("targetIcon", "procedure");

                String targetType = (String) syn.get("target_type");
                if (targetType != null) {
                    transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                } else {
                    transformed.put("targetDisplayType", "Procedure");
                }

                if (syn.get("db_link") == null && syn.get("target_name") != null) {
                    try {
                        String targetSql = "SELECT status, created, last_ddl_time " +
                                "FROM all_objects " +
                                "WHERE owner = ? AND object_name = ? AND object_type = 'PROCEDURE'";
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
            log.error("Error fetching synonyms for procedures: {}", e.getMessage());
        }
        return result;
    }

    private List<Map<String, Object>> getProcedureParametersFromDatabase(String owner, String procedureName,
                                                                         boolean isPackageProcedure, List<String> packages) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            if (isPackageProcedure && !packages.isEmpty()) {
                String packageName = packages.get(0);
                String paramSql = "SELECT " +
                        "    argument_name, " +
                        "    position, " +
                        "    sequence, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    defaulted, " +
                        "    package_name " +
                        "FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) " +
                        "  AND UPPER(package_name) = UPPER(?) " +
                        "  AND UPPER(object_name) = UPPER(?) " +
                        "  AND argument_name IS NOT NULL " +
                        "ORDER BY position, sequence";

                params = getJdbcTemplate().queryForList(paramSql, owner, packageName, procedureName);
            } else {
                String paramSql = "SELECT " +
                        "    argument_name, " +
                        "    position, " +
                        "    sequence, " +
                        "    data_type, " +
                        "    in_out, " +
                        "    data_length, " +
                        "    data_precision, " +
                        "    data_scale, " +
                        "    defaulted " +
                        "FROM all_arguments " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL " +
                        "AND argument_name IS NOT NULL " +
                        "ORDER BY position, sequence";

                params = getJdbcTemplate().queryForList(paramSql, owner, procedureName);
            }
        } catch (Exception e) {
            log.debug("Error getting database parameters: {}", e.getMessage());
        }

        return params;
    }

    private List<Map<String, Object>> parseProcedureParametersEnhanced(String owner, String procedureName) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String sourceSql = "SELECT text FROM all_source " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                    "AND UPPER(type) IN ('PROCEDURE', 'PACKAGE', 'PACKAGE BODY') " +
                    "ORDER BY line";

            List<String> sourceLines = getJdbcTemplate().queryForList(sourceSql, String.class, owner, procedureName);

            if (sourceLines.isEmpty()) {
                log.warn("No source found for {}.{}", owner, procedureName);
                return params;
            }

            StringBuilder fullSourceBuilder = new StringBuilder();
            for (String line : sourceLines) {
                fullSourceBuilder.append(line).append("\n");
            }
            String fullSource = fullSourceBuilder.toString();

            String sourceWithoutComments = removeComments(fullSource);

            String[] patterns = {
                    "PROCEDURE\\s+" + procedureName + "\\s*\\(([\\s\\S]*?)\\)\\s*(?:IS|AS)",
                    "PROCEDURE\\s+" + procedureName + "\\s*\\(([\\s\\S]*?)\\)",
                    "procedure\\s+" + procedureName + "\\s*\\(([\\s\\S]*?)\\)\\s*(?:is|as)",
                    procedureName + "\\s*\\(([\\s\\S]*?)\\)\\s*(?:IS|AS)"
            };

            String paramsSection = null;

            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(sourceWithoutComments);

                if (matcher.find()) {
                    paramsSection = matcher.group(1).trim();
                    log.info("Found parameter section using pattern: {}", patternStr);
                    break;
                }
            }

            if (paramsSection == null || paramsSection.isEmpty()) {
                log.warn("Could not find parameter section for {}", procedureName);
                return params;
            }

            log.info("Parameter section for {}: {}", procedureName, paramsSection);

            List<String> paramDeclarations = splitParameterDeclarations(paramsSection);

            int position = 1;
            for (String paramDecl : paramDeclarations) {
                Map<String, Object> param = parseParameterDeclarationEnhanced(paramDecl, position++);
                if (param != null && !param.isEmpty()) {
                    params.add(param);
                    log.debug("Parsed parameter: {}", param);
                }
            }

            log.info("Successfully parsed {} parameters for {}", params.size(), procedureName);

        } catch (Exception e) {
            log.error("Error parsing parameters for {}.{}: {}", owner, procedureName, e.getMessage(), e);
        }

        return params;
    }

    private Map<String, Object> parseParameterDeclarationEnhanced(String paramDecl, int position) {
        Map<String, Object> param = new HashMap<>();

        paramDecl = paramDecl.replaceAll("\\s+", " ").trim();

        String[] patterns = {
                "^(\\w+)\\s+(IN\\s+OUT|IN|OUT)\\s+(.+?)$",
                "^(\\w+)\\s+([^\\s]+(?:\\s*\\)[^\\s]*)?)$",
                "^(\\w+)\\s+(IN\\s+OUT|IN|OUT)\\s+(.+?)(?:\\s+(?:DEFAULT|:=)\\s+(.+))?$",
                "^(\\w+)\\s+([^\\s]+(?:\\s*\\()?[^)]*\\)?)(?:\\s+(?:DEFAULT|:=)\\s+(.+))?$"
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(paramDecl);

            if (matcher.find()) {
                String paramName = matcher.group(1);
                String dataType;
                String inOut = "IN";
                String defaultValue = null;

                if (patternStr.contains("IN\\s+OUT|IN|OUT")) {
                    inOut = matcher.group(2).toUpperCase().replace(" ", "_");
                    dataType = matcher.group(3).trim();
                    if (matcher.groupCount() >= 4) {
                        defaultValue = matcher.group(4);
                    }
                } else {
                    dataType = matcher.group(2).trim();
                    if (matcher.groupCount() >= 3) {
                        defaultValue = matcher.group(3);
                    }
                }

                dataType = dataType.replaceAll("[,;]$", "").trim();

                param.put("argument_name", paramName);
                param.put("position", position);
                param.put("sequence", position);
                param.put("data_type", dataType);
                param.put("in_out", inOut);
                param.put("data_length", extractDataTypeLength(dataType));
                param.put("data_precision", extractDataTypePrecision(dataType));
                param.put("data_scale", extractDataTypeScale(dataType));
                param.put("defaulted", defaultValue != null ? "Y" : "N");

                log.debug("Parsed: {} {} {}", paramName, inOut, dataType);
                break;
            }
        }

        return param.isEmpty() ? null : param;
    }

    private List<Map<String, Object>> parseProcedureParametersFromSource(String owner, String procedureName) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String sourceSql = "SELECT text FROM all_source " +
                    "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                    "AND UPPER(type) = 'PROCEDURE' ORDER BY line";

            List<String> sourceLines = getJdbcTemplate().queryForList(sourceSql, String.class, owner, procedureName);

            if (sourceLines.isEmpty()) {
                return params;
            }

            StringBuilder fullSourceBuilder = new StringBuilder();
            for (String line : sourceLines) {
                fullSourceBuilder.append(line).append(" ");
            }
            String fullSource = fullSourceBuilder.toString();

            String patternStr = "PROCEDURE\\s+" + procedureName + "\\s*\\((.*?)\\)\\s*(?:IS|AS)";
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(fullSource);

            if (matcher.find()) {
                String paramsSection = matcher.group(1).trim();
                paramsSection = removeComments(paramsSection);

                List<String> paramDeclarations = splitParametersByComma(paramsSection);

                int position = 1;
                for (String paramDecl : paramDeclarations) {
                    paramDecl = paramDecl.trim();
                    if (paramDecl.isEmpty()) {
                        continue;
                    }

                    Map<String, Object> param = parseParameterFromString(paramDecl, position++);
                    if (param != null && !param.isEmpty()) {
                        params.add(param);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error parsing parameters from source: {}", e.getMessage());
        }

        return params;
    }

    private Map<String, Object> parseParameterFromString(String paramDecl, int position) {
        Map<String, Object> param = new HashMap<>();

        paramDecl = paramDecl.replaceAll("--.*$", "").trim();

        Pattern paramPattern = Pattern.compile(
                "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s+" +
                        "(?:(IN|OUT|IN\\s+OUT)\\s+)?" +
                        "([a-zA-Z_][a-zA-Z0-9_]*" +
                        "(?:\\s*\\(" +
                        "[^)]*" +
                        "\\))?)" +
                        "(?:\\s+(?:DEFAULT|:=)\\s+(.+))?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = paramPattern.matcher(paramDecl);

        if (matcher.find()) {
            String paramName = matcher.group(1);
            String direction = matcher.group(2);
            String dataType = matcher.group(3);
            String defaultValue = matcher.group(4);

            dataType = dataType.trim().toUpperCase();

            param.put("argument_name", paramName);
            param.put("position", position);
            param.put("sequence", position);
            param.put("data_type", dataType);
            param.put("in_out", direction != null ? direction.toUpperCase().replace(" ", "_") : "IN");
            param.put("data_length", extractDataTypeLength(dataType));
            param.put("data_precision", extractDataTypePrecision(dataType));
            param.put("data_scale", extractDataTypeScale(dataType));
            param.put("defaulted", defaultValue != null ? "YES" : "NO");
        } else {
            Pattern simplePattern = Pattern.compile(
                    "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s+" +
                            "(?:IN|OUT|IN\\s+OUT)\\s+" +
                            "([a-zA-Z_][a-zA-Z0-9_]*" +
                            "(?:\\s*\\(" +
                            "[^)]*" +
                            "\\))?)",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher simpleMatcher = simplePattern.matcher(paramDecl);
            if (simpleMatcher.find()) {
                String paramName = simpleMatcher.group(1);
                String dataType = simpleMatcher.group(2);

                String direction = "IN";
                if (paramDecl.toUpperCase().contains("OUT")) {
                    if (paramDecl.toUpperCase().contains("IN OUT")) {
                        direction = "IN_OUT";
                    } else {
                        direction = "OUT";
                    }
                }

                param.put("argument_name", paramName);
                param.put("position", position);
                param.put("sequence", position);
                param.put("data_type", dataType.trim().toUpperCase());
                param.put("in_out", direction);
                param.put("data_length", null);
                param.put("data_precision", null);
                param.put("data_scale", null);
                param.put("defaulted", "NO");
            }
        }

        return param;
    }

    private boolean isLikelyCorrectParameterCount(List<Map<String, Object>> params, String procedureName) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        if (params.size() >= 2) {
            return true;
        }

        String upperName = procedureName.toUpperCase();
        if (upperName.contains("DETAILS") || upperName.contains("INFO") ||
                upperName.contains("DATA") || upperName.contains("POST") ||
                upperName.contains("INSERT") || upperName.contains("UPDATE")) {
            return false;
        }

        return true;
    }

    private String getProcedureSource(String owner, String procedureName,
                                      boolean isPackageProcedure, List<String> packages) {
        try {
            if (isPackageProcedure && !packages.isEmpty()) {
                String packageName = packages.get(0);
                String sourceSql = "SELECT text FROM all_source " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                        "AND UPPER(type) IN ('PACKAGE', 'PACKAGE BODY') " +
                        "ORDER BY line";

                List<String> sourceLines = getJdbcTemplate().queryForList(sourceSql, String.class, owner, packageName);
                if (!sourceLines.isEmpty()) {
                    return String.join("", sourceLines);
                }
            } else {
                String sourceSql = "SELECT text FROM all_source " +
                        "WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                        "AND UPPER(type) IN ('PROCEDURE', 'PACKAGE', 'PACKAGE BODY') " +
                        "ORDER BY line";

                List<String> sourceLines = getJdbcTemplate().queryForList(sourceSql, String.class, owner, procedureName);
                if (!sourceLines.isEmpty()) {
                    return String.join("", sourceLines);
                }
            }
        } catch (Exception e) {
            log.debug("Error getting source: {}", e.getMessage());
        }

        return null;
    }

    private void addProcedureSynonymsPaginated(List<Map<String, Object>> allItems, int page, int pageSize, int procedureCount) {
        try {
            int offset = (page - 1) * pageSize;
            int synOffset;
            int synLimit;

            if (offset >= procedureCount) {
                synOffset = offset - procedureCount;
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
                                "      NULL as created, " +
                                "      NULL as last_ddl_time, " +
                                "      0 as parameter_count, " +
                                "      s.table_owner as target_owner, " +
                                "      s.table_name as target_name, " +
                                "      CASE " +
                                "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_PROCEDURE' " +
                                "        ELSE o.object_type " +
                                "      END as target_type, " +
                                "      s.db_link " +
                                "    FROM user_synonyms s " +
                                "    LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                                "        AND s.table_name = o.object_name " +
                                "    WHERE (o.object_type = 'PROCEDURE' OR s.db_link IS NOT NULL) " +
                                "    ORDER BY s.synonym_name " +
                                "  ) a " +
                                ") WHERE rnum > ? AND rnum <= ?";

                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(
                        synonymSql, synOffset, synOffset + synLimit);
                allItems.addAll(synonyms);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for procedures: {}", e.getMessage());
        }
    }

    private List<Map<String, Object>> transformProcedureItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("PROCEDURE".equals(type)) {
                transformedItem.put("id", "procedure-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("procedure_name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "PROCEDURE");
                transformedItem.put("status", item.get("status"));
                transformedItem.put("parameterCount", item.get("parameter_count") != null ? item.get("parameter_count") : 0);
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("last_ddl_time"));
                transformedItem.put("icon", "procedure");
                transformedItem.put("isSynonym", false);
            } else {
                transformedItem.put("id", "syn-proc-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("procedure_name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "SYNONYM");
                transformedItem.put("status", item.get("status") != null ? item.get("status") : "VALID");
                transformedItem.put("parameterCount", 0);
                transformedItem.put("created", null);
                transformedItem.put("lastModified", null);
                transformedItem.put("targetOwner", item.get("target_owner"));
                transformedItem.put("targetName", item.get("target_name"));
                transformedItem.put("targetType", item.get("target_type") != null ? item.get("target_type") : "PROCEDURE");
                transformedItem.put("dbLink", item.get("db_link"));
                transformedItem.put("isRemote", item.get("db_link") != null);
                transformedItem.put("isSynonym", true);
                transformedItem.put("icon", "synonym");
                transformedItem.put("targetIcon", "procedure");

                String targetType = (String) item.get("target_type");
                if (targetType != null) {
                    transformedItem.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                } else {
                    transformedItem.put("targetDisplayType", "Procedure");
                }
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }



    public List<Map<String, Object>> getAllProcedures() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // Get actual procedures
            String procedureSql =
                    "SELECT " +
                            "    object_name as name, " +
                            "    'PROCEDURE' as type, " +
                            "    status, " +
                            "    created, " +
                            "    last_ddl_time, " +
                            "    temporary, " +
                            "    generated, " +
                            "    secondary, " +
                            "    (SELECT COUNT(*) FROM user_arguments " +
                            "     WHERE object_name = o.object_name " +
                            "       AND package_name IS NULL " +
                            "       AND argument_name IS NOT NULL) as parameter_count, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM user_objects o " +
                            "WHERE object_type = 'PROCEDURE' " +
                            "ORDER BY object_name";

            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql);
            log.info("Found {} procedures", procedures.size());

            // Transform procedures to frontend format
            for (Map<String, Object> proc : procedures) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "proc-" + System.currentTimeMillis() + "-" + proc.get("name"));
                transformed.put("name", proc.get("name"));
                transformed.put("procedure_name", proc.get("name")); // Keep original field name for backward compatibility
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "PROCEDURE");
                transformed.put("object_type", "PROCEDURE");
                transformed.put("status", proc.get("status"));
                transformed.put("created", proc.get("created"));
                transformed.put("lastModified", proc.get("last_ddl_time"));
                transformed.put("last_ddl_time", proc.get("last_ddl_time"));
                transformed.put("temporary", proc.get("temporary"));
                transformed.put("generated", proc.get("generated"));
                transformed.put("secondary", proc.get("secondary"));
                transformed.put("parameterCount", proc.get("parameter_count") != null ? proc.get("parameter_count") : 0);
                transformed.put("icon", "procedure");
                transformed.put("isSynonym", false);

                result.add(transformed);
            }

            // Get synonyms that target procedures
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
                            "    NULL as temporary, " +
                            "    NULL as generated, " +
                            "    NULL as secondary, " +
                            "    0 as parameter_count, " +
                            "    s.table_owner as target_owner, " +
                            "    s.table_name as target_name, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_PROCEDURE' " +
                            "        ELSE o.object_type " +
                            "    END as target_type, " +
                            "    s.db_link " +
                            "FROM user_synonyms s " +
                            "LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                            "    AND s.table_name = o.object_name " +
                            "WHERE (o.object_type = 'PROCEDURE' OR s.db_link IS NOT NULL) " +
                            "ORDER BY s.synonym_name";

            try {
                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);
                log.info("Found {} synonyms targeting procedures", synonyms.size());

                // Transform synonyms to frontend format
                for (Map<String, Object> syn : synonyms) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "syn-" + System.currentTimeMillis() + "-" + syn.get("name"));
                    transformed.put("name", syn.get("name"));
                    transformed.put("procedure_name", syn.get("name")); // Keep for backward compatibility
                    transformed.put("owner", getCurrentUser());
                    transformed.put("type", "SYNONYM");
                    transformed.put("object_type", "SYNONYM");
                    transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                    transformed.put("created", null);
                    transformed.put("lastModified", null);
                    transformed.put("last_ddl_time", null);
                    transformed.put("temporary", "N");
                    transformed.put("generated", "N");
                    transformed.put("secondary", "N");
                    transformed.put("parameterCount", 0);
                    transformed.put("targetOwner", syn.get("target_owner"));
                    transformed.put("targetName", syn.get("target_name"));
                    transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "PROCEDURE");
                    transformed.put("dbLink", syn.get("db_link"));
                    transformed.put("isRemote", syn.get("db_link") != null);
                    transformed.put("isSynonym", true);
                    transformed.put("icon", "synonym");
                    transformed.put("targetIcon", "procedure");

                    // For display purposes
                    String targetType = (String) syn.get("target_type");
                    if (targetType != null) {
                        transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                    } else {
                        transformed.put("targetDisplayType", "Procedure");
                    }

                    // Get target procedure info if accessible
                    if (syn.get("db_link") == null && syn.get("target_name") != null) {
                        try {
                            String targetSql = "SELECT status, created, last_ddl_time " +
                                    "FROM all_objects " +
                                    "WHERE owner = ? AND object_name = ? AND object_type = 'PROCEDURE'";
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
                log.error("Error fetching synonyms for procedures: {}", e.getMessage());

                // Fallback to simpler query if the complex one fails
                try {
                    String simpleSynonymSql =
                            "SELECT " +
                                    "    synonym_name as name, " +
                                    "    'SYNONYM' as type, " +
                                    "    'VALID' as status, " +
                                    "    NULL as created, " +
                                    "    NULL as last_ddl_time, " +
                                    "    NULL as temporary, " +
                                    "    NULL as generated, " +
                                    "    NULL as secondary, " +
                                    "    0 as parameter_count, " +
                                    "    table_owner as target_owner, " +
                                    "    table_name as target_name, " +
                                    "    'PROCEDURE' as target_type, " +
                                    "    db_link " +
                                    "FROM user_synonyms s " +
                                    "WHERE db_link IS NULL " +
                                    "ORDER BY synonym_name";

                    List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(simpleSynonymSql);
                    log.info("Found {} synonyms from fallback query", synonyms.size());

                    for (Map<String, Object> syn : synonyms) {
                        Map<String, Object> transformed = new HashMap<>();
                        transformed.put("id", "syn-" + System.currentTimeMillis() + "-" + syn.get("name"));
                        transformed.put("name", syn.get("name"));
                        transformed.put("procedure_name", syn.get("name"));
                        transformed.put("owner", getCurrentUser());
                        transformed.put("type", "SYNONYM");
                        transformed.put("object_type", "SYNONYM");
                        transformed.put("status", "VALID");
                        transformed.put("created", null);
                        transformed.put("lastModified", null);
                        transformed.put("last_ddl_time", null);
                        transformed.put("temporary", "N");
                        transformed.put("generated", "N");
                        transformed.put("secondary", "N");
                        transformed.put("parameterCount", 0);
                        transformed.put("targetOwner", syn.get("target_owner"));
                        transformed.put("targetName", syn.get("target_name"));
                        transformed.put("targetType", "PROCEDURE");
                        transformed.put("dbLink", syn.get("db_link"));
                        transformed.put("isRemote", syn.get("db_link") != null);
                        transformed.put("isSynonym", true);
                        transformed.put("icon", "synonym");
                        transformed.put("targetIcon", "procedure");
                        transformed.put("targetDisplayType", "Procedure");
                        transformed.put("targetStatus", "UNKNOWN");

                        result.add(transformed);
                    }
                } catch (Exception ex) {
                    log.error("Fallback synonym query also failed: {}", ex.getMessage());
                }
            }

            // Sort combined results by name
            result.sort((a, b) -> {
                String nameA = (String) a.get("name");
                String nameB = (String) b.get("name");
                return nameA.compareTo(nameB);
            });

            log.info("Returning {} total items (procedures + synonyms)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllProcedures: {}", e.getMessage(), e);
            // Return empty list instead of throwing exception to maintain backward compatibility
            return new ArrayList<>();
        }

        return result;
    }

}