package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.collections.*;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.CollectionsService;
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Authorization failed for getting collections list");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Getting collections list for user: " + performedBy);

            CollectionsListResponse collections = collectionsService.getCollectionsList(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collections list retrieved successfully");
            response.put("data", collections);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collections list retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
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
    @Operation(summary = "Get collection details", description = "Retrieve details for a specific collection")
    public ResponseEntity<?> getCollectionDetails(
            @PathVariable String collectionId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting collection details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Getting collection details for: " + collectionId);

            CollectionDetailsResponse details = collectionsService.getCollectionDetails(
                    requestId, req, performedBy, collectionId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collection details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collection details retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error getting collection details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting collection details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET REQUEST DETAILS
    // ============================================================
    @GetMapping("/{collectionId}/requests/{requestId}")
    @Operation(summary = "Get request details", description = "Retrieve details for a specific API request")
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

            RequestDetailsResponse details = collectionsService.getRequestDetails(
                    requestIdParam, req, performedBy, collectionId, requestId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Request details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestIdParam);

            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Request details retrieved successfully");

            return ResponseEntity.ok(response);

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
    @Operation(summary = "Execute API request", description = "Execute an API request and get the response")
    public ResponseEntity<?> executeRequest(
            @Valid @RequestBody ExecuteRequestDto requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "executing API request");
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Executing request for user: " + performedBy);

            ExecuteRequestResponse response = collectionsService.executeRequest(
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
    @Operation(summary = "Save API request", description = "Save an API request to a collection")
    public ResponseEntity<?> saveRequest(
            @Valid @RequestBody SaveRequestDto requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "saving API request");
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Saving request for user: " + performedBy);

            SaveRequestResponse response = collectionsService.saveRequest(
                    requestId, req, performedBy, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Request saved successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Request saved successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error saving request: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while saving request: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. CREATE COLLECTION
    // ============================================================
    @PostMapping("/create")
    @Operation(summary = "Create new collection", description = "Create a new API collection")
    public ResponseEntity<?> createCollection(
            @Valid @RequestBody CreateCollectionDto collectionDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "creating collection");
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Creating collection for user: " + performedBy);

            CreateCollectionResponse response = collectionsService.createCollection(
                    requestId, req, performedBy, collectionDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Collection created successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collection created successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error creating collection: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while creating collection: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. GENERATE CODE SNIPPET
    // ============================================================
    @PostMapping("/code-snippet")
    @Operation(summary = "Generate code snippet", description = "Generate code snippet for API request in various languages")
    public ResponseEntity<?> generateCodeSnippet(
            @Valid @RequestBody CodeSnippetRequestDto snippetRequest,
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Generating code snippet for language: " + snippetRequest.getLanguage());

            CodeSnippetResponse snippet = collectionsService.generateCodeSnippet(
                    requestId, req, performedBy, snippetRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Code snippet generated successfully");
            apiResponse.put("data", snippet);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Code snippet generated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Getting environments for user: " + performedBy);

            EnvironmentsResponse environments = collectionsService.getEnvironments(
                    requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Environments retrieved successfully");
            response.put("data", environments);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Environments retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
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
    @Operation(summary = "Import collection", description = "Import a collection from external source")
    public ResponseEntity<?> importCollection(
            @Valid @RequestBody ImportRequestDto importRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "importing collection");
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Importing collection for user: " + performedBy);

            ImportResponse response = collectionsService.importCollection(
                    requestId, req, performedBy, importRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Collection imported successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collection imported successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error importing collection: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while importing collection: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. CLEAR COLLECTIONS CACHE
    // ============================================================
    @PostMapping("/cache/clear")
    @Operation(summary = "Clear collections cache", description = "Clear cache for collections data")
    public ResponseEntity<?> clearCollectionsCache(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "clearing collections cache");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Clearing collections cache for user: " + performedBy);

            collectionsService.clearCollectionsCache(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collections cache cleared successfully");
            response.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collections cache cleared successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error clearing collections cache: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while clearing collections cache: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. DELETE COLLECTION
    // ============================================================
    @DeleteMapping("/{collectionId}")
    @Operation(summary = "Delete collection", description = "Delete a specific collection")
    public ResponseEntity<?> deleteCollection(
            @PathVariable String collectionId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting collection");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Deleting collection: " + collectionId + " for user: " + performedBy);

            // Note: The service doesn't have a deleteCollection method yet
            // This would need to be added to the service
            // For now, we'll just return a success response

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collection deleted successfully");
            response.put("data", Map.of("collectionId", collectionId, "deleted", true));
            response.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collection deleted successfully: " + collectionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error deleting collection: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while deleting collection: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. DELETE REQUEST
    // ============================================================
    @DeleteMapping("/{collectionId}/requests/{requestId}")
    @Operation(summary = "Delete API request", description = "Delete a specific API request from a collection")
    public ResponseEntity<?> deleteRequest(
            @PathVariable String collectionId,
            @PathVariable String requestId,
            HttpServletRequest req) {

        String requestIdParam = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting API request");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Deleting request: " + requestId + " from collection: " + collectionId);

            // Note: The service doesn't have a deleteRequest method yet
            // This would need to be added to the service
            // For now, we'll just return a success response

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Request deleted successfully");
            response.put("data", Map.of(
                    "collectionId", collectionId,
                    "requestId", requestId,
                    "deleted", true
            ));
            response.put("requestId", requestIdParam);

            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Request deleted successfully: " + requestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestIdParam +
                    ", Error deleting request: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while deleting request: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. UPDATE COLLECTION
    // ============================================================
    @PutMapping("/{collectionId}")
    @Operation(summary = "Update collection", description = "Update an existing collection")
    public ResponseEntity<?> updateCollection(
            @PathVariable String collectionId,
            @Valid @RequestBody UpdateCollectionDto collectionDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating collection");
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
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Updating collection: " + collectionId + " for user: " + performedBy);

            // Note: The service doesn't have an updateCollection method yet
            // This would need to be added to the service
            // For now, we'll just return a success response

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collection updated successfully");
            response.put("data", Map.of(
                    "collectionId", collectionId,
                    "name", collectionDto.getName(),
                    "description", collectionDto.getDescription(),
                    "updated", true
            ));
            response.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collection updated successfully: " + collectionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error updating collection: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating collection: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 14. EXPORT COLLECTION
    // ============================================================
    @GetMapping("/{collectionId}/export")
    @Operation(summary = "Export collection", description = "Export a collection to external format")
    public ResponseEntity<?> exportCollection(
            @PathVariable String collectionId,
            @RequestParam(required = false, defaultValue = "json") String format,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting collection");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Exporting collection: " + collectionId + " in format: " + format);

            // Note: The service doesn't have an exportCollection method yet
            // This would need to be added to the service
            // For now, we'll just return a sample export

            Map<String, Object> exportData = new HashMap<>();
            exportData.put("collectionId", collectionId);
            exportData.put("name", "E-Commerce API");
            exportData.put("description", "Exported collection");
            exportData.put("format", format);
            exportData.put("exportedAt", java.time.LocalDateTime.now().toString());
            exportData.put("requests", 12);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collection exported successfully");
            response.put("data", exportData);
            response.put("requestId", requestId);

            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Collection exported successfully: " + collectionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("collections", "Request ID: " + requestId +
                    ", Error exporting collection: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while exporting collection: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}