import { useState, useCallback } from 'react';
import { useAuth } from '@/context/AuthContext';
import {
  createFormField,
  updateFormField,
  deleteFormField,
  getFormFieldsByOperationId,
  validateFormFieldData
} from '@/controllers/FormFieldController';
import {
  createOperation,
  updateOperation,
  deleteOperation,
  getOperations,
  validateOperationData
} from '@/controllers/OperationController';
import { tokenAtom } from '@/recoil/tokenAtom';
import { useSetRecoilState, useRecoilValue } from 'recoil';

export const useConfiguration = () => {
  const { getAuthHeader } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const token = useRecoilValue(tokenAtom);

  // Service Configuration
  const manageService = useCallback(async (action, serviceData, serviceId = null) => {
    try {
      setLoading(true);
      setError(null);
      
      const authHeader = token;
      const validationErrors = validateServiceUIConfigData(serviceData);
      
      if (validationErrors.length > 0) {
        // throw new Error(`Validation failed: ${validationErrors.join(', ')}`);
      }
      
      let response;
      switch (action) {
        case 'create':
          response = await createServiceUIConfig(authHeader, serviceData);
          break;
        case 'update':
          if (!serviceId) throw new Error('Service ID required for update');
          response = await updateServiceUIConfig(authHeader, serviceId, serviceData);
          break;
        case 'delete':
          if (!serviceId) throw new Error('Service ID required for delete');
          response = await deleteServiceUIConfig(authHeader, serviceId);
          break;
        default:
          throw new Error('Invalid action');
      }
      
      return { success: true, data: response.data };
    } catch (err) {
      setError(err.message);
      return { success: false, error: err.message };
    } finally {
      setLoading(false);
    }
  }, [getAuthHeader]);

  // Tab Configuration
  const manageTab = useCallback(async (action, tabData, tabId = null) => {
    try {
      setLoading(true);
      setError(null);
      
      const authHeader = token;
      const validationErrors = validateTabData(tabData);
      
      if (validationErrors.length > 0) {
        // throw new Error(`Validation failed: ${validationErrors.join(', ')}`);
      }
      
      let response;
      switch (action) {
        case 'create':
          response = await createTab(authHeader, tabData);
          break;
        case 'update':
          if (!tabId) throw new Error('Tab ID required for update');
          response = await updateTab(authHeader, tabId, tabData);
          break;
        case 'delete':
          if (!tabId) throw new Error('Tab ID required for delete');
          response = await deleteTab(authHeader, tabId);
          break;
        default:
          throw new Error('Invalid action');
      }
      
      return { success: true, data: response.data };
    } catch (err) {
      setError(err.message);
      return { success: false, error: err.message };
    } finally {
      setLoading(false);
    }
  }, [getAuthHeader]);

  // Form Field Configuration
  const manageFormField = useCallback(async (action, fieldData, fieldId = null) => {
    try {
      setLoading(true);
      setError(null);
      
      const authHeader = token;
      const validationErrors = validateFormFieldData(fieldData);
      
      if (validationErrors.length > 0) {
        // throw new Error(`Validation failed: ${validationErrors.join(', ')}`);
      }
      
      let response;
      switch (action) {
        case 'create':
          response = await createFormField(authHeader, fieldData);
          break;
        case 'update':
          if (!fieldId) throw new Error('Field ID required for update');
          response = await updateFormField(authHeader, fieldId, fieldData);
          break;
        case 'delete':
          if (!fieldId) throw new Error('Field ID required for delete');
          response = await deleteFormField(authHeader, fieldId);
          break;
        default:
          throw new Error('Invalid action');
      }
      
      return { success: true, data: response.data };
    } catch (err) {
      setError(err.message);
      return { success: false, error: err.message };
    } finally {
      setLoading(false);
    }
  }, [getAuthHeader]);

  // Operation Configuration
  const manageOperation = useCallback(async (action, operationData, operationId = null) => {
    try {
      setLoading(true);
      setError(null);
      
      const authHeader = token;
      const validationErrors = validateOperationData(operationData);
      
      if (validationErrors.length > 0) {
        // throw new Error(`Validation failed: ${validationErrors.join(', ')}`);
      }
      
      let response;
      switch (action) {
        case 'create':
          response = await createOperation(authHeader, operationData);
          break;
        case 'update':
          if (!operationId) throw new Error('Operation ID required for update');
          response = await updateOperation(authHeader, operationId, operationData);
          break;
        case 'delete':
          if (!operationId) throw new Error('Operation ID required for delete');
          response = await deleteOperation(authHeader, operationId);
          break;
        default:
          throw new Error('Invalid action');
      }
      
      return { success: true, data: response.data };
    } catch (err) {
      setError(err.message);
      return { success: false, error: err.message };
    } finally {
      setLoading(false);
    }
  }, [getAuthHeader]);

  // Load all configurations
  const loadAllConfigurations = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const authHeader = token;
      
      const [services, tabs, operations] = await Promise.all([
        getServiceUIConfigs(authHeader),
        getTabs(authHeader),
        getOperations(authHeader)
      ]);
      
      return {
        success: true,
        data: {
          services: services.data || [],
          tabs: tabs.data || [],
          operations: operations.data || []
        }
      };
    } catch (err) {
      setError(err.message);
      return { success: false, error: err.message };
    } finally {
      setLoading(false);
    }
  }, [getAuthHeader]);

  return {
    loading,
    error,
    manageService,
    manageTab,
    manageFormField,
    manageOperation,
    loadAllConfigurations,
    resetError: () => setError(null)
  };
};