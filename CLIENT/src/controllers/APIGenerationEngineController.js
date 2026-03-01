// controllers/APIGenerationEngineController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// ============================================================
// HELPER FUNCTIONS
// ============================================================

/**
 * Get authorization headers
 * @param {string} jwtToken - JWT token
 * @returns {Object} Headers object
 */
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

/**
 * Build query parameters
 * @param {Object} params - Parameters object
 * @returns {URLSearchParams} URL search params
 */
const buildQueryParams = (params = {}) => {
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

/**
 * Generate request ID
 * @returns {string} Request ID
 */
const generateRequestId = () => {
    return 'req_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

/**
 * Get random color for collections
 * @returns {string} Hex color code
 */
const getRandomColor = () => {
    const colors = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
                    "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"];
    return colors[Math.floor(Math.random() * colors.length)];
};

// ============================================================
// API GENERATION ENGINE CONTROLLER
// ============================================================

/**
 * Generate a new API based on configuration
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} request - Generate API request
 * @param {string} request.apiName - API name
 * @param {string} request.apiCode - API code (unique identifier)
 * @param {string} request.description - API description
 * @param {string} request.version - API version
 * @param {string} request.httpMethod - HTTP method (GET, POST, PUT, DELETE, etc.)
 * @param {string} request.basePath - Base path (e.g., /api/v1)
 * @param {string} request.endpointPath - Endpoint path (e.g., /users)
 * @param {string} request.category - API category
 * @param {string} request.owner - API owner
 * @param {string} request.status - API status (DRAFT, ACTIVE, DEPRECATED)
 * @param {Array<string>} request.tags - API tags
 * @param {Object} request.sourceObject - Source Oracle object configuration
 * @param {Object} request.schemaConfig - Schema configuration
 * @param {Object} request.authConfig - Authentication configuration
 * @param {Object} request.requestBody - Request body configuration
 * @param {Object} request.responseBody - Response body configuration
 * @param {Object} request.settings - API settings
 * @param {Array<Object>} request.parameters - API parameters
 * @param {Array<Object>} request.responseMappings - Response mappings
 * @param {Array<Object>} request.headers - API headers
 * @param {Object} request.tests - Test configurations
 * @param {boolean} request.regenerateComponents - Whether to regenerate components
 * @returns {Promise} API response
 */
export const generateApi = async (authorizationHeader, request = {}) => {
    const requestId = generateRequestId();
    
    // Validate required fields
    if (!request.apiName) {
        return Promise.reject({
            responseCode: 400,
            message: "API name is required",
            requestId
        });
    }
    
    if (!request.apiCode) {
        return Promise.reject({
            responseCode: 400,
            message: "API code is required",
            requestId
        });
    }
    
    if (!request.httpMethod) {
        return Promise.reject({
            responseCode: 400,
            message: "HTTP method is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/generate`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(request),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error generating API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to generate API',
            data: null,
            requestId
        };
    });
};

/**
 * Execute a generated API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {Object} executeRequest - Execute request
 * @param {string} executeRequest.requestId - Request ID (optional)
 * @param {Object} executeRequest.pathParams - Path parameters
 * @param {Object} executeRequest.queryParams - Query parameters
 * @param {Object} executeRequest.headers - Request headers
 * @param {Object} executeRequest.body - Request body
 * @returns {Promise} API response
 */
export const executeApi = async (authorizationHeader, apiId, executeRequest = {}) => {
    const requestId = executeRequest.requestId || generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/execute`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(executeRequest),
            requestId: requestId
        })
    ).then(response => {
        return transformExecuteApiResponse(response);
    }).catch(error => {
        console.error('Error executing API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to execute API',
            data: null,
            requestId
        };
    });
};

/**
 * Test a generated API with sample data
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {Object} testRequest - Test request
 * @param {string} testRequest.testName - Test name
 * @param {string} testRequest.testType - Test type (UNIT, INTEGRATION, PERFORMANCE)
 * @param {Object} testRequest.pathParams - Path parameters
 * @param {Object} testRequest.queryParams - Query parameters
 * @param {Object} testRequest.headers - Request headers
 * @param {Object} testRequest.body - Request body
 * @param {Object} testRequest.expectedResponse - Expected response
 * @returns {Promise} API response
 */
export const testApi = async (authorizationHeader, apiId, testRequest = {}) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    if (!testRequest.testName) {
        return Promise.reject({
            responseCode: 400,
            message: "Test name is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/test`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(testRequest),
            requestId: requestId
        })
    ).then(response => {
        return transformApiTestResultResponse(response);
    }).catch(error => {
        console.error('Error testing API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to test API',
            data: null,
            requestId
        };
    });
};

