package com.usg.autoAPIGenerator.utils.apiEngine.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.utils.apiEngine.generator.CustomQueryParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomQueryExecutionHelper {

    private final CustomQueryParserUtil queryParserUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Pattern to remove SQL single-line comments (-- comment)
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("--[^\n\r]*");
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    // Date formats to try for parsing
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,  // 1995-06-15
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy")
    );

    // SOAP Action to SQL operation mapping
    private static final Map<String, String> SOAP_ACTION_TO_SQL_TYPE = Map.ofEntries(
            Map.entry("SELECT", "SELECT"),
            Map.entry("SELECT_ONE", "SELECT"),
            Map.entry("SEARCH", "SELECT"),
            Map.entry("COUNT", "SELECT"),
            Map.entry("AGGREGATE", "SELECT"),
            Map.entry("EXPORT", "SELECT"),
            Map.entry("PAGINATE", "SELECT"),
            Map.entry("GET", "SELECT"),
            Map.entry("QUERY", "SELECT"),
            Map.entry("ANALYZE", "SELECT"),
            Map.entry("EXPLAIN", "SELECT"),

            Map.entry("INSERT", "INSERT"),
            Map.entry("CREATE", "INSERT"),
            Map.entry("BULK_INSERT", "INSERT"),

            Map.entry("UPDATE", "UPDATE"),
            Map.entry("MODIFY", "UPDATE"),
            Map.entry("UPSERT", "UPDATE"),
            Map.entry("BULK_UPDATE", "UPDATE"),

            Map.entry("DELETE", "DELETE"),
            Map.entry("REMOVE", "DELETE"),
            Map.entry("PURGE", "DELETE"),

            Map.entry("EXECUTE", "CALL"),
            Map.entry("VALIDATE", "CALL"),
            Map.entry("DRY_RUN", "CALL"),
            Map.entry("SCHEDULE", "CALL"),
            Map.entry("ASYNC", "CALL"),
            Map.entry("DEBUG", "CALL"),
            Map.entry("PROFILE", "CALL"),
            Map.entry("EXECUTE_PROCEDURE", "CALL"),
            Map.entry("EXECUTE_FUNCTION", "CALL"),
            Map.entry("GET_STATE", "CALL"),
            Map.entry("RESET", "CALL"),

            Map.entry("PROCESS", "CALL"),
            Map.entry("EXISTS", "SELECT")
    );

    // GraphQL Operation Type to SQL operation mapping
    private static final Map<String, String> GRAPHQL_OP_TYPE_TO_SQL = Map.ofEntries(
            // Query operations (read-only)
            Map.entry("query", "SELECT"),
            Map.entry("get", "SELECT"),
            Map.entry("find", "SELECT"),
            Map.entry("list", "SELECT"),
            Map.entry("search", "SELECT"),
            Map.entry("fetch", "SELECT"),
            Map.entry("lookup", "SELECT"),
            Map.entry("aggregate", "SELECT"),
            Map.entry("count", "SELECT"),
            Map.entry("exists", "SELECT"),

            // Mutation operations (write operations)
            Map.entry("mutation", "CALL"),
            Map.entry("create", "INSERT"),
            Map.entry("add", "INSERT"),
            Map.entry("insert", "INSERT"),
            Map.entry("save", "INSERT"),
            Map.entry("register", "INSERT"),

            Map.entry("update", "UPDATE"),
            Map.entry("modify", "UPDATE"),
            Map.entry("edit", "UPDATE"),
            Map.entry("change", "UPDATE"),
            Map.entry("set", "UPDATE"),
            Map.entry("upsert", "UPDATE"),

            Map.entry("delete", "DELETE"),
            Map.entry("remove", "DELETE"),
            Map.entry("purge", "DELETE"),
            Map.entry("destroy", "DELETE"),

            // Subscription operations (real-time)
            Map.entry("subscription", "SELECT"),
            Map.entry("subscribe", "SELECT"),
            Map.entry("watch", "SELECT"),
            Map.entry("listen", "SELECT"),
            Map.entry("on", "SELECT"),

            // Procedure/Function calls
            Map.entry("execute", "CALL"),
            Map.entry("run", "CALL"),
            Map.entry("process", "CALL"),
            Map.entry("validate", "CALL"),
            Map.entry("calculate", "CALL"),
            Map.entry("compute", "CALL")
    );

    /**
     * Execute a custom SQL statement
     * Handles SELECT, INSERT, UPDATE, DELETE, DDL, CALL, EXECUTE, and PLSQL
     *
     * For SOAP APIs: Uses SOAP Action to determine operation
     * For GraphQL APIs: Uses GraphQL Operation Type to determine operation
     * For REST APIs: Parses the SQL to determine operation
     */
    public Object executeCustomQuery(GeneratedApiEntity api,
                                     ApiSourceObjectDTO sourceObject,
                                     ExecuteApiRequestDTO executeRequest,
                                     List<ApiParameterDTO> configuredParamDTOs,
                                     JdbcTemplate jdbcTemplate) {

        String sql = sourceObject.getCustomSelectStatement();

        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("No SQL statement provided for custom query API");
        }

        log.info("Executing custom query for API: {}", api.getApiCode());
        log.debug("Original Query: {}", sql);

        try {
            String cleanedQuery = cleanSqlQuery(sql);
            log.debug("Cleaned Query: {}", cleanedQuery);

            // Determine query type based on protocol type
            String queryType = determineQueryType(api, cleanedQuery);
            log.info("Detected Query Type: {}", queryType);

            if ("UNKNOWN".equals(queryType)) {
                throw new RuntimeException("Unsupported or invalid SQL statement");
            }

            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            // Get column types dynamically based on query type
            Map<String, String> columnTypes;
            if ("SELECT".equals(queryType)) {
                columnTypes = queryParserUtil.getColumnTypeMap(cleanedQuery, sourceObject.getDatabaseType());
            } else {
                // For INSERT/UPDATE/DELETE, use the DML-specific method
                columnTypes = queryParserUtil.getColumnTypeMapForDML(cleanedQuery, sourceObject.getDatabaseType());
            }

            log.debug("Column types detected: {}", columnTypes);

            // Build parameter source with dynamic type conversion
            MapSqlParameterSource parameterSource = buildParameterSource(executeRequest, configuredParamDTOs, columnTypes);

            switch (queryType) {

                case "SELECT":
                    List<Map<String, Object>> results = namedTemplate.queryForList(cleanedQuery, parameterSource);

                    for (Map<String, Object> row : results) {
                        parseJsonFields(row);
                    }

                    if (Boolean.TRUE.equals(sourceObject.getEnablePagination())) {
                        return applyPagination(results, executeRequest, sourceObject.getDefaultPageSize());
                    }

                    return results;

                case "INSERT":
                case "UPDATE":
                case "DELETE":
                    int affectedRows = namedTemplate.update(cleanedQuery, parameterSource);
                    return Map.of(
                            "success", true,
                            "queryType", queryType,
                            "affectedRows", affectedRows
                    );

                case "DDL":
                case "CALL":
                case "EXECUTE":
                case "PLSQL":
                    namedTemplate.getJdbcTemplate().execute(cleanedQuery);
                    return Map.of(
                            "success", true,
                            "queryType", queryType,
                            "message", "Statement executed successfully"
                    );

                default:
                    throw new RuntimeException("Unhandled query type: " + queryType);
            }

        } catch (Exception e) {
            log.error("Error executing custom query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute SQL statement: " + e.getMessage(), e);
        }
    }

    /**
     * Determine query type based on the API's protocol:
     * - SOAP: Uses SOAP Action mapping
     * - GraphQL: Uses GraphQL Operation Type mapping
     * - REST: Parses the SQL query
     *
     * @param api The API entity containing GraphQL/SOAP configuration
     * @param query The SQL query
     * @return The query type (SELECT, INSERT, UPDATE, DELETE, CALL, etc.)
     */
    private String determineQueryType(GeneratedApiEntity api, String query) {
        // Check protocol type from API
        String protocolType = null;
        if (api != null) {
            protocolType = api.getProtocolType();
        }

        // For SOAP APIs: Use SOAP Action ONLY
        if ("soap".equalsIgnoreCase(protocolType) && api != null && api.getSoapConfig() != null) {
            String soapAction = api.getSoapConfig().getSoapAction();

            if (soapAction != null && !soapAction.trim().isEmpty()) {
                log.info("SOAP API detected - using SOAP Action to determine operation: {}", soapAction);

                // Try exact match first
                String mappedType = SOAP_ACTION_TO_SQL_TYPE.get(soapAction.toUpperCase());
                if (mappedType != null) {
                    log.info("SOAP Action '{}' mapped to SQL operation: {}", soapAction, mappedType);
                    return mappedType;
                }

                // Try to match by prefix (e.g., "SELECT_USER" -> "SELECT")
                for (Map.Entry<String, String> entry : SOAP_ACTION_TO_SQL_TYPE.entrySet()) {
                    if (soapAction.toUpperCase().startsWith(entry.getKey())) {
                        log.info("SOAP Action '{}' matched prefix '{}' -> SQL operation: {}",
                                soapAction, entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }

                // Try to match by contains (e.g., "GET_USER_BY_ID" contains "GET" -> "SELECT")
                for (Map.Entry<String, String> entry : SOAP_ACTION_TO_SQL_TYPE.entrySet()) {
                    if (soapAction.toUpperCase().contains(entry.getKey())) {
                        log.info("SOAP Action '{}' contains '{}' -> SQL operation: {}",
                                soapAction, entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }

                log.warn("SOAP Action '{}' not recognized, defaulting to SELECT", soapAction);
                return "SELECT";
            }

            // Check if it's a procedure/function execution based on SOAP binding style
            if (api.getSoapConfig().getBindingStyle() != null) {
                String bindingStyle = api.getSoapConfig().getBindingStyle();
                if ("rpc".equalsIgnoreCase(bindingStyle)) {
                    log.info("RPC binding style detected, treating as procedure/function execution");
                    return "CALL";
                }
            }

            log.warn("No SOAP Action defined, defaulting to SELECT");
            return "SELECT";
        }

        // For GraphQL APIs: Use GraphQL Operation Type ONLY
        if ("graphql".equalsIgnoreCase(protocolType) && api != null && api.getGraphqlConfig() != null) {
            String operationType = api.getGraphqlConfig().getOperationType();
            String operationName = api.getGraphqlConfig().getOperationName();

            if (operationType != null && !operationType.trim().isEmpty()) {
                log.info("GraphQL API detected - using Operation Type to determine operation: {}", operationType);

                // Try exact match first
                String mappedType = GRAPHQL_OP_TYPE_TO_SQL.get(operationType.toLowerCase());
                if (mappedType != null) {
                    log.info("GraphQL Operation Type '{}' mapped to SQL operation: {}", operationType, mappedType);
                    return refineGraphQLOperation(mappedType, operationName);
                }

                log.warn("GraphQL Operation Type '{}' not recognized, defaulting to SELECT", operationType);
                return "SELECT";
            }

            log.warn("No GraphQL Operation Type defined, defaulting to SELECT");
            return "SELECT";
        }

        // For REST APIs (or any other protocol): Parse the SQL query directly
        log.info("REST API or unknown protocol - parsing SQL to determine operation");
        return getQueryTypeFromSql(query);
    }

    /**
     * Refine GraphQL operation type based on operation name
     *
     * @param baseType The base SQL operation type from operation type
     * @param operationName The GraphQL operation name
     * @return The refined SQL operation type
     */
    private String refineGraphQLOperation(String baseType, String operationName) {
        // For mutation operations, we need to determine if it's INSERT, UPDATE, or DELETE
        if ("CALL".equals(baseType) && operationName != null) {
            String refined = determineFromGraphQLOperationName(operationName);
            if (refined != null) {
                log.info("Refined GraphQL mutation '{}' from 'CALL' to '{}' based on operation name: {}",
                        operationName, refined, operationName);
                return refined;
            }
        }
        return baseType;
    }

    /**
     * Determine SQL operation type from GraphQL operation name patterns
     *
     * @param operationName The GraphQL operation name
     * @return The SQL operation type or null if not determinable
     */
    private String determineFromGraphQLOperationName(String operationName) {
        if (operationName == null || operationName.trim().isEmpty()) {
            return null;
        }

        String lowerName = operationName.toLowerCase();

        // Check for mutation patterns first (these are more specific)
        if (lowerName.startsWith("create") || lowerName.startsWith("add") ||
                lowerName.startsWith("insert") || lowerName.startsWith("save") ||
                lowerName.startsWith("register")) {
            return "INSERT";
        }

        if (lowerName.startsWith("update") || lowerName.startsWith("modify") ||
                lowerName.startsWith("edit") || lowerName.startsWith("change") ||
                lowerName.startsWith("set") || lowerName.startsWith("upsert")) {
            return "UPDATE";
        }

        if (lowerName.startsWith("delete") || lowerName.startsWith("remove") ||
                lowerName.startsWith("purge") || lowerName.startsWith("destroy")) {
            return "DELETE";
        }

        if (lowerName.startsWith("execute") || lowerName.startsWith("run") ||
                lowerName.startsWith("process") || lowerName.startsWith("validate") ||
                lowerName.startsWith("calculate") || lowerName.startsWith("compute")) {
            return "CALL";
        }

        // Check for query patterns (read-only)
        if (lowerName.startsWith("get") || lowerName.startsWith("find") ||
                lowerName.startsWith("list") || lowerName.startsWith("search") ||
                lowerName.startsWith("fetch") || lowerName.startsWith("lookup") ||
                lowerName.startsWith("query") || lowerName.startsWith("aggregate") ||
                lowerName.startsWith("count") || lowerName.startsWith("exists")) {
            return "SELECT";
        }

        // Check content of operation name for hints
        if (lowerName.contains("find") || lowerName.contains("get") ||
                lowerName.contains("list") || lowerName.contains("search")) {
            return "SELECT";
        }

        if (lowerName.contains("insert") || lowerName.contains("create") ||
                lowerName.contains("add")) {
            return "INSERT";
        }

        if (lowerName.contains("update") || lowerName.contains("modify")) {
            return "UPDATE";
        }

        if (lowerName.contains("delete") || lowerName.contains("remove")) {
            return "DELETE";
        }

        return null;
    }

    /**
     * Determine SQL query type by parsing the SQL statement (for REST APIs)
     *
     * @param query The SQL query
     * @return The query type
     */
    private String getQueryTypeFromSql(String query) {
        if (query == null) return "UNKNOWN";

        String trimmed = query.trim().toUpperCase();

        // Check for SELECT (including WITH clause common in PostgreSQL)
        if (trimmed.matches("^SELECT\\b.*") || trimmed.matches("^WITH\\b.*SELECT\\b.*")) {
            return "SELECT";
        }

        if (trimmed.matches("^INSERT\\b.*")) return "INSERT";
        if (trimmed.matches("^UPDATE\\b.*")) return "UPDATE";
        if (trimmed.matches("^DELETE\\b.*")) return "DELETE";
        if (trimmed.matches("^(CREATE|ALTER|DROP|TRUNCATE)\\b.*")) return "DDL";

        // Check for CALL (Oracle/PostgreSQL procedures)
        if (trimmed.matches("^CALL\\b.*")) return "CALL";
        if (trimmed.matches("^EXEC(UTE)?\\b.*")) return "EXECUTE";

        // Check for BEGIN (PL/SQL or PL/pgSQL anonymous block)
        if (trimmed.matches("^(BEGIN|DECLARE)\\b.*")) return "PLSQL";

        // Check for DO (PostgreSQL anonymous block)
        if (trimmed.matches("^DO\\b.*")) return "PLSQL";

        return "UNKNOWN";
    }

    /**
     * Parse JSON string fields into objects
     */
    private void parseJsonFields(Map<String, Object> row) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                String stringValue = ((String) value).trim();
                if ((stringValue.startsWith("[") && stringValue.endsWith("]")) ||
                        (stringValue.startsWith("{") && stringValue.endsWith("}"))) {
                    try {
                        Object parsedJson = objectMapper.readValue(stringValue, Object.class);
                        entry.setValue(parsedJson);
                    } catch (Exception e) {
                        log.debug("Field {} is not valid JSON: {}", entry.getKey(), e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Clean SQL query by removing comments and trailing semicolons
     */
    private String cleanSqlQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) return sql;

        String cleaned = sql;
        cleaned = SQL_COMMENT_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = MULTI_LINE_COMMENT_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned.trim();
        if (cleaned.endsWith(";")) cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * Validate and format IP address for PostgreSQL inet type
     */
    private String validateAndFormatInet(String ipStr) {
        if (ipStr == null || ipStr.trim().isEmpty()) {
            return null;
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipStr.trim());
            String validatedIp = inetAddress.getHostAddress();
            log.debug("Validated IP address: {} -> {}", ipStr, validatedIp);
            return validatedIp;
        } catch (UnknownHostException e) {
            log.warn("Invalid IP address format: {}", ipStr);
            throw new IllegalArgumentException("Invalid IP address format: " + ipStr + ". Expected valid IPv4 or IPv6 address.");
        }
    }

    /**
     * Convert string to Date with multiple format attempts
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate localDate = LocalDate.parse(dateStr.trim(), formatter);
                return Date.valueOf(localDate);
            } catch (Exception e) {
                // Continue to next formatter
            }
        }

        try {
            return Date.valueOf(dateStr.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to parse date: {}", dateStr);
            throw new IllegalArgumentException("Invalid date format: " + dateStr + ". Expected format: yyyy-MM-dd");
        }
    }

    /**
     * Convert string to Timestamp with multiple format attempts
     */
    private Timestamp parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            return null;
        }

        try {
            return Timestamp.valueOf(timestampStr.trim());
        } catch (IllegalArgumentException e) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(timestampStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return Timestamp.valueOf(localDateTime);
            } catch (Exception ex) {
                log.warn("Failed to parse timestamp: {}", timestampStr);
                throw new IllegalArgumentException("Invalid timestamp format: " + timestampStr);
            }
        }
    }

    /**
     * Build parameter source from request with dynamic type conversion
     */
    private MapSqlParameterSource buildParameterSource(ExecuteApiRequestDTO executeRequest,
                                                       List<ApiParameterDTO> configuredParamDTOs,
                                                       Map<String, String> columnTypes) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        Map<String, Object> allParams = new java.util.HashMap<>();

        if (executeRequest.getQueryParams() != null) allParams.putAll(executeRequest.getQueryParams());
        if (executeRequest.getPathParams() != null) allParams.putAll(executeRequest.getPathParams());
        if (executeRequest.getBody() instanceof Map) allParams.putAll((Map<String, Object>) executeRequest.getBody());

        log.debug("Building parameter source with column types: {}", columnTypes);

        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                parameterSource.addValue(key, null);
                continue;
            }

            String columnType = columnTypes.getOrDefault(key.toLowerCase(), "VARCHAR").toUpperCase();
            log.debug("Processing parameter '{}' with value '{}' as type: {}", key, value, columnType);

            try {
                switch (columnType) {
                    case "DATE":
                    case "DATE_":
                        if (value instanceof String) {
                            Date dateValue = parseDate((String) value);
                            parameterSource.addValue(key, dateValue, Types.DATE);
                            log.debug("Converted '{}' from String to Date: {}", value, dateValue);
                        } else if (value instanceof Date) {
                            parameterSource.addValue(key, value, Types.DATE);
                        } else if (value instanceof LocalDate) {
                            parameterSource.addValue(key, Date.valueOf((LocalDate) value), Types.DATE);
                        } else if (value instanceof java.util.Date) {
                            parameterSource.addValue(key, new Date(((java.util.Date) value).getTime()), Types.DATE);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "TIMESTAMP":
                    case "TIMESTAMPTZ":
                    case "TIMESTAMP WITH TIME ZONE":
                    case "TIMESTAMP WITHOUT TIME ZONE":
                        if (value instanceof String) {
                            Timestamp tsValue = parseTimestamp((String) value);
                            parameterSource.addValue(key, tsValue, Types.TIMESTAMP);
                            log.debug("Converted '{}' from String to Timestamp: {}", value, tsValue);
                        } else if (value instanceof Timestamp) {
                            parameterSource.addValue(key, value, Types.TIMESTAMP);
                        } else if (value instanceof LocalDateTime) {
                            parameterSource.addValue(key, Timestamp.valueOf((LocalDateTime) value), Types.TIMESTAMP);
                        } else if (value instanceof java.util.Date) {
                            parameterSource.addValue(key, new Timestamp(((java.util.Date) value).getTime()), Types.TIMESTAMP);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "TIME":
                        if (value instanceof String) {
                            try {
                                java.sql.Time timeValue = java.sql.Time.valueOf((String) value);
                                parameterSource.addValue(key, timeValue, Types.TIME);
                            } catch (IllegalArgumentException e) {
                                log.warn("Failed to parse time: {}", value);
                                parameterSource.addValue(key, value);
                            }
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "INET":
                        if (value instanceof String) {
                            String validatedIp = validateAndFormatInet((String) value);
                            parameterSource.addValue(key, validatedIp, Types.OTHER);
                            log.debug("Validated and set '{}' as inet value: {}", key, validatedIp);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "CIDR":
                        if (value instanceof String) {
                            String cidrStr = ((String) value).trim();
                            if (!cidrStr.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}/[0-9]{1,2}$")) {
                                log.warn("Invalid CIDR format: {}", cidrStr);
                            }
                            parameterSource.addValue(key, cidrStr, Types.OTHER);
                            log.debug("Set '{}' as cidr value: {}", key, cidrStr);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "MACADDR":
                        if (value instanceof String) {
                            String macStr = ((String) value).trim();
                            if (!macStr.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
                                log.warn("Invalid MAC address format: {}", macStr);
                            }
                            parameterSource.addValue(key, macStr, Types.OTHER);
                            log.debug("Set '{}' as macaddr value: {}", key, macStr);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "UUID":
                        if (value instanceof String) {
                            try {
                                java.util.UUID uuidValue = java.util.UUID.fromString((String) value);
                                parameterSource.addValue(key, uuidValue, Types.OTHER);
                                log.debug("Converted '{}' from String to UUID: {}", value, uuidValue);
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid UUID format: {}", value);
                                parameterSource.addValue(key, value);
                            }
                        } else if (value instanceof java.util.UUID) {
                            parameterSource.addValue(key, value, Types.OTHER);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "JSON":
                    case "JSONB":
                        if (value instanceof String) {
                            try {
                                objectMapper.readTree((String) value);
                                parameterSource.addValue(key, value, Types.OTHER);
                                log.debug("Validated JSON for '{}'", key);
                            } catch (Exception e) {
                                log.warn("Invalid JSON format for {}: {}", key, e.getMessage());
                                parameterSource.addValue(key, value);
                            }
                        } else {
                            try {
                                String jsonValue = objectMapper.writeValueAsString(value);
                                parameterSource.addValue(key, jsonValue, Types.OTHER);
                                log.debug("Converted '{}' from Object to JSON string", key);
                            } catch (Exception e) {
                                log.warn("Failed to convert to JSON: {}", e.getMessage());
                                parameterSource.addValue(key, value);
                            }
                        }
                        break;

                    case "BOOLEAN":
                    case "BOOL":
                        if (value instanceof String) {
                            String strValue = ((String) value).toLowerCase();
                            boolean boolValue = "true".equals(strValue) || "1".equals(strValue) || "yes".equals(strValue);
                            parameterSource.addValue(key, boolValue, Types.BOOLEAN);
                            log.debug("Converted '{}' from String to Boolean: {}", value, boolValue);
                        } else if (value instanceof Number) {
                            parameterSource.addValue(key, ((Number) value).intValue() != 0, Types.BOOLEAN);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "INTEGER":
                    case "INT":
                    case "INT4":
                        if (value instanceof String) {
                            Integer intValue = Integer.parseInt((String) value);
                            parameterSource.addValue(key, intValue, Types.INTEGER);
                            log.debug("Converted '{}' from String to Integer: {}", value, intValue);
                        } else if (value instanceof Number) {
                            parameterSource.addValue(key, ((Number) value).intValue(), Types.INTEGER);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "BIGINT":
                    case "INT8":
                    case "LONG":
                        if (value instanceof String) {
                            Long longValue = Long.parseLong((String) value);
                            parameterSource.addValue(key, longValue, Types.BIGINT);
                            log.debug("Converted '{}' from String to Long: {}", value, longValue);
                        } else if (value instanceof Number) {
                            parameterSource.addValue(key, ((Number) value).longValue(), Types.BIGINT);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "SMALLINT":
                    case "INT2":
                        if (value instanceof String) {
                            Short shortValue = Short.parseShort((String) value);
                            parameterSource.addValue(key, shortValue, Types.SMALLINT);
                        } else if (value instanceof Number) {
                            parameterSource.addValue(key, ((Number) value).shortValue(), Types.SMALLINT);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "DECIMAL":
                    case "NUMERIC":
                        if (value instanceof String) {
                            java.math.BigDecimal decimalValue = new java.math.BigDecimal((String) value);
                            parameterSource.addValue(key, decimalValue, Types.DECIMAL);
                        } else if (value instanceof Number) {
                            parameterSource.addValue(key, java.math.BigDecimal.valueOf(((Number) value).doubleValue()), Types.DECIMAL);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "DOUBLE":
                    case "FLOAT":
                    case "FLOAT8":
                    case "FLOAT4":
                        if (value instanceof String) {
                            Double doubleValue = Double.parseDouble((String) value);
                            parameterSource.addValue(key, doubleValue, Types.DOUBLE);
                        } else if (value instanceof Number) {
                            parameterSource.addValue(key, ((Number) value).doubleValue(), Types.DOUBLE);
                        } else {
                            parameterSource.addValue(key, value);
                        }
                        break;

                    case "CHAR":
                    case "VARCHAR":
                    case "TEXT":
                    case "BPCHAR":
                    default:
                        parameterSource.addValue(key, value, Types.VARCHAR);
                        log.debug("Keeping '{}' as string type", key);
                }
            } catch (Exception e) {
                log.error("Error converting parameter '{}' with value '{}' to type {}: {}", key, value, columnType, e.getMessage());
                parameterSource.addValue(key, value);
            }
        }

        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (!parameterSource.hasValue(param.getKey()) && param.getDefaultValue() != null) {
                    parameterSource.addValue(param.getKey(), param.getDefaultValue());
                    log.debug("Added default value for parameter '{}': {}", param.getKey(), param.getDefaultValue());
                }
            }
        }

        return parameterSource;
    }

    /**
     * Apply pagination to results
     */
    private Map<String, Object> applyPagination(List<Map<String, Object>> results,
                                                ExecuteApiRequestDTO executeRequest,
                                                Integer defaultPageSize) {
        int page = 0;
        int size = defaultPageSize != null ? defaultPageSize : 20;

        if (executeRequest.getQueryParams() != null) {
            if (executeRequest.getQueryParams().containsKey("page")) {
                page = Integer.parseInt(executeRequest.getQueryParams().get("page").toString());
            }
            if (executeRequest.getQueryParams().containsKey("size")) {
                size = Integer.parseInt(executeRequest.getQueryParams().get("size").toString());
            }
        }

        int start = page * size;
        int end = Math.min(start + size, results.size());
        List<Map<String, Object>> pagedResults = results.subList(start, end);

        return Map.of(
                "content", pagedResults,
                "page", page,
                "size", size,
                "totalElements", results.size(),
                "totalPages", (int) Math.ceil((double) results.size() / size)
        );
    }
}