// controllers/IntegrationController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for search/filter
const buildIntegrationQueryParams = (params = {}) => {
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

// ============ INTEGRATION MANAGEMENT METHODS ============

/**
 * Create a new integration
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} integrationData - Integration DTO
 * @returns {Promise} API response
 */
export const createIntegration = async (authorizationHeader, integrationData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/integrations`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(integrationData)
        })
    );
};

/**
 * Create multiple integrations in bulk
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} integrationsData - Array of Integration DTOs
 * @returns {Promise} API response
 */
export const createIntegrationsBulk = async (authorizationHeader, integrationsData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/integrations/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(integrationsData)
        })
    );
};

/**
 * Get a single integration by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} integrationId - Integration ID (UUID)
 * @returns {Promise} API response
 */
export const getIntegrationById = async (authorizationHeader, integrationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/integrations/${integrationId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get integration
 * @param {string} authorizationHeader - Bearer token
 * @param {string} integrationId - Integration ID (UUID)
 * @returns {Promise} API response
 */
export const getIntegration = async (authorizationHeader, integrationId) => {
    return getIntegrationById(authorizationHeader, integrationId);
};

/**
 * Get all integrations (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllIntegrations = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'integrationCode', direction = 'ASC' } = pagination;
    
    const queryParams = buildIntegrationQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/integrations${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get all integrations
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getIntegrations = async (authorizationHeader) => {
    return getAllIntegrations(authorizationHeader);
};

/**
 * Search integrations with filters
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.status - Status filter
 * @param {string} filters.provider - Provider filter
 * @param {string} filters.type - Type filter
 * @param {string} filters.name - Name filter
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchIntegrations = async (authorizationHeader, filters = {}, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'integrationCode', direction = 'ASC' } = pagination;
    
    const queryParams = buildIntegrationQueryParams({
        ...filters,
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/integrations/search${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Update an integration by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} integrationId - Integration ID (UUID)
 * @param {Object} integrationData - Updated integration data
 * @returns {Promise} API response
 */
export const updateIntegration = async (authorizationHeader, integrationId, integrationData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/integrations/${integrationId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(integrationData)
        })
    );
};

/**
 * Delete an integration by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} integrationId - Integration ID (UUID)
 * @returns {Promise} API response
 */
export const deleteIntegration = async (authorizationHeader, integrationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/integrations/${integrationId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for integration operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleIntegrationResponse = (response) => {
    if (!response) {
        throw new Error('No response received from integration service');
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
            return { message: 'No integrations found', data: [] };
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Integration not found: ${response.message}`);
        case 409: throw new Error(`Conflict: ${response.message}`);
        case 422: throw new Error(`Business rule violation: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

/**
 * Extract pagination info from integration response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractIntegrationPaginationInfo = (response) => {
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
 * Validate integration data
 * @param {Object} integrationData - Integration data to validate
 * @returns {Array} Array of validation errors
 */
export const validateIntegrationData = (integrationData) => {
    const errors = [];
    
    if (!integrationData.integrationCode) errors.push('Integration code is required');
    if (!integrationData.integrationName) errors.push('Integration name is required');
    if (!integrationData.provider) errors.push('Provider is required');
    if (!integrationData.type) errors.push('Type is required');
    if (!integrationData.baseUrl) errors.push('Base URL is required');
    
    // Validate integration code format (alphanumeric with underscores)
    if (integrationData.integrationCode && !/^[A-Z_][A-Z0-9_]*$/.test(integrationData.integrationCode)) {
        errors.push('Integration code must start with an uppercase letter or underscore and contain only uppercase letters, numbers, and underscores');
    }
    
    // Validate type
    const validTypes = ['API', 'DATABASE', 'MESSAGE_QUEUE', 'WEBHOOK', 'FILE_TRANSFER', 'SOCIAL_MEDIA', 'PAYMENT_GATEWAY'];
    if (integrationData.type && !validTypes.includes(integrationData.type.toUpperCase())) {
        errors.push(`Invalid integration type. Must be one of: ${validTypes.join(', ')}`);
    }
    
    // Validate base URL format
    if (integrationData.baseUrl) {
        try {
            new URL(integrationData.baseUrl);
        } catch (e) {
            errors.push('Invalid base URL format');
        }
    }
    
    // Validate authentication type if provided
    if (integrationData.authType) {
        const validAuthTypes = ['API_KEY', 'BEARER_TOKEN', 'BASIC_AUTH', 'OAUTH2', 'OAUTH1', 'NONE'];
        if (!validAuthTypes.includes(integrationData.authType.toUpperCase())) {
            errors.push(`Invalid authentication type. Must be one of: ${validAuthTypes.join(', ')}`);
        }
    }
    
    // Validate timeout if provided
    if (integrationData.timeoutMs && (isNaN(integrationData.timeoutMs) || integrationData.timeoutMs < 1000)) {
        errors.push('Timeout must be at least 1000 milliseconds');
    }
    
    // Validate description length if provided
    if (integrationData.description && integrationData.description.length > 1000) {
        errors.push('Description cannot exceed 1000 characters');
    }
    
    return errors;
};

/**
 * Build pagination parameters for integrations
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildIntegrationPaginationParams = (page = 0, size = 1000, sortField = 'integrationCode', sortDirection = 'ASC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build search filter parameters for integrations
 * @param {Object} filters - Filter criteria
 * @returns {Object} Filter parameters
 */
export const buildIntegrationSearchFilters = (filters = {}) => {
    const {
        status = '',
        provider = '',
        type = '',
        name = ''
    } = filters;
    
    const searchFilters = {};
    
    if (status) searchFilters.status = status;
    if (provider) searchFilters.provider = provider;
    if (type) searchFilters.type = type;
    if (name) searchFilters.name = name;
    
    return searchFilters;
};

/**
 * Build Integration DTO
 * @param {Object} integrationData - Integration data
 * @returns {Object} Integration DTO
 */
export const buildIntegrationDTO = (integrationData) => {
    const {
        integrationCode,
        integrationName,
        provider,
        type,
        baseUrl,
        description = '',
        authType = 'API_KEY',
        apiKey = null,
        secretKey = null,
        bearerToken = null,
        username = null,
        password = null,
        clientId = null,
        clientSecret = null,
        redirectUri = null,
        scope = null,
        timeoutMs = 30000,
        retryCount = 3,
        retryDelayMs = 1000,
        maxConcurrentRequests = 10,
        status = 'ACTIVE',
        metadata = {}
    } = integrationData;
    
    const dto = {
        integrationCode: integrationCode.toUpperCase(),
        integrationName,
        provider,
        type: type.toUpperCase(),
        baseUrl,
        description,
        authType: authType.toUpperCase(),
        timeoutMs,
        retryCount,
        retryDelayMs,
        maxConcurrentRequests,
        status: status.toUpperCase(),
        metadata
    };
    
    // Add authentication details based on auth type
    switch (authType.toUpperCase()) {
        case 'API_KEY':
            if (apiKey) dto.apiKey = apiKey;
            if (secretKey) dto.secretKey = secretKey;
            break;
        case 'BEARER_TOKEN':
            if (bearerToken) dto.bearerToken = bearerToken;
            break;
        case 'BASIC_AUTH':
            if (username) dto.username = username;
            if (password) dto.password = password;
            break;
        case 'OAUTH2':
            if (clientId) dto.clientId = clientId;
            if (clientSecret) dto.clientSecret = clientSecret;
            if (redirectUri) dto.redirectUri = redirectUri;
            if (scope) dto.scope = scope;
            break;
    }
    
    return dto;
};

/**
 * Build bulk integrations request
 * @param {Array} integrations - Array of integration data objects
 * @returns {Array} Array of Integration DTOs
 */
export const buildBulkIntegrationsRequest = (integrations) => {
    if (!Array.isArray(integrations)) {
        throw new Error('Integrations must be an array');
    }
    
    return integrations.map(integration => buildIntegrationDTO(integration));
};

/**
 * Get authentication configuration based on auth type
 * @param {Object} integration - Integration data
 * @returns {Object} Authentication configuration
 */
export const getIntegrationAuthConfig = (integration) => {
    const { authType } = integration;
    
    switch (authType?.toUpperCase()) {
        case 'API_KEY':
            return {
                type: 'api_key',
                headers: {
                    'X-API-Key': integration.apiKey,
                    ...(integration.secretKey && { 'X-API-Secret': integration.secretKey })
                }
            };
        case 'BEARER_TOKEN':
            return {
                type: 'bearer',
                headers: {
                    'Authorization': `Bearer ${integration.bearerToken}`
                }
            };
        case 'BASIC_AUTH':
            return {
                type: 'basic',
                headers: {
                    'Authorization': `Basic ${btoa(`${integration.username}:${integration.password}`)}`
                }
            };
        case 'OAUTH2':
            return {
                type: 'oauth2',
                config: {
                    clientId: integration.clientId,
                    clientSecret: integration.clientSecret,
                    redirectUri: integration.redirectUri,
                    scope: integration.scope
                }
            };
        default:
            return {
                type: 'none',
                headers: {}
            };
    }
};

/**
 * Prepare integration request
 * @param {Object} integration - Integration configuration
 * @param {Object} requestData - Request data
 * @returns {Object} Prepared request configuration
 */
export const prepareIntegrationRequest = (integration, requestData = {}) => {
    const {
        endpoint = '',
        method = 'GET',
        headers = {},
        params = {},
        data = null,
        timeout = null
    } = requestData;
    
    const authConfig = getIntegrationAuthConfig(integration);
    const url = `${integration.baseUrl}${endpoint}`;
    
    const requestConfig = {
        url,
        method: method.toUpperCase(),
        headers: {
            ...authConfig.headers,
            'Content-Type': 'application/json',
            ...headers
        },
        params,
        timeout: timeout || integration.timeoutMs,
        retryCount: integration.retryCount,
        retryDelay: integration.retryDelayMs
    };
    
    if (data && (method.toUpperCase() === 'POST' || method.toUpperCase() === 'PUT' || method.toUpperCase() === 'PATCH')) {
        requestConfig.data = typeof data === 'string' ? data : JSON.stringify(data);
    }
    
    return requestConfig;
};

/**
 * Test integration connectivity
 * @param {Object} integration - Integration configuration
 * @returns {Promise} Test result
 */
export const testIntegrationConnectivity = async (integration) => {
    try {
        const requestConfig = prepareIntegrationRequest(integration, {
            endpoint: '/health', // Common health check endpoint
            method: 'GET',
            timeout: 5000 // Short timeout for connectivity test
        });
        
        const response = await fetch(requestConfig.url, {
            method: requestConfig.method,
            headers: requestConfig.headers,
            signal: AbortSignal.timeout(requestConfig.timeout)
        });
        
        return {
            success: response.ok,
            status: response.status,
            statusText: response.statusText,
            timestamp: new Date().toISOString()
        };
    } catch (error) {
        return {
            success: false,
            error: error.message,
            timestamp: new Date().toISOString()
        };
    }
};

/**
 * Execute integration request
 * @param {Object} integration - Integration configuration
 * @param {Object} requestData - Request data
 * @returns {Promise} API response
 */
export const executeIntegrationRequest = async (integration, requestData = {}) => {
    const requestConfig = prepareIntegrationRequest(integration, requestData);
    
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
            timestamp: new Date().toISOString()
        };
    } catch (error) {
        return {
            success: false,
            error: error.message,
            timestamp: new Date().toISOString()
        };
    }
};

/**
 * Extract integration statistics from response
 * @param {Object} response - API response
 * @returns {Object} Integration statistics
 */
export const extractIntegrationStatistics = (response) => {
    if (!response.data) return null;
    
    const data = response.data;
    return {
        totalIntegrations: data.totalIntegrations || 0,
        activeIntegrations: data.activeIntegrations || 0,
        inactiveIntegrations: data.inactiveIntegrations || 0,
        integrationsByType: data.integrationsByType || {},
        integrationsByProvider: data.integrationsByProvider || {},
        integrationsByStatus: data.integrationsByStatus || {},
        mostUsedIntegrations: data.mostUsedIntegrations || [],
        averageResponseTime: data.averageResponseTime || 0
    };
};

/**
 * Mask sensitive data in integration object
 * @param {Object} integration - Integration object
 * @returns {Object} Integration with masked sensitive data
 */
export const maskSensitiveIntegrationData = (integration) => {
    const masked = { ...integration };
    
    // Mask sensitive fields
    if (masked.apiKey) masked.apiKey = '********';
    if (masked.secretKey) masked.secretKey = '********';
    if (masked.bearerToken) masked.bearerToken = '********';
    if (masked.password) masked.password = '********';
    if (masked.clientSecret) masked.clientSecret = '********';
    
    return masked;
};