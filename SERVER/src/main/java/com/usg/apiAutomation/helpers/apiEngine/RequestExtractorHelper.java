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

        // Extract headers - FIXED: Now includes ALL headers
        Map<String, String> headers = extractHeaders(request);
        executeRequest.setHeaders(headers);

        // DEBUG: Log authentication headers specifically
        log.debug("=== AUTH HEADERS EXTRACTED ===");
        if (headers.containsKey("x-api-key")) {
            log.debug("x-api-key found with value: {}", maskSensitiveValue(headers.get("x-api-key")));
        } else {
            log.debug("x-api-key NOT found in headers");
        }

        if (headers.containsKey("x-api-secret")) {
            log.debug("x-api-secret found with value: {}", maskSensitiveValue(headers.get("x-api-secret")));
        } else {
            log.debug("x-api-secret NOT found in headers");
        }

        // Also check case-insensitive variations
        headers.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase("x-api-key"))
                .findFirst()
                .ifPresent(e -> log.debug("Found case-insensitive x-api-key: {} = {}",
                        e.getKey(), maskSensitiveValue(e.getValue())));

        headers.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase("x-api-secret"))
                .findFirst()
                .ifPresent(e -> log.debug("Found case-insensitive x-api-secret: {} = {}",
                        e.getKey(), maskSensitiveValue(e.getValue())));

        // Extract body based on content type
        extractBodyBasedOnContentType(request, executeRequest);

        log.debug("Request extracted - Path params: {}, Query params: {}, Headers: {}, Body: {}",
                pathParams.size(), queryParams.size(), headers.size(),
                executeRequest.getBody() != null ? "present" : "null");

        return executeRequest;
    }

    /**
     * Extract path parameters from URL based on API ID and the API's endpoint pattern
     * ULTRA FLEXIBLE - Handles any URL structure with dynamic prefixes
     */
    private Map<String, Object> extractPathParametersFromUrl(String requestURI, String apiId) {
        Map<String, Object> pathParams = new HashMap<>();

        try {
            // Get the API entity to know its endpoint pattern
            GeneratedApiEntity api = automationEngineService.getApiEntity(apiId);
            String endpointPattern = api.getEndpointPath();

            log.info("Endpoint pattern: {}", endpointPattern);
            log.info("Request URI: {}", requestURI);

            // Find the API ID in the URL - this is our only fixed point
            String apiIdSegment = "/" + apiId;
            int apiIdIndex = requestURI.indexOf(apiIdSegment);

            if (apiIdIndex == -1) {
                log.warn("Could not find API ID {} in request URI", apiId);
                return pathParams;
            }

            // Extract everything after the API ID
            String remainingPath = requestURI.substring(apiIdIndex + apiIdSegment.length());
            log.info("Path after API ID: {}", remainingPath);

            // Remove leading and trailing slashes
            remainingPath = remainingPath.replaceAll("^/+|/+$", "");

            // Clean up the endpoint pattern - remove leading/trailing slashes
            String cleanPattern = endpointPattern.replaceAll("^/+|/+$", "");

            // Split into segments
            String[] requestSegments = remainingPath.split("/");
            String[] patternSegments = cleanPattern.split("/");

            log.info("Request segments: {}", Arrays.toString(requestSegments));
            log.info("Pattern segments: {}", Arrays.toString(patternSegments));

            // SMART MATCHING: Find where the pattern actually starts by matching static segments
            // This handles any prefix: /api/v1, /core/v1/api, /custom/prefix, or no prefix at all

            int requestIdx = 0;
            int patternIdx = 0;

            // First, find where the pattern starts by matching the first static segment
            // Find the first static segment in the pattern (non-parameter)
            String firstStaticSegment = null;
            int firstStaticPatternIndex = -1;

            for (int i = 0; i < patternSegments.length; i++) {
                if (!patternSegments[i].startsWith("{") && !patternSegments[i].endsWith("}")) {
                    firstStaticSegment = patternSegments[i];
                    firstStaticPatternIndex = i;
                    break;
                }
            }

            if (firstStaticSegment != null) {
                // Find this static segment in the request
                for (int i = 0; i < requestSegments.length; i++) {
                    if (requestSegments[i].equals(firstStaticSegment)) {
                        requestIdx = i;
                        patternIdx = firstStaticPatternIndex;
                        log.info("Pattern starts at request segment {} with static segment '{}'",
                                requestIdx, firstStaticSegment);
                        break;
                    }
                }
            }

            // If we couldn't find a static segment match, start from the beginning
            if (patternIdx == 0 && requestIdx == 0 && firstStaticSegment != null) {
                log.warn("Could not find static segment '{}' in request, starting from beginning",
                        firstStaticSegment);
            }

            // Now extract parameters by walking through the pattern
            while (patternIdx < patternSegments.length) {
                String patternSegment = patternSegments[patternIdx];

                // If this is a path parameter
                if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                    String paramName = patternSegment.substring(1, patternSegment.length() - 1);

                    // Check if we have a corresponding request segment
                    if (requestIdx < requestSegments.length) {
                        String value = requestSegments[requestIdx];
                        pathParams.put(paramName, value);
                        log.info("Extracted path parameter: {} = {}", paramName, value);
                        requestIdx++;
                    } else {
                        // No value provided for this parameter
                        pathParams.put(paramName, "");
                        log.warn("Missing path parameter: {} - no value provided in URL", paramName);
                    }
                    patternIdx++;
                }
                // If this is a static segment
                else {
                    // Check if we have a corresponding request segment
                    if (requestIdx < requestSegments.length) {
                        String requestSegment = requestSegments[requestIdx];

                        // If it matches, great! If not, we might have an extra prefix
                        if (patternSegment.equals(requestSegment)) {
                            log.info("Static segment matched: {} = {}", patternSegment, requestSegment);
                            requestIdx++;
                            patternIdx++;
                        } else {
                            // This static segment doesn't match - we might have an extra prefix
                            // Skip ahead in request to find this static segment
                            log.info("Static segment '{}' doesn't match '{}', searching ahead...",
                                    patternSegment, requestSegment);

                            boolean found = false;
                            for (int i = requestIdx + 1; i < requestSegments.length; i++) {
                                if (requestSegments[i].equals(patternSegment)) {
                                    log.info("Found static segment '{}' at position {}", patternSegment, i);
                                    // Skip all segments up to this point (they're prefixes)
                                    requestIdx = i + 1;
                                    patternIdx++;
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                log.warn("Could not find static segment '{}' in remaining URL", patternSegment);
                                patternIdx++;
                            }
                        }
                    } else {
                        log.warn("Ran out of request segments while processing pattern");
                        break;
                    }
                }
            }

            // Log any remaining request segments (they're extra and will be ignored)
            if (requestIdx < requestSegments.length) {
                log.info("Extra request segments ignored: {}",
                        Arrays.toString(Arrays.copyOfRange(requestSegments, requestIdx, requestSegments.length)));
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
     * Extract headers including ALL headers
     * FIXED: Now includes ALL headers without filtering
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);

            // Log all headers at debug level
            log.debug("Header extracted: {} = {}", headerName,
                    headerName.toLowerCase().contains("key") || headerName.toLowerCase().contains("secret")
                            ? maskSensitiveValue(headerValue) : headerValue);
        });

        log.info("Total headers extracted: {}", headers.size());
        return headers;
    }

    /**
     * Mask sensitive values for logging
     */
    private String maskSensitiveValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }



    /**
     * Extract body based on content type, with fallback to content detection
     */
    private void extractBodyBasedOnContentType(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        if (request.getContentLength() <= 0 && request.getContentLengthLong() <= 0) {
            return;
        }

        String contentType = request.getContentType();
        log.debug("Content-Type: {}", contentType);

        // Read the body first to detect its actual format
        String rawBody = null;
        try {
            rawBody = request.getReader().lines().collect(Collectors.joining());
            log.debug("Raw body (first 200 chars): {}",
                    rawBody != null ? rawBody.substring(0, Math.min(200, rawBody.length())) : "null");
        } catch (IOException e) {
            log.error("Failed to read request body: {}", e.getMessage());
            return;
        }

        if (rawBody == null || rawBody.isEmpty()) {
            return;
        }

        // Detect actual content type from the body
        boolean isXml = rawBody.trim().startsWith("<");
        boolean isJson = rawBody.trim().startsWith("{") || rawBody.trim().startsWith("[");

        log.info("Content detection: isXml={}, isJson={}", isXml, isJson);

        // Handle based on actual content type, not just the header
        if (isXml) {
            // It's XML regardless of what the header says
            log.info("Body detected as XML, processing as XML");
            executeRequest.setBody(rawBody);
        }
        else if (isJson) {
            // It's JSON
            log.info("Body detected as JSON, processing as JSON");
            try {
                JsonNode jsonNode = objectMapper.readTree(rawBody);
                if (jsonNode.isObject()) {
                    Map<String, Object> bodyMap = objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {});
                    executeRequest.setBody(bodyMap);
                } else if (jsonNode.isArray()) {
                    List<Object> bodyList = objectMapper.convertValue(jsonNode, new TypeReference<List<Object>>() {});
                    executeRequest.setBody(bodyList);
                } else {
                    executeRequest.setBody(jsonNode.asText());
                }
            } catch (IOException e) {
                log.error("Failed to parse JSON body: {}", e.getMessage());
                executeRequest.setBody(rawBody);
            }
        }
        else {
            // Unknown format - try to parse based on content type
            if (contentType != null) {
                if (contentType.contains("xml")) {
                    executeRequest.setBody(rawBody);
                } else if (contentType.contains("json")) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(rawBody);
                        Map<String, Object> bodyMap = objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {});
                        executeRequest.setBody(bodyMap);
                    } catch (IOException e) {
                        executeRequest.setBody(rawBody);
                    }
                } else {
                    // Store as raw string
                    Map<String, Object> textWrapper = new HashMap<>();
                    textWrapper.put("_raw", rawBody);
                    textWrapper.put("_contentType", contentType);
                    executeRequest.setBody(textWrapper);
                }
            } else {
                // No content type, store as raw
                executeRequest.setBody(rawBody);
            }
        }

        log.debug("Body processed - final type: {}",
                executeRequest.getBody() != null ? executeRequest.getBody().getClass().getSimpleName() : "null");
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
     * Handle application/json requests - FALLBACK if content-type is json
     */
    private void handleJsonRequest(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        // This method is now only called if we specifically need to handle JSON
        // The main detection logic should be used instead
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
     * Handle application/xml requests - FIXED to pass raw XML string
     */
    private void handleXmlRequest(HttpServletRequest request, ExecuteApiRequestDTO executeRequest) {
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            if (!body.isEmpty()) {
                // Store as raw XML string, not wrapped in a map
                executeRequest.setBody(body);
                log.debug("Processed XML request body - length: {} characters", body.length());
                log.debug("XML body preview: {}", body.substring(0, Math.min(200, body.length())));
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