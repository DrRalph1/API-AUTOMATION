// controllers/CodeBaseController.js
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper function to get base API URL
const getBaseUrl = () => {
  return '/codebase';
};

// ============ CODEBASE METHODS ============

/**
 * Get collections list from codebase
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getCollectionsListFromCodebase = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/collections`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get collection details from codebase
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @returns {Promise} API response
 */
export const getCollectionDetailsFromCodebase = async (authorizationHeader, collectionId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/collections/${collectionId}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get folder requests from codebase
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} folderId - Folder ID
 * @returns {Promise} API response
 */
export const getFolderRequestsFromCodebase = async (authorizationHeader, collectionId, folderId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/collections/${collectionId}/folders/${folderId}/requests`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get request details from codebase
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} requestId - Request ID
 * @returns {Promise} API response
 */
export const getRequestDetailsFromCodebase = async (authorizationHeader, collectionId, requestId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/collections/${collectionId}/requests/${requestId}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get implementation details for specific component and language
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} requestId - Request ID
 * @param {string} language - Programming language (java, javascript, python, etc.)
 * @param {string} component - Component type (controller, service, repository, etc.)
 * @returns {Promise} API response
 */
export const getImplementationDetails = async (authorizationHeader, collectionId, requestId, language, component) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/collections/${collectionId}/requests/${requestId}/implementations/${language}/${component}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get all implementations for a request
 * @param {string} authorizationHeader - Bearer token
 * @param {string} collectionId - Collection ID
 * @param {string} requestId - Request ID
 * @returns {Promise} API response
 */
export const getAllImplementations = async (authorizationHeader, collectionId, requestId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/collections/${collectionId}/requests/${requestId}/implementations`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Generate API implementation
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} generateRequest - Generate implementation request data
 * @param {string} generateRequest.requestId - Request ID
 * @param {string} generateRequest.collectionId - Collection ID
 * @param {string} generateRequest.language - Programming language (required)
 * @param {Array} generateRequest.components - Components to generate (controller, service, etc.)
 * @param {Object} generateRequest.options - Generation options
 * @returns {Promise} API response
 */
export const generateImplementation = async (authorizationHeader, generateRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/generate-implementation`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(generateRequest)
    })
  );
};

/**
 * Export implementation
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} exportRequest - Export request data
 * @param {string} exportRequest.language - Programming language (required)
 * @param {string} exportRequest.format - Export format (complete, single) (required)
 * @param {string} exportRequest.requestId - Request ID (optional)
 * @param {string} exportRequest.collectionId - Collection ID (optional)
 * @param {Array} exportRequest.components - Components to export
 * @returns {Promise} API response
 */
export const exportImplementation = async (authorizationHeader, exportRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/export`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(exportRequest)
    })
  );
};

/**
 * Get available programming languages
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getLanguages = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/languages`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Search implementations
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchRequest - Search request data
 * @param {string} searchRequest.query - Search query (required)
 * @param {string} searchRequest.language - Filter by language
 * @param {string} searchRequest.collection - Filter by collection
 * @param {string} searchRequest.component - Filter by component
 * @param {number} searchRequest.limit - Result limit
 * @param {number} searchRequest.offset - Result offset
 * @returns {Promise} API response
 */
export const searchImplementations = async (authorizationHeader, searchRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/search`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(searchRequest)
    })
  );
};

/**
 * Import API specification
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} importRequest - Import specification request data
 * @param {string} importRequest.source - Import source (openapi, postman, github) (required)
 * @param {string} importRequest.format - Import format
 * @param {string|Object} importRequest.data - Import data (URL or file content)
 * @param {Object} importRequest.options - Import options
 * @returns {Promise} API response
 */
export const importSpecification = async (authorizationHeader, importRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/import`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(importRequest)
    })
  );
};

/**
 * Validate implementation code
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} validationRequest - Validation request data
 * @param {string} validationRequest.language - Programming language (required)
 * @param {string} validationRequest.code - Code to validate (required)
 * @param {string} validationRequest.component - Component type
 * @param {Object} validationRequest.rules - Validation rules
 * @returns {Promise} API response
 */
