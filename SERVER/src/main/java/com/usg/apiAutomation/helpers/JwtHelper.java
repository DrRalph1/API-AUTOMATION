package com.usg.apiAutomation.helpers;

import com.usg.apiAutomation.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtHelper {

    private final JwtUtil jwtUtil;

    public JwtHelper(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Helper method to extract token from Authorization header
    public String extractTokenFromHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    // Helper method to extract userId from token
    public String extractPerformedBy(HttpServletRequest request) {
        String token = extractTokenFromHeader(request);
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    // Helper method to validate Authorization header
    public ResponseEntity<?> validateAuthorizationHeader(HttpServletRequest request, String endpointName) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 401);
            errorResponse.put("message", "Authorization header is required for " + endpointName);
            errorResponse.put("data", Map.of("requiredHeader", "Authorization", "format", "Bearer {token}"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 400);
            errorResponse.put("message", "Invalid Authorization header format");
            errorResponse.put("data", Map.of("currentFormat", authorizationHeader, "expectedFormat", "Bearer {token}"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        String token = authorizationHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 401);
            errorResponse.put("message", "Invalid or expired token");
            errorResponse.put("data", Map.of("tokenStatus", "invalid"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        return null; // No error
    }

}
