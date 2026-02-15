// ============================================
// UPDATED CONTROLLER WITH ALL HARDCODED ROLE VALIDATIONS REMOVED
// ============================================

// controllers/UserManagementController.js
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// ============ USER MANAGEMENT METHODS ============

/**
 * Get users list with filters
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Filter parameters
 * @param {string} filters.searchQuery - Search query
 * @param {string} filters.roleFilter - Role ID (UUID)
 * @param {string} filters.statusFilter - Status filter
 * @param {string} filters.sortField - Sort field
 * @param {string} filters.sortDirection - Sort direction (asc/desc)
 * @param {number} filters.page - Page number
 * @param {number} filters.pageSize - Page size
 * @returns {Promise} API response
 */
export const getUsersList = async (authorizationHeader, filters = {}) => {
  const params = new URLSearchParams();
  
  if (filters.searchQuery) params.append('searchQuery', filters.searchQuery);
  if (filters.roleFilter && filters.roleFilter !== 'all') params.append('roleFilter', filters.roleFilter);
  if (filters.statusFilter && filters.statusFilter !== 'all') params.append('statusFilter', filters.statusFilter);
  if (filters.sortField) params.append('sortField', filters.sortField);
  if (filters.sortDirection) params.append('sortDirection', filters.sortDirection);
  if (filters.page) params.append('page', filters.page);
  if (filters.pageSize) params.append('pageSize', filters.pageSize);
  
  const queryString = params.toString();
  const url = `/user-management/users${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get user details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} userId - User ID
 * @returns {Promise} API response
 */
export const getUserDetails = async (authorizationHeader, userId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/${userId}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Create new user
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} createRequest - Create user request data
 * @param {string} createRequest.username - Username (required)
 * @param {string} createRequest.email - Email (required)
 * @param {string} createRequest.fullName - Full name (required)
 * @param {string} createRequest.roleId - Role ID (UUID) (required)
 * @param {string} createRequest.department - Department
 * @param {string} createRequest.location - Location
 * @param {boolean} createRequest.mfaEnabled - Enable MFA
 * @param {string} createRequest.status - Status
 * @param {string} createRequest.password - Password (required for creation)
 * @returns {Promise} API response
 */
export const createUser = async (authorizationHeader, createRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(createRequest)
    })
  );
};

/**
 * Update user
 * @param {string} authorizationHeader - Bearer token
 * @param {string} userId - User ID
 * @param {Object} updateRequest - Update user request data
 * @param {string} updateRequest.fullName - Full name
 * @param {string} updateRequest.roleId - Role ID (UUID)
 * @param {string} updateRequest.status - Status
 * @param {string} updateRequest.department - Department
 * @param {boolean} updateRequest.mfaEnabled - Enable MFA
 * @param {boolean} updateRequest.emailVerified - Email verified
 * @param {boolean} updateRequest.phoneVerified - Phone verified
 * @param {string} updateRequest.location - Location
 * @param {Array} updateRequest.tags - Tags
 * @returns {Promise} API response
 */
export const updateUser = async (authorizationHeader, userId, updateRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/${userId}`, {
      method: 'PUT',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(updateRequest)
    })
  );
};

/**
 * Delete user
 * @param {string} authorizationHeader - Bearer token
 * @param {string} userId - User ID
 * @returns {Promise} API response
 */
export const deleteUser = async (authorizationHeader, userId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/${userId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Bulk operations on users
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} bulkRequest - Bulk operation request data
 * @param {string} bulkRequest.operation - Operation type (activate/suspend/deactivate/delete/reset_password) (required)
 * @param {Array} bulkRequest.userIds - Array of user IDs (required)
 * @param {Object} bulkRequest.options - Additional options
 * @returns {Promise} API response
 */
export const bulkUserOperation = async (authorizationHeader, bulkRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/bulk`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(bulkRequest)
    })
  );
};

/**
 * Reset user password
 * @param {string} authorizationHeader - Bearer token
 * @param {string} userId - User ID
 * @param {Object} resetRequest - Reset password request data
 * @param {boolean} resetRequest.forceLogout - Force logout all devices
 * @param {string} resetRequest.resetMethod - Reset method (email/sms/temporary)
 * @returns {Promise} API response
 */
export const resetUserPassword = async (authorizationHeader, userId, resetRequest = {}) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/${userId}/reset-password`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify({ ...resetRequest, userId })
    })
  );
};

