// controllers/PostgreSQLSchemaController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js"

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters
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

// Helper to generate request ID
const generateRequestId = () => {
    return 'req_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// Cache management
const schemaCache = new Map();

export const cacheSchemaData = (key, data, ttlMinutes = 15) => {
    schemaCache.set(key, {
        data,
        timestamp: Date.now(),
        ttl: ttlMinutes * 60 * 1000
    });
};

export const getCachedSchemaData = (key) => {
    const cached = schemaCache.get(key);
    if (cached && (Date.now() - cached.timestamp) < cached.ttl) {
        return cached.data;
    }
    return null;
};

export const clearCachedSchemaData = (key) => {
    if (key) {
        schemaCache.delete(key);
    } else {
        schemaCache.clear();
    }
};

// ============================================================
// 1. SCHEMA INFO ENDPOINTS
// ============================================================

/**
 * Get current schema information
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getCurrentSchemaInfo = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/info`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return response;
    });
};

/**
 * Get current user
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getCurrentUser = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/current-user`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformCurrentUserResponse(response);
    });
};

// ============================================================
// 2. TABLE ENDPOINTS - LEGACY FORMAT
// ============================================================

/**
 * Get all PostgreSQL tables
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTables = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/tables`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTablesResponse(response);
    });
};

/**
 * Get tables by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getTablesBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/tables/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTablesResponse(response);
    });
};

/**
 * Get table details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} tableName - Table name
 * @returns {Promise} API response
 */
export const getTableDetails = async (authorizationHeader, tableName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/tables/${encodeURIComponent(tableName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTableDetailsResponse(response, tableName);
    });
};

/**
 * Search tables by pattern
 * @param {string} authorizationHeader - Bearer token
 * @param {string} pattern - Search pattern (e.g., 'EMP%')
 * @returns {Promise} API response
 */
export const searchTables = async (authorizationHeader, pattern) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ pattern });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/tables/search?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTablesResponse(response);
    });
};

/**
 * Get table statistics
 * @param {string} authorizationHeader - Bearer token
 * @param {string} tableName - Table name
 * @returns {Promise} API response
 */
export const getTableStatistics = async (authorizationHeader, tableName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/tables/${encodeURIComponent(tableName)}/statistics`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformStatisticsResponse(response, tableName);
    });
};

/**
 * Get tables with row count
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getTablesWithRowCount = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/tables/with-row-count`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTablesWithRowCountResponse(response);
    });
};

/**
 * Get tablespace statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getTablespaceStats = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/tablespace-stats`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTablespaceStatsResponse(response);
    });
};

/**
 * Get recent tables
 * @param {string} authorizationHeader - Bearer token
 * @param {number} days - Number of days (default: 7)
 * @returns {Promise} API response
 */
export const getRecentTables = async (authorizationHeader, days = 7) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ days });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/tables/recent?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTablesResponse(response);
    });
};

// ============================================================
// 3. TABLE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all PostgreSQL tables (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTablesForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/tables`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated tables (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllTablesForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/tables/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get table details for frontend
 * @param {string} authorizationHeader - Bearer token
 * @param {string} tableName - Table name
 * @returns {Promise} API response
 */
