// controllers/UserManagementController.js
import { apiCall } from "@/helpers/APIHelper";
import { API_CONFIG } from "@/config/APIConfig";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to get API key headers using config
const getApiKeyHeaders = () => ({
  "x-api-key": API_CONFIG.HEADERS["x-api-key"],
  "x-api-secret": API_CONFIG.HEADERS["x-api-secret"],
  "x-forwarded-for": API_CONFIG.HEADERS["x-forwarded-for"],
  "Content-Type": "application/json"
});


// Helper to extract token from Authorization header
const extractTokenFromHeader = (authorizationHeader) => {
    if (authorizationHeader && authorizationHeader.startsWith('Bearer ')) {
        return authorizationHeader.substring(7);
    }
    return authorizationHeader;
};

/**
 * API call wrapper with token refresh
 */
const apiCallWithTokenRefresh = async (authorizationHeader, apiCallFunction, ...args) => {
  try {
    // First, try the API call with the current token
    return await apiCallFunction(authorizationHeader, ...args);
  } catch (error) {
    const isUnauthorized =
      error?.responseCode === 401 ||
      error?.response?.status === 401 ||
      error?.message?.toLowerCase().includes("unauthorized");

    // Only attempt token refresh if we got a 401 error
    if (!isUnauthorized) {
      throw error;
    }

    console.warn(`🔐 401 detected → attempting token refresh...`);

    try {
      // Extract token and refresh
      const token = extractTokenFromHeader(authorizationHeader);
      const refreshResponse = await refreshToken(token);

      if (!refreshResponse.data?.token) {
        throw new Error("Refresh failed - no token received");
      }

      // Update auth header with new token
      const newAuthHeader = `Bearer ${refreshResponse.data.token}`;
      console.log("✅ Token refreshed via 401, retrying request...");
      
      // Update global token state if possible
      if (typeof window !== 'undefined' && window.updateAuthToken) {
        window.updateAuthToken(refreshResponse.data.token);
      }
      
      // Retry the original API call with new token
      return await apiCallFunction(newAuthHeader, ...args);
    } catch (refreshError) {
      console.error("❌ Token refresh failed after 401:", refreshError);
      
      // Check if it's the specific "Invalid refresh token" error
      if (refreshError.message?.includes("Invalid refresh token") || 
          refreshError.responseCode === 401) {
        throw new Error("Session expired. Please login again.");
      }
      throw refreshError;
    }
  }
};

// Helper function to build query parameters
const buildQueryParams = (params = {}) => {
    const queryParams = new URLSearchParams();
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            queryParams.append(key, params[key]);
        }
    });
    return queryParams;
};

// ============ AUTHENTICATION METHODS ============

// User login - UPDATED to match Java controller
export const userLogin = async (loginRequest, apiKey, apiSecret) => {
    console.log('🔐 Login request payload:', JSON.stringify(loginRequest, null, 2));
    
    try {
        const response = await apiCall(`/users/login`, {
            method: 'POST',
            headers: getApiKeyHeaders(apiKey, apiSecret),
            body: JSON.stringify(loginRequest)
        });
        
        console.log('🔐 Login API raw response:', response);
        return response;
    } catch (error) {
        console.error('🔐 Login API error:', error);
        throw error;
    }
};

// Alternative login method
export const login = async (loginRequest, apiKey, apiSecret) => {
    return userLogin(loginRequest, apiKey, apiSecret);
};

// Refresh Token - Assuming this is a different endpoint
export const refreshToken = async (refreshToken) => {
    return apiCall(`/users/refresh-token`, {
        method: 'POST',
        body: JSON.stringify({ refresh_token: refreshToken })
    });
};

// Check Token Expiry - Assuming this is a different endpoint
export const checkTokenExpiry = async (token) => {
    return apiCall(`/users/check-token-expiry`, {
        method: 'POST',
        body: JSON.stringify({ token: token })
    });
};

