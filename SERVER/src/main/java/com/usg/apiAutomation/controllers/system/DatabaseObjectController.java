package com.usg.apiAutomation.controllers.system;

import com.usg.apiAutomation.dtos.database.*;
import com.usg.apiAutomation.helpers.ApiKeyNSecretHelper;
import com.usg.apiAutomation.helpers.ClientIpHelper;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.system.DatabaseObjectService;
import com.usg.apiAutomation.utils.JwtUtil;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/database/objects")
@RequiredArgsConstructor
//@Tag(name = "DATABASE OBJECTS", description = "Endpoints for managing and querying database objects")
@Tag(name = "SYSTEM ACTIVITIES", description = "System-level endpoints")
public class DatabaseObjectController {

    private final DatabaseObjectService databaseObjectService;
    private final LoggerUtil loggerUtil;
    private final ApiKeyNSecretHelper apiKeyNSecretHelper;
    private final ClientIpHelper clientIpHelper;
    private final JwtUtil jwtUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET ALL PROCEDURES
    // ============================================================
    @GetMapping("/procedures")
    @Operation(summary = "Get all stored procedures", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "owner", description = "Filter by schema owner", in = ParameterIn.QUERY),
            @Parameter(name = "status", description = "Filter by status (VALID/INVALID)", in = ParameterIn.QUERY),
            @Parameter(name = "search", description = "Search in procedure name", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procedures retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllProcedures(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "owner", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all procedures");
        if (authValidation != null) {
            loggerUtil.log("database-objects", "Request ID: " + requestId +
                    ", Authorization failed for getting all procedures");
            return authValidation;
        }

        try {
            loggerUtil.log("database-objects", "Request ID: " + requestId +
                    ", Getting all procedures with filters - owner: " + owner +
                    ", status: " + status + ", search: " + search);

            DatabaseObjectSearchDTO searchDTO = DatabaseObjectSearchDTO.builder()
                    .owner(owner)
                    .status(status)
                    .searchTerm(search)
                    .objectType("PROCEDURE")
                    .build();

            Map<String, Object> result = databaseObjectService.searchObjects(searchDTO, pageable, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Procedures retrieved successfully");
            response.put("data", result.get("objects"));
            response.put("pagination", result.get("pagination"));
            response.put("requestId", requestId);

            loggerUtil.log("database-objects", "Request ID: " + requestId +
                    ", Procedures retrieved successfully. Count: " +
                    ((List<?>) result.get("objects")).size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("database-objects", "Request ID: " + requestId +
                    ", Error getting procedures: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting procedures: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 2. GET ALL FUNCTIONS
    // ============================================================
    @GetMapping("/functions")
    @Operation(summary = "Get all stored functions", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "owner", description = "Filter by schema owner", in = ParameterIn.QUERY),
            @Parameter(name = "status", description = "Filter by status (VALID/INVALID)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getAllFunctions(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "owner", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all functions");
        if (authValidation != null) {
            loggerUtil.log("database-objects", "Request ID: " + requestId +
                    ", Authorization failed for getting all functions");
            return authValidation;
        }

        try {
            DatabaseObjectSearchDTO searchDTO = DatabaseObjectSearchDTO.builder()
                    .owner(owner)
                    .status(status)
                    .objectType("FUNCTION")
                    .build();

            Map<String, Object> result = databaseObjectService.searchObjects(searchDTO, pageable, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Functions retrieved successfully");
            response.put("data", result.get("objects"));
            response.put("pagination", result.get("pagination"));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting functions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 3. GET ALL VIEWS
    // ============================================================
    @GetMapping("/views")
    @Operation(summary = "Get all database views", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "owner", description = "Filter by schema owner", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getAllViews(
            @RequestParam(required = false) String owner,
            @PageableDefault(size = 20, sort = "owner", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all views");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            DatabaseObjectSearchDTO searchDTO = DatabaseObjectSearchDTO.builder()
                    .owner(owner)
                    .objectType("VIEW")
                    .build();

            Map<String, Object> result = databaseObjectService.searchObjects(searchDTO, pageable, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Views retrieved successfully");
            response.put("data", result.get("objects"));
            response.put("pagination", result.get("pagination"));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting views: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 4. GET ALL TRIGGERS
    // ============================================================
    @GetMapping("/triggers")
    @Operation(summary = "Get all database triggers", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "owner", description = "Filter by schema owner", in = ParameterIn.QUERY),
            @Parameter(name = "tableName", description = "Filter by table name", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getAllTriggers(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String tableName,
            @PageableDefault(size = 20, sort = "owner", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all triggers");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            DatabaseObjectSearchDTO searchDTO = DatabaseObjectSearchDTO.builder()
                    .owner(owner)
                    .tableName(tableName)
                    .objectType("TRIGGER")
                    .build();

            Map<String, Object> result = databaseObjectService.searchObjects(searchDTO, pageable, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Triggers retrieved successfully");
            response.put("data", result.get("objects"));
            response.put("pagination", result.get("pagination"));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting triggers: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 5. GET ALL JOBS
    // ============================================================
    @GetMapping("/jobs")
    @Operation(summary = "Get all database jobs", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "owner", description = "Filter by schema owner", in = ParameterIn.QUERY),
            @Parameter(name = "jobType", description = "Filter by job type (DBMS_JOB/SCHEDULER_JOB)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String jobType,
            @PageableDefault(size = 20, sort = "owner", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all jobs");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            DatabaseObjectSearchDTO searchDTO = DatabaseObjectSearchDTO.builder()
                    .owner(owner)
                    .jobType(jobType)
                    .objectType("JOB")
                    .build();

            Map<String, Object> result = databaseObjectService.searchObjects(searchDTO, pageable, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Jobs retrieved successfully");
            response.put("data", result.get("objects"));
            response.put("pagination", result.get("pagination"));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting jobs: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 6. GET OBJECT BY NAME
    // ============================================================
    @GetMapping("/{objectType}/{objectName}")
    @Operation(summary = "Get database object by name", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "owner", description = "Schema owner", in = ParameterIn.QUERY),
            @Parameter(name = "objectType", description = "Object type (PROCEDURE/FUNCTION/VIEW/TRIGGER/JOB)",
                    required = true, in = ParameterIn.PATH),
            @Parameter(name = "objectName", description = "Object name", required = true, in = ParameterIn.PATH)
    })
    public ResponseEntity<?> getObjectByName(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting database object");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            DatabaseObjectDetailDTO object = databaseObjectService.getObjectDetail(
                    objectType, objectName, owner, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Object retrieved successfully");
            response.put("data", object);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 404);
            errorResponse.put("message", "Object not found: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    // ============================================================
    // 7. GET OBJECT SUMMARY
    // ============================================================
    @GetMapping("/summary")
    @Operation(summary = "Get database objects summary", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getObjectsSummary(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting database summary");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            DatabaseSummaryDTO summary = databaseObjectService.getDatabaseSummary(requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Database summary retrieved successfully");
            response.put("data", summary);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting database summary: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 8. SEARCH OBJECTS
    // ============================================================
    @PostMapping("/search")
    @Operation(summary = "Search database objects with advanced filters", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> searchObjects(
            @Valid @RequestBody DatabaseObjectSearchDTO searchDTO,
            BindingResult bindingResult,
            @PageableDefault(size = 20, sort = "owner", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching database objects");
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

            Map<String, Object> result = databaseObjectService.searchObjects(searchDTO, pageable, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Objects retrieved successfully");
            response.put("data", result.get("objects"));
            response.put("pagination", result.get("pagination"));
            response.put("filters", result.get("filters"));
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching objects: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. GET OBJECT DEPENDENCIES
    // ============================================================
    @GetMapping("/{objectType}/{objectName}/dependencies")
    @Operation(summary = "Get object dependencies", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> getObjectDependencies(
            @PathVariable String objectType,
            @PathVariable String objectName,
            @RequestParam(required = false) String owner,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting object dependencies");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            List<DatabaseObjectDTO> dependencies = databaseObjectService.getObjectDependencies(
                    objectType, objectName, owner, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Dependencies retrieved successfully");
            response.put("data", dependencies);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting dependencies: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. VALIDATE OBJECT
    // ============================================================
    @PostMapping("/validate")
    @Operation(summary = "Validate database object", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    public ResponseEntity<?> validateObject(
            @Valid @RequestBody ObjectValidationRequestDTO validationRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating database object");
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

            ObjectValidationResultDTO result = databaseObjectService.validateObject(
                    validationRequest, requestId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Object validation completed");
            response.put("data", result);
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred during validation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}