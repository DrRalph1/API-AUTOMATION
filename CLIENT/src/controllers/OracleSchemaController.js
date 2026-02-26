// controllers/OracleSchemaController.js
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
// 1. PAGINATED OBJECT DETAILS ENDPOINT
// ============================================================

/**
 * Get paginated object details
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE, etc.)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @param {boolean} params.includeCounts - Include total counts without fetching all data
 * @returns {Promise} API response
 */
export const getObjectDetailsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner, page = 1, pageSize = 20, includeCounts = false } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize,
        includeCounts
    });
    
    const url = `/oracle/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/details${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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
// 2. ADVANCED TABLE DATA ENDPOINT
// ============================================================

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
    const { tableName, page = 1, pageSize = 50, sortColumn, sortDirection = 'ASC', filter } = params;
    
    const queryParams = buildQueryParams({
        page,
        pageSize,
        sortColumn,
        sortDirection,
        filter
    });
    
    const url = `/oracle/schema/tables/${encodeURIComponent(tableName)}/data/advanced${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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

// ============================================================
// 3. PAGINATED PROCEDURE PARAMETERS ENDPOINT
// ============================================================

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
    const { procedureName, owner, page = 1, pageSize = 20 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize
    });
    
    const url = `/oracle/schema/procedures/${encodeURIComponent(procedureName)}/parameters${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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
// 4. PAGINATED FUNCTION PARAMETERS ENDPOINT
// ============================================================

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
    const { functionName, owner, page = 1, pageSize = 20 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize
    });
    
    const url = `/oracle/schema/functions/${encodeURIComponent(functionName)}/parameters${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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
// 5. PAGINATED PACKAGE ITEMS ENDPOINT
// ============================================================

/**
 * Get package items with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.packageName - Package name
 * @param {string} params.owner - Package owner (optional)
 * @param {string} params.itemType - Item type (PROCEDURE, FUNCTION, VARIABLE, ALL)
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getPackageItemsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { packageName, owner, itemType = 'ALL', page = 1, pageSize = 20 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        itemType,
        page,
        pageSize
    });
    
    const url = `/oracle/schema/packages/${encodeURIComponent(packageName)}/items${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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
// 6. PAGINATED TABLE COLUMNS ENDPOINT
// ============================================================

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
    const { tableName, owner, page = 1, pageSize = 20 } = params;
    
    const queryParams = buildQueryParams({
        owner,
        page,
        pageSize
    });
    
    const url = `/oracle/schema/tables/${encodeURIComponent(tableName)}/columns${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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
// 7. PAGINATED SEARCH ENDPOINT
// ============================================================

/**
 * Paginated search for Oracle objects
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
    const { query, type, page = 1, pageSize = 20 } = params;
    
    const queryParams = buildQueryParams({
        query,
        type,
        page,
        pageSize
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/search/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

// ============================================================
// 8. GET TOTAL COUNTS ONLY ENDPOINT
// ============================================================

/**
 * Get object counts only
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const getObjectCountsOnly = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner } = params;
    
    const queryParams = buildQueryParams({ owner });
    
    const url = `/oracle/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/counts${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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

// ============================================================
// 9. SCHEMA INFO ENDPOINT
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
        (authHeader) => apiCall(`/oracle/schema/info`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSchemaInfoResponse(response);
    });
};

// ============================================================
// 10. TABLE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle tables
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTables = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/tables`, {
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
        (authHeader) => apiCall(`/oracle/schema/tables/by-schema?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/tables/${encodeURIComponent(tableName)}/details`;
    
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
        (authHeader) => apiCall(`/oracle/schema/tables/search?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/tables/${encodeURIComponent(tableName)}/statistics`;
    
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
        (authHeader) => apiCall(`/oracle/schema/tables/with-row-count`, {
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
        (authHeader) => apiCall(`/oracle/schema/tablespace-stats`, {
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
        (authHeader) => apiCall(`/oracle/schema/tables/recent?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformTablesResponse(response);
    });
};

/**
 * Get tables for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTablesForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/tables`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get table details for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} tableName - Table name
 * @returns {Promise} API response
 */
export const getTableDetailsForFrontend = async (authorizationHeader, tableName) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/frontend/tables/${encodeURIComponent(tableName)}/details`;
    
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
 * Get table data with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.tableName - Table name
 * @param {number} params.page - Page number (0-based)
 * @param {number} params.pageSize - Number of rows per page
 * @param {string} params.sortColumn - Column to sort by
 * @param {string} params.sortDirection - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getTableData = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { tableName, page = 0, pageSize = 50, sortColumn, sortDirection = 'ASC' } = params;
    
    const queryParams = buildQueryParams({
        page,
        pageSize,
        sortColumn,
        sortDirection
    });
    
    const url = `/oracle/schema/tables/${encodeURIComponent(tableName)}/data${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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

// ============================================================
// 11. VIEW ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle views
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllViews = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/views`, {
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
        (authHeader) => apiCall(`/oracle/schema/views/by-schema?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/views/${encodeURIComponent(viewName)}/details`;
    
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

/**
 * Get views for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllViewsForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/views`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 12. PROCEDURE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle procedures
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllProcedures = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/procedures`, {
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
        (authHeader) => apiCall(`/oracle/schema/procedures/by-schema?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/procedures/${encodeURIComponent(procedureName)}/details`;
    
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
 * Get procedures for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllProceduresForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/procedures`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 13. FUNCTION ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle functions
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllFunctions = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/functions`, {
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
        (authHeader) => apiCall(`/oracle/schema/functions/by-schema?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/functions/${encodeURIComponent(functionName)}/details`;
    
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
 * Get functions for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllFunctionsForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/functions`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 14. PACKAGE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle packages
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllPackages = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/packages`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPackagesResponse(response);
    });
};

/**
 * Get packages by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getPackagesBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/packages/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPackagesResponse(response);
    });
};

/**
 * Get package details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} packageName - Package name
 * @returns {Promise} API response
 */
export const getPackageDetails = async (authorizationHeader, packageName) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/packages/${encodeURIComponent(packageName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPackageDetailsResponse(response, packageName);
    });
};

/**
 * Get packages for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllPackagesForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/packages`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 15. SEQUENCE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle sequences
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSequences = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/sequences`, {
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
        (authHeader) => apiCall(`/oracle/schema/sequences/by-schema?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/sequences/${encodeURIComponent(sequenceName)}/details`;
    
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

/**
 * Get sequences for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSequencesForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/sequences`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 16. SYNONYM ENDPOINTS - ENHANCED + FRONTEND
// ============================================================

/**
 * Get all Oracle synonyms
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSynonyms = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/synonyms`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSynonymsResponse(response);
    });
};

/**
 * Get synonyms by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getSynonymsBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/synonyms/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSynonymsResponse(response);
    });
};

/**
 * Get synonym details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} synonymName - Synonym name
 * @returns {Promise} API response
 */
export const getSynonymDetails = async (authorizationHeader, synonymName) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/synonyms/${encodeURIComponent(synonymName)}/details`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSynonymDetailsResponse(response, synonymName);
    });
};

/**
 * Get all synonyms with details (enhanced)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSynonymsWithDetails = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/synonyms/details`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

/**
 * Get synonyms by target type
 * @param {string} authorizationHeader - Bearer token
 * @param {string} targetType - Target object type (TABLE, VIEW, PROCEDURE, etc.)
 * @returns {Promise} API response
 */
export const getSynonymsByTargetType = async (authorizationHeader, targetType) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/synonyms/byTargetType/${encodeURIComponent(targetType)}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSynonymsResponse(response);
    });
};

