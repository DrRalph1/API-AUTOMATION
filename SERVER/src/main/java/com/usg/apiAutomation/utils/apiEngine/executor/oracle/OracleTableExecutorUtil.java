package com.usg.apiAutomation.utils.apiEngine.executor.oracle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.utils.apiEngine.OracleParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OracleTableExecutorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final OracleParameterValidatorUtil parameterValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OracleTableExecutorUtil(
            OracleParameterValidatorUtil parameterValidator) {
        this.parameterValidator = parameterValidator;
    }

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

    /**
     * Extracts the full Oracle error message from the exception chain
     */
    private String extractFullOracleError(Exception e) {
        Throwable cause = e;
        Set<String> seenMessages = new HashSet<>();
        StringBuilder fullError = new StringBuilder();

        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && !message.isEmpty()) {
                // Avoid duplicate messages
                if (!seenMessages.contains(message)) {
                    seenMessages.add(message);

                    // Look for ORA-xxxxx pattern
                    if (message.contains("ORA-")) {
                        // This is the Oracle error we want
                        return message;
                    }

                    // Build chain if needed for debugging
                    if (fullError.length() > 0) {
                        fullError.append(" -> ");
                    }
                    fullError.append(message);
                }
            }
            cause = cause.getCause();
        }

        // If we found an ORA error, we would have returned it already
        // Otherwise return the chain or original message
        return fullError.length() > 0 ? fullError.toString() : e.getMessage();
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

    public Object executeSelect(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM ");
            if (owner != null && !owner.isEmpty()) {
                sql.append(owner).append(".");
            }
            sql.append(tableName);

            List<Object> paramValues = new ArrayList<>();
            List<String> whereClauses = new ArrayList<>();

            log.info("=== TABLE SELECT DEBUG ===");
            log.info("Table: {}.{}", owner, tableName);
            log.info("All incoming params: {}", params);

            // Build a clean parameter map - keep ALL parameters
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
                        apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toUpperCase());
                        log.info("Parameter mapping: API '{}' -> Database Column '{}'", param.getKey(), dbColumnName.toUpperCase());
                    }
                }
            }

            // Process XML body if present
            String xmlBody = null;
            boolean isXmlBody = false;

            if (params != null && params.containsKey("_xml")) {
                Object xmlObj = params.get("_xml");
                if (xmlObj instanceof String) {
                    String xmlString = (String) xmlObj;
                    if (xmlString.trim().startsWith("<")) {
                        isXmlBody = true;
                        xmlBody = xmlString;
                        log.info("=========================================");
                        log.info("XML BODY DETECTED in TableExecutor!");
                        log.info("XML Length: {} characters", xmlBody.length());
                        log.info("XML Preview: {}", xmlBody.substring(0, Math.min(500, xmlBody.length())));
                        log.info("=========================================");

                        // Extract parameters from XML
                        Map<String, Object> extractedParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
                        if (!extractedParams.isEmpty()) {
                            cleanParams.putAll(extractedParams);
                            log.info("✅ Extracted {} parameters from XML and added to cleanParams", extractedParams.size());
                        }
                    }
                }
            }

            // Copy all parameters
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    // Skip the _xml key as it's already processed
                    if ("_xml".equals(key)) {
                        continue;
                    }

                    // Map the key to database column name if mapping exists
                    String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
                    cleanParams.put(dbColumnName, entry.getValue());
                }

                // Also handle any numbered parameters (param1, param2, etc.) that might come from path
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("param") && key.length() > 5) {
                        try {
                            int position = Integer.parseInt(key.substring(5)) - 1;
                            cleanParams.put("position_" + position, entry.getValue());
                            log.info("Mapped numbered param {} to position {}", key, position);
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            }

            log.info("Cleaned params after XML processing: {}", cleanParams);

            // Build WHERE clause from configured parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                log.info("Processing {} configured parameters", api.getParameters().size());

                for (ApiParameterEntity configuredParam : api.getParameters()) {
                    String paramKey = configuredParam.getKey();
                    String dbColumn = configuredParam.getDbColumn();
                    String paramType = configuredParam.getParameterType();

                    // Default to "query" if paramType is null
                    if (paramType == null) {
                        paramType = "query";
                        log.debug("Parameter {} has null paramType, defaulting to 'query'", paramKey);
                    }

                    if (dbColumn == null || dbColumn.isEmpty()) {
                        log.debug("Parameter {} has no dbColumn mapping, skipping", paramKey);
                        continue;
                    }

                    boolean isValidForFiltering = "query".equals(paramType) ||
                            "path".equals(paramType) ||
                            "body".equals(paramType);

                    if (isValidForFiltering) {
                        log.info("Processing parameter: key='{}', dbColumn='{}', paramType='{}', required={}",
                                paramKey, dbColumn, paramType, configuredParam.getRequired());

                        Object value = null;

                        // Try to find value by direct key match
                        if (cleanParams.containsKey(paramKey)) {
                            value = cleanParams.get(paramKey);
                            log.info("  Found exact match for key '{}' with value: {}", paramKey, value);
                        }

                        // Try case-insensitive match
                        if (value == null) {
                            for (Map.Entry<String, Object> entry : cleanParams.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(paramKey)) {
                                    value = entry.getValue();
                                    log.info("  Found case-insensitive match for key '{}' with value: {}", paramKey, value);
                                    break;
                                }
                            }
                        }

                        // Try to find by position if this is a path parameter
                        if (value == null && "path".equals(paramType)) {
                            Integer position = configuredParam.getPosition();
                            if (position != null) {
                                String positionKey = "position_" + position;
                                if (cleanParams.containsKey(positionKey)) {
                                    value = cleanParams.get(positionKey);
                                    log.info("  Found by position {} with value: {}", position, value);
                                }
                            }
                        }

                        if (value != null) {
                            // Handle collection/array values
                            if (value instanceof List || value.getClass().isArray()) {
                                Collection<?> collection = value instanceof List ?
                                        (List<?>) value : Arrays.asList((Object[]) value);

                                if (!collection.isEmpty()) {
                                    value = collection.iterator().next();
                                    log.info("  Converted collection to single value: {}", value);
                                } else {
                                    value = null;
                                }
                            }

                            if (value != null && !value.toString().trim().isEmpty()) {
                                whereClauses.add(dbColumn + " = ?");
                                paramValues.add(value);
                                log.info("  ADDED FILTER: {} = ? with value: {}", dbColumn, value);
                            } else {
                                if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                                    throw new ValidationException(
                                            String.format("Required parameter '%s' cannot be empty", paramKey)
                                    );
                                }
                                log.info("Optional parameter {} not provided or empty, skipping filter", paramKey);
                            }
                        } else {
                            if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                                // FIX: Better error message showing all available params
                                throw new ValidationException(
                                        String.format("Required parameter '%s' is missing. Available params: %s. Request params: %s",
                                                paramKey, cleanParams.keySet(), params.keySet())
                                );
                            }
                            log.info("Optional parameter {} not provided, skipping filter", paramKey);
                        }
                    } else {
                        log.info("Parameter {} with type '{}' is not for filtering, skipping", paramKey, paramType);
                    }
                }
            }

            // Add WHERE clause if we have conditions
            if (!whereClauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
            }

            // Handle pagination if enabled
            if (api.getSchemaConfig() != null &&
                    Boolean.TRUE.equals(api.getSchemaConfig().getEnablePagination())) {
                int pageSize = api.getSchemaConfig().getPageSize() != null ?
                        api.getSchemaConfig().getPageSize() : 10;
                int page = 1;

                if (params != null && params.containsKey("page")) {
                    try {
                        page = Integer.parseInt(params.get("page").toString());
                        if (page < 1) page = 1;
                        log.info("Page parameter found: {}", page);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid page parameter, using default: 1");
                    }
                }

                int offset = (page - 1) * pageSize;
                sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
                paramValues.add(offset);
                paramValues.add(pageSize);
                log.info("Added pagination: offset={}, pageSize={}", offset, pageSize);
            }

            // CLEAN THE SQL BEFORE EXECUTION - THIS IS THE FIX
            String originalSql = sql.toString();
            String cleanedSql = cleanSqlStatement(originalSql);

            log.info("Final SQL before cleaning: {}", originalSql);
            log.info("Final SQL after cleaning: {}", cleanedSql);
            log.info("SQL parameters: {}", paramValues.size());

            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(
                    cleanedSql, paramValues.toArray());
            log.info("Query returned {} rows", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error executing table select: {}", e.getMessage(), e);

            String detailedError = extractFullOracleError(e);

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

            throw new RuntimeException("Failed to execute SELECT operation: " + detailedError, e);
        }
    }

    public Object executeInsert(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for INSERT operation");
        }

        log.info("=== TABLE INSERT DEBUG ===");
        log.info("Table: {}.{}", owner, tableName);
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
                    apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toUpperCase());
                    log.info("Parameter mapping: API '{}' -> DB Column '{}'", param.getKey(), dbColumnName.toUpperCase());
                }
            }
        }

        // Process all parameters and map them to database columns
        Map<String, Object> processedParams = new HashMap<>();

        // Process XML body if present
        String xmlBody = null;
        boolean isXmlBody = false;

        if (params.containsKey("_xml")) {
            Object xmlObj = params.get("_xml");
            if (xmlObj instanceof String) {
                String xmlString = (String) xmlObj;
                if (xmlString.trim().startsWith("<")) {
                    isXmlBody = true;
                    xmlBody = xmlString;
                    log.info("=========================================");
                    log.info("XML BODY DETECTED in INSERT operation!");
                    log.info("XML Length: {} characters", xmlBody.length());
                    log.info("XML Preview: {}", xmlBody.substring(0, Math.min(500, xmlBody.length())));
                    log.info("=========================================");

                    // Extract parameters from XML
                    Map<String, Object> extractedParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                        log.info("✅ Extracted {} parameters from XML and added to processedParams", extractedParams.size());
                    }
                }
            }
        }

        // IMPORTANT FIX: Process all parameters, not just XML-extracted ones
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip the _xml key if we already processed it (or if it's not needed)
            if ("_xml".equals(key)) {
                // Only skip if we actually used the XML body (i.e., it was valid XML)
                // If it was just a string but not XML, we might still need it
                if (isXmlBody) {
                    continue;
                }
            }

            // Skip if this key is already in processedParams (from XML extraction)
            if (processedParams.containsKey(key) || processedParams.containsKey(key.toUpperCase())) {
                continue;
            }

            // Map the key to database column name if mapping exists
            String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key.toUpperCase());

            // Handle the value
            Object finalValue = value;
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    finalValue = collection.iterator().next();
                    log.info("Converted collection parameter '{}' to single value: {}", key, finalValue);
                } else {
                    finalValue = null;
                }
            }

            if (finalValue != null && !finalValue.toString().trim().isEmpty()) {
                processedParams.put(dbColumnName, finalValue);
                log.info("Added param: {} -> {} = {}", key, dbColumnName, finalValue);
            } else {
                log.info("Skipped empty param: {} -> {}", key, dbColumnName);
            }
        }

        // Also process any parameters that might be in the API parameters but not in the request
        // This handles default values
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            for (ApiParameterEntity apiParam : api.getParameters()) {
                String paramKey = apiParam.getKey();
                String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());

                // If this parameter hasn't been set yet and has a default value, use it
                if (!processedParams.containsKey(dbColumnName) && apiParam.getDefaultValue() != null) {
                    processedParams.put(dbColumnName, apiParam.getDefaultValue());
                    log.info("Using default value for parameter '{}': {}", paramKey, apiParam.getDefaultValue());
                }
            }
        }

        log.info("Final processed params for INSERT: {}", processedParams);
        log.info("Final processed params keys: {}", processedParams.keySet());

        // Check if we have any parameters to insert
        if (processedParams.isEmpty()) {
            log.error("No parameters to insert! Original params: {}, Processed params: {}", params.keySet(), processedParams.keySet());
            throw new RuntimeException("No parameters provided for INSERT operation");
        }

        // Build the INSERT SQL
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("?");
            paramValues.add(entry.getValue());
            log.debug("Column: {} = {}", entry.getKey(), entry.getValue());
        }

        String sql = "INSERT INTO " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                " (" + columns + ") VALUES (" + values + ")";

        log.info("Final INSERT SQL: {}", sql);
        log.info("INSERT parameters: {}", paramValues);

        try {
            int rowsAffected = oracleJdbcTemplate.update(sql, paramValues.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Insert successful" : "No rows inserted");

            // If we have response mappings, try to fetch the inserted record
            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                // Find primary key column
                String pkColumn = api.getResponseMappings().stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                        .map(ApiResponseMappingEntity::getDbColumn)
                        .findFirst()
                        .orElse(null);

                if (pkColumn != null && processedParams.containsKey(pkColumn)) {
                    String selectSql = "SELECT * FROM " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                            " WHERE " + pkColumn + " = ?";
                    try {
                        List<Map<String, Object>> inserted = oracleJdbcTemplate.queryForList(
                                selectSql, processedParams.get(pkColumn));
                        if (!inserted.isEmpty()) {
                            result.put("data", inserted.get(0));
                        }
                    } catch (Exception e) {
                        log.warn("Could not fetch inserted record: {}", e.getMessage());
                    }
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Error executing INSERT on {}: {}", tableName, e.getMessage(), e);

            String detailedError = extractFullOracleError(e);

            // Provide user-friendly error messages with full Oracle details
            if (e.getMessage() != null) {
                if (e.getMessage().contains("ORA-00942")) {
                    throw new RuntimeException("Table not found: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-01031")) {
                    throw new RuntimeException("Insufficient privileges: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-01400")) {
                    throw new RuntimeException("Cannot insert NULL into non-nullable column: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-02291")) {
                    throw new RuntimeException("Parent key not found - integrity constraint violation: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-02290")) {
                    throw new RuntimeException("Check constraint violation: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-12899")) {
                    throw new RuntimeException("Value too large for column: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-00001")) {
                    throw new RuntimeException("Unique constraint violation: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-01722")) {
                    throw new RuntimeException("Invalid number: " + detailedError, e);
                }
            }

            throw new RuntimeException("Failed to execute INSERT operation: " + detailedError, e);
        }
    }

    public Object executeUpdate(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for UPDATE operation");
        }

        log.info("=== TABLE UPDATE DEBUG ===");
        log.info("Table: {}.{}", owner, tableName);

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
                    apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toUpperCase());
                }
            }
        }

        // Process XML body if present
        Map<String, Object> processedParams = new HashMap<>();
        String xmlBody = null;

        if (params.containsKey("_xml")) {
            Object xmlObj = params.get("_xml");
            if (xmlObj instanceof String) {
                String xmlString = (String) xmlObj;
                if (xmlString.trim().startsWith("<")) {
                    log.info("XML BODY DETECTED in UPDATE operation!");
                    xmlBody = xmlString;

                    // Extract parameters from XML
                    Map<String, Object> extractedParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                        log.info("✅ Extracted {} parameters from XML for UPDATE", extractedParams.size());
                    }
                }
            }
        }

        // Copy all parameters, mapping to database column names
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            // Skip the _xml key as it's already processed
            if ("_xml".equals(key)) {
                continue;
            }

            // Map the key to database column name if mapping exists
            String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
            processedParams.put(dbColumnName, entry.getValue());
        }

        // Handle collection/array parameters - convert to single values for database
        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value
                    processedParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value for UPDATE", entry.getKey());
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

        String sql = "UPDATE " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                " SET " + setClause + whereClause;

        List<Object> allParams = new ArrayList<>(setValues);
        allParams.addAll(whereValues);

        try {
            int rowsAffected = oracleJdbcTemplate.update(sql, allParams.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Update successful" : "No rows updated");

            return result;

        } catch (Exception e) {
            log.error("Error executing UPDATE on {}: {}", tableName, e.getMessage(), e);

            String detailedError = extractFullOracleError(e);

            // Provide user-friendly error messages with full Oracle details
            if (e.getMessage() != null) {
                if (e.getMessage().contains("ORA-00942")) {
                    throw new RuntimeException("Table not found: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-01031")) {
                    throw new RuntimeException("Insufficient privileges: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-02291")) {
                    throw new RuntimeException("Parent key not found - integrity constraint violation: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-02292")) {
                    throw new RuntimeException("Child record found - integrity constraint violation: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-12899")) {
                    throw new RuntimeException(detailedError, e);
                }
                if (e.getMessage().contains("ORA-00001")) {
                    throw new RuntimeException("Unique constraint violation: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-01407")) {
                    throw new RuntimeException("Cannot update to NULL: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-01722")) {
                    throw new RuntimeException("Invalid number: " + detailedError, e);
                }
            }

            throw new RuntimeException("Failed to execute UPDATE operation: " + detailedError, e);
        }
    }

    public Object executeDelete(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for DELETE operation");
        }

        log.info("=== TABLE DELETE DEBUG ===");
        log.info("Table: {}.{}", owner, tableName);

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
                    apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toUpperCase());
                }
            }
        }

        // Process XML body if present
        Map<String, Object> processedParams = new HashMap<>();
        String xmlBody = null;

        if (params.containsKey("_xml")) {
            Object xmlObj = params.get("_xml");
            if (xmlObj instanceof String) {
                String xmlString = (String) xmlObj;
                if (xmlString.trim().startsWith("<")) {
                    log.info("XML BODY DETECTED in DELETE operation!");
                    xmlBody = xmlString;

                    // Extract parameters from XML
                    Map<String, Object> extractedParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
                    if (!extractedParams.isEmpty()) {
                        processedParams.putAll(extractedParams);
                        log.info("✅ Extracted {} parameters from XML for DELETE", extractedParams.size());
                    }
                }
            }
        }

        // Copy all parameters, mapping to database column names
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            // Skip the _xml key as it's already processed
            if ("_xml".equals(key)) {
                continue;
            }

            // Map the key to database column name if mapping exists
            String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
            processedParams.put(dbColumnName, entry.getValue());
        }

        // Handle collection/array parameters - convert to single values for database
        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value
                    processedParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value for DELETE", entry.getKey());
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

        String sql = "DELETE FROM " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName + whereClause;

        try {
            int rowsAffected = oracleJdbcTemplate.update(sql, whereValues.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Delete successful" : "No rows deleted");

            return result;

        } catch (Exception e) {
            log.error("Error executing DELETE on {}: {}", tableName, e.getMessage(), e);

            String detailedError = extractFullOracleError(e);

            // Provide user-friendly error messages with full Oracle details
            if (e.getMessage() != null) {
                if (e.getMessage().contains("ORA-00942")) {
                    throw new RuntimeException("Table not found: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-01031")) {
                    throw new RuntimeException("Insufficient privileges: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-02292")) {
                    throw new RuntimeException("Child record found - integrity constraint violation: " + detailedError, e);
                }
                if (e.getMessage().contains("ORA-02291")) {
                    throw new RuntimeException("Parent key not found: " + detailedError, e);
                }
            }

            throw new RuntimeException("Failed to execute DELETE operation: " + detailedError, e);
        }
    }
}