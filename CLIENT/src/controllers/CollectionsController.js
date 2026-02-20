// controllers/CollectionsController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// ============ COLLECTIONS METHODS ============

/**
 * Get collections list
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getCollectionsList = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get collection details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @returns {Promise} API response
 */
export const getCollectionDetails = async (authorizationHeader, collectionId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/${collectionId}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get request details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} requestId - Request ID
 * @returns {Promise} API response
 */
export const getRequestDetails = async (authorizationHeader, collectionId, requestId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/${collectionId}/requests/${requestId}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Execute API request
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} executeRequest - Execute request data
 * @param {string} executeRequest.method - HTTP method (required)
 * @param {string} executeRequest.url - Request URL (required)
 * @param {Object} executeRequest.headers - Request headers
 * @param {Object} executeRequest.body - Request body
 * @param {Object} executeRequest.queryParams - Query parameters
 * @param {string} executeRequest.authType - Auth type (noauth, bearer, basic, etc.)
 * @param {Object} executeRequest.authConfig - Auth configuration
 * @returns {Promise} API response
 */
export const executeRequest = async (authorizationHeader, executeRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/execute`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(executeRequest)
    })
  );
};

/**
 * Save API request
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} saveRequestData - Save request data
 * @param {string} saveRequestData.collectionId - Collection ID (required)
 * @param {string} saveRequestData.requestId - Request ID (optional, for updates)
 * @param {string} saveRequestData.name - Request name (required)
 * @param {string} saveRequestData.method - HTTP method (required)
 * @param {string} saveRequestData.url - Request URL (required)
 * @param {Array} saveRequestData.headers - Request headers array
 * @param {Object} saveRequestData.body - Request body
 * @param {Array} saveRequestData.params - Query parameters array
 * @param {Object} saveRequestData.auth - Auth configuration
 * @param {string} saveRequestData.tests - Test scripts
 * @param {string} saveRequestData.preRequestScript - Pre-request scripts
 * @param {string} saveRequestData.folderId - Folder ID
 * @returns {Promise} API response
 */
export const saveRequest = async (authorizationHeader, saveRequestData) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/save`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(saveRequestData)
    })
  );
};

/**
 * Create new collection
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} collectionData - Collection creation data
 * @param {string} collectionData.name - Collection name (required)
 * @param {string} collectionData.description - Collection description
 * @param {Array} collectionData.variables - Collection variables
 * @param {Array} collectionData.tags - Collection tags
 * @returns {Promise} API response
 */
export const createCollection = async (authorizationHeader, collectionData) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/create`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(collectionData)
    })
  );
};

/**
 * Generate code snippet
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} snippetRequest - Code snippet request data
 * @param {string} snippetRequest.language - Programming language (required)
 * @param {string} snippetRequest.method - HTTP method (required)
 * @param {string} snippetRequest.url - Request URL (required)
 * @param {Array} snippetRequest.headers - Request headers array
 * @param {string} snippetRequest.body - Request body
 * @returns {Promise} API response
 */
export const generateCodeSnippet = async (authorizationHeader, snippetRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/code-snippet`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(snippetRequest)
    })
  );
};

/**
 * Get environments list
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getEnvironments = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/environments`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Import collection
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} importRequest - Import request data
 * @param {string} importRequest.source - Import source
 * @param {string} importRequest.format - Import format
 * @param {string|Object} importRequest.data - Import data
 * @returns {Promise} API response
 */
export const importCollection = async (authorizationHeader, importRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/import`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(importRequest)
    })
  );
};

/**
 * Delete collection
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @returns {Promise} API response
 */
export const deleteCollection = async (authorizationHeader, collectionId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/${collectionId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Delete API request
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} requestId - Request ID
 * @returns {Promise} API response
 */
export const deleteRequest = async (authorizationHeader, collectionId, requestId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/${collectionId}/requests/${requestId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Update collection
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {Object} collectionData - Updated collection data
 * @param {string} collectionData.name - Collection name
 * @param {string} collectionData.description - Collection description
 * @param {Array} collectionData.variables - Collection variables
 * @param {Array} collectionData.tags - Collection tags
 * @param {boolean} collectionData.favorite - Is favorite
 * @returns {Promise} API response
 */
export const updateCollection = async (authorizationHeader, collectionId, collectionData) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/${collectionId}`, {
      method: 'PUT',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(collectionData)
    })
  );
};