/**
 * Get API details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const getApiDetails = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error getting API details:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get API details',
            data: null,
            requestId
        };
    });
};

/**
 * Get API analytics
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {string} startDate - Start date (ISO format)
 * @param {string} endDate - End date (ISO format)
 * @returns {Promise} API response
 */
export const getApiAnalytics = async (authorizationHeader, apiId, startDate, endDate) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    if (!startDate || !endDate) {
        return Promise.reject({
            responseCode: 400,
            message: "Start date and end date are required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ startDate, endDate });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/analytics?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformApiAnalyticsResponse(response);
    }).catch(error => {
        console.error('Error getting API analytics:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get API analytics',
            data: null,
            requestId
        };
    });
};

/**
 * Generate API code (PL/SQL, OpenAPI, Postman)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response with generated files
 */
export const generateApiCode = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/code`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedCodeResponse(response);
    }).catch(error => {
        console.error('Error generating API code:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to generate API code',
            data: null,
            requestId
        };
    });
};

/**
 * Get code example for a specific language
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {string} language - Programming language (curl, javascript, python, java, etc.)
 * @returns {Promise} API response with code example
 */
export const getCodeExample = async (authorizationHeader, apiId, language) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    if (!language) {
        return Promise.reject({
            responseCode: 400,
            message: "Language is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/code/${encodeURIComponent(language)}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformCodeExampleResponse(response);
    }).catch(error => {
        console.error('Error getting code example:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get code example',
            data: null,
            requestId
        };
    });
};

/**
 * Get execution logs for an API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {string} fromDate - From date (ISO format, optional)
 * @param {string} toDate - To date (ISO format, optional)
 * @param {number} limit - Maximum number of logs (default: 100)
 * @returns {Promise} API response
 */
export const getExecutionLogs = async (authorizationHeader, apiId, fromDate, toDate, limit = 100) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ fromDate, toDate, limit });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/logs${queryParams.toString() ? `?${queryParams.toString()}` : ''}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformExecutionLogsResponse(response);
    }).catch(error => {
        console.error('Error getting execution logs:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get execution logs',
            data: null,
            requestId
        };
    });
};

/**
 * Get test results for an API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const getTestResults = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/tests`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTestResultsResponse(response);
    }).catch(error => {
        console.error('Error getting test results:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get test results',
            data: null,
            requestId
        };
    });
};

/**
 * Update API status
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {string} status - New status (DRAFT, ACTIVE, DEPRECATED, ARCHIVED)
 * @returns {Promise} API response
 */
export const updateApiStatus = async (authorizationHeader, apiId, status) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    if (!status) {
        return Promise.reject({
            responseCode: 400,
            message: "Status is required",
            requestId
        });
    }
    
    const validStatuses = ['DRAFT', 'ACTIVE', 'DEPRECATED', 'ARCHIVED'];
    if (!validStatuses.includes(status)) {
        return Promise.reject({
            responseCode: 400,
            message: `Invalid status. Valid statuses: ${validStatuses.join(', ')}`,
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/status`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify({ status }),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error updating API status:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to update API status',
            data: null,
            requestId
        };
    });
};

/**
 * Validate an Oracle source object before API generation
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectName - Object name
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE, SYNONYM)
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const validateSourceObject = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectName, objectType, owner } = params;
    
    if (!objectName) {
        return Promise.reject({
            responseCode: 400,
            message: "Object name is required",
            requestId
        });
    }
    
    if (!objectType) {
        return Promise.reject({
            responseCode: 400,
            message: "Object type is required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ objectName, objectType, owner });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/validate/source-object?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformValidationResponse(response);
    }).catch(error => {
        console.error('Error validating source object:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to validate source object',
            data: null,
            requestId
        };
    });
};

/**
 * Get related components (Code Base, Collections, Documentation)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const getRelatedComponents = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/related-components`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformRelatedComponentsResponse(response);
    }).catch(error => {
        console.error('Error getting related components:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get related components',
            data: null,
            requestId
        };
    });
};

/**
 * Update an existing generated API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {Object} request - Generate API request (same as generateApi)
 * @returns {Promise} API response
 */
