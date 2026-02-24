package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.OracleSchemaService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/plx/api/oracle/schema")
@RequiredArgsConstructor
@Tag(name = "ORACLE SCHEMA BROWSER", description = "Endpoints for browsing Oracle database schema")
public class OracleSchemaController {

    private final OracleSchemaService oracleSchemaService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET ALL TABLES
    // ============================================================
    @GetMapping("/tables")
    @Operation(summary = "Get all Oracle tables",
            description = "Retrieves all tables from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tables retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllTables(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle tables");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle tables");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle tables for user: " + performedBy);

            Map<String, Object> tables = oracleSchemaService.getAllTables(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle tables retrieved successfully");
            response.put("data", tables);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle tables retrieved successfully, count: " + tables.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET TABLES BY SCHEMA
    // ============================================================
    @GetMapping("/tables/by-schema")
    @Operation(summary = "Get tables by schema",
            description = "Retrieves all tables from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTablesBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle tables by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle tables for schema: " + schemaName);

            Map<String, Object> tables = oracleSchemaService.getTablesBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle tables retrieved successfully for schema: " + schemaName);
            response.put("data", tables);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tables for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET TABLE DETAILS
    // ============================================================
    @GetMapping("/tables/{tableName}/details")
    @Operation(summary = "Get table details",
            description = "Retrieves detailed information about a specific Oracle table including columns, constraints, and indexes",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "tableName", description = "Table name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getTableDetails(
            @PathVariable String tableName,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle table details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle table: " + tableName);

            Map<String, Object> details = oracleSchemaService.getTableDetails(requestId, req, performedBy, tableName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle table details retrieved successfully for: " + tableName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. SEARCH TABLES
    // ============================================================
    @GetMapping("/tables/search")
    @Operation(summary = "Search tables",
            description = "Searches for Oracle tables by name pattern",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "pattern", description = "Search pattern (e.g., 'EMP%')",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchTables(
            @RequestParam String pattern,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching Oracle tables");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Searching Oracle tables with pattern: " + pattern);

            Map<String, Object> searchResults = oracleSchemaService.searchTables(requestId, req, performedBy, pattern);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle tables searched successfully");
            response.put("data", searchResults);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error searching Oracle tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching Oracle tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. GET TABLE STATISTICS
    // ============================================================
    @GetMapping("/tables/{tableName}/statistics")
    @Operation(summary = "Get table statistics",
            description = "Retrieves statistics for a specific Oracle table",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "tableName", description = "Table name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getTableStatistics(
            @PathVariable String tableName,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle table statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting statistics for Oracle table: " + tableName);

            Map<String, Object> statistics = oracleSchemaService.getTableStatistics(requestId, req, performedBy, tableName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle table statistics retrieved successfully for: " + tableName);
            response.put("data", statistics);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting statistics for Oracle table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. GET TABLES WITH ROW COUNT
    // ============================================================
    @GetMapping("/tables/with-row-count")
    @Operation(summary = "Get tables with row count",
            description = "Retrieves all Oracle tables with their estimated row counts",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getTablesWithRowCount(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle tables with row count");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle tables with row count for user: " + performedBy);

            Map<String, Object> tables = oracleSchemaService.getTablesWithRowCount(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle tables with row count retrieved successfully");
            response.put("data", tables);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tables with row count: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting tables with row count: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. GET TABLESPACE STATISTICS
    // ============================================================
    @GetMapping("/tablespace-stats")
    @Operation(summary = "Get tablespace statistics",
            description = "Retrieves table count and statistics by tablespace",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getTablespaceStats(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle tablespace statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle tablespace statistics for user: " + performedBy);

            Map<String, Object> stats = oracleSchemaService.getTableCountByTablespace(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle tablespace statistics retrieved successfully");
            response.put("data", stats);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tablespace statistics: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting tablespace statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. GET RECENT TABLES
    // ============================================================
    @GetMapping("/tables/recent")
    @Operation(summary = "Get recent tables",
            description = "Retrieves tables created or modified in the last N days",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "days", description = "Number of days (default: 7)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getRecentTables(
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting recent Oracle tables");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting recent Oracle tables from last " + days + " days");

            Map<String, Object> tables = oracleSchemaService.getRecentTables(requestId, req, performedBy, days);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Recent Oracle tables retrieved successfully");
            response.put("data", tables);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting recent Oracle tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting recent tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. HEALTH CHECK
    // ============================================================
    @GetMapping("/health")
    @Operation(summary = "Oracle schema browser health check",
            description = "Checks the health status of the Oracle schema browser service",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> healthCheck(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);

            Map<String, Object> healthData = new HashMap<>();
            healthData.put("status", "UP");
            healthData.put("timestamp", java.time.LocalDateTime.now().toString());
            healthData.put("service", "OracleSchemaBrowserService");
            healthData.put("database", "Oracle");
            healthData.put("version", "1.0.0");
            healthData.put("checkedBy", performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle schema browser service is healthy");
            response.put("data", healthData);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Health check failed: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 503);
            errorResponse.put("message", "Oracle schema browser service is unhealthy: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
}