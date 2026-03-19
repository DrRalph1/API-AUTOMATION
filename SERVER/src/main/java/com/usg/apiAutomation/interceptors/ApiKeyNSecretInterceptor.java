package com.usg.apiAutomation.interceptors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.helpers.ApiKeyNSecretHelper;
import com.usg.apiAutomation.helpers.ClientIpHelper;
import com.usg.apiAutomation.helpers.DataMaskingHelper;
import com.usg.apiAutomation.helpers.ErrorHandlingHelper;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.usg.apiAutomation.wrappers.CachedBodyHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.util.*;

@Component
public class ApiKeyNSecretInterceptor implements HandlerInterceptor {

    @Autowired
    private ApiKeyNSecretHelper apiKeyNSecretHelper;

    @Autowired
    private ClientIpHelper clientIpHelper;

    @Autowired
    private LoggerUtil loggerUtil;

    @Autowired
    private ErrorHandlingHelper errorHandlingHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // ============== CRITICAL FIX: Allow all OPTIONS requests for CORS preflight ==============
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("[DEBUG] OPTIONS request detected - allowing without authentication");

            // Set CORS headers for preflight response
            String origin = request.getHeader("Origin");
            if (origin != null && (origin.contains("10.113.214.76:9874") ||
                    origin.contains("localhost:9874") ||
                    origin.contains("127.0.0.1:9874"))) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
                response.setHeader("Access-Control-Allow-Headers",
                        "Content-Type, Authorization, X-API-Key, X-API-Secret, X-Requested-With, Origin, Accept, " +
                                "Access-Control-Request-Method, Access-Control-Request-Headers");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Vary", "Origin");
            }

            response.setStatus(HttpServletResponse.SC_OK);
            return false; // Return false to stop further processing for OPTIONS requests
        }

        System.out.println("[DEBUG] ApiKeyNSecretInterceptor triggered for path: " + request.getRequestURI() + ", method: " + request.getMethod());

        // Wrap request to cache body for multiple reads
        CachedBodyHttpServletRequest cachedRequest = (request instanceof CachedBodyHttpServletRequest)
                ? (CachedBodyHttpServletRequest) request
                : new CachedBodyHttpServletRequest(request);

        // Extract API Key and Secret from headers
        String apiKey = cachedRequest.getHeader("x-api-key");
        String apiSecret = cachedRequest.getHeader("x-api-secret");

        // Build headers map for logging
        Map<String, Object> headersMap = new HashMap<>();
        Enumeration<String> headerNames = cachedRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = cachedRequest.getHeaders(headerName);
            headersMap.put(headerName, Collections.list(headerValues));
        }

        // Generate request ID and extract request details
        String requestId = UUID.randomUUID().toString();
        String method = cachedRequest.getMethod();
        String path = cachedRequest.getRequestURI();
        String rawRequestBody = cachedRequest.getCachedBodyAsString();
        String contentType = cachedRequest.getContentType();
        String activityType = pathToActivityType(path);

        // Parse request body based on content type
        Map<String, Object> requestBodyMap;

        // Check for query parameters first
        Map<String, String[]> parameterMap = cachedRequest.getParameterMap();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            Map<String, Object> paramMap = new HashMap<>();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue().length == 1) {
                    paramMap.put(entry.getKey(), entry.getValue()[0]);
                } else {
                    paramMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
            }
            requestBodyMap = paramMap;
        }
        // Parse body content if no query parameters
        else if (rawRequestBody != null && !rawRequestBody.isEmpty()) {
            try {
                if (contentType != null && contentType.contains("application/json")) {
                    String trimmedBody = rawRequestBody.trim();
                    if (trimmedBody.startsWith("[")) {
                        // Handle JSON array
                        List<Map<String, Object>> listBody = objectMapper.readValue(rawRequestBody,
                                new TypeReference<List<Map<String, Object>>>() {});
                        if (listBody.size() == 1) {
                            requestBodyMap = listBody.get(0);
                        } else {
                            requestBodyMap = new HashMap<>();
                            requestBodyMap.put("body", listBody);
                        }
                    } else {
                        // Handle JSON object
                        requestBodyMap = objectMapper.readValue(rawRequestBody,
                                new TypeReference<Map<String, Object>>() {});
                    }
                } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                    // Parse form data
                    Map<String, Object> formMap = new HashMap<>();
                    String[] pairs = rawRequestBody.split("&");
                    for (String pair : pairs) {
                        int idx = pair.indexOf("=");
                        if (idx > 0 && pair.length() > idx + 1) {
                            String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                            String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                            formMap.put(key, value);
                        }
                    }
                    requestBodyMap = formMap;
                } else {
                    // Handle other content types
                    requestBodyMap = new HashMap<>();
                    requestBodyMap.put("rawBody", rawRequestBody);
                    requestBodyMap.put("contentType", contentType);
                }
            } catch (Exception e) {
                loggerUtil.log("api-automation", "Failed to parse request body: " + e.getMessage());
                requestBodyMap = new HashMap<>();
                requestBodyMap.put("rawBody", rawRequestBody);
                requestBodyMap.put("parseError", e.getMessage());
            }
        } else {
            requestBodyMap = Collections.emptyMap();
        }

        // Mask sensitive data for logging
        String maskedApiKey = DataMaskingHelper.maskSensitiveData(apiKey);
        String maskedApiSecret = DataMaskingHelper.maskSensitiveData(apiSecret);

        loggerUtil.log("api-automation", "Incoming Request: x-api-key=" + maskedApiKey
                + ", x-api-secret=" + maskedApiSecret + ", path=" + path + ", method=" + method);

        // Validate API Key presence
        if (apiKey == null || apiKey.isEmpty()) {
            return handleForbidden(cachedRequest, response, requestId, method, path, headersMap, requestBodyMap, 403,
                    "Access Forbidden. Missing required header: x-api-key.", activityType);
        }

        // Validate API Secret presence
        if (apiSecret == null || apiSecret.isEmpty()) {
            return handleForbidden(cachedRequest, response, requestId, method, path, headersMap, requestBodyMap, 403,
                    "Access Forbidden. Missing required header: x-api-secret.", activityType);
        }

        // ============== UNCOMMENT THIS SECTION FOR PRODUCTION ==============
        // Validate API Key and Secret credentials
        /*
        if (!apiKeyNSecretHelper.validateCredentials(apiKey, apiSecret)) {
            return handleForbidden(
                    cachedRequest,
                    response,
                    requestId,
                    method,
                    path,
                    headersMap,
                    requestBodyMap,
                    403,
                    "Access Forbidden. Invalid API Key or Secret.",
                    activityType
            );
        }

        // Validate client IP if required
        if (!clientIpHelper.validateClientIp(apiKey, apiSecret, cachedRequest.getRemoteAddr())) {
            return handleForbidden(
                    cachedRequest,
                    response,
                    requestId,
                    method,
                    path,
                    headersMap,
                    requestBodyMap,
                    403,
                    "Access Forbidden. Invalid Client IP.",
                    activityType
            );
        }
        */
        // ============== END OF PRODUCTION SECTION ==============

        // Store validated credentials in request for later use
        request.setAttribute("authenticatedApiKey", apiKey);
        request.setAttribute("authenticatedApiSecret", apiSecret);
        request.setAttribute("requestId", requestId);

        return true;
    }

    /**
     * Handle forbidden access by logging and sending error response
     */
    private boolean handleForbidden(
            CachedBodyHttpServletRequest cachedRequest,
            HttpServletResponse response,
            String requestId,
            String method,
            String path,
            Map<String, Object> headersMap,
            Map<String, Object> requestBodyMap,
            int statusCode,
            String responseMessage,
            String activityType) throws Exception {

        loggerUtil.log("api-automation", "Access Forbidden: " + responseMessage + " for path: " + path);

        // Log to error handling helper if needed
        if (activityType == null ||
                !(activityType.toLowerCase().contains("plx sha") ||
                        activityType.toLowerCase().contains("swagger") ||
                        activityType.toLowerCase().contains("favicon.ico"))) {

            // Build resource URL for logging
            String resourceUrl = cachedRequest.getScheme() + "://" +
                    cachedRequest.getServerName() + ":" +
                    cachedRequest.getServerPort() + path;

            // You can add additional logging here if needed
            loggerUtil.log("api-automation", "Forbidden access attempt to: " + resourceUrl);
        }

        sendErrorResponse(response, statusCode, responseMessage);
        return false;
    }

    /**
     * Send JSON error response
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws Exception {
        response.setStatus(statusCode);
        response.setContentType("application/json");

        // Create structured error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", statusCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", new Date().toString());
        errorResponse.put("error", "Forbidden");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Convert path to activity type for logging
     */
    private String pathToActivityType(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        // Remove leading/trailing slashes
        String trimmedPath = path.replaceAll("^/+", "").replaceAll("/+$", "");

        // Split by slashes and hyphens
        String[] parts = trimmedPath.split("[/-]");

        // Build activity type with proper capitalization
        StringBuilder activityTypeBuilder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (activityTypeBuilder.length() > 0) {
                    activityTypeBuilder.append(" ");
                }
                // Capitalize first letter, lower case rest
                activityTypeBuilder.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase());
            }
        }

        return activityTypeBuilder.toString();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clean up if needed
        if (ex != null) {
            loggerUtil.log("api-automation", "Request completed with error: " + ex.getMessage());
        }
    }
}