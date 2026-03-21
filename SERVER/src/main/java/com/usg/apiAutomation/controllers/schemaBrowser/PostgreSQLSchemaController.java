package com.usg.apiAutomation.controllers.schemaBrowser;

import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.schemaBrowser.PostgreSQLSchemaService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
@RequestMapping("/plx/api/postgresql/schema")
@RequiredArgsConstructor
@Tag(name = "POSTGRESQL SCHEMA BROWSER", description = "Endpoints for browsing PostgreSQL database schema including tables, views, procedures, functions, sequences, triggers, types etc.")
public class PostgreSQLSchemaController {

    private final PostgreSQLSchemaService postgresqlSchemaService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. SCHEMA INFO ENDPOINTS
    // ============================================================

    @GetMapping("/info")
    @Operation(summary = "Get current schema information",
            description = "Retrieves information about the current PostgreSQL schema including user, schema name, and database version",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getCurrentSchemaInfo(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting schema info");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting schema info");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting schema info for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getCurrentSchemaInfo(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            description = "Retrieves the current PostgreSQL database user",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting current user for: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getCurrentUserInfo(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    @Operation(summary = "Get all PostgreSQL tables",
            description = "Retrieves all tables from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTables(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL tables");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL tables");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL tables for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllTables(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/tables/by-schema")
    @Operation(summary = "Get tables by schema",
            description = "Retrieves all tables from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL tables by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL tables for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getTablesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL tables for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/tables/{tableName}/details")
    @Operation(summary = "Get table details",
            description = "Retrieves detailed information about a specific PostgreSQL table including columns, constraints, and indexes",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL table details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL table: " + tableName);

            Map<String, Object> result = postgresqlSchemaService.getTableDetails(requestId, req, performedBy, tableName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL table " + tableName + ": " + e.getMessage());

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
            description = "Searches for PostgreSQL tables by name pattern",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching PostgreSQL tables");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Searching PostgreSQL tables with pattern: " + pattern);

            Map<String, Object> result = postgresqlSchemaService.searchTables(requestId, req, performedBy, pattern);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error searching PostgreSQL tables: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching PostgreSQL tables: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/tables/{tableName}/statistics")
    @Operation(summary = "Get table statistics",
            description = "Retrieves statistics for a specific PostgreSQL table",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL table statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting statistics for PostgreSQL table: " + tableName);

            Map<String, Object> result = postgresqlSchemaService.getTableStatistics(requestId, req, performedBy, tableName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting statistics for PostgreSQL table " + tableName + ": " + e.getMessage());

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
            description = "Retrieves all PostgreSQL tables with their estimated row counts",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getTablesWithRowCount(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL tables with row count");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL tables with row count for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getTablesWithRowCount(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL tables with row count: " + e.getMessage());

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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL tablespace statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL tablespace statistics for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getTableCountByTablespace(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL tablespace statistics: " + e.getMessage());

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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting recent PostgreSQL tables");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting recent PostgreSQL tables from last " + days + " days");

            Map<String, Object> result = postgresqlSchemaService.getRecentTables(requestId, req, performedBy, days);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting recent PostgreSQL tables: " + e.getMessage());

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
    @Operation(summary = "Get all PostgreSQL tables (Frontend format)",
            description = "Retrieves all tables from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTablesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL tables for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL tables for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL tables for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllTablesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL tables for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL tables for frontend: " + e.getMessage());
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated tables for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllTablesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            description = "Retrieves detailed information about a specific PostgreSQL table in a frontend-friendly format",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL table details for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL table for frontend: " + tableName);

            Map<String, Object> result = postgresqlSchemaService.getTableDetailsForFrontend(requestId, req, performedBy, tableName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL table for frontend " + tableName + ": " + e.getMessage());

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
            description = "Retrieves detailed information about a specific PostgreSQL table with paginated columns",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated table details for: " + tableName + ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getTableDetailsForFrontendPaginated(
                    requestId, req, performedBy, tableName, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            description = "Retrieves paginated data from a specific PostgreSQL table",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL table data");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting data for PostgreSQL table: " + tableName + ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getTableData(
                    requestId, req, performedBy, tableName, page, pageSize, sortColumn, sortDirection);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting data for PostgreSQL table " + tableName + ": " + e.getMessage());

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
            description = "Retrieves paginated data from a specific PostgreSQL table with sorting and filtering",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting advanced data for PostgreSQL table: " + tableName +
                    ", page: " + page + ", pageSize: " + pageSize + ", filter: " + filter);

            Map<String, Object> result = postgresqlSchemaService.getTableDataAdvanced(
                    requestId, req, performedBy, tableName, page, pageSize, sortColumn, sortDirection, filter);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting advanced data for PostgreSQL table " + tableName + ": " + e.getMessage());

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
            description = "Retrieves paginated columns for a specific PostgreSQL table",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated columns for table: " + tableName);

            Map<String, Object> result = postgresqlSchemaService.getTableColumnsPaginated(
                    requestId, req, performedBy, tableName, owner, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    @Operation(summary = "Get all PostgreSQL views",
            description = "Retrieves all views from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllViews(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL views");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL views");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL views for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllViews(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL views: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/views/by-schema")
    @Operation(summary = "Get views by schema",
            description = "Retrieves all views from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL views by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL views for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getViewsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL views for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/views/{viewName}/details")
    @Operation(summary = "Get view details",
            description = "Retrieves detailed information about a specific PostgreSQL view including columns",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL view details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL view: " + viewName);

            Map<String, Object> result = postgresqlSchemaService.getViewDetails(requestId, req, performedBy, viewName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL view " + viewName + ": " + e.getMessage());

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
    @Operation(summary = "Get all PostgreSQL views (Frontend format)",
            description = "Retrieves all views from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllViewsForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL views for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL views for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL views for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllViewsForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL views for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL views for frontend: " + e.getMessage());
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated views for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllViewsForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    @Operation(summary = "Get all PostgreSQL procedures",
            description = "Retrieves all procedures from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllProcedures(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL procedures");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL procedures");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL procedures for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllProcedures(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL procedures: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/procedures/by-schema")
    @Operation(summary = "Get procedures by schema",
            description = "Retrieves all procedures from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL procedures by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL procedures for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getProceduresBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL procedures for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/procedures/{procedureName}/details")
    @Operation(summary = "Get procedure details",
            description = "Retrieves detailed information about a specific PostgreSQL procedure including parameters",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL procedure details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL procedure: " + procedureName);

            Map<String, Object> result = postgresqlSchemaService.getProcedureDetails(requestId, req, performedBy, procedureName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL procedure " + procedureName + ": " + e.getMessage());

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
            description = "Retrieves paginated parameters for a specific PostgreSQL procedure",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated parameters for procedure: " + procedureName);

            Map<String, Object> result = postgresqlSchemaService.getProcedureParametersPaginated(
                    requestId, req, performedBy, procedureName, owner, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    @Operation(summary = "Get all PostgreSQL procedures (Frontend format)",
            description = "Retrieves all procedures from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllProceduresForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL procedures for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL procedures for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL procedures for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllProceduresForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL procedures for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL procedures for frontend: " + e.getMessage());
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated procedures for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllProceduresForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    @Operation(summary = "Get all PostgreSQL functions",
            description = "Retrieves all functions from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllFunctions(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL functions");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL functions");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL functions for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllFunctions(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL functions: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/functions/by-schema")
    @Operation(summary = "Get functions by schema",
            description = "Retrieves all functions from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL functions by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL functions for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getFunctionsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL functions for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/functions/{functionName}/details")
    @Operation(summary = "Get function details",
            description = "Retrieves detailed information about a specific PostgreSQL function including parameters and return type",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL function details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL function: " + functionName);

            Map<String, Object> result = postgresqlSchemaService.getFunctionDetails(requestId, req, performedBy, functionName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL function " + functionName + ": " + e.getMessage());

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
            description = "Retrieves paginated parameters for a specific PostgreSQL function",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated parameters for function: " + functionName);

            Map<String, Object> result = postgresqlSchemaService.getFunctionParametersPaginated(
                    requestId, req, performedBy, functionName, owner, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    @Operation(summary = "Get all PostgreSQL functions (Frontend format)",
            description = "Retrieves all functions from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllFunctionsForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL functions for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL functions for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL functions for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllFunctionsForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL functions for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL functions for frontend: " + e.getMessage());
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated functions for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllFunctionsForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    // 10. PACKAGE ENDPOINTS (PostgreSQL doesn't have packages - return schemas instead)
    // ============================================================

    @GetMapping("/packages")
    @Operation(summary = "Get all schemas (as package equivalents)",
            description = "PostgreSQL doesn't have packages. This returns schemas which serve as the PostgreSQL equivalent for organizing objects.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllPackages(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL schemas");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL schemas");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL schemas for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllPackages(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL schemas: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL schemas: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/packages/by-schema")
    @Operation(summary = "Get schemas by name",
            description = "Retrieves a specific schema by name",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getPackagesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL schema: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/packages/{packageName}/details")
    @Operation(summary = "Get schema details",
            description = "Retrieves detailed information about a specific schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "packageName", description = "Schema name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getPackageDetails(
            @PathVariable String packageName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL schema details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL schema: " + packageName);

            Map<String, Object> result = postgresqlSchemaService.getPackageDetails(requestId, req, performedBy, packageName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL schema " + packageName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting schema details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/packages/{packageName}/items")
    @Operation(summary = "Get schema items with pagination",
            description = "Retrieves paginated items (functions, procedures, tables, views) from a schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "packageName", description = "Schema name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Schema owner (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "itemType", description = "Item type (FUNCTION, PROCEDURE, TABLE, VIEW, ALL)",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting schema items");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated items for schema: " + packageName + ", type: " + itemType);

            Map<String, Object> result = postgresqlSchemaService.getPackageItemsPaginated(
                    requestId, req, performedBy, packageName, owner, itemType, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting items for schema " + packageName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting schema items: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. PACKAGE ENDPOINTS - FRONTEND FORMAT
    // ============================================================

    @GetMapping("/frontend/packages")
    @Operation(summary = "Get all schemas (Frontend format)",
            description = "Retrieves all schemas from PostgreSQL in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllPackagesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL schemas for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL schemas for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL schemas for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllPackagesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL schemas for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL schemas for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/frontend/packages/paginated")
    @Operation(summary = "Get paginated schemas (Frontend format)",
            description = "Retrieves schemas with pagination support in a frontend-friendly format",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated schemas for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated schemas for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllPackagesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated schemas: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated schemas: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. SEQUENCE ENDPOINTS - LEGACY FORMAT
    // ============================================================

    @GetMapping("/sequences")
    @Operation(summary = "Get all PostgreSQL sequences",
            description = "Retrieves all sequences from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSequences(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL sequences");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL sequences");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL sequences for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllSequences(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL sequences: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sequences/by-schema")
    @Operation(summary = "Get sequences by schema",
            description = "Retrieves all sequences from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL sequences by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL sequences for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getSequencesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL sequences for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL sequences: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sequences/{sequenceName}/details")
    @Operation(summary = "Get sequence details",
            description = "Retrieves detailed information about a specific PostgreSQL sequence",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL sequence details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL sequence: " + sequenceName);

            Map<String, Object> result = postgresqlSchemaService.getSequenceDetails(requestId, req, performedBy, sequenceName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL sequence " + sequenceName + ": " + e.getMessage());

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
    @Operation(summary = "Get all PostgreSQL sequences (Frontend format)",
            description = "Retrieves all sequences from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSequencesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL sequences for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL sequences for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL sequences for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllSequencesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL sequences for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL sequences for frontend: " + e.getMessage());
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated sequences for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllSequencesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    // 14. SYNONYM ENDPOINTS (PostgreSQL doesn't have synonyms)
    // ============================================================

    @GetMapping("/synonyms")
    @Operation(summary = "Get synonyms (Not supported in PostgreSQL)",
            description = "PostgreSQL does not support synonyms. This endpoint returns an empty list.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllSynonyms(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL synonyms");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL synonyms");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL synonyms (not supported) for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllSynonyms(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL synonyms: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/synonyms/by-schema")
    @Operation(summary = "Get synonyms by schema (Not supported)",
            description = "PostgreSQL does not support synonyms.",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL synonyms by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL synonyms for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getSynonymsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL synonyms for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL synonyms: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 15. SYNONYM ENDPOINTS - ENHANCED FORMAT (Not supported)
    // ============================================================

    @GetMapping("/synonyms/details")
    @Operation(summary = "Get all synonyms with details (Not supported)",
            description = "PostgreSQL does not support synonyms.")
    public ResponseEntity<?> getAllSynonymsWithDetails(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL synonyms with details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getAllSynonymsWithDetails(requestId, req, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/synonyms/byTargetType/{targetType}")
    @Operation(summary = "Get synonyms by target type (Not supported)")
    public ResponseEntity<?> getSynonymsByTargetType(
            @PathVariable String targetType,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL synonyms by target type");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getSynonymsByTargetType(requestId, req, performedBy, targetType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/synonyms/{synonymName}/details")
    @Operation(summary = "Get synonym details (Not supported)")
    public ResponseEntity<?> getSynonymDetailsEnhanced(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL synonym details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getSynonymDetailsEnhanced(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/synonyms/{synonymName}/resolve")
    @Operation(summary = "Resolve synonym (Not supported)")
    public ResponseEntity<?> resolveSynonym(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "resolving PostgreSQL synonym");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.resolveSynonym(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/synonyms/{synonymName}/validate")
    @Operation(summary = "Validate synonym (Not supported)")
    public ResponseEntity<?> validateSynonym(
            @PathVariable String synonymName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating PostgreSQL synonym");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.validateSynonym(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/synonyms/{synonymName}/target")
    @Operation(summary = "Get synonym target details (Not supported)")
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
            Map<String, Object> result = postgresqlSchemaService.getSynonymTargetDetails(requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    // ============================================================
    // 16. SYNONYM ENDPOINTS - FRONTEND FORMAT (Not supported)
    // ============================================================

    @GetMapping("/frontend/synonyms")
    @Operation(summary = "Get all PostgreSQL synonyms (Frontend format) - Not supported")
    public ResponseEntity<?> getAllSynonymsForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL synonyms for frontend");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getAllSynonymsForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/frontend/synonyms/paginated")
    @Operation(summary = "Get paginated synonyms (Frontend format) - Not supported")
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
            Map<String, Object> result = postgresqlSchemaService.getAllSynonymsForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    // ============================================================
    // 17. TYPE ENDPOINTS - LEGACY FORMAT
    // ============================================================

    @GetMapping("/types")
    @Operation(summary = "Get all PostgreSQL types",
            description = "Retrieves all types from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTypes(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL types");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL types");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL types for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllTypes(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL types: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/types/by-schema")
    @Operation(summary = "Get types by schema",
            description = "Retrieves all types from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL types by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL types for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getTypesBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL types for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL types: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/types/{typeName}/details")
    @Operation(summary = "Get type details",
            description = "Retrieves detailed information about a specific PostgreSQL type including attributes",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL type details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL type: " + typeName);

            Map<String, Object> result = postgresqlSchemaService.getTypeDetails(requestId, req, performedBy, typeName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL type " + typeName + ": " + e.getMessage());

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
    @Operation(summary = "Get all PostgreSQL types (Frontend format)",
            description = "Retrieves all types from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTypesForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL types for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL types for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL types for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllTypesForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL types for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL types for frontend: " + e.getMessage());
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated types for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllTypesForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    @Operation(summary = "Get all PostgreSQL triggers",
            description = "Retrieves all triggers from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTriggers(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL triggers");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL triggers");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL triggers for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllTriggers(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL triggers: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/triggers/by-schema")
    @Operation(summary = "Get triggers by schema",
            description = "Retrieves all triggers from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL triggers by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL triggers for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getTriggersBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL triggers for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/triggers/{triggerName}/details")
    @Operation(summary = "Get trigger details",
            description = "Retrieves detailed information about a specific PostgreSQL trigger",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL trigger details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL trigger: " + triggerName);

            Map<String, Object> result = postgresqlSchemaService.getTriggerDetails(requestId, req, performedBy, triggerName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL trigger " + triggerName + ": " + e.getMessage());

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
    @Operation(summary = "Get all PostgreSQL triggers (Frontend format)",
            description = "Retrieves all triggers from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllTriggersForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL triggers for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL triggers for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL triggers for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllTriggersForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL triggers for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL triggers for frontend: " + e.getMessage());
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated triggers for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllTriggersForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
    // 21. DATABASE LINK ENDPOINTS (PostgreSQL uses FDW, but we'll return empty)
    // ============================================================

    @GetMapping("/db-links")
    @Operation(summary = "Get all database links (PostgreSQL uses Foreign Data Wrappers)",
            description = "Retrieves all foreign data wrappers from PostgreSQL",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllDbLinks(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL foreign data wrappers");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getAllDbLinks(requestId, req, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/db-links/by-schema")
    @Operation(summary = "Get database links by schema (PostgreSQL FDW)")
    public ResponseEntity<?> getDbLinksBySchema(
            @RequestParam String schemaName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL foreign data wrappers by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getDbLinksBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    // ============================================================
    // 22. GENERAL OBJECT ENDPOINTS
    // ============================================================

    @GetMapping("/objects")
    @Operation(summary = "Get all objects",
            description = "Retrieves all objects from the current PostgreSQL schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllObjects(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all PostgreSQL objects");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting all PostgreSQL objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all PostgreSQL objects for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllObjects(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/by-schema")
    @Operation(summary = "Get objects by schema",
            description = "Retrieves all objects from a specific PostgreSQL schema",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL objects by schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL objects for schema: " + schemaName);

            Map<String, Object> result = postgresqlSchemaService.getObjectsBySchema(requestId, req, performedBy, schemaName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL objects for schema " + schemaName + ": " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting PostgreSQL objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/search")
    @Operation(summary = "Search objects",
            description = "Searches for any PostgreSQL objects by name pattern",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching PostgreSQL objects");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for searching PostgreSQL objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Searching PostgreSQL objects with pattern: " + pattern);

            Map<String, Object> result = postgresqlSchemaService.searchObjects(requestId, req, performedBy, pattern);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error searching PostgreSQL objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching PostgreSQL objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/count-by-type")
    @Operation(summary = "Get object count by type",
            description = "Retrieves count of PostgreSQL objects grouped by type",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getObjectCountByType(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL object count by type");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL object count by type");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL object count by type for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getObjectCountByType(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL object count by type: " + e.getMessage());

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
            description = "Retrieves all invalid objects from the PostgreSQL schema (views/materialized views that may be broken)",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getInvalidObjects(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting invalid PostgreSQL objects");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting invalid PostgreSQL objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting invalid PostgreSQL objects for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getInvalidObjects(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting invalid PostgreSQL objects: " + e.getMessage());

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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL objects by status");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL objects by status");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting PostgreSQL objects with status: " + status);

            Map<String, Object> result = postgresqlSchemaService.getObjectsByStatus(requestId, req, performedBy, status);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting PostgreSQL objects with status " + status + ": " + e.getMessage());

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
            description = "Retrieves detailed information about a specific PostgreSQL object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, FUNCTION, etc.)",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL object details");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL object details");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting details for PostgreSQL " + objectType + ": " + objectName);

            Map<String, Object> result = postgresqlSchemaService.getObjectDetailsByNameAndType(
                    requestId, req, performedBy, objectName, objectType, owner);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting details for PostgreSQL " + objectType + " " + objectName + ": " + e.getMessage());

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
            description = "Retrieves detailed information about a specific PostgreSQL object with pagination support",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting paginated object details");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated details for PostgreSQL " + objectType + ": " + objectName +
                    ", page: " + page + ", pageSize: " + pageSize + ", includeCounts: " + includeCounts);

            Map<String, Object> result = postgresqlSchemaService.getObjectDetailsPaginated(
                    requestId, req, performedBy, objectName, objectType, owner, page, pageSize, includeCounts);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated details for PostgreSQL " + objectType + " " + objectName + ": " + e.getMessage());

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
                    @Parameter(name = "objectType", description = "Object type",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting object counts");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting counts only for " + objectType + ": " + objectName);

            Map<String, Object> result = postgresqlSchemaService.getObjectCountsOnly(
                    requestId, req, performedBy, objectName, objectType, owner);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating PostgreSQL object");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for validating PostgreSQL object");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Validating PostgreSQL object: " + objectName + " of type: " + objectType);

            Map<String, Object> result = postgresqlSchemaService.validateObject(
                    requestId, req, performedBy, objectName, objectType, owner);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error validating PostgreSQL object: " + e.getMessage());

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
            description = "Retrieves size information for a specific PostgreSQL object",
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

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL object size");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL object size");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting size for PostgreSQL " + objectType + ": " + objectName);

            Map<String, Object> result = postgresqlSchemaService.getObjectSize(
                    requestId, req, performedBy, objectName, objectType);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting size for PostgreSQL " + objectType + " " + objectName + ": " + e.getMessage());

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
            description = "Retrieves the DDL for a specific PostgreSQL object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (table, view, procedure, function, trigger, sequence, type)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH)
            })
    public ResponseEntity<?> getObjectDDL(
            @PathVariable String objectType,
            @PathVariable String objectName,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting PostgreSQL object DDL");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting PostgreSQL object DDL");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting DDL for PostgreSQL " + objectType + ": " + objectName);

            Map<String, Object> result = postgresqlSchemaService.getObjectDDL(
                    requestId, req, performedBy, objectName, objectType);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting DDL for PostgreSQL " + objectType + " " + objectName + ": " + e.getMessage());

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
            description = "Searches for PostgreSQL objects with pagination support",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for paginated search");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Paginated search with query: " + query + ", type: " + type +
                    ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.searchObjectsPaginated(
                    requestId, req, performedBy, query, type, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            description = "Searches for PostgreSQL objects with pagination support in a frontend-friendly format",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for paginated search for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Paginated search for frontend with query: " + query + ", type: " + type +
                    ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.searchObjectsForFrontendPaginated(
                    requestId, req, performedBy, query, type, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            description = "Performs comprehensive search across all objects",
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for comprehensive search");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Performing comprehensive search with pattern: " + pattern);

            Map<String, Object> result = postgresqlSchemaService.comprehensiveSearch(requestId, req, performedBy, pattern);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting recent objects");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting objects modified in last " + days + " days");

            Map<String, Object> result = postgresqlSchemaService.getRecentTables(requestId, req, performedBy, days);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            description = "Executes a SQL query against the PostgreSQL database",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> executeQuery(
            @RequestBody Map<String, Object> queryRequest,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "executing PostgreSQL query");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for executing PostgreSQL query");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            String query = (String) queryRequest.get("query");
            int timeoutSeconds = queryRequest.get("timeoutSeconds") != null ?
                    ((Number) queryRequest.get("timeoutSeconds")).intValue() : 30;
            boolean readOnly = queryRequest.get("readOnly") != null ?
                    (Boolean) queryRequest.get("readOnly") : true;

            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Executing query for user: " + performedBy + ", readOnly: " + readOnly);

            Map<String, Object> result = postgresqlSchemaService.executeQuery(
                    requestId, req, performedBy, query, timeoutSeconds, readOnly);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
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
            description = "Runs comprehensive diagnostics on the PostgreSQL database connection and schema",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> diagnoseDatabase(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "diagnosing PostgreSQL database");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for diagnosing PostgreSQL database");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Running PostgreSQL database diagnostics for user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.diagnoseDatabase(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error diagnosing PostgreSQL database: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while diagnosing PostgreSQL database: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 28. PAGINATED OBJECT LISTS FOR FRONTEND
    // ============================================================

    @GetMapping("/tables/filtered/paginated")
    @Operation(summary = "Get paginated tables (Frontend format)")
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
            Map<String, Object> result = postgresqlSchemaService.getTablesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/views/filtered/paginated")
    @Operation(summary = "Get paginated views (Frontend format)")
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
            Map<String, Object> result = postgresqlSchemaService.getViewsPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/procedures/filtered/paginated")
    @Operation(summary = "Get paginated procedures (Frontend format)")
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
            Map<String, Object> result = postgresqlSchemaService.getProceduresPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/functions/filtered/paginated")
    @Operation(summary = "Get paginated functions (Frontend format)")
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
            Map<String, Object> result = postgresqlSchemaService.getFunctionsPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/packages/filtered/paginated")
    @Operation(summary = "Get paginated schemas (Frontend format)")
    public ResponseEntity<?> getPackagesPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated schemas");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getPackagesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/synonyms/filtered/paginated")
    @Operation(summary = "Get paginated synonyms (Not supported)")
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
            Map<String, Object> result = postgresqlSchemaService.getSynonymsPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/sequences/filtered/paginated")
    @Operation(summary = "Get paginated sequences (Frontend format)")
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
            Map<String, Object> result = postgresqlSchemaService.getSequencesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/types/filtered/paginated")
    @Operation(summary = "Get paginated types (Frontend format)")
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
            Map<String, Object> result = postgresqlSchemaService.getTypesPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/triggers/filtered/paginated")
    @Operation(summary = "Get paginated triggers (Frontend format)")
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
            Map<String, Object> result = postgresqlSchemaService.getTriggersPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/frontend/counts")
    @Operation(summary = "Get all object counts")
    public ResponseEntity<?> getAllObjectCounts(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object counts");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getAllObjectCounts(
                    requestId, req, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/frontend/synonyms/{synonymName}/resolve-target")
    @Operation(summary = "Resolve synonym target (Not supported)")
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
            Map<String, Object> result = postgresqlSchemaService.resolveSynonymTarget(
                    requestId, req, performedBy, synonymName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
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
            result.put("service", "PostgreSQL Schema Browser");
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
            description = "Retrieves a list of all supported PostgreSQL object types",
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
                    "TABLE", "VIEW", "MATERIALIZED VIEW", "PROCEDURE", "FUNCTION",
                    "SEQUENCE", "TRIGGER", "TYPE", "INDEX", "SCHEMA",
                    "DOMAIN", "FOREIGN TABLE"
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
    // 31. SEARCH ACROSS MULTIPLE OBJECT TYPES WITH PAGINATION
    // ============================================================

    @GetMapping("/frontend/search/combined")
    @Operation(summary = "Search across multiple object types")
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
            Map<String, Object> result = postgresqlSchemaService.searchCombinedTypes(
                    requestId, req, performedBy, query, types, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    // ============================================================
    // 32. SEARCH PROCEDURES AND THEIR SYNONYMS TOGETHER (Not supported)
    // ============================================================

    @GetMapping("/frontend/procedures/with-synonyms/search")
    @Operation(summary = "Search procedures including their synonyms (Not supported)")
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
            Map<String, Object> result = postgresqlSchemaService.searchProceduresWithSynonyms(
                    requestId, req, performedBy, query, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    // ============================================================
    // 33. GET COUNT FOR SEARCH RESULTS (Fast)
    // ============================================================

    @GetMapping("/frontend/search/count")
    @Operation(summary = "Get search result count only")
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

            int count = postgresqlSchemaService.getSearchCount(query, types);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("query", query);
            result.put("types", types);
            result.put("responseCode", 200);
            result.put("requestId", requestId);
            result.put("timestamp", java.time.Instant.now().toString());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    // ============================================================
    // 34. ADDITIONAL ENDPOINTS
    // ============================================================

    @GetMapping("/objects/{objectType}/{objectName}/info")
    @Operation(summary = "Get basic object info")
    public ResponseEntity<?> getObjectBasicInfo(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting basic object info");
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getObjectBasicInfo(
                    requestId, objectName, objectType, owner, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/objects/{objectName}/synonym-resolution")
    @Operation(summary = "Resolve synonym (Not supported)")
    public ResponseEntity<?> resolveSynonym(
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "resolving synonym");
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.resolveSynonymCustom(
                    requestId, objectName, owner, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/properties")
    @Operation(summary = "Get object properties")
    public ResponseEntity<?> getObjectProperties(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object properties");
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getObjectProperties(
                    requestId, objectName, objectType, owner, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/columns")
    @Operation(summary = "Get columns or parameters")
    public ResponseEntity<?> getObjectColumns(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting columns");
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getObjectColumnsPaginated(
                    requestId, objectName, objectType, owner, page, pageSize, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/constraints")
    @Operation(summary = "Get table constraints")
    public ResponseEntity<?> getTableConstraints(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        if (!"TABLE".equalsIgnoreCase(objectType)) {
            return postgresqlSchemaService.createErrorResponse(requestId, "Constraints are only available for tables", 400);
        }

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting constraints");
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getTableConstraints(
                    requestId, objectName, owner, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/ddl-new")
    @Operation(summary = "Get object DDL")
    public ResponseEntity<?> getObjectDDL(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting DDL");
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getObjectDDL(
                    requestId, objectName, objectType, owner, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/counts-new")
    @Operation(summary = "Get object counts")
    public ResponseEntity<?> getObjectCounts(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting counts");
        if (authValidation != null) return authValidation;

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            Map<String, Object> result = postgresqlSchemaService.getObjectCounts(
                    requestId, objectName, objectType, owner, performedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return postgresqlSchemaService.createErrorResponse(requestId, e.getMessage(), 500);
        }
    }


    @GetMapping("/frontend/materialized-views/paginated")
    @Operation(summary = "Get paginated materialized views (Frontend format)",
            description = "Retrieves materialized views with pagination support in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getAllMaterializedViewsForFrontendPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting paginated materialized views for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting paginated materialized views for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting paginated materialized views for frontend, page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getAllMaterializedViewsForFrontendPaginated(
                    requestId, req, performedBy, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting paginated materialized views: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting paginated materialized views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    @GetMapping("/frontend/materialized-views")
    @Operation(summary = "Get all materialized views (Frontend format)",
            description = "Retrieves all materialized views from the current PostgreSQL schema in a frontend-friendly format",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> getAllMaterializedViewsForFrontend(HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting materialized views for frontend");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting materialized views for frontend");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting all materialized views for frontend, user: " + performedBy);

            Map<String, Object> result = postgresqlSchemaService.getAllMaterializedViewsForFrontend(requestId, req, performedBy);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting materialized views for frontend: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting materialized views for frontend: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }




    @GetMapping("/objects/{objectType}/{objectName}/used-by/summary")
    @Operation(summary = "Get used by summary",
            description = "Retrieves summary of objects that depend on this object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, FUNCTION, etc.)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Object owner (optional)",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getUsedBySummary(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting used by summary");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting used by summary");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting used by summary for " + objectType + ": " + objectName);

            Map<String, Object> result = postgresqlSchemaService.getUsedBySummary(
                    requestId, req, performedBy, objectName, objectType, owner);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting used by summary: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting used by summary: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/objects/{objectType}/{objectName}/used-by/paginated")
    @Operation(summary = "Get used by paginated",
            description = "Retrieves paginated list of objects that depend on this object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}",
                            required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, FUNCTION, etc.)",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "objectName", description = "Object name",
                            required = true, in = ParameterIn.PATH),
                    @Parameter(name = "owner", description = "Object owner (optional)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (1-based)",
                            required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Number of items per page",
                            required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getUsedByPaginated(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting used by paginated");
        if (authValidation != null) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting used by paginated");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Getting used by paginated for " + objectType + ": " + objectName +
                    ", page: " + page + ", pageSize: " + pageSize);

            Map<String, Object> result = postgresqlSchemaService.getUsedByPaginated(
                    requestId, req, performedBy, objectName, objectType, owner, page, pageSize);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("postgresqlSchema", "RequestEntity ID: " + requestId +
                    ", Error getting used by paginated: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting used by paginated: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



}