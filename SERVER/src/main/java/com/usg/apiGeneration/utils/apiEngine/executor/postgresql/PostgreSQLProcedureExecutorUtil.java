package com.usg.apiGeneration.utils.apiEngine.executor.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.*;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiGeneration.utils.apiEngine.PostgreSQLObjectResolverUtil;
import com.usg.apiGeneration.utils.apiEngine.PostgreSQLParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PostgreSQLProcedureExecutorUtil {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    private final PostgreSQLParameterValidatorUtil parameterValidatorUtil;
    private final PostgreSQLObjectResolverUtil objectResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Timeout constants
    private static final int STATEMENT_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 30000;

    public PostgreSQLProcedureExecutorUtil(
            PostgreSQLParameterValidatorUtil parameterValidatorUtil,
            PostgreSQLObjectResolverUtil objectResolver) {
        this.parameterValidatorUtil = parameterValidatorUtil;
        this.objectResolver = objectResolver;
    }


    /**
     * Execute a PostgreSQL procedure (stored procedure)
     */
    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String procedureName, String schema, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ POSTGRESQL PROCEDURE EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("Procedure Name: {}", procedureName);
        log.info("Schema: {}", schema);

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

        if (request.getBody() != null) {
            if (request.getBody() instanceof String) {
                String bodyString = (String) request.getBody();
                String trimmed = bodyString.trim();

                if (trimmed.startsWith("<")) {
                    isXmlBody = true;
                    xmlBody = bodyString;
                    log.info("XML BODY DETECTED! Length: {} characters", xmlBody.length());
                } else if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                    try {
                        Map<String, Object> jsonMap = objectMapper.readValue(bodyString, new TypeReference<Map<String, Object>>() {});
                        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                            String paramKey = entry.getKey().toLowerCase();
                            String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                            dbParams.put(dbParamName, entry.getValue());
                            log.debug("Added JSON param: {} -> {}", entry.getKey(), dbParamName);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse JSON body: {}", e.getMessage());
                    }
                }
            } else if (request.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
                log.info("JSON body detected with keys: {}", bodyMap.keySet());
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    String paramKey = entry.getKey().toLowerCase();
                    String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                    dbParams.put(dbParamName, entry.getValue());
                    log.debug("Added JSON param: {} -> {}", entry.getKey(), dbParamName);
                }
            }
        }

        // ============ PROCESS XML BODY ============
        if (isXmlBody && xmlBody != null) {
            log.info("Processing XML body for procedure execution");
            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbParamMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML", extractedXmlParams.size());
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        // ============ HANDLE AUTOGENERATE PARAMETERS ============
        if (api != null && api.getParameters() != null && !api.getParameters().isEmpty()) {
            log.info("Checking for AUTOGENERATE parameters to auto-populate");

            for (ApiParameterEntity param : api.getParameters()) {
                if ("AUTOGENERATE".equalsIgnoreCase(param.getOracleType())) {
                    String paramKey = param.getKey();
                    String dbParamName = getDbParamName(param);
                    String timestamp = getCurrentTimestamp();

                    // Check if this parameter already has a value
                    boolean hasExistingValue = false;

                    if (dbParams.containsKey(dbParamName)) {
                        hasExistingValue = true;
                        log.info("AUTOGENERATE parameter [{}] already has value: {}, keeping existing value",
                                paramKey, dbParams.get(dbParamName));
                    }

                    if (request.getPathParams() != null && request.getPathParams().containsKey(paramKey)) {
                        hasExistingValue = true;
                        log.info("AUTOGENERATE parameter [{}] found in path params", paramKey);
                    }

                    if (request.getQueryParams() != null && request.getQueryParams().containsKey(paramKey)) {
                        hasExistingValue = true;
                        log.info("AUTOGENERATE parameter [{}] found in query params", paramKey);
                    }

                    if (!hasExistingValue) {
                        dbParams.put(dbParamName, timestamp);
                        log.info("🔧 AUTOGENERATE parameter [{}] auto-populated with timestamp: {}",
                                paramKey, timestamp);
                    }
                }
            }
        }

        // Convert complex objects to JSON strings
        for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map || value instanceof List) {
                try {
                    String jsonValue = objectMapper.writeValueAsString(value);
                    dbParams.put(entry.getKey(), jsonValue);
                    log.debug("Converted complex object to JSON string for parameter: {}", entry.getKey());
                } catch (Exception e) {
                    log.warn("Failed to convert complex object to string: {}", e.getMessage());
                }
            }
        }

        log.info("Final DB params prepared: {}", dbParams.keySet());

        // ============ SCHEMA RESOLUTION ============
        String pgSchema = resolveSchema(schema, sourceObject, api, procedureName);
        if (pgSchema == null || pgSchema.trim().isEmpty()) {
            throw new ValidationException("Could not determine the database schema for procedure: " + procedureName);
        }

        pgSchema = pgSchema.toLowerCase();
        String pgProcedureName = procedureName != null ? procedureName.trim().toLowerCase() : null;

        log.info("Final resolved schema: {}", pgSchema);
        log.info("Final procedure name: {}", pgProcedureName);

        // ============ VALIDATION ============
        try {
            objectResolver.validateDatabaseObject(pgSchema, pgProcedureName, "PROCEDURE");
            log.info("✅ Procedure {}.{} exists", pgSchema, pgProcedureName);
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                    pgSchema, pgProcedureName));
        }

        try {
            parameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, pgSchema, pgProcedureName);
            log.info("✅ All parameter validations passed");
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ============ EXECUTE PROCEDURE ============
        try {
            DataSource dataSource = postgresqlJdbcTemplate.getDataSource();
            if (dataSource == null) {
                throw new SQLException("No DataSource available");
            }

            try (Connection conn = dataSource.getConnection()) {
                conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), CONNECTION_TIMEOUT_MS);

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET statement_timeout = '" + STATEMENT_TIMEOUT_SECONDS + "s'");
                }

                String callSql = buildCallStatement(pgSchema, pgProcedureName, api.getParameters(), dbParams);
                log.info("Executing: {}", callSql);

                Map<String, Object> responseData = new HashMap<>();

                try (CallableStatement cs = conn.prepareCall(callSql)) {
                    cs.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                    // Set IN parameters
                    int index = 1;
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        for (ApiParameterEntity param : api.getParameters()) {
                            if (param == null || param.getKey() == null) continue;
                            String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                            if (!paramMode.contains("IN") && !paramMode.equals("INOUT")) continue;

                            String dbParamName = getDbParamName(param);
                            if (dbParams.containsKey(dbParamName)) {
                                Object value = dbParams.get(dbParamName);
                                if (value == null) {
                                    cs.setNull(index++, Types.NULL);
                                } else {
                                    cs.setObject(index++, value);
                                }
                                log.debug("Set IN parameter {}: {}", dbParamName, value);
                            } else {
                                cs.setNull(index++, Types.NULL);
                            }
                        }
                    }

                    // Register OUT parameters
                    if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                        for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                            if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                                String dbColumn = mapping.getDbColumn();
                                if (dbColumn != null && !dbColumn.isEmpty()) {
                                    int sqlType = mapToSqlType(mapping.getOracleType());
                                    cs.registerOutParameter(index++, sqlType);
                                    log.debug("Registered OUT parameter: {}", dbColumn);
                                }
                            }
                        }
                    }

                    boolean hasResultSet = cs.execute();

                    // Get OUT parameters
                    int outIndex = 1;
                    if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                        for (ApiParameterEntity param : api.getParameters()) {
                            if (param == null || param.getKey() == null) continue;
                            String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                            if (!paramMode.contains("IN") && !paramMode.equals("INOUT")) {
                                outIndex++;
                            } else {
                                outIndex++;
                            }
                        }
                    }

                    if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                        for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                            if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                                try {
                                    Object value = cs.getObject(outIndex++);
                                    if (value != null) {
                                        responseData.put(mapping.getApiField(), value);
                                        log.debug("Got OUT parameter {}: {}", mapping.getApiField(), value);
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to get OUT parameter: {}", e.getMessage());
                                }
                            }
                        }
                    }

                    // Process result set
                    if (hasResultSet) {
                        try (ResultSet rs = cs.getResultSet()) {
                            if (rs != null) {
                                List<Map<String, Object>> resultSetData = new ArrayList<>();
                                ResultSetMetaData metaData = rs.getMetaData();
                                int columnCount = metaData.getColumnCount();

                                while (rs.next()) {
                                    Map<String, Object> row = new HashMap<>();
                                    for (int i = 1; i <= columnCount; i++) {
                                        row.put(metaData.getColumnName(i).toLowerCase(), rs.getObject(i));
                                    }
                                    resultSetData.add(row);
                                }
                                if (!resultSetData.isEmpty()) {
                                    responseData.put("data", resultSetData);
                                    log.info("Retrieved {} rows", resultSetData.size());
                                }
                            }
                        }
                    }

                    if (responseData.isEmpty()) {
                        responseData.put("success", true);
                        responseData.put("message", "Procedure executed successfully");
                    }
                }

                log.info("============ PROCEDURE EXECUTION COMPLETE ============");
                return responseData;
            }
        } catch (SQLTimeoutException e) {
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing procedure: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute procedure: " + e.getMessage(), e);
        }
    }

    // Helper method to get current timestamp
    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d%02d%02d%02d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
    }

    // Helper method to get DB param name
    private String getDbParamName(ApiParameterEntity param) {
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toLowerCase();
        }
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toLowerCase();
        }
        return param.getKey().toLowerCase();
    }

    // Helper method to build CALL statement
    private String buildCallStatement(String schema, String procedureName,
                                      List<ApiParameterEntity> parameters,
                                      Map<String, Object> dbParams) {
        StringBuilder callBuilder = new StringBuilder("CALL ");
        if (schema != null && !schema.isEmpty()) {
            callBuilder.append(schema).append(".");
        }
        callBuilder.append(procedureName).append("(");

        List<String> paramPlaceholders = new ArrayList<>();
        if (parameters != null && !parameters.isEmpty()) {
            for (ApiParameterEntity param : parameters) {
                if (param == null || param.getKey() == null) continue;
                String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                String dbParamName = getDbParamName(param);
                if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                    paramPlaceholders.add("?");
                }
            }
        }

        callBuilder.append(String.join(", ", paramPlaceholders));
        callBuilder.append(")");
        return callBuilder.toString();
    }

    // Helper method to map PostgreSQL type to SQL type
    private int mapToSqlType(String pgType) {
        if (pgType == null) return Types.VARCHAR;
        String lowerType = pgType.toLowerCase();
        if (lowerType.contains("varchar")) return Types.VARCHAR;
        if (lowerType.contains("integer") || lowerType.contains("int")) return Types.INTEGER;
        if (lowerType.contains("bigint")) return Types.BIGINT;
        if (lowerType.contains("numeric") || lowerType.contains("decimal")) return Types.NUMERIC;
        if (lowerType.contains("timestamp")) return Types.TIMESTAMP;
        if (lowerType.contains("date")) return Types.DATE;
        if (lowerType.contains("boolean") || lowerType.contains("bool")) return Types.BOOLEAN;
        if (lowerType.contains("json") || lowerType.contains("jsonb")) return Types.VARCHAR;
        return Types.VARCHAR;
    }

    // Helper method to resolve schema
    private String resolveSchema(String schema, ApiSourceObjectDTO sourceObject,
                                 GeneratedApiEntity api, String procedureName) {
        if (schema != null && !schema.trim().isEmpty()) {
            return schema.trim().toLowerCase();
        }
        if (sourceObject != null && sourceObject.getOwner() != null && !sourceObject.getOwner().trim().isEmpty()) {
            return sourceObject.getOwner().trim().toLowerCase();
        }
        if (sourceObject != null && sourceObject.getSchemaName() != null && !sourceObject.getSchemaName().trim().isEmpty()) {
            return sourceObject.getSchemaName().trim().toLowerCase();
        }
        return "public";
    }

    // Helper method to parse XML parameters
    private Map<String, Object> parseXmlParameters(String xmlBody, List<ApiParameterDTO> configuredParamDTOs,
                                                   Map<String, String> apiToDbParamMap) {
        Map<String, Object> extractedParams = new HashMap<>();
        if (xmlBody == null || xmlBody.trim().isEmpty()) {
            return extractedParams;
        }

        try {
            for (ApiParameterDTO param : configuredParamDTOs) {
                String paramKey = param.getKey();
                if (paramKey == null || paramKey.isEmpty()) continue;

                Pattern pattern = Pattern.compile("<" + paramKey + ">(.*?)</" + paramKey + ">",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(xmlBody);

                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    if (!value.isEmpty()) {
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toLowerCase());
                        extractedParams.put(dbParamName, value);
                        log.info("✅ Extracted XML parameter: {} -> {}", paramKey, dbParamName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
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
                String pgType = param.getOracleType();

                if (value == null) {
                    cs.setNull(index++, Types.NULL);
                    log.debug("Set NULL parameter {} (index {})", dbParamName, index-1);
                } else {
                    cs.setObject(index++, value);
                    log.debug("Set parameter {} (index {}): type={}, value={}",
                            dbParamName, index-1, pgType, value);
                }
            }
        }
    }

    /**
     * Register OUT parameters and return the next available index
     */
    private int registerOutParameters(CallableStatement cs, List<ApiResponseMappingEntity> responseMappings,
                                      List<ApiParameterEntity> parameters, Map<String, Object> dbParams) throws SQLException {
        if (responseMappings == null || responseMappings.isEmpty()) {
            return 1;
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

        return outParamIndex;
    }

    /**
     * Process results from procedure execution
     */
    private Map<String, Object> processResults(CallableStatement callableStatement, GeneratedApiEntity api,
                                               Map<String, Object> dbParams, boolean hasResultSet)
            throws SQLException {
        Map<String, Object> responseData = new HashMap<>();

        // Process OUT parameters
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            // Calculate OUT parameter indices
            int inParamCount = 0;
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    if (param == null || param.getKey() == null) continue;
                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                    String dbParamName = getDbParamName(param);
                    if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                        inParamCount++;
                    }
                }
            }

            int outParamIndex = inParamCount + 1;
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    try {
                        Object value = callableStatement.getObject(outParamIndex);
                        if (value != null) {
                            responseData.put(mapping.getApiField(), value);
                            log.debug("Mapped OUT parameter to {} with value: {}", mapping.getApiField(), value);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to get OUT parameter: {}", e.getMessage());
                    }
                    outParamIndex++;
                }
            }
        }

        // Process result set
        if (hasResultSet) {
            try (ResultSet rs = callableStatement.getResultSet()) {
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

        // Default success response
        if (responseData.isEmpty()) {
            responseData.put("success", true);
            responseData.put("message", "Procedure executed successfully");
        }

        return responseData;
    }

    /**
     * Add parameters from request (path, query, headers)
     */
    private void addParameters(ExecuteApiRequestDTO request, GeneratedApiEntity api,
                               Map<String, String> apiToDbParamMap, Map<String, Object> dbParams) {
        // Add path parameters
        if (request.getPathParams() != null) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        // Add query parameters
        if (request.getQueryParams() != null) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        // Add headers that are defined as parameters
        if (request.getHeaders() != null && api.getParameters() != null) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                boolean headerIsParameter = api.getParameters().stream()
                        .anyMatch(p -> p.getKey().equalsIgnoreCase(entry.getKey()));
                if (headerIsParameter) {
                    String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toLowerCase());
                    dbParams.put(dbParamName, entry.getValue());
                    log.debug("Added header as parameter: {} -> {}", entry.getKey(), dbParamName);
                }
            }
        }
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
     * Extract PostgreSQL error message
     */
    private String extractPostgreSQLError(String errorMessage) {
        if (errorMessage == null) return "Unknown error";

        Pattern pattern = Pattern.compile("ERROR:[^\\n]*");
        Matcher matcher = pattern.matcher(errorMessage);
        if (matcher.find()) {
            return matcher.group();
        }

        return errorMessage.length() > 200 ? errorMessage.substring(0, 200) + "..." : errorMessage;
    }


}