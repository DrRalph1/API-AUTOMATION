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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewExecutorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final ParameterValidatorUtil parameterValidator;
    private final OracleObjectResolverUtil objectResolver;
    private final TableExecutorUtil tableExecutorUtil;

    public Object execute(GeneratedApiEntity api, ApiSourceObjectDTO sourceObject,
                          String viewName, String owner, ExecuteApiRequestDTO request,
                          List<ApiParameterDTO> configuredParamDTOs) {

        // FIX: Combine ALL parameters - path, query, and body
        Map<String, Object> allParams = new HashMap<>();

        // Add path params
        if (request.getPathParams() != null) {
            allParams.putAll(request.getPathParams());
            log.info("Added path params: {}", request.getPathParams());
        }

        // Add query params
        if (request.getQueryParams() != null) {
            allParams.putAll(request.getQueryParams());
            log.info("Added query params: {}", request.getQueryParams());
        }

        // Add body params if body is a map
        if (request.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
            if (bodyMap != null) {
                allParams.putAll(bodyMap);
                log.info("Added body params: {}", bodyMap);
            }
        }

        log.info("Combined all params for view execution: {}", allParams);

        // Handle collection/array parameters in all params
        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || value.getClass().isArray()) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value for WHERE clause
                    allParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value", entry.getKey());
                }
            }
        }

        // Resolve the actual owner if null
        String resolvedOwner = resolveOwner(owner, sourceObject);
        String resolvedViewName = viewName != null ? viewName.toUpperCase() : null;

        if (resolvedViewName == null) {
            throw new ValidationException("View name cannot be null");
        }

        log.info("Resolved view owner: {}, view name: {}", resolvedOwner, resolvedViewName);

        // ==================== VALIDATION STEP 1: Check if it's a synonym first ====================
        Map<String, Object> resolutionResult = objectResolver.resolveObject(resolvedOwner, resolvedViewName, "VIEW");

        if (!(boolean) resolutionResult.getOrDefault("exists", false)) {
            String errorMsg = String.format("The view '%s.%s' does not exist or you don't have access to it.",
                    resolvedOwner, resolvedViewName);
            log.error("❌ {}", errorMsg);
            throw new ValidationException(errorMsg);
        }

        // Get the resolved target information
        String targetOwner = (String) resolutionResult.get("targetOwner");
        String targetName = (String) resolutionResult.get("targetName");
        String targetType = (String) resolutionResult.get("targetType");
        boolean isSynonym = (boolean) resolutionResult.getOrDefault("isSynonym", false);

        log.info("Resolved to: {}.{} ({}) {}", targetOwner, targetName, targetType,
                isSynonym ? "(via synonym)" : "");

        // If it resolved to something other than a VIEW, that's a problem
        if (!"VIEW".equalsIgnoreCase(targetType)) {
            throw new ValidationException(
                    String.format("Object '%s.%s' resolved to %s, but VIEW was expected",
                            resolvedOwner, resolvedViewName, targetType)
            );
        }

        // ==================== VALIDATION STEP 2: Validate view is accessible ====================
        try {
            // Use the resolved target for validation
            objectResolver.validateDatabaseObject(targetOwner, targetName, "VIEW");
            log.info("✅ View {}.{} exists and is accessible", targetOwner, targetName);
        } catch (ValidationException e) {
            log.error("❌ View validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== VALIDATION STEP 3: Validate query parameters against view columns ====================
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

            validateViewQuery(targetOwner, targetName, allParams, allowedColumns);
            log.info("✅ View query validation passed");
        } catch (ValidationException e) {
            log.error("❌ View query validation failed: {}", e.getMessage());
            throw e;
        }

        // ==================== VALIDATION STEP 4: Validate all parameters ====================
        try {
            parameterValidator.validateParameters(configuredParamDTOs, allParams, targetOwner, targetName);
            log.info("✅ All parameter validations passed for view {}.{}", targetOwner, targetName);
        } catch (ValidationException e) {
            log.error("❌ Parameter validation failed: {}", e.getMessage());
            throw e;
        }

        // FIX: Pass ALL combined parameters to TableExecutorUtil
        return tableExecutorUtil.executeSelect(targetName, targetOwner, allParams, api, configuredParamDTOs);
    }

    /**
     * Helper method to resolve the owner from multiple sources
     */
    private String resolveOwner(String owner, ApiSourceObjectDTO sourceObject) {
        // Try in order: explicit owner parameter, sourceObject owner, sourceObject schemaName, current schema
        if (owner != null && !owner.trim().isEmpty()) {
            return owner.trim().toUpperCase();
        }

        if (sourceObject != null) {
            if (sourceObject.getOwner() != null && !sourceObject.getOwner().trim().isEmpty()) {
                return sourceObject.getOwner().trim().toUpperCase();
            }
            if (sourceObject.getSchemaName() != null && !sourceObject.getSchemaName().trim().isEmpty()) {
                return sourceObject.getSchemaName().trim().toUpperCase();
            }
        }

        // Try to get current schema as last resort
        try {
            String currentSchema = objectResolver.getCurrentSchema();
            if (currentSchema != null && !currentSchema.isEmpty()) {
                log.info("Using current schema as owner: {}", currentSchema);
                return currentSchema;
            }
        } catch (Exception e) {
            log.warn("Could not get current schema: {}", e.getMessage());
        }

        // If all else fails, return null (but the resolver should handle it)
        return null;
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