// Reset default password - UPDATED to match Java controller
export const resetDefaultPassword = async (authorizationHeader, resetRequest) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users/reset-default-password`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(resetRequest)
        })
    );
};

// ============ USER MANAGEMENT METHODS ============

// Get all users - UPDATED to match Java controller (no API key required, uses JWT)
export const getAllUsers = async (authorizationHeader, page = 0, size = 10, sort = 'username,asc') => {
    const params = buildQueryParams({ page, size, sort });
    const queryString = params.toString() ? `?${params.toString()}` : '';
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users/${queryString}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// Alternative get users method
export const getUsers = async (authorizationHeader) => {
    return getAllUsers(authorizationHeader);
};

// Get single user by ID - NEW: Uses API key authentication (not JWT)
export const findUserById = async (userId, apiKey, apiSecret) => {
    return apiCall(`/users/${userId}`, {
        method: 'GET',
        headers: getApiKeyHeaders(apiKey, apiSecret)
    });
};

// Alternative find user method
export const findUser = async (userId, apiKey, apiSecret) => {
    return findUserById(userId, apiKey, apiSecret);
};

// Search users with filters - UPDATED to match Java controller
export const searchUsers = async (authorizationHeader, filters = {}, page = 0, size = 10, sort = 'username,asc') => {
    const params = buildQueryParams({
        ...filters,
        page,
        size,
        sort
    });
    const queryString = params.toString() ? `?${params.toString()}` : '';
    
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users/search${queryString}`, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// Create user - UPDATED endpoint to match Java controller
export const createUser = async (authorizationHeader, userDTO) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(userDTO)
        })
    );
};

// Bulk user registration - UPDATED endpoint
export const registerUsers = async (authorizationHeader, userRequestDTOs) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(userRequestDTOs)
        })
    );
};

