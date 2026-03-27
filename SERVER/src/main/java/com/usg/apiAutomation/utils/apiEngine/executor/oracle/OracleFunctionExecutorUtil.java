package com.usg.apiAutomation.utils.apiEngine.executor.oracle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.apiAutomation.utils.apiEngine.OracleParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OracleFunctionExecutorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final OracleParameterValidatorUtil oracleParameterValidatorUtil;
    private final OracleObjectResolverUtil objectResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Timeout constants
    private static final int STATEMENT_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 30000;

    public OracleFunctionExecutorUtil(
            OracleParameterValidatorUtil oracleParameterValidatorUtil,
            OracleObjectResolverUtil objectResolver) {
        this.oracleParameterValidatorUtil = oracleParameterValidatorUtil;
        this.objectResolver = objectResolver;
    }

    /**
     * Get connection with timeout settings
     */
    private Connection getConnectionWithTimeout() throws SQLException {
        DataSource dataSource = oracleJdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new SQLException("No DataSource available");
        }

        Connection conn = dataSource.getConnection();

        // Set network timeout
        conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), CONNECTION_TIMEOUT_MS);

        // Set session timeout for Oracle
        try (Statement stmt = conn.createStatement()) {
            // Set query timeout (in seconds)
            stmt.execute("ALTER SESSION SET SQL_TRACE = FALSE");
            // Set resource limits
            stmt.execute("ALTER SESSION SET RESOURCE_LIMIT = TRUE");
        }

        return conn;
    }

    /**
     * Cleans SQL statements by removing trailing semicolons and other common issues
     */
    private String cleanSqlStatement(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        String cleaned = sql.trim();

        // Remove trailing semicolon(s) - JDBC doesn't need them and Oracle rejects them
        while (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }

        // Remove any leading/trailing whitespace
        cleaned = cleaned.trim();

        // Log the cleaning for debugging
        if (!cleaned.equals(sql.trim())) {
            log.info("Cleaned SQL statement - Original: [{}], Cleaned: [{}]", sql, cleaned);
        }

        return cleaned;
    }

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String functionName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ ORACLE FUNCTION EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("Function Name parameter: {}", functionName);
        log.info("Owner parameter: {}", owner);

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
                    apiToDbParamMap.put(param.getKey().toLowerCase(), dbParamName.toUpperCase());
                    log.info("Parameter mapping: API '{}' -> Database '{}'", param.getKey(), dbParamName.toUpperCase());
                }
            }
        }

        // ============ HANDLE XML BODY ============
        Map<String, Object> dbParams = new HashMap<>();
        String xmlBody = null;
        boolean isXmlBody = false;
        boolean hasXmlParameter = false;

        // Check if request body is a String and looks like XML
        if (request.getBody() != null) {
            if (request.getBody() instanceof String) {
                String bodyString = (String) request.getBody();
                if (bodyString.trim().startsWith("<")) {
                    isXmlBody = true;
                    xmlBody = bodyString;
                    log.info("XML BODY DETECTED! Length: {} characters", xmlBody.length());
                } else {
                    log.info("String body detected (non-XML)");
                }
            } else if (request.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();

                if (bodyMap.containsKey("_xml")) {
                    isXmlBody = true;
                    xmlBody = (String) bodyMap.get("_xml");
                    log.info("Found XML in _xml wrapper");
                } else {
                    log.info("JSON body detected with keys: {}", bodyMap.keySet());
                    for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                        String paramKey = entry.getKey().toLowerCase();
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());

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
        }

        // ============ PROCESS XML BODY ============
        if (isXmlBody && xmlBody != null) {
            log.info("Processing XML body for function execution");

            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbParamMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML and added to dbParams", extractedXmlParams.size());
            }

            // Look for explicit XML/CLOB parameter
            for (ApiParameterEntity param : api.getParameters()) {
                String paramKey = param.getKey().toLowerCase();
                String dbParamName = getDbParamName(param);

                boolean isXmlParameter = paramKey.contains("xml") ||
                        paramKey.contains("clob") ||
                        paramKey.contains("request") ||
                        paramKey.contains("payload") ||
                        paramKey.contains("body") ||
                        paramKey.equals("_xml");

                if (isXmlParameter && dbParamName != null && !dbParams.containsKey(dbParamName)) {
                    dbParams.put(dbParamName, xmlBody);
                    hasXmlParameter = true;
                    log.info("✅ Mapped full XML body to database parameter: {}", dbParamName);
                    break;
                }
            }

            // If no explicit XML parameter, look for CLOB/VARCHAR parameter
            if (!hasXmlParameter && extractedXmlParams.isEmpty()) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String dbParamName = getDbParamName(param);
                    if (dbParamName != null) {
                        String oracleType = param.getOracleType();
                        if (oracleType != null) {
                            String upperType = oracleType.toUpperCase();
                            if (upperType.contains("CLOB") || upperType.contains("VARCHAR")) {
                                dbParams.put(dbParamName, xmlBody);
                                hasXmlParameter = true;
                                log.info("✅ Mapped full XML body to CLOB/VARCHAR parameter: {}", dbParamName);
                                break;
                            }
                        }
                    }
                }
            }

            if (!hasXmlParameter && extractedXmlParams.isEmpty()) {
                log.warn("⚠️ No suitable database parameter found for XML body");
                dbParams.put("XML_BODY", xmlBody);
            }
        }

        // ============ ADD PATH, QUERY, AND HEADER PARAMETERS ============
        addParameters(request, api, apiToDbParamMap, dbParams);

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

        // ============ OWNER RESOLUTION STRATEGY ============
        String oracleOwner = resolveOwner(owner, sourceObject, api, functionName);

        if (oracleOwner == null || oracleOwner.trim().isEmpty()) {
            log.error("❌ COULD NOT DETERMINE OWNER/SCHEMA NAME");
            throw new ValidationException(
                    "Could not determine the database schema/owner for function: " + functionName
            );
        }

        oracleOwner = oracleOwner.toUpperCase();
        String oracleFunctionName = functionName != null ? functionName.trim().toUpperCase() : null;

        log.info("Final resolved owner: {}", oracleOwner);
        log.info("Final function name: {}", oracleFunctionName);

        // ============ SYNONYM RESOLUTION ============
        Map<String, Object> resolution = objectResolver.resolveProcedureTarget(oracleOwner, oracleFunctionName);
        log.info("🔍 Synonym resolution result: {}", resolution);

        String actualOwner;
        String actualFunctionName;

        if (resolution != null && resolution.containsKey("isSynonym") && (boolean) resolution.get("isSynonym")) {
            actualOwner = (String) resolution.get("targetOwner");
            actualFunctionName = (String) resolution.get("targetName");
            log.info("✅ Resolved synonym to: {}.{}", actualOwner, actualFunctionName);
        } else {
            actualOwner = oracleOwner;
            actualFunctionName = oracleFunctionName;
            log.info("ℹ️ Not a synonym, using original: {}.{}", actualOwner, actualFunctionName);
        }

        // ==================== VALIDATION STEP 1: Validate function exists ====================
        try {
            objectResolver.validateDatabaseObject(actualOwner, actualFunctionName, "FUNCTION");
            log.info("✅ Function {}.{} exists and is valid", actualOwner, actualFunctionName);
        } catch (EmptyResultDataAccessException e) {
            log.error("❌ Function {}.{} does not exist", actualOwner, actualFunctionName);
            throw new ValidationException(
                    String.format("The function '%s.%s' does not exist or you don't have access to it.",
                            actualOwner, actualFunctionName)
            );
        }

        // ==================== VALIDATION STEP 2: Validate all parameters ====================
        try {
            oracleParameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, actualOwner, actualFunctionName);
            log.info("✅ All parameter validations passed for function {}.{}", actualOwner, actualFunctionName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== EXECUTE FUNCTION WITH PROPER CONNECTION MANAGEMENT ====================
        try {
            return executeFunctionWithManualConnection(api, actualOwner, actualFunctionName, dbParams);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing function {}.{}: {}", actualOwner, actualFunctionName, e.getMessage(), e);

            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                // Provide user-friendly error messages for common Oracle errors
                if (errorMessage.contains("ORA-06550")) {
                    throw new ValidationException(
                            String.format("Invalid parameters provided for function '%s.%s'. Please check parameter names and data types. Details: %s",
                                    actualOwner, actualFunctionName, extractOracleError(errorMessage))
                    );
                }
                if (errorMessage.contains("ORA-00942")) {
                    throw new ValidationException(
                            String.format("Table or view referenced in function '%s.%s' could not be found. Details: %s",
                                    actualOwner, actualFunctionName, extractOracleError(errorMessage))
                    );
                }
                if (errorMessage.contains("ORA-01031")) {
                    throw new ValidationException(
                            String.format("Insufficient privileges to execute function '%s.%s'. Details: %s",
                                    actualOwner, actualFunctionName, extractOracleError(errorMessage))
                    );
                }
            }

            throw new RuntimeException("Failed to execute the requested operation: " + extractOracleError(errorMessage), e);
        }
    }

    /**
     * Execute function using manual connection management
     */
    private Object executeFunctionWithManualConnection(GeneratedApiEntity api,
                                                       String owner,
                                                       String functionName,
                                                       Map<String, Object> dbParams) throws SQLException {

        // Build the function call SQL
        StringBuilder callSql = new StringBuilder("{? = call ");
        if (owner != null && !owner.isEmpty()) {
            callSql.append(owner).append(".");
        }
        callSql.append(functionName).append("(");

        // Add parameter placeholders
        List<String> paramPlaceholders = new ArrayList<>();
        List<ApiParameterEntity> parameters = api.getParameters();

        if (parameters != null && !parameters.isEmpty()) {
            int inParamCount = 0;
            for (ApiParameterEntity param : parameters) {
                if (param == null || param.getKey() == null) continue;

                String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                String dbParamName = getDbParamName(param);

                if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                    paramPlaceholders.add("?");
                    inParamCount++;
                }
            }

            // If we have no IN parameters, add empty placeholder or none
            if (inParamCount == 0) {
                // No parameters
            }
        }

        callSql.append(String.join(", ", paramPlaceholders));
        callSql.append(")}");

        log.info("Executing Oracle function: {}", callSql.toString());

        try (Connection conn = getConnectionWithTimeout();
             CallableStatement cs = conn.prepareCall(callSql.toString())) {

            // Set statement timeout
            cs.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

            // Register return parameter (OUT parameter at index 1)
            String returnType = null;
            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                returnType = api.getResponseMappings().get(0).getOracleType();
                cs.registerOutParameter(1, mapToSqlType(returnType));
                log.debug("Registered return parameter of type: {}", returnType);
            } else {
                cs.registerOutParameter(1, Types.VARCHAR);
            }

            // Set IN parameters (starting at index 2 because index 1 is the return value)
            int paramIndex = 2;
            if (parameters != null && !parameters.isEmpty()) {
                for (ApiParameterEntity param : parameters) {
                    if (param == null || param.getKey() == null) continue;

                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                    String dbParamName = getDbParamName(param);

                    if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                        Object value = dbParams.get(dbParamName);
                        if (value == null) {
                            cs.setNull(paramIndex, mapToSqlType(param.getOracleType()));
                        } else {
                            cs.setObject(paramIndex, value);
                        }
                        log.debug("Set IN parameter {} at index {}: value={}", dbParamName, paramIndex, value);
                        paramIndex++;
                    }
                }
            }

            // Execute the function
            log.info("Executing function {}.{} with {} parameters", owner, functionName, paramIndex - 2);
            boolean hasResultSet = cs.execute();

            // Get the return value
            Object returnValue = cs.getObject(1);
            log.info("Function returned: {}", returnValue);

            // Process result set if any
            Map<String, Object> responseData = new HashMap<>();

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
                                row.put(columnName, rs.getObject(i));
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

            // Process OUT parameters (for INOUT parameters)
            if (parameters != null && !parameters.isEmpty()) {
                paramIndex = 2; // Reset to after return parameter
                for (ApiParameterEntity param : parameters) {
                    if (param == null || param.getKey() == null) continue;

                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                    String dbParamName = getDbParamName(param);

                    if (paramMode.equals("INOUT") && dbParams.containsKey(dbParamName)) {
                        try {
                            Object outValue = cs.getObject(paramIndex);
                            if (outValue != null) {
                                String apiField = param.getKey();
                                responseData.put(apiField, outValue);
                                log.debug("Retrieved INOUT parameter {} at index {}: value={}",
                                        dbParamName, paramIndex, outValue);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to get INOUT parameter: {}", e.getMessage());
                        }
                        paramIndex++;
                    } else if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                        paramIndex++; // Skip IN parameters
                    }
                }
            }

            // Map response mappings
            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        String dbColumn = mapping.getDbColumn();
                        if (dbColumn != null && !dbColumn.isEmpty()) {
                            String upperDbColumn = dbColumn.toUpperCase();

                            // Try to get from result set or return value
                            if (responseData.containsKey(upperDbColumn)) {
                                // Already have it from result set
                            } else if (returnValue != null && dbColumn.equalsIgnoreCase("RETURN")) {
                                responseData.put(mapping.getApiField(), returnValue);
                                log.debug("Mapped return value to {}: {}", mapping.getApiField(), returnValue);
                            } else if (returnValue instanceof Map && ((Map<?, ?>) returnValue).containsKey(upperDbColumn)) {
                                responseData.put(mapping.getApiField(), ((Map<?, ?>) returnValue).get(upperDbColumn));
                                log.debug("Mapped from return map {} to {}: {}",
                                        upperDbColumn, mapping.getApiField(), ((Map<?, ?>) returnValue).get(upperDbColumn));
                            }
                        }
                    }
                }
            } else if (returnValue != null) {
                // If no response mappings, use the return value
                responseData.put("result", returnValue);
            }

            // If no response data, add default success
            if (responseData.isEmpty()) {
                responseData.put("success", true);
                responseData.put("message", "Function executed successfully");
                if (returnValue != null) {
                    responseData.put("result", returnValue);
                }
            }

            log.info("============ FUNCTION EXECUTION COMPLETE ============");
            return responseData;
        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for {}.{}", owner, functionName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        }
    }

    /**
     * Add parameters from request (path, query, headers)
     */
    private void addParameters(ExecuteApiRequestDTO request, GeneratedApiEntity api,
                               Map<String, String> apiToDbParamMap, Map<String, Object> dbParams) {
        // Add path parameters
        if (request.getPathParams() != null) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toUpperCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        // Add query parameters
        if (request.getQueryParams() != null) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toUpperCase());
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
                    String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toUpperCase());
                    dbParams.put(dbParamName, entry.getValue());
                    log.debug("Added header as parameter: {} -> {}", entry.getKey(), dbParamName);
                }
            }
        }
    }

    /**
     * Helper method to extract Oracle error message
     */
    private String extractOracleError(String errorMessage) {
        if (errorMessage == null) return "Unknown error";

        Pattern pattern = Pattern.compile("ORA-\\d{5}:[^\\n]*");
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
     * Helper method to resolve owner from multiple sources
     */
    private String resolveOwner(String owner, ApiSourceObjectDTO sourceObject,
                                GeneratedApiEntity api, String functionName) {
        // Strategy 1: Use the owner parameter
        if (owner != null && !owner.trim().isEmpty()) {
            log.info("Strategy 1 - Using owner parameter: {}", owner);
            return owner.trim().toUpperCase();
        }

        // Strategy 2: Try sourceObject.getOwner()
        if (sourceObject != null && sourceObject.getOwner() != null && !sourceObject.getOwner().trim().isEmpty()) {
            log.info("Strategy 2 - Using sourceObject.getOwner(): {}", sourceObject.getOwner());
            return sourceObject.getOwner().trim().toUpperCase();
        }

        // Strategy 3: Try sourceObject.getSchemaName()
        if (sourceObject != null && sourceObject.getSchemaName() != null && !sourceObject.getSchemaName().trim().isEmpty()) {
            log.info("Strategy 3 - Using sourceObject.getSchemaName(): {}", sourceObject.getSchemaName());
            return sourceObject.getSchemaName().trim().toUpperCase();
        }

        // Strategy 4: Get from API's source_object_info
        if (api != null && api.getSourceObjectInfo() != null) {
            try {
                if (api.getSourceObjectInfo() instanceof Map) {
                    Map<String, Object> sourceInfo = (Map<String, Object>) api.getSourceObjectInfo();
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        String schemaName = sourceInfo.get("schemaName").toString();
                        log.info("Strategy 4 - Using schemaName from source_object_info Map: {}", schemaName);
                        return schemaName.trim().toUpperCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        String ownerName = sourceInfo.get("owner").toString();
                        log.info("Strategy 4 - Using owner from source_object_info Map: {}", ownerName);
                        return ownerName.trim().toUpperCase();
                    }
                } else {
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        String schemaName = sourceInfo.get("schemaName").toString();
                        log.info("Strategy 4 - Using schemaName from source_object_info JSON: {}", schemaName);
                        return schemaName.trim().toUpperCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        String ownerName = sourceInfo.get("owner").toString();
                        log.info("Strategy 4 - Using owner from source_object_info JSON: {}", ownerName);
                        return ownerName.trim().toUpperCase();
                    }
                }
            } catch (Exception e) {
                log.warn("Could not process sourceObjectInfo: {}", e.getMessage());
            }
        }

        // Strategy 5: Try to get current user's default schema
        try (Connection conn = getConnectionWithTimeout();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL")) {
            if (rs.next()) {
                String currentSchema = rs.getString(1);
                if (currentSchema != null && !currentSchema.isEmpty()) {
                    log.info("Strategy 5 - Using current schema from Oracle: {}", currentSchema);
                    return currentSchema;
                }
            }
        } catch (Exception e) {
            log.warn("Could not get current schema: {}", e.getMessage());
        }

        // Strategy 6: Try to locate the function in accessible schemas
        try (Connection conn = getConnectionWithTimeout();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND OBJECT_TYPE = 'FUNCTION' AND ROWNUM = 1")) {

            pstmt.setString(1, functionName);
            pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String foundOwner = rs.getString(1);
                    log.info("Strategy 6 - Found function '{}' in schema: {}", functionName, foundOwner);
                    return foundOwner;
                }
            }

            // If not found as function, check if it's a procedure
            try (PreparedStatement pstmt2 = conn.prepareStatement(
                    "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND OBJECT_TYPE = 'PROCEDURE' AND ROWNUM = 1")) {
                pstmt2.setString(1, functionName);
                pstmt2.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                try (ResultSet rs2 = pstmt2.executeQuery()) {
                    if (rs2.next()) {
                        String foundOwner = rs2.getString(1);
                        log.warn("Strategy 6 - Found procedure '{}' in schema: {} (treating as function)", functionName, foundOwner);
                        return foundOwner;
                    }
                }
            }

            log.warn("Strategy 6 - Could not locate function '{}' in any accessible schema", functionName);

        } catch (Exception e) {
            log.warn("Error while searching for function in accessible schemas: {}", e.getMessage());
        }

        log.error("❌ All owner resolution strategies failed for function: {}", functionName);
        return null;
    }

    private int mapToSqlType(String oracleType) {
        if (oracleType == null) return Types.VARCHAR;

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR")) return Types.VARCHAR;
        if (upperType.contains("CHAR")) return Types.CHAR;
        if (upperType.contains("CLOB")) return Types.CLOB;
        if (upperType.contains("NUMBER") || upperType.contains("NUMERIC")) return Types.NUMERIC;
        if (upperType.contains("INTEGER")) return Types.INTEGER;
        if (upperType.contains("DATE")) return Types.DATE;
        if (upperType.contains("TIMESTAMP")) return Types.TIMESTAMP;
        if (upperType.contains("BLOB")) return Types.BLOB;
        if (upperType.contains("BOOLEAN")) return Types.BOOLEAN;

        return Types.VARCHAR;
    }

    private String getDbParamName(ApiParameterEntity param) {
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toUpperCase();
        }
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toUpperCase();
        }
        return param.getKey().toUpperCase();
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
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbParamName, value);
                        log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbParamName, value);
                    } else {
                        log.info("⚠️ XML tag <{}> found but empty", paramKey);
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbParamName, "");
                        log.info("Added empty parameter: {} -> {}", paramKey, dbParamName);
                    }
                }
            }

            log.info("Extracted {} parameters from XML: {}", extractedParams.size(), extractedParams.keySet());

        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
    }
}