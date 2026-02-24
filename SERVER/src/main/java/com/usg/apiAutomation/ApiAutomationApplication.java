package com.usg.apiAutomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.usg.apiAutomation.entities.postgres",
        "com.usg.apiAutomation.entities.oracle"
})
public class ApiAutomationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiAutomationApplication.class, args);
    }
}