/**
 * Resolve synonym to its target object
 * @param {string} authorizationHeader - Bearer token
 * @param {string} synonymName - Synonym name
 * @returns {Promise} API response
 */
export const resolveSynonym = async (authorizationHeader, synonymName) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/synonyms/${encodeURIComponent(synonymName)}/resolve`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformResolveSynonymResponse(response);
    });
};

/**
 * Validate synonym and check if target exists
 * @param {string} authorizationHeader - Bearer token
 * @param {string} synonymName - Synonym name
 * @returns {Promise} API response
 */
export const validateSynonym = async (authorizationHeader, synonymName) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/synonyms/${encodeURIComponent(synonymName)}/validate`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformValidateSynonymResponse(response);
    });
};

/**
 * Get synonyms for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllSynonymsForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/synonyms`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 17. TYPE ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle types
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTypes = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/types`, {
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
        (authHeader) => apiCall(`/oracle/schema/types/by-schema?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/types/${encodeURIComponent(typeName)}/details`;
    
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

/**
 * Get types for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTypesForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/types`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 18. TRIGGER ENDPOINTS - FRONTEND FORMAT
// ============================================================

/**
 * Get all Oracle triggers
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTriggers = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/triggers`, {
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
        (authHeader) => apiCall(`/oracle/schema/triggers/by-schema?${queryParams.toString()}`, {
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
    
    const url = `/oracle/schema/triggers/${encodeURIComponent(triggerName)}/details`;
    
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

/**
 * Get triggers for frontend (optimized format)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllTriggersForFrontend = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/triggers`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformFrontendResponse(response);
    });
};

// ============================================================
// 19. DATABASE LINK ENDPOINTS
// ============================================================

/**
 * Get all database links
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllDbLinks = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/db-links`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDbLinksResponse(response);
    });
};

/**
 * Get database links by schema
 * @param {string} authorizationHeader - Bearer token
 * @param {string} schemaName - Schema name
 * @returns {Promise} API response
 */
export const getDbLinksBySchema = async (authorizationHeader, schemaName) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ schemaName });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/db-links/by-schema?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDbLinksResponse(response);
    });
};

// ============================================================
// 20. GENERAL OBJECT ENDPOINTS
// ============================================================

/**
 * Get all Oracle objects
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllObjects = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/objects`, {
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
        (authHeader) => apiCall(`/oracle/schema/objects/by-schema?${queryParams.toString()}`, {
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
        (authHeader) => apiCall(`/oracle/schema/objects/search?${queryParams.toString()}`, {
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
        (authHeader) => apiCall(`/oracle/schema/objects/count-by-type`, {
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
        (authHeader) => apiCall(`/oracle/schema/objects/invalid`, {
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
    
    const url = `/oracle/schema/objects/by-status/${encodeURIComponent(status)}`;
    
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
// 21. OBJECT DETAILS AND VALIDATION ENDPOINTS
// ============================================================

/**
 * Get object details by name and type
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, FUNCTION, PACKAGE, etc.)
 * @param {string} params.objectName - Object name
 * @param {string} params.owner - Object owner (optional)
 * @returns {Promise} API response
 */
export const getObjectDetails = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName, owner } = params;
    
    const queryParams = buildQueryParams({ owner });
    const url = `/oracle/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/details${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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
 * Validate object existence and accessibility
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectName - Object name
 * @param {string} params.objectType - Object type
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
        (authHeader) => apiCall(`/oracle/schema/objects/validate?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformValidateObjectResponse(response);
    });
};

// ============================================================
// 22. SEARCH ENDPOINTS - ENHANCED
// ============================================================

/**
 * Advanced search for Oracle objects (frontend format)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.query - Search query
 * @param {string} params.type - Object type to search (optional)
 * @param {number} params.maxResults - Maximum number of results (default: 100)
 * @returns {Promise} API response
 */
export const searchObjectsAdvanced = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { query, type, maxResults = 100 } = params;
    
    const queryParams = buildQueryParams({
        query,
        type,
        maxResults
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/search?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSearchResponse(response);
    });
};

