package com.usg.autoAPIGenerator.utils.apiEngine.generator;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.QueryColumnDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.QueryParameterDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CustomQueryParserUtil {

    private static final Pattern PARAMETER_PATTERN = Pattern.compile(":(\\w+)");
    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile("@(\\w+)");
    private static final Pattern POSITIONAL_PARAMETER_PATTERN = Pattern.compile("\\?(\\d*)");
    private static final Pattern FROM_PATTERN = Pattern.compile("\\bFROM\\s+([^\\s]+(?:\\s*,\\s*[^\\s]+)*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bWHERE\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GROUP_BY_PATTERN = Pattern.compile("\\bGROUP\\s+BY\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAVING_PATTERN = Pattern.compile("\\bHAVING\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_BY_PATTERN = Pattern.compile("\\bORDER\\s+BY\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SELECT_COLUMNS_PATTERN = Pattern.compile("^SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern WITH_CLAUSE_PATTERN = Pattern.compile("\\bWITH\\s+(.+?)\\s+AS\\s*\\(.+?\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern UNION_PATTERN = Pattern.compile("\\bUNION\\s+(ALL\\s+)?SELECT", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\bLIMIT\\s+(\\d+)(?:\\s+OFFSET\\s+(\\d+))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern OFFSET_PATTERN = Pattern.compile("\\bOFFSET\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FETCH_PATTERN = Pattern.compile("\\bFETCH\\s+FIRST\\s+(\\d+)\\s+ROWS\\s+ONLY", Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate oracleJdbcTemplate;
    private final JdbcTemplate postgresqlJdbcTemplate;

    public CustomQueryParserUtil(
            @Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbcTemplate,
            @Qualifier("postgresqlJdbcTemplate") JdbcTemplate postgresqlJdbcTemplate) {
        this.oracleJdbcTemplate = oracleJdbcTemplate;
        this.postgresqlJdbcTemplate = postgresqlJdbcTemplate;
    }

    /**
     * Parse a custom SELECT statement and extract metadata
     */
    public Map<String, Object> parseCustomQuery(String selectStatement, String databaseType) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Clean and normalize the query
            String normalizedQuery = normalizeQuery(selectStatement);

            // Extract parameters from multiple formats (:param, @param, ?)
            List<QueryParameterDTO> parameters = extractAllParameters(normalizedQuery);
            result.put("parameters", parameters);

            // Extract column information - pass databaseType to use correct template
            List<QueryColumnDTO> columns = extractColumnMetadata(normalizedQuery, databaseType);
            result.put("columns", columns);

            // Extract query clauses
            result.put("fromClause", extractFromClause(normalizedQuery));
            result.put("whereClause", extractWhereClause(normalizedQuery));
            result.put("groupByClause", extractGroupByClause(normalizedQuery));
            result.put("havingClause", extractHavingClause(normalizedQuery));
            result.put("orderByClause", extractOrderByClause(normalizedQuery));

            // Extract pagination clauses
            result.put("limitClause", extractLimitClause(normalizedQuery));
            result.put("offsetClause", extractOffsetClause(normalizedQuery));

            // Extract WITH clause (CTE)
            result.put("withClause", extractWithClause(normalizedQuery));

            // Check if query has UNION
            result.put("hasUnion", UNION_PATTERN.matcher(normalizedQuery).find());

            // Extract source tables
            List<String> sourceTables = extractSourceTables(normalizedQuery);
            result.put("sourceTables", sourceTables);

            // Check if query has parameters
            result.put("hasParameters", !parameters.isEmpty());

            // Get column count
            result.put("columnCount", columns.size());

            // Generate a query alias
            result.put("queryAlias", generateQueryAlias(selectStatement));

            // Detect query type (SELECT, INSERT, UPDATE, DELETE)
            result.put("queryType", detectQueryType(normalizedQuery));

            // Detect if it's a complex query
            result.put("isComplexQuery", isComplexQuery(normalizedQuery));

            // Estimate query complexity
            result.put("complexity", estimateQueryComplexity(normalizedQuery));

            log.info("Parsed custom query: {} parameters, {} columns, {} source tables, complexity: {}",
                    parameters.size(), columns.size(), sourceTables.size(), result.get("complexity"));

        } catch (Exception e) {
            log.error("Error parsing custom query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse custom SELECT statement: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Extract parameters from the query (supports :paramName, @paramName, and ?)
     */
    private List<QueryParameterDTO> extractAllParameters(String query) {
        List<QueryParameterDTO> parameters = new ArrayList<>();
        Set<String> uniqueParams = new LinkedHashSet<>();

        // Extract :paramName style parameters
        Matcher colonMatcher = PARAMETER_PATTERN.matcher(query);
        while (colonMatcher.find()) {
            uniqueParams.add(colonMatcher.group(1));
        }

        // Extract @paramName style parameters
        Matcher atMatcher = NAMED_PARAMETER_PATTERN.matcher(query);
        while (atMatcher.find()) {
            uniqueParams.add(atMatcher.group(1));
        }

        // Extract positional parameters (?)
        Matcher positionalMatcher = POSITIONAL_PARAMETER_PATTERN.matcher(query);
        int positionalCount = 0;
        while (positionalMatcher.find()) {
            positionalCount++;
        }

        int position = 1;
        for (String paramName : uniqueParams) {
            QueryParameterDTO param = QueryParameterDTO.builder()
                    .parameterName(paramName)
                    .parameterType("IN")
                    .dataType("VARCHAR2")
                    .dataTypeClass("string")
                    .isRequired(true)
                    .position(position++)
                    .description("Parameter: " + paramName)
                    .build();
            parameters.add(param);
        }

        // Add positional parameters as generic names if present
        for (int i = 0; i < positionalCount; i++) {
            QueryParameterDTO param = QueryParameterDTO.builder()
                    .parameterName("param" + (i + 1))
                    .parameterType("IN")
                    .dataType("VARCHAR2")
                    .dataTypeClass("string")
                    .isRequired(true)
                    .position(position++)
                    .description("Positional parameter " + (i + 1))
                    .build();
            parameters.add(param);
        }

        return parameters;
    }

    /**
     * Extract parameters from the query (e.g., :paramName) - legacy method
     */
    private List<QueryParameterDTO> extractParameters(String query) {
        return extractAllParameters(query);
    }

    /**
     * Extract column metadata using JDBC - FIXED to handle parameterized queries
     */
    private List<QueryColumnDTO> extractColumnMetadata(String query, String databaseType) {
        List<QueryColumnDTO> columns = new ArrayList<>();

        // Select the appropriate JdbcTemplate based on database type
        JdbcTemplate templateToUse = "postgresql".equalsIgnoreCase(databaseType) ?
                postgresqlJdbcTemplate : oracleJdbcTemplate;

        if (templateToUse == null) {
            log.warn("No JdbcTemplate available for database type: {}, falling back to regex extraction", databaseType);
            return extractColumnsFromQuery(query);
        }

        // Remove named parameters (e.g., :paramName, @paramName) and replace with NULL values for metadata extraction
        String queryForMetadata = removeAllParametersForMetadata(query);
        log.debug("Query for metadata: {}", queryForMetadata);

        // Wrap the query to get metadata without executing
        String metadataQuery = "SELECT * FROM (" + queryForMetadata + ") t WHERE 1=0";

        try (Connection conn = templateToUse.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(metadataQuery);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                QueryColumnDTO column = QueryColumnDTO.builder()
                        .columnName(metaData.getColumnName(i))
                        .alias(metaData.getColumnLabel(i))
                        .dataType(metaData.getColumnTypeName(i))
                        .dataTypeClass(getJavaTypeForSqlType(metaData.getColumnType(i)))
                        .dataLength(metaData.getColumnDisplaySize(i))
                        .dataPrecision(metaData.getPrecision(i))
                        .dataScale(metaData.getScale(i))
                        .isNullable(metaData.isNullable(i) == ResultSetMetaData.columnNullable)
                        .isPrimaryKey(false)
                        .position(i)
                        .build();
                columns.add(column);
            }

        } catch (SQLException e) {
            log.warn("Could not extract column metadata using JDBC for {}: {}", databaseType, e.getMessage());
            // Fallback to regex-based column extraction
            columns = extractColumnsFromQuery(query);
        }

        return columns;
    }

    /**
     * Remove all named parameters for metadata extraction
     * Replaces :paramName, @paramName with appropriate NULL values
     */
    private String removeAllParametersForMetadata(String query) {
        String result = query;

        // Remove :paramName style parameters
        Matcher colonMatcher = PARAMETER_PATTERN.matcher(result);
        Set<String> colonParams = new HashSet<>();
        while (colonMatcher.find()) {
            colonParams.add(colonMatcher.group(1));
        }
        for (String param : colonParams) {
            result = result.replaceAll(":" + param + "\\b", "NULL");
        }

        // Remove @paramName style parameters
        Matcher atMatcher = NAMED_PARAMETER_PATTERN.matcher(result);
        Set<String> atParams = new HashSet<>();
        while (atMatcher.find()) {
            atParams.add(atMatcher.group(1));
        }
        for (String param : atParams) {
            result = result.replaceAll("@" + param + "\\b", "NULL");
        }

        // Remove positional parameters (?)
        result = result.replaceAll("\\?\\d*", "NULL");

        return result;
    }

    /**
     * Remove named parameters for metadata extraction - legacy method
     */
    private String removeNamedParametersForMetadata(String query) {
        return removeAllParametersForMetadata(query);
    }

    /**
     * Get column types for SELECT queries by parsing the query and extracting from database schema
     */
    public Map<String, String> getColumnTypeMapForSelect(String query, String databaseType) {
        Map<String, String> columnTypes = new HashMap<>();

        try {
            // Extract table name from query
            String tableName = extractTableNameFromSelect(query);
            if (tableName == null) {
                log.warn("Could not extract table name from SELECT query: {}", query);
                return columnTypes;
            }

            columnTypes.putAll(getAllColumnTypesForTable(tableName, databaseType));
            log.debug("Extracted column types for table {}: {}", tableName, columnTypes);

        } catch (Exception e) {
            log.warn("Could not extract column types from information_schema: {}", e.getMessage());
        }

        return columnTypes;
    }

    /**
     * Get all column types for a given table
     */
    private Map<String, String> getAllColumnTypesForTable(String tableName, String databaseType) {
        Map<String, String> columnTypes = new HashMap<>();

        JdbcTemplate templateToUse = "postgresql".equalsIgnoreCase(databaseType) ?
                postgresqlJdbcTemplate : oracleJdbcTemplate;

        if (templateToUse == null) {
            return columnTypes;
        }

        try {
            String typeQuery;
            if ("postgresql".equalsIgnoreCase(databaseType)) {
                typeQuery = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ?";
            } else {
                typeQuery = "SELECT column_name, data_type FROM all_tab_columns WHERE table_name = ?";
            }

            List<Map<String, Object>> columns = templateToUse.queryForList(typeQuery, tableName.toLowerCase());
            for (Map<String, Object> column : columns) {
                String columnName = ((String) column.get("column_name")).toLowerCase();
                String dataType = ((String) column.get("data_type")).toUpperCase();
                columnTypes.put(columnName, dataType);
            }
        } catch (Exception e) {
            log.warn("Failed to get column types for table {}: {}", tableName, e.getMessage());
        }

        return columnTypes;
    }

    /**
     * Extract table name from SELECT query (handles aliases and multiple tables)
     */
    private String extractTableNameFromSelect(String query) {
        // Remove comments and normalize
        String normalized = query.replaceAll("--[^\n]*", "").replaceAll("/\\*.*?\\*/", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        // Pattern to match FROM clause and extract table name (handle schema.table format)
        Pattern fromPattern = Pattern.compile("\\bFROM\\s+([\\w\\.]+)(?:\\s+(?:AS\\s+)?(\\w+))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = fromPattern.matcher(normalized);

        if (matcher.find()) {
            String tableWithSchema = matcher.group(1);
            // Handle schema.table format
            String[] parts = tableWithSchema.split("\\.");
            return parts[parts.length - 1];
        }

        return null;
    }

    /**
     * Extract table name from INSERT/UPDATE/DELETE query
     */
    private String extractTableNameFromDML(String query) {
        String upperQuery = query.toUpperCase().trim();

        if (upperQuery.startsWith("INSERT")) {
            Pattern pattern = Pattern.compile("INSERT\\s+INTO\\s+([\\w\\.]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(query);
            if (matcher.find()) {
                String tableWithSchema = matcher.group(1);
                String[] parts = tableWithSchema.split("\\.");
                return parts[parts.length - 1];
            }
        } else if (upperQuery.startsWith("UPDATE")) {
            Pattern pattern = Pattern.compile("UPDATE\\s+([\\w\\.]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(query);
            if (matcher.find()) {
                String tableWithSchema = matcher.group(1);
                String[] parts = tableWithSchema.split("\\.");
                return parts[parts.length - 1];
            }
        } else if (upperQuery.startsWith("DELETE")) {
            Pattern pattern = Pattern.compile("DELETE\\s+FROM\\s+([\\w\\.]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(query);
            if (matcher.find()) {
                String tableWithSchema = matcher.group(1);
                String[] parts = tableWithSchema.split("\\.");
                return parts[parts.length - 1];
            }
        }

        return null;
    }

    /**
     * Extract all column names from UPDATE statement (both SET and WHERE clauses)
     */
    private Set<String> extractAllColumnNamesFromUpdate(String query) {
        Set<String> columns = new HashSet<>();

        // Extract columns from SET clause
        Pattern setPattern = Pattern.compile("SET\\s+(.+?)(?:WHERE|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher setMatcher = setPattern.matcher(query);
        if (setMatcher.find()) {
            String setClause = setMatcher.group(1);
            String[] assignments = setClause.split(",");
            for (String assignment : assignments) {
                String columnName = assignment.split("=")[0].trim();
                columns.add(columnName.toLowerCase());
            }
        }

        // Extract columns from WHERE clause
        Pattern wherePattern = Pattern.compile("WHERE\\s+(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher whereMatcher = wherePattern.matcher(query);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            // Extract column names from conditions (e.g., user_id = :user_id)
            Pattern conditionPattern = Pattern.compile("(\\w+)\\s*[=<>!]+");
            Matcher conditionMatcher = conditionPattern.matcher(whereClause);
            while (conditionMatcher.find()) {
                columns.add(conditionMatcher.group(1).toLowerCase());
            }
        }

        return columns;
    }

    /**
     * Extract all column names from DELETE statement (WHERE clause)
     */
    private Set<String> extractAllColumnNamesFromDelete(String query) {
        Set<String> columns = new HashSet<>();

        // Extract columns from WHERE clause
        Pattern wherePattern = Pattern.compile("WHERE\\s+(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher whereMatcher = wherePattern.matcher(query);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);
            // Extract column names from conditions (e.g., user_id = :user_id)
            Pattern conditionPattern = Pattern.compile("(\\w+)\\s*[=<>!]+");
            Matcher conditionMatcher = conditionPattern.matcher(whereClause);
            while (conditionMatcher.find()) {
                columns.add(conditionMatcher.group(1).toLowerCase());
            }
        }

        return columns;
    }

    /**
     * Get column types for UPDATE statement
     */
    public Map<String, String> getColumnTypeMapForUpdate(String query, String databaseType) {
        Map<String, String> columnTypes = new HashMap<>();

        try {
            String tableName = extractTableNameFromDML(query);
            if (tableName == null) {
                log.warn("Could not extract table name from UPDATE query");
                return columnTypes;
            }

            Map<String, String> allColumnTypes = getAllColumnTypesForTable(tableName, databaseType);

            Set<String> usedColumns = extractAllColumnNamesFromUpdate(query);

            for (String column : usedColumns) {
                if (allColumnTypes.containsKey(column)) {
                    columnTypes.put(column, allColumnTypes.get(column));
                }
            }

            log.debug("Extracted column types for UPDATE on table {}: {}", tableName, columnTypes);

        } catch (Exception e) {
            log.warn("Could not extract column types for UPDATE: {}", e.getMessage());
        }

        return columnTypes;
    }

    /**
     * Get column types for DELETE statement
     */
    public Map<String, String> getColumnTypeMapForDelete(String query, String databaseType) {
        Map<String, String> columnTypes = new HashMap<>();

        try {
            String tableName = extractTableNameFromDML(query);
            if (tableName == null) {
                log.warn("Could not extract table name from DELETE query");
                return columnTypes;
            }

            Map<String, String> allColumnTypes = getAllColumnTypesForTable(tableName, databaseType);

            Set<String> usedColumns = extractAllColumnNamesFromDelete(query);

            for (String column : usedColumns) {
                if (allColumnTypes.containsKey(column)) {
                    columnTypes.put(column, allColumnTypes.get(column));
                }
            }

            log.debug("Extracted column types for DELETE on table {}: {}", tableName, columnTypes);

        } catch (Exception e) {
            log.warn("Could not extract column types for DELETE: {}", e.getMessage());
        }

        return columnTypes;
    }

    /**
     * Get column types for INSERT statement
     */
    public Map<String, String> getColumnTypeMapForInsert(String query, String databaseType) {
        Map<String, String> columnTypes = new HashMap<>();

        try {
            Pattern insertPattern = Pattern.compile("INSERT\\s+INTO\\s+([\\w\\.]+)\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = insertPattern.matcher(query);

            if (matcher.find()) {
                String tableWithSchema = matcher.group(1);
                String[] tableParts = tableWithSchema.split("\\.");
                String tableName = tableParts[tableParts.length - 1];
                String columnsStr = matcher.group(2);
                String[] columns = columnsStr.split(",");

                Map<String, String> allColumnTypes = getAllColumnTypesForTable(tableName, databaseType);

                for (String column : columns) {
                    String columnName = column.trim().toLowerCase();
                    if (allColumnTypes.containsKey(columnName)) {
                        columnTypes.put(columnName, allColumnTypes.get(columnName));
                    }
                }

                log.debug("Extracted column types for INSERT on table {}: {}", tableName, columnTypes);
            }
        } catch (Exception e) {
            log.warn("Could not extract column types from INSERT statement: {}", e.getMessage());
        }

        return columnTypes;
    }

    /**
     * Extract LIMIT clause
     */
    private String extractLimitClause(String query) {
        Matcher matcher = LIMIT_PATTERN.matcher(query);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    /**
     * Extract OFFSET clause
     */
    private String extractOffsetClause(String query) {
        Matcher limitMatcher = LIMIT_PATTERN.matcher(query);
        if (limitMatcher.find() && limitMatcher.group(2) != null) {
            return "OFFSET " + limitMatcher.group(2);
        }

        Matcher offsetMatcher = OFFSET_PATTERN.matcher(query);
        if (offsetMatcher.find()) {
            return offsetMatcher.group(0);
        }

        Matcher fetchMatcher = FETCH_PATTERN.matcher(query);
        if (fetchMatcher.find()) {
            return fetchMatcher.group(0);
        }

        return null;
    }

    /**
     * Extract WITH clause (CTE)
     */
    private String extractWithClause(String query) {
        Matcher matcher = WITH_CLAUSE_PATTERN.matcher(query);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    /**
     * Detect query type (SELECT, INSERT, UPDATE, DELETE)
     */
    private String detectQueryType(String query) {
        String upperQuery = query.trim().toUpperCase();
        if (upperQuery.startsWith("SELECT")) return "SELECT";
        if (upperQuery.startsWith("INSERT")) return "INSERT";
        if (upperQuery.startsWith("UPDATE")) return "UPDATE";
        if (upperQuery.startsWith("DELETE")) return "DELETE";
        if (upperQuery.startsWith("WITH")) return "WITH_CTE";
        return "UNKNOWN";
    }

    /**
     * Check if query is complex (has subqueries, joins, unions, etc.)
     */
    private boolean isComplexQuery(String query) {
        String upperQuery = query.toUpperCase();
        return upperQuery.contains("JOIN") ||
                upperQuery.contains("UNION") ||
                upperQuery.contains("INTERSECT") ||
                upperQuery.contains("EXCEPT") ||
                (upperQuery.contains("SELECT") && upperQuery.indexOf("SELECT") != upperQuery.lastIndexOf("SELECT")) ||
                upperQuery.contains("SUBQUERY");
    }

    /**
     * Estimate query complexity (SIMPLE, MODERATE, COMPLEX)
     */
    private String estimateQueryComplexity(String query) {
        int score = 0;
        String upperQuery = query.toUpperCase();

        // Count JOINs
        Matcher joinMatcher = Pattern.compile("\\bJOIN\\b", Pattern.CASE_INSENSITIVE).matcher(upperQuery);
        while (joinMatcher.find()) score += 2;

        // Count UNIONs
        Matcher unionMatcher = Pattern.compile("\\bUNION\\b", Pattern.CASE_INSENSITIVE).matcher(upperQuery);
        while (unionMatcher.find()) score += 3;

        // Count subqueries (nested SELECT)
        Matcher subqueryMatcher = Pattern.compile("\\bSELECT\\b", Pattern.CASE_INSENSITIVE).matcher(upperQuery);
        int selectCount = 0;
        while (subqueryMatcher.find()) selectCount++;
        if (selectCount > 1) score += (selectCount - 1) * 2;

        // Check for GROUP BY, HAVING, ORDER BY
        if (upperQuery.contains("GROUP BY")) score += 1;
        if (upperQuery.contains("HAVING")) score += 1;
        if (upperQuery.contains("ORDER BY")) score += 1;

        // Check for WITH clause (CTE)
        if (upperQuery.contains("WITH")) score += 2;

        // Check for window functions
        if (upperQuery.contains("OVER(")) score += 2;

        if (score <= 2) return "SIMPLE";
        if (score <= 6) return "MODERATE";
        return "COMPLEX";
    }

    /**
     * Fallback: Extract columns from SELECT clause using regex
     */
    private List<QueryColumnDTO> extractColumnsFromQuery(String query) {
        List<QueryColumnDTO> columns = new ArrayList<>();
        Matcher matcher = SELECT_COLUMNS_PATTERN.matcher(query);

        if (matcher.find()) {
            String columnsClause = matcher.group(1);
            String[] columnParts = splitColumns(columnsClause);

            int position = 1;
            for (String columnPart : columnParts) {
                String trimmed = columnPart.trim();
                String columnName = trimmed;
                String alias = null;

                // Check for alias (AS or just space)
                Pattern aliasPattern = Pattern.compile("(.+?)\\s+(?:AS\\s+)?(\\w+)$", Pattern.CASE_INSENSITIVE);
                Matcher aliasMatcher = aliasPattern.matcher(trimmed);
                if (aliasMatcher.find()) {
                    columnName = aliasMatcher.group(1).trim();
                    alias = aliasMatcher.group(2);
                } else if (trimmed.contains(" ")) {
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length >= 2) {
                        columnName = parts[0];
                        alias = parts[1];
                    }
                }

                // Handle function calls like COUNT(*), MAX(column), etc.
                if (columnName.contains("(") && columnName.contains(")")) {
                    columnName = extractFunctionColumnName(columnName);
                }

                QueryColumnDTO column = QueryColumnDTO.builder()
                        .columnName(columnName)
                        .alias(alias)
                        .dataType("VARCHAR2")
                        .dataTypeClass("string")
                        .position(position++)
                        .build();
                columns.add(column);
            }
        }

        return columns;
    }

    /**
     * Extract column name from function call
     */
    private String extractFunctionColumnName(String functionCall) {
        Pattern funcPattern = Pattern.compile("(\\w+)\\s*\\(.*\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = funcPattern.matcher(functionCall);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return "function_result";
    }

    /**
     * Split columns clause handling nested functions and commas
     */
    private String[] splitColumns(String columnsClause) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parentheses = 0;

        for (char c : columnsClause.toCharArray()) {
            if (c == '(') {
                parentheses++;
                current.append(c);
            } else if (c == ')') {
                parentheses--;
                current.append(c);
            } else if (c == ',' && parentheses == 0) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            parts.add(current.toString());
        }

        return parts.toArray(new String[0]);
    }

    /**
     * Normalize the query for parsing
     */
    private String normalizeQuery(String query) {
        String normalized = query.replaceAll("\\s+", " ").trim();
        if (!normalized.endsWith(";")) {
            normalized = normalized + ";";
        }
        return normalized;
    }

    /**
     * Extract FROM clause
     */
    private String extractFromClause(String query) {
        Matcher matcher = FROM_PATTERN.matcher(query);
        if (matcher.find()) {
            String fromPart = matcher.group(1);
            int endIndex = findNextClauseStart(query, matcher.end());
            if (endIndex > matcher.end()) {
                return query.substring(matcher.start(), endIndex).replaceFirst("FROM\\s+", "").trim();
            }
            return fromPart;
        }
        return null;
    }

    /**
     * Extract WHERE clause
     */
    private String extractWhereClause(String query) {
        Matcher matcher = WHERE_PATTERN.matcher(query);
        if (matcher.find()) {
            int endIndex = findNextClauseStart(query, matcher.end());
            if (endIndex > matcher.end()) {
                return query.substring(matcher.start(), endIndex).replaceFirst("WHERE\\s+", "").trim();
            }
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Extract GROUP BY clause
     */
    private String extractGroupByClause(String query) {
        Matcher matcher = GROUP_BY_PATTERN.matcher(query);
        if (matcher.find()) {
            int endIndex = findNextClauseStart(query, matcher.end());
            if (endIndex > matcher.end()) {
                return query.substring(matcher.start(), endIndex).replaceFirst("GROUP\\s+BY\\s+", "").trim();
            }
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Extract HAVING clause
     */
    private String extractHavingClause(String query) {
        Matcher matcher = HAVING_PATTERN.matcher(query);
        if (matcher.find()) {
            int endIndex = findNextClauseStart(query, matcher.end());
            if (endIndex > matcher.end()) {
                return query.substring(matcher.start(), endIndex).replaceFirst("HAVING\\s+", "").trim();
            }
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Extract ORDER BY clause
     */
    private String extractOrderByClause(String query) {
        Matcher matcher = ORDER_BY_PATTERN.matcher(query);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Find the index of the next clause start
     */
    private int findNextClauseStart(String query, int startPos) {
        String[] clauses = {"WHERE", "GROUP BY", "HAVING", "ORDER BY", "LIMIT", "OFFSET", ";"};
        int nextStart = query.length();

        for (String clause : clauses) {
            Pattern pattern = Pattern.compile("\\b" + clause + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(query);
            if (matcher.find(startPos) && matcher.start() < nextStart) {
                nextStart = matcher.start();
            }
        }

        return nextStart;
    }

    /**
     * Extract source tables from the query
     */
    private List<String> extractSourceTables(String query) {
        List<String> tables = new ArrayList<>();
        String simplified = query.replaceAll("\\([^)]+\\)", " ");

        Matcher matcher = FROM_PATTERN.matcher(simplified);
        if (matcher.find()) {
            String fromClause = matcher.group(1);
            String[] parts = fromClause.split("\\s*,\\s*|\\s+JOIN\\s+");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty() && !trimmed.toUpperCase().startsWith("JOIN")) {
                    String[] tableParts = trimmed.split("\\s+");
                    String tableName = tableParts[0];
                    // Handle schema.table format
                    if (tableName.contains(".")) {
                        String[] schemaParts = tableName.split("\\.");
                        tableName = schemaParts[schemaParts.length - 1];
                    }
                    tables.add(tableName);
                }
            }
        }

        return tables;
    }

    /**
     * Generate a query alias from the SELECT statement
     */
    private String generateQueryAlias(String query) {
        String upperQuery = query.toUpperCase();

        if (upperQuery.contains("FROM")) {
            String fromPart = extractFromClause(query);
            if (fromPart != null) {
                String[] tables = fromPart.split(",");
                if (tables.length > 0) {
                    String firstTable = tables[0].trim().split("\\s+")[0];
                    // Handle schema.table format
                    if (firstTable.contains(".")) {
                        String[] parts = firstTable.split("\\.");
                        firstTable = parts[parts.length - 1];
                    }
                    return "query_" + firstTable.toLowerCase();
                }
            }
        }

        return "custom_query_" + Math.abs(query.hashCode() % 10000);
    }

    /**
     * Map SQL type to Java type
     */
    private String getJavaTypeForSqlType(int sqlType) {
        switch (sqlType) {
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
                return "string";
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                return "integer";
            case Types.BIGINT:
                return "long";
            case Types.DECIMAL:
            case Types.NUMERIC:
                return "bigdecimal";
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                return "double";
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIME:
                return "localdatetime";
            case Types.BOOLEAN:
            case Types.BIT:
                return "boolean";
            case Types.ARRAY:
                return "array";
            case Types.JAVA_OBJECT:
                return "object";
            default:
                return "string";
        }
    }

    /**
     * Validate that the SELECT statement is safe to execute
     */
    public void validateSelectStatement(String selectStatement) {
        String upperStatement = selectStatement.toUpperCase();

        String[] dangerousKeywords = {"INSERT", "UPDATE", "DELETE", "DROP", "CREATE",
                "ALTER", "TRUNCATE", "GRANT", "REVOKE", "EXEC", "EXECUTE"};

        for (String keyword : dangerousKeywords) {
            if (upperStatement.contains(keyword)) {
                throw new IllegalArgumentException(
                        "SELECT statement contains dangerous keyword: " + keyword +
                                ". Only SELECT statements are allowed."
                );
            }
        }

        if (!upperStatement.trim().startsWith("SELECT") && !upperStatement.trim().startsWith("WITH")) {
            throw new IllegalArgumentException(
                    "Only SELECT statements are allowed. Query must start with SELECT or WITH."
            );
        }
    }

    public Map<String, String> getColumnTypeMap(String query, String databaseType) {
        return getColumnTypeMapForSelect(query, databaseType);
    }

    /**
     * Get column types specifically for INSERT/UPDATE/DELETE statements
     */
    public Map<String, String> getColumnTypeMapForDML(String query, String databaseType) {
        String upperQuery = query.toUpperCase().trim();

        if (upperQuery.startsWith("INSERT")) {
            return getColumnTypeMapForInsert(query, databaseType);
        } else if (upperQuery.startsWith("UPDATE")) {
            return getColumnTypeMapForUpdate(query, databaseType);
        } else if (upperQuery.startsWith("DELETE")) {
            return getColumnTypeMapForDelete(query, databaseType);
        }

        return new HashMap<>();
    }
}