export const updateApi = async (authorizationHeader, apiId, request = {}) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    // Validate required fields
    if (!request.apiName) {
        return Promise.reject({
            responseCode: 400,
            message: "API name is required",
            requestId
        });
    }
    
    if (!request.apiCode) {
        return Promise.reject({
            responseCode: 400,
            message: "API code is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(request),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error updating API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to update API',
            data: null,
            requestId
        };
    });
};

/**
 * Partially update specific fields of an API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {Object} updates - Fields to update
 * @param {string} updates.apiName - API name (optional)
 * @param {string} updates.description - API description (optional)
 * @param {string} updates.version - API version (optional)
 * @param {string} updates.status - API status (optional)
 * @param {string} updates.category - API category (optional)
 * @param {Array<string>} updates.tags - API tags (optional)
 * @returns {Promise} API response
 */
export const partialUpdateApi = async (authorizationHeader, apiId, updates = {}) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    if (Object.keys(updates).length === 0) {
        return Promise.reject({
            responseCode: 400,
            message: "At least one field to update is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}`, {
            method: 'PATCH',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(updates),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error partially updating API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to update API',
            data: null,
            requestId
        };
    });
};

/**
 * Manually sync code base, collections, and documentation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const syncComponents = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/sync-components`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return {
            responseCode: 200,
            message: "Components synced successfully",
            data: response.data,
            requestId
        };
    }).catch(error => {
        console.error('Error syncing components:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to sync components',
            data: null,
            requestId
        };
    });
};

// ============================================================
// LIST/SEARCH ENDPOINTS
// ============================================================

/**
 * Get all generated APIs with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @param {string} params.status - Filter by status (DRAFT, ACTIVE, DEPRECATED)
 * @param {string} params.category - Filter by category
 * @param {string} params.search - Search term
 * @returns {Promise} API response
 */
export const getAllApis = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10, status, category, search } = params;
    
    const queryParams = buildQueryParams({ page, pageSize, status, category, search });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedApisResponse(response);
    }).catch(error => {
        console.error('Error getting APIs:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get APIs',
            data: { items: [], totalCount: 0, page, pageSize },
            requestId
        };
    });
};

/**
 * Get all APIs for the current user
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getMyApis = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/my-apis?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedApisResponse(response);
    }).catch(error => {
        console.error('Error getting my APIs:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get my APIs',
            data: { items: [], totalCount: 0, page, pageSize },
            requestId
        };
    });
};

/**
 * Get API versions
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiCode - API code
 * @returns {Promise} API response
 */
export const getApiVersions = async (authorizationHeader, apiCode) => {
    const requestId = generateRequestId();
    
    if (!apiCode) {
        return Promise.reject({
            responseCode: 400,
            message: "API code is required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ apiCode });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/versions?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformApiVersionsResponse(response);
    }).catch(error => {
        console.error('Error getting API versions:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get API versions',
            data: [],
            requestId
        };
    });
};

/**
 * Check if API code is available
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiCode - API code to check
 * @returns {Promise} API response
 */
export const checkApiCodeAvailability = async (authorizationHeader, apiCode) => {
    const requestId = generateRequestId();
    
    if (!apiCode) {
        return Promise.reject({
            responseCode: 400,
            message: "API code is required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ apiCode });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/check-code?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return {
            responseCode: 200,
            message: response.message || "Code availability checked",
            data: {
                available: response.data?.available || false,
                apiCode
            },
            requestId
        };
    }).catch(error => {
        console.error('Error checking API code:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to check API code',
            data: { available: false, apiCode },
            requestId
        };
    });
};

/**
 * Get API categories
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getApiCategories = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/categories`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformCategoriesResponse(response);
    }).catch(error => {
        console.error('Error getting API categories:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get API categories',
            data: [],
            requestId
        };
    });
};

// ============================================================
// DASHBOARD/STATS ENDPOINTS
// ============================================================

/**
 * Get API dashboard statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getApiDashboardStats = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/dashboard/stats`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDashboardStatsResponse(response);
    }).catch(error => {
        console.error('Error getting dashboard stats:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get dashboard stats',
            data: {
                totalApis: 0,
                activeApis: 0,
                draftApis: 0,
                deprecatedApis: 0,
                totalCalls: 0,
                successRate: 0,
                avgResponseTime: 0,
                byCategory: [],
                recentActivity: []
            },
            requestId
        };
    });
};

/**
 * Get recent API activity
 * @param {string} authorizationHeader - Bearer token
 * @param {number} limit - Number of activities (default: 10)
 * @returns {Promise} API response
 */
export const getRecentApiActivity = async (authorizationHeader, limit = 10) => {
    const requestId = generateRequestId();
    
    const queryParams = buildQueryParams({ limit });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/recent-activity?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformRecentActivityResponse(response);
    }).catch(error => {
        console.error('Error getting recent activity:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get recent activity',
            data: [],
            requestId
        };
    });
};