/**
 * Comprehensive search across all objects including synonym targets
 * @param {string} authorizationHeader - Bearer token
 * @param {string} pattern - Search pattern
 * @returns {Promise} API response
 */
export const comprehensiveSearch = async (authorizationHeader, pattern) => {
    const requestId = generateRequestId();
    const queryParams = buildQueryParams({ pattern });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/comprehensive-search?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformComprehensiveSearchResponse(response);
    });
};

// ============================================================
// 23. DDL ENDPOINTS
// ============================================================

/**
 * Get object DDL
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (table, view, procedure, function, package, trigger, synonym, sequence, type)
 * @param {string} params.objectName - Object name
 * @returns {Promise} API response
 */
export const getObjectDDL = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName } = params;
    
    const url = `/oracle/schema/${objectType.toUpperCase()}s/${encodeURIComponent(objectName)}/ddl`;
    
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

/**
 * Get object size information
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.objectType - Object type (TABLE, INDEX, etc.)
 * @param {string} params.objectName - Object name
 * @returns {Promise} API response
 */
export const getObjectSize = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { objectType, objectName } = params;
    
    const url = `/oracle/schema/objects/${encodeURIComponent(objectType)}/${encodeURIComponent(objectName)}/size`;
    
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

// ============================================================
// 24. RECENT OBJECTS ENDPOINTS
// ============================================================

/**
 * Get objects modified in the last N days
 * @param {string} authorizationHeader - Bearer token
 * @param {number} days - Number of days
 * @returns {Promise} API response
 */
export const getRecentObjects = async (authorizationHeader, days) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/recent/${encodeURIComponent(days)}`;
    
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
// 25. EXECUTE QUERY ENDPOINT
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
        (authHeader) => apiCall(`/oracle/schema/execute`, {
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
// 26. DIAGNOSTICS ENDPOINT
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
        (authHeader) => apiCall(`/oracle/schema/diagnose`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDiagnosticsResponse(response);
    });
};

// ============================================================
// PAGINATED ENDPOINTS FOR FRONTEND
// ============================================================

/**
 * Get paginated tables
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getTablesPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/tables/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated views
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getViewsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/views/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated procedures
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getProceduresPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/procedures/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated functions
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getFunctionsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/functions/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated packages
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getPackagesPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/packages/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated synonyms
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getSynonymsPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/synonyms/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated sequences
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getSequencesPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/sequences/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated types
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getTypesPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/types/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get paginated triggers
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {number} params.page - Page number (1-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise} API response
 */
export const getTriggersPaginated = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { page = 1, pageSize = 50 } = params;
    
    const queryParams = buildQueryParams({ page, pageSize });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/triggers/paginated?${queryParams.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformPaginatedResponse(response);
    });
};

/**
 * Get all object counts
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllObjectCounts = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/frontend/counts`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformObjectCountResponse(response);
    });
};

/**
 * Resolve synonym target (lazy loading)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} synonymName - Synonym name
 * @returns {Promise} API response
 */
export const resolveSynonymTarget = async (authorizationHeader, synonymName) => {
    const requestId = generateRequestId();
    
    const url = `/oracle/schema/frontend/synonyms/${encodeURIComponent(synonymName)}/resolve`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformResolveSynonymResponse(response);
    });
};

// ============================================================
// COMPREHENSIVE SCHEMA DATA (Enhanced)
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
        let schemaInfo, tables, views, procedures, functions, packages, sequences, synonyms, types, triggers, dbLinks;
        
        if (useFrontendEndpoints) {
            [schemaInfo, tables, views, procedures, functions, packages, sequences, synonyms, types, triggers] = await Promise.all([
                getCurrentSchemaInfo(authorizationHeader).catch(() => ({ data: {} })),
                getAllTablesForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllViewsForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllProceduresForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllFunctionsForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllPackagesForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllSequencesForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllSynonymsForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllTypesForFrontend(authorizationHeader).catch(() => ({ data: [] })),
                getAllTriggersForFrontend(authorizationHeader).catch(() => ({ data: [] }))
            ]);
            
            dbLinks = { data: [] };
        } else {
            [schemaInfo, tables, views, procedures, functions, packages, sequences, synonyms, types, triggers, dbLinks] = await Promise.all([
                getCurrentSchemaInfo(authorizationHeader).catch(() => ({ data: {} })),
                getAllTables(authorizationHeader).catch(() => ({ data: [] })),
                getAllViews(authorizationHeader).catch(() => ({ data: [] })),
                getAllProcedures(authorizationHeader).catch(() => ({ data: [] })),
                getAllFunctions(authorizationHeader).catch(() => ({ data: [] })),
                getAllPackages(authorizationHeader).catch(() => ({ data: [] })),
                getAllSequences(authorizationHeader).catch(() => ({ data: [] })),
                getAllSynonyms(authorizationHeader).catch(() => ({ data: [] })),
                getAllTypes(authorizationHeader).catch(() => ({ data: [] })),
                getAllTriggers(authorizationHeader).catch(() => ({ data: [] })),
                getAllDbLinks(authorizationHeader).catch(() => ({ data: [] }))
            ]);
        }

        const response = {
            responseCode: 200,
            message: "Comprehensive schema data retrieved successfully",
            data: {
                schemaInfo: schemaInfo.data || {},
                tables: {
                    objects: tables.data || [],
                    totalCount: (tables.data || []).length
                },
                views: {
                    objects: views.data || [],
                    totalCount: (views.data || []).length
                },
                procedures: {
                    objects: procedures.data || [],
                    totalCount: (procedures.data || []).length
                },
                functions: {
                    objects: functions.data || [],
                    totalCount: (functions.data || []).length
                },
                packages: {
                    objects: packages.data || [],
                    totalCount: (packages.data || []).length
                },
                sequences: {
                    objects: sequences.data || [],
                    totalCount: (sequences.data || []).length
                },
                synonyms: {
                    objects: synonyms.data || [],
                    totalCount: (synonyms.data || []).length
                },
                types: {
                    objects: types.data || [],
                    totalCount: (types.data || []).length
                },
                triggers: {
                    objects: triggers.data || [],
                    totalCount: (triggers.data || []).length
                },
                dbLinks: dbLinks ? {
                    objects: dbLinks.data || [],
                    totalCount: (dbLinks.data || []).length
                } : { objects: [], totalCount: 0 }
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
                tables: { objects: [], totalCount: 0 },
                views: { objects: [], totalCount: 0 },
                procedures: { objects: [], totalCount: 0 },
                functions: { objects: [], totalCount: 0 },
                packages: { objects: [], totalCount: 0 },
                sequences: { objects: [], totalCount: 0 },
                synonyms: { objects: [], totalCount: 0 },
                types: { objects: [], totalCount: 0 },
                triggers: { objects: [], totalCount: 0 },
                dbLinks: { objects: [], totalCount: 0 }
            },
            requestId
        };
    }
};

