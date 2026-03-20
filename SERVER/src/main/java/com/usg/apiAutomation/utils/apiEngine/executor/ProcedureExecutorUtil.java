package com.usg.apiAutomation.utils.apiEngine.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.apiAutomation.utils.apiEngine.ParameterValidatorUtil;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ProcedureExecutorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final ParameterValidatorUtil parameterValidatorUtil;
    private final OracleObjectResolverUtil objectResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProcedureExecutorUtil(
            ParameterValidatorUtil parameterValidatorUtil,
            OracleObjectResolverUtil objectResolver) {
        this.parameterValidatorUtil = parameterValidatorUtil;
        this.objectResolver = objectResolver;
    }

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String procedureName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // ============ DEBUGGING: Log all input parameters ============
        log.info("============ PROCEDURE EXECUTOR DEBUG ============");
        log.info("API ID: {}", api != null ? api.getId() : "null");
        log.info("API Name: {}", api != null ? api.getApiName() : "null");
        log.info("Procedure Name parameter: {}", procedureName);
        log.info("Owner parameter: {}", owner);
        log.info("Request Body Type: {}", request.getBody() != null ? request.getBody().getClass().getName() : "null");
        log.info("Request Body: {}", request.getBody());

        // ============ CREATE PARAMETER MAPPING ============
        // Build a map of API parameter keys to database parameter names
        Map<String, String> apiToDbParamMap = new HashMap<>();
        if (configuredParamDTOs != null) {
            for (ApiParameterDTO param : configuredParamDTOs) {
                if (param.getKey() != null) {
                    // Use dbParameter if available, otherwise use dbColumn, otherwise use the key
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
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());

                        // Handle nested objects and arrays
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

            // FIRST: Try to extract individual parameters from XML
            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbParamMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML and added to dbParams", extractedXmlParams.size());
                log.info("Extracted params: {}", extractedXmlParams.keySet());
            }

            // Strategy 1: Look for explicit XML/CLOB parameter in API configuration
            for (ApiParameterEntity param : api.getParameters()) {
                String paramKey = param.getKey().toLowerCase();
                String dbParamName = getDbParamName(param);

                // Check if this parameter is designed to accept XML
                boolean isXmlParameter = paramKey.contains("xml") ||
                        paramKey.contains("clob") ||
                        paramKey.contains("request") ||
                        paramKey.contains("payload") ||
                        paramKey.contains("body") ||
                        paramKey.equals("_xml") ||
                        paramKey.equals("xmldata");

                if (isXmlParameter && dbParamName != null) {
                    // Only add the full XML if we haven't already extracted individual params
                    // and this parameter hasn't been set yet
                    if (!dbParams.containsKey(dbParamName)) {
                        dbParams.put(dbParamName, xmlBody);
                        hasXmlParameter = true;
                        log.info("✅ Mapped full XML body to database parameter: {}", dbParamName);
                    }
                    break;
                }
            }

            // Strategy 2: If no explicit XML parameter found, look for any CLOB/VARCHAR2 parameter
            if (!hasXmlParameter && extractedXmlParams.isEmpty()) {
                for (ApiParameterEntity param : api.getParameters()) {
                    String dbParamName = getDbParamName(param);
                    if (dbParamName != null) {
                        String oracleType = param.getOracleType();
                        if (oracleType != null) {
                            String upperType = oracleType.toUpperCase();
                            if (upperType.contains("CLOB") ||
                                    upperType.contains("VARCHAR") ||
                                    upperType.contains("LONG")) {
                                dbParams.put(dbParamName, xmlBody);
                                hasXmlParameter = true;
                                log.info("✅ Mapped full XML body to CLOB/VARCHAR parameter: {}", dbParamName);
                                break;
                            }
                        }
                    }
                }
            }

            // Strategy 3: If no suitable parameter found, log warning
            if (!hasXmlParameter && extractedXmlParams.isEmpty()) {
                log.warn("⚠️ No suitable database parameter found for XML body and no individual parameters extracted. " +
                        "XML will be stored in a default location.");
                dbParams.put("XML_BODY", xmlBody);
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        // Add path parameters
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
            log.info("Path params added: {}", request.getPathParams().keySet());
        }

        // Add query parameters
        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
            log.info("Query params added: {}", request.getQueryParams().keySet());
        }

        // ============ ADD HEADERS AS PARAMETERS (if needed) ============
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String headerKey = entry.getKey().toLowerCase();
                // Check if the API expects this header as a parameter
                boolean headerIsParameter = api.getParameters().stream()
                        .anyMatch(p -> p.getKey().equalsIgnoreCase(headerKey));

                if (headerIsParameter) {
                    String dbParamName = apiToDbParamMap.getOrDefault(headerKey, headerKey.toUpperCase());
                    dbParams.put(dbParamName, entry.getValue());
                    log.debug("Added header as parameter: {} -> {} = {}", headerKey, dbParamName, entry.getValue());
                }
            }
        }

        // ============ HANDLE COLLECTION/ARRAY PARAMETERS ============
        // Convert collection/array parameters to single values for database
        for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || (value != null && value.getClass().isArray())) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value
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
        // Resolve the actual target (handle synonyms)
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

        // ==================== VALIDATION STEP 1: Validate procedure exists and is valid ====================
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
            parameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, actualOwner, actualProcedureName);
            log.info("✅ All parameter validations passed for procedure {}.{}", actualOwner, actualProcedureName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== EXECUTE PROCEDURE ====================
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate);

            // Set schema and procedure name - use resolved actual owner
            if (actualOwner != null && !actualOwner.isEmpty()) {
                jdbcCall = jdbcCall.withSchemaName(actualOwner);
                log.info("Setting schema name to: {}", actualOwner);
            }

            jdbcCall = jdbcCall.withProcedureName(actualProcedureName);
            log.info("Setting procedure name to: {}", actualProcedureName);

            log.info("Oracle will execute: {}.{}", actualOwner != null ? actualOwner : "<default schema>", actualProcedureName);

            // Declare output parameters from response mappings
            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                log.debug("Declaring {} OUT parameters from response mappings", api.getResponseMappings().size());

                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        String outParamName = mapping.getDbColumn() != null && !mapping.getDbColumn().isEmpty() ?
                                mapping.getDbColumn().toUpperCase() : "out_param_" + mapping.getPosition();

                        int sqlType = mapToSqlType(mapping.getOracleType());
                        jdbcCall.declareParameters(new SqlOutParameter(outParamName, sqlType));

                        log.debug("Declared OUT parameter: {} of type: {} (SQL type: {})",
                                outParamName, mapping.getOracleType(), sqlType);
                    }
                }
            }

            // Declare input parameters from API parameters - use database parameter names
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                int inParamCount = 0;

                for (ApiParameterEntity param : api.getParameters()) {
                    // Skip if parameter is null or has no key
                    if (param == null || param.getKey() == null) continue;

                    String paramType = param.getParameterType();
                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";

                    // Get the database parameter name
                    String dbParamName = getDbParamName(param);

                    // Check if this parameter is meant to be an IN parameter
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
                    } else if (dbParams.containsKey(dbParamName)) {
                        log.debug("Parameter {} is not an IN parameter (mode: {}), skipping", dbParamName, paramMode);
                    }
                }

                log.debug("Declared {} IN parameters", inParamCount);
            }

            log.info("Executing SimpleJdbcCall for {}.{} with {} input parameters",
                    actualOwner != null ? actualOwner : "<default>", actualProcedureName, dbParams.size());

            // Execute the procedure with the mapped database parameters
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
                            // Try with uppercase first (Oracle returns uppercase column names)
                            String upperDbColumn = dbColumn.toUpperCase();

                            if (result.containsKey(upperDbColumn)) {
                                responseData.put(mapping.getApiField(), result.get(upperDbColumn));
                                log.debug("Mapped output {} to {} with value: {}",
                                        upperDbColumn, mapping.getApiField(), result.get(upperDbColumn));
                                mappedCount++;
                            }
                            // Try with original case if uppercase not found
                            else if (result.containsKey(dbColumn)) {
                                responseData.put(mapping.getApiField(), result.get(dbColumn));
                                log.debug("Mapped output {} to {} with value: {}",
                                        dbColumn, mapping.getApiField(), result.get(dbColumn));
                                mappedCount++;
                            }
                            else {
                                log.warn("Output parameter {} not found in result set. Available keys: {}",
                                        dbColumn, result.keySet());
                            }
                        }
                    }
                }

                log.debug("Mapped {} output parameters to response", mappedCount);
            } else {
                // If no response mappings, just return the whole result
                log.debug("No response mappings found, returning entire result map");
                responseData.putAll(result);
            }

            log.info("============ PROCEDURE EXECUTION COMPLETE ============");
            return responseData.isEmpty() ? result : responseData;

        } catch (ValidationException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error executing procedure {}.{}: {}",
                    actualOwner != null ? actualOwner : "<default>", actualProcedureName, e.getMessage(), e);

            // Provide user-friendly error messages for common Oracle errors
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
                if (errorMessage.contains("ORA-01400")) {
                    throw new ValidationException(
                            "A required value is missing for a NOT NULL column. Please provide all required parameters."
                    );
                }
                if (errorMessage.contains("ORA-12899")) {
                    throw new ValidationException(
                            String.format("Value too large for column. Details: %s", extractOracleError(errorMessage))
                    );
                }
                if (errorMessage.contains("Invalid column type")) {
                    throw new ValidationException(
                            "Invalid parameter format. Please check the data types of your parameters."
                    );
                }
            }

            throw new RuntimeException("Failed to execute the requested operation: " + extractOracleError(errorMessage), e);
        }
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
     * Helper method to resolve owner from multiple sources
     */
    private String resolveOwner(String owner, ApiSourceObjectDTO sourceObject, GeneratedApiEntity api, String procedureName) {
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
            String currentSchema = oracleJdbcTemplate.queryForObject(
                    "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL",
                    String.class);
            if (currentSchema != null && !currentSchema.isEmpty()) {
                log.info("Strategy 5 - Using current schema from Oracle: {}", currentSchema);
                return currentSchema;
            }
        } catch (Exception e) {
            log.warn("Could not get current schema: {}", e.getMessage());
        }

        // Strategy 6: Try to resolve the procedure from all accessible schemas
        try {
            log.info("Strategy 6 - Attempting to locate procedure '{}' in accessible schemas", procedureName);

            // Query to find the procedure in any schema the current user has access to
            String findProcedureSql = "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND OBJECT_TYPE = 'PROCEDURE' AND ROWNUM = 1";
            List<String> owners = oracleJdbcTemplate.queryForList(findProcedureSql, String.class, procedureName);

            if (!owners.isEmpty()) {
                String foundOwner = owners.get(0);
                log.info("Strategy 6 - Found procedure '{}' in schema: {}", procedureName, foundOwner);
                return foundOwner;
            }

            // If not found as procedure, check if it's a function (in case of mixed usage)
            String findFunctionSql = "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND OBJECT_TYPE = 'FUNCTION' AND ROWNUM = 1";
            List<String> functionOwners = oracleJdbcTemplate.queryForList(findFunctionSql, String.class, procedureName);

            if (!functionOwners.isEmpty()) {
                String foundOwner = functionOwners.get(0);
                log.warn("Strategy 6 - Found function '{}' in schema: {} (treating as procedure)", procedureName, foundOwner);
                return foundOwner;
            }

            // If still not found, try to find as any object type
            String findAnyObjectSql = "SELECT OWNER FROM ALL_OBJECTS WHERE OBJECT_NAME = ? AND ROWNUM = 1";
            List<String> anyOwners = oracleJdbcTemplate.queryForList(findAnyObjectSql, String.class, procedureName);

            if (!anyOwners.isEmpty()) {
                String foundOwner = anyOwners.get(0);
                log.warn("Strategy 6 - Found object '{}' in schema: {} (type unknown, treating as procedure)", procedureName, foundOwner);
                return foundOwner;
            }

            log.warn("Strategy 6 - Could not locate procedure '{}' in any accessible schema", procedureName);

        } catch (Exception e) {
            log.warn("Error while searching for procedure in accessible schemas: {}", e.getMessage());
        }

        log.error("❌ All owner resolution strategies failed for procedure: {}", procedureName);
        return null;
    }



    private int mapToSqlType(String oracleType) {
        if (oracleType == null) return java.sql.Types.VARCHAR;

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR")) return java.sql.Types.VARCHAR;
        if (upperType.contains("CHAR")) return java.sql.Types.CHAR;
        if (upperType.contains("CLOB")) return java.sql.Types.CLOB;
        if (upperType.contains("NUMBER") || upperType.contains("NUMERIC")) return java.sql.Types.NUMERIC;
        if (upperType.contains("INTEGER")) return java.sql.Types.INTEGER;
        if (upperType.contains("DATE")) return java.sql.Types.DATE;
        if (upperType.contains("TIMESTAMP")) return java.sql.Types.TIMESTAMP;
        if (upperType.contains("BLOB")) return java.sql.Types.BLOB;
        if (upperType.contains("BOOLEAN")) return java.sql.Types.BOOLEAN;

        return java.sql.Types.VARCHAR;
    }


    // Add these helper methods to ProcedureExecutorUtil
    private String getDbParamName(ApiParameterEntity param) {
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toUpperCase();
        }
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toUpperCase();
        }
        return param.getKey().toUpperCase();
    }

    private String getDbParamNameForApiKey(String apiKey, List<ApiParameterDTO> configuredParamDTOs) {
        for (ApiParameterDTO param : configuredParamDTOs) {
            if (param.getKey().equalsIgnoreCase(apiKey)) {
                if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
                    return param.getDbParameter().toUpperCase();
                }
                if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
                    return param.getDbColumn().toUpperCase();
                }
                return apiKey.toUpperCase();
            }
        }
        return apiKey.toUpperCase();
    }

    private void addPathAndQueryParams(ExecuteApiRequestDTO request,
                                       List<ApiParameterDTO> configuredParamDTOs,
                                       Map<String, Object> dbParams) {
        // Add path parameters
        if (request.getPathParams() != null) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String dbParamName = getDbParamNameForApiKey(entry.getKey(), configuredParamDTOs);
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
        }

        // Add query parameters
        if (request.getQueryParams() != null) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String dbParamName = getDbParamNameForApiKey(entry.getKey(), configuredParamDTOs);
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {} = {}", entry.getKey(), dbParamName, entry.getValue());
            }
        }
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
                        // Map to database parameter name
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbParamName, value);
                        log.info("✅ Extracted XML parameter: {} -> {} = {}", paramKey, dbParamName, value);
                    } else {
                        log.info("⚠️ XML tag <{}> found but empty", paramKey);
                        // Still add empty string as a value (required parameter might accept empty)
                        String dbParamName = apiToDbParamMap.getOrDefault(paramKey.toLowerCase(), paramKey.toUpperCase());
                        extractedParams.put(dbParamName, "");
                        log.info("Added empty parameter: {} -> {}", paramKey, dbParamName);
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
}