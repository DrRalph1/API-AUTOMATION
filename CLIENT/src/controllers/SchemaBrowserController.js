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
// CONNECTION MANAGEMENT
// ============================================================

/**
 * Get all schema connections
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response with connections
 */
export const getSchemaConnections = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/connections`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        // Transform backend response to frontend format
        return transformConnectionsResponse(response);
    }).catch(error => {
        console.error('Error fetching schema connections:', error);
        // Return fallback data for development
        return {
            responseCode: 200,
            message: "Connections retrieved successfully (fallback)",
            data: [
                {
                    id: 'conn-1',
                    name: 'CBX_DMX',
                    description: 'Production HR Database',
                    host: 'db-prod.company.com',
                    port: '1521',
                    service: 'ORCL',
                    username: 'HR',
                    status: 'connected',
                    lastUsed: new Date().toISOString()
                },
                {
                    id: 'conn-2',
                    name: 'DEV_SCHEMA',
                    description: 'Development Schema',
                    host: 'db-dev.company.com',
                    port: '1521',
                    service: 'XE',
                    username: 'DEV',
                    status: 'disconnected',
                    lastUsed: new Date().toISOString()
                }
            ],
            requestId
        };
    });
};

// ============================================================
// TABLE ENDPOINTS
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
 * Get table data (rows)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.connectionId - Connection ID
 * @param {string} params.tableName - Table name
 * @param {number} params.page - Page number
 * @param {number} params.pageSize - Page size
 * @param {string} params.sortColumn - Sort column
 * @param {string} params.sortDirection - Sort direction
 * @returns {Promise} API response
 */
export const getTableData = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { connectionId, tableName, page = 1, pageSize = 50, sortColumn, sortDirection = 'ASC' } = params;
    
    const queryParams = buildQueryParams({
        connectionId,
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
// VIEW ENDPOINTS
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

// ============================================================
// PROCEDURE ENDPOINTS
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

// ============================================================
// FUNCTION ENDPOINTS
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

// ============================================================
// PACKAGE ENDPOINTS
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

// ============================================================
// SEQUENCE ENDPOINTS
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

// ============================================================
// SYNONYM ENDPOINTS
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

// ============================================================
// TYPE ENDPOINTS
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

// ============================================================
// TRIGGER ENDPOINTS
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

// ============================================================
// COMPREHENSIVE SCHEMA DATA
// ============================================================

/**
 * Get comprehensive schema data including all object types
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Request parameters
 * @param {string} params.connectionId - Connection ID
 * @returns {Promise} API response with all schema objects
 */
export const getComprehensiveSchemaData = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { connectionId } = params;
    
    try {
        // Fetch all object types in parallel
        const [tables, views, procedures, functions, packages, sequences, synonyms, types, triggers] = await Promise.all([
            getAllTables(authorizationHeader).catch(() => ({ data: [] })),
            getAllViews(authorizationHeader).catch(() => ({ data: [] })),
            getAllProcedures(authorizationHeader).catch(() => ({ data: [] })),
            getAllFunctions(authorizationHeader).catch(() => ({ data: [] })),
            getAllPackages(authorizationHeader).catch(() => ({ data: [] })),
            getAllSequences(authorizationHeader).catch(() => ({ data: [] })),
            getAllSynonyms(authorizationHeader).catch(() => ({ data: [] })),
            getAllTypes(authorizationHeader).catch(() => ({ data: [] })),
            getAllTriggers(authorizationHeader).catch(() => ({ data: [] }))
        ]);

        const response = {
            responseCode: 200,
            message: "Comprehensive schema data retrieved successfully",
            data: {
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
                }
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
                tables: { objects: [], totalCount: 0 },
                views: { objects: [], totalCount: 0 },
                procedures: { objects: [], totalCount: 0 },
                functions: { objects: [], totalCount: 0 },
                packages: { objects: [], totalCount: 0 },
                sequences: { objects: [], totalCount: 0 },
                synonyms: { objects: [], totalCount: 0 },
                types: { objects: [], totalCount: 0 },
                triggers: { objects: [], totalCount: 0 }
            },
            requestId
        };
    }
};

// ============================================================
// OBJECT DDL
// ============================================================

/**
 * Get object DDL
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.connectionId - Connection ID
 * @param {string} params.objectType - Object type
 * @param {string} params.objectName - Object name
 * @returns {Promise} API response with DDL
 */
export const getObjectDDL = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { connectionId, objectType, objectName } = params;
    
    const queryParams = buildQueryParams({ connectionId });
    const url = `/oracle/schema/${objectType.toLowerCase()}s/${encodeURIComponent(objectName)}/ddl${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformDDLResponse(response);
    });
};

