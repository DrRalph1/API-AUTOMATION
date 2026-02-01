import { useState, useEffect } from 'react';
import { getFormFieldsByOperationId, validateFormFieldValue } from '@/controllers/FormFieldController';
import { useAuth } from '@/context/AuthContext';
import { tokenAtom } from '@/recoil/tokenAtom';
import { useSetRecoilState, useRecoilValue } from 'recoil';

/**
 * Hook for dynamic form generation and management
 */
export const useDynamicForms = (operationId) => {
  const { getAuthHeader } = useAuth();
  const [fields, setFields] = useState([]);
  const [formData, setFormData] = useState({});
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [fieldConfigs, setFieldConfigs] = useState([]);
  const token = useRecoilValue(tokenAtom);

  // In useDynamicForms.js
  useEffect(() => {
    console.log('useDynamicForms hook received operationId:', operationId);
  }, [operationId]);

  // Load form fields from backend
  useEffect(() => {
    if (!operationId) return;

    const loadFormFields = async () => {
      try {
        setLoading(true);
        const authHeader = token;
        const response = await getFormFieldsByOperationId(authHeader, operationId);
        
        if (response && Array.isArray(response.data)) {
          const fieldConfigs = response.data.map(field => ({
            id: field.fieldName,
            name: field.fieldName,
            label: field.fieldLabel,
            type: mapFieldType(field.fieldType),
            placeholder: field.placeholder || '',
            required: field.required,
            defaultValue: field.defaultValue || '',
            validation: {
              minLength: field.minLength,
              maxLength: field.maxLength,
              minValue: field.minValue,
              maxValue: field.maxValue,
              pattern: field.pattern
            },
            options: field.options || [],
            uiConfig: {
              rows: field.rows,
              cols: field.cols,
              step: field.step,
              helpText: field.helpText
            }
          }));
          
          setFieldConfigs(fieldConfigs);
          
          // Initialize form data with defaults
          const initialData = {};
          fieldConfigs.forEach(field => {
            initialData[field.id] = field.defaultValue || '';
          });
          setFormData(initialData);
        }
      } catch (error) {
        console.error('Error loading form fields:', error);
      } finally {
        setLoading(false);
      }
    };

    loadFormFields();
  }, [operationId, getAuthHeader]);

  // Map backend field types to React input types
  const mapFieldType = (backendType) => {
    const typeMap = {
      'TEXT': 'text',
      'NUMBER': 'number',
      'EMAIL': 'email',
      'PASSWORD': 'password',
      'TEXTAREA': 'textarea',
      'SELECT': 'select',
      'CHECKBOX': 'checkbox',
      'RADIO': 'radio',
      'DATE': 'date',
      'DATETIME': 'datetime-local',
      'TIME': 'time',
      'FILE': 'file',
      'HIDDEN': 'hidden',
      'RICH_TEXT': 'textarea',
      'COLOR': 'color',
      'RANGE': 'range',
      'URL': 'url'
    };
    return typeMap[backendType] || 'text';
  };

  // Handle field value change
  const handleChange = (fieldId, value) => {
    setFormData(prev => ({ ...prev, [fieldId]: value }));
    
    // Validate on change
    const field = fieldConfigs.find(f => f.id === fieldId);
    if (field) {
      validateField(fieldId, value, field);
    }
  };

  // Validate single field
  const validateField = (fieldId, value, fieldConfig) => {
    const validation = validateFormFieldValue(value, {
      type: fieldConfig.type,
      label: fieldConfig.label,
      required: fieldConfig.required,
      ...fieldConfig.validation
    });

    setErrors(prev => ({
      ...prev,
      [fieldId]: validation.errors
    }));

    return validation.isValid;
  };

  // Validate entire form
  const validateForm = () => {
    const newErrors = {};
    let isValid = true;

    fieldConfigs.forEach(field => {
      const fieldValue = formData[field.id];
      const validation = validateFormFieldValue(fieldValue, {
        type: field.type,
        label: field.label,
        required: field.required,
        ...field.validation
      });

      if (!validation.isValid) {
        newErrors[field.id] = validation.errors;
        isValid = false;
      }
    });

    setErrors(newErrors);
    return isValid;
  };

  // Reset form
  const resetForm = () => {
    const initialData = {};
    fieldConfigs.forEach(field => {
      initialData[field.id] = field.defaultValue || '';
    });
    setFormData(initialData);
    setErrors({});
  };

  // Generate form UI components
  const generateFormComponents = () => {
    return fieldConfigs.map(field => ({
      ...field,
      value: formData[field.id] || '',
      error: errors[field.id] || [],
      onChange: (value) => handleChange(field.id, value)
    }));
  };

  return {
    fields: generateFormComponents(),
    formData,
    setFormData, // ADD THIS LINE
    errors,
    loading,
    fieldConfigs,
    handleChange,
    validateForm,
    resetForm,
    isValid: Object.keys(errors).length === 0
  };
};