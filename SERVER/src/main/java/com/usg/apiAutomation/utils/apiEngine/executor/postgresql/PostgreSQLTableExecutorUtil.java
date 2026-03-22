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

        // Copy all parameters
        Map<String, Object> allParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("_xml".equals(key) || "_json".equals(key)) {
                continue;
            }
            String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
            allParams.put(dbColumnName, entry.getValue());
        }
        allParams.putAll(processedParams);

        log.info("All params before type conversion: {}", allParams);

        // Convert to proper types
        Map<String, Object> typedParams = new HashMap<>();

        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            String dbColumnName = entry.getKey();
            Object value = entry.getValue();

            // First, try to find the parameter definition for this column
            ApiParameterEntity paramDef = api.getParameters().stream()
                    .filter(p -> dbColumnName.equalsIgnoreCase(p.getDbColumn()) ||
                            dbColumnName.equalsIgnoreCase(p.getKey()))
                    .findFirst()
                    .orElse(null);

            Object convertedValue = null;

            if (paramDef != null) {
                // We have parameter definition, use it for conversion
                convertedValue = convertParameterValueWithDefinition(value, paramDef);
                log.debug("Converted param {} using definition: {} -> {}", dbColumnName, value, convertedValue);
            } else {
                // No parameter definition, try automatic conversion for dates
                if (value instanceof String) {
                    String strValue = (String) value;
                    // Check if it looks like a timestamp
                    if (strValue.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?.*")) {
                        convertedValue = convertToTimestamp(strValue);
                        log.debug("Auto-converted param {} as timestamp: {} -> {}", dbColumnName, value, convertedValue);
                    } else {
                        convertedValue = value;
                    }
                } else {
                    convertedValue = value;
                }
            }

            if (convertedValue != null) {
                typedParams.put(dbColumnName, convertedValue);
                log.debug("Added param: {} = {} (type: {})", dbColumnName, convertedValue,
                        convertedValue != null ? convertedValue.getClass().getSimpleName() : "null");
            } else {
                log.debug("Skipped null/empty param: {}", dbColumnName);
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
                log.debug("Column: {} = {} (type: {})", entry.getKey(), entry.getValue(),
                        entry.getValue().getClass().getSimpleName());
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
            String rawError = extractFullPostgreSQLError(e);
            throw new RuntimeException(rawError, e);
        }
    }

    private Object convertParameterValueWithDefinition(Object value, ApiParameterEntity paramDef) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }

        String stringValue = value.toString();

        // AUTO-DETECT date/time strings even if type is not explicitly set
        boolean looksLikeDate = stringValue.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?.*");

        // Check if it looks like a UUID
        boolean looksLikeUUID = stringValue.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        String oracleType = paramDef.getOracleType();
        if (oracleType == null) {
            oracleType = paramDef.getApiType();
        }

        // If no type defined but it looks like a UUID, convert it
        if (oracleType == null && looksLikeUUID) {
            try {
                log.info("Auto-converting param {} as UUID: {}", paramDef.getKey(), stringValue);
                return java.util.UUID.fromString(stringValue);
            } catch (IllegalArgumentException e) {
                log.debug("Failed to parse as UUID: {}", e.getMessage());
            }
        }

        // If no type defined but it looks like a date, convert it
        if (oracleType == null) {
            if (looksLikeDate) {
                log.info("Auto-converting param {} as date (no type defined): {}", paramDef.getKey(), stringValue);
                return convertToTimestamp(stringValue);
            }
            return value;
        }

        String type = oracleType.toUpperCase();

        try {
            if (type.contains("BOOL")) {
                if ("true".equalsIgnoreCase(stringValue) || "1".equals(stringValue) || "yes".equalsIgnoreCase(stringValue)) {
                    return true;
                }
                if ("false".equalsIgnoreCase(stringValue) || "0".equals(stringValue) || "no".equalsIgnoreCase(stringValue)) {
                    return false;
                }
                return Boolean.parseBoolean(stringValue);
            }
            else if (type.contains("INT") || type.contains("NUMERIC")) {
                if (stringValue.matches("-?\\d+")) {
                    return Long.parseLong(stringValue);
                }
                if (stringValue.matches("-?\\d+\\.\\d+")) {
                    return Double.parseDouble(stringValue);
                }
                return value;
            }
            else if (type.contains("DATE") || type.contains("TIMESTAMP")) {
                log.info("Converting date value: {} with type: {}", stringValue, type);
                return convertToTimestamp(stringValue);
            }
            else if (type.contains("UUID")) {
                log.info("Converting UUID value: {} with type: {}", stringValue, type);
                try {
                    return java.util.UUID.fromString(stringValue);
                } catch (IllegalArgumentException e) {
                    log.warn("Failed to parse UUID: {}", stringValue);
                    return value;
                }
            }
            else {
                // Even if type doesn't contain DATE/TIMESTAMP, check if it looks like a date
                if (looksLikeDate) {
                    log.info("Auto-converting param {} as date (type {} doesn't match): {}", paramDef.getKey(), type, stringValue);
                    return convertToTimestamp(stringValue);
                }
                // Check if it looks like a UUID
                if (looksLikeUUID) {
                    try {
                        log.info("Auto-converting param {} as UUID: {}", paramDef.getKey(), stringValue);
                        return java.util.UUID.fromString(stringValue);
                    } catch (IllegalArgumentException e) {
                        log.debug("Failed to parse as UUID: {}", e.getMessage());
                    }
                }
                return value;
            }
        } catch (Exception e) {
            log.warn("Failed to convert value '{}' to type {}: {}", stringValue, type, e.getMessage());
            return value;
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
                // FIXED: Properly handle ISO 8601 datetime strings
                return convertToTimestamp(stringValue);
            }
            else {
                return value;
            }
        } catch (Exception e) {
            log.warn("Failed to convert value '{}' to type {}: {}", stringValue, type, e.getMessage());
            return value;
        }
    }

    /**
     * Convert various datetime formats to java.sql.Timestamp
     * Handles ISO 8601 formats like:
     * - 2026-03-22T19:07:59.830+00:00
     * - 2026-03-22T19:07:59.830Z
     * - 2026-03-22 19:07:59.830
     */
    private Object convertToTimestamp(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove any leading/trailing whitespace
            dateStr = dateStr.trim();

            // Check if it's already in JDBC format
            try {
                return java.sql.Timestamp.valueOf(dateStr);
            } catch (IllegalArgumentException e) {
                // Not in standard format, continue
            }

            // Handle ISO 8601 format: 2026-03-22T19:07:59.830+00:00 or 2026-03-22T19:07:59.830Z
            if (dateStr.contains("T")) {
                // Remove timezone part (everything after + or - or Z)
                String dateTimePart = dateStr.split("[+-]")[0];
                dateTimePart = dateTimePart.replace("Z", "");

                // Replace T with space
                String jdbcFormat = dateTimePart.replace("T", " ");

                // Handle milliseconds (ensure proper format)
                if (jdbcFormat.contains(".")) {
                    // Keep as is
                } else {
                    jdbcFormat = jdbcFormat + ".0";
                }

                try {
                    return java.sql.Timestamp.valueOf(jdbcFormat);
                } catch (IllegalArgumentException e) {
                    log.debug("Failed to parse as JDBC format after conversion: {}", jdbcFormat);
                }
            }

            // Try to parse with OffsetDateTime directly and convert to LocalDateTime
            try {
                // Handle Zulu time
                String tempStr = dateStr;
                if (tempStr.endsWith("Z")) {
                    tempStr = tempStr.substring(0, tempStr.length() - 1) + "+00:00";
                }

                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(tempStr);
                // Convert to LocalDateTime (strip timezone) for timestamp without time zone
                java.time.LocalDateTime ldt = odt.toLocalDateTime();
                return java.sql.Timestamp.valueOf(ldt);
            } catch (Exception e) {
                log.debug("Failed to parse as OffsetDateTime: {}", e.getMessage());
            }

            // Try to parse with LocalDateTime directly (without timezone)
            try {
                String noZoneStr = dateStr.split("[+-]")[0].replace("T", " ");
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(noZoneStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                return java.sql.Timestamp.valueOf(ldt);
            } catch (Exception e) {
                log.debug("Failed to parse as LocalDateTime: {}", e.getMessage());
            }

            // Try date only format
            try {
                return java.sql.Timestamp.valueOf(dateStr + " 00:00:00.000");
            } catch (Exception e) {
                // Still can't parse
            }

            // If all else fails, log warning and return the original string
            log.warn("Unable to parse date string: {}, keeping as string", dateStr);
            return dateStr;

        } catch (Exception e) {
            log.warn("Error converting date string '{}': {}", dateStr, e.getMessage());
            return dateStr;
        }
    }


    /**
     * Pre-process parameters to convert ISO 8601 datetime strings to Timestamp
     */
    private Map<String, Object> preProcessDateTimeParameters(Map<String, Object> params, GeneratedApiEntity api) {
        Map<String, Object> processed = new HashMap<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();

            // Check if this parameter corresponds to a date/timestamp column
            boolean isDateTimeColumn = api.getParameters().stream()
                    .filter(p -> key.equalsIgnoreCase(p.getKey()) || key.equalsIgnoreCase(p.getDbColumn()))
                    .anyMatch(p -> {
                        String type = p.getOracleType();
                        if (type == null) type = p.getApiType();
                        return type != null && (type.toUpperCase().contains("DATE") || type.toUpperCase().contains("TIMESTAMP"));
                    });

            if (isDateTimeColumn && value instanceof String) {
                // Convert ISO 8601 string to Timestamp
                value = convertToTimestamp((String) value);
            }

            processed.put(key, value);
        }

        return processed;
    }
}