// ============================================================
// OBJECT DETAILS
// ============================================================

/**
 * Get object details
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.connectionId - Connection ID
 * @param {string} params.objectType - Object type
 * @param {string} params.objectName - Object name
 * @returns {Promise} API response with object details
 */
export const getObjectDetails = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { connectionId, objectType, objectName } = params;
    
    const queryParams = buildQueryParams({ connectionId });
    const url = `/oracle/schema/${objectType.toLowerCase()}s/${encodeURIComponent(objectName)}/details${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
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

// ============================================================
// SEARCH
// ============================================================

/**
 * Search schema objects
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Parameters
 * @param {string} params.connectionId - Connection ID
 * @param {string} params.searchQuery - Search query
 * @param {string} params.searchType - Search type (optional)
 * @param {number} params.maxResults - Max results
 * @returns {Promise} API response with search results
 */
export const searchSchema = async (authorizationHeader, params = {}) => {
    const requestId = generateRequestId();
    const { connectionId, searchQuery, searchType, maxResults = 100 } = params;
    
    const queryParams = buildQueryParams({
        connectionId,
        query: searchQuery,
        type: searchType,
        maxResults
    });
    
    const url = `/oracle/schema/search${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    ).then(response => {
        return transformSearchResponse(response);
    });
};

// ============================================================
// EXECUTE QUERY
// ============================================================

/**
 * Execute SQL query
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} queryRequest - Query request
 * @param {string} queryRequest.connectionId - Connection ID
 * @param {string} queryRequest.query - SQL query
 * @param {number} queryRequest.timeoutSeconds - Timeout in seconds
 * @param {boolean} queryRequest.readOnly - Read only flag
 * @returns {Promise} API response with query results
 */
export const executeQuery = async (authorizationHeader, queryRequest = {}) => {
    const requestId = generateRequestId();
    const { connectionId, query, timeoutSeconds = 30, readOnly = true } = queryRequest;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/execute`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify({
                connectionId,
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
// GENERATE API
// ============================================================

/**
 * Generate API from database object
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} apiRequest - API generation request
 * @param {string} apiRequest.connectionId - Connection ID
 * @param {string} apiRequest.objectType - Object type
 * @param {string} apiRequest.objectName - Object name
 * @param {string} apiRequest.apiType - API type (REST, GraphQL, etc.)
 * @param {Object} apiRequest.options - Additional options
 * @returns {Promise} API response with generated API
 */
export const generateAPIFromObject = async (authorizationHeader, apiRequest = {}) => {
    const requestId = generateRequestId();
    const { connectionId, objectType, objectName, apiType = 'REST', options = {} } = apiRequest;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/generate-api`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify({
                connectionId,
                objectType,
                objectName,
                apiType,
                ...options
            }),
            requestId: requestId
        })
    ).then(response => {
        return transformApiResponse(response);
    });
};

// ============================================================
// EXPORT SCHEMA
// ============================================================

/**
 * Export schema data
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} exportRequest - Export request
 * @param {string} exportRequest.connectionId - Connection ID
 * @param {string} exportRequest.format - Export format (JSON, SQL, CSV)
 * @param {Array} exportRequest.objectTypes - Object types to export
 * @param {Array} exportRequest.objectNames - Object names to export
 * @returns {Promise} API response with export data
 */
