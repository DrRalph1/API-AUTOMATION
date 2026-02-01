package com.usg.apiAutomation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Remove servers/schemas
                .servers(Collections.emptyList())

                // Add API information
                .info(new Info()
                        .title("Dynamic API Automation")
                        .version("1.0")
                        .description("API documentation for dynamically generated endpoints from Oracle database objects. The aim is to do away with the tideous process involved in API development."));
    }
}