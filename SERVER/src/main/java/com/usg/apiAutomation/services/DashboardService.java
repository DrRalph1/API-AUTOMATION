package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.dashboard.*;
import com.usg.apiAutomation.dtos.userManagement.SearchUsersRequestDTO;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DashboardService {

    private final LoggerUtil loggerUtil;

    // Inject all services
    private final APISecurityService apiSecurityService;
    private final CodeBaseService codeBaseService;
    private final CollectionsService collectionsService;
    private final DocumentationService documentationService;
    private final UserManagementService userManagementService;

    @PostConstruct
    public void init() {
        log.info("DashboardService initialized with all database services");
    }

    // ============================================================
    // 1. DASHBOARD STATISTICS
    // ============================================================
    public DashboardStatsResponseDTO getDashboardStats(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting dashboard statistics for user: {}", requestId, performedBy);
        loggerUtil.log("dashboard", "Request ID: " + requestId + ", Getting dashboard statistics");

        DashboardStatsResponseDTO stats = new DashboardStatsResponseDTO();

        // Collections/APIs stats
        var collections = collectionsService.getCollectionsList(requestId, req, performedBy);
        int totalApis = collections.getCollections().stream()
                .mapToInt(c -> c.getRequestsCount())
                .sum();
        stats.setTotalApis(totalApis);
        stats.setTotalCollections(collections.getCollections().size());

        // Security stats
        var rateLimitRules = apiSecurityService.getRateLimitRules(requestId, performedBy);
        stats.setTotalRateLimitRules(rateLimitRules.getRules().size());

        var ipWhitelist = apiSecurityService.getIPWhitelist(requestId, performedBy);
        stats.setTotalIpWhitelistEntries(ipWhitelist.getEntries().size());

        var securityAlerts = apiSecurityService.getSecurityAlerts(requestId, performedBy);
        stats.setUnreadSecurityAlerts(Math.toIntExact(securityAlerts.getUnread()));

        // Code generation stats
        var languages = codeBaseService.getLanguages(requestId, performedBy);
        int totalImplementations = languages.getLanguages().stream()
                .mapToInt(l -> l.getImplementationCount())
                .sum();
        stats.setTotalCodeImplementations(totalImplementations);
        stats.setSupportedLanguages(languages.getLanguages().size());

        // User stats
        var userStats = userManagementService.getUserStatistics(requestId, performedBy);
        stats.setTotalUsers(userStats.getTotalUsers());
        stats.setActiveUsers(userStats.getActiveUsers());

        // Documentation stats
        var docStats = documentationService.getDocumentationStats(performedBy);
        stats.setTotalDocumentationEndpoints(docStats.getTotalEndpoints());
        stats.setPublishedDocumentation(docStats.getPublishedCollections());

        // Timestamp
        stats.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        log.info("Request ID: {}, Retrieved dashboard statistics", requestId);
        return stats;
    }

    // ============================================================
    // 2. API COLLECTIONS OVERVIEW
    // ============================================================
    public DashboardCollectionsResponseDTO getDashboardCollections(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting collections overview for user: {}", requestId, performedBy);

        List<DashboardCollectionDTO> collections = new ArrayList<>();

        var collectionsList = collectionsService.getCollectionsList(requestId, req, performedBy);

        for (var collection : collectionsList.getCollections()) {
            DashboardCollectionDTO dto = new DashboardCollectionDTO();
            dto.setId(collection.getId());
            dto.setName(collection.getName());
            dto.setDescription(collection.getDescription());
            dto.setRequestsCount(collection.getRequestsCount());
            dto.setFolderCount(collection.getFolderCount());
            dto.setFavorite(collection.isFavorite());
            dto.setOwner(collection.getOwner());
            dto.setLastUpdated(collection.getUpdatedAt());

            // Get collection details for more info
            try {
                var details = collectionsService.getCollectionDetails(requestId, req, performedBy, collection.getId());
                dto.setTotalFolders(details.getTotalFolders());
                dto.setTotalRequests(details.getTotalRequests());
            } catch (Exception e) {
                log.warn("Could not get details for collection {}: {}", collection.getId(), e.getMessage());
            }

            collections.add(dto);
        }

        log.info("Request ID: {}, Retrieved {} collections", requestId, collections.size());
        return new DashboardCollectionsResponseDTO(collections);
    }

    // ============================================================
    // 3. API ENDPOINTS OVERVIEW
    // ============================================================
    public DashboardEndpointsResponseDTO getDashboardEndpoints(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting endpoints overview for user: {}", requestId, performedBy);

        List<DashboardEndpointDTO> endpoints = new ArrayList<>();

        var collections = collectionsService.getCollectionsList(requestId, req, performedBy);

        for (var collection : collections.getCollections()) {
            try {
                var collectionDetails = collectionsService.getCollectionDetails(requestId, req, performedBy, collection.getId());

                for (var folder : collectionDetails.getFolders()) {
                    for (var request : folder.getRequests()) {
                        DashboardEndpointDTO dto = new DashboardEndpointDTO();
                        dto.setId(request.getId());
                        dto.setName(request.getName());
                        dto.setMethod(request.getMethod());
                        dto.setUrl(request.getUrl());
                        dto.setDescription(request.getDescription());
                        dto.setCollectionId(collection.getId());
                        dto.setCollectionName(collection.getName());
                        dto.setFolderId(folder.getId());
                        dto.setFolderName(folder.getName());
                        dto.setLastModified(request.getLastModified());
                        endpoints.add(dto);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not get endpoints for collection {}: {}", collection.getId(), e.getMessage());
            }
        }

        log.info("Request ID: {}, Retrieved {} endpoints", requestId, endpoints.size());
        return new DashboardEndpointsResponseDTO(endpoints);
    }

    // ============================================================
    // 4. RATE LIMIT RULES OVERVIEW
    // ============================================================
    public DashboardRateLimitRulesResponseDTO getDashboardRateLimitRules(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting rate limit rules overview for user: {}", requestId, performedBy);

        var rulesResponse = apiSecurityService.getRateLimitRules(requestId, performedBy);

        List<DashboardRateLimitRuleDTO> rules = rulesResponse.getRules().stream()
                .map(rule -> {
                    DashboardRateLimitRuleDTO dto = new DashboardRateLimitRuleDTO();
                    dto.setId(rule.getId());
                    dto.setName(rule.getName());
                    dto.setDescription(rule.getDescription());
                    dto.setEndpoint(rule.getEndpoint());
                    dto.setMethod(rule.getMethod());
                    dto.setLimit(rule.getLimit());
                    dto.setWindow(rule.getWindow());
                    dto.setStatus(rule.getStatus());
                    dto.setCreatedAt(rule.getCreatedAt() != null ? rule.getCreatedAt().toString() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} rate limit rules", requestId, rules.size());
        return new DashboardRateLimitRulesResponseDTO(rules, rulesResponse.getStatistics());
    }

    // ============================================================
    // 5. IP WHITELIST OVERVIEW
    // ============================================================
    public DashboardIpWhitelistResponseDTO getDashboardIpWhitelist(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting IP whitelist overview for user: {}", requestId, performedBy);

        var whitelistResponse = apiSecurityService.getIPWhitelist(requestId, performedBy);

        List<DashboardIpWhitelistEntryDTO> entries = whitelistResponse.getEntries().stream()
                .map(entry -> {
                    DashboardIpWhitelistEntryDTO dto = new DashboardIpWhitelistEntryDTO();
                    dto.setId(entry.getId());
                    dto.setName(entry.getName());
                    dto.setIpRange(entry.getIpRange());
                    dto.setDescription(entry.getDescription());
                    dto.setStatus(entry.getStatus());
                    dto.setCreatedAt(entry.getCreatedAt() != null ? entry.getCreatedAt().toString() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} IP whitelist entries", requestId, entries.size());
        return new DashboardIpWhitelistResponseDTO(entries, whitelistResponse.getAnalysis());
    }

    // ============================================================
    // 6. LOAD BALANCERS OVERVIEW
    // ============================================================
    public DashboardLoadBalancersResponseDTO getDashboardLoadBalancers(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting load balancers overview for user: {}", requestId, performedBy);

        var lbResponse = apiSecurityService.getLoadBalancers(requestId, performedBy);

        List<DashboardLoadBalancerDTO> loadBalancers = lbResponse.getLoadBalancers().stream()
                .map(lb -> {
                    DashboardLoadBalancerDTO dto = new DashboardLoadBalancerDTO();
                    dto.setId((String) lb.getId());
                    dto.setName((String) lb.getName());
                    dto.setAlgorithm((String) lb.getAlgorithm());
                    dto.setStatus((String) lb.getStatus());
                    dto.setTotalConnections((Integer) lb.getTotalConnections());

                    List<?> servers = (List<?>) lb.getServers();
                    dto.setServerCount(servers != null ? servers.size() : 0);

                    long healthyServers = servers != null ?
                            servers.stream()
                                    .filter(s -> {
                                        Map<?, ?> serverMap = (Map<?, ?>) s;
                                        return "healthy".equals(serverMap.get("status"));
                                    })
                                    .count() : 0;
                    dto.setHealthyServers((int) healthyServers);

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} load balancers", requestId, loadBalancers.size());
        return new DashboardLoadBalancersResponseDTO(loadBalancers, lbResponse.getPerformance());
    }

    // ============================================================
    // 7. SECURITY EVENTS OVERVIEW
    // ============================================================
    public DashboardSecurityEventsResponseDTO getDashboardSecurityEvents(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting security events overview for user: {}", requestId, performedBy);

        var eventsResponse = apiSecurityService.getSecurityEvents(requestId, performedBy);

        List<DashboardSecurityEventDTO> events = eventsResponse.getEvents().stream()
                .map(event -> {
                    DashboardSecurityEventDTO dto = new DashboardSecurityEventDTO();
                    dto.setId(event.getId());
                    dto.setType(event.getType());
                    dto.setSeverity(event.getSeverity());
                    dto.setSourceIp(event.getSourceIp());
                    dto.setEndpoint(event.getEndpoint());
                    dto.setMessage(event.getMessage());
                    dto.setTimestamp(event.getTimestamp());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} security events", requestId, events.size());
        return new DashboardSecurityEventsResponseDTO(events, eventsResponse.getInsights());
    }

    // ============================================================
    // 8. SECURITY ALERTS OVERVIEW
    // ============================================================
    public DashboardSecurityAlertsResponseDTO getDashboardSecurityAlerts(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting security alerts overview for user: {}", requestId, performedBy);

        var alertsResponse = apiSecurityService.getSecurityAlerts(requestId, performedBy);

        List<DashboardSecurityAlertDTO> alerts = alertsResponse.getAlerts().stream()
                .map(alert -> {
                    DashboardSecurityAlertDTO dto = new DashboardSecurityAlertDTO();
                    dto.setId(alert.getId());
                    dto.setType(alert.getType());
                    dto.setSeverity(alert.getSeverity());
                    dto.setMessage(alert.getMessage());
                    dto.setEndpoint(alert.getEndpoint());
                    dto.setRead(alert.isRead());
                    dto.setTimestamp(alert.getTimestamp());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} security alerts ({} unread)",
                requestId, alerts.size(), alertsResponse.getUnread());
        return new DashboardSecurityAlertsResponseDTO(alerts, Math.toIntExact(alertsResponse.getUnread()));
    }

    // ============================================================
    // 9. CODE LANGUAGES OVERVIEW
    // ============================================================
    public DashboardLanguagesResponseDTO getDashboardLanguages(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting languages overview for user: {}", requestId, performedBy);

        var languagesResponse = codeBaseService.getLanguages(requestId, performedBy);

        List<DashboardLanguageDTO> languages = languagesResponse.getLanguages().stream()
                .map(lang -> {
                    DashboardLanguageDTO dto = new DashboardLanguageDTO();
                    dto.setId(lang.getId());
                    dto.setName(lang.getName());
                    dto.setFramework(lang.getFramework());
                    dto.setColor(lang.getColor());
                    dto.setIcon(lang.getIcon());
                    dto.setImplementationCount(lang.getImplementationCount());
                    dto.setAvailable(lang.getIsAvailable());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} languages", requestId, languages.size());
        return new DashboardLanguagesResponseDTO(languages);
    }

    // ============================================================
    // 10. CODE IMPLEMENTATIONS OVERVIEW
    // ============================================================
    public DashboardImplementationsResponseDTO getDashboardImplementations(String requestId, HttpServletRequest req,
                                                                           String performedBy, int page, int size) {
        log.info("Request ID: {}, Getting implementations overview for user: {}, page: {}, size: {}",
                requestId, performedBy, page, size);

        var searchResults = codeBaseService.searchImplementations(requestId, performedBy,
                com.usg.apiAutomation.dtos.codeBase.SearchRequest.builder()
                        .query("*")
                        .page(page)
                        .pageSize(size)
                        .build());

        List<DashboardImplementationDTO> implementations = searchResults.getResults().stream()
                .map(result -> {
                    DashboardImplementationDTO dto = new DashboardImplementationDTO();
                    dto.setId(result.getId());
                    dto.setName(result.getName());
                    dto.setDescription(result.getDescription());
                    dto.setMethod(result.getMethod());
                    dto.setUrl(result.getUrl());
                    dto.setCollection(result.getCollection());
                    dto.setFolder(result.getFolder());
                    dto.setLanguages(result.getLanguages());
                    dto.setImplementationsCount(result.getImplementations());
                    dto.setLastModified(result.getLastModified());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} implementations", requestId, implementations.size());
        return new DashboardImplementationsResponseDTO(
                implementations,
                searchResults.getTotal(),
                searchResults.getPage(),
                searchResults.getPageSize(),
                searchResults.getTotalPages()
        );
    }

    // ============================================================
    // 11. DOCUMENTATION OVERVIEW
    // ============================================================
    public DashboardDocumentationResponseDTO getDashboardDocumentation(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting documentation overview for user: {}", requestId, performedBy);

        var collections = documentationService.getAPICollections(requestId, req, performedBy);

        List<DashboardDocumentationDTO> docs = new ArrayList<>();

        for (var collection : collections.getCollections()) {
            try {
                var endpoints = documentationService.getAPIEndpoints(requestId, req, performedBy, collection.getId(), null);

                DashboardDocumentationDTO dto = new DashboardDocumentationDTO();
                dto.setCollectionId(collection.getId());
                dto.setCollectionName(collection.getName());
                dto.setDescription(collection.getDescription());
                dto.setVersion(collection.getVersion());
                dto.setEndpointsCount(endpoints.getEndpoints().size());
                dto.setLastUpdated(collection.getUpdatedAt());

                // Get published info
                var published = documentationService.getPublishedDocumentation(collection.getId());
                dto.setPublished(!published.isEmpty());
                if (!published.isEmpty()) {
                    dto.setPublishedUrl(published.get(0).getPublishedUrl());
                    dto.setPublishedAt(published.get(0).getPublishedAt() != null ?
                            published.get(0).getPublishedAt().toString() : null);
                }

                docs.add(dto);
            } catch (Exception e) {
                log.warn("Could not get documentation for collection {}: {}", collection.getId(), e.getMessage());
            }
        }

        log.info("Request ID: {}, Retrieved {} documented collections", requestId, docs.size());
        return new DashboardDocumentationResponseDTO(docs);
    }

    // ============================================================
    // 12. MOCK SERVERS OVERVIEW
    // ============================================================
    public DashboardMockServersResponseDTO getDashboardMockServers(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting mock servers overview for user: {}", requestId, performedBy);

        List<DashboardMockServerDTO> mockServers = new ArrayList<>();

        var collections = collectionsService.getCollectionsList(requestId, req, performedBy);

        for (var collection : collections.getCollections()) {
            try {
                var mockServer = documentationService.getMockServer(collection.getId());
                if (mockServer != null) {
                    DashboardMockServerDTO dto = new DashboardMockServerDTO();
                    dto.setId(mockServer.getId());
                    dto.setCollectionId(collection.getId());
                    dto.setCollectionName(collection.getName());
                    dto.setMockServerUrl(mockServer.getMockServerUrl());
                    dto.setActive(mockServer.isActive());
                    dto.setDescription(mockServer.getDescription());
                    dto.setCreatedAt(mockServer.getCreatedAt() != null ?
                            mockServer.getCreatedAt().toString() : null);
                    dto.setExpiresAt(mockServer.getExpiresAt() != null ?
                            mockServer.getExpiresAt().toString() : null);
                    mockServers.add(dto);
                }
            } catch (Exception e) {
                log.debug("No mock server for collection {}: {}", collection.getId(), e.getMessage());
            }
        }

        log.info("Request ID: {}, Retrieved {} mock servers", requestId, mockServers.size());
        return new DashboardMockServersResponseDTO(mockServers);
    }

    // ============================================================
    // 13. USERS OVERVIEW
    // ============================================================
    public DashboardUsersResponseDTO getDashboardUsers(String requestId, HttpServletRequest req,
                                                       String performedBy, int page, int size) {
        log.info("Request ID: {}, Getting users overview for user: {}, page: {}, size: {}",
                requestId, performedBy, page, size);

        var usersResponse = userManagementService.getUsersList(
                requestId, performedBy, null, null, null, null, null, page, size);

        List<DashboardUserDTO> users = usersResponse.getUsers().stream()
                .map(user -> {
                    DashboardUserDTO dto = new DashboardUserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setFullName(user.getFullName());
                    dto.setRole(user.getRole());
                    dto.setStatus(user.getStatus());
                    dto.setDepartment(user.getDepartment());
                    dto.setLastActive(user.getLastActive());
                    dto.setJoinedDate(user.getJoinedDate());
                    dto.setSecurityScore(user.getSecurityScore());
                    dto.setMfaEnabled(user.isMfaEnabled());
                    dto.setTags(user.getTags());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} users", requestId, users.size());
        return new DashboardUsersResponseDTO(
                users,
                usersResponse.getTotal(),
                usersResponse.getPage(),
                usersResponse.getPageSize(),
                usersResponse.getTotalPages(),
                usersResponse.getStats()
        );
    }

    // ============================================================
    // 14. USER ACTIVITIES OVERVIEW
    // ============================================================
    public DashboardUserActivitiesResponseDTO getDashboardUserActivities(String requestId, HttpServletRequest req,
                                                                         String performedBy, int limit) {
        log.info("Request ID: {}, Getting user activities overview for user: {}, limit: {}",
                requestId, performedBy, limit);

        List<ActivityDTO> allActivities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Get user activities
        try {
            var userActivity = userManagementService.getUserActivity(requestId, performedBy, performedBy,
                    Date.from(now.minusDays(7).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                    new Date());

            for (var activity : userActivity.getActivities()) {
                ActivityDTO dto = new ActivityDTO();
                dto.setId("act-" + UUID.randomUUID().toString().substring(0, 8));
                dto.setAction(activity.getAction());
                dto.setDescription(activity.getDetails().toString());
                dto.setUser(activity.getPerformedBy());
                dto.setTimestamp(String.valueOf(activity.getTimestamp()));
                dto.setTime(getTimeAgo(String.valueOf(activity.getTimestamp())));
                dto.setIcon("user");
                dto.setPriority(activity.getSeverity() != null ? activity.getSeverity() : "low");
                allActivities.add(dto);
            }
        } catch (Exception e) {
            log.error("Failed to get user activities: {}", e.getMessage());
        }

        // Sort and limit
        allActivities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        if (allActivities.size() > limit) {
            allActivities = allActivities.subList(0, limit);
        }

        log.info("Request ID: {}, Retrieved {} recent user activities", requestId, allActivities.size());
        return new DashboardUserActivitiesResponseDTO(allActivities);
    }

    // ============================================================
    // 15. SECURITY SUMMARY
    // ============================================================
    public DashboardSecuritySummaryResponseDTO getDashboardSecuritySummary(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting security summary for user: {}", requestId, performedBy);

        var securitySummary = apiSecurityService.getSecuritySummary(requestId, performedBy);

        DashboardSecuritySummaryResponseDTO response = new DashboardSecuritySummaryResponseDTO();
        response.setTotalEndpoints(securitySummary.getTotalEndpoints());
        response.setSecuredEndpoints(securitySummary.getSecuredEndpoints());
        response.setVulnerableEndpoints(securitySummary.getVulnerableEndpoints());
        response.setBlockedRequests(securitySummary.getBlockedRequests());
        response.setThrottledRequests(securitySummary.getThrottledRequests());
        response.setAvgResponseTime(securitySummary.getAvgResponseTime());
        response.setSecurityScore(securitySummary.getSecurityScore());
        response.setLastScan(securitySummary.getLastScan());
        response.setQuickStats(securitySummary.getQuickStats());

        // Add additional stats from other services
        var rateLimitRules = apiSecurityService.getRateLimitRules(requestId, performedBy);
        response.setActiveRateLimitRules(rateLimitRules.getRules().size());

        var ipWhitelist = apiSecurityService.getIPWhitelist(requestId, performedBy);
        response.setActiveIpWhitelistEntries(ipWhitelist.getEntries().size());

        var securityAlerts = apiSecurityService.getSecurityAlerts(requestId, performedBy);
        response.setUnreadAlerts(Math.toIntExact(securityAlerts.getUnread()));

        log.info("Request ID: {}, Retrieved security summary", requestId);
        return response;
    }

    // ============================================================
    // 16. CODE GENERATION SUMMARY
    // ============================================================
    public DashboardCodeGenerationSummaryResponseDTO getDashboardCodeGenerationSummary(String requestId, HttpServletRequest req,
                                                                                       String performedBy) {
        log.info("Request ID: {}, Getting code generation summary for user: {}", requestId, performedBy);

        DashboardCodeGenerationSummaryResponseDTO response = new DashboardCodeGenerationSummaryResponseDTO();

        // Get languages and implementations
        var languages = codeBaseService.getLanguages(requestId, performedBy);
        int totalImplementations = languages.getLanguages().stream()
                .mapToInt(l -> l.getImplementationCount())
                .sum();

        response.setTotalImplementations(totalImplementations);
        response.setSupportedLanguages(languages.getLanguages().size());

        // Language distribution
        Map<String, Integer> languageDistribution = new HashMap<>();
        for (var lang : languages.getLanguages()) {
            languageDistribution.put(lang.getName(), lang.getImplementationCount());
        }
        response.setLanguageDistribution(languageDistribution);

        // Get recent implementations
        var searchResults = codeBaseService.searchImplementations(requestId, performedBy,
                com.usg.apiAutomation.dtos.codeBase.SearchRequest.builder()
                        .query("*")
                        .page(0)
                        .pageSize(5)
                        .build());

        List<Map<String, Object>> recentImplementations = new ArrayList<>();
        for (var result : searchResults.getResults()) {
            Map<String, Object> impl = new HashMap<>();
            impl.put("id", result.getId());
            impl.put("name", result.getName());
            impl.put("method", result.getMethod());
            impl.put("languages", result.getLanguages());
            impl.put("lastModified", result.getLastModified());
            recentImplementations.add(impl);
        }
        response.setRecentImplementations(recentImplementations);

        // Get validation stats (would need actual data)
        response.setValidationSuccessRate("98.5%");
        response.setAverageGenerationTime("2.3s");

        log.info("Request ID: {}, Retrieved code generation summary", requestId);
        return response;
    }

    // ============================================================
    // 17. ENVIRONMENTS OVERVIEW
    // ============================================================
    public DashboardEnvironmentsResponseDTO getDashboardEnvironments(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting environments overview for user: {}", requestId, performedBy);

        var environments = documentationService.getEnvironments(requestId, req, performedBy);

        List<DashboardEnvironmentDTO> envDtos = environments.getEnvironments().stream()
                .map(env -> {
                    DashboardEnvironmentDTO dto = new DashboardEnvironmentDTO();
                    dto.setId(env.getId());
                    dto.setName(env.getName());
                    dto.setBaseUrl(env.getBaseUrl());
                    dto.setActive(env.isActive());
                    dto.setDescription(env.getDescription());
                    dto.setLastUsed(env.getLastUsed());

                    if (env.getVariables() != null) {
                        dto.setVariablesCount(env.getVariables().size());
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Request ID: {}, Retrieved {} environments", requestId, envDtos.size());
        return new DashboardEnvironmentsResponseDTO(envDtos);
    }

    // ============================================================
    // 18. NOTIFICATIONS OVERVIEW
    // ============================================================
    public DashboardNotificationsResponseDTO getDashboardNotifications(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting notifications overview for user: {}", requestId, performedBy);

        var notifications = documentationService.getNotifications(requestId, req, performedBy);

        List<DashboardNotificationDTO> notificationDtos = notifications.getNotifications().stream()
                .map(notif -> {
                    DashboardNotificationDTO dto = new DashboardNotificationDTO();
                    dto.setId(notif.getId());
                    dto.setTitle(notif.getTitle());
                    dto.setMessage(notif.getMessage());
                    dto.setType(notif.getType());
                    dto.setRead(notif.isRead());
                    dto.setTime(notif.getTime());
                    dto.setIcon(notif.getIcon());
                    dto.setActionUrl(notif.getActionUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        long unreadCount = notificationDtos.stream().filter(n -> !n.isRead()).count();

        log.info("Request ID: {}, Retrieved {} notifications ({} unread)",
                requestId, notificationDtos.size(), unreadCount);
        return new DashboardNotificationsResponseDTO(notificationDtos, (int) unreadCount);
    }

    // ============================================================
    // 19. COMPREHENSIVE DASHBOARD
    // ============================================================
    public ComprehensiveDashboardResponseDTO getComprehensiveDashboard(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting comprehensive dashboard data for user: {}", requestId, performedBy);

        ComprehensiveDashboardResponseDTO response = new ComprehensiveDashboardResponseDTO();

        // Basic stats
        response.setStats(getDashboardStats(requestId, req, performedBy));

        // Collections and endpoints
        response.setCollections(getDashboardCollections(requestId, req, performedBy));
        response.setEndpoints(getDashboardEndpoints(requestId, req, performedBy));

        // Security data
        response.setRateLimitRules(getDashboardRateLimitRules(requestId, req, performedBy));
        response.setIpWhitelist(getDashboardIpWhitelist(requestId, req, performedBy));
        response.setLoadBalancers(getDashboardLoadBalancers(requestId, req, performedBy));
        response.setSecurityEvents(getDashboardSecurityEvents(requestId, req, performedBy));
        response.setSecurityAlerts(getDashboardSecurityAlerts(requestId, req, performedBy));
        response.setSecuritySummary(getDashboardSecuritySummary(requestId, req, performedBy));

        // Code generation
        response.setLanguages(getDashboardLanguages(requestId, req, performedBy));
        response.setCodeGenerationSummary(getDashboardCodeGenerationSummary(requestId, req, performedBy));

        // Documentation
        response.setDocumentation(getDashboardDocumentation(requestId, req, performedBy));
        response.setMockServers(getDashboardMockServers(requestId, req, performedBy));
        response.setEnvironments(getDashboardEnvironments(requestId, req, performedBy));

        // Users
        response.setUsers(getDashboardUsers(requestId, req, performedBy, 1, 10));
        response.setUserActivities(getDashboardUserActivities(requestId, req, performedBy, 20));

        // Notifications
        response.setNotifications(getDashboardNotifications(requestId, req, performedBy));

        // Metadata
        response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.setGeneratedFor(performedBy);
        response.setRequestId(requestId);

        log.info("Request ID: {}, Retrieved comprehensive dashboard data", requestId);
        return response;
    }

    // ============================================================
    // 20. SEARCH DASHBOARD
    // ============================================================
    public DashboardSearchResponseDTO searchDashboard(String requestId, HttpServletRequest req,
                                                      String performedBy, String query, String type, int limit) {
        log.info("Request ID: {}, Searching dashboard for: {}, type: {}, limit: {}",
                requestId, query, type, limit);

        List<DashboardSearchResultDTO> results = new ArrayList<>();

        // Search collections
        if (type == null || type.equals("all") || type.equals("collections")) {
            try {
                var collections = collectionsService.getCollectionsList(requestId, req, performedBy);
                for (var collection : collections.getCollections()) {
                    if (collection.getName().toLowerCase().contains(query.toLowerCase()) ||
                            (collection.getDescription() != null && collection.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                        DashboardSearchResultDTO dto = new DashboardSearchResultDTO();
                        dto.setId(collection.getId());
                        dto.setTitle(collection.getName());
                        dto.setType("Collection");
                        dto.setDescription(collection.getDescription());
                        dto.setUrl("/collections/" + collection.getId());
                        results.add(dto);
                    }
                }
            } catch (Exception e) {
                log.warn("Error searching collections: {}", e.getMessage());
            }
        }

        // Search endpoints
        if (type == null || type.equals("all") || type.equals("endpoints")) {
            try {
                var collections = collectionsService.getCollectionsList(requestId, req, performedBy);
                for (var collection : collections.getCollections()) {
                    try {
                        var details = collectionsService.getCollectionDetails(requestId, req, performedBy, collection.getId());
                        for (var folder : details.getFolders()) {
                            for (var endpoint : folder.getRequests()) {
                                if (endpoint.getName().toLowerCase().contains(query.toLowerCase()) ||
                                        (endpoint.getDescription() != null && endpoint.getDescription().toLowerCase().contains(query.toLowerCase())) ||
                                        endpoint.getUrl().toLowerCase().contains(query.toLowerCase())) {
                                    DashboardSearchResultDTO dto = new DashboardSearchResultDTO();
                                    dto.setId(endpoint.getId());
                                    dto.setTitle(endpoint.getName() + " (" + endpoint.getMethod() + ")");
                                    dto.setType("Endpoint");
                                    dto.setDescription(endpoint.getDescription());
                                    dto.setUrl("/collections/" + collection.getId() + "/requests/" + endpoint.getId());
                                    dto.setSubtitle(collection.getName() + " > " + folder.getName());
                                    results.add(dto);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error searching endpoints in collection {}: {}", collection.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("Error searching endpoints: {}", e.getMessage());
            }
        }

        // Search users
        if (type == null || type.equals("all") || type.equals("users")) {
            try {
                SearchUsersRequestDTO searchRequest = SearchUsersRequestDTO.builder()
                        .query(query)
                        .page(0)
                        .pageSize(100) // or whatever default you want
                        .build();

                var users = userManagementService.searchUsers(requestId, performedBy, searchRequest);

                for (var user : users.getResults()) {
                    DashboardSearchResultDTO dto = new DashboardSearchResultDTO();
                    dto.setId(user.getId());
                    dto.setTitle(user.getFullName() + " (" + user.getUsername() + ")");
                    dto.setType("User");
                    dto.setDescription(user.getEmail() + " - " + user.getRole());
                    dto.setUrl("/users/" + user.getId());
                    results.add(dto);
                }
            } catch (Exception e) {
                log.warn("Error searching users: {}", e.getMessage());
            }
        }

        // Limit results
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        log.info("Request ID: {}, Found {} search results", requestId, results.size());
        return new DashboardSearchResponseDTO(query, results, results.size());
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private String getTimeAgo(String timestamp) {
        try {
            LocalDateTime time = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();
            long minutesAgo = java.time.Duration.between(time, now).toMinutes();

            if (minutesAgo < 1) return "Just now";
            if (minutesAgo < 60) return minutesAgo + " minute" + (minutesAgo > 1 ? "s" : "") + " ago";
            if (minutesAgo < 1440) {
                long hoursAgo = minutesAgo / 60;
                return hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago";
            }
            long daysAgo = minutesAgo / 1440;
            return daysAgo + " day" + (daysAgo > 1 ? "s" : "") + " ago";
        } catch (Exception e) {
            return timestamp;
        }
    }
}