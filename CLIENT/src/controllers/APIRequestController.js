// controllers/APIRequestController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper.js";

// ============================================================
// HELPER FUNCTIONS
// ============================================================

/**
 * Get authorization headers - FIXED
 * @param {string} authHeader - Authorization header (can be raw token or Bearer token)
 * @returns {Object} Headers object
 */
const getAuthHeaders = (authHeader) => {
    const headers = {
        "Content-Type": "application/json",
        "x-api-key": API_CONFIG.HEADERS["x-api-key"],
        "x-api-secret": API_CONFIG.HEADERS["x-api-secret"]
    };
    
    if (!authHeader) {
        console.warn('⚠️ No authorization header provided');
        return headers;
    }
    
    // Clean the token - remove 'Bearer ' if present
    let cleanToken = authHeader;
    if (authHeader.startsWith('Bearer ')) {
        cleanToken = authHeader.substring(7);
    }
    
    headers["Authorization"] = `Bearer ${cleanToken}`;
    
    return headers;
};

/**
 * Build query parameters
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
 */
const generateRequestId = () => {
    return 'req_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// ============================================================
// CORE API FUNCTIONS
// ============================================================

/**
 * Search requests with advanced filtering
 */
export const searchRequests = async (authorizationHeader, filter = {}) => {
    const requestId = generateRequestId();
    
    if (!authorizationHeader) {
        console.error('No authorization header provided');
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: {
                content: [],
                totalElements: 0,
                totalPages: 0,
                currentPage: 0,
                pageSize: 10,
                apiSummaries: []
            },
            requestId
        };
    }
    
    try {
        const response = await apiCall(`/requests/search`, {
            method: 'POST',
            headers: getAuthHeaders(authorizationHeader),
            body: JSON.stringify(filter)
        });
        
        return transformSearchRequestsResponse(response);
    } catch (error) {
        console.error('Error searching requests:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to search requests',
            data: {
                content: [],
                totalElements: 0,
                totalPages: 0,
                currentPage: 0,
                pageSize: 10,
                apiSummaries: []
            },
            requestId
        };
    }
};


/**
 * Get dashboard statistics - Uses the correct DashboardController endpoint
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardStats = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    if (!authorizationHeader) {
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: {
                totalApis: 0,
                totalCollections: 0,
                totalApiRequests: 0,
                totalDocumentationEndpoints: 0,
                activeApis: 0,
                totalCodeImplementations: 0,
                publishedDocumentation: 0,
                activeUsers: 0,
                totalIpWhitelistEntries: 0,
                unreadSecurityAlerts: 0
            },
            requestId
        };
    }
    
    try {
        const headers = getAuthHeaders(authorizationHeader);
        
        // Use the correct endpoint from DashboardController
        const response = await apiCall(`/dashboard/stats`, {
            method: 'GET',
            headers: headers,
            skipAuthRedirect: true
        });
        
        return transformDashboardStatsResponse(response);
    } catch (error) {
        console.error('Error getting dashboard stats:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to get dashboard stats',
            data: {
                totalApis: 0,
                totalCollections: 0,
                totalApiRequests: 0,
                totalDocumentationEndpoints: 0,
                activeApis: 0,
                totalCodeImplementations: 0,
                publishedDocumentation: 0,
                activeUsers: 0,
                totalIpWhitelistEntries: 0,
                unreadSecurityAlerts: 0
            },
            requestId
        };
    }
};


/**
 * Get request by ID
 */
export const getRequestById = async (authorizationHeader, capturedRequestId) => {
    const requestId = generateRequestId();
    
    if (!authorizationHeader) {
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: null,
            requestId
        };
    }
    
    if (!capturedRequestId) {
        return {
            responseCode: 400,
            message: "Captured request ID is required",
            data: null,
            requestId
        };
    }
    
    try {
        const response = await apiCall(`/requests/${encodeURIComponent(capturedRequestId)}`, {
            method: 'GET',
            headers: getAuthHeaders(authorizationHeader)
        });
        
        return transformApiRequestResponse(response);
    } catch (error) {
        console.error('Error getting request by ID:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to get request by ID',
            data: null,
            requestId
        };
    }
};

/**
 * Get request dashboard statistics - FIXED (single definition)
 */
export const getRequestDashboardStats = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    console.log('📊 getRequestDashboardStats called, auth header present:', !!authorizationHeader);
    
    if (!authorizationHeader) {
        console.error('No authorization header provided for dashboard stats');
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: {
                totalRequests: 0,
                successRate: 0,
                averageResponseTime: 0,
                activeApis: 0
            },
            requestId
        };
    }
    
    try {
        const headers = getAuthHeaders(authorizationHeader);
        console.log('📊 Dashboard stats headers:', Object.keys(headers));
        
        const response = await apiCall(`/requests/dashboard/stats`, {
            method: 'GET',
            headers: headers
        });
        
        console.log('📊 Dashboard stats response code:', response?.responseCode);
        
        return transformDashboardStatsResponse(response);
    } catch (error) {
        console.error('Error getting request dashboard stats:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to get request dashboard stats',
            data: {
                totalRequests: 0,
                successRate: 0,
                averageResponseTime: 0,
                activeApis: 0
            },
            requestId
        };
    }
};

