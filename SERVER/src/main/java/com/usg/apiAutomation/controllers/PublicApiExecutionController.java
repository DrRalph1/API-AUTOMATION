package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiResponseDTO;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.APIGenerationEngineService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/gen") // This makes it available at /gen directly
@RequiredArgsConstructor
@Tag(name = "API EXECUTION ENGINE", description = "Public endpoints for executing generated APIs")
public class PublicApiExecutionController {

    private final APIGenerationEngineService apiGenerationEngineService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    @GetMapping("/{apiId}/**")
    @PostMapping("/{apiId}/**")
    @PutMapping("/{apiId}/**")
    @DeleteMapping("/{apiId}/**")
    @PatchMapping("/{apiId}/**")
    @Operation(summary = "Execute API by ID", description = "Execute a generated API using its ID in the URL path")
    public ResponseEntity<?> executeApiById(
            @PathVariable String apiId,
            HttpServletRequest req,
            @RequestBody(required = false) Object jsonBody,
            @RequestParam(required = false) MultiValueMap<String, String> formParams) {

        System.out.println("adey here now..");

        System.out.println("Public API Execution Controller - Request received for API ID: " + apiId);
        System.out.println("Full URL: " + req.getRequestURL().toString());
        System.out.println("Method: " + req.getMethod());

        String requestId = UUID.randomUUID().toString();

        try {
            // Extract the remaining path after /gen/{apiId}
            String fullPath = req.getRequestURI();
            String genPattern = "/gen/" + apiId;
            int genIndex = fullPath.indexOf(genPattern);

            String remainingPath = "";
            if (genIndex >= 0) {
                remainingPath = fullPath.substring(genIndex + genPattern.length());
                if (!remainingPath.isEmpty() && !remainingPath.startsWith("/")) {
                    remainingPath = "/" + remainingPath;
                }
            }

            String performedBy = jwtHelper.extractPerformedBy(req); // This will be "anonymous" if no token
            String clientIp = req.getRemoteAddr();
            String userAgent = req.getHeader("User-Agent");

            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Executing API by ID: " + apiId +
                    ", Path: " + remainingPath +
                    ", Method: " + req.getMethod() +
                    " by: " + performedBy);

            // Extract query parameters
            Map<String, Object> queryParams = new HashMap<>();
            req.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    queryParams.put(key, values.length > 1 ? values : values[0]);
                }
            });

            // Extract headers
            Map<String, String> headers = new HashMap<>();
            java.util.Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, req.getHeader(headerName));
            }

            // Extract path parameters
            Map<String, Object> pathParams = extractPathParameters(remainingPath);

            // Determine the body based on content type
            Object body;
            String contentType = req.getContentType();

            if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                // For form data, convert to Map
                Map<String, Object> formData = new HashMap<>();
                if (formParams != null) {
                    formParams.forEach((key, values) -> {
                        if (values != null && !values.isEmpty()) {
                            formData.put(key, values.size() > 1 ? values : values.get(0));
                        }
                    });
                }
                body = formData;
            } else {
                // For JSON, use the jsonBody parameter
                body = jsonBody;
            }

            // Build execute request
            ExecuteApiRequestDTO executeRequest = ExecuteApiRequestDTO.builder()
                    .pathParams(pathParams)
                    .queryParams(queryParams)
                    .headers(headers)
                    .body(body)
                    .requestId(requestId)
                    .build();

            // Execute the API
            ExecuteApiResponseDTO response = apiGenerationEngineService.executeApi(
                    requestId, performedBy, apiId, executeRequest, clientIp, userAgent);

            return ResponseEntity.status(response.getStatusCode()).body(response);

        } catch (Exception e) {
            loggerUtil.log("apiGeneration", "Request ID: " + requestId +
                    ", Error executing API by ID: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while executing API: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Helper method to extract path parameters from the remaining path
     */
    private Map<String, Object> extractPathParameters(String remainingPath) {
        Map<String, Object> pathParams = new HashMap<>();

        if (remainingPath == null || remainingPath.isEmpty() || remainingPath.equals("/")) {
            return pathParams;
        }

        // Split the path into segments
        String[] segments = remainingPath.split("/");

        // Store as numbered parameters (param1, param2, etc.)
        for (int i = 0; i < segments.length; i++) {
            if (!segments[i].isEmpty()) {
                pathParams.put("param" + (i + 1), segments[i]);
            }
        }

        return pathParams;
    }
}