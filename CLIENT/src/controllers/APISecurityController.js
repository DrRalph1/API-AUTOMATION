// controllers/APISecurityController.js
import { apiCall } from "@/helpers/APIHelper.js";
import { apiCallWithTokenRefresh } from "./UserManagementController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// ============ API SECURITY METHODS ============

/**
 * Get rate limit rules
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getRateLimitRules = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/rate-limit-rules`, {
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
export const getIPWhitelist = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/ip-whitelist`, {
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
export const getLoadBalancers = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/load-balancers`, {
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
export const getSecurityEvents = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/security-events`, {
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
export const getSecuritySummary = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/security-summary`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Add rate limit rule
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} addRuleRequest - Add rule request data
 * @param {string} addRuleRequest.name - Rule name (required)
 * @param {string} addRuleRequest.description - Rule description
 * @param {string} addRuleRequest.endpoint - Endpoint pattern (required)
 * @param {string} addRuleRequest.method - HTTP method (required)
 * @param {number} addRuleRequest.limit - Request limit (required)
 * @param {string} addRuleRequest.window - Time window (required)
 * @param {number} addRuleRequest.burst - Burst limit
 * @param {string} addRuleRequest.action - Action (throttle/block) (required)
 * @param {Object} addRuleRequest.options - Additional options
 * @returns {Promise} API response
 */
export const addRateLimitRule = async (authorizationHeader, addRuleRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/rate-limit-rules`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(addRuleRequest)
    })
  );
};

/**
 * Add IP whitelist entry
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} addIPEntryRequest - Add IP entry request data
 * @param {string} addIPEntryRequest.name - Entry name (required)
 * @param {string} addIPEntryRequest.ipRange - IP range/CIDR (required)
 * @param {string} addIPEntryRequest.description - Entry description
 * @param {string} addIPEntryRequest.endpoints - Protected endpoints
 * @param {Object} addIPEntryRequest.options - Additional options
 * @returns {Promise} API response
 */
export const addIPWhitelistEntry = async (authorizationHeader, addIPEntryRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/ip-whitelist`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(addIPEntryRequest)
    })
  );
};

/**
 * Add load balancer
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} addLoadBalancerRequest - Add load balancer request data
 * @param {string} addLoadBalancerRequest.name - Load balancer name (required)
 * @param {string} addLoadBalancerRequest.algorithm - Load balancing algorithm (required)
 * @param {string} addLoadBalancerRequest.healthCheck - Health check endpoint
 * @param {string} addLoadBalancerRequest.healthCheckInterval - Health check interval
 * @param {Array} addLoadBalancerRequest.servers - Server list
 * @param {Object} addLoadBalancerRequest.options - Additional options
 * @returns {Promise} API response
 */
export const addLoadBalancer = async (authorizationHeader, addLoadBalancerRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/load-balancers`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(addLoadBalancerRequest)
    })
  );
};

/**
 * Update rule status
 * @param {string} authorizationHeader - Bearer token
 * @param {string} ruleId - Rule ID
 * @param {Object} updateRequest - Update status request data
 * @param {string} updateRequest.status - New status (active/inactive) (required)
 * @returns {Promise} API response
 */
export const updateRuleStatus = async (authorizationHeader, ruleId, updateRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/rate-limit-rules/${ruleId}/status`, {
      method: 'PUT',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(updateRequest)
    })
  );
};

/**
 * Delete rule
 * @param {string} authorizationHeader - Bearer token
 * @param {string} ruleId - Rule ID
 * @returns {Promise} API response
 */
export const deleteRule = async (authorizationHeader, ruleId) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/rate-limit-rules/${ruleId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Generate security report
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} reportRequest - Generate report request data
 * @param {string} reportRequest.reportType - Report type (required)
 * @param {string} reportRequest.format - Output format (pdf/html)
 * @param {string} reportRequest.startDate - Start date (YYYY-MM-DD)
 * @param {string} reportRequest.endDate - End date (YYYY-MM-DD)
 * @param {Object} reportRequest.options - Additional options
 * @returns {Promise} API response
 */
