// controllers/ReceiptTemplateController.js
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh } from "./AuthController.js";

// Base URL for receipt templates (matches backend @RequestMapping)
const BASE_URL = '/receipt-templates';

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper for multipart/form-data headers
const getMultipartHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`
});

// Helper to build query parameters for search/filter with pagination
const buildQueryParams = (params = {}, pageable = {}) => {
    const queryParams = new URLSearchParams();
    
    // Add regular parameters
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '' && params[key] !== 'all') {
            if (Array.isArray(params[key])) {
                params[key].forEach(value => queryParams.append(key, value));
            } else {
                queryParams.append(key, params[key]);
            }
        }
    });
    
    // Add pagination parameters - match backend Pageable structure
    if (pageable.page !== undefined) {
        queryParams.append('page', pageable.page);
    }
    if (pageable.size !== undefined) {
        queryParams.append('size', pageable.size);
    }
    if (pageable.sort) {
        // Backend expects sort parameter (e.g., "createdAt,desc")
        const sortDirection = pageable.direction === 'ASC' ? 'asc' : 'desc';
        queryParams.append('sort', `${pageable.sort},${sortDirection}`);
    }
    
    return queryParams;
};

// Helper to build design query parameters specifically for search
const buildDesignQueryParams = (params = {}, pageable = {}) => {
    const queryParams = buildQueryParams(params, pageable);
    
    // Add specific design filters
    if (params.hasLogo !== undefined && params.hasLogo !== null) {
        queryParams.append('hasLogo', params.hasLogo.toString());
    }
    
    if (params.hasQrCode !== undefined && params.hasQrCode !== null) {
        queryParams.append('hasQrCode', params.hasQrCode.toString());
    }
    
    // Integration ID filter
    if (params.integrationId) {
        queryParams.append('integrationId', params.integrationId);
    }
    
    if (params.sortBy) {
        queryParams.append('sortBy', params.sortBy);
    }
    
    // Remove any duplicate pagination params
    if (queryParams.has('page') && params.page !== undefined) {
        queryParams.delete('page');
        queryParams.append('page', params.page);
    }
    
    if (queryParams.has('size') && params.size !== undefined) {
        queryParams.delete('size');
        queryParams.append('size', params.size);
    }
    
    return queryParams;
};

// ============ RECEIPT DESIGN MANAGEMENT METHODS ============

/**
 * Get all receipt designs with pagination
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pageable - Pagination options
 * @param {number} pageable.page - Page number (0-indexed)
 * @param {number} pageable.size - Page size
 * @param {string} pageable.sort - Sort field
 * @param {string} pageable.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllReceiptDesigns = async (authorizationHeader, pageable = {}) => {
    const queryParams = buildQueryParams({}, pageable);
    const url = `${BASE_URL}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    console.log('DEBUG getAllReceiptDesigns URL:', url);
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get receipt design by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @returns {Promise} API response
 */
export const getReceiptDesignById = async (authorizationHeader, designId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/${designId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Create a new receipt design with logo and QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} designData - Design data matching ReceiptDesignCreateDTO
 * @returns {Promise} API response
 */
export const createReceiptDesign = async (authorizationHeader, designData) => {
    // Prepare data matching backend ReceiptDesignCreateDTO structure
    const processedData = {
        name: designData.name,
        description: designData.description,
        category: designData.category,
        features: designData.features,
        isActive: designData.isActive,
        isDefault: designData.isDefault,
        integrationId: designData.integrationId,
        templateConfig: designData.templateConfig,
        
        // Add logo fields at top level
        companyLogoPreview: designData.companyLogoPreview || null,
        thirdPartyLogoPreview: designData.thirdPartyLogoPreview || null,
        companyLogoText: designData.companyLogoText,
        thirdPartyLogoText: designData.thirdPartyLogoText,
        showCompanyLogoText: designData.showCompanyLogoText,
        showThirdPartyLogoText: designData.showThirdPartyLogoText,
        showCompanyLogo: designData.showCompanyLogo,
        showThirdPartyLogo: designData.showThirdPartyLogo,
        companyLogoPosition: designData.companyLogoPosition,
        thirdPartyLogoPosition: designData.thirdPartyLogoPosition,
        logosAlignment: designData.logosAlignment,
        companyLogoSize: designData.companyLogoSize,
        thirdPartyLogoSize: designData.thirdPartyLogoSize,
        
        // Add QR fields at top level
        showQrCode: designData.showQrCode,
        qrCodeType: designData.qrCodeType,
        qrCodeUrl: designData.qrCodeUrl,
        qrCodeText: designData.qrCodeText,
        qrCodeSize: designData.qrCodeSize,
        qrCodeColor: designData.qrCodeColor,
        qrCodeBgColor: designData.qrCodeBgColor
    };
    
    // Clean up undefined values (keep null values for optional fields)
    const cleanData = Object.fromEntries(
        Object.entries(processedData).filter(([_, v]) => v !== undefined)
    );
    
    console.log('DEBUG createReceiptDesign sending:', {
        url: BASE_URL,
        hasTemplateConfig: !!cleanData.templateConfig,
        companyLogoPreview: !!cleanData.companyLogoPreview,
        companyLogoPreviewLength: cleanData.companyLogoPreview?.length
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(BASE_URL, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(cleanData)
        })
    );
};

/**
 * Update an existing receipt design including logo and QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @param {Object} designData - Updated design data matching ReceiptDesignUpdateDTO
 * @returns {Promise} API response
 */
export const updateReceiptDesign = async (authorizationHeader, designId, designData) => {
    // Prepare data matching backend ReceiptDesignUpdateDTO structure
    const processedData = {
        name: designData.name,
        description: designData.description,
        category: designData.category,
        features: designData.features,
        isActive: designData.isActive,
        isDefault: designData.isDefault,
        integrationId: designData.integrationId,
        templateConfig: designData.templateConfig,
        
        // Logo fields - use values directly from designData
        companyLogoPreview: designData.companyLogoPreview ?? null,
        thirdPartyLogoPreview: designData.thirdPartyLogoPreview ?? null,
        companyLogoText: designData.companyLogoText,
        thirdPartyLogoText: designData.thirdPartyLogoText,
        showCompanyLogoText: designData.showCompanyLogoText,
        showThirdPartyLogoText: designData.showThirdPartyLogoText,
        showCompanyLogo: designData.showCompanyLogo,
        showThirdPartyLogo: designData.showThirdPartyLogo,
        companyLogoPosition: designData.companyLogoPosition,
        thirdPartyLogoPosition: designData.thirdPartyLogoPosition,
        logosAlignment: designData.logosAlignment,
        companyLogoSize: designData.companyLogoSize,
        thirdPartyLogoSize: designData.thirdPartyLogoSize,
        
        // QR fields
        showQrCode: designData.showQrCode,
        qrCodeType: designData.qrCodeType,
        qrCodeUrl: designData.qrCodeUrl,
        qrCodeText: designData.qrCodeText,
        qrCodeSize: designData.qrCodeSize,
        qrCodeColor: designData.qrCodeColor,
        qrCodeBgColor: designData.qrCodeBgColor
    };
    
    // Remove undefined values but keep null
    const cleanData = Object.fromEntries(
        Object.entries(processedData).filter(([_, v]) => v !== undefined)
    );
    
    console.log('DEBUG updateReceiptDesign sending:', {
        designId,
        hasCompanyLogoPreview: !!cleanData.companyLogoPreview,
        companyLogoPreviewIsNull: cleanData.companyLogoPreview === null,
        hasThirdPartyLogoPreview: !!cleanData.thirdPartyLogoPreview,
        thirdPartyLogoPreviewIsNull: cleanData.thirdPartyLogoPreview === null
    });
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/${designId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(cleanData)
        })
    );
};

