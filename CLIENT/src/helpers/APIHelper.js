// src/helpers/APIHelper.js
import { API_CONFIG } from "../config/APIConfig.js";

// Prevent multiple redirects
let isRedirecting = false;

// Navigation function (will be set by React Router)
let navigateFunction = null;

// Export function to set navigate from React Router
export const setNavigateFunction = (navigate) => {
  navigateFunction = navigate;
  console.log('✅ Navigate function set in APIHelper');
};

// Reset redirect flag (for testing)
export const resetRedirectFlag = () => {
  isRedirecting = false;
};

// Internal reset function
const resetRedirect = () => {
  setTimeout(() => {
    isRedirecting = false;
  }, 500);
};

// Safe redirect function that uses React Router if available
const safeRedirect = (url) => {
  if (isRedirecting) return;
  isRedirecting = true;
  
  setTimeout(() => {
    if (navigateFunction) {
      console.log('🔀 Redirecting using React Router to:', url);
      navigateFunction('/login', { replace: true });
      resetRedirect();
    } else {
      console.log('⚠️ Navigate function not set, using window.location');
      window.location.href = url;
      resetRedirect();
    }
  }, 100);
};

// Helper to get token from storage
export const getTokenFromStorage = () => {
  let token = localStorage.getItem('auth_token');
  if (!token) {
    token = sessionStorage.getItem('auth_token');
  }
  return token;
};

// Helper to save token to storage
export const saveTokenToStorage = (token) => {
  const storageType = localStorage.getItem('auth_storage_type') || 'localStorage';
  
  if (storageType === 'sessionStorage') {
    if (token) {
      sessionStorage.setItem('auth_token', token);
    } else {
      sessionStorage.removeItem('auth_token');
    }
  } else {
    if (token) {
      localStorage.setItem('auth_token', token);
    } else {
      localStorage.removeItem('auth_token');
    }
  }
};

// Helper to clear all auth storage
export const clearAuthStorage = () => {
  localStorage.removeItem('auth_token');
  localStorage.removeItem('auth_user');
  localStorage.removeItem('auth_role');
  localStorage.removeItem('auth_token_expiry');
  localStorage.removeItem('auth_storage_type');
  sessionStorage.removeItem('auth_token');
  sessionStorage.removeItem('auth_user');
  sessionStorage.removeItem('auth_role');
  sessionStorage.removeItem('auth_token_expiry');
};

// Helper to get auth headers
const getAuthHeaders = () => {
  const token = getTokenFromStorage();
  const headers = { ...API_CONFIG.HEADERS };
  
  if (token) {
    const cleanToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    headers['Authorization'] = cleanToken;
  }
  
  return headers;
};

// Token refresh mechanism
let isRefreshing = false;
let refreshSubscribers = [];

const onRefreshed = (token) => {
  refreshSubscribers.forEach(callback => callback(token));
  refreshSubscribers = [];
};

const addRefreshSubscriber = (callback) => {
  refreshSubscribers.push(callback);
};

const refreshAccessToken = async () => {
  try {
    const oldToken = getTokenFromStorage();
    if (!oldToken) return null;
    
    console.log('🔄 Attempting to refresh token...');
    
    const response = await fetch(`${API_CONFIG.BASE_URL}/users/refresh-token`, {
      method: 'POST',
      headers: {
        ...API_CONFIG.HEADERS,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ refresh_token: oldToken })
    });
    
    if (response.ok) {
      const data = await response.json();
      const newToken = data.data?.token || data.token;
      
      if (newToken) {
        console.log('✅ Token refreshed successfully');
        saveTokenToStorage(newToken);
        
        const expiryTime = new Date().getTime() + (24 * 60 * 60 * 1000);
        const storage = localStorage.getItem('auth_storage_type') === 'sessionStorage' ? sessionStorage : localStorage;
        storage.setItem('auth_token_expiry', expiryTime.toString());
        
        return newToken;
      }
    }
    console.log('❌ Token refresh failed');
    return null;
  } catch (error) {
    console.error('Token refresh error:', error);
    return null;
  }
};

