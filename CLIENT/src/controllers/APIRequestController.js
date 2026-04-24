// controllers/APIRequestController.js
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

// ============================================================
// API REQUEST CONTROLLER
// ============================================================

/**
 * Capture an API request before execution
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {Object} requestDTO - API request DTO
 * @param {string} requestDTO.requestName - Request name
 * @param {string} requestDTO.description - Request description
 * @param {string} requestDTO.httpMethod - HTTP method
 * @param {string} requestDTO.url - Full URL
 * @param {string} requestDTO.basePath - Base path
 * @param {string} requestDTO.endpointPath - Endpoint path
 * @param {number} requestDTO.requestTimeoutSeconds - Request timeout
 * @param {Object} requestDTO.pathParameters - Path parameters
 * @param {Object} requestDTO.queryParameters - Query parameters
 * @param {Object} requestDTO.headers - Request headers
 * @param {Object} requestDTO.requestBody - Request body
 * @param {Object} requestDTO.formData - Form data
 * @param {Object} requestDTO.multipartData - Multipart data
 * @param {string} requestDTO.authType - Authentication type
 * @param {string} requestDTO.authToken - Auth token
 * @param {string} requestDTO.apiKey - API key
 * @param {string} requestDTO.clientIpAddress - Client IP
 * @param {string} requestDTO.userAgent - User agent
 * @param {string} requestDTO.sourceApplication - Source application
 * @param {string} requestDTO.requestedBy - Requested by
 * @param {string} requestDTO.correlationId - Correlation ID
 * @param {boolean} requestDTO.isMockRequest - Is mock request
 * @param {Object} requestDTO.metadata - Metadata
 * @param {Array} requestDTO.tags - Tags
 * @returns {Promise} API response
 */
export const captureRequest = async (authorizationHeader, apiId, requestDTO = {}) => {
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
        (authHeader) => apiCall(`/requests/capture/${encodeURIComponent(apiId)}`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(requestDTO),
            requestId: requestId
        })
    ).then(response => {
        return transformApiRequestResponse(response);
    }).catch(error => {
        console.error('Error capturing request:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to capture request',
            data: null,
            requestId
        };
    });
};

/**
 * Capture a request from execution details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {Object} executeRequest - Execute request DTO
 * @param {string} executeRequest.requestId - Request ID
 * @param {string} executeRequest.httpMethod - HTTP method
 * @param {string} executeRequest.url - URL
 * @param {Object} executeRequest.pathParams - Path parameters
 * @param {Object} executeRequest.queryParams - Query parameters
 * @param {Object} executeRequest.headers - Headers
 * @param {Object} executeRequest.body - Request body
 * @param {number} executeRequest.timeoutSeconds - Timeout seconds
 * @param {Object} executeRequest.metadata - Metadata
 * @returns {Promise} API response
 */
export const captureRequestFromExecution = async (authorizationHeader, apiId, executeRequest = {}) => {
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
        (authHeader) => apiCall(`/requests/capture/${encodeURIComponent(apiId)}/from-execution`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(executeRequest),
            requestId: requestId
        })
    ).then(response => {
        return transformApiRequestResponse(response);
    }).catch(error => {
        console.error('Error capturing request from execution:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to capture request from execution',
            data: null,
            requestId
        };
    });
};

/**
 * Update a captured request with response details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} capturedRequestId - Captured request ID
 * @param {Object} responseRequest - Update response request
 * @param {number} responseRequest.statusCode - Response status code
 * @param {string} responseRequest.message - Response message
 * @param {Object} responseRequest.data - Response data
 * @param {number} responseRequest.executionDurationMs - Execution duration
 * @returns {Promise} API response
 */
