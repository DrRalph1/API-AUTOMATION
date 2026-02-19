package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.codeBase.*;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.CodeBaseService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/plx/api/codebase")
@RequiredArgsConstructor
@Tag(name = "CODEBASE", description = "Endpoints for codebase and API implementation management")
public class CodeBaseController {

    private final CodeBaseService codeBaseService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET COLLECTIONS LIST
    // ============================================================
    @GetMapping("/collections")
    @Operation(summary = "Get collections list", description = "Retrieve all collections for the authenticated user from codebase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collections list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCollectionsList(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting collections list from codebase");
        if (authValidation != null) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting collections list from codebase");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Getting collections list from codebase for user: " + performedBy);

            CollectionsListResponse collections = codeBaseService.getCollectionsList(requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Collections list retrieved successfully from codebase");
            response.put("data", collections);
            response.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Collections list retrieved successfully from codebase");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error getting collections list from codebase: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting collections list from codebase: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET COLLECTION DETAILS
    // ============================================================
    @GetMapping("/collections/{collectionId}")
    @Operation(summary = "Get collectionEntity details", description = "Retrieve details for a specific collectionEntity from codebase")
    public ResponseEntity<?> getCollectionDetails(
            @PathVariable String collectionId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting collectionEntity details from codebase");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Getting collectionEntity details from codebase for: " + collectionId);

            CollectionDetailsResponse details = codeBaseService.getCollectionDetails(
                    requestId, performedBy, collectionId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "CollectionEntity details retrieved successfully from codebase");
            response.put("data", details);
            response.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", CollectionEntity details retrieved successfully from codebase");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error getting collectionEntity details from codebase: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting collectionEntity details from codebase: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. GET FOLDER REQUESTS
    // ============================================================
    @GetMapping("/collections/{collectionId}/folders/{folderId}/requests")
    @Operation(summary = "Get folderEntity requestEntities", description = "Retrieve all requestEntities in a specific folderEntity")
    public ResponseEntity<?> getFolderRequests(
            @PathVariable String collectionId,
            @PathVariable String folderId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting folderEntity requestEntities from codebase");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Getting folderEntity requestEntities from codebase for folderEntity: " + folderId + " in collectionEntity: " + collectionId);

            FolderRequestsResponse details = codeBaseService.getFolderRequests(
                    requestId, performedBy, collectionId, folderId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "FolderEntity requestEntities retrieved successfully from codebase");
            response.put("data", details);
            response.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", FolderEntity requestEntities retrieved successfully from codebase");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error getting folderEntity requestEntities from codebase: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting folderEntity requestEntities from codebase: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. GET REQUEST DETAILS
    // ============================================================
    @GetMapping("/collections/{collectionId}/requests/{requestId}")
    @Operation(summary = "Get requestEntity details", description = "Retrieve details for a specific API requestEntity from codebase")
    public ResponseEntity<?> getRequestDetails(
            @PathVariable String collectionId,
            @PathVariable String requestId,
            HttpServletRequest req) {

        String requestIdParam = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting requestEntity details from codebase");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", Getting requestEntity details from codebase for: " + requestId);

            RequestDetailsResponse details = codeBaseService.getRequestDetails(
                    requestIdParam, performedBy, collectionId, requestId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "RequestEntity details retrieved successfully from codebase");
            response.put("data", details);
            response.put("requestId", requestIdParam);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", RequestEntity details retrieved successfully from codebase");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", Error getting requestEntity details from codebase: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting requestEntity details from codebase: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. GET IMPLEMENTATION DETAILS
    // ============================================================
    @GetMapping("/collections/{collectionId}/requests/{requestId}/implementations/{language}/{component}")
    @Operation(summary = "Get implementation details", description = "Retrieve implementation details for a specific component and language")
    public ResponseEntity<?> getImplementationDetails(
            @PathVariable String collectionId,
            @PathVariable String requestId,
            @PathVariable String language,
            @PathVariable String component,
            HttpServletRequest req) {

        String requestIdParam = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting implementation details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", Getting implementation details for language: " + language + ", component: " + component);

            ImplementationResponse details = codeBaseService.getImplementationDetails(
                    requestIdParam, performedBy, collectionId, requestId, language, component);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "ImplementationEntity details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestIdParam);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", ImplementationEntity details retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", Error getting implementation details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting implementation details: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. GET ALL IMPLEMENTATIONS FOR REQUEST
    // ============================================================
    @GetMapping("/collections/{collectionId}/requests/{requestId}/implementations")
    @Operation(summary = "Get all implementationEntities", description = "Retrieve all available implementationEntities for a specific requestEntity")
    public ResponseEntity<?> getAllImplementations(
            @PathVariable String collectionId,
            @PathVariable String requestId,
            HttpServletRequest req) {

        String requestIdParam = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all implementationEntities");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", Getting all implementationEntities for requestEntity: " + requestId);

            AllImplementationsResponse implementations = codeBaseService.getAllImplementations(
                    requestIdParam, performedBy, collectionId, requestId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "All implementationEntities retrieved successfully");
            response.put("data", implementations);
            response.put("requestId", requestIdParam);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", All implementationEntities retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestIdParam +
                    ", Error getting all implementationEntities: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting all implementationEntities: " + e.getMessage());
            errorResponse.put("requestId", requestIdParam);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. GENERATE IMPLEMENTATION
    // ============================================================
    @PostMapping("/generate-implementation")
    @Operation(summary = "Generate implementation", description = "Generate API implementation in specified language")
    public ResponseEntity<?> generateImplementation(
            @Valid @RequestBody GenerateImplementationRequest requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating implementation");
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
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Generating implementation for language: " + requestDto.getLanguage());

            GenerateImplementationResponse response = codeBaseService.generateImplementation(
                    requestId, performedBy, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "ImplementationEntity generated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", ImplementationEntity generated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error generating implementation: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while generating implementation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. EXPORT IMPLEMENTATION
    // ============================================================
    @PostMapping("/export")
    @Operation(summary = "Export implementation", description = "Export API implementation in specified format")
    public ResponseEntity<?> exportImplementation(
            @Valid @RequestBody ExportRequest exportRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting implementation");
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
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Exporting implementation in format: " + exportRequest.getFormat());

            ExportResponse response = codeBaseService.exportImplementation(
                    requestId, performedBy, exportRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "ImplementationEntity exported successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", ImplementationEntity exported successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error exporting implementation: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while exporting implementation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. GET LANGUAGES
    // ============================================================
    @GetMapping("/languages")
    @Operation(summary = "Get languages", description = "Retrieve available programming languages for implementationEntities")
    public ResponseEntity<?> getLanguages(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting languages");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Getting available languages");

            LanguagesResponse languages = codeBaseService.getLanguages(requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Languages retrieved successfully");
            response.put("data", languages);
            response.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Languages retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error getting languages: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting languages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. SEARCH IMPLEMENTATIONS
    // ============================================================
    @PostMapping("/search")
    @Operation(summary = "Search implementationEntities", description = "Search for implementationEntities across collections")
    public ResponseEntity<?> searchImplementations(
            @Valid @RequestBody SearchRequest searchRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching implementationEntities");
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
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Searching implementationEntities for query: " + searchRequest.getQuery());

            SearchResponse response = codeBaseService.searchImplementations(
                    requestId, performedBy, searchRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Search completed successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Search completed successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error searching implementationEntities: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching implementationEntities: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. IMPORT SPECIFICATION
    // ============================================================
    @PostMapping("/import")
    @Operation(summary = "Import specification", description = "Import API specification from external source")
    public ResponseEntity<?> importSpecification(
            @Valid @RequestBody ImportSpecRequest importRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "importing specification");
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
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Importing specification from source: " + importRequest.getSource());

            ImportSpecResponse response = codeBaseService.importSpecification(
                    requestId, performedBy, importRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Specification imported successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Specification imported successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error importing specification: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while importing specification: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. VALIDATE IMPLEMENTATION
    // ============================================================
    @PostMapping("/validate")
    @Operation(summary = "Validate implementation", description = "Validate generated implementation code")
    public ResponseEntity<?> validateImplementation(
            @RequestBody ValidateImplementationRequest validationRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating implementation");
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
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Validating implementation for language: " + validationRequest.getLanguage());

            ValidationResponse response = codeBaseService.validateImplementation(
                    requestId, performedBy, validationRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "ImplementationEntity validated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", ImplementationEntity validated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error validating implementation: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while validating implementation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. TEST IMPLEMENTATION
    // ============================================================
    @PostMapping("/test")
    @Operation(summary = "Test implementation", description = "Test generated implementation with sample data")
    public ResponseEntity<?> testImplementation(
            @RequestBody TestImplementationRequest testRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "testing implementation");
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
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Testing implementation for requestEntity: " + testRequest.getRequestId());

            TestResponse response = codeBaseService.testImplementation(
                    requestId, performedBy, testRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "ImplementationEntity tested successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", ImplementationEntity tested successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error testing implementation: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while testing implementation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 15. GET SUPPORTED PROGRAMMING LANGUAGES
    // ============================================================
    @GetMapping("/supported-languages")
    @Operation(summary = "Get supported programming languages", description = "Retrieve all supported programming languages with details")
    public ResponseEntity<?> getSupportedProgrammingLanguages(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting supported languages");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Getting supported programming languages");

            Map<String, Object> response = codeBaseService.getSupportedProgrammingLanguages(requestId, performedBy);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Supported programming languages retrieved successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Supported programming languages retrieved successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error getting supported programming languages: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting supported programming languages: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 16. GET QUICK START GUIDE
    // ============================================================
    @GetMapping("/quick-start/{language}")
    @Operation(summary = "Get quick start guide", description = "Retrieve quick start guide for a specific language")
    public ResponseEntity<?> getQuickStartGuide(
            @PathVariable String language,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting quick start guide");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Getting quick start guide for language: " + language);

            Map<String, Object> guide = codeBaseService.getQuickStartGuide(requestId, performedBy, language);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Quick start guide retrieved successfully");
            response.put("data", guide);
            response.put("requestId", requestId);

            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Quick start guide retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("codebase", "RequestEntity ID: " + requestId +
                    ", Error getting quick start guide: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting quick start guide: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}