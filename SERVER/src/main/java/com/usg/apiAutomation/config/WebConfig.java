package com.usg.apiAutomation.config;

import com.usg.apiAutomation.interceptors.ApiKeyNSecretInterceptor;
import com.usg.apiAutomation.interceptors.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
                        "/error",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );
    }

    // REMOVE addCorsMappings() method entirely to avoid conflicts
}