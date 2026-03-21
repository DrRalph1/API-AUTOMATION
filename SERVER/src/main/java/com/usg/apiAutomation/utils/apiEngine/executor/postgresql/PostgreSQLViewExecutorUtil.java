package com.usg.apiAutomation.utils.apiEngine.executor.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.utils.apiEngine.PostgreSQLObjectResolverUtil;
import com.usg.apiAutomation.utils.apiEngine.PostgreSQLParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgreSQLViewExecutorUtil {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    private final PostgreSQLParameterValidatorUtil parameterValidator;
    private final PostgreSQLObjectResolverUtil objectResolver;
    private final PostgreSQLTableExecutorUtil tableExecutorUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String viewName, String schema, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ POSTGRESQL VIEW EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("View Name parameter: {}", viewName);
        log.info("Schema parameter: {}", schema);
        log.info("Request Body Type: {}", request.getBody() != null ? request.getBody().getClass().getName() : "null");
        log.info("Request Body: {}", request.getBody());

        // ============ CREATE PARAMETER MAPPING ============
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

        // ============ HANDLE XML/JSON BODY ============
        Map<String, Object> dbParams = new HashMap<>();
        String body = null;
        boolean isXmlBody = false;
        boolean isJsonBody = false;
        boolean hasBodyParameter = false;

        if (request.getBody() != null) {
            if (request.getBody() instanceof String) {
                String bodyString = (String) request.getBody();
                String trimmed = bodyString.trim();

                if (trimmed.startsWith("<")) {
                    isXmlBody = true;
                    body = bodyString;
                    log.info("XML BODY DETECTED!");
                    log.info("XML Length: {} characters", body.length());
                    log.info("XML Preview: {}", body.substring(0, Math.min(500, body.length())));
                } else if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                    isJsonBody = true;
                    body = bodyString;
                    log.info("JSON BODY DETECTED!");
                    log.info("JSON Preview: {}", body.substring(0, Math.min(200, body.length())));
                } else {
                    log.info("String body detected (non-XML/JSON): {}", bodyString.substring(0, Math.min(100, bodyString.length())));
                    if (!bodyString.isEmpty() && !bodyString.equals("{}")) {
                        dbParams.put("body_string", bodyString);
                    }
                }
            } else if (request.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
                isJsonBody = true;
                log.info("JSON body detected with keys: {}", bodyMap.keySet());
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    String paramKey = entry.getKey().toLowerCase();
                    String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toLowerCase());

                    Object value = entry.getValue();
                    if (value instanceof Map || value instanceof List) {
                        try {
                            value = objectMapper.writeValueAsString(value);
                            log.debug("Converted complex object to JSON string for parameter: {}", dbColumnName);
                        } catch (Exception e) {
                            log.warn("Failed to convert complex object to string: {}", e.getMessage());
                        }
                    }

                    dbParams.put(dbColumnName, value);
                    log.debug("Added JSON param: {} -> {} = {}", entry.getKey(), dbColumnName, value);
                }
            }
        }

        // ============ PROCESS XML/JSON BODY ============
        if ((isXmlBody || isJsonBody) && body != null) {
            log.info("Processing {} body for view execution", isXmlBody ? "XML" : "JSON");

            // Try to extract individual parameters from body
            Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, isXmlBody);
            if (!extractedParams.isEmpty()) {
                dbParams.putAll(extractedParams);
                log.info("✅ Extracted {} parameters from {} and added to dbParams", extractedParams.size(), isXmlBody ? "XML" : "JSON");
                log.info("Extracted params: {}", extractedParams.keySet());
            }

            // Look for explicit body parameter in API configuration
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String paramKey = param.getKey().toLowerCase();
                    String dbColumnName = getDbColumnName(param);

                    boolean isBodyParameter = paramKey.contains("xml") ||
                            paramKey.contains("json") ||
                            paramKey.contains("text") ||
                            paramKey.contains("where") ||
                            paramKey.contains("filter") ||
                            paramKey.contains("condition");

                    if (isBodyParameter && dbColumnName != null && !dbParams.containsKey(dbColumnName)) {
                        dbParams.put(dbColumnName, body);
                        hasBodyParameter = true;
                        log.info("✅ Mapped full {} body to database column: {}", isXmlBody ? "XML" : "JSON", dbColumnName);
                        break;
                    }
                }
            }

            // If no explicit parameter found, store as WHERE_CONDITION
            if (!hasBodyParameter && extractedParams.isEmpty()) {
                log.warn("⚠️ No suitable database column found for {} body. Will be stored as WHERE_CONDITION.", isXmlBody ? "XML" : "JSON");
                dbParams.put("where_condition", body);
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbColumnName, entry.getValue());
                log.debug("Added path param: {} -> {} = {}", entry.getKey(), dbColumnName, entry.getValue());
            }
            log.info("Path params added: {}", request.getPathParams().keySet());
        }

        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbColumnName, entry.getValue());
                log.debug("Added query param: {} -> {} = {}", entry.getKey(), dbColumnName, entry.getValue());
            }
            log.info("Query params added: {}", request.getQueryParams().keySet());
        }

        // ============ ADD HEADERS AS PARAMETERS ============
        Map<String, Object> headerParams = new HashMap<>();
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String headerKey = entry.getKey().toLowerCase();
                boolean headerIsParameter = api.getParameters() != null && api.getParameters().stream()
                        .anyMatch(p -> p.getKey().equalsIgnoreCase(headerKey));

                if (headerIsParameter) {
                    String dbColumnName = apiToDbColumnMap.getOrDefault(headerKey, headerKey.toLowerCase());
                    dbParams.put(dbColumnName, entry.getValue());
                    headerParams.put(headerKey, entry.getValue());
                    log.debug("Added header as parameter: {} -> {} = {}", headerKey, dbColumnName, entry.getValue());
                } else {
                    headerParams.put(headerKey, entry.getValue());
                    log.debug("Header stored for reference: {} = {}", headerKey, entry.getValue());
                }
            }
            log.info("Headers added: {}", request.getHeaders().keySet());
        }

        // ============ HANDLE COLLECTION/ARRAY PARAMETERS ============
        for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    dbParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value", entry.getKey());
                } else {
                    dbParams.put(entry.getKey(), null);
                }
            }
        }

        log.info("Final DB params for view execution: {}", dbParams.keySet());

        // ============ SCHEMA RESOLUTION STRATEGY ============
        String resolvedSchema = resolveSchema(schema, sourceObject, api, viewName);
        String resolvedViewName = viewName != null ? viewName.toLowerCase() : null;

        if (resolvedViewName == null) {
            throw new ValidationException("View name cannot be null");
        }

        log.info("Final resolved schema: {}", resolvedSchema);
        log.info("Final view name: {}", resolvedViewName);

        // ============ RESOLVE VIEW (PostgreSQL doesn't have synonyms) ============
        String targetSchema = resolvedSchema;
        String targetName = resolvedViewName;

        // ==================== VALIDATION STEP 1: Validate view is accessible ====================
        try {
            objectResolver.validateDatabaseObject(targetSchema, targetName, "VIEW");
            log.info("✅ View {}.{} exists and is accessible", targetSchema, targetName);
        } catch (ValidationException e) {
            log.error("❌ View validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== VALIDATION STEP 2: Validate view query parameters ====================
        try {
            List<String> allowedColumns = new ArrayList<>();
            if (api.getResponseMappings() != null) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (mapping.getDbColumn() != null) {
                        allowedColumns.add(mapping.getDbColumn().toLowerCase());
                    }
                }
            }

            // Filter parameters for validation
            Set<String> configuredParamKeys = new HashSet<>();
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    configuredParamKeys.add(param.getKey().toLowerCase());
                }
            }

            log.info("Configured parameter keys: {}", configuredParamKeys);

            Map<String, Object> queryParamsForValidation = new HashMap<>();
            for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
                String key = entry.getKey().toLowerCase();
                boolean isConfigured = configuredParamKeys.contains(key);
                if (isConfigured || "page".equals(key) || "pagesize".equals(key) ||
                        "sort".equals(key) || "order".equals(key) ||
                        "where_condition".equals(key)) {
                    queryParamsForValidation.put(key, entry.getValue());
                }
            }

            log.info("Validating view query with filtered params: {}", queryParamsForValidation.keySet());

            validateViewQuery(targetSchema, targetName, queryParamsForValidation, allowedColumns);
            log.info("✅ View query validation passed");
        } catch (ValidationException e) {
            log.error("❌ View query validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== VALIDATION STEP 3: Validate all parameters ====================
        try {
            parameterValidator.validateParameters(configuredParamDTOs, dbParams, targetSchema, targetName);
            log.info("✅ All parameter validations passed for view {}.{}", targetSchema, targetName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        log.info("============ VIEW EXECUTION COMPLETE ============");

        // Execute the SELECT on the view using TableExecutorUtil
        return tableExecutorUtil.executeSelect(targetName, targetSchema, dbParams, api, configuredParamDTOs);
    }

    /**
     * Helper method to resolve schema from multiple sources
     */
    private String resolveSchema(String schema, ApiSourceObjectDTO sourceObject,
                                 GeneratedApiEntity api, String viewName) {
        // Strategy 1: Use the schema parameter
        if (schema != null && !schema.trim().isEmpty()) {
            log.info("Strategy 1 - Using schema parameter: {}", schema);
            return schema.trim().toLowerCase();
        }

        // Strategy 2: Try sourceObject.getOwner()
        if (sourceObject != null && sourceObject.getOwner() != null && !sourceObject.getOwner().trim().isEmpty()) {
            log.info("Strategy 2 - Using sourceObject.getOwner(): {}", sourceObject.getOwner());
            return sourceObject.getOwner().trim().toLowerCase();
        }

        // Strategy 3: Try sourceObject.getSchemaName()
        if (sourceObject != null && sourceObject.getSchemaName() != null && !sourceObject.getSchemaName().trim().isEmpty()) {
            log.info("Strategy 3 - Using sourceObject.getSchemaName(): {}", sourceObject.getSchemaName());
            return sourceObject.getSchemaName().trim().toLowerCase();
        }

        // Strategy 4: Get from API's source_object_info
        if (api != null && api.getSourceObjectInfo() != null) {
            try {
                if (api.getSourceObjectInfo() instanceof Map) {
                    Map<String, Object> sourceInfo = (Map<String, Object>) api.getSourceObjectInfo();
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        String schemaName = sourceInfo.get("schemaName").toString();
                        log.info("Strategy 4 - Using schemaName from source_object_info Map: {}", schemaName);
                        return schemaName.trim().toLowerCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        String ownerName = sourceInfo.get("owner").toString();
                        log.info("Strategy 4 - Using owner from source_object_info Map: {}", ownerName);
                        return ownerName.trim().toLowerCase();
                    }
                } else {
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        String schemaName = sourceInfo.get("schemaName").toString();
                        log.info("Strategy 4 - Using schemaName from source_object_info JSON: {}", schemaName);
                        return schemaName.trim().toLowerCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        String ownerName = sourceInfo.get("owner").toString();
                        log.info("Strategy 4 - Using owner from source_object_info JSON: {}", ownerName);
                        return ownerName.trim().toLowerCase();
                    }
                }
            } catch (Exception e) {
                log.warn("Could not process sourceObjectInfo: {}", e.getMessage());
            }
        }

        // Strategy 5: Try to get current schema
        try {
            String currentSchema = postgresqlJdbcTemplate.queryForObject("SELECT current_schema()", String.class);
            if (currentSchema != null && !currentSchema.isEmpty()) {
                log.info("Strategy 5 - Using current schema: {}", currentSchema);
                return currentSchema;
            }
        } catch (Exception e) {
            log.warn("Could not get current schema: {}", e.getMessage());
        }

        // Strategy 6: Try to locate the view in accessible schemas
        try {
            log.info("Strategy 6 - Attempting to locate view '{}' in accessible schemas", viewName);

            String findViewSql = "SELECT table_schema FROM information_schema.views " +
                    "WHERE table_name = ? AND table_schema NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            List<String> schemas = postgresqlJdbcTemplate.queryForList(findViewSql, String.class, viewName);

            if (!schemas.isEmpty()) {
                String foundSchema = schemas.get(0);
                log.info("Strategy 6 - Found view '{}' in schema: {}", viewName, foundSchema);
                return foundSchema;
            }

            // If not found in views, check tables
            String findTableSql = "SELECT table_schema FROM information_schema.tables " +
                    "WHERE table_name = ? AND table_type = 'BASE TABLE' " +
                    "AND table_schema NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            List<String> tableSchemas = postgresqlJdbcTemplate.queryForList(findTableSql, String.class, viewName);

            if (!tableSchemas.isEmpty()) {
                String foundSchema = tableSchemas.get(0);
                log.info("Strategy 6 - Found table '{}' in schema: {} (treating as view)", viewName, foundSchema);
                return foundSchema;
            }

            log.warn("Strategy 6 - Could not locate view '{}' in any accessible schema", viewName);

        } catch (Exception e) {
            log.warn("Error while searching for view in accessible schemas: {}", e.getMessage());
        }

        // Strategy 7: Default to 'public' schema
        log.info("Strategy 7 - Using default 'public' schema");
        return "public";
    }

    private String getDbColumnName(ApiParameterEntity param) {
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toLowerCase();
        }
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toLowerCase();
        }
        return param.getKey().toLowerCase();
    }

    private void validateViewQuery(String schemaName, String viewName, Map<String, Object> queryParams,
                                   List<String> allowedColumns) {
        // Check if view exists
        String sql = "SELECT COUNT(*) FROM information_schema.views " +
                "WHERE table_schema = ? AND table_name = ?";

        Integer count = postgresqlJdbcTemplate.queryForObject(sql, Integer.class, schemaName, viewName);
        if (count == null || count == 0) {
            throw new ValidationException(
                    String.format("View '%s.%s' does not exist", schemaName, viewName)
            );
        }

        // Validate view is accessible
        try {
            postgresqlJdbcTemplate.execute("SELECT 1 FROM " + schemaName + "." + viewName + " LIMIT 1");
        } catch (Exception e) {
            throw new ValidationException(
                    String.format("Cannot access view '%s.%s': %s",
                            schemaName, viewName, extractPostgreSQLError(e.getMessage()))
            );
        }

        // Validate query parameters against view columns
        if (queryParams != null && !queryParams.isEmpty()) {
            // Get view columns
            String columnSql = "SELECT column_name, data_type, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ?";

            List<Map<String, Object>> columns = postgresqlJdbcTemplate.queryForList(
                    columnSql, schemaName, viewName);

            Map<String, Map<String, Object>> columnMap = new HashMap<>();
            for (Map<String, Object> column : columns) {
                columnMap.put(((String) column.get("column_name")).toLowerCase(), column);
            }

            // Validate each query parameter
            for (Map.Entry<String, Object> param : queryParams.entrySet()) {
                String paramName = param.getKey().toLowerCase();

                // Skip special parameters that aren't view columns
                if ("page".equals(paramName) || "pagesize".equals(paramName) ||
                        "sort".equals(paramName) || "order".equals(paramName) ||
                        "where_condition".equals(paramName)) {
                    continue;
                }

                Map<String, Object> column = columnMap.get(paramName);
                if (column == null && allowedColumns != null && !allowedColumns.contains(paramName)) {
                    throw new ValidationException(
                            String.format("Invalid query parameter '%s'. Not a valid column in view %s.%s",
                                    param.getKey(), schemaName, viewName)
                    );
                }

                // Validate data type if column exists
                if (column != null && param.getValue() != null) {
                    validateColumnDataType(param.getKey(), param.getValue(), column);
                }
            }
        }
    }

    private void validateColumnDataType(String columnName, Object value, Map<String, Object> column) {
        String dataType = (String) column.get("data_type");
        String nullable = (String) column.get("is_nullable");

        // Check for NOT NULL constraint
        if ("NO".equals(nullable) && (value == null || value.toString().trim().isEmpty())) {
            throw new ValidationException(
                    String.format("Column '%s' cannot be null", columnName)
            );
        }

        // Basic data type validation
        if (value != null) {
            String lowerDataType = dataType != null ? dataType.toLowerCase() : "";

            if (lowerDataType.contains("int") || lowerDataType.contains("integer") ||
                    lowerDataType.contains("smallint") || lowerDataType.contains("bigint") ||
                    lowerDataType.contains("decimal") || lowerDataType.contains("numeric") ||
                    lowerDataType.contains("real") || lowerDataType.contains("double") ||
                    lowerDataType.contains("float")) {
                if (!(value instanceof Number)) {
                    try {
                        new java.math.BigDecimal(value.toString());
                    } catch (NumberFormatException e) {
                        throw new ValidationException(
                                String.format("Column '%s' expects a numeric value, but got: %s",
                                        columnName, value)
                        );
                    }
                }
            } else if (lowerDataType.contains("date") || lowerDataType.contains("time") ||
                    lowerDataType.contains("timestamp")) {
                // Basic date validation
                if (!(value instanceof java.util.Date) && !(value instanceof java.sql.Timestamp) &&
                        !(value instanceof java.sql.Date) && !(value instanceof java.sql.Time)) {
                    log.debug("Date/time value provided for column {}: {}", columnName, value);
                }
            } else if (lowerDataType.contains("bool") || lowerDataType.contains("boolean")) {
                if (!(value instanceof Boolean)) {
                    String strValue = value.toString().toLowerCase();
                    if (!strValue.equals("true") && !strValue.equals("false") &&
                            !strValue.equals("1") && !strValue.equals("0") &&
                            !strValue.equals("yes") && !strValue.equals("no") &&
                            !strValue.equals("y") && !strValue.equals("n")) {
                        throw new ValidationException(
                                String.format("Column '%s' expects a boolean value, but got: %s",
                                        columnName, value)
                        );
                    }
                }
            } else if (lowerDataType.contains("json") || lowerDataType.contains("jsonb")) {
                // JSON validation is basic - PostgreSQL will handle full validation
                try {
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(value.toString());
                } catch (Exception e) {
                    log.debug("JSON value for column {}: {}", columnName, value);
                }
            }
        }
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
                            String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toLowerCase());
                            extractedParams.put(dbColumnName, value);
                            log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbColumnName, value);
                        } else {
                            log.info("⚠️ XML tag <{}> found but empty", paramKey);
                            String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toLowerCase());
                            extractedParams.put(dbColumnName, "");
                        }
                    }
                }
            } else {
                // JSON parsing
                Map<String, Object> jsonMap = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    String paramKey = entry.getKey().toLowerCase();
                    String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
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

    /**
     * Helper method to extract PostgreSQL error message
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
}