export const generateSecurityReport = async (authorizationHeader, reportRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/reports/generate`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(reportRequest)
    })
  );
};

/**
 * Run security scan
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const runSecurityScan = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/scan`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Get security configuration
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getSecurityConfiguration = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/configuration`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Update security configuration
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} configRequest - Update configuration request data
 * @param {Object} configRequest.configuration - Configuration settings
 * @returns {Promise} API response
 */
export const updateSecurityConfiguration = async (authorizationHeader, configRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/configuration`, {
      method: 'PUT',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(configRequest)
    })
  );
};

/**
 * Get security alerts
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getSecurityAlerts = async (authorizationHeader) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/alerts`, {
      method: 'GET',
      headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
    })
  );
};

/**
 * Export security data
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} exportRequest - Export security data request
 * @param {string} exportRequest.format - Export format (json/csv/pdf) (required)
 * @param {string} exportRequest.dataType - Data type to export (rules/events/whitelist/reports/all)
 * @param {string} exportRequest.startDate - Start date (YYYY-MM-DD)
 * @param {string} exportRequest.endDate - End date (YYYY-MM-DD)
 * @param {Object} exportRequest.options - Additional options
 * @returns {Promise} API response
 */
export const exportSecurityData = async (authorizationHeader, exportRequest) => {
  return apiCallWithTokenRefresh(
    authorizationHeader,
    (authHeader) => apiCall(`/security/export`, {
      method: 'POST',
      headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
      body: JSON.stringify(exportRequest)
    })
  );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for security operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleSecurityResponse = (response) => {
  if (!response) {
    throw new Error('No response received from security service');
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
 * Extract rate limit rules from response
 * @param {Object} response - API response
 * @returns {Array} Rate limit rules
 */
export const extractRateLimitRules = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.rules && Array.isArray(data.rules)) {
    return data.rules;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract IP whitelist entries from response
 * @param {Object} response - API response
 * @returns {Array} IP whitelist entries
 */
export const extractIPWhitelist = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (data.entries && Array.isArray(data.entries)) {
    return data.entries;
  }
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract load balancers from response
 * @param {Object} response - API response
 * @returns {Array} Load balancers
 */
export const extractLoadBalancers = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (data.loadBalancers && Array.isArray(data.loadBalancers)) {
    return data.loadBalancers;
  }
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract security events from response
 * @param {Object} response - API response
 * @returns {Array} Security events
 */
export const extractSecurityEvents = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (data.events && Array.isArray(data.events)) {
    return data.events;
  }
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract security summary from response
 * @param {Object} response - API response
 * @returns {Object} Security summary
 */
export const extractSecuritySummary = (response) => {
  if (!response || !response.data) return null;
  
  const summary = response.data;
  
  return {
    totalEndpoints: summary.totalEndpoints || 0,
    securedEndpoints: summary.securedEndpoints || 0,
    vulnerableEndpoints: summary.vulnerableEndpoints || 0,
    blockedRequests: summary.blockedRequests || 0,
    throttledRequests: summary.throttledRequests || 0,
    avgResponseTime: summary.avgResponseTime || '0ms',
    securityScore: summary.securityScore || 0,
    lastScan: summary.lastScan,
    quickStats: summary.quickStats || {}
  };
};

/**
 * Extract security scan results
 * @param {Object} response - API response
 * @returns {Object} Security scan results
 */
export const extractSecurityScanResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    scanId: data.scanId,
    startedAt: data.startedAt,
    status: data.status,
    findings: data.findings || [],
    totalFindings: data.totalFindings || 0,
    criticalFindings: data.criticalFindings || 0,
    scanDuration: data.scanDuration,
    securityScore: data.securityScore || 0,
    success: data.scanId !== undefined
  };
};

/**
 * Extract security configuration
 * @param {Object} response - API response
 * @returns {Object} Security configuration
 */
export const extractSecurityConfiguration = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    configuration: data.configuration || {},
    lastUpdated: data.lastUpdated,
    success: data.configuration !== undefined
  };
};

/**
 * Extract security alerts
 * @param {Object} response - API response
 * @returns {Array} Security alerts
 */
