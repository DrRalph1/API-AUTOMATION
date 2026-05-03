// hooks/useLogin.js
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useSetRecoilState, useRecoilValue } from 'recoil';
import { tokenAtom } from '@/recoil/tokenAtom';
import { roleAtom } from '@/recoil/roleAtom';
import { 
  login as apiLogin,
  resetDefaultPassword,
  findUser,
  forgotPassword 
} from "../controllers/AuthController.js";
import {
  verifyOTP,
  sendOTP
} from "../controllers/OTPController.js";
import { showError, showSuccess, showWarning } from "@/lib/sweetAlert";
import { API_CONFIG } from "@/config/APIConfig";

export const useLogin = () => {
  const { loginWithUserData, user } = useAuth();
  const navigate = useNavigate();
  
  const token = useRecoilValue(tokenAtom);
  const setToken = useSetRecoilState(tokenAtom);
  const role = useRecoilValue(roleAtom);
  const setRole = useSetRecoilState(roleAtom);

  const [state, setState] = useState({
    userId: "",
    password: "",
    showPassword: false,
    loading: false,
    showOtp: false,
    otp: ["", "", "", "", "", ""],
    loginData: null,
    userType: "user",
    portalValidationError: "",
    lastAttemptedRole: "",
    resendCountdown: 0,
    canResendOtp: false,
    requiresPasswordReset: false,
    newPassword: "",
    confirmPassword: "",
    showNewPassword: false,
    showConfirmPassword: false,
    showForgotPassword: false,
    forgotUserId: "",
    forgotLoading: false,
    isMobile: false
  });

  const inputsRef = useRef([]);

  // State setters
  const setUserId = (value) => setState(prev => ({ ...prev, userId: value }));
  const setPassword = (value) => setState(prev => ({ ...prev, password: value }));
  const setShowPassword = (value) => setState(prev => ({ ...prev, showPassword: value }));
  const setLoading = (value) => setState(prev => ({ ...prev, loading: value }));
  const setShowOtp = (value) => setState(prev => ({ ...prev, showOtp: value }));
  const setOtp = (value) => setState(prev => ({ ...prev, otp: value }));
  const setLoginData = (value) => setState(prev => ({ ...prev, loginData: value }));
  const setUserType = (value) => setState(prev => ({ ...prev, userType: value }));
  const setPortalValidationError = (value) => setState(prev => ({ ...prev, portalValidationError: value }));
  const setLastAttemptedRole = (value) => setState(prev => ({ ...prev, lastAttemptedRole: value }));
  const setResendCountdown = (value) => setState(prev => ({ ...prev, resendCountdown: value }));
  const setCanResendOtp = (value) => setState(prev => ({ ...prev, canResendOtp: value }));
  const setRequiresPasswordReset = (value) => setState(prev => ({ ...prev, requiresPasswordReset: value }));
  const setNewPassword = (value) => setState(prev => ({ ...prev, newPassword: value }));
  const setConfirmPassword = (value) => setState(prev => ({ ...prev, confirmPassword: value }));
  const setShowNewPassword = (value) => setState(prev => ({ ...prev, showNewPassword: value }));
  const setShowConfirmPassword = (value) => setState(prev => ({ ...prev, showConfirmPassword: value }));
  const setShowForgotPassword = (value) => setState(prev => ({ ...prev, showForgotPassword: value }));
  const setForgotUserId = (value) => setState(prev => ({ ...prev, forgotUserId: value }));
  const setForgotLoading = (value) => setState(prev => ({ ...prev, forgotLoading: value }));
  const setIsMobile = (value) => setState(prev => ({ ...prev, isMobile: value }));

  // Helper function to get user role for alerts
  const getUserRoleForAlert = () => {
    let role = state.userType;
    
    if (state.showOtp && state.loginData?.role) {
      role = state.loginData.role;
    }
    
    if (role && typeof role === 'string') {
      return role.toLowerCase() === "system administrator" ? "system administrator" : "user";
    }
    
    console.log("⚠️ getUserRoleForAlert: role is not a string, defaulting to USER", role);
    return "user";
  };

  // Alert functions
  const showCustomError = (title, message) => {
    const userRole = getUserRoleForAlert();
    showError(title, message, userRole, {
      width: state.isMobile ? '90%' : 600,
      padding: state.isMobile ? '1rem' : '2rem',
      showConfirmButton: true,
      showCancelButton: false,
      showLoaderOnConfirm: false,
      allowOutsideClick: false,
      allowEscapeKey: false
    });
  };

  const showCustomSuccess = (title, message) => {
    const userRole = getUserRoleForAlert();
    showSuccess(title, message, userRole, {
      width: state.isMobile ? '90%' : 600,
      padding: state.isMobile ? '1rem' : '2rem',
      showConfirmButton: true,
      showCancelButton: false,
      showLoaderOnConfirm: false,
      allowOutsideClick: false,
      allowEscapeKey: false
    });
  };

  const showCustomWarning = (title, message) => {
    const userRole = getUserRoleForAlert();
    showWarning(title, message, userRole, {
      width: state.isMobile ? '90%' : 600,
      padding: state.isMobile ? '1rem' : '2rem',
      showConfirmButton: true,
      showCancelButton: false,
      showLoaderOnConfirm: false,
      allowOutsideClick: false,
      allowEscapeKey: false
    });
  };

  // Portal validation
  const validatePortalAccess = (userRole, selectedPortal) => {
    console.log(`🔍 Portal Validation: User Role = "${userRole}", Selected Portal = "${selectedPortal}"`);
    
    setLastAttemptedRole(userRole || 'undefined');
    
    if (!userRole || typeof userRole !== 'string') {
      console.log(`⚠️ User role is not a string: ${typeof userRole}`, userRole);
      return {
        valid: true,
        message: "",
        autoCorrected: false
      };
    }
    
    const normalizedRole = userRole.toLowerCase().trim();
    const normalizedPortal = selectedPortal ? selectedPortal.toLowerCase().trim() : '';
    
    console.log(`🔍 Normalized: Role = "${normalizedRole}", Portal = "${normalizedPortal}"`);
    
    const validRoles = ["system administrator", "user"];
    if (!validRoles.includes(normalizedRole)) {
      console.log(`❌ Invalid role detected: "${normalizedRole}"`);
      return {
        valid: false,
        message: `Unknown user role: "${userRole}". Access denied. Please contact system administrator.`,
        autoCorrected: false
      };
    }

    if (normalizedRole !== normalizedPortal) {
      console.log(`⚠️ Role/portal mismatch: ${normalizedRole} != ${normalizedPortal}`);
      console.log(`🔄 Auto-correcting portal from "${selectedPortal}" to match user role "${userRole}"`);
      
      setUserType(userRole);
      
      const correctedPortalName = userRole === "system administrator" ? "Admin Portal" : "USER Portal";
      const userTypeDescription = userRole === "system administrator" ? "an ADMINISTRATOR" : "a USER";
      
      return {
        valid: true,
        message: `You are logged in as ${userTypeDescription}. Redirecting to ${correctedPortalName}.`,
        autoCorrected: true,
        correctedRole: userRole
      };
    }

    console.log("✅ Portal validation passed - role matches selected portal");
    return {
      valid: true,
      message: "",
      autoCorrected: false
    };
  };

  // Password validation
  const validatePasswordResetForm = (values) => {
    const errors = {};
    const password = values.newPassword || '';

    if (!password) {
      errors.newPassword = 'New password is required';
      return errors;
    }

    if (password.length < 12) {
      errors.newPassword = 'Password must be at least 12 characters';
      return errors;
    }

    const commonPatterns = ['123456', 'password', 'qwerty', 'abc123', 'letmein', 'welcome'];
    if (commonPatterns.some(pattern => password.toLowerCase().includes(pattern))) {
      errors.newPassword = 'Password contains common patterns that are easy to guess';
      return errors;
    }

    if (values.userId && password.toLowerCase().includes(values.userId.toLowerCase())) {
      errors.newPassword = 'Password should not contain your user ID';
      return errors;
    }

    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumbers = /\d/.test(password);
    const hasSpecialChars = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    if (!(hasUpperCase && hasLowerCase && hasNumbers && hasSpecialChars)) {
      errors.newPassword = 'Password must include uppercase, lowercase, numbers, and special characters';
      return errors;
    }

    if (/(.)\1{2,}/.test(password)) {
      errors.newPassword = 'Password contains too many repeating characters';
      return errors;
    }

    const keyboardPatterns = ['qwertyuiop', 'asdfghjkl', 'zxcvbnm', '1234567890', '1qaz2wsx', '1q2w3e4r'];
    if (keyboardPatterns.some(pattern => password.toLowerCase().includes(pattern))) {
      errors.newPassword = 'Password contains common keyboard patterns';
      return errors;
    }

    if (!values.confirmPassword) {
      errors.confirmPassword = 'Please confirm your new password';
    } else if (password !== values.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    return errors;
  };

  // Login handler
  const handleLogin = async (e) => {
    e.preventDefault();
    
    // Clear any existing tab parameters from URL to prevent interference
    if (window.location.search.includes('tab=')) {
      const cleanUrl = window.location.pathname;
      window.history.replaceState({}, '', cleanUrl);
      sessionStorage.removeItem('last_active_tab');
    }

    if (!state.userId.trim() || !state.password.trim()) {
      const userRole = getUserRoleForAlert();
      showWarning("Missing Information", "Please enter both your User ID and Password", userRole);
      return;
    }

    try {
      setLoading(true);
      setPortalValidationError("");

      console.log("🔄 Starting login process for user:", state.userId);
      console.log("🎯 Selected portal:", state.userType);

      setToken(null);
      setRole(null);
      setLastAttemptedRole("");

      const loginResponse = await apiLogin({
        user_id: state.userId,
        password: state.password,
        entrySource: "web",
        deviceIp: "",
        channel: "browser",
        authToken: "",
        userName: state.userId,
        deviceId: "web-client",
        deviceName: navigator.userAgent,
        country: "",
        brand: "",
        manufacturer: "",
        phoneNumber: ""
      });

      console.log("📦 Full login response:", loginResponse);

      if (loginResponse.responseCode !== 200) {
        throw new Error(loginResponse.message || "Login failed");
      }

      let userData = loginResponse.data;
      
      if (userData && typeof userData === 'object' && userData.data) {
        userData = userData.data;
      }

      const possibleRoleFields = ['role', 'userType', 'user_role', 'user_role_name', 'type', 'role_name'];
      let userRole = null;
      
      for (const field of possibleRoleFields) {
        if (userData && userData[field]) {
          userRole = typeof userData[field] === 'string' 
            ? userData[field].toLowerCase() 
            : String(userData[field]).toLowerCase();
          console.log(`✅ Found role in field '${field}': ${userRole}`);
          break;
        }
      }
      
      if (!userRole && userData && userData.user) {
        const userObj = userData.user;
        for (const field of possibleRoleFields) {
          if (userObj && userObj[field]) {
            userRole = typeof userObj[field] === 'string' 
              ? userObj[field].toLowerCase() 
              : String(userObj[field]).toLowerCase();
            console.log(`✅ Found role in user.${field}: ${userRole}`);
            break;
          }
        }
      }

      const receivedToken = userData.authToken || userData.token || loginResponse.token;
      
      if (!receivedToken) {
        throw new Error("Login failed - no authentication token received");
      }

      if (!userRole) {
        console.log("⚠️ No role found in response, using selected portal type:", state.userType);
        userRole = state.userType;
        if (userData && typeof userData === 'object') {
          userData.role = state.userType;
        }
      }

      const portalValidation = validatePortalAccess(userRole, state.userType);
      
      if (!portalValidation.valid) {
        setToken(null);
        setRole(null);
        setLoginData(null);
        setPortalValidationError(portalValidation.message);
        showError("Login Denied", portalValidation.message, userRole);
        return;
      }

      setToken(receivedToken);
      setRole(userRole);
      setLoginData(userData);
      setPortalValidationError("");

      const requiresReset = userData.requiresPasswordReset || 
                           userData.is_default_password || 
                           userData.passwordResetRequired;
      
      if (requiresReset) {
        console.log("🔄 Password reset required");
        setRequiresPasswordReset(true);
        return;
      }

      console.log("🔄 Sending OTP for user:", state.userId);
      // Pass raw token (without Bearer prefix)
      await sendOTP(state.userId, receivedToken);
      setShowOtp(true);
      console.log("✅ OTP sent, showing OTP form");

    } catch (err) {
      console.error("❌ Login error:", err);
      setToken(null);
      setLoginData(null);
      setPortalValidationError("");
      const userRole = getUserRoleForAlert();
      showError("Login Failed", err.message, userRole);
    } finally {
      setLoading(false);
    }
  };

  // OTP handlers
  const handleOtpChange = (value, idx) => {
    if (!/^[0-9]*$/.test(value)) return;
    
    const updated = [...state.otp];
    updated[idx] = value.slice(-1);
    setOtp(updated);
    
    if (value && idx < 5) {
      setTimeout(() => {
        const nextInput = inputsRef.current[idx + 1];
        if (nextInput) {
          nextInput.focus();
          nextInput.select();
        }
      }, 10);
    }
  };

  const handleOtpKeyDown = (e, idx) => {
    if (e.key === 'Backspace') {
      if (!state.otp[idx] && idx > 0) {
        e.preventDefault();
        const updated = [...state.otp];
        updated[idx - 1] = "";
        setOtp(updated);
        
        setTimeout(() => {
          const prevInput = inputsRef.current[idx - 1];
          if (prevInput) {
            prevInput.focus();
            prevInput.select();
          }
        }, 10);
      } else if (state.otp[idx]) {
        const updated = [...state.otp];
        updated[idx] = "";
        setOtp(updated);
      }
    } else if (e.key === 'ArrowLeft' && idx > 0) {
      e.preventDefault();
      setTimeout(() => {
        const prevInput = inputsRef.current[idx - 1];
        if (prevInput) {
          prevInput.focus();
          prevInput.select();
        }
      }, 10);
    } else if (e.key === 'ArrowRight' && idx < 5) {
      e.preventDefault();
      setTimeout(() => {
        const nextInput = inputsRef.current[idx + 1];
        if (nextInput) {
          nextInput.focus();
          nextInput.select();
        }
      }, 10);
    }
  };

  const handleResendOtp = async () => {
    if (!state.canResendOtp || !token) {
      const userRole = getUserRoleForAlert();
      showError("Session Expired", "Your session has expired. Please login again.", userRole);
      return;
    }

    try {
      setLoading(true);
      console.log("🔄 Resending OTP for user:", state.userId);

      // Pass raw token
      await sendOTP(state.userId, token);

      setResendCountdown(30);
      setCanResendOtp(false);
      setOtp(["", "", "", "", "", ""]);
      
      setTimeout(() => {
        if (inputsRef.current[0]) {
          inputsRef.current[0].focus();
          inputsRef.current[0].select();
        }
      }, 200);

      const userRole = getUserRoleForAlert();
      showSuccess("Verification Code Sent", "We've sent a new 6-digit code to your registered mobile number. Please check your messages.", userRole);
    } catch (err) {
      console.error("❌ OTP resend error:", err);
      if (err.message?.includes('token') || err.message?.includes('auth') || err.response?.status === 401) {
        setToken(null);
        setRole(null);
        setLoginData(null);
      }
      const userRole = getUserRoleForAlert();
      showError("Resend Failed", err.message || "Failed to resend OTP. Please try again.", userRole);
    } finally {
      setLoading(false);
    }
  };

  const verifyOtp = async (e) => {
    e.preventDefault();
    const code = state.otp.join("");

    if (code.length !== 6) {
      const userRole = getUserRoleForAlert();
      showWarning("Incomplete Code", "Please enter all 6 digits of your verification code.", userRole);
      return;
    }

    if (!token || !state.loginData) {
      const userRole = getUserRoleForAlert();
      showError("Session Expired", "Your session has expired. Please login again.", userRole);
      setShowOtp(false);
      setToken(null);
      setRole(null);
      setPortalValidationError("");
      return;
    }

    try {
      setLoading(true);

      const otpData = {
        user_id: state.userId,
        otp: code
      };

      console.log("🔄 Verifying OTP for user:", state.userId);
      
      // Pass raw token (without Bearer prefix)
      const response = await verifyOTP(otpData, token);

      console.log("✅ OTP verification response:", response);

      if (response.responseCode === 200) {
        const finalUserData = {
          ...state.loginData,
          ...response.data,
          token: token,
          authenticated: true
        };

        console.log("🎯 Final user data for authentication:", finalUserData);

        // ============ CRITICAL FIX: Clear all persisted tab state BEFORE navigation ============
        
        // 1. Clear session storage tab persistence
        sessionStorage.removeItem('last_active_tab');
        
        // 2. Clear any auth redirect flags
        sessionStorage.removeItem('auth_redirect_pending');
        sessionStorage.removeItem('last_login_time');
        
        // 3. Clear URL parameters if present (prevents tab restoration interference)
        if (window.location.search) {
          const cleanUrl = window.location.pathname;
          window.history.replaceState({}, '', cleanUrl);
          console.log("🧹 Cleared URL parameters:", cleanUrl);
        }
        
        // 4. Set a login timestamp to prevent immediate tab restoration
        sessionStorage.setItem('last_login_time', Date.now().toString());
        
        // 5. Optional: Set a flag that we're coming from successful login
        sessionStorage.setItem('just_logged_in', 'true');
        
        // 6. Clear after 3 seconds (enough time for navigation to complete)
        setTimeout(() => {
          sessionStorage.removeItem('just_logged_in');
          sessionStorage.removeItem('last_login_time');
        }, 3000);
        
        // ============ End of fixes ============

        // Set the user data in auth context
        await loginWithUserData(finalUserData);
        
        // Wait longer for auth context to fully update
        await new Promise(resolve => setTimeout(resolve, 0));
        
        // Get the actual role from user data
        const actualRole = finalUserData.role?.toLowerCase();
        
        console.log("🎯 Actual role:", actualRole);
        console.log("🎯 Token exists:", !!token);
        console.log("🎯 User data set, navigating with clean state...");
        
        // Use React Router navigate for smooth transitions
        if (actualRole === "system administrator") {
          navigate('/admin', { replace: true });
        } else if (actualRole === "user") {
          navigate('/user-dashboard', { replace: true });
        } else {
          navigate('/', { replace: true });
        }
      } else {
        console.error("❌ OTP verification failed:", response.message);
        const userRole = getUserRoleForAlert();
        showError("Verification Failed", response.message || "Invalid OTP", userRole);
        
        // Clear OTP fields on failure
        setOtp(["", "", "", "", "", ""]);
        
        // Focus first OTP input
        setTimeout(() => {
          if (inputsRef.current[0]) {
            inputsRef.current[0].focus();
            inputsRef.current[0].select();
          }
        }, 100);
      }
    } catch (err) {
      console.error("❌ OTP verification error:", err);
      
      // Handle specific error cases
      if (err.message?.includes('token') || err.message?.includes('auth') || err.response?.status === 401) {
        setToken(null);
        setRole(null);
        setLoginData(null);
        setPortalValidationError("");
        
        const userRole = getUserRoleForAlert();
        showError("Session Expired", "Your session has expired. Please login again.", userRole);
        
        // Clear any saved state
        sessionStorage.removeItem('last_active_tab');
        
        // Navigate to login smoothly
        setTimeout(() => {
          navigate('/login', { replace: true });
        }, 2000);
      } else if (err.message?.includes('rate limit') || err.message?.includes('too many')) {
        const userRole = getUserRoleForAlert();
        showError("Too Many Attempts", "Too many failed attempts. Please wait 5 minutes before trying again.", userRole);
        
        // Clear OTP and go back to login
        setTimeout(() => {
          setShowOtp(false);
          setOtp(["", "", "", "", "", ""]);
          setToken(null);
          setRole(null);
          setLoginData(null);
          navigate('/login', { replace: true });
        }, 3000);
      } else {
        const userRole = getUserRoleForAlert();
        showError("Verification Failed", err.message || "Failed to verify OTP. Please try again.", userRole);
        
        // Clear OTP fields
        setOtp(["", "", "", "", "", ""]);
      }
    } finally {
      setLoading(false);
    }
  };

  // Password reset handlers
  const handlePasswordReset = async (e) => {
    e.preventDefault();

    const errors = validatePasswordResetForm({
      newPassword: state.newPassword,
      confirmPassword: state.confirmPassword,
      userId: state.userId
    });

    if (Object.keys(errors).length > 0) {
      const userRole = getUserRoleForAlert();
      showError("Validation Error", Object.values(errors).join('\n'), userRole);
      return;
    }

    try {
      setLoading(true);

      if (!token) {
        throw new Error("Session expired. Please login again.");
      }

      const resetResponse = await resetDefaultPassword(`Bearer ${token}`, {
        user_id: state.userId,
        old_password: state.password,
        new_password: state.newPassword
      });

      console.log("🔧 Reset password response:", resetResponse);

      if (resetResponse.responseCode === 200) {
        const userRole = getUserRoleForAlert();
        showSuccess("Password Updated Successfully", "Your password has been updated. Please log in with your new credentials.", userRole);

        setRequiresPasswordReset(false);
        setPassword("");
        setNewPassword("");
        setConfirmPassword("");
        setToken(null);
        setRole(null);
        setLoginData(null);
        setPortalValidationError("");
        
        // Navigate to login after password reset
        setTimeout(() => {
          navigate('/login', { replace: true });
        }, 2000);
      } else {
        throw new Error(resetResponse.message || "Failed to reset password");
      }
    } catch (err) {
      console.error("❌ Password reset error:", err);
      const userRole = getUserRoleForAlert();
      showError("Password Reset Failed", err.message || "Password reset failed. Please try again.", userRole);
    } finally {
      setLoading(false);
    }
  };

  // Forgot password handler
  const handleForgotPassword = async (e) => {
    e.preventDefault();

    if (!state.forgotUserId.trim()) {
      const userRole = getUserRoleForAlert();
      showWarning("User ID Required", "Please enter your User ID to reset your password.", userRole);
      return false;
    }

    try {
      setForgotLoading(true);

      const apiKey = API_CONFIG.HEADERS["x-api-key"];
      const apiSecret = API_CONFIG.HEADERS["x-api-secret"];

      const userResponse = await findUser(state.forgotUserId, apiKey, apiSecret);
      
      const userData = userResponse?.data;
      if (!userData) {
        throw new Error("User not found. Please check the User ID and try again.");
      }

      if (!userData.phoneNumber) {
        throw new Error("No phone number found for this user. Please contact system administrator.");
      }

      const forgotPasswordPayload = { user_id: state.forgotUserId };
      const resetResponse = await forgotPassword(forgotPasswordPayload, apiKey, apiSecret);

      if (resetResponse?.responseCode === 200) {
        const userRole = getUserRoleForAlert();
        showSuccess(
          "Password Reset Instructions Sent",
          `We've sent password reset instructions to your registered mobile number.`,
          userRole
        );
        setShowForgotPassword(false);
        setForgotUserId("");
        return true;
      } else {
        throw new Error(resetResponse?.message || "Failed to reset password.");
      }
    } catch (error) {
      console.error("❌ Forgot password error:", error);
      const userRole = getUserRoleForAlert();
      showError("Password Reset Failed", error.message || "An error occurred while resetting your password.", userRole);
      return false;
    } finally {
      setForgotLoading(false);
    }
  };

  // Navigation handlers
  const handleBackFromReset = () => {
    setRequiresPasswordReset(false);
    setNewPassword("");
    setConfirmPassword("");
    setToken(null);
    setRole(null);
    setLoginData(null);
    setPortalValidationError("");
    setLastAttemptedRole("");
    navigate('/login', { replace: true });
  };

  const handleBackFromOtp = () => {
    setShowOtp(false);
    setOtp(["", "", "", "", "", ""]);
    setToken(null);
    setRole(null);
    setLoginData(null);
    setResendCountdown(0);
    setCanResendOtp(false);
    setPortalValidationError("");
    setLastAttemptedRole("");
    navigate('/login', { replace: true });
  };

  const clearPortalValidationError = () => {
    setPortalValidationError("");
  };

  return {
    state,
    inputsRef,
    token,
    role,
    user,
    
    setUserId,
    setPassword,
    setShowPassword,
    setLoading,
    setShowOtp,
    setOtp,
    setLoginData,
    setUserType,
    setPortalValidationError,
    setLastAttemptedRole,
    setResendCountdown,
    setCanResendOtp,
    setRequiresPasswordReset,
    setNewPassword,
    setConfirmPassword,
    setShowNewPassword,
    setShowConfirmPassword,
    setShowForgotPassword,
    setForgotUserId,
    setForgotLoading,
    setIsMobile,
    
    handleLogin,
    handleOtpChange,
    handleOtpKeyDown,
    handleResendOtp,
    verifyOtp,
    handlePasswordReset,
    handleForgotPassword,
    handleBackFromReset,
    handleBackFromOtp,
    clearPortalValidationError,
    
    getUserRoleForAlert,
    showCustomError,
    showCustomSuccess,
    showCustomWarning,
    validatePortalAccess,
    validatePasswordResetForm
  };
};