// ============================================================
// RESPONSE HANDLERS (Enhanced)
// ============================================================

/**
 * Handle standardized API responses
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleSchemaBrowserResponse = (response) => {
    if (!response) {
        throw new Error('No response received from schema browser service');
    }

    if (response.responseCode === 200 || response.responseCode === 201) {
        return response.data || response;
    }

    switch (response.responseCode) {
        case 204: return { message: 'No data found', data: [] };
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Object not found: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        case 503: throw new Error(`Service Unavailable: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

// ============================================================
// TRANSFORM FUNCTIONS (Enhanced)
// ============================================================

/**
 * Transform paginated response
 */
const transformPaginatedResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        items: data.items || data.data || [],
        totalCount: data.totalCount || data.total || 0,
        totalPages: data.totalPages || data.pages || 1,
        page: data.page || 1,
        pageSize: data.pageSize || 20,
        hasNext: data.hasNext || (data.page < data.totalPages),
        hasPrev: data.hasPrev || (data.page > 1)
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform advanced table data response
 */
const transformTableDataAdvancedResponse = (response, tableName, params) => {
    const data = response.data || {};
    
    const transformedData = {
        tableName: tableName,
        columns: data.columns || [],
        rows: data.rows || [],
        totalRows: data.totalRows || 0,
        totalPages: data.totalPages || 1,
        page: data.page || params.page || 1,
        pageSize: data.pageSize || params.pageSize || 50,
        sortColumn: params.sortColumn,
        sortDirection: params.sortDirection,
        filter: params.filter,
        hasNext: data.hasNext || (data.page < data.totalPages),
        hasPrev: data.hasPrev || (data.page > 1)
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform object counts only response
 */
const transformObjectCountsOnlyResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        objectName: data.object_name,
        objectType: data.object_type,
        owner: data.owner,
        counts: {
            columns: data.columns || 0,
            parameters: data.parameters || 0,
            items: data.items || 0,
            rows: data.rows || 0,
            dependencies: data.dependencies || 0
        },
        timestamp: data.timestamp || new Date().toISOString()
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform schema info response
 */
const transformSchemaInfoResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        currentUser: data.current_user || data.user || '',
        schemaName: data.schema_name || data.schema || '',
        databaseVersion: data.database_version || data.version || '',
        defaultTablespace: data.default_tablespace || '',
        tempTablespace: data.temp_tablespace || '',
        created: data.created
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform tables response
 */
const transformTablesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.tables || [])).map((table, index) => ({
        id: `table-${index + 1}`,
        name: table.table_name || table.name,
        owner: table.owner || 'HR',
        type: 'TABLE',
        status: table.status || 'VALID',
        numRows: table.num_rows || 0,
        lastAnalyzed: table.last_analyzed,
        created: table.created,
        comment: table.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform table details response
 */
const transformTableDetailsResponse = (response, tableName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `table-${Date.now()}`,
        name: data.table_name || tableName,
        owner: data.owner || 'HR',
        type: 'TABLE',
        status: data.status || 'VALID',
        columns: (data.columns || []).map((col, idx) => ({
            name: col.column_name || col.name,
            type: col.data_type || col.type,
            nullable: col.nullable === 'Y' || col.nullable === true ? 'Y' : 'N',
            dataLength: col.data_length || 0,
            dataPrecision: col.data_precision,
            dataScale: col.data_scale,
            default: col.data_default,
            position: col.column_id || idx + 1,
            comment: col.comments
        })),
        constraints: (data.constraints || []).map(con => ({
            name: con.constraint_name,
            type: con.constraint_type,
            columns: con.columns || [],
            searchCondition: con.search_condition,
            rOwner: con.r_owner,
            rConstraintName: con.r_constraint_name,
            deleteRule: con.delete_rule,
            status: con.status
        })),
        indexes: (data.indexes || []).map(idx => ({
            name: idx.index_name,
            type: idx.index_type,
            uniqueness: idx.uniqueness,
            columns: idx.column_names || [],
            status: idx.status
        })),
        triggers: (data.triggers || []).map(trg => ({
            name: trg.trigger_name,
            type: trg.trigger_type,
            event: trg.triggering_event,
            status: trg.status
        })),
        statistics: {
            numRows: data.num_rows || 0,
            blocks: data.blocks || 0,
            avgRowLen: data.avg_row_len || 0,
            lastAnalyzed: data.last_analyzed
        },
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform table data response
 */
const transformTableDataResponse = (response, tableName, params) => {
    const data = response.data || {};
    
    // Handle different response structures
    let rows = [];
    let columns = [];
    let totalRows = 0;
    let totalPages = 1;
    let page = params?.page || 0;
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
        hasNext: page < totalPages - 1,
        hasPrev: page > 0
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform views response
 */
const transformViewsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.views || [])).map((view, index) => ({
        id: `view-${index + 1}`,
        name: view.view_name || view.name,
        owner: view.owner || 'HR',
        type: 'VIEW',
        status: view.status || 'VALID',
        textLength: view.text_length || 0,
        readOnly: view.read_only === 'Y' || view.read_only === true,
        created: view.created,
        comment: view.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform view details response
 */
const transformViewDetailsResponse = (response, viewName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `view-${Date.now()}`,
        name: data.view_name || viewName,
        owner: data.owner || 'HR',
        type: 'VIEW',
        status: data.status || data.object_status || 'VALID',
        text: data.text || data.viewDefinition || '',
        textLength: data.text_length || 0,
        readOnly: data.read_only === 'Y' || data.read_only === true,
        columns: (data.columns || []).map((col, idx) => ({
            name: col.column_name || col.name,
            type: col.data_type || col.type,
            nullable: col.nullable === 'Y' || col.nullable === true ? 'Y' : 'N',
            position: col.column_id || col.position || idx + 1,
            dataLength: col.data_length || 0,
            dataPrecision: col.data_precision,
            dataScale: col.data_scale
        })),
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform procedures response
 */
const transformProceduresResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.procedures || [])).map((proc, index) => ({
        id: `procedure-${index + 1}`,
        name: proc.procedure_name || proc.name,
        owner: proc.owner || 'HR',
        type: 'PROCEDURE',
        status: proc.status || 'VALID',
        created: proc.created,
        lastModified: proc.last_ddl_time,
        comment: proc.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform procedure details response
 */
const transformProcedureDetailsResponse = (response, procedureName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `procedure-${Date.now()}`,
        name: data.procedure_name || procedureName,
        owner: data.owner || 'HR',
        type: 'PROCEDURE',
        status: data.status || data.object_status || 'VALID',
        parameters: (data.parameters || []).map((param, idx) => ({
            name: param.argument_name || param.name,
            type: param.data_type || param.type,
            mode: param.in_out || param.mode || 'IN',
            position: param.position || idx + 1,
            dataLength: param.data_length,
            dataPrecision: param.data_precision,
            dataScale: param.data_scale,
            defaultValue: param.default_value
        })),
        parameterCount: data.parameters?.length || 0,
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform functions response
 */
const transformFunctionsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.functions || [])).map((func, index) => ({
        id: `function-${index + 1}`,
        name: func.function_name || func.name,
        owner: func.owner || 'HR',
        type: 'FUNCTION',
        status: func.status || 'VALID',
        created: func.created,
        lastModified: func.last_ddl_time,
        comment: func.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform function details response
 */
const transformFunctionDetailsResponse = (response, functionName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `function-${Date.now()}`,
        name: data.function_name || functionName,
        owner: data.owner || 'HR',
        type: 'FUNCTION',
        status: data.status || data.object_status || 'VALID',
        returnType: data.return_type || data.returnType || '',
        parameters: (data.parameters || []).map((param, idx) => ({
            name: param.argument_name || param.name,
            type: param.data_type || param.type,
            mode: param.in_out || param.mode || 'IN',
            position: param.position || idx + 1,
            dataLength: param.data_length,
            dataPrecision: param.data_precision,
            dataScale: param.data_scale,
            defaultValue: param.default_value
        })),
        parameterCount: data.parameters?.length || 0,
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform packages response
 */
const transformPackagesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.packages || [])).map((pkg, index) => ({
        id: `package-${index + 1}`,
        name: pkg.package_name || pkg.name,
        owner: pkg.owner || 'HR',
        type: 'PACKAGE',
        status: pkg.status || 'VALID',
        created: pkg.created,
        lastModified: pkg.last_ddl_time,
        comment: pkg.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform package details response
 */
const transformPackageDetailsResponse = (response, packageName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `package-${Date.now()}`,
        name: data.package_name || packageName,
        owner: data.owner || 'HR',
        type: 'PACKAGE',
        status: data.status || data.object_status || 'VALID',
        spec: data.spec || data.packageSpec || '',
        body: data.body || data.packageBody || '',
        procedures: (data.procedures || []).map(proc => ({
            name: proc.procedure_name || proc.name,
            parameters: (proc.parameters || []).map(p => p.name),
            parameterCount: proc.parameters?.length || 0
        })),
        functions: (data.functions || []).map(func => ({
            name: func.function_name || func.name,
            returnType: func.return_type,
            parameters: (func.parameters || []).map(p => p.name),
            parameterCount: func.parameters?.length || 0
        })),
        variables: data.variables || [],
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform sequences response
 */
const transformSequencesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.sequences || [])).map((seq, index) => ({
        id: `sequence-${index + 1}`,
        name: seq.sequence_name || seq.name,
        owner: seq.sequence_owner || seq.owner || 'HR',
        type: 'SEQUENCE',
        minValue: seq.min_value || 0,
        maxValue: seq.max_value || 0,
        incrementBy: seq.increment_by || 1,
        cycleFlag: seq.cycle_flag === 'Y' || seq.cycle_flag === true,
        cacheSize: seq.cache_size || 0,
        lastNumber: seq.last_number || 0,
        created: seq.created
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform sequence details response
 */
const transformSequenceDetailsResponse = (response, sequenceName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `sequence-${Date.now()}`,
        name: data.sequence_name || sequenceName,
        owner: data.owner || 'HR',
        type: 'SEQUENCE',
        minValue: data.min_value || 0,
        maxValue: data.max_value || 0,
        incrementBy: data.increment_by || 1,
        cycleFlag: data.cycle_flag === 'Y' || data.cycle_flag === true,
        orderFlag: data.order_flag === 'Y' || data.order_flag === true,
        cacheSize: data.cache_size || 0,
        lastNumber: data.last_number || 0,
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform synonyms response
 */
const transformSynonymsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.synonyms || [])).map((syn, index) => ({
        id: `synonym-${index + 1}`,
        name: syn.synonym_name || syn.name,
        owner: syn.owner || 'PUBLIC',
        type: 'SYNONYM',
        tableOwner: syn.table_owner || '',
        tableName: syn.table_name || '',
        dbLink: syn.db_link || '',
        public: syn.public === 'Y' || syn.public === true,
        created: syn.created
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform synonym details response
 */
const transformSynonymDetailsResponse = (response, synonymName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `synonym-${Date.now()}`,
        name: data.synonym_name || synonymName,
        owner: data.owner || 'HR',
        type: 'SYNONYM',
        tableOwner: data.table_owner || '',
        tableName: data.table_name || '',
        dbLink: data.db_link || '',
        public: data.public === 'Y' || data.public === true,
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform resolve synonym response
 */
const transformResolveSynonymResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        synonymName: data.synonym_name,
        owner: data.owner,
        targetOwner: data.target_owner,
        targetName: data.target_name,
        targetType: data.target_type,
        dbLink: data.db_link,
        valid: data.valid === true,
        exists: data.exists === true
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform validate synonym response
 */
const transformValidateSynonymResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        synonymName: data.synonym_name,
        owner: data.owner,
        valid: data.valid === true,
        targetExists: data.target_exists === true,
        targetStatus: data.target_status,
        message: data.message || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform types response
 */
const transformTypesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.types || [])).map((type, index) => ({
        id: `type-${index + 1}`,
        name: type.type_name || type.name,
        owner: type.owner || 'HR',
        type: 'TYPE',
        status: type.status || 'VALID',
        typecode: type.typecode || '',
        attributes: type.attributes || 0,
        methods: type.methods || 0,
        created: type.created
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform type details response
 */
const transformTypeDetailsResponse = (response, typeName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `type-${Date.now()}`,
        name: data.type_name || typeName,
        owner: data.owner || 'HR',
        type: 'TYPE',
        status: data.status || data.object_status || 'VALID',
        typecode: data.typecode || '',
        attributes: (data.attributes || []).map(attr => ({
            name: attr.attribute_name || attr.name,
            type: attr.attribute_type || attr.type,
            dataType: attr.data_type || attr.type,
            dataLength: attr.data_length,
            dataPrecision: attr.data_precision,
            dataScale: attr.data_scale
        })),
        methods: (data.methods || []).map(method => ({
            name: method.method_name || method.name,
            type: method.method_type || '',
            parameters: (method.parameters || []).map(p => p.name)
        })),
        attributeCount: data.attributes?.length || 0,
        methodCount: data.methods?.length || 0,
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform triggers response
 */
const transformTriggersResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.triggers || [])).map((trg, index) => ({
        id: `trigger-${index + 1}`,
        name: trg.trigger_name || trg.name,
        owner: trg.owner || 'HR',
        type: 'TRIGGER',
        status: trg.trigger_status || trg.status || 'ENABLED',
        objectStatus: trg.object_status || 'VALID',
        triggerType: trg.trigger_type || '',
        event: trg.triggering_event || '',
        tableName: trg.table_name || '',
        created: trg.created
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform trigger details response
 */
const transformTriggerDetailsResponse = (response, triggerName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `trigger-${Date.now()}`,
        name: data.trigger_name || triggerName,
        owner: data.owner || 'HR',
        type: 'TRIGGER',
        status: data.trigger_status || data.status || 'ENABLED',
        objectStatus: data.object_status || 'VALID',
        triggerType: data.trigger_type || '',
        triggeringEvent: data.triggering_event || '',
        tableName: data.table_name || '',
        tableOwner: data.table_owner || '',
        body: data.trigger_body || '',
        description: data.description || '',
        whenClause: data.when_clause || '',
        referencing: data.referencing_names || '',
        created: data.created,
        lastModified: data.last_ddl_time,
        comment: data.comments || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform database links response
 */
const transformDbLinksResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.dbLinks || [])).map((link, index) => ({
        id: `dblink-${index + 1}`,
        name: link.db_link || link.name,
        owner: link.owner || 'PUBLIC',
        username: link.username || '',
        host: link.host || '',
        created: link.created,
        public: link.public === 'Y' || link.public === true,
        authenticated: link.authenticated === 'Y' || link.authenticated === true
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform objects response
 */
const transformObjectsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = (Array.isArray(data) ? data : (data.objects || [])).map((obj, index) => ({
        id: `object-${index + 1}`,
        name: obj.object_name || obj.name,
        owner: obj.owner || 'HR',
        type: obj.object_type || obj.type,
        status: obj.status || 'VALID',
        created: obj.created,
        lastModified: obj.last_ddl_time,
        temporary: obj.temporary === 'Y' || obj.temporary === true,
        generated: obj.generated === 'Y' || obj.generated === true,
        secondary: obj.secondary === 'Y' || obj.secondary === true
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform object details response
 */
const transformObjectDetailsResponse = (response, objectType, objectName) => {
    const data = response.data || {};
    
    const transformedData = {
        id: `object-${Date.now()}`,
        name: objectName,
        type: objectType,
        ...data
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform validate object response
 */
const transformValidateObjectResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        objectName: data.object_name,
        objectType: data.object_type,
        owner: data.owner,
        exists: data.exists === true,
        status: data.status,
        accessible: data.accessible === true,
        message: data.message || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform object count response
 */
const transformObjectCountResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        counts: data.counts || [],
        totalObjects: data.totalObjects || 0,
        byType: (data.counts || []).reduce((acc, item) => {
            acc[item.object_type] = item.count;
            return acc;
        }, {})
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform object size response
 */
const transformObjectSizeResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        objectName: data.object_name,
        objectType: data.object_type,
        owner: data.owner,
        bytes: data.bytes || 0,
        size: formatBytes(data.bytes || 0),
        extents: data.extents || 0,
        blocks: data.blocks || 0
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

/**
 * Transform search response
 */
const transformSearchResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = {
        results: (Array.isArray(data) ? data : (data.results || [])).map((result, index) => ({
            id: `result-${index + 1}`,
            name: result.object_name || result.name,
            owner: result.owner || 'HR',
            type: result.object_type || result.type,
            status: result.status || 'VALID',
            created: result.created,
            lastModified: result.last_ddl_time
        })),
        totalCount: data.totalCount || (Array.isArray(data) ? data.length : 0)
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform comprehensive search response
 */
const transformComprehensiveSearchResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        results: (data.results || []).map((result, index) => ({
            id: `result-${index + 1}`,
            name: result.object_name,
            owner: result.owner,
            type: result.object_type,
            status: result.status,
            matchedBy: result.matched_by || 'DIRECT'
        })),
        totalCount: data.totalCount || 0,
        searchPattern: data.search_pattern
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform query response
 */
const transformQueryResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        rows: data.rows || [],
        columns: data.columns || [],
        rowCount: data.rowCount || 0,
        executionTimeMs: data.executionTimeMs || 0,
        query: data.query || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform statistics response
 */
const transformStatisticsResponse = (response, objectName) => {
    const data = response.data || {};
    
    const transformedData = {
        objectName: objectName,
        statistics: {
            numRows: data.num_rows || 0,
            blocks: data.blocks || 0,
            emptyBlocks: data.empty_blocks || 0,
            avgRowLen: data.avg_row_len || 0,
            lastAnalyzed: data.last_analyzed,
            sampleSize: data.sample_size || 0,
            globalStats: data.global_stats || '',
            userStats: data.user_stats || '',
            stattypeLocked: data.stattype_locked || '',
            staleStats: data.stale_stats || 'NO'
        },
        columnStatistics: (data.column_statistics || []).map(col => ({
            columnName: col.column_name,
            distinctCount: col.num_distinct,
            nullCount: col.num_nulls,
            avgColLen: col.avg_col_len,
            histogram: col.histogram || 'NONE'
        }))
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform tables with row count response
 */
const transformTablesWithRowCountResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        tables: (data.tables || []).map((table, index) => ({
            id: `table-${index + 1}`,
            name: table.table_name || table.name,
            owner: table.owner || 'HR',
            rowCount: table.row_count || table.num_rows || 0,
            estimatedRows: table.estimated_rows || 0,
            lastAnalyzed: table.last_analyzed,
            sizeBytes: table.bytes || 0,
            size: formatBytes(table.bytes || 0)
        })),
        totalTables: data.totalCount || 0,
        totalRows: data.totalRows || 0
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform tablespace statistics response
 */
const transformTablespaceStatsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        tablespaces: (data.tablespaces || []).map((ts, index) => ({
            id: `tablespace-${index + 1}`,
            name: ts.tablespace_name || ts.name,
            tableCount: ts.table_count || 0,
            totalSize: formatBytes(ts.total_bytes || 0),
            usedSize: formatBytes(ts.used_bytes || 0),
            freeSize: formatBytes(ts.free_bytes || 0),
            usedPercent: ts.used_percent || 0,
            status: ts.status || 'ONLINE'
        })),
        totalTables: data.totalTables || 0,
        totalTablespaces: data.totalCount || 0
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform diagnostics response
 */
const transformDiagnosticsResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        diagnosticStatus: data.diagnosticStatus || 'COMPLETED',
        connection: {
            status: data.connection?.status || 'UNKNOWN',
            details: data.connection?.details || {}
        },
        schema: {
            status: data.schema?.status || 'UNKNOWN',
            objectCounts: data.schema?.objectCounts || {},
            details: data.schema?.details || {}
        },
        permissions: {
            status: data.permissions?.status || 'UNKNOWN',
            granted: data.permissions?.granted || []
        },
        issues: data.issues || [],
        warnings: data.warnings || [],
        timestamp: data.timestamp || new Date().toISOString()
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform frontend response (already optimized)
 */
const transformFrontendResponse = (response) => {
    return response;
};

// ============================================================
// EXTRACT FUNCTIONS - Enhanced
// ============================================================

export const extractSchemaInfo = (response) => {
    return response?.data || {};
};

export const extractSchemaObjects = (response, objectType) => {
    if (objectType) {
        return response?.data?.[objectType]?.objects || [];
    }
    return response?.data || [];
};

export const extractObjectDetails = (response) => {
    return response?.data || {};
};

export const extractTableData = (response) => {
    if (response?.data?.data) {
        const tableData = response.data.data;
        return {
            columns: tableData.columns || [],
            rows: tableData.rows || [],
            totalRows: tableData.totalRows || tableData.rows?.length || 0,
            totalPages: tableData.totalPages || 1,
            pageSize: tableData.pageSize || 50,
            page: tableData.page || 0
        };
    } else if (response?.data?.columns && response?.data?.rows) {
        return {
            columns: response.data.columns || [],
            rows: response.data.rows || [],
            totalRows: response.data.totalRows || response.data.rows.length,
            totalPages: response.data.totalPages || 1,
            pageSize: response.data.pageSize || 50,
            page: response.data.page || 0
        };
    } else if (response?.columns && response?.rows) {
        return {
            columns: response.columns || [],
            rows: response.rows || [],
            totalRows: response.totalRows || response.rows.length,
            totalPages: response.totalPages || 1,
            pageSize: response.pageSize || 50,
            page: response.page || 0
        };
    }
    
    return {
        columns: [],
        rows: [],
        totalRows: 0,
        totalPages: 1,
        pageSize: 50,
        page: 0
    };
};

export const extractDDL = (response) => {
    return response?.data?.ddl || response?.data || '';
};

export const extractSearchResults = (response) => {
    return response?.data || { results: [], totalCount: 0 };
};

export const extractQueryResults = (response) => {
    return response?.data || { rows: [], columns: [] };
};

export const extractComprehensiveSchemaData = (response) => {
    return response?.data || {
        schemaInfo: {},
        tables: { objects: [], totalCount: 0 },
        views: { objects: [], totalCount: 0 },
        procedures: { objects: [], totalCount: 0 },
        functions: { objects: [], totalCount: 0 },
        packages: { objects: [], totalCount: 0 },
        sequences: { objects: [], totalCount: 0 },
        synonyms: { objects: [], totalCount: 0 },
        types: { objects: [], totalCount: 0 },
        triggers: { objects: [], totalCount: 0 },
        dbLinks: { objects: [], totalCount: 0 }
    };
};

export const extractStatistics = (response) => {
    return response?.data?.statistics || {};
};

export const extractDiagnostics = (response) => {
    return response?.data || {};
};

export const extractObjectCounts = (response) => {
    return response?.data?.counts || [];
};

export const extractResolvedSynonym = (response) => {
    return response?.data || {};
};

export const extractValidatedSynonym = (response) => {
    return response?.data || {};
};

export const extractValidatedObject = (response) => {
    return response?.data || {};
};

export const extractPaginatedData = (response) => {
    return response?.data || {
        items: [],
        totalCount: 0,
        totalPages: 1,
        page: 1,
        pageSize: 20
    };
};

export const extractObjectCountsOnly = (response) => {
    return response?.data || {
        counts: {},
        timestamp: new Date().toISOString()
    };
};

// ============================================================
// UTILITY FUNCTIONS (Enhanced)
// ============================================================

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
 * Format data type for display
 */
export const formatDataType = (type, length, precision, scale) => {
    if (!type) return '';
    
    let formatted = type;
    if (length && type.includes('CHAR')) {
        formatted += `(${length})`;
    } else if (precision) {
        if (scale && scale > 0) {
            formatted += `(${precision},${scale})`;
        } else {
            formatted += `(${precision})`;
        }
    }
    
    return formatted;
};

/**
 * Get object type icon
 */
export const getObjectTypeIcon = (type) => {
    const icons = {
        'TABLE': '📊',
        'VIEW': '👁️',
        'PROCEDURE': '⚙️',
        'FUNCTION': '🔧',
        'PACKAGE': '📦',
        'SEQUENCE': '🔢',
        'SYNONYM': '🔄',
        'TYPE': '📐',
        'TRIGGER': '⚡',
        'INDEX': '📌',
        'DATABASE LINK': '🔗',
        'DB_LINK': '🔗'
    };
    return icons[type?.toUpperCase()] || '📄';
};

/**
 * Get object type color
 */
export const getObjectTypeColor = (type, isDark = true) => {
    const colors = {
        'TABLE': isDark ? '#60A5FA' : '#3B82F6',
        'VIEW': isDark ? '#34D399' : '#10B981',
        'PROCEDURE': isDark ? '#A78BFA' : '#8B5CF6',
        'FUNCTION': isDark ? '#FBBF24' : '#F59E0B',
        'PACKAGE': isDark ? '#94A3B8' : '#6B7280',
        'SEQUENCE': isDark ? '#64748B' : '#4B5563',
        'SYNONYM': isDark ? '#22D3EE' : '#06B6D4',
        'TYPE': isDark ? '#C084FC' : '#A855F7',
        'TRIGGER': isDark ? '#F472B6' : '#EC4899',
        'INDEX': isDark ? '#2DD4BF' : '#14B8A6',
        'DATABASE LINK': isDark ? '#F97316' : '#EA580C',
        'DB_LINK': isDark ? '#F97316' : '#EA580C'
    };
    return colors[type?.toUpperCase()] || (isDark ? '#94A3B8' : '#6B7280');
};

/**
 * Check if object is supported for API generation
 */
export const isSupportedForAPIGeneration = (objectType) => {
    const supportedTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'PACKAGE'];
    return supportedTypes.includes(objectType?.toUpperCase());
};

/**
 * Generate sample query for object
 */
export const generateSampleQuery = (objectName, objectType) => {
    switch (objectType?.toUpperCase()) {
        case 'TABLE':
            return `SELECT * FROM ${objectName} WHERE ROWNUM <= 10`;
        case 'VIEW':
            return `SELECT * FROM ${objectName} WHERE ROWNUM <= 10`;
        case 'PROCEDURE':
            return `BEGIN ${objectName}(parameter1 => value1); END;`;
        case 'FUNCTION':
            return `SELECT ${objectName}(parameter1) FROM DUAL`;
        case 'SEQUENCE':
            return `SELECT ${objectName}.NEXTVAL FROM DUAL`;
        case 'SYNONYM':
            return `SELECT * FROM ${objectName} WHERE ROWNUM <= 10`;
        default:
            return `-- No sample query available for ${objectType}`;
    }
};

/**
 * Refresh schema data
 */
export const refreshSchemaData = async (authorizationHeader) => {
    clearCachedSchemaData('schema_objects');
    return getComprehensiveSchemaData(authorizationHeader);
};

/**
 * Download exported schema data
 */
export const downloadExportedSchema = (exportResult, filename) => {
    if (!exportResult || !exportResult.content) return;
    
    const blob = new Blob([exportResult.content], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename || exportResult.filename || 'export.json';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
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
        'O': 'READ ONLY VIEW',
        'H': 'HASH EXPRESSION',
        'F': 'REF COLUMN',
        'S': 'SUPPLEMENTAL LOGGING'
    };
    return types[type] || type;
};