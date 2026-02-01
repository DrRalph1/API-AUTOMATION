package com.usg.apiAutomation.controllers;

import com.usg.apiAutomation.dtos.role.AppRoleDTO;
import com.usg.apiAutomation.dtos.ApiResponseDTO;
import com.usg.apiAutomation.dtos.role.BulkRoleResponseDTO;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.helpers.AuditLogHelper;
import com.usg.apiAutomation.services.AppRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/plx/api/roles")
@RequiredArgsConstructor
@Tag(name = "APP ROLES", description = "Endpoints for managing application roles")
public class AppRoleController {

    private final AppRoleService roleService;
    private final JwtHelper jwtHelper;
    private final AuditLogHelper auditLogHelper;

    @PostMapping
    @Operation(summary = "Create a new role", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "409", description = "Role already exists"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponseDTO<AppRoleDTO>> createRole(
            @Valid @RequestBody AppRoleDTO dto,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "creating a role");
        if (authValidation != null) {
            log.warn("Request ID: {}, Authorization failed for creating role: {}", requestId, dto.getRoleName());
            return (ResponseEntity<ApiResponseDTO<AppRoleDTO>>) authValidation;
        }

        // Extract performedBy from token
        String performedBy = jwtHelper.extractPerformedBy(req);

        log.info("Request ID: {}, Creating role: {}, Requested by: {}", requestId, dto.getRoleName(), performedBy);
        auditLogHelper.logAuditAction("CREATE_ROLE_REQUEST", performedBy,
                String.format("Creating role: %s", dto.getRoleName()), requestId);

        AppRoleDTO created = roleService.createRole(dto, requestId, req, performedBy);

        auditLogHelper.logAuditAction("CREATE_ROLE_SUCCESS", performedBy,
                String.format("Role created successfully. Role ID: %s", created.getRoleId()), requestId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created("Role created successfully", created));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple roles in bulk", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All roles created successfully"),
            @ApiResponse(responseCode = "207", description = "Partial success (some roles created, some duplicates)"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "409", description = "All roles already exist"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<BulkRoleResponseDTO> createRolesBulk(
            @Valid @RequestBody List<AppRoleDTO> dtos,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "bulk creating roles");
        if (authValidation != null) {
            log.warn("Request ID: {}, Authorization failed for bulk create roles", requestId);
            return (ResponseEntity<BulkRoleResponseDTO>) authValidation;
        }

        // Extract performedBy from token
        String performedBy = jwtHelper.extractPerformedBy(req);

        log.info("Request ID: {}, Bulk creating {} roles, Requested by: {}", requestId, dtos.size(), performedBy);
        auditLogHelper.logAuditAction("BULK_CREATE_ROLES_REQUEST", performedBy,
                String.format("Bulk creating %d roles", dtos.size()), requestId);

        BulkRoleResponseDTO response = roleService.createRolesBulk(dtos, requestId, req, performedBy);

        auditLogHelper.logAuditAction("BULK_CREATE_ROLES_COMPLETED", performedBy,
                String.format("Bulk create completed. Response code: %d, Message: %s", response.getResponseCode(), response.getMessage()),
                requestId);

        HttpStatus httpStatus = switch (response.getResponseCode()) {
            case 200 -> HttpStatus.OK;
            case 207 -> HttpStatus.MULTI_STATUS;
            case 409 -> HttpStatus.CONFLICT;
            default -> HttpStatus.valueOf(response.getResponseCode());
        };

        return ResponseEntity.status(httpStatus).body(response);
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get a single role by ID", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponseDTO<AppRoleDTO>> getRole(
            @PathVariable UUID roleId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting a role");
        if (authValidation != null) {
            log.warn("Request ID: {}, Authorization failed for getting role: {}", requestId, roleId);
            return (ResponseEntity<ApiResponseDTO<AppRoleDTO>>) authValidation;
        }

        // Extract performedBy from token
        String performedBy = jwtHelper.extractPerformedBy(req);

        log.debug("Request ID: {}, Getting role with ID: {}, Requested by: {}", requestId, roleId, performedBy);
        auditLogHelper.logAuditAction("RETRIEVE_ROLE", performedBy,
                String.format("Getting role with ID: %s", roleId), requestId);

        AppRoleDTO dto = roleService.getRole(roleId, requestId, req, performedBy);

        auditLogHelper.logAuditAction("RETRIEVE_ROLE_SUCCESS", performedBy,
                String.format("Role retrieved successfully. Role ID: %s", dto.getRoleId()), requestId);

        return ResponseEntity.ok(ApiResponseDTO.success("Role retrieved successfully", dto));
    }