// Update user details - UPDATED endpoint
export const updateUser = async (authorizationHeader, userId, userUpdateDTO) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users/${userId}`, {
            method: 'PUT',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(userUpdateDTO)
        })
    );
};

// Alternative update user method
export const updateUserDetails = async (authorizationHeader, userId, userData) => {
    return updateUser(authorizationHeader, userId, userData);
};

// Delete user - UPDATED endpoint
export const deleteUser = async (authorizationHeader, userId) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users/${userId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// User password reset - UPDATED to use API key authentication (not JWT)
export const resetPassword = async (passwordResetRequest, apiKey, apiSecret) => {
    return apiCall(`/users/password-reset`, {
        method: 'PUT',
        headers: getApiKeyHeaders(apiKey, apiSecret),
        body: JSON.stringify(passwordResetRequest)
    });
};

// User forgot password - UPDATED to use API key authentication (not JWT)
export const forgotPassword = async (forgotPasswordRequest, apiKey, apiSecret) => {
    return apiCall(`/users/forgot-password`, {
        method: 'PUT',
        headers: getApiKeyHeaders(apiKey, apiSecret),
        body: JSON.stringify(forgotPasswordRequest)
    });
};

// ============ NOTIFICATION METHODS ============

// Send SMS - UPDATED endpoint to match Java controller
export const sendSMS = async (authorizationHeader, smsRequest) => {
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(`/users/send-sms`, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(smsRequest)
        })
    );
};

// Note: The Java controller doesn't show an email endpoint, so removing sendEmail

// ============ RESPONSE HANDLERS & UTILITIES ============

// Response helper to handle standardized API responses
export const handleAuthResponse = (response) => {
    if (!response) {
        throw new Error('No response received from authentication service');
    }

    // Check if response has responseCode (from Java controller)
    if (response.responseCode === 200 || response.responseCode === 201) {
        return response.data || response;
    }

    switch (response.responseCode) {
        case 204: // No content
            return {
                ...response,
                message: response.message || 'No data found'
            };
        case 207: // Partial success for bulk operations
            return {
                ...response,
                partialSuccess: true,
                message: response.message || 'Partial success - some operations completed'
            };
        case 400: throw new Error(`Bad Request: ${response.message}`);
        case 401: throw new Error(`Unauthorized: ${response.message}`);
        case 403: throw new Error(`Forbidden: ${response.message}`);
        case 404: throw new Error(`Not Found: ${response.message}`);
        case 409: throw new Error(`Conflict: ${response.message}`);
        case 422: throw new Error(`Business Rule Violation: ${response.message}`);
        case 423: throw new Error(`Account Locked: ${response.message}`);
        case 500: throw new Error(`Server Error: ${response.message}`);
        default: throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

// Utility to extract pagination info from response - UPDATED for Java controller structure
export const extractPaginationInfo = (response) => {
    if (!response) return null;

    // Handle different response structures
    if (response.data?.content !== undefined) {
        // Standard paginated response
        const data = response.data;
        return {
            content: data.content || [],
            totalPages: data.totalPages || 0,
            totalElements: data.totalElements || 0,
            size: data.size || 0,
            number: data.number || 0,
            first: data.first || false,
            last: data.last || false,
            empty: data.empty || true,
            numberOfElements: data.numberOfElements || 0,
            sort: data.sort || {}
        };
    } else if (response.data && Array.isArray(response.data)) {
        // Array response
        const data = response.data;
        return {
            content: data,
            totalElements: data.length,
            totalPages: 1,
            size: data.length,
            number: 0,
            first: true,
            last: true,
            empty: data.length === 0,
            numberOfElements: data.length
        };
    } else if (response.pagination) {
        // Java controller returns pagination in separate field
        return {
            content: response.data || [],
            ...response.pagination
        };
    }
    
    return null;
};

// Utility to check if user is authenticated
export const isAuthenticated = (token) => !!token;

// Utility to get user info from token
export const getUserInfoFromToken = (token) => {
    if (!token) return null;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return {
            userId: payload.sub || payload.userId,
            username: payload.username || payload.preferred_username,
            roles: payload.roles || payload.authorities || [],
            permissions: payload.permissions || payload.scope || []
        };
    } catch (error) {
        console.error('Error decoding token:', error);
        return null;
    }
};

// Utility to get current user session info
export const getCurrentUser = async (token) => {
    if (!token) return null;
    try {
        const userInfo = getUserInfoFromToken(token);
        return userInfo;
    } catch (error) {
        console.error('Error getting current user:', error);
        return null;
    }
};

// Validation helpers - UPDATED to match DTO structure
export const validateLoginRequest = (loginData) => {
    const errors = [];
    if (!loginData.userId) errors.push('User ID is required');
    if (!loginData.password) errors.push('Password is required');
    return errors;
};

export const validateUserRegistration = (userData) => {
    const errors = [];
    if (!userData.username) errors.push('Username is required');
    if (!userData.email) errors.push('Email is required');
    else if (!/\S+@\S+\.\S+/.test(userData.email)) errors.push('Email is invalid');
    if (!userData.firstName) errors.push('First name is required');
    if (!userData.lastName) errors.push('Last name is required');
    if (!userData.roleId) errors.push('Role is required');
    return errors;
};

// Request builders - UPDATED to match Java DTO structure
export const buildLoginRequest = (userId, password) => ({ userId, password });
export const buildSMSRequest = (to, message) => ({ to, message });
export const buildPasswordResetRequest = (userId, newPassword, confirmPassword) => ({ 
    userId, 
    newPassword, 
    confirmPassword 
});
export const buildResetDefaultPasswordRequest = (userId, currentPassword, newPassword) => ({
    userId,
    currentPassword,
    newPassword
});
export const buildForgotPasswordRequest = (userId) => ({
    userId
});
export const buildUserDTO = (userData) => ({
    username: userData.username,
    email: userData.email,
    firstName: userData.firstName,
    lastName: userData.lastName,
    phoneNumber: userData.phoneNumber,
    roleId: userData.roleId,
    status: userData.status || 'ACTIVE'
});

// Export token management utilities
export { apiCallWithTokenRefresh };