export const getTableDetailsForFrontend = async (authorizationHeader, tableName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/frontend/tables/${encodeURIComponent(tableName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get table details with paginated columns (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} tableName - Table name
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of columns per page
 * @returns {Promise} API response
 */
export const getTableDetailsForFrontendPaginated = async (authorizationHeader, tableName, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    const url = `/postgresql/schema/frontend/tables/${encodeURIComponent(tableName)}/details/paginated${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get table data with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.tableName - Table name
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of rows per page
 * @param {string} params.sortColumn - Column to sort by
 * @param {string} params.sortDirection - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getTableData = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { tableName, page = 1, pageSize = 10, sortColumn, sortDirection = 'ASC' } = params;
    
    const queryParams = buildQueryParams({
        page,
        pageSize,
        sortColumn,
        sortDirection
    });
    
    const url = `/postgresql/schema/tables/${encodeURIComponent(tableName)}/data${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTableDataResponse(response, tableName, params);
    });
};

/**
 * Get table data with advanced pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.tableName - Table name
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of rows per page
 * @param {string} params.sortColumn - Column to sort by
 * @param {string} params.sortDirection - Sort direction (ASC/DESC)
 * @param {string} params.filter - Filter condition (e.g., column=value)
 * @returns {Promise} API response
 */
export const getTableDataAdvanced = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { tableName, page = 1, pageSize = 10, sortColumn, sortDirection = 'ASC', filter } = params;
    
    const queryParams = buildQueryParams({
        page,
        pageSize,
        sortColumn,
        sortDirection,
        filter
    });
    
    const url = `/postgresql/schema/tables/${encodeURIComponent(tableName)}/data/advanced${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTableDataAdvancedResponse(response, tableName, params);
    });
};

/**
 * Get table columns with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.tableName - Table name
 * @param {string} params.owner - Table owner (optional)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of columns per page
 * @returns {Promise} API response
 */
export const getTableColumnsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { tableName, owner, page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize
    });
    
    const url = `/postgresql/schema/tables/${encodeURIComponent(tableName)}/columns${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 4. VIEW ENDPOINTS - LEGACY FORMAT
// ============================================================

/**
 * Get all PostgreSQL views
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllViews = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/views`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformViewsResponse(response);
    });
};

/**
 * Get views by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getViewsBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/views/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformViewsResponse(response);
    });
};

/**
 * Get view details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} viewName - View name
 * @returns {Promise} API response
 */
export const getViewDetails = async (authorizationHeader, viewName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/views/${encodeURIComponent(viewName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformViewDetailsResponse(response, viewName);
    });
};

// ============================================================
// 5. VIEW ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all PostgreSQL views (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllViewsForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/views`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated views (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllViewsForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/views/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 6. PROCEDURE ENDPOINTS - LEGACY FORMAT
// ============================================================

/**
 * Get all PostgreSQL procedures
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllProcedures = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/procedures`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformProceduresResponse(response);
    });
};

/**
 * Get procedures by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getProceduresBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/procedures/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformProceduresResponse(response);
    });
};

/**
 * Get procedure details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} procedureName - Procedure name
 * @returns {Promise} API response
 */
export const getProcedureDetails = async (authorizationHeader, procedureName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/procedures/${encodeURIComponent(procedureName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformProcedureDetailsResponse(response, procedureName);
    });
};

/**
 * Get procedure parameters with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.procedureName - Procedure name
 * @param {string} params.owner - Procedure owner (optional)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of parameters per page
 * @returns {Promise} API response
 */
export const getProcedureParametersPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { procedureName, owner, page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize
    });
    
    const url = `/postgresql/schema/procedures/${encodeURIComponent(procedureName)}/parameters${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 7. PROCEDURE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all PostgreSQL procedures (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllProceduresForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/procedures`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated procedures (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllProceduresForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/procedures/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 8. FUNCTION ENDPOINTS - LEGACY FORMAT
// ============================================================

/**
 * Get all PostgreSQL functions
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllFunctions = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/functions`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFunctionsResponse(response);
    });
};

/**
 * Get functions by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getFunctionsBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/functions/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFunctionsResponse(response);
    });
};

/**
 * Get function details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} functionName - Function name
 * @returns {Promise} API response
 */
export const getFunctionDetails = async (authorizationHeader, functionName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/functions/${encodeURIComponent(functionName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFunctionDetailsResponse(response, functionName);
    });
};

/**
 * Get function parameters with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.functionName - Function name
 * @param {string} params.owner - Function owner (optional)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of parameters per page
 * @returns {Promise} API response
 */
export const getFunctionParametersPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { functionName, owner, page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize
    });
    
    const url = `/postgresql/schema/functions/${encodeURIComponent(functionName)}/parameters${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFunctionParametersResponse(response);
    });
};

// ============================================================
// 9. FUNCTION ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all PostgreSQL functions (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllFunctionsForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/functions`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated functions (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllFunctionsForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/functions/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 10. SCHEMA ENDPOINTS (PostgreSQL equivalent of packages)
// ============================================================

/**
 * Get all PostgreSQL schemas (package equivalents)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSchemas = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/schemas`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSchemasResponse(response);
    });
};

/**
 * Get schemas by name
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getSchemasByName = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/schemas/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSchemasResponse(response);
    });
};

/**
 * Get schema details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getSchemaDetails = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/schemas/${encodeURIComponent(schemaName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSchemaDetailsResponse(response, schemaName);
    });
};

/**
 * Get schema items with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.schemaName - Schema name
 * @param {string} params.owner - Schema owner (optional)
 * @param {string} params.itemType - Item type (FUNCTION, PROCEDURE, TABLE, VIEW, ALL)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getSchemaItemsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { schemaName, owner, itemType = 'ALL', page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        itemType,
        page,
        pageSize
    });
    
    const url = `/postgresql/schema/schemas/${encodeURIComponent(schemaName)}/items${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 11. SCHEMA ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all schemas (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSchemasForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/schemas`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated schemas (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllSchemasForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/schemas/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 12. SEQUENCE ENDPOINTS - LEGACY FORMAT
// ============================================================

/**
 * Get all PostgreSQL sequences
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSequences = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/sequences`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSequencesResponse(response);
    });
};

/**
 * Get sequences by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getSequencesBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/sequences/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSequencesResponse(response);
    });
};

/**
 * Get sequence details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} sequenceName - Sequence name
 * @returns {Promise} API response
 */
export const getSequenceDetails = async (authorizationHeader, sequenceName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/sequences/${encodeURIComponent(sequenceName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSequenceDetailsResponse(response, sequenceName);
    });
};

// ============================================================
// 13. SEQUENCE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all PostgreSQL sequences (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSequencesForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/sequences`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated sequences (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllSequencesForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/sequences/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};


/**
 * Get all materialized views (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllMaterializedViewsForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/materialized-views`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};



/**
 * Get paginated materialized views (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllMaterializedViewsForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/materialized-views/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};



/**
 * Transform table data response
 */
const transformTableDataResponse = (response, tableName, params) => {
    const data = response.data || {};
    
    let rows = [];
    let columns = [];
    let totalRows = 0;
    let totalPages = 1;
    let page = params?.page || 1;
    let pageSize = params?.pageSize || 50;
    
    if (data.data) {
        rows = data.data.rows || [];
        columns = data.data.columns || [];
        totalRows = data.data.totalRows || 0;
        totalPages = data.data.totalPages || 1;
        page = data.data.page || page;
        pageSize = data.data.pageSize || pageSize;
    } else if (data.rows) {
        rows = data.rows || [];
        columns = data.columns || [];
        totalRows = data.totalRows || rows.length;
        totalPages = data.totalPages || 1;
        page = data.page || page;
        pageSize = data.pageSize || pageSize;
    } else if (Array.isArray(data)) {
        rows = data;
        if (data.length > 0) {
            columns = Object.keys(data[0]).map(key => ({ name: key }));
        }
        totalRows = data.length;
        totalPages = 1;
    }
    
    const transformedData = {
        tableName: tableName,
        columns: columns,
        rows: rows,
        totalRows: totalRows,
        totalPages: totalPages,
        pageSize: pageSize,
        page: page,
        hasNext: page < totalPages,
        hasPrev: page > 1
    };

    return {
        ...response,
        data: transformedData
    };
};



/**
 * Transform DDL response
 */
const transformDDLResponse = (response) => {
    const data = response.data || {};
    
    let ddlText = '';
    
    if (typeof data === 'string') {
        ddlText = data;
    } else if (data.ddl) {
        ddlText = data.ddl;
    } else if (data.text) {
        ddlText = data.text;
    } else if (data.sql) {
        ddlText = data.sql;
    } else if (data.source) {
        ddlText = data.source;
    } else if (Array.isArray(data)) {
        ddlText = data.join('\n');
    } else {
        try {
            ddlText = JSON.stringify(data, null, 2);
        } catch (e) {
            ddlText = '';
        }
    }
    
    const transformedData = {
        ddl: ddlText,
        objectName: data.object_name || data.name || '',
        objectType: data.object_type || data.type || '',
        owner: data.owner || '',
        lines: ddlText ? ddlText.split('\n').length : 0
    };

    return {
        ...response,
        data: transformedData
    };
};


// ============================================================
// 14. SYNONYM ENDPOINTS (Not supported in PostgreSQL)
// ============================================================

/**
 * Get all synonyms (Not supported in PostgreSQL)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSynonyms = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/synonyms`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        // Return empty result with message
        return {
            responseCode: 200,
            message: "Synonyms are not supported in PostgreSQL",
            data: [],
            requestId
        };
    });
};

/**
 * Get synonyms by schema (Not supported)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getSynonymsBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/synonyms/by-schema?schemaName=${encodeURIComponent(schemaName)}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return {
            responseCode: 200,
            message: "Synonyms are not supported in PostgreSQL",
            data: [],
            requestId
        };
    });
};

// ============================================================
// 15. TYPE ENDPOINTS - LEGACY FORMAT
// ============================================================

/**
 * Get all PostgreSQL types
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTypes = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/types`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTypesResponse(response);
    });
};

/**
 * Get types by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getTypesBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/types/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTypesResponse(response);
    });
};

/**
 * Get type details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} typeName - Type name
 * @returns {Promise} API response
 */
export const getTypeDetails = async (authorizationHeader, typeName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/types/${encodeURIComponent(typeName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTypeDetailsResponse(response, typeName);
    });
};

// ============================================================
// 16. TYPE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all PostgreSQL types (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTypesForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/types`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated types (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllTypesForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/types/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 17. TRIGGER ENDPOINTS - LEGACY FORMAT
// ============================================================

/**
 * Get all PostgreSQL triggers
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTriggers = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/triggers`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTriggersResponse(response);
    });
};

/**
 * Get triggers by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getTriggersBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/triggers/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTriggersResponse(response);
    });
};

/**
 * Get trigger details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} triggerName - Trigger name
 * @returns {Promise} API response
 */
export const getTriggerDetails = async (authorizationHeader, triggerName) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/triggers/${encodeURIComponent(triggerName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTriggerDetailsResponse(response, triggerName);
    });
};

// ============================================================
// 18. TRIGGER ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all PostgreSQL triggers (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTriggersForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/triggers`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get paginated triggers (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getAllTriggersForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/triggers/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 19. FOREIGN DATA WRAPPER ENDPOINTS (PostgreSQL DB Link equivalent)
// ============================================================

/**
 * Get all foreign data wrappers
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllForeignDataWrappers = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/fdw`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformForeignDataWrappersResponse(response);
    });
};

/**
 * Get foreign data wrappers by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getForeignDataWrappersBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/fdw/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformForeignDataWrappersResponse(response);
    });
};

// ============================================================
// 20. GENERAL OBJECT ENDPOINTS
// ============================================================

/**
 * Get all PostgreSQL objects
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllObjects = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/objects`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectsResponse(response);
    });
};

/**
 * Get objects by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getObjectsBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/objects/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectsResponse(response);
    });
};

/**
 * Search objects by pattern
 * @param {string} authorizationHeader - Bearer token
 * @param {string} pattern - Search pattern (e.g., 'EMP%')
 * @returns {Promise} API response
 */
export const searchObjects = async (authorizationHeader, pattern) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ pattern });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/objects/search?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSearchResponse(response);
    });
};

