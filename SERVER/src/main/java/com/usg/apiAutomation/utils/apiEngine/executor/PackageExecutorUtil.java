package com.usg.apiAutomation.utils.apiEngine.executor;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PackageExecutorUtil {

    private final JdbcTemplate oracleJdbcTemplate;

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String packageName, String owner, ExecuteApiRequestDTO request) {
        if (sourceObject.getPackageProcedure() == null) {
            throw new RuntimeException("Package procedure not specified");
        }

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

        log.info("Executing package procedure - {}.{} with params: {}", packageName,
                sourceObject.getPackageProcedure(), inParams);

        // Ensure package and procedure names are in uppercase for Oracle
        String oraclePackageName = packageName != null ? packageName.toUpperCase() : null;
        String oracleProcedureName = sourceObject.getPackageProcedure() != null ?
                sourceObject.getPackageProcedure().toUpperCase() : null;

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(oracleJdbcTemplate)
                    .withSchemaName(owner)
                    .withCatalogName(oraclePackageName)
                    .withProcedureName(oracleProcedureName);

            if (api.getResponseMappings() != null) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                        jdbcCall.declareParameters(
                                new SqlOutParameter(mapping.getDbColumn(), mapToSqlType(mapping.getOracleType()))
                        );
                    }
                }
            }

            // Declare input parameters from API parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                int inParamCount = 0;

                for (ApiParameterEntity param : api.getParameters()) {
                    // Skip if parameter is null or has no key
                    if (param == null || param.getKey() == null) continue;

                    // Handle null parameterType - treat as valid IN parameter
                    String paramType = param.getParameterType();
                    String paramMode = param.getParamMode() != null ? param.getParamMode().toUpperCase() : "IN";

                    boolean isValidParameter = (paramMode.contains("IN") || paramType == null) &&
                            (paramType == null || "query".equals(paramType) ||
                                    "path".equals(paramType) || "body".equals(paramType));

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

                log.debug("Declared {} IN parameters for package procedure", inParamCount);
            }

            log.info("Executing package procedure {}.{}.{} with {} input parameters",
                    owner, oraclePackageName, oracleProcedureName, inParams.size());

            Map<String, Object> result = jdbcCall.execute(inParams);

            Map<String, Object> responseData = new HashMap<>();
            if (api.getResponseMappings() != null) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (result.containsKey(mapping.getDbColumn())) {
                        responseData.put(mapping.getApiField(), result.get(mapping.getDbColumn()));
                        log.debug("Mapped output {} to {}", mapping.getDbColumn(), mapping.getApiField());
                    }
                }
            }

            return responseData;

        } catch (Exception e) {
            log.error("Error executing package procedure {}.{}.{}: {}",
                    owner, oraclePackageName, oracleProcedureName, e.getMessage(), e);

            // Provide user-friendly error messages
            if (e.getMessage() != null && e.getMessage().contains("no procedure/function/signature")) {
                throw new RuntimeException("The requested package procedure could not be found.");
            }

            if (e.getMessage() != null && e.getMessage().contains("ORA-06550")) {
                throw new RuntimeException("Invalid parameters provided for the package procedure.");
            }

            throw new RuntimeException("Failed to execute the requested package procedure.", e);
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