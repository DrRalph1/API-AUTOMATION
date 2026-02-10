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
 * Get dashboard connections
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardConnections = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/connections`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get dashboard APIs
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardApis = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/apis`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get dashboard activities
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (default: 1)
 * @param {number} pagination.size - Page size (default: 10)
 * @returns {Promise} API response
 */
export const getDashboardActivities = async (authorizationHeader, pagination = {}) => {
  const { page = 1, size = 10 } = pagination;
  
  const queryString = buildQueryParams({ page, size });
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
 * Get dashboard schema statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardSchemaStats = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/schema-stats`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get code generation statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getCodeGenerationStats = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/code-generation-stats`, {
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
 * Search dashboard activities with filters
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} searchDTO - Search criteria
 * @param {string} searchDTO.userId - Filter by user ID
 * @param {string} searchDTO.action - Filter by action type
 * @param {string} searchDTO.operation - Filter by operation
 * @param {string} searchDTO.startDate - Start date filter
 * @param {string} searchDTO.endDate - End date filter
 * @param {number} searchDTO.page - Page number (default: 1)
 * @param {number} searchDTO.size - Page size (default: 10)
 * @returns {Promise} API response
 */
export const searchDashboardActivities = async (authorizationHeader, searchDTO = {}) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/activities/search`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(searchDTO)
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
  
  const data = response.data;
  return {
    stats: data.stats || {},
    connections: data.connections || {},
    apis: data.apis || {},
    recentActivities: data.recentActivities || {},
    schemaStats: data.schemaStats || {},
    systemHealth: data.systemHealth || {},
    codeGenerationStats: data.codeGenerationStats || {},
    lastUpdated: data.lastUpdated || new Date().toISOString(),
    generatedFor: data.generatedFor || 'Unknown'
  };
};

/**
 * Validate dashboard search criteria
 * @param {Object} searchCriteria - Search criteria to validate
 * @returns {Array} Array of validation errors
 */
export const validateDashboardSearchCriteria = (searchCriteria) => {
  const errors = [];
  
  // Validate date range
  if (searchCriteria.startDate && searchCriteria.endDate) {
    const start = new Date(searchCriteria.startDate);
    const end = new Date(searchCriteria.endDate);
    
    if (start > end) {
      errors.push('Start date cannot be after end date');
    }
  }
  
  // Validate page and size
  if (searchCriteria.page !== undefined && searchCriteria.page < 1) {
    errors.push('Page number must be at least 1');
  }
  
  if (searchCriteria.size !== undefined && (searchCriteria.size < 1 || searchCriteria.size > 100)) {
    errors.push('Page size must be between 1 and 100');
  }
  
  return errors;
};

/**
 * Build dashboard search DTO
 * @param {Object} criteria - Search criteria
 * @returns {Object} Search DTO
 */
export const buildDashboardSearchDTO = (criteria = {}) => {
  return {
    userId: criteria.userId || '',
    action: criteria.action || '',
    operation: criteria.operation || '',
    startDate: criteria.startDate || '',
    endDate: criteria.endDate || '',
    page: criteria.page || 1,
    size: criteria.size || 10
  };
};

/**
 * Build pagination parameters for dashboard activities
 * @param {number} page - Page number (default: 1)
 * @param {number} size - Page size (default: 10)
 * @returns {Object} Pagination parameters
 */
export const buildDashboardPaginationParams = (page = 1, size = 10) => ({
  page,
  size
});

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
  
  // Add human-readable metrics if needed
  if (formatted.stats && typeof formatted.stats.totalConnections === 'number') {
    formatted.stats.totalConnectionsFormatted = formatted.stats.totalConnections.toLocaleString();
  }
  
  if (formatted.stats && typeof formatted.stats.totalApis === 'number') {
    formatted.stats.totalApisFormatted = formatted.stats.totalApis.toLocaleString();
  }
  
  if (formatted.stats && typeof formatted.stats.totalUsers === 'number') {
    formatted.stats.totalUsersFormatted = formatted.stats.totalUsers.toLocaleString();
  }
  
  return formatted;
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
 * Get dashboard system health (if endpoint available)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getDashboardSystemHealth = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/dashboard/system-health`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  ).catch(() => {
    // If endpoint doesn't exist, return default health
    return {
      responseCode: 200,
      data: {
        status: 'UP',
        uptime: 'Unknown',
        database: 'Connected',
        memoryUsage: 'Normal',
        lastCheck: new Date().toISOString()
      }
    };
  });
};

/**
 * Refresh all dashboard data (comprehensive)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} Combined dashboard data
 */
export const refreshDashboard = async (authorizationHeader) => {
  try {
    // Try to get comprehensive data first
    const comprehensiveResponse = await getComprehensiveDashboard(authorizationHeader);
    const comprehensiveData = handleDashboardResponse(comprehensiveResponse);
    
    if (comprehensiveData && comprehensiveData.data) {
      return comprehensiveData;
    }
    
    // If comprehensive fails, fetch individual components
    const [
      statsResponse,
      connectionsResponse,
      apisResponse,
      activitiesResponse
    ] = await Promise.all([
      getDashboardStats(authorizationHeader),
      getDashboardConnections(authorizationHeader),
      getDashboardApis(authorizationHeader),
      getDashboardActivities(authorizationHeader, { page: 1, size: 5 })
    ]);
    
    const stats = handleDashboardResponse(statsResponse);
    const connections = handleDashboardResponse(connectionsResponse);
    const apis = handleDashboardResponse(apisResponse);
    const activities = handleDashboardResponse(activitiesResponse);
    
    return {
      responseCode: 200,
      message: 'Dashboard data refreshed successfully',
      data: {
        stats: stats.data,
        connections: connections.data,
        apis: apis.data,
        recentActivities: activities.data,
        lastUpdated: new Date().toISOString()
      },
      requestId: `refresh-${Date.now()}`
    };
    
  } catch (error) {
    throw new Error(`Failed to refresh dashboard: ${error.message}`);
  }
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
    // Convert to CSV format
    const csvData = convertDashboardToCSV(dashboardData.data);
    return {
      ...dashboardData,
      format: 'csv',
      csvData: csvData
    };
  }
  
  // Default to JSON
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
  
  // Add header
  rows.push(['Metric', 'Value', 'Timestamp'].join(','));
  
  // Add stats
  if (dashboardData.stats) {
    Object.keys(dashboardData.stats).forEach(key => {
      if (typeof dashboardData.stats[key] === 'number' || typeof dashboardData.stats[key] === 'string') {
        rows.push([`stats.${key}`, dashboardData.stats[key], dashboardData.lastUpdated || ''].join(','));
      }
    });
  }
  
  return rows.join('\n');
};

/**
 * Monitor dashboard metrics over time
 * @param {string} authorizationHeader - Bearer token
 * @param {number} intervalMinutes - Monitoring interval in minutes
 * @param {Function} callback - Callback function for metrics updates
 * @returns {Function} Function to stop monitoring
 */
export const monitorDashboardMetrics = (authorizationHeader, intervalMinutes = 5, callback) => {
  let isMonitoring = true;
  
  const monitor = async () => {
    while (isMonitoring) {
      try {
        const dashboardData = await refreshDashboard(authorizationHeader);
        if (callback) {
          callback(dashboardData);
        }
      } catch (error) {
        console.error('Dashboard monitoring error:', error);
      }
      
      // Wait for the specified interval
      await new Promise(resolve => 
        setTimeout(resolve, intervalMinutes * 60 * 1000)
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