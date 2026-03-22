package com.usg.apiAutomation.utils.apiEngine.executor.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.utils.apiEngine.PostgreSQLParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostgreSQLTableExecutorUtil {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    private final PostgreSQLParameterValidatorUtil parameterValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PostgreSQLTableExecutorUtil(
            PostgreSQLParameterValidatorUtil parameterValidator) {
        this.parameterValidator = parameterValidator;
    }

    /**
     * Extracts the full PostgreSQL error message from the exception chain
     */
    private String extractFullPostgreSQLError(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.contains("ERROR:")) {
                // Return the complete PostgreSQL error line
                Pattern pattern = Pattern.compile("ERROR:[^\\n]*");
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    return matcher.group();
                }
                return message;
            }
            cause = cause.getCause();
        }
        return e.getMessage();
    }

    /**
     * Helper method to extract PostgreSQL error message (raw version)
     */
    private String extractPostgreSQLError(String errorMessage) {
        if (errorMessage == null) return "Unknown error";

        Pattern pattern = Pattern.compile("ERROR:[^\\n]*");
        Matcher matcher = pattern.matcher(errorMessage);
        if (matcher.find()) {
            return matcher.group();
        }

        if (errorMessage.length() > 200) {
            return errorMessage.substring(0, 200) + "...";
        }
        return errorMessage;
    }

    /**
     * Parse XML/JSON body and extract parameter values
     */
    private Map<String, Object> parseBodyParameters(String body, List<ApiParameterDTO> configuredParamDTOs,
                                                    Map<String, String> apiToDbColumnMap, boolean isXml) {
        Map<String, Object> extractedParams = new HashMap<>();

        if (body == null || body.trim().isEmpty()) {
            return extractedParams;
        }

        log.info("Parsing {} body to extract parameter values", isXml ? "XML" : "JSON");

        try {
            if (isXml) {
                // XML parsing
                for (ApiParameterDTO param : configuredParamDTOs) {
                    String paramKey = param.getKey();
                    if (paramKey == null || paramKey.isEmpty()) {
                        continue;
                    }

                    Pattern pattern = Pattern.compile("<" + paramKey + ">(.*?)</" + paramKey + ">",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(body);

                    if (matcher.find()) {
                        String value = matcher.group(1).trim();
                        if (!value.isEmpty()) {
                            String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                            extractedParams.put(dbColumnName, value);
                            log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbColumnName, value);
                        } else {
                            log.info("⚠️ XML tag <{}> found but empty", paramKey);
                            String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                            extractedParams.put(dbColumnName, "");
                        }
                    }
                }
            } else {
                // JSON parsing
                Map<String, Object> jsonMap = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    String paramKey = entry.getKey().toLowerCase();
                    String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                    Object value = entry.getValue();

                    if (value instanceof Map || value instanceof List) {
                        value = objectMapper.writeValueAsString(value);
                    }

                    extractedParams.put(dbColumnName, value);
                    log.info("✅ Extracted JSON parameter: {} -> {} = {}", entry.getKey(), dbColumnName, value);
                }
            }

            log.info("Extracted {} parameters from {}: {}", extractedParams.size(), isXml ? "XML" : "JSON", extractedParams.keySet());

        } catch (Exception e) {
            log.error("Error parsing {} parameters: {}", isXml ? "XML" : "JSON", e.getMessage(), e);
        }

        return extractedParams;
    }

    public Object executeSelect(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM ");
            if (schema != null && !schema.isEmpty()) {
                sql.append(schema).append(".");
            }
            sql.append(tableName);

            List<Object> paramValues = new ArrayList<>();
            List<String> whereClauses = new ArrayList<>();

            log.info("=== TABLE SELECT DEBUG ===");
            log.info("Table: {}.{}", schema, tableName);
            log.info("All incoming params: {}", params);

            // Build a clean parameter map
            Map<String, Object> cleanParams = new HashMap<>();

            // Build parameter mapping
            Map<String, String> apiToDbColumnMap = new HashMap<>();
            if (configuredParamDTOs != null) {
                for (ApiParameterDTO param : configuredParamDTOs) {
                    if (param.getKey() != null) {
                        String dbColumnName = param.getDbColumn();
                        if (dbColumnName == null || dbColumnName.isEmpty()) {
                            dbColumnName = param.getDbParameter();
                        }
                        if (dbColumnName == null || dbColumnName.isEmpty()) {
                            dbColumnName = param.getKey();
                        }
                        apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toLowerCase());
                        log.info("Parameter mapping: API '{}' -> Database Column '{}'", param.getKey(), dbColumnName.toLowerCase());
                    }
                }
            }

            // Process XML/JSON body if present
            String body = null;
            boolean isXmlBody = false;
            boolean isJsonBody = false;

            if (params != null) {
                // Check for XML body
                if (params.containsKey("_xml")) {
                    Object xmlObj = params.get("_xml");
                    if (xmlObj instanceof String) {
                        String xmlString = (String) xmlObj;
                        if (xmlString.trim().startsWith("<")) {
                            isXmlBody = true;
                            body = xmlString;
                            log.info("XML BODY DETECTED in TableExecutor!");

                            Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, true);
                            if (!extractedParams.isEmpty()) {
                                cleanParams.putAll(extractedParams);
                            }
                        }
                    }
                }

                // Check for JSON body (if not XML)
                if (!isXmlBody && params.containsKey("_json")) {
                    Object jsonObj = params.get("_json");
                    if (jsonObj instanceof String) {
                        String jsonString = (String) jsonObj;
                        if (jsonString.trim().startsWith("{") || jsonString.trim().startsWith("[")) {
                            isJsonBody = true;
                            body = jsonString;
                            log.info("JSON BODY DETECTED in TableExecutor!");

                            Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, false);
                            if (!extractedParams.isEmpty()) {
                                cleanParams.putAll(extractedParams);
                            }
                        }
                    }
                }
            }

            // Copy all parameters
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    if ("_xml".equals(key) || "_json".equals(key)) {
                        continue;
                    }

                    String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
                    cleanParams.put(dbColumnName, entry.getValue());
                }

                // Handle numbered parameters from path
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("param") && key.length() > 5) {
                        try {
                            int position = Integer.parseInt(key.substring(5)) - 1;
                            cleanParams.put("position_" + position, entry.getValue());
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            }

            log.info("Cleaned params after body processing: {}", cleanParams);

            // Build WHERE clause
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                for (ApiParameterEntity configuredParam : api.getParameters()) {
                    String paramKey = configuredParam.getKey();
                    String dbColumn = configuredParam.getDbColumn();
                    String paramType = configuredParam.getParameterType();

                    if (paramType == null) {
                        paramType = "query";
                    }

                    if (dbColumn == null || dbColumn.isEmpty()) {
                        continue;
                    }

                    boolean isValidForFiltering = "query".equals(paramType) ||
                            "path".equals(paramType) ||
                            "body".equals(paramType);

                    if (isValidForFiltering) {
                        Object value = null;

                        if (cleanParams.containsKey(paramKey)) {
                            value = cleanParams.get(paramKey);
                        }

                        if (value == null) {
                            for (Map.Entry<String, Object> entry : cleanParams.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(paramKey)) {
                                    value = entry.getValue();
                                    break;
                                }
                            }
                        }

                        if (value == null && "path".equals(paramType)) {
                            Integer position = configuredParam.getPosition();
                            if (position != null) {
                                String positionKey = "position_" + position;
                                if (cleanParams.containsKey(positionKey)) {
                                    value = cleanParams.get(positionKey);
                                }
                            }
                        }

                        if (value != null) {
                            if (value instanceof List || value.getClass().isArray()) {
                                Collection<?> collection = value instanceof List ?
                                        (List<?>) value : Arrays.asList((Object[]) value);
                                if (!collection.isEmpty()) {
                                    value = collection.iterator().next();
                                } else {
                                    value = null;
                                }
                            }

                            if (value != null && !value.toString().trim().isEmpty()) {
                                whereClauses.add(dbColumn + " = ?");
                                paramValues.add(value);
                                log.info("ADDED FILTER: {} = ? with value: {}", dbColumn, value);
                            } else if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                                throw new ValidationException(
                                        String.format("Required parameter '%s' cannot be empty", paramKey)
                                );
                            }
                        } else if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                            throw new ValidationException(
                                    String.format("Required parameter '%s' is missing. Available params: %s",
                                            paramKey, cleanParams.keySet())
                            );
                        }
                    }
                }
            }

            if (!whereClauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
            }

            // Handle pagination
            if (api.getSchemaConfig() != null &&
                    Boolean.TRUE.equals(api.getSchemaConfig().getEnablePagination())) {
                int pageSize = api.getSchemaConfig().getPageSize() != null ?
                        api.getSchemaConfig().getPageSize() : 10;
                int page = 1;

                if (params != null && params.containsKey("page")) {
                    try {
                        page = Integer.parseInt(params.get("page").toString());
                        if (page < 1) page = 1;
                    } catch (NumberFormatException e) {
                        log.warn("Invalid page parameter, using default: 1");
                    }
                }

                int offset = (page - 1) * pageSize;
                sql.append(" OFFSET ? LIMIT ?");
                paramValues.add(offset);
                paramValues.add(pageSize);
            }

            log.info("Final SQL: {} with {} parameters", sql.toString(), paramValues.size());

            List<Map<String, Object>> results = postgresqlJdbcTemplate.queryForList(
                    sql.toString(), paramValues.toArray());
            log.info("Query returned {} rows", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error executing table select: {}", e.getMessage(), e);

            String detailedError = extractFullPostgreSQLError(e);

            if (e.getMessage() != null && e.getMessage().contains("Invalid column type")) {
                if (params != null) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getValue() instanceof List || entry.getValue() instanceof Collection) {
                            throw new ValidationException(
                                    String.format("Parameter '%s' cannot accept multiple values. Please provide a single value.",
                                            entry.getKey())
                            );
                        }
                    }
                }
                throw new ValidationException("Invalid parameter format. Please check the data types of your parameters.");
            }

            // Throw the raw PostgreSQL error
            throw new RuntimeException(detailedError, e);
        }
    }

    /**
     * UPDATED: Execute INSERT with raw PostgreSQL error propagation
     */
    public Object executeInsert(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for INSERT operation");
        }

        log.info("=== TABLE INSERT DEBUG ===");
        log.info("Table: {}.{}", schema, tableName);
        log.info("Original params: {}", params);

        // Build parameter mapping
        Map<String, String> apiToDbColumnMap = new HashMap<>();
        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (param.getKey() != null) {
                    String dbColumnName = param.getDbColumn();
                    if (dbColumnName == null || dbColumnName.isEmpty()) {
                        dbColumnName = param.getDbParameter();
                    }
                    if (dbColumnName == null || dbColumnName.isEmpty()) {
                        dbColumnName = param.getKey();
                    }
                    apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toLowerCase());
                    log.info("Parameter mapping: API '{}' -> DB Column '{}'", param.getKey(), dbColumnName.toLowerCase());
                }
            }
        }

        // Process body parameters
        Map<String, Object> processedParams = new HashMap<>();
        String body = null;
        boolean isXmlBody = false;
        boolean isJsonBody = false;

        if (params.containsKey("_xml")) {
            Object xmlObj = params.get("_xml");
            if (xmlObj instanceof String) {
                String xmlString = (String) xmlObj;
                if (xmlString.trim().startsWith("<")) {
                    isXmlBody = true;
                    body = xmlString;
                    log.info("XML BODY DETECTED in INSERT operation!");

                    Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, true);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                        log.info("✅ Extracted {} parameters from XML", extractedParams.size());
                    }
                }
            }
        }

        if (!isXmlBody && params.containsKey("_json")) {
            Object jsonObj = params.get("_json");
            if (jsonObj instanceof String) {
                String jsonString = (String) jsonObj;
                if (jsonString.trim().startsWith("{") || jsonString.trim().startsWith("[")) {
                    isJsonBody = true;
                    body = jsonString;
                    log.info("JSON BODY DETECTED in INSERT operation!");

                    Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, false);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                        log.info("✅ Extracted {} parameters from JSON", extractedParams.size());
                    }
                }
            }
        }

        // Copy all parameters and convert to proper types
        Map<String, Object> typedParams = new HashMap<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("_xml".equals(key) || "_json".equals(key)) {
                continue;
            }

            String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
            Object value = entry.getValue();

            // Convert value to proper type based on parameter definition
            Object convertedValue = convertParameterValue(value, dbColumnName, api);
            if (convertedValue != null) {
                typedParams.put(dbColumnName, convertedValue);
                log.debug("Added param: {} -> {} = {} (converted from {})", key, dbColumnName, convertedValue, value);
            } else {
                log.debug("Skipped null/empty param: {} -> {}", key, dbColumnName);
            }
        }

        // Also process any XML/JSON extracted parameters that weren't already in params
        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            String dbColumnName = entry.getKey();
            if (!typedParams.containsKey(dbColumnName)) {
                Object convertedValue = convertParameterValue(entry.getValue(), dbColumnName, api);
                if (convertedValue != null) {
                    typedParams.put(dbColumnName, convertedValue);
                    log.debug("Added extracted param: {} = {} (converted)", dbColumnName, convertedValue);
                }
            }
        }

        // Handle collection/array parameters
        for (Map.Entry<String, Object> entry : typedParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    typedParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value", entry.getKey());
                } else {
                    typedParams.put(entry.getKey(), null);
                }
            }
        }

        log.info("Final typed params for INSERT: {}", typedParams.keySet());

        // Check if we have any parameters to insert
        if (typedParams.isEmpty()) {
            log.error("No valid parameters to insert after type conversion!");
            throw new ValidationException(
                    "No valid parameters provided for INSERT operation. " +
                            "Please provide at least one field with a non-empty value."
            );
        }

        // Build the INSERT SQL
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : typedParams.entrySet()) {
            if (entry.getValue() != null) {
                if (columns.length() > 0) {
                    columns.append(", ");
                    values.append(", ");
                }
                columns.append(entry.getKey());
                values.append("?");
                paramValues.add(entry.getValue());
                log.debug("Column: {} = {}", entry.getKey(), entry.getValue());
            }
        }

        String sql = "INSERT INTO " + (schema != null && !schema.isEmpty() ? schema + "." : "") + tableName +
                " (" + columns + ") VALUES (" + values + ") RETURNING *";

        log.info("Final INSERT SQL: {}", sql);
        log.info("INSERT parameters: {}", paramValues);

        try {
            List<Map<String, Object>> inserted = postgresqlJdbcTemplate.queryForList(sql, paramValues.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", inserted.size());
            result.put("message", inserted.size() > 0 ? "Insert successful" : "No rows inserted");

            if (!inserted.isEmpty()) {
                result.put("data", inserted.get(0));
            }

            return result;

        } catch (Exception e) {
            log.error("Error executing INSERT on {}: {}", tableName, e.getMessage(), e);

            // Get the raw PostgreSQL error message
            String rawError = extractFullPostgreSQLError(e);

            // THROW THE RAW ERROR DIRECTLY - don't wrap or modify it
            // This ensures users see the actual database constraint violation
            throw new RuntimeException(rawError, e);
        }
    }

    public Object executeUpdate(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for UPDATE operation");
        }

        log.info("=== TABLE UPDATE DEBUG ===");
        log.info("Table: {}.{}", schema, tableName);

        List<String> pkColumns = api.getResponseMappings().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                .map(ApiResponseMappingEntity::getDbColumn)
                .collect(Collectors.toList());

        if (pkColumns.isEmpty()) {
            throw new RuntimeException("No primary key defined for UPDATE operation");
        }

        // Build parameter mapping
        Map<String, String> apiToDbColumnMap = new HashMap<>();
        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (param.getKey() != null) {
                    String dbColumnName = param.getDbColumn();
                    if (dbColumnName == null || dbColumnName.isEmpty()) {
                        dbColumnName = param.getDbParameter();
                    }
                    if (dbColumnName == null || dbColumnName.isEmpty()) {
                        dbColumnName = param.getKey();
                    }
                    apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toLowerCase());
                }
            }
        }

        // Process body parameters
        Map<String, Object> processedParams = new HashMap<>();
        String body = null;
        boolean isXmlBody = false;
        boolean isJsonBody = false;

        if (params.containsKey("_xml")) {
            Object xmlObj = params.get("_xml");
            if (xmlObj instanceof String) {
                String xmlString = (String) xmlObj;
                if (xmlString.trim().startsWith("<")) {
                    isXmlBody = true;
                    body = xmlString;
                    log.info("XML BODY DETECTED in UPDATE operation!");

                    Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, true);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                    }
                }
            }
        }

        if (!isXmlBody && params.containsKey("_json")) {
            Object jsonObj = params.get("_json");
            if (jsonObj instanceof String) {
                String jsonString = (String) jsonObj;
                if (jsonString.trim().startsWith("{") || jsonString.trim().startsWith("[")) {
                    isJsonBody = true;
                    body = jsonString;
                    log.info("JSON BODY DETECTED in UPDATE operation!");

                    Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, false);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                    }
                }
            }
        }

        // Copy all parameters
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("_xml".equals(key) || "_json".equals(key)) {
                continue;
            }

            String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
            processedParams.put(dbColumnName, entry.getValue());
        }

        // Handle collection/array parameters
        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    processedParams.put(entry.getKey(), collection.iterator().next());
                } else {
                    processedParams.put(entry.getKey(), null);
                }
            }
        }

        log.info("Processed params for UPDATE: {}", processedParams.keySet());

        StringBuilder setClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        List<Object> setValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            String key = entry.getKey();
            boolean isPk = pkColumns.stream().anyMatch(pk -> pk.equalsIgnoreCase(key));

            if (isPk) {
                if (whereClause.length() > 0) {
                    whereClause.append(" AND ");
                } else {
                    whereClause.append(" WHERE ");
                }
                whereClause.append(key).append(" = ?");
                whereValues.add(entry.getValue());
            } else {
                if (setClause.length() > 0) {
                    setClause.append(", ");
                }
                setClause.append(key).append(" = ?");
                setValues.add(entry.getValue());
            }
        }

        if (whereValues.isEmpty()) {
            throw new RuntimeException("No primary key values provided for UPDATE operation");
        }

        String sql = "UPDATE " + (schema != null && !schema.isEmpty() ? schema + "." : "") + tableName +
                " SET " + setClause + whereClause;

        List<Object> allParams = new ArrayList<>(setValues);
        allParams.addAll(whereValues);

        try {
            int rowsAffected = postgresqlJdbcTemplate.update(sql, allParams.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Update successful" : "No rows updated");

            return result;

        } catch (Exception e) {
            log.error("Error executing UPDATE on {}: {}", tableName, e.getMessage(), e);

            // Get the raw PostgreSQL error
            String rawError = extractFullPostgreSQLError(e);

            // Throw raw error
            throw new RuntimeException(rawError, e);
        }
    }

    public Object executeDelete(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for DELETE operation");
        }

        log.info("=== TABLE DELETE DEBUG ===");
        log.info("Table: {}.{}", schema, tableName);

        // Build parameter mapping
        Map<String, String> apiToDbColumnMap = new HashMap<>();
        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (param.getKey() != null) {
                    String dbColumnName = param.getDbColumn();
                    if (dbColumnName == null || dbColumnName.isEmpty()) {
                        dbColumnName = param.getDbParameter();
                    }
                    if (dbColumnName == null || dbColumnName.isEmpty()) {
                        dbColumnName = param.getKey();
                    }
                    apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toLowerCase());
                }
            }
        }

        // Process body parameters
        Map<String, Object> processedParams = new HashMap<>();
        String body = null;
        boolean isXmlBody = false;
        boolean isJsonBody = false;

        if (params.containsKey("_xml")) {
            Object xmlObj = params.get("_xml");
            if (xmlObj instanceof String) {
                String xmlString = (String) xmlObj;
                if (xmlString.trim().startsWith("<")) {
                    isXmlBody = true;
                    body = xmlString;
                    log.info("XML BODY DETECTED in DELETE operation!");

                    Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, true);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                    }
                }
            }
        }

        if (!isXmlBody && params.containsKey("_json")) {
            Object jsonObj = params.get("_json");
            if (jsonObj instanceof String) {
                String jsonString = (String) jsonObj;
                if (jsonString.trim().startsWith("{") || jsonString.trim().startsWith("[")) {
                    isJsonBody = true;
                    body = jsonString;
                    log.info("JSON BODY DETECTED in DELETE operation!");

                    Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, false);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                    }
                }
            }
        }

        // Copy all parameters
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("_xml".equals(key) || "_json".equals(key)) {
                continue;
            }

            String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
            processedParams.put(dbColumnName, entry.getValue());
        }

        // Handle collection/array parameters
        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    processedParams.put(entry.getKey(), collection.iterator().next());
                } else {
                    processedParams.put(entry.getKey(), null);
                }
            }
        }

        log.info("Processed params for DELETE: {}", processedParams.keySet());

        StringBuilder whereClause = new StringBuilder();
        List<Object> whereValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            } else {
                whereClause.append(" WHERE ");
            }
            whereClause.append(entry.getKey()).append(" = ?");
            whereValues.add(entry.getValue());
        }

        String sql = "DELETE FROM " + (schema != null && !schema.isEmpty() ? schema + "." : "") + tableName + whereClause;

        try {
            int rowsAffected = postgresqlJdbcTemplate.update(sql, whereValues.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Delete successful" : "No rows deleted");

            return result;

        } catch (Exception e) {
            log.error("Error executing DELETE on {}: {}", tableName, e.getMessage(), e);

            // Get the raw PostgreSQL error
            String rawError = extractFullPostgreSQLError(e);

            // Throw raw error
            throw new RuntimeException(rawError, e);
        }
    }


    private Object convertParameterValue(Object value, String dbColumn, GeneratedApiEntity api) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }

        // Find the parameter definition for this column
        ApiParameterEntity paramDef = api.getParameters().stream()
                .filter(p -> dbColumn.equalsIgnoreCase(p.getDbColumn()))
                .findFirst()
                .orElse(null);

        if (paramDef == null) {
            return value;
        }

        String oracleType = paramDef.getOracleType();
        if (oracleType == null) {
            oracleType = paramDef.getApiType();
        }

        if (oracleType == null) {
            return value;
        }

        String type = oracleType.toUpperCase();
        String stringValue = value.toString();

        try {
            if (type.contains("BOOL")) {
                // Handle boolean conversion
                if ("true".equalsIgnoreCase(stringValue) || "1".equals(stringValue) || "yes".equalsIgnoreCase(stringValue)) {
                    return true;
                }
                if ("false".equalsIgnoreCase(stringValue) || "0".equals(stringValue) || "no".equalsIgnoreCase(stringValue)) {
                    return false;
                }
                return Boolean.parseBoolean(stringValue);
            }
            else if (type.contains("INT") || type.contains("NUMERIC")) {
                // Handle numeric conversion
                if (stringValue.matches("-?\\d+")) {
                    return Long.parseLong(stringValue);
                }
                if (stringValue.matches("-?\\d+\\.\\d+")) {
                    return Double.parseDouble(stringValue);
                }
                return value;
            }
            else if (type.contains("DATE") || type.contains("TIMESTAMP")) {
                // Handle date conversion (you may need to parse different formats)
                try {
                    return java.sql.Timestamp.valueOf(stringValue);
                } catch (Exception e) {
                    try {
                        return java.sql.Date.valueOf(stringValue);
                    } catch (Exception e2) {
                        return value;
                    }
                }
            }
            else {
                return value;
            }
        } catch (Exception e) {
            log.warn("Failed to convert value '{}' to type {}: {}", stringValue, type, e.getMessage());
            return value;
        }
    }
}