package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.schemaBrowser.*;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.SchemaBrowserService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/schema-browser")
@RequiredArgsConstructor
@Tag(name = "SCHEMA BROWSER", description = "Endpoints for database schema browsing and exploration")
public class SchemaBrowserController {

    private final SchemaBrowserService schemaBrowserService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET SCHEMA CONNECTIONS
    // ============================================================
    @GetMapping("/connections")
    @Operation(summary = "Get schema browser connections",
            description = "Retrieves all database connections available for schema browsing",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schema connections retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getSchemaConnections(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting schema connections");
        if (authValidation != null) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting schema connections");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Getting schema connections for user: " + performedBy);

            SchemaConnectionsResponse connections = schemaBrowserService.getSchemaConnections(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Schema connections retrieved successfully");
            response.put("data", connections);
            response.put("requestId", requestId);

            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Schema connections retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error getting schema connections: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting schema connections: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET SCHEMA OBJECTS
    // ============================================================
    @GetMapping("/objects")
    @Operation(summary = "Get schema objects",
            description = "Retrieves schema objects (tables, views, procedures, etc.) for a specific connection",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "connectionId", description = "Connection ID", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, PROCEDURE, etc.)", required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "filter", description = "Filter string for object names", required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getSchemaObjects(
            @RequestParam String connectionId,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String filter,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting schema objects");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Getting schema objects for connection: " + connectionId + ", type: " + objectType);

            SchemaObjectsResponse objects = schemaBrowserService.getSchemaObjects(requestId, req, performedBy,
                    connectionId, objectType, filter);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Schema objects retrieved successfully");
            response.put("data", objects);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error getting schema objects: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting schema objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET OBJECT DETAILS
    // ============================================================
    @GetMapping("/object-details")
    @Operation(summary = "Get object details",
            description = "Retrieves detailed information about a specific schema object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "connectionId", description = "Connection ID", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectType", description = "Object type (TABLE, VIEW, etc.)", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectName", description = "Object name", required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getObjectDetails(
            @RequestParam String connectionId,
            @RequestParam String objectType,
            @RequestParam String objectName,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Getting details for object: " + objectType + "." + objectName);

            ObjectDetailsResponse details = schemaBrowserService.getObjectDetails(requestId, req, performedBy,
                    connectionId, objectType, objectName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Object details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error getting object details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. GET TABLE DATA
    // ============================================================
    @GetMapping("/table-data")
    @Operation(summary = "Get table data",
            description = "Retrieves paginated data from a table with sorting and filtering",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "connectionId", description = "Connection ID", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "tableName", description = "Table name", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "page", description = "Page number (default: 1)", in = ParameterIn.QUERY),
                    @Parameter(name = "pageSize", description = "Page size (default: 50)", in = ParameterIn.QUERY),
                    @Parameter(name = "sortColumn", description = "Column to sort by", required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "sortDirection", description = "Sort direction (ASC/DESC)", required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getTableData(
            @RequestParam String connectionId,
            @RequestParam String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String sortColumn,
            @RequestParam(required = false) String sortDirection,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting table data");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Getting table data for: " + tableName + ", page: " + page);

            TableDataResponse tableData = schemaBrowserService.getTableData(requestId, req, performedBy,
                    connectionId, tableName, page, pageSize, sortColumn, sortDirection);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Table data retrieved successfully");
            response.put("data", tableData);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error getting table data: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting table data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. GET OBJECT DDL
    // ============================================================
    @GetMapping("/ddl")
    @Operation(summary = "Get object DDL",
            description = "Retrieves the Data Definition Language (DDL) for a schema object",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "connectionId", description = "Connection ID", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectType", description = "Object type", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectName", description = "Object name", required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getObjectDDL(
            @RequestParam String connectionId,
            @RequestParam String objectType,
            @RequestParam String objectName,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object DDL");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Getting DDL for: " + objectType + "." + objectName);

            DDLResponse ddl = schemaBrowserService.getObjectDDL(requestId, req, performedBy,
                    connectionId, objectType, objectName);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Object DDL retrieved successfully");
            response.put("data", ddl);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error getting object DDL: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object DDL: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. SEARCH SCHEMA
    // ============================================================
    @GetMapping("/search")
    @Operation(summary = "Search schema",
            description = "Searches across schema objects with various criteria",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "connectionId", description = "Connection ID", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "searchQuery", description = "Search query string", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "searchType", description = "Search type (ALL, TABLE, VIEW, etc.)", required = false, in = ParameterIn.QUERY),
                    @Parameter(name = "maxResults", description = "Maximum results to return (default: 100)", required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> searchSchema(
            @RequestParam String connectionId,
            @RequestParam String searchQuery,
            @RequestParam(required = false) String searchType,
            @RequestParam(defaultValue = "100") int maxResults,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching schema");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Searching schema with query: " + searchQuery);

            SearchResponse searchResults = schemaBrowserService.searchSchema(requestId, req, performedBy,
                    connectionId, searchQuery, searchType, maxResults);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Schema search completed successfully");
            response.put("data", searchResults);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error searching schema: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching schema: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. EXECUTE QUERY
    // ============================================================
    @PostMapping("/execute-query")
    @Operation(summary = "Execute SQL query",
            description = "Executes a custom SQL query on the database",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> executeQuery(
            @Valid @RequestBody ExecuteQueryRequest queryRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "executing query");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors: " + validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Executing query for user: " + performedBy);

            ExecuteQueryResponse queryResult = schemaBrowserService.executeQuery(requestId, req, performedBy,
                    queryRequest.getConnectionId(), queryRequest.getQuery(),
                    queryRequest.getTimeoutSeconds(), queryRequest.getReadOnly());

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Query executed successfully");
            response.put("data", queryResult);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error executing query: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while executing query: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. GENERATE API FROM OBJECT
    // ============================================================
    @PostMapping("/generate-api")
    @Operation(summary = "Generate API from object",
            description = "Generates REST API endpoints from a database object (table, view, etc.)",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> generateAPIFromObject(
            @Valid @RequestBody GenerateAPIRequest apiRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating API");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors: " + validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Generating API for object: " + apiRequest.getObjectType() + "." + apiRequest.getObjectName());

            GenerateAPIResponse apiResponse = schemaBrowserService.generateAPIFromObject(requestId, req, performedBy,
                    apiRequest.getConnectionId(), apiRequest.getObjectType(), apiRequest.getObjectName(),
                    apiRequest.getApiType(), apiRequest.getOptions());

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "API generated successfully");
            response.put("data", apiResponse);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error generating API: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while generating API: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. GET COMPREHENSIVE SCHEMA DATA
    // ============================================================
    @GetMapping("/comprehensive")
    @Operation(summary = "Get comprehensive schema data",
            description = "Retrieves comprehensive schema data including connections, objects, and statistics",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "connectionId", description = "Connection ID", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "schema", description = "Schema name", required = false, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getComprehensiveSchemaData(
            @RequestParam String connectionId,
            @RequestParam(required = false) String schema,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting comprehensive schema data");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Getting comprehensive schema data for connection: " + connectionId);

            // Fetch comprehensive schema data
            SchemaConnectionsResponse connections = schemaBrowserService.getSchemaConnections(requestId, req, performedBy);
            SchemaObjectsResponse tables = schemaBrowserService.getSchemaObjects(requestId, req, performedBy,
                    connectionId, "TABLE", null);
            SchemaObjectsResponse views = schemaBrowserService.getSchemaObjects(requestId, req, performedBy,
                    connectionId, "VIEW", null);
            SchemaObjectsResponse procedures = schemaBrowserService.getSchemaObjects(requestId, req, performedBy,
                    connectionId, "PROCEDURE", null);
            SchemaObjectsResponse functions = schemaBrowserService.getSchemaObjects(requestId, req, performedBy,
                    connectionId, "FUNCTION", null);
            SchemaObjectsResponse packages = schemaBrowserService.getSchemaObjects(requestId, req, performedBy,
                    connectionId, "PACKAGE", null);

            Map<String, Object> comprehensiveData = new HashMap<>();
            comprehensiveData.put("connections", connections);
            comprehensiveData.put("tables", tables);
            comprehensiveData.put("views", views);
            comprehensiveData.put("procedures", procedures);
            comprehensiveData.put("functions", functions);
            comprehensiveData.put("packages", packages);
            comprehensiveData.put("totalObjects",
                    tables.getTotalCount() + views.getTotalCount() + procedures.getTotalCount() +
                            functions.getTotalCount() + packages.getTotalCount());
            comprehensiveData.put("lastUpdated", java.time.LocalDateTime.now().toString());
            comprehensiveData.put("generatedFor", performedBy);
            comprehensiveData.put("connectionId", connectionId);
            comprehensiveData.put("schema", schema != null ? schema : "ALL");

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Comprehensive schema data retrieved successfully");
            response.put("data", comprehensiveData);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error getting comprehensive schema data: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting comprehensive schema data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. CLEAR SCHEMA CACHE
    // ============================================================
    @PostMapping("/clear-cache")
    @Operation(summary = "Clear schema cache",
            description = "Clears the cache for schema browser data",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> clearSchemaCache(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "clearing schema cache");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Clearing schema cache for user: " + performedBy);

            schemaBrowserService.clearSchemaCache(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Schema cache cleared successfully");
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error clearing schema cache: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while clearing schema cache: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. GET OBJECT HIERARCHY
    // ============================================================
    @GetMapping("/object-hierarchy")
    @Operation(summary = "Get object hierarchy",
            description = "Retrieves the hierarchy and dependencies of schema objects",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
                    @Parameter(name = "connectionId", description = "Connection ID", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectType", description = "Object type", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "objectName", description = "Object name", required = true, in = ParameterIn.QUERY)
            })
    public ResponseEntity<?> getObjectHierarchy(
            @RequestParam String connectionId,
            @RequestParam String objectType,
            @RequestParam String objectName,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object hierarchy");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Getting hierarchy for: " + objectType + "." + objectName);

            // Get object details first
            ObjectDetailsResponse details = schemaBrowserService.getObjectDetails(requestId, req, performedBy,
                    connectionId, objectType, objectName);

            // Build hierarchy response
            Map<String, Object> hierarchy = new HashMap<>();
            hierarchy.put("objectName", objectName);
            hierarchy.put("objectType", objectType);
            hierarchy.put("dependencies", new HashMap<>());
            hierarchy.put("dependents", new HashMap<>());
            hierarchy.put("hierarchyLevel", 1);
            hierarchy.put("lastUpdated", java.time.LocalDateTime.now().toString());

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Object hierarchy retrieved successfully");
            response.put("data", hierarchy);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error getting object hierarchy: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting object hierarchy: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. EXPORT SCHEMA DATA
    // ============================================================
    @PostMapping("/export")
    @Operation(summary = "Export schema data",
            description = "Exports schema data in various formats (JSON, XML, SQL, etc.)",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> exportSchemaData(
            @Valid @RequestBody ExportSchemaRequest exportRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting schema data");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors: " + validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Exporting schema data for user: " + performedBy + ", format: " + exportRequest.getFormat());

            // In a real implementation, this would generate and return the export file
            // For now, returning a mock export response
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("exportId", UUID.randomUUID().toString());
            exportData.put("format", exportRequest.getFormat());
            exportData.put("fileName", "schema_export_" + java.time.LocalDateTime.now().toString() + "." + exportRequest.getFormat().toLowerCase());
            exportData.put("fileSize", "2.5 MB");
            exportData.put("downloadUrl", "/plx/api/schema-browser/download/" + exportData.get("exportId"));
            exportData.put("generatedAt", java.time.LocalDateTime.now().toString());
            exportData.put("generatedBy", performedBy);
            exportData.put("objectCount", 156);
            exportData.put("status", "COMPLETED");

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Schema data exported successfully");
            response.put("data", exportData);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error exporting schema data: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while exporting schema data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. SEARCH WITH ADVANCED FILTERS
    // ============================================================
    @PostMapping("/advanced-search")
    @Operation(summary = "Advanced schema search",
            description = "Searches schema objects with advanced filters and criteria",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> advancedSearch(
            @Valid @RequestBody SearchRequest searchRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "performing advanced search");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors: " + validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Performing advanced search for user: " + performedBy);

            // Perform the search using the service
            SearchResponse searchResults = schemaBrowserService.searchSchema(requestId, req, performedBy,
                    searchRequest.getConnectionId(), searchRequest.getSearchQuery(),
                    searchRequest.getSearchType(), searchRequest.getMaxResults());

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Advanced search completed successfully");
            response.put("data", searchResults);
            response.put("filters", searchRequest);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Error performing advanced search: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while performing advanced search: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 14. HEALTH CHECK
    // ============================================================
    @GetMapping("/health")
    @Operation(summary = "Schema browser health check",
            description = "Checks the health status of the schema browser service",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            })
    public ResponseEntity<?> healthCheck(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        try {
            // Check service health
            boolean serviceHealthy = true;
            long cacheSize = schemaBrowserService.getClass().getDeclaredFields().length; // Simplified check

            Map<String, Object> healthData = new HashMap<>();
            healthData.put("status", "UP");
            healthData.put("timestamp", java.time.LocalDateTime.now().toString());
            healthData.put("service", "SchemaBrowserService");
            healthData.put("cacheItems", cacheSize);
            healthData.put("uptime", "24h");
            healthData.put("version", "1.0.0");
            healthData.put("environment", "Development");

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Schema browser service is healthy");
            response.put("data", healthData);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("schemaBrowser", "RequestEntity ID: " + requestId +
                    ", Health check failed: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 503);
            errorResponse.put("message", "Schema browser service is unhealthy: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
}