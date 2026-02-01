// src/hooks/useAdminForms.js
import { useState, useCallback } from 'react';
import { showSuccess, showError } from '@/lib/sweetAlert';
import {
  createFormField,
  getFormFieldsByOperationId,
  updateFormField,
  deleteFormField,
  buildFormFieldPaginationParams,
  buildFormFieldDTO,
  validateFormFieldData,
  extractFormFieldPaginationInfo,
  handleFormFieldResponse,
  getFormFieldUIConfig
} from '@/controllers/FormFieldController';

export const useAdminForms = () => {
  const [formFields, setFormFields] = useState([]);
  const [selectedOperationFormFields, setSelectedOperationFormFields] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });

  const loadFormFields = useCallback(async (authHeader, operationId = null, page = 0, size = 10) => {
    setLoading(true);
    setError(null);
    try {
      let response;
      
      if (operationId) {
        const paginationParams = buildFormFieldPaginationParams(page, size);
        response = await getFormFieldsByOperationId(authHeader, operationId, paginationParams);
        const processedResponse = handleFormFieldResponse(response);
        const paginationInfo = extractFormFieldPaginationInfo(response);
        
        setSelectedOperationFormFields(processedResponse.content || []);
        setPagination({
          page: paginationInfo?.number || page,
          size: paginationInfo?.size || size,
          totalPages: paginationInfo?.totalPages || 0,
          totalElements: paginationInfo?.totalElements || 0
        });
      } else {
        // If no operationId, clear the selected operation fields
        setSelectedOperationFormFields([]);
      }
    } catch (err) {
      setError(err.message);
      showError('Failed to load form fields');
    } finally {
      setLoading(false);
    }
  }, []);

  const createFormField = useCallback(async (formFieldData, authHeader) => {
    try {
      const validationErrors = validateFormFieldData(formFieldData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildFormFieldDTO(formFieldData);
      const response = await createFormField(authHeader, dto);
      const processedResponse = handleFormFieldResponse(response);
      
      showSuccess('Form field created successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to create form field');
      return { success: false, error: err.message };
    }
  }, []);

  const updateFormField = useCallback(async (fieldId, formFieldData, authHeader) => {
    try {
      const validationErrors = validateFormFieldData(formFieldData);
      if (validationErrors.length > 0) {
        throw new Error(validationErrors.join(', '));
      }
      
      const dto = buildFormFieldDTO(formFieldData);
      const response = await updateFormField(authHeader, fieldId, dto);
      const processedResponse = handleFormFieldResponse(response);
      
      showSuccess('Form field updated successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to update form field');
      return { success: false, error: err.message };
    }
  }, []);

  const deleteFormField = useCallback(async (fieldId, authHeader) => {
    try {
      const response = await deleteFormField(authHeader, fieldId);
      const processedResponse = handleFormFieldResponse(response);
      
      showSuccess('Form field deleted successfully');
      return { success: true, data: processedResponse };
    } catch (err) {
      showError(err.message || 'Failed to delete form field');
      return { success: false, error: err.message };
    }
  }, []);

  const getUIConfig = useCallback((field) => {
    try {
      return getFormFieldUIConfig(field);
    } catch (err) {
      console.error('Error getting UI config:', err);
      return null;
    }
  }, []);

  return {
    formFields,
    selectedOperationFormFields,
    loading,
    error,
    pagination,
    loadFormFields,
    createFormField,
    updateFormField,
    deleteFormField,
    getUIConfig
  };
};