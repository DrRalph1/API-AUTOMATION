// controllers/FormFieldController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for search/filter
const buildFormFieldQueryParams = (params = {}) => {
    console.log("params:::::" + JSON.stringify(params))
    const queryParams = new URLSearchParams();
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            if (Array.isArray(params[key])) {
                params[key].forEach(value => queryParams.append(key, value));
            } else {
                queryParams.append(key, params[key]);
            }
        }
    });
    return queryParams;
};

// ============ FORM FIELD MANAGEMENT METHODS ============

/**
 * Create a new form field
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} formFieldData - FormField DTO
 * @returns {Promise} API response
 */
export const createFormField = async (authorizationHeader, formFieldData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(formFieldData)
        })
    );
};

/**
 * Alternative method to create form field
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} formFieldData - FormField DTO
 * @returns {Promise} API response
 */
export const createField = async (authorizationHeader, formFieldData) => {
    return createFormField(authorizationHeader, formFieldData);
};

/**
 * Create multiple form fields in bulk
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} formFieldsData - Array of FormField DTOs
 * @returns {Promise} API response
 */
export const createFormFieldsBulk = async (authorizationHeader, formFieldsData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(formFieldsData)
        })
    );
};

/**
 * Alternative method for bulk creation
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} formFieldsData - Array of FormField DTOs
 * @returns {Promise} API response
 */
export const createFieldsBulk = async (authorizationHeader, formFieldsData) => {
    return createFormFieldsBulk(authorizationHeader, formFieldsData);
};

/**
 * Get form field by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @returns {Promise} API response
 */
