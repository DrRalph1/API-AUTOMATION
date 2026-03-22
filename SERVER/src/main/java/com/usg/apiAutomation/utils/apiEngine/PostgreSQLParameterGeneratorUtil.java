package com.usg.apiAutomation.utils.apiEngine;

import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.enums.DatabaseTypeEnum;
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
public class PostgreSQLParameterGeneratorUtil implements DatabaseParameterGeneratorUtil {

    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    private final PostgreSQLTypeMapper typeMapper;
    private final PostgreSQLObjectResolverUtil objectResolver;

    // ==================== IMPLEMENTATION OF DatabaseParameterGeneratorUtil INTERFACE ====================

    @Override
    public List<ApiParameterEntity> generateParameters(
            ApiSourceObjectDTO sourceObjectDTO,
            List<ApiParameterDTO> parameterDTOs,
            String generatedApiId) {

        List<ApiParameterEntity> parameters = new ArrayList<>();

        // If parameterDTOs are provided, convert them to entities
        if (parameterDTOs != null && !parameterDTOs.isEmpty()) {
            for (int i = 0; i < parameterDTOs.size(); i++) {
                ApiParameterDTO dto = parameterDTOs.get(i);
                ApiParameterEntity param = convertDtoToEntity(dto, generatedApiId);
                param.setPosition(i);
                parameters.add(param);
            }
            log.info("Generated {} parameters from provided DTOs", parameters.size());
            return parameters;
        }

        // Otherwise, generate from source object
        if (sourceObjectDTO != null) {
            try {
                List<ApiParameterDTO> generatedDTOs = generateParameterDTOsFromSource(sourceObjectDTO);
                for (int i = 0; i < generatedDTOs.size(); i++) {
                    ApiParameterDTO dto = generatedDTOs.get(i);
                    ApiParameterEntity param = convertDtoToEntity(dto, generatedApiId);
                    param.setPosition(i);
                    parameters.add(param);
                }
                log.info("Generated {} parameters from source object", parameters.size());
            } catch (Exception e) {
                log.error("Error generating parameters from source: {}", e.getMessage(), e);
            }
        }

        return parameters;
    }

    @Override
    public List<ApiResponseMappingEntity> generateResponseMappings(
            ApiSourceObjectDTO sourceObjectDTO,
            String generatedApiId) {

        List<ApiResponseMappingEntity> mappings = new ArrayList<>();

        if (sourceObjectDTO == null) {
            return mappings;
        }

        try {
            String targetType = sourceObjectDTO.getTargetType() != null ?
                    sourceObjectDTO.getTargetType() : sourceObjectDTO.getObjectType();
            String targetOwner = sourceObjectDTO.getTargetOwner() != null ?
                    sourceObjectDTO.getTargetOwner() : sourceObjectDTO.getOwner();
            String targetName = sourceObjectDTO.getTargetName() != null ?
                    sourceObjectDTO.getTargetName() : sourceObjectDTO.getObjectName();

            switch (targetType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                case "MATERIALIZED VIEW":
                    generateTableResponseMappings(targetOwner, targetName, generatedApiId, mappings);
                    break;
                case "PROCEDURE":
                    generateProcedureResponseMappings(targetOwner, targetName, generatedApiId, mappings);
                    break;
                case "FUNCTION":
                    generateFunctionResponseMappings(targetOwner, targetName, generatedApiId, mappings);
                    break;
            }

            log.info("Generated {} response mappings", mappings.size());

        } catch (Exception e) {
            log.error("Error generating response mappings: {}", e.getMessage(), e);
        }

        return mappings;
    }

    @Override
    public List<ApiHeaderEntity> generateHeaders(
            List<com.usg.apiAutomation.dtos.apiGenerationEngine.ApiHeaderDTO> headerDTOs,
            String generatedApiId) {

        List<ApiHeaderEntity> headers = new ArrayList<>();

        if (headerDTOs == null) {
            return headers;
        }

        for (com.usg.apiAutomation.dtos.apiGenerationEngine.ApiHeaderDTO dto : headerDTOs) {
            ApiHeaderEntity header = convertHeaderDtoToEntity(dto, generatedApiId);
            headers.add(header);
        }

        log.info("Generated {} headers", headers.size());
        return headers;
    }

    @Override
    public DatabaseTypeEnum getSupportedDatabaseType() {
        return DatabaseTypeEnum.POSTGRESQL;
    }

    // ==================== HELPER METHODS FOR CONVERSION ====================