    @GetMapping
    @Operation(summary = "Get all roles (paginated + sortable)", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No roles found"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Authorization required")
    })
    public ResponseEntity<ApiResponseDTO<Page<AppRoleDTO>>> getAllRoles(
            @PageableDefault(size = 10, sort = "roleName", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all roles");
        if (authValidation != null) {
            log.warn("Request ID: {}, Authorization failed for getting all roles", requestId);
            return (ResponseEntity<ApiResponseDTO<Page<AppRoleDTO>>>) authValidation;
        }

        // Extract performedBy from token
        String performedBy = jwtHelper.extractPerformedBy(req);

        log.debug("Request ID: {}, Getting all roles with pagination - Page: {}, Size: {}, Sort: {}, Requested by: {}",
                requestId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), performedBy);
        auditLogHelper.logAuditAction("GET_ALL_ROLES", performedBy,
                String.format("Getting all roles - Page: %d, Size: %d, Sort: %s",
                        pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()),
                requestId);

        Page<AppRoleDTO> page = roleService.getAllRoles(pageable, requestId, req, performedBy);

        log.debug("Request ID: {}, Service returned - Total: {}, Content size: {}, Empty: {}",
                requestId, page.getTotalElements(), page.getContent().size(), page.isEmpty());

        if (page.isEmpty()) {
            log.info("Request ID: {}, No roles found in database", requestId);
            auditLogHelper.logAuditAction("GET_ALL_ROLES_NO_CONTENT", performedBy,
                    "No roles found", requestId);

            // Return 200 with empty page instead of 204
            return ResponseEntity.ok(
                    ApiResponseDTO.success(
                            "No roles found",
                            page // This will be an empty page with totalElements = 0
                    )
            );
        }

        auditLogHelper.logAuditAction("GET_ALL_ROLES_SUCCESS", performedBy,
                String.format("Retrieved %d roles across %d pages", page.getTotalElements(), page.getTotalPages()),
                requestId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Roles retrieved successfully",
                        page
                )
        );
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update a role by ID", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "Role name already exists"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponseDTO<AppRoleDTO>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AppRoleDTO dto,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating a role");
        if (authValidation != null) {
            log.warn("Request ID: {}, Authorization failed for updating role: {}", requestId, roleId);
            return (ResponseEntity<ApiResponseDTO<AppRoleDTO>>) authValidation;
        }

        // Extract performedBy from token
        String performedBy = jwtHelper.extractPerformedBy(req);

        log.info("Request ID: {}, Updating role with ID: {}, Requested by: {}", requestId, roleId, performedBy);
        auditLogHelper.logAuditAction("UPDATE_ROLE_REQUEST", performedBy,
                String.format("Updating role with ID: %s, New name: %s", roleId, dto.getRoleName()), requestId);

        AppRoleDTO updated = roleService.updateRole(roleId, dto, requestId, req, performedBy);

        auditLogHelper.logAuditAction("UPDATE_ROLE_SUCCESS", performedBy,
                String.format("Role updated successfully. Role ID: %s", updated.getRoleId()), requestId);

        return ResponseEntity.ok(ApiResponseDTO.success("Role updated successfully", updated));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete a role by ID", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "Role is in use and cannot be deleted")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteRole(
            @PathVariable UUID roleId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting a role");
        if (authValidation != null) {
            log.warn("Request ID: {}, Authorization failed for deleting role: {}", requestId, roleId);
            return (ResponseEntity<ApiResponseDTO<Void>>) authValidation;
        }

        // Extract performedBy from token
        String performedBy = jwtHelper.extractPerformedBy(req);

        log.info("Request ID: {}, Deleting role with ID: {}, Requested by: {}", requestId, roleId, performedBy);
        auditLogHelper.logAuditAction("DELETE_ROLE_REQUEST", performedBy,
                String.format("Deleting role with ID: %s", roleId), requestId);

        roleService.deleteRole(roleId, requestId, req, performedBy);

        log.info("Request ID: {}, Role deleted successfully: {}", requestId, roleId);
        auditLogHelper.logAuditAction("DELETE_ROLE_SUCCESS", performedBy,
                String.format("Role deleted successfully. Role ID: %s", roleId), requestId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Role deleted successfully",
                        null
                )
        );
    }
}