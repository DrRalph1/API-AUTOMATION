// controllers/SchemaBrowserController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./UserManagementController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters
const buildQueryParams = (params = {}) => {
  const queryParams = new URLSearchParams();
  
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      queryParams.append(key, params[key]);
    }
  });
  
  return queryParams.toString();
};

// ============ SCHEMA BROWSER METHODS ============

/**
 * Get schema browser connections
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getSchemaConnections = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/schema-browser/connections`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get schema objects
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {string} params.connectionId - Connection ID (required)
 * @param {string} params.objectType - Object type (TABLE, VIEW, PROCEDURE, etc.)
 * @param {string} params.filter - Filter string for object names
 * @returns {Promise} API response
 */
export const getSchemaObjects = async (authorizationHeader, params = {}) => {
  const queryString = buildQueryParams(params);
  const url = `/schema-browser/objects${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get object details
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {string} params.connectionId - Connection ID (required)
 * @param {string} params.objectType - Object type (required)
 * @param {string} params.objectName - Object name (required)
 * @returns {Promise} API response
 */
export const getObjectDetails = async (authorizationHeader, params = {}) => {
  const queryString = buildQueryParams(params);
  const url = `/schema-browser/object-details${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get table data
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {string} params.connectionId - Connection ID (required)
 * @param {string} params.tableName - Table name (required)
 * @param {number} params.page - Page number (default: 1)
 * @param {number} params.pageSize - Page size (default: 50)
 * @param {string} params.sortColumn - Column to sort by
 * @param {string} params.sortDirection - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getTableData = async (authorizationHeader, params = {}) => {
  const queryString = buildQueryParams(params);
  const url = `/schema-browser/table-data${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get object DDL
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {string} params.connectionId - Connection ID (required)
 * @param {string} params.objectType - Object type (required)
 * @param {string} params.objectName - Object name (required)
 * @returns {Promise} API response
 */
export const getObjectDDL = async (authorizationHeader, params = {}) => {
  const queryString = buildQueryParams(params);
  const url = `/schema-browser/ddl${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Search schema
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {string} params.connectionId - Connection ID (required)
 * @param {string} params.searchQuery - Search query string (required)
 * @param {string} params.searchType - Search type (ALL, TABLE, VIEW, etc.)
 * @param {number} params.maxResults - Maximum results to return (default: 100)
 * @returns {Promise} API response
 */
export const searchSchema = async (authorizationHeader, params = {}) => {
  const queryString = buildQueryParams(params);
  const url = `/schema-browser/search${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Execute SQL query
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} queryData - Query request data
 * @param {string} queryData.connectionId - Connection ID (required)
 * @param {string} queryData.query - SQL query to execute (required)
 * @param {number} queryData.timeoutSeconds - Query timeout in seconds
 * @param {boolean} queryData.readOnly - Whether query is read-only
 * @returns {Promise} API response
 */
export const executeQuery = async (authorizationHeader, queryData) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/schema-browser/execute-query`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(queryData)
    })
  );
};

/**
 * Generate API from object
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} apiRequest - API generation request data
 * @param {string} apiRequest.connectionId - Connection ID (required)
 * @param {string} apiRequest.objectType - Object type (required)
 * @param {string} apiRequest.objectName - Object name (required)
 * @param {string} apiRequest.apiType - API type (REST, GraphQL, etc.)
 * @param {Object} apiRequest.options - Generation options
 * @returns {Promise} API response
 */
export const generateAPIFromObject = async (authorizationHeader, apiRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/schema-browser/generate-api`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(apiRequest)
    })
  );
};

/**
 * Get comprehensive schema data
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {string} params.connectionId - Connection ID (required)
 * @param {string} params.schema - Schema name
 * @returns {Promise} API response
 */
export const getComprehensiveSchemaData = async (authorizationHeader, params = {}) => {
  const queryString = buildQueryParams(params);
  const url = `/schema-browser/comprehensive${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Clear schema cache
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const clearSchemaCache = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/schema-browser/clear-cache`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get object hierarchy
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {string} params.connectionId - Connection ID (required)
 * @param {string} params.objectType - Object type (required)
 * @param {string} params.objectName - Object name (required)
 * @returns {Promise} API response
 */
export const getObjectHierarchy = async (authorizationHeader, params = {}) => {
  const queryString = buildQueryParams(params);
  const url = `/schema-browser/object-hierarchy${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Export schema data
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} exportRequest - Export request data
 * @param {string} exportRequest.connectionId - Connection ID (required)
 * @param {string} exportRequest.schema - Schema name
 * @param {string} exportRequest.format - Export format (JSON, XML, SQL, CSV)
 * @param {Array} exportRequest.objectTypes - Object types to include
 * @param {Array} exportRequest.objectNames - Specific object names to include
 * @param {Object} exportRequest.options - Export options
 * @returns {Promise} API response
 */
export const exportSchemaData = async (authorizationHeader, exportRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/schema-browser/export`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(exportRequest)
    })
  );
};

/**
 * Advanced schema search
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchRequest - Advanced search request data
 * @param {string} searchRequest.connectionId - Connection ID (required)
 * @param {string} searchRequest.searchQuery - Search query string (required)
 * @param {string} searchRequest.searchType - Search type (ALL, TABLE, VIEW, etc.)
 * @param {number} searchRequest.maxResults - Maximum results to return (default: 100)
 * @param {Object} searchRequest.filters - Advanced filters
 * @param {Array} searchRequest.objectTypes - Filter by object types
 * @param {string} searchRequest.schema - Filter by schema
 * @returns {Promise} API response
 */
export const advancedSearch = async (authorizationHeader, searchRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/schema-browser/advanced-search`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(searchRequest)
    })
  );
};