/**
 * Get user statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getUserStatistics = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/statistics`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Search users with advanced criteria
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchRequest - Search request data
 * @param {string} searchRequest.query - Search query (required)
 * @param {Object} searchRequest.filters - Additional filters
 * @param {string} searchRequest.filters.roleId - Filter by role ID (UUID)
 * @param {string} searchRequest.filters.status - Filter by status
 * @param {string} searchRequest.filters.department - Filter by department
 * @param {boolean} searchRequest.filters.mfaEnabled - Filter by MFA status
 * @param {Date} searchRequest.filters.createdAfter - Filter by creation date
 * @returns {Promise} API response
 */
export const searchUsers = async (authorizationHeader, searchRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/search`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(searchRequest)
    })
  );
};

/**
 * Import users from file
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} importRequest - Import users request data
 * @param {string} importRequest.fileName - File name (required)
 * @param {string} importRequest.fileType - File type (csv/json/excel) (required)
 * @param {string} importRequest.fileContent - File content (base64 encoded)
 * @param {Object} importRequest.options - Import options
 * @param {boolean} importRequest.options.sendWelcomeEmail - Send welcome email
 * @param {boolean} importRequest.options.generatePasswords - Generate passwords
 * @param {string} importRequest.options.defaultRoleId - Default role ID (UUID)
 * @returns {Promise} API response
 */
export const importUsers = async (authorizationHeader, importRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/import`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(importRequest)
    })
  );
};

/**
 * Export users data
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} exportRequest - Export users request data
 * @param {string} exportRequest.format - Export format (csv/json/excel/pdf) (required)
 * @param {Object} exportRequest.filters - Export filters
 * @param {string} exportRequest.filters.roleId - Filter by role ID (UUID)
 * @param {string} exportRequest.filters.status - Filter by status
 * @param {string} exportRequest.filters.department - Filter by department
 * @param {Date} exportRequest.filters.createdAfter - Filter by creation date
 * @param {Array} exportRequest.fields - Fields to include in export
 * @returns {Promise} API response
 */
