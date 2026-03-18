package com.usg.apiAutomation.utils.apiEngine;

import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
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
public class ParameterGeneratorUtil {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private final OracleTypeMapper typeMapper;
    private final OracleObjectResolverUtil objectResolver;

    /**
     * Generate parameter DTOs from source object for path building purposes
     */
    public List<ApiParameterDTO> generateParameterDTOsFromSource(ApiSourceObjectDTO sourceObjectDTO) {
        List<ApiParameterDTO> parameterDTOs = new ArrayList<>();

        if (sourceObjectDTO == null || sourceObjectDTO.getObjectName() == null) {
            log.warn("Source object or object name is null");
            return parameterDTOs;
        }

        try {
            String targetType = sourceObjectDTO.getTargetType() != null ?
                    sourceObjectDTO.getTargetType() : sourceObjectDTO.getObjectType();
            String targetOwner = sourceObjectDTO.getTargetOwner() != null ?
                    sourceObjectDTO.getTargetOwner() : sourceObjectDTO.getOwner();
            String targetName = sourceObjectDTO.getTargetName() != null ?
                    sourceObjectDTO.getTargetName() : sourceObjectDTO.getObjectName();
            String operation = sourceObjectDTO.getOperation();

            log.info("Generating parameter DTOs from source - Type: {}, Name: {}, Owner: {}, Operation: {}",
                    targetType, targetName, targetOwner, operation);

            // Resolve synonyms to get actual object
            String actualType = targetType;
            String actualOwner = targetOwner;
            String actualName = targetName;

            Map<String, Object> resolvedInfo = objectResolver.resolveObject(
                    targetOwner, targetName, targetType);

            if (resolvedInfo != null && Boolean.TRUE.equals(resolvedInfo.get("isSynonym"))) {
                actualOwner = (String) resolvedInfo.get("targetOwner");
                actualName = (String) resolvedInfo.get("targetName");
                log.info("Resolved synonym {} to {}.{}", targetName, actualOwner, actualName);
            }

            switch (actualType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    generateTableParameters(actualOwner, actualName, operation, parameterDTOs);
                    break;

                case "PROCEDURE":
                case "FUNCTION":
                    generateProcedureParametersRaw(actualOwner, actualName, operation, parameterDTOs);
                    break;

                default:
                    log.warn("Unsupported object type: {} for parameter generation", actualType);
                    break;
            }

            log.info("Generated {} parameter DTOs from source", parameterDTOs.size());

        } catch (Exception e) {
            log.error("Failed to generate parameter DTOs from source: {}", e.getMessage(), e);
        }

        return parameterDTOs;
    }

    /**
     * Generate parameters for procedures/functions WITHOUT path/query classification
     */
    private void generateProcedureParametersRaw(String owner, String name, String operation,
                                                List<ApiParameterDTO> parameterDTOs) {
        String sql = "SELECT " +
                "ARGUMENT_NAME, " +
                "DATA_TYPE, " +
                "IN_OUT, " +
                "POSITION, " +
                "DATA_LENGTH, " +
                "DATA_PRECISION, " +
                "DATA_SCALE, " +
                "DEFAULTED " +
                "FROM ALL_ARGUMENTS " +
                "WHERE OWNER = ? AND OBJECT_NAME = ? AND DATA_LEVEL = 0 " +
                "ORDER BY POSITION";

        try {
            List<Map<String, Object>> params = oracleJdbcTemplate.queryForList(sql, owner, name);
            log.info("Found {} parameters for {}.{}", params.size(), owner, name);

            int position = 0;

            for (Map<String, Object> param : params) {
                String paramName = (String) param.get("ARGUMENT_NAME");
                String dataType = (String) param.get("DATA_TYPE");
                String inOut = (String) param.get("IN_OUT");
                String defaulted = (String) param.get("DEFAULTED");
                boolean hasDefault = "YES".equalsIgnoreCase(defaulted);

                if (paramName == null) {
                    continue;
                }

                log.debug("Processing parameter: {} - Type: {} - Mode: {} - Position: {}",
                        paramName, dataType, inOut, param.get("POSITION"));

                if ("IN".equals(inOut) || "IN/OUT".equals(inOut)) {
                    String parameterLocation = determineParameterLocation(dataType);
                    boolean required = !hasDefault;

                    ApiParameterDTO paramDTO = new ApiParameterDTO();
                    paramDTO.setKey(convertToCamelCase(paramName));
                    paramDTO.setDbParameter(paramName);
                    paramDTO.setOracleType(typeMapper.mapOracleType(dataType));
                    paramDTO.setApiType(mapOracleToApiType(dataType));
                    paramDTO.setParameterType(null);
                    paramDTO.setParameterLocation(parameterLocation);
                    paramDTO.setRequired(required);
                    paramDTO.setDescription(String.format("%s (%s) - %s", paramName, inOut, dataType));
                    paramDTO.setExample(generateExample(dataType));
                    paramDTO.setPosition(position++);
                    paramDTO.setParamMode(inOut);
                    paramDTO.setIsPrimaryKey(false);

                    parameterDTOs.add(paramDTO);
                    log.debug("Generated parameter: {} ({}) - location hint: {} (required: {})",
                            paramName, dataType, parameterLocation, required);
                } else {
                    log.debug("Skipping OUT parameter: {} (should be in response mappings)", paramName);
                }
            }

            log.info("Generated {} IN/IN-OUT parameters for {}", parameterDTOs.size(), name);

        } catch (Exception e) {
            log.error("Error generating procedure parameters for {}.{}: {}", owner, name, e.getMessage());
        }
    }