export const extractSecurityAlerts = (response) => {
  if (!response || !response.data) return [];
  
  const data = response.data;
  
  if (data.alerts && Array.isArray(data.alerts)) {
    return data.alerts;
  }
  
  if (Array.isArray(data)) {
    return data;
  }
  
  if (data.data && Array.isArray(data.data)) {
    return data.data;
  }
  
  return [];
};

/**
 * Extract security report results
 * @param {Object} response - API response
 * @returns {Object} Security report results
 */
export const extractSecurityReportResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    reportId: data.reportId,
    generatedAt: data.generatedAt,
    status: data.status,
    summary: data.summary || {},
    recommendations: data.recommendations || [],
    downloadUrl: data.downloadUrl,
    expiresAt: data.expiresAt,
    success: data.reportId !== undefined
  };
};

/**
 * Extract export security data results
 * @param {Object} response - API response
 * @returns {Object} Export security data results
 */
export const extractExportSecurityResults = (response) => {
  if (!response || !response.data) return null;
  
  const data = response.data;
  
  return {
    exportId: data.exportId,
    format: data.format,
    status: data.status,
    exportedAt: data.exportedAt,
    exportInfo: data.exportInfo || {},
    success: data.exportId !== undefined
  };
};

// ============ VALIDATION FUNCTIONS ============

/**
 * Validate add rate limit rule request
 * @param {Object} addRuleRequest - Add rule request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateAddRateLimitRule = (addRuleRequest) => {
  const errors = [];
  
  if (!addRuleRequest.name || addRuleRequest.name.trim().length === 0) {
    errors.push('Rule name is required');
  }
  
  if (!addRuleRequest.endpoint || addRuleRequest.endpoint.trim().length === 0) {
    errors.push('Endpoint pattern is required');
  }
  
  if (!addRuleRequest.method) {
    errors.push('HTTP method is required');
  }
  
  const validMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'ALL'];
  if (addRuleRequest.method && !validMethods.includes(addRuleRequest.method.toUpperCase())) {
    errors.push(`Method must be one of: ${validMethods.join(', ')}`);
  }
  
  if (!addRuleRequest.limit || addRuleRequest.limit <= 0) {
    errors.push('Valid request limit is required');
  }
  
  if (!addRuleRequest.window || addRuleRequest.window.trim().length === 0) {
    errors.push('Time window is required');
  }
  
  if (!addRuleRequest.action) {
    errors.push('Action is required');
  }
  
  const validActions = ['throttle', 'block'];
  if (addRuleRequest.action && !validActions.includes(addRuleRequest.action.toLowerCase())) {
    errors.push(`Action must be one of: ${validActions.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate add IP whitelist entry request
 * @param {Object} addIPEntryRequest - Add IP entry request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateAddIPWhitelistEntry = (addIPEntryRequest) => {
  const errors = [];
  
  if (!addIPEntryRequest.name || addIPEntryRequest.name.trim().length === 0) {
    errors.push('Entry name is required');
  }
  
  if (!addIPEntryRequest.ipRange || addIPEntryRequest.ipRange.trim().length === 0) {
    errors.push('IP range/CIDR is required');
  }
  
  // Basic CIDR validation
  const cidrRegex = /^(\d{1,3}\.){3}\d{1,3}(\/\d{1,2})?$/;
  if (addIPEntryRequest.ipRange && !cidrRegex.test(addIPEntryRequest.ipRange)) {
    errors.push('Invalid IP range/CIDR format');
  }
  
  return errors;
};

/**
 * Validate add load balancer request
 * @param {Object} addLoadBalancerRequest - Add load balancer request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateAddLoadBalancer = (addLoadBalancerRequest) => {
  const errors = [];
  
  if (!addLoadBalancerRequest.name || addLoadBalancerRequest.name.trim().length === 0) {
    errors.push('Load balancer name is required');
  }
  
  if (!addLoadBalancerRequest.algorithm) {
    errors.push('Load balancing algorithm is required');
  }
  
  const validAlgorithms = ['round_robin', 'least_connections', 'ip_hash', 'weighted'];
  if (addLoadBalancerRequest.algorithm && !validAlgorithms.includes(addLoadBalancerRequest.algorithm)) {
    errors.push(`Algorithm must be one of: ${validAlgorithms.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate generate security report request
 * @param {Object} reportRequest - Generate report request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateGenerateSecurityReport = (reportRequest) => {
  const errors = [];
  
  if (!reportRequest.reportType) {
    errors.push('Report type is required');
  }
  
  const validFormats = ['pdf', 'html', 'json'];
  if (reportRequest.format && !validFormats.includes(reportRequest.format.toLowerCase())) {
    errors.push(`Format must be one of: ${validFormats.join(', ')}`);
  }
  
  // Date validation
  if (reportRequest.startDate && reportRequest.endDate) {
    const startDate = new Date(reportRequest.startDate);
    const endDate = new Date(reportRequest.endDate);
    
    if (startDate > endDate) {
      errors.push('Start date cannot be after end date');
    }
    
    if (endDate > new Date()) {
      errors.push('End date cannot be in the future');
    }
  }
  
  return errors;
};

/**
 * Validate update rule status request
 * @param {Object} updateRequest - Update status request data to validate
 * @returns {Array} Array of validation errors
 */
