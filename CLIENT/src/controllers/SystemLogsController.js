// controllers/SystemLogsController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./UserManagementController.js"

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for log operations
const buildLogQueryParams = (params = {}) => {
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

// Helper to generate request ID
const generateRequestId = () => {
    return 'req_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// ============ LOG FILES METHODS ============

/**
 * Get all log files
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllLogFiles = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/logs/files`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    );
};

/**
 * Get log entries with filtering and pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.search - Search term
 * @param {string} filters.severity - Severity level filter
 * @param {string} filters.timeFilter - Time range filter
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getLogEntries = async (authorizationHeader, filters = {}, pagination = {}) => {
    const requestId = generateRequestId();
    const { search = '', severity = 'all', timeFilter = 'all' } = filters;
    const { page = 0, size = 10, sort = 'timestamp', direction = 'DESC' } = pagination;
    
    const queryParams = buildLogQueryParams({
        search,
        severity,
        timeFilter,
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/logs/entries${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    );
};

/**
 * Get log file content
 * @param {string} authorizationHeader - Bearer token
 * @param {string} filename - Name of the log file
 * @param {string} search - Search term within file
 * @returns {Promise} API response
 */
export const getLogFileContent = async (authorizationHeader, filename, search = '') => {
    const requestId = generateRequestId();
    
    const queryParams = buildLogQueryParams({ search });
    const url = `/logs/files/${encodeURIComponent(filename)}/content${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    );
};

/**
 * Export logs to CSV
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Export filters
 * @param {string} filters.search - Search term
 * @param {string} filters.severity - Severity level filter
 * @param {string} filters.timeFilter - Time range filter
 * @returns {Promise} API response with file download
 */
export const exportLogsToCsv = async (authorizationHeader, filters = {}) => {
    const requestId = generateRequestId();
    const { search = '', severity = 'all', timeFilter = 'all' } = filters;
    
    const queryParams = buildLogQueryParams({
        search,
        severity,
        timeFilter
    });
    
    const url = `/logs/export${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: {
                ...getAuthHeaders(authHeader.replace('Bearer ', '')),
                'Accept': 'text/csv'
            },
            responseType: 'blob', // Important for file downloads
            requestId: requestId
        })
    );
};

/**
 * Get log statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getLogStatistics = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/logs/statistics`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    );
};

/**
 * Search logs with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {string} query - Search query
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const searchLogs = async (authorizationHeader, query = '', pagination = {}) => {
    const requestId = generateRequestId();
    const { page = 0, size = 10, sort = 'timestamp', direction = 'DESC' } = pagination;
    
    const queryParams = buildLogQueryParams({
        query,
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/logs/search${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for log operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleLogsResponse = (response) => {
    if (!response) {
        throw new Error('No response received from logs service');
    }

    if (response.responseCode === 200 || response.responseCode === 201) {
        return response.data || response;
    }

    switch (response.responseCode) {
        case 204:
            return { message: 'No logs found', data: [] };
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Log file not found: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

/**
 * Extract pagination info from log entries response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractLogsPaginationInfo = (response) => {
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

    // Handle LogEntriesResponse structure
    if (response.data.logs && response.data.totalItems !== undefined) {
        return {
            content: response.data.logs || [],
            totalPages: response.data.totalPages || 0,
            totalElements: response.data.totalItems || 0,
            size: response.data.pageSize || 10,
            number: response.data.pageNumber || 0,
            first: response.data.pageNumber === 0,
            last: response.data.pageNumber === (response.data.totalPages - 1),
            empty: (response.data.logs || []).length === 0,
            numberOfElements: (response.data.logs || []).length
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
 * Validate log search parameters
 * @param {Object} filters - Log search filters
 * @returns {Array} Array of validation errors
 */
export const validateLogSearchParams = (filters = {}) => {
    const errors = [];
    const { severity = '', timeFilter = '' } = filters;
    
    // Validate severity if provided
    if (severity && severity !== 'all') {
        const validSeverities = ['ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE'];
        if (!validSeverities.includes(severity.toUpperCase())) {
            errors.push(`Invalid severity. Must be one of: ${validSeverities.join(', ')} or 'all'`);
        }
    }
    
    // Validate timeFilter if provided
    if (timeFilter && timeFilter !== 'all') {
        const validTimeFilters = ['today', 'yesterday', 'week', 'month', 'year'];
        if (!validTimeFilters.includes(timeFilter.toLowerCase())) {
            errors.push(`Invalid time filter. Must be one of: ${validTimeFilters.join(', ')} or 'all'`);
        }
    }
    
    // Validate search query length if provided
    if (filters.search && filters.search.length > 200) {
        errors.push('Search query cannot exceed 200 characters');
    }
    
    return errors;
};

