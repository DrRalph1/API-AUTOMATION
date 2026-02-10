package com.usg.apiAutomation.controllers.systemActivities;

import com.usg.apiAutomation.dtos.systemActivities.systemLogs.LogEntriesResponse;
import com.usg.apiAutomation.dtos.systemActivities.systemLogs.LogEntry;
import com.usg.apiAutomation.dtos.systemActivities.systemLogs.LogFileContentResponse;
import com.usg.apiAutomation.dtos.systemActivities.systemLogs.LogFileResponse;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.systemActivities.SystemLogService;
import com.usg.apiAutomation.utils.JwtUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/plx/api/")
@RequiredArgsConstructor
//@Tag(name = "SYSTEM LOGS", description = "Endpoints for managing and retrieving systemActivities logs")
@Tag(name = "SYSTEM ACTIVITIES", description = "System-level endpoints")
public class SystemLogController {

    private final SystemLogService systemLogService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;
    private final JwtUtil jwtUtil;

    // Helper method to create consistent error response
    private Map<String, Object> createErrorResponse(int responseCode, String message, String requestId) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("responseCode", responseCode);
        errorResponse.put("message", message);
        errorResponse.put("requestId", requestId);
        return errorResponse;
    }

    // Helper method to create success response
    private Map<String, Object> createSuccessResponse(int responseCode, String message, Object data, String requestId) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("responseCode", responseCode);
        successResponse.put("message", message);
        successResponse.put("data", data);
        successResponse.put("requestId", requestId);
        return successResponse;
    }

    // Get All Log Files
    @GetMapping("/logs/files")
    @Operation(summary = "Get All Log Files", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log files successfully retrieved"),
            @ApiResponse(responseCode = "204", description = "No log files found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllLogFiles(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all log files");
        if (authValidation != null) {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Authorization failed for getting all log files");
            return authValidation;
        }

        // Extract user from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String userId = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId +
                            ", Getting all log files" +
                            ", Requested by: " + userId);

            // Call the service
            List<LogFileResponse> logFiles = systemLogService.getLogFiles(requestId, req, userId);

            if (logFiles.isEmpty()) {
                loggerUtil.log("web-application-firewall",
                        "Request ID: " + requestId + ", No log files found");

                Map<String, Object> noContentResponse = createSuccessResponse(204, "No log files found in the systemActivities", null, requestId);
                noContentResponse.put("totalFiles", 0);
                return ResponseEntity.ok(noContentResponse);
            }

            Map<String, Object> successResponse = createSuccessResponse(200, "Log files retrieved successfully", logFiles, requestId);
            successResponse.put("totalFiles", logFiles.size());

            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Get all log files completed. Total files: " + logFiles.size());

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("Request ID: {}, Error getting log files: {}", requestId, e.getMessage());
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Error getting log files: " + e.getMessage());

            Map<String, Object> errorResponse = createErrorResponse(500,
                    "Failed to fetch log files: " + e.getMessage(), requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get Log Entries with Filtering and Pagination
    @GetMapping("/logs/entries")
    @Operation(summary = "Get Log Entries", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "search", description = "Search term", in = ParameterIn.QUERY),
            @Parameter(name = "severity", description = "Filter by severity level", in = ParameterIn.QUERY),
            @Parameter(name = "timeFilter", description = "Time range filter", in = ParameterIn.QUERY),
            @Parameter(name = "page", description = "Page number (0-based)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log entries successfully retrieved"),
            @ApiResponse(responseCode = "204", description = "No log entries found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getLogEntries(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "all") String severity,
            @RequestParam(required = false, defaultValue = "all") String timeFilter,
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting log entries");
        if (authValidation != null) {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Authorization failed for getting log entries");
            return authValidation;
        }

        // Extract user from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String userId = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId +
                            ", Getting log entries - Search: " + search +
                            ", Severity: " + severity +
                            ", TimeFilter: " + timeFilter +
                            ", Page: " + pageable.getPageNumber() +
                            ", Size: " + pageable.getPageSize() +
                            ", Requested by: " + userId);

            // Call the service
            LogEntriesResponse logEntries = systemLogService.getLogEntries(search, severity, timeFilter, pageable.getPageNumber() + 1, pageable.getPageSize(), requestId, req, userId);

            if (logEntries.getLogs().isEmpty()) {
                loggerUtil.log("web-application-firewall",
                        "Request ID: " + requestId + ", No log entries found matching criteria");

                Map<String, Object> noContentResponse = createSuccessResponse(204, "No log entries found matching the specified criteria", null, requestId);
                return ResponseEntity.ok(noContentResponse);
            }

            Map<String, Object> successResponse = createSuccessResponse(200, "Log entries retrieved successfully", logEntries, requestId);
            successResponse.put("pagination", Map.of(
                    "page_number", pageable.getPageNumber(),
                    "page_size", pageable.getPageSize(),
                    "total_elements", logEntries.getTotalItems(),
                    "total_pages", logEntries.getTotalPages()
            ));

            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Get log entries completed. Total elements: " +
                            logEntries.getTotalItems() + ", Total pages: " + logEntries.getTotalPages());

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("Request ID: {}, Error getting log entries: {}", requestId, e.getMessage());
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Error getting log entries: " + e.getMessage());

            Map<String, Object> errorResponse = createErrorResponse(500,
                    "Failed to fetch log entries: " + e.getMessage(), requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get Log File Content
    @GetMapping("/logs/files/{filename}/content")
    @Operation(summary = "Get Log File Content", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "filename", description = "Name of the log file", required = true, in = ParameterIn.PATH),
            @Parameter(name = "search", description = "Search term within file", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log file content successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Log file not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getLogFileContent(
            @PathVariable String filename,
            @RequestParam(required = false, defaultValue = "") String search,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting log file content");
        if (authValidation != null) {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Authorization failed for getting log file content: " + filename);
            return authValidation;
        }

        // Extract user from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String userId = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId +
                            ", Getting log file content - Filename: " + filename +
                            ", Search: " + search +
                            ", Requested by: " + userId);

            // Call the service
            LogFileContentResponse fileContent = systemLogService.getLogFileContent(filename, search, requestId, req, userId);

            if (fileContent.getContent().contains("File not found") || fileContent.getContent().contains("Error reading file")) {
                loggerUtil.log("web-application-firewall",
                        "Request ID: " + requestId + ", Log file not found: " + filename);

                Map<String, Object> errorResponse = createErrorResponse(404,
                        "Log file not found or cannot be read: " + filename, requestId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> successResponse = createSuccessResponse(200, "Log file content retrieved successfully", fileContent, requestId);

            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Get log file content completed. Filename: " + filename +
                            ", Content length: " + fileContent.getContent().length());

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("Request ID: {}, Error getting log file content: {}", requestId, e.getMessage());
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Error getting log file content: " + e.getMessage());

            Map<String, Object> errorResponse = createErrorResponse(500,
                    "Failed to fetch log file content: " + e.getMessage(), requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Export Logs to CSV
    @GetMapping("/logs/export")
    @Operation(summary = "Export Logs to CSV", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "search", description = "Search term", in = ParameterIn.QUERY),
            @Parameter(name = "severity", description = "Filter by severity level", in = ParameterIn.QUERY),
            @Parameter(name = "timeFilter", description = "Time range filter", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs successfully exported to CSV"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> exportLogs(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "all") String severity,
            @RequestParam(required = false, defaultValue = "all") String timeFilter,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting logs to CSV");
        if (authValidation != null) {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Authorization failed for exporting logs to CSV");
            return authValidation;
        }

        // Extract user from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String userId = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId +
                            ", Exporting logs to CSV - Search: " + search +
                            ", Severity: " + severity +
                            ", TimeFilter: " + timeFilter +
                            ", Requested by: " + userId);

            // Call the service
            String csvContent = systemLogService.exportLogsToCsv(search, severity, timeFilter, requestId, req, userId);
            Path tempFile = Files.createTempFile("waf_logs_export", ".csv");
            Files.write(tempFile, csvContent.getBytes());

            Resource resource = new UrlResource(tempFile.toUri());

            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Export logs to CSV completed. File size: " + csvContent.length());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=system_logs_export.csv")
                    .body(resource);

        } catch (Exception e) {
            log.error("Request ID: {}, Error exporting logs to CSV: {}", requestId, e.getMessage());
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Error exporting logs to CSV: " + e.getMessage());

            Map<String, Object> errorResponse = createErrorResponse(500,
                    "Failed to export logs to CSV: " + e.getMessage(), requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get Log Statistics
    @GetMapping("/logs/statistics")
    @Operation(summary = "Get Log Statistics", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log statistics successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getLogStatistics(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting log statistics");
        if (authValidation != null) {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Authorization failed for getting log statistics");
            return authValidation;
        }

        // Extract user from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String userId = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId +
                            ", Getting log statistics" +
                            ", Requested by: " + userId);

            // Call the service
            Map<String, Object> statistics = systemLogService.getLogStatistics(requestId, req, userId);

            Map<String, Object> successResponse = createSuccessResponse(200, "Log statistics retrieved successfully", statistics, requestId);

            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Get log statistics completed");

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("Request ID: {}, Error getting log statistics: {}", requestId, e.getMessage());
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Error getting log statistics: " + e.getMessage());

            Map<String, Object> errorResponse = createErrorResponse(500,
                    "Failed to fetch log statistics: " + e.getMessage(), requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Search Logs with Pagination
    @GetMapping("/logs/search")
    @Operation(summary = "Search Logs", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "query", description = "Search query", in = ParameterIn.QUERY),
            @Parameter(name = "page", description = "Page number (0-based)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs successfully searched"),
            @ApiResponse(responseCode = "204", description = "No logs found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchLogs(
            @RequestParam(required = false, defaultValue = "") String query,
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching logs");
        if (authValidation != null) {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Authorization failed for searching logs");
            return authValidation;
        }

        // Extract user from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String userId = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId +
                            ", Searching logs - Query: " + query +
                            ", Page: " + pageable.getPageNumber() +
                            ", Size: " + pageable.getPageSize() +
                            ", Requested by: " + userId);

            // Call the service
            Page<LogEntry> searchResults = systemLogService.searchLogs(query, pageable, requestId, req, userId);

            if (searchResults.isEmpty()) {
                loggerUtil.log("web-application-firewall",
                        "Request ID: " + requestId + ", No logs found for query: " + query);

                Map<String, Object> noContentResponse = createSuccessResponse(204, "No logs found for the given query", null, requestId);
                return ResponseEntity.ok(noContentResponse);
            }

            Map<String, Object> successResponse = createSuccessResponse(200, "Logs searched successfully", searchResults.getContent(), requestId);
            successResponse.put("pagination", Map.of(
                    "page_number", searchResults.getNumber(),
                    "page_size", searchResults.getSize(),
                    "total_elements", searchResults.getTotalElements(),
                    "total_pages", searchResults.getTotalPages(),
                    "is_first", searchResults.isFirst(),
                    "is_last", searchResults.isLast()
            ));

            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Search logs completed. Found " + searchResults.getTotalElements() +
                            " logs matching the query");

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("Request ID: {}, Error searching logs: {}", requestId, e.getMessage());
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Error searching logs: " + e.getMessage());

            Map<String, Object> errorResponse = createErrorResponse(500,
                    "Failed to search logs: " + e.getMessage(), requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}