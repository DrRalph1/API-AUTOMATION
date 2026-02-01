// controllers/TransactionController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { apiCallWithTokenRefresh } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to build query parameters for search/filter
const buildTransactionQueryParams = (params = {}) => {
    const queryParams = new URLSearchParams();
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            if (Array.isArray(params[key])) {
                params[key].forEach(value => queryParams.append(key, value));
            } else if (typeof params[key] === 'boolean') {
                queryParams.append(key, params[key].toString());
            } else if (params[key] instanceof Date) {
                // Format date as yyyy-MM-dd as expected by Java controller
                const date = params[key];
                const formattedDate = date.toISOString().split('T')[0];
                queryParams.append(key, formattedDate);
            } else if (typeof params[key] === 'number') {
                queryParams.append(key, params[key].toString());
            } else if (typeof params[key] === 'object' && params[key] !== null) {
                // Handle BigDecimal conversion for amounts
                if (key.includes('Amount')) {
                    queryParams.append(key, params[key].toString());
                }
            } else {
                queryParams.append(key, params[key]);
            }
        }
    });
    return queryParams;
};

// ============ TRANSACTION MANAGEMENT METHODS ============

/**
 * Get transaction statistics
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getTransactionStatistics = async (authorizationHeader) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/transactions/statistics`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Create a new transaction
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} transactionData - Transaction DTO
 * @returns {Promise} API response
 */
