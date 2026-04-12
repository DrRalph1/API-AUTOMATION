package com.usg.autoAPIGenerator.helpers.apiEngine;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ResponseBuilderHelper {

    /**
     * Build success response
     */
    public ResponseEntity<Map<String, Object>> buildSuccessResponse(
            String requestId,
            String message,
            Object data) {

        Map<String, Object> response = new HashMap<>();
        response.put("responseCode", 200);
        response.put("message", message);
        response.put("data", data);
        response.put("requestId", requestId);

        return ResponseEntity.ok(response);
    }

    /**
     * Build success response with custom status code
     */
    public ResponseEntity<Map<String, Object>> buildSuccessResponse(
            String requestId,
            String message,
            Object data,
            HttpStatus status) {

        Map<String, Object> response = new HashMap<>();
        response.put("responseCode", status.value());
        response.put("message", message);
        response.put("data", data);
        response.put("requestId", requestId);

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Build error response - MODIFIED to include data when present
     */
    public ResponseEntity<Map<String, Object>> buildErrorResponse(
            String requestId,
            String message,
            HttpStatus status) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", status.value());
        errorResponse.put("message", message);
        errorResponse.put("requestId", requestId);
        // No data field here - this is fine for simple errors

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Build error response with additional details - MODIFIED to preserve data
     */
    public ResponseEntity<Map<String, Object>> buildErrorResponse(
            String requestId,
            String message,
            HttpStatus status,
            Map<String, Object> additionalDetails) {

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("responseCode", status.value());
        errorResponse.put("message", message);
        errorResponse.put("requestId", requestId);

        if (additionalDetails != null) {
            // Check if additionalDetails contains a 'data' field with debug information
            if (additionalDetails.containsKey("data")) {
                // Preserve the data field exactly as is
                errorResponse.put("data", additionalDetails.get("data"));
                // Also copy any other important fields like 'success', 'debug_data', etc.
                additionalDetails.forEach((key, value) -> {
                    if (!"data".equals(key) && !"responseCode".equals(key) &&
                            !"message".equals(key) && !"requestId".equals(key)) {
                        errorResponse.put(key, value);
                    }
                });
            } else {
                // If no data field, just add all additional details
                errorResponse.putAll(additionalDetails);
            }
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Build validation error response
     */
    public ResponseEntity<Map<String, Object>> buildValidationErrorResponse(
            String requestId,
            String validationErrors) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", 400);
        errorResponse.put("message", "Validation errors: " + validationErrors);
        errorResponse.put("requestId", requestId);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * NEW: Build error response that preserves the original detailed response
     * Use this for batch operations that return detailed error information
     */
    public ResponseEntity<Map<String, Object>> buildDetailedErrorResponse(
            String requestId,
            Object detailedResponse,
            HttpStatus status) {

        if (detailedResponse instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) detailedResponse;

            // Ensure required fields are present
            if (!responseMap.containsKey("requestId")) {
                responseMap.put("requestId", requestId);
            }

            // The responseCode and message should already be in the map
            return ResponseEntity.status(status).body(responseMap);
        }

        // Fallback to standard error response
        return buildErrorResponse(requestId, "Error processing request", status);
    }
}