/**
 * Schema browser health check
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const healthCheck = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/schema-browser/health`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for schema browser operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleSchemaBrowserResponse = (response) => {
  if (!response) {
    throw new Error('No response received from schema browser service');
  }

  const responseCode = response.responseCode || response.status;
  
  if (responseCode === 200 || responseCode === 201) {
    return {
      ...response,
      data: response.data || {},
      responseCode: responseCode,
      requestId: response.requestId
    };
  }

  const errorMessage = response.message || response.error || 'Unknown error';
  switch (responseCode) {
    case 400: 
      throw new Error(`Bad Request: ${errorMessage}`);
    case 401: 
      throw new Error(`Unauthorized: ${errorMessage}`);
    case 403: 
      throw new Error(`Forbidden: ${errorMessage}`);
    case 404: 
      throw new Error(`Not Found: ${errorMessage}`);
    case 500: 
      throw new Error(`Server Error: ${errorMessage}`);
    default: 
      throw new Error(`Error ${responseCode || 'Unknown'}: ${errorMessage}`);
  }
};

/**
 * Extract schema connections from response
 * @param {Object} response - API response
 * @returns {Array} Schema connections
 */
export const extractSchemaConnections = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.connections && Array.isArray(data.connections)) {
    return data.connections;
  }
  
  return [];
};

/**
 * Extract schema objects from response
 * @param {Object} response - API response
 * @param {string} objectType - Type of objects to extract
 * @returns {Array} Schema objects
 */
export const extractSchemaObjects = (response, objectType = null) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (data.objects && Array.isArray(data.objects)) {
    if (objectType) {
      return data.objects.filter(obj => 
        !objectType || obj.objectType === objectType.toUpperCase()
      );
    }
    return data.objects;
  }
  
  if (Array.isArray(data)) {
    return data;
  }
  
  return [];
};

/**
 * Extract object details from response
 * @param {Object} response - API response
 * @returns {Object} Object details
 */
export const extractObjectDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  // Standardize the response format
  return {
    name: details.objectName || details.name,
    type: details.objectType || details.type,
    columns: details.columns || [],
    constraints: details.constraints || [],
    indexes: details.indexes || [],
    foreignKeys: details.foreignKeys || [],
    ddl: details.ddl || details.definition,
    rowCount: details.rowCount || details.count,
    created: details.createdDate || details.created,
    lastModified: details.lastModifiedDate || details.modified,
    size: details.size || details.storage,
    description: details.description || details.comments,
    schema: details.schema || details.owner,
    connectionId: details.connectionId,
    metadata: details.metadata || {}
  };
};

