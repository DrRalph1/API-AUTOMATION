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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewExecutorUtil {

    private final JdbcTemplate oracleJdbcTemplate;
    private final ParameterValidatorUtil parameterValidator;
    private final OracleObjectResolverUtil objectResolver;
    private final TableExecutorUtil tableExecutorUtil;

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String viewName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {
        Map<String, Object> queryParams = request.getQueryParams() != null ?
                request.getQueryParams() : new HashMap<>();

        // Handle collection/array parameters in query params
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || value.getClass().isArray()) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value for WHERE clause
                    queryParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection query parameter '{}' to single value", entry.getKey());
                }
            }
        }

        String oracleOwner = owner != null && !owner.trim().isEmpty() ? owner.trim().toUpperCase() : null;
        String oracleViewName = viewName != null ? viewName.toUpperCase() : null;

        // ==================== VALIDATION STEP 1: Validate view exists and is accessible ====================
        try {
            objectResolver.validateDatabaseObject(oracleOwner, oracleViewName, "VIEW");
            log.info("✅ View {}.{} exists and is accessible", oracleOwner, oracleViewName);
        } catch (ValidationException e) {
            log.error("❌ View validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== VALIDATION STEP 2: Validate query parameters against view columns ====================
        try {
            // Get allowed columns from response mappings or API parameters
            List<String> allowedColumns = new ArrayList<>();
            if (api.getResponseMappings() != null) {
                for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                    if (mapping.getDbColumn() != null) {
                        allowedColumns.add(mapping.getDbColumn().toLowerCase());
                    }
                }
            }

            validateViewQuery(oracleOwner, oracleViewName, queryParams, allowedColumns);
            log.info("✅ View query validation passed");
        } catch (ValidationException e) {
            log.error("❌ View query validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== VALIDATION STEP 3: Validate all parameters ====================
        try {
            parameterValidator.validateParameters(configuredParamDTOs, queryParams, oracleOwner, oracleViewName);
            log.info("✅ All parameter validations passed for view {}.{}", oracleOwner, oracleViewName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        return tableExecutorUtil.executeSelect(oracleViewName, oracleOwner, queryParams, api, configuredParamDTOs);
    }

    private void validateViewQuery(String schemaName, String viewName, Map<String, Object> queryParams,
                                   List<String> allowedColumns) {
        // Check if view exists
        String sql = "SELECT COUNT(*) FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME = ?";

        Integer count = oracleJdbcTemplate.queryForObject(sql, Integer.class, schemaName, viewName);
        if (count == 0) {
            throw new ValidationException(
                    String.format("View '%s.%s' does not exist", schemaName, viewName)
            );
        }

        // Validate view is accessible
        try {
            oracleJdbcTemplate.execute("SELECT 1 FROM " + schemaName + "." + viewName + " WHERE ROWNUM = 1");
        } catch (Exception e) {
            throw new ValidationException(
                    String.format("Cannot access view '%s.%s': %s",
                            schemaName, viewName, e.getMessage())
            );
        }

        // Validate query parameters against view columns
        if (queryParams != null && !queryParams.isEmpty()) {
            // Get view columns
            String columnSql =
                    "SELECT COLUMN_NAME, DATA_TYPE, NULLABLE " +
                            "FROM ALL_TAB_COLUMNS " +
                            "WHERE OWNER = ? AND TABLE_NAME = ?";

            List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(
                    columnSql, schemaName, viewName);

            Map<String, Map<String, Object>> columnMap = new HashMap<>();
            for (Map<String, Object> column : columns) {
                columnMap.put(((String) column.get("COLUMN_NAME")).toLowerCase(), column);
            }

            // Validate each query parameter
            for (Map.Entry<String, Object> param : queryParams.entrySet()) {
                String paramName = param.getKey().toLowerCase();

                // Check if parameter corresponds to a view column
                Map<String, Object> column = columnMap.get(paramName);
                if (column == null && allowedColumns != null && !allowedColumns.contains(paramName)) {
                    throw new ValidationException(
                            String.format("Invalid query parameter '%s'. Not a valid column in view %s.%s",
                                    param.getKey(), schemaName, viewName)
                    );
                }

                // Validate data type if column exists
                if (column != null && param.getValue() != null) {
                    validateColumnDataType(param.getKey(), param.getValue(), column);
                }
            }
        }
    }

    private void validateColumnDataType(String columnName, Object value, Map<String, Object> column) {
        String dataType = (String) column.get("DATA_TYPE");
        String nullable = (String) column.get("NULLABLE");

        // Check for NOT NULL constraint
        if ("N".equals(nullable) && (value == null || value.toString().trim().isEmpty())) {
            throw new ValidationException(
                    String.format("Column '%s' cannot be null", columnName)
            );
        }

        // Additional data type validation would go here
    }
}