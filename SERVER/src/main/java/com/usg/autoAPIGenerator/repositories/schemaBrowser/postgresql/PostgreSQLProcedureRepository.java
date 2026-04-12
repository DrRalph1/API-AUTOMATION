package com.usg.autoAPIGenerator.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class PostgreSQLProcedureRepository extends PostgreSQLRepository {

    // ============================================================
    // PROCEDURE METHODS - POSTGRESQL VERSION WITH FALLBACK
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
                    "    p.pronargs as parameter_count, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link " +
                    "FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
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

            String procedureCountSql = "SELECT COUNT(*) FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
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
                            "    p.pronargs as parameter_count, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM pg_catalog.pg_proc p " +
                            "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
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
     * Get procedure details with owner - WITH FALLBACK TO SOURCE PARSING
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

            // Get parameters from database first
            List<Map<String, Object>> params = getProcedureParametersFromDatabase(owner, procedureName);

            // If no parameters found in database, fallback to source parsing
            if (params.isEmpty()) {
                log.info("No parameters found in database for {}.{}, falling back to source parsing", owner, procedureName);
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

    /**
     * Get procedure parameters with pagination - WITH FALLBACK TO SOURCE PARSING
     */
    public Map<String, Object> getProcedureParametersPaginated(String procedureName, String owner,
                                                               int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            log.info("Getting paginated parameters for {}.{}, page: {}, pageSize: {}",
                    owner, procedureName, page, pageSize);

            int offset = (page - 1) * pageSize;
            int totalCount = 0;
            List<Map<String, Object>> parameters = new ArrayList<>();
            boolean fromSource = false;

            // Try to get parameters from database first
            try {
                List<Map<String, Object>> allParams = getProcedureParametersFromDatabase(owner, procedureName);

                if (!allParams.isEmpty()) {
                    totalCount = allParams.size();

                    // Apply pagination
                    if (!allParams.isEmpty() && offset < allParams.size()) {
                        int endIndex = Math.min(offset + pageSize, allParams.size());
                        parameters = allParams.subList(offset, endIndex);
                    }

                    log.info("Found {} parameters in database for {}.{}", totalCount, owner, procedureName);
                }
            } catch (Exception e) {
                log.debug("Error getting database parameters: {}", e.getMessage());
            }

            // If no parameters found in database, fallback to source parsing
            if (parameters.isEmpty()) {
                log.info("No parameters found in database for {}.{}, falling back to source parsing",
                        owner, procedureName);

                List<Map<String, Object>> allParams = parseProcedureParametersFromSource(owner, procedureName);
                totalCount = allParams.size();

                // Apply pagination to the parsed parameters
                if (!allParams.isEmpty() && offset < allParams.size()) {
                    int endIndex = Math.min(offset + pageSize, allParams.size());
                    parameters = allParams.subList(offset, endIndex);
                }

                fromSource = true;
                log.info("Parsed {} parameters from source for {}.{}", totalCount, owner, procedureName);
            }

            result.put("items", parameters);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

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

            String procedureCountSql = "SELECT COUNT(*) FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
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
                            "FROM pg_catalog.pg_proc p " +
                            "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
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
                    "    p.pronargs as parameter_count " +
                    "FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.prokind = 'p' " +
                    "ORDER BY p.proname";
            return getJdbcTemplate().queryForList(sql, schemaName);
        } catch (Exception e) {
            log.error("Error in getProceduresBySchema: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ============================================================
    // PUBLIC DEBUGGING METHODS
    // ============================================================

    public boolean checkProcedureExists(String procedureName, String schema) {
        try {
            String sql = "SELECT COUNT(*) FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";
            Integer count = getJdbcTemplate().queryForObject(sql, Integer.class, schema, procedureName);
            log.info("Procedure {}.{} exists count: {}", schema, procedureName, count);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking procedure existence: {}", e.getMessage());
            return false;
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    /**
     * Get procedure parameters from PostgreSQL system catalogs
     */
    private List<Map<String, Object>> getProcedureParametersFromDatabase(String owner, String procedureName) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            log.debug("Getting parameters from database for {}.{}", owner, procedureName);

            // Get procedure OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            Long procOid;
            try {
                procOid = getJdbcTemplate().queryForObject(oidSql, Long.class, owner, procedureName);
                log.debug("Found procedure OID: {} for {}.{}", procOid, owner, procedureName);
            } catch (EmptyResultDataAccessException e) {
                log.debug("Procedure {}.{} not found in database", owner, procedureName);
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

            log.debug("Successfully retrieved {} parameters from database for {}.{}",
                    params.size(), owner, procedureName);

        } catch (Exception e) {
            log.debug("Error getting database parameters: {}", e.getMessage());
        }

        return params;
    }

    /**
     * Parse procedure parameters directly from source code - FALLBACK METHOD
     */
    private List<Map<String, Object>> parseProcedureParametersFromSource(String owner, String procedureName) {
        List<Map<String, Object>> params = new ArrayList<>();

        try {
            String source = getProcedureSource(owner, procedureName);
            if (source == null || source.isEmpty()) {
                log.debug("No source found for {}.{}", owner, procedureName);
                return params;
            }

            log.debug("Parsing parameters from source for {}.{}", owner, procedureName);

            // Remove comments first
            String sourceWithoutComments = removeComments(source);

            // Multiple patterns to match different procedure signatures
            String[] patterns = {
                    // Pattern 1: CREATE OR REPLACE PROCEDURE name ( ... ) AS/LANGUAGE
                    "CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+" +
                            "(?:" + Pattern.quote(owner) + "\\.)?" +
                            Pattern.quote(procedureName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*(?:AS|IS|LANGUAGE|\\$\\w+\\$)",

                    // Pattern 2: PROCEDURE name ( ... ) AS/IS
                    "PROCEDURE\\s+" +
                            "(?:" + Pattern.quote(owner) + "\\.)?" +
                            Pattern.quote(procedureName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*(?:AS|IS)",

                    // Pattern 3: Just the signature without CREATE
                    Pattern.quote(procedureName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*(?:AS|IS|LANGUAGE|RETURNS|\\$\\w+\\$)"
            };

            String paramsSection = null;

            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(sourceWithoutComments);

                if (matcher.find()) {
                    paramsSection = matcher.group(1).trim();
                    log.debug("Found parameter section using pattern: {}", patternStr);
                    break;
                }
            }

            if (paramsSection == null || paramsSection.isEmpty()) {
                log.debug("Could not find parameter section for {}.{}", owner, procedureName);
                return params;
            }

            log.debug("Parameter section: {}", paramsSection.length() > 200 ?
                    paramsSection.substring(0, 200) + "..." : paramsSection);

            // Split parameters by comma, respecting parentheses
            List<String> paramDeclarations = splitParametersByComma(paramsSection);

            int position = 1;
            for (String paramDecl : paramDeclarations) {
                Map<String, Object> param = parseParameterDeclaration(paramDecl, position++);
                if (param != null && !param.isEmpty()) {
                    params.add(param);
                    log.debug("Parsed parameter {}: {}", position-1, param);
                }
            }

            log.info("Parsed {} parameters from source for {}.{}", params.size(), owner, procedureName);

        } catch (Exception e) {
            log.error("Error parsing parameters from source for {}.{}: {}",
                    owner, procedureName, e.getMessage(), e);
        }

        return params;
    }

    /**
     * Parse a single parameter declaration
     */
    private Map<String, Object> parseParameterDeclaration(String paramDecl, int position) {
        Map<String, Object> param = new HashMap<>();

        try {
            // Remove trailing commas and whitespace
            paramDecl = paramDecl.replaceAll(",\\s*$", "").trim();

            // Handle DEFAULT values that might contain commas
            String defaultValue = null;
            String defaultPattern = "\\s+DEFAULT\\s+";

            if (paramDecl.toUpperCase().contains("DEFAULT")) {
                String[] parts = paramDecl.split(defaultPattern, 2);
                paramDecl = parts[0].trim();
                defaultValue = parts[1].trim();
                // Remove trailing comma if exists
                if (defaultValue.endsWith(",")) {
                    defaultValue = defaultValue.substring(0, defaultValue.length() - 1).trim();
                }
            }

            // PostgreSQL parameter format: [mode] name data_type
            // Patterns to match different parameter styles
            String[] patterns = {
                    // Pattern with explicit mode: IN name data_type
                    "^(IN|OUT|INOUT|IN OUT)\\s+(\\w+)\\s+(\\w+(?:\\([^)]*\\))?(?:\\[\\])?)",
                    // Pattern without explicit mode (default IN): name data_type
                    "^(\\w+)\\s+(\\w+(?:\\([^)]*\\))?(?:\\[\\])?)"
            };

            String paramName = null;
            String dataType = null;
            String mode = "IN"; // Default mode

            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(paramDecl);

                if (matcher.find()) {
                    if (patternStr.contains("IN|OUT|INOUT")) {
                        mode = matcher.group(1).toUpperCase().replace(" ", "_");
                        paramName = matcher.group(2);
                        dataType = matcher.group(3);
                    } else {
                        paramName = matcher.group(1);
                        dataType = matcher.group(2);
                    }
                    break;
                }
            }

            if (paramName != null && dataType != null) {
                param.put("argument_name", paramName);
                param.put("position", position);
                param.put("sequence", position);
                param.put("data_type", dataType.toUpperCase());
                param.put("in_out", mode);
                param.put("defaulted", defaultValue != null ? "Y" : "N");
                if (defaultValue != null) {
                    param.put("default_value", defaultValue);
                }
            } else {
                log.debug("Could not parse parameter declaration: {}", paramDecl);
            }

        } catch (Exception e) {
            log.debug("Error parsing parameter declaration: {}", e.getMessage());
        }

        return param.isEmpty() ? null : param;
    }

    /**
     * Get parameter mode string from PostgreSQL mode code
     */
    private String getParameterMode(String modeCode) {
        if (modeCode == null) return "IN";
        switch (modeCode.toLowerCase()) {
            case "i": return "IN";
            case "o": return "OUT";
            case "b": return "IN/OUT";
            case "v": return "VARIADIC";
            case "t": return "TABLE";
            default: return "IN";
        }
    }

    /**
     * Get data type name from PostgreSQL type OID
     */
    private String getDataTypeName(String typeOid) {
        try {
            if (typeOid == null || typeOid.isEmpty()) {
                return "UNKNOWN";
            }

            try {
                int oid = Integer.parseInt(typeOid);
                String sql = "SELECT typname FROM pg_type WHERE oid = ?";
                return getJdbcTemplate().queryForObject(sql, String.class, oid);
            } catch (NumberFormatException e) {
                return typeOid;
            }
        } catch (Exception e) {
            log.debug("Error getting type name for OID {}: {}", typeOid, e.getMessage());
            return typeOid;
        }
    }

    /**
     * Get procedure source code
     */
    private String getProcedureSource(String owner, String procedureName) {
        try {
            String sql = "SELECT pg_get_functiondef(p.oid) as source " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";
            return getJdbcTemplate().queryForObject(sql, String.class, owner, procedureName);
        } catch (Exception e) {
            log.debug("Error getting procedure source: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Split parameters by comma, respecting parentheses and quotes
     */
    public List<String> splitParametersByComma(String paramsSection) {
        List<String> params = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenthesesLevel = 0;
        boolean inQuotes = false;
        boolean inSingleQuotes = false;

        for (int i = 0; i < paramsSection.length(); i++) {
            char c = paramsSection.charAt(i);

            // Handle string literals
            if (c == '"' && !inSingleQuotes) {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == '\'' && !inQuotes) {
                inSingleQuotes = !inSingleQuotes;
                current.append(c);
            } else if (!inQuotes && !inSingleQuotes && c == '(') {
                parenthesesLevel++;
                current.append(c);
            } else if (!inQuotes && !inSingleQuotes && c == ')') {
                parenthesesLevel--;
                current.append(c);
            } else if (!inQuotes && !inSingleQuotes && c == ',' && parenthesesLevel == 0) {
                params.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            String lastParam = current.toString().trim();
            if (!lastParam.isEmpty()) {
                params.add(lastParam);
            }
        }

        return params;
    }

    /**
     * Remove SQL comments from source
     */
    public String removeComments(String source) {
        if (source == null) return null;

        // Remove line comments
        String noLineComments = source.replaceAll("--.*$", "");
        // Remove block comments
        String noBlockComments = noLineComments.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        return noBlockComments;
    }

    /**
     * Transform procedure items for frontend
     */
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
                    "    p.pronargs as parameter_count, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link " +
                    "FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
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