// controllers/ServiceCategoryController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh, extractTokenFromHeader } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for search/filter
const buildServiceCategoryQueryParams = (params = {}) => {
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

// ============ CREATE OPERATIONS ============

/**
 * Create a new service category
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} categoryData - ServiceCategoryCreateDTO
 * @returns {Promise} API response
 */
export const createServiceCategory = async (authorizationHeader, categoryData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(categoryData)
        })
    );
};

/**
 * Alternative method to create service category
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} categoryData - ServiceCategoryCreateDTO
 * @returns {Promise} API response
 */
export const createCategory = async (authorizationHeader, categoryData) => {
    return createServiceCategory(authorizationHeader, categoryData);
};

/**
 * Create multiple service categories in bulk
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} categoriesData - Array of ServiceCategoryCreateDTOs
 * @returns {Promise} API response
 */
export const createServiceCategoriesBulk = async (authorizationHeader, categoriesData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(categoriesData)
        })
    );
};

/**
 * Alternative method for bulk creation
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} categoriesData - Array of ServiceCategoryCreateDTOs
 * @returns {Promise} API response
 */
export const createCategoriesBulk = async (authorizationHeader, categoriesData) => {
    return createServiceCategoriesBulk(authorizationHeader, categoriesData);
};

// ============ GET OPERATIONS ============

/**
 * Get a single service category by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Category ID (UUID)
 * @returns {Promise} API response
 */