export const exportSchemaData = async (authorizationHeader, exportRequest = {}) => {
    const requestId = generateRequestId();
    const { connectionId, format = 'JSON', objectTypes = [], objectNames = [], options = {} } = exportRequest;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/export`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify({
                connectionId,
                format,
                objectTypes,
                objectNames,
                options
            }),
            requestId: requestId
        })
    ).then(response => {
        return transformExportResponse(response);
    });
};

// ============================================================
// CLEAR CACHE
// ============================================================

/**
 * Clear schema cache
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const clearSchemaCache = async (authorizationHeader) => {
    const requestId = generateRequestId();
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/oracle/schema/cache/clear`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    );
};

// ============================================================
// HEALTH CHECK
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
        (authHeader) => apiCall(`/oracle/schema/health`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            requestId: requestId
        })
    );
};

// ============================================================
// RESPONSE HANDLERS
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
        case 204:
            return { message: 'No data found', data: [] };
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
// TRANSFORMATION FUNCTIONS - Match frontend expectations
// ============================================================

/**
 * Transform connections response to frontend format
 */
const transformConnectionsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((conn, index) => ({
        id: conn.id || `conn-${index + 1}`,
        name: conn.name || conn.connectionName || 'Unknown',
        description: conn.description || '',
        host: conn.host || 'localhost',
        port: conn.port || '1521',
        service: conn.service || conn.serviceName || 'ORCL',
        username: conn.username || conn.user || 'HR',
        status: conn.status || 'connected',
        lastUsed: conn.lastUsed || new Date().toISOString()
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform tables response to frontend format
 */
const transformTablesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((table, index) => ({
        id: `table-${index + 1}`,
        name: table.table_name || table.name,
        owner: table.owner || 'HR',
        type: 'TABLE',
        status: table.status || table.object_status || table.table_status || 'VALID',
        rowCount: table.num_rows || 0,
        size: formatBytes(table.bytes || 0),
        comment: table.comments || '',
        columns: table.columns || [],
        constraints: table.constraints || [],
        indexes: table.indexes || [],
        created: table.created,
        lastModified: table.last_ddl_time || table.last_analyzed,
        tablespace: table.tablespace_name
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform views response to frontend format
 */
const transformViewsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((view, index) => ({
        id: `view-${index + 1}`,
        name: view.view_name || view.name,
        owner: view.owner || 'HR',
        type: 'VIEW',
        status: view.status || view.object_status || 'VALID',
        text: view.text || view.viewDefinition || '',
        textLength: view.text_length || 0,
        readOnly: view.read_only === 'Y' || view.read_only === true,
        columns: view.columns || [],
        created: view.created,
        lastModified: view.last_ddl_time,
        comment: view.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform procedures response to frontend format
 */
const transformProceduresResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((proc, index) => ({
        id: `procedure-${index + 1}`,
        name: proc.procedure_name || proc.name || proc.object_name,
        owner: proc.owner || 'HR',
        type: 'PROCEDURE',
        status: proc.status || proc.object_status || 'VALID',
        parameters: proc.parameters || [],
        parameterCount: proc.parameter_count || 0,
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
 * Transform functions response to frontend format
 */
const transformFunctionsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((func, index) => ({
        id: `function-${index + 1}`,
        name: func.function_name || func.name || func.object_name,
        owner: func.owner || 'HR',
        type: 'FUNCTION',
        status: func.status || func.object_status || 'VALID',
        parameters: func.parameters || [],
        parameterCount: func.parameter_count || 0,
        returnType: func.return_type || func.returnType || '',
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
 * Transform packages response to frontend format
 */
const transformPackagesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((pkg, index) => ({
        id: `package-${index + 1}`,
        name: pkg.package_name || pkg.name || pkg.object_name,
        owner: pkg.owner || 'HR',
        type: 'PACKAGE',
        status: pkg.status || pkg.object_status || 'VALID',
        spec: pkg.specification || pkg.spec || '',
        body: pkg.body || '',
        procedures: pkg.procedures || [],
        functions: pkg.functions || [],
        variables: pkg.variables || [],
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
 * Transform sequences response to frontend format
 */
const transformSequencesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((seq, index) => ({
        id: `sequence-${index + 1}`,
        name: seq.sequence_name || seq.name,
        owner: seq.owner || seq.sequence_owner || 'HR',
        type: 'SEQUENCE',
        minValue: seq.min_value || 0,
        maxValue: seq.max_value || 0,
        incrementBy: seq.increment_by || 1,
        cycleFlag: seq.cycle_flag === 'Y' || seq.cycle_flag === true,
        orderFlag: seq.order_flag === 'Y' || seq.order_flag === true,
        cacheSize: seq.cache_size || 0,
        lastNumber: seq.last_number || 0,
        comment: seq.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform synonyms response to frontend format
 */
const transformSynonymsResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((syn, index) => ({
        id: `synonym-${index + 1}`,
        name: syn.synonym_name || syn.name,
        owner: syn.owner || 'HR',
        type: 'SYNONYM',
        tableOwner: syn.table_owner || '',
        tableName: syn.table_name || syn.target_object || '',
        dbLink: syn.db_link || '',
        public: syn.public === 'Y' || syn.public === true,
        comment: syn.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform types response to frontend format
 */
const transformTypesResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((type, index) => ({
        id: `type-${index + 1}`,
        name: type.type_name || type.name,
        owner: type.owner || 'HR',
        type: 'TYPE',
        status: type.status || type.object_status || 'VALID',
        typecode: type.typecode || '',
        attributes: type.attributes || [],
        methods: type.methods || [],
        attributeCount: type.attribute_count || (type.attributes || []).length,
        methodCount: type.method_count || (type.methods || []).length,
        created: type.created,
        lastModified: type.last_ddl_time,
        comment: type.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform triggers response to frontend format
 */
const transformTriggersResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = data.map((trigger, index) => ({
        id: `trigger-${index + 1}`,
        name: trigger.trigger_name || trigger.name,
        owner: trigger.owner || 'HR',
        type: 'TRIGGER',
        status: trigger.trigger_status || trigger.status || 'ENABLED',
        objectStatus: trigger.object_status || 'VALID',
        triggerType: trigger.trigger_type || '',
        triggeringEvent: trigger.triggering_event || '',
        tableName: trigger.table_name || '',
        tableOwner: trigger.table_owner || '',
        body: trigger.trigger_body || '',
        description: trigger.description || '',
        whenClause: trigger.when_clause || '',
        referencing: trigger.referencing_names || '',
        created: trigger.created,
        lastModified: trigger.last_ddl_time,
        comment: trigger.comments || ''
    }));

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform table details response to frontend format
 */
const transformTableDetailsResponse = (response, tableName) => {
    const data = response.data || {};
    
    // Get columns from various possible locations in the response
    let columns = [];
    if (data.columns) {
        columns = data.columns;
    } else if (data.columnInfo) {
        columns = data.columnInfo;
    } else if (data.tableInfo && data.tableInfo.columns) {
        columns = data.tableInfo.columns;
    }

    // Get constraints
    let constraints = [];
    if (data.constraints) {
        constraints = data.constraints;
    } else if (data.constraintInfo) {
        constraints = data.constraintInfo;
    }

    // Get indexes
    let indexes = [];
    if (data.indexes) {
        indexes = data.indexes;
    } else if (data.indexInfo) {
        indexes = data.indexInfo;
    }

    // Get statistics
    let statistics = {};
    if (data.statistics) {
        statistics = data.statistics;
    } else if (data.stats) {
        statistics = data.stats;
    }

    const transformedData = {
        id: `table-${Date.now()}`,
        name: data.table_name || tableName,
        owner: data.owner || 'HR',
        type: 'TABLE',
        status: data.table_status || data.status || data.object_status || 'VALID',
        rowCount: data.num_rows || statistics.num_rows || 0,
        size: formatBytes(data.bytes || statistics.bytes || 0),
        comment: data.comments || '',
        
        // Columns with proper formatting
        columns: (columns || []).map((col, idx) => ({
            name: col.column_name || col.name,
            type: col.data_type || col.type,
            nullable: col.nullable === 'Y' || col.nullable === true ? 'Y' : 'N',
            key: getColumnKey(col, constraints),
            position: col.column_id || col.position || idx + 1,
            dataLength: col.data_length || 0,
            dataPrecision: col.data_precision,
            dataScale: col.data_scale,
            defaultValue: col.data_default || col.default_value
        })),
        
        // Constraints
        constraints: (constraints || []).map(con => ({
            name: con.constraint_name || con.name,
            type: formatConstraintType(con.constraint_type || con.type),
            columns: con.columns || '',
            status: con.constraint_status || con.status || 'ENABLED',
            refTable: con.references_owner ? `${con.references_owner}.${con.references_constraint}` : '',
            deleteRule: con.delete_rule || '',
            deferrable: con.deferrable || '',
            validated: con.validated || ''
        })),
        
        // Indexes
        indexes: (indexes || []).map(idx => ({
            name: idx.index_name || idx.name,
            type: idx.index_type || '',
            uniqueness: idx.uniqueness || 'NONUNIQUE',
            columns: idx.columns || '',
            status: idx.index_status || idx.status || 'VALID',
            visibility: idx.visibility || 'VISIBLE',
            tablespace: idx.tablespace_name || ''
        })),
        
        // Statistics
        statistics: {
            numRows: statistics.num_rows || 0,
            blocks: statistics.blocks || 0,
            emptyBlocks: statistics.empty_blocks || 0,
            avgRowLen: statistics.avg_row_len || 0,
            lastAnalyzed: statistics.last_analyzed,
            sampleSize: statistics.sample_size || 0,
            globalStats: statistics.global_stats || '',
            userStats: statistics.user_stats || '',
            stattypeLocked: statistics.stattype_locked || ''
        },
        
        created: data.created,
        lastModified: data.last_ddl_time || data.last_analyzed,
        tablespace: data.tablespace_name || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform table data response to frontend format
 */
const transformTableDataResponse = (response, tableName, params = {}) => {
    const data = response.data || {};
    const { page = 1, pageSize = 50 } = params;
    
    // Get rows from response
    let rows = [];
    if (data.rows) {
        rows = data.rows;
    } else if (data.data) {
        rows = Array.isArray(data.data) ? data.data : [data.data];
    } else if (Array.isArray(data)) {
        rows = data;
    }

    // Get columns from response or derive from rows
    let columns = [];
    if (data.columns) {
        columns = data.columns;
    } else if (rows.length > 0) {
        // Derive columns from first row
        columns = Object.keys(rows[0]).map(key => ({
            name: key,
            type: 'VARCHAR2',
            nullable: 'Y'
        }));
    }

    // Get pagination info
    const totalRows = data.total_rows || data.totalCount || rows.length;
    const totalPages = Math.ceil(totalRows / pageSize);

    const transformedData = {
        rows: rows,
        columns: columns,
        page: data.page || page,
        pageSize: data.pageSize || pageSize,
        totalRows: totalRows,
        totalPages: totalPages,
        queryTime: data.queryTime || data.executionTime || 0
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform object details response to frontend format
 */
const transformObjectDetailsResponse = (response, objectType, objectName) => {
    const data = response.data || {};
    
    let transformedData = {
        id: `${objectType.toLowerCase()}-${Date.now()}`,
        name: objectName,
        owner: data.owner || 'HR',
        type: objectType,
        status: data.status || data.object_status || 'VALID'
    };

    // Add type-specific details
    switch (objectType.toUpperCase()) {
        case 'TABLE':
            transformedData = {
                ...transformedData,
                ...transformTableDetailsResponse(response, objectName).data
            };
            break;
            
        case 'VIEW':
            transformedData = {
                ...transformedData,
                text: data.text || data.viewDefinition || '',
                readOnly: data.read_only === 'Y' || data.read_only === true,
                columns: (data.columns || []).map(col => ({
                    name: col.column_name || col.name,
                    type: col.data_type || col.type,
                    nullable: col.nullable === 'Y' || col.nullable === true ? 'Y' : 'N',
                    position: col.column_id || col.position
                }))
            };
            break;
            
        case 'PROCEDURE':
        case 'FUNCTION':
            transformedData = {
                ...transformedData,
                parameters: (data.parameters || []).map(param => ({
                    name: param.argument_name || param.name,
                    type: param.data_type || param.type,
                    inOut: param.in_out || param.mode || 'IN',
                    position: param.position || 0,
                    dataType: param.data_type || param.type,
                    dataLength: param.data_length,
                    dataPrecision: param.data_precision,
                    dataScale: param.data_scale,
                    defaultValue: param.default_value || param.defaulted
                })),
                returnType: data.return_type || data.returnType
            };
            break;
            
        case 'PACKAGE':
            transformedData = {
                ...transformedData,
                spec: data.spec || data.packageSpec || '',
                body: data.body || data.packageBody || '',
                procedures: data.procedures || [],
                functions: data.functions || [],
                variables: data.variables || []
            };
            break;
            
        case 'TRIGGER':
            transformedData = {
                ...transformedData,
                triggerType: data.trigger_type || data.type,
                triggeringEvent: data.triggering_event || data.event,
                tableName: data.table_name || data.table,
                tableOwner: data.table_owner,
                body: data.trigger_body || data.body,
                description: data.description,
                whenClause: data.when_clause,
                referencing: data.referencing_names
            };
            break;
            
        case 'TYPE':
            transformedData = {
                ...transformedData,
                typecode: data.typecode,
                attributes: data.attributes || [],
                methods: data.methods || []
            };
            break;
            
        case 'SEQUENCE':
            transformedData = {
                ...transformedData,
                minValue: data.min_value || data.minValue,
                maxValue: data.max_value || data.maxValue,
                incrementBy: data.increment_by || data.incrementBy,
                cycleFlag: data.cycle_flag === 'Y' || data.cycleFlag === true,
                orderFlag: data.order_flag === 'Y' || data.orderFlag === true,
                cacheSize: data.cache_size || data.cacheSize,
                lastNumber: data.last_number || data.lastNumber
            };
            break;
            
        case 'SYNONYM':
            transformedData = {
                ...transformedData,
                tableOwner: data.table_owner,
                tableName: data.table_name,
                dbLink: data.db_link
            };
            break;
    }

    return {
                ...response,
        data: transformedData
    };
};

/**
 * Transform DDL response to frontend format
 */
const transformDDLResponse = (response) => {
    const data = response.data || {};
    
    const ddl = data.ddl || data.text || data.source || '';

    return {
        ...response,
        data: ddl
    };
};

/**
 * Transform search response to frontend format
 */
const transformSearchResponse = (response) => {
    const data = response.data || [];
    
    const transformedData = {
        results: data.map((item, index) => ({
            id: `search-${index + 1}`,
            name: item.object_name || item.name,
            owner: item.owner || 'HR',
            type: item.object_type || item.type,
            status: item.status || 'VALID',
            created: item.created,
            lastModified: item.last_ddl_time
        })),
        totalCount: data.length,
        query: data.query || ''
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform query response to frontend format
 */
const transformQueryResponse = (response) => {
    const data = response.data || {};
    
    let rows = [];
    if (data.rows) {
        rows = data.rows;
    } else if (Array.isArray(data)) {
        rows = data;
    }

    let columns = [];
    if (data.columns) {
        columns = data.columns;
    } else if (rows.length > 0) {
        columns = Object.keys(rows[0]).map(key => ({
            name: key,
            type: 'VARCHAR2'
        }));
    }

    const transformedData = {
        rows: rows,
        columns: columns,
        rowCount: rows.length,
        executionTime: data.executionTime || data.queryTime || 0,
        message: data.message || `Query executed successfully, ${rows.length} rows returned`
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform API generation response to frontend format
 */
const transformApiResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        apiName: data.apiName || data.name || 'Generated API',
        apiType: data.apiType || 'REST',
        endpoints: data.endpoints || [],
        code: data.code || data.source || '',
        documentation: data.documentation || '',
        curlExample: data.curlExample || '',
        message: data.message || 'API generated successfully'
    };

    return {
        ...response,
        data: transformedData
    };
};

/**
 * Transform export response to frontend format
 */
const transformExportResponse = (response) => {
    const data = response.data || {};
    
    const transformedData = {
        format: data.format || 'JSON',
        content: data.content || data.data || '',
        filename: data.filename || `export_${Date.now()}.${(data.format || 'json').toLowerCase()}`,
        size: data.size || 0,
        message: data.message || 'Export completed successfully'
    };

    return {
        ...response,
        data: transformedData
    };
};

// ============================================================
// EXTRACT FUNCTIONS - For use in the frontend
// ============================================================

export const extractSchemaConnections = (response) => {
    return response?.data || [];
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
    return response?.data || { rows: [], columns: [] };
};

export const extractDDL = (response) => {
    return response?.data || '';
};

export const extractSearchResults = (response) => {
    return response?.data || { results: [], totalCount: 0 };
};

export const extractQueryResults = (response) => {
    return response?.data || { rows: [], columns: [] };
};

export const extractAPIGenerationResults = (response) => {
    return response?.data || {};
};

export const extractComprehensiveSchemaData = (response) => {
    return response?.data || {
        tables: { objects: [], totalCount: 0 },
        views: { objects: [], totalCount: 0 },
        procedures: { objects: [], totalCount: 0 },
        functions: { objects: [], totalCount: 0 },
        packages: { objects: [], totalCount: 0 },
        sequences: { objects: [], totalCount: 0 },
        synonyms: { objects: [], totalCount: 0 },
        types: { objects: [], totalCount: 0 },
        triggers: { objects: [], totalCount: 0 }
    };
};

export const extractExportResults = (response) => {
    return response?.data || {};
};

export const extractHealthCheck = (response) => {
    return response?.data || { status: 'UNKNOWN' };
};

// ============================================================
// UTILITY FUNCTIONS
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
        'INDEX': '📌'
    };
    return icons[type] || '📄';
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
        'INDEX': isDark ? '#2DD4BF' : '#14B8A6'
    };
    return colors[type] || (isDark ? '#94A3B8' : '#6B7280');
};

/**
 * Check if object is supported for API generation
 */
export const isSupportedForAPIGeneration = (objectType) => {
    const supportedTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'PACKAGE'];
    return supportedTypes.includes(objectType.toUpperCase());
};

/**
 * Generate sample query for object
 */
export const generateSampleQuery = (objectName, objectType) => {
    switch (objectType.toUpperCase()) {
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
        default:
            return `-- No sample query available for ${objectType}`;
    }
};

/**
 * Refresh schema data
 */
export const refreshSchemaData = async (authorizationHeader, connectionId) => {
    clearCachedSchemaData(`schema_objects_${connectionId}`);
    return getComprehensiveSchemaData(authorizationHeader, { connectionId });
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


// Add this function to the exports section at the bottom of the file
export const formatObjectDetails = (details) => {
    if (!details) return null;
    
    // Format object details for display in the UI
    return {
        ...details,
        // Ensure columns are properly formatted
        columns: (details.columns || []).map(col => ({
            ...col,
            // Add display-friendly properties
            displayType: formatDataType(col.type, col.dataLength, col.dataPrecision, col.dataScale),
            nullable: col.nullable === 'Y' || col.nullable === true ? 'Y' : 'N'
        })),
        // Add formatted timestamps
        createdFormatted: formatDateForDisplay(details.created),
        lastModifiedFormatted: formatDateForDisplay(details.lastModified || details.last_ddl_time),
        // Add size in human-readable format
        sizeFormatted: details.size ? formatBytes(details.size) : undefined
    };
};

// Helper functions
const getColumnKey = (column, constraints) => {
    if (!constraints || !column) return '';
    
    for (const con of constraints) {
        if (con.columns && con.columns.includes(column.column_name || column.name)) {
            if (con.constraint_type === 'P') return 'PK';
            if (con.constraint_type === 'R') return 'FK';
            if (con.constraint_type === 'U') return 'UQ';
        }
    }
    return '';
};

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