package com.usg.apiAutomation.services.apiGenerationEngine;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ExecuteApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiAuthConfigEntity;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiValidatorService {

    private final Map<String, Map<String, RateLimitInfo>> rateLimitCounters = new ConcurrentHashMap<>();

    private static class RateLimitInfo {
        private final AtomicLong counter;
        private LocalDateTime windowStart;

        public RateLimitInfo() {
            this.counter = new AtomicLong(0);
            this.windowStart = LocalDateTime.now();
        }

        public AtomicLong getCounter() {
            return counter;
        }

        public LocalDateTime getWindowStart() {
            return windowStart;
        }

        public void setWindowStart(LocalDateTime windowStart) {
            this.windowStart = windowStart;
        }
    }

    /**
     * Validate authentication based on API configuration
     */
    public boolean validateAuthentication(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        if (api.getAuthConfig() == null || api.getAuthConfig().getAuthType() == null) {
            return true; // No auth required
        }

        String authType = api.getAuthConfig().getAuthType();

        switch (authType) {
            case "NONE":
                return true;

            case "API_KEY":
                return validateApiKey(api.getAuthConfig(), request);

            case "BASIC":
                return validateBasicAuth(api.getAuthConfig(), request);

            case "JWT":
            case "BEARER":
                return validateJwt(api.getAuthConfig(), request);

            case "OAUTH2":
                return validateOAuth2(api.getAuthConfig(), request);

            case "ORACLE_ROLES":
                return validateOracleRoles(api.getAuthConfig(), request);

            default:
                log.warn("Unknown auth type: {}", authType);
                return false;
        }
    }

    /**
     * Validate API Key authentication
     */
    private boolean validateApiKey(ApiAuthConfigEntity auth, ExecuteApiRequestDTO request) {
        if (auth == null) return false;

        String headerName = auth.getApiKeyHeader() != null ? auth.getApiKeyHeader() : "X-API-Key";
        String expectedKey = auth.getApiKeyValue();

        if (expectedKey == null || expectedKey.isEmpty()) {
            log.warn("API Key value not configured");
            return false;
        }

        String providedKey = null;
        if (request.getHeaders() != null) {
            providedKey = request.getHeaders().get(headerName);
        }

        // Check if key matches
        if (!expectedKey.equals(providedKey)) {
            log.warn("API Key mismatch: expected={}, provided={}", expectedKey, providedKey);
            return false;
        }

        // Check secret if configured
        if (auth.getApiKeySecret() != null && !auth.getApiKeySecret().isEmpty()) {
            String secretHeader = auth.getApiKeyLocation() != null && "header".equals(auth.getApiKeyLocation()) ?
                    "X-API-Secret" : headerName + "-Secret";
            String providedSecret = request.getHeaders() != null ?
                    request.getHeaders().get(secretHeader) : null;

            if (!auth.getApiKeySecret().equals(providedSecret)) {
                log.warn("API Secret mismatch");
                return false;
            }
        }

        return true;
    }

    /**
     * Validate Basic Authentication
     */
    private boolean validateBasicAuth(ApiAuthConfigEntity auth, ExecuteApiRequestDTO request) {
        if (auth == null || auth.getBasicUsername() == null || auth.getBasicPassword() == null) {
            log.warn("Basic auth credentials not configured");
            return false;
        }

        String authHeader = request.getHeaders() != null ?
                request.getHeaders().get("Authorization") : null;

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("Missing or invalid Basic auth header");
            return false;
        }

        try {
            String base64Credentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);

            if (parts.length != 2) {
                return false;
            }

            String username = parts[0];
            String password = parts[1];

            return auth.getBasicUsername().equals(username) &&
                    auth.getBasicPassword().equals(password);

        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Basic auth header: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate JWT token
     */
    private boolean validateJwt(ApiAuthConfigEntity auth, ExecuteApiRequestDTO request) {
        if (auth == null || auth.getJwtSecret() == null) {
            log.warn("JWT secret not configured");
            return false;
        }

        String authHeader = request.getHeaders() != null ?
                request.getHeaders().get("Authorization") : null;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Bearer token header");
            return false;
        }

        String token = authHeader.substring("Bearer ".length());

        // In a real implementation, you would:
        // 1. Validate JWT signature using the secret
        // 2. Check expiration (exp claim)
        // 3. Validate issuer (iss) if configured
        // 4. Validate audience (aud) if configured

        // This is a simplified validation - in production, use a proper JWT library
        try {
            // Simple signature validation (placeholder - use JWT library in production)
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return false;
            }

            // Validate expiration if present (simplified)
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
            if (payload.contains("exp")) {
                // Parse expiration and check if token is expired
                // This would require a proper JWT library
            }

            // Validate issuer if configured
            if (auth.getJwtIssuer() != null && !auth.getJwtIssuer().isEmpty()) {
                if (!payload.contains("\"iss\":\"" + auth.getJwtIssuer() + "\"")) {
                    return false;
                }
            }

            // Validate audience if configured
            if (auth.getJwtAudience() != null && !auth.getJwtAudience().isEmpty()) {
                if (!payload.contains("\"aud\":\"" + auth.getJwtAudience() + "\"")) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate OAuth2 token (simplified)
     */
    private boolean validateOAuth2(ApiAuthConfigEntity auth, ExecuteApiRequestDTO request) {
        if (auth == null || auth.getOauthClientId() == null) {
            log.warn("OAuth2 client ID not configured");
            return false;
        }

        String authHeader = request.getHeaders() != null ?
                request.getHeaders().get("Authorization") : null;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Bearer token header for OAuth2");
            return false;
        }

        // In a real implementation, you would:
        // 1. Validate token with OAuth2 provider (introspection endpoint)
        // 2. Check scopes if configured
        // 3. Validate client ID

        // This is a simplified validation
        String token = authHeader.substring("Bearer ".length());

        // Check if token is not empty
        if (token.isEmpty()) {
            return false;
        }

        // Validate scopes if configured
        if (auth.getOauthScopes() != null && !auth.getOauthScopes().isEmpty()) {
            // In real implementation, check if token has required scopes
            // For now, just return true
        }

        return true;
    }

    /**
     * Validate Oracle roles (simplified)
     */
    private boolean validateOracleRoles(ApiAuthConfigEntity auth, ExecuteApiRequestDTO request) {
        if (auth == null || auth.getRequiredRoles() == null || auth.getRequiredRoles().isEmpty()) {
            return true; // No roles required
        }

        // In a real implementation, you would:
        // 1. Get the database session/user from the request context
        // 2. Check if the user has the required Oracle roles
        // 3. Validate object privileges if configured

        // This would typically involve calling a PL/SQL function or querying
        // Oracle data dictionary views like DBA_ROLE_PRIVS, SESSION_ROLES, etc.

        String username = extractUsernameFromRequest(request);
        if (username == null) {
            log.warn("No username found for Oracle roles validation");
            return false;
        }

        // Simplified validation - in production, query Oracle for role membership
        // For example:
        // SELECT COUNT(*) FROM SESSION_ROLES WHERE ROLE IN (:roles)

        log.info("Validating Oracle roles for user: {}, required roles: {}",
                username, auth.getRequiredRoles());

        // If custom auth function is specified, call it
        if (auth.getCustomAuthFunction() != null && !auth.getCustomAuthFunction().isEmpty()) {
            // In real implementation, call the custom PL/SQL function
            // e.g., "BEGIN :result := " + auth.getCustomAuthFunction() + "(:username); END;"
            return callCustomAuthFunction(auth, username);
        }

        // Placeholder - in production, this should actually validate against Oracle
        return true;
    }

    /**
     * Validate authorization (IP whitelist, etc.)
     */
    public boolean validateAuthorization(GeneratedApiEntity api, String performedBy) {
        // Check if API is active
        if (!Boolean.TRUE.equals(api.getIsActive())) {
            log.warn("API is not active: {}", api.getId());
            return false;
        }

        // Check IP whitelist if configured
        if (api.getAuthConfig() != null && api.getAuthConfig().getIpWhitelist() != null) {
            String ipWhitelist = api.getAuthConfig().getIpWhitelist();
            // In real implementation, check if client IP is in whitelist
            // This would need the client IP passed to this method
        }

        // Check owner/creator restrictions if needed
        if (api.getOwner() != null && !api.getOwner().isEmpty()) {
            // In real implementation, check if user has access to this API
        }

        return true;
    }

    /**
     * Check rate limiting
     */
    public boolean checkRateLimit(GeneratedApiEntity api, String clientIp) {
        if (api.getAuthConfig() == null ||
                api.getAuthConfig().getRateLimitRequests() == null ||
                api.getAuthConfig().getRateLimitRequests() <= 0) {
            return true; // Rate limiting not enabled
        }

        String key = api.getId() + ":" + clientIp;
        Map<String, RateLimitInfo> apiCounters = rateLimitCounters.computeIfAbsent(api.getId(),
                k -> new ConcurrentHashMap<>());

        RateLimitInfo info = apiCounters.computeIfAbsent(clientIp, k -> new RateLimitInfo());

        // Check if we need to reset the window
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = info.getWindowStart();
        String period = api.getAuthConfig().getRateLimitPeriod() != null ?
                api.getAuthConfig().getRateLimitPeriod() : "minute";

        boolean shouldReset = false;

        switch (period) {
            case "second":
                shouldReset = windowStart.plusSeconds(1).isBefore(now);
                break;
            case "minute":
                shouldReset = windowStart.plusMinutes(1).isBefore(now);
                break;
            case "hour":
                shouldReset = windowStart.plusHours(1).isBefore(now);
                break;
            case "day":
                shouldReset = windowStart.plusDays(1).isBefore(now);
                break;
            default:
                shouldReset = windowStart.plusMinutes(1).isBefore(now);
        }

        if (shouldReset) {
            info.getCounter().set(0);
            info.setWindowStart(now);
        }

        long currentCount = info.getCounter().incrementAndGet();
        int limit = api.getAuthConfig().getRateLimitRequests();

        if (currentCount > limit) {
            log.warn("Rate limit exceeded for API {} from IP {}. Current: {}, Limit: {}",
                    api.getId(), clientIp, currentCount, limit);
            return false;
        }

        return true;
    }

    /**
     * Validate request parameters
     */
    public Map<String, String> validateParameters(GeneratedApiEntity api, ExecuteApiRequestDTO request) {
        Map<String, String> errors = new HashMap<>();

        if (api.getParameters() != null) {
            for (var param : api.getParameters()) {
                String paramValue = null;

                // Get parameter value based on location
                if ("path".equals(param.getParameterType())) {
                    if (request.getPathParams() != null) {
                        Object value = request.getPathParams().get(param.getKey());
                        paramValue = value != null ? value.toString() : null;
                    }
                } else if ("query".equals(param.getParameterType())) {
                    if (request.getQueryParams() != null) {
                        Object value = request.getQueryParams().get(param.getKey());
                        paramValue = value != null ? value.toString() : null;
                    }
                } else if ("header".equals(param.getParameterType())) {
                    if (request.getHeaders() != null) {
                        paramValue = request.getHeaders().get(param.getKey());
                    }
                } else if ("body".equals(param.getParameterType())) {
                    // Body parameters are handled separately
                    continue;
                }

                // Check required parameters
                if (Boolean.TRUE.equals(param.getRequired())) {
                    if (paramValue == null || paramValue.trim().isEmpty()) {
                        errors.put(param.getKey(), "Required parameter '" + param.getKey() + "' is missing");
                    }
                }

                // Validate pattern if specified and value exists
                if (paramValue != null && !paramValue.isEmpty() &&
                        param.getValidationPattern() != null && !param.getValidationPattern().isEmpty()) {
                    if (!paramValue.matches(param.getValidationPattern())) {
                        errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                "' does not match required pattern: " + param.getValidationPattern());
                    }
                }

                // Validate type if possible
                if (paramValue != null && !paramValue.isEmpty() && param.getApiType() != null) {
                    switch (param.getApiType()) {
                        case "integer":
                            try {
                                Integer.parseInt(paramValue);
                            } catch (NumberFormatException e) {
                                errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                        "' must be an integer");
                            }
                            break;
                        case "number":
                            try {
                                Double.parseDouble(paramValue);
                            } catch (NumberFormatException e) {
                                errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                        "' must be a number");
                            }
                            break;
                        case "boolean":
                            if (!"true".equalsIgnoreCase(paramValue) &&
                                    !"false".equalsIgnoreCase(paramValue)) {
                                errors.put(param.getKey(), "Parameter '" + param.getKey() +
                                        "' must be a boolean (true/false)");
                            }
                            break;
                    }
                }
            }
        }

        // Validate request body if configured
        if (api.getRequestConfig() != null &&
                Boolean.TRUE.equals(api.getRequestConfig().getValidateSchema()) &&
                request.getBody() != null) {

            // In real implementation, validate JSON schema
            if (api.getRequestConfig().getRequiredFields() != null) {
                // Check required fields in body
            }

            // Check max size
            if (api.getRequestConfig().getMaxSize() != null) {
                try {
                    String bodyJson = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(request.getBody());
                    if (bodyJson.length() > api.getRequestConfig().getMaxSize()) {
                        errors.put("body", "Request body exceeds maximum size of " +
                                api.getRequestConfig().getMaxSize() + " bytes");
                    }
                } catch (Exception e) {
                    // Ignore size check if can't serialize
                }
            }
        }

        return errors;
    }

    /**
     * Extract username from request for Oracle roles validation
     */
    private String extractUsernameFromRequest(ExecuteApiRequestDTO request) {
        // Try to get from headers first
        if (request.getHeaders() != null) {
            String username = request.getHeaders().get("X-User-Name");
            if (username != null && !username.isEmpty()) {
                return username;
            }
        }

        // Try to get from JWT token if present
        if (request.getHeaders() != null) {
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring("Bearer ".length());
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        if (payload.contains("sub")) {
                            // Parse subject from JWT (simplified)
                            int subIndex = payload.indexOf("\"sub\"");
                            if (subIndex > 0) {
                                // Extract username - in production use proper JWT library
                                return "user_from_jwt";
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not extract username from JWT: {}", e.getMessage());
                }
            }
        }

        return null;
    }

    /**
     * Call custom PL/SQL authentication function
     */
    private boolean callCustomAuthFunction(ApiAuthConfigEntity auth, String username) {
        // In real implementation, this would call a PL/SQL function
        // For example:
        // String sql = "BEGIN :result := " + auth.getCustomAuthFunction() + "(:username); END;";
        // CallableStatement cs = connection.prepareCall(sql);
        // cs.registerOutParameter("result", Types.BOOLEAN);
        // cs.setString("username", username);
        // cs.execute();
        // return cs.getBoolean("result");

        log.info("Would call custom auth function: {} for user: {}",
                auth.getCustomAuthFunction(), username);

        // Placeholder - in production, return actual result from database
        return true;
    }

    /**
     * Hash a value (for secure comparison)
     */
    private String hashValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hashing algorithm not available: {}", e.getMessage());
            return value;
        }
    }
}