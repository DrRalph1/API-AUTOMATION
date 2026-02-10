package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.dtos.systemActivities.role.AppRoleDTO;
import com.usg.apiAutomation.dtos.systemActivities.role.BulkRoleResponseDTO;
import com.usg.apiAutomation.dtos.systemActivities.role.RoleResultDTO;
import com.usg.apiAutomation.entities.UserRoleEntity;
import com.usg.apiAutomation.exceptions.GlobalExceptionHandler.BusinessRuleException;
import com.usg.apiAutomation.exceptions.GlobalExceptionHandler.ConflictException;
import com.usg.apiAutomation.exceptions.GlobalExceptionHandler.ResourceNotFoundException;
import com.usg.apiAutomation.helpers.SortValidationHelper;
import com.usg.apiAutomation.repositories.AppRoleRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final AppRoleRepository roleRepository;
    private final SortValidationHelper sortValidationHelper;

    // List of valid sort fields for UserRoleEntity
    private static final String[] VALID_SORT_FIELDS = {
            "roleId", "roleName", "description", "createdAt", "updatedAt"
    };

    @Transactional
    public AppRoleDTO createRole(AppRoleDTO dto, String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Creating role: {}, Performed by: {}", requestId, dto.getRoleName(), performedBy);

        validateRoleBusinessRules(dto);

        if (roleRepository.existsByRoleNameIgnoreCase(dto.getRoleName())) {
            log.warn("Request ID: {}, Role creation failed - role already exists: {}", requestId, dto.getRoleName());
            throw new ConflictException(String.format(
                    "Role with name '%s' already exists",
                    dto.getRoleName()
            ));
        }

        try {
            UserRoleEntity entity = UserRoleEntity.builder()
                    .roleName(dto.getRoleName())
                    .description(dto.getDescription())
                    .build();

            entity = roleRepository.save(entity);
            dto.setRoleId(entity.getRoleId());

            log.info("Request ID: {}, Role created successfully: {} (ID: {}), Performed by: {}",
                    requestId, dto.getRoleName(), entity.getRoleId(), performedBy);
            return dto;

        } catch (DataIntegrityViolationException ex) {
            log.error("Request ID: {}, Data integrity violation while creating role: {}",
                    requestId, dto.getRoleName(), ex);
            throw new ConflictException("A role with similar data already exists");
        }
    }

    @Transactional
    public BulkRoleResponseDTO createRolesBulk(List<AppRoleDTO> dtos, String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Bulk creating {} roles, Performed by: {}",
                requestId, dtos.size(), performedBy);

        if (dtos == null || dtos.isEmpty()) {
            log.warn("Request ID: {}, Bulk creation failed - no roles provided", requestId);
            throw new IllegalArgumentException("No roles provided for bulk creation");
        }

        // Validate all DTOs first
        dtos.forEach(this::validateRoleBusinessRules);

        List<RoleResultDTO> results = new ArrayList<>();
        List<UserRoleEntity> entitiesToSave = new ArrayList<>();
        AtomicInteger createdCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();

        // Process each DTO
        for (AppRoleDTO dto : dtos) {
            RoleResultDTO result = RoleResultDTO.builder()
                    .roleName(dto.getRoleName())
                    .description(dto.getDescription())
                    .build();

            // Check for duplicate role name
            if (roleRepository.existsByRoleNameIgnoreCase(dto.getRoleName())) {
                result.setStatus("DUPLICATE");
                result.setMessage(String.format(
                        "Role with name '%s' already exists",
                        dto.getRoleName()
                ));
                roleRepository.findByRoleNameIgnoreCase(dto.getRoleName())
                        .ifPresent(existing -> result.setRoleId(existing.getRoleId().toString()));
                duplicateCount.getAndIncrement();
                log.debug("Request ID: {}, Role '{}' is a duplicate", requestId, dto.getRoleName());
            } else {
                UserRoleEntity entity = UserRoleEntity.builder()
                        .roleName(dto.getRoleName())
                        .description(dto.getDescription())
                        .build();
                entitiesToSave.add(entity);
                result.setStatus("PENDING_CREATION");
                createdCount.getAndIncrement();
                log.debug("Request ID: {}, Role '{}' marked for creation", requestId, dto.getRoleName());
            }
            results.add(result);
        }

        // Bulk save all new roles
        if (!entitiesToSave.isEmpty()) {
            try {
                log.debug("Request ID: {}, Saving {} roles in bulk", requestId, entitiesToSave.size());
                List<UserRoleEntity> savedEntities = roleRepository.saveAll(entitiesToSave);

                // Update results with created role IDs
                int savedIndex = 0;
                for (RoleResultDTO result : results) {
                    if ("PENDING_CREATION".equals(result.getStatus())) {
                        UserRoleEntity savedEntity = savedEntities.get(savedIndex);
                        result.setRoleId(savedEntity.getRoleId().toString());
                        result.setStatus("CREATED");
                        result.setMessage("Role created successfully");
                        savedIndex++;
                        log.debug("Request ID: {}, Role '{}' created with ID: {}",
                                requestId, result.getRoleName(), savedEntity.getRoleId());
                    }
                }

            } catch (DataIntegrityViolationException ex) {
                log.error("Request ID: {}, Data integrity violation during bulk role creation", requestId, ex);
                throw new ConflictException("One or more roles already exist");
            }
        }

        // Prepare response
        BulkRoleResponseDTO response = BulkRoleResponseDTO.builder()
                .data(results)
                .build();

        // Set appropriate response code and message
        if (createdCount.get() > 0 && duplicateCount.get() == 0) {
            response.setResponseCode(200);
            response.setMessage("All roles created successfully");
            log.info("Request ID: {}, Bulk creation completed - {} roles created successfully",
                    requestId, createdCount.get());
        } else if (createdCount.get() == 0 && duplicateCount.get() > 0) {
            response.setResponseCode(409);
            response.setMessage("All roles already exist");
            log.warn("Request ID: {}, Bulk creation failed - all {} roles are duplicates",
                    requestId, duplicateCount.get());
        } else if (createdCount.get() > 0 && duplicateCount.get() > 0) {
            response.setResponseCode(207); // Multi-Status
            response.setMessage(String.format(
                    "Bulk creation completed: %d created, %d duplicates",
                    createdCount.get(), duplicateCount.get()
            ));
            log.info("Request ID: {}, Bulk creation partial success - {} created, {} duplicates",
                    requestId, createdCount.get(), duplicateCount.get());
        } else {
            response.setResponseCode(400);
            response.setMessage("No roles were processed");
            log.warn("Request ID: {}, Bulk creation failed - no roles were processed", requestId);
        }

        return response;
    }

    public AppRoleDTO getRole(UUID roleId, String requestId, HttpServletRequest req, String performedBy) {
        log.debug("Request ID: {}, Getting role with ID: {}, Requested by: {}",
                requestId, roleId, performedBy);

        UserRoleEntity entity = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Request ID: {}, Role not found: {}", requestId, roleId);
                    return new ResourceNotFoundException(
                            String.format("Role with ID %s not found", roleId)
                    );
                });

        log.debug("Request ID: {}, Role retrieved: {} (ID: {})",
                requestId, entity.getRoleName(), entity.getRoleId());
        return AppRoleDTO.builder()
                .roleId(entity.getRoleId())
                .roleName(entity.getRoleName())
                .description(entity.getDescription())
                .build();
    }

    public Page<AppRoleDTO> getAllRoles(Pageable pageable, String requestId, HttpServletRequest req, String performedBy) {
        log.debug("Request ID: {}, Getting all roles with pagination - Page: {}, Size: {}, Sort: {}, Requested by: {}",
                requestId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), performedBy);

        // Validate sort fields
        sortValidationHelper.validateSortFieldsOrThrow(pageable.getSort(), VALID_SORT_FIELDS, "Role");

        // Fix sorting field mappings
        Sort sort = pageable.getSort().stream()
                .map(order -> {
                    String property = order.getProperty();
                    // Handle common field name mappings
                    if (property.contains("[")) {
                        property = property.replaceAll("[\\[\\]\"]", "");
                    }
                    // Map 'name' to 'roleName'
                    if ("name".equalsIgnoreCase(property)) {
                        property = "roleName";
                    }
                    return new Sort.Order(order.getDirection(), property);
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), Sort::by));

        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        // Execute the paginated query
        Page<UserRoleEntity> result = roleRepository.findAll(fixedPageable);

        log.debug("Request ID: {}, PAGINATED RESULT - Total elements: {}, Content size: {}, Total pages: {}",
                requestId,
                result.getTotalElements(),
                result.getContent().size(),
                result.getTotalPages());

        return result.map(entity -> AppRoleDTO.builder()
                .roleId(entity.getRoleId())
                .roleName(entity.getRoleName())
                .description(entity.getDescription())
                .build());
    }

    @Transactional
    public AppRoleDTO updateRole(UUID roleId, AppRoleDTO dto, String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Updating role with ID: {}, Performed by: {}",
                requestId, roleId, performedBy);

        UserRoleEntity entity = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Request ID: {}, Role not found for update: {}", requestId, roleId);
                    return new ResourceNotFoundException(
                            String.format("Role with ID %s not found", roleId)
                    );
                });

        validateRoleBusinessRules(dto);

        // Check if new role name conflicts with existing role (excluding current)
        if (!entity.getRoleName().equalsIgnoreCase(dto.getRoleName()) &&
                roleRepository.existsByRoleNameIgnoreCase(dto.getRoleName())) {
            log.warn("Request ID: {}, Role update failed - name '{}' already exists",
                    requestId, dto.getRoleName());
            throw new ConflictException(String.format(
                    "Role name '%s' already exists",
                    dto.getRoleName()
            ));
        }

        log.debug("Request ID: {}, Updating role '{}' (ID: {}) to new name: '{}'",
                requestId, entity.getRoleName(), roleId, dto.getRoleName());

        entity.setRoleName(dto.getRoleName());
        entity.setDescription(dto.getDescription());

        roleRepository.save(entity);
        dto.setRoleId(entity.getRoleId());

        log.info("Request ID: {}, Role updated successfully: {} (ID: {}), Performed by: {}",
                requestId, dto.getRoleName(), entity.getRoleId(), performedBy);
        return dto;
    }

    @Transactional
    public void deleteRole(UUID roleId, String requestId, HttpServletRequest req, String performedBy) {
        log.info("Request ID: {}, Deleting role with ID: {}, Performed by: {}",
                requestId, roleId, performedBy);

        UserRoleEntity entity = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Request ID: {}, Role not found for deletion: {}", requestId, roleId);
                    return new ResourceNotFoundException(
                            String.format("Role with ID %s not found", roleId)
                    );
                });

        log.debug("Request ID: {}, Found role to delete: {} (ID: {})",
                requestId, entity.getRoleName(), roleId);

        // Optional: Check if role is in use before deletion
        boolean isInUse = checkIfRoleIsInUse(roleId);
        if (isInUse) {
            log.warn("Request ID: {}, Role deletion failed - role is in use: {} (ID: {})",
                    requestId, entity.getRoleName(), roleId);
            throw new ConflictException("Role is in use and cannot be deleted");
        }

        roleRepository.deleteById(roleId);
        log.info("Request ID: {}, Role deleted successfully: {} (ID: {}), Performed by: {}",
                requestId, entity.getRoleName(), roleId, performedBy);
    }

    // Optional: Keep this method if you need search functionality (not called by controller)
    public Page<AppRoleDTO> searchRoles(
            String roleName,
            String description,
            Pageable pageable,
            String requestId,
            HttpServletRequest req,
            String performedBy) {
        log.debug("Request ID: {}, Searching roles with filters - name: {}, description: {}, Requested by: {}",
                requestId, roleName, description, performedBy);

        // Validate sort fields
        sortValidationHelper.validateSortFieldsOrThrow(pageable.getSort(), VALID_SORT_FIELDS, "Role");

        // Fix sorting field mappings
        Sort sort = pageable.getSort().stream()
                .map(order -> {
                    String property = order.getProperty();
                    // Handle common field name mappings
                    if (property.contains("[")) {
                        property = property.replaceAll("[\\[\\]\"]", "");
                    }
                    if ("name".equalsIgnoreCase(property)) {
                        property = "roleName";
                    }
                    return new Sort.Order(order.getDirection(), property);
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), Sort::by));

        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        // Create Specification for dynamic query
        Specification<UserRoleEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by role name
            if (StringUtils.hasText(roleName)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("roleName")),
                        "%" + roleName.toLowerCase() + "%"));
            }

            // Filter by description
            if (StringUtils.hasText(description)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + description.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<UserRoleEntity> result = roleRepository.findAll(spec, fixedPageable);

        log.debug("Request ID: {}, SEARCH RESULT - Total elements: {}, Content size: {}, Total pages: {}",
                requestId,
                result.getTotalElements(),
                result.getContent().size(),
                result.getTotalPages());

        return result.map(entity -> AppRoleDTO.builder()
                .roleId(entity.getRoleId())
                .roleName(entity.getRoleName())
                .description(entity.getDescription())
                .build());
    }

    private void validateRoleBusinessRules(AppRoleDTO dto) {
        if (dto.getRoleName() == null || dto.getRoleName().trim().isEmpty()) {
            throw new BusinessRuleException("Role name cannot be empty");
        }

        dto.setRoleName(dto.getRoleName().trim());

        if (dto.getRoleName().length() > 50) {
            throw new BusinessRuleException("Role name cannot exceed 50 characters");
        }

        // Validate role name format (alphanumeric with underscores)
        if (!dto.getRoleName().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessRuleException(
                    "Role name can only contain alphanumeric characters and underscores"
            );
        }

        if (dto.getDescription() != null) {
            dto.setDescription(dto.getDescription().trim());
            if (dto.getDescription().length() > 255) {
                throw new BusinessRuleException("Description cannot exceed 255 characters");
            }
        }
    }

    // Helper method (if needed for role usage check)
    private boolean checkIfRoleIsInUse(UUID roleId) {
        // Implement logic to check if role is assigned to any users
        // This would require a user-role relationship repository
        // For now, return false (allow deletion)
        return false;
    }
}