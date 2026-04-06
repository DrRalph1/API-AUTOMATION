package com.usg.apiGeneration.utils.apiEngine.executor.oracle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.*;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiGeneration.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.apiGeneration.utils.apiEngine.OracleParameterValidatorUtil;
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

import java.time.LocalDateTime;
import java.util.*;
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

    public OracleProcedureExecutorUtil(
            OracleParameterValidatorUtil oracleParameterValidatorUtil,
            OracleObjectResolverUtil objectResolver) {
        this.oracleParameterValidatorUtil = oracleParameterValidatorUtil;
        this.objectResolver = objectResolver;
    }


    /**
     * Generate current timestamp in format YYYYMMDDHHMMSS
     * Used for AUTOGENERATE data type parameters
     */
    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d%02d%02d%02d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
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

        if (request.getBody() != null) {
            if (request.getBody() instanceof String) {
                String bodyString = (String) request.getBody();
                if (bodyString.trim().startsWith("<")) {
                    isXmlBody = true;
                    xmlBody = bodyString;
                    log.info("XML BODY DETECTED! Length: {} characters", xmlBody.length());
                } else if (bodyString.trim().startsWith("{") || bodyString.trim().startsWith("[")) {
                    try {
                        Map<String, Object> jsonMap = objectMapper.readValue(bodyString, new TypeReference<Map<String, Object>>() {});
                        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                            String paramKey = entry.getKey().toLowerCase();
                            String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                            dbParams.put(dbParamName, entry.getValue());
                            log.debug("Added JSON param: {} -> {}", entry.getKey(), dbParamName);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse JSON body: {}", e.getMessage());
                    }
                }
            } else if (request.getBody() instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
                log.info("JSON body detected with keys: {}", bodyMap.keySet());
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    String paramKey = entry.getKey().toLowerCase();
                    String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                    dbParams.put(dbParamName, entry.getValue());
                    log.debug("Added JSON param: {} -> {}", entry.getKey(), dbParamName);
                }
            }
        }

        // ============ PROCESS XML BODY ============
        if (isXmlBody && xmlBody != null) {
            log.info("Processing XML body for procedure execution");
            Map<String, Object> extractedXmlParams = parseXmlParameters(xmlBody, configuredParamDTOs, apiToDbParamMap);
            if (!extractedXmlParams.isEmpty()) {
                dbParams.putAll(extractedXmlParams);
                log.info("✅ Extracted {} parameters from XML", extractedXmlParams.size());
            }
        }

        // ============ ADD PATH AND QUERY PARAMETERS ============
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added path param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String paramKey = entry.getKey().toLowerCase();
                String dbParamName = apiToDbParamMap.getOrDefault(paramKey, entry.getKey().toUpperCase());
                dbParams.put(dbParamName, entry.getValue());
                log.debug("Added query param: {} -> {}", entry.getKey(), dbParamName);
            }
        }

        // ============ HANDLE AUTOGENERATE PARAMETERS ============
        if (api != null && api.getParameters() != null && !api.getParameters().isEmpty()) {
            log.info("Checking for AUTOGENERATE parameters to auto-populate");

            for (ApiParameterEntity param : api.getParameters()) {
                if ("AUTOGENERATE".equalsIgnoreCase(param.getOracleType())) {
                    String paramKey = param.getKey();
                    String dbParamName = getDbParamName(param);
                    String timestamp = getCurrentTimestamp();

                    // Check if this parameter already has a value
                    boolean hasExistingValue = false;

                    if (dbParams.containsKey(dbParamName)) {
                        hasExistingValue = true;
                        log.info("AUTOGENERATE parameter [{}] already has value: {}, keeping existing value",
                                paramKey, dbParams.get(dbParamName));
                    }

                    if (request.getPathParams() != null && request.getPathParams().containsKey(paramKey)) {
                        hasExistingValue = true;
                        log.info("AUTOGENERATE parameter [{}] found in path params", paramKey);
                    }

                    if (request.getQueryParams() != null && request.getQueryParams().containsKey(paramKey)) {
                        hasExistingValue = true;
                        log.info("AUTOGENERATE parameter [{}] found in query params", paramKey);
                    }

                    if (!hasExistingValue) {
                        dbParams.put(dbParamName, timestamp);
                        log.info("🔧 AUTOGENERATE parameter [{}] auto-populated with timestamp: {}",
                                paramKey, timestamp);
                    }
                }
            }
        }

        // Convert complex objects to JSON strings
        for (Map.Entry<String, Object> entry : dbParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map || value instanceof List) {
                try {
                    String jsonValue = objectMapper.writeValueAsString(value);
                    dbParams.put(entry.getKey(), jsonValue);
                    log.debug("Converted complex object to JSON string for parameter: {}", entry.getKey());
                } catch (Exception e) {
                    log.warn("Failed to convert complex object to string: {}", e.getMessage());
                }
            }
        }

        log.info("Final DB params prepared: {}", dbParams.keySet());

        // ============ OWNER RESOLUTION ============
        String oracleOwner = resolveOwner(owner, sourceObject, api, procedureName);
        if (oracleOwner == null || oracleOwner.trim().isEmpty()) {
            throw new ValidationException("Could not determine the database schema/owner for procedure: " + procedureName);
        }

        oracleOwner = oracleOwner.toUpperCase();
        String oracleProcedureName = procedureName != null ? procedureName.trim().toUpperCase() : null;

        log.info("Final resolved owner: {}", oracleOwner);
        log.info("Final procedure name: {}", oracleProcedureName);

        // ============ SYNONYM RESOLUTION ============
        Map<String, Object> resolution = objectResolver.resolveProcedureTarget(oracleOwner, oracleProcedureName);
        String actualOwner;
        String actualProcedureName;

        if (resolution != null && resolution.containsKey("isSynonym") && (boolean) resolution.get("isSynonym")) {
            actualOwner = (String) resolution.get("targetOwner");
            actualProcedureName = (String) resolution.get("targetName");
            log.info("✅ Resolved synonym to: {}.{}", actualOwner, actualProcedureName);
        } else {
            actualOwner = oracleOwner;
            actualProcedureName = oracleProcedureName;
        }

        // ============ VALIDATION ============
        try {
            objectResolver.validateDatabaseObject(actualOwner, actualProcedureName, "PROCEDURE");
            log.info("✅ Procedure {}.{} exists and is valid", actualOwner, actualProcedureName);
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException(String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                    actualOwner, actualProcedureName));
        }

        try {
            oracleParameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, actualOwner, actualProcedureName);
            log.info("✅ All parameter validations passed");
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // ============ EXECUTE PROCEDURE ============
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate);

            if (actualOwner != null && !actualOwner.isEmpty()) {
                jdbcCall = jdbcCall.withSchemaName(actualOwner);
            }

            jdbcCall = jdbcCall.withProcedureName(actualProcedureName);
            log.info("Executing: {}.{}", actualOwner, actualProcedureName);

            // Declare parameters in order
            List<ParameterDeclaration> allParameters = new ArrayList<>();

            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                for (ApiParameterEntity param : api.getParameters()) {
                    if (param == null || param.getKey() == null) continue;

                    String dbParamName = getDbParamName(param);
                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";
                    int position = param.getPosition() != null ? param.getPosition() : Integer.MAX_VALUE;
                    int sqlType = mapToSqlType(param.getOracleType());

                    allParameters.add(new ParameterDeclaration(
                            dbParamName, paramMode, position, sqlType, param.getOracleType(), param));
                }
            }

            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        String outParamName = mapping.getDbColumn() != null && !mapping.getDbColumn().isEmpty() ?
                                mapping.getDbColumn().toUpperCase() : "out_param_" + mapping.getPosition();

                        boolean exists = allParameters.stream()
                                .anyMatch(p -> p.parameterName.equals(outParamName));

                        if (!exists) {
                            int position = mapping.getPosition() != null ? mapping.getPosition() : Integer.MAX_VALUE;
                            int sqlType = mapToSqlType(mapping.getOracleType());

                            allParameters.add(new ParameterDeclaration(
                                    outParamName, "OUT", position, sqlType, mapping.getOracleType(), null));
                        }
                    }
                }
            }

            allParameters.sort(Comparator.comparingInt(p -> p.position));

            for (ParameterDeclaration param : allParameters) {
                if ("OUT".equals(param.mode) || "IN/OUT".equals(param.mode) || "INOUT".equals(param.mode)) {
                    jdbcCall.declareParameters(new SqlOutParameter(param.parameterName, param.sqlType));
                    log.debug("Declared OUT parameter: {}", param.parameterName);
                } else {
                    if (dbParams.containsKey(param.parameterName)) {
                        jdbcCall.declareParameters(new SqlParameter(param.parameterName, param.sqlType));
                        log.debug("Declared IN parameter: {}", param.parameterName);
                    }
                }
            }

            Map<String, Object> result = jdbcCall.execute(dbParams);
            log.info("Procedure executed successfully");

            // Process results
            Map<String, Object> responseData = new HashMap<>();

            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        String dbColumn = mapping.getDbColumn();
                        if (dbColumn != null && !dbColumn.isEmpty()) {
                            String upperDbColumn = dbColumn.toUpperCase();
                            if (result.containsKey(upperDbColumn)) {
                                responseData.put(mapping.getApiField(), result.get(upperDbColumn));
                                log.debug("Mapped {} to {}", upperDbColumn, mapping.getApiField());
                            }
                        }
                    }
                }
            }

            return responseData.isEmpty() ? result : responseData;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing procedure: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute procedure: " + e.getMessage(), e);
        }
    }

    // Helper method to get DB param name
    private String getDbParamName(ApiParameterEntity param) {
        if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
            return param.getDbParameter().toUpperCase();
        }
        if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
            return param.getDbColumn().toUpperCase();
        }
        return param.getKey().toUpperCase();
    }

    // Helper method to map Oracle type to SQL type
    private int mapToSqlType(String oracleType) {
        if (oracleType == null) return java.sql.Types.VARCHAR;
        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR")) return java.sql.Types.VARCHAR;
        if (upperType.contains("NUMBER")) return java.sql.Types.NUMERIC;
        if (upperType.contains("DATE")) return java.sql.Types.DATE;
        if (upperType.contains("TIMESTAMP")) return java.sql.Types.TIMESTAMP;
        if (upperType.contains("CLOB")) return java.sql.Types.CLOB;
        return java.sql.Types.VARCHAR;
    }

    // Helper class for parameter declaration
    private static class ParameterDeclaration {
        final String parameterName;
        final String mode;
        final int position;
        final int sqlType;
        final String oracleType;
        final ApiParameterEntity apiParameter;

        ParameterDeclaration(String parameterName, String mode, int position,
                             int sqlType, String oracleType, ApiParameterEntity apiParameter) {
            this.parameterName = parameterName;
            this.mode = mode;
            this.position = position;
            this.sqlType = sqlType;
            this.oracleType = oracleType;
            this.apiParameter = apiParameter;
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

        // Strategy 3: Try sourceObject.getSchemaName() (note: this is the getter for schemaName)
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

                    // Try multiple possible keys (case-insensitive)
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
                    // If it's a String, parse it
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});

                    // Try multiple possible keys (case-insensitive)
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