export const updateRequestWithResponse = async (authorizationHeader, capturedRequestId, responseRequest = {}) => {
    const requestId = generateRequestId();
    
    if (!capturedRequestId) {
        return Promise.reject({
            responseCode: 400,
            message: "Captured request ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/${encodeURIComponent(capturedRequestId)}/response`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(responseRequest),
            requestId: requestId
        })
    ).then(response => {
        return transformApiRequestResponse(response);
    }).catch(error => {
        console.error('Error updating request with response:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to update request with response',
            data: null,
            requestId
        };
    });
};

/**
 * Update a captured request with error details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} capturedRequestId - Captured request ID
 * @param {Object} errorRequest - Update error request
 * @param {number} errorRequest.statusCode - Error status code
 * @param {string} errorRequest.errorMessage - Error message
 * @param {number} errorRequest.executionDurationMs - Execution duration
 * @returns {Promise} API response
 */
export const updateRequestWithError = async (authorizationHeader, capturedRequestId, errorRequest = {}) => {
    const requestId = generateRequestId();
    
    if (!capturedRequestId) {
        return Promise.reject({
            responseCode: 400,
            message: "Captured request ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/${encodeURIComponent(capturedRequestId)}/error`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(errorRequest),
            requestId: requestId
        })
    ).then(response => {
        return transformApiRequestResponse(response);
    }).catch(error => {
        console.error('Error updating request with error:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to update request with error',
            data: null,
            requestId
        };
    });
};

/**
 * Batch update multiple requests with responses
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} requestIdToResponseMap - Map of request IDs to responses
 * @returns {Promise} API response
 */
export const batchUpdateResponses = async (authorizationHeader, requestIdToResponseMap = {}) => {
    const requestId = generateRequestId();
    
    if (Object.keys(requestIdToResponseMap).length === 0) {
        return Promise.reject({
            responseCode: 400,
            message: "Request ID to response map is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/batch/update-responses`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(requestIdToResponseMap),
            requestId: requestId
        })
    ).then(response => {
        return transformBatchUpdateResponse(response);
    }).catch(error => {
        console.error('Error batch updating responses:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to batch update responses',
            data: null,
            requestId
        };
    });
};

/**
 * Get request by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} capturedRequestId - Captured request ID
 * @returns {Promise} API response
 */
export const getRequestById = async (authorizationHeader, capturedRequestId) => {
    const requestId = generateRequestId();
    
    if (!capturedRequestId) {
        return Promise.reject({
            responseCode: 400,
            message: "Captured request ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/${encodeURIComponent(capturedRequestId)}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformApiRequestResponse(response);
    }).catch(error => {
        console.error('Error getting request by ID:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get request by ID',
            data: null,
            requestId
        };
    });
};

/**
 * Get request by correlation ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} correlationId - Correlation ID
 * @returns {Promise} API response
 */
export const getRequestByCorrelationId = async (authorizationHeader, correlationId) => {
    const requestId = generateRequestId();
    
    if (!correlationId) {
        return Promise.reject({
            responseCode: 400,
            message: "Correlation ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/correlation/${encodeURIComponent(correlationId)}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformApiRequestResponse(response);
    }).catch(error => {
        console.error('Error getting request by correlation ID:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get request by correlation ID',
            data: null,
            requestId
        };
    });
};

/**
 * Get requests by API ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {number} limit - Maximum number of requests (default: 100)
 * @returns {Promise} API response
 */
export const getRequestsByApiId = async (authorizationHeader, apiId, limit = 100) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ limit });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/api/${encodeURIComponent(apiId)}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformApiRequestsListResponse(response);
    }).catch(error => {
        console.error('Error getting requests by API ID:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get requests by API ID',
            data: { requests: [], totalCount: 0 },
            requestId
        };
    });
};