/**
 * Get API usage summary
 * @param {string} authorizationHeader - Bearer token
 * @param {string} period - Period (day, week, month, year)
 * @returns {Promise} API response
 */
export const getApiUsageSummary = async (authorizationHeader, period = 'week') => {
    const requestId = generateRequestId();
    
    const queryParams = buildQueryParams({ period });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/usage-summary?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformUsageSummaryResponse(response);
    }).catch(error => {
        console.error('Error getting usage summary:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get usage summary',
            data: {
                totalCalls: 0,
                uniqueApis: 0,
                avgCallsPerApi: 0,
                topApis: [],
                callsByDay: []
            },
            requestId
        };
    });
};

// ============================================================
// EXPORT/IMPORT ENDPOINTS
// ============================================================

/**
 * Export API configuration
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response with export data
 */
export const exportApiConfig = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/export`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformExportResponse(response);
    }).catch(error => {
        console.error('Error exporting API config:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to export API config',
            data: null,
            requestId
        };
    });
};

/**
 * Import API configuration
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} importData - Import data
 * @returns {Promise} API response
 */
export const importApiConfig = async (authorizationHeader, importData) => {
    const requestId = generateRequestId();
    
    if (!importData) {
        return Promise.reject({
            responseCode: 400,
            message: "Import data is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/import`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(importData),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error importing API config:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to import API config',
            data: null,
            requestId
        };
    });
};

/**
 * Clone an existing API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} sourceApiId - Source API ID
 * @param {Object} cloneRequest - Clone request
 * @param {string} cloneRequest.newApiName - New API name
 * @param {string} cloneRequest.newApiCode - New API code
 * @returns {Promise} API response
 */
export const cloneApi = async (authorizationHeader, sourceApiId, cloneRequest = {}) => {
    const requestId = generateRequestId();
    
    if (!sourceApiId) {
        return Promise.reject({
            responseCode: 400,
            message: "Source API ID is required",
            requestId
        });
    }
    
    if (!cloneRequest.newApiName) {
        return Promise.reject({
            responseCode: 400,
            message: "New API name is required",
            requestId
        });
    }
    
    if (!cloneRequest.newApiCode) {
        return Promise.reject({
            responseCode: 400,
            message: "New API code is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(sourceApiId)}/clone`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(cloneRequest),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error cloning API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to clone API',
            data: null,
            requestId
        };
    });
};

// ============================================================
// DELETE ENDPOINTS
// ============================================================

/**
 * Delete an API (soft delete - archive)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const deleteApi = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return {
            responseCode: 200,
            message: "API deleted successfully",
            data: { apiId },
            requestId
        };
    }).catch(error => {
        console.error('Error deleting API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to delete API',
            data: null,
            requestId
        };
    });
};

/**
 * Permanently delete an API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const permanentlyDeleteApi = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/permanent`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return {
            responseCode: 200,
            message: "API permanently deleted",
            data: { apiId },
            requestId
        };
    }).catch(error => {
        console.error('Error permanently deleting API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to permanently delete API',
            data: null,
            requestId
        };
    });
};

/**
 * Restore a deleted (archived) API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const restoreApi = async (authorizationHeader, apiId) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/gen-engine/${encodeURIComponent(apiId)}/restore`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformGeneratedApiResponse(response);
    }).catch(error => {
        console.error('Error restoring API:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to restore API',
            data: null,
            requestId
        };
    });
};

// ============================================================
// TRANSFORM FUNCTIONS
// ============================================================

/**
 * Transform generated API response
 */
const transformGeneratedApiResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        id: data.id,
        apiName: data.apiName,
        apiCode: data.apiCode,
        description: data.description,
        version: data.version,
        status: data.status || 'DRAFT',
        httpMethod: data.httpMethod,
        basePath: data.basePath,
        endpointPath: data.endpointPath,
        fullEndpoint: data.fullEndpoint || (data.basePath && data.endpointPath ? 
            `${data.basePath}${data.endpointPath}` : ''),
        category: data.category,
        owner: data.owner,
        createdAt: data.createdAt,
        updatedAt: data.updatedAt,
        createdBy: data.createdBy,
        isActive: data.isActive !== false,
        totalCalls: data.totalCalls || 0,
        lastCalledAt: data.lastCalledAt,
        tags: data.tags || [],
        parametersCount: data.parametersCount || 0,
        responseMappingsCount: data.responseMappingsCount || 0,
        headersCount: data.headersCount || 0,
        
        // Nested objects
        schemaConfig: transformSchemaConfig(data.schemaConfig),
        authConfig: transformAuthConfig(data.authConfig),
        requestConfig: transformRequestConfig(data.requestBody || data.requestConfig),
        responseConfig: transformResponseConfig(data.responseBody || data.responseConfig),
        settings: transformSettings(data.settings),
        parameters: (data.parameters || []).map(transformParameter),
        responseMappings: (data.responseMappings || []).map(transformResponseMapping),
        headers: (data.headers || []).map(transformHeader),
        
        // Metadata
        metadata: data.metadata || {},
        generatedFiles: data.generatedFiles || {}
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform schema config
 */
const transformSchemaConfig = (config) => {
    if (!config) return null;
    
    return {
        schemaName: config.schemaName,
        objectType: config.objectType,
        objectName: config.objectName,
        operation: config.operation || 'SELECT',
        primaryKeyColumn: config.primaryKeyColumn,
        sequenceName: config.sequenceName,
        enablePagination: config.enablePagination || false,
        pageSize: config.pageSize || 10,
        enableSorting: config.enableSorting || false,
        defaultSortColumn: config.defaultSortColumn,
        defaultSortDirection: config.defaultSortDirection || 'ASC',
        isSynonym: config.isSynonym || false,
        targetType: config.targetType,
        targetName: config.targetName,
        targetOwner: config.targetOwner
    };
};

/**
 * Transform auth config
 */
const transformAuthConfig = (config) => {
    if (!config) return null;
    
    return {
        authType: config.authType || 'NONE',
        apiKeyHeader: config.apiKeyHeader,
        apiKeyValue: config.apiKeyValue,
        apiKeySecret: config.apiKeySecret,
        apiKeyLocation: config.apiKeyLocation || 'header',
        apiKeyPrefix: config.apiKeyPrefix,
        basicUsername: config.basicUsername,
        basicPassword: config.basicPassword,
        basicRealm: config.basicRealm,
        jwtSecret: config.jwtSecret,
        jwtIssuer: config.jwtIssuer,
        jwtAudience: config.jwtAudience,
        jwtExpiration: config.jwtExpiration,
        jwtAlgorithm: config.jwtAlgorithm || 'HS256',
        oauthClientId: config.oauthClientId,
        oauthClientSecret: config.oauthClientSecret,
        oauthTokenUrl: config.oauthTokenUrl,
        oauthAuthUrl: config.oauthAuthUrl,
        oauthScopes: config.oauthScopes || [],
        requiredRoles: config.requiredRoles || [],
        customAuthFunction: config.customAuthFunction,
        validateSession: config.validateSession || false,
        checkObjectPrivileges: config.checkObjectPrivileges || false,
        ipWhitelist: config.ipWhitelist || [],
        rateLimitRequests: config.rateLimitRequests,
        rateLimitPeriod: config.rateLimitPeriod,
        auditLevel: config.auditLevel || 'NONE',
        corsOrigins: config.corsOrigins || [],
        corsCredentials: config.corsCredentials || false
    };
};

/**
 * Transform request config
 */
const transformRequestConfig = (config) => {
    if (!config) return null;
    
    return {
        schemaType: config.schemaType || 'application/json',
        sample: config.sample || config.example,
        maxSize: config.maxSize || 1048576, // 1MB default
        validateSchema: config.validateSchema !== false,
        allowedMediaTypes: config.allowedMediaTypes || ['application/json'],
        requiredFields: config.requiredFields || []
    };
};

/**
 * Transform response config
 */
const transformResponseConfig = (config) => {
    if (!config) return null;
    
    return {
        successSchema: config.successSchema,
        errorSchema: config.errorSchema,
        includeMetadata: config.includeMetadata || false,
        metadataFields: config.metadataFields || [],
        contentType: config.contentType || 'application/json',
        compression: config.compression || 'none'
    };
};

