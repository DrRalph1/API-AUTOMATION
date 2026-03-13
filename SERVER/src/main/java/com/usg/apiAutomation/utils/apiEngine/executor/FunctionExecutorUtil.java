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
public class FunctionExecutorUtil {

    private final JdbcTemplate oracleJdbcTemplate;
    private final ParameterValidatorUtil parameterValidatorUtil;
    private final OracleObjectResolverUtil objectResolver;

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String functionName, String owner, ExecuteApiRequestDTO request,
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

        log.info("Executing function - {}.{} with params: {}", owner, functionName, inParams);

        String oracleOwner = owner != null && !owner.trim().isEmpty() ? owner.trim().toUpperCase() : null;
        String oracleFunctionName = functionName != null ? functionName.toUpperCase() : null;

        // ==================== VALIDATION STEP 1: Validate function exists and is valid ====================
        try {
            objectResolver.validateDatabaseObject(oracleOwner, oracleFunctionName, "FUNCTION");
            log.info("✅ Function {}.{} exists and is valid", oracleOwner, oracleFunctionName);
        } catch (EmptyResultDataAccessException e) {
            log.error("❌ Function validation failed: {}", e.getMessage());
            throw new ValidationException(
                    String.format("The function '%s.%s' does not exist or you don't have access to it.",
                            oracleOwner, oracleFunctionName)
            );
        }

        // ==================== VALIDATION STEP 2: Validate all parameters ====================
        try {
            parameterValidatorUtil.validateParameters(configuredParamDTOs, inParams, oracleOwner, oracleFunctionName);
            log.info("✅ All parameter validations passed for function {}.{}", oracleOwner, oracleFunctionName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                    .withSchemaName(oracleOwner)
                    .withFunctionName(oracleFunctionName);

            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                String returnType = api.getResponseMappings().get(0).getOracleType();
                jdbcCall.declareParameters(
                        new SqlOutParameter("return", mapToSqlType(returnType))
                );
            }

            // Declare input parameters from API parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                int inParamCount = 0;

                for (ApiParameterEntity param : api.getParameters()) {
                    // Skip if parameter is null or has no key
                    if (param == null || param.getKey() == null) continue;

                    // Handle null parameterType - treat as valid IN parameter
                    String paramType = param.getParameterType();
                    boolean isValidParameter = paramType == null ||
                            "query".equals(paramType) ||
                            "path".equals(paramType) ||
                            "body".equals(paramType);

                    if (inParams.containsKey(param.getKey()) && isValidParameter) {
                        String paramName = param.getDbParameter() != null ?
                                param.getDbParameter() :
                                (param.getDbColumn() != null ? param.getDbColumn() : param.getKey());

                        jdbcCall.declareParameters(
                                new SqlParameter(paramName, mapToSqlType(param.getOracleType()))
                        );
                        inParamCount++;
                        log.debug("Declared IN parameter: {} with value: {}", paramName, inParams.get(param.getKey()));
                    }
                }

                log.debug("Declared {} IN parameters for function", inParamCount);
            }

            log.info("Executing function {}.{} with {} input parameters", oracleOwner, oracleFunctionName, inParams.size());

            Map<String, Object> result = jdbcCall.execute(inParams);

            Map<String, Object> responseData = new HashMap<>();
            if (result.containsKey("return")) {
                responseData.put("result", result.get("return"));
                log.info("Function returned value: {}", result.get("return"));
            }

            return responseData;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing function {}.{}: {}", oracleOwner, oracleFunctionName, e.getMessage(), e);

            // Provide user-friendly error messages
            if (e.getMessage() != null && e.getMessage().contains("ORA-06550")) {
                throw new ValidationException("Invalid parameters provided for the function. Please check parameter names and data types.");
            }
            if (e.getMessage() != null && e.getMessage().contains("ORA-00942")) {
                throw new ValidationException("The requested table or view could not be found.");
            }
            if (e.getMessage() != null && e.getMessage().contains("ORA-01031")) {
                throw new ValidationException("Insufficient privileges to execute this function.");
            }
            if (e.getMessage() != null && e.getMessage().contains("Invalid column type")) {
                throw new ValidationException("Invalid parameter format. Please check the data types of your parameters.");
            }

            throw new RuntimeException("Failed to execute the requested function.", e);
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