/**
 * Search requests with advanced filtering
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filter - Search filter
 * @param {string} filter.apiId - API ID
 * @param {string} filter.apiCode - API code
 * @param {string} filter.requestName - Request name
 * @param {string} filter.httpMethod - HTTP method
 * @param {string} filter.requestStatus - Request status
 * @param {Array} filter.responseStatusCodes - Response status codes
 * @param {string} filter.correlationId - Correlation ID
 * @param {string} filter.fromDate - From date
 * @param {string} filter.toDate - To date
 * @param {number} filter.minDuration - Minimum duration
 * @param {number} filter.maxDuration - Maximum duration
 * @param {boolean} filter.hasError - Has error
 * @param {string} filter.clientIpAddress - Client IP
 * @param {string} filter.userAgent - User agent
 * @param {string} filter.sourceApplication - Source application
 * @param {string} filter.requestedBy - Requested by
 * @param {boolean} filter.isMockRequest - Is mock request
 * @param {string} filter.authType - Auth type
 * @param {Array} filter.tags - Tags
 * @param {number} filter.page - Page number
 * @param {number} filter.size - Page size
 * @param {string} filter.sortBy - Sort by field
 * @param {string} filter.sortDirection - Sort direction
 * @returns {Promise} API response
 */
export const searchRequests = async (authorizationHeader, filter = {}) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/search`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(filter),
            requestId: requestId
        })
    ).then(response => {
        return transformSearchRequestsResponse(response);
    }).catch(error => {
        console.error('Error searching requests:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to search requests',
            data: {
                content: [],
                totalElements: 0,
                totalPages: 0,
                currentPage: 0,
                pageSize: 10
            },
            requestId
        };
    });
};

/**
 * Get request statistics for an API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {string} fromDate - From date (ISO format)
 * @param {string} toDate - To date (ISO format)
 * @returns {Promise} API response
 */
export const getRequestStatistics = async (authorizationHeader, apiId, fromDate, toDate) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ fromDate, toDate });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/statistics/api/${encodeURIComponent(apiId)}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformStatisticsResponse(response);
    }).catch(error => {
        console.error('Error getting request statistics:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get request statistics',
            data: {
                totalRequests: 0,
                successfulRequests: 0,
                failedRequests: 0,
                averageResponseTime: 0
            },
            requestId
        };
    });
};

/**
 * Get system-wide request statistics
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fromDate - From date (ISO format)
 * @param {string} toDate - To date (ISO format)
 * @returns {Promise} API response
 */
export const getSystemStatistics = async (authorizationHeader, fromDate, toDate) => {
    const requestId = generateRequestId();
    
    const queryParams = buildQueryParams({ fromDate, toDate });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/statistics/system${queryParams.toString() ? `?${queryParams.toString()}` : ''}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformStatisticsResponse(response);
    }).catch(error => {
        console.error('Error getting system statistics:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get system statistics',
            data: {
                totalRequests: 0,
                successfulRequests: 0,
                failedRequests: 0,
                averageResponseTime: 0
            },
            requestId
        };
    });
};

/**
 * Get daily breakdown of requests for an API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {string} fromDate - From date (ISO format)
 * @param {string} toDate - To date (ISO format)
 * @returns {Promise} API response
 */
export const getDailyBreakdown = async (authorizationHeader, apiId, fromDate, toDate) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    if (!fromDate || !toDate) {
        return Promise.reject({
            responseCode: 400,
            message: "From date and to date are required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ fromDate, toDate });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/statistics/api/${encodeURIComponent(apiId)}/daily-breakdown?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDailyBreakdownResponse(response);
    }).catch(error => {
        console.error('Error getting daily breakdown:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get daily breakdown',
            data: {},
            requestId
        };
    });
};

/**
 * Delete a captured request
 * @param {string} authorizationHeader - Bearer token
 * @param {string} capturedRequestId - Captured request ID
 * @returns {Promise} API response
 */
export const deleteRequest = async (authorizationHeader, capturedRequestId) => {
    const requestId = generateRequestId();
    
    if (!capturedRequestId) {
        return Promise.reject({
            responseCode: 400,
            message: "Captured request ID is required",
            requestId
        });
    }
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/${encodeURIComponent(capturedRequestId)}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return {
            responseCode: 200,
            message: "Request deleted successfully",
            data: { deletedRequestId: capturedRequestId },
            requestId
        };
    }).catch(error => {
        console.error('Error deleting request:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to delete request',
            data: null,
            requestId
        };
    });
};

/**
 * Clean up old requests
 * @param {string} authorizationHeader - Bearer token
 * @param {string} beforeDate - Delete requests older than this date
 * @returns {Promise} API response
 */
export const cleanupOldRequests = async (authorizationHeader, beforeDate) => {
    const requestId = generateRequestId();
    
    if (!beforeDate) {
        return Promise.reject({
            responseCode: 400,
            message: "Before date is required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ beforeDate });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/cleanup?${queryParams.toString()}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformCleanupResponse(response);
    }).catch(error => {
        console.error('Error cleaning up old requests:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to clean up old requests',
            data: { deletedCount: 0 },
            requestId
        };
    });
};

/**
 * Export requests for an API within a date range
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @param {string} fromDate - From date (ISO format)
 * @param {string} toDate - To date (ISO format)
 * @param {string} format - Export format (JSON, CSV, XML, EXCEL)
 * @param {Object} config - Export configuration
 * @returns {Promise} API response
 */
export const exportRequests = async (authorizationHeader, apiId, fromDate, toDate, format = 'JSON', config = null) => {
    const requestId = generateRequestId();
    
    if (!apiId) {
        return Promise.reject({
            responseCode: 400,
            message: "API ID is required",
            requestId
        });
    }
    
    if (!fromDate || !toDate) {
        return Promise.reject({
            responseCode: 400,
            message: "From date and to date are required",
            requestId
        });
    }
    
    const queryParams = buildQueryParams({ fromDate, toDate, format });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/export/api/${encodeURIComponent(apiId)}?${queryParams.toString()}`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: config ? JSON.stringify(config) : null,
            requestId: requestId
        })
    ).then(response => {
        return transformExportResponse(response);
    }).catch(error => {
        console.error('Error exporting requests:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to export requests',
            data: null,
            requestId
        };
    });
};

