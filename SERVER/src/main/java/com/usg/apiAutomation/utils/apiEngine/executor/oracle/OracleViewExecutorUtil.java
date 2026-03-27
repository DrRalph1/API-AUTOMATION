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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequiredArgsConstructor
public class OracleViewExecutorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final OracleParameterValidatorUtil parameterValidator;
    private final OracleObjectResolverUtil objectResolver;
    private final OracleTableExecutorUtil oracleTableExecutorUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Timeout constants
    private static final int STATEMENT_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 30000;

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
                          String viewName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ ORACLE VIEW EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("View Name parameter: {}", viewName);
        log.info("Owner parameter: {}", owner);

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
                    apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toUpperCase());
                    log.info("Parameter mapping: API '{}' -> Database Column '{}'", param.getKey(), dbColumnName.toUpperCase());
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
                    if (!bodyString.isEmpty() && !bodyString.equals("{}")) {
                        dbParams.put("BODY_STRING", bodyString);
                    }
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
                        String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toUpperCase());

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
        }

        // ============ PROCESS XML BODY ============
        if (isXmlBody && xmlBody != null) {
            log.info("Processing XML body for view execution");

            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML", extractedXmlParams.size());
            }

            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String paramKey = param.getKey().toLowerCase();
                    String dbColumnName = getDbColumnName(param);

                    boolean isXmlParameter = paramKey.contains("xml") ||
                            paramKey.contains("clob") ||
                            paramKey.contains("where") ||
                            paramKey.contains("filter") ||
                            paramKey.contains("condition");

                    if (isXmlParameter && dbColumnName != null && !dbParams.containsKey(dbColumnName)) {
                        dbParams.put(dbColumnName, xmlBody);
                        hasXmlParameter = true;
                        log.info("✅ Mapped full XML body to database column: {}", dbColumnName);
                        break;
                    }
                }
            }

            if (!hasXmlParameter && extractedXmlParams.isEmpty()) {
                log.warn("⚠️ No suitable database column found for XML body");
                dbParams.put("WHERE_CONDITION", xmlBody);
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        addParameters(request, api, apiToDbColumnMap, dbParams);

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

        // ============ OWNER RESOLUTION STRATEGY ============
        String resolvedOwner = resolveOwner(owner, sourceObject, api, viewName);
        String resolvedViewName = viewName != null ? viewName.toUpperCase() : null;

        if (resolvedViewName == null) {
            throw new ValidationException("View name cannot be null");
        }

        log.info("Final resolved owner: {}", resolvedOwner);
        log.info("Final view name: {}", resolvedViewName);

        // ============ SYNONYM RESOLUTION ============
        Map<String, Object> resolutionResult = objectResolver.resolveObject(resolvedOwner, resolvedViewName, "VIEW");

        if (!(boolean) resolutionResult.getOrDefault("exists", false)) {
            String errorMsg = String.format("The view '%s.%s' does not exist or you don't have access to it.",
                    resolvedOwner, resolvedViewName);
            log.error("❌ {}", errorMsg);
            throw new ValidationException(errorMsg);
        }

        String targetOwner = (String) resolutionResult.get("targetOwner");
        String targetName = (String) resolutionResult.get("targetName");
        String targetType = (String) resolutionResult.get("targetType");
        boolean isSynonym = (boolean) resolutionResult.getOrDefault("isSynonym", false);

        log.info("🔍 Resolved to: {}.{} ({}) {}", targetOwner, targetName, targetType,
                isSynonym ? "(via synonym)" : "");

        if (!"VIEW".equalsIgnoreCase(targetType)) {
            throw new ValidationException(
                    String.format("Object '%s.%s' resolved to %s, but VIEW was expected",
                            resolvedOwner, resolvedViewName, targetType)
            );
        }

        // ==================== VALIDATION STEP 1: Validate view is accessible ====================
        try {
            objectResolver.validateDatabaseObject(targetOwner, targetName, "VIEW");
            log.info("✅ View {}.{} exists and is accessible", targetOwner, targetName);
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

            Set<String> configuredParamKeys = new HashSet<>();
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    configuredParamKeys.add(param.getKey().toLowerCase());
                    configuredParamKeys.add(param.getKey());
                }
            }

            log.info("Configured parameter keys: {}", configuredParamKeys);

            Map<String, Object> queryParamsForValidation = new HashMap<>();
            for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
                String key = entry.getKey();
                boolean isConfigured = false;
                for (String configuredKey : configuredParamKeys) {
                    if (configuredKey.equalsIgnoreCase(key)) {
                        isConfigured = true;
                        break;
                    }
                }

                if (isConfigured || "page".equalsIgnoreCase(key) || "pagesize".equalsIgnoreCase(key) ||
                        "sort".equalsIgnoreCase(key) || "order".equalsIgnoreCase(key) ||
                        "where_condition".equalsIgnoreCase(key)) {
                    queryParamsForValidation.put(key, entry.getValue());
                }
            }

            log.info("Validating view query with filtered params: {}", queryParamsForValidation.keySet());

            validateViewQuery(targetOwner, targetName, queryParamsForValidation, allowedColumns);
            log.info("✅ View query validation passed");
        } catch (ValidationException e) {
            log.error("❌ View query validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== VALIDATION STEP 3: Validate all parameters ====================
        try {
            parameterValidator.validateParameters(configuredParamDTOs, dbParams, targetOwner, targetName);
            log.info("✅ All parameter validations passed for view {}.{}", targetOwner, targetName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        log.info("============ VIEW EXECUTION COMPLETE ============");

        // Delegate to TableExecutorUtil for execution
        return oracleTableExecutorUtil.executeSelect(targetName, targetOwner, dbParams, api, configuredParamDTOs);
    }

    /**
     * Add parameters from request (path, query, headers)
     */
    private void addParameters(ExecuteApiRequestDTO request, GeneratedApiEntity api,
                               Map<String, String> apiToDbColumnMap, Map<String, Object> dbParams) {
        // Add path parameters
        if (request.getPathParams() != null) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String dbColumnName = apiToDbColumnMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toUpperCase());
                dbParams.put(dbColumnName, entry.getValue());
                log.debug("Added path param: {} -> {}", entry.getKey(), dbColumnName);
            }
        }

        // Add query parameters
        if (request.getQueryParams() != null) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String dbColumnName = apiToDbColumnMap.getOrDefault(entry.getKey().toLowerCase(), entry.getKey().toUpperCase());
                dbParams.put(dbColumnName, entry.getValue());
                log.debug("Added query param: {} -> {}", entry.getKey(), dbColumnName);
            }
        }

        // Add headers that are defined as parameters
        if (request.getHeaders() != null && api.getParameters() != null) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String headerKey = entry.getKey().toLowerCase();
                boolean headerIsParameter = api.getParameters().stream()
                        .anyMatch(p -> p.getKey().equalsIgnoreCase(headerKey));

                if (headerIsParameter) {
                    String dbColumnName = apiToDbColumnMap.getOrDefault(headerKey, headerKey.toUpperCase());
                    dbParams.put(dbColumnName, entry.getValue());
                    log.debug("Added header as parameter: {} -> {}", headerKey, dbColumnName);
                }
            }
        }
    }

    /**
     * Helper method to resolve the owner from multiple sources
     */
    private String resolveOwner(String owner, ApiSourceObjectDTO sourceObject,
                                GeneratedApiEntity api, String viewName) {
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

        // Strategy 6: Try to locate the view in accessible schemas
        try (Connection conn = getConnectionWithTimeout();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT OWNER FROM ALL_VIEWS WHERE VIEW_NAME = ? AND ROWNUM = 1")) {

            pstmt.setString(1, viewName);
            pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String foundOwner = rs.getString(1);
                    log.info("Strategy 6 - Found view '{}' in schema: {}", viewName, foundOwner);
                    return foundOwner;
                }
            }

            // If not found in views, check tables
            try (PreparedStatement pstmt2 = conn.prepareStatement(
                    "SELECT OWNER FROM ALL_TABLES WHERE TABLE_NAME = ? AND ROWNUM = 1")) {
                pstmt2.setString(1, viewName);
                pstmt2.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                try (ResultSet rs2 = pstmt2.executeQuery()) {
                    if (rs2.next()) {
                        String foundOwner = rs2.getString(1);
                        log.info("Strategy 6 - Found table '{}' in schema: {} (treating as view)", viewName, foundOwner);
                        return foundOwner;
                    }
                }
            }

            log.warn("Strategy 6 - Could not locate view '{}' in any accessible schema", viewName);

        } catch (Exception e) {
            log.warn("Error while searching for view in accessible schemas: {}", e.getMessage());
        }

        log.error("❌ All owner resolution strategies failed for view: {}", viewName);
        return null;
    }

    private String getDbColumnName(ApiParameterEntity param) {
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toUpperCase();
        }
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toUpperCase();
        }
        return param.getKey().toUpperCase();
    }

    private void validateViewQuery(String schemaName, String viewName, Map<String, Object> queryParams,
                                   List<String> allowedColumns) {
        try (Connection conn = getConnectionWithTimeout()) {
            // Check if view exists
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME = ?")) {

                pstmt.setString(1, schemaName);
                pstmt.setString(2, viewName);
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count == 0) {
                            throw new ValidationException(
                                    String.format("View '%s.%s' does not exist", schemaName, viewName)
                            );
                        }
                    }
                }
            }

            // Validate view is accessible
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);
                stmt.execute("SELECT 1 FROM " + schemaName + "." + viewName + " WHERE ROWNUM = 1");
            } catch (Exception e) {
                throw new ValidationException(
                        String.format("Cannot access view '%s.%s': %s",
                                schemaName, viewName, extractOracleError(e.getMessage()))
                );
            }

            // Validate query parameters against view columns
            if (queryParams != null && !queryParams.isEmpty()) {
                // Get view columns
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT COLUMN_NAME, DATA_TYPE, NULLABLE FROM ALL_TAB_COLUMNS " +
                                "WHERE OWNER = ? AND TABLE_NAME = ?")) {

                    pstmt.setString(1, schemaName);
                    pstmt.setString(2, viewName);
                    pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                    Map<String, Map<String, Object>> columnMap = new HashMap<>();
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> column = new HashMap<>();
                            column.put("DATA_TYPE", rs.getString("DATA_TYPE"));
                            column.put("NULLABLE", rs.getString("NULLABLE"));
                            columnMap.put(rs.getString("COLUMN_NAME").toLowerCase(), column);
                        }
                    }

                    // Validate each query parameter
                    for (Map.Entry<String, Object> param : queryParams.entrySet()) {
                        String paramName = param.getKey().toLowerCase();

                        // Skip special parameters
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
        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out validating view {}.{}", schemaName, viewName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (SQLException e) {
            throw new ValidationException(
                    String.format("Cannot validate view '%s.%s': %s", schemaName, viewName, e.getMessage())
            );
        }
    }

    private void validateColumnDataType(String columnName, Object value, Map<String, Object> column) {
        String dataType = (String) column.get("DATA_TYPE");
        String nullable = (String) column.get("NULLABLE");

        // Check for NOT NULL constraint
        if ("N".equals(nullable) && (value == null || value.toString().trim().isEmpty())) {
            throw new ValidationException(
                    String.format("Column '%s' cannot be null", columnName)
            );
        }

        // Basic data type validation
        if (value != null) {
            String upperDataType = dataType != null ? dataType.toUpperCase() : "";

            if (upperDataType.contains("NUMBER") || upperDataType.contains("INTEGER") ||
                    upperDataType.contains("NUMERIC")) {
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
            } else if (upperDataType.contains("DATE") || upperDataType.contains("TIMESTAMP")) {
                log.debug("Date value provided for column {}: {}", columnName, value);
            }
        }
    }

    /**
     * Parse XML body and extract parameter values
     */
    private Map<String, Object> parseXmlParameters(String xmlBody, List<ApiParameterDTO> configuredParamDTOs,
                                                   Map<String, String> apiToDbColumnMap) {
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
                        String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbColumnName, value);
                        log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbColumnName, value);
                    } else {
                        log.info("⚠️ XML tag <{}> found but empty", paramKey);
                        String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbColumnName, "");
                        log.info("Added empty parameter: {} -> {}", paramKey, dbColumnName);
                    }
                }
            }

            log.info("Extracted {} parameters from XML: {}", extractedParams.size(), extractedParams.keySet());

        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
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
}