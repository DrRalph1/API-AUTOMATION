// hooks/useAuditLogger.js
import { useCallback } from 'react';
import { useAuth } from '@/context/AuthContext';
import { 
  logAction,
  getAllAuditLogs,
  searchAuditLogs,
  searchAuditLogsByQuery,
  advancedAuditLogSearch,
  createStandardAuditLog,
  createLoginAuditLog,
  createLogoutAuditLog,
  getClientIpAddress,
  validateSearchCriteria,
  buildSearchCriteria,
  buildAuditPaginationParams,
  formatDateForDisplay
} from '@/controllers/AuditController';

export const useAuditLogger = () => {
  const { user, token } = useAuth();

  // ============ LOGGING METHODS ============

  const logActionWithUser = useCallback(async (action, operation, entityId, entityType, options = {}) => {
    try {
      const authHeader = token || '';
      
      // Get current user ID
      const userId = user?.userId || options.userId || 'SYSTEM';
      
      // Get client IP address
      const ipAddress = await getClientIpAddress();
      
      // Create audit log with backend DTO structure
      const auditLog = createStandardAuditLog(
        userId,
        action,
        operation,
        entityId,
        entityType,
        {
          ...options,
          ipAddress
        }
      );
      
      const response = await logAction(authHeader, auditLog);
      
      return { success: true, data: response };
    } catch (error) {
      console.error('Failed to log audit action:', error);
      return { success: false, error: error.message };
    }
  }, [user, token]);

  const logUserLogin = useCallback(async (userId, status = 'SUCCESS') => {
    try {
      const authHeader = token || '';
      const ipAddress = await getClientIpAddress();
      
      const loginLog = createLoginAuditLog(userId, ipAddress, status);
      const response = await logAction(authHeader, loginLog);
      
      return { success: true, data: response };
    } catch (error) {
      console.error('Failed to log login:', error);
      return { success: false, error: error.message };
    }
  }, [token]);

  const logUserLogout = useCallback(async (userId) => {
    try {
      const authHeader = token || '';
      
      const logoutLog = createLogoutAuditLog(userId);
      const response = await logAction(authHeader, logoutLog);
      
      return { success: true, data: response };
    } catch (error) {
      console.error('Failed to log logout:', error);
      return { success: false, error: error.message };
    }
  }, [token]);

  const logTransaction = useCallback(async (transactionId, action, data = {}) => {
    const userId = user?.userId || data.userId || 'SYSTEM';
    
    return logActionWithUser(
      action,
      'TRANSACTION_API',
      transactionId,
      'TRANSACTION',
      { 
        description: `Transaction ${action.toLowerCase()} operation`,
        ...data,
        userId
      }
    );
  }, [logActionWithUser, user]);

  const logServiceUsage = useCallback(async (serviceId, action) => {
    const userId = user?.userId || 'SYSTEM';
    
    return logActionWithUser(
      action,
      'SERVICE_API',
      serviceId,
      'SERVICE',
      { 
        description: `Service ${action.toLowerCase()} operation`,
        userId
      }
    );
  }, [logActionWithUser, user]);

  const logFormSubmission = useCallback(async (formId, operationId, data) => {
    const userId = user?.userId || data?.userId || 'SYSTEM';
    
    return logActionWithUser(
      'SUBMIT',
      'FORM_API',
      formId,
      'FORM',
      { 
        description: `Form submitted for operation ${operationId}`,
        metadata: { operationId, ...data },
        userId
      }
    );
  }, [logActionWithUser, user]);

  const logBackendAudit = useCallback(async (auditData) => {
    try {
      const authHeader = token || '';
      
      // Validate required fields
      if (!auditData.userId || !auditData.action || !auditData.operation) {
        throw new Error('Missing required audit fields: userId, action, operation');
      }
      
      const response = await logAction(authHeader, auditData);
      return { success: true, data: response };
    } catch (error) {
      console.error('Failed to log backend audit:', error);
      return { success: false, error: error.message };
    }
  }, [token]);

  // ============ SEARCH METHODS ============

  const getLogs = useCallback(async (pagination = {}) => {
    try {
      const authHeader = token || '';
      const response = await getAllAuditLogs(authHeader, pagination);
      return { success: true, data: response };
    } catch (error) {
      console.error('Failed to get audit logs:', error);
      return { success: false, error: error.message };
    }
  }, [token]);

  const searchLogs = useCallback(async (searchCriteria = {}, pagination = {}, usePostMethod = true) => {
    try {
      const authHeader = token || '';
      
      // Validate search criteria
      const validationErrors = validateSearchCriteria(searchCriteria);
      if (validationErrors.length > 0) {
        return {
          success: false,
          error: 'Invalid search criteria',
          validationErrors
        };
      }
      
      let response;
      if (usePostMethod) {
        // Use POST for complex searches
        response = await searchAuditLogs(authHeader, searchCriteria, pagination);
      } else {
        // Use GET for simple searches
        response = await searchAuditLogsByQuery(authHeader, searchCriteria, pagination);
      }
      
      return { success: true, data: response };
    } catch (error) {
      console.error('Failed to search audit logs:', error);
      return { success: false, error: error.message };
    }
  }, [token]);

  const advancedSearch = useCallback(async (options = {}) => {
    try {
      const authHeader = token || '';
      const response = await advancedAuditLogSearch(authHeader, options);
      return { success: true, data: response };
    } catch (error) {
      console.error('Failed to perform advanced audit log search:', error);
      return { success: false, error: error.message };
    }
  }, [token]);

  // ============ HELPER METHODS ============

  const validateAuditData = useCallback((auditData) => {
    // Basic validation for audit data
    const errors = [];
    
    if (!auditData.userId) errors.push('User ID is required');
    if (!auditData.action || auditData.action.trim() === '') errors.push('Action is required');
    if (!auditData.operation || auditData.operation.trim() === '') errors.push('Operation is required');
    
    return errors;
  }, []);

  const createSearchParams = useCallback((criteria = {}) => {
    return buildSearchCriteria(criteria);
  }, []);

  const createPaginationParams = useCallback((page = 0, size = 10, sort = 'createdAt', direction = 'DESC') => {
    return buildAuditPaginationParams(page, size, sort, direction);
  }, []);

  const formatAuditDate = useCallback((date) => {
    return formatDateForDisplay(date);
  }, []);

  // ============ COMMON SEARCH PATTERNS ============

  const searchUserActions = useCallback(async (userId, startDate, endDate, pagination = {}) => {
    return searchLogs(
      {
        userId,
        startDate,
        endDate
      },
      {
        page: pagination.page || 0,
        size: pagination.size || 10,
        sort: 'createdAt',
        direction: 'DESC'
      },
      false // Use GET method for simple searches
    );
  }, [searchLogs]);

  const searchByActionAndOperation = useCallback(async (action, operation, startDate, endDate, pagination = {}) => {
    return searchLogs(
      {
        action,
        operation,
        startDate,
        endDate
      },
      {
        page: pagination.page || 0,
        size: pagination.size || 10,
        sort: 'createdAt',
        direction: 'DESC'
      },
      true // Use POST method for multiple criteria
    );
  }, [searchLogs]);

  const searchByDetails = useCallback(async (searchText, startDate, endDate, pagination = {}) => {
    return searchLogs(
      {
        details: searchText,
        startDate,
        endDate
      },
      {
        page: pagination.page || 0,
        size: pagination.size || 10,
        sort: 'createdAt',
        direction: 'DESC'
      },
      true // Use POST method for text search
    );
  }, [searchLogs]);

  // ============ QUICK LOGGING METHODS ============

  const logError = useCallback(async (operation, error, entityId = '', entityType = 'ERROR') => {
    const userId = user?.userId || 'SYSTEM';
    
    return logActionWithUser(
      'ERROR',
      operation,
      entityId,
      entityType,
      {
        description: error.message || 'An error occurred',
        status: 'FAILED',
        metadata: {
          error: error.toString(),
          stack: error.stack
        }
      }
    );
  }, [logActionWithUser, user]);

  const logAccess = useCallback(async (resource, action, details = '') => {
    const userId = user?.userId || 'SYSTEM';
    
    return logActionWithUser(
      action,
      'ACCESS_CONTROL',
      resource,
      'RESOURCE',
      {
        description: details || `${action} access to ${resource}`,
        status: 'SUCCESS'
      }
    );
  }, [logActionWithUser, user]);

  const logDataExport = useCallback(async (exportType, filters = {}, recordCount = 0) => {
    const userId = user?.userId || 'SYSTEM';
    
    return logActionWithUser(
      'EXPORT',
      'DATA_MANAGEMENT',
      exportType,
      'REPORT',
      {
        description: `Exported ${recordCount} records as ${exportType}`,
        metadata: {
          filters,
          recordCount
        }
      }
    );
  }, [logActionWithUser, user]);

  const logDataImport = useCallback(async (importType, recordCount = 0, status = 'SUCCESS') => {
    const userId = user?.userId || 'SYSTEM';
    
    return logActionWithUser(
      'IMPORT',
      'DATA_MANAGEMENT',
      importType,
      'DATA',
      {
        description: `Imported ${recordCount} records as ${importType} - ${status}`,
        status
      }
    );
  }, [logActionWithUser, user]);

  return {
    // Core logging methods
    logAction: logActionWithUser,
    logBackendAudit,
    
    // Specific logging methods
    logUserLogin,
    logUserLogout,
    logTransaction,
    logServiceUsage,
    logFormSubmission,
    
    // Quick logging helpers
    logError,
    logAccess,
    logDataExport,
    logDataImport,
    
    // Search methods
    getLogs,
    searchLogs,
    advancedSearch,
    
    // Common search patterns
    searchUserActions,
    searchByActionAndOperation,
    searchByDetails,
    
    // Helper methods
    validateAuditData,
    validateSearchCriteria: useCallback((criteria) => validateSearchCriteria(criteria), []),
    createSearchParams,
    createPaginationParams,
    formatAuditDate,
    
    // Utility constants
    ACTIONS: {
      CREATE: 'CREATE',
      READ: 'READ',
      UPDATE: 'UPDATE',
      DELETE: 'DELETE',
      LOGIN: 'LOGIN',
      LOGOUT: 'LOGOUT',
      EXPORT: 'EXPORT',
      IMPORT: 'IMPORT',
      APPROVE: 'APPROVE',
      REJECT: 'REJECT',
      ERROR: 'ERROR',
      SUBMIT: 'SUBMIT'
    },
    
    OPERATIONS: {
      AUTHENTICATION: 'AUTHENTICATION',
      TRANSACTION_API: 'TRANSACTION_API',
      SERVICE_API: 'SERVICE_API',
      FORM_API: 'FORM_API',
      ACCESS_CONTROL: 'ACCESS_CONTROL',
      DATA_MANAGEMENT: 'DATA_MANAGEMENT',
      USER_MANAGEMENT: 'USER_MANAGEMENT'
    }
  };
};