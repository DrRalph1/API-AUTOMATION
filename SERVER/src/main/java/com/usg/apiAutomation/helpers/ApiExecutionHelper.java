package com.usg.apiAutomation.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiTestEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.helpers.apiEngine.ApiConversionHelper;
import com.usg.apiAutomation.repositories.apiGenerationEngine.ApiExecutionLogRepository;
import com.usg.apiAutomation.repositories.apiGenerationEngine.ApiTestRepository;
import com.usg.apiAutomation.repositories.apiGenerationEngine.GeneratedAPIRepository;
import com.usg.apiAutomation.utils.apiEngine.DatabaseParameterGeneratorUtil;

import java.util.List;

public interface ApiExecutionHelper {

    GeneratedApiEntity createAndSaveApiEntity(
            GenerateApiRequestDTO request,
            ApiSourceObjectDTO sourceObjectDTO,
            CollectionInfoDTO collectionInfo,
            String endpointPath,
            String performedBy,
            String sourceRequestId,
            GeneratedAPIRepository repository,
            ObjectMapper objectMapper,
            DatabaseParameterGeneratorUtil parameterGenerator,
            ApiConversionHelper conversionHelper,
            String databaseType);

    GeneratedApiEntity getApiEntity(GeneratedAPIRepository repository, String apiId);

    void updateApiEntity(GeneratedApiEntity api, GenerateApiRequestDTO request,
                         ApiSourceObjectDTO sourceObjectDTO, CollectionInfoDTO collectionInfo,
                         String performedBy);

    void clearApiRelationships(GeneratedApiEntity api);

    void recreateApiRelationships(GeneratedApiEntity api, GenerateApiRequestDTO request,
                                  ApiSourceObjectDTO sourceObjectDTO,
                                  DatabaseParameterGeneratorUtil parameterGenerator,
                                  ApiConversionHelper conversionHelper);

    ExecuteApiRequestDTO prepareValidatedRequest(GeneratedApiEntity api, ExecuteApiRequestDTO executeRequest);

    void logExecution(ApiExecutionLogRepository logRepository, GeneratedApiEntity api,
                      ExecuteApiRequestDTO request, Object response, int statusCode,
                      long executionTime, String performedBy, String clientIp,
                      String userAgent, String errorMessage, ObjectMapper objectMapper);

    void updateApiStats(GeneratedApiEntity api, GeneratedAPIRepository repository);

    ApiTestEntity saveTestResult(ApiTestRepository testRepository, GeneratedApiEntity api,
                                 ApiTestRequestDTO testRequest, ExecuteApiResponseDTO executionResult,
                                 boolean passed, long executionTime, String performedBy,
                                 ObjectMapper objectMapper);

    Object executeAgainstDatabase(GeneratedApiEntity api,
                                  ApiSourceObjectDTO sourceObject,
                                  ExecuteApiRequestDTO validatedRequest,
                                  List<ApiParameterDTO> configuredParamDTOs,
                                  String databaseType);
}