package com.usg.apiAutomation.utils.apiEngine;

import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.services.OracleSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParameterGeneratorUtil {

    private final OracleSchemaService oracleSchemaService;
    private final OracleTypeMapper typeMapper;

    /**
     * Generate parameter DTOs from source object for path building purposes
     */
    public List<ApiParameterDTO> generateParameterDTOsFromSource(ApiSourceObjectDTO sourceObjectDTO) {
        List<ApiParameterDTO> parameterDTOs = new ArrayList<>();

        if (sourceObjectDTO == null) {
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

            // First, if it's a synonym, resolve it to get the actual table/view
            String actualType = targetType;
            String actualOwner = targetOwner;
            String actualName = targetName;

            // TODO: Implement synonym resolution logic
            // For now, assume it's not a synonym

            switch (actualType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    // Fetch column information from the database
                    Map<String, Object> tableDetails = oracleSchemaService.getTableDetailsForFrontendPaginated(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            actualName,
                            1, 1000  // Get up to 1000 columns
                    );

                    Map<String, Object> tableData = (Map<String, Object>) tableDetails.get("data");
                    if (tableData != null && tableData.containsKey("columns")) {
                        List<Map<String, Object>> columns = (List<Map<String, Object>>) tableData.get("columns");

                        log.info("Found {} columns for table {}.{}", columns.size(), actualOwner, actualName);

                        int position = 0;
                        int pathParamCount = 0;
                        int queryParamCount = 0;

                        for (Map<String, Object> column : columns) {
                            String columnName = (String) column.get("name");
                            if (columnName == null) {
                                columnName = (String) column.get("COLUMN_NAME");
                            }

                            String dataType = (String) column.get("data_type");
                            if (dataType == null) {
                                dataType = (String) column.get("DATA_TYPE");
                            }

                            // Check if it's a primary key
                            boolean isPrimaryKey = false;
                            Object isPkObj = column.get("isPrimaryKey");
                            if (isPkObj instanceof Boolean) {
                                isPrimaryKey = (Boolean) isPkObj;
                            } else if (column.get("constraint_type") != null) {
                                isPrimaryKey = "P".equals(column.get("constraint_type"));
                            }

                            // Also check for column name patterns that might indicate primary keys
                            if (!isPrimaryKey && columnName != null) {
                                String upperColName = columnName.toUpperCase();
                                isPrimaryKey = upperColName.equals("ID") ||
                                        upperColName.equals("CODE") ||
                                        upperColName.equals("ACCT_LINK") ||
                                        upperColName.endsWith("_ID") ||
                                        upperColName.endsWith("_CODE") ||
                                        upperColName.endsWith("_LINK");
                            }

                            String nullable = (String) column.get("nullable");
                            if (nullable == null) {
                                nullable = (String) column.get("NULLABLE");
                            }
                            boolean isNullable = "Y".equalsIgnoreCase(nullable);

                            // Determine parameter type based on operation
                            String parameterType;
                            boolean required;

                            if ("SELECT".equals(operation)) {
                                // For SELECT, primary keys become path parameters, others query
                                if (isPrimaryKey) {
                                    parameterType = "path";
                                    required = true;
                                    pathParamCount++;
                                } else {
                                    parameterType = "query";
                                    required = false;
                                    queryParamCount++;
                                }
                            } else if ("INSERT".equals(operation)) {
                                // For INSERT, all non-nullable columns are required body parameters
                                parameterType = "body";
                                required = !isNullable;
                            } else if ("UPDATE".equals(operation) || "DELETE".equals(operation)) {
                                // For UPDATE/DELETE, primary keys are path parameters
                                if (isPrimaryKey) {
                                    parameterType = "path";
                                    required = true;
                                    pathParamCount++;
                                } else if ("UPDATE".equals(operation)) {
                                    // For UPDATE, other columns are optional body parameters
                                    parameterType = "body";
                                    required = false;
                                } else {
                                    continue; // DELETE only needs primary keys
                                }
                            } else {
                                // Default to query for unknown operations
                                parameterType = "query";
                                required = false;
                                queryParamCount++;
                            }

                            // Create parameter DTO
                            ApiParameterDTO paramDTO = new ApiParameterDTO();
                            paramDTO.setKey(columnName.toLowerCase());
                            paramDTO.setDbColumn(columnName);
                            paramDTO.setOracleType(typeMapper.mapOracleType(dataType));
                            paramDTO.setApiType(typeMapper.mapToApiType(dataType));
                            paramDTO.setParameterType(parameterType);
                            paramDTO.setRequired(required);
                            paramDTO.setDescription("From " + sourceObjectDTO.getObjectName() + "." + columnName);
                            paramDTO.setExample(typeMapper.generateExample(dataType));
                            paramDTO.setPosition(position++);
                            paramDTO.setParamMode("IN");
                            paramDTO.setIsPrimaryKey(isPrimaryKey);

                            parameterDTOs.add(paramDTO);
                            log.debug("Generated parameter: {} ({}) as {} (required: {}, PK: {})",
                                    columnName, dataType, parameterType, required, isPrimaryKey);
                        }

                        log.info("Generated {} parameters for {}: {} path params, {} query params",
                                parameterDTOs.size(), actualName, pathParamCount, queryParamCount);
                    } else {
                        log.warn("No column data found for table {}.{}", actualOwner, actualName);
                    }
                    break;

                case "PROCEDURE":
                case "FUNCTION":
                    // Fetch procedure/function parameters from the database
                    Map<String, Object> objectDetails;
                    if ("PROCEDURE".equalsIgnoreCase(actualType)) {
                        objectDetails = oracleSchemaService.getProcedureDetails(
                                UUID.randomUUID().toString(),
                                null,
                                "system",
                                actualName
                        );
                    } else {
                        objectDetails = oracleSchemaService.getFunctionDetails(
                                UUID.randomUUID().toString(),
                                null,
                                "system",
                                actualName
                        );
                    }

                    Map<String, Object> procData = (Map<String, Object>) objectDetails.get("data");
                    if (procData != null && procData.containsKey("parameters")) {
                        List<Map<String, Object>> params = (List<Map<String, Object>>) procData.get("parameters");

                        log.info("Found {} parameters for {}.{}", params.size(), actualOwner, actualName);

                        int position = 0;
                        int pathParamCount = 0;
                        int queryParamCount = 0;

                        for (Map<String, Object> param : params) {
                            String paramName = (String) param.get("ARGUMENT_NAME");
                            if (paramName == null) {
                                paramName = (String) param.get("argument_name");
                            }

                            String dataType = (String) param.get("DATA_TYPE");
                            if (dataType == null) {
                                dataType = (String) param.get("data_type");
                            }

                            String inOut = (String) param.get("IN_OUT");
                            if (inOut == null) {
                                inOut = (String) param.get("in_out");
                            }

                            String defaultValue = (String) param.get("DEFAULTED");
                            boolean hasDefault = "YES".equalsIgnoreCase(defaultValue);

                            // Determine parameter type based on IN/OUT and operation
                            String parameterType;
                            boolean required;

                            if ("EXECUTE".equals(operation)) {
                                if ("IN".equals(inOut)) {
                                    // For procedures, make the first few parameters path params if they look like IDs
                                    if (position < 2 && paramName != null &&
                                            (paramName.toUpperCase().contains("ID") ||
                                                    paramName.toUpperCase().contains("CODE") ||
                                                    paramName.toUpperCase().contains("LINK"))) {
                                        parameterType = "path";
                                        pathParamCount++;
                                    } else {
                                        parameterType = "query";
                                        queryParamCount++;
                                    }
                                    required = !hasDefault;
                                } else {
                                    // OUT parameters are not sent in request
                                    continue;
                                }
                            } else {
                                continue;
                            }

                            ApiParameterDTO paramDTO = new ApiParameterDTO();
                            paramDTO.setKey(paramName.toLowerCase());
                            paramDTO.setDbParameter(paramName);
                            paramDTO.setOracleType(typeMapper.mapOracleType(dataType));
                            paramDTO.setApiType(typeMapper.mapToApiType(dataType));
                            paramDTO.setParameterType(parameterType);
                            paramDTO.setRequired(required);
                            paramDTO.setDescription("Parameter: " + paramName + " (" + inOut + ")");
                            paramDTO.setExample(typeMapper.generateExample(dataType));
                            paramDTO.setPosition(position++);
                            paramDTO.setParamMode(inOut);
                            paramDTO.setIsPrimaryKey(false);

                            parameterDTOs.add(paramDTO);
                            log.debug("Generated parameter: {} ({}) as {} (required: {})",
                                    paramName, dataType, parameterType, required);
                        }

                        log.info("Generated {} parameters for {}: {} path params, {} query params",
                                parameterDTOs.size(), actualName, pathParamCount, queryParamCount);
                    }
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
     * Generate parameters from source for saving to database
     */
    public List<ApiParameterEntity> generateParametersFromSource(ApiSourceObjectDTO sourceObject, GeneratedApiEntity api) {
        List<ApiParameterEntity> parameters = new ArrayList<>();

        try {
            List<ApiParameterDTO> parameterDTOs = generateParameterDTOsFromSource(sourceObject);

            for (int i = 0; i < parameterDTOs.size(); i++) {
                ApiParameterDTO dto = parameterDTOs.get(i);
                ApiParameterEntity param = mapToParameterEntity(dto, api);
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
     * Generate response mappings from source
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

            switch (targetType.toUpperCase()) {
                case "TABLE":
                case "VIEW":
                    Map<String, Object> tableDetails = oracleSchemaService.getTableDetailsForFrontend(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> tableData = (Map<String, Object>) tableDetails.get("data");
                    if (tableData != null && tableData.containsKey("columns")) {
                        List<Map<String, Object>> columns = (List<Map<String, Object>>) tableData.get("columns");
                        int position = 0;
                        for (Map<String, Object> column : columns) {
                            ApiResponseMappingEntity mapping = createResponseMappingFromColumn(column, api, position++);
                            mappings.add(mapping);
                        }
                    }
                    break;

                case "FUNCTION":
                    Map<String, Object> functionDetails = oracleSchemaService.getFunctionDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> funcData = (Map<String, Object>) functionDetails.get("data");
                    if (funcData != null && funcData.containsKey("returnType")) {
                        Map<String, Object> returnType = (Map<String, Object>) funcData.get("returnType");
                        ApiResponseMappingEntity mapping = createResponseMappingFromReturnType(returnType, api, 0);
                        mappings.add(mapping);
                    }
                    break;

                case "PROCEDURE":
                    Map<String, Object> procedureDetails = oracleSchemaService.getProcedureDetails(
                            UUID.randomUUID().toString(),
                            null,
                            "system",
                            targetName
                    );

                    Map<String, Object> procData = (Map<String, Object>) procedureDetails.get("data");
                    if (procData != null && procData.containsKey("parameters")) {
                        List<Map<String, Object>> params = (List<Map<String, Object>>) procData.get("parameters");
                        int position = 0;
                        for (Map<String, Object> param : params) {
                            String inOut = (String) param.get("IN_OUT");
                            if ("OUT".equals(inOut) || "IN/OUT".equals(inOut)) {
                                ApiResponseMappingEntity mapping = createResponseMappingFromArgument(param, api, position++);
                                mappings.add(mapping);
                            }
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            log.warn("Could not auto-generate response mappings from source: {}", e.getMessage());
        }

        return mappings;
    }

    private ApiResponseMappingEntity createResponseMappingFromColumn(Map<String, Object> column, GeneratedApiEntity api, int position) {
        String columnName = (String) (column.get("name") != null ? column.get("name") : column.get("COLUMN_NAME"));
        String dataType = (String) (column.get("data_type") != null ? column.get("data_type") : column.get("DATA_TYPE"));
        boolean nullable = "Y".equals(column.get("nullable")) || "Y".equals(column.get("NULLABLE"));
        boolean isPrimaryKey = column.get("isPrimaryKey") != null && (Boolean) column.get("isPrimaryKey") ||
                "P".equals(column.get("constraint_type"));

        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField(columnName.toLowerCase())
                .dbColumn(columnName)
                .oracleType(typeMapper.mapOracleType(dataType))
                .apiType(typeMapper.mapToApiType(dataType))
                .format(typeMapper.mapToFormat(dataType))
                .nullable(nullable)
                .isPrimaryKey(isPrimaryKey)
                .includeInResponse(true)
                .position(position)
                .build();
    }

    private ApiResponseMappingEntity createResponseMappingFromArgument(Map<String, Object> argument, GeneratedApiEntity api, int position) {
        String argName = (String) (argument.get("ARGUMENT_NAME") != null ?
                argument.get("ARGUMENT_NAME") : argument.get("argument_name"));
        String dataType = (String) (argument.get("DATA_TYPE") != null ?
                argument.get("DATA_TYPE") : argument.get("data_type"));

        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField(argName != null ? argName.toLowerCase() : "result")
                .dbColumn(argName)
                .oracleType(typeMapper.mapOracleType(dataType))
                .apiType(typeMapper.mapToApiType(dataType))
                .format(typeMapper.mapToFormat(dataType))
                .nullable(true)
                .isPrimaryKey(false)
                .includeInResponse(true)
                .position(position)
                .build();
    }

    private ApiResponseMappingEntity createResponseMappingFromReturnType(Map<String, Object> returnType, GeneratedApiEntity api, int position) {
        String dataType = (String) (returnType.get("DATA_TYPE") != null ?
                returnType.get("DATA_TYPE") : returnType.get("data_type"));

        return ApiResponseMappingEntity.builder()
                .generatedApi(api)
                .apiField("result")
                .dbColumn("RETURN_VALUE")
                .oracleType(typeMapper.mapOracleType(dataType))
                .apiType(typeMapper.mapToApiType(dataType))
                .format(typeMapper.mapToFormat(dataType))
                .nullable(false)
                .isPrimaryKey(false)
                .includeInResponse(true)
                .position(position)
                .build();
    }

    private ApiParameterEntity mapToParameterEntity(ApiParameterDTO dto, GeneratedApiEntity api) {
        if (dto == null) return null;

        return ApiParameterEntity.builder()
                .id(UUID.randomUUID().toString())
                .generatedApi(api)
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
                .isPrimaryKey(dto.getIsPrimaryKey())
                .paramMode(dto.getParamMode())
                .position(dto.getPosition() != null ? dto.getPosition() : 0)
                .build();
    }
}