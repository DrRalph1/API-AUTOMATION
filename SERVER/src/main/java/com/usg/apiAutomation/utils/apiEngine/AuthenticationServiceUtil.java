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
        log.debug("========== AUTHENTICATION VALIDATION STARTED ==========");
        log.debug("API ID: {}", api != null ? api.getId() : "null");
        log.debug("AuthConfig present: {}", api.getAuthConfig() != null);

        if (api.getAuthConfig() != null) {
            log.debug("AuthConfig - authType: {}", api.getAuthConfig().getAuthType());
            log.debug("AuthConfig - apiKeyHeader: {}", api.getAuthConfig().getApiKeyHeader());
            log.debug("AuthConfig - apiSecretHeader: {}", api.getAuthConfig().getApiSecretHeader());
            log.debug("AuthConfig - has apiKeyValue: {}", api.getAuthConfig().getApiKeyValue() != null);
            log.debug("AuthConfig - has apiKeySecret: {}", api.getAuthConfig().getApiKeySecret() != null);
        }

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

        // Check request headers
        if (request != null && request.getHeaders() != null) {
            log.debug("Request headers present: {}", request.getHeaders().keySet());
            // Log API key and secret headers specifically if they exist
            if (request.getHeaders().containsKey("x-api-key")) {
                log.debug("x-api-key header found with value: {}", maskSensitiveValue(request.getHeaders().get("x-api-key")));
            }
            if (request.getHeaders().containsKey("x-api-secret")) {
                log.debug("x-api-secret header found with value: {}", maskSensitiveValue(request.getHeaders().get("x-api-secret")));
            }
        } else {
            log.debug("No headers in request");
        }

        // Normalize the auth type for comparison
        String normalizedAuthType = apiAuthType.toUpperCase().replace("-", "").replace("_", "");

        // Check for API Key variations
        if (normalizedAuthType.equals("APIKEY") ||
                (normalizedAuthType.contains("API") && normalizedAuthType.contains("KEY"))) {
            log.debug("Matched API Key auth type, calling validateApiKeyNSecret");
            return validateApiKeyNSecret(api, request);
        }

        // Use switch on the original type for other cases
        switch (apiAuthType.toUpperCase()) {
            case "API_KEY":
            case "APIKEY":
            case "API-KEY":
                log.debug("Matched API_KEY case, calling validateApiKeyNSecret");
                return validateApiKeyNSecret(api, request);

            case "BASIC":
                log.debug("Matched BASIC auth");
                return validateBasicAuth(api, request);

            case "BEARER":
            case "JWT":
                log.debug("Matched BEARER/JWT auth");
                return validateBearerToken(api, request);

            case "OAUTH2":
                log.debug("Matched OAUTH2 auth");
                return validateOAuth2(api, request);

            case "ORACLE_ROLES":
                log.debug("Matched ORACLE_ROLES auth");
                return validateOracleSession(api, request);

            default:
                log.warn("❌ Unsupported auth type: {}", apiAuthType);
                return AuthenticationResult.failure("Unsupported auth type: " + apiAuthType);
        }
    }

    private AuthenticationResult validateApiKeyNSecret(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        log.debug("===== VALIDATE API KEY & SECRET STARTED =====");

        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            log.debug("❌ API Key & Secret validation failed: No headers provided");
            return AuthenticationResult.failure("No headers provided for API Key authentication");
        }

        log.debug("🔍 Validating API Key & Secret - Available headers: {}", headers.keySet());

        // Get expected API key header and value from API config
        String expectedApiKeyHeader = api.getAuthConfig().getApiKeyHeader();
        String expectedApiKeyValue = api.getAuthConfig().getApiKeyValue();

        // FIX: Use getApiSecretValue() instead of getApiKeySecret()
        String expectedApiSecret = api.getAuthConfig().getApiSecretValue();
        String expectedApiSecretHeader = api.getAuthConfig().getApiSecretHeader();

        log.debug("Expected API Key header: '{}', value: '{}'",
                expectedApiKeyHeader, maskSensitiveValue(expectedApiKeyValue));
        log.debug("Expected API Secret header: '{}', value: '{}'",
                expectedApiSecretHeader, maskSensitiveValue(expectedApiSecret));

        // DEBUG: Check if expected values are also placeholders
        if (expectedApiKeyValue != null && isPlaceholder(expectedApiKeyValue)) {
            log.warn("⚠️ WARNING: Expected API Key value in database is a placeholder: {}", maskSensitiveValue(expectedApiKeyValue));
        }
        if (expectedApiSecret != null && isPlaceholder(expectedApiSecret)) {
            log.warn("⚠️ WARNING: Expected API Secret value in database is a placeholder: {}", maskSensitiveValue(expectedApiSecret));
        }

        // If no API credentials are configured, authentication passes
        boolean hasKeyConfig = expectedApiKeyHeader != null && !expectedApiKeyHeader.isEmpty() &&
                expectedApiKeyValue != null && !expectedApiKeyValue.isEmpty();

        // FIX: Check apiSecretValue instead of apiKeySecret
        boolean hasSecretConfig = expectedApiSecretHeader != null && !expectedApiSecretHeader.isEmpty() &&
                expectedApiSecret != null && !expectedApiSecret.isEmpty();

        log.debug("hasKeyConfig: {}, hasSecretConfig: {}", hasKeyConfig, hasSecretConfig);

        // If no credentials configured, skip validation
        if (!hasKeyConfig && !hasSecretConfig) {
            log.debug("⚠️ No API Key & Secret configured, skipping validation");
            return AuthenticationResult.success();
        }

        // If only one is configured, validation should FAIL
        if (hasKeyConfig != hasSecretConfig) {
            log.error("❌ Incomplete API Key & Secret configuration - Both key and secret are required");
            return AuthenticationResult.failure("Invalid API Key or Secret");
        }

        // Find API Key header value
        String actualApiKeyValue = findHeaderValue(headers, expectedApiKeyHeader, "API Key");
        log.debug("Actual API Key value found: {}", maskSensitiveValue(actualApiKeyValue));

        if (actualApiKeyValue == null) {
            log.debug("❌ Missing required API Key header: {}", expectedApiKeyHeader);
            return AuthenticationResult.failure("Invalid API Key or Secret");
        }

        // REJECT PLACEHOLDER VALUES
        if (isPlaceholder(actualApiKeyValue)) {
            log.debug("❌ API Key contains placeholder value: {}", maskSensitiveValue(actualApiKeyValue));
            return AuthenticationResult.failure("Invalid API Key or Secret");
        }
        log.debug("Found API Key value: {}", maskSensitiveValue(actualApiKeyValue));

        // Find API Secret header value
        String actualApiSecretValue = findHeaderValue(headers, expectedApiSecretHeader, "API Secret");
        log.debug("Actual API Secret value found: {}", maskSensitiveValue(actualApiSecretValue));

        if (actualApiSecretValue == null) {
            log.debug("❌ Missing required API Secret header: {}", expectedApiSecretHeader);
            return AuthenticationResult.failure("Invalid API Key or Secret");
        }

        // REJECT PLACEHOLDER VALUES
        if (isPlaceholder(actualApiSecretValue)) {
            log.debug("❌ API Secret contains placeholder value: {}", maskSensitiveValue(actualApiSecretValue));
            return AuthenticationResult.failure("Invalid API Key or Secret");
        }
        log.debug("Found API Secret value: {}", maskSensitiveValue(actualApiSecretValue));

        // TEMPORARY DEBUG: Show unmasked values for troubleshooting
        log.debug("=== UNMASKED VALUES FOR TROUBLESHOOTING ===");
        log.debug("Comparing API Key - Expected: '{}', Actual: '{}'", expectedApiKeyValue, actualApiKeyValue);
        log.debug("Comparing API Secret - Expected: '{}', Actual: '{}'", expectedApiSecret, actualApiSecretValue);

        // Validate both values match
        String cleanedActualValue = cleanupHeaderValue(actualApiKeyValue);
        if (!expectedApiKeyValue.equals(cleanedActualValue)) {
            log.debug("❌ API Key value mismatch");
            return AuthenticationResult.failure("Invalid API Key or Secret");
        }

        if (!expectedApiSecret.equals(actualApiSecretValue)) {
            log.debug("❌ API Secret value mismatch");
            return AuthenticationResult.failure("Invalid API Key or Secret");
        }

        log.debug("✅ API Key & Secret validation successful - Key header: {}, Secret header: {}",
                expectedApiKeyHeader, expectedApiSecretHeader);
        log.debug("===== VALIDATE API KEY & SECRET COMPLETED SUCCESSFULLY =====");
        return AuthenticationResult.success();
    }

    /**
     * Check if a value is a placeholder like {{api_key}}
     */
    private boolean isPlaceholder(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        // Check for pattern like {{anything}}, ${anything}, or {anything}
        return trimmed.matches("\\{\\{[^}]+\\}\\}") ||  // {{api_key}}
                trimmed.matches("\\$\\{[^}]+\\}") ||     // ${api_key}
                trimmed.matches("\\{[^}]+\\}");          // {api_key}
    }

    private String cleanupHeaderValue(String value) {
        if (value == null) return null;
        // If the value contains a comma, take the first part
        if (value.contains(",")) {
            String firstPart = value.split(",")[0].trim();
            log.debug("Cleaned up header value from '{}' to '{}'", value, firstPart);
            return firstPart;
        }
        return value;
    }

    private String findHeaderValue(Map<String, String> headers, String expectedHeader, String headerType) {
        // Try exact match
        if (headers.containsKey(expectedHeader)) {
            log.debug("✅ Found exact match for {} header: {}", headerType, expectedHeader);
            return headers.get(expectedHeader);
        }

        // Try case-insensitive match
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(expectedHeader)) {
                log.debug("✅ Found case-insensitive match for {} header: {} -> {}",
                        headerType, entry.getKey(), expectedHeader);
                return entry.getValue();
            }
        }

        // Check for common API key header names based on type
        String[] commonHeaders;
        if (headerType.contains("Key")) {
            commonHeaders = new String[]{"x-api-key", "api-key", "apikey", "X-API-Key"};
        } else {
            commonHeaders = new String[]{"x-api-secret", "api-secret", "apisecret", "X-API-Secret"};
        }

        for (String commonHeader : commonHeaders) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(commonHeader)) {
                    log.debug("✅ Found common {} header: {}", headerType, commonHeader);
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private String maskSensitiveValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private AuthenticationResult validateBasicAuth(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return AuthenticationResult.failure("No headers provided for Basic authentication");
        }

        String authHeader = extractAuthorizationHeader(headers);

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

        String authHeader = extractAuthorizationHeader(headers);

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

        String authHeader = extractAuthorizationHeader(headers);

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

        String sessionHeader = extractOracleSessionHeader(headers);

        if (sessionHeader == null) {
            return AuthenticationResult.failure("Missing Oracle session header");
        }

        if (sessionHeader.isEmpty()) {
            return AuthenticationResult.failure("Empty Oracle session ID");
        }

        log.debug("Oracle session validation successful");
        return AuthenticationResult.success();
    }

    private String extractAuthorizationHeader(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("Authorization")) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String extractOracleSessionHeader(Map<String, String> headers) {
        String[] sessionHeaders = {"X-Oracle-Session", "Oracle-Session-ID", "Session-ID"};

        for (String headerName : sessionHeaders) {
            // Try exact match
            if (headers.containsKey(headerName)) {
                return headers.get(headerName);
            }
            // Try case-insensitive match
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(headerName)) {
                    return entry.getValue();
                }
            }
        }
        return null;
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