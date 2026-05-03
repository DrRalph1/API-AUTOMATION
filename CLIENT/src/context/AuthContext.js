import React, {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  useEffect
} from "react";
import { useLog } from "@/context/LogContext";

import {
  login as apiLogin,
  registerUsers as apiRegister,
  resetPassword,
  updateUserDetails,
  deleteUser,
  findUser,
  getUsers
} from "../controllers/AuthController.js";

import {
  verifyOTP,
  sendOTP
} from "../controllers/OTPController.js";

const AuthContext = createContext(null);

// ============================================
// CONFIGURATION - Make this configurable
// ============================================
const AUTH_CONFIG = {
  // Set to false for memory-only storage (current behavior)
  // Set to true for persistent storage across page refreshes
  ENABLE_PERSISTENCE: true,
  
  // Token expiry duration (in milliseconds) - fallback if JWT doesn't have expiry
  TOKEN_EXPIRY_DURATION: 24 * 60 * 60 * 1000, // 24 hours
  
  // Storage type: 'localStorage' or 'sessionStorage'
  STORAGE_TYPE: 'localStorage',
  
  // Enable token expiry check (uses JWT expiry if available)
  // SET TO FALSE to prevent frontend from logging out
  CHECK_TOKEN_EXPIRY: false,
  
  // Enable automatic token refresh
  ENABLE_AUTO_REFRESH: true,
  
  // Refresh threshold (refresh token when this much time remaining)
  REFRESH_THRESHOLD: 5 * 60 * 1000, // 5 minutes
};

// Storage keys
const STORAGE_KEYS = {
  USER: 'auth_user',
  TOKEN: 'auth_token',
  ROLE: 'auth_role',
  TOKEN_EXPIRY: 'auth_token_expiry'
};

// ============================================
// JWT Helper Functions
// ============================================
const getTokenExpiryFromJWT = (token) => {
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    
    const payload = JSON.parse(atob(parts[1]));
    return payload.exp ? payload.exp * 1000 : null;
  } catch (error) {
    console.error('Failed to decode JWT:', error);
    return null;
  }
};

const isJWTExpired = (token) => {
  const expiry = getTokenExpiryFromJWT(token);
  if (!expiry) return false;
  return new Date().getTime() + 5000 > expiry;
};

// ============================================
// Storage Helpers
// ============================================
const getStorage = () => {
  return AUTH_CONFIG.STORAGE_TYPE === 'sessionStorage' ? sessionStorage : localStorage;
};

const saveToStorage = (key, value) => {
  if (!AUTH_CONFIG.ENABLE_PERSISTENCE) return;
  
  try {
    const storage = getStorage();
    if (value !== null && value !== undefined) {
      storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
    } else {
      storage.removeItem(key);
    }
  } catch (error) {
    console.error(`Failed to save ${key} to storage:`, error);
  }
};

const getFromStorage = (key, isJson = false) => {
  if (!AUTH_CONFIG.ENABLE_PERSISTENCE) return null;
  
  try {
    const storage = getStorage();
    const value = storage.getItem(key);
    if (!value) return null;
    return isJson ? JSON.parse(value) : value;
  } catch (error) {
    console.error(`Failed to retrieve ${key} from storage:`, error);
    return null;
  }
};

const clearAuthStorage = () => {
  if (!AUTH_CONFIG.ENABLE_PERSISTENCE) return;
  
  try {
    const storage = getStorage();
    Object.values(STORAGE_KEYS).forEach(key => {
      storage.removeItem(key);
    });
  } catch (error) {
    console.error('Failed to clear auth storage:', error);
  }
};

// ============================================
// Token Management Helpers - FIXED
// ============================================
const setTokenExpiry = () => {
  if (!AUTH_CONFIG.ENABLE_PERSISTENCE || !AUTH_CONFIG.CHECK_TOKEN_EXPIRY) return;
  
  const token = getFromStorage(STORAGE_KEYS.TOKEN);
  if (token) {
    const jwtExpiry = getTokenExpiryFromJWT(token);
    if (jwtExpiry) {
      saveToStorage(STORAGE_KEYS.TOKEN_EXPIRY, jwtExpiry.toString());
      return;
    }
  }
  
  // Only set fallback expiry if we have a token
  const currentToken = getFromStorage(STORAGE_KEYS.TOKEN);
  if (currentToken) {
    const expiryTime = new Date().getTime() + AUTH_CONFIG.TOKEN_EXPIRY_DURATION;
    saveToStorage(STORAGE_KEYS.TOKEN_EXPIRY, expiryTime.toString());
  }
};

