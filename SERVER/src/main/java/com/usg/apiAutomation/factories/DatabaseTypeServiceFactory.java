package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.enums.DatabaseTypeEnum;
import com.usg.apiAutomation.helpers.ApiAnalyticsHelper;
import com.usg.apiAutomation.helpers.DatabaseMetadataHelper;
import com.usg.apiAutomation.helpers.DatabaseValidationHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiValidationHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiMetadataHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiValidationHelper;
import com.usg.apiAutomation.services.schemaBrowser.OracleSchemaService;
import com.usg.apiAutomation.services.schemaBrowser.PostgreSQLSchemaService;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DatabaseTypeServiceFactory {

    // Service Maps
    private final Map<DatabaseTypeEnum, DatabaseValidationHelper> validationHelpers = new EnumMap<>(DatabaseTypeEnum.class);
    private final Map<DatabaseTypeEnum, DatabaseMetadataHelper> metadataHelpers = new EnumMap<>(DatabaseTypeEnum.class);
    private final Map<DatabaseTypeEnum, ApiAnalyticsHelper> analyticsHelpers = new EnumMap<>(DatabaseTypeEnum.class);
    private final Map<DatabaseTypeEnum, Object> schemaServices = new EnumMap<>(DatabaseTypeEnum.class);
    
    // Connection Pool Management
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> connectionLeakCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalExecutions = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failedExecutions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthChecker = Executors.newSingleThreadScheduledExecutor();
    
    @Autowired
    @Qualifier("postgresqlDataSource")
    private DataSource postgresqlDataSource;
    
    @Autowired
    @Qualifier("oracleDataSource")
    private DataSource oracleDataSource;
    
    @Autowired
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;
    
    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;
    
    @Autowired
    private OracleApiValidationHelper oracleValidationHelper;
    
    @Autowired
    private OracleApiMetadataHelper oracleMetadataHelper;
    
    @Autowired
    private OracleSchemaService oracleSchemaService;
    
    @Autowired
    private PostgreSQLApiValidationHelper postgresValidationHelper;
    
    @Autowired
    private PostgreSQLApiMetadataHelper postgresMetadataHelper;
    
    @Autowired
    private PostgreSQLSchemaService postgreSQLSchemaService;
    
    @PostConstruct
    public void init() {
        // Register Oracle services
        validationHelpers.put(DatabaseTypeEnum.ORACLE, oracleValidationHelper);
        metadataHelpers.put(DatabaseTypeEnum.ORACLE, oracleMetadataHelper);
        analyticsHelpers.put(DatabaseTypeEnum.ORACLE, oracleMetadataHelper);
        schemaServices.put(DatabaseTypeEnum.ORACLE, oracleSchemaService);
        
        // Register PostgreSQL services
        validationHelpers.put(DatabaseTypeEnum.POSTGRESQL, postgresValidationHelper);
        metadataHelpers.put(DatabaseTypeEnum.POSTGRESQL, postgresMetadataHelper);
        analyticsHelpers.put(DatabaseTypeEnum.POSTGRESQL, postgresMetadataHelper);
        schemaServices.put(DatabaseTypeEnum.POSTGRESQL, postgreSQLSchemaService);
        
        // Register DataSources
        dataSources.put("postgresql", postgresqlDataSource);
        dataSources.put("oracle", oracleDataSource);
        
        // Initialize counters
        connectionLeakCounts.put("postgresql", new AtomicLong(0));
        connectionLeakCounts.put("oracle", new AtomicLong(0));
        totalExecutions.put("postgresql", new AtomicLong(0));
        totalExecutions.put("oracle", new AtomicLong(0));
        failedExecutions.put("postgresql", new AtomicLong(0));
        failedExecutions.put("oracle", new AtomicLong(0));
        
        // Start health checker
        startHealthChecker();
        
        log.info("DatabaseTypeServiceFactory initialized with {} database types and connection pool monitoring", 
            validationHelpers.size());
    }
    
    private void startHealthChecker() {
        healthChecker.scheduleAtFixedRate(() -> {
            try {
                checkConnectionPools();
                logConnectionPoolStats();
            } catch (Exception e) {
                log.error("Health check failed: {}", e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    private void checkConnectionPools() {
        // Check PostgreSQL pool
        checkPool("postgresql", postgresqlDataSource);
        
        // Check Oracle pool
        checkPool("oracle", oracleDataSource);
        
        // Check for stuck connections
        checkForStuckConnections();
    }
    
    private void checkPool(String name, DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
            
            int active = pool.getActiveConnections();
            int idle = pool.getIdleConnections();
            int total = pool.getTotalConnections();
            int waiting = pool.getThreadsAwaitingConnection();
            
            // Auto-heal when connections are stuck
            if (active > total * 0.8) { // 80% usage
                log.warn("{} Pool at high usage: {} active out of {}", name, active, total);
                
                if (waiting > 0) {
                    log.error("{} Pool has {} waiting threads - possible connection leak!", name, waiting);
                    connectionLeakCounts.get(name).incrementAndGet();
                    
                    // Auto-heal: Force connection pool reset if too many leaks
                    if (connectionLeakCounts.get(name).get() > 10) {
                        log.error("Too many connection leaks detected in {} pool! Forcing pool reset...", name);
                        resetPool(name);
                        connectionLeakCounts.get(name).set(0);
                    }
                }
            }
        }
    }
    
    private void checkForStuckConnections() {
        try {
            // Test PostgreSQL connection with timeout
            postgresqlJdbcTemplate.setQueryTimeout(5);
            postgresqlJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            // Test Oracle connection with timeout
            oracleJdbcTemplate.setQueryTimeout(5);
            oracleJdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            
        } catch (Exception e) {
            log.error("Connection test failed: {}", e.getMessage());
        } finally {
            // Reset timeouts
            postgresqlJdbcTemplate.setQueryTimeout(0);
            oracleJdbcTemplate.setQueryTimeout(0);
        }
    }
    
    private void resetPool(String databaseType) {
        DataSource dataSource = dataSources.get(databaseType);
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            try {
                log.warn("Resetting connection pool for {}", databaseType);
                
                // Soft evict all connections
                hikari.getHikariPoolMXBean().softEvictConnections();
                
                // Wait a moment for connections to be released
                Thread.sleep(1000);
                
                // Force a connection test
                if ("postgresql".equals(databaseType)) {
                    postgresqlJdbcTemplate.queryForObject("SELECT 1", Integer.class);
                } else {
                    oracleJdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
                }
                
                log.info("Connection pool reset completed for {}", databaseType);
            } catch (Exception e) {
                log.error("Failed to reset pool for {}: {}", databaseType, e.getMessage());
            }
        }
    }
    
    private void logConnectionPoolStats() {
        if (postgresqlDataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) postgresqlDataSource;
            HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
            
            int active = pool.getActiveConnections();
            int idle = pool.getIdleConnections();
            int total = pool.getTotalConnections();
            int waiting = pool.getThreadsAwaitingConnection();
            
//            log.info("PostgreSQL Pool - Active: {}, Idle: {}, Total: {}, Waiting: {}, Leaks: {}, Success: {}, Failed: {}",
//                active, idle, total, waiting,
//                connectionLeakCounts.get("postgresql").get(),
//                totalExecutions.get("postgresql").get(),
//                failedExecutions.get("postgresql").get());
        }
        
        if (oracleDataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) oracleDataSource;
            HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
            
            int active = pool.getActiveConnections();
            int idle = pool.getIdleConnections();
            int total = pool.getTotalConnections();
            int waiting = pool.getThreadsAwaitingConnection();
            
//            log.info("Oracle Pool - Active: {}, Idle: {}, Total: {}, Waiting: {}, Leaks: {}, Success: {}, Failed: {}",
//                active, idle, total, waiting,
//                connectionLeakCounts.get("oracle").get(),
//                totalExecutions.get("oracle").get(),
//                failedExecutions.get("oracle").get());
        }
    }
    
    @PreDestroy
    public void shutdown() {
        healthChecker.shutdown();
        try {
            if (!healthChecker.awaitTermination(5, TimeUnit.SECONDS)) {
                healthChecker.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthChecker.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("DatabaseTypeServiceFactory shutdown");
    }
    
    // ==================== EXISTING METHODS ====================
    
    public DatabaseValidationHelper getValidationHelper(DatabaseTypeEnum type) {
        DatabaseValidationHelper helper = validationHelpers.get(type);
        if (helper == null) {
            log.error("No validation helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }
    
    public DatabaseValidationHelper getValidationHelper(String typeStr) {
        return getValidationHelper(DatabaseTypeEnum.fromString(typeStr));
    }
    
    public DatabaseMetadataHelper getMetadataHelper(DatabaseTypeEnum type) {
        DatabaseMetadataHelper helper = metadataHelpers.get(type);
        if (helper == null) {
            log.error("No metadata helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }
    
    public DatabaseMetadataHelper getMetadataHelper(String typeStr) {
        return getMetadataHelper(DatabaseTypeEnum.fromString(typeStr));
    }
    
    public ApiAnalyticsHelper getAnalyticsHelper(DatabaseTypeEnum type) {
        ApiAnalyticsHelper helper = analyticsHelpers.get(type);
        if (helper == null) {
            log.error("No analytics helper found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return helper;
    }
    
    public ApiAnalyticsHelper getAnalyticsHelper(String typeStr) {
        return getAnalyticsHelper(DatabaseTypeEnum.fromString(typeStr));
    }
    
    public Object getSchemaService(DatabaseTypeEnum type) {
        Object service = schemaServices.get(type);
        if (service == null) {
            log.error("No schema service found for database type: {}", type);
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
        return service;
    }
    
    public Object getSchemaService(String typeStr) {
        return getSchemaService(DatabaseTypeEnum.fromString(typeStr));
    }
    
    public DatabaseTypeEnum getDatabaseType(String typeStr) {
        return DatabaseTypeEnum.fromString(typeStr);
    }
    
    // New methods for connection pool management
    public Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // PostgreSQL stats
        if (postgresqlDataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) postgresqlDataSource;
            HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
            
            Map<String, Object> pgStats = new ConcurrentHashMap<>();
            pgStats.put("active", pool.getActiveConnections());
            pgStats.put("idle", pool.getIdleConnections());
            pgStats.put("total", pool.getTotalConnections());
            pgStats.put("waiting", pool.getThreadsAwaitingConnection());
            pgStats.put("leakCount", connectionLeakCounts.get("postgresql").get());
            pgStats.put("totalExecutions", totalExecutions.get("postgresql").get());
            pgStats.put("failedExecutions", failedExecutions.get("postgresql").get());
            stats.put("postgresql", pgStats);
        }
        
        // Oracle stats
        if (oracleDataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) oracleDataSource;
            HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
            
            Map<String, Object> oracleStats = new ConcurrentHashMap<>();
            oracleStats.put("active", pool.getActiveConnections());
            oracleStats.put("idle", pool.getIdleConnections());
            oracleStats.put("total", pool.getTotalConnections());
            oracleStats.put("waiting", pool.getThreadsAwaitingConnection());
            oracleStats.put("leakCount", connectionLeakCounts.get("oracle").get());
            oracleStats.put("totalExecutions", totalExecutions.get("oracle").get());
            oracleStats.put("failedExecutions", failedExecutions.get("oracle").get());
            stats.put("oracle", oracleStats);
        }
        
        return stats;
    }
    
    public void resetConnectionPool(String databaseType) {
        resetPool(databaseType);
    }
    
    public void incrementExecution(String databaseType, boolean success) {
        totalExecutions.computeIfAbsent(databaseType, k -> new AtomicLong()).incrementAndGet();
        if (!success) {
            failedExecutions.computeIfAbsent(databaseType, k -> new AtomicLong()).incrementAndGet();
        }
    }
}