/**
 * Extract table data from response
 * @param {Object} response - API response
 * @returns {Object} Table data with pagination
 */
export const extractTableData = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    rows: data.rows || data.data || [],
    columns: data.columns || [],
    totalRows: data.totalRows || data.totalCount || 0,
    page: data.page || data.currentPage || 1,
    pageSize: data.pageSize || data.pageSize || 50,
    totalPages: data.totalPages || Math.ceil((data.totalRows || 0) / (data.pageSize || 50)),
    sortColumn: data.sortColumn,
    sortDirection: data.sortDirection,
    queryTime: data.queryTime || data.executionTime,
    rowLimitExceeded: data.rowLimitExceeded || false,
    metadata: data.metadata || {}
  };
};

/**
 * Extract DDL from response
 * @param {Object} response - API response
 * @returns {string} DDL string
 */
export const extractDDL = (response) => {
  if (!response || !response.data) return '';
  
  const data = response.data;
  return data.ddl || data.definition || data.sql || '';
};

/**
 * Extract search results from response
 * @param {Object} response - API response
 * @returns {Object} Search results
 */
export const extractSearchResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    results: data.results || data.matches || [],
    totalCount: data.totalCount || data.totalMatches || 0,
    searchQuery: data.searchQuery,
    searchType: data.searchType,
    connectionId: data.connectionId,
    executionTime: data.executionTime || data.queryTime,
    highlight: data.highlight || {},
    facets: data.facets || {},
    metadata: data.metadata || {}
  };
};

/**
 * Extract query execution results
 * @param {Object} response - API response
 * @returns {Object} Query execution results
 */
export const extractQueryResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    rows: data.rows || data.results || [],
    columns: data.columns || [],
    rowCount: data.rowCount || data.affectedRows || 0,
    queryTime: data.queryTime || data.executionTime,
    success: data.success !== undefined ? data.success : true,
    message: data.message || data.status,
    warnings: data.warnings || [],
    metadata: data.metadata || {},
    isReadOnly: data.readOnly || false,
    limitReached: data.limitReached || false
  };
};

/**
 * Extract API generation results
 * @param {Object} response - API response
 * @returns {Object} API generation results
 */
export const extractAPIGenerationResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    apiId: data.apiId || data.generatedId,
    objectName: data.objectName,
    objectType: data.objectType,
    apiType: data.apiType,
    endpoints: data.endpoints || [],
    models: data.models || [],
    controllers: data.controllers || [],
    services: data.services || [],
    repository: data.repository,
    totalFiles: data.totalFiles,
    zipUrl: data.zipUrl || data.downloadUrl,
    generationTime: data.generationTime,
    status: data.status || 'SUCCESS',
    warnings: data.warnings || [],
    metadata: data.metadata || {}
  };
};

/**
 * Extract comprehensive schema data
 * @param {Object} response - API response
 * @returns {Object} Comprehensive schema data
 */
export const extractComprehensiveSchemaData = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    connections: data.connections || [],
    tables: data.tables || { objects: [], totalCount: 0 },
    views: data.views || { objects: [], totalCount: 0 },
    procedures: data.procedures || { objects: [], totalCount: 0 },
    functions: data.functions || { objects: [], totalCount: 0 },
    packages: data.packages || { objects: [], totalCount: 0 },
    totalObjects: data.totalObjects || 0,
    lastUpdated: data.lastUpdated || new Date().toISOString(),
    generatedFor: data.generatedFor || 'Unknown',
    connectionId: data.connectionId,
    schema: data.schema || 'ALL',
    statistics: data.statistics || {},
    health: data.health || {}
  };
};

/**
 * Extract object hierarchy
 * @param {Object} response - API response
 * @returns {Object} Object hierarchy
 */
