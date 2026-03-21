package com.usg.apiAutomation.repositories.schemaBrowser.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostgreSQLFunctionRepository extends PostgreSQLRepository {

    // ============================================================
    // FUNCTION METHODS - POSTGRESQL VERSION WITH FALLBACK
    // ============================================================

    public List<Map<String, Object>> getAllFunctions() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String functionSql = "SELECT " +
                    "    p.proname as name, " +
                    "    'FUNCTION' as type, " +
                    "    'VALID' as status, " +
                    "    p.proowner as created, " +
                    "    p.proowner as last_ddl_time, " +
                    "    p.pronargs as parameter_count, " +
                    "    pg_get_function_result(p.oid) as return_type " +
                    "FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'f' " +
                    "ORDER BY p.proname";

            List<Map<String, Object>> functions = getJdbcTemplate().queryForList(functionSql);

            for (Map<String, Object> func : functions) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "function-" + System.currentTimeMillis() + "-" + func.get("name"));
                transformed.put("name", func.get("name"));
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "FUNCTION");
                transformed.put("status", func.get("status"));
                transformed.put("parameterCount", func.get("parameter_count") != null ? func.get("parameter_count") : 0);
                transformed.put("returnType", func.get("return_type"));
                transformed.put("created", func.get("created"));
                transformed.put("lastModified", func.get("last_ddl_time"));
                transformed.put("icon", "function");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (functions)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllFunctions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    public List<Map<String, Object>> getAllFunctionsForFrontend() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String functionSql = "SELECT " +
                    "    p.proname as name, " +
                    "    'FUNCTION' as type, " +
                    "    'VALID' as status, " +
                    "    pg_get_function_result(p.oid) as return_type, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link, " +
                    "    p.pronargs as parameter_count " +
                    "FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() " +
                    "AND p.prokind = 'f' " +
                    "ORDER BY p.proname";

            List<Map<String, Object>> functions = getJdbcTemplate().queryForList(functionSql);
            log.info("Found {} functions", functions.size());

            for (Map<String, Object> func : functions) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "function-" + System.currentTimeMillis() + "-" + func.get("name"));
                transformed.put("name", func.get("name"));
                transformed.put("function_name", func.get("name"));
                transformed.put("owner", getCurrentSchema());
                transformed.put("type", "FUNCTION");
                transformed.put("object_type", "FUNCTION");
                transformed.put("status", func.get("status"));
                transformed.put("parameterCount", func.get("parameter_count") != null ? func.get("parameter_count") : 0);
                transformed.put("returnType", func.get("return_type"));
                transformed.put("created", null);
                transformed.put("lastModified", null);
                transformed.put("icon", "function");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

            result.sort((a, b) -> {
                String nameA = (String) a.get("name");
                String nameB = (String) b.get("name");
                return nameA.compareTo(nameB);
            });

            log.info("Returning {} total items (functions)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllFunctionsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * Get function details with fallback to source parsing
     */
    public Map<String, Object> getFunctionDetails(String owner, String functionName) {
        Map<String, Object> details = new HashMap<>();
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            // Get function info
            String sql = "SELECT " +
                    "    p.proname as function_name, " +
                    "    'FUNCTION' as object_type, " +
                    "    'VALID' as status, " +
                    "    n.nspname as owner, " +
                    "    pg_get_function_result(p.oid) as return_type, " +
                    "    pg_get_function_arguments(p.oid) as arguments, " +
                    "    pg_get_functiondef(p.oid) as source_code " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'f'";

            try {
                Map<String, Object> funcInfo = getJdbcTemplate().queryForMap(sql, owner, functionName);
                details.putAll(funcInfo);
                details.put("function_name", functionName);
                details.put("owner", owner);

                // Get parameters and return type
                Map<String, Object> paramsAndReturn = getFunctionParametersAndReturn(owner, functionName);
                details.put("parameters", paramsAndReturn.get("parameters"));
                details.put("parameterCount", ((List) paramsAndReturn.get("parameters")).size());
                details.put("returnType", paramsAndReturn.get("returnType"));

                // Source code is already in the query result
                if (funcInfo.get("source_code") != null) {
                    details.put("sourceCode", funcInfo.get("source_code"));
                }

            } catch (EmptyResultDataAccessException e) {
                log.debug("Function {}.{} not found in database, will try source parsing", owner, functionName);

                // Try to get parameters and return type from source parsing
                List<Map<String, Object>> params = parseFunctionParametersFromSource(owner, functionName);
                String returnType = parseFunctionReturnTypeFromSource(owner, functionName);

                details.put("function_name", functionName);
                details.put("owner", owner);
                details.put("object_type", "FUNCTION");
                details.put("status", "VALID");
                details.put("parameters", params);
                details.put("parameterCount", params.size());
                details.put("returnType", returnType);

                // Get source code if available
                String source = getFunctionSource(owner, functionName);
                if (source != null) {
                    details.put("sourceCode", source);
                }
            }

        } catch (Exception e) {
            log.warn("Error getting function details for {}.{}: {}", owner, functionName, e.getMessage());
            details.put("error", e.getMessage());
        }
        return details;
    }

    /**
     * Get function parameters with pagination - WITH FALLBACK TO SOURCE PARSING
     */
    public Map<String, Object> getFunctionParametersPaginated(String functionName, String owner,
                                                              int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (owner == null || owner.isEmpty()) {
                owner = getCurrentSchema();
            }

            log.info("Getting paginated parameters for function {}.{}, page: {}, pageSize: {}",
                    owner, functionName, page, pageSize);

            int offset = (page - 1) * pageSize;
            int totalCount = 0;
            List<Map<String, Object>> parameters = new ArrayList<>();
            boolean fromSource = false;

            // Try to get parameters from database first
            try {
                Map<String, Object> paramsAndReturn = getFunctionParametersAndReturn(owner, functionName);
                List<Map<String, Object>> allParams = (List<Map<String, Object>>) paramsAndReturn.get("parameters");

                if (!allParams.isEmpty()) {
                    totalCount = allParams.size();

                    // Apply pagination
                    if (!allParams.isEmpty() && offset < allParams.size()) {
                        int endIndex = Math.min(offset + pageSize, allParams.size());
                        parameters = allParams.subList(offset, endIndex);
                    }

                    log.info("Found {} parameters in database for function {}.{}", totalCount, owner, functionName);
                }
            } catch (Exception e) {
                log.debug("Error getting database parameters: {}", e.getMessage());
            }

            // If no parameters found in database, fallback to source parsing
            if (parameters.isEmpty()) {
                log.info("No parameters found in database for function {}.{}, falling back to source parsing",
                        owner, functionName);

                List<Map<String, Object>> allParams = parseFunctionParametersFromSource(owner, functionName);
                totalCount = allParams.size();

                // Apply pagination to the parsed parameters
                if (!allParams.isEmpty() && offset < allParams.size()) {
                    int endIndex = Math.min(offset + pageSize, allParams.size());
                    parameters = allParams.subList(offset, endIndex);
                }

                fromSource = true;
                log.info("Parsed {} parameters from source for function {}.{}", totalCount, owner, functionName);
            }

            result.put("parameters", parameters);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            if (fromSource) {
                result.put("fromSource", true);
                result.put("message", "Parameters parsed from source code");
            }

        } catch (Exception e) {
            log.error("Error in getFunctionParametersPaginated for {}.{}: {}", owner, functionName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve function parameters: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================================================
    // PRIVATE HELPER METHODS WITH FALLBACK
    // ============================================================

    /**
     * Get function parameters and return type - tries database first, falls back to source parsing
     */
    private Map<String, Object> getFunctionParametersAndReturn(String owner, String functionName) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> params = new ArrayList<>();
        String returnType = null;

        try {
            // Get function OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'f'";

            Long funcOid;
            try {
                funcOid = getJdbcTemplate().queryForObject(oidSql, Long.class, owner, functionName);
                log.debug("Found function OID: {} for {}.{}", funcOid, owner, functionName);
            } catch (EmptyResultDataAccessException e) {
                log.debug("Function {}.{} not found in database", owner, functionName);
                // Fallback to source parsing
                params = parseFunctionParametersFromSource(owner, functionName);
                returnType = parseFunctionReturnTypeFromSource(owner, functionName);
                result.put("parameters", params);
                result.put("returnType", returnType);
                return result;
            }

            if (funcOid != null) {
                // Get function arguments from pg_proc
                String argsSql = "SELECT " +
                        "    proname, " +
                        "    pronargs, " +
                        "    pronargdefaults, " +
                        "    proargnames, " +
                        "    proargtypes, " +
                        "    proallargtypes, " +
                        "    proargmodes " +
                        "FROM pg_proc WHERE oid = ?";

                Map<String, Object> funcInfo = getJdbcTemplate().queryForMap(argsSql, funcOid);

                // Parse arguments
                Array proargnames = (Array) funcInfo.get("proargnames");
                Array proargtypes = (Array) funcInfo.get("proargtypes");
                Array proallargtypes = (Array) funcInfo.get("proallargtypes");
                Array proargmodes = (Array) funcInfo.get("proargmodes");
                Integer pronargs = (Integer) funcInfo.get("pronargs");

                String[] argNames = proargnames != null ? (String[]) proargnames.getArray() : new String[0];
                String[] argTypes = proargtypes != null ? (String[]) proargtypes.getArray() : new String[0];
                String[] allArgTypes = proallargtypes != null ? (String[]) proallargtypes.getArray() : new String[0];
                String[] argModes = proargmodes != null ? (String[]) proargmodes.getArray() : new String[0];

                // Determine return type
                if (allArgTypes != null && allArgTypes.length > 0) {
                    String lastArgType = allArgTypes[allArgTypes.length - 1];
                    if (argModes != null && argModes.length > 0) {
                        for (int i = 0; i < argModes.length; i++) {
                            if ("o".equals(argModes[i]) || "b".equals(argModes[i])) {
                                returnType = getDataTypeName(lastArgType);
                                break;
                            }
                        }
                    }
                }

                if (returnType == null) {
                    // Get return type from pg_get_function_result
                    String returnTypeSql = "SELECT pg_get_function_result(?) as return_type";
                    returnType = getJdbcTemplate().queryForObject(returnTypeSql, String.class, funcOid);
                }

                // Build parameters list
                for (int i = 0; i < pronargs; i++) {
                    Map<String, Object> param = new HashMap<>();
                    String paramName = i < argNames.length && argNames[i] != null ? argNames[i] : "arg" + (i + 1);
                    String paramType = i < argTypes.length ? getDataTypeName(argTypes[i]) : "unknown";
                    String paramMode = i < argModes.length ? getParameterMode(argModes[i]) : "IN";

                    param.put("argument_name", paramName);
                    param.put("data_type", paramType);
                    param.put("in_out", paramMode);
                    param.put("position", i + 1);
                    param.put("sequence", i + 1);

                    params.add(param);
                }
            }

            // If we couldn't get parameters from system catalogs, try parsing from source
            if (params.isEmpty()) {
                log.debug("No parameters found in database for {}.{}, parsing from source", owner, functionName);
                params = parseFunctionParametersFromSource(owner, functionName);
                if (returnType == null || returnType.isEmpty()) {
                    returnType = parseFunctionReturnTypeFromSource(owner, functionName);
                }
            }

        } catch (Exception e) {
            log.debug("Error getting function parameters from database: {}", e.getMessage());
            // Fallback to source parsing
            params = parseFunctionParametersFromSource(owner, functionName);
            if (returnType == null) {
                returnType = parseFunctionReturnTypeFromSource(owner, functionName);
            }
        }

        result.put("parameters", params);
        result.put("returnType", returnType);
        return result;
    }

    /**
     * Parse function parameters from source code - FALLBACK METHOD
     */
    private List<Map<String, Object>> parseFunctionParametersFromSource(String owner, String functionName) {
        List<Map<String, Object>> params = new ArrayList<>();
        try {
            String source = getFunctionSource(owner, functionName);
            if (source == null || source.isEmpty()) {
                log.debug("No source found for function {}.{}", owner, functionName);
                return params;
            }

            log.debug("Parsing function parameters from source for {}.{}", owner, functionName);

            // Remove comments
            String sourceWithoutComments = removeComments(source);

            // Multiple patterns to match different function signatures
            String[] patterns = {
                    // Pattern 1: CREATE OR REPLACE FUNCTION name ( ... ) RETURNS
                    "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+" +
                            "(?:" + Pattern.quote(owner) + "\\.)?" +
                            Pattern.quote(functionName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*RETURNS",

                    // Pattern 2: FUNCTION name ( ... ) RETURNS
                    "FUNCTION\\s+" +
                            "(?:" + Pattern.quote(owner) + "\\.)?" +
                            Pattern.quote(functionName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*RETURNS",

                    // Pattern 3: Just the signature without CREATE
                    Pattern.quote(functionName) +
                            "\\s*\\(([\\s\\S]*?)\\)\\s*RETURNS"
            };

            String paramsSection = null;

            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(sourceWithoutComments);

                if (matcher.find()) {
                    paramsSection = matcher.group(1).trim();
                    log.debug("Found parameter section for function using pattern: {}", patternStr);
                    break;
                }
            }

            if (paramsSection == null || paramsSection.isEmpty()) {
                log.debug("Could not find parameter section for function {}.{}", owner, functionName);
                return params;
            }

            log.debug("Parameter section: {}", paramsSection.length() > 200 ?
                    paramsSection.substring(0, 200) + "..." : paramsSection);

            // Split parameters by comma, respecting parentheses and quotes
            List<String> paramDeclarations = splitParametersByComma(paramsSection);

            int position = 1;
            for (String paramDecl : paramDeclarations) {
                Map<String, Object> param = parseFunctionParameterFromString(paramDecl, position++);
                if (param != null && !param.isEmpty()) {
                    params.add(param);
                    log.debug("Parsed function parameter {}: {}", position-1, param);
                }
            }

            log.info("Parsed {} parameters from source for function {}.{}", params.size(), owner, functionName);

        } catch (Exception e) {
            log.error("Error parsing function parameters from source for {}.{}: {}",
                    owner, functionName, e.getMessage(), e);
        }
        return params;
    }

    /**
     * Parse function return type from source code - FALLBACK METHOD
     */
    private String parseFunctionReturnTypeFromSource(String owner, String functionName) {
        try {
            String source = getFunctionSource(owner, functionName);
            if (source == null || source.isEmpty()) {
                return null;
            }

            String sourceWithoutComments = removeComments(source);

            // Multiple patterns to match return type
            String[] patterns = {
                    "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+" +
                            "(?:" + Pattern.quote(owner) + "\\.)?" +
                            Pattern.quote(functionName) +
                            "\\s*\\([\\s\\S]*?\\)\\s*RETURNS\\s+(\\w+(?:\\([^)]*\\))?(?:\\[\\])?)",

                    "FUNCTION\\s+" +
                            "(?:" + Pattern.quote(owner) + "\\.)?" +
                            Pattern.quote(functionName) +
                            "\\s*\\([\\s\\S]*?\\)\\s*RETURNS\\s+(\\w+(?:\\([^)]*\\))?(?:\\[\\])?)",

                    Pattern.quote(functionName) +
                            "\\s*\\([\\s\\S]*?\\)\\s*RETURNS\\s+(\\w+(?:\\([^)]*\\))?(?:\\[\\])?)"
            };

            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(sourceWithoutComments);

                if (matcher.find()) {
                    String returnType = matcher.group(1).trim();
                    log.debug("Found return type for function {}.{}: {}", owner, functionName, returnType);
                    return returnType;
                }
            }

        } catch (Exception e) {
            log.error("Error parsing function return type for {}.{}: {}", owner, functionName, e.getMessage());
        }
        return null;
    }

    /**
     * Parse a single function parameter from string
     */
    private Map<String, Object> parseFunctionParameterFromString(String paramDecl, int position) {
        paramDecl = paramDecl.replaceAll("--.*$", "").trim();
        Map<String, Object> param = new HashMap<>();

        try {
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

            // PostgreSQL function parameter format: [mode] name type
            String[] patterns = {
                    // Pattern with explicit mode: IN name type
                    "^(IN|OUT|INOUT|IN OUT|VARIADIC)\\s+(\\w+)\\s+(\\w+(?:\\([^)]*\\))?(?:\\[\\])?)",
                    // Pattern without explicit mode (default IN): name type
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
                if (defaultValue != null) {
                    param.put("defaulted", "Y");
                    param.put("default_value", defaultValue);
                } else {
                    param.put("defaulted", "N");
                }
            } else {
                log.debug("Could not parse function parameter: {}", paramDecl);
            }

        } catch (Exception e) {
            log.debug("Error parsing function parameter: {}", e.getMessage());
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
     * Get function source code
     */
    private String getFunctionSource(String owner, String functionName) {
        try {
            String sql = "SELECT pg_get_functiondef(p.oid) as source " +
                    "FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'f'";
            return getJdbcTemplate().queryForObject(sql, String.class, owner, functionName);
        } catch (Exception e) {
            log.debug("Error getting function source: {}", e.getMessage());
            return null;
        }
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

    public Map<String, Object> getFunctionsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND p.prokind = 'f'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT " +
                    "    p.proname as function_name, " +
                    "    'VALID' as status, " +
                    "    NULL as created, " +
                    "    NULL as last_ddl_time " +
                    "FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND p.prokind = 'f' " +
                    "ORDER BY p.proname " +
                    "OFFSET ? LIMIT ?";

            List<Map<String, Object>> functions = getJdbcTemplate().queryForList(dataSql, offset, pageSize);

            result.put("items", functions);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getFunctionsPaginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
        }
        return result;
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
                    "    p.pronargs as parameter_count, " +
                    "    pg_get_function_result(p.oid) as return_type " +
                    "FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.prokind = 'f' " +
                    "ORDER BY p.proname";

            return getJdbcTemplate().queryForList(sql, schemaName);

        } catch (Exception e) {
            log.error("Error in getFunctionsBySchema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve functions for schema: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAllFunctionsForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM pg_catalog.pg_proc p " +
                    "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = current_schema() AND p.prokind = 'f'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            log.info("Found {} functions", totalCount);

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (totalCount > 0 && offset < totalCount) {
                int funcOffset = offset;
                int funcLimit = Math.min(pageSize, totalCount - funcOffset);

                if (funcLimit > 0) {
                    String functionSql = "SELECT " +
                            "    p.proname as name, " +
                            "    'FUNCTION' as type, " +
                            "    'VALID' as status, " +
                            "    NULL as created, " +
                            "    NULL as last_ddl_time, " +
                            "    p.pronargs as parameter_count, " +
                            "    pg_get_function_result(p.oid) as return_type, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM pg_catalog.pg_proc p " +
                            "JOIN pg_catalog.pg_namespace n ON p.pronamespace = n.oid " +
                            "WHERE n.nspname = current_schema() AND p.prokind = 'f' " +
                            "ORDER BY p.proname " +
                            "OFFSET ? LIMIT ?";

                    try {
                        List<Map<String, Object>> functions = getJdbcTemplate().queryForList(functionSql, funcOffset, funcLimit);
                        allItems.addAll(functions);
                    } catch (Exception e) {
                        log.error("Error fetching functions: {}", e.getMessage());
                    }
                }
            }

            List<Map<String, Object>> transformed = new ArrayList<>();
            for (Map<String, Object> item : allItems) {
                Map<String, Object> transformedItem = new HashMap<>();
                String type = (String) item.get("type");
                String name = (String) item.get("name");

                if ("FUNCTION".equals(type)) {
                    transformedItem.put("id", "function-" + System.currentTimeMillis() + "-" + name);
                    transformedItem.put("name", name);
                    transformedItem.put("function_name", name);
                    transformedItem.put("owner", getCurrentSchema());
                    transformedItem.put("type", "FUNCTION");
                    transformedItem.put("status", item.get("status"));
                    transformedItem.put("parameterCount", item.get("parameter_count") != null ? item.get("parameter_count") : 0);
                    transformedItem.put("returnType", item.get("return_type"));
                    transformedItem.put("created", item.get("created"));
                    transformedItem.put("lastModified", item.get("last_ddl_time"));
                    transformedItem.put("icon", "function");
                    transformedItem.put("isSynonym", false);
                }
                transformed.add(transformedItem);
            }

            result.put("items", transformed);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0);

        } catch (Exception e) {
            log.error("Error in getAllFunctionsForFrontend paginated: {}", e.getMessage(), e);
            result.put("items", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
        }
        return result;
    }

    /**
     * Get function details by name only
     */
    public Map<String, Object> getFunctionDetails(String functionName) {
        try {
            Map<String, Object> funcLocation = findObjectLocation(functionName, "FUNCTION");
            String owner = (String) funcLocation.get("owner");

            if (owner == null) {
                owner = getCurrentSchema();
            }

            return getFunctionDetails(owner, functionName);

        } catch (EmptyResultDataAccessException e) {
            log.warn("Function {} not found", functionName);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("function_name", functionName);
            emptyResult.put("message", "Function not found");
            emptyResult.put("status", "NOT_FOUND");
            return emptyResult;
        } catch (Exception e) {
            log.error("Error in getFunctionDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve function details: " + e.getMessage(), e);
        }
    }
}