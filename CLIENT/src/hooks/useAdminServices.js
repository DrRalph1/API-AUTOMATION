// src/hooks/useAdminServices.js
import { useState, useCallback } from 'react';
import { showSuccess, showError } from '@/lib/sweetAlert';
import { 
  createIntegration,
  getAllIntegrations,
  getIntegrationById,
  updateIntegration,
  deleteIntegration,
  searchIntegrations,
  buildIntegrationPaginationParams,
  buildIntegrationSearchFilters,
  buildIntegrationDTO,
  validateIntegrationData,
  extractIntegrationPaginationInfo,
  handleIntegrationResponse
} from '@/controllers/IntegrationController';

export const useAdminServices = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalPages: 0,
    totalElements: 0
  });

  const loadServices = useCallback(async (authHeader, page = 0, size = 1000) => {
    setLoading(true);
    setError(null);
    try {
      const paginationParams = buildIntegrationPaginationParams(page, size);
      const response = await getAllIntegrations(authHeader, paginationParams);
      const processedResponse = handleIntegrationResponse(response);
      const paginationInfo = extractIntegrationPaginationInfo(response);
      
      setServices(processedResponse.content || []);
      setPagination({
        page: paginationInfo?.number || page,
        size: paginationInfo?.size || size,
        totalPages: paginationInfo?.totalPages || 0,
        totalElements: paginationInfo?.totalElements || 0
      });
    } catch (err) {
      setError(err.message);
      showError('Failed to load services');
    } finally {
      setLoading(false);
    }
  }, []);

  const createService = useCallback(async (serviceData, authHeader) => {
    try {
      const validationErrors = validateIntegrationData(serviceData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildIntegrationDTO(serviceData);
      const response = await createIntegration(authHeader, dto);
      const processedResponse = handleIntegrationResponse(response);
      
      showSuccess('Service created successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to create service');
      return { success: false, error: err.message };
    }
  }, []);

  const updateService = useCallback(async (serviceId, serviceData, authHeader) => {
    try {
      const validationErrors = validateIntegrationData(serviceData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildIntegrationDTO(serviceData);
      const response = await updateIntegration(authHeader, serviceId, dto);
      const processedResponse = handleIntegrationResponse(response);
      
      showSuccess('Service updated successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to update service');
      return { success: false, error: err.message };
    }
  }, []);

  const deleteService = useCallback(async (serviceId, authHeader) => {
    try {
      const response = await deleteIntegration(authHeader, serviceId);
      const processedResponse = handleIntegrationResponse(response);
      
      showSuccess('Service deleted successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to delete service');
      return { success: false, error: err.message };
    }
  }, []);

  const searchServices = useCallback(async (authHeader, filters, page = 0, size = 1000) => {
    setLoading(true);
    setError(null);
    try {
      const searchFilters = buildIntegrationSearchFilters(filters);
      const response = await searchIntegrations(authHeader, searchFilters, { page, size });
      const processedResponse = handleIntegrationResponse(response);
      const paginationInfo = extractIntegrationPaginationInfo(response);
      
      setServices(processedResponse.content || []);
      setPagination({
        page: paginationInfo?.number || page,
        size: paginationInfo?.size || size,
        totalPages: paginationInfo?.totalPages || 0,
        totalElements: paginationInfo?.totalElements || 0
      });
    } catch (err) {
      setError(err.message);
      showError('Failed to search services');
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    services,
    loading,
    error,
    pagination,
    loadServices,
    createService,
    updateService,
    deleteService,
    searchServices
  };
};