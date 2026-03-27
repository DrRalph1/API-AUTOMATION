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

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
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

    // Timeout constants
    private static final int STATEMENT_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_TIMEOUT_MS = 30000;

    public OracleTableExecutorUtil(
            OracleParameterValidatorUtil parameterValidator) {
        this.parameterValidator = parameterValidator;
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
                if (!seenMessages.contains(message)) {
                    seenMessages.add(message);

                    if (message.contains("ORA-")) {
                        return message;
                    }

                    if (fullError.length() > 0) {
                        fullError.append(" -> ");
                    }
                    fullError.append(message);
                }
            }
            cause = cause.getCause();
        }

        return fullError.length() > 0 ? fullError.toString() : e.getMessage();
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

    public Object executeSelect(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        try (Connection conn = getConnectionWithTimeout()) {
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

            // Build a clean parameter map
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
                        log.info("XML BODY DETECTED in TableExecutor!");

                        Map<String, Object> extractedParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
                        if (!extractedParams.isEmpty()) {
                            cleanParams.putAll(extractedParams);
                            log.info("✅ Extracted {} parameters from XML", extractedParams.size());
                        }
                    }
                }
            }

            // Copy all parameters
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    if ("_xml".equals(key)) {
                        continue;
                    }

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
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            }

            log.info("Cleaned params after processing: {}", cleanParams);

            // Build WHERE clause
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                for (ApiParameterEntity configuredParam : api.getParameters()) {
                    String paramKey = configuredParam.getKey();
                    String dbColumn = configuredParam.getDbColumn();
                    String paramType = configuredParam.getParameterType();

                    if (paramType == null) {
                        paramType = "query";
                    }

                    if (dbColumn == null || dbColumn.isEmpty()) {
                        continue;
                    }

                    boolean isValidForFiltering = "query".equals(paramType) ||
                            "path".equals(paramType) ||
                            "body".equals(paramType);

                    if (isValidForFiltering) {
                        Object value = null;

                        if (cleanParams.containsKey(paramKey)) {
                            value = cleanParams.get(paramKey);
                        }

                        if (value == null) {
                            for (Map.Entry<String, Object> entry : cleanParams.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(paramKey)) {
                                    value = entry.getValue();
                                    break;
                                }
                            }
                        }

                        if (value == null && "path".equals(paramType)) {
                            Integer position = configuredParam.getPosition();
                            if (position != null) {
                                String positionKey = "position_" + position;
                                if (cleanParams.containsKey(positionKey)) {
                                    value = cleanParams.get(positionKey);
                                }
                            }
                        }

                        if (value != null) {
                            if (value instanceof List || value.getClass().isArray()) {
                                Collection<?> collection = value instanceof List ?
                                        (List<?>) value : Arrays.asList((Object[]) value);
                                if (!collection.isEmpty()) {
                                    value = collection.iterator().next();
                                } else {
                                    value = null;
                                }
                            }

                            if (value != null && !value.toString().trim().isEmpty()) {
                                whereClauses.add(dbColumn + " = ?");
                                paramValues.add(value);
                                log.info("ADDED FILTER: {} = ? with value: {}", dbColumn, value);
                            } else if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                                throw new ValidationException(
                                        String.format("Required parameter '%s' cannot be empty", paramKey)
                                );
                            }
                        } else if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                            throw new ValidationException(
                                    String.format("Required parameter '%s' is missing. Available params: %s",
                                            paramKey, cleanParams.keySet())
                            );
                        }
                    }
                }
            }

            if (!whereClauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
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
                    } catch (NumberFormatException e) {
                        log.warn("Invalid page parameter, using default: 1");
                    }
                }

                int offset = (page - 1) * pageSize;
                sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
                paramValues.add(offset);
                paramValues.add(pageSize);
            }

            String cleanedSql = cleanSqlStatement(sql.toString());
            log.info("Final SQL: {}", cleanedSql);
            log.info("SQL parameters: {}", paramValues.size());

            // Execute with manual PreparedStatement
            List<Map<String, Object>> results;
            try (PreparedStatement pstmt = conn.prepareStatement(cleanedSql)) {
                // Set parameters
                for (int i = 0; i < paramValues.size(); i++) {
                    pstmt.setObject(i + 1, paramValues.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                // Execute and process result set
                results = new ArrayList<>();
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
                        results.add(row);
                    }
                }
            }

            log.info("Query returned {} rows", results.size());
            return results;

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for table {}.{}", owner, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
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
                        log.info("XML BODY DETECTED in INSERT operation!");

                        Map<String, Object> extractedParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbColumnMap);
                        if (!extractedParams.isEmpty()) {
                            processedParams.putAll(extractedParams);
                            log.info("✅ Extracted {} parameters from XML", extractedParams.size());
                        }
                    }
                }
            }

            // Process all parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if ("_xml".equals(key) && isXmlBody) {
                    continue;
                }

                if (processedParams.containsKey(key) || processedParams.containsKey(key.toUpperCase())) {
                    continue;
                }

                String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key.toUpperCase());

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
                }
            }

            // Add default values from API parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                for (ApiParameterEntity apiParam : api.getParameters()) {
                    String paramKey = apiParam.getKey();
                    String dbColumnName = apiToDbColumnMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());

                    if (!processedParams.containsKey(dbColumnName) && apiParam.getDefaultValue() != null) {
                        processedParams.put(dbColumnName, apiParam.getDefaultValue());
                        log.info("Using default value for parameter '{}': {}", paramKey, apiParam.getDefaultValue());
                    }
                }
            }

            log.info("Final processed params for INSERT: {}", processedParams.keySet());

            if (processedParams.isEmpty()) {
                log.error("No parameters to insert! Original params: {}", params.keySet());
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
            }

            String sql = "INSERT INTO " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                    " (" + columns + ") VALUES (" + values + ")";

            log.info("Final INSERT SQL: {}", sql);
            log.info("INSERT parameters: {}", paramValues);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Set parameters
                for (int i = 0; i < paramValues.size(); i++) {
                    pstmt.setObject(i + 1, paramValues.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                int rowsAffected = pstmt.executeUpdate();

                Map<String, Object> result = new HashMap<>();
                result.put("rowsAffected", rowsAffected);
                result.put("message", rowsAffected > 0 ? "Insert successful" : "No rows inserted");

                // Try to fetch the inserted record
                if (rowsAffected > 0 && api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                    String pkColumn = api.getResponseMappings().stream()
                            .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                            .map(ApiResponseMappingEntity::getDbColumn)
                            .findFirst()
                            .orElse(null);

                    if (pkColumn != null && processedParams.containsKey(pkColumn)) {
                        String selectSql = "SELECT * FROM " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                                " WHERE " + pkColumn + " = ?";
                        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                            selectStmt.setObject(1, processedParams.get(pkColumn));
                            selectStmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                            try (ResultSet rs = selectStmt.executeQuery()) {
                                if (rs.next()) {
                                    ResultSetMetaData metaData = rs.getMetaData();
                                    int columnCount = metaData.getColumnCount();
                                    Map<String, Object> row = new HashMap<>();
                                    for (int i = 1; i <= columnCount; i++) {
                                        row.put(metaData.getColumnName(i), rs.getObject(i));
                                    }
                                    result.put("data", row);
                                }
                            }
                        }
                    }
                }

                return result;
            }

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for INSERT on {}.{}", owner, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (Exception e) {
            log.error("Error executing INSERT on {}: {}", tableName, e.getMessage(), e);

            String detailedError = extractFullOracleError(e);

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
                        apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toUpperCase());
                    }
                }
            }

            // Process XML body if present
            Map<String, Object> processedParams = new HashMap<>();

            if (params.containsKey("_xml")) {
                Object xmlObj = params.get("_xml");
                if (xmlObj instanceof String) {
                    String xmlString = (String) xmlObj;
                    if (xmlString.trim().startsWith("<")) {
                        log.info("XML BODY DETECTED in UPDATE operation!");

                        Map<String, Object> extractedParams = parseXmlParameters(xmlString, configuredParamDTOs, apiToDbColumnMap);
                        if (!extractedParams.isEmpty()) {
                            processedParams.putAll(extractedParams);
                            log.info("✅ Extracted {} parameters from XML", extractedParams.size());
                        }
                    }
                }
            }

            // Copy all parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if ("_xml".equals(key)) {
                    continue;
                }

                String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
                processedParams.put(dbColumnName, entry.getValue());
            }

            // Handle collection/array parameters
            for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof List || (value != null && value.getClass().isArray())) {
                    Collection<?> collection = value instanceof List ?
                            (List<?>) value : Arrays.asList((Object[]) value);
                    if (!collection.isEmpty()) {
                        processedParams.put(entry.getKey(), collection.iterator().next());
                        log.info("Converted collection parameter '{}' to single value", entry.getKey());
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

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Set parameters
                for (int i = 0; i < allParams.size(); i++) {
                    pstmt.setObject(i + 1, allParams.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                int rowsAffected = pstmt.executeUpdate();

                Map<String, Object> result = new HashMap<>();
                result.put("rowsAffected", rowsAffected);
                result.put("message", rowsAffected > 0 ? "Update successful" : "No rows updated");

                return result;
            }

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for UPDATE on {}.{}", owner, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (Exception e) {
            log.error("Error executing UPDATE on {}: {}", tableName, e.getMessage(), e);

            String detailedError = extractFullOracleError(e);

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
                        apiToDbColumnMap.put(param.getKey().toLowerCase(), dbColumnName.toUpperCase());
                    }
                }
            }

            // Process XML body if present
            Map<String, Object> processedParams = new HashMap<>();

            if (params.containsKey("_xml")) {
                Object xmlObj = params.get("_xml");
                if (xmlObj instanceof String) {
                    String xmlString = (String) xmlObj;
                    if (xmlString.trim().startsWith("<")) {
                        log.info("XML BODY DETECTED in DELETE operation!");

                        Map<String, Object> extractedParams = parseXmlParameters(xmlString, configuredParamDTOs, apiToDbColumnMap);
                        if (!extractedParams.isEmpty()) {
                            processedParams.putAll(extractedParams);
                            log.info("✅ Extracted {} parameters from XML", extractedParams.size());
                        }
                    }
                }
            }

            // Copy all parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if ("_xml".equals(key)) {
                    continue;
                }

                String dbColumnName = apiToDbColumnMap.getOrDefault(key.toLowerCase(), key);
                processedParams.put(dbColumnName, entry.getValue());
            }

            // Handle collection/array parameters
            for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof List || (value != null && value.getClass().isArray())) {
                    Collection<?> collection = value instanceof List ?
                            (List<?>) value : Arrays.asList((Object[]) value);
                    if (!collection.isEmpty()) {
                        processedParams.put(entry.getKey(), collection.iterator().next());
                        log.info("Converted collection parameter '{}' to single value", entry.getKey());
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

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Set parameters
                for (int i = 0; i < whereValues.size(); i++) {
                    pstmt.setObject(i + 1, whereValues.get(i));
                }

                // Set statement timeout
                pstmt.setQueryTimeout(STATEMENT_TIMEOUT_SECONDS);

                int rowsAffected = pstmt.executeUpdate();

                Map<String, Object> result = new HashMap<>();
                result.put("rowsAffected", rowsAffected);
                result.put("message", rowsAffected > 0 ? "Delete successful" : "No rows deleted");

                return result;
            }

        } catch (SQLTimeoutException e) {
            log.error("Database operation timed out for DELETE on {}.{}", owner, tableName, e);
            throw new RuntimeException("Database operation timed out after " + STATEMENT_TIMEOUT_SECONDS + " seconds", e);
        } catch (Exception e) {
            log.error("Error executing DELETE on {}: {}", tableName, e.getMessage(), e);

            String detailedError = extractFullOracleError(e);

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