/**
 * Get object count by type
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getObjectCountByType = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/objects/count-by-type`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectCountResponse(response);
    });
};

/**
 * Get invalid objects
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getInvalidObjects = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/objects/invalid`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectsResponse(response);
    });
};

/**
 * Get objects by status
 * @param {string} authorizationHeader - Bearer token
 * @param {string} status - Object status (VALID, INVALID, etc.)
 * @returns {Promise} API response
 */
export const getObjectsByStatus = async (authorizationHeader, status) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/objects/by-status/${encodeURIComponent(status)}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectsResponse(response);
    });
};

// ============================================================
// 21. OBJECT DETAILS ENDPOINTS
// ============================================================

/**
 * Get object details by name and type
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, etc.)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const getObjectDetails = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner } = params;
    
    const queryParams = buildQueryParams({ owner });
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/details${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectDetailsResponse(response, objectType, objectName);
    });
};

/**
 * Get paginated object details
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, etc.)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @param {boolean} params.includeCounts - Include total counts without fetching all data
 * @returns {Promise} API response
 */
export const getObjectDetailsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner, page = 1, pageSize = 10, includeCounts = false } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize,
        includeCounts
    });
    
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/details/paginated${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get object counts only
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const getObjectCountsOnly = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner } = params;
    
    const queryParams = buildQueryParams({ owner });
    
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/counts${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectCountsOnlyResponse(response);
    });
};

