package com.usg.autoAPIGenerator.helpers.apiEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.*;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.ApiResponseMappingEntity;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.utils.apiEngine.GenUrlBuilderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

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

        ExecuteApiResponseDTO response = new ExecuteApiResponseDTO();
        response.setResponseCode(200);
        response.setSuccess(true);
        response.setMessage("API executed successfully");

        // DON'T set executionTimeMs and correlationId if you want them removed
        // response.setExecutionTimeMs(executionTime); // Comment this out

        // Extract the actual data without the list wrapper
        Object simplifiedData = extractActualData(formattedResponse);
        response.setData(simplifiedData);

        return response;
    }

    /**
     * Extract just the actual business data from the response
     */
    private Object extractActualData(Object formattedResponse) {
        if (formattedResponse == null) {
            return null;
        }

        // If it's a List, check if it contains the actual data
        if (formattedResponse instanceof List) {
            List<?> responseList = (List<?>) formattedResponse;

            // Look for the actual business data in the list
            for (Object item : responseList) {
                if (item instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) item;

                    // Check if this item has the business response fields
                    if (itemMap.containsKey("response_code") ||
                            itemMap.containsKey("mess") ||
                            itemMap.containsKey("batchnumber")) {
                        return item;
                    }
                }
            }

            // If list has one item, return it directly
            if (responseList.size() == 1) {
                return responseList.get(0);
            }

            // Otherwise return the list (might be multiple records)
            return responseList;
        }

        // If it's a Map, try to extract the actual data
        if (formattedResponse instanceof Map) {
            Map<?, ?> responseMap = (Map<?, ?>) formattedResponse;

            // Skip metadata wrapper if present
            if (responseMap.containsKey("data")) {
                return extractActualData(responseMap.get("data"));
            }

            // If it has the business fields directly, return it
            if (responseMap.containsKey("response_code") ||
                    responseMap.containsKey("mess") ||
                    responseMap.containsKey("batchnumber")) {
                return responseMap;
            }
        }

        return formattedResponse;
    }

    public ExecuteApiResponseDTO createErrorResponse(int statusCode, String message, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;

        ExecuteApiResponseDTO response = new ExecuteApiResponseDTO();
        response.setResponseCode(statusCode);
        response.setSuccess(false);
        response.setMessage(message);
//        response.setExecutionTimeMs(executionTime);

        // Create a simple error object instead of a list
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", message);
        errorData.put("timestamp", LocalDateTime.now().toString());

        response.setData(errorData);

        return response;
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
        // If data is null, return empty list
        if (data == null) {
            return Collections.emptyList();
        }

        log.info("Formatting response - raw data type: {}", data.getClass().getSimpleName());

        // CRITICAL FIX: Flatten nested data.data structure
        Object flattenedData = dynamicallyFlattenData(data);

        // Log the flattened result for debugging
        if (flattenedData instanceof List) {
            log.info("Flattened data is a List with {} items", ((List<?>) flattenedData).size());
        } else if (flattenedData instanceof Map) {
            log.info("Flattened data is a Map with keys: {}", ((Map<?, ?>) flattenedData).keySet());
        }

        // Only add metadata if configured (commented out for now)
        // if (api.getResponseConfig() != null && Boolean.TRUE.equals(api.getResponseConfig().getIncludeMetadata())) {
        //     Map<String, Object> formatted = new HashMap<>();
        //     formatted.put("data", flattenedData);
        //
        //     Map<String, Object> metadata = new HashMap<>();
        //     metadata.put("timestamp", LocalDateTime.now().toString());
        //     metadata.put("apiVersion", api.getVersion());
        //     metadata.put("requestId", UUID.randomUUID().toString());
        //
        //     if (api.getResponseConfig().getMetadataFields() != null && !api.getResponseConfig().getMetadataFields().isEmpty()) {
        //         Map<String, Object> filteredMetadata = new HashMap<>();
        //         for (String field : api.getResponseConfig().getMetadataFields()) {
        //             if (field != null && metadata.containsKey(field)) {
        //                 filteredMetadata.put(field, metadata.get(field));
        //             }
        //         }
        //         if (!filteredMetadata.isEmpty()) {
        //             formatted.put("metadata", filteredMetadata);
        //         }
        //     } else {
        //         formatted.put("metadata", metadata);
        //     }
        //
        //     return formatted;
        // }

        return flattenedData;
    }

    /**
     * Dynamically flattens any nested data structure by removing all wrapper objects
     * until reaching the actual business data. Continues recursively until no more
     * single-key wrappers or common data containers are found.
     */
    private Object dynamicallyFlattenData(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }

        // Handle Map objects
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;

            // If map is empty, return it
            if (dataMap.isEmpty()) {
                return dataMap;
            }

            // STRATEGY 1: If map has only one entry, the value is likely the actual data
            if (dataMap.size() == 1) {
                Map.Entry<?, ?> singleEntry = dataMap.entrySet().iterator().next();
                Object value = singleEntry.getValue();

                // Recursively flatten the value (it might be another wrapper)
                if (value instanceof Map || value instanceof List) {
                    log.debug("Flattening single-key wrapper: '{}' -> {}",
                            singleEntry.getKey(), value.getClass().getSimpleName());
                    return dynamicallyFlattenData(value);
                }
            }

            // STRATEGY 2: Look for common data container field names
            String[] commonWrappers = {"data", "result", "JSON_RESULT", "response",
                    "body", "content", "items", "records", "payload",
                    "output", "results", "Data", "Result"};

            for (String wrapperName : commonWrappers) {
                if (dataMap.containsKey(wrapperName)) {
                    Object wrappedValue = dataMap.get(wrapperName);
                    if (wrappedValue instanceof Map || wrappedValue instanceof List) {
                        log.debug("Found common wrapper field '{}' -> flattening", wrapperName);
                        return dynamicallyFlattenData(wrappedValue);
                    }
                }
            }

            // STRATEGY 3: Check if any value in the map contains business data
            // Look for lists that aren't empty and contain maps with multiple fields
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                Object value = entry.getValue();

                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty()) {
                        Object firstItem = list.get(0);
                        // If list contains maps that have multiple keys (likely business data)
                        if (firstItem instanceof Map && ((Map<?, ?>) firstItem).size() > 1) {
                            log.debug("Found business data list under key: '{}'", entry.getKey());
                            // Don't flatten further - this is likely the actual data
                            return dataMap;
                        }
                    }
                }

                // Also check for nested maps that contain lists
                if (value instanceof Map) {
                    Map<?, ?> nestedMap = (Map<?, ?>) value;
                    for (Object nestedValue : nestedMap.values()) {
                        if (nestedValue instanceof List) {
                            List<?> nestedList = (List<?>) nestedValue;
                            if (!nestedList.isEmpty() && nestedList.get(0) instanceof Map) {
                                log.debug("Found business data in nested map under key: '{}'", entry.getKey());
                                return dynamicallyFlattenData(value);
                            }
                        }
                    }
                }
            }

            // If we couldn't flatten further, return the map as-is
            return dataMap;
        }

        // Handle List objects
        if (data instanceof List) {
            List<?> dataList = (List<?>) data;
            if (dataList.isEmpty()) {
                return dataList;
            }

            // Check each item in the list for wrappers
            Object firstItem = dataList.get(0);

            // If list contains maps that are likely wrappers, try to flatten them
            if (firstItem instanceof Map) {
                Map<?, ?> firstMap = (Map<?, ?>) firstItem;

                // If the map has a single key that contains a list, flatten it
                if (firstMap.size() == 1) {
                    Object onlyValue = firstMap.values().iterator().next();
                    if (onlyValue instanceof List) {
                        log.debug("Flattening list of single-key map wrappers");
                        return dynamicallyFlattenData(onlyValue);
                    }
                }

                // Check for common wrapper fields in list items
                for (String wrapperName : new String[]{"data", "result", "JSON_RESULT", "response"}) {
                    if (firstMap.containsKey(wrapperName)) {
                        Object wrappedValue = firstMap.get(wrapperName);
                        if (wrappedValue instanceof List) {
                            log.debug("Found wrapper field '{}' in list item, flattening", wrapperName);
                            return dynamicallyFlattenData(wrappedValue);
                        }
                    }
                }
            }

            // If the list contains business objects (maps with multiple fields), return it
            if (firstItem instanceof Map && ((Map<?, ?>) firstItem).size() > 1) {
                return dataList;
            }

            // Recursively flatten each item in the list if needed
            List<Object> flattenedList = new ArrayList<>();
            boolean needFlattening = false;
            for (Object item : dataList) {
                Object flattenedItem = dynamicallyFlattenData(item);
                if (flattenedItem != item) {
                    needFlattening = true;
                }
                flattenedList.add(flattenedItem);
            }

            return needFlattening ? flattenedList : dataList;
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