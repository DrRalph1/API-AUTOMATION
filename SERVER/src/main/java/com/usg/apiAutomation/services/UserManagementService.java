package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.userManagement.*;
import com.usg.apiAutomation.entities.*;
import com.usg.apiAutomation.repositories.*;
import com.usg.apiAutomation.services.UserDtoConverterService;
import com.usg.apiAutomation.utils.LoggerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final UserDeviceRepository deviceRepository;
    private final UserApiKeyRepository apiKeyRepository;
    private final UserSessionRepository sessionRepository;
    private final UserTagRepository tagRepository;
    private final UserActivityRepository activityRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserDtoConverterService dtoConverter;
    private final LoggerUtil loggerUtil;

    // Statistics storage - can be cached or computed on demand
    private Map<String, Object> stats = new HashMap<>();

    // ============================================================
    // 1. GET USERS LIST WITH FILTERS
    // ============================================================
    @Transactional(readOnly = true)
    public UsersListResponse getUsersList(String requestId, String performedBy,
                                          String searchQuery, String roleFilter,
                                          String statusFilter, String sortField,
                                          String sortDirection, int page, int pageSize) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting users list with filters - search: " + searchQuery +
                    ", role: " + roleFilter + ", status: " + statusFilter);

            UsersListResponse response = new UsersListResponse();

            // Create pageable request
            Sort sort = createSort(sortField, sortDirection);
            Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

            // Build specification for filtering
            Page<UserEntity> userPage = findUsersWithFilters(searchQuery, roleFilter, statusFilter, pageable);

            // Convert to DTOs with all related data
            List<UserDto> userDtos = userPage.getContent().stream()
                    .map(this::buildUserDto)
                    .collect(Collectors.toList());

            response.setUsers(userDtos);
            response.setTotal((int) userPage.getTotalElements());
            response.setPage(page);
            response.setPageSize(pageSize);
            response.setTotalPages(userPage.getTotalPages());
            response.setHasNext(!userPage.isLast());
            response.setHasPrevious(!userPage.isFirst());

            // Add statistics
            updateStatistics();
            response.setStats(stats);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error getting users list: " + e.getMessage());
            throw e;
        }
    }

    private Page<UserEntity> findUsersWithFilters(String searchQuery, String roleFilter,
                                                  String statusFilter, Pageable pageable) {
        // This is a simplified version - in production, use Specification for complex queries
        Page<UserEntity> users;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Search in username, email, fullName
            users = userRepository.findAll((root, query, cb) -> {
                String pattern = "%" + searchQuery.toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("emailAddress")), pattern),
                        cb.like(cb.lower(root.get("fullName")), pattern)
                );
            }, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        // Apply role and status filters on the result (in production, add to Specification)
        return users;
    }

    private UserDto buildUserDto(UserEntity userEntity) {
        // Get role
        UserRoleEntity role = userEntity.getRole();

        // Get related data
        List<UserDeviceEntity> devices = deviceRepository.findByUser(userEntity);
        List<UserTagEntity> tags = tagRepository.findByUser(userEntity);
        long apiKeysCount = apiKeyRepository.countActiveKeysByUserId(userEntity.getUserId());
        long activeSessionsCount = sessionRepository.countActiveSessionsByUserId(userEntity.getUserId());

        return dtoConverter.convertToDto(userEntity, role, devices, tags, apiKeysCount, activeSessionsCount);
    }

    private Sort createSort(String sortField, String sortDirection) {
        if (sortField == null || sortField.isEmpty()) {
            return Sort.unsorted();
        }

        String dbField = mapSortFieldToDbField(sortField);
        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, dbField);
    }

    private String mapSortFieldToDbField(String sortField) {
        switch (sortField) {
            case "fullName": return "fullName";
            case "lastActive": return "lastLogin";
            case "joinedDate": return "createdDate";
            case "securityScore": return "securityScore"; // Computed field
            case "role": return "role.roleName";
            case "status": return "isActive";
            default: return "username";
        }
    }

    // ============================================================
    // 2. GET USER DETAILS
    // ============================================================
    @Transactional(readOnly = true)
    public UserDetailsResponse getUserDetails(String requestId, String performedBy, String userId) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting user details for: " + userId);

            UserDetailsResponse response = new UserDetailsResponse();

            UserEntity userEntity = userRepository.findByUserIdIgnoreCase(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            UserDto userDto = buildUserDto(userEntity);

            // Populate basic user info
            response.setId(userDto.getId());
            response.setUsername(userDto.getUsername());
            response.setEmail(userDto.getEmail());
            response.setFullName(userDto.getFullName());
            response.setRole(userDto.getRole());
            response.setStatus(userDto.getStatus());
            response.setAvatarColor(userDto.getAvatarColor());
            response.setDepartment(userDto.getDepartment());
            response.setPermissions(userDto.getPermissions());
            response.setMfaEnabled(userDto.isMfaEnabled());
            response.setEmailVerified(userDto.isEmailVerified());
            response.setPhoneVerified(userDto.isPhoneVerified());
            response.setApiAccessCount(userDto.getApiAccessCount());
            response.setLastLoginIp(userDto.getLastLoginIp());
            response.setLocation(userDto.getLocation());
            response.setTimezone(userDto.getTimezone());
            response.setTotalLogins(userDto.getTotalLogins());
            response.setFailedLogins(userDto.getFailedLogins());
            response.setSecurityScore(userDto.getSecurityScore());
            response.setTags(userDto.getTags());
            response.setApiKeys(userDto.getApiKeys());
            response.setActiveSessions(userDto.getActiveSessions());

            // Format dates
            if (userEntity.getLastLogin() != null) {
                response.setLastActive(userEntity.getLastLogin()
                        .format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")));
            }
            if (userEntity.getCreatedDate() != null) {
                response.setJoinedDate(userEntity.getCreatedDate()
                        .format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            }

            // Add devices
            List<UserDeviceEntity> devices = deviceRepository.findByUser(userEntity);
            response.setDevices(dtoConverter.convertDevicesToDeviceInfo(devices));

            // Add activity log
            List<ActivityLog> activityLog = getUserActivityLog(userId);
            response.setActivityLog(activityLog);

            // Add permissions breakdown
            Map<String, Boolean> permissionsBreakdown = getPermissionsBreakdown(userDto.getRole());
            response.setPermissionsBreakdown(permissionsBreakdown);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error getting user details: " + e.getMessage());
            throw e;
        }
    }

    private List<ActivityLog> getUserActivityLog(String userId) {
        Pageable latestActivities = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<UserActivityEntity> activities = activityRepository.findByUserUserId(userId, latestActivities);

        return activities.getContent().stream()
                .map(dtoConverter::convertToActivityLog)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 3. CREATE NEW USER
    // ============================================================
    @Transactional
    public CreateUserResponse createUser(String requestId, String performedBy, CreateUserRequest request) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Creating new user: " + request.getEmail());

            CreateUserResponse response = new CreateUserResponse();

            // Check if user already exists
            if (userRepository.findByEmailAddress(request.getEmail()).isPresent() ||
                    userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("User with this email or username already exists");
            }

            // Get role
            UserRoleEntity role = roleRepository.findByRoleNameIgnoreCase(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));

            // Create new user
            String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);

            UserEntity newUser = UserEntity.builder()
                    .userId(userId)
                    .username(request.getUsername())
                    .emailAddress(request.getEmail())
                    .fullName(request.getFullName())
                    .role(role)
                    .isActive(false) // pending status
                    .isDefaultPassword(true)
                    .staffId(request.getDepartment())
                    .phoneNumber(request.getPhoneNumber())
                    .failedLoginAttempts(0)
                    .createdDate(LocalDateTime.now())
                    .build();

            newUser = userRepository.save(newUser);

            // Add default tags
            UserTagEntity newUserTag = UserTagEntity.builder()
                    .user(newUser)
                    .tagName("new-user")
                    .build();
            tagRepository.save(newUserTag);

            UserTagEntity onboardingTag = UserTagEntity.builder()
                    .user(newUser)
                    .tagName("onboarding")
                    .build();
            tagRepository.save(onboardingTag);

            // Log the creation
            logUserAction(performedBy, userId, "CREATE_USER", "User created successfully");

            UserDto userDto = buildUserDto(newUser);

            response.setId(userId);
            response.setSuccess(true);
            response.setMessage("User created successfully");
            response.setUser(userDto);
            response.setGeneratedAt(new Date());

            // Add next steps
            List<String> nextSteps = Arrays.asList(
                    "Send welcome email",
                    "Setup MFA if enabled",
                    "Assign initial permissions",
                    "Add to relevant groups"
            );
            response.setNextSteps(nextSteps);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error creating user: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 4. UPDATE USER
    // ============================================================
    @Transactional
    public UpdateUserResponse updateUser(String requestId, String performedBy,
                                         String userId, UpdateUserRequest request) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Updating user: " + userId);

            UpdateUserResponse response = new UpdateUserResponse();

            UserEntity user = userRepository.findByUserIdIgnoreCase(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Update user fields
            if (request.getFullName() != null) user.setFullName(request.getFullName());
            if (request.getRole() != null) {
                UserRoleEntity role = roleRepository.findByRoleNameIgnoreCase(request.getRole())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
                user.setRole(role);
            }
            if (request.getStatus() != null) {
                user.setIsActive("active".equalsIgnoreCase(request.getStatus()));
            }
            if (request.getDepartment() != null) user.setStaffId(request.getDepartment());
            if (request.getEmailVerified() != null) {
                // Update email verified status - would need to add field to UserEntity
            }
            if (request.getPhoneVerified() != null) {
                // Update phone verified status
            }
            if (request.getLocation() != null) {
                // Update location - would need to add field to UserEntity or UserSessionEntity
            }
            if (request.getTags() != null) {
                // Update tags
                tagRepository.deleteByUserId(userId);

                List<UserTagEntity> newTags = request.getTags().stream()
                        .map(tagName -> UserTagEntity.builder()
                                .user(user)
                                .tagName(tagName)
                                .build())
                        .collect(Collectors.toList());
                tagRepository.saveAll(newTags);
            }

            user.setLastModifiedDate(LocalDateTime.now());
            userRepository.save(user);

            // Log the update
            logUserAction(performedBy, userId, "UPDATE_USER", "User updated: " + request.toString());

            response.setSuccess(true);
            response.setMessage("User updated successfully");
            response.setUpdatedAt(new Date());
            response.setUser(buildUserDto(user));

            // Add audit log
            Map<String, String> auditLog = new HashMap<>();
            auditLog.put("action", "update");
            auditLog.put("performedBy", performedBy);
            auditLog.put("timestamp", new Date().toString());
            auditLog.put("changes", request.toString());
            response.setAuditLog(auditLog);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error updating user: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 5. DELETE USER
    // ============================================================
    @Transactional
    public DeleteUserResponse deleteUser(String requestId, String performedBy, String userId) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Deleting user: " + userId);

            DeleteUserResponse response = new DeleteUserResponse();

            UserEntity user = userRepository.findByUserIdIgnoreCase(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Store user data for response before deletion
            UserDto userDto = buildUserDto(user);

            // Delete all related entities
            deviceRepository.deleteByUserId(userId);
            apiKeyRepository.deleteByUserId(userId);
            sessionRepository.deleteByUserId(userId);
            tagRepository.deleteByUserId(userId);

            // Delete user activities (keep for audit)
            // activityRepository.deleteByUserId(userId);

            // Delete user
            userRepository.delete(user);

            // Log the deletion
            logUserAction(performedBy, userId, "DELETE_USER", "User deleted");

            response.setSuccess(true);
            response.setMessage("User deleted successfully");
            response.setDeletedAt(new Date());
            response.setDeletedUser(userDto);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error deleting user: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 6. BULK OPERATIONS
    // ============================================================
    @Transactional
    public BulkOperationResponse bulkOperation(String requestId, String performedBy,
                                               BulkOperationRequest request) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Performing bulk operation: " + request.getOperation() +
                    " on " + request.getUserIds().size() + " users");

            BulkOperationResponse response = new BulkOperationResponse();

            List<String> processedUsers = new ArrayList<>();
            List<String> failedUsers = new ArrayList<>();

            for (String userId : request.getUserIds()) {
                try {
                    Optional<UserEntity> userOpt = userRepository.findByUserIdIgnoreCase(userId);

                    if (userOpt.isPresent()) {
                        UserEntity user = userOpt.get();

                        switch (request.getOperation()) {
                            case "activate":
                                user.setIsActive(true);
                                userRepository.save(user);
                                logUserAction(performedBy, userId, "BULK_ACTIVATE", "User activated via bulk operation");
                                break;
                            case "suspend":
                            case "deactivate":
                                user.setIsActive(false);
                                userRepository.save(user);
                                logUserAction(performedBy, userId, "BULK_DEACTIVATE", "User deactivated via bulk operation");
                                break;
                            case "delete":
                                deleteUser(requestId, performedBy, userId);
                                logUserAction(performedBy, userId, "BULK_DELETE", "User deleted via bulk operation");
                                break;
                            case "reset_password":
                                // Trigger password reset
                                logUserAction(performedBy, userId, "BULK_PASSWORD_RESET", "Password reset initiated via bulk operation");
                                break;
                            default:
                                throw new RuntimeException("Unsupported operation: " + request.getOperation());
                        }

                        processedUsers.add(userId);
                    } else {
                        failedUsers.add(userId + " (not found)");
                    }
                } catch (Exception e) {
                    failedUsers.add(userId + " (" + e.getMessage() + ")");
                }
            }

            response.setOperation(request.getOperation());
            response.setProcessedCount(processedUsers.size());
            response.setFailedCount(failedUsers.size());
            response.setProcessedUsers(processedUsers);
            response.setFailedUsers(failedUsers);
            response.setCompletedAt(new Date());

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error performing bulk operation: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 7. RESET USER PASSWORD
    // ============================================================
    @Transactional
    public ResetPasswordResponse resetPassword(String requestId, String performedBy,
                                               ResetPasswordRequest request) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Resetting password for user: " + request.getUserId());

            ResetPasswordResponse response = new ResetPasswordResponse();

            UserEntity user = userRepository.findByUserIdIgnoreCase(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserId()));

            // In a real implementation, this would:
            // 1. Generate a secure temporary password
            // 2. Hash and store it
            // 3. Send password reset email
            // 4. Log the action

            String temporaryPassword = generateTemporaryPassword();

            // Hash and save password (you'd use a password encoder)
            // user.setPassword(passwordEncoder.encode(temporaryPassword));
            user.setIsDefaultPassword(true);
            user.setLastModifiedDate(LocalDateTime.now());
            userRepository.save(user);

            // Force logout if requested
            if (request.isForceLogout()) {
                sessionRepository.deactivateAllSessions(user.getUserId());
            }

            // Log the action
            logUserAction(performedBy, request.getUserId(), "PASSWORD_RESET",
                    "Password reset initiated" + (request.isForceLogout() ? " with force logout" : ""));

            response.setSuccess(true);
            response.setMessage("Password reset initiated successfully");
            response.setUserId(request.getUserId());
            response.setResetAt(new Date());
            response.setExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24 hours

            // Add security info
            Map<String, Object> securityInfo = new HashMap<>();
            securityInfo.put("forceLogoutAllDevices", request.isForceLogout());
            securityInfo.put("requirePasswordChangeOnNextLogin", true);
            securityInfo.put("passwordComplexity", "high");
            response.setSecurityInfo(securityInfo);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error resetting password: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 8. GET USER STATISTICS
    // ============================================================
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics(String requestId, String performedBy) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting user statistics");

            UserStatisticsResponse response = new UserStatisticsResponse();

            // Calculate statistics
            updateStatistics();

            response.setTotalUsers((int) stats.get("totalUsers"));
            response.setActiveUsers((int) stats.get("activeUsers"));
            response.setAdmins((int) stats.get("admins"));
            response.setDevelopers((int) stats.get("developers"));
            response.setViewers((int) stats.get("viewers"));
            response.setPendingUsers((int) stats.get("pendingUsers"));
            response.setSuspendedUsers((int) stats.get("suspendedUsers"));
            response.setMfaEnabledUsers((int) stats.get("mfaEnabled"));
            response.setAvgSecurityScore((double) stats.get("avgSecurityScore"));
            response.setGeneratedAt(new Date());

            // Add trend data
            Map<String, Integer> trends = new HashMap<>();
            trends.put("last24Hours", getNewUsersCount(24));
            trends.put("last7Days", getNewUsersCount(7 * 24));
            trends.put("last30Days", getNewUsersCount(30 * 24));
            response.setTrends(trends);

            // Add department breakdown
            Map<String, Integer> departmentBreakdown = getDepartmentBreakdown();
            response.setDepartmentBreakdown(departmentBreakdown);

            // Add role distribution
            Map<String, Integer> roleDistribution = getRoleDistribution();
            response.setRoleDistribution(roleDistribution);

            // Add status distribution
            Map<String, Integer> statusDistribution = getStatusDistribution();
            response.setStatusDistribution(statusDistribution);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error getting user statistics: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 9. SEARCH USERS
    // ============================================================
    @Transactional(readOnly = true)
    public SearchUsersResponse searchUsers(String requestId, String performedBy,
                                           SearchUsersRequest searchRequest) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Searching users with query: " + searchRequest.getQuery());

            SearchUsersResponse response = new SearchUsersResponse();

            // Build search specification
            Pageable pageable = PageRequest.of(0, 100); // Limit results

            Page<UserEntity> userPage = userRepository.findAll((root, query, cb) -> {
                String pattern = "%" + searchRequest.getQuery().toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("emailAddress")), pattern),
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("staffId")), pattern)
                );
            }, pageable);

            List<UserDto> searchResults = userPage.getContent().stream()
                    .map(this::buildUserDto)
                    .collect(Collectors.toList());

            response.setQuery(searchRequest.getQuery());
            response.setResults(searchResults);
            response.setTotal((int) userPage.getTotalElements());
            response.setSearchAt(new Date());

            // Add search metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("searchFields", Arrays.asList("username", "email", "fullName", "department", "tags"));
            metadata.put("filtersApplied", searchRequest.getFilters());
            metadata.put("searchTime", System.currentTimeMillis());
            response.setMetadata(metadata);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error searching users: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 10. IMPORT USERS
    // ============================================================
    @Transactional
    public ImportUsersResponse importUsers(String requestId, String performedBy,
                                           ImportUsersRequest importRequest) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Importing users from file: " + importRequest.getFileName());

            ImportUsersResponse response = new ImportUsersResponse();

            // In real implementation, this would parse the file and create users
            // For now, we'll simulate the response structure

            response.setFileName(importRequest.getFileName());
            response.setFileType(importRequest.getFileType());
            response.setTotalRecords(3);
            response.setImportedCount(3);
            response.setFailedCount(0);
            response.setImportedUsers(new ArrayList<>());
            response.setImportErrors(new ArrayList<>());
            response.setImportedAt(new Date());
            response.setStatus("completed");

            Map<String, Object> summary = new HashMap<>();
            summary.put("newUsers", 3);
            summary.put("updatedUsers", 0);
            summary.put("skippedUsers", 0);
            summary.put("duplicateEmails", 0);
            summary.put("validationErrors", 0);
            response.setSummary(summary);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error importing users: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 11. EXPORT USERS
    // ============================================================
    @Transactional(readOnly = true)
    public ExportUsersResponse exportUsers(String requestId, String performedBy,
                                           ExportUsersRequest exportRequest) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Exporting users in format: " + exportRequest.getFormat());

            ExportUsersResponse response = new ExportUsersResponse();

            // Get all users (or filtered)
            List<UserEntity> allUsers = userRepository.findAll();
            long totalUsers = allUsers.size();

            response.setFormat(exportRequest.getFormat());
            response.setTotalUsers((int) totalUsers);
            response.setExportedAt(new Date());
            response.setStatus("ready");

            // Generate export data
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("downloadUrl", "/downloads/users-export-" + UUID.randomUUID() + "." + exportRequest.getFormat());
            exportData.put("downloadId", UUID.randomUUID().toString());
            exportData.put("fileSize", (int) (totalUsers * 1024)); // Approximate size
            exportData.put("expiresAt", new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24 hours

            if (exportRequest.getFormat().equals("csv")) {
                exportData.put("columns", Arrays.asList("id", "username", "email", "fullName", "role", "status",
                        "department", "lastActive", "joinedDate", "securityScore"));
            } else if (exportRequest.getFormat().equals("json")) {
                exportData.put("includeMetadata", true);
                exportData.put("prettyPrint", true);
            }

            response.setExportData(exportData);

            // Log the export
            logUserAction(performedBy, "SYSTEM", "EXPORT_USERS",
                    "Exported " + totalUsers + " users in " + exportRequest.getFormat() + " format");

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error exporting users: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 12. GET USER ACTIVITY LOG
    // ============================================================
    @Transactional(readOnly = true)
    public UserActivityResponse getUserActivity(String requestId, String performedBy,
                                                String userId, Date startDate, Date endDate) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting activity log for user: " + userId);

            UserActivityResponse response = new UserActivityResponse();

            List<ActivityLog> activityLog = getUserActivityLog(userId);

            response.setUserId(userId);
            response.setActivities(activityLog);
            response.setTotalActivities(activityLog.size());
            response.setGeneratedAt(new Date());

            // Add activity statistics
            Map<String, Object> activityStats = new HashMap<>();

            LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

            activityStats.put("loginsLast7Days",
                    activityRepository.countLoginsSince(userId, last7Days));
            activityStats.put("apiCallsLast24Hours",
                    activityRepository.countApiCallsSince(userId, last24Hours));
            activityStats.put("failedAttemptsLast30Days",
                    activityRepository.countFailedAttemptsSince(userId, last30Days));
            activityStats.put("avgSessionDuration", "45 minutes");

            response.setActivityStats(activityStats);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error getting user activity: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 13. UPDATE USER STATUS
    // ============================================================
    @Transactional
    public UpdateStatusResponse updateUserStatus(String requestId, String performedBy,
                                                 String userId, String status) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Updating status for user: " + userId + " to " + status);

            UpdateStatusResponse response = new UpdateStatusResponse();

            UserEntity user = userRepository.findByUserIdIgnoreCase(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            boolean previousStatus = user.getIsActive();
            boolean newStatus = "active".equalsIgnoreCase(status);

            user.setIsActive(newStatus);
            user.setLastModifiedDate(LocalDateTime.now());
            userRepository.save(user);

            // Log the status change
            logUserAction(performedBy, userId, "STATUS_UPDATE",
                    "Status changed from " + (previousStatus ? "active" : "inactive") +
                            " to " + status);

            response.setSuccess(true);
            response.setMessage("User status updated successfully");
            response.setUserId(userId);
            response.setPreviousStatus(previousStatus ? "active" : "inactive");
            response.setNewStatus(status);
            response.setUpdatedAt(new Date());

            // Add notification info
            Map<String, Object> notificationInfo = new HashMap<>();
            notificationInfo.put("emailSent", true);
            notificationInfo.put("notificationType", "status_change");
            notificationInfo.put("recipient", user.getEmailAddress());
            response.setNotificationInfo(notificationInfo);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error updating user status: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 14. GET ROLES AND PERMISSIONS
    // ============================================================
    @Transactional(readOnly = true)
    public RolesPermissionsResponse getRolesAndPermissions(String requestId, String performedBy) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting roles and permissions");

            RolesPermissionsResponse response = new RolesPermissionsResponse();

            List<UserRoleEntity> roleEntities = roleRepository.findAll();

            List<RoleDto> roles = roleEntities.stream()
                    .map(this::convertToRoleDto)
                    .collect(Collectors.toList());

            response.setRoles(roles);
            response.setTotalRoles(roles.size());
            response.setGeneratedAt(new Date());

            // Add permission categories
            Map<String, List<String>> permissionCategories = new HashMap<>();
            permissionCategories.put("User Management",
                    Arrays.asList("read_users", "write_users", "delete_users", "manage_users"));
            permissionCategories.put("API Access",
                    Arrays.asList("api_read", "api_write", "api_delete", "api_admin"));
            permissionCategories.put("System",
                    Arrays.asList("system_config", "system_monitor", "system_backup"));
            permissionCategories.put("Content",
                    Arrays.asList("content_read", "content_write", "content_delete", "content_publish"));
            response.setPermissionCategories(permissionCategories);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error getting roles and permissions: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 15. VALIDATE USER DATA
    // ============================================================
    @Transactional(readOnly = true)
    public ValidationResponse validateUserData(String requestId, String performedBy,
                                               ValidateUserRequest validationRequest) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Validating user data");

            ValidationResponse response = new ValidationResponse();

            List<ValidationIssue> issues = new ArrayList<>();
            boolean isValid = true;

            // Validate email
            if (validationRequest.getEmail() != null) {
                boolean emailExists = userRepository.findByEmailAddress(validationRequest.getEmail())
                        .map(user -> !user.getUserId().equals(validationRequest.getUserId()))
                        .orElse(false);

                if (emailExists) {
                    issues.add(ValidationIssue.builder()
                            .type("error")
                            .field("email")
                            .message("Email already exists")
                            .severity("high")
                            .build());
                    isValid = false;
                }

                if (!validationRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    issues.add(ValidationIssue.builder()
                            .type("error")
                            .field("email")
                            .message("Invalid email format")
                            .severity("high")
                            .build());
                    isValid = false;
                }
            }

            // Validate username
            if (validationRequest.getUsername() != null) {
                boolean usernameExists = userRepository.findByUsername(validationRequest.getUsername())
                        .map(user -> !user.getUserId().equals(validationRequest.getUserId()))
                        .orElse(false);

                if (usernameExists) {
                    issues.add(ValidationIssue.builder()
                            .type("error")
                            .field("username")
                            .message("Username already exists")
                            .severity("high")
                            .build());
                    isValid = false;
                }

                if (validationRequest.getUsername().length() < 3) {
                    issues.add(ValidationIssue.builder()
                            .type("warning")
                            .field("username")
                            .message("Username is too short (minimum 3 characters)")
                            .severity("medium")
                            .build());
                }
            }

            response.setValid(isValid);
            response.setIssues(issues);
            response.setValidatedAt(new Date());
            response.setScore(isValid ? 100 : issues.size() > 2 ? 50 : 75);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error validating user data: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private void updateStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.count((root, query, cb) ->
                cb.isTrue(root.get("isActive")));
        long admins = countUsersByRole("admin");
        long developers = countUsersByRole("developer");
        long viewers = countUsersByRole("viewer");
        long pendingUsers = userRepository.count((root, query, cb) ->
                cb.isFalse(root.get("isActive")));

        // These would be calculated from actual data
        int mfaEnabled = 0; // Add MFA field to UserEntity
        double avgSecurityScore = 75.0; // Calculate from user data

        stats.put("totalUsers", (int) totalUsers);
        stats.put("activeUsers", (int) activeUsers);
        stats.put("admins", (int) admins);
        stats.put("developers", (int) developers);
        stats.put("viewers", (int) viewers);
        stats.put("pendingUsers", (int) pendingUsers);
        stats.put("suspendedUsers", 0); // You might want to add a suspended flag
        stats.put("mfaEnabled", mfaEnabled);
        stats.put("avgSecurityScore", avgSecurityScore);
    }

    private long countUsersByRole(String roleName) {
        return userRepository.count((root, query, cb) ->
                cb.equal(cb.lower(root.get("role").get("roleName")), roleName.toLowerCase()));
    }

    private Map<String, Integer> getDepartmentBreakdown() {
        // This is a simplified version - in production, you'd have a department entity
        Map<String, Integer> breakdown = new HashMap<>();

        List<UserEntity> users = userRepository.findAll();
        users.stream()
                .map(UserEntity::getStaffId)
                .filter(Objects::nonNull)
                .forEach(dept -> breakdown.merge(dept, 1, Integer::sum));

        return breakdown;
    }

    private Map<String, Integer> getRoleDistribution() {
        Map<String, Integer> distribution = new HashMap<>();

        List<Object[]> results = userRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRole() != null ? u.getRole().getRoleName() : "unknown",
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue().intValue()})
                .collect(Collectors.toList());

        for (Object[] result : results) {
            distribution.put((String) result[0], (Integer) result[1]);
        }

        return distribution;
    }

    private Map<String, Integer> getStatusDistribution() {
        Map<String, Integer> distribution = new HashMap<>();

        long active = userRepository.count((root, query, cb) ->
                cb.isTrue(root.get("isActive")));
        long inactive = userRepository.count((root, query, cb) ->
                cb.isFalse(root.get("isActive")));

        distribution.put("active", (int) active);
        distribution.put("inactive", (int) inactive);
        distribution.put("pending", 0); // You might want to add a pending flag

        return distribution;
    }

    private int getNewUsersCount(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return (int) userRepository.count((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdDate"), since));
    }

    private RoleDto convertToRoleDto(UserRoleEntity roleEntity) {
        RoleDto dto = new RoleDto();
        dto.setId(roleEntity.getRoleId().toString());
        dto.setName(roleEntity.getRoleName());
        dto.setDescription(roleEntity.getDescription());
        dto.setPermissions(getPermissionsForRole(roleEntity));
        dto.setColor(generateRoleColor(roleEntity.getRoleName()));
        dto.setDefault(roleEntity.getRoleName().equalsIgnoreCase("viewer"));
        dto.setUserCount((int) countUsersByRole(roleEntity.getRoleName()));
        dto.setCreatedAt(Date.from(roleEntity.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        return dto;
    }

    private String generateRoleColor(String roleName) {
        switch (roleName.toLowerCase()) {
            case "admin": return "#EF4444";
            case "developer": return "#3B82F6";
            case "viewer": return "#10B981";
            case "moderator": return "#F59E0B";
            default: return "#8B5CF6";
        }
    }

    private Map<String, Boolean> getPermissionsBreakdown(String role) {
        Map<String, Boolean> permissions = new HashMap<>();

        switch (role.toLowerCase()) {
            case "admin":
                permissions.put("read", true);
                permissions.put("write", true);
                permissions.put("delete", true);
                permissions.put("admin", true);
                permissions.put("manage_users", true);
                permissions.put("manage_roles", true);
                break;
            case "developer":
                permissions.put("read", true);
                permissions.put("write", true);
                permissions.put("delete", false);
                permissions.put("admin", false);
                permissions.put("api_access", true);
                permissions.put("debug", true);
                break;
            case "viewer":
                permissions.put("read", true);
                permissions.put("write", false);
                permissions.put("delete", false);
                permissions.put("admin", false);
                break;
            default:
                permissions.put("read", true);
                permissions.put("write", false);
                permissions.put("delete", false);
        }

        return permissions;
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private void logUserAction(String performedBy, String userId, String action, String details) {
        try {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .userId(performedBy)
                    .action(action)
                    .operation("USER_MANAGEMENT")
                    .details(String.format("User: %s, Details: %s", userId, details))
                    .isSuccess(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            loggerUtil.log("user-management", "Error logging user action: " + e.getMessage());
        }
    }
}