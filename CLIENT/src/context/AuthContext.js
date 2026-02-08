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
} from "../controllers/UserManagementController.js";

import {
  verifyOTP,
  sendOTP
} from "../controllers/OTPController.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const { log } = useLog();

  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);

  /** ✅ INITIALIZE CONTEXT */
  useEffect(() => {
    console.log("✅ Auth initialized (memory only)");
    setIsInitialized(true);
  }, []);

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

  /** ✅ LOGIN VIA API */
  const login = useCallback(async (credentials) => {
    const response = await apiLogin(credentials);

    if (response?.responseCode !== 200 || !response?.data) {
      throw new Error(response?.message || "Login failed");
    }

    return await loginWithUserData(response.data);
  }, []);

  /** ✅ SET USER AFTER LOGIN */
  const loginWithUserData = useCallback(async (userData) => {
    if (!userData) throw new Error("User data required");

    const formattedUser = {
      id: userData.id || userData.user_id,
      userId: userData.user_id,
      staffId: userData.staff_id || "",
      userGroup: userData.user_group || "user",
      role: userData.role || "user",
      roleId: userData.role_id || "",
      name: userData.username,
      email: userData.email || "",
      phoneNumber: userData.phone_number || "",
      lastLogin: userData.last_login || new Date().toISOString(),
      isActive: userData.is_active !== false,
      isDefaultPassword: userData.is_default_password || false,
      failedLoginAttempts: userData.failed_login_attempts || 0,
      createdAt: userData.created_at || new Date().toISOString(),
      updatedAt: userData.updated_at || new Date().toISOString(),
      authenticated: true
    };

    setAuthUser(formattedUser);
    setAuthToken(userData.token || userData.authToken);
    setUserRole(formattedUser.role);

    log({
      level: "success",
      category: "auth",
      action: "login",
      message: `Login successful for ${formattedUser.name}`,
      user: formattedUser.userId
    });

    return formattedUser;
  }, [log, setAuthUser, setAuthToken, setUserRole]);

  /** ✅ REGISTER */
  const register = useCallback(async (userData) => {
    const userArray = Array.isArray(userData) ? userData : [userData];
    const response = await apiRegister(userArray);

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
  const forgotPassword = useCallback(async (userId) => {
    return await sendOTP(userId);
  }, []);

  const verifyOtpCode = useCallback(async (otpData) => {
    return await verifyOTP(otpData);
  }, []);

  const resetUserPassword = useCallback(async (resetData) => {
    return await resetPassword(resetData);
  }, []);

  /** ✅ LOGOUT */
  const logout = useCallback(() => {
    const userId = user?.userId;

    setAuthUser(null);
    setAuthToken(null);
    setUserRole(null);

    log({
      level: "audit",
      category: "auth",
      action: "logout",
      message: "User logged out",
      user: userId
    });
  }, [log, user?.userId, setAuthUser, setAuthToken, setUserRole]);

  /** ✅ TOKENIZED API */
  const api = useMemo(() => ({
    getUsers: () => getUsers(token),
    findUser: (userId) => findUser(userId, token),
    updateUserDetails: (userData) => updateUserDetails(userData, token),
    deleteUser: (userId) => deleteUser(userId, token)
  }), [token]);

  /** ✅ CONTEXT VALUE */
  const value = useMemo(() => ({
    user,
    token,
    role,
    isInitialized,

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

    api
  }), [
    user,
    token,
    role,
    isInitialized,
    login,
    loginWithUserData,
    register,
    forgotPassword,
    verifyOtpCode,
    resetUserPassword,
    logout,
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
