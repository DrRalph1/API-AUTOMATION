package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.userManagement.*;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.services.UserManagementService;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/user-management")
@RequiredArgsConstructor
@Tag(name = "USER MANAGEMENT", description = "Endpoints for user management operations")
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final LoggerUtil loggerUtil;
    private final JwtHelper jwtHelper;

    // ============================================================
    // 1. GET USERS LIST WITH FILTERS
    // ============================================================
    @GetMapping("/users")
    @Operation(summary = "Get users list", description = "Retrieve users list with filtering, sorting and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUsersList(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "all") String roleFilter,
            @RequestParam(required = false, defaultValue = "all") String statusFilter,
            @RequestParam(required = false, defaultValue = "fullName") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting users list");
        if (authValidation != null) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Authorization failed for getting users list");
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Getting users list for user: " + performedBy);

            UsersListResponseDTO users = userManagementService.getUsersList(
                    requestId, performedBy, searchQuery, roleFilter, statusFilter,
                    sortField, sortDirection, page, pageSize);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "Users list retrieved successfully");
            response.put("data", users);
            response.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Users list retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error getting users list: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting users list: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 2. GET USER DETAILS
    // ============================================================
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details", description = "Retrieve detailed information for a specific user")
    public ResponseEntity<?> getUserDetails(
            @PathVariable String userId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting user details");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Getting user details for: " + userId);

            UserDetailsResponseDTO details = userManagementService.getUserDetails(
                    requestId, performedBy, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "User details retrieved successfully");
            response.put("data", details);
            response.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User details retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error getting user details: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting user details: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 3. CREATE NEW USER
    // ============================================================
    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Create a new user account")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody CreateUserRequestDTO requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "creating user");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Creating new user with email: " + requestDto.getEmail());

            CreateUserResponseDTO response = userManagementService.createUser(
                    requestId, performedBy, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 201);
            apiResponse.put("message", "User created successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error creating user: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while creating user: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 4. UPDATE USER
    // ============================================================
    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user", description = "Update an existing user account")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequestDTO requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating user");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Updating user: " + userId);

            UpdateUserResponseDTO response = userManagementService.updateUser(
                    requestId, performedBy, userId, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "User updated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User updated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error updating user: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating user: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 5. DELETE USER
    // ============================================================
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Delete a user account")
    public ResponseEntity<?> deleteUser(
            @PathVariable String userId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting user");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Deleting user: " + userId);

            DeleteUserResponseDTO response = userManagementService.deleteUser(
                    requestId, performedBy, userId);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "User deleted successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User deleted successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error deleting user: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while deleting user: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 6. BULK OPERATIONS
    // ============================================================
    @PostMapping("/users/bulk")
    @Operation(summary = "Bulk operations", description = "Perform bulk operations on multiple users")
    public ResponseEntity<?> bulkOperation(
            @Valid @RequestBody BulkOperationRequestDTO requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "performing bulk operations");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Performing bulk operation: " + requestDto.getOperation());

            BulkOperationResponseDTO response = userManagementService.bulkOperation(
                    requestId, performedBy, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Bulk operation completed successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Bulk operation completed successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error performing bulk operation: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while performing bulk operation: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 7. RESET USER PASSWORD
    // ============================================================
    @PostMapping("/users/{userId}/reset-password")
    @Operation(summary = "Reset password", description = "Reset password for a user account")
    public ResponseEntity<?> resetPassword(
            @PathVariable String userId,
            @Valid @RequestBody ResetPasswordRequestDTO requestDto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "resetting password");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Resetting password for user: " + userId);

            // Set userId from path variable
            requestDto.setUserId(userId);

            ResetPasswordResponseDTO response = userManagementService.resetPassword(
                    requestId, performedBy, requestDto);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Password reset initiated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Password reset initiated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error resetting password: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while resetting password: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 8. GET USER STATISTICS
    // ============================================================
    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Retrieve user management statistics")
    public ResponseEntity<?> getUserStatistics(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting user statistics");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Getting user statistics");

            UserStatisticsResponseDTO statistics = userManagementService.getUserStatistics(
                    requestId, performedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "User statistics retrieved successfully");
            response.put("data", statistics);
            response.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User statistics retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error getting user statistics: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting user statistics: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 9. SEARCH USERS
    // ============================================================
    @PostMapping("/users/search")
    @Operation(summary = "Search users", description = "Search users with advanced criteria")
    public ResponseEntity<?> searchUsers(
            @Valid @RequestBody SearchUsersRequestDTO searchRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching users");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Searching users with query: " + searchRequest.getQuery());

            SearchUsersResponseDTO response = userManagementService.searchUsers(
                    requestId, performedBy, searchRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Search completed successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Search completed successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error searching users: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while searching users: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 10. IMPORT USERS
    // ============================================================
    @PostMapping("/users/import")
    @Operation(summary = "Import users", description = "Import users from external file")
    public ResponseEntity<?> importUsers(
            @Valid @RequestBody ImportUsersRequestDTO importRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "importing users");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Importing users from file: " + importRequest.getFileName());

            ImportUsersResponseDTO response = userManagementService.importUsers(
                    requestId, performedBy, importRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Users imported successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Users imported successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error importing users: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while importing users: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 11. EXPORT USERS
    // ============================================================
    @PostMapping("/users/export")
    @Operation(summary = "Export users", description = "Export users data in specified format")
    public ResponseEntity<?> exportUsers(
            @Valid @RequestBody ExportUsersRequestDTO exportRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "exporting users");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Exporting users in format: " + exportRequest.getFormat());

            ExportUsersResponseDTO response = userManagementService.exportUsers(
                    requestId, performedBy, exportRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Users exported successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Users exported successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error exporting users: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while exporting users: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 12. GET USER ACTIVITY LOG
    // ============================================================
    @GetMapping("/users/{userId}/activity")
    @Operation(summary = "Get user activity", description = "Retrieve activity log for a specific user")
    public ResponseEntity<?> getUserActivity(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting user activity");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Getting activity log for user: " + userId);

            UserActivityResponseDTO activity = userManagementService.getUserActivity(
                    requestId, performedBy, userId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("responseCode", 200);
            response.put("message", "User activity retrieved successfully");
            response.put("data", activity);
            response.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User activity retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error getting user activity: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting user activity: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 13. UPDATE USER STATUS
    // ============================================================
    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Update user status", description = "Update status for a specific user")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable String userId,
            @RequestParam String status,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating user status");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Updating status for user: " + userId + " to " + status);

            UpdateStatusResponseDTO response = userManagementService.updateUserStatus(
                    requestId, performedBy, userId, status);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "User status updated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User status updated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error updating user status: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while updating user status: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 14. GET ROLES AND PERMISSIONS
    // ============================================================
    @GetMapping("/roles")
    @Operation(summary = "Get roles and permissions", description = "Retrieve all roles and permissions")
    public ResponseEntity<?> getRolesAndPermissions(HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting roles and permissions");
        if (authValidation != null) {
            return authValidation;
        }

        try {
            String performedBy = jwtHelper.extractPerformedBy(req);
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Getting roles and permissions");

            RolesPermissionsResponseDTO response = userManagementService.getRolesAndPermissions(
                    requestId, performedBy);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "Roles and permissions retrieved successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Roles and permissions retrieved successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error getting roles and permissions: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting roles and permissions: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 15. VALIDATE USER DATA
    // ============================================================
    @PostMapping("/validate")
    @Operation(summary = "Validate user data", description = "Validate user data before creation or update")
    public ResponseEntity<?> validateUserData(
            @Valid @RequestBody ValidateUserRequestDTO validationRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "validating user data");
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
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Validating user data");

            ValidationResponseDTO response = userManagementService.validateUserData(
                    requestId, performedBy, validationRequest);

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("responseCode", 200);
            apiResponse.put("message", "User data validated successfully");
            apiResponse.put("data", response);
            apiResponse.put("requestId", requestId);

            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", User data validated successfully");

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            loggerUtil.log("user-management", "RequestEntity ID: " + requestId +
                    ", Error validating user data: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while validating user data: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}