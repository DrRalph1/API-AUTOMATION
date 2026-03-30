package com.usg.apiGeneration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
public class RepositoryConfig {

    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.usg.apiGeneration.repositories.postgres",
            entityManagerFactoryRef = "postgresEntityManagerFactory",
            transactionManagerRef = "postgresTransactionManager"
    )
    public static class PostgresRepositoryConfig {
    }

    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.usg.apiGeneration.repositories.oracle",
            entityManagerFactoryRef = "oracleEntityManagerFactory",
            transactionManagerRef = "oracleTransactionManager"
    )
    public static class OracleRepositoryConfig {
    }
}