// Helper to check if token is expired
export const isTokenExpired = () => {
  const storage = localStorage.getItem('auth_storage_type') === 'sessionStorage' ? sessionStorage : localStorage;
  const tokenExpiry = storage.getItem('auth_token_expiry');
  if (!tokenExpiry) return true;
  return new Date().getTime() > parseInt(tokenExpiry);
};

// Generic API call function
export const apiCall = async (endpoint, options = {}) => {
  const makeRequest = async (retryCount = 0) => {
    try {
      const headers = { ...getAuthHeaders(), ...options.headers };
      
      if (options.body instanceof FormData) {
        delete headers['Content-Type'];
      }
      
      console.log(`🌐 API Call: ${API_CONFIG.BASE_URL}${endpoint}`, {
        method: options.method || 'GET',
        headers: { ...headers, Authorization: headers.Authorization ? 'Bearer [HIDDEN]' : undefined },
      });

      const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
        headers: headers,
        ...options,
        body: options.body
      });

      const responseText = await response.text();
      let data = {};
      if (responseText) {
        try {
          data = JSON.parse(responseText);
        } catch (e) {
          data = { message: responseText };
        }
      }

      // Handle 401 Unauthorized
      if (response.status === 401 && retryCount === 0 && !options._retry && !isRedirecting) {
        console.log('🔐 401 detected, attempting token refresh...');
        
        if (!isRefreshing) {
          isRefreshing = true;
          try {
            const newToken = await refreshAccessToken();
            isRefreshing = false;
            
            if (newToken) {
              console.log('✅ Token refreshed, retrying request');
              onRefreshed(newToken);
              options._retry = true;
              return await makeRequest(retryCount + 1);
            } else {
              console.log('❌ Token refresh failed');
              clearAuthStorage();
              safeRedirect('/login');
              throw new Error('Session expired. Please login again.');
            }
          } catch (refreshError) {
            isRefreshing = false;
            throw refreshError;
          }
        } else {
          return new Promise((resolve, reject) => {
            addRefreshSubscriber(async (token) => {
              try {
                options._retry = true;
                const retryResponse = await makeRequest(retryCount + 1);
                resolve(retryResponse);
              } catch (err) {
                reject(err);
              }
            });
          });
        }
      }

      if (!response.ok) {
        let errorMessage = "";
        
        switch (response.status) {
          case 400:
            errorMessage = data.message || "Invalid request.";
            break;
          case 401:
            errorMessage = data.message || "Session expired.";
            clearAuthStorage();
            safeRedirect('/login');
            break;
          case 403:
            errorMessage = data.message || "Access denied.";
            break;
          case 404:
            errorMessage = data.message || "Service not found.";
            break;
          case 429:
            errorMessage = "Too many requests. Please wait.";
            break;
          case 500:
            errorMessage = data.message || "Server error.";
            break;
          default:
            errorMessage = data.message || `Request failed (${response.status})`;
        }
        
        const error = new Error(errorMessage);
        error.status = response.status;
        error.data = data;
        throw error;
      }

      console.log(`✅ API Response for ${endpoint}:`, data);
      return data;
      
    } catch (error) {
      if (error.message === 'Request cancelled') throw error;
      
      if (!error.message || error.message.includes("fetch") || error.message.includes("NetworkError")) {
        error.message = "Unable to connect to server. Please check your connection.";
      }
      
      console.error(`❌ API Call failed for ${endpoint}:`, error);
      throw error;
    }
  };

  return makeRequest();
};

// Export default object
export default {
  apiCall,
  setNavigateFunction,
  getTokenFromStorage,
  saveTokenToStorage,
  clearAuthStorage,
  isTokenExpired,
  resetRedirectFlag
};