    /**
     * Convert ApiParameterDTO to ApiParameterEntity
     */
    private ApiParameterEntity convertDtoToEntity(ApiParameterDTO dto, String generatedApiId) {
        ApiParameterEntity entity = ApiParameterEntity.builder()
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
                .paramMode(dto.getParamMode() != null ? dto.getParamMode() : "IN")
                .position(dto.getPosition() != null ? dto.getPosition() : 0)
                .build();

        // The generatedApi will be set by the caller
        return entity;
    }

    /**
     * Convert ApiHeaderDTO to ApiHeaderEntity
     */
    private ApiHeaderEntity convertHeaderDtoToEntity(
            com.usg.apiAutomation.dtos.apiGenerationEngine.ApiHeaderDTO dto,
            String generatedApiId) {
        ApiHeaderEntity entity = ApiHeaderEntity.builder()
                .key(dto.getKey())
                .value(dto.getValue())
                .required(dto.getRequired())
                .description(dto.getDescription())
                .isRequestHeader(dto.getIsRequestHeader() != null ? dto.getIsRequestHeader() : true)
                .isResponseHeader(dto.getIsResponseHeader() != null ? dto.getIsResponseHeader() : false)
                .build();

        // The generatedApi will be set by the caller
        return entity;
    }

    // ==================== GENERATE FROM SOURCE OBJECT ====================

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

            // PostgreSQL doesn't have synonyms, so use actual values directly
            String actualType = targetType;
            String actualOwner = targetOwner;
            String actualName = targetName;

            switch (actualType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                case "MATERIALIZED VIEW":
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
     * Generate parameters for tables/views using direct JDBC
     */
    private void generateTableParameters(String schema, String tableName, String operation,
                                         List<ApiParameterDTO> parameterDTOs) {
        String sql = "SELECT " +
                "    column_name, " +
                "    data_type, " +
                "    is_nullable, " +
                "    character_maximum_length as data_length, " +
                "    numeric_precision as data_precision, " +
                "    numeric_scale as data_scale " +
                "FROM information_schema.columns " +
                "WHERE table_schema = ? AND table_name = ? " +
                "ORDER BY ordinal_position";

        try {
            List<Map<String, Object>> columns = postgresqlJdbcTemplate.queryForList(sql, schema, tableName);
            log.info("Found {} columns for {}.{}", columns.size(), schema, tableName);

            Set<String> primaryKeys = getPrimaryKeys(schema, tableName);

            int position = 0;

            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("column_name");
                String dataType = (String) column.get("data_type");
                String nullable = (String) column.get("is_nullable");
                boolean isNullable = "YES".equalsIgnoreCase(nullable);
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
                paramDTO.setOracleType(typeMapper.mapToPostgreSQLType(dataType));
                paramDTO.setApiType(typeMapper.mapToApiType(dataType));
                paramDTO.setParameterType(parameterType);
                paramDTO.setParameterLocation(parameterType);
                paramDTO.setRequired(required);
                paramDTO.setDescription(String.format("Column: %s (%s)", columnName, dataType));
                paramDTO.setExample(typeMapper.generateExample(dataType));
                paramDTO.setPosition(position++);
                paramDTO.setParamMode("IN");
                paramDTO.setIsPrimaryKey(isPrimaryKey);
                paramDTO.setInBody(!"path".equals(parameterType) && !"query".equals(parameterType));

                parameterDTOs.add(paramDTO);
                log.debug("Generated parameter: {} ({}) as {} (required: {}, PK: {})",
                        columnName, dataType, parameterType, required, isPrimaryKey);
            }

            log.info("Generated {} parameters for {}", parameterDTOs.size(), tableName);

        } catch (Exception e) {
            log.error("Error generating table parameters for {}.{}: {}", schema, tableName, e.getMessage());
        }
    }

