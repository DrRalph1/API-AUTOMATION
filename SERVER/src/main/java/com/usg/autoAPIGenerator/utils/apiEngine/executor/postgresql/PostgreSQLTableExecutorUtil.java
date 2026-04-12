package com.usg.autoAPIGenerator.utils.apiEngine.executor.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.utils.apiEngine.PostgreSQLParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
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

    // Timeout constants
    private static final int STATEMENT_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 30000;

    // Flag to control whether to capture RAISE NOTICE messages
    private boolean captureNotices = false;

    public PostgreSQLTableExecutorUtil(
            PostgreSQLParameterValidatorUtil parameterValidator) {
        this.parameterValidator = parameterValidator;
    }

    /**
     * Set whether to capture RAISE NOTICE messages
     */
    public void setCaptureNotices(boolean captureNotices) {
        this.captureNotices = captureNotices;
    }

    /**
     * Get whether notices are being captured
     */
    public boolean isCaptureNotices() {
        return captureNotices;
    }

    /**
     * Build SELECT clause with field selection
     * Supports:
     * 1. fields parameter in request (comma-separated string or array)
     * 2. Response mappings for default field selection
     * 3. Aliases for fields (apiField -> dbColumn)
     */
    private String buildSelectClause(GeneratedApiEntity api, Map<String, Object> params,
                                     Map<String, String> apiToDbColumnMap) {
        // Priority 1: Check if fields are specified in the request
        if (params != null && params.containsKey("fields")) {
            Object fieldsObj = params.get("fields");
            if (fieldsObj != null) {
                List<String> selectedFields = new ArrayList<>();

                // Handle comma-separated string
                if (fieldsObj instanceof String) {
                    String fields = (String) fieldsObj;
                    for (String field : fields.split(",")) {
                        field = field.trim();
                        if (!field.isEmpty()) {
                            selectedFields.add(field);
                        }
                    }
                }
                // Handle array/list
                else if (fieldsObj instanceof List) {
                    for (Object field : (List<?>) fieldsObj) {
                        if (field != null) {
                            selectedFields.add(field.toString().trim());
                        }
                    }
                }

                if (!selectedFields.isEmpty()) {
                    List<String> mappedFields = new ArrayList<>();
                    for (String field : selectedFields) {
                        // Check if we have response mapping for this field (for alias)
                        boolean found = false;
                        if (api.getResponseMappings() != null) {
                            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                                if (mapping.getApiField() != null &&
                                        mapping.getApiField().equalsIgnoreCase(field) &&
                                        mapping.getDbColumn() != null) {
                                    // Use dbColumn with apiField alias (PostgreSQL uses double quotes for identifiers)
                                    mappedFields.add("\"" + mapping.getDbColumn() + "\" AS \"" + mapping.getApiField() + "\"");
                                    found = true;
                                    break;
                                }
                            }
                        }

                        // If no mapping found, use field name as is (might be a direct column name)
                        if (!found) {
                            // Try to find in API to DB mapping
                            String dbColumn = apiToDbColumnMap.getOrDefault(field.toLowerCase(), field);
                            // Quote column name to preserve case sensitivity
                            mappedFields.add("\"" + dbColumn + "\"");
                        }
                    }
                    return String.join(", ", mappedFields);
                }
            }
        }

        // Priority 2: Use response mappings to determine fields
        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            List<String> selectedFields = new ArrayList<>();

            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse()) && mapping.getDbColumn() != null) {
                    String dbColumn = mapping.getDbColumn();
                    String apiField = mapping.getApiField();

                    // Use alias if apiField is different from dbColumn
                    if (apiField != null && !apiField.isEmpty() && !apiField.equals(dbColumn)) {
                        selectedFields.add("\"" + dbColumn + "\" AS \"" + apiField + "\"");
                    } else {
                        selectedFields.add("\"" + dbColumn + "\"");
                    }
                }
            }

            if (!selectedFields.isEmpty()) {
                return String.join(", ", selectedFields);
            }
        }

        // Priority 3: Return all columns
        return "*";
    }

    /**
     * Get connection with timeout settings
     */
    private Connection getConnectionWithTimeout() throws SQLException {
        DataSource dataSource = postgresqlJdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new SQLException("No DataSource available");
        }

        Connection conn = dataSource.getConnection();

        // Set network timeout
        conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), CONNECTION_TIMEOUT_MS);

        // Set session statement timeout
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET statement_timeout = '" + STATEMENT_TIMEOUT_SECONDS + "s'");
            stmt.execute("SET lock_timeout = '" + STATEMENT_TIMEOUT_SECONDS + "s'");
        }

        return conn;
    }

    /**
     * Extracts the full PostgreSQL error message from the exception chain
     */
    private String extractFullPostgreSQLError(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.contains("ERROR:")) {
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
                for (ApiParameterDTO param : configuredParamDTOs) {
                    String paramKey = param.getKey();
                    if (paramKey == null || paramKey.isEmpty()) continue;

                    Pattern pattern = Pattern.compile("<" + paramKey + ">(.*?)</" + paramKey + ">",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(body);

                    if (matcher.find()) {
                        String value = matcher.group(1).trim();
                        String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbColumnName, value.isEmpty() ? "" : value);
                        log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbColumnName, value);
                    }
                }
            } else {
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

    // ============ HANDLE FILE UPLOADS (ONLY IF PRESENT) ============
    private void handleFileUploads(Map<String, Object> params, Map<String, MultipartFile> fileMap, MultipartFile singleFile,
                                   Map<String, String> apiToDbParamMap) {
        if ((fileMap != null && !fileMap.isEmpty()) ||
                (singleFile != null && !singleFile.isEmpty())) {

            log.info("Processing file uploads for table operation");

            if (fileMap != null && !fileMap.isEmpty()) {
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    String paramName = entry.getKey();
                    MultipartFile file = entry.getValue();
                    try {
                        byte[] fileBytes = file.getBytes();
                        String dbParamName = apiToDbParamMap.getOrDefault(paramName.toLowerCase(), paramName.toLowerCase());
                        params.put(dbParamName, fileBytes);
                        log.info("✅ Added file to params: {} -> {} ({} bytes)", paramName, dbParamName, fileBytes.length);
                    } catch (IOException e) {
                        log.error("Failed to read file: {}", e.getMessage());
                        throw new RuntimeException("Failed to read uploaded file", e);
                    }
                }
            }

            if (singleFile != null && !singleFile.isEmpty()) {
                MultipartFile file = singleFile;
                try {
                    byte[] fileBytes = file.getBytes();
                    String dbParamName = apiToDbParamMap.getOrDefault("file", "file");
                    params.put(dbParamName, fileBytes);
                    log.info("✅ Added single file to params: {} -> {} ({} bytes)", file.getOriginalFilename(), dbParamName, fileBytes.length);
                } catch (IOException e) {
                    log.error("Failed to read file: {}", e.getMessage());
                    throw new RuntimeException("Failed to read uploaded file", e);
                }
            }
        }
    }

    public Object executeSelect(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {

        // Store captured notices only if enabled
        List<String> capturedNotices = captureNotices ? new ArrayList<>() : null;
        Map<String, Object> noticeResult = new HashMap<>();

        try (Connection conn = getConnectionWithTimeout()) {
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

            // Build SELECT clause with field selection
            String selectClause = buildSelectClause(api, params, apiToDbColumnMap);

            // Build SQL with selected fields
            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append(selectClause);
            sql.append(" FROM ");
            if (schema != null && !schema.isEmpty()) {
                sql.append(schema).append(".");
            }
            sql.append(tableName);

            List<Object> paramValues = new ArrayList<>();
            List<String> whereClauses = new ArrayList<>();

            log.info("=== TABLE SELECT DEBUG ===");
            log.info("Table: {}.{}", schema, tableName);
            log.info("Select clause: {}", selectClause);
            log.info("All incoming params: {}", params);
            log.info("Capture notices: {}", captureNotices);

            // Build a clean parameter map
            Map<String, Object> cleanParams = new HashMap<>();

            // Process XML/JSON body if present
            String body = null;
            boolean isXmlBody = false;
            boolean isJsonBody = false;

            if (params != null) {
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
                                log.info("✅ Extracted {} parameters from XML and added to cleanParams", extractedParams.size());
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
                            log.info("JSON BODY DETECTED in TableExecutor!");

                            Map<String, Object> extractedParams = parseBodyParameters(body, configuredParamDTOs, apiToDbColumnMap, false);
                            if (!extractedParams.isEmpty()) {
                                cleanParams.putAll(extractedParams);
                                log.info("✅ Extracted {} parameters from JSON and added to cleanParams", extractedParams.size());
                            }
                        }
                    }
                }
            }

            // Copy all parameters
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    // Skip the _xml/_json keys as they're already processed
                    if ("_xml".equals(key) || "_json".equals(key)) {
                        continue;
                    }

                    // Skip fields parameter as it's already used for SELECT clause
                    if ("fields".equals(key)) {
                        continue;
                    }

                    // Map the key to database column name if mapping exists
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
                            log.info("Mapped numbered param {} to position {}", key, position);
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            }

            log.info("Cleaned params after body processing: {}", cleanParams);

            // Build WHERE clause
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
                                // Skip byte arrays - they're file data
                                if (value instanceof byte[]) {
                                    log.debug("Skipping byte array parameter '{}' (file data)", paramKey);
                                    continue;
                                }
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
                                whereClauses.add("\"" + dbColumn + "\" = ?");
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
                                // Better error message showing all available params
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

            if (!whereClauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
            }

            // Handle ORDER BY if specified
            if (params != null && params.containsKey("sort")) {
                String sortColumn = params.get("sort").toString();
                String order = "ASC";
                if (params.containsKey("order")) {
                    order = params.get("order").toString().toUpperCase();
                    if (!"ASC".equals(order) && !"DESC".equals(order)) {
                        order = "ASC";
                    }
                }
                sql.append(" ORDER BY \"").append(sortColumn).append("\" ").append(order);
                log.info("Added ORDER BY: {} {}", sortColumn, order);
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
                        log.info("Page parameter found: {}", page);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid page parameter, using default: 1");
                    }
                }

                int offset = (page - 1) * pageSize;
                sql.append(" OFFSET ? LIMIT ?");
                paramValues.add(offset);
                paramValues.add(pageSize);
                log.info("Added pagination: offset={}, pageSize={}", offset, pageSize);
            }

            log.info("Final SQL: {} with {} parameters", sql.toString(), paramValues.size());

            // Execute query with optional notice capture using PreparedStatement
            List<Map<String, Object>> results;

            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                // Set parameters
                for (int i = 0; i < paramValues.size(); i++) {
                    pstmt.setObject(i + 1, paramValues.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                try (ResultSet rs = pstmt.executeQuery()) {
                    // Capture warnings if enabled
                    if (captureNotices && capturedNotices != null) {
                        SQLWarning warning = pstmt.getWarnings();
                        while (warning != null) {
                            String warningMessage = warning.getMessage();
                            if (warningMessage != null) {
                                capturedNotices.add(warningMessage);
                                log.debug("Captured warning/notice: {}", warningMessage);
                            }
                            warning = warning.getNextWarning();
                        }
                    }

                    // Process result set
                    results = new ArrayList<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        results.add(row);
                    }
                }
            }

            // Process captured notices
            if (captureNotices && capturedNotices != null && !capturedNotices.isEmpty()) {
                log.info("Captured {} NOTICE messages from select execution", capturedNotices.size());

                for (String notice : capturedNotices) {
                    log.debug("Processing notice: {}", notice);

                    if (notice != null && notice.contains("Result: ")) {
                        String jsonPart = extractJsonFromNotice(notice);
                        if (jsonPart != null) {
                            try {
                                Map<String, Object> jsonResult = objectMapper.readValue(jsonPart,
                                        new TypeReference<Map<String, Object>>() {});
                                noticeResult.putAll(jsonResult);
                                log.info("✅ Parsed JSON result from NOTICE: {}", jsonResult);
                            } catch (Exception e) {
                                log.warn("Failed to parse JSON from notice: {} - {}", jsonPart, e.getMessage());
                                noticeResult.put("notice", notice);
                            }
                        } else {
                            noticeResult.put("notice", notice);
                        }
                    } else if (notice != null) {
                        noticeResult.put("notice", notice);
                        log.info("Captured notice: {}", notice);
                    }
                }
            }

            log.info("Query returned {} rows", results.size());

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("data", results);
            if (!noticeResult.isEmpty()) {
                response.putAll(noticeResult);
            }

            return response;

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for table {}.{}", schema, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (Exception e) {
            log.error("Error executing table select: {}", e.getMessage(), e);

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

            throw new RuntimeException("Failed to execute SELECT operation: " + detailedError, e);
        }
    }

    public Object executeInsert(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {

        // Store captured notices only if enabled
        List<String> capturedNotices = captureNotices ? new ArrayList<>() : null;
        Map<String, Object> noticeResult = new HashMap<>();

        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for INSERT operation");
        }

        log.info("=== TABLE INSERT DEBUG ===");
        log.info("Table: {}.{}", schema, tableName);
        log.info("Original params: {}", params);
        log.info("Capture notices: {}", captureNotices);

        try (Connection conn = getConnectionWithTimeout()) {
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

                ApiParameterEntity paramDef = api.getParameters().stream()
                        .filter(p -> dbColumnName.equalsIgnoreCase(p.getDbColumn()) ||
                                dbColumnName.equalsIgnoreCase(p.getKey()))
                        .findFirst()
                        .orElse(null);

                Object convertedValue = null;

                if (paramDef != null) {
                    convertedValue = convertParameterValueWithDefinition(value, paramDef);
                    log.debug("Converted param {} using definition: {} -> {}", dbColumnName, value, convertedValue);
                } else {
                    if (value instanceof String) {
                        String strValue = (String) value;
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
                }
            }

            // Handle collection/array parameters - Skip byte arrays
            for (Map.Entry<String, Object> entry : typedParams.entrySet()) {
                Object value = entry.getValue();
                // Skip byte arrays - they're file data
                if (value instanceof byte[]) {
                    log.debug("Skipping byte array parameter '{}' (file data)", entry.getKey());
                    continue;
                }
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
                    columns.append("\"").append(entry.getKey()).append("\"");
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

            List<Map<String, Object>> inserted;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Set parameters
                for (int i = 0; i < paramValues.size(); i++) {
                    pstmt.setObject(i + 1, paramValues.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                // Capture warnings if enabled
                if (captureNotices && capturedNotices != null) {
                    SQLWarning warning = pstmt.getWarnings();
                    while (warning != null) {
                        String warningMessage = warning.getMessage();
                        if (warningMessage != null) {
                            capturedNotices.add(warningMessage);
                            log.debug("Captured warning/notice: {}", warningMessage);
                        }
                        warning = warning.getNextWarning();
                    }
                }

                // Execute and process result set
                inserted = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        inserted.add(row);
                    }
                }
            }

            // Process captured notices
            if (captureNotices && capturedNotices != null && !capturedNotices.isEmpty()) {
                log.info("Captured {} NOTICE messages from insert execution", capturedNotices.size());

                for (String notice : capturedNotices) {
                    log.debug("Processing notice: {}", notice);

                    if (notice != null && notice.contains("Result: ")) {
                        String jsonPart = extractJsonFromNotice(notice);
                        if (jsonPart != null) {
                            try {
                                Map<String, Object> jsonResult = objectMapper.readValue(jsonPart,
                                        new TypeReference<Map<String, Object>>() {});
                                noticeResult.putAll(jsonResult);
                                log.info("✅ Parsed JSON result from NOTICE: {}", jsonResult);
                            } catch (Exception e) {
                                log.warn("Failed to parse JSON from notice: {} - {}", jsonPart, e.getMessage());
                                noticeResult.put("notice", notice);
                            }
                        } else {
                            noticeResult.put("notice", notice);
                        }
                    } else if (notice != null) {
                        noticeResult.put("notice", notice);
                        log.info("Captured notice: {}", notice);
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", inserted.size());
            result.put("message", inserted.size() > 0 ? "Insert successful" : "No rows inserted");
            if (!noticeResult.isEmpty()) {
                result.putAll(noticeResult);
            }
            if (!inserted.isEmpty()) {
                result.put("data", inserted.get(0));
            }

            return result;

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for INSERT on {}.{}", schema, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (Exception e) {
            log.error("Error executing INSERT on {}: {}", tableName, e.getMessage(), e);

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

            String rawError = extractFullPostgreSQLError(e);
            throw new RuntimeException(rawError, e);
        }
    }

    public Object executeUpdate(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {

        // Store captured notices only if enabled
        List<String> capturedNotices = captureNotices ? new ArrayList<>() : null;
        Map<String, Object> noticeResult = new HashMap<>();

        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for UPDATE operation");
        }

        log.info("=== TABLE UPDATE DEBUG ===");
        log.info("Table: {}.{}", schema, tableName);
        log.info("Capture notices: {}", captureNotices);

        try (Connection conn = getConnectionWithTimeout()) {
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
                            log.info("✅ Extracted {} parameters from XML for UPDATE", extractedParams.size());
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
                            log.info("✅ Extracted {} parameters from JSON for UPDATE", extractedParams.size());
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

            // Handle collection/array parameters - Skip byte arrays
            for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
                Object value = entry.getValue();
                // Skip byte arrays - they're file data
                if (value instanceof byte[]) {
                    log.debug("Skipping byte array parameter '{}' (file data)", entry.getKey());
                    continue;
                }
                if (value instanceof List || (value != null && value.getClass().isArray())) {
                    Collection<?> collection = value instanceof List ?
                            (List<?>) value : Arrays.asList((Object[]) value);
                    if (!collection.isEmpty()) {
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
                    whereClause.append("\"").append(key).append("\" = ?");
                    whereValues.add(entry.getValue());
                } else {
                    if (setClause.length() > 0) {
                        setClause.append(", ");
                    }
                    setClause.append("\"").append(key).append("\" = ?");
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

            log.info("Final UPDATE SQL: {}", sql);
            log.info("UPDATE parameters: {}", allParams);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Set parameters
                for (int i = 0; i < allParams.size(); i++) {
                    pstmt.setObject(i + 1, allParams.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                // Capture warnings if enabled
                if (captureNotices && capturedNotices != null) {
                    SQLWarning warning = pstmt.getWarnings();
                    while (warning != null) {
                        String warningMessage = warning.getMessage();
                        if (warningMessage != null) {
                            capturedNotices.add(warningMessage);
                            log.debug("Captured warning/notice: {}", warningMessage);
                        }
                        warning = warning.getNextWarning();
                    }
                }

                int rowsAffected = pstmt.executeUpdate();

                // Process captured notices
                if (captureNotices && capturedNotices != null && !capturedNotices.isEmpty()) {
                    log.info("Captured {} NOTICE messages from update execution", capturedNotices.size());

                    for (String notice : capturedNotices) {
                        log.debug("Processing notice: {}", notice);

                        if (notice != null && notice.contains("Result: ")) {
                            String jsonPart = extractJsonFromNotice(notice);
                            if (jsonPart != null) {
                                try {
                                    Map<String, Object> jsonResult = objectMapper.readValue(jsonPart,
                                            new TypeReference<Map<String, Object>>() {});
                                    noticeResult.putAll(jsonResult);
                                    log.info("✅ Parsed JSON result from NOTICE: {}", jsonResult);
                                } catch (Exception e) {
                                    log.warn("Failed to parse JSON from notice: {} - {}", jsonPart, e.getMessage());
                                    noticeResult.put("notice", notice);
                                }
                            } else {
                                noticeResult.put("notice", notice);
                            }
                        } else if (notice != null) {
                            noticeResult.put("notice", notice);
                            log.info("Captured notice: {}", notice);
                        }
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("rowsAffected", rowsAffected);
                result.put("message", rowsAffected > 0 ? "Update successful" : "No rows updated");
                if (!noticeResult.isEmpty()) {
                    result.putAll(noticeResult);
                }

                return result;
            }

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for UPDATE on {}.{}", schema, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (Exception e) {
            log.error("Error executing UPDATE on {}: {}", tableName, e.getMessage(), e);

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

            String rawError = extractFullPostgreSQLError(e);
            throw new RuntimeException(rawError, e);
        }
    }

    public Object executeDelete(String tableName, String schema, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {

        // Store captured notices only if enabled
        List<String> capturedNotices = captureNotices ? new ArrayList<>() : null;
        Map<String, Object> noticeResult = new HashMap<>();

        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for DELETE operation");
        }

        log.info("=== TABLE DELETE DEBUG ===");
        log.info("Table: {}.{}", schema, tableName);
        log.info("Capture notices: {}", captureNotices);

        try (Connection conn = getConnectionWithTimeout()) {
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
                            log.info("✅ Extracted {} parameters from XML for DELETE", extractedParams.size());
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
                            log.info("✅ Extracted {} parameters from JSON for DELETE", extractedParams.size());
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

            // Handle collection/array parameters - Skip byte arrays
            for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
                Object value = entry.getValue();
                // Skip byte arrays - they're file data
                if (value instanceof byte[]) {
                    log.debug("Skipping byte array parameter '{}' (file data)", entry.getKey());
                    continue;
                }
                if (value instanceof List || (value != null && value.getClass().isArray())) {
                    Collection<?> collection = value instanceof List ?
                            (List<?>) value : Arrays.asList((Object[]) value);
                    if (!collection.isEmpty()) {
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
                whereClause.append("\"").append(entry.getKey()).append("\" = ?");
                whereValues.add(entry.getValue());
            }

            String sql = "DELETE FROM " + (schema != null && !schema.isEmpty() ? schema + "." : "") + tableName + whereClause;

            log.info("Final DELETE SQL: {}", sql);
            log.info("DELETE parameters: {}", whereValues);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Set parameters
                for (int i = 0; i < whereValues.size(); i++) {
                    pstmt.setObject(i + 1, whereValues.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                // Capture warnings if enabled
                if (captureNotices && capturedNotices != null) {
                    SQLWarning warning = pstmt.getWarnings();
                    while (warning != null) {
                        String warningMessage = warning.getMessage();
                        if (warningMessage != null) {
                            capturedNotices.add(warningMessage);
                            log.debug("Captured warning/notice: {}", warningMessage);
                        }
                        warning = warning.getNextWarning();
                    }
                }

                int rowsAffected = pstmt.executeUpdate();

                // Process captured notices
                if (captureNotices && capturedNotices != null && !capturedNotices.isEmpty()) {
                    log.info("Captured {} NOTICE messages from delete execution", capturedNotices.size());

                    for (String notice : capturedNotices) {
                        log.debug("Processing notice: {}", notice);

                        if (notice != null && notice.contains("Result: ")) {
                            String jsonPart = extractJsonFromNotice(notice);
                            if (jsonPart != null) {
                                try {
                                    Map<String, Object> jsonResult = objectMapper.readValue(jsonPart,
                                            new TypeReference<Map<String, Object>>() {});
                                    noticeResult.putAll(jsonResult);
                                    log.info("✅ Parsed JSON result from NOTICE: {}", jsonResult);
                                } catch (Exception e) {
                                    log.warn("Failed to parse JSON from notice: {} - {}", jsonPart, e.getMessage());
                                    noticeResult.put("notice", notice);
                                }
                            } else {
                                noticeResult.put("notice", notice);
                            }
                        } else if (notice != null) {
                            noticeResult.put("notice", notice);
                            log.info("Captured notice: {}", notice);
                        }
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("rowsAffected", rowsAffected);
                result.put("message", rowsAffected > 0 ? "Delete successful" : "No rows deleted");
                if (!noticeResult.isEmpty()) {
                    result.putAll(noticeResult);
                }

                return result;
            }

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for DELETE on {}.{}", schema, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (Exception e) {
            log.error("Error executing DELETE on {}: {}", tableName, e.getMessage(), e);

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

            String rawError = extractFullPostgreSQLError(e);
            throw new RuntimeException(rawError, e);
        }
    }

    private Object convertParameterValueWithDefinition(Object value, ApiParameterEntity paramDef) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }

        String stringValue = value.toString();
        boolean looksLikeDate = stringValue.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?.*");
        boolean looksLikeUUID = stringValue.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        String oracleType = paramDef.getOracleType();
        if (oracleType == null) {
            oracleType = paramDef.getApiType();
        }

        if (oracleType == null && looksLikeUUID) {
            try {
                log.info("Auto-converting param {} as UUID: {}", paramDef.getKey(), stringValue);
                return java.util.UUID.fromString(stringValue);
            } catch (IllegalArgumentException e) {
                log.debug("Failed to parse as UUID: {}", e.getMessage());
            }
        }

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
                if (looksLikeDate) {
                    log.info("Auto-converting param {} as date (type {} doesn't match): {}", paramDef.getKey(), type, stringValue);
                    return convertToTimestamp(stringValue);
                }
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

    /**
     * Convert various datetime formats to java.sql.Timestamp
     */
    private Object convertToTimestamp(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            dateStr = dateStr.trim();

            try {
                return java.sql.Timestamp.valueOf(dateStr);
            } catch (IllegalArgumentException e) {
                // Not in standard format, continue
            }

            if (dateStr.contains("T")) {
                String dateTimePart = dateStr.split("[+-]")[0];
                dateTimePart = dateTimePart.replace("Z", "").replace("T", " ");
                if (!dateTimePart.contains(".")) dateTimePart += ".0";
                try {
                    return java.sql.Timestamp.valueOf(dateTimePart);
                } catch (IllegalArgumentException e) {
                    log.debug("Failed to parse as JDBC format after conversion: {}", dateTimePart);
                }
            }

            try {
                String tempStr = dateStr.endsWith("Z") ? dateStr.substring(0, dateStr.length() - 1) + "+00:00" : dateStr;
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(tempStr);
                return java.sql.Timestamp.valueOf(odt.toLocalDateTime());
            } catch (Exception e) {
                log.debug("Failed to parse as OffsetDateTime: {}", e.getMessage());
            }

            try {
                String noZoneStr = dateStr.split("[+-]")[0].replace("T", " ");
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(noZoneStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                return java.sql.Timestamp.valueOf(ldt);
            } catch (Exception e) {
                log.debug("Failed to parse as LocalDateTime: {}", e.getMessage());
            }

            try {
                return java.sql.Timestamp.valueOf(dateStr + " 00:00:00.000");
            } catch (Exception e) {
                // Still can't parse
            }

            log.warn("Unable to parse date string: {}, keeping as string", dateStr);
            return dateStr;

        } catch (Exception e) {
            log.warn("Error converting date string '{}': {}", dateStr, e.getMessage());
            return dateStr;
        }
    }
}