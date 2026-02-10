// controllers/AuditController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for pagination and search
const buildAuditQueryParams = (params = {}) => {
    const queryParams = new URLSearchParams();
    
    // Map frontend parameters to backend parameters
    if (params.page !== undefined) queryParams.append('page', params.page);
    if (params.size !== undefined) queryParams.append('size', params.size);
    
    // Handle sort parameters
    if (params.sort && params.direction) {
        queryParams.append('sort', `${params.sort},${params.direction}`);
    } else if (params.sort) {
        queryParams.append('sort', params.sort);
    }
    
    // Search parameters (for GET /search endpoint)
    if (params.userId !== undefined) queryParams.append('userId', params.userId);
    if (params.action !== undefined) queryParams.append('action', params.action);
    if (params.operation !== undefined) queryParams.append('operation', params.operation);
    if (params.details !== undefined) queryParams.append('details', params.details);
    if (params.startDate !== undefined) queryParams.append('startDate', params.startDate);
    if (params.endDate !== undefined) queryParams.append('endDate', params.endDate);
    
    return queryParams.toString();
};

// Helper to format date for search
const formatDateForSearch = (date) => {
    if (!date) return '';
    
    if (date instanceof Date) {
        // Format: YYYY-MM-DDTHH:mm:ss
        const pad = (num) => num.toString().padStart(2, '0');
        const year = date.getFullYear();
        const month = pad(date.getMonth() + 1);
        const day = pad(date.getDate());
        const hours = pad(date.getHours());
        const minutes = pad(date.getMinutes());
        const seconds = pad(date.getSeconds());
        
        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    }
    
    // If it's already a string, assume it's in correct format
    return date;
};

// ============ AUDIT LOG MANAGEMENT METHODS ============

/**
 * Create a new audit log
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} auditData - AuditLog DTO matching backend structure
 * @returns {Promise} API response
 */
export const logAction = async (authorizationHeader, auditData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/audit`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(auditData)
        })
    );
};

/**
 * Alternative method to create audit log
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} auditData - AuditLog DTO
 * @returns {Promise} API response
 */
export const createAuditLog = async (authorizationHeader, auditData) => {
    return logAction(authorizationHeader, auditData);
};

/**
 * Get all audit logs (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field (default: 'createdAt')
 * @param {string} pagination.direction - Sort direction (ASC/DESC, default: 'DESC')
 * @returns {Promise} API response
 */
export const getAllAuditLogs = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 10, sort = 'createdAt', direction = 'DESC' } = pagination;
    
    const queryString = buildAuditQueryParams({
        page,
        size,
        sort,
        direction
    });
    
    const url = `/audit${queryString ? `?${queryString}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get audit logs
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAuditLogs = async (authorizationHeader) => {
    return getAllAuditLogs(authorizationHeader);
};

/**
 * Search audit logs with filters using POST method (for complex searches)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchCriteria - Search criteria
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchAuditLogs = async (authorizationHeader, searchCriteria = {}, pagination = {}) => {
    const { page = 0, size = 10, sort = 'createdAt', direction = 'DESC' } = pagination;
    
    // Prepare search request body
    const searchRequest = {
        ...searchCriteria,
        // Format dates if provided
        startDate: searchCriteria.startDate ? formatDateForSearch(searchCriteria.startDate) : undefined,
        endDate: searchCriteria.endDate ? formatDateForSearch(searchCriteria.endDate) : undefined
    };
    
    // Clean up undefined/null values
    Object.keys(searchRequest).forEach(key => {
        if (searchRequest[key] === undefined || searchRequest[key] === null || searchRequest[key] === '') {
            delete searchRequest[key];
        }
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/audit/search`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(searchRequest)
        })
    );
};

