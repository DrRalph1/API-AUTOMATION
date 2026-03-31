package com.usg.apiGeneration.utils.apiEngine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SQLParserUtil {

    /**
     * Parse SQL statement and extract structure
     */
    public ParsedSQL parseSQL(String sql, String databaseType) {
        ParsedSQL parsed = new ParsedSQL();
        parsed.setRawSql(sql);
        parsed.setDatabaseType(databaseType);

        String trimmedSql = sql.trim().toUpperCase();

        // Determine operation type
        if (trimmedSql.startsWith("SELECT")) {
            parsed.setOperationType("SELECT");
            parseSelectStatement(sql, parsed);
        } else if (trimmedSql.startsWith("INSERT")) {
            parsed.setOperationType("INSERT");
            parseInsertStatement(sql, parsed);
        } else if (trimmedSql.startsWith("UPDATE")) {
            parsed.setOperationType("UPDATE");
            parseUpdateStatement(sql, parsed);
        } else if (trimmedSql.startsWith("DELETE")) {
            parsed.setOperationType("DELETE");
            parseDeleteStatement(sql, parsed);
        } else {
            throw new IllegalArgumentException("Unsupported SQL operation: " + parsed.getOperationType());
        }

        // Extract parameters from WHERE clause
        extractParametersFromSQL(sql, parsed);

        return parsed;
    }

    private void parseSelectStatement(String sql, ParsedSQL parsed) {
        // Extract FROM table
        Pattern fromPattern = Pattern.compile("FROM\\s+([a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(sql);
        if (fromMatcher.find()) {
            String tableWithSchema = fromMatcher.group(1);
            parsed.setTargetObject(tableWithSchema);

            if (tableWithSchema.contains(".")) {
                String[] parts = tableWithSchema.split("\\.");
                parsed.setSchema(parts[0]);
                parsed.setObjectName(parts[1]);
            } else {
                parsed.setObjectName(tableWithSchema);
            }
        }

        // Extract SELECT columns
        Pattern selectPattern = Pattern.compile("SELECT\\s+(.*?)\\s+FROM", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher selectMatcher = selectPattern.matcher(sql);
        if (selectMatcher.find()) {
            String columnsClause = selectMatcher.group(1);
            parseColumns(columnsClause, parsed);
        }
    }

    private void parseInsertStatement(String sql, ParsedSQL parsed) {
        // Extract INTO table
        Pattern intoPattern = Pattern.compile("INTO\\s+([a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher intoMatcher = intoPattern.matcher(sql);
        if (intoMatcher.find()) {
            String tableWithSchema = intoMatcher.group(1);
            parsed.setTargetObject(tableWithSchema);

            if (tableWithSchema.contains(".")) {
                String[] parts = tableWithSchema.split("\\.");
                parsed.setSchema(parts[0]);
                parsed.setObjectName(parts[1]);
            } else {
                parsed.setObjectName(tableWithSchema);
            }
        }
    }

    private void parseUpdateStatement(String sql, ParsedSQL parsed) {
        // Extract UPDATE table
        Pattern updatePattern = Pattern.compile("UPDATE\\s+([a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher updateMatcher = updatePattern.matcher(sql);
        if (updateMatcher.find()) {
            String tableWithSchema = updateMatcher.group(1);
            parsed.setTargetObject(tableWithSchema);

            if (tableWithSchema.contains(".")) {
                String[] parts = tableWithSchema.split("\\.");
                parsed.setSchema(parts[0]);
                parsed.setObjectName(parts[1]);
            } else {
                parsed.setObjectName(tableWithSchema);
            }
        }
    }

    private void parseDeleteStatement(String sql, ParsedSQL parsed) {
        // Extract FROM table
        Pattern fromPattern = Pattern.compile("FROM\\s+([a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(sql);
        if (fromMatcher.find()) {
            String tableWithSchema = fromMatcher.group(1);
            parsed.setTargetObject(tableWithSchema);

            if (tableWithSchema.contains(".")) {
                String[] parts = tableWithSchema.split("\\.");
                parsed.setSchema(parts[0]);
                parsed.setObjectName(parts[1]);
            } else {
                parsed.setObjectName(tableWithSchema);
            }
        }
    }

    private void parseColumns(String columnsClause, ParsedSQL parsed) {
        String[] columns = columnsClause.split(",");
        List<String> columnList = new ArrayList<>();

        for (String col : columns) {
            col = col.trim();
            // Handle aliases like "column AS alias" or "column alias"
            String[] parts = col.split("\\s+(?:AS\\s+)?");
            String columnName = parts[0].trim();

            // Remove table prefix (table.column -> column)
            if (columnName.contains(".")) {
                columnName = columnName.substring(columnName.lastIndexOf(".") + 1);
            }

            columnList.add(columnName);
        }

        parsed.setSelectedColumns(columnList);
    }

    private void extractParametersFromSQL(String sql, ParsedSQL parsed) {
        List<SQLParameter> parameters = new ArrayList<>();

        // Look for placeholders like :paramName, ?paramName, or #{paramName}
        Pattern paramPattern = Pattern.compile("[:?]\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher paramMatcher = paramPattern.matcher(sql);

        Set<String> paramNames = new LinkedHashSet<>();
        while (paramMatcher.find()) {
            paramNames.add(paramMatcher.group(1));
        }

        // Also look for named parameters in WHERE clause
        Pattern wherePattern = Pattern.compile("WHERE\\s+(.*?)(?:ORDER BY|GROUP BY|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) {
            String whereClause = whereMatcher.group(1);

            // Extract column names from WHERE clause
            Pattern columnPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:=|>|<|>=|<=|LIKE|IN)");
            Matcher columnMatcher = columnPattern.matcher(whereClause);
            while (columnMatcher.find()) {
                String column = columnMatcher.group(1);
                if (!paramNames.contains(column) && !column.toUpperCase().equals("AND") && !column.toUpperCase().equals("OR")) {
                    paramNames.add(column);
                }
            }
        }

        for (String paramName : paramNames) {
            SQLParameter param = new SQLParameter();
            param.setName(paramName);
            param.setParameterType("query"); // Can be path, query, or body

            // Infer parameter location
            if (paramName.equalsIgnoreCase("id") || paramName.toLowerCase().contains("id")) {
                param.setParameterLocation("path");
            } else {
                param.setParameterLocation("query");
            }

            // Infer data type
            param.setDataType(inferDataType(paramName));
            param.setRequired(true);

            parameters.add(param);
        }

        parsed.setParameters(parameters);
    }

    private String inferDataType(String paramName) {
        String lowerName = paramName.toLowerCase();
        if (lowerName.contains("id") || lowerName.contains("code") || lowerName.contains("number")) {
            return "number";
        }
        if (lowerName.contains("date") || lowerName.contains("time")) {
            return "date";
        }
        if (lowerName.contains("flag") || lowerName.contains("active") || lowerName.contains("enabled")) {
            return "boolean";
        }
        return "string";
    }

    /**
     * Convert parsed SQL to a parameterized SQL statement
     */
    public String toParameterizedSQL(String sql, List<SQLParameter> parameters) {
        String result = sql;

        for (SQLParameter param : parameters) {
            // Replace :paramName with ?
            result = result.replaceAll("[:?]" + param.getName() + "\\b", "?");
        }

        return result;
    }

    // Inner classes
    @lombok.Data
    public static class ParsedSQL {
        private String rawSql;
        private String operationType;
        private String targetObject;
        private String schema;
        private String objectName;
        private List<String> selectedColumns = new ArrayList<>();
        private List<SQLParameter> parameters = new ArrayList<>();
        private String databaseType;
    }

    @lombok.Data
    public static class SQLParameter {
        private String name;
        private String parameterType; // path, query, body
        private String parameterLocation;
        private String dataType;
        private boolean required;
        private String description;
        private String example;
    }
}