package com.usg.apiAutomation.utils.apiEngine.executor;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.utils.apiEngine.OracleObjectResolverUtil;
import com.usg.apiAutomation.utils.apiEngine.ParameterValidator;
import com.usg.apiAutomation.utils.apiEngine.ParameterValidatorUtil;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TableExecutorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final ParameterValidatorUtil parameterValidator;

    public TableExecutorUtil(
            ParameterValidatorUtil parameterValidator) {
        this.parameterValidator = parameterValidator;
    }

    public Object executeSelect(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api, List<ApiParameterDTO> configuredParamDTOs) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM ");
            if (owner != null && !owner.isEmpty()) {
                sql.append(owner).append(".");
            }
            sql.append(tableName);

            List<Object> paramValues = new ArrayList<>();
            List<String> whereClauses = new ArrayList<>();

            log.info("=== TABLE SELECT DEBUG ===");
            log.info("Table: {}.{}", owner, tableName);
            log.info("All incoming params: {}", params);

            // Build a clean parameter map - keep ALL parameters
            Map<String, Object> cleanParams = new HashMap<>();
            if (params != null) {
                // Simply copy all parameters
                cleanParams.putAll(params);

                // Also handle any numbered parameters (param1, param2, etc.) that might come from path
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("param") && key.length() > 5) {
                        try {
                            int position = Integer.parseInt(key.substring(5)) - 1;
                            cleanParams.put("position_" + position, entry.getValue());
                            log.info("Mapped numbered param {} to position {}", key, position);
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            }

            log.info("Cleaned params: {}", cleanParams);

            // Build WHERE clause from configured parameters
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                log.info("Processing {} configured parameters", api.getParameters().size());

                for (ApiParameterEntity configuredParam : api.getParameters()) {
                    String paramKey = configuredParam.getKey();
                    String dbColumn = configuredParam.getDbColumn();
                    String paramType = configuredParam.getParameterType();

                    // Default to "query" if paramType is null
                    if (paramType == null) {
                        paramType = "query";
                        log.debug("Parameter {} has null paramType, defaulting to 'query'", paramKey);
                    }

                    if (dbColumn == null || dbColumn.isEmpty()) {
                        log.debug("Parameter {} has no dbColumn mapping, skipping", paramKey);
                        continue;
                    }

                    boolean isValidForFiltering = "query".equals(paramType) ||
                            "path".equals(paramType) ||
                            "body".equals(paramType);

                    if (isValidForFiltering) {
                        log.info("Processing parameter: key='{}', dbColumn='{}', paramType='{}', required={}",
                                paramKey, dbColumn, paramType, configuredParam.getRequired());

                        Object value = null;

                        // Try to find value by direct key match
                        if (cleanParams.containsKey(paramKey)) {
                            value = cleanParams.get(paramKey);
                            log.info("  Found exact match for key '{}' with value: {}", paramKey, value);
                        }

                        // Try case-insensitive match
                        if (value == null) {
                            for (Map.Entry<String, Object> entry : cleanParams.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(paramKey)) {
                                    value = entry.getValue();
                                    log.info("  Found case-insensitive match for key '{}' with value: {}", paramKey, value);
                                    break;
                                }
                            }
                        }

                        // Try to find by position if this is a path parameter
                        if (value == null && "path".equals(paramType)) {
                            Integer position = configuredParam.getPosition();
                            if (position != null) {
                                String positionKey = "position_" + position;
                                if (cleanParams.containsKey(positionKey)) {
                                    value = cleanParams.get(positionKey);
                                    log.info("  Found by position {} with value: {}", position, value);
                                }
                            }
                        }

                        if (value != null) {
                            // Handle collection/array values
                            if (value instanceof List || value.getClass().isArray()) {
                                Collection<?> collection = value instanceof List ?
                                        (List<?>) value : Arrays.asList((Object[]) value);

                                if (!collection.isEmpty()) {
                                    value = collection.iterator().next();
                                    log.info("  Converted collection to single value: {}", value);
                                } else {
                                    value = null;
                                }
                            }

                            if (value != null && !value.toString().trim().isEmpty()) {
                                whereClauses.add(dbColumn + " = ?");
                                paramValues.add(value);
                                log.info("  ADDED FILTER: {} = ? with value: {}", dbColumn, value);
                            } else {
                                if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                                    throw new ValidationException(
                                            String.format("Required parameter '%s' cannot be empty", paramKey)
                                    );
                                }
                                log.info("Optional parameter {} not provided or empty, skipping filter", paramKey);
                            }
                        } else {
                            if (Boolean.TRUE.equals(configuredParam.getRequired())) {
                                // FIX: Better error message showing all available params
                                throw new ValidationException(
                                        String.format("Required parameter '%s' is missing. Available params: %s. Request params: %s",
                                                paramKey, cleanParams.keySet(), params.keySet())
                                );
                            }
                            log.info("Optional parameter {} not provided, skipping filter", paramKey);
                        }
                    } else {
                        log.info("Parameter {} with type '{}' is not for filtering, skipping", paramKey, paramType);
                    }
                }
            }

            // Add WHERE clause if we have conditions
            if (!whereClauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
            }

            // Handle pagination if enabled
            if (api.getSchemaConfig() != null &&
                    Boolean.TRUE.equals(api.getSchemaConfig().getEnablePagination())) {
                int pageSize = api.getSchemaConfig().getPageSize() != null ?
                        api.getSchemaConfig().getPageSize() : 10;
                int page = 1;

                if (params != null && params.containsKey("page")) {
                    try {
                        page = Integer.parseInt(params.get("page").toString());
                        if (page < 1) page = 1;
                        log.info("Page parameter found: {}", page);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid page parameter, using default: 1");
                    }
                }

                int offset = (page - 1) * pageSize;
                sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
                paramValues.add(offset);
                paramValues.add(pageSize);
                log.info("Added pagination: offset={}, pageSize={}", offset, pageSize);
            }

            log.info("Final SQL: {} with {} parameters", sql.toString(), paramValues.size());

            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(
                    sql.toString(), paramValues.toArray());
            log.info("Query returned {} rows", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error executing table select: {}", e.getMessage(), e);

            if (e.getMessage() != null && e.getMessage().contains("Invalid column type")) {
                if (params != null) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getValue() instanceof List || entry.getValue() instanceof Collection) {
                            throw new ValidationException(
                                    String.format("Parameter '%s' cannot accept multiple values. Please provide a single value.",
                                            entry.getKey())
                            );
                        }
                    }
                }
                throw new ValidationException("Invalid parameter format. Please check the data types of your parameters.");
            }

            throw new RuntimeException("Failed to execute SELECT operation: " + e.getMessage(), e);
        }
    }

    public Object executeInsert(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for INSERT operation");
        }

        // Handle collection/array parameters - convert to single values for database
        Map<String, Object> processedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || value.getClass().isArray()) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value
                    processedParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value for INSERT", entry.getKey());
                } else {
                    processedParams.put(entry.getKey(), null);
                }
            } else {
                processedParams.put(entry.getKey(), value);
            }
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("?");
            paramValues.add(entry.getValue());
        }

        String sql = "INSERT INTO " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                " (" + columns + ") VALUES (" + values + ")";

        try {
            int rowsAffected = oracleJdbcTemplate.update(sql, paramValues.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Insert successful" : "No rows inserted");

            if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
                String pkColumn = api.getResponseMappings().stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                        .map(ApiResponseMappingEntity::getDbColumn)
                        .findFirst()
                        .orElse(null);

                if (pkColumn != null && processedParams.containsKey(pkColumn.toLowerCase())) {
                    String selectSql = "SELECT * FROM " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                            " WHERE " + pkColumn + " = ?";
                    List<Map<String, Object>> inserted = oracleJdbcTemplate.queryForList(
                            selectSql, processedParams.get(pkColumn.toLowerCase()));
                    if (!inserted.isEmpty()) {
                        result.put("data", inserted.get(0));
                    }
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Error executing INSERT on {}: {}", tableName, e.getMessage(), e);

            // Provide user-friendly error messages
            if (e.getMessage() != null && e.getMessage().contains("ORA-00942")) {
                throw new RuntimeException("The requested table could not be found.");
            }

            if (e.getMessage() != null && e.getMessage().contains("ORA-01031")) {
                throw new RuntimeException("Insufficient privileges to insert into this table.");
            }

            if (e.getMessage() != null && e.getMessage().contains("ORA-01400")) {
                throw new RuntimeException("A required value is missing for a non-nullable column.");
            }

            if (e.getMessage() != null && e.getMessage().contains("ORA-02291")) {
                throw new RuntimeException("Referential integrity constraint violation - parent record not found.");
            }

            if (e.getMessage() != null && e.getMessage().contains("ORA-12899")) {
                throw new RuntimeException("Value too large for the target column.");
            }

            throw new RuntimeException("Failed to execute INSERT operation.", e);
        }
    }

    public Object executeUpdate(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for UPDATE operation");
        }

        List<String> pkColumns = api.getResponseMappings().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimaryKey()))
                .map(ApiResponseMappingEntity::getDbColumn)
                .collect(Collectors.toList());

        if (pkColumns.isEmpty()) {
            throw new RuntimeException("No primary key defined for UPDATE operation");
        }

        // Handle collection/array parameters - convert to single values for database
        Map<String, Object> processedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || value.getClass().isArray()) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value
                    processedParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value for UPDATE", entry.getKey());
                } else {
                    processedParams.put(entry.getKey(), null);
                }
            } else {
                processedParams.put(entry.getKey(), value);
            }
        }

        StringBuilder setClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        List<Object> setValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            String key = entry.getKey();
            boolean isPk = pkColumns.stream().anyMatch(pk -> pk.equalsIgnoreCase(key));

            if (isPk) {
                if (whereClause.length() > 0) {
                    whereClause.append(" AND ");
                } else {
                    whereClause.append(" WHERE ");
                }
                whereClause.append(key).append(" = ?");
                whereValues.add(entry.getValue());
            } else {
                if (setClause.length() > 0) {
                    setClause.append(", ");
                }
                setClause.append(key).append(" = ?");
                setValues.add(entry.getValue());
            }
        }

        if (whereValues.isEmpty()) {
            throw new RuntimeException("No primary key values provided for UPDATE operation");
        }

        String sql = "UPDATE " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName +
                " SET " + setClause + whereClause;

        List<Object> allParams = new ArrayList<>(setValues);
        allParams.addAll(whereValues);

        try {
            int rowsAffected = oracleJdbcTemplate.update(sql, allParams.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Update successful" : "No rows updated");

            return result;

        } catch (Exception e) {
            log.error("Error executing UPDATE on {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute UPDATE operation.", e);
        }
    }

    public Object executeDelete(String tableName, String owner, Map<String, Object> params,
                                GeneratedApiEntity api) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("No parameters provided for DELETE operation");
        }

        // Handle collection/array parameters - convert to single values for database
        Map<String, Object> processedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || value.getClass().isArray()) {
                Collection<?> collection = value instanceof List ?
                        (List<?>) value : Arrays.asList((Object[]) value);
                if (!collection.isEmpty()) {
                    // Take the first value
                    processedParams.put(entry.getKey(), collection.iterator().next());
                    log.info("Converted collection parameter '{}' to single value for DELETE", entry.getKey());
                } else {
                    processedParams.put(entry.getKey(), null);
                }
            } else {
                processedParams.put(entry.getKey(), value);
            }
        }

        StringBuilder whereClause = new StringBuilder();
        List<Object> whereValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : processedParams.entrySet()) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            } else {
                whereClause.append(" WHERE ");
            }
            whereClause.append(entry.getKey()).append(" = ?");
            whereValues.add(entry.getValue());
        }

        String sql = "DELETE FROM " + (owner != null && !owner.isEmpty() ? owner + "." : "") + tableName + whereClause;

        try {
            int rowsAffected = oracleJdbcTemplate.update(sql, whereValues.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            result.put("message", rowsAffected > 0 ? "Delete successful" : "No rows deleted");

            return result;

        } catch (Exception e) {
            log.error("Error executing DELETE on {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute DELETE operation.", e);
        }
    }
}