export const createTransaction = async (authorizationHeader, transactionData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/transactions`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(transactionData)
        })
    );
};

/**
 * Create multiple transactions in bulk
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} transactionsData - Array of Transaction DTOs
 * @returns {Promise} API response
 */
export const createTransactionsBulk = async (authorizationHeader, transactionsData) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/transactions/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(transactionsData)
        })
    );
};

/**
 * Update transaction status
 * @param {string} authorizationHeader - Bearer token
 * @param {string} transactionId - Transaction ID (UUID)
 * @param {string} status - New status
 * @param {Object|string} responsePayload - Response payload (object or string)
 * @returns {Promise} API response
 */
export const updateTransactionStatus = async (authorizationHeader, transactionId, status, responseCode, responsePayload) => {
    // VALIDATE transactionId first
    if (!transactionId || transactionId === 'undefined') {
        throw new Error('Transaction ID is required and must be a valid UUID');
    }
    
    if (!isValidTransactionId(transactionId)) {
        throw new Error(`Invalid transaction ID format: ${transactionId}`);
    }
    
    // Using query parameters as per Java controller @RequestParam
    const queryParams = new URLSearchParams();
    
    if (status) {
        queryParams.append('status', status);
    }

    if (responseCode) {
        queryParams.append('responseCode', responseCode);
    }
    
    if (responsePayload) {
        // If responsePayload is an object, stringify it
        // If it's already a string, use it as-is
        const payloadStr = typeof responsePayload === 'object' 
            ? JSON.stringify(responsePayload) 
            : responsePayload;
        
        queryParams.append('responsePayload', payloadStr);
    }
    
    const url = `/transactions/${transactionId}/status${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    console.log('Update transaction status URL:', url);
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'PATCH',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Get transactions by operation ID (paginated)
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getTransactionsByOperation = async (authorizationHeader, operationId, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'createdAt', direction = 'DESC' } = pagination;
    
    const queryParams = buildTransactionQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    const url = `/transactions/operation/${operationId}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get transactions by operation
 * @param {string} authorizationHeader - Bearer token
 * @param {string} operationId - Operation ID (UUID)
 * @returns {Promise} API response
 */
export const getOperationTransactions = async (authorizationHeader, operationId) => {
    return getTransactionsByOperation(authorizationHeader, operationId);
};

/**
 * Get a single transaction by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} transactionId - Transaction ID (UUID)
 * @returns {Promise} API response
 */
export const getTransactionById = async (authorizationHeader, transactionId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/transactions/${transactionId}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get transaction
 * @param {string} authorizationHeader - Bearer token
 * @param {string} transactionId - Transaction ID (UUID)
 * @returns {Promise} API response
 */
export const getTransaction = async (authorizationHeader, transactionId) => {
    return getTransactionById(authorizationHeader, transactionId);
};

/**
 * Get all transactions (paginated + sortable)
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} pagination - Pagination parameters
 * @param {number} pagination.page - Page number (0-indexed)
 * @param {number} pagination.size - Page size
 * @param {string} pagination.sort - Sort field
 * @param {string} pagination.direction - Sort direction (ASC/DESC)
 * @returns {Promise} API response
 */
export const getAllTransactions = async (authorizationHeader, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'createdAt', direction = 'DESC' } = pagination;
    
    const queryParams = buildTransactionQueryParams({
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    // Note: Java controller uses GET "/" for getAllTransactions
    const url = `/transactions${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Alternative method to get all transactions
 * @param {string} authorizationHeader - Bearer token
 * @returns {Promise} API response
 */
export const getTransactions = async (authorizationHeader) => {
    return getAllTransactions(authorizationHeader);
};

/**
 * Search transactions with enhanced filters
 * @param {string} authorizationHeader - Bearer token
 * @param {Object} filters - Search filters
 * @param {string} filters.status - Transaction status filter
 * @param {string} filters.operationId - Operation ID filter (UUID)
 * @param {string} filters.userId - User ID filter
 * @param {string} filters.reference - Reference filter (partial match)
 * @param {number} filters.minAmount - Minimum amount filter
 * @param {number} filters.maxAmount - Maximum amount filter
 * @param {Date|string} filters.startDate - Start date filter (yyyy-MM-dd)
 * @param {Date|string} filters.endDate - End date filter (yyyy-MM-dd)
 * @param {Object} pagination - Pagination parameters
 * @returns {Promise} API response
 */
export const searchTransactions = async (authorizationHeader, filters = {}, pagination = {}) => {
    const { page = 0, size = 1000, sort = 'createdAt', direction = 'DESC', include = [] } = pagination;
    
    // Format dates if provided
    const formattedFilters = { ...filters };
    
    if (filters.startDate) {
        if (filters.startDate instanceof Date) {
            formattedFilters.startDate = filters.startDate.toISOString().split('T')[0];
        } else {
            // Ensure date string is in yyyy-MM-dd format
            const date = new Date(filters.startDate);
            if (!isNaN(date.getTime())) {
                formattedFilters.startDate = date.toISOString().split('T')[0];
            }
        }
    }
    
    if (filters.endDate) {
        if (filters.endDate instanceof Date) {
            formattedFilters.endDate = filters.endDate.toISOString().split('T')[0];
        } else {
            // Ensure date string is in yyyy-MM-dd format
            const date = new Date(filters.endDate);
            if (!isNaN(date.getTime())) {
                formattedFilters.endDate = date.toISOString().split('T')[0];
            }
        }
    }
    
    // Handle include parameter for relations
    const queryParams = buildTransactionQueryParams({
        ...formattedFilters,
        page,
        size,
        sort: `${sort},${direction}`
    });
    
    // Add include parameters if provided
    if (include && Array.isArray(include) && include.length > 0) {
        include.forEach(relation => {
            queryParams.append('include', relation);
        });
    }
    
    const url = `/transactions/search${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    console.log('Search URL with include:', url);
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(url, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

/**
 * Delete a transaction by ID
 * @param {string} authorizationHeader - Bearer token
 * @param {string} transactionId - Transaction ID (UUID)
 * @returns {Promise} API response
 */
export const deleteTransaction = async (authorizationHeader, transactionId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/transactions/${transactionId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ RESPONSE HANDLERS & UTILITIES ============

/**
 * Handle standardized API responses for transaction operations
 * @param {Object} response - API response
 * @returns {Object} Processed response data
 */
export const handleTransactionResponse = (response) => {
    if (!response) {
        throw new Error('No response received from transaction service');
    }

    console.log("Transaction Response:::::::::" + JSON.stringify(response));

    // Handle bulk response (207 status)
    if (response.responseCode === 207) {
        return response; // Return as-is for partial success
    }

    // Handle 204 (No Content) responses - Java controller returns 204 with message
    if (response.responseCode === 204) {
        return {
            data: response.data || [],
            message: response.message || 'No transactions found',
            requestId: response.requestId,
            pagination: response.pagination
        };
    }

    if ([200, 201].includes(response.responseCode)) {
        return {
            data: response.data || response,
            message: response.message,
            requestId: response.requestId,
            pagination: response.pagination
        };
    }

    // Handle sort validation errors (400 with specific message)
    if (response.responseCode === 400 && response.message && response.message.includes('Invalid sort parameter')) {
        throw new Error(`Invalid sort parameter: ${response.message}`);
    }

    switch (response.responseCode) {
        case 400: 
            const errorDetails = response.data ? ` - Details: ${JSON.stringify(response.data)}` : '';
            console.log(`Something went ${response.message}`)
                // throw new Error(`Bad Request: ${response.message}${errorDetails}`);
        case 401: 
            console.log(`Something went ${response.message}`)
            // throw new Error(`Unauthorized: ${response.message}`);
        case 403: 
            console.log(`Something went ${response.message}`)
            // throw new Error(`Forbidden: ${response.message}`);
        case 404: 
            console.log(`Something went ${response.message}`)
            // throw new Error(`Transaction not found: ${response.message}`);
        case 409: 
            console.log(`Something went ${response.message}`)
            // throw new Error(`Conflict: ${response.message}`);
        case 422: 
            console.log(`Something went ${response.message}`)
            // throw new Error(`Business rule violation: ${response.message}`);
        case 500: 
            console.log(`Something went ${response.message}`)
            // throw new Error(`Server Error: ${response.message}`);
        default: 
            console.log(`Something went ${response.message}`)
            // throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

/**
 * Extract pagination info from transaction response
 * @param {Object} response - API response
 * @returns {Object} Pagination information
 */
export const extractTransactionPaginationInfo = (response) => {
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
 * Validate transaction search parameters
 * @param {Object} filters - Search filters to validate
 * @returns {Array} Array of validation errors
 */
export const validateTransactionSearchParams = (filters = {}) => {
    const errors = [];
    
    // Validate amount range
    if (filters.minAmount !== undefined && filters.minAmount !== null) {
        if (typeof filters.minAmount !== 'number' || filters.minAmount < 0) {
            errors.push('Minimum amount must be a positive number');
        }
    }
    
    if (filters.maxAmount !== undefined && filters.maxAmount !== null) {
        if (typeof filters.maxAmount !== 'number' || filters.maxAmount < 0) {
            errors.push('Maximum amount must be a positive number');
        }
    }
    
    if (filters.minAmount !== undefined && filters.maxAmount !== undefined && 
        filters.minAmount > filters.maxAmount) {
        errors.push('Minimum amount cannot be greater than maximum amount');
    }
    
    // Validate date range
    if (filters.startDate) {
        const startDate = new Date(filters.startDate);
        if (isNaN(startDate.getTime())) {
            errors.push('Invalid start date format. Use yyyy-MM-dd');
        }
    }
    
    if (filters.endDate) {
        const endDate = new Date(filters.endDate);
        if (isNaN(endDate.getTime())) {
            errors.push('Invalid end date format. Use yyyy-MM-dd');
        }
    }
    
    if (filters.startDate && filters.endDate) {
        const startDate = new Date(filters.startDate);
        const endDate = new Date(filters.endDate);
        if (startDate > endDate) {
            errors.push('Start date cannot be after end date');
        }
    }
    
    // Validate operationId format if provided
    if (filters.operationId && !isValidTransactionId(filters.operationId)) {
        errors.push('Operation ID must be a valid UUID');
    }
    
    // Validate status if provided
    if (filters.status) {
        const validStatuses = ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED'];
        if (!validStatuses.includes(filters.status.toUpperCase())) {
            errors.push(`Status must be one of: ${validStatuses.join(', ')}`);
        }
    }
    
    // Validate sort field names for search
    if (filters.sort) {
        const validSortFields = [
            'transactionId', 'id',
            'status',
            'reference',
            'amount',
            'createdAt', 'date',
            'updatedAt', 'modified'
        ];
        
        // Extract sort field name (remove direction if present)
        const sortField = filters.sort.split(',')[0].toLowerCase();
        if (!validSortFields.some(validField => validField.toLowerCase() === sortField)) {
            errors.push(`Invalid sort field: ${sortField}. Valid fields are: ${validSortFields.join(', ')}`);
        }
    }
    
    return errors;
};

/**
 * Build transaction search filters with enhanced parameters
 * @param {Object} filters - Filter criteria
 * @returns {Object} Filter parameters
 */
export const buildTransactionSearchFilters = (filters = {}) => {
    const {
        status = '',
        operationId = '',
        userId = '',
        reference = '',
        minAmount = null,
        maxAmount = null,
        startDate = null,
        endDate = null
    } = filters;
    
    const searchFilters = {};
    
    if (status) searchFilters.status = status;
    if (operationId) searchFilters.operationId = operationId;
    if (userId) searchFilters.userId = userId;
    if (reference) searchFilters.reference = reference;
    if (minAmount !== null && minAmount !== undefined) searchFilters.minAmount = minAmount;
    if (maxAmount !== null && maxAmount !== undefined) searchFilters.maxAmount = maxAmount;
    if (startDate) searchFilters.startDate = startDate;
    if (endDate) searchFilters.endDate = endDate;
    
    return searchFilters;
};

/**
 * Build enhanced pagination parameters with sort validation
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size (default 20 as per Java controller)
 * @param {string} sortField - Field to sort by (must be valid)
 * @param {string} sortDirection - Sort direction (ASC/DESC)
 * @returns {Object} Pagination parameters
 */
export const buildTransactionPaginationParams = (page = 0, size = 10, sortField = 'createdAt', sortDirection = 'DESC') => {
    // Map user-friendly sort field names to actual field names
    const sortFieldMap = {
        'id': 'transactionId',
        'date': 'createdAt',
        'modified': 'updatedAt',
        'transactionid': 'transactionId',
        'createdat': 'createdAt',
        'updatedat': 'updatedAt'
    };
    
    const mappedSortField = sortFieldMap[sortField.toLowerCase()] || sortField;
    
    return {
        page,
        size,
        sort: mappedSortField,
        direction: sortDirection
    };
};

/**
 * Format date for search API (yyyy-MM-dd)
 * @param {Date|string} date - Date to format
 * @returns {string} Formatted date string
 */
export const formatSearchDate = (date) => {
    if (!date) return '';
    
    const dateObj = date instanceof Date ? date : new Date(date);
    if (isNaN(dateObj.getTime())) return '';
    
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
};

/**
 * Parse search response and handle edge cases
 * @param {Object} response - API response from search
 * @returns {Object} Processed search results
 */
export const parseSearchResponse = (response) => {
    if (!response) {
        return {
            data: [],
            message: 'No response received',
            requestId: null,
            pagination: {
                total_pages: 0,
                total_elements: 0,
                page_number: 0,
                page_size: 0,
                is_first: true,
                is_last: true
            }
        };
    }
    
    // Handle 204 (No Content) with message
    if (response.responseCode === 204) {
        return {
            data: [],
            message: response.message || 'No transactions found',
            requestId: response.requestId,
            pagination: response.pagination || {
                total_pages: 0,
                total_elements: 0,
                page_number: 0,
                page_size: 0,
                is_first: true,
                is_last: true
            }
        };
    }
    
    // Handle success response
    if (response.responseCode === 200) {
        return {
            data: response.data || [],
            message: response.message || 'Search completed successfully',
            requestId: response.requestId,
            pagination: response.pagination || {
                total_pages: 1,
                total_elements: response.data ? response.data.length : 0,
                page_number: 0,
                page_size: response.data ? response.data.length : 0,
                is_first: true,
                is_last: true
            }
        };
    }
    
    // Handle error response
    throw new Error(`Search failed: ${response.message || 'Unknown error'}`);
};

/**
 * Generate search summary for logging/display
 * @param {Object} filters - Search filters used
 * @param {Object} response - Search response
 * @returns {string} Search summary text
 */
export const generateSearchSummary = (filters, response) => {
    if (!response) return 'Search not performed';
    
    const activeFilters = [];
    
    if (filters.status) activeFilters.push(`Status: ${filters.status}`);
    if (filters.operationId) activeFilters.push(`Operation: ${filters.operationId}`);
    if (filters.userId) activeFilters.push(`User: ${filters.userId}`);
    if (filters.reference) activeFilters.push(`Reference: ${filters.reference}`);
    if (filters.minAmount !== undefined) activeFilters.push(`Min Amount: ${filters.minAmount}`);
    if (filters.maxAmount !== undefined) activeFilters.push(`Max Amount: ${filters.maxAmount}`);
    if (filters.startDate) activeFilters.push(`From: ${formatSearchDate(filters.startDate)}`);
    if (filters.endDate) activeFilters.push(`To: ${formatSearchDate(filters.endDate)}`);
    
    const filterSummary = activeFilters.length > 0 ? `Filters: ${activeFilters.join(', ')}` : 'All transactions';
    const resultSummary = response.pagination 
        ? `Found ${response.pagination.total_elements} transaction(s) across ${response.pagination.total_pages} page(s)`
        : `Found ${response.data ? response.data.length : 0} transaction(s)`;
    
    return `${filterSummary} - ${resultSummary}`;
};

// ============ UPDATED VALIDATION FUNCTIONS ============

/**
 * Validate transaction data according to backend DTO structure
 * @param {Object} transactionData - Transaction data to validate
 * @returns {Array} Array of validation errors
 */
export const validateTransactionData = (transactionData) => {
    const errors = validateTransactionSearchParams(transactionData); // Reuse search validation
    
    // Required fields based on backend TransactionDTO
    if (!transactionData.operationId) errors.push('Operation ID is required');
    if (!transactionData.userId) errors.push('User ID is required');
    if (!transactionData.amount || transactionData.amount <= 0) errors.push('Valid amount is required');
    if (!transactionData.currency) errors.push('Currency is required');
    if (!transactionData.paymentMethod) errors.push('Payment method is required');
    if (!transactionData.reference) errors.push('Reference is required');
    
    // Validate amount format
    if (transactionData.amount && (isNaN(transactionData.amount) || transactionData.amount <= 0)) {
        errors.push('Amount must be a positive number');
    }
    
    // Validate currency format (ISO 4217)
    if (transactionData.currency && !/^[A-Z]{3}$/.test(transactionData.currency)) {
        errors.push('Currency must be a valid 3-letter ISO code');
    }
    
    // Validate reference length
    if (transactionData.reference && transactionData.reference.length > 100) {
        errors.push('Reference cannot exceed 100 characters');
    }
    
    // Validate description length if provided
    if (transactionData.description && transactionData.description.length > 500) {
        errors.push('Description cannot exceed 500 characters');
    }
    
    // Validate UUID format for operationId if provided
    if (transactionData.operationId && !isValidTransactionId(transactionData.operationId)) {
        errors.push('Operation ID must be a valid UUID');
    }
    
    // Validate status if provided
    if (transactionData.status) {
        const validStatuses = ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED'];
        if (!validStatuses.includes(transactionData.status.toUpperCase())) {
            errors.push(`Status must be one of: ${validStatuses.join(', ')}`);
        }
    }
    
    return errors;
};


/**
 * Build Transaction DTO matching backend structure
 * @param {Object} transactionData - Transaction data
 * @returns {Object} Transaction DTO
 */
export const buildTransactionDTO = (transactionData) => {
    const {
        operationId,
        userId,
        amount,
        currency = 'USD',
        paymentMethod,
        reference,
        description = '',
        status = 'PENDING',
        dueDate = null,
        category = '',
        tags = [],
        metadata = {}
    } = transactionData;
    
    // Convert amount to number if it's a string
    const parsedAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
    
    // Build DTO matching Java TransactionDTO structure
    const dto = {
        operationId,
        userId,
        amount: parsedAmount,
        currency: currency.toUpperCase(),
        paymentMethod,
        reference,
        description,
        status: status.toUpperCase()
    };
    
    // Add optional fields if they exist
    if (dueDate) dto.dueDate = dueDate instanceof Date ? dueDate.toISOString() : dueDate;
    if (category) dto.category = category;
    if (tags && Array.isArray(tags)) dto.tags = tags;
    if (metadata && Object.keys(metadata).length > 0) dto.metadata = metadata;
    
    return dto;
};

/**
 * Build bulk transactions request
 * @param {Array} transactions - Array of transaction data objects
 * @returns {Array} Array of Transaction DTOs
 */
export const buildBulkTransactionsRequest = (transactions) => {
    if (!Array.isArray(transactions)) {
        throw new Error('Transactions must be an array');
    }
    
    return transactions.map(transaction => buildTransactionDTO(transaction));
};

/**
 * Get transaction status options
 * @returns {Array} Array of status options
 */
export const getTransactionStatusOptions = () => {
    return [
        { value: 'PENDING', label: 'Pending', color: 'warning' },
        { value: 'PROCESSING', label: 'Processing', color: 'info' },
        { value: 'COMPLETED', label: 'Completed', color: 'success' },
        { value: 'FAILED', label: 'Failed', color: 'error' },
        { value: 'CANCELLED', label: 'Cancelled', color: 'secondary' },
        { value: 'REFUNDED', label: 'Refunded', color: 'default' }
    ];
};

/**
 * Get transaction status badge configuration
 * @param {string} status - Transaction status
 * @returns {Object} Badge configuration { color: string, label: string }
 */
export const getTransactionStatusBadge = (status) => {
    const statusMap = {
        'PENDING': { color: 'warning', label: 'Pending' },
        'PROCESSING': { color: 'info', label: 'Processing' },
        'COMPLETED': { color: 'success', label: 'Completed' },
        'FAILED': { color: 'error', label: 'Failed' },
        'CANCELLED': { color: 'secondary', label: 'Cancelled' },
        'REFUNDED': { color: 'default', label: 'Refunded' }
    };
    
    return statusMap[status?.toUpperCase()] || { color: 'default', label: status || 'Unknown' };
};

/**
 * Validate UUID format
 * @param {string} uuid - UUID string to validate
 * @returns {boolean} True if valid UUID
 */
export const isValidTransactionId = (uuid) => {
    if (!uuid) return false;
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(uuid);
};

/**
 * Format transaction amount with currency
 * @param {number} amount - Transaction amount
 * @param {string} currency - Currency code
 * @returns {string} Formatted amount
 */
export const formatTransactionAmount = (amount, currency = 'USD') => {
    if (amount === null || amount === undefined) return 'N/A';
    
    try {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency,
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(amount);
    } catch (error) {
        return `${currency} ${amount.toFixed(2)}`;
    }
};

/**
 * Format transaction date
 * @param {string|Date} date - Transaction date
 * @returns {string} Formatted date
 */
export const formatTransactionDate = (date) => {
    if (!date) return 'N/A';
    
    const dateObj = date instanceof Date ? date : new Date(date);
    
    if (isNaN(dateObj.getTime())) return 'Invalid Date';
    
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(dateObj);
};

/**
 * Calculate transaction summary statistics
 * @param {Array} transactions - Array of transactions
 * @returns {Object} Summary statistics
 */
export const calculateTransactionSummary = (transactions) => {
    if (!Array.isArray(transactions) || transactions.length === 0) {
        return {
            total: 0,
            totalAmount: 0,
            completed: 0,
            pending: 0,
            failed: 0,
            averageAmount: 0,
            highestAmount: 0,
            lowestAmount: 0
        };
    }
    
    const completedTransactions = transactions.filter(t => t.status === 'COMPLETED');
    const amounts = completedTransactions.map(t => t.amount || 0);
    
    const totalAmount = amounts.reduce((sum, amount) => sum + amount, 0);
    const averageAmount = amounts.length > 0 ? totalAmount / amounts.length : 0;
    
    return {
        total: transactions.length,
        totalAmount,
        completed: completedTransactions.length,
        pending: transactions.filter(t => t.status === 'PENDING').length,
        failed: transactions.filter(t => t.status === 'FAILED').length,
        averageAmount,
        highestAmount: amounts.length > 0 ? Math.max(...amounts) : 0,
        lowestAmount: amounts.length > 0 ? Math.min(...amounts) : 0
    };
};

/**
 * Sort transactions by various criteria
 * @param {Array} transactions - Array of transactions
 * @param {string} sortBy - Sort criteria (amount, date, status, reference)
 * @param {string} direction - Sort direction (asc, desc)
 * @returns {Array} Sorted transactions
 */
export const sortTransactions = (transactions, sortBy = 'date', direction = 'desc') => {
    if (!Array.isArray(transactions)) return [];
    
    const sorted = [...transactions];
    
    switch (sortBy.toLowerCase()) {
        case 'amount':
            sorted.sort((a, b) => (a.amount || 0) - (b.amount || 0));
            break;
        case 'date':
            sorted.sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0));
            break;
        case 'status':
            sorted.sort((a, b) => (a.status || '').localeCompare(b.status || ''));
            break;
        case 'reference':
            sorted.sort((a, b) => (a.reference || '').localeCompare(b.reference || ''));
            break;
        case 'user':
            sorted.sort((a, b) => (a.userId || '').localeCompare(b.userId || ''));
            break;
        default:
            sorted.sort((a, b) => new Date(a.createdAt || 0) - new Date(b.createdAt || 0));
            break;
    }
    
    if (direction.toLowerCase() === 'desc') {
        sorted.reverse();
    }
    
    return sorted;
};

/**
 * Filter transactions by criteria
 * @param {Array} transactions - Array of transactions
 * @param {Object} filters - Filter criteria
 * @returns {Array} Filtered transactions
 */
export const filterTransactions = (transactions, filters = {}) => {
    if (!Array.isArray(transactions)) return [];
    
    return transactions.filter(transaction => {
        // Status filter
        if (filters.status && transaction.status !== filters.status) {
            return false;
        }
        
        // Operation filter
        if (filters.operationId && transaction.operationId !== filters.operationId) {
            return false;
        }
        
        // User filter
        if (filters.userId && transaction.userId !== filters.userId) {
            return false;
        }
        
        // Date range filter
        if (filters.startDate) {
            const transactionDate = new Date(transaction.createdAt || transaction.date);
            const startDate = new Date(filters.startDate);
            if (transactionDate < startDate) return false;
        }
        
        if (filters.endDate) {
            const transactionDate = new Date(transaction.createdAt || transaction.date);
            const endDate = new Date(filters.endDate);
            if (transactionDate > endDate) return false;
        }
        
        // Amount range filter
        if (filters.minAmount !== undefined && (transaction.amount || 0) < filters.minAmount) {
            return false;
        }
        
        if (filters.maxAmount !== undefined && (transaction.amount || 0) > filters.maxAmount) {
            return false;
        }
        
        // Search term filter
        if (filters.searchTerm) {
            const searchTerm = filters.searchTerm.toLowerCase();
            const searchableFields = [
                transaction.reference,
                transaction.description,
                transaction.userId,
                transaction.status,
                transaction.transactionId
            ].join(' ').toLowerCase();
            
            if (!searchableFields.includes(searchTerm)) {
                return false;
            }
        }
        
        // Category filter
        if (filters.category && transaction.category !== filters.category) {
            return false;
        }
        
        return true;
    });
};

/**
 * Generate transaction report data
 * @param {Array} transactions - Array of transactions
 * @param {string} reportType - Type of report (summary, monthly, category)
 * @returns {Object} Report data
 */
export const generateTransactionReport = (transactions, reportType = 'summary') => {
    if (!Array.isArray(transactions) || transactions.length === 0) {
        return { data: [], totals: {}, chartData: [] };
    }
    
    const filteredTransactions = transactions.filter(t => t.status === 'COMPLETED');
    
    switch (reportType.toLowerCase()) {
        case 'monthly':
            return generateMonthlyReport(filteredTransactions);
        case 'category':
            return generateCategoryReport(filteredTransactions);
        case 'user':
            return generateUserReport(filteredTransactions);
        default:
            return generateSummaryReport(filteredTransactions);
    }
};

/**
 * Generate summary report
 */
const generateSummaryReport = (transactions) => {
    const summary = calculateTransactionSummary(transactions);
    
    return {
        data: transactions,
        totals: summary,
        chartData: [
            { name: 'Completed', value: summary.completed },
            { name: 'Pending', value: summary.pending },
            { name: 'Failed', value: summary.failed }
        ]
    };
};

/**
 * Generate monthly report
 */
const generateMonthlyReport = (transactions) => {
    const monthlyData = {};
    
    transactions.forEach(transaction => {
        const date = new Date(transaction.createdAt || transaction.date);
        const monthYear = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        
        if (!monthlyData[monthYear]) {
            monthlyData[monthYear] = {
                month: monthYear,
                count: 0,
                totalAmount: 0,
                transactions: []
            };
        }
        
        monthlyData[monthYear].count++;
        monthlyData[monthYear].totalAmount += transaction.amount || 0;
        monthlyData[monthYear].transactions.push(transaction);
    });
    
    const monthlyArray = Object.values(monthlyData);
    monthlyArray.sort((a, b) => a.month.localeCompare(b.month));
    
    return {
        data: monthlyArray,
        totals: calculateTransactionSummary(transactions),
        chartData: monthlyArray.map(month => ({
            name: month.month,
            value: month.totalAmount
        }))
    };
};

/**
 * Generate category report
 */
const generateCategoryReport = (transactions) => {
    const categoryData = {};
    
    transactions.forEach(transaction => {
        const category = transaction.category || 'Uncategorized';
        
        if (!categoryData[category]) {
            categoryData[category] = {
                category,
                count: 0,
                totalAmount: 0,
                transactions: []
            };
        }
        
        categoryData[category].count++;
        categoryData[category].totalAmount += transaction.amount || 0;
        categoryData[category].transactions.push(transaction);
    });
    
    const categoryArray = Object.values(categoryData);
    categoryArray.sort((a, b) => b.totalAmount - a.totalAmount);
    
    return {
        data: categoryArray,
        totals: calculateTransactionSummary(transactions),
        chartData: categoryArray.map(cat => ({
            name: cat.category,
            value: cat.totalAmount
        }))
    };
};

/**
 * Generate user report
 */
const generateUserReport = (transactions) => {
    const userData = {};
    
    transactions.forEach(transaction => {
        const userId = transaction.userId;
        
        if (!userData[userId]) {
            userData[userId] = {
                userId,
                count: 0,
                totalAmount: 0,
                transactions: []
            };
        }
        
        userData[userId].count++;
        userData[userId].totalAmount += transaction.amount || 0;
        userData[userId].transactions.push(transaction);
    });
    
    const userArray = Object.values(userData);
    userArray.sort((a, b) => b.totalAmount - a.totalAmount);
    
    return {
        data: userArray,
        totals: calculateTransactionSummary(transactions),
        chartData: userArray.slice(0, 10).map(user => ({
            name: user.userId,
            value: user.totalAmount
        }))
    };
};

/**
 * Parse error response from API
 * @param {Error} error - Error object
 * @returns {Object} Parsed error information
 */
export const parseTransactionError = (error) => {
    if (!error.message) {
        return {
            message: 'An unknown error occurred',
            code: 500,
            requestId: null
        };
    }
    
    // Extract requestId if present in error message
    let requestId = null;
    const requestIdMatch = error.message.match(/requestId: ([a-f0-9-]+)/i);
    if (requestIdMatch) {
        requestId = requestIdMatch[1];
    }
    
    // Parse HTTP error codes from message
    const errorMatch = error.message.match(/Error (\d+): (.+)/);
    if (errorMatch) {
        return {
            message: errorMatch[2],
            code: parseInt(errorMatch[1], 10),
            requestId
        };
    }
    
    // Parse specific error types
    if (error.message.includes('Unauthorized')) {
        return {
            message: error.message.replace('Unauthorized: ', ''),
            code: 401,
            requestId
        };
    }
    
    if (error.message.includes('Forbidden')) {
        return {
            message: error.message.replace('Forbidden: ', ''),
            code: 403,
            requestId
        };
    }
    
    if (error.message.includes('Not found')) {
        return {
            message: error.message.replace('Transaction not found: ', ''),
            code: 404,
            requestId
        };
    }
    
    if (error.message.includes('Bad Request')) {
        return {
            message: error.message.replace('Bad Request: ', ''),
            code: 400,
            requestId
        };
    }
    
    if (error.message.includes('Conflict')) {
        return {
            message: error.message.replace('Conflict: ', ''),
            code: 409,
            requestId
        };
    }
    
    return {
        message: error.message,
        code: 500,
        requestId
    };
};

/**
 * Create success response object
 * @param {Object} data - Response data
 * @param {string} message - Success message
 * @param {string} requestId - Request ID
 * @returns {Object} Formatted success response
 */
export const createSuccessResponse = (data, message = 'Success', requestId = null) => {
    return {
        responseCode: 200,
        message,
        data,
        requestId: requestId || generateRequestId(),
        timestamp: new Date().toISOString()
    };
};

/**
 * Generate a request ID
 * @returns {string} Request ID
 */
export const generateRequestId = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
};