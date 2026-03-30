package com.usg.apiGeneration.config;

import com.usg.apiGeneration.interceptors.ApiKeyNSecretInterceptor;
import com.usg.apiGeneration.interceptors.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ApiKeyNSecretInterceptor apiKeyNSecretInterceptor;

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // API key / secret interceptor
        registry.addInterceptor(apiKeyNSecretInterceptor)
                .addPathPatterns("/plx/api/users/login")
                .excludePathPatterns(
                        "/login",
                        "/error",
                        "/plx/api/gen/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );

        // JWT auth interceptor
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/plx/api/**")
                .excludePathPatterns(
                        "/plx/api/users/login",  // Exclude login from JWT check
                        "/login",
                        "/plx/api/gen/**",
                        "/error",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/plx/api/users/login",           // Login endpoint
                        "/plx/api/users/forgot-password", // Forgot password
                        "/plx/api/users/password-reset",  // Password reset
                        "/plx/api/users/{userId}",        // Public user lookup for forgot password
                        "/plx/api/health/**",              // Health checks
                        "/plx/api/public/**"
                );
    }


    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // In Spring 6+, the recommended approach is to use the default PathPatternParser
        // and configure it through properties or accept the defaults

        // If you need custom path matching, you can still set a custom parser
        // but the API has changed
        PathPatternParser parser = new PathPatternParser();
        // Note: setMatchOptionalTrailingSeparator is removed/deprecated
        // The default behavior now handles trailing slashes appropriately
        configurer.setPatternParser(parser);
    }
}