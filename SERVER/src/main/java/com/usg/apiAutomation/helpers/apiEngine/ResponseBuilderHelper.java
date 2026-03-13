package com.usg.apiAutomation.helpers.apiEngine;

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
     * Build error response
     */
    public ResponseEntity<Map<String, Object>> buildErrorResponse(
            String requestId,
            String message,
            HttpStatus status) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", status.value());
        errorResponse.put("message", message);
        errorResponse.put("requestId", requestId);

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Build error response with additional details
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
            errorResponse.putAll(additionalDetails);
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
}