export const validateUpdateRuleStatus = (updateRequest) => {
  const errors = [];
  
  if (!updateRequest.status) {
    errors.push('Status is required');
  }
  
  const validStatuses = ['active', 'inactive'];
  if (updateRequest.status && !validStatuses.includes(updateRequest.status.toLowerCase())) {
    errors.push(`Status must be one of: ${validStatuses.join(', ')}`);
  }
  
  return errors;
};

/**
 * Validate export security data request
 * @param {Object} exportRequest - Export security data request to validate
 * @returns {Array} Array of validation errors
 */
export const validateExportSecurityData = (exportRequest) => {
  const errors = [];
  
  if (!exportRequest.format) {
    errors.push('Export format is required');
  }
  
  const validFormats = ['json', 'csv', 'pdf'];
  if (exportRequest.format && !validFormats.includes(exportRequest.format.toLowerCase())) {
    errors.push(`Format must be one of: ${validFormats.join(', ')}`);
  }
  
  const validDataTypes = ['rules', 'events', 'whitelist', 'reports', 'all'];
  if (exportRequest.dataType && !validDataTypes.includes(exportRequest.dataType.toLowerCase())) {
    errors.push(`Data type must be one of: ${validDataTypes.join(', ')}`);
  }
  
  return errors;
};

// ============ UTILITY FUNCTIONS ============

/**
 * Get security score color based on score
 * @param {number} score - Security score (0-100)
 * @returns {string} Color class
 */
export const getSecurityScoreColor = (score) => {
  if (score >= 90) return 'success';
  if (score >= 70) return 'warning';
  return 'danger';
};

/**
 * Get security score label based on score
 * @param {number} score - Security score (0-100)
 * @returns {string} Security level label
 */
export const getSecurityScoreLabel = (score) => {
  if (score >= 90) return 'Excellent';
  if (score >= 80) return 'Good';
  if (score >= 70) return 'Fair';
  if (score >= 60) return 'Poor';
  return 'Critical';
};

/**
 * Get severity color for security events
 * @param {string} severity - Severity level
 * @returns {string} Color class
 */
export const getSeverityColor = (severity) => {
  const severityMap = {
    'critical': 'danger',
    'high': 'warning',
    'medium': 'info',
    'low': 'secondary'
  };
  return severityMap[severity.toLowerCase()] || 'secondary';
};

/**
 * Format IP range for display
 * @param {string} ipRange - IP range/CIDR
 * @returns {string} Formatted IP range
 */
export const formatIPRange = (ipRange) => {
  if (!ipRange) return '';
  
  // Add network class if not present
  if (ipRange.includes('/')) {
    return ipRange;
  }
  
  // Check if it's a single IP
  if (/^(\d{1,3}\.){3}\d{1,3}$/.test(ipRange)) {
    return `${ipRange}/32`;
  }
  
  return ipRange;
};

/**
 * Calculate total blocked/throttled requests from summary
 * @param {Object} summary - Security summary
 * @returns {number} Total requests
 */
