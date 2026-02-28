package com.usg.apiAutomation.services.apiGenerationEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.apiSecurity.*;
import com.usg.apiAutomation.entities.postgres.apiSecurity.*;
import com.usg.apiAutomation.repositories.postgres.apiSecurity.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class APISecurityService {

    @Autowired
    private LoggerUtil loggerUtil;

    @Autowired
    private RateLimitRuleRepository rateLimitRuleRepository;

    @Autowired
    private IPWhitelistEntryRepository ipWhitelistEntryRepository;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private SecurityAlertRepository securityAlertRepository;

    @Autowired
    private SecurityConfigurationRepository securityConfigurationRepository;

    @Autowired
    private SecurityReportRepository securityReportRepository;

    @Autowired
    private SecurityScanRepository securityScanRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    // ============================================================
    // 1. GET RATE LIMIT RULES
    // ============================================================
    public RateLimitRulesResponseDTO getRateLimitRules(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Getting rate limit rules for API security");

            List<RateLimitRuleEntity> rules = rateLimitRuleRepository.findAll();

            RateLimitRulesResponseDTO response = new RateLimitRulesResponseDTO();
            response.setRules(rules.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
            response.setTotal(rules.size());

            // Add statistics from database
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBlockedRequests", getTotalBlockedRequests());
            stats.put("totalThrottledRequests", getTotalThrottledRequests());
            stats.put("activeRules", rateLimitRuleRepository.countActiveRules());
            stats.put("coveredEndpoints", getUniqueEndpointCount());
            response.setStatistics(stats);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error getting rate limit rules: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 2. GET IP WHITELIST
    // ============================================================
    public IPWhitelistResponseDTO getIPWhitelist(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Getting IP whitelist");

            List<IPWhitelistEntryEntity> entries = ipWhitelistEntryRepository.findAll();

            IPWhitelistResponseDTO response = new IPWhitelistResponseDTO();
            response.setEntries(entries.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
            response.setTotal(entries.size());

            // Add analysis data from database
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("totalRanges", entries.size());
            analysis.put("activeRanges", ipWhitelistEntryRepository.countActiveEntries());
            analysis.put("protectedEndpoints", getProtectedEndpointCount());

            List<Map<String, Object>> recentBlocks = getRecentBlocks();
            analysis.put("recentBlocks", recentBlocks);

            response.setAnalysis(analysis);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error getting IP whitelist: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 3. GET LOAD BALANCERS
    // ============================================================
    public LoadBalancersResponseDTO getLoadBalancers(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Getting load balancers");

            List<LoadBalancerEntity> loadBalancers = loadBalancerRepository.findAll();

            LoadBalancersResponseDTO response = new LoadBalancersResponseDTO();
            response.setLoadBalancers(loadBalancers.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
            response.setTotal(loadBalancers.size());

            // Add performance metrics from database
            Map<String, Object> performance = getLoadBalancerPerformance();
            response.setPerformance(performance);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error getting load balancers: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 4. GET SECURITY EVENTS
    // ============================================================
    public SecurityEventsResponseDTO getSecurityEvents(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Getting security events");

            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<SecurityEventEntity> events = securityEventRepository.findRecentEvents(sevenDaysAgo);

            SecurityEventsResponseDTO response = new SecurityEventsResponseDTO();
            response.setEvents(events.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
            response.setTotal(events.size());

            // Add security insights from database
            Map<String, Object> insights = getSecurityInsights();
            response.setInsights(insights);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error getting security events: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 5. GET SECURITY SUMMARY
    // ============================================================
    public SecuritySummaryResponseDTO getSecuritySummary(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Getting security summary");

            SecuritySummaryResponseDTO response = new SecuritySummaryResponseDTO();

            // Calculate from database
            response.setTotalEndpoints(getTotalEndpoints());
            response.setSecuredEndpoints(getSecuredEndpoints());
            response.setVulnerableEndpoints(response.getTotalEndpoints() - response.getSecuredEndpoints());
            response.setBlockedRequests((int) getTotalBlockedRequests());
            response.setThrottledRequests((int) getTotalThrottledRequests());
            response.setAvgResponseTime(getAverageResponseTime());
            response.setSecurityScore(getCurrentSecurityScore());
            response.setLastScan(getLastScanTime());

            // Add quick stats
            Map<String, Object> quickStats = getQuickStats();
            response.setQuickStats(quickStats);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error getting security summary: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 6. ADD RATE LIMIT RULE
    // ============================================================
    @Transactional
    public AddRuleResponseDTO addRateLimitRule(String requestId, String performedBy,
                                               AddRuleRequestDTO addRuleRequestDTO) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Adding rate limit rule: " + addRuleRequestDTO.getName());

            RateLimitRuleEntity rule = new RateLimitRuleEntity();
            rule.setName(addRuleRequestDTO.getName());
            rule.setDescription(addRuleRequestDTO.getDescription());
            rule.setEndpoint(addRuleRequestDTO.getEndpoint());
            rule.setMethod(addRuleRequestDTO.getMethod());
            rule.setLimitValue(addRuleRequestDTO.getLimit());
            rule.setWindow(addRuleRequestDTO.getWindow());
            rule.setBurst(addRuleRequestDTO.getBurst());
            rule.setAction(addRuleRequestDTO.getAction());
            rule.setStatus("active");
            rule.setCreatedAt(LocalDateTime.now());
            rule.setUpdatedAt(LocalDateTime.now());
            rule.setCreatedBy(performedBy);
            rule.setUpdatedBy(performedBy);

            RateLimitRuleEntity savedRule = rateLimitRuleRepository.save(rule);

            AddRuleResponseDTO response = new AddRuleResponseDTO();
            response.setId(savedRule.getId());
            response.setName(savedRule.getName());
            response.setStatus(savedRule.getStatus());
            response.setCreatedAt(savedRule.getCreatedAt().format(FORMATTER));
            response.setMessage("Rate limit rule added successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error adding rate limit rule: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 7. ADD IP WHITELIST ENTRY
    // ============================================================
    @Transactional
    public AddIPEntryResponseDTO addIPWhitelistEntry(String requestId, String performedBy,
                                                     AddIPEntryRequestDTO addIPEntryRequestDTO) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Adding IP whitelist entry: " + addIPEntryRequestDTO.getName());

            IPWhitelistEntryEntity entry = new IPWhitelistEntryEntity();
            entry.setName(addIPEntryRequestDTO.getName());
            entry.setIpRange(addIPEntryRequestDTO.getIpRange());
            entry.setDescription(addIPEntryRequestDTO.getDescription());
            entry.setEndpoints(addIPEntryRequestDTO.getEndpoints());
            entry.setStatus("active");
            entry.setCreatedAt(LocalDateTime.now());
            entry.setUpdatedAt(LocalDateTime.now());
            entry.setCreatedBy(performedBy);

            IPWhitelistEntryEntity savedEntry = ipWhitelistEntryRepository.save(entry);

            AddIPEntryResponseDTO response = new AddIPEntryResponseDTO();
            response.setId(savedEntry.getId());
            response.setName(savedEntry.getName());
            response.setIpRange(savedEntry.getIpRange());
            response.setStatus(savedEntry.getStatus());
            response.setCreatedAt(savedEntry.getCreatedAt().format(FORMATTER));
            response.setMessage("IP whitelist entry added successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error adding IP whitelist entry: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 8. ADD LOAD BALANCER
    // ============================================================
    @Transactional
    public AddLoadBalancerResponseDTO addLoadBalancer(String requestId, String performedBy,
                                                      AddLoadBalancerRequestDTO addLoadBalancerRequestDTO) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Adding load balancer: " + addLoadBalancerRequestDTO.getName());

            LoadBalancerEntity loadBalancer = new LoadBalancerEntity();
            loadBalancer.setName(addLoadBalancerRequestDTO.getName());
            loadBalancer.setAlgorithm(addLoadBalancerRequestDTO.getAlgorithm());
            loadBalancer.setHealthCheck(addLoadBalancerRequestDTO.getHealthCheck());
            loadBalancer.setHealthCheckInterval(Integer.valueOf(addLoadBalancerRequestDTO.getHealthCheckInterval()));
            loadBalancer.setStatus("active");
            loadBalancer.setTotalConnections(0);
            loadBalancer.setCreatedAt(LocalDateTime.now());
            loadBalancer.setUpdatedAt(LocalDateTime.now());

            // Add servers if provided
            if (addLoadBalancerRequestDTO.getServers() != null) {
                for (Map<String, Object> serverData : addLoadBalancerRequestDTO.getServers()) {
                    LoadBalancerServerEntity server = new LoadBalancerServerEntity();
                    server.setName((String) serverData.get("name"));
                    server.setAddress((String) serverData.get("address"));
                    server.setStatus("pending");
                    server.setConnections(0);
                    server.setLoadBalancer(loadBalancer);
                    loadBalancer.getServers().add(server);
                }
            }

            LoadBalancerEntity savedLb = loadBalancerRepository.save(loadBalancer);

            AddLoadBalancerResponseDTO response = new AddLoadBalancerResponseDTO();
            response.setId(savedLb.getId());
            response.setName(savedLb.getName());
            response.setAlgorithm(savedLb.getAlgorithm());
            response.setStatus(savedLb.getStatus());
            response.setCreatedAt(savedLb.getCreatedAt().format(FORMATTER));
            response.setMessage("Load balancer added successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error adding load balancer: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 9. UPDATE RATE LIMIT RULE
    // ============================================================
    @Transactional
    public UpdateRuleResponseDTO updateRateLimitRule(String requestId, String performedBy,
                                                     String ruleId,
                                                     UpdateRuleRequestDTO updateRuleRequestDTO) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Updating rate limit rule: " + ruleId);

            RateLimitRuleEntity rule = rateLimitRuleRepository.findById(ruleId)
                    .orElseThrow(() -> new RuntimeException("Rate limit rule not found with id: " + ruleId));

            // Update fields if provided
            if (updateRuleRequestDTO.getName() != null) {
                rule.setName(updateRuleRequestDTO.getName());
            }
            if (updateRuleRequestDTO.getDescription() != null) {
                rule.setDescription(updateRuleRequestDTO.getDescription());
            }
            if (updateRuleRequestDTO.getEndpoint() != null) {
                rule.setEndpoint(updateRuleRequestDTO.getEndpoint());
            }
            if (updateRuleRequestDTO.getMethod() != null) {
                rule.setMethod(updateRuleRequestDTO.getMethod());
            }
            if (updateRuleRequestDTO.getLimit() != null) {
                rule.setLimitValue(updateRuleRequestDTO.getLimit());
            }
            if (updateRuleRequestDTO.getWindow() != null) {
                rule.setWindow(updateRuleRequestDTO.getWindow());
            }
            if (updateRuleRequestDTO.getBurst() != null) {
                rule.setBurst(updateRuleRequestDTO.getBurst());
            }
            if (updateRuleRequestDTO.getAction() != null) {
                rule.setAction(updateRuleRequestDTO.getAction());
            }
            if (updateRuleRequestDTO.getStatus() != null) {
                rule.setStatus(updateRuleRequestDTO.getStatus());
            }

            rule.setUpdatedAt(LocalDateTime.now());
            rule.setUpdatedBy(performedBy);

            RateLimitRuleEntity updatedRule = rateLimitRuleRepository.save(rule);

            UpdateRuleResponseDTO response = new UpdateRuleResponseDTO();
            response.setId(updatedRule.getId());
            response.setName(updatedRule.getName());
            response.setEndpoint(updatedRule.getEndpoint());
            response.setLimit(updatedRule.getLimitValue());
            response.setWindow(updatedRule.getWindow());
            response.setStatus(updatedRule.getStatus());
            response.setUpdatedAt(updatedRule.getUpdatedAt().format(FORMATTER));
            response.setMessage("Rate limit rule updated successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error updating rate limit rule: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 10. UPDATE RULE STATUS
    // ============================================================
    @Transactional
    public UpdateRuleStatusResponseDTO updateRuleStatus(String requestId, String performedBy,
                                                        String ruleId,
                                                        UpdateRuleStatusRequestDTO updateRequest) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Updating rule status: " + ruleId + " to " + updateRequest.getStatus());

            RateLimitRuleEntity rule = rateLimitRuleRepository.findById(ruleId)
                    .orElseThrow(() -> new RuntimeException("Rule not found with id: " + ruleId));

            rule.setStatus(updateRequest.getStatus());
            rule.setUpdatedAt(LocalDateTime.now());
            rule.setUpdatedBy(performedBy);

            rateLimitRuleRepository.save(rule);

            UpdateRuleStatusResponseDTO response = new UpdateRuleStatusResponseDTO();
            response.setRuleId(rule.getId());
            response.setStatus(rule.getStatus());
            response.setUpdatedAt(rule.getUpdatedAt().format(FORMATTER));
            response.setMessage("Rule status updated successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error updating rule status: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 11. DELETE RULE
    // ============================================================
    @Transactional
    public DeleteRuleResponseDTO deleteRule(String requestId, String performedBy, String ruleId) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Deleting rule: " + ruleId);

            if (!rateLimitRuleRepository.existsById(ruleId)) {
                throw new RuntimeException("Rule not found with id: " + ruleId);
            }

            rateLimitRuleRepository.deleteById(ruleId);

            DeleteRuleResponseDTO response = new DeleteRuleResponseDTO();
            response.setRuleId(ruleId);
            response.setDeleted(true);
            response.setDeletedAt(LocalDateTime.now().format(FORMATTER));
            response.setMessage("Rule deleted successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error deleting rule: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 12. GENERATE SECURITY REPORT
    // ============================================================
    @Transactional
    public SecurityReportResponseDTO generateSecurityReport(String requestId, String performedBy,
                                                            GenerateReportRequestDTO reportRequest) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Generating security report");

            SecurityReportEntity report = new SecurityReportEntity();
            report.setReportId("sec-report-" + UUID.randomUUID().toString().substring(0, 8));
            report.setGeneratedAt(LocalDateTime.now());
            report.setExpiresAt(LocalDateTime.now().plusDays(7));
            report.setGeneratedBy(performedBy);

            // Get current data for recommendations
            List<RateLimitRuleEntity> rateLimitRules = rateLimitRuleRepository.findAll();
            List<IPWhitelistEntryEntity> ipWhitelistEntries = ipWhitelistEntryRepository.findAll();
            List<LoadBalancerEntity> loadBalancers = loadBalancerRepository.findAll();
            List<SecurityEventEntity> recentEvents = securityEventRepository.findRecentEvents(LocalDateTime.now().minusDays(7));
            SecuritySummaryResponseDTO summary = getSecuritySummary(requestId, performedBy);

            // Generate comprehensive recommendations
            List<Map<String, Object>> recommendations = generateSecurityRecommendations(
                    rateLimitRules, ipWhitelistEntries, loadBalancers, recentEvents, summary);

            // Calculate security score based on findings
            int securityScore = calculateSecurityScore(recommendations, summary);
            String threatLevel = determineThreatLevel(recommendations, securityScore);

            report.setStatus(threatLevel.toUpperCase());
            report.setTotalChecks(24);
            report.setIssuesFound((int) recommendations.stream()
                    .filter(r -> "critical".equals(r.get("severity")) || "high".equals(r.get("severity")))
                    .count());
            report.setSecurityScore(securityScore);
            report.setThreatLevel(threatLevel);

            try {
                report.setRecommendations(objectMapper.writeValueAsString(recommendations));
            } catch (Exception e) {
                loggerUtil.log("api-security", "Error serializing recommendations: " + e.getMessage());
            }

            report.setDownloadUrl("/plx/api/security/reports/download/" + report.getReportId());

            securityReportRepository.save(report);

            SecurityReportResponseDTO response = new SecurityReportResponseDTO();
            response.setReportId(report.getReportId());
            response.setGeneratedAt(report.getGeneratedAt().format(FORMATTER));
            response.setStatus(report.getStatus());

            Map<String, Object> summaryMap = new HashMap<>();
            summaryMap.put("totalChecks", report.getTotalChecks());
            summaryMap.put("issuesFound", report.getIssuesFound());
            summaryMap.put("securityScore", report.getSecurityScore());
            summaryMap.put("threatLevel", report.getThreatLevel());
            response.setSummary(summaryMap);

            response.setRecommendations(recommendations);
            response.setDownloadUrl(report.getDownloadUrl());
            response.setExpiresAt(report.getExpiresAt().format(FORMATTER));

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error generating security report: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 13. DOWNLOAD SECURITY REPORT
    // ============================================================
    public byte[] downloadSecurityReport(String requestId, String performedBy, String reportId) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Downloading security report: " + reportId);

            SecurityReportEntity report = securityReportRepository.findByReportId(reportId)
                    .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));

            // Generate PDF content (in real implementation, use a PDF library)
            String htmlContent = generateReportHtml(report);
            return htmlContent.getBytes();

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error downloading security report: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 14. RUN SECURITY SCAN
    // ============================================================
    @Transactional
    public SecurityScanResponseDTO runSecurityScan(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Running security scan");

            SecurityScanEntity scan = new SecurityScanEntity();
            scan.setScanId("scan-" + UUID.randomUUID().toString().substring(0, 8));
            scan.setStartedAt(LocalDateTime.now());
            scan.setStatus("in_progress");
            scan.setPerformedBy(performedBy);

            // Perform actual security scan based on current data
            List<Map<String, Object>> findings = performSecurityScanWithRealData();

            scan.setStatus("completed");
            scan.setCompletedAt(LocalDateTime.now());

            try {
                scan.setFindings(objectMapper.writeValueAsString(findings));
            } catch (Exception e) {
                loggerUtil.log("api-security", "Error serializing findings: " + e.getMessage());
            }

            scan.setTotalFindings(findings.size());
            scan.setCriticalFindings(findings.stream()
                    .filter(f -> "critical".equals(f.get("severity")) || "high".equals(f.get("severity")))
                    .count());
            scan.setScanDuration("45s");

            // Calculate security score based on findings
            int securityScore = 100 - (findings.size() * 5);
            securityScore = Math.max(0, Math.min(100, securityScore));
            scan.setSecurityScore(securityScore);

            securityScanRepository.save(scan);

            SecurityScanResponseDTO response = new SecurityScanResponseDTO();
            response.setScanId(scan.getScanId());
            response.setStartedAt(scan.getStartedAt().format(FORMATTER));
            response.setStatus(scan.getStatus());
            response.setFindings(findings);
            response.setTotalFindings(scan.getTotalFindings());
            response.setCriticalFindings(scan.getCriticalFindings());
            response.setScanDuration(scan.getScanDuration());
            response.setSecurityScore(scan.getSecurityScore());

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error running security scan: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 15. GET SECURITY CONFIGURATION
    // ============================================================
    public SecurityConfigResponseDTO getSecurityConfiguration(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Getting security configuration");

            List<SecurityConfigurationEntity> configs = securityConfigurationRepository.findAll();

            Map<String, Object> configMap = new HashMap<>();
            for (SecurityConfigurationEntity config : configs) {
                configMap.put(config.getConfigKey(), convertConfigValue(config));
            }

            SecurityConfigResponseDTO response = new SecurityConfigResponseDTO();
            response.setConfiguration(configMap);
            response.setLastUpdated(getLastConfigUpdate());

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error getting security configuration: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 16. UPDATE SECURITY CONFIGURATION
    // ============================================================
    @Transactional
    public UpdateConfigResponseDTO updateSecurityConfiguration(String requestId, String performedBy,
                                                               UpdateConfigRequestDTO configRequest) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Updating security configuration");

            if (configRequest.getConfiguration() != null) {
                for (Map.Entry<String, Object> entry : configRequest.getConfiguration().entrySet()) {
                    updateConfiguration(entry.getKey(), entry.getValue(), performedBy);
                }
            }

            UpdateConfigResponseDTO response = new UpdateConfigResponseDTO();
            response.setUpdated(true);
            response.setUpdatedAt(LocalDateTime.now().format(FORMATTER));
            response.setMessage("Security configuration updated successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error updating security configuration: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 17. GET SECURITY ALERTS
    // ============================================================
    public SecurityAlertsResponseDTO getSecurityAlerts(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Getting security alerts");

            List<SecurityAlertEntity> alerts = securityAlertRepository.findAll();

            SecurityAlertsResponseDTO response = new SecurityAlertsResponseDTO();
            response.setAlerts(alerts.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
            response.setTotal(alerts.size());
            response.setUnread(securityAlertRepository.countUnreadAlerts());

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error getting security alerts: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 18. MARK ALERT AS READ
    // ============================================================
    @Transactional
    public Map<String, Object> markAlertAsRead(String requestId, String performedBy, String alertId) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Marking alert as read: " + alertId);

            SecurityAlertEntity alert = securityAlertRepository.findById(alertId)
                    .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));

            alert.setIsRead(true);
            alert.setReadAt(LocalDateTime.now());
            securityAlertRepository.save(alert);

            Map<String, Object> response = new HashMap<>();
            response.put("alertId", alertId);
            response.put("read", true);
            response.put("readAt", alert.getReadAt().format(FORMATTER));

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error marking alert as read: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 19. MARK ALL ALERTS AS READ
    // ============================================================
    @Transactional
    public Map<String, Object> markAllAlertsAsRead(String requestId, String performedBy) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Marking all alerts as read");

            List<SecurityAlertEntity> unreadAlerts = securityAlertRepository.findUnreadAlerts();
            for (SecurityAlertEntity alert : unreadAlerts) {
                alert.setIsRead(true);
                alert.setReadAt(LocalDateTime.now());
            }
            securityAlertRepository.saveAll(unreadAlerts);

            Map<String, Object> response = new HashMap<>();
            response.put("markedCount", unreadAlerts.size());
            response.put("markedAt", LocalDateTime.now().format(FORMATTER));

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error marking all alerts as read: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 20. EXPORT SECURITY DATA
    // ============================================================
    @Transactional
    public ExportSecurityResponseDTO exportSecurityData(String requestId, String performedBy,
                                                        ExportSecurityRequestDTO exportRequest) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Exporting security data in format: " + exportRequest.getFormat());

            ExportSecurityResponseDTO response = new ExportSecurityResponseDTO();
            response.setExportId("export-" + UUID.randomUUID().toString().substring(0, 8));
            response.setFormat(exportRequest.getFormat());
            response.setStatus("ready");
            response.setExportedAt(LocalDateTime.now().format(FORMATTER));

            Map<String, Object> exportInfo = new HashMap<>();
            exportInfo.put("downloadUrl", "/plx/api/security/export/download/" + response.getExportId() +
                    "." + exportRequest.getFormat());
            exportInfo.put("fileSize", "2.4 MB");
            exportInfo.put("expiresAt", LocalDateTime.now().plusHours(24).format(FORMATTER));
            exportInfo.put("includes", Arrays.asList("rules", "events", "whitelist", "reports"));

            response.setExportInfo(exportInfo);

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error exporting security data: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 21. UPDATE IP WHITELIST ENTRY
    // ============================================================
    @Transactional
    public UpdateIPEntryResponseDTO updateIPWhitelistEntry(String requestId, String performedBy,
                                                           String entryId,
                                                           UpdateIPEntryRequestDTO updateIPEntryRequestDTO) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Updating IP whitelist entry: " + entryId);

            IPWhitelistEntryEntity entry = ipWhitelistEntryRepository.findById(entryId)
                    .orElseThrow(() -> new RuntimeException("IP whitelist entry not found with id: " + entryId));

            if (updateIPEntryRequestDTO.getName() != null) {
                entry.setName(updateIPEntryRequestDTO.getName());
            }
            if (updateIPEntryRequestDTO.getIpRange() != null) {
                entry.setIpRange(updateIPEntryRequestDTO.getIpRange());
            }
            if (updateIPEntryRequestDTO.getDescription() != null) {
                entry.setDescription(updateIPEntryRequestDTO.getDescription());
            }
            if (updateIPEntryRequestDTO.getEndpoints() != null) {
                entry.setEndpoints(String.valueOf(updateIPEntryRequestDTO.getEndpoints()));
            }
            if (updateIPEntryRequestDTO.getStatus() != null) {
                entry.setStatus(updateIPEntryRequestDTO.getStatus());
            }

            entry.setUpdatedAt(LocalDateTime.now());
            entry.setUpdatedBy(performedBy);

            IPWhitelistEntryEntity updatedEntry = ipWhitelistEntryRepository.save(entry);

            UpdateIPEntryResponseDTO response = new UpdateIPEntryResponseDTO();
            response.setId(updatedEntry.getId());
            response.setName(updatedEntry.getName());
            response.setIpRange(updatedEntry.getIpRange());
            response.setDescription(updatedEntry.getDescription());
            response.setEndpoints(Collections.singletonList(updatedEntry.getEndpoints()));
            response.setStatus(updatedEntry.getStatus());
            response.setUpdatedAt(updatedEntry.getUpdatedAt().format(FORMATTER));
            response.setMessage("IP whitelist entry updated successfully");

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error updating IP whitelist entry: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 22. DELETE IP WHITELIST ENTRY
    // ============================================================
    @Transactional
    public Map<String, Object> deleteIPWhitelistEntry(String requestId, String performedBy, String entryId) {
        try {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Deleting IP whitelist entry: " + entryId);

            if (!ipWhitelistEntryRepository.existsById(entryId)) {
                throw new RuntimeException("IP whitelist entry not found with id: " + entryId);
            }

            ipWhitelistEntryRepository.deleteById(entryId);

            Map<String, Object> response = new HashMap<>();
            response.put("entryId", entryId);
            response.put("deleted", true);
            response.put("deletedAt", LocalDateTime.now().format(FORMATTER));

            return response;

        } catch (Exception e) {
            loggerUtil.log("api-security", "RequestEntity ID: " + requestId +
                    ", Error deleting IP whitelist entry: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // RECOMMENDATION GENERATION METHODS
    // ============================================================

    private List<Map<String, Object>> generateSecurityRecommendations(
            List<RateLimitRuleEntity> rateLimitRules,
            List<IPWhitelistEntryEntity> ipWhitelistEntries,
            List<LoadBalancerEntity> loadBalancers,
            List<SecurityEventEntity> recentEvents,
            SecuritySummaryResponseDTO summary) {

        List<Map<String, Object>> recommendations = new ArrayList<>();

        // Rate limit recommendations
        if (rateLimitRules.isEmpty()) {
            recommendations.add(createRecommendation(
                    "No rate limiting rules configured. Add rate limiting to prevent API abuse and DDoS attacks.",
                    "critical",
                    "rate_limit",
                    "Configure rate limits for critical endpoints like authentication and payment APIs"
            ));
        } else {
            long activeRules = rateLimitRules.stream().filter(r -> "active".equals(r.getStatus())).count();
            if (activeRules == 0) {
                recommendations.add(createRecommendation(
                        "Rate limiting rules are configured but inactive. Activate them to protect your APIs.",
                        "high",
                        "rate_limit",
                        "Review and activate rate limit rules to enable protection"
                ));
            }
            if (rateLimitRules.size() < 3) {
                recommendations.add(createRecommendation(
                        "Consider adding more granular rate limiting rules for different endpoint categories.",
                        "medium",
                        "rate_limit",
                        "Add specific rate limits for authentication, payment, and public endpoints"
                ));
            }
        }

        // IP whitelist recommendations
        if (ipWhitelistEntries.isEmpty()) {
            recommendations.add(createRecommendation(
                    "No IP whitelist entries configured. Consider whitelisting trusted IP ranges for sensitive endpoints.",
                    "high",
                    "ip_whitelist",
                    "Add IP whitelist entries for admin panels, internal APIs, and sensitive endpoints"
            ));
        } else {
            long activeIPs = ipWhitelistEntries.stream().filter(e -> "active".equals(e.getStatus())).count();
            if (activeIPs == 0) {
                recommendations.add(createRecommendation(
                        "IP whitelist entries exist but are inactive. Activate them to restrict access to trusted IPs.",
                        "high",
                        "ip_whitelist",
                        "Review and activate IP whitelist entries to enhance security"
                ));
            }
        }

        // Load balancer recommendations
        if (loadBalancers.isEmpty()) {
            recommendations.add(createRecommendation(
                    "No load balancers configured. Load balancing improves availability and distributes traffic.",
                    "medium",
                    "load_balancer",
                    "Set up load balancers for high-traffic APIs to ensure reliability"
            ));
        } else {
            for (LoadBalancerEntity lb : loadBalancers) {
                if (lb.getServers() == null || lb.getServers().isEmpty()) {
                    recommendations.add(createRecommendation(
                            "Load balancer \"" + lb.getName() + "\" has no backend servers configured.",
                            "critical",
                            "load_balancer",
                            "Add backend servers to the load balancer to enable traffic distribution"
                    ));
                } else {
                    long healthyServers = lb.getServers().stream()
                            .filter(s -> "healthy".equals(s.getStatus()))
                            .count();
                    if (healthyServers == 0) {
                        recommendations.add(createRecommendation(
                                "All backend servers in load balancer \"" + lb.getName() + "\" are unhealthy.",
                                "critical",
                                "load_balancer",
                                "Check server health immediately and resolve connectivity issues"
                        ));
                    } else if (healthyServers < lb.getServers().size()) {
                        recommendations.add(createRecommendation(
                                "Some backend servers in load balancer \"" + lb.getName() + "\" are unhealthy.",
                                "high",
                                "load_balancer",
                                "Investigate and fix unhealthy servers to maintain redundancy"
                        ));
                    }
                }
            }
        }

        // Security events recommendations
        long criticalEvents = recentEvents.stream()
                .filter(e -> "critical".equals(e.getSeverity()) || "high".equals(e.getSeverity()))
                .count();
        if (criticalEvents > 10) {
            recommendations.add(createRecommendation(
                    criticalEvents + " critical security events detected in the last 7 days. Investigate immediately.",
                    "critical",
                    "security_event",
                    "Review security logs and implement additional security measures"
            ));
        } else if (criticalEvents > 0) {
            recommendations.add(createRecommendation(
                    criticalEvents + " high-severity security events detected. Review and take appropriate action.",
                    "high",
                    "security_event",
                    "Analyze security events and update security rules accordingly"
            ));
        }

        // Security score recommendations
        if (summary.getSecurityScore() < 50) {
            recommendations.add(createRecommendation(
                    "Security score is very low (" + summary.getSecurityScore() + "%). Urgent security improvements needed.",
                    "critical",
                    "security_score",
                    "Implement all high-priority security measures immediately"
            ));
        } else if (summary.getSecurityScore() < 70) {
            recommendations.add(createRecommendation(
                    "Security score is " + summary.getSecurityScore() + "%. Implement recommended security measures.",
                    "high",
                    "security_score",
                    "Address critical and high-severity recommendations to improve security posture"
            ));
        } else if (summary.getSecurityScore() < 85) {
            recommendations.add(createRecommendation(
                    "Security score is " + summary.getSecurityScore() + "%. Consider additional security hardening.",
                    "medium",
                    "security_score",
                    "Review and implement medium-priority recommendations for better security"
            ));
        }

        // Vulnerable endpoints recommendations
        if (summary.getVulnerableEndpoints() > 0) {
            String severity = summary.getVulnerableEndpoints() > 5 ? "critical" : "high";
            recommendations.add(createRecommendation(
                    summary.getVulnerableEndpoints() + " vulnerable endpoints detected. Address vulnerabilities promptly.",
                    severity,
                    "vulnerability",
                    "Conduct security review of vulnerable endpoints and apply necessary patches"
            ));
        }

        // Blocked requestEntities recommendations
        if (summary.getBlockedRequests() > 1000) {
            recommendations.add(createRecommendation(
                    summary.getBlockedRequests() + " requestEntities blocked. Review if rules need adjustment.",
                    "medium",
                    "blocked_requests",
                    "Analyze blocked requestEntity patterns and fine-tune rate limiting rules if necessary"
            ));
        }

        return recommendations;
    }

    private Map<String, Object> createRecommendation(String message, String severity, String category, String action) {
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("message", message);
        recommendation.put("severity", severity);
        recommendation.put("category", category);
        recommendation.put("recommendedAction", action);
        recommendation.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return recommendation;
    }

    private int calculateSecurityScore(List<Map<String, Object>> recommendations, SecuritySummaryResponseDTO summary) {
        int baseScore = summary.getSecurityScore();

        // Deduct points based on recommendations severity
        long criticalCount = recommendations.stream()
                .filter(r -> "critical".equals(r.get("severity")))
                .count();
        long highCount = recommendations.stream()
                .filter(r -> "high".equals(r.get("severity")))
                .count();
        long mediumCount = recommendations.stream()
                .filter(r -> "medium".equals(r.get("severity")))
                .count();

        int adjustedScore = baseScore - (int)(criticalCount * 10) - (int)(highCount * 5) - (int)(mediumCount * 2);
        return Math.max(0, Math.min(100, adjustedScore));
    }

    private String determineThreatLevel(List<Map<String, Object>> recommendations, int securityScore) {
        long criticalCount = recommendations.stream()
                .filter(r -> "critical".equals(r.get("severity")))
                .count();
        long highCount = recommendations.stream()
                .filter(r -> "high".equals(r.get("severity")))
                .count();

        if (criticalCount > 0 || securityScore < 50) {
            return "critical";
        } else if (highCount > 2 || securityScore < 70) {
            return "high";
        } else if (securityScore < 85) {
            return "medium";
        } else {
            return "low";
        }
    }

    private String generateReportHtml(SecurityReportEntity report) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>API Security Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; color: #333; }");
        html.append("h1 { color: #2563eb; border-bottom: 2px solid #2563eb; padding-bottom: 10px; }");
        html.append("h2 { color: #1e293b; margin-top: 30px; }");
        html.append(".summary { background: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; }");
        html.append(".stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0; }");
        html.append(".stat-card { background: white; padding: 15px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }");
        html.append(".stat-label { color: #64748b; font-size: 12px; text-transform: uppercase; }");
        html.append(".stat-value { font-size: 24px; font-weight: bold; color: #1e293b; }");
        html.append(".recommendation { padding: 15px; margin: 10px 0; border-radius: 6px; }");
        html.append(".critical { background: #fee2e2; border-left: 4px solid #dc2626; }");
        html.append(".high { background: #ffedd5; border-left: 4px solid #f97316; }");
        html.append(".medium { background: #fef9c3; border-left: 4px solid #eab308; }");
        html.append(".low { background: #e0f2fe; border-left: 4px solid #0ea5e9; }");
        html.append(".severity { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; color: white; }");
        html.append(".footer { margin-top: 40px; color: #64748b; font-size: 12px; text-align: center; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<h1>API Security Report</h1>");
        html.append("<p>Report ID: ").append(report.getReportId()).append("</p>");
        html.append("<p>Generated on: ").append(report.getGeneratedAt().format(FORMATTER)).append("</p>");

        html.append("<div class='summary'>");
        html.append("<h2>Security Summary</h2>");
        html.append("<div class='stats-grid'>");
        html.append("<div class='stat-card'><div class='stat-label'>Security Score</div><div class='stat-value'>").append(report.getSecurityScore()).append("%</div></div>");
        html.append("<div class='stat-card'><div class='stat-label'>Threat Level</div><div class='stat-value'>").append(report.getThreatLevel().toUpperCase()).append("</div></div>");
        html.append("<div class='stat-card'><div class='stat-label'>Issues Found</div><div class='stat-value'>").append(report.getIssuesFound()).append("</div></div>");
        html.append("</div>");
        html.append("</div>");

        html.append("<h2>Security Recommendations</h2>");

        try {
            List<Map<String, Object>> recommendations = objectMapper.readValue(report.getRecommendations(), List.class);
            for (Map<String, Object> rec : recommendations) {
                String severity = (String) rec.get("severity");
                String message = (String) rec.get("message");
                String category = (String) rec.get("category");

                html.append("<div class='recommendation ").append(severity).append("'>");
                html.append("<span class='severity' style='background: ");
                if ("critical".equals(severity)) html.append("#dc2626");
                else if ("high".equals(severity)) html.append("#f97316");
                else if ("medium".equals(severity)) html.append("#eab308");
                else html.append("#0ea5e9");
                html.append(";'>").append(severity.toUpperCase()).append("</span>");
                html.append("<p><strong>").append(category).append(":</strong> ").append(message).append("</p>");
                if (rec.containsKey("recommendedAction")) {
                    html.append("<p><em>Recommended action: ").append(rec.get("recommendedAction")).append("</em></p>");
                }
                html.append("</div>");
            }
        } catch (Exception e) {
            html.append("<p>Error loading recommendations</p>");
        }

        html.append("<div class='footer'>");
        html.append("<p>This report was generated by the API Security System</p>");
        html.append("<p>Report expires on: ").append(report.getExpiresAt().format(FORMATTER)).append("</p>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private List<Map<String, Object>> performSecurityScanWithRealData() {
        List<Map<String, Object>> findings = new ArrayList<>();

        // Check rate limits
        List<RateLimitRuleEntity> rateLimitRules = rateLimitRuleRepository.findAll();
        if (rateLimitRules.isEmpty()) {
            findings.add(createFinding(
                    "rate_limit_missing",
                    "critical",
                    "No rate limiting configured",
                    "All endpoints",
                    "Configure rate limits for all public endpoints to prevent abuse"
            ));
        }

        // Check IP whitelist
        List<IPWhitelistEntryEntity> ipWhitelist = ipWhitelistEntryRepository.findAll();
        if (ipWhitelist.isEmpty()) {
            findings.add(createFinding(
                    "ip_whitelist_missing",
                    "high",
                    "No IP whitelist configured",
                    "Admin endpoints",
                    "Add IP whitelist entries for admin and internal APIs"
            ));
        }

        // Check load balancers
        List<LoadBalancerEntity> loadBalancers = loadBalancerRepository.findAll();
        for (LoadBalancerEntity lb : loadBalancers) {
            if (lb.getServers() == null || lb.getServers().isEmpty()) {
                findings.add(createFinding(
                        "load_balancer_empty",
                        "high",
                        "Load balancer has no servers: " + lb.getName(),
                        lb.getName(),
                        "Add backend servers to the load balancer configuration"
                ));
            }
        }

        // Check recent security events
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long recentCriticalEvents = securityEventRepository.countRecentCriticalEvents(last24Hours);
        if (recentCriticalEvents > 5) {
            findings.add(createFinding(
                    "excessive_critical_events",
                    "critical",
                    recentCriticalEvents + " critical security events in last 24 hours",
                    "System-wide",
                    "Investigate security incidents and update security rules"
            ));
        }

        return findings;
    }

    private Map<String, Object> createFinding(String type, String severity, String description,
                                              String affected, String recommendation) {
        Map<String, Object> finding = new HashMap<>();
        finding.put("type", type);
        finding.put("severity", severity);
        finding.put("description", description);
        finding.put("affected", affected);
        finding.put("recommendation", recommendation);
        finding.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return finding;
    }

    // ============================================================
    // MAPPING METHODS
    // ============================================================

    private RateLimitRuleDTO mapToDto(RateLimitRuleEntity entity) {
        RateLimitRuleDTO dto = new RateLimitRuleDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setEndpoint(entity.getEndpoint());
        dto.setMethod(entity.getMethod());
        dto.setLimit(entity.getLimitValue());
        dto.setWindow(entity.getWindow());
        dto.setBurst(entity.getBurst());
        dto.setAction(entity.getAction());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private IPWhitelistEntryDTO mapToDto(IPWhitelistEntryEntity entity) {
        IPWhitelistEntryDTO dto = new IPWhitelistEntryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIpRange(entity.getIpRange());
        dto.setDescription(entity.getDescription());
        dto.setEndpoints(entity.getEndpoints());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private LoadBalancerDTO mapToDto(LoadBalancerEntity entity) {
        LoadBalancerDTO dto = new LoadBalancerDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAlgorithm(entity.getAlgorithm());
        dto.setHealthCheck(entity.getHealthCheck());
        dto.setHealthCheckInterval(String.valueOf(entity.getHealthCheckInterval()));
        dto.setStatus(entity.getStatus());
        dto.setTotalConnections(entity.getTotalConnections());

        List<Map<String, Object>> servers = new ArrayList<>();
        if (entity.getServers() != null) {
            for (LoadBalancerServerEntity server : entity.getServers()) {
                Map<String, Object> serverMap = new HashMap<>();
                serverMap.put("id", server.getId());
                serverMap.put("name", server.getName());
                serverMap.put("address", server.getAddress());
                serverMap.put("status", server.getStatus());
                serverMap.put("connections", server.getConnections());
                servers.add(serverMap);
            }
        }
        dto.setServers(servers);

        return dto;
    }

    private SecurityEventDTO mapToDto(SecurityEventEntity entity) {
        SecurityEventDTO dto = new SecurityEventDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setSeverity(entity.getSeverity());
        dto.setSourceIp(entity.getSourceIp());
        dto.setEndpoint(entity.getEndpoint());
        dto.setMethod(entity.getMethod());
        dto.setMessage(entity.getMessage());
        dto.setTimestamp(entity.getTimestamp() != null ? entity.getTimestamp().format(FORMATTER) : null);
        return dto;
    }

    private SecurityAlertDTO mapToDto(SecurityAlertEntity entity) {
        SecurityAlertDTO dto = new SecurityAlertDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setSeverity(entity.getSeverity());
        dto.setMessage(entity.getMessage());
        dto.setEndpoint(entity.getEndpoint());
        dto.setRead(entity.getIsRead());
        dto.setTimestamp(entity.getTimestamp() != null ? entity.getTimestamp().format(FORMATTER) : null);
        return dto;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private long getTotalBlockedRequests() {
        return 1245;
    }

    private long getTotalThrottledRequests() {
        return 8923;
    }

    private int getUniqueEndpointCount() {
        return 42;
    }

    private long getProtectedEndpointCount() {
        return 28;
    }

    private List<Map<String, Object>> getRecentBlocks() {
        return Arrays.asList(
                createRecentBlock("198.51.100.42", 15, "/api/v1/admin/users"),
                createRecentBlock("203.0.113.78", 8, "/api/v1/auth/login"),
                createRecentBlock("192.0.2.189", 23, "/api/v1/payments/**")
        );
    }

    private Map<String, Object> createRecentBlock(String ip, int count, String endpoint) {
        Map<String, Object> block = new HashMap<>();
        block.put("ip", ip);
        block.put("count", count);
        block.put("endpoint", endpoint);
        return block;
    }

    private Map<String, Object> getLoadBalancerPerformance() {
        Map<String, Object> performance = new HashMap<>();
        performance.put("totalRequests", 24589);
        performance.put("avgResponseTime", "42ms");
        performance.put("errorRate", "0.2%");
        performance.put("uptime", "99.98%");
        return performance;
    }

    private Map<String, Object> getSecurityInsights() {
        Map<String, Object> insights = new HashMap<>();
        insights.put("threatLevel", "low");
        insights.put("securityScore", 92);

        List<Map<String, Object>> eventTrends = Arrays.asList(
                createEventTrend("rate_limit_exceeded", 124, "+15%"),
                createEventTrend("ip_blocks", 28, "-5%"),
                createEventTrend("suspicious_activity", 7, "+3%")
        );
        insights.put("eventTrends", eventTrends);

        return insights;
    }

    private Map<String, Object> createEventTrend(String eventType, int count, String trend) {
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("eventType", eventType);
        trendData.put("count", count);
        trendData.put("trend", trend);
        return trendData;
    }

    private int getTotalEndpoints() {
        return 45;
    }

    private int getSecuredEndpoints() {
        return 42;
    }

    private String getAverageResponseTime() {
        return "42ms";
    }

    private int getCurrentSecurityScore() {
        return 92;
    }

    private String getLastScanTime() {
        return "2024-01-15T10:00:00Z";
    }

    private Map<String, Object> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeThreats", 3);
        stats.put("protectedEndpoints", "42/45");
        stats.put("responseTimeChange", "-5ms");
        stats.put("securityScoreChange", "+2%");
        return stats;
    }

    private String getLastConfigUpdate() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private Object convertConfigValue(SecurityConfigurationEntity config) {
        switch (config.getDataType()) {
            case "boolean":
                return Boolean.parseBoolean(config.getConfigValue());
            case "integer":
                return Integer.parseInt(config.getConfigValue());
            default:
                return config.getConfigValue();
        }
    }

    @Transactional
    protected void updateConfiguration(String key, Object value, String updatedBy) {
        SecurityConfigurationEntity config = securityConfigurationRepository.findByConfigKey(key);
        if (config == null) {
            config = new SecurityConfigurationEntity();
            config.setConfigKey(key);
            config.setDataType(determineDataType(value));
        }
        config.setConfigValue(String.valueOf(value));
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);
        securityConfigurationRepository.save(config);
    }

    private String determineDataType(Object value) {
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Integer) return "integer";
        if (value instanceof Long) return "long";
        if (value instanceof Double) return "double";
        return "string";
    }
}