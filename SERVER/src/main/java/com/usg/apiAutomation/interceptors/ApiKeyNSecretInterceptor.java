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
        System.out.println("[DEBUG] Interceptor triggered for path: " + request.getRequestURI());
        CachedBodyHttpServletRequest cachedRequest = (request instanceof CachedBodyHttpServletRequest)
                ? (CachedBodyHttpServletRequest) request
                : new CachedBodyHttpServletRequest(request);

        String apiKey = cachedRequest.getHeader("x-api-key");
        String apiSecret = cachedRequest.getHeader("x-api-secret");

        Map<String, Object> headersMap = new HashMap<>();
        Enumeration<String> headerNames = cachedRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = cachedRequest.getHeaders(headerName);
            headersMap.put(headerName, Collections.list(headerValues));
        }

        String requestId = UUID.randomUUID().toString();
        String method = cachedRequest.getMethod();
        String path = cachedRequest.getRequestURI();
        String rawRequestBody = cachedRequest.getCachedBodyAsString();
        String contentType = cachedRequest.getContentType();
        String activityType = pathToActivityType(path);

        Map<String, Object> requestBodyMap;

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
        } else if (rawRequestBody != null && !rawRequestBody.isEmpty()) {
            try {
                if (contentType != null && contentType.contains("application/json")) {
                    String trimmedBody = rawRequestBody.trim();
                    if (trimmedBody.startsWith("[")) {
                        // Use TypeReference to avoid unchecked warning
                        List<Map<String, Object>> listBody = objectMapper.readValue(rawRequestBody,
                                new TypeReference<List<Map<String, Object>>>() {});
                        if (listBody.size() == 1) {
                            requestBodyMap = listBody.get(0);
                        } else {
                            requestBodyMap = new HashMap<>();
                            requestBodyMap.put("body", listBody);
                        }
                    } else {
                        // Use TypeReference to avoid unchecked warning
                        requestBodyMap = objectMapper.readValue(rawRequestBody,
                                new TypeReference<Map<String, Object>>() {});
                    }
                } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
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
                    requestBodyMap = new HashMap<>();
                    requestBodyMap.put("rawBody", rawRequestBody);
                    requestBodyMap.put("contentType", contentType);
                }
            } catch (Exception e) {
                loggerUtil.log("api-automation", "Failed to parse requestEntity body: " + e.getMessage());
                requestBodyMap = new HashMap<>();
                requestBodyMap.put("rawBody", rawRequestBody);
            }
        } else {
            requestBodyMap = Collections.emptyMap();
        }

        String maskedApiKey = DataMaskingHelper.maskSensitiveData(apiKey);
        String maskedApiSecret = DataMaskingHelper.maskSensitiveData(apiSecret);

        loggerUtil.log("api-automation", "Incoming RequestEntity: x-api-key=" + maskedApiKey
                + ", x-api-secret=" + maskedApiSecret);

        if (apiKey == null || apiKey.isEmpty()) {
            return handleForbidden(cachedRequest, response, requestId, method, path, headersMap, requestBodyMap, 403,
                    "Access Forbidden. Missing required header: x-api-key.", activityType);
        }

        if (apiSecret == null || apiSecret.isEmpty()) {
            return handleForbidden(cachedRequest, response, requestId, method, path, headersMap, requestBodyMap, 403,
                    "Access Forbidden. Missing required header: x-api-secret.", activityType);
        }

        // -----------------------------
        // Enforce validation ONLY for swagger + JSON endpoints
        // -----------------------------
//        if (path.contains("swagger-ui") ||
//                path.matches("^/plx/api//v1\\.0/Json.*") ||
//                path.startsWith("/plx/api/v1.0/Json")) {
//            if (!apiKeyNSecretHelper.validateSwaggerCredentials(apiKey, apiSecret)) {
//                return handleForbidden(
//                        cachedRequest,
//                        response,
//                        requestId,
//                        method,
//                        path,
//                        headersMap,
//                        requestBodyMap,
//                        403,
//                        "Access Forbidden. Invalid API Key or Secret.",
//                        activityType
//                );
//            }
//
//            if (!clientIpHelper.validateSwaggerClientIp(apiKey, apiSecret)) {
//                return handleForbidden(
//                        cachedRequest,
//                        response,
//                        requestId,
//                        method,
//                        path,
//                        headersMap,
//                        requestBodyMap,
//                        403,
//                        "Access Forbidden. Invalid Client IP.",
//                        activityType
//                );
//            }
//        }

        return true;
    }

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

        loggerUtil.log("api-automation", responseMessage);

        // Skip audit log if activityType contains any of these substrings
        if (activityType == null ||
                !(activityType.toLowerCase().contains("plx sha") ||
                        activityType.toLowerCase().contains("swagger") ||
                        activityType.toLowerCase().contains("favicon.ico"))) {


            // CORRECTED: Use single resourceUrl parameter instead of two URL parameters
            String resourceUrl = cachedRequest.getScheme() + "://" + cachedRequest.getServerName() + ":" + cachedRequest.getServerPort() + path;

        }

        sendErrorResponse(response, statusCode, responseMessage);
        return false;
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws Exception {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        String jsonResponse = String.format("{\"responseCode\": %d, \"message\": \"%s\"}", statusCode, message);
        response.getWriter().write(jsonResponse);
    }

    private String pathToActivityType(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String trimmedPath = path.replaceAll("^/+", "").replaceAll("/+$", "");
        String[] parts = trimmedPath.split("[/-]");
        StringBuilder activityTypeBuilder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                activityTypeBuilder
                        .append(activityTypeBuilder.length() > 0 ? " " : "")
                        .append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase());
            }
        }
        return activityTypeBuilder.toString();
    }
}