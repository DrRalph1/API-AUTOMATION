package com.usg.apiAutomation.utils.apiEngine.executor;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.apiAutomation.utils.apiEngine.ParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcedureExecutorUtil {

    private final JdbcTemplate oracleJdbcTemplate;
    private final ParameterValidatorUtil parameterValidatorUtil;
    private final OracleObjectResolverUtil objectResolver;

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String procedureName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {
        // Create consolidated parameters map from all sources
        Map<String, Object> inParams = new HashMap<>();

        // Add path parameters
        if (request.getPathParams() != null) {
            inParams.putAll(request.getPathParams());
        }

        // Add query parameters
        if (request.getQueryParams() != null) {
            inParams.putAll(request.getQueryParams());
        }

        // Add body parameters if it's a map
        if (request.getBody() instanceof Map) {
            inParams.putAll((Map<String, Object>) request.getBody());
        }

        // Handle collection/array parameters - convert to single values for database
        for (Map.Entry<String, Object> entry : inParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || value.getClass().isArray()) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value
                    inParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value", entry.getKey());
                } else {
                    inParams.put(entry.getKey(), null);
                }
            }
        }

        log.info("Executing procedure - Original: {}.{} with params: {}", owner, procedureName, inParams);

        String oracleOwner = owner != null && !owner.trim().isEmpty() ? owner.trim().toUpperCase() : null;
        String oracleProcedureName = procedureName != null ? procedureName.trim().toUpperCase() : null;

        // ==================== VALIDATION STEP 1: Validate procedure exists and is valid ====================
        try {
            objectResolver.validateDatabaseObject(oracleOwner, oracleProcedureName, "PROCEDURE");
            log.info("✅ Procedure {}.{} exists and is valid", oracleOwner, oracleProcedureName);
        } catch (EmptyResultDataAccessException e) {
            log.error("❌ Procedure {}.{} does not exist", oracleOwner, oracleProcedureName);
            throw new ValidationException(
                    String.format("The procedure '%s.%s' does not exist or you don't have access to it.",
                            oracleOwner, oracleProcedureName)
            );
        }

        // ==================== VALIDATION STEP 2: Validate all parameters ====================
        try {
            parameterValidatorUtil.validateParameters(configuredParamDTOs, inParams, oracleOwner, oracleProcedureName);
            log.info("✅ All parameter validations passed for procedure {}.{}", oracleOwner, oracleProcedureName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // Resolve the actual target (handle synonyms)
        Map<String, Object> resolution = objectResolver.resolveProcedureTarget(oracleOwner, oracleProcedureName);

        String actualOwner;
        String actualProcedureName;

        if ((boolean) resolution.getOrDefault("isSynonym", false)) {
            actualOwner = (String) resolution.get("targetOwner");
            actualProcedureName = (String) resolution.get("targetName");
            log.info("Resolved synonym to: {}.{}", actualOwner, actualProcedureName);

            // Validate the resolved target as well
            try {
                objectResolver.validateDatabaseObject(actualOwner, actualProcedureName, "PROCEDURE");
                log.info("✅ Resolved procedure {}.{} is valid", actualOwner, actualProcedureName);
            } catch (ValidationException e) {
                log.error("❌ Resolved procedure validation failed: {}", e.getMessage());
                throw e;
            }
        } else {
            actualOwner = oracleOwner;
            actualProcedureName = oracleProcedureName;
        }

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate);

            // Set schema and procedure name - use resolved actual owner
            if (actualOwner != null && !actualOwner.isEmpty()) {
                jdbcCall = jdbcCall.withSchemaName(actualOwner);
            }

            jdbcCall = jdbcCall.withProcedureName(actualProcedureName);

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

            // Declare input parameters from API parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                int inParamCount = 0;

                for (ApiParameterEntity param : api.getParameters()) {
                    // Skip if parameter is null or has no key
                    if (param == null || param.getKey() == null) continue;

                    String paramType = param.getParameterType();
                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";

                    // Check if this parameter is meant to be an IN parameter
                    boolean isInParameter = paramMode.contains("IN") || paramType == null ||
                            "query".equals(paramType) || "path".equals(paramType) || "body".equals(paramType);

                    if (inParams.containsKey(param.getKey()) && isInParameter) {
                        String paramName = param.getDbParameter() != null && !param.getDbParameter().isEmpty() ?
                                param.getDbParameter().toUpperCase() :
                                (param.getDbColumn() != null && !param.getDbColumn().isEmpty() ?
                                        param.getDbColumn().toUpperCase() : param.getKey().toUpperCase());

                        int sqlType = mapToSqlType(param.getOracleType());
                        jdbcCall.declareParameters(new SqlParameter(paramName, sqlType));

                        log.debug("Declared IN parameter: {} of type: {} (SQL type: {}) with value: {}",
                                paramName, param.getOracleType(), sqlType, inParams.get(param.getKey()));
                        inParamCount++;
                    } else if (inParams.containsKey(param.getKey())) {
                        log.debug("Parameter {} is not an IN parameter (mode: {}), skipping", param.getKey(), paramMode);
                    }
                }

                log.debug("Declared {} IN parameters", inParamCount);
            }

            log.info("Executing SimpleJdbcCall for {}.{} with {} input parameters",
                    actualOwner != null ? actualOwner : "<default>", actualProcedureName, inParams.size());

            // Execute the procedure
            Map<String, Object> result = jdbcCall.execute(inParams);

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