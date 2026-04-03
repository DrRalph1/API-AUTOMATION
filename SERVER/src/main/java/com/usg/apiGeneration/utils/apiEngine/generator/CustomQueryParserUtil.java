package com.usg.apiGeneration.utils.apiEngine.generator;

import com.usg.apiGeneration.dtos.apiGenerationEngine.QueryColumnDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.QueryParameterDTO;
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
    private static final Pattern FROM_PATTERN = Pattern.compile("\\bFROM\\s+([^\\s]+(?:\\s*,\\s*[^\\s]+)*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bWHERE\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GROUP_BY_PATTERN = Pattern.compile("\\bGROUP\\s+BY\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAVING_PATTERN = Pattern.compile("\\bHAVING\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_BY_PATTERN = Pattern.compile("\\bORDER\\s+BY\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SELECT_COLUMNS_PATTERN = Pattern.compile("^SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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

            // Extract parameters
            List<QueryParameterDTO> parameters = extractParameters(normalizedQuery);
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

            // Extract source tables
            List<String> sourceTables = extractSourceTables(normalizedQuery);
            result.put("sourceTables", sourceTables);

            // Check if query has parameters
            result.put("hasParameters", !parameters.isEmpty());

            // Get column count
            result.put("columnCount", columns.size());

            // Generate a query alias
            result.put("queryAlias", generateQueryAlias(selectStatement));

            log.info("Parsed custom query: {} parameters, {} columns, {} source tables",
                    parameters.size(), columns.size(), sourceTables.size());

        } catch (Exception e) {
            log.error("Error parsing custom query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse custom SELECT statement: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Extract parameters from the query (e.g., :paramName)
     */
    private List<QueryParameterDTO> extractParameters(String query) {
        List<QueryParameterDTO> parameters = new ArrayList<>();
        Matcher matcher = PARAMETER_PATTERN.matcher(query);
        Set<String> uniqueParams = new LinkedHashSet<>();

        while (matcher.find()) {
            uniqueParams.add(matcher.group(1));
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

        return parameters;
    }

    /**
     * Extract column metadata using JDBC - FIXED to use correct template based on database type
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

        // Wrap the query to get metadata without executing
        String metadataQuery = "SELECT * FROM (" + query + ") WHERE 1=0";

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
                    // Handle "column alias" without AS
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length >= 2) {
                        columnName = parts[0];
                        alias = parts[1];
                    }
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
        String[] clauses = {"WHERE", "GROUP BY", "HAVING", "ORDER BY", ";"};
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
                    tables.add(tableParts[0]);
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
                "ALTER", "TRUNCATE", "GRANT", "REVOKE"};

        for (String keyword : dangerousKeywords) {
            if (upperStatement.contains(keyword)) {
                throw new IllegalArgumentException(
                        "SELECT statement contains dangerous keyword: " + keyword +
                                ". Only SELECT statements are allowed."
                );
            }
        }

        if (!upperStatement.trim().startsWith("SELECT")) {
            throw new IllegalArgumentException(
                    "Only SELECT statements are allowed. Query must start with SELECT."
            );
        }
    }
}