    /**
     * Determine where a parameter naturally belongs based on its characteristics
     */
    private String determineParameterLocation(String dataType) {
        if (dataType == null) return "body";

        String type = dataType.toUpperCase();

        if (type.contains("CLOB") || type.contains("BLOB") || type.contains("LONG") ||
                type.contains("XML") || type.contains("JSON")) {
            return "body";
        }

        if (type.contains("CHAR") || type.contains("VARCHAR") ||
                type.contains("NUMBER") || type.contains("INT") ||
                type.contains("DATE")) {
            return "scalar";
        }

        return "body";
    }

    /**
     * Generate parameters for tables/views using direct JDBC
     */
    private void generateTableParameters(String owner, String tableName, String operation,
                                         List<ApiParameterDTO> parameterDTOs) {
        String sql = "SELECT " +
                "COLUMN_NAME, " +
                "DATA_TYPE, " +
                "NULLABLE, " +
                "DATA_LENGTH, " +
                "DATA_PRECISION, " +
                "DATA_SCALE " +
                "FROM ALL_TAB_COLUMNS " +
                "WHERE OWNER = ? AND TABLE_NAME = ? " +
                "ORDER BY COLUMN_ID";

        try {
            List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(sql, owner, tableName);
            log.info("Found {} columns for {}.{}", columns.size(), owner, tableName);

            Set<String> primaryKeys = getPrimaryKeys(owner, tableName);

            int position = 0;

            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                String dataType = (String) column.get("DATA_TYPE");
                String nullable = (String) column.get("NULLABLE");
                boolean isNullable = "Y".equalsIgnoreCase(nullable);
                boolean isPrimaryKey = primaryKeys.contains(columnName);

                String parameterType;
                boolean required;

                if ("SELECT".equals(operation)) {
                    if (isPrimaryKey) {
                        parameterType = "path";
                        required = true;
                    } else {
                        parameterType = "query";
                        required = false;
                    }
                } else if ("INSERT".equals(operation)) {
                    parameterType = "body";
                    required = !isNullable && !isPrimaryKey;
                } else if ("UPDATE".equals(operation)) {
                    if (isPrimaryKey) {
                        parameterType = "path";
                        required = true;
                    } else {
                        parameterType = "body";
                        required = false;
                    }
                } else if ("DELETE".equals(operation)) {
                    if (isPrimaryKey) {
                        parameterType = "path";
                        required = true;
                    } else {
                        continue;
                    }
                } else {
                    parameterType = "query";
                    required = false;
                }

                ApiParameterDTO paramDTO = new ApiParameterDTO();
                paramDTO.setKey(convertToCamelCase(columnName));
                paramDTO.setDbColumn(columnName);
                paramDTO.setOracleType(typeMapper.mapOracleType(dataType));
                paramDTO.setApiType(mapOracleToApiType(dataType));
                paramDTO.setParameterType(parameterType);
                paramDTO.setParameterLocation(parameterType);
                paramDTO.setRequired(required);
                paramDTO.setDescription(String.format("Column: %s (%s)", columnName, dataType));
                paramDTO.setExample(generateExample(dataType));
                paramDTO.setPosition(position++);
                paramDTO.setParamMode("IN");
                paramDTO.setIsPrimaryKey(isPrimaryKey);

                parameterDTOs.add(paramDTO);
                log.debug("Generated parameter: {} ({}) as {} (required: {}, PK: {})",
                        columnName, dataType, parameterType, required, isPrimaryKey);
            }

            log.info("Generated {} parameters for {}", parameterDTOs.size(), tableName);

        } catch (Exception e) {
            log.error("Error generating table parameters for {}.{}: {}", owner, tableName, e.getMessage());
        }
    }

    /**
     * Get primary key columns for a table
     */
    private Set<String> getPrimaryKeys(String owner, String tableName) {
        Set<String> primaryKeys = new HashSet<>();

        String sql = "SELECT cols.column_name " +
                "FROM all_constraints cons, all_cons_columns cols " +
                "WHERE cons.constraint_type = 'P' " +
                "AND cons.owner = ? " +
                "AND cons.table_name = ? " +
                "AND cons.owner = cols.owner " +
                "AND cons.constraint_name = cols.constraint_name " +
                "AND cols.table_name = ?";

        try {
            List<Map<String, Object>> results = oracleJdbcTemplate.queryForList(sql, owner, tableName, tableName);
            for (Map<String, Object> row : results) {
                String columnName = (String) row.get("COLUMN_NAME");
                if (columnName != null) {
                    primaryKeys.add(columnName);
                }
            }
            log.debug("Found primary keys for {}.{}: {}", owner, tableName, primaryKeys);
        } catch (Exception e) {
            log.warn("Error getting primary keys for {}.{}: {}", owner, tableName, e.getMessage());
        }

        return primaryKeys;
    }

    /**
     * Convert database name to camelCase
     */
    private String convertToCamelCase(String dbName) {
        if (dbName == null) return null;

        String name = dbName.replaceAll("^P_", "").replaceAll("^V_", "").replaceAll("^P", "");

        String[] parts = name.toLowerCase().split("_");

        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                result.append(parts[i].substring(0, 1).toUpperCase())
                        .append(parts[i].substring(1));
            }
        }

        return result.toString();
    }

    /**
     * Map Oracle data types to API types
     */
    private String mapOracleToApiType(String oracleType) {
        if (oracleType == null) return "string";

        String type = oracleType.toUpperCase();
        if (type.contains("CHAR") || type.contains("VARCHAR") || type.contains("CLOB")) {
            return "string";
        } else if (type.contains("NUMBER") || type.contains("INT") || type.contains("FLOAT") ||
                type.contains("DECIMAL") || type.contains("NUMERIC")) {
            return "number";
        } else if (type.contains("DATE") || type.contains("TIMESTAMP")) {
            return "string";
        } else if (type.contains("BLOB") || type.contains("RAW")) {
            return "string";
        } else {
            return "string";
        }
    }

    /**
     * Generate example value based on data type
     */
    private String generateExample(String dataType) {
        if (dataType == null) return "example";

        String type = dataType.toUpperCase();
        if (type.contains("CHAR") || type.contains("VARCHAR")) {
            return "sample-value";
        } else if (type.contains("NUMBER") || type.contains("INT")) {
            return "123";
        } else if (type.contains("DATE")) {
            return "2024-01-01";
        } else if (type.contains("TIMESTAMP")) {
            return "2024-01-01T00:00:00";
        } else {
            return "example";
        }
    }

    /**
     * Generate parameters from source for saving to database - WITH BIDIRECTIONAL LINK
     */
    public List<ApiParameterEntity> generateParametersFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api) {
        List<ApiParameterEntity> parameters = new ArrayList<>();

        try {
            List<ApiParameterDTO> parameterDTOs = generateParameterDTOsFromSource(sourceObject);

            for (int i = 0; i < parameterDTOs.size(); i++) {
                ApiParameterDTO dto = parameterDTOs.get(i);
                ApiParameterEntity param = createParameterEntity(dto, api);
                param.setPosition(i);
                parameters.add(param);
            }

            log.info("Generated {} parameters from source", parameters.size());

        } catch (Exception e) {
            log.warn("Could not auto-generate parameters from source: {}", e.getMessage());
        }

        return parameters;
    }

    /**
     * Generate response mappings from source - WITH BIDIRECTIONAL LINK
     */
    public List<ApiResponseMappingEntity> generateResponseMappingsFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api) {
        List<ApiResponseMappingEntity> mappings = new ArrayList<>();

        try {
            String targetType = sourceObject.getTargetType() != null ?
                    sourceObject.getTargetType() : sourceObject.getObjectType();
            String targetOwner = sourceObject.getTargetOwner() != null ?
                    sourceObject.getTargetOwner() : sourceObject.getOwner();
            String targetName = sourceObject.getTargetName() != null ?
                    sourceObject.getTargetName() : sourceObject.getObjectName();

            Map<String, Object> resolvedInfo = objectResolver.resolveObject(
                    targetOwner, targetName, targetType);

            String actualOwner = targetOwner;
            String actualName = targetName;

            if (resolvedInfo != null && Boolean.TRUE.equals(resolvedInfo.get("isSynonym"))) {
                actualOwner = (String) resolvedInfo.get("targetOwner");
                actualName = (String) resolvedInfo.get("targetName");
            }

            switch (targetType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    generateTableResponseMappings(actualOwner, actualName, api, mappings);
                    break;

                case "PROCEDURE":
                    generateProcedureResponseMappings(actualOwner, actualName, api, mappings);
                    break;

                case "FUNCTION":
                    generateFunctionResponseMappings(actualOwner, actualName, api, mappings);
                    break;
            }

        } catch (Exception e) {
            log.warn("Could not auto-generate response mappings from source: {}", e.getMessage());
        }

        return mappings;
    }

    /**
     * CREATE PARAMETER ENTITY WITH BIDIRECTIONAL LINK - NEW METHOD
     */
    public ApiParameterEntity createParameterEntity(ApiParameterDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        ApiParameterEntity param = ApiParameterEntity.builder()
                .id(UUID.randomUUID().toString())
                .key(dto.getKey())
                .dbColumn(dto.getDbColumn())
                .dbParameter(dto.getDbParameter())
                .oracleType(dto.getOracleType())
                .apiType(dto.getApiType())
                .parameterType(dto.getParameterType())
                .parameterLocation(dto.getParameterLocation())
                .required(dto.getRequired())
                .description(dto.getDescription())
                .example(dto.getExample())
                .validationPattern(dto.getValidationPattern())
                .defaultValue(dto.getDefaultValue())
                .inBody(dto.getInBody())
                .isPrimaryKey(dto.getIsPrimaryKey() != null ? dto.getIsPrimaryKey() : false)
                .paramMode(dto.getParamMode())
                .position(dto.getPosition() != null ? dto.getPosition() : 0)
                .build();

        // CRITICAL: Set the bidirectional relationship
        param.setGeneratedApi(api);

        return param;
    }

    /**
     * CREATE HEADER ENTITY WITH BIDIRECTIONAL LINK - NEW METHOD
     */
    public ApiHeaderEntity createHeaderEntity(ApiHeaderDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        ApiHeaderEntity header = ApiHeaderEntity.builder()
                .id(UUID.randomUUID().toString())
                .key(dto.getKey())
                .value(dto.getValue())
                .description(dto.getDescription())
                .required(dto.getRequired())
                .isRequestHeader(dto.getIsRequestHeader())
                .isResponseHeader(dto.getIsResponseHeader())
                .build();

        // CRITICAL: Set the bidirectional relationship
        header.setGeneratedApi(api);

        return header;
    }

    /**
     * CREATE RESPONSE MAPPING ENTITY WITH BIDIRECTIONAL LINK - NEW METHOD
     */
    public ApiResponseMappingEntity createResponseMappingEntity(ApiResponseMappingDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                .id(UUID.randomUUID().toString())
                .apiField(dto.getApiField())
                .dbColumn(dto.getDbColumn())
                .oracleType(dto.getOracleType())
                .apiType(dto.getApiType())
                .format(dto.getFormat())
                .nullable(dto.getNullable())
                .isPrimaryKey(dto.getIsPrimaryKey())
                .includeInResponse(dto.getIncludeInResponse())
                .inResponse(dto.getInResponse())
                .position(dto.getPosition())
                .build();

        // CRITICAL: Set the bidirectional relationship
        mapping.setGeneratedApi(api);

        return mapping;
    }

    private void generateTableResponseMappings(String owner, String tableName, GeneratedApiEntity api,
                                               List<ApiResponseMappingEntity> mappings) {
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, NULLABLE " +
                "FROM ALL_TAB_COLUMNS " +
                "WHERE OWNER = ? AND TABLE_NAME = ? " +
                "ORDER BY COLUMN_ID";

        try {
            List<Map<String, Object>> columns = oracleJdbcTemplate.queryForList(sql, owner, tableName);
            Set<String> primaryKeys = getPrimaryKeys(owner, tableName);

            int position = 0;
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                String dataType = (String) column.get("DATA_TYPE");
                String nullable = (String) column.get("NULLABLE");

                ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .apiField(convertToCamelCase(columnName))
                        .dbColumn(columnName)
                        .oracleType(typeMapper.mapOracleType(dataType))
                        .apiType(mapOracleToApiType(dataType))
                        .format(typeMapper.mapToFormat(dataType))
                        .nullable("Y".equalsIgnoreCase(nullable))
                        .isPrimaryKey(primaryKeys.contains(columnName))
                        .includeInResponse(true)
                        .inResponse(true)
                        .position(position++)
                        .build();

                // CRITICAL: Set the bidirectional relationship
                mapping.setGeneratedApi(api);

                mappings.add(mapping);
            }
        } catch (Exception e) {
            log.error("Error generating table response mappings: {}", e.getMessage());
        }
    }

    private void generateProcedureResponseMappings(String owner, String procedureName, GeneratedApiEntity api,
                                                   List<ApiResponseMappingEntity> mappings) {
        String sql = "SELECT ARGUMENT_NAME, DATA_TYPE, IN_OUT, POSITION " +
                "FROM ALL_ARGUMENTS " +
                "WHERE OWNER = ? AND OBJECT_NAME = ? AND DATA_LEVEL = 0 " +
                "AND IN_OUT IN ('OUT', 'IN/OUT') " +
                "ORDER BY POSITION";

        try {
            List<Map<String, Object>> outParams = oracleJdbcTemplate.queryForList(sql, owner, procedureName);
            int position = 0;

            for (Map<String, Object> param : outParams) {
                String paramName = (String) param.get("ARGUMENT_NAME");
                String dataType = (String) param.get("DATA_TYPE");

                if (paramName == null) continue;

                ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .apiField(convertToCamelCase(paramName))
                        .dbColumn(paramName)
                        .oracleType(typeMapper.mapOracleType(dataType))
                        .apiType(mapOracleToApiType(dataType))
                        .format(typeMapper.mapToFormat(dataType))
                        .nullable(true)
                        .isPrimaryKey(false)
                        .includeInResponse(true)
                        .inResponse(true)
                        .position(position++)
                        .build();

                // CRITICAL: Set the bidirectional relationship
                mapping.setGeneratedApi(api);

                mappings.add(mapping);
            }
        } catch (Exception e) {
            log.error("Error generating procedure response mappings: {}", e.getMessage());
        }
    }

    private void generateFunctionResponseMappings(String owner, String functionName, GeneratedApiEntity api,
                                                  List<ApiResponseMappingEntity> mappings) {
        String sql = "SELECT DATA_TYPE " +
                "FROM ALL_ARGUMENTS " +
                "WHERE OWNER = ? AND OBJECT_NAME = ? AND DATA_LEVEL = 0 " +
                "AND ARGUMENT_NAME IS NULL";

        try {
            List<Map<String, Object>> returnVal = oracleJdbcTemplate.queryForList(sql, owner, functionName);

            if (!returnVal.isEmpty()) {
                String dataType = (String) returnVal.get(0).get("DATA_TYPE");

                ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .apiField("result")
                        .dbColumn("RETURN_VALUE")
                        .oracleType(typeMapper.mapOracleType(dataType))
                        .apiType(mapOracleToApiType(dataType))
                        .format(typeMapper.mapToFormat(dataType))
                        .nullable(false)
                        .isPrimaryKey(false)
                        .includeInResponse(true)
                        .inResponse(true)
                        .position(0)
                        .build();

                // CRITICAL: Set the bidirectional relationship
                mapping.setGeneratedApi(api);

                mappings.add(mapping);
            }
        } catch (Exception e) {
            log.error("Error generating function response mappings: {}", e.getMessage());
        }
    }
}