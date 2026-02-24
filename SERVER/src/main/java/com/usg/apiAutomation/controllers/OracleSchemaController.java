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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/plx/api/oracle/schema")
@RequiredArgsConstructor
@Tag(name = "ORACLE SCHEMA BROWSER", description = "Endpoints for browsing Oracle database schema including tables, views, procedures, functions, packages, triggers, synonyms, types, and database links")
public class OracleSchemaController {

    private final OracleSchemaService oracleSchemaService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. SCHEMA INFO ENDPOINTS
    // ============================================================

    @GetMapping("/info")
    @Operation(summary = "Get current schema information",
            description = "Retrieves information about the current Oracle schema including user, schema name, and database version",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getCurrentSchemaInfo(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting schema info");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting schema info");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting schema info for user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getCurrentSchemaInfo(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting schema info: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting schema info: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. TABLE ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
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

    @GetMapping("/frontend/tables")
    @Operation(summary = "Get all Oracle tables (Frontend format)",
            description = "Retrieves all tables from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTablesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle tables for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle tables for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle tables for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllTablesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tables for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle tables for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/tables/{tableName}/details")
    @Operation(summary = "Get table details (Frontend format)",
            description = "Retrieves detailed information about a specific Oracle table in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "tableName", description = "Table name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getTableDetailsForFrontend(
            @PathVariable String tableName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle table details for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle table for frontend: " + tableName);

            Map<String, Object> result = oracleSchemaService.getTableDetailsForFrontend(requestId, req, performedBy, tableName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle table for frontend " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table details for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/tables/{tableName}/data")
    @Operation(summary = "Get table data with pagination",
            description = "Retrieves paginated data from a specific Oracle table",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "tableName", description = "Table name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "page", description = "Page number (0-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of rows per page",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "sortColumn", description = "Column to sort by",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "sortDirection", description = "Sort direction (ASC/DESC)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String sortColumn,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle table data");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting data for Oracle table: " + tableName + ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getTableData(requestId, req, performedBy,
                    tableName, page, pageSize, sortColumn, sortDirection);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting data for Oracle table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. VIEW ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
    // ============================================================

    @GetMapping("/views")
    @Operation(summary = "Get all Oracle views",
            description = "Retrieves all views from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllViews(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle views");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle views");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle views for user: " + performedBy);

            Map<String, Object> views = oracleSchemaService.getAllViews(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle views retrieved successfully");
            response.put("data", views);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle views retrieved successfully, count: " + views.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle views: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/views/by-schema")
    @Operation(summary = "Get views by schema",
            description = "Retrieves all views from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getViewsBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle views by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle views for schema: " + schemaName);

            Map<String, Object> views = oracleSchemaService.getViewsBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle views retrieved successfully for schema: " + schemaName);
            response.put("data", views);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle views for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/views/{viewName}/details")
    @Operation(summary = "Get view details",
            description = "Retrieves detailed information about a specific Oracle view including columns",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "viewName", description = "View name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getViewDetails(
            @PathVariable String viewName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle view details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle view: " + viewName);

            Map<String, Object> details = oracleSchemaService.getViewDetails(requestId, req, performedBy, viewName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle view details retrieved successfully for: " + viewName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle view " + viewName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting view details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/views")
    @Operation(summary = "Get all Oracle views (Frontend format)",
            description = "Retrieves all views from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllViewsForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle views for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle views for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle views for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllViewsForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle views for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle views for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. PROCEDURE ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
    // ============================================================

    @GetMapping("/procedures")
    @Operation(summary = "Get all Oracle procedures",
            description = "Retrieves all procedures from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllProcedures(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle procedures");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle procedures");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle procedures for user: " + performedBy);

            Map<String, Object> procedures = oracleSchemaService.getAllProcedures(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle procedures retrieved successfully");
            response.put("data", procedures);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle procedures retrieved successfully, count: " + procedures.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle procedures: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/procedures/by-schema")
    @Operation(summary = "Get procedures by schema",
            description = "Retrieves all procedures from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getProceduresBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle procedures by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle procedures for schema: " + schemaName);

            Map<String, Object> procedures = oracleSchemaService.getProceduresBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle procedures retrieved successfully for schema: " + schemaName);
            response.put("data", procedures);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle procedures for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/procedures/{procedureName}/details")
    @Operation(summary = "Get procedure details",
            description = "Retrieves detailed information about a specific Oracle procedure including parameters",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "procedureName", description = "Procedure name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getProcedureDetails(
            @PathVariable String procedureName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle procedure details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle procedure: " + procedureName);

            Map<String, Object> details = oracleSchemaService.getProcedureDetails(requestId, req, performedBy, procedureName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle procedure details retrieved successfully for: " + procedureName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle procedure " + procedureName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting procedure details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/procedures")
    @Operation(summary = "Get all Oracle procedures (Frontend format)",
            description = "Retrieves all procedures from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllProceduresForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle procedures for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle procedures for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle procedures for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllProceduresForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle procedures for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle procedures for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. FUNCTION ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
    // ============================================================

    @GetMapping("/functions")
    @Operation(summary = "Get all Oracle functions",
            description = "Retrieves all functions from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllFunctions(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle functions");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle functions");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle functions for user: " + performedBy);

            Map<String, Object> functions = oracleSchemaService.getAllFunctions(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle functions retrieved successfully");
            response.put("data", functions);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle functions retrieved successfully, count: " + functions.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle functions: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/functions/by-schema")
    @Operation(summary = "Get functions by schema",
            description = "Retrieves all functions from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getFunctionsBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle functions by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle functions for schema: " + schemaName);

            Map<String, Object> functions = oracleSchemaService.getFunctionsBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle functions retrieved successfully for schema: " + schemaName);
            response.put("data", functions);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle functions for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/functions/{functionName}/details")
    @Operation(summary = "Get function details",
            description = "Retrieves detailed information about a specific Oracle function including parameters and return type",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "functionName", description = "Function name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getFunctionDetails(
            @PathVariable String functionName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle function details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle function: " + functionName);

            Map<String, Object> details = oracleSchemaService.getFunctionDetails(requestId, req, performedBy, functionName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle function details retrieved successfully for: " + functionName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle function " + functionName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting function details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/functions")
    @Operation(summary = "Get all Oracle functions (Frontend format)",
            description = "Retrieves all functions from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllFunctionsForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle functions for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle functions for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle functions for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllFunctionsForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle functions for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle functions for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. PACKAGE ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
    // ============================================================

    @GetMapping("/packages")
    @Operation(summary = "Get all Oracle packages",
            description = "Retrieves all packages from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllPackages(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle packages");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle packages");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle packages for user: " + performedBy);

            Map<String, Object> packages = oracleSchemaService.getAllPackages(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle packages retrieved successfully");
            response.put("data", packages);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle packages retrieved successfully, count: " + packages.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle packages: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle packages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/packages/by-schema")
    @Operation(summary = "Get packages by schema",
            description = "Retrieves all packages from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getPackagesBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle packages by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle packages for schema: " + schemaName);

            Map<String, Object> packages = oracleSchemaService.getPackagesBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle packages retrieved successfully for schema: " + schemaName);
            response.put("data", packages);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle packages for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle packages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/packages/{packageName}/details")
    @Operation(summary = "Get package details",
            description = "Retrieves detailed information about a specific Oracle package including procedures, functions, and variables",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "packageName", description = "Package name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getPackageDetails(
            @PathVariable String packageName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle package details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle package: " + packageName);

            Map<String, Object> details = oracleSchemaService.getPackageDetails(requestId, req, performedBy, packageName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle package details retrieved successfully for: " + packageName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle package " + packageName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting package details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/packages")
    @Operation(summary = "Get all Oracle packages (Frontend format)",
            description = "Retrieves all packages from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllPackagesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle packages for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle packages for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle packages for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllPackagesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle packages for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle packages for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. SEQUENCE ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
    // ============================================================

    @GetMapping("/sequences")
    @Operation(summary = "Get all Oracle sequences",
            description = "Retrieves all sequences from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSequences(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle sequences");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle sequences");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle sequences for user: " + performedBy);

            Map<String, Object> sequences = oracleSchemaService.getAllSequences(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle sequences retrieved successfully");
            response.put("data", sequences);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle sequences retrieved successfully, count: " + sequences.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle sequences: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sequences/by-schema")
    @Operation(summary = "Get sequences by schema",
            description = "Retrieves all sequences from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getSequencesBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle sequences by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle sequences for schema: " + schemaName);

            Map<String, Object> sequences = oracleSchemaService.getSequencesBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle sequences retrieved successfully for schema: " + schemaName);
            response.put("data", sequences);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle sequences for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sequences/{sequenceName}/details")
    @Operation(summary = "Get sequence details",
            description = "Retrieves detailed information about a specific Oracle sequence",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "sequenceName", description = "Sequence name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getSequenceDetails(
            @PathVariable String sequenceName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle sequence details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle sequence: " + sequenceName);

            Map<String, Object> details = oracleSchemaService.getSequenceDetails(requestId, req, performedBy, sequenceName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle sequence details retrieved successfully for: " + sequenceName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle sequence " + sequenceName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting sequence details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/sequences")
    @Operation(summary = "Get all Oracle sequences (Frontend format)",
            description = "Retrieves all sequences from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSequencesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle sequences for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle sequences for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle sequences for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllSequencesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle sequences for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle sequences for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. SYNONYM ENDPOINTS - EXISTING METHODS + ENHANCED + FRONTEND
    // ============================================================

    @GetMapping("/synonyms")
    @Operation(summary = "Get all Oracle synonyms",
            description = "Retrieves all synonyms from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSynonyms(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle synonyms");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle synonyms");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle synonyms for user: " + performedBy);

            Map<String, Object> synonyms = oracleSchemaService.getAllSynonyms(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle synonyms retrieved successfully");
            response.put("data", synonyms);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle synonyms retrieved successfully, count: " + synonyms.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle synonyms: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/by-schema")
    @Operation(summary = "Get synonyms by schema",
            description = "Retrieves all synonyms from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getSynonymsBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle synonyms by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle synonyms for schema: " + schemaName);

            Map<String, Object> synonyms = oracleSchemaService.getSynonymsBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle synonyms retrieved successfully for schema: " + schemaName);
            response.put("data", synonyms);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle synonyms for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/{synonymName}/details")
    @Operation(summary = "Get synonym details",
            description = "Retrieves detailed information about a specific Oracle synonym",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "synonymName", description = "Synonym name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getSynonymDetails(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle synonym details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle synonym: " + synonymName);

            Map<String, Object> details = oracleSchemaService.getSynonymDetailsEnhanced(requestId, req, performedBy, synonymName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle synonym details retrieved successfully for: " + synonymName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle synonym " + synonymName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting synonym details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/details")
    @Operation(summary = "Get all synonyms with details",
            description = "Retrieves all synonyms with detailed information",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSynonymsWithDetails(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle synonyms with details");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle synonyms with details");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle synonyms with details for user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllSynonymsWithDetails(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle synonyms with details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting synonyms with details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/byTargetType/{targetType}")
    @Operation(summary = "Get synonyms by target type",
            description = "Retrieves synonyms filtered by target object type",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "targetType", description = "Target object type (TABLE, VIEW, PROCEDURE, etc.)",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getSynonymsByTargetType(
            @PathVariable String targetType,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle synonyms by target type");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting synonyms by target type: " + targetType);

            Map<String, Object> result = oracleSchemaService.getSynonymsByTargetType(requestId, req, performedBy, targetType);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting synonyms by target type: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting synonyms by target type: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/{synonymName}/resolve")
    @Operation(summary = "Resolve synonym",
            description = "Resolves a synonym to its target object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "synonymName", description = "Synonym name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> resolveSynonym(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "resolving Oracle synonym");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Resolving Oracle synonym: " + synonymName);

            Map<String, Object> result = oracleSchemaService.resolveSynonym(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error resolving Oracle synonym " + synonymName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while resolving synonym: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/{synonymName}/validate")
    @Operation(summary = "Validate synonym",
            description = "Validates a synonym and checks if its target exists",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "synonymName", description = "Synonym name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> validateSynonym(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating Oracle synonym");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Validating Oracle synonym: " + synonymName);

            Map<String, Object> result = oracleSchemaService.validateSynonym(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error validating Oracle synonym " + synonymName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while validating synonym: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/synonyms")
    @Operation(summary = "Get all Oracle synonyms (Frontend format)",
            description = "Retrieves all synonyms from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSynonymsForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle synonyms for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle synonyms for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle synonyms for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllSynonymsForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle synonyms for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle synonyms for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. TYPE ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
    // ============================================================

    @GetMapping("/types")
    @Operation(summary = "Get all Oracle types",
            description = "Retrieves all types from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTypes(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle types");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle types");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle types for user: " + performedBy);

            Map<String, Object> types = oracleSchemaService.getAllTypes(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle types retrieved successfully");
            response.put("data", types);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle types retrieved successfully, count: " + types.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle types: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/types/by-schema")
    @Operation(summary = "Get types by schema",
            description = "Retrieves all types from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTypesBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle types by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle types for schema: " + schemaName);

            Map<String, Object> types = oracleSchemaService.getTypesBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle types retrieved successfully for schema: " + schemaName);
            response.put("data", types);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle types for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/types/{typeName}/details")
    @Operation(summary = "Get type details",
            description = "Retrieves detailed information about a specific Oracle type including attributes and methods",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "typeName", description = "Type name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getTypeDetails(
            @PathVariable String typeName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle type details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle type: " + typeName);

            Map<String, Object> details = oracleSchemaService.getTypeDetails(requestId, req, performedBy, typeName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle type details retrieved successfully for: " + typeName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle type " + typeName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting type details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/types")
    @Operation(summary = "Get all Oracle types (Frontend format)",
            description = "Retrieves all types from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTypesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle types for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle types for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle types for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllTypesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle types for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle types for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. TRIGGER ENDPOINTS - EXISTING METHODS + FRONTEND FORMAT
    // ============================================================

    @GetMapping("/triggers")
    @Operation(summary = "Get all Oracle triggers",
            description = "Retrieves all triggers from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTriggers(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle triggers");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle triggers");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle triggers for user: " + performedBy);

            Map<String, Object> triggers = oracleSchemaService.getAllTriggers(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle triggers retrieved successfully");
            response.put("data", triggers);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle triggers retrieved successfully, count: " + triggers.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle triggers: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/triggers/by-schema")
    @Operation(summary = "Get triggers by schema",
            description = "Retrieves all triggers from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTriggersBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle triggers by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle triggers for schema: " + schemaName);

            Map<String, Object> triggers = oracleSchemaService.getTriggersBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle triggers retrieved successfully for schema: " + schemaName);
            response.put("data", triggers);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle triggers for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/triggers/{triggerName}/details")
    @Operation(summary = "Get trigger details",
            description = "Retrieves detailed information about a specific Oracle trigger",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "triggerName", description = "Trigger name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getTriggerDetails(
            @PathVariable String triggerName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle trigger details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle trigger: " + triggerName);

            Map<String, Object> details = oracleSchemaService.getTriggerDetails(requestId, req, performedBy, triggerName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle trigger details retrieved successfully for: " + triggerName);
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle trigger " + triggerName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting trigger details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/triggers")
    @Operation(summary = "Get all Oracle triggers (Frontend format)",
            description = "Retrieves all triggers from the current Oracle schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTriggersForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle triggers for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle triggers for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle triggers for frontend, user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getAllTriggersForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle triggers for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle triggers for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. DATABASE LINK ENDPOINTS - EXISTING METHODS
    // ============================================================

    @GetMapping("/db-links")
    @Operation(summary = "Get all database links",
            description = "Retrieves all database links from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllDbLinks(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle database links");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle database links");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle database links for user: " + performedBy);

            Map<String, Object> dbLinks = oracleSchemaService.getAllDbLinks(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle database links retrieved successfully");
            response.put("data", dbLinks);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle database links retrieved successfully, count: " + dbLinks.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle database links: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle database links: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/db-links/by-schema")
    @Operation(summary = "Get database links by schema",
            description = "Retrieves all database links from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getDbLinksBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle database links by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle database links for schema: " + schemaName);

            Map<String, Object> dbLinks = oracleSchemaService.getDbLinksBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle database links retrieved successfully for schema: " + schemaName);
            response.put("data", dbLinks);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle database links for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle database links: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. GENERAL OBJECT ENDPOINTS - EXISTING METHODS
    // ============================================================

    @GetMapping("/objects")
    @Operation(summary = "Get all objects",
            description = "Retrieves all objects from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllObjects(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all Oracle objects");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting all Oracle objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all Oracle objects for user: " + performedBy);

            Map<String, Object> objects = oracleSchemaService.getAllObjects(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle objects retrieved successfully");
            response.put("data", objects);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle objects retrieved successfully, count: " + objects.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/by-schema")
    @Operation(summary = "Get objects by schema",
            description = "Retrieves all objects from a specific Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "schemaName", description = "Schema name",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getObjectsBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle objects by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle objects for schema: " + schemaName);

            Map<String, Object> objects = oracleSchemaService.getObjectsBySchema(requestId, req, performedBy, schemaName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle objects retrieved successfully for schema: " + schemaName);
            response.put("data", objects);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle objects for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/search")
    @Operation(summary = "Search objects",
            description = "Searches for any Oracle objects by name pattern",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "pattern", description = "Search pattern (e.g., 'EMP%')",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchObjects(
            @RequestParam String pattern,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching Oracle objects");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for searching Oracle objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Searching Oracle objects with pattern: " + pattern);

            Map<String, Object> searchResults = oracleSchemaService.searchObjects(requestId, req, performedBy, pattern);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle objects searched successfully");
            response.put("data", searchResults);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle objects search completed, found: " + searchResults.get("totalCount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error searching Oracle objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching Oracle objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/count-by-type")
    @Operation(summary = "Get object count by type",
            description = "Retrieves count of Oracle objects grouped by type",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getObjectCountByType(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle object count by type");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle object count by type");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle object count by type for user: " + performedBy);

            Map<String, Object> objectCounts = oracleSchemaService.getObjectCountByType(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle object count by type retrieved successfully");
            response.put("data", objectCounts);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle object count by type retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle object count by type: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object count by type: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/invalid")
    @Operation(summary = "Get invalid objects",
            description = "Retrieves all invalid objects from the Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getInvalidObjects(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting invalid Oracle objects");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting invalid Oracle objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting invalid Oracle objects for user: " + performedBy);

            Map<String, Object> invalidObjects = oracleSchemaService.getInvalidObjects(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Invalid Oracle objects retrieved successfully");
            response.put("data", invalidObjects);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting invalid Oracle objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting invalid objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/by-status/{status}")
    @Operation(summary = "Get objects by status",
            description = "Retrieves all objects with a specific status",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "status", description = "Object status (VALID, INVALID, etc.)",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getObjectsByStatus(
            @PathVariable String status,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle objects by status");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle objects by status");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting Oracle objects with status: " + status);

            Map<String, Object> objects = oracleSchemaService.getObjectsByStatus(requestId, req, performedBy, status);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle objects with status " + status + " retrieved successfully");
            response.put("data", objects);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle objects with status " + status + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting objects by status: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/details")
    @Operation(summary = "Get object details",
            description = "Retrieves detailed information about a specific Oracle object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE, etc.)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Object owner (optional)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getObjectDetails(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle object details");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle object details");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for Oracle " + objectType + ": " + objectName);

            Map<String, Object> result = oracleSchemaService.getObjectDetailsByNameAndType(
                    requestId, req, performedBy, objectName, objectType, owner);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle " + objectType + " " + objectName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/validate")
    @Operation(summary = "Validate object",
            description = "Validates if an object exists and is accessible",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, etc.)",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "owner", description = "Object owner (optional)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> validateObject(
            @RequestParam String objectName,
            @RequestParam String objectType,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating Oracle object");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for validating Oracle object");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Validating Oracle object: " + objectName + " of type: " + objectType);

            Map<String, Object> result = oracleSchemaService.validateObject(
                    requestId, req, performedBy, objectName, objectType, owner);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error validating Oracle object: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while validating object: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. SEARCH ENDPOINTS - ENHANCED
    // ============================================================

    @GetMapping("/search")
    @Operation(summary = "Search Oracle objects",
            description = "Searches for Oracle objects by name pattern",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "query", description = "Search query",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "type", description = "Object type to search (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "maxResults", description = "Maximum number of results (default: 100)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchObjectsFrontend(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "100") int maxResults,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching Oracle objects");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for searching Oracle objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Searching Oracle objects with query: " + query + ", type: " + type);

            Map<String, Object> result = oracleSchemaService.searchObjectsForFrontend(
                    requestId, req, performedBy, query, type, maxResults);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error searching Oracle objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching Oracle objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/comprehensive-search")
    @Operation(summary = "Comprehensive search",
            description = "Performs comprehensive search across all objects including synonym targets",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "pattern", description = "Search pattern",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> comprehensiveSearch(
            @RequestParam String pattern,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "comprehensive search");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for comprehensive search");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Performing comprehensive search with pattern: " + pattern);

            Map<String, Object> result = oracleSchemaService.comprehensiveSearch(requestId, req, performedBy, pattern);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error in comprehensive search: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred during comprehensive search: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 14. DDL ENDPOINTS
    // ============================================================

    @GetMapping("/{objectType}s/{objectName}/ddl")
    @Operation(summary = "Get object DDL",
            description = "Retrieves the DDL for a specific Oracle object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (table, view, procedure, function, package, trigger, synonym, sequence, type)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getObjectDDL(
            @PathVariable String objectType,
            @PathVariable String objectName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle object DDL");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle object DDL");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting DDL for Oracle " + objectType + ": " + objectName);

            Map<String, Object> result = oracleSchemaService.getObjectDDL(
                    requestId, req, performedBy, objectType, objectName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting DDL for Oracle " + objectType + " " + objectName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object DDL: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/size")
    @Operation(summary = "Get object size",
            description = "Retrieves size information for a specific Oracle object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (TABLE, INDEX, etc.)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getObjectSize(
            @PathVariable String objectType,
            @PathVariable String objectName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting Oracle object size");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting Oracle object size");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting size for Oracle " + objectType + ": " + objectName);

            Map<String, Object> result = oracleSchemaService.getObjectSize(
                    requestId, req, performedBy, objectName, objectType);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting size for Oracle " + objectType + " " + objectName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object size: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 15. RECENT OBJECTS ENDPOINTS
    // ============================================================

    @GetMapping("/recent/{days}")
    @Operation(summary = "Get recent objects",
            description = "Retrieves objects modified in the last N days",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "days", description = "Number of days",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getRecentObjects(
            @PathVariable int days,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting recent objects");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting recent objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting objects modified in last " + days + " days");

            Map<String, Object> result = oracleSchemaService.getRecentObjects(requestId, req, performedBy, days);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting recent objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting recent objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 16. EXECUTE QUERY ENDPOINT
    // ============================================================

    @PostMapping("/execute")
    @Operation(summary = "Execute SQL query",
            description = "Executes a SQL query against the Oracle database",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> executeQuery(
            @RequestBody Map<String, Object> queryRequest,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "executing Oracle query");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for executing Oracle query");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            String query = (String) queryRequest.get("query");
            int timeoutSeconds = queryRequest.get("timeoutSeconds") != null ?
                    (int) queryRequest.get("timeoutSeconds") : 30;
            boolean readOnly = queryRequest.get("readOnly") != null ?
                    (boolean) queryRequest.get("readOnly") : true;

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Executing query for user: " + performedBy);

            Map<String, Object> result = oracleSchemaService.executeQuery(
                    requestId, req, performedBy, query, timeoutSeconds, readOnly);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error executing query: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while executing query: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 17. DIAGNOSTICS ENDPOINT
    // ============================================================

    @GetMapping("/diagnose")
    @Operation(summary = "Run database diagnostics",
            description = "Runs comprehensive diagnostics on the Oracle database connection and schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> diagnoseDatabase(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "diagnosing Oracle database");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for diagnosing Oracle database");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Running Oracle database diagnostics for user: " + performedBy);

            Map<String, Object> diagnostics = oracleSchemaService.diagnoseDatabase(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Oracle database diagnostics completed");
            response.put("data", diagnostics);
            response.put("requestId", requestId);

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Oracle database diagnostics completed with status: " + diagnostics.get("diagnosticStatus"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error diagnosing Oracle database: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while diagnosing Oracle database: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}