/**
 * Validate object
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectName - Object name
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, etc.)
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const validateObject = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectName, objectType, owner } = params;
    
    const queryParams = buildQueryParams({
        objectName,
        objectType,
        owner
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/objects/validate?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformValidateObjectResponse(response);
    });
};

/**
 * Get object size
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, INDEX, etc.)
 * @param {string} params.objectName - Object name
 * @returns {Promise} API response
 */
export const getObjectSize = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName } = params;
    
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/size`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectSizeResponse(response);
    });
};

/**
 * Get object DDL
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (table, view, procedure, function, trigger, sequence, type)
 * @param {string} params.objectName - Object name
 * @returns {Promise} API response
 */
export const getObjectDDL = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName } = params;
    
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType).toUpperCase()}/${encodeURIComponent(objectName)}/ddl`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDDLResponse(response);
    }).catch(error => {
        console.error('Error fetching DDL:', error);
        return {
            responseCode: error.response?.status || 500,
            message: error.message || 'Failed to fetch DDL',
            data: {
                ddl: `-- Error fetching DDL for ${objectType} ${objectName}\n-- ${error.message}`,
                objectName,
                objectType,
                error: error.message
            },
            requestId
        };
    });
};

// ============================================================
// 22. SEARCH ENDPOINTS - ENHANCED
// ============================================================

/**
 * Paginated search for PostgreSQL objects
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.query - Search query
 * @param {string} params.type - Search type (ALL, TABLE, VIEW, PROCEDURE, etc.)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of results per page
 * @returns {Promise} API response
 */
export const searchObjectsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { query, type, page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({
        query,
        type,
        page,
        pageSize
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/search/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSearchPaginatedResponse(response);
    });
};

/**
 * Paginated search for frontend
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.query - Search query
 * @param {string} params.type - Search type (ALL, TABLE, VIEW, PROCEDURE, etc.)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of results per page
 * @returns {Promise} API response
 */
export const searchObjectsForFrontendPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { query, type, page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({
        query,
        type,
        page,
        pageSize
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/search/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Comprehensive search across all objects
 * @param {string} authorizationHeader - Bearer token
 * @param {string} pattern - Search pattern
 * @returns {Promise} API response
 */
export const comprehensiveSearch = async (authorizationHeader, pattern) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ pattern });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/comprehensive-search?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformComprehensiveSearchResponse(response);
    });
};

// ============================================================
// 23. RECENT OBJECTS ENDPOINTS
// ============================================================

/**
 * Get objects modified in the last N days
 * @param {string} authorizationHeader - Bearer token
 * @param {number} days - Number of days
 * @returns {Promise} API response
 */
export const getRecentObjects = async (authorizationHeader, days) => {
    const requestId = generateRequestId();
    
    const url = `/postgresql/schema/recent/${encodeURIComponent(days)}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectsResponse(response);
    });
};

// ============================================================
// 24. EXECUTE QUERY ENDPOINT
// ============================================================

/**
 * Execute SQL query
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} queryRequest - Query request
 * @param {string} queryRequest.query - SQL query
 * @param {number} queryRequest.timeoutSeconds - Timeout in seconds (default: 30)
 * @param {boolean} queryRequest.readOnly - Read only flag (default: true)
 * @returns {Promise} API response with query results
 */
export const executeQuery = async (authorizationHeader, queryRequest = {}) => {
    const requestId = generateRequestId();
    const { query, timeoutSeconds = 30, readOnly = true } = queryRequest;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/execute`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify({
                query,
                timeoutSeconds,
                readOnly
            }),
            requestId: requestId
        })
    ).then(response => {
        return transformQueryResponse(response);
    });
};

// ============================================================
// 25. DIAGNOSTICS ENDPOINT
// ============================================================

/**
 * Run database diagnostics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const diagnoseDatabase = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/diagnose`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDiagnosticsResponse(response);
    });
};

// ============================================================
// 26. PAGINATED OBJECT LISTS FOR FRONTEND
// ============================================================

/**
 * Get paginated tables (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getTablesPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/tables/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated views (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getViewsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/views/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated procedures (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getProceduresPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/procedures/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated functions (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getFunctionsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/functions/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated schemas (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getSchemasPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/schemas/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated sequences (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getSequencesPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/sequences/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated types (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getTypesPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/types/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated triggers (Frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getTriggersPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/triggers/filtered/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get all object counts (fast)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllObjectCounts = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/frontend/counts`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectCountResponse(response);
    });
};



// Add these functions to your PostgreSQLSchemaController.js

/**
 * Get basic object info (owner, status, timestamps)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const getObjectBasicInfo = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner } = params;
    
    const queryParams = buildQueryParams({ owner });
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/info${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectBasicInfoResponse(response);
    });
};

/**
 * Get table constraints
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.tableName - Table name
 * @param {string} params.owner - Table owner (optional)
 * @returns {Promise} API response
 */
