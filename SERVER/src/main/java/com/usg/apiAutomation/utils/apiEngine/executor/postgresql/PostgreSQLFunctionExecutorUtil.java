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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PostgreSQLFunctionExecutorUtil {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    private final PostgreSQLParameterValidatorUtil parameterValidatorUtil;
    private final PostgreSQLObjectResolverUtil objectResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Timeout constants
    private static final int STATEMENT_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 30000;

    public PostgreSQLFunctionExecutorUtil(
            PostgreSQLParameterValidatorUtil parameterValidatorUtil,
            PostgreSQLObjectResolverUtil objectResolver) {
        this.parameterValidatorUtil = parameterValidatorUtil;
        this.objectResolver = objectResolver;
    }

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String functionName, String schema, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ POSTGRESQL FUNCTION EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("Function Name parameter: {}", functionName);
        log.info("Schema parameter: {}", schema);

        // ============ CREATE PARAMETER MAPPING ============
        Map<String, String> apiToDbParamMap = new HashMap<>();
        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (param.getKey() != null) {
                    String dbParamName = param.getDbParameter();
                    if (dbParamName == null || dbParamName.isEmpty()) {
                        dbParamName = param.getDbColumn();
                    }
                    if (dbParamName == null || dbParamName.isEmpty()) {
                        dbParamName = param.getKey();
                    }
                    apiToDbParamMap.put(param.getKey().toLowerCase(), dbParamName.toLowerCase());
                    log.info("Parameter mapping: API '{}' -> Database '{}'", param.getKey(), dbParamName.toLowerCase());
                }
            }
        }

        // ============ HANDLE XML/JSON BODY ============
        Map<String, Object> dbParams = new HashMap<>();
        String xmlBody = null;
        boolean isXmlBody = false;
        boolean hasBodyParameter = false;

        // Check if request body is a String and looks like XML or JSON
        if (request.getBody() != null) {
            if (request.getBody() instanceof String) {
                String bodyString = (String) request.getBody();
                String trimmed = bodyString.trim();

                if (trimmed.startsWith("<")) {
                    isXmlBody = true;
                    xmlBody = bodyString;
                    log.info("XML BODY DETECTED! Length: {} characters", xmlBody.length());
                } else if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                    log.info("JSON BODY DETECTED!");
                    // Parse JSON body
                    try {
                        Map<String, Object> jsonMap = objectMapper.readValue(bodyString, new TypeReference<Map<String, Object>>() {});
                        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                            String paramKey = entry.getKey().toLowerCase();
                            String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());

                            Object value = entry.getValue();
                            if (value instanceof Map || value instanceof List) {
                                value = objectMapper.writeValueAsString(value);
                            }

                            dbParams.put(dbParamName, value);
                            log.debug("Added JSON param: {} -> {} = {}", entry.getKey(), dbParamName, value);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse JSON body: {}", e.getMessage());
                        dbParams.put("request_body", bodyString);
                    }
                } else {
                    log.info("String body detected");
                    dbParams.put("request_body", bodyString);
                }
            } else if (request.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
                log.info("JSON body detected with keys: {}", bodyMap.keySet());
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    String paramKey = entry.getKey().toLowerCase();
                    String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());

                    Object value = entry.getValue();
                    if (value instanceof Map || value instanceof List) {
                        try {
                            value = objectMapper.writeValueAsString(value);
                            log.debug("Converted complex object to JSON string for parameter: {}", dbParamName);
                        } catch (Exception e) {
                            log.warn("Failed to convert complex object to string: {}", e.getMessage());
                        }
                    }

                    dbParams.put(dbParamName, value);
                    log.debug("Added JSON param: {} -> {} = {}", entry.getKey(), dbParamName, value);
                }
            }
        }

        // ============ PROCESS XML BODY ============
        if (isXmlBody && xmlBody != null) {
            log.info("Processing XML body for function execution");

            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbParamMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML and added to dbParams", extractedXmlParams.size());
            }

            // Look for explicit XML/JSON parameter in API configuration
            for (ApiParameterEntity param : api.getParameters()) {
                String paramKey = param.getKey().toLowerCase();
                String dbParamName = getDbParamName(param);

                boolean isBodyParameter = paramKey.contains("xml") ||
                        paramKey.contains("json") ||
                        paramKey.contains("text") ||
                        paramKey.contains("request") ||
                        paramKey.contains("payload") ||
                        paramKey.contains("body") ||
                        paramKey.equals("_xml");

                if (isBodyParameter && dbParamName != null && !dbParams.containsKey(dbParamName)) {
                    dbParams.put(dbParamName, xmlBody);
                    hasBodyParameter = true;
                    log.info("✅ Mapped full XML body to database parameter: {}", dbParamName);
                    break;
                }
            }

            // If no explicit parameter found, look for text parameter
            if (!hasBodyParameter && extractedXmlParams.isEmpty()) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String dbParamName = getDbParamName(param);
                    if (dbParamName != null) {
                        String pgType = param.getOracleType();
                        if (pgType != null && pgType.toLowerCase().contains("text")) {
                            dbParams.put(dbParamName, xmlBody);
                            hasBodyParameter = true;
                            log.info("✅ Mapped full XML body to TEXT parameter: {}", dbParamName);
                            break;
                        }
                    }
                }
            }

            if (!hasBodyParameter && extractedXmlParams.isEmpty()) {
                log.warn("⚠️ No suitable database parameter found for XML body");
                dbParams.put("xml_body", xmlBody);
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
        }

        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
        }

        // ============ ADD HEADERS AS PARAMETERS ============
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String headerKey = entry.getKey().toLowerCase();
                boolean headerIsParameter = api.getParameters().stream()
                        .anyMatch(p -> p.getKey().equalsIgnoreCase(headerKey));

                if (headerIsParameter) {
                    String dbParamName = apiToDbParamMap.getOrDefault(headerKey, headerKey.toLowerCase());
                    dbParams.put(dbParamName, entry.getValue());
                    log.debug("Added header as parameter: {} -> {} = {}", headerKey, dbParamName, entry.getValue());
                }
            }
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

        log.info("Final DB params prepared: {}", dbParams.keySet());

        // ============ SCHEMA RESOLUTION STRATEGY ============
        String pgSchema = resolveSchema(schema, sourceObject, api, functionName);

        if (pgSchema == null || pgSchema.trim().isEmpty()) {
            log.error("❌ COULD NOT DETERMINE SCHEMA NAME");
            throw new ValidationException(
                    "Could not determine the database schema for function: " + functionName
            );
        }

        pgSchema = pgSchema.toLowerCase();
        String pgFunctionName = functionName != null ? functionName.trim().toLowerCase() : null;

        log.info("Final resolved schema: {}", pgSchema);
        log.info("Final function name: {}", pgFunctionName);

        // ============ RESOLVE FUNCTION ============
        String actualSchema = pgSchema;
        String actualFunctionName = pgFunctionName;

        // ==================== VALIDATION STEP 1: Validate function exists ====================
        try {
            objectResolver.validateDatabaseObject(actualSchema, actualFunctionName, "FUNCTION");
            log.info("✅ Function {}.{} exists", actualSchema, actualFunctionName);
        } catch (EmptyResultDataAccessException e) {
            log.error("❌ Function {}.{} does not exist", actualSchema, actualFunctionName);
            throw new ValidationException(
                    String.format("The function '%s.%s' does not exist or you don't have access to it.",
                            actualSchema, actualFunctionName)
            );
        }

        // ==================== VALIDATION STEP 2: Validate all parameters ====================
        try {
            parameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, actualSchema, actualFunctionName);
            log.info("✅ All parameter validations passed for function {}.{}", actualSchema, actualFunctionName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== EXECUTE FUNCTION WITH PROPER CONNECTION MANAGEMENT ====================
        try {
            // Get DataSource from JdbcTemplate
            DataSource dataSource = postgresqlJdbcTemplate.getDataSource();
            if (dataSource == null) {
                throw new SQLException("No DataSource available");
            }

            // Use try-with-resources to ensure connection is properly closed
            try (Connection conn = dataSource.getConnection()) {
                // Set network timeout
                conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), CONNECTION_TIMEOUT_MS);

                // Set session statement timeout
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET statement_timeout = '" + STATEMENT_TIMEOUT_SECONDS + "s'");
                    stmt.execute("SET lock_timeout = '" + STATEMENT_TIMEOUT_SECONDS + "s'");
                }

                // Build the CALL statement
                String callSql = buildCallStatement(actualSchema, actualFunctionName, api.getParameters(), dbParams);
                log.info("Executing PostgreSQL function with CALL syntax: {}", callSql);

                // Store captured notices and result
                List<String> capturedNotices = new ArrayList<>();
                Map<String, Object> finalResult = new HashMap<>();

                // Use try-with-resources for CallableStatement
                try (CallableStatement cs = conn.prepareCall(callSql)) {
                    // Set statement timeout
                    cs.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                    // Set parameters
                    setParameters(cs, api.getParameters(), dbParams);

                    // Register OUT parameters
                    registerOutParameters(cs, api.getResponseMappings(), api.getParameters(), dbParams);

                    // Execute
                    log.info("Executing function {}.{} with {} parameters", actualSchema, actualFunctionName, dbParams.size());
                    boolean hasResultSet = cs.execute();

                    // Process SQL warnings (these contain RAISE NOTICE messages)
                    SQLWarning warning = cs.getWarnings();
                    while (warning != null) {
                        if (warning.getMessage() != null) {
                            capturedNotices.add(warning.getMessage());
                            log.debug("Captured warning/notice: {}", warning.getMessage());
                        }
                        warning = warning.getNextWarning();
                    }

                    // Process captured notices
                    if (!capturedNotices.isEmpty()) {
                        log.info("Captured {} NOTICE messages from function", capturedNotices.size());

                        for (String notice : capturedNotices) {
                            if (notice != null && notice.contains("Result: ")) {
                                String jsonPart = extractJsonFromNotice(notice);
                                if (jsonPart != null) {
                                    try {
                                        Map<String, Object> jsonResult = objectMapper.readValue(jsonPart,
                                                new TypeReference<Map<String, Object>>() {});
                                        finalResult.putAll(jsonResult);
                                        log.info("✅ Parsed JSON result from NOTICE: {}", jsonResult);
                                    } catch (Exception e) {
                                        log.warn("Failed to parse JSON from notice: {}", e.getMessage());
                                        finalResult.put("notice", notice);
                                    }
                                } else {
                                    finalResult.put("notice", notice);
                                }
                            } else if (notice != null) {
                                finalResult.put("notice", notice);
                                log.info("Captured notice: {}", notice);
                            }
                        }
                    }

                    // Process results
                    Map<String, Object> responseData = processResults(cs, api, hasResultSet);

                    // Merge captured notices into response
                    if (!finalResult.isEmpty()) {
                        responseData.putAll(finalResult);
                    }

                    log.info("============ FUNCTION EXECUTION COMPLETE ============");
                    return responseData;
                }
            }
        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for {}.{}", actualSchema, actualFunctionName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing function {}.{}: {}", actualSchema, actualFunctionName, e.getMessage(), e);

            // Check if the exception contains the result JSON
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Result: ")) {
                String jsonPart = extractJsonFromNotice(errorMessage);
                if (jsonPart != null) {
                    try {
                        Map<String, Object> jsonResult = objectMapper.readValue(jsonPart,
                                new TypeReference<Map<String, Object>>() {});
                        log.info("Parsed JSON from exception: {}", jsonResult);
                        return jsonResult;
                    } catch (Exception parseEx) {
                        log.warn("Failed to parse JSON from exception: {}", jsonPart);
                    }
                }
            }

            if (errorMessage != null) {
                // PostgreSQL-specific error handling
                if (errorMessage.contains("ERROR: function") && errorMessage.contains("does not exist")) {
                    throw new ValidationException(
                            String.format("Function '%s.%s' does not exist. Please check the function name and schema.",
                                    actualSchema, actualFunctionName)
                    );
                }
                if (errorMessage.contains("ERROR: permission denied")) {
                    throw new ValidationException(
                            String.format("Insufficient privileges to execute function '%s.%s'. Details: %s",
                                    actualSchema, actualFunctionName, extractPostgreSQLError(errorMessage))
                    );
                }
            }

            throw new RuntimeException("Failed to execute the requested operation: " + extractPostgreSQLError(errorMessage), e);
        }
    }

    /**
     * Build the CALL statement for PostgreSQL function
     */
    private String buildCallStatement(String schema, String functionName,
                                      List<ApiParameterEntity> parameters,
                                      Map<String, Object> dbParams) {
        StringBuilder callBuilder = new StringBuilder("SELECT * FROM ");

        if (schema != null && !schema.isEmpty()) {
            callBuilder.append(schema).append(".");
        }

        callBuilder.append(functionName).append("(");

        List<String> paramPlaceholders = new ArrayList<>();

        if (parameters != null && !parameters.isEmpty()) {
            for (ApiParameterEntity param : parameters) {
                if (param == null || param.getKey() == null) continue;

                String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                String dbParamName = getDbParamName(param);

                if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                    String pgType = param.getOracleType();

                    // Add explicit casts for specific types
                    if (pgType != null) {
                        String lowerType = pgType.toLowerCase();
                        if (lowerType.contains("jsonb")) {
                            paramPlaceholders.add("?::jsonb");
                        } else if (lowerType.contains("json")) {
                            paramPlaceholders.add("?::json");
                        } else if (lowerType.contains("uuid")) {
                            paramPlaceholders.add("?::uuid");
                        } else if (lowerType.contains("timestamp")) {
                            paramPlaceholders.add("?::timestamp");
                        } else if (lowerType.contains("date")) {
                            paramPlaceholders.add("?::date");
                        } else if (lowerType.contains("integer") || lowerType.contains("int")) {
                            paramPlaceholders.add("?::integer");
                        } else if (lowerType.contains("bigint")) {
                            paramPlaceholders.add("?::bigint");
                        } else if (lowerType.contains("numeric") || lowerType.contains("decimal")) {
                            paramPlaceholders.add("?::numeric");
                        } else if (lowerType.contains("boolean") || lowerType.contains("bool")) {
                            paramPlaceholders.add("?::boolean");
                        } else {
                            paramPlaceholders.add("?");
                        }
                    } else {
                        paramPlaceholders.add("?");
                    }
                }
            }
        }

        // If no parameters from config, use generic placeholders
        if (paramPlaceholders.isEmpty() && !dbParams.isEmpty()) {
            for (int i = 0; i < dbParams.size(); i++) {
                paramPlaceholders.add("?");
            }
        }

        callBuilder.append(String.join(", ", paramPlaceholders));
        callBuilder.append(")");

        log.info("Built CALL statement: {}", callBuilder.toString());
        return callBuilder.toString();
    }

    /**
     * Set IN and INOUT parameters
     */
    private void setParameters(CallableStatement cs, List<ApiParameterEntity> parameters,
                               Map<String, Object> dbParams) throws SQLException {
        int index = 1;

        if (parameters != null && !parameters.isEmpty()) {
            for (ApiParameterEntity param : parameters) {
                if (param == null || param.getKey() == null) continue;

                String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                if (!paramMode.contains("IN") && !paramMode.equals("INOUT")) {
                    continue;
                }

                String dbParamName = getDbParamName(param);
                if (!dbParams.containsKey(dbParamName)) {
                    continue;
                }

                Object value = dbParams.get(dbParamName);
                if (value == null) {
                    cs.setNull(index++, Types.NULL);
                } else {
                    cs.setObject(index++, value);
                }
                log.debug("Set parameter {} (index {}): value={}", dbParamName, index - 1, value);
            }
        }
    }

    /**
     * Register OUT parameters
     */
    private void registerOutParameters(CallableStatement cs, List<ApiResponseMappingEntity> responseMappings,
                                       List<ApiParameterEntity> parameters, Map<String, Object> dbParams) throws SQLException {
        if (responseMappings == null || responseMappings.isEmpty()) {
            return;
        }

        // Calculate how many IN parameters we have
        int inParamCount = 0;
        if (parameters != null) {
            for (ApiParameterEntity param : parameters) {
                if (param == null || param.getKey() == null) continue;
                String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                String dbParamName = getDbParamName(param);
                if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                    inParamCount++;
                }
            }
        }

        // Register OUT parameters starting after IN parameters
        int outParamIndex = inParamCount + 1;
        for (ApiResponseMappingEntity mapping : responseMappings) {
            if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                String dbColumn = mapping.getDbColumn();
                String oracleType = mapping.getOracleType();
                if (dbColumn != null && !dbColumn.isEmpty()) {
                    cs.registerOutParameter(outParamIndex, mapToSqlType(oracleType));
                    log.debug("Registered OUT parameter {} at index {} with type: {}",
                            dbColumn, outParamIndex, oracleType);
                    outParamIndex++;
                }
            }
        }
    }

    /**
     * Process results from function execution
     */
    private Map<String, Object> processResults(CallableStatement cs, GeneratedApiEntity api,
                                               boolean hasResultSet) throws SQLException {
        Map<String, Object> responseData = new HashMap<>();

        // Process result set if present
        if (hasResultSet) {
            try (ResultSet rs = cs.getResultSet()) {
                if (rs != null) {
                    List<Map<String, Object>> resultSetData = new ArrayList<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            row.put(columnName.toLowerCase(), rs.getObject(i));
                        }
                        resultSetData.add(row);
                    }
                    if (!resultSetData.isEmpty()) {
                        responseData.put("data", resultSetData);
                        log.info("Retrieved {} rows from result set", resultSetData.size());
                    }
                }
            }
        }

        // Process OUT parameters
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            // Calculate how many IN parameters we have
            int inParamCount = 0;
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    if (param == null || param.getKey() == null) continue;
                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                    if (paramMode.contains("IN") || paramMode.equals("INOUT")) {
                        inParamCount++;
                    }
                }
            }

            int outParamIndex = inParamCount + 1;
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    try {
                        Object value = cs.getObject(outParamIndex);
                        if (value != null) {
                            responseData.put(mapping.getApiField(), value);
                            log.debug("Mapped OUT parameter {} to {} with value: {}",
                                    mapping.getDbColumn(), mapping.getApiField(), value);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to get OUT parameter: {}", e.getMessage());
                    }
                    outParamIndex++;
                }
            }
        }

        // If no response data, add default success
        if (responseData.isEmpty()) {
            responseData.put("success", true);
            responseData.put("message", "Function executed successfully");
        }

        return responseData;
    }

    /**
     * Extract JSON from RAISE NOTICE message
     */
    private String extractJsonFromNotice(String notice) {
        if (notice == null) return null;

        int resultIndex = notice.indexOf("Result: ");
        if (resultIndex >= 0) {
            String afterResult = notice.substring(resultIndex + 8);
            int start = afterResult.indexOf('{');
            int end = afterResult.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return afterResult.substring(start, end + 1);
            }
        }

        int start = notice.indexOf('{');
        int end = notice.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return notice.substring(start, end + 1);
        }

        return null;
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

    /**
     * Helper method to resolve schema from multiple sources
     */
    private String resolveSchema(String schema, ApiSourceObjectDTO sourceObject,
                                 GeneratedApiEntity api, String functionName) {
        // Strategy 1: Use the schema parameter passed to the method
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
            DataSource dataSource = postgresqlJdbcTemplate.getDataSource();
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT current_schema()")) {
                    if (rs.next()) {
                        String currentSchema = rs.getString(1);
                        if (currentSchema != null && !currentSchema.isEmpty()) {
                            log.info("Strategy 5 - Using current schema: {}", currentSchema);
                            return currentSchema;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not get current schema: {}", e.getMessage());
        }

        // Strategy 6: Try to locate the function in accessible schemas
        try {
            log.info("Strategy 6 - Attempting to locate function '{}' in accessible schemas", functionName);

            String findFunctionSql = "SELECT n.nspname FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE p.proname = ? AND p.prokind = 'f' " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            List<String> schemas = postgresqlJdbcTemplate.queryForList(findFunctionSql, String.class, functionName);

            if (!schemas.isEmpty()) {
                String foundSchema = schemas.get(0);
                log.info("Strategy 6 - Found function '{}' in schema: {}", functionName, foundSchema);
                return foundSchema;
            }

            // Check if it's a procedure
            String findProcedureSql = "SELECT n.nspname FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE p.proname = ? AND p.prokind = 'p' " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            List<String> procSchemas = postgresqlJdbcTemplate.queryForList(findProcedureSql, String.class, functionName);

            if (!procSchemas.isEmpty()) {
                String foundSchema = procSchemas.get(0);
                log.warn("Strategy 6 - Found procedure '{}' in schema: {} (treating as function)", functionName, foundSchema);
                return foundSchema;
            }

            log.warn("Strategy 6 - Could not locate function '{}' in any accessible schema", functionName);

        } catch (Exception e) {
            log.warn("Error while searching for function in accessible schemas: {}", e.getMessage());
        }

        // Strategy 7: Default to 'public' schema
        log.info("Strategy 7 - Using default 'public' schema");
        return "public";
    }

    private int mapToSqlType(String pgType) {
        if (pgType == null) return Types.VARCHAR;

        String lowerType = pgType.toLowerCase();

        if (lowerType.contains("varchar")) return Types.VARCHAR;
        if (lowerType.contains("char")) return Types.CHAR;
        if (lowerType.contains("text")) return Types.LONGVARCHAR;
        if (lowerType.contains("smallint")) return Types.SMALLINT;
        if (lowerType.contains("integer") || lowerType.contains("int")) return Types.INTEGER;
        if (lowerType.contains("bigint")) return Types.BIGINT;
        if (lowerType.contains("decimal") || lowerType.contains("numeric")) return Types.NUMERIC;
        if (lowerType.contains("real")) return Types.REAL;
        if (lowerType.contains("double")) return Types.DOUBLE;
        if (lowerType.contains("float")) return Types.FLOAT;
        if (lowerType.contains("date")) return Types.DATE;
        if (lowerType.contains("timestamp")) return Types.TIMESTAMP;
        if (lowerType.contains("time")) return Types.TIME;
        if (lowerType.contains("bool")) return Types.BOOLEAN;
        if (lowerType.contains("bytea")) return Types.BINARY;
        if (lowerType.contains("json") || lowerType.contains("jsonb")) return Types.VARCHAR;
        if (lowerType.contains("xml")) return Types.SQLXML;
        if (lowerType.contains("uuid")) return Types.VARCHAR;
        if (lowerType.contains("[]")) return Types.ARRAY;

        return Types.VARCHAR;
    }

    private String getDbParamName(ApiParameterEntity param) {
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toLowerCase();
        }
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toLowerCase();
        }
        return param.getKey().toLowerCase();
    }

    /**
     * Parse XML body and extract parameter values
     */
    private Map<String, Object> parseXmlParameters(String xmlBody, List<ApiParameterDTO> configuredParamDTOs,
                                                   Map<String, String> apiToDbParamMap) {
        Map<String, Object> extractedParams = new HashMap<>();

        if (xmlBody == null || xmlBody.trim().isEmpty()) {
            return extractedParams;
        }

        log.info("Parsing XML body to extract parameter values");
        log.debug("XML Body: {}", xmlBody);

        try {
            for (ApiParameterDTO param : configuredParamDTOs) {
                String paramKey = param.getKey();
                if (paramKey == null || paramKey.isEmpty()) {
                    continue;
                }

                Pattern pattern = Pattern.compile("<" + paramKey + ">(.*?)</" + paramKey + ">",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(xmlBody);

                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    if (!value.isEmpty()) {
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toLowerCase());
                        extractedParams.put(dbParamName, value);
                        log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbParamName, value);
                    } else {
                        log.info("⚠️ XML tag <{}> found but empty", paramKey);
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toLowerCase());
                        extractedParams.put(dbParamName, "");
                        log.info("Added empty parameter: {} -> {}", paramKey, dbParamName);
                    }
                } else {
                    log.debug("XML tag <{}> not found in body", paramKey);
                }
            }

            log.info("Extracted {} parameters from XML: {}", extractedParams.size(), extractedParams.keySet());

        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
    }
}