/**
 * Get system-wide request statistics
 */
export const getSystemStatistics = async (authorizationHeader, fromDate, toDate) => {
    const requestId = generateRequestId();
    
    if (!authorizationHeader) {
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: {
                totalRequests: 0,
                successfulRequests: 0,
                failedRequests: 0,
                averageResponseTime: 0
            },
            requestId
        };
    }
    
    const queryParams = buildQueryParams({ fromDate, toDate });
    const queryString = queryParams.toString();
    
    try {
        const response = await apiCall(`/requests/statistics/system${queryString ? `?${queryString}` : ''}`, {
            method: 'GET',
            headers: getAuthHeaders(authorizationHeader)
        });
        
        return transformStatisticsResponse(response);
    } catch (error) {
        console.error('Error getting system statistics:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to get system statistics',
            data: {
                totalRequests: 0,
                successfulRequests: 0,
                failedRequests: 0,
                averageResponseTime: 0
            },
            requestId
        };
    }
};

/**
 * Get recent requests
 */
export const getRecentRequests = async (authorizationHeader, limit = 10) => {
    const requestId = generateRequestId();
    
    if (!authorizationHeader) {
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: [],
            requestId
        };
    }
    
    const queryParams = buildQueryParams({ limit });
    
    try {
        const response = await apiCall(`/requests/recent?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authorizationHeader)
        });
        
        return transformRecentRequestsResponse(response);
    } catch (error) {
        console.error('Error getting recent requests:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to get recent requests',
            data: [],
            requestId
        };
    }
};

/**
 * Export requests
 */
export const exportRequests = async (authorizationHeader, apiId, fromDate, toDate, format = 'JSON', config = null) => {
    const requestId = generateRequestId();
    
    if (!authorizationHeader) {
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: null,
            requestId
        };
    }
    
    if (!apiId) {
        return {
            responseCode: 400,
            message: "API ID is required",
            data: null,
            requestId
        };
    }
    
    const queryParams = buildQueryParams({ fromDate, toDate, format });
    
    try {
        const response = await apiCall(`/requests/export/api/${encodeURIComponent(apiId)}?${queryParams.toString()}`, {
            method: 'POST',
            headers: getAuthHeaders(authorizationHeader),
            body: config ? JSON.stringify(config) : null
        });
        
        return transformExportResponse(response);
    } catch (error) {
        console.error('Error exporting requests:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to export requests',
            data: null,
            requestId
        };
    }
};

/**
 * Delete a captured request
 */
export const deleteRequest = async (authorizationHeader, capturedRequestId) => {
    const requestId = generateRequestId();
    
    if (!authorizationHeader) {
        return {
            responseCode: 401,
            message: 'Authentication required',
            data: null,
            requestId
        };
    }
    
    if (!capturedRequestId) {
        return {
            responseCode: 400,
            message: "Captured request ID is required",
            data: null,
            requestId
        };
    }
    
    try {
        await apiCall(`/requests/${encodeURIComponent(capturedRequestId)}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authorizationHeader)
        });
        
        return {
            responseCode: 200,
            message: "Request deleted successfully",
            data: { deletedRequestId: capturedRequestId },
            requestId
        };
    } catch (error) {
        console.error('Error deleting request:', error);
        return {
            responseCode: error.status || 500,
            message: error.message || 'Failed to delete request',
            data: null,
            requestId
        };
    }
};

// ============================================================
// TRANSFORM FUNCTIONS
// ============================================================

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
            isMockRequest: req.isMockRequest || false,
            pathParameters: req.pathParameters || {},
            queryParameters: req.queryParameters || {},
            headers: req.headers || {},
            requestBody: req.requestBody,
            responseBody: req.responseBody
        })),
        apiSummaries: data.apiSummaries || [],
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        currentPage: data.currentPage || 0,
        pageSize: data.pageSize || 20
    };

    return { ...response, data: transformedData };
};

const transformApiRequestResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        id: data.id,
        apiId: data.apiId,
        apiName: data.apiName,
        apiCode: data.apiCode,
        requestName: data.requestName,
        correlationId: data.correlationId,
        httpMethod: data.httpMethod,
        url: data.url,
        basePath: data.basePath,
        endpointPath: data.endpointPath,
        requestTimeoutSeconds: data.requestTimeoutSeconds,
        pathParameters: data.pathParameters || {},
        queryParameters: data.queryParameters || {},
        headers: data.headers || {},
        requestBody: data.requestBody,
        rawRequestBody: data.rawRequestBody,
        rawResponseBody: data.rawResponseBody,
        responseStatusCode: data.responseStatusCode,
        responseStatusMessage: data.responseStatusMessage,
        responseBody: data.responseBody,
        responseHeaders: data.responseHeaders || {},
        responseSizeBytes: data.responseSizeBytes,
        requestTimestamp: data.requestTimestamp,
        responseTimestamp: data.responseTimestamp,
        executionDurationMs: data.executionDurationMs,
        formattedDuration: data.formattedDuration,
        requestStatus: data.requestStatus,
        errorMessage: data.errorMessage,
        retryCount: data.retryCount || 0,
        authType: data.authType,
        isAuthenticated: data.isAuthenticated || false,
        clientIpAddress: data.clientIpAddress,
        userAgent: data.userAgent,
        sourceApplication: data.sourceApplication,
        requestedBy: data.requestedBy,
        isMockRequest: data.isMockRequest || false,
        curlCommand: data.curlCommand,
        metadata: data.metadata || {},
        createdAt: data.createdAt,
        createdBy: data.createdBy
    };

    return { ...response, data: transformedData };
};

const transformStatisticsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        totalRequests: data.totalRequests || 0,
        successfulRequests: data.successfulRequests || 0,
        failedRequests: data.failedRequests || 0,
        pendingRequests: data.pendingRequests || 0,
        timeoutRequests: data.timeoutRequests || 0,
        successRate: data.successRate || 0,
        failureRate: data.failureRate || 0,
        averageResponseTime: data.averageResponseTime || 0,
        minResponseTime: data.minResponseTime || 0,
        maxResponseTime: data.maxResponseTime || 0,
        medianResponseTime: data.medianResponseTime || 0,
        statusCodeDistribution: data.statusCodeDistribution || {},
        methodDistribution: data.methodDistribution || {},
        statusDistribution: data.statusDistribution || {},
        timeSeriesData: (data.timeSeriesData || []).map(point => ({
            timestamp: point.timestamp,
            requestCount: point.requestCount || 0,
            successCount: point.successCount || 0,
            failureCount: point.failureCount || 0,
            averageResponseTime: point.averageResponseTime || 0
        }))
    };

    return { ...response, data: transformedData };
};

const transformDashboardStatsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        // Stats card data
        totalApis: data.totalApis || 0,
        totalCollections: data.totalCollections || 0,
        totalApiRequests: data.totalApiRequests || 0,
        totalDocumentationEndpoints: data.totalDocumentationEndpoints || 0,
        
        // Additional stats for dashboard
        activeApis: data.activeApis || 0,
        totalCodeImplementations: data.totalCodeImplementations || 0,
        publishedDocumentation: data.publishedDocumentation || 0,
        activeUsers: data.activeUsers || 0,
        totalIpWhitelistEntries: data.totalIpWhitelistEntries || 0,
        unreadSecurityAlerts: data.unreadSecurityAlerts || 0,
        
        // For backwards compatibility with existing code
        totalRequests: data.totalApiRequests || 0,
        successRate: data.successRate || 0,
        averageResponseTime: data.averageResponseTime || 0,
        failedRequests: data.failedRequests || 0
    };

    return { ...response, data: transformedData };
};

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

    return { ...response, data: transformedData };
};

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

    return { ...response, data: transformedData };
};

// ============================================================
// EXTRACT FUNCTIONS
// ============================================================

export const extractRequestData = (response) => response?.data || {};
export const extractRequestsList = (response) => response?.data?.requests || [];
export const extractSearchResults = (response) => response?.data?.content || [];
export const extractStatistics = (response) => response?.data || {};
export const extractDailyBreakdown = (response) => response?.data || {};
export const extractExportData = (response) => response?.data || {};

// ============================================================
// UTILITY FUNCTIONS
// ============================================================

export const getRequestStatusColor = (status) => {
    switch (status?.toUpperCase()) {
        case 'SUCCESS': return 'green';
        case 'FAILED': return 'red';
        case 'TIMEOUT': return 'orange';
        case 'PENDING': return 'blue';
        default: return 'gray';
    }
};

export const getStatusCodeColor = (code) => {
    if (!code) return 'gray';
    if (code >= 200 && code < 300) return 'green';
    if (code >= 300 && code < 400) return 'blue';
    if (code >= 400 && code < 500) return 'orange';
    if (code >= 500) return 'red';
    return 'gray';
};

export const formatExecutionTime = (ms) => {
    if (!ms) return '0ms';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    const minutes = Math.floor(ms / 60000);
    const seconds = ((ms % 60000) / 1000).toFixed(0);
    return `${minutes}m ${seconds}s`;
};

export const formatRequestTimestamp = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleString();
};

export const getRequestSummary = (request) => {
    if (!request) return '';
    return `${request.httpMethod} ${request.url} - ${request.responseStatusCode || 'Pending'}`;
};

export const isRequestSuccessful = (request) => {
    return request?.responseStatusCode >= 200 && request?.responseStatusCode < 300;
};

export const isRequestFailed = (request) => {
    return request?.responseStatusCode >= 400 || request?.requestStatus === 'FAILED';
};

export const downloadExportedRequests = (exportData) => {
    if (!exportData || !exportData.downloadUrl) return;
    window.open(exportData.downloadUrl, '_blank');
};

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