package com.usg.apiAutomation.controllers.systemActivities;

import com.usg.apiAutomation.dtos.systemActivities.jwt.CheckTokenExpiryRequestDTO;
import com.usg.apiAutomation.dtos.systemActivities.jwt.RefreshTokenRequestDTO;
import com.usg.apiAutomation.helpers.ApiKeyNSecretHelper;
import com.usg.apiAutomation.helpers.ClientIpHelper;
import com.usg.apiAutomation.services.systemActivities.JWTService;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.usg.apiAutomation.helpers.AuditLogHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Profile({"SLCB", "RCB", "UTB"})
@RestController
@RequiredArgsConstructor
//@Tag(name = "JWT Operations", description = "JSON Web Token Management Operations")
@Tag(name = "SYSTEM ACTIVITIES", description = "System-level endpoints")
@RequestMapping("/plx/api")
public class JWTController {

    @Schema(description = "Helper for API key and secret validation")
    private final ApiKeyNSecretHelper apiKeyNSecretHelper;

    @Schema(description = "Helper for client IP validation")
    private final ClientIpHelper clientIpHelper;

    @Schema(description = "Utility for logging operations")
    private final LoggerUtil loggerUtil;

    @Schema(description = "Service for JWT token operations")
    private final JWTService jwtService;

    @Schema(description = "Helper for creating audit logs")
    private final AuditLogHelper auditLogHelper;

