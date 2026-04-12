package com.usg.autoAPIGenerator.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    // ==================== POSTGRESQL DATASOURCE (PRIMARY) ====================

    @Primary
    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource(
            @Value("${spring.datasource.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.hikari.maximum-pool-size}") int maximumPoolSize,
            @Value("${spring.datasource.hikari.minimum-idle}") int minimumIdle,
            @Value("${spring.datasource.hikari.connection-timeout}") int connectionTimeout,
            @Value("${spring.datasource.hikari.idle-timeout}") long idleTimeout,
            @Value("${spring.datasource.hikari.max-lifetime}") long maxLifetime,
            @Value("${spring.datasource.hikari.leak-detection-threshold}") long leakDetectionThreshold,
            @Value("${spring.datasource.hikari.validation-timeout}") long validationTimeout,
            @Value("${spring.datasource.hikari.connection-test-query}") String connectionTestQuery) {

        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        config.setValidationTimeout(validationTimeout);
        config.setConnectionTestQuery(connectionTestQuery);
        config.setPoolName("PostgreSQL-Hikari-Pool");
        config.setAutoCommit(true);

        // Add PostgreSQL specific optimizations
        config.addDataSourceProperty("prepareThreshold", "3");
        config.addDataSourceProperty("preparedStatementCacheQueries", "256");
        config.addDataSourceProperty("preparedStatementCacheSizeMiB", "50");
        config.addDataSourceProperty("defaultRowFetchSize", "500");
        config.addDataSourceProperty("binaryTransfer", "true");
        config.addDataSourceProperty("reWriteBatchedInserts", "true");

        return new HikariDataSource(config);
    }

    @SuppressWarnings("deprecation")
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");

        return new EntityManagerFactoryBuilder(vendorAdapter, new HashMap<>(), null);
    }

    // For DatabaseTypeServiceFactory compatibility - REMOVE @Primary
    @Bean(name = "postgresqlDataSource")
    public DataSource postgresqlDataSource(@Qualifier("postgresDataSource") DataSource dataSource) {
        return dataSource;
    }

    @Primary
    @Bean(name = "postgreSQLJdbcTemplate")
    public JdbcTemplate postgreSQLJdbcTemplate(@Qualifier("postgresDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "postgresqlJdbcTemplate")
    public JdbcTemplate postgresqlJdbcTemplate(@Qualifier("postgresDataSource") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(500);
        jdbcTemplate.setQueryTimeout(30);
        jdbcTemplate.setMaxRows(10000);
        jdbcTemplate.setResultsMapCaseInsensitive(true);
        return jdbcTemplate;
    }

    @Primary
    @Bean(name = "postgresEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("postgresDataSource") DataSource dataSource,
            @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${spring.jpa.hibernate.ddl-auto:update}") String ddlAuto,
            @Value("${spring.jpa.show-sql:true}") String showSql,
            @Value("${spring.jpa.properties.hibernate.format_sql:true}") String formatSql) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        properties.put("hibernate.jdbc.batch_size", "100");
        properties.put("hibernate.jdbc.fetch_size", "500");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");

        return builder
                .dataSource(dataSource)
                .packages("com.usg.autoAPIGenerator.entities.postgres")
                .persistenceUnit("postgres")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "postgresTransactionManager")
    public PlatformTransactionManager postgresTransactionManager(
            @Qualifier("postgresEntityManagerFactory") LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory) {
        return new JpaTransactionManager(postgresEntityManagerFactory.getObject());
    }

    // ==================== DEFAULT BEANS FOR COMPATIBILITY ====================

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("postgresEntityManagerFactory") LocalContainerEntityManagerFactoryBean postgresEmf) {
        return postgresEmf;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("postgresTransactionManager") PlatformTransactionManager postgresTm) {
        return postgresTm;
    }

    // ==================== ORACLE DATASOURCE (SECONDARY) ====================
    // REMOVE @Primary from Oracle - it's secondary

    @Bean(name = "oracleDataSource")
    @Lazy
    public DataSource oracleDataSource(
            @Value("${spring.datasource.oracle.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.oracle.url}") String url,
            @Value("${spring.datasource.oracle.username}") String username,
            @Value("${spring.datasource.oracle.password}") String password,
            @Value("${spring.datasource.oracle.hikari.maximum-pool-size}") int maximumPoolSize,
            @Value("${spring.datasource.oracle.hikari.minimum-idle}") int minimumIdle,
            @Value("${spring.datasource.oracle.hikari.connection-timeout}") int connectionTimeout,
            @Value("${spring.datasource.oracle.hikari.idle-timeout}") long idleTimeout,
            @Value("${spring.datasource.oracle.hikari.max-lifetime}") long maxLifetime,
            @Value("${spring.datasource.oracle.hikari.leak-detection-threshold}") long leakDetectionThreshold,
            @Value("${spring.datasource.oracle.hikari.validation-timeout}") long validationTimeout,
            @Value("${spring.datasource.oracle.hikari.connection-test-query}") String connectionTestQuery) {

        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        config.setValidationTimeout(validationTimeout);
        config.setConnectionTestQuery(connectionTestQuery);
        config.setPoolName("Oracle-Hikari-Pool");
        config.setAutoCommit(true);
        config.setInitializationFailTimeout(-1);

        // Oracle specific optimizations
        config.addDataSourceProperty("defaultRowPrefetch", "500");
        config.addDataSourceProperty("defaultBatchValue", "100");
        config.addDataSourceProperty("implicitStatementCacheSize", "50");
        config.addDataSourceProperty("oracle.jdbc.ReadTimeout", "30000");
        config.addDataSourceProperty("oracle.net.CONNECT_TIMEOUT", "10000");

        return new HikariDataSource(config);
    }

    @Bean(name = "oracleJdbcTemplate")
    @Lazy
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(500);
        jdbcTemplate.setQueryTimeout(30);
        jdbcTemplate.setMaxRows(10000);
        jdbcTemplate.setResultsMapCaseInsensitive(false);
        return jdbcTemplate;
    }

    @Bean(name = "oracleEntityManagerFactory")
    @Lazy
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("oracleDataSource") DataSource dataSource,
            @Value("${spring.jpa.oracle.properties.hibernate.dialect:org.hibernate.dialect.OracleDialect}") String dialect,
            @Value("${spring.jpa.oracle.hibernate.ddl-auto:none}") String ddlAuto,
            @Value("${spring.jpa.oracle.show-sql:true}") String showSql) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.jdbc.lob.non_contextual_creation", true);

        properties.put("hibernate.jdbc.batch_size", "100");
        properties.put("hibernate.jdbc.fetch_size", "500");

        return builder
                .dataSource(dataSource)
                .packages("com.usg.autoAPIGenerator.entities.oracle")
                .persistenceUnit("oracle")
                .properties(properties)
                .build();
    }

    @Bean(name = "oracleTransactionManager")
    @Lazy
    public PlatformTransactionManager oracleTransactionManager(
            @Qualifier("oracleEntityManagerFactory") LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory) {
        return new JpaTransactionManager(oracleEntityManagerFactory.getObject());
    }

    // ==================== ADD TRANSACTION TEMPLATE FOR EXECUTION HELPERS ====================

    @Bean(name = "transactionTemplate")
    public org.springframework.transaction.support.TransactionTemplate transactionTemplate(
            @Qualifier("postgresTransactionManager") PlatformTransactionManager transactionManager) {
        org.springframework.transaction.support.TransactionTemplate template =
                new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        template.setTimeout(30);
        template.setReadOnly(false);
        return template;
    }
}