/**
 * Transform settings
 */
const transformSettings = (settings) => {
    if (!settings) return null;
    
    return {
        timeout: settings.timeout || 30000,
        maxRecords: settings.maxRecords || 1000,
        enableLogging: settings.enableLogging !== false,
        logLevel: settings.logLevel || 'INFO',
        enableCaching: settings.enableCaching || false,
        cacheTtl: settings.cacheTtl || 300,
        enableRateLimiting: settings.enableRateLimiting || false,
        rateLimit: settings.rateLimit || 60,
        rateLimitPeriod: settings.rateLimitPeriod || 'MINUTE',
        enableAudit: settings.enableAudit || false,
        auditLevel: settings.auditLevel || 'BASIC',
        generateSwagger: settings.generateSwagger || false,
        generatePostman: settings.generatePostman || false,
        generateClientSdk: settings.generateClientSdk || false,
        enableMonitoring: settings.enableMonitoring || false,
        enableAlerts: settings.enableAlerts || false,
        alertEmail: settings.alertEmail,
        enableTracing: settings.enableTracing || false,
        corsEnabled: settings.corsEnabled || false,
        corsOrigins: settings.corsOrigins || []
    };
};

/**
 * Transform parameter
 */
const transformParameter = (param) => {
    return {
        key: param.key,
        dbColumn: param.dbColumn || param.key,
        dbParameter: param.dbParameter || param.key,
        oracleType: param.oracleType || 'VARCHAR2',
        apiType: param.apiType || 'string',
        parameterType: param.parameterType || 'query',
        required: param.required || false,
        description: param.description || '',
        example: param.example,
        validationPattern: param.validationPattern,
        defaultValue: param.defaultValue,
        position: param.position || 0
    };
};

/**
 * Transform response mapping
 */
const transformResponseMapping = (mapping) => {
    return {
        apiField: mapping.apiField,
        dbColumn: mapping.dbColumn,
        oracleType: mapping.oracleType || 'VARCHAR2',
        apiType: mapping.apiType || 'string',
        format: mapping.format,
        nullable: mapping.nullable || true,
        isPrimaryKey: mapping.isPrimaryKey || false,
        includeInResponse: mapping.includeInResponse !== false,
        position: mapping.position || 0
    };
};

/**
 * Transform header
 */
const transformHeader = (header) => {
    return {
        key: header.key,
        value: header.value || '',
        required: header.required || false,
        description: header.description || '',
        isRequestHeader: header.isRequestHeader !== false,
        isResponseHeader: header.isResponseHeader || false
    };
};

/**
 * Transform paginated APIs response
 */
const transformPaginatedApisResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        items: (data.items || []).map(item => ({
            id: item.id,
            apiName: item.apiName,
            apiCode: item.apiCode,
            description: item.description,
            version: item.version,
            status: item.status || 'DRAFT',
            httpMethod: item.httpMethod,
            category: item.category,
            owner: item.owner,
            createdAt: item.createdAt,
            updatedAt: item.updatedAt,
            totalCalls: item.totalCalls || 0,
            lastCalledAt: item.lastCalledAt,
            tags: item.tags || [],
            parametersCount: item.parametersCount || 0
        })),
        totalCount: data.totalCount || 0,
        totalPages: data.totalPages || 1,
        page: data.page || 1,
        pageSize: data.pageSize || 10,
        hasNext: data.page < data.totalPages,
        hasPrev: data.page > 1
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform execute API response
 */
const transformExecuteApiResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        requestId: data.requestId,
        statusCode: data.statusCode || 200,
        success: data.success !== false,
        message: data.message || 'API executed successfully',
        data: data.data || data.result,
        error: data.error,
        executionTimeMs: data.executionTimeMs || 0,
        headers: data.headers || {},
        metadata: data.metadata || {}
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform API test result response
 */
const transformApiTestResultResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        testName: data.testName,
        passed: data.passed === true,
        executionTimeMs: data.executionTimeMs || 0,
        statusCode: data.statusCode,
        actualResponse: data.actualResponse,
        message: data.message || (data.passed ? 'Test passed' : 'Test failed')
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform API analytics response
 */
const transformApiAnalyticsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiId: data.apiId,
        startDate: data.startDate,
        endDate: data.endDate,
        totalCalls: data.totalCalls || 0,
        averageExecutionTimeMs: data.averageExecutionTimeMs || 0,
        totalErrors: data.totalErrors || 0,
        successRate: data.successRate || 100,
        statusDistribution: data.statusDistribution || {},
        dailyCallStats: data.dailyCallStats || {}
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform generated code response
 */