export const getTableConstraints = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { tableName, owner } = params;
    
    const queryParams = buildQueryParams({ owner });
    const url = `/postgresql/schema/objects/TABLE/${encodeURIComponent(tableName)}/constraints${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTableConstraintsResponse(response);
    });
};

/**
 * Extract DDL from response
 * @param {Object} response - API response
 * @returns {string} DDL text
 */
export const extractDDL = (response) => {
    const data = response.data || {};
    
    let ddlText = '';
    
    if (typeof data === 'string') {
        ddlText = data;
    } else if (data.ddl) {
        ddlText = data.ddl;
    } else if (data.text) {
        ddlText = data.text;
    } else if (data.sql) {
        ddlText = data.sql;
    } else if (data.source) {
        ddlText = data.source;
    } else if (Array.isArray(data)) {
        ddlText = data.join('\n');
    } else {
        try {
            ddlText = JSON.stringify(data, null, 2);
        } catch (e) {
            ddlText = '';
        }
    }
    
    return ddlText;
};

/**
 * Get used by paginated objects
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, etc.)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getUsedByPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner, page = 1, pageSize = 10 } = params;
    
    const queryParams = buildQueryParams({ owner, page, pageSize });
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/used-by/paginated${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedUsedByResponse(response);
    });
};

/**
 * Get used by summary grouped by object type
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, etc.)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const getUsedBySummary = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner } = params;
    
    const queryParams = buildQueryParams({ owner });
    const url = `/postgresql/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/used-by/summary${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformUsedBySummaryResponse(response);
    });
};

// Add these transform functions as well

/**
 * Transform object basic info response
 */
const transformObjectBasicInfoResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        ...data,
        objectName: data.object_name || data.name,
        objectType: data.object_type || data.type,
        owner: data.owner,
        status: data.status,
        created: data.created,
        lastModified: data.last_ddl_time || data.last_modified,
        temporary: data.temporary,
        generated: data.generated,
        secondary: data.secondary,
        OBJECT_NAME: data.OBJECT_NAME || data.object_name || data.name,
        OBJECT_TYPE: data.OBJECT_TYPE || data.object_type || data.type,
        OWNER: data.OWNER || data.owner,
        STATUS: data.STATUS || data.status,
        CREATED: data.CREATED || data.created,
        LAST_DDL_TIME: data.LAST_DDL_TIME || data.last_ddl_time || data.lastModified,
        isSynonym: false
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform table constraints response
 */
