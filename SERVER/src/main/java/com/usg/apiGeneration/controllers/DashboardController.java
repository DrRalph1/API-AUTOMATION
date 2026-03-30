package com.usg.apiGeneration.controllers;

import com.usg.apiGeneration.dtos.dashboard.*;
import com.usg.apiGeneration.helpers.JwtHelper;
import com.usg.apiGeneration.services.DashboardService;
import com.usg.apiGeneration.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/plx/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "DASHBOARD", description = "Endpoints for dashboard statistics and monitoring")
public class DashboardController {

    private final DashboardService dashboardService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    private ResponseEntity<?> validateAuth(HttpServletRequest req, String action, String requestId) {
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, action);
        if (authValidation != null) {
            loggerUtil.log("dashboard", "Request ID: " + requestId + ", Authorization failed for " + action);
        }
        return authValidation;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data, String requestId) {
        Map<String, Object> response = new HashMap<>();
        response.put("responseCode", 200);
        response.put("message", message);
        response.put("data", data);
        response.put("requestId", requestId);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String requestId, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("responseCode", status.value());
        response.put("message", message);
        response.put("requestId", requestId);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    // ============================================================
    // 1. DASHBOARD STATS (Fast, lightweight)
    // ============================================================
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getDashboardStats(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting dashboard statistics", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardStatsResponseDTO stats = dashboardService.getDashboardStats(requestId, req, performedBy);

            loggerUtil.log("dashboard", "Request ID: " + requestId + ", Stats retrieved successfully");
            return ResponseEntity.ok(createSuccessResponse("Dashboard statistics retrieved successfully", stats, requestId));
        } catch (Exception e) {
            loggerUtil.log("dashboard", "Request ID: " + requestId + ", Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting statistics: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // ============================================================
    // 2. COLLECTIONS with Pagination
    // ============================================================
    @GetMapping("/collections")
    @Operation(summary = "Get collections with pagination", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "page", description = "Page number (0-based)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size", in = ParameterIn.QUERY),
            @Parameter(name = "sortBy", description = "Sort field", in = ParameterIn.QUERY),
            @Parameter(name = "sortDir", description = "Sort direction", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getCollections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting collections", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            PaginatedResponseDTO<DashboardCollectionDTO> collections =
                    dashboardService.getCollectionsPaginated(requestId, req, performedBy, pageable);

            return ResponseEntity.ok(createSuccessResponse("Collections retrieved successfully", collections, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting collections: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // ============================================================
    // 3. ENDPOINTS with Pagination and Filtering
    // ============================================================
    @GetMapping("/endpoints")
    @Operation(summary = "Get endpoints with pagination and filtering", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "page", description = "Page number (0-based)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size", in = ParameterIn.QUERY),
            @Parameter(name = "collectionId", description = "Filter by collection", in = ParameterIn.QUERY),
            @Parameter(name = "method", description = "Filter by HTTP method", in = ParameterIn.QUERY),
            @Parameter(name = "search", description = "Search term", in = ParameterIn.QUERY),
            @Parameter(name = "sortBy", description = "Sort field (lastUpdated, createdAt, updatedAt, name, method, etc.)", in = ParameterIn.QUERY),
            @Parameter(name = "sortDir", description = "Sort direction (asc/desc)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getEndpoints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String collectionId,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "lastUpdated") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting endpoints", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);

            // Create filter DTO
            EndpointFilterDTO filter = EndpointFilterDTO.builder()
                    .collectionId(collectionId)
                    .method(method)
                    .search(search)
                    .build();

            // Get paginated endpoints with sorting
            PaginatedResponseDTO<DashboardEndpointDTO> endpoints =
                    dashboardService.getEndpointsPaginated(
                            requestId, req, performedBy, filter,
                            page, size, sortBy, sortDir);

            return ResponseEntity.ok(createSuccessResponse("Endpoints retrieved successfully", endpoints, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting endpoints: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // ============================================================
    // 4. RECENT ACTIVITIES with Pagination
    // ============================================================
    @GetMapping("/activities")
    @Operation(summary = "Get recent activities with pagination", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "page", description = "Page number (0-based)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size", in = ParameterIn.QUERY),
            @Parameter(name = "from", description = "Start date", in = ParameterIn.QUERY),
            @Parameter(name = "to", description = "End date", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting activities", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

            PaginatedResponseDTO<ActivityDTO> activities =
                    dashboardService.getActivitiesPaginated(requestId, req, performedBy, from, to, pageable);

            return ResponseEntity.ok(createSuccessResponse("Activities retrieved successfully", activities, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting activities: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // ============================================================
    // 5. SECURITY SUMMARY
    // ============================================================
    @GetMapping("/security/summary")
    @Operation(summary = "Get security summary", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getSecuritySummary(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting security summary", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardSecuritySummaryResponseDTO summary = dashboardService.getDashboardSecuritySummary(requestId, req, performedBy);

            return ResponseEntity.ok(createSuccessResponse("Security summary retrieved successfully", summary, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting security summary: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }



    // ============================================================
    // 11. LANGUAGES (Code Generation)
    // ============================================================
    @GetMapping("/languages")
    @Operation(summary = "Get supported languages", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getLanguages(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting languages", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardLanguagesResponseDTO languages = dashboardService.getDashboardLanguages(requestId, req, performedBy);

            return ResponseEntity.ok(createSuccessResponse("Languages retrieved successfully", languages, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting languages: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


    // ============================================================
    // 13. ENVIRONMENTS
    // ============================================================
    @GetMapping("/environments")
    @Operation(summary = "Get environments", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getEnvironments(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting environments", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            DashboardEnvironmentsResponseDTO environments = dashboardService.getDashboardEnvironments(requestId, req, performedBy);

            return ResponseEntity.ok(createSuccessResponse("Environments retrieved successfully", environments, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting environments: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


    // ============================================================
    // 16. SEARCH (Global search across all sections)
    // ============================================================
    @GetMapping("/search")
    @Operation(summary = "Global search across dashboard", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "q", description = "Search query", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "types", description = "Comma-separated types to search (collections,endpoints,users)", in = ParameterIn.QUERY),
            @Parameter(name = "page", in = ParameterIn.QUERY),
            @Parameter(name = "size", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> globalSearch(
            @RequestParam("q") String query,
            @RequestParam(required = false) String types,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "global search", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Pageable pageable = PageRequest.of(page, size);

            SearchResponseDTO searchResults = dashboardService.globalSearch(
                    requestId, req, performedBy, query, types, pageable);

            return ResponseEntity.ok(createSuccessResponse("Search completed successfully", searchResults, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error during search: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // ============================================================
    // 17. LIGHTWEIGHT INITIAL DASHBOARD (Fast initial load)
    // ============================================================
    @GetMapping("/initial")
    @Operation(summary = "Get lightweight initial dashboard data", parameters = {
            @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getInitialDashboard(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = validateAuth(req, "getting initial dashboard", requestId);
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);

            // Only fetch essential data for initial render
            InitialDashboardResponseDTO initialData = InitialDashboardResponseDTO.builder()
                    .stats(dashboardService.getDashboardStats(requestId, req, performedBy))
                    .recentCollections(dashboardService.getRecentCollections(requestId, req, performedBy, 3))
                    .recentEndpoints(dashboardService.getRecentEndpoints(requestId, req, performedBy, 5))
                    .recentActivities(dashboardService.getRecentActivities(requestId, req, performedBy, 5))
                    .securitySummary(dashboardService.getDashboardSecuritySummary(requestId, req, performedBy))
                    .build();

            return ResponseEntity.ok(createSuccessResponse("Initial dashboard data retrieved successfully", initialData, requestId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting initial dashboard: " + e.getMessage(), requestId, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}