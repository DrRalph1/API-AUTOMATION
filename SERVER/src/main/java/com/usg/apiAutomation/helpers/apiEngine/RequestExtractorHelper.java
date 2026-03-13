package com.usg.apiAutomation.helpers.apiEngine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiParameterEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiAutomation.services.AutomationEngineService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RequestExtractorHelper {

    private final ObjectMapper objectMapper;

    public RequestExtractorHelper() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Extract all components from HTTP request and populate ExecuteApiRequestDTO
     */
    public ExecuteApiRequestDTO extractRequestComponents(
            HttpServletRequest request,
            String requestId,
            String apiId) {

        ExecuteApiRequestDTO executeRequest = new ExecuteApiRequestDTO();
        executeRequest.setRequestId(requestId);

        // Extract path parameters from URL using the API's endpoint pattern
        Map<String, Object> pathParams = extractPathParametersFromUrl(request.getRequestURI(), apiId);
        executeRequest.setPathParams(pathParams);

        // Extract query parameters
        Map<String, Object> queryParams = extractQueryParams(request);
        executeRequest.setQueryParams(queryParams);

        // Extract headers
        Map<String, String> headers = extractHeaders(request);
        executeRequest.setHeaders(headers);

        // Extract body based on content type
        extractBodyBasedOnContentType(request, executeRequest);

        log.debug("Request extracted - Path params: {}, Query params: {}, Headers: {}, Body: {}",
                pathParams.size(), queryParams.size(), headers.size(),
                executeRequest.getBody() != null ? "present" : "null");

        return executeRequest;
    }

    /**
     * Extract path parameters from URL based on API ID and the API's endpoint pattern
     */
    private Map<String, Object> extractPathParametersFromUrl(String requestURI, String apiId) {
        Map<String, Object> pathParams = new HashMap<>();

        try {
            // Get the API entity to know its endpoint pattern
            GeneratedApiEntity api = automationEngineService.getApiEntity(apiId);
            String endpointPattern = api.getEndpointPath();

            log.info("Endpoint pattern: {}", endpointPattern);
            log.info("Request URI: {}", requestURI);

            // Extract the part after /plx/api/gen/{apiId}/
            String basePath = "/plx/api/gen/" + apiId;
            if (!requestURI.startsWith(basePath)) {
                log.warn("Request URI does not start with expected base path: {}", basePath);
                return pathParams;
            }

            String requestPath = requestURI.substring(basePath.length());
            log.info("Request path: {}", requestPath);

            // Remove leading and trailing slashes
            requestPath = requestPath.replaceAll("^/+|/+$", "");

            // Clean up the endpoint pattern - remove leading/trailing slashes
            String cleanPattern = endpointPattern.replaceAll("^/+|/+$", "");

            // Split into segments
            String[] requestSegments = requestPath.split("/");
            String[] patternSegments = cleanPattern.split("/");

            log.info("Request segments: {}", Arrays.toString(requestSegments));
            log.info("Pattern segments: {}", Arrays.toString(patternSegments));

            // Find where the pattern actually starts in the request segments
            int patternStartIndex = -1;
            for (int i = 0; i < requestSegments.length; i++) {
                if (requestSegments[i].equals(patternSegments[0])) {
                    patternStartIndex = i;
                    break;
                }
            }

            if (patternStartIndex == -1) {
                log.warn("Could not find pattern start in request segments. Using default matching.");
                patternStartIndex = 0;
            } else {
                log.info("Pattern starts at request segment index: {}", patternStartIndex);
            }

            // Now match the pattern against the request segments starting from patternStartIndex
            for (int i = 0; i < patternSegments.length; i++) {
                int requestIndex = patternStartIndex + i;

                String patternSegment = patternSegments[i];

                // If pattern segment is a placeholder like {acct_link}
                if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                    String paramName = patternSegment.substring(1, patternSegment.length() - 1);

                    // Check if we have a value for this parameter in the request
                    if (requestIndex < requestSegments.length) {
                        // We have a value
                        String value = requestSegments[requestIndex];
                        pathParams.put(paramName, value);
                        log.info("Extracted path parameter: {} = {}", paramName, value);
                    } else {
                        // No value provided for this path parameter
                        pathParams.put(paramName, "");
                        log.warn("Missing path parameter: {} - no value provided in URL", paramName);
                    }
                } else {
                    // Static segment - just log it
                    if (requestIndex < requestSegments.length) {
                        log.info("Static segment matched: {} = {}", patternSegment, requestSegments[requestIndex]);
                    } else {
                        log.warn("Missing static segment: {} - URL is too short", patternSegment);
                    }
                }
            }

            log.info("Final extracted path params: {}", pathParams);

        } catch (Exception e) {
            log.error("Failed to extract path parameters: {}", e.getMessage(), e);
        }

        return pathParams;
    }


    // You need to inject the service to fetch the API entity
    // Add this to the class:
    @Autowired
    private AutomationEngineService automationEngineService;

    private GeneratedApiEntity getApiEntity(String apiId) {
        return automationEngineService.getApiEntity(apiId);
    }

    /**
     * Extract query parameters from request
     */
    private Map<String, Object> extractQueryParams(HttpServletRequest request) {
        Map<String, Object> queryParams = new HashMap<>();

        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                queryParams.put(key, values.length == 1 ? values[0] : Arrays.asList(values));
            }
        });

        return queryParams;
    }

    /**
     * Extract headers excluding content-type and content-length
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            if (!"content-type".equalsIgnoreCase(headerName) &&
                    !"content-length".equalsIgnoreCase(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        });

        return headers;
    }

    /**
     * Extract body based on content type
     */
    private void extractBodyBasedOnContentType(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        if (request.getContentLength() <= 0 && request.getContentLengthLong() <= 0) {
            return;
        }

        String contentType = request.getContentType();

        if (contentType != null) {
            if (contentType.contains("multipart/form-data")) {
                handleMultipartRequest(request, executeRequest);
            } else if (contentType.contains("application/x-www-form-urlencoded")) {
                handleUrlEncodedRequest(request, executeRequest);
            } else if (contentType.contains("application/json")) {
                handleJsonRequest(request, executeRequest);
            } else if (contentType.contains("application/xml")) {
                handleXmlRequest(request, executeRequest);
            } else {
                handleRawRequest(request, executeRequest);
            }
        }
    }

    /**
     * Handle multipart/form-data requests
     */
    private void handleMultipartRequest(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        Map<String, Object> bodyParams = new HashMap<>();

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

            // Handle regular form fields
            multipartRequest.getParameterMap().forEach((key, values) -> {
                if (values.length > 0 && !values[0].isEmpty()) {
                    bodyParams.put(key, values.length == 1 ? values[0] : Arrays.asList(values));
                }
            });

            // Handle file parts
            multipartRequest.getFileMap().forEach((key, file) -> {
                try {
                    if (file.getContentType() != null && file.getContentType().startsWith("text/")) {
                        bodyParams.put(key, new String(file.getBytes()));
                    } else {
                        Map<String, Object> fileInfo = extractFileInfo(file);
                        bodyParams.put(key, fileInfo);
                    }
                } catch (IOException e) {
                    log.warn("Failed to read file part: {}", key);
                }
            });

            log.debug("Processed multipart/form-data with {} fields", bodyParams.size());
        } else {
            // Fallback
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0 && !values[0].isEmpty()) {
                    bodyParams.put(key, values.length == 1 ? values[0] : Arrays.asList(values));
                }
            });
        }

        executeRequest.setBody(bodyParams);
    }

    /**
     * Extract file information from multipart file
     */
    private Map<String, Object> extractFileInfo(MultipartFile file) throws IOException {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("filename", file.getOriginalFilename());
        fileInfo.put("contentType", file.getContentType());
        fileInfo.put("size", file.getSize());
        fileInfo.put("bytes", file.getBytes()); // Be careful with large files
        return fileInfo;
    }

    /**
     * Handle application/x-www-form-urlencoded requests
     */
    private void handleUrlEncodedRequest(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        Map<String, Object> bodyParams = new HashMap<>();

        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0 && !values[0].isEmpty()) {
                bodyParams.put(key, values.length == 1 ? values[0] : Arrays.asList(values));
            }
        });

        executeRequest.setBody(bodyParams);
        log.debug("Processed URL-encoded form with {} fields", bodyParams.size());
    }

    /**
     * Handle application/json requests
     */
    private void handleJsonRequest(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            if (!body.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(body);

                if (jsonNode.isObject()) {
                    Map<String, Object> bodyMap = objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {});
                    executeRequest.setBody(bodyMap);
                } else if (jsonNode.isArray()) {
                    List<Object> bodyList = objectMapper.convertValue(jsonNode, new TypeReference<List<Object>>() {});
                    executeRequest.setBody(bodyList);
                } else {
                    executeRequest.setBody(jsonNode.asText());
                }

                log.debug("Processed JSON request body");
            }
        } catch (IOException e) {
            log.error("Failed to parse JSON body: {}", e.getMessage());
        }
    }

    /**
     * Handle application/xml requests
     */
    private void handleXmlRequest(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            if (!body.isEmpty()) {
                Map<String, Object> xmlWrapper = new HashMap<>();
                xmlWrapper.put("_xml", body);
                executeRequest.setBody(xmlWrapper);
                log.debug("Processed XML request body");
            }
        } catch (IOException e) {
            log.error("Failed to read XML body: {}", e.getMessage());
        }
    }

    /**
     * Handle raw text or unknown content types
     */
    private void handleRawRequest(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            if (!body.isEmpty()) {
                Map<String, Object> textWrapper = new HashMap<>();
                textWrapper.put("_raw", body);
                textWrapper.put("_contentType", request.getContentType());
                executeRequest.setBody(textWrapper);
                log.debug("Processed raw request body");
            }
        } catch (IOException e) {
            log.error("Failed to read raw body: {}", e.getMessage());
        }
    }

    /**
     * Extract client IP considering proxy headers
     */
    public String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // If multiple IPs in X-Forwarded-For, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}