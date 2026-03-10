package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiResponseDTO;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.APIGenerationEngineService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/gen")
@RequiredArgsConstructor
@Tag(name = "API EXECUTION ENGINE", description = "Endpoints for executing generated APIs")
public class ApiExecutionController {

    private final APIGenerationEngineService apiGenerationEngineService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    @PostConstruct
    public void init() {
        log.info("ApiExecutionController initialized and mapped to: /plx/api/gen/{{apiId}}/**");
    }

    @RequestMapping(
            value = "/{apiId}/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD,
                    RequestMethod.OPTIONS}
    )
    @Operation(summary = "Execute API by ID", description = "Execute a generated API using its ID in the URL path")
    public ResponseEntity<?> executeApiById(
            @PathVariable String apiId,
            HttpServletRequest req,
            @RequestBody(required = false) Object jsonBody,
            @RequestParam(required = false) MultiValueMap<String, String> formParams) {

        // Generate unique request ID for tracking
        String requestId = UUID.randomUUID().toString();

        // Log request details
        log.debug("Request ID: {} - Received {} request for API ID: {}", requestId, req.getMethod(), apiId);
        log.debug("Request ID: {} - Full URL: {}", requestId, req.getRequestURL().toString());
        log.debug("Request ID: {} - Content Type: {}", requestId, req.getContentType());

        try {
            // Extract the remaining path after /gen/{apiId}
            String remainingPath = extractRemainingPath(req, apiId);
            log.debug("Request ID: {} - Remaining path: {}", requestId, remainingPath);

            // Extract performer information
            String performedBy = jwtHelper.extractPerformedBy(req);
            String clientIp = extractClientIp(req);
            String userAgent = req.getHeader("User-Agent");

            // Log the request
            loggerUtil.log("apiGeneration", String.format(
                    "Request ID: %s, Executing API by ID: %s, Path: %s, Method: %s by: %s, IP: %s",
                    requestId, apiId, remainingPath, req.getMethod(), performedBy, clientIp));

            // Extract all request components
            Map<String, Object> queryParams = extractQueryParams(req);
            Map<String, String> headers = extractHeaders(req);
            Map<String, Object> pathParams = extractPathParameters(remainingPath);
            Object body = extractBody(req, jsonBody, formParams);

            // Build execute request
            ExecuteApiRequestDTO executeRequest = ExecuteApiRequestDTO.builder()
                    .pathParams(pathParams)
                    .queryParams(queryParams)
                    .headers(headers)
                    .body(body)
                    .requestId(requestId)
                    .httpMethod(req.getMethod())
                    .build();

            // Execute the API
            ExecuteApiResponseDTO response = apiGenerationEngineService.executeApi(
                    requestId, performedBy, apiId, executeRequest, clientIp, userAgent);

            // Log successful execution
            log.debug("Request ID: {} - API execution completed with status: {}",
                    requestId, response.getResponseCode());

            return ResponseEntity.status(response.getResponseCode()).body(response);

        } catch (Exception e) {
            return handleException(requestId, apiId, e);
        }
    }

    /**
     * Extracts the remaining path after the API ID
     */
    private String extractRemainingPath(HttpServletRequest req, String apiId) {
        String fullPath = req.getRequestURI();
        String contextPath = req.getContextPath();
        String basePattern = "/plx/api/gen/" + apiId;

        // Remove context path if present
        String pathWithoutContext = fullPath;
        if (!contextPath.isEmpty() && fullPath.startsWith(contextPath)) {
            pathWithoutContext = fullPath.substring(contextPath.length());
        }

        // Extract remaining path after base pattern
        int genIndex = pathWithoutContext.indexOf(basePattern);
        if (genIndex >= 0) {
            String remainingPath = pathWithoutContext.substring(genIndex + basePattern.length());
            return remainingPath.isEmpty() ? "/" : remainingPath;
        }

        return "/";
    }

    /**
     * Extracts query parameters from the request
     */
    private Map<String, Object> extractQueryParams(HttpServletRequest req) {
        Map<String, Object> queryParams = new HashMap<>();

        req.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                queryParams.put(key, values.length > 1 ? Arrays.asList(values) : values[0]);
            }
        });

        return queryParams;
    }

    /**
     * Extracts headers from the request (excluding security-sensitive ones)
     */
    private Map<String, String> extractHeaders(HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();

        // Headers to exclude for security reasons
        Set<String> excludedHeaders = Set.of(
                "authorization", "cookie", "set-cookie"
        );

        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            // Skip excluded headers
            if (excludedHeaders.contains(headerName.toLowerCase())) {
                continue;
            }

            headers.put(headerName, req.getHeader(headerName));
        }

        return headers;
    }

    /**
     * Extracts path parameters from the remaining path
     */
    private Map<String, Object> extractPathParameters(String remainingPath) {
        Map<String, Object> pathParams = new HashMap<>();

        if (remainingPath == null || remainingPath.isEmpty() || remainingPath.equals("/")) {
            return pathParams;
        }

        // Remove leading and trailing slashes
        String path = remainingPath;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Split the path into segments
        String[] segments = path.split("/");

        // Store as numbered parameters (param1, param2, etc.)
        for (int i = 0; i < segments.length; i++) {
            if (!segments[i].isEmpty()) {
                pathParams.put("param" + (i + 1), segments[i]);

                // Also add with the actual segment value as key if it's a valid parameter name
                // This helps when you want to access by actual parameter name
                if (isValidParameterName(segments[i])) {
                    pathParams.put(segments[i], segments[i]);
                }
            }
        }

        return pathParams;
    }

    /**
     * Extracts the request body based on content type
     */
    private Object extractBody(HttpServletRequest req, Object jsonBody,
                               MultiValueMap<String, String> formParams) {
        String contentType = req.getContentType();

        if (contentType != null) {
            if (contentType.contains("application/x-www-form-urlencoded")) {
                return extractFormData(formParams);
            } else if (contentType.contains("multipart/form-data")) {
                // Handle multipart form data
                return extractMultipartFormData(formParams);
            } else if (contentType.contains("application/json")) {
                return jsonBody;
            } else if (contentType.contains("text/plain")) {
                // Handle plain text
                return jsonBody != null ? jsonBody.toString() : null;
            }
        }

        return jsonBody;
    }

    /**
     * Extracts form data from form parameters
     */
    private Map<String, Object> extractFormData(MultiValueMap<String, String> formParams) {
        Map<String, Object> formData = new HashMap<>();

        if (formParams != null) {
            formParams.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    formData.put(key, values.size() > 1 ? values : values.get(0));
                }
            });
        }

        return formData;
    }

    /**
     * Extracts multipart form data
     */
    private Map<String, Object> extractMultipartFormData(MultiValueMap<String, String> formParams) {
        // This is a simplified version - you might need to handle file uploads differently
        return extractFormData(formParams);
    }

    /**
     * Extracts the real client IP considering proxy headers
     */
    private String extractClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }

        // If multiple IPs in X-Forwarded-For, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * Checks if a string can be a valid parameter name
     */
    private boolean isValidParameterName(String str) {
        return str != null && str.matches("^[a-zA-Z0-9_\\-]+$");
    }

    /**
     * Handles exceptions and returns appropriate error response
     */
    private ResponseEntity<Map<String, Object>> handleException(String requestId, String apiId, Exception e) {
        // Log the error
        loggerUtil.log("apiGeneration", String.format(
                "Request ID: %s, Error executing API ID: %s, Error: %s",
                requestId, apiId, e.getMessage()));

        log.error("Request ID: {} - Error executing API ID: {}", requestId, apiId, e);

        // Build error response
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("requestId", requestId);
        errorResponse.put("timestamp", new Date().toInstant().toString());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An error occurred while executing API: " + e.getMessage());
        errorResponse.put("path", apiId);

        // Add detailed error info in development mode
        if (log.isDebugEnabled()) {
            errorResponse.put("exception", e.getClass().getName());
            errorResponse.put("stackTrace", Arrays.stream(e.getStackTrace())
                    .limit(5)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * OPTIONS handler for CORS preflight requests
     */
    @RequestMapping(value = "/{apiId}/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions(HttpServletRequest req) {
        return ResponseEntity.ok()
                .header("Allow", "GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS")
                .header("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }
}