export const calculateTotalSecurityRequests = (summary) => {
  if (!summary) return 0;
  return (summary.blockedRequests || 0) + (summary.throttledRequests || 0);
};

/**
 * Get endpoint protection percentage
 * @param {Object} summary - Security summary
 * @returns {number} Protection percentage
 */
export const getEndpointProtectionPercentage = (summary) => {
  if (!summary || !summary.totalEndpoints || summary.totalEndpoints === 0) return 0;
  return Math.round((summary.securedEndpoints || 0) / summary.totalEndpoints * 100);
};

/**
 * Filter security events by severity
 * @param {Array} events - Security events
 * @param {string} severity - Severity filter
 * @returns {Array} Filtered events
 */
export const filterSecurityEventsBySeverity = (events, severity) => {
  if (!events || !Array.isArray(events)) return [];
  if (!severity || severity === 'all') return events;
  
  return events.filter(event => 
    event.severity && event.severity.toLowerCase() === severity.toLowerCase()
  );
};

/**
 * Sort security events by date
 * @param {Array} events - Security events
 * @param {string} order - Sort order (asc/desc)
 * @returns {Array} Sorted events
 */
export const sortSecurityEventsByDate = (events, order = 'desc') => {
  if (!events || !Array.isArray(events)) return [];
  
  return [...events].sort((a, b) => {
    const dateA = new Date(a.timestamp || a.createdAt || 0);
    const dateB = new Date(b.timestamp || b.createdAt || 0);
    
    return order === 'asc' ? dateA - dateB : dateB - dateA;
  });
};

/**
 * Get recent security alerts
 * @param {Array} alerts - Security alerts
 * @param {number} limit - Number of alerts to return
 * @returns {Array} Recent alerts
 */
export const getRecentSecurityAlerts = (alerts, limit = 5) => {
  if (!alerts || !Array.isArray(alerts)) return [];
  
  const sortedAlerts = sortSecurityEventsByDate(alerts, 'desc');
  return sortedAlerts.slice(0, limit);
};

/**
 * Get unread security alerts count
 * @param {Array} alerts - Security alerts
 * @returns {number} Unread alerts count
 */
export const getUnreadSecurityAlertsCount = (alerts) => {
  if (!alerts || !Array.isArray(alerts)) return 0;
  return alerts.filter(alert => !alert.read).length;
};

/**
 * Mark security alert as read
 * @param {Array} alerts - Security alerts array
 * @param {string} alertId - Alert ID to mark as read
 * @returns {Array} Updated alerts array
 */
export const markAlertAsRead = (alerts, alertId) => {
  if (!alerts || !Array.isArray(alerts)) return [];
  
  return alerts.map(alert => {
    if (alert.id === alertId) {
      return { ...alert, read: true };
    }
    return alert;
  });
};

/**
 * Mark all security alerts as read
 * @param {Array} alerts - Security alerts array
 * @returns {Array} Updated alerts array
 */
export const markAllAlertsAsRead = (alerts) => {
  if (!alerts || !Array.isArray(alerts)) return [];
  
  return alerts.map(alert => ({ ...alert, read: true }));
};

/**
 * Cache security data in localStorage
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @param {Object} data - Data to cache
 * @param {number} ttlMinutes - Time to live in minutes (default: 15)
 */
export const cacheSecurityData = (userId, cacheKey, data, ttlMinutes = 15) => {
  if (!userId || !cacheKey || !data) return;
  
  const fullCacheKey = `security_cache_${userId}_${cacheKey}`;
  const cacheData = {
    data: data,
    timestamp: Date.now(),
    ttl: ttlMinutes * 60 * 1000 // Convert to milliseconds
  };
  
  try {
    localStorage.setItem(fullCacheKey, JSON.stringify(cacheData));
  } catch (error) {
    console.error('Failed to cache security data:', error);
  }
};

/**
 * Get cached security data
 * @param {string} userId - User ID
 * @param {string} cacheKey - Cache key
 * @returns {Object|null} Cached data or null
 */
export const getCachedSecurityData = (userId, cacheKey) => {
  if (!userId || !cacheKey) return null;
  
  const fullCacheKey = `security_cache_${userId}_${cacheKey}`;
  
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
    console.error('Failed to get cached security data:', error);
    return null;
  }
};

