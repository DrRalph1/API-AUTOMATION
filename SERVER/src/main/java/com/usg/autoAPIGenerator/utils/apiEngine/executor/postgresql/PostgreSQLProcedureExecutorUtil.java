package com.usg.autoAPIGenerator.utils.apiEngine.executor.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.autoAPIGenerator.utils.apiEngine.PostgreSQLObjectResolverUtil;
import com.usg.autoAPIGenerator.utils.apiEngine.PostgreSQLParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
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

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String procedureName, String schema, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ POSTGRESQL PROCEDURE EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("Procedure Name parameter: {}", procedureName);
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
                log.info("✅ Extracted {} parameters from XML and added to dbParams", extractedXmlParams.size());
            }

            boolean hasBodyParameter = false;
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

            if (!hasBodyParameter && extractedXmlParams.isEmpty()) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String dbParamName = getDbParamName(param);
                    if (dbParamName != null) {
                        String pgType = param.getOracleType();
                        if (pgType != null && pgType.toLowerCase().contains("text")) {
                            dbParams.put(dbParamName, xmlBody);
                            log.info("✅ Mapped full XML body to TEXT parameter: {}", dbParamName);
                            break;
                        }
                    }
                }
            }
        }

        // ============ ADD PATH, QUERY, HEADER PARAMETERS ============
        addParameters(request, api, apiToDbParamMap, dbParams);

        // ============ HANDLE FILE UPLOADS (ONLY IF PRESENT) ============
        if ((request.getFileMap() != null && !request.getFileMap().isEmpty()) ||
                (request.getFile() != null && !request.getFile().isEmpty())) {

            log.info("Processing file uploads for procedure execution");

            if (request.getFileMap() != null && !request.getFileMap().isEmpty()) {
                for (Map.Entry<String, MultipartFile> entry : request.getFileMap().entrySet()) {
                    String paramName = entry.getKey();
                    MultipartFile file = entry.getValue();
                    try {
                        byte[] fileBytes = file.getBytes();
                        String dbParamName = apiToDbParamMap.getOrDefault(paramName.toLowerCase(), paramName.toLowerCase());
                        dbParams.put(dbParamName, fileBytes);
                        log.info("✅ Added file to dbParams: {} -> {} ({} bytes)", paramName, dbParamName, fileBytes.length);
                    } catch (IOException e) {
                        log.error("Failed to read file: {}", e.getMessage());
                        throw new RuntimeException("Failed to read uploaded file", e);
                    }
                }
            }

            if (request.getFile() != null && !request.getFile().isEmpty()) {
                MultipartFile file = request.getFile();
                try {
                    byte[] fileBytes = file.getBytes();
                    String dbParamName = apiToDbParamMap.getOrDefault("file", "file");
                    dbParams.put(dbParamName, fileBytes);
                    log.info("✅ Added single file to dbParams: {} -> {} ({} bytes)", file.getOriginalFilename(), dbParamName, fileBytes.length);
                } catch (IOException e) {
                    log.error("Failed to read file: {}", e.getMessage());
                    throw new RuntimeException("Failed to read uploaded file", e);
                }
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

        // Convert complex objects to JSON strings for JSON parameters
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

        // Handle collection/array parameters (skip byte arrays)
        for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof byte[]) {
                continue; // Skip byte arrays - they're file data
            }
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
            log.info("✅ All parameter validations passed for procedure {}.{}", pgSchema, pgProcedureName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ============ EXECUTE PROCEDURE WITH PROPER CONNECTION MANAGEMENT ============
        try {
            DataSource dataSource = postgresqlJdbcTemplate.getDataSource();
            if (dataSource == null) {
                throw new SQLException("No DataSource available");
            }

            try (Connection conn = dataSource.getConnection()) {
                conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), CONNECTION_TIMEOUT_MS);

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET statement_timeout = '" + STATEMENT_TIMEOUT_SECONDS + "s'");
                    stmt.execute("SET lock_timeout = '" + STATEMENT_TIMEOUT_SECONDS + "s'");
                }

                String callSql = buildCallStatement(pgSchema, pgProcedureName, api.getParameters(), dbParams);
                log.info("Executing PostgreSQL procedure with CALL syntax: {}", callSql);
                log.info("Parameters: {}", dbParams);

                List<String> capturedNotices = new ArrayList<>();
                Map<String, Object> finalResult = new HashMap<>();

                try (CallableStatement cs = conn.prepareCall(callSql)) {
                    cs.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);
                    setParameters(cs, api.getParameters(), dbParams);
                    int outParamIndex = registerOutParameters(cs, api.getResponseMappings(), api.getParameters(), dbParams);
                    log.info("Executing procedure {}.{}", pgSchema, pgProcedureName);
                    boolean hasResultSet = cs.execute();
                    log.info("Procedure executed successfully");

                    SQLWarning warning = cs.getWarnings();
                    while (warning != null) {
                        if (warning.getMessage() != null) {
                            capturedNotices.add(warning.getMessage());
                            log.debug("Captured warning/notice: {}", warning.getMessage());
                        }
                        warning = warning.getNextWarning();
                    }

                    if (!capturedNotices.isEmpty()) {
                        log.info("Captured {} NOTICE messages from procedure", capturedNotices.size());

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

                    Map<String, Object> responseData = processResults(cs, api, dbParams, hasResultSet);

                    if (!finalResult.isEmpty()) {
                        responseData.putAll(finalResult);
                    }

                    log.info("============ PROCEDURE EXECUTION COMPLETE ============");
                    return responseData;
                }
            }
        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for {}.{}", pgSchema, pgProcedureName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing procedure {}.{}: {}", pgSchema, pgProcedureName, e.getMessage(), e);
            String errorMessage = e.getMessage();

            // FIRST: Check for custom format from PostgreSQL RAISE with detail
            if (errorMessage != null) {
                // Look for pattern: "Detail: 409" or "Detail: 400" etc.
                Pattern detailPattern = Pattern.compile("Detail:\\s*(\\d{3})", Pattern.MULTILINE);
                Matcher detailMatcher = detailPattern.matcher(errorMessage);
                if (detailMatcher.find()) {
                    String statusCode = detailMatcher.group(1);
                    // Extract the actual error message (remove PostgreSQL formatting)
                    String cleanMessage = errorMessage;

                    // Remove the PostgreSQL prefix and detail line
                    if (cleanMessage.contains("ERROR:")) {
                        cleanMessage = cleanMessage.substring(cleanMessage.indexOf("ERROR:") + 6).trim();
                        // Remove the Detail line
                        int detailIndex = cleanMessage.indexOf("\n  Detail:");
                        if (detailIndex > 0) {
                            cleanMessage = cleanMessage.substring(0, detailIndex).trim();
                        }
                        // Remove the Where line if present
                        int whereIndex = cleanMessage.indexOf("\n  Where:");
                        if (whereIndex > 0) {
                            cleanMessage = cleanMessage.substring(0, whereIndex).trim();
                        }
                    }

                    // Create formatted exception with status code
                    String formattedMessage = statusCode + "|" + cleanMessage;
                    log.info("✅ Extracted custom error: status={}, message={}", statusCode, cleanMessage);
                    throw new RuntimeException(formattedMessage, e);
                }

                // Check for SQLSTATE unique violation (23505)
                if (errorMessage.contains("SQLSTATE=23505") || errorMessage.contains("duplicate key")) {
                    String cleanMessage = "Duplicate record already exists";
                    if (errorMessage.contains("Key")) {
                        Pattern keyPattern = Pattern.compile("Key \\((.*?)\\)=(.*?)\\)");
                        Matcher keyMatcher = keyPattern.matcher(errorMessage);
                        if (keyMatcher.find()) {
                            cleanMessage = "Record with " + keyMatcher.group(1) + " already exists";
                        }
                    }
                    log.info("✅ PostgreSQL unique violation (23505) mapped to 409: {}", cleanMessage);
                    throw new RuntimeException("409|" + cleanMessage, e);
                }

                // Check for Result: JSON pattern (existing logic)
                if (errorMessage.contains("Result: ")) {
                    String jsonPart = extractJsonFromNotice(errorMessage);
                    if (jsonPart != null) {
                        try {
                            return objectMapper.readValue(jsonPart, new TypeReference<Map<String, Object>>() {});
                        } catch (Exception parseEx) {
                            log.warn("Failed to parse JSON from exception: {}", jsonPart);
                        }
                    }
                }
            }

            // Fallback: wrap in generic error
            throw new RuntimeException("500|" + extractPostgreSQLError(errorMessage), e);
        }
    }

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

    private int registerOutParameters(CallableStatement cs, List<ApiResponseMappingEntity> responseMappings,
                                      List<ApiParameterEntity> parameters, Map<String, Object> dbParams) throws SQLException {
        if (responseMappings == null || responseMappings.isEmpty()) {
            return 1;
        }

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
                    String pgType = param.getOracleType();

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

    private Map<String, Object> processResults(CallableStatement callableStatement, GeneratedApiEntity api,
                                               Map<String, Object> dbParams, boolean hasResultSet)
            throws SQLException {
        Map<String, Object> responseData = new HashMap<>();

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
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

        if (responseData.isEmpty()) {
            responseData.put("success", true);
            responseData.put("message", "Procedure executed successfully");
        }

        return responseData;
    }

    private void addParameters(ExecuteApiRequestDTO request, GeneratedApiEntity api,
                               Map<String, String> apiToDbParamMap, Map<String, Object> dbParams) {
        if (request.getPathParams() != null) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        if (request.getQueryParams() != null) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

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

    private String extractPostgreSQLError(String errorMessage) {
        if (errorMessage == null) return "Unknown error";

        // First, try to extract just the ERROR: line without the detail
        Pattern errorPattern = Pattern.compile("ERROR:\\s*([^\\n]+)");
        Matcher errorMatcher = errorPattern.matcher(errorMessage);
        if (errorMatcher.find()) {
            String errorLine = errorMatcher.group(1).trim();
            // Remove any trailing Detail or Where references
            int detailIndex = errorLine.indexOf("Detail:");
            if (detailIndex > 0) {
                errorLine = errorLine.substring(0, detailIndex).trim();
            }
            return errorLine;
        }

        // If no ERROR: pattern, return first line or truncated message
        String firstLine = errorMessage.split("\n")[0];
        return firstLine.length() > 200 ? firstLine.substring(0, 200) + "..." : firstLine;
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

    private String resolveSchema(String schema, ApiSourceObjectDTO sourceObject,
                                 GeneratedApiEntity api, String procedureName) {
        if (schema != null && !schema.trim().isEmpty()) {
            log.info("Strategy 1 - Using schema parameter: {}", schema);
            return schema.trim().toLowerCase();
        }

        if (sourceObject != null && sourceObject.getOwner() != null && !sourceObject.getOwner().trim().isEmpty()) {
            log.info("Strategy 2 - Using sourceObject.getOwner(): {}", sourceObject.getOwner());
            return sourceObject.getOwner().trim().toLowerCase();
        }

        if (sourceObject != null && sourceObject.getSchemaName() != null && !sourceObject.getSchemaName().trim().isEmpty()) {
            log.info("Strategy 3 - Using sourceObject.getSchemaName(): {}", sourceObject.getSchemaName());
            return sourceObject.getSchemaName().trim().toLowerCase();
        }

        if (api != null && api.getSourceObjectInfo() != null) {
            try {
                if (api.getSourceObjectInfo() instanceof Map) {
                    Map<String, Object> sourceInfo = (Map<String, Object>) api.getSourceObjectInfo();
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        return sourceInfo.get("schemaName").toString().trim().toLowerCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        return sourceInfo.get("owner").toString().trim().toLowerCase();
                    }
                } else {
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        return sourceInfo.get("schemaName").toString().trim().toLowerCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        return sourceInfo.get("owner").toString().trim().toLowerCase();
                    }
                }
            } catch (Exception e) {
                log.warn("Could not process sourceObjectInfo: {}", e.getMessage());
            }
        }

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

        try {
            String findProcedureSql = "SELECT n.nspname FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE p.proname = ? AND p.prokind = 'p' " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            List<String> schemas = postgresqlJdbcTemplate.queryForList(findProcedureSql, String.class, procedureName);
            if (!schemas.isEmpty()) {
                log.info("Strategy 6 - Found procedure in schema: {}", schemas.get(0));
                return schemas.get(0);
            }
        } catch (Exception e) {
            log.warn("Error searching for procedure: {}", e.getMessage());
        }

        log.info("Strategy 7 - Using default 'public' schema");
        return "public";
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
                        log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbParamName, value);
                    } else {
                        log.info("⚠️ XML tag <{}> found but empty", paramKey);
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toLowerCase());
                        extractedParams.put(dbParamName, "");
                        log.info("Added empty parameter: {} -> {}", paramKey, dbParamName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
    }
}