    @Operation(
            summary = "Refresh Access Token",
            description = "Refresh access token using a valid refresh token. Returns new access token and new refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid token format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden due to invalid API credentials or IP",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @Parameter(description = "API Key for authentication", required = true, example = "your-api-key-here")
            @RequestHeader(value = "x-api-key", required = true)
            String apiKey,

            @Parameter(description = "API Secret for authentication", required = true, example = "your-api-secret-here")
            @RequestHeader(value = "x-api-secret", required = true)
            String apiSecret,

            @Valid @RequestBody
            RefreshTokenRequestDTO refreshTokenRequestDTO,

            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate API Credentials
        boolean validAPICredentials = apiKeyNSecretHelper.validateApiCredentials(apiKey, apiSecret);
        boolean validClientIP = clientIpHelper.validateAPIClientIp(apiKey, apiSecret);

        if (!validAPICredentials) {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Access Forbidden. Invalid API Key or Secret.");
            auditLogHelper.logAuditAction("REFRESH_TOKEN_AUTH_FAILED", apiKey,
                    "Access Forbidden. Invalid API Key or Secret.", requestId);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "responseCode", 403,
                            "message", "Access Forbidden. Invalid API Key or Secret."
                    ));
        }

        if (!validClientIP) {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Access Forbidden. Unknown Client IP.");
            auditLogHelper.logAuditAction("REFRESH_TOKEN_CLIENT_IP_FAILED", apiKey,
                    "Access Forbidden. Unknown Client IP.", requestId);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "responseCode", 403,
                            "message", "Access Forbidden. Unknown Client IP."
                    ));
        }

        try {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Refresh token request received");
            auditLogHelper.logAuditAction("REFRESH_TOKEN_REQUEST", apiKey,
                    "Refresh token request received", requestId);

            // Delegate to service method which already handles response structure
            ResponseEntity<?> serviceResponse = jwtService.refreshToken(refreshTokenRequestDTO.getRefreshToken(), requestId, req);

            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Refresh token processed. Status: " + serviceResponse.getStatusCode());
            auditLogHelper.logAuditAction("REFRESH_TOKEN_COMPLETED", apiKey,
                    "Refresh token processed. Status: " + serviceResponse.getStatusCode(), requestId);

            return serviceResponse;
        } catch (Exception e) {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Refresh token error: " + e.getMessage());
            auditLogHelper.logAuditAction("REFRESH_TOKEN_ERROR", apiKey,
                    "Error refreshing token: " + e.getMessage(), requestId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "responseCode", 500,
                            "message", "Error refreshing token: " + e.getMessage()
                    ));
        }
    }

    @Operation(
            summary = "Check Token Expiry Status",
            description = "Check if a JWT token has expired and get its expiry status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token expiry status checked successfully",
                    content = @Content(schema = @Schema(implementation = TokenExpiryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid token format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden due to invalid API credentials or IP",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/check-token-expiry")
    public ResponseEntity<?> checkTokenExpiry(
            @Parameter(description = "API Key for authentication", required = true, example = "your-api-key-here")
            @RequestHeader(value = "x-api-key", required = true)
            String apiKey,

            @Parameter(description = "API Secret for authentication", required = true, example = "your-api-secret-here")
            @RequestHeader(value = "x-api-secret", required = true)
            String apiSecret,

            @Valid @RequestBody
            CheckTokenExpiryRequestDTO checkTokenExpiryRequestDTO,

            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate API Credentials
        boolean validAPICredentials = apiKeyNSecretHelper.validateApiCredentials(apiKey, apiSecret);
        boolean validClientIP = clientIpHelper.validateAPIClientIp(apiKey, apiSecret);

        if (!validAPICredentials) {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Access Forbidden. Invalid API Key or Secret.");
            auditLogHelper.logAuditAction("CHECK_TOKEN_EXPIRY_AUTH_FAILED", apiKey,
                    "Access Forbidden. Invalid API Key or Secret.", requestId);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "responseCode", 403,
                            "message", "Access Forbidden. Invalid API Key or Secret."
                    ));
        }

        if (!validClientIP) {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Access Forbidden. Unknown Client IP.");
            auditLogHelper.logAuditAction("CHECK_TOKEN_EXPIRY_CLIENT_IP_FAILED", apiKey,
                    "Access Forbidden. Unknown Client IP.", requestId);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "responseCode", 403,
                            "message", "Access Forbidden. Unknown Client IP."
                    ));
        }

        try {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Check token expiry request received");
            auditLogHelper.logAuditAction("CHECK_TOKEN_EXPIRY_REQUEST", apiKey,
                    "Check token expiry request received", requestId);

            // Delegate to service method which already handles response structure
            ResponseEntity<?> serviceResponse = jwtService.checkTokenExpiry(checkTokenExpiryRequestDTO.getToken(), requestId, req);

            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Check token expiry processed. Status: " + serviceResponse.getStatusCode());
            auditLogHelper.logAuditAction("CHECK_TOKEN_EXPIRY_COMPLETED", apiKey,
                    "Check token expiry processed. Status: " + serviceResponse.getStatusCode(), requestId);

            return serviceResponse;
        } catch (Exception e) {
            loggerUtil.log("api-gateway", "Request ID: " + requestId + " - Check token expiry error: " + e.getMessage());
            auditLogHelper.logAuditAction("CHECK_TOKEN_EXPIRY_ERROR", apiKey,
                    "Error checking token expiry: " + e.getMessage(), requestId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "responseCode", 500,
                            "message", "Error checking token expiry: " + e.getMessage()
                    ));
        }
    }

    // Swagger Schema Definitions
    @Schema(name = "ErrorResponse", description = "Error response structure")
    static class ErrorResponse {
        @Schema(description = "Response code", example = "403")
        private int responseCode;

        @Schema(description = "Error message", example = "Access Forbidden. Invalid API Key or Secret.")
        private String message;

        // Getters and setters
        public int getResponseCode() { return responseCode; }
        public void setResponseCode(int responseCode) { this.responseCode = responseCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @Schema(name = "TokenRefreshResponse", description = "Token refresh response structure")
    static class TokenRefreshResponse {
        @Schema(description = "Response code", example = "200")
        private int responseCode;

        @Schema(description = "Response message", example = "Token refreshed successfully")
        private String message;

        @Schema(description = "Token data")
        private TokenData data;

        // Getters and setters
        public int getResponseCode() { return responseCode; }
        public void setResponseCode(int responseCode) { this.responseCode = responseCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public TokenData getData() { return data; }
        public void setData(TokenData data) { this.data = data; }

        @Schema(name = "TokenData", description = "Token information")
        static class TokenData {
            @Schema(description = "Access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            private String token;

            @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            private String refresh_token;

            @Schema(description = "Token expiration timestamp in milliseconds", example = "1678901234567")
            private long token_expires_in;

            @Schema(description = "Refresh token expiration timestamp in milliseconds", example = "1678901234567")
            private long refresh_token_expires_in;

            // Getters and setters
            public String getToken() { return token; }
            public void setToken(String token) { this.token = token; }
            public String getRefresh_token() { return refresh_token; }
            public void setRefresh_token(String refresh_token) { this.refresh_token = refresh_token; }
            public long getToken_expires_in() { return token_expires_in; }
            public void setToken_expires_in(long token_expires_in) { this.token_expires_in = token_expires_in; }
            public long getRefresh_token_expires_in() { return refresh_token_expires_in; }
            public void setRefresh_token_expires_in(long refresh_token_expires_in) { this.refresh_token_expires_in = refresh_token_expires_in; }
        }
    }

    @Schema(name = "TokenExpiryResponse", description = "Token expiry check response structure")
    static class TokenExpiryResponse {
        @Schema(description = "Response code", example = "200")
        private int responseCode;

        @Schema(description = "Response message", example = "Token expiry check completed successfully")
        private String message;

        @Schema(description = "Token expiry data")
        private TokenExpiryData data;

        // Getters and setters
        public int getResponseCode() { return responseCode; }
        public void setResponseCode(int responseCode) { this.responseCode = responseCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public TokenExpiryData getData() { return data; }
        public void setData(TokenExpiryData data) { this.data = data; }

        @Schema(name = "TokenExpiryData", description = "Token expiry information")
        static class TokenExpiryData {
            @Schema(description = "Whether the token is expired", example = "false")
            private boolean is_expired;

            @Schema(description = "User ID from the token", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            private String user_id;

            @Schema(description = "Expiration timestamp", example = "2023-12-31T23:59:59.000Z")
            private String expires_at;

            @Schema(description = "Current timestamp", example = "2023-12-31T23:50:00.000Z")
            private String current_time;

            @Schema(description = "Time until expiration in milliseconds", example = "599000")
            private long time_until_expiration_ms;

            @Schema(description = "Time until expiration in seconds", example = "599")
            private long time_until_expiration_seconds;

            // Getters and setters
            public boolean isIs_expired() { return is_expired; }
            public void setIs_expired(boolean is_expired) { this.is_expired = is_expired; }
            public String getUser_id() { return user_id; }
            public void setUser_id(String user_id) { this.user_id = user_id; }
            public String getExpires_at() { return expires_at; }
            public void setExpires_at(String expires_at) { this.expires_at = expires_at; }
            public String getCurrent_time() { return current_time; }
            public void setCurrent_time(String current_time) { this.current_time = current_time; }
            public long getTime_until_expiration_ms() { return time_until_expiration_ms; }
            public void setTime_until_expiration_ms(long time_until_expiration_ms) { this.time_until_expiration_ms = time_until_expiration_ms; }
            public long getTime_until_expiration_seconds() { return time_until_expiration_seconds; }
            public void setTime_until_expiration_seconds(long time_until_expiration_seconds) { this.time_until_expiration_seconds = time_until_expiration_seconds; }
        }
    }
}