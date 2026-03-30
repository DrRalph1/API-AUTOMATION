package com.usg.apiGeneration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.usg.apiGeneration.entities.postgres",
        "com.usg.apiGeneration.entities.oracle"
})
@EnableJpaRepositories(basePackages = {
        "com.usg.apiGeneration.repositories"
})
public class ApiGenerationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGenerationApplication.class, args);
    }
}