export const getServiceCategoryById = async (authorizationHeader, categoryId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/${categoryId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get service category
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Category ID (UUID)
 * @returns {Promise} API response
 */
export const getCategory = async (authorizationHeader, categoryId) => {
    return getServiceCategoryById(authorizationHeader, categoryId);
};

/**
 * Get a service category by code
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryCode - Category code
 * @returns {Promise} API response
 */
export const getServiceCategoryByCode = async (authorizationHeader, categoryCode) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/code/${categoryCode}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get a service category by slug
 * @param {string} authorizationHeader - Bearer token
 * @param {string} slug - Category slug
 * @returns {Promise} API response
 */
export const getServiceCategoryBySlug = async (authorizationHeader, slug) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/slug/${slug}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get all service categories (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllServiceCategories = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'displayOrder', direction = 'ASC' } = pagination;
    
    const queryParams = buildServiceCategoryQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/service-categories${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};




/**
 * Get all integrations for a specific service category (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Service category ID (UUID)
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getCategoryIntegrations = async (authorizationHeader, categoryId, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'createdDate', direction = 'DESC' } = pagination;
    
    const queryParams = buildServiceCategoryQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/service-categories/${categoryId}/integrations${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};



/**
 * Alternative method to get all service categories
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getServiceCategories = async (authorizationHeader) => {
    return getAllServiceCategories(authorizationHeader);
};

// ============ SERVICE CATEGORIES WITH COMPLETE DETAILS ============

/**
 * Get all service categories with complete details (paginated)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllServiceCategoriesWithCompleteDetailsPaginated = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'displayOrder', direction = 'ASC' } = pagination;
    
    const queryParams = buildServiceCategoryQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/service-categories/complete-details/paginated${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get all service categories with complete details (no pagination)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getAllServiceCategoriesWithCompleteDetails = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/complete-details`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};



/**
 * Get all parent categories by operation ID (no pagination)
 * Returns the complete hierarchy of parent categories for a specific operation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID
 * @returns {Promise} API response
 */
export const getAllParentCategoriesByOperationId = async (authorizationHeader, operationId) => {
    if (!operationId) {
        throw new Error('Operation ID is required');
    }

    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/by-operation/${operationId}/complete-hierarchy`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};


/**
 * Extract form fields from the operation hierarchy response
 * @param {Object} responseData - The API response data
 * @param {string} operationId - The target operation ID
 * @returns {Array} Array of form fields
 */
export const extractFormFieldsFromHierarchy = (responseData, operationId) => {
  if (!responseData || !responseData.data) return [];
  
  const { categories, operationDetails } = responseData.data;
  let formFields = [];
  
  // Extract from categories
  if (categories && Array.isArray(categories)) {
    categories.forEach(category => {
      if (category.integrations && Array.isArray(category.integrations)) {
        category.integrations.forEach(integration => {
          if (integration.operations && Array.isArray(integration.operations)) {
            integration.operations.forEach(operation => {
              if (operation.operationId === operationId && operation.formFields) {
                formFields = formFields.concat(operation.formFields);
              }
            });
          }
        });
      }
    });
  }
  
  return formFields;
};




/**
 * Get single service category with complete details by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Category ID (UUID)
 * @returns {Promise} API response
 */
export const getServiceCategoryWithCompleteDetailsById = async (authorizationHeader, categoryId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/complete-details/${categoryId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get service categories with complete details by status
 * @param {string} authorizationHeader - Bearer token
 * @param {boolean} status - Status filter (true for active, false for inactive)
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const getServiceCategoriesWithCompleteDetailsByStatus = async (authorizationHeader, status, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'displayOrder', direction = 'ASC' } = pagination;
    
    const queryParams = buildServiceCategoryQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/service-categories/complete-details/status/${status}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Search service categories with complete details
 * @param {string} authorizationHeader - Bearer token
 * @param {string} query - Search query (category name or code)
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchServiceCategoriesWithCompleteDetails = async (authorizationHeader, query, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'displayOrder', direction = 'ASC' } = pagination;
    
    const queryParams = buildServiceCategoryQueryParams({
        query,
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/service-categories/complete-details/search${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get service categories with complete details by parent category
 * @param {string} authorizationHeader - Bearer token
 * @param {string} parentCategoryId - Parent category ID (UUID)
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const getServiceCategoriesWithCompleteDetailsByParent = async (authorizationHeader, parentCategoryId, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'displayOrder', direction = 'ASC' } = pagination;
    
    const queryParams = buildServiceCategoryQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/service-categories/complete-details/parent/${parentCategoryId}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get statistics for service categories with complete details
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getServiceCategoriesCompleteDetailsStatistics = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/complete-details/statistics`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ HIERARCHICAL OPERATIONS ============

/**
 * Get all root categories (no parent)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getRootCategories = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/roots`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get sub-categories of a parent category
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Parent category ID (UUID)
 * @returns {Promise} API response
 */
export const getSubCategories = async (authorizationHeader, categoryId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/${categoryId}/subcategories`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get featured service categories
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getFeaturedCategories = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/featured`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get complete category hierarchy tree
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getCategoryHierarchy = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/hierarchy`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get category hierarchy starting from a specific category
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Starting category ID (UUID)
 * @returns {Promise} API response
 */
export const getCategoryHierarchyById = async (authorizationHeader, categoryId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/${categoryId}/hierarchy`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ UPDATE OPERATIONS ============

/**
 * Update a service category by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Category ID (UUID)
 * @param {Object} categoryData - ServiceCategoryUpdateDTO
 * @returns {Promise} API response
 */
export const updateServiceCategory = async (authorizationHeader, categoryId, categoryData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/${categoryId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(categoryData)
        })
    );
};

/**
 * Alternative method to update service category
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Category ID (UUID)
 * @param {Object} categoryData - Updated category data
 * @returns {Promise} API response
 */
export const updateCategory = async (authorizationHeader, categoryId, categoryData) => {
    return updateServiceCategory(authorizationHeader, categoryId, categoryData);
};

// ============ SEARCH OPERATIONS ============

/**
 * Search service categories with filters (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.searchTerm - Search term
 * @param {boolean} filters.active - Active status filter
 * @param {boolean} filters.featured - Featured status filter
 * @param {boolean} filters.publicCategory - Public status filter
 * @param {string} filters.parentCategoryId - Parent category ID filter (UUID or 'NULL')
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchServiceCategories = async (authorizationHeader, filters = {}, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'displayOrder', direction = 'ASC' } = pagination;
    
    const queryParams = buildServiceCategoryQueryParams({
        ...filters,
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/service-categories/search${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ DELETE OPERATIONS ============

/**
 * Delete a service category by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Category ID (UUID)
 * @returns {Promise} API response
 */
export const deleteServiceCategory = async (authorizationHeader, categoryId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/service-categories/${categoryId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to delete service category
 * @param {string} authorizationHeader - Bearer token
 * @param {string} categoryId - Category ID (UUID)
 * @returns {Promise} API response
 */
export const deleteCategory = async (authorizationHeader, categoryId) => {
    return deleteServiceCategory(authorizationHeader, categoryId);
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for service category operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleServiceCategoryResponse = (response) => {
    if (!response) {
        throw new Error('No response received from service category service');
    }

    // Handle bulk response (207 status)
    if (response.responseCode === 207) {
        return response; // Return as-is for partial success
    }

    if (response.responseCode === 200 || response.responseCode === 201 || response.responseCode === 204) {
        // For 204 responses, return empty data with message
        if (response.responseCode === 204) {
            return { 
                message: response.message || 'No content found', 
                data: [] 
            };
        }
        return response.data || response;
    }

    switch (response.responseCode) {
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
 * Extract pagination info from service category response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractServiceCategoryPaginationInfo = (response) => {
    if (!response) return null;

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
    const data = response.data ? (Array.isArray(response.data) ? response.data : [response.data]) : [];
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
 * Extract complete details statistics from response
 * @param {Object} response - API response with statistics
 * @returns {Object} Complete details statistics
 */
export const extractCompleteDetailsStatistics = (response) => {
    if (!response.statistics && !response.data?.statistics) return null;
    
    return response.statistics || response.data?.statistics || {};
};

/**
 * Extract unique filter values from response
 * @param {Object} response - API response with unique filters
 * @returns {Object} Unique filter values
 */
export const extractUniqueFilters = (response) => {
    const filters = {
        uniqueCodes: response.uniqueCodes || response.data?.uniqueCodes || [],
        uniqueNames: response.uniqueNames || response.data?.uniqueNames || [],
        uniqueStatuses: response.uniqueStatuses || response.data?.uniqueStatuses || [],
        uniqueCategoryCodes: response.uniqueCategoryCodes || response.data?.uniqueCategoryCodes || [],
        uniqueCategoryNames: response.uniqueCategoryNames || response.data?.uniqueCategoryNames || []
    };
    
    // Also check filters property
    if (response.filters) {
        Object.keys(filters).forEach(key => {
            if (response.filters[key]) {
                filters[key] = response.filters[key];
            }
        });
    }
    
    return filters;
};

/**
 * Validate service category data
 * @param {Object} categoryData - Service category data to validate
 * @returns {Array} Array of validation errors
 */
export const validateServiceCategoryData = (categoryData) => {
    const errors = [];
    
    if (!categoryData.categoryCode) errors.push('Category code is required');
    if (!categoryData.categoryName) errors.push('Category name is required');
    if (!categoryData.displayOrder) errors.push('Display order is required');
    
    // Validate category code format (alphanumeric with hyphens)
    if (categoryData.categoryCode && !/^[a-zA-Z0-9-]+$/.test(categoryData.categoryCode)) {
        errors.push('Category code must contain only alphanumeric characters and hyphens');
    }
    
    // Validate display order (positive integer)
    if (categoryData.displayOrder && (!Number.isInteger(categoryData.displayOrder) || categoryData.displayOrder < 0)) {
        errors.push('Display order must be a positive integer');
    }
    
    // Validate icon class length if provided
    if (categoryData.iconClass && categoryData.iconClass.length > 100) {
        errors.push('Icon class cannot exceed 100 characters');
    }
    
    // Validate slug format if provided
    if (categoryData.slug && !/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(categoryData.slug)) {
        errors.push('Slug must be lowercase alphanumeric with hyphens');
    }
    
    return errors;
};

/**
 * Build pagination parameters for service categories
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @param {string} sortField - Field to sort by
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildServiceCategoryPaginationParams = (page = 0, size = 1000, sortField = 'displayOrder', sortDirection = 'ASC') => ({
    page,
    size,
    sort: sortField,
    direction: sortDirection
});

/**
 * Build search filter parameters for service categories
 * @param {Object} filters - Filter criteria
 * @returns {Object} Filter parameters
 */
export const buildServiceCategorySearchFilters = (filters = {}) => {
    const {
        searchTerm = '',
        active = null,
        featured = null,
        publicCategory = null,
        parentCategoryId = ''
    } = filters;
    
    const searchFilters = {};
    
    if (searchTerm) searchFilters.searchTerm = searchTerm;
    if (active !== null) searchFilters.active = active.toString();
    if (featured !== null) searchFilters.featured = featured.toString();
    if (publicCategory !== null) searchFilters.publicCategory = publicCategory.toString();
    if (parentCategoryId) searchFilters.parentCategoryId = parentCategoryId;
    
    return searchFilters;
};

/**
 * Build ServiceCategoryCreate DTO
 * @param {Object} categoryData - Service category data
 * @returns {Object} ServiceCategoryCreate DTO
 */
export const buildServiceCategoryCreateDTO = (categoryData) => {
    const {
        categoryCode,
        categoryName,
        displayOrder,
        parentCategoryId = null,
        iconClass = '',
        slug = '',
        featured = false,
        active = true,
        publicCategory = true,
        description = '',
        metadata = {}
    } = categoryData;
    
    return {
        categoryCode,
        categoryName,
        displayOrder,
        parentCategoryId,
        iconClass,
        slug: slug || categoryCode.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
        featured,
        active,
        publicCategory,
        description,
        metadata: typeof metadata === 'object' ? metadata : {}
    };
};

/**
 * Build ServiceCategoryUpdate DTO
 * @param {Object} categoryData - Service category data for update
 * @returns {Object} ServiceCategoryUpdate DTO
 */
export const buildServiceCategoryUpdateDTO = (categoryData) => {
    const {
        categoryId,
        categoryCode,
        categoryName,
        displayOrder,
        parentCategoryId = null,
        iconClass = '',
        slug = '',
        featured = false,
        active = true,
        publicCategory = true,
        description = '',
        metadata = {}
    } = categoryData;
    
    return {
        categoryId,
        categoryCode,
        categoryName,
        displayOrder,
        parentCategoryId,
        iconClass,
        slug,
        featured,
        active,
        publicCategory,
        description,
        metadata: typeof metadata === 'object' ? metadata : {}
    };
};

/**
 * Build bulk service categories request
 * @param {Array} categories - Array of service category data objects
 * @returns {Array} Array of ServiceCategoryCreate DTOs
 */
export const buildBulkServiceCategoriesRequest = (categories) => {
    if (!Array.isArray(categories)) {
        throw new Error('Service categories must be an array');
    }
    
    return categories.map(category => buildServiceCategoryCreateDTO(category));
};

/**
 * Extract service category options from response
 * @param {Object} response - API response
 * @returns {Array} Array of service category options
 */
export const extractServiceCategoryOptions = (response) => {
    if (!response.data) return [];
    
    const data = Array.isArray(response.data) ? response.data : [response.data];
    
    return data.map(category => ({
        value: category.categoryId || category.categoryCode,
        label: category.categoryName,
        code: category.categoryCode,
        icon: category.iconClass,
        parentId: category.parentCategoryId,
        featured: category.featured,
        active: category.active
    }));
};

/**
 * Extract service category details options from response
 * @param {Object} response - API response with complete details
 * @returns {Array} Array of service category details options
 */
export const extractServiceCategoryDetailsOptions = (response) => {
    if (!response.data) return [];
    
    const data = Array.isArray(response.data) ? response.data : [response.data];
    
    return data.map(category => ({
        value: category.categoryId,
        label: category.categoryName,
        code: category.categoryCode,
        icon: category.iconClass,
        parentId: category.parentCategoryId,
        featured: category.featured,
        active: category.active,
        integrations: category.integrations || [],
        operations: category.operations || [],
        hasIntegrations: (category.integrations && category.integrations.length > 0) || false,
        hasOperations: (category.operations && category.operations.length > 0) || false
    }));
};

/**
 * Build hierarchical tree structure from categories
 * @param {Array} categories - Array of categories with parent-child relationships
 * @returns {Array} Hierarchical tree structure
 */
export const buildCategoryHierarchyTree = (categories) => {
    if (!Array.isArray(categories)) return [];
    
    // Create a map of categories by ID
    const categoryMap = new Map();
    categories.forEach(category => {
        categoryMap.set(category.categoryId, {
            ...category,
            children: []
        });
    });
    
    // Build tree structure
    const tree = [];
    categories.forEach(category => {
        const node = categoryMap.get(category.categoryId);
        if (category.parentCategoryId) {
            const parent = categoryMap.get(category.parentCategoryId);
            if (parent) {
                parent.children.push(node);
            } else {
                tree.push(node); // Orphan node (parent not found)
            }
        } else {
            tree.push(node); // Root node
        }
    });
    
    return tree;
};

/**
 * Flatten hierarchical tree structure
 * @param {Array} tree - Hierarchical tree structure
 * @param {number} level - Current level (for indentation)
 * @returns {Array} Flattened array with levels
 */
export const flattenCategoryHierarchy = (tree, level = 0) => {
    let result = [];
    
    tree.forEach(node => {
        result.push({
            ...node,
            level: level,
            hasChildren: node.children && node.children.length > 0
        });
        
        if (node.children && node.children.length > 0) {
            result = result.concat(flattenCategoryHierarchy(node.children, level + 1));
        }
    });
    
    return result;
};

/**
 * Get parent category options for dropdown
 * @param {Array} categories - Array of all categories
 * @param {string} excludeId - Category ID to exclude (current category)
 * @returns {Array} Parent category options
 */
export const getParentCategoryOptions = (categories, excludeId = null) => {
    if (!Array.isArray(categories)) return [];
    
    // Filter root categories and exclude current category
    return categories
        .filter(category => 
            !category.parentCategoryId && 
            (!excludeId || category.categoryId !== excludeId)
        )
        .map(category => ({
            value: category.categoryId,
            label: category.categoryName,
            code: category.categoryCode
        }));
};

/**
 * Get category statistics from response
 * @param {Object} response - API response with statistics
 * @returns {Object} Category statistics
 */
export const extractCategoryStatistics = (response) => {
    if (!response.statistics) return null;
    
    return {
        totalRootCategories: response.statistics.totalRootCategories || 0,
        totalActiveCategories: response.statistics.totalActiveCategories || 0,
        totalCategories: response.pagination?.total_elements || 0
    };
};

/**
 * Validate category hierarchy operations
 * @param {Object} categoryData - Category data
 * @param {Array} allCategories - All existing categories
 * @returns {Array} Validation errors
 */
export const validateCategoryHierarchy = (categoryData, allCategories) => {
    const errors = [];
    
    // Check for circular reference
    if (categoryData.parentCategoryId) {
        let currentParentId = categoryData.parentCategoryId;
        const visited = new Set([categoryData.categoryId]);
        
        while (currentParentId) {
            if (visited.has(currentParentId)) {
                errors.push('Circular reference detected in category hierarchy');
                break;
            }
            
            visited.add(currentParentId);
            const parentCategory = allCategories.find(cat => cat.categoryId === currentParentId);
            if (!parentCategory) break;
            
            currentParentId = parentCategory.parentCategoryId;
        }
    }
    
    // Check if parent exists
    if (categoryData.parentCategoryId) {
        const parentExists = allCategories.some(cat => cat.categoryId === categoryData.parentCategoryId);
        if (!parentExists) {
            errors.push('Parent category does not exist');
        }
    }
    
    return errors;
};

/**
 * Generate slug from category name
 * @param {string} categoryName - Category name
 * @returns {string} Generated slug
 */
export const generateCategorySlug = (categoryName) => {
    if (!categoryName) return '';
    
    return categoryName
        .toLowerCase()
        .replace(/[^\w\s-]/g, '') // Remove special characters
        .replace(/\s+/g, '-')     // Replace spaces with hyphens
        .replace(/-+/g, '-')      // Replace multiple hyphens with single hyphen
        .trim();
};

/**
 * Format category for display
 * @param {Object} category - Category data
 * @returns {Object} Formatted category
 */
export const formatCategoryForDisplay = (category) => {
    if (!category) return null;
    
    return {
        id: category.categoryId,
        code: category.categoryCode,
        name: category.categoryName,
        description: category.description,
        icon: category.iconClass,
        slug: category.slug,
        featured: category.featured,
        active: category.active,
        public: category.publicCategory,
        displayOrder: category.displayOrder,
        parentId: category.parentCategoryId,
        metadata: category.metadata || {},
        createdAt: category.createdAt,
        updatedAt: category.updatedAt
    };
};

/**
 * Format category with complete details for display
 * @param {Object} category - Category data with complete details
 * @returns {Object} Formatted category with details
 */
export const formatCategoryWithCompleteDetails = (category) => {
    if (!category) return null;
    
    const baseCategory = formatCategoryForDisplay(category);
    
    return {
        ...baseCategory,
        integrations: category.integrations || [],
        operations: category.operations || [],
        formFields: category.formFields || [],
        thirdPartyApis: category.thirdPartyApis || [],
        hasIntegrations: (category.integrations && category.integrations.length > 0) || false,
        hasOperations: (category.operations && category.operations.length > 0) || false,
        hasFormFields: (category.formFields && category.formFields.length > 0) || false,
        hasThirdPartyApis: (category.thirdPartyApis && category.thirdPartyApis.length > 0) || false
    };
};

/**
 * Extract search results metadata
 * @param {Object} response - API response from search
 * @returns {Object} Search metadata
 */
export const extractSearchMetadata = (response) => {
    return {
        query: response.search?.query || '',
        totalResults: response.search?.totalResults || response.data?.length || 0,
        filters: response.filters || {}
    };
};

/**
 * Validate UUID format
 * @param {string} uuid - UUID string to validate
 * @returns {boolean} True if valid UUID
 */
export const isValidCategoryId = (uuid) => {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(uuid);
};

/**
 * Prepare category data for API request
 * @param {Object} categoryData - Raw category data from form
 * @param {boolean} isUpdate - Whether this is for an update operation
 * @returns {Object} Prepared category data
 */
export const prepareCategoryData = (categoryData, isUpdate = false) => {
    const prepared = { ...categoryData };
    
    // Convert displayOrder to number
    if (prepared.displayOrder) {
        prepared.displayOrder = parseInt(prepared.displayOrder, 10);
    }
    
    // Convert boolean values
    if (typeof prepared.featured === 'string') {
        prepared.featured = prepared.featured === 'true' || prepared.featured === '1';
    }
    if (typeof prepared.active === 'string') {
        prepared.active = prepared.active === 'true' || prepared.active === '1';
    }
    if (typeof prepared.publicCategory === 'string') {
        prepared.publicCategory = prepared.publicCategory === 'true' || prepared.publicCategory === '1';
    }
    
    // Generate slug if not provided
    if (!prepared.slug && prepared.categoryName) {
        prepared.slug = generateCategorySlug(prepared.categoryName);
    }
    
    // Handle parentCategoryId - convert empty string to null
    if (prepared.parentCategoryId === '') {
        prepared.parentCategoryId = null;
    }
    
    return isUpdate 
        ? buildServiceCategoryUpdateDTO(prepared)
        : buildServiceCategoryCreateDTO(prepared);
};

/**
 * Process bulk response with success/failure details
 * @param {Object} response - Bulk API response
 * @returns {Object} Processed bulk response
 */
export const processBulkResponse = (response) => {
    if (!response) return null;
    
    return {
        responseCode: response.responseCode,
        message: response.message,
        totalCount: response.totalCount || 0,
        successCount: response.successCount || 0,
        failureCount: response.failureCount || 0,
        successfulCategories: response.successfulCategories || [],
        failedCategories: response.failedCategories || [],
        requestId: response.requestId
    };
};