    /**
     * Generate parameters for procedures/functions WITHOUT path/query classification
     */
    private void generateProcedureParametersRaw(String schema, String name, String operation,
                                                List<ApiParameterDTO> parameterDTOs) {
        try {
            // Get function/procedure OID
            String oidSql = "SELECT p.oid, p.prokind FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ?";

            Map<String, Object> procInfo = null;
            try {
                procInfo = postgresqlJdbcTemplate.queryForMap(oidSql, schema, name);
            } catch (Exception e) {
                log.warn("Could not find function/procedure {}.{}", schema, name);
                return;
            }

            Long oid = (Long) procInfo.get("oid");
            String prokind = (String) procInfo.get("prokind");

            if (oid == null) {
                log.warn("Could not get OID for {}.{}", schema, name);
                return;
            }

            // Get argument information
            String argsSql = "SELECT " +
                    "    proargnames, " +
                    "    proargtypes, " +
                    "    proargmodes " +
                    "FROM pg_proc WHERE oid = ?";

            Map<String, Object> argsInfo = postgresqlJdbcTemplate.queryForMap(argsSql, oid);

            java.sql.Array proargnames = (java.sql.Array) argsInfo.get("proargnames");
            java.sql.Array proargtypes = (java.sql.Array) argsInfo.get("proargtypes");
            java.sql.Array proargmodes = (java.sql.Array) argsInfo.get("proargmodes");

            String[] argNames = proargnames != null ? (String[]) proargnames.getArray() : new String[0];
            String[] argTypes = proargtypes != null ? (String[]) proargtypes.getArray() : new String[0];
            String[] argModes = proargmodes != null ? (String[]) proargmodes.getArray() : new String[0];

            log.info("Found {} arguments for {}.{}", argNames.length, schema, name);

            int position = 0;

            for (int i = 0; i < argNames.length; i++) {
                String paramName = argNames[i];
                String dataType = getDataTypeName(argTypes[i]);
                String mode = getParameterMode(i < argModes.length ? argModes[i] : "i");

                if (paramName == null) {
                    continue;
                }

                log.debug("Processing parameter: {} - Type: {} - Mode: {}",
                        paramName, dataType, mode);

                // Only IN and INOUT parameters are for input
                if ("IN".equals(mode) || "IN/OUT".equals(mode)) {
                    String parameterLocation = determineParameterLocation(dataType);
                    boolean required = true; // PostgreSQL doesn't track default values in the same way

                    // Check if parameter has default value
                    try {
                        String defaultSql = "SELECT pronargdefaults FROM pg_proc WHERE oid = ?";
                        Integer defaultCount = postgresqlJdbcTemplate.queryForObject(defaultSql, Integer.class, oid);
                        if (defaultCount != null && i >= argNames.length - defaultCount) {
                            required = false;
                        }
                    } catch (Exception e) {
                        log.debug("Could not determine default for parameter: {}", paramName);
                    }

                    ApiParameterDTO paramDTO = new ApiParameterDTO();
                    paramDTO.setKey(convertToCamelCase(paramName));
                    paramDTO.setDbParameter(paramName);
                    paramDTO.setOracleType(typeMapper.mapToPostgreSQLType(dataType));
                    paramDTO.setApiType(typeMapper.mapToApiType(dataType));
                    paramDTO.setParameterType(null);
                    paramDTO.setParameterLocation(parameterLocation);
                    paramDTO.setRequired(required);
                    paramDTO.setDescription(String.format("%s (%s) - %s", paramName, mode, dataType));
                    paramDTO.setExample(typeMapper.generateExample(dataType));
                    paramDTO.setPosition(position++);
                    paramDTO.setParamMode(mode);
                    paramDTO.setIsPrimaryKey(false);
                    paramDTO.setInBody(true);

                    parameterDTOs.add(paramDTO);
                    log.debug("Generated parameter: {} ({}) - location hint: {} (required: {})",
                            paramName, dataType, parameterLocation, required);
                } else {
                    log.debug("Skipping {} parameter: {} (should be in response mappings)", mode, paramName);
                }
            }

            log.info("Generated {} IN/IN-OUT parameters for {}", parameterDTOs.size(), name);

        } catch (Exception e) {
            log.error("Error generating procedure parameters for {}.{}: {}", schema, name, e.getMessage());
        }
    }

    // ==================== RESPONSE MAPPINGS GENERATION ====================