/**
 * Build pagination parameters for logs
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildLogsPaginationParams = (page = 0, size = 10, sortField = 'timestamp', sortDirection = 'DESC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build search filter parameters for logs
 * @param {Object} filters - Filter criteria
 * @returns {Object} Filter parameters
 */
export const buildLogsSearchFilters = (filters = {}) => {
    const {
        search = '',
        severity = 'all',
        timeFilter = 'all'
    } = filters;
    
    const searchFilters = {};
    
    if (search) searchFilters.search = search;
    if (severity && severity !== 'all') searchFilters.severity = severity;
    if (timeFilter && timeFilter !== 'all') searchFilters.timeFilter = timeFilter;
    
    return searchFilters;
};

/**
 * Parse log file content response
 * @param {Object} response - API response with file content
 * @returns {Object} Parsed log file content
 */
export const parseLogFileContent = (response) => {
    if (!response.data) return null;
    
    // If data is already parsed, return it
    if (typeof response.data === 'object' && response.data.content !== undefined) {
        return {
            filename: response.data.filename || '',
            content: response.data.content || '',
            lines: (response.data.content || '').split('\n').filter(line => line.trim()),
            totalLines: (response.data.content || '').split('\n').length,
            size: response.data.size || 0,
            lastModified: response.data.lastModified || ''
        };
    }
    
    // If data is a string, parse it
    if (typeof response.data === 'string') {
        return {
            content: response.data,
            lines: response.data.split('\n').filter(line => line.trim()),
            totalLines: response.data.split('\n').length,
            size: new Blob([response.data]).size
        };
    }
    
    return response.data;
};

/**
 * Download CSV file from export response
 * @param {Blob} blob - CSV file blob
 * @param {string} filename - Download filename
 */
export const downloadCsvFile = (blob, filename = 'system_logs_export.csv') => {
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
 * Parse log statistics from response
 * @param {Object} response - API response
 * @returns {Object} Log statistics
 */
export const parseLogStatistics = (response) => {
    if (!response.data) return null;
    
    const data = response.data;
    return {
        totalLogs: data.totalLogs || 0,
        logsBySeverity: data.logsBySeverity || {},
        logsByModule: data.logsByModule || {},
        logsByDay: data.logsByDay || {},
        errorsCount: data.errorsCount || 0,
        warningsCount: data.warningsCount || 0,
        infoCount: data.infoCount || 0,
        averageLogsPerDay: data.averageLogsPerDay || 0,
        busiestModule: data.busiestModule || '',
        mostCommonSeverity: data.mostCommonSeverity || '',
        timeRange: data.timeRange || {}
    };
};

/**
 * Parse log entry from response
 * @param {Object} logData - Raw log entry data
 * @returns {Object} Parsed log entry
 */
export const parseLogEntry = (logData) => {
    return {
        id: logData.id || generateRequestId(),
        timestamp: logData.timestamp || new Date().toISOString(),
        severity: logData.severity || 'INFO',
        module: logData.module || 'unknown',
        message: logData.message || '',
        userId: logData.userId || '',
        ipAddress: logData.ipAddress || '',
        requestId: logData.requestId || '',
        endpoint: logData.endpoint || '',
        method: logData.method || '',
        statusCode: logData.statusCode || 0,
        durationMs: logData.durationMs || 0,
        metadata: logData.metadata || {},
        formattedMessage: `${logData.timestamp || ''} [${logData.severity || ''}] ${logData.module || ''}: ${logData.message || ''}`
    };
};

/**
 * Filter log entries by criteria
 * @param {Array} logEntries - Array of log entries
 * @param {Object} filters - Filter criteria
 * @returns {Array} Filtered log entries
 */
export const filterLogEntries = (logEntries, filters = {}) => {
    if (!Array.isArray(logEntries)) return [];
    
    const { severity = '', search = '', timeFilter = '' } = filters;
    
    return logEntries.filter(entry => {
        // Filter by severity
        if (severity && severity !== 'all' && entry.severity !== severity) {
            return false;
        }
        
        // Filter by search term
        if (search) {
            const searchLower = search.toLowerCase();
            const matches = 
                (entry.message && entry.message.toLowerCase().includes(searchLower)) ||
                (entry.module && entry.module.toLowerCase().includes(searchLower)) ||
                (entry.userId && entry.userId.toLowerCase().includes(searchLower)) ||
                (entry.requestId && entry.requestId.toLowerCase().includes(searchLower));
            
            if (!matches) return false;
        }
        
        // Filter by time
        if (timeFilter && timeFilter !== 'all') {
            const now = new Date();
            const logTime = new Date(entry.timestamp);
            
            switch (timeFilter.toLowerCase()) {
                case 'today':
                    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
                    return logTime >= today;
                case 'yesterday':
                    const yesterday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1);
                    const today2 = new Date(now.getFullYear(), now.getMonth(), now.getDate());
                    return logTime >= yesterday && logTime < today2;
                case 'week':
                    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
                    return logTime >= weekAgo;
                case 'month':
                    const monthAgo = new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
                    return logTime >= monthAgo;
                case 'year':
                    const yearAgo = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate());
                    return logTime >= yearAgo;
                default:
                    return true;
            }
        }
        
        return true;
    });
};