/**
 * Export collection
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} format - Export format (json, yaml, postman, openapi)
 * @returns {Promise} API response
 */
export const exportCollection = async (authorizationHeader, collectionId, format = 'json') => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/${collectionId}/export?format=${format}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Search collections
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchParams - Search parameters
 * @param {string} searchParams.query - Search query
 * @param {string} searchParams.filter - Filter by visibility
 * @param {string} searchParams.sort - Sort field
 * @param {string} searchParams.order - Sort order (ASC/DESC)
 * @returns {Promise} API response
 */
export const searchCollections = async (authorizationHeader, searchParams = {}) => {
  const queryString = new URLSearchParams(searchParams).toString();
  const url = `/collections/search${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Duplicate collection
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {Object} duplicateOptions - Duplicate options
 * @returns {Promise} API response
 */
export const duplicateCollection = async (authorizationHeader, collectionId, duplicateOptions = {}) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/collections/${collectionId}/duplicate`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(duplicateOptions)
    })
  );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for collections operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleCollectionsResponse = (response) => {
  if (!response) {
    throw new Error('No response received from collections service');
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
 * Extract collections list from response
 * @param {Object} response - API response
 * @returns {Array} Collections list
 */
export const extractCollectionsList = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.collections && Array.isArray(data.collections)) {
    return data.collections;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract collection details from response
 * @param {Object} response - API response
 * @returns {Object} Collection details
 */
export const extractCollectionDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    id: details.id || details.collectionId,
    name: details.name || details.collectionName,
    description: details.description,
    createdAt: details.createdAt || details.createdDate,
    updatedAt: details.updatedAt || details.modifiedDate,
    createdBy: details.createdBy,
    updatedBy: details.updatedBy,
    owner: details.owner,
    folders: details.folders || [],
    tags: details.tags || [],
    variables: details.variables || [],
    favorite: details.favorite || false,
    totalRequests: details.totalRequests || 0,
    totalFolders: details.totalFolders || 0,
    comments: details.comments,
    lastActivity: details.lastActivity,
    color: details.color,
    metadata: details.metadata || {}
  };
};

/**
 * Extract request details from response
 * @param {Object} response - API response
 * @returns {Object} Request details
 */
export const extractRequestDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    id: details.id || details.requestId,
    name: details.name || details.requestName,
    method: details.method,
    url: details.url,
    description: details.description,
    headers: details.headers || [],
    parameters: details.parameters || [],
    body: details.body || {},
    authType: details.authType,
    authConfig: details.authConfig || {},
    preRequestScript: details.preRequestScript || '',
    tests: details.tests || '',
    saved: details.saved || false,
    createdAt: details.createdAt,
    updatedAt: details.updatedAt,
    collectionId: details.collectionId,
    folderId: details.folderId,
    tags: details.tags || [],
    metadata: details.metadata || {}
  };
};

/**
 * Extract execute request results
 * @param {Object} response - API response
 * @returns {Object} Execute request results
 */
export const extractExecuteResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    responseBody: data.responseBody || data.body,
    statusCode: data.statusCode,
    statusText: data.statusText,
    headers: data.headers || [],
    responseTime: data.responseTime,
    responseSize: data.responseSize,
    success: data.statusCode >= 200 && data.statusCode < 300,
    errorMessage: data.errorMessage,
    metadata: data.metadata || {}
  };
};

/**
 * Extract save request results
 * @param {Object} response - API response
 * @returns {Object} Save request results
 */
export const extractSaveRequestResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    requestId: data.requestId || data.id,
    collectionId: data.collectionId,
    message: data.message,
    success: data.requestId !== undefined,
    metadata: data.metadata || {}
  };
};

/**
 * Extract create collection results
 * @param {Object} response - API response
 * @returns {Object} Create collection results
 */
export const extractCreateCollectionResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    collectionId: data.collectionId || data.id,
    name: data.name || data.collectionName,
    description: data.description,
    message: data.message,
    success: data.collectionId !== undefined,
    metadata: data.metadata || {}
  };
};

