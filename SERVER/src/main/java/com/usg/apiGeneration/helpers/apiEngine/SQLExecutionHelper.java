package com.usg.apiGeneration.helpers.apiEngine;

import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiGeneration.utils.apiEngine.SQLParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SQLExecutionHelper {

    private final SQLParserUtil sqlParser;

    public Object executeSQL(GeneratedApiEntity api,
                             ExecuteApiRequestDTO request,
                             List<ApiParameterDTO> configuredParamDTOs,
                             JdbcTemplate jdbcTemplate) {

        String sql = api.getSourceSql();
        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("No SQL defined for this API");
        }

        // Parse SQL to understand structure
        SQLParserUtil.ParsedSQL parsedSQL = sqlParser.parseSQL(sql, api.getDatabaseType());

        // Build parameter list
        List<Object> parameters = new ArrayList<>();
        Map<String, Object> parameterValues = extractParameterValues(request, configuredParamDTOs);

        // Create parameterized SQL
        String parameterizedSQL = sqlParser.toParameterizedSQL(sql, parsedSQL.getParameters());

        // Add parameters in order
        for (SQLParserUtil.SQLParameter param : parsedSQL.getParameters()) {
            Object value = parameterValues.get(param.getName());
            if (value == null && param.isRequired()) {
                throw new RuntimeException("Missing required parameter: " + param.getName());
            }
            parameters.add(value);
        }

        log.info("Executing SQL: {}", parameterizedSQL);
        log.info("Parameters: {}", parameters);

        // Execute based on operation type
        switch (parsedSQL.getOperationType()) {
            case "SELECT":
                return executeSelect(parameterizedSQL, parameters, jdbcTemplate);
            case "INSERT":
                return executeUpdate(parameterizedSQL, parameters, jdbcTemplate);
            case "UPDATE":
                return executeUpdate(parameterizedSQL, parameters, jdbcTemplate);
            case "DELETE":
                return executeUpdate(parameterizedSQL, parameters, jdbcTemplate);
            default:
                throw new RuntimeException("Unsupported SQL operation: " + parsedSQL.getOperationType());
        }
    }

    private Map<String, Object> extractParameterValues(ExecuteApiRequestDTO request,
                                                       List<ApiParameterDTO> configuredParamDTOs) {
        Map<String, Object> values = new HashMap<>();

        // Extract from path params
        if (request.getPathParams() != null) {
            values.putAll(request.getPathParams());
        }

        // Extract from query params
        if (request.getQueryParams() != null) {
            values.putAll(request.getQueryParams());
        }

        // Extract from body
        if (request.getBody() instanceof Map) {
            values.putAll((Map<String, Object>) request.getBody());
        }

        // Extract from headers
        if (request.getHeaders() != null) {
            values.putAll(request.getHeaders());
        }

        return values;
    }

    private List<Map<String, Object>> executeSelect(String sql, List<Object> parameters,
                                                    JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForList(sql, parameters.toArray());
    }

    private Map<String, Object> executeUpdate(String sql, List<Object> parameters,
                                              JdbcTemplate jdbcTemplate) {
        int rowsAffected = jdbcTemplate.update(sql, parameters.toArray());
        Map<String, Object> result = new HashMap<>();
        result.put("rowsAffected", rowsAffected);
        result.put("success", rowsAffected > 0);
        return result;
    }
}