/**
 * Delete a receipt design
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @returns {Promise} API response
 */
export const deleteReceiptDesign = async (authorizationHeader, designId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/${designId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Set a design as default
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @returns {Promise} API response
 */
export const setDefaultReceiptDesign = async (authorizationHeader, designId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/${designId}/set-default`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Duplicate a receipt design including logo and QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @param {string} newName - Optional new name for duplicated design
 * @returns {Promise} API response
 */
export const duplicateReceiptDesign = async (authorizationHeader, designId, newName = null) => {
    const queryParams = new URLSearchParams();
    if (newName) {
        queryParams.append('newName', newName);
    }
    const url = `${BASE_URL}/${designId}/duplicate${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Preview a receipt design with logo and QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @returns {Promise} API response
 */
export const previewReceiptDesign = async (authorizationHeader, designId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/${designId}/preview`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Export a receipt design including logo and QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @param {string} format - Export format (json or xml)
 * @returns {Promise} API response
 */
export const exportReceiptDesign = async (authorizationHeader, designId, format = 'json') => {
    const queryParams = new URLSearchParams();
    queryParams.append('format', format);
    const url = `${BASE_URL}/${designId}/export${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Import a receipt design with logo and QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} importOptions - Import options
 * @param {File} importOptions.file - Design file (optional)
 * @param {string} importOptions.jsonData - JSON string data (optional)
 * @param {boolean} importOptions.overwrite - Whether to overwrite existing designs
 * @returns {Promise} API response
 */
export const importReceiptDesign = async (authorizationHeader, importOptions = {}) => {
    const { file, jsonData, overwrite = false } = importOptions;
    
    const formData = new FormData();
    if (file) {
        formData.append('file', file);
    }
    if (jsonData) {
        formData.append('jsonData', jsonData);
    }
    formData.append('overwrite', overwrite.toString());
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => {
            return apiCall(`${BASE_URL}/import`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${authHeader.replace('Bearer ', '')}`
                },
                body: formData
            });
        }
    );
};

/**
 * Get receipt design categories
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getReceiptDesignCategories = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/categories`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Search receipt designs with filters including logo/QR features and integration
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.search - Search query
 * @param {string} filters.category - Category filter
 * @param {string} filters.status - Status filter (active/inactive)
 * @param {Array} filters.features - Array of feature IDs
 * @param {string} filters.sortBy - Sort field (popularity, recent, name, category, createdAt, updatedAt, usageCount)
 * @param {boolean} filters.hasLogo - Filter designs with logo
 * @param {boolean} filters.hasQrCode - Filter designs with QR code
 * @param {string} filters.integrationId - Integration ID filter
 * @param {Object} pagination - Pagination options
 * @returns {Promise} API response
 */
export const searchReceiptTemplates = async (
    authorizationHeader,
    filters = {},
    pagination = {}
) => {
    const page = Number.isInteger(pagination?.page) ? pagination.page : 0;
    const size = Number.isInteger(pagination?.size) ? pagination.size : 20;
    const sort = pagination?.sort || 'createdAt';
    const direction = pagination?.direction || 'DESC';

    // Create URLSearchParams with pagination first
    const queryParams = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort: `${sort},${direction.toLowerCase()}`
    });

    console.log('DEBUG - Pagination params:', { page, size, sort, direction });
    console.log('DEBUG - Filters:', filters);

    // Process filters
    if (filters && Object.prototype.toString.call(filters) === '[object Object]') {
        Object.entries(filters).forEach(([key, value]) => {
            // Skip pagination-related fields
            const paginationFields = ['page', 'size', 'sort', 'direction'];
            if (!paginationFields.includes(key) && 
                value !== undefined && value !== null && value !== '') {
                
                // Handle arrays (like features)
                if (Array.isArray(value)) {
                    value.forEach(item => {
                        queryParams.append(key, String(item));
                    });
                } else {
                    queryParams.append(key, String(value));
                }
            }
        });
    }

    const url = `${BASE_URL}/search?${queryParams.toString()}`;
    
    console.log('DEBUG - Final URL:', url);
    console.log('DEBUG - All query params:', Object.fromEntries(queryParams));

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
 * Get active receipt design (default or most used)
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getActiveReceiptDesign = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/active`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get receipt design statistics including logo/QR usage
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getReceiptDesignStatistics = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/statistics`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Apply receipt design with logo/QR to a receipt
 * @param {string} authorizationHeader - Bearer token
 * @param {string} designId - Design ID (UUID)
 * @param {string} receiptId - Receipt ID (UUID)
 * @returns {Promise} API response
 */
export const applyDesignToReceipt = async (authorizationHeader, designId, receiptId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/${designId}/apply-to-receipt/${receiptId}`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get designs by category with logo/QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {string} category - Design category
 * @returns {Promise} API response
 */
export const getDesignsByCategory = async (authorizationHeader, category) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`${BASE_URL}/category/${category}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get popular designs (most used) with logo/QR configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {number} limit - Maximum number of designs to return (1-100)
 * @returns {Promise} API response
 */
export const getPopularDesigns = async (authorizationHeader, limit = 10) => {
    const queryParams = new URLSearchParams();
    queryParams.append('limit', limit.toString());
    const url = `${BASE_URL}/popular${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get designs with logo configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {string} logoType - Optional logo type filter (company/thirdparty)
 * @param {Object} pageable - Pagination options
 * @returns {Promise} API response
 */
export const getDesignsWithLogo = async (authorizationHeader, logoType = null, pageable = {}) => {
    const queryParams = buildQueryParams({}, pageable);
    if (logoType) {
        queryParams.append('logoType', logoType);
    }
    
    const url = `${BASE_URL}/with-logo${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get designs with QR code configurations
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pageable - Pagination options
 * @returns {Promise} API response
 */
export const getDesignsWithQR = async (authorizationHeader, pageable = {}) => {
    const queryParams = buildQueryParams({}, pageable);
    
    const url = `${BASE_URL}/with-qr${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get receipt designs by integration ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} integrationId - Integration ID (UUID)
 * @param {Object} pageable - Pagination options
 * @returns {Promise} API response
 */
export const getReceiptDesignsByIntegrationId = async (authorizationHeader, integrationId, pageable = {}) => {
    const queryParams = buildQueryParams({}, pageable);
    const url = `${BASE_URL}/integration/${integrationId}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get default receipt design for integration
 * @param {string} authorizationHeader - Bearer token
 * @param {string} integrationId - Integration ID (UUID)
 * @returns {Promise} API response
 */
export const getDefaultReceiptDesignByIntegrationId = async (authorizationHeader, integrationId) => {
    const url = `${BASE_URL}/integration/${integrationId}/default`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for design operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleDesignResponse = (response) => {
    if (!response) {
        throw new Error('No response received from receipt design service');
    }

    // Handle 204 No Content responses
    if (response.responseCode === 204) {
        return { 
            data: [], 
            message: response.message || 'No designs found',
            requestId: response.requestId 
        };
    }

    if (response.responseCode === 200 || response.responseCode === 201) {
        return {
            data: response.data || [],
            pagination: response.pagination,
            message: response.message,
            requestId: response.requestId
        };
    }

    // Error handling
    switch (response.responseCode) {
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Design not found: ${response.message}`);
        case 409: throw new Error(`Conflict: ${response.message}`);
        case 422: throw new Error(`Business rule violation: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

/**
 * Extract pagination metadata from response
 * @param {Object} response - API response
 * @returns {Object} Pagination metadata
 */
export const extractPaginationMetadata = (response) => {
    if (!response.pagination) {
        return {
            page: 0,
            size: 20,
            totalElements: 0,
            totalPages: 0,
            isFirst: true,
            isLast: true
        };
    }
    
    return {
        page: response.pagination.page_number || 0,
        size: response.pagination.page_size || 1000,
        totalElements: response.pagination.total_elements || 0,
        totalPages: response.pagination.total_pages || 0,
        isFirst: response.pagination.is_first || false,
        isLast: response.pagination.is_last || false
    };
};

/**
 * Validate design data
 * @param {Object} designData - Design data to validate
 * @returns {Array} Array of validation errors
 */
export const validateDesignData = (designData) => {
    const errors = [];
    
    if (!designData.name || designData.name.trim().length === 0) {
        errors.push('Design name is required');
    }
    
    if (designData.name && designData.name.length > 100) {
        errors.push('Design name must be less than 100 characters');
    }
    
    if (designData.description && designData.description.length > 500) {
        errors.push('Description must be less than 500 characters');
    }
    
    // Validate category based on backend validation
    const validCategories = ['MODERN', 'CLASSIC', 'MINIMALIST', 'PROFESSIONAL', 'CREATIVE', 'COMPACT'];
    if (designData.category && !validCategories.includes(designData.category.toUpperCase())) {
        errors.push(`Invalid category. Must be one of: ${validCategories.join(', ')}`);
    }
    
    // Validate features based on backend
    const validFeatures = ['QR_CODE', 'LOGO', 'SIGNATURE', 'MULTI_LANGUAGE', 'TAX_BREAKDOWN', 'ITEM_LIST', 'FOOTER_NOTES', 'BRANDING'];
    if (designData.features) {
        if (!Array.isArray(designData.features)) {
            errors.push('Features must be an array');
        } else {
            designData.features.forEach((feature, index) => {
                if (!validFeatures.includes(feature)) {
                    errors.push(`Invalid feature at index ${index}: ${feature}. Must be one of: ${validFeatures.join(', ')}`);
                }
            });
        }
    }
    
    // Validate template config with logo and QR configurations
    if (designData.templateConfig) {
        if (typeof designData.templateConfig !== 'object') {
            errors.push('Template config must be an object');
        } else {
            // Validate logo config
            if (designData.templateConfig.logoConfig) {
                const logoConfig = designData.templateConfig.logoConfig;
                
                if (!logoConfig.position || !['HEADER', 'FOOTER', 'SIDEBAR'].includes(logoConfig.position)) {
                    errors.push('Logo position must be one of: HEADER, FOOTER, SIDEBAR');
                }
                
                if (logoConfig.maxWidth && (logoConfig.maxWidth < 50 || logoConfig.maxWidth > 500)) {
                    errors.push('Logo max width must be between 50 and 500 pixels');
                }
                
                if (logoConfig.maxHeight && (logoConfig.maxHeight < 50 || logoConfig.maxHeight > 500)) {
                    errors.push('Logo max height must be between 50 and 500 pixels');
                }
            }
            
            // Validate QR config
            if (designData.templateConfig.qrConfig) {
                const qrConfig = designData.templateConfig.qrConfig;
                
                if (!qrConfig.size || (qrConfig.size < 100 || qrConfig.size > 500)) {
                    errors.push('QR code size must be between 100 and 500 pixels');
                }
                
                if (!qrConfig.color || !isValidColor(qrConfig.color)) {
                    errors.push('QR code color must be a valid hex color');
                }
                
                if (!qrConfig.contentType || !['URL', 'TEXT', 'JSON'].includes(qrConfig.contentType)) {
                    errors.push('QR content type must be one of: URL, TEXT, JSON');
                }
            }
            
            // Validate colors if provided
            if (designData.templateConfig.styling) {
                const { primaryColor, secondaryColor } = designData.templateConfig.styling;
                
                if (primaryColor && !isValidColor(primaryColor)) {
                    errors.push('Primary color must be a valid hex color');
                }
                
                if (secondaryColor && !isValidColor(secondaryColor)) {
                    errors.push('Secondary color must be a valid hex color');
                }
            }
        }
    }
    
    return errors;
};

/**
 * Check if a string is a valid hex color
 * @param {string} color - Color string
 * @returns {boolean} True if valid hex color
 */
const isValidColor = (color) => {
    return /^#([0-9A-F]{3}){1,2}$/i.test(color);
};

/**
 * Generate design preview HTML
 * @param {Object} design - Design data
 * @param {Object} sampleData - Sample receipt data for preview
 * @returns {string} HTML preview
 */
export const generateDesignPreviewHTML = (design, sampleData = {}) => {
    const defaultSample = {
        referenceNumber: 'REC-2024-001',
        transactionId: 'TXN-001-2024',
        amount: 83.00,
        currency: 'USD',
        customerName: 'John Doe',
        items: [
            { description: 'Product A', quantity: 1, unitPrice: 50.00 },
            { description: 'Product B', quantity: 2, unitPrice: 15.00 }
        ],
        tax: 8.00,
        discount: 5.00,
        paymentMethod: 'Credit Card'
    };
    
    const data = { ...defaultSample, ...sampleData };
    const config = design.templateConfig || {};
    const styling = config.styling || {};
    
    // Get logo data directly from design object
    const hasCompanyLogo = design.showCompanyLogo && design.companyLogoPreview;
    const hasThirdPartyLogo = design.showThirdPartyLogo && design.thirdPartyLogoPreview;
    const companyLogoText = design.companyLogoText || 'Company Logo';
    const thirdPartyLogoText = design.thirdPartyLogoText || 'Partner Logo';
    
    // Get QR data directly from design object
    const hasQR = design.showQrCode;
    const qrUrl = design.qrCodeUrl || 'https://example.com/receipt/TXN-001-2024';
    const qrText = design.qrCodeText || 'Transaction ID: TXN-001-2024';
    const qrColor = design.qrCodeColor || '#000000';
    const qrBgColor = design.qrCodeBgColor || '#FFFFFF';
    const qrSize = design.qrCodeSize || 150;
    
    // Get logo positions and alignment
    const companyLogoPosition = design.companyLogoPosition || 'center';
    const thirdPartyLogoPosition = design.thirdPartyLogoPosition || 'center';
    const logosAlignment = design.logosAlignment || 'space-between';
    
    // Get logo sizes
    const companyLogoSize = design.companyLogoSize || 'medium';
    const thirdPartyLogoSize = design.thirdPartyLogoSize || 'medium';
    
    // Convert size strings to pixels
    const getLogoSizePx = (size) => {
        switch(size) {
            case 'small': return 40;
            case 'large': return 80;
            case 'medium': 
            default: return 60;
        }
    };
    
    // Generate logo HTML
    const generateLogoHTML = (logoType) => {
        const isCompany = logoType === 'company';
        const showLogo = isCompany ? design.showCompanyLogo : design.showThirdPartyLogo;
        const logoPreview = isCompany ? design.companyLogoPreview : design.thirdPartyLogoPreview;
        const logoText = isCompany ? companyLogoText : thirdPartyLogoText;
        const showLogoText = isCompany ? design.showCompanyLogoText : design.showThirdPartyLogoText;
        const logoSize = isCompany ? getLogoSizePx(companyLogoSize) : getLogoSizePx(thirdPartyLogoSize);
        
        if (!showLogo || !logoPreview) return '';
        
        return `
            <div style="display: flex; flex-direction: column; align-items: center; margin: 8px;">
                <img src="${logoPreview}" 
                     alt="${logoText}" 
                     style="width: ${logoSize}px; height: ${logoSize}px; object-fit: contain; margin-bottom: 4px;" />
                ${showLogoText ? `<div style="font-size: 10px; color: #6b7280;">${logoText}</div>` : ''}
            </div>
        `;
    };
    
    // Generate logos container based on number of logos
    const generateLogosContainer = () => {
        const companyLogoHTML = generateLogoHTML('company');
        const thirdPartyLogoHTML = generateLogoHTML('thirdParty');
        
        // Count how many logos we have
        const hasCompany = design.showCompanyLogo && design.companyLogoPreview;
        const hasThirdParty = design.showThirdPartyLogo && design.thirdPartyLogoPreview;
        
        if (!hasCompany && !hasThirdParty) return '';
        
        // If only one logo, center it
        if ((hasCompany && !hasThirdParty) || (!hasCompany && hasThirdParty)) {
            return `
                <div style="display: flex; justify-content: center; align-items: center; padding: 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 20px;">
                    ${hasCompany ? companyLogoHTML : thirdPartyLogoHTML}
                </div>
            `;
        }
        
        // If two logos, use the configured alignment
        const justifyContent = {
            'space-between': 'space-between',
            'center': 'center',
            'flex-start': 'flex-start',
            'flex-end': 'flex-end'
        }[logosAlignment] || 'space-between';
        
        return `
            <div style="display: flex; justify-content: ${justifyContent}; align-items: center; padding: 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 20px;">
                ${companyLogoHTML}
                ${thirdPartyLogoHTML}
            </div>
        `;
    };
    
    // Generate QR code HTML
    const generateQRHTML = () => {
        const hasQR = design.showQrCode ?? config?.qrConfig?.showQrCode;

        if (!hasQR) return '';

        const qrUrl = design.qrCodeUrl || config?.qrConfig?.qrCodeUrl || '';
        const qrText = design.qrCodeText || config?.qrConfig?.qrCodeText || 'Scan to verify';
        const qrColor = design.qrCodeColor || config?.qrConfig?.qrCodeColor || '#000000';
        const qrBgColor = design.qrCodeBgColor || config?.qrConfig?.qrCodeBgColor || '#ffffff';
        const qrSizeRaw = design.qrCodeSize || config?.qrConfig?.qrCodeSize;

        // Safe size parser ("100px" → 100)
        const qrSize = typeof qrSizeRaw === 'string'
            ? parseInt(qrSizeRaw.replace('px', ''), 10)
            : qrSizeRaw || 150;

        const qrContent = design.qrCodeType === 'text' ? qrText : qrUrl;

        // Real QR image (scannable)
        const qrImageUrl = `https://api.qrserver.com/v1/create-qr-code/?size=${qrSize}x${qrSize}&data=${encodeURIComponent(qrContent)}&color=${qrColor.replace('#','')}&bgcolor=${qrBgColor.replace('#','')}`;

        return `
            <div class="qr-container" style="
                display:flex;
                flex-direction:column;
                align-items:center;
                margin:20px 0;
                padding:20px;
                background:#f9fafb;
                border-radius:8px;
            ">

                <img 
                    src="${qrImageUrl}"
                    alt="QR Code"
                    style="
                        width:${qrSize}px;
                        height:${qrSize}px;
                        border-radius:8px;
                        border:4px solid white;
                        box-shadow:0 4px 12px rgba(0,0,0,0.1);
                    "
                />

                <div style="
                    margin-top:12px;
                    font-size:12px;
                    color:#6b7280;
                    text-align:center;
                    word-break:break-all;
                ">
                    ${qrText}
                </div>
            </div>
        `;
    };
    
    return `
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Preview: ${design.name}</title>
            <style>
                body {
                    font-family: '${styling.fontFamily || 'Inter, sans-serif'}';
                    margin: 0;
                    padding: 20px;
                    background: linear-gradient(135deg, #ffffffff 0%, #f6f0fcff 100%);
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                
                .preview-container {
                    background: white;
                    border-radius: ${styling.borderRadius || '16px'};
                    box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                    overflow: hidden;
                    max-width: 400px;
                    width: 100%;
                    border: ${styling.showBorder ? `2px solid ${styling.primaryColor || '#3B82F6'}` : 'none'};
                }
                
                .design-header {
                    background: ${styling.primaryColor || '#3B82F6'};
                    color: white;
                    padding: 24px;
                    text-align: center;
                    position: relative;
                }
                
                .design-body {
                    padding: 24px;
                }
                
                .section {
                    margin-bottom: 20px;
                }
                
                .section-title {
                    font-weight: 600;
                    color: ${styling.secondaryColor || '#6B7280'};
                    margin-bottom: 10px;
                    font-size: 14px;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }
                
                .info-row {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 8px;
                    font-size: 14px;
                }
                
                .items-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 10px 0;
                }
                
                .items-table th {
                    text-align: left;
                    padding: 8px 0;
                    border-bottom: 1px solid #e5e7eb;
                    font-weight: 600;
                    color: ${styling.secondaryColor || '#6B7280'};
                    font-size: 12px;
                }
                
                .items-table td {
                    padding: 8px 0;
                    border-bottom: 1px solid #f3f4f6;
                }
                
                .total-section {
                    background: #f9fafb;
                    padding: 16px;
                    border-radius: 8px;
                    margin-top: 20px;
                }
                
                .total-row {
                    display: flex;
                    justify-content: space-between;
                    font-size: 18px;
                    font-weight: 700;
                    color: ${styling.primaryColor || '#3B82F6'};
                }
                
                .footer {
                    text-align: center;
                    padding: 20px;
                    color: #9ca3af;
                    font-size: 12px;
                    border-top: 1px solid #f3f4f6;
                }
            </style>
        </head>
        <body>
            <div class="preview-container">
                <div class="design-header">
                    ${config.header?.showTitle ? '<h1 style="margin: 0; font-size: 20px;">Payment Receipt</h1>' : ''}
                    ${config.header?.showDate ? `<p style="margin: 8px 0 0; opacity: 0.9; font-size: 14px;">${new Date().toLocaleDateString()}</p>` : ''}
                </div>
                
                <div class="design-body">
                    <!-- Logos Container -->
                    ${generateLogosContainer()}
                    
                    <div class="section">
                        <div class="section-title">Transaction Details</div>
                        <div class="info-row">
                            <span>Reference:</span>
                            <span>${data.referenceNumber}</span>
                        </div>
                        <div class="info-row">
                            <span>Transaction ID:</span>
                            <span style="font-family: monospace;">${(data.transactionId).substring(0, 16)}</span>
                        </div>
                        <div class="info-row">
                            <span>Customer:</span>
                            <span>${data.customerName}</span>
                        </div>
                    </div>
                    
                    ${config.body?.showItems && data.items && data.items.length > 0 ? `
                    <div class="section">
                        <div class="section-title">Items</div>
                        <table class="items-table">
                            <thead>
                                <tr>
                                    <th>Description</th>
                                    <th>Qty</th>
                                    <th>Price</th>
                                    <th>Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${data.items.map(item => `
                                    <tr>
                                        <td>${item.description}</td>
                                        <td>${item.quantity}</td>
                                        <td>$${item.unitPrice.toFixed(2)}</td>
                                        <td>$${(item.quantity * item.unitPrice).toFixed(2)}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                    ` : ''}
                    
                    ${config.body?.showTaxes && data.tax ? `
                    <div class="section">
                        <div class="info-row">
                            <span>Tax:</span>
                            <span>$${data.tax.toFixed(2)}</span>
                        </div>
                    </div>
                    ` : ''}
                    
                    ${config.body?.showDiscounts && data.discount ? `
                    <div class="section">
                        <div class="info-row">
                            <span>Discount:</span>
                            <span style="color: #10b981;">-$${data.discount.toFixed(2)}</span>
                        </div>
                    </div>
                    ` : ''}
                    
                    <!-- QR Code -->
                    ${generateQRHTML()}
                    
                    <div class="total-section">
                        <div class="section-title">Total Amount</div>
                        <div class="total-row">
                            <span>Total:</span>
                            <span>${data.currency} ${data.amount.toFixed(2)}</span>
                        </div>
                    </div>
                    
                    ${config.footer?.showPaymentMethod && data.paymentMethod ? `
                    <div class="section">
                        <div class="info-row">
                            <span>Payment Method:</span>
                            <span>${data.paymentMethod}</span>
                        </div>
                    </div>
                    ` : ''}
                </div>
                
                ${config.footer?.showThankYou ? `
                <div class="footer">
                    <p style="margin: 0; color: ${styling.primaryColor || '#3B82F6'};">Thank you for your business!</p>
                    <p style="margin: 8px 0 0;">Generated with ${design.name}</p>
                </div>
                ` : ''}
            </div>
        </body>
        </html>
    `;
};

/**
 * Extract design statistics from response
 * @param {Object} response - API response
 * @returns {Object} Design statistics
 */
export const extractDesignStatistics = (response) => {
    if (!response.data) return null;
    
    const data = response.data;
    return {
        totalDesigns: data.totalDesigns || 0,
        activeDesigns: data.activeDesigns || 0,
        defaultDesign: data.defaultDesign || null,
        designsByCategory: data.designsByCategory || {},
        popularDesigns: data.popularDesigns || [],
        recentActivity: data.recentActivity || [],
        usageStatistics: data.usageStatistics || {},
        logoUsage: data.logoUsage || { withLogo: 0, withoutLogo: 0 },
        qrCodeUsage: data.qrCodeUsage || { withQR: 0, withoutQR: 0 }
    };
};

/**
 * Generate design export data
 * @param {Object} design - Design data
 * @returns {Object} Export-ready design data
 */
export const generateDesignExport = (design) => {
    return {
        name: design.name,
        description: design.description,
        category: design.category,
        features: design.features,
        isActive: design.isActive,
        isDefault: design.isDefault,
        templateConfig: design.templateConfig,
        version: '2.0',
        exportDate: new Date().toISOString(),
        metadata: {
            designId: design.designId || design.id,
            createdBy: design.createdBy,
            createdAt: design.createdAt,
            updatedAt: design.updatedAt
        },
        logoConfig: design.templateConfig?.logoConfig,
        qrConfig: design.templateConfig?.qrConfig
    };
};

/**
 * Process design import data
 * @param {Object} importData - Import data
 * @returns {Object} Processed design data
 */
export const processDesignImport = (importData) => {
    const design = {
        name: importData.name,
        description: importData.description || '',
        category: importData.category || 'MODERN',
        features: importData.features || [],
        isActive: importData.isActive || false,
        isDefault: importData.isDefault || false,
        templateConfig: importData.templateConfig || {
            header: { showLogo: true, showTitle: true, showDate: true },
            body: { showItems: true, showTaxes: true, showDiscounts: true },
            footer: { showTotal: true, showPaymentMethod: true, showThankYou: true },
            styling: {
                primaryColor: '#3B82F6',
                secondaryColor: '#6B7280',
                fontFamily: 'Inter',
                borderRadius: '8px',
                showBorder: true,
                showShadows: false
            },
            logoConfig: importData.logoConfig || {
                enabled: false,
                position: 'HEADER',
                maxWidth: 100,
                maxHeight: 100,
                alignment: 'CENTER'
            },
            qrConfig: importData.qrConfig || {
                enabled: false,
                size: 150,
                color: '#000000',
                contentType: 'URL',
                position: 'BODY'
            }
        }
    };

    // Generate a new name if needed (to avoid duplicates)
    if (importData.metadata?.designId) {
        design.name = `${design.name} (Imported)`;
    }

    return design;
};

/**
 * Get design badge configuration
 * @param {Object} design - Design object
 * @returns {Object} Badge configuration { color: string, label: string }
 */
export const getDesignStatusBadge = (design) => {
    if (!design) return { color: 'default', label: 'Unknown' };
    
    if (design.isDefault) {
        return { color: 'primary', label: 'Default' };
    }
    
    if (design.isActive) {
        return { color: 'success', label: 'Active' };
    }
    
    return { color: 'warning', label: 'Inactive' };
};

/**
 * Get category information
 * @param {string} category - Category ID
 * @returns {Object} Category info { label: string, color: string, bg: string }
 */
export const getCategoryInfo = (category) => {
    const categories = {
        'MODERN': { label: 'Modern', color: 'text-blue-600', bg: 'bg-blue-100' },
        'CLASSIC': { label: 'Classic', color: 'text-amber-600', bg: 'bg-amber-100' },
        'MINIMALIST': { label: 'Minimalist', color: 'text-gray-600', bg: 'bg-gray-100' },
        'PROFESSIONAL': { label: 'Professional', color: 'text-emerald-600', bg: 'bg-emerald-100' },
        'CREATIVE': { label: 'Creative', color: 'text-purple-600', bg: 'bg-purple-100' },
        'COMPACT': { label: 'Compact', color: 'text-red-600', bg: 'bg-red-100' }
    };
    
    return categories[category?.toUpperCase()] || { label: category, color: 'text-gray-600', bg: 'bg-gray-100' };
};

/**
 * Get feature information
 * @param {string} featureId - Feature ID
 * @returns {Object} Feature info { label: string, icon: string }
 */
export const getFeatureInfo = (featureId) => {
    const features = {
        'QR_CODE': { label: 'QR Code', icon: 'QrCode' },
        'LOGO': { label: 'Logo Support', icon: 'Image' },
        'SIGNATURE': { label: 'Digital Signature', icon: 'Edit' },
        'MULTI_LANGUAGE': { label: 'Multi-language', icon: 'Type' },
        'TAX_BREAKDOWN': { label: 'Tax Breakdown', icon: 'Receipt' },
        'ITEM_LIST': { label: 'Itemized List', icon: 'List' },
        'FOOTER_NOTES': { label: 'Footer Notes', icon: 'FileText' },
        'BRANDING': { label: 'Branding', icon: 'Building' }
    };
    
    return features[featureId] || { label: featureId, icon: 'HelpCircle' };
};

/**
 * Prepare search filters for API
 * @param {Object} filters - Frontend filters
 * @returns {Object} Backend-ready filters
 */
export const prepareSearchFilters = (filters = {}) => {
    const backendFilters = {};
    
    if (filters.search) backendFilters.search = filters.search;
    if (filters.category) backendFilters.category = filters.category.toUpperCase();
    if (filters.status) backendFilters.status = filters.status;
    if (filters.features && filters.features.length > 0) backendFilters.features = filters.features;
    if (filters.sortBy) backendFilters.sortBy = filters.sortBy;
    if (filters.hasLogo !== undefined) backendFilters.hasLogo = filters.hasLogo;
    if (filters.hasQrCode !== undefined) backendFilters.hasQrCode = filters.hasQrCode;
    if (filters.integrationId) backendFilters.integrationId = filters.integrationId;
    
    return backendFilters;
};

/**
 * Format design data for display
 * @param {Object} design - Raw design data from API
 * @returns {Object} Formatted design data
 */
export const formatDesignForDisplay = (design) => {
    if (!design) return null;
    
    // Check if design has logo/QR based on top-level fields
    const hasCompanyLogo = design.showCompanyLogo && design.companyLogoPreview;
    const hasThirdPartyLogo = design.showThirdPartyLogo && design.thirdPartyLogoPreview;
    const hasQR = design.showQrCode;
    
    return {
        ...design,
        categoryInfo: getCategoryInfo(design.category),
        statusBadge: getDesignStatusBadge(design),
        formattedFeatures: (design.features || []).map(feature => getFeatureInfo(feature)),
        createdAt: design.createdAt ? new Date(design.createdAt).toLocaleDateString() : '',
        updatedAt: design.updatedAt ? new Date(design.updatedAt).toLocaleDateString() : '',
        hasLogo: hasCompanyLogo || hasThirdPartyLogo,
        hasQR: hasQR,
        logoConfig: design.templateConfig?.logoConfig,
        qrConfig: design.templateConfig?.qrConfig
    };
};

/**
 * Prepare design data for API submission
 * @param {Object} designData - Frontend design data
 * @returns {Object} API-ready design data matching ReceiptDesignCreateDTO
 */
export const prepareDesignForAPI = (designData) => {
    const apiData = {
        name: designData.name,
        description: designData.description || '',
        category: designData.category || 'MODERN',
        features: designData.features || [],
        isActive: designData.isActive !== undefined ? designData.isActive : true,
        isDefault: designData.isDefault || false,
        integrationId: designData.integrationId || null,
        templateConfig: designData.templateConfig || {},
        
        // Logo fields
        companyLogoPreview: designData.companyLogoPreview || null,
        thirdPartyLogoPreview: designData.thirdPartyLogoPreview || null,
        companyLogoText: designData.companyLogoText || '',
        thirdPartyLogoText: designData.thirdPartyLogoText || '',
        showCompanyLogoText: designData.showCompanyLogoText || false,
        showThirdPartyLogoText: designData.showThirdPartyLogoText || false,
        showCompanyLogo: designData.showCompanyLogo || false,
        showThirdPartyLogo: designData.showThirdPartyLogo || false,
        companyLogoPosition: designData.companyLogoPosition || 'center',
        thirdPartyLogoPosition: designData.thirdPartyLogoPosition || 'center',
        logosAlignment: designData.logosAlignment || 'space-between',
        companyLogoSize: designData.companyLogoSize || 'medium',
        thirdPartyLogoSize: designData.thirdPartyLogoSize || 'medium',
        
        // QR fields
        showQrCode: designData.showQrCode || false,
        qrCodeType: designData.qrCodeType || 'url',
        qrCodeUrl: designData.qrCodeUrl || '',
        qrCodeText: designData.qrCodeText || '',
        qrCodeSize: designData.qrCodeSize || 150,
        qrCodeColor: designData.qrCodeColor || '#000000',
        qrCodeBgColor: designData.qrCodeBgColor || '#FFFFFF'
    };

    // Ensure templateConfig has proper structure
    if (!apiData.templateConfig.logoConfig) {
        apiData.templateConfig.logoConfig = {
            enabled: false,
            position: 'HEADER',
            maxWidth: 100,
            maxHeight: 100,
            alignment: 'CENTER'
        };
    }

    if (!apiData.templateConfig.qrConfig) {
        apiData.templateConfig.qrConfig = {
            enabled: false,
            size: 150,
            color: '#000000',
            contentType: 'URL',
            position: 'BODY'
        };
    }

    return apiData;
};

/**
 * Get logo position options
 * @returns {Array} Array of logo position options
 */
export const getLogoPositionOptions = () => {
    return [
        { value: 'HEADER', label: 'Header' },
        { value: 'FOOTER', label: 'Footer' },
        { value: 'SIDEBAR', label: 'Sidebar' }
    ];
};

/**
 * Get QR content type options
 * @returns {Array} Array of QR content type options
 */
export const getQRContentTypeOptions = () => {
    return [
        { value: 'URL', label: 'URL' },
        { value: 'TEXT', label: 'Text' },
        { value: 'JSON', label: 'JSON' }
    ];
};

/**
 * Get logo alignment options
 * @returns {Array} Array of logo alignment options
 */
export const getLogoAlignmentOptions = () => {
    return [
        { value: 'LEFT', label: 'Left' },
        { value: 'CENTER', label: 'Center' },
        { value: 'RIGHT', label: 'Right' }
    ];
};

/**
 * Get QR position options
 * @returns {Array} Array of QR position options
 */
export const getQRPositionOptions = () => {
    return [
        { value: 'HEADER', label: 'Header' },
        { value: 'BODY', label: 'Body' },
        { value: 'FOOTER', label: 'Footer' }
    ];
};

/**
 * Convert frontend design data to backend DTO format
 * @param {Object} designData - Frontend design data
 * @param {boolean} isUpdate - Whether this is for update operation
 * @returns {Object} Backend DTO-ready data
 */
export const toBackendDTO = (designData, isUpdate = false) => {
    const dto = {
        name: designData.name,
        description: designData.description || '',
        category: designData.category || 'MODERN',
        features: designData.features || [],
        isActive: designData.isActive !== undefined ? designData.isActive : true,
        isDefault: designData.isDefault || false,
        integrationId: designData.integrationId || null,
        templateConfig: designData.templateConfig || {},
        
        // Logo fields
        companyLogoPreview: designData.companyLogoPreview,
        thirdPartyLogoPreview: designData.thirdPartyLogoPreview,
        companyLogoText: designData.companyLogoText || '',
        thirdPartyLogoText: designData.thirdPartyLogoText || '',
        showCompanyLogoText: designData.showCompanyLogoText || false,
        showThirdPartyLogoText: designData.showThirdPartyLogoText || false,
        showCompanyLogo: designData.showCompanyLogo || false,
        showThirdPartyLogo: designData.showThirdPartyLogo || false,
        companyLogoPosition: designData.companyLogoPosition || 'center',
        thirdPartyLogoPosition: designData.thirdPartyLogoPosition || 'center',
        logosAlignment: designData.logosAlignment || 'space-between',
        companyLogoSize: designData.companyLogoSize || 'medium',
        thirdPartyLogoSize: designData.thirdPartyLogoSize || 'medium',
        
        // QR fields
        showQrCode: designData.showQrCode || false,
        qrCodeType: designData.qrCodeType || 'url',
        qrCodeUrl: designData.qrCodeUrl || '',
        qrCodeText: designData.qrCodeText || '',
        qrCodeSize: designData.qrCodeSize || 150,
        qrCodeColor: designData.qrCodeColor || '#000000',
        qrCodeBgColor: designData.qrCodeBgColor || '#FFFFFF'
    };

    // For update, we need to handle null values differently
    if (isUpdate) {
        // Ensure we send null for logo previews if they should be cleared
        if (designData.companyLogoPreview === undefined) {
            delete dto.companyLogoPreview;
        }
        if (designData.thirdPartyLogoPreview === undefined) {
            delete dto.thirdPartyLogoPreview;
        }
    }

    return dto;
};

/**
 * Extract error message from API response
 * @param {Object} error - Error object or response
 * @returns {string} Error message
 */
export const extractErrorMessage = (error) => {
    if (!error) return 'Unknown error occurred';
    
    if (error.message) return error.message;
    
    if (error.response && error.response.message) {
        return error.response.message;
    }
    
    if (typeof error === 'string') return error;
    
    return 'An unexpected error occurred';
};

/**
 * Check if response indicates success
 * @param {Object} response - API response
 * @returns {boolean} True if successful
 */
export const isSuccessResponse = (response) => {
    return response && 
           (response.responseCode === 200 || 
            response.responseCode === 201 || 
            response.responseCode === 204);
};

/**
 * Get default pagination settings
 * @returns {Object} Default pagination settings
 */
export const getDefaultPagination = () => {
    return {
        page: 0,
        size: 20,
        sort: 'createdAt',
        direction: 'DESC'
    };
};

/**
 * Transform backend design data to frontend format
 * @param {Object} backendDesign - Backend design DTO
 * @returns {Object} Frontend design data
 */
export const fromBackendDTO = (backendDesign) => {
    if (!backendDesign) return null;
    
    return {
        ...backendDesign,
        id: backendDesign.designId || backendDesign.id,
        // Ensure all fields exist
        companyLogoPreview: backendDesign.companyLogoPreview || null,
        thirdPartyLogoPreview: backendDesign.thirdPartyLogoPreview || null,
        companyLogoText: backendDesign.companyLogoText || '',
        thirdPartyLogoText: backendDesign.thirdPartyLogoText || '',
        showCompanyLogoText: backendDesign.showCompanyLogoText || false,
        showThirdPartyLogoText: backendDesign.showThirdPartyLogoText || false,
        showCompanyLogo: backendDesign.showCompanyLogo || false,
        showThirdPartyLogo: backendDesign.showThirdPartyLogo || false,
        companyLogoPosition: backendDesign.companyLogoPosition || 'center',
        thirdPartyLogoPosition: backendDesign.thirdPartyLogoPosition || 'center',
        logosAlignment: backendDesign.logosAlignment || 'space-between',
        companyLogoSize: backendDesign.companyLogoSize || 'medium',
        thirdPartyLogoSize: backendDesign.thirdPartyLogoSize || 'medium',
        showQrCode: backendDesign.showQrCode || false,
        qrCodeType: backendDesign.qrCodeType || 'url',
        qrCodeUrl: backendDesign.qrCodeUrl || '',
        qrCodeText: backendDesign.qrCodeText || '',
        qrCodeSize: backendDesign.qrCodeSize || 150,
        qrCodeColor: backendDesign.qrCodeColor || '#000000',
        qrCodeBgColor: backendDesign.qrCodeBgColor || '#FFFFFF'
    };
};