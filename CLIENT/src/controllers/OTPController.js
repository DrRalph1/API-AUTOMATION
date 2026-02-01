// controllers/OTPController.js
import { API_CONFIG } from "../config/APIConfig.js";
import { apiCall } from "@/helpers/APIHelper";
import { refreshToken } from "./AuthController.js";

// Helper function to get authorization header
const getAuthHeaders = (jwtToken) => ({
  Authorization: `Bearer ${jwtToken}`,
  "Content-Type": "application/json"
});

// Helper to extract token from Authorization header
const extractTokenFromHeader = (authorizationHeader) => {
    if (authorizationHeader && authorizationHeader.startsWith('Bearer ')) {
        return authorizationHeader.substring(7);
    }
    return authorizationHeader; // Return as-is if no Bearer prefix
};

/**
 * SIMPLIFIED API call wrapper - ONLY refreshes on 401 errors
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

    // Add parameters
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            queryParams.append(key, params[key]);
        }
    });

    return queryParams;
};

// ============ OTP METHODS ============

/**
 * Send OTP to user
 * Generate and send One-Time Password via email or SMS
 */
export const sendOTP = async (userId = null, authorizationHeader = null) => {
    const endpoint = '/send-otp';
    
    // If no authorization header provided, make unauthenticated call
    if (!authorizationHeader) {
        const options = {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            }
        };

        if (userId) {
            const queryParams = buildQueryParams({ userId });
            const queryString = queryParams.toString();
            return apiCall(`${endpoint}${queryString ? `?${queryString}` : ''}`, options);
        }

        return apiCall(endpoint, options);
    }

    // Use token refresh for authenticated calls
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => {
            const options = {
                method: 'POST',
                headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
            };

            if (userId) {
                const queryParams = buildQueryParams({ userId });
                const queryString = queryParams.toString();
                return apiCall(`${endpoint}${queryString ? `?${queryString}` : ''}`, options);
            }

            return apiCall(endpoint, options);
        }
    );
};

/**
 * Verify OTP code
 * Validate One-Time Password for user authentication
 */
export const verifyOTP = async (otpData, authorizationHeader = null) => {
    const endpoint = '/verify-otp';
    
    // If no authorization header provided, make unauthenticated call
    if (!authorizationHeader) {
        return apiCall(endpoint, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(otpData)
        });
    }

    // Use token refresh for authenticated calls
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(endpoint, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(otpData)
        })
    );
};

/**
 * Resend OTP code
 * Generate and send a new OTP to the user
 */
export const resendOTP = async (userId = null, authorizationHeader = null) => {
    const endpoint = '/resend-otp';
    
    // If no authorization header provided, make unauthenticated call
    if (!authorizationHeader) {
        const options = {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            }
        };

        if (userId) {
            const queryParams = buildQueryParams({ userId });
            const queryString = queryParams.toString();
            return apiCall(`${endpoint}${queryString ? `?${queryString}` : ''}`, options);
        }

        return apiCall(endpoint, options);
    }

    // Use token refresh for authenticated calls
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => {
            const options = {
                method: 'POST',
                headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
            };

            if (userId) {
                const queryParams = buildQueryParams({ userId });
                const queryString = queryParams.toString();
                return apiCall(`${endpoint}${queryString ? `?${queryString}` : ''}`, options);
            }

            return apiCall(endpoint, options);
        }
    );
};

/**
 * Validate OTP without consuming it
 * Check if OTP is valid without marking it as used
 */
export const validateOTP = async (otpData, authorizationHeader = null) => {
    const endpoint = '/validate-otp';
    
    // If no authorization header provided, make unauthenticated call
    if (!authorizationHeader) {
        return apiCall(endpoint, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(otpData)
        });
    }

    // Use token refresh for authenticated calls
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(endpoint, {
            method: 'POST',
            headers: getAuthHeaders(authHeader.replace('Bearer ', '')),
            body: JSON.stringify(otpData)
        })
    );
};

/**
 * Get OTP status
 * Retrieve current OTP status and metadata for a user
 */
export const getOTPStatus = async (userId, authorizationHeader = null) => {
    const endpoint = `/otp-status/${userId}`;
    
    // If no authorization header provided, make unauthenticated call
    if (!authorizationHeader) {
        return apiCall(endpoint, {
            method: 'GET',
            headers: {
                "Content-Type": "application/json"
            }
        });
    }

    // Use token refresh for authenticated calls
    return apiCallWithTokenRefresh(
        authorizationHeader,
        (authHeader) => apiCall(endpoint, {
            method: 'GET',
            headers: getAuthHeaders(authHeader.replace('Bearer ', ''))
        })
    );
};

