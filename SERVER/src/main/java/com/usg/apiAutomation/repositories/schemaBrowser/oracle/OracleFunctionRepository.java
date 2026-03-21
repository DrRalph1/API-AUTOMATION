package com.usg.apiAutomation.repositories.schemaBrowser.oracle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OracleFunctionRepository extends OracleRepository {

    // ============================================================
    // FUNCTION METHODS
    // ============================================================

    public List<Map<String, Object>> getAllFunctions() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String functionSql = "SELECT object_name as name, 'FUNCTION' as type, status, created, last_ddl_time, " +
                    "(SELECT COUNT(*) FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL " +
                    "AND argument_name IS NOT NULL) as parameter_count, " +
                    "(SELECT data_type FROM user_arguments WHERE object_name = o.object_name AND package_name IS NULL " +
                    "AND argument_name IS NULL AND ROWNUM = 1) as return_type " +
                    "FROM user_objects o WHERE object_type = 'FUNCTION' ORDER BY object_name";

            List<Map<String, Object>> functions = getJdbcTemplate().queryForList(functionSql);

            for (Map<String, Object> func : functions) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "function-" + System.currentTimeMillis() + "-" + func.get("name"));
                transformed.put("name", func.get("name"));
                transformed.put("owner", getCurrentUser());
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

            result.addAll(getFunctionSynonyms());
            result.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            log.info("Returning {} total items (functions + synonyms)", result.size());

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
                    "    object_name as name, " +
                    "    'FUNCTION' as type, " +
                    "    status, " +
                    "    created, " +
                    "    last_ddl_time, " +
                    "    (SELECT COUNT(*) FROM user_arguments " +
                    "     WHERE object_name = o.object_name " +
                    "       AND package_name IS NULL " +
                    "       AND argument_name IS NOT NULL) as parameter_count, " +
                    "    (SELECT data_type FROM user_arguments " +
                    "     WHERE object_name = o.object_name " +
                    "       AND package_name IS NULL " +
                    "       AND argument_name IS NULL " +
                    "       AND ROWNUM = 1) as return_type, " +
                    "    NULL as target_owner, " +
                    "    NULL as target_name, " +
                    "    NULL as target_type, " +
                    "    NULL as db_link " +
                    "FROM user_objects o " +
                    "WHERE object_type = 'FUNCTION' " +
                    "ORDER BY object_name";

            List<Map<String, Object>> functions = getJdbcTemplate().queryForList(functionSql);
            log.info("Found {} functions", functions.size());

            for (Map<String, Object> func : functions) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "function-" + System.currentTimeMillis() + "-" + func.get("name"));
                transformed.put("name", func.get("name"));
                transformed.put("function_name", func.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "FUNCTION");
                transformed.put("object_type", "FUNCTION");
                transformed.put("status", func.get("status"));
                transformed.put("parameterCount", func.get("parameter_count") != null ? func.get("parameter_count") : 0);
                transformed.put("returnType", func.get("return_type"));
                transformed.put("created", func.get("created"));
                transformed.put("lastModified", func.get("last_ddl_time"));
                transformed.put("icon", "function");
                transformed.put("isSynonym", false);
                result.add(transformed);
            }

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
                            "    NULL as return_type, " +
                            "    s.table_owner as target_owner, " +
                            "    s.table_name as target_name, " +
                            "    CASE " +
                            "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_FUNCTION' " +
                            "        ELSE o.object_type " +
                            "    END as target_type, " +
                            "    s.db_link " +
                            "FROM user_synonyms s " +
                            "LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                            "    AND s.table_name = o.object_name " +
                            "WHERE (o.object_type = 'FUNCTION' OR s.db_link IS NOT NULL) " +
                            "ORDER BY s.synonym_name";

            try {
                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);
                log.info("Found {} synonyms targeting functions", synonyms.size());

                for (Map<String, Object> syn : synonyms) {
                    Map<String, Object> transformed = new HashMap<>();
                    transformed.put("id", "syn-func-" + System.currentTimeMillis() + "-" + syn.get("name"));
                    transformed.put("name", syn.get("name"));
                    transformed.put("function_name", syn.get("name"));
                    transformed.put("owner", getCurrentUser());
                    transformed.put("type", "SYNONYM");
                    transformed.put("object_type", "SYNONYM");
                    transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                    transformed.put("parameterCount", 0);
                    transformed.put("returnType", null);
                    transformed.put("created", null);
                    transformed.put("lastModified", null);
                    transformed.put("targetOwner", syn.get("target_owner"));
                    transformed.put("targetName", syn.get("target_name"));
                    transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "FUNCTION");
                    transformed.put("dbLink", syn.get("db_link"));
                    transformed.put("isRemote", syn.get("db_link") != null);
                    transformed.put("isSynonym", true);
                    transformed.put("icon", "synonym");
                    transformed.put("targetIcon", "function");

                    String targetType = (String) syn.get("target_type");
                    if (targetType != null) {
                        transformed.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                    } else {
                        transformed.put("targetDisplayType", "Function");
                    }

                    if (syn.get("db_link") == null && syn.get("target_name") != null) {
                        try {
                            String targetSql = "SELECT status, created, last_ddl_time " +
                                    "FROM all_objects " +
                                    "WHERE owner = ? AND object_name = ? AND object_type = 'FUNCTION'";
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
                log.error("Error fetching synonyms for functions: {}", e.getMessage());
            }

            result.sort((a, b) -> {
                String nameA = (String) a.get("name");
                String nameB = (String) b.get("name");
                return nameA.compareTo(nameB);
            });

            log.info("Returning {} total items (functions + synonyms)", result.size());

        } catch (Exception e) {
            log.error("Error in getAllFunctionsForFrontend: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        return result;
    }



    public Map<String, Object> getFunctionDetails(String owner, String functionName) {
        Map<String, Object> details = new HashMap<>();
        try {
            boolean isPackageFunction = checkIfPackageFunction(owner, functionName);
            String packageName = null;

            if (isPackageFunction) {
                packageName = getPackageNameForFunction(owner, functionName);
                details.put("package_name", packageName);
                details.put("is_package_function", true);
            }

            // Get basic object info
            String sql = "SELECT object_name, object_type, status, created, last_ddl_time, temporary, generated, secondary " +
                    "FROM all_objects WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?)";
            if (!isPackageFunction) {
                sql += " AND object_type = 'FUNCTION'";
            }

            try {
                Map<String, Object> funcInfo = getJdbcTemplate().queryForMap(sql, owner, functionName);
                details.putAll(funcInfo);
            } catch (Exception e) {
                log.debug("Could not get basic info for {}.{}", owner, functionName);
            }

            // Get parameters and return type
            Map<String, Object> paramsAndReturn = getFunctionParametersAndReturn(owner, functionName, isPackageFunction, packageName);
            details.put("parameters", paramsAndReturn.get("parameters"));
            details.put("parameterCount", ((List) paramsAndReturn.get("parameters")).size());
            details.put("returnType", paramsAndReturn.get("returnType"));

            // Get source code
            String source = getFunctionSource(owner, functionName, isPackageFunction, packageName);
            if (source != null) {
                details.put("sourceCode", source);
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
            boolean isPackageFunction = checkIfPackageFunction(owner, functionName);
            String packageName = isPackageFunction ? getPackageNameForFunction(owner, functionName) : null;

            Map<String, Object> paramsAndReturn = getFunctionParametersAndReturn(owner, functionName, isPackageFunction, packageName);
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

    private List<Map<String, Object>> getFunctionSynonyms() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String synonymSql = "SELECT s.synonym_name as name, 'SYNONYM' as type, " +
                    "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE o.status END as status, " +
                    "NULL as created, NULL as last_ddl_time, 0 as parameter_count, NULL as return_type, " +
                    "s.table_owner as target_owner, s.table_name as target_name, " +
                    "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE_FUNCTION' ELSE o.object_type END as target_type, s.db_link " +
                    "FROM user_synonyms s LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                    "WHERE (o.object_type = 'FUNCTION' OR s.db_link IS NOT NULL) ORDER BY s.synonym_name";

            List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql);

            for (Map<String, Object> syn : synonyms) {
                Map<String, Object> transformed = new HashMap<>();
                transformed.put("id", "syn-func-" + System.currentTimeMillis() + "-" + syn.get("name"));
                transformed.put("name", syn.get("name"));
                transformed.put("owner", getCurrentUser());
                transformed.put("type", "SYNONYM");
                transformed.put("status", syn.get("status") != null ? syn.get("status") : "VALID");
                transformed.put("parameterCount", 0);
                transformed.put("targetOwner", syn.get("target_owner"));
                transformed.put("targetName", syn.get("target_name"));
                transformed.put("targetType", syn.get("target_type") != null ? syn.get("target_type") : "FUNCTION");
                transformed.put("dbLink", syn.get("db_link"));
                transformed.put("isRemote", syn.get("db_link") != null);
                transformed.put("isSynonym", true);
                transformed.put("icon", "synonym");
                transformed.put("targetIcon", "function");
                result.add(transformed);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for functions: {}", e.getMessage());
        }
        return result;
    }

    private int countFunctionSynonyms() {
        try {
            String synonymCountSql = "SELECT COUNT(*) FROM user_synonyms s WHERE " +
                    "EXISTS (SELECT 1 FROM all_objects WHERE owner = s.table_owner AND object_name = s.table_name AND object_type = 'FUNCTION') " +
                    "OR (s.db_link IS NOT NULL)";
            return getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
        } catch (Exception e) {
            log.warn("Error counting synonyms for functions: {}", e.getMessage());
            return 0;
        }
    }

    private void addFunctionSynonymsPaginated(List<Map<String, Object>> allItems, int page, int pageSize, int functionCount) {
        try {
            int offset = (page - 1) * pageSize;
            int synOffset;
            int synLimit;

            if (offset >= functionCount) {
                synOffset = offset - functionCount;
                synLimit = pageSize;
            } else {
                synOffset = 0;
                synLimit = pageSize - allItems.size();
            }

            if (synLimit > 0) {
                String synonymSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT s.synonym_name as name, " +
                        "'SYNONYM' as type, CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE' ELSE o.status END as status, " +
                        "NULL as created, NULL as last_ddl_time, 0 as parameter_count, NULL as return_type, " +
                        "s.table_owner as target_owner, s.table_name as target_name, " +
                        "CASE WHEN s.db_link IS NOT NULL THEN 'REMOTE_FUNCTION' ELSE o.object_type END as target_type, s.db_link " +
                        "FROM user_synonyms s LEFT JOIN all_objects o ON s.table_owner = o.owner AND s.table_name = o.object_name " +
                        "WHERE (o.object_type = 'FUNCTION' OR s.db_link IS NOT NULL) ORDER BY s.synonym_name) a " +
                        ") WHERE rnum > ? AND rnum <= ?";

                List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(synonymSql, synOffset, synOffset + synLimit);
                allItems.addAll(synonyms);
            }
        } catch (Exception e) {
            log.error("Error fetching synonyms for functions: {}", e.getMessage());
        }
    }

    private List<Map<String, Object>> transformFunctionItems(List<Map<String, Object>> items) {
        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> transformedItem = new HashMap<>();
            String type = (String) item.get("type");
            String name = (String) item.get("name");

            if ("FUNCTION".equals(type)) {
                transformedItem.put("id", "function-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "FUNCTION");
                transformedItem.put("status", item.get("status"));
                transformedItem.put("parameterCount", item.get("parameter_count") != null ? item.get("parameter_count") : 0);
                transformedItem.put("returnType", item.get("return_type"));
                transformedItem.put("created", item.get("created"));
                transformedItem.put("lastModified", item.get("last_ddl_time"));
                transformedItem.put("icon", "function");
                transformedItem.put("isSynonym", false);
            } else {
                transformedItem.put("id", "syn-func-" + System.currentTimeMillis() + "-" + name);
                transformedItem.put("name", name);
                transformedItem.put("owner", getCurrentUser());
                transformedItem.put("type", "SYNONYM");
                transformedItem.put("status", item.get("status") != null ? item.get("status") : "VALID");
                transformedItem.put("parameterCount", 0);
                transformedItem.put("returnType", null);
                transformedItem.put("targetOwner", item.get("target_owner"));
                transformedItem.put("targetName", item.get("target_name"));
                transformedItem.put("targetType", item.get("target_type") != null ? item.get("target_type") : "FUNCTION");
                transformedItem.put("dbLink", item.get("db_link"));
                transformedItem.put("isRemote", item.get("db_link") != null);
                transformedItem.put("isSynonym", true);
                transformedItem.put("icon", "synonym");
                transformedItem.put("targetIcon", "function");
            }
            transformed.add(transformedItem);
        }
        return transformed;
    }

    private boolean checkIfPackageFunction(String owner, String functionName) {
        try {
            String sql = "SELECT COUNT(*) FROM all_arguments WHERE UPPER(owner) = UPPER(?) " +
                    "AND UPPER(object_name) = UPPER(?) AND package_name IS NOT NULL AND ROWNUM = 1";
            int count = getJdbcTemplate().queryForObject(sql, Integer.class, owner, functionName);
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String getPackageNameForFunction(String owner, String functionName) {
        try {
            String sql = "SELECT package_name FROM all_arguments WHERE UPPER(owner) = UPPER(?) " +
                    "AND UPPER(object_name) = UPPER(?) AND package_name IS NOT NULL AND ROWNUM = 1";
            return getJdbcTemplate().queryForObject(sql, String.class, owner, functionName);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> getFunctionParametersAndReturn(String owner, String functionName,
                                                               boolean isPackageFunction, String packageName) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> returnType = new HashMap<>();

        try {
            String sql;
            List<Map<String, Object>> allArgs;

            if (isPackageFunction && packageName != null) {
                sql = "SELECT argument_name, position, sequence, data_type, in_out, " +
                        "data_length, data_precision, data_scale, defaulted " +
                        "FROM all_arguments WHERE UPPER(owner) = UPPER(?) AND UPPER(package_name) = UPPER(?) " +
                        "AND UPPER(object_name) = UPPER(?) ORDER BY position, sequence";
                allArgs = getJdbcTemplate().queryForList(sql, owner, packageName, functionName);
            } else {
                sql = "SELECT argument_name, position, sequence, data_type, in_out, " +
                        "data_length, data_precision, data_scale, defaulted " +
                        "FROM all_arguments WHERE UPPER(owner) = UPPER(?) AND UPPER(object_name) = UPPER(?) " +
                        "AND package_name IS NULL ORDER BY position, sequence";
                allArgs = getJdbcTemplate().queryForList(sql, owner, functionName);
            }

            for (Map<String, Object> arg : allArgs) {
                Number position = (Number) arg.get("position");
                if (position != null && position.intValue() == 0) {
                    returnType.put("data_type", arg.get("data_type"));
                    returnType.put("data_length", arg.get("data_length"));
                    returnType.put("data_precision", arg.get("data_precision"));
                    returnType.put("data_scale", arg.get("data_scale"));
                } else if (arg.get("argument_name") != null) {
                    params.add(arg);
                }
            }

            if (params.isEmpty()) {
                params = parseFunctionParametersFromSource(owner, functionName);
                if (returnType.isEmpty()) {
                    returnType = parseFunctionReturnTypeFromSource(owner, functionName);
                }
            }

        } catch (Exception e) {
            log.debug("Error getting function parameters: {}", e.getMessage());
        }

        result.put("parameters", params);
        result.put("returnType", returnType.get("data_type"));
        return result;
    }

    private List<Map<String, Object>> parseFunctionParametersFromSource(String owner, String functionName) {
        List<Map<String, Object>> params = new ArrayList<>();
        try {
            String sourceSql = "SELECT text FROM all_source WHERE UPPER(owner) = UPPER(?) " +
                    "AND UPPER(name) = UPPER(?) AND UPPER(type) = 'FUNCTION' ORDER BY line";
            List<String> sourceLines = getJdbcTemplate().queryForList(sourceSql, String.class, owner, functionName);

            if (sourceLines.isEmpty()) return params;

            StringBuilder fullSource = new StringBuilder();
            for (String line : sourceLines) fullSource.append(line).append(" ");
            String source = removeComments(fullSource.toString());

            String patternStr = "FUNCTION\\s+" + functionName + "\\s*\\((.*?)\\)\\s*RETURN";
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);

            if (matcher.find()) {
                String paramsSection = matcher.group(1).trim();
                if (!paramsSection.isEmpty()) {
                    List<String> paramDeclarations = splitParametersByComma(paramsSection);
                    int position = 1;
                    for (String paramDecl : paramDeclarations) {
                        Map<String, Object> param = parseFunctionParameterFromString(paramDecl, position++);
                        if (param != null && !param.isEmpty()) params.add(param);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing function parameters from source: {}", e.getMessage());
        }
        return params;
    }

    private Map<String, Object> parseFunctionReturnTypeFromSource(String owner, String functionName) {
        Map<String, Object> returnType = new HashMap<>();
        try {
            String sourceSql = "SELECT text FROM all_source WHERE UPPER(owner) = UPPER(?) " +
                    "AND UPPER(name) = UPPER(?) AND UPPER(type) = 'FUNCTION' ORDER BY line";
            List<String> sourceLines = getJdbcTemplate().queryForList(sourceSql, String.class, owner, functionName);

            if (sourceLines.isEmpty()) return returnType;

            StringBuilder fullSource = new StringBuilder();
            for (String line : sourceLines) fullSource.append(line).append(" ");
            String source = removeComments(fullSource.toString());

            Pattern pattern = Pattern.compile("FUNCTION\\s+" + functionName + "\\s*\\([\\s\\S]*?\\)\\s*RETURN\\s+(\\w+(?:\\([^)]*\\))?)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);
            if (matcher.find()) {
                returnType.put("data_type", matcher.group(1).trim());
            }
        } catch (Exception e) {
            log.error("Error parsing function return type: {}", e.getMessage());
        }
        return returnType;
    }

    private Map<String, Object> parseFunctionParameterFromString(String paramDecl, int position) {
        paramDecl = paramDecl.replaceAll("--.*$", "").trim();
        Map<String, Object> param = new HashMap<>();
        Pattern pattern = Pattern.compile("^(\\w+)\\s+(IN\\s+OUT|IN|OUT)?\\s*(.*?)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(paramDecl);
        if (matcher.find()) {
            param.put("argument_name", matcher.group(1));
            param.put("position", position);
            param.put("in_out", matcher.group(2) != null ? matcher.group(2).trim().toUpperCase() : "IN");
            param.put("data_type", matcher.group(3).trim().replaceAll("[,;]$", "").toUpperCase());
        }
        return param.isEmpty() ? null : param;
    }

    public String removeComments(String source) {
        StringBuilder result = new StringBuilder();
        String[] lines = source.split("\\n");
        for (String line : lines) {
            boolean inQuotes = false;
            int commentStart = -1;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\'') inQuotes = !inQuotes;
                else if (!inQuotes && c == '-' && i + 1 < line.length() && line.charAt(i + 1) == '-') {
                    commentStart = i;
                    break;
                }
            }
            if (commentStart != -1) result.append(line.substring(0, commentStart));
            else result.append(line);
            result.append("\n");
        }
        return result.toString();
    }

    public List<String> splitParametersByComma(String text) {
        List<String> parameters = new ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        int parenCount = 0;
        boolean inQuotes = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') inQuotes = !inQuotes;
            if (!inQuotes) {
                if (c == '(') parenCount++;
                else if (c == ')') parenCount--;
                else if (c == ',' && parenCount == 0) {
                    String param = currentParam.toString().trim();
                    if (!param.isEmpty()) parameters.add(param);
                    currentParam = new StringBuilder();
                    continue;
                }
            }
            currentParam.append(c);
        }
        String lastParam = currentParam.toString().trim();
        if (!lastParam.isEmpty()) parameters.add(lastParam);
        return parameters;
    }

    private String getFunctionSource(String owner, String functionName, boolean isPackageFunction, String packageName) {
        try {
            if (isPackageFunction && packageName != null) {
                String sql = "SELECT text FROM all_source WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                        "AND UPPER(type) IN ('PACKAGE', 'PACKAGE BODY') ORDER BY line";
                List<String> lines = getJdbcTemplate().queryForList(sql, String.class, owner, packageName);
                return lines.isEmpty() ? null : String.join("", lines);
            } else {
                String sql = "SELECT text FROM all_source WHERE UPPER(owner) = UPPER(?) AND UPPER(name) = UPPER(?) " +
                        "AND UPPER(type) = 'FUNCTION' ORDER BY line";
                List<String> lines = getJdbcTemplate().queryForList(sql, String.class, owner, functionName);
                return lines.isEmpty() ? null : String.join("", lines);
            }
        } catch (Exception e) {
            return null;
        }
    }



    public Map<String, Object> getFunctionsPaginated(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) FROM user_objects WHERE object_type = 'FUNCTION'";
            int totalCount = getJdbcTemplate().queryForObject(countSql, Integer.class);

            String dataSql = "SELECT object_name as function_name, status, created, last_ddl_time " +
                    "FROM user_objects WHERE object_type = 'FUNCTION' " +
                    "ORDER BY object_name OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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


    public Map<String, Object> getAllFunctionsForFrontend(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;

            String functionCountSql = "SELECT COUNT(*) FROM user_objects WHERE object_type = 'FUNCTION'";
            int functionCount = getJdbcTemplate().queryForObject(functionCountSql, Integer.class);

            String synonymCountSql =
                    "SELECT COUNT(*) FROM user_synonyms s " +
                            "WHERE EXISTS (SELECT 1 FROM all_objects " +
                            "              WHERE owner = s.table_owner " +
                            "                AND object_name = s.table_name " +
                            "                AND object_type = 'FUNCTION') " +
                            "   OR (s.db_link IS NOT NULL)";

            int synonymCount = 0;
            try {
                synonymCount = getJdbcTemplate().queryForObject(synonymCountSql, Integer.class);
            } catch (Exception e) {
                log.warn("Error counting synonyms for functions: {}", e.getMessage());
            }

            int totalCount = functionCount + synonymCount;
            log.info("Found {} functions and {} synonyms targeting functions, total: {}", functionCount, synonymCount, totalCount);

            List<Map<String, Object>> allItems = new ArrayList<>();

            if (functionCount > 0 && offset < functionCount) {
                int funcOffset = offset;
                int funcLimit = Math.min(pageSize, functionCount - funcOffset);

                if (funcLimit > 0) {
                    String functionSql = "SELECT " +
                            "    object_name as name, " +
                            "    'FUNCTION' as type, " +
                            "    status, " +
                            "    created, " +
                            "    last_ddl_time, " +
                            "    (SELECT COUNT(*) FROM user_arguments " +
                            "     WHERE object_name = o.object_name " +
                            "       AND package_name IS NULL " +
                            "       AND argument_name IS NOT NULL) as parameter_count, " +
                            "    (SELECT data_type FROM user_arguments " +
                            "     WHERE object_name = o.object_name " +
                            "       AND package_name IS NULL " +
                            "       AND argument_name IS NULL " +
                            "       AND ROWNUM = 1) as return_type, " +
                            "    NULL as target_owner, " +
                            "    NULL as target_name, " +
                            "    NULL as target_type, " +
                            "    NULL as db_link " +
                            "FROM user_objects o " +
                            "WHERE object_type = 'FUNCTION' " +
                            "ORDER BY object_name " +
                            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                    try {
                        List<Map<String, Object>> functions = getJdbcTemplate().queryForList(functionSql, funcOffset, funcLimit);
                        allItems.addAll(functions);
                    } catch (Exception e) {
                        log.error("Error fetching functions: {}", e.getMessage());
                    }
                }
            }

            if (allItems.size() < pageSize && synonymCount > 0) {
                int synOffset;
                int synLimit;

                if (offset >= functionCount) {
                    synOffset = offset - functionCount;
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
                                    "      NULL as return_type, " +
                                    "      s.table_owner as target_owner, " +
                                    "      s.table_name as target_name, " +
                                    "      CASE " +
                                    "        WHEN s.db_link IS NOT NULL THEN 'REMOTE_FUNCTION' " +
                                    "        ELSE o.object_type " +
                                    "      END as target_type, " +
                                    "      s.db_link " +
                                    "    FROM user_synonyms s " +
                                    "    LEFT JOIN all_objects o ON s.table_owner = o.owner " +
                                    "        AND s.table_name = o.object_name " +
                                    "    WHERE (o.object_type = 'FUNCTION' OR s.db_link IS NOT NULL) " +
                                    "    ORDER BY s.synonym_name " +
                                    "  ) a " +
                                    ") WHERE rnum > ? AND rnum <= ?";

                    try {
                        List<Map<String, Object>> synonyms = getJdbcTemplate().queryForList(
                                synonymSql, synOffset, synOffset + synLimit);
                        allItems.addAll(synonyms);
                    } catch (Exception e) {
                        log.error("Error fetching synonyms for functions: {}", e.getMessage());
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
                    transformedItem.put("owner", getCurrentUser());
                    transformedItem.put("type", "FUNCTION");
                    transformedItem.put("status", item.get("status"));
                    transformedItem.put("parameterCount", item.get("parameter_count") != null ? item.get("parameter_count") : 0);
                    transformedItem.put("returnType", item.get("return_type"));
                    transformedItem.put("created", item.get("created"));
                    transformedItem.put("lastModified", item.get("last_ddl_time"));
                    transformedItem.put("icon", "function");
                    transformedItem.put("isSynonym", false);
                } else {
                    transformedItem.put("id", "syn-func-" + System.currentTimeMillis() + "-" + name);
                    transformedItem.put("name", name);
                    transformedItem.put("function_name", name);
                    transformedItem.put("owner", getCurrentUser());
                    transformedItem.put("type", "SYNONYM");
                    transformedItem.put("status", item.get("status") != null ? item.get("status") : "VALID");
                    transformedItem.put("parameterCount", 0);
                    transformedItem.put("returnType", null);
                    transformedItem.put("created", null);
                    transformedItem.put("lastModified", null);
                    transformedItem.put("targetOwner", item.get("target_owner"));
                    transformedItem.put("targetName", item.get("target_name"));
                    transformedItem.put("targetType", item.get("target_type") != null ? item.get("target_type") : "FUNCTION");
                    transformedItem.put("dbLink", item.get("db_link"));
                    transformedItem.put("isRemote", item.get("db_link") != null);
                    transformedItem.put("isSynonym", true);
                    transformedItem.put("icon", "synonym");
                    transformedItem.put("targetIcon", "function");

                    String targetType = (String) item.get("target_type");
                    if (targetType != null) {
                        transformedItem.put("targetDisplayType", formatObjectTypeForDisplay(targetType));
                    } else {
                        transformedItem.put("targetDisplayType", "Function");
                    }
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
     * Get function details
     */
    public Map<String, Object> getFunctionDetails(String functionName) {
        try {
            Map<String, Object> funcLocation = findObjectLocation(functionName, "FUNCTION");
            String owner = (String) funcLocation.get("owner");

            if (owner == null) {
                owner = getCurrentUser();
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