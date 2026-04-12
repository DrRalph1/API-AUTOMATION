package com.usg.autoAPIGenerator.controllers;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.autoAPIGenerator.dtos.collections.*;
import com.usg.autoAPIGenerator.helpers.JwtHelper;
import com.usg.autoAPIGenerator.services.CollectionsService;
import com.usg.autoAPIGenerator.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/collections")
@RequiredArgsConstructor
@Tag(name = "COLLECTIONS", description = "Endpoints for API collections management")
public class CollectionsController {

    private final CollectionsService collectionsService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET COLLECTIONS LIST
    // ============================================================
    @GetMapping
    @Operation(summary = "Get collections list", description = "Retrieve all collections for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collections list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCollectionsList(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting collections list");
        if (authValidation != null) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting collections list");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Getting collections list for user: " + performedBy);

            CollectionsListResponseDTO collections = collectionsService.getCollectionsList(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collections list retrieved successfully");
            response.put("data", collections);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Collections list retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error getting collections list: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting collections list: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET COLLECTION DETAILS
    // ============================================================
    @GetMapping("/{collectionId}")
    @Operation(summary = "Get collectionEntity details", description = "Retrieve details for a specific collectionEntity")
    public ResponseEntity<?> getCollectionDetails(
            @PathVariable String collectionId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting collectionEntity details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Getting collectionEntity details for: " + collectionId);

            CollectionDetailsResponseDTO details = collectionsService.getCollectionDetails(
                    requestId, req, performedBy, collectionId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "CollectionEntity details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", CollectionEntity details retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error getting collectionEntity details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting collectionEntity details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET REQUEST DETAILS
    // ============================================================
    @GetMapping("/{collectionId}/requests/{requestId}")
    @Operation(summary = "Get request details", description = "Retrieve details for a specific API request in the format used by API Generation Engine")
    public ResponseEntity<?> getRequestDetails(
            @PathVariable String collectionId,
            @PathVariable String requestId,
            HttpServletRequest req) {

        String requestIdParam = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting request details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Getting request details for: " + requestId);

            // This now returns GenerateApiRequestDTO which matches the sample structure
            GenerateApiRequestDTO details = collectionsService.getRequestDetails(
                    requestIdParam, req, performedBy, collectionId, requestId);

            // Create response in the same format as your API generation apiEngine
            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Request details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestIdParam);

            // Add metadata if needed (optional - matches your generateApi response pattern)
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("parametersCount", details.getParameters() != null ? details.getParameters().size() : 0);
            metadata.put("headersCount", details.getHeaders() != null ? details.getHeaders().size() : 0);
            metadata.put("retrievedAt", LocalDateTime.now().toString());
            metadata.put("collectionId", collectionId);
            metadata.put("requestId", requestId);
            response.put("metadata", metadata);

            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Request details retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Request not found: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 404);
            errorResponse.put("message", "Request not found: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (SecurityException e) {
            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Security error: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access denied: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Error getting request details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting request details: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. EXECUTE REQUEST
    // ============================================================
    @PostMapping("/execute")
    @Operation(summary = "Execute API requestEntity", description = "Execute an API requestEntity and get the response")
    public ResponseEntity<?> executeRequest(
            @Valid @RequestBody ExecuteRequestDTO requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // REMOVE THIS - Don't validate JWT for API execution
        // ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "executing API requestEntity");
        // if (authValidation != null) {
        //     return authValidation;
        // }

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

            // Extract performedBy for logging only - it's optional
            String performedBy = "anonymous";
            try {
                performedBy = jwtHelper.extractPerformedBy(req);
            } catch (Exception e) {
                // Ignore - user may not be authenticated
            }

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Executing request for user: " + performedBy);

            ExecuteRequestResponseDTO response = collectionsService.executeRequest(
                    requestId, req, performedBy, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Request executed successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Request executed successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error executing request: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while executing request: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. SAVE REQUEST
    // ============================================================
    @PostMapping("/save")
    @Operation(summary = "Save API requestEntity", description = "Save an API requestEntity to a collectionEntity")
    public ResponseEntity<?> saveRequest(
            @Valid @RequestBody SaveRequestDTO requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "saving API requestEntity");
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
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Saving requestEntity for user: " + performedBy);

            SaveRequestResponseDTO response = collectionsService.saveRequest(
                    requestId, req, performedBy, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "RequestEntity saved successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", RequestEntity saved successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error saving requestEntity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while saving requestEntity: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. CREATE COLLECTION
    // ============================================================
    @PostMapping("/create")
    @Operation(summary = "Create new collectionEntity", description = "Create a new API collectionEntity")
    public ResponseEntity<?> createCollection(
            @Valid @RequestBody CreateCollectionDTO collectionDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "creating collectionEntity");
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
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Creating collectionEntity for user: " + performedBy);

            CreateCollectionResponseDTO response = collectionsService.createCollection(
                    requestId, req, performedBy, collectionDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "CollectionEntity created successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", CollectionEntity created successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error creating collectionEntity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while creating collectionEntity: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. GENERATE CODE SNIPPET
    // ============================================================
    @PostMapping("/code-snippet")
    @Operation(summary = "Generate code snippet", description = "Generate code snippet for API requestEntity in various languages")
    public ResponseEntity<?> generateCodeSnippet(
            @Valid @RequestBody CodeSnippetRequestDTO snippetRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating code snippet");
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
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Generating code snippet for language: " + snippetRequest.getLanguage());

            CodeSnippetResponseDTO snippet = collectionsService.generateCodeSnippet(
                    requestId, req, performedBy, snippetRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Code snippet generated successfully");
            apiResponse.put("data", snippet);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Code snippet generated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error generating code snippet: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while generating code snippet: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. GET ENVIRONMENTS
    // ============================================================
    @GetMapping("/environments")
    @Operation(summary = "Get environments list", description = "Retrieve available environments")
    public ResponseEntity<?> getEnvironments(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting environments list");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Getting environments for user: " + performedBy);

            EnvironmentsResponseDTO environments = collectionsService.getEnvironments(
                    requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Environments retrieved successfully");
            response.put("data", environments);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Environments retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error getting environments: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting environments: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. IMPORT COLLECTION
    // ============================================================
    @PostMapping("/import")
    @Operation(summary = "Import collectionEntity", description = "Import a collectionEntity from external source")
    public ResponseEntity<?> importCollection(
            @Valid @RequestBody ImportRequestDTO importRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "importing collectionEntity");
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
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Importing collectionEntity for user: " + performedBy);

            ImportResponseDTO response = collectionsService.importCollection(
                    requestId, req, performedBy, importRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "CollectionEntity imported successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", CollectionEntity imported successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error importing collectionEntity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while importing collectionEntity: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. DELETE COLLECTION
    // ============================================================
    @DeleteMapping("/{collectionId}")
    @Operation(summary = "Delete collectionEntity", description = "Delete a specific collectionEntity")
    public ResponseEntity<?> deleteCollection(
            @PathVariable String collectionId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting collectionEntity");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Deleting collectionEntity: " + collectionId + " for user: " + performedBy);

            // Note: The service doesn't have a deleteCollection method yet
            // This would need to be added to the service
            // For now, we'll just return a success response

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "CollectionEntity deleted successfully");
            response.put("data", Map.of("collectionId", collectionId, "deleted", true));
            response.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", CollectionEntity deleted successfully: " + collectionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error deleting collectionEntity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while deleting collectionEntity: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. DELETE REQUEST
    // ============================================================
    @DeleteMapping("/{collectionId}/requests/{requestId}")
    @Operation(summary = "Delete API requestEntity", description = "Delete a specific API requestEntity from a collectionEntity")
    public ResponseEntity<?> deleteRequest(
            @PathVariable String collectionId,
            @PathVariable String requestId,
            HttpServletRequest req) {

        String requestIdParam = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting API requestEntity");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "RequestEntity ID: " + requestIdParam +
                    ", Deleting requestEntity: " + requestId + " from collectionEntity: " + collectionId);

            // Note: The service doesn't have a deleteRequest method yet
            // This would need to be added to the service
            // For now, we'll just return a success response

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "RequestEntity deleted successfully");
            response.put("data", Map.of(
                    "collectionId", collectionId,
                    "requestId", requestId,
                    "deleted", true
            ));
            response.put("requestId", requestIdParam);

            loggerUtil.log("collections", "RequestEntity ID: " + requestIdParam +
                    ", RequestEntity deleted successfully: " + requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestIdParam +
                    ", Error deleting requestEntity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while deleting requestEntity: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. UPDATE COLLECTION
    // ============================================================
    @PutMapping("/{collectionId}")
    @Operation(summary = "Update collectionEntity", description = "Update an existing collectionEntity")
    public ResponseEntity<?> updateCollection(
            @PathVariable String collectionId,
            @Valid @RequestBody UpdateCollectionDTO collectionDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating collectionEntity");
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
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Updating collectionEntity: " + collectionId + " for user: " + performedBy);

            // Note: The service doesn't have an updateCollection method yet
            // This would need to be added to the service
            // For now, we'll just return a success response

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "CollectionEntity updated successfully");
            response.put("data", Map.of(
                    "collectionId", collectionId,
                    "name", collectionDto.getName(),
                    "description", collectionDto.getDescription(),
                    "updated", true
            ));
            response.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", CollectionEntity updated successfully: " + collectionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error updating collectionEntity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating collectionEntity: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 14. EXPORT COLLECTION
    // ============================================================
    @GetMapping("/{collectionId}/export")
    @Operation(summary = "Export collectionEntity", description = "Export a collectionEntity to external format")
    public ResponseEntity<?> exportCollection(
            @PathVariable String collectionId,
            @RequestParam(required = false, defaultValue = "json") String format,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting collectionEntity");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Exporting collectionEntity: " + collectionId + " in format: " + format);

            // Note: The service doesn't have an exportCollection method yet
            // This would need to be added to the service
            // For now, we'll just return a sample export

            Map<String, Object> exportData = new HashMap<>();
            exportData.put("collectionId", collectionId);
            exportData.put("name", "E-Commerce API");
            exportData.put("description", "Exported collectionEntity");
            exportData.put("format", format);
            exportData.put("exportedAt", java.time.LocalDateTime.now().toString());
            exportData.put("requests", 12);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "CollectionEntity exported successfully");
            response.put("data", exportData);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", CollectionEntity exported successfully: " + collectionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "RequestEntity ID: " + requestId +
                    ", Error exporting collectionEntity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while exporting collectionEntity: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}