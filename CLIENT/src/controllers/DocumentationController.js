// controllers/DocumentationController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// ============ DOCUMENTATION METHODS ============

/**
 * Get API collections
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAPICollections = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/collections`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get API endpoints
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} folderId - Folder ID
 * @returns {Promise} API response
 */
export const getAPIEndpoints = async (authorizationHeader, collectionId, folderId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/collections/${collectionId}/folders/${folderId}/endpoints`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get endpoint details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} endpointId - Endpoint ID
 * @returns {Promise} API response
 */
export const getEndpointDetails = async (authorizationHeader, collectionId, endpointId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/collections/${collectionId}/endpoints/${endpointId}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get code examples
 * @param {string} authorizationHeader - Bearer token
 * @param {string} endpointId - Endpoint ID
 * @param {string} language - Programming language
 * @returns {Promise} API response
 */
export const getCodeExamples = async (authorizationHeader, endpointId, language) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/endpoints/${endpointId}/code-examples?language=${encodeURIComponent(language)}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Search documentation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} query - Search query
 * @param {Object} options - Search options
 * @param {string} options.type - Search type (default: 'all')
 * @param {number} options.maxResults - Maximum results (default: 10)
 * @returns {Promise} API response
 */
export const searchDocumentation = async (authorizationHeader, query, options = {}) => {
  const { type = 'all', maxResults = 10 } = options;
  
  const queryString = new URLSearchParams({
    query: encodeURIComponent(query),
    type: type,
    maxResults: maxResults.toString()
  }).toString();
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/search?${queryString}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Publish documentation
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} publishData - Publish request data
 * @param {string} publishData.collectionId - Collection ID (required)
 * @param {string} publishData.title - Documentation title (required)
 * @param {string} publishData.visibility - Visibility (public/private) (required)
 * @param {string} publishData.customDomain - Custom domain (optional)
 * @returns {Promise} API response
 */
export const publishDocumentation = async (authorizationHeader, publishData) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/publish`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(publishData)
    })
  );
};

/**
 * Get environments
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDocumentationEnvironments = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/environments`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get notifications
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDocumentationNotifications = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/notifications`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get changelog
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @returns {Promise} API response
 */
export const getChangelog = async (authorizationHeader, collectionId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/collections/${collectionId}/changelog`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Generate mock server
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} mockRequest - Mock server request data
 * @param {string} mockRequest.collectionId - Collection ID (required)
 * @param {Object} mockRequest.options - Mock server options
 * @returns {Promise} API response
 */
export const generateMockServer = async (authorizationHeader, mockRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/generate-mock`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(mockRequest)
    })
  );
};

/**
 * Clear documentation cache
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const clearDocumentationCache = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/documentation/cache/clear`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for documentation operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleDocumentationResponse = (response) => {
  if (!response) {
    throw new Error('No response received from documentation service');
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
 * Extract API collections from response
 * @param {Object} response - API response
 * @returns {Array} API collections list
 */
export const extractAPICollections = (response) => {
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
 * Extract API endpoints from response
 * @param {Object} response - API response
 * @returns {Array} API endpoints list
 */
export const extractAPIEndpoints = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.endpoints && Array.isArray(data.endpoints)) {
    return data.endpoints;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract endpoint details from response
 * @param {Object} response - API response
 * @returns {Object} Endpoint details
 */
export const extractEndpointDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    id: details.endpointId || details.id,
    name: details.name || details.endpointName,
    method: details.method,
    url: details.url,
    description: details.description,
    category: details.category,
    tags: details.tags || [],
    lastModified: details.lastModified || details.updatedAt,
    version: details.version,
    requiresAuthentication: details.requiresAuthentication || false,
    rateLimit: details.rateLimit,
    deprecated: details.deprecated || false,
    headers: details.headers || [],
    parameters: details.parameters || [],
    requestBodyExample: details.requestBodyExample,
    responseExamples: details.responseExamples || [],
    rateLimitInfo: details.rateLimitInfo || {},
    changelog: details.changelog || [],
    metadata: details.metadata || {}
  };
};