/**
 * Extract code snippet results
 * @param {Object} response - API response
 * @returns {Object} Code snippet results
 */
export const extractCodeSnippetResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    code: data.code || data.snippet,
    language: data.language,
    message: data.message,
    success: data.code !== undefined,
    metadata: data.metadata || {}
  };
};

/**
 * Extract environments list
 * @param {Object} response - API response
 * @returns {Array} Environments list
 */
export const extractEnvironments = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.environments && Array.isArray(data.environments)) {
    return data.environments;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract import results
 * @param {Object} response - API response
 * @returns {Object} Import results
 */
export const extractImportResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    collectionId: data.collectionId,
    name: data.name || data.collectionName,
    message: data.message,
    success: data.collectionId !== undefined,
    metadata: data.metadata || {}
  };
};

// ============ VALIDATION & UTILITY FUNCTIONS ============

/**
 * Validate save request data
 * @param {Object} requestData - Save request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateSaveRequest = (requestData) => {
  const errors = [];
  
  if (!requestData.collectionId) {
    errors.push('Collection ID is required');
  }
  
  if (!requestData.name || requestData.name.trim().length === 0) {
    errors.push('Request name is required');
  }
  
  if (!requestData.method) {
    errors.push('HTTP method is required');
  }
  
  if (!requestData.url || requestData.url.trim().length === 0) {
    errors.push('URL is required');
  }
  
  const validMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'];
  if (requestData.method && !validMethods.includes(requestData.method.toUpperCase())) {
    errors.push(`HTTP method must be one of: ${validMethods.join(', ')}`);
  }
  
  if (requestData.url && !isValidUrl(requestData.url)) {
    errors.push('Invalid URL format');
  }
  
  return errors;
};

/**
 * Validate execute request data
 * @param {Object} requestData - Execute request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateExecuteRequest = (requestData) => {
  const errors = [];
  
  if (!requestData.method) {
    errors.push('HTTP method is required');
  }
  
  if (!requestData.url || requestData.url.trim().length === 0) {
    errors.push('URL is required');
  }
  
  const validMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'];
  if (requestData.method && !validMethods.includes(requestData.method.toUpperCase())) {
    errors.push(`HTTP method must be one of: ${validMethods.join(', ')}`);
  }
  
  if (requestData.url && !isValidUrl(requestData.url)) {
    errors.push('Invalid URL format');
  }
  
  return errors;
};

/**
 * Validate create collection data
 * @param {Object} collectionData - Collection data to validate
 * @returns {Array} Array of validation errors
 */
export const validateCreateCollection = (collectionData) => {
  const errors = [];
  
  if (!collectionData.name || collectionData.name.trim().length === 0) {
    errors.push('Collection name is required');
  }
  
  if (collectionData.name && collectionData.name.length > 255) {
    errors.push('Collection name must be 255 characters or less');
  }
  
  return errors;
};

/**
 * Validate code snippet request
 * @param {Object} snippetRequest - Code snippet request to validate
 * @returns {Array} Array of validation errors
 */
