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

import java.util.*;
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

    /**
     * Cleans SQL statements by removing trailing semicolons and other common issues
     * @param sql The SQL statement to clean
     * @return Cleaned SQL statement safe for JDBC execution
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
                          String viewName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ VIEW EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("View Name parameter: {}", viewName);
        log.info("Owner parameter: {}", owner);
        log.info("Request Body Type: {}", request.getBody() != null ? request.getBody().getClass().getName() : "null");
        log.info("Request Body: {}", request.getBody());

        // ============ CREATE PARAMETER MAPPING ============
        // Build a map of API parameter keys to database column names
        Map<String, String> apiToDbColumnMap = new HashMap<>();
        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (param.getKey() != null) {
                    // For views, we typically use dbColumn as the column name
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

        // ============ HANDLE XML BODY ============
        Map<String, Object> dbParams = new HashMap<>();
        String xmlBody = null;
        boolean isXmlBody = false;
        boolean hasXmlParameter = false;

        // Check if request body is a String and looks like XML
        if (request.getBody() != null) {
            if (request.getBody() instanceof String) {
                String bodyString = (String) request.getBody();
                // Check if it's XML (starts with <)
                if (bodyString.trim().startsWith("<")) {
                    isXmlBody = true;
                    xmlBody = bodyString;
                    log.info("=========================================");
                    log.info("XML BODY DETECTED!");
                    log.info("XML Length: {} characters", xmlBody.length());
                    log.info("XML Preview: {}", xmlBody.substring(0, Math.min(500, xmlBody.length())));
                    log.info("=========================================");
                } else {
                    // Regular string body - might be JSON or plain text
                    log.info("String body detected (non-XML): {}", bodyString.substring(0, Math.min(100, bodyString.length())));
                    // For view queries, a plain string might be a simple query parameter
                    if (!bodyString.isEmpty() && !bodyString.equals("{}")) {
                        dbParams.put("BODY_STRING", bodyString);
                    }
                }
            } else if (request.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();

                // Check for wrapped XML from old format
                if (bodyMap.containsKey("_xml")) {
                    isXmlBody = true;
                    xmlBody = (String) bodyMap.get("_xml");
                    log.info("Found XML in _xml wrapper: {}", xmlBody.substring(0, Math.min(200, xmlBody.length())));
                } else {
                    // Regular JSON body
                    log.info("JSON body detected with keys: {}", bodyMap.keySet());
                    for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                        String paramKey = entry.getKey().toLowerCase();
                        String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toUpperCase());

                        // Handle nested objects and arrays
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

            // FIRST: Try to extract individual parameters from XML
            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML and added to dbParams", extractedXmlParams.size());
                log.info("Extracted params: {}", extractedXmlParams.keySet());
            }

            // For views, we might need to handle XML specially - it could be used for dynamic WHERE clauses
            // or stored as a parameter for XML-based queries
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String paramKey = param.getKey().toLowerCase();
                    String dbColumnName = getDbColumnName(param);

                    // Check if this parameter is designed to accept XML
                    boolean isXmlParameter = paramKey.contains("xml") ||
                            paramKey.contains("clob") ||
                            paramKey.contains("where") ||
                            paramKey.contains("filter") ||
                            paramKey.contains("condition");

                    if (isXmlParameter && dbColumnName != null) {
                        if (!dbParams.containsKey(dbColumnName)) {
                            dbParams.put(dbColumnName, xmlBody);
                            hasXmlParameter = true;
                            log.info("✅ Mapped full XML body to database column: {}", dbColumnName);
                        }
                        break;
                    }
                }
            }

            // If no explicit XML parameter found, log warning
            if (!hasXmlParameter && extractedXmlParams.isEmpty()) {
                log.warn("⚠️ No suitable database column found for XML body. XML will be stored as a WHERE_CONDITION.");
                dbParams.put("WHERE_CONDITION", xmlBody);
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        // Add path parameters
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                dbParams.put(dbColumnName, entry.getValue());
                log.debug("Added path param: {} -> {} = {}", entry.getKey(), dbColumnName, entry.getValue());
            }
            log.info("Path params added: {}", request.getPathParams().keySet());
        }

        // Add query parameters
        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                dbParams.put(dbColumnName, entry.getValue());
                log.debug("Added query param: {} -> {} = {}", entry.getKey(), dbColumnName, entry.getValue());
            }
            log.info("Query params added: {}", request.getQueryParams().keySet());
        }

        // ============ ADD HEADERS AS PARAMETERS (if needed) ============
        Map<String, Object> headerParams = new HashMap<>();
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String headerKey = entry.getKey().toLowerCase();
                // Check if the API expects this header as a parameter
                boolean headerIsParameter = api.getParameters() != null && api.getParameters().stream()
                        .anyMatch(p -> p.getKey().equalsIgnoreCase(headerKey));

                if (headerIsParameter) {
                    String dbColumnName = apiToDbColumnMap.getOrDefault(headerKey, headerKey.toUpperCase());
                    dbParams.put(dbColumnName, entry.getValue());
                    headerParams.put(headerKey, entry.getValue());
                    log.debug("Added header as parameter: {} -> {} = {}", headerKey, dbColumnName, entry.getValue());
                } else {
                    // Store header for potential use but not for column validation
                    headerParams.put(headerKey, entry.getValue());
                    log.debug("Header stored for reference (not as column param): {} = {}", headerKey, entry.getValue());
                }
            }
            log.info("Headers added: {}", request.getHeaders().keySet());

            if (request.getHeaders().containsKey("group_id")) {
                log.info("✅ group_id header found with value: {}", request.getHeaders().get("group_id"));
            }
        }

        // ============ HANDLE COLLECTION/ARRAY PARAMETERS ============
        // Convert collection/array parameters to single values for WHERE clause
        for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value for WHERE clause
                    dbParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value", entry.getKey());
                } else {
                    dbParams.put(entry.getKey(), null);
                }
            }
        }

        log.info("Final DB params for view execution: {}", dbParams.keySet());

        // Verify group_id is in the params
        if (dbParams.containsKey("GROUP_ID") || headerParams.containsKey("group_id")) {
            log.info("✅ group_id successfully passed to view executor");
        }

        // ============ OWNER RESOLUTION STRATEGY ============
        String resolvedOwner = resolveOwner(owner, sourceObject, api, viewName);
        String resolvedViewName = viewName != null ? viewName.toUpperCase() : null;

        if (resolvedViewName == null) {
            throw new ValidationException("View name cannot be null");
        }

        log.info("Final resolved owner: {}", resolvedOwner);
        log.info("Final view name: {}", resolvedViewName);

        // ============ SYNONYM RESOLUTION ============
        // Resolve the actual target (handle synonyms)
        Map<String, Object> resolutionResult = objectResolver.resolveObject(resolvedOwner, resolvedViewName, "VIEW");

        if (!(boolean) resolutionResult.getOrDefault("exists", false)) {
            String errorMsg = String.format("The view '%s.%s' does not exist or you don't have access to it.",
                    resolvedOwner, resolvedViewName);
            log.error("❌ {}", errorMsg);
            throw new ValidationException(errorMsg);
        }

        // Get the resolved target information
        String targetOwner = (String) resolutionResult.get("targetOwner");
        String targetName = (String) resolutionResult.get("targetName");
        String targetType = (String) resolutionResult.get("targetType");
        boolean isSynonym = (boolean) resolutionResult.getOrDefault("isSynonym", false);

        log.info("🔍 Resolved to: {}.{} ({}) {}", targetOwner, targetName, targetType,
                isSynonym ? "(via synonym)" : "");

        // If it resolved to something other than a VIEW, that's a problem
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
            // Get allowed columns from response mappings or API parameters
            List<String> allowedColumns = new ArrayList<>();
            if (api.getResponseMappings() != null) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (mapping.getDbColumn() != null) {
                        allowedColumns.add(mapping.getDbColumn().toLowerCase());
                    }
                }
            }

            // CRITICAL FIX: Create a filtered map for view column validation
            // Only include parameters that are configured in the API definition
            Map<String, Object> queryParamsForValidation = new HashMap<>();

            // Get the list of configured parameter keys from the API
            Set<String> configuredParamKeys = new HashSet<>();
            if (api.getParameters() != null) {
                for (ApiParameterEntity param : api.getParameters()) {
                    configuredParamKeys.add(param.getKey().toLowerCase());
                    configuredParamKeys.add(param.getKey()); // Keep original case too
                }
            }

            log.info("Configured parameter keys: {}", configuredParamKeys);

            // Only include parameters that are in the configured list
            for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
                String key = entry.getKey();
                // Check if this is a configured parameter (case-insensitive)
                boolean isConfigured = false;
                for (String configuredKey : configuredParamKeys) {
                    if (configuredKey.equalsIgnoreCase(key)) {
                        isConfigured = true;
                        break;
                    }
                }

                // Also include common pagination parameters
                if (isConfigured || "page".equalsIgnoreCase(key) || "pagesize".equalsIgnoreCase(key) ||
                        "sort".equalsIgnoreCase(key) || "order".equalsIgnoreCase(key) ||
                        "where_condition".equalsIgnoreCase(key)) {
                    queryParamsForValidation.put(key, entry.getValue());
                } else {
                    log.debug("Excluding header/other param from view validation: {} = {}", key, entry.getValue());
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

        // Pass ALL combined parameters to TableExecutorUtil (including headers)
        return oracleTableExecutorUtil.executeSelect(targetName, targetOwner, dbParams, api, configuredParamDTOs);
    }

    /**
     * Helper method to resolve the owner from multiple sources
     */
    private String resolveOwner(String owner, ApiSourceObjectDTO sourceObject, GeneratedApiEntity api, String viewName) {
        // Strategy 1: Use the owner parameter passed to the method
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

        // Strategy 4: Get from API's source_object_info (as Map)
        if (api != null && api.getSourceObjectInfo() != null) {
            try {
                // Check if it's already a Map
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
                    // If it's a String, parse it
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
        try {
            String currentSchema = objectResolver.getCurrentSchema();
            if (currentSchema != null && !currentSchema.isEmpty()) {
                log.info("Strategy 5 - Using current schema from Oracle: {}", currentSchema);
                return currentSchema;
            }
        } catch (Exception e) {
            log.warn("Could not get current schema: {}", e.getMessage());
        }

        // Strategy 6: Try to resolve the view from all accessible schemas
        try {
            log.info("Strategy 6 - Attempting to locate view '{}' in accessible schemas", viewName);

            // Query to find the view in any schema the current user has access to
            String findViewSql = "SELECT OWNER FROM ALL_VIEWS WHERE VIEW_NAME = ? AND ROWNUM = 1";
            List<String> owners = oracleJdbcTemplate.queryForList(findViewSql, String.class, viewName);

            if (!owners.isEmpty()) {
                String foundOwner = owners.get(0);
                log.info("Strategy 6 - Found view '{}' in schema: {}", viewName, foundOwner);
                return foundOwner;
            }

            // If not found in views, check if it's a table (in case it's a table being accessed as a view)
            String findTableSql = "SELECT OWNER FROM ALL_TABLES WHERE TABLE_NAME = ? AND ROWNUM = 1";
            List<String> tableOwners = oracleJdbcTemplate.queryForList(findTableSql, String.class, viewName);

            if (!tableOwners.isEmpty()) {
                String foundOwner = tableOwners.get(0);
                log.info("Strategy 6 - Found table '{}' in schema: {} (treating as view)", viewName, foundOwner);
                return foundOwner;
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
        // Check if view exists
        String sql = "SELECT COUNT(*) FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME = ?";

        // Clean the SQL before execution
        String cleanedSql = cleanSqlStatement(sql);

        Integer count = oracleJdbcTemplate.queryForObject(cleanedSql, Integer.class, schemaName, viewName);
        if (count == null || count == 0) {
            throw new ValidationException(
                    String.format("View '%s.%s' does not exist", schemaName, viewName)
            );
        }

        // Validate view is accessible
        try {
            String testSql = "SELECT 1 FROM " + schemaName + "." + viewName + " WHERE ROWNUM = 1";
            String cleanedTestSql = cleanSqlStatement(testSql);
            oracleJdbcTemplate.execute(cleanedTestSql);
        } catch (Exception e) {
            throw new ValidationException(
                    String.format("Cannot access view '%s.%s': %s",
                            schemaName, viewName, extractOracleError(e.getMessage()))
            );
        }

        // Validate query parameters against view columns
        if (queryParams != null && !queryParams.isEmpty()) {
            // Get view columns
            String columnSql =
                    "SELECT COLUMN_NAME, DATA_TYPE, NULLABLE " +
                            "FROM ALL_TAB_COLUMNS " +
                            "WHERE OWNER = ? AND TABLE_NAME = ?";

            // Clean the SQL before execution
            String cleanedColumnSql = cleanSqlStatement(columnSql);

            List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(
                    cleanedColumnSql, schemaName, viewName);

            Map<String, Map<String, Object>> columnMap = new HashMap<>();
            for (Map<String, Object> column : columns) {
                columnMap.put(((String) column.get("COLUMN_NAME")).toLowerCase(), column);
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

                // Check if parameter corresponds to a view column
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
                // Basic date validation - you might want to enhance this
                if (!(value instanceof Date) && !(value instanceof java.sql.Timestamp)) {
                    log.debug("Date value provided for column {}: {}", columnName, value);
                }
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
        log.debug("XML Body: {}", xmlBody);

        try {
            // For each configured parameter, try to extract its value from XML
            for (ApiParameterDTO param : configuredParamDTOs) {
                String paramKey = param.getKey();
                if (paramKey == null || paramKey.isEmpty()) {
                    continue;
                }

                // Look for XML tags with this key (case-insensitive)
                // Pattern matches: <acct_link>value</acct_link> or <ACCT_LINK>value</ACCT_LINK>
                Pattern pattern = Pattern.compile("<" + paramKey + ">(.*?)</" + paramKey + ">",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(xmlBody);

                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    if (!value.isEmpty()) {
                        // Map to database column name
                        String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbColumnName, value);
                        log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbColumnName, value);
                    } else {
                        log.info("⚠️ XML tag <{}> found but empty", paramKey);
                        // Still add empty string as a value (required parameter might accept empty)
                        String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbColumnName, "");
                        log.info("Added empty parameter: {} -> {}", paramKey, dbColumnName);
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

    /**
     * Helper method to extract Oracle error message
     */
    private String extractOracleError(String errorMessage) {
        if (errorMessage == null) return "Unknown error";

        // Look for ORA-xxxxx pattern
        Pattern pattern = Pattern.compile("ORA-\\d{5}:[^\\n]*");
        Matcher matcher = pattern.matcher(errorMessage);
        if (matcher.find()) {
            return matcher.group();
        }

        // Return first line if it's long
        if (errorMessage.length() > 200) {
            return errorMessage.substring(0, 200) + "...";
        }
        return errorMessage;
    }
}