package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.userManagement.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    @Autowired
    private LoggerUtil loggerUtil;

    // Mock data storage (in a real app, this would be a database)
    private List<UserDto> users = initializeMockUsers();

    // Statistics storage
    private Map<String, Object> stats = new HashMap<>();

    // ============================================================
    // 1. GET USERS LIST WITH FILTERS
    // ============================================================
    public UsersListResponse getUsersList(String requestId, String performedBy,
                                          String searchQuery, String roleFilter,
                                          String statusFilter, String sortField,
                                          String sortDirection, int page, int pageSize) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting users list with filters - search: " + searchQuery +
                    ", role: " + roleFilter + ", status: " + statusFilter);

            UsersListResponse response = new UsersListResponse();

            // Apply filters
            List<UserDto> filteredUsers = users.stream()
                    .filter(user -> matchesSearch(user, searchQuery))
                    .filter(user -> matchesRole(user, roleFilter))
                    .filter(user -> matchesStatus(user, statusFilter))
                    .collect(Collectors.toList());

            // Apply sorting
            filteredUsers = sortUsers(filteredUsers, sortField, sortDirection);

            // Calculate pagination
            int totalUsers = filteredUsers.size();
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalUsers);

            List<UserDto> paginatedUsers = filteredUsers.subList(startIndex, endIndex);

            response.setUsers(paginatedUsers);
            response.setTotal(totalUsers);
            response.setPage(page);
            response.setPageSize(pageSize);
            response.setTotalPages(totalPages);
            response.setHasNext(page < totalPages);
            response.setHasPrevious(page > 1);

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

    // ============================================================
    // 2. GET USER DETAILS
    // ============================================================
    public UserDetailsResponse getUserDetails(String requestId, String performedBy, String userId) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting user details for: " + userId);

            UserDetailsResponse response = new UserDetailsResponse();

            Optional<UserDto> userOpt = users.stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst();

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }

            UserDto user = userOpt.get();

            // Populate basic user info
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole());
            response.setStatus(user.getStatus());
            response.setAvatarColor(user.getAvatarColor());
            response.setDepartment(user.getDepartment());
            response.setPermissions(user.getPermissions());
            response.setMfaEnabled(user.isMfaEnabled());
            response.setEmailVerified(user.isEmailVerified());
            response.setPhoneVerified(user.isPhoneVerified());
            response.setApiAccessCount(user.getApiAccessCount());
            response.setLastLoginIp(user.getLastLoginIp());
            response.setLocation(user.getLocation());
            response.setTimezone(user.getTimezone());
            response.setTotalLogins(user.getTotalLogins());
            response.setFailedLogins(user.getFailedLogins());
            response.setSecurityScore(user.getSecurityScore());
            response.setTags(user.getTags());
            response.setApiKeys(user.getApiKeys());
            response.setActiveSessions(user.getActiveSessions());

            // Format dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            response.setLastActive(LocalDateTime.parse(user.getLastActive()).format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")));
            response.setJoinedDate(LocalDateTime.parse(user.getJoinedDate()).format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

            // Add devices
            List<DeviceInfo> devices = new ArrayList<>();
            if (user.getDevices() != null && !user.getDevices().isEmpty()) {
                for (Map<String, Object> device : user.getDevices()) {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setType((String) device.get("type"));
                    deviceInfo.setLastUsed((String) device.get("lastUsed"));
                    devices.add(deviceInfo);
                }
            }
            response.setDevices(devices);

            // Add activity log
            List<ActivityLog> activityLog = getMockActivityLog(userId);
            response.setActivityLog(activityLog);

            // Add permissions breakdown
            Map<String, Boolean> permissionsBreakdown = getPermissionsBreakdown(user.getRole());
            response.setPermissionsBreakdown(permissionsBreakdown);

            return response;

        } catch (Exception e) {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Error getting user details: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // 3. CREATE NEW USER
    // ============================================================
    public CreateUserResponse createUser(String requestId, String performedBy, CreateUserRequest request) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Creating new user: " + request.getEmail());

            CreateUserResponse response = new CreateUserResponse();

            // Check if user already exists
            boolean userExists = users.stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(request.getEmail()) ||
                            u.getUsername().equalsIgnoreCase(request.getUsername()));

            if (userExists) {
                throw new RuntimeException("User with this email or username already exists");
            }

            // Create new user
            String userId = "user-" + (users.size() + 1);
            UserDto newUser = new UserDto();
            newUser.setId(userId);
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            newUser.setFullName(request.getFullName());
            newUser.setRole(request.getRole());
            newUser.setStatus("pending"); // Default status for new users
            newUser.setAvatarColor(generateRandomColor());
            newUser.setDepartment(request.getDepartment());
            newUser.setPermissions(getDefaultPermissions(request.getRole()));
            newUser.setMfaEnabled(request.isMfaEnabled());
            newUser.setEmailVerified(false); // Email verification required
            newUser.setPhoneVerified(false);
            newUser.setApiAccessCount(0);
            newUser.setLastLoginIp("N/A");
            newUser.setLocation(request.getLocation());
            newUser.setTimezone(getTimezoneByLocation(request.getLocation()));
            newUser.setTotalLogins(0);
            newUser.setFailedLogins(0);
            newUser.setSecurityScore(calculateInitialSecurityScore(request));
            newUser.setTags(Arrays.asList("new-user", "onboarding"));
            newUser.setDevices(new ArrayList<>());
            newUser.setApiKeys(0);
            newUser.setActiveSessions(0);
            newUser.setLastActive(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            newUser.setJoinedDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            users.add(newUser);

            // Update statistics
            updateStatistics();

            response.setId(userId);
            response.setSuccess(true);
            response.setMessage("User created successfully");
            response.setUser(newUser);
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
    public UpdateUserResponse updateUser(String requestId, String performedBy,
                                         String userId, UpdateUserRequest request) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Updating user: " + userId);

            UpdateUserResponse response = new UpdateUserResponse();

            Optional<UserDto> userOpt = users.stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst();

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }

            UserDto user = userOpt.get();

            // Update user fields
            if (request.getFullName() != null) user.setFullName(request.getFullName());
            if (request.getRole() != null) {
                user.setRole(request.getRole());
                user.setPermissions(getDefaultPermissions(request.getRole()));
            }
            if (request.getStatus() != null) user.setStatus(request.getStatus());
            if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
            if (request.getMfaEnabled() != null) user.setMfaEnabled(request.getMfaEnabled());
            if (request.getEmailVerified() != null) user.setEmailVerified(request.getEmailVerified());
            if (request.getPhoneVerified() != null) user.setPhoneVerified(request.getPhoneVerified());
            if (request.getLocation() != null) user.setLocation(request.getLocation());
            if (request.getTags() != null) user.setTags(request.getTags());

            // Update last modified timestamp
            user.setLastActive(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            response.setSuccess(true);
            response.setMessage("User updated successfully");
            response.setUpdatedAt(new Date());
            response.setUser(user);

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
    public DeleteUserResponse deleteUser(String requestId, String performedBy, String userId) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Deleting user: " + userId);

            DeleteUserResponse response = new DeleteUserResponse();

            Optional<UserDto> userOpt = users.stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst();

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }

            UserDto user = userOpt.get();

            // Remove user
            users.removeIf(u -> u.getId().equals(userId));

            // Update statistics
            updateStatistics();

            response.setSuccess(true);
            response.setMessage("User deleted successfully");
            response.setDeletedAt(new Date());
            response.setDeletedUser(user);

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
                    Optional<UserDto> userOpt = users.stream()
                            .filter(u -> u.getId().equals(userId))
                            .findFirst();

                    if (userOpt.isPresent()) {
                        UserDto user = userOpt.get();

                        switch (request.getOperation()) {
                            case "activate":
                                user.setStatus("active");
                                break;
                            case "suspend":
                                user.setStatus("suspended");
                                break;
                            case "deactivate":
                                user.setStatus("inactive");
                                break;
                            case "delete":
                                users.remove(user);
                                break;
                            case "reset_password":
                                // In real implementation, this would trigger a password reset email
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

            // Update statistics if needed
            if (request.getOperation().equals("delete")) {
                updateStatistics();
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
    public ResetPasswordResponse resetPassword(String requestId, String performedBy,
                                               ResetPasswordRequest request) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Resetting password for user: " + request.getUserId());

            ResetPasswordResponse response = new ResetPasswordResponse();

            Optional<UserDto> userOpt = users.stream()
                    .filter(u -> u.getId().equals(request.getUserId()))
                    .findFirst();

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + request.getUserId());
            }

            // In a real implementation, this would:
            // 1. Generate a secure temporary password
            // 2. Hash and store it
            // 3. Send password reset email
            // 4. Log the action

            String temporaryPassword = generateTemporaryPassword();

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
            trends.put("last24Hours", 5); // New users in last 24 hours
            trends.put("last7Days", 12); // New users in last 7 days
            trends.put("last30Days", 45); // New users in last 30 days
            response.setTrends(trends);

            // Add department breakdown
            Map<String, Integer> departmentBreakdown = users.stream()
                    .collect(Collectors.groupingBy(UserDto::getDepartment, Collectors.summingInt(u -> 1)));
            response.setDepartmentBreakdown(departmentBreakdown);

            // Add role distribution
            Map<String, Integer> roleDistribution = users.stream()
                    .collect(Collectors.groupingBy(UserDto::getRole, Collectors.summingInt(u -> 1)));
            response.setRoleDistribution(roleDistribution);

            // Add status distribution
            Map<String, Integer> statusDistribution = users.stream()
                    .collect(Collectors.groupingBy(UserDto::getStatus, Collectors.summingInt(u -> 1)));
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
    public SearchUsersResponse searchUsers(String requestId, String performedBy,
                                           SearchUsersRequest searchRequest) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Searching users with query: " + searchRequest.getQuery());

            SearchUsersResponse response = new SearchUsersResponse();

            List<UserDto> searchResults = users.stream()
                    .filter(user -> matchesSearchCriteria(user, searchRequest))
                    .collect(Collectors.toList());

            response.setQuery(searchRequest.getQuery());
            response.setResults(searchResults);
            response.setTotal(searchResults.size());
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
    public ImportUsersResponse importUsers(String requestId, String performedBy,
                                           ImportUsersRequest importRequest) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Importing users from file: " + importRequest.getFileName());

            ImportUsersResponse response = new ImportUsersResponse();

            // Simulate import process
            List<Map<String, Object>> importedUsers = new ArrayList<>();
            List<Map<String, String>> importErrors = new ArrayList<>();

            // In real implementation, this would parse the file
            // For now, simulate importing 3 users
            for (int i = 0; i < 3; i++) {
                Map<String, Object> importedUser = new HashMap<>();
                importedUser.put("email", "imported.user" + i + "@example.com");
                importedUser.put("username", "imported.user" + i);
                importedUser.put("fullName", "Imported User " + i);
                importedUser.put("status", "pending");
                importedUsers.add(importedUser);
            }

            response.setFileName(importRequest.getFileName());
            response.setFileType(importRequest.getFileType());
            response.setTotalRecords(importedUsers.size() + importErrors.size());
            response.setImportedCount(importedUsers.size());
            response.setFailedCount(importErrors.size());
            response.setImportedUsers(importedUsers);
            response.setImportErrors(importErrors);
            response.setImportedAt(new Date());
            response.setStatus("completed");

            // Add import summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("newUsers", importedUsers.size());
            summary.put("updatedUsers", 0);
            summary.put("skippedUsers", importErrors.size());
            summary.put("duplicateEmails", 0);
            summary.put("validationErrors", importErrors.size());
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
    public ExportUsersResponse exportUsers(String requestId, String performedBy,
                                           ExportUsersRequest exportRequest) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Exporting users in format: " + exportRequest.getFormat());

            ExportUsersResponse response = new ExportUsersResponse();

            // Filter users based on export criteria
            List<UserDto> usersToExport = users;
            if (exportRequest.getFilters() != null && !exportRequest.getFilters().isEmpty()) {
                usersToExport = users.stream()
                        .filter(user -> matchesExportFilters(user, exportRequest.getFilters()))
                        .collect(Collectors.toList());
            }

            response.setFormat(exportRequest.getFormat());
            response.setTotalUsers(usersToExport.size());
            response.setExportedAt(new Date());
            response.setStatus("ready");

            // Generate export data
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("downloadUrl", "/downloads/users-export-" + UUID.randomUUID() + "." + exportRequest.getFormat());
            exportData.put("downloadId", UUID.randomUUID().toString());
            exportData.put("fileSize", usersToExport.size() * 1024); // Approximate size
            exportData.put("expiresAt", new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24 hours

            if (exportRequest.getFormat().equals("csv")) {
                exportData.put("columns", Arrays.asList("id", "username", "email", "fullName", "role", "status",
                        "department", "lastActive", "joinedDate", "securityScore"));
            } else if (exportRequest.getFormat().equals("json")) {
                exportData.put("includeMetadata", true);
                exportData.put("prettyPrint", true);
            }

            response.setExportData(exportData);

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
    public UserActivityResponse getUserActivity(String requestId, String performedBy,
                                                String userId, Date startDate, Date endDate) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting activity log for user: " + userId);

            UserActivityResponse response = new UserActivityResponse();

            List<ActivityLog> activityLog = getMockActivityLog(userId);

            response.setUserId(userId);
            response.setActivities(activityLog);
            response.setTotalActivities(activityLog.size());
            response.setGeneratedAt(new Date());

            // Add activity statistics
            Map<String, Object> activityStats = new HashMap<>();
            activityStats.put("loginsLast7Days", 12);
            activityStats.put("apiCallsLast24Hours", 45);
            activityStats.put("failedAttemptsLast30Days", 2);
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
    public UpdateStatusResponse updateUserStatus(String requestId, String performedBy,
                                                 String userId, String status) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Updating status for user: " + userId + " to " + status);

            UpdateStatusResponse response = new UpdateStatusResponse();

            Optional<UserDto> userOpt = users.stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst();

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }

            UserDto user = userOpt.get();
            String previousStatus = user.getStatus();
            user.setStatus(status);
            user.setLastActive(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            response.setSuccess(true);
            response.setMessage("User status updated successfully");
            response.setUserId(userId);
            response.setPreviousStatus(previousStatus);
            response.setNewStatus(status);
            response.setUpdatedAt(new Date());

            // Add notification info
            Map<String, Object> notificationInfo = new HashMap<>();
            notificationInfo.put("emailSent", true);
            notificationInfo.put("notificationType", "status_change");
            notificationInfo.put("recipient", user.getEmail());
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
    public RolesPermissionsResponse getRolesAndPermissions(String requestId, String performedBy) {
        try {
            loggerUtil.log("user-management", "Request ID: " + requestId +
                    ", Getting roles and permissions");

            RolesPermissionsResponse response = new RolesPermissionsResponse();

            List<RoleDto> roles = Arrays.asList(
                    createRoleDto("admin", "Administrator",
                            Arrays.asList("read", "write", "delete", "admin", "manage_users", "manage_roles", "audit_logs"),
                            "#EF4444", true, 3),
                    createRoleDto("developer", "Developer",
                            Arrays.asList("read", "write", "api_access", "debug", "test"),
                            "#3B82F6", true, 5),
                    createRoleDto("viewer", "Viewer",
                            Arrays.asList("read"),
                            "#10B981", false, 3),
                    createRoleDto("moderator", "Moderator",
                            Arrays.asList("read", "write", "moderate", "review"),
                            "#F59E0B", false, 2)
            );

            response.setRoles(roles);
            response.setTotalRoles(roles.size());
            response.setGeneratedAt(new Date());

            // Add permission categories
            Map<String, List<String>> permissionCategories = new HashMap<>();
            permissionCategories.put("User Management", Arrays.asList("read_users", "write_users", "delete_users", "manage_users"));
            permissionCategories.put("API Access", Arrays.asList("api_read", "api_write", "api_delete", "api_admin"));
            permissionCategories.put("System", Arrays.asList("system_config", "system_monitor", "system_backup"));
            permissionCategories.put("Content", Arrays.asList("content_read", "content_write", "content_delete", "content_publish"));
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
                boolean emailExists = users.stream()
                        .anyMatch(u -> u.getEmail().equalsIgnoreCase(validationRequest.getEmail()) &&
                                !u.getId().equals(validationRequest.getUserId()));

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
                boolean usernameExists = users.stream()
                        .anyMatch(u -> u.getUsername().equalsIgnoreCase(validationRequest.getUsername()) &&
                                !u.getId().equals(validationRequest.getUserId()));

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

    private List<UserDto> initializeMockUsers() {
        List<UserDto> mockUsers = new ArrayList<>();

        // Create mock users matching the UI data
        mockUsers.add(createUserDto("user-1", "john.doe", "john.doe@example.com", "John Doe",
                "admin", "active", "#3B82F6", "Engineering",
                Arrays.asList("read", "write", "delete", "admin"), true, true, true,
                24, "192.168.1.100", "San Francisco, CA", "PST", 156, 3, 95,
                Arrays.asList("core-team", "backend", "devops"), 3, 2));

        mockUsers.add(createUserDto("user-2", "jane.smith", "jane.smith@example.com", "Jane Smith",
                "developer", "active", "#10B981", "Frontend",
                Arrays.asList("read", "write"), true, true, false,
                12, "192.168.1.101", "New York, NY", "EST", 89, 1, 88,
                Arrays.asList("frontend", "ui-ux"), 2, 1));

        mockUsers.add(createUserDto("user-3", "bob.johnson", "bob.johnson@example.com", "Bob Johnson",
                "viewer", "active", "#F59E0B", "Marketing",
                Arrays.asList("read"), false, true, true,
                5, "192.168.1.102", "Chicago, IL", "CST", 42, 0, 75,
                Arrays.asList("marketing", "analytics"), 1, 1));

        // Add more mock users as needed...

        return mockUsers;
    }

    private UserDto createUserDto(String id, String username, String email, String fullName,
                                  String role, String status, String avatarColor, String department,
                                  List<String> permissions, boolean mfaEnabled, boolean emailVerified,
                                  boolean phoneVerified, int apiAccessCount, String lastLoginIp,
                                  String location, String timezone, int totalLogins, int failedLogins,
                                  int securityScore, List<String> tags, int apiKeys, int activeSessions) {
        UserDto user = new UserDto();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(status);
        user.setAvatarColor(avatarColor);
        user.setDepartment(department);
        user.setPermissions(permissions);
        user.setMfaEnabled(mfaEnabled);
        user.setEmailVerified(emailVerified);
        user.setPhoneVerified(phoneVerified);
        user.setApiAccessCount(apiAccessCount);
        user.setLastLoginIp(lastLoginIp);
        user.setLocation(location);
        user.setTimezone(timezone);
        user.setTotalLogins(totalLogins);
        user.setFailedLogins(failedLogins);
        user.setSecurityScore(securityScore);
        user.setTags(tags);
        user.setApiKeys(apiKeys);
        user.setActiveSessions(activeSessions);

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        user.setLastActive(now.minusHours(2).format(DateTimeFormatter.ISO_DATE_TIME));
        user.setJoinedDate(now.minusMonths(6).format(DateTimeFormatter.ISO_DATE_TIME));

        // Add devices
        List<Map<String, Object>> devices = new ArrayList<>();
        devices.add(Map.of("type", "laptop", "lastUsed", now.minusHours(3).format(DateTimeFormatter.ISO_DATE_TIME)));
        devices.add(Map.of("type", "phone", "lastUsed", now.minusHours(4).format(DateTimeFormatter.ISO_DATE_TIME)));
        user.setDevices(devices);

        return user;
    }

    private RoleDto createRoleDto(String id, String name, List<String> permissions,
                                  String color, boolean isDefault, int userCount) {
        RoleDto role = new RoleDto();
        role.setId(id);
        role.setName(name);
        role.setDescription(getRoleDescription(id));
        role.setPermissions(permissions);
        role.setColor(color);
        role.setDefault(isDefault);
        role.setUserCount(userCount);
        role.setCreatedAt(new Date());
        return role;
    }

    private String getRoleDescription(String roleId) {
        switch(roleId) {
            case "admin": return "Full system access with administrative privileges";
            case "developer": return "Access to development tools and APIs";
            case "viewer": return "Read-only access to system resources";
            case "moderator": return "Content moderation and user management capabilities";
            default: return "Custom role";
        }
    }

    private boolean matchesSearch(UserDto user, String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }

        String query = searchQuery.toLowerCase();
        return user.getUsername().toLowerCase().contains(query) ||
                user.getEmail().toLowerCase().contains(query) ||
                user.getFullName().toLowerCase().contains(query) ||
                user.getDepartment().toLowerCase().contains(query) ||
                (user.getTags() != null && user.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query)));
    }

    private boolean matchesRole(UserDto user, String roleFilter) {
        if (roleFilter == null || roleFilter.equals("all")) {
            return true;
        }
        return user.getRole().equals(roleFilter);
    }

    private boolean matchesStatus(UserDto user, String statusFilter) {
        if (statusFilter == null || statusFilter.equals("all")) {
            return true;
        }
        return user.getStatus().equals(statusFilter);
    }

    private List<UserDto> sortUsers(List<UserDto> users, String sortField, String sortDirection) {
        if (sortField == null || sortField.isEmpty()) {
            return users;
        }

        List<UserDto> sorted = new ArrayList<>(users);

        sorted.sort((a, b) -> {
            int result = 0;

            switch (sortField) {
                case "fullName":
                    result = a.getFullName().compareToIgnoreCase(b.getFullName());
                    break;
                case "lastActive":
                    result = a.getLastActive().compareTo(b.getLastActive());
                    break;
                case "joinedDate":
                    result = a.getJoinedDate().compareTo(b.getJoinedDate());
                    break;
                case "securityScore":
                    result = Integer.compare(a.getSecurityScore(), b.getSecurityScore());
                    break;
                case "role":
                    result = a.getRole().compareTo(b.getRole());
                    break;
                case "status":
                    result = a.getStatus().compareTo(b.getStatus());
                    break;
                default:
                    result = 0;
            }

            return sortDirection != null && sortDirection.equalsIgnoreCase("desc") ? -result : result;
        });

        return sorted;
    }

    private void updateStatistics() {
        int totalUsers = users.size();
        int activeUsers = (int) users.stream().filter(u -> u.getStatus().equals("active")).count();
        int admins = (int) users.stream().filter(u -> u.getRole().equals("admin")).count();
        int developers = (int) users.stream().filter(u -> u.getRole().equals("developer")).count();
        int viewers = (int) users.stream().filter(u -> u.getRole().equals("viewer")).count();
        int pendingUsers = (int) users.stream().filter(u -> u.getStatus().equals("pending")).count();
        int suspendedUsers = (int) users.stream().filter(u -> u.getStatus().equals("suspended")).count();
        int mfaEnabled = (int) users.stream().filter(UserDto::isMfaEnabled).count();
        double avgSecurityScore = users.stream()
                .mapToInt(UserDto::getSecurityScore)
                .average()
                .orElse(0.0);

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("admins", admins);
        stats.put("developers", developers);
        stats.put("viewers", viewers);
        stats.put("pendingUsers", pendingUsers);
        stats.put("suspendedUsers", suspendedUsers);
        stats.put("mfaEnabled", mfaEnabled);
        stats.put("avgSecurityScore", avgSecurityScore);
    }

    private List<ActivityLog> getMockActivityLog(String userId) {
        List<ActivityLog> activities = new ArrayList<>();

        activities.add(ActivityLog.builder()
                .id("activity-1")
                .type("login")
                .description("User logged in successfully")
                .timestamp(new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000)) // 2 hours ago
                .ipAddress("192.168.1.100")
                .device("Chrome on Windows")
                .location("San Francisco, CA")
                .success(true)
                .build());

        activities.add(ActivityLog.builder()
                .id("activity-2")
                .type("password_change")
                .description("Password changed successfully")
                .timestamp(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)) // 1 day ago
                .ipAddress("192.168.1.100")
                .device("Chrome on Windows")
                .location("San Francisco, CA")
                .success(true)
                .build());

        activities.add(ActivityLog.builder()
                .id("activity-3")
                .type("api_call")
                .description("GET /api/v1/users")
                .timestamp(new Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000)) // 3 hours ago
                .ipAddress("192.168.1.100")
                .device("Chrome on Windows")
                .location("San Francisco, CA")
                .success(true)
                .build());

        return activities;
    }

    private Map<String, Boolean> getPermissionsBreakdown(String role) {
        Map<String, Boolean> permissions = new HashMap<>();

        switch (role) {
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

    private List<String> getDefaultPermissions(String role) {
        switch (role) {
            case "admin":
                return Arrays.asList("read", "write", "delete", "admin", "manage_users", "manage_roles");
            case "developer":
                return Arrays.asList("read", "write", "api_access", "debug");
            case "viewer":
                return Arrays.asList("read");
            default:
                return Arrays.asList("read");
        }
    }

    private String generateRandomColor() {
        String[] colors = {"#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899", "#14B8A6", "#F97316"};
        return colors[new Random().nextInt(colors.length)];
    }

    private String getTimezoneByLocation(String location) {
        if (location == null) return "UTC";

        if (location.contains("San Francisco") || location.contains("CA")) return "PST";
        if (location.contains("New York") || location.contains("NY")) return "EST";
        if (location.contains("Chicago") || location.contains("IL")) return "CST";
        if (location.contains("Denver") || location.contains("CO")) return "MST";

        return "UTC";
    }

    private int calculateInitialSecurityScore(CreateUserRequest request) {
        int score = 50; // Base score

        if (request.isMfaEnabled()) score += 20;
        if (request.getPassword() != null && request.getPassword().length() >= 12) score += 15;
        if (request.getEmail() != null && request.getEmail().contains("@company.com")) score += 10;

        return Math.min(score, 100);
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

    private boolean matchesSearchCriteria(UserDto user, SearchUsersRequest searchRequest) {
        if (searchRequest.getQuery() == null || searchRequest.getQuery().isEmpty()) {
            return true;
        }

        String query = searchRequest.getQuery().toLowerCase();
        boolean matches = user.getUsername().toLowerCase().contains(query) ||
                user.getEmail().toLowerCase().contains(query) ||
                user.getFullName().toLowerCase().contains(query) ||
                user.getDepartment().toLowerCase().contains(query);

        // Apply additional filters if present
        if (searchRequest.getFilters() != null) {
            Map<String, Object> filters = searchRequest.getFilters();

            if (filters.containsKey("role") && !user.getRole().equals(filters.get("role"))) {
                matches = false;
            }

            if (filters.containsKey("status") && !user.getStatus().equals(filters.get("status"))) {
                matches = false;
            }

            if (filters.containsKey("department") && !user.getDepartment().equals(filters.get("department"))) {
                matches = false;
            }
        }

        return matches;
    }

    private boolean matchesExportFilters(UserDto user, Map<String, Object> filters) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            switch (entry.getKey()) {
                case "role":
                    if (!user.getRole().equals(entry.getValue())) return false;
                    break;
                case "status":
                    if (!user.getStatus().equals(entry.getValue())) return false;
                    break;
                case "department":
                    if (!user.getDepartment().equals(entry.getValue())) return false;
                    break;
                case "mfaEnabled":
                    if (user.isMfaEnabled() != (boolean) entry.getValue()) return false;
                    break;
            }
        }
        return true;
    }
}