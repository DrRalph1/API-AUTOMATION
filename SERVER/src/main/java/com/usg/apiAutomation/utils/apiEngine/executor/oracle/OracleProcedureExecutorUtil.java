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
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OracleProcedureExecutorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final OracleParameterValidatorUtil oracleParameterValidatorUtil;
    private final OracleObjectResolverUtil objectResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Timeout constants
    private static final int STATEMENT_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 30000;

    public OracleProcedureExecutorUtil(
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
            stmt.execute("ALTER SESSION SET SQL_TRACE = FALSE");
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

        while (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }

        cleaned = cleaned.trim();

        if (!cleaned.equals(sql.trim())) {
            log.info("Cleaned SQL statement - Original: [{}], Cleaned: [{}]", sql, cleaned);
        }

        return cleaned;
    }

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String procedureName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ ORACLE PROCEDURE EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("Procedure Name parameter: {}", procedureName);
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

        // ============ HANDLE XML/JSON BODY ============
        Map<String, Object> dbParams = new HashMap<>();
        String xmlBody = null;
        boolean isXmlBody = false;
        boolean hasXmlParameter = false;

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
            log.info("Processing XML body for procedure execution");

            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbParamMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML and added to dbParams", extractedXmlParams.size());
            }

            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String paramKey = param.getKey().toLowerCase();
                    String dbParamName = getDbParamName(param);

                    boolean isXmlParameter = paramKey.contains("xml") ||
                            paramKey.contains("clob") ||
                            paramKey.contains("request") ||
                            paramKey.contains("payload") ||
                            paramKey.contains("body") ||
                            paramKey.equals("_xml") ||
                            paramKey.equals("xmldata");

                    if (isXmlParameter && dbParamName != null && !dbParams.containsKey(dbParamName)) {
                        dbParams.put(dbParamName, xmlBody);
                        hasXmlParameter = true;
                        log.info("✅ Mapped full XML body to database parameter: {}", dbParamName);
                        break;
                    }
                }
            }

            if (!hasXmlParameter && extractedXmlParams.isEmpty() && api.getParameters() != null) {
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
        String oracleOwner = resolveOwner(owner, sourceObject, api, procedureName);

        if (oracleOwner == null || oracleOwner.trim().isEmpty()) {
            log.error("❌ COULD NOT DETERMINE OWNER/SCHEMA NAME");
            throw new ValidationException(
                    "Could not determine the database schema/owner for procedure: " + procedureName
            );
        }

        oracleOwner = oracleOwner.toUpperCase();
        String oracleProcedureName = procedureName != null ? procedureName.trim().toUpperCase() : null;

        log.info("Final resolved owner: {}", oracleOwner);
        log.info("Final procedure name: {}", oracleProcedureName);

        // ============ SYNONYM RESOLUTION ============
        Map<String, Object> resolution = objectResolver.resolveProcedureTarget(oracleOwner, oracleProcedureName);
        log.info("🔍 Synonym resolution result: {}", resolution);

        String actualOwner;
        String actualProcedureName;

        if (resolution != null && resolution.containsKey("isSynonym") && (boolean) resolution.get("isSynonym")) {
            actualOwner = (String) resolution.get("targetOwner");
            actualProcedureName = (String) resolution.get("targetName");
            log.info("✅ Resolved synonym to: {}.{}", actualOwner, actualProcedureName);
        } else {
            actualOwner = oracleOwner;
            actualProcedureName = oracleProcedureName;
            log.info("ℹ️ Not a synonym, using original: {}.{}", actualOwner, actualProcedureName);
        }

        // ==================== VALIDATION STEP 1: Validate procedure exists ====================
        try {
            objectResolver.validateDatabaseObject(actualOwner, actualProcedureName, "PROCEDURE");
            log.info("✅ Procedure {}.{} exists and is valid", actualOwner, actualProcedureName);
        } catch (EmptyResultDataAccessException e) {
            log.error("❌ Procedure {}.{} does not exist", actualOwner, actualProcedureName);
            throw new ValidationException(
                    String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                            actualOwner, actualProcedureName)
            );
        }

        // ==================== VALIDATION STEP 2: Validate all parameters ====================
        try {
            oracleParameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, actualOwner, actualProcedureName);
            log.info("✅ All parameter validations passed for procedure {}.{}", actualOwner, actualProcedureName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== EXECUTE PROCEDURE WITH PROPER CONNECTION MANAGEMENT ====================
        try {
            return executeProcedureWithManualConnection(api, actualOwner, actualProcedureName, dbParams);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing procedure {}.{}: {}", actualOwner, actualProcedureName, e.getMessage(), e);

            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("ORA-06550")) {
                    throw new ValidationException(
                            String.format("Invalid parameters provided for procedure '%s.%s'. Please check parameter names and data types. Details: %s",
                                    actualOwner, actualProcedureName, extractOracleError(errorMessage))
                    );
                }
                if (errorMessage.contains("ORA-00942")) {
                    throw new ValidationException(
                            String.format("Table or view referenced in procedure '%s.%s' could not be found. Details: %s",
                                    actualOwner, actualProcedureName, extractOracleError(errorMessage))
                    );
                }
                if (errorMessage.contains("ORA-01031")) {
                    throw new ValidationException(
                            String.format("Insufficient privileges to execute procedure '%s.%s'. Details: %s",
                                    actualOwner, actualProcedureName, extractOracleError(errorMessage))
                    );
                }
            }

            throw new RuntimeException("Failed to execute the requested operation: " + extractOracleError(errorMessage), e);
        }
    }

    /**
     * Execute procedure using manual connection management
     */
    private Object executeProcedureWithManualConnection(GeneratedApiEntity api,
                                                        String owner,
                                                        String procedureName,
                                                        Map<String, Object> dbParams) throws SQLException {

        // Build the procedure call SQL
        StringBuilder callSql = new StringBuilder("{call ");
        if (owner != null && !owner.isEmpty()) {
            callSql.append(owner).append(".");
        }
        callSql.append(procedureName).append("(");

        // Add parameter placeholders
        List<String> paramPlaceholders = new ArrayList<>();
        List<ApiParameterEntity> parameters = api.getParameters();
        List<ApiResponseMappingEntity> responseMappings = api.getResponseMappings();

        // Count total parameters (IN + OUT)
        int totalParamCount = 0;

        if (parameters != null && !parameters.isEmpty()) {
            for (ApiParameterEntity param : parameters) {
                if (param == null || param.getKey() == null) continue;

                String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                String dbParamName = getDbParamName(param);

                if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                    paramPlaceholders.add("?");
                    totalParamCount++;
                }
            }
        }

        // Add OUT parameters from response mappings
        if (responseMappings != null && !responseMappings.isEmpty()) {
            for (ApiResponseMappingEntity mapping : responseMappings) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    paramPlaceholders.add("?");
                    totalParamCount++;
                }
            }
        }

        callSql.append(String.join(", ", paramPlaceholders));
        callSql.append(")}");

        log.info("Executing Oracle procedure: {}", callSql.toString());

        try (Connection conn = getConnectionWithTimeout();
             CallableStatement cs = conn.prepareCall(callSql.toString())) {

            // Set statement timeout
            cs.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

            // Track parameter index
            int paramIndex = 1;

            // Set IN and INOUT parameters
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
                        log.debug("Set parameter {} at index {}: value={}", dbParamName, paramIndex, value);
                        paramIndex++;
                    }
                }
            }

            // Register OUT parameters from response mappings
            if (responseMappings != null && !responseMappings.isEmpty()) {
                for (ApiResponseMappingEntity mapping : responseMappings) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        String outParamName = mapping.getDbColumn() != null && !mapping.getDbColumn().isEmpty() ?
                                mapping.getDbColumn().toUpperCase() : "out_param_" + mapping.getPosition();

                        int sqlType = mapToSqlType(mapping.getOracleType());
                        cs.registerOutParameter(paramIndex, sqlType);
                        log.debug("Registered OUT parameter {} at index {}: type={}",
                                outParamName, paramIndex, mapping.getOracleType());
                        paramIndex++;
                    }
                }
            }

            // Execute the procedure
            log.info("Executing procedure {}.{} with {} parameters", owner, procedureName, paramIndex - 1);
            cs.execute();
            log.info("Procedure executed successfully");

            // Process results
            Map<String, Object> responseData = new HashMap<>();

            // Retrieve OUT parameters
            paramIndex = 1;

            // Skip IN parameters to get to OUT parameters
            if (parameters != null && !parameters.isEmpty()) {
                for (ApiParameterEntity param : parameters) {
                    if (param == null || param.getKey() == null) continue;

                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                    String dbParamName = getDbParamName(param);

                    if ((paramMode.contains("IN") || paramMode.equals("INOUT")) && dbParams.containsKey(dbParamName)) {
                        paramIndex++;
                    }
                }
            }

            // Retrieve OUT parameters from response mappings
            if (responseMappings != null && !responseMappings.isEmpty()) {
                for (ApiResponseMappingEntity mapping : responseMappings) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        try {
                            Object value = cs.getObject(paramIndex);
                            if (value != null) {
                                responseData.put(mapping.getApiField(), value);
                                log.debug("Retrieved OUT parameter {} at index {}: value={}",
                                        mapping.getDbColumn(), paramIndex, value);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to get OUT parameter: {}", e.getMessage());
                        }
                        paramIndex++;
                    }
                }
            }

            // Process result set if any
            if (cs.getMoreResults()) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null) {
                        List<Map<String, Object>> resultSetData = new ArrayList<>();
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnName(i);
                                Object value = rs.getObject(i);
                                row.put(columnName, value);
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

            // If no response data, add default success
            if (responseData.isEmpty()) {
                responseData.put("success", true);
                responseData.put("message", "Procedure executed successfully");
            }

            log.info("============ PROCEDURE EXECUTION COMPLETE ============");
            return responseData;

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for {}.{}", owner, procedureName, e);
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
                                GeneratedApiEntity api, String procedureName) {
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

                    String[] possibleKeys = {"schemaName", "SchemaName", "schema_name", "SCHEMA_NAME",
                            "owner", "Owner", "OWNER", "targetOwner", "TargetOwner"};

                    for (String key : possibleKeys) {
                        if (sourceInfo.containsKey(key) && sourceInfo.get(key) != null) {
                            String value = sourceInfo.get(key).toString();
                            if (!value.trim().isEmpty()) {
                                log.info("Strategy 4 - Using {} from source_object_info Map: {}", key, value);
                                return value.trim().toUpperCase();
                            }
                        }
                    }
                } else {
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});

                    String[] possibleKeys = {"schemaName", "SchemaName", "schema_name", "SCHEMA_NAME",
                            "owner", "Owner", "OWNER", "targetOwner", "TargetOwner"};

                    for (String key : possibleKeys) {
                        if (sourceInfo.containsKey(key) && sourceInfo.get(key) != null) {
                            String value = sourceInfo.get(key).toString();
                            if (!value.trim().isEmpty()) {
                                log.info("Strategy 4 - Using {} from source_object_info JSON: {}", key, value);
                                return value.trim().toUpperCase();
                            }
                        }
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

        // Strategy 6: Try to locate the procedure in accessible schemas
        try (Connection conn = getConnectionWithTimeout();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND OBJECT_TYPE = 'PROCEDURE' AND ROWNUM = 1")) {

            pstmt.setString(1, procedureName);
            pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String foundOwner = rs.getString(1);
                    log.info("Strategy 6 - Found procedure '{}' in schema: {}", procedureName, foundOwner);
                    return foundOwner;
                }
            }

            // If not found as procedure, check if it's a function
            try (PreparedStatement pstmt2 = conn.prepareStatement(
                    "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND OBJECT_TYPE = 'FUNCTION' AND ROWNUM = 1")) {
                pstmt2.setString(1, procedureName);
                pstmt2.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                try (ResultSet rs2 = pstmt2.executeQuery()) {
                    if (rs2.next()) {
                        String foundOwner = rs2.getString(1);
                        log.warn("Strategy 6 - Found function '{}' in schema: {} (treating as procedure)", procedureName, foundOwner);
                        return foundOwner;
                    }
                }
            }

            log.warn("Strategy 6 - Could not locate procedure '{}' in any accessible schema", procedureName);

        } catch (Exception e) {
            log.warn("Error while searching for procedure in accessible schemas: {}", e.getMessage());
        }

        log.error("❌ All owner resolution strategies failed for procedure: {}", procedureName);
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