export const getFormFieldById = async (authorizationHeader, fieldId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/${fieldId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get form field
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @returns {Promise} API response
 */
export const getField = async (authorizationHeader, fieldId) => {
    return getFormFieldById(authorizationHeader, fieldId);
};

/**
 * Get form field by ID with children
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @returns {Promise} API response
 */
export const getFormFieldWithChildren = async (authorizationHeader, fieldId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/${fieldId}/with-children`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get all form fields (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllFormFields = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 10000, sort = 'fieldLabel', direction = 'ASC' } = pagination;
    
    const queryParams = buildFormFieldQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/form-fields${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get all form fields
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getFormFields = async (authorizationHeader) => {
    return getAllFormFields(authorizationHeader);
};

/**
 * Get form fields by operation ID (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const getFormFieldsByOperationId = async (authorizationHeader, operationId, options = {}) => {
    const { sort = 'fieldLabel', direction = 'asc' } = options;
    
    // Build query parameters
    const queryParams = new URLSearchParams();
    
    // Add sort if provided
    if (sort && sort.trim() !== '') {
        queryParams.append('sort', sort.trim());
    }
    
    // Add direction if provided and not default ascending
    if (direction && direction.toLowerCase() !== 'asc') {
        queryParams.append('direction', direction.toLowerCase());
    }
    
    const queryString = queryParams.toString();
    const url = `/form-fields/operation/${operationId}${queryString ? `?${queryString}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// Helper function for consistent response handling
export const handleFormFieldsResponse = (response) => {
    if (!response || !response.data) {
        return {
            success: false,
            message: 'No response data received',
            data: [],
            totalElements: 0
        };
    }
    
    const responseData = response.data;
    
    if (responseData.responseCode === 200) {
        return {
            success: true,
            message: responseData.message || 'Form fields retrieved successfully',
            data: responseData.data || [],
            totalElements: responseData.totalElements || 0,
            operationId: responseData.operationId,
            requestId: responseData.requestId,
            sort: responseData.sort,
            direction: responseData.direction
        };
    } 
    
    if (responseData.responseCode === 204) {
        return {
            success: true,
            message: responseData.message || 'No form fields found',
            data: [],
            totalElements: 0,
            operationId: responseData.operationId,
            requestId: responseData.requestId
        };
    }
    
    return {
        success: false,
        message: responseData.message || 'Failed to get form fields',
        data: [],
        totalElements: 0,
        responseCode: responseData.responseCode,
        requestId: responseData.requestId
    };
};

/**
 * Alternative method to get form fields by operation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getFieldsByOperation = async (authorizationHeader, operationId) => {
    return getFormFieldsByOperationId(authorizationHeader, operationId);
};

/**
 * Get top-level form fields for an operation (no parent)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getTopLevelFields = async (authorizationHeader, operationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/operation/${operationId}/top-level`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get child form fields for a parent field
 * @param {string} authorizationHeader - Bearer token
 * @param {string} parentFieldId - Parent field ID (UUID)
 * @returns {Promise} API response
 */
export const getChildFields = async (authorizationHeader, parentFieldId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/parent/${parentFieldId}/children`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get OBJECT type fields for an operation (potential parents)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getObjectFields = async (authorizationHeader, operationId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/operation/${operationId}/object-fields`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Update an existing form field
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @param {Object} formFieldData - Updated form field data
 * @returns {Promise} API response
 */
export const updateFormField = async (authorizationHeader, fieldId, formFieldData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/${fieldId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(formFieldData)
        })
    );
};

/**
 * Alternative method to update form field
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @param {Object} formFieldData - Updated form field data
 * @returns {Promise} API response
 */
export const updateField = async (authorizationHeader, fieldId, formFieldData) => {
    return updateFormField(authorizationHeader, fieldId, formFieldData);
};

/**
 * Search form fields with filters (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.fieldName - Field name filter
 * @param {string} filters.fieldLabel - Field label filter
 * @param {string} filters.fieldType - Field type filter
 * @param {boolean} filters.required - Required field filter
 * @param {boolean} filters.readOnly - Read only field filter
 * @param {string} filters.status - Status filter (ACTIVE, INACTIVE, DRAFT, ARCHIVED)
 * @param {string} filters.operationId - Operation ID filter
 * @param {string} filters.parentFieldId - Parent field ID filter (NEW - can be 'null' for top-level fields)
 * @param {string} filters.parameterCategory - API Parameter category filter (NONE, PATH, QUERY, HEADER, BODY, AUTH)
 * @param {string} filters.parameterLocation - API Parameter location filter (NONE, URL, HEADER, BODY)
 * @param {string} filters.apiMapped - Maps to API filter (true/false as string)
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchFormFields = async (
    authorizationHeader,
    filters = {},
    pagination = {}
) => {
    const page = Number.isInteger(pagination?.page) ? pagination.page : 0;
    const size = Number.isInteger(pagination?.size) ? pagination.size : 10;
    const sort = pagination?.sort || 'fieldLabel';
    const direction = pagination?.direction || 'ASC';

    const queryParams = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort: `${sort},${direction}`
    });

    console.log(JSON.stringify("filters::::" + filters));

    // filters must be a plain object
    if (filters && Object.prototype.toString.call(filters) === '[object Object]') {
        Object.entries(filters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, String(value)); // IMPORTANT: convert value to string
            }
        });
    } else {
        console.warn('Filters ignored (not a plain object):', filters);
    }

    const url = `/form-fields/search?${queryParams.toString()}`;

    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) =>
            apiCall(url, {
                method: 'GET',
                headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
            })
    );
};

/**
 * Get form fields by API parameter category for an operation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @param {string} parameterCategory - API Parameter category (NONE, PATH, QUERY, HEADER, BODY, AUTH)
 * @returns {Promise} API response
 */
export const getFieldsByApiParameterCategory = async (authorizationHeader, operationId, parameterCategory) => {
    const url = `/form-fields/operation/${operationId}/api-parameters/${parameterCategory}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get all API-integrated form fields for an operation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getApiIntegratedFields = async (authorizationHeader, operationId) => {
    const url = `/form-fields/operation/${operationId}/api-integrated`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get PATH parameters for an operation ordered by position
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getPathParameters = async (authorizationHeader, operationId) => {
    const fields = await getFieldsByApiParameterCategory(authorizationHeader, operationId, 'PATH');
    return fields;
};

/**
 * Get QUERY parameters for an operation ordered by name
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getQueryParameters = async (authorizationHeader, operationId) => {
    const fields = await getFieldsByApiParameterCategory(authorizationHeader, operationId, 'QUERY');
    return fields;
};

/**
 * Get HEADER parameters for an operation ordered by name
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getHeaderParameters = async (authorizationHeader, operationId) => {
    const fields = await getFieldsByApiParameterCategory(authorizationHeader, operationId, 'HEADER');
    return fields;
};

/**
 * Get BODY parameters for an operation ordered by field name
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getBodyParameters = async (authorizationHeader, operationId) => {
    const fields = await getFieldsByApiParameterCategory(authorizationHeader, operationId, 'BODY');
    return fields;
};

/**
 * Get AUTH parameters for an operation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getAuthParameters = async (authorizationHeader, operationId) => {
    const fields = await getFieldsByApiParameterCategory(authorizationHeader, operationId, 'AUTH');
    return fields;
};

/**
 * Delete a form field
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @returns {Promise} API response
 */
export const deleteFormField = async (authorizationHeader, fieldId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/${fieldId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to delete form field
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @returns {Promise} API response
 */
export const deleteField = async (authorizationHeader, fieldId) => {
    return deleteFormField(authorizationHeader, fieldId);
};

/**
 * Delete a form field and all its children
 * @param {string} authorizationHeader - Bearer token
 * @param {string} fieldId - Field ID (UUID)
 * @returns {Promise} API response
 */
export const deleteFieldWithChildren = async (authorizationHeader, fieldId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/form-fields/${fieldId}/with-children`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for form field operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleFormFieldResponse = (response) => {
    if (!response) {
        throw new Error('No response received from form field service');
    }

    // Handle bulk response (207 status)
    if (response.responseCode === 207) {
        return response; // Return as-is for partial success
    }

    if (response.responseCode === 200 || response.responseCode === 201 || response.responseCode === 204) {
        return response.data || response;
    }

    switch (response.responseCode) {
        case 204:
            return { message: 'No form fields found', data: [] };
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Not Found: ${response.message}`);
        case 409: throw new Error(`Conflict: ${response.message}`);
        case 422: throw new Error(`Business rule violation: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

/**
 * Extract pagination info from form field response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractFormFieldPaginationInfo = (response) => {
    if (!response.data) return null;

    // Check if response has pagination metadata
    if (response.pagination) {
        return {
            content: response.data || [],
            totalPages: response.pagination.total_pages || 0,
            totalElements: response.pagination.total_elements || 0,
            size: response.pagination.page_size || 0,
            number: response.pagination.page_number || 0,
            first: response.pagination.is_first || false,
            last: response.pagination.is_last || false,
            empty: (response.data || []).length === 0,
            numberOfElements: (response.data || []).length
        };
    }

    // Fallback for non-paginated responses
    const data = Array.isArray(response.data) ? response.data : [response.data];
    return {
        content: data,
        totalPages: 1,
        totalElements: data.length,
        size: data.length,
        number: 0,
        first: true,
        last: true,
        empty: data.length === 0,
        numberOfElements: data.length
    };
};

/**
 * Validate form field data
 * @param {Object} formFieldData - Form field data to validate
 * @returns {Array} Array of validation errors
 */
export const validateFormFieldData = (formFieldData) => {
    const errors = [];
    
    if (!formFieldData.fieldName) errors.push('Field name is required');
    if (!formFieldData.fieldLabel) errors.push('Field label is required');
    if (!formFieldData.fieldType) errors.push('Field type is required');
    if (!formFieldData.operationId) errors.push('Operation ID is required');
    
    // Validate field name format (alphanumeric with underscores)
    if (formFieldData.fieldName && !/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(formFieldData.fieldName)) {
        errors.push('Field name must start with a letter or underscore and contain only alphanumeric characters and underscores');
    }
    
    // Validate field type
    const validFieldTypes = [
        'TEXT', 'NUMBER', 'EMAIL', 'PHONE', 'DATE', 
        'SELECT', 'MULTISELECT', 'CHECKBOX', 'RADIO', 
        'TEXTAREA', 'PASSWORD', 'CURRENCY', 'PERCENTAGE', 
        'URL', 'FILE', 'IMAGE', 'HIDDEN', 'OBJECT', 'AUTOGENERATED'
    ];
    
    if (formFieldData.fieldType && !validFieldTypes.includes(formFieldData.fieldType.toUpperCase())) {
        errors.push(`Invalid field type. Must be one of: ${validFieldTypes.join(', ')}`);
    }
    
    // Validate status
    const validStatusValues = ['ACTIVE', 'INACTIVE', 'DRAFT', 'ARCHIVED'];
    if (formFieldData.status && !validStatusValues.includes(formFieldData.status.toUpperCase())) {
        errors.push(`Invalid status. Must be one of: ${validStatusValues.join(', ')}`);
    }
    
    // Validate parameter category (NEW)
    const validParameterCategories = ['NONE', 'PATH', 'QUERY', 'HEADER', 'BODY', 'AUTH'];
    if (formFieldData.parameterCategory && !validParameterCategories.includes(formFieldData.parameterCategory.toUpperCase())) {
        errors.push(`Invalid parameter category. Must be one of: ${validParameterCategories.join(', ')}`);
    }
    
    // Validate parameter location (NEW)
    const validParameterLocations = ['NONE', 'URL', 'HEADER', 'BODY'];
    if (formFieldData.parameterLocation && !validParameterLocations.includes(formFieldData.parameterLocation.toUpperCase())) {
        errors.push(`Invalid parameter location. Must be one of: ${validParameterLocations.join(', ')}`);
    }
    
    // Validate auth type (NEW)
    const validAuthTypes = ['NONE', 'API_KEY', 'BEARER_TOKEN', 'BASIC_AUTH', 'OAUTH2'];
    if (formFieldData.authType && !validAuthTypes.includes(formFieldData.authType.toUpperCase())) {
        errors.push(`Invalid auth type. Must be one of: ${validAuthTypes.join(', ')}`);
    }
    
    // Validate content type (NEW)
    const validContentTypes = [
        'application/json', 'application/xml', 'application/x-www-form-urlencoded',
        'multipart/form-data', 'text/plain'
    ];
    if (formFieldData.contentType && !validContentTypes.includes(formFieldData.contentType)) {
        errors.push(`Invalid content type. Must be one of: ${validContentTypes.join(', ')}`);
    }
    
    // API Mapping validation (NEW)
    if (formFieldData.apiMapping) {
        if (formFieldData.apiMapping.mapsToApi === true) {
            if (!formFieldData.apiMapping.apiFieldName) {
                errors.push('API field name is required when mapsToApi is true');
            }
        }
        
        // Validate API data type
        const validApiDataTypes = ['STRING', 'NUMBER', 'BOOLEAN', 'DATE', 'DATETIME', 'ARRAY', 'OBJECT'];
        if (formFieldData.apiMapping.dataType && !validApiDataTypes.includes(formFieldData.apiMapping.dataType.toUpperCase())) {
            errors.push(`Invalid API data type. Must be one of: ${validApiDataTypes.join(', ')}`);
        }
    }
    
    // API Validation validation (NEW)
    if (formFieldData.apiValidation) {
        const validApiFormats = [
            'STRING', 'EMAIL', 'PHONE', 'DATE', 'DATETIME', 'NUMBER',
            'INTEGER', 'BOOLEAN', 'UUID', 'URL'
        ];
        if (formFieldData.apiValidation.format && !validApiFormats.includes(formFieldData.apiValidation.format.toUpperCase())) {
            errors.push(`Invalid API format. Must be one of: ${validApiFormats.join(', ')}`);
        }
        
        if (formFieldData.apiValidation.minValue !== undefined && 
            formFieldData.apiValidation.maxValue !== undefined &&
            formFieldData.apiValidation.minValue > formFieldData.apiValidation.maxValue) {
            errors.push('API maximum value must be greater than minimum value');
        }
    }
    
    // Parameter-specific validations (NEW)
    if (formFieldData.parameterCategory && formFieldData.parameterCategory.toUpperCase() !== 'NONE') {
        switch (formFieldData.parameterCategory.toUpperCase()) {
            case 'PATH':
                if (formFieldData.pathPosition === undefined || formFieldData.pathPosition === null || formFieldData.pathPosition < 0) {
                    errors.push('Path position is required and must be 0 or greater for PATH parameters');
                }
                if (!formFieldData.pathVariableName) {
                    errors.push('Path variable name is required for PATH parameters');
                }
                // Validate path variable name format
                if (formFieldData.pathVariableName && !/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(formFieldData.pathVariableName)) {
                    errors.push('Path variable name must start with a letter or underscore and contain only alphanumeric characters and underscores');
                }
                break;
                
            case 'QUERY':
                if (!formFieldData.queryParamName) {
                    errors.push('Query parameter name is required for QUERY parameters');
                }
                break;
                
            case 'HEADER':
                if (!formFieldData.headerName) {
                    errors.push('Header name is required for HEADER parameters');
                }
                break;
                
            case 'BODY':
                if (!formFieldData.bodyFieldName) {
                    errors.push('Body field name is required for BODY parameters');
                }
                break;
                
            case 'AUTH':
                if (!formFieldData.authType || formFieldData.authType.toUpperCase() === 'NONE') {
                    errors.push('Auth type is required for AUTH parameters');
                }
                break;
        }
    }
    
    // Auto-generation validation
    if (formFieldData.fieldType === 'AUTOGENERATED' && !formFieldData.generationType) {
        errors.push('Generation type is required for AUTOGENERATED field type');
    }
    
    // Parent field validation
    if (formFieldData.parentFieldId) {
        try {
            // Validate UUID format
            const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
            if (!uuidRegex.test(formFieldData.parentFieldId)) {
                errors.push('Parent field ID must be a valid UUID');
            }
        } catch (error) {
            errors.push('Invalid parent field ID format');
        }
    }
    
    // Validate placeholder length if provided
    if (formFieldData.placeholder && formFieldData.placeholder.length > 500) {
        errors.push('Placeholder cannot exceed 500 characters');
    }
    
    // Validate help text length if provided
    if (formFieldData.helpText && formFieldData.helpText.length > 2000) {
        errors.push('Help text cannot exceed 2000 characters');
    }
    
    // Validate order index
    if (formFieldData.orderIndex !== undefined && formFieldData.orderIndex !== null) {
        if (isNaN(formFieldData.orderIndex) || formFieldData.orderIndex < 0) {
            errors.push('Order index must be 0 or greater');
        }
    }
    
    // Validate length constraints
    if (formFieldData.minLength !== undefined && formFieldData.minLength !== null) {
        if (formFieldData.minLength < 0) {
            errors.push('Minimum length must be 0 or greater');
        }
    }
    
    if (formFieldData.maxLength !== undefined && formFieldData.maxLength !== null) {
        if (formFieldData.maxLength < 1) {
            errors.push('Maximum length must be at least 1');
        }
    }
    
    if (formFieldData.minLength !== undefined && formFieldData.minLength !== null &&
        formFieldData.maxLength !== undefined && formFieldData.maxLength !== null) {
        if (formFieldData.minLength > formFieldData.maxLength) {
            errors.push('Maximum length must be greater than minimum length');
        }
    }
    
    // Validate value constraints
    if (formFieldData.minValue !== undefined && formFieldData.minValue !== null &&
        formFieldData.maxValue !== undefined && formFieldData.maxValue !== null) {
        if (formFieldData.minValue > formFieldData.maxValue) {
            errors.push('Maximum value must be greater than minimum value');
        }
    }
    
    // Validate step value
    if (formFieldData.step !== undefined && formFieldData.step !== null) {
        if (formFieldData.step < 0) {
            errors.push('Step value must be 0 or greater');
        }
    }
    
    // Validate textarea dimensions
    if (formFieldData.rows !== undefined && formFieldData.rows !== null) {
        if (formFieldData.rows < 0) {
            errors.push('Rows must be 0 or greater');
        }
    }
    
    if (formFieldData.cols !== undefined && formFieldData.cols !== null) {
        if (formFieldData.cols < 0) {
            errors.push('Columns must be 0 or greater');
        }
    }
    
    // Validate options for select/multi-select/checkbox/radio fields
    const optionBasedFields = ['SELECT', 'MULTISELECT', 'CHECKBOX', 'RADIO'];
    if (optionBasedFields.includes(formFieldData.fieldType?.toUpperCase())) {
        if (!formFieldData.options || !Array.isArray(formFieldData.options) || formFieldData.options.length === 0) {
            errors.push('At least one option is required for this field type');
        } else {
            // Validate each option
            formFieldData.options.forEach((option, index) => {
                if (!option.value || option.value.trim() === '') {
                    errors.push(`Option ${index + 1}: value cannot be empty`);
                }
                if (!option.label || option.label.trim() === '') {
                    errors.push(`Option ${index + 1}: label cannot be empty`);
                }
            });
        }
    }
    
    return errors;
};

/**
 * Build pagination parameters for form fields
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildFormFieldPaginationParams = (page = 0, size = 10, sortField = 'fieldLabel', sortDirection = 'ASC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build search filter parameters for form fields
 * @param {Object} filters - Filter criteria
 * @returns {Object} Filter parameters
 */
export const buildFormFieldSearchFilters = (filters = {}) => {
    const {
        fieldName = '',
        fieldLabel = '',
        fieldType = '',
        required = null,
        readOnly = null,
        status = '',
        operationId = '',
        parentFieldId = '', // NEW: Parent field filter
        parameterCategory = '', // NEW
        parameterLocation = '', // NEW
        apiMapped = '' // NEW
    } = filters;
    
    const searchFilters = {};
    
    if (fieldName) searchFilters.fieldName = fieldName;
    if (fieldLabel) searchFilters.fieldLabel = fieldLabel;
    if (fieldType) searchFilters.fieldType = fieldType;
    if (required !== null) searchFilters.required = required.toString();
    if (readOnly !== null) searchFilters.readOnly = readOnly.toString();
    if (status) searchFilters.status = status;
    if (operationId) searchFilters.operationId = operationId;
    if (parentFieldId) searchFilters.parentFieldId = parentFieldId; // NEW
    // NEW: API integration filters
    if (parameterCategory) searchFilters.parameterCategory = parameterCategory;
    if (parameterLocation) searchFilters.parameterLocation = parameterLocation;
    if (apiMapped) searchFilters.apiMapped = apiMapped;
    
    return searchFilters;
};

/**
 * Build FormField DTO with API integration and parent-child support
 * @param {Object} formFieldData - Form field data
 * @returns {Object} FormField DTO
 */
export const buildFormFieldDTO = (formFieldData) => {
    const {
        fieldName,
        fieldLabel,
        fieldType,
        operationId,
        parentFieldId = null, // NEW: Parent field ID
        placeholder = '',
        helpText = '',
        defaultValue = null,
        required = false,
        readOnly = false,
        minLength = null,
        maxLength = null,
        minValue = null,
        maxValue = null,
        pattern = null,
        options = [], // For SELECT, CHECKBOX, RADIO, MULTISELECT fields
        rows = null, // For TEXTAREA
        cols = null, // For TEXTAREA
        step = null, // For NUMBER, CURRENCY, PERCENTAGE
        orderIndex = 0,
        status = 'ACTIVE',
        // NEW: Auto-generation fields
        autoGenerated = false,
        generationType = '',
        // NEW: API Integration fields
        parameterCategory = 'NONE',
        parameterLocation = 'NONE',
        pathPosition = null,
        pathVariableName = '',
        queryParamName = '',
        headerName = '',
        bodyFieldName = '',
        authType = 'NONE',
        contentType = 'application/json',
        apiMapping = {
            mapsToApi: false,
            apiFieldName: '',
            dataType: 'STRING',
            transformFunction: '',
            defaultValueMapping: '',
            requiredInApi: false
        },
        apiValidation = {
            minValue: null,
            maxValue: null,
            pattern: '',
            enumValues: [],
            format: 'STRING'
        }
    } = formFieldData;
    
    return {
        fieldName,
        fieldLabel,
        fieldType: fieldType.toUpperCase(),
        operationId,
        parentFieldId,
        placeholder,
        helpText,
        defaultValue,
        required,
        readOnly,
        minLength,
        maxLength,
        minValue,
        maxValue,
        pattern,
        options: Array.isArray(options) ? options : [],
        rows,
        cols,
        step,
        orderIndex,
        status: status.toUpperCase(),
        // NEW: Auto-generation fields
        autoGenerated,
        generationType,
        // NEW: API Integration fields
        parameterCategory: parameterCategory.toUpperCase(),
        parameterLocation: parameterLocation.toUpperCase(),
        pathPosition,
        pathVariableName,
        queryParamName,
        headerName,
        bodyFieldName,
        authType: authType.toUpperCase(),
        contentType,
        apiMapping,
        apiValidation
    };
};

/**
 * Build bulk form fields request
 * @param {Array} formFields - Array of form field data objects
 * @returns {Array} Array of FormField DTOs
 */
export const buildBulkFormFieldsRequest = (formFields) => {
    if (!Array.isArray(formFields)) {
        throw new Error('Form fields must be an array');
    }
    
    return formFields.map(field => buildFormFieldDTO(field));
};

/**
 * Extract form field options from response
 * @param {Object} response - API response
 * @returns {Array} Array of form field options
 */
export const extractFormFieldOptions = (response) => {
    if (!response.data) return [];
    
    if (Array.isArray(response.data)) {
        return response.data.map(field => ({
            value: field.fieldId || field.fieldName,
            label: field.fieldLabel,
            type: field.fieldType,
            required: field.required,
            readOnly: field.readOnly,
            status: field.status,
            orderIndex: field.orderIndex,
            defaultValue: field.defaultValue,
            parentFieldId: field.parentFieldId, // NEW
            autoGenerated: field.autoGenerated, // NEW
            generationType: field.generationType, // NEW
            // NEW: API integration fields
            parameterCategory: field.parameterCategory,
            parameterLocation: field.parameterLocation,
            apiMapped: field.apiMapping?.mapsToApi || false
        }));
    }
    
    const field = response.data;
    return [{
        value: field.fieldId || field.fieldName,
        label: field.fieldLabel,
        type: field.fieldType,
        required: field.required,
        readOnly: field.readOnly,
        status: field.status,
        orderIndex: field.orderIndex,
        defaultValue: field.defaultValue,
        parentFieldId: field.parentFieldId, // NEW
        autoGenerated: field.autoGenerated, // NEW
        generationType: field.generationType, // NEW
        // NEW: API integration fields
        parameterCategory: field.parameterCategory,
        parameterLocation: field.parameterLocation,
        apiMapped: field.apiMapping?.mapsToApi || false
    }];
};

/**
 * Get form field configuration for UI rendering with API integration
 * @param {Object} formField - Form field data
 * @returns {Object} UI configuration object
 */
export const getFormFieldUIConfig = (formField) => {
    const {
        fieldId,
        fieldName,
        fieldLabel,
        fieldType,
        placeholder,
        helpText,
        defaultValue,
        required,
        readOnly,
        minLength,
        maxLength,
        minValue,
        maxValue,
        pattern,
        options,
        rows,
        cols,
        step,
        orderIndex,
        status,
        operationId,
        operationName,
        parentFieldId, // NEW
        parentFieldLabel, // NEW
        childFields = [], // NEW
        autoGenerated, // NEW
        generationType, // NEW
        createdAt,
        lastModifiedDate,
        // NEW: API integration fields
        parameterCategory,
        parameterLocation,
        pathPosition,
        pathVariableName,
        queryParamName,
        headerName,
        bodyFieldName,
        authType,
        contentType,
        apiMapping,
        apiValidation
    } = formField;
    
    const config = {
        id: fieldId,
        name: fieldName,
        label: fieldLabel,
        type: fieldType.toLowerCase(),
        placeholder: placeholder || '',
        helperText: helpText || '',
        defaultValue: defaultValue || '',
        required: required || false,
        disabled: readOnly || false,
        status: status || 'ACTIVE',
        orderIndex: orderIndex || 0,
        operationId: operationId,
        operationName: operationName,
        parentFieldId: parentFieldId, // NEW
        parentFieldLabel: parentFieldLabel, // NEW
        childFields: childFields || [], // NEW
        autoGenerated: autoGenerated || false, // NEW
        generationType: generationType || '', // NEW
        createdAt: createdAt,
        lastModifiedDate: lastModifiedDate,
        // NEW: API integration configuration
        apiIntegration: {
            isApiIntegrated: parameterCategory && parameterCategory !== 'NONE',
            parameterCategory: parameterCategory || 'NONE',
            parameterLocation: parameterLocation || 'NONE',
            pathPosition,
            pathVariableName,
            queryParamName,
            headerName,
            bodyFieldName,
            authType,
            contentType,
            apiMapping: apiMapping || {},
            apiValidation: apiValidation || {}
        }
    };
    
    // Add type-specific configurations
    switch (fieldType.toUpperCase()) {
        case 'TEXT':
        case 'EMAIL':
        case 'PHONE':
        case 'PASSWORD':
        case 'URL':
            if (minLength !== null) config.minLength = minLength;
            if (maxLength !== null) config.maxLength = maxLength;
            if (pattern) config.pattern = pattern;
            break;
            
        case 'NUMBER':
        case 'CURRENCY':
        case 'PERCENTAGE':
            if (minValue !== null) config.min = minValue;
            if (maxValue !== null) config.max = maxValue;
            if (step !== null) config.step = step;
            break;
            
        case 'TEXTAREA':
            if (rows !== null) config.rows = rows;
            if (cols !== null) config.cols = cols;
            if (minLength !== null) config.minLength = minLength;
            if (maxLength !== null) config.maxLength = maxLength;
            break;
            
        case 'SELECT':
        case 'MULTISELECT':
        case 'CHECKBOX':
        case 'RADIO':
            if (Array.isArray(options) && options.length > 0) {
                config.options = options.map(option => ({
                    value: option.value,
                    label: option.label || option.value
                }));
            }
            break;
            
        case 'DATE':
            config.format = 'date';
            break;
            
        case 'OBJECT':
            config.isParent = true;
            break;
            
        case 'AUTOGENERATED':
            config.autoGenerated = true;
            config.generationType = generationType;
            break;
    }
    
    return config;
};

/**
 * Validate form field value based on field configuration
 * @param {string} value - Field value to validate
 * @param {Object} fieldConfig - Field configuration
 * @returns {Object} Validation result { isValid: boolean, errors: Array }
 */
export const validateFormFieldValue = (value, fieldConfig) => {
    const errors = [];
    
    // Check required field
    if (fieldConfig.required && (value === null || value === undefined || value === '')) {
        errors.push(`${fieldConfig.label} is required`);
        return { isValid: false, errors };
    }
    
    // If value is empty and not required, it's valid
    if (!value && !fieldConfig.required) {
        return { isValid: true, errors: [] };
    }
    
    // Type-specific validation
    switch (fieldConfig.type) {
        case 'email':
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(value)) {
                errors.push('Invalid email format');
            }
            break;
            
        case 'phone':
            const phoneRegex = /^[+]?[0-9\s\-().]{10,}$/;
            if (!phoneRegex.test(value)) {
                errors.push('Invalid phone number format');
            }
            break;
            
        case 'number':
        case 'currency':
        case 'percentage':
            const numValue = Number(value);
            if (isNaN(numValue)) {
                errors.push('Must be a valid number');
            } else {
                if (fieldConfig.min !== undefined && numValue < fieldConfig.min) {
                    errors.push(`Must be at least ${fieldConfig.min}`);
                }
                if (fieldConfig.max !== undefined && numValue > fieldConfig.max) {
                    errors.push(`Must be at most ${fieldConfig.max}`);
                }
            }
            break;
            
        case 'url':
            try {
                new URL(value);
            } catch (e) {
                errors.push('Invalid URL format');
            }
            break;
            
        case 'text':
        case 'textarea':
            if (fieldConfig.minLength !== undefined && value.length < fieldConfig.minLength) {
                errors.push(`Must be at least ${fieldConfig.minLength} characters`);
            }
            if (fieldConfig.maxLength !== undefined && value.length > fieldConfig.maxLength) {
                errors.push(`Must be at most ${fieldConfig.maxLength} characters`);
            }
            break;
    }
    
    // Pattern validation
    if (fieldConfig.pattern && !new RegExp(fieldConfig.pattern).test(value)) {
        errors.push('Value does not match required pattern');
    }
    
    // NEW: API Validation if field is API integrated
    if (fieldConfig.apiIntegration?.isApiIntegrated && fieldConfig.apiIntegration.apiValidation) {
        const apiValidation = fieldConfig.apiIntegration.apiValidation;
        
        // Validate against API enum values
        if (apiValidation.enumValues && apiValidation.enumValues.length > 0) {
            if (!apiValidation.enumValues.includes(value)) {
                errors.push(`Value must be one of: ${apiValidation.enumValues.join(', ')}`);
            }
        }
        
        // Validate against API format
        if (apiValidation.format) {
            switch (apiValidation.format.toUpperCase()) {
                case 'EMAIL':
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(value)) {
                        errors.push('Invalid email format for API');
                    }
                    break;
                    
                case 'UUID':
                    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
                    if (!uuidRegex.test(value)) {
                        errors.push('Invalid UUID format for API');
                    }
                    break;
                    
                case 'URL':
                    try {
                        new URL(value);
                    } catch (e) {
                        errors.push('Invalid URL format for API');
                    }
                    break;
                    
                case 'PHONE':
                    const phoneRegex = /^[+]?[0-9\s\-().]{10,}$/;
                    if (!phoneRegex.test(value)) {
                        errors.push('Invalid phone number format for API');
                    }
                    break;
            }
        }
    }
    
    return {
        isValid: errors.length === 0,
        errors
    };
};

/**
 * Get form field summary for display with API integration info
 * @param {Object} formField - Form field data
 * @returns {Object} Summary object
 */
export const getFormFieldSummary = (formField) => {
    return {
        id: formField.fieldId,
        name: formField.fieldName,
        label: formField.fieldLabel,
        type: formField.fieldType,
        required: formField.required,
        readOnly: formField.readOnly,
        status: formField.status,
        orderIndex: formField.orderIndex,
        operationName: formField.operationName,
        parentFieldId: formField.parentFieldId, // NEW
        parentFieldLabel: formField.parentFieldLabel, // NEW
        hasChildren: formField.childFields && formField.childFields.length > 0, // NEW
        autoGenerated: formField.autoGenerated, // NEW
        // NEW: API integration info
        apiIntegrated: formField.parameterCategory && formField.parameterCategory !== 'NONE',
        parameterCategory: formField.parameterCategory,
        mapsToApi: formField.apiMapping?.mapsToApi || false
    };
};

/**
 * Format form field value for display
 * @param {string} value - Field value
 * @param {string} fieldType - Field type
 * @param {Object} options - Field options (for select/multi-select fields)
 * @returns {string} Formatted value
 */
export const formatFormFieldValue = (value, fieldType, options = []) => {
    if (value === null || value === undefined || value === '') {
        return 'N/A';
    }
    
    switch (fieldType.toUpperCase()) {
        case 'SELECT':
        case 'CHECKBOX':
        case 'RADIO':
            const option = options.find(opt => opt.value === value);
            return option ? option.label : value;
            
        case 'MULTISELECT':
            if (Array.isArray(value)) {
                return value.map(v => {
                    const option = options.find(opt => opt.value === v);
                    return option ? option.label : v;
                }).join(', ');
            }
            return value;
            
        case 'DATE':
            return new Date(value).toLocaleDateString();
            
        case 'CURRENCY':
            return new Intl.NumberFormat('en-US', {
                style: 'currency',
                currency: 'USD'
            }).format(Number(value));
            
        case 'PERCENTAGE':
            return `${Number(value)}%`;
            
        default:
            return String(value);
    }
};

/**
 * Get API parameter name from form field
 * @param {Object} formField - Form field data
 * @returns {string} API parameter name
 */
export const getApiParameterName = (formField) => {
    if (!formField.parameterCategory || formField.parameterCategory === 'NONE') {
        return null;
    }
    
    switch (formField.parameterCategory) {
        case 'PATH':
            return formField.pathVariableName;
        case 'QUERY':
            return formField.queryParamName;
        case 'HEADER':
            return formField.headerName;
        case 'BODY':
            return formField.bodyFieldName;
        case 'AUTH':
            return formField.authType;
        default:
            return null;
    }
};

/**
 * Check if form field is API integrated
 * @param {Object} formField - Form field data
 * @returns {boolean} True if field is API integrated
 */
export const isApiIntegratedField = (formField) => {
    return formField.parameterCategory && formField.parameterCategory !== 'NONE';
};

/**
 * Check if form field has API mapping
 * @param {Object} formField - Form field data
 * @returns {boolean} True if field has API mapping
 */
export const hasApiMapping = (formField) => {
    return formField.apiMapping && formField.apiMapping.mapsToApi === true;
};

/**
 * Transform form field value for API based on mapping configuration
 * @param {string} value - Original field value
 * @param {Object} formField - Form field data
 * @returns {any} Transformed value for API
 */
export const transformValueForApi = (value, formField) => {
    if (!formField.apiMapping || !formField.apiMapping.mapsToApi) {
        return value;
    }
    
    let transformedValue = value;
    
    // Apply transformation function if provided
    if (formField.apiMapping.transformFunction) {
        try {
            // Note: In production, use a safer evaluation method or predefined transformation functions
            const transformFunc = new Function('value', formField.apiMapping.transformFunction);
            transformedValue = transformFunc(value);
        } catch (error) {
            console.warn('Failed to apply transformation function:', error);
        }
    }
    
    // Convert to appropriate data type
    if (formField.apiMapping.dataType) {
        switch (formField.apiMapping.dataType.toUpperCase()) {
            case 'NUMBER':
                transformedValue = Number(transformedValue);
                break;
            case 'BOOLEAN':
                transformedValue = Boolean(transformedValue);
                break;
            case 'DATE':
            case 'DATETIME':
                transformedValue = new Date(transformedValue).toISOString();
                break;
            case 'ARRAY':
                if (!Array.isArray(transformedValue)) {
                    transformedValue = [transformedValue];
                }
                break;
        }
    }
    
    return transformedValue;
};

/**
 * Check if field has children
 * @param {Object} formField - Form field data
 * @returns {boolean} True if field has children
 */
export const hasChildren = (formField) => {
    return formField.childFields && formField.childFields.length > 0;
};

/**
 * Check if field is top-level (no parent)
 * @param {Object} formField - Form field data
 * @returns {boolean} True if field is top-level
 */
export const isTopLevelField = (formField) => {
    return !formField.parentFieldId;
};

/**
 * Check if field can have children (OBJECT type)
 * @param {Object} formField - Form field data
 * @returns {boolean} True if field can have children
 */
export const canHaveChildren = (formField) => {
    return formField.fieldType === 'OBJECT';
};

/**
 * Get field hierarchy level
 * @param {Object} formField - Form field data
 * @param {Array} allFields - All form fields
 * @returns {number} Hierarchy level (0 for top-level, 1 for first child, etc.)
 */
export const getFieldHierarchyLevel = (formField, allFields) => {
    if (!formField.parentFieldId) {
        return 0;
    }
    
    let level = 1;
    let currentField = formField;
    
    while (currentField.parentFieldId) {
        const parent = allFields.find(f => f.fieldId === currentField.parentFieldId);
        if (!parent) break;
        level++;
        currentField = parent;
    }
    
    return level;
};