const transformGeneratedCodeResponse = (response) => {
    const data = response.data || {};
    
    // Extract API details and generated files
    const apiDetails = data.apiDetails || {};
    const generatedFiles = data.generatedFiles || {};
    
    const transformedData = {
        apiId: apiDetails.id,
        apiName: apiDetails.apiName,
        apiCode: apiDetails.apiCode,
        generatedFiles: {
            plsql: generatedFiles.plsql || '',
            openapi: generatedFiles.openapi || '',
            postman: generatedFiles.postman || ''
        }
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform code example response
 */
const transformCodeExampleResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiId: data.apiId,
        language: data.language,
        code: data.code || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform execution logs response
 */
const transformExecutionLogsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiId: data.apiId,
        logs: (data.logs || []).map(log => ({
            id: log.id,
            requestId: log.requestId,
            timestamp: log.executedAt || log.timestamp,
            executedBy: log.executedBy,
            clientIp: log.clientIp,
            userAgent: log.userAgent,
            responseStatus: log.responseStatus,
            executionTimeMs: log.executionTimeMs,
            requestParams: log.requestParams,
            requestBody: log.requestBody,
            responseBody: log.responseBody,
            errorMessage: log.errorMessage
        })),
        totalCount: data.totalCount || (data.logs || []).length
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform test results response
 */
const transformTestResultsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiId: data.apiId,
        tests: (data.tests || []).map(test => ({
            testName: test.testName,
            testType: test.testType || 'UNIT',
            passed: test.status === 'PASSED',
            executionTimeMs: test.executionTimeMs || 0,
            executedAt: test.executedAt,
            executedBy: test.executedBy,
            message: test.message || (test.status === 'PASSED' ? 'Test passed' : 'Test failed')
        })),
        totalCount: data.totalCount || (data.tests || []).length,
        summary: {
            passed: (data.tests || []).filter(t => t.status === 'PASSED').length,
            failed: (data.tests || []).filter(t => t.status === 'FAILED').length,
            total: (data.tests || []).length
        }
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform validation response
 */
const transformValidationResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        valid: data.valid === true,
        exists: data.exists === true,
        objectName: data.objectName,
        objectType: data.objectType,
        owner: data.owner,
        targetOwner: data.targetOwner,
        targetName: data.targetName,
        targetType: data.targetType,
        isSynonym: data.isSynonym || false,
        message: data.message,
        details: data.details || {}
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform related components response
 */
const transformRelatedComponentsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        codeBaseRequestId: data.codeBaseRequestId,
        collectionsCollectionId: data.collectionsCollectionId,
        documentationCollectionId: data.documentationCollectionId,
        urls: data.urls || {}
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform API versions response
 */
const transformApiVersionsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : []).map(version => ({
        id: version.id,
        version: version.version,
        status: version.status,
        createdAt: version.createdAt,
        createdBy: version.createdBy
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform categories response
 */
const transformCategoriesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : []).map(category => ({
        name: category.name,
        count: category.count || 0
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform dashboard stats response
 */
const transformDashboardStatsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        totalApis: data.totalApis || 0,
        activeApis: data.activeApis || 0,
        draftApis: data.draftApis || 0,
        deprecatedApis: data.deprecatedApis || 0,
        totalCalls: data.totalCalls || 0,
        successRate: data.successRate || 0,
        avgResponseTime: data.avgResponseTime || 0,
        byCategory: (data.byCategory || []).map(cat => ({
            category: cat.category,
            count: cat.count
        })),
        recentActivity: (data.recentActivity || []).map(activity => ({
            apiName: activity.apiName,
            apiCode: activity.apiCode,
            action: activity.action,
            timestamp: activity.timestamp,
            performedBy: activity.performedBy
        }))
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform recent activity response
 */
const transformRecentActivityResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : []).map(activity => ({
        id: activity.id,
        apiId: activity.apiId,
        apiName: activity.apiName,
        apiCode: activity.apiCode,
        action: activity.action,
        timestamp: activity.timestamp,
        performedBy: activity.performedBy,
        details: activity.details
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform usage summary response
 */
const transformUsageSummaryResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        period: data.period || 'week',
        totalCalls: data.totalCalls || 0,
        uniqueApis: data.uniqueApis || 0,
        avgCallsPerApi: data.avgCallsPerApi || 0,
        topApis: (data.topApis || []).map(api => ({
            apiId: api.apiId,
            apiName: api.apiName,
            apiCode: api.apiCode,
            calls: api.calls
        })),
        callsByDay: (data.callsByDay || []).map(day => ({
            date: day.date,
            calls: day.calls
        }))
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform export response
 */
const transformExportResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiId: data.apiId,
        apiCode: data.apiCode,
        version: data.version,
        exportedAt: data.exportedAt,
        config: data.config || {},
        filename: data.filename || `api_${data.apiCode}_export.json`,
        content: data.content
    };

    return {
        ...response,
        data: transformedData
    };
};

// ============================================================
// EXTRACT FUNCTIONS
// ============================================================

/**
 * Extract API data from response
 */
export const extractApiData = (response) => {
    return response?.data || {};
};

/**
 * Extract API list from response
 */
export const extractApiList = (response) => {
    return response?.data?.items || [];
};

/**
 * Extract generated files from response
 */
export const extractGeneratedFiles = (response) => {
    return response?.data?.generatedFiles || {};
};

/**
 * Extract code example from response
 */
export const extractCodeExample = (response) => {
    return response?.data?.code || '';
};

/**
 * Extract execution logs from response
 */
export const extractExecutionLogs = (response) => {
    return response?.data?.logs || [];
};

/**
 * Extract test results from response
 */
export const extractTestResults = (response) => {
    return response?.data || { tests: [], summary: {} };
};

/**
 * Extract validation result from response
 */
export const extractValidationResult = (response) => {
    return response?.data || { valid: false };
};

/**
 * Extract dashboard stats from response
 */
export const extractDashboardStats = (response) => {
    return response?.data || {};
};

// ============================================================
// UTILITY FUNCTIONS
// ============================================================

/**
 * Get HTTP method color
 */
export const getHttpMethodColor = (method, isDark = true) => {
    const colors = {
        'GET': isDark ? '#60A5FA' : '#3B82F6',
        'POST': isDark ? '#34D399' : '#10B981',
        'PUT': isDark ? '#FBBF24' : '#F59E0B',
        'DELETE': isDark ? '#F87171' : '#EF4444',
        'PATCH': isDark ? '#A78BFA' : '#8B5CF6',
        'HEAD': isDark ? '#94A3B8' : '#6B7280',
        'OPTIONS': isDark ? '#64748B' : '#4B5563'
    };
    return colors[method?.toUpperCase()] || (isDark ? '#94A3B8' : '#6B7280');
};

/**
 * Get status badge color
 */
export const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
        case 'ACTIVE': return 'green';
        case 'DRAFT': return 'blue';
        case 'DEPRECATED': return 'yellow';
        case 'ARCHIVED': return 'gray';
        default: return 'gray';
    }
};

/**
 * Format execution time
 */
export const formatExecutionTime = (ms) => {
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(2)}s`;
};

/**
 * Check if API can be executed
 */
export const canExecuteApi = (api) => {
    return api && api.status === 'ACTIVE';
};

/**
 * Check if API can be edited
 */
export const canEditApi = (api) => {
    return api && (api.status === 'DRAFT' || api.status === 'ACTIVE');
};

/**
 * Download generated file
 */
export const downloadGeneratedFile = (content, filename, type = 'application/json') => {
    if (!content) return;
    
    const blob = new Blob([content], { type });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
};

/**
 * Build sample request from API definition
 */
export const buildSampleRequest = (api) => {
    const request = {
        pathParams: {},
        queryParams: {},
        headers: {},
        body: null
    };
    
    if (api.parameters) {
        api.parameters.forEach(param => {
            const value = param.example || (param.apiType === 'number' ? 1 : 'sample');
            if (param.parameterType === 'path') {
                request.pathParams[param.key] = value;
            } else if (param.parameterType === 'query') {
                request.queryParams[param.key] = value;
            }
        });
    }
    
    if (api.headers) {
        api.headers.forEach(header => {
            if (header.isRequestHeader && header.value) {
                request.headers[header.key] = header.value;
            }
        });
    }
    
    if (api.requestConfig && api.requestConfig.sample) {
        try {
            request.body = JSON.parse(api.requestConfig.sample);
        } catch (e) {
            request.body = api.requestConfig.sample;
        }
    }
    
    return request;
};