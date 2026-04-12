package com.usg.autoAPIGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.usg.autoAPIGenerator.entities.postgres",
        "com.usg.autoAPIGenerator.entities.oracle"
})
@EnableJpaRepositories(basePackages = {
        "com.usg.autoAPIGenerator.repositories"
})
public class AutoAPIGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoAPIGeneratorApplication.class, args);
    }
}