const transformTableConstraintsResponse = (response) => {
    const data = response.data || [];
    
    let constraintsArray = [];
    
    if (Array.isArray(data)) {
        constraintsArray = data;
    } else if (data.constraints && Array.isArray(data.constraints)) {
        constraintsArray = data.constraints;
    } else {
        constraintsArray = [];
    }
    
    const transformedData = constraintsArray.map((con, index) => ({
        id: `constraint-${index + 1}`,
        name: con.name || con.CONSTRAINT_NAME || '-',
        type: con.type || con.CONSTRAINT_TYPE || '-',
        typeFormatted: formatConstraintType(con.type || con.CONSTRAINT_TYPE || '-'),
        columns: con.columns ? (Array.isArray(con.columns) ? con.columns : [con.columns]) : (con.COLUMNS || []),
        columnsString: con.columnsString || con.COLUMNS || '-',
        status: con.status || con.CONSTRAINT_STATUS || '-',
        validated: con.validated || con.VALIDATED || '-',
        deferrable: con.deferrable || con.DEFERRABLE,
        deferred: con.deferred || con.DEFERRED,
        columnCount: con.columnCount || con.COLUMN_COUNT
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform paginated used by response
 */
const transformPaginatedUsedByResponse = (response) => {
  const data = response.data || {};
  
  // Your actual payload structure might be different for the items list
  // Adjust based on your actual API response
  const items = (data.items || []).map((item, index) => {
    // Handle both uppercase/lowercase field names
    const name = item.name || item.NAME || item.object_name || item.OBJECT_NAME;
    const owner = item.owner || item.OWNER;
    const type = item.type || item.TYPE || item.object_type || item.OBJECT_TYPE;
    const status = item.status || item.STATUS;
    
    return {
      id: `usedby-${index + 1}`,
      name: name,
      owner: owner,
      type: type,
      objectType: type,
      status: status,
      created: item.created || item.CREATED,
      lastModified: item.last_modified || item.LAST_MODIFIED || item.lastModified,
      dependencyType: item.dependency_type || item.DEPENDENCY_TYPE,
      parameterCount: item.parameter_count || item.parameterCount || 0,
      hasParameters: (item.parameter_count || item.parameterCount || 0) > 0,
      icon: getObjectTypeIcon(type)
    };
  });
  
  const transformedData = {
    items: items,
    totalCount: data.totalCount || data.total_count || 0,
    totalPages: data.totalPages || data.total_pages || 1,
    page: data.page || 1,
    pageSize: data.pageSize || data.page_size || 10,
    hasNext: (data.page || 1) < (data.totalPages || data.total_pages || 1),
    hasPrev: (data.page || 1) > 1,
    objectName: data.objectName || data.object_name || '',
    objectType: data.objectType || data.object_type || '',
    owner: data.owner || ''
  };

  return {
    ...response,
    data: transformedData
  };
};


/**
 * Transform function parameters response
 */
const transformFunctionParametersResponse = (response) => {
    const data = response.data || {};
    
    // Check if parameters are in data.parameters (as in your response)
    let parameters = [];
    let totalCount = 0;
    let totalPages = 1;
    let page = data.page || 1;
    let pageSize = data.pageSize || 10;
    
    if (data.parameters && Array.isArray(data.parameters)) {
        parameters = data.parameters;
        totalCount = data.totalCount || parameters.length;
        totalPages = data.totalPages || Math.ceil(totalCount / pageSize);
    } else if (data.items && Array.isArray(data.items)) {
        parameters = data.items;
        totalCount = data.totalCount || parameters.length;
        totalPages = data.totalPages || Math.ceil(totalCount / pageSize);
    } else if (Array.isArray(data)) {
        parameters = data;
        totalCount = parameters.length;
        totalPages = 1;
    }
    
    const transformedData = {
        items: parameters.map((param, index) => ({
            POSITION: param.sequence || param.position || index + 1,
            ARGUMENT_NAME: param.argument_name || param.name,
            DATA_TYPE: param.data_type || param.type,
            IN_OUT: param.in_out || param.mode || 'IN',
            DATA_LENGTH: param.data_length || '-',
            DEFAULT_VALUE: param.default_value,
            DEFAULTED: param.defaulted || 'N'
        })),
        totalCount: totalCount,
        totalPages: totalPages,
        page: page,
        pageSize: pageSize
    };

    return {
        ...response,
        data: transformedData
    };
};



/**
 * Transform search response (special handling for search results)
 */
const transformSearchPaginatedResponse = (response) => {
    // Extract the data object from response
    const responseData = response.data || {};
    
    // The search API returns results in a different structure
    const transformedData = {
        items: responseData.results || [],
        totalCount: responseData.totalCount || 0,
        totalPages: responseData.totalPages || 1,
        page: responseData.page || 1,
        pageSize: responseData.pageSize || 20,
        hasNext: responseData.page < responseData.totalPages,
        hasPrev: responseData.page > 1
    };

    return {
        ...response,
        data: transformedData
    };
};



/**
 * Transform used by summary response
 */
const transformUsedBySummaryResponse = (response) => {
  const data = response.data || {};
  
  // Handle both uppercase and lowercase field names
  const transformedData = {
    byType: (data.byType || []).map(item => ({
      // Handle lowercase fields (as in your payload)
      dependentType: item.dependent_type || item.dependentType || item.DEPENDENT_TYPE || 'Unknown',
      count: item.count || item.COUNT || 0,
      validCount: item.valid_count || item.validCount || item.VALID_COUNT || 0,
      invalidCount: item.invalid_count || item.invalidCount || item.INVALID_COUNT || 0
    })),
    totalCount: data.totalCount || data.total_count || 0,
    objectName: data.objectName || data.object_name || '',
    objectType: data.objectType || data.object_type || '',
    owner: data.owner || '',
    schema: data.schema || '',
    generatedAt: data.generatedAt || data.generated_at || new Date().toISOString(),
    generatedBy: data.generatedBy || data.generated_by || ''
  };

  return {
    ...response,
    data: transformedData
  };
};

/**
 * Format constraint type
 */
const formatConstraintType = (type) => {
    const types = {
        'P': 'PRIMARY KEY',
        'R': 'FOREIGN KEY',
        'U': 'UNIQUE',
        'C': 'CHECK',
        'V': 'VIEW CHECK',
        'O': 'READ ONLY VIEW'
    };
    return types[type] || type || 'UNKNOWN';
};

/**
 * Get object type icon
 */
export const getObjectTypeIcon = (type) => {
    const icons = {
        'TABLE': '📊',
        'VIEW': '👁️',
        'MATERIALIZED VIEW': '📋',
        'PROCEDURE': '⚙️',
        'FUNCTION': '🔧',
        'SEQUENCE': '🔢',
        'TYPE': '📐',
        'TRIGGER': '⚡',
        'INDEX': '📌',
        'SCHEMA': '📁'
    };
    return icons[type?.toUpperCase()] || '📄';
};

/**
 * Format bytes to human readable string
 */
export const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0 || !bytes) return '0 Bytes';
    
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};


/**
 * Check if object is supported for API generation
 */
export const isSupportedForAPIGeneration = (objectType) => {
    const supportedTypes = ['TABLE', 'VIEW', 'MATERIALIZED VIEW', 'PROCEDURE', 'FUNCTION'];
    return supportedTypes.includes(objectType?.toUpperCase());
};

/**
 * Generate sample query for object
 */
export const generateSampleQuery = (objectName, objectType) => {
    switch (objectType?.toUpperCase()) {
        case 'TABLE':
        case 'VIEW':
        case 'MATERIALIZED VIEW':
            return `SELECT * FROM ${objectName} LIMIT 10`;
        case 'PROCEDURE':
            return `CALL ${objectName}();`;
        case 'FUNCTION':
            return `SELECT ${objectName}();`;
        case 'SEQUENCE':
            return `SELECT nextval('${objectName}');`;
        default:
            return `-- No sample query available for ${objectType}`;
    }
};



// ============================================================
// 27. HEALTH CHECK ENDPOINT
// ============================================================

/**
 * Health check
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const healthCheck = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/health`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformHealthCheckResponse(response, requestId);
    });
};

// ============================================================
// 28. HELPER ENDPOINTS
// ============================================================

/**
 * Get supported object types
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getSupportedObjectTypes = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/postgresql/schema/supported-object-types`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSupportedObjectTypesResponse(response);
    });
};

// ============================================================
// NEW TRANSFORM FUNCTIONS FOR POSTGRESQL
// ============================================================

/**
 * Transform schemas response
 */
const transformSchemasResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.schemas || [])).map((schema, index) => ({
        id: `schema-${index + 1}`,
        name: schema.name || schema.schema_name,
        owner: schema.owner,
        type: 'SCHEMA',
        status: schema.status || 'VALID',
        objectCount: schema.objectCount || 0,
        created: schema.created,
        icon: '📁'
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform schema details response
 */
const transformSchemaDetailsResponse = (response, schemaName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `schema-${Date.now()}`,
        name: data.name || schemaName,
        owner: data.owner,
        type: 'SCHEMA',
        status: data.status || 'VALID',
        objectCount: data.objectCount || 0,
        ownerName: data.owner_name,
        created: data.created,
        tables: data.tables || [],
        views: data.views || [],
        functions: data.functions || [],
        procedures: data.procedures || [],
        sequences: data.sequences || [],
        types: data.types || []
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform foreign data wrappers response
 */
const transformForeignDataWrappersResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.fdw || [])).map((fdw, index) => ({
        id: `fdw-${index + 1}`,
        name: fdw.fdw_name || fdw.name,
        owner: fdw.fdw_owner,
        handler: fdw.fdw_handler,
        validator: fdw.fdw_validator,
        options: fdw.fdw_options || [],
        servers: fdw.servers || []
    }));

    return {
        ...response,
        data: transformedData
    };
};


/**
 * Format date for display
 */
export const formatDateForDisplay = (dateString) => {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        return date.toLocaleString();
    } catch (e) {
        return dateString;
    }
};