// ============================================================
// DASHBOARD/ANALYTICS ENDPOINTS
// ============================================================

/**
 * Get request dashboard statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getRequestDashboardStats = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/dashboard/stats`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDashboardStatsResponse(response);
    }).catch(error => {
        console.error('Error getting request dashboard stats:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get request dashboard stats',
            data: {
                totalRequests: 0,
                successRate: 0,
                avgResponseTime: 0,
                activeApis: 0
            },
            requestId
        };
    });
};

/**
 * Get recent requests
 * @param {string} authorizationHeader - Bearer token
 * @param {number} limit - Number of requests (default: 10)
 * @returns {Promise} API response
 */
export const getRecentRequests = async (authorizationHeader, limit = 10) => {
    const requestId = generateRequestId();
    
    const queryParams = buildQueryParams({ limit });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/requests/recent?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformRecentRequestsResponse(response);
    }).catch(error => {
        console.error('Error getting recent requests:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get recent requests',
            data: [],
            requestId
        };
    });
};

/**
 * Get request health status for an API
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - API ID
 * @returns {Promise} API response
 */
export const getRequestHealth = async (authorizationHeader, apiId) => {
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
        (authHeader) => apiCall(`/requests/health/${encodeURIComponent(apiId)}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformHealthResponse(response);
    }).catch(error => {
        console.error('Error getting request health:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to get request health',
            data: { overallHealth: 'UNKNOWN' },
            requestId
        };
    });
};

// ============================================================
// TRANSFORM FUNCTIONS
// ============================================================

/**
 * Transform API request response
 */
const transformApiRequestResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        id: data.id,
        apiId: data.apiId,
        apiName: data.apiName,
        apiCode: data.apiCode,
        requestName: data.requestName,
        correlationId: data.correlationId,
        
        // Request details
        httpMethod: data.httpMethod,
        url: data.url,
        basePath: data.basePath,
        endpointPath: data.endpointPath,
        requestTimeoutSeconds: data.requestTimeoutSeconds,
        
        // Request components
        pathParameters: data.pathParameters || {},
        queryParameters: data.queryParameters || {},
        headers: data.headers || {},
        requestBody: data.requestBody,
        formData: data.formData,
        multipartData: data.multipartData,
        
        // ============ ADD RAW FIELDS ============
        rawRequestBody: data.rawRequestBody,     // Exact raw request body
        rawResponseBody: data.rawResponseBody,   // Exact raw response body
        
        // Response details
        responseStatusCode: data.responseStatusCode,
        responseStatusMessage: data.responseStatusMessage,
        responseBody: data.responseBody,
        responseHeaders: data.responseHeaders || {},
        responseSizeBytes: data.responseSizeBytes,
        
        // Timing
        requestTimestamp: data.requestTimestamp,
        responseTimestamp: data.responseTimestamp,
        executionDurationMs: data.executionDurationMs,
        formattedDuration: data.formattedDuration,
        
        // Status
        requestStatus: data.requestStatus,
        errorMessage: data.errorMessage,
        retryCount: data.retryCount || 0,
        
        // Authentication
        authType: data.authType,
        isAuthenticated: data.isAuthenticated || false,
        
        // Client info
        clientIpAddress: data.clientIpAddress,
        userAgent: data.userAgent,
        sourceApplication: data.sourceApplication,
        requestedBy: data.requestedBy,
        
        // Additional
        isMockRequest: data.isMockRequest || false,
        curlCommand: data.curlCommand,
        metadata: data.metadata || {},
        
        // Audit
        createdAt: data.createdAt,
        createdBy: data.createdBy,
        
        // Summary
        summary: data.summary || {
            totalRequestsForApi: 0,
            successfulRequests: 0,
            failedRequests: 0,
            averageResponseTime: 0
        }
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform API requests list response
 */
const transformApiRequestsListResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiId: data.apiId,
        requests: (data.requests || []).map(req => ({
            id: req.id,
            requestName: req.requestName,
            correlationId: req.correlationId,
            httpMethod: req.httpMethod,
            url: req.url,
            responseStatusCode: req.responseStatusCode,
            executionDurationMs: req.executionDurationMs,
            formattedDuration: req.formattedDuration,
            requestStatus: req.requestStatus,
            requestTimestamp: req.requestTimestamp,
            responseTimestamp: req.responseTimestamp,
            errorMessage: req.errorMessage,
            requestedBy: req.requestedBy,
            isMockRequest: req.isMockRequest || false
        })),
        totalCount: data.totalCount || 0
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform search requests response
 */
const transformSearchRequestsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        content: (data.content || []).map(req => ({
            id: req.id,
            apiId: req.apiId,
            apiName: req.apiName,
            apiCode: req.apiCode,
            requestName: req.requestName,
            correlationId: req.correlationId,
            httpMethod: req.httpMethod,
            url: req.url,
            responseStatusCode: req.responseStatusCode,
            executionDurationMs: req.executionDurationMs,
            formattedDuration: req.formattedDuration,
            requestStatus: req.requestStatus,
            requestTimestamp: req.requestTimestamp,
            responseTimestamp: req.responseTimestamp,
            errorMessage: req.errorMessage,
            requestedBy: req.requestedBy,
            clientIpAddress: req.clientIpAddress,
            sourceApplication: req.sourceApplication,
            isMockRequest: req.isMockRequest || false
        })),
        // ADD THIS LINE - preserve apiSummaries from the response
        apiSummaries: data.apiSummaries || [],
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        currentPage: data.currentPage || 0,
        pageSize: data.pageSize || 20
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform batch update response
 */
const transformBatchUpdateResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        updatedCount: data.updatedCount || 0,
        requests: (data.requests || []).map(req => ({
            id: req.id,
            requestName: req.requestName,
            correlationId: req.correlationId,
            requestStatus: req.requestStatus,
            responseStatusCode: req.responseStatusCode,
            executionDurationMs: req.executionDurationMs
        }))
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform statistics response
 */
const transformStatisticsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        fromDate: data.fromDate,
        toDate: data.toDate,
        period: data.period || 'CUSTOM',
        
        // Overall stats
        totalRequests: data.totalRequests || 0,
        successfulRequests: data.successfulRequests || 0,
        failedRequests: data.failedRequests || 0,
        pendingRequests: data.pendingRequests || 0,
        timeoutRequests: data.timeoutRequests || 0,
        successRate: data.successRate || 0,
        failureRate: data.failureRate || 0,
        
        // Performance
        averageResponseTime: data.averageResponseTime || 0,
        minResponseTime: data.minResponseTime || 0,
        maxResponseTime: data.maxResponseTime || 0,
        medianResponseTime: data.medianResponseTime || 0,
        p95ResponseTime: data.p95ResponseTime || 0,
        p99ResponseTime: data.p99ResponseTime || 0,
        
        // Distributions
        statusCodeDistribution: data.statusCodeDistribution || {},
        methodDistribution: data.methodDistribution || {},
        statusDistribution: data.statusDistribution || {},
        
        // Time series
        timeSeriesData: (data.timeSeriesData || []).map(point => ({
            timestamp: point.timestamp,
            requestCount: point.requestCount || 0,
            successCount: point.successCount || 0,
            failureCount: point.failureCount || 0,
            averageResponseTime: point.averageResponseTime || 0
        }))
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform daily breakdown response
 */
const transformDailyBreakdownResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {};
    Object.keys(data).forEach(date => {
        transformedData[date] = data[date];
    });

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform cleanup response
 */
const transformCleanupResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        deletedCount: data.deletedCount || 0,
        beforeDate: data.beforeDate,
        message: data.message || `Deleted ${data.deletedCount || 0} old requests`
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
        exportId: data.exportId,
        exportTimestamp: data.exportTimestamp,
        exportedBy: data.exportedBy,
        format: data.format || 'JSON',
        config: data.config || {},
        recordCount: data.recordCount || 0,
        fileSize: data.fileSize,
        fileExtension: data.fileExtension,
        contentType: data.contentType,
        downloadUrl: data.downloadUrl
    };

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
        totalRequests: data.totalRequests || 0,
        successfulRequests: data.successfulRequests || 0,
        failedRequests: data.failedRequests || 0,
        successRate: data.successRate || 0,
        averageResponseTime: data.averageResponseTime || 0,
        activeApis: data.activeApis || 0,
        uniqueClients: data.uniqueClients || 0,
        requestsByStatus: data.requestsByStatus || {},
        requestsByMethod: data.requestsByMethod || {},
        topApis: (data.topApis || []).map(api => ({
            apiId: api.apiId,
            apiName: api.apiName,
            apiCode: api.apiCode,
            requestCount: api.requestCount || 0
        }))
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform recent requests response
 */
const transformRecentRequestsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : []).map(req => ({
        id: req.id,
        apiId: req.apiId,
        apiName: req.apiName,
        apiCode: req.apiCode,
        requestName: req.requestName,
        correlationId: req.correlationId,
        httpMethod: req.httpMethod,
        responseStatusCode: req.responseStatusCode,
        requestStatus: req.requestStatus,
        executionDurationMs: req.executionDurationMs,
        formattedDuration: req.formattedDuration,
        requestTimestamp: req.requestTimestamp,
        requestedBy: req.requestedBy
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform health response
 */
const transformHealthResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiId: data.apiId,
        apiName: data.apiName,
        checkTime: data.checkTime,
        overallHealth: data.overallHealth || 'UNKNOWN',
        metrics: data.metrics || {
            successRate: 0,
            averageResponseTime: 0,
            errorRate: 0,
            totalRequestsLastHour: 0
        },
        checks: (data.checks || []).map(check => ({
            checkName: check.checkName,
            status: check.status || 'UNKNOWN',
            message: check.message,
            value: check.value,
            threshold: check.threshold
        })),
        activeAlerts: (data.activeAlerts || []).map(alert => ({
            alertId: alert.alertId,
            severity: alert.severity,
            message: alert.message,
            triggeredAt: alert.triggeredAt
        }))
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
 * Extract request data from response
 */
export const extractRequestData = (response) => {
    return response?.data || {};
};

/**
 * Extract requests list from response
 */
export const extractRequestsList = (response) => {
    return response?.data?.requests || [];
};

/**
 * Extract search results from response
 */
export const extractSearchResults = (response) => {
    return response?.data?.content || [];
};

/**
 * Extract statistics from response
 */
export const extractStatistics = (response) => {
    return response?.data || {};
};

/**
 * Extract daily breakdown from response
 */
export const extractDailyBreakdown = (response) => {
    return response?.data || {};
};

/**
 * Extract export data from response
 */
export const extractExportData = (response) => {
    return response?.data || {};
};

// ============================================================
// UTILITY FUNCTIONS
// ============================================================

/**
 * Get request status color
 */
export const getRequestStatusColor = (status) => {
    switch (status?.toUpperCase()) {
        case 'SUCCESS': return 'green';
        case 'FAILED': return 'red';
        case 'TIMEOUT': return 'orange';
        case 'PENDING': return 'blue';
        default: return 'gray';
    }
};

/**
 * Get status code color
 */
export const getStatusCodeColor = (code) => {
    if (!code) return 'gray';
    if (code >= 200 && code < 300) return 'green';
    if (code >= 300 && code < 400) return 'blue';
    if (code >= 400 && code < 500) return 'orange';
    if (code >= 500) return 'red';
    return 'gray';
};

/**
 * Format execution time
 */
export const formatExecutionTime = (ms) => {
    if (!ms) return '0ms';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    const minutes = Math.floor(ms / 60000);
    const seconds = ((ms % 60000) / 1000).toFixed(0);
    return `${minutes}m ${seconds}s`;
};

/**
 * Format timestamp
 */
export const formatRequestTimestamp = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleString();
};

/**
 * Get request summary text
 */
export const getRequestSummary = (request) => {
    if (!request) return '';
    return `${request.httpMethod} ${request.url} - ${request.responseStatusCode || 'Pending'}`;
};

/**
 * Check if request was successful
 */
export const isRequestSuccessful = (request) => {
    return request?.responseStatusCode >= 200 && request?.responseStatusCode < 300;
};

/**
 * Check if request failed
 */
export const isRequestFailed = (request) => {
    return request?.responseStatusCode >= 400 || request?.requestStatus === 'FAILED';
};

/**
 * Download exported requests
 */
export const downloadExportedRequests = (exportData) => {
    if (!exportData || !exportData.downloadUrl) return;
    
    window.open(exportData.downloadUrl, '_blank');
};

/**
 * Build filter from query parameters
 */
export const buildFilterFromQuery = (queryParams = {}) => {
    return {
        apiId: queryParams.apiId,
        apiCode: queryParams.apiCode,
        requestName: queryParams.requestName,
        httpMethod: queryParams.httpMethod,
        requestStatus: queryParams.status,
        correlationId: queryParams.correlationId,
        fromDate: queryParams.fromDate,
        toDate: queryParams.toDate,
        requestedBy: queryParams.requestedBy,
        sourceApplication: queryParams.source,
        page: queryParams.page ? parseInt(queryParams.page) : 0,
        size: queryParams.size ? parseInt(queryParams.size) : 10,
        sortBy: queryParams.sortBy || 'requestTimestamp',
        sortDirection: queryParams.sortDir || 'DESC'
    };
};