    /**
     * Generate table response mappings
     */
    private void generateTableResponseMappings(String schema, String tableName, String generatedApiId,
                                               List<ApiResponseMappingEntity> mappings) {
        String sql = "SELECT column_name, data_type, is_nullable " +
                "FROM information_schema.columns " +
                "WHERE table_schema = ? AND table_name = ? " +
                "ORDER BY ordinal_position";

        try {
            List<Map<String, Object>> columns = postgresqlJdbcTemplate.queryForList(sql, schema, tableName);
            Set<String> primaryKeys = getPrimaryKeys(schema, tableName);

            int position = 0;
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("column_name");
                String dataType = (String) column.get("data_type");
                String nullable = (String) column.get("is_nullable");

                ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                        .apiField(convertToCamelCase(columnName))
                        .dbColumn(columnName)
                        .oracleType(typeMapper.mapToPostgreSQLType(dataType))
                        .apiType(typeMapper.mapToApiType(dataType))
                        .format(typeMapper.mapToFormat(dataType))
                        .nullable("YES".equalsIgnoreCase(nullable))
                        .isPrimaryKey(primaryKeys.contains(columnName))
                        .includeInResponse(true)
                        .inResponse(true)
                        .position(position++)
                        .build();

                mappings.add(mapping);
            }
            log.info("Generated {} table response mappings", mappings.size());
        } catch (Exception e) {
            log.error("Error generating table response mappings: {}", e.getMessage());
        }
    }

    /**
     * Generate procedure response mappings
     */
    private void generateProcedureResponseMappings(String schema, String procedureName, String generatedApiId,
                                                   List<ApiResponseMappingEntity> mappings) {
        try {
            // Get procedure OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'p'";

            Long oid = null;
            try {
                oid = postgresqlJdbcTemplate.queryForObject(oidSql, Long.class, schema, procedureName);
            } catch (Exception e) {
                log.warn("Could not find procedure {}.{}", schema, procedureName);
                return;
            }

            if (oid == null) return;

            // Get OUT and INOUT parameters
            String argsSql = "SELECT proargnames, proargtypes, proargmodes FROM pg_proc WHERE oid = ?";
            Map<String, Object> argsInfo = postgresqlJdbcTemplate.queryForMap(argsSql, oid);

            java.sql.Array proargnames = (java.sql.Array) argsInfo.get("proargnames");
            java.sql.Array proargtypes = (java.sql.Array) argsInfo.get("proargtypes");
            java.sql.Array proargmodes = (java.sql.Array) argsInfo.get("proargmodes");

            String[] argNames = proargnames != null ? (String[]) proargnames.getArray() : new String[0];
            String[] argTypes = proargtypes != null ? (String[]) proargtypes.getArray() : new String[0];
            String[] argModes = proargmodes != null ? (String[]) proargmodes.getArray() : new String[0];

            int position = 0;

            for (int i = 0; i < argNames.length; i++) {
                String mode = getParameterMode(i < argModes.length ? argModes[i] : "i");

                if ("OUT".equals(mode) || "IN/OUT".equals(mode)) {
                    String paramName = argNames[i];
                    String dataType = getDataTypeName(argTypes[i]);

                    if (paramName == null) continue;

                    ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                            .apiField(convertToCamelCase(paramName))
                            .dbColumn(paramName)
                            .oracleType(typeMapper.mapToPostgreSQLType(dataType))
                            .apiType(typeMapper.mapToApiType(dataType))
                            .format(typeMapper.mapToFormat(dataType))
                            .nullable(true)
                            .isPrimaryKey(false)
                            .includeInResponse(true)
                            .inResponse(true)
                            .position(position++)
                            .build();

                    mappings.add(mapping);
                }
            }
            log.info("Generated {} procedure response mappings", mappings.size());
        } catch (Exception e) {
            log.error("Error generating procedure response mappings: {}", e.getMessage());
        }
    }

    /**
     * Generate function response mappings
     */
    private void generateFunctionResponseMappings(String schema, String functionName, String generatedApiId,
                                                  List<ApiResponseMappingEntity> mappings) {
        try {
            // Get function OID
            String oidSql = "SELECT p.oid FROM pg_proc p " +
                    "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname = ? AND p.proname = ? AND p.prokind = 'f'";

            Long oid = null;
            try {
                oid = postgresqlJdbcTemplate.queryForObject(oidSql, Long.class, schema, functionName);
            } catch (Exception e) {
                log.warn("Could not find function {}.{}", schema, functionName);
                return;
            }

            if (oid == null) return;

            // Get return type
            String returnSql = "SELECT pg_get_function_result(?) as return_type";
            String returnType = postgresqlJdbcTemplate.queryForObject(returnSql, String.class, oid);

            if (returnType != null) {
                ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
                        .apiField("result")
                        .dbColumn("RETURN_VALUE")
                        .oracleType(typeMapper.mapToPostgreSQLType(returnType))
                        .apiType(typeMapper.mapToApiType(returnType))
                        .format(typeMapper.mapToFormat(returnType))
                        .nullable(false)
                        .isPrimaryKey(false)
                        .includeInResponse(true)
                        .inResponse(true)
                        .position(0)
                        .build();

                mappings.add(mapping);
                log.info("Generated function response mapping for return value");
            }
        } catch (Exception e) {
            log.error("Error generating function response mappings: {}", e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get PostgreSQL data type name from type OID
     */
    private String getDataTypeName(String typeOid) {
        try {
            String sql = "SELECT typname FROM pg_type WHERE oid = ?::regtype::oid";
            return postgresqlJdbcTemplate.queryForObject(sql, String.class, typeOid);
        } catch (Exception e) {
            return typeOid;
        }
    }

    /**
     * Get parameter mode from PostgreSQL mode code
     */
    private String getParameterMode(String modeCode) {
        if (modeCode == null) return "IN";
        switch (modeCode) {
            case "i": return "IN";
            case "o": return "OUT";
            case "b": return "IN/OUT";
            case "v": return "VARIADIC";
            default: return "IN";
        }
    }

    /**
     * Determine where a parameter naturally belongs based on its characteristics
     */
    private String determineParameterLocation(String dataType) {
        if (dataType == null) return "body";

        String type = dataType.toLowerCase();

        if (type.contains("json") || type.contains("xml") ||
                type.contains("bytea") || type.contains("text")) {
            return "body";
        }

        if (type.contains("char") || type.contains("varchar") ||
                type.contains("int") || type.contains("decimal") ||
                type.contains("numeric") || type.contains("float") ||
                type.contains("double") || type.contains("real") ||
                type.contains("date") || type.contains("time") ||
                type.contains("timestamp")) {
            return "scalar";
        }

        return "body";
    }

    /**
     * Get primary key columns for a table
     */
    private Set<String> getPrimaryKeys(String schema, String tableName) {
        Set<String> primaryKeys = new HashSet<>();

        String sql = "SELECT kcu.column_name " +
                "FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu " +
                "ON tc.constraint_name = kcu.constraint_name " +
                "WHERE tc.constraint_type = 'PRIMARY KEY' " +
                "AND tc.table_schema = ? " +
                "AND tc.table_name = ?";

        try {
            List<Map<String, Object>> results = postgresqlJdbcTemplate.queryForList(sql, schema, tableName);
            for (Map<String, Object> row : results) {
                String columnName = (String) row.get("column_name");
                if (columnName != null) {
                    primaryKeys.add(columnName);
                }
            }
            log.debug("Found primary keys for {}.{}: {}", schema, tableName, primaryKeys);
        } catch (Exception e) {
            log.warn("Error getting primary keys for {}.{}: {}", schema, tableName, e.getMessage());
        }

        return primaryKeys;
    }

    /**
     * Convert database name to camelCase
     */
    private String convertToCamelCase(String dbName) {
        if (dbName == null) return null;

        String name = dbName.replaceAll("^[a-z]_", "");

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

    // ==================== LEGACY METHODS FOR BACKWARD COMPATIBILITY ====================

    /**
     * Generate parameters from source for saving to database
     */
    public List<ApiParameterEntity> generateParametersFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api) {
        List<ApiParameterEntity> parameters = generateParameters(sourceObject, null, api.getId());
        for (ApiParameterEntity param : parameters) {
            param.setGeneratedApi(api);
        }
        return parameters;
    }

    /**
     * Generate response mappings from source
     */
    public List<ApiResponseMappingEntity> generateResponseMappingsFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api) {
        List<ApiResponseMappingEntity> mappings = generateResponseMappings(sourceObject, api.getId());
        for (ApiResponseMappingEntity mapping : mappings) {
            mapping.setGeneratedApi(api);
        }
        return mappings;
    }

    /**
     * CREATE PARAMETER ENTITY WITH BIDIRECTIONAL LINK
     */
    public ApiParameterEntity createParameterEntity(ApiParameterDTO dto, GeneratedApiEntity api) {
        ApiParameterEntity param = convertDtoToEntity(dto, api.getId());
        param.setGeneratedApi(api);
        return param;
    }

    /**
     * CREATE HEADER ENTITY WITH BIDIRECTIONAL LINK
     */
    public ApiHeaderEntity createHeaderEntity(ApiHeaderDTO dto, GeneratedApiEntity api) {
        ApiHeaderEntity header = convertHeaderDtoToEntity(dto, api.getId());
        header.setGeneratedApi(api);
        return header;
    }

    /**
     * CREATE RESPONSE MAPPING ENTITY WITH BIDIRECTIONAL LINK
     */
    public ApiResponseMappingEntity createResponseMappingEntity(ApiResponseMappingDTO dto, GeneratedApiEntity api) {
        ApiResponseMappingEntity mapping = ApiResponseMappingEntity.builder()
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

        mapping.setGeneratedApi(api);
        return mapping;
    }
}