/**
 * Transform paginated response
 */
const transformPaginatedResponse = (response) => {
    // Extract the data object from response
    const responseData = response.data || {};
    
    // In the updated structure, pagination metadata is inside responseData
    const transformedData = {
        items: responseData.items || [],
        totalCount: responseData.totalCount || responseData.pagination?.totalCount || 0,
        totalPages: responseData.pagination?.totalPages || 1,
        page: responseData.pagination?.page || 1,
        pageSize: responseData.pagination?.pageSize || 20,
        hasNext: responseData.pagination?.hasNext || 
                (responseData.pagination?.page < responseData.pagination?.totalPages),
        hasPrev: responseData.pagination?.hasPrev || 
                (responseData.pagination?.page > 1)
    };

    return {
        ...response,
        data: transformedData
    };
};



/**
 * Handle PostgreSQL schema browser API responses
 * @param {Object} response - API response from the server
 * @returns {Object} Processed response data
 * @throws {Error} If response indicates an error
 */
export const handlePostgreSQLSchemaBrowserResponse = (response) => {
    // Check if response exists
    if (!response) {
        throw new Error('No response received from schema browser service');
    }

    // Successful response codes
    if (response.responseCode === 200 || response.responseCode === 201) {
        // Return the data portion, or the whole response if data is not present
        return response.data || response;
    }

    // Handle different error codes
    switch (response.responseCode) {
        case 204:
            return { 
                message: 'No data found', 
                data: [] 
            };
        case 400:
            throw new Error(`Bad Request: ${response.message || 'Invalid request parameters'}`);
        case 401:
            throw new Error(`Unauthorized: ${response.message || 'Authentication required'}`);
        case 403:
            throw new Error(`Forbidden: ${response.message || 'You do not have permission to access this resource'}`);
        case 404:
            throw new Error(`Not Found: ${response.message || 'The requested object was not found'}`);
        case 500:
            throw new Error(`Server Error: ${response.message || 'An internal server error occurred'}`);
        case 503:
            throw new Error(`Service Unavailable: ${response.message || 'The service is temporarily unavailable'}`);
        default:
            throw new Error(`Error ${response.responseCode}: ${response.message || 'Unknown error occurred'}`);
    }
};


/**
 * Extract table data from response
 * @param {Object} response - API response
 * @returns {Object} Processed table data
 */
export const extractTableData = (response) => {
    if (response?.data?.data) {
        const tableData = response.data.data;
        return {
            columns: tableData.columns || [],
            rows: tableData.rows || [],
            totalRows: tableData.totalRows || tableData.rows?.length || 0,
            totalPages: tableData.totalPages || 1,
            pageSize: tableData.pageSize || 50,
            page: tableData.page || 1
        };
    } else if (response?.data?.columns && response?.data?.rows) {
        return {
            columns: response.data.columns || [],
            rows: response.data.rows || [],
            totalRows: response.data.totalRows || response.data.rows.length,
            totalPages: response.data.totalPages || 1,
            pageSize: response.data.pageSize || 50,
            page: response.data.page || 1
        };
    } else if (response?.columns && response?.rows) {
        return {
            columns: response.columns || [],
            rows: response.rows || [],
            totalRows: response.totalRows || response.rows.length,
            totalPages: response.totalPages || 1,
            pageSize: response.pageSize || 50,
            page: response.page || 1
        };
    }
    
    return {
        columns: [],
        rows: [],
        totalRows: 0,
        totalPages: 1,
        pageSize: 10,
        page: 1
    };
};


/**
 * Extract used by summary from response
 */
export const extractUsedBySummary = (response) => {
    // The data is already transformed in transformUsedBySummaryResponse
    return response?.data || {
        byType: [],
        totalCount: 0
    };
};

// ============================================================
// COMPREHENSIVE SCHEMA DATA (PostgreSQL Version)
// ============================================================

/**
 * Get comprehensive schema data including all object types
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Request parameters
 * @param {boolean} params.useFrontendEndpoints - Use frontend-optimized endpoints
 * @returns {Promise} API response with all schema objects
 */
