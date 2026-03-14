// controllers/DashboardController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

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

// ============ DASHBOARD METHODS ============

/**
 * Get lightweight initial dashboard data for fast initial load
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response with minimal dashboard data
 */
export const getInitialDashboard = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/initial`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get dashboard statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardStats = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/stats`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};



/**
 * Get details of a generated API by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} apiId - The ID of the generated API
 * @returns {Promise} API response
 */
export const getGeneratedApiDetails = async (authorizationHeader, apiId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/gen/api/${apiId}`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};


/**
 * Get collections with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-based, default: 0)
 * @param {number} pagination.size - Page size (default: 10)
 * @param {string} pagination.sortBy - Sort field (default: 'name')
 * @param {string} pagination.sortDir - Sort direction 'asc' or 'desc' (default: 'asc')
 * @returns {Promise} API response
 */
export const getDashboardCollections = async (authorizationHeader, pagination = {}) => {
  const { page = 0, size = 10, sortBy = 'name', sortDir = 'asc' } = pagination;
  
  const queryString = buildQueryParams({ page, size, sortBy, sortDir });
  const url = `/dashboard/collections${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get endpoints with pagination and filtering
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {number} params.page - Page number (0-based, default: 0)
 * @param {number} params.size - Page size (default: 10)
 * @param {string} params.collectionId - Filter by collection ID
 * @param {string} params.method - Filter by HTTP method
 * @param {string} params.search - Search term
 * @param {string} params.sortBy - Sort field (default: 'lastUpdated')
 * @param {string} params.sortDir - Sort direction (default: 'desc')
 * @returns {Promise} API response
 */
export const getDashboardEndpoints = async (authorizationHeader, params = {}) => {
  const { 
    page = 0, 
    size = 10, 
    collectionId, 
    method, 
    search,
    sortBy = 'lastUpdated', 
    sortDir = 'desc' 
  } = params;
  
  const queryString = buildQueryParams({ 
    page, 
    size, 
    collectionId, 
    method, 
    search,
    sortBy, 
    sortDir 
  });
  
  const url = `/dashboard/endpoints${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get recent activities with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} params - Query parameters
 * @param {number} params.page - Page number (0-based, default: 0)
 * @param {number} params.size - Page size (default: 20)
 * @param {string} params.from - Start date (ISO datetime)
 * @param {string} params.to - End date (ISO datetime)
 * @returns {Promise} API response
 */
export const getDashboardActivities = async (authorizationHeader, params = {}) => {
  const { page = 0, size = 20, from, to } = params;
  
  const queryString = buildQueryParams({ page, size, from, to });
  const url = `/dashboard/activities${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get security summary
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardSecuritySummary = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/security/summary`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get supported programming languages
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardLanguages = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/languages`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get environments overview
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardEnvironments = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/environments`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Global search across dashboard
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchParams - Search parameters
 * @param {string} searchParams.q - Search query (required)
 * @param {string} searchParams.types - Comma-separated types to search (collections,endpoints,users)
 * @param {number} searchParams.page - Page number (0-based, default: 0)
 * @param {number} searchParams.size - Page size (default: 20)
 * @returns {Promise} API response
 */
export const globalDashboardSearch = async (authorizationHeader, searchParams = {}) => {
  const { q, types, page = 0, size = 20 } = searchParams;
  
  if (!q) {
    throw new Error('Search query (q) is required');
  }
  
  const queryString = buildQueryParams({ q, types, page, size });
  const url = `/dashboard/search${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for dashboard operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleDashboardResponse = (response) => {
  if (!response) {
    throw new Error('No response received from dashboard service');
  }

  // The Java controller returns responses with responseCode field
  const responseCode = response.responseCode || response.status;
  
  if (responseCode === 200) {
    return {
      ...response,
      data: response.data || {},
      responseCode: responseCode,
      requestId: response.requestId,
      timestamp: response.timestamp
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
 * Extract paginated response data
 * @param {Object} response - API response
 * @returns {Object} Paginated data structure
 */
export const extractPaginatedData = (response) => {
  if (!response || !response.data) return null;
  
  return {
    content: response.data.content || [],
    pageNumber: response.data.pageNumber || 0,
    pageSize: response.data.pageSize || 0,
    totalElements: response.data.totalElements || 0,
    totalPages: response.data.totalPages || 0,
    last: response.data.last || true,
    requestId: response.requestId
  };
};

/**
 * Validate dashboard search parameters
 * @param {Object} searchParams - Search parameters to validate
 * @returns {Array} Array of validation errors
 */
export const validateDashboardSearchParams = (searchParams) => {
  const errors = [];
  
  if (!searchParams.q || searchParams.q.trim() === '') {
    errors.push('Search query is required');
  }
  
  if (searchParams.page !== undefined && (searchParams.page < 0 || !Number.isInteger(searchParams.page))) {
    errors.push('Page must be a non-negative integer');
  }
  
  if (searchParams.size !== undefined && (searchParams.size < 1 || searchParams.size > 100)) {
    errors.push('Size must be between 1 and 100');
  }
  
  if (searchParams.types) {
    const validTypes = ['collections', 'endpoints', 'users'];
    const types = searchParams.types.split(',');
    const invalidTypes = types.filter(type => !validTypes.includes(type));
    if (invalidTypes.length > 0) {
      errors.push(`Invalid types: ${invalidTypes.join(', ')}. Valid types: ${validTypes.join(', ')}`);
    }
  }
  
  return errors;
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
 * Refresh initial dashboard data (lightweight)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} Initial dashboard data
 */
export const refreshInitialDashboard = async (authorizationHeader) => {
  try {
    const response = await getInitialDashboard(authorizationHeader);
    return handleDashboardResponse(response);
  } catch (error) {
    throw new Error(`Failed to refresh initial dashboard: ${error.message}`);
  }
};

/**
 * Format dashboard data for display
 * @param {Object} dashboardData - Raw dashboard data
 * @returns {Object} Formatted dashboard data with date strings
 */
export const formatDashboardData = (dashboardData) => {
  if (!dashboardData) return {};
  
  const formatted = { ...dashboardData };
  
  // Format timestamps in the response
  if (formatted.timestamp) {
    formatted.formattedTimestamp = formatDateForDisplay(formatted.timestamp);
  }
  
  return formatted;
};

// Export all methods with consistent naming
export default {
  getInitialDashboard,
  getDashboardStats,
  getDashboardCollections,
  getDashboardEndpoints,
  getDashboardActivities,
  getDashboardSecuritySummary,
  getDashboardLanguages,
  getDashboardEnvironments,
  globalDashboardSearch,
  handleDashboardResponse,
  extractPaginatedData,
  validateDashboardSearchParams,
  formatDateForDisplay,
  refreshInitialDashboard,
  formatDashboardData
};