export const exportUsers = async (authorizationHeader, exportRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/export`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(exportRequest)
    })
  );
};

/**
 * Get user activity log
 * @param {string} authorizationHeader - Bearer token
 * @param {string} userId - User ID
 * @param {Date} startDate - Start date for activity filter
 * @param {Date} endDate - End date for activity filter
 * @returns {Promise} API response
 */
export const getUserActivity = async (authorizationHeader, userId, startDate = null, endDate = null) => {
  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate.toISOString());
  if (endDate) params.append('endDate', endDate.toISOString());
  
  const queryString = params.toString();
  const url = `/user-management/users/${userId}/activity${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Update user status
 * @param {string} authorizationHeader - Bearer token
 * @param {string} userId - User ID
 * @param {string} status - New status (active/inactive/pending/suspended) (required)
 * @returns {Promise} API response
 */
export const updateUserStatus = async (authorizationHeader, userId, status) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/users/${userId}/status?status=${encodeURIComponent(status)}`, {
      method: 'PATCH',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get roles and permissions
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getRolesAndPermissions = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/roles`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Validate user data
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} validationRequest - Validation request data
 * @param {string} validationRequest.userId - User ID (for updates)
 * @param {string} validationRequest.email - Email to validate
 * @param {string} validationRequest.username - Username to validate
 * @param {string} validationRequest.fullName - Full name to validate
 * @param {string} validationRequest.roleId - Role ID to validate (UUID)
 * @param {Object} validationRequest.options - Validation options
 * @returns {Promise} API response
 */
export const validateUserData = async (authorizationHeader, validationRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/user-management/validate`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(validationRequest)
    })
  );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for user management operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleUserManagementResponse = (response) => {
  if (!response) {
    throw new Error('No response received from user management service');
  }

  const responseCode = response.responseCode || response.status;
  
  if (responseCode === 200 || responseCode === 201) {
    return {
      ...response,
      data: response.data || {},
      responseCode: responseCode,
      requestId: response.requestId
    };
  }

  const errorMessage = response.message || response.error || 'Unknown error';
  switch (responseCode) {
    case 400: 
      throw new Error(`Bad Request: ${errorMessage}`);
    case 401: 
      throw new Error(`Unauthorized: ${errorMessage}`);
    case 403: 
      throw new Error(`Forbidden: ${errorMessage}`);
    case 404: 
      throw new Error(`Not Found: ${errorMessage}`);
    case 409: 
      throw new Error(`Conflict: ${errorMessage}`);
    case 422: 
      throw new Error(`Validation Error: ${errorMessage}`);
    case 500: 
      throw new Error(`Server Error: ${errorMessage}`);
    default: 
      throw new Error(`Error ${responseCode || 'Unknown'}: ${errorMessage}`);
  }
};

/**
 * Extract users list from response
 * @param {Object} response - API response
 * @returns {Array} Users list
 */
export const extractUsersList = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (data.users && Array.isArray(data.users)) {
    return data.users;
  }
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  if (data.content && Array.isArray(data.content)) {
    return data.content;
  }
  
  return [];
};

/**
 * Extract user details from response
 * @param {Object} response - API response
 * @returns {Object} User details
 */
export const extractUserDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    id: details.id || details.userId,
    username: details.username,
    email: details.email,
    fullName: details.fullName,
    roleId: details.roleId || details.role,
    roleName: details.roleName,
    status: details.status,
    avatarColor: details.avatarColor,
    department: details.department,
    permissions: details.permissions || [],
    mfaEnabled: details.mfaEnabled || false,
    emailVerified: details.emailVerified || false,
    phoneVerified: details.phoneVerified || false,
    lastActive: details.lastActive,
    joinedDate: details.joinedDate,
    location: details.location,
    timezone: details.timezone,
    securityScore: details.securityScore || 0,
    tags: details.tags || [],
    devices: details.devices || [],
    apiKeys: details.apiKeys || 0,
    activeSessions: details.activeSessions || 0,
    activityLog: details.activityLog || [],
    permissionsBreakdown: details.permissionsBreakdown || {}
  };
};

/**
 * Extract create user results
 * @param {Object} response - API response
 * @returns {Object} Create user results
 */
export const extractCreateUserResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    id: data.id || data.userId,
    success: data.success !== false,
    message: data.message || 'User created successfully',
    user: data.user || {},
    generatedAt: data.generatedAt,
    nextSteps: data.nextSteps || []
  };
};

/**
 * Extract update user results
 * @param {Object} response - API response
 * @returns {Object} Update user results
 */
export const extractUpdateUserResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    success: data.success !== false,
    message: data.message || 'User updated successfully',
    updatedAt: data.updatedAt,
    user: data.user || {},
    auditLog: data.auditLog || {}
  };
};

/**
 * Extract delete user results
 * @param {Object} response - API response
 * @returns {Object} Delete user results
 */
export const extractDeleteUserResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    success: data.success !== false,
    message: data.message || 'User deleted successfully',
    deletedAt: data.deletedAt,
    deletedUser: data.deletedUser || {}
  };
};

/**
 * Extract bulk operation results
 * @param {Object} response - API response
 * @returns {Object} Bulk operation results
 */
export const extractBulkOperationResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    operation: data.operation,
    processedCount: data.processedCount || 0,
    failedCount: data.failedCount || 0,
    processedUsers: data.processedUsers || [],
    failedUsers: data.failedUsers || [],
    completedAt: data.completedAt
  };
};

/**
 * Extract reset password results
 * @param {Object} response - API response
 * @returns {Object} Reset password results
 */
export const extractResetPasswordResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    success: data.success !== false,
    message: data.message || 'Password reset successful',
    userId: data.userId,
    resetAt: data.resetAt,
    expiresAt: data.expiresAt,
    securityInfo: data.securityInfo || {}
  };
};

/**
 * Extract user statistics
 * @param {Object} response - API response
 * @returns {Object} User statistics
 */
export const extractUserStatistics = (response) => {
  if (!response || !response.data) return {
    totalUsers: 0,
    activeUsers: 0,
    admins: 0,
    developers: 0,
    pendingUsers: 0,
    suspendedUsers: 0,
    mfaEnabled: 0,
    avgSecurityScore: 0
  };
  
  const data = response.data;
  
  return {
    totalUsers: data.totalUsers || 0,
    activeUsers: data.activeUsers || 0,
    admins: data.admins || 0,
    developers: data.developers || 0,
    pendingUsers: data.pendingUsers || 0,
    suspendedUsers: data.suspendedUsers || 0,
    mfaEnabled: data.mfaEnabledUsers || data.mfaEnabled || 0,
    avgSecurityScore: data.avgSecurityScore || 0,
    generatedAt: data.generatedAt,
    trends: data.trends || {},
    departmentBreakdown: data.departmentBreakdown || {},
    roleDistribution: data.roleDistribution || {},
    statusDistribution: data.statusDistribution || {}
  };
};

/**
 * Extract search users results
 * @param {Object} response - API response
 * @returns {Object} Search results
 */
export const extractSearchUsersResults = (response) => {
  if (!response || !response.data) return { results: [], total: 0 };
  
  const data = response.data;
  
  return {
    query: data.query,
    results: data.results || data.users || [],
    total: data.total || 0,
    searchAt: data.searchAt,
    metadata: data.metadata || {}
  };
};

/**
 * Extract import users results
 * @param {Object} response - API response
 * @returns {Object} Import users results
 */
export const extractImportUsersResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    fileName: data.fileName,
    fileType: data.fileType,
    totalRecords: data.totalRecords || 0,
    importedCount: data.importedCount || 0,
    failedCount: data.failedCount || 0,
    importedUsers: data.importedUsers || [],
    importErrors: data.importErrors || [],
    importedAt: data.importedAt,
    status: data.status,
    summary: data.summary || {}
  };
};

/**
 * Extract export users results
 * @param {Object} response - API response
 * @returns {Object} Export users results
 */
export const extractExportUsersResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    format: data.format,
    totalUsers: data.totalUsers || 0,
    exportedAt: data.exportedAt,
    status: data.status,
    exportData: data.exportData || {}
  };
};

/**
 * Extract user activity results
 * @param {Object} response - API response
 * @returns {Object} User activity results
 */
export const extractUserActivityResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    userId: data.userId,
    activities: data.activities || [],
    totalActivities: data.totalActivities || 0,
    generatedAt: data.generatedAt,
    activityStats: data.activityStats || {}
  };
};

/**
 * Extract update status results
 * @param {Object} response - API response
 * @returns {Object} Update status results
 */
export const extractUpdateStatusResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    success: data.success !== false,
    message: data.message || 'Status updated successfully',
    userId: data.userId,
    previousStatus: data.previousStatus,
    newStatus: data.newStatus,
    updatedAt: data.updatedAt,
    notificationInfo: data.notificationInfo || {}
  };
};

/**
 * Extract roles and permissions results
 * @param {Object} response - API response
 * @returns {Object} Roles and permissions results
 */
export const extractRolesAndPermissions = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    roles: data.roles || data.content || [],
    totalRoles: data.totalRoles || 0,
    generatedAt: data.generatedAt,
    permissionCategories: data.permissionCategories || {}
  };
};

/**
 * Extract validation results
 * @param {Object} response - API response
 * @returns {Object} Validation results
 */
export const extractValidationResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    valid: data.valid !== false,
    issues: data.issues || [],
    validatedAt: data.validatedAt,
    score: data.score || 0
  };
};

// ============ VALIDATION FUNCTIONS - REMOVED ALL HARDCODED ROLE VALIDATIONS ============

/**
 * Validate create user request
 * @param {Object} createRequest - Create user request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateCreateUser = (createRequest) => {
  const errors = [];
  
  if (!createRequest.username) {
    errors.push('Username is required');
  } else if (createRequest.username.length < 3) {
    errors.push('Username must be at least 3 characters long');
  }
  
  if (!createRequest.email) {
    errors.push('Email is required');
  } else if (!isValidEmail(createRequest.email)) {
    errors.push('Valid email is required');
  }
  
  if (!createRequest.fullName) {
    errors.push('Full name is required');
  }
  
  if (!createRequest.roleId) {
    errors.push('Role ID is required');
  }
  // Role ID validation removed - now treated as UUID string from API
  
  if (!createRequest.password) {
    errors.push('Password is required');
  } else if (createRequest.password.length < 8) {
    errors.push('Password must be at least 8 characters long');
  }
  
  return errors;
};

/**
 * Validate update user request
 * @param {Object} updateRequest - Update user request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateUpdateUser = (updateRequest) => {
  const errors = [];
  
  if (updateRequest.email && !isValidEmail(updateRequest.email)) {
    errors.push('Valid email is required');
  }
  
  if (updateRequest.status) {
    const validStatuses = ['active', 'inactive', 'pending', 'suspended'];
    if (!validStatuses.includes(updateRequest.status.toLowerCase())) {
      errors.push(`Status must be one of: ${validStatuses.join(', ')}`);
    }
  }
  
  if (updateRequest.fullName && updateRequest.fullName.trim().length === 0) {
    errors.push('Full name cannot be empty');
  }
  
  // Role ID validation removed - now treated as UUID string from API
  
  return errors;
};

/**
 * Validate bulk operation request
 * @param {Object} bulkRequest - Bulk operation request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateBulkOperation = (bulkRequest) => {
  const errors = [];
  
  if (!bulkRequest.operation) {
    errors.push('Operation type is required');
  } else {
    const validOperations = ['activate', 'suspend', 'deactivate', 'delete', 'reset_password'];
    if (!validOperations.includes(bulkRequest.operation.toLowerCase())) {
      errors.push(`Operation must be one of: ${validOperations.join(', ')}`);
    }
  }
  
  if (!bulkRequest.userIds || !Array.isArray(bulkRequest.userIds) || bulkRequest.userIds.length === 0) {
    errors.push('At least one user ID is required');
  }
  
  if (bulkRequest.userIds && bulkRequest.userIds.length > 100) {
    errors.push('Maximum 100 users allowed per bulk operation');
  }
  
  return errors;
};

/**
 * Validate search users request
 * @param {Object} searchRequest - Search request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateSearchUsers = (searchRequest) => {
  const errors = [];
  
  if (!searchRequest.query || searchRequest.query.trim().length === 0) {
    errors.push('Search query is required');
  }
  
  if (searchRequest.query && searchRequest.query.length > 255) {
    errors.push('Search query must be 255 characters or less');
  }
  
  return errors;
};

/**
 * Validate import users request
 * @param {Object} importRequest - Import request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateImportUsers = (importRequest) => {
  const errors = [];
  
  if (!importRequest.fileName) {
    errors.push('File name is required');
  }
  
  if (!importRequest.fileType) {
    errors.push('File type is required');
  } else {
    const validFileTypes = ['csv', 'json', 'excel', 'xlsx', 'xls'];
    if (!validFileTypes.includes(importRequest.fileType.toLowerCase())) {
      errors.push(`File type must be one of: ${validFileTypes.join(', ')}`);
    }
  }
  
  if (!importRequest.fileContent) {
    errors.push('File content is required');
  }
  
  return errors;
};

/**
 * Validate export users request
 * @param {Object} exportRequest - Export request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateExportUsers = (exportRequest) => {
  const errors = [];
  
  if (!exportRequest.format) {
    errors.push('Export format is required');
  } else {
    const validFormats = ['csv', 'json', 'excel', 'xlsx', 'xls', 'pdf'];
    if (!validFormats.includes(exportRequest.format.toLowerCase())) {
      errors.push(`Format must be one of: ${validFormats.join(', ')}`);
    }
  }
  
  return errors;
};

/**
 * Validate update status request
 * @param {string} status - Status to validate
 * @returns {Array} Array of validation errors
 */
export const validateUpdateStatus = (status) => {
  const errors = [];
  
  if (!status) {
    errors.push('Status is required');
  } else {
    const validStatuses = ['active', 'inactive', 'pending', 'suspended'];
    if (!validStatuses.includes(status.toLowerCase())) {
      errors.push(`Status must be one of: ${validStatuses.join(', ')}`);
    }
  }
  
  return errors;
};

/**
 * Validate user data request
 * @param {Object} validationRequest - Validation request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateUserDataRequest = (validationRequest) => {
  const errors = [];
  
  if (!validationRequest.email && !validationRequest.username && !validationRequest.fullName) {
    errors.push('At least one field (email, username, or fullName) is required for validation');
  }
  
  if (validationRequest.email && !isValidEmail(validationRequest.email)) {
    errors.push('Valid email is required');
  }
  
  if (validationRequest.username && validationRequest.username.length < 3) {
    errors.push('Username must be at least 3 characters long');
  }
  
  return errors;
};

// ============ HELPER FUNCTIONS ============

/**
 * Check if email is valid
 * @param {string} email - Email to validate
 * @returns {boolean} True if email is valid
 */
export const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Generate a deterministic color from a UUID string
 * @param {string} uuid - UUID string
 * @returns {string} HSL color string
 */
export const getColorFromUuid = (uuid) => {
  if (!uuid) return '#6B7280'; // Default gray
  
  // Create a hash from the UUID
  let hash = 0;
  for (let i = 0; i < uuid.length; i++) {
    hash = ((hash << 5) - hash) + uuid.charCodeAt(i);
    hash |= 0; // Convert to 32-bit integer
  }
  
  // Generate HSL values
  const hue = Math.abs(hash) % 360;
  const saturation = 70 + (Math.abs(hash >> 8) % 20); // 70-90%
  const lightness = 45 + (Math.abs(hash >> 16) % 15); // 45-60%
  
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`;
};

/**
 * Get role color from UUID
 * @param {string} roleId - Role UUID
 * @returns {string} Color code
 */
export const getRoleColor = (roleId) => {
  return getColorFromUuid(roleId);
};

/**
 * Get user status display name
 * @param {string} status - Status identifier
 * @returns {string} Display name for status
 */
export const getUserStatusDisplayName = (status) => {
  const statusMap = {
    active: 'Active',
    inactive: 'Inactive',
    pending: 'Pending',
    suspended: 'Suspended'
  };
  
  return statusMap[status] || status || 'Unknown';
};

/**
 * Get status color
 * @param {string} status - User status
 * @returns {string} Color code
 */
export const getStatusColor = (status) => {
  const colors = {
    active: '#10B981',    // Green
    inactive: '#6B7280',  // Gray
    pending: '#F59E0B',   // Amber
    suspended: '#EF4444'  // Red
  };
  
  return colors[status] || '#6B7280';
};

/**
 * Get security score color
 * @param {number} score - Security score (0-100)
 * @returns {string} Color code
 */
export const getSecurityScoreColor = (score) => {
  if (score >= 90) return '#10B981';  // Green
  if (score >= 70) return '#F59E0B';  // Amber
  return '#EF4444';                   // Red
};

/**
 * Format date for display
 * @param {string|Date} date - Date to format
 * @param {boolean} includeTime - Whether to include time
 * @returns {string} Formatted date
 */
export const formatDateForDisplay = (date, includeTime = false) => {
  if (!date) return '';
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  if (isNaN(dateObj.getTime())) return '';
  
  const options = {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    ...(includeTime && { hour: '2-digit', minute: '2-digit' })
  };
  
  return dateObj.toLocaleDateString('en-US', options);
};

/**
 * Get default user filters
 * @returns {Object} Default filter values
 */
export const getDefaultUserFilters = () => {
  return {
    searchQuery: '',
    roleFilter: 'all',
    statusFilter: 'all',
    sortField: 'fullName',
    sortDirection: 'asc',
    page: 1,
    pageSize: 10
  };
};

/**
 * Get export fields for user data
 * @returns {Array} Available export fields
 */
export const getExportFields = () => {
  return [
    { value: 'id', label: 'ID' },
    { value: 'username', label: 'Username' },
    { value: 'email', label: 'Email' },
    { value: 'fullName', label: 'Full Name' },
    { value: 'roleId', label: 'Role ID' },
    { value: 'roleName', label: 'Role Name' },
    { value: 'status', label: 'Status' },
    { value: 'department', label: 'Department' },
    { value: 'lastActive', label: 'Last Active' },
    { value: 'joinedDate', label: 'Joined Date' },
    { value: 'securityScore', label: 'Security Score' },
    { value: 'mfaEnabled', label: 'MFA Enabled' },
    { value: 'emailVerified', label: 'Email Verified' },
    { value: 'location', label: 'Location' },
    { value: 'timezone', label: 'Timezone' }
  ];
};

/**
 * Generate CSV content from user data
 * @param {Array} users - Array of user objects
 * @param {Array} fields - Fields to include
 * @returns {string} CSV content
 */
export const generateCSVFromUsers = (users, fields = []) => {
  const exportFields = fields.length > 0 ? fields : getExportFields();
  
  // Create header row
  const headers = exportFields.map(field => `"${field.label}"`).join(',');
  
  // Create data rows
  const rows = users.map(user => {
    const rowData = exportFields.map(field => {
      let value = user[field.value];
      
      // Handle special cases
      if (field.value === 'mfaEnabled' || field.value === 'emailVerified') {
        value = value ? 'Yes' : 'No';
      } else if (field.value === 'lastActive' || field.value === 'joinedDate') {
        value = formatDateForDisplay(value, field.value === 'lastActive');
      } else if (field.value === 'roleName' && !value && user.role) {
        value = user.role?.name || user.role?.roleName || '';
      }
      
      return `"${value || ''}"`;
    });
    
    return rowData.join(',');
  });
  
  return [headers, ...rows].join('\n');
};

/**
 * Cache user management data in localStorage
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @param {Object} data - Data to cache
 * @param {number} ttlMinutes - Time to live in minutes (default: 15)
 */
export const cacheUserManagementData = (userId, cacheKey, data, ttlMinutes = 15) => {
  if (!userId || !cacheKey || !data) return;
  
  const fullCacheKey = `usermgmt_cache_${userId}_${cacheKey}`;
  const cacheData = {
    data: data,
    timestamp: Date.now(),
    ttl: ttlMinutes * 60 * 1000 // Convert to milliseconds
  };
  
  try {
    localStorage.setItem(fullCacheKey, JSON.stringify(cacheData));
  } catch (error) {
    console.error('Failed to cache user management data:', error);
  }
};

/**
 * Get cached user management data
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @returns {Object|null} Cached data or null
 */
export const getCachedUserManagementData = (userId, cacheKey) => {
  if (!userId || !cacheKey) return null;
  
  const fullCacheKey = `usermgmt_cache_${userId}_${cacheKey}`;
  
  try {
    const cached = localStorage.getItem(fullCacheKey);
    
    if (!cached) return null;
    
    const cacheData = JSON.parse(cached);
    
    // Check if cache is expired
    const now = Date.now();
    const isExpired = now - cacheData.timestamp > cacheData.ttl;
    
    if (isExpired) {
      localStorage.removeItem(fullCacheKey);
      return null;
    }
    
    return cacheData.data;
  } catch (error) {
    console.error('Failed to get cached user management data:', error);
    return null;
  }
};

/**
 * Clear cached user management data
 * @param {string} userId - User ID (optional, clears all if not provided)
 * @param {string} cacheKey - Cache key (optional, clears all for user if not provided)
 */
export const clearCachedUserManagementData = (userId = null, cacheKey = null) => {
  try {
    if (userId && cacheKey) {
      localStorage.removeItem(`usermgmt_cache_${userId}_${cacheKey}`);
    } else if (userId) {
      // Clear all user management cache for user
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith(`usermgmt_cache_${userId}_`)) {
          localStorage.removeItem(key);
        }
      });
    } else {
      // Clear all user management cache
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('usermgmt_cache_')) {
          localStorage.removeItem(key);
        }
      });
    }
  } catch (error) {
    console.error('Failed to clear cached user management data:', error);
  }
};

