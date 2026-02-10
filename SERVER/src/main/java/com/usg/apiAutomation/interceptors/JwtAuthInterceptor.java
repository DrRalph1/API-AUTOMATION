package com.usg.apiAutomation.interceptors;

import com.usg.apiAutomation.utils.JwtUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoggerUtil loggerUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String path = request.getRequestURI();

        // Bypass public / exempt paths (same pattern as ApiKeyNSecretInterceptor)
        if (path != null && (path.contains("/plx/api/") || path.equals("/plx/api") || path.startsWith("/plx/api")
                || path.contains("swagger-ui") || path.startsWith("/v3/plx/api/-docs") || path.startsWith("/swagger"))) {
            return true;
        }

        // Allow login tpartyAPI without JWT
        if ("/userManagement-login".equals(path) || "/userManagement-login/".equals(path)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            // Missing token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"responseCode\":401,\"message\":\"Missing or invalid Authorization header\"}");
            loggerUtil.log("api-gateway", "Unauthorized: Missing or invalid Authorization header for path: " + path);
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"responseCode\":401,\"message\":\"Invalid or expired token\"}");
            loggerUtil.log("api-gateway", "Unauthorized: Invalid/expired token for path: " + path);
            return false;
        }

        // Optionally set authenticated userId as request attribute
        String userId = jwtUtil.extractUserId(token);
        request.setAttribute("authenticatedUserId", userId);

        return true;
    }
}
