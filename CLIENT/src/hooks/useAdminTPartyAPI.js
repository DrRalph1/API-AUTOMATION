// src/hooks/useAdmincreateTPartyAPI.js
import { useState, useCallback } from 'react';
import { showSuccess, showError } from '@/lib/sweetAlert';
import {
  createcreateTPartyAPI,
  getTPartyAPIByOperationId,
  updatecreateTPartyAPI,
  deletecreateTPartyAPI,
  testEndpoint,
  buildPaginationParams,
  buildSearchFilters,
  buildTPartyAPIDTO,
  validateTPartyAPIData,
  extractEndpointPaginationInfo,
  handleEndpointResponse
} from '@/controllers/TPartyAPIConfigController';

export const useAdmincreateTPartyAPI = () => {
  const [createTPartyAPI, setcreateTPartyAPI] = useState([]);
  const [selectedOperationEndpoints, setSelectedOperationEndpoints] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });

  const loadcreateTPartyAPI = useCallback(async (authHeader, operationId = null, page = 0, size = 10) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      
      if (operationId) {
        const paginationParams = buildPaginationParams(page, size);
        response = await getTPartyAPIByOperationId(authHeader, operationId);
        const processedResponse = handleEndpointResponse(response);
        
        // Since getTPartyAPIByOperationId might not be paginated, handle both cases
        const endpoints = Array.isArray(processedResponse) ? processedResponse : [processedResponse];
        setSelectedOperationEndpoints(endpoints);
        
        setPagination({
          page: 0,
          size: endpoints.length,
          totalPages: 1,
          totalElements: endpoints.length
        });
      } else {
        // If no operationId, clear the selected operation endpoints
        setSelectedOperationEndpoints([]);
      }
    } catch (err) {
      setError(err.message);
      showError('Failed to load API endpoints');
    } finally {
      setLoading(false);
    }
  }, []);

  const createcreateTPartyAPI = useCallback(async (endpointData, authHeader) => {
    try {
      const validationErrors = validateTPartyAPIData(endpointData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildcreateTPartyAPIDTO(endpointData);
      const response = await createcreateTPartyAPI(authHeader, dto);
      const processedResponse = handleEndpointResponse(response);
      
      showSuccess('API endpoint created successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to create API endpoint');
      return { success: false, error: err.message };
    }
  }, []);

  const updatecreateTPartyAPI = useCallback(async (endpointId, endpointData, authHeader) => {
    try {
      const validationErrors = validateTPartyAPIData(endpointData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildcreateTPartyAPIDTO(endpointData);
      const response = await updatecreateTPartyAPI(authHeader, endpointId, dto);
      const processedResponse = handleEndpointResponse(response);
      
      showSuccess('API endpoint updated successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to update API endpoint');
      return { success: false, error: err.message };
    }
  }, []);

  const deletecreateTPartyAPI = useCallback(async (endpointId, authHeader) => {
    try {
      const response = await deletecreateTPartyAPI(authHeader, endpointId);
      const processedResponse = handleEndpointResponse(response);
      
      showSuccess('API endpoint deleted successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to delete API endpoint');
      return { success: false, error: err.message };
    }
  }, []);

  const testEndpoint = useCallback(async (endpointId, authHeader) => {
    try {
      const response = await testEndpoint(authHeader, endpointId);
      const processedResponse = handleEndpointResponse(response);
      
      if (processedResponse.success) {
        showSuccess('API endpoint test successful');
      } else {
        showError('API endpoint test failed');
      }
      
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to test API endpoint');
      return { success: false, error: err.message };
    }
  }, []);

  return {
    createTPartyAPI,
    selectedOperationEndpoints,
    loading,
    error,
    pagination,
    loadcreateTPartyAPI,
    createcreateTPartyAPI,
    updatecreateTPartyAPI,
    deletecreateTPartyAPI,
    testEndpoint
  };
};