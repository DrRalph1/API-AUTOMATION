package com.usg.apiAutomation.helpers.apiEngine.postgresql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.helpers.BaseApiExecutionHelper;
import com.usg.apiAutomation.helpers.apiEngine.ApiConversionHelper;
import com.usg.apiAutomation.helpers.apiEngine.ApiResponseHelper;
import com.usg.apiAutomation.repositories.apiGenerationEngine.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.usg.apiAutomation.utils.apiEngine.DatabaseParameterGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.executor.postgresql.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class PostgreSQLApiExecutionHelper extends BaseApiExecutionHelper {

    private final PostgreSQLTableExecutorUtil postgreSQLTableExecutorUtil;
    private final PostgreSQLViewExecutorUtil postgreSQLViewExecutorUtil;
    private final PostgreSQLProcedureExecutorUtil postgreSQLProcedureExecutorUtil;
    private final PostgreSQLFunctionExecutorUtil postgreSQLFunctionExecutorUtil;

    public PostgreSQLApiExecutionHelper(
            ApiResponseHelper responseHelper,
            LoggerUtil loggerUtil,
            ApiConversionHelper conversionHelper,
            PostgreSQLTableExecutorUtil postgreSQLTableExecutorUtil,
            PostgreSQLViewExecutorUtil postgreSQLViewExecutorUtil,
            PostgreSQLProcedureExecutorUtil postgreSQLProcedureExecutorUtil,
            PostgreSQLFunctionExecutorUtil postgreSQLFunctionExecutorUtil) {
        super(responseHelper, loggerUtil, conversionHelper);
        this.postgreSQLTableExecutorUtil = postgreSQLTableExecutorUtil;
        this.postgreSQLViewExecutorUtil = postgreSQLViewExecutorUtil;
        this.postgreSQLProcedureExecutorUtil = postgreSQLProcedureExecutorUtil;
        this.postgreSQLFunctionExecutorUtil = postgreSQLFunctionExecutorUtil;
    }

    @Override
    protected void setupApiRelationships(GeneratedApiEntity api,
                                         GenerateApiRequestDTO request,
                                         ApiSourceObjectDTO sourceObjectDTO,
                                         DatabaseParameterGeneratorUtil parameterGenerator,
                                         ApiConversionHelper conversionHelper) {

        // Ensure collections are initialized (if they're null, initialize them)
        if (api.getParameters() == null) {
            api.setParameters(new ArrayList<>());
        }
        if (api.getHeaders() == null) {
            api.setHeaders(new ArrayList<>());
        }
        if (api.getResponseMappings() == null) {
            api.setResponseMappings(new ArrayList<>());
        }

        // Create and set auth config (one-to-one - this is fine to set)
        if (request.getAuthConfig() != null) {
            ApiAuthConfigEntity authConfig = conversionHelper.createAuthConfigEntity(request.getAuthConfig());
            authConfig.setGeneratedApi(api);
            api.setAuthConfig(authConfig);
        }

        // Create and set request config (one-to-one - this is fine to set)
        if (request.getRequestBody() != null) {
            ApiRequestConfigEntity requestConfig = conversionHelper.createRequestConfigEntity(request.getRequestBody());
            requestConfig.setGeneratedApi(api);
            api.setRequestConfig(requestConfig);
        }

        // Create and set response config (one-to-one - this is fine to set)
        if (request.getResponseBody() != null) {
            ApiResponseConfigEntity responseConfig = conversionHelper.createResponseConfigEntity(request.getResponseBody());
            responseConfig.setGeneratedApi(api);
            api.setResponseConfig(responseConfig);
        }

        // Create and set schema config (one-to-one - this is fine to set)
        if (sourceObjectDTO != null) {
            ApiSchemaConfigEntity schemaConfig = conversionHelper.createSchemaConfigEntity(sourceObjectDTO);

            // If schema name is null, default to "public" for PostgreSQL
            if (schemaConfig.getSchemaName() == null || schemaConfig.getSchemaName().isEmpty()) {
                schemaConfig.setSchemaName("public");
                log.info("Defaulting to 'public' schema for PostgreSQL table: {}", schemaConfig.getObjectName());
            }

            schemaConfig.setGeneratedApi(api);
            api.setSchemaConfig(schemaConfig);
        }

        // Create and set settings (one-to-one - this is fine to set)
        if (request.getSettings() != null) {
            ApiSettingsEntity settings = conversionHelper.createSettingsEntity(request.getSettings());
            settings.setGeneratedApi(api);
            api.setSettings(settings);
        }

        // ADD to parameters collection (don't replace)
        List<ApiParameterEntity> parameters = parameterGenerator.generateParameters(
                sourceObjectDTO, request.getParameters(), api.getId());
        for (ApiParameterEntity param : parameters) {
            param.setGeneratedApi(api);
            api.getParameters().add(param);  // ADD to existing collection, not set
        }

        // ADD to headers collection (don't replace)
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            List<ApiHeaderEntity> headers = conversionHelper.createHeaderEntities(request.getHeaders(), api);
            for (ApiHeaderEntity header : headers) {
                header.setGeneratedApi(api);
                api.getHeaders().add(header);  // ADD to existing collection
            }
        }

        // ADD to response mappings collection (don't replace)
        if (request.getResponseMappings() != null && !request.getResponseMappings().isEmpty()) {
            List<ApiResponseMappingEntity> responseMappings =
                    conversionHelper.createResponseMappingEntities(request.getResponseMappings(), api.getId());
            for (ApiResponseMappingEntity mapping : responseMappings) {
                mapping.setGeneratedApi(api);
                api.getResponseMappings().add(mapping);  // ADD to existing collection
            }
        }

        log.debug("Setup relationships for PostgreSQL API: {}", api.getId());
    }

    @Override
    public Object executeAgainstDatabase(GeneratedApiEntity api,
                                         ApiSourceObjectDTO sourceObject,
                                         ExecuteApiRequestDTO validatedRequest,
                                         List<ApiParameterDTO> configuredParamDTOs) {

        log.info("Executing PostgreSQL operation for API: {}", api.getId());

        ApiSchemaConfigEntity schemaConfig = api.getSchemaConfig();
        if (schemaConfig == null) {
            throw new RuntimeException("Schema configuration not found for PostgreSQL API");
        }

        String objectType = schemaConfig.getObjectType();
        String objectName = schemaConfig.getObjectName();
        String schema = schemaConfig.getSchemaName();
        String operation = schemaConfig.getOperation();

        // Create consolidated params
        Map<String, Object> params = new HashMap<>();
        if (validatedRequest.getPathParams() != null) params.putAll(validatedRequest.getPathParams());
        if (validatedRequest.getQueryParams() != null) params.putAll(validatedRequest.getQueryParams());
        if (validatedRequest.getBody() instanceof Map) {
            params.putAll((Map<String, Object>) validatedRequest.getBody());
        }

        switch (objectType.toUpperCase()) {
            case "TABLE":
                return executeTableOperation(objectName, schema, operation, params, api, configuredParamDTOs);
            case "VIEW":
            case "MATERIALIZED VIEW":
                return postgreSQLViewExecutorUtil.execute(api, sourceObject, objectName, schema,
                        validatedRequest, configuredParamDTOs);
            case "PROCEDURE":
                return postgreSQLProcedureExecutorUtil.execute(api, sourceObject, objectName, schema,
                        validatedRequest, configuredParamDTOs);
            case "FUNCTION":
                return postgreSQLFunctionExecutorUtil.execute(api, sourceObject, objectName, schema,
                        validatedRequest, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported PostgreSQL object type: " + objectType);
        }
    }

    private Object executeTableOperation(String tableName, String schema, String operation,
                                         Map<String, Object> params, GeneratedApiEntity api,
                                         List<ApiParameterDTO> configuredParamDTOs) {

        if (operation == null) {
            operation = "SELECT";
        }

        log.info("Executing {} operation on {}.{} with params: {}", operation, schema, tableName, params.keySet());

        switch (operation.toUpperCase()) {
            case "SELECT":
                return postgreSQLTableExecutorUtil.executeSelect(tableName, schema, params, api, configuredParamDTOs);
            case "INSERT":
                return postgreSQLTableExecutorUtil.executeInsert(tableName, schema, params, api, configuredParamDTOs);
            case "UPDATE":
                return postgreSQLTableExecutorUtil.executeUpdate(tableName, schema, params, api, configuredParamDTOs);
            case "DELETE":
                return postgreSQLTableExecutorUtil.executeDelete(tableName, schema, params, api, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported table operation: " + operation);
        }
    }
}