package com.usg.apiAutomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.usg.apiAutomation.repositories")
@EntityScan(basePackages = "com.usg.apiAutomation.entities")
public class ApiAutomationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiAutomationApplication.class, args);
    }
}