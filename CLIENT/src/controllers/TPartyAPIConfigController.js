// controllers/TPartyAPIController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for search/filter
const buildEndpointQueryParams = (params = {}) => {
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

// ============ API ENDPOINT MANAGEMENT METHODS ============

/**
 * Create a new API endpoint
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} endpointData - API endpoint DTO
 * @returns {Promise} API response
 */
export const createTPartyAPI = async (authorizationHeader, endpointData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(endpointData)
        })
    );
};

/**
 * Create multiple API endpoints in bulk
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} endpointsData - Array of API endpoint DTOs
 * @returns {Promise} API response
 */
export const createTPartyAPIBulk = async (authorizationHeader, endpointsData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(endpointsData)
        })
    );
};

/**
 * Get a single API endpoint by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} endpointId - Endpoint ID
 * @returns {Promise} API response
 */
export const getTPartyAPIById = async (authorizationHeader, endpointId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/${endpointId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoint by code
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiCode - API code
 * @returns {Promise} API response
 */
export const getTPartyAPIByCode = async (authorizationHeader, apiCode) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/by-code/${apiCode}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoint by code and environment
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiCode - API code
 * @param {string} environment - Environment (Development, Staging, Production)
 * @returns {Promise} API response
 */
export const getTPartyAPIByCodeAndEnvironment = async (authorizationHeader, apiCode, environment) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/by-code-environment/${apiCode}/${environment}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoint by operation ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getTPartyAPIByOperationId = async (authorizationHeader, operationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/by-operation/${operationId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get all API endpoints (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllTPartyAPI = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 10, sort = 'apiName', direction = 'ASC' } = pagination;
    
    const queryParams = buildEndpointQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/tparty-api-config${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Search API endpoints with filters
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.apiCode - API code filter
 * @param {string} filters.apiName - API name filter
 * @param {string} filters.apiUrl - API URL filter
 * @param {string} filters.httpMethod - HTTP method filter
 * @param {string} filters.authType - Authentication type filter
 * @param {string} filters.status - Status filter
 * @param {string} filters.environment - Environment filter
 * @param {string} filters.collectionName - Collection name filter
 * @param {string} filters.category - Category filter
 * @param {string} filters.requestBodyType - Request body type filter
 * @param {string} filters.operationId - Operation ID filter
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchTPartyAPI = async (authorizationHeader, filters = {}, pagination = {}) => {
    const page = Number.isInteger(pagination?.page) ? pagination.page : 0;
    const size = Number.isInteger(pagination?.size) ? pagination.size : 10;
    const sort = pagination?.sort || 'apiName';
    const direction = pagination?.direction || 'ASC';

    const queryParams = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort: `${sort},${direction}`
    });

    console.log("TParty API filters::::", filters);

    // filters must be a plain object
    if (filters && Object.prototype.toString.call(filters) === '[object Object]') {
        Object.entries(filters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, String(value));
            }
        });
    } else {
        console.warn('TParty API filters ignored (not a plain object):', filters);
    }

    const url = `/tparty-api-config/search?${queryParams.toString()}`;

    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get all active API endpoints
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllActiveEndpoints = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/active`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get all active API endpoints (with operation active status check)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllActiveEndpointsWithOperationCheck = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/active-with-operation-check`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoints by authentication type
 * @param {string} authorizationHeader - Bearer token
 * @param {string} authType - Authentication type
 * @returns {Promise} API response
 */
export const getEndpointsByAuthType = async (authorizationHeader, authType) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/auth-type/${authType}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoints by environment
 * @param {string} authorizationHeader - Bearer token
 * @param {string} environment - Environment (Development, Staging, Production)
 * @returns {Promise} API response
 */
export const getEndpointsByEnvironment = async (authorizationHeader, environment) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/environment/${environment}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoints by collection ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @returns {Promise} API response
 */
export const getEndpointsByCollection = async (authorizationHeader, collectionId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/collection/${collectionId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoints by category
 * @param {string} authorizationHeader - Bearer token
 * @param {string} category - Category name
 * @returns {Promise} API response
 */
export const getEndpointsByCategory = async (authorizationHeader, category) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/category/${category}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Execute an API endpoint
 * @param {string} authorizationHeader - Bearer token
 * @param {string} endpointId - Endpoint ID
 * @param {Object} requestData - Request data for execution
 * @returns {Promise} API response
 */
export const executeApi = async (authorizationHeader, endpointId, requestData = {}) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/${endpointId}/execute`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(requestData)
        })
    );
};

/**
 * Test API endpoint connectivity
 * @param {string} authorizationHeader - Bearer token
 * @param {string} endpointId - Endpoint ID
 * @returns {Promise} API response
 */
export const testEndpoint = async (authorizationHeader, endpointId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/${endpointId}/test`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Update an API endpoint by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} endpointId - Endpoint ID
 * @param {Object} endpointData - Updated endpoint data
 * @returns {Promise} API response
 */
export const updateTPartyAPI = async (authorizationHeader, endpointId, endpointData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/${endpointId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(endpointData)
        })
    );
};