export const validateImplementation = async (authorizationHeader, validationRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/validate`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(validationRequest)
    })
  );
};

/**
 * Test implementation
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} testRequest - Test request data
 * @param {string} testRequest.requestId - Request ID (required)
 * @param {string} testRequest.language - Programming language (required)
 * @param {Object} testRequest.implementation - Implementation code
 * @param {Object} testRequest.testData - Test data
 * @param {Object} testRequest.testCases - Test cases
 * @param {Object} testRequest.options - Test options
 * @returns {Promise} API response
 */
export const testImplementation = async (authorizationHeader, testRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/test`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(testRequest)
    })
  );
};

/**
 * Clear codebase cache
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const clearCodebaseCache = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/cache/clear`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get supported programming languages with details
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getSupportedProgrammingLanguages = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/supported-languages`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get quick start guide for a language
 * @param {string} authorizationHeader - Bearer token
 * @param {string} language - Programming language
 * @returns {Promise} API response
 */
export const getQuickStartGuide = async (authorizationHeader, language) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`${getBaseUrl()}/quick-start/${language}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for codebase operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleCodebaseResponse = (response) => {
  if (!response) {
    throw new Error('No response received from codebase service');
  }

  const responseCode = response.responseCode || response.status;
  
  if (responseCode === 200 || responseCode === 201) {
    return {
      ...response,
      data: response.data || {},
      responseCode: responseCode,
      requestId: response.requestId,
      message: response.message || 'Success'
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
 * Extract collections list from codebase response
 * @param {Object} response - API response
 * @returns {Array} Collections list
 */
export const extractCodebaseCollectionsList = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  // Handle different response structures
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.collections && Array.isArray(data.collections)) {
    return data.collections;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  // Try to extract from common DTO structure
  if (data.collectionList && Array.isArray(data.collectionList)) {
    return data.collectionList;
  }
  
  return [];
};

/**
 * Extract collection details from codebase response
 * @param {Object} response - API response
 * @returns {Object} Collection details
 */
export const extractCodebaseCollectionDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    id: details.id || details.collectionId,
    name: details.name || details.collectionName,
    description: details.description,
    version: details.version,
    owner: details.owner || details.performedBy,
    createdAt: details.createdAt || details.createdDate,
    updatedAt: details.updatedAt || details.modifiedDate,
    isFavorite: details.isFavorite || details.favorite || false,
    folders: details.folders || [],
    totalRequests: details.totalRequests || 0,
    totalFolders: details.totalFolders || 0,
    tags: details.tags || [],
    metadata: details.metadata || {}
  };
};

/**
 * Extract folder requests from codebase response
 * @param {Object} response - API response
 * @returns {Object} Folder requests details
 */
export const extractFolderRequests = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    folderId: details.folderId,
    folderName: details.folderName,
    collectionId: details.collectionId,
    requests: details.requests || [],
    totalRequests: details.totalRequests || 0,
    subfolders: details.subfolders || [],
    createdAt: details.createdAt,
    updatedAt: details.updatedAt,
    metadata: details.metadata || {}
  };
};

/**
 * Extract request details from codebase response
 * @param {Object} response - API response
 * @returns {Object} Request details
 */
export const extractCodebaseRequestDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    id: details.id || details.requestId,
    name: details.name || details.requestName,
    method: details.method,
    url: details.url || details.endpoint,
    description: details.description,
    collectionId: details.collectionId,
    folderId: details.folderId,
    lastModified: details.lastModified || details.updatedAt,
    headers: details.headers || [],
    tags: details.tags || [],
    body: details.body || {},
    implementations: details.implementations || {},
    metadata: details.metadata || {},
    parameters: details.parameters || [],
    responses: details.responses || {}
  };
};

/**
 * Extract implementation details from response
 * @param {Object} response - API response
 * @returns {Object} Implementation details
 */
export const extractImplementationDetails = (response) => {
  if (!response || !response.data) return null;
  
  const details = response.data;
  
  return {
    language: details.language,
    component: details.component,
    requestId: details.requestId,
    collectionId: details.collectionId,
    code: details.code || details.snippet || details.implementation,
    fileName: details.fileName,
    languageInfo: details.languageInfo || {},
    success: details.success !== undefined ? details.success : (details.code !== undefined),
    // Add these fields to handle the "not found" case
    notFound: details.notFound === true,
    status: details.status,
    fileSize: details.fileSize,
    linesOfCode: details.linesOfCode,
    syntaxInfo: details.syntaxInfo,
    generatedAt: details.generatedAt,
    metadata: details.metadata || {},
    validationStatus: details.validationStatus,
    testResults: details.testResults
  };
};

/**
 * Extract all implementations for a request
 * @param {Object} response - API response
 * @returns {Object} All implementations data
 */
export const extractAllImplementations = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  // Transform the implementations data
  const transformedImplementations = {};
  
  if (data.implementations) {
    Object.entries(data.implementations).forEach(([language, implData]) => {
      transformedImplementations[language] = {};
      
      // Handle different possible structures
      if (typeof implData === 'object' && implData !== null) {
        Object.entries(implData).forEach(([componentKey, code]) => {
          // Map common component keys
          const componentName = componentKey === 'main' ? 'controller' : componentKey;
          transformedImplementations[language][componentName] = code;
        });
      }
    });
  }
  
  return {
    requestId: data.requestId,
    collectionId: data.collectionId,
    implementations: transformedImplementations,
    availableLanguages: data.availableLanguages || Object.keys(transformedImplementations),
    totalImplementations: data.totalImplementations || 0,
    totalFiles: data.totalFiles || 0,
    retrievedAt: data.retrievedAt,
    metadata: data.metadata || {}
  };
};

/**
 * Extract generate implementation results
 * @param {Object} response - API response
 * @returns {Object} Generate implementation results
 */
export const extractGenerateResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    requestId: data.requestId,
    language: data.language,
    generatedAt: data.generatedAt || data.timestamp,
    status: data.status,
    implementations: data.implementations || {},
    quickStartGuide: data.quickStartGuide || {},
    features: data.features || [],
    success: data.success !== undefined ? data.success : (data.requestId !== undefined),
    metadata: data.metadata || {},
    generatedComponents: data.generatedComponents || []
  };
};

/**
 * Extract export implementation results
 * @param {Object} response - API response
 * @returns {Object} Export implementation results
 */
export const extractExportResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    format: data.format,
    language: data.language,
    exportedAt: data.exportedAt || data.timestamp,
    exportData: data.exportData || {},
    success: data.success !== undefined ? data.success : (data.format !== undefined),
    metadata: data.metadata || {},
    downloadUrl: data.downloadUrl,
    fileContent: data.fileContent
  };
};

/**
 * Extract languages list
 * @param {Object} response - API response
 * @returns {Array} Languages list
 */
export const extractLanguages = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.languages && Array.isArray(data.languages)) {
    return data.languages;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  if (data.languageList && Array.isArray(data.languageList)) {
    return data.languageList;
  }
  
  return [];
};

/**
 * Extract search results
 * @param {Object} response - API response
 * @returns {Array} Search results
 */
export const extractSearchResults = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (data.results && Array.isArray(data.results)) {
    return data.results;
  }
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  if (data.searchResults && Array.isArray(data.searchResults)) {
    return data.searchResults;
  }
  
  return [];
};

/**
 * Extract import specification results
 * @param {Object} response - API response
 * @returns {Object} Import specification results
 */
export const extractImportSpecResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    source: data.source,
    importedAt: data.importedAt || data.timestamp,
    status: data.status,
    importData: data.importData || {},
    success: data.success !== undefined ? data.success : (data.importedAt !== undefined),
    metadata: data.metadata || {},
    importedCollections: data.importedCollections || [],
    totalRequests: data.totalRequests || 0
  };
};

/**
 * Extract validation results
 * @param {Object} response - API response
 * @returns {Object} Validation results
 */
export const extractValidationResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    language: data.language,
    isValid: data.isValid || false,
    warnings: data.warnings || [],
    suggestions: data.suggestions || [],
    errors: data.errors || [],
    score: data.score || 0,
    success: data.success !== undefined ? data.success : (data.language !== undefined),
    metadata: data.metadata || {},
    validationDetails: data.validationDetails || {}
  };
};

/**
 * Extract test results
 * @param {Object} response - API response
 * @returns {Object} Test results
 */
export const extractTestResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    testsPassed: data.testsPassed || 0,
    testsFailed: data.testsFailed || 0,
    totalTests: data.totalTests || 0,
    coverage: data.coverage || '0%',
    executionTime: data.executionTime,
    status: data.status || 'UNKNOWN',
    details: data.details || {},
    success: data.success !== undefined ? data.success : (data.testsPassed !== undefined),
    metadata: data.metadata || {},
    requestId: data.requestId,
    language: data.language
  };
};

/**
 * Extract supported programming languages with details
 * @param {Object} response - API response
 * @returns {Array} Supported languages with details
 */
export const extractSupportedProgrammingLanguages = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.languages && Array.isArray(data.languages)) {
    return data.languages;
  }
  
  if (data.supportedLanguages && Array.isArray(data.supportedLanguages)) {
    return data.supportedLanguages;
  }
  
  return [];
};

/**
 * Extract quick start guide
 * @param {Object} response - API response
 * @returns {Object} Quick start guide
 */
export const extractQuickStartGuide = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    language: data.language,
    steps: data.steps || [],
    prerequisites: data.prerequisites || [],
    installation: data.installation || {},
    configuration: data.configuration || {},
    running: data.running || {},
    testing: data.testing || {},
    metadata: data.metadata || {}
  };
};

// ============ VALIDATION & UTILITY FUNCTIONS ============

/**
 * Validate generate implementation request
 * @param {Object} generateRequest - Generate request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateGenerateImplementation = (generateRequest) => {
  const errors = [];
  
  if (!generateRequest.language) {
    errors.push('Programming language is required');
  }
  
  if (!generateRequest.requestId) {
    errors.push('Request ID is required');
  }
  
  if (!generateRequest.collectionId) {
    errors.push('Collection ID is required');
  }
  
  const validLanguages = ['java', 'javascript', 'python', 'csharp', 'php', 'go', 'ruby', 'kotlin', 'swift', 'rust'];
  if (generateRequest.language && !validLanguages.includes(generateRequest.language.toLowerCase())) {
    errors.push(`Language must be one of: ${validLanguages.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate export implementation request
 * @param {Object} exportRequest - Export request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateExportImplementation = (exportRequest) => {
  const errors = [];
  
  if (!exportRequest.language) {
    errors.push('Programming language is required');
  }
  
  if (!exportRequest.format) {
    errors.push('Export format is required');
  }
  
  const validLanguages = ['java', 'javascript', 'python', 'csharp', 'php', 'go', 'ruby', 'kotlin', 'swift', 'rust'];
  if (exportRequest.language && !validLanguages.includes(exportRequest.language.toLowerCase())) {
    errors.push(`Language must be one of: ${validLanguages.join(', ')}`);
  }
  
  const validFormats = ['complete', 'single'];
  if (exportRequest.format && !validFormats.includes(exportRequest.format.toLowerCase())) {
    errors.push(`Format must be one of: ${validFormats.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate search implementation request
 * @param {Object} searchRequest - Search request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateSearchImplementation = (searchRequest) => {
  const errors = [];
  
  if (!searchRequest.query || searchRequest.query.trim().length === 0) {
    errors.push('Search query is required');
  }
  
  if (searchRequest.query && searchRequest.query.length > 255) {
    errors.push('Search query must be 255 characters or less');
  }
  
  return errors;
};

/**
 * Validate import specification request
 * @param {Object} importRequest - Import request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateImportSpecification = (importRequest) => {
  const errors = [];
  
  if (!importRequest.source) {
    errors.push('Import source is required');
  }
  
  const validSources = ['openapi', 'postman', 'github'];
  if (importRequest.source && !validSources.includes(importRequest.source.toLowerCase())) {
    errors.push(`Source must be one of: ${validSources.join(', ')}`);
  }
  
  if (!importRequest.data) {
    errors.push('Import data is required');
  }
  
  return errors;
};

/**
 * Validate implementation validation request
 * @param {Object} validationRequest - Validation request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateImplementationValidation = (validationRequest) => {
  const errors = [];
  
  if (!validationRequest.language) {
    errors.push('Programming language is required');
  }
  
  if (!validationRequest.code || validationRequest.code.trim().length === 0) {
    errors.push('Code is required');
  }
  
  return errors;
};

/**
 * Validate test implementation request
 * @param {Object} testRequest - Test request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateTestImplementation = (testRequest) => {
  const errors = [];
  
  if (!testRequest.language) {
    errors.push('Programming language is required');
  }
  
  if (!testRequest.requestId) {
    errors.push('Request ID is required');
  }
  
  if (!testRequest.implementation) {
    errors.push('Implementation code is required');
  }
  
  return errors;
};

/**
 * Get default programming languages (fallback if API fails)
 * @returns {Array} Default supported languages
 */
export const getDefaultSupportedProgrammingLanguages = () => {
  return [
    { value: 'java', label: 'Java', framework: 'Spring Boot', color: '#f89820', icon: 'java' },
    { value: 'javascript', label: 'JavaScript', framework: 'Node.js/Express', color: '#f0db4f', icon: 'javascript' },
    { value: 'python', label: 'Python', framework: 'FastAPI/Django', color: '#3776ab', icon: 'python' },
    { value: 'csharp', label: 'C#', framework: '.NET Core', color: '#9b4993', icon: 'csharp' },
    { value: 'php', label: 'PHP', framework: 'Laravel', color: '#777bb4', icon: 'php' },
    { value: 'go', label: 'Go', framework: 'Gin', color: '#00add8', icon: 'go' },
    { value: 'ruby', label: 'Ruby', framework: 'Ruby on Rails', color: '#cc342d', icon: 'ruby' },
    { value: 'kotlin', label: 'Kotlin', framework: 'Ktor/Spring', color: '#7f52ff', icon: 'kotlin' },
    { value: 'swift', label: 'Swift', framework: 'Vapor', color: '#f05138', icon: 'swift' },
    { value: 'rust', label: 'Rust', framework: 'Actix-web', color: '#dea584', icon: 'rust' }
  ];
};

/**
 * Get supported components by language
 * @param {string} language - Programming language
 * @returns {Array} Supported components
 */
export const getSupportedComponents = (language) => {
  const componentsByLanguage = {
    java: [
      { value: 'controller', label: 'Controller' },
      { value: 'service', label: 'Service' },
      { value: 'repository', label: 'Repository' },
      { value: 'model', label: 'Model' },
      { value: 'dto', label: 'DTO' },
      { value: 'config', label: 'Configuration' }
    ],
    javascript: [
      { value: 'controller', label: 'Controller' },
      { value: 'service', label: 'Service' },
      { value: 'model', label: 'Model' },
      { value: 'routes', label: 'Routes' },
      { value: 'config', label: 'Configuration' }
    ],
    python: [
      { value: 'fastapi', label: 'FastAPI Main' },
      { value: 'schemas', label: 'Schemas' },
      { value: 'models', label: 'Models' },
      { value: 'routes', label: 'Routes' },
      { value: 'config', label: 'Configuration' }
    ],
    csharp: [
      { value: 'controller', label: 'Controller' },
      { value: 'service', label: 'Service' },
      { value: 'model', label: 'Model' },
      { value: 'repository', label: 'Repository' }
    ],
    php: [
      { value: 'controller', label: 'Controller' },
      { value: 'model', label: 'Model' },
      { value: 'service', label: 'Service' },
      { value: 'repository', label: 'Repository' }
    ],
    default: [
      { value: 'main', label: 'Main Implementation' },
      { value: 'config', label: 'Configuration' }
    ]
  };
  
  return componentsByLanguage[language] || componentsByLanguage.default;
};

/**
 * Get default quick start guide for language (fallback)
 * @param {string} language - Programming language
 * @returns {Object} Quick start guide
 */
export const getDefaultQuickStartGuide = (language) => {
  const guides = {
    java: {
      language: 'java',
      prerequisites: ['JDK 11+', 'Maven 3.6+'],
      steps: [
        { step: 1, title: 'Install dependencies', command: 'mvn install' },
        { step: 2, title: 'Run application', command: 'mvn spring-boot:run' },
        { step: 3, title: 'Test API', description: 'Open http://localhost:8080/swagger-ui.html' }
      ],
      configuration: {
        port: 8080,
        database: 'Configure in application.properties'
      }
    },
    javascript: {
      language: 'javascript',
      prerequisites: ['Node.js 14+', 'npm 6+'],
      steps: [
        { step: 1, title: 'Install dependencies', command: 'npm install' },
        { step: 2, title: 'Run application', command: 'npm start' },
        { step: 3, title: 'Test API', description: 'Open http://localhost:3000/api/docs' }
      ],
      configuration: {
        port: 3000,
        environment: 'Set in .env file'
      }
    },
    python: {
      language: 'python',
      prerequisites: ['Python 3.8+', 'pip'],
      steps: [
        { step: 1, title: 'Install dependencies', command: 'pip install -r requirements.txt' },
        { step: 2, title: 'Run application', command: 'uvicorn main:app --reload' },
        { step: 3, title: 'Test API', description: 'Open http://localhost:8000/docs' }
      ],
      configuration: {
        port: 8000,
        environment: 'Set in .env file'
      }
    },
    default: {
      language: 'default',
      prerequisites: ['Check language-specific requirements'],
      steps: [
        { step: 1, title: 'Install dependencies', command: 'See language documentation' },
        { step: 2, title: 'Run application', command: 'See language documentation' },
        { step: 3, title: 'Test API', description: 'Refer to API documentation' }
      ]
    }
  };
  
  return guides[language] || guides.default;
};

/**
 * Get language color
 * @param {string} language - Programming language
 * @returns {string} Color code
 */
export const getLanguageColor = (language) => {
  const colors = {
    java: '#f89820',
    javascript: '#f0db4f',
    python: '#3776ab',
    csharp: '#9b4993',
    php: '#777bb4',
    go: '#00add8',
    ruby: '#cc342d',
    kotlin: '#7f52ff',
    swift: '#f05138',
    rust: '#dea584'
  };
  
  return colors[language] || '#64748b';
};

/**
 * Format code for display
 * @param {string} code - Code to format
 * @param {string} language - Programming language
 * @returns {string} Formatted code
 */
export const formatCodeForDisplay = (code, language) => {
  if (!code) return '';
  
  // Simple formatting for common languages
  const languageFormatters = {
    javascript: (code) => code,
    java: (code) => code,
    python: (code) => code,
    default: (code) => code
  };
  
  const formatter = languageFormatters[language] || languageFormatters.default;
  return formatter(code);
};

/**
 * Get file extension for language
 * @param {string} language - Programming language
 * @returns {string} File extension
 */
export const getFileExtension = (language) => {
  const extensions = {
    java: '.java',
    javascript: '.js',
    python: '.py',
    csharp: '.cs',
    php: '.php',
    go: '.go',
    ruby: '.rb',
    kotlin: '.kt',
    swift: '.swift',
    rust: '.rs'
  };
  
  return extensions[language] || '.txt';
};

/**
 * Get default implementation options
 * @param {string} language - Programming language
 * @returns {Object} Default options
 */
export const getDefaultImplementationOptions = (language) => {
  const defaults = {
    includeTests: true,
    includeComments: true,
    includeExamples: true,
    useDtos: language === 'java',
    useRepositoryPattern: language === 'java' || language === 'csharp',
    includeValidation: true,
    includeLogging: true,
    includeErrorHandling: true,
    framework: language === 'java' ? 'spring-boot' : 
               language === 'javascript' ? 'express' :
               language === 'python' ? 'fastapi' : 'default'
  };
  
  return defaults;
};

/**
 * Cache codebase data in localStorage
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @param {Object} data - Data to cache
 * @param {number} ttlMinutes - Time to live in minutes (default: 30)
 */
export const cacheCodebaseData = (userId, cacheKey, data, ttlMinutes = 30) => {
  if (!userId || !cacheKey || !data) return;
  
  const fullCacheKey = `codebase_cache_${userId}_${cacheKey}`;
  const cacheData = {
    data: data,
    timestamp: Date.now(),
    ttl: ttlMinutes * 60 * 1000 // Convert to milliseconds
  };
  
  try {
    localStorage.setItem(fullCacheKey, JSON.stringify(cacheData));
  } catch (error) {
    console.error('Failed to cache codebase data:', error);
  }
};

/**
 * Get cached codebase data
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @returns {Object|null} Cached data or null
 */
export const getCachedCodebaseData = (userId, cacheKey) => {
  if (!userId || !cacheKey) return null;
  
  const fullCacheKey = `codebase_cache_${userId}_${cacheKey}`;
  
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
    console.error('Failed to get cached codebase data:', error);
    return null;
  }
};

/**
 * Clear cached codebase data
 * @param {string} userId - User ID (optional, clears all if not provided)
 * @param {string} cacheKey - Cache key (optional, clears all for user if not provided)
 */
export const clearCachedCodebaseData = (userId = null, cacheKey = null) => {
  try {
    if (userId && cacheKey) {
      localStorage.removeItem(`codebase_cache_${userId}_${cacheKey}`);
    } else if (userId) {
      // Clear all codebase cache for user
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith(`codebase_cache_${userId}_`)) {
          localStorage.removeItem(key);
        }
      });
    } else {
      // Clear all codebase cache
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('codebase_cache_')) {
          localStorage.removeItem(key);
        }
      });
    }
  } catch (error) {
    console.error('Failed to clear cached codebase data:', error);
  }
};

/**
 * Extract user ID from authorization header
 * @param {string} authorizationHeader - Bearer token
 * @returns {string|null} User ID or null
 */
export const extractUserIdFromToken = (authorizationHeader) => {
  if (!authorizationHeader || !authorizationHeader.startsWith('Bearer ')) {
    return null;
  }
  
  try {
    const token = authorizationHeader.replace('Bearer ', '');
    // Simple extraction - in real app, you might want to decode JWT
    // This is a placeholder - implement proper JWT decoding if needed
    const parts = token.split('.');
    if (parts.length === 3) {
      const payload = JSON.parse(atob(parts[1]));
      return payload.sub || payload.userId || payload.username || null;
    }
  } catch (error) {
    console.error('Error extracting user ID from token:', error);
  }
  
  return null;
};

// Export all functions
export default {
  // Main API methods
  getCollectionsListFromCodebase,
  getCollectionDetailsFromCodebase,
  getFolderRequestsFromCodebase,
  getRequestDetailsFromCodebase,
  getImplementationDetails,
  getAllImplementations,
  generateImplementation,
  exportImplementation,
  getLanguages,
  searchImplementations,
  importSpecification,
  validateImplementation,
  testImplementation,
  clearCodebaseCache,
  getSupportedProgrammingLanguages,
  getQuickStartGuide,
  
  // Response handlers
  handleCodebaseResponse,
  extractCodebaseCollectionsList,
  extractCodebaseCollectionDetails,
  extractFolderRequests,
  extractCodebaseRequestDetails,
  extractImplementationDetails,
  extractAllImplementations,
  extractGenerateResults,
  extractExportResults,
  extractLanguages,
  extractSearchResults,
  extractImportSpecResults,
  extractValidationResults,
  extractTestResults,
  extractSupportedProgrammingLanguages,
  extractQuickStartGuide,
  
  // Validation functions
  validateGenerateImplementation,
  validateExportImplementation,
  validateSearchImplementation,
  validateImportSpecification,
  validateImplementationValidation,
  validateTestImplementation,
  
  // Utility functions
  getDefaultSupportedProgrammingLanguages,
  getSupportedComponents,
  getDefaultQuickStartGuide,
  getLanguageColor,
  formatCodeForDisplay,
  getFileExtension,
  getDefaultImplementationOptions,
  
  // Cache functions
  cacheCodebaseData,
  getCachedCodebaseData,
  clearCachedCodebaseData,
  
  // Token utilities
  extractUserIdFromToken
};