/**
 * Extract code examples from response
 * @param {Object} response - API response
 * @returns {Object} Code examples data
 */
export const extractCodeExamples = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    language: data.language,
    endpointId: data.endpointId,
    code: data.code || data.example,
    message: data.message,
    success: data.code !== undefined,
    metadata: data.metadata || {}
  };
};

/**
 * Extract search results
 * @param {Object} response - API response
 * @returns {Array} Search results
 */
export const extractSearchResults = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.results && Array.isArray(data.results)) {
    return data.results;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract publish results
 * @param {Object} response - API response
 * @returns {Object} Publish results
 */
export const extractPublishResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    publishedUrl: data.publishedUrl,
    collectionId: data.collectionId,
    message: data.message,
    success: data.publishedUrl !== undefined,
    metadata: data.metadata || {}
  };
};

/**
 * Extract environments list from documentation response
 * @param {Object} response - API response
 * @returns {Array} Environments list
 */
export const extractDocumentationEnvironments = (response) => {
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
 * Extract notifications
 * @param {Object} response - API response
 * @returns {Array} Notifications list
 */
export const extractNotifications = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.notifications && Array.isArray(data.notifications)) {
    return data.notifications;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract changelog
 * @param {Object} response - API response
 * @returns {Array} Changelog entries
 */
export const extractChangelog = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.entries && Array.isArray(data.entries)) {
    return data.entries;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract mock server results
 * @param {Object} response - API response
 * @returns {Object} Mock server results
 */
export const extractMockServerResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    mockEndpoints: data.mockEndpoints || [],
    collectionId: data.collectionId,
    message: data.message,
    success: (data.mockEndpoints && Array.isArray(data.mockEndpoints)) || false,
    metadata: data.metadata || {}
  };
};

// ============ VALIDATION & UTILITY FUNCTIONS ============

/**
 * Validate publish documentation data
 * @param {Object} publishData - Publish data to validate
 * @returns {Array} Array of validation errors
 */