// ============ ENHANCED SERVICE METHODS ============

export const OTPService = {
    /**
     * Send OTP with automatic token refresh
     */
    async sendOTP(userId = null, authorizationHeader = null) {
        try {
            const response = await sendOTP(userId, authorizationHeader);
            return {
                success: true,
                ...extractOTPResponse(response)
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                sent: false,
                message: 'Failed to send OTP'
            };
        }
    },

    /**
     * Verify OTP with automatic token refresh
     */
    async verifyOTP(otpData, authorizationHeader = null) {
        try {
            const response = await verifyOTP(otpData, authorizationHeader);
            return {
                success: true,
                ...extractVerificationResult(response)
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                verified: false,
                message: 'OTP verification failed'
            };
        }
    },

    /**
     * Resend OTP with automatic token refresh
     */
    async resendOTP(userId = null, authorizationHeader = null) {
        try {
            const response = await resendOTP(userId, authorizationHeader);
            return {
                success: true,
                ...extractOTPResponse(response)
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                sent: false,
                message: 'Failed to resend OTP'
            };
        }
    },

    /**
     * Validate OTP without consuming it
     */
    async validateOTP(otpData, authorizationHeader = null) {
        try {
            const response = await validateOTP(otpData, authorizationHeader);
            return {
                success: true,
                ...extractValidationResult(response)
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                valid: false,
                message: 'OTP validation failed'
            };
        }
    },

    /**
     * Get OTP status
     */
    async getOTPStatus(userId, authorizationHeader = null) {
        try {
            const response = await getOTPStatus(userId, authorizationHeader);
            return {
                success: true,
                ...extractOTPStatus(response)
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                status: 'unknown',
                expiresAt: null,
                attempts: 0
            };
        }
    },

    /**
     * Complete OTP flow (send + verify)
     */
    async completeOTPFlow(userId, otpCode, authorizationHeader = null) {
        try {
            // First send OTP
            const sendResponse = await this.sendOTP(userId, authorizationHeader);
            
            if (!sendResponse.success) {
                return sendResponse;
            }

            // Then verify OTP
            const verifyResponse = await this.verifyOTP(
                { userId, code: otpCode }, 
                authorizationHeader
            );

            return {
                success: verifyResponse.success,
                sent: sendResponse.sent,
                verified: verifyResponse.verified,
                message: verifyResponse.message,
                timestamp: new Date().toISOString()
            };
        } catch (error) {
            return {
                success: false,
                error: error.message,
                sent: false,
                verified: false,
                message: 'OTP flow failed'
            };
        }
    }
};

// ============ RESPONSE HANDLERS & UTILITIES ============

// Response helper to handle standardized API responses for OTP
export const handleOTPResponse = (response) => {
    if (!response) {
        throw new Error('No response received from OTP service');
    }

    if (response.responseCode === 200 || response.responseCode === 201) {
        return response.data || response;
    }

    // Handle different error codes
    switch (response.responseCode) {
        case 400:
            throw new Error(`Bad Request: ${response.message}`);
        case 401:
            throw new Error(`Unauthorized: ${response.message}`);
        case 404:
            throw new Error(`Not Found: ${response.message}`);
        case 429:
            throw new Error(`Too Many Requests: ${response.message}`);
        case 500:
            throw new Error(`Server Error: ${response.message}`);
        default:
            throw new Error(`Error ${response.responseCode}: ${response.message}`);
    }
};

// Helper to extract OTP response data
export const extractOTPResponse = (response) => {
    if (!response || !response.data) {
        return {
            sent: false,
            message: 'No response received',
            expiresAt: null,
            method: 'unknown'
        };
    }

    const data = response.data;
    
    return {
        sent: data.sent || true,
        message: data.message || 'OTP sent successfully',
        expiresAt: data.expiresAt || null,
        method: data.method || 'email',
        otpId: data.otpId || null,
        timestamp: data.timestamp || new Date().toISOString()
    };
};

// Helper to extract verification result
export const extractVerificationResult = (response) => {
    if (!response || !response.data) {
        return {
            verified: false,
            message: 'Verification failed',
            token: null,
            user: null
        };
    }

    const data = response.data;
    
    return {
        verified: data.verified || false,
        message: data.message || 'OTP verified successfully',
        token: data.token || null,
        user: data.user || null,
        expiresAt: data.expiresAt || null,
        timestamp: data.timestamp || new Date().toISOString()
    };
};

