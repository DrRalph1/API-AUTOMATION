// controllers/OperationController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for search/filter
const buildOperationQueryParams = (params = {}) => {
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

// ============ OPERATION MANAGEMENT METHODS ============

/**
 * Create a new operation
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} operationData - Operation DTO
 * @returns {Promise} API response
 */
export const createOperation = async (authorizationHeader, operationData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/operations`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(operationData)
        })
    );
};

/**
 * Create multiple operations in bulk
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} operationsData - Array of Operation DTOs
 * @returns {Promise} API response
 */
export const createOperationsBulk = async (authorizationHeader, operationsData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/operations/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(operationsData)
        })
    );
};

/**
 * Get a single operation by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getOperationById = async (authorizationHeader, operationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/operations/${operationId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};



/**
 * Get a single operation by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} integrationId - integration ID (UUID)
 * @returns {Promise} API response
 */
export const getOperationsByIntegrationId = async (authorizationHeader, integrationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/operations/integration/${integrationId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};



/**
 * Alternative method to get operation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getOperation = async (authorizationHeader, operationId) => {
    return getOperationById(authorizationHeader, operationId);
};

/**
 * Get all operations (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllOperations = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 100, sort = 'operationName', direction = 'ASC' } = pagination;
    
    const queryParams = buildOperationQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/operations${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get all operations
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getOperations = async (authorizationHeader) => {
    return getAllOperations(authorizationHeader);
};

/**
 * Search operations with filters (code, name, method, status, integration)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.operationCode - Operation code filter
 * @param {string} filters.operationName - Operation name filter
 * @param {string} filters.httpMethod - HTTP method filter
 * @param {string} filters.status - Status filter
 * @param {string} filters.integrationId - Integration ID filter
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
// In OperationController.js - UPDATED VERSION
export const searchOperations = async (authorizationHeader, filters = {}, pagination = {}) => {
    const { page = 0, size = 10, sort = 'operationName', direction = 'ASC' } = pagination;
    
    // Build query parameters from filters
    const queryParams = new URLSearchParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    // Add filters to query params
    Object.entries(filters).forEach(([key, value]) => {
        if (value && value !== 'all' && value !== '') {
            queryParams.append(key, value);
        }
    });
    
    // Try different endpoints based on what your backend supports
    const url = `/operations?${queryParams.toString()}`;
    // OR if you have a search endpoint:
    // const url = `/operations/search?${queryParams.toString()}`;
    
    console.log('🔍 Controller: Searching operations URL:', url);
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Update an operation by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @param {Object} operationData - Updated operation data
 * @returns {Promise} API response
 */
export const updateOperation = async (authorizationHeader, operationId, operationData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/operations/${operationId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(operationData)
        })
    );
};

/**
 * Delete an operation by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const deleteOperation = async (authorizationHeader, operationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/operations/${operationId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for operation operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleOperationResponse = (response) => {
    if (!response) {
        throw new Error('No response received from operation service');
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
            return { message: 'No operations found', data: [] };
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Operation not found: ${response.message}`);
        case 409: throw new Error(`Conflict: ${response.message}`);
        case 422: throw new Error(`Business rule violation: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

/**
 * Extract pagination info from operation response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractOperationPaginationInfo = (response) => {
    if (!response.data) return null;

    // Check if response has pagination metadata
    if (response.pagination) {
        return {
            content: response.data || [],
            totalPages: response.pagination.total_pages || 0,
            totalElements: response.pagination.total_elements || 0,
            size: response.pagination.page_size || 0,
            number: response.pagination.page_number || 0,
            first: response.pagination.is_first || false,
            last: response.pagination.is_last || false,
            empty: (response.data || []).length === 0,
            numberOfElements: (response.data || []).length
        };
    }

    // Fallback for non-paginated responses
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
 * Validate operation data
 * @param {Object} operationData - Operation data to validate
 * @returns {Array} Array of validation errors
 */
export const validateOperationData = (operationData) => {
    const errors = [];
    
    if (!operationData.operationCode) errors.push('Operation code is required');
    if (!operationData.operationName) errors.push('Operation name is required');
    if (!operationData.httpMethod) errors.push('HTTP method is required');
    if (!operationData.endpoint) errors.push('Endpoint is required');
    if (!operationData.integrationId) errors.push('Integration ID is required');
    
    // Validate operation code format (alphanumeric with underscores)
    if (operationData.operationCode && !/^[A-Z_][A-Z0-9_]*$/.test(operationData.operationCode)) {
        errors.push('Operation code must start with an uppercase letter or underscore and contain only uppercase letters, numbers, and underscores');
    }
    
    // Validate HTTP method
    const validMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS', 'HEAD'];
    if (operationData.httpMethod && !validMethods.includes(operationData.httpMethod.toUpperCase())) {
        errors.push(`Invalid HTTP method. Must be one of: ${validMethods.join(', ')}`);
    }
    
    // Validate endpoint format (should start with /)
    if (operationData.endpoint && !operationData.endpoint.startsWith('/')) {
        errors.push('Endpoint must start with a forward slash (/)');
    }
    
    // Validate operation type if provided
    if (operationData.operationType) {
        const validTypes = ['READ', 'WRITE', 'UPDATE', 'DELETE', 'BATCH', 'STREAM'];
        if (!validTypes.includes(operationData.operationType.toUpperCase())) {
            errors.push(`Invalid operation type. Must be one of: ${validTypes.join(', ')}`);
        }
    }
    
    // Validate timeout if provided
    if (operationData.timeoutMs && (isNaN(operationData.timeoutMs) || operationData.timeoutMs < 1000)) {
        errors.push('Timeout must be at least 1000 milliseconds');
    }
    
    // Validate description length if provided
    if (operationData.description && operationData.description.length > 1000) {
        errors.push('Description cannot exceed 1000 characters');
    }
    
    return errors;
};

/**
 * Build pagination parameters for operations
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildOperationPaginationParams = (page = 0, size = 10, sortField = 'operationName', sortDirection = 'ASC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build search filter parameters for operations
 * @param {Object} filters - Filter criteria
 * @returns {Object} Filter parameters
 */
export const buildOperationSearchFilters = (filters = {}) => {
    const {
        operationCode = '',
        operationName = '',
        httpMethod = '',
        status = '',
        integrationId = ''
    } = filters;
    
    const searchFilters = {};
    
    if (operationCode) searchFilters.operationCode = operationCode;
    if (operationName) searchFilters.operationName = operationName;
    if (httpMethod) searchFilters.httpMethod = httpMethod;
    if (status) searchFilters.status = status;
    if (integrationId) searchFilters.integrationId = integrationId;
    
    return searchFilters;
};

/**
 * Build Operation DTO
 * @param {Object} operationData - Operation data
 * @returns {Object} Operation DTO
 */
export const buildOperationDTO = (operationData) => {
    const {
        operationCode,
        operationName,
        httpMethod,
        endpoint,
        integrationId,
        description = '',
        operationType = 'READ',
        requiresAuthentication = true,
        timeoutMs = 30000,
        retryCount = 3,
        retryDelayMs = 1000,
        requestBodyTemplate = null,
        responseBodyTemplate = null,
        headersTemplate = null,
        queryParamsTemplate = null,
        status = 'ACTIVE',
        metadata = {}
    } = operationData;
    
    return {
        operationCode: operationCode.toUpperCase(),
        operationName,
        httpMethod: httpMethod.toUpperCase(),
        endpoint,
        integrationId,
        description,
        operationType: operationType.toUpperCase(),
        requiresAuthentication,
        timeoutMs,
        retryCount,
        retryDelayMs,
        requestBodyTemplate,
        responseBodyTemplate,
        headersTemplate,
        queryParamsTemplate,
        status: status.toUpperCase(),
        metadata
    };
};

/**
 * Build bulk operations request
 * @param {Array} operations - Array of operation data objects
 * @returns {Array} Array of Operation DTOs
 */
export const buildBulkOperationsRequest = (operations) => {
    if (!Array.isArray(operations)) {
        throw new Error('Operations must be an array');
    }
    
    return operations.map(operation => buildOperationDTO(operation));
};

/**
 * Get operation configuration for execution
 * @param {Object} operation - Operation data
 * @param {Object} integration - Integration data
 * @returns {Object} Operation execution configuration
 */
export const getOperationExecutionConfig = (operation, integration) => {
    const {
        httpMethod,
        endpoint,
        requiresAuthentication,
        timeoutMs,
        retryCount,
        retryDelayMs,
        headersTemplate,
        queryParamsTemplate,
        requestBodyTemplate
    } = operation;
    
    const { baseUrl, authType } = integration;
    
    const config = {
        url: `${baseUrl}${endpoint}`,
        method: httpMethod,
        timeout: timeoutMs,
        retryCount,
        retryDelay: retryDelayMs,
        requiresAuth: requiresAuthentication,
        authType
    };
    
    // Add headers template if available
    if (headersTemplate) {
        try {
            config.headers = typeof headersTemplate === 'string' ? 
                JSON.parse(headersTemplate) : headersTemplate;
        } catch (error) {
            console.warn('Failed to parse headers template:', error);
        }
    }
    
    // Add query params template if available
    if (queryParamsTemplate) {
        try {
            config.queryParams = typeof queryParamsTemplate === 'string' ? 
                JSON.parse(queryParamsTemplate) : queryParamsTemplate;
        } catch (error) {
            console.warn('Failed to parse query params template:', error);
        }
    }
    
    // Add request body template if available
    if (requestBodyTemplate && ['POST', 'PUT', 'PATCH'].includes(httpMethod.toUpperCase())) {
        try {
            config.requestBody = typeof requestBodyTemplate === 'string' ? 
                JSON.parse(requestBodyTemplate) : requestBodyTemplate;
        } catch (error) {
            console.warn('Failed to parse request body template:', error);
        }
    }
    
    return config;
};

/**
 * Prepare operation request
 * @param {Object} operation - Operation configuration
 * @param {Object} integration - Integration configuration
 * @param {Object} requestData - Request data (overrides templates)
 * @returns {Object} Prepared request configuration
 */
export const prepareOperationRequest = (operation, integration, requestData = {}) => {
    const executionConfig = getOperationExecutionConfig(operation, integration);
    
    const {
        headers = {},
        params = {},
        data = null,
        timeout = null
    } = requestData;
    
    // Merge template headers with provided headers
    const finalHeaders = {
        'Content-Type': 'application/json',
        ...(executionConfig.headers || {}),
        ...headers
    };
    
    // Merge template query params with provided params
    const finalParams = {
        ...(executionConfig.queryParams || {}),
        ...params
    };
    
    // Use provided data or template data
    const finalData = data || executionConfig.requestBody;
    
    const requestConfig = {
        url: executionConfig.url,
        method: executionConfig.method,
        headers: finalHeaders,
        params: finalParams,
        timeout: timeout || executionConfig.timeout,
        retryCount: executionConfig.retryCount,
        retryDelay: executionConfig.retryDelay
    };
    
    if (finalData && ['POST', 'PUT', 'PATCH'].includes(executionConfig.method.toUpperCase())) {
        requestConfig.data = typeof finalData === 'string' ? finalData : JSON.stringify(finalData);
    }
    
    return requestConfig;
};

/**
 * Execute operation
 * @param {Object} operation - Operation configuration
 * @param {Object} integration - Integration configuration
 * @param {Object} requestData - Request data
 * @returns {Promise} API response
 */
export const executeOperation = async (operation, integration, requestData = {}) => {
    const requestConfig = prepareOperationRequest(operation, integration, requestData);
    
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), requestConfig.timeout);
        
        const fetchOptions = {
            method: requestConfig.method,
            headers: requestConfig.headers,
            signal: controller.signal
        };
        
        if (requestConfig.data) {
            fetchOptions.body = requestConfig.data;
        }
        
        // Add query parameters to URL
        let url = requestConfig.url;
        if (requestConfig.params && Object.keys(requestConfig.params).length > 0) {
            const queryParams = new URLSearchParams(requestConfig.params).toString();
            url += `${url.includes('?') ? '&' : '?'}${queryParams}`;
        }
        
        const response = await fetch(url, fetchOptions);
        clearTimeout(timeoutId);
        
        const responseData = await response.json().catch(() => response.text());
        
        return {
            success: response.ok,
            status: response.status,
            statusText: response.statusText,
            data: responseData,
            headers: Object.fromEntries(response.headers.entries()),
            timestamp: new Date().toISOString(),
            operationId: operation.operationId,
            operationName: operation.operationName
        };
    } catch (error) {
        return {
            success: false,
            error: error.message,
            timestamp: new Date().toISOString(),
            operationId: operation.operationId,
            operationName: operation.operationName
        };
    }
};

/**
 * Validate operation response against template
 * @param {Object} response - API response
 * @param {Object} responseBodyTemplate - Response body template
 * @returns {Object} Validation result { isValid: boolean, errors: Array }
 */
export const validateOperationResponse = (response, responseBodyTemplate) => {
    const errors = [];
    
    if (!responseBodyTemplate) {
        return { isValid: true, errors: [] };
    }
    
    try {
        const template = typeof responseBodyTemplate === 'string' ? 
            JSON.parse(responseBodyTemplate) : responseBodyTemplate;
        
        const responseData = response.data;
        
        // Simple validation: check if all required fields from template exist in response
        if (template.requiredFields && Array.isArray(template.requiredFields)) {
            template.requiredFields.forEach(field => {
                if (responseData[field] === undefined) {
                    errors.push(`Missing required field: ${field}`);
                }
            });
        }
        
        // Validate data types if schema is provided
        if (template.schema) {
            // This is a simplified validation - in production, use a proper schema validator
            Object.keys(template.schema).forEach(field => {
                const fieldType = template.schema[field];
                const fieldValue = responseData[field];
                
                if (fieldValue !== undefined) {
                    const actualType = typeof fieldValue;
                    if (fieldType === 'string' && actualType !== 'string') {
                        errors.push(`Field ${field} should be string but got ${actualType}`);
                    } else if (fieldType === 'number' && actualType !== 'number') {
                        errors.push(`Field ${field} should be number but got ${actualType}`);
                    } else if (fieldType === 'boolean' && actualType !== 'boolean') {
                        errors.push(`Field ${field} should be boolean but got ${actualType}`);
                    } else if (fieldType === 'object' && (actualType !== 'object' || Array.isArray(fieldValue))) {
                        errors.push(`Field ${field} should be object but got ${actualType}`);
                    } else if (fieldType === 'array' && !Array.isArray(fieldValue)) {
                        errors.push(`Field ${field} should be array but got ${actualType}`);
                    }
                }
            });
        }
        
    } catch (error) {
        errors.push(`Failed to validate response: ${error.message}`);
    }
    
    return {
        isValid: errors.length === 0,
        errors
    };
};

/**
 * Extract operation statistics from response
 * @param {Object} response - API response
 * @returns {Object} Operation statistics
 */
export const extractOperationStatistics = (response) => {
    if (!response.data) return null;
    
    const data = response.data;
    return {
        totalOperations: data.totalOperations || 0,
        activeOperations: data.activeOperations || 0,
        inactiveOperations: data.inactiveOperations || 0,
        operationsByMethod: data.operationsByMethod || {},
        operationsByType: data.operationsByType || {},
        operationsByStatus: data.operationsByStatus || {},
        operationsByIntegration: data.operationsByIntegration || {},
        mostExecutedOperations: data.mostExecutedOperations || [],
        averageExecutionTime: data.averageExecutionTime || 0,
        successRate: data.successRate || 0
    };
};

/**
 * Get operation status badge configuration
 * @param {string} status - Operation status
 * @returns {Object} Badge configuration { color: string, label: string }
 */
export const getOperationStatusBadge = (status) => {
    const statusMap = {
        'ACTIVE': { color: 'success', label: 'Active' },
        'INACTIVE': { color: 'secondary', label: 'Inactive' },
        'DRAFT': { color: 'warning', label: 'Draft' },
        'DEPRECATED': { color: 'error', label: 'Deprecated' },
        'MAINTENANCE': { color: 'info', label: 'Maintenance' }
    };
    
    return statusMap[status?.toUpperCase()] || { color: 'default', label: status || 'Unknown' };
};