// Export all functions
export default {
  // Main API methods
  getUsersList,
  getUserDetails,
  createUser,
  updateUser,
  deleteUser,
  bulkUserOperation,
  resetUserPassword,
  getUserStatistics,
  searchUsers,
  importUsers,
  exportUsers,
  getUserActivity,
  updateUserStatus,
  getRolesAndPermissions,
  validateUserData,
  
  // Response handlers
  handleUserManagementResponse,
  extractUsersList,
  extractUserDetails,
  extractCreateUserResults,
  extractUpdateUserResults,
  extractDeleteUserResults,
  extractBulkOperationResults,
  extractResetPasswordResults,
  extractUserStatistics,
  extractSearchUsersResults,
  extractImportUsersResults,
  extractExportUsersResults,
  extractUserActivityResults,
  extractUpdateStatusResults,
  extractRolesAndPermissions,
  extractValidationResults,
  
  // Validation functions - NO HARDCODED ROLE VALIDATIONS
  validateCreateUser,
  validateUpdateUser,
  validateBulkOperation,
  validateSearchUsers,
  validateImportUsers,
  validateExportUsers,
  validateUpdateStatus,
  validateUserDataRequest,
  
  // Utility functions
  isValidEmail,
  getColorFromUuid,
  getRoleColor,
  getUserStatusDisplayName,
  getStatusColor,
  getSecurityScoreColor,
  formatDateForDisplay,
  getDefaultUserFilters,
  getExportFields,
  generateCSVFromUsers,
  
  // Cache functions
  cacheUserManagementData,
  getCachedUserManagementData,
  clearCachedUserManagementData
};