/**
 * Search audit logs with filters using GET method (for simple searches)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchCriteria - Search criteria
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchAuditLogsByQuery = async (authorizationHeader, searchCriteria = {}, pagination = {}) => {
    const { page = 0, size = 10, sort = 'createdAt', direction = 'DESC' } = pagination;
    
    // Build query parameters including search criteria
    const queryParams = {
        page,
        size,
        sort,
        direction,
        ...searchCriteria
    };
    
    // Format dates if provided
    if (searchCriteria.startDate) {
        queryParams.startDate = formatDateForSearch(searchCriteria.startDate);
    }
    if (searchCriteria.endDate) {
        queryParams.endDate = formatDateForSearch(searchCriteria.endDate);
    }
    
    // Clean up undefined/null values
    Object.keys(queryParams).forEach(key => {
        if (queryParams[key] === undefined || queryParams[key] === null || queryParams[key] === '') {
            delete queryParams[key];
        }
    });
    
    const queryString = buildAuditQueryParams(queryParams);
    const url = `/audit/search${queryString ? `?${queryString}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Advanced search with date range and multiple criteria
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} options - Search options
 * @returns {Promise} API response
 */
export const advancedAuditLogSearch = async (authorizationHeader, options = {}) => {
    const {
        userId,
        action,
        operation,
        details,
        startDate,
        endDate,
        auditId,
        usePostMethod = true, // Use POST for complex searches, GET for simple
        page = 0,
        size = 10,
        sort = 'createdAt',
        direction = 'DESC'
    } = options;
    
    const searchCriteria = {
        userId,
        action,
        operation,
        details,
        startDate,
        endDate,
        auditId
    };
    
    const pagination = { page, size, sort, direction };
    
    if (usePostMethod) {
        return searchAuditLogs(authorizationHeader, searchCriteria, pagination);
    } else {
        return searchAuditLogsByQuery(authorizationHeader, searchCriteria, pagination);
    }
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for audit operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleAuditResponse = (response) => {
    if (!response) {
        throw new Error('No response received from audit service');
    }

    // Handle backend response structure
    const responseCode = response.responseCode || response.status;
    
    if (responseCode === 200 || responseCode === 201 || responseCode === 204) {
        // For 204, ensure we return proper structure
        if (responseCode === 204) {
            return {
                message: response.message || 'No audit logs found',
                data: [],
                responseCode: responseCode,
                pagination: response.pagination,
                searchCriteria: response.searchCriteria,
                requestId: response.requestId
            };
        }
        
        return {
            ...response,
            data: response.data || [],
            responseCode: responseCode,
            searchCriteria: response.searchCriteria || {}
        };
    }

    // Handle error responses
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
        case 500: 
            throw new Error(`Server Error: ${errorMessage}`);
        default: 
            throw new Error(`Error ${responseCode || 'Unknown'}: ${errorMessage}`);
    }
};

/**
 * Extract pagination info from audit response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractAuditPaginationInfo = (response) => {
    if (!response) return null;

    // Check if response has pagination metadata from backend
    if (response.pagination) {
        return {
            content: response.data || [],
            totalPages: response.pagination.total_pages || 0,
            totalElements: response.pagination.total_elements || 0,
            size: response.pagination.page_size || 0,
            number: response.pagination.page_number || 0,
            first: response.pagination.is_first || false,
            last: response.pagination.is_last || false,
            empty: !response.data || response.data.length === 0,
            numberOfElements: response.data ? response.data.length : 0,
            searchCriteria: response.searchCriteria || {}
        };
    }

    // Fallback for non-paginated responses
    const data = response.data ? (Array.isArray(response.data) ? response.data : [response.data]) : [];
    return {
        content: data,
        totalPages: 1,
        totalElements: data.length,
        size: data.length,
        number: 0,
        first: true,
        last: true,
        empty: data.length === 0,
        numberOfElements: data.length,
        searchCriteria: response.searchCriteria || {}
    };
};

/**
 * Validate audit log data according to backend DTO
 * @param {Object} auditData - Audit data to validate
 * @returns {Array} Array of validation errors
 */