/**
 * Clear cached security data
 * @param {string} userId - User ID (optional, clears all if not provided)
 * @param {string} cacheKey - Cache key (optional, clears all for user if not provided)
 */
export const clearCachedSecurityData = (userId = null, cacheKey = null) => {
  try {
    if (userId && cacheKey) {
      localStorage.removeItem(`security_cache_${userId}_${cacheKey}`);
    } else if (userId) {
      // Clear all security cache for user
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith(`security_cache_${userId}_`)) {
          localStorage.removeItem(key);
        }
      });
    } else {
      // Clear all security cache
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('security_cache_')) {
          localStorage.removeItem(key);
        }
      });
    }
  } catch (error) {
    console.error('Failed to clear cached security data:', error);
  }
};

/**
 * Get default security configuration
 * @returns {Object} Default security configuration
 */
export const getDefaultSecurityConfiguration = () => {
  return {
    enableRateLimiting: true,
    enableIPWhitelisting: false,
    enableDDoSProtection: true,
    enableRequestSigning: false,
    enableAPIKeyRotation: true,
    securityScanInterval: '24h',
    alertThreshold: 'high',
    autoBlockSuspiciousIPs: false,
    maxFailedAttempts: 5,
    lockoutDuration: '30m'
  };
};

/**
 * Get security event types
 * @returns {Array} Security event types
 */
export const getSecurityEventTypes = () => {
  return [
    { value: 'rate_limit_exceeded', label: 'Rate Limit Exceeded' },
    { value: 'ip_blocked', label: 'IP Blocked' },
    { value: 'suspicious_activity', label: 'Suspicious Activity' },
    { value: 'ddos_protection', label: 'DDoS Protection' },
    { value: 'unauthorized_access', label: 'Unauthorized Access' },
    { value: 'api_key_abuse', label: 'API Key Abuse' }
  ];
};

/**
 * Get load balancing algorithms
 * @returns {Array} Load balancing algorithms
 */
export const getLoadBalancingAlgorithms = () => {
  return [
    { value: 'round_robin', label: 'Round Robin' },
    { value: 'least_connections', label: 'Least Connections' },
    { value: 'ip_hash', label: 'IP Hash' },
    { value: 'weighted', label: 'Weighted' }
  ];
};

// Export all functions
export default {
  // Main API methods
  getRateLimitRules,
  getIPWhitelist,
  getLoadBalancers,
  getSecurityEvents,
  getSecuritySummary,
  addRateLimitRule,
  addIPWhitelistEntry,
  addLoadBalancer,
  updateRuleStatus,
  deleteRule,
  generateSecurityReport,
  runSecurityScan,
  getSecurityConfiguration,
  updateSecurityConfiguration,
  getSecurityAlerts,
  exportSecurityData,
  
  // Response handlers
  handleSecurityResponse,
  extractRateLimitRules,
  extractIPWhitelist,
  extractLoadBalancers,
  extractSecurityEvents,
  extractSecuritySummary,
  extractSecurityScanResults,
  extractSecurityConfiguration,
  extractSecurityAlerts,
  extractSecurityReportResults,
  extractExportSecurityResults,
  
  // Validation functions
  validateAddRateLimitRule,
  validateAddIPWhitelistEntry,
  validateAddLoadBalancer,
  validateGenerateSecurityReport,
  validateUpdateRuleStatus,
  validateExportSecurityData,
  
  // Utility functions
  getSecurityScoreColor,
  getSecurityScoreLabel,
  getSeverityColor,
  formatIPRange,
  calculateTotalSecurityRequests,
  getEndpointProtectionPercentage,
  filterSecurityEventsBySeverity,
  sortSecurityEventsByDate,
  getRecentSecurityAlerts,
  getUnreadSecurityAlertsCount,
  markAlertAsRead,
  markAllAlertsAsRead,
  getDefaultSecurityConfiguration,
  getSecurityEventTypes,
  getLoadBalancingAlgorithms,
  
  // Cache functions
  cacheSecurityData,
  getCachedSecurityData,
  clearCachedSecurityData
};