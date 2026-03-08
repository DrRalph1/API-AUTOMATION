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

// Add these imports at the top
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiResponseMappingDTO;
import com.usg.apiAutomation.dtos.dashboard.DashboardEndpointDTO; // Make sure this DTO has all fields

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
                // Get collection details with folders and endpoints
                var collectionDetails = collectionsService.getCollectionDetails(requestId, req, performedBy, collection.getId());

                for (var folder : collectionDetails.getFolders()) {
                    for (var request : folder.getRequests()) {
                        DashboardEndpointDTO dto = new DashboardEndpointDTO();

                        // Basic fields
                        dto.setId(request.getId());
                        dto.setName(request.getName());
                        dto.setMethod(request.getMethod());
                        dto.setUrl(request.getUrl());
                        dto.setDescription(request.getDescription());
                        dto.setCollectionId(collection.getId());
                        dto.setCollectionName(collection.getName());
                        dto.setFolderId(folder.getId()); // Make sure this is set
                        dto.setFolderName(folder.getName()); // Make sure this is set
                        dto.setLastUpdated(request.getLastModified());

                        // Calculate time ago
                        dto.setTimeAgo(calculateTimeAgo(request.getLastModified()));

                        // Set collectionInfo for easier access in UI
                        Map<String, Object> collectionInfo = new HashMap<>();
                        collectionInfo.put("collectionId", collection.getId());
                        collectionInfo.put("collectionName", collection.getName());
                        collectionInfo.put("folderId", folder.getId());
                        collectionInfo.put("folderName", folder.getName());

                        // Get detailed request information from CollectionsService
                        try {
                            var requestDetails = collectionsService.getRequestDetails(
                                    requestId, req, performedBy, collection.getId(), request.getId());

                            // Set parameters
                            if (requestDetails.getParameters() != null && !requestDetails.getParameters().isEmpty()) {
                                List<Map<String, Object>> parameters = new ArrayList<>();
                                for (ApiParameterDTO param : requestDetails.getParameters()) {
                                    Map<String, Object> paramMap = new HashMap<>();
                                    paramMap.put("id", param.getId());
                                    paramMap.put("name", param.getKey());
                                    paramMap.put("type", param.getType());
                                    paramMap.put("required", param.getRequired());
                                    paramMap.put("description", param.getDescription());
                                    paramMap.put("in", param.getParameterLocation());
                                    paramMap.put("schema", Map.of("type", mapOracleTypeToApiType(param.getOracleType())));
                                    parameters.add(paramMap);
                                }
                                dto.setParameters(parameters);
                            } else {
                                dto.setParameters(new ArrayList<>());
                            }

                            // Set response mappings
                            if (requestDetails.getResponseMappings() != null && !requestDetails.getResponseMappings().isEmpty()) {
                                List<Map<String, Object>> responseMappings = new ArrayList<>();
                                for (ApiResponseMappingDTO mapping : requestDetails.getResponseMappings()) {
                                    Map<String, Object> mappingMap = new HashMap<>();
                                    mappingMap.put("id", mapping.getId());
                                    mappingMap.put("apiField", mapping.getApiField());
                                    mappingMap.put("dbColumn", mapping.getDbColumn());
                                    mappingMap.put("type", mapping.getApiType());
                                    mappingMap.put("oracleType", mapping.getOracleType());
                                    mappingMap.put("nullable", mapping.getNullable());
                                    mappingMap.put("isPrimaryKey", mapping.getIsPrimaryKey());
                                    responseMappings.add(mappingMap);
                                }
                                dto.setResponseMappings(responseMappings);
                            } else {
                                dto.setResponseMappings(new ArrayList<>());
                            }

                            // Set tags
                            if (request.getTags() != null) {
                                List<Map<String, Object>> tagsList = new ArrayList<>();
                                Object tagsObj = request.getTags();

                                if (tagsObj instanceof List) {
                                    // Handle List type
                                    List<?> tags = (List<?>) tagsObj;
                                    for (Object tag : tags) {
                                        if (tag instanceof Map) {
                                            // If it's already a Map with the right structure
                                            tagsList.add((Map<String, Object>) tag);
                                        } else if (tag instanceof String) {
                                            // If it's a String, create a Map
                                            Map<String, Object> tagMap = new HashMap<>();
                                            tagMap.put("name", tag);
                                            tagMap.put("id", UUID.randomUUID().toString());
                                            tagsList.add(tagMap);
                                        }
                                    }
                                } else if (tagsObj instanceof String) {
                                    // Handle comma-separated string
                                    String tagsStr = (String) tagsObj;
                                    if (!tagsStr.isEmpty()) {
                                        String[] tagArray = tagsStr.split(",");
                                        for (String tag : tagArray) {
                                            String trimmedTag = tag.trim();
                                            if (!trimmedTag.isEmpty()) {
                                                Map<String, Object> tagMap = new HashMap<>();
                                                tagMap.put("name", trimmedTag);
                                                tagMap.put("id", UUID.randomUUID().toString());
                                                tagsList.add(tagMap);
                                            }
                                        }
                                    }
                                } else {
                                    // Handle single object
                                    Map<String, Object> tagMap = new HashMap<>();
                                    tagMap.put("name", tagsObj.toString());
                                    tagMap.put("id", UUID.randomUUID().toString());
                                    tagsList.add(tagMap);
                                }

                                dto.setTags(tagsList);
                            } else {
                                dto.setTags(new ArrayList<>());
                            }

                            // Set status (default to "active" if not specified)
                            dto.setStatus(request.getStatus() != null ? request.getStatus() : "active");

                            // Set version
                            dto.setVersion(collection.getVersion() != null ? collection.getVersion() : "v1");

                            // Set owner
                            dto.setOwner(collection.getOwner() != null ? collection.getOwner() : "System");

                            // Set API code if available
                            if (requestDetails.getApiCode() != null) {
                                dto.setApiCode(requestDetails.getApiCode());
                            }

                            // Set mock data for calls, latency, success rate, errors, avgResponseTime
                            dto.setCalls(generateRandomCalls(request.getId()));
                            dto.setLatency("42ms");
                            dto.setSuccessRate("98.5%");
                            dto.setErrors(generateRandomErrors(request.getId()));
                            dto.setAvgResponseTime("42ms");

                        } catch (Exception e) {
                            log.warn("Could not get detailed request info for endpoint {}: {}", request.getId(), e.getMessage());

                            // Set fallback values
                            dto.setParameters(new ArrayList<>());
                            dto.setResponseMappings(new ArrayList<>());
                            dto.setTags(convertToTagsList(request.getTags()));
                            dto.setStatus("active");
                            dto.setVersion(collection.getVersion() != null ? collection.getVersion() : "v1");
                            dto.setOwner(collection.getOwner() != null ? collection.getOwner() : "System");
                            dto.setCalls(generateRandomCalls(request.getId()));
                            dto.setLatency("42ms");
                            dto.setSuccessRate("98.5%");
                            dto.setErrors(generateRandomErrors(request.getId()));
                            dto.setAvgResponseTime("42ms");
                        }

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

    // Helper method to calculate time ago
    private String calculateTimeAgo(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "Unknown";
        try {
            LocalDateTime time = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();
            long minutesAgo = java.time.Duration.between(time, now).toMinutes();

            if (minutesAgo < 1) return "0 seconds ago";
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

    // Helper method to generate random calls (would be replaced by actual monitoring data)
    private int generateRandomCalls(String endpointId) {
        // Use hash of endpointId to generate consistent but varied numbers
        int hash = Math.abs(endpointId.hashCode());
        return 100 + (hash % 900); // 100-999
    }

    // Helper method to generate random errors
    private int generateRandomErrors(String endpointId) {
        int hash = Math.abs(endpointId.hashCode());
        return 1 + (hash % 20); // 1-20
    }

    // Helper method to map Oracle types to API types
    private String mapOracleTypeToApiType(String oracleType) {
        if (oracleType == null) return "string";

        String upperType = oracleType.toUpperCase();
        if (upperType.contains("VARCHAR") || upperType.contains("CHAR") || upperType.contains("CLOB")) {
            return "string";
        } else if (upperType.contains("NUMBER") || upperType.contains("INTEGER") || upperType.contains("FLOAT") || upperType.contains("DECIMAL")) {
            return "number";
        } else if (upperType.contains("DATE") || upperType.contains("TIMESTAMP")) {
            return "string"; // or "date" if you have a date type
        } else if (upperType.contains("BOOLEAN")) {
            return "boolean";
        }
        return "string";
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

        // Collections and endpoints (make sure endpoints have all fields with IDs)
        response.setCollections(getDashboardCollections(requestId, req, performedBy));

        // Get endpoints and ensure all nested elements have IDs
        DashboardEndpointsResponseDTO endpointsResponse = getDashboardEndpoints(requestId, req, performedBy);

        // Ensure all nested elements in each endpoint have proper IDs
        if (endpointsResponse.getEndpoints() != null) {
            for (DashboardEndpointDTO endpoint : endpointsResponse.getEndpoints()) {
                // Ensure endpoint itself has ID
                if (endpoint.getId() == null || endpoint.getId().isEmpty()) {
                    endpoint.setId(UUID.randomUUID().toString());
                }

                // Ensure parameters have IDs
                if (endpoint.getParameters() != null) {
                    for (Map<String, Object> param : endpoint.getParameters()) {
                        if (!param.containsKey("id") || param.get("id") == null) {
                            param.put("id", UUID.randomUUID().toString());
                        }
                    }
                }

                // Ensure response mappings have IDs
                if (endpoint.getResponseMappings() != null) {
                    for (Map<String, Object> mapping : endpoint.getResponseMappings()) {
                        if (!mapping.containsKey("id") || mapping.get("id") == null) {
                            mapping.put("id", UUID.randomUUID().toString());
                        }
                    }
                }

                // Ensure tags have IDs
                if (endpoint.getTags() != null) {
                    for (Map<String, Object> tag : endpoint.getTags()) {
                        if (!tag.containsKey("id") || tag.get("id") == null) {
                            tag.put("id", UUID.randomUUID().toString());
                        }
                    }
                }
            }
        }
        response.setEndpoints(endpointsResponse);

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
        response.setGeneratedFor(performedBy);
        response.setRequestId(requestId);

        log.info("Request ID: {}, Retrieved comprehensive dashboard data with all element IDs", requestId);
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

    // Robust tags conversion

    // Add this helper method in your service class
    private List<Map<String, Object>> convertToTagsList(Object tagsInput) {
        List<Map<String, Object>> tagsList = new ArrayList<>();

        if (tagsInput == null) {
            return tagsList;
        }

        try {
            // Case 1: If it's already a List of Maps (ideal case)
            if (tagsInput instanceof List) {
                List<?> list = (List<?>) tagsInput;
                for (Object item : list) {
                    if (item instanceof Map) {
                        // Ensure the map has the required structure
                        Map<?, ?> map = (Map<?, ?>) item;
                        Map<String, Object> tagMap = new HashMap<>();

                        // Copy existing properties or create default ones
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            if (entry.getKey() != null) {
                                tagMap.put(entry.getKey().toString(), entry.getValue());
                            }
                        }

                        // Ensure minimum required fields exist
                        if (!tagMap.containsKey("name") && !tagMap.containsKey("id")) {
                            tagMap.put("name", "tag-" + UUID.randomUUID().toString().substring(0, 6));
                        } else if (!tagMap.containsKey("name") && tagMap.containsKey("id")) {
                            tagMap.put("name", "tag-" + tagMap.get("id"));
                        } else if (tagMap.containsKey("name") && !tagMap.containsKey("id")) {
                            tagMap.put("id", UUID.randomUUID().toString());
                        }

                        tagsList.add(tagMap);
                    } else if (item != null) {
                        // Item is not a Map, convert to a Map with default structure
                        Map<String, Object> tagMap = new HashMap<>();
                        tagMap.put("name", item.toString());
                        tagMap.put("id", UUID.randomUUID().toString());
                        tagMap.put("type", "simple");
                        tagMap.put("value", item.toString());
                        tagsList.add(tagMap);
                    }
                }
            }
            // Case 2: If it's a Map (single tag as Map)
            else if (tagsInput instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) tagsInput;
                Map<String, Object> tagMap = new HashMap<>();

                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() != null) {
                        tagMap.put(entry.getKey().toString(), entry.getValue());
                    }
                }

                // Ensure required fields
                if (!tagMap.containsKey("name")) {
                    tagMap.put("name", tagMap.containsKey("id") ? "tag-" + tagMap.get("id") : "tag-" + UUID.randomUUID().toString().substring(0, 6));
                }
                if (!tagMap.containsKey("id")) {
                    tagMap.put("id", UUID.randomUUID().toString());
                }

                tagsList.add(tagMap);
            }
            // Case 3: If it's a String (comma-separated or single)
            else if (tagsInput instanceof String) {
                String tagsStr = (String) tagsInput;
                if (!tagsStr.trim().isEmpty()) {
                    // Check if it's JSON array format
                    if (tagsStr.trim().startsWith("[") && tagsStr.trim().endsWith("]")) {
                        try {
                            // Try to parse as JSON array
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            List<Map<String, Object>> parsedTags = mapper.readValue(tagsStr, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                            for (Map<String, Object> tag : parsedTags) {
                                if (!tag.containsKey("id")) {
                                    tag.put("id", UUID.randomUUID().toString());
                                }
                                if (!tag.containsKey("name")) {
                                    tag.put("name", "tag-" + tag.getOrDefault("id", UUID.randomUUID().toString()).toString().substring(0, 6));
                                }
                                tagsList.add(tag);
                            }
                        } catch (Exception e) {
                            // If JSON parsing fails, treat as comma-separated
                            processCommaSeparatedTags(tagsStr, tagsList);
                        }
                    } else {
                        // Treat as comma-separated string
                        processCommaSeparatedTags(tagsStr, tagsList);
                    }
                }
            }
            // Case 4: If it's an array
            else if (tagsInput.getClass().isArray()) {
                Object[] array = (Object[]) tagsInput;
                for (Object item : array) {
                    if (item != null) {
                        Map<String, Object> tagMap = new HashMap<>();
                        tagMap.put("name", item.toString());
                        tagMap.put("id", UUID.randomUUID().toString());
                        tagMap.put("type", "array-item");
                        tagMap.put("value", item.toString());
                        tagsList.add(tagMap);
                    }
                }
            }
            // Case 5: Any other type
            else {
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("name", tagsInput.toString());
                tagMap.put("id", UUID.randomUUID().toString());
                tagMap.put("type", tagsInput.getClass().getSimpleName());
                tagMap.put("value", tagsInput.toString());
                tagsList.add(tagMap);
            }
        } catch (Exception e) {
            // Log the error and return empty list or create a default tag
            log.warn("Error converting tags: {}", e.getMessage());
            Map<String, Object> errorTag = new HashMap<>();
            errorTag.put("name", "error-tag");
            errorTag.put("id", UUID.randomUUID().toString());
            errorTag.put("error", e.getMessage());
            tagsList.add(errorTag);
        }

        return tagsList;
    }

    // Helper method to process comma-separated tags
    private void processCommaSeparatedTags(String tagsStr, List<Map<String, Object>> tagsList) {
        String[] tagArray = tagsStr.split(",");
        for (String tag : tagArray) {
            String trimmedTag = tag.trim();
            if (!trimmedTag.isEmpty()) {
                // Check if tag contains key:value format
                if (trimmedTag.contains(":")) {
                    String[] keyValue = trimmedTag.split(":", 2);
                    Map<String, Object> tagMap = new HashMap<>();
                    tagMap.put("name", keyValue[0].trim());
                    tagMap.put("value", keyValue[1].trim());
                    tagMap.put("id", UUID.randomUUID().toString());
                    tagsList.add(tagMap);
                } else {
                    Map<String, Object> tagMap = new HashMap<>();
                    tagMap.put("name", trimmedTag);
                    tagMap.put("id", UUID.randomUUID().toString());
                    tagMap.put("type", "simple");
                    tagMap.put("value", trimmedTag);
                    tagsList.add(tagMap);
                }
            }
        }
    }

    // Alternative even more robust version with builder pattern
    public List<Map<String, Object>> convertToTagsListRobust(Object tagsInput) {
        return Optional.ofNullable(tagsInput)
                .map(this::convertToTagsListInternal)
                .orElseGet(ArrayList::new);
    }

    private List<Map<String, Object>> convertToTagsListInternal(Object tagsInput) {
        List<Map<String, Object>> tagsList = new ArrayList<>();

        // Use Optional to handle nulls elegantly
        Optional.ofNullable(tagsInput)
                .ifPresent(input -> {
                    try {
                        // Stream-based processing for Collections
                        if (input instanceof Collection) {
                            ((Collection<?>) input).stream()
                                    .filter(Objects::nonNull)
                                    .forEach(item -> processTagItem(item, tagsList));
                        }
                        // Handle arrays
                        else if (input.getClass().isArray()) {
                            Arrays.stream((Object[]) input)
                                    .filter(Objects::nonNull)
                                    .forEach(item -> processTagItem(item, tagsList));
                        }
                        // Handle single items
                        else {
                            processTagItem(input, tagsList);
                        }
                    } catch (Exception e) {
                        log.error("Error processing tags: {}", e.getMessage(), e);
                        createErrorTag(tagsList, e);
                    }
                });

        return tagsList;
    }

    private void processTagItem(Object item, List<Map<String, Object>> tagsList) {
        Map<String, Object> tagMap = createTagMap(item);
        if (!tagMap.isEmpty()) {
            tagsList.add(tagMap);
        }
    }

    private Map<String, Object> createTagMap(Object item) {
        Map<String, Object> tagMap = new HashMap<>();

        if (item instanceof Map) {
            // Copy existing map and ensure required fields
            ((Map<?, ?>) item).forEach((key, value) -> {
                if (key != null) {
                    tagMap.put(key.toString(), value);
                }
            });

            // Ensure required fields exist
            if (!tagMap.containsKey("id")) {
                tagMap.put("id", UUID.randomUUID().toString());
            }
            if (!tagMap.containsKey("name")) {
                String name = tagMap.containsKey("value") ?
                        tagMap.get("value").toString() :
                        "tag-" + tagMap.get("id").toString().substring(0, 6);
                tagMap.put("name", name);
            }
        } else {
            // Create new map for non-Map items
            tagMap.put("id", UUID.randomUUID().toString());
            tagMap.put("name", item.toString());
            tagMap.put("value", item.toString());
            tagMap.put("type", item.getClass().getSimpleName());
            tagMap.put("original", item.toString());
        }

        // Add metadata
        tagMap.put("convertedAt", LocalDateTime.now().toString());
        tagMap.put("source", item.getClass().getSimpleName());

        return tagMap;
    }

    private void createErrorTag(List<Map<String, Object>> tagsList, Exception e) {
        Map<String, Object> errorTag = new HashMap<>();
        errorTag.put("id", UUID.randomUUID().toString());
        errorTag.put("name", "conversion-error");
        errorTag.put("error", e.getMessage());
        errorTag.put("timestamp", LocalDateTime.now().toString());
        tagsList.add(errorTag);
    }

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