export const validateAuditData = (auditData) => {
    const errors = [];
    
    // Required fields based on Java DTO
    if (!auditData.userId) errors.push('User ID cannot be null');
    if (!auditData.action || auditData.action.trim() === '') errors.push('Action cannot be blank');
    if (!auditData.operation || auditData.operation.trim() === '') errors.push('Operation cannot be blank');
    
    // Optional: Validate action format if needed
    const validActions = ['CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'EXPORT', 'IMPORT', 'APPROVE', 'REJECT'];
    if (auditData.action && !validActions.includes(auditData.action.toUpperCase())) {
        errors.push(`Action should be one of: ${validActions.join(', ')}`);
    }
    
    // Validate details length if provided
    if (auditData.details && auditData.details.length > 1000) {
        errors.push('Details cannot exceed 1000 characters');
    }
    
    return errors;
};

/**
 * Validate search criteria
 * @param {Object} searchCriteria - Search criteria to validate
 * @returns {Array} Array of validation errors
 */
export const validateSearchCriteria = (searchCriteria) => {
    const errors = [];
    
    // Validate date range
    if (searchCriteria.startDate && searchCriteria.endDate) {
        const start = new Date(searchCriteria.startDate);
        const end = new Date(searchCriteria.endDate);
        
        if (start > end) {
            errors.push('Start date cannot be after end date');
        }
    }
    
    // Validate user ID format (if provided)
    if (searchCriteria.userId) {
        const userIdRegex = /^[a-zA-Z0-9._@-]*$/;
        if (!userIdRegex.test(searchCriteria.userId)) {
            errors.push('User ID can only contain alphanumeric characters, dots, underscores, @, and hyphens');
        }
    }
    
    // Validate action format (if provided)
    if (searchCriteria.action) {
        const actionRegex = /^[a-zA-Z0-9_]*$/;
        if (!actionRegex.test(searchCriteria.action)) {
            errors.push('Action can only contain alphanumeric characters and underscores');
        }
    }
    
    // Validate operation format (if provided)
    if (searchCriteria.operation) {
        const operationRegex = /^[a-zA-Z0-9_]*$/;
        if (!operationRegex.test(searchCriteria.operation)) {
            errors.push('Operation can only contain alphanumeric characters and underscores');
        }
    }
    
    return errors;
};

/**
 * Build pagination parameters for audit logs
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by (default: 'createdAt')
 * @param {string} sortDirection - Sort direction (ASC/DESC, default: 'DESC')
 * @returns {Object} Pagination parameters
 */
export const buildAuditPaginationParams = (page = 0, size = 10, sortField = 'createdAt', sortDirection = 'DESC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build search criteria object
 * @param {Object} criteria - Search criteria
 * @returns {Object} Search criteria object
 */
export const buildSearchCriteria = (criteria = {}) => {
    return {
        userId: criteria.userId || '',
        action: criteria.action || '',
        operation: criteria.operation || '',
        details: criteria.details || '',
        startDate: criteria.startDate || '',
        endDate: criteria.endDate || '',
        auditId: criteria.auditId || ''
    };
};

/**
 * Build AuditLog DTO matching backend structure
 * @param {Object} auditData - Audit data
 * @returns {Object} AuditLog DTO matching Java AuditLogDTO
 */
export const buildAuditLogDTO = (auditData) => {
    const {
        userId,
        action,
        operation,
        details = '',
        timestamp = new Date().toISOString()
    } = auditData;
    
    return {
        userId,
        action: action.toUpperCase(),
        operation,
        details,
        timestamp
    };
};

/**
 * Create a standardized audit log for common actions
 * @param {string} userId - User ID
 * @param {string} action - Action type
 * @param {string} operation - Operation name (replaces 'module' from old structure)
 * @param {string} entityId - Entity ID (for details)
 * @param {string} entityType - Entity type (for details)
 * @param {Object} options - Additional options
 * @returns {Object} AuditLog DTO matching backend
 */
export const createStandardAuditLog = (userId, action, operation, entityId, entityType, options = {}) => {
    const {
        description = '',
        oldValue = null,
        newValue = null,
        status = 'SUCCESS',
        metadata = {}
    } = options;
    
    // Build details string
    let details = description;
    if (!details && entityId && entityType) {
        details = `${action} ${entityType} ${entityId}`;
        if (status && status !== 'SUCCESS') {
            details += ` - ${status}`;
        }
    }
    
    return buildAuditLogDTO({
        userId,
        action,
        operation,
        details
    });
};

/**
 * Create audit log for user login
 * @param {string} userId - User ID
 * @param {string} ipAddress - IP address (for details)
 * @param {string} status - Login status
 * @returns {Object} AuditLog DTO
 */
export const createLoginAuditLog = (userId, ipAddress = '', status = 'SUCCESS') => {
    let details = `User login attempt - ${status}`;
    if (ipAddress) {
        details += ` from IP: ${ipAddress}`;
    }
    
    return buildAuditLogDTO({
        userId,
        action: 'LOGIN',
        operation: 'AUTHENTICATION',
        details
    });
};

/**
 * Create audit log for user logout
 * @param {string} userId - User ID
 * @returns {Object} AuditLog DTO
 */
export const createLogoutAuditLog = (userId) => {
    return buildAuditLogDTO({
        userId,
        action: 'LOGOUT',
        operation: 'AUTHENTICATION',
        details: 'User logged out'
    });
};

/**
 * Create audit log for create operation
 * @param {string} userId - User ID
 * @param {string} entityId - Entity ID
 * @param {string} entityType - Entity type
 * @param {Object} newValue - New value/entity data
 * @param {string} operation - Operation name
 * @returns {Object} AuditLog DTO
 */
export const createCreateAuditLog = (userId, entityId, entityType, newValue, operation = 'DEFAULT') => {
    let details = `Created ${entityType} ${entityId}`;
    if (newValue) {
        details += ` with data: ${JSON.stringify(newValue).substring(0, 200)}...`;
    }
    
    return buildAuditLogDTO({
        userId,
        action: 'CREATE',
        operation,
        details
    });
};

/**
 * Create audit log for update operation
 * @param {string} userId - User ID
 * @param {string} entityId - Entity ID
 * @param {string} entityType - Entity type
 * @param {Object} oldValue - Old value/entity data
 * @param {Object} newValue - New value/entity data
 * @param {string} operation - Operation name
 * @returns {Object} AuditLog DTO
 */
export const createUpdateAuditLog = (userId, entityId, entityType, oldValue, newValue, operation = 'DEFAULT') => {
    let details = `Updated ${entityType} ${entityId}`;
    if (oldValue && newValue) {
        details += ` - Changed from: ${JSON.stringify(oldValue).substring(0, 100)} to: ${JSON.stringify(newValue).substring(0, 100)}...`;
    }
    
    return buildAuditLogDTO({
        userId,
        action: 'UPDATE',
        operation,
        details
    });
};

/**
 * Create audit log for delete operation
 * @param {string} userId - User ID
 * @param {string} entityId - Entity ID
 * @param {string} entityType - Entity type
 * @param {Object} oldValue - Old value/entity data
 * @param {string} operation - Operation name
 * @returns {Object} AuditLog DTO
 */
export const createDeleteAuditLog = (userId, entityId, entityType, oldValue, operation = 'DEFAULT') => {
    let details = `Deleted ${entityType} ${entityId}`;
    if (oldValue) {
        details += ` - Previous data: ${JSON.stringify(oldValue).substring(0, 200)}...`;
    }
    
    return buildAuditLogDTO({
        userId,
        action: 'DELETE',
        operation,
        details
    });
};

/**
 * Get client IP address (for browser environments)
 * @returns {string} Client IP address or empty string
 */
export const getClientIpAddress = async () => {
    try {
        // For browser environments, we can try to get the IP
        if (typeof window !== 'undefined') {
            // Try to get IP from external service
            const response = await fetch('https://api.ipify.org?format=json');
            if (response.ok) {
                const data = await response.json();
                return data.ip || '';
            }
        }
        return '';
    } catch (error) {
        console.warn('Could not get client IP address:', error);
        return '';
    }
};

/**
 * Get user agent string (for browser environments)
 * @returns {string} User agent string
 */
export const getUserAgent = () => {
    if (typeof window !== 'undefined' && window.navigator) {
        return window.navigator.userAgent;
    }
    return '';
};

/**
 * Format date for display
 * @param {string|Date} date - Date to format
 * @returns {string} Formatted date string
 */
export const formatDateForDisplay = (date) => {
    if (!date) return '';
    
    const d = new Date(date);
    return d.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
};