// Helper to extract validation result
export const extractValidationResult = (response) => {
    if (!response || !response.data) {
        return {
            valid: false,
            message: 'Validation failed',
            expiresIn: 0
        };
    }

    const data = response.data;
    
    return {
        valid: data.valid || false,
        message: data.message || 'OTP is valid',
        expiresIn: data.expiresIn || 0,
        attemptsRemaining: data.attemptsRemaining || 0,
        timestamp: data.timestamp || new Date().toISOString()
    };
};

// Helper to extract OTP status
export const extractOTPStatus = (response) => {
    if (!response || !response.data) {
        return {
            status: 'unknown',
            expiresAt: null,
            attempts: 0,
            maxAttempts: 3,
            method: 'unknown'
        };
    }

    const data = response.data;
    
    return {
        status: data.status || 'unknown',
        expiresAt: data.expiresAt || null,
        attempts: data.attempts || 0,
        maxAttempts: data.maxAttempts || 3,
        method: data.method || 'email',
        createdAt: data.createdAt || null,
        timestamp: data.timestamp || new Date().toISOString()
    };
};

// Export types for better TypeScript support
export const OTPMethods = {
    EMAIL: 'email',
    SMS: 'sms',
    APP: 'app'
};

export const OTPStatus = {
    PENDING: 'pending',
    VERIFIED: 'verified',
    EXPIRED: 'expired',
    FAILED: 'failed',
    CANCELLED: 'cancelled'
};

export const OTPPurposes = {
    LOGIN: 'login',
    REGISTRATION: 'registration',
    PASSWORD_RESET: 'password_reset',
    TWO_FACTOR: 'two_factor',
    ACCOUNT_VERIFICATION: 'account_verification'
};

// Utility functions for building requests
export const buildOTPRequest = (userId, method = 'email', purpose = 'login') => {
    return {
        userId: userId,
        method: method,
        purpose: purpose,
        timestamp: new Date().toISOString()
    };
};

export const buildVerificationRequest = (userId, code, purpose = 'login') => {
    return {
        userId: userId,
        code: code,
        purpose: purpose,
        timestamp: new Date().toISOString()
    };
};

export const buildValidationRequest = (userId, code) => {
    return {
        userId: userId,
        code: code,
        timestamp: new Date().toISOString()
    };
};

// Data transformation utilities
export const transformOTPForDisplay = (otpData) => {
    if (!otpData) return null;

    return {
        id: otpData.id,
        userId: otpData.userId,
        method: otpData.method,
        purpose: otpData.purpose,
        status: otpData.status,
        createdAt: otpData.createdAt,
        expiresAt: otpData.expiresAt,
        attempts: otpData.attempts || 0,
        // Formatted fields for display
        formattedCreatedAt: new Date(otpData.createdAt).toLocaleString(),
        formattedExpiresAt: new Date(otpData.expiresAt).toLocaleString(),
        statusColor: getStatusColor(otpData.status),
        methodIcon: getMethodIcon(otpData.method),
        shortPurpose: otpData.purpose?.replace('_', ' ') || 'Unknown'
    };
};

const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
        case 'verified': return 'success';
        case 'pending': return 'warning';
        case 'expired': return 'error';
        case 'failed': return 'error';
        default: return 'default';
    }
};

const getMethodIcon = (method) => {
    switch (method?.toLowerCase()) {
        case 'email': return 'mail';
        case 'sms': return 'message';
        case 'app': return 'smartphone';
        default: return 'lock';
    }
};

// Validation helpers
export const validateOTPRequest = (otpRequest) => {
    const errors = [];

    if (!otpRequest.userId) {
        errors.push('User ID is required');
    }

    if (!otpRequest.method || !Object.values(OTPMethods).includes(otpRequest.method)) {
        errors.push(`Method must be one of: ${Object.values(OTPMethods).join(', ')}`);
    }

    return errors;
};

export const validateVerificationRequest = (verificationRequest) => {
    const errors = [];

    if (!verificationRequest.userId) {
        errors.push('User ID is required');
    }

    if (!verificationRequest.code || verificationRequest.code.trim() === '') {
        errors.push('OTP code is required');
    }

    if (verificationRequest.code && verificationRequest.code.length !== 6) {
        errors.push('OTP code must be 6 digits');
    }

    return errors;
};

// Export for use in other components
export { apiCallWithTokenRefresh };

// Export all functions as default
export default {
    // Core OTP functions
    sendOTP,
    verifyOTP,
    resendOTP,
    validateOTP,
    getOTPStatus,
    
    // Service methods
    OTPService,
    
    // Utility functions
    handleOTPResponse,
    extractOTPResponse,
    extractVerificationResult,
    extractValidationResult,
    extractOTPStatus,
    
    // Transformation utilities
    transformOTPForDisplay,
    
    // Token management
    apiCallWithTokenRefresh
};