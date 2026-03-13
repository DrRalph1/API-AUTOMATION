package com.usg.apiAutomation.helpers.apiEngine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.*;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiResponseMappingEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.utils.apiEngine.GenUrlBuilderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ApiResponseHelper {

    public GeneratedApiResponseDTO buildGenerateApiResponse(
            GeneratedApiEntity savedApi,
            GenUrlBuilderUtil.GenUrlInfo genUrlInfo,
            String codeBaseRequestId,
            String collectionId,
            String docCollectionId,
            CollectionInfoDTO collectionInfo,
            GenUrlBuilderUtil genUrlBuilder,
            ApiConversionHelper conversionHelper,
            ApiCodeGenerator codeGenerator) {

        GeneratedApiResponseDTO response = conversionHelper.mapToResponse(savedApi);

        // Add generated files
        Map<String, String> generatedFiles = codeGenerator.generateApiCode(savedApi);
        response.setGeneratedFiles(generatedFiles);

        // Build metadata
        Map<String, Object> metadata = buildMetadata(savedApi, genUrlInfo, codeBaseRequestId,
                collectionId, docCollectionId, collectionInfo, genUrlBuilder);

        response.setMetadata(metadata);

        return response;
    }

    public Map<String, Object> buildMetadata(GeneratedApiEntity savedApi,
                                             GenUrlBuilderUtil.GenUrlInfo genUrlInfo,
                                             String codeBaseRequestId,
                                             String collectionId,
                                             String docCollectionId,
                                             CollectionInfoDTO collectionInfo,
                                             GenUrlBuilderUtil genUrlBuilder) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("parametersCount", savedApi.getParameters() != null ? savedApi.getParameters().size() : 0);
        metadata.put("responseMappingsCount", savedApi.getResponseMappings() != null ? savedApi.getResponseMappings().size() : 0);
        metadata.put("headersCount", savedApi.getHeaders() != null ? savedApi.getHeaders().size() : 0);
        metadata.put("generatedAt", LocalDateTime.now().toString());
        metadata.put("codeBaseRequestId", codeBaseRequestId);
        metadata.put("collectionsCollectionId", collectionId);
        metadata.put("documentationCollectionId", docCollectionId);

        // Add gen endpoint information
        metadata.put("genEndpointPath", genUrlInfo.getEndpointPath());
        metadata.put("fullGenUrl", genUrlInfo.getFullUrl());
        metadata.put("exampleGenUrl", genUrlInfo.getExampleUrl());
        metadata.put("genUrlPattern", genUrlInfo.getUrlPattern());
        metadata.put("curlExample", genUrlInfo.getCurlExample());

        // Add parameter information
        if (savedApi.getParameters() != null && !savedApi.getParameters().isEmpty()) {
            metadata.put("parameters", buildParameterInfo(savedApi));
        }

        // Add collection info
        if (collectionInfo != null) {
            Map<String, Object> collectionMetadata = new HashMap<>();
            collectionMetadata.put("collectionId", collectionInfo.getCollectionId());
            collectionMetadata.put("collectionName", collectionInfo.getCollectionName());
            collectionMetadata.put("collectionType", collectionInfo.getCollectionType());
            collectionMetadata.put("folderId", collectionInfo.getFolderId());
            collectionMetadata.put("folderName", collectionInfo.getFolderName());
            metadata.put("collectionInfo", collectionMetadata);
        }

        // Add URLs
        Map<String, String> urls = buildUrls(savedApi, genUrlInfo, codeBaseRequestId,
                collectionId, docCollectionId, genUrlBuilder);
        metadata.put("urls", urls);

        // Add URL template
        metadata.put("urlTemplate", genUrlBuilder.buildUrlTemplate(savedApi));
        metadata.put("pathPlaceholders", genUrlBuilder.buildPathPlaceholders(savedApi));
        metadata.put("queryPlaceholders", genUrlBuilder.buildQueryPlaceholders(savedApi));

        return metadata;
    }

    private Map<String, Object> buildParameterInfo(GeneratedApiEntity api) {
        Map<String, Object> paramInfo = new HashMap<>();
        List<Map<String, Object>> pathParams = new ArrayList<>();
        List<Map<String, Object>> queryParams = new ArrayList<>();
        List<Map<String, Object>> headerParams = new ArrayList<>();
        List<Map<String, Object>> bodyParams = new ArrayList<>();

        if (api.getParameters() == null) {
            return paramInfo;
        }

        api.getParameters().forEach(param -> {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("key", param.getKey() != null ? param.getKey() : "");
            paramMap.put("required", param.getRequired() != null ? param.getRequired() : false);
            paramMap.put("example", param.getExample() != null ? param.getExample() : "");
            paramMap.put("description", param.getDescription() != null ? param.getDescription() : "");
            paramMap.put("parameterType", param.getParameterType() != null ? param.getParameterType() : "query");
            paramMap.put("oracleType", param.getOracleType() != null ? param.getOracleType() : "VARCHAR2");
            paramMap.put("apiType", param.getApiType() != null ? param.getApiType() : "string");

            // FIX: Handle null parameterType safely
            String parameterType = param.getParameterType();
            if (parameterType == null) {
                // Default to query parameter if type is null
                parameterType = "query";
            }

            // Use if-else instead of switch to avoid NullPointerException
            if ("path".equalsIgnoreCase(parameterType)) {
                pathParams.add(paramMap);
            } else if ("query".equalsIgnoreCase(parameterType)) {
                queryParams.add(paramMap);
            } else if ("header".equalsIgnoreCase(parameterType)) {
                headerParams.add(paramMap);
            } else if ("body".equalsIgnoreCase(parameterType)) {
                bodyParams.add(paramMap);
            } else {
                // Default to query for unknown types
                queryParams.add(paramMap);
            }
        });

        if (!pathParams.isEmpty()) paramInfo.put("pathParameters", pathParams);
        if (!queryParams.isEmpty()) paramInfo.put("queryParameters", queryParams);
        if (!headerParams.isEmpty()) paramInfo.put("headerParameters", headerParams);
        if (!bodyParams.isEmpty()) paramInfo.put("bodyParameters", bodyParams);

        // Add summary counts
        Map<String, Integer> summary = new HashMap<>();
        summary.put("total", api.getParameters().size());
        summary.put("path", pathParams.size());
        summary.put("query", queryParams.size());
        summary.put("header", headerParams.size());
        summary.put("body", bodyParams.size());
        paramInfo.put("summary", summary);

        return paramInfo;
    }


    private Map<String, String> buildUrls(GeneratedApiEntity api,
                                          GenUrlBuilderUtil.GenUrlInfo genUrlInfo,
                                          String codeBaseRequestId,
                                          String collectionId,
                                          String docCollectionId,
                                          GenUrlBuilderUtil genUrlBuilder) {
        Map<String, String> urls = new HashMap<>();
        urls.put("codeBase", "/plx/api/code-base/requests/" + codeBaseRequestId);
        urls.put("collections", "/plx/api/collections/collections/" + collectionId);
        urls.put("documentation", "/plx/api/documentation/collections/" + docCollectionId);
        urls.put("genEndpoint", genUrlInfo.getEndpointPath());
        urls.put("fullGenUrl", genUrlInfo.getFullUrl());
        urls.put("exampleGenUrl", genUrlInfo.getExampleUrl());
        urls.put("genUrlPattern", genUrlInfo.getUrlPattern());
        urls.put("curlExample", genUrlInfo.getCurlExample());
        urls.put("urlTemplate", genUrlBuilder.buildUrlTemplate(api));
        return urls;
    }

    public ExecuteApiResponseDTO buildSuccessResponse(Object formattedResponse,
                                                      long executionTime,
                                                      GeneratedApiEntity api) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        if (formattedResponse instanceof List) {
            List<?> resultList = (List<?>) formattedResponse;
            for (Object item : resultList) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    dataList.add(itemMap);
                } else {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("value", item);
                    dataList.add(itemMap);
                }
            }
        } else if (formattedResponse instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) formattedResponse;
            dataList.add(responseMap);
        } else {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("result", formattedResponse);
            dataList.add(itemMap);
        }

        // Add metadata if configured
        if (api.getResponseConfig() != null &&
                Boolean.TRUE.equals(api.getResponseConfig().getIncludeMetadata())) {
            Map<String, Object> metadataMap = new HashMap<>();
            metadataMap.put("executionTimeMs", executionTime);
            metadataMap.put("timestamp", LocalDateTime.now().toString());
            metadataMap.put("apiVersion", api.getVersion());
            metadataMap.put("totalRecords", dataList.size());
            dataList.add(0, metadataMap);
        }

        return ExecuteApiResponseDTO.builder()
                .responseCode(200)
                .message("API executed successfully")
                .data(dataList)
                .success(true)
                .build();
    }

    public ExecuteApiResponseDTO createErrorResponse(int statusCode, String message, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;

        List<Map<String, Object>> errorData = new ArrayList<>();
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", message);
        errorMap.put("timestamp", LocalDateTime.now().toString());
        errorMap.put("executionTimeMs", executionTime);
        errorData.add(errorMap);

        return ExecuteApiResponseDTO.builder()
                .responseCode(statusCode)
                .message(message)
                .data(errorData)
                .success(false)
                .build();
    }

    public ExecuteApiResponseDTO createSafeErrorResponse(Exception e, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        log.error("Error executing API: {}", e.getMessage(), e);

        String userMessage;
        int statusCode;
        Map<String, Object> errorDetails = new HashMap<>();

        if (e instanceof jakarta.validation.ValidationException) {
            userMessage = e.getMessage();
            statusCode = 400;
            log.info("ValidationException caught: {}", userMessage);
        }
        else if (e.getMessage() != null && e.getMessage().contains("Required headers missing")) {
            userMessage = e.getMessage();
            statusCode = 400;
        }
        else if (e.getMessage() != null && e.getMessage().contains("Required parameter")) {
            Pattern pattern = Pattern.compile("'([^']+)'");
            java.util.regex.Matcher matcher = pattern.matcher(e.getMessage());
            if (matcher.find()) {
                String missingParam = matcher.group(1);
                userMessage = "Missing required parameter: '" + missingParam + "'";
                errorDetails.put("missingParameter", missingParam);
            } else {
                userMessage = "Missing required parameter. Please check your request.";
            }
            statusCode = 400;
        }
        else if (e.getMessage() != null && e.getMessage().contains("Invalid column type")) {
            userMessage = "Invalid parameter format. Please check the data types of your parameters.";
            statusCode = 400;
        }
        else if (e.getMessage() != null &&
                (e.getMessage().contains("does not exist") ||
                        e.getMessage().contains("INVALID") ||
                        e.getMessage().contains("compilation error"))) {
            userMessage = e.getMessage();
            statusCode = 400;
        }
        else if (e.getMessage() != null &&
                (e.getMessage().toLowerCase().contains("authentication") ||
                        e.getMessage().toLowerCase().contains("unauthorized"))) {
            userMessage = "Authentication failed. Please check your credentials.";
            statusCode = 401;
        }
        else if (e.getMessage() != null && e.getMessage().toLowerCase().contains("authoriz")) {
            userMessage = "You don't have permission to access this resource.";
            statusCode = 403;
        }
        else if (e.getMessage() != null && e.getMessage().toLowerCase().contains("rate limit")) {
            userMessage = "Rate limit exceeded. Please try again later.";
            statusCode = 429;
        }
        else if (e.getMessage() != null && e.getMessage().contains("ORA-")) {
            Pattern pattern = Pattern.compile("(ORA-\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(e.getMessage());
            String oraCode = matcher.find() ? matcher.group(1) : "unknown";
            log.warn("Oracle error {}: {}", oraCode, e.getMessage());

            userMessage = "A database error occurred while processing your request.";

            if (oraCode.equals("ORA-00942") || oraCode.equals("ORA-00904")) {
                userMessage = "The database object referenced in this API does not exist or is invalid.";
                errorDetails.put("action", "Please check that all database objects exist and are accessible.");
            } else if (oraCode.equals("ORA-01031")) {
                userMessage = "Insufficient privileges to access the database object.";
                errorDetails.put("action", "Please contact your database administrator.");
            } else if (oraCode.equals("ORA-06550")) {
                userMessage = "Invalid parameters provided for the database procedure/function.";
                errorDetails.put("action", "Please check parameter names and data types.");
            } else if (oraCode.equals("ORA-01400")) {
                userMessage = "A required value is missing for a NOT NULL column.";
                errorDetails.put("action", "Please provide all required parameters.");
            } else if (oraCode.equals("ORA-12899")) {
                userMessage = "Value too large for the target column.";
                errorDetails.put("action", "Please check the length of your input values.");
            }
            statusCode = 500;
        }
        else {
            userMessage = "An unexpected error occurred while processing your request.";
            statusCode = 500;
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", userMessage);
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        if (!errorDetails.isEmpty()) {
            errorResponse.put("details", errorDetails);
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(errorResponse);

        return ExecuteApiResponseDTO.builder()
                .responseCode(statusCode)
                .message(userMessage)
                .data(dataList)
                .success(false)
                .build();
    }

    public ApiTestResultDTO buildTestResult(ApiTestRequestDTO testRequest, boolean passed,
                                            long executionTime, int statusCode, Object data) {
        return ApiTestResultDTO.builder()
                .testName(testRequest.getTestName())
                .passed(passed)
                .executionTimeMs(executionTime)
                .statusCode(statusCode)
                .actualResponse(data)
                .message(passed ? "Test passed" : "Test failed - response mismatch")
                .build();
    }

    public boolean compareResponses(ExecuteApiResponseDTO actual, Object expected, ObjectMapper objectMapper) {
        try {
            if (expected == null) return true;

            String actualJson = objectMapper.writeValueAsString(actual.getData());
            String expectedJson = objectMapper.writeValueAsString(expected);

            return actualJson.equals(expectedJson);

        } catch (Exception e) {
            return false;
        }
    }

    public Object formatResponse(GeneratedApiEntity api, Object data) {
        if (api.getResponseConfig() != null && Boolean.TRUE.equals(api.getResponseConfig().getIncludeMetadata())) {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("data", data);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timestamp", LocalDateTime.now().toString());
            metadata.put("apiVersion", api.getVersion());
            metadata.put("requestId", UUID.randomUUID().toString());

            if (api.getResponseConfig().getMetadataFields() != null && !api.getResponseConfig().getMetadataFields().isEmpty()) {
                Map<String, Object> filteredMetadata = new HashMap<>();
                for (String field : api.getResponseConfig().getMetadataFields()) {
                    if (field != null && metadata.containsKey(field)) {
                        filteredMetadata.put(field, metadata.get(field));
                    }
                }
                if (!filteredMetadata.isEmpty()) {
                    formatted.put("metadata", filteredMetadata);
                }
            } else {
                formatted.put("metadata", metadata);
            }

            return formatted;
        }

        return data;
    }

    public Object generateSampleResponse(GeneratedApiEntity api) {
        Map<String, Object> result = new HashMap<>();

        if (api.getResponseMappings() != null && !api.getResponseMappings().isEmpty()) {
            for (ApiResponseMappingEntity mapping : api.getResponseMappings()) {
                if (Boolean.TRUE.equals(mapping.getIncludeInResponse())) {
                    switch (mapping.getApiType() != null ? mapping.getApiType() : "string") {
                        case "integer":
                        case "number":
                            result.put(mapping.getApiField(), 1);
                            break;
                        case "boolean":
                            result.put(mapping.getApiField(), true);
                            break;
                        case "string":
                        default:
                            if ("date".equals(mapping.getFormat())) {
                                result.put(mapping.getApiField(), LocalDateTime.now().toString());
                            } else {
                                result.put(mapping.getApiField(), "sample_" + mapping.getApiField());
                            }
                            break;
                    }
                }
            }
        } else {
            result.put("message", "API executed successfully");
            result.put("api", api.getApiCode());
            result.put("timestamp", LocalDateTime.now().toString());
        }

        return result;
    }

    @FunctionalInterface
    public interface ApiCodeGenerator {
        Map<String, String> generateApiCode(GeneratedApiEntity api);
    }
}