package com.usg.apiAutomation.repositories.schemaBrowser.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostgreSQLProcedureRepository extends PostgreSQLRepository {

    // ============================================================
    // PROCEDURE METHODS - POSTGRESQL VERSION
    // ============================================================

    public List<Map<String, Object>> getAllProceduresForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String procedureSql = "SELECT " +
                    "    p.proname as name, " +
                    "    'PROCEDURE' as type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'p' " +
                    "ORDER BY p.proname";

            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql);

            for (Map<String, Object> proc : procedures) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "procedure-" + System.currentTimeMillis() + "-" + proc.get("name"));
                transformed.put("name", proc.get("name"));
                transformed.put("procedure_name", proc.get("name"));
                transformed.put("owner", getCurrentSchema());
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

            // PostgreSQL doesn't have synonyms
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (procedures)", result.size());

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

            String procedureCountSql = "SELECT COUNT(*) FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND p.prokind = 'p'";
            int procedureCount = getJdbcTemplate().queryForObject(procedureCountSql, Integer.class);

            int totalCount = procedureCount;

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (procedureCount > 0 && offset < procedureCount) {
                int procOffset = offset;
                int procLimit = Math.min(pageSize, procedureCount - procOffset);

                if (procLimit > 0) {
                    String procedureSql = "SELECT " +
                            "    p.proname as name, " +
                            "    'PROCEDURE' as type, " +
                            "    'VALID' as status, " +
                            "    NULL as created, " +
                            "    NULL as last_ddl_time, " +
                            "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM pg_proc p " +
                            "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                            "WHERE n.nspname = current_schema() AND p.prokind = 'p' " +
                            "ORDER BY p.proname " +
                            "OFFSET ? LIMIT ?";

                    List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql, procOffset, procLimit);
                    allItems.addAll(procedures);
                }
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
     * Get procedure details with owner
     */
    public Map<String, Object> getProcedureDetails(String owner, String procedureName) {
        Map<String, Object> details = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            // Check if this is a procedure (prokind = 'p')
            String sql = "SELECT " +
                    "    p.proname as object_name, " +
                    "    'PROCEDURE' as object_type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'p'";

            try {
                Map<String, Object> procInfo = getJdbcTemplate().queryForMap(sql, owner, procedureName);
                details.putAll(procInfo);
            } catch (EmptyResultDataAccessException e) {
                // Try without procedure kind filter
                String altSql = "SELECT " +
                        "    p.proname as object_name, " +
                        "    CASE WHEN p.prokind = 'p' THEN 'PROCEDURE' ELSE 'FUNCTION' END as object_type, " +
                        "    'VALID' as status, " +
                        "    NULL as created, " +
                        "    NULL as last_ddl_time " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = ? AND p.proname = ?";
                Map<String, Object> procInfo = getJdbcTemplate().queryForMap(altSql, owner, procedureName);
                details.putAll(procInfo);
            }

            // Get parameters
            List<Map<String, Object>> params = getProcedureParametersFromDatabase(owner, procedureName);

            // If no parameters found, try parsing from source
            if (params.isEmpty()) {
                log.info("No parameters found in database for {}.{}, trying source parsing", owner, procedureName);
                params = parseProcedureParametersFromSource(owner, procedureName);
            }

            details.put("parameters", params);
            details.put("parameterCount", params.size());

            // Calculate parameter counts by type
            int inCount = 0;
            int outCount = 0;
            int inOutCount = 0;

            for (Map<String, Object> param : params) {
                String inOut = (String) param.get("in_out");
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
            String source = getProcedureSource(owner, procedureName);
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
                owner = getCurrentSchema();
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
                owner = getCurrentSchema();
            }

            int offset = (page - 1) * pageSize;

            // Get parameters from database
            List<Map<String, Object>> allParams = getProcedureParametersFromDatabase(owner, procedureName);

            // If no parameters found, try parsing from source
            if (allParams.isEmpty()) {
                allParams = parseProcedureParametersFromSource(owner, procedureName);
            }

            int totalCount = allParams.size();

            // Apply pagination
            List<Map<String, Object>> parameters = new ArrayList<>();
            if (!allParams.isEmpty() && offset < allParams.size()) {
                int endIndex = Math.min(offset + pageSize, allParams.size());
                parameters = allParams.subList(offset, endIndex);
            }

            result.put("items", parameters);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            return result;

        } catch (Exception e) {
            log.error("Error in getProcedureParametersPaginated for {}.{}: {}", owner, procedureName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve procedure parameters: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getProceduresPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String procedureCountSql = "SELECT COUNT(*) FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND p.prokind = 'p'";
            int totalCount = getJdbcTemplate().queryForObject(procedureCountSql, Integer.class);

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (totalCount > 0 && offset < totalCount) {
                int procOffset = offset;
                int procLimit = Math.min(pageSize, totalCount - procOffset);

                if (procLimit > 0) {
                    String procedureSql = "SELECT " +
                            "    p.proname as name, " +
                            "    'PROCEDURE' as type, " +
                            "    'VALID' as status, " +
                            "    NULL as created, " +
                            "    NULL as last_ddl_time, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM pg_proc p " +
                            "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                            "WHERE n.nspname = current_schema() AND p.prokind = 'p' " +
                            "ORDER BY p.proname " +
                            "OFFSET ? LIMIT ?";

                    List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql, procOffset, procLimit);
                    allItems.addAll(procedures);
                }
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
                    "    n.nspname as owner, " +
                    "    p.proname as procedure_name, " +
                    "    'PROCEDURE' as object_type, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    'VALID' as status, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary, " +
                    "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.prokind = 'p' " +
                    "ORDER BY p.proname";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getProceduresBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<Map<String, Object>> getProcedureParametersFromDatabase(String owner, String procedureName) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            // Get procedure OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'p'";

            Long procOid;
            try {
                procOid = getJdbcTemplate().queryForObject(oidSql, Long.class, owner, procedureName);
            } catch (EmptyResultDataAccessException e) {
                return params;
            }

            // Get procedure arguments
            String argsSql = "SELECT " +
                    "    proargnames, " +
                    "    proargtypes, " +
                    "    proargmodes, " +
                    "    pronargdefaults " +
                    "FROM pg_proc WHERE oid = ?";

            Map<String, Object> procInfo = getJdbcTemplate().queryForMap(argsSql, procOid);

            Array proargnames = (Array) procInfo.get("proargnames");
            Array proargtypes = (Array) procInfo.get("proargtypes");
            Array proargmodes = (Array) procInfo.get("proargmodes");
            Integer pronargdefaults = (Integer) procInfo.get("pronargdefaults");

            String[] argNames = proargnames != null ? (String[]) proargnames.getArray() : new String[0];
            String[] argTypes = proargtypes != null ? (String[]) proargtypes.getArray() : new String[0];
            String[] argModes = proargmodes != null ? (String[]) proargmodes.getArray() : new String[0];

            int defaultCount = pronargdefaults != null ? pronargdefaults : 0;

            for (int i = 0; i < argNames.length; i++) {
                Map<String, Object> param = new HashMap<>();
                param.put("argument_name", argNames[i]);
                param.put("position", i + 1);
                param.put("sequence", i + 1);
                param.put("data_type", getDataTypeName(argTypes[i]));
                param.put("in_out", getParameterMode(i < argModes.length ? argModes[i] : "i"));
                param.put("defaulted", (i >= argNames.length - defaultCount) ? "Y" : "N");

                params.add(param);
            }

        } catch (Exception e) {
            log.debug("Error getting database parameters: {}", e.getMessage());
        }

        return params;
    }

    private List<Map<String, Object>> parseProcedureParametersFromSource(String owner, String procedureName) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String source = getProcedureSource(owner, procedureName);
            if (source == null || source.isEmpty()) {
                return params;
            }

            String sourceWithoutComments = removeComments(source);

            // Pattern to find procedure signature
            String patternStr = "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+" +
                    Pattern.quote(procedureName) +
                    "\\s*\\(([\\s\\S]*?)\\)\\s*(?:AS|IS|LANGUAGE)";
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(sourceWithoutComments);

            if (matcher.find()) {
                String paramsSection = matcher.group(1).trim();
                if (!paramsSection.isEmpty()) {
                    List<String> paramDeclarations = splitParametersByComma(paramsSection);
                    int position = 1;
                    for (String paramDecl : paramDeclarations) {
                        Map<String, Object> param = parseParameterDeclaration(paramDecl, position++);
                        if (param != null && !param.isEmpty()) {
                            params.add(param);
                        }
                    }
                }
            }

            log.info("Parsed {} parameters from source for {}", params.size(), procedureName);

        } catch (Exception e) {
            log.error("Error parsing parameters from source: {}", e.getMessage());
        }

        return params;
    }

    private Map<String, Object> parseParameterDeclaration(String paramDecl, int position) {
        Map<String, Object> param = new HashMap<>();

        paramDecl = paramDecl.replaceAll("--.*$", "").trim();

        // PostgreSQL parameter format: [mode] name data_type [DEFAULT value]
        Pattern pattern = Pattern.compile(
                "^(?:IN|OUT|INOUT)?\\s*(\\w+)\\s+(\\w+(?:\\([^)]*\\))?)\\s*(?:DEFAULT\\s+(.+))?$",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(paramDecl);
        if (matcher.find()) {
            String paramName = matcher.group(1);
            String dataType = matcher.group(2);
            String defaultValue = matcher.group(3);
            String mode = extractMode(paramDecl);

            param.put("argument_name", paramName);
            param.put("position", position);
            param.put("sequence", position);
            param.put("data_type", dataType.toUpperCase());
            param.put("in_out", mode);
            param.put("defaulted", defaultValue != null ? "Y" : "N");
        }

        return param.isEmpty() ? null : param;
    }

    private String extractMode(String paramDecl) {
        String upperDecl = paramDecl.toUpperCase();
        if (upperDecl.startsWith("INOUT")) {
            return "IN/OUT";
        } else if (upperDecl.startsWith("OUT")) {
            return "OUT";
        } else if (upperDecl.startsWith("IN")) {
            return "IN";
        }
        return "IN";
    }

    private String getParameterMode(String modeCode) {
        if (modeCode == null) return "IN";
        switch (modeCode) {
            case "i": return "IN";
            case "o": return "OUT";
            case "b": return "IN/OUT";
            case "v": return "VARIADIC";
            default: return "IN";
        }
    }

    private String getDataTypeName(String typeOid) {
        try {
            String sql = "SELECT typname FROM pg_type WHERE oid = ?::regtype::oid";
            return getJdbcTemplate().queryForObject(sql, String.class, typeOid);
        } catch (Exception e) {
            return typeOid;
        }
    }

    private String getProcedureSource(String owner, String procedureName) {
        try {
            String sql = "SELECT pg_get_functiondef(p.oid) as source " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'p'";
            return getJdbcTemplate().queryForObject(sql, String.class, owner, procedureName);
        } catch (Exception e) {
            log.debug("Error getting procedure source: {}", e.getMessage());
            return null;
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
                transformedItem.put("owner", getCurrentSchema());
                transformedItem.put("type", "PROCEDURE");
                transformedItem.put("status", item.get("status"));
                transformedItem.put("parameterCount", item.get("parameter_count") != null ? item.get("parameter_count") : 0);
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("last_ddl_time"));
                transformedItem.put("icon", "procedure");
                transformedItem.put("isSynonym", false);
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }

    public List<Map<String, Object>> getAllProcedures() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String procedureSql = "SELECT " +
                    "    p.proname as name, " +
                    "    'PROCEDURE' as type, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time, " +
                    "    false as temporary, " +
                    "    false as generated, " +
                    "    false as secondary, " +
                    "    (SELECT COUNT(*) FROM pg_proc_info WHERE proname = p.proname) as parameter_count, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'p' " +
                    "ORDER BY p.proname";

            List<Map<String, Object>> procedures = getJdbcTemplate().queryForList(procedureSql);
            log.info("Found {} procedures", procedures.size());

            for (Map<String, Object> proc : procedures) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "proc-" + System.currentTimeMillis() + "-" + proc.get("name"));
                transformed.put("name", proc.get("name"));
                transformed.put("procedure_name", proc.get("name"));
                transformed.put("owner", getCurrentSchema());
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

            // PostgreSQL doesn't have synonyms
            result.sort((a, b) -> {
                String nameA = (String) a.get("name");
                String nameB = (String) b.get("name");
                return nameA.compareTo(nameB);
            });

            log.info("Returning {} total items (procedures)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllProcedures: {}", e.getMessage(), e);
            return new ArrayList<>();
        }

        return result;
    }
}