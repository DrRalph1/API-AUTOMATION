package com.usg.apiAutomation.factories;

import com.usg.apiAutomation.helpers.BaseApiExecutionHelper;
import com.usg.apiAutomation.helpers.apiEngine.oracle.OracleApiExecutionHelper;
import com.usg.apiAutomation.helpers.apiEngine.postgresql.PostgreSQLApiExecutionHelper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiExecutionHelperFactory {

    private final OracleApiExecutionHelper oracleExecutionHelper;
    private final PostgreSQLApiExecutionHelper postgresqlExecutionHelper;
    private final DatabaseTypeServiceFactory databaseTypeFactory;
    
    // Cache for execution helpers
    private final Map<String, BaseApiExecutionHelper> helperCache = new ConcurrentHashMap<>();
    
    // Performance metrics
    private final Map<String, AtomicLong> executionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalExecutionTime = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        helperCache.put("oracle", oracleExecutionHelper);
        helperCache.put("postgresql", postgresqlExecutionHelper);
        helperCache.put("postgres", postgresqlExecutionHelper);
        
        log.info("ApiExecutionHelperFactory initialized with {} helpers", helperCache.size());
    }
    
    public BaseApiExecutionHelper getExecutionHelper(String databaseType) {
        if (databaseType == null || databaseType.isEmpty()) {
            log.warn("Database type is null or empty, defaulting to Oracle");
            return oracleExecutionHelper;
        }
        
        String key = databaseType.toLowerCase();
        BaseApiExecutionHelper helper = helperCache.get(key);
        
        if (helper == null) {
            log.warn("No helper found for database type: {}, defaulting to Oracle", databaseType);
            helper = oracleExecutionHelper;
        }
        
        // Track usage for metrics
        executionCounts.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
        
        return helper;
    }
    
    /**
     * Execute with performance monitoring and automatic connection pool recovery
     */
    public <T> T executeWithMonitoring(String databaseType, String operation, 
                                       java.util.function.Supplier<T> supplier) {
        long startTime = System.nanoTime();
        String key = databaseType + ":" + operation;
        
        try {
            T result = supplier.get();
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            
            // Update execution time metrics
            totalExecutionTime.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(duration);
            
            // Track success
            databaseTypeFactory.incrementExecution(databaseType, true);
            
            // Log slow operations
            if (duration > 1000) {
                log.warn("Slow operation on {}: {} took {} ms", databaseType, operation, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            errorCounts.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
            
            // Track failure
            databaseTypeFactory.incrementExecution(databaseType, false);
            
            // Check for connection pool errors
            if (e.getMessage() != null && e.getMessage().contains("Connection is not available")) {
                log.error("Connection pool exhausted for {}: {}", databaseType, e.getMessage());
                log.error("Current pool stats: {}", databaseTypeFactory.getConnectionPoolStats());
                
                // Force connection pool reset
                databaseTypeFactory.resetConnectionPool(databaseType);
                
                // Retry once after reset
                try {
                    log.info("Retrying operation after pool reset...");
                    T retryResult = supplier.get();
                    log.info("Retry successful after pool reset");
                    return retryResult;
                } catch (Exception retryException) {
                    log.error("Retry also failed: {}", retryException.getMessage());
                }
            }
            
            log.error("Operation failed on {} after {} ms: {}", databaseType, operation, duration, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get execution statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // Copy current values
        Map<String, Long> counts = new ConcurrentHashMap<>();
        executionCounts.forEach((k, v) -> counts.put(k, v.get()));
        stats.put("executionCounts", counts);
        
        Map<String, Long> errors = new ConcurrentHashMap<>();
        errorCounts.forEach((k, v) -> errors.put(k, v.get()));
        stats.put("errorCounts", errors);
        
        // Calculate averages
        Map<String, Double> averages = new ConcurrentHashMap<>();
        totalExecutionTime.forEach((key, totalTime) -> {
            String[] parts = key.split(":");
            String dbType = parts[0];
            AtomicLong count = executionCounts.get(dbType);
            if (count != null && count.get() > 0) {
                averages.put(key, totalTime.get() / (double) count.get());
            }
        });
        stats.put("averageTimes", averages);
        
        return stats;
    }
    
    /**
     * Clear all caches across all helpers
     */
    public void clearAllCaches() {
        oracleExecutionHelper.clearCaches();
        postgresqlExecutionHelper.clearCaches();
        log.info("All caches cleared across all execution helpers");
    }
    
    /**
     * Get cache statistics from all helpers
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("oracle", oracleExecutionHelper.getCacheStats());
        stats.put("postgresql", postgresqlExecutionHelper.getCacheStats());
        stats.put("factory", Map.of(
            "helperCacheSize", helperCache.size(),
            "executionCounts", executionCounts.size(),
            "errorCounts", errorCounts.size()
        ));
        return stats;
    }
}