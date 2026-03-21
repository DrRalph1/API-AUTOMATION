package com.usg.apiAutomation.utils.apiEngine.executor.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.utils.apiEngine.PostgreSQLObjectResolverUtil;
import com.usg.apiAutomation.utils.apiEngine.PostgreSQLParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.*;
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

    public PostgreSQLProcedureExecutorUtil(
            PostgreSQLParameterValidatorUtil parameterValidatorUtil,
            PostgreSQLObjectResolverUtil objectResolver) {
        this.parameterValidatorUtil = parameterValidatorUtil;
        this.objectResolver = objectResolver;
    }

    /**
     * Execute a PostgreSQL procedure (stored procedure)
     *
     * @param api The generated API entity
     * @param sourceObject The source object containing procedure/schema information
     * @param procedureName The procedure name to execute
     * @param schema The schema name (equivalent to Oracle owner)
     * @param request The execution request with parameters
     * @param configuredParamDTOs Configured parameter DTOs
     * @return Execution result
     */
    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String procedureName, String schema, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ POSTGRESQL PROCEDURE EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("Procedure Name parameter: {}", procedureName);
        log.info("Schema parameter: {}", schema);
        log.info("Request Body Type: {}", request.getBody() != null ? request.getBody().getClass().getName() : "null");
        log.info("Request Body: {}", request.getBody());

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
        boolean hasBodyParameter = false;

        if (request.getBody() != null) {
            if (request.getBody() instanceof String) {
                String bodyString = (String) request.getBody();
                String trimmed = bodyString.trim();

                if (trimmed.startsWith("<")) {
                    isXmlBody = true;
                    xmlBody = bodyString;
                    log.info("=========================================");
                    log.info("XML BODY DETECTED!");
                    log.info("XML Length: {} characters", xmlBody.length());
                    log.info("XML Preview: {}", xmlBody.substring(0, Math.min(500, xmlBody.length())));
                    log.info("=========================================");
                } else if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                    log.info("JSON BODY DETECTED!");
                    log.info("JSON Preview: {}", bodyString.substring(0, Math.min(200, bodyString.length())));

                    // Parse JSON body
                    try {
                        Map<String, Object> jsonMap = objectMapper.readValue(bodyString, new TypeReference<Map<String, Object>>() {});
                        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                            String paramKey = entry.getKey().toLowerCase();
                            String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());

                            Object value = entry.getValue();
                            if (value instanceof Map || value instanceof List) {
                                value = objectMapper.writeValueAsString(value);
                            }

                            dbParams.put(dbParamName, value);
                            log.debug("Added JSON param: {} -> {} = {}", entry.getKey(), dbParamName, value);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse JSON body: {}", e.getMessage());
                        // Treat as plain text if JSON parsing fails
                        dbParams.put("request_body", bodyString);
                    }
                } else {
                    log.info("String body detected: {}", bodyString.substring(0, Math.min(100, bodyString.length())));
                    dbParams.put("request_body", bodyString);
                }
            } else if (request.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
                log.info("JSON body detected with keys: {}", bodyMap.keySet());
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    String paramKey = entry.getKey().toLowerCase();
                    String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());

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

        // ============ PROCESS XML BODY ============
        if (isXmlBody && xmlBody != null) {
            log.info("Processing XML body for procedure execution");

            // Try to extract individual parameters from XML
            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbParamMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML and added to dbParams", extractedXmlParams.size());
                log.info("Extracted params: {}", extractedXmlParams.keySet());
            }

            // Look for explicit XML/TEXT parameter in API configuration
            for (ApiParameterEntity param : api.getParameters()) {
                String paramKey = param.getKey().toLowerCase();
                String dbParamName = getDbParamName(param);

                boolean isBodyParameter = paramKey.contains("xml") ||
                        paramKey.contains("json") ||
                        paramKey.contains("text") ||
                        paramKey.contains("request") ||
                        paramKey.contains("payload") ||
                        paramKey.contains("body");

                if (isBodyParameter && dbParamName != null && !dbParams.containsKey(dbParamName)) {
                    dbParams.put(dbParamName, xmlBody);
                    hasBodyParameter = true;
                    log.info("✅ Mapped full XML body to database parameter: {}", dbParamName);
                    break;
                }
            }

            // If no explicit parameter found, look for TEXT parameter
            if (!hasBodyParameter && extractedXmlParams.isEmpty()) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String dbParamName = getDbParamName(param);
                    if (dbParamName != null) {
                        String pgType = param.getOracleType();
                        if (pgType != null && pgType.toLowerCase().contains("text")) {
                            dbParams.put(dbParamName, xmlBody);
                            hasBodyParameter = true;
                            log.info("✅ Mapped full XML body to TEXT parameter: {}", dbParamName);
                            break;
                        }
                    }
                }
            }

            if (!hasBodyParameter && extractedXmlParams.isEmpty()) {
                log.warn("⚠️ No suitable database parameter found for XML body");
                dbParams.put("xml_body", xmlBody);
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
            log.info("Path params added: {}", request.getPathParams().keySet());
        }

        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toLowerCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
            log.info("Query params added: {}", request.getQueryParams().keySet());
        }

        // ============ ADD HEADERS AS PARAMETERS ============
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String headerKey = entry.getKey().toLowerCase();
                boolean headerIsParameter = api.getParameters().stream()
                        .anyMatch(p -> p.getKey().equalsIgnoreCase(headerKey));

                if (headerIsParameter) {
                    String dbParamName = apiToDbParamMap.getOrDefault(headerKey, headerKey.toLowerCase());
                    dbParams.put(dbParamName, entry.getValue());
                    log.debug("Added header as parameter: {} -> {} = {}", headerKey, dbParamName, entry.getValue());
                }
            }
        }

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

        // ============ SCHEMA RESOLUTION STRATEGY ============
        String pgSchema = resolveSchema(schema, sourceObject, api, procedureName);

        if (pgSchema == null || pgSchema.trim().isEmpty()) {
            log.error("❌ COULD NOT DETERMINE SCHEMA NAME");
            throw new ValidationException(
                    "Could not determine the database schema for procedure: " + procedureName
            );
        }

        pgSchema = pgSchema.toLowerCase();
        String pgProcedureName = procedureName != null ? procedureName.trim().toLowerCase() : null;

        log.info("Final resolved schema: {}", pgSchema);
        log.info("Final procedure name: {}", pgProcedureName);

        // ============ RESOLVE PROCEDURE (PostgreSQL doesn't have synonyms) ============
        String actualSchema = pgSchema;
        String actualProcedureName = pgProcedureName;

        // ==================== VALIDATION STEP 1: Validate procedure exists ====================
        try {
            objectResolver.validateDatabaseObject(actualSchema, actualProcedureName, "PROCEDURE");
            log.info("✅ Procedure {}.{} exists", actualSchema, actualProcedureName);
        } catch (EmptyResultDataAccessException e) {
            log.error("❌ Procedure {}.{} does not exist", actualSchema, actualProcedureName);
            throw new ValidationException(
                    String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                            actualSchema, actualProcedureName)
            );
        }

        // ==================== VALIDATION STEP 2: Validate all parameters ====================
        try {
            parameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, actualSchema, actualProcedureName);
            log.info("✅ All parameter validations passed for procedure {}.{}", actualSchema, actualProcedureName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== EXECUTE PROCEDURE ====================
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(postgresqlJdbcTemplate);

            // Set schema and procedure name
            if (actualSchema != null && !actualSchema.isEmpty()) {
                jdbcCall = jdbcCall.withSchemaName(actualSchema);
                log.info("Setting schema name to: {}", actualSchema);
            }

            jdbcCall = jdbcCall.withProcedureName(actualProcedureName);
            log.info("Setting procedure name to: {}", actualProcedureName);

            log.info("PostgreSQL will execute: {}.{}", actualSchema, actualProcedureName);

            // Declare output parameters from response mappings
            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                log.debug("Declaring {} OUT parameters from response mappings", api.getResponseMappings().size());

                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        String outParamName = mapping.getDbColumn() != null && !mapping.getDbColumn().isEmpty() ?
                                mapping.getDbColumn().toLowerCase() : "out_param_" + mapping.getPosition();

                        int sqlType = mapToSqlType(mapping.getOracleType());
                        jdbcCall.declareParameters(new SqlOutParameter(outParamName, sqlType));

                        log.debug("Declared OUT parameter: {} of type: {} (SQL type: {})",
                                outParamName, mapping.getOracleType(), sqlType);
                    }
                }
            }

            // Declare input parameters from API parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                int inParamCount = 0;

                for (ApiParameterEntity param : api.getParameters()) {
                    if (param == null || param.getKey() == null) continue;

                    String paramType = param.getParameterType();
                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";

                    String dbParamName = getDbParamName(param);

                    boolean isInParameter = paramMode.contains("IN") || paramType == null ||
                            "query".equals(paramType) || "path".equals(paramType) || "body".equals(paramType);

                    if (dbParams.containsKey(dbParamName) && isInParameter) {
                        int sqlType = mapToSqlType(param.getOracleType());
                        jdbcCall.declareParameters(new SqlParameter(dbParamName, sqlType));

                        Object paramValue = dbParams.get(dbParamName);
                        log.debug("Declared IN parameter: {} of type: {} (SQL type: {}) with value: {}",
                                dbParamName, param.getOracleType(), sqlType,
                                paramValue instanceof String && paramValue.toString().length() > 100 ?
                                        paramValue.toString().substring(0, 100) + "..." : paramValue);
                        inParamCount++;
                    }
                }

                log.debug("Declared {} IN parameters", inParamCount);
            }

            log.info("Executing SimpleJdbcCall for {}.{} with {} input parameters",
                    actualSchema, actualProcedureName, dbParams.size());

            // Execute the procedure
            Map<String, Object> result = jdbcCall.execute(dbParams);

            log.info("Procedure executed successfully, result contains {} keys: {}", result.size(), result.keySet());

            // Map the results to response data
            Map<String, Object> responseData = new HashMap<>();

            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                int mappedCount = 0;

                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        String dbColumn = mapping.getDbColumn();
                        if (dbColumn != null && !dbColumn.isEmpty()) {
                            // PostgreSQL returns lowercase column names
                            String lowerDbColumn = dbColumn.toLowerCase();

                            if (result.containsKey(lowerDbColumn)) {
                                responseData.put(mapping.getApiField(), result.get(lowerDbColumn));
                                log.debug("Mapped output {} to {} with value: {}",
                                        lowerDbColumn, mapping.getApiField(), result.get(lowerDbColumn));
                                mappedCount++;
                            } else if (result.containsKey(dbColumn)) {
                                responseData.put(mapping.getApiField(), result.get(dbColumn));
                                log.debug("Mapped output {} to {} with value: {}",
                                        dbColumn, mapping.getApiField(), result.get(dbColumn));
                                mappedCount++;
                            } else {
                                log.warn("Output parameter {} not found in result set. Available keys: {}",
                                        dbColumn, result.keySet());
                            }
                        }
                    }
                }

                log.debug("Mapped {} output parameters to response", mappedCount);
            } else {
                log.debug("No response mappings found, returning entire result map");
                responseData.putAll(result);
            }

            log.info("============ PROCEDURE EXECUTION COMPLETE ============");
            return responseData.isEmpty() ? result : responseData;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing procedure {}.{}: {}",
                    actualSchema, actualProcedureName, e.getMessage(), e);

            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("ERROR: procedure") && errorMessage.contains("does not exist")) {
                    throw new ValidationException(
                            String.format("Procedure '%s.%s' does not exist. Please check the procedure name and schema.",
                                    actualSchema, actualProcedureName)
                    );
                }
                if (errorMessage.contains("ERROR: permission denied")) {
                    throw new ValidationException(
                            String.format("Insufficient privileges to execute procedure '%s.%s'. Details: %s",
                                    actualSchema, actualProcedureName, extractPostgreSQLError(errorMessage))
                    );
                }
                if (errorMessage.contains("ERROR: null value in column")) {
                    throw new ValidationException(
                            "A required value is missing for a NOT NULL column. Please provide all required parameters."
                    );
                }
                if (errorMessage.contains("ERROR: value too long for type")) {
                    throw new ValidationException(
                            String.format("Value too large for column. Details: %s", extractPostgreSQLError(errorMessage))
                    );
                }
                if (errorMessage.contains("ERROR: invalid input syntax")) {
                    throw new ValidationException(
                            "Invalid parameter format. Please check the data types of your parameters."
                    );
                }
            }

            throw new RuntimeException("Failed to execute the requested operation: " + extractPostgreSQLError(errorMessage), e);
        }
    }

    /**
     * Helper method to extract PostgreSQL error message
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
     * Helper method to resolve schema from multiple sources
     */
    private String resolveSchema(String schema, ApiSourceObjectDTO sourceObject,
                                 GeneratedApiEntity api, String procedureName) {
        // Strategy 1: Use the schema parameter
        if (schema != null && !schema.trim().isEmpty()) {
            log.info("Strategy 1 - Using schema parameter: {}", schema);
            return schema.trim().toLowerCase();
        }

        // Strategy 2: Try sourceObject.getOwner()
        if (sourceObject != null && sourceObject.getOwner() != null && !sourceObject.getOwner().trim().isEmpty()) {
            log.info("Strategy 2 - Using sourceObject.getOwner(): {}", sourceObject.getOwner());
            return sourceObject.getOwner().trim().toLowerCase();
        }

        // Strategy 3: Try sourceObject.getSchemaName()
        if (sourceObject != null && sourceObject.getSchemaName() != null && !sourceObject.getSchemaName().trim().isEmpty()) {
            log.info("Strategy 3 - Using sourceObject.getSchemaName(): {}", sourceObject.getSchemaName());
            return sourceObject.getSchemaName().trim().toLowerCase();
        }

        // Strategy 4: Get from API's source_object_info
        if (api != null && api.getSourceObjectInfo() != null) {
            try {
                if (api.getSourceObjectInfo() instanceof Map) {
                    Map<String, Object> sourceInfo = (Map<String, Object>) api.getSourceObjectInfo();
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        String schemaName = sourceInfo.get("schemaName").toString();
                        log.info("Strategy 4 - Using schemaName from source_object_info Map: {}", schemaName);
                        return schemaName.trim().toLowerCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        String ownerName = sourceInfo.get("owner").toString();
                        log.info("Strategy 4 - Using owner from source_object_info Map: {}", ownerName);
                        return ownerName.trim().toLowerCase();
                    }
                } else {
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        String schemaName = sourceInfo.get("schemaName").toString();
                        log.info("Strategy 4 - Using schemaName from source_object_info JSON: {}", schemaName);
                        return schemaName.trim().toLowerCase();
                    }
                    if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        String ownerName = sourceInfo.get("owner").toString();
                        log.info("Strategy 4 - Using owner from source_object_info JSON: {}", ownerName);
                        return ownerName.trim().toLowerCase();
                    }
                }
            } catch (Exception e) {
                log.warn("Could not process sourceObjectInfo: {}", e.getMessage());
            }
        }

        // Strategy 5: Try to get current schema
        try {
            String currentSchema = postgresqlJdbcTemplate.queryForObject("SELECT current_schema()", String.class);
            if (currentSchema != null && !currentSchema.isEmpty()) {
                log.info("Strategy 5 - Using current schema: {}", currentSchema);
                return currentSchema;
            }
        } catch (Exception e) {
            log.warn("Could not get current schema: {}", e.getMessage());
        }

        // Strategy 6: Try to locate the procedure in accessible schemas
        try {
            log.info("Strategy 6 - Attempting to locate procedure '{}' in accessible schemas", procedureName);

            String findProcedureSql = "SELECT n.nspname FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE p.proname = ? AND p.prokind = 'p' " +
                    "AND n.nspname NOT IN ('pg_catalog', 'information_schema') " +
                    "LIMIT 1";

            List<String> schemas = postgresqlJdbcTemplate.queryForList(findProcedureSql, String.class, procedureName);

            if (!schemas.isEmpty()) {
                String foundSchema = schemas.get(0);
                log.info("Strategy 6 - Found procedure '{}' in schema: {}", procedureName, foundSchema);
                return foundSchema;
            }

            log.warn("Strategy 6 - Could not locate procedure '{}' in any accessible schema", procedureName);

        } catch (Exception e) {
            log.warn("Error while searching for procedure in accessible schemas: {}", e.getMessage());
        }

        // Strategy 7: Default to 'public' schema
        log.info("Strategy 7 - Using default 'public' schema");
        return "public";
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

    private String getDbParamName(ApiParameterEntity param) {
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toLowerCase();
        }
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toLowerCase();
        }
        return param.getKey().toLowerCase();
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
        log.debug("XML Body: {}", xmlBody);

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

            log.info("Extracted {} parameters from XML: {}", extractedParams.size(), extractedParams.keySet());

        } catch (Exception e) {
            log.error("Error parsing XML parameters: {}", e.getMessage(), e);
        }

        return extractedParams;
    }
}