export const validateCodeSnippetRequest = (snippetRequest) => {
  const errors = [];
  
  if (!snippetRequest.language) {
    errors.push('Programming language is required');
  }
  
  if (!snippetRequest.method) {
    errors.push('HTTP method is required');
  }
  
  if (!snippetRequest.url) {
    errors.push('URL is required');
  }
  
  const validLanguages = ['javascript', 'python', 'java', 'curl', 'php', 'ruby', 'nodejs', 'csharp', 'go', 'swift'];
  if (snippetRequest.language && !validLanguages.includes(snippetRequest.language.toLowerCase())) {
    errors.push(`Language must be one of: ${validLanguages.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate import request
 * @param {Object} importRequest - Import request to validate
 * @returns {Array} Array of validation errors
 */
export const validateImportRequest = (importRequest) => {
  const errors = [];
  
  if (!importRequest.source) {
    errors.push('Import source is required');
  }
  
  if (!importRequest.data) {
    errors.push('Import data is required');
  }
  
  return errors;
};

/**
 * Check if URL is valid
 * @param {string} url - URL to validate
 * @returns {boolean} True if valid
 */
export const isValidUrl = (url) => {
  try {
    new URL(url);
    return true;
  } catch (error) {
    return false;
  }
};

/**
 * Build HTTP headers object
 * @param {Array} headersArray - Array of header objects with key and value
 * @returns {Object} Headers object
 */
export const buildHeadersObject = (headersArray) => {
  if (!Array.isArray(headersArray)) return {};
  
  return headersArray.reduce((acc, header) => {
    if (header.key && header.value !== undefined) {
      acc[header.key] = header.value;
    }
    return acc;
  }, {});
};

/**
 * Build query parameters string
 * @param {Array} paramsArray - Array of parameter objects with key and value
 * @returns {string} Query string
 */
export const buildQueryString = (paramsArray) => {
  if (!Array.isArray(paramsArray)) return '';
  
  const validParams = paramsArray.filter(param => param.key && param.value !== undefined);
  
  if (validParams.length === 0) return '';
  
  const queryParams = new URLSearchParams();
  validParams.forEach(param => {
    queryParams.append(param.key, param.value);
  });
  
  return queryParams.toString();
};

/**
 * Format collection for display
 * @param {Object} collection - Collection data
 * @returns {Object} Formatted collection
 */
export const formatCollection = (collection) => {
  if (!collection) return {};
  
  const formatted = { ...collection };
  
  // Format dates
  if (formatted.createdAt) {
    formatted.formattedCreatedAt = formatDateForDisplay(formatted.createdAt);
  }
  
  if (formatted.updatedAt) {
    formatted.formattedUpdatedAt = formatDateForDisplay(formatted.updatedAt);
  }
  
  // Format request count
  if (typeof formatted.requestsCount === 'number') {
    formatted.formattedRequestCount = `${formatted.requestsCount} request${formatted.requestsCount !== 1 ? 's' : ''}`;
  }
  
  // Format favorite status
  if (formatted.favorite !== undefined) {
    formatted.isFavorite = formatted.favorite;
  }
  
  return formatted;
};

/**
 * Format request for display
 * @param {Object} request - Request data
 * @returns {Object} Formatted request
 */
export const formatRequest = (request) => {
  if (!request) return {};
  
  const formatted = { ...request };
  
  // Format dates
  if (formatted.createdAt) {
    formatted.formattedCreatedAt = formatDateForDisplay(formatted.createdAt);
  }
  
  if (formatted.updatedAt) {
    formatted.formattedUpdatedAt = formatDateForDisplay(formatted.updatedAt);
  }
  
  // Format method with color
  if (formatted.method) {
    formatted.methodColor = getMethodColor(formatted.method);
  }
  
  // Format URL
  if (formatted.url) {
    const urlParts = formatted.url.split('?');
    formatted.baseUrl = urlParts[0];
    formatted.queryString = urlParts[1] || '';
  }
  
  // Format body preview
  if (formatted.body) {
    formatted.bodyPreview = getBodyPreview(formatted.body);
  }
  
  // Format auth type
  if (formatted.authType) {
    formatted.formattedAuthType = formatAuthType(formatted.authType);
  }
  
  return formatted;
};

/**
 * Format auth type for display
 * @param {string} authType - Auth type value
 * @returns {string} Formatted auth type
 */
export const formatAuthType = (authType) => {
  const authTypeMap = {
    'noauth': 'No Auth',
    'bearer': 'Bearer Token',
    'basic': 'Basic Auth',
    'apikey': 'API Key',
    'oauth2': 'OAuth 2.0'
  };
  
  return authTypeMap[authType] || authType;
};

/**
 * Get HTTP method color
 * @param {string} method - HTTP method
 * @returns {string} Color code
 */
export const getMethodColor = (method) => {
  const methodColors = {
    'GET': '#61affe',
    'POST': '#49cc90',
    'PUT': '#fca130',
    'DELETE': '#f93e3e',
    'PATCH': '#50e3c2',
    'HEAD': '#9012fe',
    'OPTIONS': '#0d5aa7'
  };
  
  return methodColors[method.toUpperCase()] || '#999999';
};

/**
 * Get body preview
 * @param {any} body - Request body
 * @returns {string} Body preview
 */
export const getBodyPreview = (body) => {
  if (!body) return '';
  
  try {
    if (typeof body === 'string') {
      // Try to parse as JSON
      try {
        const parsed = JSON.parse(body);
        return JSON.stringify(parsed, null, 2).substring(0, 200) + (JSON.stringify(parsed).length > 200 ? '...' : '');
      } catch {
        // Not JSON, return as string
        return body.substring(0, 200) + (body.length > 200 ? '...' : '');
      }
    } else if (typeof body === 'object') {
      return JSON.stringify(body, null, 2).substring(0, 200) + (JSON.stringify(body).length > 200 ? '...' : '');
    } else {
      return String(body).substring(0, 200);
    }
  } catch (error) {
    return String(body).substring(0, 200);
  }
};

/**
 * Format date for display
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
 * Get supported languages for code generation
 * @returns {Array} Supported languages
 */
export const getSupportedLanguages = () => {
  return [
    { value: 'javascript', label: 'JavaScript' },
    { value: 'python', label: 'Python' },
    { value: 'java', label: 'Java' },
    { value: 'curl', label: 'cURL' },
    { value: 'php', label: 'PHP' },
    { value: 'ruby', label: 'Ruby' },
    { value: 'nodejs', label: 'Node.js' },
    { value: 'csharp', label: 'C#' },
    { value: 'go', label: 'Go' },
    { value: 'swift', label: 'Swift' }
  ];
};

/**
 * Get supported import formats
 * @returns {Array} Supported import formats
 */
export const getSupportedImportFormats = () => {
  return [
    { value: 'postman', label: 'Postman Collection' },
    { value: 'openapi', label: 'OpenAPI/Swagger' },
    { value: 'insomnia', label: 'Insomnia' },
    { value: 'raml', label: 'RAML' },
    { value: 'graphql', label: 'GraphQL Schema' }
  ];
};

/**
 * Get default environment variables
 * @returns {Object} Default environment variables
 */
export const getDefaultEnvironmentVariables = () => {
  return {
    base_url: 'http://localhost:8080',
    api_url: 'https://api.example.com',
    access_token: '',
    refresh_token: '',
    api_key: ''
  };
};

// ============ CLIENT-SIDE CACHING UTILITIES ============
// Note: Server-side caching has been removed from the backend.
// The following utilities are for optional client-side caching only.
// Use them if you want to implement caching on the frontend.

/**
 * Cache collections data in localStorage (client-side only)
 * @param {string} userId - User ID
 * @param {Object} collectionsData - Collections data to cache
 * @param {number} ttlMinutes - Time to live in minutes (default: 15)
 */
export const cacheCollectionsData = (userId, collectionsData, ttlMinutes = 15) => {
  if (!userId || !collectionsData) return;
  
  const cacheKey = `collections_cache_${userId}`;
  const cacheData = {
    data: collectionsData,
    timestamp: Date.now(),
    ttl: ttlMinutes * 60 * 1000 // Convert to milliseconds
  };
  
  try {
    localStorage.setItem(cacheKey, JSON.stringify(cacheData));
  } catch (error) {
    console.error('Failed to cache collections data:', error);
  }
};

/**
 * Get cached collections data from localStorage (client-side only)
 * @param {string} userId - User ID
 * @returns {Object|null} Cached collections data or null
 */
export const getCachedCollectionsData = (userId) => {
  if (!userId) return null;
  
  const cacheKey = `collections_cache_${userId}`;
  
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
    console.error('Failed to get cached collections data:', error);
    return null;
  }
};

/**
 * Clear cached collections data from localStorage (client-side only)
 * @param {string} userId - User ID (optional, clears all if not provided)
 */
export const clearCachedCollectionsData = (userId = null) => {
  try {
    if (userId) {
      localStorage.removeItem(`collections_cache_${userId}`);
    } else {
      // Clear all collections cache
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('collections_cache_')) {
          localStorage.removeItem(key);
        }
      });
    }
  } catch (error) {
    console.error('Failed to clear cached collections data:', error);
  }
};