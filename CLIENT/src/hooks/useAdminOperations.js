// src/hooks/useAdminOperations.js
import { useState, useCallback } from 'react';
import { showSuccess, showError } from '@/lib/sweetAlert';
import {
  createOperation,
  getAllOperations,
  getOperationById,
  updateOperation,
  deleteOperation,
  searchOperations,
  buildOperationPaginationParams,
  buildOperationSearchFilters,
  buildOperationDTO,
  validateOperationData,
  extractOperationPaginationInfo,
  handleOperationResponse
} from '@/controllers/OperationController';

export const useAdminOperations = () => {
  const [operations, setOperations] = useState([]);
  const [selectedServiceOperations, setSelectedServiceOperations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });

  const loadOperations = useCallback(async (authHeader, page = 0, size = 10) => {
    setLoading(true);
    setError(null);
    try {
      const paginationParams = buildOperationPaginationParams(page, size);
      const response = await getAllOperations(authHeader, paginationParams);
      const processedResponse = handleOperationResponse(response);
      const paginationInfo = extractOperationPaginationInfo(response);
      
      setOperations(processedResponse.content || []);
      setPagination({
        page: paginationInfo?.number || page,
        size: paginationInfo?.size || size,
        totalPages: paginationInfo?.totalPages || 0,
        totalElements: paginationInfo?.totalElements || 0
      });
    } catch (err) {
      setError(err.message);
      showError('Failed to load operations');
    } finally {
      setLoading(false);
    }
  }, []);

  const loadOperationsByService = useCallback(async (authHeader, serviceId, page = 0, size = 10) => {
    setLoading(true);
    setError(null);
    try {
      const filters = { integrationId: serviceId };
      const searchFilters = buildOperationSearchFilters(filters);
      const response = await searchOperations(authHeader, searchFilters, { page, size });
      const processedResponse = handleOperationResponse(response);
      const paginationInfo = extractOperationPaginationInfo(response);
      
      setSelectedServiceOperations(processedResponse.content || []);
      setPagination({
        page: paginationInfo?.number || page,
        size: paginationInfo?.size || size,
        totalPages: paginationInfo?.totalPages || 0,
        totalElements: paginationInfo?.totalElements || 0
      });
    } catch (err) {
      setError(err.message);
      showError('Failed to load service operations');
    } finally {
      setLoading(false);
    }
  }, []);

  const createOperation = useCallback(async (operationData, authHeader) => {
    try {
      const validationErrors = validateOperationData(operationData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildOperationDTO(operationData);
      const response = await createOperation(authHeader, dto);
      const processedResponse = handleOperationResponse(response);
      
      showSuccess('Operation created successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to create operation');
      return { success: false, error: err.message };
    }
  }, []);

  const updateOperation = useCallback(async (operationId, operationData, authHeader) => {
    try {
      const validationErrors = validateOperationData(operationData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildOperationDTO(operationData);
      const response = await updateOperation(authHeader, operationId, dto);
      const processedResponse = handleOperationResponse(response);
      
      showSuccess('Operation updated successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to update operation');
      return { success: false, error: err.message };
    }
  }, []);

  const deleteOperation = useCallback(async (operationId, authHeader) => {
    try {
      const response = await deleteOperation(authHeader, operationId);
      const processedResponse = handleOperationResponse(response);
      
      showSuccess('Operation deleted successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to delete operation');
      return { success: false, error: err.message };
    }
  }, []);

  return {
    operations,
    selectedServiceOperations,
    loading,
    error,
    pagination,
    loadOperations,
    loadOperationsByService,
    createOperation,
    updateOperation,
    deleteOperation
  };
};