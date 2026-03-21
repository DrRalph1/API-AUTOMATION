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
    // FUNCTION METHODS - POSTGRESQL VERSION
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

                // Get parameters
                Map<String, Object> paramsAndReturn = getFunctionParametersAndReturn(owner, functionName);
                details.put("parameters", paramsAndReturn.get("parameters"));
                details.put("parameterCount", ((List) paramsAndReturn.get("parameters")).size());
                details.put("returnType", paramsAndReturn.get("returnType"));

                // Source code is already in the query result
                if (funcInfo.get("source_code") != null) {
                    details.put("sourceCode", funcInfo.get("source_code"));
                }

            } catch (EmptyResultDataAccessException e) {
                log.debug("Function {}.{} not found", owner, functionName);
                details.put("error", "Function not found");
            }

        } catch (Exception e) {
            log.warn("Error getting function details for {}.{}: {}", owner, functionName, e.getMessage());
            details.put("error", e.getMessage());
        }
        return details;
    }

    public Map<String, Object> getFunctionParametersPaginated(String functionName, String owner,
                                                              int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            Map<String, Object> paramsAndReturn = getFunctionParametersAndReturn(owner, functionName);
            List<Map<String, Object>> allParams = (List<Map<String, Object>>) paramsAndReturn.get("parameters");
            int totalCount = allParams.size();

            List<Map<String, Object>> paginatedParams = allParams.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            result.put("parameters", paginatedParams);
            result.put("totalCount", totalCount);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        } catch (Exception e) {
            log.error("Error in getFunctionParametersPaginated for {}.{}: {}", owner, functionName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve function parameters: " + e.getMessage(), e);
        }
        return result;
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private Map<String, Object> getFunctionParametersAndReturn(String owner, String functionName) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> params = new ArrayList<>();
        String returnType = null;

        try {
            // Get function OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'f'";

            Long funcOid = getJdbcTemplate().queryForObject(oidSql, Long.class, owner, functionName);

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
                    // Last argument might be the return type if OUT parameters exist
                    String lastArgType = allArgTypes[allArgTypes.length - 1];
                    if (argModes != null && argModes.length > 0) {
                        for (int i = 0; i < argModes.length; i++) {
                            if ("o".equals(argModes[i]) || "b".equals(argModes[i])) {
                                // OUT or INOUT parameter could be return type
                                returnType = getDataTypeName(lastArgType);
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
                    String paramName = i < argNames.length ? argNames[i] : "arg" + (i + 1);
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
                params = parseFunctionParametersFromSource(owner, functionName);
                if (returnType == null) {
                    returnType = parseFunctionReturnTypeFromSource(owner, functionName);
                }
            }

        } catch (Exception e) {
            log.debug("Error getting function parameters: {}", e.getMessage());
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

    private List<Map<String, Object>> parseFunctionParametersFromSource(String owner, String functionName) {
        List<Map<String, Object>> params = new ArrayList<>();
        try {
            String source = getFunctionSource(owner, functionName);
            if (source == null || source.isEmpty()) return params;

            // Parse function signature
            Pattern pattern = Pattern.compile(
                    "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+" +
                            Pattern.quote(functionName) +
                            "\\s*\\((.*?)\\)\\s*RETURNS\\s+",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(source);
            if (matcher.find()) {
                String paramsSection = matcher.group(1).trim();
                if (!paramsSection.isEmpty()) {
                    List<String> paramDeclarations = splitParametersByComma(paramsSection);
                    int position = 1;
                    for (String paramDecl : paramDeclarations) {
                        Map<String, Object> param = parseFunctionParameterFromString(paramDecl, position++);
                        if (param != null && !param.isEmpty()) {
                            params.add(param);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing function parameters from source: {}", e.getMessage());
        }
        return params;
    }

    private String parseFunctionReturnTypeFromSource(String owner, String functionName) {
        try {
            String source = getFunctionSource(owner, functionName);
            if (source == null || source.isEmpty()) return null;

            Pattern pattern = Pattern.compile(
                    "CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+" +
                            Pattern.quote(functionName) +
                            "\\s*\\([\\s\\S]*?\\)\\s*RETURNS\\s+(\\w+(?:\\([^)]*\\))?)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(source);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.error("Error parsing function return type: {}", e.getMessage());
        }
        return null;
    }

    private Map<String, Object> parseFunctionParameterFromString(String paramDecl, int position) {
        paramDecl = paramDecl.replaceAll("--.*$", "").trim();
        Map<String, Object> param = new HashMap<>();

        // PostgreSQL parameter format: [mode] name type [DEFAULT value]
        Pattern pattern = Pattern.compile(
                "^(?:IN|OUT|INOUT|VARIADIC)?\\s*(\\w+)\\s+(\\w+(?:\\([^)]*\\))?)\\s*(?:DEFAULT\\s+.*)?$",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(paramDecl);
        if (matcher.find()) {
            param.put("argument_name", matcher.group(1));
            param.put("position", position);
            param.put("in_out", extractParameterMode(paramDecl));
            param.put("data_type", matcher.group(2).trim().toUpperCase());
        } else {
            // Try simpler pattern
            String[] parts = paramDecl.split("\\s+");
            if (parts.length >= 2) {
                param.put("argument_name", parts[0]);
                param.put("position", position);
                param.put("in_out", extractParameterMode(paramDecl));
                param.put("data_type", parts[1].trim().toUpperCase());
            }
        }

        return param.isEmpty() ? null : param;
    }

    private String extractParameterMode(String paramDecl) {
        String upperDecl = paramDecl.toUpperCase();
        if (upperDecl.startsWith("OUT ")) {
            return "OUT";
        } else if (upperDecl.startsWith("INOUT ")) {
            return "IN/OUT";
        } else if (upperDecl.startsWith("VARIADIC ")) {
            return "VARIADIC";
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