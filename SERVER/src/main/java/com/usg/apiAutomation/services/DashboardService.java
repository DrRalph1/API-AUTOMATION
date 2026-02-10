package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.dashboard.*;
import com.usg.apiAutomation.helpers.FilePathHelper;
import com.usg.apiAutomation.utils.FileUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DashboardService {

    private final LoggerUtil loggerUtil;

    // Cache for dashboard data
    private final Map<String, DashboardCache> dashboardCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes cache TTL
    private static final int MAX_CACHED_ACTIVITIES = 100;

    // Sample data generators (would be replaced with real data sources)
    private static final String[] ACTIVITY_TYPES = {"API Generated", "Database Connected", "Code Generated",
            "Schema Updated", "User Login", "Configuration Updated",
            "Backup Created", "Test Executed", "Deployment Completed",
            "Security Scan", "Performance Test", "Bug Fixed"};
    private static final String[] ACTIVITY_USERS = {"Admin", "System", "Developer", "DBA", "Tester",
            "DevOps", "Security Analyst"};
    private static final String[] API_CATEGORIES = {"Authentication", "Payments", "Inventory", "Orders",
            "Support", "Analytics", "Notifications", "Content"};
    private static final String[] DATABASE_TYPES = {"oracle", "postgresql", "mysql", "mongodb", "redis"};

    @PostConstruct
    public void init() {
        log.info("DashboardService initialized");
        preloadDashboardCache();
    }

    // ========== PUBLIC SERVICE METHODS ==========

    public DashboardStatsResponse getDashboardStats(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting dashboard statistics for userManagement: {}", requestId, performedBy);
            loggerUtil.log("dashboard",
                    "Request ID: " + requestId + ", Getting dashboard statistics for userManagement: " + performedBy);

            // Check cache first
            String cacheKey = "dashboard_stats_" + performedBy;
            DashboardCache cachedData = dashboardCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached dashboard statistics", requestId);
                return (DashboardStatsResponse) cachedData.getData();
            }

            DashboardStatsResponse stats = generateDashboardStats();

            // Update cache
            dashboardCache.put(cacheKey, new DashboardCache(stats, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved dashboard statistics", requestId);
            loggerUtil.log("dashboard",
                    "Request ID: " + requestId + ", Retrieved dashboard statistics for userManagement: " + performedBy);

            return stats;

        } catch (Exception e) {
            String errorMsg = "Error retrieving dashboard statistics: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            loggerUtil.log("dashboard",
                    "Request ID: " + requestId + ", " + errorMsg);
            return getFallbackDashboardStats();
        }
    }

    public DashboardConnectionsResponse getDashboardConnections(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting dashboard connections for userManagement: {}", requestId, performedBy);

            // Check cache first
            String cacheKey = "dashboard_connections_" + performedBy;
            DashboardCache cachedData = dashboardCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached dashboard connections", requestId);
                return (DashboardConnectionsResponse) cachedData.getData();
            }

            DashboardConnectionsResponse connections = generateDashboardConnections();

            // Update cache
            dashboardCache.put(cacheKey, new DashboardCache(connections, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved {} dashboard connections", requestId, connections.getConnections().size());

            return connections;

        } catch (Exception e) {
            String errorMsg = "Error retrieving dashboard connections: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackDashboardConnections();
        }
    }

    public DashboardApisResponse getDashboardApis(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting dashboard APIs for userManagement: {}", requestId, performedBy);

            // Check cache first
            String cacheKey = "dashboard_apis_" + performedBy;
            DashboardCache cachedData = dashboardCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached dashboard APIs", requestId);
                return (DashboardApisResponse) cachedData.getData();
            }

            DashboardApisResponse apis = generateDashboardApis();

            // Update cache
            dashboardCache.put(cacheKey, new DashboardCache(apis, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved {} dashboard APIs", requestId, apis.getApis().size());

            return apis;

        } catch (Exception e) {
            String errorMsg = "Error retrieving dashboard APIs: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackDashboardApis();
        }
    }

    public DashboardActivitiesResponse getDashboardActivities(String requestId, HttpServletRequest req,
                                                              String performedBy, int page, int size) {
        try {
            log.info("Request ID: {}, Getting dashboard activities for userManagement: {}, Page: {}, Size: {}",
                    requestId, performedBy, page, size);

            // Check cache first
            String cacheKey = "dashboard_activities_" + performedBy + "_" + page + "_" + size;
            DashboardCache cachedData = dashboardCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached dashboard activities", requestId);
                return (DashboardActivitiesResponse) cachedData.getData();
            }

            List<ActivityDto> allActivities = generateDashboardActivities();

            // Pagination
            int totalItems = allActivities.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = Math.min((page - 1) * size, totalItems);
            int endIndex = Math.min(startIndex + size, totalItems);

            List<ActivityDto> paginatedActivities = allActivities.subList(startIndex, endIndex);

            DashboardActivitiesResponse response = new DashboardActivitiesResponse(
                    paginatedActivities, page, size, totalItems, totalPages
            );

            // Update cache
            dashboardCache.put(cacheKey, new DashboardCache(response, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved {} dashboard activities (page {})",
                    requestId, paginatedActivities.size(), page);

            return response;

        } catch (Exception e) {
            String errorMsg = "Error retrieving dashboard activities: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new DashboardActivitiesResponse(Collections.emptyList(), page, size, 0, 0);
        }
    }

    public DashboardSchemaStatsResponse getDashboardSchemaStats(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting dashboard schema statistics for userManagement: {}", requestId, performedBy);

            // Check cache first
            String cacheKey = "dashboard_schema_stats_" + performedBy;
            DashboardCache cachedData = dashboardCache.get(cacheKey);

            if (cachedData != null && !isCacheExpired(cachedData)) {
                log.debug("Request ID: {}, Returning cached dashboard schema statistics", requestId);
                return (DashboardSchemaStatsResponse) cachedData.getData();
            }

            DashboardSchemaStatsResponse schemaStats = generateDashboardSchemaStats();

            // Update cache
            dashboardCache.put(cacheKey, new DashboardCache(schemaStats, System.currentTimeMillis()));

            log.info("Request ID: {}, Retrieved dashboard schema statistics", requestId);

            return schemaStats;

        } catch (Exception e) {
            String errorMsg = "Error retrieving dashboard schema statistics: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return getFallbackDashboardSchemaStats();
        }
    }

    public Map<String, Object> getDashboardSystemHealth(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting dashboard systemActivities health for userManagement: {}", requestId, performedBy);

            Map<String, Object> systemHealth = new HashMap<>();

            // CPU usage (simulated)
            systemHealth.put("cpu", Math.round(Math.random() * 40 + 10)); // 10-50%
            systemHealth.put("memory", Math.round(Math.random() * 50 + 30)); // 30-80%
            systemHealth.put("disk", Math.round(Math.random() * 60 + 20)); // 20-80%
            systemHealth.put("network", Math.round(Math.random() * 70 + 20)); // 20-90%

            // Active connections
            systemHealth.put("activeConnections", Math.round(Math.random() * 50 + 10));
            systemHealth.put("maxConnections", 100);

            // API health
            systemHealth.put("apiSuccessRate", 99.5 + (Math.random() * 0.5)); // 99.5-100%
            systemHealth.put("apiAvgResponseTime", Math.round(Math.random() * 30 + 20)); // 20-50ms

            // Database health
            systemHealth.put("databaseSize", "2.4 GB");
            systemHealth.put("databaseUptime", "99.9%");
            systemHealth.put("databaseConnections", Math.round(Math.random() * 20 + 5));

            // Service status
            Map<String, String> serviceStatus = new HashMap<>();
            serviceStatus.put("apiGateway", "healthy");
            serviceStatus.put("database", "healthy");
            serviceStatus.put("cache", "healthy");
            serviceStatus.put("messageQueue", "healthy");
            serviceStatus.put("monitoring", "healthy");
            systemHealth.put("serviceStatus", serviceStatus);

            // Last updated
            systemHealth.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            log.info("Request ID: {}, Retrieved dashboard systemActivities health", requestId);

            return systemHealth;

        } catch (Exception e) {
            String errorMsg = "Error retrieving dashboard systemActivities health: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return Map.of("error", "Failed to fetch systemActivities health: " + e.getMessage());
        }
    }

    public Map<String, Object> getCodeGenerationStats(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting code generation statistics for userManagement: {}", requestId, performedBy);

            Map<String, Object> codeStats = new HashMap<>();

            // Language distribution
            Map<String, Integer> languageDistribution = new HashMap<>();
            languageDistribution.put("java", 45);
            languageDistribution.put("javascript", 32);
            languageDistribution.put("python", 18);
            languageDistribution.put("csharp", 5);
            codeStats.put("languageDistribution", languageDistribution);

            // Total generations
            codeStats.put("totalGenerations", 1500);
            codeStats.put("generationsThisMonth", 125);
            codeStats.put("generationsToday", 12);

            // Success rate
            codeStats.put("successRate", "98.7%");
            codeStats.put("avgGenerationTime", "2.3s");

            // Popular templates
            List<Map<String, Object>> popularTemplates = Arrays.asList(
                    Map.of("name", "REST API", "count", 450, "language", "java"),
                    Map.of("name", "Database Model", "count", 320, "language", "java"),
                    Map.of("name", "Authentication Service", "count", 280, "language", "javascript"),
                    Map.of("name", "Payment Processor", "count", 210, "language", "python"),
                    Map.of("name", "Web Socket Server", "count", 180, "language", "javascript")
            );
            codeStats.put("popularTemplates", popularTemplates);

            // Recent activity
            codeStats.put("lastGenerated", LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            codeStats.put("generatedBy", performedBy);

            log.info("Request ID: {}, Retrieved code generation statistics", requestId);

            return codeStats;

        } catch (Exception e) {
            String errorMsg = "Error retrieving code generation statistics: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return Map.of("error", "Failed to fetch code generation statistics: " + e.getMessage());
        }
    }

    public void clearDashboardCache(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Clearing dashboard cache for userManagement: {}", requestId, performedBy);

            int beforeSize = dashboardCache.size();
            dashboardCache.clear();
            int afterSize = dashboardCache.size();

            log.info("Request ID: {}, Cleared {} dashboard cache entries", requestId, beforeSize - afterSize);
            loggerUtil.log("dashboard",
                    "Request ID: " + requestId + ", Cleared dashboard cache for userManagement: " + performedBy);

        } catch (Exception e) {
            String errorMsg = "Error clearing dashboard cache: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void preloadDashboardCache() {
        try {
            log.info("Preloading dashboard cache with sample data");

            // Preload stats
            DashboardStatsResponse stats = generateDashboardStats();
            dashboardCache.put("dashboard_stats_admin", new DashboardCache(stats, System.currentTimeMillis()));

            // Preload connections
            DashboardConnectionsResponse connections = generateDashboardConnections();
            dashboardCache.put("dashboard_connections_admin", new DashboardCache(connections, System.currentTimeMillis()));

            // Preload APIs
            DashboardApisResponse apis = generateDashboardApis();
            dashboardCache.put("dashboard_apis_admin", new DashboardCache(apis, System.currentTimeMillis()));

            // Preload schema stats
            DashboardSchemaStatsResponse schemaStats = generateDashboardSchemaStats();
            dashboardCache.put("dashboard_schema_stats_admin", new DashboardCache(schemaStats, System.currentTimeMillis()));

            log.info("Dashboard cache preloaded with {} entries", dashboardCache.size());

        } catch (Exception e) {
            log.warn("Failed to preload dashboard cache: {}", e.getMessage());
        }
    }

    private boolean isCacheExpired(DashboardCache cache) {
        return (System.currentTimeMillis() - cache.getTimestamp()) > CACHE_TTL_MS;
    }

    private DashboardStatsResponse generateDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Generate random stats (in real implementation, these would come from database)
        stats.setTotalConnections((int) (Math.random() * 10 + 5)); // 5-15 connections
        stats.setActiveConnections((int) (Math.random() * 8 + 3)); // 3-11 active connections
        stats.setTotalApis((int) (Math.random() * 15 + 10)); // 10-25 APIs
        stats.setActiveApis((int) (Math.random() * 12 + 8)); // 8-20 active APIs
        stats.setTotalCalls((int) (Math.random() * 10000 + 5000)); // 5000-15000 calls
        stats.setAvgLatency(String.format("%.0fms", Math.random() * 30 + 20)); // 20-50ms
        stats.setSuccessRate(String.format("%.1f%%", 95 + Math.random() * 5)); // 95-100%
        stats.setUptime(String.format("%.1f%%", 99 + Math.random() * 1)); // 99-100%

        // Change percentages (random positive changes for demo)
        stats.setConnectionsChange((int) (Math.random() * 5 + 1)); // 1-6%
        stats.setApisChange((int) (Math.random() * 8 + 2)); // 2-10%
        stats.setCallsChange((int) (Math.random() * 10 + 5)); // 5-15%
        stats.setSuccessRateChange((float) (Math.random() * 0.5 + 0.1)); // 0.1-0.6%

        // Additional metrics
        stats.setAvgResponseTime(String.format("%.0fms", Math.random() * 40 + 20)); // 20-60ms
        stats.setErrorRate(String.format("%.2f%%", Math.random() * 0.5)); // 0-0.5%
        stats.setPeakConnections((int) (Math.random() * 20 + 10)); // 10-30 peak connections
        stats.setDataTransferred(String.format("%.1f GB", Math.random() * 10 + 1)); // 1-11 GB

        // Timestamp
        stats.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return stats;
    }

    private DashboardConnectionsResponse generateDashboardConnections() {
        List<ConnectionDto> connections = new ArrayList<>();

        // Generate sample connections
        for (int i = 1; i <= 1; i++) {
            ConnectionDto connection = new ConnectionDto();
            connection.setId("conn-" + i);
            connection.setName("DB_CONNECTION_" + i);
            connection.setDescription(i == 1 ? "Development Database" :
                    i == 2 ? "Production Database" :
                            "Test Database " + (i - 2));
            connection.setHost("db" + i + ".unionsg.com");
            connection.setPort(i == 1 ? "1521" : i == 2 ? "5432" : "3306");
            connection.setService(i == 1 ? "ORCL" : i == 2 ? "postgres" : "mysql");
            connection.setUsername(i == 1 ? "HR" : i == 2 ? "admin" : "developer");
            connection.setStatus(i <= 6 ? "connected" : i == 7 ? "idle" : "disconnected");
            connection.setType(DATABASE_TYPES[(i - 1) % DATABASE_TYPES.length]);
            connection.setLatency(String.format("%.0fms", Math.random() * 20 + 10)); // 10-30ms
            connection.setUptime(String.format("%.1f%%", 99 + Math.random() * 1)); // 99-100%
            connection.setLastConnected(LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            connection.setDriver(i == 1 ? "Oracle JDBC" : i == 2 ? "PostgreSQL JDBC" : "MySQL Connector/J");
            connection.setVersion(i == 1 ? "19c" : i == 2 ? "14" : "8.0");
            connection.setMaxConnections(50);
            connection.setCurrentConnections((int) (Math.random() * 20 + 5));
            connection.setDatabaseSize(String.format("%.1f GB", Math.random() * 5 + 1));
            connection.setTablespaceUsed(String.format("%.0f%%", Math.random() * 40 + 30));

            connections.add(connection);
        }

        return new DashboardConnectionsResponse(connections);
    }

    private DashboardApisResponse generateDashboardApis() {
        List<ApiDto> apis = new ArrayList<>();

        // Generate sample APIs
        for (int i = 1; i <= 8; i++) {
            ApiDto api = new ApiDto();
            api.setId("api-" + i);
            api.setName(getApiName(i));
            api.setDescription(getApiDescription(i));
            api.setVersion(getApiVersion(i));
            api.setStatus("active");
            api.setEndpointCount((int) (Math.random() * 15 + 5)); // 5-20 endpoints
            api.setLastUpdated(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            api.setCalls((int) (Math.random() * 5000 + 1000)); // 1000-6000 calls
            api.setLatency(String.format("%.0fms", Math.random() * 40 + 20)); // 20-60ms
            api.setSuccessRate(String.format("%.1f%%", 98 + Math.random() * 2)); // 98-100%
            api.setBaseUrl("https://api.example.com/" + getApiVersion(i) + "/" + getApiPath(i));
            api.setDocumentation("https://docs.example.com/api/" + getApiVersion(i));
            api.setSupportedMethods(getApiMethods(i));
            api.setSecurity(getApiSecurity(i));
            api.setRateLimit((int) (Math.random() * 2000 + 500) + " requests/hour");
            api.setErrors((int) (Math.random() * 20 + 1)); // 1-20 errors
            api.setAvgResponseTime(String.format("%.0fms", Math.random() * 35 + 15)); // 15-50ms
            api.setUptime(String.format("%.1f%%", 99 + Math.random() * 1)); // 99-100%
            api.setLastDeployed(LocalDateTime.now().minusDays(i + 2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            api.setOwner(getApiOwner(i));
            api.setCategory(API_CATEGORIES[(i - 1) % API_CATEGORIES.length]);

            apis.add(api);
        }

        return new DashboardApisResponse(apis);
    }

    private List<ActivityDto> generateDashboardActivities() {
        List<ActivityDto> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= MAX_CACHED_ACTIVITIES; i++) {
            ActivityDto activity = new ActivityDto();
            activity.setId("act-" + i);

            // Random activity type
            String action = ACTIVITY_TYPES[(int) (Math.random() * ACTIVITY_TYPES.length)];
            activity.setAction(action);

            // Generate description based on action
            activity.setDescription(generateActivityDescription(action, i));

            // Random userManagement
            activity.setUser(ACTIVITY_USERS[(int) (Math.random() * ACTIVITY_USERS.length)]);

            // Random time (within last 7 days)
            int hoursAgo = (int) (Math.random() * 168); // 0-168 hours (7 days)
            activity.setTimestamp(now.minusHours(hoursAgo).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // Format time for display
            if (hoursAgo == 0) {
                activity.setTime("Just now");
            } else if (hoursAgo < 24) {
                activity.setTime(hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago");
            } else {
                int daysAgo = hoursAgo / 24;
                activity.setTime(daysAgo + " day" + (daysAgo > 1 ? "s" : "") + " ago");
            }

            // Random icon
            activity.setIcon(getActivityIcon(action));

            // Random priority (70% low, 20% medium, 10% high)
            double rand = Math.random();
            if (rand < 0.7) {
                activity.setPriority("low");
            } else if (rand < 0.9) {
                activity.setPriority("medium");
            } else {
                activity.setPriority("high");
            }

            // Detailed description
            activity.setDetails(generateActivityDetails(action, activity.getUser(), i));

            // Affected resource
            if (action.contains("API")) {
                activity.setAffectedResource("API-" + (int) (Math.random() * 100));
                activity.setActionType("api");
            } else if (action.contains("Database")) {
                activity.setAffectedResource("DB-" + (int) (Math.random() * 100));
                activity.setActionType("database");
            } else if (action.contains("Code")) {
                activity.setAffectedResource("CODE-" + (int) (Math.random() * 100));
                activity.setActionType("code");
            } else {
                activity.setAffectedResource("SYS-" + (int) (Math.random() * 100));
                activity.setActionType("systemActivities");
            }

            activities.add(activity);
        }

        // Sort by timestamp (most recent first)
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return activities;
    }

    private DashboardSchemaStatsResponse generateDashboardSchemaStats() {
        DashboardSchemaStatsResponse schemaStats = new DashboardSchemaStatsResponse();

        // Basic stats
        schemaStats.setTotalObjects(156);
        schemaStats.setTables(45);
        schemaStats.setViews(12);
        schemaStats.setProcedures(23);
        schemaStats.setPackages(8);
        schemaStats.setFunctions(15);
        schemaStats.setTriggers(9);
        schemaStats.setIndexes(44);
        schemaStats.setSequences(5);
        schemaStats.setMaterializedViews(3);
        schemaStats.setPartitions(21);

        // Database info
        schemaStats.setDatabaseName("HR_DEV");
        schemaStats.setDatabaseSize("2.4 GB");
        schemaStats.setVersion("1.2.3");

        // Monthly changes
        schemaStats.setMonthlyGrowth(12);
        schemaStats.setTableChange(3);
        schemaStats.setViewChange(1);
        schemaStats.setProcedureChange(2);
        schemaStats.setFunctionChange(4);
        schemaStats.setPackageChange(0);
        schemaStats.setTriggerChange(1);
        schemaStats.setIndexChange(5);
        schemaStats.setSequenceChange(0);
        schemaStats.setMaterializedViewChange(0);
        schemaStats.setTotalObjectsChange(16);

        // Last updated
        schemaStats.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return schemaStats;
    }

    private String getActivityIcon(String action) {
        if (action.contains("API")) return "api";
        if (action.contains("Database")) return "database";
        if (action.contains("Code")) return "code";
        if (action.contains("Schema")) return "schema";
        if (action.contains("User")) return "userManagement";
        if (action.contains("Configuration")) return "settings";
        if (action.contains("Backup")) return "backup";
        if (action.contains("Test")) return "test";
        return "systemActivities";
    }

    private String generateActivityDescription(String action, int id) {
        return String.format("%s for task #%d", action, id);
    }

    private String generateActivityDetails(String action, String user, int id) {
        return String.format("Detailed information about %s. This activity involved %s working on task #%d. " +
                        "The action was completed successfully and all systems are operational.",
                action, user, id);
    }

    private String getApiName(int index) {
        String[] names = {
                "User Management API",
                "Payment Processing API",
                "Inventory Management API",
                "Order Processing API",
                "Customer Support API",
                "Analytics API",
                "Notification API",
                "Content Management API"
        };
        return names[(index - 1) % names.length];
    }

    private String getApiDescription(int index) {
        String[] descriptions = {
                "Complete userManagement authentication and management",
                "Secure payment processing",
                "Manage product inventory and stock levels",
                "Handle customer orders and order tracking",
                "Customer support ticket management",
                "Data analytics and reporting endpoints",
                "Send email and push notifications",
                "Manage website content and media"
        };
        return descriptions[(index - 1) % descriptions.length];
    }

    private String getApiVersion(int index) {
        return "v" + ((index % 3) + 1) + "." + (index % 5);
    }

    private String getApiPath(int index) {
        String[] paths = {"users", "payments", "inventory", "orders", "support", "analytics", "notifications", "content"};
        return paths[(index - 1) % paths.length];
    }

    private List<String> getApiMethods(int index) {
        if (index == 2 || index == 7) {
            return Arrays.asList("POST");
        } else if (index == 5 || index == 6) {
            return Arrays.asList("GET", "POST");
        } else {
            return Arrays.asList("GET", "POST", "PUT", "DELETE");
        }
    }

    private String getApiSecurity(int index) {
        if (index == 1 || index == 4 || index == 8) {
            return "JWT Authentication";
        } else if (index == 2) {
            return "API Key + SSL";
        } else if (index == 3) {
            return "OAuth 2.0";
        } else if (index == 6) {
            return "JWT + IP Whitelist";
        } else {
            return "API Key";
        }
    }

    private String getApiOwner(int index) {
        String[] owners = {"John Doe", "Jane Smith", "Bob Johnson", "Alice Brown",
                "Charlie Wilson", "David Lee", "Emma Davis", "Frank Miller"};
        return owners[(index - 1) % owners.length];
    }

    // ========== FALLBACK METHODS ==========

    private DashboardStatsResponse getFallbackDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalConnections(8);
        stats.setActiveConnections(5);
        stats.setTotalApis(12);
        stats.setActiveApis(8);
        stats.setTotalCalls(12500);
        stats.setAvgLatency("48ms");
        stats.setSuccessRate("99.8%");
        stats.setUptime("99.9%");
        stats.setConnectionsChange(5);
        stats.setApisChange(12);
        stats.setCallsChange(8);
        stats.setSuccessRateChange(0.2f);
        stats.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return stats;
    }

    private DashboardConnectionsResponse getFallbackDashboardConnections() {
        List<ConnectionDto> connections = new ArrayList<>();

        ConnectionDto conn = new ConnectionDto();
        conn.setId("conn-1");
        conn.setName("CBX_DMX");
        conn.setDescription("Development Database");
        conn.setHost("db.unionsg.com");
        conn.setPort("1521");
        conn.setService("ORCL");
        conn.setUsername("HR");
        conn.setStatus("connected");
        conn.setType("oracle");
        conn.setLatency("12ms");
        conn.setUptime("99.9%");
        conn.setLastConnected(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        connections.add(conn);

        return new DashboardConnectionsResponse(connections);
    }

    private DashboardApisResponse getFallbackDashboardApis() {
        List<ApiDto> apis = new ArrayList<>();

        ApiDto api = new ApiDto();
        api.setId("api-1");
        api.setName("User Management API");
        api.setDescription("Complete userManagement authentication and management");
        api.setVersion("v2.1");
        api.setStatus("active");
        api.setEndpointCount(12);
        api.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        api.setCalls(1250);
        api.setLatency("45ms");
        api.setSuccessRate("99.8%");
        api.setBaseUrl("https://api.example.com/v2.1/users");

        apis.add(api);

        return new DashboardApisResponse(apis);
    }

    private DashboardSchemaStatsResponse getFallbackDashboardSchemaStats() {
        DashboardSchemaStatsResponse schemaStats = new DashboardSchemaStatsResponse();
        schemaStats.setTotalObjects(156);
        schemaStats.setTables(45);
        schemaStats.setDatabaseName("HR_DEV");
        schemaStats.setDatabaseSize("2.4 GB");
        schemaStats.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return schemaStats;
    }

    // ========== INNER CLASSES ==========

    private static class DashboardCache {
        private final Object data;
        private final long timestamp;

        public DashboardCache(Object data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        public Object getData() {
            return data;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}