const isTokenExpired = () => {
  if (!AUTH_CONFIG.ENABLE_PERSISTENCE || !AUTH_CONFIG.CHECK_TOKEN_EXPIRY) return false;
  
  let tokenExpiry = getFromStorage(STORAGE_KEYS.TOKEN_EXPIRY);
  
  if (!tokenExpiry) {
    const token = getFromStorage(STORAGE_KEYS.TOKEN);
    if (token) {
      const jwtExpiry = getTokenExpiryFromJWT(token);
      if (jwtExpiry) {
        tokenExpiry = jwtExpiry.toString();
        saveToStorage(STORAGE_KEYS.TOKEN_EXPIRY, tokenExpiry);
      }
    }
  }
  
  if (!tokenExpiry) return false;
  
  const buffer = 5000;
  const currentTime = new Date().getTime();
  const expiryTime = parseInt(tokenExpiry);
  
  return currentTime + buffer > expiryTime;
};

export function AuthProvider({ children, config = {} }) {
  const { log } = useLog();
  
  const effectiveConfig = { ...AUTH_CONFIG, ...config };

  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const [persistenceMode, setPersistenceMode] = useState(effectiveConfig.ENABLE_PERSISTENCE ? 'persistent' : 'memory');

  // Setup global token update function
  useEffect(() => {
    if (typeof window !== 'undefined') {
      window.updateAuthToken = (newToken) => {
        console.log('🔄 Updating token via global interceptor');
        setAuthToken(newToken);
        if (effectiveConfig.ENABLE_PERSISTENCE) {
          saveToStorage(STORAGE_KEYS.TOKEN, newToken);
          setTokenExpiry();
        }
      };
    }
    
    return () => {
      if (typeof window !== 'undefined') {
        delete window.updateAuthToken;
      }
    };
  }, [effectiveConfig.ENABLE_PERSISTENCE]);

  /** ✅ LOAD PERSISTED SESSION ON MOUNT */
  useEffect(() => {
    const loadPersistedSession = () => {
      try {
        if (!effectiveConfig.ENABLE_PERSISTENCE) {
          console.log("ℹ️ Auth running in MEMORY-ONLY mode (no persistence)");
          setIsInitialized(true);
          return;
        }

        console.log("🔄 Loading persisted session from storage...");
        
        const storedToken = getFromStorage(STORAGE_KEYS.TOKEN);
        const storedUser = getFromStorage(STORAGE_KEYS.USER, true);
        const storedRole = getFromStorage(STORAGE_KEYS.ROLE);

        if (storedToken && storedUser && storedRole) {
          if (effectiveConfig.CHECK_TOKEN_EXPIRY) {
            const isExpired = isJWTExpired(storedToken);
            if (isExpired) {
              console.log("⚠️ Token expired based on JWT, clearing session");
              clearAuthStorage();
              return;
            }
            const jwtExpiry = getTokenExpiryFromJWT(storedToken);
            if (jwtExpiry) {
              saveToStorage(STORAGE_KEYS.TOKEN_EXPIRY, jwtExpiry.toString());
            }
          }

          console.log("✅ Restored session from storage");
          setToken(storedToken);
          setUser(storedUser);
          setRole(storedRole);
        } else {
          console.log("ℹ️ No persisted session found");
        }
      } catch (error) {
        console.error("❌ Failed to load persisted session:", error);
        clearAuthStorage();
      } finally {
        setIsInitialized(true);
      }
    };

    loadPersistedSession();
  }, [effectiveConfig.ENABLE_PERSISTENCE, effectiveConfig.CHECK_TOKEN_EXPIRY]);

  /** ✅ PERSIST SESSION CHANGES */
  useEffect(() => {
    if (!isInitialized) return;
    
    if (effectiveConfig.ENABLE_PERSISTENCE) {
      if (token) {
        saveToStorage(STORAGE_KEYS.TOKEN, token);
        setTokenExpiry();
      } else {
        saveToStorage(STORAGE_KEYS.TOKEN, null);
        if (effectiveConfig.CHECK_TOKEN_EXPIRY) {
          saveToStorage(STORAGE_KEYS.TOKEN_EXPIRY, null);
        }
      }
      
      if (user) {
        saveToStorage(STORAGE_KEYS.USER, user);
      } else {
        saveToStorage(STORAGE_KEYS.USER, null);
      }
      
      if (role) {
        saveToStorage(STORAGE_KEYS.ROLE, role);
      } else {
        saveToStorage(STORAGE_KEYS.ROLE, null);
      }
    }
  }, [user, token, role, isInitialized, effectiveConfig.ENABLE_PERSISTENCE]);

  /** ✅ SAFE SETTERS */
  const setAuthUser = useCallback((userData) => {
    console.log("🔄 Updating user in context");
    setUser(userData);
  }, []);

  const setAuthToken = useCallback((newToken) => {
    console.log("🔄 Updating token in context");
    setToken(newToken);
  }, []);

  const setUserRole = useCallback((newRole) => {
    console.log("🔄 Updating role in context");
    setRole(newRole);
  }, []);

  /** ✅ REFRESH TOKEN */
  const refreshToken = useCallback(async () => {
    if (!token) return null;
    if (!effectiveConfig.ENABLE_AUTO_REFRESH) return token;
    
    try {
      const { refreshToken: refreshTokenApi } = await import("../controllers/AuthController.js");
      const response = await refreshTokenApi(token);
      
      if (response?.data?.token) {
        const newToken = response.data.token;
        setAuthToken(newToken);
        if (effectiveConfig.ENABLE_PERSISTENCE) {
          saveToStorage(STORAGE_KEYS.TOKEN, newToken);
          setTokenExpiry();
        }
        return newToken;
      }
      return token;
    } catch (error) {
      console.error("Token refresh failed:", error);
      return null;
    }
  }, [token, effectiveConfig.ENABLE_AUTO_REFRESH]);

  /** ✅ LOGIN VIA API */
  const login = useCallback(async (credentials, apiKey, apiSecret) => {
    const response = await apiLogin(credentials, apiKey, apiSecret);

    if (response?.responseCode !== 200 || !response?.data) {
      throw new Error(response?.message || "Login failed");
    }

    return await loginWithUserData(response.data);
  }, []);

  /** ✅ SET USER AFTER LOGIN */
const loginWithUserData = useCallback(async (userData) => {
  if (!userData) throw new Error("User data required");

  // Get token first
  const authToken = userData.token || userData.authToken;
  
  console.log('🔐 loginWithUserData - Token received:', authToken ? `${authToken.substring(0, 50)}...` : 'NO TOKEN');
  console.log('🔐 Token expiry from JWT:', getTokenExpiryFromJWT(authToken));

  // CRITICAL FIX: Clean the token (remove any Bearer prefix if present)
  const cleanToken = authToken?.replace(/^Bearer\s+/i, '');
  
  const formattedUser = {
    id: userData.id || userData.user_id,
    userId: userData.user_id || userData.userId,
    staffId: userData.staff_id || "",
    userGroup: userData.user_group || "user",
    role: userData.role || "user",
    roleId: userData.role_id || "",
    name: userData.username || userData.name,
    email: userData.email || "",
    phoneNumber: userData.phone_number || userData.phoneNumber || "",
    lastLogin: userData.last_login || new Date().toISOString(),
    isActive: userData.is_active !== false,
    isDefaultPassword: userData.is_default_password || false,
    failedLoginAttempts: userData.failed_login_attempts || 0,
    createdAt: userData.created_at || new Date().toISOString(),
    updatedAt: userData.updated_at || new Date().toISOString(),
    authenticated: true,
  };
  
  // ============ IMPORTANT FIX: Save to storage FIRST ============
  if (effectiveConfig.ENABLE_PERSISTENCE) {
    console.log('💾 Saving to storage...');
    saveToStorage(STORAGE_KEYS.TOKEN, cleanToken);
    saveToStorage(STORAGE_KEYS.USER, formattedUser);
    saveToStorage(STORAGE_KEYS.ROLE, formattedUser.role);
    setTokenExpiry();
    
    // Verify token was saved
    const verifyToken = getFromStorage(STORAGE_KEYS.TOKEN);
    console.log('✅ Token saved to storage, verification:', verifyToken ? `${verifyToken.substring(0, 30)}...` : 'FAILED');
  }
  
  // Then update state
  setAuthToken(cleanToken);
  setAuthUser(formattedUser);
  setUserRole(formattedUser.role);

  // Force a small delay to ensure state updates are processed
  await new Promise(resolve => setTimeout(resolve, 0));

  log({
    level: "success",
    category: "auth",
    action: "login",
    message: `Login successful for ${formattedUser.name}`,
    user: formattedUser.userId
  });

  return formattedUser;
}, [log, setAuthUser, setAuthToken, setUserRole, effectiveConfig.ENABLE_PERSISTENCE]);

  /** ✅ REGISTER */
  const register = useCallback(async (authorizationHeader, userData) => {
    const userArray = Array.isArray(userData) ? userData : [userData];
    const response = await apiRegister(authorizationHeader, userArray);

    if (response?.responseCode !== 200) {
      throw new Error(response?.message || "Registration failed");
    }

    log({
      level: "success",
      category: "auth",
      action: "register",
      message: `User registration successful`,
      user: userData?.userId
    });

    return response;
  }, [log]);

  /** ✅ OTP */
  const forgotPassword = useCallback(async (userId, apiKey, apiSecret) => {
    return await sendOTP(userId, apiKey, apiSecret);
  }, []);

  const verifyOtpCode = useCallback(async (otpData, token) => {
    return await verifyOTP(otpData, token);
  }, []);

  const resetUserPassword = useCallback(async (resetData, apiKey, apiSecret) => {
    return await resetPassword(resetData, apiKey, apiSecret);
  }, []);

  /** ✅ LOGOUT */
  const logout = useCallback(() => {
    const userId = user?.userId;

    setAuthUser(null);
    setAuthToken(null);
    setUserRole(null);
    
    if (effectiveConfig.ENABLE_PERSISTENCE) {
      clearAuthStorage();
    }

    log({
      level: "audit",
      category: "auth",
      action: "logout",
      message: "User logged out",
      user: userId
    });
  }, [log, user?.userId, setAuthUser, setAuthToken, setUserRole, effectiveConfig.ENABLE_PERSISTENCE]);

  /** ✅ Check if user is authenticated */
  const isAuthenticated = useMemo(() => {
    if (!token || !user) return false;
    
    if (effectiveConfig.ENABLE_PERSISTENCE && effectiveConfig.CHECK_TOKEN_EXPIRY) {
      if (isTokenExpired()) {
        logout();
        return false;
      }
    }
    
    return true;
  }, [token, user, logout, effectiveConfig.ENABLE_PERSISTENCE, effectiveConfig.CHECK_TOKEN_EXPIRY]);

  /** ✅ Get persistence info */
  const getPersistenceInfo = useCallback(() => {
    return {
      enabled: effectiveConfig.ENABLE_PERSISTENCE,
      storageType: effectiveConfig.STORAGE_TYPE,
      tokenExpiryEnabled: effectiveConfig.CHECK_TOKEN_EXPIRY,
      tokenExpiryDuration: effectiveConfig.TOKEN_EXPIRY_DURATION,
      autoRefreshEnabled: effectiveConfig.ENABLE_AUTO_REFRESH
    };
  }, [effectiveConfig]);

  /** ✅ TOKENIZED API */
  const api = useMemo(() => ({
    getUsers: (page, size) => getUsers(token, page, size),
    findUser: (userId) => findUser(userId, token),
    updateUserDetails: (userData) => updateUserDetails(token, userData),
    deleteUser: (userId) => deleteUser(token, userId)
  }), [token]);


  // Add this to your AuthContext.js (add to the returned value)
const debugAuthState = useCallback(() => {
  const storedToken = getFromStorage(STORAGE_KEYS.TOKEN);
  const storedUser = getFromStorage(STORAGE_KEYS.USER, true);
  const storedRole = getFromStorage(STORAGE_KEYS.ROLE);
  
  console.log('🔍 DEBUG - Auth State:', {
    persistenceEnabled: effectiveConfig.ENABLE_PERSISTENCE,
    storageType: effectiveConfig.STORAGE_TYPE,
    tokenInState: !!token,
    tokenInStorage: !!storedToken,
    tokenValue: storedToken ? `${storedToken.substring(0, 30)}...` : 'none',
    userInState: !!user,
    userInStorage: !!storedUser,
    roleInState: !!role,
    roleInStorage: !!storedRole,
    isInitialized,
    isAuthenticated: isAuthenticated
  });
  
  return { tokenInState: !!token, tokenInStorage: !!storedToken };
}, [token, user, role, isInitialized, isAuthenticated, effectiveConfig.ENABLE_PERSISTENCE, effectiveConfig.STORAGE_TYPE]);

  /** ✅ CONTEXT VALUE */
  const value = useMemo(() => ({
    user,
    token,
    role,
    debugAuthState,
    isInitialized,
    isAuthenticated,
    persistenceMode,

    setUser: setAuthUser,
    setToken: setAuthToken,
    setRole: setUserRole,

    login,
    loginWithUserData,
    register,
    forgotPassword,
    verifyOtpCode,
    resetUserPassword,
    logout,
    refreshToken,
    
    getPersistenceInfo,
    clearStorage: clearAuthStorage,

    api
  }), [
    user,
    token,
    role,
    isInitialized,
    isAuthenticated,
    persistenceMode,
    login,
    loginWithUserData,
    register,
    forgotPassword,
    verifyOtpCode,
    resetUserPassword,
    logout,
    refreshToken,
    getPersistenceInfo,
    api
  ]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/** ✅ SAFE HOOK */
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}