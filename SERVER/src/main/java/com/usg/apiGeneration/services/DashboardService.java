package com.usg.apiGeneration.services;

import com.usg.apiGeneration.dtos.apiGenerationEngine.ApiAuthConfigDTO;
import com.usg.apiGeneration.dtos.dashboard.*;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.ApiAuthConfigEntity;
import com.usg.apiGeneration.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.apiGeneration.repositories.apiGenerationEngine.ApiAuthConfigRepository;
import com.usg.apiGeneration.repositories.apiGenerationEngine.ApiRequestRepository;
import com.usg.apiGeneration.repositories.apiGenerationEngine.GeneratedAPIRepository;
import com.usg.apiGeneration.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LoggerUtil loggerUtil;
    private final APISecurityService apiSecurityService;
    private final CodeBaseService codeBaseService;
    private final CollectionsService collectionsService;
    private final DocumentationService documentationService;
    private final UserManagementService userManagementService;
    private final ApiAuthConfigRepository authConfigRepository;
    private final GeneratedAPIRepository generatedAPIRepository;
    private final ApiRequestRepository apiRequestRepository;

    // Inner classes for structured data
    private static class FolderData {
        String id;
        String name;
        List<RequestData> requests;

        FolderData(String id, String name) {
            this.id = id;
            this.name = name;
            this.requests = new ArrayList<>();
        }
    }

    private static class RequestData {
        String sourceRequestId;
        String id;
        String name;
        String method;
        String url;
        String description;
        String lastModified;
        String createdAt;
        Object tags;
        String status;
    }

    @PostConstruct
    public void init() {
        log.info("DashboardService initialized with all database services");
    }

    // ============================================================
    // 1. DASHBOARD STATISTICS - WITH TIMEOUT
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public DashboardStatsResponseDTO getDashboardStats(String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Getting dashboard statistics for user: {}", requestId, performedBy);
        loggerUtil.log("dashboard", "Request ID: " + requestId + ", Getting dashboard statistics");

        DashboardStatsResponseDTO stats = new DashboardStatsResponseDTO();

        var collections = collectionsService.getCollectionsList(requestId, req, performedBy);
        int totalApis = collections.getCollections().stream()
                .mapToInt(c -> c.getRequestsCount())
                .sum();
        stats.setTotalApis(totalApis);
        stats.setTotalCollections(collections.getCollections().size());

        long totalApiRequests = apiRequestRepository.count();
        stats.setTotalApiRequests(totalApiRequests);

        var rateLimitRules = apiSecurityService.getRateLimitRules(requestId, performedBy);
        stats.setTotalRateLimitRules(rateLimitRules.getRules().size());

        var ipWhitelist = apiSecurityService.getIPWhitelist(requestId, performedBy);
        stats.setTotalIpWhitelistEntries(ipWhitelist.getEntries().size());

        var securityAlerts = apiSecurityService.getSecurityAlerts(requestId, performedBy);
        stats.setUnreadSecurityAlerts(Math.toIntExact(securityAlerts.getUnread()));

        var languages = codeBaseService.getLanguages(requestId, performedBy);
        int totalImplementations = languages.getLanguages().stream()
                .mapToInt(l -> l.getImplementationCount())
                .sum();
        stats.setTotalCodeImplementations(totalImplementations);
        stats.setSupportedLanguages(languages.getLanguages().size());

        var userStats = userManagementService.getUserStatistics(requestId, performedBy);
        stats.setTotalUsers(userStats.getTotalUsers());
        stats.setActiveUsers(userStats.getActiveUsers());

        var docStats = documentationService.getDocumentationStats();
        stats.setTotalDocumentationEndpoints(docStats.getTotalEndpoints());
        stats.setPublishedDocumentation(docStats.getPublishedCollections());

        stats.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        log.info("Request ID: {}, Retrieved dashboard statistics. Total API Requests: {}", requestId, totalApiRequests);
        return stats;
    }

    // ============================================================
    // 2. API COLLECTIONS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
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
    // 3. API ENDPOINTS OVERVIEW - OPTIMIZED WITH BATCH QUERIES
    // ============================================================
    @Transactional(readOnly = true, timeout = 45)
    public DashboardEndpointsResponseDTO getDashboardEndpoints(String requestId, HttpServletRequest req, String performedBy) {
        long startTime = System.currentTimeMillis();
        log.info("Request ID: {}, Getting endpoints overview for user: {}", requestId, performedBy);

        List<DashboardEndpointDTO> endpoints = getAllEndpointsOptimized(requestId, req, performedBy);

        // SORT ENDPOINTS IN DESCENDING ORDER BY MOST RECENT ACTIVITY
        endpoints.sort((e1, e2) -> {
            String date1 = getMostRecentDate(e1);
            String date2 = getMostRecentDate(e2);

            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return 1;
            if (date2 == null) return -1;

            try {
                LocalDateTime time1 = LocalDateTime.parse(date1, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime time2 = LocalDateTime.parse(date2, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return time2.compareTo(time1);
            } catch (Exception e) {
                return date2.compareTo(date1);
            }
        });

        long duration = System.currentTimeMillis() - startTime;
        log.info("Request ID: {}, Retrieved {} endpoints (sorted) in {} ms", requestId, endpoints.size(), duration);
        return new DashboardEndpointsResponseDTO(endpoints);
    }

    // ============================================================
    // OPTIMIZED METHOD TO GET ALL ENDPOINTS WITH BATCH QUERIES
    // ============================================================
    private List<DashboardEndpointDTO> getAllEndpointsOptimized(String requestId, HttpServletRequest req, String performedBy) {
        List<DashboardEndpointDTO> endpoints = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try {
            var collections = collectionsService.getCollectionsList(requestId, req, performedBy);

            // Collect all source request IDs for batch processing
            List<String> allSourceRequestIds = new ArrayList<>();
            Map<String, List<FolderData>> collectionFoldersMap = new HashMap<>();

            for (var collection : collections.getCollections()) {
                try {
                    var collectionDetails = collectionsService.getCollectionDetails(requestId, req, performedBy, collection.getId());
                    List<FolderData> folderList = new ArrayList<>();

                    for (var folder : collectionDetails.getFolders()) {
                        FolderData folderData = new FolderData(folder.getId(), folder.getName());

                        for (var request : folder.getRequests()) {
                            String sourceRequestId = request.getId();
                            allSourceRequestIds.add(sourceRequestId);

                            RequestData requestData = new RequestData();
                            requestData.sourceRequestId = sourceRequestId;
                            requestData.id = sourceRequestId;
                            requestData.name = request.getName();
                            requestData.method = request.getMethod();
                            requestData.url = request.getUrl();
                            requestData.description = request.getDescription();
                            requestData.lastModified = request.getLastModified();
                            requestData.createdAt = request.getCreatedAt();
                            requestData.tags = request.getTags();
                            requestData.status = request.getStatus();

                            folderData.requests.add(requestData);
                        }

                        folderList.add(folderData);
                    }

                    collectionFoldersMap.put(collection.getId(), folderList);
                } catch (Exception e) {
                    log.warn("Could not get endpoints for collection {}: {}", collection.getId(), e.getMessage());
                }
            }

            // BATCH FETCH: Get all generated APIs in ONE query
            Map<String, String> sourceRequestIdToApiIdMap = new HashMap<>();
            Map<String, GeneratedApiEntity> sourceRequestIdToApiEntityMap = new HashMap<>();

            if (!allSourceRequestIds.isEmpty()) {
                try {
                    List<GeneratedApiEntity> generatedApis = generatedAPIRepository.findBySourceRequestIdIn(allSourceRequestIds);
                    log.debug("Request ID: {}, Batch fetched {} generated APIs for {} source requests",
                            requestId, generatedApis.size(), allSourceRequestIds.size());

                    for (GeneratedApiEntity api : generatedApis) {
                        if (api.getSourceRequestId() != null) {
                            sourceRequestIdToApiIdMap.put(api.getSourceRequestId(), api.getId());
                            sourceRequestIdToApiEntityMap.put(api.getSourceRequestId(), api);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error batch fetching generated APIs: {}", e.getMessage(), e);
                }
            }

            // Build endpoints
            for (var collection : collections.getCollections()) {
                List<FolderData> folderList = collectionFoldersMap.get(collection.getId());
                if (folderList == null) continue;

                for (FolderData folderData : folderList) {
                    for (RequestData requestData : folderData.requests) {
                        DashboardEndpointDTO dto = new DashboardEndpointDTO();
                        dto.setId(requestData.id);

                        String generatedApiId = sourceRequestIdToApiIdMap.get(requestData.sourceRequestId);
                        dto.setApiId(generatedApiId);

                        dto.setName(requestData.name);
                        dto.setMethod(requestData.method);
                        dto.setUrl(requestData.url);
                        dto.setDescription(requestData.description);
                        dto.setCollectionId(collection.getId());
                        dto.setCollectionName(collection.getName());
                        dto.setFolderId(folderData.id);
                        dto.setFolderName(folderData.name);
                        dto.setLastUpdated(requestData.lastModified);
                        dto.setCreatedAt(requestData.createdAt);
                        dto.setTimeAgo(calculateTimeAgo(getMostRecentDateFromStrings(requestData.lastModified, requestData.createdAt)));
                        dto.setStatus(requestData.status != null ? requestData.status : "active");
                        dto.setVersion(collection.getVersion() != null ? collection.getVersion() : "v1");
                        dto.setOwner(collection.getOwner() != null ? collection.getOwner() : "System");

                        GeneratedApiEntity generatedApi = sourceRequestIdToApiEntityMap.get(requestData.sourceRequestId);
                        if (generatedApi != null) {
                            dto.setApiCode(generatedApi.getApiCode());
                        }

                        endpoints.add(dto);
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Request ID: {}, Built {} endpoints in {} ms", requestId, endpoints.size(), duration);

        } catch (Exception e) {
            log.error("Request ID: {}, Error in getAllEndpointsOptimized: {}", requestId, e.getMessage(), e);
        }

        return endpoints;
    }

    // ============================================================
    // PAGINATED COLLECTIONS
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public PaginatedResponseDTO<DashboardCollectionDTO> getCollectionsPaginated(
            String requestId, HttpServletRequest req, String performedBy, Pageable pageable) {

        log.info("Request ID: {}, Getting collections paginated: page={}, size={}",
                requestId, pageable.getPageNumber(), pageable.getPageSize());

        var collectionsList = collectionsService.getCollectionsList(requestId, req, performedBy);
        List<DashboardCollectionDTO> allCollections = new ArrayList<>();

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
            allCollections.add(dto);
        }

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allCollections.size());
        List<DashboardCollectionDTO> paginatedContent = start > allCollections.size() ?
                Collections.emptyList() : allCollections.subList(start, end);

        return PaginatedResponseDTO.<DashboardCollectionDTO>builder()
                .content(paginatedContent)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(allCollections.size())
                .totalPages((int) Math.ceil((double) allCollections.size() / pageable.getPageSize()))
                .first(pageable.getPageNumber() == 0)
                .last(pageable.getPageNumber() >= (int) Math.ceil((double) allCollections.size() / pageable.getPageSize()) - 1)
                .empty(paginatedContent.isEmpty())
                .build();
    }

    // ============================================================
    // PAGINATED ACTIVITIES
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public PaginatedResponseDTO<ActivityDTO> getActivitiesPaginated(
            String requestId, HttpServletRequest req, String performedBy,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {

        log.info("Request ID: {}, Getting activities paginated: from={}, to={}", requestId, from, to);

        List<ActivityDTO> allActivities = getAllActivities(requestId, req, performedBy);

        // Apply date filters
        Stream<ActivityDTO> stream = allActivities.stream();

        if (from != null) {
            stream = stream.filter(a -> {
                try {
                    LocalDateTime activityTime = LocalDateTime.parse(a.getTimestamp());
                    return !activityTime.isBefore(from);
                } catch (Exception e) {
                    return true;
                }
            });
        }

        if (to != null) {
            stream = stream.filter(a -> {
                try {
                    LocalDateTime activityTime = LocalDateTime.parse(a.getTimestamp());
                    return !activityTime.isAfter(to);
                } catch (Exception e) {
                    return true;
                }
            });
        }

        List<ActivityDTO> filteredList = stream.collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        List<ActivityDTO> paginatedContent = start > filteredList.size() ?
                Collections.emptyList() : filteredList.subList(start, end);

        return PaginatedResponseDTO.<ActivityDTO>builder()
                .content(paginatedContent)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(filteredList.size())
                .totalPages((int) Math.ceil((double) filteredList.size() / pageable.getPageSize()))
                .first(pageable.getPageNumber() == 0)
                .last(pageable.getPageNumber() >= (int) Math.ceil((double) filteredList.size() / pageable.getPageSize()) - 1)
                .empty(paginatedContent.isEmpty())
                .build();
    }

    // ============================================================
    // GLOBAL SEARCH
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public SearchResponseDTO globalSearch(String requestId, HttpServletRequest req,
                                          String performedBy, String query,
                                          String types, Pageable pageable) {

        log.info("Request ID: {}, Global search: query={}, types={}", requestId, query, types);

        List<SearchResultDTO> allResults = new ArrayList<>();
        Map<String, Long> countsByType = new HashMap<>();

        Set<String> searchTypes = types != null ?
                new HashSet<>(Arrays.asList(types.split(","))) :
                new HashSet<>(Arrays.asList("collections", "endpoints", "users"));

        // Search collections
        if (searchTypes.contains("collections")) {
            var collections = getRecentCollections(requestId, req, performedBy, Integer.MAX_VALUE);
            List<SearchResultDTO> collectionResults = collections.stream()
                    .filter(c -> matchesQuery(c.getName(), query) || matchesQuery(c.getDescription(), query))
                    .map(c -> SearchResultDTO.builder()
                            .id(c.getId())
                            .type("collection")
                            .title(c.getName())
                            .description(c.getDescription())
                            .url("/collections/" + c.getId())
                            .build())
                    .collect(Collectors.toList());

            countsByType.put("collections", (long) collectionResults.size());
            allResults.addAll(collectionResults);
        }

        // Search endpoints
        if (searchTypes.contains("endpoints")) {
            var endpoints = getAllEndpointsOptimized(requestId, req, performedBy).stream()
                    .filter(e -> matchesQuery(e.getName(), query) ||
                            matchesQuery(e.getDescription(), query) ||
                            matchesQuery(e.getUrl(), query) ||
                            matchesQuery(e.getMethod(), query))
                    .map(e -> SearchResultDTO.builder()
                            .id(e.getId())
                            .type("endpoint")
                            .title(e.getName() + " (" + e.getMethod() + ")")
                            .description(e.getDescription())
                            .url("/collections/" + e.getCollectionId() + "/requests/" + e.getId())
                            .metadata(Map.of(
                                    "method", e.getMethod(),
                                    "collectionName", e.getCollectionName()
                            ))
                            .build())
                    .collect(Collectors.toList());

            countsByType.put("endpoints", (long) endpoints.size());
            allResults.addAll(endpoints);
        }

        // Apply pagination to combined results
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResults.size());
        List<SearchResultDTO> paginatedResults = start > allResults.size() ?
                Collections.emptyList() : allResults.subList(start, end);

        return SearchResponseDTO.builder()
                .query(query)
                .results(paginatedResults)
                .totalResults(allResults.size())
                .countsByType(countsByType)
                .build();
    }

    // ============================================================
    // RECENT COLLECTIONS
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public List<DashboardCollectionDTO> getRecentCollections(
            String requestId, HttpServletRequest req, String performedBy, int limit) {

        var collectionsList = collectionsService.getCollectionsList(requestId, req, performedBy);

        return collectionsList.getCollections().stream()
                .limit(limit)
                .map(collection -> {
                    DashboardCollectionDTO dto = new DashboardCollectionDTO();
                    dto.setId(collection.getId());
                    dto.setName(collection.getName());
                    dto.setDescription(collection.getDescription());
                    dto.setRequestsCount(collection.getRequestsCount());
                    dto.setFolderCount(collection.getFolderCount());
                    dto.setFavorite(collection.isFavorite());
                    dto.setOwner(collection.getOwner());
                    dto.setLastUpdated(collection.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    // RECENT ENDPOINTS
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public List<DashboardEndpointDTO> getRecentEndpoints(
            String requestId, HttpServletRequest req, String performedBy, int limit) {

        List<DashboardEndpointDTO> allEndpoints = getAllEndpointsOptimized(requestId, req, performedBy);

        return allEndpoints.stream()
                .sorted((e1, e2) -> {
                    String date1 = getMostRecentDate(e1);
                    String date2 = getMostRecentDate(e2);
                    if (date1 == null && date2 == null) return 0;
                    if (date1 == null) return 1;
                    if (date2 == null) return -1;
                    return date2.compareTo(date1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ============================================================
    // RECENT ACTIVITIES
    // ============================================================
    @Transactional(readOnly = true, timeout = 20)
    public List<ActivityDTO> getRecentActivities(
            String requestId, HttpServletRequest req, String performedBy, int limit) {

        List<ActivityDTO> allActivities = getAllActivities(requestId, req, performedBy);

        return allActivities.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 4. RATE LIMIT RULES OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 15)
    public DashboardRateLimitRulesResponseDTO getDashboardRateLimitRules(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardRateLimitRulesResponseDTO(rules, rulesResponse.getStatistics());
    }

    // ============================================================
    // 5. IP WHITELIST OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 15)
    public DashboardIpWhitelistResponseDTO getDashboardIpWhitelist(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardIpWhitelistResponseDTO(entries, whitelistResponse.getAnalysis());
    }

    // ============================================================
    // 6. LOAD BALANCERS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 15)
    public DashboardLoadBalancersResponseDTO getDashboardLoadBalancers(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardLoadBalancersResponseDTO(loadBalancers, lbResponse.getPerformance());
    }

    // ============================================================
    // 7. SECURITY EVENTS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 15)
    public DashboardSecurityEventsResponseDTO getDashboardSecurityEvents(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardSecurityEventsResponseDTO(events, eventsResponse.getInsights());
    }

    // ============================================================
    // 8. SECURITY ALERTS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 15)
    public DashboardSecurityAlertsResponseDTO getDashboardSecurityAlerts(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardSecurityAlertsResponseDTO(alerts, Math.toIntExact(alertsResponse.getUnread()));
    }

    // ============================================================
    // 9. CODE LANGUAGES OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 15)
    public DashboardLanguagesResponseDTO getDashboardLanguages(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardLanguagesResponseDTO(languages);
    }

    // ============================================================
    // 10. DOCUMENTATION OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public DashboardDocumentationResponseDTO getDashboardDocumentation(String requestId, HttpServletRequest req, String performedBy) {
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
                var published = documentationService.getPublishedDocumentation(collection.getId());
                dto.setPublished(!published.isEmpty());
                if (!published.isEmpty()) {
                    dto.setPublishedUrl(published.get(0).getPublishedUrl());
                    dto.setPublishedAt(published.get(0).getPublishedAt() != null ? published.get(0).getPublishedAt().toString() : null);
                }
                docs.add(dto);
            } catch (Exception e) {
                log.warn("Could not get documentation for collection {}: {}", collection.getId(), e.getMessage());
            }
        }
        return new DashboardDocumentationResponseDTO(docs);
    }

    // ============================================================
    // 11. MOCK SERVERS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 30)
    public DashboardMockServersResponseDTO getDashboardMockServers(String requestId, HttpServletRequest req, String performedBy) {
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
                    dto.setCreatedAt(mockServer.getCreatedAt() != null ? mockServer.getCreatedAt().toString() : null);
                    dto.setExpiresAt(mockServer.getExpiresAt() != null ? mockServer.getExpiresAt().toString() : null);
                    mockServers.add(dto);
                }
            } catch (Exception e) {
                log.debug("No mock server for collection {}: {}", collection.getId(), e.getMessage());
            }
        }
        return new DashboardMockServersResponseDTO(mockServers);
    }

    // ============================================================
    // 12. USERS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 20)
    public DashboardUsersResponseDTO getDashboardUsers(String requestId, HttpServletRequest req,
                                                       String performedBy, int page, int size) {
        var usersResponse = userManagementService.getUsersList(requestId, performedBy, null, null, null, null, null, page, size);
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
        return new DashboardUsersResponseDTO(users, usersResponse.getTotal(), usersResponse.getPage(),
                usersResponse.getPageSize(), usersResponse.getTotalPages(), usersResponse.getStats());
    }

    // ============================================================
    // 13. USER ACTIVITIES OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 20)
    public DashboardUserActivitiesResponseDTO getDashboardUserActivities(String requestId, HttpServletRequest req,
                                                                         String performedBy, int limit) {
        List<ActivityDTO> allActivities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        try {
            var userActivity = userManagementService.getUserActivity(requestId, performedBy, performedBy,
                    Date.from(now.minusDays(7).atZone(java.time.ZoneId.systemDefault()).toInstant()), new Date());
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
        allActivities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        if (allActivities.size() > limit) {
            allActivities = allActivities.subList(0, limit);
        }
        return new DashboardUserActivitiesResponseDTO(allActivities);
    }

    // ============================================================
    // 14. SECURITY SUMMARY
    // ============================================================
    @Transactional(readOnly = true, timeout = 20)
    public DashboardSecuritySummaryResponseDTO getDashboardSecuritySummary(String requestId, HttpServletRequest req, String performedBy) {
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
        var rateLimitRules = apiSecurityService.getRateLimitRules(requestId, performedBy);
        response.setActiveRateLimitRules(rateLimitRules.getRules().size());
        var ipWhitelist = apiSecurityService.getIPWhitelist(requestId, performedBy);
        response.setActiveIpWhitelistEntries(ipWhitelist.getEntries().size());
        var securityAlerts = apiSecurityService.getSecurityAlerts(requestId, performedBy);
        response.setUnreadAlerts(Math.toIntExact(securityAlerts.getUnread()));
        return response;
    }

    // ============================================================
    // 15. ENVIRONMENTS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 20)
    public DashboardEnvironmentsResponseDTO getDashboardEnvironments(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardEnvironmentsResponseDTO(envDtos);
    }

    // ============================================================
    // 16. NOTIFICATIONS OVERVIEW
    // ============================================================
    @Transactional(readOnly = true, timeout = 15)
    public DashboardNotificationsResponseDTO getDashboardNotifications(String requestId, HttpServletRequest req, String performedBy) {
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
        return new DashboardNotificationsResponseDTO(notificationDtos, (int) unreadCount);
    }

    // ============================================================
    // 17. COMPREHENSIVE DASHBOARD
    // ============================================================
    @Transactional(readOnly = true, timeout = 60)
    public ComprehensiveDashboardResponseDTO getComprehensiveDashboard(String requestId, HttpServletRequest req, String performedBy) {
        long startTime = System.currentTimeMillis();
        log.info("Request ID: {}, Getting comprehensive dashboard data for user: {}", requestId, performedBy);
        ComprehensiveDashboardResponseDTO response = new ComprehensiveDashboardResponseDTO();
        response.setStats(getDashboardStats(requestId, req, performedBy));
        response.setCollections(getDashboardCollections(requestId, req, performedBy));
        response.setEndpoints(getDashboardEndpoints(requestId, req, performedBy));
        response.setRateLimitRules(getDashboardRateLimitRules(requestId, req, performedBy));
        response.setIpWhitelist(getDashboardIpWhitelist(requestId, req, performedBy));
        response.setLoadBalancers(getDashboardLoadBalancers(requestId, req, performedBy));
        response.setSecurityEvents(getDashboardSecurityEvents(requestId, req, performedBy));
        response.setSecurityAlerts(getDashboardSecurityAlerts(requestId, req, performedBy));
        response.setSecuritySummary(getDashboardSecuritySummary(requestId, req, performedBy));
        response.setLanguages(getDashboardLanguages(requestId, req, performedBy));
        response.setDocumentation(getDashboardDocumentation(requestId, req, performedBy));
        response.setMockServers(getDashboardMockServers(requestId, req, performedBy));
        response.setEnvironments(getDashboardEnvironments(requestId, req, performedBy));
        response.setUsers(getDashboardUsers(requestId, req, performedBy, 1, 10));
        response.setUserActivities(getDashboardUserActivities(requestId, req, performedBy, 20));
        response.setNotifications(getDashboardNotifications(requestId, req, performedBy));
        response.setGeneratedFor(performedBy);
        response.setRequestId(requestId);
        long duration = System.currentTimeMillis() - startTime;
        log.info("Request ID: {}, Retrieved comprehensive dashboard data in {} ms", requestId, duration);
        return response;
    }

    // ============================================================
    // AUTH CONFIG METHODS
    // ============================================================
    @Transactional(readOnly = true, timeout = 10)
    public ApiAuthConfigDTO getAuthConfigByApiId(String requestId, String performedBy, String apiId) {
        log.info("Request ID: {}, Getting auth config for API ID: {} by user: {}", requestId, apiId, performedBy);
        try {
            Optional<ApiAuthConfigEntity> authConfigOpt = authConfigRepository.findByGeneratedApiId(apiId);
            if (authConfigOpt.isPresent()) {
                ApiAuthConfigEntity entity = authConfigOpt.get();
                return ApiAuthConfigDTO.fromEntity(entity, apiId);
            } else {
                return ApiAuthConfigDTO.builder()
                        .apiId(apiId)
                        .isConfigured(false)
                        .authType("none")
                        .enableRateLimiting(false)
                        .corsCredentials(false)
                        .validateSession(false)
                        .checkObjectPrivileges(false)
                        .oauthScopes(new ArrayList<>())
                        .requiredRoles(new ArrayList<>())
                        .build();
            }
        } catch (Exception e) {
            log.error("Request ID: {}, Error fetching auth config for API {}: {}", requestId, apiId, e.getMessage(), e);
            return ApiAuthConfigDTO.builder()
                    .apiId(apiId)
                    .isConfigured(false)
                    .authType("none")
                    .enableRateLimiting(false)
                    .corsCredentials(false)
                    .validateSession(false)
                    .checkObjectPrivileges(false)
                    .oauthScopes(new ArrayList<>())
                    .requiredRoles(new ArrayList<>())
                    .build();
        }
    }


    // ============================================================
// PAGINATED ENDPOINTS WITH FILTERING
// ============================================================
    @Transactional(readOnly = true, timeout = 45)
    public PaginatedResponseDTO<DashboardEndpointDTO> getEndpointsPaginated(
            String requestId, HttpServletRequest req, String performedBy,
            EndpointFilterDTO filter, int page, int size, String sortBy, String sortDir) {

        long startTime = System.currentTimeMillis();
        log.info("Request ID: {}, Getting endpoints paginated: filter={}, page={}, size={}, sortBy={}, sortDir={}",
                requestId, filter, page, size, sortBy, sortDir);

        // Get all endpoints using optimized method
        List<DashboardEndpointDTO> allEndpoints = getAllEndpointsOptimized(requestId, req, performedBy);

        // Apply filters
        Stream<DashboardEndpointDTO> stream = allEndpoints.stream();

        if (filter != null) {
            if (filter.getCollectionId() != null && !filter.getCollectionId().isEmpty()) {
                stream = stream.filter(e -> filter.getCollectionId().equals(e.getCollectionId()));
            }
            if (filter.getMethod() != null && !filter.getMethod().isEmpty()) {
                stream = stream.filter(e -> filter.getMethod().equalsIgnoreCase(e.getMethod()));
            }
            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String searchLower = filter.getSearch().toLowerCase();
                stream = stream.filter(e ->
                        (e.getName() != null && e.getName().toLowerCase().contains(searchLower)) ||
                                (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchLower)) ||
                                (e.getUrl() != null && e.getUrl().toLowerCase().contains(searchLower)) ||
                                (e.getMethod() != null && e.getMethod().toLowerCase().contains(searchLower))
                );
            }
        }

        List<DashboardEndpointDTO> filteredList = stream.collect(Collectors.toList());

        // Apply sorting
        if (sortBy == null || sortBy.isEmpty() || "lastUpdated".equals(sortBy)) {
            String direction = (sortBy != null && !sortBy.isEmpty()) ? sortDir : "desc";
            filteredList.sort((e1, e2) -> {
                String date1 = getMostRecentDate(e1);
                String date2 = getMostRecentDate(e2);

                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1;
                if (date2 == null) return -1;

                try {
                    LocalDateTime dt1 = LocalDateTime.parse(date1, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    LocalDateTime dt2 = LocalDateTime.parse(date2, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    int result = dt1.compareTo(dt2);
                    return "desc".equalsIgnoreCase(direction) ? -result : result;
                } catch (Exception e) {
                    int result = date1.compareTo(date2);
                    return "desc".equalsIgnoreCase(direction) ? -result : result;
                }
            });
        } else {
            Comparator<DashboardEndpointDTO> comparator = getComparator(sortBy, sortDir);
            filteredList.sort(comparator);
        }

        // Apply pagination
        int totalElements = filteredList.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<DashboardEndpointDTO> paginatedContent;
        if (start >= totalElements || start < 0) {
            paginatedContent = Collections.emptyList();
        } else {
            paginatedContent = filteredList.subList(start, end);
        }

        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        long duration = System.currentTimeMillis() - startTime;
        log.info("Request ID: {}, Retrieved {} filtered endpoints ({} total) in {} ms",
                requestId, paginatedContent.size(), totalElements, duration);

        return PaginatedResponseDTO.<DashboardEndpointDTO>builder()
                .content(paginatedContent)
                .page(page)
                .size(paginatedContent.size())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(paginatedContent.isEmpty())
                .build();
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private List<ActivityDTO> getAllActivities(String requestId, HttpServletRequest req, String performedBy) {
        List<ActivityDTO> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        try {
            var userActivity = userManagementService.getUserActivity(requestId, performedBy, performedBy,
                    Date.from(now.minusDays(7).atZone(java.time.ZoneId.systemDefault()).toInstant()), new Date());
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
                activities.add(dto);
            }
        } catch (Exception e) {
            log.error("Failed to get user activities: {}", e.getMessage());
        }
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return activities;
    }

    private String getMostRecentDate(DashboardEndpointDTO endpoint) {
        String lastUpdated = endpoint.getLastUpdated();
        String createdAt = endpoint.getCreatedAt();

        if (lastUpdated == null && createdAt == null) return null;
        if (lastUpdated == null) return createdAt;
        if (createdAt == null) return lastUpdated;

        try {
            LocalDateTime lastUpdatedTime = LocalDateTime.parse(lastUpdated, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime createdAtTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return lastUpdatedTime.isAfter(createdAtTime) ? lastUpdated : createdAt;
        } catch (Exception e) {
            return lastUpdated.compareTo(createdAt) > 0 ? lastUpdated : createdAt;
        }
    }

    private String getMostRecentDateFromStrings(String lastModified, String createdAt) {
        if (lastModified == null && createdAt == null) return null;
        if (lastModified == null) return createdAt;
        if (createdAt == null) return lastModified;

        try {
            LocalDateTime lastModifiedTime = LocalDateTime.parse(lastModified, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime createdAtTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return lastModifiedTime.isAfter(createdAtTime) ? lastModified : createdAt;
        } catch (Exception e) {
            return lastModified.compareTo(createdAt) > 0 ? lastModified : createdAt;
        }
    }

    private Comparator<DashboardEndpointDTO> getComparator(String sortBy, String sortDir) {
        Comparator<DashboardEndpointDTO> comparator;
        switch (sortBy) {
            case "lastUpdated":
                comparator = (e1, e2) -> {
                    String date1 = getMostRecentDate(e1);
                    String date2 = getMostRecentDate(e2);
                    if (date1 == null && date2 == null) return 0;
                    if (date1 == null) return 1;
                    if (date2 == null) return -1;
                    try {
                        LocalDateTime dt1 = LocalDateTime.parse(date1, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        LocalDateTime dt2 = LocalDateTime.parse(date2, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        return dt1.compareTo(dt2);
                    } catch (Exception e) {
                        return date1.compareTo(date2);
                    }
                };
                break;
            case "name":
                comparator = Comparator.comparing(DashboardEndpointDTO::getName, Comparator.nullsLast(String::compareTo));
                break;
            case "method":
                comparator = Comparator.comparing(DashboardEndpointDTO::getMethod, Comparator.nullsLast(String::compareTo));
                break;
            case "collectionName":
                comparator = Comparator.comparing(DashboardEndpointDTO::getCollectionName, Comparator.nullsLast(String::compareTo));
                break;
            default:
                comparator = Comparator.comparing(DashboardEndpointDTO::getName, Comparator.nullsLast(String::compareTo));
                break;
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private boolean matchesQuery(String field, String query) {
        return field != null && field.toLowerCase().contains(query.toLowerCase());
    }

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