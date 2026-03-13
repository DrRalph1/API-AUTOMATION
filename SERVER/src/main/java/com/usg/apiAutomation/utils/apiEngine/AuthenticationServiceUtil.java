package com.usg.apiAutomation.utils.apiEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class AuthenticationServiceUtil {

    public AuthenticationResult validateAuthentication(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        // If API has no auth configured, always pass
        if (api.getAuthConfig() == null || api.getAuthConfig().getAuthType() == null) {
            log.debug("✅ No authentication configured, allowing access");
            return AuthenticationResult.success();
        }

        String apiAuthType = api.getAuthConfig().getAuthType();

        // Handle "NONE" or "none" as valid no-auth cases
        if ("NONE".equalsIgnoreCase(apiAuthType) || apiAuthType.trim().isEmpty()) {
            log.debug("✅ Authentication type is NONE, allowing access");
            return AuthenticationResult.success();
        }

        log.debug("🔐 Validating authentication type: {}", apiAuthType);

        // Normalize the auth type for comparison
        String normalizedAuthType = apiAuthType.toUpperCase().replace("-", "").replace("_", "");

        // Check for API Key variations
        if (normalizedAuthType.equals("APIKEY") ||
                normalizedAuthType.contains("API") && normalizedAuthType.contains("KEY")) {
            return validateApiKeyFromHeaders(api, request);
        }

        // Use switch on the original type for other cases
        switch (apiAuthType.toUpperCase()) {
            case "API_KEY":
            case "APIKEY":
            case "API-KEY":
                return validateApiKeyFromHeaders(api, request);

            case "BASIC":
                return validateBasicAuth(api, request);

            case "BEARER":
            case "JWT":
                return validateBearerToken(api, request);

            case "OAUTH2":
                return validateOAuth2(api, request);

            case "ORACLE_ROLES":
                return validateOracleSession(api, request);

            default:
                log.warn("❌ Unsupported auth type: {}", apiAuthType);
                return AuthenticationResult.failure("Unsupported auth type: " + apiAuthType);
        }
    }

    private AuthenticationResult validateApiKeyFromHeaders(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            log.debug("❌ API Key validation failed: No headers provided");
            return AuthenticationResult.failure("No headers provided for API Key authentication");
        }

        log.debug("🔍 Validating API Key - Available headers: {}", headers.keySet());

        // Get expected API key header and value from API config
        String expectedApiKeyHeader = api.getAuthConfig().getApiKeyHeader();
        String expectedApiKeyValue = api.getAuthConfig().getApiKeyValue();
        String expectedApiSecret = api.getAuthConfig().getApiKeySecret();

        log.debug("Expected API Key header: '{}', value: '{}'", expectedApiKeyHeader, expectedApiKeyValue);

        // If no API key is configured, authentication passes
        if (expectedApiKeyHeader == null || expectedApiKeyHeader.isEmpty() ||
                expectedApiKeyValue == null || expectedApiKeyValue.isEmpty()) {
            log.debug("⚠️ API Key not fully configured, skipping validation");
            return AuthenticationResult.success();
        }

        // Validate API Key header
        String actualApiKeyValue = null;

        // Try exact match
        if (headers.containsKey(expectedApiKeyHeader)) {
            actualApiKeyValue = headers.get(expectedApiKeyHeader);
            log.debug("✅ Found exact match for header: {}", expectedApiKeyHeader);
        } else {
            // Try case-insensitive match
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(expectedApiKeyHeader)) {
                    actualApiKeyValue = entry.getValue();
                    log.debug("✅ Found case-insensitive match: {} -> {}", entry.getKey(), expectedApiKeyHeader);
                    break;
                }
            }

            // Check for common API key header names
            if (actualApiKeyValue == null) {
                String[] commonHeaders = {"x-api-key", "api-key", "apikey", "x-api-secret"};
                for (String commonHeader : commonHeaders) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(commonHeader)) {
                            actualApiKeyValue = entry.getValue();
                            log.debug("✅ Found common API header: {}", commonHeader);
                            break;
                        }
                    }
                    if (actualApiKeyValue != null) break;
                }
            }
        }

        if (actualApiKeyValue == null) {
            log.debug("❌ Missing required header: {}", expectedApiKeyHeader);
            return AuthenticationResult.failure("Missing required header: " + expectedApiKeyHeader);
        }

        if (!expectedApiKeyValue.equals(actualApiKeyValue)) {
            log.debug("❌ API Key value mismatch for header: {}", expectedApiKeyHeader);
            return AuthenticationResult.failure("Invalid API Key value for header: " + expectedApiKeyHeader);
        }

        // Validate API Secret if configured
        if (expectedApiSecret != null && !expectedApiSecret.isEmpty()) {
            String actualApiSecret = null;

            // Check for common secret header names
            String[] secretHeaders = {"X-API-Secret", "X-API-Secret-Key", "API-Secret", "Secret"};
            for (String secretHeader : secretHeaders) {
                if (headers.containsKey(secretHeader)) {
                    actualApiSecret = headers.get(secretHeader);
                    log.debug("Found exact match for secret header: {}", secretHeader);
                    break;
                }
                // Case-insensitive check
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(secretHeader)) {
                        actualApiSecret = entry.getValue();
                        log.debug("Found case-insensitive secret match: {} -> {}", entry.getKey(), secretHeader);
                        break;
                    }
                }
                if (actualApiSecret != null) break;
            }

            if (actualApiSecret == null) {
                log.debug("❌ Missing API Secret header");
                return AuthenticationResult.failure("Missing API Secret header");
            }

            if (!expectedApiSecret.equals(actualApiSecret)) {
                log.debug("❌ API Secret value mismatch");
                return AuthenticationResult.failure("Invalid API Secret");
            }
        }

        log.debug("✅ API Key validation successful for header: {}", expectedApiKeyHeader);
        return AuthenticationResult.success();
    }

    private AuthenticationResult validateBasicAuth(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for Basic authentication");
        }

        String authHeader = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Authorization")) {
                authHeader = entry.getValue();
                break;
            }
        }

        if (authHeader == null) {
            return AuthenticationResult.failure("Missing Authorization header for Basic authentication");
        }

        if (!authHeader.startsWith("Basic ")) {
            return AuthenticationResult.failure("Authorization header must start with 'Basic '");
        }

        try {
            String base64Credentials = authHeader.substring(6).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);

            if (parts.length != 2) {
                return AuthenticationResult.failure("Invalid Basic authentication format");
            }

            String username = parts[0];
            String password = parts[1];

            String expectedUsername = api.getAuthConfig().getBasicUsername();
            String expectedPassword = api.getAuthConfig().getBasicPassword();

            if (expectedUsername == null || expectedPassword == null) {
                return AuthenticationResult.failure("Basic authentication not configured on API");
            }

            if (!expectedUsername.equals(username)) {
                return AuthenticationResult.failure("Invalid username");
            }

            if (!expectedPassword.equals(password)) {
                return AuthenticationResult.failure("Invalid password");
            }

            log.debug("Basic authentication successful for user: {}", username);
            return AuthenticationResult.success();

        } catch (IllegalArgumentException e) {
            return AuthenticationResult.failure("Invalid Base64 encoding in Authorization header");
        }
    }

    private AuthenticationResult validateBearerToken(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for Bearer authentication");
        }

        String authHeader = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Authorization")) {
                authHeader = entry.getValue();
                break;
            }
        }

        if (authHeader == null) {
            return AuthenticationResult.failure("Missing Authorization header for Bearer authentication");
        }

        if (!authHeader.startsWith("Bearer ")) {
            return AuthenticationResult.failure("Authorization header must start with 'Bearer '");
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            return AuthenticationResult.failure("Empty Bearer token");
        }

        String expectedToken = api.getAuthConfig().getJwtSecret();
        if (expectedToken == null || expectedToken.isEmpty()) {
            return AuthenticationResult.failure("Bearer token not configured on API");
        }

        if (!expectedToken.equals(token)) {
            return AuthenticationResult.failure("Invalid Bearer token");
        }

        log.debug("Bearer token validation successful");
        return AuthenticationResult.success();
    }

    private AuthenticationResult validateOAuth2(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        // Simplified OAuth2 validation
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for OAuth2 authentication");
        }

        String authHeader = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Authorization")) {
                authHeader = entry.getValue();
                break;
            }
        }

        if (authHeader == null) {
            return AuthenticationResult.failure("Missing Authorization header for OAuth2 authentication");
        }

        if (!authHeader.startsWith("Bearer ")) {
            return AuthenticationResult.failure("Authorization header must start with 'Bearer ' for OAuth2");
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            return AuthenticationResult.failure("Empty OAuth2 token");
        }

        String expectedToken = api.getAuthConfig().getOauthClientSecret();
        if (expectedToken != null && !expectedToken.isEmpty() && !expectedToken.equals(token)) {
            return AuthenticationResult.failure("Invalid OAuth2 token");
        }

        log.debug("OAuth2 token validation successful");
        return AuthenticationResult.success();
    }

    private AuthenticationResult validateOracleSession(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for Oracle session authentication");
        }

        String sessionHeader = null;
        String[] sessionHeaders = {"X-Oracle-Session", "Oracle-Session-ID", "Session-ID"};

        for (String headerName : sessionHeaders) {
            if (headers.containsKey(headerName)) {
                sessionHeader = headers.get(headerName);
                break;
            }
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(headerName)) {
                    sessionHeader = entry.getValue();
                    break;
                }
            }
            if (sessionHeader != null) break;
        }

        if (sessionHeader == null) {
            return AuthenticationResult.failure("Missing Oracle session header");
        }

        if (sessionHeader.isEmpty()) {
            return AuthenticationResult.failure("Empty Oracle session ID");
        }

        log.debug("Oracle session validation successful");
        return AuthenticationResult.success();
    }

    public static class AuthenticationResult {
        private final boolean authenticated;
        private final String reason;

        public AuthenticationResult(boolean authenticated, String reason) {
            this.authenticated = authenticated;
            this.reason = reason;
        }

        public boolean isAuthenticated() { return authenticated; }
        public String getReason() { return reason; }

        public static AuthenticationResult success() {
            return new AuthenticationResult(true, null);
        }

        public static AuthenticationResult failure(String reason) {
            return new AuthenticationResult(false, reason);
        }
    }
}