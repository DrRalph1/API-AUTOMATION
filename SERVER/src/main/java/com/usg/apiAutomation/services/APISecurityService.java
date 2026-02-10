package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.apiSecurity.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class APISecurityService {

    @Autowired
    private LoggerUtil loggerUtil;

    // ============================================================
    // 1. GET RATE LIMIT RULES
    // ============================================================
    public RateLimitRulesResponse getRateLimitRules(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting rate limit rules for API security");

            RateLimitRulesResponse response = new RateLimitRulesResponse();

            List<RateLimitRule> rules = Arrays.asList(
                    createRateLimitRule("rl-1", "Public API - Standard",
                            "Standard rate limiting for public API endpoints",
                            "/api/v1/**", "ALL", 100, "1m", 20, "throttle", "active",
                            "2024-01-15T10:30:00Z", "Today, 09:45 AM"),
                    createRateLimitRule("rl-2", "Authentication Endpoints",
                            "Strict rate limiting for authentication",
                            "/api/v1/auth/**", "POST", 10, "1m", 5, "block", "active",
                            "2024-01-10T14:20:00Z", "Yesterday, 03:30 PM"),
                    createRateLimitRule("rl-3", "Payment Processing",
                            "Higher limits for payment endpoints",
                            "/api/v1/payments/**", "POST", 500, "1m", 100, "throttle", "active",
                            "2024-01-05T11:15:00Z", "2 days ago")
            );

            response.setRules(rules);
            response.setTotal(rules.size());

            // Add statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBlockedRequests", 1245);
            stats.put("totalThrottledRequests", 8923);
            stats.put("activeRules", rules.size());
            stats.put("coveredEndpoints", 42);
            response.setStatistics(stats);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting rate limit rules: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 2. GET IP WHITELIST
    // ============================================================
    public IPWhitelistResponse getIPWhitelist(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting IP whitelist");

            IPWhitelistResponse response = new IPWhitelistResponse();

            List<IPWhitelistEntry> entries = Arrays.asList(
                    createIPWhitelistEntry("ip-1", "Office Network", "192.168.1.0/24",
                            "Corporate office network", "/api/v1/admin/**", "active",
                            "2024-01-12T09:00:00Z"),
                    createIPWhitelistEntry("ip-2", "VPN Users", "10.0.0.0/16",
                            "Company VPN range", "/api/v1/**", "active",
                            "2024-01-08T13:45:00Z"),
                    createIPWhitelistEntry("ip-3", "Development Team", "172.16.32.0/20",
                            "Development team access", "/api/v1/dev/**", "inactive",
                            "2024-01-03T16:20:00Z")
            );

            response.setEntries(entries);
            response.setTotal(entries.size());

            // Add analysis data
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("totalRanges", entries.size());
            analysis.put("activeRanges", entries.stream().filter(e -> "active".equals(e.getStatus())).count());
            analysis.put("protectedEndpoints", 28);

            List<Map<String, Object>> recentBlocks = Arrays.asList(
                    createRecentBlock("198.51.100.42", 15, "/api/v1/admin/users"),
                    createRecentBlock("203.0.113.78", 8, "/api/v1/auth/login"),
                    createRecentBlock("192.0.2.189", 23, "/api/v1/payments/**")
            );
            analysis.put("recentBlocks", recentBlocks);

            response.setAnalysis(analysis);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting IP whitelist: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 3. GET LOAD BALANCERS
    // ============================================================
    public LoadBalancersResponse getLoadBalancers(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting load balancers");

            LoadBalancersResponse response = new LoadBalancersResponse();

            List<LoadBalancer> loadBalancers = Arrays.asList(
                    createLoadBalancer("lb-1", "Primary API Cluster", "round_robin",
                            "/api/v1/health", "30s", createServers(), "active", 599),
                    createLoadBalancer("lb-2", "Payment Processing", "least_connections",
                            "/api/v1/payments/health", "15s", createPaymentServers(), "active", 165)
            );

            response.setLoadBalancers(loadBalancers);
            response.setTotal(loadBalancers.size());

            // Add performance metrics
            Map<String, Object> performance = new HashMap<>();
            performance.put("totalRequests", 24589);
            performance.put("avgResponseTime", "42ms");
            performance.put("errorRate", "0.2%");
            performance.put("uptime", "99.98%");
            response.setPerformance(performance);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting load balancers: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 4. GET SECURITY EVENTS
    // ============================================================
    public SecurityEventsResponse getSecurityEvents(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security events");

            SecurityEventsResponse response = new SecurityEventsResponse();

            List<SecurityEvent> events = Arrays.asList(
                    createSecurityEvent("evt-1", "rate_limit_exceeded", "medium",
                            "203.0.113.25", "/api/v1/auth/login", "POST",
                            "Rate limit exceeded - 15 requests in 1 minute", "2024-01-15T14:32:10Z"),
                    createSecurityEvent("evt-2", "ip_blocked", "high",
                            "198.51.100.42", "/api/v1/admin/users", "GET",
                            "IP blocked - Not in whitelist", "2024-01-15T13:45:22Z"),
                    createSecurityEvent("evt-3", "suspicious_activity", "critical",
                            "192.0.2.189", "/api/v1/payments/process", "POST",
                            "Multiple failed payment attempts", "2024-01-15T11:20:15Z"),
                    createSecurityEvent("evt-4", "ddos_protection", "high",
                            "203.0.113.0/24", "/api/v1/**", "ALL",
                            "DDoS protection activated - 5000 requests/sec", "2024-01-14T09:15:30Z")
            );

            response.setEvents(events);
            response.setTotal(events.size());

            // Add security insights
            Map<String, Object> insights = new HashMap<>();
            insights.put("threatLevel", "low");
            insights.put("securityScore", 92);

            List<Map<String, Object>> eventTrends = Arrays.asList(
                    createEventTrend("rate_limit_exceeded", 124, "+15%"),
                    createEventTrend("ip_blocks", 28, "-5%"),
                    createEventTrend("suspicious_activity", 7, "+3%")
            );
            insights.put("eventTrends", eventTrends);

            response.setInsights(insights);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security events: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 5. GET SECURITY SUMMARY
    // ============================================================
    public SecuritySummaryResponse getSecuritySummary(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security summary");

            SecuritySummaryResponse response = new SecuritySummaryResponse();

            response.setTotalEndpoints(45);
            response.setSecuredEndpoints(42);
            response.setVulnerableEndpoints(3);
            response.setBlockedRequests(1245);
            response.setThrottledRequests(8923);
            response.setAvgResponseTime("42ms");
            response.setSecurityScore(92);
            response.setLastScan("2024-01-15T10:00:00Z");

            // Add quick stats
            Map<String, Object> quickStats = new HashMap<>();
            quickStats.put("activeThreats", 3);
            quickStats.put("protectedEndpoints", "42/45");
            quickStats.put("responseTimeChange", "-5ms");
            quickStats.put("securityScoreChange", "+2%");
            response.setQuickStats(quickStats);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security summary: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 6. ADD RATE LIMIT RULE
    // ============================================================
    public AddRuleResponse addRateLimitRule(String requestId, String performedBy,
                                            AddRuleRequest addRuleRequest) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Adding rate limit rule: " + addRuleRequest.getName());

            // In real implementation, save to database
            AddRuleResponse response = new AddRuleResponse();
            response.setId("rl-" + UUID.randomUUID().toString().substring(0, 8));
            response.setName(addRuleRequest.getName());
            response.setStatus("active");
            response.setCreatedAt(LocalDateTime.now().toString());
            response.setMessage("Rate limit rule added successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error adding rate limit rule: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 7. ADD IP WHITELIST ENTRY
    // ============================================================
    public AddIPEntryResponse addIPWhitelistEntry(String requestId, String performedBy,
                                                  AddIPEntryRequest addIPEntryRequest) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Adding IP whitelist entry: " + addIPEntryRequest.getName());

            // In real implementation, save to database
            AddIPEntryResponse response = new AddIPEntryResponse();
            response.setId("ip-" + UUID.randomUUID().toString().substring(0, 8));
            response.setName(addIPEntryRequest.getName());
            response.setIpRange(addIPEntryRequest.getIpRange());
            response.setStatus("active");
            response.setCreatedAt(LocalDateTime.now().toString());
            response.setMessage("IP whitelist entry added successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error adding IP whitelist entry: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 8. ADD LOAD BALANCER
    // ============================================================
    public AddLoadBalancerResponse addLoadBalancer(String requestId, String performedBy,
                                                   AddLoadBalancerRequest addLoadBalancerRequest) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Adding load balancer: " + addLoadBalancerRequest.getName());

            // In real implementation, save to database
            AddLoadBalancerResponse response = new AddLoadBalancerResponse();
            response.setId("lb-" + UUID.randomUUID().toString().substring(0, 8));
            response.setName(addLoadBalancerRequest.getName());
            response.setAlgorithm(addLoadBalancerRequest.getAlgorithm());
            response.setStatus("active");
            response.setCreatedAt(LocalDateTime.now().toString());
            response.setMessage("Load balancer added successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error adding load balancer: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 9. UPDATE RULE STATUS
    // ============================================================
    public UpdateRuleStatusResponse updateRuleStatus(String requestId, String performedBy,
                                                     UpdateRuleStatusRequest updateRequest) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Updating rule status: " + updateRequest.getRuleId() +
                    " to " + updateRequest.getStatus());

            UpdateRuleStatusResponse response = new UpdateRuleStatusResponse();
            response.setRuleId(updateRequest.getRuleId());
            response.setStatus(updateRequest.getStatus());
            response.setUpdatedAt(LocalDateTime.now().toString());
            response.setMessage("Rule status updated successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error updating rule status: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 10. DELETE RULE
    // ============================================================
    public DeleteRuleResponse deleteRule(String requestId, String performedBy, String ruleId) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Deleting rule: " + ruleId);

            DeleteRuleResponse response = new DeleteRuleResponse();
            response.setRuleId(ruleId);
            response.setDeleted(true);
            response.setDeletedAt(LocalDateTime.now().toString());
            response.setMessage("Rule deleted successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error deleting rule: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 11. GENERATE SECURITY REPORT
    // ============================================================
    public SecurityReportResponse generateSecurityReport(String requestId, String performedBy,
                                                         GenerateReportRequest reportRequest) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Generating security report");

            SecurityReportResponse response = new SecurityReportResponse();
            response.setReportId("sec-report-" + UUID.randomUUID().toString().substring(0, 8));
            response.setGeneratedAt(LocalDateTime.now().toString());
            response.setStatus("SECURE");

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalChecks", 24);
            summary.put("issuesFound", 1);
            summary.put("securityScore", 92);
            summary.put("threatLevel", "low");
            response.setSummary(summary);

            List<Map<String, Object>> recommendations = Arrays.asList(
                    createRecommendation("Consider implementing stricter rate limits for authentication endpoints", "info"),
                    createRecommendation("Add IP whitelist for admin endpoints", "warning"),
                    createRecommendation("Enable API key rotation for long-lived keys", "info"),
                    createRecommendation("Implement request signing for critical endpoints", "warning")
            );
            response.setRecommendations(recommendations);

            response.setDownloadUrl("/downloads/security-report-" + response.getReportId() + ".pdf");
            response.setExpiresAt(LocalDateTime.now().plusDays(7).toString());

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error generating security report: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 12. RUN SECURITY SCAN
    // ============================================================
    public SecurityScanResponse runSecurityScan(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Running security scan");

            SecurityScanResponse response = new SecurityScanResponse();
            response.setScanId("scan-" + UUID.randomUUID().toString().substring(0, 8));
            response.setStartedAt(LocalDateTime.now().toString());
            response.setStatus("completed");

            List<Map<String, Object>> findings = Arrays.asList(
                    createFinding("rate_limit_misconfiguration", "medium",
                            "Rate limits not configured for authentication endpoints", "/api/v1/auth/**"),
                    createFinding("ip_whitelist_missing", "low",
                            "Admin endpoints not protected by IP whitelist", "/api/v1/admin/**"),
                    createFinding("api_key_exposure", "high",
                            "API keys exposed in logs", "System-wide")
            );
            response.setFindings(findings);

            response.setTotalFindings(findings.size());
            response.setCriticalFindings(findings.stream().filter(f -> "high".equals(f.get("severity"))).count());
            response.setScanDuration("45s");
            response.setSecurityScore(88);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error running security scan: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 13. GET SECURITY CONFIGURATION
    // ============================================================
    public SecurityConfigResponse getSecurityConfiguration(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security configuration");

            SecurityConfigResponse response = new SecurityConfigResponse();

            Map<String, Object> config = new HashMap<>();
            config.put("enableRateLimiting", true);
            config.put("enableIPWhitelisting", true);
            config.put("enableDDoSProtection", true);
            config.put("enableRequestSigning", false);
            config.put("enableAPIKeyRotation", true);
            config.put("securityScanInterval", "24h");
            config.put("alertThreshold", "high");
            config.put("autoBlockSuspiciousIPs", true);
            config.put("maxFailedAttempts", 5);
            config.put("lockoutDuration", "30m");

            response.setConfiguration(config);
            response.setLastUpdated(LocalDateTime.now().toString());

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security configuration: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 14. UPDATE SECURITY CONFIGURATION
    // ============================================================
    public UpdateConfigResponse updateSecurityConfiguration(String requestId, String performedBy,
                                                            UpdateConfigRequest configRequest) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Updating security configuration");

            UpdateConfigResponse response = new UpdateConfigResponse();
            response.setUpdated(true);
            response.setUpdatedAt(LocalDateTime.now().toString());
            response.setMessage("Security configuration updated successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error updating security configuration: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 15. GET SECURITY ALERTS
    // ============================================================
    public SecurityAlertsResponse getSecurityAlerts(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Getting security alerts");

            SecurityAlertsResponse response = new SecurityAlertsResponse();

            List<SecurityAlert> alerts = Arrays.asList(
                    createSecurityAlert("alert-1", "rate_limit_exceeded", "medium",
                            "Rate limit exceeded on authentication endpoint",
                            "/api/v1/auth/login", true, "2024-01-15T14:32:10Z"),
                    createSecurityAlert("alert-2", "suspicious_ip", "high",
                            "Suspicious IP attempting admin access",
                            "/api/v1/admin/users", false, "2024-01-15T13:45:22Z"),
                    createSecurityAlert("alert-3", "ddos_attempt", "critical",
                            "Potential DDoS attack detected",
                            "/api/v1/**", true, "2024-01-14T09:15:30Z")
            );

            response.setAlerts(alerts);
            response.setTotal(alerts.size());
            response.setUnread(alerts.stream().filter(a -> !a.isRead()).count());

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error getting security alerts: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 16. EXPORT SECURITY DATA
    // ============================================================
    public ExportSecurityResponse exportSecurityData(String requestId, String performedBy,
                                                     ExportSecurityRequest exportRequest) {
        try {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Exporting security data in format: " + exportRequest.getFormat());

            ExportSecurityResponse response = new ExportSecurityResponse();
            response.setExportId("export-" + UUID.randomUUID().toString().substring(0, 8));
            response.setFormat(exportRequest.getFormat());
            response.setStatus("ready");
            response.setExportedAt(LocalDateTime.now().toString());

            Map<String, Object> exportInfo = new HashMap<>();
            exportInfo.put("downloadUrl", "/downloads/security-export-" + response.getExportId() +
                    "." + exportRequest.getFormat());
            exportInfo.put("fileSize", "2.4 MB");
            exportInfo.put("expiresAt", LocalDateTime.now().plusHours(24).toString());
            exportInfo.put("includes", Arrays.asList("rules", "events", "whitelist", "reports"));

            response.setExportInfo(exportInfo);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "Request ID: " + requestId +
                    ", Error exporting security data: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private RateLimitRule createRateLimitRule(String id, String name, String description,
                                              String endpoint, String method, int limit,
                                              String window, int burst, String action,
                                              String status, String createdAt, String updatedAt) {
        RateLimitRule rule = new RateLimitRule();
        rule.setId(id);
        rule.setName(name);
        rule.setDescription(description);
        rule.setEndpoint(endpoint);
        rule.setMethod(method);
        rule.setLimit(limit);
        rule.setWindow(window);
        rule.setBurst(burst);
        rule.setAction(action);
        rule.setStatus(status);
        rule.setCreatedAt(createdAt);
        rule.setUpdatedAt(updatedAt);
        return rule;
    }

    private IPWhitelistEntry createIPWhitelistEntry(String id, String name, String ipRange,
                                                    String description, String endpoints,
                                                    String status, String createdAt) {
        IPWhitelistEntry entry = new IPWhitelistEntry();
        entry.setId(id);
        entry.setName(name);
        entry.setIpRange(ipRange);
        entry.setDescription(description);
        entry.setEndpoints(endpoints);
        entry.setStatus(status);
        entry.setCreatedAt(createdAt);
        return entry;
    }

    private List<Map<String, Object>> createServers() {
        List<Map<String, Object>> servers = new ArrayList<>();

        Map<String, Object> server1 = new HashMap<>();
        server1.put("id", "srv-1");
        server1.put("name", "API-Node-1");
        server1.put("address", "10.0.1.1:8080");
        server1.put("status", "healthy");
        server1.put("connections", 245);
        servers.add(server1);

        Map<String, Object> server2 = new HashMap<>();
        server2.put("id", "srv-2");
        server2.put("name", "API-Node-2");
        server2.put("address", "10.0.1.2:8080");
        server2.put("status", "healthy");
        server2.put("connections", 198);
        servers.add(server2);

        Map<String, Object> server3 = new HashMap<>();
        server3.put("id", "srv-3");
        server3.put("name", "API-Node-3");
        server3.put("address", "10.0.1.3:8080");
        server3.put("status", "degraded");
        server3.put("connections", 156);
        servers.add(server3);

        return servers;
    }

    private List<Map<String, Object>> createPaymentServers() {
        List<Map<String, Object>> servers = new ArrayList<>();

        Map<String, Object> server1 = new HashMap<>();
        server1.put("id", "srv-4");
        server1.put("name", "Payment-Node-1");
        server1.put("address", "10.0.2.1:8081");
        server1.put("status", "healthy");
        server1.put("connections", 89);
        servers.add(server1);

        Map<String, Object> server2 = new HashMap<>();
        server2.put("id", "srv-5");
        server2.put("name", "Payment-Node-2");
        server2.put("address", "10.0.2.2:8081");
        server2.put("status", "healthy");
        server2.put("connections", 76);
        servers.add(server2);

        return servers;
    }

    private LoadBalancer createLoadBalancer(String id, String name, String algorithm,
                                            String healthCheck, String healthCheckInterval,
                                            List<Map<String, Object>> servers, String status,
                                            int totalConnections) {
        LoadBalancer lb = new LoadBalancer();
        lb.setId(id);
        lb.setName(name);
        lb.setAlgorithm(algorithm);
        lb.setHealthCheck(healthCheck);
        lb.setHealthCheckInterval(healthCheckInterval);
        lb.setServers(servers);
        lb.setStatus(status);
        lb.setTotalConnections(totalConnections);
        return lb;
    }

    private SecurityEvent createSecurityEvent(String id, String type, String severity,
                                              String sourceIp, String endpoint, String method,
                                              String message, String timestamp) {
        SecurityEvent event = new SecurityEvent();
        event.setId(id);
        event.setType(type);
        event.setSeverity(severity);
        event.setSourceIp(sourceIp);
        event.setEndpoint(endpoint);
        event.setMethod(method);
        event.setMessage(message);
        event.setTimestamp(timestamp);
        return event;
    }

    private Map<String, Object> createRecentBlock(String ip, int count, String endpoint) {
        Map<String, Object> block = new HashMap<>();
        block.put("ip", ip);
        block.put("count", count);
        block.put("endpoint", endpoint);
        return block;
    }

    private Map<String, Object> createEventTrend(String eventType, int count, String trend) {
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("eventType", eventType);
        trendData.put("count", count);
        trendData.put("trend", trend);
        return trendData;
    }

    private Map<String, Object> createRecommendation(String text, String level) {
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("text", text);
        recommendation.put("level", level);
        recommendation.put("priority", level.equals("warning") ? "high" : "medium");
        return recommendation;
    }

    private Map<String, Object> createFinding(String type, String severity, String description, String affected) {
        Map<String, Object> finding = new HashMap<>();
        finding.put("type", type);
        finding.put("severity", severity);
        finding.put("description", description);
        finding.put("affected", affected);
        finding.put("recommendation", getFindingRecommendation(type));
        return finding;
    }

    private String getFindingRecommendation(String type) {
        switch (type) {
            case "rate_limit_misconfiguration":
                return "Configure appropriate rate limits for authentication endpoints";
            case "ip_whitelist_missing":
                return "Implement IP whitelisting for admin endpoints";
            case "api_key_exposure":
                return "Review logging configuration and mask sensitive data";
            default:
                return "Review security configuration";
        }
    }

    private SecurityAlert createSecurityAlert(String id, String type, String severity,
                                              String message, String endpoint,
                                              boolean read, String timestamp) {
        SecurityAlert alert = new SecurityAlert();
        alert.setId(id);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setEndpoint(endpoint);
        alert.setRead(read);
        alert.setTimestamp(timestamp);
        return alert;
    }
}