export const validatePublishDocumentation = (publishData) => {
  const errors = [];
  
  if (!publishData.collectionId) {
    errors.push('Collection ID is required');
  }
  
  if (!publishData.title || publishData.title.trim().length === 0) {
    errors.push('Documentation title is required');
  }
  
  if (!publishData.visibility) {
    errors.push('Visibility is required');
  }
  
  const validVisibilities = ['public', 'private', 'restricted'];
  if (publishData.visibility && !validVisibilities.includes(publishData.visibility.toLowerCase())) {
    errors.push(`Visibility must be one of: ${validVisibilities.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate generate mock server data
 * @param {Object} mockData - Mock server data to validate
 * @returns {Array} Array of validation errors
 */
export const validateGenerateMockServer = (mockData) => {
  const errors = [];
  
  if (!mockData.collectionId) {
    errors.push('Collection ID is required');
  }
  
  return errors;
};

/**
 * Validate search documentation parameters
 * @param {Object} searchParams - Search parameters to validate
 * @returns {Array} Array of validation errors
 */
export const validateSearchDocumentation = (searchParams) => {
  const errors = [];
  
  if (!searchParams.query || searchParams.query.trim().length === 0) {
    errors.push('Search query is required');
  }
  
  const validTypes = ['all', 'endpoints', 'collections', 'folders', 'parameters', 'responses'];
  if (searchParams.type && !validTypes.includes(searchParams.type.toLowerCase())) {
    errors.push(`Search type must be one of: ${validTypes.join(', ')}`);
  }
  
  if (searchParams.maxResults && (isNaN(searchParams.maxResults) || searchParams.maxResults < 1 || searchParams.maxResults > 100)) {
    errors.push('Max results must be a number between 1 and 100');
  }
  
  return errors;
};

/**
 * Get supported programming languages for code examples
 * @returns {Array} Supported languages
 */
export const getSupportedLanguages = () => {
  return [
    { value: 'curl', label: 'cURL' },
    { value: 'javascript', label: 'JavaScript' },
    { value: 'python', label: 'Python' },
    { value: 'nodejs', label: 'Node.js' },
    { value: 'java', label: 'Java' },
    { value: 'csharp', label: 'C#' },
    { value: 'php', label: 'PHP' },
    { value: 'go', label: 'Go' },
    { value: 'ruby', label: 'Ruby' },
    { value: 'swift', label: 'Swift' },
    { value: 'kotlin', label: 'Kotlin' }
  ];
};

/**
 * Get supported API types
 * @returns {Array} Supported API types
 */
export const getSupportedAPITypes = () => {
  return [
    { value: 'REST', label: 'REST API' },
    { value: 'SOAP', label: 'SOAP API' },
    { value: 'GraphQL', label: 'GraphQL' },
    { value: 'gRPC', label: 'gRPC' },
    { value: 'WebSocket', label: 'WebSocket' }
  ];
};

/**
 * Get supported HTTP methods
 * @returns {Array} Supported HTTP methods
 */
export const getSupportedHTTPMethods = () => {
  return [
    { value: 'GET', label: 'GET' },
    { value: 'POST', label: 'POST' },
    { value: 'PUT', label: 'PUT' },
    { value: 'DELETE', label: 'DELETE' },
    { value: 'PATCH', label: 'PATCH' },
    { value: 'HEAD', label: 'HEAD' },
    { value: 'OPTIONS', label: 'OPTIONS' }
  ];
};

/**
 * Get supported content types
 * @returns {Array} Supported content types
 */
export const getSupportedContentTypes = () => {
  return [
    { value: 'application/json', label: 'JSON' },
    { value: 'application/xml', label: 'XML' },
    { value: 'application/x-www-form-urlencoded', label: 'Form URL Encoded' },
    { value: 'multipart/form-data', label: 'Multipart Form Data' },
    { value: 'text/plain', label: 'Plain Text' },
    { value: 'text/html', label: 'HTML' }
  ];
};

/**
 * Get supported visibility options
 * @returns {Array} Supported visibility options
 */
export const getSupportedVisibilityOptions = () => {
  return [
    { value: 'public', label: 'Public', description: 'Accessible to anyone with the link' },
    { value: 'private', label: 'Private', description: 'Only accessible to authenticated users' },
    { value: 'restricted', label: 'Restricted', description: 'Accessible to specific users/groups' }
  ];
};

/**
 * Get supported environment types
 * @returns {Array} Supported environment types
 */
export const getSupportedEnvironmentTypes = () => {
  return [
    { value: 'sandbox', label: 'Sandbox', description: 'Testing environment with mock data' },
    { value: 'development', label: 'Development', description: 'Development environment' },
    { value: 'staging', label: 'Staging', description: 'Staging environment' },
    { value: 'uat', label: 'UAT', description: 'User Acceptance Testing' },
    { value: 'production', label: 'Production', description: 'Live production environment' }
  ];
};

/**
 * Get supported documentation formats
 * @returns {Array} Supported documentation formats
 */
export const getSupportedDocumentationFormats = () => {
  return [
    { value: 'html', label: 'HTML', description: 'Static HTML documentation' },
    { value: 'markdown', label: 'Markdown', description: 'Markdown documentation' },
    { value: 'openapi', label: 'OpenAPI', description: 'OpenAPI/Swagger specification' },
    { value: 'postman', label: 'Postman', description: 'Postman collection' }
  ];
};

/**
 * Format collection for documentation display
 * @param {Object} collection - Collection data
 * @returns {Object} Formatted collection
 */
export const formatDocumentationCollection = (collection) => {
  if (!collection) return {};
  
  const formatted = { ...collection };
  
  // Format dates
  if (formatted.updatedAt) {
    formatted.formattedUpdatedAt = formatDateForDisplay(formatted.updatedAt);
    formatted.timeAgo = getTimeAgo(formatted.updatedAt);
  }
  
  if (formatted.createdAt) {
    formatted.formattedCreatedAt = formatDateForDisplay(formatted.createdAt);
  }
  
  // Format endpoint count
  if (typeof formatted.totalEndpoints === 'number') {
    formatted.formattedEndpointCount = `${formatted.totalEndpoints} endpoint${formatted.totalEndpoints !== 1 ? 's' : ''}`;
  }
  
  if (typeof formatted.totalFolders === 'number') {
    formatted.formattedFolderCount = `${formatted.totalFolders} folder${formatted.totalFolders !== 1 ? 's' : ''}`;
  }
  
  // Format API type
  if (formatted.type) {
    formatted.formattedType = formatAPIType(formatted.type);
  }
  
  // Add color if not present
  if (!formatted.color) {
    formatted.color = getCollectionColor(formatted.id || formatted.name);
  }
  
  // Set default expanded state
  if (formatted.expanded === undefined) {
    formatted.expanded = false;
  }
  
  // Format owner
  if (formatted.owner) {
    formatted.ownerInitials = getInitials(formatted.owner);
  }
  
  return formatted;
};

/**
 * Format endpoint for documentation display
 * @param {Object} endpoint - Endpoint data
 * @returns {Object} Formatted endpoint
 */
export const formatDocumentationEndpoint = (endpoint) => {
  if (!endpoint) return {};
  
  const formatted = { ...endpoint };
  
  // Format last modified
  if (formatted.lastModified) {
    formatted.formattedLastModified = formatDateForDisplay(formatted.lastModified);
    formatted.timeAgo = getTimeAgo(formatted.lastModified);
  }
  
  // Format method with color
  if (formatted.method) {
    formatted.methodColor = getMethodColor(formatted.method);
  }
  
  // Format URL
  if (formatted.url) {
    const urlParts = formatted.url.split('/');
    formatted.path = '/' + urlParts.slice(3).join('/'); // Remove protocol and domain
    formatted.formattedPath = formatted.path.replace(/{([^}]+)}/g, '<span class="path-param">{$1}</span>');
  }
  
  // Format requires auth
  if (formatted.requiresAuth !== undefined) {
    formatted.authStatus = formatted.requiresAuth ? 'Requires Auth' : 'No Auth';
  }
  
  // Format deprecated
  if (formatted.deprecated) {
    formatted.deprecatedBadge = 'Deprecated';
  }
  
  // Format tags
  if (formatted.tags && Array.isArray(formatted.tags)) {
    formatted.formattedTags = formatted.tags.map(tag => ({
      name: tag,
      color: getTagColor(tag)
    }));
  }
  
  // Set default expanded state
  if (formatted.expanded === undefined) {
    formatted.expanded = false;
  }
  
  return formatted;
};

/**
 * Format endpoint details for display
 * @param {Object} details - Endpoint details
 * @returns {Object} Formatted endpoint details
 */
export const formatEndpointDetails = (details) => {
  if (!details) return {};
  
  const formatted = { ...details };
  
  // Format headers
  if (formatted.headers && Array.isArray(formatted.headers)) {
    formatted.formattedHeaders = formatted.headers.map(header => ({
      ...header,
      requiredBadge: header.required ? 'Required' : 'Optional'
    }));
  }
  
  // Format parameters
  if (formatted.parameters && Array.isArray(formatted.parameters)) {
    formatted.formattedParameters = formatted.parameters.map(param => ({
      ...param,
      requiredBadge: param.required ? 'Required' : 'Optional'
    }));
  }
  
  // Format response examples
  if (formatted.responseExamples && Array.isArray(formatted.responseExamples)) {
    formatted.formattedResponseExamples = formatted.responseExamples.map(example => ({
      ...example,
      statusBadge: getStatusCodeBadge(example.statusCode),
      formattedExample: formatJsonExample(example.example)
    }));
  }
  
  // Format changelog
  if (formatted.changelog && Array.isArray(formatted.changelog)) {
    formatted.formattedChangelog = formatted.changelog.map(entry => ({
      ...entry,
      timeAgo: getTimeAgo(entry.date)
    }));
  }
  
  // Format rate limit
  if (formatted.rateLimit) {
    formatted.formattedRateLimit = formatRateLimit(formatted.rateLimit);
  }
  
  return formatted;
};

/**
 * Format search result for display
 * @param {Object} result - Search result
 * @returns {Object} Formatted search result
 */
export const formatSearchResult = (result) => {
  if (!result) return {};
  
  const formatted = { ...result };
  
  // Format last updated
  if (formatted.lastUpdated) {
    formatted.formattedLastUpdated = formatDateForDisplay(formatted.lastUpdated);
    formatted.timeAgo = getTimeAgo(formatted.lastUpdated);
  }
  
  // Format relevance score
  if (formatted.relevanceScore) {
    formatted.relevancePercentage = Math.min(100, Math.max(0, formatted.relevanceScore));
    formatted.relevanceColor = getRelevanceColor(formatted.relevanceScore);
  }
  
  // Format type
  if (formatted.type) {
    formatted.formattedType = formatSearchResultType(formatted.type);
  }
  
  // Format URL
  if (formatted.endpointUrl) {
    formatted.formattedPath = formatted.endpointUrl;
  }
  
  return formatted;
};

/**
 * Format API type for display
 * @param {string} type - API type
 * @returns {string} Formatted API type
 */
export const formatAPIType = (type) => {
  const typeMap = {
    'REST': 'REST API',
    'SOAP': 'SOAP API',
    'GraphQL': 'GraphQL',
    'gRPC': 'gRPC',
    'WebSocket': 'WebSocket'
  };
  
  return typeMap[type] || type;
};

/**
 * Format search result type for display
 * @param {string} type - Search result type
 * @returns {string} Formatted type
 */
export const formatSearchResultType = (type) => {
  const typeMap = {
    'Endpoint': 'API Endpoint',
    'Collection': 'API Collection',
    'Folder': 'Folder',
    'Parameter': 'Parameter',
    'Response': 'Response',
    'Example': 'Code Example'
  };
  
  return typeMap[type] || type;
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
 * Get tag color
 * @param {string} tag - Tag name
 * @returns {string} Color code
 */
export const getTagColor = (tag) => {
  // Simple hash function to generate consistent colors
  let hash = 0;
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  const colors = [
    '#FF6B6B', '#4ECDC4', '#FFD166', '#06D6A0', '#118AB2',
    '#EF476F', '#1B9AAA', '#FF9A76', '#7D5BA6', '#03CEA4',
    '#FBAF00', '#5F5AA2', '#35524A', '#627264', '#C2EABD'
  ];
  
  return colors[Math.abs(hash) % colors.length];
};

/**
 * Get collection color
 * @param {string} identifier - Collection identifier or name
 * @returns {string} Color code
 */
export const getCollectionColor = (identifier) => {
  // Simple hash function to generate consistent colors
  let hash = 0;
  for (let i = 0; i < identifier.length; i++) {
    hash = identifier.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  const colors = [
    '#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#EF4444',
    '#06B6D4', '#84CC16', '#F97316', '#8B5CF6', '#EC4899',
    '#14B8A6', '#F43F5E', '#0EA5E9', '#84CC16', '#6366F1'
  ];
  
  return colors[Math.abs(hash) % colors.length];
};

/**
 * Get relevance color based on score
 * @param {number} score - Relevance score (0-100)
 * @returns {string} Color code
 */
export const getRelevanceColor = (score) => {
  if (score >= 80) return '#10B981'; // High relevance - green
  if (score >= 60) return '#F59E0B'; // Medium relevance - amber
  if (score >= 40) return '#3B82F6'; // Low relevance - blue
  return '#6B7280'; // Very low relevance - gray
};

/**
 * Get status code badge color
 * @param {number} statusCode - HTTP status code
 * @returns {Object} Badge configuration
 */
export const getStatusCodeBadge = (statusCode) => {
  const badgeMap = {
    200: { text: '200 OK', color: '#10B981' },
    201: { text: '201 Created', color: '#10B981' },
    204: { text: '204 No Content', color: '#10B981' },
    400: { text: '400 Bad Request', color: '#F59E0B' },
    401: { text: '401 Unauthorized', color: '#F59E0B' },
    403: { text: '403 Forbidden', color: '#F59E0B' },
    404: { text: '404 Not Found', color: '#F59E0B' },
    500: { text: '500 Server Error', color: '#EF4444' },
    502: { text: '502 Bad Gateway', color: '#EF4444' },
    503: { text: '503 Service Unavailable', color: '#EF4444' }
  };
  
  return badgeMap[statusCode] || { text: `${statusCode}`, color: '#6B7280' };
};

/**
 * Format rate limit string
 * @param {string} rateLimit - Rate limit string
 * @returns {string} Formatted rate limit
 */
export const formatRateLimit = (rateLimit) => {
  if (!rateLimit) return 'No rate limit';
  
  // Try to extract numeric values
  const matches = rateLimit.match(/(\d+)\s*(?:requests?)?\s*per\s*(\w+)/i);
  if (matches) {
    const count = matches[1];
    const unit = matches[2].toLowerCase();
    
    const unitMap = {
      'second': 'second',
      'minute': 'minute',
      'hour': 'hour',
      'day': 'day',
      'month': 'month'
    };
    
    const formattedUnit = unitMap[unit] || unit;
    return `${count} requests per ${formattedUnit}`;
  }
  
  return rateLimit;
};

/**
 * Format JSON example for display
 * @param {string} json - JSON string
 * @returns {string} Formatted JSON
 */
export const formatJsonExample = (json) => {
  if (!json) return '';
  
  try {
    // Try to parse as JSON
    const parsed = JSON.parse(json);
    return JSON.stringify(parsed, null, 2);
  } catch {
    // Not valid JSON, return as-is
    return json;
  }
};

/**
 * Get time ago string
 * @param {string|Date} date - Date
 * @returns {string} Time ago string
 */
export const getTimeAgo = (date) => {
  if (!date) return '';
  
  const d = new Date(date);
  const now = new Date();
  const diffMs = now - d;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);
  const diffWeek = Math.floor(diffDay / 7);
  const diffMonth = Math.floor(diffDay / 30);
  const diffYear = Math.floor(diffDay / 365);
  
  if (diffYear > 0) return `${diffYear} year${diffYear > 1 ? 's' : ''} ago`;
  if (diffMonth > 0) return `${diffMonth} month${diffMonth > 1 ? 's' : ''} ago`;
  if (diffWeek > 0) return `${diffWeek} week${diffWeek > 1 ? 's' : ''} ago`;
  if (diffDay > 0) return `${diffDay} day${diffDay > 1 ? 's' : ''} ago`;
  if (diffHour > 0) return `${diffHour} hour${diffHour > 1 ? 's' : ''} ago`;
  if (diffMin > 0) return `${diffMin} minute${diffMin > 1 ? 's' : ''} ago`;
  return 'just now';
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
    minute: '2-digit'
  });
};

/**
 * Get initials from name
 * @param {string} name - Full name
 * @returns {string} Initials
 */
export const getInitials = (name) => {
  if (!name) return '';
  
  return name
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .substring(0, 2);
};

/**
 * Generate documentation URL
 * @param {Object} publishData - Publish data
 * @returns {string} Documentation URL
 */
export const generateDocumentationUrl = (publishData) => {
  const { collectionId, title, customDomain } = publishData;
  
  let baseUrl = 'https://docs.fintech.com';
  if (customDomain) {
    baseUrl = `https://${customDomain}`;
  }
  
  const slug = title
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '');
  
  return `${baseUrl}/api/${collectionId}/${slug}`;
};

