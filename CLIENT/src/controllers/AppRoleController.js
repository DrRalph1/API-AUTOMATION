// controllers/AppRoleController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for pagination
const buildRoleQueryParams = (params = {}) => {
    const queryParams = new URLSearchParams();
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            if (Array.isArray(params[key])) {
                params[key].forEach(value => queryParams.append(key, value));
            } else {
                queryParams.append(key, params[key]);
            }
        }
    });
    return queryParams;
};

// ============ APP ROLE MANAGEMENT METHODS ============

/**
 * Create a new role
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} roleData - AppRole DTO
 * @returns {Promise} API response
 */
export const createRole = async (authorizationHeader, roleData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/roles`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(roleData)
        })
    );
};

/**
 * Create multiple roles in bulk
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} rolesData - Array of AppRole DTOs
 * @returns {Promise} API response
 */
export const createRolesBulk = async (authorizationHeader, rolesData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/roles/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(rolesData)
        })
    );
};

/**
 * Get a single role by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} roleId - Role ID (UUID)
 * @returns {Promise} API response
 */
export const getRole = async (authorizationHeader, roleId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/roles/${roleId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get role by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} roleId - Role ID (UUID)
 * @returns {Promise} API response
 */
export const getRoleById = async (authorizationHeader, roleId) => {
    return getRole(authorizationHeader, roleId);
};

/**
 * Get all roles (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllRoles = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 10, sort = 'roleName', direction = 'ASC' } = pagination;
    
    const queryParams = buildRoleQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/roles${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get all roles
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getRoles = async (authorizationHeader) => {
    return getAllRoles(authorizationHeader);
};

/**
 * Update a role by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} roleId - Role ID (UUID)
 * @param {Object} roleData - Updated role data
 * @returns {Promise} API response
 */
export const updateRole = async (authorizationHeader, roleId, roleData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/roles/${roleId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(roleData)
        })
    );
};

/**
 * Alternative method to update role
 * @param {string} authorizationHeader - Bearer token
 * @param {string} roleId - Role ID (UUID)
 * @param {Object} roleData - Updated role data
 * @returns {Promise} API response
 */
export const updateRoleById = async (authorizationHeader, roleId, roleData) => {
    return updateRole(authorizationHeader, roleId, roleData);
};

/**
 * Delete a role by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} roleId - Role ID (UUID)
 * @returns {Promise} API response
 */
export const deleteRole = async (authorizationHeader, roleId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/roles/${roleId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for role operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleRoleResponse = (response) => {
    if (!response) {
        throw new Error('No response received from role service');
    }

    // Handle bulk response (207 status)
    if (response.responseCode === 207) {
        return response; // Return as-is for partial success
    }

    if (response.responseCode === 200 || response.responseCode === 201) {
        return response.data || response;
    }

    switch (response.responseCode) {
        case 204:
            return { message: 'No roles found', data: [] };
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Role not found: ${response.message}`);
        case 409: throw new Error(`Conflict: ${response.message}`);
        case 422: throw new Error(`Business rule violation: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

/**
 * Extract pagination info from response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractRolePaginationInfo = (response) => {
    if (!response.data) return null;

    // Check if response is a paginated response
    if (response.data && typeof response.data === 'object' && 'content' in response.data) {
        const pageData = response.data;
        return {
            content: pageData.content || [],
            totalPages: pageData.totalPages || 0,
            totalElements: pageData.totalElements || 0,
            size: pageData.size || 0,
            number: pageData.number || 0,
            first: pageData.first || false,
            last: pageData.last || false,
            empty: pageData.empty || true,
            numberOfElements: pageData.numberOfElements || 0
        };
    }

    // Fallback for single object or array response
    const data = Array.isArray(response.data) ? response.data : [response.data];
    return {
        content: data,
        totalPages: 1,
        totalElements: data.length,
        size: data.length,
        number: 0,
        first: true,
        last: true,
        empty: data.length === 0,
        numberOfElements: data.length
    };
};

/**
 * Validate role data
 * @param {Object} roleData - Role data to validate
 * @returns {Array} Array of validation errors
 */
export const validateRoleData = (roleData) => {
    const errors = [];
    
    if (!roleData.roleName) errors.push('Role name is required');
    if (!roleData.roleCode) errors.push('Role code is required');
    
    // Validate role code format (alphanumeric with underscores)
    if (roleData.roleCode && !/^[A-Z_]+$/.test(roleData.roleCode)) {
        errors.push('Role code must contain only uppercase letters and underscores');
    }
    
    // Validate role name length
    if (roleData.roleName && roleData.roleName.length > 100) {
        errors.push('Role name cannot exceed 100 characters');
    }
    
    // Validate description length if provided
    if (roleData.description && roleData.description.length > 500) {
        errors.push('Description cannot exceed 500 characters');
    }
    
    return errors;
};

/**
 * Build pagination parameters for roles
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildRolePaginationParams = (page = 0, size = 10, sortField = 'roleName', sortDirection = 'ASC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build AppRole DTO
 * @param {Object} roleData - Role data
 * @returns {Object} AppRole DTO
 */
export const buildAppRoleDTO = (roleData) => {
    const {
        roleCode,
        roleName,
        description = '',
        status = 'ACTIVE',
        permissions = []
    } = roleData;
    
    return {
        roleCode: roleCode.toUpperCase(),
        roleName,
        description,
        status,
        permissions: Array.isArray(permissions) ? permissions : []
    };
};

/**
 * Build bulk role creation request
 * @param {Array} roles - Array of role data objects
 * @returns {Array} Array of AppRole DTOs
 */
export const buildBulkRolesRequest = (roles) => {
    if (!Array.isArray(roles)) {
        throw new Error('Roles must be an array');
    }
    
    return roles.map(role => buildAppRoleDTO(role));
};

/**
 * Extract role statistics from response
 * @param {Object} response - API response
 * @returns {Object} Role statistics
 */
export const extractRoleStatistics = (response) => {
    if (!response.data) return null;
    
    const data = response.data;
    return {
        totalRoles: data.totalRoles || 0,
        activeRoles: data.activeRoles || 0,
        inactiveRoles: data.inactiveRoles || 0,
        rolesWithPermissions: data.rolesWithPermissions || 0,
        rolesWithoutPermissions: data.rolesWithoutPermissions || 0,
        mostCommonPermissions: data.mostCommonPermissions || []
    };
};

/**
 * Check if user has role
 * @param {Array} userRoles - User's roles
 * @param {string} roleCode - Role code to check
 * @returns {boolean} True if user has the role
 */
export const hasRole = (userRoles, roleCode) => {
    if (!userRoles || !Array.isArray(userRoles)) return false;
    return userRoles.some(role => 
        role.roleCode === roleCode || 
        (typeof role === 'string' && role === roleCode)
    );
};

/**
 * Check if user has any of the specified roles
 * @param {Array} userRoles - User's roles
 * @param {Array} roleCodes - Array of role codes to check
 * @returns {boolean} True if user has any of the roles
 */
export const hasAnyRole = (userRoles, roleCodes) => {
    if (!userRoles || !Array.isArray(userRoles) || !Array.isArray(roleCodes)) return false;
    return userRoles.some(userRole => 
        roleCodes.some(roleCode => 
            (typeof userRole === 'object' && userRole.roleCode === roleCode) ||
            (typeof userRole === 'string' && userRole === roleCode)
        )
    );
};

/**
 * Check if user has all of the specified roles
 * @param {Array} userRoles - User's roles
 * @param {Array} roleCodes - Array of role codes to check
 * @returns {boolean} True if user has all of the roles
 */
export const hasAllRoles = (userRoles, roleCodes) => {
    if (!userRoles || !Array.isArray(userRoles) || !Array.isArray(roleCodes)) return false;
    
    const userRoleCodes = userRoles.map(role => 
        typeof role === 'object' ? role.roleCode : role
    );
    
    return roleCodes.every(roleCode => userRoleCodes.includes(roleCode));
};

/**
 * Get role codes from user roles
 * @param {Array} userRoles - User's roles
 * @returns {Array} Array of role codes
 */
export const getUserRoleCodes = (userRoles) => {
    if (!userRoles || !Array.isArray(userRoles)) return [];
    return userRoles.map(role => 
        typeof role === 'object' ? role.roleCode : role
    );
};