export const extractObjectHierarchy = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    objectName: data.objectName,
    objectType: data.objectType,
    dependencies: data.dependencies || {},
    dependents: data.dependents || {},
    hierarchyLevel: data.hierarchyLevel || 1,
    lastUpdated: data.lastUpdated,
    depth: data.depth || 0,
    children: data.children || [],
    parents: data.parents || [],
    references: data.references || [],
    metadata: data.metadata || {}
  };
};

/**
 * Extract export results
 * @param {Object} response - API response
 * @returns {Object} Export results
 */
export const extractExportResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    exportId: data.exportId,
    format: data.format,
    fileName: data.fileName,
    fileSize: data.fileSize,
    downloadUrl: data.downloadUrl,
    generatedAt: data.generatedAt,
    generatedBy: data.generatedBy,
    objectCount: data.objectCount,
    status: data.status,
    progress: data.progress || 100,
    estimatedTimeRemaining: data.estimatedTimeRemaining,
    metadata: data.metadata || {}
  };
};

/**
 * Extract health check results
 * @param {Object} response - API response
 * @returns {Object} Health check results
 */
export const extractHealthCheck = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    status: data.status || 'UNKNOWN',
    timestamp: data.timestamp,
    service: data.service || 'SchemaBrowserService',
    cacheItems: data.cacheItems || 0,
    uptime: data.uptime || 'Unknown',
    version: data.version || '1.0.0',
    environment: data.environment || 'Development',
    databaseConnections: data.databaseConnections || 0,
    activeQueries: data.activeQueries || 0,
    memoryUsage: data.memoryUsage || 'Normal',
    cpuUsage: data.cpuUsage || 'Normal',
    lastBackup: data.lastBackup,
    metrics: data.metrics || {}
  };
};

// ============ VALIDATION & UTILITY FUNCTIONS ============

/**
 * Validate schema search criteria
 * @param {Object} searchCriteria - Search criteria to validate
 * @returns {Array} Array of validation errors
 */
export const validateSchemaSearchCriteria = (searchCriteria) => {
  const errors = [];
  
  if (!searchCriteria.connectionId) {
    errors.push('Connection ID is required');
  }
  
  if (!searchCriteria.searchQuery || searchCriteria.searchQuery.trim().length === 0) {
    errors.push('Search query is required');
  }
  
  if (searchCriteria.maxResults !== undefined && (searchCriteria.maxResults < 1 || searchCriteria.maxResults > 1000)) {
    errors.push('Maximum results must be between 1 and 1000');
  }
  
  return errors;
};

/**
 * Validate execute query request
 * @param {Object} queryRequest - Query request to validate
 * @returns {Array} Array of validation errors
 */
export const validateExecuteQueryRequest = (queryRequest) => {
  const errors = [];
  
  if (!queryRequest.connectionId) {
    errors.push('Connection ID is required');
  }
  
  if (!queryRequest.query || queryRequest.query.trim().length === 0) {
    errors.push('Query is required');
  }
  
  if (queryRequest.query && queryRequest.query.trim().toUpperCase().includes('DROP TABLE')) {
    errors.push('DROP TABLE queries are not allowed');
  }
  
  if (queryRequest.query && queryRequest.query.trim().toUpperCase().includes('DELETE FROM') && 
      !queryRequest.query.toUpperCase().includes('WHERE')) {
    errors.push('DELETE queries must include a WHERE clause');
  }
  
  if (queryRequest.timeoutSeconds !== undefined && (queryRequest.timeoutSeconds < 1 || queryRequest.timeoutSeconds > 300)) {
    errors.push('Timeout must be between 1 and 300 seconds');
  }
  
  return errors;
};

/**
 * Validate API generation request
 * @param {Object} apiRequest - API generation request to validate
 * @returns {Array} Array of validation errors
 */
