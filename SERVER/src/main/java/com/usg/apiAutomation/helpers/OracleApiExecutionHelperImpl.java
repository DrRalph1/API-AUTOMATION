package com.usg.apiAutomation.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.helpers.apiEngine.ApiConversionHelper;
import com.usg.apiAutomation.helpers.apiEngine.ApiResponseHelper;
import com.usg.apiAutomation.utils.apiEngine.DatabaseParameterGeneratorUtil;
import com.usg.apiAutomation.utils.apiEngine.executor.oracle.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class OracleApiExecutionHelperImpl extends BaseApiExecutionHelper {

    private final OracleTableExecutorUtil oracleTableExecutorUtil;
    private final OracleViewExecutorUtil oracleViewExecutorUtil;
    private final OracleProcedureExecutorUtil oracleProcedureExecutorUtil;
    private final OracleFunctionExecutorUtil oracleFunctionExecutorUtil;
    private final OraclePackageExecutorUtil oraclePackageExecutorUtil;

    public OracleApiExecutionHelperImpl(
            ApiResponseHelper responseHelper,
            LoggerUtil loggerUtil,
            ApiConversionHelper conversionHelper,
            OracleTableExecutorUtil oracleTableExecutorUtil,
            OracleViewExecutorUtil oracleViewExecutorUtil,
            OracleProcedureExecutorUtil oracleProcedureExecutorUtil,
            OracleFunctionExecutorUtil oracleFunctionExecutorUtil,
            OraclePackageExecutorUtil oraclePackageExecutorUtil) {
        super(responseHelper, loggerUtil, conversionHelper);
        this.oracleTableExecutorUtil = oracleTableExecutorUtil;
        this.oracleViewExecutorUtil = oracleViewExecutorUtil;
        this.oracleProcedureExecutorUtil = oracleProcedureExecutorUtil;
        this.oracleFunctionExecutorUtil = oracleFunctionExecutorUtil;
        this.oraclePackageExecutorUtil = oraclePackageExecutorUtil;
    }

    @Override
    protected void setupApiRelationships(GeneratedApiEntity api,
                                         GenerateApiRequestDTO request,
                                         ApiSourceObjectDTO sourceObjectDTO,
                                         DatabaseParameterGeneratorUtil parameterGenerator,
                                         ApiConversionHelper conversionHelper) {

        // Ensure collections are initialized
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
            api.getParameters().add(param);  // ADD to existing collection
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

        log.debug("Setup relationships for Oracle API: {}", api.getId());
    }

    @Override
    public Object executeAgainstDatabase(GeneratedApiEntity api,
                                         ApiSourceObjectDTO sourceObject,
                                         ExecuteApiRequestDTO validatedRequest,
                                         List<ApiParameterDTO> configuredParamDTOs) {

        log.info("Executing Oracle operation for API: {}", api.getId());

        ApiSchemaConfigEntity schemaConfig = api.getSchemaConfig();
        if (schemaConfig == null) {
            throw new RuntimeException("Schema configuration not found");
        }

        String objectType = schemaConfig.getObjectType();
        String objectName = schemaConfig.getObjectName();
        String owner = schemaConfig.getSchemaName();

        switch (objectType.toUpperCase()) {
            case "TABLE":
                return executeOracleTable(api, sourceObject, objectName, owner,
                        schemaConfig.getOperation(), validatedRequest, configuredParamDTOs);
            case "VIEW":
                return executeOracleView(api, sourceObject, objectName, owner,
                        validatedRequest, configuredParamDTOs);
            case "PROCEDURE":
                return executeOracleProcedure(api, sourceObject, objectName, owner,
                        validatedRequest, configuredParamDTOs);
            case "FUNCTION":
                return executeOracleFunction(api, sourceObject, objectName, owner,
                        validatedRequest, configuredParamDTOs);
            case "PACKAGE":
                return executeOraclePackage(api, sourceObject, objectName, owner,
                        validatedRequest, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported Oracle object type: " + objectType);
        }
    }

    private Object executeOracleTable(GeneratedApiEntity api,
                                      ApiSourceObjectDTO sourceObject,
                                      String tableName,
                                      String owner,
                                      String operation,
                                      ExecuteApiRequestDTO validatedRequest,
                                      List<ApiParameterDTO> configuredParamDTOs) {

        // Create consolidated params
        Map<String, Object> params = new HashMap<>();
        if (validatedRequest.getPathParams() != null) params.putAll(validatedRequest.getPathParams());
        if (validatedRequest.getQueryParams() != null) params.putAll(validatedRequest.getQueryParams());
        if (validatedRequest.getBody() instanceof Map) {
            params.putAll((Map<String, Object>) validatedRequest.getBody());
        }

        switch (operation.toUpperCase()) {
            case "SELECT":
                return oracleTableExecutorUtil.executeSelect(tableName, owner, params, api, configuredParamDTOs);
            case "INSERT":
                return oracleTableExecutorUtil.executeInsert(tableName, owner, params, api, configuredParamDTOs);
            case "UPDATE":
                return oracleTableExecutorUtil.executeUpdate(tableName, owner, params, api, configuredParamDTOs);
            case "DELETE":
                return oracleTableExecutorUtil.executeDelete(tableName, owner, params, api, configuredParamDTOs);
            default:
                throw new RuntimeException("Unsupported table operation: " + operation);
        }
    }

    private Object executeOracleView(GeneratedApiEntity api,
                                     ApiSourceObjectDTO sourceObject,
                                     String viewName,
                                     String owner,
                                     ExecuteApiRequestDTO validatedRequest,
                                     List<ApiParameterDTO> configuredParamDTOs) {

        // OracleViewExecutorUtil.execute signature: (api, sourceObject, viewName, owner, request, configuredParamDTOs)
        return oracleViewExecutorUtil.execute(api, sourceObject, viewName, owner, validatedRequest, configuredParamDTOs);
    }

    private Object executeOracleProcedure(GeneratedApiEntity api,
                                          ApiSourceObjectDTO sourceObject,
                                          String procedureName,
                                          String owner,
                                          ExecuteApiRequestDTO validatedRequest,
                                          List<ApiParameterDTO> configuredParamDTOs) {

        // OracleProcedureExecutorUtil.execute signature: (api, sourceObject, procedureName, owner, request, configuredParamDTOs)
        return oracleProcedureExecutorUtil.execute(api, sourceObject, procedureName, owner, validatedRequest, configuredParamDTOs);
    }

    private Object executeOracleFunction(GeneratedApiEntity api,
                                         ApiSourceObjectDTO sourceObject,
                                         String functionName,
                                         String owner,
                                         ExecuteApiRequestDTO validatedRequest,
                                         List<ApiParameterDTO> configuredParamDTOs) {

        // OracleFunctionExecutorUtil.execute signature: (api, sourceObject, functionName, owner, request, configuredParamDTOs)
        return oracleFunctionExecutorUtil.execute(api, sourceObject, functionName, owner, validatedRequest, configuredParamDTOs);
    }

    private Object executeOraclePackage(GeneratedApiEntity api,
                                        ApiSourceObjectDTO sourceObject,
                                        String packageName,
                                        String owner,
                                        ExecuteApiRequestDTO validatedRequest,
                                        List<ApiParameterDTO> configuredParamDTOs) {

        // Ensure sourceObject has packageProcedure set
        if (sourceObject == null || sourceObject.getPackageProcedure() == null) {
            log.warn("Package procedure not specified in sourceObject, using default");
            // Create a temporary sourceObject with packageProcedure if needed
            if (sourceObject == null) {
                sourceObject = new ApiSourceObjectDTO();
            }
            // Set packageProcedure from schema config if available
            if (api.getSchemaConfig() != null && api.getSchemaConfig().getObjectName() != null) {
                // If objectName contains dot, split it
                String fullName = api.getSchemaConfig().getObjectName();
                if (fullName.contains(".")) {
                    String[] parts = fullName.split("\\.");
                    sourceObject.setPackageProcedure(parts[1]);
                } else {
                    sourceObject.setPackageProcedure(packageName);
                }
            }
        }

        // OraclePackageExecutorUtil.execute signature: (api, sourceObject, packageName, owner, request, configuredParamDTOs)
        return oraclePackageExecutorUtil.execute(api, sourceObject, packageName, owner, validatedRequest, configuredParamDTOs);
    }
}