/**
 * Group log entries by criteria
 * @param {Array} logEntries - Array of log entries
 * @param {string} groupBy - Grouping criteria ('severity', 'module', 'day', 'hour')
 * @returns {Object} Grouped log entries
 */
export const groupLogEntries = (logEntries, groupBy = 'severity') => {
    if (!Array.isArray(logEntries)) return {};
    
    const grouped = {};
    
    logEntries.forEach(entry => {
        let key;
        
        switch (groupBy.toLowerCase()) {
            case 'severity':
                key = entry.severity || 'UNKNOWN';
                break;
            case 'module':
                key = entry.module || 'UNKNOWN';
                break;
            case 'day':
                const date = new Date(entry.timestamp);
                key = date.toISOString().split('T')[0]; // YYYY-MM-DD
                break;
            case 'hour':
                const hourDate = new Date(entry.timestamp);
                key = `${hourDate.toISOString().split('T')[0]} ${hourDate.getHours()}:00`;
                break;
            case 'user':
                key = entry.userId || 'ANONYMOUS';
                break;
            default:
                key = 'UNKNOWN';
        }
        
        if (!grouped[key]) {
            grouped[key] = [];
        }
        
        grouped[key].push(entry);
    });
    
    return grouped;
};

/**
 * Generate CSV content from log entries
 * @param {Array} logEntries - Array of log entries
 * @returns {string} CSV content
 */
export const generateLogsCsv = (logEntries) => {
    if (!Array.isArray(logEntries) || logEntries.length === 0) {
        return 'Timestamp,Severity,Module,Message,User ID,IP Address,Request ID,Endpoint,Method,Status Code,Duration (ms)\n';
    }
    
    const headers = ['Timestamp', 'Severity', 'Module', 'Message', 'User ID', 'IP Address', 'Request ID', 'Endpoint', 'Method', 'Status Code', 'Duration (ms)'];
    
    const csvRows = [
        headers.join(','),
        ...logEntries.map(entry => [
            `"${entry.timestamp || ''}"`,
            `"${entry.severity || ''}"`,
            `"${entry.module || ''}"`,
            `"${(entry.message || '').replace(/"/g, '""')}"`,
            `"${entry.userId || ''}"`,
            `"${entry.ipAddress || ''}"`,
            `"${entry.requestId || ''}"`,
            `"${entry.endpoint || ''}"`,
            `"${entry.method || ''}"`,
            entry.statusCode || 0,
            entry.durationMs || 0
        ].join(','))
    ];
    
    return csvRows.join('\n');
};

/**
 * Format log severity with color coding
 * @param {string} severity - Log severity
 * @returns {Object} Formatted severity with color
 */
export const formatLogSeverity = (severity) => {
    const severityUpper = severity.toUpperCase();
    
    const severityConfig = {
        'ERROR': { color: '#f44336', label: 'Error', icon: '❌' },
        'WARN': { color: '#ff9800', label: 'Warning', icon: '⚠️' },
        'INFO': { color: '#2196f3', label: 'Info', icon: 'ℹ️' },
        'DEBUG': { color: '#9c27b0', label: 'Debug', icon: '🐛' },
        'TRACE': { color: '#607d8b', label: 'Trace', icon: '📋' }
    };
    
    return severityConfig[severityUpper] || { color: '#757575', label: 'Unknown', icon: '❓' };
};

/**
 * Format log timestamp for display
 * @param {string} timestamp - ISO timestamp
 * @returns {string} Formatted timestamp
 */
export const formatLogTimestamp = (timestamp) => {
    try {
        const date = new Date(timestamp);
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: true
        });
    } catch (e) {
        return timestamp || 'Unknown';
    }
};