export const validateAPIGenerationRequest = (apiRequest) => {
  const errors = [];
  
  if (!apiRequest.connectionId) {
    errors.push('Connection ID is required');
  }
  
  if (!apiRequest.objectType) {
    errors.push('Object type is required');
  }
  
  if (!apiRequest.objectName) {
    errors.push('Object name is required');
  }
  
  if (!apiRequest.apiType) {
    errors.push('API type is required');
  }
  
  const validApiTypes = ['REST', 'GRAPHQL', 'SOAP', 'GRPC', 'ALL'];
  if (apiRequest.apiType && !validApiTypes.includes(apiRequest.apiType.toUpperCase())) {
    errors.push(`API type must be one of: ${validApiTypes.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate export request
 * @param {Object} exportRequest - Export request to validate
 * @returns {Array} Array of validation errors
 */
export const validateExportRequest = (exportRequest) => {
  const errors = [];
  
  if (!exportRequest.connectionId) {
    errors.push('Connection ID is required');
  }
  
  if (!exportRequest.format) {
    errors.push('Export format is required');
  }
  
  const validFormats = ['JSON', 'XML', 'SQL', 'CSV', 'EXCEL', 'PDF'];
  if (exportRequest.format && !validFormats.includes(exportRequest.format.toUpperCase())) {
    errors.push(`Export format must be one of: ${validFormats.join(', ')}`);
  }
  
  if (exportRequest.objectTypes && !Array.isArray(exportRequest.objectTypes)) {
    errors.push('Object types must be an array');
  }
  
  return errors;
};

/**
 * Build schema objects request parameters
 * @param {string} connectionId - Connection ID
 * @param {string} objectType - Object type
 * @param {string} filter - Filter string
 * @returns {Object} Request parameters
 */
export const buildSchemaObjectsParams = (connectionId, objectType = null, filter = null) => ({
  connectionId,
  ...(objectType && { objectType }),
  ...(filter && { filter })
});

/**
 * Build object details parameters
 * @param {string} connectionId - Connection ID
 * @param {string} objectType - Object type
 * @param {string} objectName - Object name
 * @returns {Object} Request parameters
 */
export const buildObjectDetailsParams = (connectionId, objectType, objectName) => ({
  connectionId,
  objectType,
  objectName
});

/**
 * Build table data parameters
 * @param {string} connectionId - Connection ID
 * @param {string} tableName - Table name
 * @param {number} page - Page number
 * @param {number} pageSize - Page size
 * @param {string} sortColumn - Sort column
 * @param {string} sortDirection - Sort direction
 * @returns {Object} Request parameters
 */
export const buildTableDataParams = (
  connectionId, 
  tableName, 
  page = 1, 
  pageSize = 50, 
  sortColumn = null, 
  sortDirection = null
) => ({
  connectionId,
  tableName,
  page,
  pageSize,
  ...(sortColumn && { sortColumn }),
  ...(sortDirection && { sortDirection })
});

/**
 * Build search parameters
 * @param {string} connectionId - Connection ID
 * @param {string} searchQuery - Search query
 * @param {string} searchType - Search type
 * @param {number} maxResults - Maximum results
 * @returns {Object} Request parameters
 */
export const buildSearchParams = (connectionId, searchQuery, searchType = null, maxResults = 100) => ({
  connectionId,
  searchQuery,
  ...(searchType && { searchType }),
  maxResults
});

/**
 * Format object details for display
 * @param {Object} objectDetails - Object details
 * @returns {Object} Formatted object details
 */
export const formatObjectDetails = (objectDetails) => {
  if (!objectDetails) return {};
  
  const formatted = { ...objectDetails };
  
  // Format dates
  if (formatted.created) {
    formatted.formattedCreated = formatDateForDisplay(formatted.created);
  }
  
  if (formatted.lastModified) {
    formatted.formattedLastModified = formatDateForDisplay(formatted.lastModified);
  }
  
  // Format size
  if (formatted.size) {
    formatted.formattedSize = formatBytes(formatted.size);
  }
  
  // Format row count
  if (typeof formatted.rowCount === 'number') {
    formatted.formattedRowCount = formatted.rowCount.toLocaleString();
  }
  
  // Format columns
  if (Array.isArray(formatted.columns)) {
    formatted.formattedColumns = formatted.columns.map(col => ({
      ...col,
      formattedType: formatDataType(col.dataType)
    }));
  }
  
  return formatted;
};

/**
 * Format table data for display
 * @param {Object} tableData - Table data
 * @returns {Object} Formatted table data
 */
export const formatTableData = (tableData) => {
  if (!tableData) return {};
  
  const formatted = { ...tableData };
  
  // Format pagination info
  if (formatted.totalRows) {
    formatted.formattedTotalRows = formatted.totalRows.toLocaleString();
  }
  
  if (formatted.page && formatted.pageSize) {
    const startRow = (formatted.page - 1) * formatted.pageSize + 1;
    const endRow = Math.min(formatted.page * formatted.pageSize, formatted.totalRows || 0);
    formatted.range = `${startRow.toLocaleString()} - ${endRow.toLocaleString()}`;
  }
  
  // Format query time
  if (formatted.queryTime) {
    formatted.formattedQueryTime = `${formatted.queryTime}ms`;
  }
  
  return formatted;
};

/**
 * Format bytes to human-readable format
 * @param {number} bytes - Bytes
 * @param {number} decimals - Decimal places
 * @returns {string} Formatted size
 */
export const formatBytes = (bytes, decimals = 2) => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
  
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

/**
 * Format date for display (import from AuditController or define here)
 * @param {string|Date} date - Date to format
 * @returns {string} Formatted date string
 */
export const formatDateForDisplay = (date) => {
  if (!date) return '';
  
  const d = new Date(date);
  return d.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

/**
 * Format data type for display
 * @param {string} dataType - Data type
 * @returns {string} Formatted data type
 */
export const formatDataType = (dataType) => {
  if (!dataType) return '';
  
  const type = dataType.toUpperCase();
  
  // Common data type mappings
  const typeMap = {
    'VARCHAR2': 'String',
    'VARCHAR': 'String',
    'CHAR': 'String',
    'NVARCHAR2': 'String',
    'NUMBER': 'Number',
    'INT': 'Integer',
    'INTEGER': 'Integer',
    'FLOAT': 'Float',
    'DOUBLE': 'Double',
    'DATE': 'Date',
    'TIMESTAMP': 'Timestamp',
    'CLOB': 'Text',
    'BLOB': 'Binary',
    'BOOLEAN': 'Boolean',
    'JSON': 'JSON'
  };
  
  return typeMap[type] || dataType;
};

/**
 * Get object type icon
 * @param {string} objectType - Object type
 * @returns {string} Icon class or identifier
 */
export const getObjectTypeIcon = (objectType) => {
  if (!objectType) return 'help';
  
  const type = objectType.toUpperCase();
  
  const iconMap = {
    'TABLE': 'table_chart',
    'VIEW': 'visibility',
    'PROCEDURE': 'code',
    'FUNCTION': 'functions',
    'PACKAGE': 'inventory_2',
    'INDEX': 'trending_up',
    'TRIGGER': 'bolt',
    'SEQUENCE': 'linear_scale',
    'SYNONYM': 'link',
    'MATERIALIZED_VIEW': 'view_quilt',
    'TYPE': 'category',
    'DATABASE': 'storage',
    'SCHEMA': 'folder',
    'COLUMN': 'view_column'
  };
  
  return iconMap[type] || 'help';
};

/**
 * Get object type color
 * @param {string} objectType - Object type
 * @returns {string} Color code or class
 */
export const getObjectTypeColor = (objectType) => {
  if (!objectType) return '#757575';
  
  const type = objectType.toUpperCase();
  
  const colorMap = {
    'TABLE': '#1976d2',
    'VIEW': '#388e3c',
    'PROCEDURE': '#f57c00',
    'FUNCTION': '#7b1fa2',
    'PACKAGE': '#00897b',
    'INDEX': '#d81b60',
    'TRIGGER': '#5d4037',
    'SEQUENCE': '#00acc1',
    'SYNONYM': '#455a64'
  };
  
  return colorMap[type] || '#757575';
};

/**
 * Check if object type is supported for API generation
 * @param {string} objectType - Object type
 * @returns {boolean} True if supported
 */
export const isSupportedForAPIGeneration = (objectType) => {
  if (!objectType) return false;
  
  const type = objectType.toUpperCase();
  const supportedTypes = ['TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION'];
  
  return supportedTypes.includes(type);
};

/**
 * Generate sample SQL query based on object type
 * @param {string} objectType - Object type
 * @param {string} objectName - Object name
 * @returns {string} Sample SQL query
 */
export const generateSampleQuery = (objectType, objectName) => {
  if (!objectType || !objectName) return '';
  
  const type = objectType.toUpperCase();
  
  switch (type) {
    case 'TABLE':
      return `SELECT * FROM ${objectName} WHERE rownum <= 100`;
    case 'VIEW':
      return `SELECT * FROM ${objectName} WHERE rownum <= 100`;
    case 'PROCEDURE':
      return `BEGIN\n  ${objectName};\nEND;`;
    case 'FUNCTION':
      return `SELECT ${objectName}() FROM dual`;
    default:
      return `SELECT * FROM ${objectName} WHERE rownum <= 100`;
  }
};

/**
 * Monitor schema browser health
 * @param {string} authorizationHeader - Bearer token
 * @param {number} intervalSeconds - Monitoring interval in seconds
 * @param {Function} callback - Callback function for health updates
 * @returns {Function} Function to stop monitoring
 */
export const monitorSchemaHealth = (authorizationHeader, intervalSeconds = 30, callback) => {
  let isMonitoring = true;
  
  const monitor = async () => {
    while (isMonitoring) {
      try {
        const healthResponse = await healthCheck(authorizationHeader);
        const healthData = extractHealthCheck(healthResponse);
        
        if (callback) {
          callback(healthData);
        }
      } catch (error) {
        console.error('Schema health monitoring error:', error);
        
        if (callback) {
          callback({
            status: 'DOWN',
            timestamp: new Date().toISOString(),
            error: error.message
          });
        }
      }
      
      // Wait for the specified interval
      await new Promise(resolve => 
        setTimeout(resolve, intervalSeconds * 1000)
      );
    }
  };
  
  // Start monitoring
  monitor();
  
  // Return stop function
  return () => {
    isMonitoring = false;
  };
};

/**
 * Refresh all schema data for a connection
 * @param {string} authorizationHeader - Bearer token
 * @param {string} connectionId - Connection ID
 * @returns {Promise} Combined schema data
 */
export const refreshSchemaData = async (authorizationHeader, connectionId) => {
  try {
    const comprehensiveResponse = await getComprehensiveSchemaData(authorizationHeader, { connectionId });
    const comprehensiveData = handleSchemaBrowserResponse(comprehensiveResponse);
    
    if (comprehensiveData && comprehensiveData.data) {
      return comprehensiveData;
    }
    
    // If comprehensive fails, fetch individual components
    const [
      connectionsResponse,
      tablesResponse,
      viewsResponse,
      proceduresResponse
    ] = await Promise.all([
      getSchemaConnections(authorizationHeader),
      getSchemaObjects(authorizationHeader, { connectionId, objectType: 'TABLE' }),
      getSchemaObjects(authorizationHeader, { connectionId, objectType: 'VIEW' }),
      getSchemaObjects(authorizationHeader, { connectionId, objectType: 'PROCEDURE' })
    ]);
    
    const connections = handleSchemaBrowserResponse(connectionsResponse);
    const tables = handleSchemaBrowserResponse(tablesResponse);
    const views = handleSchemaBrowserResponse(viewsResponse);
    const procedures = handleSchemaBrowserResponse(proceduresResponse);
    
    return {
      responseCode: 200,
      message: 'Schema data refreshed successfully',
      data: {
        connections: connections.data,
        tables: tables.data,
        views: views.data,
        procedures: procedures.data,
        lastUpdated: new Date().toISOString(),
        connectionId
      },
      requestId: `refresh-${Date.now()}`
    };
    
  } catch (error) {
    throw new Error(`Failed to refresh schema data: ${error.message}`);
  }
};

/**
 * Download exported schema data
 * @param {string} exportId - Export ID
 * @param {string} fileName - File name
 */
export const downloadExportedSchema = (exportId, fileName = 'schema_export') => {
  const downloadUrl = `${API_CONFIG.baseUrl}/schema-browser/download/${exportId}`;
  
  // Create a temporary anchor element
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = fileName;
  link.target = '_blank';
  
  // Trigger the download
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * Cache schema data in localStorage
 * @param {string} connectionId - Connection ID
 * @param {Object} schemaData - Schema data to cache
 * @param {number} ttlMinutes - Time to live in minutes (default: 30)
 */
export const cacheSchemaData = (connectionId, schemaData, ttlMinutes = 30) => {
  if (!connectionId || !schemaData) return;
  
  const cacheKey = `schema_cache_${connectionId}`;
  const cacheData = {
    data: schemaData,
    timestamp: Date.now(),
    ttl: ttlMinutes * 60 * 1000 // Convert to milliseconds
  };
  
  try {
    localStorage.setItem(cacheKey, JSON.stringify(cacheData));
  } catch (error) {
    console.error('Failed to cache schema data:', error);
  }
};

/**
 * Get cached schema data
 * @param {string} connectionId - Connection ID
 * @returns {Object|null} Cached schema data or null
 */
export const getCachedSchemaData = (connectionId) => {
  if (!connectionId) return null;
  
  const cacheKey = `schema_cache_${connectionId}`;
  
  try {
    const cached = localStorage.getItem(cacheKey);
    
    if (!cached) return null;
    
    const cacheData = JSON.parse(cached);
    
    // Check if cache is expired
    const now = Date.now();
    const isExpired = now - cacheData.timestamp > cacheData.ttl;
    
    if (isExpired) {
      localStorage.removeItem(cacheKey);
      return null;
    }
    
    return cacheData.data;
  } catch (error) {
    console.error('Failed to get cached schema data:', error);
    return null;
  }
};

/**
 * Clear cached schema data
 * @param {string} connectionId - Connection ID (optional, clears all if not provided)
 */
export const clearCachedSchemaData = (connectionId = null) => {
  try {
    if (connectionId) {
      localStorage.removeItem(`schema_cache_${connectionId}`);
    } else {
      // Clear all schema cache
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('schema_cache_')) {
          localStorage.removeItem(key);
        }
      });
    }
  } catch (error) {
    console.error('Failed to clear cached schema data:', error);
  }
};

// Export all functions
export default {
  // Main API methods
  getSchemaConnections,
  getSchemaObjects,
  getObjectDetails,
  getTableData,
  getObjectDDL,
  searchSchema,
  executeQuery,
  generateAPIFromObject,
  getComprehensiveSchemaData,
  clearSchemaCache,
  getObjectHierarchy,
  exportSchemaData,
  advancedSearch,
  healthCheck,
  
  // Response handlers
  handleSchemaBrowserResponse,
  extractSchemaConnections,
  extractSchemaObjects,
  extractObjectDetails,
  extractTableData,
  extractDDL,
  extractSearchResults,
  extractQueryResults,
  extractAPIGenerationResults,
  extractComprehensiveSchemaData,
  extractObjectHierarchy,
  extractExportResults,
  extractHealthCheck,
  
  // Validation functions
  validateSchemaSearchCriteria,
  validateExecuteQueryRequest,
  validateAPIGenerationRequest,
  validateExportRequest,
  
  // Utility functions
  buildSchemaObjectsParams,
  buildObjectDetailsParams,
  buildTableDataParams,
  buildSearchParams,
  formatObjectDetails,
  formatTableData,
  formatBytes,
  formatDateForDisplay,
  formatDataType,
  getObjectTypeIcon,
  getObjectTypeColor,
  isSupportedForAPIGeneration,
  generateSampleQuery,
  
  // Advanced functions
  monitorSchemaHealth,
  refreshSchemaData,
  downloadExportedSchema,
  cacheSchemaData,
  getCachedSchemaData,
  clearCachedSchemaData
};