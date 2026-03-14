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
 * Get lightweight dashboard data for initial load
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response with minimal dashboard data
 */
export const getLightweightDashboard = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/lightweight`, {
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
 * Get dashboard collections
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardCollections = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/collections`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get dashboard endpoints
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardEndpoints = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/endpoints`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get rate limit rules
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardRateLimitRules = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/rate-limit-rules`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get IP whitelist
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardIpWhitelist = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/ip-whitelist`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get load balancers
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardLoadBalancers = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/load-balancers`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get security events
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardSecurityEvents = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/security-events`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get security alerts
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardSecurityAlerts = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/security-alerts`, {
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
    (authHeader) => apiCall(`/dashboard/security-summary`, {
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
 * Get code implementations
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (default: 1)
 * @param {number} pagination.size - Page size (default: 10)
 * @returns {Promise} API response
 */
export const getDashboardImplementations = async (authorizationHeader, pagination = {}) => {
  const { page = 1, size = 10 } = pagination;
  
  const queryString = buildQueryParams({ page, size });
  const url = `/dashboard/implementations${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get code generation summary
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardCodeGenerationSummary = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/code-generation-summary`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get documentation overview
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardDocumentation = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/documentation`, {
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
 * Get users overview
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (default: 1)
 * @param {number} pagination.size - Page size (default: 10)
 * @returns {Promise} API response
 */
export const getDashboardUsers = async (authorizationHeader, pagination = {}) => {
  const { page = 1, size = 10 } = pagination;
  
  const queryString = buildQueryParams({ page, size });
  const url = `/dashboard/users${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get user activities
 * @param {string} authorizationHeader - Bearer token
 * @param {number} limit - Number of activities to return (default: 10)
 * @returns {Promise} API response
 */
export const getDashboardUserActivities = async (authorizationHeader, limit = 10) => {
  const queryString = buildQueryParams({ limit });
  const url = `/dashboard/user-activities${queryString ? `?${queryString}` : ''}`;
  
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(url, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get comprehensive dashboard data
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response with all dashboard data
 */
export const getComprehensiveDashboard = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/comprehensive`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Search across dashboard
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchParams - Search parameters
 * @param {string} searchParams.query - Search query (required)
 * @param {string} searchParams.type - Type to search (all, collections, endpoints, users)
 * @param {number} searchParams.limit - Maximum results (default: 10)
 * @returns {Promise} API response
 */
export const searchDashboard = async (authorizationHeader, searchParams = {}) => {
  const { query, type, limit = 10 } = searchParams;
  
  if (!query) {
    throw new Error('Search query is required');
  }
  
  const queryString = buildQueryParams({ query, type, limit });
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
 * Extract dashboard statistics from response
 * @param {Object} response - API response
 * @returns {Object} Dashboard statistics
 */
export const extractDashboardStats = (response) => {
  if (!response || !response.data) return null;
  return response.data;
};

/**
 * Extract comprehensive dashboard data
 * @param {Object} response - API response
 * @returns {Object} Comprehensive dashboard data structure
 */
export const extractComprehensiveDashboard = (response) => {
  if (!response || !response.data) return null;
  
  return {
    ...response.data,
    lastUpdated: response.data.lastUpdated || new Date().toISOString(),
    generatedFor: response.data.generatedFor || 'Unknown',
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
  
  if (!searchParams.query || searchParams.query.trim() === '') {
    errors.push('Search query is required');
  }
  
  if (searchParams.limit !== undefined && (searchParams.limit < 1 || searchParams.limit > 100)) {
    errors.push('Limit must be between 1 and 100');
  }
  
  if (searchParams.type && !['all', 'collections', 'endpoints', 'users'].includes(searchParams.type)) {
    errors.push('Type must be one of: all, collections, endpoints, users');
  }
  
  return errors;
};

/**
 * Build pagination parameters
 * @param {number} page - Page number (default: 1)
 * @param {number} size - Page size (default: 10)
 * @returns {Object} Pagination parameters
 */
export const buildPaginationParams = (page = 1, size = 10) => ({
  page,
  size
});

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
 * Refresh all dashboard data (comprehensive)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} Combined dashboard data
 */
export const refreshDashboard = async (authorizationHeader) => {
  try {
    const comprehensiveResponse = await getComprehensiveDashboard(authorizationHeader);
    return handleDashboardResponse(comprehensiveResponse);
  } catch (error) {
    throw new Error(`Failed to refresh dashboard: ${error.message}`);
  }
};

// Keep these utility functions but update names to match backend:
export const buildDashboardSearchDTO = (criteria = {}) => {
  return {
    query: criteria.query || '',
    type: criteria.type || 'all',
    limit: criteria.limit || 10
  };
};

/**
 * Format dashboard data for display
 * @param {Object} dashboardData - Raw dashboard data
 * @returns {Object} Formatted dashboard data
 */
export const formatDashboardData = (dashboardData) => {
  if (!dashboardData) return {};
  
  const formatted = { ...dashboardData };
  
  // Format dates for display
  if (formatted.lastUpdated) {
    formatted.formattedLastUpdated = formatDateForDisplay(formatted.lastUpdated);
  }
  
  return formatted;
};

/**
 * Export dashboard data for reporting
 * @param {string} authorizationHeader - Bearer token
 * @param {string} format - Export format ('json' or 'csv')
 * @returns {Promise} Export data
 */
export const exportDashboardData = async (authorizationHeader, format = 'json') => {
  const dashboardData = await refreshDashboard(authorizationHeader);
  
  if (format.toLowerCase() === 'csv') {
    const csvData = convertDashboardToCSV(dashboardData.data);
    return {
      ...dashboardData,
      format: 'csv',
      csvData: csvData
    };
  }
  
  return {
    ...dashboardData,
    format: 'json'
  };
};

/**
 * Convert dashboard data to CSV format
 * @param {Object} dashboardData - Dashboard data
 * @returns {string} CSV string
 */
const convertDashboardToCSV = (dashboardData) => {
  if (!dashboardData) return '';
  
  const rows = [];
  rows.push(['Section', 'Metric', 'Value', 'Timestamp'].join(','));
  
  // Helper to flatten object
  const flattenObject = (obj, prefix = '') => {
    Object.keys(obj).forEach(key => {
      if (obj[key] && typeof obj[key] === 'object' && !Array.isArray(obj[key])) {
        flattenObject(obj[key], `${prefix}${key}.`);
      } else if (Array.isArray(obj[key])) {
        rows.push([prefix.slice(0, -1), key, `Array(${obj[key].length})`, new Date().toISOString()]);
      } else {
        rows.push([prefix.slice(0, -1), key, obj[key], new Date().toISOString()]);
      }
    });
  };
  
  flattenObject(dashboardData);
  return rows.join('\n');
};