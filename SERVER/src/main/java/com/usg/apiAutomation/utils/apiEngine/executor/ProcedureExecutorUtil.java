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

        // Log sourceObject details
        if (sourceObject != null) {
            log.info("Source Object - Owner: {}", sourceObject.getOwner());
            log.info("Source Object - Name: {}", sourceObject.getObjectName());
            log.info("Source Object - Type: {}", sourceObject.getObjectType());
            log.info("Source Object - Schema: {}", sourceObject.getSchemaName());
        } else {
            log.info("Source Object is NULL");
        }

        // Log API's source_object_info
        if (api != null && api.getSourceObjectInfo() != null) {
            log.info("API SourceObjectInfo type: {}", api.getSourceObjectInfo().getClass().getName());

            // Try to cast to Map if it's already a Map
            if (api.getSourceObjectInfo() instanceof Map) {
                Map<String, Object> sourceInfo = (Map<String, Object>) api.getSourceObjectInfo();
                log.info("SourceObjectInfo Map: {}", sourceInfo);
                if (sourceInfo.containsKey("schemaName")) {
                    log.info("schemaName from sourceObjectInfo: {}", sourceInfo.get("schemaName"));
                }
                if (sourceInfo.containsKey("owner")) {
                    log.info("owner from sourceObjectInfo: {}", sourceInfo.get("owner"));
                }
            } else {
                // If it's a String, try to parse it
                try {
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                    log.info("Parsed sourceObjectInfo: {}", sourceInfo);
                    if (sourceInfo.containsKey("schemaName")) {
                        log.info("schemaName from sourceObjectInfo: {}", sourceInfo.get("schemaName"));
                    }
                    if (sourceInfo.containsKey("owner")) {
                        log.info("owner from sourceObjectInfo: {}", sourceInfo.get("owner"));
                    }
                } catch (Exception e) {
                    log.warn("Could not parse sourceObjectInfo JSON: {}", e.getMessage());
                }
            }
        }

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
                    apiToDbParamMap.put(param.getKey(), dbParamName.toUpperCase());
                    log.info("Parameter mapping: API '{}' -> Database '{}'", param.getKey(), dbParamName.toUpperCase());
                }
            }
        }

        // Create consolidated parameters map from all sources, using database parameter names
        Map<String, Object> dbParams = new HashMap<>();

        // Add path parameters
        if (request.getPathParams() != null) {
            for (Map.Entry<String, Object> entry : request.getPathParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey(), entry.getKey());
                dbParams.put(dbParamName, entry.getValue());
            }
            log.info("Path params (mapped to DB names): {}", request.getPathParams());
        }

        // Add query parameters
        if (request.getQueryParams() != null) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey(), entry.getKey());
                dbParams.put(dbParamName, entry.getValue());
            }
            log.info("Query params (mapped to DB names): {}", request.getQueryParams());
        }

        // Add body parameters if it's a map
        if (request.getBody() instanceof Map) {
            Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
            for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                String dbParamName = apiToDbParamMap.getOrDefault(entry.getKey(), entry.getKey());
                dbParams.put(dbParamName, entry.getValue());
            }
            log.info("Body params (mapped to DB names): {}", request.getBody());
        }

        // Also keep original params for logging
        Map<String, Object> originalParams = new HashMap<>();
        if (request.getPathParams() != null) originalParams.putAll(request.getPathParams());
        if (request.getQueryParams() != null) originalParams.putAll(request.getQueryParams());
        if (request.getBody() instanceof Map) originalParams.putAll((Map<String, Object>) request.getBody());

        // Handle collection/array parameters - convert to single values for database
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

        log.info("Executing procedure - Original: {}.{} with original params: {}", owner, procedureName, originalParams);
        log.info("Executing procedure - Mapped DB params: {}", dbParams);

        // ============ OWNER RESOLUTION STRATEGY ============
        String oracleOwner = null;

        // Strategy 1: Use the owner parameter passed to the method
        if (owner != null && !owner.trim().isEmpty()) {
            oracleOwner = owner.trim();
            log.info("Strategy 1 - Using owner parameter: {}", oracleOwner);
        }

        // Strategy 2: Try sourceObject.getOwner()
        if (oracleOwner == null && sourceObject != null && sourceObject.getOwner() != null && !sourceObject.getOwner().trim().isEmpty()) {
            oracleOwner = sourceObject.getOwner().trim();
            log.info("Strategy 2 - Using sourceObject.getOwner(): {}", oracleOwner);
        }

        // Strategy 3: Try sourceObject.getSchemaName()
        if (oracleOwner == null && sourceObject != null && sourceObject.getSchemaName() != null && !sourceObject.getSchemaName().trim().isEmpty()) {
            oracleOwner = sourceObject.getSchemaName().trim();
            log.info("Strategy 3 - Using sourceObject.getSchemaName(): {}", oracleOwner);
        }

        // Strategy 4: Get from API's source_object_info (as Map)
        if (oracleOwner == null && api != null && api.getSourceObjectInfo() != null) {
            try {
                // Check if it's already a Map
                if (api.getSourceObjectInfo() instanceof Map) {
                    Map<String, Object> sourceInfo = (Map<String, Object>) api.getSourceObjectInfo();
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        oracleOwner = sourceInfo.get("schemaName").toString().trim();
                        log.info("Strategy 4 - Using schemaName from source_object_info Map: {}", oracleOwner);
                    } else if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        oracleOwner = sourceInfo.get("owner").toString().trim();
                        log.info("Strategy 4 - Using owner from source_object_info Map: {}", oracleOwner);
                    }
                } else {
                    // If it's a String, parse it
                    String jsonString = api.getSourceObjectInfo().toString();
                    Map<String, Object> sourceInfo = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                    if (sourceInfo.containsKey("schemaName") && sourceInfo.get("schemaName") != null) {
                        oracleOwner = sourceInfo.get("schemaName").toString().trim();
                        log.info("Strategy 4 - Using schemaName from source_object_info JSON: {}", oracleOwner);
                    } else if (sourceInfo.containsKey("owner") && sourceInfo.get("owner") != null) {
                        oracleOwner = sourceInfo.get("owner").toString().trim();
                        log.info("Strategy 4 - Using owner from source_object_info JSON: {}", oracleOwner);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not process sourceObjectInfo: {}", e.getMessage());
            }
        }

        // Strategy 5: Try to get current user's default schema
        if (oracleOwner == null) {
            try {
                String currentSchema = oracleJdbcTemplate.queryForObject(
                        "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL",
                        String.class);
                oracleOwner = currentSchema;
                log.info("Strategy 5 - Using current schema from Oracle: {}", oracleOwner);
            } catch (Exception e) {
                log.warn("Could not get current schema: {}", e.getMessage());
            }
        }

        // Strategy 6: Last resort - try common schemas
        if (oracleOwner == null) {
            String[] commonSchemas = {"CBXDMX", "CBX_APIGEN", "SYSTEM", "SYS"};
            for (String schema : commonSchemas) {
                try {
                    String checkSql = "SELECT COUNT(*) FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND ROWNUM = 1";
                    Integer count = oracleJdbcTemplate.queryForObject(checkSql, Integer.class, schema, procedureName);
                    if (count != null && count > 0) {
                        oracleOwner = schema;
                        log.info("Strategy 6 - Found object in common schema: {}", schema);
                        break;
                    }
                } catch (Exception e) {
                    // Ignore and try next
                }
            }
        }

        if (oracleOwner == null || oracleOwner.trim().isEmpty()) {
            log.error("❌ COULD NOT DETERMINE OWNER/SCHEMA NAME");
            throw new ValidationException(
                    "Could not determine the database schema/owner for procedure: " + procedureName
            );
        }

        oracleOwner = oracleOwner.toUpperCase();
        String oracleProcedureName = procedureName != null ? procedureName.trim().toUpperCase() : null;

        log.info("Final resolved owner before synonym resolution: {}", oracleOwner);
        log.info("Final procedure name before synonym resolution: {}", oracleProcedureName);

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
        // Pass the mapped DB parameters to validation
        try {
            parameterValidatorUtil.validateParameters(configuredParamDTOs, dbParams, actualOwner, actualProcedureName);
            log.info("✅ All parameter validations passed for procedure {}.{}", actualOwner, actualProcedureName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

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
                    String dbParamName = null;
                    if (param.getDbParameter() != null && !param.getDbParameter().isEmpty()) {
                        dbParamName = param.getDbParameter().toUpperCase();
                    } else if (param.getDbColumn() != null && !param.getDbColumn().isEmpty()) {
                        dbParamName = param.getDbColumn().toUpperCase();
                    } else {
                        dbParamName = param.getKey().toUpperCase();
                    }

                    // Check if this parameter is meant to be an IN parameter
                    boolean isInParameter = paramMode.contains("IN") || paramType == null ||
                            "query".equals(paramType) || "path".equals(paramType) || "body".equals(paramType);

                    if (dbParams.containsKey(dbParamName) && isInParameter) {
                        int sqlType = mapToSqlType(param.getOracleType());
                        jdbcCall.declareParameters(new SqlParameter(dbParamName, sqlType));

                        log.debug("Declared IN parameter: {} of type: {} (SQL type: {}) with value: {}",
                                dbParamName, param.getOracleType(), sqlType, dbParams.get(dbParamName));
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

            log.info("Procedure executed successfully, result contains {} keys: {}",
                    result.size(), result.keySet());

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
            if (e.getMessage() != null) {
                if (e.getMessage().contains("ORA-06550")) {
                    throw new ValidationException(
                            String.format("Invalid parameters provided for procedure '%s.%s'. Please check parameter names and data types.",
                                    actualOwner, actualProcedureName)
                    );
                }
                if (e.getMessage().contains("ORA-00942")) {
                    throw new ValidationException(
                            String.format("Table or view referenced in procedure '%s.%s' could not be found.",
                                    actualOwner, actualProcedureName)
                    );
                }
                if (e.getMessage().contains("ORA-01031")) {
                    throw new ValidationException(
                            String.format("Insufficient privileges to execute procedure '%s.%s'.",
                                    actualOwner, actualProcedureName)
                    );
                }
                if (e.getMessage().contains("ORA-01400")) {
                    throw new ValidationException(
                            "A required value is missing for a NOT NULL column. Please provide all required parameters."
                    );
                }
                if (e.getMessage().contains("Invalid column type")) {
                    // Try to extract which parameter had the issue
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("'(\\w+)'\\s*=\\s*\\[?([^\\]]+)\\]?");
                    java.util.regex.Matcher matcher = pattern.matcher(e.getMessage());
                    if (matcher.find()) {
                        String paramName = matcher.group(1);
                        throw new ValidationException(
                                String.format("Invalid format for parameter '%s'. Please provide a single value, not a list.",
                                        paramName)
                        );
                    }
                    throw new ValidationException(
                            "Invalid parameter format. Please check the data types of your parameters."
                    );
                }
            }

            throw new RuntimeException("Failed to execute the requested operation.", e);
        }
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
}