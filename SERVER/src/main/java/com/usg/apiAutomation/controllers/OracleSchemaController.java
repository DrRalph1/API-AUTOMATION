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
@Tag(name = "ORACLE SCHEMA BROWSER", description = "Endpoints for browsing Oracle database schema including tables, views, procedures, functions, packages, triggers, synonyms, sequences, types, and database links")
public class OracleSchemaController {

    private final OracleSchemaService oracleSchemaService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. SCHEMA INFO ENDPOINT
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/current-user")
    @Operation(summary = "Get current user",
            description = "Retrieves the current Oracle database user",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getCurrentUser(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting current user");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting current user for: " + performedBy);

            Map<String, Object> result = oracleSchemaService.getCurrentUserInfo(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting current user: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting current user: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. TABLE ENDPOINTS - LEGACY FORMAT
    // ============================================================

    @GetMapping("/tables")
    @Operation(summary = "Get all Oracle tables",
            description = "Retrieves all tables from the current Oracle schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
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

            Map<String, Object> result = oracleSchemaService.getAllTables(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTablesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tables for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTableDetails(requestId, req, performedBy, tableName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.searchTables(requestId, req, performedBy, pattern);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error searching Oracle tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching Oracle tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTableStatistics(requestId, req, performedBy, tableName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting statistics for Oracle table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTablesWithRowCount(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tables with row count: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting tables with row count: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTableCountByTablespace(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle tablespace statistics: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting tablespace statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getRecentTables(requestId, req, performedBy, days);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting recent Oracle tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting recent tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. TABLE ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/tables/paginated")
    @Operation(summary = "Get paginated tables (Frontend format)",
            description = "Retrieves tables with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllTablesForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated tables for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated tables for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllTablesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/tables/{tableName}/details/paginated")
    @Operation(summary = "Get table details with paginated columns (Frontend format)",
            description = "Retrieves detailed information about a specific Oracle table with paginated columns",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "tableName", description = "Table name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of columns per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTableDetailsForFrontendPaginated(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting table details with paginated columns");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated table details for: " + tableName + ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getTableDetailsForFrontendPaginated(
                    requestId, req, performedBy, tableName, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated table details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated table details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
                    @Parameter(name = "page", description = "Page number (1-based)",
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
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

            Map<String, Object> result = oracleSchemaService.getTableData(
                    requestId, req, performedBy, tableName, page, pageSize, sortColumn, sortDirection);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting data for Oracle table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/tables/{tableName}/data/advanced")
    @Operation(summary = "Get table data with advanced pagination",
            description = "Retrieves paginated data from a specific Oracle table with sorting and filtering",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "tableName", description = "Table name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of rows per page",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "sortColumn", description = "Column to sort by",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "sortDirection", description = "Sort direction (ASC/DESC)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "filter", description = "Filter condition (e.g., column=value)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTableDataAdvanced(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sortColumn,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String filter,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting advanced table data");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting advanced data for Oracle table: " + tableName +
                    ", page: " + page + ", pageSize: " + pageSize + ", filter: " + filter);

            Map<String, Object> result = oracleSchemaService.getTableDataAdvanced(
                    requestId, req, performedBy, tableName, page, pageSize, sortColumn, sortDirection, filter);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting advanced data for Oracle table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting advanced table data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/tables/{tableName}/columns")
    @Operation(summary = "Get table columns with pagination",
            description = "Retrieves paginated columns for a specific Oracle table",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "tableName", description = "Table name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Table owner (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of columns per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTableColumnsPaginated(
            @PathVariable String tableName,
            @RequestParam(required = false) String owner,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting table columns");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated columns for table: " + tableName);

            Map<String, Object> result = oracleSchemaService.getTableColumnsPaginated(
                    requestId, req, performedBy, tableName, owner, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting columns for table " + tableName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table columns: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. VIEW ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllViews(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle views: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getViewsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle views for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getViewDetails(requestId, req, performedBy, viewName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle view " + viewName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting view details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. VIEW ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/views/paginated")
    @Operation(summary = "Get paginated views (Frontend format)",
            description = "Retrieves views with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllViewsForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated views for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated views for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllViewsForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated views: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. PROCEDURE ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllProcedures(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle procedures: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getProceduresBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle procedures for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getProcedureDetails(requestId, req, performedBy, procedureName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle procedure " + procedureName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting procedure details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/procedures/{procedureName}/parameters")
    @Operation(summary = "Get procedure parameters with pagination",
            description = "Retrieves paginated parameters for a specific Oracle procedure",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "procedureName", description = "Procedure name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Procedure owner (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of parameters per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getProcedureParametersPaginated(
            @PathVariable String procedureName,
            @RequestParam(required = false) String owner,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting procedure parameters");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated parameters for procedure: " + procedureName);

            Map<String, Object> result = oracleSchemaService.getProcedureParametersPaginated(
                    requestId, req, performedBy, procedureName, owner, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting parameters for procedure " + procedureName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting procedure parameters: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. PROCEDURE ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/procedures/paginated")
    @Operation(summary = "Get paginated procedures (Frontend format)",
            description = "Retrieves procedures with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllProceduresForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated procedures for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated procedures for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllProceduresForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated procedures: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. FUNCTION ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllFunctions(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle functions: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getFunctionsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle functions for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getFunctionDetails(requestId, req, performedBy, functionName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle function " + functionName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting function details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/functions/{functionName}/parameters")
    @Operation(summary = "Get function parameters with pagination",
            description = "Retrieves paginated parameters for a specific Oracle function",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "functionName", description = "Function name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Function owner (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of parameters per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getFunctionParametersPaginated(
            @PathVariable String functionName,
            @RequestParam(required = false) String owner,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting function parameters");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated parameters for function: " + functionName);

            Map<String, Object> result = oracleSchemaService.getFunctionParametersPaginated(
                    requestId, req, performedBy, functionName, owner, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting parameters for function " + functionName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting function parameters: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. FUNCTION ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/functions/paginated")
    @Operation(summary = "Get paginated functions (Frontend format)",
            description = "Retrieves functions with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllFunctionsForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated functions for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated functions for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllFunctionsForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated functions: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. PACKAGE ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllPackages(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle packages: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle packages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getPackagesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle packages for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle packages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getPackageDetails(requestId, req, performedBy, packageName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle package " + packageName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting package details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/packages/{packageName}/items")
    @Operation(summary = "Get package items with pagination",
            description = "Retrieves paginated procedures, functions, and variables from a package",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "packageName", description = "Package name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Package owner (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "itemType", description = "Item type (PROCEDURE, FUNCTION, VARIABLE, ALL)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getPackageItemsPaginated(
            @PathVariable String packageName,
            @RequestParam(required = false) String owner,
            @RequestParam(defaultValue = "ALL") String itemType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting package items");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated items for package: " + packageName + ", type: " + itemType);

            Map<String, Object> result = oracleSchemaService.getPackageItemsPaginated(
                    requestId, req, performedBy, packageName, owner, itemType, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting items for package " + packageName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting package items: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. PACKAGE ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/packages/paginated")
    @Operation(summary = "Get paginated packages (Frontend format)",
            description = "Retrieves packages with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllPackagesForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated packages for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated packages for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllPackagesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated packages: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated packages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. SEQUENCE ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllSequences(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle sequences: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getSequencesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle sequences for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getSequenceDetails(requestId, req, performedBy, sequenceName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle sequence " + sequenceName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting sequence details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. SEQUENCE ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/sequences/paginated")
    @Operation(summary = "Get paginated sequences (Frontend format)",
            description = "Retrieves sequences with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllSequencesForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated sequences for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated sequences for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllSequencesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated sequences: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 14. SYNONYM ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllSynonyms(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle synonyms: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getSynonymsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle synonyms for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 15. SYNONYM ENDPOINTS - ENHANCED FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
    public ResponseEntity<?> getSynonymDetailsEnhanced(
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

            Map<String, Object> result = oracleSchemaService.getSynonymDetailsEnhanced(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle synonym " + synonymName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting synonym details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/{synonymName}/target")
    @Operation(summary = "Get synonym target details",
            description = "Retrieves lazy loaded target details for a synonym",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "synonymName", description = "Synonym name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getSynonymTargetDetails(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting synonym target details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting target details for synonym: " + synonymName);

            Map<String, Object> result = oracleSchemaService.getSynonymTargetDetails(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting target details for synonym: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting synonym target details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 16. SYNONYM ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/synonyms/paginated")
    @Operation(summary = "Get paginated synonyms (Frontend format)",
            description = "Retrieves synonyms with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllSynonymsForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated synonyms for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated synonyms for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllSynonymsForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated synonyms: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 17. TYPE ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllTypes(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle types: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTypesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle types for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTypeDetails(requestId, req, performedBy, typeName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle type " + typeName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting type details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 18. TYPE ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/types/paginated")
    @Operation(summary = "Get paginated types (Frontend format)",
            description = "Retrieves types with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllTypesForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated types for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated types for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllTypesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated types: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 19. TRIGGER ENDPOINTS - LEGACY FORMAT
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

            Map<String, Object> result = oracleSchemaService.getAllTriggers(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle triggers: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTriggersBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle triggers for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getTriggerDetails(requestId, req, performedBy, triggerName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for Oracle trigger " + triggerName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting trigger details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 20. TRIGGER ENDPOINTS - FRONTEND FORMAT
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/triggers/paginated")
    @Operation(summary = "Get paginated triggers (Frontend format)",
            description = "Retrieves triggers with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllTriggersForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated triggers for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated triggers for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getAllTriggersForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated triggers: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 21. DATABASE LINK ENDPOINTS
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

            Map<String, Object> result = oracleSchemaService.getAllDbLinks(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle database links: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle database links: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getDbLinksBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle database links for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle database links: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 22. GENERAL OBJECT ENDPOINTS
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

            Map<String, Object> result = oracleSchemaService.getAllObjects(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getObjectsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle objects for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting Oracle objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.searchObjects(requestId, req, performedBy, pattern);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error searching Oracle objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching Oracle objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getObjectCountByType(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle object count by type: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object count by type: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getInvalidObjects(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting invalid Oracle objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting invalid objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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

            Map<String, Object> result = oracleSchemaService.getObjectsByStatus(requestId, req, performedBy, status);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting Oracle objects with status " + status + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting objects by status: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 23. OBJECT DETAILS ENDPOINTS
    // ============================================================

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/details/paginated")
    @Operation(summary = "Get paginated object details",
            description = "Retrieves detailed information about a specific Oracle object with pagination support",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE, etc.)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Object owner (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "includeCounts", description = "Include total counts without fetching all data",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getObjectDetailsPaginated(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "false") boolean includeCounts,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated object details");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting paginated object details");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated details for Oracle " + objectType + ": " + objectName +
                    ", page: " + page + ", pageSize: " + pageSize + ", includeCounts: " + includeCounts);

            Map<String, Object> result = oracleSchemaService.getObjectDetailsPaginated(
                    requestId, req, performedBy, objectName, objectType, owner, page, pageSize, includeCounts);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated details for Oracle " + objectType + " " + objectName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated object details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/counts")
    @Operation(summary = "Get object counts only",
            description = "Retrieves only the counts for an object (columns, parameters, etc.) without fetching all data",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Object owner (optional)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getObjectCountsOnly(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object counts");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting object counts");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting counts only for " + objectType + ": " + objectName);

            Map<String, Object> result = oracleSchemaService.getObjectCountsOnly(
                    requestId, req, performedBy, objectName, objectType, owner);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting counts for " + objectType + " " + objectName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object counts: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/ddl")
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
                    requestId, req, performedBy, objectName, objectType);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting DDL for Oracle " + objectType + " " + objectName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object DDL: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 24. SEARCH ENDPOINTS - ENHANCED
    // ============================================================

    @GetMapping("/search/paginated")
    @Operation(summary = "Paginated search",
            description = "Searches for Oracle objects with pagination support",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "query", description = "Search query",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "type", description = "Search type (ALL, TABLE, VIEW, PROCEDURE, etc.)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of results per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchObjectsPaginated(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "paginated search");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for paginated search");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Paginated search with query: " + query + ", type: " + type +
                    ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.searchObjectsPaginated(
                    requestId, req, performedBy, query, type, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error in paginated search: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred during paginated search: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/search/paginated")
    @Operation(summary = "Paginated search for frontend",
            description = "Searches for Oracle objects with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "query", description = "Search query",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "type", description = "Search type (ALL, TABLE, VIEW, PROCEDURE, etc.)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of results per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchObjectsForFrontendPaginated(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "paginated search for frontend");
        if (authValidation != null) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for paginated search for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Paginated search for frontend with query: " + query + ", type: " + type +
                    ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.searchObjectsForFrontendPaginated(
                    requestId, req, performedBy, query, type, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error in paginated search for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred during paginated search for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 25. RECENT OBJECTS ENDPOINTS
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

            Map<String, Object> result = oracleSchemaService.getRecentTables(requestId, req, performedBy, days);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting recent objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting recent objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 26. EXECUTE QUERY ENDPOINT
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
                    ((Number) queryRequest.get("timeoutSeconds")).intValue() : 30;
            boolean readOnly = queryRequest.get("readOnly") != null ?
                    (Boolean) queryRequest.get("readOnly") : true;

            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Executing query for user: " + performedBy + ", readOnly: " + readOnly);

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
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 27. DIAGNOSTICS ENDPOINT
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

            Map<String, Object> result = oracleSchemaService.diagnoseDatabase(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error diagnosing Oracle database: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while diagnosing Oracle database: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 28. PAGINATED OBJECT LISTS FOR FRONTEND
    // ============================================================

    @GetMapping("/tables/filtered/paginated")
    @Operation(summary = "Get paginated tables (Frontend format)",
            description = "Retrieves tables with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTablesPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated tables");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated tables, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getTablesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/views/filtered/paginated")
    @Operation(summary = "Get paginated views (Frontend format)",
            description = "Retrieves views with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getViewsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated views");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated views, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getViewsPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated views: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/procedures/filtered/paginated")
    @Operation(summary = "Get paginated procedures (Frontend format)",
            description = "Retrieves procedures with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getProceduresPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated procedures");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated procedures, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getProceduresPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated procedures: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/functions/filtered/paginated")
    @Operation(summary = "Get paginated functions (Frontend format)",
            description = "Retrieves functions with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getFunctionsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated functions");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated functions, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getFunctionsPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated functions: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/packages/filtered/paginated")
    @Operation(summary = "Get paginated packages (Frontend format)",
            description = "Retrieves packages with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getPackagesPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated packages");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated packages, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getPackagesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated packages: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated packages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/filtered/paginated")
    @Operation(summary = "Get paginated synonyms (Frontend format)",
            description = "Retrieves synonyms with pagination support in a frontend-friendly format - optimized for large datasets",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getSynonymsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated synonyms");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated synonyms, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getSynonymsPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated synonyms: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sequences/filtered/paginated")
    @Operation(summary = "Get paginated sequences (Frontend format)",
            description = "Retrieves sequences with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getSequencesPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated sequences");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated sequences, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getSequencesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated sequences: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/types/filtered/paginated")
    @Operation(summary = "Get paginated types (Frontend format)",
            description = "Retrieves types with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTypesPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated types");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated types, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getTypesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated types: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/triggers/filtered/paginated")
    @Operation(summary = "Get paginated triggers (Frontend format)",
            description = "Retrieves triggers with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTriggersPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated triggers");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated triggers, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.getTriggersPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated triggers: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/counts")
    @Operation(summary = "Get all object counts",
            description = "Retrieves total counts for all object types - very fast",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllObjectCounts(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object counts");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Getting all object counts");

            Map<String, Object> result = oracleSchemaService.getAllObjectCounts(
                    requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting object counts: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object counts: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/synonyms/{synonymName}/resolve-target")
    @Operation(summary = "Resolve synonym target",
            description = "Resolves a single synonym's target type (lazy loading)",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "synonymName", description = "Synonym name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> resolveSynonymTarget(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "resolving synonym target");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Resolving synonym target: " + synonymName);

            Map<String, Object> result = oracleSchemaService.resolveSynonymTarget(
                    requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error resolving synonym target: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while resolving synonym: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 29. HEALTH CHECK ENDPOINT
    // ============================================================

    @GetMapping("/health")
    @Operation(summary = "Health check",
            description = "Simple health check endpoint to verify the service is running",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> healthCheck(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "health check");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "UP");
            result.put("service", "Oracle Schema Browser");
            result.put("timestamp", java.time.Instant.now().toString());
            result.put("requestId", requestId);
            result.put("performedBy", performedBy);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 30. HELPER ENDPOINTS
    // ============================================================

    @GetMapping("/supported-object-types")
    @Operation(summary = "Get supported object types",
            description = "Retrieves a list of all supported Oracle object types",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getSupportedObjectTypes(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting supported object types");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);

            Map<String, Object> result = new HashMap<>();
            result.put("objectTypes", new String[]{
                    "TABLE", "VIEW", "PROCEDURE", "FUNCTION", "PACKAGE",
                    "SYNONYM", "SEQUENCE", "TRIGGER", "TYPE", "INDEX",
                    "CONSTRAINT", "DB_LINK", "MATERIALIZED_VIEW"
            });
            result.put("responseCode", 200);
            result.put("message", "Supported object types retrieved successfully");
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
// NEW: SEARCH ACROSS MULTIPLE OBJECT TYPES WITH PAGINATION
// ============================================================

    @GetMapping("/frontend/search/combined")
    @Operation(summary = "Search across multiple object types",
            description = "Searches for objects across specified types (e.g., procedures AND synonyms) with pagination",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "query", description = "Search query",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "types", description = "Comma-separated object types (e.g., 'PROCEDURE,SYNONYM')",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchCombinedTypes(
            @RequestParam String query,
            @RequestParam String types,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "combined search");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Combined search with query: " + query + ", types: " + types +
                    ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.searchCombinedTypes(
                    requestId, req, performedBy, query, types, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error in combined search: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred during combined search: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

// ============================================================
// NEW: SEARCH PROCEDURES AND THEIR SYNONYMS TOGETHER
// ============================================================

    @GetMapping("/frontend/procedures/with-synonyms/search")
    @Operation(summary = "Search procedures including their synonyms",
            description = "Searches for procedures AND synonyms that target procedures with pagination",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "query", description = "Search query",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchProceduresWithSynonyms(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "search procedures with synonyms");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Searching procedures with synonyms, query: " + query +
                    ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = oracleSchemaService.searchProceduresWithSynonyms(
                    requestId, req, performedBy, query, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error searching procedures with synonyms: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

// ============================================================
// NEW: GET COUNT FOR SEARCH RESULTS (Fast)
// ============================================================

    @GetMapping("/frontend/search/count")
    @Operation(summary = "Get search result count only",
            description = "Returns only the count of matching objects (very fast)",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "query", description = "Search query",
                            required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "types", description = "Comma-separated object types",
                            required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getSearchCount(
            @RequestParam String query,
            @RequestParam String types,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "get search count");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);

            int count = oracleSchemaService.getSearchCount(query, types);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("query", query);
            result.put("types", types);
            result.put("responseCode", 200);
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("oracleSchema", "RequestEntity ID: " + requestId +
                    ", Error getting search count: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}