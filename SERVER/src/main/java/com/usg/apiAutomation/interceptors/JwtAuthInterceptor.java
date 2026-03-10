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

        // ============== CRITICAL FIX: Allow all OPTIONS requests ==============
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Key, X-API-Secret, X-Requested-With");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            return true; // Skip authentication for preflight requests
        }

        String path = request.getRequestURI();

        // Bypass public / exempt paths
        if (path != null && (path.contains("/plx/api/users/login")
                || path.contains("swagger-ui")
                || path.contains("/v3/api-docs")
                || path.contains("/swagger"))) {
            return true;
        }

        // Allow login endpoint without JWT
        if ("/user-login".equals(path) || "/user-login/".equals(path)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            // Missing token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"responseCode\":401,\"message\":\"Missing or invalid Authorization header\"}");
            loggerUtil.log("api-automation", "Unauthorized: Missing or invalid Authorization header for path: " + path);
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"responseCode\":401,\"message\":\"Invalid or expired token\"}");
            loggerUtil.log("api-automation", "Unauthorized: Invalid/expired token for path: " + path);
            return false;
        }

        // Optionally set authenticated userId as request attribute
        String userId = jwtUtil.extractUserId(token);
        request.setAttribute("authenticatedUserId", userId);

        return true;
    }
}