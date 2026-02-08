package com.usg.apiAutomation.services.system;

import com.usg.apiAutomation.dtos.user.UserDTO;
import com.usg.apiAutomation.repositories.AppUserRepository;
import com.usg.apiAutomation.utils.JwtUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JWTService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private LoggerUtil loggerUtil;


    @Transactional
    public ResponseEntity<?> refreshToken(String refreshToken, String requestId, HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Use basic validation that works with current tokens
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            // Check if token is expired
            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw new RuntimeException("Refresh token has expired");
            }

            // Extract user ID from refresh token
            String userId = jwtUtil.extractUserId(refreshToken);

            // Verify user exists and is active
            Optional<UserDTO> userOptional = appUserRepository.getUserByUserId(userId);
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            UserDTO user = userOptional.get();

            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new RuntimeException("Your account has been deactivated. Please contact support");
            }

            // Generate NEW access token and NEW refresh token
            String newAccessToken = jwtUtil.generateToken(userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("token", newAccessToken);
            tokenData.put("refresh_token", newRefreshToken);
            tokenData.put("token_expires_in", jwtUtil.extractExpiration(newAccessToken).getTime());
            tokenData.put("refresh_token_expires_in", jwtUtil.extractExpiration(newRefreshToken).getTime());

            result.put("status", "success");
            result.put("message", "Token refreshed successfully");
            result.put("code", 200);
            result.put("data", tokenData);

            // Log the token refresh activity
            loggerUtil.log("api-gateway",
                    String.format("Token refreshed for user %s. Request ID: %s", userId, requestId));

        } catch (Exception e) {
            loggerUtil.log("api-gateway",
                    String.format("Refresh token failed - Error: %s, Request ID: %s", e.getMessage(), requestId));

            result.put("status", "failed");
            result.put("message", e.getMessage() != null ? e.getMessage() : "Unknown error");
            result.put("code", 401);
        }

        // Build response
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("responseCode", result.get("code"));
        responseBody.put("message", result.get("message"));
        if (result.get("data") != null) {
            responseBody.put("data", result.get("data"));
        }

        return ResponseEntity.status((int) result.get("code")).body(responseBody);
    }


    @Transactional
    public ResponseEntity<?> checkTokenExpiry(String token, String requestId, HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Validate token format first
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Token is required");
            }

            // Check if token is expired using the isTokenExpired method
            boolean isExpired = jwtUtil.isTokenExpired(token);

            // Additional validation to ensure token is properly parsable
            try {
                String userId = jwtUtil.extractUserId(token);
                Date expiration = jwtUtil.extractExpiration(token);

                Map<String, Object> tokenData = new HashMap<>();
                tokenData.put("is_expired", isExpired);
                tokenData.put("user_id", userId);
                tokenData.put("expires_at", expiration);
                tokenData.put("current_time", new Date());

                // Calculate time until expiration in milliseconds
                long timeUntilExpiration = expiration.getTime() - new Date().getTime();
                tokenData.put("time_until_expiration_ms", timeUntilExpiration);
                tokenData.put("time_until_expiration_seconds", timeUntilExpiration / 1000);

                result.put("status", "success");
                result.put("message", "Token expiry check completed successfully");
                result.put("code", 200);
                result.put("data", tokenData);

            } catch (Exception e) {
                // Token is not parsable - consider it invalid/expired
                Map<String, Object> tokenData = new HashMap<>();
                tokenData.put("is_expired", true);
                tokenData.put("error", "Token is invalid or malformed");

                result.put("status", "success");
                result.put("message", "Token is invalid");
                result.put("code", 400);
                result.put("data", tokenData);
            }

        } catch (Exception e) {
            loggerUtil.log("api-gateway", "Check token expiry error: " + e.getMessage());

            result.put("status", "failed");
            result.put("message", e.getMessage() != null ? e.getMessage() : "Unknown error");
            result.put("code", 400);
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("responseCode", result.get("code"));
        responseBody.put("message", result.get("message"));
        if (result.get("data") != null) {
            responseBody.put("data", result.get("data"));
        }

        return ResponseEntity.status((int) result.get("code")).body(responseBody);
    }


}
