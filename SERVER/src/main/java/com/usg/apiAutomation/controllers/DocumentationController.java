package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.documentation.*;
import com.usg.apiAutomation.entities.documentation.FolderEntity;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.repositories.documentation.FolderRepository;
import com.usg.apiAutomation.services.DocumentationService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/documentation")
@RequiredArgsConstructor
@Tag(name = "DOCUMENTATION", description = "Endpoints for API documentation management")
public class DocumentationController {

    private final DocumentationService documentationService;
    private final FolderRepository folderRepository;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET API COLLECTIONS
    // ============================================================
    @GetMapping("/collections")
    @Operation(summary = "Get API collections", description = "Retrieve all API collections for documentation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API collections retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAPICollections(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting API collections");
        if (authValidation != null) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting API collections");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Getting API collections for user: " + performedBy);

            APICollectionResponseDTO collections = documentationService.getAPICollections(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "API collections retrieved successfully");
            response.put("data", collections);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", API collections retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error getting API collections: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting API collections: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET API ENDPOINTS
    // ============================================================
    @GetMapping("/collections/{collectionId}/folders/{folderId}/endpoints")
    @Operation(summary = "Get API endpoints", description = "Retrieve endpoints for a specific collectionEntity and folderEntity")
    public ResponseEntity<?> getAPIEndpoints(
            @PathVariable String collectionId,
            @PathVariable String folderId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting API endpoints");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Getting API endpoints for collectionEntity: " + collectionId + ", folderEntity: " + folderId);

            APIEndpointResponseDTO endpoints = documentationService.getAPIEndpoints(
                    requestId, req, performedBy, collectionId, folderId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "API endpoints retrieved successfully");
            response.put("data", endpoints);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", API endpoints retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error getting API endpoints: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting API endpoints: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // ============================================================
    // 3. GET ENDPOINT DETAILS
    // ============================================================
    @GetMapping("/collections/{collectionId}/endpoints/{endpointId}")
    @Operation(summary = "Get endpoint details", description = "Retrieve detailed information for a specific endpoint")
    public ResponseEntity<?> getEndpointDetails(
            @PathVariable String collectionId,
            @PathVariable String endpointId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting endpoint details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Getting endpoint details for: " + endpointId);

            EndpointDetailResponseDTO details = documentationService.getEndpointDetails(
                    requestId, req, performedBy, collectionId, endpointId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Endpoint details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Endpoint details retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error getting endpoint details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting endpoint details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. GET CODE EXAMPLES
    // ============================================================
    @GetMapping("/endpoints/{endpointId}/code-examples")
    @Operation(summary = "Get code examples", description = "Retrieve code examples for a specific endpoint in various languages")
    public ResponseEntity<?> getCodeExamples(
            @PathVariable String endpointId,
            @RequestParam String language,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting code examples");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Getting code examples for endpoint: " + endpointId + ", language: " + language);

            CodeExampleResponseDTO examples = documentationService.getCodeExamples(
                    requestId, req, performedBy, endpointId, language);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Code examples retrieved successfully");
            response.put("data", examples);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Code examples retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error getting code examples: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting code examples: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. SEARCH DOCUMENTATION
    // ============================================================
    @GetMapping("/search")
    @Operation(summary = "Search documentation", description = "Search across API documentation")
    public ResponseEntity<?> searchDocumentation(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "10") int maxResults,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching documentation");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Searching documentation with query: " + query + ", type: " + type);

            SearchDocumentationResponseDTO searchResults = documentationService.searchDocumentation(
                    requestId, req, performedBy, query, type, maxResults);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Documentation search completed successfully");
            response.put("data", searchResults);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Documentation search completed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error searching documentation: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching documentation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. PUBLISH DOCUMENTATION
    // ============================================================
    @PostMapping("/publish")
    @Operation(summary = "Publish documentation", description = "Publish API documentation")
    public ResponseEntity<?> publishDocumentation(
            @Valid @RequestBody PublishDocumentationRequestDto publishRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "publishing documentation");
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
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Publishing documentation for collectionEntity: " + publishRequest.getCollectionId());

            PublishDocumentationResponseDTO publishResponse = documentationService.publishDocumentation(
                    requestId, req, performedBy,
                    publishRequest.getCollectionId(),
                    publishRequest.getTitle(),
                    publishRequest.getVisibility(),
                    publishRequest.getCustomDomain());

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Documentation published successfully");
            response.put("data", publishResponse);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Documentation published successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error publishing documentation: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while publishing documentation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. GET ENVIRONMENTS
    // ============================================================
    @GetMapping("/environments")
    @Operation(summary = "Get environments", description = "Retrieve available environments for API documentation")
    public ResponseEntity<?> getEnvironments(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting environments");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Getting environments for user: " + performedBy);

            EnvironmentResponseDTO environments = documentationService.getEnvironments(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Environments retrieved successfully");
            response.put("data", environments);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Environments retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error getting environments: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting environments: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. GET NOTIFICATIONS
    // ============================================================
    @GetMapping("/notifications")
    @Operation(summary = "Get notifications", description = "Retrieve documentation-related notifications")
    public ResponseEntity<?> getNotifications(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting notifications");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Getting notifications for user: " + performedBy);

            NotificationResponseDTO notifications = documentationService.getNotifications(requestId, req, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Notifications retrieved successfully");
            response.put("data", notifications);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Notifications retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error getting notifications: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting notifications: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. GET CHANGELOG
    // ============================================================
    @GetMapping("/collections/{collectionId}/changelog")
    @Operation(summary = "Get changelog", description = "Retrieve changelog for a specific collectionEntity")
    public ResponseEntity<?> getChangelog(
            @PathVariable String collectionId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting changelog");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Getting changelog for collectionEntity: " + collectionId);

            ChangelogResponseDTO changelog = documentationService.getChangelog(requestId, req, performedBy, collectionId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Changelog retrieved successfully");
            response.put("data", changelog);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Changelog retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error getting changelog: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting changelog: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. GENERATE MOCK SERVER
    // ============================================================
    @PostMapping("/generate-mock")
    @Operation(summary = "Generate mock server", description = "Generate a mock server for API documentation")
    public ResponseEntity<?> generateMockServer(
            @Valid @RequestBody GenerateMockRequestDto mockRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "generating mock server");
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
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Generating mock server for collectionEntity: " + mockRequest.getCollectionId());

            GenerateMockResponseDTO mockResponse = documentationService.generateMockServer(
                    requestId, req, performedBy, mockRequest.getCollectionId(), mockRequest.getOptions());

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Mock server generated successfully");
            response.put("data", mockResponse);
            response.put("requestId", requestId);

            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Mock server generated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("documentation", "RequestEntity ID: " + requestId +
                    ", Error generating mock server: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while generating mock server: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // DTO CLASSES FOR REQUEST BODIES
    // ============================================================

    @Setter
    @Getter
    public static class PublishDocumentationRequestDto {
        // Getters and setters
        private String collectionId;
        private String title;
        private String visibility;
        private String customDomain;

    }

    @Setter
    @Getter
    public static class GenerateMockRequestDto {
        // Getters and setters
        private String collectionId;
        private Map<String, String> options;

    }


    // ============================================================
    // 12. GET API FOLDERS
    // ============================================================
    @GetMapping("/collections/{collectionId}/folders")
    @Operation(summary = "Get folders", description = "Retrieve folders for a specific collection")
    public ResponseEntity<?> getFolders(
            @PathVariable String collectionId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting folders");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("documentation", "Request ID: " + requestId +
                    ", Getting folders for collection: " + collectionId);

            // Get folders from database
            List<FolderEntity> folders = folderRepository.findByCollectionId(collectionId);

            // Convert to DTOs
            List<FolderDTO> folderDTOs = folders.stream()
                    .map(FolderDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("folders", folderDTOs);
            responseData.put("collectionId", collectionId);
            responseData.put("totalFolders", folderDTOs.size());
            responseData.put("timestamp", LocalDateTime.now().toString());

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Folders retrieved successfully");
            apiResponse.put("data", responseData);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("documentation", "Request ID: " + requestId +
                    ", Folders retrieved successfully: " + folderDTOs.size());

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("documentation", "Request ID: " + requestId +
                    ", Error getting folders: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting folders: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


}