/**
 * Cache documentation data in localStorage
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @param {Object} data - Data to cache
 * @param {number} ttlMinutes - Time to live in minutes (default: 30)
 */
export const cacheDocumentationData = (userId, cacheKey, data, ttlMinutes = 30) => {
  if (!userId || !cacheKey || !data) return;
  
  const fullCacheKey = `documentation_${userId}_${cacheKey}`;
  const cacheData = {
    data: data,
    timestamp: Date.now(),
    ttl: ttlMinutes * 60 * 1000 // Convert to milliseconds
  };
  
  try {
    localStorage.setItem(fullCacheKey, JSON.stringify(cacheData));
  } catch (error) {
    console.error('Failed to cache documentation data:', error);
  }
};

/**
 * Get cached documentation data
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @returns {Object|null} Cached data or null
 */
export const getCachedDocumentationData = (userId, cacheKey) => {
  if (!userId || !cacheKey) return null;
  
  const fullCacheKey = `documentation_${userId}_${cacheKey}`;
  
  try {
    const cached = localStorage.getItem(fullCacheKey);
    
    if (!cached) return null;
    
    const cacheData = JSON.parse(cached);
    
    // Check if cache is expired
    const now = Date.now();
    const isExpired = now - cacheData.timestamp > cacheData.ttl;
    
    if (isExpired) {
      localStorage.removeItem(fullCacheKey);
      return null;
    }
    
    return cacheData.data;
  } catch (error) {
    console.error('Failed to get cached documentation data:', error);
    return null;
  }
};