export const getComprehensiveSchemaData = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { useFrontendEndpoints = false } = params;
    
    try {
        let schemaInfo, currentUser, tables, views, procedures, functions, schemas, sequences, types, triggers, fdw, objectCounts, tablespaceStats, recentTables;
        
        if (useFrontendEndpoints) {
            [schemaInfo, currentUser, tables, views, procedures, functions, schemas, sequences, types, triggers, objectCounts, tablespaceStats, recentTables] = await Promise.all([
                getCurrentSchemaInfo(authorizationHeader).catch(() => ({ data: {} })),
                getCurrentUser(authorizationHeader).catch(() => ({ data: {} })),
                getAllTablesForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllViewsForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllProceduresForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllFunctionsForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllSchemasForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllSequencesForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllTypesForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllTriggersForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllObjectCounts(authorizationHeader).catch(() => ({ data: { counts: [], totalObjects: 0 } })),
                getTablespaceStats(authorizationHeader).catch(() => ({ data: { tablespaces: [] } })),
                getRecentTables(authorizationHeader, 7).catch(() => ({ data: [] }))
            ]);
            
            fdw = { data: [] };
        } else {
            [schemaInfo, currentUser, tables, views, procedures, functions, schemas, sequences, types, triggers, fdw, objectCounts, tablespaceStats, recentTables] = await Promise.all([
                getCurrentSchemaInfo(authorizationHeader).catch(() => ({ data: {} })),
                getCurrentUser(authorizationHeader).catch(() => ({ data: {} })),
                getAllTables(authorizationHeader).catch(() => ({ data: [] })),
                getAllViews(authorizationHeader).catch(() => ({ data: [] })),
                getAllProcedures(authorizationHeader).catch(() => ({ data: [] })),
                getAllFunctions(authorizationHeader).catch(() => ({ data: [] })),
                getAllSchemas(authorizationHeader).catch(() => ({ data: [] })),
                getAllSequences(authorizationHeader).catch(() => ({ data: [] })),
                getAllTypes(authorizationHeader).catch(() => ({ data: [] })),
                getAllTriggers(authorizationHeader).catch(() => ({ data: [] })),
                getAllForeignDataWrappers(authorizationHeader).catch(() => ({ data: [] })),
                getObjectCountByType(authorizationHeader).catch(() => ({ data: { counts: [], totalObjects: 0 } })),
                getTablespaceStats(authorizationHeader).catch(() => ({ data: { tablespaces: [] } })),
                getRecentTables(authorizationHeader, 7).catch(() => ({ data: [] }))
            ]);
        }

        const response = {
            responseCode: 200,
            message: "Comprehensive schema data retrieved successfully",
            data: {
                schemaInfo: schemaInfo.data || {},
                currentUser: currentUser.data || {},
                tables: {
                    objects: tables.data || [],
                    totalCount: (tables.data || []).length
                },
                views: {
                    objects: views.data || [],
                    totalCount: (views.data || []).length
                },
                materializedViews: {
                    objects: [],
                    totalCount: 0
                },
                procedures: {
                    objects: procedures.data || [],
                    totalCount: (procedures.data || []).length
                },
                functions: {
                    objects: functions.data || [],
                    totalCount: (functions.data || []).length
                },
                schemas: {
                    objects: schemas.data || [],
                    totalCount: (schemas.data || []).length
                },
                sequences: {
                    objects: sequences.data || [],
                    totalCount: (sequences.data || []).length
                },
                types: {
                    objects: types.data || [],
                    totalCount: (types.data || []).length
                },
                triggers: {
                    objects: triggers.data || [],
                    totalCount: (triggers.data || []).length
                },
                foreignDataWrappers: fdw ? {
                    objects: fdw.data || [],
                    totalCount: (fdw.data || []).length
                } : { objects: [], totalCount: 0 },
                objectCounts: objectCounts.data || { counts: [], totalObjects: 0 },
                tablespaceStats: tablespaceStats.data || { tablespaces: [] },
                recentTables: recentTables.data || []
            },
            requestId
        };

        return response;
    } catch (error) {
        console.error('Error fetching comprehensive schema data:', error);
        return {
            responseCode: 500,
            message: error.message,
            data: {
                schemaInfo: {},
                currentUser: {},
                tables: { objects: [], totalCount: 0 },
                views: { objects: [], totalCount: 0 },
                materializedViews: { objects: [], totalCount: 0 },
                procedures: { objects: [], totalCount: 0 },
                functions: { objects: [], totalCount: 0 },
                schemas: { objects: [], totalCount: 0 },
                sequences: { objects: [], totalCount: 0 },
                types: { objects: [], totalCount: 0 },
                triggers: { objects: [], totalCount: 0 },
                foreignDataWrappers: { objects: [], totalCount: 0 },
                objectCounts: { counts: [], totalObjects: 0 },
                tablespaceStats: { tablespaces: [] },
                recentTables: []
            },
            requestId
        };
    }
};

// ============================================================
// EXPORT FUNCTIONS (Aliases for backward compatibility)
// ============================================================

// Schema aliases
export const getAllPackages = getAllSchemas;
export const getPackagesBySchema = getSchemasByName;
export const getPackageDetails = getSchemaDetails;
export const getPackageItemsPaginated = getSchemaItemsPaginated;
export const getAllPackagesForFrontend = getAllSchemasForFrontend;
export const getAllPackagesForFrontendPaginated = getAllSchemasForFrontendPaginated;

// DB Link aliases (PostgreSQL FDW)
export const getAllDbLinks = getAllForeignDataWrappers;
export const getDbLinksBySchema = getForeignDataWrappersBySchema;