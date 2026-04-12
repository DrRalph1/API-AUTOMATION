package com.usg.autoAPIGenerator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
public class RepositoryConfig {

    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.usg.autoAPIGenerator.repositories.postgres",
            entityManagerFactoryRef = "postgresEntityManagerFactory",
            transactionManagerRef = "postgresTransactionManager"
    )
    public static class PostgresRepositoryConfig {
    }

    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.usg.autoAPIGenerator.repositories.oracle",
            entityManagerFactoryRef = "oracleEntityManagerFactory",
            transactionManagerRef = "oracleTransactionManager"
    )
    public static class OracleRepositoryConfig {
    }
}