/**
 * Clear cached documentation data
 * @param {string} userId - User ID (optional, clears all if not provided)
 */
export const clearCachedDocumentationData = (userId = null) => {
  try {
    if (userId) {
      // Clear all documentation cache for this user
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith(`documentation_${userId}_`)) {
          localStorage.removeItem(key);
        }
      });
    } else {
      // Clear all documentation cache
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('documentation_')) {
          localStorage.removeItem(key);
        }
      });
    }
  } catch (error) {
    console.error('Failed to clear cached documentation data:', error);
  }
};

// Export all functions
export default {
  // Main API methods
  getAPICollections,
  getAPIEndpoints,
  getEndpointDetails,
  getCodeExamples,
  searchDocumentation,
  publishDocumentation,
  getDocumentationEnvironments,
  getDocumentationNotifications,
  getChangelog,
  generateMockServer,
  clearDocumentationCache,
  
  // Response handlers
  handleDocumentationResponse,
  extractAPICollections,
  extractAPIEndpoints,
  extractEndpointDetails,
  extractCodeExamples,
  extractSearchResults,
  extractPublishResults,
  extractDocumentationEnvironments,
  extractNotifications,
  extractChangelog,
  extractMockServerResults,
  
  // Validation functions
  validatePublishDocumentation,
  validateGenerateMockServer,
  validateSearchDocumentation,
  
  // Utility functions
  getSupportedLanguages,
  getSupportedAPITypes,
  getSupportedHTTPMethods,
  getSupportedContentTypes,
  getSupportedVisibilityOptions,
  getSupportedEnvironmentTypes,
  getSupportedDocumentationFormats,
  formatDocumentationCollection,
  formatDocumentationEndpoint,
  formatEndpointDetails,
  formatSearchResult,
  formatAPIType,
  formatSearchResultType,
  getMethodColor,
  getTagColor,
  getCollectionColor,
  getRelevanceColor,
  getStatusCodeBadge,
  formatRateLimit,
  formatJsonExample,
  getTimeAgo,
  formatDateForDisplay,
  getInitials,
  generateDocumentationUrl,
  
  // Cache functions
  cacheDocumentationData,
  getCachedDocumentationData,
  clearCachedDocumentationData
};