/**
 * Delete an API endpoint by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} endpointId - Endpoint ID
 * @returns {Promise} API response
 */
export const deleteTPartyAPI = async (authorizationHeader, endpointId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/${endpointId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoint statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getEndpointStatistics = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/statistics`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get API endpoint statistics by environment
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getStatisticsByEnvironment = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/statistics/environment`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Bulk update environment for multiple endpoints
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} updateRequest - Update request containing oldEnvironment, newEnvironment, and optional endpointIds
 * @returns {Promise} API response
 */
export const bulkUpdateEnvironment = async (authorizationHeader, updateRequest) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/tparty-api-config/bulk-update-environment`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(updateRequest)
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for endpoint operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleEndpointResponse = (response) => {
    if (!response) {
        throw new Error('No response received from API endpoint service');
    }

    if (response.responseCode === 200 || response.responseCode === 201) {
        return response.data || response;
    }

    switch (response.responseCode) {
        case 204:
            return { message: 'No content found', data: [] };
        case 207:
            return response; // Partial success - return as-is
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Not Found: ${response.message}`);
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
export const extractEndpointPaginationInfo = (response) => {
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
 * Validate API endpoint data
 * @param {Object} endpointData - Endpoint data to validate
 * @returns {Array} Array of validation errors
 */
export const validateTPartyAPIData = (endpointData) => {
    const errors = [];
    
    if (!endpointData.apiCode) errors.push('API code is required');
    if (!endpointData.apiName) errors.push('API name is required');
    if (!endpointData.apiUrl) errors.push('API URL is required');
    if (!endpointData.httpMethod) errors.push('HTTP method is required');
    if (!endpointData.authType) errors.push('Authentication type is required');
    if (!endpointData.operationId) errors.push('Operation ID is required');
    if (!endpointData.requestBodyType) errors.push('Request body type is required');
    if (!endpointData.environment) errors.push('Environment is required');
    
    // Validate HTTP method
    const validMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'];
    if (endpointData.httpMethod && !validMethods.includes(endpointData.httpMethod.toUpperCase())) {
        errors.push(`Invalid HTTP method. Must be one of: ${validMethods.join(', ')}`);
    }
    
    // Validate auth type
    const validAuthTypes = ['API_KEY', 'BEARER_TOKEN', 'BASIC_AUTH', 'NONE'];
    if (endpointData.authType && !validAuthTypes.includes(endpointData.authType.toUpperCase())) {
        errors.push(`Invalid authentication type. Must be one of: ${validAuthTypes.join(', ')}`);
    }
    
    // Validate body type
    const validBodyTypes = ['NONE', 'RAW', 'FORM_DATA', 'X_WWW_FORM_URLENCODED', 'URL_ENCODED', 'BINARY', 'GRAPHQL'];
    if (endpointData.requestBodyType && !validBodyTypes.includes(endpointData.requestBodyType.toUpperCase())) {
        errors.push(`Invalid request body type. Must be one of: ${validBodyTypes.join(', ')}`);
    }
    
    // Validate environment
    const validEnvironments = ['DEVELOPMENT', 'STAGING', 'PRODUCTION'];
    if (endpointData.environment && !validEnvironments.includes(endpointData.environment.toUpperCase())) {
        errors.push(`Invalid environment. Must be one of: ${validEnvironments.join(', ')}`);
    }
    
    // Validate URL format
    if (endpointData.apiUrl) {
        try {
            new URL(endpointData.apiUrl);
        } catch (e) {
            errors.push('Invalid API URL format');
        }
    }
    
    // Validate numeric fields
    if (endpointData.timeoutMs && (isNaN(endpointData.timeoutMs) || endpointData.timeoutMs <= 0)) {
        errors.push('Timeout must be a positive number');
    }
    
    if (endpointData.retryCount && (isNaN(endpointData.retryCount) || endpointData.retryCount < 0)) {
        errors.push('Retry count cannot be negative');
    }
    
    if (endpointData.retryDelayMs && (isNaN(endpointData.retryDelayMs) || endpointData.retryDelayMs < 0)) {
        errors.push('Retry delay cannot be negative');
    }
    
    if (endpointData.maxResponseSize && (isNaN(endpointData.maxResponseSize) || endpointData.maxResponseSize <= 0)) {
        errors.push('Max response size must be a positive number');
    }
    
    return errors;
};

/**
 * Build pagination parameters
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildPaginationParams = (page = 0, size = 10, sortField = 'apiName', sortDirection = 'ASC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build search filter parameters
 * @param {Object} filters - Filter criteria
 * @returns {Object} Filter parameters
 */
export const buildSearchFilters = (filters = {}) => {
    const {
        apiCode = '',
        apiName = '',
        apiUrl = '',
        httpMethod = '',
        authType = '',
        status = '',
        environment = '',
        collectionName = '',
        category = '',
        requestBodyType = '',
        operationId = ''
    } = filters;
    
    return {
        apiCode,
        apiName,
        apiUrl,
        httpMethod,
        authType,
        status,
        environment,
        collectionName,
        category,
        requestBodyType,
        operationId
    };
};

/**
 * Build API endpoint DTO from form data
 * @param {Object} endpointData - Endpoint data from form
 * @returns {Object} API endpoint DTO
 */
export const buildTPartyAPIDTO = (endpointData) => {
    const {
        apiCode,
        apiName,
        apiUrl,
        httpMethod,
        operationId,
        description,
        isActive = true,
        requestBodyType = 'NONE',
        requestBody,
        bodyLanguage = 'json',
        headers = {},
        queryParams = {},
        pathParams = {},
        metadata = {},
        authType = 'NONE',
        authToken,
        authTokenKey,
        authUsername,
        authPassword,
        authApiKeyHeader,
        authApiKeyValue,
        authAddTo = 'header',
        apiKeyLocation = 'header',
        timeoutMs = 30000,
        retryCount = 3,
        retryDelayMs = 1000,
        followRedirects = true,
        validateSSL = true,
        compressRequest = false,
        saveResponses = true,
        dnsServer = '',
        proxyServer = '',
        maxResponseSize = 1024,
        successCriteria = {},
        responseMapping = {},
        environment = 'Development',
        environmentVariables = {},
        collectionId,
        collectionName,
        category = 'Uncategorized',
        tags = [],
        preRequestScript = '',
        testScripts = '',
        testResults = ''
    } = endpointData;
    
    return {
        apiCode,
        apiName,
        apiUrl,
        httpMethod: httpMethod.toUpperCase(),
        operationId,
        description,
        isActive,
        requestBodyType: requestBodyType.toUpperCase(),
        requestBody,
        bodyLanguage,
        headers,
        queryParams,
        pathParams,
        metadata,
        authType: authType.toUpperCase(),
        authToken,
        authTokenKey,
        authUsername,
        authPassword,
        authApiKeyHeader,
        authApiKeyValue,
        authAddTo,
        apiKeyLocation,
        timeoutMs,
        retryCount,
        retryDelayMs,
        followRedirects,
        validateSSL,
        compressRequest,
        saveResponses,
        dnsServer,
        proxyServer,
        maxResponseSize,
        successCriteria,
        responseMapping,
        environment,
        environmentVariables,
        collectionId,
        collectionName,
        category,
        tags,
        preRequestScript,
        testScripts,
        testResults
    };
};

/**
 * Build simplified API endpoint DTO for form initialization
 * @param {Object} endpointData - Raw endpoint data
 * @returns {Object} Simplified DTO for form
 */
export const buildTPartyAPIFormData = (endpointData) => {
    if (!endpointData) return null;
    
    return {
        apiCode: endpointData.apiCode || '',
        apiName: endpointData.apiName || '',
        apiUrl: endpointData.apiUrl || '',
        httpMethod: endpointData.httpMethod || 'GET',
        operationId: endpointData.operationId || '',
        description: endpointData.description || '',
        isActive: endpointData.isActive !== undefined ? endpointData.isActive : true,
        requestBodyType: endpointData.requestBodyType || 'NONE',
        requestBody: endpointData.requestBody || null,
        bodyLanguage: endpointData.bodyLanguage || 'json',
        headers: endpointData.headers || {},
        queryParams: endpointData.queryParams || {},
        pathParams: endpointData.pathParams || {},
        metadata: endpointData.metadata || {},
        authType: endpointData.authType || 'NONE',
        authToken: endpointData.authToken || '',
        authTokenKey: endpointData.authTokenKey || '',
        authUsername: endpointData.authUsername || '',
        authPassword: endpointData.authPassword || '',
        authApiKeyHeader: endpointData.authApiKeyHeader || '',
        authApiKeyValue: endpointData.authApiKeyValue || '',
        authAddTo: endpointData.authAddTo || 'header',
        apiKeyLocation: endpointData.apiKeyLocation || 'header',
        timeoutMs: endpointData.timeoutMs || 30000,
        retryCount: endpointData.retryCount || 3,
        retryDelayMs: endpointData.retryDelayMs || 1000,
        followRedirects: endpointData.followRedirects !== undefined ? endpointData.followRedirects : true,
        validateSSL: endpointData.validateSSL !== undefined ? endpointData.validateSSL : true,
        compressRequest: endpointData.compressRequest !== undefined ? endpointData.compressRequest : false,
        saveResponses: endpointData.saveResponses !== undefined ? endpointData.saveResponses : true,
        dnsServer: endpointData.dnsServer || '',
        proxyServer: endpointData.proxyServer || '',
        maxResponseSize: endpointData.maxResponseSize || 1024,
        successCriteria: endpointData.successCriteria || {},
        responseMapping: endpointData.responseMapping || {},
        environment: endpointData.environment || 'Development',
        environmentVariables: endpointData.environmentVariables || {},
        collectionId: endpointData.collectionId || '',
        collectionName: endpointData.collectionName || '',
        category: endpointData.category || 'Uncategorized',
        tags: endpointData.tags || [],
        preRequestScript: endpointData.preRequestScript || '',
        testScripts: endpointData.testScripts || '',
        testResults: endpointData.testResults || '',
        
        // Additional derived fields
        formattedUrl: endpointData.formattedUrl || '',
        status: endpointData.status || '',
        executionCount: endpointData.executionCount || 0,
        successCount: endpointData.successCount || 0,
        failureCount: endpointData.failureCount || 0,
        averageResponseTimeMs: endpointData.averageResponseTimeMs || 0,
        lastExecutedDate: endpointData.lastExecutedDate || '',
        createdDate: endpointData.createdDate || '',
        lastModifiedDate: endpointData.lastModifiedDate || ''
    };
};

/**
 * Build API execution request data
 * @param {Object} executionData - Execution parameters
 * @returns {Object} Execution request
 */
export const buildApiExecutionRequest = (executionData = {}) => {
    const {
        requestBody = {},
        queryParams = {},
        headers = {},
        timeout = 30000,
        validateSSL = true,
        followRedirects = true,
        compressRequest = false
    } = executionData;
    
    return {
        ...requestBody,
        _metadata: {
            queryParams,
            headers,
            timeout,
            validateSSL,
            followRedirects,
            compressRequest
        }
    };
};

/**
 * Extract environment data from endpoint
 * @param {Object} endpoint - API endpoint
 * @returns {Object} Environment data
 */
export const extractEnvironmentData = (endpoint) => {
    if (!endpoint) return null;
    
    return {
        environment: endpoint.environment || 'Development',
        environmentVariables: endpoint.environmentVariables || {},
        collectionId: endpoint.collectionId || '',
        collectionName: endpoint.collectionName || '',
        category: endpoint.category || 'Uncategorized'
    };
};

/**
 * Extract authentication data from endpoint
 * @param {Object} endpoint - API endpoint
 * @returns {Object} Authentication data
 */
export const extractAuthData = (endpoint) => {
    if (!endpoint) return null;
    
    return {
        authType: endpoint.authType || 'NONE',
        authToken: endpoint.authToken || '',
        authTokenKey: endpoint.authTokenKey || '',
        authUsername: endpoint.authUsername || '',
        authPassword: endpoint.authPassword || '',
        authApiKeyHeader: endpoint.authApiKeyHeader || '',
        authApiKeyValue: endpoint.authApiKeyValue || '',
        authAddTo: endpoint.authAddTo || 'header',
        apiKeyLocation: endpoint.apiKeyLocation || 'header'
    };
};

/**
 * Extract body configuration from endpoint
 * @param {Object} endpoint - API endpoint
 * @returns {Object} Body configuration
 */
export const extractBodyConfig = (endpoint) => {
    if (!endpoint) return null;
    
    return {
        requestBodyType: endpoint.requestBodyType || 'NONE',
        requestBody: endpoint.requestBody || null,
        bodyLanguage: endpoint.bodyLanguage || 'json',
        metadata: endpoint.metadata || {}
    };
};

/**
 * Extract settings from endpoint
 * @param {Object} endpoint - API endpoint
 * @returns {Object} Settings data
 */
export const extractSettings = (endpoint) => {
    if (!endpoint) return null;
    
    return {
        timeoutMs: endpoint.timeoutMs || 30000,
        retryCount: endpoint.retryCount || 3,
        retryDelayMs: endpoint.retryDelayMs || 1000,
        followRedirects: endpoint.followRedirects !== undefined ? endpoint.followRedirects : true,
        validateSSL: endpoint.validateSSL !== undefined ? endpoint.validateSSL : true,
        compressRequest: endpoint.compressRequest !== undefined ? endpoint.compressRequest : false,
        saveResponses: endpoint.saveResponses !== undefined ? endpoint.saveResponses : true,
        dnsServer: endpoint.dnsServer || '',
        proxyServer: endpoint.proxyServer || '',
        maxResponseSize: endpoint.maxResponseSize || 1024
    };
};

/**
 * Check if endpoint is testable
 * @param {Object} endpoint - API endpoint
 * @returns {boolean} True if endpoint can be tested
 */
export const isEndpointTestable = (endpoint) => {
    if (!endpoint) return false;
    
    if (!endpoint.isActive) return false;
    if (!endpoint.apiUrl || endpoint.apiUrl.trim() === '') return false;
    if (!endpoint.httpMethod) return false;
    if (!endpoint.environment) return false;
    
    return true;
};

/**
 * Get environment display name
 * @param {string} environment - Environment value
 * @returns {string} Display name
 */
export const getEnvironmentDisplay = (environment) => {
    if (!environment) return 'Development';
    
    switch (environment.toUpperCase()) {
        case 'DEVELOPMENT': return 'Development';
        case 'STAGING': return 'Staging';
        case 'PRODUCTION': return 'Production';
        default: return environment;
    }
};

/**
 * Get auth type display name
 * @param {string} authType - Auth type value
 * @returns {string} Display name
 */
export const getAuthTypeDisplay = (authType) => {
    if (!authType) return 'No Auth';
    
    switch (authType.toUpperCase()) {
        case 'BEARER_TOKEN': return 'Bearer Token';
        case 'API_KEY': return 'API Key';
        case 'BASIC_AUTH': return 'Basic Auth';
        case 'NONE': return 'No Auth';
        default: return authType;
    }
};

/**
 * Get request body type display name
 * @param {string} requestBodyType - Body type value
 * @returns {string} Display name
 */
export const getRequestBodyTypeDisplay = (requestBodyType) => {
    if (!requestBodyType) return 'None';
    
    switch (requestBodyType.toUpperCase()) {
        case 'RAW': return 'Raw';
        case 'FORM_DATA': return 'Form Data';
        case 'X_WWW_FORM_URLENCODED':
        case 'URL_ENCODED': return 'x-www-form-urlencoded';
        case 'BINARY': return 'Binary';
        case 'GRAPHQL': return 'GraphQL';
        case 'NONE': return 'None';
        default: return requestBodyType;
    }
};

/**
 * Get HTTP method color for UI display
 * @param {string} httpMethod - HTTP method
 * @returns {string} Color name
 */
export const getHttpMethodColor = (httpMethod) => {
    if (!httpMethod) return 'gray';
    
    switch (httpMethod.toUpperCase()) {
        case 'GET': return 'green';
        case 'POST': return 'blue';
        case 'PUT': return 'orange';
        case 'DELETE': return 'red';
        case 'PATCH': return 'purple';
        default: return 'gray';
    }
};

/**
 * Calculate success rate
 * @param {number} executionCount - Total executions
 * @param {number} successCount - Successful executions
 * @returns {number} Success rate percentage
 */
export const calculateSuccessRate = (executionCount, successCount) => {
    if (!executionCount || executionCount === 0) return 0;
    return (successCount / executionCount) * 100;
};

/**
 * Build bulk update environment request
 * @param {string} oldEnvironment - Old environment value
 * @param {string} newEnvironment - New environment value
 * @param {Array} endpointIds - Optional array of endpoint IDs to update
 * @returns {Object} Update request
 */
export const buildBulkEnvironmentUpdateRequest = (oldEnvironment, newEnvironment, endpointIds = null) => {
    const request = {
        oldEnvironment,
        newEnvironment
    };
    
    if (endpointIds && endpointIds.length > 0) {
        request.endpointIds = endpointIds;
    }
    
    return request;
};